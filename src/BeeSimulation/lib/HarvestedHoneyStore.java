package BeeSimulation.lib;

import java.util.ArrayList;
import java.util.List;

public class HarvestedHoneyStore {

	private static HarvestedHoneyStore harvestedHoneyStore = new HarvestedHoneyStore();
	private List<Integer> ticks = new ArrayList<>();

	private HarvestedHoneyStore() {

	}

	public HarvestedHoneyStore getInstance() {
		return harvestedHoneyStore;
	}

	public void addTick(int tick) {
		ticks.add(tick);
	}

	public List<Integer> getTicks() {
		return ticks;
	}

}
