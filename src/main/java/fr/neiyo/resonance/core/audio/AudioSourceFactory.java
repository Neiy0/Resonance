package fr.neiyo.resonance.core.audio;

import fr.neiyo.resonance.api.audio.IAudio;
import fr.neiyo.resonance.core.audio.type.FileAudioSource;
import fr.neiyo.resonance.core.audio.type.HttpAudioSource;
import fr.neiyo.resonance.core.audio.type.Mp3AudioSource;

import javax.annotation.Nonnull;
import java.nio.file.Path;

public final class AudioSourceFactory {

    public static AudioSource create(IAudio clip) {
        return switch (clip) {
            case IAudio.File file -> resolveFile(file.path());
            case IAudio.Url url -> new HttpAudioSource(url.uri());
        };
    }

    private static AudioSource resolveFile(@Nonnull Path path) {
        String name = path.getFileName().toString().toLowerCase();
        if (name.endsWith(".mp3")) return new Mp3AudioSource(path);
        if (name.endsWith(".wav") || name.endsWith(".ogg")) return new FileAudioSource(path);
        throw new IllegalArgumentException("Unsupported audio format: " + name);
    }
}