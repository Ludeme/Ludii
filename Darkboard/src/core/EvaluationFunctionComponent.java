/*
 * Created on 17-mar-06
 *
 */
package core;

import java.util.Vector;

/**
 * @author Nikola Novarlic
 *
 */
public class EvaluationFunctionComponent {
	
	protected String name;
	
	public float evaluate(core.Metaposition start, core.Metaposition dest, Move m, Vector history)
	{
		return 0.0f;
	}
	
	/**
	 * If true, then the component will attach a custom Object to the game tree node, otherwise
	 * the evaluation function will attach the evaluation score by default.
	 * @return
	 */
	public boolean generatesCustomReport()
	{
		return false;
	}

}
