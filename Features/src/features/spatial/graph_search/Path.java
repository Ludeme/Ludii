package features.spatial.graph_search;

import java.util.List;

import features.spatial.Walk;
import other.topology.TopologyElement;

/**
 * A Path used in graph search algorithms for features. A path consists of a
 * starting site, a destination site (may be null for off-board, or may be
 * the same as the start vertex for a 0-length path), and a Walk that moves from
 * the start to the destination.
 * 
 * @author Dennis Soemers
 */
public class Path 
{
	
	//-------------------------------------------------------------------------
	
	/** List of sites on this path */
	protected final List<TopologyElement> sites;
	
	/** */
	protected final Walk walk;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param sites
	 * @param walk
	 */
	public Path(final List<TopologyElement> sites, final Walk walk)
	{
		this.sites = sites;
		this.walk = walk;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Destination Vertex
	 */
	public TopologyElement destination()
	{
		return sites.get(sites.size() - 1);
	}
	
	/**
	 * @return Start vertex
	 */
	public TopologyElement start()
	{
		return sites.get(0);
	}
	
	/**
	 * @return All sites on the path
	 */
	public List<TopologyElement> sites()
	{
		return sites;
	}
	
	/**
	 * @return The Walk
	 */
	public Walk walk()
	{
		return walk;
	}
	
	//-------------------------------------------------------------------------

}
