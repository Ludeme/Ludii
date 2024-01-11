package game.util.equipment;

import java.io.Serializable;
import java.util.List;

import annotations.Hide;
import game.Game;
import game.equipment.container.board.Board;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.graph.Step;
import gnu.trove.list.array.TIntArrayList;
import main.collections.ChunkSet;
import other.BaseLudeme;
import other.topology.Edge;
import other.topology.SiteFinder;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Defines a region of sites within a container.
 * 
 * @author cambolbro
 */
public final class Region extends BaseLudeme implements Serializable
{
	/** */
	private static final long serialVersionUID = 1L;

	/** Sites in the region. */
	private final ChunkSet bitSet;

	private final String name;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor with a name, a board and coords.
	 * 
	 * @param name
	 * @param board
	 * @param coords
	 */
	@Hide
	public Region
	(
		final String    name, 
		final Board     board, 
		final String... coords
	)
	{
		assert(board != null);
		
		bitSet = new ChunkSet(1, board.topology().cells().size());
		for (final String coord : coords)
		{
			final TopologyElement element = SiteFinder.find(board, coord, SiteType.Cell);
			if (element == null)
				System.out.println("** Region: Coord " + coord + " not found.");
			else
				bitSet.setChunk(element.index(), 1);
		}
		this.name = name;
	}

	/**
	 * Constructor with a counter.
	 * 
	 * @param count
	 */
	@Hide
	public Region(final int count)
	{
		bitSet = new ChunkSet(1, count);
		bitSet.set(0, count);
		name = "?";
	}

