package features.spatial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import features.spatial.elements.AbsoluteFeatureElement;
import features.spatial.elements.FeatureElement;
import features.spatial.elements.RelativeFeatureElement;
import features.spatial.graph_search.Path;
import gnu.trove.list.array.TFloatArrayList;
import main.collections.ArrayUtils;

/**
 * A local, lightweight Pattern, for Features<br>
 * 
 * @author Dennis Soemers and cambolbro
 */
public class Pattern
{	
	//-------------------------------------------------------------------------
	
	/** Array of elements (positions + Element Types) that define this pattern. */
	protected FeatureElement[] featureElements;
	
	/** 
	 * List of complete Pattern rotations that are allowed
	 */
	protected TFloatArrayList allowedRotations = null;
	
	/** Whether we allow reflection (true by default) */
	protected boolean allowsReflection = true;
	
	/** If set to true, the pattern will automatically be rotated to match the mover's direction */
	protected boolean matchMoverDirection = false;
	
	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public Pattern()
	{
		featureElements = new FeatureElement[0];
	}
	
	/**
	 * Constructor
	 * @param elements
	 */
	public Pattern(final FeatureElement... elements)
	{
		featureElements = Arrays.copyOf(elements, elements.length);
	}

	/**
	 * Copy constructor
	 * @param other
	 */
	public Pattern(final Pattern other)
	{
		featureElements = new FeatureElement[other.featureElements.length];
		for (int i = 0; i < featureElements.length; ++i)
		{
			featureElements[i] = FeatureElement.copy(other.featureElements[i]);
		}

		allowedRotations = other.allowedRotations;
	}
	/**
	 * Constructs pattern from string
	 * @param string
	 */
	public Pattern(final String string)
	{
		int currIdx = 0;	// move through the entire string
		
		// Default enable reflection and rotations if not specified
		allowsReflection = true;
		allowedRotations = null;
		
		while (currIdx < string.length())
		{
			if (string.startsWith("refl=true,", currIdx))
			{
				allowsReflection = true;
				currIdx += "refl=true,".length();	// skip ahead
			}
			else if (string.startsWith("refl=false,", currIdx))
			{
				allowsReflection = false;
				currIdx += "refl=false,".length();	// skip ahead
			}
			else if (string.startsWith("rots=", currIdx))
			{
				if (string.startsWith("rots=all,", currIdx))
				{
					allowedRotations = null;
					currIdx += "rots=all,".length();	// skip ahead
				}
				else	// legal rotations are written as an array, got some work to do
				{
					final int rotsListEnd = string.indexOf("]", currIdx);
					
					// the following substring includes "rots=[" at the beginning, and "]," at the end
					String rotsListSubstring = string.substring(currIdx, rotsListEnd + 2);
					
					// already move currIdx ahead based on this substring
					currIdx += rotsListSubstring.length();
					
					// get rid of the unnecessary parts in beginning and end
					rotsListSubstring = rotsListSubstring.substring("rots=[".length(), rotsListSubstring.length() - "],".length());
					
					// now we can split on ","
					final String[] rotElements = rotsListSubstring.split(",");
					allowedRotations = new TFloatArrayList(rotElements.length);
					
					for (final String rotElement : rotElements)
					{
						allowedRotations.add(Float.parseFloat(rotElement));
					}
				}
			}
			else if (string.startsWith("els=", currIdx))
			{
				final int elsListEnd = string.indexOf("]", currIdx);
				
				// the following substring includes "els=[" at the beginning, and "]" at the end
				String elsListSubstring = string.substring(currIdx, elsListEnd + 1);
				
				// already move currIdx ahead based on this substring
				currIdx += elsListSubstring.length();
				
				// get rid of the unnecessary parts in beginning and end
				elsListSubstring = elsListSubstring.substring("els=[".length(), elsListSubstring.length() - "]".length());
				
				// we don't split on commas, because commas are used both for separating elements AND for
				// separating steps in a Walk inside the elements
				// 
				// our first element starts immediately at index 0, and ends when we see a closing curly brace: }
				// then, we expect a comma and a second element to start after that, etc.
				final List<String> elements = new ArrayList<String>();
				
				int elsIdx = 0;
				String elementString = "";
				
				while (elsIdx < elsListSubstring.length())
				{
					final char nextChar = elsListSubstring.charAt(elsIdx);
					elementString += Character.toString(nextChar);
					
					if (nextChar == '}')	// finished this element
					{
						elements.add(elementString.trim());
						elementString = "";		// start creating a new element string
						elsIdx += 2;	// increment by 2 because we expect to see an extra comma which can be skipped
					}
					else
					{
						elsIdx += 1;
					}
				}
				
				final List<FeatureElement> list = new ArrayList<FeatureElement>(elements.size());
				
				for (final String element : elements)
				{
					list.add(FeatureElement.fromString(element));
				}
				
				featureElements = list.toArray(new FeatureElement[list.size()]);
			}
			else
			{
				System.err.println("Error in Pattern(String) constructor: don't know how to handle: " + string.substring(currIdx));
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param patterns
	 * @return A new list of patterns where duplicates from the given list are removed.
	 * 	Whenever there are multiple patterns in the list such that one is strictly a generalization
	 * 	of the other, the most general pattern will be kept and the more specific one removed.
	 */
	public static List<Pattern> deduplicate(List<Pattern> patterns)
	{
		final List<Pattern> newPatterns = new ArrayList<Pattern>(patterns.size());
		
		for (final Pattern pattern : patterns)
		{
			boolean shouldAdd = true;
			
			for (int i = 0; i < newPatterns.size(); /**/)
			{
				final Pattern otherPattern = newPatterns.get(i);
				
				if (pattern.equals(otherPattern))
				{
					shouldAdd = false;
					break;
				}
				else if (pattern.generalises(otherPattern))
				{
					newPatterns.remove(i);		// pattern is more general than otherPattern, so remove otherPattern again
				}
				else
				{
					++i;
				}
			}
			
			if (shouldAdd)
			{
				newPatterns.add(pattern);
			}
		}
		
		return newPatterns;
	}
	
	/**
	 * @param p1
	 * @param p2
	 * @return The given two patterns merged into a single one
	 */
	public static Pattern merge(final Pattern p1, final Pattern p2)
	{
		final FeatureElement[] mergedElements = new FeatureElement[p1.featureElements.length + p2.featureElements.length];

		for (int i = 0; i < p1.featureElements.length; ++i)
		{
			mergedElements[i] = FeatureElement.copy(p1.featureElements[i]);
		}
		
		for (int i = 0; i < p2.featureElements.length; ++i)
		{
			mergedElements[i + p1.featureElements.length] = FeatureElement.copy(p2.featureElements[i]); 
		}
		
		final Pattern merged = new Pattern(mergedElements);
		
		return merged.allowRotations(p1.allowedRotations).allowRotations(p2.allowedRotations);
	}
	
	//------------------------------------------------------------------------
	
	/**
	 * Adds given new element to this pattern
	 * @param newElement
	 */
	public void addElement(final FeatureElement newElement)
	{
		final FeatureElement[] temp = Arrays.copyOf(featureElements, featureElements.length + 1);
		temp[temp.length - 1] = newElement;
		featureElements = temp;
	}
	
	/**
	 * Sets array of feature elements
	 * @param elements
	 */
	public void setFeatureElements(final FeatureElement... elements)
	{
		this.featureElements = elements;
	}
	
	/**
	 * @return Array of all feature elements in this pattern
	 */
	public FeatureElement[] featureElements()
	{
		return featureElements;
	}
	
	/**
	 * Moves all relative elements currently in the pattern one additional step away from
	 * the reference point of the pattern. This additional step is prepended, so
	 * the additional step becomes the first step of every Walk
	 * 
	 * @param direction
	 */
	public void prependStep(final int direction)
	{
		for (final FeatureElement element : featureElements)
		{
			if (element instanceof RelativeFeatureElement)
			{
				((RelativeFeatureElement) element).walk().prependStep(direction);
			}
			else
			{
				System.err.println("Warning: trying to prepend a step to an Absolute Feature Element!");
			}
		}
	}
	
	/**
	 * Prepends all steps of the given walk to all relative elements in this pattern
	 * @param walk
	 */
	public void prependWalk(final Walk walk)
	{
		for (final FeatureElement featureElement : featureElements)
		{
			if (featureElement instanceof RelativeFeatureElement)
			{
				final RelativeFeatureElement relativeFeatureEl = (RelativeFeatureElement) featureElement;
				relativeFeatureEl.walk().prependWalk(walk);
			}
		}
	}
	
	/**
	 * Prepends all steps of the given walk to all relative elements in this pattern.
	 * Additionally applies a correction to the first steps of any existing walks,
	 * to make sure that they still continue moving in the same direction they
	 * would have without prepending the new walk.
	 * 
	 * @param walk
	 * @param path The path we followed when walking the prepended walk
	 * @param rotToRevert Already-applied rotation which should be reverted
	 * @param refToRevert Already-applied reflection which should be reverted
	 */
	public void prependWalkWithCorrection
	(
		final Walk walk, 
		final Path path, 
		final float rotToRevert,
		final int refToRevert
	)
	{
		for (final FeatureElement featureElement : featureElements)
		{
			if (featureElement instanceof RelativeFeatureElement)
			{
				final RelativeFeatureElement relativeFeatureEl = (RelativeFeatureElement) featureElement;
				relativeFeatureEl.walk().prependWalkWithCorrection(walk, path, rotToRevert, refToRevert);
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Set list of allowed rotations. If the list of allowed rotations was already previously specified,
	 * we will use the intersection of the lists of allowed rotations
	 * 
	 * @param allowed
	 * @return this AtomicPattern object
	 */
	public Pattern allowRotations(final TFloatArrayList allowed)
	{
		if (this.allowedRotations == null)
		{
			this.allowedRotations = allowed;
		}
		else
		{
			this.allowedRotations.retainAll(allowed);
		}
		
		return this;
	}
	
	/**
	 * @param flag Whether or not the Pattern should allow reflection.
	 * @return this Pattern object
	 */
	public Pattern allowReflection(final boolean flag)
	{
		allowsReflection = flag;
		return this;
	}
	
	/**
	 * Makes the Pattern auto-rotate to match the mover's direction
	 * @return this Pattern object
	 */
	public Pattern matchMoverDirection()
	{
		matchMoverDirection = true;
		return this;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return List of allowed rotations for this pattern
	 */
	public TFloatArrayList allowedRotations()
	{
		return allowedRotations;
	}
	
	/**
	 * @return Whether we allow reflection of this pattern
	 */
	public boolean allowsReflection()
	{
		return allowsReflection;
	}
	
	/**
	 * @return Whether the pattern should be rotated to match mover's direction
	 */
	public boolean matchesMoverDirection()
	{
		return matchMoverDirection;
	}
	
	/**
	 * Sets the list of allowed rotations to the given new list
	 * @param allowedRotations
	 */
	public void setAllowedRotations(final TFloatArrayList allowedRotations)
	{
		this.allowedRotations = allowedRotations;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Applies the given reflection to the complete Pattern. Note that this
	 * modifies the pattern itself!
	 * @param reflection
	 */
	public void applyReflection(final int reflection)
	{
		if (reflection == 1)
		{
			// guess we're doing nothing at all
			return;
		}
		
		for (final FeatureElement element : featureElements)
		{
			if (element instanceof RelativeFeatureElement)
			{
				final RelativeFeatureElement rel = (RelativeFeatureElement) element;
				final TFloatArrayList steps = rel.walk().steps();
				for (int i = 0; i < steps.size(); ++i)
				{
					steps.setQuick(i, steps.getQuick(i) * reflection);
				}
			}
		}
	}
	
	/**
	 * Applies the given rotation to the complete Pattern. Note that this
	 * modifies the pattern itself!
	 * @param rotation
	 */
	public void applyRotation(final float rotation)
	{
		for (final FeatureElement element : featureElements)
		{
			if (element instanceof RelativeFeatureElement)
			{
				final RelativeFeatureElement rel = (RelativeFeatureElement) element;
				final TFloatArrayList steps = rel.walk().steps();
				if (steps.size() > 0)
				{
					steps.setQuick(0, steps.getQuick(0) + rotation);
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * We are consistent if, for any pair of feature elements with the same position 
	 * (either equal relative Walk or equal absolute position), the elements are either
	 * equal or one of them generalizes the other.
	 * 
	 * @return Whether we are consistent
	 */
	public boolean isConsistent()
	{
		final List<AbsoluteFeatureElement> checkedAbsolutes = new ArrayList<AbsoluteFeatureElement>();
		final List<RelativeFeatureElement> checkedRelatives = new ArrayList<RelativeFeatureElement>();
		
		for (final FeatureElement element : featureElements)
		{
			if (element instanceof AbsoluteFeatureElement)
			{
				final AbsoluteFeatureElement abs = (AbsoluteFeatureElement) element;
				
				for (final AbsoluteFeatureElement other : checkedAbsolutes)
				{
					if (abs.position() == other.position())	// same positions, so need compatible elements
					{
						if (!(abs.equals(other) || abs.isCompatibleWith(other) || abs.generalises(other) || other.generalises(abs)))
						{
							return false;
						}
					}
				}
				
				checkedAbsolutes.add(abs);
			}
			else
			{
				final RelativeFeatureElement rel = (RelativeFeatureElement) element;
				
				for (final RelativeFeatureElement other : checkedRelatives)
				{
					if (rel.walk().equals(other.walk()))	// equal Walks, so need compatible elements
					{
						if (!(rel.equals(other) || rel.isCompatibleWith(other) || rel.generalises(other) || other.generalises(rel)))
						{
							return false;
						}
					}
				}
				
				checkedRelatives.add(rel);
			}
		}
		
		return true;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * We generalise the other pattern if and only if the following conditions hold:	<br> &emsp;
	 * 	- For every element that we have, there is one in the other pattern that is at least as restrictive		<br> &emsp;
	 * 	- For at least one feature element in the other Pattern, we are strictly less restrictive (otherwise we just equal)		<br> &emsp;
	 * <br>
	 * 	Our list of allowed rotations can also be viewed as "feature elements" in the list of conditions above
	 * 
	 * @param other
	 * @return Whether could generalise.
	 */
	public boolean generalises(final Pattern other)
	{
		boolean foundStrictGeneralisation = false;

		for (final FeatureElement featureElement : featureElements())
		{
			boolean foundGeneralisation = false;

			for (final FeatureElement otherElement : other.featureElements())
			{
				if (featureElement.generalises(otherElement))	// found an element that we strictly generalise
				{
					foundStrictGeneralisation = true;
					foundGeneralisation = true;
					break;
				}
				else if (featureElement.equals(otherElement))	// found an element that we equal (non-strict generalisation)
				{
					foundGeneralisation = true;
					break;
				}
			}

			if (!foundGeneralisation)
			{
				return false;
			}
		}
		
		if (other.allowedRotations == null)		// other allows ALL rotations
		{
			if (allowedRotations != null)	// we don't, so we're more restrictive
			{
				return false;
			}
		}
		else if (allowedRotations == null)	// we allow ALL rotations, and other doesn't, so we have a strict generalisation here
		{
			foundStrictGeneralisation = true;
		}
		else
		{
			for (int i = 0; i < other.allowedRotations().size(); ++i)
			{
				final float allowedRotation = other.allowedRotations().getQuick(i);
				
				if (!allowedRotations.contains(allowedRotation))	// other allows a rotation that we don't
				{
					return false;
				}
			}
			
			// we have strict generalisation if we allow more rotations than other
			foundStrictGeneralisation = (allowedRotations.size() > other.allowedRotations().size());
		}

		return foundStrictGeneralisation;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Whenever we have multiple elements specifying the same place (either relatively using Walks,
	 * or using absolute positions):
	 * 	- If they are equal, we keep just one of them,
	 * 	- If one generalises the other, we keep the most specific one
	 * 
	 * If neither of the two conditions above applies, we probably have an inconsistency,
	 * which we won't resolve here.
	 */
	public void removeRedundancies()
	{
		final List<FeatureElement> newFeatureElements = new ArrayList<FeatureElement>(featureElements.length);
		
		for (final FeatureElement element : featureElements)
		{
			boolean shouldAdd = true;
			
			if (element instanceof AbsoluteFeatureElement)
			{
				final AbsoluteFeatureElement abs = (AbsoluteFeatureElement) element;
				
				for (int i = 0; i < newFeatureElements.size(); ++i)
				{
					final FeatureElement alreadyAdded = newFeatureElements.get(i);
					
					if (alreadyAdded instanceof AbsoluteFeatureElement)
					{
						final AbsoluteFeatureElement other = (AbsoluteFeatureElement) alreadyAdded;
						
						if (abs.position() == other.position())	// same positions
						{
							if (abs.equals(other))	// already have this one, no need to add
							{
								shouldAdd = false;
								break;
							}
							else if (abs.generalises(other))	// have something more specific, so no need to add
							{
								shouldAdd = false;
								break;
							}
							else if (other.generalises(abs))	// have something more general, so we should replace that
							{
								newFeatureElements.set(i, abs);
								
								// just already added by replacement, so no need to add again
								shouldAdd = false;
								break;
							}
						}
					}
				}
			}
			else
			{
				final RelativeFeatureElement rel = (RelativeFeatureElement) element;
				
				for (int i = 0; i < newFeatureElements.size(); ++i)
				{
					final FeatureElement alreadyAdded = newFeatureElements.get(i);
					
					if (alreadyAdded instanceof RelativeFeatureElement)
					{
						final RelativeFeatureElement other = (RelativeFeatureElement) alreadyAdded;
						
						if (rel.walk().equals(other.walk()))	// equal walks
						{
							if (rel.equals(other))	// already have this one, no need to add
							{
								shouldAdd = false;
								break;
							}
							else if (rel.generalises(other))	// have something more specific, so no need to add
							{
								shouldAdd = false;
								break;
							}
							else if (other.generalises(rel))	// have something more general, so we should replace that
							{
								newFeatureElements.set(i, rel);
								
								// just already added by replacement, so no need to add again
								shouldAdd = false;
								break;
							}
						}
					}
				}
			}
			
			if (shouldAdd)
			{
				newFeatureElements.add(element);
			}
		}
		
		featureElements = newFeatureElements.toArray(new FeatureElement[newFeatureElements.size()]);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		
		// NOTE: we have custom equals() implementation which makes the order 
		// of allowed rotations and featureElements lists not matter. Our 
		// hashCode() implementation should therefore also be invariant to 
		// order in those lists
		
		if (allowedRotations == null)
		{
			result = prime * result;
		}
		else
		{
			int allowedRotsHash = 0;
			for (int i = 0; i < allowedRotations.size(); ++i)
			{
				// XORing them all means order does not matter
				allowedRotsHash ^= 41 * Float.floatToIntBits(allowedRotations.getQuick(i));
			}
			
			// adding prime inside brackets because that would also happen 
			// in ArrayList.hashCode()
			result = prime * result + (prime + allowedRotsHash);
		}
		
		result = prime * result + (allowsReflection ? 1231 : 1237);
		
		if (featureElements == null)
		{
			result = prime * result;
		}
		else
		{
			int featureElementsHash = 0;
			for (final FeatureElement element : featureElements)
			{
				// XORing them all means order does not matter
				featureElementsHash ^= 37 * element.hashCode();
			}
			
			// adding prime inside brackets because that would also happen in 
			// ArrayList.hashCode()
			result = prime * result + (prime + featureElementsHash);
		}
		
		result = prime * result + (matchMoverDirection ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(final Object other)
	{
		if (!(other instanceof Pattern))
			return false;
		
		final Pattern otherPattern = (Pattern) other;
		
		// For every feature element, we also need it to be present in other, 
		// and vice versa
		if (featureElements.length != otherPattern.featureElements.length)
			return false;
		
		for (final FeatureElement element : featureElements)
		{
			if (!ArrayUtils.contains(otherPattern.featureElements, element))
				return false;
		}
		
//		for (final FeatureElement element : otherPattern.featureElements())
//		{
//			if (!featureElements.contains(element))
//			{
//				return false;
//			}
//		}
		
		if (otherPattern.allowedRotations == null)
		{
			return allowedRotations == null;
		}
		else if (allowedRotations == null)
		{
			return false;
		}
		else
		{
			if (allowedRotations.size() != otherPattern.allowedRotations.size())
				return false;
			
			for (int i = 0; i < otherPattern.allowedRotations().size(); ++i)
			{
				if (!allowedRotations.contains(otherPattern.allowedRotations().getQuick(i)))
					return false;
			}
		}
		
		return allowsReflection == otherPattern.allowsReflection;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * equals() method that ignores restrictions on rotation / reflection
	 * 
	 * @param other
	 * @return True if, ignoring rotation / reflection, the patterns are equal
	 */
	public boolean equalsIgnoreRotRef(final Pattern other)
	{
		// for every feature element, we also need it to be present in other, 
		// and vice versa
		if (featureElements.length != other.featureElements.length)
		{
			return false;
		}
		
		for (final FeatureElement element : featureElements)
		{
			if (!ArrayUtils.contains(other.featureElements, element))
			{
				return false;
			}
		}
		
		for (final FeatureElement element : other.featureElements())
		{
			if (!ArrayUtils.contains(featureElements, element))
			{
				return false;
			}
		}
		
		return allowsReflection == other.allowsReflection;
	}
	
	/**
	 * hashCode() method that ignores restrictions on rotation / reflection
	 * 
	 * @return Hash code.
	 */
	public int hashCodeIgnoreRotRef()
	{
		final int prime = 31;
		int result = 1;
		
		if (featureElements == null)
		{
			result = prime * result;
		}
		else
		{
			int featureElementsHash = 0;
			for (final FeatureElement element : featureElements)
			{
				// XORing them all means order does not matter
				featureElementsHash ^= element.hashCode();
			}
			
			// adding prime inside brackets because that would also happen in 
			// ArrayList.hashCode()
			result = prime * result + (prime + featureElementsHash);
		}
		
		result = prime * result + (matchMoverDirection ? 1231 : 1237);
		return result;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		String str = "";
		
		if (!allowsReflection)
			str += "refl=false,";
		
		String rotsStr;
		if (allowedRotations != null)
		{
			rotsStr = "[";
			for (int i = 0; i < allowedRotations.size(); ++i)
			{
				rotsStr += allowedRotations.getQuick(i);
				if (i < allowedRotations.size() - 1)
				{
					rotsStr += ",";
				}
			}
			rotsStr += "]";
		}
		else
		{
			rotsStr = "all";
		}
		
		if (allowedRotations != null)
			str += String.format("rots=%s,", rotsStr);
		
		str += String.format("els=%s", Arrays.toString(featureElements));
		
		return str;
	}
	
	//-------------------------------------------------------------------------
	
}
