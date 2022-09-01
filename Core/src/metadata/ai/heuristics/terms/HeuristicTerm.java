package metadata.ai.heuristics.terms;

import java.util.ArrayList;
import java.util.List;

import game.Game;
import game.equipment.component.Component;
import gnu.trove.list.array.TFloatArrayList;
import main.collections.FVector;
import metadata.ai.AIItem;
import metadata.ai.heuristics.transformations.HeuristicTransformation;
import other.context.Context;

/**
 * Abstract class for heuristic terms. Every heuristic term is expected to implement
 * a function that outputs a score for a given game state and player, and every term
 * has a weight that is used for computing linear combinations of multiple terms.
 *
 * @author Dennis Soemers and matthew.stephenson
 */
public abstract class HeuristicTerm implements AIItem
{
	//-------------------------------------------------------------------------
	
	/** The weight of this term in linear combinations */
	protected float weight;
	
	/** Transformation to apply to heuristic score (before multiplying with weight!) */
	protected final HeuristicTransformation transformation;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param transformation An optional transformation to be applied to any raw heuristic
	 * score outputs.
	 * @param weight The weight for this term in a linear combination of multiple terms.
	 * If not specified, a default weight of 1.0 is used.
	 */
	public HeuristicTerm(final HeuristicTransformation transformation, final Float weight)
	{
		if (weight == null)
			this.weight = 1.f;
		else
			this.weight = weight.floatValue();
		
		this.transformation = transformation;
	}
	
	/**
	 * Copy method (not copy constructor, so not visible to grammar)
	 * @return Copy of the heuristic term
	 */
	public abstract HeuristicTerm copy();
	
	//-------------------------------------------------------------------------
	
	/** 
	 * @return English description of this heuristic. 
	 */
	public abstract String description();
	
	/** 
	 * @param context 
	 * @param playerIndex 
	 * @return toString of this Heuristic in an English language format. 
	 */
	public abstract String toEnglishString(final Context context, final int playerIndex);
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param term 
	 * @return if this HeuristicTerm can be merged with the parameter term.
	 */
	public boolean canBeMerged(final HeuristicTerm term)
	{
		return this.getClass().getName().equals(term.getClass().getName());
	}
	
	/**
	 * Merges this HeuristicTerm with the parameter term. Make sure that all pieceWeightNames are the same.
	 * @param term 
	 */
	public void merge(final HeuristicTerm term) 
	{
		setWeight(weight() + term.weight());
	}
	
	/**
	 * Simplifies this heuristic, usually by combining weights.
	 */
	public void simplify() 
	{
		// do nothing
	}
	
