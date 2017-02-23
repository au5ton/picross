package puzzleCreator;//TODO create interactive tutorial
//TODO begin work on solution creator jar

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static java.awt.Color.*;
import static puzzleCreator.Main.*;

public class Graphics implements Runnable, KeyListener, WindowListener {
	static int bSize;
	private final int MIN_BSIZE = 14;
	//base components
	private Grid grid;
	private Box currBox;
	private int x;
	private int y;
	private int numFadeFrames = 0;//counts frames for fading effect
	private int fadeAlpha;
	private int cWidth;
	private int sizeX = 10;
	private int sizeY = 10;
	//private Scanner s;
	private String currWindow;
	private String status;
	//flags
	private boolean isRunning;
	private boolean isDone;
	//private boolean playable;
	private boolean faded = false;
	private boolean modifier = false;
	private boolean puzzleInvalid = false;
	private boolean puzzleValid = false;
	private boolean showDialog = false;
	//graphics
	@SuppressWarnings("CanBeFinal")
	private FancyFrame frame;
	private Image imgBuffer;
	static int[] clueLen;
	private Font f;
	//button categories
	private ButtonList mainMenuButtons;
	private ButtonList sizePickerButtons;
	private ButtonList creatorButtons;
	//private ButtonList pauseMenuButtons;
	//buttons
	private Button bNewPuzzle;
	private Button bXUp;
	private Button bXDown;
	private Button bYUp;
	private Button bYDown;
	private Button bBack;
	private Button bStart;
	private Button bMainMenu;
	private Button bQuitGame;
	//private Button bBegin;
	private Button bCheck;
	private Button bClear;

