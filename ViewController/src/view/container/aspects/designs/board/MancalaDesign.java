package view.container.aspects.designs.board;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import game.equipment.container.board.custom.MancalaBoard;
import game.types.board.StoreType;
import gnu.trove.list.array.TIntArrayList;
import metadata.graphics.util.HoleType;
import other.concept.Concept;
import other.context.Context;
import other.topology.Vertex;
import view.container.aspects.designs.BoardDesign;
import view.container.styles.BoardStyle;

/**
 * Graphics for Mancala boards.
 * 
 * @author cambolbro and Eric.Piette
 */
public class MancalaDesign extends BoardDesign
{
	public MancalaDesign(final BoardStyle boardStyle)
	{
		super(boardStyle, null);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String createSVGImage(final Bridge bridge, final Context context)
	{
		final SVGGraphics2D g2d = boardStyle.setSVGRenderingValues();

		final int swThin  = Math.max(1, (int)(0.001 * boardStyle.placement().width + 0.5));
		final int swThick = 2 * swThin;

		setStrokesAndColours
		(
			bridge,
			context,
			null,
			new Color(125, 75, 0),
			new Color(255, 220, 100),
			null,
			null,
			null,
			null,
			null,
			new Color(127, 100, 50),
			swThin,
			swThick
		);

		final Rectangle2D bounds = context.board().graph().bounds();
		final int numColumns = (context.board() instanceof MancalaBoard) ? ((MancalaBoard) context.board()).numColumns()
				: (int) (bounds.getWidth() - 0.5);
		final int numRows = (context.board() instanceof MancalaBoard) ? ((MancalaBoard) context.board()).numRows()
				: (int) (bounds.getHeight() + 0.5) + 1;
		
		final boolean withStore = (context.board() instanceof MancalaBoard)
				? !((MancalaBoard) context.board()).storeType().equals(StoreType.None)
				: true;
		
		final int[] specialHoles = context.metadata().graphics().sitesAsSpecialHoles();
		final HoleType type = context.metadata().graphics().shapeSpecialHole();

		final boolean circleTiling = context.game().booleanConcepts().get(Concept.CircleTiling.id());
		final boolean notMancalaBoard = !circleTiling && !(context.board() instanceof MancalaBoard);

		drawMancalaBoard(g2d, numRows, numColumns, withStore, circleTiling, new TIntArrayList(specialHoles), type, notMancalaBoard);

		return g2d.getSVGDocument();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Draw the Mancala board.
	 * 
	 * @param g2d             The graphics 2D.
	 * @param rows            The number of rows.
	 * @param cols            The number of columns.
	 * @param withStore       True if the board has storage for each player.
	 * @param circleTiling    True if the tiling used is circle.
	 * @param specialHoles    The sites which should be squares.
	 * @param type            The shape of the special holes.
	 * @param notMancalaBoard True if the board is not mancala and not a circle.
	 */
	void drawMancalaBoard
	(
		final Graphics2D g2d,
		final int rows,
		final int cols,
		final boolean withStore,
		final boolean circleTiling,
		final TIntArrayList specialHoles, 
		final HoleType type,
		final boolean notMancalaBoard
	)
	{
		final int indexHoleBL = (withStore) ? 1 : 0;
		final int indexHoleTR = (withStore) ? rows * cols : rows * cols - 1;
		final int indexHoleBR = (withStore) ? cols : cols - 1;
		final int indexHoleTL = indexHoleBR + 1 + (rows - 2) * cols;

		// Distance between pits in x direction
		final Point pt1 = screenPosn(topology().vertices().get(circleTiling ? 0 : indexHoleBL).centroid());
		final Point pt2 = screenPosn(topology().vertices().get(circleTiling ? 1 : (indexHoleBL + 1)).centroid());
		final int dx = Math.abs(pt2.x - pt1.x);

		final double radius = 0.666 * dx; // radius of rounded board ends
		Point pt = null;

		if (circleTiling)
		{
			// Compute the centre of the board.
			double sumX = 0.0;
			double sumY = 0.0;

			// Compute the radius of the board;
			double circleDx = 0.0;
			double circleDy = 0.0;

			for (final Vertex v : topology().vertices())
			{
				sumX += screenPosn(v.centroid()).getX();
				sumY += screenPosn(v.centroid()).getY();

				for (final Vertex v2 : topology().vertices())
				{
					final double currentDx = Math
							.abs(screenPosn(v.centroid()).getX() - screenPosn(v2.centroid()).getX());
					final double currentDy = Math
							.abs(screenPosn(v.centroid()).getY() - screenPosn(v2.centroid()).getY());
					if (currentDx > circleDx)
						circleDx = currentDx;
					if (currentDy > circleDy)
						circleDy = currentDy;
				}
			}

			final double centreX = sumX / topology().vertices().size();
			final double centreY = sumY / topology().vertices().size();
			circleDx = circleDx / 2 + radius;
			circleDy = circleDy / 2 + radius;

			g2d.setColor(colorFillPhase0);
			final Shape circleShape = new Ellipse2D.Double(centreX - circleDx, centreY - circleDy, 2.0 * circleDx,
					2.0 * circleDy);
			g2d.fill(circleShape);

			g2d.setColor(colorEdgesOuter);
			g2d.setStroke(strokeThick());
			g2d.draw(circleShape);
		}
		else if (notMancalaBoard)
		{
			// Compute the centre of the board.
			double maxDx = 0.0;
			double maxDy = 0.0;
			
			double topY = screenPosn(topology().vertices().get(0).centroid()).getY();
			double leftX = screenPosn(topology().vertices().get(0).centroid()).getX();

			for (final Vertex v : topology().vertices())
			{
				final Point2D screenPosnV = screenPosn(v.centroid());

				if (topY > screenPosnV.getY())
					topY = screenPosnV.getY();
				if (leftX > screenPosnV.getX())
					leftX = screenPosnV.getX();

				for (final Vertex v2 : topology().vertices())
				{
					final Point2D screenPosnV2 = screenPosn(v2.centroid());
					final double dY = Math.abs(screenPosnV.getY() - screenPosnV2.getY());
					if (maxDy < dY)
						maxDy = dY;
					final double dX = Math.abs(screenPosnV.getX() - screenPosnV2.getX());
					if (maxDx < dX)
						maxDx = dX;
				}
			}
			
			// final int angle = (rows < 30) ? rows * 15 : rows * 10; // arc angle for board
			// corners (30 and 60)
			final int angle = 60;

			maxDx += dx;
			maxDy += dx;
			leftX -= dx / 2;
			topY -= dx / 2;
			final RoundRectangle2D shape = new RoundRectangle2D.Double(leftX, topY, maxDx, maxDy,
					angle, angle);

			g2d.setColor(colorFillPhase0);
			g2d.fill(shape);

			g2d.setColor(colorEdgesOuter);
			g2d.setStroke(strokeThick());
			g2d.draw(shape);
		}
		else if (withStore)
		{
			final Point2D ptBL = topology().vertices().get(indexHoleBL).centroid();
			final Point2D ptTR = topology().vertices().get(indexHoleTR).centroid();

			final Point2D ptBR = topology().vertices().get(indexHoleBR).centroid();
			final Point2D ptTL = topology().vertices().get(indexHoleTL).centroid();

			// correct only if the board has stores.
			final Point2D ptL = topology().vertices().get(0).centroid();
			final Point2D ptR = (withStore) ? topology().vertices().get(rows * cols + 1).centroid()
					: topology().vertices().get(0).centroid();

			final int angleForStorage = 120 / rows; // arc angle for storage pits (60 and 30)
			final int angleForCorners = rows * 15; // arc angle for board corners (30 and 60)

			final GeneralPath boardShape = new GeneralPath();
			pt = screenPosn(withStore ? ptL : ptBL);

			boardShape.append(new Arc2D.Double(pt.x - radius, pt.y - radius, 2 * radius, 2 * radius,
					180 - angleForStorage, 2 * angleForStorage, Arc2D.OPEN), true);
			pt = screenPosn(ptBL);
			boardShape.append(new Arc2D.Double(pt.x - radius, pt.y - radius, 2 * radius, 2 * radius,
					270 - angleForCorners, angleForCorners, Arc2D.OPEN), true);
			pt = screenPosn(ptBR);
			boardShape.append(new Arc2D.Double(pt.x - radius, pt.y - radius, 2 * radius, 2 * radius, 270,
					angleForCorners, Arc2D.OPEN), true);
			pt = screenPosn(ptR);
			boardShape.append(new Arc2D.Double(pt.x - radius, pt.y - radius, 2 * radius, 2 * radius,
					360 - angleForStorage, 2 * angleForStorage, Arc2D.OPEN), true);
			pt = screenPosn(ptTR);
			boardShape.append(new Arc2D.Double(pt.x - radius, pt.y - radius, 2 * radius, 2 * radius,
					90 - angleForCorners, angleForCorners, Arc2D.OPEN), true);
			pt = screenPosn(ptTL);
			boardShape.append(new Arc2D.Double(pt.x - radius, pt.y - radius, 2 * radius, 2 * radius, 90,
					angleForCorners, Arc2D.OPEN), true);
			boardShape.closePath();

			g2d.setColor(colorFillPhase0);
			g2d.fill(boardShape);

			g2d.setColor(colorEdgesOuter);
			g2d.setStroke(strokeThick());
			g2d.draw(boardShape);
		}
		else
		{
			final Point2D ptBL = topology().vertices().get(indexHoleBL).centroid();
			final Point2D ptTR = topology().vertices().get(indexHoleTR).centroid();

			final Point2D ptBR = topology().vertices().get(indexHoleBR).centroid();
			final Point2D ptTL = topology().vertices().get(indexHoleTL).centroid();

			// correct only if the board has stores.
			final Point2D ptL = topology().vertices().get(0).centroid();
			pt = screenPosn(withStore ? ptL : ptBL);

			pt = screenPosn(ptTL);
			final double width = screenPosn(ptBR).x - screenPosn(ptBL).x + 2 * radius;
			final double height = screenPosn(ptBR).y - screenPosn(ptTR).y + 2 * radius;

			final int angle = (rows < 30) ? rows * 15 : rows * 10; // arc angle for board corners (30 and 60)
			final RoundRectangle2D shape = new RoundRectangle2D.Double(pt.x - radius, pt.y - radius, width, height,
					angle, angle);

			g2d.setColor(colorFillPhase0);
			g2d.fill(shape);

			g2d.setColor(colorEdgesOuter);
			g2d.setStroke(strokeThick());
			g2d.draw(shape);
		}


		// Determine pit colours based on board colour
		final int fillR = colorFillPhase0.getRed();
		final int fillG = colorFillPhase0.getGreen();
		final int fillB = colorFillPhase0.getBlue();

		final float[] hsv = new float[3];
		Color.RGBtoHSB(fillR, fillG, fillB, hsv);

		final Color dark   = new Color(Color.HSBtoRGB(hsv[0], hsv[1], 0.75f * hsv[2]));
		final Color darker = new Color(Color.HSBtoRGB(hsv[0], hsv[1], 0.5f * hsv[2]));

		// Draw pits
		g2d.setStroke(strokeThin);
		final int r = (int) (0.45 * dx); // pit radius
	
		for (final Vertex vertex : topology().vertices())
		{
			pt = screenPosn(vertex.centroid());
			if (specialHoles.contains(vertex.index()))
			{
				if (type == HoleType.Square)
					drawSquare(g2d, pt.x, pt.y, r, null, dark, darker);
				else if (type == HoleType.Oval)
					drawOval(g2d, pt.x, pt.y, r, null, dark, darker);
			}
			else
				drawPit(g2d, pt.x, pt.y, r, null, dark, darker);
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Draws a board pit at the specified location.
	 */
	@SuppressWarnings("static-method")
	void drawPit
	(
		final Graphics2D g2d, final int x, final int y, final int r,
		final Color lines, final Color dark, final Color darker
	)
	{
		final int rr = (int)(0.85 * r);

		g2d.setColor(darker);
		g2d.fillArc(x-r, y-r, 2*r, 2*r, 0, 360);

		g2d.setColor(dark);
		g2d.fillArc(x-r, y-r,  2*r, 2*r, 180, 180);
		g2d.fillArc(x-r, y-rr, 2*r, 2*rr,  0, 360);

		if (lines != null)
		{
			g2d.setColor(lines);
			g2d.drawArc(x-r, y-r, 2*r, 2*r, 0, 360);
		}
	}
	
	/**
	 * Draws a square pit at the specified location.
	 */
	@SuppressWarnings("static-method")
	void drawSquare
	(
		final Graphics2D g2d, final int x, final int y, final int r,
		final Color lines, final Color dark, final Color darker
	)
	{
		final int rr = (int) (0.95 * r);

		g2d.setColor(darker);
		g2d.fillRect(x - r, y - r, r * 2, r * 2);
		g2d.setColor(dark);
		g2d.fillRect(x - rr, y - rr, rr * 2, rr * 2);
	}
	
	/**
	 * Draws an oval pit at the specified location.
	 */
	@SuppressWarnings("static-method")
	void drawOval
	(
		final Graphics2D g2d, final int x, final int y, final int r,
		final Color lines, final Color dark, final Color darker
	)
	{
		final int rr = (int)(0.85 * r);

		g2d.setColor(darker);
		g2d.fillArc(x - 3 * r, y - r, 6 * r, 2 * r, 0, 360);

		g2d.setColor(dark);
		g2d.fillArc(x - r * 3, y - r, 6 * r, 2 * r, 180, 180);
		g2d.fillArc(x - r * 3, y - rr, 6 * r, 2 * rr, 0, 360);

		if (lines != null)
		{
			g2d.setColor(lines);
			g2d.drawArc(x - r * 3, y - r, 6 * r, 2 * r, 0, 360);
		}
	}
	
	//-------------------------------------------------------------------------

}
