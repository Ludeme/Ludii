package game.rules.play.moves.nonDecision.effect;

import java.util.Arrays;
import java.util.BitSet;

import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.state.Mover;
import game.functions.region.RegionFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.util.equipment.Region;
import game.util.moves.Piece;
import game.util.moves.To;
import main.Constants;
import other.action.Action;
import other.action.move.ActionAdd;
import other.context.Context;
import other.context.EvalContextData;
import other.move.Move;

/**
 * Claims a site by adding a piece of the specified colour there.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * @remarks This ludeme is used for graph games.
 */
public final class Claim extends Effect
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
	private final BooleanFunction test;
	
	/** Add on Cell/Edge/Vertex. */
	private SiteType type;
	
	//-------------------------------------------------------------------------

	/** Action cache (indexed by move first, component second, state+1 third, site fourth) */
	private Move[][][][] actionCache = null;
	
	/** Set to false if use of action cache is not allowed */
	private boolean allowCacheUse = false;

	//-------------------------------------------------------------------------

	/**
	 * @param what The data about the components to claim.
	 * @param to   The data on the location to claim.
	 * @param then The moves applied after that move is applied.
	 * 
	 * @example (claim (to Cell (site)) (then (and (addScore Mover 1) (moveAgain))))
	 */
	public Claim
	(
		@Opt final Piece what, 
			 final To    to,
		@Opt final Then  then
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
		type = to.type();
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

		for (final IntFunction compomentFn : components) 
		{
			final int componentId = compomentFn.eval(context);
		
			if (site != null)
			{
				// TODO we're not making use of cache inside this block?
				final int siteEval = site.eval(context);
			
				context.setTo(siteEval);
				if (test == null || test.eval(context)) 
				{
					final Move action;
					final int state = (localState == null) ? -1 : localState.eval(context);
					final Action actionAdd = new ActionAdd(type, siteEval, componentId, 1, state, Constants.UNDEFINED,
							Constants.UNDEFINED,
							null);
					if (isDecision())
						actionAdd.setDecision(true);
					action = new Move(actionAdd);
					if (type.equals(SiteType.Edge))
					{
						action.setFromNonDecision(siteEval);
						action.setToNonDecision(siteEval);
						action.setEdgeMove(siteEval);
						action.setOrientedMove(false);
					}
					else
					{
						action.setFromNonDecision(siteEval);
						action.setToNonDecision(siteEval);
					}
					if (then() != null) 
					{
						// action.consequents().add(consequents().moves());
						final int fromOrigCsq = context.from();
						final int toOrigCsq = context.to();
						context.setFrom(action.fromNonDecision());
						context.setTo(action.toNonDecision());
						final Moves m = then().moves().eval(context);
						context.setFrom(fromOrigCsq);
						context.setTo(toOrigCsq);
						for (final Move mCsq : m.moves())
						{
							for (final Action a : mCsq.actions())
							{
								action.actions().add(a);
							}
						}
					}
					moves.moves().add(action);
//					for (final Move m : moves.moves())
//					{
//						System.out.println(m);
//						if (m.consequents() != null)
//							System.out.println("csq: " + m.consequents());
//					}

					context.setFrom(origFrom);
					context.setTo(origTo);

					// Store the Moves in the computed moves.
					for (int j = 0; j < moves.moves().size(); j++)
						moves.moves().get(j).setMovesLudeme(this);

					return moves;
				}
			}

			final Move[][] compActionCache = actionCache[mover][componentId];
			final Region sites = region.eval(context);
			
			for (int toSite = sites.bitSet().nextSetBit(0); toSite >= 0; toSite = sites.bitSet().nextSetBit(toSite + 1))
			{
				final Move action;

				context.setTo(toSite);
				if (test == null || test.eval(context)) 
				{
					final int state = (localState == null) ? -1 : localState.eval(context);
					
					if (compActionCache[state+1][toSite] == null)
					{
						final Action actionAdd = new ActionAdd(type, toSite, componentId, 1, state,
								Constants.UNDEFINED, Constants.UNDEFINED,
								null);

						if (isDecision())
							actionAdd.setDecision(true);
						action = new Move(actionAdd);
						if (type.equals(SiteType.Edge))
						{
							action.setFromNonDecision(toSite);
							action.setToNonDecision(toSite);
							action.setEdgeMove(toSite);
							action.setOrientedMove(false);
						}
						else
						{
							action.setFromNonDecision(toSite);
							action.setToNonDecision(toSite);
						}
						if (then() != null)
							action.then().add(then().moves());
				
						action.setMover(mover);
						
						if (allowCacheUse)
							compActionCache[state+1][toSite] = action;
					} 
					else
					{
						action = compActionCache[state+1][toSite];
						// action.consequents().clear();
					}
					
					moves.moves().add(action);
				}
			}
		}
		
		context.setTo(origTo);
		context.setFrom(origFrom);

//		System.out.println("MOVES ARE");
//		for (final Move m : moves.moves())
//			System.out.println(m);
		
		// Store the Moves in the computed moves.
		for (int j = 0; j < moves.moves().size(); j++)
			moves.moves().get(j).setMovesLudeme(this);

		return moves;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = super.gameFlags(game);
		
		gameFlags |= SiteType.gameFlags(type);

		if (region != null)
			gameFlags |= region.gameFlags(game);
		
		if (test != null)
			gameFlags |= test.gameFlags(game);
		
		for (final IntFunction comp : components)
			gameFlags |= comp.gameFlags(game);
		
		if (site != null)
			gameFlags |= site.gameFlags(game);
		
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

		if (region != null)
			concepts.or(region.concepts(game));

		if (test != null)
			concepts.or(test.concepts(game));

		for (final IntFunction comp : components)
			concepts.or(comp.concepts(game));

		if (site != null)
			concepts.or(site.concepts(game));

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

		if (site != null)
			writeEvalContext.or(site.writesEvalContextRecursive());

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

		if (site != null)
			readEvalContext.or(site.readsEvalContextRecursive());

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

		if (site != null)
			missingRequirement |= site.missingRequirement(game);

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

		if (then() != null)
			willCrash |= then().willCrash(game);
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
		
		return true;
	}
	
	@Override
	public void preprocess(final Game game)
	{
		if (type == null)
			type = game.board().defaultSite();
		
		super.preprocess(game);
		
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
		
		final int maxNumStates;
		if (game.requiresLocalState())
			maxNumStates = game.maximalLocalStates();
		else
			maxNumStates = 0;
		
		// generate action cache
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
						[game.players().count() + 1]
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
				"[Colour: " + 
				components[0] + ", " + 
				region + ", " + 
				site + ", " + 
				then() + 
				"]";
		}
		else
		{
			return 
				"[Colour: " + 
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
		
		if (region != null)
			englishString = "claim the region " + region.toEnglish(game);
		else
			englishString = "claim the site " + site.toEnglish(game);
		
		String thenString = "";
		if (then() != null)
			thenString = " then " + then().toEnglish(game);
		
		return englishString + thenString;
	}
}
