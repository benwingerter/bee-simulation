package BeeSimulation.lib;

public enum Params {

	RANDOM_SEED("randomSeed"), SIGHT_RADIUS("sightRadius"), GRID_WIDTH("gridWidth"), GRID_HEIGHT("gridHeight"),
	NUM_BEES("numBees"), FLOWER_DENSITY("flowerDensity"), BEE_COST("beeCost"), BEE_REGEN_RATE("beeRegenRate"),
	FLOWER_REGEN_RATE("flowerRegenRate"), MAX_TICKS("maxTicks"), HERBICIDE_DRIFT_PROB("herbicideDriftProb"),
	HONEY_HARVEST_PROB("honeyHarvestProb"), DEATH_PROB("deathProb"), FOOD_PER_FLOWER("foodPerFlower"),
	PROPORTION_HONEY_HARVESTED("proportionHoneyHarvested");

	private final String value;

	private Params(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
