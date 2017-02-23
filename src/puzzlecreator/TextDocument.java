package puzzlecreator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created on 4/12/2016 at 9:44 PM.
 */
@SuppressWarnings("SameParameterValue")
class TextDocument {
	private final String locationStr;
	private Scanner s;
	private final List<String> contents;
	@SuppressWarnings("CanBeFinal")
	private HashMap<String, String> parsedContents;

	public TextDocument(String path) throws IOException {
		contents = new ArrayList<>();
		parsedContents = new HashMap<>();
		locationStr = path;
		String temp;
		s = new Scanner(new File(path));
		while (s.hasNext()) {
			temp = s.nextLine();
			if (temp.length() > 1 && temp.indexOf('=') > 0)
				contents.add(temp);
		}
	}

	public TextDocument(String path, boolean isSettings) throws IOException {
		contents = new ArrayList<>();
		parsedContents = new HashMap<>();
		locationStr = path;
		if (! new File(path).exists()) {
			FileWriter makeDoc = new FileWriter(new File(path));
			makeDoc.close();
		}
		String temp;
		s = new Scanner(new File(path));
		while (s.hasNext()) {
			temp = s.nextLine();
			if (temp.length() > 1 && temp.indexOf('=') > 0)
				contents.add(temp);
		}
		if (isSettings) {
			contents.stream().filter(line -> line.length() > 2).forEach(line -> {
				String label = line.substring(0, line.indexOf('='));
				String setting = line.substring(line.indexOf('=') + 1);
				parsedContents.put(label, setting);
			});
		}
	}

	public String get(String key) throws NullPointerException {
		return parsedContents.get(key);
	}

	public String get(int pos) throws NullPointerException {
		return contents.get(pos);
	}

	public boolean has(String key) {
		return parsedContents.containsKey(key);
	}

	public boolean has(int pos) {
		return contents.size() > pos;
	}

	public void put(String k, String v) {
		parsedContents.put(k, v);
	}

	@SuppressWarnings("unchecked")
	public void close() throws IOException {
		FileWriter fw = new FileWriter(new File(locationStr));
		Set everything = parsedContents.entrySet();
		Map.Entry<String, String> things[] = new Map.Entry[everything.size()];
		things = (Map.Entry<String, String>[]) everything.toArray(things);
		s.reset();
		for (Map.Entry<String, String> thing : things) {
			System.out.println("Writing \"" + thing.getKey() + "=" + thing.getValue() + "\" to " + locationStr);
			fw.write(thing.getKey() + "=" + thing.getValue() + '\n');
		}
		fw.flush();
		fw.close();
	}
}