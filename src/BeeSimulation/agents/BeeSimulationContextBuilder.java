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

		Parameters p = RunEnvironment.getInstance().getParameters();
		var seed = (Integer) p.getValue("randomSeed");
		var random = new Random(seed);

		var startX = 0;
		var startY = 0;

		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("Grid", context,
				new GridBuilderParameters<Object>(new StickyBorders(), new SimpleGridAdder<Object>(), true, 50, 50));

		Hive hive = new Hive(startX, startY);
		context.add(hive);
		grid.moveTo(hive, startX, startY);

		for (int i = 0; i < 15; i++) {
			final var bee = new Bee(grid, random, i);
			context.add(bee);
			grid.moveTo(bee, startX, startY);
		}

		for (int i = 0; i < 20; i++) {
			context.add(new Flower(grid, random, i));
		}

		return context;
	}

}
