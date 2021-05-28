package game.util.graph;

import main.Constants;

//-----------------------------------------------------------------------------

/**
 * Record of graph element properties.
 * 
 * @author cambolbro
 */
public class Properties
{
	//-------------------------------------------------------------------------

	/** Whether the element is inside the perimeter of the graph. */
	public static final long INNER = (0x1L << 0);

	/** Whether the element is on the perimeter and is an outer element. */
	public static final long OUTER = (0x1L << 1);

	/** Whether the element is on the perimeter. */
	public static final long PERIMETER  = (0x1L << 2);

	/** Whether the element is a centre element. */
	public static final long CENTRE = (0x1L << 3);

	/** Whether the element is a major generator element in tiling. */
	public static final long MAJOR = (0x1L << 4);

	/** Whether the element is a minor satellite element in tiling. */
	public static final long MINOR = (0x1L << 5);

	/** Whether the element is a pivot element. */
	public static final long PIVOT = (0x1L << 6);

	/** Whether the element is an inter-layer element. */
	public static final long INTERLAYER = (0x1L << 7);

	/** Whether the element is a face with a null neighbour. */
	public static final long NULL_NBOR = (0x1L << 8);

	/** Whether the element is a corner. */
	public static final long CORNER = (0x1L << 10);	

	/** Whether the element is a convex corner. */
	public static final long CORNER_CONVEX = (0x1L << 11);	

	/** Whether the element is a concave corner. */
	public static final long CORNER_CONCAVE = (0x1L << 12);	

	/** Whether the element is on the phase 0. */
	public static final long PHASE_0 = (0x1L << 13);

	/** Whether the element is on the phase 1. */
	public static final long PHASE_1 = (0x1L << 14);

	/** Whether the element is on the phase 2. */
	public static final long PHASE_2 = (0x1L << 15);

	/** Whether the element is on the phase 3. */
	public static final long PHASE_3 = (0x1L << 16);
	
	/** Whether the element is on the phase 4. */
	public static final long PHASE_4 = (0x1L << 17);

	/** Whether the element is on the phase 5. */
	public static final long PHASE_5 = (0x1L << 18);

	/** Whether the element is on the left of the board. */
	public static final long LEFT = (0x1L << 25);

	/** Whether the element is on the right of the board. */
	public static final long RIGHT = (0x1L << 26);

	/** Whether the element is on the top of the board. */
	public static final long TOP = (0x1L << 27);

	/** Whether the element is on the bottom of the board. */
	public static final long BOTTOM = (0x1L << 28);

	/** Whether the element is an axial edge. */
	public static final long AXIAL = (0x1L << 30);

	/** Whether the element is an horizontal edge. */
	public static final long HORIZONTAL = (0x1L << 31);

	/** Whether the element is a vertical edge. */
	public static final long VERTICAL = (0x1L << 32);

	/** Whether the element is an angled edge. */
	public static final long ANGLED = (0x1L << 33);

	/** Whether the element is a slash edge /. */
	public static final long SLASH = (0x1L << 34);

	/** Whether the element is a slosh edge \. */
	public static final long SLOSH = (0x1L << 35);

	/** Whether the element is on the North side. */
	public static final long SIDE_N = (0x1L << 40);

	/** Whether the element is on the East side. */
	public static final long SIDE_E = (0x1L << 41);

	/** Whether the element is on the South side. */
	public static final long SIDE_S = (0x1L << 42);

	/** Whether the element is on the West side. */
	public static final long SIDE_W = (0x1L << 43);

	/** Whether the element is on the North East side. */
	public static final long SIDE_NE = (0x1L << 44);

	/** Whether the element is on the South East side. */
	public static final long SIDE_SE = (0x1L << 45);

	/** Whether the element is on the South West side. */
	public static final long SIDE_SW = (0x1L << 46);

	/** Whether the element is on the North West side. */
	public static final long SIDE_NW = (0x1L << 47);

	private long properties = 0;
	
	//-------------------------------------------------------------------------

	/**
	 * Default constructor.
	 */
	public Properties()
	{
	}

	/**
	 * Constructor with the properties in a long.
	 * 
	 * @param properties The long value.
	 */
	public Properties(final long properties)
	{
		this.properties = properties;
	}

