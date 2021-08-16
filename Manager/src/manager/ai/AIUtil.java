package manager.ai;

import java.awt.EventQueue;
import java.util.ArrayList;

import org.json.JSONObject;

import game.Game;
import manager.Manager;
import other.AI;
import other.context.Context;
import other.model.SimultaneousMove;
import search.flat.FlatMonteCarlo;
import search.mcts.MCTS;
import search.mcts.finalmoveselection.RobustChild;
import search.mcts.playout.RandomPlayout;
import search.mcts.selection.McGRAVE;
import search.minimax.AlphaBetaSearch;
import search.minimax.BRSPlus;
import utils.AIFactory;
import utils.AIUtils;
import utils.RandomAI;

/**
 * Functions for handling AI agents.
 * 
 * @author Matthew.Stephenson
 */
public class AIUtil
{
	/** 
	 * Cycles all players backwards by one.
	 */
	public static void cycleAgents(final Manager manager)
	{
		manager.settingsManager().setAgentsPaused(manager, true);
		
		final AIDetails player1Details = AIDetails.getCopyOf(manager, manager.aiSelected()[1], 1);
		for (int i = 2; i <= manager.ref().context().game().players().count(); i++)
			manager.aiSelected()[i-1] = AIDetails.getCopyOf(manager, manager.aiSelected()[i], i);

		manager.aiSelected()[manager.ref().context().game().players().count()] = player1Details;
		
		manager.settingsNetwork().backupAiPlayers(manager);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Update the selected AI agents for the given player numbers.
	 */
	public static void updateSelectedAI(final Manager manager, final JSONObject inJSON, final int playerNum, final AIMenuName aImenuName)
	{
		AIMenuName menuName = aImenuName;
		JSONObject json = inJSON;
		final JSONObject aiObj = json.getJSONObject("AI");
		final String algName = aiObj.getString("algorithm");

		if (algName.equals("Human"))
		{
			// First close previous AI if it exists
			if (manager.aiSelected()[playerNum].ai() != null)
				manager.aiSelected()[playerNum].ai().closeAI();
			
			manager.aiSelected()[playerNum] = new AIDetails(manager, null, playerNum, AIMenuName.Human);
			return;
		}
		else if (algName.equals("From JAR"))
		{
			if (!aiObj.has("JAR File") || !aiObj.has("Class Name"))
			{
				json = manager.getPlayerInterface().getNameFromJar();
				if (json == null)
					return;
				menuName = AIMenuName.FromJAR;
			}
		}

		// First close previous AI if it exists
		if (manager.aiSelected()[playerNum].ai() != null)
			manager.aiSelected()[playerNum].ai().closeAI();
		
		manager.aiSelected()[playerNum] = new AIDetails(manager, json, playerNum, menuName);
		
		manager.settingsNetwork().backupAiPlayers(manager);
		pauseAgentsIfNeeded(manager);
		
		return;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Pauses all agents if required.
	 * Should be called any time the game is restarted/loaded, or an AI is selected.
	 */
	public static void pauseAgentsIfNeeded(final Manager manager)
	{
		if (manager.settingsNetwork().getActiveGameId() != 0 && !manager.settingsNetwork().getOnlineAIAllowed())
			manager.settingsManager().setAgentsPaused(manager, true);
		else if (manager.aiSelected()[manager.ref().context().state().mover()].ai() != null)
			manager.settingsManager().setAgentsPaused(manager, true);
		else if (manager.ref().context().model() instanceof SimultaneousMove)
			manager.settingsManager().setAgentsPaused(manager, true);
		else if (manager.ref().context().game().players().count() == 0)
			manager.settingsManager().setAgentsPaused(manager, true);
		else
			manager.settingsManager().setAgentsPaused(manager, false);
			
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Checks if any of the currently selected AI are not supported by the current game.
	 * @param context
	 */
	public static void checkAISupported(final Manager manager, final Context context)
	{
		// Make sure all AIs are initialised.
		for (int p = 1; p < manager.aiSelected().length; ++p)
		{
			if (manager.aiSelected()[p].ai() == null)
				continue;

			if (!manager.aiSelected()[p].ai().supportsGame(context.game()))
			{
				final AI oldAI = manager.aiSelected()[p].ai();
				final AI newAI = AIUtils.defaultAiForGame(context.game());

				final JSONObject json = new JSONObject()
						.put("AI", new JSONObject()
						.put("algorithm", newAI.friendlyName())
						);
				
				manager.aiSelected()[p] = new AIDetails(manager, json, p, AIMenuName.LudiiAI);

				EventQueue.invokeLater(() -> 
				{
					manager.getPlayerInterface().addTextToStatusPanel(oldAI.friendlyName() + " does not support this game. Switching to default AI for this game: " + newAI.friendlyName() + ".\n");
				});
			}

			if (p <= context.game().players().count())
				manager.aiSelected()[p].ai().initIfNeeded(context.game(), p);
		}
		manager.settingsNetwork().backupAiPlayers(manager);
	}
	
	//-------------------------------------------------------------------------
	
	public static ArrayList<String> allValidAgentNames(final Game game)
	{
		final ArrayList<String> aiStrings = new ArrayList<>();
		
		aiStrings.add(AIMenuName.LudiiAI.label());

		if (new RandomAI().supportsGame(game))
			aiStrings.add(AIMenuName.Random.label());

		if (new FlatMonteCarlo().supportsGame(game))
			aiStrings.add(AIMenuName.FlatMC.label());

		if (MCTS.createUCT().supportsGame(game))
		{
			aiStrings.add(AIMenuName.UCT.label());	
			aiStrings.add(AIMenuName.UCTUncapped.label());
		}

		if (new MCTS(new McGRAVE(), new RandomPlayout(200), new RobustChild()).supportsGame(game))
			aiStrings.add(AIMenuName.MCGRAVE.label());
		
		if (AIFactory.createAI("Progressive History").supportsGame(game))
			aiStrings.add(AIMenuName.ProgressiveHistory.label());
		
		if (AIFactory.createAI("MAST").supportsGame(game))
			aiStrings.add(AIMenuName.MAST.label());

		if (MCTS.createBiasedMCTS(0.0).supportsGame(game))
		{
			aiStrings.add(AIMenuName.BiasedMCTS.label());
			aiStrings.add(AIMenuName.BiasedMCTSUniformPlayouts.label());
		}
		
		if (MCTS.createHybridMCTS().supportsGame(game))
			aiStrings.add(AIMenuName.HybridMCTS.label());
		
		if (MCTS.createBanditTreeSearchAvg().supportsGame(game))
			aiStrings.add(AIMenuName.BanditTreeSearchAvg.label());
		
		if (MCTS.createBanditTreeSearchMinMax().supportsGame(game))
			aiStrings.add(AIMenuName.BanditTreeSearchMinMax.label());
		
		if (MCTS.createBanditTreeSearchSumAvgMinMax().supportsGame(game))
			aiStrings.add(AIMenuName.BanditTreeSearchSumAvgMinMax.label());

		if (AlphaBetaSearch.createAlphaBeta().supportsGame(game))
			aiStrings.add(AIMenuName.AlphaBeta.label());
		
		if (new BRSPlus().supportsGame(game))
			aiStrings.add(AIMenuName.BRSPlus.label());
		
		return aiStrings;
	}
	
}
