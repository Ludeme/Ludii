/*
 * Created on 2-mar-06
 *
 */
package core.eval;

import ai.player.Darkboard;
import core.EvaluationFunction;
import core.eval.components.CheckmateProgressComponent;
import core.eval.components.DefaultPositionComponent;
import core.eval.components.ExtendedMaterialComponent;

/**
 * @author Nikola Novarlic
 * Default evaluation function used most of the time.
 */
public class DefaultEvaluationFunction extends EvaluationFunction {

	public DefaultEvaluationFunction(Darkboard d)
	{
		super();
		
		this.addComponent(new ExtendedMaterialComponent());
		this.addComponent(new DefaultPositionComponent());
		this.addComponent(new CheckmateProgressComponent());
		//this.addComponent(new PlanExecutionComponent(d!=null? d.dashboard : null));
	}
	
	public String getName() { return "Default Evaluation Function"; }
	
	public boolean useLoopDetector() { return true; }

	/*public float evaluate(Metaposition start, Metaposition dest, Move m)
	{
		return Metaposition.evaluate(start,dest,m);
	}*/

}

