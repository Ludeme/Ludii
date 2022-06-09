package metadata.graphics.show;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.functions.ints.IntFunction;
import game.functions.region.RegionFunction;
import game.types.board.RelationType;
import game.types.board.SiteType;
import game.types.play.RoleType;
import metadata.graphics.GraphicsItem;
import metadata.graphics.show.Boolean.ShowCost;
import metadata.graphics.show.Boolean.ShowCurvedEdges;
import metadata.graphics.show.Boolean.ShowEdgeDirections;
import metadata.graphics.show.Boolean.ShowLocalStateHoles;
import metadata.graphics.show.Boolean.ShowPits;
import metadata.graphics.show.Boolean.ShowPlayerHoles;
import metadata.graphics.show.Boolean.ShowPossibleMoves;
import metadata.graphics.show.Boolean.ShowRegionOwner;
import metadata.graphics.show.Boolean.ShowStraightEdges;
import metadata.graphics.show.check.ShowCheck;
import metadata.graphics.show.component.ShowPieceState;
import metadata.graphics.show.component.ShowPieceValue;
import metadata.graphics.show.edges.ShowEdges;
import metadata.graphics.show.line.ShowLine;
import metadata.graphics.show.score.ShowScore;
import metadata.graphics.show.sites.ShowSitesAsHoles;
import metadata.graphics.show.sites.ShowSitesIndex;
import metadata.graphics.show.symbol.ShowSymbol;
import metadata.graphics.util.BoardGraphicsType;
import metadata.graphics.util.CurveType;
import metadata.graphics.util.EdgeType;
import metadata.graphics.util.HoleType;
import metadata.graphics.util.LineStyle;
import metadata.graphics.util.ValueLocationType;
import metadata.graphics.util.WhenScoreType;
import metadata.graphics.util.colour.Colour;

/**
 * Shows a graphic property or an information.
 * 
 * @author Eric.Piette
 */
@SuppressWarnings("javadoc")
public class Show implements GraphicsItem
{
	/**
	 * For showing properties on the holes.
	 * 
	 * @param showDataType The type of data to apply to the holes.
	 * @param indices      The list of indices of the holes.
	 * @param type         The shape of the holes.
	 * 
	 * @example (show AsHoles {5 10} Square)
	 */
	public static GraphicsItem construct
	(
		 final ShowSiteDataType showDataType, 
	     final Integer[] indices,
	     final HoleType type
	)
	{
		return new ShowSitesAsHoles(indices,type);
	}

	//-------------------------------------------------------------------------------
	
	/**
	 * For showing the index of sites on the board.
	 * 
	 * @param showDataType 		The type of data to apply.
	 * @param type 				Site Type [Cell].
	 * @param additionalValue   Additional value to add to the index [0].
	 * 
	 * @example (show SiteIndex Cell 5)
	 */
	public static GraphicsItem construct
	(
			  final ShowSiteDataType showDataType, 
		 @Opt final SiteType type,
		 @Opt final Integer additionalValue
	)
	{
		return new ShowSitesIndex(type, additionalValue);
	}

	//-------------------------------------------------------------------------------
	
