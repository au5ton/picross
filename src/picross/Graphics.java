package picross;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Graphics implements Runnable, KeyListener, WindowListener {
	//basic window properties
	public final String TITLE = "Picross";
	public final Dimension SIZE = new Dimension(1000, 1000);
	public static int bSize;
	//
	private Grid gameGrid, solutionGrid;
	private Box currBox;
	private int selX, selY;
	private boolean isRunning, isDone, solved;
	public FancyFrame frame;
	public Image imgBuffer;
	private String currWindow;
	private Scanner s;
	public Graphics() {
		frame = new FancyFrame(TITLE, SIZE);
		frame.addKeyListener(this);
		frame.addWindowListener(this);
		isRunning = true;
		isDone = false;
		frame.setVisible(true);
		imgBuffer = frame.createImage(SIZE.width, SIZE.height);
		currBox = null;
		try {
			s = new Scanner(new File("solution.dat"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		for(int i = 0; i < gameGrid.sizeX; i++) {
			for(int j = 0; j < gameGrid.sizeY; j++) {
				if(i == 0 || j == 0 || i == 4 || j == 4)
					solutionGrid.getBox(i, j).setState(1);
			}
		}
		*/
		getSolution();
		gameGrid.generateClues(solutionGrid);
		currBox = null;
		
	}
	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		while(true) {
			if(isDone) System.exit(0);
			try {
				Thread.sleep(100);
			}
			catch(InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		frame.setVisible(false);
		isRunning = false;
		frame.dispose();
		isDone = true;
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run() {
		while(isRunning) {
			//check for completeness
			bSize = getBoxSize();
			boolean temp = true;
			for(int i = 0; i < gameGrid.sizeX; i++) {
				for(int j = 0; j < gameGrid.sizeY; j++) {
					if(gameGrid.getBox(i, j).getState() != 1 && solutionGrid.getBox(i, j).getState() == 1)
						temp = false;
				}
			}
			if(temp)
				solved = true;
			if(frame.hasClicked) {
				frame.hasClicked = false;
			}
			int x = frame.mouseX, y = frame.mouseY;
			if(currBox != null && (x - 100) / bSize < gameGrid.sizeX && (y - 100) / bSize < gameGrid.sizeY && x > 100 && y > 100 && currBox != gameGrid.getBox((x - 100) / bSize, (y - 100) / bSize))
				currBox.canModify = true;
			if((x - 100) / bSize < gameGrid.sizeX && (y - 100) / bSize < gameGrid.sizeY && x > 100 && y > 100) {
				currBox = gameGrid.getBox((x - 100) / bSize, (y - 100) / bSize);
			}
			else {
				currBox = null;
			}
			if(frame.clicking) {
				if(solved && currBox != null)
					currBox.canModify = false;
				if(frame.mouseButton == 1) {
					if(currBox != null) {
						currBox.green(solutionGrid);
						currBox.canModify = false;
					}
				}
				else if(frame.mouseButton == 3) {
					if(currBox != null) {
						currBox.impossibru();
						currBox.canModify = false;
					}
				}
			}
			else {
				if(currBox != null)
					currBox.canModify = true;
			}
			selX = x;
			selY = y;
			draw();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	public void draw() {
		Graphics2D art = (Graphics2D)imgBuffer.getGraphics();
		art.setFont(art.getFont().deriveFont(12f));//12x7 pixels
		for(int i = 0; i < (gameGrid.sizeX); i++) {
			for(int j = 0; j < (gameGrid.sizeY); j++) {
				gameGrid.drawGrid(i, j, art);
				art.setColor(Color.BLACK);
				gameGrid.drawClues(i, 0, art);
				gameGrid.drawClues(i, 1, art);
				if(i == (selX - 100) / bSize && j == (selY - 100) / bSize) {
					art.setColor(new Color(0, 0, 0, 128));
					art.fillRect(100 + i * bSize, 100 + j * bSize, bSize, bSize);
				}
				else if(i == (selX - 100) / bSize || j == (selY - 100) / bSize) {
					art.setColor(new Color(0, 0, 0, 64));
					art.fillRect(100 + i * bSize, 100 + j * bSize, bSize, bSize);
				}
				/*
				else {
					art.setColor(new Color(255,255,255));
					art.fillRect(i, j, 100, 100);
				}
				*/
				art.setColor(Color.BLACK);
				art.drawRect(100 + i * bSize, 100 + j * bSize, bSize, bSize);
			}
		}
		if(solved) {
			art.drawString("SOLVED", 50, 50);
		}
		art = (Graphics2D)frame.getGraphics();
		art.drawImage(imgBuffer, 0, 0, SIZE.width, SIZE.height, 0, 0, SIZE.width, SIZE.height, null);
		art.dispose();
	}
	private void getSolution() {
		int x, y;
		x = Integer.parseInt(s.nextLine());
		y = Integer.parseInt(s.nextLine());
		gameGrid = new Grid(x,y);
		solutionGrid = new Grid(x, y);
		for(int i = 0; i < x; i++) {
			for(int j = 0; j < y; j++) {
				int b = s.nextInt();
				if(b == 1) {
					solutionGrid.getBox(i, j).setState(1);
				}
			}
		}
	}
	private int getBoxSize() {
		if(gameGrid.sizeX >= gameGrid.sizeY) {
			return (int) ((frame.getSize().getWidth() - 200) / gameGrid.sizeX);
		}
		else {
			return (int)((frame.getSize().getHeight() - 200) / gameGrid.sizeY);
		}
	}
}
