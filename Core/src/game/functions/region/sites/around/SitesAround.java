package game.functions.region.sites.around;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.component.Component;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.region.BaseRegionFunction;
import game.functions.region.RegionFunction;
import game.types.board.RegionTypeDynamic;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.equipment.Region;
import game.util.graph.Radial;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.context.Context;
import other.context.EvalContextData;
import other.state.State;
import other.state.container.ContainerState;

/**
 * Returns the sites around a given region or site according to specified
 * directions and conditions.
 * 
 * @author Eric Piette
 */
@Hide
public final class SitesAround extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** If we can, we'll precompute once and cache. */
	private Region precomputedRegion = null;

	/** Region to check. */
	private final RegionFunction where;

	/** Loc to check. */
	private final IntFunction locWhere;

	/** Type of sites around (empty, enemy, all, ...). */
	private final RegionTypeDynamic typeDynamic;

	/** Distance around. */
	private final IntFunction distance;

	/** Choice of directions. */
	private final AbsoluteDirection directions;

	/** Condition to check by each site to be on the return region. */
	private final BooleanFunction cond;

	/** Origin included or not. */
	private final BooleanFunction originIncluded;

	//-------------------------------------------------------------------------

	/**
	 * @param typeLoc     The graph element type [default SiteType of the board].
	 * @param where       The location to check.
	 * @param regionWhere The region to check.
	 * @param type        The type of the dynamic region.
	 * @param distance    The distance around which to check [1].
	 * @param directions  The directions to use [Adjacent].
	 * @param If          The condition to satisfy around the site to be included in
	 *                    the result.
	 * @param includeSelf True if the origin site/region is included in the result
	 *                    [False].
	 */
	public SitesAround
	(
		@Opt           final SiteType          typeLoc,
		     @Or       final IntFunction       where,
		     @Or  	   final RegionFunction    regionWhere,
		@Opt 	       final RegionTypeDynamic type,
		@Opt     @Name final IntFunction       distance,
		@Opt 	       final AbsoluteDirection directions,
		@Opt 	 @Name final BooleanFunction   If,
		@Opt     @Name final BooleanFunction   includeSelf
	)
	{
		this.where = regionWhere;
		this.locWhere = where;
		this.typeDynamic = type;
		this.distance = (distance == null) ? new IntConstant(1) : distance;
		this.directions = (directions == null) ? AbsoluteDirection.Adjacent : directions;
		this.cond = (If == null) ? null : If;
		this.originIncluded = (includeSelf == null) ? new BooleanConstant(false) : includeSelf;
		this.type = typeLoc;
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		if (precomputedRegion != null)
			return precomputedRegion;

		// sites Around
		final TIntArrayList sitesAround = new TIntArrayList();

		// distance
		final int dist = distance.eval(context);

		// Get the list of index of the regionTo
		final TIntArrayList typeRegionTo = convertRegion(context, typeDynamic);

		final int origFrom = context.from();
		final int origTo = context.to();

		// sites to check
		final int[] sites;
		if (where != null)
		{
			sites = where.eval(context).sites();
		}
		else
		{
			sites = new int[1];
			sites[0] = locWhere.eval(context);
		}

		final other.topology.Topology graph = context.topology();

		final ContainerState state = context.state().containerStates()[0];
		
		// if region empty & site not specify
		if (sites.length == 0)
			return new Region(sitesAround.toArray());

		if (sites[0] == Constants.UNDEFINED)
			return new Region();

		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
		
		for (final int site : sites)
		{
			if (site >= graph.getGraphElements(realType).size())
				continue;
			
			context.setFrom(site);

			final List<Radial> radials = graph.trajectories().radials(realType, site, directions);
			for (final Radial radial : radials)
			{
				if (dist >= radial.steps().length)
					continue;
				
				for (int toIdx = 0; toIdx < radial.steps().length; toIdx++)
				{
					final int to = radial.steps()[dist].id();
					context.setTo(to);
					if ((cond == null || cond.eval(context))
							&& (typeDynamic == null || typeRegionTo.contains(state.what(to, realType))))
					{
						sitesAround.add(to);
					}
				}
			}
		}

		context.setFrom(origFrom);
		context.setTo(origTo);

		if (originIncluded.eval(context))
			sitesAround.add(sites[0]);

		return new Region(sitesAround.toArray());
	}

	//-------------------------------------------------------------------------

	/**
	 * @param context       The context.
	 * @param dynamicRegion The dynamic region.
	 * @return A list of the index of the player for each kind of region.
	 */
	public static TIntArrayList convertRegion(final Context context, final RegionTypeDynamic dynamicRegion)
	{
		final int indexSharedPlayer = context.game().players().size();
		final TIntArrayList whatIndex = new TIntArrayList();
		final State state = context.state();

		if (dynamicRegion == RegionTypeDynamic.Empty)
		{
			whatIndex.add(0);
		}
		else if (dynamicRegion == RegionTypeDynamic.NotEmpty)
		{
			final Component[] components = context.equipment().components();
			for (int i = 1; i < components.length; i++)
				whatIndex.add(components[i].index());
		}
		else if (dynamicRegion == RegionTypeDynamic.Own)
		{
			final int moverId = state.mover();
			final Component[] components = context.equipment().components();
			for (int i = 1; i < components.length; i++)
			{
				final Component component = components[i];
				if (component.owner() == moverId || component.owner() == indexSharedPlayer)
					whatIndex.add(component.index());
			}
		}
		else if (dynamicRegion == RegionTypeDynamic.Enemy)
		{
			final Component[] components = context.equipment().components();
			if (context.game().requiresTeams())
			{
				final TIntArrayList enemies = new TIntArrayList();
				final int teamMover = context.state().getTeam(context.state().mover());
				for (int pid = 1; pid < context.game().players().size(); ++pid)
					if (pid != context.state().mover() && !context.state().playerInTeam(pid, teamMover))
						enemies.add(pid);

				for (int i = 1; i < components.length; i++)
				{
					final Component component = components[i];
					if (enemies.contains(component.owner()))
						whatIndex.add(component.index());
				}
			}
			else
			{
				final int moverId = state.mover();
				for (int i = 1; i < components.length; i++)
				{
					final Component component = components[i];
					if (component.owner() != moverId && component.owner() > 0 && component.owner() < indexSharedPlayer)
						whatIndex.add(component.index());
				}
			}
		}
		else if (dynamicRegion == RegionTypeDynamic.NotEnemy)
		{
			final Component[] components = context.equipment().components();
			if (context.game().requiresTeams())
			{
				final TIntArrayList enemies = new TIntArrayList();
				final int teamMover = context.state().getTeam(context.state().mover());
				for (int pid = 1; pid < context.game().players().size(); ++pid)
					if (pid != context.state().mover() && !context.state().playerInTeam(pid, teamMover))
						enemies.add(pid);

				for (int i = 1; i < components.length; i++)
				{
					final Component component = components[i];
					if (enemies.contains(component.owner()) || component.owner() == indexSharedPlayer)
						whatIndex.add(component.index());
				}
			}
			else
			{
				final int moverId = state.mover();
				for (int i = 1; i < components.length; i++)
				{
					final Component component = components[i];
					if (component.owner() == moverId || component.owner() == indexSharedPlayer)
						whatIndex.add(component.index());
				}
			}
		}
		else if (dynamicRegion == RegionTypeDynamic.NotOwn)
		{
			final int moverId = state.mover();
			final Component[] components = context.equipment().components();
			for (int i = 1; i < components.length; i++)
			{
				final Component component = components[i];
				if (component.owner() != moverId && component.owner() != indexSharedPlayer)
					whatIndex.add(component.index());
			}
		}

		return whatIndex;
	}

	@Override
	public boolean isStatic()
	{
		if (where != null && !where.isStatic())
			return false;
		if (locWhere != null && !locWhere.isStatic())
			return false;
		if (distance != null && !distance.isStatic())
			return false;
		if (originIncluded != null && !originIncluded.isStatic())
			return false;
		if (cond != null && !cond.isStatic())
			return false;
		return true;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flag = 0;

		flag |= SiteType.gameFlags(type);

		if (where != null)
			flag |= where.gameFlags(game);
		
		if (locWhere != null)
			flag |= locWhere.gameFlags(game);
		
		if (distance != null)
			flag |= distance.gameFlags(game);
		
		if (originIncluded != null)
			flag |= originIncluded.gameFlags(game);
		
		if (cond != null)
			flag |= cond.gameFlags(game);
		
		return flag;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		if (where != null)
			missingRequirement |= where.missingRequirement(game);

		if (locWhere != null)
			missingRequirement |= locWhere.missingRequirement(game);

		if (distance != null)
			missingRequirement |= distance.missingRequirement(game);

		if (originIncluded != null)
			missingRequirement |= originIncluded.missingRequirement(game);

		if (cond != null)
			missingRequirement |= cond.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (where != null)
			willCrash |= where.willCrash(game);

		if (locWhere != null)
			willCrash |= locWhere.willCrash(game);

		if (distance != null)
			willCrash |= distance.willCrash(game);

		if (originIncluded != null)
			willCrash |= originIncluded.willCrash(game);

		if (cond != null)
			willCrash |= cond.willCrash(game);
		return willCrash;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.or(SiteType.concepts(type));

		if (where != null)
			concepts.or(where.concepts(game));

		if (locWhere != null)
			concepts.or(locWhere.concepts(game));

		if (distance != null)
			concepts.or(distance.concepts(game));

		if (originIncluded != null)
			concepts.or(originIncluded.concepts(game));

		if (cond != null)
			concepts.or(cond.concepts(game));

		if (directions != null)
			concepts.or(AbsoluteDirection.concepts(directions));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(super.writesEvalContextRecursive());

		if (where != null)
			writeEvalContext.or(where.writesEvalContextRecursive());

		if (locWhere != null)
			writeEvalContext.or(locWhere.writesEvalContextRecursive());

		if (distance != null)
			writeEvalContext.or(distance.writesEvalContextRecursive());

		if (originIncluded != null)
			writeEvalContext.or(originIncluded.writesEvalContextRecursive());

		if (cond != null)
			writeEvalContext.or(cond.writesEvalContextRecursive());
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

		if (where != null)
			readEvalContext.or(where.readsEvalContextRecursive());

		if (locWhere != null)
			readEvalContext.or(locWhere.readsEvalContextRecursive());

		if (distance != null)
			readEvalContext.or(distance.readsEvalContextRecursive());

		if (originIncluded != null)
			readEvalContext.or(originIncluded.readsEvalContextRecursive());

		if (cond != null)
			readEvalContext.or(cond.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);

		if (where != null)
			where.preprocess(game);
		if(locWhere != null)
			locWhere.preprocess(game);
		if(distance != null)
			distance.preprocess(game);
		if(originIncluded != null)
			originIncluded.preprocess(game);
		if(cond != null)
			cond.preprocess(game);

		if (isStatic() && typeDynamic == null )
		{
			final Context gameContext = new Context(game, null);

			// sites Around
			final TIntArrayList sitesAround = new TIntArrayList();

			// distance
			final int dist = distance.eval(gameContext);

			final int origFrom = gameContext.from();
			final int origTo = gameContext.to();

			// sites to check
			final int[] sites;
			if (where != null)
			{
				sites = where.eval(gameContext).sites();
			}
			else
			{
				sites = new int[1];
				sites[0] = locWhere.eval(gameContext);
			}

			final other.topology.Topology graph = gameContext.topology();

			// if region empty & site not specify
			if (sites.length == 0)
				precomputedRegion = new Region(sitesAround.toArray());

			if (sites.length > 0)
				if (sites[0] == Constants.UNDEFINED)
					precomputedRegion = new Region();

			final SiteType realType = (type != null) ? type : gameContext.game().board().defaultSite();

			for (final int site : sites)
			{
				if (site >= graph.getGraphElements(realType).size())
					continue;

				final List<Radial> radials = graph.trajectories().radials(realType, site, directions);
				for (final Radial radial : radials)
				{
					if (dist >= radial.steps().length)
						continue;

					for (int toIdx = 0; toIdx < radial.steps().length; toIdx++)
					{
						gameContext.setFrom(radial.steps()[0].id());
						final int to = radial.steps()[dist].id();
						gameContext.setTo(to);
						if ((cond == null || cond.eval(gameContext)))
						{
							sitesAround.add(to);
						}
					}
				}
			}

			gameContext.setFrom(origFrom);
			gameContext.setTo(origTo);

			if (originIncluded.eval(gameContext))
				sitesAround.add(sites[0]);

			precomputedRegion = new Region(sitesAround.toArray());
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The origin location.
	 */
	public IntFunction locWhere() 
	{
		return locWhere;
	}
	
	/**
	 * @return Region we're looking at
	 */
	public RegionFunction where()
	{
		return where;
	}
	
	/**
	 * @return Type of the "around" sites
	 */
	public RegionTypeDynamic type()
	{
		return typeDynamic;
	}
	
	/**
	 * @return Distance
	 */
	public IntFunction distance()
	{
		return distance;
	}
	
	/**
	 * @return Directions
	 */
	public AbsoluteDirection directions()
	{
		return directions;
	}
	
	/**
	 * @return Condition that must hold for "around" cells
	 */
	public BooleanFunction cond()
	{
		return cond;
	}

	/**
	 * @return Whether the origin should be included
	 */
	public BooleanFunction originIncluded()
	{
		return originIncluded;
	}

	@Override
	public String toString()
	{
		return "Sites Around";
	}
}
