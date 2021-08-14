package BeeSimulation.agents;

import java.util.LinkedList;
import java.util.List;

public class Hive {

	private final int x;
	private final int y;
	private int nectar;
	private List<Bee> wagglers = new LinkedList<Bee>();

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

	public void joinWagglers(Bee bee) {
		wagglers.add(bee);
	}

	public void leaveWagglers(Bee bee) {
		wagglers.remove(bee);
	}
	
	public List<Bee> getWagglers() {
		return wagglers;
	}
}
