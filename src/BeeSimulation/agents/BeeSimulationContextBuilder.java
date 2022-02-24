package BeeSimulation.agents;

import repast.simphony.context.Context;
import repast.simphony.context.DefaultContext;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.StickyBorders;

import java.util.Random;

import BeeSimulation.lib.Params;

/**
 * Builds the simulation context. This class is NOT re-instantiated when the
 * model is reset
 *
 */
public class BeeSimulationContextBuilder extends DefaultContext<Object> implements ContextBuilder<Object> {

	private Context<Object> context;
	private Grid<Object> grid;
	private Random random;
	private int hiveX;
	private int hiveY;
	private int gridWidth;
	private int gridHeight;
	private int maxTicks;
	private int beeIdCntr;
	private int flowerIdCntr = -1;
	private double flowerRegenRate;
	private long ticks = 0;

	/**
	 * Setup the simulation context
	 * 
	 * @return the created context
	 */
	public Context<Object> build(Context<Object> context) {

		this.context = context;

		ticks = 0;

		context.setId("BeeSimulation");
		Parameters p = RunEnvironment.getInstance().getParameters();

		/// Pull in parameters
		gridWidth = (Integer) p.getValue(Params.GRID_WIDTH.getValue());
		gridHeight = (Integer) p.getValue(Params.GRID_HEIGHT.getValue());
		flowerRegenRate = (Double) p.getValue(Params.FLOWER_REGEN_RATE.getValue());
		maxTicks = (Integer) p.getValue(Params.MAX_TICKS.getValue());
		int numBees = (Integer) p.getValue(Params.NUM_BEES.getValue());
		double flowerDensity = (Double) p.getValue(Params.FLOWER_DENSITY.getValue());
		int seed = (Integer) p.getValue(Params.RANDOM_SEED.getValue());

		// Setup
		random = new Random(seed);
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		grid = gridFactory.createGrid("Grid", context, new GridBuilderParameters<Object>(new StickyBorders(),
				new SimpleGridAdder<Object>(), true, gridWidth, gridHeight));

		// Add hive
		hiveX = random.nextInt(gridWidth);
		hiveY = random.nextInt(gridHeight);
		Hive hive = new Hive(grid, hiveX, hiveY, random);
		context.add(hive);
		grid.moveTo(hive, hiveX, hiveY);

		// Add bees
		for (beeIdCntr = 0; beeIdCntr < numBees; beeIdCntr++) {
			Bee bee = new Bee(grid, random, beeIdCntr, hiveX, hiveY);
			context.add(bee);
			grid.moveTo(bee, hiveX, hiveY);
		}

		// Add flowers
		for (int i = 0; i < gridHeight; i++) {
			for (int j = 0; j < gridWidth; j++) {
				double r = random.nextDouble();
				if (r < flowerDensity && i != hiveY && j != hiveX) {
					Flower flower = new Flower(random, ++flowerIdCntr, j, i);
					context.add(flower);
					grid.moveTo(flower, j, i);
				}
			}
		}

		// Setup scheduled methods
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		ScheduleParameters addFlower = ScheduleParameters.createRepeating(1, 1);
		schedule.schedule(addFlower, this, "addFlower");
		ScheduleParameters checkDone = ScheduleParameters.createRepeating(1, 1);
		schedule.schedule(checkDone, this, "checkDone");
		return context;
	}

	/**
	 * Terminate the simulation if it is finished
	 */
	public void checkDone() {
		ticks++;
		// loop through all bees and see if any are still alive.
		Iterable<Object> objs = grid.getObjects();
		boolean bees = false;
		for (Object obj : objs) {
			if (obj instanceof Bee) {
				bees = true;
				break;
			}
		}
		if (!bees || maxTicks <= ticks) {
			RunEnvironment.getInstance().endRun();
		}
	}

	/**
	 * Dynamically add flowers during runtime
	 */
	public void addFlower() {
		if (random.nextDouble() < flowerRegenRate) {
			int x, y;
			do {
				x = random.nextInt(gridWidth);
				y = random.nextInt(gridHeight);
			} while (x == hiveX && y == hiveY);
			Flower flower = new Flower(random, ++flowerIdCntr, x, y);
			context.add(flower);
			grid.moveTo(flower, x, y);
		}
	}

}
