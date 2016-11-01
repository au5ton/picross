package picross;

import java.awt.*;
import java.awt.event.*;

class FancyFrame extends Frame implements MouseMotionListener, MouseListener, MouseWheelListener {
	/**
	 *
	 */
	public int mouseX;
	public int mouseY;
	private int mouseButton;
	public int scrollAmt;
	private boolean hasClicked;
	private boolean clicking;

	@SuppressWarnings ("SameParameterValue")
	public FancyFrame(String title, Dimension size) {
		setTitle(title);
		setSize(size);
		addMouseMotionListener(this);
		addMouseListener(this);
		addMouseWheelListener(this);
	}

	@Override
	public void mouseDragged(MouseEvent me) {
		mouseX = me.getX();
		mouseY = me.getY();
	}

	@Override
	public void mouseMoved(MouseEvent me) {
		mouseX = me.getX();
		mouseY = me.getY();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		hasClicked = true;
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		clicking = true;
		mouseButton = e.getButton();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		clicking = false;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		scrollAmt = arg0.getWheelRotation();
	}

	public boolean hasClicked() {
		return hasClicked;
	}

	@SuppressWarnings ("SameParameterValue")
	public void setHasClicked(boolean b) {
		hasClicked = b;
	}

	public boolean isClicking() {
		return clicking;
	}

	public int getMouseButton() {
		return mouseButton;
	}
}
