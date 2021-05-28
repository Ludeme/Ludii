package other.state.owned;

import java.util.List;

import game.types.board.SiteType;
import gnu.trove.list.array.TIntArrayList;
import other.location.Location;

/**
 * Interface for objects that can quickly tell us which location contain
 * pieces (of certain types) for certain players (without requiring full
 * scans of the containers).
 * 
 * @author Eric.Piette and Dennis Soemers
 */
public interface Owned
{

	//-------------------------------------------------------------------------

	/**
	 * @return A deep copy of this Owned object.
	 */
	public Owned copy();
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param playerId
	 * @param componentId
	 * @return Mapped index that this Owned object would use instead of componentId (for given player)
	 */
	public int mapCompIndex(final int playerId, final int componentId);
	
	/**
	 * @param playerId
	 * @param mappedIndex
	 * @return Reverses a given mapped index back into a component ID (for given player ID)
	 */
	public int reverseMap(final int playerId, final int mappedIndex);

	/**
	 * @param playerId
	 * @param componentId
	 * @param site
	 * @return All the levels on the site owned by the playerId with the
	 *         componentId.
	 */
	public TIntArrayList levels(final int playerId, final int componentId, final int site);

	/**
	 * @param playerId
	 * @param componentId
	 * @return All the sites with at least one specific component owned by the
	 *         player.
	 */
	public TIntArrayList sites(final int playerId, final int componentId);

	/**
	 * @param playerId
	 * @return All the sites with at least one component owned by the player.
	 */
	public TIntArrayList sites(final int playerId);

	/**
	 * @param playerId
	 * @return All the sites owned by a player, only if the top component is owned
	 *         by him.
	 */
	public TIntArrayList sitesOnTop(final int playerId);

	/**
	 * @param playerId
	 * @param componentId
	 * @return All the positions owned by the playerId with the componentId.
	 */
	public List<? extends Location> positions(final int playerId, final int componentId);

	/**
	 * WARNING: the returned array is not indexed directly by component indices, but
	 * by mapped component indices!
	 * 
	 * @param playerId
	 * @return All the positions of all the component owned by the player
	 */
	public List<? extends Location>[] positions(final int playerId);
	
	//-------------------------------------------------------------------------

	/**
	 * To remove a loc (at any level) for a player and a specific component.
	 * 
	 * @param playerId
	 * @param componentId
	 * @param pieceLoc
	 * @param type
	 */
	public void remove(final int playerId, final int componentId, final int pieceLoc, final SiteType type);

	/**
	 * To remove a loc at a specific level for a player and a specific component.
	 * 
	 * @param playerId
	 * @param componentId
	 * @param pieceLoc
	 * @param level
	 * @param type
	 */
	public void remove(final int playerId, final int componentId, final int pieceLoc, final int level,
			final SiteType type);

	/**
	 * To add a loc for a player with a component (at level 0)
	 * 
	 * @param playerId
	 * @param componentId
	 * @param pieceLoc
	 * @param type
	 */
	public void add(final int playerId, final int componentId, final int pieceLoc, final SiteType type);

	/**
	 * To add a loc for a player with a component at a specific level.
	 * 
	 * @param playerId
	 * @param componentId
	 * @param pieceLoc
	 * @param level
	 * @param type
	 */
	public void add(final int playerId, final int componentId, final int pieceLoc, final int level, final SiteType type);
	
	//-------------------------------------------------------------------------
}