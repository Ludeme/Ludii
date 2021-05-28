package game.util.graph;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import main.math.Point3D;
import main.math.Vector;

//-----------------------------------------------------------------------------

/**
 * Defines a Graph edge.
 * 
 * @author cambolbro
 */
public class Edge extends GraphElement
{
	/** These can't be final, as faces might get merged. */
	private Vertex vertexA = null;
	private Vertex vertexB = null;
	
	/** These can't be final, as faces get calculated afterwards. */
	private Face left  = null;
	private Face right = null;
	
	/** Whether to use end point pivots to define curved edge. */
	//private final boolean curved;
	
	private Vector tangentA = null;
	private Vector tangentB = null;

	//-------------------------------------------------------------------------

	/**
	 * Define an edge.
	 * 
	 * @param id
	 * @param va
	 * @param vb
	 */
	public Edge
	(
		final int id, final Vertex va, final Vertex vb  //, final boolean curved
	)
	{
		this.id = id;
		this.vertexA = va;
		this.vertexB = vb;
		
//		this.curved = curved;
		
		setMidpoint();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The vertex A.
	 */
	public Vertex vertexA()
	{
		return vertexA;
	}

	/**
	 * Set the vertex A.
	 * 
	 * @param vertA
	 */
	public void setVertexA(final Vertex vertA)
	{
		vertexA = vertA;
		setMidpoint();
	}
	
	/**
	 * @return The vertex B.
	 */
	public Vertex vertexB()
	{
		return vertexB;
	}
	
	/**
	 * Set the vertex B.
	 * 
	 * @param vertB
	 */
	public void setVertexB(final Vertex vertB)
	{
		vertexB = vertB;
		setMidpoint();
	}
	
	/**
	 * @return The face to the left.
	 */
	public Face left()
	{
		return left;
	}
	
	/**
	 * Set the face to the left.
	 * 
	 * @param face
	 */
	public void setLeft(final Face face)
	{
		left = face;
	}
	
	/**
	 * @return The face to the right.
	 */
	public Face right()
	{
		return right;
	}
	
	/**
	 * Set the face to the right.
	 * 
	 * @param face
	 */
	public void setRight(final Face face)
	{
		right = face;
	}
	
	/**
	 * @return True if the edge is curved.
	 */
	public boolean curved()
	{
//		return curved;
		return tangentA != null && tangentB != null;
	}
	
//	public void setCurved(final boolean value)
//	{
//		curved = value;
//	}

	/**
	 * @return The vector of the tangent from the vertex A.
	 */
	public Vector tangentA()
	{
		return tangentA;
	}
	
	/**
	 * Set the vector of the tangent A.
	 * 
	 * @param vec
	 */
	public void setTangentA(final Vector vec)
	{
		tangentA = vec;
	}
	
	/**
	 * @return The vector of the tangent from the vertex B.
	 */
	public Vector tangentB()
	{
		return tangentB;
	}
		
	/**
	 * Set the vector of the tangent B.
	 * 
	 * @param vec
	 */
	public void setTangentB(final Vector vec)
	{
		tangentB = vec;
	}

	//-------------------------------------------------------------------------

	@Override
	public Vertex pivot()
	{
		if (vertexA.pivot() != null)
			return vertexA.pivot();
		
		return vertexB.pivot();
	}

	//-------------------------------------------------------------------------

	@Override
	public SiteType siteType()
	{
		return SiteType.Edge;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param vertex
	 * @return True if the vertex is on the edge.
	 */
	public boolean contains(final Vertex vertex)
	{
		return contains(vertex.id());
	}

	/**
	 * @param vid
	 * @return True if the vertex is on the edge.
	 */
	public boolean contains(final int vid)
	{
		return vertexA.id() == vid || vertexB.id() == vid;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return True if the edge is exterior.
	 */
	public boolean isExterior()
	{
		return left == null || right == null;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return The length of the edge.
	 */
	public double length()
	{
		return vertexA.pt().distance(vertexB.pt());  //MathRoutines.distance(va.x(), va.y(), vb.x(), vb.y());
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param other
	 * @return True if the edge is the same of the object in entry.
	 */
	public boolean matches(final Edge other)
	{
		return matches(other.vertexA.id(), other.vertexB.id()); 
	}

	/**
	 * @param vc
	 * @param vd
	 * @return True if the edge uses the two vertices in entry.
	 */
	public boolean matches(final Vertex vc, final Vertex vd)
	{
		return matches(vc.id(), vd.id()); 
	}

	/**
	 * @param idA
	 * @param idB
	 * @return True if the edge uses the two vertices indices in entry.
	 */
	public boolean matches(final int idA, final int idB)
	{
		return 	vertexA.id() == idA && vertexB.id() == idB 
				|| 
				vertexA.id() == idB && vertexB.id() == idA; 
	}

	/**
	 * @param idA
	 * @param idB
	 * @param curved
	 * @return True if the edge match and if the edge is curved.
	 */
	public boolean matches(final int idA, final int idB, final boolean curved)
	{
		return  (
					vertexA.id() == idA && vertexB.id() == idB 
					|| 
					vertexA.id() == idB && vertexB.id() == idA
				) 
				&& 
				curved() == curved; 
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param other
	 * @param tolerance
	 * @return True if the vertices are coincident.
	 */
	public boolean coincidentVertices(final Edge other, final double tolerance)
	{
		return 	vertexA.coincident(other.vertexA, tolerance) && vertexB.coincident(other.vertexB, tolerance) 
				||
				vertexA.coincident(other.vertexB, tolerance) && vertexB.coincident(other.vertexA, tolerance);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param other
	 * @return True if two edges shares a vertex.
	 */
	public boolean sharesVertex(final Edge other)
	{
		return sharesVertex(other.vertexA().id()) || sharesVertex(other.vertexB().id()); 
	}

	/**
	 * @param v
	 * @return True if the vertex is on the edge.
	 */
	public boolean sharesVertex(final Vertex v)
	{
		return sharesVertex(v.id()); 
	}

	/**
	 * @param vid
	 * @return True if the vertex is on the edge.
	 */
	public boolean sharesVertex(final int vid)
	{
		return vertexA.id() == vid || vertexB.id() == vid;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param vid
	 * @return The other vertex of the edge.
	 */
	public Vertex otherVertex(final int vid)
	{
		if (vid == vertexA.id())
			return vertexB;
		if (vid == vertexB.id())
			return vertexA;
		return null;
	}
	
	/**
	 * @param v
	 * @return The other vertex of the edge.
	 */
	public Vertex otherVertex(final Vertex v)
	{
		return otherVertex(v.id());
	}

	//-------------------------------------------------------------------------

	/**
	 * @param fid
	 * @return The other face of the edge.
	 */
	public Face otherFace(final int fid)
	{
		if (left != null && left.id() == fid)
			return right;
		
		if (right != null && right.id() == fid)
			return left;
		
		return null;
	}
	
	/**
	 * @param face
	 * @return The other face of the edge.
	 */
	public Face otherFace(final Face face)
	{
		return otherFace(face.id());
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Set the midpoint of the edge.
	 */
	public void setMidpoint()
	{
		pt = new Point3D
		(	
			(vertexA.pt.x() + vertexB.pt.x()) * 0.5,
			(vertexA.pt.y() + vertexB.pt.y()) * 0.5,
			(vertexA.pt.z() + vertexB.pt.z()) * 0.5
		);
	}

	//-------------------------------------------------------------------------

	@Override
	public List<GraphElement> nbors()
	{
		final List<GraphElement> nbors = new ArrayList<GraphElement>();
		
		for (final Edge edgeA : vertexA.edges())
			if (edgeA.id() != id)
				nbors.add(edgeA);	
		
		for (final Edge edgeB : vertexB.edges())
			if (edgeB.id() != id)
				nbors.add(edgeB);			
		
		return nbors;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public void stepsTo(final Steps steps)
	{	
		//-------------------------------------------------
		// Steps to coincident edges
		
		for (int v = 0; v < 2; v++)
		{
			final Vertex vertex = (v == 0) ? vertexA : vertexB;
			for (final Edge other : vertex.edges())
			{
				if (other.id == id)
					continue;  // don't step to self
					
				final Step newStep = new Step(this, other);
				
				newStep.directions().set(AbsoluteDirection.Orthogonal.ordinal());
				newStep.directions().set(AbsoluteDirection.Adjacent.ordinal());
				newStep.directions().set(AbsoluteDirection.All.ordinal());
				steps.add(newStep);
			}
		}
		
		// **
		// ** TODO: Assumes that all coincident edges are orthogonal/adjacent.
		// **       Instead, perhaps only the straightest continuation should 
		// **       be orthogonal and all others diagonal?
		// **
		
		//-------------------------------------------------
		// Steps to vertices
		
		for (int v = 0; v < 2; v++)
		{
			final Vertex vertex = (v == 0) ? vertexA : vertexB;
				
			final Step newStep = new Step(this, vertex);

			newStep.directions().set(AbsoluteDirection.Orthogonal.ordinal());
			newStep.directions().set(AbsoluteDirection.Adjacent.ordinal());
			newStep.directions().set(AbsoluteDirection.All.ordinal());
			steps.add(newStep);
		}
		
		//-------------------------------------------------
		// Steps to faces
		
		final BitSet usedFaces = new BitSet();
	
		if (left != null)
		{
			// Left edge is orthogonal (perpendicular)
			usedFaces.set(left.id(), true);
				
			final Step newStep = new Step(this, left);

			newStep.directions().set(AbsoluteDirection.Orthogonal.ordinal());
			newStep.directions().set(AbsoluteDirection.Adjacent.ordinal());
			newStep.directions().set(AbsoluteDirection.All.ordinal());
			steps.add(newStep);
		}
		
		if (right != null)
		{
			// Right edge is orthogonal (perpendicular)
			usedFaces.set(right.id(), true);
				
			final Step newStep = new Step(this, right);
			
			newStep.directions().set(AbsoluteDirection.Orthogonal.ordinal());
			newStep.directions().set(AbsoluteDirection.Adjacent.ordinal());
			newStep.directions().set(AbsoluteDirection.All.ordinal());
			steps.add(newStep);
		}
		
		// Add faces coincident with end points
		for (int v = 0; v < 2; v++)
		{
			final Vertex vertex = (v == 0) ? vertexA : vertexB;
			for (final Face face : vertex.faces())
			{
				if (usedFaces.get(face.id()))
					continue;
				
				usedFaces.set(face.id(), true);
				
				final Step newStep = new Step(this, face);
				
				newStep.directions().set(AbsoluteDirection.Orthogonal.ordinal());
				newStep.directions().set(AbsoluteDirection.Adjacent.ordinal());
				newStep.directions().set(AbsoluteDirection.All.ordinal());
				steps.add(newStep);
			}
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("Edge[" + id + "]: " + vertexA.id() + " => " + vertexB.id());
		
		if (left != null)
			sb.append(" L=" + left.id());
		
		if (right != null)
			sb.append(" R=" + right.id());
				
		sb.append(" " + properties);

//		if (curved)
//			sb.append(" curved");

		if (tangentA != null)
			sb.append(" " + tangentA);
		
		if (tangentB != null)
			sb.append(" " + tangentB);

		sb.append(" \"" + situation.label() + "\"");

		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
}
