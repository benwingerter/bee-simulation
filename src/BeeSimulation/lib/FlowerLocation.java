package BeeSimulation.lib;

public class FlowerLocation {
	private int x;
	private int y;
	private int food;
	private double distance;

	public FlowerLocation(int x, int y, int food, double distance) {
		this.x = x;
		this.y = y;
		this.food = food;
		this.distance = distance;
	}

	public double getScore() {
		return (double)food - distance;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}

	public FlowerLocation getCopy() {
		return new FlowerLocation(x, y, food, distance);
	}
}
