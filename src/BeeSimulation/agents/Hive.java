package BeeSimulation.agents;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import BeeSimulation.lib.Params;
import BeeSimulation.userpanel.EventConsumer;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;

public class Hive {

	private final Random random;
	private final Grid<Object> grid;
	private final int x;
	private final int y;
	private final int beeCost;
	private int nectar;
	private int cumulativeNectar;
	private List<Bee> wagglers = new LinkedList<Bee>();
	private int beeIdCntr;
	private final double bearAttackProb;
	private Optional<EventConsumer> userPanel = Optional.empty();

	public Hive(Grid<Object> grid, final int x, final int y, Random random) {
		this.grid = grid;
		this.random = random;
		this.x = x;
		this.y = y;
		Parameters p = RunEnvironment.getInstance().getParameters();
		beeIdCntr = (Integer) p.getValue(Params.NUM_BEES.getValue()) - 1;
		beeCost = (Integer) p.getValue(Params.BEE_COST.getValue());
		bearAttackProb = (Double) p.getValue(Params.BEAR_ATTACK_PROB.getValue());
	}

	public void bearAttack() {
		nectar = 0;
	}

	public long beeCount() {
		@SuppressWarnings("unchecked")
		Context<Hive> context = (Context<Hive>) ContextUtils.getContext(this);
		return context.getObjectsAsStream(Bee.class).count();
	}

	public void deposit(int nectar) {
		this.nectar += nectar;
		this.cumulativeNectar += nectar;
	}

	public int getCumulativeNectar() {
		return cumulativeNectar;
	}

	public int getNectar() {
		return nectar;
	}

	public List<Bee> getWagglers() {
		return wagglers;
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

	public void registerPanel(EventConsumer panel) {
		this.userPanel = Optional.of(panel);
	}

	@ScheduledMethod(start = 1, interval = 1, shuffle = true)
	public void step() {
		@SuppressWarnings("unchecked")
		Context<Bee> context = (Context<Bee>)ContextUtils.getContext(this);

		// Add Bees
		double r = random.nextDouble();
		// Add bee randomly and if required nectar is available
		if (r <= 0.01 && beeCost <= nectar) {
			nectar -= beeCost;
			Bee bee = new Bee(grid, random, ++beeIdCntr, x, y);
			context.add(bee);
			grid.moveTo(bee, x, y);
		}

		// Bear Attack
		if (random.nextDouble() < bearAttackProb) {
			bearAttack();
			userPanel.ifPresent(panel -> panel.logEvent());
		}

	}
}
