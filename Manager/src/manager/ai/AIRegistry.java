package manager.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import game.Game;
import search.flat.FlatMonteCarlo;
import search.mcts.MCTS;
import search.mcts.finalmoveselection.RobustChild;
import search.mcts.playout.RandomPlayout;
import search.mcts.selection.McGRAVE;
import search.minimax.AlphaBetaSearch;
import search.minimax.BRSPlus;
import utils.AIFactory;
import utils.RandomAI;

/**
 * A registry of AIs that can be instantiated in the GUI of Ludii.
 *
 * @author Dennis Soemers
 */
public class AIRegistry
{
	
	//-------------------------------------------------------------------------
	
	/** Our registry */
	protected static Map<String, AIRegistryEntry> registry = new HashMap<String, AIRegistryEntry>();
	
	/** Rank to assign to next registered AI (used for sorting when we want a sorted list of AIs) */
	protected static int nextRank = 0;
	
	static
	{
		// Static block to register our built-in AIs
		registerAI("Human", -1, (game) -> {return false;});		// We have special handling for human in dropdown menus
		registerAI("Ludii AI", -1, (game) -> {return true;});
		registerAI("Random", 1, (game) -> {return new RandomAI().supportsGame(game);});
		registerAI("Flat MC", 2, (game) -> {return new FlatMonteCarlo().supportsGame(game);});
		registerAI("UCT", 3, (game) -> {return MCTS.createUCT().supportsGame(game);});
		registerAI("UCT (Uncapped)", 4, (game) -> {return MCTS.createUCT().supportsGame(game);});
		registerAI("MC-GRAVE", 5, (game) -> {return new MCTS(new McGRAVE(), new RandomPlayout(200), new RobustChild()).supportsGame(game);});
		registerAI("Progressive History", 6, (game) -> {return AIFactory.createAI("Progressive History").supportsGame(game);});
		registerAI("MAST", 7, (game) -> {return AIFactory.createAI("MAST").supportsGame(game);});
		registerAI("Biased MCTS", 8, (game) -> {return MCTS.createBiasedMCTS(0.0).supportsGame(game);});
		registerAI("MCTS (Biased Selection)", 9, (game) -> {return MCTS.createBiasedMCTS(1.0).supportsGame(game);});
		registerAI("Alpha-Beta", 10, (game) -> {return AlphaBetaSearch.createAlphaBeta().supportsGame(game);});
		registerAI("BRS+", 11, (game) -> {return new BRSPlus().supportsGame(game);});
		registerAI("MCTS (Hybrid Selection)", 12, (game) -> {return MCTS.createHybridMCTS().supportsGame(game);});
		registerAI("Bandit Tree Search (Avg)", 13, (game) -> {return MCTS.createBanditTreeSearchAvg().supportsGame(game);});
		registerAI("Bandit Tree Search (MinMax)", 14, (game) -> {return MCTS.createBanditTreeSearchMinMax().supportsGame(game);});
		registerAI("Bandit Tree Search (Avg+MinMax)", 15, (game) -> {return MCTS.createBanditTreeSearchSumAvgMinMax().supportsGame(game);});
		registerAI("From JAR", (game) -> {return false;});	// We have special handling for From JAR in dropdown menus
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Registers a new AI. NOTE: this method does not provide a predicate to test
	 * whether or not any given game is supported, so we assume that ANY game is 
	 * supported!
	 * 
	 * @param label
	 * @return True if we successfully registered an AI, false if an AI with 
	 * 	the same label was already registered.
	 */
	public static boolean registerAI(final String label)
	{
		return registerAI(label, -1, (game) -> {return true;});
	}
	
	/**
	 * Registers a new AI.
	 * 
	 * @param label
	 * @param supportsGame Predicate to test whether or not any given game is supported.
	 * @return True if we successfully registered an AI, false if an AI with 
	 * 	the same label was already registered.
	 */
	public static boolean registerAI(final String label, final SupportsGamePredicate supportsGame)
	{
		return registerAI(label, -1, supportsGame);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param game
	 * @return List of all agent names that are valid for given game
	 */
	public static List<String> generateValidAgentNames(final Game game)
	{
		final List<String> names = new ArrayList<String>();
		
		for (final Entry<String, AIRegistryEntry> entry : registry.entrySet())
		{
			if (entry.getValue().supportsGame(game))
				names.add(entry.getKey());
		}
		
		names.sort
		(
			new Comparator<String>()
			{
				@Override
				public int compare(final String o1, final String o2)
				{
					return registry.get(o1).rank - registry.get(o2).rank;
				}
			}
		);
		return names;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Registers a new AI
	 * @param label
	 * @param dbID
	 * @param supportsGame
	 * @return True if we successfully registered an AI, false if an AI with 
	 * 	the same label was already registered.
	 */
	private static boolean registerAI(final String label, final int dbID, final SupportsGamePredicate supportsGame)
	{
		if (registry.containsKey(label))
			return false;
		
		registry.put(label, new AIRegistryEntry(label, dbID, supportsGame));
		return true;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Interface for a predicate that tests whether or not an AI supports a given game.
	 *
	 * @author Dennis Soemers
	 */
	public static interface SupportsGamePredicate
	{
		/**
		 * @param game
		 * @return True if and only if the given game is supported.
		 */
		public boolean supportsGame(final Game game);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * An entry in the AI registry
	 *
	 * @author Dennis Soemers
	 */
	public static class AIRegistryEntry
	{
		
		/** Label of the entry */
		private final String label;
		/** Database ID of the AI (only built-in Ludii agents can have a database ID >= 0) */
		private final int dbID;
		/** Predicate to test whether or not we support a given game */
		private final SupportsGamePredicate supportsGame;
		/** Used for sorting when we want sorted lists (in order of registration) */
		protected final int rank;
		
		/**
		 * Constructor.
		 * 
		 * NOTE: intentionally private. We want third-party users to go through the static
		 * registerAI() method.
		 * 
		 * @param label
		 * @param dbID
		 * @param supportsGame
		 */
		protected AIRegistryEntry(final String label, final int dbID, final SupportsGamePredicate supportsGame)
		{
			this.label = label;
			this.dbID = dbID;
			this.supportsGame = supportsGame;
			this.rank = nextRank++;
		}
		
		/**
		 * @return The AI's label
		 */
		public String label()
		{
			return label;
		}
		
		/**
		 * @return The AI's database ID (only >= 0 for Ludii built-in AIs)
		 */
		public int dbID()
		{
			return dbID;
		}
		
		/**
		 * @param game
		 * @return True if and only if we support the given game
		 */
		public boolean supportsGame(final Game game)
		{
			return supportsGame.supportsGame(game);
		}
		
	}
	
	//-------------------------------------------------------------------------

}
