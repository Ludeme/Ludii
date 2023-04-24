package metadata.graphics;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import annotations.Or;
import game.Game;
import game.equipment.container.Container;
import game.equipment.other.Regions;
import game.functions.ints.board.Id;
import game.functions.region.RegionFunction;
import game.types.board.RelationType;
import game.types.board.ShapeType;
import game.types.board.SiteType;
import game.types.component.SuitType;
import game.types.play.RoleType;
import main.Constants;
import main.StringRoutines;
import metadata.graphics.board.Boolean.BoardCheckered;
import metadata.graphics.board.colour.BoardColour;
import metadata.graphics.board.curvature.BoardCurvature;
import metadata.graphics.board.ground.BoardBackground;
import metadata.graphics.board.ground.BoardForeground;
import metadata.graphics.board.placement.BoardPlacement;
import metadata.graphics.board.shape.BoardShape;
import metadata.graphics.board.style.BoardStyle;
import metadata.graphics.board.styleThickness.BoardStyleThickness;
import metadata.graphics.hand.placement.HandPlacement;
import metadata.graphics.no.Boolean.NoAnimation;
import metadata.graphics.no.Boolean.NoBoard;
import metadata.graphics.no.Boolean.NoCurves;
import metadata.graphics.no.Boolean.NoDicePips;
import metadata.graphics.no.Boolean.NoSunken;
import metadata.graphics.others.HiddenImage;
import metadata.graphics.others.StackType;
import metadata.graphics.others.SuitRanking;
import metadata.graphics.piece.colour.PieceColour;
import metadata.graphics.piece.families.PieceFamilies;
import metadata.graphics.piece.ground.PieceBackground;
import metadata.graphics.piece.ground.PieceForeground;
import metadata.graphics.piece.name.PieceAddStateToName;
import metadata.graphics.piece.name.PieceExtendName;
import metadata.graphics.piece.name.PieceRename;
import metadata.graphics.piece.rotate.PieceRotate;
import metadata.graphics.piece.scale.PieceScale;
import metadata.graphics.piece.style.PieceStyle;
import metadata.graphics.player.colour.PlayerColour;
import metadata.graphics.player.name.PlayerName;
import metadata.graphics.puzzle.AdversarialPuzzle;
import metadata.graphics.puzzle.DrawHint;
import metadata.graphics.puzzle.HintLocation;
import metadata.graphics.region.colour.RegionColour;
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
import metadata.graphics.show.sites.ShowSitesShape;
import metadata.graphics.show.symbol.ShowSymbol;
import metadata.graphics.util.BoardGraphicsType;
import metadata.graphics.util.ComponentStyleType;
import metadata.graphics.util.ContainerStyleType;
import metadata.graphics.util.CurveType;
import metadata.graphics.util.EdgeInfoGUI;
import metadata.graphics.util.EdgeType;
import metadata.graphics.util.HoleType;
import metadata.graphics.util.LineStyle;
import metadata.graphics.util.MetadataFunctions;
import metadata.graphics.util.MetadataImageInfo;
import metadata.graphics.util.PieceColourType;
import metadata.graphics.util.PieceStackType;
import metadata.graphics.util.PuzzleDrawHintType;
import metadata.graphics.util.PuzzleHintLocationType;
import metadata.graphics.util.ScoreDisplayInfo;
import metadata.graphics.util.StackPropertyType;
import metadata.graphics.util.ValueDisplayInfo;
import metadata.graphics.util.colour.Colour;
import other.context.Context;
import other.topology.TopologyElement;

/**
 * Graphics hints for rendering the game.
 * 
 * @author matthew.stephenson and cambolbro
 */
