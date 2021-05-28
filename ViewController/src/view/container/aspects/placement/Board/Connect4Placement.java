package view.container.aspects.placement.Board;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.List;

import bridge.Bridge;
import game.types.board.SiteType;
import other.context.Context;
import other.topology.Cell;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class Connect4Placement extends BoardPlacement
{
	/** Number of rows in the connect-4 board. */
	private final int connect4Rows = 6;
	
	//-------------------------------------------------------------------------
	
	public Connect4Placement(final Bridge bridge, final BoardStyle containerStyle) 
	{
		super(bridge, containerStyle);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void setPlacement(final Context context, final Rectangle placement)
	{
		super.setCustomPlacement(context, placement, new Point2D.Double(0.5, 0.5), 1.0);
		setCellLocations(placement.width, topology().cells());
	}
	
	//-------------------------------------------------------------------------

	public void setCellLocations(final int pixels, final List<Cell> cells)
	{
		final int cols = topology().columns(SiteType.Cell).size();
		final int rows = connect4Rows;
		
		final int u = pixels / (cols + 1);

		final int x0 = pixels / 2 - (int) (0.5 * cols * u + 0.5);
		final int y0 = pixels / 2 - (int) (0.5 * rows * u + 0.5);

		for (int n = 0; n < cols; n++)
		{
			final Cell cell = cells.get(n);

			final int row = 0; // rows - 1;
			final int col = n;

			final int x = x0 + col * u + u / 2;
			final int y = y0 + row * u + u / 2;

			cell.setCentroid(x / (double) pixels, y / (double) pixels, 0);
			topology().cells().get(cell.index()).setCentroid(x / (double) pixels, y / (double) pixels, 0);
		}
	}
	
	//-------------------------------------------------------------------------
	
	public int connect4Rows()
	{
		return connect4Rows;
	}

}
