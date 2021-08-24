package BeeSimulation.lib;

public enum Params {

	RANDOM_SEED("randomSeed");

	private final String value;

	private Params(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
