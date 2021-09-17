package game.functions.region.sites.incidents;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.ints.IntFunction;
import game.functions.region.BaseRegionFunction;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.util.equipment.Region;
import game.util.moves.Player;
import gnu.trove.list.array.TIntArrayList;
import main.StringRoutines;
import other.context.Context;
import other.state.container.ContainerState;
import other.topology.Cell;
import other.topology.Edge;
import other.topology.Topology;
import other.topology.Vertex;

/**
 * Returns other graph elements connected to a given graph element.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * @remarks Graph elements can be Vertex, Edge or Cell.
 */
@Hide
public final class SitesIncident extends BaseRegionFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Type of the element of the graph. */
	private final SiteType ofType;

	/** Type of the result. */
	private final SiteType resultType;

	/** Index of the element of the graph. */
	private final IntFunction indexFn;
	
	/** Owner of the sites to return. */
	private final IntFunction ownerFn;

	/** If we can, we'll precompute once and cache. */
	private Region precomputedRegion = null;

	//-------------------------------------------------------------------------

	/**
	 * @param resultType The graph type of the result.
	 * @param of         The graph type of the index.
	 * @param at         Index of the element to check.
	 * @param owner      The owner of the site to return.
	 * @param roleOwner  The role of the owner of the site to return.
	 */
	public SitesIncident
	(
			           final SiteType    resultType,
		         @Name final SiteType    of,
		         @Name final IntFunction at,
		@Opt @Or @Name final Player      owner,
		@Opt @Or       final RoleType    roleOwner
	)
	{
		indexFn    = at;
		ofType  = of;
		this.resultType = resultType;
		ownerFn = (owner != null) ? owner.index() : (roleOwner != null) ? RoleType.toIntFunction(roleOwner) : null;
	}

	//-------------------------------------------------------------------------

	@Override
	public Region eval(final Context context)
	{
		if (precomputedRegion != null)
			return precomputedRegion;
		
		switch (ofType)
		{
		case Vertex:
			return evalVertex(context, indexFn.eval(context));
		case Edge:
			return evalEdge(context, indexFn.eval(context));
		case Cell:
			return evalCell(context, indexFn.eval(context));
		default:
			return new Region();
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * @param context
	 * @param index
	 * @return the result from a Cell index
	 */
	private Region evalCell(Context context, final int index)
	{
		final Topology graph = context.topology();
		final TIntArrayList result = new TIntArrayList();

		if (index < 0 || index >= context.topology().cells().size())
			return new Region(result.toArray());

		final Cell cell = graph.cells().get(index);
		
		switch (resultType)
		{		case Cell:
		{
			for (final Edge edge : cell.edges())
				for (final Cell cell2 : edge.cells())
					if (cell2.index() != cell.index())
						result.add(cell2.index());
			break;
		}
		case Edge:
			for (final Edge edge : cell.edges())
				result.add(edge.index());
			break;
		case Vertex:
			for (final Vertex vertex : cell.vertices())
				result.add(vertex.index());
			break;
		default:
			break;
		}

		if(ownerFn == null)
			return new Region(result.toArray());
		else
		{
			final TIntArrayList resultOwner = new TIntArrayList();
			final int owner = ownerFn.eval(context);
			final ContainerState cs = context.containerState(0);
			for(int i = 0 ; i < result.size();i++)
			{
				final int site = result.get(i);
				final int who = cs.who(site, resultType);
				if((who != 0 && owner == context.game().players().size()) || who == owner)
					resultOwner.add(site);
			}
			return new Region(resultOwner.toArray());
		}
	}

	/**
	 * @param context
	 * @param index
	 * @return the result from a Edge index
	 */
	private Region evalEdge(Context context, final int index)
	{
		final Topology graph = context.topology();
		final TIntArrayList result = new TIntArrayList();

		if (index < 0 || index >= context.topology().edges().size())
			return new Region(result.toArray());

		final Edge edge = graph.edges().get(index);
		
		switch (resultType)
		{
		case Vertex:
		{
			result.add(edge.vA().index());
			result.add(edge.vB().index());
			break;
		}
		case Edge:
			for (final Edge edge2: edge.vA().edges())
				if (edge2.index() != edge.index())
					result.add(edge2.index());
			for (final Edge edge2: edge.vB().edges())
				if (edge2.index() != edge.index())
					result.add(edge2.index());
			break;
		case Cell:
			for (final Cell face : edge.cells())
				result.add(face.index());
			break;
		default:
			break;
		}

		if (ownerFn == null)
			return new Region(result.toArray());
		else
		{
			final TIntArrayList resultOwner = new TIntArrayList();
			final int owner = ownerFn.eval(context);
			final ContainerState cs = context.containerState(0);
			for (int i = 0; i < result.size(); i++)
			{
				final int site = result.get(i);
				final int who = cs.who(site, resultType);
				if ((who != 0 && owner == context.game().players().size()) || who == owner)
					resultOwner.add(site);
			}
			return new Region(resultOwner.toArray());
		}
	}

	/**
	 * @param context
	 * @param index
	 * @return the result from a Vertex index
	 */
	private Region evalVertex(Context context, final int index)
	{
		final Topology graph = context.topology();
		final TIntArrayList result = new TIntArrayList();

		if (index < 0 || index >= context.topology().vertices().size())
			return new Region(result.toArray());

		final Vertex vertex = graph.vertices().get(index);
		
		switch (resultType)
		{
		case Cell:
		{
			for (final Cell cell : vertex.cells())
				result.add(cell.index());
			break;
		}
		case Edge:
			for (final Edge edge : vertex.edges())
				result.add(edge.index());
			break;
		case Vertex:
			for (final Edge edge : vertex.edges())
				for (final Cell vertex2 : edge.cells())
					if (vertex2.index() != vertex.index())
						result.add(vertex2.index());
			break;
		default:
			break;
		}

		if (ownerFn == null)
			return new Region(result.toArray());
		else
		{
			final TIntArrayList resultOwner = new TIntArrayList();
			final int owner = ownerFn.eval(context);
			final ContainerState cs = context.containerState(0);
			for (int i = 0; i < result.size(); i++)
			{
				final int site = result.get(i);
				final int who = cs.who(site, resultType);
				if ((who != 0 && owner == context.game().players().size()) || who == owner)
					resultOwner.add(site);
			}
			return new Region(resultOwner.toArray());
		}
	}

	@Override
	public boolean isStatic()
	{
		if (ownerFn != null)
			if (!ownerFn.isStatic())
				return false;

		return indexFn.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = indexFn.gameFlags(game);

		gameFlags |= SiteType.gameFlags(ofType);
		gameFlags |= SiteType.gameFlags(resultType);

		if (ownerFn != null)
			gameFlags |= ownerFn.gameFlags(game);

		return gameFlags;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= indexFn.missingRequirement(game);

		if (ownerFn != null)
			missingRequirement |= ownerFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= indexFn.willCrash(game);

		if (ownerFn != null)
			willCrash |= ownerFn.willCrash(game);
		return willCrash;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(indexFn.concepts(game));
		concepts.or(SiteType.concepts(ofType));
		concepts.or(SiteType.concepts(resultType));

		if (ownerFn != null)
			concepts.or(ownerFn.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(indexFn.writesEvalContextRecursive());

		if (ownerFn != null)
			writeEvalContext.or(ownerFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(indexFn.readsEvalContextRecursive());

		if (ownerFn != null)
			readEvalContext.or(ownerFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		indexFn.preprocess(game);
		if (ownerFn != null)
			ownerFn.preprocess(game);
		
		if (isStatic())
			precomputedRegion = eval(new Context(game, null));
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{		
		String ownerString = "";
		if (ownerFn != null)
			ownerString = " which are owned by Player " + ownerFn.toEnglish(game) + " ";
		
		return "all " + resultType.name() + StringRoutines.getPlural(resultType.name()) + " that are incident of " + ofType.name() + " " + indexFn.toEnglish(game) + ownerString;
	}
	
	//-------------------------------------------------------------------------
		
}
