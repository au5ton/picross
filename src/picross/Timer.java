package picross;

import java.time.*;

public class Timer implements Runnable{
	private Duration startTime;
	private boolean running;
	public Timer() {
		running = false;
		startTime = Duration.ZERO;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true) {
			if(running)
				startTime = startTime.plusMillis(10);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public int getHours() {
		return (int) (startTime.getSeconds() / 3600.0);
	}
	public int getSeconds() {
		return (int) startTime.getSeconds();
	}
	public int getMS() {
		return (int) startTime.toMillis();
	}
	public String toString(boolean zeroes) {
		int ms = (int)startTime.toMillis();
		int seconds = ms / 1000;
		int minutes = seconds / 60;
		int hours = minutes / 60;
		seconds %= 60;
		minutes %= 60;
		String out = "";
		int numSeparators;
		if(!zeroes) {
			if(hours > 0) {
				numSeparators = 4;
			}
			else if (minutes > 0) {
				numSeparators = 3;
			}
			else if (seconds > 0){
				numSeparators = 2;
			}
			else {
				numSeparators = 1;
			}
			switch(numSeparators) {
			case 4:
				out = "" + hours + ":" + (minutes < 10 ? '0' : "") + minutes + ':' + (seconds < 10 ? '0' : "") + seconds + '.' + (ms / 10 % 100 < 10 ? '0' : "") + ms / 10 % 100;
				break;
			case 3:
				out = "" + minutes + ':' + (seconds < 10 ? '0' : "") + seconds + '.' + (ms / 10 % 100 < 10 ? '0' : "") + ms / 10 % 100;
				break;
			case 2:
				out = "" + seconds + '.' + (ms / 10 % 100 < 10 ? '0' : "") + ms / 10 % 100 + " s";
				break;
			case 1:
				out = "" + ms % 1000 + " ms";
			}
			return out;
		}
		else
			return ("" + hours + ':' + (minutes < 10 ? '0' : "") + minutes + ':' + (seconds < 10 ? '0' : "") + seconds + '.' + (ms / 10 % 100 < 10 ? '0' : "") + (ms / 10 % 100));
	}
	public String toString() {
		int ms = (int)startTime.toMillis();
		int seconds = ms / 1000;
		int minutes = seconds / 60;
		int hours = minutes / 60;
		seconds %= 60;
		minutes %= 60;
		ms /= 10;
		ms %= 100;
		return ("" + hours + ':' + (minutes < 10 ? '0' : "") + minutes + ':' + (seconds < 10 ? '0' : "") + seconds + '.' + (ms < 10 ? '0' : "") + (ms));
	}
	public void pause() {
		running = false;
	}
	public void resume() {
		running = true;
	}
	public void reset() {
		startTime = Duration.ZERO;
		running = false;
	}
	public void begin() {
		startTime = Duration.ZERO;
		running = true;
	}
	public boolean isRunning() {
		return running;
	}
}
