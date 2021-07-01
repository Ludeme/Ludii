package manager;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.rng.core.RandomProviderDefaultState;

import main.Constants;
import manager.ai.AIDetails;
import manager.network.DatabaseFunctionsPublic;
import manager.network.SettingsNetwork;
import manager.utils.SettingsManager;
import other.AI;
import other.move.Move;
import tournament.Tournament;

/**
 * The Manager class provides the link between the logic (Core/Referee) and the playerDesktop.
 * I handles all aspects of Ludii that are not specific to the PC environment, e.g. Graphics2D.
 *
 * @author Matthew.Stephenson and cambolbro and Eric.Piette
 */
public final class Manager 
{
	private PlayerInterface playerInterface;
	
	private final DatabaseFunctionsPublic databaseFunctionsPublic = DatabaseFunctionsPublic.construct();
	
	/** Referee object that controls play. */
	private final Referee ref;
	
	/** Selects AI, based on player's choices in the Settings menu. */
	private final AIDetails[] aiSelected = new AIDetails[Constants.MAX_PLAYERS + 1];
	
	/** Our current tournament */
	private Tournament tournament;
	
	/** Internal state of Context's RNG at the beginning of the game currently in the App. */
	private RandomProviderDefaultState currGameStartRngState = null;
	
	/** References to AIs for which we're visualising what they're thinking live. */
	private List<AI> liveAIs = null;
	
	/** lud filename for the last loaded game. */
	private String savedLudName;
	
	/** list of the undoneMoves when viewing previous game states. */
	private List<Move> undoneMoves = new ArrayList<>();
	
	private final SettingsManager settingsManager = new SettingsManager();
	private final SettingsNetwork settingsNetwork = new SettingsNetwork();
	
	//-------------------------------------------------------------------------
	
	public Manager(final PlayerInterface playerInterface)
	{
		setPlayerInterface(playerInterface);
		ref = new Referee();
	}
	
	//-------------------------------------------------------------------------

	public Referee ref()
	{
		return ref;
	}

	public AIDetails[] aiSelected() 
	{
		return aiSelected;
	}

	public Tournament tournament() 
	{
		return tournament;
	}
	
	//-------------------------------------------------------------------------
	
	public void updateCurrentGameRngInternalState()
	{
		setCurrGameStartRngState((RandomProviderDefaultState) ref().context().rng().saveState());
	}
	
	public RandomProviderDefaultState currGameStartRngState()
	{
		return currGameStartRngState;
	}

	public void setCurrGameStartRngState(final RandomProviderDefaultState newCurrGameStartRngState)
	{
		currGameStartRngState = newCurrGameStartRngState;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The AIs for which we're visualising the thought process live
	 */
	public List<AI> liveAIs()
	{
		return liveAIs;
	}

	/**
	 * Sets the AIs for which we're visualising the thought process live
	 *
	 * @param ais
	 */
	public void setLiveAIs(final List<AI> ais)
	{
		liveAIs = ais;
	}
	
	//-------------------------------------------------------------------------
	
	public String savedLudName()
	{
		return savedLudName;
	}

	public void setSavedLudName(final String savedLudName)
	{
		this.savedLudName = savedLudName;
	}
	
	//-------------------------------------------------------------------------

	public void setUndoneMoves(final List<Move> moves)
	{
		undoneMoves = moves;
	}

	public List<Move> undoneMoves()
	{
		return undoneMoves;
	}
	
	//-------------------------------------------------------------------------

	public SettingsManager settingsManager() 
	{
		return settingsManager;
	}

	//-------------------------------------------------------------------------

	public SettingsNetwork settingsNetwork() 
	{
		return settingsNetwork;
	}

	//-------------------------------------------------------------------------

	public PlayerInterface getPlayerInterface()
	{
		return playerInterface;
	}

	public void setPlayerInterface(final PlayerInterface playerInterface)
	{
		this.playerInterface = playerInterface;
	}

	//-------------------------------------------------------------------------

	public Tournament getTournament()
	{
		return tournament;
	}

	public void setTournament(final Tournament tournament)
	{
		this.tournament = tournament;
	}

	//-------------------------------------------------------------------------
	
	public DatabaseFunctionsPublic databaseFunctionsPublic() 
	{
		return databaseFunctionsPublic;
	}
	
	//-------------------------------------------------------------------------

}
