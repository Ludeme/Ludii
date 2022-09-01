package game.players;

import java.awt.Color;
import java.io.Serializable;
import java.util.BitSet;

import game.Game;
import game.util.directions.DirectionFacing;
import gnu.trove.list.array.TIntArrayList;
import other.BaseLudeme;
import other.concept.Concept;

/**
 * A player of the game.
 * 
 * @author cambolbro and Eric.Piette
 * 
 * @remarks Defines a player with a specific name or direction.
 */
public final class Player extends BaseLudeme implements Serializable
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Player index, starting at 1. */
	private int index;

	/** Player name. */
	private String name;

	/** Player colour for UI. */
	private Color colour;

	/** Player colour for UI. */
	private final DirectionFacing direction;

	/** List of enemies. */
	private final TIntArrayList enemies = new TIntArrayList();
	
	//-------------------------------------------------------------------------

	/**
	 * For defining a player with a facing direction.
	 * 
	 * @param dirn The direction of the pieces of the player.
	 * 
	 * @example (player N)
	 */
	public Player(final DirectionFacing dirn)
	{
		this.direction = dirn;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Player index, starting at 1.
	 */
	public int index()
	{
		return index;
	}

	/**
	 * Set the index of the player.
	 * 
	 * @param id The index.
	 */
	public void setIndex(final int id)
	{
		index = id;
	}

	/**
	 * @return Player name.
	 */
	public String name()
	{
		return name;
	}

	/**
	 * To set the name of the player.
	 * 
	 * @param s
	 */
	public void setName(final String s)
	{
		name = s;
	}

	/**
	 * @return Player colour.
	 */
	public Color colour()
	{
		return colour;
	}

	/**
	 * @return Player direction.
	 */
	public DirectionFacing direction()
	{
		return direction;
	}

	/**
	 * @return Enemies.
	 */
	public TIntArrayList enemies()
	{
		return enemies;
	}

	/**
	 * To init the enemies of the player.
	 * 
	 * @param numPlayers
	 */
	public void setEnemies(int numPlayers)
	{
		for (int id = 1; id <= numPlayers; id++)
			if (id != index)
				enemies.add(id);
	}

	/**
	 * @return true of the function is immutable, allowing extra optimisations.
	 */
	public static boolean isStatic()
	{
		return false;
	}

	/**
	 * Called once after a game object has been created. Allows for any game-
	 * specific preprocessing (e.g. precomputing and caching of static results).
	 * 
	 * @param game
	 */
	public void preprocess(final Game game)
	{
		// Nothing to do.
	}

	/**
	 * @param game The game.
	 * @return Accumulated flags for this state type.
	 */
	public static long gameFlags(final Game game)
	{
		return 0l;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		if (direction != null)
		{
			concepts.set(Concept.PieceDirection.id(), true);
			concepts.set(Concept.PlayersWithDirections.id(), true);
		}
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		final boolean missingRequirement = false;
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		final boolean willCrash = false;
		return willCrash;
	}

	//-------------------------------------------------------------------------

	/**
	 * Set default colour. Can be overridden by user UI settings.
	 */
	public void setDefaultColour()
	{
		switch (index)
		{
		case 1:
			this.colour = new Color(255, 255, 255);
			break;
		case 2:
			this.colour = new Color(63, 63, 63);
			break;
		case 3:
			this.colour = new Color(191, 191, 191);
			break;
		case 4:
			this.colour = new Color(255, 0, 0);
			break;
		case 5:
			this.colour = new Color(0, 127, 255);
			break;
		case 6:
			this.colour = new Color(0, 200, 255);
			break;
		case 7:
			this.colour = new Color(230, 230, 0);
			break;
		case 8:
			this.colour = new Color(0, 230, 230);
			break;
		default:
			this.colour = null;
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final String str = "Player(name: " + name + ", index: " + index + ", colour: " + colour + ")";
		return str;
	}
}
