package picross;//TODO create interactive tutorial
//TODO redesign main menu, similar to original but new color scheme

import common.Background;
import common.DrawingTools;
import common.TextEntryBox;

import java.awt.Color;
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
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.awt.Color.*;
import static picross.Main.*;

public class Graphics implements Runnable, KeyListener, WindowListener {

	private static final String VERSION = "v1.4";
	static int bSize;
	private static int numFrames = 0;
	private final int MIN_BSIZE = 14;
	private final int controlMouse = 0;
	private final int controlKeyboard = 1;
	//base components
	private Grid gameGrid;
	private Grid solutionGrid;
	private Box currBox;
	private int mouseX;
	private int mouseY;
	private int savedMouseX;
	private int savedMouseY;
	private int kbX;
	private int kbY;
	private int controlMode;
	private int numMistakes;
	private int numFadeFrames = 0;//counts frames for fading effect
	private int fadeAlpha;
	private int cWidth;
	private int sizeX;
	private int sizeY;
	private int fps = 0;
	private int scrollIndex = 0;
	private Scanner s;
	private String currWindow;
	private Stack<String> windows;
	private String status;
	//flags
	private boolean isRunning;
	private boolean isDone;
	private boolean playable;
	private boolean faded = false;
	private boolean modifier = false;
	private boolean debugging = false;
	private boolean pushingSolveKey = false;
	private boolean scoreSubmitted = false;
	//graphics
	@SuppressWarnings("CanBeFinal")
	private FancyFrame frame;
	private Image imgBuffer;
	private Color bgColor = new Color(128, 128, 255);
	static int[] clueLen;
	private Font f;
	//controls menu elements
	private List<Button> controlsButtons;
	private List<String> controlsDescriptions;
	//key assignments
	private int keyPauseGame;
	private int keyUp;
	private int keyLeft;
	private int keyDown;
	private int keyRight;
	private int keyResolve1;
	private int keyResolve2;
	private int keyCancelAssignment = KeyEvent.VK_BACK_SPACE;
	private String keyAssigning = null;
	//all buttons
	private AllButtons allButtons;
	//button categories
	private ButtonList mainMenuButtons;
	private ButtonList gameChoiceButtons;
	private ButtonList loadMenuButtons;
	private ButtonList sizePickerButtons;
	private ButtonList gameButtons;
	private ButtonList pauseMenuButtons;
	private ButtonList optionsMenuButtons;
	private ButtonList controlsMenuButtons;
	private ButtonList gameEndButtons;
	private ButtonList puzzleButtons;//load screen entries (puzzles to choose from)
	//buttons
	private Button bPause;
	private Button bResume;
	private Button bNewPuzzle;
	private Button bXUp;
	private Button bXDown;
	private Button bYUp;
	private Button bYDown;
	private Button bBack;
	private Button bStart;
	private Button bMainMenu;
	private Button bMainMenu2;
	private Button bQuitGame;
	private Button bBegin;
	private Button bRegenPuzzle;
	private Button bControlsMenu;
	private Button bCreator;
	private Button bRandomPuzzle;
	private Button bLoadPuzzle;
	private Button bRestoreControls;
	private Button bGamba;
	//text boxes
	private TextEntryBox userNameBox;

