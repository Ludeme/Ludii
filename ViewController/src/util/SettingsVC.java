package util;

import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import main.Constants;
import other.action.Action;
import other.location.FullLocation;
import other.location.Location;
import other.move.Move;

/**
 * User settings specific to the ViewController.
 *
 * @author matthew.stephenson and cambolbro and Eric.Piette
 */
public final class SettingsVC
{
	//-------------------------------------------------------------------------
	// User settings

	/** Show indices of every cell. */
	private boolean showCellIndices = false;

	/** Show indices of every edge. */
	private boolean showEdgeIndices = false;

	/** Show indices of every vertex. */
	private boolean showVertexIndices = false;
	
	/** Show indices of every container. */
	private boolean showContainerIndices = false;

	/** Show coordinates of every cell */
	private boolean showCellCoordinates = false;
	
	/** Show coordinates of every cell */
	private boolean showEdgeCoordinates = false;
	
	/** Show coordinates of every cell */
	private boolean showVertexCoordinates = false;
	
	/** Show indices of every relevant graph element for this game */
	private boolean showIndices = false;
	
	/** Show coordinates of every relevant graph element for this game */
	private boolean showCoordinates = false;

	/** Whether to show all possible moves that the player can select*/
	private boolean showPossibleMoves = false;

	/** Whether to display certain tracks */
	private ArrayList<String> trackNames = new ArrayList<>();
	private ArrayList<Boolean> trackShown = new ArrayList<>();
	
	/** The location that is currently selected by the user. */
	private Location selectedFromLocation = new FullLocation(Constants.UNDEFINED);

	/** Extension text to add onto the end of piece names when searching for the correct SVG image. */
	private String pieceStyleExtension = "";

	/** Font used when displaying text. */
	private Font displayFont;
	
	/** Whether the board should be drawn flat. */
	private boolean flatBoard = false;
		
	//-------------------------------------------------------------------------
	// Developer pre-generation display settings
	
	private boolean drawCornerCells = false;
	private boolean drawCornerConcaveCells = false;
	private boolean drawCornerConvexCells = false;
	private boolean drawMajorCells = false;
	private boolean drawMinorCells = false;
	private boolean drawOuterCells = false;
	private boolean drawPerimeterCells = false;
	private boolean drawInnerCells = false;
	private boolean drawTopCells = false;
	private boolean drawBottomCells = false;
	private boolean drawLeftCells = false;
	private boolean drawRightCells = false;
	private boolean drawCenterCells = false;
	private boolean drawPhasesCells = false;
	private Map<String, Boolean> drawSideCells = new HashMap<String, Boolean>();
	private boolean drawNeighboursCells = false;
	private boolean drawRadialsCells = false;
	private boolean drawDistanceCells = false;
	
	private boolean drawCornerVertices = false;
	private boolean drawCornerConcaveVertices = false;
	private boolean drawCornerConvexVertices = false;
	private boolean drawMajorVertices = false;
	private boolean drawMinorVertices = false;
	private boolean drawOuterVertices = false;
	private boolean drawPerimeterVertices = false;
	private boolean drawInnerVertices = false;
	private boolean drawTopVertices = false;
	private boolean drawBottomVertices = false;
	private boolean drawLeftVertices = false;
	private boolean drawRightVertices = false;
	private boolean drawCenterVertices = false;
	private boolean drawPhasesVertices = false;
	private Map<String, Boolean> drawSideVertices = new HashMap<String, Boolean>();
	private boolean drawNeighboursVertices = false;
	private boolean drawRadialsVertices = false;
	private boolean drawDistanceVertices = false;

	private boolean drawCentreEdges = false;
	private boolean drawCornerEdges = false;
	private boolean drawCornerConcaveEdges = false;
	private boolean drawCornerConvexEdges = false;
	private boolean drawMajorEdges = false;
	private boolean drawMinorEdges = false;
	private boolean drawOuterEdges = false;
	private boolean drawPerimeterEdges = false;
	private boolean drawInnerEdges = false;
	private boolean drawTopEdges = false;
	private boolean drawBottomEdges = false;
	private boolean drawLeftEdges = false;
	private boolean drawRightEdges = false;
	private boolean drawDistanceEdges = false;
	private boolean drawPhasesEdges = false;
	private Map<String, Boolean> drawSideEdges = new HashMap<String, Boolean>();
	private boolean drawAxialEdges = false;
	private boolean drawHorizontalEdges = false;
	private boolean drawVerticalEdges = false;
	private boolean drawAngledEdges = false;
	private boolean drawSlashEdges = false;
	private boolean drawSloshEdges = false;

