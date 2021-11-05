package BeeSimulation.agents;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import BeeSimulation.lib.BeeLogger;
import BeeSimulation.lib.Coordinate;
import BeeSimulation.lib.FlowerLocation;
import BeeSimulation.lib.Params;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.grid.*;
import repast.simphony.util.ContextUtils;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import org.apache.commons.math3.distribution.NormalDistribution;

public class Bee {

	private final static Logger LOGGER = BeeLogger.getLogger();
	private final int sightRadius;
	private final int id;
	private final Grid<Object> grid;
	private final Random random;
	private final int wanderThreshold = 10;
	private final int wagglePersistence = 3;
	private final int jumpPersistence = 10;
	private FlowerLocation knownFlower;
	private State state = State.WANDER;
	private int hiveX;
	private int hiveY;
	private Optional<Coordinate> jumpTo = Optional.empty();
	private int nectar;
	private int waggleCount;
	private int wanderCount;
	private int jumpCount;
	private boolean alive = true;

	private static enum State {
		WANDER, RETURN_TO_HIVE, DEPOSIT_NECTAR, AT_HIVE, WAGGLE, EXPLOIT, JUMP
	}

	public Bee(Grid<Object> grid, Random random, int id, int hiveX, int hiveY) {
		this.id = id;
		this.grid = grid;
		this.random = random;
		this.hiveX = hiveX;
		this.hiveY = hiveY;
		Parameters p = RunEnvironment.getInstance().getParameters();
		sightRadius = (Integer) p.getValue(Params.SIGHT_RADIUS.getValue());
	}

