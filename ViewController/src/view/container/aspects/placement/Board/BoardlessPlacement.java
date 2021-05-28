package view.container.aspects.placement.Board;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import bridge.Bridge;
import game.equipment.component.Component;
import main.Constants;
import other.context.Context;
import other.state.State;
import other.topology.Cell;
import other.topology.Edge;
import other.topology.Vertex;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class BoardlessPlacement extends BoardPlacement
{
	// Various variables representing visual information about the container.
	private State currentState;
	private long currentStateHash = -1;
	private List<Cell> zoomedCells;
	private List<Edge> zoomedEdges;
	private List<Vertex> zoomedVertices;
	protected double zoom = 1.0;
	
	//-------------------------------------------------------------------------
	
	public BoardlessPlacement(final Bridge bridge, final BoardStyle containerStyle) 
	{
		super(bridge, containerStyle);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public List<Cell> drawnCells()
	{
		final List<Cell> drawnCells = new ArrayList<Cell>();
		for (final Cell vOri : topology().cells())
		{			
			if (currentState.containerStates()[0].isPlayable(vOri.index()) || currentState.containerStates()[0].isOccupied(vOri.index()))
			{
				final Cell newCell = zoomedCells.get(vOri.index());
				final ArrayList<Vertex> newVertices = new ArrayList<>();
				for (final Vertex v : vOri.vertices())
				{
					newVertices.add(zoomedVertices.get(v.index()));
				}
				newCell.setVertices(newVertices);
				drawnCells.add(newCell);
			}
			else
			{
				final Cell newCell = new Cell(vOri.index(), Constants.UNDEFINED, Constants.UNDEFINED,
						Constants.UNDEFINED);
				drawnCells.add(newCell);
			}
		}
		
		return drawnCells;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public List<Edge> drawnEdges()
	{
		final List<Edge> drawnEdges = new ArrayList<Edge>();
		
		for (final Edge eOri : topology().edges())
		{	
			for (final Cell c : eOri.cells())
			{
				if 
				(
					currentState.containerStates()[0].isPlayable(c.index()) 
					|| 
					currentState.containerStates()[0].isOccupied(c.index())
				)
				{
					final Edge e = zoomedEdges.get(eOri.index());
					drawnEdges.add(e);
					continue;
				}
			}
		}
		
		return drawnEdges;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public List<Vertex> drawnVertices()
	{
		final List<Vertex> drawnVertices = new ArrayList<Vertex>();
		
		for (final Vertex fOri : topology().vertices())
		{
			for (final Cell v : fOri.cells())
			{
				if 
				(
					currentState.containerStates()[0].isPlayable(v.index()) 
					|| 
					currentState.containerStates()[0].isOccupied(v.index())
				)
				{
					drawnVertices.add(zoomedVertices.get(fOri.index()));
					break;
				}
			}
		}
		
		return drawnVertices;
	}
	
	//-------------------------------------------------------------------------

	/** 
	 * Apply zoomAmount onto a specified point, based on the provided center point.
	 */
	public static Point2D.Double applyZoomToPoint(final Point2D point, final double zoomAmount, final Point2D.Double centerPoint)
	{
		final Point2D.Double zoomedPoint = new Point2D.Double();
		zoomedPoint.x = (0.5 + (point.getX() - centerPoint.x) * zoomAmount);
		zoomedPoint.y = (0.5 + (point.getY() - centerPoint.y) * zoomAmount);
		return zoomedPoint;
	}

	//-------------------------------------------------------------------------

	/**
	 * Set the locations for the zoomedCells, zoomedEdges and zoomedVertices of the container graph.
	 */
	public double setZoomedLocations(final Context context)
	{
		int numberOccupiedCells = 0;

		double minX = 99999;
		double minY = 99999;
		double maxX = -99999;
		double maxY = -99999;

		if (currentState != null)
		{
			for (final Cell vertex : topology().cells())
			{					
				if (currentState.containerStates()[0].isPlayable(vertex.index()) || currentState.containerStates()[0].isOccupied(vertex.index()))
				{
					numberOccupiedCells++;
					if (vertex.centroid().getX() < minX)
						minX = vertex.centroid().getX();
					if (vertex.centroid().getX() > maxX)
						maxX = vertex.centroid().getX();
					if (vertex.centroid().getY() < minY)
						minY = vertex.centroid().getY();
					if (vertex.centroid().getY() > maxY)
						maxY = vertex.centroid().getY();
				}
			}
		}
		
		// Used to make sure there is some padding on either side of the board.
		minX = minX - cellRadius();
		minY = minY - cellRadius();
		maxX = maxX + cellRadius();
		maxY = maxY + cellRadius();

		double newZoom = 1.0;
		double centerPointX = 0.5;
		double centerPointY = 0.5;

		if (numberOccupiedCells > 0)
		{
			final double boardZoomX = 1.0 / (maxX - minX);
			final double boardZoomY = 1.0 / (maxY - minY);

			newZoom = Math.min(boardZoomX, boardZoomY);

			centerPointX = (maxX + minX) /2.0;
			centerPointY = (maxY + minY) /2.0;
		}
		
		int largestPieceWalk = 1;
		for (final Component component : context.components())
		{
			if (component != null && component.isLargePiece())
			{
				final int stepsForward = component.maxStepsForward();
				if (largestPieceWalk < stepsForward)
					largestPieceWalk = stepsForward;
			}
		}
		
		final double maxZoom = 10.0 / largestPieceWalk;
		if (newZoom > maxZoom)
			newZoom = maxZoom;
		
		final List<Cell> graphCells = topology().cells();
		final List<Edge> graphEdges = topology().edges();
		final List<Vertex> graphVertices = topology().vertices();
		
		if (zoomedVertices == null || zoomedEdges == null || zoomedCells == null)
		{
			zoomedCells = new ArrayList<>(topology().cells().size());
			zoomedEdges = new ArrayList<>(topology().edges().size());
			zoomedVertices = new ArrayList<>(topology().vertices().size());
		}
		else
		{
			zoomedCells.clear();
			zoomedEdges.clear();
			zoomedVertices.clear();
		}

		for (int i = 0; i < graphCells.size(); i++)
		{
			final Cell cell = graphCells.get(i);
			final Point2D.Double zoomedPoint = applyZoomToPoint(cell.centroid(), newZoom, new Point2D.Double(centerPointX, centerPointY));
			final Cell zoomedCell = new Cell(i, zoomedPoint.x, zoomedPoint.y, 0);
			zoomedCell.setOrthogonal(cell.orthogonal());
			zoomedCell.setDiagonal(cell.diagonal());
			zoomedCell.setOff(cell.off());
			zoomedCells.add(zoomedCell);
		}

		for (int i = 0; i < graphEdges.size(); i++)
		{
			final Edge edge = graphEdges.get(i);
			final Point2D.Double zoomedPointA = applyZoomToPoint(edge.vA().centroid(), newZoom, new Point2D.Double(centerPointX, centerPointY));
			final Point2D.Double zoomedPointB = applyZoomToPoint(edge.vB().centroid(), newZoom, new Point2D.Double(centerPointX, centerPointY));
			final Edge zoomedEdge = new Edge(i, new Vertex(edge.vA().index(), zoomedPointA.x, zoomedPointA.y, 0),
					new Vertex(edge.vB().index(), zoomedPointB.x, zoomedPointB.y, 0));
			zoomedEdges.add(zoomedEdge);
		}

		for (int i = 0; i < graphVertices.size(); i++)
		{
			
			final Vertex vertex = graphVertices.get(i);
			final Point2D.Double zoomedPoint = applyZoomToPoint(vertex.centroid(), newZoom, new Point2D.Double(centerPointX, centerPointY));
			final Vertex zoomedVertex = new Vertex(i, zoomedPoint.x, zoomedPoint.y, 0);
			zoomedVertices.add(zoomedVertex);
		}
	
		return newZoom;
	}

	//-------------------------------------------------------------------------

	/**
	 * Calculate the zoom amount for the container.
	 */
	public void calculateZoom(final Context context)
	{		
		currentState = context.state();
		if (currentStateHash != currentState.stateHash())
		{
			currentStateHash = currentState.stateHash();
			zoom = setZoomedLocations(context);
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Update the zoomed image of the game board.
	 */
	public void updateZoomImage(final Context context) 
	{
		calculateZoom(context);
		setCellRadiusPixels((int)(cellRadius() * placement().width * containerZoom()));
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public double containerZoom()
	{
		return zoom;
	}
	
	//-------------------------------------------------------------------------
	
}
