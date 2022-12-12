package game.functions.region.sites.index;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.region.BaseRegionFunction;
import game.functions.region.RegionFunction;
import game.types.board.SiteType;
import game.util.equipment.Region;
import other.context.Context;

/**
 * Returns the empty (i.e. unoccupied) sites of a container.
 * 
 * @author cambolbro and Eric.Piette and Dennis
 */
@SuppressWarnings("javadoc")
@Hide
public final class SitesEmpty extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which container. */
	private final IntFunction containerFunction;
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param type Type of graph element [default SiteType of the board].
	 * @param cont Index of the container [0].
	 * 
	 * @example (empty)
	 */
	public static RegionFunction construct
	(
		@Opt final SiteType    type, 
		@Opt final IntFunction cont
	)
	{
		if (cont == null || (cont.isStatic() && cont.eval(null) == 0))
			return new EmptyDefault(type);
		
		return new SitesEmpty(type, cont);
	}

	/**
	 * @param cont Index of the container.
	 * @param type Type of graph element [default SiteType of the board].
	 * 
	 * @example (empty)
	 */
	private SitesEmpty
	(
		@Opt final SiteType type,
		@Opt final IntFunction cont
	)
	{
		containerFunction = (cont == null) ? new IntConstant(0) : cont;
		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		final int container = containerFunction.eval(context);
		final SiteType realType = container > 0 ? SiteType.Cell : type;
		final Region region = context.state().containerStates()[container].emptyRegion((realType != null) ? realType
				: context.board().defaultSite());
		if (container < 1)
			return region;
		final int siteFrom = context.sitesFrom()[container];
		final int[] sites = region.sites();
		for (int i = 0; i < sites.length; i++)
			sites[i] += siteFrom;
		return new Region(sites);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		// we're looking at "empty" in a specific context, so never static
		return false;
	}

	@Override
	public String toString()
	{
		if (type == null)
			return "Null type in Empty.";
		
		if (type.equals(SiteType.Cell))
			return "Empty(" + containerFunction + ")";
		else if(type.equals(SiteType.Edge))
			return "EmptyEdge(" + containerFunction + ")";
		else 
			return "EmptyVertex(" + containerFunction + ")";
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = containerFunction.gameFlags(game);

		gameFlags |= SiteType.gameFlags(type);

		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.or(containerFunction.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(containerFunction.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(containerFunction.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= containerFunction.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= containerFunction.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		containerFunction.preprocess(game);
	}
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "empty " + type.name();
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return Container
	 */
	public IntFunction containerFunction()
	{
		return containerFunction;
	}
	
	/**
	 * @return Variable type
	 */
	public SiteType type()
	{
		return type;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * An optimised "default" version of Empty ludeme, for container 0
	 * 
	 * @author Dennis Soemers
	 */
	public static class EmptyDefault extends BaseRegionFunction
	{
		//---------------------------------------------------------------------
		
		/** */
		private static final long serialVersionUID = 1L;
		
		//---------------------------------------------------------------------
		
		/**
		 * Constructor
		 * @param type
		 */
		EmptyDefault(final SiteType type)
		{
			this.type = type;
		}
		
		//---------------------------------------------------------------------

		@Override
		public Region eval(final Context context)
		{
			return context.state().containerStates()[0].emptyRegion(type);
		}
	
		//---------------------------------------------------------------------
	
		@Override
		public String toString()
		{
			return "Empty()";
		}
	
		@Override
		public boolean isStatic()
		{
			return false;
		}
	
		@Override
		public long gameFlags(final Game game)
		{	
			return 0L;
		}
		
		@Override
		public BitSet concepts(final Game game)
		{
			final BitSet concepts = new BitSet();
			return concepts;
		}

		@Override
		public BitSet writesEvalContextRecursive()
		{
			final BitSet writeEvalContext = new BitSet();
			return writeEvalContext;
		}

		@Override
		public BitSet readsEvalContextRecursive()
		{
			final BitSet readEvalContext = new BitSet();
			return readEvalContext;
		}

		@Override
		public void preprocess(final Game game)
		{
			type = SiteType.use(type, game);
		}
		
		@Override
		public String toEnglish(final Game game) 
		{
			final SiteType realType = (type != null) ? type : game.board().defaultSite();
			return "the set of empty " + realType.name().toLowerCase() + "s";
		}
	}
}
