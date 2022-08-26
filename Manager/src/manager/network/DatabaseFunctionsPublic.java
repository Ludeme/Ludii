package manager.network;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.rng.core.RandomProviderDefaultState;

import manager.Manager;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;

/**
 * Public class for calling database functions on the Ludii Server.
 * Fake function calls. Database functionality is not available in the source code to prevent server spamming.
 * 
 * @author Matthew.Stephenson and Dennis Soemers
 */
@SuppressWarnings("static-method")
public class DatabaseFunctionsPublic 
{
	
	//-------------------------------------------------------------------------
	
	/** Class loader used to load private network code if available (not included in public source code repo) */
	private static URLClassLoader privateNetworkCodeClassLoader = null;
	
	// Static block to initialise classloader
	static 
	{
		final File networkPrivateBin = new File("../../LudiiPrivate/NetworkPrivate/bin");
		if (networkPrivateBin.exists())
		{
			try
			{
				privateNetworkCodeClassLoader = new URLClassLoader(new URL[]{networkPrivateBin.toURI().toURL()});
			} 
			catch (final MalformedURLException e)
			{
				// If this fails, that's fine, just means we don't have private code available
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Constructs a wrapper around the database functions.
	 */
	public static DatabaseFunctionsPublic construct()
	{
		// See if we can find private code first
		final ClassLoader classLoader = 
				privateNetworkCodeClassLoader != null ? privateNetworkCodeClassLoader : DatabaseFunctionsPublic.class.getClassLoader();
		try
		{
			final Class<?> privateClass = Class.forName("manager.network.privateFiles.DatabaseFunctionsPrivate", true, classLoader);

			if (privateClass != null)
			{
				// Found private network code, use its zero-args constructor
				return (DatabaseFunctionsPublic) privateClass.getConstructor().newInstance();
			}
		}
		catch (final Exception e)
		{
			// Nothing to do
		}
		
		// Failed to load the private class, so we're probably working just with public source code
		return new DatabaseFunctionsPublic();
	}

	//-------------------------------------------------------------------------
	// Analysis
	
	/**
	 * Gets all valid trials from the database for the provided parameters.
	 */
	public ArrayList<String> getTrialsFromDatabase(final String gameName, final List<String> gameOptions, final String agentName, final double thinkingTime, final int maxTurns, final int gameHash)
	{
		return new ArrayList<>();
	}
	
	/**
	 * Stores a trail in the database.
	 */
	public void storeTrialInDatabase(final String gameName, final List<String> gameOptions, final String agentName, final double thinkingTime, final int maxTurns, final int gameHash, final Trial trial, final RandomProviderDefaultState RNG)
	{
		// Do nothing.
	}
	
	/**
	 * Stores a website trail in the database.
	 */
	public void storeWebTrialInDatabase(final String gameName, final String rulesetName, final List<String> gameOptions, final int gameId, final int rulesetId, final boolean[] agents, final String username, final int gameHash, final Trial trial, final RandomProviderDefaultState RNG)
	{
		// Do nothing.
	}
	
	//-------------------------------------------------------------------------
	// Remote
	
	/**
	 * Begins repeating network actions that must be continuously performed while online.
	 */
	public void repeatNetworkActions(final Manager manager)
	{
		// Do nothing.
	}
	
	/**
	 * Provides an md5 encrypted hash string of a given password.
	 */
	public String md5(final String passwordToHash)
	{
		return "";
	}

	/**
	 * Refresh login flag on server, and make sure secret number up to date.
	 */
	public void refreshLogin(final Manager manager) 
	{
		// Do nothing.
	}
	
	/** 
	 * Sets the remaining time for each player. 
	 */
	public void checkRemainingTime(final Manager manager)
	{
		// Do nothing.
	}
	
	/**
	 * Get a String representation of the remaining time for all players.
	 */
	public String getRemainingTime(final Manager manager)
	{
		return "";
	}
	
	/**
	 * Get a String representation of all (offline or online) players.
	 */
	public String GetAllPlayers()
	{
		return "";
	}

	/**
	 * Get a String representation of all tournaments that we have joined.
	 */
	public String findJoinedTournaments(final Manager manager)
	{
		return "";
	}

	/** 
	 * Update the incoming messages chat with any new private messages. 
	 */
	public void updateIncomingMessages(final Manager manager)
	{
		// Do nothing.
	}

	/** 
	 * Update the last time the server was contacted. 
	 */
	public void updateLastServerTime(final Manager manager)
	{
		// Do nothing.
	}

	/**
	 * Get a String representation of all private games that we have not previously joined.
	 */
	public String findJoinableGames(final Manager manager)
	{
		return "";
	}

	/**
	 * Get a String representation of all games that we have previously joined.
	 */
	public String findJoinedGames(final Manager manager)
	{
		return "";
	}
	
	/**
	 * Get a String representation of all games that we can spectate.
	 */
	public String findOtherGames(final Manager manager)
	{
		return "";
	}
	
	/**
	 * Get a String representation of all private tournaments that we have not previously joined.
	 */
	public String findJoinableTournaments(final Manager manager)
	{
		return "";
	}
	
	/**
	 * Get a String representation of all tournaments that we have previously joined.
	 */
	public String findHostedTournaments(final Manager manager)
	{
		return "";
	}
	
	/**
	 * Sends a message to the group chat for the current game.
	 */
	public void sendGameChatMessage(final Manager manager, final String s)
	{
		// Do nothing.
	}
	
	/**
	 * Sends a specified move to the database.
	 */
	public void sendMoveToDatabase(final Manager manager, final Move m, final int nextMover, final String score, final int moveNumber)
	{
		// Do nothing.
	}
	
	/**
	 * Sends the database a message to say that the game is finished
	 */
	public void sendGameOverDatabase(final Manager manager)
	{
		// Do nothing.
	}
	
	/**
	 * Sends the database a message to say that you have forfeit the game.
	 */
	public void sendForfeitToDatabase(final Manager manager)
	{
		// Do nothing.
	}

	/**
	 * Sends the database a message to say that you have proposed a draw.
	 */
	public void sendProposeDraw(final Manager manager)
	{
		// Do nothing.
	}

	/**
	 * Checks if each of the current game players are online or offline.
	 */
	public void checkOnlinePlayers(final Manager manager)
	{
		// Do nothing.
	}

	/**
	 * Checks if there are any outstanding moves in the database that need to be carried out.
	 */
	public void getMoveFromDatabase(final Manager manager)
	{
		// Do nothing.	
	}

	/**
	 * Checks if there are any outstanding moves in the database that need to be carried out.
	 */
	public void checkStatesMatch(final Manager manager)
	{
		// Do nothing.
	}

	/**
	 * Checks if any players have forfeit or timed out.
	 */
	public void checkForfeitAndTimeoutAndDraw(final Manager manager)
	{
		// Do nothing.
	}

	/**
	 * Checks if any players have proposed a draw
	 */
	public void checkDrawProposed(final Manager manager)
	{
		// Do nothing.
	}

	/**
	 * Gets the username of each player in the game.
	 */
	public String[] getActiveGamePlayerNames(final Manager manager)
	{
		return new String[0];
	}

	/**
	 * Sends the database the current ranking of all played in the game.
	 */
	public void sendGameRankings(final Manager manager, final double[] rankingOriginal)
	{
		// Do nothing.
	}

	/**
	 * Converts an RandomProviderDefaultState object into a String representation.
	 */
	public String convertRNGToText(final RandomProviderDefaultState rngState) 
	{
		return "";
	}

	/**
	 * Gets the initial RNG seed for the game from the database.
	 */
	public String getRNG(final Manager manager)
	{
		return "";
	}

	/**
	 * Gets leaderboard information from the database (leaderboard across all games is used).
	 */
	public String getLeaderboard()
	{
		return "";
	}

	/**
	 * Ping the server to check if we are still connected to it.
	 */
	public boolean pingServer(final String URLName)
	{
		return false;
	}

	/**
	 * Send result to database and update each player's statistics
	 */
	public void sendResultToDatabase(final Manager manager, final Context context)
	{
		// Do nothing.
	}

	/**
	 * Log out of the server.
	 */
	public void logout(final Manager manager) 
	{
		// Do nothing.
	}

	/**
	 * Updates network player number if a swap action is made.
	 */
	public void checkNetworkSwap(final Manager manager, final Move move)
    {
		// Do nothing.
    }
	
	/**
	 * Location on server where remote scripts are stored.
	 */
	public String appFolderLocation()
	{
		return "";
	}
	
	/**
	 * Secret network number used for validating network actions.
	 */
	public double getSecretNetworkNumber(final Manager manager) 
	{
		return 0.0;
	}

}
