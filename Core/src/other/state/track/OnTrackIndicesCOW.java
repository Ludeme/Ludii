package other.state.track;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import main.collections.FastTIntArrayList;

/**
 * A subclass of OnTrackIndices, with copy-on-write (COW) optimisations.
 *
 * @author Dennis Soemers
 */
public class OnTrackIndicesCOW extends OnTrackIndices
{
	
	//------------------------------------------------------------------------
	
	/** Did we make a deep copy for given onTrackIndices at given trackIdx? */
	private final boolean[] copiedOnTrackIndices;
	
	//------------------------------------------------------------------------
	
	/**
	 * Copy constructor (with copy-on-write behaviour)
	 * 
	 * @param other
	 */
	public OnTrackIndicesCOW(final OnTrackIndices other)
	{
		// We just copy the references
		super(Arrays.copyOf(other.onTrackIndices, other.onTrackIndices.length), other.locToIndex);
		
		// Remember that we didn't make any deep copies yet
		copiedOnTrackIndices = new boolean[onTrackIndices.length];
	}
	
	//------------------------------------------------------------------------
	
	@Override
	public void add(final int trackIdx, final int what, final int count, final int index)
	{
		ensureDeepCopy(trackIdx);
		super.add(trackIdx, what, count, index);
	}

	@Override
	public void remove(final int trackIdx, final int what, final int count, final int index)
	{
		ensureDeepCopy(trackIdx);
		super.remove(trackIdx, what, count, index);
	}
	
	//------------------------------------------------------------------------
	
	/**
	 * Ensures that we made a deep copy of the data for the given track index
	 * @param trackIdx
	 */
	public void ensureDeepCopy(final int trackIdx)
	{
		// We're about to make a modification to data for given track index, so should
		// make sure that we're operating on a deep copy
		if (!copiedOnTrackIndices[trackIdx])
		{
			// Didn't deep copy yet, so need to do so now
			final List<FastTIntArrayList> otherOnTracks = onTrackIndices[trackIdx];
			final List<FastTIntArrayList> onTracks = new ArrayList<FastTIntArrayList>(otherOnTracks.size());
			
			for (final FastTIntArrayList whatOtherOnTrack : otherOnTracks)
				onTracks.add(new FastTIntArrayList(whatOtherOnTrack));
			
			onTrackIndices[trackIdx] = onTracks;
			
			copiedOnTrackIndices[trackIdx] = true;
		}
	}
	
	//------------------------------------------------------------------------

}
