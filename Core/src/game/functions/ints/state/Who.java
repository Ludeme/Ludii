package game.functions.ints.state;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.types.board.SiteType;
import game.types.state.GameType;
import main.Constants;
import other.context.Context;
import other.state.container.ContainerState;
import other.state.stacking.BaseContainerStateStacking;

/**
 * Returns the index of the owner at a specific location/level.
 * 
 * @author Eric Piette and cambolbro
 */
public final class Who extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which location. */
	private final IntFunction loc;

	/** Which level (for a stacking game). */
	private final IntFunction level;

	/** Cell/Edge/Vertex. */
	private SiteType type;

	//-------------------------------------------------------------------------
	
	/**
	 * @param type  The graph element type [default SiteType of the board].
	 * @param at    The location to check.
	 * @param level The level to check.
	 * 
	 * @return Return the index of the owner of a specific location/level.
	 *
	 * @example (who at:(last To))
	 */
	public static IntFunction construct
	(
		@Opt 	   final SiteType    type,
			 @Name final IntFunction at,
		@Opt @Name final IntFunction level
	)
	{
		if (level == null || (level.isStatic() && level.eval(null) == Constants.UNDEFINED))
			return new WhoNoLevel(at, type);
		else
			return new Who(type, at, level);
	}

	/**
	 * Return the index of the owner of a specific location/level.
	 * 
	 * @param type  The graph element type [default SiteType of the board].
	 * @param at    The location to check.
	 * @param level The level to check.
	 * 
	 * @example (who at:(last To))
	 */
	private Who
	(
		@Opt 	   final SiteType type,
			 @Name final IntFunction at,
		@Opt @Name final IntFunction level
	)
	{
		loc = at;
		this.level = (level == null) ? new IntConstant(Constants.UNDEFINED) : level;
		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final int location = loc.eval(context);
		if (location == Constants.OFF)
			return Constants.NOBODY;

		final int containerId = context.containerId()[location];

		if ((context.game().gameFlags() & GameType.Stacking) != 0)
		{
			// Is stacking game
			final BaseContainerStateStacking state = (BaseContainerStateStacking) context.state()
					.containerStates()[containerId];
			if (level.eval(context) == -1)
				return state.who(location, type);
			else
				return state.who(location, level.eval(context), type);
		}

		final ContainerState cs = context.state().containerStates()[containerId];
		return cs.who(loc.eval(context), type);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		// we're looking at the "who" in a specific context, so not static
		return false;
		
		//return site.isStatic() && level.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = loc.gameFlags(game) | level.gameFlags(game);

		gameFlags |= SiteType.gameFlags(type);

		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.or(loc.concepts(game));
		concepts.or(level.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(loc.writesEvalContextRecursive());
		writeEvalContext.or(level.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(loc.readsEvalContextRecursive());
		readEvalContext.or(level.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= loc.missingRequirement(game);
		missingRequirement |= level.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= loc.willCrash(game);
		willCrash |= level.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		loc.preprocess(game);
		level.preprocess(game);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The function that generates the site we want to look at
	 */
	public IntFunction site()
	{
		return loc;
	}
	
	@Override
	public String toEnglish(final Game game)
	{
		return "Player at " + loc.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * An optimised version of Who ludeme, without levels
	 * 
	 * @author Eric Piette and cambolbro and Dennis Soemers
	 */
	public static class WhoNoLevel extends BaseIntFunction
	{
		private static final long serialVersionUID = 1L;

		//---------------------------------------------------------------------
	
		/** Which site. */
		protected final IntFunction site;

		/** Cell/Edge/Vertex. */
		private SiteType type;
	
		//---------------------------------------------------------------------
	
		/**
		 * Constructor.
		 * 
		 * @param site
		 * @param type
		 */
		public WhoNoLevel(final IntFunction site, @Opt final SiteType type)
		{
			this.site = site;
			this.type = type;
		}
	
		//---------------------------------------------------------------------
	
		@Override
		public final int eval(final Context context)
		{
			final int location = site.eval(context);

			if (location < 0)
				return Constants.NOBODY;
	
			final int containerId = type.equals(SiteType.Cell) ? context.containerId()[location] : 0;
			final ContainerState cs = context.state().containerStates()[containerId];
			return cs.who(location, type);
		}
	
		//---------------------------------------------------------------------
	
		@Override
		public boolean isStatic()
		{
			return false;
		}
	
		@Override
		public long gameFlags(final Game game)
		{
			long gameFlags = site.gameFlags(game);

			gameFlags |= SiteType.gameFlags(type);

			return gameFlags;
		}
		
		@Override
		public BitSet concepts(final Game game)
		{
			final BitSet concepts = new BitSet();
			concepts.or(SiteType.concepts(type));
			concepts.or(site.concepts(game));
			return concepts;
		}

		@Override
		public void preprocess(final Game game)
		{
			if (type == null)
				type = game.board().defaultSite();
			
			site.preprocess(game);
		}
		
		@Override
		public BitSet writesEvalContextRecursive()
		{
			final BitSet writeEvalContext = new BitSet();
			writeEvalContext.or(site.writesEvalContextRecursive());
			return writeEvalContext;
		}
	
		@Override
		public BitSet readsEvalContextRecursive()
		{
			final BitSet readEvalContext = new BitSet();
			readEvalContext.or(site.readsEvalContextRecursive());
			return readEvalContext;
		}

		@Override
		public boolean missingRequirement(final Game game)
		{
			boolean missingRequirement = false;
			missingRequirement |= site.missingRequirement(game);
			return missingRequirement;
		}

		@Override
		public boolean willCrash(final Game game)
		{
			boolean willCrash = false;
			willCrash |= site.willCrash(game);
			return willCrash;
		}
		
		@Override
		public String toEnglish(final Game game)
		{
			return "Player at " + site.toEnglish(game);
		}
	}
}
