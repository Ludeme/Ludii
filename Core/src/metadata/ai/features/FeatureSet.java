package metadata.ai.features;

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
	
	/** Array of weights (one per feature) */
	protected final float[] featureWeights;
	
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
		featureWeights = new float[features.length];
		
		for (int i = 0; i < features.length; ++i)
		{
			featureStrings[i] = features[i].key();
			featureWeights[i] = features[i].floatVal();
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
	 * @return Array of weights for features
	 */
	public float[] featureWeights()
	{
		return featureWeights;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("    (featureSet " + role + " {\n");
		
		for (int i = 0; i < featureStrings.length; ++i)
		{
			sb.append("        (pair ");
			sb.append(StringRoutines.quote(featureStrings[i].trim()) + " ");
			sb.append(featureWeights[i]);
			sb.append(")\n");
		}
		
		sb.append("    })\n");
		
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
		
		sb.append("    (featureSet " + role + " {\n");
		
		for (int i = 0; i < featureStrings.length; ++i)
		{
			if (Math.abs(featureWeights[i]) >= threshold)
			{
				sb.append("        (pair ");
				sb.append(StringRoutines.quote(featureStrings[i].trim()) + " ");
				sb.append(featureWeights[i]);
				sb.append(")\n");
			}
		}
		
		sb.append("    })\n");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------

}
