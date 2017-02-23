package puzzleCreator;

import javax.smartcardio.CardTerminal;

/**
 * Created on 5/15/2016 at 11:02 AM.
 */
public class CryptoException extends Exception {
	public CryptoException() {

	}

	public CryptoException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
