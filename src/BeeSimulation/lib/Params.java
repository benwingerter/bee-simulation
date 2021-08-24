package BeeSimulation.lib;

public enum Params {

	RANDOM_SEED("randomSeed"),
	SIGHT_RADIUS("sightRadius"),
	GRID_WIDTH("gridWidth"),
	GRID_HEIGHT("gridHeight");

	private final String value;

	private Params(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
