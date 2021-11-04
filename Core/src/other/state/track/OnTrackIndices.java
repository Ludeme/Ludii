package other.state.track;

import java.util.ArrayList;
import java.util.List;

import game.equipment.container.board.Track;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import main.collections.FastTIntArrayList;

/**
 * Structure used to know where are each kind of piece on each track.
 * Note: TrackIndex --> IndexComponent --> IndexOnTrack --> Count.
 * 
 * @author Eric.Piette
 */
public class OnTrackIndices
{
	/** The map for each track to know where are the pieces on each track. */
	protected final List<FastTIntArrayList>[] onTrackIndices;

	/**
	 * The map between the index of the tracks and the corresponding sites.
	 */
	protected final TIntObjectMap<FastTIntArrayList>[] locToIndex;

	/**
	 * Constructor.
	 * 
	 * @param tracks  The list of tracks.
	 * @param numWhat The number of components.
	 */
	@SuppressWarnings("unchecked")
	public OnTrackIndices(final List<Track> tracks, final int numWhat)
	{
		onTrackIndices = new List[tracks.size()];
		locToIndex = new TIntObjectMap[tracks.size()];
		
		for (int trackIdx = 0; trackIdx < tracks.size(); ++trackIdx)
		{
			final Track track = tracks.get(trackIdx);
			final int size = track.elems().length;
			final List<FastTIntArrayList> onTracks = new ArrayList<FastTIntArrayList>();
			for (int i = 0; i < numWhat; i++)
			{
				final FastTIntArrayList indicesTrack = new FastTIntArrayList();
				for (int j = 0; j < size; j++)
					indicesTrack.add(0);
				onTracks.add(indicesTrack);
			}
			
			onTrackIndices[trackIdx] = onTracks;

			final TIntObjectMap<FastTIntArrayList> locToIndexTrack = new TIntObjectHashMap<FastTIntArrayList>();
			for (int j = 0; j < size; j++)
			{
				final int site = track.elems()[j].site;

				if (locToIndexTrack.get(site) == null)
					locToIndexTrack.put(site, new FastTIntArrayList());

				locToIndexTrack.get(site).add(j);
			}
			
			locToIndex[trackIdx] = locToIndexTrack;
		}
	}

	/**
	 * Deep copy.
	 * @param other Structure to be copied
	 */
	@SuppressWarnings("unchecked")
	public OnTrackIndices(final OnTrackIndices other)
	{
		final List<FastTIntArrayList>[] otherOnTrackIndices = other.onTrackIndices;
		onTrackIndices = new List[otherOnTrackIndices.length];
		
		for (int i = 0; i < otherOnTrackIndices.length; ++i)
		{
			final List<FastTIntArrayList> otherOnTracks = otherOnTrackIndices[i];
			final List<FastTIntArrayList> onTracks = new ArrayList<FastTIntArrayList>(otherOnTracks.size());
			
			for (final FastTIntArrayList whatOtherOnTrack : otherOnTracks)
				onTracks.add(new FastTIntArrayList(whatOtherOnTrack));
			
			onTrackIndices[i] = onTracks;
		}

		// This can just be copied by reference
		locToIndex = other.locToIndex;
	}
	
	/**
	 * Constructor that directly uses the given references for its data.
	 * 
	 * @param onTrackIndices
	 * @param locToIndex
	 */
	protected OnTrackIndices
	(
		final List<FastTIntArrayList>[] onTrackIndices, 
		final TIntObjectMap<FastTIntArrayList>[] locToIndex
	)
	{
		this.onTrackIndices = onTrackIndices;
		this.locToIndex = locToIndex;
	}

	//------------------------------------------------------------------------

	/**
	 * NOTE: callers should not modify the returned list or its contents!
	 * 
	 * @param trackIdx The index of the track
	 * @return The onTracks for a what on a track.
	 */
	public List<FastTIntArrayList> whats(final int trackIdx)
	{
		return this.onTrackIndices[trackIdx];
	}
	
	/**
	 * NOTE: callers should not modify the returned list or its contents!
	 * 
	 * @return The onTracks for each what on a track.
	 */
	public List<FastTIntArrayList>[] onTrackIndices()
	{
		return this.onTrackIndices;
	}

	/**
	 * NOTE: callers should not modify the returned list!
	 * 
	 * @param trackIdx 	The index of the track
	 * @param what      The component to look.
	 * @return The onTracks for a component on a track.
	 */
	public FastTIntArrayList whats(final int trackIdx, final int what)
	{
		return this.onTrackIndices[trackIdx].get(what);
	}

	/**
	 * @param trackIdx 	The index of the track
	 * @param what      The component to look.
	 * @param index     The index on the track.
	 * @return The number of pieces corresponding to the what at that index on the
	 *         track.
	 */
	public int whats(final int trackIdx, final int what, final int index)
	{
		return this.onTrackIndices[trackIdx].get(what).getQuick(index);
	}

