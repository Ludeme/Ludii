package view.container.aspects.axes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import bridge.Bridge;
import other.topology.AxisLabel;
import other.topology.TopologyElement;
import util.StringUtil;
import view.container.styles.BoardStyle;

/**
 * Board axis properties.
 * 
 * @author Matthew.Stephenson
 */
public class BoardAxis extends ContainerAxis
{
	protected BoardStyle boardStyle;
	
	//-------------------------------------------------------------------------
	
	public BoardAxis(final BoardStyle boardStyle)
	{
		this.boardStyle = boardStyle;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void drawAxes(final Bridge bridge, final Graphics2D g2d)
	{
		final List<AxisLabel> axisLabels = getAxisLabels();
		
		final Font oldFont = g2d.getFont();

		g2d.setFont(bridge.settingsVC().displayFont());
		g2d.setColor(new Color(0, 0, 0));

		for (final AxisLabel al : axisLabels)
		{
			final String label = al.label();
			final Point drawPosn = boardStyle.screenPosn(al.posn());
			StringUtil.drawStringAtPoint(g2d, label, null, drawPosn, true);
		}

		g2d.setFont(oldFont);			
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Determine the axis labels for the board.
	 */
	protected List<AxisLabel> getAxisLabels() 
	{
		final int numCols = boardStyle.container().topology().columns(boardStyle.container().defaultSite()).size();
		final int numRows = boardStyle.container().topology().rows(boardStyle.container().defaultSite()).size();
		final double cellRadius = boardStyle.cellRadius();
		
		final List<AxisLabel> axisLabels = new ArrayList<>();

		double minX = 9999.9;
		double minY = 9999.9;
		double maxX = -9999.9;
		double maxY = -9999.9;

		for (final TopologyElement v : boardStyle.topology().getGraphElements(boardStyle.container().defaultSite()))
		{
			if (v.centroid().getX() < minX)
				minX = v.centroid().getX();
			if (v.centroid().getY() < minY)
				minY = v.centroid().getY();
			if (v.centroid().getX() > maxX)
				maxX = v.centroid().getX();
			if (v.centroid().getY() > maxY)
				maxY = v.centroid().getY();
		}

		if (boardStyle.container().topology().numEdges() == 4)
		{
			final int[] dim = {numRows, numCols};
			final double u = cellRadius * 2;

			// Create axis labels
			axisLabels.clear();

			for (int row = 0; row < dim[0]; row++)
			{
				final String label = String.format("%d", Integer.valueOf((row + 1)));

				final double x = minX - u * 3/4;
				final double y = minY + u * (row);

				final AxisLabel axisLabel = new AxisLabel(label, x, y);
				axisLabels.add(axisLabel);
			}

			for (int col = 0; col < dim[1]; col++)
			{
				final String label = String.format("%c", Character.valueOf((char) ('A' + col)));

				final double x = minX + u * (col);
				final double y = minY - u * 3/4;

				final AxisLabel axisLabel = new AxisLabel(label, x, y);
				axisLabels.add(axisLabel);
			}
		}
		
		return axisLabels;
	}
	
}
