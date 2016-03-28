package picross;

import java.io.File;
import java.io.IOException;

public class Main {
	public static Timer timer, FPSCounter;
	public static Timer animator, fader;
	public static Graphics mainWindow;
	public static void main(String[] args) {
		FPSCounter = new Timer();
		new Thread(FPSCounter).start();
		mainWindow = new Graphics();
		new Thread(mainWindow).start();
		timer = new Timer();
		new Thread(timer).start();
		animator = new Timer();
		new Thread(animator).start();
		fader = new Timer();
		new Thread(fader).start();
	}
	public static void runSolver() {
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "bgusolver.jar", "-file", "clues.nin");
        pb.directory(new File("."));
        try {
            Process p = pb.start();
            LogStreamReader lsr = new LogStreamReader(p.getInputStream());
            Thread thread = new Thread(lsr, "LogStreamReader");
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
