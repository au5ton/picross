package picross;

import java.io.File;
import java.io.IOException;

class Main {
	public static Timer timer, FPSCounter;
	public static Timer animator;
	public static Graphics mainWindow;
	public static TextDocument prefs;

	public static void main(String[] args) {
		try {
			prefs = new TextDocument("prefs.txt", true);
		} catch(IOException e) {
			System.out.println("Could not load prefs.txt! Oh no! This is bad! Did you delete it, you monster?");
			e.printStackTrace();
		}
		try {
			TextDocument savedPuzzleTest = new TextDocument("puzzle.txt", false);
		} catch(IOException e) {
			e.printStackTrace();
		}
		System.out.println(prefs.get("volume"));
		FPSCounter = new Timer();
		new Thread(FPSCounter).start();
		mainWindow = new Graphics();
		new Thread(mainWindow).start();
		timer = new Timer();
		new Thread(timer).start();
		animator = new Timer();
		new Thread(animator).start();
	}

	public static void runSolver() {
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "bgusolver.jar", "-file", "clues.nin");
		pb.directory(new File("."));
		try {
			Process p = pb.start();
			LogStreamReader lsr = new LogStreamReader(p.getInputStream());
			Thread thread = new Thread(lsr, "LogStreamReader");
			thread.start();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}
