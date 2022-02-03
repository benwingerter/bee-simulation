package BeeSimulation.agents;

import java.util.Random;

import BeeSimulation.lib.Params;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.ContextUtils;

public class Flower {

	private final int id;
	private int nectar;
	private int x;
	private int y;
	private final double pesticideDriftProb;

	public Flower(Random random, int id, int x, int y) {
		this.nectar = random.nextInt(5) + 1;
		this.x = x;
		this.y = y;
		this.id = id;
		Parameters p = RunEnvironment.getInstance().getParameters();
		pesticideDriftProb = (Double) p.getValue(Params.PESTICIDE_DRIFT_PROB.getValue());
	}

	public int getId() {
		return id;
	}

	@ScheduledMethod(start = 1, interval = 1, shuffle = true)
	public void step() {
		// Random Pesticide Drift
		var dist = RandomHelper.getGenerator();
		if (dist.nextDouble() < pesticideDriftProb) {
			@SuppressWarnings("unchecked")
			var context = (Context<Object>) ContextUtils.getContext(this);
			context.remove(this);
			nectar = 0;
		}
	}

	/**
	 * Get nectar from the flower
	 * 
	 * @return amount of nectar retrieved
	 */
	public int grabNectar() {
		nectar--;
		if (nectar == 0) {
			@SuppressWarnings("unchecked")
			final Context<Object> context = (Context<Object>) ContextUtils.getContext(this);
			context.remove(this);
		}
		return 1;
	}

	/**
	 * How much nectar the flower has
	 * 
	 * @return
	 */
	public int nectarContent() {
		return nectar;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

}
