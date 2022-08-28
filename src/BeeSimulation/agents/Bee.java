package BeeSimulation.agents;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import BeeSimulation.lib.Coordinate;
import BeeSimulation.lib.FlowerLocation;
import BeeSimulation.lib.Params;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.random.RandomHelper;

public class Bee {

	private static enum State {
		WANDER, RETURN_TO_NEST, DEPOSIT_FOOD, AT_NEST, WAGGLE, EXPLOIT, JUMP
	}

	/**
	 * Get the ideal flower from all of the bees waggling
	 *
	 * @param wagglers The bees currently dancing
	 * @return The flower of the best dance
	 */
	private static FlowerLocation bestWaggle(List<Bee> wagglers) {
		FlowerLocation best = wagglers.get(0).getKnownFlower();
		for (Bee bee : wagglers) {
			FlowerLocation flower = bee.getKnownFlower();
			if (flower.getScore() > best.getScore()) {
				best = flower;
			}
		}
		return best.getCopy();
	}

	/**
	 * Get the flower in the given grid position if it exists
	 *
	 * @param grid   environment grid
	 * @param x      x position
	 * @param y      y position
	 * @param radius The radius to check for seeing a flower
	 * @return the flower if exists
	 */
	private static List<Flower> getFlower(Grid<Object> grid, int x, int y, int radius) {
		Iterable<Object> objs = grid.getObjects();
		List<Flower> flowers = new ArrayList<>();
		for (Object obj : objs) {
			if (obj instanceof Flower && Math
					.sqrt(Math.pow(((Flower) obj).getX() - x, 2) + Math.pow(((Flower) obj).getY() - y, 2)) <= radius) {
				flowers.add((Flower) obj);
			}
		}
		return flowers;
	}

	private static Coordinate jumpPosition(int x, int y) {
		// Move 10 steps in random direction
		// Find a random angle

		double slopeRad = Math.atan(Math.random() * 2 * Math.PI);
		int newX = (int) (x + Math.cos(slopeRad) * 10);
		int newY = (int) (x + Math.sin(slopeRad) * 10);

		return new Coordinate(newX, newY);
	}

	private final int sightRadius;
	private final double deathProb;
	private final int id;
	private final Grid<Object> grid;
	private final int wanderThreshold = 10;
	private final int wagglePersistence = 3;
	private final int jumpPersistence = 10;
	private FlowerLocation knownFlower;
	private State state = State.WANDER;
	private Optional<Coordinate> jumpTo = Optional.empty();
	private int nestX;
	private int nestY;
	private int food;
	private int waggleCount;

	private int wanderCount;

	private int jumpCount;

	private long foundFoodTick = (long) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();

	private boolean alive = true;

	public Bee(Grid<Object> grid, int id, int nestX, int nestY) {
		this.id = id;
		this.grid = grid;
		this.nestX = nestX;
		this.nestY = nestY;
		Parameters p = RunEnvironment.getInstance().getParameters();
		sightRadius = (Integer) p.getValue(Params.SIGHT_RADIUS.getValue());
		deathProb = (Double) p.getValue(Params.DEATH_PROB.getValue());
	}

	private void atNest() {
		getNest(grid, nestX, nestY).ifPresent(nest -> {
			if (knownFlower != null) {
				// Waggle
				if (waggleCount < wagglePersistence) {
					if (waggleCount == 0) {
						nest.joinWagglers(this);
					}
					waggleCount++;
				} else {
					nest.leaveWagglers(this);
					this.state = State.EXPLOIT;
				}
			} else {
				// Observe the best dance
				List<Bee> wagglers = nest.getWagglers();
				if (wagglers.size() == 0) {
					this.state = State.WANDER;
				} else {
					this.knownFlower = bestWaggle(wagglers);
					this.state = State.EXPLOIT;
				}
			}
		});
	}

	private void exploit() {
		boolean arrived = moveTowards(knownFlower.getX(), knownFlower.getY());
		if (arrived) {
			handleAtFlower();
		}
	}

	/**
	 * Indicates whether food was found during the current tick. This is used by
	 * the Repast logging tool.
	 *
	 * @return if food was found during this tick
	 */
	public int foundFood() {
		long tick = (long) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		if (foundFoodTick == tick) {
			return 1;
		}
		return 0;
	}

