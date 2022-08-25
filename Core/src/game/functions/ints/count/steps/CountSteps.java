package game.functions.ints.count.steps;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.equipment.component.Component;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.rules.play.moves.nonDecision.effect.Step;
import game.types.board.RelationType;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.DirectionFacing;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.IntArrayFromRegion;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.state.container.ContainerState;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Returns the number of steps between two sites.
 * 
 * @author Eric.Piette
 */
@Hide
public final class CountSteps extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Cell/Edge/Vertex. */
	private SiteType type;

	/** The step relation. */
	private final RelationType relation;

	/** The first site. */
	private final IntFunction site1Fn;

	/** New Rotation after each step move. */
	private final IntFunction newRotationFn;

	/** The second region. */
	private final IntArrayFromRegion region2;

	/** The specific step move. */
	private final Step stepMove;

	/**
	 * 
	 * @param type        The graph element type [default site type of the board].
	 * @param relation    The relation type of the steps [Adjacent].
	 * @param stepMove    Define a particular step move to step.
	 * @param newRotation Define a new rotation at each step move in using the (value) iterator for the rotation.
	 * @param site1       The first site.
	 * @param region2     The second region.
	 */
	public CountSteps
	(
		@Opt        final SiteType           type, 
		@Opt        final RelationType       relation,
	    @Opt        final Step               stepMove,
		@Opt @Name	final IntFunction        newRotation,
		            final IntFunction        site1, 
			        final IntArrayFromRegion region2
	)
	{
		this.type = type;
		site1Fn = site1;
		this.region2 = region2;
		this.relation = (relation == null) ? RelationType.Adjacent : relation;
		this.stepMove = stepMove;
		newRotationFn = newRotation;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final SiteType realType = (type == null) ? context.board().defaultSite() : type;
		final int site1 = site1Fn.eval(context);
		final TIntArrayList sites2 = new TIntArrayList(region2.eval(context));

		if (site1 < 0)
			return 0;

		for (int i = sites2.size() - 1; i >= 0; i--)
			if (sites2.get(i) < 0)
				sites2.removeAt(i);

		if (sites2.size() == 0)
			return 0;

		if (stepMove == null)
		{
			int min = context.board().topology().distancesToOtherSite(realType)[site1][sites2.get(0)];
			for (int i = 1; i < sites2.size(); i++)
			{
				final int distance = context.board().topology().distancesToOtherSite(realType)[site1][sites2.get(i)];
				if (min > distance)
					min = distance;
			}
			return min;
		}
		else // We count the number of steps in using the specific step move defined.
		{
			final Topology graph = context.topology();
			final int maxSize = graph.getGraphElements(realType).size();
			if (site1 >= maxSize)
				return Constants.INFINITY;

			for (int i = sites2.size() - 1; i >= 0; i--)
				if (sites2.get(i) >= maxSize)
					sites2.removeAt(i);

			if (sites2.size() == 0)
				return Constants.INFINITY;

			if (sites2.contains(site1))
				return 0;

			int numSteps = 1;
			final TIntArrayList currList = new TIntArrayList();

			final ContainerState cs = context.containerState(0);
			final int what = cs.what(site1, realType);
			int rotation = cs.rotation(site1, realType);
			DirectionFacing facingDirection = null;
			Component component = null;
			if(what != 0)
			{
				component = context.components()[what];
				facingDirection = component.getDirn();
			}
			
			
			final TIntArrayList originStepMove = stepMove(context, realType, site1, stepMove.goRule(), component, facingDirection, rotation);
			for (int i = 0; i < originStepMove.size(); i++)
			{
				final int to = originStepMove.get(i);
				if (!currList.contains(to))
					currList.add(to);
			}

			final TIntArrayList nextList = new TIntArrayList();
			final TIntArrayList sitesChecked = new TIntArrayList();
			sitesChecked.add(site1);
			sitesChecked.addAll(currList);

			while (!currList.isEmpty() && !containsAtLeastOneElement(currList, sites2))
			{
				for (int i = 0; i < currList.size(); i++)
				{
					final int newSite = currList.get(i);

					if(newRotationFn != null)
					{
						final int originValue = context.value();
						context.setValue(rotation);
						rotation = newRotationFn.eval(context);
						context.setValue(originValue);
					}
					
					final TIntArrayList stepMoves = stepMove(context, realType, newSite, stepMove.goRule(), component, facingDirection, rotation);
					for (int j = 0; j < stepMoves.size(); j++)
					{
						final int to = stepMoves.get(j);
						if (!sitesChecked.contains(to) && !nextList.contains(to))
							nextList.add(to);
					}
				}

				sitesChecked.addAll(currList);
				currList.clear();
				currList.addAll(nextList);

				nextList.clear();

				++numSteps;
			}

			if (containsAtLeastOneElement(currList, sites2))
				return numSteps;

			return Constants.INFINITY;
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		if (stepMove != null)
			return false;

		return site1Fn.isStatic() && region2.isStatic();
	}

	@Override
	public String toString()
	{
		return "CountSteps()";
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = site1Fn.gameFlags(game) | region2.gameFlags(game);

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
		concepts.or(site1Fn.concepts(game));
		concepts.or(region2.concepts(game));
		concepts.or(SiteType.concepts(type));
		concepts.set(Concept.Distance.id(), true);
		if (newRotationFn != null)
			concepts.or(newRotationFn.concepts(game));

		if (stepMove != null)
			concepts.or(stepMove.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(site1Fn.writesEvalContextRecursive());
		writeEvalContext.or(region2.writesEvalContextRecursive());
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
		readEvalContext.or(site1Fn.readsEvalContextRecursive());
		readEvalContext.or(region2.readsEvalContextRecursive());
		if (stepMove != null)
			readEvalContext.or(stepMove.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		site1Fn.preprocess(game);
		region2.preprocess(game);

		if (stepMove != null)
			stepMove.preprocess(game);
		if (newRotationFn != null)
			newRotationFn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		missingRequirement |= site1Fn.missingRequirement(game);
		missingRequirement |= region2.missingRequirement(game);

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
		willCrash |= site1Fn.willCrash(game);
		willCrash |= region2.willCrash(game);

		if (stepMove != null)
			willCrash |= stepMove.willCrash(game);
		if (newRotationFn != null)
			willCrash |= newRotationFn.willCrash(game);
		return willCrash;
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
		final List<AbsoluteDirection> directions = stepMove.directions().convertToAbsolute(realType, fromV, component,
				facingDirection, Integer.valueOf(rotation), context);
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

	/**
	 * @param list        List containing.
	 * @param listToCheck elements to check if they are in the list.
	 * @return True if at least one element of listToCheck is in list.
	 */
	public static boolean containsAtLeastOneElement(final TIntArrayList list, final TIntArrayList listToCheck)
	{
		for (int i = 0; i < listToCheck.size(); i++)
		{
			final int value = listToCheck.get(i);
			if (list.contains(value))
				return true;
		}
		return false;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{		
		String stepString = " step moves";
		if (stepMove != null)
			stepString = " " + stepMove.toEnglish(game);
		
		return "the number of " + relation.name() + stepString + " from " + type.name() + " " + site1Fn.toEnglish(game) + " to " + region2.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------
}
