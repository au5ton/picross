package picross;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class Button {
	public int x1, y1, sizeX, sizeY;
	private boolean mouseHovering, clicking, isVisible;
	private Color baseColor, coverColor;
	private String text;
	private float textSize;
	private Font font;
	private FontMetrics fontInfo;
	public Button(int x, int y, int size_x, int size_y, String text) {
		x1 = x;
		y1 = y;
		sizeX = size_x;
		sizeY = size_y;
		this.text = text;
		mouseHovering = false;
		clicking = false;
		baseColor = Color.WHITE;
		coverColor = new Color(0, 0, 0, 0);
		isVisible = false;
	}
	public Button(int x, int y, int size_x, int size_y, String text, Color bColor) {
		x1 = x;
		y1 = y;
		sizeX = size_x;
		sizeY = size_y;
		this.text = text;
		mouseHovering = false;
		clicking = false;
		baseColor = bColor;
		coverColor = new Color(0, 0, 0, 0);
		isVisible = false;
	}
	public void hover() {
		if(isVisible) {
			mouseHovering = true;
			coverColor = new Color(0, 0, 0, 32);
		}
	}
	public void click() {
		if(isVisible) {
			clicking = true;
			coverColor = new Color(0, 0, 0, 128);
		}
	}
	public void unClick() {
		clicking = false;
		if(isVisible) {
			coverColor = new Color(0, 0, 0, 32);
		}
	}
	public void unHover() {
		if(isVisible) {
			mouseHovering = false;
			coverColor = new Color(0, 0, 0, 0);
		}
	}
	public void setVisible(boolean b) {
		isVisible = b;
	}
	public void draw(Graphics2D art) {
		font = art.getFont().deriveFont(sizeY);
		fontInfo = art.getFontMetrics(font);
		font = art.getFont().deriveFont(getTextSize(art));
		art.setFont(font);
		art.setColor(baseColor);
		art.fillRect(x1, y1, sizeX, sizeY);
		art.setColor(coverColor);
		art.fillRect(x1, y1, sizeX, sizeY);
		art.setColor(Color.BLACK);
		art.drawRect(x1, y1, sizeX, sizeY);
		art.drawString(text, x1, y1 + (sizeY / 2));
		art.setFont(art.getFont().deriveFont(12f));
	}
	private float getTextSize(Graphics2D art) {
		float width = fontInfo.stringWidth(text) * text.length(), height = sizeY;
		while(width > sizeX) {
			height -= 0.1;
			font = art.getFont().deriveFont(height);
			fontInfo = art.getFontMetrics(font);
			width = fontInfo.stringWidth(text);
		}
		return height;
	}
	public boolean isInBounds(int x, int y) {
		if(x > x1 && y > y1 && x < (x1 + sizeX) && y < (y1 + sizeY))
			return true;
		else
			return false;
	}
	public boolean isClicking() {
		return clicking;
	}
}
