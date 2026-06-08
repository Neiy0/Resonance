package fr.neiyo.resonance.core.audio;

import javax.annotation.Nullable;

public interface AudioSource {

    /**
     * Reads the next PCM frame (960 samples = 20 ms at 48 kHz mono).
     * @return a short[960] array or null if the source is finished.
     */
    @Nullable
    short[] nextFrame();

    /**
     * Resets the source to the beginning (for looped playback).
     */
    void reset();

    /**
     * Releases resources.
     */
    void close();

    /**
     * @return true if the source has more frames to read
     */
    boolean hasNext();
}