package game.functions.booleans.is.Hidden;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.ints.board.Id;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.types.state.GameType;
import game.util.moves.Player;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;

/**
 * Checks if the piece index is hidden to a player.
 * 
 * @author Eric.Piette
 */
@Hide
public final class IsHiddenWhat extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which site. */
	private final IntFunction siteFn;

	/** Level. */
	private final IntFunction levelFn;

	/** The player to get the hidden information. */
	private final IntFunction whoFn;

	/** Cell/Edge/Vertex. */
	private final SiteType type;

	/** Precomputed boolean. */
	private Boolean precomputedBoolean;

	//-------------------------------------------------------------------------

	/**
	 * For checking the hidden information about the piece index at a location for a
	 * specific player.
	 * 
	 * @param type  The graph element type [default of the board].
	 * @param at    The site to set the hidden information.
	 * @param level The level to set the hidden information [0].
	 * @param to    The player with these hidden information.
	 * @param To    The roleType with these hidden information.
	 */
	public IsHiddenWhat
	(
		final SiteType    type, 
		final IntFunction at,
		final IntFunction level,
		final Player      to,
		final RoleType    To
	)
	{
		 this.type = type;
		 this.siteFn = at;
		 this.levelFn = (level == null) ? new IntConstant(0) : level;
		 this.whoFn = (to == null && To == null) ? null : To != null ? new Id(null, To) : to.originalIndex();
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		if (precomputedBoolean != null)
			return precomputedBoolean.booleanValue();

		final int site = siteFn.eval(context);

		if (site < 0)
			return false;

		final int containerId = context.containerId()[site];
		final ContainerState cs = context.state().containerStates()[containerId];
		final int level = levelFn.eval(context);
		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
		final int who = whoFn.eval(context);

		return cs.isHiddenWhat(who, site, level, realType);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return this.siteFn.isStatic() && this.levelFn.isStatic() && whoFn.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = siteFn.gameFlags(game) | levelFn.gameFlags(game) | whoFn.gameFlags(game) | GameType.HiddenInfo;

		gameFlags |= SiteType.gameFlags(type);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(siteFn.concepts(game));
		concepts.or(levelFn.concepts(game));
		concepts.or(whoFn.concepts(game));
		concepts.set(Concept.HiddenInformation.id(), true);
		concepts.set(Concept.HidePieceType.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(siteFn.writesEvalContextRecursive());
		writeEvalContext.or(levelFn.writesEvalContextRecursive());
		writeEvalContext.or(whoFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(siteFn.readsEvalContextRecursive());
		readEvalContext.or(levelFn.readsEvalContextRecursive());
		readEvalContext.or(whoFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		levelFn.preprocess(game);
		siteFn.preprocess(game);
		whoFn.preprocess(game);

		if (isStatic())
			precomputedBoolean = Boolean.valueOf(eval(new Context(game, null)));
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= levelFn.missingRequirement(game);
		missingRequirement |= siteFn.missingRequirement(game);
		missingRequirement |= whoFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= levelFn.willCrash(game);
		willCrash |= siteFn.willCrash(game);
		willCrash |= whoFn.willCrash(game);
		return willCrash;
	}
}
