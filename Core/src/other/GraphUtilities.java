package other;

import annotations.Hide;
import game.types.board.SiteType;
import other.topology.Cell;
import other.topology.Edge;
import other.topology.TopologyElement;
import other.topology.Vertex;

/**
 * Graph utilities methods.
 * 
 * @author Eric.Piette
 */
@Hide
public final class GraphUtilities
{
	/** Utility class - do not construct */
	private GraphUtilities()
	{
		// Nothing to do
	}

	/**
	 * To add a neighbour element to a graph element.
	 * 
	 * @param type      The type of the graph element.
	 * @param element   The element.
	 * @param neighbour The neighbour to add.
	 */
	public static void addNeighbour(final SiteType type, final TopologyElement element, final TopologyElement neighbour)
	{
		switch (type)
		{
		case Cell:
			((Cell) element).neighbours().add((Cell) neighbour);
			break;
		case Vertex:
			((Vertex) element).neighbours().add((Vertex) neighbour);
			break;
		case Edge:
			((Edge) element).neighbours().add((Edge) neighbour);
			break;
		default:
			break;
		}
	}

	/**
	 * To add an adjacent element to a graph element.
	 * 
	 * @param type     The type of the graph element.
	 * @param element  The element.
	 * @param adjacent The adjacent element to add.
	 */
	public static void addAdjacent(final SiteType type, final TopologyElement element, final TopologyElement adjacent)
	{
		switch (type)
		{
		case Cell:
			((Cell) element).adjacent().add((Cell) adjacent);
			break;
		case Vertex:
			((Vertex) element).adjacent().add((Vertex) adjacent);
			break;
		case Edge:
			break;
		default:
			break;
		}
	}

	/**
	 * To add an orthogonal element to a graph element.
	 * 
	 * @param type       The type of the graph element.
	 * @param element    The element.
	 * @param orthogonal The orthogonal element to add.
	 */
	public static void addOrthogonal(final SiteType type, final TopologyElement element, final TopologyElement orthogonal)
	{
		switch (type)
		{
		case Cell:
			((Cell) element).orthogonal().add((Cell) orthogonal);
			break;
		case Vertex:
			((Vertex) element).orthogonal().add((Vertex) orthogonal);
			break;
		case Edge:
			break;
		default:
			break;
		}
	}

	/**
	 * To add an diagonal element to a graph element.
	 * 
	 * @param type     The type of the graph element.
	 * @param element  The element.
	 * @param diagonal The diagonal element to add.
	 */
	public static void addDiagonal(final SiteType type, final TopologyElement element, final TopologyElement diagonal)
	{
		switch (type)
		{
		case Cell:
			((Cell) element).diagonal().add((Cell) diagonal);
			break;
		case Vertex:
			((Vertex) element).diagonal().add((Vertex) diagonal);
			break;
		case Edge:
			break;
		default:
			break;
		}
	}

	/**
	 * To add an off element to a graph element.
	 * 
	 * @param type    The type of the graph element.
	 * @param element The element.
	 * @param off     The off element to add.
	 */
	public static void addOff(final SiteType type, final TopologyElement element, final TopologyElement off)
	{
		switch (type)
		{
		case Cell:
			((Cell) element).off().add((Cell) off);
			break;
		case Vertex:
			((Vertex) element).off().add((Vertex) off);
			break;
		case Edge:
			break;
		default:
			break;
		}
	}

}
