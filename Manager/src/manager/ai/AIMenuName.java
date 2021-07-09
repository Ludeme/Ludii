package manager.ai;

import java.util.HashMap;

/**
 * Possible menu names (in the Player Panel) for the AI.
 * 
 * @author Matthew.Stephenson
 */
public enum AIMenuName
{
	Human("Human", -1),
	LudiiAI("Ludii AI", -1),
	Random("Random", 1),
	FlatMC("Flat MC", 2),
	UCT("UCT", 3),
	UCTUncapped("UCT (Uncapped)", 4),
	MCGRAVE("MC-GRAVE", 5),
	ProgressiveHistory("Progressive History", 6),
	MAST("MAST", 7),
	BiasedMCTS("Biased MCTS", 8),
	BiasedMCTSUniformPlayouts("MCTS (Biased Selection)", 9),
	AlphaBeta("Alpha-Beta", 10),
	BRSPlus("BRS+", 11),
	HybridMCTS("MCTS (Hybrid Selection)", 12),
	BanditTreeSearch("Bandit Tree Search", 13),
	FromJAR("From JAR", -1);
	
	//-------------------------------------------------------------------------
	
	private final String label;
	private final int id;
	
	//-------------------------------------------------------------------------
	 
    private AIMenuName(final String label, final int id) 
    {
        this.label = label;
        this.id = id;
    }
    
    //-------------------------------------------------------------------------
    
    public String label()
    {
    	return label;
    }
    
    public static AIMenuName getAIMenuName(final String label)
    {
    	for (final AIMenuName menuName : AIMenuName.values()) 
    		if (menuName.label.equals(label))
    			return menuName;
    	
    	return null;
    }

	public int id() 
	{
		return id;
	}
	
	/** Returns the names and database Ids of all agents. */
	public static HashMap<String, Integer> getAllAgentIds()
	{
		final HashMap<String, Integer> allAgentNameIdPairs = new HashMap<String, Integer>();
		for (final AIMenuName menuName : AIMenuName.values()) 
			allAgentNameIdPairs.put(menuName.label, Integer.valueOf(menuName.id));
		return allAgentNameIdPairs;
	}
    
    //-------------------------------------------------------------------------
	
}
