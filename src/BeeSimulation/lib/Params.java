package BeeSimulation.lib;

public enum Params {

	RANDOM_SEED("randomSeed"), SIGHT_RADIUS("sightRadius"), GRID_WIDTH("gridWidth"), GRID_HEIGHT("gridHeight"),
	NUM_BEES("numBees"), FLOWER_DENSITY("flowerDensity"), BEE_COST("beeCost"), BEE_REGEN_RATE("beeRegenRate"),
	FLOWER_REGEN_RATE("flowerRegenRate");

	private final String value;

	private Params(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
