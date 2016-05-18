package picross;


import org.jaxen.util.SingletonList;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 5/18/2016 at 10:45 AM.
 */
public class AllButtons {
	private List<ButtonList> buttonLists;
	private String currWindow;
	public AllButtons() {
		buttonLists = new ArrayList<>();
	}
	public AllButtons(ButtonList[] bLists) {
		buttonLists = new ArrayList<>();
		for(ButtonList buttonList : bLists) {
			buttonLists.add(buttonList);
		}
	}
	public void setWindow(String windowName) {
		currWindow = windowName;
		for(ButtonList buttonList : buttonLists) {
			buttonList.setVisible(buttonList.getName().equals(windowName));
		}
	}
	public void drawButtons(int x, int y, Graphics2D art) {
		for(ButtonList buttonList : buttonLists) {
			if(buttonList.getName().equals(currWindow)) {
				buttonList.drawAll(x, y, art);
			}
		}
	}
	public void addButtonLists(ButtonList[] bLists) {
		for(ButtonList buttonList : bLists) {
			buttonLists.add(buttonList);
		}
	}
}
