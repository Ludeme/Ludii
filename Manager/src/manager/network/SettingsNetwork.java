package manager.network;

import java.awt.Rectangle;
import java.util.Arrays;

import main.Constants;
import manager.Manager;
import manager.ai.AIDetails;

/**
 * Network settings.
 * 
 * @author Matthew.Stephenson
 */
public class SettingsNetwork 
{
	
	//-------------------------------------------------------------------------
	// Database function parameters	
	
	/** Thread for handling repeated network actions. */
	private Thread repeatNetworkActionsThread;
	
	/** 
	 * Number of refreshes where the local state doesn't match that stored in the DB. 
	 * If true for too long, a move may have been lost.
	 */
	private int localStateMatchesDB = 0;
	
	//-------------------------------------------------------------------------
	// Network game parameters	
	
	/** Player number in a network game, 0 if not in a network game. */
	private int networkPlayerNumber = 0;
	
	/** Number of other connected players in the current network game. */
	private int numberConnectedPlayers = 0;
	
	/** ID for the network game being played, 0 if not in a network game. */
	private int activeGameId = 0;
	
	/** ID for the tournament being played, 0 if not in a network game. */
	private int tournamentId = 0;
	
	/** Secret player network number, used for verifying player identity. */
	private int secretPlayerNetworkNumber = 0;
	
	/** If AI agents are allowed for the current network game. */
	private boolean onlineAIAllowed = false;
	
	/** Last time the server was contacted. */
	private int lastServerTime = -1;
	
	//-------------------------------------------------------------------------
	// Login settings
	/** ID of the user, 0 if not logged in. */
	private int loginId = 0;
	
	/** Username of the user, "" if not logged in. */
	private String loginUsername = "";
	
	/** If the user wants their username to be remembered. */
	private boolean rememberDetails = false;
	
	//-------------------------------------------------------------------------
	// Remote Dialog Settings
	
	/** Index of the remote dialog tab currently selected. */
	private int tabSelected = 0;
	
	/** Position of the remote dialog. */
	private Rectangle remoteDialogPosition;
	
	/** Backup of the AIDetails array stored in PlayerApp, for when we do online games. */
	private AIDetails[] onlineBackupAiPlayers = new AIDetails[Constants.MAX_PLAYERS+1];
	
	/** Total time remaining for each player. */
	private int[] playerTimeRemaining = new int[Constants.MAX_PLAYERS];
	
	/** If we are currently loading a network game. */
	private boolean loadingNetworkGame = false;
	
	//-------------------------------------------------------------------------
	// Network player parameters	
	
	/** The active players in the current Network game. */
	private boolean[] activePlayers = new boolean[Constants.MAX_PLAYERS+1];

	/** The players who are online in the current Network game. */
	private boolean[] onlinePlayers = new boolean[Constants.MAX_PLAYERS+1];
	
	/** The players who have proposed a draw this move. */
	private boolean[] drawProposedPlayers = new boolean[Constants.MAX_PLAYERS+1];
	
	//-------------------------------------------------------------------------
	// Other	
	
	/** If the network should be polled for moves less often, useful when slow Internet connection. */
	private boolean longerNetworkPolling = false;
	
	/** If the network should not be automatically refreshed, useful when slow Internet connection. */
	private boolean noNetworkRefresh = false;
	
	//-------------------------------------------------------------------------
	
