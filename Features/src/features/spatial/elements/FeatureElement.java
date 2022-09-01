package features.spatial.elements;

/**
 * Abstract class for an element (for example, "empty", or "friend", or "any", etc.) that can be located 
 * in a single site in a feature/pattern, which is specified relatively using a Walk.
 * 
 * @author Dennis Soemers
 *
 */
public abstract class FeatureElement
{
	//-------------------------------------------------------------------------
	
	/** Element types. */
	public static enum ElementType
	{
		/** */
		Empty("Empty", "-"),

		/** */
		Friend("Friend", "f"),

		/** */
		Enemy("Enemy", "e"),
		
		// NOTE: not supporting "Next" because we can't easily pre-generate instances that directly check for a context-dependent value
		/** Next player will be equal to Enemy in most games (exception: games where X may have more than 1 turn before Y has a single turn) */
		//Next("Next", "n"),

		/** */
		Off("Off", "#"),

		/** */
		Any("Any", "*"),

		/** */
		P1("P1", "1"),

		/** */
		P2("P2", "2"),
		
		/** Index for a specific item */
		Item("Item", "I"),
		
		/** Check if a position specified relatively evaluates to a specific (absolute) position */
		IsPos("IsPos", "pos"),
		
		/** Check if a position specified relatively has number of connections = N */
		Connectivity("Connectivity", "N"),
		
		/** Check if a position specified relatively is closer to a specific Region than the anchor position */
		RegionProximity("RegionProximity", "R"),
		
		/** Check if a specific piece type is in orthogonal line-of-sight */
		LineOfSightOrth("LineOfSightOrth", "LOSO"),
		
		/** Check if a specific piece type is in diagonal line-of-sight */
		LineOfSightDiag("LineOfSightDiag", "LOSD"),
		
		// The following two are only used as "dummies" in Atomic Feature Generation, not inside actual features
		
		/** Only for use in atomic feature generation, not in real features */
		LastFrom("LastFrom", "last_from"),
		
		/** Only for use in atomic feature generation, not in real features */
		LastTo("LastTo", "last_to")
		;

		/** Full name. */
		public String name;

		/** Short label. */
		public String label;

		//---------------------------------

