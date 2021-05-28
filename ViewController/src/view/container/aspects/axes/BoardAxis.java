package view.container.aspects.axes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import other.topology.AxisLabel;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

/**
 * Board axis properties.
 * 
 * @author Matthew.Stephenson
 */
public class BoardAxis extends ContainerAxis
{
	protected BoardStyle boardStyle;
	protected BoardPlacement boardPlacement;
	
	//-------------------------------------------------------------------------
	
	public BoardAxis(final BoardStyle boardStyle, final BoardPlacement boardPlacement)
	{
		this.boardStyle = boardStyle;
		this.boardPlacement = boardPlacement;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void drawAxes(final Graphics2D g2d)
	{
		final List<AxisLabel> axisLabels = getAxisLabels();
		
		final Font oldFont = g2d.getFont();

		g2d.setFont(new Font("Arial", Font.BOLD, 16));
			
		g2d.setColor(new Color(180, 180, 180));

		for (final AxisLabel al : axisLabels)
		{
			final String label = al.label();
			final Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(label, g2d);
			final Point drawPosn = boardStyle.screenPosn(al.posn());
			g2d.drawString(label, (int) (drawPosn.x - bounds.getWidth()/2), (int) (boardPlacement.unscaledPlacement().height - (drawPosn.y - bounds.getHeight()/2)));
		}

		g2d.setFont(oldFont);			
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Determine the axis labels for the board.
	 */
	protected List<AxisLabel> getAxisLabels() 
	{
//		final Shape shape = boardStyle.shape();
//		final double cellRadius = boardStyle.cellRadius();
//		
		final List<AxisLabel> axisLabels = new ArrayList<>();
//
//		double minX = 9999.9;
//		double minY = 9999.9;
//		double maxX = -9999.9;
//		double maxY = -9999.9;
//
//		for (final Cell v : boardStyle.topology().cells())
//		{
//			if (v.centroid().getX() < minX)
//				minX = v.centroid().getX();
//			if (v.centroid().getY() < minY)
//				minY = v.centroid().getY();
//			if (v.centroid().getX() > maxX)
//				maxX = v.centroid().getX();
//			if (v.centroid().getY() > maxY)
//				maxY = v.centroid().getY();
//		}
//
//		if (tiling.numberEdges() == 4)
//		{
//			final int[] dim = {shape.dim()[0], shape.dim()[1]};
//			final double u = cellRadius * 2;
//			
////			if (tiling instanceof PyramidalTiling)
////			{
////				dim[0] = dim[0] * 2 - 1;
////				dim[1] = dim[1] * 2 - 1;
////				u = u / 2;
////			}
//
//			// Create vertices for cell corners
//			final double x0 = minX;
//			final double y0 = minY;
//
//			// Create axis labels
//			axisLabels.clear();
//
//			final double ax0 = x0 - u * 3/4;
//			final double ay1 = y0 + u * dim[0] - u/4;
//
//			for (int row = 0; row < dim[0]; row++)
//			{
//
//				final String label = String.format("%d", Integer.valueOf((dim[0] - row)));
//
//				final double y = y0 + u * (row);
//
//				final AxisLabel axisLabel = new AxisLabel(label, ax0, y);
//				axisLabels.add(axisLabel);
//			}
//
//			for (int col = 0; col < dim[1]; col++)
//			{
//
//				final String label = String.format("%c", Character.valueOf((char) ('A' + col)));
//
//				final double x = x0 + u * (col);
//
//				final AxisLabel axisLabel = new AxisLabel(label, x, ay1);
//				axisLabels.add(axisLabel);
//			}
//		}
//
//		else if (tiling.numberEdges() == 6)
//		{
//			if (shape.type() == ShapeType.Rhombus)  //shape.isRhombus())
//			{
//				final int[] dim = shape.dim();
//				final double xDistance = Math.sqrt(3) * cellRadius;
//				final double yDistance = cellRadius;
//
//				axisLabels.clear();
//
//				for (int row = 0; row < dim[0]; row++)
//				{
//					final double y = 0.5 + dim[0] * 0.001 + yDistance * row + yDistance / 2; // not sure why 0.001 is needed? Rounding error maybe?
//					final double x = 0.5 + xDistance * row - dim[0] * xDistance;
//					final String label = String.format("%c", Character.valueOf((char) ('A' + row)));
//					final AxisLabel axisLabel = new AxisLabel(label, x, y);
//					axisLabels.add(axisLabel);
//				}
//
//				for (int col = 0; col < dim[1]; col++)
//				{
//					final double y = 0.5 + dim[1] * yDistance - yDistance * col - (dim[1] + 2) * yDistance + yDistance / 2;
//					final double x = 0.5 + xDistance * col - dim[1] * xDistance;
//					final String label = String.format("%d", Integer.valueOf(col + 1));
//					final AxisLabel axisLabel = new AxisLabel(label, x, y);
//					axisLabels.add(axisLabel);
//				}
//			}
//
//			else if (shape.type() == ShapeType.Hexagon)  //shape.isHexagon())
//			{
//				if (tiling.northOriented())
//				{
//					final int[] dim = { 0, 0 };
//					dim[0] = shape.dim()[0] * 2;
//					dim[1] = shape.dim()[1] * 2;
//					final double u = cellRadius * 2;
//					final double x0 = minX;
//					final double y0 = minY;
//					final double ax0 = x0 - 3 * u / 4;
//
//					axisLabels.clear();
//
//					for (int row = 0; row < dim[0] - 1; row++)
//					{
//						if (row < (dim[0] - 1) / 2)
//						{
//							final double y = y0 - (Math.sqrt(3.0) * u / 2 * row) + (Math.sqrt(3.0) * u / 2.0 * (dim[0] - 1) / 2) - u * 0.25 - (1.0 * u);
//							final double x = ax0 + row * u / 2 + u / 4;
//							final String label = String.format("%d", Integer.valueOf((row + 1)));
//							final AxisLabel axisLabel = new AxisLabel(label, x, y);
//							axisLabels.add(axisLabel);
//						}
//						else
//						{
//							final double y = y0 + u * 0.25 - (1.0 * u);
//							final double x = ax0 - ((dim[0] - 1) / 2) * u / 2 + row * u + u / 4;
//							final String label = String.format("%d", Integer.valueOf((row + 1)));
//							final AxisLabel axisLabel = new AxisLabel(label, x, y);
//							axisLabels.add(axisLabel);
//						}
//					}
//
//					for (int col = 0; col < dim[1] - 1; col++)
//					{
//						if (col < (dim[0] - 1) / 2)
//						{
//							final double y = y0 + (Math.sqrt(3.0) * u / 2 * col) + (Math.sqrt(3.0) * u / 2.0 * (dim[0] - 1) / 2) + u - (0.75 * u);
//							final double x = ax0 + col * u / 2 + u / 4;
//							final String label = String.format("%c", Character.valueOf((char) ('A' + col)));
//							final AxisLabel axisLabel = new AxisLabel(label, x, y);
//							axisLabels.add(axisLabel);
//						}
//						else
//						{
//							final double y = y0 + dim[0] * (Math.sqrt(3.0) * u / 2) - 0.25 * u - (0.75 * u);
//							final double x = ax0 - ((dim[0] - 1) / 2) * u / 2 + col * u + u / 4;
//							final String label = String.format("%c", Character.valueOf((char) ('A' + col)));
//							final AxisLabel axisLabel = new AxisLabel(label, x, y);
//							axisLabels.add(axisLabel);
//						}
//					}
//				}
//				else
//				{
//					final int[] dim = { 0, 0 };
//					dim[0] = shape.dim()[0] * 2;
//					dim[1] = shape.dim()[1] * 2;
//					final double u = cellRadius * 2;
//					final double x0 = minX;
//					final double y0 = minY;
//					final double widerUnit = u * (2.0 / Math.sqrt(3.0));
//					final double ax0 = x0 - 3 * u / 4;
//					final double ay1 = y0 + u * (dim[0] - 0.25);
//
//					axisLabels.clear();
//
//					for (int row = 1; row < dim[0]; row++)
//					{
//						if (row > (dim[0] - 1) / 2)
//						{
//							final double y = 0.5 - u / 2 * (dim[1] - 1) / 2 + u * row - u / 8 - u * dim[1] / 2;
//							final double x = ax0;
//							final String label = String.format("%d", Integer.valueOf((dim[0] - row)));
//							final AxisLabel axisLabel = new AxisLabel(label, x, y);
//							axisLabels.add(axisLabel);
//						}
//						else
//						{
//							final double y = y0 + u * (row) / 2 - u * 0.875;
//							final double x = ax0 - (row - dim[0] / 2) * (Math.sqrt(3) * u / 2.0) - u / 8;
//							final String label = String.format("%d", Integer.valueOf((dim[0] - row)));
//							final AxisLabel axisLabel = new AxisLabel(label, x, y);
//							axisLabels.add(axisLabel);
//						}
//					}
//
//					for (int col = 0; col < dim[1] - 1; col++)
//					{
//						final double y = ay1 - (u / 2.0) * Math.abs(col - (dim[0] - 1) / 2) - u;
//						final double x = x0 + widerUnit * 0.75 * (col);
//						final String label = String.format("%c", Character.valueOf((char) ('A' + col)));
//						final AxisLabel axisLabel = new AxisLabel(label, x, y);
//						axisLabels.add(axisLabel);
//					}
//				}
//			}
//		}
//		else if (tiling.numberEdges() == 3)
//		{
//			
//			if (shape.type() == ShapeType.Triangle)  //shape.isTriangle())
//			{
//				final int[] dim = {shape.dim()[0], shape.dim()[1]};	
//				final double DistanceX = cellRadius * 2 * Math.cos(Math.toRadians(30));
//				final double DistanceY = cellRadius + (cellRadius / Math.sin(Math.toRadians(30)));
//				
//				// Create vertices for cell corners
//				final double x0 = minX;
//				final double y0 = minY;
//				
//				final int colNumber = dim[1] * 2 - 1;
//				final int rowNumber = dim[0];
//	
//				// Create axis labels
//				axisLabels.clear();
//	
//				final double ax0 = x0 - DistanceX * 2;
//				final double ay1 = y0 + DistanceY * dim[0] - DistanceY/2;
//	
//				for (int row = 0; row < rowNumber; row++)
//				{
//					final String label = String.format("%d", Integer.valueOf((dim[0] - row)));
//	
//					final double y = y0 + DistanceY * (row);
//					final double x = ax0 + DistanceX * (dim[0] - row);
//	
//					final AxisLabel axisLabel = new AxisLabel(label, x, y);
//					axisLabels.add(axisLabel);
//				}
//	
//				for (int col = 0; col < colNumber; col++)
//				{
//					final String label = String.format("%c", Character.valueOf((char) ('A' + col)));
//	
//					final double x = x0 + DistanceX * (col);
//	
//					final AxisLabel axisLabel = new AxisLabel(label, x, ay1);
//					axisLabels.add(axisLabel);
//				}
//			}
//			else if (shape.type() == ShapeType.Hexagon)  //shape.isHexagon())
//			{
//				if (tiling.northOriented())
//				{
//					final int[] dim = { 0, 0 };
//					dim[0] = shape.dim()[0] * 2;
//					dim[1] = shape.dim()[1] * 2;
//					final double u = cellRadius * 2;
//					final double x0 = minX;
//					final double y0 = minY;
//					final double ax0 = x0 - 3 * u / 4;
//
//					axisLabels.clear();
//
//					for (int row = 0; row < dim[0] - 1; row++)
//					{
//						if (row < (dim[0] - 1) / 2)
//						{
//							final double y = y0 - (Math.sqrt(3.0) * u / 2 * row) + (Math.sqrt(3.0) * u / 2.0 * (dim[0] - 1) / 2) - u * 0.25 - (1.0 * u);
//							final double x = ax0 + row * u / 2 + u / 4;
//							final String label = String.format("%d", Integer.valueOf((row + 1)));
//							final AxisLabel axisLabel = new AxisLabel(label, x, y);
//							axisLabels.add(axisLabel);
//						}
//						else
//						{
//							final double y = y0 + u * 0.25 - (1.0 * u);
//							final double x = ax0 - ((dim[0] - 1) / 2) * u / 2 + row * u + u / 4;
//							final String label = String.format("%d", Integer.valueOf((row + 1)));
//							final AxisLabel axisLabel = new AxisLabel(label, x, y);
//							axisLabels.add(axisLabel);
//						}
//					}
//
//					for (int col = 0; col < dim[1] - 1; col++)
//					{
//						if (col < (dim[0] - 1) / 2)
//						{
//							final double y = y0 + (Math.sqrt(3.0) * u / 2 * col) + (Math.sqrt(3.0) * u / 2.0 * (dim[0] - 1) / 2) + u - (0.75 * u);
//							final double x = ax0 + col * u / 2 + u / 4;
//							final String label = String.format("%c", Character.valueOf((char) ('A' + col)));
//							final AxisLabel axisLabel = new AxisLabel(label, x, y);
//							axisLabels.add(axisLabel);
//						}
//						else
//						{
//							final double y = y0 + dim[0] * (Math.sqrt(3.0) * u / 2) - 0.25 * u - (0.75 * u);
//							final double x = ax0 - ((dim[0] - 1) / 2) * u / 2 + col * u + u / 4;
//							final String label = String.format("%c", Character.valueOf((char) ('A' + col)));
//							final AxisLabel axisLabel = new AxisLabel(label, x, y);
//							axisLabels.add(axisLabel);
//						}
//					}
//				}
//				else
//				{
//					// Not done yet
//				}
//			}
//		}
		
		return axisLabels;
	}
	
}
