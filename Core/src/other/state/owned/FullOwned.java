package other.state.owned;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import game.Game;
import game.types.board.SiteType;
import gnu.trove.list.array.TIntArrayList;
import other.location.FullLocation;
import other.location.Location;
import other.state.OwnedIndexMapper;

/**
 * A "Full" version of Owned, with all the data we could ever need (no optimisations)
 *
 * @author Dennis Soemers
 */
public final class FullOwned implements Owned, Serializable
{
	//-------------------------------------------------------------------------
	
	/** */
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/**
	 * All the positions (site & level) where is a piece owned by a player. PlayerId
	 * --> ComponentId --> Positions.
	 */
	protected final List<FullLocation>[][] locations;
	
	/** Our index mapper */
	protected final OwnedIndexMapper indexMapper;
	
	//-------------------------------------------------------------------------

	/**
	 * To init the positions of the owned site
	 * 
	 * @param game
	 */
	@SuppressWarnings("unchecked")
	public FullOwned(final Game game)
	{
		indexMapper = new OwnedIndexMapper(game);
		
		locations = (List<FullLocation>[][]) new List<?>[game.players().size() + 1][];
		for (int p = 0; p <= game.players().size(); p++)
		{
			locations[p] = (List<FullLocation>[]) new List<?>[indexMapper.numValidIndices(p)];
			for (int i = 0; i < locations[p].length; i++)
			{
				locations[p][i] = new ArrayList<FullLocation>();
			}
		}
	}

	/**
	 * Copy Constructor.
	 * 
	 * @param other
	 */
	@SuppressWarnings("unchecked")
	private FullOwned(final FullOwned other)
	{
		// We can simply copy the reference for this one
		this.indexMapper = other.indexMapper;
		
		// Need deep copies here
		this.locations = (List<FullLocation>[][]) new List<?>[other.locations.length][];
		
		for (int p = 0; p < other.locations.length; ++p)
		{
			this.locations[p] = (List<FullLocation>[]) new List<?>[other.locations[p].length];
			for (int i = 0; i < other.locations[p].length; ++i)
			{
				final List<FullLocation> otherPositionsComp = other.locations[p][i];
				final List<FullLocation> newPositionsComp = new ArrayList<FullLocation>(otherPositionsComp.size());
				
				for (int k = 0; k < otherPositionsComp.size(); ++k)
				{
					newPositionsComp.add((FullLocation) otherPositionsComp.get(k).copy());
				}
				
				this.locations[p][i] = newPositionsComp;
			}
		}
	}
	
	@Override
	public Owned copy()
	{
		return new FullOwned(this);
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
		final int compIndex = indexMapper.compIndex(playerId, componentId);
		
		if (compIndex >= 0)
		{
			final List<FullLocation> locs = locations[playerId][compIndex];
			for (final Location pos : locs)
			{
				if (pos.site() == site)
				{
					levels.add(pos.level());
				}
			}
		}

		return levels;
	}

	@Override
	public TIntArrayList sites(final int playerId, final int componentId)
	{
		final TIntArrayList sites = new TIntArrayList();
		final int compIndex = indexMapper.compIndex(playerId, componentId);
		
		if (compIndex >= 0)
		{
			final List<FullLocation> locs = locations[playerId][compIndex];
			for (final Location loc : locs)
			{
				if (!sites.contains(loc.site()))
					sites.add(loc.site());
			}
		}
			
		return sites;
	}

	@Override
	public TIntArrayList sites(final int playerId)
	{
		final TIntArrayList sites = new TIntArrayList();
		for (int i = 0; i < locations[playerId].length; i++)
		{
			final List<FullLocation> locs = locations[playerId][i];
			for (final FullLocation loc : locs)
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
			final List<FullLocation> locs = locations[playerId][i];
			for (final FullLocation loc : locs)
			{
				if (!sites.contains(loc.site()))
					sites.add(loc.site());
			}
		}
		return sites;
	}

	@Override
	public List<FullLocation> positions(final int playerId, final int componentId)
	{
		final int indexMapped = indexMapper.compIndex(playerId, componentId);
		if (indexMapped < 0)
			return new ArrayList<FullLocation>();
		return locations[playerId][indexMapped];
	}

	@Override
	public List<FullLocation>[] positions(final int playerId)
	{
		return locations[playerId];
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void remove(final int playerId, final int componentId, final int pieceLoc, final SiteType type)
	{
		final List<FullLocation> compPositions = locations[playerId][indexMapper.compIndex(playerId, componentId)];
		for (int i = 0; i < compPositions.size(); /** */)
		{
			if (compPositions.get(i).site() == pieceLoc
					&& (type == null || compPositions.get(i).siteType().equals(type)))
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
		final List<FullLocation> locs = locations[playerId][indexMapper.compIndex(playerId, componentId)];
		for (int i = 0; i < locs.size(); i++)
		{
			if 
			(
				locs.get(i).site() == pieceLoc
				&& 
				locs.get(i).level() == level
					&& (type == null || locs.get(i).siteType().equals(type))
			)
			{
				locs.remove(i);
				i--;
			}
		}
		
		for (int idPlayer = 0; idPlayer < locations.length; idPlayer++)
		{
			for (int i = 0; i < locations[idPlayer].length; i++)
			{
				for (int idPos = 0; idPos < locations[idPlayer][i].size(); idPos++)
				{
					final int sitePos = locations[idPlayer][i].get(idPos).site();
					final int levelPos = locations[idPlayer][i].get(idPos).level();
					
					if (sitePos == pieceLoc && levelPos > level
							&& (type == null || i >= locs.size() || locations[idPlayer][i].get(idPos).siteType().equals(type)))
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
		locations[playerId][indexMapper.compIndex(playerId, componentId)].add(new FullLocation(pieceLoc, type));
	}

	@Override
	public void add(final int playerId, final int componentId, final int pieceLoc, final int level, final SiteType type)
	{
		locations[playerId][indexMapper.compIndex(playerId, componentId)].add(new FullLocation(pieceLoc, level, type));
	}
	
	//-------------------------------------------------------------------------

}
