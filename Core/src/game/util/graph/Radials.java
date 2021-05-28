package game.util.graph;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import main.math.MathRoutines;

//-----------------------------------------------------------------------------

/**
 * Record of radials from a given element.
 * 
 * @author cambolbro
 */
public class Radials
{	
	private final SiteType siteType;
	private final int      siteId;
	
	private final List<Radial> radials = new ArrayList<Radial>();

	private List<Radial>[] inDirection;
	
	/** Sublists of distinct radials, i.e. no duplication with opposites. */
	private List<Radial>[] distinctInDirection;

	/** Total directions for these Steps. */
	private final BitSet totalDirections = new BitSet();
	
	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 * 
	 * @param siteType The graph element type.
	 * @param id       The index of the radial.
	 */
	public Radials(final SiteType siteType, final int id)
	{
		this.siteType = siteType;
		this.siteId = id;
		
		allocate();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The list of the radials.
	 */
	public List<Radial> radials()
	{
		return Collections.unmodifiableList(radials);
	}

	/**
	 * @param dirn The absolute direction.
	 * @return The list of the radials.
	 */
	public List<Radial> inDirection(final AbsoluteDirection dirn)
	{
		return Collections.unmodifiableList(inDirection[dirn.ordinal()]);
	}
	
	/**
	 * @param dirn
	 * @return A subset of the "inDirection" radials, which excludes radials that are
	 * 	opposites of already-included radials.
	 */
	public List<Radial> distinctInDirection(final AbsoluteDirection dirn)
	{
		return Collections.unmodifiableList(distinctInDirection[dirn.ordinal()]);
	}
	
	/**
	 * @return The bitset corresponding of the directions.
	 */
	public BitSet totalDirections()
	{
		return totalDirections;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Allocate the radials.
	 */
	@SuppressWarnings("unchecked")
	public void allocate()
	{
		final int numDirections = AbsoluteDirection.values().length;
		
		inDirection = new ArrayList[numDirections];
		distinctInDirection = new ArrayList[numDirections];
		
		for (int dirn = 0; dirn < numDirections; dirn++)
		{
			inDirection[dirn] = new ArrayList<Radial>();
			distinctInDirection[dirn] = new ArrayList<Radial>();
		}
	}
	
	//-------------------------------------------------------------------------
		
	/**
	 * Add a radial in a direction.
	 * 
	 * @param dirn   The direction.
	 * @param radial The radial.
	 */
	public void addInDirection(final AbsoluteDirection dirn, final Radial radial)
	{
		inDirection[dirn.ordinal()].add(radial);
	}
		
	/**
	 * Add a radial in a distinct direction.
	 * 
	 * @param dirn   The direction.
	 * @param radial The radial.
	 */
	public void addDistinctInDirection(final AbsoluteDirection dirn, final Radial radial)
	{
		distinctInDirection[dirn.ordinal()].add(radial);
	}
	
	//-------------------------------------------------------------------------

	void addSafe(final Radial radial)
	{
		// Check if radial duplicates existing radial
		for (final Radial existing : radials)
			if (existing.matches(radial))
			{
				// Don't add duplicate radials
				//System.out.println("Steps.add(): Duplicate steps:\na) " + step + "\nb) " + existing);
				//existing.directions().or(radial.directions());  // keep any unknown directions
				return;
			}

		// Check if is opposite of existing radial
		for (final Radial existing : radials)  //inDirection[radial.direction().ordinal()])  //radials)
			if 
			(
				radial.direction() == AbsoluteDirection.CW && existing.direction() == AbsoluteDirection.CCW
				||
				radial.direction() == AbsoluteDirection.CCW && existing.direction() == AbsoluteDirection.CW
				||
				radial.direction() == AbsoluteDirection.In && existing.direction() == AbsoluteDirection.Out
				||
				radial.direction() == AbsoluteDirection.Out && existing.direction() == AbsoluteDirection.In
				||	
				radial.isOppositeAngleTo(existing)
				&&
				(
					radial.direction().specific() && existing.direction().specific() 
					|| 
					radial.direction() == existing.direction()
				)
			)
			{
				// Add but don't duplicate
				radial.addOpposite(existing);  // note - may be more than one opposite!
				existing.addOpposite(radial);
			}			

		// Check if is distinct for this direction
		boolean isDistinct = true;
		for (final Radial existing : inDirection[radial.direction().ordinal()])
		{
			// **
			// ** TODO: Check this logic. If some radials start failing to be 
			// **       found, or recognised as distinct, this is the first
			// **       thing to check.
			// **
			
			if (radial.stepsMatch(existing) || radial.isOppositeAngleTo(existing))			
			{
				isDistinct = false;
				break;
			}
			
			if (radial.opposites() != null)
				for (final Radial existingOpposite : radial.opposites())
				{
					if (radial.stepsMatch(existingOpposite))			
					{	
						isDistinct = false;
						break;
					}
				}
		}			
		
		radials.add(radial);
		
		// Store by direction
		inDirection[radial.direction().ordinal()].add(radial);
		totalDirections.set(radial.direction().ordinal());		
				
		if (isDistinct)
		{
//			distinct.add(radial);
			distinctInDirection[radial.direction().ordinal()].add(radial);
		}
	}
	
	/**
	 * Remove the subsets in the direction.
	 * 
	 * @param dirn The direction.
	 */
	public void removeSubsetsInDirection(final AbsoluteDirection dirn)
	{
		final int dirnId = dirn.ordinal();
		for (int n = inDirection[dirnId].size() - 1; n >= 0; n--)
		{
			final Radial radial = inDirection[dirnId].get(n);
			for (int nn = 0; nn < inDirection[dirnId].size(); nn++)
			{
				if (n == nn)
					continue;
				
				if (radial.isSubsetOf(inDirection[dirnId].get(nn)))
				{
					inDirection[dirnId].remove(n);
					break;
				}
			}
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Set the distinct radials.
	 */
	public void setDistinct()
	{
//		distinct.clear();
		for (int dirn = 0; dirn < AbsoluteDirection.values().length; dirn++)
			distinctInDirection[dirn].clear();
	
		// Find distinct radials over all radials
//		for (int r = 0; r < radials.size(); r++)
//		{
//			final Radial radial = radials.get(r);
//			
//			boolean isDistinct = true;
//			for (int rr = 0; rr < radials.size() && isDistinct; rr++)
//			{
//				if (r == rr)
//					continue;
//			
//				final Radial other = radials.get(rr);
//				if (other.opposites() == null)
//					continue;
//				
//				for (final Radial opp : other.opposites())
//					if (radial.stepsMatch(opp))
//					{
//						isDistinct = false;
//						break;
//					}
//			}
//			
//			//if (isDistinct)
//				distinct.add(radial);
//		}
		
//			for (final Radial radial : radials)
//			{
//				boolean isDistinct = true;
//				for (final Radial existing : distinct)
//				{
//					if (radial == existing)
//						continue;
//				
//					if (existing.opposites() == null)
//						continue;
//					
//					for (final Radial opp : existing.opposites())
//						if (radial.stepsMatch(opp))
//						{
//							isDistinct = false;
//							break;
//						}
//				}
//				
//				if (isDistinct)
//					distinct.add(radial);
//			}
	
		// Find distinct radials within each direction
		for (int dirn = 0; dirn < AbsoluteDirection.values().length; dirn++)
			for (final Radial radial : inDirection[dirn])
			{
				boolean isDistinct = true;
				for (final Radial existing : distinctInDirection[dirn])
				{
					if (radial == existing)
						continue;
				
					if (existing.opposites() == null)
						continue;
					
					for (final Radial opp : existing.opposites())
						if (radial.stepsMatch(opp))
						{
							isDistinct = false;
							break;
						}
				}
				
				if (isDistinct)
					distinctInDirection[dirn].add(radial);
			}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Sort radials in all lists CW from N.
	 */
	public void sort()
	{
		sort(radials);
		for (int dirn = 0; dirn < AbsoluteDirection.values().length; dirn++)
		{
			sort(inDirection[dirn]);
			sort(distinctInDirection[dirn]);
		}
	}
	
	/**
	 * Sort radials in the specified list CW from N.
	 * @param list List of radials to be sorted
	 */
	public static void sort(final List<Radial> list)
	{
		final List<ItemScore> rank = new ArrayList<ItemScore>();
		for (int n = 0; n < list.size(); n++)
		{
			final Radial radial = list.get(n);
			final double theta = MathRoutines.angle
								 (
									radial.steps()[0].pt2D(),
									radial.steps()[1].pt2D()
								 );
			double score = Math.PI / 2 - theta + 0.0001;
			while (score < 0)
				score += 2 * Math.PI;
			rank.add(new ItemScore(n, score));
		}
		Collections.sort(rank);
		
		// Remember original order
		final Radial[] orig = list.toArray(new Radial[list.size()]);
		
		// Set the items in list in new order
		for (int r = 0; r < rank.size(); r++)
			list.set(r, orig[rank.get(r).id()]);
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @param r1
	 * @param r2
	 * @return Angle formed by the two given radials, which are assumed to have a shared
	 * 	starting point.
	 */
	public static double angle(final Radial r1, final Radial r2)
	{
		assert (r1.steps()[0].equals(r2.steps()[0]));
		
		// Law of Cosines
		//
		// we want to compute angle C, opposite of side c, radials form rays a and b
		//
		// c^2 = a^2 + b^2 - 2ab cos(C)
		// 2ab cos(C) = a^2 + b^2 - c^2
		// cos(C) = (a^2 + b^2 - c^2) / 2ab
		// C = acos( (a^2 + b^2 - c^2) / 2ab )
		
		final double a = r1.steps()[1].pt2D().distance(r1.steps()[0].pt2D());
		final double b = r2.steps()[1].pt2D().distance(r2.steps()[0].pt2D());
		final double c = r2.steps()[1].pt2D().distance(r1.steps()[1].pt2D());
		
		return Math.acos((a*a + b*b - c*c) / (2.0 * a * b));
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("Radials from " + siteType + " " + siteId + ":\n");
		for (final AbsoluteDirection dirn : AbsoluteDirection.values())
		{
			for (final Radial radial : inDirection[dirn.ordinal()])
			{
				sb.append("- " + dirn + ": " + radial.toString());
			
				// Denote if this radial is distinct
				boolean isDistinct = false;
				for (final Radial dist : distinctInDirection[dirn.ordinal()])
					if (dist.matches(radial))
					{
						isDistinct = true;
						break;
					}
				if (isDistinct)
					sb.append("*");
				
				sb.append("\n");
			}
		}
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
}