	/**
	 * To add some pieces to a specific index of a track.
	 * 
	 * @param trackIdx 	The index of the track
	 * @param what      The index of the component.
	 * @param count     The number of pieces.
	 * @param index     The index on the track.
	 */
	public void add(final int trackIdx, final int what, final int count, final int index)
	{
		final int currentCount = this.onTrackIndices[trackIdx].get(what).getQuick(index);
		this.onTrackIndices[trackIdx].get(what).setQuick(index, currentCount + count);
	}

	/**
	 * To remove some pieces to a specific index of a track.
	 * 
	 * @param trackIdx 	The index of the track
	 * @param what      The index of the component.
	 * @param count     The number of pieces.
	 * @param index     The index on the track.
	 */
	public void remove(final int trackIdx, final int what, final int count, final int index)
	{
		final int currentCount = this.onTrackIndices[trackIdx].get(what).getQuick(index);
		this.onTrackIndices[trackIdx].get(what).setQuick(index, currentCount - count);
	}

	/**
	 * NOTE: callers should not modify the returned list!
	 * 
	 * @param trackIdx 	The index of the track
	 * @param what      The index of the component.
	 * @return The list of indices on the track with at least a component on that
	 *         type on them.
	 */
	public FastTIntArrayList indicesWithWhat(final int trackIdx, final int what)
	{
		final FastTIntArrayList indicesWithThatComponent = new FastTIntArrayList();
		final FastTIntArrayList indicesOnTrack = this.onTrackIndices[trackIdx].get(what);

		for (int i = 0; i < indicesOnTrack.size(); i++)
			if (indicesOnTrack.getQuick(i) != 0)
				indicesWithThatComponent.add(i);

		return indicesWithThatComponent;
	}

	//------------------------------------------------------------------------

	/**
	 * NOTE: callers should not modify the returned map or its contents!
	 * 
	 * @param trackIdx The track indices.
	 * 
	 * @return The map between the site and the indices of a specific track.
	 */
	public TIntObjectMap<FastTIntArrayList> locToIndex(final int trackIdx)
	{
		return locToIndex[trackIdx];
	}

	/**
	 * NOTE: callers should not modify the returned list!
	 * 
	 * @param trackIdx The track indices.
	 * @param site     The site index.
	 * 
	 * @return All the indices of a specific track corresponding to a specific site.
	 */
	public FastTIntArrayList locToIndex(final int trackIdx, final int site)
	{
		final FastTIntArrayList indices = locToIndex[trackIdx].get(site);
		if (indices == null)
			return new FastTIntArrayList();

		return indices;
	}

	/**
	 * NOTE: callers should not modify the returned list!
	 * 
	 * @param trackIdx The track indices.
	 * @param site     The site index.
	 * @param from     The last index reached.
	 * 
	 * @return All the indices of a specific track corresponding to a specific site.
	 */
	public FastTIntArrayList locToIndexFrom(final int trackIdx, final int site, final int from)
	{
		final FastTIntArrayList indices = locToIndex[trackIdx].get(site);
		if (indices == null)
			return new FastTIntArrayList();

		final FastTIntArrayList indicesToReturn = new FastTIntArrayList();
		for (int i = 0; i < indices.size(); i++)
			if (indices.get(i) > from)
				indicesToReturn.add(indices.get(i));

		return indicesToReturn;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof OnTrackIndices))
			return false;

		final OnTrackIndices other = (OnTrackIndices) obj;
		
		if(locToIndex.length != other.locToIndex.length)
			return false;
		
		if(onTrackIndices.length != other.onTrackIndices.length)
			return false;
		
		for(int i = 0; i < locToIndex.length; i++)
			if(!locToIndex[i].equals(other.locToIndex[i]))
				return false;
		
		for(int i = 0; i < onTrackIndices.length; i++)
			if(!onTrackIndices[i].equals(other.onTrackIndices[i]))
				return false;
		
		return true;
		
	}
	
	//------------------------------------------------------------------------

	@Override
	public String toString()
	{
		String str = "OnTrackIndices:\n";

		for (int i = 0; i < onTrackIndices.length; ++i)
		{
			str += "Track: " + i + "\n";
			final List<FastTIntArrayList> whatOnTracks = onTrackIndices[i];

			for (int what = 0; what < whatOnTracks.size(); what++)
			{
				final TIntArrayList onTracks = whatOnTracks.get(what);

				for (int j = 0; j < onTracks.size(); j++)
				{
					if (onTracks.get(j) > 0)
						str += "Component " + what + " at index " + i + " count = " + onTracks.get(j) + "\n";
				}
			}
			str += "\n";
		}

		return str;
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();
	}

}
