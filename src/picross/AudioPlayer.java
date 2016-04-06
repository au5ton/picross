package picross;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

public class AudioPlayer {
	private Clip clip;
	private static boolean play = true;

	public AudioPlayer(int loops, String name) {
		if(play) {
			try {
				URL url = this.getClass().getClassLoader().getResource("resources/" + name + ".wav");
				AudioInputStream aIn = AudioSystem.getAudioInputStream(url);
				clip = AudioSystem.getClip();
				clip.open(aIn);
				clip.start();
				clip.loop(loops);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	void stop() {
		if(play) {
			try {
				clip.stop();
			} catch(NullPointerException e) {
				e.printStackTrace();
			}
		}
	}
	void start(int loops) {
		if(play) {
			try {
				clip.start();
				clip.loop(loops);
			} catch(NullPointerException e) {
				e.printStackTrace();
			}
		}
	}
}
