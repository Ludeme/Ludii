package metadata.graphics.util;

import metadata.MetadataItem;

//-----------------------------------------------------------------------------

/**
 * Supported style types for rendering particular components.
 * 
 * @author matthew.stephenson and cambolbro
 */
public enum ComponentStyleType implements MetadataItem
{
	/** Style for pieces. */
	Piece,
	
	/** Style for tiles (components that fill a cell and may have marked paths). */ 
	Tile,
	
	/** Style for playing cards. */
	Card,
	
	/** Style for die components used as playing pieces. */
	Die,
	
	/** Style for dominoes */
	Domino,
	
	/** Style for large pieces that srtaddle more than once site, e.g. the L Game. */
	LargePiece,
	
	/** Extended style for Shogi pieces. */
	ExtendedShogi,
	
	/** Extended style for Shogi pieces. */
	ExtendedXiangqi,
	
	/** Style for native american dice. */
	NativeAmericanDice;
	
	//-----------------------------------------------------------------------------
	
	/**
	 * @param name The name.
	 * 
	 * @return The component style from its name.
	 */
	public static ComponentStyleType fromName(final String name)
	{
		try
		{
			return valueOf(name);
		}
		catch (final Exception e)
		{
			return Piece;
		}
	}
	
}
