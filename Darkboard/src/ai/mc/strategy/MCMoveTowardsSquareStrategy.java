package ai.mc.strategy;

import ai.mc.MCHighLevelStrategy;
import ai.mc.MCStrategy;

public class MCMoveTowardsSquareStrategy extends MCHighLevelStrategy {
	
	public MCMoveTowardsSquareStrategy(int x, int y)
	{
		super("MV",x,y,0,0);
	}
	
	protected void init()
	{
		MCStrategy st[] = {new MCThreatenSquareStrategy(parameters[0],parameters[1]),
				new MCStrikeSquareStrategy(parameters[0],parameters[1])};
		
		messages = st;
		transitions = new int[2][2];
		transitions[0][0] = 0;
		transitions[0][1] = 1;
		transitions[1][0] = -1;
		transitions[1][1] = -1;
		
		continuations = new MCStrategy[2][];
		continuations[0] = st;
		continuations[1] = new MCStrategy[1];
		continuations[1][0] = null;

		stratStrings = new String[messages.length];
		for (int k=0; k<stratStrings.length; k++)
			stratStrings[k] = messages[k].toString();
	}

}