	/**
	 * For showing symbols on sites.
	 * 
	 * @param showType          The type of data to show.
	 * @param imageName         Name of the image to show.
	 * @param region            Draw image on all sites in this region.
	 * @param roleType          Player whose index is to be matched (only for
	 *                          Region).
	 * @param graphElementType  The GraphElementType for the specified sites [Cell].
	 * @param sites             Draw image on all specified sites.
	 * @param site              Draw image on this site.
	 * @param regionFunction              Draw image on this regionFunction.
	 * @param boardGraphicsType Only apply image onto sites that are also part of
	 *                          this BoardGraphicsType.
	 * @param fillColour        Colour for the inner sections of the image. Default
	 *                          value is the fill colour of the component.
	 * @param edgeColour        Colour for the edges of the image. Default value is
	 *                          the edge colour of the component.
	 * @param scale             Scale for the drawn image relative to the cell size
	 *                          of the container [1.0].
	 * @param scaleX            The scale of the image along x-axis.
	 * @param scaleY            The scale of the image along y-axis.
	 * @param rotation          The rotation of the symbol.
	 * @param offsetX			Horizontal offset for image (to the right) [0.0].
	 * @param offsetY 			Vertical offset for image (downwards) [0.0].
	 * 
	 * @example (show Symbol "water" Cell 15 scale:0.85)
	 */
	public static GraphicsItem construct
	(
				   	   final ShowSymbolType showType, 
		               final String imageName,
		@Opt           final String region,
		@Opt           final RoleType roleType,
		@Opt           final SiteType graphElementType,
		@Opt @Or       final Integer[] sites,
		@Opt @Or       final Integer site,
		@Opt           final RegionFunction regionFunction,
		@Opt           final BoardGraphicsType boardGraphicsType,
		@Opt     @Name final Colour fillColour,
		@Opt     @Name final Colour edgeColour,
		@Opt     @Name final Float scale,
		@Opt     @Name final Float scaleX,
		@Opt     @Name final Float scaleY,
		@Opt	 @Name final Integer rotation,
		@Opt	 @Name final Float offsetX,
		@Opt	 @Name final Float offsetY
	)
	{
		switch (showType)
		{
		case Symbol:
			return new ShowSymbol(imageName, region, roleType, graphElementType,sites,site,regionFunction,boardGraphicsType,fillColour,edgeColour,scale,scaleX,scaleY,rotation,offsetX,offsetY);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Show(): A ShowSymbolType is not implemented.");
	}
	
	//-------------------------------------------------------------------------------
	
	/**
	 * For showing symbols on sites.
	 * 
	 * @param showType		The type of data to show.
	 * @param lines			The line to draw (pairs of vertices).
	 * @param siteType		The GraphElementType for the specified sites [Vertex].
	 * @param style			Line style [Thin].
	 * @param colour		The colour of the line.
	 * @param scale			The scale of the line.
	 * @param curve			The control points for the line to create a Bézier curve with (4 values: x1, y1, x2, y2, between 0 and 1).
	 * @param curveType		Type of curve [Spline].
	 * 
	 * @example (show Line { {1 0} {2 4} })
	 */
	public static GraphicsItem construct
	(
					   final ShowLineType showType, 
					   final Integer[][] lines,
		@Opt	 	   final SiteType siteType,
		@Opt       	   final LineStyle style,
		@Opt     	   final Colour colour,
		@Opt     @Name final Float scale,
		@Opt     @Name final Float[] curve,
		@Opt	 	   final CurveType curveType
	)
	{
		switch (showType)
		{
		case Line:
			return new ShowLine(lines, siteType, style, colour, scale, curve, curveType);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Show(): A ShowLineType is not implemented.");
	}

	//-------------------------------------------------------------------------------
	
	/**
	 * For showing specific edges of the graph board (only valid with GraphStyle or its children).
	 * 
	 * @param showType     The type of data to show.
	 * @param type         EdgeType condition [All].
	 * @param relationType RelationType condition[Neighbour].
	 * @param connection   If this concerns cell connections, rather than graph
	 *                     edges [False].
	 * @param style        Line style for drawing edges [ThinDotted].
	 * @param colour       Colour in which to draw edges [LightGrey].
	 * 
	 * @example (show Edges Diagonal Thin)
	 */
	public static GraphicsItem construct
	(
				   final ShowEdgeType 	showType, 
		@Opt       final EdgeType  		type,
		@Opt       final RelationType  	relationType,
		@Opt @Name final Boolean 		connection,
		@Opt       final LineStyle 		style,
		@Opt       final Colour    		colour
	)
	{
		switch (showType)
		{
		case Edges:
			return new ShowEdges(type, relationType, connection, style, colour);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Show(): A ShowEdgeType is not implemented.");
	}
	
	/**
	 * For showing properties.
	 * 
	 * @param showType The type of data to show.
	 * @param value    Whether the graphic data has to be showed. [True].
	 * 
	 * @example (show Pits)
	 * @example (show PlayerHoles)
	 * @example (show RegionOwner)
	 */
	public static GraphicsItem construct
	(
			 final ShowBooleanType showType,
		@Opt final Boolean value
	)
	{
		switch (showType)
		{
		case Pits:
			return new ShowPits(value);
		case PlayerHoles:
			return new ShowPlayerHoles(value);
		case LocalStateHoles:
			return new ShowLocalStateHoles(value);
		case RegionOwner:
			return new ShowRegionOwner(value);
		case Cost:
			return new ShowCost(value);
		case EdgeDirections:
			return new ShowEdgeDirections(value);
		case PossibleMoves:
			return new ShowPossibleMoves(value);
		case CurvedEdges:
			return new ShowCurvedEdges(value);
		case StraightEdges:
			return new ShowStraightEdges(value);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Show(): A ShowBooleanType is not implemented.");
	}

	//-------------------------------------------------------------------------------
	
	/**
	 * For showing properties on a piece.
	 * 
	 * @param showType     	The type of element to show a data on it.
	 * @param showDataType 	The type of data to show.
	 * @param roleType     	Player whose index is to be matched.
	 * @param pieceName    	Base piece name to match.
	 * @param location     	The location to draw the value [Corner].
	 * @param offsetImage   Offset the image by the size of the displayed value [False].
	 * @param valueOutline  Draw outline around the displayed value [False].
	 * @param scale			Scale for the drawn image relative to the cell size of the container [1.0].
	 * @param offsetX       Offset distance percentage to push the image to the right [0].
	 * @param offsetY       Offset distance percentage to push the image down [0].
	 * 
	 * @example (show Piece State)
	 * @example (show Piece Value)
	 */
	public static GraphicsItem construct
	(
			 final ShowComponentType showType, 
			 final ShowComponentDataType showDataType,
		@Opt final RoleType roleType,
		@Opt final String pieceName,
		@Opt final ValueLocationType location,
		@Opt @Name final Boolean offsetImage,
		@Opt @Name final Boolean valueOutline,
		@Opt @Name final Float scale,
		@Opt @Name final Float offsetX,
		@Opt @Name final Float offsetY
	)
	{
		switch (showType)
		{
		case Piece:
		{
			switch (showDataType)
			{
			case State:
				return new ShowPieceState(roleType, pieceName, location, offsetImage, valueOutline, scale, offsetX, offsetY);
			case Value:
				return new ShowPieceValue(roleType, pieceName, location, offsetImage, valueOutline, scale, offsetX, offsetY);
			default:
				break;
			}

			// We should never reach that except if we forget some codes.
			throw new IllegalArgumentException(
					"Show(): A ShowComponentDataType is not implemented for the Piece type.");
		}
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Show(): A ShowComponentType is not implemented.");
	}

	//-------------------------------------------------------------------------------
	
	/**
	 * For showing the check message.
	 * 
	 * @param showType  The type of data to show.
	 * @param roleType  Player whose index is to be matched.
	 * @param pieceName Base piece name to match.
	 * 
	 * @example (show Check "King")
	 */
	public static GraphicsItem construct
	(
			 final ShowCheckType showType,
		@Opt final RoleType roleType,
		@Opt final String pieceName
	)
	{
		switch (showType)
		{
		case Check:
			return new ShowCheck(roleType, pieceName);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Show(): A ShowCheckType is not implemented.");
	}
	
	//-------------------------------------------------------------------------------
	
	/**
	 * For showing the score.
	 * 
	 * @param showType  The type of data to show.
	 * @param whenScore When the score should be shown [Always].
	 * @param roleType  Player whose index is to be matched [All].
	 * @param scoreReplacement  Replacement value to display instead of score.
	 * @param scoreSuffix  Extra string to append to the score displayed [""].
	 * 
	 * @example (show Score Never)
	 */
	public static GraphicsItem construct
	(
			 final ShowScoreType showType,
		@Opt final WhenScoreType whenScore,
		@Opt final RoleType roleType,
		@Opt final IntFunction scoreReplacement,
		@Opt final String scoreSuffix
	)
	{
		switch (showType)
		{
		case Score:
			return new ShowScore(whenScore, roleType, scoreReplacement, scoreSuffix);
		default:
			break;
		}

		// We should never reach that except if we forget some codes.
		throw new IllegalArgumentException("Show(): A ShowScoreType is not implemented.");
	}

	//-------------------------------------------------------------------------------

	private Show()
	{
		// Ensure that compiler does not pick up default constructor
	}

	//-------------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Game game)
	{
		throw new UnsupportedOperationException("Board.concepts(...): Should never be called directly.");
	}

	@Override
	public long gameFlags(final Game game)
	{
		throw new UnsupportedOperationException("Board.gameFlags(...): Should never be called directly.");
	}

	@Override
	public boolean needRedraw()
	{
		throw new UnsupportedOperationException("Board.gameFlags(...): Should never be called directly.");
	}
}
