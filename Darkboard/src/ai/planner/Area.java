package ai.planner;

import java.util.Vector;

import core.Metaposition;

public interface Area {
	
	public boolean isIncluded(int x, int y, Metaposition m);
	
	public Vector<int[]> getAreaSquares(Metaposition m);
	
	public int getSquareNumber(Metaposition m);

}
