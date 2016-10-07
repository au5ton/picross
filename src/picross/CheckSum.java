package picross;

import java.io.FileInputStream;
import java.security.MessageDigest;

/**
 * Created on 5/15/2016 at 10:21 AM.
 */
public class CheckSum {
	public static String generate(String path) throws Exception {
		MessageDigest md = MessageDigest.getInstance("MD5");
		FileInputStream fis = new FileInputStream(path);
		byte[] data = new byte[1024];
		int read = 0;
		while((read = fis.read(data)) != -1) {
			md.update(data, 0, read);
		}
		byte[] mdBytes = md.digest();
		StringBuffer out = new StringBuffer();
		for(int i = 0; i < mdBytes.length; i++) {
			out.append(Integer.toString((mdBytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		return out.toString();
	}
}
