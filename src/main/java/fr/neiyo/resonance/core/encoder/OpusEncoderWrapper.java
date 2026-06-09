package fr.neiyo.resonance.core.encoder;

import io.github.jaredmdobson.concentus.OpusApplication;
import io.github.jaredmdobson.concentus.OpusEncoder;
import io.github.jaredmdobson.concentus.OpusSignal;

import java.util.Arrays;

public final class OpusEncoderWrapper {

    private static final int SAMPLE_RATE = 48000;
    private static final int CHANNELS = 1;
    private static final int FRAME_SIZE = 960; // 20 ms at 48 kHz
    private static final int BITRATE = 64000; // 64 kbps
    private static final int MAX_PACKET_SIZE = 512; // Hytale protocol limit (RelayedVoiceData max 512)

    private final OpusEncoder encoder;

    public OpusEncoderWrapper() {
        this(BITRATE);
    }

    public OpusEncoderWrapper(int bitrate) {
        try {
            this.encoder = new OpusEncoder(SAMPLE_RATE, CHANNELS, OpusApplication.OPUS_APPLICATION_AUDIO);
            this.encoder.setBitrate(bitrate);
            this.encoder.setComplexity(5);
            this.encoder.setSignalType(OpusSignal.OPUS_SIGNAL_MUSIC);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Opus encoder", e);
        }
    }

    public byte[] encode(short[] pcmFrame) {
        if (pcmFrame == null || pcmFrame.length != FRAME_SIZE) {
            throw new IllegalArgumentException("Frame must be exactly " + FRAME_SIZE + " samples, got " + (pcmFrame == null ? "null" : pcmFrame.length));
        }

        try {
            byte[] output = new byte[MAX_PACKET_SIZE];
            int encodedLength = encoder.encode(pcmFrame, 0, FRAME_SIZE, output, 0, output.length);

            if (encodedLength <= 0) {
                return null;
            }

            if (encodedLength > MAX_PACKET_SIZE) {
                return null;
            }

            return Arrays.copyOf(output, encodedLength);
        } catch (Exception e) {
            return null;
        }
    }

    public byte[] encodeSilence() {
        return encode(new short[FRAME_SIZE]);
    }
}