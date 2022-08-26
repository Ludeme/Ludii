package view.container.aspects.designs.board;

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
import main.math.MathRoutines;
import main.math.Vector;
import other.context.Context;
import other.topology.Vertex;
import view.container.aspects.designs.BoardDesign;
import view.container.styles.BoardStyle;

/**
 * Graphics for the spiral board (e.g. Mehen).
 * 
 * @author cambolbro
 */

public class SpiralDesign extends BoardDesign 
{
	private int numSites = 1;
	private int numTurns = 1;

	/** The angles for calculating vertex positions. */
	private double[] thetas;
	
	//-------------------------------------------------------------------------
	
	public SpiralDesign(final BoardStyle boardStyle) 
	{
		super(boardStyle, null);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String createSVGImage(final Bridge bridge, final Context context) 
	{
		final SVGGraphics2D g2d = boardStyle.setSVGRenderingValues();

		final float swRatio = 5 / 1000.0f;
		final float swThin  = Math.max(1, (int) (swRatio * boardStyle.placement().width + 0.5));
		final float swThick = 1 * swThin;

		setStrokesAndColours
		(
			bridge,
			context,
			new Color(0,0,0),
			new Color(150,  75,   0),  // border
			new Color(200, 150,  75),  // dark cells
			new Color(250, 221, 144),  // light cells
			new Color(223, 178, 110),  // middle cells,
			null,
			null,
			null,
			null,
			swThin,
			swThick
		);

		// Number of turns is first dimension of Spiral shape
		numTurns = context.board().graph().dim()[0];
		numSites = topology().vertices().size();
		
		setThetas();
		drawSpiralBoard(g2d);
		return g2d.getSVGDocument();
	}
	
	//-------------------------------------------------------------------------
	
	void setThetas()
	{
		// Twice the number of vertices per ring, offset between rings
		final int base = baseNumber();

		thetas = new double[2 * numSites];
		int index = 1;

		int steps = base;  // number of steps per ring
		for (int ring = 1; ring <= numTurns + 1; ring++)
		{
			final double dTheta = Math.PI * 2 / steps;
			double theta = Math.PI * 2 * ring;
			
			if (ring <= 2 || ring % 2 == 1)
				theta -= dTheta / 2;  // offset so that lines don't coincide between rings
			
			for (int step = 0; step < steps; step++)
			{	
				thetas[index++] = theta;
				theta += dTheta;
			}
			if (ring <= 2)
				steps *= 2;
		}

		// Smoothing passes to reduce unevenness between steps
		for (int vid = 2; vid < numSites; vid++)
			thetas[vid] = (thetas[vid-1] + thetas[vid+1]) / 2.0;
		
		for (int vid = 2; vid < numSites; vid++)
			thetas[vid] = (thetas[vid-1] + thetas[vid+1]) / 2.0;
		
		thetas[1] -= 0.5 * (thetas[2] - thetas[1]);  // fudge to nudge vertex 1 into place
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Number of cells in the inner ring, doubling with each layer.
	 */
	private int baseNumber()
	{
		for (int base = 1; base < numSites; base++)
		{
			// Try this base
			int steps = base;
			int total = 1;
			
			for (int ring = 1; ring < numTurns; ring++)
			{
				total += steps;
				if (total > numSites)
				{
					if (ring <= numTurns)
						return base - 1;
					break;
				}
				if (ring <= 2)
					steps *= 2;
			}
		}
		
		System.out.println("** Error: Couldn't find base number for spiral.");
		return 0;
	}
	
	//-------------------------------------------------------------------------

	void drawSpiralBoard(final Graphics2D g2d)
	{
		final int rd = 2;
		g2d.setColor(new Color(0, 127, 255));
		for (final Vertex vertex : topology().vertices())
		{
			final Point pt = boardStyle.screenPosn(vertex.centroid());
			g2d.fillOval(pt.x-rd, pt.y-rd,  2*rd,  2*rd);
		}
		
		final double a = 0.05;
		final double b = 1.0 / (2.0 * numTurns * numTurns) * 0.8;

		final double end = //(numTurns + 0.325) * 2 * Math.PI;
				(thetas[thetas.length / 2 - 1] + thetas[thetas.length / 2]) / 2;
		
		final double nudge = 0.005;
		
		final double x0 = topology().vertices().get(0).centroid().getX();
		final double y0 = topology().vertices().get(0).centroid().getY();
		
		final List<Point> pts = new ArrayList<Point>();
				
		for (double theta = -0.05; theta < end + 1; theta += 0.2)
		{
			final double clipTheta = (theta > end ? end : theta) - nudge;
			final double r = a + b * clipTheta;
			final double x = x0 - r * Math.cos(clipTheta);
			final double y = y0 + r * Math.sin(clipTheta);
			
			final Point2D.Double xy = new Point2D.Double(x, y);
			final Point pt = boardStyle.screenPosn(xy);
			pts.add(pt);
			
			if (theta > end)
			{
				// Store one last sample at end point
				final double r2 = a + b * theta;
				final double x2 = x0 - r2 * Math.cos(theta);
				final double y2 = y0 + r2 * Math.sin(theta);
			
				final Point2D.Double xy2 = new Point2D.Double(x2, y2);
				final Point pt2 = boardStyle.screenPosn(xy2);
				pts.add(pt2);
				
				// Store final point to close on
				final double r3 = a - 0.1 + b * (end + nudge);
				final double x3 = x0 - r3 * Math.cos(end + nudge);
				final double y3 = y0 + r3 * Math.sin(end + nudge);
			
				final Point2D.Double xy3 = new Point2D.Double(x3, y3);
				final Point pt3 = boardStyle.screenPosn(xy3);
				pts.add(pt3);
				
				break;
			}
		}
		
		// Draw smooth spline
		GeneralPath path = new GeneralPath();
		for (int n = 0; n < pts.size()-3; n++) 
		{
			final Point pt = pts.get(n);
			if (n == 0)
			{
				path.moveTo(pt.x, pt.y);
			}
			else
			{
				final Point ptA = pts.get(n-1);
				final Point ptB = pts.get(n);
				final Point ptC = pts.get(n+1);
				final Point ptD = pts.get(n+2);
				
				final Vector vecAC = new Vector(ptC.x-ptA.x, ptC.y-ptA.y);
				vecAC.normalise();
				
				final Vector vecDB = new Vector(ptB.x-ptD.x, ptB.y-ptD.y);
				vecDB.normalise();
				
				final double distBC = MathRoutines.distance(ptB, ptC);
				final double off = 0.3 * distBC;
				
				final double bx = ptB.x + vecAC.x() * off;
				final double by = ptB.y + vecAC.y() * off;
				final double cx = ptC.x + vecDB.x() * off;
				final double cy = ptC.y + vecDB.y() * off;
				final double dx = ptC.x;
				final double dy = ptC.y;
				
				path.curveTo(bx, by, cx, cy, dx, dy);
			}
		}
		
		// Final closing point to draw straight edge 
		final Point ptN = pts.get(pts.size() - 1);
		path.lineTo(ptN.x, ptN.y);
		
		g2d.setColor(new Color(255, 240, 220));
		g2d.fill(path);

		g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
		g2d.setColor(new Color(220, 180, 120));
		g2d.draw(path);
		
		// Draw the central cell
		final Point ptC1 = boardStyle.screenPosn(topology().vertices().get(1).centroid());
		final Point ptC2 = boardStyle.screenPosn(topology().vertices().get(2).centroid());
		final double u = MathRoutines.distance(ptC1.x, ptC1.y, ptC2.x, ptC2.y);

		path = new GeneralPath();
		final Point ptA = pts.get(0);
		final Point ptB = pts.get(22);
		path.moveTo(ptA.x, ptA.y);
		path.curveTo(ptA.x+(int)(.25 * u), ptA.y+(int)(.5 * u), ptB.x+(int)(0 * u), ptB.y-(int)(.5 * u), ptB.x, ptB.y);
		g2d.draw(path);
		
		// Draw the septum divisions
		for (int vid = 1; vid < thetas.length / 2; vid++)
		{
			final double theta = (thetas[vid] + thetas[vid+1]) / 2;
		
			final double r1 = a - 0.1 + b * (theta + nudge);
			final double x1 = (x0 - r1 * Math.cos(theta + nudge));
			final double y1 = y0 + r1 * Math.sin(theta + nudge);
			
			final Point2D.Double xy1 = new Point2D.Double(x1, y1);
			final Point pt1 = boardStyle.screenPosn(xy1);
		
			final double r2 = a + b * (theta - nudge);
			final double x2 = (x0 - r2 * Math.cos(theta - nudge));
			final double y2 = y0 + r2 * Math.sin(theta - nudge);
			
			final Point2D.Double xy2 = new Point2D.Double(x2, y2);
			final Point pt2 = boardStyle.screenPosn(xy2);
			
			g2d.drawLine(pt1.x, pt1.y, pt2.x, pt2.y);
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Point projected inwards to the ring that the specified vertex angle would lie on.
	 */
	@SuppressWarnings("static-method")
	Point2D.Double ptOnRing
	(
		final double x0, final double y0, final double a, final double b, 
		final double theta, final double scale
	)
	{
		final double thetaPrev = theta - 2 * Math.PI;
		
		final double r     = a + b * theta;
		final double rPrev = a + b * thetaPrev;
		
		final Point2D.Double pt = 	new Point2D.Double
								 	(
								 		x0 + r * Math.cos(theta), 
								 		y0 - r * Math.sin(theta)
								 	);
		final Point2D.Double ptP =  new Point2D.Double
								    (
										x0 + rPrev * Math.cos(thetaPrev), 
										y0 - rPrev * Math.sin(thetaPrev)
								    );
		
		return new Point2D.Double((pt.x + ptP.x)/2.0*scale, (pt.y + ptP.y)/2.0*scale);
	}
	
	//-------------------------------------------------------------------------
	
}
