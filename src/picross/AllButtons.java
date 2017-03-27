package picross;


import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created on 5/18/2016 at 10:45 AM.
 */
@SuppressWarnings("WeakerAccess")
public class AllButtons {
	private List<ButtonList> buttonLists;
	private PicrossWindow currWindow;

	public AllButtons() {
		buttonLists = new ArrayList<>();
	}

	public AllButtons(ButtonList[] bLists) {
		buttonLists = new ArrayList<>();
		Collections.addAll(buttonLists, bLists);
	}

	public void setWindow(PicrossWindow windowName) {
		currWindow = windowName;
		for (ButtonList buttonList : buttonLists) {
			buttonList.setVisible(buttonList.getName().equals(windowName));
		}
	}

	public void drawButtons(Graphics2D art) {
		for (ButtonList buttonList : buttonLists) {
			if (buttonList.getName().equals(currWindow)) {
				buttonList.drawAll(art);
			}
		}
	}

	public void addButtonLists(ButtonList[] bLists) {
		Collections.addAll(buttonLists, bLists);
	}
}
