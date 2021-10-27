package metadata.ai.features;

import annotations.Name;
import annotations.Opt;
import game.types.play.RoleType;
import main.StringRoutines;
import metadata.ai.AIItem;
import metadata.ai.misc.Pair;

/**
 * Defines a single feature set, which may be applicable to either a
 * single specific player in a game, or to all players in a game.
 * 
 * @remarks Use \\texttt{All} for feature sets that are applicable to all players
 * in a game, or \\texttt{P1}, \\texttt{P2}, etc. for feature sets that are 
 * applicable only to individual players.
 *
 * @author Dennis Soemers
 */
public class FeatureSet implements AIItem
{
	
	//-------------------------------------------------------------------------
	
	/** Role (should either be All, or a specific Player) */
	protected final RoleType role;
	
	/** Array of strings describing features */
	protected final String[] featureStrings;
	
	/** Array of weights (one per feature) for Selection */
	protected final float[] selectionWeights;
	
	/** Array of weights (one per feature) for Playouts */
	protected final float[] playoutWeights;
	
	/** Array of weights (one per feature) for TSPG objective */
	protected final float[] tspgWeights;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param role The Player (P1, P2, etc.) for which the feature set should apply,
	 * or All if it is applicable to all features in a game.
	 * @param features Complete list of all features and weights for this feature
	 * set.
	 * 
	 * @example (featureSet All { (pair "rel:to=<{}>:pat=<els=[-{}]>" 1.0) })
	 * @example (featureSet P1 { (pair "rel:to=<{}>:pat=<els=[-{}]>" 1.0) })
	 */
	public FeatureSet(final RoleType role, final Pair[] features)
	{
		this.role = role;
		
		featureStrings = new String[features.length];
		selectionWeights = new float[features.length];
		
		for (int i = 0; i < features.length; ++i)
		{
			featureStrings[i] = features[i].key();
			selectionWeights[i] = features[i].floatVal();
		}
		
		playoutWeights = null;
		tspgWeights = null;
	}
	
