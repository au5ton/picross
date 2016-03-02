package picross;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

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
		switch(boxes[x][y].getState()) {
		case 0:
			art.setColor(Color.WHITE);
			art.fillRect(100 + x * Graphics.bSize, 100 + y * Graphics.bSize, Graphics.bSize, Graphics.bSize);
			break;
		case 1:
			art.setColor(Color.GREEN);
			art.fillRect(100 + x * Graphics.bSize, 100 + y * Graphics.bSize, Graphics.bSize, Graphics.bSize);
			break;
		case 2:
			art.setColor(Color.WHITE);
			art.fillRect(100 + x * Graphics.bSize, 100 + y * Graphics.bSize, Graphics.bSize, Graphics.bSize);
			art.setColor(Color.BLACK);
			art.drawLine(100 + x * Graphics.bSize + Graphics.bSize / 10, 100 + y * Graphics.bSize + Graphics.bSize / 10, 100 + x * Graphics.bSize + Graphics.bSize * 9 / 10, 100 + y * Graphics.bSize + Graphics.bSize * 9 / 10);
			art.drawLine(100 + x * Graphics.bSize + Graphics.bSize / 10, 100 + y * Graphics.bSize + Graphics.bSize * 9 / 10, 100 + x * Graphics.bSize + Graphics.bSize * 9 / 10, 100 + y * Graphics.bSize + Graphics.bSize / 10);
			break;
		case 3:
			art.setColor(Color.RED);
			art.fillRect(100 + x * Graphics.bSize, 100 + y * Graphics.bSize, Graphics.bSize, Graphics.bSize);
			art.setColor(Color.BLACK);
			art.drawLine(100 + x * Graphics.bSize + Graphics.bSize / 10, 100 + y * Graphics.bSize + Graphics.bSize / 10, 100 + x * Graphics.bSize + Graphics.bSize * 9 / 10, 100 + y * Graphics.bSize + Graphics.bSize * 9 / 10);
			art.drawLine(100 + x * Graphics.bSize + Graphics.bSize / 10, 100 + y * Graphics.bSize + Graphics.bSize * 9 / 10, 100 + x * Graphics.bSize + Graphics.bSize * 9 / 10, 100 + y * Graphics.bSize + Graphics.bSize / 10);
		}
	}
	public void drawClues(int x, int type, Graphics2D art) {
		if(type == 0) {
			String s = cluesX[x].toString();
			art.drawString(s, 20, 100 + x * Graphics.bSize + Graphics.bSize / 2);
		}
		else {
			List<Integer> values = cluesY[x].getValues();
			for(int i = 0; i < values.size(); i++) {
				String s = Integer.toString(values.get(i));
				art.drawString(s, 100 + x * Graphics.bSize * 3 / 2, 20 + (12 * i));
			}//TODO set variable for y-oriented border to make clue rendering more effective, must replace lots of 100s isn't it fun lolololololol :'(
		}
	}
	public Box getBox(int x, int y) {
		if(x >= 0 && x < sizeX && y >= 0 && y < sizeY)
			return boxes[x][y];
		else
			return null;
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
