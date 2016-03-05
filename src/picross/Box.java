package picross;

public class Box {
	public boolean canModify;
	private int x, y, state;//x is left->right, y is down->up
	/*state:
	 * 0 = empty
	 * 1 = solved
	 * 2 = flagged
	 * 3 = mistake
	 * all boxes start empty
	 * game is not complete until # of solved tiles = number in solution
	 */
	public Box(int x_, int y_) {
		x = x_;
		y = y_;
		state = 0;
		canModify = true;
	}
	public void impossibru() {//toggles flagged state
		if(state == 0)
			setState(2);
		else if(state == 2)
			setState(0);
	}
	public boolean green(Grid solution) {//checks solution
		if(solution.getBox(x, y).getState() == 1 && state != 2) {
			setState(1);
			canModify = false;
			return true;
		} else if(state == 2) {
			setState(0);
			canModify = false;
			return true;
		} 
		else if(state == 3) {
			return true;
		}else {
			setState(3);
			canModify = false;
			return false;
		}
		
		//else
		//state = 3;
	}
	public int[] getPos() {
		int[] pos = {x, y};
		return pos;
	}
	public int getState() {
		return state;
	}
	public void setState(int s) {
		if(canModify)
			state = s;
	}
}
