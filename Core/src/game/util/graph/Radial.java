package game.util.graph;

//import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import game.util.directions.AbsoluteDirection;
import main.math.MathRoutines;

//-----------------------------------------------------------------------------

/**
 * Sequence of steps in a direction.
 * 
 * @author cambolbro
 */
public class Radial
{
	/** The array of steps making up this radial. */
	private final GraphElement[] steps;
	
	/** 
	 * Direction of the first step in this radial. 
	 * This is useful to store so that radials can be quickly sorted by direction in Trajectories.
	 */
	private final AbsoluteDirection direction;
	
	/** Matching radials in the opposite direction (if any). */
	private List<Radial> opposites = null;
	
	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 * 
	 * @param steps     The array of steps in this radial.
	 * @param direction The absolute direction of the radial.
	 */
	public Radial(final GraphElement[] steps, final AbsoluteDirection direction)
	{
		this.steps = steps;
		this.direction = direction;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The array of the graph elements in the steps.
	 */
	public GraphElement[] steps()
	{
		return steps;
	}
	
	/**
	 * @return Direction of the first step in this radial
	 */
	public AbsoluteDirection direction()
	{
		return direction;
	}

	/**
	 * @return The list of the opposite radials.
	 */
	public List<Radial> opposites()
	{
		return opposites;
	}
	
	//-------------------------------------------------------------------------
	// Graph element routines

	/**
	 * @return The origin of the steps.
	 */
	public GraphElement from()
	{
		return steps[0];
	}

	/**
	 * @return The last step.
	 */
	public GraphElement lastStep()
	{
		return steps[steps.length - 1];
	}
	
	//-------------------------------------------------------------------------
	/**
	 * @param other The other radial.
	 * @return True if the radial match.
	 */
	public boolean matches(final Radial other)
	{
		if (direction != other.direction)
			return false;
		
		return stepsMatch(other);
	}
	
	/**
	 * @param other The radial.
	 * @return True if the step match.
	 */
	public boolean stepsMatch(final Radial other)
	{
		if (steps.length != other.steps.length)
			return false;
		
		for (int n = 0; n < steps.length; n++)
			if (!steps[n].matches(other.steps[n]))
				return false;
		
		return true;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param other The opposite radial.
	 * @return Whether the specified radial is opposite to this one.
	 */
	public boolean isOppositeAngleTo(final Radial other)
	{
		final double threshold = 0.1;  // amount of allowable bend ~9 degrees
		final double tanThreshold = Math.tan(threshold);
		
		final GraphElement geA = steps[1];
		final GraphElement geB = steps[0];
		final GraphElement geC = other.steps[1];
		
//		final Point2D ptA = geA.pt2D();
//		final Point2D ptB = geB.pt2D();
//		final Point2D ptC = geC.pt2D();
		
		//final double diff = MathRoutines.angleDifference(ptA, ptB, ptC);
		//
		//return Math.abs(diff) < threshold;
		
		// Same optimisation as in Trajectories::followRadial()
		// see comments in that method for extensive explanation
		final double absTanDiff = MathRoutines.absTanAngleDifference3D(geA.pt(), geB.pt(), geC.pt());
//		final double absTanDiff = MathRoutines.absTanAngleDifferencePosX(ptA, ptB, ptC);
		return absTanDiff < tanThreshold;
	}

	//-------------------------------------------------------------------------

	/**
	 * Add an opposite radial.
	 * 
	 * @param opp The opposite radial.
	 */
	public void addOpposite(final Radial opp)
	{
		if (opposites == null)
		{
			// Start the opposites list with this one
			opposites = new ArrayList<Radial>();
			opposites.add(opp);
		}
		else
		{
			// Check that this opposite isn't already included
			for (final Radial existingOpposite : opposites)
				if 
				(
					(direction.specific() || opp.direction() == direction)
					||
					opp.stepsMatch(existingOpposite)
				)
					return;
			opposites.add(opp);
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Removes subsets from the list of opposites.
	 */
	public void removeOppositeSubsets()
	{
		if (opposites == null)
			return;
		
		for (int o = opposites.size() - 1; o >= 0; o--)
		{
			final Radial oppositeO = opposites.get(o);			
			for (int n = 0; n < opposites.size(); n++)
			{
				if (n == o)
					continue;
				if (oppositeO.isSubsetOf(opposites.get(n)))
				{
					opposites.remove(o);
					break;
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param other The radial.
	 * @return True if the radial is a subset of the radial in entry.
	 */
	public boolean isSubsetOf(final Radial other)
	{
		//if (direction != other.direction)
		//	return false;
		
		if (steps.length > other.steps.length)
			return false;
		
		for (int n = 0; n < steps.length; n++)
			if (!steps[n].matches(other.steps[n]))
				return false;
		
		return true;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		for (int n = 0; n < steps.length; n++)
		{
			if (n > 0)
				sb.append("-");
			sb.append(steps[n].label());
		}
		//sb.append(":" + direction);
		
		if (opposites != null)
		{
			sb.append(" [");
			for (int o = 0; o < opposites.size(); o++)
			{
				final Radial opp = opposites.get(o);
				if (o > 0)
					sb.append(", ");
				for (int n = 0; n < opp.steps.length; n++)
				{
					if (n > 0)
						sb.append("-");
					sb.append(opp.steps[n].label());
				}
				//sb.append(":" + opp.direction());
			}
			sb.append("]");
		}
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
}
