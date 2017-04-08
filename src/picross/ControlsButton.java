package picross;

import common.Graphics;
import java.awt.*;

/**
 * Created by kyle on 10/24/16 at 11:32 AM.
 */
@SuppressWarnings("WeakerAccess")
public class ControlsButton extends Button {
	private String label;

	public ControlsButton(Graphics g) {
		super(g);
	}

	public ControlsButton(int x, int y, int sizeX, int sizeY, String text, String label, Graphics g) {
		super(x, y, sizeX, sizeY, text, g);
		this.label = label;
	}

	public ControlsButton(int x, int y, int sizeX, int sizeY, String text, String label, int maxFontSize, Graphics g) {
		super(x, y, sizeX, sizeY, text, maxFontSize, g);
		this.label = label;
	}

	public ControlsButton(int x, int y, int sizeX, int sizeY, String text, String label, Color bgColor, Graphics g) {
		super(x, y, sizeX, sizeY, text, bgColor, g);
		this.label = label;
	}

	public void setLabel(String s) {
		label = s;
	}

	public String getLabel() {
		return label;
	}

}
