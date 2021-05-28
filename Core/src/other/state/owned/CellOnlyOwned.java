package other.state.owned;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import game.Game;
import game.types.board.SiteType;
import gnu.trove.list.array.TIntArrayList;
import other.location.CellOnlyLocation;
import other.location.Location;
import other.state.OwnedIndexMapper;

/**
 * A version of Owned for games that only use Cell (no other site types)
 *
 * @author Dennis Soemers
 */
public final class CellOnlyOwned implements Owned, Serializable
{
	//-------------------------------------------------------------------------
	
	/** */
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/**
	 * All the positions (site & level) where is a piece owned by a player. PlayerId
	 * --> ComponentId --> Positions.
	 */
	protected final List<CellOnlyLocation>[][] locations;
	
	/** Our index mapper */
	protected final OwnedIndexMapper indexMapper;
	
	//-------------------------------------------------------------------------

	/**
	 * To init the positions of the owned site
	 * 
	 * @param game
	 */
	@SuppressWarnings("unchecked")
	public CellOnlyOwned(final Game game)
	{
		indexMapper = new OwnedIndexMapper(game);
		
		locations = (List<CellOnlyLocation>[][]) new List<?>[game.players().size() + 1][];
		for (int p = 0; p <= game.players().size(); p++)
		{
			locations[p] = (List<CellOnlyLocation>[]) new List<?>[indexMapper.numValidIndices(p)];
			for (int i = 0; i < locations[p].length; i++)
			{
				locations[p][i] = new ArrayList<CellOnlyLocation>();
			}
		}
	}

	/**
	 * Copy Constructor.
	 * 
	 * @param other
	 */
	@SuppressWarnings("unchecked")
	private CellOnlyOwned(final CellOnlyOwned other)
	{
		// We can simply copy the reference for this one
		this.indexMapper = other.indexMapper;
		
		// Need deep copies here
		this.locations = (List<CellOnlyLocation>[][]) new List<?>[other.locations.length][];
		
		for (int p = 0; p < other.locations.length; ++p)
		{
			this.locations[p] = (List<CellOnlyLocation>[]) new List<?>[other.locations[p].length];
			for (int i = 0; i < other.locations[p].length; ++i)
			{
				final List<CellOnlyLocation> otherPositionsComp = other.locations[p][i];
				final List<CellOnlyLocation> newPositionsComp = new ArrayList<CellOnlyLocation>(otherPositionsComp.size());
				
				for (int k = 0; k < otherPositionsComp.size(); ++k)
				{
					newPositionsComp.add((CellOnlyLocation) otherPositionsComp.get(k).copy());
				}
				
				this.locations[p][i] = newPositionsComp;
			}
		}
	}
	
	@Override
	public Owned copy()
	{
		return new CellOnlyOwned(this);
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
		final TIntArrayList levels = new TIntArrayList();
		final List<CellOnlyLocation> locs = locations[playerId][indexMapper.compIndex(playerId, componentId)];
		for (final Location pos : locs)
		{
			if (pos.site() == site)
			{
				levels.add(pos.level());
			}
		}

		return levels;
	}

	@Override
	public TIntArrayList sites(final int playerId, final int componentId)
	{
		final int indexMapped = indexMapper.compIndex(playerId, componentId);
		if (indexMapped < 0)
			return new TIntArrayList();
		
		final TIntArrayList sites = new TIntArrayList();
		final List<CellOnlyLocation> locs = locations[playerId][indexMapped];
		for (final Location loc : locs)
		{
			if (!sites.contains(loc.site()))
				sites.add(loc.site());
		}
		return sites;
	}

	@Override
	public TIntArrayList sites(final int playerId)
	{
		final TIntArrayList sites = new TIntArrayList();
		for (int i = 0; i < locations[playerId].length; i++)
		{
			final List<CellOnlyLocation> locs = locations[playerId][i];
			for (final CellOnlyLocation loc : locs)
			{
				if (!sites.contains(loc.site()))
					sites.add(loc.site());
			}
		}
		return sites;
	}

	@Override
	public TIntArrayList sitesOnTop(final int playerId)
	{
		final TIntArrayList sites = new TIntArrayList();
		for (int i = 0; i < locations[playerId].length; i++)
		{
			final List<CellOnlyLocation> locs = locations[playerId][i];
			for (final CellOnlyLocation loc : locs)
			{
				if (!sites.contains(loc.site()))
					sites.add(loc.site());
			}
		}
		return sites;
	}

	@Override
	public List<CellOnlyLocation> positions(final int playerId, final int componentId)
	{
		final int indexMapped = indexMapper.compIndex(playerId, componentId);
		if (indexMapped < 0)
			return new ArrayList<CellOnlyLocation>();
		return locations[playerId][indexMapped];
	}

	@Override
	public List<CellOnlyLocation>[] positions(final int playerId)
	{
		return locations[playerId];
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void remove(final int playerId, final int componentId, final int pieceLoc, final SiteType type)
	{
		final List<CellOnlyLocation> compPositions = locations[playerId][indexMapper.compIndex(playerId, componentId)];
		for (int i = 0; i < compPositions.size(); /** */)
		{
			if (compPositions.get(i).site() == pieceLoc)
			{
				compPositions.remove(i);
			}
			else
			{
				++i;
			}
		}
	}

	@Override
	public void remove(final int playerId, final int componentId, final int pieceLoc, final int level,
			final SiteType type)
	{
		final List<CellOnlyLocation> locs = locations[playerId][indexMapper.compIndex(playerId, componentId)];
		for (int i = 0; i < locs.size(); i++)
		{
			if 
			(
				locs.get(i).site() == pieceLoc
				&& 
				locs.get(i).level() == level
			)
			{
				locs.remove(i);
				i--;
			}
		}
		
		for (int idPlayer = 1; idPlayer < locations.length; idPlayer++)
		{
			for (int i = 0; i < locations[idPlayer].length; i++)
			{
				for (int idPos = 0; idPos < locations[idPlayer][i].size(); idPos++)
				{
					final int sitePos = locations[idPlayer][i].get(idPos).site();
					final int levelPos = locations[idPlayer][i].get(idPos).level();
					if (sitePos == pieceLoc && levelPos > level)
					{
						locations[idPlayer][i].get(idPos).decrementLevel();
					}
				}
			}
		}
	}

	@Override
	public void add(final int playerId, final int componentId, final int pieceLoc, final SiteType type)
	{
		assert (type == SiteType.Cell);
		locations[playerId][indexMapper.compIndex(playerId, componentId)].add(new CellOnlyLocation(pieceLoc));
	}

	@Override
	public void add(final int playerId, final int componentId, final int pieceLoc, final int level, final SiteType type)
	{
		assert (type == SiteType.Cell);
		locations[playerId][indexMapper.compIndex(playerId, componentId)].add(new CellOnlyLocation(pieceLoc, level));
	}
	
	//-------------------------------------------------------------------------

}
