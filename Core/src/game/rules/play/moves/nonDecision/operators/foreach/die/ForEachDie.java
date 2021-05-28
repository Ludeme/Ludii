package game.rules.play.moves.nonDecision.operators.foreach.die;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.equipment.container.other.Dice;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Then;
import game.rules.play.moves.nonDecision.operator.Operator;
import main.Constants;
import main.collections.FastArrayList;
import other.action.die.ActionUpdateDice;
import other.action.die.ActionUseDie;
import other.action.state.ActionSetTemp;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.move.Move;

/**
 * Generates moves according to the values of the dice.
 * 
 * @author Eric.Piette
 * 
 * @remarks This ludeme is used in dice games, and works for any combination of dice.
 */
@Hide
public final class ForEachDie extends Operator
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The index of the hand of dice. */
	private final IntFunction handDiceIndexFn;

	/** To combine dice. */
	private final BooleanFunction combined;

	/** If double rules (e.g. Backgammon). */
	private final BooleanFunction replayDoubleFn;

	/** The rule to respect. */
	private final BooleanFunction rule;

	/** The moves to apply */
	private final Moves moves;

	/**
	 * @param handDiceIndex The index of the dice container [0].
	 * @param combined      True if the combination is allowed [False].
	 * @param replayDouble  True if double allows a second move [False].
	 * @param If            The condition to satisfy to move [True].
	 * @param moves         The moves to apply.
	 * @param then          The moves applied after that move is applied.
	 */
	public ForEachDie
	(
		@Opt 	     final IntFunction     handDiceIndex,
	    @Opt @Name 	 final BooleanFunction combined,
	    @Opt @Name 	 final BooleanFunction replayDouble,
		@Opt @Name 	 final BooleanFunction If,
		     		 final Moves           moves,
		@Opt 		 final Then            then
	)
	{
		super(then);
		this.handDiceIndexFn = (handDiceIndex == null) ? new IntConstant(0) : handDiceIndex;
		this.rule = (If == null) ? new BooleanConstant(true) : If;
		this.combined = (combined == null) ? new BooleanConstant(false) : combined;
		this.replayDoubleFn = (replayDouble == null) ? new BooleanConstant(false) : replayDouble;
		this.moves = moves;
	}

	@Override
	public Moves eval(final Context context)
	{
		final Moves returnMoves = new BaseMoves(super.then());
		final int handDiceIndex = handDiceIndexFn.eval(context);
		if (context.state().currentDice() == null)
			return returnMoves;
		final int[] dieValues = context.state().currentDice(handDiceIndex);

		final int containerIndex = context.game().getHandDice(handDiceIndex).index();

		boolean replayDouble = replayDoubleFn.eval(context);
		if (replayDouble)
		{
			final int firstDieValue = dieValues[0];
			for (int i = 0; i < dieValues.length; i++)
			{
				if (dieValues[i] != firstDieValue)
				{
					replayDouble = false;
					break;
				}
			}
		}

		final int origDieValue = context.pipCount();
		for (int i = 0 ; i < dieValues.length; i++)
		{
			final int pipCount = dieValues[i];
			context.setPipCount(pipCount);
			if (rule.eval(context))
			{
				final Moves computedMoves = moves.eval(context);
				final FastArrayList<Move> moveList = computedMoves.moves();
				final int site = context.sitesFrom()[containerIndex] + i;
					final ActionUseDie action = new ActionUseDie(handDiceIndex, i, site);
					for (final Move m : moveList)
						m.actions().add(action);

				// Double
				if (replayDouble && context.state().temp() == Constants.UNDEFINED)
				{
					final ActionSetTemp setTemp = new ActionSetTemp(pipCount);
					for (final Move m : moveList)
						m.actions().add(setTemp);
				}
				else if (replayDouble)
				{
					final ActionSetTemp setTemp = new ActionSetTemp(Constants.UNDEFINED);
					for (final Move m : moveList)
						m.actions().add(setTemp);
				}
				else if (context.state().temp() != Constants.UNDEFINED)
				{
					for (final Dice dice : context.game().handDice())
					{
						if ((context.state().temp() - 1) < dice.getNumFaces())
							for (int loc = context.sitesFrom()[dice.index()]; loc < context.sitesFrom()[dice.index()]
									+ dice.numLocs(); loc++)
							{
								final ActionUpdateDice actionState = new ActionUpdateDice(loc,
										context.state().temp() - 1);
								for (final Move m : moveList)
									m.actions().add(actionState);
							}
					}
				}

				returnMoves.moves().addAll(computedMoves.moves());
			}
		}

		if (combined.eval(context))
		{
			if (dieValues.length == 2)
			{
				final int dieValue1 = dieValues[0];
				final int dieValue2 = dieValues[1];
				if (dieValue1 != 0 && dieValue2 != 0)
				{
					context.setPipCount(dieValue1 + dieValue2);
					if (rule.eval(context))
					{
						final Moves computedMoves = moves.eval(context);
						final FastArrayList<Move> moveList = computedMoves.moves();
						final int siteFrom = context.sitesFrom()[containerIndex];
						final ActionUseDie actionDie1 = new ActionUseDie(handDiceIndex, 0, siteFrom);
						final ActionUseDie actionDie2 = new ActionUseDie(handDiceIndex, 1, siteFrom + 1);
						for (final Move m : moveList)
						{
							m.actions().add(actionDie1);
							m.actions().add(actionDie2);
						}
						returnMoves.moves().addAll(computedMoves.moves());
					}
				}
			}
			else if (dieValues.length == 3)
			{
				// Sum of the three dice
				final int dieValue1 = dieValues[0];
				final int dieValue2 = dieValues[1];
				final int dieValue3 = dieValues[2];
				if(dieValue1 != 0 && dieValue2 != 0 && dieValue3 != 0)
				{
					context.setPipCount(dieValue1 + dieValue2 + dieValue3);
					if (rule.eval(context))
					{
						final Moves computedMoves = moves.eval(context);
						final FastArrayList<Move> moveList = computedMoves.moves();
						final int siteFrom = context.sitesFrom()[containerIndex];
						final ActionUseDie actionDie1 = new ActionUseDie(handDiceIndex, 0, siteFrom);
						final ActionUseDie actionDie2 = new ActionUseDie(handDiceIndex, 1, siteFrom +1);
						final ActionUseDie actionDie3 = new ActionUseDie(handDiceIndex, 2, siteFrom +2);
						for (final Move m : moveList)
						{
							m.actions().add(actionDie1);
							m.actions().add(actionDie2);
							m.actions().add(actionDie3);
						}
						returnMoves.moves().addAll(computedMoves.moves());
					}
				}

				// Each combination of two dices
				for (int i = 0; i < 2; i++)
				{
					for (int j = i + 1; j < 3; j++)
					{
						final int d1 = dieValues[i];
						final int d2 = dieValues[j];
						if (d1 != 0 && d2 != 0)
						{
							context.setPipCount(d1 + d2);
							if (rule.eval(context))
							{
								final Moves computedMoves = moves.eval(context);
								final FastArrayList<Move> moveList = computedMoves.moves();
								final int siteFrom = context.sitesFrom()[containerIndex];
								final ActionUseDie actionDie1 = new ActionUseDie(handDiceIndex, i, siteFrom +i);
								final ActionUseDie actionDie2 = new ActionUseDie(handDiceIndex, j, siteFrom +j);
								for (final Move m : moveList)
								{
									m.actions().add(actionDie1);
									m.actions().add(actionDie2);
								}
								returnMoves.moves().addAll(computedMoves.moves());
							}
						}
					}
				}
			}
			// TO DO more than two dices
		}

		context.setPipCount(origDieValue);
		if (then() != null)
			for (int j = 0; j < returnMoves.moves().size(); j++)
				returnMoves.moves().get(j).then().add(then().moves());

		return returnMoves;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = handDiceIndexFn.gameFlags(game) | super.gameFlags(game);
		gameFlags |= rule.gameFlags(game);
		gameFlags |= moves.gameFlags(game);
		gameFlags |= replayDoubleFn.gameFlags(game);
		gameFlags |= combined.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.or(handDiceIndexFn.concepts(game));
		concepts.set(Concept.Dice.id(), true);
		concepts.set(Concept.ByDieMove.id(), true);
		concepts.or(rule.concepts(game));
		concepts.or(moves.concepts(game));
		concepts.or(replayDoubleFn.concepts(game));
		concepts.or(combined.concepts(game));

		concepts.set(Concept.ControlFlowStatement.id(), true);

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(handDiceIndexFn.writesEvalContextRecursive());
		writeEvalContext.or(rule.writesEvalContextRecursive());
		writeEvalContext.or(moves.writesEvalContextRecursive());
		writeEvalContext.or(replayDoubleFn.writesEvalContextRecursive());
		writeEvalContext.or(combined.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.PipCount.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(handDiceIndexFn.readsEvalContextRecursive());
		readEvalContext.or(rule.readsEvalContextRecursive());
		readEvalContext.or(moves.readsEvalContextRecursive());
		readEvalContext.or(replayDoubleFn.readsEvalContextRecursive());
		readEvalContext.or(combined.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (!game.hasHandDice())
		{
			game.addRequirementToReport("The ludeme (forEach Die ...) is used but the equipment has no dice.");
			missingRequirement = true;
		}
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= handDiceIndexFn.missingRequirement(game);
		missingRequirement |= rule.missingRequirement(game);
		missingRequirement |= moves.missingRequirement(game);
		missingRequirement |= replayDoubleFn.missingRequirement(game);
		missingRequirement |= combined.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= handDiceIndexFn.willCrash(game);
		willCrash |= rule.willCrash(game);
		willCrash |= moves.willCrash(game);
		willCrash |= replayDoubleFn.willCrash(game);
		willCrash |= combined.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		return handDiceIndexFn.isStatic() && rule.isStatic() && moves.isStatic() && replayDoubleFn.isStatic()
				&& combined.isStatic();
	}

	@Override
	public void preprocess(final Game game)
	{
		super.preprocess(game);
		handDiceIndexFn.preprocess(game);
		rule.preprocess(game);
		moves.preprocess(game);
		replayDoubleFn.gameFlags(game);
		combined.gameFlags(game);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toEnglish(final Game game)
	{
		return "ForEachDie";
	}
}
