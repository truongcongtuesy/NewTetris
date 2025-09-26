import javax.sound.sampled.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {
    private Map<String, Clip> soundClips = new HashMap<>();
    private Map<String, Clip> musicClips = new HashMap<>();
    private Clip currentBackgroundMusic;
    private float musicVolume = 0.7f;
    private float effectsVolume = 0.8f;
    private boolean musicEnabled = true;
    private boolean soundEnabled = true;
    
    public SoundManager() {
        loadSounds();
        loadMusic();
    }
    
    private void loadSounds() {
        // Load sound effects
        String[] effectFiles = {"move", "rotate", "drop", "clear", "gameover"};
        for (String effect : effectFiles) {
            try {
                File soundFile = new File("sounds/effects/" + effect + ".wav");
                if (soundFile.exists()) {
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioStream);
                    soundClips.put(effect, clip);
                }
            } catch (Exception e) {
                System.out.println("Could not load sound effect: " + effect + ".wav");
            }
        }
    }
    
    private void loadMusic() {
        // Load music files
        String[] musicFiles = {"background", "menu", "gameover", "pause"};
        for (String music : musicFiles) {
            try {
                File musicFile = new File("sounds/music/" + music + ".wav");
                if (musicFile.exists()) {
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioStream);
                    musicClips.put(music, clip);
                }
            } catch (Exception e) {
                System.out.println("Could not load music: " + music + ".wav");
            }
        }
    }
    
    public void playSound(String soundName) {
        if (!soundEnabled) return;
        
        Clip clip = soundClips.get(soundName);
        if (clip != null) {
            clip.setFramePosition(0); // Rewind to beginning
            setClipVolume(clip, effectsVolume);
            clip.start();
        } else {
            // Fallback to system beep
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }
    
    public void playBackgroundMusic(String musicName, float volume) {
        if (!musicEnabled) return;
        
        stopBackgroundMusic();
        
        Clip clip = musicClips.get(musicName);
        if (clip != null) {
            currentBackgroundMusic = clip;
            clip.setFramePosition(0);
            setClipVolume(clip, volume);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        }
    }
    
    public void stopBackgroundMusic() {
        if (currentBackgroundMusic != null && currentBackgroundMusic.isRunning()) {
            currentBackgroundMusic.stop();
            currentBackgroundMusic = null;
        }
    }
    
    public void pauseBackgroundMusic() {
        if (currentBackgroundMusic != null && currentBackgroundMusic.isRunning()) {
            currentBackgroundMusic.stop();
        }
    }
    
    public void resumeBackgroundMusic() {
        if (currentBackgroundMusic != null && !currentBackgroundMusic.isRunning()) {
            currentBackgroundMusic.start();
        }
    }
    
    private void setClipVolume(Clip clip, float volume) {
        try {
            FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float range = volumeControl.getMaximum() - volumeControl.getMinimum();
            float gain = (range * volume) + volumeControl.getMinimum();
            volumeControl.setValue(gain);
        } catch (Exception e) {
            // Volume control not supported
            System.out.println("Volume control not supported for this audio clip");
        }
    }
    
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0.0f, Math.min(1.0f, volume));
        if (currentBackgroundMusic != null) {
            setClipVolume(currentBackgroundMusic, this.musicVolume);
        }
    }
    
    public void setEffectsVolume(float volume) {
        this.effectsVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }
    
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled) {
            stopBackgroundMusic();
        }
    }
    
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }
    
    public float getMusicVolume() {
        return musicVolume;
    }
    
    public float getEffectsVolume() {
        return effectsVolume;
    }
    
    public boolean isMusicEnabled() {
        return musicEnabled;
    }
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    public void cleanup() {
        stopBackgroundMusic();
        
        // Close all clips
        for (Clip clip : soundClips.values()) {
            if (clip != null) {
                clip.close();
            }
        }
        
        for (Clip clip : musicClips.values()) {
            if (clip != null) {
                clip.close();
            }
        }
        
        soundClips.clear();
        musicClips.clear();
    }
}