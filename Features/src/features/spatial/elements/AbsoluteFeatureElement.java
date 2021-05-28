package features.spatial.elements;

/**
 * Absolute Feature Element; a single element of a (state-action) feature,
 * where positions are specified in an absolute manner.
 *
 * @author Dennis Soemers
 */
public class AbsoluteFeatureElement extends FeatureElement
{
	
	//-------------------------------------------------------------------------

	/** */
	protected ElementType type = null;

	/** Set to true to negate any element-type-based test */
	protected boolean not = false;

	/** Absolute position */
	protected int position;
	
	/** Index of Item to check for in cases where type == ElementType.Item */
	protected final int itemIndex;

	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 * @param type
	 * @param position
	 */
	public AbsoluteFeatureElement(final ElementType type, final int position)
	{
		this.type = type;
		this.position = position;
		this.itemIndex = -1;
	}

	/**
	 * Constructor.
	 * @param type
	 * @param not
	 * @param position
	 */
	public AbsoluteFeatureElement(final ElementType type, final boolean not, final int position)
	{
		this.type = type;
		this.not  = not;
		this.position = position;
		this.itemIndex = -1;
	}
	
	/**
	 * Constructor.
	 * @param type
	 * @param position
	 * @param itemIndex
	 */
	public AbsoluteFeatureElement(final ElementType type, final int position, final int itemIndex)
	{
		this.type = type;
		this.position = position;
		this.itemIndex = itemIndex;
	}

	/**
	 * Constructor.
	 * @param type
	 * @param not
	 * @param position
	 * @param itemIndex
	 */
	public AbsoluteFeatureElement(final ElementType type, final boolean not, final int position, final int itemIndex)
	{
		this.type = type;
		this.not  = not;
		this.position = position;
		this.itemIndex = itemIndex;
	}

	/**
	 * Copy constructor.
	 * @param other
	 */
	public AbsoluteFeatureElement(final AbsoluteFeatureElement other)
	{
		type = other.type;
		not  = other.not;
		position = other.position;
		itemIndex = other.itemIndex;
	}
	
	/**
	 * Constructor from String
	 * @param string
	 */
	public AbsoluteFeatureElement(final String string)
	{
		final int startPosStringIdx = string.indexOf("{");
		String typeString = string.substring(0, startPosStringIdx);
		String posString = string.substring(startPosStringIdx);
		
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
					iIdx = Integer.parseInt(typeString.substring(elType.label.length()));
				}
				
				break;
			}
		}
		
		itemIndex = iIdx;
		
		posString = posString.substring("{abs-".length(), posString.length() - "}".length());
		position = Integer.parseInt(posString);
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
	 * @return The position
	 */
	public int position()
	{
		return position;
	}
	
	@Override
	public int itemIndex()
	{
		return itemIndex;
	}
	
	@Override
	public boolean isAbsolute()
	{
		return true;
	}
	
	@Override
	public boolean isRelative()
	{
		return false;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean generalises(final FeatureElement other)
	{
		// absolute will never generalise relative
		if (other instanceof RelativeFeatureElement)
		{
			return false;
		}
		
		final AbsoluteFeatureElement otherElement = (AbsoluteFeatureElement) other;
		
		// absolute can only generalise another absolute if the positions are equal
		if (position == otherElement.position)
		{
			// and then we'll still need strict generalization from the type + not-flag test
			return FeatureElement.testTypeGeneralisation(type, not, otherElement.type, otherElement.not).strictlyGeneralises;
		}
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + itemIndex;
		result = prime * result + (not ? 1231 : 1237);
		result = prime * result + position;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object other)
	{
		if (!(other instanceof AbsoluteFeatureElement))
		{
			return false;
		}
		
		AbsoluteFeatureElement otherElement = (AbsoluteFeatureElement) other;
		
		return (type == otherElement.type && 
				not == otherElement.not && 
				position == otherElement.position && 
				itemIndex == otherElement.itemIndex);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		String str = type().label;
		
		if (type == ElementType.Item || type == ElementType.IsPos || type == ElementType.Connectivity)
		{
			str += itemIndex;
		}

		if (not)
		{
			str = "!" + str;
		}
		
		str += String.format("{abs-%s}", Integer.valueOf(position));

		return str;
	}
	
	//-------------------------------------------------------------------------

}
