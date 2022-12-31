package BeeSimulation.agents;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

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
 * Represents the hive agent
 *
 */
public class Hive {

	private final Grid<Object> grid;
	private final int x;
	private final int y;
	private final int beeCost;
	private int food;
	private int cumulativeFood;
	private List<Bee> wagglers = new LinkedList<Bee>();
	private int beeIdCntr;
	private final double honeyHarvestProb;
	private final double propHoneyHarvested;
	private Optional<EventConsumer> userPanel = Optional.empty();
	private final Parameters params = RunEnvironment.getInstance().getParameters();

	/**
	 * Create a new hive
	 * 
	 * @param grid The grid on which the hive exists
	 * @param x    x position of hive
	 * @param y    y position of hive
	 */
	public Hive(Grid<Object> grid, final int x, final int y) {
		this.grid = grid;
		this.x = x;
		this.y = y;
		Parameters p = RunEnvironment.getInstance().getParameters();
		beeIdCntr = (Integer) p.getValue(Params.NUM_BEES.getValue()) - 1;
		beeCost = (Integer) p.getValue(Params.BEE_COST.getValue());
		honeyHarvestProb = (Double) p.getValue(Params.HONEY_HARVEST_PROB.getValue());
		propHoneyHarvested = (Double) p.getValue(Params.PROPORTION_HONEY_HARVESTED.getValue());
	}

	public void harvestHoney() {
		double remaining = food * propHoneyHarvested;
		food = (int) Math.round(remaining);
	}

	public long beeCount() {
		@SuppressWarnings("unchecked")
		Context<Hive> context = ContextUtils.getContext(this);
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
		Context<Bee> context = ContextUtils.getContext(this);

		// Add Bees
		double r = RandomHelper.nextDouble();
		// Add bee randomly and if required food is available
		if (r <= 0.01 && beeCost <= food) {
			food -= beeCost;
			Bee bee = new Bee(grid, ++beeIdCntr, x, y);
			context.add(bee);
			grid.moveTo(bee, x, y);
		}

		// Harvest Honey
		if (RandomHelper.nextDouble() < honeyHarvestProb) {
			harvestHoney();
			userPanel.ifPresent(panel -> panel.logEvent());
		}

	}

	/**
	 * Get the current number of flowers in the simulation
	 * 
	 * @return flower count
	 */
	public long getFlowerCount() {
		System.out.println("collecting data");
		Iterable<Object> objs = grid.getObjects();
		return StreamSupport.stream(objs.spliterator(), false).filter(Flower.class::isInstance).count();
	}

	/**
	 * Get current number of bees
	 * 
	 * @return bee count
	 */
	public long getBeeCount() {
		System.out.println("collecting data");
		Iterable<Object> objs = grid.getObjects();
		return StreamSupport.stream(objs.spliterator(), false).filter(Bee.class::isInstance).count();
	}

	/**
	 * Get amount of food collected this tick
	 * 
	 * @return food count
	 */
	public int getFoodCollected() {
		System.out.println("collecting data");
		Iterable<Object> objs = grid.getObjects();
		return StreamSupport.stream(objs.spliterator(), false).filter(Bee.class::isInstance).map(Bee.class::cast)
				.map(bee -> bee.foundFood()).reduce(0, Integer::sum);
	}

	public int getRandomSeed() {
		return params.getInteger(Params.RANDOM_SEED.getValue());
	}

	public int getSightRadius() {
		return params.getInteger(Params.SIGHT_RADIUS.getValue());
	}

	public int getGridWidth() {
		return params.getInteger(Params.GRID_WIDTH.getValue());
	}

	public int getGridHeight() {
		return params.getInteger(Params.GRID_HEIGHT.getValue());
	}

	public int getNumBees() {
		return params.getInteger(Params.NUM_BEES.getValue());
	}

	public double getFlowerDensity() {
		return params.getDouble(Params.FLOWER_DENSITY.getValue());
	}

	public int getBeeCost() {
		return params.getInteger(Params.BEE_COST.getValue());
	}

	public double getBeeRegenRate() {
		return params.getDouble(Params.BEE_REGEN_RATE.getValue());
	}

	public double getFlowerRegenRate() {
		return params.getDouble(Params.FLOWER_REGEN_RATE.getValue());
	}

	public int getMaxTicks() {
		return params.getInteger(Params.MAX_TICKS.getValue());
	}

	public double getHerbicideDriftProb() {
		return params.getDouble(Params.HERBICIDE_DRIFT_PROB.getValue());
	}

	public double getHoneyHarvestProb() {
		return params.getDouble(Params.HONEY_HARVEST_PROB.getValue());
	}

	public double getDeathProb() {
		return params.getDouble(Params.DEATH_PROB.getValue());
	}

	public int getFoodPerFlower() {
		return params.getInteger(Params.FOOD_PER_FLOWER.getValue());
	}

	public double getProportionHoneyHarvested() {
		return params.getDouble(Params.PROPORTION_HONEY_HARVESTED.getValue());
	}
}
