package game.functions.booleans.is.repeat;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.types.play.RepetitionType;
import game.types.state.GameType;
import other.concept.Concept;
import other.context.Context;

/**
 * Returns true if a location is under threat for one specific player.
 * 
 * @author Eric.Piette
 * @remarks Used to avoid being under threat, for example to know if the king is
 *          check.
 */
@Hide
public final class IsRepeat extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The repetitionType. */
	private final RepetitionType type;
	
	/**
	 * @param type The component.
	 */
	public IsRepeat 
	(
		@Opt final RepetitionType type
	)
	{
		this.type = (type == null) ? RepetitionType.Positional : type;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{

		switch (type)
		{
		case PositionalInTurn:
		{
			final long hashState = context.state().stateHash();
			return context.trial().previousStateWithinATurn().contains(hashState);
		}
		case SituationalInTurn:
		{
			final long hashState = context.state().fullHash();
			return context.trial().previousStateWithinATurn().contains(hashState);
		}
		case Positional:
		{
			final long hashState = context.state().stateHash();
			return context.trial().previousState().contains(hashState);
		}
		case Situational:
		{
			final long hashState = context.state().fullHash();
			return context.trial().previousState().contains(hashState);
		}
		default:
			break;
		}

		return false;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "IsRepeat(" + type + ")";
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0l;

		if (type == RepetitionType.Positional)
			gameFlags |= GameType.RepeatPositionalInGame;

		if (type == RepetitionType.PositionalInTurn)
			gameFlags |= GameType.RepeatPositionalInTurn;

		if (type == RepetitionType.Situational)
			gameFlags |= GameType.RepeatSituationalInGame;

		if (type == RepetitionType.SituationalInTurn)
			gameFlags |= GameType.RepeatSituationalInTurn;

		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.PositionalSuperko.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		// Nothing to do.
	}
	
}
