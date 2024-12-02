package com.svx.chess.utility;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundUtility {

    private static void playSound(String soundFileName) {
        try {
            String path = "/com/svx/chess/chess_sfx/" + soundFileName + ".wav";

            URL soundURL = SoundUtility.class.getResource(path);
            if (soundURL == null) {
                System.err.println("Sound file not found: " + path);
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);

            Clip clip = AudioSystem.getClip();

            clip.open(audioStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error playing sound: " + e.getMessage());
        }
    }

    public enum SoundType {
        CAPTURE("capture"),
        CASTLE("castle"),
        CHECK("check"),
        CHECKMATE("finish"),
        MOVE("move"),
        START("start");

        private final String soundFileName;

        SoundType(String soundFileName) {
            this.soundFileName = soundFileName;
        }

        public void play() {
            SoundUtility.playSound(soundFileName);
        }
    }
}