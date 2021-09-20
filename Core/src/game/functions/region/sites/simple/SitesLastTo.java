package game.functions.region.sites.simple;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.region.BaseRegionFunction;
import game.rules.play.moves.Moves;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import other.action.Action;
import other.context.Context;
import other.move.Move;

/**
 * Returns all the "to" positions of the last move.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SitesLastTo extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * 
	 */
	public SitesLastTo()
	{
		// Nothing to do.
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		final Move lastMove = context.trial().lastMove();
		final TIntArrayList allToMove = new TIntArrayList();
		allToMove.add(lastMove.toNonDecision());

		for (final Action action : lastMove.actions())
		{
			final int to = action.to();
			if (!allToMove.contains(to) && to < context.board().numSites() && to >= 0)
				allToMove.add(to);
		}

		for (final Moves moves : lastMove.then())
		{
			moves.eval(context);
			for (final Move m : moves.moves())
			{
				final int to = m.toNonDecision();
				if (!allToMove.contains(to) && to >= 0)
					allToMove.add(to);
			}
		}

		return new Region(allToMove.toArray());
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		return 0L;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
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
		// nothing todo
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "the last to sites of the board";
	}
	
	//-------------------------------------------------------------------------
}
