package fr.neiyo.resonance.api.session;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import fr.neiyo.resonance.api.IResonanceManager;

import javax.annotation.Nonnull;
import java.util.Collection;

public interface IAudioSession {

    /**
     * Returns the unique identifier of this session.
     *
     * @return the session ID, never {@code null}
     */
    @Nonnull String getId();

    /**
     * Pauses or resumes this session.
     *
     * <p>When paused, the audio stream is temporarily halted but the session remains active.
     * Audiences are still registered and can be resumed later. When resumed, playback continues
     * from the current position.
     *
     * @param pause {@code true} to pause the session, {@code false} to resume if currently paused
     */
    void pause(boolean pause);

    /**
     * Returns whether this session is currently paused.
     *
     * @return {@code true} if the session is paused, {@code false} if it is playing or stopped
     */
    boolean isPaused();

    /**
     * Stops and terminates this session permanently.
     *
     * <p>Once stopped, the session cannot be resumed. All audiences are removed
     * and any associated resources are released. This is equivalent to calling
     * {@link IResonanceManager#stop(String)} with this session's ID.
     */
    void stop();

    /**
     * Sets the playback volume for this session.
     *
     * @param volume the desired volume level, typically in the range {@code [0.0, 1.0]}
     *               where {@code 0.0} is silent and {@code 1.0} is full volume
     */
    void setVolume(float volume);

    /**
     * Returns the current playback volume of this session.
     *
     * @return the current volume level
     */
    float getVolume();

    /**
     * Sets whether this session should loop playback.
     *
     * <p>When looping is enabled, the audio restarts automatically once it reaches
     * the end. Changes take effect on the next loop boundary.
     *
     * @param loop {@code true} to enable looping, {@code false} to play once and stop
     */
    void setLooping(boolean loop);

    /**
     * Returns whether this session is configured to loop.
     *
     * @return {@code true} if looping is enabled, {@code false} otherwise
     */
    boolean isLooping();

    /**
     * Adds a player to this session's audience list.
     *
     * <p>The player will start receiving the audio stream on the next applicable
     * network tick. If the player is already an audience, this method has no effect.
     *
     * @param playerRef a reference to the player to add, must not be {@code null}
     */
    void addAudience(@Nonnull PlayerRef playerRef);

    /**
     * Removes a player from this session's audience list.
     *
     * <p>The player will stop receiving the audio stream. If the player is not
     * currently an audience, this method has no effect.
     *
     * @param playerRef a reference to the player to remove, must not be {@code null}
     */
    void removeAudience(@Nonnull PlayerRef playerRef);

    /**
     * Returns a snapshot of the current audience list.
     *
     * <p>The returned collection is a view or copy of the internal state at the time
     * of the call. Modifications to the returned collection do not affect session state.
     *
     * @return an unmodifiable collection of {@link PlayerRef} currently in this session,
     *         never {@code null}, may be empty
     */
    @Nonnull Collection<PlayerRef> getAudiences();
}