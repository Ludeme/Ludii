package app.boardMaker.utils;

public class Vertex
{
	private double x;
	private double y;
	
	public Vertex(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}

	/**
	 * Converts coordinates into an array [x,y] of Float
	 * @return the coordinates in the form of an array
	 */
	public Float[] convert() {
		return new Float[]{(float) x,(float) y};
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Vertex)) {
			return false;
		}

		Vertex cmp = (Vertex) obj;
		return x == cmp.getX() && y == cmp.getY();
	}
	
	@Override
	public String toString() {
		return String.format("{%.3f %.3f}",x,y);
	}

}
