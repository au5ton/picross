package puzzlecreator;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created on 4/7/2016 at 11:17 PM.
 */
class ButtonList {
	@SuppressWarnings("CanBeFinal")
	private List<Button> buttons;

	public ButtonList() {
		buttons = new ArrayList<>();
	}

	public ButtonList(Button b) {
		buttons = new ArrayList<>(Collections.singletonList(b));
	}

	public ButtonList(List<Button> b) {
		buttons = b;
	}

	public List<Button> getButtons() {
		return buttons;
	}

	public void addButton(Button b) {
		buttons.add(b);
	}

	public void drawAll(int x, int y, Graphics2D art) {
		for (Button b : buttons) {
			b.draw(x, y, art);
		}
	}

	public void addButtons(Button[] bs) {
		Collections.addAll(buttons, bs);
	}

	public void addButtons(List<Button> bs) {
		buttons.addAll(bs);
	}

	public void setVisible(boolean visible) {
		for (Button button : buttons) {
			button.setVisible(visible);
		}
	}
}
