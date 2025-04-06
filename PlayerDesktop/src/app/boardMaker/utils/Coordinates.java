package app.boardMaker.utils;

public class Coordinates
{
	private int x;
	private int y;
	
	public Coordinates(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}

	@Override
	public boolean equals(Object obj)
	{
		Coordinates cmp = (Coordinates) obj;
		return x == cmp.getX() && y == cmp.getY();
	}
	
	@Override
	public String toString() {
		return "("+x+","+y+")";
	}

}
