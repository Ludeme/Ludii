package game.functions.booleans.is.in;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import annotations.Or;
import annotations.Or2;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanFunction;
import game.functions.intArray.IntArrayFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.iterator.To;
import game.functions.region.RegionFunction;
import main.Constants;
import other.concept.Concept;
import other.context.Context;

/**
 * Tests if a specific location is in a region.
 * 
 * @author mrraow and cambolbro
 */
@Hide
public final class IsIn extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which site. */
	private final IntFunction[] sites;

	/** Which region. */
	private final RegionFunction region;
	
	/** Which array. */
	private final IntArrayFunction array;

	//-------------------------------------------------------------------------

	/**
	 * @param site           The site to check if they are in the region [(to)].
	 * @param sites          The sites to check if they are in the region.
	 * @param containsRegion The region possibly containing the site(s).
	 */
	@SuppressWarnings("javadoc")
	public static BooleanFunction construct
	(
		@Opt @Or  final IntFunction      site,
		@Opt @Or  final IntFunction[]    sites,
		@Opt @Or2 final RegionFunction   containsRegion,
		@Opt @Or2 final IntArrayFunction array
	)
	{
		if(array != null)
		{
			if (sites != null)
				return new IsIn(sites, array);
			else
				return new IsIn(site == null ? new IntFunction[]
				{ To.instance() } : new IntFunction[]
				{ site }, array);
		}
		else
		{
			if (sites != null)
				return new IsIn(sites, containsRegion);
			else
				return new InSingleSite(site == null ? To.instance() : site, containsRegion);
		}
	}
	
	/**
	 * @param sites  The sites.
	 * @param region The region.
	 */
	private IsIn
	(
		final IntFunction[] sites, 
		final IntArrayFunction array
	)
	{
		this.sites = sites;
		region = null;
		this.array = array;
	}

	/**
	 * @param sites  The sites.
	 * @param region The region.
	 */
	private IsIn
	(
		final IntFunction[] sites, 
		final RegionFunction region
	)
	{
		// TODO should probably make this a construct() method such that we can
		// also jump to the single-site case if we're given an array of length 1?
		this.sites = sites;
		this.region = region;
		array = null;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		String text="";
		
		if (region != null)
			text+=region.toEnglish(game);
		
		return text;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		if (sites != null)
		{
			if (region != null)
			{
				for (final IntFunction site : sites)
				{
					final int location = site.eval(context);
					if (location == Constants.OFF || !region.eval(context).bitSet().get(location))
						return false;
				}
			}
			else
			{
				final int[] values = array.eval(context);
				for (final IntFunction site : sites)
				{
					final int value = site.eval(context);
					boolean found = false;
					for (int i = 0; i < values.length; i++)
					{
						if (value == values[i])
						{
							found = true;
							break;
						}
					}
					if (!found)
						return false;
				}
			}
		}
		return true;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		if (sites.length == 1)
		{
			return "In(" + sites[0] + "," + region + ")";
		}
		else
		{
			String str = "In({";
			for (final IntFunction site : sites)
			{
				str += site + " ";
			}
			if (region != null)
				str += "}," + region + ")";
			else
				str += "}," + array + ")";
			return str;
		}
	}

	@Override
	public boolean isStatic()
	{
		if (sites != null)
			for (final IntFunction site : sites)
				if (!site.isStatic())
					return false;

		if (region != null)
			return region.isStatic();
		else
			return array.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0l;

		if (region != null)
			gameFlags |= region.gameFlags(game);

		if (array != null)
			gameFlags |= array.gameFlags(game);

		if (sites != null)
		{
			for (final IntFunction site : sites)
				gameFlags |= site.gameFlags(game);
		}
		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		if (region != null)
			concepts.or(region.concepts(game));

		if (array != null)
			concepts.or(array.concepts(game));

		concepts.set(Concept.Contains.id(), true);
		if (sites != null)
			for (final IntFunction site : sites)
				concepts.or(site.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (region != null)
			writeEvalContext.or(region.writesEvalContextRecursive());

		if (array != null)
			writeEvalContext.or(array.writesEvalContextRecursive());

		if (sites != null)
			for (final IntFunction site : sites)
				writeEvalContext.or(site.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (region != null)
			readEvalContext.or(region.readsEvalContextRecursive());

		if (array != null)
			readEvalContext.or(array.readsEvalContextRecursive());

		if (sites != null)
			for (final IntFunction site : sites)
				readEvalContext.or(site.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (region != null)
			missingRequirement |= region.missingRequirement(game);

		if (array != null)
			missingRequirement |= array.missingRequirement(game);
		if (sites != null)
			for (final IntFunction site : sites)
				missingRequirement |= site.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (region != null)
			willCrash |= region.willCrash(game);

		if (array != null)
			willCrash |= array.willCrash(game);
		if (sites != null)
			for (final IntFunction site : sites)
				willCrash |= site.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (region != null)
			region.preprocess(game);

		if (array != null)
			array.preprocess(game);

		if (sites != null)
		{
			for (final IntFunction site : sites)
				site.preprocess(game);
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * @return RegionFunction of this In test
	 */
	public RegionFunction region()
	{
		return region;
	}

	/**
	 * @return Sites
	 */
	public IntFunction[] site()
	{
		return sites;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Optimised version of In ludeme for single-site arg
	 * 
	 * @author Dennis Soemers
	 */
	private static class InSingleSite extends BaseBooleanFunction
	{
		private static final long serialVersionUID = 1L;

		//---------------------------------------------------------------------

		/** Which site. */
		protected final IntFunction siteFunc;
	
		/** Which region. */
		protected final RegionFunction region;
		
		//---------------------------------------------------------------------
		
		/**
		 * @param site   The site.
		 * @param region The region.
		 * 
		 * @example (in (lastTo) (region Mover))
		 */
		public InSingleSite
		(
			final IntFunction site,
			final RegionFunction region
		)
		{
			siteFunc = site;
			this.region = region;
		}
		
		//---------------------------------------------------------------------

		@Override
		public boolean eval(final Context context)
		{
			final int location = siteFunc.eval(context);
			return (location >= 0 && region.contains(context, location));
		}
	
		//---------------------------------------------------------------------
		
		@Override
		public String toEnglish(final Game game) 
		{
			return siteFunc.toEnglish(game) + " is in " + region.toEnglish(game);
		}
	
		@Override
		public String toString()
		{
			return "In(" + siteFunc + "," + region + ")";
		}
	
		@Override
		public boolean isStatic()
		{
			return (region.isStatic() && siteFunc.isStatic());
		}
	
		@Override
		public long gameFlags(final Game game)
		{	
			long gameFlags = region.gameFlags(game);
			gameFlags |= siteFunc.gameFlags(game);
			return gameFlags;
		}
		
		@Override
		public BitSet concepts(final Game game)
		{
			final BitSet concepts = new BitSet();
			concepts.set(Concept.Contains.id(), true);
			concepts.or(region.concepts(game));
			concepts.or(siteFunc.concepts(game));
			return concepts;
		}

		@Override
		public BitSet writesEvalContextRecursive()
		{
			final BitSet writeEvalContext = new BitSet();
			writeEvalContext.or(region.writesEvalContextRecursive());
			writeEvalContext.or(siteFunc.writesEvalContextRecursive());
			return writeEvalContext;
		}

		@Override
		public BitSet readsEvalContextRecursive()
		{
			final BitSet readEvalContext = new BitSet();
			readEvalContext.or(region.readsEvalContextRecursive());
			readEvalContext.or(siteFunc.readsEvalContextRecursive());
			return readEvalContext;
		}

		@Override
		public void preprocess(final Game game)
		{
			region.preprocess(game);
			siteFunc.preprocess(game);
		}

		@Override
		public boolean missingRequirement(final Game game)
		{
			boolean missingRequirement = false;
			missingRequirement |= region.missingRequirement(game);
			missingRequirement |= siteFunc.missingRequirement(game);
			return missingRequirement;
		}

		@Override
		public boolean willCrash(final Game game)
		{
			boolean willCrash = false;
			willCrash |= region.willCrash(game);
			willCrash |= siteFunc.willCrash(game);
			return willCrash;
		}
	}
}