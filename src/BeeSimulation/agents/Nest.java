package BeeSimulation.agents;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import BeeSimulation.lib.Params;
import BeeSimulation.userpanel.EventConsumer;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;

/**
 * Represents the nest agent
 *
 */
public class Nest {

	private final Grid<Object> grid;
	private final int x;
	private final int y;
	private final int beeCost;
	private int food;
	private int cumulativeFood;
	private List<Bee> wagglers = new LinkedList<Bee>();
	private int beeIdCntr;
	private final double bearAttackProb;
	private Optional<EventConsumer> userPanel = Optional.empty();

	/**
	 * Create a new nest
	 * 
	 * @param grid The grid on which the nest exists
	 * @param x    x position of nest
	 * @param y    y position of nest
	 */
	public Nest(Grid<Object> grid, final int x, final int y) {
		this.grid = grid;
		this.x = x;
		this.y = y;
		Parameters p = RunEnvironment.getInstance().getParameters();
		beeIdCntr = (Integer) p.getValue(Params.NUM_BEES.getValue()) - 1;
		beeCost = (Integer) p.getValue(Params.BEE_COST.getValue());
		bearAttackProb = (Double) p.getValue(Params.BEAR_ATTACK_PROB.getValue());
	}

	public void bearAttack() {
		food = 0;
	}

	public long beeCount() {
		@SuppressWarnings("unchecked")
		Context<Nest> context = (Context<Nest>) ContextUtils.getContext(this);
		return context.getObjectsAsStream(Bee.class).count();
	}

	public void deposit(int food) {
		this.food += food;
		this.cumulativeFood += food;
	}

	public int getCumulativeFood() {
		return cumulativeFood;
	}

	public int getFood() {
		return food;
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
		Context<Bee> context = (Context<Bee>) ContextUtils.getContext(this);

		// Add Bees
		double r = RandomHelper.nextDouble();
		// Add bee randomly and if required food is available
		if (r <= 0.01 && beeCost <= food) {
			food -= beeCost;
			Bee bee = new Bee(grid, ++beeIdCntr, x, y);
			context.add(bee);
			grid.moveTo(bee, x, y);
		}

		// Bear Attack
		if (RandomHelper.nextDouble() < bearAttackProb) {
			bearAttack();
			userPanel.ifPresent(panel -> panel.logEvent());
		}

	}
}