	/**
	 * Constructor with a ChunkSet.
	 * 
	 * @param bitSet
	 */
	@Hide
	public Region(final ChunkSet bitSet)
	{
		this.bitSet = bitSet.clone();
		name = "?";
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 */
	@Hide
	public Region(final Region other)
	{
		bitSet = other.bitSet().clone();
		name = "?";
	}

	/**
	 * Constructor for an empty region.
	 */
	@Hide
	public Region()
	{
		bitSet = new ChunkSet();
		name = "?";
	}

	/**
	 * Constructor with an array of int.
	 * @param bitsToSet
	 */
	@Hide
	public Region(final int[] bitsToSet)
	{
		bitSet = new ChunkSet();
		
		// In practice we very often pass arrays sorted from low to high, or
		// at least partially sorted as such. A reverse loop through such an
		// array is more efficient, since it lets us set the highest bit 
		// as early as possible, which means our ChunkSet can be correctly
		// sized early on and doesn't need many subsequent re-sizes
		for (int i = bitsToSet.length - 1; i >= 0; --i)
			bitSet.set(bitsToSet[i]);
		
		name = "?";
	}

	/**
	 * Constructor with a list of elements.
	 * 
	 * @param elements the graph elements.
	 */
	@Hide
	public Region(final List<? extends TopologyElement> elements)
	{
		bitSet = new ChunkSet();
		for (final TopologyElement v : elements)
			bitSet.set(v.index());
		name = "?";
	}
	
	/**
	 * Constructor with a list
	 * 
	 * @param list the list of sites.
	 */
	@Hide
	public Region(final TIntArrayList list)
	{
		bitSet = new ChunkSet();
		for (int i = 0 ; i < list.size();i++)
			bitSet.set(list.get(i));
		name = "?";
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The ChunkSet representation of the sites.
	 */
	public ChunkSet bitSet()
	{
		return bitSet;
	}

	/**
	 * @return Region name.
	 */
	public String name()
	{
		return name;
	}
	
	/**
	 * @return Number of set entries.
	 */
	public int count()
	{
		return bitSet.cardinality();
	}

	/**
	 * @return True if the region is empty.
	 */
	public boolean isEmpty()
	{
		return bitSet.isEmpty();
	}

	/**
	 * @return Array of ints containing the indices in the BitSet that are set.
	 */
	public int[] sites()
	{
		final int[] sites = new int[count()];

		for (int i = 0, n = bitSet.nextSetBit(0); n >= 0; ++i, n = bitSet.nextSetBit(n + 1))
			sites[i] = n;
		
		return sites;
	}

	//-------------------------------------------------------------------------

	/**
	 * To set a new count.
	 * @param newCount
	 */
	public void set(final int newCount)
	{
		bitSet.clear();
		bitSet.set(0, newCount);
	}

	/**
	 * To set the current bitset with another region.
	 * @param other
	 */
	public void set(final Region other)
	{
		//bitSet = (ChunkSet) other.bitSet().clone();
		bitSet.clear();
		bitSet.or(other.bitSet());
	}

	/**
	 * @param n
	 * @return The bit.
	 */
	public int nthValue(final int n)
	{
		int bit = -1;

		for (int i = 0; i <= n; ++i)
		{
			bit = bitSet.nextSetBit(bit + 1);
		}

		return bit;
	}

	/**
	 * To add a new site in the region.
	 * 
	 * @param val
	 */

	public void add(final int val)
	{
		bitSet.set(val);
	}

	/**
	 * To remove a site in the region.
	 * 
	 * @param val
	 */

	public void remove(final int val)
	{
		bitSet.clear(val);
	}

	/**
	 * @param loc
	 * @return true if this location is in this region
	 */
	public boolean contains(final int loc)
	{
		return bitSet.get(loc);
	}
	
	/**
	 * 
	 * 
	 * @param n
	 */

	public void removeNth(final int n)
	{
		remove(nthValue(n));
	}

	/**
	 * Performs a logical OR with this Region's BitSet and the other Region's
	 * BitSet. NOTE: this method modifies this Region, it does not create a new
	 * Region!
	 * 
	 * @param other
	 */
	public void union(final Region other)
	{
		bitSet.or(other.bitSet());
	}
	
	/**
	 * Performs a logical OR with this Region's BitSet and the other BitSet. 
	 * NOTE: this method modifies this Region, it does not create a new Region!
	 * 
	 * @param other
	 */
	public void union(final ChunkSet other)
	{
		bitSet.or(other);
	}

	/**
	 * Performs a logical And with this Region's BitSet and the other Region's
	 * BitSet. NOTE: this method modifies this Region, it does not create a new
	 * Region!
	 * 
	 * @param other
	 */
	public void intersection(final Region other)
	{
		bitSet.and(other.bitSet());
	}

	/**
	 * Removes bits in other from this bitset. 
	 * NOTE: this method modifies this Region, it does not create a new Region!
	 * 
	 * @param other
	 */
	public void remove(final Region other)
	{
		bitSet.andNot(other.bitSet());
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (/*(bitSet == null) ? 0 :*/ bitSet.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object other)
	{
		return (other instanceof Region) && bitSet.equals(((Region) other).bitSet);
	}

	//-------------------------------------------------------------------------

	/**
	 * Expand this region to adjacent cells a specified number of times.
	 * 
	 * @param region    The region.
	 * @param graph     The topology.
	 * @param numLayers The number of layers.
	 * @param type      The graph element type.
	 */
	public static void expand(final Region region, final Topology graph, final int numLayers, final SiteType type)
	{
		final List<? extends TopologyElement> elements = graph.getGraphElements(type);
		for (int layer = 0; layer < numLayers; layer++)
		{
			final ChunkSet nbors = new ChunkSet();
			
			final int[] sites = region.sites();
			for (final int site : sites)
			{
				final TopologyElement element = elements.get(site);
				for (final TopologyElement elementAdj : element.adjacent())
					nbors.set(elementAdj.index(), true);
			}
			
			region.union(nbors);
		}
	}
	
	/**
	 * Expand this region to one direction choice.
	 * 
	 * @param region     The region to expand.
	 * @param graph      The graph.
	 * @param numLayers  The number of layers.
	 * @param dirnChoice The direction.
	 * @param type       The graph element type.
	 */
	public static void expand
	(
		final Region region, 
		final Topology graph, 
		final int numLayers, 
		final AbsoluteDirection dirnChoice, 
		final SiteType type
	)
	{
		for (int i = 0; i < numLayers; i++)
		{
			final int[] sites = region.sites();

			if (type.equals(SiteType.Edge))
			{
				for (final int site : sites)
				{
					final Edge edge = graph.edges().get(site);
					for (final Edge edgeAdj : edge.adjacent())
						if (!region.contains(edgeAdj.index()))
							region.add(edgeAdj.index());
				}
			}
			else
			{
				for (final int site : sites)
				{
					final List<Step> steps = graph.trajectories().steps(type, site, dirnChoice);

					for (final Step step : steps)
					{
						if (step.from().siteType() != step.to().siteType())
							continue;

						final int to = step.to().id();

						if (!region.contains(to))
							region.add(to);
					}
				}
			}
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		String str = "[";

		for (int n = bitSet.nextSetBit(0); n >= 0; n = bitSet.nextSetBit(n + 1))
			str += n + ",";
		if (str.length() > 1)
			str = str.substring(0, str.length() - 1);
		str += "]";
		return str;
	}
	
	@Override
	public String toEnglish(final Game game)
	{
		String regionName = "";
		if (!name.equals("?"))
			regionName = "region \"" + name + "\" ";
		
		return regionName + toString();
	}

	//-------------------------------------------------------------------------

}
