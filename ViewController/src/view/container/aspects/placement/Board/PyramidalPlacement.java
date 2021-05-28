package view.container.aspects.placement.Board;

import bridge.Bridge;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class PyramidalPlacement extends BoardPlacement
{
	public PyramidalPlacement(final Bridge bridge, final BoardStyle containerStyle) 
	{
		super(bridge, containerStyle);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void calculateCellRadius()
	{
		super.calculateCellRadius();
		setCellRadius(cellRadius*1.4);
	}
	
	//-------------------------------------------------------------------------
	
//	@Override
//	public void calculateAverageCellRadius()
//	{
//		final List<Cell> groundVertices = new ArrayList<Cell>();
//
//		for (final Cell cell : topology().cells())
//			if (cell.layer() == 0)
//				groundVertices.add(cell);
//
//		double min = 1.0;
//		for (int i = 0; i < groundVertices.size(); i++)
//		{
//			final Point2D.Double vi = new Point2D.Double(groundVertices.get(i).centroid().getX(),
//					groundVertices.get(i).centroid().getY());
//			for (int j = i + 1; j < groundVertices.size(); j++)
//			{
//				final Point2D.Double vj = new Point2D.Double(groundVertices.get(j).centroid().getX(),
//						groundVertices.get(j).centroid().getY());
//				final double dx = vi.x - vj.x;
//				final double dy = vi.y - vj.y;
//				final double dist = Math.sqrt(dx * dx + dy * dy);
//				if (min > dist)
//					min = dist;
//			}
//		}
//		setCellRadius(min / 2);
//	}
	
	//-------------------------------------------------------------------------
	
}
