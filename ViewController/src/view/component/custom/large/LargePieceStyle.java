package view.component.custom.large;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import bridge.ViewControllerFactory;
import game.equipment.component.Component;
import game.equipment.container.board.Board;
import game.functions.dim.DimConstant;
import game.functions.graph.generators.basis.hex.HexagonOnHex;
import game.functions.graph.generators.basis.square.RectangleOnSquare;
import game.functions.graph.generators.basis.tri.TriangleOnTri;
import game.types.board.SiteType;
import gnu.trove.list.array.TIntArrayList;
import graphics.svg.SVGtoImage;
import other.context.Context;
import other.topology.Cell;
import view.container.ContainerStyle;

/**
 * Implementation of large piece component style. 
 * 
 * @author matthew.stephenson
 */
public class LargePieceStyle extends TileStyle
{
	/** In case of large Piece, here is the size. */
	protected Point size;

	/** In case of large Piece, here is the origin of the piece. */
	private final ArrayList<Point> origin = new ArrayList<>();
	
	/** List of offsets used for each large piece state. */
	protected ArrayList<Point2D> largeOffsets = new ArrayList<>();

	/** Cell locations on boardForLargePiece are adjust each time we create an image, so they need to be stored and reset afterwards. */
	protected ArrayList<Point2D> originalCellLocations = new ArrayList<>();
	
	//----------------------------------------------------------------------------
	
	public LargePieceStyle(final Bridge bridge, final Component component) 
	{
		super(bridge, component);
	}
	
	//----------------------------------------------------------------------------
	
	@Override
	public SVGGraphics2D getSVGImageFromFilePath(final SVGGraphics2D g2dOriginal, final Context context, final int imageSize, final String filePath, 
			final int containerIndex, final int localState, final int value, final int hiddenValue, final int rotation, final boolean secondary)
	{		
		// Calculate the maximum size that this piece could be.
		final int maxStepsForward = component.maxStepsForward() + 1;
		final int pieceScale = maxStepsForward*2 + 1;
		
		// Board that is "walked" on when creating images of large pieces.
		Board boardForLargePiece;
		
		final int numEdges = component.numSides();
		if (numEdges == 3)
			boardForLargePiece = new Board(new TriangleOnTri(new DimConstant(pieceScale)), null, null, null, null, null, Boolean.valueOf(false));
		else if (numEdges == 6)
			boardForLargePiece = new Board(new HexagonOnHex(new DimConstant(pieceScale)), null, null, null, null, null, Boolean.valueOf(false));
		else if (numEdges == 4)
			boardForLargePiece = new Board(new RectangleOnSquare(new DimConstant(pieceScale), null, null, null), null, null, null, null, null, Boolean.valueOf(false));
		else
			return null;	// Large pieces are not possible for a component with this many sides.
		
		boardForLargePiece.createTopology(0, context.board().topology().edges().size());
		boardForLargePiece.setTopology(boardForLargePiece.topology());
		boardForLargePiece.topology().computeSupportedDirection(SiteType.Cell);
		boardForLargePiece.setStyle(context.board().style());
		
		for (final Cell c : boardForLargePiece.topology().cells())
			originalCellLocations.add(c.centroid());
		
		final ContainerStyle boardForLargePieceStyle = ViewControllerFactory.createStyle(bridge, boardForLargePiece, boardForLargePiece.style(), context);
		
		// Calculate the cell locations of the graph based on the piece walk, later user for creating the piece image.
		final TIntArrayList cellLocations = component.locs(context, boardForLargePiece.numSites()/2 + 1, localState, boardForLargePiece.topology());
		final double boardSizeDif = (bridge.getContainerStyle(context.board().index()).cellRadius() / boardForLargePieceStyle.cellRadius()) * bridge.getContainerStyle(context.board().index()).containerZoom();
		
		final int boardForLargePieceSize = (int) (bridge.getContainerStyle(0).placement().getWidth() * boardSizeDif);

		// default values
		int imageX = 0;
		int imageY = 0;
		int imageWidth = 0;
		int imageHeight = 0;
		double minCellX = 9999;
		double maxCellX = -9999;
		double minCellY = 9999;
		double maxCellY = -9999;

		final Point2D startPoint = boardForLargePiece.topology().cells().get(cellLocations.getQuick(0)).centroid();
		Point2D currentPoint = null;
		
		for (int i = 0; i < cellLocations.size(); i++)
		{
			if (i > 0) 
			{
				currentPoint = boardForLargePiece.topology().cells().get(cellLocations.get(i)).centroid();
				currentPoint.setLocation((currentPoint.getX() - startPoint.getX()) * boardForLargePieceSize, (currentPoint.getY() - startPoint.getY()) * boardForLargePieceSize);
				
				// Adjust the position of the graphs cells to line up with the screen position.
				boardForLargePiece.topology().cells().get(cellLocations.get(i)).setCentroid(currentPoint.getX(), currentPoint.getY(), 0);
			} 
			else 
			{
				currentPoint = startPoint;
			}

			if (minCellX > currentPoint.getX())
				minCellX = currentPoint.getX();
			if (maxCellX < currentPoint.getX())
				maxCellX = currentPoint.getX();
			if (minCellY > currentPoint.getY())
				minCellY = currentPoint.getY();
			if (maxCellY < currentPoint.getY())
				maxCellY = currentPoint.getY();

			imageX = (int) Math.min(imageX, currentPoint.getX());
			imageY = (int) Math.min(imageY, currentPoint.getY());
			imageWidth = (int) Math.max(imageWidth, currentPoint.getX() + imageSize);
			imageHeight = (int) Math.max(imageHeight, currentPoint.getY() + imageSize);
		}

		// Set the offset for the large piece.
		final Point2D offsetPoint = new Point();
		offsetPoint.setLocation((minCellX+((maxCellX-minCellX)/2.0)), (minCellY+((maxCellY-minCellY)/2.0)));
		
		while(largeOffsets.size() <= localState)
			largeOffsets.add(null);
		
		largeOffsets.set(localState,offsetPoint);

		// set the size of the large piece
		size = new Point(imageWidth + Math.abs(imageX), imageHeight + Math.abs(imageY));
		
		// set the origin point of the large piece
		while(origin.size() <= localState)
			origin.add(null);
		
		final int x = -imageX;
		final int y = size.y + imageY - imageSize;
		origin.set(localState, new Point(x, y));

		// store the image to return, because we need to reset the cells first.
		final SVGGraphics2D g2d = new SVGGraphics2D(size.x, size.y);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		final SVGGraphics2D imageToReturn = drawLargePieceVisuals(g2d, cellLocations, imageSize, imageX, imageY, localState, value, context, secondary, hiddenValue, rotation, boardForLargePiece, containerIndex);
		
		// reset the positions of the cells on the board
		for (int i = 0; i < boardForLargePiece.topology().cells().size(); i++)
			boardForLargePiece.topology().cells().get(i).setCentroid(originalCellLocations.get(i).getX(), originalCellLocations.get(i).getY(), 0);
		
		return imageToReturn;
	}
	
