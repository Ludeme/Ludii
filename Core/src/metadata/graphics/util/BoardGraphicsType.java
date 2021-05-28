package metadata.graphics.util;

import game.types.board.SiteType;
import metadata.MetadataItem;

/**
 * 
 * @author matthew.stephenson
 */
public enum BoardGraphicsType implements MetadataItem 
{
	/** Edges that are not along a board side. */
	InnerEdges(0, SiteType.Edge),
	
	/** Edges that define a board side. */
	OuterEdges(1, SiteType.Edge),
	
	/** Cells in phase 0, e.g. dark cells on the Chess board. */
	Phase0(2, SiteType.Cell),
	
	/** Cells in phase 1, e.g. light cells on the Chess board. */
	Phase1(3, SiteType.Cell),
	
	/** Cells in phase 2, e.g. for hexagonal tiling colouring. */
	Phase2(4, SiteType.Cell),
	
	/** Cells in phase 3, e.g. for exotic tiling colourings. */
	Phase3(5, SiteType.Cell),
	
	/** Cells in phase 4, e.g. for exotic tiling colouring. */
	Phase4(6, SiteType.Cell),
	
	/** Cells in phase 5, e.g. for exotic tiling colourings. */
	Phase5(7, SiteType.Cell),
	
	/** Symbols drawn on the board, e.g. Senet, Hnefatafl, Royal Game of Ur... */
	Symbols(8, null),
	
	/** Intersections of lines on the board, e.g. where Go stones are played. */
	InnerVertices(9, SiteType.Vertex),
	
	/** Intersections of lines on the board, along the perimeter of the board. */
	OuterVertices(10, SiteType.Vertex);
	
	private final int value;
	private final SiteType siteType;

	//-------------------------------------------------------------------------
	
	private BoardGraphicsType(final int value, final SiteType siteType) 
	{
        this.value = value;
        this.siteType = siteType;
    }
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The value of the graphic element type.
	 */
	public int value()
	{
		return value;
	}
	
	/**
	 * @return The SiteType of the graphic element type.
	 */
	public SiteType siteType()
	{
		return siteType;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param value The value.
	 * 
	 * @return the Board Graphics Type from its value.
	 */
	public static BoardGraphicsType getTypeFromValue(final int value)
	{
	    for (final BoardGraphicsType type : values())
	        if (type.value == value)
	            return type;
	    return null;
	}
	
	//-------------------------------------------------------------------------

}
