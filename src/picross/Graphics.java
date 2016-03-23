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
	private int x, y, numMistakes, currDifficulty;
	private int sizeX = 10, sizeY = 10;
	private Scanner s;
	private String currWindow, status;
	public List<String> output;
	//flags
	private boolean isRunning, isDone, playable;
	//graphics
	public FancyFrame frame;
	public Image imgBuffer;
	public static int[] clueLen;
	private Button bPause, bResume, bNewPuzzle, bXUp, bXDown, bYUp, bYDown, bBack, bStart, bMainMenu;
	public Graphics() {
		//initialize frame & basic flags
		frame = new FancyFrame("Loading...", SIZE);
		frame.addKeyListener(this);
		frame.addWindowListener(this);
		isRunning = true;
		isDone = false;
		frame.setVisible(true);
		//makes graphics look like not trash
		imgBuffer = frame.createImage(SIZE.width, SIZE.height);
		//determines if a game is in progress
		playable = false;
		status = "menu";
		currWindow = "menu";
		//get file for interfacing, temporary
		try {
			s = new Scanner(new File("solution.dat"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//currently random

		currBox = null;
		//graphics elements
		bNewPuzzle = new Button(SIZE.width / 2 - 100, 250, 200, 100, "New Game", Color.GREEN, 20);
		bNewPuzzle.setVisible(true);
		bXUp = new Button(300, 400, 100, 50, "Λ", 30);
		bXDown = new Button(300, 510, 100, 50, "V", 30);
		bYUp = new Button(600, 400, 100, 50, "Λ", 30);
		bYDown = new Button(600, 510, 100, 50, "V", 30);
		bBack = new Button(50, 50, 50, 50, "<", Color.RED, 30);
		bStart = new Button(SIZE.width / 2 - 50, SIZE.height - 100, 100, 75, "BEGIN", Color.GREEN, 30);
		bPause = new Button(SIZE.width - 60, 45, 50, 35, "Pause", Color.YELLOW, 17);
		bResume = new Button(SIZE.width / 2 - 25, SIZE.height / 2 - 10 + 30, 50, 20, "Resume", Color.GREEN, 17);
		bMainMenu = new Button(SIZE.width / 2 - 30, SIZE.height / 2 - 10 + 30, 60, 20, "Main Menu", new Color(128, 128, 255), 17);
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
			if(currWindow.equals("game")) {
				bPause.setVisible(true);
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
					if(Main.timer != null)
						Main.timer.pause();
				}
				//maximum mistakes
				if(numMistakes == 5) {
					status = "failed";
					playable = false;
					Main.timer.pause();
				}
			}
			else if(currWindow.equals("size picker")) {
				bXUp.setVisible(true);
				bXDown.setVisible(true);
				bYUp.setVisible(true);
				bYDown.setVisible(true);
				bBack.setVisible(true);
				bStart.setVisible(true);
			}
			x = frame.mouseX;
			y = frame.mouseY;
			mouseActions();
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
		if(currWindow.equals("menu")) {
			frame.setTitle("Main Menu");
			art.setColor(new Color(128, 128, 255));
			art.fillRect(0, 0, frame.getHeight(),frame.getWidth());
			art.setColor(Color.BLACK);
			art.setFont(art.getFont().deriveFont(50f));
			art.drawString("MAIN MENU", SIZE.width / 2 - 150, 100);
			bNewPuzzle.draw(x, y, art);
		}
		else if(currWindow.equals("size picker")) {
			frame.setTitle("Size Picker");
			if(sizeX == 25) {
				bXUp.setVisible(false);
			}
			else if (sizeX == 1) {
				bXDown.setVisible(false);
			}
			else {
				bXUp.setVisible(true);
				bXDown.setVisible(true);
			}
			if(sizeY == 25) {
				bYUp.setVisible(false);
			}
			else if(sizeY == 1) {
				bYDown.setVisible(false);
			}
			else {
				bYUp.setVisible(true);
				bYDown.setVisible(true);
			}
			art.setColor(new Color(128, 128, 255));
			art.fillRect(0, 0, frame.getHeight(), frame.getWidth());
			art.setColor(Color.BLACK);
			art.setFont(art.getFont().deriveFont(50f));
			art.drawString("SIZE PICKER", SIZE.width / 2 - 150, 100);
			art.drawString("X", 333, 390);
			art.drawString("Y", 633, 390);
			art.drawString(Integer.toString(sizeX), sizeX > 9 ? 320 : 335, 500);
			art.drawString(Integer.toString(sizeY), sizeY > 9 ? 620 : 635, 500);
			bXUp.draw(x, y, art);
			bXDown.draw(x, y, art);
			bYUp.draw(x, y, art);
			bYDown.draw(x, y, art);
			bBack.draw(x, y, art);
			bStart.draw(x, y, art);
		}
		else if(currWindow.equals("game")) {
			frame.setTitle(TITLE);
			art.setColor(Color.WHITE);
			art.fillRect(0, 0, SIZE.width, SIZE.height);
			art.setFont(art.getFont().deriveFont(12f));//12x7 pixels
			bPause.draw(x, y, art);
			for(int i = 0; i < (gameGrid.sizeX); i++) {
				for(int j = 0; j < (gameGrid.sizeY); j++) {
					gameGrid.drawGrid(i, j, art);
					art.setColor(Color.BLACK);
					if(!status.equals("paused")) {
						gameGrid.drawClues(j, 0, art);
						gameGrid.drawClues(i, 1, art);
					}
					else {
						art.setColor(Color.WHITE);
						art.fillRect(0, clueLen[1], clueLen[0], bSize * gameGrid.sizeY);
						art.fillRect(clueLen[0], 0, bSize * gameGrid.sizeX, clueLen[1]);
					}
					if(playable && i == (x - clueLen[0]) / bSize && j == (y - clueLen[1]) / bSize) {
						art.setColor(new Color(0, 0, 0, 64));
						art.fillRect(clueLen[0] + i * bSize, clueLen[1] + j * bSize, bSize, bSize);
					}
					else if(playable && (i == (x - clueLen[0]) / bSize || j == (y - clueLen[1]) / bSize)) {
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
				art.fillRect(frame.getWidth() / 2 - 50, frame.getHeight() / 2 - 25, 100, 70);
				art.setColor(Color.BLACK);
				art.drawRect(frame.getWidth() / 2 - 50, frame.getHeight() / 2 - 25, 100, 70);
				String showText = "";
				if(status.equals("solved")) {
					art.setColor(Color.GREEN);
					showText = "SOLVED";
					bPause.setVisible(false);
					bMainMenu.setVisible(true);
				} else if(status.equals("failed")) {
					art.setColor(Color.RED);
					showText = "FAILED";
					bMainMenu.setVisible(true);
					bPause.setVisible(false);
				} else if(status.equals("paused")) {
					showText = "PAUSED";
				}
				art.drawString(showText, frame.getWidth() / 2 - 21, frame.getHeight() / 2 - 6);
				art.setColor(Color.BLACK);
				art.drawString("TIME:" + Main.timer.toString(), frame.getWidth() / 2 - 45, frame.getHeight() / 2 + 12);
			}
			art.setColor(Color.WHITE);
			art.fillRect(20, 38, 60, 12);
			art.setColor(Color.black);
			art.drawString(Main.timer.toString(false), 20, 50);
			art.drawString("Difficulty: " + currDifficulty, 20, 70);
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
			bResume.draw(x, y, art);
			bMainMenu.draw(x, y, art);
			art.setFont(art.getFont().deriveFont(20f));
			while(xRendered < 5) {
				art.setColor(new Color(192,192,192));
				art.drawString("X", xRendered * 25 + 125, SIZE.height - 15);
				xRendered++;
			}
		}
		art = (Graphics2D)frame.getGraphics();
		if(art != null) {
			art.drawImage(imgBuffer, 0, 0, frame.getWidth(), frame.getHeight(), 0, 0, frame.getWidth(), frame.getHeight(), null);
			art.dispose();
		}
	}
	private void getSolution() {
		//x = Integer.parseInt(s.nextLine());
		//y = Integer.parseInt(s.nextLine());
		gameGrid = new Grid(sizeX,sizeY);
		solutionGrid = new Grid(sizeX, sizeY);
		for(int i = 0; i < sizeX; i++) {
			for(int j = 0; j < sizeY; j++) {
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
	private void writeClues() {
		try {
			FileWriter writer = new FileWriter("clues.nin");
			BufferedWriter strings = new BufferedWriter(writer);
			strings.write(Integer.toString(sizeX) + " " + sizeY);
			strings.newLine();
			for(Clue c : gameGrid.cluesX) {
				String s = c.toString(true);
				strings.write(s);
				strings.newLine();
			}
			for(Clue c : gameGrid.cluesY) {
				String s = c.toString(true);
				strings.write(s);
				strings.newLine();
			}
			strings.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void mouseActions() {
		if(frame.hasClicked) {
			frame.hasClicked = false;
		}
		if(currWindow.equals("game")) {
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
				if(frame.mouseButton == 3) {
					if(currBox != null) {
						currBox.impossibru();
						currBox.canModify = false;
					}
				}
				else if(frame.mouseButton == 1) {
					//click buttons
					if(currBox != null) {
						//if the box is not part of the solution, you made a mistake
						if(!currBox.green(solutionGrid)) {
							numMistakes++;
							currBox.canModify = false;
						}
						currBox.canModify = false;
					}
				}
				//right click = flag, is not checked with the solution to prevent cheating
				
			}
			else {
				if(currBox != null)
					currBox.canModify = true;
			}
		}
		else if(currWindow.equals("menu")) {
		}
		else if(currWindow.equals("size picker")) {
			
		}
	}
	private void generatePuzzle() {
		output = new ArrayList<String>();
		int numSolutions = 0;
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
			if(output.size() > 1) {
				int solutionsLine = Strings.findLineWith(output, "Solutions : ", true);
				numSolutions = Integer.parseInt(output.get(solutionsLine).substring(output.get(solutionsLine).length() - 1, output.get(solutionsLine).length()));
				String difficulty = "";
				int diffLine = Strings.findLineWith(output, "Decisions : ", true);
				/*for(int i = output.get(diffLine).length() - 1; i > 0; i--) {
					if(!Character.isDigit(output.get(diffLine).charAt(i))) {
						difficulty = output.get(diffLine).substring(i + 1, output.get(diffLine).length());
					}
				}*/
				difficulty = output.get(diffLine).substring(12, output.get(diffLine).length());
				currDifficulty = Integer.parseInt(difficulty);
			}
		} while(numSolutions > 1 || output.size() < 2);
		//find maximum size of clues on left & top
		clueLen = new int[2];
		clueLen[0] = 0; clueLen[1] = 0;
		for(int i = 0; i < gameGrid.sizeY; i++) {
			if(gameGrid.cluesX[i].toString().length() > clueLen[0]) {
				clueLen[0] = gameGrid.cluesX[i].toString().length();
			}
		}
		clueLen[0] *= 7;
		clueLen[0] += 10;
		if(clueLen[0] < 50)
			clueLen[0] = 50;
		for(int i = 0; i < gameGrid.sizeX; i++) {
			if(gameGrid.cluesY[i].getValues().size() > clueLen[1]) {
				clueLen[1] = gameGrid.cluesY[i].getValues().size();
			}
		}
		clueLen[1] *= 12;
		clueLen[1] += 50;
	}
	public void doClickAction(Button b) {
		if(b == bNewPuzzle) {
			bNewPuzzle.unClick();
			currWindow = "size picker";
		}
		else if(b == bResume) {
			bResume.unClick();
			status = "";
			bResume.setVisible(false);
			bPause.setVisible(true);
			Main.timer.resume();
			playable = true;
		}
		else if(b == bPause) {
			bPause.unClick();
			if (status.equals("")) {
				status = "paused";
				bPause.setVisible(false);
				bResume.setVisible(true);
				Main.timer.pause();
			}
			playable = false;
		}
		else if(b == bXUp) {
			bXUp.unClick();
			sizeX++;
		}
		else if(b == bXDown) {
			bXDown.unClick();
			sizeX--;
		}
		else if(b == bYUp) {
			bYUp.unClick();
			sizeY++;
		}
		else if(b == bYDown) {
			bYDown.unClick();
			sizeY--;
		}
		else if(b == bBack) {
			bBack.unClick();
			currWindow = "menu";
		}
		else if(b == bStart) {
			bStart.unClick();
			frame.setTitle("GENERATING...");
			currWindow = "game";
			status = "";
			playable = true;
			generatePuzzle();
			Main.timer.begin();
		}
		else if(b == bMainMenu) {
			bMainMenu.unClick();
			frame.setTitle("Main Menu");
			currWindow = "menu";
			status = "menu";
			numMistakes = 0;
			bMainMenu.setVisible(false);
			playable = false;
		}
	}
}
