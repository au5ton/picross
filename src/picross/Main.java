package picross;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class Main {
	public static Timer timer, FPSCounter;
	public static Timer animator;
	public static Graphics mainWindow;
	public static SettingsDocument prefs;

	public static void main(String[] args) {
		try {
			prefs = new SettingsDocument("prefs.txt");
		} catch(IOException e) {
			System.out.println("Could not load prefs.txt! Oh no! This is bad! Did you delete it?");
			e.printStackTrace();
		}
		try {
			TextDocument savedPuzzleTest = new TextDocument("puzzle.txt");
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

	public static void runCreator() {
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "puzzleCreator.jar");
		pb.directory(new File("."));
		try {
			Process p = pb.start();
			LogStreamReader lsr = new LogStreamReader(p.getInputStream());
			Thread thread = new Thread(lsr, "PuzzleCreatorStream");
			thread.start();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static int getNumPuzzles() {
		int i = 0;
		File savesFolder = new File(".\\saves");
		File[] saves = savesFolder.listFiles();
		if(saves != null) {
			for(File f : saves) {
				if(f.getName().contains(".nin")) {
					i++;
				}
			}
		}
		return i;
	}

	public static List<String> getPuzzleNames() {
		List<String> out = new ArrayList<>();
		File savesFolder = new File(".\\saves");
		File[] saves = savesFolder.listFiles();
		if(saves != null) {
			for(File f : saves) {
				if(f.getName().contains(".nin")) {
					out.add(f.getName());
				}
			}
		}
		return out;
	}

	public static List<TextDocument> getPuzzles() throws IOException {
		List<String> puzzleTitles = getPuzzleNames();
		List<TextDocument> out = new ArrayList<>();
		for(String puzzle : puzzleTitles) {
			out.add(new TextDocument(puzzle));
		}
		return out;
	}
}
