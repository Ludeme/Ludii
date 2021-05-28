package features.spatial;

import features.spatial.elements.FeatureElement;
import features.spatial.elements.RelativeFeatureElement;
import gnu.trove.list.array.TFloatArrayList;

/**
 * In a Relative Feature, the Action-to-play is implied by
 * relative "from" and "to" Walks (sometimes only a "to" Walk)
 * 
 * @author Dennis Soemers
 */
public class RelativeFeature extends SpatialFeature 
{
	
	//-------------------------------------------------------------------------
	
	/** Relative position that we want to move to */
	protected final Walk toPosition;
	
	/** 
	 * Relative position that we want to move from (null in cases where 
	 * there is no from position, or where it's not restricted) 
	 */
	protected final Walk fromPosition;
	
	/** 
	 * Relative position that was moved to last 
	 * (null for proactive features) 
	 */
	protected final Walk lastToPosition;
	
	/** 
	 * Relative position that was last moved from (null for proactive features 
	 * and in cases where we don't care about restricting from-positions) 
	 */
	protected final Walk lastFromPosition;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param pattern
	 * @param toPosition
	 * @param fromPosition
	 */
	public RelativeFeature
	(
		final Pattern pattern, 
		final Walk toPosition, 
		final Walk fromPosition
	)
	{
		this.pattern = pattern;
		this.toPosition = toPosition;
		this.fromPosition = fromPosition;
		this.lastToPosition = null;
		this.lastFromPosition = null;
	}
	
	/**
	 * Constructor
	 * @param pattern
	 * @param toPosition
	 * @param fromPosition
	 * @param lastToPosition
	 * @param lastFromPosition
	 */
	public RelativeFeature
	(
		final Pattern pattern, 
		final Walk toPosition, 
		final Walk fromPosition,
		final Walk lastToPosition,
		final Walk lastFromPosition
	)
	{
		this.pattern = pattern;
		this.toPosition = toPosition;
		this.fromPosition = fromPosition;
		this.lastToPosition = lastToPosition;
		this.lastFromPosition = lastFromPosition;
	}
	
	/**
	 * Copy constructor
	 * @param other
	 */
	public RelativeFeature(final RelativeFeature other)
	{
		this.pattern = new Pattern(other.pattern);
		this.toPosition = 
				other.toPosition == null ? null : 
					new Walk(other.toPosition);
		this.fromPosition = 
				other.fromPosition == null ? null : 
					new Walk(other.fromPosition);
		this.lastToPosition = 
				other.lastToPosition == null ? null : 
					new Walk(other.lastToPosition);
		this.lastFromPosition = 
				other.lastFromPosition == null ? null : 
					new Walk(other.lastFromPosition);
		
		this.comment = new String(other.comment);
	}
	
