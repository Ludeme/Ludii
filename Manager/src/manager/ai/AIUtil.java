package manager.ai;

import java.awt.EventQueue;

import org.json.JSONObject;

import manager.Manager;
import other.AI;
import other.context.Context;
import other.model.SimultaneousMove;
import utils.AIRegistry;
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
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Update the selected AI agents for the given player numbers.
	 */
	public static void updateSelectedAI(final Manager manager, final JSONObject inJSON, final int playerNum, final String aiMenuName)
	{
		String menuName = aiMenuName;
		JSONObject json = inJSON;
		final JSONObject aiObj = json.getJSONObject("AI");
		final String algName = aiObj.getString("algorithm");

		if (algName.equals("Human"))
		{
			// First close previous AI if it exists
			if (manager.aiSelected()[playerNum].ai() != null)
				manager.aiSelected()[playerNum].ai().closeAI();
			
			manager.aiSelected()[playerNum] = new AIDetails(manager, null, playerNum, "Human");
			return;
		}
		else if (algName.equals("From JAR"))
		{
			if (!aiObj.has("JAR File") || !aiObj.has("Class Name"))
			{
				json = manager.getPlayerInterface().getNameFromJar();
				if (json == null)
					return;
				menuName = "From JAR";
			}
		}
		else if (algName.equals("From JSON"))
		{
			if (!aiObj.has("JSON File") || !aiObj.has("Class Name"))
			{
				json = manager.getPlayerInterface().getNameFromJson();
				if (json == null)
					return;
				menuName = "From JSON";
			}
		}
		else if (algName.equals("From AI.DEF"))
		{
			if (!aiObj.has("AI.DEF File") || !aiObj.has("Class Name"))
			{
				json = manager.getPlayerInterface().getNameFromAiDef();
				if (json == null)
					return;
				menuName = "From AI.DEF";
			}
		}
		else
		{
			AIRegistry.processJson(json);
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
		else if (manager.aiSelected()[manager.moverToAgent()].ai() != null)
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
				
				manager.aiSelected()[p] = new AIDetails(manager, json, p, "Ludii AI");

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
	
	/**
	 * @param manager
	 * @return If any of the games players are being controlled by an AI.
	 */
	public static boolean anyAIPlayer(final Manager manager)
	{
		for (int i = 1; i <= manager.ref().context().game().players().count(); i++)
			if (manager.aiSelected()[i].ai() != null)
				return true;
		
		return false;
	}
	
	//-------------------------------------------------------------------------
	
}
