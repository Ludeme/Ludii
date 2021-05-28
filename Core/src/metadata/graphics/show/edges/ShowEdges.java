package metadata.graphics.show.edges;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.types.board.RelationType;
import metadata.graphics.GraphicsItem;
import metadata.graphics.util.EdgeType;
import metadata.graphics.util.LineStyle;
import metadata.graphics.util.colour.Colour;
import metadata.graphics.util.colour.UserColourType;

//-----------------------------------------------------------------------------

/**
 * Specifies customised drawing of edges in the board graph.
 * 
 * @author matthew.stephenson and cambolbro
 * 
 * @remarks Useful for graph games to show possible edge moves. Only works for games that use GraphStyle, or a child of GraphStyle (e.g. Pen and Paper style)
 */
@Hide
public class ShowEdges implements GraphicsItem
{
	/** EdgeType condition to check. */
	private final EdgeType  type;
	
	/** RelationType condition to check. */
	private final RelationType relationType;
	
	/** If this concerns cell connections, rather than graph edges. */
	private final Boolean connection;
	
	/** Line style of the edge. */
	private final LineStyle style;
	
	/** Colour of the edge. */
	private final Colour colour;
	
	//-------------------------------------------------------------------------

	/**
	 * @param type   		EdgeType condition [All].
	 * @param relationType 	RelationType condition[Neighbour].
	 * @param connection   	If this concerns cell connections, rather than graph edges [False].
	 * @param style  		Line style for drawing edges [ThinDotted].
	 * @param colour 		Colour in which to draw edges [LightGrey].
	 */
	public ShowEdges
	(
		@Opt       final EdgeType  		type,
		@Opt       final RelationType  	relationType,
		@Opt @Name final Boolean 		connection,
		@Opt       final LineStyle 		style,
		@Opt       final Colour    		colour
	)
	{
		this.type   = (type   != null) ? type   : EdgeType.All;
		this.relationType = (relationType != null) ? relationType : RelationType.All;
		this.connection = (connection == null) ? Boolean.valueOf(false) : connection;
		this.style  = (style  != null) ? style  : LineStyle.ThinDotted;
		this.colour = (colour != null) ? colour : new Colour(UserColourType.LightGrey);
	}

	//-------------------------------------------------------------------------

	/**
	 * @return EdgeType condition to check.
	 */
	public EdgeType type()
	{
		return type;
	}
	
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return RelationType condition to check.
	 */
	public RelationType relationType()
	{
		return relationType;
	}
	
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return If this concerns cell connections, rather than graph edges.
	 */
	public Boolean connection()
	{
		return connection;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Line style of the edge.
	 */
	public LineStyle style()
	{
		return style;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Colour of the edge.
	 */
	public Colour colour()
	{
		return colour;
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
