package game.rules.meta;

import java.util.BitSet;

import annotations.Opt;
import game.Game;
import game.types.play.RepetitionType;
import game.types.state.GameType;
import other.MetaRules;
import other.concept.Concept;
import other.context.Context;
import other.context.TempContext;
import other.move.Move;

/**
 * Specifies a particular type of repetition that is forbidden in the game.
 * 
 * @author Eric.Piette
 * 
 * @remarks The Infinite option disallows players from making consecutive
 *          sequences of moves that would lead to the same state twice, which
 *          would indicate the start of an infinite cycle of moves.
 */
public class NoRepeat extends MetaRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	private final RepetitionType type;

	//-------------------------------------------------------------------------

	/**
	 * @param type Type of repetition to forbid [Positional].
	 * 
	 * @example (noRepeat PositionalInTurn)
	 */
	public NoRepeat
	(
		@Opt final RepetitionType type
	)
	{
		this.type = (type == null) ? RepetitionType.Positional : type;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void eval(final Context context)
	{
		context.game().metaRules().setRepetitionType(type);
	}
	
	/**
	 * @param context The context.
	 * @param move    The move to check.
	 * @return True if the given move does not reach a state previously reached.
	 */
	public static boolean apply(final Context context, final Move move)
	{
		final Game game = context.game();
		final MetaRules metaRules = game.metaRules();
		
		final RepetitionType type = metaRules.repetitionType();
		if (type != null)
		{
			if (move.isPass())
				return true;
			
			final Context newContext = new TempContext(context);
			move.apply(newContext, true);
			switch (type)
			{
				case PositionalInTurn:
					return !context.trial().previousStateWithinATurn().contains(newContext.state().stateHash());
				case SituationalInTurn:
					return !context.trial().previousStateWithinATurn().contains(newContext.state().fullHash());
				case Positional:
					return !context.trial().previousState().contains(newContext.state().stateHash());
				case Situational:
					return !context.trial().previousState().contains(newContext.state().fullHash());
				default:
					break;
			}
		}
		return true;
	}

	//-------------------------------------------------------------------------

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
	public boolean isStatic()
	{
		return true;
	}

	@Override
	public void preprocess(final Game game)
	{
		// Nothing to do.
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.CopyContext.id(), true);
		concepts.set(Concept.PositionalSuperko.id(), true);
		return concepts;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + type.hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof NoRepeat))
			return false;

		final NoRepeat other = (NoRepeat) obj;
		return type.equals(other.type);
	}
}