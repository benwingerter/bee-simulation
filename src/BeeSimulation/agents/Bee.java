package BeeSimulation.agents;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import BeeSimulation.lib.Coordinate;
import BeeSimulation.lib.FlowerLocation;
import BeeSimulation.lib.Params;
import cern.jet.random.Normal;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

public class Bee {

	private static enum State {
		WANDER, RETURN_TO_HIVE, DEPOSIT_NECTAR, AT_HIVE, WAGGLE, EXPLOIT, JUMP
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
	private final int id;
	private final Grid<Object> grid;
	private final Random random;
	private final int wanderThreshold = 10;
	private final int wagglePersistence = 3;
	private final int jumpPersistence = 10;
	private FlowerLocation knownFlower;
	private State state = State.WANDER;
	private Optional<Coordinate> jumpTo = Optional.empty();
	private int hiveX;
	private int hiveY;
	private int nectar;
	private int waggleCount;

	private int wanderCount;

	private int jumpCount;

	private long foundNectarTick = (long) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();

	private boolean alive = true;

	public Bee(Grid<Object> grid, Random random, int id, int hiveX, int hiveY) {
		this.id = id;
		this.grid = grid;
		this.random = random;
		this.hiveX = hiveX;
		this.hiveY = hiveY;
		Parameters p = RunEnvironment.getInstance().getParameters();
		sightRadius = (Integer) p.getValue(Params.SIGHT_RADIUS.getValue());
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
				List<Bee> wagglers = hive.getWagglers();
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
	 * TODO
	 *
	 * @return if nectar was found during this tick
	 */
	public int foundNectar() {
		long tick = (long) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		if (foundNectarTick == tick) {
			return 1;
		}
		return 0;
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
		Context<Hive> context = ContextUtils.getContext(this);
		Stream<Hive> s = context.getObjectsAsStream(Hive.class);
		List<Hive> hives = s.filter(h -> {
			return h.getX() == x && h.getY() == y;
		}).collect(Collectors.toList());
		if (hives.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(hives.get(0));
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
			this.foundNectarTick = (long) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
			Flower flower = flowers.get(0);
			this.state = State.RETURN_TO_HIVE;
			int flowerNectar = flower.nectarContent();
			if (flowerNectar > 0) {
				nectar += flower.grabNectar();
				if (--flowerNectar > 0) {
					// save the Flower
					double dist = Math.sqrt(Math.pow(hiveX - x, 2) + Math.pow(hiveY - y, 2));
					this.knownFlower = new FlowerLocation(x, y, flowerNectar, dist);
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
			boolean moved = false;
			do {
				// this needs to be saved.
				this.jumpTo = Optional.of(jumpPosition(x, y));

			} while (!moved);
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

	private boolean moveTowards(int destX, int destY) {
		GridPoint location = grid.getLocation(this);
		int x = location.getX();
		int y = location.getY();

		int diffX = destX - x;
		if (diffX > 0) {
			// Move right
			x++;
			grid.moveByDisplacement(this, 1, 0);
		} else if (diffX < 0) {
			// Move left
			x--;
			grid.moveByDisplacement(this, -1, 0);
		}
		int diffY = destY - y;
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

	private void moveTowardsHive() {
		boolean arrived = moveTowards(hiveX, hiveY);
		if (arrived) {
			state = State.DEPOSIT_NECTAR;
			Optional<Hive> optHive = getHive(grid, hiveX, hiveY);
			if (optHive.isPresent()) {
				Hive hive = optHive.get();
				hive.deposit(nectar);
				nectar = 0;
			}
		}
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
		double days1 = 26.6 * 60 * 60 * 24;
		double days2 = 8.9 * 60 * 60 * 24;

		Normal dist = RandomHelper.createNormal(days1, days2);

		// Get seed
		long tick = (long) RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
		double prob = dist.cdf(tick);
		if (random.nextDouble() < prob) {
			// Kill the bee
			Context<Hive> context = ContextUtils.getContext(this);
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

	private void wander() {
		if (wanderCount > wanderThreshold) {

			// Jump to a new location
			this.state = State.JUMP;
			jump();

		} else {
			// Move to random direction, see if there is honey, if so, pick it up
			int direction = random.nextInt(4);
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
					this.knownFlower = new FlowerLocation(closest.getX(), closest.getY(), closest.nectarContent(),
							closestDist);
					this.state = State.EXPLOIT;
				}
			}
		}
	}
}
