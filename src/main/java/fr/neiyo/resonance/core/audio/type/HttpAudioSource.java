package fr.neiyo.resonance.core.audio.type;

import fr.neiyo.resonance.core.audio.AudioSource;

import javax.annotation.Nonnull;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

public final class HttpAudioSource implements AudioSource {

    private static final ConcurrentHashMap<URI, Path> CACHE = new ConcurrentHashMap<>();

    private final URI uri;
    private final AudioSource delegate;

    public HttpAudioSource(@Nonnull URI uri) {
        this.uri = uri;
        this.delegate = resolve();
    }

    private AudioSource resolve() {
        Path cached = CACHE.computeIfAbsent(uri, this::download);
        String name = cached.getFileName().toString();
        if (name.endsWith(".mp3")) return new Mp3AudioSource(cached);
        return new FileAudioSource(cached);
    }

    private Path download(URI uri) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                throw new RuntimeException("HTTP " + response.statusCode() + " for " + uri);
            }

            String contentType = response.headers().firstValue("Content-Type").orElse("");
            String ext = guessExtension(uri.getPath(), contentType);

            Path tempFile = Files.createTempFile("voicechat-audio-", ext);
            tempFile.toFile().deleteOnExit();

            try (InputStream in = new BufferedInputStream(response.body())) {
                Files.copy(in, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            return tempFile;

        } catch (Exception e) {
            throw new RuntimeException("Failed to download audio from: " + uri, e);
        }
    }

    private String guessExtension(String path, String contentType) {
        if (path.endsWith(".mp3") || contentType.contains("mpeg")) return ".mp3";
        if (path.endsWith(".ogg") || contentType.contains("ogg")) return ".ogg";
        return ".wav";
    }

    @Override
    public short[] nextFrame() {
        return delegate.nextFrame();
    }

    @Override
    public void reset() {
        delegate.reset();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }
}