package game.rules.play.moves.nonDecision.effect;

import java.util.BitSet;

import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.moves.From;
import game.util.moves.To;
import main.Constants;
import other.IntArrayFromRegion;
import other.action.move.ActionSelect;
import other.context.Context;
import other.context.EvalContextData;
import other.move.Move;
import other.state.container.ContainerState;

/**
 * Selects either a site or a pair of ``from'' and ``to'' locations.
 * 
 * @author Eric.Piette
 * 
 * @remarks This ludeme is used to select one or two sites in order to apply a 
 *          consequence to them. If the ``to'' location is not specified, then it 
 *          is assumed to be the same as the 'from' location.
 */
public final class Select extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which region. */
	private final IntArrayFromRegion region;

	/** Condition to select. */
	private final BooleanFunction condition;

	/** Which region To. */
	private final IntArrayFromRegion regionTo;

	/** Condition to select. */
	private final BooleanFunction conditionTo;

	/** Select From Cell/Edge/Vertex. */
	private SiteType typeFrom;
	
	/** Level From Select */
	private final IntFunction levelFromFn;

	/** Select To Cell/Edge/Vertex. */
	private SiteType typeTo;

	/** Level To Select */
	private final IntFunction levelToFn;

	/** Does our game involve stacking? */
	private boolean gameUsesStacking = false;

	//-------------------------------------------------------------------------

	/**
	 * @param from Describes the ``from'' location to select [(from)].
	 * @param to   Describes the ``to'' location to select.
	 * @param then The moves applied after that move is applied.
	 * 
	 * @example (select (from) (then (remove (last To))))
	 * 
	 * @example (select (from (sites Occupied by:Mover) if:(!= (state at:(to)) 0) )
	 *          (to (sites Occupied by:Next) if:(!= (state at:(to)) 0) ) (then (set
	 *          State at:(last To) (% (+ (state at:(last From)) (state at:(last
	 *          To))) 5) ) ) )
	 */
	public Select
	(
			 final From from,
		@Opt final To   to,
		@Opt final Then then
	) 
	{ 
		super(then);

		this.region = new IntArrayFromRegion(from.loc(), from.region());
		
		if (to == null)
			this.regionTo = null;
		else if (to.region() != null)
			this.regionTo = new IntArrayFromRegion(null, to.region());
		else if (to.loc() != null)
			this.regionTo = new IntArrayFromRegion(to.loc(), null);
		else
			this.regionTo = null;

		this.condition = (from.cond() == null) ? new BooleanConstant(true) : from.cond();
		this.conditionTo = (to == null || to.cond() == null) ? new BooleanConstant(true) : to.cond();
		this.typeFrom = from.type();
		this.typeTo = (to != null) ? to.type() : null;
		this.levelToFn = (to != null && to.level() != null) ? to.level() : null;
		this.levelFromFn = (from.level() != null) ? from.level() : null;
	}
 
	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final BaseMoves moves = new BaseMoves(super.then());

		final int[] sites;
		sites = region.eval(context);

		final int origTo = context.to();
		final int origFrom = context.from();

		for (final int site : sites)
		{
			int cid = 0;

			if (typeFrom == SiteType.Cell)
			{
				if (site >= context.containerId().length)
					continue;
				cid = context.containerId()[site];
			}
			else
			{
				if (site >= context.topology().getGraphElements(typeFrom).size())
					continue;
			}

			final ContainerState cs = context.containerState(cid);
			final int levelFrom = !gameUsesStacking ? Constants.UNDEFINED
					: (levelFromFn != null) ? levelFromFn.eval(context) : cs.sizeStack(site, typeFrom) - 1;

			if (site == Constants.UNDEFINED)
				continue;

			context.setFrom(site);
			context.setTo(site);
			if (condition.eval(context))
			{
				if (regionTo == null)
				{
					final ActionSelect ActionSelect = new ActionSelect(typeFrom, site, levelFrom, null,
							Constants.UNDEFINED,
							levelFrom);
					if (isDecision())
						ActionSelect.setDecision(true);
					final Move action = new Move(ActionSelect);
					action.setFromNonDecision(site);
					action.setToNonDecision(site);
					action.setMover(context.state().mover());
					moves.moves().add(action);
				}
				else
				{
					final int[] sitesTo = regionTo.eval(context);
					for (final int siteTo : sitesTo)
					{
						if (siteTo < 0)
							continue;

						int cidTo = 0;

						if (typeTo == SiteType.Cell)
						{
							if (siteTo >= context.containerId().length)
								continue;
							cidTo = context.containerId()[siteTo];
						}
						else
						{
							if (siteTo >= context.topology().getGraphElements(typeTo).size())
								continue;
						}

						final ContainerState csTo = context.containerState(cidTo);

						final int levelTo = levelToFn != null ? levelToFn.eval(context)
								: csTo.sizeStack(siteTo, typeTo) - 1;

						//context.setFrom(site);
						context.setTo(siteTo);

						if (conditionTo.eval(context))
						{
							final ActionSelect ActionSelect = new ActionSelect(typeFrom, site, levelFrom, typeTo,
									siteTo,
									levelTo);
							if (isDecision())
								ActionSelect.setDecision(true);
							final Move action = new Move(ActionSelect);
							action.setFromNonDecision(site);
							action.setToNonDecision(siteTo);
							action.setMover(context.state().mover());
							moves.moves().add(action);
						}

						//context.setFrom(origFrom);
					}
				}
			}
		}
		
		if (then() != null)
			for (int j = 0; j < moves.moves().size(); j++)
				moves.moves().get(j).then().add(then().moves());
				
		context.setTo(origTo);
		context.setFrom(origFrom);

		// Store the Moves in the computed moves.
		for (int j = 0; j < moves.moves().size(); j++)
			moves.moves().get(j).setMovesLudeme(this);

		return moves;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = super.gameFlags(game) | region.gameFlags(game) | condition.gameFlags(game) | conditionTo.gameFlags(game);
		
		if (regionTo != null)
		{
			gameFlags |= regionTo.gameFlags(game);
			gameFlags |= GameType.UsesFromPositions;
		}

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		gameFlags |= SiteType.gameFlags(typeFrom);
		gameFlags |= SiteType.gameFlags(typeTo);

		if (levelFromFn != null)
			gameFlags |= levelFromFn.gameFlags(game);

		if (levelToFn != null)
			gameFlags |= levelToFn.gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(typeFrom));
		concepts.or(SiteType.concepts(typeTo));
		concepts.or(super.concepts(game));
		concepts.or(region.concepts(game));
		concepts.or(condition.concepts(game));
		concepts.or(conditionTo.concepts(game));

		if (regionTo != null)
			concepts.or(regionTo.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		if (levelFromFn != null)
			concepts.or(levelFromFn.concepts(game));

		if (levelToFn != null)
			concepts.or(levelToFn.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(region.writesEvalContextRecursive());
		writeEvalContext.or(condition.writesEvalContextRecursive());
		writeEvalContext.or(conditionTo.writesEvalContextRecursive());

		if (regionTo != null)
			writeEvalContext.or(regionTo.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());

		if (levelFromFn != null)
			writeEvalContext.or(levelFromFn.writesEvalContextRecursive());

		if (levelToFn != null)
			writeEvalContext.or(levelToFn.writesEvalContextRecursive());

		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.To.id(), true);
		writeEvalContext.set(EvalContextData.From.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(region.readsEvalContextRecursive());
		readEvalContext.or(condition.readsEvalContextRecursive());
		readEvalContext.or(conditionTo.readsEvalContextRecursive());

		if (regionTo != null)
			readEvalContext.or(regionTo.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());

		if (levelFromFn != null)
			readEvalContext.or(levelFromFn.readsEvalContextRecursive());

		if (levelToFn != null)
			readEvalContext.or(levelToFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= region.missingRequirement(game);
		missingRequirement |= condition.missingRequirement(game);
		missingRequirement |= conditionTo.missingRequirement(game);

		if (regionTo != null)
			missingRequirement |= regionTo.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);

		if (levelFromFn != null)
			missingRequirement |= levelFromFn.missingRequirement(game);

		if (levelToFn != null)
			missingRequirement |= levelToFn.missingRequirement(game);

		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= region.willCrash(game);
		willCrash |= condition.willCrash(game);
		willCrash |= conditionTo.willCrash(game);

		if (regionTo != null)
			willCrash |= regionTo.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);

		if (levelFromFn != null)
			willCrash |= levelFromFn.willCrash(game);

		if (levelToFn != null)
			willCrash |= levelToFn.willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		if (regionTo != null && !regionTo.isStatic())
			return false;
		
		if (levelFromFn != null && !levelFromFn.isStatic())
			return false;

		if (levelToFn != null && !levelToFn.isStatic())
			return false;

		return super.isStatic() && region.isStatic() && condition.isStatic() && conditionTo.isStatic();
	}
	
	@Override
	public void preprocess(final Game game)
	{
		typeFrom = SiteType.use(typeFrom, game);
		typeTo = SiteType.use(typeTo, game);
		super.preprocess(game);
		region.preprocess(game);
		condition.preprocess(game);
		if (regionTo != null)
			regionTo.preprocess(game);
		conditionTo.preprocess(game);

		if (levelFromFn != null)
			levelFromFn.preprocess(game);

		if (levelToFn != null)
			levelToFn.preprocess(game);
		
		gameUsesStacking = game.isStacking();
	}
}
