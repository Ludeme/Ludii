/*
 * Created on 17-mar-06
 *
 */
package core.eval.components;

import java.util.Vector;

import core.EvaluationFunctionComponent;
import core.Metaposition;
import core.Move;

/**
 * @author Nikola Novarlic
 *
 */
public class CheckmateProgressComponent extends EvaluationFunctionComponent {

	public CheckmateProgressComponent()
	{
		super();
		name = "Progress";
	}


	public float evaluate(Metaposition start, Metaposition dest, Move m, Vector history)
	{
		float result = (m!=null? dest.computeEnemyKingBonus(start,dest,m) : 0.0f);
		
		return result;
	}

}