	/** 
	 * Constructor.
	 */
	public SettingsNetwork()
	{
		resetNetworkPlayers();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Keep a backup of the AI players.
	 */
	public void backupAiPlayers(final Manager manager)
	{
		if (activeGameId == 0)
			for (int i = 0; i < manager.aiSelected().length; i++)
				onlineBackupAiPlayers()[i] = AIDetails.getCopyOf(manager, manager.aiSelected()[i], i);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Restore the AI players from the saved backup.
	 * Used if a player joins and then leaves an online game.
	 */
	public void restoreAiPlayers(final Manager manager)
	{
		for (int i = 0; i < onlineBackupAiPlayers().length; i++)
			manager.aiSelected()[i] = AIDetails.getCopyOf(manager, onlineBackupAiPlayers()[i], i);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Reset the network players, after a game is restarted.
	 */
	public void resetNetworkPlayers()
	{
		Arrays.fill(activePlayers(), true);
		Arrays.fill(onlinePlayers(), false);
		Arrays.fill(drawProposedPlayers(), false);
	}
	
	//-------------------------------------------------------------------------
	
	public int getNetworkPlayerNumber() 
	{
		return networkPlayerNumber;
	}
	
	public void setNetworkPlayerNumber(final int networkPlayerNumber) 
	{
		this.networkPlayerNumber = networkPlayerNumber;
	}
	
	public int getNumberConnectedPlayers() 
	{
		return numberConnectedPlayers;
	}
	
	public void setNumberConnectedPlayers(final int numberConnectedPlayers) 
	{
		this.numberConnectedPlayers = numberConnectedPlayers;
	}
	
	public int getLoginId() 
	{
		return loginId;
	}
	
	public void setLoginId(final int loginId) 
	{
		this.loginId = loginId;
	}
	
	public String loginUsername() 
	{
		return loginUsername;
	}
	
	public void setLoginUsername(final String loginUsername) 
	{
		this.loginUsername = loginUsername;
	}
	
	public boolean rememberDetails() 
	{
		return rememberDetails;
	}
	
	public void setRememberDetails(final boolean rememberDetails) 
	{
		this.rememberDetails = rememberDetails;
	}
	
	public int getActiveGameId() 
	{
		return activeGameId;
	}
	
	public void setActiveGameId(final int activeGameId) 
	{
		this.activeGameId = activeGameId;
	}
	
	public int tabSelected() 
	{
		return tabSelected;
	}
	
	public void setTabSelected(final int tabSelected) 
	{
		this.tabSelected = tabSelected;
	}
	
	public Rectangle remoteDialogPosition() 
	{
		return remoteDialogPosition;
	}
	
	public void setRemoteDialogPosition(final Rectangle remoteDialogPosition) 
	{
		this.remoteDialogPosition = remoteDialogPosition;
	}
	
	public int getTournamentId() 
	{
		return tournamentId;
	}
	
	public void setTournamentId(final int tournamentId) 
	{
		this.tournamentId = tournamentId;
	}
	
	public void setSecretNetworkNumber(final int secretNetworkNumber) 
	{
		setSecretPlayerNetworkNumber(secretNetworkNumber);
	}
	
	public boolean getOnlineAIAllowed() 
	{
		return onlineAIAllowed;
	}
	
	public void setOnlineAIAllowed(final boolean onlineAIAllowed) 
	{
		this.onlineAIAllowed = onlineAIAllowed;
	}

	public Thread repeatNetworkActionsThread() 
	{
		return repeatNetworkActionsThread;
	}

	public void setRepeatNetworkActionsThread(final Thread repeatNetworkActionsThread) 
	{
		this.repeatNetworkActionsThread = repeatNetworkActionsThread;
	}

	public int localStateMatchesDB() 
	{
		return localStateMatchesDB;
	}

	public void setLocalStateMatchesDB(final int localStateMatchesDB) 
	{
		this.localStateMatchesDB = localStateMatchesDB;
	}

	public AIDetails[] onlineBackupAiPlayers() 
	{
		return onlineBackupAiPlayers;
	}

	public void setOnlineBackupAiPlayers(final AIDetails[] onlineBackupAiPlayers) 
	{
		this.onlineBackupAiPlayers = onlineBackupAiPlayers;
	}

	public int[] playerTimeRemaining() 
	{
		return playerTimeRemaining;
	}

	public void setPlayerTimeRemaining(final int[] playerTimeRemaining) 
	{
		this.playerTimeRemaining = playerTimeRemaining;
	}

	public boolean loadingNetworkGame() 
	{
		return loadingNetworkGame;
	}

	public void setLoadingNetworkGame(final boolean loadingNetworkGame) 
	{
		this.loadingNetworkGame = loadingNetworkGame;
	}

	public boolean[] activePlayers() 
	{
		return activePlayers;
	}

	public void setActivePlayers(final boolean[] activePlayers) 
	{
		this.activePlayers = activePlayers;
	}

	public boolean[] onlinePlayers() 
	{
		return onlinePlayers;
	}

	public void setOnlinePlayers(final boolean[] onlinePlayers) 
	{
		this.onlinePlayers = onlinePlayers;
	}

	public boolean[] drawProposedPlayers() 
	{
		return drawProposedPlayers;
	}

	public void setDrawProposedPlayers(final boolean[] drawProposedPlayers) 
	{
		this.drawProposedPlayers = drawProposedPlayers;
	}
	
	public boolean longerNetworkPolling()
	{
		return longerNetworkPolling;
	}

	public void setLongerNetworkPolling(final boolean longer)
	{
		longerNetworkPolling = longer;
	}

	public boolean noNetworkRefresh()
	{
		return noNetworkRefresh;
	}

	public void setNoNetworkRefresh(final boolean no)
	{
		noNetworkRefresh = no;
	}

	public int lastServerTime() 
	{
		return lastServerTime;
	}

	public void setLastServerTime(final int lastServerTime) 
	{
		this.lastServerTime = lastServerTime;
	}

	public int secretPlayerNetworkNumber() 
	{
		return secretPlayerNetworkNumber;
	}

	public void setSecretPlayerNetworkNumber(final int secretPlayerNetworkNumber) 
	{
		this.secretPlayerNetworkNumber = secretPlayerNetworkNumber;
	}
	
	//-------------------------------------------------------------------------
}
