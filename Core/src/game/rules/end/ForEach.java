package game.rules.end;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.booleans.BooleanConstant.FalseConstant;
import game.functions.booleans.BooleanFunction;
import game.types.board.TrackType;
import game.types.play.RoleType;
import other.concept.EndConcepts;
import other.context.Context;
import other.context.EvalContextData;

/**
 * Applies the end condition to each player of a certain type.
 * 
 * @author cambolbro and Eric.Piette
 */
public final class ForEach extends BaseEndRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The Roletype to iterate. */
	final private RoleType type;

	/** To iterate on the tracks. */
	final private TrackType trackType;

	/** The condition to check for each iteration. */
	final private BooleanFunction cond; 

	//-------------------------------------------------------------------------

	/**
	 * @param type      Role type to iterate through [Shared].
	 * @param trackType To iterate on each track.
	 * @param If        Condition to apply.
	 * @param result    Result to return.
	 * 
	 * @example (forEach NonMover if:(is Blocked Player) (result Player Loss))
	 */
	public ForEach
	(
		@Opt @Or       final RoleType        type,
		@Opt @Or       final TrackType       trackType,
		         @Name final BooleanFunction If, 
			           final Result          result
	)
	{
		super(result);
		
		int numNonNull = 0;
		if (type != null)
			numNonNull++;
		if (trackType != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException(
					"ForEach(): one of RoleType or trackType has to be null.");
		
		this.type = (type == null) ? RoleType.Shared : type;
		this.cond = If;
		this.trackType = trackType;
	}

	//-------------------------------------------------------------------------

	@Override
	public EndRule eval(final Context context)
	{
		final int numPlayers = context.game().players().count();

		if (trackType != null) // If track is specified we iterate by track.
		{
			final int originalTrackIndex = context.track();
			for(int trackIndex = 0; trackIndex < context.board().tracks().size(); trackIndex++)
			{
				// Stop the loop if the game is inactive
				if (!context.active())
					break;

				context.setTrack(trackIndex);
				if (cond.eval(context))
					End.applyResult(result(), context);
			}
			context.setTrack(originalTrackIndex);
		}
		else
		{
			final int originPlayer = context.player();
			// Step through each player
			for (int pid = 1; pid <= numPlayers; pid++)
			{
				if (type == RoleType.NonMover)
				{
					final int mover = context.state().mover();
					if (pid == mover)
						continue;
				}

				// Do nothing if the player is not active.
				if (!context.active(pid))
					continue;

				// Check that player.
				context.setPlayer(pid);
				if (cond.eval(context))
					End.applyResult(result(), context);
			}
			context.setPlayer(originPlayer);
		}
		
		return new BaseEndRule(null);
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0;

		if (cond != null)
			gameFlags |= cond.gameFlags(game);

		if (result() != null)
			gameFlags |= result().gameFlags(game);

		return gameFlags;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		if (cond instanceof FalseConstant)
		{
			game.addRequirementToReport("One of the ending condition is \"false\" which is wrong.");
			missingRequirement = true;
		}

		if (trackType != null && !game.hasTrack())
		{
			game.addRequirementToReport(
					"The ludeme (forEach Track ...) is used in the ending rules but the board has no tracks.");
			missingRequirement = true;
		}

		// We check if the roletype is correct.
		if (type != null)
		{
			final int indexOwnerPhase = type.owner();
			if (((indexOwnerPhase < 1 && !type.equals(RoleType.NonMover)) && !type.equals(RoleType.Mover)
					&& !type.equals(RoleType.Player) && !type.equals(RoleType.All) && !type.equals(RoleType.Shared)
					&& !type.equals(RoleType.Each)) || indexOwnerPhase > game.players().count())
			{
				game.addRequirementToReport(
						"(forEach ...) is used in the ending conditions with an incorrect RoleType " + type + ".");
				missingRequirement = true;
			}
		}

		if (cond != null)
			missingRequirement |= cond.missingRequirement(game);

		if (result() != null)
			missingRequirement |= result().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (cond != null)
			willCrash |= cond.willCrash(game);

		if (result() != null)
			willCrash |= result().willCrash(game);
		return willCrash;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));

		if (cond != null)
			concepts.or(cond.concepts(game));

		if (result() != null)
			concepts.or(result().concepts(game));

		concepts.or(EndConcepts.get(cond, null, game, result()));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();

		if (cond != null)
			writeEvalContext.or(cond.writesEvalContextRecursive());

		if (result() != null)
			writeEvalContext.or(result().writesEvalContextRecursive());

		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();

		if (trackType != null)
			writeEvalContext.set(EvalContextData.Track.id(), true);
		else
			writeEvalContext.set(EvalContextData.Player.id(), true);

		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());

		if (cond != null)
			readEvalContext.or(cond.readsEvalContextRecursive());

		if (result() != null)
			readEvalContext.or(result().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (cond != null)
			cond.preprocess(game);
		
		if (result() != null)
			result().preprocess(game);
	}

	//-------------------------------------------------------------------------

	@Override
	public BitSet stateConcepts(final Context context)
	{
		final int numPlayers = context.game().players().count();
		final BitSet concepts = new BitSet();

		if (trackType != null) // If track is specified we iterate by track.
		{
			final int originalTrackIndex = context.track();
			for (int trackIndex = 0; trackIndex < context.board().tracks().size(); trackIndex++)
			{
				// Stop the loop if the game is inactive
				if (!context.active())
					break;

				context.setTrack(trackIndex);
				if (cond.eval(context))
					concepts.or(EndConcepts.get(cond, context, context.game(), result()));
			}
			context.setTrack(originalTrackIndex);
		}
		else
		{
			final int originPlayer = context.player();
			// Step through each player
			for (int pid = 1; pid <= numPlayers; pid++)
			{
				if (type == RoleType.NonMover)
				{
					final int mover = context.state().mover();
					if (pid == mover)
						continue;
				}

				// Do nothing if the player is not active.
				if (!context.active(pid))
					continue;

				// Check that player.
				context.setPlayer(pid);
				if (cond.eval(context))
					concepts.or(EndConcepts.get(cond, context, context.game(), result()));
			}
			context.setPlayer(originPlayer);
		}

		return concepts;
	}
}
