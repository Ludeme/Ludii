package metadata.graphics.show.symbol;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.region.RegionFunction;
import game.types.board.SiteType;
import game.types.play.RoleType;
import metadata.graphics.GraphicsItem;
import metadata.graphics.util.BoardGraphicsType;
import metadata.graphics.util.colour.Colour;
import other.concept.Concept;
import other.context.Context;

/**
 * Draws a specified image on the board.
 * 
 * @author Matthew.Stephenson and cambolbro
 * 
 * @remarks The image name specifies the name of an image file that comes packed 
 *          into the Ludii distribution. See Appendix A for a list of provided images.
 */
@Hide
public class ShowSymbol implements GraphicsItem
{
	/** Image to draw. */
	private final String imageName;
	
	/** text to draw. */
	private final String text;
	
	/** Region to add the image onto. */
	private final String region;
	
	/** GraphElementType for the specified location(s). */
	private final SiteType graphElementType;
	
	/** Set of locations to add the image onto. */
	private final Integer[] sites;
	
	/** Region to colour. */
	private final RegionFunction regionFunction;
	
	/** RoleType condition. */
	private final RoleType roleType;
	
	/** Scale of drawn image. */
	private final float scale;
	
	/** Scale of drawn image along x-axis. */
	private final float scaleX;
	
	/** Scale of drawn image along y-axis. */
	private final float scaleY;
	
	/** Fill colour of drawn image. */
	private final Colour fillColour;
	
	/** Edge colour of drawn image. */
	private final Colour edgeColour;
	
	/** boardGraphicsType condition. */
	private final BoardGraphicsType boardGraphicsType;
	
	/** rotation of the drawn image. */
	private final int rotation;
	
	/** Offset right for drawn image. */
	private final float offsetX;
	
	/** Offset down for drawn image. */
	private final float offsetY;
	
	//-------------------------------------------------------------------------

	/**
	 * @param imageName         Name of the image to draw.
	 * @param text        		Text string to show.
	 * @param region            Draw image on all sites in this region.
	 * @param graphElementType  The GraphElementType for the specified sites [Default board type].
	 * @param sites             Draw image on all specified sites.
	 * @param site              Draw image on this site.
	 * @param regionFunction    Draw image on this regionFunction.
	 * @param boardGraphicsType Only apply image onto sites that are also part of
	 *                          this BoardGraphicsType.
	 * @param fillColour        Colour for the inner sections of the image. Default
	 *                          value is the fill colour of the component.
	 * @param edgeColour        Colour for the edges of the image. Default value is
	 *                          the edge colour of the component.
	 * @param scale             Scale for the drawn image relative to the cell size
	 *                          of the container [1.0].
	 * @param scaleX			Scale for the drawn image, relative to the cell size of the container, along x-axis [1.0].
	 * @param scaleY			Scale for the drawn image, relative to the cell size of the container, along y-axis [1.0].
	 * @param roleType          Player whose index is to be matched (only for
	 *                          Region).
	 * @param rotation          The rotation of the symbol.
	 * @param offsetX			Horizontal offset for image (to the right) [0.0].
	 * @param offsetY 			Vertical offset for image (downwards) [0.0].
	 */
	public ShowSymbol
	(
		@Opt           final String imageName,
		@Opt     @Name final String text,
		@Opt           final String region,
		@Opt           final RoleType roleType,
		@Opt           final SiteType graphElementType,
		@Opt @Or       final Integer[] sites,
		@Opt @Or       final Integer site,
		@Opt 		   final RegionFunction regionFunction,
		@Opt           final BoardGraphicsType boardGraphicsType,
		@Opt     @Name final Colour fillColour,
		@Opt     @Name final Colour edgeColour,
		@Opt     @Name final Float scale,
		@Opt 	 @Name final Float scaleX,
		@Opt 	 @Name final Float scaleY,
		@Opt	 @Name final Integer rotation,
		@Opt	 @Name final Float offsetX,
		@Opt	 @Name final Float offsetY
	)
	{
		int numNonNull = 0;
		if (sites != null)
			numNonNull++;
		if (site != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Only one of @Or should be different to null");
		
		this.imageName = imageName;
		this.text = text;
		this.region = region;
		this.graphElementType = graphElementType;
		this.sites = ((sites != null) ? sites : ((site != null) ? (new Integer[]{ site }) : null));
		this.regionFunction = regionFunction;
		this.boardGraphicsType = boardGraphicsType;
		this.fillColour = fillColour;
		this.edgeColour = edgeColour;
		this.scale = (scale == null) ? (float)1.0 : scale.floatValue();
		this.scaleX = (scaleX == null) ? (float)1.0 : scaleX.floatValue();
		this.scaleY = (scaleY == null) ? (float)1.0 : scaleY.floatValue();
		this.rotation = (rotation == null) ? 0 : rotation.intValue();
		this.roleType = roleType;
		this.offsetX = (offsetX == null) ? (float)0.0 : offsetX.floatValue();
		this.offsetY = (offsetY == null) ? (float)0.0 : offsetY.floatValue();
	}

	//-------------------------------------------------------------------------

	/**
	 * @param context The context.
	 * @return GraphElementType for the specified location(s).
	 */
	public SiteType graphElementType(final Context context)
	{
		if (graphElementType == null)
			return context.game().board().defaultSite();
		return graphElementType;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Sites to add the image onto.
	 */
	public Integer[] sites()
	{
		return sites;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Image to draw.
	 */
	public String imageName()
	{
		return imageName;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Text to draw.
	 */
	public String text()
	{
		return text;
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
	 * @return Scale of drawn image along x-axis.
	 */
	public float scaleX()
	{
		return scaleX;
	}
	//-------------------------------------------------------------------------
	
	/**
	 * @return Scale of drawn image along y-axis.
	 */
	public float scaleY()
	{
		return scaleY;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Fill colour of drawn image.
	 */
	public Colour fillColour()
	{
		return fillColour;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Edge colour of drawn image.
	 */
	public Colour edgeColour()
	{
		return edgeColour;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return BoardGraphicsType that the image is drawn on.
	 */
	public BoardGraphicsType boardGraphicsType()
	{
		return boardGraphicsType;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Region to add the image onto.
	 */
	public String region()
	{
		return region;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Region to Colour.
	 */
	public RegionFunction regionFunction()
	{
		return regionFunction;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Rotation of drawn image.
	 */
	public int rotation()
	{
		return rotation;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return RoleType condition to check.
	 */
	public RoleType roleType()
	{
		return roleType;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Offset right for drawn image
	 */
	public float getOffsetX() 
	{
		return offsetX;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Offset down for drawn image
	 */
	public float getOffsetY() 
	{
		return offsetY;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.Symbols.id(), true);
		return concepts;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = 0l;
		if (regionFunction != null)
			gameFlags |= regionFunction.gameFlags(game);
		return gameFlags;
	}

	@Override
	public boolean needRedraw()
	{
		if (regionFunction != null)
			return !regionFunction.isStatic();
		return false;
	}
}
