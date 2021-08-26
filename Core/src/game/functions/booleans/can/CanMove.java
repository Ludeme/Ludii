package game.functions.booleans.can;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.rules.play.moves.Moves;
import other.concept.Concept;
import other.context.Context;
import other.context.TempContext;

/**
 * Checks if a list of moves is not empty.
 * 
 * @author Eric.Piette
 */
@Hide
public class CanMove extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	/** The moves to check. */
	private final Moves moves;

	//-------------------------------------------------------------------------

	/**
	 * @param moves The list of moves.
	 */
	public CanMove
	(
		final Moves moves
	)
	{
		this.moves = moves;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		if(moves != null)
			return "can move " + moves.toEnglish(game);
		else
			return "can move";
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		if (context.game().requiresVisited())
		{
			final Context newContext = new TempContext(context);
			final int from = newContext.trial().lastMove().fromNonDecision();
			final int to = newContext.trial().lastMove().toNonDecision();
			newContext.state().visit(from);
			newContext.state().visit(to);
			return moves.canMove(newContext);
		}
		return moves.canMove(context);
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		return moves.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(moves.concepts(game));
		concepts.set(Concept.CanMove.id(), true);
		if (game.requiresVisited())
			concepts.set(Concept.CopyContext.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(moves.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(moves.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean isStatic()
	{
		return moves.isStatic();
	}
	
	@Override
	public void preprocess(final Game game)
	{
		moves.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= moves.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= moves.willCrash(game);
		return willCrash;
	}
}
