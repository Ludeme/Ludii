package game.functions.region.sites.distance;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.equipment.component.Component;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntFunction;
import game.functions.range.Range;
import game.functions.range.RangeFunction;
import game.functions.region.BaseRegionFunction;
import game.rules.play.moves.nonDecision.effect.Step;
import game.types.board.RelationType;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.DirectionFacing;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.state.container.ContainerState;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * All the sites at a specific distance from another.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SitesDistance extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** If we can, we'll precompute once and cache. */
	private Region precomputedRegion = null;

	//-------------------------------------------------------------------------

	/** The step relation. */
	private final RelationType relation;

	/** Which site. */
	private final IntFunction fromFn;

	/** Site included or not. */
	private final RangeFunction distanceFn;

	/** New Rotation after each step move. */
	private final IntFunction newRotationFn;

	/** The specific step move. */
	private final Step stepMove;

	//-------------------------------------------------------------------------

	/**
	 * @param type        The graph element type [default site type of the board].
	 * @param relation    The relation type of the steps [Adjacent].
	 * @param stepMove    Define a particular step move to step.
	 * @param newRotation Define a new rotation at each step move in using the (value) iterator for the rotation.
	 * @param from        Index of the site.
	 * @param distance    Distance from the site.
	 */
	public SitesDistance
	(
		@Opt          final SiteType      type,
		@Opt 	      final RelationType  relation,
		@Opt 	      final Step          stepMove,
		@Opt @Name	  final IntFunction   newRotation,
		     @Name    final IntFunction   from, 
			          final RangeFunction distance
	)
	{

		fromFn = from;
		distanceFn = distance;
		this.type = type;
		this.relation = (relation == null) ? RelationType.Adjacent : relation;
		this.stepMove = stepMove;
		newRotationFn = newRotation;
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		if (precomputedRegion != null)
			return precomputedRegion;

		final int from = fromFn.eval(context);
		final Range distance = distanceFn.eval(context);
		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
		final TIntArrayList sites = new TIntArrayList();
		final List<? extends TopologyElement> elements = context.topology().getGraphElements(realType);
		
		if (from < 0 || from >= elements.size())
			return new Region(sites.toArray());

		final int minDistance = distance.min(context);

		if (minDistance < 0)
			return new Region(sites.toArray());

		final int maxDistance = distance.max(context);

		if (stepMove == null)
		{
			final TopologyElement element = elements.get(from);

			if (minDistance >= element.sitesAtDistance().size())
				return new Region(sites.toArray());

			for (int i = minDistance; i <= maxDistance; i++)
			{
				if (i >= 0 && i < element.sitesAtDistance().size())
				{
					final List<TopologyElement> elementsAtDistance = element.sitesAtDistance().get(i);
					for (final TopologyElement elementAtDistance : elementsAtDistance)
						sites.add(elementAtDistance.index());
				}
			}
			return new Region(sites.toArray());
		}
		else
		{
			int numSteps = 1;
			final TIntArrayList currList = new TIntArrayList();
			final TIntArrayList sitesToReturn = new TIntArrayList();

			final Topology graph = context.topology();
			final int maxSize = graph.getGraphElements(realType).size();
			if (from >= maxSize)
				return new Region(sitesToReturn.toArray());

			final ContainerState cs = context.containerState(0);
			final int what = cs.what(from, realType);
			int rotation = cs.rotation(from, realType);
			DirectionFacing facingDirection = null;
			Component component = null;
			if (what != 0)
			{
				component = context.components()[what];
				facingDirection = component.getDirn();
			}
			
			final TIntArrayList originStepMove = stepMove(context, realType, from, stepMove.goRule(), component, facingDirection, rotation);
			for (int i = 0; i < originStepMove.size(); i++)
			{
				final int to = originStepMove.getQuick(i);
				if (!currList.contains(to))
					currList.add(to);
			}

			final TIntArrayList nextList = new TIntArrayList();
			final TIntArrayList sitesChecked = new TIntArrayList();
			sitesChecked.add(from);
			sitesChecked.addAll(currList);
			
			if (numSteps >= minDistance)
				for (int i = 0; i < currList.size(); i++)
					if (!sitesToReturn.contains(currList.getQuick(i)))
						sitesToReturn.add(currList.getQuick(i));

			while (!currList.isEmpty() && numSteps < maxDistance)
			{
				for (int i = 0; i < currList.size(); i++)
				{
					final int newSite = currList.getQuick(i);
					
					if (newRotationFn != null)
					{
						final int originValue = context.value();
						context.setValue(rotation);
						rotation = newRotationFn.eval(context);
						context.setValue(originValue);
					}
					
					final TIntArrayList stepMoves = stepMove(context, realType, newSite, stepMove.goRule(), component, facingDirection, rotation);
					
					for (int j = 0; j < stepMoves.size(); j++)
					{
						final int to = stepMoves.getQuick(j);
						if (!sitesChecked.contains(to) && !nextList.contains(to))
							nextList.add(to);
					}
				}

				sitesChecked.addAll(currList);
				currList.clear();
				currList.addAll(nextList);

				++numSteps;
				
				// We keep only the non duplicate site which are >= min
				if (numSteps >= minDistance)
					for (int i = 0; i < nextList.size(); i++)
						if (!sitesToReturn.contains(nextList.getQuick(i)))
							sitesToReturn.add(nextList.getQuick(i));

				nextList.clear();
			}

			return new Region(sitesToReturn.toArray());
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		if (stepMove != null)
			return false;
		
		if (newRotationFn != null)
			return false;
		
		return fromFn.isStatic() && distanceFn.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = fromFn.gameFlags(game)
				| distanceFn.gameFlags(game);

		if (stepMove == null)
		{
			switch (relation)
			{
			case Adjacent:
				gameFlags |= GameType.StepAdjacentDistance;
				break;
			case All:
				gameFlags |= GameType.StepAllDistance;
				break;
			case Diagonal:
				gameFlags |= GameType.StepDiagonalDistance;
				break;
			case OffDiagonal:
				gameFlags |= GameType.StepOffDistance;
				break;
			case Orthogonal:
				gameFlags |= GameType.StepOrthogonalDistance;
				break;
			default:
				break;
			}
		}
		else
		{
			gameFlags |= stepMove.gameFlags(game);
		}

		gameFlags |= SiteType.gameFlags(type);
		
		if (newRotationFn != null)
			gameFlags |= newRotationFn.gameFlags(game);

		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.or(fromFn.concepts(game));
		concepts.set(Concept.Distance.id(), true);
		concepts.or(distanceFn.concepts(game));

		if (stepMove != null)
			concepts.or(stepMove.concepts(game));
		if (newRotationFn != null)
			concepts.or(newRotationFn.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(fromFn.writesEvalContextRecursive());
		writeEvalContext.or(distanceFn.writesEvalContextRecursive());
		if (stepMove != null)
			writeEvalContext.or(stepMove.writesEvalContextRecursive());
		if (newRotationFn != null)
		{
			writeEvalContext.or(newRotationFn.writesEvalContextRecursive());
			writeEvalContext.set(EvalContextData.Value.id(), true);
		}
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(fromFn.readsEvalContextRecursive());
		readEvalContext.or(distanceFn.readsEvalContextRecursive());
		if (stepMove != null)
			readEvalContext.or(stepMove.readsEvalContextRecursive());
		if (newRotationFn != null)
			readEvalContext.or(newRotationFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= fromFn.missingRequirement(game);
		missingRequirement |= distanceFn.missingRequirement(game);
		if (stepMove != null)
			missingRequirement |= stepMove.missingRequirement(game);
		if (newRotationFn != null)
			missingRequirement |= newRotationFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= fromFn.willCrash(game);
		willCrash |= distanceFn.willCrash(game);

		if (stepMove != null)
			willCrash |= stepMove.willCrash(game);
		if (newRotationFn != null)
			willCrash |= newRotationFn.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);

		fromFn.preprocess(game);
		distanceFn.preprocess(game);
		if (stepMove != null)
			stepMove.preprocess(game);
		if (newRotationFn != null)
			newRotationFn.preprocess(game);

		if (isStatic())
			precomputedRegion = eval(new Context(game, null));
	}

	/**
	 * @param context         The context.
	 * @param realType        The SiteType of the site.
	 * @param from            The origin of the step move.
	 * @param goRule          The rule to step.
	 * @param component       The component at site1.
	 * @param facingDirection The facing direction of the piece in site1.
	 * @param rotation        The rotation of the piece.
	 * @return The to positions of the step move.
	 */
	public TIntArrayList stepMove
	(
			final Context context, 
			final SiteType realType, 
			final int from,
			final BooleanFunction goRule, 
			final Component component,
			final DirectionFacing facingDirection,
			final int rotation
	)
	{
		final TIntArrayList stepTo = new TIntArrayList();
		final int origFrom = context.from();
		final int origTo = context.to();
		final Topology graph = context.topology();

		final List<? extends TopologyElement> elements = graph.getGraphElements(realType);
		final TopologyElement fromV = elements.get(from);
		context.setFrom(from);
		final List<AbsoluteDirection> directions = stepMove.directions().convertToAbsolute(realType, fromV, component, facingDirection,
				Integer.valueOf(rotation), context);
		for (final AbsoluteDirection direction : directions)
		{
			final List<game.util.graph.Step> steps = graph.trajectories().steps(realType, from, realType, direction);
			for (final game.util.graph.Step step : steps)
			{
				final int to = step.to().id();
				context.setTo(to);
				if (goRule.eval(context))
					stepTo.add(to);
			}
		}

		context.setTo(origTo);
		context.setFrom(origFrom);

		return stepTo;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{		
		String stepMoveString = "";
		if (stepMove != null)
			stepMoveString = " when applying " + stepMove.toEnglish(game);
		
		String relationString = "";
		if (relation != null)
			relationString = " for the " + relation.name() + " relations";
		
		return "the sites which are " + distanceFn.toEnglish(game) + " spaces from site " + fromFn.toEnglish(game) + stepMoveString + relationString;
	}

	//-------------------------------------------------------------------------
		
}