	public Graphics() {
		//initialize frame & basic flags
		Dimension SIZE = new Dimension(800, 600);
		frame = new FancyFrame("Loading...", SIZE);
		frame.addKeyListener(this);
		frame.addWindowListener(this);
		//basic window flags
		isRunning = true;
		isDone = false;
		frame.setVisible(true);
		//makes graphics look like not trash
		imgBuffer = frame.createImage(frame.getWidth(), frame.getHeight());
		//important flags to determine what is displayed on screen
		status = "menu";
		currWindow = "menu";
		//initializes currBox so the game doesn't freak out
		currBox = null;
		//buttons, sliders, and checkboxes
		initButtons();
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		while (true) {
			if (isDone)
				frame.setVisible(false);
			try {
				Thread.sleep(100);
			} catch (InterruptedException ie) {
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
	public void keyPressed(KeyEvent e) {
		char keyChar = e.getKeyChar();
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_SHIFT) {
			modifier = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		int key = arg0.getKeyCode();
		if (key == KeyEvent.VK_SHIFT) {
			modifier = false;
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}

	@Override
	public void run() {
		while (isRunning && ! isDone) {
			switch (currWindow) {
				case "creator":
					bClear.setPos(20, frame.getHeight() - 50);
					creatorButtons.setVisible(true);
					//get size of each box for optimal display size, takes into account clueLen and mistakes box
					bSize = getBoxSize();
					frame.setMinimumSize(new Dimension(clueLen[0] + MIN_BSIZE * grid.sizeX + 25, clueLen[1] + MIN_BSIZE * grid.sizeY + 50));
					break;
				case "size picker":
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
					if (sizeX > 25) {
						sizeX = 25;
					}
					if (sizeX < 1) {
						sizeX = 1;
					}
					if (sizeY > 25) {
						sizeY = 25;
					}
					if (sizeY < 1) {
						sizeY = 1;
					}
					break;
				case "menu":
					if (f != null) {
						frame.setMinimumSize(new Dimension(getStrLen("MAIN MENU", 50f) + 25, 550));
					}
					bNewPuzzle.setPos(frame.getWidth() / 2 - 125, bNewPuzzle.getY());
					bQuitGame.setPos(frame.getWidth() / 2 - 125, bQuitGame.getY());
					break;
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

	/**
	 * Renders an image of the game.
	 */
	private void draw() {
		Graphics2D art = (Graphics2D) imgBuffer.getGraphics();
		art.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		art.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		f = art.getFont();
		switch (currWindow) {
			case "menu":
				frame.setTitle("Main Menu | Picross");
				art.setColor(new Color(128, 128, 255));
				//art.setColor(getRandomColor());
				art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				art.setColor(BLACK);
				art = setFont(50f, art);
				drawCenteredText(f, "MAIN MENU", 100, art);
				art = setFont(23f, art);
				drawCenteredText(f, "Loading...", bNewPuzzle.getY() + 65, art);
				//art.drawString("Loading...", frame.getWidth() / 2 - 125, bNewPuzzle.getY() + 65);
				mainMenuButtons.drawAll(x, y, art);
				art = setFont(20f, art);
				break;
			case "size picker":
				frame.setTitle("Size Picker | Picross");
				if (sizeX == 25 || (modifier && sizeX + 5 > 25)) {
					bXUp.setVisible(false);
				} else if (sizeX == 1 || (modifier && sizeX - 5 < 1)) {
					bXDown.setVisible(false);
				} else {
					bXUp.setVisible(true);
					bXDown.setVisible(true);
				}
				if (sizeY == 25 || (modifier && sizeY + 5 > 25)) {
					bYUp.setVisible(false);
				} else if (sizeY == 1 || (modifier && sizeY - 5 < 1)) {
					bYDown.setVisible(false);
				} else {
					bYUp.setVisible(true);
					bYDown.setVisible(true);
				}

				art.setColor(new Color(128, 128, 255));
				art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				art.setColor(BLACK);
				art = setFont(50f, art);
				drawCenteredText(f, "PICK A SIZE", 100, art);
				drawCenteredText(f, "X", bXUp.getX() + bXUp.getSize().width / 2, bXUp.getY() - 10, art);
				drawCenteredText(f, "Y", bYUp.getX() + bYUp.getSize().width / 2, bYUp.getY() - 10, art);
				drawCenteredText(f, Integer.toString(sizeX), bXUp.getX() + bXUp.getSize().width / 2, bXDown.getY() - 10, art);
				drawCenteredText(f, Integer.toString(sizeY), bYUp.getX() + bYUp.getSize().width / 2, bYDown.getY() - 10, art);
				art = setFont(20f, art);
				sizePickerButtons.drawAll(x, y, art);
				break;
			case "creator":
				frame.setTitle("Puzzle Creator");
				art.setColor(new Color(128, 128, 255));
				art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				art.setColor(fadeOff(64, 100));
				art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				art = setFont(12f, art);//12x7 pixels

				art.setColor(BLACK);
				cWidth = frame.getWidth() / 2 - (grid.sizeX * bSize / 2) - clueLen[0];
				if (cWidth < 0) {
					cWidth = 0;
				}
				for (int i = 0; i < (grid.sizeX); i++) {
					for (int j = 0; j < (grid.sizeY); j++) {
						grid.drawGrid(i, j, art, cWidth);
						art.setColor(BLACK);
						if (! showDialog && i == (x - clueLen[0] - cWidth) / bSize && j == (y - clueLen[1]) / bSize && x > clueLen[0] + cWidth && y > clueLen[1]) {
							art.setColor(new Color(0, 0, 0, 64));
							art.fillRect(clueLen[0] + i * bSize + cWidth, clueLen[1] + j * bSize, bSize, bSize);
						} else if (! showDialog && ((i == (x - clueLen[0] - cWidth) / bSize && x > clueLen[0] + cWidth) || (j == (y - clueLen[1]) / bSize && y > clueLen[1]))) {
							art.setColor(new Color(0, 0, 0, 32));
							art.fillRect(clueLen[0] + i * bSize + cWidth, clueLen[1] + j * bSize, bSize, bSize);
						}
						art.setColor(BLACK);
						art.drawRect(clueLen[0] + i * bSize + cWidth, clueLen[1] + j * bSize, bSize, bSize);
					}
				}
				for (int i = 0; i < grid.sizeX; i++) {
					grid.drawClues(i, 1, art, cWidth);
				}
				for (int j = 0; j < grid.sizeY; j++) {
					grid.drawClues(j, 0, art, cWidth);
				}
				creatorButtons.drawAll(x, y, art);
				for (int i = 5; i < grid.sizeX; i += 5) {
					art.drawLine(clueLen[0] + i * bSize + 1 + cWidth, clueLen[1], clueLen[0] + i * bSize + 1 + cWidth, clueLen[1] + grid.sizeY * bSize);
				}
				for (int i = 5; i < grid.sizeY; i += 5) {
					art.drawLine(clueLen[0] + cWidth, clueLen[1] + i * bSize + 1, clueLen[0] + grid.sizeX * bSize + cWidth, clueLen[1] + i * bSize + 1);
				}
				//display message if puzzle is valid/invalid
				Color fade = fadeOff(64, 100);
				if (showDialog && (puzzleInvalid || puzzleValid)) {
					if (puzzleValid) puzzleInvalid = false;
					art.setColor(fadeOn(64, 100));
					art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
					art.setColor(WHITE);
					art.fillRect(frame.getWidth() / 2 - 125, frame.getHeight() / 2 - 50, 250, 100);
					art.setColor(BLACK);
					art.drawRect(frame.getWidth() / 2 - 125, frame.getHeight() / 2 - 50, 250, 100);
					String message = puzzleValid ? "Puzzle works! Saving..." : "Puzzle is ambiguous!";
					art = setFont(20f, art);

					drawCenteredText(f, message, frame.getHeight() / 2 + 10, art);
				} else if (fade.getAlpha() != 0) {
					art.setColor(fade);
					art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				}
				//render mistakes/timer
				art = setFont(20f, art);
				art.setColor(BLACK);
				break;
		}
		art = (Graphics2D) frame.getGraphics();
		if (art != null) {
			imgBuffer = Resizer.PROGRESSIVE_BILINEAR.resize((BufferedImage) imgBuffer, frame.getWidth(), frame.getHeight());
			art.drawImage(imgBuffer, 0, 0, frame.getWidth(), frame.getHeight(), 0, 0, frame.getWidth(), frame.getHeight(), null);
			art.dispose();
		}
	}

	/**
	 * Creates a Grid with random states of size sizeX, sizeY.
	 */
	private void checkSolution() {
		//x = Integer.parseInt(s.nextLine());
		//y = Integer.parseInt(s.nextLine());
		for (int i = 0; i < sizeX; i++) {
			for (int j = 0; j < sizeY; j++) {
				//int b = s.nextInt();
				//TODO grab grid boxes and check

			}
		}
	}

	/**
	 * @return Returns the side length of a box in pixels based on graphics elements in game and frame size
	 */
	private int getBoxSize() {
		int temp = (frame.getWidth() - (clueLen[0] > clueLen[1] ? clueLen[0] : clueLen[1])) / (grid.sizeX + 1);
		if (temp * grid.sizeY + clueLen[1] + 50 > frame.getHeight()) {
			return Math.max((frame.getHeight() - 50 - clueLen[1]) / grid.sizeY, MIN_BSIZE);
		} else {
			return Math.max(temp, MIN_BSIZE);
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
			for (Clue c : grid.cluesX) {
				String s = c.toString(true);
				strings.write(s);
				strings.newLine();
			}
			for (Clue c : grid.cluesY) {
				String s = c.toString(true);
				strings.write(s);
				strings.newLine();
			}
			strings.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeClues(String fileName) {
		try {
			FileWriter writer = new FileWriter(fileName);
			BufferedWriter strings = new BufferedWriter(writer);
			strings.write(Integer.toString(sizeX) + " " + sizeY);
			strings.newLine();
			for (Clue c : grid.cluesX) {
				String s = c.toString(Clue.READABLE);
				strings.write(s);
				strings.newLine();
			}
			for (Clue c : grid.cluesY) {
				String s = c.toString(Clue.READABLE);
				strings.write(s);
				strings.newLine();
			}
			strings.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Performs any actions regarding mouse clicks that are not handled by the Button class. Includes gameplay and scrolling on the size picker.
	 */
	private void mouseActions() {
		if (frame.hasClicked()) {
			frame.setHasClicked(false);
		}
		switch (currWindow) {
			case "creator":
				//bound checking to prevent instant toggling of a flag
				if (! showDialog && currBox != null && (x - clueLen[0] - cWidth) / bSize < grid.sizeX && (y - clueLen[1]) / bSize < grid.sizeY && x > clueLen[0] + cWidth && y > clueLen[1] && currBox != grid.getBox((x - clueLen[0] - cWidth) / bSize, (y - clueLen[1]) / bSize)) {
					currBox.setCanModify(true);
				} else if (currBox != null && showDialog) {
					currBox.setCanModify(false);
				}
				//get box only if mouse is within game grid, otherwise it is null
				if (! showDialog && (x - clueLen[0] - cWidth) / bSize < grid.sizeX && (y - clueLen[1]) / bSize < grid.sizeY && x > clueLen[0] + cWidth && y > clueLen[1]) {
					currBox = grid.getBox((x - clueLen[0] - cWidth) / bSize, (y - clueLen[1]) / bSize);
				} else {
					currBox = null;
				}
				if (! showDialog && currBox != null) {
					if (frame.isClicking()) {
						//only disables boxes as the player attempts to modify them
						//left click = reveal
						if (frame.getMouseButton() == 3) {
							currBox.impossibru();
							grid.generateClues(grid);
							currBox.setCanModify(false);
						} else if (frame.getMouseButton() == 1) {
							//click box
							currBox.green();
							grid.generateClues(grid);
							currBox.setCanModify(false);
						}
						//right click = flag, is not checked with the solution to prevent cheating

					} else {
						currBox.setCanModify(true);
					}
				}
				if (frame.isClicking() && showDialog) {
					showDialog = false;
				}
				break;
			case "menu":
				//no special actions for menu
				break;
			case "size picker":
				if (frame.scrollAmt != 0) {
					if (isInBounds(bXUp.getX(), bXUp.getY() + bXUp.getSize().height, bXUp.getX() + bXUp.getSize().width, bXUp.getY() + bXUp.getSize().height + 60)) {
						if (modifier) {
							sizeX -= sizeX - (5 * frame.scrollAmt) > 0 && sizeX - (5 * frame.scrollAmt) <= 25 ? frame.scrollAmt * 5 : 0;
						} else {
							sizeX -= sizeX - frame.scrollAmt > 0 && sizeX - frame.scrollAmt <= 25 ? frame.scrollAmt : 0;
						}
					} else if (isInBounds(bYUp.getX(), bYUp.getY() + bYUp.getSize().height, bYUp.getX() + bYUp.getSize().width, bYUp.getY() + bYUp.getSize().height + 60)) {
						if (modifier) {
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

	private int checkPuzzle() throws InterruptedException {
		List<String> output;
		int numSolutions = 0;
		checkSolution();
		grid.generateClues(grid);
		writeClues();
		runSolver();
		do {
			output = LogStreamReader.output;
			Thread.sleep(100);
		} while (output.size() < 5);
		int solutionsLine = Strings.findLineWith(output, "Solutions : ", true);
		numSolutions = Integer.parseInt(output.get(solutionsLine).substring(output.get(solutionsLine).length() - 1, output.get(solutionsLine).length()));
		try {
			Files.deleteIfExists(FileSystems.getDefault().getPath("clues.nin"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return numSolutions;
	}

	private void getClueLen() {
		//find maximum size of clues on left & top
		clueLen = new int[2];
		clueLen[0] = 0;
		clueLen[1] = 0;
		for (int i = 0; i < grid.sizeY; i++) {
			if (grid.cluesX[i].toString().length() > clueLen[0]) {
				clueLen[0] = grid.cluesX[i].toString().length();
			}
		}
		clueLen[0] *= 7;
		clueLen[0] += 10;
		if (clueLen[0] < 100)
			clueLen[0] = 100;
		for (int i = 0; i < grid.sizeX; i++) {
			if (grid.cluesY[i].getValues().size() > clueLen[1]) {
				clueLen[1] = grid.cluesY[i].getValues().size();
			}
		}
		clueLen[1] *= 12;
		clueLen[1] += 50;
		if (clueLen[1] < 130) {
			clueLen[1] = 130;
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
	void drawCenteredText(Font f, String s, int x, int y, Graphics2D art) {
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
	void drawRightText(Font f, String s, int x, int y, Graphics2D art) {
		int len = art.getFontMetrics(f).stringWidth(s);
		art.drawString(s, x - len - 10, y);
	}

	/**
	 * Performs a predetermined action based on the button passed.
	 *
	 * @param b button to be compared with known buttons
	 */
	void doClickAction(Button b) {
		if (b == bNewPuzzle) {
			b.setVisible(false);
			currWindow = "size picker";
		} else if (b == bXUp) {
			if (modifier) {
				sizeX += 5;
			} else {
				sizeX++;
			}
		} else if (b == bXDown) {
			if (modifier) {
				sizeX -= 5;
			} else {
				sizeX--;
			}
		} else if (b == bYUp) {
			if (modifier) {
				sizeY += 5;
			} else {
				sizeY++;
			}
		} else if (b == bYDown) {
			if (modifier) {
				sizeY -= 5;
			} else {
				sizeY--;
			}
		} else if (b == bBack) {
			currWindow = "menu";
			mainMenuButtons.setVisible(true);
		} else if (b == bStart) {
			frame.setTitle("LOADING...");
			{
				Graphics2D art = (Graphics2D) imgBuffer.getGraphics();
				RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				art.setRenderingHints(rh);
				art.setColor(new Color(128, 128, 255));
				art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				art.setColor(black);
				art = setFont(50f, art);
				drawCenteredText(f, "Initializing puzzle environment...", frame.getHeight() / 2 + 25, art);
				art = (Graphics2D) frame.getGraphics();
				if (art != null) {
					art.drawImage(imgBuffer, 0, 0, frame.getWidth(), frame.getHeight(), 0, 0, frame.getWidth(), frame.getHeight(), null);
					art.dispose();
				}
			}
			b.setVisible(false);
			currWindow = "creator";
			status = "get ready";
			grid = new Grid(sizeX, sizeY);
			getClueLen();
			bMainMenu.setVisible(false);
		} else if (b == bMainMenu) {
			frame.setTitle("Main Menu | Picross");
			currWindow = "menu";
			status = "menu";
			bMainMenu.setVisible(false);
			bNewPuzzle.setVisible(true);
		} else if (b == bQuitGame) {
			frame.setTitle("Quitting...");
			frame.setVisible(false);
			isRunning = false;
			frame.dispose();
			isDone = true;
			frame.setVisible(false);
//			Thread.currentThread().stop();
		} else if (b == bClear) {
			grid = new Grid(sizeX, sizeY);
			grid.generateClues(grid);
		} else if (b == bCheck) {
			int numSolutions = 0;
			try {
				numSolutions = checkPuzzle();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			showDialog = true;
			if (numSolutions > 1) {
				puzzleInvalid = true;
			} else if (numSolutions == 1) {
				puzzleValid = true;
				puzzleInvalid = false;
				int numPuzzles = getNumPuzzles();
				String fileName = "." + slashCharacter + "saves" + slashCharacter + "Puzzle" + String.format("%03d", numPuzzles + 1) + ".nin";
				System.out.println("Saving as Puzzle" + String.format("%03d", numPuzzles + 1) + ".nin");
				writeClues(fileName);
			}
		}
	}

	/**
	 * Returns a color that slowly darkens to amt.
	 *
	 * @param amt      Amount to darken, from 0-255
	 * @param duration Time in frames to darken
	 * @return Color to cover frame with for a fading effect
	 */
	@SuppressWarnings("SameParameterValue")
	private Color fadeOn(int amt, int duration) {
		duration /= 10;
		if (numFadeFrames == duration) {
			numFadeFrames = 0;
			fadeAlpha = 0;
			faded = true;
		}
		fadeAlpha = numFadeFrames * amt / duration;
		Color out = faded ? new Color(0, 0, 0, amt) : new Color(0, 0, 0, fadeAlpha);
		if (! faded) {
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
	@SuppressWarnings("SameParameterValue")
	private Color fadeOff(int amtInit, int duration) {
		duration /= 10;
		if (numFadeFrames > duration) {
			numFadeFrames = 0;
			fadeAlpha = 0;
			faded = true;
		}
		fadeAlpha = amtInit - (numFadeFrames * amtInit / duration);
		Color out = faded ? new Color(0, 0, 0, 0) : new Color(0, 0, 0, fadeAlpha);
		if (! faded) {
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

	private int getMaxStrLen(List<String> strings, float fontHeight) {
		int max = 0;
		for (String s : strings) {
			if (getStrLen(s, fontHeight) > max) {
				max = getStrLen(s, fontHeight);
			}
		}
		return max;
	}

	FancyFrame getFrame() {
		return frame;
	}

	private Graphics2D setFont(float size, Graphics2D art) {
		art.setFont(f.deriveFont(size));
		f = f.deriveFont(size);
		return art;
	}

	private void initButtons() {
		mainMenuButtons = new ButtonList();
		bNewPuzzle = new Button(frame.getWidth() / 2 - 125, 125, 250, 100, "Create Puzzle", GREEN, 20);
		bNewPuzzle.setVisible(true);
		bQuitGame = new Button(frame.getWidth() / 2 - 125, 275, 250, 100, "Exit", RED, 20);
		bQuitGame.setVisible(true);
		mainMenuButtons.addButtons(new Button[] {bNewPuzzle, bQuitGame});

		sizePickerButtons = new ButtonList();
		bXUp = new Button(300, 400, 100, 50, "Λ", 30);
		bXDown = new Button(300, 510, 100, 50, "V", 30);
		bYUp = new Button(600, 400, 100, 50, "Λ", 30);
		bYDown = new Button(600, 510, 100, 50, "V", 30);
		bBack = new Button(10, 55, 50, 50, "<", RED, 30);
		bStart = new Button(frame.getWidth() / 2 - 50, frame.getHeight() - 100, 100, 75, "CREATE", GREEN, 30);
		sizePickerButtons.addButtons(new Button[] {bXUp, bXDown, bYUp, bYDown, bBack, bStart});

		creatorButtons = new ButtonList();
		bMainMenu = new Button(20, 40, 60, 30, "Menu", YELLOW, 17);
		bCheck = new Button(20, 80, 60, 30, "Check", GREEN, 17);
		bClear = new Button(20, frame.getHeight() - 50, 60, 30, "CLEAR", RED, 17);
		creatorButtons.addButtons(new Button[] {bMainMenu, bCheck, bClear});

		//pauseMenuButtons = new ButtonList();
		//bBegin = new Button(frame.getWidth() / 2 - 100, frame.getHeight() / 2 - 50, 200, 100, "BEGIN", GREEN, 20);
		//pauseMenuButtons.addButtons(new Button[] {bBegin});
	}
}
