package game.functions.booleans.is.integer;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntFunction;
import game.types.state.GameType;
import other.concept.Concept;
import other.context.Context;

/**
 * Returns true if a site was already visited by a player in the same sequence
 * of their turn.
 * 
 * @author Eric.Piette
 * @remarks This is used to avoid visiting a site more than once during a
 *          sequence of moves.
 */
@Hide
public final class IsVisited extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The site to check. */
	private final IntFunction siteId;

	//-------------------------------------------------------------------------

	/**
	 * @param site The site to check.
	 */
	public IsVisited(final IntFunction site)
	{
		siteId = site;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		return context.state().isVisited(siteId.eval(context));
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "Visited(" + siteId + ")";
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return siteId.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = GameType.Visited;

		if (siteId != null)
			gameFlags |= siteId.gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.VisitedSites.id(), true);

		if (siteId != null)
			concepts.or(siteId.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		if (siteId != null)
			writeEvalContext.or(siteId.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		if (siteId != null)
			readEvalContext.or(siteId.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		siteId.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= siteId.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= siteId.willCrash(game);
		return willCrash;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "site " + siteId.toEnglish(game) + " has already been visited";
	}
	
	//-------------------------------------------------------------------------
}
