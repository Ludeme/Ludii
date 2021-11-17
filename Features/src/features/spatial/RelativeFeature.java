package features.spatial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import features.spatial.elements.FeatureElement;
import features.spatial.elements.RelativeFeatureElement;
import game.Game;
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
		
//		this.comment = new String(other.comment);
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
		
//		if (comment.length() > 0)
//		{
//			str = String.format("%s:comment=\"%s\"", str, comment);
//		}

		return "rel:" + str;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String generateTikzCode(final Game game)
	{
		final Map<TFloatArrayList, List<String>> stringsPerWalk = new HashMap<TFloatArrayList, List<String>>();
		
		// Anchor
		stringsPerWalk.put(new TFloatArrayList(), new ArrayList<String>());
		stringsPerWalk.get(new TFloatArrayList()).add("");
		
		if (toPosition != null)
		{
			final TFloatArrayList key = toPosition.steps();
			List<String> strings = stringsPerWalk.get(key);
			
			if (strings == null)
			{
				strings = new ArrayList<String>();
				stringsPerWalk.put(key, strings);
			}
			
			strings.add("To");
		}
		
		if (fromPosition != null)
		{
			final TFloatArrayList key = fromPosition.steps();
			List<String> strings = stringsPerWalk.get(key);
			
			if (strings == null)
			{
				strings = new ArrayList<String>();
				stringsPerWalk.put(key, strings);
			}
			
			strings.add("From");
		}
		
		if (lastToPosition != null)
		{
			final TFloatArrayList key = lastToPosition.steps();
			List<String> strings = stringsPerWalk.get(key);
			
			if (strings == null)
			{
				strings = new ArrayList<String>();
				stringsPerWalk.put(key, strings);
			}
			
			strings.add("Last To");
		}
		
		if (lastFromPosition != null)
		{
			final TFloatArrayList key = lastFromPosition.steps();
			List<String> strings = stringsPerWalk.get(key);
			
			if (strings == null)
			{
				strings = new ArrayList<String>();
				stringsPerWalk.put(key, strings);
			}
			
			strings.add("Last From");
		}
		
		for (final FeatureElement el : pattern.featureElements())
		{
			final TFloatArrayList key = ((RelativeFeatureElement)el).walk().steps();
			List<String> strings = stringsPerWalk.get(key);
			
			if (strings == null)
			{
				strings = new ArrayList<String>();
				stringsPerWalk.put(key, strings);
			}
			
			strings.add((el.not() ? "!" : "") + el.type().label + (el.itemIndex() >= 0 ? String.valueOf(el.itemIndex()) : ""));
		}
		
		final StringBuilder sb = new StringBuilder();
		
		final Map<TFloatArrayList, String> walksToLabels = new HashMap<TFloatArrayList, String>();
		
		// Start with node for anchor
		sb.append("\\node[ellipse, draw, align=center] (Anchor) at (0,0) {");
		final List<String> anchorStrings = stringsPerWalk.get(new TFloatArrayList());
		while (anchorStrings.remove("")) { /** Keep going */ }
		for (int i = 0; i < anchorStrings.size(); ++i)
		{
			if (i > 0)
				sb.append("\\\\");
			sb.append(anchorStrings.get(i));
		}
		sb.append("}; \n");
		
		walksToLabels.put(new TFloatArrayList(), "(Anchor)");
		
		final double STEP_SIZE = 2.0;
		
		// TODO should use (x, y) coordinates as keys instead of lists of steps
		
		for (final Entry<TFloatArrayList, List<String>> entry : stringsPerWalk.entrySet())
		{
			final TFloatArrayList walk = entry.getKey();
			
			if (!walksToLabels.containsKey(walk))
			{
				String currLabel = "(Anchor)";
				double x = 0.0;
				double y = 0.0;
				double currTheta = 0.5 * Math.PI;
				
				final TFloatArrayList partialWalk = new TFloatArrayList();
				for (int i = 0; i < walk.size(); ++i)
				{
					final float step = walk.getQuick(i);
					partialWalk.add(step);
					
					currTheta -= step * 2.0 * Math.PI;
					x += STEP_SIZE * Math.cos(currTheta);
					y += STEP_SIZE * Math.sin(currTheta);
					
					final String newLabel = "(N" + partialWalk.toString().replaceAll("[{} ]", "").replaceAll("[,]", "_") + ")";
					
					if (!walksToLabels.containsKey(partialWalk))
					{
						walksToLabels.put(partialWalk, newLabel);
						
						// Need to draw a node for this partial walk
						final StringBuilder nodeText = new StringBuilder();
						final List<String> walkStrings = stringsPerWalk.get(partialWalk);
						
						if (walkStrings != null)
						{
							while (walkStrings.remove("")) { /** Keep going */ }
							for (int j = 0; j < walkStrings.size(); ++j)
							{
								if (j > 0)
									nodeText.append("\\\\");
								nodeText.append(walkStrings.get(j));
							}
						}
						
						sb.append("\\node[ellipse, draw, align=center] " + newLabel + " at (" + x + ", " + y + ") {" + nodeText + "}; \n");
						
						// Draw arrow between previous node and this node
						sb.append("\\path[->,draw] " + currLabel + " edge " + newLabel + "; \n");
					}
					
					currLabel = newLabel;
				}
			}
		}
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------

}