	//----------------------------------------------------------------------------
	
	/**
	 * Creates the image of the large piece based on the cell locations and state that is passed in.
	 */
	protected SVGGraphics2D drawLargePieceVisuals(final SVGGraphics2D g2d, final TIntArrayList cellLocations, final int imageSize, final int imageX, final int imageY, 
			final int state, final int value, final Context context, final boolean secondary, final int hiddenValue, final int rotation, final Board boardForLargePiece, final int containerIndex)
	{
		final String defaultFilePath = "/svg/shapes/square.svg";
		final InputStream in = getClass().getResourceAsStream(defaultFilePath);

		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(in)))
		{
			for (int i = 0; i < cellLocations.size(); i++)
			{
				final Point2D currentPoint = boardForLargePiece.topology().cells().get(cellLocations.get(i)).centroid();
				
				final int y = size.y - ((int) currentPoint.getY() - imageY) - imageSize;
				final int x = (int) currentPoint.getX() - imageX;

				final SVGGraphics2D g2dIndividual = super.getSVGImageFromFilePath(g2d, context, imageSize, defaultFilePath, containerIndex, state, value, hiddenValue, rotation, secondary);

				SVGtoImage.loadFromSource
				(
					g2d, g2dIndividual.getSVGDocument(), new Rectangle(x, y, imageSize+4, imageSize+4), 
					fillColour, fillColour, 0
				);
			}
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}

		return g2d;
	}

	//----------------------------------------------------------------------------
	
	@Override
	public ArrayList<Point2D> getLargeOffsets()
	{
		return largeOffsets;
	}
	
	@Override
	public ArrayList<Point> origin() 
	{
		return origin;
	}

	@Override
	public Point largePieceSize() 
	{
		return size;
	}

}
