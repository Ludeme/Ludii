package manager.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

import game.Game;
import other.AI;
import search.flat.FlatMonteCarlo;
import search.mcts.MCTS;
import search.minimax.AlphaBetaSearch;
import search.minimax.BRSPlus;
import utils.AIFactory;
import utils.AIFactory.AIConstructor;
import utils.RandomAI;

/**
 * A registry of AIs that can be instantiated in the GUI of Ludii.
 *
 * @author Dennis Soemers, Eric.Piette
 */
public class AIRegistry
{
	
	//-------------------------------------------------------------------------
	
	/** Our registry */
	protected static Map<String, AIRegistryEntry> registry = new HashMap<String, AIRegistryEntry>();
	
	/** Rank to assign to next registered AI (used for sorting when we want a sorted list of AIs) */
	protected static volatile int nextRank = 0;
	
	static
	{
		// Static block to register our built-in AIs
		registerAI("Human", -1, (game) -> {return false;}, null);	// We have special handling for human in dropdown menus
		registerAI("Ludii AI", -1, (game) -> {return true;}, null);
		registerAI("Random", 1, (game) -> {return new RandomAI().supportsGame(game);}, null);
		registerAI("Flat MC", 2, (game) -> {return new FlatMonteCarlo().supportsGame(game);}, null);
		registerAI("UCT", 3, (game) -> {return MCTS.createUCT().supportsGame(game);}, null);
		registerAI("UCT (Uncapped)", 4, (game) -> {return MCTS.createUCT().supportsGame(game);}, null);
		registerAI("MC-GRAVE", 5, (game) -> {return AIFactory.createAI("MC-GRAVE").supportsGame(game);}, null);
		registerAI("Progressive History", 6, (game) -> {return AIFactory.createAI("Progressive History").supportsGame(game);}, null);
		registerAI("MAST", 7, (game) -> {return AIFactory.createAI("MAST").supportsGame(game);}, null);
		registerAI("Biased MCTS", 8, (game) -> {return MCTS.createBiasedMCTS(0.0).supportsGame(game);}, null);
		registerAI("MCTS (Biased Selection)", 9, (game) -> {return MCTS.createBiasedMCTS(1.0).supportsGame(game);}, null);
		registerAI("Alpha-Beta", 10, (game) -> {return AlphaBetaSearch.createAlphaBeta().supportsGame(game);}, null);
		registerAI("BRS+", 11, (game) -> {return new BRSPlus().supportsGame(game);}, null);
		registerAI("MCTS (Hybrid Selection)", 12, (game) -> {return MCTS.createHybridMCTS().supportsGame(game);}, null);
		registerAI("Bandit Tree Search", 13, (game) -> {return MCTS.createBanditTreeSearch().supportsGame(game);}, null);
		registerAI("NST", 14, (game) -> {return AIFactory.createAI("NST").supportsGame(game);}, null);
		registerAI("UCB1Tuned", 15, (game) -> {return AIFactory.createAI("UCB1Tuned").supportsGame(game);}, null);
		registerAI("Progressive Bias", 16, (game) -> {return AIFactory.createAI("Progressive Bias").supportsGame(game);}, null);
		registerAI("EPT", 17, (game) -> {return AIFactory.createAI("EPT").supportsGame(game);}, null);
		registerAI("EPT-QB", 18, (game) -> {return AIFactory.createAI("EPT-QB").supportsGame(game);}, null);
		registerAI("Score Bounded MCTS", 19, (game) -> {return AIFactory.createAI("Score Bounded MCTS").supportsGame(game);}, null);
		registerAI("Heuristic Sampling", 20, (game) -> {return AIFactory.createAI("Heuristic Sampling").supportsGame(game);}, null);
		registerAI("One-Ply (No Heuristic)", 21, (game) -> {return AIFactory.createAI("One-Ply (No Heuristic)").supportsGame(game);}, null);
		//registerAI("Bob the Basic AI", 22, (game) -> {return true;}, null);
		registerAI("UBFM", 23, (game) -> {return AIFactory.createAI("UBFM").supportsGame(game);}, null);
		registerAI("Hybrid UBFM", 24, (game) -> {return AIFactory.createAI("Hybrid UBFM").supportsGame(game);}, null);
		registerAI("Biased UBFM", 25, (game) -> {return AIFactory.createAI("Biased UBFM").supportsGame(game);}, null);
		registerAI("Lazy UBFM", 26, (game) -> {return AIFactory.createAI("Lazy UBFM").supportsGame(game);}, null);
		registerAI("UCB1-GRAVE", 27, (game) -> {return AIFactory.createAI("UCB1-GRAVE").supportsGame(game);}, null);
		registerAI("From JAR", -1, (game) -> {return false;}, null);	// We have special handling for From JAR in dropdown menus
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Registers a new AI. NOTE: this method does not provide a predicate to test
	 * whether or not any given game is supported, so we assume that ANY game is 
	 * supported!
	 * 
	 * @param label
	 * @param aiConstructor Functor to use for constructing AIs
	 * @return True if we successfully registered an AI, false if an AI with 
	 * 	the same label was already registered.
	 */
	public static boolean registerAI(final String label, final AIConstructor aiConstructor)
	{
		return registerAI(label, -1, (game) -> {return true;}, aiConstructor);
	}
	
	/**
	 * Registers a new AI.
	 * 
	 * @param label
	 * @param aiConstructor Functor to use for constructing AIs
	 * @param supportsGame Predicate to test whether or not any given game is supported.
	 * @return True if we successfully registered an AI, false if an AI with 
	 * 	the same label was already registered.
	 */
	public static boolean registerAI
	(
		final String label, final AIConstructor aiConstructor, final SupportsGamePredicate supportsGame
	)
	{
		return registerAI(label, -1, supportsGame, aiConstructor);
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
	
	/**
	 * Updates the given JSON object to properly handle registered third-party AIs
	 * @param json
	 */
	public static void processJson(final JSONObject json)
	{
		if (json == null || json.getJSONObject("AI") == null)
			return;
		
		final AIRegistryEntry entry = registry.get(json.getJSONObject("AI").getString("algorithm"));
		if (entry != null)
		{
			final AIConstructor constructor = entry.aiConstructor();
			if (constructor != null)
				json.put("constructor", constructor);
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Registers a new AI
	 * @param label
	 * @param dbID
	 * @param supportsGame
	 * @param aiConstructor
	 * @return True if we successfully registered an AI, false if an AI with 
	 * 	the same label was already registered.
	 */
	private static boolean registerAI
	(
		final String label, final int dbID, 
		final SupportsGamePredicate supportsGame, final AIConstructor aiConstructor
	)
	{
		if (registry.containsKey(label))
			return false;
		
		registry.put(label, new AIRegistryEntry(label, dbID, supportsGame, aiConstructor));
		return true;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param agentName The name of the agent.
	 * @return The AI object from its name.
	 */
	public static AI fromRegistry
	(
		final String agentName
	)
	{
		final JSONObject json = new JSONObject();
		final JSONObject aiJson = new JSONObject();
		aiJson.put("algorithm", agentName);
		json.put("AI", aiJson);
		AIRegistry.processJson(json);
		return AIFactory.fromJson(json);
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
		/** Functor to construct AIs. If null, we'll use the AI factory to construct based on just name */
		private final AIConstructor aiConstructor;
		/** Used for sorting when we want sorted lists (in order of registration) */
		protected final int rank;
		
		/**
		 * Constructor.
		 * 
		 * NOTE: intentionally protected. We want third-party users to go through the static
		 * registerAI() method.
		 * 
		 * @param label
		 * @param dbID
		 * @param supportsGame
		 */
		protected AIRegistryEntry
		(
			final String label, final int dbID, 
			final SupportsGamePredicate supportsGame, final AIConstructor aiConstructor
		)
		{
			this.label = label;
			this.dbID = dbID;
			this.supportsGame = supportsGame;
			this.aiConstructor = aiConstructor;
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
		 * @return Functor to use to construct AIs. If this returns null, should instead
		 * 	construct AIs just from name.
		 */
		public AIConstructor aiConstructor()
		{
			return aiConstructor;
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
