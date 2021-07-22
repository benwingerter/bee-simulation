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

public class BeeSimulationContextBuilder implements ContextBuilder<Object> {

	@Override
	public Context<Object> build(Context<Object> context) {
		context.setId("BeeSimulation");

		final Parameters p = RunEnvironment.getInstance().getParameters();
		final var seed = (Integer) p.getValue("randomSeed");
		final var random = new Random(seed);

		final var startX = 0;
		final var startY = 0;

		final GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		final Grid<Object> grid = gridFactory.createGrid("Grid", context,
				new GridBuilderParameters<Object>(new StickyBorders(), new SimpleGridAdder<Object>(), true, 50, 50));

		final Hive hive = new Hive(startX, startY);
		context.add(hive);

		for (int i = 0; i < 15; i++) {
			final var bee = new Bee(grid, random);
			context.add(bee);
			grid.moveTo(bee, startX, startY);
		}

		for (int i = 0; i < 20; i++) {
			context.add(new Flower(grid, random));
		}

		return context;
	}

}
