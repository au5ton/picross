package picross;

import common.DrawingTools;

import java.awt.*;
import java.util.List;

import static java.awt.Color.*;
import static picross.GameWindow.bSize;
import static picross.GameWindow.clueLen;

@SuppressWarnings("WeakerAccess")
class Grid {
	public final int sizeX;
	public final int sizeY;
	private final Box[][] boxes;
	public final Clue[] cluesX;
	public final Clue[] cluesY;

	public Grid(int size_x, int size_y) {
		boxes = new Box[size_x][size_y];
		cluesX = new Clue[size_y];
		cluesY = new Clue[size_x];
		sizeX = size_x;
		sizeY = size_y;
		for (int i = 0; i < sizeY; i++) {
			cluesX[i] = new Clue(i, 0);
			for (int j = 0; j < sizeX; j++) {
				boxes[j][i] = new Box(j, i);
				cluesY[j] = new Clue(j, 1);
			}
		}
	}

	public void drawGrid(int x, int y, Graphics2D art, int cWidth) {
		switch (boxes[x][y].getState()) {
			case 0:
				art.setColor(WHITE);
				art.fillRect(clueLen[0] + x * bSize + cWidth,//draw white background
						clueLen[1] + y * bSize,
						bSize,
						bSize);
				break;
			case 1:
				art.setColor(GREEN);
				art.fillRect(clueLen[0] + x * bSize + cWidth, //draw green background
						clueLen[1] + y * bSize,
						bSize,
						bSize);
				break;
			case 2:
				art.setColor(WHITE);
				art.fillRect(clueLen[0] + x * bSize + cWidth, //draw white background
						clueLen[1] + y * bSize,
						bSize,
						bSize);
				art.setColor(BLACK);
				art.drawLine(clueLen[0] + x * bSize + bSize / 10 + cWidth, //draw x
						clueLen[1] + y * bSize + bSize / 10,
						clueLen[0] + x * bSize + bSize * 9 / 10 + cWidth,
						clueLen[1] + y * bSize + bSize * 9 / 10);
				art.drawLine(clueLen[0] + x * bSize + bSize / 10 + cWidth,
						clueLen[1] + y * bSize + bSize * 9 / 10,
						clueLen[0] + x * bSize + bSize * 9 / 10 + cWidth,
						clueLen[1] + y * bSize + bSize / 10);
				break;
			case 3:
				art.setColor(RED);
				art.fillRect(clueLen[0] + x * bSize + cWidth, //draw red background
						clueLen[1] + y * bSize,
						bSize,
						bSize);
				art.setColor(BLACK);
				art.drawLine(clueLen[0] + x * bSize + bSize / 10 + cWidth, //draw x
						clueLen[1] + y * bSize + bSize / 10,
						clueLen[0] + x * bSize + bSize * 9 / 10 + cWidth,
						clueLen[1] + y * bSize + bSize * 9 / 10);
				art.drawLine(clueLen[0] + x * bSize + bSize / 10 + cWidth,
						clueLen[1] + y * bSize + bSize * 9 / 10,
						clueLen[0] + x * bSize + bSize * 9 / 10 + cWidth,
						clueLen[1] + y * bSize + bSize / 10);
		}
	}

	public void drawClues(int x, int type, Graphics2D art, int cWidth) {
		art.setFont(art.getFont().deriveFont(12f));
		if (type == 0) {
			String s = cluesX[x].toString();
			DrawingTools.drawRightText(art.getFont(), s, clueLen[0] + cWidth, clueLen[1] + x * bSize + bSize / 2 + 6, art);
			//graphics2D.drawString(s, 20, GameWindow.clueLen[1] + x * GameWindow.bSize + GameWindow.bSize / 2);
		} else {
			List<Integer> values = cluesY[x].getValues();
			int begin = (clueLen[1] - values.size() * 12);
			for (int i = 0; i < values.size(); i++) {
				String s = Integer.toString(values.get(i));
				DrawingTools.drawCenteredText(art.getFont(), s, clueLen[0] + x * bSize + bSize / 2 + cWidth, begin + (12 * i), art);
				//graphics2D.drawString(s, GameWindow.clueLen[0] + x * GameWindow.bSize + GameWindow.bSize/ 2, 50 + (12 * i));
			}
		}
	}

	public Box getBox(int x, int y) {
		if (x >= 0 && x < sizeX && y >= 0 && y < sizeY)
			return boxes[x][y];
		else
			return null;
	}

	public void generateClues(Grid g) {
		for (int i = 0; i < sizeY; i++) {
			cluesX[i].generateClue(g);
		}
		for (int i = 0; i < sizeX; i++) {
			cluesY[i].generateClue(g);
		}
	}
}
