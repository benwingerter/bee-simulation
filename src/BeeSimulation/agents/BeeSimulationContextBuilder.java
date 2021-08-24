package BeeSimulation.agents;

import repast.simphony.context.Context;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.StickyBorders;
import java.util.Random;

import BeeSimulation.lib.Params;

public class BeeSimulationContextBuilder implements ContextBuilder<Object> {

	@Override
	public Context<Object> build(Context<Object> context) {
		context.setId("BeeSimulation");

		var gridXWidth = 50;
		var gridYWidth = 50;

		Parameters p = RunEnvironment.getInstance().getParameters();
		var seed = (Integer) p.getValue(Params.RANDOM_SEED.getValue());
		var random = new Random(seed);

		var hiveX = random.nextInt(gridXWidth);
		var hiveY = random.nextInt(gridYWidth);

		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("Grid", context, new GridBuilderParameters<Object>(
				new StickyBorders(), new SimpleGridAdder<Object>(), true, gridXWidth, gridXWidth));

		Hive hive = new Hive(hiveX, hiveY);
		context.add(hive);
		grid.moveTo(hive, hiveX, hiveY);

		for (int i = 0; i < 15; i++) {
			final var bee = new Bee(grid, random, i, hiveX, hiveY);
			context.add(bee);
			grid.moveTo(bee, hiveX, hiveY);
		}

		for (int i = 0; i < 20; i++) {
			context.add(new Flower(grid, random, i, hiveX, hiveY));
		}

		return context;
	}

}
