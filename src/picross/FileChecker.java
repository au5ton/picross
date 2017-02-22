package picross;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Created by kyle on 10/5/16.
 */
public class FileChecker {
	public FileChecker() throws IOException {
		File bgusolver = new File("bgusolver.jar");
		if (! bgusolver.exists()) {
			InputStream link = (getClass().getResourceAsStream("bgusolver.jar"));
			Files.copy(link, bgusolver.getAbsoluteFile().toPath());
		}
	}
}
