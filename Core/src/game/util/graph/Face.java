package game.util.graph;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import main.math.MathRoutines;
import main.math.Point3D;

//-----------------------------------------------------------------------------

/**
 * Defines a face in a graph.
 * 
 * @author cambolbro
 */
public class Face extends GraphElement
{
	private final List<Vertex> vertices = new ArrayList<Vertex>();
	private final List<Edge>   edges    = new ArrayList<Edge>();
	
	//-------------------------------------------------------------------------

	/**
	 * A face.
	 * 
	 * @param id
	 */
	public Face(final int id)
	{
		this.id = id;
	}

	//-------------------------------------------------------------------------

//	@Override
//	public int id()
//	{
//		return id;
//	}

	/**
	 * @return The vertices of the face.
	 */
	public List<Vertex> vertices()
	{
		return Collections.unmodifiableList(vertices);
	}
	
	/**
	 * @return The edges of the face.
	 */
	public List<Edge> edges()
	{
		return Collections.unmodifiableList(edges);
	}
	
	//-------------------------------------------------------------------------

//	public void clearVertices()
//	{
//		vertices.clear();
//	}
//	
//	public void addVertex(final Vertex vertex)
//	{
//		vertices.add(vertex);
//	}
//	
//	public void removeVertex(final int n)
//	{
//		vertices.remove(n);
//	}
//
//	public void clearEdges()
//	{
//		edges.clear();
//	}
//
//	public void addEdge(final Edge edge)
//	{
//		edges.add(edge);
//	}
//	
//	public void removeEdge(final int n)
//	{
//		edges.remove(n);
//	}
	
	//-------------------------------------------------------------------------

	@Override
	public Vertex pivot()
	{
		for (final Vertex vertex : vertices)
			if (vertex.pivot() != null)
				return vertex.pivot();
		
		return null;
	}

	//-------------------------------------------------------------------------

	@Override
	public SiteType siteType()
	{
		return SiteType.Cell;
	}

	//-------------------------------------------------------------------------

