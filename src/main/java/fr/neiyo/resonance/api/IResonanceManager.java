package fr.neiyo.resonance.api;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.universe.world.World;
import fr.neiyo.resonance.api.audio.IAudio;
import fr.neiyo.resonance.api.session.IAudioSession;

import javax.annotation.Nonnull;

public interface IResonanceManager {

    /**
     * Creates a new global (non-positional) audio session.
     *
     * <p>The audio is played at a fixed volume for all audiences, with no
     * spatial attenuation applied.
     *
     * @param id     the unique identifier for this session, must not be {@code null}
     * @param audio  the audio source to play, must not be {@code null}
     * @param volume the initial playback volume, typically in the range {@code [0.0, 1.0]}
     * @param loop   {@code true} to loop the audio indefinitely, {@code false} to play once
     * @return the newly created {@link IAudioSession}, never {@code null}
     */
    IAudioSession create(@Nonnull String id, @Nonnull IAudio audio, float volume, boolean loop);

    /**
     * Creates a new positional audio session anchored to a location in the world.
     *
     * <p>The audio source is spatially placed at the given {@link Transform} within
     * the specified {@link World}. Audiences receive distance-based volume attenuation
     * and directional audio rendering according to the client-side audio engine configuration.
     *
     * @param id        the unique identifier for this session, must not be {@code null}
     * @param audio     the audio source to play, must not be {@code null}
     * @param world     the world in which the audio is spatially placed, must not be {@code null}
     * @param transform the position and orientation of the audio emitter in the world,
     *                  must not be {@code null}
     * @param volume    the initial playback volume before distance attenuation is applied,
     *                  typically in the range {@code [0.0, 1.0]}
     * @param loop      {@code true} to loop the audio indefinitely, {@code false} to play once
     * @return the newly created {@link IAudioSession}, never {@code null}
     */
    IAudioSession create(@Nonnull String id, @Nonnull IAudio audio, @Nonnull World world, @Nonnull Transform transform, float volume, boolean loop);

    /**
     * Retrieves the audio session with the given ID.
     *
     * @param id the unique identifier of the session to retrieve, must not be {@code null}
     * @return the {@link IAudioSession} with the specified ID, or {@code null} if no such session exists
     */
    IAudioSession get(@Nonnull String id);

    /**
     * Stops and removes the session with the given ID.
     *
     * <p>This is equivalent to calling {@link IAudioSession#stop()} on the session
     * directly. All audiences are disconnected and resources are released.
     * If no session with the given ID exists, this method has no effect.
     *
     * @param id the unique identifier of the session to stop, must not be {@code null}
     */
    void stop(@Nonnull String id);
}