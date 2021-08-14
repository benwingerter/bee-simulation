package BeeSimulation.lib;

public class FlowerLocation {
	private int x;
	private int y;
	private int nectar;
	private double distance;

	public FlowerLocation(int x, int y, int nectar, double distance) {
		this.x = x;
		this.y = y;
		this.nectar = nectar;
		this.distance = distance;
	}

	public double getScore() {
		return (double)nectar - distance;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}

	public FlowerLocation getCopy() {
		return new FlowerLocation(x, y, nectar, distance);
	}
}
