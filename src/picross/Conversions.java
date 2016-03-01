package picross;

public class Conversions {
	public static int ctoi(char c) {//directly converts one character into a single-digit integer
		if((int)c >= 48 && (int)c <= 57)
			return (int)c - 48;
		else if(c != '-')
			return -1;
		else
			return -2;
	}
	public static int stoi(String s) {//combines ctoi and pow to return an integer conversion of s
		boolean isNegative = false;
		for(int i = 0; i < s.length(); i++) {
			if(ctoi(s.charAt(i)) == -1)
				return -1;
			else if(ctoi(s.charAt(i)) == -2)
				isNegative = true;
		}
		int out = 0;
		for(int i = (isNegative ? 1 : 0); i < s.length(); i++) {
			out += ctoi(s.charAt(i)) * pow(10, s.length() - i - 1);
		}
		if(isNegative)
			out -= out * 2;
		return out;
	}
	public static int pow(int n, int p) {//returns n^p
		int out = n;
		if(p < 0) {
			return 1 / pow(n, -p);
		}
		else if(p == 0)
			return 1;
		else {
			for(int i = 1; i < p; i++) {
				out *= n;
			}
		}
		return out;
	}
	public static int intLen(int n) {
		if(n == 0) return 1;
		int i = 0;
		while(n != 0) {
			i++;
			n /= 10;
		}
		return i;
	}
	public static String itos(int n) {
		String out = "";
		if(n < 0) {
			out += "-";
			n -= n * 2;
		}
		for(int i = intLen(n); i > 0; i--) {
			out += "" + (char)(n / pow(10, i - 1) + 48);
			n -= (n / pow(10, i - 1));
		}
		return out;
	}
}