	@ScheduledMethod(start = 1, interval = 1, shuffle = true)
	public void step() {
		if (!alive) {
			return;
		}

		// Adapted from Khoury DS, Myerscough MR, Barron AB (2011) A Quantitative Model
		// of Honey Bee Colony Population Dynamics. PLoS ONE 6(4): e18491. doi:10.1371/
		// journal.pone.0018491
		// Grid size: 6.5m
		var days1 = 26.6 * 60 * 60 * 24;
		var days2 = 8.9 * 60 * 60 * 24;

		var dist = new NormalDistribution(days1, days2);

		// Get seed
		Parameters p = RunEnvironment.getInstance().getParameters();
		var seed = (Integer) p.getValue(Params.RANDOM_SEED.getValue());

		dist.reseedRandomGenerator(seed);
		long tick = (long) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		var prob = dist.cumulativeProbability(tick);
		if (random.nextDouble() < prob) {
			// Kill the bee
			@SuppressWarnings("unchecked")
			var context = (Context<Object>) ContextUtils.getContext(this);
			context.remove(this);
			alive = false;
			return;
		}

		switch (state) {
		case AT_HIVE:
			atHive();
			break;
		case DEPOSIT_NECTAR:
			state = State.AT_HIVE;
			break;
		case RETURN_TO_HIVE:
			moveTowardsHive();
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

	private void exploit() {
		var arrived = moveTowards(knownFlower.getX(), knownFlower.getY());
		if (arrived) {
			handleAtFlower();
		}
	}

	private void atHive() {
		getHive(grid, hiveX, hiveY).ifPresent(hive -> {
			if (knownFlower != null) {
				// Waggle
				if (waggleCount < wagglePersistence) {
					if (waggleCount == 0) {
						hive.joinWagglers(this);
					}
					waggleCount++;
				} else {
					hive.leaveWagglers(this);
					this.state = State.EXPLOIT;
				}
			} else {
				// Observe the best dance
				var wagglers = hive.getWagglers();
				if (wagglers.size() == 0) {
					this.state = State.WANDER;
				} else {
					this.knownFlower = bestWaggle(wagglers);
					this.state = State.EXPLOIT;
				}
			}
		});
	}

	private void jump() {
		jumpCount++;

		var location = grid.getLocation(this);
		var x = location.getX();
		var y = location.getY();

		// Save a new jump to location if necessary
		if (jumpTo.isEmpty()) {
			var moved = false;
			do {
				// this needs to be saved.
				this.jumpTo = Optional.of(jumpPosition(x, y));

			} while (!moved);
		}

		var moveTo = jumpTo.get();

		// Move towards there
		// No need to check for out-of-grid
		var diffX = moveTo.getX() - x;
		if (diffX > 0) {
			grid.moveByDisplacement(this, 1, 0);
		} else if (diffX < 0) {
			grid.moveByDisplacement(this, -1, 0);
		}

		var diffY = moveTo.getY() - y;
		if (diffY > 0) {
			grid.moveByDisplacement(this, 0, 1);
		} else if (diffX < 0) {
			grid.moveByDisplacement(this, 0, -1);
		}
		// Stop jumping if not moved
		var newLocation = grid.getLocation(this);
		var newX = newLocation.getX();
		var newY = newLocation.getY();

		// End jumping if the edge was reached or persistence is exceeded
		if ((jumpCount == jumpPersistence) || (newX == x && newY == y)) {
			state = State.WANDER;
			jumpCount = 0;
		}

		handleAtFlower();

	}

	private static Coordinate jumpPosition(int x, int y) {
		// Move 10 steps in random direction
		// Find a random angle

		var slopeRad = Math.atan(Math.random() * 2 * Math.PI);
		var newX = (int) (x + Math.cos(slopeRad) * 10);
		var newY = (int) (x + Math.sin(slopeRad) * 10);

		return new Coordinate(newX, newY);
	}

	private void wander() {
		if (wanderCount > wanderThreshold) {

			// Jump to a new location
			this.state = State.JUMP;
			jump();

		} else {
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

			var atFlower = handleAtFlower();
			if (!atFlower) {
				// Look for nearby flowers
				var location = grid.getLocation(this);
				var x = location.getX();
				var y = location.getY();
				var flowers = getFlower(grid, x, y, sightRadius);
				if (!flowers.isEmpty()) {
					// Get the nearest flower
					var closest = flowers.get(0);
					int closestDist = Integer.MAX_VALUE;
					for (Flower flower : flowers) {
						var dist = Math.sqrt(Math.pow(closest.getX() - x, 2) + Math.pow(closest.getY() - y, 2));
						// If flower is closer, save it
						if (dist < closestDist) {
							closest = flower;
						}
					}
					// Move towards nearest flower
					this.knownFlower = new FlowerLocation(closest.getX(), closest.getY(), closest.nectarContent(),
							(double) closestDist);
					this.state = State.EXPLOIT;
				}
			}
		}
	}

	/**
	 * Checks if the bee is at a flower and handles state changes accordingly
	 * 
	 * @return Whether the bee is at a flower
	 */
	private boolean handleAtFlower() {
		var location = grid.getLocation(this);
		var x = location.getX();
		var y = location.getY();

		var flowers = getFlower(grid, x, y, 0);
		if (!flowers.isEmpty()) {
			var flower = flowers.get(0);
			this.state = State.RETURN_TO_HIVE;
			var flowerNectar = flower.nectarContent();
			if (flowerNectar > 0) {
				nectar += flower.grabNectar();
				if (--flowerNectar > 0) {
					// save the Flower
					var dist = Math.sqrt(Math.pow(hiveX - x, 2) + Math.pow(hiveY - y, 2));
					this.knownFlower = new FlowerLocation(x, y, flowerNectar, dist);
				}
			}

			// Log
			var template = "NECTAR FOUND\nBee: %d \nFlower: %d\nTick: %d\nBee Count: %d";
			long tick = (long) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
			@SuppressWarnings("unchecked")
			var context = (Context<Object>) ContextUtils.getContext(this);
			Stream<Object> s = context.getObjectsAsStream(Bee.class);
			long beeCnt = s.count();
			LOGGER.info(String.format(template, id, flower.getId(), tick, beeCnt));
		} else {
			this.state = State.WANDER;
			this.knownFlower = null;
		}
		return !flowers.isEmpty();
	}

	private void moveTowardsHive() {
		var arrived = moveTowards(hiveX, hiveY);
		if (arrived) {
			state = State.DEPOSIT_NECTAR;
			var optHive = getHive(grid, hiveX, hiveY);
			if (optHive.isPresent()) {
				final var hive = optHive.get();
				hive.deposit(nectar);
				nectar = 0;
			}
		}
	}

	private boolean moveTowards(int destX, int destY) {
		var location = grid.getLocation(this);
		var x = location.getX();
		var y = location.getY();

		var diffX = destX - x;
		if (diffX > 0) {
			// Move right
			x++;
			grid.moveByDisplacement(this, 1, 0);
		} else if (diffX < 0) {
			// Move left
			x--;
			grid.moveByDisplacement(this, -1, 0);
		}
		var diffY = destY - y;
		if (diffY > 0) {
			// Move up
			y++;
			grid.moveByDisplacement(this, 0, 1);
		} else if (diffY < 0) {
			// Move down
			y--;
			grid.moveByDisplacement(this, 0, -1);
		}

		// Check if the cell is the destination
		return x == destX && y == destY;
	}

	public FlowerLocation getKnownFlower() {
		return knownFlower;
	}

	public State getState() {
		return state;
	}

	/**
	 * Get the ideal flower from all of the bees waggling
	 * 
	 * @param wagglers The bees currently dancing
	 * @return The flower of the best dance
	 */
	private static FlowerLocation bestWaggle(List<Bee> wagglers) {
		var best = wagglers.get(0).getKnownFlower();
		for (Bee bee : wagglers) {
			var flower = bee.getKnownFlower();
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
		var objs = grid.getObjects();
		List<Flower> flowers = new ArrayList<>();
		for (Object obj : objs) {
			if (obj instanceof Flower && Math
					.sqrt(Math.pow(((Flower) obj).getX() - x, 2) + Math.pow(((Flower) obj).getY() - y, 2)) <= radius) {
				flowers.add((Flower) obj);
			}
		}
		return flowers;
	}

	/**
	 * Get the hive in the given grid position if it exists
	 * 
	 * @param grid environment grid
	 * @param x    x position
	 * @param y    y position
	 * @return the hive if exists
	 */
	private Optional<Hive> getHive(Grid<Object> grid, int x, int y) {
		@SuppressWarnings("unchecked")
		var context = (Context<Hive>) ContextUtils.getContext(this);
		Stream<Hive> s = context.getObjectsAsStream(Hive.class);
		List<Hive> hives = s.filter(h -> {
			return h.getX() == x && h.getY() == y;
		}).collect(Collectors.toList());
		if (hives.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(hives.get(0));
	}
}
