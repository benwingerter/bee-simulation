package BeeSimulation.agents;

import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.grid.Grid;
import repast.simphony.util.ContextUtils;

public class Flower {

	private final int id;
	private final Grid<Object> grid;
	private final Random random;
	private int nectar = 5;
	private int x;
	private int y;

	public Flower(Grid<Object> grid, Random random, int id) {
		this.grid = grid;
		this.random = random;
		// TODO handle case for hive location
		x = random.nextInt(50);
		y = random.nextInt(50);
		this.id = id;
	}

	public int getId() {
		return id;
	}

	@ScheduledMethod(start = 0)
	public void init() {
		// Move to a random spot on the grid
		grid.moveTo(this, x, y);
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
