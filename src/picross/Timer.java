package picross;

import java.time.*;

public class Timer implements Runnable{
	private Duration startTime;
	private boolean running;
	public Timer() {
		running = true;
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
	public int getTime() {
		return (int) startTime.getSeconds();
	}
	public String toString() {
		int ms = (int)startTime.toMillis();
		int seconds = ms / 1000;
		int minutes = seconds / 60;
		int hours = minutes / 60;
		return ("" + hours + ':' + (minutes % 60 < 10 ? '0' : "") + minutes % 60 + ':' + (seconds % 60 < 10 ? '0' : "") + seconds % 60 + '.' + (ms / 10 % 100 < 10 ? '0' : "") + (ms / 10 % 100));
	}
	public void pause() {
		running = false;
	}
	public void resume() {
		running = true;
	}
}
