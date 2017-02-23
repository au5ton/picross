package puzzlecreator;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 4/22/2016 at 2:17 PM.
 */
public class Main {
	public static Graphics mainWindow;
	public static Timer timer;
	static char slashCharacter = File.separatorChar;

	public static void start() {
		File testFile = new File("." + slashCharacter + "resources" + slashCharacter + "bgusolver.jar");
		if (! testFile.exists()) {
			System.out.println("FATAL: bgusolver.jar not found! Please reinstall Picross.");
			System.exit(1);
		}

		mainWindow = new Graphics();
		new Thread(mainWindow).start();
		timer = new Timer();
		new Thread(timer).start();
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

	public static int getNumPuzzles() {
		/*int i = 0;
		String num; File f;
		do {
			i++;
			num = String.format("%03d", i);
			f = new File(".\\saves\\Puzzle" + num + ".nin");
		} while(f.exists());
		return --i;*/
		int numPuzzles = 0;
		File savesFolder = new File("." + slashCharacter + "saves");
		File[] saves = savesFolder.listFiles();

		for (File f : saves) {
			if (f.getName().contains(".nin")) {
				numPuzzles++;
			}
		}
		return numPuzzles;
	}

	static String getRandomString(int length) {
		String out = "";
		Random r = new Random();
		for (; length > 0; length--) {
			out += "" + (char) r.nextInt(128);
		}
		return out;
	}
}