	/**
	 * Get the nest in the given grid position if it exists
	 *
	 * @param grid environment grid
	 * @param x    x position
	 * @param y    y position
	 * @return the nest if exists
	 */
	private Optional<Nest> getNest(Grid<Object> grid, int x, int y) {
		@SuppressWarnings("unchecked")
		Context<Nest> context = (Context<Nest>) ContextUtils.getContext(this);
		Stream<Nest> s = context.getObjectsAsStream(Nest.class);
		List<Nest> nests = s.filter(h -> {
			return h.getX() == x && h.getY() == y;
		}).collect(Collectors.toList());
		if (nests.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(nests.get(0));
	}

	public int getId() {
		return id;
	}

	public FlowerLocation getKnownFlower() {
		return knownFlower;
	}

	public State getState() {
		return state;
	}

	/**
	 * Checks if the bee is at a flower and handles state changes accordingly
	 *
	 * @return Whether the bee is at a flower
	 */
	private boolean handleAtFlower() {
		GridPoint location = grid.getLocation(this);
		int x = location.getX();
		int y = location.getY();

		List<Flower> flowers = getFlower(grid, x, y, 0);
		if (!flowers.isEmpty()) {
			this.foundFoodTick = (long) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
			Flower flower = flowers.get(0);
			this.state = State.RETURN_TO_NEST;
			int flowerFood = flower.foodContent();
			if (flowerFood > 0) {
				food += flower.grabFood();
				if (--flowerFood > 0) {
					// save the Flower
					double dist = Math.sqrt(Math.pow(nestX - x, 2) + Math.pow(nestY - y, 2));
					this.knownFlower = new FlowerLocation(x, y, flowerFood, dist);
				}
			}
		} else {
			this.state = State.WANDER;
			this.knownFlower = null;
		}
		return !flowers.isEmpty();
	}

	private void jump() {
		jumpCount++;

		GridPoint location = grid.getLocation(this);
		int x = location.getX();
		int y = location.getY();

		// Save a new jump to location if necessary
		if (!jumpTo.isPresent()) {
			this.jumpTo = Optional.of(jumpPosition(x, y));
		}

		Coordinate moveTo = jumpTo.get();

		// Move towards there
		// No need to check for out-of-grid
		int diffX = moveTo.getX() - x;
		if (diffX > 0) {
			grid.moveByDisplacement(this, 1, 0);
		} else if (diffX < 0) {
			grid.moveByDisplacement(this, -1, 0);
		}

		int diffY = moveTo.getY() - y;
		if (diffY > 0) {
			grid.moveByDisplacement(this, 0, 1);
		} else if (diffX < 0) {
			grid.moveByDisplacement(this, 0, -1);
		}
		// Stop jumping if not moved
		GridPoint newLocation = grid.getLocation(this);
		int newX = newLocation.getX();
		int newY = newLocation.getY();

		// End jumping if the edge was reached or persistence is exceeded
		if ((jumpCount == jumpPersistence) || (newX == x && newY == y)) {
			state = State.WANDER;
			jumpCount = 0;
		}

		handleAtFlower();

	}

	/**
	 * Move the bee towards a location. The bee can up one unit in the cardinal
	 * directions or diagonally.
	 * 
	 * @param xDestination Destination x coordinate
	 * @param yDestination Destination y coordinate
	 * @return The bee has arrived to the destination
	 */
	private boolean moveTowards(int xDestination, int yDestination) {
		GridPoint location = grid.getLocation(this);
		int x = location.getX();
		int y = location.getY();

		int xDifference = xDestination - x;
		int yDifference = yDestination - y;

		int yDirection = yDifference > 0 ? 1 : -1;
		int xDirection = xDifference > 0 ? 1 : -1;

		if (xDifference == 0) {
			// move up or down
			grid.moveByDisplacement(this, 0, yDirection);
		} else {
			double absoluteSlope = Math.abs(yDifference / (double) xDifference);
			double degreesToDestination = Math.atan(absoluteSlope);
			if (degreesToDestination < Math.PI / 8) {
				x += xDirection;
				grid.moveByDisplacement(this, xDirection, 0);
			} else if (degreesToDestination < 3 * Math.PI / 8) {
				x += xDirection;
				y += yDirection;
				grid.moveByDisplacement(this, xDirection, yDirection);
			} else {
				y += yDirection;
				grid.moveByDisplacement(this, 0, yDirection);
			}
		}

		// Check if the cell is the destination
		return x == xDestination && y == yDestination;
	}

	/**
	 * Move towards the nest
	 */
	private void moveTowardsNest() {
		boolean arrived = moveTowards(nestX, nestY);
		if (arrived) {
			state = State.DEPOSIT_FOOD;
			Optional<Nest> optNest = getNest(grid, nestX, nestY);
			if (optNest.isPresent()) {
				Nest nest = optNest.get();
				nest.deposit(food);
				food = 0;
			}
		}
	}

	/**
	 * Method that gets called on every tick increment that handles bee behavior
	 */
	@ScheduledMethod(start = 1, interval = 1, shuffle = true)
	public void step() {
		if (!alive) {
			return;
		}

		if (RandomHelper.nextDouble() < deathProb) {
			// Kill the bee
			@SuppressWarnings("unchecked")
			Context<Bee> context = (Context<Bee>) ContextUtils.getContext(this);
			context.remove(this);
			alive = false;
			return;
		}

		switch (state) {
		case AT_NEST:
			atNest();
			break;
		case DEPOSIT_FOOD:
			state = State.AT_NEST;
			break;
		case RETURN_TO_NEST:
			moveTowardsNest();
			break;
		case JUMP:
			jump();
		case WANDER:
			wander();
			break;
		case EXPLOIT:
			exploit();
			break;
		default:
			break;
		}
	}

	/**
	 * Handles the wander state of a bee.
	 */
	private void wander() {
		if (wanderCount > wanderThreshold) {

			// Jump to a new location
			this.state = State.JUMP;
			jump();

		} else {
			// Move to random direction, see if there is honey, if so, pick it up
			int direction = RandomHelper.nextIntFromTo(0, 4 - 1);
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

			boolean atFlower = handleAtFlower();
			if (!atFlower) {
				// Look for nearby flowers
				GridPoint location = grid.getLocation(this);
				int x = location.getX();
				int y = location.getY();
				List<Flower> flowers = getFlower(grid, x, y, sightRadius);
				if (!flowers.isEmpty()) {
					// Get the nearest flower
					Flower closest = flowers.get(0);
					int closestDist = Integer.MAX_VALUE;
					for (Flower flower : flowers) {
						double dist = Math.sqrt(Math.pow(closest.getX() - x, 2) + Math.pow(closest.getY() - y, 2));
						// If flower is closer, save it
						if (dist < closestDist) {
							closest = flower;
						}
					}
					// Move towards nearest flower
					this.knownFlower = new FlowerLocation(closest.getX(), closest.getY(), closest.foodContent(),
							closestDist);
					this.state = State.EXPLOIT;
				}
			}
		}
	}
}
