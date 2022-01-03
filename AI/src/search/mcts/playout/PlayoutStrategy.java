package search.mcts.playout;

import java.util.Arrays;

import org.json.JSONObject;

import game.Game;
import other.context.Context;
import other.trial.Trial;
import policies.GreedyPolicy;
import policies.ProportionalPolicyClassificationTree;
import policies.softmax.SoftmaxPolicyLinear;
import policies.softmax.SoftmaxPolicyLogitTree;
import search.mcts.MCTS;

/**
 * Interface for Play-out strategies for MCTS
 * 
 * @author Dennis Soemers
 */
public interface PlayoutStrategy
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Runs full play-out
	 * 
	 * @param mcts
	 * @param context
	 * @return Trial object at end of playout.
	 */
	public Trial runPlayout(final MCTS mcts, final Context context);
	
	/**
	 * Allows a Playout strategy to tell Ludii whether or not it can support playing
	 * any given game. 
	 * 
	 * @param game
	 * @return False if the playout strategy cannot be used in a given game
	 */
	public boolean playoutSupportsGame(final Game game);
	
	/**
	 * @return Flags indicating stats that should be backpropagated
	 */
	public int backpropFlags();
	
	//-------------------------------------------------------------------------
	
	/**
	 * Customise the play-out strategy based on a list of given string inputs.
	 * 
	 * @param inputs
	 */
	public void customise(final String[] inputs);

	//-------------------------------------------------------------------------
	
	/**
	 * @param json
	 * @return Playout strategy constructed from given JSON object
	 */
	public static PlayoutStrategy fromJson(final JSONObject json)
	{
		PlayoutStrategy playout = null;
		final String strategy = json.getString("strategy");
		
		if (strategy.equalsIgnoreCase("Random"))
		{
			return new RandomPlayout(200);
		}
		
		return playout;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param inputs
	 * @return A play-out strategy constructed based on an array of inputs
	 */
	public static PlayoutStrategy constructPlayoutStrategy(final String[] inputs)
	{
		PlayoutStrategy playout = null;
		
		if (inputs[0].endsWith("random") || inputs[0].endsWith("randomplayout"))
		{
			playout = new RandomPlayout();
			playout.customise(inputs);
		}
		else if (inputs[0].endsWith("mast"))
		{
			playout = new MAST();
			playout.customise(inputs);
		}
		else if (inputs[0].endsWith("nst"))
		{
			playout = new NST();
			playout.customise(inputs);
		}
		else if (inputs[0].endsWith("softmax") || inputs[0].endsWith("softmaxplayout") || inputs[0].endsWith("softmaxlinear"))
		{
			playout = new SoftmaxPolicyLinear();
			playout.customise(inputs);
		}
		else if (inputs[0].endsWith("softmaxlogittree"))
		{
			playout = new SoftmaxPolicyLogitTree();
			playout.customise(inputs);
		}
		else if (inputs[0].endsWith("classificationtreepolicy"))
		{
			playout = new ProportionalPolicyClassificationTree();
			playout.customise(inputs);
		}
		else if (inputs[0].endsWith("greedy"))
		{
			playout = new GreedyPolicy();
			playout.customise(inputs);
		}
		else
		{
			System.err.println("Unknown play-out strategy: " + Arrays.toString(inputs));
		}
		
		return playout;
	}
	
	//-------------------------------------------------------------------------

}
