package fr.neiyo.resonance.core.session;

import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.io.ChannelConnection;
import com.hypixel.hytale.protocol.packets.stream.StreamType;
import com.hypixel.hytale.protocol.packets.voice.RelayedVoiceData;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import fr.neiyo.resonance.api.ResonanceProvider;
import fr.neiyo.resonance.api.audio.IAudio;
import fr.neiyo.resonance.api.session.IAudioSession;
import fr.neiyo.resonance.core.audio.AudioSource;
import fr.neiyo.resonance.core.audio.AudioSourceFactory;
import fr.neiyo.resonance.core.encoder.OpusEncoderWrapper;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AudioSession implements IAudioSession {

    protected static final AtomicInteger ID_COUNTER = new AtomicInteger(-1000);
    protected static final OpusEncoderWrapper ENCODER = new OpusEncoderWrapper();

    protected final int virtual_entity_id = ID_COUNTER.getAndDecrement();
    protected final UUID virtual_entity_uuid = UUID.randomUUID();

    protected final String id;
    protected final AudioSource source;

    protected final Set<UUID> audiences = new HashSet<>();
    protected boolean paused;
    protected float volume;
    protected boolean looping;

    protected final AtomicInteger sequenceNumber = new AtomicInteger(0);
    protected long startTimestamp = -1;

    public AudioSession(@Nonnull String id, @Nonnull IAudio audio, float volume, boolean loop) {
        this.id = id;
        this.source = AudioSourceFactory.create(audio);

        this.paused = false;
        this.volume = volume;
        this.looping = loop;
    }

    @Nonnull
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void pause(boolean pause) {
        this.paused = pause;
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public void stop() {
        ResonanceProvider.get().stop(id);
    }

    @Override
    public void setVolume(float volume) {
        this.volume = Math.max(0f, Math.min(1f, volume));
    }

    @Override
    public float getVolume() {
        return volume;
    }

    @Override
    public void setLooping(boolean loop) {
        this.looping = loop;
    }

    @Override
    public boolean isLooping() {
        return looping;
    }

    @Override
    public void addAudience(@Nonnull PlayerRef playerRef) {
        audiences.add(playerRef.getUuid());
    }

    @Override
    public void removeAudience(@Nonnull PlayerRef playerRef) {
        audiences.remove(playerRef.getUuid());
    }

    @Nonnull
    @Override
    public Collection<PlayerRef> getAudiences() {
        Set<PlayerRef> refs = new HashSet<>(audiences.size());
        for (UUID uuid : audiences) {
            PlayerRef ref = Universe.get().getPlayer(uuid);
            if (ref != null && ref.isValid()) {
                refs.add(ref);
            }
        }
        return refs;
    }

    public void clear() {
        pause(true);
        audiences.clear();
    }

    public void tick() {
        if (isPaused()) return;

        Collection<PlayerRef> players = getAudiences();
        if (players.isEmpty()) return;

        if (startTimestamp < 0) startTimestamp = System.currentTimeMillis();

        short[] pcmFrame = source.nextFrame();
        if (pcmFrame == null) {
            if (looping) {
                source.reset();
                pcmFrame = source.nextFrame();
            }
            if (pcmFrame == null) {
                stop();
                return;
            }
        }

        if (volume < 1.0f) {
            for (int i = 0; i < pcmFrame.length; i++) {
                pcmFrame[i] = (short) (pcmFrame[i] * volume);
            }
        }

        byte[] opusData = ENCODER.encode(pcmFrame);
        if (opusData == null || opusData.length > 512) return;

        short seq = (short) (sequenceNumber.getAndIncrement() & 0xFFFF);
        int timestamp = (int) (System.currentTimeMillis() - startTimestamp);

        for (PlayerRef player : players) {
            sendToPlayer(player, opusData, seq, timestamp);
        }
    }

    protected void sendToPlayer(@Nonnull PlayerRef player, byte[] opusData, short seq, int timestamp) {
        PacketHandler handler = player.getPacketHandler();

        ChannelConnection voiceChannel = handler.getChannel(StreamType.Voice);
        if (voiceChannel == null || !voiceChannel.isActive()) return;

        Vector3d pos = player.getTransform().getPosition();
        Position position = new Position(pos.x(), pos.y(), pos.z());

        RelayedVoiceData packet = new RelayedVoiceData();
        packet.speakerId = virtual_entity_uuid;
        packet.entityId = virtual_entity_id;
        packet.sequenceNumber = seq;
        packet.timestamp = timestamp;
        packet.speakerPosition = position;
        packet.speakerIsUnderwater = false;
        packet.opusData = opusData;

        voiceChannel.writeAndFlush(packet);
    }
}
