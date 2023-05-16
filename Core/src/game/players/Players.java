package game.players;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exception.LimitPlayerException;
import game.Game;
import main.Constants;
import other.BaseLudeme;

/**
 * Defines the players of the game.
 * 
 * @author cambolbro and Eric.Piette
 */
public final class Players extends BaseLudeme implements Serializable
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Player records: 0 is empty, then player indices are 1..P. */
	protected final List<Player> players = new ArrayList<Player>();

	//-------------------------------------------------------------------------

	/**
	 * To define a set of many players with specific data for each.
	 * 
	 * @param players The list of players.
	 * @example (players {(player N) (player S)})
	 */
	public Players
	(
		 final Player[] players
	)
	{
		this.players.add(null);  // pad slot 0 to null player
		if (players != null)
			for (int p = 0; p < players.length; p++)
			{
				final Player player = players[p];
				if(player.name() == null)
					player.setName("Player "  + (p+1));
				player.setIndex(p+1);
				player.setDefaultColour();				
				player.setEnemies(players.length);
				this.players.add(player);
			}
		
		if (this.players.size() > Constants.MAX_PLAYERS + 1)
			throw new LimitPlayerException(this.players.size());
	}

	/**
	 * To define a set of many players with the same data for each.
	 * 
	 * @param numPlayers The number of players.
	 * 
	 * @example (players 2)
	 */
	public Players
	(
		final Integer numPlayers
	)
	{
		players.add(null);  // pad slot 0 to null player
		if (players != null)
			for (int p = 0; p < numPlayers.intValue(); p++)
			{
				final Player player = new Player(null);
						
				if (player.name() == null)
					player.setName("Player "  + (p+1));
				
				player.setIndex(p+1);
				player.setDefaultColour();				
				player.setEnemies(numPlayers.intValue());
				players.add(player);
			}
		
		if (players.size() > Constants.MAX_PLAYERS + 1)
			throw new LimitPlayerException(players.size());

		if (numPlayers.intValue() < 0)
			throw new LimitPlayerException(numPlayers.intValue());
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Number of players.
	 */
	public int count()
	{
		return players.size() - 1;
	}

	/**
	 * @return Number of player slots including null player.
	 */
	public int size()
	{
		return players.size();
	}

	/**
	 * @return Player records: 0 is empty, then player indices are 1..P.
	 */
	public List<Player> players()
	{
		return Collections.unmodifiableList(players);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		for (int i = 1; i < players.size(); i++)
		{
			final Player player = players.get(i);
			concepts.or(player.concepts(game));
		}
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		for (int i = 1; i < players.size(); i++)
		{
			final Player player = players.get(i);
			writeEvalContext.or(player.writesEvalContextRecursive());
		}
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		for (int i = 1; i < players.size(); i++)
		{
			final Player player = players.get(i);
			readEvalContext.or(player.readsEvalContextRecursive());
		}
		return readEvalContext;
	}

	/**
	 * Called once after a game object has been created. Allows for any game-
	 * specific preprocessing (e.g. precomputing and caching of static results).
	 * 
	 * @param game
	 */
	public void preprocess(final Game game)
	{
		for (int i = 1; i < players.size(); i++)
		{
			final Player player = players.get(i);
			player.preprocess(game);
		}
	}

	/**
	 * @param game The game.
	 * @return Accumulated flags for this state type.
	 */
	public long gameFlags(final Game game)
	{
		long gameFlags = 0l;
		for (int i = 1; i < players.size(); i++)
		{
			//final Player player = players.get(i);
			gameFlags |= Player.gameFlags(game);
		}
		return gameFlags;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		for (int i = 1; i < players.size(); i++)
		{
			final Player player = players.get(i);
			missingRequirement |= player.missingRequirement(game);
		}
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		for (int i = 1; i < players.size(); i++)
		{
			final Player player = players.get(i);
			willCrash |= player.willCrash(game);
		}
		return willCrash;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		if(count() == 0)
			return "";

		final Pattern p = Pattern.compile("Player \\d+");
        Matcher m = null;
        String playerName = null;

        boolean allMatch = false;

		String text = "";
		for(int i = 1; i < players.size(); i++) 
		{
			playerName = players.get(i).name();
			m = p.matcher(playerName);
			
			// Does the player have a specific name?
			final boolean match = m.matches();
			if(i == 1)
				allMatch = match;
			else if(allMatch ^ match)
				throw new RuntimeException("We assume that every player has a unique name or no one has one!");

			if(!match) 
			{
				if(!text.isEmpty())
					text += i == players.size() - 1 ? " and " : ", ";
				text += playerName;
			}
		}

		return text;
	}
	
	//-------------------------------------------------------------------------
	
}
