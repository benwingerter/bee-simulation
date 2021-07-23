package BeeSimulation.agents;

import java.util.Optional;
import java.util.Random;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.grid.*;

public class Bee {

	final private Grid<Object> grid;
	final private Random random;
	private int hiveX;
	private int hiveY;
	private int nectar;
	private State state = State.WANDER;

	private static enum State {
		WANDER, RETURN_TO_HIVE, DEPOSIT_NECTAR
	}

	public Bee(Grid<Object> grid, Random random) {
		this.grid = grid;
		this.random = random;
		hiveX = 0;
		hiveY = 0;
	}

	@ScheduledMethod(start = 1, interval = 1, shuffle = true)
	public void step() {
		move();
	}

	private void moveTowardsHive(int currX, int currY) {
		if (hiveX - currX > 0) {
			// Move Up
			grid.moveByDisplacement(this, 1, 0);
		} else {
			// Move down
			grid.moveByDisplacement(this, -1, 0);
		}
		if (hiveY - currY > 0) {
			// Move right
			grid.moveByDisplacement(this, 0, 1);
		} else {
			// Move left
			grid.moveByDisplacement(this, 0, -1);
		}
	}

	private void allNectarCollected() {
		// TODO implement
	}

	/**
	 * Move the bee to a random empty adjacent site.
	 */
	private void move() {

		var location = grid.getLocation(this);
		var x = location.getX();
		var y = location.getY();

		switch (this.state) {
		case RETURN_TO_HIVE:
			moveTowardsHive(x, y);
			// Check if the cell is the hive
			if (x == hiveX && y == hiveY) {
				state = State.DEPOSIT_NECTAR;
				final var optHive = getHive(grid, x, y);
				if (optHive.isPresent()) {
					final var hive = optHive.get();
					hive.deposit(nectar);
					nectar = 0;
				}
			}
			break;
		case DEPOSIT_NECTAR:
			state = State.WANDER;
			break;
		case WANDER:
			// Move to random direction, see if there is honey, if so, pick it up
			var direction = random.nextInt(4);
			switch (direction) {
			case 0:
				// Move up
				grid.moveByDisplacement(this, 0, 1);
				break;
			case 1:
				// Move down
				grid.moveByDisplacement(this, 0, -1);
				break;
			case 2:
				// Move left
				grid.moveByDisplacement(this, -1, 0);
				break;
			case 3:
				// Move right
				grid.moveByDisplacement(this, 1, 0);
				break;
			}

			var optionalFlower = getFlower(grid, x, y);
			if (optionalFlower.isPresent()) {
				this.state = State.RETURN_TO_HIVE;
				var flower = optionalFlower.get();
				if (flower.hasNectar()) {
					nectar += flower.grabNectar();
					moveTowardsHive(x, y);
				}
				// TODO check if all honey is gone
			}
			break;
		}

	}

	/**
	 * Get the flower in the given grid position if it exists
	 * 
	 * @param grid environment grid
	 * @param x    x position
	 * @param y    y position
	 * @return the flower if exists
	 */
	private static Optional<Flower> getFlower(Grid<Object> grid, int x, int y) {
		var objs = grid.getObjects();
		for (Object obj : objs) {
			if (obj instanceof Flower && ((Flower) obj).getX() == x && ((Flower) obj).getY() == y) {
				return Optional.of((Flower) obj);
			}
		}
		return Optional.empty();
	}

	/**
	 * Get the hive in the given grid position if it exists
	 * 
	 * @param grid environment grid
	 * @param x    x position
	 * @param y    y position
	 * @return the hive if exists
	 */
	private static Optional<Hive> getHive(Grid<Object> grid, int x, int y) {
		var objs = grid.getObjects();
		for (Object obj : objs) {
			if (obj instanceof Hive && ((Hive) obj).getX() == x && ((Hive) obj).getY() == y) {
				return Optional.of((Hive) obj);
			}
		}
		return Optional.empty();
	}
}
