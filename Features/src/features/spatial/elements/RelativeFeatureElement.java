package features.spatial.elements;

import features.spatial.Walk;

/**
 * Feature Elements with positions specified relatively (through Walks)
 * 
 * @author Dennis Soemers and cambolbro
 */
public class RelativeFeatureElement extends FeatureElement
{
	
	//-------------------------------------------------------------------------

	/** */
	protected ElementType type = null;

	/** Set to true to negate any element-type-based test */
	protected boolean not = false;

	/** How do we end up in the site where we expect to see this from some reference point? */
	protected Walk walk;
	
	/** Index of Item to check for in cases where type == ElementType.Item */
	protected final int itemIndex;

	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 * @param type
	 * @param walk
	 */
	public RelativeFeatureElement(final ElementType type, final Walk walk)
	{
		this(type, false, walk, -1);
	}

	/**
	 * Constructor.
	 * @param type
	 * @param not
	 * @param walk
	 */
	public RelativeFeatureElement
	(
		final ElementType type, 
		final boolean not, 
		final Walk walk
	)
	{
		this(type, not, walk, -1);
	}
	
	/**
	 * Constructor.
	 * @param type
	 * @param walk
	 * @param itemIndex
	 */
	public RelativeFeatureElement
	(
		final ElementType type, 
		final Walk walk, 
		final int itemIndex
	)
	{
		this(type, false, walk, itemIndex);
	}
	
	/**
	 * Constructor.
	 * @param type
	 * @param not
	 * @param walk
	 * @param itemIndex
	 */
	public RelativeFeatureElement
	(
		final ElementType type, 
		final boolean not, 
		final Walk walk, 
		final int itemIndex
	)
	{
		this.type = type;
		this.not  = not;
		this.walk = walk;
		this.itemIndex = itemIndex;
	}

	/**
	 * Copy constructor.
	 * @param other
	 */
	public RelativeFeatureElement(final RelativeFeatureElement other)
	{
		type = other.type;
		not  = other.not;
		walk = new Walk(other.walk());
		itemIndex = other.itemIndex;
	}
	
	/**
	 * Constructor from string
	 * @param string
	 */
	public RelativeFeatureElement(final String string)
	{
		final int startWalkStringIdx = string.indexOf("{");
		String typeString = string.substring(0, startWalkStringIdx);
		final String walkString = string.substring(startWalkStringIdx);
		
		if (typeString.startsWith("!"))
		{
			not = true;
			typeString = typeString.substring("!".length());
		}
		
		int iIdx = -1;
		
		for (final ElementType elType : ElementType.values())
		{
			if (typeString.startsWith(elType.label))
			{
				type = elType;
				
				if (typeString.length() > elType.label.length())
				{
					iIdx = Integer.parseInt(
							typeString.substring(elType.label.length()));
				}
				
				break;
			}
		}
		
		itemIndex = iIdx;
		
		walk = new Walk(walkString);
	}

	//-------------------------------------------------------------------------

	@Override
	public ElementType type() 
	{
		return type;
	}

	@Override
	public void setType(final ElementType type)
	{
		this.type = type;
	}
	
	@Override
	public void negate()
	{
		not = true;
	}

	@Override
	public boolean not() 
	{
		return not;
	}

	/**
	 * @return The walk
	 */
	public Walk walk()
	{
		return walk;
	}
	
	@Override
	public int itemIndex()
	{
		return itemIndex;
	}
	
	@Override
	public boolean isAbsolute()
	{
		return false;
	}
	
	@Override
	public boolean isRelative()
	{
		return true;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean generalises(final FeatureElement other)
	{
		final TypeGeneralisationResult generalisationResult = 
				FeatureElement.testTypeGeneralisation(type, not, other.type(), other.not());
		
		if (!generalisationResult.generalises)
		{
			return false;
		}

		//---------------------------------------------------------------------
		// We have compared types and not-modifiers, need to look at Walks now
		//---------------------------------------------------------------------
		if (other instanceof AbsoluteFeatureElement)
		{
			// relative can only generalise absolute if we have no 
			// restriction on relative positions
			return walk.steps().size() == 0;
		}
		
		final RelativeFeatureElement otherRel = (RelativeFeatureElement) other;
		
		return (generalisationResult.strictlyGeneralises && walk.equals(otherRel.walk()));
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + itemIndex;
		result = prime * result + (not ? 1231 : 1237);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((walk == null) ? 0 : walk.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object other)
	{
		if (!(other instanceof RelativeFeatureElement))
			return false;
				
		final RelativeFeatureElement otherElement = (RelativeFeatureElement) other;
		
		return (type == otherElement.type && 
				not == otherElement.not && 
				walk.equals(otherElement.walk()) &&
				itemIndex == otherElement.itemIndex);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		String str = type().label;
		
		if 
		(
			type == ElementType.Item || 
			type == ElementType.IsPos || 
			type == ElementType.Connectivity || 
			type == ElementType.RegionProximity ||
			type == ElementType.LineOfSightOrth ||
			type == ElementType.LineOfSightDiag
		)
		{
			str += itemIndex;
		}

		if (not)
		{
			str = "!" + str;
		}
		
		str += walk;

		return str;
	}

	//-------------------------------------------------------------------------	

}