	private boolean drawFacesOfVertices = false;
	private boolean drawEdgesOfVertices = false;
	private boolean drawVerticesOfFaces = false;
	private boolean drawEdgesOfFaces = false;
	private boolean drawVerticesOfEdges = false;
	private boolean drawFacesOfEdges = false;
	
	private Location lastClickedSite = new FullLocation(Constants.UNDEFINED);
	
	private ArrayList<Boolean> drawColumnsCells = new ArrayList<>();
	private ArrayList<Boolean> drawRowsCells = new ArrayList<>();
	private ArrayList<Boolean> drawColumnsVertices = new ArrayList<>();
	private ArrayList<Boolean> drawRowsVertices = new ArrayList<>();

	//-------------------------------------------------------------------------
	// Variables used for multiple consequence selection.
	
	/** If the user is currently selecting a possible consequence to the prior move. */
	private boolean selectingConsequenceMove = false;
	
	/** List of possible consequence locations. */
	private ArrayList<Location> possibleConsequenceLocations = new ArrayList<>();
	
	//-------------------------------------------------------------------------
	// Animation
	
	/** If the animation for the current Frame has already started. */
	private boolean thisFrameIsAnimated = false;
	
	/** If all movement animation is disabled for the current game. */
	private boolean noAnimation = false;
	
	/** From index for the animation. */
	private Move animationMove = new Move(new ArrayList<Action>());
	
	//-------------------------------------------------------------------------
	// Other
	
	/** If a piece is being dragged. */
	private boolean pieceBeingDragged = false;

	/** Whether to show candidate values on puzzle boards. */
	private boolean showCandidateValues = false;

	/** The last error message that was reported. Stored to prevent the same error message being repeated multiple times. */
	private String lastErrorMessage = "";
	
	/** If the coordinates/indices should be drawn with a white outline. */
	private boolean coordWithOutline = false;
	
	/** Errors that were recorded when rendering or painting some VC aspect. **/
	private String errorReport = "";
	
	/** Map of all piece families for displaying different piece styles, e.g. Chess. */
	private HashMap<String,String> pieceFamilies = new HashMap<String,String>();
	
	/** Multiplier, applied to cell radius, for determining minimumal click distance. */
	private double furthestDistanceMultiplier = 0.9;

	//-------------------------------------------------------------------------
	// Getters and setters

	public boolean showCellIndices()
	{
		return showCellIndices;
	}

	public void setShowCellIndices(final boolean showCellIndices)
	{
		this.showCellIndices = showCellIndices;
	}

	public boolean showEdgeIndices()
	{
		return showEdgeIndices;
	}

	public void setShowEdgeIndices(final boolean show)
	{
		showEdgeIndices = show;
	}

	public boolean showVertexIndices()
	{
		return showVertexIndices;
	}

	public void setShowVertexIndices(final boolean show)
	{
		showVertexIndices = show;
	}

	public boolean showContainerIndices()
	{
		return showContainerIndices;
	}

	public void setShowContainerIndices(final boolean show)
	{
		showContainerIndices = show;
	}

	public boolean showCellCoordinates()
	{
		return showCellCoordinates;
	}

	public void setShowCellCoordinates(final boolean show)
	{
		showCellCoordinates = show;
	}

	public boolean showEdgeCoordinates()
	{
		return showEdgeCoordinates;
	}

	public void setShowEdgeCoordinates(final boolean show)
	{
		showEdgeCoordinates = show;
	}

	public boolean showVertexCoordinates()
	{
		return showVertexCoordinates;
	}

	public void setShowVertexCoordinates(final boolean show)
	{
		showVertexCoordinates = show;
	}

	public boolean showIndices()
	{
		return showIndices;
	}

	public void setShowIndices(final boolean show)
	{
		showIndices = show;
	}

	public boolean showCoordinates()
	{
		return showCoordinates;
	}

	public void setShowCoordinates(final boolean show)
	{
		showCoordinates = show;
	}

	public boolean showPossibleMoves()
	{
		return showPossibleMoves;
	}

	public void setShowPossibleMoves(final boolean show)
	{
		showPossibleMoves = show;
	}

