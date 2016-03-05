package picross;

import java.io.File;
import java.io.IOException;

public class Main {
	public static Timer timer;
	public static Graphics mainWindow;
	public static void main(String[] args) {
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
}
