package game.util.graph;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.types.board.SiteType;
import main.Constants;
import main.math.MathRoutines;
import main.math.RCL;
import main.math.RCLType;

//-----------------------------------------------------------------------------

/**
 * Measure graph properties. This helper class is not meant to be instantiated.
 * 
 * @author cambolbro
 */
public abstract class MeasureGraph
{	
	//-------------------------------------------------------------------------
	
	/**
	 * @param graph     The graph to measure.
	 * @param boardless True if the graph is boardless.
	 */
	public static void measure(final Graph graph, final boolean boardless)
	{
		graph.clearProperties();
		
		if (!boardless)
		{
			measurePivot(graph);
			measurePerimeter(graph);
			measureInnerOuter(graph);
			measureExtremes(graph);
			measureMajorMinor(graph);
			measureCorners(graph);
			measureSides(graph);
			measurePhase(graph);
			measureEdgeOrientation(graph);
			measureSituation(graph);
		}

		measureCentre(graph);
	
		//System.out.println(graph);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param graph The graph to measure.
	 */
	public static void measurePivot(final Graph graph)
	{
		for (final Vertex vertex : graph.vertices())
			if (vertex.pivot() != null)
				vertex.pivot().properties().add(Properties.PIVOT);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Sets elements on the graph perimeter.
	 * 
	 * Generate the perimeter from the vertices, not the edges. If the graph was
	 * generated in "use:Vertex" mode it will not have any faces, so edge analysis
	 * is misleading. It's safer to use vertices in all cases.
	 * 
	 * @param graph The graph to measure.
	 */
	public static void measurePerimeter(final Graph graph)
	{
//		graph.perimeters().clear();
		graph.clearPerimeters();
		
		final BitSet covered = new BitSet();
		
		// Generate the candidate polygons. 
		// There should be one for each connected component of graph.
		while (true)
		{
			final Perimeter perimeter = createNextPerimeter(graph, covered);
			if (perimeter == null)
				break;  // no more perimeters
//			graph.perimeters().add(perimeter);
			graph.addPerimeter(perimeter);
		}
		
		// Remove candidates contained in other candidates		
		for (int pa = graph.perimeters().size() - 1; pa >= 0; pa--)
		{
			final Point2D.Double ptA = (Point2D.Double)graph.perimeters().get(pa).startPoint();
			
			boolean isInside = false;
			for (int pb = 0; pb < graph.perimeters().size(); pb++)
			{
				if (pa == pb)
					continue;
				
				if (MathRoutines.pointInPolygon(ptA, graph.perimeters().get(pb).positions()))
				{
					isInside = true;
					break;
				}
			}
			
			if (isInside)
//				graph.perimeters().remove(pa);  // perimeter A is inside perimeter B
				graph.removePerimeter(pa);  // perimeter A is inside perimeter B
		}
		
		// Set properties of perimeter elements
		for (final Perimeter perimeter : graph.perimeters())
		{
			final int numVerts = perimeter.elements().size();
			for (int n = 0; n < numVerts; n++)
			{
				final Vertex vertexA = (Vertex)perimeter.elements().get(n);
				final Vertex vertexB = (Vertex)perimeter.elements().get((n + 1) % numVerts);
			
				vertexA.properties().set(Properties.PERIMETER);
				vertexB.properties().set(Properties.PERIMETER);  // redundant, but just to be safe...
				
				final Edge edge = vertexA.incidentEdge(vertexB);
				if (edge == null)
					continue;
				
				edge.properties().set(Properties.PERIMETER);
				
				if (edge.left() != null)
					edge.left().properties().set(Properties.PERIMETER);
				else if (edge.right() != null)
					edge.right().properties().set(Properties.PERIMETER);
			}
		}
		
//		System.out.println("\nPerimeter report: " + perimeters.size() + " perimeters.");
//		for (int n = 0; n < perimeters.size(); n++) 
//		{
//			final Perimeter perimeter = perimeters.get(n);
//			System.out.print("- " + n + " perimeter:");
//			for (final GraphElement ge : perimeter.elements())
//				System.out.print(" " + ge.id());
//			System.out.print("\n- " + n + " inside   :");
//			for (final GraphElement ge : perimeter.inside())
//				System.out.print(" " + ge.id());
//			System.out.println();
//		}
//		final int[] counts = new int[3];
//		for (int st = 0; st < siteTypes.length; st++)
//			for (final GraphElement ge : graphElementList(graph, siteTypes[st]))
//				if (ge.properties().get(Properties.PERIMETER))
//					counts[st]++;
//		System.out.println("Perimeter counts: vertices=" + counts[0] + ", edges=" + counts[1] + ", faces=" + counts[2] + ".");
	}
	
	/**
	 * @return The perimeter for the next unvisited connected component, else null if no more connected components.
	 */
	private static Perimeter createNextPerimeter(final Graph graph, final BitSet covered)
	{
		// Find leftmost unused vertex; it must be on the perimeter of its connected component 
		Vertex start = null;
		double minX = 1000000;
		
		for (final Vertex vertex : graph.vertices())
		{
			if (!covered.get(vertex.id()) && vertex.pt().x() < minX)
			{
				start = vertex;
				minX = vertex.pt().x();
			}
		}
		
		if (start == null)
			return null;  // no more perimeters to find
		
		// Create polygon from leftmost vertex
		final Perimeter perimeter = followPerimeterClockwise(start);
		if (perimeter == null)
			return null;
		
		// Mark polygon vertices as covered
		for (final GraphElement ge : perimeter.elements())
			covered.set(ge.id(), true);
		
		// Make a copy of this polygon converted to Point2D
		final List<Point2D> polygon2D = perimeter.positions();
		
		// Mark vertices contained by this polygon as covered
		for (final Vertex vertex : graph.vertices())
			if (!covered.get(vertex.id()) && MathRoutines.pointInPolygon(vertex.pt2D(), polygon2D))
			{
				if (!perimeter.on().get(vertex.id()))
					perimeter.addInside(vertex);
				covered.set(vertex.id(), true);
			}
		
		return perimeter;
	}

	/**
	 * @param from Leftmost unvisited vertex, guaranteed to be on the perimeter.
	 * @return Polygon formed by following this perimeter.
	 */
	private static Perimeter followPerimeterClockwise(final Vertex from)
	{
		final Perimeter perimeter = new Perimeter();
		
		if (from.edges().isEmpty())
		{
			// Must be single disconnected vertex
			perimeter.add(from);
			return perimeter;
		}
			
		// Find vertex edge closest to 90 degrees (straight up)
		Edge   bestEdge  = null;
		double bestAngle = 1000000;

		for (final Edge edge : from.edges())
		{
			final Vertex to = edge.otherVertex(from);
			final double dx = to.pt().x() - from.pt().x();
			final double dy = to.pt().y() - from.pt().y();

			final double angle = Math.atan2(dx, -dy);  // minimises in up direction

			// Check dot product
			if (angle < bestAngle)
			{
				bestEdge = edge;
				bestAngle = angle;
			}
		}
		
		// Follow this vertex around this perimeter
		Vertex vertex = from;
		Vertex prev   = from;
		Edge   edge   = bestEdge;	
		
		while (true) 
		{
			perimeter.add(vertex);
			
			// Step to next vertex
			prev = vertex;
			vertex = edge.otherVertex(vertex);
			
			final int i = vertex.edgePosition(edge);
			//edge = vertex.edges().get((i + 1) % vertex.edges().size());
				
			for (int n = 1; n < vertex.edges().size(); n++)
			{
				edge = vertex.edges().get((i + n) % vertex.edges().size());
				if (edge.otherVertex(vertex).id() != prev.id())
					break;  // avoid stepping back to previous vertex, in case of doubled edges
			}
			
			if (vertex.id() == from.id())
				break;  // closed loop completed
		}
		
		return perimeter;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Determines inner and outer graph elements.
	 * 
	 * @param graph The graph to measure.
	 */
	public static void measureInnerOuter(final Graph graph)
	{
		// Set all perimeter elements to outer
		for (int st = 0; st < Graph.siteTypes.length; st++)
			for (final GraphElement ge : graph.elements(Graph.siteTypes[st]))
				if (ge.properties().get(Properties.PERIMETER))
					ge.properties().set(Properties.OUTER);		

		// Set faces with a perimeter vertex to outer
		for (final Face face : graph.faces())
			for (final Vertex vertex : face.vertices())
				if (vertex.properties().get(Properties.PERIMETER))
				{
					face.properties().set(Properties.OUTER);
					face.properties().set(Properties.PERIMETER);
				}
					
		// Set edges with a perimeter vertex to outer
		for (final Edge edge : graph.edges())
			if 
			(
				edge.vertexA().properties().get(Properties.PERIMETER)
				&&
				edge.vertexB().properties().get(Properties.PERIMETER)
				&& 
				(edge.left() == null || edge.right() == null) 
			)
			{
				edge.properties().set(Properties.OUTER);
				edge.properties().set(Properties.PERIMETER);
			}
		
		// Set faces with a null neighbour
		for (final Face face : graph.faces())
			for (final Edge edge : face.edges())
				if (edge.left() == null || edge.right() == null)
					face.properties().set(Properties.NULL_NBOR);
		
		// Set non-outer items to inner
		for (int st = 0; st < Graph.siteTypes.length; st++)
			for (final GraphElement ge : graph.elements(Graph.siteTypes[st]))
				if (!ge.properties().get(Properties.OUTER))
					ge.properties().set(Properties.INNER);		
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Determines the left/right/top/bottom graph elements.
	 * 
	 * @param graph The graph to measure.
	 */
	public static void measureExtremes(final Graph graph)
	{
		final double tolerance = 0.01;

		for (int st = 0; st < Graph.siteTypes.length; st++)
		{
			double minX =  1000000;
			double minY =  1000000;
			double maxX = -1000000;
			double maxY = -1000000;

			for (final GraphElement ge : graph.elements(Graph.siteTypes[st]))
			{
				if (ge.pt().x() < minX)
					minX = ge.pt().x();
				if (ge.pt().y() < minY)
					minY = ge.pt().y();
				if (ge.pt().x() > maxX)
					maxX = ge.pt().x();
				if (ge.pt().y() > maxY)
					maxY = ge.pt().y();
			}
			
			for (final GraphElement ge : graph.elements(Graph.siteTypes[st]))
			{
				if (ge.pt().x() - minX < tolerance)
					ge.properties().set(Properties.LEFT);
				if (ge.pt().y() - minY < tolerance)
					ge.properties().set(Properties.BOTTOM);
				if (maxX - ge.pt().x() < tolerance)
					ge.properties().set(Properties.RIGHT);
				if (maxY - ge.pt().y() < tolerance)
					ge.properties().set(Properties.TOP);
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Determines which cells are major and which are minor.
	 * 
	 * @param graph The graph to measure.
	 */
	public static void measureMajorMinor(final Graph graph)
	{
		int maxSides = 0;
		
		for (final Face face : graph.faces())
			if (face.vertices().size() > maxSides)
				maxSides = face.vertices().size();
		
		for (final Face face : graph.faces())
			if (face.vertices().size() == maxSides)
				face.properties().set(Properties.MAJOR);
			else
				face.properties().set(Properties.MINOR);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Determines outer graph elements that are corners.
	 * 
	 * Assumes that measureExtremes() has already been called.
	 * 
	 * Assumes only one perimeter per graph.
	 * 
	 * @param graph The graph to measure.
	 */
	public static void measureCorners(final Graph graph)
	{
		cornersFromPerimeters(graph);
		
		for (final Vertex vertex : graph.vertices())
		{
			if (!vertex.properties().get(Properties.CORNER))
				continue;
			
//				System.out.print("Vertex " + vertex.id() + " has edges:");
//				for (final Edge edge : vertex.edges())
//					System.out.print(" " + edge.id());
//				System.out.println();
				
			final boolean isConcave = vertex.properties().get(Properties.CORNER_CONCAVE);
					
			// Check for incident edge with other endpoint on perimeter
			boolean singleEdge = false;
			for (final Edge edge : vertex.edges())
			{
				final Vertex other = edge.otherVertex(vertex);
				if (other.properties().get(Properties.CORNER))
				{
					// Both endpoints of this edge are corners
					edge.properties().set(Properties.CORNER);
					
					if (isConcave)
						edge.properties().set(Properties.CORNER_CONCAVE);
					else
						edge.properties().set(Properties.CORNER_CONVEX);
					
					final Face faceL = edge.left();
					final Face faceR = edge.right();
					
					if (faceL != null)
					{
						faceL.properties().set(Properties.CORNER);
						
						if (isConcave)
							faceL.properties().set(Properties.CORNER_CONCAVE);
						else
							faceL.properties().set(Properties.CORNER_CONVEX);
					}
					else if (faceR != null)
					{
						faceR.properties().set(Properties.CORNER);
						
						if (isConcave)
							faceR.properties().set(Properties.CORNER_CONCAVE);
						else
							faceR.properties().set(Properties.CORNER_CONVEX);
					}

					singleEdge = true;
					
					// Don't break! e.g. single square with 4 vertices will not pick up all vertices
					//break;
				}
			}
			
			if (singleEdge)
				continue;
			
			// Vertex is a corner on its own.
			// Note that there may be more than two edges, e.g. Alquerque.
			for (final Edge edge : vertex.edges())
			{
				if (!edge.properties().get(Properties.PERIMETER))
					continue;

				edge.properties().set(Properties.CORNER);

				if (isConcave)
					edge.properties().set(Properties.CORNER_CONCAVE);
				else
					edge.properties().set(Properties.CORNER_CONVEX);

				if (isConcave)
				{
					for (final Face face : vertex.faces())
						if (numPerimeterVertices(face) == 1)
						{
							face.properties().set(Properties.CORNER);
							if (isConcave)
								face.properties().set(Properties.CORNER_CONCAVE);
							else
								face.properties().set(Properties.CORNER_CONVEX);
						}
				}
				else
				{
					final Face faceL = edge.left();
					final Face faceR = edge.right();
					
					if (faceL != null)
					{
						faceL.properties().set(Properties.CORNER);
						faceL.properties().set(Properties.CORNER_CONVEX);
					}
					else if (faceR != null)
					{
						faceR.properties().set(Properties.CORNER);
						faceR.properties().set(Properties.CORNER_CONVEX);
					}
				}
			}
		}
		
//		// Debug report
//		final int[] counts = new int[3];
//		for (final Vertex vertex : graph.vertices())
//			if (vertex.properties().get(Properties.CORNER))
//				counts[0]++;
//		
//		for (final Edge edge : graph.edges())
//			if (edge.properties().get(Properties.CORNER))
//				counts[1]++;
//		
//		for (final Face face : graph.faces())
//			if (face.properties().get(Properties.CORNER))
//				counts[2]++;
//		
//		System.out.println("Corner counts: vertices=" + counts[0] + 
//							", edges=" + counts[1] + ", faces=" + counts[2] + ".");	
		
//		// Debug report
//		System.out.print("Vertex corners:");
//		for (final Vertex vertex : graph.vertices())
//			if (vertex.properties().get(Properties.CORNER))
//					System.out.print(" " + vertex.id());
//		System.out.println();
//		
//		System.out.print("Edge corners:");
//		for (final Edge edge : graph.edges())
//			if (edge.properties().get(Properties.CORNER))
//					System.out.print(" " + edge.id());
//		System.out.println();
//		
//		System.out.print("Face corners:");
//		for (final Face face : graph.faces())
//			if (face.properties().get(Properties.CORNER))
//				System.out.print(" " + face.id());
//		System.out.println();
	}
		
	static int numPerimeterVertices(final Face face)
	{
		int num = 0;
		for (final Vertex vertex : face.vertices())
			if (vertex.properties().get(Properties.PERIMETER))
				num++;
		return num;
	}
	
	/**
	 * Measures corners for the specified graph element type. 
	 */
	static void cornersFromPerimeters(final Graph graph)
	{
		for (final Perimeter perimeter : graph.perimeters())
			cornersFromPerimeter(graph, perimeter);
	}
	
	/**
	 * Measures corners for the specified graph element type. 
	 */
	static void cornersFromPerimeter(final Graph graph, final Perimeter perimeter)
	{
		final double tolerance = 0.001;
	
		if (perimeter.elements().size() < 6)
		{
			// Simple convex polygon
			for (final GraphElement ge : perimeter.elements())
			{
				ge.properties().set(Properties.CORNER);
				ge.properties().set(Properties.CORNER_CONVEX);
			}
			return;
		}
		
		final int num = perimeter.elements().size();
		
		final List<Point2D> polygon2D = perimeter.positions();
		//boolean isClockwise = MathRoutines.clockwise(polygon2D);
		
//		System.out.print("\nPerimeter:");
//		for (final GraphElement ge : perimeter.elements())
//			System.out.print(" " + ge.id());
//		System.out.println();
		
		// Calculate average determinant at each perimeter site
		final double[] scores = new double[num];
		
		final int numK = 4; 
		
		for (int n = 0; n < num; n++)
		{
			final Point2D.Double pt = (Point2D.Double)polygon2D.get(n);
		
  			// Accumulated line segment error approach
  			double score = 0;
  			for (int k = 1; k < numK; k++)
  			{
  				final Point2D.Double ptA = (Point2D.Double)polygon2D.get((n - k + num) % num);
 				final Point2D.Double ptB = (Point2D.Double)polygon2D.get((n + k) % num);
			
 				double dist = MathRoutines.distanceToLine(pt, ptA, ptB);
				
 				if (MathRoutines.clockwise(ptA, pt, ptB))
  					dist = -dist;
  				
  				score += dist / k;  // / k;  //(k * k);
  			}
  			//score /= numK;
   			scores[n] = score; 			
		}
	
		// Smooth the scores
		final int numSmoothingPasses = 1;
 		final double[] temp = new double[num];
 		
  		for (int pass = 0; pass < numSmoothingPasses; pass++)
 		{
 			for (int n = 0; n < num; n++)
 				temp[n] = (4 * scores[n] + scores[(n + 1) % num]  + scores[(n - 1 + num) % num]) / 6;
 			for (int n = 0; n < num; n++)
 				scores[n] = temp[n];
 		}

//		System.out.println("Smoothed:");
//		for (int n = 0; n < num; n++)
//			System.out.println("id=" + perimeter.elements().get(n).id() + ", scores=" + scores[n] + ".");
			
		// Detect convex corners
		final BitSet keep = new BitSet();
		keep.set(0, num, true);
		
		for (int n = 0; n < num; n++)
			if 
			(
				scores[n] < 0.320  // this value catches bad corners in rectangular shapes on hex 
				||
				scores[n] < scores[(n - 1 + num) % num] - tolerance 
				|| 
				scores[n] < scores[(n + 1) % num] - tolerance
			)
				keep.set(n, false);
		
		// Keep similar nbors
		final double similar = 0.95;
		for (int n = keep.nextSetBit(0); n >= 0; n = keep.nextSetBit(n + 1))
		{
			if (scores[(n - 1 + num) % num] >= similar * scores[n])
				keep.set((n - 1 + num) % num, true);
			if (scores[(n + 1) % num] >= similar * scores[n])
				keep.set((n + 1) % num, true);
		}
		
		for (int n = keep.nextSetBit(0); n >= 0; n = keep.nextSetBit(n + 1))
		{
			perimeter.elements().get(n).properties().set(Properties.CORNER);
			perimeter.elements().get(n).properties().set(Properties.CORNER_CONVEX);
		}
			
		// Detect concave corners
		keep.set(0, num, true);
		
//		for (int n = 0; n < num; n++)
//			if (scores[n] > 0)  //-0.320  
//			{
//				keep.set(n, false);
//			}
//			else if 
//			(	
//				scores[n] > 0
//				||
//				scores[n] > scores[(n - 1 + num) % num] + tolerance 
//				|| 
//				scores[n] > scores[(n + 1) % num] + tolerance
//			)
//			{
//				// Is a candidate for removal
//				final double diffA = Math.abs(scores[n] - scores[(n - 1 + num) % num]);
//				final double diffB = Math.abs(scores[n] - scores[(n + 1) % num]);
//		
////				if (Math.min(diffA, diffB) > 0.1)
//					keep.set(n, false);
//			}
		
			for (int n = 0; n < num; n++)
			if 
			(
				scores[n] > -0.25  // 0.320  // this value catches bad corners in rectangular shapes on hex 
				||
				scores[n] > scores[(n - 1 + num) % num] + tolerance 
				|| 
				scores[n] > scores[(n + 1) % num] + tolerance
			)
				keep.set(n, false);

		
		for (int n = keep.nextSetBit(0); n >= 0; n = keep.nextSetBit(n + 1))
		{
			perimeter.elements().get(n).properties().set(Properties.CORNER);
			perimeter.elements().get(n).properties().set(Properties.CORNER_CONCAVE);
		}
			
//		System.out.print("Corners:");
//		int count = 0;
//		for (final GraphElement ge : graph.vertices())
//			if (ge.properties().get(Properties.CORNER))
//			{
//				System.out.print(" " + ge.id());
//				count++;
//			}
//		System.out.println("\n" + count + " corners found.");
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Determines which board side perimeter graph elements are on.
	 * 
	 * Must be done *after* corners have been determined.
	 * 
	 * @param graph The graph to measure.
	 */
	public static void measureSides(final Graph graph)
	{
		final Point2D mid = graph.centroid();
		
		for (final Perimeter perimeter : graph.perimeters())
			findSides(graph, mid, perimeter);
		
		// Determine edge and cell sides based on perimeter vertices
		final long[] sides = 
		{ 
			Properties.SIDE_N,  Properties.SIDE_E,  Properties.SIDE_S,  Properties.SIDE_W, 
			Properties.SIDE_NE, Properties.SIDE_SE, Properties.SIDE_SW, Properties.SIDE_NW 
		};
		
		for (final Vertex vertex : graph.vertices())
		{
			// Every perimeter vertex should be assigned a side by now
			if (!vertex.properties().get(Properties.CORNER))
				continue;

			for (int side = 0; side < sides.length; side++)
			{
				final long sideCode = sides[side];
				
				if (!vertex.properties().get(sideCode))
					continue;  // not this side
				
				// Edges: both end points must be on this side
				for (final Edge edge : vertex.edges())
				{
					final Vertex other = edge.otherVertex(vertex);
					if (other.properties().get(sideCode))
						edge.properties().set(sideCode);
				}
				
				// Cells: any face with this vertex is on this side
				for (final Face face : vertex.faces())
					face.properties().set(sideCode);
			}
		}
		
		// Ensure that edges and faces inherit the side(s) their vertices are on 
		final long sidesMask = 
				Properties.SIDE_N | Properties.SIDE_E | Properties.SIDE_S | Properties.SIDE_W 
				| 
				Properties.SIDE_NE | Properties.SIDE_SE | Properties.SIDE_SW | Properties.SIDE_NW; 

		for (final Vertex vertex : graph.vertices())
		{
			for (final Edge edge : vertex.edges())
				edge.properties().add(vertex.properties().get() & sidesMask);

			for (final Face face : vertex.faces())
				face.properties().add(vertex.properties().get() & sidesMask);
		}
	}
	
	static void findSides(final Graph graph, final Point2D mid, final Perimeter perimeter)
	{
		// Find each side run
		final int num = perimeter.elements().size();
		for (int from = 0; from < num; from++)
		{
			final GraphElement geFrom = perimeter.elements().get(from);
			if (geFrom.properties().get(Properties.CORNER))
			{
				// Handle this side run
				int to = from;
				GraphElement geTo;
				do
				{
					geTo = perimeter.elements().get(++to % num);
				} while (!geTo.properties().get(Properties.CORNER));
				
				final int runLength = to - from;
				
				sideFromRun(graph, perimeter, mid, from, runLength);
			}
		}
	}
	
	static void sideFromRun
	(
		final Graph graph, final Perimeter perimeter, final Point2D mid, 
		final int from, final int runLength
	)
	{
		final int numDirections = 16;
			
		final int num = perimeter.elements().size();
		final GraphElement geFrom = perimeter.elements().get(from);
				
		// Locate average position of elements along run
		double avgX = geFrom.pt().x();
		double avgY = geFrom.pt().y();				
		
		GraphElement ge = null;
		for (int r = 0; r < runLength; r++)
		{
			ge = perimeter.elements().get((from + 1 + r) % num);
		
			avgX += ge.pt().x();
			avgY += ge.pt().y();
		}
		avgX /= (runLength + 1);
		avgY /= (runLength + 1);
		
		final int dirn = discreteDirection(mid, new Point2D.Double(avgX, avgY), numDirections);
		
		long property = 0;
		
		if (dirn == 0)
			property = Properties.SIDE_E;
		else if (dirn == numDirections / 4)
			property = Properties.SIDE_N;
		else if (dirn == numDirections / 2)
				property = Properties.SIDE_W;
		else if (dirn == 3 * numDirections / 4)
			property = Properties.SIDE_S;
		else if (dirn > 0 && dirn < numDirections / 4)
			property = Properties.SIDE_NE;
		else if (dirn > dirn / 4 && dirn < numDirections / 2)
			property = Properties.SIDE_NW;
		else if (dirn > dirn / 2 && dirn < 3 * numDirections / 4)
			property = Properties.SIDE_SW;
		else 
			property = Properties.SIDE_SE;
		
		// Store result in run elements
		for (int r = 0; r < runLength + 1; r++)
		{
			ge = perimeter.elements().get((from + r) % num);
			ge.properties().set(property);
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param angleIn       Input angle.
	 * @param numDirections Number of direction increments.
	 * @return Index of the arc segment in which this direction lies.
	 */
	public static int discreteDirection(final double angleIn, final int numDirections)
	{
		final double arc = 2 * Math.PI / numDirections;
		final double off = arc / 2;

		double angle = angleIn;
		while (angle < 0)
			angle += 2 * Math.PI;
		while (angle > 2 * Math.PI)
			angle -= 2 * Math.PI;
		
		return ((int)((angle + off) / arc) + numDirections) % numDirections;
	}

	/**
	 * @param ptA           First end point of vector.
	 * @param ptB           Second end point of vector.
	 * @param numDirections Number of the direction increments.
	 * @return Index of the arc segment in which this direction lies.
	 */
	public static int discreteDirection(final Point2D ptA, final Point2D ptB, final int numDirections)
	{
		final double angle = Math.atan2(ptB.getY() - ptA.getY(), ptB.getX() - ptA.getX());
		return discreteDirection(angle, numDirections);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Determines which graph element(s) are at the centre.
	 * 
	 * Assumes that perimeter elements have already been set.
	 * 
	 * @param graph The graph to measure.
	 */
	public static void measureCentre(final Graph graph)
	{
		final Rectangle2D bounds = Graph.bounds(graph.elements(SiteType.Vertex));
		final Point2D mid = new Point2D.Double
								(
									bounds.getX() + bounds.getWidth()  / 2.0,
									bounds.getY() + bounds.getHeight() / 2.0
								);
		for (int st = 0; st < Graph.siteTypes.length; st++)
			measureGeometricCentre(graph, mid, Graph.siteTypes[st]);
	}

	/**
	 * Measures total distance squared to perimeter and corner elements.
	 * 
	 * Could be sped up by only taking a sample of perimeter elements, but this could lead to biases.
	 * 
	 * Combine distance to: 1. perimeter, and 2. perimeter corners, for edge cases like single column of cells.
	 */
	private static void measureGeometricCentre(final Graph graph, final Point2D mid, final SiteType type)
	{
		final double tolerance = 0.0001;	
	
		final List<? extends GraphElement> list = graph.elements(type);
	
		final List<GraphElement> perimeterGE = new ArrayList<GraphElement>();
		
		// If board is large, do not include perimeter, just use the corners
		if (list.size() < 100)
			for (final GraphElement ge : list)
				if (ge.properties().get(Properties.PERIMETER))
					perimeterGE.add(ge);

		final List<GraphElement> cornersGE = new ArrayList<GraphElement>();
		for (final GraphElement ge : list)
			if (ge.properties().get(Properties.CORNER))
				cornersGE.add(ge);

		final double[] distances = new double[list.size()];
		
		for (final GraphElement geA : list)
		{
			double acc = 0;
			for (final GraphElement geB : perimeterGE)
				acc += MathRoutines.distanceSquared(geA.pt2D(), geB.pt2D());
			for (final GraphElement geB : cornersGE)
				acc += MathRoutines.distanceSquared(geA.pt2D(), geB.pt2D());
			distances[geA.id()] = acc;
		}
		
		double minDistance = 1000000;
		for (int n = 0; n < distances.length; n++)
			if (distances[n] < minDistance)
				minDistance = distances[n];
		
		for (int n = 0; n < distances.length; n++)
			if (Math.abs(distances[n] - minDistance) < tolerance)
				list.get(n).properties().set(Properties.CENTRE);
	}
	//-------------------------------------------------------------------------
		
	/**
	 * Determines the phase of each cell (no two adjacent cells have the same
	 * phase).
	 * 
	 * @param graph The graph to measure.
	 */
	public static void measurePhase(final Graph graph)
	{
		for (int st = 0; st < Graph.siteTypes.length; st++)
			measurePhase(graph, Graph.siteTypes[st]);
	}
	
	/**
	 * Determines the phase of each cell (no two adjacent cells have the same
	 * phase).
	 * 
	 * @param graph The graph to measure.
	 * @param type  The graph element type.
	 */
	public static void measurePhase(final Graph graph, final SiteType type)
	{
		final List<? extends GraphElement> elements = graph.elements(type);
		if (elements.isEmpty())
			return;
		
//		for (final Face face : graph.faces())
//			System.out.println("Face " + face.id() + " momentum is " + face.momentum() + ".");
			
		while (true)
		{
			// Find the starting element, if any
			GraphElement start = null;
			for (final GraphElement ge : elements)
				if (ge.properties().phase() == Constants.UNDEFINED)
				{
					start = ge;
					break;
				}
			if (start == null)
				break;  // no more elements to set
			
			// We need efficient adding and removing to/from front, and efficient adding to back.
			// So a Deque is perfect for this
			final Deque<GraphElement> queue = new ArrayDeque<GraphElement>();
			final BitSet visited = new BitSet();
			
			// Add the first face
			start.properties().set(Properties.PHASE_0);
			queue.add(start);
			
			// Spread phase over remaining faces
			while (!queue.isEmpty())
			{
				final GraphElement ge = queue.removeFirst();
				
				if (visited.get(ge.id()))
					continue;
					
				// Gather known phases of all nbors 
				final List<GraphElement> nbors = ge.nbors();
				final BitSet nborPhases = nborPhases(nbors);
			
				int phase;
				for (phase = 0; phase < 4; phase++)
					if (!nborPhases.get(phase))
						break;  // this phase is not in any nbor
				
				// Set this element's phase
				if (phase == 0)
					ge.properties().set(Properties.PHASE_0);
				else if (phase == 1)
					ge.properties().set(Properties.PHASE_1);
				else if (phase == 2)
					ge.properties().set(Properties.PHASE_2);
				else if (phase == 3)
					ge.properties().set(Properties.PHASE_3);
				else if (phase == 4)
					ge.properties().set(Properties.PHASE_4);
				else if (phase == 5)
					ge.properties().set(Properties.PHASE_5);
		
				visited.set(ge.id(), true);
				
				// Visit each nbor, prioritising more constrained ones
				for (final GraphElement nbor : nbors)
				{			
					if (visited.get(nbor.id()))
						continue;
				
					final List<GraphElement> nborNbors = nbor.nbors();
					
					final BitSet nborNborPhases = nborPhases(nborNbors);
					final int numNborNborPhases = nborNborPhases.cardinality();
					if (numNborNborPhases > 1)
						queue.addFirst(nbor);	// more than one phase to match, higher priority
					else
						queue.addLast(nbor);
				}
			}
		}
	}
	
	static BitSet nborPhases(final List<GraphElement> nbors)
	{
		final BitSet phases = new BitSet();

		for (final GraphElement nbor : nbors)
		{			
			if (nbor.properties().get(Properties.PHASE_0))
				phases.set(0, true);
			else if (nbor.properties().get(Properties.PHASE_1))
				phases.set(1, true);
			else if (nbor.properties().get(Properties.PHASE_2))
				phases.set(2, true);
			else if (nbor.properties().get(Properties.PHASE_3))
				phases.set(3, true);
			else if (nbor.properties().get(Properties.PHASE_4))
				phases.set(4, true);
			else if (nbor.properties().get(Properties.PHASE_5))
				phases.set(5, true);
		}

		return phases;
	}
	

	//-------------------------------------------------------------------------
	
	/**
	 * Determines the orientation of each edge (axial, angle, horz/vert, etc).
	 * 
	 * @param graph The graph to measure.
	 */
	public static void measureEdgeOrientation(final Graph graph)
	{
		final int numDirections = 16;
		
		for (final Edge edge : graph.edges())
		{
			final Vertex va = edge.vertexA();
			final Vertex vb = edge.vertexB();

			final int direction = discreteDirection(va.pt2D(), vb.pt2D(), numDirections);
			
			if (direction == 0 || direction == numDirections / 2)
			{
				edge.properties().set(Properties.AXIAL);
				edge.properties().set(Properties.HORIZONTAL);
			}
			else if (direction == numDirections / 4 || direction == 3 * numDirections / 4)
			{
				edge.properties().set(Properties.AXIAL);
				edge.properties().set(Properties.VERTICAL);
			}
			else if 
			(
				direction > 0 && direction < numDirections / 4 
				|| 
				direction > numDirections / 2 && direction < 3 * numDirections / 4
			)
			{
				edge.properties().set(Properties.ANGLED);
				edge.properties().set(Properties.SLASH);
			}
			else
			{
				edge.properties().set(Properties.ANGLED);
				edge.properties().set(Properties.SLOSH);
			}
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Determines the coordinate label and row/column/level of each graph element. 
	 * 
	 * Note: Assumes that elements will not have negative z values.
	 * 
	 * @param graph The graph to measure.
	 */
	public static void measureSituation(final Graph graph)
	{
		final double[] bestVertexThetas = { 0, 0.5 };
		
		for (final SiteType siteType : SiteType.values())
		{
			final List<? extends GraphElement> elements = graph.elements(siteType);
			if (elements.isEmpty())
				continue;
			
			// Estimate unit size (expected average distance between elements positions)
			final Rectangle2D bounds = Graph.bounds(elements);	
			final double unit = (bounds.getWidth() + bounds.getHeight()) / 2 / Math.sqrt(elements.size());

//			System.out.println("\n" + siteType + ":\n");
		
			if (siteType == SiteType.Edge)
				clusterByRowAndColumn(elements, unit, bestVertexThetas, true);
			else
				clusterByRowAndColumn(elements, unit, bestVertexThetas, false);
			clusterByDimension(elements, RCLType.Layer, unit, 0);
			
			setCoordinateLabels(graph, siteType, elements);
		}
	}

	
	/**
	 * Clusters coordinates along a given dimension.
	 * 
	 * @param elements      The elements to measure.
	 * @param unit          Estimated unit length.
	 * @param bestThetas    The best theta values.
	 * @param useBestThetas True if the best theta values have to be used.
	 */
	public static void clusterByRowAndColumn
	(
		final List<? extends GraphElement> elements,
		final double unit,
		final double[] bestThetas,
		final boolean useBestThetas
	)
	{
//		final long startAt = System.nanoTime();

		if (useBestThetas)
		{
			// Site types must be Edge: Use previously found best thetas from Vertex
			clusterByDimension(elements, RCLType.Row,    unit, bestThetas[0]);
			clusterByDimension(elements, RCLType.Column, unit, bestThetas[1]);
			return;
		}
		
		// Find best row angle		
		double bestError = 1000;
		bestThetas[0] = 0;
		
		for (int angle = 0; angle <= 60; angle += 15)
		{
			final double theta = angle / 180.0 * Math.PI;
			final double error = clusterByDimension(elements, RCLType.Row, unit, theta);
			
			if (error < bestError)
			{
				bestError = error;
				bestThetas[0] = theta;
				if (error < 0.01)
					break;  // won't get much better
			}
		}
		clusterByDimension(elements, RCLType.Row, unit, bestThetas[0]);
		
//		System.out.println("Best row angle is " + (int)(Math.toDegrees(bestThetas[0]) + 0.5) + ".");

		// Find best column angle, based on best row angle
		bestError = 1000;
		bestThetas[1] = 0;
		
		for (int angle = 90; angle <= 120; angle += 15)
		{
			final double theta = bestThetas[0] + angle / 180.0 * Math.PI;
			final double error = clusterByDimension(elements, RCLType.Column, unit, theta);
			
			if (error < bestError)
			{
				bestError = error;
				bestThetas[1] = theta;
				if (error < 0.01)
					break;  // won't get much better
			}
		}
		clusterByDimension(elements, RCLType.Column, unit, bestThetas[1]);

//		System.out.println("Best column angle is " + (int)(Math.toDegrees(bestThetas[1]) + 0.5) + ".");
		
//		final long stopAt = System.nanoTime();
//		final double secs = (stopAt - startAt) / 1000000.0;
//		System.out.println("Time: " + secs + "ms.");
	}
	
	/**
	 * Clusters coordinates along a given dimension. 
	 * 
	 * @param elements The elements to measure.
	 * @param rclType  Coordinate dimension to cluster on.
	 * @param unit     Expected element spacing.
	 * @param theta    Angle of base line.
	 *
	 * @return Average cluster error.
	 */
	public static double clusterByDimension
	(
		final List<? extends GraphElement> elements,
		final RCLType rclType,
		final double unit,
		final double theta
	)
	{	
		// Prepare base line to measure from
		Point2D refA=null, refB=null;
		final Rectangle2D bounds = Graph.bounds(elements);	
		
		if (rclType == RCLType.Row)
		{
			refA = 	new Point2D.Double(bounds.getX() + bounds.getWidth() / 2, bounds.getY() - bounds.getHeight());
			refB = 	new Point2D.Double
					(
						refA.getX() + bounds.getWidth() * Math.cos(theta),
						refA.getY() + bounds.getWidth() * Math.sin(theta)
					);
		}
		else if (rclType == RCLType.Column)
		{
			refA = 	new Point2D.Double(bounds.getX() - bounds.getWidth(), bounds.getY() + bounds.getHeight() / 2);
			refB = 	new Point2D.Double
					(
						refA.getX() + bounds.getWidth() * Math.cos(theta),
						refA.getY() + bounds.getWidth() * Math.sin(theta)
					);
		}
		
		// Sort elements by distance from reference line AB
		final List<ItemScore> rank = new ArrayList<ItemScore>();
		final double margin = 0.6 * unit;
		
		for (int n = 0; n < elements.size(); n++)
		{
			final double dist = (rclType == RCLType.Layer)
								? elements.get(n).pt().z()
								: MathRoutines.distanceToLine(elements.get(n).pt2D(), refA, refB);
			rank.add(new ItemScore(n, dist));
		}
		Collections.sort(rank);

//		System.out.print("Scores:");
//		for (final ItemScore item : rank)
//			System.out.print(" " + item.score());
//		System.out.println();
		
		// Cluster
		final List<Bucket> buckets = new ArrayList<Bucket>();
		Bucket bucket = null;
		
		for (final ItemScore item : rank)
		{
			if (bucket == null || Math.abs(item.score() - bucket.mean()) > margin)
			{
				// Create a new bucket for this item
				bucket = new Bucket();
				buckets.add(bucket);
			}
			bucket.addItem(item);
		}
//		System.out.println(buckets.size() + " " + rclType + " buckets created.");
//		System.out.print("Buckets:");
//		for (final Bucket bkt : buckets)
//			System.out.print(" " + bkt.mean() + " (" + bkt.items().size() + ")");
//		System.out.println();
		
		// Assign elements to buckets
		for (int bid = 0; bid < buckets.size(); bid++)
			for (final ItemScore item : buckets.get(bid).items())
			{
				final RCL rcl = elements.get(item.id()).situation().rcl();
				switch (rclType)
				{
				case Row:	 rcl.setRow(bid);	 break;
				case Column: rcl.setColumn(bid); break;
				case Layer:	 rcl.setLayer(bid);	 break;
				default:     // do nothing
				}
			}
		
		// Determine error
		double error = 0;
		for (final Bucket bkt : buckets)
		{
			double acc = 0;
			for (final ItemScore item : bkt.items())
				acc = (bkt.mean() - item.score()) * (bkt.mean() - item.score());
			error += acc / bkt.items().size(); 
		}
		error += 0.01 * buckets.size();  // penalise greater bucket counts
		//System.out.println("Error is " + error + ".");
		
		return error;
	}
	
	/**
	 * Sets the coordinate label for each element based on its RCL situation.
	 * 
	 * @param graph    The graph to modify.
	 * @param siteType The graph element type.
	 * @param elements Elements to be labelled.
	 */
	public static void setCoordinateLabels
	(
		final Graph    graph,
		final SiteType siteType,
		final List<? extends GraphElement> elements
	)
	{
		// Check if distinct layers	
		boolean distinctLayers = false;
		for (int eid = 0; eid < elements.size()-1; eid++)
			if 
			(
				elements.get(eid).situation().rcl().layer()
				!=
				elements.get(eid+1).situation().rcl().layer()
			)
			{
				distinctLayers = true;
				break;
			}
		
		// Create coordinates, checking for duplicates within this site type
		final Map<String, GraphElement> map = new HashMap<String, GraphElement>();
		map.clear();
		
		for (final GraphElement element : elements)
		{
			// Column label
			int column = element.situation().rcl().column();
			String label = "" + (char)('A' + column % 26);
			
			while (column >= 26)
			{
				column /= 26;
				label = (char)('A' + column % 26 - 1) + label;
			}
			
			// Row label
			label += (element.situation().rcl().row() + 1);
			
			// Layer label
			if (distinctLayers)
				label += "/" + element.situation().rcl().layer();
			
			if (map.get(label) != null)
			{
//				System.out.println("Duplicate " + siteType + " coordinate " + label + ".");
				graph.setDuplicateCoordinates(siteType);
			}
			else
			{
				map.put(label, element);
			}
			
			element.situation().setLabel(label);
		}
	}
	
	//-------------------------------------------------------------------------
		
}
