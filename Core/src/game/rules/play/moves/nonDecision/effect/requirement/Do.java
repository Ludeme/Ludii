package game.rules.play.moves.nonDecision.effect.requirement;

import java.util.BitSet;
import java.util.Iterator;
import java.util.function.BiPredicate;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.types.play.RepetitionType;
import main.collections.FastArrayList;
import other.MetaRules;
import other.concept.Concept;
import other.context.Context;
import other.context.TempContext;
import other.move.Move;
import other.move.MovesIterator;

/**
 * Applies two moves in order, according to given conditions.
 * 
 * @author Eric.Piette
 * 
 * @remarks Use ifAfterwards to filter out moves that do not satisfy some
 *          required condition after they are applied.
 */
public final class Do extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The pre-condition moves. */
	final Moves prior;

	/** The moves applied next to the prior moves. */
	final Moves next;
	
	/** The conditions to check after these moves. */
	final BooleanFunction ifAfterwards;

	//-------------------------------------------------------------------------

	/**
	 * @param prior        Moves to be applied first.
	 * @param next         Follow-up moves computed after the first set of moves.
	 *                     The prior moves are not returned if that set of moves is
	 *                     empty.
	 * @param ifAfterwards Moves must satisfy this condition afterwards to be legal.
	 * @param then         The moves applied after that move is applied.
	 * 
	 * @example (do (roll) next:(if (!= (count Pips) 0) (forEach Piece)))
	 * 
	 * @example (do (fromTo (from (sites Occupied by:All container:(mover))) (to
	 *          (sites LineOfPlay))) ifAfterwards:(is PipsMatch) )
	 */
	public Do
	(
			       final Moves           prior, 
		@Opt @Name final Moves           next, 
		@Opt @Name final BooleanFunction ifAfterwards, 
		@Opt 	   final Then            then
	)
	{
		super(then);
		this.next         = next;
		this.prior        = prior;
		this.ifAfterwards = ifAfterwards;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		// Return intersected list of moves
		final Moves result = new BaseMoves(super.then());

		// Code of the previous prior code
		if (next != null)
		{
			// WARNING: if any additional code for prior / premoves is required, do it
			// in helper methods like the two we're already calling below! That allows
			// for easy reproduction of the same functionality in FilterPlayout
			final Context newContext = new TempContext(context);
			final Moves preMoves = generateAndApplyPreMoves(context, newContext);
			result.moves().addAll(next.eval(newContext).moves());
			prependPreMoves(preMoves, result, context);
		}

		// Code of the previous filter
		if (ifAfterwards != null)
		{
			final Moves movesAfterIf = new BaseMoves(super.then());

			final FastArrayList<Move> toCheck = (next == null) ? prior.eval(context).moves() : result.moves();
			if (ifAfterwards.autoSucceeds())
			{
				movesAfterIf.moves().addAll(toCheck);
			}
			else
			{
				for (final Move m : toCheck)
					if (movePassesCond(m, context, false))
						movesAfterIf.moves().add(m);
			}

			if (then() != null)
				for (int j = 0; j < movesAfterIf.moves().size(); j++)
					movesAfterIf.moves().get(j).then().add(then().moves());

			return movesAfterIf;
		}

		// End result of the previous prior code
		if (then() != null)
			for (int j = 0; j < result.moves().size(); j++)
				result.moves().get(j).then().add(then().moves());

		return result;
	}
	
	/**
	 * Helper method to generate pre-moves from one context, and apply them to
	 * another context, and return the generated pre-moves.
	 * 
	 * @param genContext
	 * @param applyContext
	 * @return Generated pre-moves (already applied to given context)
	 */
	public final Moves generateAndApplyPreMoves(final Context genContext, final Context applyContext)
	{
		final Moves preMoves = new BaseMoves(null);
		for (final Move m : prior.eval(genContext).moves())
		{
			final Move appliedMove = (Move) m.apply(applyContext, false);
			preMoves.moves().add(appliedMove);
		}
		
		return preMoves;
	}
	
	/**
	 * Processes the given result moves by prepending the given preMoves.
	 * 
	 * @param preMoves
	 * @param result
	 * @param context
	 */
	public static void prependPreMoves(final Moves preMoves, final Moves result, final Context context)
	{
		// We insert the pre elements at the beginning.
		int insertIndex = 0;
		
		for (final Move preM : preMoves.moves())
		{
			for (final Move move : result.moves())
			{
				move.actions().addAll(insertIndex, preM.actions());
			}
			
			insertIndex = preM.actions().size();
		}

		if (result.moves().isEmpty() && context.game().hasHandDice())
		{
			final Move passMove = Game.createPassMove(context,true);
			for (final Move preM : preMoves.moves())
				passMove.actions().addAll(0, preM.actions());
			result.moves().add(passMove);
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public MovesIterator movesIterator(final Context context)
	{
		if (next == null && ifAfterwards != null)
		{
			return new MovesIterator()
			{
				protected Iterator<Move> itr = prior.movesIterator(context);
				protected Move nextMove = computeNextMove();
	
				@Override
				public boolean hasNext()
				{
					return (nextMove != null);
				}
	
				@Override
				public Move next()
				{
					final Move ret = nextMove;
					nextMove = computeNextMove();
	
					if (then() != null)
						ret.then().add(then().moves());
	
					return ret;
				}
	
				/**
				 * Computes the move to return for the subsequent next() call
				 * 
				 * @return
				 */
				private Move computeNextMove()
				{
					while (itr.hasNext())
					{
						final Move nextC = itr.next();
	
						if (ifAfterwards.autoSucceeds() || movePassesCond(nextC, context, false))
							return nextC;
					}
	
					return null;
				}

				@Override
				public boolean canMoveConditionally(final BiPredicate<Context, Move> predicate)
				{
					while (nextMove != null)
					{
						if (then() != null)
							nextMove.then().add(then().moves());
						
						if (predicate.test(context, nextMove))
							return true;
						else
							nextMove = computeNextMove();
					}
					
					return false;
				}
	
			};
		}
		else
		{
			// TODO see if we can also provide optimised implementation for this case
			return super.movesIterator(context);
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Helper method to check if a move passes our condition in a given context
	 * 
	 * @param m
	 * @param context
	 * @param includeRepetitionTests If true, we'll also immediately perform any state
	 * 	repetition tests that the game may require.
	 * @return True if move passes condition
	 */
	public boolean movePassesCond
	(
		final Move m, 
		final Context context, 
		final boolean includeRepetitionTests
	)
	{
		final Context newContext = new TempContext(context);
		// System.out.println("Owner of " + m.getTo() + ": " +
		// newContext.state().containerStates()[0].who(m.getTo()));
		m.apply(newContext, true);

		// DONE TO AVOID ANY REPLAY MOVE (e.g. Bug in Chess found by Wijnand :) )
		if (newContext.state().mover() == newContext.state().next())
			newContext.state().setNext(newContext.state().prev());
		
		if (includeRepetitionTests && !m.isPass())
		{
			final Game game = context.game();
			final MetaRules metaRules = game.metaRules();
			final RepetitionType type = metaRules.repetitionType();
			if(type != null)
			{
				switch (type)
				{
					case PositionalInTurn:
						if (context.trial().previousStateWithinATurn().contains(newContext.state().stateHash()))
							return false;
						break;
					case SituationalInTurn:
						if (context.trial().previousStateWithinATurn().contains(newContext.state().fullHash()))
							return false;
						break;
					case Positional:
						if (context.trial().previousState().contains(newContext.state().stateHash()))
							return false;
						break;
					case Situational:
						if (context.trial().previousState().contains(newContext.state().fullHash()))
							return false;
						break;
					default:
						break;
				}
			}
		}

		return ifAfterwards.eval(newContext);
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = prior.gameFlags(game) | super.gameFlags(game);

		if (next != null)
			gameFlags |= next.gameFlags(game);

		if (ifAfterwards != null)
			gameFlags |= ifAfterwards.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.or(prior.concepts(game));
		concepts.set(Concept.CopyContext.id(), true);
		concepts.set(Concept.DoLudeme.id(), true);

		if (next != null)
			concepts.or(next.concepts(game));

		if (ifAfterwards != null)
			concepts.or(ifAfterwards.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(prior.writesEvalContextRecursive());

		if (next != null)
			writeEvalContext.or(next.writesEvalContextRecursive());

		if (ifAfterwards != null)
			writeEvalContext.or(ifAfterwards.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(prior.readsEvalContextRecursive());

		if (next != null)
			readEvalContext.or(next.readsEvalContextRecursive());

		if (ifAfterwards != null)
			readEvalContext.or(ifAfterwards.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= prior.missingRequirement(game);
		if (next != null)
			missingRequirement |= next.missingRequirement(game);
		if (ifAfterwards != null)
			missingRequirement |= ifAfterwards.missingRequirement(game);
		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= prior.willCrash(game);
		if (next != null)
			willCrash |= next.willCrash(game);
		if (ifAfterwards != null)
			willCrash |= ifAfterwards.willCrash(game);
		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public void preprocess(final Game game)
	{
		super.preprocess(game);

		prior.preprocess(game);

		if (next != null)
			next.preprocess(game);

		if (ifAfterwards != null)
			ifAfterwards.preprocess(game);
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return Prior moves generator (or main moves generator if there is no "then")
	 */
	public Moves prior()
	{
		return prior;
	}
	
	/**
	 * @return Main moves generator after prior (may be null, then prior becomes main
	 * moves generator)
	 */
	public Moves after()
	{
		return next;
	}
	
	/**
	 * @return The condition that must hold in game state after executing moves for
	 * those moves to be considered legal.
	 */
	public BooleanFunction ifAfter()
	{
		return ifAfterwards;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean canMoveTo(final Context context, final int target)
	{
		if (next != null)
			return next.canMoveTo(context, target);

		if (ifAfterwards != null)
		{
			if (ifAfterwards.autoSucceeds())
				return prior.canMoveTo(context, target);

			final Iterator<Move> movesIterator = movesIterator(context);
			while (movesIterator.hasNext())
			{
				final Move m = movesIterator.next();
				if (m.toNonDecision() == target)
					return true;
			}
			
			return false;
		}
		
		return false;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		String nextString = "";
		
		if (next != null)
			nextString = " and afterwards " + next.toEnglish(game);
		
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
			
		return prior.toEnglish(game) + nextString + thenString;
	}
	
	//-------------------------------------------------------------------------
	
}