	public ArrayList<String> trackNames()
	{
		return trackNames;
	}

	public void setTrackNames(final ArrayList<String> names)
	{
		trackNames = names;
	}

	public ArrayList<Boolean> trackShown()
	{
		return trackShown;
	}

	public void setTrackShown(final ArrayList<Boolean> shown)
	{
		trackShown = shown;
	}

	public Location selectedFromLocation()
	{
		return selectedFromLocation;
	}

	public void setSelectedFromLocation(final Location selected)
	{
		selectedFromLocation = selected;
	}

	public String pieceStyleExtension()
	{
		return pieceStyleExtension;
	}

	public void setPieceStyleExtension(final String extension)
	{
		pieceStyleExtension = extension;
	}

	public Font displayFont()
	{
		return displayFont;
	}

	public void setDisplayFont(final Font font)
	{
		displayFont = font;
	}

	public boolean flatBoard()
	{
		return flatBoard;
	}

	public void setFlatBoard(final boolean flat)
	{
		flatBoard = flat;
	}

	public boolean drawCornerCells()
	{
		return drawCornerCells;
	}

	public void setDrawCornerCells(final boolean draw)
	{
		drawCornerCells = draw;
	}

	public boolean drawCornerConcaveCells()
	{
		return drawCornerConcaveCells;
	}

	public void setDrawCornerConcaveCells(final boolean draw)
	{
		drawCornerConcaveCells = draw;
	}

	public boolean drawCornerConvexCells()
	{
		return drawCornerConvexCells;
	}

	public void setDrawCornerConvexCells(final boolean draw)
	{
		drawCornerConvexCells = draw;
	}

	public boolean drawMajorCells()
	{
		return drawMajorCells;
	}

	public void setDrawMajorCells(final boolean draw)
	{
		drawMajorCells = draw;
	}

	public boolean drawMinorCells()
	{
		return drawMinorCells;
	}

	public void setDrawMinorCells(final boolean draw)
	{
		drawMinorCells = draw;
	}

	public boolean drawOuterCells()
	{
		return drawOuterCells;
	}

	public void setDrawOuterCells(final boolean draw)
	{
		drawOuterCells = draw;
	}

	public boolean drawPerimeterCells()
	{
		return drawPerimeterCells;
	}

	public void setDrawPerimeterCells(final boolean draw)
	{
		drawPerimeterCells = draw;
	}

	public boolean drawInnerCells()
	{
		return drawInnerCells;
	}

	public  void setDrawInnerCells(final boolean draw)
	{
		drawInnerCells = draw;
	}

	public  boolean drawTopCells()
	{
		return drawTopCells;
	}

	public  void setDrawTopCells(final boolean draw)
	{
		drawTopCells = draw;
	}

	public  boolean drawBottomCells()
	{
		return drawBottomCells;
	}

	public  void setDrawBottomCells(final boolean draw)
	{
		drawBottomCells = draw;
	}

	public  boolean drawLeftCells()
	{
		return drawLeftCells;
	}

	public  void setDrawLeftCells(final boolean draw)
	{
		drawLeftCells = draw;
	}

	public  boolean drawRightCells()
	{
		return drawRightCells;
	}

	public  void setDrawRightCells(final boolean draw)
	{
		drawRightCells = draw;
	}

	public  boolean drawCenterCells()
	{
		return drawCenterCells;
	}

	public  void setDrawCenterCells(final boolean draw)
	{
		drawCenterCells = draw;
	}

	public  boolean drawPhasesCells()
	{
		return drawPhasesCells;
	}

	public  void setDrawPhasesCells(final boolean draw)
	{
		drawPhasesCells = draw;
	}

	public  Map<String, Boolean> drawSideCells()
	{
		return drawSideCells;
	}

	public  void setDrawSideCells(final Map<String, Boolean> draw)
	{
		drawSideCells = draw;
	}

	public  boolean drawNeighboursCells()
	{
		return drawNeighboursCells;
	}

	public  void setDrawNeighboursCells(final boolean draw)
	{
		drawNeighboursCells = draw;
	}

	public  boolean drawRadialsCells()
	{
		return drawRadialsCells;
	}

	public  void setDrawRadialsCells(final boolean draw)
	{
		drawRadialsCells = draw;
	}

	public  boolean drawDistanceCells()
	{
		return drawDistanceCells;
	}

	public  void setDrawDistanceCells(final boolean draw)
	{
		drawDistanceCells = draw;
	}

