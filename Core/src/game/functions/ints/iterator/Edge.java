package game.functions.ints.iterator;

import java.util.BitSet;

import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import main.Constants;
import other.ContainerId;
import other.context.Context;
import other.context.EvalContextData;

/**
 * Returns the corresponding edge if both vertices are specified, else returns
 * the current ``edge'' value from the context.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * @remarks This ludeme identifies the value of a move applied to an edge.
 */
public final class Edge extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Index of the Vertex vA. */
	private final IntFunction vAFn;

	/** Index of the Vertex vB. */
	private final IntFunction vBFn;

	/** Precomputed value if possible. */
	private int precomputedValue = Constants.OFF;

	//-------------------------------------------------------------------------

	/**
	 * For returning the index of an edge using the two indices of vertices.
	 * 
	 * @param vA The first vertex of the edge.
	 * @param vB The second vertex of the edge.
	 * @example (edge (from) (to))
	 */
	public Edge
	(
		final IntFunction vA, 
		final IntFunction vB
	)
	{
		this.vAFn = vA;
		this.vBFn = vB;
	}

	/**
	 * For returning the edge value of the context.
	 * 
	 * @example (edge)
	 */
	public Edge()
	{
		this.vAFn = null;
		this.vBFn = null;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (precomputedValue != Constants.OFF)
			return precomputedValue;

		if (vAFn != null && vBFn != null)
		{
			final int va = vAFn.eval(context);
			final int vb = vBFn.eval(context);
			final int cid = new ContainerId(null, null, null, null, vAFn).eval(context);
			final other.topology.Topology graph = context.containers()[cid].topology();
			final other.topology.Edge edge = graph.findEdge(graph.vertices().get(va), graph.vertices().get(vb));
			if (edge != null)
				return edge.index();
			return Constants.OFF;
		}
		
		// If no vertex parameters, the temporary variable edge from the context is returned.
		return context.edge();
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
		long gameFlags = 0l;

		if (vAFn != null)
			gameFlags |= vAFn.gameFlags(game);

		if (vBFn != null)
			gameFlags |= vBFn.gameFlags(game);

		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();

		if (vAFn != null)
			concepts.or(vAFn.concepts(game));

		if (vBFn != null)
			concepts.or(vBFn.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (vAFn != null)
			writeEvalContext.or(vAFn.writesEvalContextRecursive());

		if (vBFn != null)
			writeEvalContext.or(vBFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (vAFn == null && vAFn == null)
		{
			readEvalContext.set(EvalContextData.Edge.id(), true);
		}
		else
		{
			readEvalContext.or(vAFn.readsEvalContextRecursive());
			readEvalContext.or(vBFn.readsEvalContextRecursive());
		}
		return readEvalContext;
	}
	
	@Override
	public BitSet readsEvalContextFlat()
	{
		final BitSet readEvalContext = new BitSet();
		if (vAFn == null && vAFn == null)
			readEvalContext.set(EvalContextData.Edge.id(), true);
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (vAFn != null && vAFn.isStatic() && vBFn != null && vBFn.isStatic())
			precomputedValue = eval(new Context(game, null));
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (vAFn != null)
			missingRequirement |= vAFn.missingRequirement(game);

		if (vBFn != null)
			missingRequirement |= vBFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		if (vAFn != null)
			willCrash |= vAFn.willCrash(game);

		if (vBFn != null)
			willCrash |= vBFn.willCrash(game);
		return willCrash;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "Edge()";
	}
}
