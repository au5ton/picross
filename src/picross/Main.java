package picross;

public class Main {
	public static void main(String[] args) {
		Graphics mainWindow = new Graphics();
		new Thread(mainWindow).start();
	}
}