	public  boolean drawCornerVertices()
	{
		return drawCornerVertices;
	}

	public  void setDrawCornerVertices(final boolean draw)
	{
		drawCornerVertices = draw;
	}

	public  boolean drawCornerConcaveVertices()
	{
		return drawCornerConcaveVertices;
	}

	public  void setDrawCornerConcaveVertices(final boolean draw)
	{
		drawCornerConcaveVertices = draw;
	}

	public  boolean drawCornerConvexVertices()
	{
		return drawCornerConvexVertices;
	}

	public  void setDrawCornerConvexVertices(final boolean draw)
	{
		drawCornerConvexVertices = draw;
	}

	public  boolean drawMajorVertices()
	{
		return drawMajorVertices;
	}

	public  void setDrawMajorVertices(final boolean draw)
	{
		drawMajorVertices = draw;
	}

	public  boolean drawMinorVertices()
	{
		return drawMinorVertices;
	}

	public  void setDrawMinorVertices(final boolean draw)
	{
		drawMinorVertices = draw;
	}

	public  boolean drawOuterVertices()
	{
		return drawOuterVertices;
	}

	public  void setDrawOuterVertices(final boolean draw)
	{
		drawOuterVertices = draw;
	}

	public  boolean drawPerimeterVertices()
	{
		return drawPerimeterVertices;
	}

	public  void setDrawPerimeterVertices(final boolean draw)
	{
		drawPerimeterVertices = draw;
	}

	public  boolean drawInnerVertices()
	{
		return drawInnerVertices;
	}

	public  void setDrawInnerVertices(final boolean draw)
	{
		drawInnerVertices = draw;
	}

	public  boolean drawTopVertices()
	{
		return drawTopVertices;
	}

	public  void setDrawTopVertices(final boolean draw)
	{
		drawTopVertices = draw;
	}

	public  boolean drawBottomVertices()
	{
		return drawBottomVertices;
	}

	public  void setDrawBottomVertices(final boolean draw)
	{
		drawBottomVertices = draw;
	}

	public  boolean drawLeftVertices()
	{
		return drawLeftVertices;
	}

	public  void setDrawLeftVertices(final boolean draw)
	{
		drawLeftVertices = draw;
	}

	public  boolean drawRightVertices()
	{
		return drawRightVertices;
	}

	public  void setDrawRightVertices(final boolean draw)
	{
		drawRightVertices = draw;
	}

	public  boolean drawCenterVertices()
	{
		return drawCenterVertices;
	}

	public  void setDrawCenterVertices(final boolean draw)
	{
		drawCenterVertices = draw;
	}

	public  boolean drawPhasesVertices()
	{
		return drawPhasesVertices;
	}

	public  void setDrawPhasesVertices(final boolean draw)
	{
		drawPhasesVertices = draw;
	}

	public  Map<String, Boolean> drawSideVertices()
	{
		return drawSideVertices;
	}

	public  void setDrawSideVertices(final Map<String, Boolean> draw)
	{
		drawSideVertices = draw;
	}

	public  boolean drawNeighboursVertices()
	{
		return drawNeighboursVertices;
	}

	public  void setDrawNeighboursVertices(final boolean draw)
	{
		drawNeighboursVertices = draw;
	}

	public  boolean drawRadialsVertices()
	{
		return drawRadialsVertices;
	}

	public  void setDrawRadialsVertices(final boolean draw)
	{
		drawRadialsVertices = draw;
	}

	public  boolean drawDistanceVertices()
	{
		return drawDistanceVertices;
	}

	public  void setDrawDistanceVertices(final boolean draw)
	{
		drawDistanceVertices = draw;
	}

	public  boolean drawCentreEdges()
	{
		return drawCentreEdges;
	}

	public  void setDrawCentreEdges(final boolean draw)
	{
		drawCentreEdges = draw;
	}

	public  boolean drawCornerEdges()
	{
		return drawCornerEdges;
	}

	public  void setDrawCornerEdges(final boolean draw)
	{
		drawCornerEdges = draw;
	}

	public  boolean drawCornerConcaveEdges()
	{
		return drawCornerConcaveEdges;
	}

	public  void setDrawCornerConcaveEdges(final boolean draw)
	{
		drawCornerConcaveEdges = draw;
	}

	public  boolean drawCornerConvexEdges()
	{
		return drawCornerConvexEdges;
	}

