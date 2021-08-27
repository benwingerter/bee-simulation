package BeeSimulation.agents;

import java.util.Random;
import repast.simphony.context.Context;
import repast.simphony.util.ContextUtils;

public class Flower {

	private final int id;
	private int nectar;
	private int x;
	private int y;

	public Flower(Random random, int id, int x, int y) {
		this.nectar = random.nextInt(5) + 1;
		this.x = x;
		this.y = y;
		this.id = id;
	}

	public int getId() {
		return id;
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
