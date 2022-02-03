package BeeSimulation.lib;

import java.util.ArrayList;
import java.util.List;

public class BearAttackStore {

	private static BearAttackStore bearStore = new BearAttackStore();
	private List<Integer> ticks = new ArrayList<>();

	private BearAttackStore() {

	}

	public BearAttackStore getInstance() {
		return bearStore;
	}

	public void addTick(int tick) {
		ticks.add(tick);
	}

	public List<Integer> getTicks() {
		return ticks;
	}

}
