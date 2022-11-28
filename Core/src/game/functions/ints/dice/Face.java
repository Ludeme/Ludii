package game.functions.ints.dice;

import java.util.BitSet;

import game.Game;
import game.equipment.component.Component;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import main.Constants;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;

/**
 * Returns the face of the die according to the current state of the position of
 * the die.
 * 
 * @author  Eric Piette
 */
public final class Face extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which location. */
	private final IntFunction locn;

	//-------------------------------------------------------------------------

	/**
	 * @param locn The location of the die.
	 * @example (face (handSite P1))
	 */
	public Face
	(
		final IntFunction locn
	)
	{
		this.locn = locn;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final int loc = locn.eval(context);
		if (loc == Constants.OFF || context.containerId().length >= loc)
			return Constants.OFF;

		final int containerId = context.containerId()[loc];
		final ContainerState cs = context.state().containerStates()[containerId];

		final int what = cs.whatCell(loc);
		if (what < 1)
			return Constants.OFF;

		final Component component = context.components()[what];
		if(!component.isDie())
			return Constants.OFF;

		final int state = cs.stateCell(loc);
		if (state < 0)
			return Constants.OFF;

		return component.getFaces()[state];
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		// we're looking at state in a specific context, so not static
		return false;

		// return site.isStatic() && level.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		final long stateFlag = locn.gameFlags(game);
		return stateFlag;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(locn.concepts(game));
		concepts.set(Concept.Dice.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(locn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(locn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		locn.preprocess(game);
	}
	
	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (!game.hasHandDice())
		{
			game.addRequirementToReport("The ludeme (face ...) is used but the equipment has no dice.");
			missingRequirement = true;
		}
		missingRequirement |= locn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= locn.willCrash(game);
		return willCrash;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "the face of the die at site " + locn.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------
}
