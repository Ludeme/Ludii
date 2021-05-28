package metadata.graphics.show.line;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.types.board.SiteType;
import metadata.graphics.GraphicsItem;
import metadata.graphics.util.CurveType;
import metadata.graphics.util.colour.Colour;

/**
 * Draws a specified image on the board.
 * 
 * @author Matthew.Stephenson and cambolbro
 * 
 * @remarks The image name specifies the name of an image file that comes packed 
 *          into the Ludii distribution. See Appendix A for a list of provided images.
 */
@Hide
public class ShowLine implements GraphicsItem
{	
	/** Set of vertex locations pairs to add the line onto. */
	private final Integer[][] lines;
	
	/** Scale of drawn line. */
	private final float scale;
	
	/** Colour of drawn line. */
	private final Colour colour;
	
	/** The control points for the line to create a Bézier curve with (4 values: x1, y1, x2, y2, between 0 and 1). */
	private final Float[] curve;
	
	/** SiteType to draw line on. */
	private final SiteType siteType;
	
	/** Type of curve. */
	private final CurveType curveType;
		
	//-------------------------------------------------------------------------

	/**
	 * @param lines			Set of vertex locations pairs to add the line onto.
	 * @param siteType		SiteType to draw line on [Vertex].
	 * @param colour		Colour of drawn line.
	 * @param scale			Scale of drawn line [1.0].
	 * @param curve			The control points for the line to create a Bézier curve with (4 values: x1, y1, x2, y2, between 0 and 1).
	 * @param curveType		Type of curve [Spline].
	 */
	public ShowLine
	(
				 	   final Integer[][] lines,
		@Opt	 	   final SiteType siteType,
		@Opt           final Colour colour,
		@Opt     @Name final Float scale,
		@Opt	 @Name final Float[] curve,
		@Opt		   final CurveType curveType
	)
	{		
		this.lines = lines;
		this.siteType = (siteType == null) ? SiteType.Vertex : siteType;
		this.colour = colour;
		this.scale = (scale == null) ? (float)1.0 : scale.floatValue();
		this.curve = curve;
		this.curveType = (curveType == null) ? CurveType.Spline : curveType;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Set of vertex locations pairs to add the line onto.
	 */
	public Integer[][] lines()
	{
		return lines;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Scale of drawn image.
	 */
	public float scale()
	{
		return scale;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return SiteType to draw line on.
	 */
	public SiteType siteType()
	{
		return siteType;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return CurveType of curve.
	 */
	public CurveType curveType()
	{
		return curveType;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Colour of drawn line.
	 */
	public Colour colour()
	{
		return colour;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return The control points for the line to create a Bézier curve with (4 values: x1, y1, x2, y2, between 0 and 1).
	 */
	public Float[] curve() 
	{
		return curve;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		return concepts;
	}

	@Override
	public long gameFlags(final Game game)
	{
		final long gameFlags = 0l;
		return gameFlags;
	}

	@Override
	public boolean needRedraw()
	{
		return false;
	}

}
