package game.rules.play.moves.nonDecision.effect.set.site;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.ints.IntFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.types.board.SiteType;
import game.types.state.GameType;
import main.Constants;
import other.action.BaseAction;
import other.action.state.ActionSetState;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

/**
 * Sets the local state of a location.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SetState extends Effect
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	/** The sites. */
	private final IntFunction siteFn;

	/** The level. */
	private final IntFunction levelFn;

	/** The value. */
	private final IntFunction state;

	/** Cell/Edge/Vertex. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param type  The graph element type [default SiteType of the board].
	 * @param site  The site to modify the local state.
	 * @param level The level to modify the local state.
	 * @param state The new local state.
	 * @param then  The moves applied after that move is applied.
	 */
	public SetState
	(
		@Opt       final SiteType    type,
			 @Name final IntFunction site,
		@Opt       final IntFunction level,
			       final IntFunction state,
		@Opt       final Then        then
	)
	{
		super(then);
		siteFn = site;
		this.state = state;
		this.type = type;
		levelFn = level;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Moves moves = new BaseMoves(super.then());
		final int stateValue = state.eval(context);
		final int level = levelFn == null ? Constants.UNDEFINED : levelFn.eval(context);
		if (stateValue < 0 || level < Constants.UNDEFINED)
			return moves;

		final int site = siteFn.eval(context);
		
		final int cid = site >= context.containerId().length ? 0 : context.containerId()[site];
		SiteType realType = type;
		if (cid > 0)
			realType = SiteType.Cell;
		else if (realType == null)
			realType = context.board().defaultSite();
		if(cid == 0)
		{
			if(site >= context.containers()[0].topology().getGraphElements(realType).size())
				return moves;
		}
		else
		{
			if((site - context.sitesFrom()[cid]) >= context.containers()[cid].topology().getGraphElements(SiteType.Cell).size())
				return moves;
		}
		
		final BaseAction action = new ActionSetState(realType, site, level, stateValue);
		final Move move = new Move(action);
		moves.moves().add(move);

		if (then() != null)
			for (int j = 0; j < moves.moves().size(); j++)
				moves.moves().get(j).then().add(then().moves());

		// Store the Moves in the computed moves.
		for (int j = 0; j < moves.moves().size(); j++)
			moves.moves().get(j).setMovesLudeme(this);

		return moves;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = GameType.SiteState | siteFn.gameFlags(game) | state.gameFlags(game) | super.gameFlags(game);

		gameFlags |= SiteType.gameFlags(type);

		if (levelFn != null)
			gameFlags |= levelFn.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);
		
		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.or(siteFn.concepts(game));
		if (levelFn != null)
			concepts.or(levelFn.concepts(game));
		concepts.or(SiteType.concepts(type));
		concepts.or(state.concepts(game));
		concepts.set(Concept.SiteState.id(), true);
		concepts.set(Concept.SetSiteState.id(), true);

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		if (levelFn != null)
			writeEvalContext.or(levelFn.writesEvalContextRecursive());
		writeEvalContext.or(siteFn.writesEvalContextRecursive());
		writeEvalContext.or(state.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		if (levelFn != null)
			readEvalContext.or(levelFn.readsEvalContextRecursive());
		readEvalContext.or(siteFn.readsEvalContextRecursive());
		readEvalContext.or(state.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		if (levelFn != null)
			missingRequirement |= levelFn.missingRequirement(game);
		missingRequirement |= siteFn.missingRequirement(game);
		missingRequirement |= state.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		if (levelFn != null)
			willCrash |= levelFn.willCrash(game);
		willCrash |= siteFn.willCrash(game);
		willCrash |= state.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		if (levelFn != null && !levelFn.isStatic())
			return false;

		return siteFn.isStatic() && state.isStatic();
	}
	
	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		super.preprocess(game);
		if (levelFn != null)
			levelFn.preprocess(game);
		siteFn.preprocess(game);
		state.preprocess(game);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString() 
	{
		return "SetState [siteFn=" + siteFn + ", state=" + state + "then=" + then() + "]";
	}
	
	@Override
	public String toEnglish(final Game game)
	{
		final SiteType realType = (type != null) ? type : game.board().defaultSite();
		
		String siteString = "";
		if (siteFn != null)
			siteString = " " + siteFn.toEnglish(game);
		
		String levelString = "";
		if (levelFn != null)
			levelString = " (level " + levelFn.toEnglish(game) + ")";
		
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		return "set the state of the " + realType.name() + siteString + levelString + " to " + state.toEnglish(game) + thenString;
	}
}
