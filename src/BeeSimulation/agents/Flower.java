package BeeSimulation.agents;

import BeeSimulation.lib.Params;
import cern.jet.random.engine.RandomEngine;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.ContextUtils;

public class Flower {

	private final int id;
	private int food;
	private int x;
	private int y;
	private final double herbicideDriftProb;

	public Flower(int id, int x, int y) {
		this.x = x;
		this.y = y;
		this.id = id;
		Parameters p = RunEnvironment.getInstance().getParameters();
		herbicideDriftProb = (Double) p.getValue(Params.HERBICIDE_DRIFT_PROB.getValue());
		this.food = (Integer) p.getValue(Params.FOOD_PER_FLOWER.getValue());
	}

	public int getId() {
		return id;
	}

	@ScheduledMethod(start = 1, interval = 1, shuffle = true)
	public void step() {
		// Random Herbicide Drift
		RandomEngine dist = RandomHelper.getGenerator();
		if (dist.nextDouble() < herbicideDriftProb) {
			@SuppressWarnings("unchecked")
			Context<Flower> context = (Context<Flower>) ContextUtils.getContext(this);
			context.remove(this);
			food = 0;
		}
	}

	/**
	 * Get food from the flower
	 * 
	 * @return amount of food retrieved
	 */
	public int grabFood() {
		food--;
		if (food == 0) {
			@SuppressWarnings("unchecked")
			final Context<Object> context = (Context<Object>) ContextUtils.getContext(this);
			context.remove(this);
		}
		return 1;
	}

	/**
	 * How much food the flower has
	 * 
	 * @return
	 */
	public int foodContent() {
		return food;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

}
