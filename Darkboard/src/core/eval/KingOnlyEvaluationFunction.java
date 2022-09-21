package core.eval;

import java.util.Random;
import java.util.Vector;

import core.EvaluationFunction;
import core.EvaluationFunctionComponent;
import core.Metaposition;
import core.Move;

/**
 * If we are using this evaluation function, it means things aren't going well. At all.
 * @author Nikola Novarlic
 *
 */
public class KingOnlyEvaluationFunction extends EvaluationFunction {

	Random r = new Random();
	
	public class KingOnlyEvaluationFunctionComponent extends EvaluationFunctionComponent
	{
		public float evaluate(Metaposition start, Metaposition dest, Move m, Vector history)
		{
			int x = start.owner.globals.kingLocationX;
			int y = start.owner.globals.kingLocationY;
			
			int dx = x-4;
			if (dx<0) dx = 3-x;
			int dy = y-4;
			if (dy<0) dy = 3-y;
			int d = dx+dy;
			
			return 10.0f - d + (r.nextInt(1000)/10000.0f);
		}
	}
	

	
	public KingOnlyEvaluationFunction()
	{
		super();
		
		this.addComponent(new KingOnlyEvaluationFunctionComponent());
	}
	
	public boolean canHandleSituation(Metaposition root,
			int capx, int capy, int check1, int check2, int tries)
	{
		return (root.owner.globals.pawns+root.owner.globals.knights+root.owner.globals.bishops+
				root.owner.globals.queens+root.owner.globals.rooks==0);
	}
	
	public String getName() { return "King only evaluation function"; }
	public boolean useLoopDetector() { return false; }
	public boolean useKillerPruning() { return false; }
	public float getExplorationAlpha(Metaposition m) { return 1.0f; }
	public float getNodeMultFactor(Metaposition m) { return 0.1f; }
	
}
