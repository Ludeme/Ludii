package game.functions.ints.trackSite.move;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.container.board.Track;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.iterator.From;
import game.functions.ints.state.Mover;
import game.types.play.RoleType;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.context.Context;
import other.state.container.ContainerState;
import other.state.track.OnTrackIndices;

/**
 * Returns the new site on the player track on function of the current position
 * of the component and the number of steps to move forward.
 * 
 * @author  Eric Piette
 * 
 * @remarks Applies to any game with a defined track.
 */
@Hide
public final class TrackSiteMove extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which Index. */
	private final IntFunction currentLocation;

	/** Which Index. */
	private final IntFunction steps;

	/** Which player. */
	private final IntFunction player;

	/** Track name. */
	private final String name;
	
	//-------------------------------------------------------------------------

	/** Pre-computed track if we are sure of the track. */
	private final Track preComputedTrack = null;

	//-------------------------------------------------------------------------

	/**
	 * @param from   The current location [(from)].
	 * @param player The owner of the track [(mover)].
	 * @param role   The role of the owner of the track [Mover].
	 * @param name   The name of the track.
	 * @param steps  The distance to move on the track.
	 */
	public TrackSiteMove
	(
		@Opt     @Name final IntFunction            from,
		@Opt @Or   	   final RoleType               role,
		@Opt @Or   	   final game.util.moves.Player player,
		@Opt @Or       final String                 name,
			     @Name final IntFunction            steps
	)
	{
		int numNonNull = 0;
		if (player != null)
			numNonNull++;
		if (name != null)
			numNonNull++;
		if (role != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Zero or one Or parameter can be non-null.");

		this.player = (player == null && role == null) ? new Mover()
				: (role != null) ? RoleType.toIntFunction(role) : player.index();
		this.steps = steps;
		currentLocation = (from == null) ? new From(null) : from;
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

		if (track == null && name != null) // The track was not precomputed because it is not owned by a player.
			for (final Track t : context.game().board().tracks())
				if (t.name().contains(name))
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
			return Constants.OFF;  // no track for this player
		
		int i = track.elems().length;
		if (currentLocation == null) 
		{
			i = Constants.OFF;
		}
		else
		{
			final int currentLoc = currentLocation.eval(context);

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
			} // If the track is a full loop like the mancala games, we just try to found the
				// corresponding index for the current location.
			else
			{
				for (i = 0; i < track.elems().length; i++)
					if (track.elems()[i].site == currentLoc)
						break;
			}
		}
		
		final int numSteps = steps.eval(context);

		i += numSteps >= 0 ? numSteps : 0;

		if (i < track.elems().length) 
			return track.elems()[i].site;

		// To manage the loop track
		if (track.elems()[track.elems().length - 1].next != Constants.OFF)
		{
			while (true)
			{
				i -= track.elems().length;
				if (i == 0)
					return track.elems()[track.elems().length - 1].next;
	
				if ((i - 1) < track.elems().length) 
					return track.elems()[i - 1].next;
			}
		}
		
		return Constants.OFF;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		boolean isStatic = player.isStatic();
		if (steps != null)
			isStatic = isStatic && steps.isStatic();
		if (currentLocation != null)
			isStatic = isStatic && currentLocation.isStatic();
		return isStatic;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = player.gameFlags(game);
		
		if (steps != null)
			gameFlags = gameFlags | steps.gameFlags(game);
		
		if (currentLocation != null)
			gameFlags = gameFlags | currentLocation.gameFlags(game);
	
		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(player.concepts(game));

		if (steps != null)
			concepts.or(steps.concepts(game));

		if (currentLocation != null)
			concepts.or(currentLocation.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(player.writesEvalContextRecursive());

		if (steps != null)
			writeEvalContext.or(steps.writesEvalContextRecursive());

		if (currentLocation != null)
			writeEvalContext.or(currentLocation.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(player.readsEvalContextRecursive());

		if (steps != null)
			readEvalContext.or(steps.readsEvalContextRecursive());

		if (currentLocation != null)
			readEvalContext.or(currentLocation.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		player.preprocess(game);
		if (steps != null)
			steps.preprocess(game);
		if (currentLocation != null)
			currentLocation.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (!game.hasTrack())
		{
			game.addRequirementToReport("The ludeme (trackSite Move ...) is used but the board has no tracks.");
			missingRequirement = true;
		}
		missingRequirement |= player.missingRequirement(game);
		if (steps != null)
			missingRequirement |= steps.missingRequirement(game);
		if (currentLocation != null)
			missingRequirement |= currentLocation.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= player.willCrash(game);
		if (steps != null)
			willCrash |= steps.willCrash(game);
		if (currentLocation != null)
			willCrash |= currentLocation.willCrash(game);
		return willCrash;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{		
		return steps.toEnglish(game) + " steps forward from site " + currentLocation.toEnglish(game) + " on track " + name;
	}

	//-------------------------------------------------------------------------
		
}
