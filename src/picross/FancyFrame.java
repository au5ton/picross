package picross;

import java.awt.*;
import java.awt.event.*;

public class FancyFrame extends Frame implements MouseMotionListener, MouseListener, MouseWheelListener {
	/**
	 *
	 */
	private static final long serialVersionUID = 169736940888001290L;
	public int mouseX;
	public int mouseY;
	private int mouseButton;
	public int scrollAmt;
	private boolean hasClicked;
	private boolean clicking;

	public FancyFrame(String title, Dimension size) {
		setTitle(title);
		setSize(size);
		addMouseMotionListener(this);
		addMouseListener(this);
		addMouseWheelListener(this);
	}

	@Override
	public void mouseDragged(MouseEvent me) {
		// TODO Auto-generated method stub
		mouseX = me.getX();
		mouseY = me.getY();
	}

	@Override
	public void mouseMoved(MouseEvent me) {
		// TODO Auto-generated method stub
		mouseX = me.getX();
		mouseY = me.getY();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		hasClicked = true;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		clicking = true;
		mouseButton = e.getButton();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		clicking = false;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		// TODO Auto-generated method stub
		scrollAmt = arg0.getWheelRotation();
	}

	public boolean hasClicked() {
		return hasClicked;
	}

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
