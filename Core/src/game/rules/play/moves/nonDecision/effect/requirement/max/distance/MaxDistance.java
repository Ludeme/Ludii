package game.rules.play.moves.nonDecision.effect.requirement.max.distance;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.equipment.container.board.Track;
import game.functions.ints.board.Id;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.types.play.RoleType;
import main.Constants;
import other.concept.Concept;
import other.context.Context;
import other.context.TempContext;
import other.move.Move;

/**
 * Filters the moves to keep only the moves allowing the maximum distance on a
 * track in a turn.
 * 
 * @author Eric.Piette
 */
@Hide
public final class MaxDistance extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The moves to maximise. */
	private final Moves moves;
	
	/** The name of the track. */
	private final String trackName;

	/** The owner of the track. */
	private final RoleType owner;

	/**
	 * @param trackName The name of the track.
	 * @param owner     The owner of the track.
	 * @param moves     The moves to filter.
	 * @param then      The moves applied after that move is applied.
	 */
	public MaxDistance
	(
		@Opt final String   trackName,
		@Opt final RoleType owner, 
			 final Moves    moves, 
		@Opt final Then     then
	)
	{
		super(then);

		this.moves = moves;
		this.trackName = trackName;
		this.owner = owner;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves returnMoves = new BaseMoves(super.then());

		Track track = null;
		final int who = (owner == null) ? Constants.UNDEFINED : new Id(null, owner).eval(context);
		for (final Track t : context.tracks())
		{
			if (trackName == null || (who == Constants.OFF && t.name().equals(trackName)
					|| (who != Constants.OFF && t.owner() == who && t.name().contains(trackName))))
			{
				track = t;
				break;
			}
		}

		// If the track does exist we return an empty list of moves.
		if (track == null)
			return moves;

		// We compute the moves.
		final Moves movesToEval = moves.eval(context);
		final int[] distanceCount = new int[movesToEval.moves().size()];

		for (int i = 0; i < movesToEval.moves().size(); i++)
		{
			final Move m = movesToEval.moves().get(i);

			int indexFrom = Constants.UNDEFINED;
			int indexTo = Constants.UNDEFINED;
			for (int j = 0; j < track.elems().length; j++)
			{
				if (track.elems()[j].site == m.fromNonDecision())
					indexFrom = j;
				else if (track.elems()[j].site == m.toNonDecision())
					indexTo = j;
				
				if(indexFrom != Constants.UNDEFINED && indexTo != Constants.UNDEFINED)
					break;
			}

			final int distance = Math.abs(indexFrom - indexTo);
			distanceCount[i] = context.recursiveCalled() ? distance
					: getDistanceCount(context, track, context.state().mover(), m, distance);
		}

		int max = 0;

		// Get the max of the distanceCount.
		for (final int sizeDistance : distanceCount)
			if (sizeDistance > max)
				max = sizeDistance;


		// Keep only the large distance moves.
		for (int i = 0; i < movesToEval.moves().size(); i++)
			if (distanceCount[i] == max)
				returnMoves.moves().add(movesToEval.moves().get(i));
		
		final Moves toReturn = moves.eval(context);

		// Store the Moves in the computed moves.
		for (int j = 0; j < toReturn.moves().size(); j++)
			toReturn.moves().get(j).setMovesLudeme(toReturn);

		return toReturn;
	}

	//-------------------------------------------------------------------------

	/**
	 * 
	 * @param context
	 * @param move
	 * @return the count of the replay of this move.
	 */
	private int getDistanceCount(final Context context, final Track track, final int mover, final Move m,
			final int distance)
	{
		if (m.isPass() || m.toNonDecision() == m.fromNonDecision())
			return distance;

		final Context newContext = new TempContext(context);
		newContext.setRecursiveCalled(true);
		newContext.game().apply(newContext, m);

		// Not same player
		if (mover != newContext.state().mover())
			return distance;

		final Moves legalMoves = newContext.game().moves(newContext);

		final int[] distanceCount = new int[legalMoves.moves().size()];

		for (int i = 0; i < legalMoves.moves().size(); i++)
		{
			final Move newMove = legalMoves.moves().get(i);

			int indexFrom = Constants.UNDEFINED;
			int indexTo = Constants.UNDEFINED;
			for (int j = 0; j < track.elems().length; j++)
			{
				if (track.elems()[j].site == m.fromNonDecision())
					indexFrom = j;
				else if (track.elems()[j].site == m.toNonDecision())
					indexTo = j;

				if (indexFrom != Constants.UNDEFINED && indexTo != Constants.UNDEFINED)
					break;
			}

			final int newDistance = Math.abs(indexFrom - indexTo);
			distanceCount[i] = getDistanceCount(newContext, track, mover, newMove, distance + newDistance);
		}

		int max = 0;

		// Get the max of the replayCount.
		for (final int sizeDistance : distanceCount)
			if (sizeDistance > max)
				max = sizeDistance;

		return max;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = moves.gameFlags(game) | super.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(moves.concepts(game));
		concepts.or(super.concepts(game));
		concepts.set(Concept.MaxDistance.id(), true);
		concepts.set(Concept.CopyContext.id(), true);

		if (then() != null)
			concepts.or(then().concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(moves.writesEvalContextRecursive());
		writeEvalContext.or(super.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(moves.readsEvalContextRecursive());
		readEvalContext.or(super.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean isStatic()
	{
		final boolean isStatic = moves.isStatic();
		return isStatic;
	}

	@Override
	public void preprocess(final Game game)
	{
		moves.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (!game.hasTrack())
		{
			game.addRequirementToReport("The ludeme (max Distance ...) is used but the board has no tracks.");
			missingRequirement = true;
		}
		missingRequirement |= moves.missingRequirement(game);
		missingRequirement |= super.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= moves.willCrash(game);
		willCrash |= super.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toEnglish(final Game game)
	{
		return "MaxDistance";
	}
}
