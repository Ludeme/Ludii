package game.functions.booleans.deductionPuzzle;

import java.util.BitSet;
import java.util.List;

import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.region.sites.custom.SitesCustom;
import game.types.board.PuzzleElementType;
import game.types.state.GameType;
import main.StringRoutines;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.state.container.ContainerState;
import other.topology.Edge;
import other.topology.TopologyElement;

/**
 * Returns true if the constraint is satisfied for each element.
 * 
 * @author Eric.Piette
 * 
 * @remarks This is used to test a constraint on each vertex, edge, face or site
 *          with a hint. This works only for deduction puzzles.
 */
public class ForAll extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which type. */
	private final PuzzleElementType type;
	
	/** Constraint to satisfy. */
	private final BooleanFunction constraint;

	//-------------------------------------------------------------------------

	/**
	 * @param type       The type of the graph element.
	 * @param constraint The constraint to check.
	 * @example (forAll Hint (is Count (sites Around (from) includeSelf:True) of:1
	 *          (hint) ) )
	 */
	public ForAll
	(
		final PuzzleElementType type,
		final BooleanFunction   constraint
	)
	{
		this.type = type;
		this.constraint = constraint;
	}
	
	//--------------------------------------------------------------------------

	@Override
	public boolean eval(Context context)
	{
		final int saveTo = context.to();
		final int saveFrom = context.to();
		final int saveHint = context.hint();
		final int saveEdge = context.edge();
		
		if (!type.equals(PuzzleElementType.Hint))
		{
			final List<? extends TopologyElement> elements = context.topology()
					.getGraphElements(PuzzleElementType.convert(type));

			for (int i = 0; i < elements.size(); i++)
			{
				final TopologyElement element = elements.get(i);
				context.setFrom(element.index());
				if (!constraint.eval(context))
				{
					context.setHint(saveHint);
					context.setEdge(saveEdge);
					context.setTo(saveTo);
					context.setFrom(saveFrom);
					return false;
				}
			}
		}
		else
		{
			final Integer[][] regions = context.game().equipment().withHints(context.board().defaultSite());
			final Integer[] hints = context.game().equipment().hints(context.board().defaultSite());
			
			final int size = Math.min(regions.length, hints.length);

			for (int i = 0; i < size; i++)
			{
				// We compute the number of edges with hints
				int nbEdges = 0;
				boolean allEdgesSet = true;
				for (int j = 0; j < regions[i].length; j++)
				{
					nbEdges = 0;
					final int indexVertex = regions[i][j].intValue();
					final ContainerState ps = context.state().containerStates()[0];
					final List<Edge> edges = context.game().board().topology().edges();

					for (int indexEdge = 0; indexEdge < edges.size(); indexEdge++)
					{
						final Edge edge = edges.get(indexEdge);
						if (edge.containsVertex(indexVertex))
						{
							if (ps.isResolvedEdges(indexEdge))
								nbEdges += ps.whatEdge(indexEdge);
							else
								allEdgesSet = false;
						}
					}
				}

//				System.out.println(
//						"number of edges solved for the vertex " + regions[i][j].intValue() + " is " + nbEdges);

				// if all the edges are assigned or the number of edges is greater than the
				// hints we set the nbEdge to satisfy the constraint.
				if (!allEdgesSet && hints[i] != null && nbEdges < hints[i].intValue())
					context.setEdge(hints[i].intValue());
				else
					context.setEdge(nbEdges);

				if (regions[i].length > 0)
					context.setFrom(regions[i][0].intValue());
				if (regions[i].length > 1)
					context.setTo(regions[i][1].intValue());
				if (hints[i] != null)
					context.setHint(hints[i].intValue());


				// System.out.println("Edge: " + context.edge());
//				System.out.println("From: " + context.from());
//				System.out.println("To: " + context.to());
//				System.out.println("Hint: " + context.hint());
//				System.out.println();

				final IntFunction[] setFn = new IntFunction[regions.length];
				for (int h = 0; h < regions[i].length; h++)
					setFn[h] = new IntConstant(regions[i][h].intValue());

				context.setHintRegion(
						new SitesCustom(setFn));

				if (!constraint.eval(context))
				{
					context.setHint(saveHint);
					context.setEdge(saveEdge);
					context.setTo(saveTo);
					context.setFrom(saveFrom);
					return false;
				}
			}
		}
//		else if (type.equals(PuzzleElementType.Cell))
//		{
//			final Integer[][] regions = context.game().equipment().cellsWithHints();
//			final Integer[] hints = context.game().equipment().cellHints();
//		
//			final int size = Math.min(regions.length, hints.length);
//			for (int i = 0; i < size; i++)
//			{		
//				if (regions[i].length > 0)
//					context.setFrom(regions[i][0].intValue());
//				if (regions[i].length > 1)
//					context.setTo(regions[i][1].intValue());
//				if (hints[i] != null)
//					context.setHint(hints[i].intValue());
//				if (!constraint.eval(context))
//				{
//					context.setHint(saveHint);
//					context.setEdge(saveEdge);
//					context.setTo(saveTo);
//					context.setFrom(saveFrom);
//					return false;
//				}
//			}
//		}
		
		context.setHint(saveHint);
		context.setEdge(saveEdge);
		context.setTo(saveTo);
		context.setFrom(saveFrom);
		return true;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return constraint.isStatic();
	}
	
	@Override
	public void preprocess(final Game game)
	{
		if (constraint != null)
			constraint.preprocess(game);
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = GameType.DeductionPuzzle;

		if (constraint != null)
			gameFlags |= constraint.gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.DeductionPuzzle.id(), true);

		if (constraint != null)
			concepts.or(constraint.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		if (constraint != null)
			writeEvalContext.or(constraint.writesEvalContextRecursive());
		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.Hint.id(), true);
		writeEvalContext.set(EvalContextData.HintRegion.id(), true);
		writeEvalContext.set(EvalContextData.Edge.id(), true);
		writeEvalContext.set(EvalContextData.To.id(), true);
		writeEvalContext.set(EvalContextData.From.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		if (constraint != null)
			readEvalContext.or(constraint.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		if (constraint != null)
			missingRequirement |= constraint.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (game.players().count() != 1)
		{
			game.addCrashToReport("The ludeme (forAll ...) is used but the number of players is not 1.");
			willCrash = true;
		}
		willCrash |= super.willCrash(game);
		if (constraint != null)
			willCrash |= constraint.willCrash(game);
		return willCrash;
	}

	/**
	 * @return The graph element.
	 */
	public PuzzleElementType type()
	{
		return type;
	}
	
	/**
	 * @return The constraint.
	 */
	public BooleanFunction constraint() {
		return constraint;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		String str = "";
		str += "AllTrue " + type + ": " + constraint;
		return str;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return constraint.toEnglish(game) + " is true for all " + type.name().toLowerCase() + StringRoutines.getPlural(type.name());
	}
	
	//-------------------------------------------------------------------------

}