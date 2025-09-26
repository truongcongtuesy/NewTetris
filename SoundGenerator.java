// Simple sound effect generator for testing
// This creates basic beep sounds for game effects

import javax.sound.sampled.*;
import java.io.*;

public class SoundGenerator {
    public static void generateBeepWav(String filename, int frequency, int duration) {
        try {
            float sampleRate = 44100;
            int sampleSizeInBits = 16;
            int channels = 1;
            boolean signed = true;
            boolean bigEndian = false;
            
            AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
            
            int frames = (int) (sampleRate * duration / 1000);
            byte[] buffer = new byte[frames * 2]; // 2 bytes per frame for 16-bit
            
            // Generate sine wave
            for (int i = 0; i < frames; i++) {
                double angle = 2.0 * Math.PI * i * frequency / sampleRate;
                short sample = (short) (Math.sin(angle) * 32767 * 0.5); // 50% volume
                
                // Convert to bytes (little endian)
                buffer[i * 2] = (byte) (sample & 0xFF);
                buffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
            }
            
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            AudioInputStream audioStream = new AudioInputStream(bais, format, frames);
            
            AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, new File(filename));
            System.out.println("Generated: " + filename);
            
        } catch (Exception e) {
            System.err.println("Error generating " + filename + ": " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        // Create sounds directory structure
        new File("sounds/effects").mkdirs();
        
        // Generate basic sound effects
        generateBeepWav("sounds/effects/move.wav", 440, 100);      // A4 note, short
        generateBeepWav("sounds/effects/rotate.wav", 523, 150);   // C5 note, medium
        generateBeepWav("sounds/effects/drop.wav", 330, 200);     // E4 note, longer
        generateBeepWav("sounds/effects/line_clear.wav", 659, 300); // E5 note, success sound
        generateBeepWav("sounds/effects/pause.wav", 392, 250);    // G4 note
        generateBeepWav("sounds/effects/level_up.wav", 880, 500); // A5 note, celebration
        generateBeepWav("sounds/effects/gameover.wav", 220, 800); // A3 note, sad sound
        
        System.out.println("Sound effects generated successfully!");
    }
}