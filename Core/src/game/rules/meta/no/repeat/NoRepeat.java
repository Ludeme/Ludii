package game.rules.meta.no.repeat;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.rules.meta.MetaRule;
import game.types.play.RepetitionType;
import game.types.state.GameType;
import other.MetaRules;
import other.concept.Concept;
import other.context.Context;
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
@Hide
public class NoRepeat extends MetaRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	private final RepetitionType type;

	//-------------------------------------------------------------------------

	/**
	 * @param type Type of repetition to forbid [Positional].
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

			context.storeCurrentData(); // We need to save the data before to apply the move.
			
//			final String stacktraceString = Utilities.stackTraceString();
//			if 
//			(
//				!stacktraceString.contains("getMoveStringToDisplay") 
//				&& 
//				!stacktraceString.contains("other.context.InformationContext.moves") 
//				&& 
//				!stacktraceString.contains("game.rules.play.moves.Moves$1.canMoveConditionally(Moves.java:305)")
//			)
//			{
//				System.out.println("Applying move: " + move);
//			}
			final Move moveApplied = (Move) move.apply(context, true);
			boolean noRepeat = true;
			switch (type)
			{
				case PositionalInTurn:
				{
					noRepeat = !context.trial().previousStateWithinATurn().contains(context.state().stateHash());
					break;
				}
				case SituationalInTurn:
				{
					noRepeat = !context.trial().previousStateWithinATurn().contains(context.state().fullHash());
					break;
				}
				case Positional:
				{
					noRepeat = !context.trial().previousState().contains(context.state().stateHash());
					break;
				}
				case Situational:
				{
					noRepeat = !context.trial().previousState().contains(context.state().fullHash());
					break;
				}
				default:
					break;
			}

//			if 
//			(
//				!stacktraceString.contains("getMoveStringToDisplay") 
//				&& 
//				!stacktraceString.contains("other.context.InformationContext.moves") 
//				&& 
//				!stacktraceString.contains("game.rules.play.moves.Moves$1.canMoveConditionally(Moves.java:305)")
//			)
//			{
//				System.out.println("Undoing move: " + move);
//			}
			moveApplied.undo(context, true); // We undo the move.
			return noRepeat;
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
		if (type == RepetitionType.PositionalInTurn)
			concepts.set(Concept.TurnKo.id(), true);
		else if (type == RepetitionType.Positional)
			concepts.set(Concept.PositionalSuperko.id(), true);
		else if (type == RepetitionType.SituationalInTurn)
			concepts.set(Concept.SituationalTurnKo.id(), true);
		else if (type == RepetitionType.Situational)
			concepts.set(Concept.SituationalSuperko.id(), true);
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