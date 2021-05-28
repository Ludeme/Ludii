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
 * Draws a specified image behind a piece.
 * 
 * @author Matthew.Stephenson
 */
@Hide
public class PieceBackground implements GraphicsItem
{
	/** RoleType condition. */
	private final RoleType roleType;
	
	/** Piece name condition. */
	private final String pieceName;
	
	/** state condition. */
	private final Integer state;
	
	/** value condition. */
	private final Integer value;
	
	/** Background image to draw. */
	private final String background;
	
	/** Fill colour of drawn image. */
	private final Colour fillColour;
	
	/** Edge colour of drawn image. */
	private final Colour edgeColour;
	
	/** Scale of drawn image. */
	private final float scale;
	
	/** Rotation of drawn image. */
	private final int rotation;
		
	//-------------------------------------------------------------------------

	/**
	 * @param roleType  	Player whose index is to be matched.
	 * @param pieceName 	Base piece name to match.
	 * @param state   	    State to match.
	 * @param value  	 	Value to match.
	 * @param background	Name of the background image to draw.
	 * @param fillColour	Colour for the inner sections of the image. Default value is the fill colour of the component.
	 * @param edgeColour	Colour for the edges of the image. Default value is the edge colour of the component.
	 * @param scale			Scale for the drawn image relative to the cell size of the container [1.0].
	 * @param rotation		Rotation of the drawn image [0].
	 */
	public PieceBackground
	(
		@Opt final RoleType roleType,
		@Opt final String pieceName,
		@Opt @Name final Integer state,
		@Opt @Name final Integer value,
		@Name final String background,
		@Opt @Name final Colour fillColour,
		@Opt @Name final Colour edgeColour,
		@Opt @Name final Float scale,
		@Opt @Name final Integer rotation
	)
	{
		this.roleType = roleType;
		this.pieceName = pieceName;
		this.state = state;
		this.value = value;
		this.background = background;
		this.fillColour = fillColour;
		this.edgeColour = edgeColour;
		this.scale = (scale == null) ? (float)1.0 : scale.floatValue();
		this.rotation = (rotation == null) ? (int)0 : rotation.intValue();
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
	 * @return Piece state condition to check.
	 */
	public Integer state()
	{
		return state;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Background image to draw.
	 */
	public String background()
	{
		return background;
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
