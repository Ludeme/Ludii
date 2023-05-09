package game.rules.play.moves.nonDecision.effect.state.forget.value;

import java.util.ArrayList;
import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.types.state.GameType;
import gnu.trove.list.array.TIntArrayList;
import other.action.Action;
import other.action.state.ActionForgetValue;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

/**
 * Forgets all the values remembered before.
 * 
 * @author Eric.Piette
 */
@Hide
public final class ForgetValueAll extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The name of the remembering values. */
	private final String name;

	/**
	 * @param name The name of the remembering values.
	 * @param then The moves applied after that move is applied.
	 */
	public ForgetValueAll
	(
		@Opt final String name,
		@Opt final Then   then
	)
	{
		super(then);
		this.name = name;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());
		final Move move = new Move(new ArrayList<Action>());

		if (name != null)
		{
			final TIntArrayList rememberingValue = context.state().mapRememberingValues().get(name);

			if (rememberingValue != null)
			{
				for (int i = 0; i < rememberingValue.size(); i++)
				{
					final int value = rememberingValue.get(i);
					final ActionForgetValue action = new ActionForgetValue(name, value);
					move.actions().add(action);
				}
			}
		}
		else
		{
			final TIntArrayList rememberingValue = context.state().rememberingValues();

			if (rememberingValue != null)
			{
				for (int i = 0; i < rememberingValue.size(); i++)
				{
					final int value = rememberingValue.get(i);
					final ActionForgetValue action = new ActionForgetValue(name, value);
					move.actions().add(action);
				}
			}

			for (final String key : context.state().mapRememberingValues().keySet())
			{
				final TIntArrayList rememberingNameValue = context.state().mapRememberingValues().get(key);

				if (rememberingNameValue != null)
				{
					for (int i = 0; i < rememberingNameValue.size(); i++)
					{
						final int value = rememberingNameValue.get(i);
						final ActionForgetValue action = new ActionForgetValue(key, value);
						move.actions().add(action);
					}
				}
			}
		}


		if (!move.actions().isEmpty())
			moves.moves().add(move);

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
		long gameFlags = GameType.RememberingValues | super.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.ForgetValues.id(), true);

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
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);

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
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		return "forget all previously remembered values" + thenString;
	}
	
	//-------------------------------------------------------------------------
	
}