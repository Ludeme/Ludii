package manager.ai;

import java.awt.EventQueue;

import org.json.JSONObject;

import manager.Manager;
import other.AI;
import other.context.Context;
import other.model.SimultaneousMove;
import utils.AIUtils;

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
		manager.getPlayerInterface().updateTabs(manager.ref().context());
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
						.put("algorithm", newAI.friendlyName)
						);
				
				manager.aiSelected()[p] = new AIDetails(manager, json, p, AIMenuName.LudiiAI);

				EventQueue.invokeLater(() -> 
				{
					manager.getPlayerInterface().addTextToStatusPanel(oldAI.friendlyName + " does not support this game. Switching to default AI for this game: " + newAI.friendlyName + ".\n");
				});
			}

			if (p <= context.game().players().count())
				manager.aiSelected()[p].ai().initIfNeeded(context.game(), p);
		}
		manager.settingsNetwork().backupAiPlayers(manager);
	}
	
	//-------------------------------------------------------------------------
	
}
