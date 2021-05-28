package game.functions.booleans.is.related;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntFunction;
import game.types.board.RelationType;
import game.types.board.SiteType;
import other.IntArrayFromRegion;
import other.context.Context;
import other.topology.Cell;

/**
 * Checks if two sites are related by a specific relation. Can also checks if a
 * site is related by a specific relation with at least one site of a region.
 * 
 * @author Eric.Piette
 */
@Hide
public final class IsRelated extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//------------------------------------------------------------------------

	/** Which site. */
	protected final IntFunction site;

	/** Which region. */
	protected final IntArrayFromRegion region;
	
	/** Add on Cell/Edge/Vertex. */
	private SiteType type;

	/** Add on Cell/Edge/Vertex. */
	private final RelationType relationType;

	/** Precomputed boolean. */
	private Boolean precomputedBoolean;

	//-------------------------------------------------------------------------

	/**
	 * 
	 * @param relationType The type of relation to check between the graph elements.
	 * @param type         The graph element type [default SiteType of the board].
	 * @param siteA        The first site.
	 * @param regionB      The region of the second site.
	 */
	public IsRelated
	(
			 final RelationType       relationType,
		@Opt final SiteType           type,
			 final IntFunction        siteA,
		     final IntArrayFromRegion regionB
	)
	{
		this.site = siteA;
		this.region = regionB;
		this.type = type;
		this.relationType = relationType;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		if (precomputedBoolean != null)
			return precomputedBoolean.booleanValue();

		final int[] sites = region.eval(context);
		final int location = site.eval(context);
		final other.topology.Topology graph = context.topology();
		
		if ((type == null && context.game().board().defaultSite() != SiteType.Vertex) || type == SiteType.Cell)
		{
			final Cell cellA = graph.cells().get(location);
	
			switch (relationType)
			{
			case Adjacent:
				for (final int st : sites)
				{
					final Cell cellB = graph.cells().get(st);
					if (cellB.adjacent().contains(cellA))
						return true;
				}
				break;
			case Diagonal:
				for (final int st : sites)
				{
					final Cell cellB = graph.cells().get(st);
					if (cellB.diagonal().contains(cellA))
						return true;
				}
				break;
			case All:
				for (final int st : sites)
				{
					final Cell cellB = graph.cells().get(st);
					if (cellB.neighbours().contains(cellA))
						return true;
				}
				break;
			case OffDiagonal:
				for (final int st : sites)
				{
					final Cell cellB = graph.cells().get(st);
					if (cellB.off().contains(cellA))
						return true;
				}
				break;
			case Orthogonal:
				for (final int st : sites)
				{
					final Cell cellB = graph.cells().get(st);
					if (cellB.orthogonal().contains(cellA))
						return true;
				}
				break;
			default:
				break;
			}
		}
		else if ((type == null && context.game().board().defaultSite() != SiteType.Vertex) || type == SiteType.Vertex)
		{
			final other.topology.Vertex vertexA = graph.vertices().get(location);

			switch (relationType)
			{
			case Adjacent:
				for (final int st : sites)
					for (final other.topology.Vertex v : vertexA.adjacent())
						if (v.index() == st)
							return true;
				break;
			case Diagonal:
				for (final int st : sites)
					for (final other.topology.Vertex v : vertexA.diagonal())
						if (v.index() == st)
							return true;
				break;
			case All:
				for (final int st : sites)
					for (final other.topology.Vertex v : vertexA.neighbours())
						if (v.index() == st)
							return true;
				break;
			case OffDiagonal:
				for (final int st : sites)
					for (final other.topology.Vertex v : vertexA.off())
						if (v.index() == st)
							return true;
				break;
			case Orthogonal:
				for (final int st : sites)
					for (final other.topology.Vertex v : vertexA.orthogonal())
						if (v.index() == st)
							return true;
				break;
			default:
				break;

			}
		}
		else
		{
			final other.topology.Edge edgeA = graph.edges().get(location);
			switch (relationType)
			{
			case Adjacent:
				for (final int st : sites)
				{
					for (final other.topology.Edge edgeB : edgeA.vA().edges())
						if (edgeB.index() == st)
							return true;
					for (final other.topology.Edge edgeB : edgeA.vB().edges())
						if (edgeB.index() == st)
							return true;
				}
				break;
			case Diagonal:
				return false;
			case All:
				for (final int st : sites)
				{
					for (final other.topology.Edge edgeB : edgeA.vA().edges())
						if (edgeB.index() == st)
							return true;
					for (final other.topology.Edge edgeB : edgeA.vB().edges())
						if (edgeB.index() == st)
							return true;
				}
				break;
			case OffDiagonal:
				return false;
			case Orthogonal:
				for (final int st : sites)
				{
					for (final other.topology.Edge edgeB : edgeA.vA().edges())
						if (edgeB.index() == st)
							return true;
					for (final other.topology.Edge edgeB : edgeA.vB().edges())
						if (edgeB.index() == st)
							return true;
				}
				break;
			default:
				break;

			}
		}

		return false;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "Related(" + site + "," + region + ")";
	}

	@Override
	public boolean isStatic()
	{
		return region.isStatic() && site.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = region.gameFlags(game) | site.gameFlags(game);

		gameFlags |= SiteType.gameFlags(type);

		return gameFlags;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(region.concepts(game));
		concepts.or(SiteType.concepts(type));
		concepts.or(site.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(region.writesEvalContextRecursive());
		writeEvalContext.or(site.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(region.readsEvalContextRecursive());
		readEvalContext.or(site.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= region.missingRequirement(game);
		missingRequirement |= site.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= region.willCrash(game);
		willCrash |= site.willCrash(game);
		return willCrash;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);

		site.preprocess(game);
		region.preprocess(game);

		if (isStatic())
			precomputedBoolean = Boolean.valueOf(eval(new Context(game, null)));
	}
}