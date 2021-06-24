package metadata.graphics.piece.scale;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.types.play.RoleType;
import metadata.graphics.GraphicsItem;

/**
 * Sets the image scale of a piece.
 * 
 * @author Matthew.Stephenson
 * 
 * @remarks A scale of 0 shrinks the piece to nothing, 1 is full (100 percent) size.
 *          Use piece scaling for fitting pieces to boards with small cells, 
 *          or if pieces should be sized relative to each other, e.g. Chess pawns are smaller.
 */
@Hide
public class PieceScale implements GraphicsItem
{
	/** RoleType condition. */
	private final RoleType roleType;
	
	/** Piece name condition. */
	private final String pieceName;
	
	/** state condition. */
	private final Integer state;
	
	/** value condition. */
	private final Integer value;
	
	/** Component image scale to apply. */
	private final float scale;
	
	/** Scale of drawn image along x-axis. */
	private final float scaleX;
	
	/** Scale of drawn image along y-axis. */
	private final float scaleY;
		
	//-------------------------------------------------------------------------

	/**
	 * @param roleType  Player whose index is to be matched.
	 * @param pieceName Base piece name to match.
	 * @param state   	State to match.
	 * @param value   	Value to match. 
	 * @param scale     Scaling factor.
	 * @param scaleX    Scaling factor on dimension X.
	 * @param scaleY    Scaling factor on dimension Y.
	 */
	public PieceScale
	(
		@Opt 	    final RoleType roleType,
		@Opt 	    final String pieceName,
		@Opt @Name  final Integer state,
		@Opt @Name  final Integer value,
		@Opt @Name  final Float scale,
		@Opt @Name  final Float scaleX,
		@Opt @Name  final Float scaleY
	)
	{
		this.roleType = roleType;
		this.pieceName = pieceName;
		this.state = state;
		this.value = value;
		this.scale = (scale == null) ? (float)1.0 : scale.floatValue();
		this.scaleX = (scaleX == null) ? (float)1.0 : scaleX.floatValue();
		this.scaleY = (scaleY == null) ? (float)1.0 : scaleY.floatValue();
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
	 * @return Piece value condition to check.
	 */
	public Integer value()
	{
		return value;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return X scale to apply onto component image.
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
