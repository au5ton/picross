package picross;

import java.awt.*;

/**
 * Created by kyle on 10/24/16 at 11:32 AM.
 */
@SuppressWarnings("WeakerAccess")
public class ControlsButton extends Button {
	private String label;

	public ControlsButton() {
		super();
	}

	public ControlsButton(int x, int y, int sizeX, int sizeY, String text, String label) {
		super(x, y, sizeX, sizeY, text);
		this.label = label;
	}

	public ControlsButton(int x, int y, int sizeX, int sizeY, String text, String label, int maxFontSize) {
		super(x, y, sizeX, sizeY, text, maxFontSize);
		this.label = label;
	}

	public ControlsButton(int x, int y, int sizeX, int sizeY, String text, String label, Color bgColor) {
		super(x, y, sizeX, sizeY, text, bgColor);
		this.label = label;
	}

	public void setLabel(String s) {
		label = s;
	}

	public String getLabel() {
		return label;
	}

}
