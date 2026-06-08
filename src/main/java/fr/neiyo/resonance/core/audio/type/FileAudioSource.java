package fr.neiyo.resonance.core.audio.type;

import fr.neiyo.resonance.core.audio.AudioSource;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;

public final class FileAudioSource implements AudioSource {

    private static final int SAMPLE_RATE = 48000;
    private static final int FRAME_SIZE = 960;
    private static final AudioFormat TARGET_FORMAT = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            SAMPLE_RATE,
            16,
            1,
            2,
            SAMPLE_RATE,
            false
    );

    private final Path filePath;
    private byte[] pcmData;
    private int position;

    public FileAudioSource(Path filePath) {
        this.filePath = filePath;
        this.position = 0;
        loadAndConvert();
    }

    private void loadAndConvert() {
        try {
            AudioInputStream originalStream = AudioSystem.getAudioInputStream(filePath.toFile());
            AudioFormat originalFormat = originalStream.getFormat();

            AudioInputStream convertedStream;
            if (!originalFormat.matches(TARGET_FORMAT)) {
                if (originalFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED &&
                        originalFormat.getEncoding() != AudioFormat.Encoding.PCM_UNSIGNED) {
                    AudioFormat decodedFormat = new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            originalFormat.getSampleRate(),
                            16,
                            originalFormat.getChannels(),
                            originalFormat.getChannels() * 2,
                            originalFormat.getSampleRate(),
                            false
                    );
                    originalStream = AudioSystem.getAudioInputStream(decodedFormat, originalStream);
                }
                convertedStream = AudioSystem.getAudioInputStream(TARGET_FORMAT, originalStream);
            } else {
                convertedStream = originalStream;
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = convertedStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            pcmData = outputStream.toByteArray();
            convertedStream.close();

        } catch (Exception e) {
            throw new RuntimeException("Failed to load audio file: " + filePath, e);
        }
    }

    @Override
    @Nullable
    public short[] nextFrame() {
        if (!hasNext()) return null;

        short[] frame = new short[FRAME_SIZE];
        int bytesAvailable = Math.min(FRAME_SIZE * 2, pcmData.length - position);
        ByteBuffer bb = ByteBuffer.wrap(pcmData, position, bytesAvailable);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        int samplesRead = bytesAvailable / 2;
        for (int i = 0; i < samplesRead; i++) {
            frame[i] = bb.getShort();
        }

        position += FRAME_SIZE * 2;
        return frame;
    }

    @Override
    public void reset() {
        position = 0;
    }

    @Override
    public void close() {
        pcmData = null;
    }

    @Override
    public boolean hasNext() {
        return pcmData != null && position < pcmData.length;
    }
}