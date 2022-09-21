package ai.mc;

import java.util.Vector;

import ai.mc.strategy.MCMoveTowardsSquareStrategy;
import ai.mc.strategy.MCNullPlan;
import core.Chessboard;
import core.uberposition.Uberposition;

public class MCStrategyFunctions {
	
	public static void initializeHighestLevelFunctions(ai.mc.MCStrategyNode firstRoot, MCState state)
	{
		Uberposition u = (Uberposition)state;
		Vector<MCStrategy> v = new Vector<MCStrategy>();
		
		int empt = 0;
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
				if (u.allied[k][j]==Chessboard.EMPTY) empt++;
		
		double avg = 1.0*(u.pawnsLeft+u.piecesLeft+1.0)/empt;
		double avg2 = 1.0 - avg - 0.1;
		
		//avg2 = 10.0;
		//v.add(new MCMoveTowardsSquareStrategy(1,0));
		
		boolean cont = true;
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				if (cont && u.allied[k][j]==Chessboard.EMPTY && u.empty[k][j]<avg2)
				{
					v.add(new MCMoveTowardsSquareStrategy(k,j));
					//if (v.size()==2) cont = false;
				}
			}
		
		v.add(new MCNullPlan());
		
		firstRoot.links = new MCLink[v.size()];
		for (int k=0; k<v.size(); k++)
		{
			MCStrategy s = v.get(k);
			firstRoot.links[k] = new MCLink(firstRoot);
			firstRoot.links[k].setChild(new ai.mc.MCStrategyNode(s,null,null));
		}
	}

}
