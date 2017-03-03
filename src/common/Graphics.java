package common;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;

/**
 * @author onContentStop
 */
public class Graphics implements Runnable, WindowListener, WindowFocusListener {
	public final int WINDOW_BAR_HEIGHT = 30;
	public boolean running, done, visible;
	public int width = 800, height = 600;
	public BetterFrame frame;
	public Image imgBuffer;
	public Font f;
	public Graphics2D graphics2D;

	public Graphics() {
		frame = new BetterFrame("Frame", new Dimension(width, height));
		initializeCommonVariables();
	}

	public Graphics(String title) {
		frame = new BetterFrame(title, new Dimension(width, height));
		initializeCommonVariables();
	}

	private void initializeCommonVariables() {
		running = true;
		done = false;
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int mon_width = gd.getDisplayMode().getWidth();
		int mon_height = gd.getDisplayMode().getHeight();
		frame.setLocation(mon_width / 2 - width / 2, mon_height / 2 - height / 2);
		frame.addWindowListener(this);
		frame.addWindowFocusListener(this);
		visible = false;
	}

	@Override
	public void run() {
		while (running) {
			runActions();
		}
	}

	protected void runActions() {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void updateSize() {
		if (width != frame.getWidth())
			width = frame.getWidth();
		if (height != frame.getHeight())
			height = frame.getHeight();
	}

	public void startDraw() {
		graphics2D = (Graphics2D) imgBuffer.getGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics2D.setFont(new Font("Arial", Font.PLAIN, 50));
		f = graphics2D.getFont();
	}

	public void endDraw() {
		graphics2D = (Graphics2D) frame.getGraphics();
		if (graphics2D != null) {
			imgBuffer = Resizer.PROGRESSIVE_BILINEAR.resize((BufferedImage) imgBuffer, width, height);
			graphics2D.drawImage(imgBuffer, 0, 0, width, height, 0, 0, width, height, null);
			graphics2D.dispose();
		}
	}


	@Override
	public void windowClosing(WindowEvent e) {
		frame.setVisible(false);
		running = false;
		frame.dispose();
		done = true;
	}

	@Override
	public void windowClosed(WindowEvent e) {
		if (done)
			System.exit(0);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			//e1.printStackTrace();
		}
	}

	public BetterFrame getFrame() {
		return frame;
	}

	public void setVisible(boolean visible) {
		frame.setVisible(visible);
		if (visible)
			imgBuffer = frame.createImage(width, height);
		this.visible = visible;
	}

	public void setFont(Font font) {
		graphics2D.setFont(font);
	}

	public Graphics2D getGraphics() {
		return graphics2D;
	}

	public Font getFont() {
		return f;
	}

	//region unused overrides
	@Override
	public void windowOpened(WindowEvent e) {

	}

	@Override
	public void windowIconified(WindowEvent e) {

	}

	@Override
	public void windowDeiconified(WindowEvent e) {

	}

	@Override
	public void windowActivated(WindowEvent e) {

	}

	@Override
	public void windowDeactivated(WindowEvent e) {

	}


	@Override
	public void windowGainedFocus(WindowEvent e) {

	}

	@Override
	public void windowLostFocus(WindowEvent e) {

	}
	//endregion
}
