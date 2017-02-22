package picross;

@SuppressWarnings("WeakerAccess")
public class Box {
	private boolean canModify;
	private final int x;
	private final int y;
	/**
	 * 0 = empty
	 * 1 = solved
	 * 2 = flagged
	 * 3 = mistake
	 * all boxes start empty
	 * game is not complete until # of solved tiles = number in solution
	 * //x is left->right, y is down->up
	 */
	private int state;


	public Box(int x_, int y_) {
		x = x_;
		y = y_;
		state = 0;
		setCanModify(true);
	}

	public void impossibru() {//toggles flagged state
		if (state == 0)
			setState(2);
		else if (state == 2)
			setState(0);
	}

	public boolean green(Grid solution) {//checks solution
		if (solution.getBox(x, y).getState() == 1 && state == 0) {
			setState(1);
			setCanModify(false);
			return true;
		} else if (state == 2) {
			setState(0);
			setCanModify(false);
			return true;
		} else if (state == 3) {
			return true;
		} else if (state == 0) {
			setState(3);
			return ! canModify();//ONLY returns false if the box can be modified, i.e is deliberately clicked on
		}
		setCanModify(false);
		return true;

	}

	public int[] getPos() {
		return new int[] {x, y};
	}

	public int getState() {
		return state;
	}

	public void setState(int s) {
		if (canModify())
			state = s;
	}

	public boolean canModify() {
		return canModify;
	}

	public void setCanModify(boolean canModify) {
		this.canModify = canModify;
	}
}
