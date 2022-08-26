package view.container.aspects.placement;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.List;

import bridge.Bridge;
import game.equipment.container.Container;
import game.types.board.SiteType;
import main.math.MathRoutines;
import other.context.Context;
import other.topology.Cell;
import other.topology.Edge;
import other.topology.Topology;
import other.topology.Vertex;
import util.GraphUtil;
import view.container.BaseContainerStyle;

public class ContainerPlacement 
{
	protected BaseContainerStyle containerStyle;
	
	//-------------------------------------------------------------------------
	
	/** the scale of the container, relative to the size of the view it is placed on */
	protected double containerScale = 1.0;
	
	/** Average distance from cell centroid to edge midpoints, in range 0..1. */
	protected double cellRadius = 0;
	
	/** cell size in pixels */
	private int cellRadiusPixels;
	
	/** location and dimensions of this container (in pixels) */
	protected Rectangle placement;
	
	/** Size of the placement before board scaling (i.e. the size of the view placement). */
	private Rectangle unscaledPlacement;
	
	protected Bridge bridge;
	
	//-------------------------------------------------------------------------
	
	public ContainerPlacement(final Bridge bridge, final BaseContainerStyle containerStyle) 
	{
		this.containerStyle = containerStyle;
		this.bridge = bridge;
		calculateCellRadius();
	}

	//-------------------------------------------------------------------------

	public void setPlacement(final Context context, final Rectangle placement)
	{
		setUnscaledPlacement(placement);
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Calculates average distance from cell centroid to edge midpoints, in range 0..1.
	 */
	public void calculateCellRadius()
	{
		double min = 1.0;
		
		if (container().defaultSite() == SiteType.Cell)
		{
			final List<Cell> cells = topology().cells();
			if (cells.size() > 0)
			{
				for (final Cell cell : cells)
				{
					final double acc = GraphUtil.calculateCellRadius(cell);
					if (acc < min)
						min = acc;
				}
			}
		}
		else
		{
			final List<Vertex> vertices = topology().vertices();
			if (vertices.size() > 0)
			{
				for (final Vertex vertex : vertices)
					for (final Vertex v : vertex.neighbours())
					{
						final double dist = MathRoutines.distance(v.centroid(), vertex.centroid()) * 0.5;
						if (dist > 0 && dist < min)
							min = dist;					
					}
			}
		}
	
			
		if (min > 0.0 && min < 1.0)
		{
			setCellRadius(min);
		}
		else // in that case the graph does not have any edges (e.g. Hounds and Jackals)
		{
			min = 1.0;
			for (int i = 0; i < topology().vertices().size(); i++)
			{
				final Point2D.Double vi = new Point2D.Double(topology().vertices().get(i).centroid().getX(),
															 topology().vertices().get(i).centroid().getY());
				for (int j = i + 1; j < topology().vertices().size(); j++)
				{
					final Point2D.Double vj = new Point2D.Double(topology().vertices().get(j).centroid().getX(),
																 topology().vertices().get(j).centroid().getY());
					final double dx = vi.x - vj.x;
					final double dy = vi.y - vj.y;
					final double dist = Math.sqrt(dx * dx + dy * dy);

					if (min > dist)
						min = dist;
				}
			}
			setCellRadius(min / 2);
		}
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Convert a normalised position into the corresponding position (pixel) in the app view.
	 */
	public Point screenPosn(final Point2D posn)
	{
		try
		{
			final Point screenPosn = new Point();
			screenPosn.x = (int) (placement.x + posn.getX()*placement.width);
			screenPosn.y = (int) ((placement.getY()*2 + placement.height) - (placement.y + posn.getY()*placement.height));
			return screenPosn;
		}
		catch (final Exception e)
		{
			return new Point((int)posn.getX(), (int)posn.getY());
		}
	}
	
	//-------------------------------------------------------------------------
	
	public double cellRadius()
	{
		return cellRadius;
	}
	
	public int cellRadiusPixels()
	{
		return cellRadiusPixels;
	}
	
	public final Rectangle placement() 
	{
		return placement;
	}
	
	public void setCellRadius(final double cellRadius) 
	{
		this.cellRadius = cellRadius;
	}
	
	public Rectangle unscaledPlacement() 
	{
		return unscaledPlacement;
	}

	public void setUnscaledPlacement(final Rectangle unscaledPlacement) 
	{
		this.unscaledPlacement = unscaledPlacement;
	}
	
	public final double containerScale() 
	{
		return containerScale;
	}
	
	public Topology topology()
	{
		return containerStyle.topology();
	}
	
	public Container container()
	{
		return containerStyle.container();
	}

	public void setCellRadiusPixels(final int cellRadiusPixels) {
		this.cellRadiusPixels = cellRadiusPixels;
	}
	
	@SuppressWarnings("static-method")
	public double containerZoom()
	{
		return 1.0;
	}
	
	public List<Cell> drawnCells()
	{
		return topology().cells();
	}
	
	public List<Edge> drawnEdges()
	{
		return topology().edges();
	}
	
	public List<Vertex> drawnVertices()
	{
		return topology().vertices();
	}

}
