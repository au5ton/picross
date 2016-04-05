package picross;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import static java.awt.Color.*;
import static picross.Main.FPSCounter;
import static picross.Main.runSolver;

public class Graphics implements Runnable, KeyListener, WindowListener {
	//basic window properties
	private final Dimension SIZE = new Dimension(800, 600);
	static int bSize;
	private static int numFrames = 0;
	private final int MIN_BSIZE = 14, MIN_WINDOW_SIZE = 250;
	//base components
	private Grid gameGrid, solutionGrid;
	private Box currBox;
	private int x, y, numMistakes, numFadeFrames = 0, fadeAlpha, cWidth;//numFadeFrames uses frames as a way to time fading
	private int sizeX = 10, sizeY = 10, fps = 0;
	private Scanner s;//will be used in future to save/load puzzles
	private String currWindow, status;
	private List<String> output;
	//flags
	private boolean isRunning, isDone, playable, faded = false, modifier = false, debugging = false;
	//graphics
	private FancyFrame frame;
	private Image imgBuffer;
	public static int[] clueLen;
	private Button bPause, bResume, bNewPuzzle, bXUp, bXDown, bYUp, bYDown, bBack, bStart, bMainMenu, bMainMenu2, bQuitGame, bBegin, bRegenPuzzle;
	private Font f;

