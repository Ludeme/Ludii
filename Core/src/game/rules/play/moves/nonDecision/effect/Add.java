package game.rules.play.moves.nonDecision.effect;

import java.util.Arrays;
import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.equipment.component.Component;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.ints.state.Mover;
import game.functions.region.RegionFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.equipment.Region;
import game.util.moves.Piece;
import game.util.moves.To;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.action.Action;
import other.action.move.ActionAdd;
import other.action.move.ActionInsert;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.move.Move;
import other.move.MoveUtilities;
import other.state.container.ContainerState;
import other.trial.Trial;

/**
 * Places one or more component(s) at a collection of sites or at one specific
 * site.
 * 
 * @author cambolbro and Eric.Piette
 * 
 * @remarks The ``to'' location is not updated until the move is made.
 */
public final class Add extends Effect
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	/** Which local state. */
	private final IntFunction localState;
	
	/** Which components. */
	private IntFunction[] components;

	/** Which region. */
	private final RegionFunction region;
	
	/** Which site. */
	private final IntFunction site;

	/** Which site. */
	private final IntFunction level;

	/** Which site. */
	private final BooleanFunction test;
	
	/** The number of pieces to add to each site. */
	private final IntFunction countFn;

	/** The condition to apply the side effect. */
	private final BooleanFunction sideEffectCondition;

	/** The side effect to apply. */
	private final Moves sideEffect;

	/** Which site. */
	private final boolean onStack;
	
	/** Add on Cell/Edge/Vertex. */
	private SiteType type;
	
	//-------------------------------------------------------------------------

	/** Action cache (indexed by mover first, component second, state+1 third, site fourth) */
	private Move[][][][] actionCache = null;
	
	/** 
	 * Set to false if use of action cache is not allowed 
	 * This being true causes the bug reported here (https://ludii.games/forums/showthread.php?tid=589)
	 */
	private boolean allowCacheUse = false;

	//-------------------------------------------------------------------------

	/**
	 * @param what  The data about the components to add.
	 * @param to    The data on the location to add.
	 * @param count The number of components to add [1].
	 * @param stack True if the move has to be applied on a stack [False].
	 * @param then  The moves applied after that move is applied.
	 * 
	 * @example (add (to (sites Empty)))
	 * 
	 * @example (add (to Cell (sites Empty Cell)))
	 * 
	 * @example (add (piece "Disc0") (to (last From)))
	 * 
	 * @example (add (piece "Disc0") (to (sites Empty)) (then (attract)) )
	 */
	public Add
	(
		@Opt       final Piece       what,
		           final To          to,
		@Opt @Name final IntFunction count,
		@Opt @Name final Boolean     stack,
		@Opt       final Then        then
	)
	{
		super(then);
		
		if (what != null && what.components() == null)
		{
			if (what.component() == null)
				components = new IntFunction[] {new Mover()};
			else
				components = new IntFunction[]
				{ what.component() };
		}
		else
		{
			components = (what == null) ? new IntFunction[]
			{ new Mover() } : what.components();
		}
		
		localState = (what == null) ? null : (what.state() == null) ? null : what.state();
			
		site = to.loc();
		region = to.region();

		test = to.cond();
		onStack = (stack == null) ? false : stack.booleanValue();
		type = to.type();
		level = to.level();
		sideEffectCondition = (to.effect() == null) ? null : to.effect().condition();
		sideEffect = to.effect();
		countFn = (count == null) ? new IntConstant(1) : count;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{	
		// Return list of legal "to" moves
		final BaseMoves moves = new BaseMoves(super.then());

		final int origFrom = context.from();
		final int origTo = context.to();
		final int mover = context.state().mover();
		final int count = countFn.eval(context);
		
		if (count < 1)
			return moves;

		for (final IntFunction componentFn : components) 
		{
			final int componentId = componentFn.eval(context);
			
			if (componentId == Constants.UNDEFINED)
				continue;
			
			// We check if we try to add a large piece.
			final Component component = context.components()[componentId];
			if (component != null && component.isLargePiece())
			{
				final Moves movesLargePiece = evalLargePiece(context, component);
				moves.moves().addAll(movesLargePiece.moves());
				continue;
			}
			
			if (site != null)
			{
				// TODO we're not making use of cache inside this block?
				final int siteEval = site.eval(context);
				
				context.setTo(siteEval);
				if (test == null || test.eval(context)) 
				{
					Move move;
					final int state = (localState == null) ? Constants.UNDEFINED : localState.eval(context);

					final Action actionToAdd = (level == null)
							? new ActionAdd(type, siteEval, componentId, 1, state, Constants.UNDEFINED,
									Constants.UNDEFINED,
									null)
							: new ActionInsert(type, siteEval, level.eval(context), componentId, state);

					final int cid = siteEval >= context.containerId().length ? 0 : context.containerId()[siteEval];
					final ContainerState cs = context.containerState(cid);
					if(context.game().isStacking())
						actionToAdd.setLevelTo(cs.sizeStack(siteEval, type));

					if (isDecision())
						actionToAdd.setDecision(true);

					move = new Move(actionToAdd);

					int remainingCount = count - 1;
					while (remainingCount > 0)
					{
						final Action actionToAddAgain = (level == null)
								? new ActionAdd(type, siteEval, componentId, 1, state, Constants.UNDEFINED,
										Constants.UNDEFINED, null)
								: new ActionInsert(type, siteEval, level.eval(context), componentId, state);

						move.actions().add(actionToAddAgain);
						remainingCount--;
					}

					if (sideEffect != null && (sideEffectCondition == null
							|| (sideEffectCondition != null && (sideEffectCondition.eval(context)))))
					{
						context.setFrom(siteEval);
						context.setTo(siteEval);
						move = MoveUtilities.chainRuleWithAction(context, sideEffect, move, true, false);
					}

					if (type.equals(SiteType.Edge))
					{
						move.setFromNonDecision(siteEval);
						move.setToNonDecision(siteEval);
						move.setEdgeMove(siteEval);
						move.setOrientedMove(false);
					}
					else
					{
						move.setFromNonDecision(siteEval);
						move.setToNonDecision(siteEval);
					}
					moves.moves().add(move);
					context.setFrom(origFrom);
					context.setTo(origTo);
					
					for (int j = 0; j < moves.moves().size(); j++)
					{
						final Move m = moves.moves().get(j);
						m.setMover(mover);
						if (then() != null)
							m.then().add(then().moves());
					}

					// Store the Moves in the computed moves.
					for (int j = 0; j < moves.moves().size(); j++)
						moves.moves().get(j).setMovesLudeme(this);

					return moves;
				}
			}

			final Move[][] compActionCache = actionCache[mover][componentId];

			if (region == null)
				return moves;

			final Region sites = region.eval(context);
			
			for (int toSite = sites.bitSet().nextSetBit(0); toSite >= 0; toSite = sites.bitSet().nextSetBit(toSite + 1))
			{
				Move move;
				
				context.setTo(toSite);
				if (test == null || test.eval(context)) 
				{
					final int state = (localState == null) ? -1 : localState.eval(context);
					
					if (compActionCache[state + 1][toSite] == null)
					{
						final Action actionToAdd = (level == null)
								? new ActionAdd(type, toSite, componentId, 1, state, Constants.UNDEFINED,
										Constants.UNDEFINED,
										null)
								: new ActionInsert(type, toSite, level.eval(context), componentId, state);

						final int cid = toSite >= context.containerId().length ? 0 : context.containerId()[toSite];
						final ContainerState cs = context.containerState(cid);
						if(context.game().isStacking())
							actionToAdd.setLevelTo(cs.sizeStack(toSite, type));
						
						actionToAdd.setDecision(isDecision());
						move = new Move(actionToAdd);

						int remainingCount = count - 1;
						while (remainingCount > 0)
						{
							final Action actionToAddAgain = (level == null)
									? new ActionAdd(type, toSite, componentId, 1, state, Constants.UNDEFINED,
											Constants.UNDEFINED, null)
									: new ActionInsert(type, toSite, level.eval(context), componentId, state);

							move.actions().add(actionToAddAgain);
							remainingCount--;
						}

						if (sideEffect != null && (sideEffectCondition == null
								|| (sideEffectCondition != null && (sideEffectCondition.eval(context)))))
						{
							context.setFrom(toSite);
							context.setTo(toSite);
							move = MoveUtilities.chainRuleWithAction(context, sideEffect, move, true, false);
							MoveUtilities.chainRuleCrossProduct(context, moves, null, move, false);
						}

						if (type.equals(SiteType.Edge))
						{
							move.setFromNonDecision(toSite);
							move.setToNonDecision(toSite);
							move.setEdgeMove(toSite);
							move.setOrientedMove(false);
						}
						else
						{
							move.setFromNonDecision(toSite);
							move.setToNonDecision(toSite);
						}
						
						if (then() != null)
							move.then().add(then().moves());
				
						move.setMover(mover);
						
						if (allowCacheUse)
							compActionCache[state+1][toSite] = move;
					} 
					else
					{
						move = compActionCache[state+1][toSite];
					}
					
					moves.moves().add(move);
				}
			}
		}
		
		context.setTo(origTo);
		context.setFrom(origFrom);

		// Store the Moves in the computed moves.
		for (int j = 0; j < moves.moves().size(); j++)
			moves.moves().get(j).setMovesLudeme(this);
		
		return moves;
	}

	//-------------------------------------------------------------------------

	private Moves evalLargePiece(final Context context, final Component largePiece)
	{
		final BaseMoves moves = new BaseMoves(super.then());
		final int largePieceId = largePiece.index();
		final int nbPossibleStates = largePiece.walk().length * 4;
		final int localStateToAdd = (localState == null) ? Constants.UNDEFINED : localState.eval(context);
		final int mover = context.state().mover();

		if (site != null)
		{
			final int siteEval = site.eval(context);
			final ContainerState cs = context.containerState(context.containerId()[siteEval]);

			for (int state = 0; state < nbPossibleStates; state++)
			{
				if (localStateToAdd != Constants.UNDEFINED && localStateToAdd != state)
					continue;

				final TIntArrayList locsLargePiece = largePiece.locs(context, siteEval, state, context.topology());

				if (locsLargePiece == null || locsLargePiece.size() <= 0)
					continue;

				boolean valid = true;
				for (int i = 0; i < locsLargePiece.size(); i++)
				{
					final int siteToCheck = locsLargePiece.get(i);
					if (!cs.isEmpty(siteToCheck, type))
					{
						valid = false;
						break;
					}
				}

				if (valid)
				{
					final Action actionAdd = new ActionAdd(type, siteEval, largePieceId, 1, state, Constants.UNDEFINED,
							Constants.UNDEFINED,
							null);
					actionAdd.setDecision(isDecision());

					final Move move = new Move(actionAdd);
					move.setFromNonDecision(siteEval);
					move.setToNonDecision(siteEval);
					move.setMover(mover);
					move.setStateNonDecision(state);
					moves.moves().add(move);
				}
			}

			// Store the Moves in the computed moves.
			for (int j = 0; j < moves.moves().size(); j++)
				moves.moves().get(j).setMovesLudeme(this);

			return moves;
		}

		final Region sites = region.eval(context);
		for (int toSite = sites.bitSet().nextSetBit(0); toSite >= 0; toSite = sites.bitSet().nextSetBit(toSite + 1))
		{
			final ContainerState cs = context.containerState(context.containerId()[toSite]);

			for (int state = 0; state < nbPossibleStates; state++)
			{
				if (localStateToAdd != Constants.UNDEFINED && localStateToAdd != state)
					continue;

				final TIntArrayList locsLargePiece = largePiece.locs(context, toSite, state, context.topology());

				if (locsLargePiece == null || locsLargePiece.size() <= 0)
					continue;

				boolean valid = true;
				for (int i = 0; i < locsLargePiece.size(); i++)
				{
					final int siteToCheck = locsLargePiece.get(i);
					if (!cs.isEmpty(siteToCheck, type))
					{
						valid = false;
						break;
					}
				}

				if (valid)
				{
					final Action actionAdd = new ActionAdd(type, toSite, largePieceId, 1, state, Constants.UNDEFINED,
							Constants.UNDEFINED,
							null);
					actionAdd.setDecision(isDecision());

					final Move move = new Move(actionAdd);
					move.setFromNonDecision(toSite);
					move.setToNonDecision(toSite);
					move.setMover(mover);
					move.setStateNonDecision(state);
					moves.moves().add(move);
				}
			}
		}

		// Store the Moves in the computed moves.
		for (int j = 0; j < moves.moves().size(); j++)
			moves.moves().get(j).setMovesLudeme(this);

		return moves;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = super.gameFlags(game);
		
		if (onStack) 
			gameFlags |= GameType.Stacking;
		else
		if (!(countFn instanceof IntConstant) || ((countFn instanceof IntConstant)
				&& ((IntConstant) countFn).eval(new Context(game, new Trial(game))) != 1))
					gameFlags |= GameType.Count;

		gameFlags |= SiteType.gameFlags(type);

		if (region != null)
			gameFlags |= region.gameFlags(game);
		
		if (countFn != null)
			gameFlags |= countFn.gameFlags(game);

		if (test != null)
			gameFlags |= test.gameFlags(game);
		
		for (final IntFunction comp : components)
			gameFlags |= comp.gameFlags(game);
		
		if (site != null)
			gameFlags |= site.gameFlags(game);

		if (level != null)
		{
			gameFlags |= level.gameFlags(game);
			gameFlags |= GameType.Stacking;
		}

		if (sideEffect != null)
			gameFlags |= sideEffect.gameFlags(game);
		
		if (then() != null)
			gameFlags |= then().gameFlags(game);
		
		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.or(super.concepts(game));

		if (isDecision())
			concepts.set(Concept.AddDecision.id(), true);
		else
			concepts.set(Concept.AddEffect.id(), true);

		if (region != null)
			concepts.or(region.concepts(game));

		if (test != null)
			concepts.or(test.concepts(game));

		for (final IntFunction comp : components)
			concepts.or(comp.concepts(game));

		if (countFn != null)
			concepts.or(countFn.concepts(game));

		if (site != null)
			concepts.or(site.concepts(game));

		if (level != null)
			concepts.or(level.concepts(game));

		if (sideEffect != null)
			concepts.or(sideEffect.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(super.writesEvalContextRecursive());

		if (region != null)
			writeEvalContext.or(region.writesEvalContextRecursive());

		if (test != null)
			writeEvalContext.or(test.writesEvalContextRecursive());

		for (final IntFunction comp : components)
			writeEvalContext.or(comp.writesEvalContextRecursive());

		if (countFn != null)
			writeEvalContext.or(countFn.writesEvalContextRecursive());

		if (site != null)
			writeEvalContext.or(site.writesEvalContextRecursive());

		if (level != null)
			writeEvalContext.or(level.writesEvalContextRecursive());

		if (sideEffect != null)
			writeEvalContext.or(sideEffect.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
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

		if (region != null)
			readEvalContext.or(region.readsEvalContextRecursive());

		if (test != null)
			readEvalContext.or(test.readsEvalContextRecursive());

		for (final IntFunction comp : components)
			readEvalContext.or(comp.readsEvalContextRecursive());

		if (countFn != null)
			readEvalContext.or(countFn.readsEvalContextRecursive());

		if (site != null)
			readEvalContext.or(site.readsEvalContextRecursive());

		if (level != null)
			readEvalContext.or(level.readsEvalContextRecursive());

		if (sideEffect != null)
			readEvalContext.or(sideEffect.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);

		if (region != null)
		missingRequirement |= region.missingRequirement(game);

		if (test != null)
		missingRequirement |= test.missingRequirement(game);

		for (final IntFunction comp : components)
		missingRequirement |= comp.missingRequirement(game);

	if (countFn != null)
		missingRequirement |= countFn.missingRequirement(game);

		if (site != null)
		missingRequirement |= site.missingRequirement(game);

		if (level != null)
		missingRequirement |= level.missingRequirement(game);

		if (sideEffect != null)
			missingRequirement |= sideEffect.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);

		if (region != null)
			willCrash |= region.willCrash(game);

		if (test != null)
			willCrash |= test.willCrash(game);

		for (final IntFunction comp : components)
			willCrash |= comp.willCrash(game);

		if (site != null)
			willCrash |= site.willCrash(game);

		if (level != null)
			willCrash |= level.willCrash(game);

		if (countFn != null)
			willCrash |= countFn.willCrash(game);

		if (sideEffect != null)
			willCrash |= sideEffect.willCrash(game);
		
		if (then() != null)
			willCrash |= then().willCrash(game);
		
		// We check if each player has a piece if the piece of the mover has to be added.
		boolean moverPieceAdded = false;
		for(final IntFunction compFn : components)
			if(compFn instanceof Mover)
			{
				moverPieceAdded =true;
				break;
			}
		boolean componentOwnedByEachPlayer = true;
		if(moverPieceAdded)
		{
			for(int pid = 1; pid < game.players().size();pid++)
			{
				boolean foundPiece = false;
				for(int compId = 1; compId < game.equipment().components().length; compId++)
				{
					final Component component = game.equipment().components()[compId];
					if(component.owner() == pid)
					{
						foundPiece = true;
						break;
					}
				}
				if(!foundPiece)
					componentOwnedByEachPlayer = false;
			}
		}
		
		if(moverPieceAdded && !componentOwnedByEachPlayer)
		{
			game.addCrashToReport("The ludeme (move Add ...) or (add ...) is used to add the piece of the mover but a player has no piece.");
			willCrash = true;
		}
		
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		for (final IntFunction comp : components)
			if (!comp.isStatic())
				return false;
		
		if (region != null && !region.isStatic())
			return false;

		if (test != null && !test.isStatic())
			return false;
		
		if (test != null && !test.isStatic())
			return false;
		
		if (localState != null && !localState.isStatic())
			return false;
		
		if (site != null && !site.isStatic())
			return false;
		
		if (level != null && !level.isStatic())
			return false;

		if (sideEffect != null && !sideEffect.isStatic())
			return false;

		if (countFn != null && !countFn.isStatic())
			return false;

		return true;
	}
	
	@Override
	public void preprocess(final Game game)
	{
		if (type == null)
			type = game.board().defaultSite();
		
		super.preprocess(game);
		
		if (countFn != null)
			countFn.preprocess(game);

		if (region != null)
			region.preprocess(game);
		
		if (test != null)
			test.preprocess(game);
		
		if (localState != null)
			localState.preprocess(game);
		
		for (final IntFunction comp : components)
			comp.preprocess(game);
		
		if (site != null)
			site.preprocess(game);
		
		if (sideEffect != null)
			sideEffect.preprocess(game);
		
		final int maxNumStates;
		if (game.requiresLocalState())
			maxNumStates = game.maximalLocalStates();
		else
			maxNumStates = 0;
		
		if (game.isStacking())
		{
			// No cache allowed in stacking games
			allowCacheUse = false;
		}

		// Generate action cache
		if (type.equals(SiteType.Cell))
		{
			actionCache = new Move[game.players().count() + 1][][][];
			
			for (int p = 1; p < actionCache.length; ++p)
			{
				actionCache[p] = new Move
						[game.numComponents() + 1]
						[maxNumStates + 2]
						[game.equipment().totalDefaultSites()];
			}
		}
		else if (type.equals(SiteType.Edge)) 
		{
			actionCache = new Move[game.players().count() + 1][][][];
			
			for (int p = 1; p < actionCache.length; ++p)
			{
				actionCache[p] = new Move
						[game.numComponents() + 1]
						[maxNumStates + 2]
				[game.board().topology().edges().size()];
			}
		}
		else if (type.equals(SiteType.Vertex))
		{
			actionCache = new Move[game.players().count() + 1][][][];
			
			for (int p = 1; p < actionCache.length; ++p)
			{
				actionCache[p] = new Move
				[game.numComponents() + 1]
						[maxNumStates + 2]
				[game.board().topology().vertices().size()];
			}
		}
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return Components which can be placed
	 */
	public IntFunction[] components()
	{
		return components;
	}
	
	/**
	 * @return The RegionFunction that tells us where we're allowed to move to
	 */
	public RegionFunction region()
	{
		return region;
	}
	
	/**
	 * @return Site we're allowed to move to
	 */
	public IntFunction site()
	{
		return site;
	}
	
	/**
	 * @return Function telling us which moves are legal
	 */
	public BooleanFunction legal()
	{
		return test;
	}
	
	/**
	 * @return On stack?
	 */
	public boolean onStack()
	{
		return onStack;
	}
	
	/**
	 * @return Variable type
	 */
	public SiteType type()
	{
		return type;
	}
	
	/**
	 * Disables use of the action cache
	 */
	public void disableActionCache()
	{
		allowCacheUse = false;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		if (components.length == 1)
		{
			return 
				"[Add: " + 
				components[0] + ", " + 
				region + ", " + 
				site + ", " + 
				then() + 
				"]";
		}
		else
		{
			return 
				"[Add: " + 
				Arrays.toString(components) + ", " + 
				region + ", " + 
				site + ", " + 
				then() + 
				"]";
		}
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		String englishString = "";
		
		if(components != null && region != null) 
		{
			String textCopm = "";
			String textRegion = "any site";
			
			for (final IntFunction comp : components) 
				if (comp instanceof Mover == false) 
					textCopm+=comp.toEnglish(game);

			if(region.toEnglish(game).startsWith("empty "))
				textRegion = "an " + region.toEnglish(game);
			else
				textRegion = region.toEnglish(game);

			if(textCopm.equals(""))
				englishString = "Add one of your pieces to " + textRegion;
			else
				englishString = "Add " + textCopm + " to " + textRegion;
		}
		else if(components != null && region == null)
		{
			String textCopm="";
			
			for (final IntFunction comp : components)
				textCopm+=comp.toEnglish(game);
			
			englishString = "add " + textCopm;
		}
		else if(components == null && region != null)
		{
			if(region.toEnglish(game).startsWith("empty "))
				englishString = "Add one of your pieces to an " + region.toEnglish(game);
			else
				englishString = "Add one of your pieces to " + region.toEnglish(game);
		}
		
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		return englishString + thenString;
	}
}
