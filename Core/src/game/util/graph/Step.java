package game.util.graph;

import java.util.BitSet;
import java.util.List;

import game.util.directions.AbsoluteDirection;

//-----------------------------------------------------------------------------

/**
 * Steps from this particular graph element.
 * 
 * @author cambolbro
 */
public class Step
{
	protected final GraphElement from;
	protected final GraphElement to;
	
	/** Record of AbsoluteDirections that this step agrees with. */
	protected final BitSet directions = new BitSet();
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor.
	 * 
	 * @param from The from graph element.
	 * @param to   The to graph element.
	 */
	public Step
	(
		final GraphElement from,
		final GraphElement to
	)
	{
		this.from = from;
		this.to   = to;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return The from graph element.
	 */
	public GraphElement from()
	{
		return from;
	}

	/**
	 * @return The to graph element.
	 */
	public GraphElement to()
	{
		return to;
	}

	/**
	 * @return The bitset of the directions supported by the step.
	 */
	public BitSet directions()
	{
		return directions;
	}
		
	//-------------------------------------------------------------------------

	/**
	 * @param list A list of steps.
	 * @return true if the steps in entries are in the steps.
	 */
	public boolean in(final List<Step> list)
	{
		for (final Step step : list)
			if (from.matches(step.from()) && to.matches(step.to()))
				return true;
		return false;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param other The step.
	 * @return True if the step match.
	 */
	public boolean matches(final Step other)
	{
		if (!from.matches(other.from))
			return false;
		
		if (!to.matches(other.to))
			return false;
		
		if (!directions.equals(other.directions))
			return false;
		
		return true;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append(from.label() + " => " + to.label());
		
		if (!directions.isEmpty())
		{
			sb.append(" (");
			for (int d = directions.nextSetBit(0); d >= 0; d = directions.nextSetBit(d + 1))
			{
				if (d > 0)
					sb.append(", ");
				sb.append(AbsoluteDirection.values()[d]);
			}
			sb.append(")");
		}

		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
}
