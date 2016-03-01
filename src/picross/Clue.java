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
		int temp = 0, x = 0, length = (type == 0 ? grid.sizeX : grid.sizeY);
		while(x < length) {
			Box currBox = grid.getBox(type == 0 ? x : y, type == 0 ? y : x);
			if(currBox.getState() == 1)
				temp++;
			else if(temp > 0) {
				values.add(temp);
				temp = 0;
			}
			x++;
		}
		if(temp > 0)
			values.add(temp);
	}
	public String toString() {
		char c = (type == 0 ? ' ' : '\n');
		String out = "";
		for(int i = 0; i < values.size(); i++) {
			out += Integer.toString(values.get(i));
			if(i + 1 < values.size())
				out += c;
		}
		if(out == "")
			out = "0";
		return out;
	}
}
