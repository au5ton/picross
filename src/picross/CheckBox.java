package picross;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import static java.awt.Color.*;

/**
 * Created on 4/12/2016 at 9:02 PM.
 */
@SuppressWarnings({"SameParameterValue", "CanBeFinal"})
class CheckBox {
	private final Color TRANSPARENT = new Color(0, 0, 0, 0);
	private final Color HOVERING = new Color(0, 0, 0, 32);
	private final Color CLICKING = new Color(0, 0, 0, 64);
	private Color coverColor = TRANSPARENT;
	private boolean checked, hovering, clicking;
	private int posX, posY, size;

	public CheckBox(int x, int y, int size) {
		checked = false;
		posX = x;
		posY = y;
		this.size = size;
	}

	public CheckBox(int x, int y, int size, boolean isChecked) {
		posX = x;
		posY = y;
		this.size = size;
		checked = isChecked;
	}

	public void draw(int mouseX, int mouseY, Graphics2D art) {
		FancyFrame frame = Main.mainWindow.getFrame();
		art.setColor(white);
		art.fillRect(posX, posY, size, size);
		art.setColor(black);
		art.drawRect(posX, posY, size, size);
		art.setColor(coverColor);
		art.fillRect(posX, posY, size, size);
		if (mouseX > posX && mouseX < posX + size && mouseY > posY && mouseY < posY + size) {
			hover();
		} else
			unHover();
		if (frame.isClicking() && hovering) {
			click();
		} else if (clicking) {
			unClick();
		}
		if (checked) {
			BufferedImage checkMark = null;
			try {
				URL url = this.getClass().getClassLoader().getResource("resources/check.png");
				if (url != null)
					checkMark = ImageIO.read(url);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (checkMark != null)
				art.drawImage(checkMark, posX, posY, size, size, (img, infoflags, x, y, width, height) -> false);
			else {
				int border = size / 6;
				art.setColor(BLACK);
				art.fillRect(posX + border, posY + border, size - 2 * border + 1, size - 2 * border + 1);
			}
		}
	}

	@SuppressWarnings("WeakerAccess")
	public void hover() {
		hovering = true;
		coverColor = HOVERING;
	}

	@SuppressWarnings("WeakerAccess")
	public void unHover() {
		hovering = false;
		clicking = false;
		coverColor = TRANSPARENT;
	}

	@SuppressWarnings("WeakerAccess")
	public void click() {
		clicking = true;
		coverColor = CLICKING;
	}

	@SuppressWarnings("WeakerAccess")
	public void unClick() {
		clicking = false;
		checked = ! checked;
		coverColor = hovering ? HOVERING : TRANSPARENT;
	}

	public boolean isChecked() {
		return checked;
	}
}
