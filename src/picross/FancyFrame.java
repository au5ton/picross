package picross;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class FancyFrame extends Frame implements MouseMotionListener, MouseListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 169736940888001290L;
	public int mouseX, mouseY, mouseButton;
	public boolean hasClicked, clicking;
	public FancyFrame(String title, Dimension size) {
		setTitle(title);
		setSize(size);
		addMouseMotionListener(this);
		addMouseListener(this);
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
		hasClicked=true;
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
	
}
