package view.container;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.List;

import game.equipment.container.Container;
import game.types.board.SiteType;
import other.context.Context;
import other.topology.Cell;
import other.topology.Edge;
import other.topology.Topology;
import other.topology.TopologyElement;
import other.topology.Vertex;
import util.PlaneType;

/**
 * Something to be drawn.
 * View part of MVC for equipment.
 * @author Matthew.Stephenson and cambolbro
 */
public interface ContainerStyle
{
	/**
	 * Renders the string description of SVG graphic to a graphics plane.
	 */
	void render(final PlaneType plane, final Context context);
	
	/**
	 * Draws specified layer to the supplied graphics context.
	 */
	void draw(Graphics2D g2d, final PlaneType plane, final Context context);

	/**
	 * Sets the dimensions of the container, set by the view that it is to be placed in.
	 * @param context
	 * @param rectangle
	 */
	void setPlacement(final Context context, final Rectangle rectangle);
	
	//-------------------------------------------------------------------------
	
	/**
	 * Get the SVG image for the container (i.e. the board image).
	 */
	String containerSVGImage();
	
	/**
	 * Get the SVG image for the graph of the container.
	 */
	String graphSVGImage();
	
	/**
	 * Get the SVG image for the dual of the container.
	 */
	String dualSVGImage();
	
	/**
	 * Get the cells of the graph that should be visible.
	 */
	List<Cell> drawnCells();
	
	/**
	 * Get the edges of the graph that should be visible.
	 */
	List<Edge> drawnEdges();
	
	/**
	 * Get the vertices of the graph that should be visible.
	 */
	List<Vertex> drawnVertices();
	
	/**
	 * Get all graph elements that should be visible.
	 */
	List<TopologyElement> drawnGraphElements();
	
	/**
	 * Return the visible graph element defined by the index and type specified.
	 * @param index
	 * @param graphElementType
	 */
	TopologyElement drawnGraphElement(int index, SiteType graphElementType);
	
	/**
	 * Get the placement of the container.
	 */
	Rectangle placement();
	
	/**
	 * Get the placement of the container.
	 */
	Rectangle unscaledPlacement();
	
	/**
	 * Get the cell radius of the container (between 0 and 1).
	 */
	double cellRadius();
	
	/**
	 * Get the cell radius of the container (screen position).
	 */
	int cellRadiusPixels();
	
	/**
	 * Get the screen position for a specified world position.
	 */
	Point screenPosn(final Point2D posn);

	/**
	 * Get the scale of the container.
	 */
	double containerScale();
	
	/**
	 * Get the zoom of the container.
	 */
	double containerZoom();

	/**
	 * Get the container style specific piece scale.
	 */
	double pieceScale();
	
	/**
	 * Get the graph of the container for this style.
	 */
	Topology topology();

	/**
	 * Draw the puzzle value on the graphics object.
	 */
	void drawPuzzleValue(int puzzleValue, int site, Context context, Graphics2D graphics, Point point, int buttonSize);

	/**
	 * If there is no minimum selection distance for a piece.
	 */
	boolean ignorePieceSelectionLimit();

	/**
	 * The container associated with this style
	 */
	Container container();

	int maxDim();

}
