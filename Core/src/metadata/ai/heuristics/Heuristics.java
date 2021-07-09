package metadata.ai.heuristics;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import annotations.Opt;
import game.Game;
import main.collections.FVector;
import metadata.ai.AIItem;
import metadata.ai.heuristics.terms.HeuristicTerm;
import other.context.Context;

/**
 * Defines a collection of heuristics, which can be used by Alpha-Beta agent in
 * Ludii for their heuristics state evaluations.
 *
 * @author Dennis Soemers
 */
public class Heuristics implements AIItem
{
	
	//-------------------------------------------------------------------------
	
	/** Our array of heuristic terms */
	protected final HeuristicTerm[] heuristicTerms;
	
	//-------------------------------------------------------------------------
	
	/**
	 * For a single heuristic term.
	 * 
	 * @param term A single heuristic term.
	 * 
	 * @example (heuristics (score))
	 */
	public Heuristics(@Opt final HeuristicTerm term)
	{
		if (term == null)
			heuristicTerms = new HeuristicTerm[]{};
		else
			heuristicTerms = new HeuristicTerm[]{term};
	}
	
	/**
	 * For a collection of multiple heuristic terms.
	 * 
	 * @param terms A sequence of multiple heuristic terms, which will all
	 * be linearly combined based on their weights.
	 * 
	 * @example (heuristics { (material) (mobilitySimple weight:0.01) })
	 */
	public Heuristics(@Opt final HeuristicTerm[] terms)
	{
		if (terms == null)
			heuristicTerms = new HeuristicTerm[]{};
		else
			heuristicTerms = terms;
	}
	
	/**
	 * Copy constructor (written as static method so it's not picked up by grammar)
	 * @param other
	 * @return Copy of heuristics
	 */
	public static Heuristics copy(final Heuristics other)
	{
		if (other == null)
			return null;
		
		return new Heuristics(other);
	}
	
	/**
	 * Copy constructor (private, not visible to grammar)
	 * @param other
	 */
	public Heuristics(final Heuristics other)
	{
		heuristicTerms = new HeuristicTerm[other.heuristicTerms.length];
		for (int i = 0; i < heuristicTerms.length; ++i)
		{
			heuristicTerms[i] = other.heuristicTerms[i].copy();
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Computes heuristic value estimate for the given state
	 * from the perspective of the given player.
	 * 
	 * @param context
	 * @param player
	 * @param absWeightThreshold We skip terms with an absolute weight below this value 
	 * 	(negative for no skipping)
	 * @return Heuristic value estimate
	 */
	public float computeValue(final Context context, final int player, final float absWeightThreshold)
	{
		float value = 0.f;
		
		for (final HeuristicTerm term : heuristicTerms)
		{
			final float weight = term.weight();
			final float absWeight = Math.abs(weight);
			if (absWeight >= absWeightThreshold)
			{
				float termOutput = term.computeValue(context, player, absWeightThreshold / absWeight);
				if (term.transformation() != null)
					termOutput = term.transformation().transform(context, termOutput);
				
				value += weight * termOutput;
			}
		}

		return value;
	}
	
	/**
	 * Initialises all terms for given game
	 * @param game
	 */
	public void init(final Game game)
	{
		for (final HeuristicTerm term : heuristicTerms)
		{
			term.init(game);
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context
	 * @param player
	 * @return Heuristics feature vector in given state, from perspective of given player
	 */
	public FVector computeStateFeatureVector(final Context context, final int player)
	{
		final Game game = context.game();
		final int numPlayers = game.players().count();
		
		FVector featureVector = new FVector(0);
		
		for (final HeuristicTerm term : heuristicTerms)
		{
			final FVector vec = term.computeStateFeatureVector(context, player);
			
			for (int p = 1; p <= numPlayers; ++p)
			{
				if (p != player)
				{
					final FVector oppVector = term.computeStateFeatureVector(context, p);
					vec.subtract(oppVector);
				}
			}
			
			for (int j = 0; j < vec.dim(); ++j)
			{
				if (term.transformation() != null)
					vec.set(j, term.transformation().transform(context, vec.get(j)));
			}
			
			featureVector = FVector.concat(featureVector, vec);
		}
		
		return featureVector;
	}
	
	/**
	 * @return Vector of parameters (weights including "internal" weights of nested heuristics)
	 */
	public FVector paramsVector()
	{
		// TODO our math here is only going to be correct for now because
		// we don't use non-linear transformations with any heuristics
		// that have internal pieceWeights vectors...
		FVector paramsVector = new FVector(0);
		
		for (final HeuristicTerm term : heuristicTerms)
		{
			final float weight = term.weight();
			final FVector vec = term.paramsVector();
			
			if (vec == null)
			{
				paramsVector = paramsVector.append(weight);
			}
			else
			{
				final FVector weightedVec = new FVector(vec);
				weightedVec.mult(weight);
				paramsVector = FVector.concat(paramsVector, weightedVec);
			}
		}
		
		return paramsVector;
	}
	
	/**
	 * Updates weights in heuristics based on given vector of params
	 * @param game
	 * @param newParams
	 * @param startIdx Index at which to start reading params
	 */
	public void updateParams(final Game game, final FVector newParams, final int startIdx)
	{
		int currentIdx = startIdx;
		
		for (final HeuristicTerm term : heuristicTerms)
		{
			currentIdx = term.updateParams(game, newParams, currentIdx);
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Our array of Heuristic Terms
	 */
	public HeuristicTerm[] heuristicTerms()
	{
		return heuristicTerms;
	}
	
	/**
	 * Writes these heuristics to a text file
	 * @param game
	 * @param filepath
	 */
	public void toFile(final Game game, final String filepath)
	{
		try (final PrintWriter writer = new PrintWriter(filepath, "UTF-8"))
		{
			writer.println("(heuristics { \n");
			for (final HeuristicTerm term : heuristicTerms)
			{
				writer.println("    " + term.toString());
			}
			writer.println("} )");
		} 
		catch (final FileNotFoundException | UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("(heuristics {\n");
		
		for (final HeuristicTerm term : heuristicTerms)
			sb.append("    " + term.toString() + "\n");
			
		sb.append("})\n");

		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param thresholdWeight
	 * @return A string representation of these heuristics, with any terms
	 * for which the absolute weight does not exceed the given threshold removed.
	 */
	public String toStringThresholded(final float thresholdWeight)
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("(heuristics {\n");
		
		for (final HeuristicTerm term : heuristicTerms)
		{
			final String termStr = term.toStringThresholded(thresholdWeight);
			
			if (termStr != null)
				sb.append("    " + termStr + "\n");
		}
			
		sb.append("})\n");

		return sb.toString();
	}
	
	//-------------------------------------------------------------------------

}
