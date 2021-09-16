package game.rules.play.moves.nonDecision.effect.state.remember.value;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.types.state.GameType;
import gnu.trove.list.array.TIntArrayList;
import other.action.state.ActionRememberValue;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

/**
 * Remembers a value (in storing it in the state).
 * 
 * @author Eric.Piette
 */
@Hide
public final class RememberValue extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The value to remember. */
	private final IntFunction value;
	
	/** If True we remember it only if not only remembered. */
	private final BooleanFunction uniqueFn;

	/** The name of the remembering values. */
	private final String name;

	/**
	 * @param name   The name of the remembering values.
	 * @param value  The value to remember.
	 * @param unique If True we remember a value only if not already remembered
	 *               [False].
	 * @param then   The moves applied after that move is applied.
	 */
	public RememberValue
	(
		@Opt       final String          name,
			       final IntFunction     value,
		@Opt @Name final BooleanFunction unique,
		@Opt       final Then            then
	)
	{
		super(then);
		this.value = value;
		uniqueFn = (unique == null) ? new BooleanConstant(false) : unique;
		this.name = name;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());
		final int valueToRemember = value.eval(context);
		final boolean hasToBeUnique = uniqueFn.eval(context);
		boolean isUnique = true;
		
		if(hasToBeUnique)
		{
			final TIntArrayList valuesInMemory = (name == null) ? context.state().rememberingValues()
					: context.state().mapRememberingValues().get(name);
			if (valuesInMemory != null)
				for (int i = 0; i < valuesInMemory.size(); i++)
				{
					final int valueInMemory = valuesInMemory.get(i);
					if (valueInMemory == valueToRemember)
					{
						isUnique = false;
						break;
					}
				}
		}

		if(!hasToBeUnique || (hasToBeUnique && isUnique))
		{
			final ActionRememberValue action = new ActionRememberValue(name, valueToRemember);
			final Move move = new Move(action);
			moves.moves().add(move);
	
			if (then() != null)
				for (int j = 0; j < moves.moves().size(); j++)
					moves.moves().get(j).then().add(then().moves());
		}

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
		long gameFlags = GameType.RememberingValues | value.gameFlags(game) | uniqueFn.gameFlags(game) | super.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(value.concepts(game));
		concepts.or(uniqueFn.concepts(game));
		concepts.or(super.concepts(game));
		concepts.set(Concept.Variable.id(), true);
		concepts.set(Concept.RememberValues.id(), true);

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(value.writesEvalContextRecursive());
		writeEvalContext.or(super.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(value.readsEvalContextRecursive());
		readEvalContext.or(super.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= value.missingRequirement(game);
		missingRequirement |= uniqueFn.missingRequirement(game);
		missingRequirement |= super.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= value.willCrash(game);
		willCrash |= uniqueFn.willCrash(game);
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
		value.preprocess(game);
		uniqueFn.preprocess(game);
		super.preprocess(game);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "remember the value " + value.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------
	
}