	/**
	 * Constructor with the properties.
	 * 
	 * @param other The properties.
	 */
	public Properties(final Properties other)
	{
		this.properties = other.properties;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Clear the properties.
	 */
	public void clear()
	{
		properties = 0;
	}
	
	/**
	 * @return The properties in a long.
	 */
	public long get()
	{
		return properties;
	}

	/**
	 * @param property The property to check.
	 * @return True if the property is on these properties.
	 */
	public boolean get(final long property)
	{
		return (properties & property) != 0;
	}
	
	/**
	 * To set a property.
	 * 
	 * @param property The property to add.
	 */
	public void set(final long property)
	{
		properties |= property;
	}

	/**
	 * To set on a property.
	 * 
	 * @param property The property.
	 * @param on       The value.
	 */
	public void set(final long property, final boolean on)
	{
		if (on)
			properties |= property;
		else
			properties &= ~property;
	}

	/**
	 * To add a property.
	 * 
	 * @param other The property to add.
	 */
	public void add(final long other)
	{
		properties |= other;
	}

//	public void set(final long property)
//	{
//		properties = property;
//	}

	//-------------------------------------------------------------------------

	/**
	 * @return The phase converted in an integer.
	 */
	public int phase()
	{
		if (get(PHASE_0))
			return 0;
		if (get(PHASE_1))
			return 1;
		if (get(PHASE_2))
			return 2;
		if (get(PHASE_3))
			return 3;
		if (get(PHASE_4))
			return 4;
		if (get(PHASE_5))
			return 5;
		
		return Constants.UNDEFINED;
	}

	/**
	 * Clear the phases.
	 */
	public void clearPhase()
	{
		properties &= ~PHASE_0;
		properties &= ~PHASE_1;
		properties &= ~PHASE_2;
		properties &= ~PHASE_3;
		properties &= ~PHASE_4;
		properties &= ~PHASE_5;
	}
	
	/**
	 * Set the phase.
	 * 
	 * @param phase The phase to set.
	 */
	public void setPhase(final int phase)
	{
		properties &= ~PHASE_0;
		properties &= ~PHASE_1;
		properties &= ~PHASE_2;
		properties &= ~PHASE_3;
		properties &= ~PHASE_4;
		properties &= ~PHASE_5;
		
		switch (phase)
		{
		case 0:	properties |= PHASE_0; break;
		case 1:	properties |= PHASE_1; break;
		case 2:	properties |= PHASE_2; break;
		case 3:	properties |= PHASE_3; break;
		case 4:	properties |= PHASE_4; break;
		case 5:	properties |= PHASE_5; break;
		default:  // do nothing
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		if (get(Properties.INNER))		sb.append(" I");
		if (get(Properties.OUTER))		sb.append(" O");
		if (get(Properties.PIVOT))		sb.append(" PVT");
		if (get(Properties.PERIMETER))	sb.append(" PRM");
		if (get(Properties.INTERLAYER))	sb.append(" IL");
		
		if (get(Properties.CORNER))			sb.append(" CNR");
		if (get(Properties.CORNER_CONVEX))	sb.append("(X)");
		if (get(Properties.CORNER_CONCAVE))	sb.append("(V)");

		if (get(Properties.CENTRE))		sb.append(" CTR");

		if (get(Properties.AXIAL))		sb.append(" AXL");
		if (get(Properties.ANGLED))		sb.append(" AGL");

		if (get(Properties.PHASE_0))	sb.append(" PH_0");
		if (get(Properties.PHASE_1))	sb.append(" PH_1");
		if (get(Properties.PHASE_2))	sb.append(" PH_2");
		if (get(Properties.PHASE_3))	sb.append(" PH_3");
		if (get(Properties.PHASE_4))	sb.append(" PH_4");
		if (get(Properties.PHASE_5))	sb.append(" PH_5");

		String side = "";
		if (get(Properties.SIDE_N))		side += "/N";
		if (get(Properties.SIDE_E))		side += "/E";
		if (get(Properties.SIDE_S))		side += "/S";
		if (get(Properties.SIDE_W))		side += "/W";
		if (get(Properties.SIDE_NE))	side += "/NE";
		if (get(Properties.SIDE_SE))	side += "/SE";
		if (get(Properties.SIDE_SW))	side += "/SW";
		if (get(Properties.SIDE_NW))	side += "/NW";
		
		if (!side.isEmpty())
			side = " SD_" + side.substring(1);
		sb.append(side);

		final String str = sb.toString();
		
		return "<" + (str.isEmpty() ? "" : str.substring(1)) + ">";
	}
	
	//-------------------------------------------------------------------------
	
}