	public  void setDrawCornerConvexEdges(final boolean draw)
	{
		drawCornerConvexEdges = draw;
	}

	public  boolean drawMajorEdges()
	{
		return drawMajorEdges;
	}

	public  void setDrawMajorEdges(final boolean draw)
	{
		drawMajorEdges = draw;
	}

	public  boolean drawMinorEdges()
	{
		return drawMinorEdges;
	}

	public  void setDrawMinorEdges(final boolean draw)
	{
		drawMinorEdges = draw;
	}

	public  boolean drawOuterEdges()
	{
		return drawOuterEdges;
	}

	public  void setDrawOuterEdges(final boolean draw)
	{
		drawOuterEdges = draw;
	}

	public  boolean drawPerimeterEdges()
	{
		return drawPerimeterEdges;
	}

	public  void setDrawPerimeterEdges(final boolean draw)
	{
		drawPerimeterEdges = draw;
	}

	public  boolean drawInnerEdges()
	{
		return drawInnerEdges;
	}

	public  void setDrawInnerEdges(final boolean draw)
	{
		drawInnerEdges = draw;
	}

	public  boolean drawTopEdges()
	{
		return drawTopEdges;
	}

	public  void setDrawTopEdges(final boolean draw)
	{
		drawTopEdges = draw;
	}

	public  boolean drawBottomEdges()
	{
		return drawBottomEdges;
	}

	public  void setDrawBottomEdges(final boolean draw)
	{
		drawBottomEdges = draw;
	}

	public  boolean drawLeftEdges()
	{
		return drawLeftEdges;
	}

	public  void setDrawLeftEdges(final boolean draw)
	{
		drawLeftEdges = draw;
	}

	public  boolean drawRightEdges()
	{
		return drawRightEdges;
	}

	public  void setDrawRightEdges(final boolean draw)
	{
		drawRightEdges = draw;
	}

	public  boolean drawDistanceEdges()
	{
		return drawDistanceEdges;
	}

	public  void setDrawDistanceEdges(final boolean draw)
	{
		drawDistanceEdges = draw;
	}

	public  boolean drawPhasesEdges()
	{
		return drawPhasesEdges;
	}

	public  void setDrawPhasesEdges(final boolean draw)
	{
		drawPhasesEdges = draw;
	}

	public  Map<String, Boolean> drawSideEdges()
	{
		return drawSideEdges;
	}

	public  void setDrawSideEdges(final Map<String, Boolean> draw)
	{
		drawSideEdges = draw;
	}

	public  boolean drawAxialEdges()
	{
		return drawAxialEdges;
	}

	public  void setDrawAxialEdges(final boolean draw)
	{
		drawAxialEdges = draw;
	}

	public  boolean drawHorizontalEdges()
	{
		return drawHorizontalEdges;
	}

	public  void setDrawHorizontalEdges(final boolean draw)
	{
		drawHorizontalEdges = draw;
	}

	public  boolean drawVerticalEdges()
	{
		return drawVerticalEdges;
	}

	public  void setDrawVerticalEdges(final boolean draw)
	{
		drawVerticalEdges = draw;
	}

	public  boolean drawAngledEdges()
	{
		return drawAngledEdges;
	}

	public  void setDrawAngledEdges(final boolean draw)
	{
		drawAngledEdges = draw;
	}

	public  boolean drawSlashEdges()
	{
		return drawSlashEdges;
	}

	public  void setDrawSlashEdges(final boolean draw)
	{
		drawSlashEdges = draw;
	}

	public  boolean drawSloshEdges()
	{
		return drawSloshEdges;
	}

	public  void setDrawSloshEdges(final boolean draw)
	{
		drawSloshEdges = draw;
	}

	public  boolean drawFacesOfVertices()
	{
		return drawFacesOfVertices;
	}

	public  void setDrawFacesOfVertices(final boolean draw)
	{
		drawFacesOfVertices = draw;
	}

	public  boolean drawEdgesOfVertices()
	{
		return drawEdgesOfVertices;
	}

	public  void setDrawEdgesOfVertices(final boolean draw)
	{
		drawEdgesOfVertices = draw;
	}

	public  boolean drawVerticesOfFaces()
	{
		return drawVerticesOfFaces;
	}

	public  void setDrawVerticesOfFaces(final boolean draw)
	{
		drawVerticesOfFaces = draw;
	}

