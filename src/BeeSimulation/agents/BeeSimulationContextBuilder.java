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
import repast.simphony.random.RandomHelper;

import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import BeeSimulation.lib.Params;

/**
 * Builds the simulation context. This class is NOT re-instantiated when the
 * model is reset
 *
 */
public class BeeSimulationContextBuilder extends DefaultContext<Object> implements ContextBuilder<Object> {

	private Context<Object> context;
	private Grid<Object> grid;
	private int nestX;
	private int nestY;
	private int gridWidth;
	private int gridHeight;
	private int maxTicks;
	private int beeIdCntr;
	private int flowerIdCntr = -1;
	private double flowerRegenRate;

	/**
	 * Setup the simulation context
	 * 
	 * @return the created context
	 */
	public Context<Object> build(Context<Object> context) {

		// Currently, multiple agent types are being stored in the same context. This
		// would ideally be solved using nested contexts or Projections. Solving it
		// would resolve some type checking issues.
		this.context = context;

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

		RandomHelper.setSeed(seed);

		// Setup
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		grid = gridFactory.createGrid("Grid", context, new GridBuilderParameters<Object>(new StickyBorders(),
				new SimpleGridAdder<Object>(), true, gridWidth, gridHeight));

		// Add nest
		nestX = RandomHelper.nextIntFromTo(0, gridWidth - 1);
		nestY = RandomHelper.nextIntFromTo(0, gridHeight - 1);
		Nest nest = new Nest(grid, nestX, nestY);
		context.add(nest);
		grid.moveTo(nest, nestX, nestY);

		// Add bees
		IntStream.range(0, numBees - 1).forEach(beeCounter -> {
			Bee bee = new Bee(grid, beeIdCntr, nestX, nestY);
			context.add(bee);
			grid.moveTo(bee, nestX, nestY);
		});

		// Add flowers
		IntStream.range(0, gridHeight).forEach(row -> {
			IntStream.range(0, gridWidth).forEach(column -> {
				if (RandomHelper.nextDouble() < flowerDensity && row != nestY && column != nestX) {
					Flower flower = new Flower(++flowerIdCntr, column, row);
					context.add(flower);
					grid.moveTo(flower, column, row);
				}
			});
		});

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
		// See if bee population still exists
		Iterable<Object> objs = grid.getObjects();
		boolean isExtinct = StreamSupport.stream(objs.spliterator(), false).allMatch(obj -> !(obj instanceof Bee));

		double tick = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		if (isExtinct || maxTicks <= tick) {
			RunEnvironment.getInstance().endRun();
		}
	}

	/**
	 * Dynamically add flowers during runtime
	 */
	public void addFlower() {
		if (RandomHelper.nextDouble() < flowerRegenRate) {
			int x = RandomHelper.nextIntFromTo(0, gridWidth - 1);
			int y = RandomHelper.nextIntFromTo(0, gridHeight - 1);
			Flower flower = new Flower(++flowerIdCntr, x, y);
			context.add(flower);
			grid.moveTo(flower, x, y);
		}
	}

}