		/**
		 * Constructor.
		 * @param name
		 * @param label
		 */
		ElementType(final String name, final String label)	
		{ 
			this.name = name;
			this.label = label;
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param other
	 * @return A deep copy of the given Feature Element
	 */
	public static FeatureElement copy(final FeatureElement other)
	{
		if (other instanceof AbsoluteFeatureElement)
			return new AbsoluteFeatureElement((AbsoluteFeatureElement) other);
		else if (other instanceof RelativeFeatureElement)
			return new RelativeFeatureElement((RelativeFeatureElement) other);
		else
			return null;
	}
	
	/**
	 * @param string
	 * @return New Feature Element constructed from string
	 */
	public static FeatureElement fromString(final String string)
	{
		if (string.contains("abs-"))
			return new AbsoluteFeatureElement(string);
		else
			return new RelativeFeatureElement(string);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param other
	 * @return Whether this element strictly generalises the other one
	 */
	public abstract boolean generalises(final FeatureElement other);
	
	/**
	 * @return Feature Element type
	 */
	public abstract ElementType type();
	
	/**
	 * Sets the element type.
	 * @param type
	 */
	public abstract void setType(final ElementType type);
	
	/**
	 * Mark that we do NOT want this element type to occur
	 */
	public abstract void negate();

	/**
	 * @return True if we're a negated element
	 */
	public abstract boolean not();
	
	/**
	 * @return True if this is an absolute feature
	 */
	public abstract boolean isAbsolute();
	
	/**
	 * @return True if this is a relative feature
	 */
	public abstract boolean isRelative();
	
	/** 
	 * Most elements don't need a specific item index, but those of type "Item" or "IsPos" do 
	 * @return Item index for elements of type Item, or position for type IsPos
	 */
	public abstract int itemIndex();
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param other
	 * @return True if this feature element is compatible with the given other element
	 * in same position.
	 */
	public final boolean isCompatibleWith(final FeatureElement other)
	{
		final ElementType myType = type();
		final ElementType otherType = other.type();
		
		if (myType == otherType && itemIndex() == other.itemIndex())
		{
			return (not() == other.not());
		}
		
		if (!not() && !other.not())		// neither negated
		{
			switch (myType)
			{
			case Empty:
				switch (otherType)
				{
				case Any:
				case IsPos:
				case Connectivity:
				case RegionProximity:
				case LineOfSightOrth:
				case LineOfSightDiag:
					return true;
					//$CASES-OMITTED$
				default:
					return false;
				}
			case Friend:
				switch (otherType)
				{
				case Any:
				case P1:
				case P2:
				case Item:
				case IsPos:
				case Connectivity:
				case RegionProximity:
				case LineOfSightOrth:
				case LineOfSightDiag:
					return true;
					//$CASES-OMITTED$
				default:
					return false;
				}
			case Enemy:
				switch (otherType)
				{
				case Any:
				case P1:
				case P2:
				case Item:
				case IsPos:
				case Connectivity:
				case RegionProximity:
				case LineOfSightOrth:
				case LineOfSightDiag:
					return true;
					//$CASES-OMITTED$
				default:
					return false;
				}
			case Off:
				return false;
			case Any:
				return (otherType != ElementType.Off);
			case P1:
				switch (otherType)
				{
				case Any:
				case Friend:
				case Enemy:
				case P2:
				case Item:
				case IsPos:
				case Connectivity:
				case RegionProximity:
				case LineOfSightOrth:
				case LineOfSightDiag:
					return true;
					//$CASES-OMITTED$
				default:
					return false;
				}
			case P2:
				switch (otherType)
				{
				case Any:
				case Friend:
				case Enemy:
				case P1:
				case Item:
				case IsPos:
				case Connectivity:
				case RegionProximity:
				case LineOfSightOrth:
				case LineOfSightDiag:
					return true;
					//$CASES-OMITTED$
				default:
					return false;
				}
			case Item:
				switch (otherType)
				{
				case Any:
				case Friend:
				case Enemy:
				case P1:
				case P2:
				case IsPos:
				case Connectivity:
				case RegionProximity:
				case LineOfSightOrth:
				case LineOfSightDiag:
					return true;
					//$CASES-OMITTED$
				default:
					return false;
				}
			case IsPos:
				switch (otherType)
				{
				case Any:
				case Friend:
				case Enemy:
				case P1:
				case P2:
				case Item:
				case Connectivity:
				case RegionProximity:
				case LineOfSightOrth:
				case LineOfSightDiag:
					return true;
					//$CASES-OMITTED$
				default:
					return false;
				}
			case Connectivity:
				return (otherType != ElementType.Off);
			case RegionProximity:
				return (otherType != ElementType.Off);
			case LineOfSightOrth:
				return (otherType != ElementType.Off);
			case LineOfSightDiag:
				return (otherType != ElementType.Off);
				//$CASES-OMITTED$
			default:
				System.err.println("Unrecognised element type: " + myType);
				throw new UnsupportedOperationException();
			}
		}
		else if (not() && other.not())	// both negated
		{
			switch (myType)
			{
			case Empty:
				return (otherType != ElementType.Any); 
			case Friend:
				return true;
			case Enemy:
				return true;
			case Off:
				return (otherType != ElementType.Any);
			case Any:
				return (otherType != ElementType.Off);
			case P1:
				return true;
			case P2:
				return true;
			case Item:
				return true;
			case IsPos:
				return true;
			case Connectivity:
				return (otherType != ElementType.Any);
			case RegionProximity:
				return (otherType != ElementType.Any);
			case LineOfSightOrth:
				return true;
			case LineOfSightDiag:
				return true;
				//$CASES-OMITTED$
			default:
				System.err.println("Unrecognised element type: " + myType);
				throw new UnsupportedOperationException();
			}
		}
		else if (not() && !other.not())	// we negated, other not negated
		{
			switch (myType)
			{
			case Empty:
				return true;
			case Friend:
				return true;
			case Enemy:
				return true;
			case Off:
				return true;
			case Any:
				return (otherType == ElementType.Off);
			case P1:
				return true;
			case P2:
				return true;
			case Item:
				return true;
			case IsPos:
				return true;
			case Connectivity:
				return (otherType != ElementType.Off);
			case RegionProximity:
				return (otherType != ElementType.Off);
			case LineOfSightOrth:
				return true;
			case LineOfSightDiag:
				return true;
				//$CASES-OMITTED$
			default:
				System.err.println("Unrecognised element type: " + myType);
				throw new UnsupportedOperationException();
			}
		}
		else							// we not negated, other negated
		{
			return other.isCompatibleWith(this);
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * A pair of booleans that we can return when testing for generalisation between
	 * two pairs of Element Type + not-flag. The first result will inform us about generalisation,
	 * and the second about strict generalisation (excluding equality).
	 * 
	 * @author Dennis Soemers
	 */
	public static class TypeGeneralisationResult
	{
		
		/** Whether we generalise (or are equal) */
		public boolean generalises;
		
		/** Whether we strictly generalise (no equality) */
		public boolean strictlyGeneralises;
		
		/**
		 * Constructor
		 * @param generalises
		 * @param strictlyGeneralises
		 */
		public TypeGeneralisationResult(final boolean generalises, final boolean strictlyGeneralises)
		{
			this.generalises = generalises;
			this.strictlyGeneralises = strictlyGeneralises;
		}
	}
	
	/**
	 * Tells us whether an element with the first type and first not-flag may generalise
	 * (both strictly and not strictly) a different element with the second type and not-flag
	 * (does not yet test for walks/positions).
	 * 
	 * @param firstType
	 * @param firstNot
	 * @param secondType
	 * @param secondNot
	 * @return Result.
	 */
	public static TypeGeneralisationResult testTypeGeneralisation
	(
		final ElementType firstType, 
		final boolean firstNot,
		final ElementType secondType, 
		final boolean secondNot
	)
	{
		if (firstNot && !secondNot)
		{
			//-----------------------------------------------------------------
			// We have a "not" modifier and the other element doesn't
			//-----------------------------------------------------------------
			switch (firstType)
			{
			case Empty:
				// "not empty" fails to generalise "empty", "off board", "item" (which may test for empty with itemIndex = 0),
				// "is pos", and "connectivity"
				if 
				(
					secondType == ElementType.Empty ||
					secondType == ElementType.Off || 
					secondType == ElementType.Item || 
					secondType == ElementType.IsPos ||
					secondType == ElementType.Connectivity ||
					secondType == ElementType.RegionProximity ||
					secondType == ElementType.LineOfSightOrth ||
					secondType == ElementType.LineOfSightDiag
				)
				{
					return new TypeGeneralisationResult(false, false);
				}
				else
				{
					return new TypeGeneralisationResult(true, true);
				}
			case Friend:
				// "not friend" generalises "empty", "enemy", and "off board"
				if 
				(
					secondType != ElementType.Empty && 
					secondType != ElementType.Enemy && 
					secondType != ElementType.Off
				)
				{
					return new TypeGeneralisationResult(false, false);
				}
				else
				{
					return new TypeGeneralisationResult(true, true);
				}
			case Enemy:
				// "not enemy" generalises "empty", "friend", and "off board"
				if 
				(
					secondType != ElementType.Empty && 
					secondType != ElementType.Friend &&
					secondType != ElementType.Off
				)
				{
					return new TypeGeneralisationResult(false, false);
				}
				else
				{
					return new TypeGeneralisationResult(true, true);
				}
			case Off:
				// "not off" only fails to generalise "any" and "off board"
				if (secondType == ElementType.Off || secondType == ElementType.Any)
					return new TypeGeneralisationResult(false, false);
				else
					return new TypeGeneralisationResult(true, true);
			case Any:
				// "not any" can only equal "off board" (but not a strict generalisation)
				if (secondType != ElementType.Off)
					return new TypeGeneralisationResult(false, false);
				else
					break;
			case P1:
				// "not P1" generalises "empty", "off board", and "P2"
				if 
				(
					secondType != ElementType.Empty && 
					secondType != ElementType.Off &&
					secondType != ElementType.P2
				)
				{
					return new TypeGeneralisationResult(false, false);
				}
				else
				{
					return new TypeGeneralisationResult(true, true);
				}
			case P2:
				// "not P2" generalises "empty", "off board", and "P1"
				if 
				(
					secondType != ElementType.Empty && 
					secondType != ElementType.Off &&
					secondType != ElementType.P1
				)
				{
					return new TypeGeneralisationResult(false, false);
				}
				else
				{
					return new TypeGeneralisationResult(true, true);
				}
			case Item:
				// "not item" only generalises "off board"
				if (secondType != ElementType.Off)
					return new TypeGeneralisationResult(false, false);
				else
					return new TypeGeneralisationResult(true, true);
			case IsPos:
				// "not is pos" doesn't generalise anything
				return new TypeGeneralisationResult(false, false);
			case Connectivity:
				// "not connectivity" doesn't generalise anything
				return new TypeGeneralisationResult(false, false);
			case RegionProximity:
				// "not closer than anchor to region" doesn't generalise anything
				return new TypeGeneralisationResult(false, false);
			case LineOfSightOrth:
				// "not in orthogonal line of sight" doesn't generalise anything
				return new TypeGeneralisationResult(false, false);
			case LineOfSightDiag:
				// "not in diagonal line of sight" doesn't generalise anything
				return new TypeGeneralisationResult(false, false);
				//$CASES-OMITTED$
			default:
				System.err.println("Unrecognised element type: " + firstType);
				throw new UnsupportedOperationException();
			}
		}
		else if (!firstNot && secondNot)
		{
			//-----------------------------------------------------------------
			// We do not have a "not" modifier and the other element does
			//-----------------------------------------------------------------

			if (firstType == ElementType.Off)		// "off board" only equals "not any" (but not a strict generalisation)
			{
				if (secondType != ElementType.Any)
				{
					return new TypeGeneralisationResult(false, false);
				}
			}
			else		// any other "X" doesn't generalise any "not Y"
			{
				return new TypeGeneralisationResult(false, false);
			}
		}
		else if (!firstNot && !secondNot)
		{
			//-----------------------------------------------------------------
			// Both elements have a "not" modifier
			//-----------------------------------------------------------------
			switch (firstType)
			{
			case Empty:
				// "not empty" only generalises "not any"
				if (secondType != ElementType.Any)
					return new TypeGeneralisationResult(false, false);
				else
					return new TypeGeneralisationResult(true, true);
			case Friend:
				// "not friend" only generalises "not any"
				if (secondType != ElementType.Any)
					return new TypeGeneralisationResult(false, false);
				else
					return new TypeGeneralisationResult(true, true);
			case Enemy:
				// "not enemy" only generalises "not any"
				if (secondType != ElementType.Any)
					return new TypeGeneralisationResult(false, false);
				else
					return new TypeGeneralisationResult(true, true);
			case Off:
				// "not off" only generalises "not empty"
				if (secondType != ElementType.Empty)
					return new TypeGeneralisationResult(false, false);
				else
					return new TypeGeneralisationResult(true, true);
			case Any:
				// "not any" can only equal "not any", but not generalise any other "not X"
				if (secondType != ElementType.Any)
					return new TypeGeneralisationResult(false, false);
				else
					break;
			case P1:
				// "not P1" only generalises "not any"
				if (secondType != ElementType.Any)
					return new TypeGeneralisationResult(false, false);
				else
					return new TypeGeneralisationResult(true, true);
			case P2:
				// "not P2" only generalises "not any"
				if (secondType != ElementType.Any)
					return new TypeGeneralisationResult(false, false);
				else
					return new TypeGeneralisationResult(true, true);
			case Item:
				// "not Item" only generalises "not any"
				if (secondType != ElementType.Any)
					return new TypeGeneralisationResult(false, false);
				else
					return new TypeGeneralisationResult(true, true);
			case IsPos:
				// "not Is Pos" only generalises "not any"
				if (secondType != ElementType.Any)
					return new TypeGeneralisationResult(false, false);
				else
					return new TypeGeneralisationResult(true, true);
			case Connectivity:
				// "not connectivity" only generalises "not any"
				if (secondType != ElementType.Any)
					return new TypeGeneralisationResult(false, false);
				else
					return new TypeGeneralisationResult(true, true);
			case RegionProximity:
				// "not closer than anchor to region" only generalises "not any"
				if (secondType != ElementType.Any)
					return new TypeGeneralisationResult(false, false);
				else
					return new TypeGeneralisationResult(true, true);
			case LineOfSightOrth:
				// "not in orthogonal line of sight" only generalises "not any"
				if (secondType != ElementType.Any)
					return new TypeGeneralisationResult(false, false);
				else
					return new TypeGeneralisationResult(true, true);
			case LineOfSightDiag:
				// "not in diagonal line of sight" only generalises "not any"
				if (secondType != ElementType.Any)
					return new TypeGeneralisationResult(false, false);
				else
					return new TypeGeneralisationResult(true, true);
				//$CASES-OMITTED$
			default:
				System.err.println("Unrecognised element type: " + firstType);
				throw new UnsupportedOperationException();
			}
		}
		else
		{
			//-----------------------------------------------------------------
			// Neither element has a "not" modifier
			//-----------------------------------------------------------------

			if (firstType != secondType)	// only need to consider cases where the types are different, otherwise they're always equal
			{
				if (firstType != ElementType.Any)	// anything other than "any" won't be a generalisation
				{
					return new TypeGeneralisationResult(false, false);
				}
				else
				{
					if (secondType != ElementType.Any)
					{
						return new TypeGeneralisationResult(true, true);
					}
				}
			}
		}
		
		return new TypeGeneralisationResult(true, false);
	}
}
