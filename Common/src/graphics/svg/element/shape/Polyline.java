package graphics.svg.element.shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import graphics.svg.element.Element;

//-----------------------------------------------------------------------------

/**
 * SVG polyline shape.
 * @author cambolbro
 */
public class Polyline extends Shape
{
	// Format: <polyline points="50,175 150,175 150,125 250,200" />
	
	protected final List<Point2D.Double> points = new ArrayList<Point2D.Double>();
	
	//-------------------------------------------------------------------------
	
	public Polyline()
	{
		super("polyline");
	}
	
	public Polyline(final String label)
	{
		super(label);
	}
	
	//-------------------------------------------------------------------------

	public List<Point2D.Double> points()
	{
		return Collections.unmodifiableList(points);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Element newInstance()
	{
		return new Polyline();
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void setBounds()
	{
		double x0 =  10000;
		double y0 =  10000;
		double x1 = -10000;
		double y1 = -10000;
		
		for (final Point2D.Double pt : points)
		{
			if (pt.x < x0)
				x0 = pt.x;
			if (pt.y < y0)
				y0 = pt.y;
			if (pt.x > x1)
				x1 = pt.x;
			if (pt.y > x1)
				y1 = pt.y;
		}
			
		bounds.setRect(x0, y0, x1-x0, y1-y0);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public boolean load(final String expr)
	{
		final boolean okay = true;
		
		if (!super.load(expr))
			return false;

		final int pos = expr.indexOf(" points=\"");
		
		int to = pos+9;
		while (to < expr.length() && expr.charAt(to) != '\"')
			to++;
		
		if (to >= expr.length())
		{
			System.out.println("* Failed to close points list in Polyline.");
			return false;
		}
		
		final String[] subs = expr.substring(pos+9, to).split(" ");
		for (int n = 0; n < subs.length-1; n+=2)
		{
			final double x = Double.parseDouble(subs[n]);
			final double y = Double.parseDouble(subs[n+1]);
			points.add(new Point2D.Double(x, y));
		}
		
		return okay;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append(label() + ": fill=" + style.fill() + ", stroke=" + style.stroke() + ", strokeWidth=" + style.strokeWidth());
		sb.append(" :");
		for (final Point2D.Double pt : points)
			sb.append(" (" + pt.x + "," + pt.y + ")");
		
		return sb.toString();

	}
	
	//-------------------------------------------------------------------------

	@Override
	public void render
	(
		final Graphics2D g2d, final double x0, final double y0, 
		final Color footprintColour, final Color fillColour, final Color strokeColour
	)
	{
		// ...
	}

	@Override
	public Element newOne()
	{
		return new Polyline();
	}
	
	//-------------------------------------------------------------------------

}
