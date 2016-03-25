package picross;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class Button {
	public int x1, y1, sizeX, sizeY;
	private int maxFontSize;
	private boolean clicking, isVisible, canClick;
	private Color baseColor, coverColor, borderColor = Color.BLACK;
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
		clicking = false;
		baseColor = Color.WHITE;
		coverColor = new Color(0, 0, 0, 0);
		isVisible = false;
		canClick = true;
	}
	public Button(int x, int y, int size_x, int size_y, String text, int maxFontSize) {
		x1 = x;
		y1 = y;
		sizeX = size_x;
		sizeY = size_y;
		this.text = text;
		clicking = false;
		baseColor = Color.WHITE;
		coverColor = new Color(0, 0, 0, 0);
		isVisible = false;
		canClick = true;
		this.maxFontSize = maxFontSize;
	}
	public Button(int x, int y, int size_x, int size_y, String text, Color bColor) {
		x1 = x;
		y1 = y;
		sizeX = size_x;
		sizeY = size_y;
		this.text = text;
		clicking = false;
		baseColor = bColor;
		coverColor = new Color(0, 0, 0, 0);
		isVisible = false;
		canClick = true;
	}
	public Button(int x, int y, int size_x, int size_y, String text, Color bColor, int maxFontSize) {
		x1 = x;
		y1 = y;
		sizeX = size_x;
		sizeY = size_y;
		this.text = text;
		clicking = false;
		baseColor = bColor;
		coverColor = new Color(0, 0, 0, 0);
		isVisible = false;
		canClick = true;
		this.maxFontSize = maxFontSize;
	}
	public void hover() {
		if(isVisible) {
			coverColor = new Color(0, 0, 0, 32);
		}
	}
	public void click() {
		if(isVisible && canClick) {
			clicking = true;
			coverColor = new Color(0, 0, 0, 128);
		}
	}
	public void unClick() {
		clicking = false;
		canClick = false;
		if(isVisible) {
			coverColor = new Color(0, 0, 0, 32);
		}
	}
	public void unHover() {
		if(isVisible) {
			coverColor = new Color(0, 0, 0, 0);
		}
	}
	public void setVisible(boolean b) {
		isVisible = b;
	}
	public void draw(int x, int y, Graphics2D art) {
		if(clicking && !Main.mainWindow.frame.clicking) {
			if(isInBounds(x, y)) {
				Main.mainWindow.doClickAction(this);
				unClick();
			}
			else {
				unClick();
			}
		}
		if(isInBounds(x, y))
			hover();
		else
			unHover();
		if(Main.mainWindow.frame.clicking) {
			if(Main.mainWindow.frame.mouseButton == 1) {
				if(isInBounds(x, y)) {
					click();
				}
			}
		}
		else {
			canClick = true;
		}
		font = art.getFont().deriveFont(sizeY);
		fontInfo = art.getFontMetrics(font);
		textSize = getTextSize(art);
		font = art.getFont().deriveFont(textSize);
		fontInfo = art.getFontMetrics(font);
		if(isVisible) {
			art.setFont(font);
			art.setColor(baseColor);
			art.fillRect(x1, y1, sizeX, sizeY);
			art.setColor(coverColor);
			art.fillRect(x1, y1, sizeX, sizeY);
			art.setColor(borderColor);
			art.drawRect(x1, y1, sizeX, sizeY);
			art.drawString(text, x1 + (sizeX / 2 - fontInfo.stringWidth(text)/ 2), y1 + (sizeY / 2 + textSize / 3));
			art.setFont(art.getFont().deriveFont(12f));
		}
	}
	private float getTextSize(Graphics2D art) {
		float width = fontInfo.stringWidth(text) * text.length(), height = sizeY;
		while(width > sizeX) {
			height -= 0.1;
			font = art.getFont().deriveFont(height);
			fontInfo = art.getFontMetrics(font);
			width = fontInfo.stringWidth(text);
		}
		if(height > maxFontSize)
			height = maxFontSize;
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
	public boolean isVisible() {
		return isVisible;
	}
	public void setText(String s) {
		text = s;
	}
	public String getText() {
		return text;
	}
	public void setBorderColor(Color c) {
		borderColor = c;
	}
}
