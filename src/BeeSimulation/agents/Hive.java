package BeeSimulation.agents;

public class Hive {

	private final int x;
	private final int y;
	private int nectar;

	public Hive(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	public void deposit(int nectar) {
		this.nectar += nectar;
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}
}
