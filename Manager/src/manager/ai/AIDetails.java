package manager.ai;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import manager.Manager;
import other.AI;
import utils.AIFactory;

/**
 * Object for storing all GUI-relevant details about an particular player/AI.
 * 
 * @author Matthew.Stephenson
 */
public class AIDetails
{
	/** AI JSONObject */
	private JSONObject object;
	
	/** AI for controlling this player (can be Human) */
	private AI aI;
	
	/** Thinking time for this AI */
	private double thinkTime;
	
	/** Name of this AI/player */
	private String name;
	
	/** Menu Name for this AI/player (used for the player panel) */
	private String menuItemName;
	
	//-------------------------------------------------------------------------

	public AIDetails(final Manager manager, final JSONObject object, final int playerId, final String menuItemName)
	{
		this.object = object;
		
		if (object != null)
		{
			final JSONObject aiObj = object.getJSONObject("AI");
			final String algName = aiObj.getString("algorithm");
			if (!algName.equalsIgnoreCase("Human"))
			{
				AI aiFromName = AIFactory.fromJson(object);		
				
				if (manager.ref().context() != null && manager.isWebApp() && aiFromName.usesFeatures(manager.ref().context().game()))
				{
					final JSONObject json = new JSONObject()
							.put("AI", new JSONObject()
							.put("algorithm", "AlphaBeta")
							);
					aiFromName = AIFactory.fromJson(json);
				}
				
				setAI(aiFromName);
			}	
		}
		else
		{
			this.object = 	new JSONObject().put
							(
								"AI", new JSONObject().put("algorithm", "Human")
							);
		}
		
		try
		{
			name = manager.aiSelected()[playerId].name();
		}
		catch (final Exception e)
		{
			name = "Player " + playerId;
		}
		
		try
		{
			thinkTime = manager.aiSelected()[playerId].thinkTime();
		}
		catch (final Exception e)
		{
			thinkTime = 1.0;
		}
		
		this.menuItemName = menuItemName;
			
	}
	
	//-------------------------------------------------------------------------

	public String name()
	{
		return name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}
	
	public String menuItemName()
	{
		return menuItemName;
	}
	
	public void setMenuItemName(final String menuItemName)
	{
		this.menuItemName = menuItemName;
	}

	public JSONObject object()
	{
		return object;
	}
	
	public AI ai()
	{
		// AI might have been set to null during game compilation, in which case it needs to be recreated.
		if (aI == null && object != null)
		{
			final JSONObject aiObj = object.getJSONObject("AI");
			final String algName = aiObj.getString("algorithm");
			if (!algName.equalsIgnoreCase("Human"))
			{
				setAI(AIFactory.fromJson(object));
			}	
		}
		
		return aI;
	}

	public double thinkTime()
	{
		return thinkTime;
	}

	public void setThinkTime(final double thinkTime)
	{
		this.thinkTime = thinkTime;
	}
	
	//-------------------------------------------------------------------------
	
	public static AIDetails getCopyOf(final Manager manager, final AIDetails oldAIDetails, final int playerId)
	{
		if (oldAIDetails == null)
			return new AIDetails(manager, null, playerId, "Human");
		
		final AIDetails newAIDetails = new AIDetails(manager, oldAIDetails.object(), playerId, oldAIDetails.menuItemName);
		newAIDetails.setName(oldAIDetails.name());
		newAIDetails.setThinkTime(oldAIDetails.thinkTime());
		newAIDetails.setName(oldAIDetails.name());
		return newAIDetails;
	}
	
	//-------------------------------------------------------------------------
	
	public static List<AI> convertToAIList(final AIDetails[] details)
	{
		final List<AI> aiList = new ArrayList<>();
		for (final AIDetails detail : details)
			aiList.add(detail.ai());

		return aiList;
	}
	
	public static double[] convertToThinkTimeArray(final AIDetails[] details)
	{
		final double[] timeArray = new double[details.length];
		for (int i = 0; i < details.length; i++)
			timeArray[i] = details[i].thinkTime();
		
		return timeArray;
	}
	
	//-------------------------------------------------------------------------
	
	public boolean equals(final AIDetails aiDetails)
	{
		if (!aiDetails.object.equals(object))
			return false;
		if (!aiDetails.name.equals(name))
			return false;
		if (!aiDetails.menuItemName.equals(menuItemName))
			return false;
		
		return true;
	}

	//-------------------------------------------------------------------------
	
	public void setAI(final AI aI) 
	{
		this.aI = aI;
	}
	
	//-------------------------------------------------------------------------
	
}
