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

public class BeeSimulationContextBuilder extends DefaultContext<Object> implements ContextBuilder<Object> {

	private Context<Object> context;
	private Grid<Object> grid;
	private Random random;
	private int hiveX;
	private int hiveY;
	private int beeIdCntr;

	public Context<Object> build(Context<Object> context) {

		this.context = context;

		context.setId("BeeSimulation");
		Parameters p = RunEnvironment.getInstance().getParameters();

		var gridWidth = (Integer) p.getValue(Params.GRID_WIDTH.getValue());
		var gridHeight = (Integer) p.getValue(Params.GRID_HEIGHT.getValue());

		var seed = (Integer) p.getValue(Params.RANDOM_SEED.getValue());
		random = new Random(seed);

		hiveX = random.nextInt(gridWidth);
		hiveY = random.nextInt(gridHeight);

		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		grid = gridFactory.createGrid("Grid", context, new GridBuilderParameters<Object>(new StickyBorders(),
				new SimpleGridAdder<Object>(), true, gridWidth, gridHeight));

		Hive hive = new Hive(hiveX, hiveY);
		context.add(hive);
		grid.moveTo(hive, hiveX, hiveY);

		for (beeIdCntr = 0; beeIdCntr < 15; beeIdCntr++) {
			var bee = new Bee(grid, random, beeIdCntr, hiveX, hiveY);
			context.add(bee);
			grid.moveTo(bee, hiveX, hiveY);
		}

		for (int i = 0; i < 20; i++) {
			context.add(new Flower(grid, random, i, hiveX, hiveY, gridWidth, gridHeight));
		}

		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		ScheduleParameters params = ScheduleParameters.createRepeating(1, 1); // .createOneTime(1);
		schedule.schedule(params, this, "generateBee");

		return context;
	}

	public void generateBee() {
		var r = random.nextDouble();
		if (r <= 0.01) {
			var bee = new Bee(grid, random, ++beeIdCntr, hiveX, hiveY);
			context.add(bee);
			grid.moveTo(bee, hiveX, hiveY);
		}
	}

}
