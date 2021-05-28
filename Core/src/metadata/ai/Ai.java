package metadata.ai;

import annotations.Opt;
import metadata.MetadataItem;
import metadata.ai.features.Features;
import metadata.ai.heuristics.Heuristics;
import metadata.ai.misc.BestAgent;

//-----------------------------------------------------------------------------

/**
 * Defines metadata that can help AIs in the Ludii app to play this game at a
 * stronger level.
 * 
 * @remarks Specifying AI metadata for games is not mandatory.
 * 
 * @author Dennis Soemers and cambolbro
 */
public class Ai implements MetadataItem
{
	// WARNING: The weird capitalisation of of the class name is INTENTIONAL!
	// This makes the type name in the grammar and documentation look better,
	// as just "<ai>" instead of the really silly "<aI>" that we would get 
	// otherwise.
	
	//-------------------------------------------------------------------------
	
	/** Best agent for this game */
	private final BestAgent bestAgent;
	
	/** Heuristics */
	private final Heuristics heuristics;
	
	/** Features */
	private final Features features;
	
	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 * @param bestAgent Can be used to specify the agent that is expected to
	 * perform best in this game. This algorithm will be used when the ``Ludii AI"
	 * option is selected in the Ludii app.
	 * @param heuristics Heuristics to be used by Alpha-Beta agents. If not specified,
	 * Alpha-Beta agents will default to a combination of Material and Mobility heuristics.
	 * @param features Feature sets to be used for biasing MCTS-based agents. If not
	 * specified, Biased MCTS will not be available as an AI for this game in Ludii.
	 * 
	 * @example (ai (bestAgent "UCT"))
	 */
	public Ai
	(
		@Opt final BestAgent bestAgent,
		@Opt final Heuristics heuristics,
		@Opt final Features features
	)
	{
		this.bestAgent = bestAgent;
		this.heuristics = heuristics;
		this.features = features;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return Metadata item describing best agent
	 */
	public BestAgent bestAgent()
	{
		return bestAgent;
	}
	
	/**
	 * @return Heuristics for this game
	 */
	public Heuristics heuristics()
	{
		return heuristics;
	}
	
	/**
	 * @return Features for this game
	 */
	public Features features()
	{
		return features;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("    (ai\n");
		
		if (bestAgent != null)
			sb.append("        " + bestAgent.toString() + "\n");
			
		if (heuristics != null)
			sb.append("        " + heuristics.toString() + "\n");
			
		if (features != null)
			sb.append("        " + features.toString() + "\n");
			
		sb.append("    )\n");

		return sb.toString();
	}
	
	//-------------------------------------------------------------------------

}
