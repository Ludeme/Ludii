package ai.mc.strategy;

import ai.mc.MCLink;
import ai.mc.MCState;
import ai.mc.MCStrategy;

public class MCNullPlan extends ai.mc.MCStrategy {

	public static MCNullPlan pl = new MCNullPlan();
	
	@Override
	public MCStrategy[] continuations(MCStrategy[] sofar) {
		// TODO Auto-generated method stub
		MCStrategy out[] = new MCStrategy[1];
		out[0] = pl;
		return out;
	}

	@Override
	public double[] progressInformation(MCState m, MCLink[] moves) {
		
		return new double[moves.length];
	}
	
	public String toString()
	{
		return "NULL";
	}

}
