package fr.neiyo.resonance.core.session;

import fr.neiyo.resonance.api.audio.IAudio;

import javax.annotation.Nonnull;

public final class PlayerAudioSession extends AudioSession {

    public PlayerAudioSession(@Nonnull String id, @Nonnull IAudio audio, float volume, boolean loop) {
        super(id, audio, volume, loop);
    }

}