	/**
	 * @return the maximum weight value for any aspect of this heuristic.
	 */
	public float maxAbsWeight() 
	{
		return Math.abs(weight());
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Computes heuristic value estimate for the given state
	 * from the perspective of the given player. This should NOT
	 * apply any transformations.
	 * 
	 * @param context
	 * @param player
	 * @param absWeightThreshold We skip terms with an absolute weight below this value 
	 * 	(negative for no skipping)
	 * @return Heuristic value estimate
	 */
	public abstract float computeValue(final Context context, final int player, final float absWeightThreshold);
	
	/**
	 * Allows for initialisation / precomputation of data for a given game.
	 * Default implementation does nothing.
	 * 
	 * @param game
	 */
	public void init(final Game game)
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context
	 * @param player
	 * @return Heuristic's feature vector in given state, from perspective of given player
	 */
	public abstract FVector computeStateFeatureVector(final Context context, final int player);
	
	/**
	 * @return Vector of parameters (weights including "internal" weights of nested heuristics).
	 * Should return null if this term does not have any internal weights (other than the single
	 * weight for use in the linear combination of multiple terms).
	 */
	public abstract FVector paramsVector();
	
	/**
	 * Updates weights in heuristic based on given vector of params.
	 * 
	 * @param game
	 * @param newParams
	 * @param startIdx Index at which to start reading params
	 * @return Index in vector at which the next heuristic term is allowed to start reading
	 */
	public int updateParams(final Game game, final FVector newParams, final int startIdx)
	{
		final FVector internalParams = paramsVector();
		
		if (internalParams == null)
		{
			weight = newParams.get(startIdx);
			return startIdx + 1;
		}
		else
		{
			internalParams.copyFrom(newParams, startIdx, 0, internalParams.dim());
			
			// We let our inner heuristic terms completely absorb the weights,
			// so must ensure to only have a weight of exactly 1.f ourselves
			weight = 1.f;
			
			return startIdx + internalParams.dim();
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return A transformation to be applied to output values (null for no transformation)
	 */
	public HeuristicTransformation transformation()
	{
		return transformation;
	}
	
	/**
	 * @return Weight for this term in linear combination of multiple heuristic terms
	 */
	public float weight()
	{
		return weight;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * A helper method that a variety of heuristic term classes can use to convert
	 * input they were given in a constructor into a game-specific vector of piece
	 * weights.
	 * 
	 * @param game
	 * @param pieceWeightNames
	 * @param gameAgnosticWeightsArray
	 * @return Vector of piece weights for given game
	 */
	protected static FVector pieceWeightsVector
	(
		final Game game,
		final String[] pieceWeightNames, 
		final float[] gameAgnosticWeightsArray
	)
	{
		final Component[] components = game.equipment().components();
		final FVector pieceWeights = new FVector(components.length);
		
		for (int nameIdx = 0; nameIdx < pieceWeightNames.length; ++nameIdx)
		{
			final String s = pieceWeightNames[nameIdx].trim();
			
			for (int i = 1; i < components.length; ++i)
			{
				final String compName = components[i].name();
				
				if (compName.startsWith(s))
				{
					boolean match = true;
					
					if (s.length() > 0)
					{
						if (Character.isDigit(s.charAt(s.length() - 1)))
						{
							if (!s.equals(compName))
								match = false;
						}
						else
						{
							for (int j = s.length(); j < compName.length(); ++j)
							{
								if (!Character.isDigit(compName.charAt(j)))
								{
									match = false;
									break;
								}
							}
						}
					}
					
					if (match)
					{
						pieceWeights.set(i, gameAgnosticWeightsArray[nameIdx]);
					}
				}
			}
		}
		
		return pieceWeights;
	}
	
	/**
	 * Helper method to update our array of game-agnostic weights
	 * based on our current vector of piece weights for a current
	 * game (which may have been modified due to training)
	 * 
	 * @param game
	 * @param pieceWeights
	 * @param pieceWeightNames
	 * @param gameAgnosticWeightsArray
	 * @return Array of two elements: first is a new array of piece names, second a new array of weights
	 */
	protected static Object[] updateGameAgnosticWeights
	(
		final Game game,
		final FVector pieceWeights,
		final String[] pieceWeightNames, 
		final float[] gameAgnosticWeightsArray
	)
	{
		final List<String> newPieceWeightNames = new ArrayList<String>();
		final TFloatArrayList newPieceWeights = new TFloatArrayList();
		
		final Component[] components = game.equipment().components();
		
		for (int i = 1; i < pieceWeights.dim(); ++i)
		{
			newPieceWeightNames.add(components[i].name());
			newPieceWeights.add(pieceWeights.get(i));
		}
		
		return new Object[]{newPieceWeightNames.toArray(new String[0]), newPieceWeights.toArray()};
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param game
	 * @return True if heuristic of this type could be applicable to given game
	 */
	public abstract boolean isApplicable(final Game game);
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param threshold
	 * @return A string representation of this heuristic term, with any components
	 * for which the absolute weight does not exceed the given threshold removed.
	 * Should return null if there are no components remaining after thresholding.
	 */
	public abstract String toStringThresholded(final float threshold);
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param weight
	 */
	public void setWeight(final float weight) 
	{
		this.weight = weight;
	}

	/** 
	 * Used for term reconstruction using a genetic code 
	 * @return The array of game-agnostic weights
	 */
	@SuppressWarnings("static-method")
	public float[] gameAgnosticWeightsArray() 
	{
		return null;
	}
	
	/** 
	 * Used for term reconstruction using a genetic code 
	 * @return The vector of piece weights
	 */
	@SuppressWarnings("static-method")
	public FVector pieceWeights() 
	{
		return null;
	}
	
	//-------------------------------------------------------------------------

}
