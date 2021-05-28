package other.state.owned;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import game.Game;
import game.types.board.SiteType;
import gnu.trove.list.array.TIntArrayList;
import main.collections.FastTIntArrayList;
import other.location.FlatVertexOnlyLocation;
import other.state.OwnedIndexMapper;

/**
 * A version of Owned for games that only use Vertices and no levels
 *
 * @author Dennis Soemers
 */
public final class FlatVertexOnlyOwned implements Owned, Serializable
{
	//-------------------------------------------------------------------------
	
	/** */
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/**
	 * All the sites where is a piece owned by a player. PlayerId
	 * --> ComponentId --> Sites.
	 */
	protected final FastTIntArrayList[][] locations;
	
	/** Our index mapper */
	protected final OwnedIndexMapper indexMapper;
	
	//-------------------------------------------------------------------------

	/**
	 * To init the positions of the owned site
	 * 
	 * @param game
	 */
	public FlatVertexOnlyOwned(final Game game)
	{
		indexMapper = new OwnedIndexMapper(game);
		
		locations = new FastTIntArrayList[game.players().size() + 1][];
		for (int p = 0; p <= game.players().size(); p++)
		{
			locations[p] = new FastTIntArrayList[indexMapper.numValidIndices(p)];
			for (int i = 0; i < locations[p].length; i++)
			{
				locations[p][i] = new FastTIntArrayList();
			}
		}
	}

	/**
	 * Copy Constructor.
	 * 
	 * @param other
	 */
	private FlatVertexOnlyOwned(final FlatVertexOnlyOwned other)
	{
		// We can simply copy the reference for this one
		this.indexMapper = other.indexMapper;
		
		// Need deep copies here
		this.locations = new FastTIntArrayList[other.locations.length][];
		
		for (int p = 0; p < other.locations.length; ++p)
		{
			this.locations[p] = new FastTIntArrayList[other.locations[p].length];
			for (int i = 0; i < other.locations[p].length; ++i)
			{
				this.locations[p][i] = new FastTIntArrayList(other.locations[p][i]);
			}
		}
	}
	
	@Override
	public FlatVertexOnlyOwned copy()
	{
		return new FlatVertexOnlyOwned(this);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public int mapCompIndex(final int playerId, final int componentId)
	{
		return indexMapper.compIndex(playerId, componentId);
	}
	
	@Override
	public int reverseMap(final int playerId, final int mappedIndex)
	{
		return indexMapper.reverseMap(playerId, mappedIndex);
	}

	@Override
	public TIntArrayList levels(final int playerId, final int componentId, final int site)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public FastTIntArrayList sites(final int playerId, final int componentId)
	{
		final int mappedIdx = indexMapper.compIndex(playerId, componentId);
		if (mappedIdx >= 0)
			return new FastTIntArrayList(locations[playerId][mappedIdx]);
		else
			return new FastTIntArrayList();
	}

	@Override
	public TIntArrayList sites(final int playerId)
	{
		final TIntArrayList sites = new TIntArrayList();
		for (int i = 0; i < locations[playerId].length; i++)
		{
			// Note: we do no contains() checks because, in non-stacking games,
			// the same site should never occur more than once anyway
			sites.addAll(locations[playerId][i]);
		}
		return sites;
	}

	@Override
	public TIntArrayList sitesOnTop(final int playerId)
	{
		return sites(playerId);
	}

	@Override
	public List<FlatVertexOnlyLocation> positions(final int playerId, final int componentId)
	{
		final int indexMapped = indexMapper.compIndex(playerId, componentId);
		if (indexMapped < 0)
			return new ArrayList<FlatVertexOnlyLocation>();
		final TIntArrayList sites = locations[playerId][indexMapped];
		final List<FlatVertexOnlyLocation> locs = new ArrayList<FlatVertexOnlyLocation>(sites.size());
		for (int i = 0; i < sites.size(); ++i)
		{
			locs.add(new FlatVertexOnlyLocation(sites.getQuick(i)));
		}
		return locs;
	}

	@Override
	public List<FlatVertexOnlyLocation>[] positions(final int playerId)
	{
		final TIntArrayList[] playerSites = locations[playerId];
		@SuppressWarnings("unchecked")
		final List<FlatVertexOnlyLocation>[] playerLocs = (List<FlatVertexOnlyLocation>[]) new List<?>[playerSites.length];
		
		for (int i = 0; i < playerSites.length; ++i)
		{
			final TIntArrayList sites = locations[playerId][i];
						
			if (sites == null)
			{
				playerLocs[i] = null;
			}
			else
			{
				final List<FlatVertexOnlyLocation> locs = new ArrayList<FlatVertexOnlyLocation>(sites.size());
				for (int j = 0; j < sites.size(); ++j)
				{
					locs.add(new FlatVertexOnlyLocation(sites.getQuick(j)));
				}
				
				playerLocs[i] = locs;
			}
		}
		
		return playerLocs;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void remove(final int playerId, final int componentId, final int pieceLoc, final SiteType type)
	{
		final FastTIntArrayList compPositions = locations[playerId][indexMapper.compIndex(playerId, componentId)];
		
		// Since order doesn't matter, we'll do a remove-swap
		final int idx = compPositions.indexOf(pieceLoc);
		if (idx >= 0)
		{
			final int lastIdx = compPositions.size() - 1;
			compPositions.set(idx, compPositions.getQuick(lastIdx));
			compPositions.removeAt(lastIdx);
		}
	}

	@Override
	public void remove(final int playerId, final int componentId, final int pieceLoc, final int level,
			final SiteType type)
	{
		assert (level == 0);
		remove(playerId, componentId, pieceLoc, type);
	}

	@Override
	public void add(final int playerId, final int componentId, final int pieceLoc, final SiteType type)
	{
		assert (type == SiteType.Vertex);
		locations[playerId][indexMapper.compIndex(playerId, componentId)].add(pieceLoc);
	}

	@Override
	public void add(final int playerId, final int componentId, final int pieceLoc, final int level, final SiteType type)
	{
		assert (type == SiteType.Vertex);
		assert (level == 0);
		add(playerId, componentId, pieceLoc, type);
	}
	
	//-------------------------------------------------------------------------

}
