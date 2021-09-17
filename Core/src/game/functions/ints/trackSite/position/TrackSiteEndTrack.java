package game.functions.ints.trackSite.position;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.container.board.Track;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.types.play.RoleType;
import main.Constants;
import other.context.Context;

/**
 * Returns the last site of a track.
 * 
 * @author Eric.Piette
 */
@Hide
public final class TrackSiteEndTrack extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The name of the track. */
	private final String name;
	
	/** Player Id function. */
	private final IntFunction pidFn;

	//-------------------------------------------------------------------------

	/** The pre-computed value. */
	private int precomputedValue = Constants.OFF;

	//-------------------------------------------------------------------------

	/**
	 * @param player The index of the player.
	 * @param role   The role of the player.
	 * @param name   The name of the track.
	 */
	public TrackSiteEndTrack
	(
			@Or @Opt final game.util.moves.Player player,
			@Or @Opt final RoleType	              role,
			    @Opt final String                 name
	)
	{
		this.name = name;
		pidFn = (player != null) ? player.index() : (role != null) ? RoleType.toIntFunction(role) : null;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (precomputedValue != Constants.OFF)
			return precomputedValue;

		final int playerId = (pidFn != null) ? pidFn.eval(context) : 0;
		Track track = null;

		for (final Track t : context.tracks())
		{
			if (name != null && playerId == 0)
			{
				if (t.name().contains(name))
				{
					track = t;
					break;
				}
			}
			else if (name != null)
			{
				if (name != null)
				{
					if (t.name().contains(name) && t.owner() == playerId)
					{
						track = t;
						break;
					}
				}
			}
			else if (t.owner() == playerId || t.owner() == 0)
			{
				track = t;
				break;
			}
		}

		if (track == null)
		{
			if (context.game().board().tracks().size() == 0)
				return Constants.UNDEFINED; // no track at all.
			else
				track = context.game().board().tracks().get(0);
		}

		// Check if the track is empty.
		if (track.elems().length == 0)
			return Constants.UNDEFINED;

		return track.elems()[track.elems().length - 1].site;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return pidFn == null || pidFn.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flags = 0L;

		if (pidFn != null)
			flags |= pidFn.gameFlags(game);

		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();

		if (pidFn != null)
			concepts.or(pidFn.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();

		if (pidFn != null)
			writeEvalContext.or(pidFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();

		if (pidFn != null)
			readEvalContext.or(pidFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (pidFn != null)
			pidFn.preprocess(game);

		if (isStatic())
			precomputedValue = eval(new Context(game, null));
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (!game.hasTrack())
		{
			game.addRequirementToReport("The ludeme (trackSite EndTrack ...) is used but the board has no tracks.");
			missingRequirement = true;
		}

		if (pidFn != null)
			missingRequirement |= pidFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (pidFn != null)
			willCrash |= pidFn.willCrash(game);
		return willCrash;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "";
	}

	@Override
	public String toEnglish(final Game game) 
	{
		String trackName = name;
		if (trackName == null)
			trackName = "the board's track";
		
		return "the last site of " + trackName;
	}
	
	//-------------------------------------------------------------------------
	
}