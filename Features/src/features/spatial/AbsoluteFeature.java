package features.spatial;

import java.util.List;
import java.util.Set;

import features.spatial.elements.FeatureElement;
import features.spatial.elements.RelativeFeatureElement;
import game.Game;
import gnu.trove.list.array.TFloatArrayList;

/**
 * In an Absolute Feature, the Action-to-play is implied by
 * absolute "from" and "to" positions (sometimes only a "to" position)
 * 
 * @author Dennis Soemers
 */
public class AbsoluteFeature extends SpatialFeature 
{
	
	//-------------------------------------------------------------------------
	
	/** Position we want to move to */
	protected final int toPosition;
	
	/** 
	 * Position we want to move from (-1 in cases where there is no from 
	 * position, or where it's not restricted) 
	 */
	protected final int fromPosition;
	
	/** Position that was moved to last (-1 for proactive features) */
	protected final int lastToPosition;
	
	/** 
	 * Position that was last moved from (-1 for proactive features and in 
	 * cases where we don't care about restricting from-positions) 
	 */
	protected final int lastFromPosition;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param pattern
	 * @param toPosition
	 * @param fromPosition
	 */
	public AbsoluteFeature
	(
		final Pattern pattern, 
		final int toPosition, 
		final int fromPosition
	)
	{
		this.pattern = pattern;
		this.toPosition = toPosition;
		this.fromPosition = fromPosition;
		this.lastToPosition = -1;
		this.lastFromPosition = -1;
	}
	
	/**
	 * Copy constructor
	 * @param other
	 */
	public AbsoluteFeature(final AbsoluteFeature other)
	{
		this.pattern = new Pattern(other.pattern);
		this.toPosition = other.toPosition;
		this.fromPosition = other.fromPosition;
		this.lastToPosition = other.lastToPosition;
		this.lastFromPosition = other.lastFromPosition;
		
//		this.comment = new String(other.comment);
	}
	
	/**
	 * Constructor from string
	 * @param string
	 */
	public AbsoluteFeature(final String string)
	{
		final String[] parts = string.split(":");
		
		// need these because we're not allowed to assign value to final 
		// members inside a loop
		int toPos = -1;
		int fromPos = -1;
		int lastToPos = -1;
		int lastFromPos = -1;
		
		for (String part : parts)
		{
			if (part.startsWith("last_to=<"))
			{
				part = part.substring(
						"last_to=<".length(), part.length() - ">".length());
				lastToPos = Integer.parseInt(part);
			}
			else if (part.startsWith("last_from=<"))
			{
				part = part.substring(
						"last_from=<".length(), part.length() - ">".length());
				lastFromPos = Integer.parseInt(part);
			}
			else if (part.startsWith("to=<"))
			{
				part = part.substring(
						"to=<".length(), part.length() - ">".length());
				toPos = Integer.parseInt(part);
			}
			else if (part.startsWith("from=<"))
			{
				part = part.substring(
						"from=<".length(), part.length() - ">".length());
				fromPos = Integer.parseInt(part);
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
//				comment = part;
			}
		}
		
		toPosition = toPos;
		fromPosition = fromPos;
		lastToPosition = lastToPos;
		lastFromPosition = lastFromPos;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Absolute to-position
	 */
	public int toPosition()
	{
		return toPosition;
	}
	
	/**
	 * @return Absolute from-position
	 */
	public int fromPosition()
	{
		return fromPosition;
	}
	
	/**
	 * @return Absolute last-to position
	 */
	public int lastToPosition()
	{
		return lastToPosition;
	}
	
	/**
	 * @return Absolute last-from position
	 */
	public int lastFromPosition()
	{
		return lastFromPosition;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public SpatialFeature rotatedCopy(final float rotation)
	{
		final AbsoluteFeature copy = new AbsoluteFeature(this);
		
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
		final AbsoluteFeature copy = new AbsoluteFeature(this);
		
		for (final FeatureElement element : copy.pattern().featureElements())
		{
			if (element instanceof RelativeFeatureElement)
			{
				final RelativeFeatureElement rel = 
						(RelativeFeatureElement) element;
				
				final TFloatArrayList steps = rel.walk().steps();
				
				for (int i = 0; i < steps.size(); ++i)
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
		if (!(other instanceof AbsoluteFeature))
		{
			return false;
		}
		
		final AbsoluteFeature otherFeature = (AbsoluteFeature) other;
		
		return (toPosition == otherFeature.toPosition && 
				fromPosition == otherFeature.fromPosition && 
				lastToPosition == otherFeature.lastToPosition &&
				lastFromPosition == otherFeature.lastFromPosition &&
				pattern.generalises(otherFeature.pattern));
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public List<SpatialFeature> generateGeneralisers(final Game game, final Set<RotRefInvariantFeature> generalisers, final int numRecursions)
	{
		System.err.println("ERROR: AbsoluteFeature::generateGeneralisers(Game) not yet implemented!");
		return null;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + fromPosition;
		result = prime * result + toPosition;
		result = prime * result + lastFromPosition;
		result = prime * result + lastToPosition;
		return result;
	}

	@Override
	public boolean equals(final Object other)
	{
		if (! super.equals(other))
			return false;
		
		if (!(other instanceof AbsoluteFeature))
			return false;
		
		final AbsoluteFeature otherFeature = (AbsoluteFeature) other;
		
		return toPosition == otherFeature.toPosition && 
				fromPosition == otherFeature.fromPosition && 
				lastToPosition == otherFeature.lastToPosition &&
				lastFromPosition == otherFeature.lastFromPosition;
	}
	
	@Override
	public boolean equalsIgnoreRotRef(final SpatialFeature other)
	{
		if (! super.equalsIgnoreRotRef(other))
			return false;
		
		if (!(other instanceof AbsoluteFeature))
			return false;
		
		final AbsoluteFeature otherFeature = (AbsoluteFeature) other;
		
		return toPosition == otherFeature.toPosition && 
				fromPosition == otherFeature.fromPosition && 
				lastToPosition == otherFeature.lastToPosition &&
				lastFromPosition == otherFeature.lastFromPosition;
	}

	@Override
	public int hashCodeIgnoreRotRef()
	{
		final int prime = 31;
		int result = super.hashCodeIgnoreRotRef();
		result = prime * result + fromPosition;
		result = prime * result + toPosition;
		result = prime * result + lastFromPosition;
		result = prime * result + lastToPosition;
		return result;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		String str = String.format("pat=<%s>", pattern);
		
		if (toPosition != -1)
		{
			str = String.format("to=<%s>:%s", Integer.valueOf(toPosition), str);
		}
		
		if (fromPosition != -1)
		{
			str = String.format("from=<%s>:%s", Integer.valueOf(fromPosition), str);
		}
		
		if (lastToPosition != -1)
		{
			str = String.format("last_to=<%s>:%s", Integer.valueOf(lastToPosition), str);
		}
		
		if (lastFromPosition != -1)
		{
			str = String.format("last_from=<%s>:%s", Integer.valueOf(lastFromPosition), str);
		}

//		if (comment.length() > 0)
//		{
//			str = String.format("%s:comment=\"%s\"", str, comment);
//		}

		return "abs:" + str;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String generateTikzCode(final Game game)
	{
		return "TO DO";
	}
	
	//-------------------------------------------------------------------------

}
