package puzzleCreator;

@SuppressWarnings("WeakerAccess")
public class Box {
	private boolean canModify;
	private final int x;
	private final int y;
	private int /**
	 * 0 = empty
	 * 1 = solved
	 * 2 = flagged
	 * 3 = mistake
	 * all boxes start empty
	 * game is not complete until # of solved tiles = number in solution
	 * //x is left->right, y is down->up
	 */
			state;


	public Box(int x_, int y_) {
		x = x_;
		y = y_;
		state = 0;
		setCanModify(true);
	}

	public void impossibru() {//toggles flagged state
		if (state == 1) setState(0);
	}

	public void green() {//checks solution
		if (state == 0) setState(1);
		setCanModify(false);

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
