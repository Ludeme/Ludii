package manager.utils;

import java.util.ArrayList;

import gnu.trove.map.hash.TObjectIntHashMap;
import main.Constants;
import main.collections.FastArrayList;
import main.options.UserSelections;
import manager.Manager;
import other.move.Move;

/**
 * Settings used by the Manager Module.
 *
 * @author Matthew.Stephenson and cambolbro
 */
public final class SettingsManager
{
	//-------------------------------------------------------------------------
	// User settings
	
	/** Visualize repetition moves */
	private boolean showRepetitions = false;
	
	/** Whether or not the agent(s) are paused. */
	private boolean agentsPaused = true;
	
	/** The time of a tick in simulation. */
	private double tickLength = 0.1;
	
	private boolean alwaysAutoPass = false;
	
	private double minimumAgentThinkTime = 0.5;
	
	//-------------------------------------------------------------------------
	// Variables used for displaying repeated moves.
	
	private ArrayList<Long> storedGameStatesForVisuals = new ArrayList<>();
	
	private FastArrayList<Move> movesAllowedWithRepetition = new FastArrayList<>();
	
	//-------------------------------------------------------------------------
	// Variables used for multiple consequence selection.
	
	/** List of possible consequence moves. */
	private ArrayList<Move> possibleConsequenceMoves = new ArrayList<>();
	
	//-------------------------------------------------------------------------
	// Variables used for turn limits.
	
	private TObjectIntHashMap<String> turnLimits = new TObjectIntHashMap<String>();
	
	//-------------------------------------------------------------------------
	
	/** 
	 * User selections for options and rulesets within a game.
	 * When compiling a game, pass in a "dirty" copy of this object if you
	 * want the settings to be set to the game's defaults, otherwise the 
	 * game will be compiled with the settings specified here. 
	 */
	private final UserSelections userSelections = new UserSelections(new ArrayList<String>());
	
	//-------------------------------------------------------------------------
	// Getter and setters

	public boolean showRepetitions()
	{
		return showRepetitions;
	}

	public void setShowRepetitions(final boolean show)
	{
		showRepetitions = show;
	}

	public double tickLength()
	{
		return tickLength;
	}

	public void setTickLength(final double length)
	{
		tickLength = length;
	}

	public ArrayList<Long> storedGameStatesForVisuals()
	{
		return storedGameStatesForVisuals;
	}

	public void setStoredGameStatesForVisuals(final ArrayList<Long> stored)
	{
		storedGameStatesForVisuals = stored;
	}

	public FastArrayList<Move> movesAllowedWithRepetition()
	{
		return movesAllowedWithRepetition;
	}

	public void setMovesAllowedWithRepetition(final FastArrayList<Move> moves)
	{
		movesAllowedWithRepetition = moves;
	}

	public ArrayList<Move> possibleConsequenceMoves()
	{
		return possibleConsequenceMoves;
	}

	public void setPossibleConsequenceMoves(final ArrayList<Move> possible)
	{
		possibleConsequenceMoves = possible;
	}

	public int turnLimit(final String gameName)
	{
		if (turnLimits.contains(gameName))
			return turnLimits.get(gameName);
		
		return Constants.DEFAULT_TURN_LIMIT;
	}
	
	public void setTurnLimit(final String gameName, final int turnLimit)
	{
		turnLimits.put(gameName, turnLimit);
	}
	
	public TObjectIntHashMap<String> turnLimits()
	{
		return turnLimits;
	}

	public void setTurnLimits(final TObjectIntHashMap<String> turnLimits)
	{
		this.turnLimits = turnLimits;
	}

	public boolean agentsPaused() 
	{
		return agentsPaused;
	}

	public void setAgentsPaused(final Manager manager, final boolean paused) 
	{
		agentsPaused = paused;
		
		if (agentsPaused)
			manager.ref().interruptAI(manager);
	}
	
	public UserSelections userSelections()
	{
		return userSelections;
	}

	public boolean alwaysAutoPass() 
	{
		return alwaysAutoPass;
	}

	public void setAlwaysAutoPass(final boolean alwaysAutoPass) 
	{
		this.alwaysAutoPass = alwaysAutoPass;
	}

	public double minimumAgentThinkTime()
	{
		return minimumAgentThinkTime;
	}

	public void setMinimumAgentThinkTime(double minimumAgentThinkTime)
	{
		this.minimumAgentThinkTime = minimumAgentThinkTime;
	}

}
