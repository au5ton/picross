package picross;

import java.awt.Color;
import java.awt.Graphics2D;

public class Grid {
	public int sizeX, sizeY;
	private Box[][] boxes;
	private Clue[] cluesX, cluesY;
	public Grid(int size_x, int size_y) {
		boxes = new Box[size_x][size_y];
		cluesX = new Clue[size_x];
		cluesY = new Clue[size_y];
		sizeX = size_x;
		sizeY = size_y;
		for(int i = 0; i < sizeX; i++) {
			cluesX[i] = new Clue(i, 0);
			for(int j = 0; j < sizeY; j++) {
				boxes[i][j] = new Box(i, j);
				cluesY[j] = new Clue(j, 1);
			}
		}
	}
	public void drawGrid(int x, int y, Graphics2D art) {
		switch(boxes[x - 1][y - 1].getState()) {
		case 0:
			art.setColor(Color.WHITE);
			art.fillRect(x * 100, y * 100, 100, 100);
			break;
		case 1:
			art.setColor(Color.GREEN);
			art.fillRect(x * 100, y * 100, 100, 100);
			break;
		case 2:
			art.setColor(Color.WHITE);
			art.fillRect(x * 100, y * 100, 100, 100);
			art.setColor(Color.BLACK);
			art.drawLine(x * 100 + 10, y * 100 + 10, x * 100 + 90, y * 100 + 90);
			art.drawLine(x * 100 + 10, y * 100 + 90, x * 100 + 90, y * 100 + 10);
			break;
		case 3:
			art.setColor(Color.RED);
			art.fillRect(x * 100, y * 100, 100, 100);
			art.setColor(Color.BLACK);
			art.drawLine(x * 100 + 10, y * 100 + 10, x * 100 + 90, y * 100 + 90);
			art.drawLine(x * 100 + 10, y * 100 + 90, x * 100 + 90, y * 100 + 10);
		}
	}
	public void drawClues(int x, int type, Graphics2D art) {
		String s = (type == 0 ? cluesX[x - 1].toString() : cluesY[x - 1].toString());
		if(type == 0) {
			art.drawString(s, 20, x * 100 + 50);
		}
		else {
			art.drawString(s, x * 100 + 50, 50);
		}
	}
	public Box getBox(int x, int y) {
		return boxes[x][y];
	}
	public void generateClues(Grid g) {
		for(int i = 0; i < sizeX; i++) {
			cluesX[i].generateClue(g, i);
		}
		for(int i = 0; i < sizeY; i++) {
			cluesY[i].generateClue(g, i);
		}
	}
}
