package fr.neiyo.resonance.core.session;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.io.ChannelConnection;
import com.hypixel.hytale.protocol.packets.stream.StreamType;
import com.hypixel.hytale.protocol.packets.voice.RelayedVoiceData;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import fr.neiyo.resonance.api.audio.IAudio;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.UUID;

public final class WorldAudioSession extends AudioSession {

    private static final int MAX_RANGE = 32;

    private final World world;
    private final Transform transform;

    public WorldAudioSession(@Nonnull String id, @Nonnull IAudio audio, @Nonnull World world, @Nonnull Transform transform, float volume, boolean loop) {
        super(id, audio, volume, loop);
        this.world = world;
        this.transform = transform;
    }

    @Override
    protected void sendToPlayer(@Nonnull PlayerRef player, byte[] opusData, short seq, int timestamp) {
        UUID playerWorld = player.getWorldUuid();
        if (playerWorld == null) return;

        World world = Universe.get().getWorld(playerWorld);
        if (world == null) return;

        if (!world.equals(this.world)) return;

        PacketHandler handler = player.getPacketHandler();

        ChannelConnection voiceChannel = handler.getChannel(StreamType.Voice);
        if (voiceChannel == null || !voiceChannel.isActive()) return;

        Vector3d vector3d = transform.getPosition();
        Position position = new Position(vector3d.x(), vector3d.y(), vector3d.z());

        if (isTooFar(player, vector3d)) return;

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

    private boolean isTooFar(PlayerRef player, Vector3d transformVector) {
        Transform playerTransform = player.getTransform();
        Vector3d playerPos = playerTransform.getPosition();
        return playerPos.distance(transformVector) > MAX_RANGE;
    }
}
