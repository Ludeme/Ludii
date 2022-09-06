package features.spatial;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import features.spatial.elements.FeatureElement;
import features.spatial.elements.RelativeFeatureElement;
import game.Game;
import gnu.trove.list.array.TFloatArrayList;
import main.Constants;
import main.math.Point2D;

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
		else if (!(pattern.equals(otherFeature.pattern)))
		{
			return false;
		}
		
		return foundStrictGeneralization;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public List<SpatialFeature> generateGeneralisers
	(
		final Game game, 
		final Set<RotRefInvariantFeature> generalisers, 
		final int numRecursions
	)
	{
		if (toPosition != null && fromPosition != null)
		{
			// We can generalise by removing either the to- or the from-specifier
			addGeneraliser
			(
				new RelativeFeature
				(
					new Pattern(pattern),
					new Walk(toPosition),
					null,
					lastToPosition == null ? null : new Walk(lastToPosition),
					lastFromPosition == null ? null : new Walk(lastFromPosition)
				),
				game,
				generalisers,
				numRecursions
			);
			
			addGeneraliser
			(
				new RelativeFeature
				(
					new Pattern(pattern),
					null,
					new Walk(fromPosition),
					lastToPosition == null ? null : new Walk(lastToPosition),
					lastFromPosition == null ? null : new Walk(lastFromPosition)
				),
				game,
				generalisers,
				numRecursions
			);
		}
		
		if (lastToPosition != null)
		{
			// We can generalise by removing last-to requirement
			addGeneraliser
			(
				new RelativeFeature
				(
					new Pattern(pattern),
					toPosition == null ? null : new Walk(toPosition),
					fromPosition == null ? null : new Walk(fromPosition),
					null,
					lastFromPosition == null ? null : new Walk(lastFromPosition)
				),
				game,
				generalisers,
				numRecursions
			);
		}
		
		if (lastFromPosition != null)
		{
			// We can generalise by removing last-to requirement
			addGeneraliser
			(
				new RelativeFeature
				(
					new Pattern(pattern),
					toPosition == null ? null : new Walk(toPosition),
					fromPosition == null ? null : new Walk(fromPosition),
					lastToPosition == null ? null : new Walk(lastToPosition),
					null
				),
				game,
				generalisers,
				numRecursions
			);
		}
		
		final FeatureElement[] patternElements = pattern.featureElements();
		for (int i = 0; i < patternElements.length; ++i)
		{
			// We can generalise by removing the ith element of the pattern
			final FeatureElement[] newElements = new FeatureElement[patternElements.length - 1];
			int nextIdx = 0;
			for (int j = 0; j < patternElements.length; ++j)
			{
				if (j != i)
					newElements[nextIdx++] = FeatureElement.copy(patternElements[j]);
			}
			
			final RelativeFeature newFeature = 
					new RelativeFeature
					(
						new Pattern(newElements),
						toPosition == null ? null : new Walk(toPosition),
						fromPosition == null ? null : new Walk(fromPosition),
						lastToPosition == null ? null : new Walk(lastToPosition),
						lastFromPosition == null ? null : new Walk(lastFromPosition)
					);
			newFeature.pattern().setAllowedRotations(pattern.allowedRotations());
			
			addGeneraliser(newFeature, game, generalisers, numRecursions);
		}

		final List<SpatialFeature> outList = new ArrayList<SpatialFeature>(generalisers.size());
		for (final RotRefInvariantFeature f : generalisers)
		{
			outList.add(f.feature());
		}
		
		return outList;
	}
	
	/**
	 * Helper method to add generaliser for given game to given set of generalisers.
	 * May not add it if it's equivalent to another feature already in the list.
	 * 
	 * @param generaliser
	 * @param game
	 * @param generalisers
	 * @param numRecursions
	 */
	private static void addGeneraliser
	(
		final RelativeFeature generaliser, 
		final Game game, 
		final Set<RotRefInvariantFeature> generalisers, 
		final int numRecursions
	)
	{
		generaliser.normalise(game);
		if (generalisers.add(new RotRefInvariantFeature(generaliser)))
		{
			if (numRecursions > 0)
			{
				// Also add generalisers of the generaliser
				generaliser.generateGeneralisers(game, generalisers, numRecursions - 1);
			}
				
		}
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
		
		final List<Point2D> points = new ArrayList<Point2D>();
		final List<String> labels = new ArrayList<String>();
		final List<List<String>> stringsPerPoint = new ArrayList<List<String>>();
		final Map<List<String>, String> connections = new HashMap<List<String>, String>();
		
		// Start with node for anchor
		sb.append("\\node[ellipse, draw, align=center] (Anchor) at (0,0) {");
		final List<String> anchorStrings = stringsPerWalk.get(new TFloatArrayList());
		while (anchorStrings.remove("")) { /** Keep going */ }
		
//		for (int i = 0; i < anchorStrings.size(); ++i)
//		{
//			if (i > 0)
//				sb.append("\\\\");
//			sb.append(anchorStrings.get(i));
//		}
		sb.append("{POINT_STRINGS_" + points.size() + "}");
		
		sb.append("}; \n");
		
		points.add(new Point2D(0.0, 0.0));
		labels.add("(Anchor)");
		stringsPerPoint.add(anchorStrings);
		
		final double STEP_SIZE = 2.0;
				
		int nextLabelIdx = 1;
		
		for (final Entry<TFloatArrayList, List<String>> entry : stringsPerWalk.entrySet())
		{
			final TFloatArrayList walk = entry.getKey();
			
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
				final Point2D currPoint = new Point2D(x, y);
				
				String nextLabel = null;
				List<String> pointStrings = null;
				
				for (int j = 0; j < points.size(); ++j)
				{
					if (points.get(j).equalsApprox(currPoint, Constants.EPSILON))
					{
						nextLabel = labels.get(j);
						pointStrings = stringsPerPoint.get(j);
						break;
					}
				}
				
				if (nextLabel == null)
				{
					nextLabel = "(N" + (nextLabelIdx++) + ")";
					
					// Need to draw a node for this partial walk
//					final StringBuilder nodeText = new StringBuilder();
//					final List<String> walkStrings = stringsPerWalk.get(partialWalk);
//					
//					if (walkStrings != null)
//					{
//						while (walkStrings.remove("")) { /** Keep going */ }
//						for (int j = 0; j < walkStrings.size(); ++j)
//						{
//							if (j > 0)
//								nodeText.append("\\\\");
//							nodeText.append(walkStrings.get(j));
//						}
//					}
					
					sb.append("\\node[ellipse, draw, align=center] " + nextLabel + " at (" + x + ", " + y + ") {{POINT_STRINGS_" + points.size() + "}}; \n");
					
					points.add(currPoint);
					labels.add(nextLabel);
					pointStrings = new ArrayList<String>();
					stringsPerPoint.add(pointStrings);
				}
				
				final List<String> walkStrings = stringsPerWalk.get(partialWalk);
				if (walkStrings != null)
				{
					while (walkStrings.remove("")) { /** Keep going */ }
					for (final String walkString : walkStrings)
					{
						if (!pointStrings.contains(walkString))
							pointStrings.add(walkString);
					}
				}
				
				connections.put(Arrays.asList(new String[] {currLabel, nextLabel}), "$" + new DecimalFormat("#.##").format(step) + "$");
				currLabel = nextLabel;
			}
		}
		
		for (final Entry<List<String>, String> connection : connections.entrySet())
		{
			sb.append("\\path[->,draw] " + connection.getKey().get(0) + " edge node {" + connection.getValue() + "} " + connection.getKey().get(1) + "; \n");
		}
		
		String returnStr = sb.toString();
		
		for (int i = 0; i < points.size(); ++i)
		{
			final StringBuilder replaceStr = new StringBuilder();
			for (int j = 0; j < stringsPerPoint.get(i).size(); ++j)
			{
				if (j > 0)
					replaceStr.append("\\\\");
				replaceStr.append(stringsPerPoint.get(i).get(j));
			}
			
			returnStr = 
					returnStr.replaceFirst
					(
						java.util.regex.Pattern.quote("{POINT_STRINGS_" + i + "}"), 
						java.util.regex.Matcher.quoteReplacement(replaceStr.toString())
					);
		}
		
		returnStr = returnStr.replaceAll
				(
					java.util.regex.Pattern.quote("#"), 
					java.util.regex.Matcher.quoteReplacement("\\#")
				);
		
		return returnStr;
	}
	
	//-------------------------------------------------------------------------

}
