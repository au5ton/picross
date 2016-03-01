package picross;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Clue {
	private int type, pos;
	private List<Integer> values = new ArrayList<Integer>();
	public Clue(int position, int type) {
		pos = position;
		this.type = type;//0 is horizontal, 1 is vertical
	}
	public int getType() {
		return type;
	}
	public int getPos() {
		return pos;
	}
	public List<Integer> getValues() {
		if(values.size() > 0) {
			return values;
		}
		else {
			return new ArrayList<Integer>(Arrays.asList(0));
		}
	}
	public void generateClue(Grid grid, int y) {
		int temp = 0, x = 0;
		do {

			x++;
			temp = 0;
			while(x + 1 < (type == 0 ? grid.sizeX : grid.sizeY) && grid.getBox(type == 0 ? x : y, type == 0 ? y : x).getState() == 1) {
				x++;
				temp++;
			}
			values.add(temp);
		} while(x + 1 < (type == 0 ? grid.sizeX : grid.sizeY));
	}
	public String toString() {
		char c = (type == 0 ? ' ' : '\n');
		String out = "";
		for(int i = 0; i < values.size(); i++) {
			out += Integer.toString(values.get(i));
			if(i + 1 < values.size())
				out += Character.toString(c);
		}
		return out;
	}
}
