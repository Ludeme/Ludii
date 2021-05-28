package view.container.aspects.axes.board;

import java.util.ArrayList;
import java.util.List;

import other.topology.AxisLabel;
import view.container.aspects.axes.BoardAxis;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class SurakartaAxis extends BoardAxis
{
	public SurakartaAxis(final BoardStyle boardStyle, final BoardPlacement boardPlacement) 
	{
		super(boardStyle, boardPlacement);
	}
	
	//-------------------------------------------------------------------------

	@Override
	protected List<AxisLabel> getAxisLabels() 
	{
//		final Tiling tiling = boardStyle.tiling();
//		final double cellRadius = boardStyle.cellRadius();
		
		final List<AxisLabel> axisLabels = new ArrayList<>();

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
//			final int[] dim =
//			{ boardStyle.container().topology().rows(SiteType.Vertex).size() - 1,
//					boardStyle.container().topology().columns(SiteType.Vertex).size() - 1 };
//			final double u = cellRadius * 2;
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
//				final String label = String.format("%d", Integer.valueOf((dim[0] - row)));
//				final double y = y0 + u * (row);
//				final AxisLabel axisLabel = new AxisLabel(label, ax0, y);
//				axisLabels.add(axisLabel);
//			}
//
//			for (int col = 0; col < dim[1]; col++)
//			{
//				final String label = String.format("%c", Character.valueOf((char) ('A' + col)));
//				final double x = x0 + u * (col);
//				final AxisLabel axisLabel = new AxisLabel(label, x, ay1);
//				axisLabels.add(axisLabel);
//			}
//		}
		
		return axisLabels;
	}
	
}
