package game.util.graph;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import main.math.MathRoutines;
import main.math.Point3D;
import main.math.Vector;

//-----------------------------------------------------------------------------

/**
 * Record of relations between elements within a graph.
 * 
 * @author cambolbro
 */
public class Trajectories
{	
	
	/** Collections of steps; first indexed by SiteType, second indexed by the from site's ID */
	private Steps[][] steps;
	
	/** Collections of radials: first indexed by SiteType, second indexed by site ID */
	private Radials[][]	radials;
		
	/** Total directions for graph. */
	private final BitSet totalDirections = new BitSet();
	
	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 */
	public Trajectories()
	{
		// Do not create relations here!
		// Only call create() from the Board class, once, on the final graph object. 
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param fromType The graph element type of the from site.
	 * @param siteId   The index of the graph element type.
	 * @param toType   The graph element type of the to site.
	 * @return The steps according to graph element types for the origin and the
	 *         target.
	 */
	public List<Step> steps(final SiteType fromType, final int siteId, final SiteType toType)
	{
		return steps[fromType.ordinal()][siteId].toSiteType(toType);
	}

	/**
	 * @param fromType The graph element type of the from site.
	 * @param siteId   The index of the graph element type.
	 * @param dirn     The direction.
	 * @return The steps according to graph element types for the origin and an
	 *         absolute direction.
	 */
	public List<Step> steps(final SiteType fromType, final int siteId, final AbsoluteDirection dirn)
	{
		return steps[fromType.ordinal()][siteId].inDirection(dirn);
	}

	/**
	 * @param fromType The graph element type of the from site.
	 * @param siteId   The index of the graph element type.
	 * @param toType   The graph element type of the to site.
	 * @param dirn     The direction.
	 * @return The steps according to graph element types for the origin and the
	 *         target and for an absolute direction.
	 */
	public List<Step> steps(final SiteType fromType, final int siteId, final SiteType toType, final AbsoluteDirection dirn)
	{
		return steps[fromType.ordinal()][siteId].toSiteTypeInDirection(toType, dirn);
	}

	/**
	 * @param fromType The graph element type of the from site.
	 * @param siteId   The index of the graph element type.
	 * @return The radials according to a graph element type for the origin.
	 */
	public Radials radials(final SiteType fromType, final int siteId)
	{
		return radials[fromType.ordinal()][siteId]; 
	}

	/**
	 * @param fromType The graph element type of the from site.
	 * @param siteId   The index of the graph element type.
	 * @param dirn     The direction.
	 * @return The radials according to a graph element type for the origin and for
	 *         an absolute direction.
	 */
	public List<Radial> radials(final SiteType fromType, final int siteId, final AbsoluteDirection dirn)
	{
		return radials[fromType.ordinal()][siteId].inDirection(dirn); 
	}
	
	/**
	 * @return The bitset of the directions.
	 */
	public BitSet totalDirections()
	{
		return totalDirections;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Create the trajectories.
	 * 
	 * @param graph The graph.
	 */
	public void create(final Graph graph)
	{
		// Prepare the arrays
		final int numSiteTypes = SiteType.values().length;
		steps   = new Steps[numSiteTypes][];
		radials = new Radials[numSiteTypes][];

		for (final SiteType siteType : SiteType.values())
		{
			final int st = siteType.ordinal();
			steps[st] = new Steps[graph.elements(siteType).size()];
			radials[st] = new Radials[graph.elements(siteType).size()];
		}
		
		generateSteps(graph);
		generateRadials(graph);
		
		//System.out.println("===========================================================\n");
		//System.out.println(graph);
		// report(graph);
	}
	
	//-------------------------------------------------------------------------

	void generateSteps(final Graph graph)
	{
		for (final SiteType siteType : SiteType.values())
		{
			for (final GraphElement from : graph.elements(siteType))
			{
				final int st = siteType.ordinal();
				final int id = from.id();
				
				final Steps stepsFrom = new Steps(siteType, id);
				from.stepsTo(stepsFrom);
				steps[st][id] = stepsFrom;
			}
		}

		setDirections(graph);

		for (int st = 0; st < SiteType.values().length; st++)
			for (final Steps stepList : steps[st])
			{
				totalDirections.or(stepList.totalDirections());
				stepList.sort();
			}
				
//		// Reorder steps in clockwise order within each relation type
//		for (int st = 0; st < SiteType.values().length; st++)
//			for (int reln = 0; reln < RelationType.values().length; reln++)
//				Collections.sort(stepsTo.steps()[st][reln], new Comparator<StepTo>() 
//				{
//					@Override
//					public int compare(final StepTo a, final StepTo b)
//					{
//						if (a.theta() == b.theta())
//							return 0;		
//						return (a.theta() > b.theta()) ? -1 : 1;
//					}
//				});
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Sets the 'dirn' field of all Steps in the list with unique directions.
	 */
	void setDirections(final Graph graph)
	{
		setCompassDirections(graph);  // includes pyramidal directions		
		setCircularDirections(graph);
	
		// Set known directions in each step
		for (final SiteType siteType : SiteType.values())
		{
			final List<? extends GraphElement> elements = graph.elements(siteType);
			final int st = siteType.ordinal();
						
			for (int id = 0; id < elements.size(); id++)
				for (final AbsoluteDirection dirn : AbsoluteDirection.values())
				{
					final int dirnIndex = dirn.ordinal();
					for (final Step step : steps[st][id].inDirection(AbsoluteDirection.values()[dirnIndex]))
					{
						step.directions().set(dirnIndex, true);	
//						steps[st].get(id).toSiteTypeInDirection(step.to.siteType(), dirn).add(step);
						steps[st][id].addToSiteTypeInDirection(step.to.siteType(), dirn, step);
						totalDirections.set(dirnIndex, true); 
					}
				}
		}
	}

	/**
	 * Sets the 'dirn' field of all Steps in the list with unique directions.
	 */
	void setCompassDirections(final Graph graph)
	{
		for (final SiteType siteType : SiteType.values())
		{
			final List<? extends GraphElement> elements = graph.elements(siteType);
			for (final GraphElement element : elements)
				setCompassDirections(graph, element);
		}
	}

	/**
	 * Sets the 'dirn' field of all Steps in the list with unique directions.
	 */
	void setCompassDirections(final Graph graph, final GraphElement element)
	{
		final int st = element.siteType().ordinal();
		final List<Step> stepList = steps[st][element.id()].steps();
	
		final double unit = graph.averageEdgeLength();
		
		// Check if only need 8 compass directions
		final BitSet used = new BitSet();		
		boolean collision = false;	
		
		for (final Step step : stepList)
		{	
			final int dirn = mapAngleToAbsoluteDirection(step, unit, false).ordinal();
			if (used.get(dirn))
			{
				// More than one step in this direction; use 16 compass directions
				collision = true;
				break;
			}
			used.set(dirn, true);
		}
		
		// Add new directions
		for (final Step step : stepList)
		{	
			final AbsoluteDirection dirn = mapAngleToAbsoluteDirection(step, unit, collision);
			// System.out.println("Settings step " + step + " to " + dirn + ".");
//			stepsInDirection[st][element.id()][dirn].add(step);
						
//			steps[st].get(element.id()).inDirection(AbsoluteDirection.values()[dirn]).add(step);
			steps[st][element.id()].addInDirection(AbsoluteDirection.values()[dirn.ordinal()], step);

			if 
			(
				dirn == AbsoluteDirection.N
				||
				dirn == AbsoluteDirection.E
				||
				dirn == AbsoluteDirection.S
				||
				dirn == AbsoluteDirection.W
				||
				dirn == AbsoluteDirection.NE
				||
				dirn == AbsoluteDirection.SE
				||
				dirn == AbsoluteDirection.SW
				||
				dirn == AbsoluteDirection.NW
				||
				dirn == AbsoluteDirection.NNE
				||
				dirn == AbsoluteDirection.ENE
				||
				dirn == AbsoluteDirection.ESE
				||
				dirn == AbsoluteDirection.SSE
				||
				dirn == AbsoluteDirection.SSW
				||
				dirn == AbsoluteDirection.WSW
				||
				dirn == AbsoluteDirection.WNW
				||
				dirn == AbsoluteDirection.NNW
			)
				steps[st][element.id()].addInDirection(AbsoluteDirection.SameLayer, step);
				
			if 
			(
				dirn == AbsoluteDirection.U
				||
				dirn == AbsoluteDirection.UN
				||
				dirn == AbsoluteDirection.UE
				||
				dirn == AbsoluteDirection.US
				||
				dirn == AbsoluteDirection.UW
				||
				dirn == AbsoluteDirection.UNE
				||
				dirn == AbsoluteDirection.USE
				||
				dirn == AbsoluteDirection.USW
				||
				dirn == AbsoluteDirection.UNW
			)
				steps[st][element.id()].addInDirection(AbsoluteDirection.Upward, step);
				
			if 
			(
				dirn == AbsoluteDirection.D
				||
				dirn == AbsoluteDirection.DN
				||
				dirn == AbsoluteDirection.DE
				||
				dirn == AbsoluteDirection.DS
				||
				dirn == AbsoluteDirection.DW
				||
				dirn == AbsoluteDirection.DNE
				||
				dirn == AbsoluteDirection.DSE
				||
				dirn == AbsoluteDirection.DSW
				||
				dirn == AbsoluteDirection.DNW
			)
				steps[st][element.id()].addInDirection(AbsoluteDirection.Downward, step);
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * @param step          The step.
	 * @param unit          The unit.
	 * @param intercardinal True if the direction has to be intercardinal.
	 * @return The absolute direction for an angle.
	 */
	public static AbsoluteDirection mapAngleToAbsoluteDirection
	(
		final Step step, final double unit, final boolean intercardinal
	)
	{
		final Point3D ptA = step.from().pt();
		final Point3D ptB = step.to().pt();
		
		int elevation = 0;
		if (ptB.z() - ptA.z() < -0.1 * unit)
			elevation = -1;  // B is below A
		else if (ptB.z() - ptA.z() > 0.1 * unit)
			elevation =  1;  // B is above A
		
		// Check 2D distance between points
		if (elevation != 0 && MathRoutines.distance(ptA.x(), ptA.y(), ptB.x(), ptB.y()) < 0.1 * unit)
		{
			// Step points are (approximately) on top of each other
			return (elevation < 0) ? AbsoluteDirection.D : AbsoluteDirection.U;
		}
		//double angle = step.theta();
		
		// Get step angle
		double angle = Math.atan2(ptB.y() - ptA.y(), ptB.x() - ptA.x());

		while (angle < 0)
			angle += 2 * Math.PI;
		while (angle > 2 * Math.PI)
			angle -= 2 * Math.PI;
		
		if (!intercardinal)
		{
			final double off = 2 * Math.PI / 16;
			
			if (elevation == 0)
			{
				// On the 2D plane
				if (angle < off)					return AbsoluteDirection.E;
				if (angle < off +  2 * Math.PI / 8)	return AbsoluteDirection.NE;
				if (angle < off +  4 * Math.PI / 8)	return AbsoluteDirection.N;
				if (angle < off +  6 * Math.PI / 8)	return AbsoluteDirection.NW;
				if (angle < off +  8 * Math.PI / 8)	return AbsoluteDirection.W;
				if (angle < off + 10 * Math.PI / 8)	return AbsoluteDirection.SW;
				if (angle < off + 12 * Math.PI / 8)	return AbsoluteDirection.S;
				if (angle < off + 14 * Math.PI / 8)	return AbsoluteDirection.SE;
				return AbsoluteDirection.E;				
			}
			else if (elevation < 0)
			{
				// Downwards angle
				if (angle < off)					return AbsoluteDirection.DE;
				if (angle < off +  2 * Math.PI / 8)	return AbsoluteDirection.DNE;
				if (angle < off +  4 * Math.PI / 8)	return AbsoluteDirection.DN;
				if (angle < off +  6 * Math.PI / 8)	return AbsoluteDirection.DNW;
				if (angle < off +  8 * Math.PI / 8)	return AbsoluteDirection.DW;
				if (angle < off + 10 * Math.PI / 8)	return AbsoluteDirection.DSW;
				if (angle < off + 12 * Math.PI / 8)	return AbsoluteDirection.DS;
				if (angle < off + 14 * Math.PI / 8)	return AbsoluteDirection.DSE;
				return AbsoluteDirection.DE;				
			}
			else
			{
				// Upwards angle
				if (angle < off)					return AbsoluteDirection.UE;
				if (angle < off +  2 * Math.PI / 8)	return AbsoluteDirection.UNE;
				if (angle < off +  4 * Math.PI / 8)	return AbsoluteDirection.UN;
				if (angle < off +  6 * Math.PI / 8)	return AbsoluteDirection.UNW;
				if (angle < off +  8 * Math.PI / 8)	return AbsoluteDirection.UW;
				if (angle < off + 10 * Math.PI / 8)	return AbsoluteDirection.USW;
				if (angle < off + 12 * Math.PI / 8)	return AbsoluteDirection.US;
				if (angle < off + 14 * Math.PI / 8)	return AbsoluteDirection.USE;
				return AbsoluteDirection.UE;				
			}
		}
		else
		{
			final double off = 2 * Math.PI / 32;
			
			if (elevation == 0)
			{
				// On the 2D plane
				if (angle < off)					 return AbsoluteDirection.E;
				if (angle < off +  2 * Math.PI / 16) return AbsoluteDirection.ENE;
				if (angle < off +  4 * Math.PI / 16) return AbsoluteDirection.NE;
				if (angle < off +  6 * Math.PI / 16) return AbsoluteDirection.NNE;
				if (angle < off +  8 * Math.PI / 16) return AbsoluteDirection.N;
				if (angle < off + 10 * Math.PI / 16) return AbsoluteDirection.NNW;
				if (angle < off + 12 * Math.PI / 16) return AbsoluteDirection.NW;
				if (angle < off + 14 * Math.PI / 16) return AbsoluteDirection.WNW;
				if (angle < off + 16 * Math.PI / 16) return AbsoluteDirection.W;
				if (angle < off + 18 * Math.PI / 16) return AbsoluteDirection.WSW;
				if (angle < off + 20 * Math.PI / 16) return AbsoluteDirection.SW;
				if (angle < off + 22 * Math.PI / 16) return AbsoluteDirection.SSW;
				if (angle < off + 24 * Math.PI / 16) return AbsoluteDirection.S;
				if (angle < off + 26 * Math.PI / 16) return AbsoluteDirection.SSE;
				if (angle < off + 28 * Math.PI / 16) return AbsoluteDirection.SE;
				if (angle < off + 30 * Math.PI / 16) return AbsoluteDirection.ESE;
				return AbsoluteDirection.E;
			}			
			else if (elevation < 0)
			{
				// Downwards
				if (angle < off)					 return AbsoluteDirection.DE;
				if (angle < off +  2 * Math.PI / 16) return AbsoluteDirection.DNE;
				if (angle < off +  4 * Math.PI / 16) return AbsoluteDirection.DNE;
				if (angle < off +  6 * Math.PI / 16) return AbsoluteDirection.DNE;
				if (angle < off +  8 * Math.PI / 16) return AbsoluteDirection.DN;
				if (angle < off + 10 * Math.PI / 16) return AbsoluteDirection.DNW;
				if (angle < off + 12 * Math.PI / 16) return AbsoluteDirection.DNW;
				if (angle < off + 14 * Math.PI / 16) return AbsoluteDirection.DNW;
				if (angle < off + 16 * Math.PI / 16) return AbsoluteDirection.DW;
				if (angle < off + 18 * Math.PI / 16) return AbsoluteDirection.DSW;
				if (angle < off + 20 * Math.PI / 16) return AbsoluteDirection.DSW;
				if (angle < off + 22 * Math.PI / 16) return AbsoluteDirection.DSW;
				if (angle < off + 24 * Math.PI / 16) return AbsoluteDirection.DS;
				if (angle < off + 26 * Math.PI / 16) return AbsoluteDirection.DSE;
				if (angle < off + 28 * Math.PI / 16) return AbsoluteDirection.DSE;
				if (angle < off + 30 * Math.PI / 16) return AbsoluteDirection.DSE;
				return AbsoluteDirection.DE;
			}			
			else 
			{
				// Upwards
				if (angle < off)					 return AbsoluteDirection.UE;
				if (angle < off +  2 * Math.PI / 16) return AbsoluteDirection.UNE;
				if (angle < off +  4 * Math.PI / 16) return AbsoluteDirection.UNE;
				if (angle < off +  6 * Math.PI / 16) return AbsoluteDirection.UNE;
				if (angle < off +  8 * Math.PI / 16) return AbsoluteDirection.UN;
				if (angle < off + 10 * Math.PI / 16) return AbsoluteDirection.UNW;
				if (angle < off + 12 * Math.PI / 16) return AbsoluteDirection.UNW;
				if (angle < off + 14 * Math.PI / 16) return AbsoluteDirection.UNW;
				if (angle < off + 16 * Math.PI / 16) return AbsoluteDirection.UW;
				if (angle < off + 18 * Math.PI / 16) return AbsoluteDirection.USW;
				if (angle < off + 20 * Math.PI / 16) return AbsoluteDirection.USW;
				if (angle < off + 22 * Math.PI / 16) return AbsoluteDirection.USW;
				if (angle < off + 24 * Math.PI / 16) return AbsoluteDirection.US;
				if (angle < off + 26 * Math.PI / 16) return AbsoluteDirection.USE;
				if (angle < off + 28 * Math.PI / 16) return AbsoluteDirection.USE;
				if (angle < off + 30 * Math.PI / 16) return AbsoluteDirection.USE;
				return AbsoluteDirection.UE;
			}			
		}
	}
	
	//-------------------------------------------------------------------------

	void setCircularDirections(final Graph graph)
	{
		final int vertexTypeId = SiteType.Vertex.ordinal();

		// Find all pivots
		final BitSet pivotIds = new BitSet();
		for (final Vertex vertex : graph.vertices())
			if (vertex.pivot() != null)
				pivotIds.set(vertex.pivot().id());
		
		// Store all steps from pivot vertices in 'out' direction
		for (int id = pivotIds.nextSetBit(0); id >= 0; id = pivotIds.nextSetBit(id + 1))
			for (final Step step : steps[vertexTypeId][id].steps())
			{
//				steps[vertexTypeId].get(id).inDirection(AbsoluteDirection.Out).add(step);
//				steps[vertexTypeId].get(id).inDirection(AbsoluteDirection.Rotational).add(step);
				steps[vertexTypeId][id].addInDirection(AbsoluteDirection.Out, step);
				steps[vertexTypeId][id].addInDirection(AbsoluteDirection.Rotational, step);
			}

		// Set circular directions for all other elements
		for (final SiteType siteType : SiteType.values())
		{
			final List<? extends GraphElement> elements = graph.elements(siteType);
			final int st = siteType.ordinal();
		
			for (int id = 0; id < elements.size(); id++)
			{
				if (siteType == SiteType.Vertex && pivotIds.get(id))
					continue;  // pivot vertices already done
				
				final GraphElement from = elements.get(id);
				
				final Vertex pivot = from.pivot();
				if (pivot == null)
					continue;  // not a pivoted element
				
				for (final Step step : steps[st][id].steps())
				{
					// Determine circular directions for this pivoted element
					final Vector vecAB = new Vector(from.pt(), step.to().pt());
					final Vector vecAP = new Vector(from.pt(), pivot.pt());

					vecAB.normalise();
					vecAP.normalise();
			
					if (step.directions().get(AbsoluteDirection.Diagonal.ordinal()))
						continue;
					
					final double dot = vecAB.dotProduct(vecAP);
					if (dot > 0.9)
					{
						// Facing in
//						steps[st].get(id).inDirection(AbsoluteDirection.In).add(step);
//						steps[st].get(id).inDirection(AbsoluteDirection.Rotational).add(step);
						steps[st][id].addInDirection(AbsoluteDirection.In, step);
						steps[st][id].addInDirection(AbsoluteDirection.Rotational, step);
					}
					else if (dot < -0.9)
					{
						// Facing out
//						steps[st].get(id).inDirection(AbsoluteDirection.Out).add(step);
//						steps[st].get(id).inDirection(AbsoluteDirection.Rotational).add(step);
						steps[st][id].addInDirection(AbsoluteDirection.Out, step);
						steps[st][id].addInDirection(AbsoluteDirection.Rotational, step);
					}
					else
					{
						final Edge curvedEdge = graph.findEdge(from.id(), step.to().id(), true);
						if (curvedEdge == null)
							continue;
						
						// Facing left or right
						if (MathRoutines.whichSide(from.pt2D(), pivot.pt2D(), step.to().pt2D()) > 0)
						{
//							steps[st].get(id).inDirection(AbsoluteDirection.CW).add(step);
//							steps[st].get(id).inDirection(AbsoluteDirection.Rotational).add(step);
							steps[st][id].addInDirection(AbsoluteDirection.CW, step);
							steps[st][id].addInDirection(AbsoluteDirection.Rotational, step);
						}
						else
						{
//							steps[st].get(id).inDirection(AbsoluteDirection.CCW).add(step);
//							steps[st].get(id).inDirection(AbsoluteDirection.Rotational).add(step);
							steps[st][id].addInDirection(AbsoluteDirection.CCW, step);
							steps[st][id].addInDirection(AbsoluteDirection.Rotational, step);
						}
					}
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------

	void generateRadials(final Graph graph)
	{
//		System.out.println("GENERATE");
		for (final SiteType siteType : SiteType.values())
		{
			
			final List<? extends GraphElement> elements = graph.elements(siteType);
			final int st = siteType.ordinal();
			
			for (int id = 0; id < elements.size(); id++)
			{
				//final GraphElement element = elements.get(id);
				
				radials[st][id] = new Radials(siteType, id);
						
				for (final AbsoluteDirection dirn : AbsoluteDirection.values())
				{
					for (final Step step : steps[st][id].inDirection(dirn))
					{
						if (step.to().siteType() != siteType)
							continue;  // don't follow radial to other site types
										
						// Create new radial in this direction
						final RadialWIP radial = new RadialWIP(step.from(), dirn);
						followRadial(graph, radial, siteType, dirn, step.to(), st, id);
						// radials[st][id].addSafe(radial.toRadial());
//						System.out.println("Added radial: " + radial.toRadial());
					}
				}
			}
		}
		
		
		// Remove opposite subsets
		for (final SiteType siteType : SiteType.values())
		{
			final List<? extends GraphElement> elements = graph.elements(siteType);
			final int st = siteType.ordinal();
			
			for (int id = 0; id < elements.size(); id++)
				for (final Radial radial : radials[st][id].radials())
					radial.removeOppositeSubsets();
		}
			
		// Reassign Rotational radials
		for (final SiteType siteType : SiteType.values())
		{
			final List<? extends GraphElement> elements = graph.elements(siteType);
			final int st = siteType.ordinal();
			
			for (int id = 0; id < elements.size(); id++)
			{
//				steps[st].get(id).inDirection(AbsoluteDirection.Rotational).clear();
				steps[st][id].clearInDirection(AbsoluteDirection.Rotational);
					
				for (final Radial radial : radials[st][id].inDirection(AbsoluteDirection.In))
//					radials[st].get(id).inDirection(AbsoluteDirection.Rotational).add(radial);
					radials[st][id].addInDirection(AbsoluteDirection.Rotational, radial);
					
				for (final Radial radial : radials[st][id].inDirection(AbsoluteDirection.Out))
//					radials[st].get(id).inDirection(AbsoluteDirection.Rotational).add(radial);
					radials[st][id].addInDirection(AbsoluteDirection.Rotational, radial);
				
				for (final Radial radial : radials[st][id].inDirection(AbsoluteDirection.CW))
//					radials[st].get(id).inDirection(AbsoluteDirection.Rotational).add(radial);
					radials[st][id].addInDirection(AbsoluteDirection.Rotational, radial);
					
				for (final Radial radial : radials[st][id].inDirection(AbsoluteDirection.CCW))
//					radials[st].get(id).inDirection(AbsoluteDirection.Rotational).add(radial);
					radials[st][id].addInDirection(AbsoluteDirection.Rotational, radial);
				
				radials[st][id].removeSubsetsInDirection(AbsoluteDirection.Rotational);
			}
		}
		
		// Determine distinct radials and sort them CW from N (for Dennis)
		for (int st = 0; st < SiteType.values().length; st++)
			for (final Radials radialSet : radials[st])
			{
				radialSet.setDistinct();
				radialSet.sort();
			}
	}

	void followRadial
	(
		final Graph  graph, final RadialWIP radial, final SiteType siteType, 
		final AbsoluteDirection dirn, final GraphElement current, int st, int id
	)
	{
		final double threshold = 0.25;  // allowable bend
		final double tanThreshold = Math.tan(threshold);
		
		final GraphElement previous = radial.lastStep();
		radial.addStep(current);
		
		// Find next step with closest trajectory
		//final Vector trajectory = new Vector(previous.pt(), current.pt());
		//trajectory.normalise();
		
		if (current.id() >= steps[siteType.ordinal()].length)
		{
			// Should not happen
			System.out.println("** Trajectories.followRadial(): " + siteType + " " + current.id() + " not in steps[][][] array.");
			return;
		}
		
		final List<Step> nextSteps = steps[siteType.ordinal()][current.id()].inDirection(dirn);
		
//		double bestScore = -100000;
		double bestAbsTanDiff = tanThreshold;	// initialising this to threshold allows us to always only compare to bestDiff
		GraphElement bestNextTo = null;
		ArrayList<GraphElement> bestsNextTo = new ArrayList<GraphElement>();
		GraphElement[] bestsNextToA = new GraphElement[0];
		
		final int dirnOrdinal = dirn.ordinal();
		
		if 
		(
			dirn == AbsoluteDirection.CW || dirn == AbsoluteDirection.CCW 
			|| 
			dirn == AbsoluteDirection.In || dirn == AbsoluteDirection.Out
//			|| 
//			dirn == AbsoluteDirection.Rotational
		)
		{
			// Follow circular step
			for (final Step next : nextSteps)
			{
				final GraphElement nextTo = next.to();
				
				if (nextTo.siteType() != siteType)
					continue;  // only follow radials along same site type
				
				if (next.directions().get(dirnOrdinal))
				{
					bestNextTo = nextTo;
					break;
				}
			}
			
			if (bestNextTo != null)
			{
				if (!radial.steps().contains(bestNextTo))
					followRadial(graph, radial, siteType, dirn, bestNextTo, st, id);
				else radials[st][id].addSafe(radial.toRadial());
					
			}
			else radials[st][id].addSafe(radial.toRadial());
				
		}
		else
		{
			// Non-circular steps
			for (final Step next : nextSteps)
			{
				
				
				final GraphElement nextTo = next.to();
				
				if (nextTo.siteType() != siteType)
					continue;  // only follow radials along same site type
				
				if (next.directions().get(dirnOrdinal))
				{
					// Follow step with smallest deviation in heading
					//
					// The angle difference computation looks like atan2(y, x) for y and x
					// computed based on the three points. Whenever x is negative, we can
					// exit early because the angle difference will be either greater than 0.5pi,
					// or smaller than -0.5pi; absolute value always bigger than our threshold.
					//
					// In the case where x is positive, we can compute the same angle difference
					// as atan(y / x), instead of atan2(y, x). The absolute value of atan(y / x)
					// is a strictly increasing function of the absolute value of (y / x), with
					// a minimum of atan(y / x) = 0 if (y / x) = 0.
					//
					// So, instead of minimising the absolute value of this angle difference, we
					// can also just minimise the absolute value of (y / x). The only complication
					// now is that we have to adjust the threshold that we compare to; originally,
					// we wanted a threshold absolute angle difference of at most 0.25rad. Now, 
					// we'll have to change that to a threshold of tan(0.25).
					
					final double absTanDiff = MathRoutines.absTanAngleDifference3D
									(
										previous.pt(), current.pt(), nextTo.pt()
									);
					
					
//					final double absTanDiff = MathRoutines.absTanAngleDifferencePosX
//							(
//								previous.pt2D(), current.pt2D(), nextTo.pt2D()
//							);					
					
					if (absTanDiff < bestAbsTanDiff)	// comparison to threshold is implicit due to init of bestAbsTanDiff
					{
						bestAbsTanDiff = absTanDiff;
						bestsNextTo = new ArrayList<GraphElement>();
						bestsNextTo.add(nextTo);
						if (bestAbsTanDiff == 0.0) {  //&& current.pt().z() == nextTo.pt().z()) {  -> to use for Sploof
							break;
						}
					}
					else if (absTanDiff == bestAbsTanDiff)
					{
						bestsNextTo.add(nextTo);
					}
				}
			}
			
			bestsNextToA = bestsNextTo.toArray(new GraphElement[0]);
			
			if (bestsNextToA.length != 0)
			{
//				if (bestNext.from.siteType() != siteType || bestNext.to.siteType() != siteType)
//					System.out.println("** Bad site type in followRadial().");
				for (final GraphElement nextTo : bestsNextToA)
				{
					final RadialWIP radialCopy = new RadialWIP(radial);
					if (!radial.steps().contains(nextTo))
						followRadial(graph, radialCopy, siteType, dirn, nextTo, st, id);
					else 
						radials[st][id].addSafe(radial.toRadial());
				}
			}
			else
				radials[st][id].addSafe(radial.toRadial());
			
		}
		
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Print the trajectories.
	 * 
	 * @param graph The graph.
	 */
	public void report(final Graph graph)
	{
		System.out.println(graph);
		
		System.out.println("\nRadials:");		
		for (final SiteType siteType : SiteType.values())
		{
			final List<? extends GraphElement> elements = graph.elements(siteType);
			final int st = siteType.ordinal();
			
			for (int id = 0; id < elements.size(); id++)
			{
				final GraphElement element = elements.get(id);
				
				System.out.println("\nSteps from " + element.label() + ":");
				for (final Step step : steps[st][id].steps())
					System.out.println(" " + step);
				
				//System.out.println("\nRadials from " + element.label() + ":");
				System.out.println("\n" + radials[st][id]);
			}
		}
		
		System.out.println("\nDirections used:");
		for (int d = totalDirections.nextSetBit(0); d >= 0; d = totalDirections.nextSetBit(d + 1))
			System.out.println("- " + AbsoluteDirection.values()[d]);
		System.out.println();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * A "work-in-progress" Radial, which we may still be busy building and adding steps to.
	 * Can be transformed into a real Radial with fixed-length array of steps when it is finished.
	 *
	 * @author Dennis Soemers
	 */
	private final static class RadialWIP
	{
		
		/** The list of steps making up this radial. */
		private final List<GraphElement> steps = new ArrayList<GraphElement>();
		
		/** 
		 * Direction of the first step in this radial. 
		 * This is useful to store so that radials can be quickly sorted by direction in Trajectories.
		 */
		private final AbsoluteDirection direction;

		/**
		 * Constructor.
		 * 
		 * @param start     The starting element of the radial.
		 * @param direction The absolute direction of the radial.
		 */
		public RadialWIP(final GraphElement start, final AbsoluteDirection direction)
		{
			this.direction = direction;
			steps.add(start);
		}
			
		/**
	     * Copy constructor.
	     * 
	     * @param other The RadialWIP object to copy.
	     */
	    public RadialWIP(final RadialWIP other)
	    {
	        this.direction = other.direction;

	        // Deep copy the steps list
	        for (GraphElement step : other.steps)
	        {
	            this.steps.add(step); // Assuming GraphElement is immutable or does not require a deep copy
	        }
	    }
		
		/**
		 * @return The list of the graph elements in the steps.
		 */
		public List<GraphElement> steps()
		{
			return steps;
		}
		
		/**
		 * Add a element to the steps.
		 * 
		 * @param to The element.
		 */
		public void addStep(final GraphElement to)
		{
			steps.add(to);
		}
		
		/**
		 * @return The last step.
		 */
		public GraphElement lastStep()
		{
			return steps.get(steps.size() - 1);
		}
		
		/**
		 * @return A proper Radial, with fixed-length array of steps
		 */
		public Radial toRadial()
		{
			return new Radial(steps.toArray(new GraphElement[steps.size()]), direction);
		}
		
	}
	
	//-------------------------------------------------------------------------
	
}
