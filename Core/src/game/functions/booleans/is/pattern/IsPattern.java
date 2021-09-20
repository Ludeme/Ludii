package game.functions.booleans.is.pattern;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.ints.last.LastTo;
import game.types.board.SiteType;
import game.types.board.StepType;
import game.util.directions.DirectionFacing;
import game.util.graph.Step;
import main.Constants;
import other.ContainerId;
import other.concept.Concept;
import other.context.Context;
import other.location.FullLocation;
import other.location.Location;
import other.state.container.ContainerState;

/**
 * For detecting a specific pattern from a site.
 * 
 * @author Eric.Piette
 */
@Hide
public final class IsPattern extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	/** The indices to follow in the pattern. */
	private final IntFunction[] whatsFn;

	/** The walk describing the pattern. */
	private final StepType[]   walk;
	
	/** The site from. */
	private final IntFunction   fromFn;
	
	/** The type of the site from. */
	private SiteType type;

	//-------------------------------------------------------------------------
 
	/**
	 * @param walk  The walk describing the pattern.
	 * @param type  The type of the site from to detect the pattern.
	 * @param from  The site from to detect the pattern [(from (last To))].
	 * @param what  The piece to check in the pattern.
	 * @param whats The sequence of pieces to check in the pattern.
	 */
	public IsPattern
	(
		               final StepType[]    walk,
			@Opt       final SiteType      type,
		    @Opt @Name final IntFunction   from,
		@Or @Opt @Name final IntFunction   what,
		@Or @Opt @Name final IntFunction[] whats
	)
	{
		this.walk = walk;
		fromFn = (from == null) ? new LastTo(null) : from;
		whatsFn = (whats != null) ? whats : (what != null) ? new IntFunction[]
		{ what } : null;
		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		final int from = fromFn.eval(context);

		if (from <= Constants.OFF)
			return false;

		final SiteType realType = (type == null) ? context.board().defaultSite() : type;
		final int cid = new ContainerId(null, null, null, null, new IntConstant(from)).eval(context);
		final other.topology.Topology graph = context.containers()[cid].topology();
		final ContainerState cs = context.containerState(0);

		if (from >= graph.getGraphElements(realType).size())
			return false;

		final int[] whats = (whatsFn != null) ? new int[whatsFn.length] : new int[1];

		if (whatsFn != null)
		{
		for (int i = 0; i < whats.length; i++)
			whats[i] = whatsFn[i].eval(context);
		}
		else
		{
			final int what = cs.what(from, realType);
			if (what == 0)
				return false;
			whats[0] = what;
		}
		
		final List<DirectionFacing> orthogonalSupported = graph.supportedOrthogonalDirections(realType);
		List<DirectionFacing> walkDirection;
		walkDirection = graph.supportedOrthogonalDirections(realType);

		for (final DirectionFacing startDirection : walkDirection)
		{
			int currentLoc = from;
			DirectionFacing currentDirection = startDirection;

			int whatIndex = 0;
			if (cs.what(from, realType) != whats[whatIndex])
				return false;

			whatIndex++;
			if (whatIndex == whats.length)
				whatIndex = 0;

			boolean found = true;
			for (final StepType step : walk)
			{
				if (step == StepType.F)
				{
					final List<Step> stepsDirection = graph.trajectories().steps(realType, currentLoc,
							currentDirection.toAbsolute());

					int to = Constants.UNDEFINED;
					for (final Step stepDirection : stepsDirection)
					{
						if (stepDirection.from().siteType() != stepDirection.to().siteType())
							continue;

						to = stepDirection.to().id();
					}

					currentLoc = to;

					// No correct walk with that state or not correct what.
					if (to == Constants.UNDEFINED || cs.what(to, realType) != whats[whatIndex])
					{
						found = false;
						break;
					}

					whatIndex++;
					if (whatIndex == whats.length)
						whatIndex = 0;
				}
				else if (step == StepType.R)
				{
					currentDirection = currentDirection.right();
					while (!orthogonalSupported.contains(currentDirection))
						currentDirection = currentDirection.right();

				}
				else if (step == StepType.L)
				{
					currentDirection = currentDirection.left();
					while (!orthogonalSupported.contains(currentDirection))
						currentDirection = currentDirection.left();
				}
			}

			if (found)
				return true;
		}

		return false;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flag = 0l;

		if (fromFn != null)
			flag |= fromFn.gameFlags(game);

		if (whatsFn != null)
			for (final IntFunction what : whatsFn)
				flag |= what.gameFlags(game);

		flag |= SiteType.gameFlags(type);

		return flag;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.Pattern.id(), true);
		concepts.or(SiteType.concepts(type));
		if (fromFn != null)
			concepts.or(fromFn.concepts(game));

		if (whatsFn != null)
			for (final IntFunction what : whatsFn)
				concepts.or(what.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (fromFn != null)
			writeEvalContext.or(fromFn.writesEvalContextRecursive());

		if (whatsFn != null)
			for (final IntFunction what : whatsFn)
				writeEvalContext.or(what.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (fromFn != null)
			readEvalContext.or(fromFn.readsEvalContextRecursive());

		if (whatsFn != null)
			for (final IntFunction what : whatsFn)
				readEvalContext.or(what.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		if (fromFn != null)
			fromFn.preprocess(game);
		if (whatsFn != null)
			for (final IntFunction what : whatsFn)
				what.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (fromFn != null)
			missingRequirement |= fromFn.missingRequirement(game);

		if (whatsFn != null)
			for (final IntFunction what : whatsFn)
				missingRequirement |= what.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (fromFn != null)
			willCrash |= fromFn.willCrash(game);

		if (whatsFn != null)
			for (final IntFunction what : whatsFn)
				willCrash |= what.willCrash(game);
		return willCrash;
	}

	// -------------------------------------------------------------------------

	@Override
	public List<Location> satisfyingSites(final Context context)
	{
		if (!eval(context))
			return new ArrayList<Location>();

		final List<Location> winningSites = new ArrayList<Location>();

		final int from = fromFn.eval(context);

		if (from <= Constants.OFF)
			return new ArrayList<Location>();

		final SiteType realType = (type == null) ? context.board().defaultSite() : type;
		final int cid = new ContainerId(null, null, null, null, new IntConstant(from)).eval(context);
		final other.topology.Topology graph = context.containers()[cid].topology();
		final ContainerState cs = context.containerState(0);

		if (from >= graph.getGraphElements(realType).size())
			return new ArrayList<Location>();

		final int[] whats = (whatsFn != null) ? new int[whatsFn.length] : new int[1];

		if (whatsFn != null)
		{
		for (int i = 0; i < whats.length; i++)
			whats[i] = whatsFn[i].eval(context);
		}
		else
		{
			final int what = cs.what(from, realType);
			if (what == 0)
				return new ArrayList<Location>();
			whats[0] = what;
		}

		final List<DirectionFacing> orthogonalSupported = graph.supportedOrthogonalDirections(realType);
		List<DirectionFacing> walkDirection;
		walkDirection = graph.supportedOrthogonalDirections(realType);

		for (final DirectionFacing startDirection : walkDirection)
		{
			int currentLoc = from;
			DirectionFacing currentDirection = startDirection;

			int whatIndex = 0;
			if (cs.what(from, realType) != whats[whatIndex])
				return new ArrayList<Location>();

			whatIndex++;
			if (whatIndex == whats.length)
				whatIndex = 0;

			winningSites.add(new FullLocation(from, 0, realType));

			boolean found = true;
			for (final StepType step : walk)
			{
				if (step == StepType.F)
				{
					final List<Step> stepsDirection = graph.trajectories().steps(realType, currentLoc,
							currentDirection.toAbsolute());

					int to = Constants.UNDEFINED;
					for (final Step stepDirection : stepsDirection)
					{
						if (stepDirection.from().siteType() != stepDirection.to().siteType())
							continue;

						to = stepDirection.to().id();
					}

					currentLoc = to;
					winningSites.add(new FullLocation(to, 0, realType));

					// No correct walk with that state or not correct what.
					if (to == Constants.UNDEFINED || cs.what(to, realType) != whats[whatIndex])
					{
						found = false;
						winningSites.clear();
						break;
					}

					whatIndex++;
					if (whatIndex == whats.length)
						whatIndex = 0;
				}
				else if (step == StepType.R)
				{
					currentDirection = currentDirection.right();
					while (!orthogonalSupported.contains(currentDirection))
						currentDirection = currentDirection.right();

				}
				else if (step == StepType.L)
				{
					currentDirection = currentDirection.left();
					while (!orthogonalSupported.contains(currentDirection))
						currentDirection = currentDirection.left();
				}
			}

			if (found)
				return winningSites;
		}

		return new ArrayList<Location>();
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		String whatArrayString = "any sites";
		if (whatsFn != null)
		{
			whatArrayString = "[";
			for (final IntFunction i : whatsFn)
				whatArrayString += i.toEnglish(game) + ",";
			whatArrayString = "the sites " + whatArrayString.substring(0,whatArrayString.length()-1) + "]";
		}
			
		String walkArrayString = "[";
		for (final StepType s : walk)
			walkArrayString += s.name() + ",";
		walkArrayString = walkArrayString.substring(0,walkArrayString.length()-1) + "]";
		
		return "the walk " + walkArrayString + " from " + type.name().toLowerCase() + " " + fromFn.toEnglish(game) + " goes through " + whatArrayString;
	}
	
	//-------------------------------------------------------------------------
}
