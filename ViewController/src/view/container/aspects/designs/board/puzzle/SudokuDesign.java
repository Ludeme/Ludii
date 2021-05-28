package view.container.aspects.designs.board.puzzle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import metadata.graphics.util.PuzzleDrawHintType;
import other.context.Context;
import other.topology.Cell;
import other.topology.Edge;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class SudokuDesign extends PuzzleDesign
{
	public SudokuDesign(final BoardStyle boardStyle, final BoardPlacement boardPlacement) 
	{
		super(boardStyle, boardPlacement);
		drawHintType = PuzzleDrawHintType.TopLeft;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String createSVGImage(final Bridge bridge, final Context context)
	{
		final SVGGraphics2D g2d = boardStyle.setSVGRenderingValues();

		final float swRatio = 5 / 1000.0f;
		final float swThin  = Math.max(1, (int) (swRatio * boardStyle.placement().width + 0.5));
		final float swThick = 2 * swThin;

		setStrokesAndColours
		(
			bridge, 
			context,
			new Color(120, 190, 240),
			new Color(120, 190, 240),
			new Color(210, 230, 255),
			null,
			null,
			null,
			null,
			null,
			new Color(200,50,200),
			swThin,
			swThick
		);
		
		detectHints(context);
		fillCells(bridge, g2d, context);
		drawInnerCellEdges(g2d, context);
		drawOuterCellEdges(bridge, g2d, context);
		drawGridEdges(g2d, colorEdgesOuter, strokeThick());

		// draw inner sudoku regions (for killer sudoko)
		final float dash1[] = {6.0f};
		final BasicStroke dashed = new BasicStroke(strokeThin.getLineWidth(),BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,5.0f, dash1, 0.0f);
		
		drawRegions(g2d, context, colorSymbol(), dashed, hintRegions);

		return g2d.getSVGDocument();
	}

	//-------------------------------------------------------------------------

	/**
	 * Draws the edges of the board grid.
	 */
	protected void drawGridEdges(final Graphics2D g2d, final Color borderColor, final BasicStroke stroke)
	{
		final List<Cell> cells = topology().cells();
		
		g2d.setColor(borderColor);
		g2d.setStroke(stroke);
		final List<Edge> sudokuEdges = new ArrayList<>();
		final GeneralPath path = new GeneralPath();
		final double boardDimension = Math.sqrt(cells.size());
		final int lineInterval = (int) Math.sqrt(boardDimension);

		for (final Cell cell : cells)
		{
			for (final Edge edge : cell.edges())
			{
				// vertical lines
				final int columnValue = (cell.index()+1);
				if ( (columnValue%lineInterval == 0) && (columnValue%boardDimension != 0) )
				{
					if (edge.vA().centroid().getX() > cell.centroid().getX() && edge.vB().centroid().getX() > cell.centroid().getX())
					{
						sudokuEdges.add(edge);
					}
				}
				
				// horizontal lines
				final int rowLength = (int) Math.sqrt(cells.size());
				final int rowValue = ((cell.index())/rowLength);
				if ( (rowValue%lineInterval == (lineInterval-1)) && (rowValue%(boardDimension-1) != 0) )
				{
					if (edge.vA().centroid().getY() > cell.centroid().getY() && edge.vB().centroid().getY() > cell.centroid().getY())
					{
						sudokuEdges.add(edge);
					}
				}
			}
		}

		while (sudokuEdges.size() > 0)
		{
			Edge currentEdge = sudokuEdges.get(0);
			boolean nextEdgeFound = true;

			final Point2D va = currentEdge.vA().centroid();
			Point2D vb = currentEdge.vB().centroid();
			final Point vAPosn = screenPosn(va);
			Point vBPosn = screenPosn(vb);

			path.moveTo(vAPosn.x, vAPosn.y);

			while (nextEdgeFound == true)
			{
				nextEdgeFound = false;
				path.lineTo(vBPosn.x, vBPosn.y);
				sudokuEdges.remove(currentEdge);

				for (final Edge nextEdge : sudokuEdges)
				{
					if (Math.abs(vb.getX() - nextEdge.vA().centroid().getX()) < 0.0001 && Math.abs(vb.getY() - nextEdge.vA().centroid().getY()) < 0.0001)
					{
						nextEdgeFound = true;

						currentEdge = nextEdge;
						vb = currentEdge.vB().centroid();
						vBPosn = screenPosn(vb);

						break;
					}
					else if (Math.abs(vb.getX() - nextEdge.vB().centroid().getX()) < 0.0001 && Math.abs(vb.getY() - nextEdge.vB().centroid().getY()) < 0.0001)
					{
						nextEdgeFound = true;

						currentEdge = nextEdge;
						vb = currentEdge.vA().centroid();
						vBPosn = screenPosn(vb);

						break;
					}
				}
			}
		}
		g2d.draw(path);
	}
	
	//-------------------------------------------------------------------------
	
}
