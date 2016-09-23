package picross;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Main {
	public static Timer timer, FPSCounter;
	public static Timer animator;
	public static Graphics mainWindow;
	public static SettingsDocument prefs;
	/**
	 * This is used to accommodate other file systems by changing to a forward slash if that is the preferred character.
	 */
	public static char slashCharacter = '\\';

	public static void main(String[] args) {
		File test = new File("." + slashCharacter + "bgusolver.jar");//uses bgusolver as test because the file is required anyway
		if (!test.exists()) {
			System.out.println("Using \'/\' for slash character in this file system.");
			slashCharacter = '/';
			test = new File("." + slashCharacter + "bgusolver.jar");
			if(!test.exists()) {
				System.out.println("FATAL: bgusolver.jar not detected! Please put it back or reinstall Picross.");
			}
		}
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

	public static void runSolver(String fileName) {
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", "bgusolver.jar", "-file", fileName);
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
		File savesFolder = new File("." + slashCharacter + "saves");
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
		File savesFolder = new File("." + slashCharacter + "saves");
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
	public static List<String> getPuzzleSizes() {
		List<String> out = new ArrayList<>();
		File savesFolder = new File("." + slashCharacter + "saves");
		File[] saves = savesFolder.listFiles();
		if(saves != null) {
			for(File f : saves) {
				if(f.getName().contains(".nin")) {
					try {
						Scanner s = new Scanner(f);
						if (s.hasNext()) {
							String size = s.next();
							int spaceLoc = size.indexOf(' ');
							out.add(size.substring(0, spaceLoc) + "x" + size.substring(spaceLoc + 1, size.length()));
						}
					} catch(IOException e) {
						e.printStackTrace();
					}
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