	public Graphics() {
		FPSCounter.begin();
		//initialize frame & basic flags
		frame = new FancyFrame("Loading...", SIZE);
		frame.addKeyListener(this);
		frame.addWindowListener(this);
		//frame.setResizable(false);//TODO make frame properly resizable -- IN PROGRESS
		isRunning = true;
		isDone = false;
		frame.setVisible(true);
		//makes graphics look like not trash
		imgBuffer = frame.createImage(frame.getWidth(), frame.getHeight());
		//determines if a game is in progress
		playable = false;
		status = "menu";
		currWindow = "menu";
		//get file for interfacing, temporary
		try {
			s = new Scanner(new File("solution.dat"));
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		//currently random

		currBox = null;
		//graphics elements
		bNewPuzzle = new Button(frame.getWidth() / 2 - 100, 250, 200, 100, "Start Game", GREEN, 20);
		bNewPuzzle.setVisible(true);
		bXUp = new Button(300, 400, 100, 50, "Λ", 30);//TODO move buttons when window is resized - b.setPos(x, y)
		bXDown = new Button(300, 510, 100, 50, "V", 30);
		bYUp = new Button(600, 400, 100, 50, "Λ", 30);
		bYDown = new Button(600, 510, 100, 50, "V", 30);
		bBack = new Button(10, 55, 50, 50, "<", RED, 30);
		bStart = new Button(frame.getWidth() / 2 - 50, frame.getHeight() - 100, 100, 75, "GENERATE", GREEN, 30);
		bPause = new Button(20, 50, 60, 60, "Pause", YELLOW, 17);
		bResume = new Button(frame.getWidth() / 2 - 100, frame.getHeight() / 2 + 7, 100, 43, "Resume", GREEN, 17);
		bMainMenu = new Button(frame.getWidth() / 2 - 100, frame.getHeight() / 2 + 7, 100, 43, "Main Menu", new Color(128, 128, 255), 17);
		bMainMenu2 = new Button(frame.getWidth() / 2, frame.getHeight() / 2 + 7, 100, 43, "Main Menu", new Color(128, 128, 255), 17);
		bRegenPuzzle = new Button(frame.getWidth() / 2, frame.getHeight() / 2 + 7, 100, 43, "New Puzzle", GREEN, 17);
		bQuitGame = new Button(frame.getWidth() / 2 - 100, 450, 200, 100, "Quit Game", RED, 20);
		bQuitGame.setVisible(true);
		bBegin = new Button(frame.getWidth() / 2 - 100, frame.getHeight() / 2 - 50, 200, 100, "BEGIN", GREEN, 20);
	}

	@Override
	public void windowActivated(WindowEvent arg0) {

	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		while(true) {
			if(isDone)
				System.exit(0);
			try {
				Thread.sleep(100);
			} catch(InterruptedException ie) {
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
		doClickAction(bPause);
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {

	}

	@Override
	public void windowIconified(WindowEvent arg0) {

	}

	@Override
	public void windowOpened(WindowEvent arg0) {

	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		int key = arg0.getKeyCode();
		if(key == KeyEvent.VK_SHIFT) {
			modifier = true;
		} else if(key == KeyEvent.VK_D) {
			debugging = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		int key = arg0.getKeyCode();
		if(key == KeyEvent.VK_SHIFT) {
			modifier = false;
		} else if(key == KeyEvent.VK_D) {
			debugging = false;
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {

	}

	@Override
	public void run() {
		while(isRunning) {
			if(currWindow.equals("game")) {
				if(playable)
					bPause.setVisible(true);
				else {
					bBegin.setPos(frame.getWidth() / 2 - 100, frame.getHeight() / 2 - 50);
					bResume.setPos(frame.getWidth() / 2 - 100, frame.getHeight() / 2 + 7);
					bMainMenu.setPos(frame.getWidth() / 2 - 100, frame.getHeight() / 2 + 7);
					bMainMenu2.setPos(frame.getWidth() / 2, frame.getHeight() / 2 + 7);
					bRegenPuzzle.setPos(frame.getWidth() / 2, frame.getHeight() / 2 + 7);
				}
				//get size of each box for optimal display size, takes into account clueLen and mistakes box
				bSize = getBoxSize();

				frame.setMinimumSize(new Dimension(
						245 + getStrLen("TIME: " + Main.timer.toString(false), 20f) + 25 >
								clueLen[0] + MIN_BSIZE * gameGrid.sizeX + 25 ?
								245 + getStrLen("TIME: " + Main.timer.toString(false), 20f) + 25 :
								clueLen[0] + MIN_BSIZE * gameGrid.sizeX + 25, clueLen[1] + MIN_BSIZE * gameGrid.sizeY + 50
				));
				//check for completeness
				boolean temp = true;
				for(int i = 0; i < gameGrid.sizeX; i++) {
					for(int j = 0; j < gameGrid.sizeY; j++) {
						if(gameGrid.getBox(i, j).getState() != 1 && solutionGrid.getBox(i, j).getState() == 1)
							temp = false;
					}
				}
				if(temp) {
					status = "solved";
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
				//maximum time
				if(Main.timer.getHours() > 9) {
					status = "failed";
					playable = false;
					Main.timer.pause();
				}
				if(status.equals("paused") && !Main.animator.isRunning()) {
					Main.animator.begin();
				} else if(!status.equals("paused") && Main.animator.isRunning()) {
					Main.animator.reset();
				}
			} else if(currWindow.equals("size picker")) {
				frame.setMinimumSize(new Dimension(
						getStrLen("SIZE PICKER", 50f) + bBack.getSize().width * 2 + 25,
						100 + 50 + 10 + 50 + 60 + 250
				));
				int freeSpace = frame.getHeight() - 100 - bStart.getSize().height - 150;
				bXUp.setVisible(true);
				bXUp.setPos(frame.getWidth() / 2 - 200, freeSpace / 2 - 55 + 160);
				bXDown.setVisible(true);
				bXDown.setPos(bXUp.getX(), bXUp.getY() + 110);
				bYUp.setVisible(true);
				bYUp.setPos(frame.getWidth() / 2 + 100, freeSpace / 2 - 55 + 160);
				bYDown.setVisible(true);
				bYDown.setPos(bYUp.getX(), bYUp.getY() + 110);
				bBack.setVisible(true);
				bStart.setVisible(true);
				bStart.setPos(frame.getWidth() / 2 - 50, frame.getHeight() - 100);
				if(sizeX > 25) {
					sizeX = 25;
				}
				if(sizeX < 1) {
					sizeX = 1;
				}
				if(sizeY > 25) {
					sizeY = 25;
				}
				if(sizeY < 1) {
					sizeY = 1;
				}
			} else if(currWindow.equals("menu")) {
				if(f != null) {
					frame.setMinimumSize(new Dimension(getStrLen("MAIN MENU", 50f) + 25, 550));
				}
				bNewPuzzle.setPos(frame.getWidth() / 2 - 100, bNewPuzzle.getY());
				bQuitGame.setPos(frame.getWidth() / 2 - 100, bQuitGame.getY());
			}
			x = frame.mouseX;
			y = frame.mouseY;
			mouseActions();
			draw();
			try {
				Thread.sleep(10);
				numFrames++;
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Renders an image of the game.
	 */
	private void draw() {
		Graphics2D art = (Graphics2D) imgBuffer.getGraphics();
		art.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		art.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		f = art.getFont();
		switch(currWindow) {
			case "menu":
				frame.setTitle("Main Menu | Picross");
				art.setColor(new Color(128, 128, 255));
				art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				art.setColor(BLACK);
				art.setFont(f.deriveFont(50f));
				f = f.deriveFont(50f);
				drawCenteredText(f, "MAIN MENU", 100, art);
				art.setFont(f.deriveFont(30f));
				f = f.deriveFont(30f);
				art.drawString("Loading...", frame.getWidth() / 2 - 65, 315);
				bNewPuzzle.draw(x, y, art);
				bQuitGame.draw(x, y, art);
				break;
			case "size picker":
				frame.setTitle("Size Picker | Picross");
				if(sizeX == 25 || (modifier && sizeX + 5 > 25)) {
					bXUp.setVisible(false);
				} else if(sizeX == 1 || (modifier && sizeX - 5 < 1)) {
					bXDown.setVisible(false);
				} else {
					bXUp.setVisible(true);
					bXDown.setVisible(true);
				}
				if(sizeY == 25 || (modifier && sizeY + 5 > 25)) {
					bYUp.setVisible(false);
				} else if(sizeY == 1 || (modifier && sizeY - 5 < 1)) {
					bYDown.setVisible(false);
				} else {
					bYUp.setVisible(true);
					bYDown.setVisible(true);
				}

				art.setColor(new Color(128, 128, 255));
				art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				art.setColor(BLACK);
				art.setFont(f.deriveFont(50f));
				f = f.deriveFont(50f);
				drawCenteredText(f, "SIZE PICKER", 100, art);
				drawCenteredText(f, "X", bXUp.getX() + bXUp.getSize().width / 2, bXUp.getY() - 10, art);
				drawCenteredText(f, "Y", bYUp.getX() + bYUp.getSize().width / 2, bYUp.getY() - 10, art);
				drawCenteredText(f, Integer.toString(sizeX), bXUp.getX() + bXUp.getSize().width / 2, bXDown.getY() - 10, art);
				drawCenteredText(f, Integer.toString(sizeY), bYUp.getX() + bYUp.getSize().width / 2, bYDown.getY() - 10, art);
				bXUp.draw(x, y, art);
				bXDown.draw(x, y, art);
				bYUp.draw(x, y, art);
				bYDown.draw(x, y, art);
				bBack.draw(x, y, art);
				bStart.draw(x, y, art);
				break;
			case "game":
				frame.setTitle("" + Main.timer.toString(true) + " | Picross");
				art.setColor(new Color(128, 128, 255));
				art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				if(playable) {
					art.setColor(fadeOff(64, 100));
					art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				}
				art.setFont(f.deriveFont(12f));
				f = f.deriveFont(12f);//12x7 pixels

				art.setColor(BLACK);
				cWidth = frame.getWidth() / 2 - (gameGrid.sizeX * bSize / 2) - clueLen[0];
				if(cWidth < 0) {
					cWidth = 0;
				}
				for(int i = 0; i < (gameGrid.sizeX); i++) {
					for(int j = 0; j < (gameGrid.sizeY); j++) {
						gameGrid.drawGrid(i, j, art, cWidth);
						art.setColor(BLACK);
						if(playable && i == (x - clueLen[0] - cWidth) / bSize && j == (y - clueLen[1]) / bSize && x > clueLen[0] + cWidth && y > clueLen[1]) {
							art.setColor(new Color(0, 0, 0, 64));
							art.fillRect(clueLen[0] + i * bSize + cWidth, clueLen[1] + j * bSize, bSize, bSize);
						} else if(playable && ((i == (x - clueLen[0] - cWidth) / bSize && x > clueLen[0] + cWidth) || (j == (y - clueLen[1]) / bSize && y > clueLen[1]))) {
							art.setColor(new Color(0, 0, 0, 32));
							art.fillRect(clueLen[0] + i * bSize + cWidth, clueLen[1] + j * bSize, bSize, bSize);
						}
						art.setColor(BLACK);
						art.drawRect(clueLen[0] + i * bSize + cWidth, clueLen[1] + j * bSize, bSize, bSize);
					}
				}
				if(!status.equals("paused") && !status.equals("get ready")) {
					for(int i = 0; i < gameGrid.sizeX; i++) {
						gameGrid.drawClues(i, 1, art, cWidth);
					}
					for(int j = 0; j < gameGrid.sizeY; j++) {
						gameGrid.drawClues(j, 0, art, cWidth);
					}
				}
				bPause.draw(x, y, art);
				for(int i = 5; i < gameGrid.sizeX; i += 5) {
					art.drawLine(clueLen[0] + i * bSize + 1 + cWidth, clueLen[1], clueLen[0] + i * bSize + 1 + cWidth, clueLen[1] + gameGrid.sizeY * bSize);
				}
				for(int i = 5; i < gameGrid.sizeY; i += 5) {
					art.drawLine(clueLen[0] + cWidth, clueLen[1] + i * bSize + 1, clueLen[0] + gameGrid.sizeX * bSize + cWidth, clueLen[1] + i * bSize + 1);
				}
				art.setFont(f.deriveFont(20f));
				f = f.deriveFont(20f);
				art.drawString("MISTAKES: ", 10, frame.getHeight() - 15);
				int xRendered = 0, mistakesTemp = numMistakes;
				art.drawRect(120, frame.getHeight() - 35, 125, 25);
				art.setColor(RED);
				while(mistakesTemp > 0 && xRendered < 5) {
					art.drawString("X", xRendered * 25 + 125, frame.getHeight() - 15);
					mistakesTemp--;
					xRendered++;
				}
				while(xRendered < 5) {
					art.setColor(new Color(0, 0, 0, 64));
					art.drawString("X", xRendered * 25 + 125, frame.getHeight() - 15);
					xRendered++;
				}
				if(!playable) {
					bPause.setVisible(false);
					if(bBegin.isVisible()) {
						faded = true;
					}
					art.setColor(fadeOn(64, 100));
					art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
					art.setColor(WHITE);
					art.fillRect(frame.getWidth() / 2 - 100, frame.getHeight() / 2 - 50, 200, 100);
					art.setColor(BLACK);
					art.drawRect(frame.getWidth() / 2 - 100, frame.getHeight() / 2 - 50, 200, 100);
					String showText = "";

					art.setFont(f.deriveFont(30f));
					f = f.deriveFont(30f);
					switch(status) {
						case "solved":
							art.setColor(GREEN);
							showText = "SOLVED";
							bMainMenu.setVisible(true);
							bRegenPuzzle.setVisible(true);
							break;
						case "failed":
							art.setColor(RED);
							showText = "FAILED";
							bMainMenu.setVisible(true);
							bRegenPuzzle.setVisible(true);
							break;
						case "paused":
							if(Main.animator.getMS() % 1000 <= 500) {
								drawCenteredText(f, "PAUSED", frame.getHeight() / 2 - 10, art);
							}
							break;
					}
					drawCenteredText(f, showText, frame.getHeight() / 2 - 10, art);
					art.setColor(BLACK);
					//if(!status.equals("get ready") && !status.equals("paused"))
					//art.drawString("TIME:" + Main.timer.toString(), frame.getWidth() / 2 - 45, frame.getHeight() / 2 - 12);
				}
				//render mistakes/timer
				art.setFont(f.deriveFont(20f));
				f = f.deriveFont(20f);
				art.setColor(BLACK);
				drawRightText(f, "TIME: " + Main.timer.toString(false), frame.getHeight() - 15, art);
				bBegin.draw(x, y, art);
				bResume.draw(x, y, art);
				bMainMenu2.draw(x, y, art);
				bMainMenu.draw(x, y, art);
				bRegenPuzzle.draw(x, y, art);
				break;
		}
		if(Main.FPSCounter.getMS() > 1000) {
			Main.FPSCounter.begin();
			fps = numFrames;
			numFrames = 0;
		}
		art.setColor(black);
		art.setFont(f.deriveFont(12f));
		f = f.deriveFont(12f);
		if(debugging) {
			art.drawString("" + fps + " FPS", 20, 50);
		}
		art = (Graphics2D) frame.getGraphics();
		if(art != null) {
			imgBuffer = Resizer.PROGRESSIVE_BILINEAR.resize((BufferedImage) imgBuffer, frame.getWidth(), frame.getHeight());
			art.drawImage(imgBuffer, 0, 0, frame.getWidth(), frame.getHeight(), 0, 0, frame.getWidth(), frame.getHeight(), null);
			art.dispose();
		}
	}

	/**
	 * Creates a Grid with random states of size sizeX, sizeY.
	 */
	private void getSolution() {
		//x = Integer.parseInt(s.nextLine());
		//y = Integer.parseInt(s.nextLine());
		gameGrid = new Grid(sizeX, sizeY);
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

	/**
	 * @return Returns the side length of a box in pixels based on graphics elements in game and frame size
	 */
	private int getBoxSize() {
		int temp = (frame.getWidth() - (clueLen[0] > clueLen[1] ? clueLen[0] : clueLen[1])) / (gameGrid.sizeX + 1);
		if(temp * gameGrid.sizeY + clueLen[1] + 50 > frame.getHeight()) {
			return (frame.getHeight() - 50 - clueLen[1]) / gameGrid.sizeY > MIN_BSIZE ? (frame.getHeight() - 50 - clueLen[1]) / gameGrid.sizeY : MIN_BSIZE;
		} else {
			return temp > MIN_BSIZE ? temp : MIN_BSIZE;
		}
	}

	/**
	 * Writes generated clues of a random puzzle to a file, to be read by the solver program.
	 */
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
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Performs any actions regarding mouse clicks that are not handled by the Button class. Includes gameplay and scrolling on the size picker.
	 */
	private void mouseActions() {
		if(frame.hasClicked()) {
			frame.setHasClicked(false);
		}
		switch(currWindow) {
			case "game":
				//bound checking to prevent instant toggling of a flag
				if(currBox != null && (x - clueLen[0] - cWidth) / bSize < gameGrid.sizeX && (y - clueLen[1]) / bSize < gameGrid.sizeY && x > clueLen[0] + cWidth && y > clueLen[1] && currBox != gameGrid.getBox((x - clueLen[0] - cWidth) / bSize, (y - clueLen[1]) / bSize)) {
					currBox.setCanModify(true);
				}
				//get box only if mouse is within game grid, otherwise it is null
				if((x - clueLen[0] - cWidth) / bSize < gameGrid.sizeX && (y - clueLen[1]) / bSize < gameGrid.sizeY && x > clueLen[0] + cWidth && y > clueLen[1]) {
					currBox = gameGrid.getBox((x - clueLen[0] - cWidth) / bSize, (y - clueLen[1]) / bSize);
				} else {
					currBox = null;
				}
				if(currBox != null) {
					if(frame.isClicking()) {
						//only disables boxes as the player attempts to modify them
						if(!playable && currBox != null)
							currBox.setCanModify(false);
						//left click = reveal
						if(frame.getMouseButton() == 3) {
							currBox.impossibru();
							currBox.setCanModify(false);
						} else if(frame.getMouseButton() == 1) {
							//click buttons
							//if the box is not part of the solution, you made a mistake
							if(!currBox.green(solutionGrid)) {
								numMistakes++;
								currBox.setCanModify(false);
							}
							currBox.setCanModify(false);
						}
						//right click = flag, is not checked with the solution to prevent cheating

					} else {
						currBox.setCanModify(true);
					}
				}
				break;
			case "menu":
				//no special actions for menu
				break;
			case "size picker":
				if(frame.scrollAmt != 0) {
					if(isInBounds(bXUp.getX(), bXUp.getY() + bXUp.getSize().height, bXUp.getX() + bXUp.getSize().width, bXUp.getY() + bXUp.getSize().height + 60)) {
						if(modifier) {
							sizeX -= sizeX - (5 * frame.scrollAmt) > 0 && sizeX - (5 * frame.scrollAmt) <= 25 ? frame.scrollAmt * 5 : 0;
						} else {
							sizeX -= sizeX - frame.scrollAmt > 0 && sizeX - frame.scrollAmt <= 25 ? frame.scrollAmt : 0;
						}
					} else if(isInBounds(bYUp.getX(), bYUp.getY() + bYUp.getSize().height, bYUp.getX() + bYUp.getSize().width, bYUp.getY() + bYUp.getSize().height + 60)) {
						if(modifier) {
							sizeY -= sizeY - (5 * frame.scrollAmt) > 0 && sizeY - (5 * frame.scrollAmt) <= 25 ? frame.scrollAmt * 5 : 0;
						} else {
							sizeY -= sizeY - frame.scrollAmt > 0 && sizeY - frame.scrollAmt <= 25 ? frame.scrollAmt : 0;
						}
					}
					frame.scrollAmt = 0;
				}
				break;
		}
	}

	private void generatePuzzle() {
		output = new ArrayList<>();
		int numSolutions = 0;
		do {
			getSolution();
			gameGrid.generateClues(solutionGrid);
			writeClues();
			runSolver();
			try {
				Thread.sleep(500);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
			this.output = LogStreamReader.output;
			if(output.size() > 1) {
				int solutionsLine = Strings.findLineWith(output, "Solutions : ", true);
				numSolutions = Integer.parseInt(output.get(solutionsLine).substring(output.get(solutionsLine).length() - 1, output.get(solutionsLine).length()));
				//String difficulty = "";
				//int diffLine = Strings.findLineWith(output, "Decisions : ", true);
				//difficulty = output.get(diffLine).substring(12, output.get(diffLine).length());
				//Integer.parseInt(difficulty);
			}
		} while(numSolutions > 1 || output.size() < 2);
		//find maximum size of clues on left & top
		clueLen = new int[2];
		clueLen[0] = 0;
		clueLen[1] = 0;
		for(int i = 0; i < gameGrid.sizeY; i++) {
			if(gameGrid.cluesX[i].toString().length() > clueLen[0]) {
				clueLen[0] = gameGrid.cluesX[i].toString().length();
			}
		}
		clueLen[0] *= 7;
		clueLen[0] += 10;
		if(clueLen[0] < 100)
			clueLen[0] = 100;
		for(int i = 0; i < gameGrid.sizeX; i++) {
			if(gameGrid.cluesY[i].getValues().size() > clueLen[1]) {
				clueLen[1] = gameGrid.cluesY[i].getValues().size();
			}
		}
		clueLen[1] *= 12;
		clueLen[1] += 50;
		if(clueLen[1] < 130) {
			clueLen[1] = 130;
		}
		try {
			Files.deleteIfExists(FileSystems.getDefault().getPath("clues.nin"));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Prints a string in the center of the frame.
	 *
	 * @param f   font, analyzed to center text exactly
	 * @param s   string to print
	 * @param y   number of pixels from top of canvas where the *bottom* of the string should go
	 * @param art canvas to paint final string
	 */
	private void drawCenteredText(Font f, String s, int y, Graphics2D art) {
		int len = art.getFontMetrics(f).stringWidth(s);
		art.drawString(s, frame.getWidth() / 2 - len / 2, y);
	}

	/**
	 * Prints a string centered at (x, y).
	 *
	 * @param f   font, analyzed to center text exactly
	 * @param s   string to print
	 * @param x   x-value of the center in pixels
	 * @param y   number of pixels from top of canvas where the *bottom* of the string should go
	 * @param art canvas to paint final string
	 */
	public void drawCenteredText(Font f, String s, int x, int y, Graphics2D art) {
		int len = art.getFontMetrics(f).stringWidth(s);
		art.drawString(s, x - len / 2, y);
	}

	/**
	 * Prints a string aligned to the right of the frame.
	 *
	 * @param f   font, analyzed to find leftmost pixel of printed text
	 * @param s   string to print
	 * @param y   number of pixels from top of canvas where the *bottom* of the string should go
	 * @param art canvas to paint final string
	 */
	private void drawRightText(Font f, String s, int y, Graphics2D art) {
		int len = art.getFontMetrics(f).stringWidth(s);
		art.drawString(s, frame.getWidth() - len - 10, y);
	}

	/**
	 * Prints a string right-aligned to the point (x, y).
	 *
	 * @param f   font, analyzed to find leftmost pixel of printed text
	 * @param s   string to print
	 * @param x   x-value of the string end in pixels
	 * @param y   number of pixels from top of canvas where the *bottom* of the string should go
	 * @param art canvas to paint final string
	 */
	public void drawRightText(Font f, String s, int x, int y, Graphics2D art) {
		int len = art.getFontMetrics(f).stringWidth(s);
		art.drawString(s, x - len - 10, y);
	}

	/**
	 * Performs a predetermined action based on the button passed.
	 *
	 * @param b button to be compared with known buttons
	 */
	public void doClickAction(Button b) {
		if(b == bNewPuzzle) {
			b.setVisible(false);
			currWindow = "size picker";
		} else if(b == bResume) {
			status = "";
			bResume.setVisible(false);
			bMainMenu2.setVisible(false);
			bPause.setVisible(true);
			Main.timer.resume();
			playable = true;
			faded = false;
		} else if(b == bPause) {
			if(status.equals("")) {
				status = "paused";
				bPause.setVisible(false);
				bResume.setVisible(true);
				bMainMenu2.setVisible(true);
				Main.timer.pause();
				playable = false;
				faded = false;
			}
		} else if(b == bXUp) {
			if(modifier) {
				sizeX += 5;
			} else {
				sizeX++;
			}
		} else if(b == bXDown) {
			if(modifier) {
				sizeX -= 5;
			} else {
				sizeX--;
			}
		} else if(b == bYUp) {
			if(modifier) {
				sizeY += 5;
			} else {
				sizeY++;
			}
		} else if(b == bYDown) {
			if(modifier) {
				sizeY -= 5;
			} else {
				sizeY--;
			}
		} else if(b == bBack) {
			currWindow = "menu";
			bNewPuzzle.setVisible(true);
		} else if(b == bStart || b == bRegenPuzzle) {
			frame.setTitle("GENERATING...");
			{
				Graphics2D art = (Graphics2D) imgBuffer.getGraphics();
				RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				art.setRenderingHints(rh);
				art.setColor(new Color(128, 128, 255));
				art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				art.setColor(black);
				art.setFont(f.deriveFont(50f));
				f = f.deriveFont(50f);
				drawCenteredText(f, "Generating random puzzle...", frame.getHeight() / 2 + 25, art);
				art = (Graphics2D) frame.getGraphics();
				if(art != null) {
					art.drawImage(imgBuffer, 0, 0, frame.getWidth(), frame.getHeight(), 0, 0, frame.getWidth(), frame.getHeight(), null);
					art.dispose();
				}
			}
			b.setVisible(false);
			currWindow = "game";
			status = "get ready";
			bBegin.setVisible(true);
			bPause.setVisible(false);
			numMistakes = 0;
			bRegenPuzzle.setVisible(false);
			bMainMenu.setVisible(false);
			playable = false;
			generatePuzzle();
			Main.timer.reset();
		} else if(b == bMainMenu || b == bMainMenu2) {
			frame.setTitle("Main Menu | Picross");
			currWindow = "menu";
			status = "menu";
			numMistakes = 0;
			bMainMenu.setVisible(false);
			bMainMenu2.setVisible(false);
			bResume.setVisible(false);
			bRegenPuzzle.setVisible(false);
			bNewPuzzle.setVisible(true);
			playable = false;
		} else if(b == bQuitGame) {
			frame.setTitle("Quitting...");
			frame.setVisible(false);
			isRunning = false;
			frame.dispose();
			isDone = true;
			System.exit(0);
		} else if(b == bBegin) {
			b.setVisible(false);
			status = "";
			Main.timer.begin();
			playable = true;
			faded = false;
		}
	}

	/**
	 * Returns a color that slowly darkens to amt.
	 *
	 * @param amt      Amount to darken, from 0-255
	 * @param duration Time in frames to darken
	 * @return Color to cover frame with for a fading effect
	 */
	private Color fadeOn(int amt, int duration) {
		duration /= 10;
		if(numFadeFrames == duration) {
			numFadeFrames = 0;
			fadeAlpha = 0;
			faded = true;
		}
		fadeAlpha = numFadeFrames * amt / duration;
		Color out = faded ? new Color(0, 0, 0, amt) : new Color(0, 0, 0, fadeAlpha);
		if(!faded) {
			numFadeFrames++;
		}
		return out;
	}

	/**
	 * Returns a color that slowly lightens from amt to 0.
	 *
	 * @param amtInit  Initial darkness, will slowly approach 0
	 * @param duration Time in frames to lighten
	 * @return Color to cover frame with for a fading effect
	 */
	private Color fadeOff(int amtInit, int duration) {
		duration /= 10;
		if(numFadeFrames > duration) {
			numFadeFrames = 0;
			fadeAlpha = 0;
			faded = true;
		}
		fadeAlpha = amtInit - (numFadeFrames * amtInit / duration);
		Color out = faded ? new Color(0, 0, 0, 0) : new Color(0, 0, 0, fadeAlpha);
		if(!faded) {
			numFadeFrames++;
		}
		return out;
	}

	/**
	 * @param x1 x-coordinate of left bound of rectangle
	 * @param y1 y-coordinate of left bound of rectangle
	 * @param x2 x-coordinate of right bound of rectangle
	 * @param y2 y-coordinate of right bound of rectangle
	 * @return Returns whether the mouse's current positions falls within the defined bounds.
	 */
	private boolean isInBounds(int x1, int y1, int x2, int y2) {
		return (x > x1) && (x < x2) && (y > y1) && (y < y2);
	}

	private int getStrLen(String s, float fontHeight) {
		FontMetrics fm = frame.getGraphics().getFontMetrics(f.deriveFont(fontHeight));
		return fm.stringWidth(s);
	}

	public FancyFrame getFrame() {
		return frame;
	}
}
