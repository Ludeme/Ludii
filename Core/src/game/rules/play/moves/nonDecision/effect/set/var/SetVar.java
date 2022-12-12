package game.rules.play.moves.nonDecision.effect.set.var;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.types.state.GameType;
import main.Constants;
import other.action.state.ActionSetTemp;
import other.action.state.ActionSetVar;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

/**
 * Stores an integer in the state in the variable "var".
 * 
 * @author Eric.Piette
 */
@Hide
public final class SetVar extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The value to set. */
	private final IntFunction value;

	/** The name of the var */
	private final String name;

	/**
	 * @param name  The name of the var.
	 * @param value The value to store in the context [-1].
	 * @param then  The moves applied after that move is applied.
	 */
	public SetVar
	(
	    @Opt final String      name,
	    @Opt final IntFunction value,
		@Opt final Then        then
	)
	{
		super(then);
		this.value = (value == null) ? new IntConstant(Constants.UNDEFINED) : value;
		this.name = name;
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());
		final Move move;

		if (name == null)
		{
			final ActionSetTemp actionTemp = new ActionSetTemp(value.eval(context));
			move = new Move(actionTemp);
			moves.moves().add(move);
		}
		else
		{
			final ActionSetVar actionSetVar = new ActionSetVar(name, value.eval(context));
			move = new Move(actionSetVar);
			moves.moves().add(move);
		}

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
		long gameFlags = GameType.MapValue | super.gameFlags(game);
		if (value != null)
			gameFlags |= value.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));

		if (value != null)
			concepts.or(value.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		concepts.set(Concept.Variable.id(), true);
		concepts.set(Concept.SetVar.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());

		if (value != null)
			writeEvalContext.or(value.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());

		if (value != null)
			readEvalContext.or(value.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);

		if (value != null)
			missingRequirement |= value.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);

		if (value != null)
			willCrash |= value.willCrash(game);

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
		if (value != null)
			value.preprocess(game);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		return "set the variable " + name + " to " + value.toEnglish(game) + thenString;
	}
	
	//-------------------------------------------------------------------------

}
