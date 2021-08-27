package BeeSimulation.agents;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import BeeSimulation.lib.Params;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;

public class Hive {

	private final Random random;
	private final Grid<Object> grid;
	private final double beeRegenRate;
	private final int x;
	private final int y;
	private final int beeCost;
	private int nectar;
	private List<Bee> wagglers = new LinkedList<Bee>();
	private int beeIdCntr;

	public Hive(Grid<Object> grid, final int x, final int y, Random random) {
		this.grid = grid;
		this.random = random;
		this.x = x;
		this.y = y;
		Parameters p = RunEnvironment.getInstance().getParameters();
		beeIdCntr = (Integer) p.getValue(Params.NUM_BEES.getValue()) - 1;
		beeCost = (Integer) p.getValue(Params.BEE_COST.getValue());
		beeRegenRate = (Double) p.getValue(Params.BEE_REGEN_RATE.getValue());
	}

	@ScheduledMethod(start = 1, interval = 1, shuffle = true)
	public void addBees() {
		@SuppressWarnings("unchecked")
		var context = (Context<Object>) ContextUtils.getContext(this);
		var r = random.nextDouble();
		// Add bee randomly and if required nectar is available
		if (r <= 0.01 && beeCost <= nectar) {
			nectar -= beeCost;
			var bee = new Bee(grid, random, ++beeIdCntr, x, y);
			context.add(bee);
			grid.moveTo(bee, x, y);
		}
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
