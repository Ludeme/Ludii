package game.functions.ints.board;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.types.board.SiteType;
import main.Constants;
import other.context.Context;
import other.topology.TopologyElement;

/**
 * Returns the phase of a graph element on the board.
 * 
 * @author Eric.Piette
 * 
 * @remarks If the graph element is not on the main board, the ludeme returns
 *          (Undefined) -1.
 */
public final class Phase extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The index of the graph element. */
	private final IntFunction indexFn;

	/** Cell/Edge/Vertex. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * The phase of a vertex in the board.
	 * 
	 * @param of   The index of the element.
	 * @param type Type of graph element.
	 * 
	 * @example (phase of:(last To))
	 */
	public Phase
	(
		@Opt       final SiteType    type,
		     @Name final IntFunction of
	)
	{
		this.indexFn = of;
		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final int index = indexFn.eval(context);

		if (index < 0)
			return Constants.UNDEFINED;

		final other.topology.Topology graph = context.topology();
		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
		final TopologyElement element = graph.getGraphElements(realType).get(index);

		return element.phase();
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return indexFn.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		long stateFlag = indexFn.gameFlags(game);
		stateFlag |= SiteType.gameFlags(type);
		return stateFlag;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.or(indexFn.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(indexFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(indexFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		indexFn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= indexFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= indexFn.willCrash(game);
		return willCrash;
	}
}
