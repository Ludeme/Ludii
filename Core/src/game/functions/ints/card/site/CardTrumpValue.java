package game.functions.ints.card.site;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.equipment.component.Component;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.types.state.GameType;
import main.Constants;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;

/**
 * Returns the trump value of a card.
 * 
 * @author Eric.Piette
  */
@Hide
public final class CardTrumpValue extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The site to check. */
	private final IntFunction siteFn;

	/** The level on the site. */
	private final IntFunction levelFn;

	/**
	 * @param site  The site where the card is.
	 * @param level The level where the card is [0].
	 */
	public CardTrumpValue
	(
			 final IntFunction site,
		@Opt final IntFunction level
	)
	{
		siteFn = site;
		levelFn = (level == null) ? new IntConstant(0) : level;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final int site = siteFn.eval(context);
		final int level = levelFn.eval(context);
		final int cid = context.containerId()[site];
		final ContainerState cs = context.containerState(cid);
		final int what = cs.whatCell(site, level);

		if (what < 1)
			return Constants.OFF;

		final Component component = context.components()[what];

		if (!component.isCard())
			return Constants.OFF;

		return component.trumpValue();
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return siteFn.isStatic() && levelFn.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		return GameType.Card | GameType.HiddenInfo | siteFn.gameFlags(game) | levelFn.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(siteFn.concepts(game));
		concepts.or(levelFn.concepts(game));
		concepts.set(Concept.Card.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(siteFn.writesEvalContextRecursive());
		writeEvalContext.or(levelFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(siteFn.readsEvalContextRecursive());
		readEvalContext.or(levelFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		siteFn.preprocess(game);
		levelFn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		boolean gameHasCard = false;
		for (int i = 1; i < game.equipment().components().length; i++)
		{
			final Component component = game.equipment().components()[i];
			if (component.isCard())
			{
				gameHasCard = true;
				break;
			}

		}

		if (!gameHasCard)
		{
			game.addRequirementToReport("The ludeme (card TrumpValue ...) is used but the equipment has no cards.");
			missingRequirement = true;
		}
		missingRequirement |= siteFn.missingRequirement(game);
		missingRequirement |= levelFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= siteFn.willCrash(game);
		willCrash |= levelFn.willCrash(game);
		return willCrash;
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "the trump value of the card at " + siteFn;
	}
}
