package game.util.graph;

import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import main.math.MathRoutines;
import main.math.Point3D;

//-----------------------------------------------------------------------------

/**
 * Defines a Graph vertex.
 * 
 * @author cambolbro
 */
public class Vertex extends GraphElement
{
	private final List<Edge> edges = new ArrayList<Edge>();
	private final List<Face> faces = new ArrayList<Face>();
	
	private Vertex pivot = null;
	
	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 * 
	 * @param id The index of the vertex.
	 * @param x  The x position of the vertex.
	 * @param y  The y position of the vertex.
	 */
	public Vertex
	(
		final int id, final double x, final double y
	)
	{
		this.id = id;
		this.pt = new Point3D(x, y);
	}

	/**
	 * Constructor.
	 * 
	 * @param id The index of the vertex.
	 * @param x  The x position of the vertex.
	 * @param y  The y position of the vertex.
	 * @param z  The z position of the vertex.
	 */
	public Vertex
	(
		final int id, final double x, final double y, final double z
	)
	{
		this.id = id;
		this.pt = new Point3D(x, y, z);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param id The index of the vertex.
	 * @param pt The Point3D of the vertex.
	 */
	public Vertex
	(
		final int id, final Point3D pt
	)
	{
		this.id = id;
		this.pt = new Point3D(pt);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param id The index of the vertex.
	 * @param pt The Point2D of the vertex.
	 */
	public Vertex
	(
		final int id, final Point2D pt
	)
	{
		this.id = id;
		this.pt = new Point3D(pt.getX(), pt.getY());
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The edges of the vertex.
	 */
	public List<Edge> edges()
	{
		return Collections.unmodifiableList(edges);
	}

	/**
	 * @return The faces of the vertex.
	 */
	public List<Face> faces()
	{
		return faces;
	}

	@Override
	public Vertex pivot()
	{
		return pivot;
	}

	/**
	 * Set the pivot of the vertex.
	 * 
	 * @param vertex The pivot vertex.
	 */
	public void setPivot(final Vertex vertex)
	{
		pivot = vertex;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Clear the edges.
	 */
	public void clearEdges()
	{
		edges.clear();
	}

	/**
	 * Add an edge.
	 * 
	 * @param edge The edge.
	 */
	public void addEdge(final Edge edge)
	{
		edges.add(edge);
	}
	
	/**
	 * Remove an edge.
	 * 
	 * @param n the index of the edge.
	 */
	public void removeEdge(final int n)
	{
		edges.remove(n);
	}
	
	/**
	 * Clear the faces.
	 */
	public void clearFaces()
	{
		faces.clear();
	}
	
	/**
	 * Add a face.
	 * 
	 * @param face The face.
	 */
	public void addFace(final Face face)
	{
		faces.add(face);
	}
	
	/**
	 * Remove a face.
	 * 
	 * @param n The index of the face.
	 */
	public void removeFace(final int n)
	{
		faces.remove(n);
	}

	//-------------------------------------------------------------------------

	@Override
	public SiteType siteType()
	{
		return SiteType.Vertex;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param edge The edge.
	 * @return Edge's position in this vertex's list, else -1 if none.
	 */
	public int edgePosition(final Edge edge)
	{
		for (int n = 0; n < edges.size(); n++)
			if (edges.get(n).matches(edge))
				return n;
		return -1;
	}

	/**
	 * @param face The face.
	 * @return Face's position in this vertex's list, else -1 if none.
	 */
	public int facePosition(final Face face)
	{
		for (int n = 0; n < faces.size(); n++)
			if (faces.get(n).id() == face.id())
				return n;
		return -1;
	}

	/**
	 * @param other The other vertex.
	 * @return Known edge between this vertex and another vertex, else null if none.
	 */
	public Edge incidentEdge(final Vertex other)
	{
		for (final Edge edge : edges)
			if (edge.matches(this,  other))
				return edge;
		return null;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param other     The other vertex.
	 * @param tolerance The tolerance.
	 * @return Vertex at same location within specified tolerance (e.g. 0.1).
	 */
	public boolean coincident(final Vertex other, final double tolerance)
	{
		return coincident(other.pt.x(), other.pt.y(), other.pt.z(), tolerance);
	}

	/**
	 * @param x         The x position.
	 * @param y         The y position.
	 * @param z         The z position.
	 * @param tolerance The tolerance.
	 * @return Vertex at same location within specified tolerance (e.g. 0.1).
	 */
	public boolean coincident(final double x, final double y, final double z, final double tolerance)
	{
		final double error = Math.abs(x - pt.x()) + Math.abs(y - pt.y()) + Math.abs(z - pt.z());
		if (error < tolerance)
			return true;
		return false;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param face The face.
	 * @return Vertex on the other end of the first edge coincident with this vertex
	 *         that is not also on the specified face.
	 */
	public Vertex edgeAwayFrom(final Face face)
	{
		for (final Edge edge : edges)
		{
			if (!face.contains(edge))
				return edge.otherVertex(id);
		}
		return null;
	}
	
	//-------------------------------------------------------------------------

	/** 
	 * Sort edges by direction in clockwise order around vertex.
	 */
	public void sortEdges()
	{
		Collections.sort(edges, new Comparator<Edge>() 
		{
			@Override
			public int compare(final Edge a, final Edge b)
			{
				final Vertex va = a.otherVertex(id());
				final Vertex vb = b.otherVertex(id());
				
				final double dirnA = Math.atan2(va.pt.y() - pt.y(), va.pt.x() - pt.x());
				final double dirnB = Math.atan2(vb.pt.y() - pt.y(), vb.pt.x() - pt.x());
				
				if (dirnA == dirnB)
					return 0;
				
				return (dirnA < dirnB) ? -1 : 1;
			}
		});
		
//		System.out.print("Edge angles for vertex " + id() + ":");
//		for (Edge edge : edges)
//		{
//			final Vertex v = edge.otherVertex(id());
//			final double dirn = Math.atan2(v.y() - y(), v.x() - x());
//			System.out.print(" " + dirn);
//		}
//		System.out.println();
	}
	
	//-------------------------------------------------------------------------

	/** 
	 * Sort edges by direction in clockwise order around vertex.
	 */
	void sortFaces()
	{
		Collections.sort(faces, new Comparator<Face>() 
		{
			@Override
			public int compare(final Face a, final Face b)
			{
				final double dirnA = Math.atan2(a.pt.y() - pt.y(), a.pt.x() - pt.x());
				final double dirnB = Math.atan2(b.pt.y() - pt.y(), b.pt.x() - pt.x());
				
				if (dirnA == dirnB)
					return 0;
				
				return (dirnA < dirnB) ? -1 : 1;
			}
		});
		
//		System.out.print("Face angles for vertex " + id() + ":");
//		for (final GraphElement ge : faces)
//		{
//			final double dirn = Math.atan2(ge.pt().y() - pt.y(), ge.pt().x() - pt.x());
//			System.out.print(" " + dirn);
//		}
//		System.out.println();
	}

	//-------------------------------------------------------------------------

	@Override
	public List<GraphElement> nbors()
	{
		final List<GraphElement> nbors = new ArrayList<GraphElement>();
		
		for (final Edge edge : edges)
			nbors.add(edge.otherVertex(id));
			
		return nbors;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public void stepsTo(final Steps steps2)
	{		
		//-------------------------------------------------
		// Steps to other vertices
		
		// Add orthogonal steps due to edges
		for (final Edge edge : edges)
		{
			final Vertex to = edge.otherVertex(id);

			final Step newStep = new Step(this, to);
						
			newStep.directions().set(AbsoluteDirection.Orthogonal.ordinal());
			newStep.directions().set(AbsoluteDirection.Adjacent.ordinal());
			newStep.directions().set(AbsoluteDirection.All.ordinal());
			steps2.add(newStep);
		}
		
		//-------------------------------------------------
		// Diagonal steps across cell faces

		for (final Face face : faces)
		{
			if (face.vertices().size() < 4)
				continue;  // no diagonal across a triangle
			
			// Find maximum distance across this diagonal
			double bestDist = 1000000;
			Vertex bestTo = null;
			
			for (final Vertex to : face.vertices())
			{
				if (to.id() == id)
					continue;
				
				final double dist = MathRoutines.distanceToLine(face.pt2D(), pt2D(), to.pt2D());
				if (dist < bestDist)
				{
					bestDist = dist;
					bestTo = to;
				}
			}
			
			// Create this diagonal 'to' step
			final Step newStep = new Step(this, bestTo);
						
			newStep.directions().set(AbsoluteDirection.Diagonal.ordinal());
			newStep.directions().set(AbsoluteDirection.All.ordinal());
			steps2.add(newStep);
		}
		
		//-------------------------------------------------
		// Steps to edges
		
		for (final Edge edge : edges)
		{
			final Step newStep = new Step(this, edge);
						
			newStep.directions().set(AbsoluteDirection.Orthogonal.ordinal());
			newStep.directions().set(AbsoluteDirection.Adjacent.ordinal());
			newStep.directions().set(AbsoluteDirection.All.ordinal());
			steps2.add(newStep);
		}
		
		//-------------------------------------------------
		// Steps to faces
		
		for (final Face face : faces)
		{
			final Step newStep = new Step(this, face);
						
			newStep.directions().set(AbsoluteDirection.Orthogonal.ordinal());
			newStep.directions().set(AbsoluteDirection.Adjacent.ordinal());
			newStep.directions().set(AbsoluteDirection.All.ordinal());
			steps2.add(newStep);
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		final DecimalFormat df = new DecimalFormat("#.###");
		
		sb.append("Vertex[" + id + "]: (");
		
		if (pt.x() == (int)pt.x() && pt.y() == (int)pt.y())
			sb.append((int)pt.x() + "," + (int)pt.y());
		else
			sb.append(df.format(pt.x()) + "," + df.format(pt.y()));
		
		if (pt.z() == (int)pt.z())
			sb.append("," + (int)pt.z());
		else
			sb.append("," + df.format(pt.z()));
		
		sb.append(")");
		
		if (pivot != null)
			sb.append(" pivot=" + pivot.id());
		
		// List edges
		sb.append(" [");
		for (int e = 0; e < edges.size(); e++)
		{
			if (e > 0)
				sb.append(" ");
			sb.append(edges.get(e).id());
		}
		sb.append("]");
		
		sb.append(" " + properties);

		sb.append(" \"" + situation.label() + "\"");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
}
