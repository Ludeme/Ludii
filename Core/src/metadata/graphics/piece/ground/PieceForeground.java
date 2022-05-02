package metadata.graphics.piece.ground;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.types.play.RoleType;
import metadata.graphics.GraphicsItem;
import metadata.graphics.util.colour.Colour;

/**
 * Draws a specified image in front of a piece.
 * 
 * @author Matthew.Stephenson
 */
@Hide
public class PieceForeground implements GraphicsItem
{
	/** RoleType condition. */
	private final RoleType roleType;
	
	/** Piece name condition. */
	private final String pieceName;
	
	/** container index condition. */
	private final Integer container;
	
	/** state condition. */
	private final Integer state;
	
	/** value condition. */
	private final Integer value;
	
	/** Foreground image to draw. */
	private final String image;
	
	/** Fill colour of drawn image. */
	private final Colour fillColour;
	
	/** Edge colour of drawn image. */
	private final Colour edgeColour;
	
	/** Scale of drawn image. */
	private final float scale;
	
	/** Scale of drawn image along x-axis. */
	private final float scaleX;
	
	/** Scale of drawn image along y-axis. */
	private final float scaleY;
	
	/** Rotation of drawn image. */
	private final int rotation;
	
	/** Offset right for drawn image. */
	private final float offsetX;
	
	/** Offset down for drawn image. */
	private final float offsetY;
		
	//-------------------------------------------------------------------------

	/**
	 * @param roleType  	Player whose index is to be matched.
	 * @param pieceName 	Base piece name to match.
	 * @param container     container index to match.
	 * @param state   	    State to match.
	 * @param value  	 	Value to match.
	 * @param image	        Name of the foreground image to draw.
	 * @param fillColour	Colour for the inner sections of the image. Default value is the fill colour of the component.
	 * @param edgeColour	Colour for the edges of the image. Default value is the edge colour of the component.
	 * @param scale			Scale for the drawn image relative to the cell size of the container [1.0].
	 * @param scaleX		Scale for the drawn image, relative to the cell size of the container, along x-axis [1.0].
	 * @param scaleY		Scale for the drawn image, relative to the cell size of the container, along y-axis [1.0].
	 * @param rotation		Rotation of the drawn image [0].
	 * @param offsetX       Offset distance percentage to push the image to the right [0].
	 * @param offsetY       Offset distance percentage to push the image down [0].
	 */
	public PieceForeground
	(
		@Opt       final RoleType roleType,
		@Opt       final String pieceName,
		@Opt @Name final Integer container,
		@Opt @Name final Integer state,
		@Opt @Name final Integer value,
		@Name      final String image,
		@Opt @Name final Colour fillColour,
		@Opt @Name final Colour edgeColour,
		@Opt @Name final Float scale,
		@Opt @Name final Float scaleX,
		@Opt @Name final Float scaleY,
		@Opt @Name final Integer rotation,
		@Opt @Name final Float offsetX,
		@Opt @Name final Float offsetY
	)
	{
		this.roleType = roleType;
		this.pieceName = pieceName;
		this.container = container;
		this.state = state;
		this.value = value;
		this.image = image;
		this.fillColour = fillColour;
		this.edgeColour = edgeColour;
		this.scale = (scale == null) ? (float)1.0 : scale.floatValue();
		this.scaleX = (scaleX == null) ? (float)1.0 : scaleX.floatValue();
		this.scaleY = (scaleY == null) ? (float)1.0 : scaleY.floatValue();
		this.rotation = (rotation == null) ? (int)0 : rotation.intValue();
		this.offsetX = (offsetX == null) ? 0 : offsetX.floatValue();
		this.offsetY = (offsetY == null) ? 0 : offsetY.floatValue();
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
	 * @return Piece name condition to check.
	 */
	public String pieceName()
	{
		return pieceName;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return container index condition to check.
	 */
	public Integer container()
	{
		return container;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Piece state condition to check.
	 */
	public Integer state()
	{
		return state;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Foreground image to draw.
	 */
	public String image()
	{
		return image;
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
	 * @return Piece value condition to check.
	 */
	public Integer value() 
	{
		return value;
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
	 * @return Offset right for drawn image.
	 */
	public float offsetX()
	{
		return offsetX;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Offset down for drawn image.
	 */
	public float offsetY()
	{
		return offsetY;
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
