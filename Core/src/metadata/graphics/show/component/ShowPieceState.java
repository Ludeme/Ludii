package metadata.graphics.show.component;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.types.play.RoleType;
import metadata.graphics.GraphicsItem;
import metadata.graphics.util.ValueLocationType;
import other.concept.Concept;

/**
 * Indicates whether the state of a piece should be displayed.
 * 
 * @author Matthew.Stephenson and cambolbro
 * 
 * @remarks Used for displaying information about pieces in specific games.
 */
@Hide
public class ShowPieceState implements GraphicsItem
{
	/** RoleType condition. */
	private final RoleType roleType;
	
	/** Piece name condition. */
	private final String pieceName;
	
	/** The location to draw the state. */
	private final ValueLocationType location;
	
	/** Offset the image by the size of the displayed value. */
	private final boolean offsetImage;
	
	/** Draw outline around the displayed value. */
	private final boolean valueOutline;
	
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
	 * @param roleType  Player whose index is to be matched.
	 * @param pieceName Base piece name to match.
	 * @param location  The location to draw the state [Corner].
	 * @param offsetImage   Offset the image by the size of the displayed value [False].
	 * @param valueOutline  Draw outline around the displayed value [False].
	 * @param scale			Scale for the drawn image relative to the cell size of the container [1.0].
	 * @param scaleX		Scale for the drawn image, relative to the cell size of the container, along x-axis [1.0].
	 * @param scaleY		Scale for the drawn image, relative to the cell size of the container, along y-axis [1.0].
	 * @param rotation		Rotation of the drawn image [0].
	 * @param offsetX       Offset distance percentage to push the image to the right [0].
	 * @param offsetY       Offset distance percentage to push the image down [0].
	 * 
	 * @example (showPieceState)
	 */
	public ShowPieceState
	(
		@Opt final RoleType roleType,
		@Opt final String pieceName,
		@Opt final ValueLocationType location,
		@Opt @Name final Boolean offsetImage,
		@Opt @Name final Boolean valueOutline,
		@Opt @Name final Float scale,
		@Opt @Name final Float scaleX,
		@Opt @Name final Float scaleY,
		@Opt @Name final Integer rotation,
		@Opt @Name final Float offsetX,
		@Opt @Name final Float offsetY
	)
	{
		this.roleType  = roleType;
		this.pieceName = pieceName;
		this.location  = (location == null) ? ValueLocationType.CornerLeft : location;
		this.offsetImage = (offsetImage == null) ? false : offsetImage.booleanValue();
		this.valueOutline = (valueOutline == null) ? false : valueOutline.booleanValue();
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
	 * @return The location to draw the value.
	 */
	public ValueLocationType location()
	{
		return location;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Offset the image by the size of the displayed value.
	 */
	public boolean offsetImage()
	{
		return offsetImage;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Draw outline around the displayed value.
	 */
	public boolean valueOutline()
	{
		return valueOutline;
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
		concepts.set(Concept.ShowPieceState.id(), true);
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
