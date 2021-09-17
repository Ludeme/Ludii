package game.rules.play.moves.nonDecision.effect;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import annotations.Opt;
import game.Game;
import game.equipment.container.other.Dice;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.types.state.GameType;
import main.Constants;
import other.action.Action;
import other.action.die.ActionSetDiceAllEqual;
import other.action.die.ActionUpdateDice;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

/**
 * Rolls the dice.
 * 
 * @author Eric.Piette
 */
public final class Roll extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @param then The moves applied after that move is applied.
	 * 
	 * @example (roll)
	 */
	public Roll
	(
		@Opt final Then then
	)
	{ 
		super(then);
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());
		
		boolean allEqual = true;
		int valueToCompare = Constants.UNDEFINED;

		final List<Action> actions = new ArrayList<Action>();
		for (final Dice dice: context.game().handDice())
		{
			for (int loc = context.sitesFrom()[dice.index()]; loc < context.sitesFrom()[dice.index()]
					+ dice.numLocs(); loc++)
			{
				final int what = context.containerState(dice.index()).what(loc, SiteType.Cell);
				final int newValue = context.components()[what].roll(context);

				if (valueToCompare == Constants.UNDEFINED)
					valueToCompare = newValue;
				else if (valueToCompare != newValue)
					allEqual = false;

				actions.add(new ActionUpdateDice(loc, newValue));
			}
		}

		actions.add(new ActionSetDiceAllEqual(allEqual));
		moves.moves().add(new Move(actions));

		if (then() != null)
			for (int j = 0; j < moves.moves().size(); j++)
				moves.moves().get(j).then().add(then().moves());

		// Store the Moves in the computed moves.
		for (int j = 0; j < moves.moves().size(); j++)
			moves.moves().get(j).setMovesLudeme(this);

		return moves;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public boolean canMoveTo(final Context context, final int target)
	{
		return false;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = GameType.Stochastic | GameType.SiteState | super.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.Roll.id(), true);

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
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
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		if (!game.hasHandDice())
		{
			game.addRequirementToReport("The ludeme (roll) is used but the equipment has no dice.");
			return true;
		}

		return false;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		return "roll the dice" + thenString;
	}
	
	//-------------------------------------------------------------------------

}
