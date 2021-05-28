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
		
	//-------------------------------------------------------------------------

	/**
	 * @param roleType  Player whose index is to be matched.
	 * @param pieceName Base piece name to match.
	 * @param location  The location to draw the state [Corner].
	 * @param offsetImage   Offset the image by the size of the displayed value [False].
	 * @param valueOutline  Draw outline around the displayed value [False].
	 * 
	 * @example (showPieceState)
	 */
	public ShowPieceState
	(
		@Opt final RoleType roleType,
		@Opt final String pieceName,
		@Opt final ValueLocationType location,
		@Opt @Name final Boolean offsetImage,
		@Opt @Name final Boolean valueOutline
	)
	{
		this.roleType  = roleType;
		this.pieceName = pieceName;
		this.location  = (location == null) ? ValueLocationType.Corner : location;
		this.offsetImage = (offsetImage == null) ? false : offsetImage.booleanValue();
		this.valueOutline = (valueOutline == null) ? false : valueOutline.booleanValue();
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