public class Graphics implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	/** All metadata graphics items. */
	final List<GraphicsItem> items = new ArrayList<GraphicsItem>();
	
	/** Report detailing any errors when attempting to process metadata. */
	String errorReport = "";
	
	/** If the graphics need to be redrawn after each move. */
	private boolean needRedraw = false;

	//-------------------------------------------------------------------------

	/**
	 * Graphics hints for rendering the game.
	 * 
	 * @param item  The graphic item of the game.
	 * @param items The graphic items of the game.
	 */
	public Graphics
	(
		@Or	final GraphicsItem item,
		@Or	final GraphicsItem[] items
	)
	{
		int numNonNull = 0;
		if (item != null)
			numNonNull++;
		if (items != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Only one of @Or should be different to null");

		if(items != null)
		for (final GraphicsItem i : items)
			this.items.add(i);
		else
			this.items.add(item);
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @param context     		The context.
	 * @param playerIndexCond 	The index of the player.
	 * @param pieceNameCond   	The name of the piece
	 * @param containerIndexCond	The index of the container.
	 * @param stateCond		  	The state.
	 * @param valueCond		  	The value.
	 * @return 					If the piece's state should be added to its name.
	 */
	public boolean addStateToName(final Context context, final int playerIndexCond, final String pieceNameCond, final int containerIndexCond, final int stateCond, final int valueCond)
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof PieceAddStateToName)
			{
				final PieceAddStateToName pieceAddStateToName = (PieceAddStateToName) graphicsItem;
				final RoleType roleType = pieceAddStateToName.roleType();
				final String pieceName = pieceAddStateToName.pieceName();
				final Integer containerIndex = pieceAddStateToName.container();
				final Integer state = pieceAddStateToName.state();
				final Integer value = pieceAddStateToName.value();
				
				if (roleType == null || MetadataFunctions.getRealOwner(context, roleType) == playerIndexCond)
					if (state == null || state.intValue() == stateCond)
						if (value == null || value.intValue() == valueCond)
							if (containerIndex == null || containerIndex.intValue() == containerIndexCond)
								if (pieceName == null || pieceName.equals(pieceNameCond) || pieceName.equals(StringRoutines.removeTrailingNumbers(pieceNameCond)))
									return true;
			}

		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context     		The context.
	 * @param playerIndexCond 	The index of the player.
	 * @param pieceNameCond   	The name of the piece.
	 * @return 					The component style type.
	 */
	public ComponentStyleType componentStyle(final Context context, final int playerIndexCond, final String pieceNameCond)
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof PieceStyle)
			{
				final PieceStyle pieceStyle = (PieceStyle) graphicsItem;
				final RoleType roleType = pieceStyle.roleType();
				final String pieceName = pieceStyle.pieceName();
				
				if (roleType == null || MetadataFunctions.getRealOwner(context, roleType) == playerIndexCond)
					if (pieceName == null || pieceName.equals(pieceNameCond) || pieceName.equals(StringRoutines.removeTrailingNumbers(pieceNameCond)))
						return pieceStyle.componentStyleType();
			}

		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context     		The context.
	 * @param playerIndexCond 	The index of the player.
	 * @param pieceNameCond   	The name of the piece.
	 * @return 					The ValueDisplayInfo for the local state.
	 */
	public ValueDisplayInfo displayPieceState(final Context context, final int playerIndexCond, final String pieceNameCond)
	{
		final float MAX_SCALE = 100f;  // multiplication factor on original size
		final float MIN_OFFSET = -1f; // decimal percentage of board size
		final float MAX_OFFSET = 1f; // decimal percentage of board size
		
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof ShowPieceState)
			{
				final ShowPieceState showPieceState = (ShowPieceState) graphicsItem;
				final RoleType roleType = showPieceState.roleType();
				final String pieceName = showPieceState.pieceName();
				
				final float offsetX = showPieceState.offsetX();
				final float offsetY = showPieceState.offsetY();
				final float scale = showPieceState.scale();
				
				if (roleType == null || MetadataFunctions.getRealOwner(context, roleType) == playerIndexCond)
					if (pieceName == null || pieceName.equals(pieceNameCond) || pieceName.equals(StringRoutines.removeTrailingNumbers(pieceNameCond)))
						if (scale >= 0 && scale <= MAX_SCALE)
							if (offsetX >= MIN_OFFSET && offsetX <= MAX_OFFSET)
								if (offsetY >= MIN_OFFSET && offsetY <= MAX_OFFSET)
									return new ValueDisplayInfo
										(
											showPieceState.location(), 
											showPieceState.offsetImage(), 
											showPieceState.valueOutline(),
											scale,
											offsetX, 
											offsetY
										);
								else
									addError("Offset Y for piece state was equal to " + offsetY + ", offset must be between " + MIN_OFFSET + " and " + MAX_OFFSET);
							else
								addError("Offset X for piece state was equal to " + offsetX + ", offset must be between " + MIN_OFFSET + " and " + MAX_OFFSET);
						else 
							addError("Scale for piece state was equal to " + scale + ", scale must be between 0 and " + MAX_SCALE);
			}

		return new ValueDisplayInfo();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context     		The context.
	 * @param playerIndexCond	The index of the player.
	 * @param pieceNameCond   	The name of the piece.
	 * @return 					The ValueDisplayInfo for the value.
	 */
	public ValueDisplayInfo displayPieceValue(final Context context, final int playerIndexCond, final String pieceNameCond)
	{
		final float MAX_SCALE = 100f;  // multiplication factor on original size
		final float MIN_OFFSET = -1f; // decimal percentage of board size
		final float MAX_OFFSET = 1f; // decimal percentage of board size
		
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof ShowPieceValue)
			{
				final ShowPieceValue showPieceValue = (ShowPieceValue) graphicsItem;
				final RoleType roleType = showPieceValue.roleType();
				final String pieceName = showPieceValue.pieceName();
				
				final float offsetX = showPieceValue.offsetX();
				final float offsetY = showPieceValue.offsetY();
				final float scale = showPieceValue.scale();
				
				if (roleType == null || MetadataFunctions.getRealOwner(context, roleType) == playerIndexCond)
					if (pieceName == null || pieceName.equals(pieceNameCond) || pieceName.equals(StringRoutines.removeTrailingNumbers(pieceNameCond)))
						if (scale >= 0 && scale <= MAX_SCALE)
							if (offsetX >= MIN_OFFSET && offsetX <= MAX_OFFSET)
								if (offsetY >= MIN_OFFSET && offsetY <= MAX_OFFSET)
									return new ValueDisplayInfo
										(
											showPieceValue.location(), 
											showPieceValue.offsetImage(), 
											showPieceValue.valueOutline(),
											scale,
											offsetX, 
											offsetY
										);
								else
									addError("Offset Y for piece value was equal to " + offsetY + ", offset must be between " + MIN_OFFSET + " and " + MAX_OFFSET);
							else
								addError("Offset X for piece value was equal to " + offsetX + ", offset must be between " + MIN_OFFSET + " and " + MAX_OFFSET);
						else 
							addError("Scale for piece value was equal to " + scale + ", scale must be between 0 and " + MAX_SCALE);
			}

		return new ValueDisplayInfo();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context     The context.
	 * @return The metadataImageInfo.
	 */
	public ArrayList<MetadataImageInfo> boardBackground(final Context context)
	{
		final float MAX_SCALE = 100f;  // multiplication factor on original size
		final int MAX_ROTATION = 360;  // degrees
		final float MIN_OFFSET = -1f; // decimal percentage of board size
		final float MAX_OFFSET = 1f; // decimal percentage of board size
		
		final ArrayList<MetadataImageInfo> allBackgrounds = new ArrayList<>();
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof BoardBackground)
			{
				final BoardBackground boardBackground = (BoardBackground)graphicsItem;
				final int rotation = boardBackground.rotation();
				final float offsetX = boardBackground.offsetX();
				final float offsetY = boardBackground.offsetY();
				
				final Colour fillColourMeta = boardBackground.fillColour();
				final Colour edgeColourMeta = boardBackground.edgeColour();
				final Color fillColour = (fillColourMeta == null) ? null : fillColourMeta.colour();
				final Color edgeColour = (edgeColourMeta == null) ? null : edgeColourMeta.colour();
				
				final float scale = boardBackground.scale();
				float scaleX, scaleY;
				if (Math.abs(scale - 1.0) > Constants.EPSILON)
				{
					scaleX = scale;
					scaleY = scale;
				}
				else
				{
					scaleX = boardBackground.scaleX();
					scaleY = boardBackground.scaleY();
				}
				
				if (scaleX >= 0 && scaleX <= MAX_SCALE && scaleY >= 0 && scaleY <= MAX_SCALE)
					if (rotation >= 0 && rotation <= MAX_ROTATION)
						if (offsetX >= MIN_OFFSET && offsetX <= MAX_OFFSET)
							if (offsetY >= MIN_OFFSET && offsetY <= MAX_OFFSET)
								allBackgrounds.add
								(
									new MetadataImageInfo
									(
										-1, 
										null, 
										boardBackground.image(), 
										scaleX, 
										scaleY, 
										fillColour,	
										edgeColour,
										rotation,
										offsetX, 
										offsetY
									)
								);
							else
								addError("Offset Y for board background was equal to " + offsetY + ", offset must be between " + MIN_OFFSET + " and " + MAX_OFFSET);
						else
							addError("Offset X for board background was equal to " + offsetX + ", offset must be between " + MIN_OFFSET + " and " + MAX_OFFSET);
					else
						addError("Rotation for board background was equal to " + rotation + ", rotation must be between 0 and " + MAX_ROTATION);
				else 
					addError("Scale for board background was equal to " + scale + ", scale must be between 0 and " + MAX_SCALE);
			}
		return allBackgrounds;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context     The context.
	 * @return The metadataImageInfo.
	 */
	public ArrayList<MetadataImageInfo> boardForeground(final Context context)
	{
		final float MAX_SCALE = 100f;  // multiplication factor on original size
		final int MAX_ROTATION = 360;  // degrees
		final float MIN_OFFSET = -1f; // decimal percentage of board size
		final float MAX_OFFSET = 1f; // decimal percentage of board size
		
		final ArrayList<MetadataImageInfo> allForegrounds = new ArrayList<>();
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof BoardForeground)
			{
				final BoardForeground boardForeground = (BoardForeground)graphicsItem;
				final int rotation = boardForeground.rotation();
				final float offsetX = boardForeground.offsetX();
				final float offsetY = boardForeground.offsetY();
				
				final Colour fillColourMeta = boardForeground.fillColour();
				final Color fillColour = (fillColourMeta == null) ? null : fillColourMeta.colour();
				final Colour edgeColourMeta = boardForeground.edgeColour();
				final Color edgeColour = (edgeColourMeta == null) ? null : edgeColourMeta.colour();
				
				final float scale = boardForeground.scale();
				float scaleX, scaleY;
				if (Math.abs(scale - 1.0) > Constants.EPSILON)
				{
					scaleX = scale;
					scaleY = scale;
				}
				else
				{
					scaleX = boardForeground.scaleX();
					scaleY = boardForeground.scaleY();
				}
				
				if (scaleX >= 0 && scaleX <= MAX_SCALE && scaleY >= 0 && scaleY <= MAX_SCALE)
					if (rotation >= 0 && rotation <= MAX_ROTATION)
						if (offsetX >= MIN_OFFSET && offsetX <= MAX_OFFSET)
							if (offsetY >= MIN_OFFSET && offsetY <= MAX_OFFSET)
								allForegrounds.add
								(
									new MetadataImageInfo
									(
										-1, 
										null, 
										boardForeground.image(), 
										scaleX, 
										scaleY, 
										fillColour,
										edgeColour,
										boardForeground.rotation(),
										boardForeground.offsetX(),
										boardForeground.offsetY()
									)
								);
							else
								addError("Offset Y for board foreground was equal to " + offsetY + ", offset must be between " + MIN_OFFSET + " and " + MAX_OFFSET);
						else
							addError("Offset X for board foreground was equal to " + offsetX + ", offset must be between " + MIN_OFFSET + " and " + MAX_OFFSET);
					else
						addError("Rotation for board foreground was equal to " + rotation + ", rotation must be between 0 and " + MAX_ROTATION);
				else 
					addError("Scale for board foreground was equal to " + scale + ", scale must be between 0 and " + MAX_SCALE);
			}
		return allForegrounds;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context     			The context.
	 * @param playerIndexCond 		The index of the player.
	 * @param pieceNameCond   		The name of the piece.
	 * @param containerIndexCond	The index of the container.
	 * @param stateCond       		The state.
	 * @param valueCond       		The value.
	 * @return 						The metadataImageInfo.
	 */
	public ArrayList<MetadataImageInfo> pieceBackground(final Context context, final int playerIndexCond, final String pieceNameCond, final int containerIndexCond, final int stateCond, final int valueCond)
	{
		final float MAX_SCALE = 100f;  // multiplication factor on original size
		final int MAX_ROTATION = 360;  // degrees
		
		final ArrayList<MetadataImageInfo> allBackground = new ArrayList<>();
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof PieceBackground)
			{
				final PieceBackground pieceBackground = (PieceBackground)graphicsItem;
				final RoleType roleType = pieceBackground.roleType();
				final String pieceName = pieceBackground.pieceName();
				final Integer containerIndex = pieceBackground.container();
				final Integer value = pieceBackground.value();
				final Integer state = pieceBackground.state();
				final int rotation = pieceBackground.rotation();
				
				final Colour fillColourMeta = pieceBackground.fillColour();
				final Color fillColour = (fillColourMeta == null) ? null : fillColourMeta.colour();
				final Colour edgeColourMeta = pieceBackground.edgeColour();
				final Color edgeColour = (edgeColourMeta == null) ? null : edgeColourMeta.colour();

				final float scale = pieceBackground.scale();
				float scaleX, scaleY;
				if (Math.abs(scale - 1.0) > Constants.EPSILON)
				{
					scaleX = scale;
					scaleY = scale;
				}
				else
				{
					scaleX = pieceBackground.scaleX();
					scaleY = pieceBackground.scaleY();
				}
				
				if (roleType == null || MetadataFunctions.getRealOwner(context, roleType) == playerIndexCond)
					if (state == null || state.intValue() == stateCond)
						if (value == null || value.intValue() == valueCond)
							if (containerIndex == null || containerIndex.intValue() == containerIndexCond)
								if (pieceName == null || pieceName.equals(pieceNameCond) || pieceName.equals(StringRoutines.removeTrailingNumbers(pieceNameCond)))
									if (scaleX >= 0 && scaleX <= MAX_SCALE && scaleY >= 0 && scaleY <= MAX_SCALE)
										if (rotation >= 0 && rotation <= MAX_ROTATION)
											allBackground.add
											(
												new MetadataImageInfo
												(
													-1, 
													null, 
													pieceBackground.image(), 
													pieceBackground.text(),
													scaleX, 
													scaleY,
													fillColour,
													edgeColour,
													pieceBackground.rotation(),
													pieceBackground.offsetX(),
													pieceBackground.offsetY()
												)
											);
										else
											addError("Rotation for background of piece " + pieceName + " was equal to " + rotation + ", rotation must be between 0 and " + MAX_ROTATION);
									else
										addError("Scale for background of piece " + pieceName + " was equal to " + scale + ", scale must be between 0 and " + MAX_SCALE);
			}

		return allBackground;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context     			The context.
	 * @param playerIndexCond 		The index of the player.
	 * @param pieceNameCond   		The name of the piece.
	 * @param containerIndexCond	The index of the container.
	 * @param stateCond       		The state.
	 * @param valueCond       		The value.
	 * @return 						The MetadataImageInfo
	 */
	public ArrayList<MetadataImageInfo> pieceForeground(final Context context, final int playerIndexCond, final String pieceNameCond, final int containerIndexCond, final int stateCond, final int valueCond)
	{
		final float MAX_SCALE = 100f;  // multiplication factor on original size
		final int MAX_ROTATION = 360;  // degrees
		
		final ArrayList<MetadataImageInfo> allForeground = new ArrayList<>();
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof PieceForeground)
			{
				final PieceForeground pieceForeground = (PieceForeground)graphicsItem;
				final RoleType roleType = pieceForeground.roleType();
				final String pieceName = pieceForeground.pieceName();
				final Integer containerIndex = pieceForeground.container();
				final Integer value = pieceForeground.value();
				final Integer state = pieceForeground.state();
				final int rotation = pieceForeground.rotation();
				
				final Colour fillColourMeta = pieceForeground.fillColour();
				final Color fillColour = (fillColourMeta == null) ? null : fillColourMeta.colour();
				final Colour edgeColourMeta = pieceForeground.edgeColour();
				final Color edgeColour = (edgeColourMeta == null) ? null : edgeColourMeta.colour();
				
				final float scale = pieceForeground.scale();
				float scaleX, scaleY;
				if (Math.abs(scale - 1.0) > Constants.EPSILON)
				{
					scaleX = scale;
					scaleY = scale;
				}
				else
				{
					scaleX = pieceForeground.scaleX();
					scaleY = pieceForeground.scaleY();
				}
				
				if (roleType == null || MetadataFunctions.getRealOwner(context, roleType) == playerIndexCond)
					if (state == null || state.intValue() == stateCond)
						if (value == null || value.intValue() == valueCond)
							if (containerIndex == null || containerIndex.intValue() == containerIndexCond)
								if (pieceName == null || pieceName.equals(pieceNameCond) || pieceName.equals(StringRoutines.removeTrailingNumbers(pieceNameCond)))
									if (scaleX >= 0 && scaleX <= MAX_SCALE && scaleY >= 0 && scaleY <= MAX_SCALE)
										if (rotation >= 0 && rotation <= MAX_ROTATION)
											allForeground.add
											(
												new MetadataImageInfo
												(
													-1, 
													null, 
													pieceForeground.image(), 
													pieceForeground.text(),
													scaleX, 
													scaleY,
													fillColour,
													edgeColour,
													pieceForeground.rotation(),
													pieceForeground.offsetX(),
													pieceForeground.offsetY()
												)
											);
										else
											addError("Rotation for foreground of piece " + pieceName + " was equal to " + rotation + ", rotation must be between 0 and " + MAX_ROTATION);
									else
										addError("Scale for foreground of piece " + pieceName + " was equal to " + scale + ", scale must be between 0 and " + MAX_SCALE);
			}
	
		return allForeground;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The hidden image name.
	 */
	public String pieceHiddenImage()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof HiddenImage)
				return ((HiddenImage) graphicsItem).hiddenImage();
	
		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context     			The context.
	 * @param playerIndexCond 		The index of the player.
	 * @param pieceNameCond   		The name of the piece.
	 * @param containerIndexCond	The index of the container.
	 * @param stateCond       		The state.
	 * @param valueCond       		The value.
	 * @param pieceColourType		The aspect of the piece that is being coloured.
	 * @return 						The colour.
	 */
	public Color pieceColour(final Context context, final int playerIndexCond, final String pieceNameCond, final int containerIndexCond, final int stateCond, final int valueCond, final PieceColourType pieceColourType)
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof PieceColour)
			{
				final PieceColour pieceColour = (PieceColour) graphicsItem;
				final RoleType roleType = pieceColour.roleType();
				final String pieceName = pieceColour.pieceName();
				final Integer containerIndex = pieceColour.container();
				final Integer state = pieceColour.state();
				final Integer value = pieceColour.value();
				
				final Colour fillColour = pieceColour.fillColour();
				final Colour strokeColour = pieceColour.strokeColour();
				final Colour secondaryColour = pieceColour.secondaryColour();
			
				if (roleType == null || MetadataFunctions.getRealOwner(context, roleType) == playerIndexCond)
					if (state == null || state.intValue() == stateCond)
						if (value == null || value.intValue() == valueCond)
							if (containerIndex == null || containerIndex.intValue() == containerIndexCond)
								if (pieceName == null || pieceName.equals(pieceNameCond) || pieceName.equals(StringRoutines.removeTrailingNumbers(pieceNameCond)))
								{
									if (pieceColourType.equals(PieceColourType.Fill))
										return (fillColour == null) ? null : fillColour.colour();
									else if (pieceColourType.equals(PieceColourType.Edge))
										return (strokeColour == null) ? null : strokeColour.colour();
									else if (pieceColourType.equals(PieceColourType.Secondary))
										return (secondaryColour == null) ? null : secondaryColour.colour();
								}
			}

		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The list of the families pieces.
	 */
	public String[] pieceFamilies()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof PieceFamilies)
				return ((PieceFamilies) graphicsItem).pieceFamilies();

		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context     			The context.
	 * @param playerIndexCond 		The index of the player.
	 * @param pieceNameCond   		The name of the piece.
	 * @param containerIndexCond	The index of the container.
	 * @param stateCond       		The state.
	 * @param valueCond       		The value.
	 * @return 						The degrees to rotate the piece image (clockwise).
	 */
	public int pieceRotate(final Context context, final int playerIndexCond, final String pieceNameCond, final int containerIndexCond, final int stateCond, final int valueCond)
	{
		final int MAX_ROTATION = 360;  // degrees
		
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof PieceRotate)
			{
				final PieceRotate pieceRotate = (PieceRotate)graphicsItem;
				final RoleType roleType = pieceRotate.roleType();
				final String pieceName = pieceRotate.pieceName();
				final Integer containerIndex = pieceRotate.container();
				final Integer value = pieceRotate.value();
				final Integer state = pieceRotate.state();
				final int rotation = pieceRotate.rotation();
				
				if (roleType == null || MetadataFunctions.getRealOwner(context, roleType) == playerIndexCond)
					if (state == null || state.intValue() == stateCond)
						if (value == null || value.intValue() == valueCond)
							if (containerIndex == null || containerIndex.intValue() == containerIndexCond)
								if (pieceName == null || pieceName.equals(pieceNameCond) || pieceName.equals(StringRoutines.removeTrailingNumbers(pieceNameCond)))
									if (rotation >= 0 && rotation <= MAX_ROTATION)
										return rotation;
									else
										addError("Rotation for piece" + pieceNameCond + "was equal to " + rotation + ", rotation must be between 0 and " + MAX_ROTATION);
			}

		return 0;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context     			The context
	 * @param playerIndexCond 		The index of the player.
	 * @param pieceNameCond   		The name of the piece.
	 * @param containerIndexCond	The index of the container.
	 * @param stateCond       		The state.
	 * @param valueCond       		The state.
	 * @return 						The piece name extended.
	 */
	public String pieceNameExtension(final Context context, final int playerIndexCond, final String pieceNameCond, final int containerIndexCond, final int stateCond, final int valueCond)
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof PieceExtendName)
			{
				final PieceExtendName pieceExtendName = (PieceExtendName)graphicsItem;
				final RoleType roleType = pieceExtendName.roleType();
				final String pieceName = pieceExtendName.pieceName();
				final Integer containerIndex = pieceExtendName.container();
				final Integer value = pieceExtendName.value();
				final Integer state = pieceExtendName.state();
				
				if (roleType == null || MetadataFunctions.getRealOwner(context, roleType) == playerIndexCond)
					if (state == null || state.intValue() == stateCond)
						if (value == null || value.intValue() == valueCond)
							if (containerIndex == null || containerIndex.intValue() == containerIndexCond)
								if (pieceName == null || pieceName.equals(pieceNameCond) || pieceName.equals(StringRoutines.removeTrailingNumbers(pieceNameCond)))
									return pieceExtendName.nameExtension();
			}
		
		return "";
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context     			The context.
	 * @param playerIndexCond 		The index of the player.
	 * @param pieceNameCond   		The name of the piece.
	 * @param containerIndexCond	The index of the container.
	 * @param stateCond       		The state.
	 * @param valueCond       		The value.
	 * @return 						The new name.
	 */
	public String pieceNameReplacement(final Context context, final int playerIndexCond, final String pieceNameCond, final int containerIndexCond, final int stateCond, final int valueCond)
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof PieceRename)
			{
				final PieceRename pieceRename = (PieceRename)graphicsItem;
				final RoleType roleType = pieceRename.roleType();
				final String pieceName = pieceRename.pieceName();
				final Integer containerIndex = pieceRename.container();
				final Integer value = pieceRename.value();
				final Integer state = pieceRename.state();
				
				if (roleType == null || MetadataFunctions.getRealOwner(context, roleType) == playerIndexCond)
					if (state == null || state.intValue() == stateCond)
						if (value == null || value.intValue() == valueCond)
							if (containerIndex == null || containerIndex.intValue() == containerIndexCond)
								if (pieceName == null || pieceName.equals(pieceNameCond) || pieceName.equals(StringRoutines.removeTrailingNumbers(pieceNameCond)))
									return pieceRename.nameReplacement();
			}

		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context     			The context.
	 * @param playerIndexCond 		The index of the player.
	 * @param pieceNameCond   		The name of the piece.
	 * @param containerIndexCond	The index of the container.
	 * @param stateCond       		The state.
	 * @param valueCond       		The value.
	 * @return 						The new scale.
	 */
	public Point2D.Float pieceScale(final Context context, final int playerIndexCond, final String pieceNameCond, final int containerIndexCond, final int stateCond, final int valueCond)
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof PieceScale)
			{
				final PieceScale pieceScale = (PieceScale)graphicsItem;
				final RoleType roleType = pieceScale.roleType();
				final String pieceName = pieceScale.pieceName();
				final Integer containerIndex = pieceScale.container();
				final Integer value = pieceScale.value();
				final Integer state = pieceScale.state();
				
				final float scale = pieceScale.scale();
				float scaleX, scaleY;
				if (Math.abs(scale - 1.0) > Constants.EPSILON)
				{
					scaleX = scale;
					scaleY = scale;
				}
				else
				{
					scaleX = pieceScale.scaleX();
					scaleY = pieceScale.scaleY();
				}
				
				if (roleType == null || MetadataFunctions.getRealOwner(context, roleType) == playerIndexCond)
					if (state == null || state.intValue() == stateCond)
						if (value == null || value.intValue() == valueCond)
							if (containerIndex == null || containerIndex.intValue() == containerIndexCond)
								if (pieceName == null || pieceName.equals(pieceNameCond) || pieceName.equals(StringRoutines.removeTrailingNumbers(pieceNameCond)))
									return new Point2D.Float(scaleX, scaleY);
			}

		return new Point2D.Float((float)0.9, (float)0.9);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context     		The context.
	 * @param playerIndexCond 	The index of the player.
	 * @param pieceNameCond   	The name of the piece.
	 * @return 					True if check is used.
	 */
	public boolean checkUsed(final Context context, final int playerIndexCond, final String pieceNameCond)
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof ShowCheck)
			{
				final ShowCheck showCheck = (ShowCheck)graphicsItem;
				final RoleType roleType = showCheck.roleType();
				final String pieceName = showCheck.pieceName();
				
				if (roleType == null || MetadataFunctions.getRealOwner(context, roleType) == playerIndexCond)
					if (pieceName == null || pieceName.equals(pieceNameCond) || pieceName.equals(StringRoutines.removeTrailingNumbers(pieceNameCond)))
						return true;
			}

		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context 	The context.
	 * @return 			The list of the MetadataImageInfo.
	 */
	public ArrayList<MetadataImageInfo> drawSymbol(final Context context)
	{
		final int MAX_ROTATION = 360;  // degrees
		
		final ArrayList<MetadataImageInfo> allSymbols = new ArrayList<>();
		for (final GraphicsItem graphicsItem : items)
		{
			if (graphicsItem instanceof ShowSymbol)
			{
				final ShowSymbol showSymbol = (ShowSymbol)graphicsItem;
				final RoleType roleType = showSymbol.roleType();
				final String imageName = showSymbol.imageName();
				final String text = showSymbol.text();
				final int rotation = showSymbol.rotation();
				final float offsetX = showSymbol.getOffsetX();
				final float offsetY = showSymbol.getOffsetY();
				
				final String region = showSymbol.region();
				final RegionFunction regionFunction = showSymbol.regionFunction();
				final Integer[] sites = showSymbol.sites();
				final SiteType graphElementType = showSymbol.graphElementType(context);
				
				final Colour fillColourMeta = showSymbol.fillColour();
				final Color fillColour = (fillColourMeta == null) ? null : fillColourMeta.colour();
				final Colour edgeColourMeta = showSymbol.edgeColour();
				final Color edgeColour = (edgeColourMeta == null) ? null : edgeColourMeta.colour();
				
				final float scale = showSymbol.scale();
				float scaleX, scaleY;
				if (Math.abs(scale - 1.0) > Constants.EPSILON)
				{
					scaleX = scale;
					scaleY = scale;
				}
				else
				{
					scaleX = showSymbol.scaleX();
					scaleY = showSymbol.scaleY();
				}
				
				if (rotation < 0 || rotation > MAX_ROTATION)
				{
					addError("Rotation for symbol" + imageName + "was equal to " + rotation + ", rotation must be between 0 and " + MAX_ROTATION);
					continue;
				}
				
				if (sites != null)
				{
					for (final Integer site : sites)
					{	
						if (context.game().board().topology().getGraphElements(graphElementType).size() > site.intValue())
							allSymbols.add(new MetadataImageInfo(site.intValue(), graphElementType, imageName, text, scaleX, scaleY, fillColour, edgeColour, rotation, offsetX, offsetY));
						else
							addError("Failed to add symbol " + imageName + " at site " + site.intValue() + " with graphElementType " + graphElementType);
					}	
				}
				else if (region != null)
				{
					for (final ArrayList<Integer> regionSites : MetadataFunctions.convertRegionToSiteArray(context, region, roleType))
					{
						for (final Integer site : regionSites)
						{
							if (context.game().board().topology().getGraphElements(graphElementType).size() > site.intValue())
								allSymbols.add(new MetadataImageInfo(site.intValue(),graphElementType, imageName, text, scaleX, scaleY, fillColour, edgeColour, rotation, offsetX, offsetY));
							else
								addError("Failed to add symbol " + imageName + " at region site " + site.intValue() + " with graphElementType " + graphElementType);	
						}
					}
				}
				else if (regionFunction != null)
				{
					regionFunction.preprocess(context.game());
					for(final int site : regionFunction.eval(context).sites())
					{
						if (context.game().board().topology().getGraphElements(graphElementType).size() > site)
							allSymbols.add(new MetadataImageInfo(site, graphElementType, imageName, text, scaleX, scaleY, fillColour, edgeColour, rotation, offsetX, offsetY));
						else
							addError("Failed to add symbol " + imageName + " at region site " + site + " with graphElementType " + graphElementType);

					}
				}
			}
		}

		return allSymbols;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @param context 	The context.
	 * @return 			The list of the MetadataImageInfo.
	 */
	public ArrayList<MetadataImageInfo> drawLines(final Context context)
	{
		final ArrayList<MetadataImageInfo> allLines = new ArrayList<>();
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof ShowLine)
			{
				final ShowLine showLine = (ShowLine)graphicsItem;
				final Integer[][] lines = showLine.lines();
				final Float[] curve = showLine.curve();
				final float scale = showLine.scale();
				final SiteType siteType = showLine.siteType();
				final CurveType curveType = showLine.curveType();
				final LineStyle lineStyle = showLine.style();
				
				final Colour colourMeta = showLine.colour();
				final Color colour = (colourMeta == null) ? null : colourMeta.colour();
				
				if (lines != null)
				{
					for (final Integer[] line : lines)
					{
						if (context.game().board().topology().vertices().size() > Math.max(line[0].intValue(), line[1].intValue()))
							if (curve == null || curve.length == 4)
								allLines.add(new MetadataImageInfo(line, colour, scale, curve, siteType, curveType, lineStyle));
							else
								addError("Exactly 4 values must be specified for the curve between " + line[0] + " and " + line[1]);
						else
							addError("Failed to draw line between vertices " + line[0] + " and " + line[1]);
					}	
				}
			}

		return allLines;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @param boardGraphicsTypeCond 	The BoardGraphicsType.
	 * @return 							The colour of the board.
	 */
	public Color boardColour(final BoardGraphicsType boardGraphicsTypeCond)
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof BoardColour)
			{
				final BoardColour boardColour = (BoardColour)graphicsItem;
				final BoardGraphicsType boardGraphicsType = boardColour.boardGraphicsType();
				final Colour colourMeta = boardColour.colour();
				
				if (boardGraphicsType == boardGraphicsTypeCond)
					return (colourMeta == null) ? null : colourMeta.colour();
			}

		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return True if the board is hidden.
	 */
	public boolean boardHidden()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof NoBoard)
				return ((NoBoard) graphicsItem).boardHidden();					

		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return True if the region owned has to be showed.
	 */
	public boolean showRegionOwner()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof ShowRegionOwner)
				return ((ShowRegionOwner) graphicsItem).show();					

		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The ContainerStyleType.
	 */
	public ContainerStyleType boardStyle()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof BoardStyle)
				return ((BoardStyle) graphicsItem).containerStyleType();					

		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return If the container is supposed to only render Edges in a special way (pen and paper style).
	 */
	public boolean replaceComponentsWithFilledCells()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof BoardStyle)
				return ((BoardStyle) graphicsItem).replaceComponentsWithFilledCells();					

		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return True if the board is checkered.
	 */
	public boolean checkeredBoard()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof BoardCheckered)
				return ((BoardCheckered) graphicsItem).checkeredBoard();					

		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context	The context.
	 * @return 			True if the regions have to be filled.
	 */
	public ArrayList<ArrayList<MetadataImageInfo>> regionsToFill(final Context context)
	{
		final ArrayList<ArrayList<MetadataImageInfo>> allRegions = new ArrayList<>();
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof RegionColour)
			{
				final RegionColour regionColour = (RegionColour)graphicsItem;
				final RoleType roleType = regionColour.roleType();
				
				final String region = regionColour.region();
				final RegionFunction regionFunction = regionColour.regionFunction();
				final Integer[] sites = regionColour.sites();
				final SiteType graphElementType = regionColour.graphElementType(context);
				final SiteType regionSiteType = regionColour.regionSiteType(context);
				
				final Colour colourMeta = regionColour.colour();
				final Color colour = (colourMeta == null) ? null : colourMeta.colour();
				final float scale = regionColour.getScale();
				
				if (sites != null)
				{
					allRegions.add(new ArrayList<>());
					
					for (final Integer site : sites)
					{
						if (context.game().board().topology().getGraphElements(graphElementType).size() > site.intValue())
						{
							allRegions.get(allRegions.size()-1).add(new MetadataImageInfo(site.intValue(), graphElementType, regionSiteType, colour, scale));
						}
						else
							addError("Failed to fill site " + site.intValue() + " with graphElementType " + graphElementType);
					}
				}
				else if (region != null)
				{
					for (final ArrayList<Integer> regionSiteList : MetadataFunctions.convertRegionToSiteArray(context, region, roleType))
					{
						allRegions.add(new ArrayList<>());
						
						for (final int site : regionSiteList)
						{
							if (context.game().board().topology().getGraphElements(graphElementType).size() > site)
								allRegions.get(allRegions.size()-1).add(new MetadataImageInfo(site, graphElementType, regionSiteType, colour, scale));
							else
								addError("Failed to fill region " + region + "at site " + site + " with graphElementType " + graphElementType);
						}
					}
				}
				else if (regionFunction != null)
				{
					allRegions.add(new ArrayList<>());
					regionFunction.preprocess(context.game());
					
					for (final int site : regionFunction.eval(context).sites())
					{
						if (context.game().board().topology().getGraphElements(graphElementType).size() > site)
							allRegions.get(allRegions.size()-1).add(new MetadataImageInfo(site, graphElementType, regionSiteType, colour, scale));
						else
							addError("Failed to fill region " + region + "at site " + site + " with graphElementType " + graphElementType);
					}
				}
			}
		
		return allRegions;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param boardGraphicsTypeCond 	The BoardGraphicsType.
	 * @return 							The new thickness.
	 */
	public float boardThickness(final BoardGraphicsType boardGraphicsTypeCond)
	{
		final float MAX_THICKNESS = 100f;  // multiplication factor on original size
		
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof BoardStyleThickness)
			{
				final BoardStyleThickness boardStyleThickness = (BoardStyleThickness)graphicsItem;
				final BoardGraphicsType boardGraphicsType = boardStyleThickness.boardGraphicsType();
				final float thickness = boardStyleThickness.thickness();
				
				if (boardGraphicsType.equals(boardGraphicsTypeCond))
					if (thickness >= 0 && thickness <= MAX_THICKNESS)
						return thickness;
					else addError("Scale for board thickness " + boardGraphicsTypeCond.name() + " was equal to " + thickness + ", scale must be between 0 and " + MAX_THICKNESS);
			}
						
		return (float) 1.0;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return 							The new thickness.
	 */
	public float boardCurvature()
	{

		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof BoardCurvature)
				return ((BoardCurvature) graphicsItem).curveOffset();
						
		return (float) 0.333;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return True if the puzzle is adversarial.
	 */
	public boolean adversarialPuzzle()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof AdversarialPuzzle)
				return ((AdversarialPuzzle) graphicsItem).adversarialPuzzle();
	
		return false;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @param context 	The context.
	 * @param playerId 	The player Id
	 * @return 			When to show the score.
	 */
	public ScoreDisplayInfo scoreDisplayInfo(final Context context, final int playerId)
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof ShowScore)
			{
				final ShowScore showScore = (ShowScore)graphicsItem;
				final RoleType roleType = showScore.roleType();
				
				if (roleType == RoleType.All || MetadataFunctions.getRealOwner(context, roleType) == playerId)
					return new ScoreDisplayInfo(showScore.showScore(), roleType, showScore.scoreReplacement(), showScore.scoreSuffix());
			}

		return new ScoreDisplayInfo();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return True if no animation.
	 */
	public boolean noAnimation()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof NoAnimation)
				return ((NoAnimation) graphicsItem).noAnimation();
				
		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return True if no sunken visuals.
	 */
	public boolean noSunken()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof NoSunken)
				return ((NoSunken) graphicsItem).noSunken();
				
		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return True if pips on the dice should be always drawn as a single number.
	 */
	public boolean noDicePips()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof NoDicePips)
				return ((NoDicePips) graphicsItem).noDicePips();
				
		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The list of the suit ranking.
	 */
	public SuitType[] suitRanking()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof SuitRanking)
				return ((SuitRanking) graphicsItem).suitRanking();

		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context   			The context.
	 * @param containerCond 		The container.
	 * @param siteCond      		The site.
	 * @param siteTypeCond  		The graph element type.
	 * @param stateCond     		The state site.
	 * @param valueCond 			The value site.
	 * @param stackPropertyType		The property of the stack being checked.
	 * @return 						The piece stack scale.
	 */
	public double stackMetadata(final Context context, final Container containerCond, final int siteCond, final SiteType siteTypeCond, final int stateCond, final int valueCond, final StackPropertyType stackPropertyType)
	{
		final float MAX_SCALE = 100f;  // multiplication factor on original size.
		final int MAX_LIMIT = 10;  // max height of stack.
		
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof StackType)
			{
				final StackType stackType = (StackType)graphicsItem;
				final RoleType roleType = stackType.roleType();
				final String containerName = stackType.name();
				final Integer containerIndex = stackType.index();
				final Integer[] sites = stackType.sites();
				final SiteType graphElementType = stackType.graphElementType();
				final Integer state = stackType.state();
				final Integer value = stackType.value();
				
				final float scale = stackType.scale();
				final float limit = stackType.limit();
				final PieceStackType pieceStackType = stackType.stackType();
				
				if (roleType == null || MetadataFunctions.getRealOwner(context, roleType) == containerCond.owner())
					if (containerName == null || containerName.equals(containerCond.name()) || containerName.equals(StringRoutines.removeTrailingNumbers(containerCond.name())))
						if (containerIndex == null || containerIndex.equals(Integer.valueOf(containerCond.index())))
							if (sites == null || Arrays.asList(sites).contains(Integer.valueOf(siteCond)) )
								if (graphElementType == null || graphElementType.equals(siteTypeCond))
									if (state == null || state.equals(Integer.valueOf(stateCond)))
										if (value == null || value.equals(Integer.valueOf(valueCond)))
										{
											if (stackPropertyType.equals(StackPropertyType.Scale))
												if (scale >= 0 && scale <= MAX_SCALE)
													return scale;
												else
													addError("Stack scale for role " + roleType + " name " + containerName + " was equal to " + scale + ", scale must be between 0 and " + MAX_SCALE);
											
											else if (stackPropertyType.equals(StackPropertyType.Limit))
												if (limit >= 1 && limit <= MAX_LIMIT)
													return limit;
												else
													addError("Stack scale for role " + roleType + " name " + containerName + " was equal to " + limit + ", limit must be between 1 and " + MAX_LIMIT);
											
											else if (stackPropertyType.equals(StackPropertyType.Type))
												return pieceStackType.ordinal();
										}
			}

		if (stackPropertyType.equals(StackPropertyType.Scale))
			return 1.0;
		else if (stackPropertyType.equals(StackPropertyType.Limit))
			return 5;
		else if (stackPropertyType.equals(StackPropertyType.Type))
			return PieceStackType.Default.ordinal();
		
		return 0;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context     	The context.
	 * @param playerIndex 	The index of the player.
	 * @return 				The colour of a given player index.
	 */
	public Color playerColour(final Context context, final int playerIndex)
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof PlayerColour)
			{
				final PlayerColour playerColour = ((PlayerColour) graphicsItem);
				final RoleType roleType = playerColour.roleType();
				final Colour colourMeta = playerColour.colour();
				
				if (MetadataFunctions.getRealOwner(context, roleType) == playerIndex)
					return (colourMeta == null) ? null : colourMeta.colour();
			}

		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * 
	 * @param context     	The context.
	 * @param playerIndex 	The index of the player.
	 * @return 				The name of a given player index.
	 */
	public String playerName(final Context context, final int playerIndex)
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof PlayerName)
			{
				final PlayerName playerName = ((PlayerName) graphicsItem);
				final RoleType roleType = playerName.roleType();
				
				if (MetadataFunctions.getRealOwner(context, roleType) == playerIndex)
					return playerName.name();
			}

		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return True if the sites has to be drawn like a hole.
	 */
	public int[] sitesAsSpecialHoles()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof ShowSitesAsHoles)
				return ((ShowSitesAsHoles) graphicsItem).indices();

		return new int[0];
	}
	
	/**
	 * @return The shape of the holes.
	 */
	public HoleType shapeSpecialHole()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof ShowSitesAsHoles)
				return ((ShowSitesAsHoles) graphicsItem).type();

		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param game The game.
	 * @param element The topology element.
	 * @return Returns the additional value to add to this sites index when displayed, Null if no value to be shown.
	 */
	public Integer showSiteIndex(final Game game, final TopologyElement element)
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof ShowSitesIndex)
				if (((ShowSitesIndex) graphicsItem).type().equals(element.elementType()))
					return ((ShowSitesIndex) graphicsItem).additionalValue();

		return null;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return True If the player holes have to be showed.
	 */
	public boolean showPlayerHoles()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof ShowPlayerHoles)
				return ((ShowPlayerHoles) graphicsItem).showPlayerHoles();
		
		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return True If the holes with a local state of zero should be marked.
	 */
	public boolean holesUseLocalState()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof ShowLocalStateHoles)
				return ((ShowLocalStateHoles) graphicsItem).useLocalState();
		
		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param edgeTypeCond      The edge type.
	 * @param relationTypeCond 	The relation type.
	 * @param connectionCond  	True if this is a connection.
	 * @return The EdgeInfoGUI.
	 */
	public EdgeInfoGUI drawEdge(final EdgeType edgeTypeCond, final RelationType relationTypeCond, final boolean connectionCond)
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof ShowEdges)
			{
				final ShowEdges showEdges = ((ShowEdges) graphicsItem);
				final EdgeType edgeType = showEdges.edgeType();
				final RelationType relationType = showEdges.relationType();
				final Boolean connection = showEdges.connection();
				
				if (edgeType.supersetOf(edgeTypeCond))
					if (relationType.supersetOf(relationTypeCond))
						if (connection.booleanValue() == connectionCond)
							return new EdgeInfoGUI(showEdges.style(), showEdges.colour().colour());
			}
		
		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return true to show the pits.
	 */
	public boolean showPits()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof ShowPits)
				return ((ShowPits) graphicsItem).showPits();

		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return True to show the costs.
	 */
	public boolean showCost()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof ShowCost)
				return ((ShowCost) graphicsItem).showCost();

		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return True to show the hints.
	 */
	public PuzzleHintLocationType hintLocationType()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof HintLocation)
				return ((HintLocation) graphicsItem).hintLocation();

		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return True to show the hints.
	 */
	public PuzzleDrawHintType drawHintType()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof DrawHint)
				return ((DrawHint) graphicsItem).drawHint();

		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return True to show the costs.
	 */
	public boolean showEdgeDirections()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof ShowEdgeDirections)
				return ((ShowEdgeDirections) graphicsItem).showEdgeDirections();

		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return True to show curved edges.
	 */
	public boolean showCurvedEdges()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof ShowCurvedEdges)
				return ((ShowCurvedEdges) graphicsItem).showCurvedEdges();

		return true;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return True to show straight edges.
	 */
	public boolean showStraightEdges()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof ShowStraightEdges)
				return ((ShowStraightEdges) graphicsItem).showStraightEdges();

		return true;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return True to show the costs.
	 */
	public boolean showPossibleMoves()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof ShowPossibleMoves)
				return ((ShowPossibleMoves) graphicsItem).showPossibleMoves();

		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return True to draw the lines of the ring with straight lines.
	 */
	public boolean straightRingLines()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof NoCurves)
				return ((NoCurves) graphicsItem).straightRingLines();

		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The shape of the cell.
	 */
	public ShapeType cellShape()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof ShowSitesShape)
				return ((ShowSitesShape) graphicsItem).shape();

		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The shape of the board.
	 */
	public ShapeType boardShape()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof BoardShape)
				return ((BoardShape) graphicsItem).shape();

		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The placement of the board.
	 */
	public Rectangle2D boardPlacement()
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof BoardPlacement)
			{
				final BoardPlacement boardPlacement = ((BoardPlacement) graphicsItem);
				return (new Rectangle2D.Double(boardPlacement.offsetX(), boardPlacement.offsetY(), boardPlacement.scale(), boardPlacement.scale()));
			}
		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context 
	 * @param playerId 
	 * @return The placement of the hand.
	 */
	public Rectangle2D handPlacement(final Context context, final int playerId)
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof HandPlacement)
			{
				final HandPlacement handPlacement = ((HandPlacement) graphicsItem);
				
				if (new Id(null,handPlacement.getPlayer()).eval(context) == playerId)
					return (new Rectangle2D.Double(handPlacement.offsetX(), handPlacement.offsetY(), handPlacement.scale(), handPlacement.scale()));
			}
		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param context
	 * @param playerId 
	 * @return If orientation of a hand is vertical.
	 */
	public boolean handVertical(final Context context, final int playerId)
	{
		for (final GraphicsItem graphicsItem : items)
			if (graphicsItem instanceof HandPlacement)
			{
				final HandPlacement handPlacement = ((HandPlacement) graphicsItem);
				
				if (new Id(null,handPlacement.getPlayer()).eval(context) == playerId)
					return handPlacement.isVertical();
			}
		return false;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		final String open  = (items.size() <= 1) ? "" : "{";
		final String close = (items.size() <= 1) ? "" : "}";
		
		sb.append("    (graphics " + open + "\n");
		for (final GraphicsItem item : items)
			if (item != null)
				sb.append("        " + item.toString());
		sb.append("    " + close + ")\n");

		return sb.toString();
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Add error to errorReport.
	 */
	private void addError(final String string) 
	{
		errorReport += "Error: " + string + "\n";
	}
	
	/**
	 * @return errorReport.
	 */
	public String getErrorReport()
	{
		return errorReport;
	}
	
	/**
	 * Set errorReport.
	 * 
	 * @param s The report.
	 */
	public void setErrorReport(final String s)
	{
		errorReport = s;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param game The game.
	 * @return Accumulated concepts.
	 */
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		for (final GraphicsItem item : items)
			if (item != null)
				concepts.or(item.concepts(game));
		return concepts;
	}

	/**
	 * @param game The game.
	 * @return Accumulated concepts.
	 */
	public long gameFlags(final Game game)
	{
		long gameFlags = 0l;
		for (final GraphicsItem item : items)
			if (item != null)
				gameFlags |= item.gameFlags(game);
		return gameFlags;
	}

	/**
	 * Compute if the game needs to be redrawn.
	 * @param game The game.
	 */
	public void computeNeedRedraw(final Game game)
	{
		for (final Regions region : game.equipment().regions())
			if (!region.isStatic())
				needRedraw = true;

		for (final GraphicsItem item : items)
			if (item != null && item.needRedraw())
				needRedraw = true;
	}

	/**
	 * @return True if the graphics need to be redrawn after each move.
	 */
	public boolean needRedrawn()
	{
		return needRedraw;
	}
	
}
