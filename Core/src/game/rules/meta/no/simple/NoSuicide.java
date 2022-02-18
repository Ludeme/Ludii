package game.rules.meta.no.simple;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.rules.meta.MetaRule;
import other.MetaRules;
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
@Hide
public class NoSuicide extends MetaRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 */
	public NoSuicide()
	{
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void eval(final Context context)
	{
		context.game().metaRules().setNoSuicide(true);
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
		if (metaRules.usesNoSuicide())
		{
			final Context newContext = new TempContext(context);
			game.applyInternal(newContext, move, false);
			final boolean moverActive = newContext.active(context.state().mover());
			if(moverActive)
				return true;
			else
				return !newContext.losers().contains(context.state().mover());
		}
		
		return true;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0l;
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
		return concepts;
	}

	@Override
	public int hashCode()
	{
		int result = 1;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof NoSuicide))
			return false;

		return true;
	}
}