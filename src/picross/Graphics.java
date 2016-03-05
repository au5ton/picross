package picross;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Graphics implements Runnable, KeyListener, WindowListener {
	//basic window properties
	public final String TITLE = "Picross";
	public final Dimension SIZE = new Dimension(1000, 1000);
	public static int bSize;
	//base components
	private Grid gameGrid, solutionGrid;
	private Box currBox;
	private int selX, selY, numMistakes;
	private final int sizeX = 10, sizeY = 10;
	private Scanner s;
	private String currWindow, status;
	public List<String> output;
	//flags
	private boolean isRunning, isDone, playable;
	//graphics
	public FancyFrame frame;
	public Image imgBuffer;
	public static int[] clueLen;
	private Button pauseMenu;
	public Graphics() {
		//initialize frame & basic flags
		frame = new FancyFrame("GENERATING...", SIZE);
		frame.addKeyListener(this);
		frame.addWindowListener(this);
		isRunning = true;
		isDone = false;
		frame.setVisible(true);
		//makes graphics look like not trash
		imgBuffer = frame.createImage(SIZE.width, SIZE.height);
		//determines if a game is in progress
		playable = true;
		status = "";
		//get file for interfacing, temporary
		try {
			s = new Scanner(new File("solution.dat"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//currently random

		output = new ArrayList<String>();
		int numSolutions;
		do {
			getSolution();
			gameGrid.generateClues(solutionGrid);
			writeClues();
			Main.runSolver();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.output = LogStreamReader.output;
			numSolutions = Integer.parseInt(output.get(1).substring(output.get(1).length() - 1, output.get(1).length()));
		} while(numSolutions > 1);
		//find maximum size of clues on left & top
		clueLen = new int[2];
		clueLen[0] = 0; clueLen[1] = 0;
		for(int i = 0; i < gameGrid.sizeY; i++) {
			if(gameGrid.cluesX[i].toString().length() > clueLen[0]) {
				clueLen[0] = gameGrid.cluesX[i].toString().length();
			}
		}
		clueLen[0] *= 7;
		if(clueLen[0] < 50)
			clueLen[0] = 50;
		for(int i = 0; i < gameGrid.sizeX; i++) {
			if(gameGrid.cluesY[i].getValues().size() > clueLen[1]) {
				clueLen[1] = gameGrid.cluesY[i].getValues().size();
			}
		}
		clueLen[1] *= 12;
		clueLen[1] += 50;
		currBox = null;
		//graphics elements
		pauseMenu = new Button(SIZE.width - 60, 45, 45, 35, "Pause");
		pauseMenu.setVisible(true);
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
			//get size of each box for optimal display size, takes into account clueLen and mistakes box
			bSize = getBoxSize();
			//check for completeness
			boolean temp = true;
			for(int i = 0; i < gameGrid.sizeX; i++) {
				for(int j = 0; j < gameGrid.sizeY; j++) {
					if(gameGrid.getBox(i, j).getState() != 1 && solutionGrid.getBox(i, j).getState() == 1)
						temp = false;
				}
			}
			if(temp) {
				status = "solved";//TODO set game state to a string
				playable = false;
				Main.timer.pause();
			}
			//maximum mistakes
			if(numMistakes == 5) {
				status = "failed";
				playable = false;
				Main.timer.pause();
			}
			if(frame.hasClicked) {
				frame.hasClicked = false;
			}
			int x = frame.mouseX, y = frame.mouseY;
			//check buttons
			if(pauseMenu.isInBounds(x, y)) {
				pauseMenu.hover();
			}
			else {
				pauseMenu.unHover();
			}
			//bound checking to prevent instant toggling of a flag
			if(currBox != null && (x - clueLen[0]) / bSize < gameGrid.sizeX && (y - clueLen[1]) / bSize < gameGrid.sizeY && x > clueLen[0] && y > clueLen[1] && currBox != gameGrid.getBox((x - clueLen[0]) / bSize, (y - clueLen[1]) / bSize))
				currBox.canModify = true;
			//get box only if mouse is within game grid, otherwise it is null
			if((x - clueLen[0]) / bSize < gameGrid.sizeX && (y - clueLen[1]) / bSize < gameGrid.sizeY && x > clueLen[0] && y > clueLen[1]) {
				currBox = gameGrid.getBox((x - clueLen[0]) / bSize, (y - clueLen[1]) / bSize);
			}
			else {
				currBox = null;
			}
			if(frame.clicking) {
				//only disables boxes as the player mouses over them once the game is complete
				if(!playable && currBox != null)
					currBox.canModify = false;
				//left click = reveal
				if(frame.mouseButton == 1) {
					//click buttons
					if(pauseMenu.isInBounds(x, y)) {
						pauseMenu.click();
					}
					if(currBox != null) {
						//if the box is not part of the solution, you made a mistake
						if(!currBox.green(solutionGrid)) {
							numMistakes++;
						}
						currBox.canModify = false;
					}
				}
				//right click = flag, is not checked with the solution to prevent cheating
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
			if(pauseMenu.isClicking()) {
				if(status.equals("paused")) {
					status = "";
				} else if (status.equals("")) {
					status = "paused";
				}
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
		frame.setTitle(TITLE);
		Graphics2D art = (Graphics2D)imgBuffer.getGraphics();
		art.setFont(art.getFont().deriveFont(12f));//12x7 pixels
		pauseMenu.draw(art);
		for(int i = 0; i < (gameGrid.sizeX); i++) {
			for(int j = 0; j < (gameGrid.sizeY); j++) {
				gameGrid.drawGrid(i, j, art);
				art.setColor(Color.BLACK);
				gameGrid.drawClues(j, 0, art);
				gameGrid.drawClues(i, 1, art);
				if(playable && i == (selX - clueLen[0]) / bSize && j == (selY - clueLen[1]) / bSize) {
					art.setColor(new Color(0, 0, 0, 64));
					art.fillRect(clueLen[0] + i * bSize, clueLen[1] + j * bSize, bSize, bSize);
				}
				else if(playable && (i == (selX - clueLen[0]) / bSize || j == (selY - clueLen[1]) / bSize)) {
					art.setColor(new Color(0, 0, 0, 32));
					art.fillRect(clueLen[0] + i * bSize, clueLen[1] + j * bSize, bSize, bSize);
				}
				art.setColor(Color.BLACK);
				art.drawRect(clueLen[0] + i * bSize, clueLen[1] + j * bSize, bSize, bSize);
			}
		}
		for(int i = 5; i < gameGrid.sizeX; i += 5) {
			art.drawLine(clueLen[0] + i * bSize + 1, clueLen[1], clueLen[0] + i * bSize + 1, clueLen[1] + gameGrid.sizeY * bSize);
		}
		for(int i = 5; i < gameGrid.sizeY; i += 5) {
			art.drawLine(clueLen[0], clueLen[1] + i * bSize + 1, clueLen[0] + gameGrid.sizeX * bSize, clueLen[1] + i * bSize + 1);
		}
		if(!playable) {
			art.setColor(Color.WHITE);
			art.fillRect(SIZE.width / 2 - 50, SIZE.height / 2 - 25, 100, 50);
			art.setColor(Color.BLACK);
			art.drawRect(SIZE.width / 2 - 50, SIZE.height / 2 - 25, 100, 50);
			String showText = "";
			if(status.equals("solved")) {
				art.setColor(Color.GREEN);
				showText = "SOLVED";
			} else if(status.equals("failed")) {
				art.setColor(Color.RED);
				showText = "FAILED";
			} else if(status.equals("paused")) {
				showText = "PAUSED";
			}
			art.drawString(showText, 479, 494);
			art.setColor(Color.BLACK);
			art.drawString("TIME:" + Main.timer.toString(), SIZE.width / 2 - 45, SIZE.height / 2 + 12);
		}
		art.setColor(Color.WHITE);
		art.fillRect(20, 38, 60, 12);
		art.setColor(Color.black);
		art.drawString(Main.timer.toString(), 20, 50);
		//render mistakes
		art.setFont(art.getFont().deriveFont(20f));
		art.drawString("MISTAKES: ", 10, SIZE.height - 15);
		int xRendered = 0, mistakesTemp = numMistakes;
		art.drawRect(120, SIZE.height - 35, 125, 25);
		art.setColor(Color.RED);
		while(mistakesTemp > 0 && xRendered < 5) {
			art.drawString("X", xRendered * 25 + 125, SIZE.height - 15);
			mistakesTemp--;
			xRendered++;
		}
		while(xRendered < 5) {
			art.setColor(new Color(192,192,192));
			art.drawString("X", xRendered * 25 + 125, SIZE.height - 15);
			xRendered++;
		}
		art = (Graphics2D)frame.getGraphics();
		if(art != null) {
			art.drawImage(imgBuffer, 0, 0, SIZE.width, SIZE.height, 0, 0, SIZE.width, SIZE.height, null);
			art.dispose();
		}
	}
	private void getSolution() {
		int x = sizeX, y = sizeY;
		//x = Integer.parseInt(s.nextLine());
		//y = Integer.parseInt(s.nextLine());
		gameGrid = new Grid(x,y);
		solutionGrid = new Grid(x, y);
		for(int i = 0; i < x; i++) {
			for(int j = 0; j < y; j++) {
				//int b = s.nextInt();
				Random random = new Random();
				int b = random.nextInt(2);
				if(b == 1) {
					solutionGrid.getBox(i, j).setState(1);
				}
			}
		}
	}
	private int getBoxSize() {
		if(gameGrid.sizeX >= gameGrid.sizeY) {
			return ((int)SIZE.width - (clueLen[0] > clueLen[1] ? clueLen[0] : clueLen[1])) / (gameGrid.sizeX + 1);
		}
		else {
			return (int)((frame.getSize().getHeight() - (clueLen[0] + clueLen[1])) / (gameGrid.sizeY + 1));
		}
	}
	public void writeClues() {
		try {
			FileWriter writer = new FileWriter("clues.nin");
			BufferedWriter strings = new BufferedWriter(writer);
			strings.write(Integer.toString(sizeX) + " " + sizeY);
			strings.newLine();
			for(int i = 0; i < gameGrid.cluesX.length; i++) {
				String s = gameGrid.cluesX[i].toString(true);
				strings.write(s);
				strings.newLine();
			}
			for(int i = 0; i < gameGrid.cluesY.length; i++) {
				String s = gameGrid.cluesY[i].toString(true);
				strings.write(s);
				strings.newLine();
			}
			strings.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