	/**
	 * Constructor
	 * 
	 * @param role The Player (P1, P2, etc.) for which the feature set should apply,
	 * or All if it is applicable to all features in a game.
	 * @param selectionFeatures Complete list of all features and weights for this feature set, 
	 * for MCTS Selection phase.
	 * @param playoutFeatures Complete list of all features and weights for this feature set, 
	 * for MCTS Playout phase.
	 * @param tspgFeatures Complete list of all features and weights for this feature set, 
	 * trained with Tree Search Policy Gradients objective.
	 * 
	 * @example (featureSet P1 selectionFeatures:{ (pair "rel:to=<{}>:pat=<els=[-{}]>" 1.0) } playoutFeatures:{ (pair "rel:to=<{}>:pat=<els=[-{}]>" 2.0) })
	 */
	public FeatureSet
	(
		final RoleType role, 
		@Opt @Name final Pair[] selectionFeatures,
		@Opt @Name final Pair[] playoutFeatures,
		@Opt @Name final Pair[] tspgFeatures
	)
	{
		this.role = role;
		
		if (selectionFeatures == null && playoutFeatures == null && tspgFeatures == null)
			throw new IllegalArgumentException("At least one of selectionFeatures, playoutFeatures and tspgFeatures must be specified!");
		
		if (selectionFeatures != null && playoutFeatures != null && selectionFeatures.length != playoutFeatures.length)
			throw new UnsupportedOperationException("Different feature strings for Selection and Playout currently not supported!");
		
		if (selectionFeatures != null && tspgFeatures != null && selectionFeatures.length != tspgFeatures.length)
			throw new UnsupportedOperationException("Different feature strings for Selection and TSPG currently not supported!");
		
		if (playoutFeatures != null && tspgFeatures != null && playoutFeatures.length != tspgFeatures.length)
			throw new UnsupportedOperationException("Different feature strings for Playout and TSPG currently not supported!");
		
		assert(selectionFeatures == null || playoutFeatures == null || featureStringsEqual(selectionFeatures, playoutFeatures));
		assert(selectionFeatures == null || tspgFeatures == null || featureStringsEqual(selectionFeatures, tspgFeatures));
		assert(playoutFeatures == null || tspgFeatures == null || featureStringsEqual(playoutFeatures, tspgFeatures));
		
		// NOTE: we currently just assume that all arrays of pairs
		// have exactly the same strings for features
		if (selectionFeatures != null)
		{
			featureStrings = new String[selectionFeatures.length];
			for (int i = 0; i < selectionFeatures.length; ++i)
			{
				featureStrings[i] = selectionFeatures[i].key();
			}
		}
		else if (playoutFeatures != null)
		{
			featureStrings = new String[playoutFeatures.length];
			for (int i = 0; i < playoutFeatures.length; ++i)
			{
				featureStrings[i] = playoutFeatures[i].key();
			}
		}
		else
		{
			featureStrings = new String[tspgFeatures.length];
			for (int i = 0; i < tspgFeatures.length; ++i)
			{
				featureStrings[i] = tspgFeatures[i].key();
			}
		}
		
		if (selectionFeatures != null)
		{
			selectionWeights = new float[featureStrings.length];
			for (int i = 0; i < selectionWeights.length; ++i)
			{
				selectionWeights[i] = selectionFeatures[i].floatVal();
			}
		}
		else
		{
			selectionWeights = null;
		}
		
		if (playoutFeatures != null)
		{
			playoutWeights = new float[featureStrings.length];
			for (int i = 0; i < playoutWeights.length; ++i)
			{
				playoutWeights[i] = playoutFeatures[i].floatVal();
			}
		}
		else
		{
			playoutWeights = null;
		}
		
		if (tspgFeatures != null)
		{
			tspgWeights = new float[featureStrings.length];
			for (int i = 0; i < tspgWeights.length; ++i)
			{
				tspgWeights[i] = tspgFeatures[i].floatVal();
			}
		}
		else
		{
			tspgWeights = null;
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Role for this feature set
	 */
	public RoleType role()
	{
		return role;
	}
	
	/**
	 * @return Array of strings describing features
	 */
	public String[] featureStrings()
	{
		return featureStrings;
	}
	
	/**
	 * @return Array of weights for Selection
	 */
	public float[] selectionWeights()
	{
		if (selectionWeights != null)
			return selectionWeights;
		
		// We'll use playout or TSPG weights as fallback if no selection weights
		if (playoutWeights != null)
			return playoutWeights;
		
		return tspgWeights;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("    (featureSet " + role + " ");
		
		if (selectionWeights != null)
		{
			sb.append("selectionFeatures:{\n");
			for (int i = 0; i < featureStrings.length; ++i)
			{
				sb.append("        (pair ");
				sb.append(StringRoutines.quote(featureStrings[i].trim()) + " ");
				sb.append(selectionWeights[i]);
				sb.append(")\n");
			}
			sb.append("    }\n");
		}
		
		if (playoutWeights != null)
		{
			sb.append("playoutWeights:{\n");
			for (int i = 0; i < featureStrings.length; ++i)
			{
				sb.append("        (pair ");
				sb.append(StringRoutines.quote(featureStrings[i].trim()) + " ");
				sb.append(playoutWeights[i]);
				sb.append(")\n");
			}
			sb.append("    }\n");
		}
		
		if (tspgWeights != null)
		{
			sb.append("tspgWeights:{\n");
			for (int i = 0; i < featureStrings.length; ++i)
			{
				sb.append("        (pair ");
				sb.append(StringRoutines.quote(featureStrings[i].trim()) + " ");
				sb.append(tspgWeights[i]);
				sb.append(")\n");
			}
			sb.append("    }\n");
		}
			
		sb.append("    )\n");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param threshold
	 * @return A string representation of these features, retaining only those for
	 * which the absolute weights exceed the given threshold.
	 */
	public String toStringThresholded(final float threshold)
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("    (featureSet " + role + " ");
		
		if (selectionWeights != null)
		{
			sb.append("selectionFeatures:{\n");
			for (int i = 0; i < featureStrings.length; ++i)
			{
				if (Math.abs(selectionWeights[i]) >= threshold)
				{
					sb.append("        (pair ");
					sb.append(StringRoutines.quote(featureStrings[i].trim()) + " ");
					sb.append(selectionWeights[i]);
					sb.append(")\n");
				}
			}
			sb.append("    }\n");
		}
		
		if (playoutWeights != null)
		{
			sb.append("playoutWeights:{\n");
			for (int i = 0; i < featureStrings.length; ++i)
			{
				if (Math.abs(playoutWeights[i]) >= threshold)
				{
					sb.append("        (pair ");
					sb.append(StringRoutines.quote(featureStrings[i].trim()) + " ");
					sb.append(playoutWeights[i]);
					sb.append(")\n");
				}
			}
			sb.append("    }\n");
		}
		
		if (tspgWeights != null)
		{
			sb.append("tspgWeights:{\n");
			for (int i = 0; i < featureStrings.length; ++i)
			{
				if (Math.abs(tspgWeights[i]) >= threshold)
				{
					sb.append("        (pair ");
					sb.append(StringRoutines.quote(featureStrings[i].trim()) + " ");
					sb.append(tspgWeights[i]);
					sb.append(")\n");
				}
			}
			sb.append("    }\n");
		}
			
		sb.append("    )\n");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Helper method for asserts, check whether two arrays of Pairs have identical
	 * feature strings (different weights are allowed).
	 * 
	 * @param pairsA
	 * @param pairsB
	 * @return
	 */
	private static boolean featureStringsEqual(final Pair[] pairsA, final Pair[] pairsB)
	{
		if (pairsA.length != pairsB.length)
			return false;
		
		for (int i = 0; i < pairsA.length; ++i)
		{
			if (!pairsA[i].key().equals(pairsB[i].key()))
				return false;
		}
		
		return true;
	}
	
	//-------------------------------------------------------------------------

}
