package fr.neiyo.resonance.api.audio;

import javax.annotation.Nonnull;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

public sealed interface IAudio permits IAudio.File, IAudio.Url {

    /**
     * An audio source backed by a local file on the server's filesystem.
     *
     * <p>Supported formats depend on the registered {@code AudioSource} implementations
     * (e.g. {@code .wav}, {@code .mp3}, {@code .ogg}).
     *
     * @param path the absolute or relative path to the audio file
     */
    record File(@Nonnull Path path) implements IAudio {}

    /**
     * An audio source fetched from a remote HTTP or HTTPS URL.
     *
     * <p>The file is downloaded and cached in a temporary location the first time
     * the session is created. The format is inferred from the URL path or the
     * {@code Content-Type} response header.
     *
     * @param uri the URI of the remote audio resource
     */
    record Url(@Nonnull URI uri) implements IAudio {}

    /**
     * Creates an {@code IAudio} referencing a local file.
     *
     * @param path the path to the audio file on the server's filesystem
     * @return a {@link File} instance wrapping the given path
     */
    static IAudio of(@Nonnull Path path) {
        return new File(path);
    }

    /**
     * Creates an {@code IAudio} referencing a remote audio resource.
     *
     * @param uri the URI of the remote audio resource
     * @return a {@link Url} instance wrapping the given URI
     */
    static IAudio of(@Nonnull URI uri) {
        return new Url(uri);
    }

    /**
     * Creates an {@code IAudio} referencing a remote audio resource from a URL string.
     *
     * <p>This is a convenience overload for {@link #of(URI)} that parses the string first.
     *
     * @param url the URL string of the remote audio resource
     * @return a {@link Url} instance wrapping the parsed URI
     * @throws IllegalArgumentException if {@code url} is not a valid URI
     */
    static IAudio fromUrl(@Nonnull String url) {
        try {
            return new Url(new URI(url));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }
}