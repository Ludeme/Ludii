package game.functions.ints.card.site;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.equipment.component.Card;
import game.equipment.component.Component;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.types.board.SiteType;
import game.types.state.GameType;
import main.Constants;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;

/**
 * Returns the suit of a card.
 * 
 * @author	Eric.Piette
 */
@Hide
public final class CardSuit extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The site to check. */
	private final IntFunction siteFn;
	
	/** The level on the site. */
	private final IntFunction levelFn;

	/**
	 * @param site  The site where the card is.
	 * @param level The level where the card is.
	 */
	public CardSuit
	(
			 final IntFunction site,
		@Opt final IntFunction level
	)
	{
		this.siteFn = site;
		this.levelFn = (level == null) ? null : level;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final int site = siteFn.eval(context);
		final int cid = context.containerId()[site];
		final ContainerState cs = context.containerState(cid);
		final int what = (levelFn != null) ? cs.what(site, levelFn.eval(context), SiteType.Cell)
				: cs.what(site, SiteType.Cell);

		if (what < 1)
			return Constants.OFF;

		final Component component = context.components()[what];

		if (!component.isCard())
			return Constants.OFF;

		return ((Card)component).suit();
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		if (!siteFn.isStatic())
			return false;

		if (levelFn != null && !levelFn.isStatic())
			return false;

		return true;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = GameType.Card | GameType.HiddenInfo | siteFn.gameFlags(game);

		if (levelFn != null)
			gameFlags |= levelFn.gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(siteFn.concepts(game));
		concepts.set(Concept.Card.id(), true);

		if (levelFn != null)
			concepts.or(levelFn.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(siteFn.writesEvalContextRecursive());
		if (levelFn != null)
			writeEvalContext.or(levelFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(siteFn.readsEvalContextRecursive());
		if (levelFn != null)
			readEvalContext.or(levelFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		siteFn.preprocess(game);
		if (levelFn != null)
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
			game.addRequirementToReport("The ludeme (card Suit ...) is used but the equipment has no cards.");
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
}