	public Graphics() {
		FPSCounter.begin();
		//initialize frame & basic flags
		Dimension SIZE = new Dimension(800, 600);
		frame = new FancyFrame("Loading...", SIZE);
		frame.addKeyListener(this);
		frame.addWindowListener(this);
		frame.setLocationRelativeTo(null);
		//basic window flags
		isRunning = true;
		isDone = false;
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage("src/resources/icon.png"));
		frame.setVisible(true);
		//makes graphics look like not trash
		imgBuffer = frame.createImage(frame.getWidth(), frame.getHeight());
		//important flags to determine what is displayed on screen
		playable = false;
		status = "menu";
		currWindow = "menu";
		windows = new Stack<>();
		windows.push(currWindow);
		//grab size from file
		if (prefs.get("size").equals("0,0") || prefs.get("size").equals("null")) {
			prefs.put("size", "10,10");
		}
		String size = prefs.get("size");
		sizeX = Integer.parseInt(size.substring(0, size.indexOf(',')));
		sizeY = Integer.parseInt(size.substring(size.indexOf(',') + 1));
		//initializes currBox so the game doesn't freak out
		currBox = null;
		//buttons, sliders, and checkboxes
		displayStatus("Creating buttons...");
		initButtons();
		initControls();
		userNameBox = new TextEntryBox(200, 30, frame.getWidth() / 2, frame.getHeight() / 2 + 100);
		displayStatus("Setting up graphics...");
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		while (true) {
			if (isDone) {
				System.exit(0);
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		writePrefs();
		frame.setVisible(false);
		isRunning = false;
		frame.dispose();
		isDone = true;
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		try {
			doClickAction(bPause);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		if (keyAssigning == null) {
			if (keyCode == KeyEvent.VK_SHIFT) {
				modifier = true;
			} else if (keyCode == keyPauseGame) {
				try {
					doClickAction(bPause);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			} else if (keyCode == keyUp || keyCode == keyDown || keyCode == keyLeft || keyCode == keyRight) {
				//enter keyboard mode
				if (controlMode == controlMouse) {
					savedMouseX = mouseX;
					savedMouseY = mouseY;
					kbX = currBox == null ? 0 : currBox.getPos()[0];
					kbY = currBox == null ? 0 : currBox.getPos()[1];
					controlMode = controlKeyboard;
					System.out.println("Entering keyboard control mode");
				} else {
					if (keyCode == keyUp && kbY > 0) {
						kbY--;
					}
					if (keyCode == keyDown && kbY < sizeY - 1) {
						kbY++;
					}
					if (keyCode == keyLeft && kbX > 0) {
						kbX--;
					}
					if (keyCode == keyRight && kbX < sizeX - 1) {
						kbX++;
					}
//				System.out.println("Keyboard X pos: " + kbX + ", Y pos: " + kbY);
				}
			} else if (controlMode == controlKeyboard && (keyCode == keyResolve1 || keyCode == keyResolve2)) {
				pushingSolveKey = true;
			}
		} else if (keyCode != keyCancelAssignment) {
			switch (keyAssigning) {
				case "pauseGame":
					keyPauseGame = keyCode;
					break;
				case "up":
					keyUp = keyCode;
					break;
				case "left":
					keyLeft = keyCode;
					break;
				case "down":
					keyDown = keyCode;
					break;
				case "right":
					keyRight = keyCode;
					break;
				case "resolve1":
					keyResolve1 = keyCode;
					break;
				case "resolve2":
					keyResolve2 = keyCode;
					break;
			}
			updateButtons("controls");
			keyAssigning = null;
		} else {
			keyAssigning = null;
			updateButtons("controls");
		}
		if (keyChar == 'd' && ! userNameBox.hasFocus()) {
			debugging = true;
		}
		if (userNameBox.hasFocus()) {
			userNameBox.handleKey(e);
		}
		if (keyCode == KeyEvent.VK_ENTER && userNameBox.getText().length() > 0 && userNameBox.hasFocus()) {
			submitScore();
			userNameBox.setText("");
			userNameBox.setHasFocus(false);
			userNameBox.setVisible(false);
			scoreSubmitted = true;
		}
	}

	private void submitScore() {
		String username = userNameBox.getText().trim();
		double time = (double) (Main.timer.getMS()) / 1000;
		System.out.println("Sending score to server...");
		try {
			URL url = new URL("https://westonreed.com/picross/addscore.php?username=" + username + "&time=" + time + "&size=" + sizeX + "x" + sizeY);
			url.openStream();
		} catch (IOException e) {
			Logger.getLogger(Graphics.class.getName()).log(Level.SEVERE, null, e);
			//TODO: Display menu stating that a connection couldn't be established.
		}

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		int key = arg0.getKeyCode();
		if (key == KeyEvent.VK_SHIFT) {
			modifier = false;
		} else if (key == KeyEvent.VK_D) {
			debugging = false;
		} else if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE) {
			pushingSolveKey = false;
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}

	@Override
	public void run() {
		while (isRunning) {
			Background.updateColor();
			bgColor = Background.getCurrentColor();
			int i;
			switch (currWindow) {
				case "game":
					if (playable) {
						bPause.setVisible(true);
						bGamba.setVisible(true);
					} else {
						bBegin.setPos(frame.getWidth() / 2 - 100, frame.getHeight() / 2 - 50);
						bResume.setPos(frame.getWidth() / 2 - 100, frame.getHeight() / 2 + 7);
						bMainMenu.setPos(frame.getWidth() / 2 - 100, frame.getHeight() / 2 + 7);
						bMainMenu2.setPos(frame.getWidth() / 2, frame.getHeight() / 2 + 7);
						bRegenPuzzle.setPos(frame.getWidth() / 2, frame.getHeight() / 2 + 7);
						bCreator.setPos(frame.getWidth() / 2 - 100, bCreator.getY());
					}
					//get size of each box for optimal display size, takes into account clueLen and mistakes box
					bSize = getBoxSize();

					frame.setMinimumSize(new Dimension(
							245 + getStrLen("TIME: " + Main.timer.toString(false), 20f) + 25
									> clueLen[0] + MIN_BSIZE * gameGrid.sizeX + 25
									? 245 + getStrLen("TIME: " + Main.timer.toString(false), 20f) + 25
									: clueLen[0] + MIN_BSIZE * gameGrid.sizeX + 25, clueLen[1] + MIN_BSIZE * gameGrid.sizeY + 50
					));
					//check for completeness
					boolean temp = true;
					for (i = 0; i < gameGrid.sizeX; i++) {
						for (int j = 0; j < gameGrid.sizeY; j++) {
							if (gameGrid.getBox(i, j).getState() != 1 && solutionGrid.getBox(i, j).getState() == 1) {
								temp = false;
							}
						}
					}
					if (temp) {
						status = "solved";
						allButtons.setWindow("game end");
						playable = false;
						if (Main.timer != null) {
							Main.timer.pause();
						}
						int timeInSeconds = Main.timer.getSeconds();
						int MS = timeInSeconds - Main.timer.getMS();
						if (! scoreSubmitted)
							userNameBox.setVisible(true);
					}
					//maximum mistakes
					if (numMistakes == 5) {
						status = "failed";
						allButtons.setWindow("game end");
						playable = false;
						Main.timer.pause();
					}
					//maximum time
					if (Main.timer.getHours() > 9) {
						status = "failed";
						playable = false;
						Main.timer.pause();
					}
					if (status.equals("paused") && ! Main.animator.isRunning()) {
						Main.animator.begin();
					} else if (! status.equals("paused") && Main.animator.isRunning()) {
						Main.animator.reset();
					}
					if (controlMode == controlKeyboard) {
						int mouseDiffX = Math.abs(mouseX - savedMouseX);
						int mouseDiffY = Math.abs(mouseY - savedMouseY);
						if (mouseDiffX > 10 || mouseDiffY > 10) {
							controlMode = controlMouse;
							System.out.println("Entering mouse control mode");
						}
					}
					userNameBox.setCenterX(frame.getWidth() / 2);
					userNameBox.setCenterY(frame.getHeight() / 2 + 100);
					break;
				case "gamemode":
					gameChoiceButtons.setVisible(true);
					bRandomPuzzle.setPos(frame.getWidth() / 2 - 100, bRandomPuzzle.getY());
					bLoadPuzzle.setPos(frame.getWidth() / 2 - 100, bLoadPuzzle.getY());
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
					mainMenuButtons.setVisible(true);
					if (f != null) {
						frame.setMinimumSize(new Dimension(getStrLen("MAIN MENU", 50f) + 25, 550));
					}
					bNewPuzzle.setPos(frame.getWidth() / 2 - 100, bNewPuzzle.getY());
					bQuitGame.setPos(frame.getWidth() / 2 - 100, bQuitGame.getY());
					bControlsMenu.setPos(frame.getWidth() / 2 - 100, bControlsMenu.getY());
					bCreator.setPos(frame.getWidth() / 2 - 100, bCreator.getY());
					break;
				case "options":
					bBack.setVisible(true);
					break;
				case "controls":
					i = 0;
					for (Button b : controlsMenuButtons.toList()) {
						if (b instanceof ControlsButton) {
							b.setPos(100, 150 + (50 * i));
							i++;
						}
					}
					bRestoreControls.setPos(frame.getWidth() - 150 - 10, bRestoreControls.getY());
					break;
				case "load":
					puzzleButtons.setVisible(false);
					for (i = scrollIndex; i < scrollIndex + (puzzleButtons.size() >= 5 ? 5 : puzzleButtons.size()); i++) {
						Button b = puzzleButtons.get(i);
						b.setX(50);
						int workingHeight = frame.getHeight() - 150;
						int topBuffer = 100;
						b.setY(workingHeight * (i - scrollIndex) / 5 + topBuffer + 25);
						b.setSizeX(frame.getWidth() - 100);
						b.setSizeY(workingHeight / 5 - 25);
						b.setVisible(true);
					}
					break;
			}
			mouseX = frame.mouseX;
			mouseY = frame.mouseY;
			mouseActions();
			draw();
			try {
				Thread.sleep(10);
				numFrames++;
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
				art.setColor(bgColor);
				//art.setColor(getRandomColor());
				art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				art.setColor(BLACK);
				art = setFont(50f, art);
				drawCenteredText(f, "MAIN MENU", 100, art);
				art = setFont(20f, art);
				art.drawString(VERSION, 15, frame.getHeight() - 15);
				break;
			case "options":
				art.setColor(bgColor);
				art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				art.setColor(BLACK);
				art = setFont(50f, art);
				drawCenteredText(f, "OPTIONS", 100, art);
				break;
			case "gamemode":
				art.setColor(bgColor);
				art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				art.setColor(BLACK);
				art = setFont(50f, art);
				drawCenteredText(f, "CHOOSE GAMEMODE", 100, art);
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

				art.setColor(bgColor);
				art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				art.setColor(BLACK);
				art = setFont(50f, art);
				drawCenteredText(f, "SIZE PICKER", 100, art);
				drawCenteredText(f, "X", bXUp.getX() + bXUp.getSize().width / 2, bXUp.getY() - 10, art);
				drawCenteredText(f, "Y", bYUp.getX() + bYUp.getSize().width / 2, bYUp.getY() - 10, art);
				drawCenteredText(f, Integer.toString(sizeX), bXUp.getX() + bXUp.getSize().width / 2, bXDown.getY() - 10, art);
				drawCenteredText(f, Integer.toString(sizeY), bYUp.getX() + bYUp.getSize().width / 2, bYDown.getY() - 10, art);
				art = setFont(20f, art);
				break;
			case "game":
				frame.setTitle("" + Main.timer.toString(true) + " | Picross");
				art.setColor(bgColor);
				art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				if (playable) {
					art.setColor(fadeOff(64, 100));
					art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				}
				art = setFont(12f, art);//12x7 pixels

				art.setColor(BLACK);
				cWidth = frame.getWidth() / 2 - (gameGrid.sizeX * bSize / 2) - clueLen[0];
				if (cWidth < 0) {
					cWidth = 0;
				}
				for (int i = 0; i < (gameGrid.sizeX); i++) {
					for (int j = 0; j < (gameGrid.sizeY); j++) {
						gameGrid.drawGrid(i, j, art, cWidth);
						art.setColor(BLACK);
						//if(playable && i == (mouseX - clueLen[0] - cWidth) / bSize && j == (mouseY - clueLen[1]) / bSize && mouseX > clueLen[0] + cWidth && mouseY > clueLen[1]) {
						if (playable && currBox != null && i == currBox.getPos()[0] && j == currBox.getPos()[1]) {
							art.setColor(new Color(0, 0, 0, 64));
							art.fillRect(clueLen[0] + i * bSize + cWidth, clueLen[1] + j * bSize, bSize, bSize);
							//} else if(playable && ((i == (mouseX - clueLen[0] - cWidth) / bSize && mouseX > clueLen[0] + cWidth) || (j == (mouseY - clueLen[1]) / bSize && mouseY > clueLen[1]))) {
						} else if (controlMode == controlKeyboard) {
							if (playable && currBox != null && (i == currBox.getPos()[0] || j == currBox.getPos()[1])) {
								art.setColor(new Color(0, 0, 0, 32));
								art.fillRect(clueLen[0] + i * bSize + cWidth, clueLen[1] + j * bSize, bSize, bSize);
							}
						} else if (playable && ((i == (mouseX - clueLen[0] - cWidth) / bSize && mouseX > clueLen[0] + cWidth) || (j == (mouseY - clueLen[1]) / bSize && mouseY > clueLen[1]))) {
							art.setColor(new Color(0, 0, 0, 32));
							art.fillRect(clueLen[0] + i * bSize + cWidth, clueLen[1] + j * bSize, bSize, bSize);
						}
						art.setColor(BLACK);
						art.drawRect(clueLen[0] + i * bSize + cWidth, clueLen[1] + j * bSize, bSize, bSize);
					}
				}
				if (! status.equals("paused") && ! status.equals("get ready")) {
					for (int i = 0; i < gameGrid.sizeX; i++) {
						Clue cTemp = new Clue(i, 1);
						cTemp.generateClue(gameGrid);
						if (cTemp.getValues().equals(gameGrid.cluesY[i].getValues())) {
							art.setColor(new Color(0, 0, 0, 128));
							for (int box = 0; box < gameGrid.sizeY; box++) {
								Box b = gameGrid.getBox(i, box);
								if (b.getState() == 0) {
									b.impossibru();
								}
							}
						} else {
							art.setColor(BLACK);
						}
						gameGrid.drawClues(i, 1, art, cWidth);
					}
					for (int j = 0; j < gameGrid.sizeY; j++) {
						Clue cTemp = new Clue(j, 0);
						cTemp.generateClue(gameGrid);
						if (cTemp.getValues().equals(gameGrid.cluesX[j].getValues())) {
							art.setColor(new Color(0, 0, 0, 128));
							for (int box = 0; box < gameGrid.sizeX; box++) {
								Box b = gameGrid.getBox(box, j);
								if (b.getState() == 0) {
									b.impossibru();
								}
							}
						} else {
							art.setColor(BLACK);
						}
						gameGrid.drawClues(j, 0, art, cWidth);
					}
				}
				for (int i = 5; i < gameGrid.sizeX; i += 5) {
					art.drawLine(clueLen[0] + i * bSize + 1 + cWidth, clueLen[1], clueLen[0] + i * bSize + 1 + cWidth, clueLen[1] + gameGrid.sizeY * bSize);
				}
				for (int i = 5; i < gameGrid.sizeY; i += 5) {
					art.drawLine(clueLen[0] + cWidth, clueLen[1] + i * bSize + 1, clueLen[0] + gameGrid.sizeX * bSize + cWidth, clueLen[1] + i * bSize + 1);
				}
				art = setFont(20f, art);
				art.drawString("MISTAKES: ", 10, frame.getHeight() - 15);
				int xRendered = 0,
						mistakesTemp = numMistakes;
				art.drawRect(120, frame.getHeight() - 35, 125, 25);
				art.setColor(RED);
				while (mistakesTemp > 0 && xRendered < 5) {
					art.drawString("X", xRendered * 25 + 125, frame.getHeight() - 15);
					mistakesTemp--;
					xRendered++;
				}
				while (xRendered < 5) {
					art.setColor(new Color(0, 0, 0, 64));
					art.drawString("X", xRendered * 25 + 125, frame.getHeight() - 15);
					xRendered++;
				}
				if (! playable) {
					bPause.setVisible(false);
					bGamba.setVisible(false);
					if (bBegin.isVisible()) {
						faded = true;
					}
					art.setColor(fadeOn(64, 100));
					art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
					art.setColor(WHITE);
					art.fillRect(frame.getWidth() / 2 - 100, frame.getHeight() / 2 - 50, 200, 100);
					art.setColor(BLACK);
					art.drawRect(frame.getWidth() / 2 - 100, frame.getHeight() / 2 - 50, 200, 100);
					String showText = "";

					art = setFont(30f, art);
					switch (status) {
						case "solved":
							art.setColor(Color.black);
							art = setFont(20f, art);
							if (! scoreSubmitted)
								DrawingTools.drawCenteredText(f, "Enter user name for score submission:", frame.getWidth() / 2, frame.getHeight() / 2 + 75, art);
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
							if (Main.animator.getMS() % 1000 <= 500) {
								drawCenteredText(f, "PAUSED", frame.getHeight() / 2 - 10, art);
							}
							bResume.draw(art);
							bMainMenu.draw(art);
							break;
					}
					if (status.equals("get ready")) {
						bBegin.draw(art);
					}
					drawCenteredText(f, showText, frame.getHeight() / 2 - 10, art);
					art.setColor(BLACK);
					//if(!status.equals("get ready") && !status.equals("paused"))
					//art.drawString("TIME:" + Main.timer.toString(), frame.getWidth() / 2 - 45, frame.getHeight() / 2 - 12);
				}
				//render mistakes/timer
				art = setFont(20f, art);
				art.setColor(BLACK);
				drawRightText(f, "TIME: " + Main.timer.toString(false), frame.getHeight() - 15, art);
				userNameBox.draw(art);
				break;
			case "controls":
				art.setColor(bgColor);
				art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				art.setColor(black);
				art = setFont(50f, art);
				drawCenteredText(f, "CONTROLS", 100, art);
				art.drawRect(100, 150, frame.getWidth() - 200, frame.getHeight() - 250);
				frame.setMinimumSize(new Dimension(100 + 100 + 10 + getMaxStrLen(controlsDescriptions, 25f) + 100, 150 + (50 * (controlsMenuButtons.size() - 2)) + 100));
				art = setFont(25f, art);
				for (int i = 0; i < controlsDescriptions.size(); i++) {
					art.drawString(controlsDescriptions.get(i), 210, 150 + 50 * (i + 1) - 15);
				}
				break;
			case "load":
				art.setColor(bgColor);
				art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
				art.setColor(BLACK);
				art = setFont(50f, art);
				drawCenteredText(f, "LOAD A PUZZLE", 100, art);
				loadMenuButtons.drawAll(art);
				puzzleButtons.drawAll(art);
				break;
		}
		allButtons.drawButtons(mouseX, mouseY, art);
		if (Main.FPSCounter.getMS() > 1000) {
			Main.FPSCounter.begin();
			fps = numFrames;
			numFrames = 0;
		}
		art.setColor(black);
		art = setFont(12f, art);
		if (debugging) {
			art.drawString("" + fps + " FPS", 20, 50);
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
	private void getSolution() {
		//mouseX = Integer.parseInt(s.nextLine());
		//mouseY = Integer.parseInt(s.nextLine());
		gameGrid = new Grid(sizeX, sizeY);
		solutionGrid = new Grid(sizeX, sizeY);
		for (int i = 0; i < sizeX; i++) {
			for (int j = 0; j < sizeY; j++) {
				//int b = s.nextInt();
				Random random = new Random();
				int b = random.nextInt(2);
				if (b == 1) {
					solutionGrid.getBox(i, j).setState(1);
				}
			}
		}
	}

	/**
	 * @return Returns the side length of a box in pixels based on graphics
	 * elements in game and frame size
	 */
	private int getBoxSize() {
		int temp = (frame.getWidth() - (clueLen[0] > clueLen[1] ? clueLen[0] : clueLen[1])) / (gameGrid.sizeX + 1);
		if (temp * gameGrid.sizeY + clueLen[1] + 50 > frame.getHeight()) {
			return (frame.getHeight() - 50 - clueLen[1]) / gameGrid.sizeY > MIN_BSIZE ? (frame.getHeight() - 50 - clueLen[1]) / gameGrid.sizeY : MIN_BSIZE;
		} else {
			return temp > MIN_BSIZE ? temp : MIN_BSIZE;
		}
	}

	/**
	 * Writes generated clues of a random puzzle to a file, to be read by the
	 * solver program.
	 */
	private void writeClues() {
		try {
			FileWriter writer = new FileWriter("clues.nin");
			BufferedWriter strings = new BufferedWriter(writer);
			strings.write(Integer.toString(sizeX) + " " + sizeY);
			strings.newLine();
			for (Clue c : gameGrid.cluesX) {
				String s = c.toString(true);
				strings.write(s);
				strings.newLine();
			}
			for (Clue c : gameGrid.cluesY) {
				String s = c.toString(true);
				strings.write(s);
				strings.newLine();
			}
			strings.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Performs any actions regarding mouse clicks that are not handled by the
	 * Button class. Includes gameplay and scrolling on the size picker.
	 */
	private void mouseActions() {
		if (frame.hasClicked()) {
			frame.setHasClicked(false);
		}
		switch (currWindow) {
			case "game":
				if (controlMode == controlMouse) {
					//bound checking to prevent instant toggling of a flag
					if (currBox != null && (mouseX - clueLen[0] - cWidth) / bSize < gameGrid.sizeX && (mouseY - clueLen[1]) / bSize < gameGrid.sizeY && mouseX > clueLen[0] + cWidth && mouseY > clueLen[1] && currBox != gameGrid.getBox((mouseX - clueLen[0] - cWidth) / bSize, (mouseY - clueLen[1]) / bSize)) {
						currBox.setCanModify(true);
					}
					//get box only if mouse is within game grid, otherwise it is null
					if ((mouseX - clueLen[0] - cWidth) / bSize < gameGrid.sizeX && (mouseY - clueLen[1]) / bSize < gameGrid.sizeY && mouseX > clueLen[0] + cWidth && mouseY > clueLen[1]) {
						currBox = gameGrid.getBox((mouseX - clueLen[0] - cWidth) / bSize, (mouseY - clueLen[1]) / bSize);
					} else {
						currBox = null;
					}
					if (currBox != null) {
						if (frame.isClicking()) {
							//only disables boxes as the player attempts to modify them
							if (! playable) {
								currBox.setCanModify(false);
							}
							//left click = reveal
							if (frame.getMouseButton() == 3) {
								currBox.impossibru();
								currBox.setCanModify(false);
							} else if (frame.getMouseButton() == 1) {
								//click buttons
								//if the box is not part of the solution, you made a mistake
								if (! currBox.green(solutionGrid)) {
									numMistakes++;
									Main.timer.addSeconds(10 * numMistakes);
									currBox.setCanModify(false);
								}
								currBox.setCanModify(false);
							}
							//right click = flag, is not checked with the solution to prevent cheating

						} else {
							currBox.setCanModify(true);
						}
					}
				} else if (controlMode == controlKeyboard) {
					currBox = gameGrid.getBox(kbX, kbY);
					if (pushingSolveKey && currBox.canModify()) {
						if (! modifier && ! currBox.green(solutionGrid)) {
							numMistakes++;
							Main.timer.addSeconds(10 * numMistakes);
						} else if (modifier) {
							currBox.impossibru();
						}
						currBox.setCanModify(false);
					}
				}
				if (frame.isClicking()) {
					userNameBox.setHasFocus(userNameBox.isInBounds(mouseX, mouseY));
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
			case "load":
				if (frame.scrollAmt != 0) {
					scrollIndex += (scrollIndex + frame.scrollAmt >= 0 && scrollIndex + frame.scrollAmt <= puzzleButtons.size() - 5 ? frame.scrollAmt : 0);

					frame.scrollAmt = 0;
				}
				break;
		}
	}

	private void generatePuzzle() {
		List<String> output;
		int numSolutions;
		int numTries = 0;
		do {
			numTries++;
			getSolution();
			gameGrid.generateClues(solutionGrid);
			writeClues();
			runSolver("clues.nin");
			do {
				output = LogStreamReader.output;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (output.size() < 5);
			int solutionsLine;
			do {
				solutionsLine = Strings.findLineWith(output, "Solutions : ", true);
			} while (Strings.findLineWith(output, "Solutions : ", true) == - 1);
			numSolutions = Integer.parseInt(output.get(solutionsLine).substring(output.get(solutionsLine).length() - 1, output.get(solutionsLine).length()));
			System.out.println(output.get(solutionsLine));
			//String difficulty = "";
			//int diffLine = Strings.findLineWith(output, "Decisions : ", true);
			//difficulty = output.get(diffLine).substring(12, output.get(diffLine).length());
			//Integer.parseInt(difficulty);
		} while (numSolutions > 1);
		System.out.println("Generated puzzle in " + numTries + " " + (numTries == 1 ? "try." : "tries."));
		//find maximum size of clues on left & top
		clueLen = new int[2];
		clueLen[0] = 0;
		clueLen[1] = 0;
		for (int i = 0; i < gameGrid.sizeY; i++) {
			if (gameGrid.cluesX[i].toString().length() > clueLen[0]) {
				clueLen[0] = gameGrid.cluesX[i].toString().length();
			}
		}
		clueLen[0] *= 7;
		clueLen[0] += 10;
		if (clueLen[0] < 100) {
			clueLen[0] = 100;
		}
		for (int i = 0; i < gameGrid.sizeX; i++) {
			if (gameGrid.cluesY[i].getValues().size() > clueLen[1]) {
				clueLen[1] = gameGrid.cluesY[i].getValues().size();
			}
		}
		clueLen[1] *= 12;
		clueLen[1] += 50;
		if (clueLen[1] < 130) {
			clueLen[1] = 130;
		}
		try {
			Files.deleteIfExists(FileSystems.getDefault().getPath("clues.nin"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadPuzzle(String name) {
		frame.setTitle("LOADING...");
		displayStatus("Loading custom puzzle...");
		windows.push(currWindow);
		currWindow = "game";
		allButtons.setWindow(currWindow);
		status = "get ready";
		bBegin.setVisible(true);
		bPause.setVisible(false);
		bGamba.setVisible(false);
		numMistakes = 0;
		bRegenPuzzle.setVisible(false);
		bMainMenu.setVisible(false);
		playable = false;
		List<String> output = new ArrayList<>();
		File puzzleFile = new File("." + slashCharacter + "saves" + slashCharacter + name + ".nin");
		try {
			s = new Scanner(puzzleFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (s.hasNext()) {
			String size = s.nextLine();
			sizeX = Integer.parseInt(size.substring(0, size.indexOf(' ')));
			sizeY = Integer.parseInt(size.substring(size.indexOf(' ') + 1, size.length()));
			gameGrid = new Grid(sizeX, sizeY);
		}
		runSolver("." + slashCharacter + "saves" + slashCharacter + name + ".nin");
		List<String> prevOutput;
		do {
			prevOutput = output;
			output = LogStreamReader.output;
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (! output.equals(prevOutput));
		List<String> puzzle = new ArrayList<>();
		int startIndex = 0;
		for (int i = 0; i < output.size(); i++) {
			puzzle.add(output.get(i));
			if (output.get(i).contains("hash misses")) {
				startIndex = i + 2;
			}
		}
		puzzle.remove(startIndex + sizeY);
		gameGrid = new Grid(sizeX, sizeY);
		solutionGrid = new Grid(sizeX, sizeY);
		for (int i = 0; i < sizeY; i++) {
			System.out.println(puzzle.get(i + startIndex));
			for (int j = 0; j < sizeX; j++) {
				char currCheck = puzzle.get(i + startIndex).charAt(j);
				solutionGrid.getBox(j, i).setState(currCheck == '#' ? 1 : 0);
			}
		}
		gameGrid.generateClues(solutionGrid);
		clueLen = new int[2];
		clueLen[0] = 0;
		clueLen[1] = 0;
		for (int i = 0; i < gameGrid.sizeY; i++) {
			if (gameGrid.cluesX[i].toString().length() > clueLen[0]) {
				clueLen[0] = gameGrid.cluesX[i].toString().length();
			}
		}
		clueLen[0] *= 7;
		clueLen[0] += 10;
		if (clueLen[0] < 100) {
			clueLen[0] = 100;
		}
		for (int i = 0; i < gameGrid.sizeX; i++) {
			if (gameGrid.cluesY[i].getValues().size() > clueLen[1]) {
				clueLen[1] = gameGrid.cluesY[i].getValues().size();
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
	 * @param y   number of pixels from top of canvas where the *bottom* of the
	 *            string should go
	 * @param art canvas to paint final string
	 */
	private void drawCenteredText(Font f, String s, int y, Graphics2D art) {
		int len = art.getFontMetrics(f).stringWidth(s);
		art.drawString(s, frame.getWidth() / 2 - len / 2, y);
	}

	/**
	 * Prints a string centered at (mouseX, mouseY).
	 *
	 * @param f   font, analyzed to center text exactly
	 * @param s   string to print
	 * @param x   mouseX-value of the center in pixels
	 * @param y   number of pixels from top of canvas where the *bottom* of the
	 *            string should go
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
	 * @param y   number of pixels from top of canvas where the *bottom* of the
	 *            string should go
	 * @param art canvas to paint final string
	 */
	private void drawRightText(Font f, String s, int y, Graphics2D art) {
		int len = art.getFontMetrics(f).stringWidth(s);
		art.drawString(s, frame.getWidth() - len - 10, y);
	}

	/**
	 * Prints a string right-aligned to the point (mouseX, mouseY).
	 *
	 * @param f   font, analyzed to find leftmost pixel of printed text
	 * @param s   string to print
	 * @param x   mouseX-value of the string end in pixels
	 * @param y   number of pixels from top of canvas where the *bottom* of the
	 *            string should go
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
	void doClickAction(Button b) throws Exception {
		if (b != bXUp && b != bXDown && b != bYUp && b != bYDown && b != bBegin && b != bPause && b != bGamba && ! (b instanceof ControlsButton)) {
			displayStatusNoBG("Loading...");
		}
		if (b == bNewPuzzle) {
			windows.push(currWindow);
			currWindow = "gamemode";
			allButtons.setWindow(currWindow);
			//get size from settings file

		} else if (b == bRandomPuzzle) {
			windows.push(currWindow);
			currWindow = "size picker";
			allButtons.setWindow(currWindow);
			String size = prefs.get("size");
			sizeX = Integer.parseInt(size.substring(0, size.indexOf(',')));
			sizeY = Integer.parseInt(size.substring(size.indexOf(',') + 1));
			if (sizeX == 0) {
				sizeX = 10;
			}
			if (sizeY == 0) {
				sizeY = 10;
			}
		} else if (b == bResume) {
			status = "";
			allButtons.setWindow("game");
			bResume.setVisible(false);
			bMainMenu2.setVisible(false);
			bPause.setVisible(true);
			bGamba.setVisible(false);
			Main.timer.resume();
			playable = true;
			faded = false;
		} else if (b == bPause) {
			if (status.equals("")) {
				allButtons.setWindow("pause");
				status = "paused";
				bPause.setVisible(false);
				bGamba.setVisible(false);
				bResume.setVisible(true);
				bMainMenu2.setVisible(true);
				Main.timer.pause();
				playable = false;
				faded = false;
			}
		} else if (b == bGamba) {
			Box randBox;
			do {
				int randX = (int) (Math.random() * sizeX);
				int randY = (int) (Math.random() * sizeY);
				randBox = gameGrid.getBox(randX, randY);
			} while (randBox.getState() != 0);
			if (randBox.green(solutionGrid)) {
				int winTime = - 10000;
				if (timer.getMS() + winTime > 0) {
					timer.addMS(winTime);
				} else {
					timer.addMS(timer.getMS() * (- 1));
				}
			} else {
				int loseTime = 10000;
				numMistakes++;
				timer.addMS(loseTime + numMistakes * 10000);
				randBox.setCanModify(false);
			}
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
			currWindow = windows.pop();
			allButtons.setWindow(currWindow);
		} else if (b == bStart || b == bRegenPuzzle) {
			frame.setTitle("GENERATING...");
			displayStatus("Generating random puzzle...");
			b.setVisible(false);
			windows.push(currWindow);
			currWindow = "game";
			allButtons.setWindow(currWindow);
			status = "get ready";
			bBegin.setVisible(true);
			bPause.setVisible(false);
			bGamba.setVisible(false);
			numMistakes = 0;
			bRegenPuzzle.setVisible(false);
			bMainMenu.setVisible(false);
			playable = false;
			kbX = 0;
			kbY = 0;
			generatePuzzle();
			Main.timer.reset();
			userNameBox.setVisible(false);
			scoreSubmitted = false;
		} else if (b == bMainMenu || b == bMainMenu2) {
			windows = new Stack<>();
			frame.setTitle("Main Menu | Picross");
			currWindow = "menu";
			windows.push(currWindow);
			allButtons.setWindow(currWindow);
			status = "menu";
			numMistakes = 0;
			playable = false;
			userNameBox.setVisible(false);
			scoreSubmitted = false;
		} else if (b == bQuitGame) {
			frame.setTitle("Quitting...");
			writePrefs();
			frame.setVisible(false);
			isRunning = false;
			frame.dispose();
			isDone = true;
			System.exit(0);
		} else if (b == bBegin) {
			b.setVisible(false);
			status = "";
			Main.timer.begin();
			playable = true;
			faded = false;
		} else if (b == bControlsMenu) {
			windows.push(currWindow);
			currWindow = "controls";
			allButtons.setWindow(currWindow);
		} else if (b == bCreator) {
			runCreator();
		} else if (b == bLoadPuzzle) {
			windows.push(currWindow);
			currWindow = "load";
			allButtons.setWindow(currWindow);
			loadMenuButtons.setVisible(true);
			//get all puzzles
			List<String> puzzleNames = getPuzzleNames();
			scrollIndex = 0;
			Button[] pButtons = new Button[getNumPuzzles()];
			for (int i = 0; i < getNumPuzzles(); i++) {
				pButtons[i] = new Button();
				pButtons[i].setText(puzzleNames.get(i).substring(0, puzzleNames.get(i).length() - 4));
			}
			puzzleButtons = new ButtonList("puzzles");
			puzzleButtons.addButtons(pButtons);
			puzzleButtons.sort();
			puzzleButtons.setVisible(true);
		} else if (b == bRestoreControls) {
			controlsMenuButtons.toList().stream().filter(b1 -> b1 instanceof ControlsButton).forEach(b1 -> {
				switch (((ControlsButton) b1).getLabel()) {
					case "pauseGame":
						keyPauseGame = KeyEvent.VK_ESCAPE;
						break;
					case "up":
						keyUp = KeyEvent.VK_UP;
						break;
					case "left":
						keyLeft = KeyEvent.VK_LEFT;
						break;
					case "down":
						keyDown = KeyEvent.VK_DOWN;
						break;
					case "right":
						keyRight = KeyEvent.VK_RIGHT;
						break;
					case "resolve1":
						keyResolve1 = KeyEvent.VK_SPACE;
						break;
					case "resolve2":
						keyResolve2 = KeyEvent.VK_ENTER;
						break;
				}
				updateButtons("controls");
			});
		} else if (b instanceof ControlsButton) {
			HashMap<String, Button> controlsButtons = new HashMap<>();
			controlsMenuButtons.toList().stream().filter(b1 -> b1 instanceof ControlsButton).forEach(b1 -> controlsButtons.put(((ControlsButton) b1).getLabel(), b1));
			if (keyAssigning != null) {
				//return previously assigning button's key to normal
				updateButtons("controls");
			}
			keyAssigning = ((ControlsButton) b).getLabel();
			b.setText("Press a key");
			System.out.println("Assigning a key to label " + keyAssigning);
		} else {
			for (int i = 0; i < puzzleButtons.size(); i++) {
				if (b == puzzleButtons.get(i)) {
					loadPuzzle(b.getText());
				}
			}
		}
	}

	private void updateButtons(String window) {
		switch (window) {
			case "controls":
				for (Button b : controlsMenuButtons.toList()) {
					if (b instanceof ControlsButton) {
						switch (((ControlsButton) b).getLabel()) {
							case "pauseGame":
								b.setText(KeyEvent.getKeyText(keyPauseGame));
								break;
							case "up":
								b.setText(KeyEvent.getKeyText(keyUp));
								break;
							case "left":
								b.setText(KeyEvent.getKeyText(keyLeft));
								break;
							case "down":
								b.setText(KeyEvent.getKeyText(keyDown));
								break;
							case "right":
								b.setText(KeyEvent.getKeyText(keyRight));
								break;
							case "resolve1":
								b.setText(KeyEvent.getKeyText(keyResolve1));
								break;
							case "resolve2":
								b.setText(KeyEvent.getKeyText(keyResolve2));
								break;
						}
					}
				}
				break;
		}
	}

	void doSlideAction(Slider s) {

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
	 * @param x1 mouseX-coordinate of left bound of rectangle
	 * @param y1 mouseY-coordinate of left bound of rectangle
	 * @param x2 mouseX-coordinate of right bound of rectangle
	 * @param y2 mouseY-coordinate of right bound of rectangle
	 * @return Returns whether the mouse's current positions falls within the
	 * defined bounds.
	 */
	//@Contract (pure = true)
	private boolean isInBounds(int x1, int y1, int x2, int y2) {
		return (mouseX > x1) && (mouseX < x2) && (mouseY > y1) && (mouseY < y2);
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
		allButtons = new AllButtons();

		mainMenuButtons = new ButtonList("menu");
		bNewPuzzle = new Button(frame.getWidth() / 2 - 100, 125, 200, 75, "Start Game", GREEN, 20);
		bNewPuzzle.setVisible(true);
		bQuitGame = new Button(frame.getWidth() / 2 - 100, 425, 200, 75, "Quit Game", RED, 20);
		bQuitGame.setVisible(true);
		bControlsMenu = new Button(frame.getWidth() / 2 - 100, 325, 200, 75, "Controls", BLUE, 20);
		bControlsMenu.setVisible(true);
		bCreator = new Button(frame.getWidth() / 2 - 100, 225, 200, 75, "Creator", YELLOW, 20);
		bCreator.setVisible(true);
		mainMenuButtons.addButtons(new Button[] {bNewPuzzle, bQuitGame, bControlsMenu, bCreator});

		gameChoiceButtons = new ButtonList("gamemode");
		bBack = new Button(10, 55, 50, 50, "<", RED, 30);
		bRandomPuzzle = new Button(frame.getWidth() / 2 - 100, 150, 200, 100, "Random Puzzle", GREEN, 20);
		bLoadPuzzle = new Button(frame.getWidth() / 2 - 100, 275, 200, 100, "Load Puzzle", YELLOW, 20);
		gameChoiceButtons.addButtons(new Button[] {bRandomPuzzle, bLoadPuzzle, bBack});

		loadMenuButtons = new ButtonList("load");
		loadMenuButtons.addButtons(new Button[] {bBack});

		puzzleButtons = new ButtonList("puzzles");

		sizePickerButtons = new ButtonList("size picker");
		bXUp = new Button(300, 400, 100, 50, "Λ", 30);
		bXDown = new Button(300, 510, 100, 50, "V", 30);
		bYUp = new Button(600, 400, 100, 50, "Λ", 30);
		bYDown = new Button(600, 510, 100, 50, "V", 30);
		bStart = new Button(frame.getWidth() / 2 - 50, frame.getHeight() - 100, 100, 75, "GENERATE", GREEN, 30);
		sizePickerButtons.addButtons(new Button[] {bXUp, bXDown, bYUp, bYDown, bBack, bStart});

		optionsMenuButtons = new ButtonList("options");
		optionsMenuButtons.addButtons(new Button[] {bBack});

		gameButtons = new ButtonList("game");
		bPause = new Button(20, 50, 60, 60, "Pause", YELLOW, 17);
		bGamba = new Button(20, 150, 60, 60, "GAMBA", ORANGE, 17);//TODO move this to a more suitable location (bottom bar?)
		gameButtons.addButtons(new Button[] {bPause, bGamba});

		pauseMenuButtons = new ButtonList("pause");
		bResume = new Button(frame.getWidth() / 2 - 100, frame.getHeight() / 2 + 7, 100, 43, "Resume", GREEN, 17);
		bMainMenu = new Button(frame.getWidth() / 2 - 100, frame.getHeight() / 2 + 7, 100, 43, "Main Menu", bgColor, 17);
		bMainMenu2 = new Button(frame.getWidth() / 2, frame.getHeight() / 2 + 7, 100, 43, "Main Menu", bgColor, 17);
		bRegenPuzzle = new Button(frame.getWidth() / 2, frame.getHeight() / 2 + 7, 100, 43, "New Puzzle", GREEN, 17);
		bBegin = new Button(frame.getWidth() / 2 - 100, frame.getHeight() / 2 - 50, 200, 100, "BEGIN", GREEN, 20);
		pauseMenuButtons.addButtons(new Button[] {bResume/*, bMainMenu*/, bMainMenu2/*, bRegenPuzzle*//*, bBegin*/});

		gameEndButtons = new ButtonList("game end");
		gameEndButtons.addButtons(new Button[] {bMainMenu, bRegenPuzzle});

		controlsMenuButtons = new ButtonList("controls");
		bRestoreControls = new Button(frame.getWidth() - 150 - 10, 55, 150, 50, "Restore Defaults", YELLOW, 20);
		controlsMenuButtons.addButtons(new Button[] {bBack, bRestoreControls});

		allButtons.addButtonLists(new ButtonList[] {mainMenuButtons, gameChoiceButtons, loadMenuButtons, sizePickerButtons, optionsMenuButtons, gameButtons, gameEndButtons, pauseMenuButtons, controlsMenuButtons});
		allButtons.setWindow("menu");
	}

	private void initControls() {
		int buttonWidth = 100;
		int buttonHeight = 50;
		controlsButtons = new ArrayList<>();
		controlsDescriptions = new ArrayList<>();
		//catch-all for if prefs is not properly initialized
		if (! (prefs.has("pauseGame") && prefs.has("up") && prefs.has("left") && prefs.has("down") && prefs.has("right") && prefs.has("resolve1") && prefs.has("resolve2"))) {
			try {
				doClickAction(bRestoreControls);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//ESC pauses the game
		controlsButtons.add(new ControlsButton(0, 0, buttonWidth, buttonHeight, KeyEvent.getKeyText(Integer.parseInt(prefs.get("pauseGame"))), "pauseGame", 20));
		controlsDescriptions.add("Pause game");
		keyPauseGame = Integer.parseInt(prefs.get("pauseGame"));
		//Up Arrow Key moves the cursor up 1 block
		controlsButtons.add(new ControlsButton(0, 0, buttonWidth, buttonHeight, KeyEvent.getKeyText(Integer.parseInt(prefs.get("up"))), "up", 20));
		controlsDescriptions.add("Move in-game cursor up");
		keyUp = Integer.parseInt(prefs.get("up"));
		//Left Arrow Key moves the cursor left 1 block
		controlsButtons.add(new ControlsButton(0, 0, buttonWidth, buttonHeight, KeyEvent.getKeyText(Integer.parseInt(prefs.get("left"))), "left", 20));
		controlsDescriptions.add("Move in-game cursor left");
		keyLeft = Integer.parseInt(prefs.get("left"));
		//Down Arrow Key moves the cursor down 1 block
		controlsButtons.add(new ControlsButton(0, 0, buttonWidth, buttonHeight, KeyEvent.getKeyText(Integer.parseInt(prefs.get("down"))), "down", 20));
		controlsDescriptions.add("Move in-game cursor down");
		keyDown = Integer.parseInt(prefs.get("down"));
		//Right Arrow Key moves the cursor right 1 block
		controlsButtons.add(new ControlsButton(0, 0, buttonWidth, buttonHeight, KeyEvent.getKeyText(Integer.parseInt(prefs.get("right"))), "right", 20));
		controlsDescriptions.add("Move in-game cursor right");
		keyRight = Integer.parseInt(prefs.get("right"));
		//Enter key marks a block
		controlsButtons.add(new ControlsButton(0, 0, buttonWidth, buttonHeight, KeyEvent.getKeyText(Integer.parseInt(prefs.get("resolve1"))), "resolve1", 20));
		controlsDescriptions.add("Resolves the current tile");
		keyResolve1 = Integer.parseInt(prefs.get("resolve1"));
		//Space also marks a block
		controlsButtons.add(new ControlsButton(0, 0, buttonWidth, buttonHeight, KeyEvent.getKeyText(Integer.parseInt(prefs.get("resolve2"))), "resolve2", 20));
		controlsDescriptions.add("Secondary key to resolve the current tile");
		keyResolve2 = Integer.parseInt(prefs.get("resolve2"));

		controlsMenuButtons.addButtons(controlsButtons);
	}

	private void writePrefs() {
		prefs.put("size", "" + sizeX + ',' + sizeY);
		prefs.put("pauseGame", Integer.toString(keyPauseGame));
		prefs.put("up", Integer.toString(keyUp));
		prefs.put("left", Integer.toString(keyLeft));
		prefs.put("down", Integer.toString(keyDown));
		prefs.put("right", Integer.toString(keyRight));
		prefs.put("resolve1", Integer.toString(keyResolve1));
		prefs.put("resolve2", Integer.toString(keyResolve2));
		try {
			prefs.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void displayStatus(String message) {
		Graphics2D art = (Graphics2D) imgBuffer.getGraphics();
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		art.setRenderingHints(rh);
		f = art.getFont();
		art.setColor(bgColor);
		art.fillRect(0, 0, frame.getWidth(), frame.getHeight());
		art.setColor(black);
		art = setFont(50f, art);
		drawCenteredText(f, message, frame.getHeight() / 2 + 25, art);
		art = (Graphics2D) frame.getGraphics();
		if (art != null) {
			art.drawImage(imgBuffer, 0, 0, frame.getWidth(), frame.getHeight(), 0, 0, frame.getWidth(), frame.getHeight(), null);
			art.dispose();
		}
	}

	private void displayStatusNoBG(String message) {
		Graphics2D art = (Graphics2D) imgBuffer.getGraphics();
		RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		art.setRenderingHints(rh);
		f = art.getFont();
		art.setColor(black);
		art = setFont(50f, art);
		drawCenteredText(f, message, frame.getHeight() / 2 + 25, art);
		art = (Graphics2D) frame.getGraphics();
		if (art != null) {
			art.drawImage(imgBuffer, 0, 0, frame.getWidth(), frame.getHeight(), 0, 0, frame.getWidth(), frame.getHeight(), null);
			art.dispose();
		}
	}
}
