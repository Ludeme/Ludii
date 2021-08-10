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
 * Record of steps from a given element.
 * 
 * @author cambolbro
 */
public class Steps
{	
	private final SiteType siteType;
	private final int id;
	
	private final List<Step> steps = new ArrayList<Step>();
	private List<Step>[] inDirection;
	private List<Step>[] toSiteType;
	private List<Step>[][] toSiteTypeInDirection;
	
	/** Total directions for these Steps. */
	private final BitSet totalDirections = new BitSet();
	
	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 * 
	 * @param siteType The graph element type.
	 * @param id       The index of the step.
	 */
	public Steps(final SiteType siteType, final int id)
	{
		this.siteType = siteType;
		this.id = id;
		
		allocate();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The steps.
	 */
	public List<Step> steps()
	{
		return steps;
	}

	/**
	 * @param toType The graph element.
	 * @return The steps only for a graph element.
	 */
	public List<Step> toSiteType(final SiteType toType)
	{
		return toSiteType[toType.ordinal()];
	}

	/**
	 * @param dirn The direction.
	 * @return The steps only in a direction.
	 */
	public List<Step> inDirection(final AbsoluteDirection dirn)
	{
		return inDirection[dirn.ordinal()];
	}

	/**
	 * @param toType The graph element.
	 * @param dirn   The direction.
	 * @return The steps only in a direction and for a graph element type.
	 */
	public List<Step> toSiteTypeInDirection(final SiteType toType, final AbsoluteDirection dirn)
	{
		return toSiteTypeInDirection[toType.ordinal()][dirn.ordinal()];
	}
	
	/**
	 * @return The bitset with all the directions.
	 */
	public BitSet totalDirections()
	{
		return totalDirections;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Clear the steps in a direction
	 * 
	 * @param dirn The steps.
	 */
	public void clearInDirection(final AbsoluteDirection dirn)
	{
		inDirection[dirn.ordinal()].clear();
	}

	/**
	 * Add a step in a direction.
	 * 
	 * @param dirn The direction.
	 * @param step The step.
	 */
	public void addInDirection(final AbsoluteDirection dirn, final Step step)
	{
		inDirection[dirn.ordinal()].add(step);
	}
	
	/**
	 * Add a step in a direction and for a graph element type.
	 * 
	 * @param toType The graph element type.
	 * @param dirn   The direction.
	 * @param step   The step.
	 */
	public void addToSiteTypeInDirection(final SiteType toType, final AbsoluteDirection dirn, final Step step)
	{
		final List<Step> stepsList = toSiteTypeInDirection[toType.ordinal()][dirn.ordinal()];
		
		for (final Step existing : stepsList)
		{
			if (step.matches(existing))
				return;
		}
		
		stepsList.add(step);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Allocate the steps.
	 */
	@SuppressWarnings("unchecked")
	public void allocate()
	{
		final int numSiteTypes  = SiteType.values().length;
		final int numDirections = AbsoluteDirection.values().length;
		
		toSiteType = new ArrayList[numSiteTypes];
		for (int st = 0; st < numSiteTypes; st++)
			toSiteType[st] = new ArrayList<Step>();
		
		inDirection = new ArrayList[numDirections];
		for (int dirn = 0; dirn < numDirections; dirn++)
			inDirection[dirn] = new ArrayList<Step>();
		
		toSiteTypeInDirection = new ArrayList[numSiteTypes][numDirections];
		for (int st = 0; st < numSiteTypes; st++)
			for (int dirn = 0; dirn < numDirections; dirn++)
				toSiteTypeInDirection[st][dirn] = new ArrayList<Step>();
	}
	
	//-------------------------------------------------------------------------

	void add(final Step step)
	{
		for (final Step existing : steps)
			if (existing.from().matches(step.from()) && existing.to().matches(step.to()))
			{
				// Don't add duplicate steps
				//System.out.println("Steps.add(): Duplicate steps:\na) " + step + "\nb) " + existing);
				existing.directions().or(step.directions());  // keep any unknown directions
				return;
			}
		
		steps.add(step);
		
		final int toSiteTypeId = step.to().siteType().ordinal();
		toSiteType[toSiteTypeId].add(step);
		
		for (int dirn = step.directions().nextSetBit(0); dirn >= 0; dirn = step.directions().nextSetBit(dirn + 1))
		{
			inDirection[dirn].add(step);
			toSiteTypeInDirection[toSiteTypeId][dirn].add(step);
			//System.out.println("Adding step to site type " + step.to().siteType() + " in direction " + dirn + "...");
		}
		
		totalDirections.or(step.directions());
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Sort steps in all lists CW from N.
	 */
	public void sort()
	{
		final int numSiteTypes  = SiteType.values().length;
		final int numDirections = AbsoluteDirection.values().length;
		
		sort(steps);
		
		for (int dirn = 0; dirn < numDirections; dirn++)
			sort(inDirection[dirn]);

		for (int st = 0; st < numSiteTypes; st++)
			sort(toSiteType[st]);

		for (int st = 0; st < numSiteTypes; st++)
			for (int dirn = 0; dirn < numDirections; dirn++)
				sort(toSiteTypeInDirection[st][dirn]);
	}
	
	/**
	 * Sort steps in the specified list CW from N.
	 * 
	 * @param list The list of the steps.
	 */
	public static void sort(final List<Step> list)
	{
		final List<ItemScore> rank = new ArrayList<ItemScore>();
		for (int n = 0; n < list.size(); n++)
		{
			final Step step = list.get(n);
			final double theta = MathRoutines.angle(step.from().pt2D(), step.to().pt2D());
			double score = Math.PI / 2 - theta + 0.0001;
			while (score < 0)
				score += 2 * Math.PI;
			rank.add(new ItemScore(n, score));
		}
		Collections.sort(rank);
		
		// Append item references in new order
		for (int r = 0; r < rank.size(); r++)
			list.add(list.get(rank.get(r).id()));

		// Remove initial reference to items
		for (int r = 0; r < rank.size(); r++)
			list.remove(0);
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("Steps from " + siteType + " " + id + ":\n");
		for (final Step step : steps)
			sb.append("- " + step.toString() + "\n");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
}
