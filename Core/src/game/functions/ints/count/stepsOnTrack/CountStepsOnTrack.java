package game.functions.ints.count.stepsOnTrack;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.container.board.Track;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.state.Mover;
import game.types.play.RoleType;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.context.Context;
import other.state.container.ContainerState;
import other.state.track.OnTrackIndices;

/**
 * Returns the number of steps between two sites.
 * 
 * @author Eric.Piette
 */
@Hide
public final class CountStepsOnTrack extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The first site. */
	private final IntFunction site1Fn;

	/** The second site. */
	private final IntFunction site2Fn;

	/** Which player. */
	private final IntFunction player;

	/** Track name. */
	private final String name;
	
	//-------------------------------------------------------------------------

	/** Pre-computed track if we are sure of the track. */
	private final Track preComputedTrack = null;

	/**
	 * @param player The owner of the track [(mover)].
	 * @param role   The role of the owner of the track [Mover].
	 * @param name   The name of the track.
	 * @param site1  The first site.
	 * @param site2  The second site.
	 */
	public CountStepsOnTrack
	(
		@Opt @Or  final RoleType               role,
		@Opt @Or  final game.util.moves.Player player,
		@Opt @Or  final String                 name,
		          final IntFunction            site1, 
		          final IntFunction            site2
	)
	{
		this.player = (player == null && role == null) ? new Mover()
				: (role != null) ? RoleType.toIntFunction(role) : player.index();
		this.site1Fn = site1;
		this.site2Fn = site2;
		this.name = name;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final int playerId = player.eval(context);
		Track track = preComputedTrack;

		if (name != null)
			for (final Track t : context.game().board().tracks())
				if (t.name().contains(name) && t.owner() == playerId)
				{
					track = t;
					break;
				}

		if (track == null) // The track was not precomputed because it is owned by a player.
		{
			final Track[] tracks = context.board().ownedTracks(playerId);
			if (tracks.length != 0)
			{
				track = tracks[0];
			}
			else
			{
				final Track[] tracksWithNoOwner = context.board().ownedTracks(0);
				if (tracksWithNoOwner.length != 0)
					track = tracksWithNoOwner[0];
			}
		}

		if (track == null)
			return Constants.OFF; // no track for this player

		final int site1 = site1Fn.eval(context);
		final int site2 = site2Fn.eval(context);

		final int currentLoc = site1;

		int i = track.elems().length;
		if (!track.islooped() && context.game().hasInternalLoopInTrack())
		{
			if (currentLoc < 0)
				return Constants.OFF;

			// We get the component on the current location.
			final ContainerState cs = context.containerState(context.containerId()[currentLoc]);
			int what = cs.what(currentLoc, context.board().defaultSite());

			// For stacking game, we get a piece owned by the owner of the track.
			final int sizeStack = cs.sizeStack(currentLoc, context.board().defaultSite());
			for (int lvl = 0; lvl < sizeStack; lvl++)
			{
				final int who = cs.who(currentLoc, lvl, context.board().defaultSite());
				if (who == playerId)
				{
					what = cs.what(currentLoc, lvl, context.board().defaultSite());
					break;
				}
			}

			// We get the current index on the track according to the onTrackIndices
			// structure.
			if (what != 0)
			{
				final OnTrackIndices onTrackIndices = context.state().onTrackIndices();
				final int trackIdx = track.trackIdx();
				final TIntArrayList locsToIndex = onTrackIndices.locToIndex(trackIdx, currentLoc);

				for (int j = 0; j < locsToIndex.size(); j++)
				{
					final int index = locsToIndex.getQuick(j);
					final int count = onTrackIndices.whats(trackIdx, what, index);

					if (count > 0)
					{
						i = index;
						break;
					}
				}
			}
			else // If no piece, we just try to found the corresponding index for the current
					// location.
			{
				for (i = 0; i < track.elems().length; i++)
					if (track.elems()[i].site == currentLoc)
						break;
			}
			
			int count = 0;
			for (; i < track.elems().length; i++)
			{
				if (track.elems()[i].site == site2)
					return count;
				count++;
			}
			
		} 
		else // If the track is a full loop.
		{
			for (i = 0; i < track.elems().length; i++)
				if (track.elems()[i].site == currentLoc)
					break;
			
			final int index = i;
			int count = 0;
			for (; i < track.elems().length; i++)
			{
				if (track.elems()[i].site == site2)
					return count;
				count++;
			}
			
			for (i = 0; i < index; i++)
			{
				if (track.elems()[i].site == site2)
					return count;
				count++;
			}
		}



		return Constants.OFF;

	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return player.isStatic() && site1Fn.isStatic() && site2Fn.isStatic();
	}

	@Override
	public String toString()
	{
		return "CountStepsOnTrack()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		final long gameFlags = site1Fn.gameFlags(game) | site2Fn.gameFlags(game) | player.gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(site1Fn.concepts(game));
		concepts.or(site2Fn.concepts(game));
		concepts.or(player.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(site1Fn.writesEvalContextRecursive());
		writeEvalContext.or(site2Fn.writesEvalContextRecursive());
		writeEvalContext.or(player.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(site1Fn.readsEvalContextRecursive());
		readEvalContext.or(site2Fn.readsEvalContextRecursive());
		readEvalContext.or(player.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		player.preprocess(game);
		site1Fn.preprocess(game);
		site2Fn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (!game.hasTrack())
		{
			game.addRequirementToReport(
					"The ludeme (count StepsOnTrack ...) is used but the board has no defined tracks.");
			missingRequirement = true;
		}
		missingRequirement |= player.missingRequirement(game);
		missingRequirement |= site1Fn.missingRequirement(game);
		missingRequirement |= site2Fn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= player.willCrash(game);
		willCrash |= site1Fn.willCrash(game);
		willCrash |= site2Fn.willCrash(game);
		return willCrash;
	}
}
