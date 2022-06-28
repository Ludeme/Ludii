package game.rules.play.moves.nonDecision.effect.requirement;

import java.util.BitSet;

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
import other.context.Context;
import other.context.EvalContextData;

/**
 * Returns the first legal move on the track.
 * 
 * @author Eric.Piette
 * 
 * @remarks Example Backgammon.
 */
public final class FirstMoveOnTrack extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The moves to check. */
	private final Moves moves;

	/** The name of the track. */
	private final String trackName;

	/** The owner of the track. */
	private final RoleType owner;

	/**
	 * @param trackName The name of the track.
	 * @param owner     The owner of the track.
	 * @param moves     The moves to check.
	 * @param then      The moves applied after that move is applied.
	 * 
	 * @example (firstMoveOnTrack (forEach Piece))
	 */
	public FirstMoveOnTrack
	(
		@Opt final String 	trackName,
		@Opt final RoleType owner, 
		     final Moves 	moves, 
		@Opt final Then 	then
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
		final int who = (owner == null) ? Constants.UNDEFINED : new Id(null, owner).eval(context);

		Track track = null;
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

		final int originSiteValue = context.site();

		for (int i = 0; i < track.elems().length; i++)
		{
			final int site = track.elems()[i].site;
			if (site < 0)
				continue;
			context.setSite(site);
			final Moves movesComputed = moves.eval(context);
			if (!movesComputed.moves().isEmpty())
			{
				returnMoves.moves().addAll(movesComputed.moves());
				break;
			}
		}
		context.setSite(originSiteValue);

		// The subsequents to add to the moves
		if (then() != null)
			for (int j = 0; j < moves.moves().size(); j++)
				moves.moves().get(j).then().add(then().moves());

		// Store the Moves in the computed moves.
		for (int j = 0; j < returnMoves.moves().size(); j++)
			returnMoves.moves().get(j).setMovesLudeme(this);

		return returnMoves;
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
		concepts.or(super.concepts(game));
		concepts.or(moves.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(moves.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.Site.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(moves.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (!game.hasTrack())
		{
			game.addRequirementToReport("The ludeme (firstMoveOnTrack ...) is used but the board has no tracks.");
			missingRequirement = true;
		}

		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= moves.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= moves.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
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

}