	/**
	 * Add a vertex and an edge to the face.
	 * 
	 * @param vertex
	 * @param edge
	 */
	public void addVertexAndEdge(final Vertex vertex, final Edge edge)
	{
		vertices.add(vertex);
		edges.add(edge);
		setMidpoint();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param vids
	 * 
	 * @return Whether the provided list of vertex indices matches this cell's
	 *         vertices, forwards or backwards, starting at any point.
	 */
	public boolean matches(final int ... vids)
	{
		final int numSides = vertices.size();
		
//		System.out.print("\nFace:");
//		for (final Vertex vertex : vertices)
//			System.out.print(" " + vertex.id());
//		System.out.println();
		
		if (vids.length != numSides)
			return false;
		
		int start;
		for (start = 0; start < numSides; start++)
			if (vertices.get(start).id() == vids[0])
				break;
		
		if (start >= numSides)
			return false;
		
		// Try forwards
//		System.out.print("Forwards:");
		int n;
		for (n = 0; n < numSides; n++)
		{
			final int nn = (start + n) % numSides;
			
//			System.out.print(" " + vertices.get(nn).id());

			if (vertices.get(nn).id() != vids[n])
				break;
		}
		if (n >= numSides)
			return true;  // all match
		
		// Try backwards
//		System.out.print("\nBackwards:");
		for (n = 0; n < numSides; n++)
		{
			final int nn = (start - n + numSides) % numSides;
			
//			System.out.print(" " + vertices.get(nn).id());

			if (vertices.get(nn).id() != vids[n])
				break;
		}
//		System.out.println();
		
		return (n >= numSides);  // all match
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Set the midpoint.
	 */
	public void setMidpoint()
	{
		double xx = 0;
		double yy = 0;
		double zz = 0;
		
		if (!vertices.isEmpty())
		{
			for (final Vertex vertex : vertices)
			{
				xx += vertex.pt().x();
				yy += vertex.pt().y();
				zz += vertex.pt().z();
			}
			xx /= vertices.size();
			yy /= vertices.size();
			zz /= vertices.size();
		}
		
		pt = new Point3D(xx, yy, zz);
	}

	//-------------------------------------------------------------------------

	/**
	 * @param vertexIn
	 * @return True if vertexIn is one of the face's vertices.
	 */
	public boolean contains(final Vertex vertexIn)
	{
		for (final Vertex vertex : vertices())
			if (vertex.id() == vertexIn.id())
				return true;
		return false;
	}

	/**
	 * @param edgeIn
	 * @return true if that face contains that edge.
	 */
	public boolean contains(final Edge edgeIn)
	{
		for (final Edge edge : edges())
			if (edge.id() == edgeIn.id())
				return true;
		return false;
	}
	
	/**
	 * @param p Point to test.
	 * @return Whether pt is within this face (assume planar).
	 */
	public boolean contains(final Point2D p)
	{
		final List<Point2D> poly = new ArrayList<>();
		
		for (final Vertex vertex : vertices)
			poly.add(vertex.pt2D());
		
		return MathRoutines.pointInPolygon(p, poly);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public List<GraphElement> nbors()
	{
		final List<GraphElement> nbors = new ArrayList<GraphElement>();
		
		for (final Edge edge : edges())
		{
			final Face nbor = edge.otherFace(id);
			if (nbor != null)
				nbors.add(nbor);
		}
		
		return nbors;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public void stepsTo(final Steps steps)
	{
		final BitSet usedFaces = new BitSet();
		usedFaces.set(id, true);  // set this face as 'used' to exclude self from tests
		
		//-------------------------------------------------
		// Steps to adjacent cells
		
		for (final Edge edge : edges)
		{
			final Face other = edge.otherFace(id);
			if (other == null)
				continue;

			usedFaces.set(other.id(), true);  // used as an adjacent neighbour
				
			final Step newStep = new Step(this, other);
				
			newStep.directions().set(AbsoluteDirection.Orthogonal.ordinal());
			newStep.directions().set(AbsoluteDirection.Adjacent.ordinal());
			newStep.directions().set(AbsoluteDirection.All.ordinal());
			steps.add(newStep);
		}
		
		// **
		// ** TODO: Assumes that all coincident edges are orthogonal/adjacent.
		// **       Instead, perhaps only the straightest continuation should 
		// **       be orthogonal and all others diagonal?
		// **
		
		//-------------------------------------------------
		// Steps to diagonal cells joined by a vertex, e.g. square grid.
		
		for (final Vertex vertex : vertices)
		{
			// Look for a diagonal cell connected through this vertex
			double bestDistance = 1000000;
			Face diagonal = null;
			
			// Check faces around each vertex
			for (final Face other : vertex.faces())
			{
				if (usedFaces.get(other.id()))
					continue;
			
				final double dist = MathRoutines.distanceToLine(vertex.pt2D(), pt2D(),  other.pt2D());
				if (dist < bestDistance)
				{
					bestDistance = dist;
					diagonal = other;
				}
			}
				
			if (diagonal == null)
				continue;  // no diagonal on this corner
			
			// **
			// ** TODO: Check for case of diagonals of equal angle.
			// **
				
			usedFaces.set(diagonal.id(), true);  // adjacent diagonal neighbour
				
			final Step newStep = new Step(this, diagonal);

			newStep.directions().set(AbsoluteDirection.Diagonal.ordinal());
			newStep.directions().set(AbsoluteDirection.Adjacent.ordinal());
			newStep.directions().set(AbsoluteDirection.All.ordinal());
			steps.add(newStep);
		}
		
		//-------------------------------------------------
		// Steps to off-diagonal cells joined by a vertex, e.g. tri grid.
		for (final Vertex vertex : vertices)
		{
			// Look for remaining off-diagonal cells connected through this vertex
			for (final Face other : vertex.faces())
			{
				if (usedFaces.get(other.id()))
					continue;
					
				usedFaces.set(other.id(), true);  // adjacent off-diagonal neighbour

				final Step newStep = new Step(this, other);
				
				newStep.directions().set(AbsoluteDirection.OffDiagonal.ordinal());
				newStep.directions().set(AbsoluteDirection.Adjacent.ordinal());
				newStep.directions().set(AbsoluteDirection.All.ordinal());
				steps.add(newStep);
			}
		}

		//-------------------------------------------------
		// Steps to non-adjacent diagonal cells joined by an edge, e.g. hex grid.
		
		for (final Vertex vertex : vertices)
		{
			// Look for non-adjacent diagonal cell connected through and edge
			if (vertex.edges().size() != 3)
				continue;  // only applies to trivalent intersections
			
			final Vertex otherVertex = vertex.edgeAwayFrom(this);
			if (otherVertex == null)
			{
				System.out.println("** Null otherVertex in Face non-adjacent diagonals test.");
				continue;
			}
			for (final Face otherFace : otherVertex.faces())
			{
				if (usedFaces.get(otherFace.id()))
					continue;
				
				final double distV  = MathRoutines.distanceToLine(vertex.pt2D(), pt2D(), otherFace.pt2D());
				final double distOV = MathRoutines.distanceToLine(otherVertex.pt2D(), pt2D(), otherFace.pt2D());
				final double distAB = MathRoutines.distance(pt2D(), otherFace.pt2D());
				final double error  = (distV + distOV) / distAB;
				
				if (error > 0.1)
				{
					// Edge is not close to lying on a diagonal between both faces 
					continue;
				}
					
				usedFaces.set(otherFace.id(), true);  // non-adjacent diagonal neighbour
				
				final Step newStep = new Step(this, otherFace);
				
				newStep.directions().set(AbsoluteDirection.Diagonal.ordinal());
				newStep.directions().set(AbsoluteDirection.All.ordinal());
				steps.add(newStep);
			}
		}

		//-------------------------------------------------
		// Steps to vertices
		
		for (final Vertex vertex : vertices)
		{
			final Step newStep = new Step(this, vertex);

			newStep.directions().set(AbsoluteDirection.Orthogonal.ordinal());
			newStep.directions().set(AbsoluteDirection.Adjacent.ordinal());
			newStep.directions().set(AbsoluteDirection.All.ordinal());
			steps.add(newStep);
		}
		
		//-------------------------------------------------
		// Steps to edges
		
		for (final Edge edge : edges)
		{
			final Step newStep = new Step(this, edge);

			newStep.directions().set(AbsoluteDirection.Orthogonal.ordinal());
			newStep.directions().set(AbsoluteDirection.Adjacent.ordinal());
			newStep.directions().set(AbsoluteDirection.All.ordinal());
			steps.add(newStep);
		}
		
//		return stepsTo;	
	}

		//-------------------------------------------------------------------------

	/**
	 *  From: Yuceer and Oflazer 'A Rotation, Scaling, and Translation Invariant
	 *        Pattern Classification System', Pattern Recognition, 26:5, 1993, 687--710.
	 *
	 * @return Angular momentum through this polygon.
	 */
	public double momentum()
	{
//		final int numVertices = vertices().size();
		
//		double xav = 0;
//		double yav = 0;
//		for (final Vertex vertex : face.vertices())
//		{
//			xav += vertex.pt().x();
//			yav += vertex.pt().y();
//		}
//		xav /= numVertices;
//		yav /= numVertices;
//		
//		double rav = 0;
//		for (final Vertex vertex : face.vertices())
//		{
//			final double dx = vertex.pt().x() - xav;
//			final double dy = vertex.pt().y() - yav;
//			rav += Math.sqrt(dx * dx + dy * dy); 
//			vertex.pt().x();
//			yav += vertex.pt().y();
//		}
//		xav /= numVertices;
//		yav /= numVertices;
		
		double Txx = 0;
		double Tyy = 0;
		double Txy = 0;
		
		for (final Vertex vertex : vertices())
		{
			final double x = vertex.pt().x() - pt.x();
			final double y = vertex.pt().y() - pt.y();
			
			Txx += x * x;
			Tyy += y * y;
			Txy += x * y;
		}
		
//		Txx -= numVertices * pt.x() * pt.x();
//		Tyy -= numVertices * pt.y() * pt.y();
//		Txy -= numVertices * pt.x() * pt.y();

		System.out.println("\nTxx=" + Txx + ", Tyy=" + Tyy + ", Txy=" + Txy + ".");

		final double sinTheta = 
				(Tyy - Txx + Math.sqrt((Tyy - Txx) * (Tyy - Txx) + 4 * Txy * Txy))
				/
				Math.sqrt
				(
					(
						8 * Txy * Txy + 2 * (Tyy - Txx) * (Tyy - Txx)
						+
						2 * Math.abs(Tyy - Txx) * Math.sqrt((Tyy - Txx) * (Tyy - Txx) + 4 * Txy * Txy)
					)
				);
				
		System.out.println("sinTheta=" + sinTheta);
		
		final double cosTheta = 
				2 * Txy
				/
				Math.sqrt
				(
					(
						8 * Txy * Txy + 2 * (Tyy - Txx) * (Tyy - Txx)
						+
						2 * Math.abs(Tyy - Txx) * Math.sqrt((Tyy - Txx) * (Tyy - Txx) + 4 * Txy * Txy)
					)
				);
		System.out.println("cosTheta=" + cosTheta);
		
		//double theta = Math.asin(sinTheta);
		final double theta = Math.acos(cosTheta);
		
		return theta;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
	
		sb.append("Face[" + id + "]:");
		for (final Vertex vertex : vertices)
			sb.append(" " + vertex.id());
		
		sb.append(" " + properties);
		
		sb.append(" \"" + situation.label() + "\"");

		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
}
