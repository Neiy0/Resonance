package fr.neiyo.resonance.core;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.universe.world.World;
import fr.neiyo.resonance.api.IResonanceManager;
import fr.neiyo.resonance.api.audio.IAudio;
import fr.neiyo.resonance.api.session.IAudioSession;
import fr.neiyo.resonance.core.session.AudioSession;
import fr.neiyo.resonance.core.session.PlayerAudioSession;
import fr.neiyo.resonance.core.session.WorldAudioSession;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class ResonanceManager implements IResonanceManager, Runnable {

    private final Map<String, AudioSession> sessions = new ConcurrentHashMap<>();

    public ResonanceManager() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "Resonance-Scheduler");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(this, 0, 20, TimeUnit.MILLISECONDS);
    }

    @Override
    public IAudioSession create(@Nonnull String id, @Nonnull IAudio audio, float volume, boolean loop) {
        if (sessions.containsKey(id)) {
            throw new IllegalArgumentException("Audio session with ID '" + id + "' already exists.");
        }

        AudioSession session = new PlayerAudioSession(id, audio, volume, loop);
        sessions.put(id, session);
        return session;
    }

    @Override
    public IAudioSession create(@Nonnull String id, @Nonnull IAudio audio, @Nonnull World world, @Nonnull Transform transform, float volume, boolean loop) {
        if (sessions.containsKey(id)) {
            throw new IllegalArgumentException("Audio session with ID '" + id + "' already exists.");
        }

        AudioSession session = new WorldAudioSession(id, audio, world, transform, volume, loop);
        sessions.put(id, session);
        return session;
    }

    @Override
    public IAudioSession get(@Nonnull String id) {
        return sessions.get(id);
    }

    @Override
    public void stop(@Nonnull String id) {
        AudioSession session = sessions.remove(id);
        if (session != null) {
            session.clear();
        }
    }

    @Override
    public void run() {
        for (AudioSession session : sessions.values()) {
            session.tick();
        }
    }
}
