/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import javax.sound.sampled.*;
import java.io.*;

/**
 * SoundEngine.java
 * Loads and plays real .wav sound files for the Hangman game.
 *
 * @author Rim
 */
public class SoundEngine {

    
      // Once a correct guess is done, it plays the applause sound as a reward sound .
    
    public static void playApplause() {
        playSound("applause.wav");
    }

    /**
     * Once an incorrect guess is done, it plays the wrong/fail sound as.
     */
    public static void playWrong() {
        playSound("wrong.wav");
    }

 
     // Loads and plays a .wav file from the src/ folder.

    private static void playSound(String filename) {
        new Thread(() -> {
            try {
                // getResourceAsStream always finds the file
                InputStream is = SoundEngine.class.getResourceAsStream(filename);

                if (is == null) {
                    System.out.println("Sound file not found: " + filename
                        + " make sure it is in the src/ folder.");
                    return;
                }

                // Wrap in BufferedInputStream so AudioSystem can mark/reset
                AudioInputStream audioStream =
                    AudioSystem.getAudioInputStream(new BufferedInputStream(is));

                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();

                // Wait for the clip to finish before releasing resources
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                        try { audioStream.close(); } catch (IOException ignored) {}
                    }
                });

            } catch (UnsupportedAudioFileException e) {
                System.out.println("Unsupported audio format for: " + filename
                    + " make sure it is a standard PCM .wav file.");
            } catch (LineUnavailableException e) {
                System.out.println("Audio line unavailable: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("Error reading sound file: " + e.getMessage());
            }
        }).start();
    }
}