	public  boolean drawEdgesOfFaces()
	{
		return drawEdgesOfFaces;
	}

	public  void setDrawEdgesOfFaces(final boolean draw)
	{
		drawEdgesOfFaces = draw;
	}

	public  boolean drawVerticesOfEdges()
	{
		return drawVerticesOfEdges;
	}

	public  void setDrawVerticesOfEdges(final boolean draw)
	{
		drawVerticesOfEdges = draw;
	}

	public  boolean drawFacesOfEdges()
	{
		return drawFacesOfEdges;
	}

	public  void setDrawFacesOfEdges(final boolean draw)
	{
		drawFacesOfEdges = draw;
	}

	public  Location lastClickedSite()
	{
		return lastClickedSite;
	}

	public  void setLastClickedSite(final Location last)
	{
		lastClickedSite = last;
	}

	public  ArrayList<Boolean> drawColumnsCells()
	{
		return drawColumnsCells;
	}

	public void setDrawColumnsCells(final ArrayList<Boolean> draw)
	{
		drawColumnsCells = draw;
	}

	public ArrayList<Boolean> drawRowsCells()
	{
		return drawRowsCells;
	}

	public void setDrawRowsCells(final ArrayList<Boolean> draw)
	{
		drawRowsCells = draw;
	}

	public ArrayList<Boolean> drawColumnsVertices()
	{
		return drawColumnsVertices;
	}

	public void setDrawColumnsVertices(final ArrayList<Boolean> draw)
	{
		drawColumnsVertices = draw;
	}

	public ArrayList<Boolean> drawRowsVertices()
	{
		return drawRowsVertices;
	}

	public void setDrawRowsVertices(final ArrayList<Boolean> draw)
	{
		drawRowsVertices = draw;
	}

	public boolean selectingConsequenceMove()
	{
		return selectingConsequenceMove;
	}

	public void setSelectingConsequenceMove(final boolean selecting)
	{
		selectingConsequenceMove = selecting;
	}

	public ArrayList<Location> possibleConsequenceLocations()
	{
		return possibleConsequenceLocations;
	}

	public void setPossibleConsequenceLocations(final ArrayList<Location> possible)
	{
		possibleConsequenceLocations = possible;
	}

	public boolean pieceBeingDragged()
	{
		return pieceBeingDragged;
	}

	public void setPieceBeingDragged(final boolean dragged)
	{
		pieceBeingDragged = dragged;
	}

	public boolean thisFrameIsAnimated()
	{
		return thisFrameIsAnimated;
	}

	public void setThisFrameIsAnimated(final boolean animated)
	{
		thisFrameIsAnimated = animated;
	}

	public boolean showCandidateValues()
	{
		return showCandidateValues;
	}

	public void setShowCandidateValues(final boolean show)
	{
		showCandidateValues = show;
	}

	public String lastErrorMessage()
	{
		return lastErrorMessage;
	}

	public void setLastErrorMessage(final String last)
	{
		lastErrorMessage = last;
	}

	public boolean noAnimation()
	{
		return noAnimation;
	}

	public void setNoAnimation(final boolean no)
	{
		noAnimation = no;
	}

	public boolean coordWithOutline()
	{
		return coordWithOutline;
	}

	public void setCoordWithOutline(final boolean outline)
	{
		coordWithOutline = outline;
	}

	public String errorReport()
	{
		return errorReport;
	}

	public void setErrorReport(final String report)
	{
		errorReport = report;
	}

	public HashMap<String, String> pieceFamilies()
	{
		return pieceFamilies;
	}

	public void setPieceFamilies(final HashMap<String, String> families)
	{
		pieceFamilies = families;
	}
	
	public String pieceFamily(final String gameName)
	{
		if (pieceFamilies.containsKey(gameName))
			return pieceFamilies.get(gameName);
		return "";
	}
	
	public void setPieceFamily(final String gameName, final String pieceFamily)
	{
		pieceFamilies.put(gameName, pieceFamily);
	}
	
	public Move getAnimationMove()
	{
		return animationMove;
	}

	public void setAnimationMove(final Move animationMove)
	{
		this.animationMove = animationMove;
	}

	public double furthestDistanceMultiplier() {
		return furthestDistanceMultiplier;
	}

	public void setFurthestDistanceMultiplier(final double furthestDistanceMultiplier) {
		this.furthestDistanceMultiplier = furthestDistanceMultiplier;
	}
}