	/**
	 * Constructor from string
	 * @param string
	 */
	public RelativeFeature(final String string)
	{
		final String[] parts = string.split(":");
		
		// need these because we're not allowed to assign value to 
		// final members inside a loop
		Walk toPos = null;
		Walk fromPos = null;
		Walk lastToPos = null;
		Walk lastFromPos = null;
		
		for (String part : parts)
		{
			if (part.startsWith("last_to=<"))
			{
				part = part.substring(
						"last_to=<".length(), part.length() - ">".length());
				lastToPos = new Walk(part);
			}
			else if (part.startsWith("last_from=<"))
			{
				part = part.substring(
						"last_from=<".length(), part.length() - ">".length());
				lastFromPos = new Walk(part);
			}
			else if (part.startsWith("to=<"))
			{
				part = part.substring(
						"to=<".length(), part.length() - ">".length());
				toPos = new Walk(part);
			}
			else if (part.startsWith("from=<"))
			{
				part = part.substring(
						"from=<".length(), part.length() - ">".length());
				fromPos = new Walk(part);
			}
			else if (part.startsWith("pat=<"))
			{
				part = part.substring(
						"pat=<".length(), part.length() - ">".length());
				pattern = new Pattern(part);
			}
			else if (part.startsWith("comment=\""))
			{
				part = part.substring(
						"comment=\"".length(), part.length() - "\"".length());
				comment = part;
			}
		}
		
		toPosition = toPos;
		fromPosition = fromPos;
		lastToPosition = lastToPos;
		lastFromPosition = lastFromPos;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Relative to-position
	 */
	public Walk toPosition()
	{
		return toPosition;
	}
	
	/**
	 * @return Relative from-position
	 */
	public Walk fromPosition()
	{
		return fromPosition;
	}
	
	/**
	 * @return Relative last-to position
	 */
	public Walk lastToPosition()
	{
		return lastToPosition;
	}
	
	/**
	 * @return Relative last-from position
	 */
	public Walk lastFromPosition()
	{
		return lastFromPosition;
	}
	
	@Override
	public boolean isReactive()
	{
		return lastToPosition != null || lastFromPosition != null;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public SpatialFeature rotatedCopy(final float rotation)
	{
		final RelativeFeature copy = new RelativeFeature(this);
		
		if (copy.toPosition != null)
		{
			if (copy.toPosition().steps().size() > 0)
			{
				copy.toPosition().steps().setQuick(
						0, copy.toPosition().steps().getQuick(0) + rotation);
			}
		}
		
		if (copy.fromPosition != null)
		{
			if (copy.fromPosition().steps().size() > 0)
			{
				copy.fromPosition().steps().setQuick(
						0, copy.fromPosition().steps().getQuick(0) + rotation);
			}
		}
		
		if (copy.lastToPosition != null)
		{
			if (copy.lastToPosition().steps().size() > 0)
			{
				copy.lastToPosition().steps().setQuick(
						0, 
						copy.lastToPosition().steps().getQuick(0) + rotation);
			}
		}
		
		if (copy.lastFromPosition != null)
		{
			if (copy.lastFromPosition().steps().size() > 0)
			{
				copy.lastFromPosition().steps().setQuick(
						0, 
						copy.lastFromPosition().steps().getQuick(0) + rotation);
			}
		}
		
		for (final FeatureElement element : copy.pattern().featureElements())
		{
			if (element instanceof RelativeFeatureElement)
			{
				final RelativeFeatureElement rel = 
						(RelativeFeatureElement) element;
				
				if (rel.walk().steps().size() > 0)
				{
					rel.walk().steps().setQuick(
							0, 
							rel.walk().steps().getQuick(0) + rotation);
				}
			}
		}
		
		return copy;
	}
	
	@Override
	public SpatialFeature reflectedCopy()
	{
		final RelativeFeature copy = new RelativeFeature(this);
		
		if (copy.toPosition != null)
		{
			final TFloatArrayList steps = copy.toPosition.steps();
			
			for (int i = 0; i < steps.size(); ++i)
			{
				steps.setQuick(i, steps.getQuick(i) * -1);
			}
		}
		
		if (copy.fromPosition != null)
		{
			final TFloatArrayList steps = copy.fromPosition.steps();
			
			for (int i = 1; i < steps.size(); ++i)
			{
				steps.setQuick(i, steps.getQuick(i) * -1);
			}
		}
		
		if (copy.lastToPosition != null)
		{
			final TFloatArrayList steps = copy.lastToPosition.steps();
			
			for (int i = 1; i < steps.size(); ++i)
			{
				steps.setQuick(i, steps.getQuick(i) * -1);
			}
		}
		
		if (copy.lastFromPosition != null)
		{
			final TFloatArrayList steps = copy.lastFromPosition.steps();
			
			for (int i = 1; i < steps.size(); ++i)
			{
				steps.setQuick(i, steps.getQuick(i) * -1);
			}
		}
		
		for (final FeatureElement element : copy.pattern().featureElements())
		{
			if (element instanceof RelativeFeatureElement)
			{
				final RelativeFeatureElement rel = 
						(RelativeFeatureElement) element;
				
				final TFloatArrayList steps = rel.walk().steps();
				
				for (int i = 1; i < steps.size(); ++i)
				{
					steps.setQuick(i, steps.getQuick(i) * -1);
				}
			}
		}
		
		return copy;
	}
	
	@Override
	public boolean generalises(final SpatialFeature other)
	{
		if (!(other instanceof RelativeFeature))
		{
			return false;
		}
		
		final RelativeFeature otherFeature = (RelativeFeature) other;
		boolean foundStrictGeneralization = false;
		
		if (toPosition != null)
		{
			if (!(toPosition.equals(otherFeature.toPosition)))
			{
				return false;
			}
		}
		else if (otherFeature.toPosition == null)
		{
			return false;
		}
		
		if (fromPosition != null)
		{
			if (!(fromPosition.equals(otherFeature.fromPosition)))
			{
				return false;
			}
		}
		else if (otherFeature.fromPosition == null)
		{
			return false;
		}
		
		if (lastToPosition != null)
		{
			if (!(lastToPosition.equals(otherFeature.lastToPosition)))
			{
				return false;
			}
		}
		else if (otherFeature.lastToPosition == null)
		{
			return false;
		}
		
		if (lastFromPosition != null)
		{
			if (!(lastFromPosition.equals(otherFeature.lastFromPosition)))
			{
				return false;
			}
		}
		else if (otherFeature.lastFromPosition == null)
		{
			return false;
		}
		
		if (pattern.generalises(otherFeature.pattern))
		{
			foundStrictGeneralization = true;
		}
		else if(!(pattern.equals(otherFeature.pattern)))
		{
			return false;
		}
		
		return foundStrictGeneralization;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + 
				((fromPosition == null) ? 0 : fromPosition.hashCode());
		result = prime * result + 
				((toPosition == null) ? 0 : toPosition.hashCode());
		result = prime * result + 
				((lastFromPosition == null) ? 0 : lastFromPosition.hashCode());
		result = prime * result + 
				((lastToPosition == null) ? 0 : lastToPosition.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object other)
	{
		if (!super.equals(other))
			return false;
		
		if (!(other instanceof RelativeFeature))
			return false;
		
		final RelativeFeature otherFeature = (RelativeFeature) other;
		
		// the extra == checks are because positions may be null, which is 
		// fine if they are null for both features
		return 
				(
					toPosition == otherFeature.toPosition 
					|| 
					(
						toPosition != null && 
						toPosition.equals(otherFeature.toPosition)
					)
				) 
				&& 
				(
					fromPosition == otherFeature.fromPosition 
					|| 
					(
						fromPosition != null && 
						fromPosition.equals(otherFeature.fromPosition)
					)
				) 
				&& 
				(
					lastToPosition == otherFeature.lastToPosition 
					|| 
					(
						lastToPosition != null && 
						lastToPosition.equals(otherFeature.lastToPosition)
					)
				) 
				&& 
				(
					lastFromPosition == otherFeature.lastFromPosition 
					|| 
					(
						lastFromPosition != null && 
						lastFromPosition.equals(otherFeature.lastFromPosition)
					)
				);
	}
	
	@Override
	public boolean equalsIgnoreRotRef(final SpatialFeature other)
	{
		if (! super.equalsIgnoreRotRef(other))
			return false;
		
		if (!(other instanceof RelativeFeature))
			return false;
		
		final RelativeFeature otherFeature = (RelativeFeature) other;
		
		// the extra == checks are because positions may be null, which is 
		// fine if they are null for both features
		return 
				(
					toPosition == otherFeature.toPosition 
					|| 
					(
						toPosition != null && 
						toPosition.equals(otherFeature.toPosition)
					)
				) 
				&& 
				(
					fromPosition == otherFeature.fromPosition 
					|| 
					(
						fromPosition != null && 
						fromPosition.equals(otherFeature.fromPosition)
					)
				) 
				&& 
				(
					lastToPosition == otherFeature.lastToPosition 
					|| 
					(
						lastToPosition != null && 
						lastToPosition.equals(otherFeature.lastToPosition)
					)
				) 
				&& 
				(
					lastFromPosition == otherFeature.lastFromPosition 
					|| 
					(
						lastFromPosition != null && 
						lastFromPosition.equals(otherFeature.lastFromPosition)
					)
				);
	}

	@Override
	public int hashCodeIgnoreRotRef()
	{
		final int prime = 31;
		int result = super.hashCodeIgnoreRotRef();
		result = prime * result + 
				((fromPosition == null) ? 0 : fromPosition.hashCode());
		result = prime * result + 
				((toPosition == null) ? 0 : toPosition.hashCode());
		result = prime * result + 
				((lastFromPosition == null) ? 0 : lastFromPosition.hashCode());
		result = prime * result + 
				((lastToPosition == null) ? 0 : lastToPosition.hashCode());
		return result;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		String str = String.format("pat=<%s>", pattern);
		
		if (toPosition != null)
		{
			str = String.format("to=<%s>:%s", toPosition, str);
		}
		
		if (fromPosition != null)
		{
			str = String.format("from=<%s>:%s", fromPosition, str);
		}
		
		if (lastToPosition != null)
		{
			str = String.format("last_to=<%s>:%s", lastToPosition, str);
		}
		
		if (lastFromPosition != null)
		{
			str = String.format("last_from=<%s>:%s", lastFromPosition, str);
		}
		
		if (comment.length() > 0)
		{
			str = String.format("%s:comment=\"%s\"", str, comment);
		}

		return "rel:" + str;
	}
	
	//-------------------------------------------------------------------------

}
