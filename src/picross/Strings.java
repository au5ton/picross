package picross;

import java.util.List;

public class Strings {
	public static int findLineWith(List<String> array, String search, boolean caseSensitive) {
		for(int i = 0; i < array.size(); i++) {
			if(contains(array.get(i), search, caseSensitive)) {
				return i;
			}
		}
		return -1;
	}
	public static boolean contains(String s, String search) {
		if(search.length() > s.length())
			return false;
		for(int i = 0; i < s.length(); i++) {
			if(s.substring(i, i + search.length()).equals(search)) {
				return true;
			}
		}
		return false;
	}
	public static boolean contains(String s, String search, boolean caseSensitive) {
		if(!caseSensitive) {
			s = s.toLowerCase();
			search = search.toLowerCase();
		}
		if(search.length() > s.length())
			return false;
		for(int i = 0; i < s.length(); i++) {
			if(i + search.length() < s.length() && s.substring(i, i + search.length()).equals(search)) {
				return true;
			}
		}
		return false;
	}
}
