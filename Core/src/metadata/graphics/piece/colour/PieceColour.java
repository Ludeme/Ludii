package metadata.graphics.piece.colour;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.types.play.RoleType;
import metadata.graphics.GraphicsItem;
import metadata.graphics.util.colour.Colour;

/**
 * Sets the colour of a piece.
 * 
 * @author Matthew.Stephenson
 */
@Hide
public class PieceColour implements GraphicsItem
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
	
	/** Component fill colour to apply. */
	private final Colour fillColour;
	
	/** Component stroke colour to apply. */
	private final Colour strokeColour;
	
	/** Component secondary colour to apply. */
	private final Colour secondaryColour;
		
	//-------------------------------------------------------------------------

	/**
	 * @param roleType     		Player whose index is to be matched.
	 * @param pieceName    		Base piece name to match.
	 * @param container container index to match.
	 * @param state   	   		State to match.
	 * @param value   	   		Value to match.
	 * @param fillColour   		Fill colour for this piece.
	 * @param strokeColour 		Stroke colour for this piece.
	 * @param secondaryColour 	Secondary colour for this piece.
	 */
	public PieceColour
	(
		@Opt       final RoleType roleType,
		@Opt       final String pieceName,
		@Opt @Name final Integer container,
		@Opt @Name final Integer state,
		@Opt @Name final Integer value,
		@Opt @Name final Colour fillColour,
		@Opt @Name final Colour strokeColour,
		@Opt @Name final Colour secondaryColour
	)
	{
		this.roleType = roleType;
		this.pieceName = pieceName;
		this.container = container;
		this.state = state;
		this.value = value;
		this.fillColour = fillColour;
		this.strokeColour = strokeColour;
		this.secondaryColour = secondaryColour;
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
	 * @return container index condition to check.
	 */
	public Integer container()
	{
		return container;
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
	 * @return Piece value condition to check.
	 */
	public Integer value()
	{
		return value;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Fill colour to apply onto component image.
	 */
	public Colour fillColour()
	{
		return fillColour;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Stroke colour to apply onto component image.
	 */
	public Colour strokeColour()
	{
		return strokeColour;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Secondary colour to apply onto component image.
	 */
	public Colour secondaryColour()
	{
		return secondaryColour;
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
