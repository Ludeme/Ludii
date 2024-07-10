package view.container.aspects.designs.board;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Ellipse2D;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import other.context.Context;
import view.container.aspects.designs.BoardDesign;
import view.container.aspects.placement.Board.TablePlacement;
import view.container.styles.board.TableStyle;

/**
 * Design for Table board.
 * 
 * @author Eric.Piette
 */
public class TableDesign extends BoardDesign
{
	/** The style of the table board. */
	private final TableStyle tableStyle;

	/** The placement in a table board. */
	private final TablePlacement tablePlacement;
	
	//-------------------------------------------------------------------------
	
	public TableDesign(final TableStyle boardStyle, final TablePlacement boardPlacement)
	{
		super(boardStyle, boardPlacement);
		tableStyle = boardStyle;
		tablePlacement = boardPlacement;
	}
	
	//-------------------------------------------------------------------------

	private final Color[] boardColours =
	{
			new Color(153, 76, 0), // base
			new Color(223, 178, 110), // frame
	};
	
	//-------------------------------------------------------------------------
	
	@Override
	public String createSVGImage(final Bridge bridge, final Context context)
	{
		boardPlacement.customiseGraphElementLocations(context);

		// Board image
		final SVGGraphics2D g2d = boardStyle.setSVGRenderingValues();
		setStrokesAndColours
		(
			bridge,
			context,
			new Color(120, 190, 240),
			new Color(125, 75, 0),
			new Color(210, 230, 255),
			null,
			null,
			null,
			null,
			null,
			new Color(0, 0, 0),
			Math.max(1, (int) (0.0025 * boardStyle.placement().width + 0.5)),
			(int) (2.0 * Math.max(1, (int) (0.0025 * boardStyle.placement().width + 0.5)))
		);
		drawTableBoard(g2d);

		return g2d.getSVGDocument();
	}

	//-------------------------------------------------------------------------

	/**
	 * Draws the board design.
	 */
	void drawTableBoard(final Graphics2D g2d)
	{
		final Point pt0 = screenPosn(topology().vertices().get(0).centroid());
		final Point pt1 = screenPosn(topology().vertices().get(1).centroid());
		final int off = pt1.x - pt0.x;
		final int unit = off;

		final Point ptBottomLeftRight = screenPosn(topology().vertices().get(tablePlacement.homeSize() - 1).centroid());
		final Point ptBottomRightLeft = screenPosn(topology().vertices().get(tablePlacement.homeSize()).centroid());
		final Point ptBottomRightRight = screenPosn(topology().vertices().get(tablePlacement.homeSize() * 2 - 1).centroid());
		final Point ptTopLeftLeft = screenPosn(topology().vertices().get(tablePlacement.homeSize() * 2).centroid());
		final Point ptTopRightLeft = screenPosn(topology().vertices().get(tablePlacement.homeSize() * 3).centroid());

		final int pr = (int) (unit * 0.5); // half an unit.
		final int borderX = (int) (unit * 0.2); // Size border.
		final int borderY = unit * 0; // Size border.
		final int diameterCircle = unit; // The diameter of the circle for the pieces.
		final int gapYCircle = (int) (diameterCircle * 0.7); // Gap y vertex and centre circle.

		final int topLeftLeftX = ptTopLeftLeft.x - pr;
		final int topLeftLeftY = ptTopLeftLeft.y - pr;

		final int topRightLeftX = ptTopRightLeft.x - pr;
		final int topRightLeftY = ptTopRightLeft.y - pr;

		final int bottomLeftRightX = ptBottomLeftRight.x + pr;
		final int bottomLeftRightY = ptBottomLeftRight.y + pr;
		final int bottomRightLeftX = ptBottomRightLeft.x + pr;

		final int bottomRightRightX = ptBottomRightRight.x + pr;
		final int bottomRightRightY = ptBottomRightRight.y + pr;

		final int topLeftBorderX = topLeftLeftX - borderX;
		final int topLeftBorderY = topLeftLeftY - borderY;

		final int bottomRightBorderX = bottomRightRightX + borderX;
		final int bottomRightBorderY = bottomRightRightY + borderY;

		// Draw the base of the board (rectangle)
		g2d.setColor(boardColours[1]);
		g2d.fillRect(topLeftBorderX, topLeftBorderY, Math.abs(bottomRightBorderX - topLeftBorderX),
				Math.abs(bottomRightBorderY - topLeftBorderY));

		// Draw middle of each side without the space for the circles
		g2d.setColor(boardColours[0]);
		g2d.fillRect(topLeftLeftX, topLeftLeftY + gapYCircle, Math.abs(bottomLeftRightX - topLeftLeftX),
				Math.abs((bottomLeftRightY - gapYCircle) - (topLeftLeftY + gapYCircle)));
		g2d.fillRect(topRightLeftX, topRightLeftY + gapYCircle, Math.abs(bottomRightRightX - topRightLeftX),
				Math.abs((bottomRightRightY - gapYCircle) - (topRightLeftY + gapYCircle)));

		// Draw gap in the middle bar in the middle.
		final int bottomMiddleY = bottomLeftRightY - (int) (Math.abs(topRightLeftY - bottomLeftRightY) * 0.65);
		final int sizeXMiddle = Math.abs(bottomLeftRightX - bottomRightLeftX);
		final int sizeYMiddle = (int) Math.abs(((bottomRightRightY - topLeftLeftY) * 0.35));
		g2d.fillRect(bottomLeftRightX, bottomMiddleY,
				sizeXMiddle, sizeYMiddle);

		g2d.setColor(boardColours[1]);
		final double offErrorMiddleCircle = 1.025;
		final Ellipse2D.Double topMiddleCircle = new Ellipse2D.Double(bottomLeftRightX,
				bottomLeftRightY - (int) (Math.abs(topRightLeftY - bottomLeftRightY) * 0.7),
				diameterCircle * offErrorMiddleCircle, diameterCircle * offErrorMiddleCircle);
		g2d.fill(topMiddleCircle);
		final Ellipse2D.Double bottomMiddleCircle = new Ellipse2D.Double(bottomLeftRightX,
				bottomLeftRightY - (int) (Math.abs(topRightLeftY - bottomLeftRightY) * 0.35),
				diameterCircle * offErrorMiddleCircle, diameterCircle * offErrorMiddleCircle);
		g2d.fill(bottomMiddleCircle);

		// Draw the circles
		g2d.setColor(boardColours[0]);
		final double offErrorCircle = 0.99;
		final int halfSize = topology().vertices().size() / 2;
		for (int n = 0; n < halfSize; n++)
		{
			final Point ptVertex = screenPosn(topology().vertices().get(n).centroid());
			final Ellipse2D.Double circle = new Ellipse2D.Double(ptVertex.x - pr, ptVertex.y - gapYCircle,
					diameterCircle * offErrorCircle, diameterCircle * offErrorCircle);
			g2d.fill(circle);
		}
		for (int n = halfSize; n < halfSize * 2; n++)
		{
			final Point ptVertex = screenPosn(topology().vertices().get(n).centroid());
			final Ellipse2D.Double circle = new Ellipse2D.Double(ptVertex.x - pr, ptVertex.y - gapYCircle / 2,
					diameterCircle * offErrorCircle, diameterCircle * offErrorCircle);
			g2d.fill(circle);
		}

	}

	public TableStyle getTableStyle()
	{
		return tableStyle;
	}
	
}
