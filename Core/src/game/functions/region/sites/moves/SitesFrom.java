package game.functions.region.sites.moves;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.region.BaseRegionFunction;
import game.rules.play.moves.Moves;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import other.context.Context;
import other.move.Move;

/**
 * Returns the ``from'' sites of a set of moves.
 * 
 * @author Dennis Soemers and Eric Piette
 */
@Hide
public final class SitesFrom extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The moves from which to take from-sites. */
	private final Moves moves;

	//-------------------------------------------------------------------------

	/**
	 * @param moves The moves from which to take from-sites.
	 */
	public SitesFrom(final Moves moves)
	{
		this.moves = moves;
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		final TIntArrayList sites = new TIntArrayList();
		final Moves generatedMoves = moves.eval(context);
		
		for (final Move m : generatedMoves.moves())
			sites.add(m.fromNonDecision());
		
		return new Region(sites.toArray());
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return moves.isStatic();
	}

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
