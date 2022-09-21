package ai.planner;

import core.Move;

public interface Discardable {
	
	public static final int YES = 0;
	public static final int NO = 1;
	public static final int MAYBE = 2;
	
	public int canKeep(Move m);

}
