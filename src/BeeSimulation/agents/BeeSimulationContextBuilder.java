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
import repast.simphony.util.ContextUtils;

import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Stream;

import BeeSimulation.lib.BeeLogger;
import BeeSimulation.lib.Params;

public class BeeSimulationContextBuilder extends DefaultContext<Object> implements ContextBuilder<Object> {

	private final static Logger LOGGER = BeeLogger.getLogger();
	private Context<Object> context;
	private Grid<Object> grid;
	private Random random;
	private int hiveX;
	private int hiveY;
	private int gridWidth;
	private int gridHeight;
	private int beeIdCntr;
	private int flowerIdCntr = -1;
	private double flowerRegenRate;

	/**
	 * Setup the simulation context
	 * 
	 * @return the created context
	 */
	public Context<Object> build(Context<Object> context) {

		this.context = context;

		context.setId("BeeSimulation");
		Parameters p = RunEnvironment.getInstance().getParameters();

		/// Pull in parameters
		gridWidth = (Integer) p.getValue(Params.GRID_WIDTH.getValue());
		gridHeight = (Integer) p.getValue(Params.GRID_HEIGHT.getValue());
		flowerRegenRate = (Double) p.getValue(Params.FLOWER_REGEN_RATE.getValue());
		var numBees = (Integer) p.getValue(Params.NUM_BEES.getValue());
		var flowerDensity = (Double) p.getValue(Params.FLOWER_DENSITY.getValue());
		var seed = (Integer) p.getValue(Params.RANDOM_SEED.getValue());

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
			var bee = new Bee(grid, random, beeIdCntr, hiveX, hiveY);
			context.add(bee);
			grid.moveTo(bee, hiveX, hiveY);
		}

		// Add flowers
		for (int i = 0; i < gridHeight; i++) {
			for (int j = 0; j < gridWidth; j++) {
				var r = random.nextDouble();
				if (r < flowerDensity && i != hiveY && j != hiveX) {
					var flower = new Flower(random, ++flowerIdCntr, j, i);
					context.add(flower);
					grid.moveTo(flower, j, i);
				}
			}
		}

		// Setup scheduled methods
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		ScheduleParameters addFlower = ScheduleParameters.createRepeating(1, 1);
		schedule.schedule(addFlower, this, "addFlower");
		ScheduleParameters checkDone = ScheduleParameters.createRepeating(1, 10);
		schedule.schedule(checkDone, this, "checkDone");
		ScheduleParameters logPop = ScheduleParameters.createRepeating(1, 10);
		schedule.schedule(checkDone, this, "logPop");
		return context;
	}

	/**
	 * Terminate the simulation if it is finished
	 */
	public void checkDone() {
		// loop through all bees and see if any are still alive.
		var objs = grid.getObjects();
		boolean bees = false;
		for (Object obj : objs) {
			if (obj instanceof Bee) {
				bees = true;
				break;
			}
		}
		if (!bees) {
			RunEnvironment.getInstance().endRun();
		}
	}

	/**
	 * Dynamically add flowers during runtime
	 */
	public void addFlower() {
		if (random.nextDouble() < flowerRegenRate) {
			int x;
			do {
				x = random.nextInt(gridWidth);
			} while (x == hiveX);
			int y;
			do {
				y = random.nextInt(gridHeight);
			} while (y == hiveY);
			var flower = new Flower(random, ++flowerIdCntr, x, y);
			context.add(flower);
			grid.moveTo(flower, x, y);
		}
	}
	
	public void logPop() {
		var template = "POP LOG\nBee Count: %d";
		Stream<Object> s = context.getObjectsAsStream(Bee.class);
		long beeCnt = s.count();
		LOGGER.info(String.format(template, beeCnt));
	}

}
