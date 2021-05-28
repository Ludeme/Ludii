package game.util.graph;

import game.functions.dim.DimFunction;
import main.math.Polygon;
import other.BaseLudeme;

//-----------------------------------------------------------------------------

/**
 * Defines a polygon composed of a list of floating point (x,y) pairs.
 * 
 * @author cambolbro
 * 
 * @remarks The polygon can be concave.
 */
public class Poly extends BaseLudeme
{
	private final Polygon polygon;
	
	//-------------------------------------------------------------------------

	/**
	 * For building a polygon with float points.
	 * 
	 * @param pts Float points defining polygon.
	 * 
	 * @example (poly { { 0 0 } { 0 2.5 } { 4.75 1 } })
	 */
	public Poly
	(
		final Float[][] pts
	) 
	{
		polygon = new Polygon(pts);
	}

	/**
	 * For building a polygon with DimFunction points.
	 * 
	 * @param pts Float points defining polygon.
	 * 
	 * @example (poly { { 0 0 } { 0 2.5 } { 4.75 1 } })
	 */
	public Poly(final DimFunction[][] pts)
	{
		final Float[][] floatPts = new Float[pts.length][];

		// Translation from DimFunction to Float.
		for (int i = 0; i < pts.length; i++)
		{
			final DimFunction[] yPoint = pts[i];
			floatPts[i] = new Float[yPoint.length];
			for (int j = 0; j < pts[i].length; j++)
				floatPts[i][j] = Float.valueOf(pts[i][j].eval());
		}

		polygon = new Polygon(floatPts);
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The polygon.
	 */
	public Polygon polygon()
	{
		return polygon;
	}
	
	//-------------------------------------------------------------------------

}
