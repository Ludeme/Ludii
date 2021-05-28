package game.functions.region.sites.track;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import annotations.Or2;
import game.Game;
import game.functions.ints.IntFunction;
import game.functions.region.BaseRegionFunction;
import game.types.play.RoleType;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.context.Context;

/**
 * Returns all the sites of a track.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SitesTrack extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** If we can, we'll precompute once and cache. */
	private Region precomputedRegion = null;

	//-------------------------------------------------------------------------

	/** The index of the owner of the track. */
	private final IntFunction pid;
	
	/** The name of the track. */
	private final String name;
	
	/** Returns the sites from that site in the track (site included). */
	private final IntFunction fromFn;

	/** Returns the sites until that site (site included) */
	private final IntFunction toFn;

	/**
	 * @param pid  Index of the player.
	 * @param role The Role type corresponding to the index.
	 * @param name The name of the track.
	 * @param from Only the sites in the track from that site (included).
	 * @param to   Only the sites in the track until to reach that site (included).
	 */
	public SitesTrack
	(
		@Or  @Opt        final game.util.moves.Player pid,
		@Or  @Opt        final RoleType               role,
		@Or2 @Opt        final String                 name,
		     @Opt @Name  final IntFunction            from,
		     @Opt @Name	 final IntFunction            to
    )
	{
		this.pid = (role != null) ? RoleType.toIntFunction(role) : (pid != null) ? pid.index() : null;
		this.name = (name == null) ? "" : name;
		this.fromFn = from;
		this.toFn = to;
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		if (precomputedRegion != null)
			return precomputedRegion;

		game.equipment.container.board.Track track = null;

		if (context.track() != Constants.UNDEFINED)
		{
			final int index = context.track();
			if (index >= 0 && index < context.tracks().size())
				track = context.tracks().get(index);
		}
		else
		{
			final int playerId = (pid != null) ? pid.eval(context) : 0;

			for (final game.equipment.container.board.Track t : context.tracks())
			{
				if (name != null)
				{
					if (t.name().contains(name))
					{
						track = t;
						break;
					}
				}
				else if (t.owner() == playerId || t.owner() == 0)
				{
					track = t;
					break;
				}
			}
		}

		if (track == null)
			return new Region(); // no track for this player

		final TIntArrayList sites = new TIntArrayList();

		if (fromFn == null && toFn == null)
		{
			for (int i = 0; i < track.elems().length; i++)
				sites.add(track.elems()[i].site);
		}
		else
		{
			final int from = (fromFn != null) ? fromFn.eval(context) : Constants.UNDEFINED;
			final int to = (toFn != null) ? toFn.eval(context) : Constants.UNDEFINED;

			// Get the first index.
			int fromIndex = Constants.UNDEFINED;

			// No from defined so we take from the start
			if (from == Constants.UNDEFINED)
				fromIndex = 0;
			else // Check the index on the track corresponding to the from site.
			{
				for (int i = 0; i < track.elems().length; i++)
				{
					final int site = track.elems()[i].site;
					if (site == from)
					{
						fromIndex = i;
						break;
					}
				}
				// From not found
				if (fromIndex == Constants.UNDEFINED)
					return new Region(sites.toArray());
			}

			// Get the sites until reaching the to site.
			boolean toFound = false;
			for (int i = fromIndex; i < track.elems().length; i++)
			{
				final int site = track.elems()[i].site;
				sites.add(site);
				if (site == to)
				{
					toFound = true;
					break;
				}
			}

			// If to is not found we check the sites before the from index on the track
			if (!toFound)
			{
				for (int i = 0; i < fromIndex; i++)
				{
					final int site = track.elems()[i].site;
					sites.add(site);
					if (site == to)
						break;
				}
			}
		}

		return new Region(sites.toArray());
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		if (fromFn != null && !fromFn.isStatic())
			return false;
		if (toFn != null && !toFn.isStatic())
			return false;

		if (pid != null)
			return pid.isStatic();

		return false;
	}

	@Override
	public String toString()
	{
		return "Track()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flags = 0L;

		if (fromFn != null)
			flags = fromFn.gameFlags(game);

		if (toFn != null)
			flags = toFn.gameFlags(game);

		if (pid != null)
			flags = pid.gameFlags(game);

		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		if (pid != null)
			concepts.or(pid.concepts(game));
		if (fromFn != null)
			concepts.or(fromFn.concepts(game));
		if (toFn != null)
			concepts.or(toFn.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (pid != null)
			writeEvalContext.or(pid.writesEvalContextRecursive());
		if (fromFn != null)
			writeEvalContext.or(fromFn.writesEvalContextRecursive());
		if (toFn != null)
			writeEvalContext.or(toFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (pid != null)
			readEvalContext.or(pid.readsEvalContextRecursive());
		if (fromFn != null)
			readEvalContext.or(fromFn.readsEvalContextRecursive());
		if (toFn != null)
			readEvalContext.or(toFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (!game.hasTrack())
		{
			game.addRequirementToReport("The ludeme (sites Track ...) is used but the board has no tracks.");
			missingRequirement = true;
		}
		if (pid != null)
			missingRequirement |= pid.missingRequirement(game);
		if (fromFn != null)
			missingRequirement |= fromFn.missingRequirement(game);
		if (toFn != null)
			missingRequirement |= toFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (pid != null)
			willCrash |= pid.willCrash(game);
		if (fromFn != null)
			willCrash |= fromFn.willCrash(game);
		if (toFn != null)
			willCrash |= toFn.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (pid != null)
			pid.preprocess(game);
		if (fromFn != null)
			fromFn.preprocess(game);
		if (toFn != null)
			toFn.preprocess(game);
		if (isStatic())
			precomputedRegion = eval(new Context(game, null));
	}
}
