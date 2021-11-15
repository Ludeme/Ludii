package view.container.aspects.designs.board.puzzle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import game.types.board.SiteType;
import game.util.directions.CompassDirection;
import metadata.graphics.util.PuzzleDrawHintType;
import metadata.graphics.util.PuzzleHintLocationType;
import other.action.puzzle.ActionSet;
import other.context.Context;
import other.location.FullLocation;
import other.location.Location;
import other.move.Move;
import other.state.State;
import other.state.container.ContainerState;
import other.topology.Edge;
import other.topology.TopologyElement;
import util.ContainerUtil;
import view.container.aspects.designs.BoardDesign;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class PuzzleDesign extends BoardDesign
{
	public PuzzleDesign(final BoardStyle boardStyle, final BoardPlacement boardPlacement) 
	{
		super(boardStyle, boardPlacement);
	}
	
	//-------------------------------------------------------------------------
	// Deduction puzzle variables

	/** Hint values. */
	protected ArrayList<Integer> hintValues = null;
	
	/** Hint Directions (when applicable) */
	protected ArrayList<CompassDirection> hintDirections = new ArrayList<>();

	/** Locations for hints. */
	protected ArrayList<Location> locationValues = new ArrayList<>();
	
	/** Regions for hints. */
	protected ArrayList<ArrayList<Location>> hintRegions = new ArrayList<>();
	
	protected PuzzleDrawHintType drawHintType = PuzzleDrawHintType.Default;
	protected PuzzleHintLocationType hintLocationType = PuzzleHintLocationType.Default;

	//-------------------------------------------------------------------------
	
	/**
	 * fill, draw internal grid lines, draw symbols, draw outer border on top.
	 * @return SVG as string.
	 */
	@Override
	public String createSVGImage(final Bridge bridge, final Context context)
	{
		// Set all values
		final SVGGraphics2D g2d = boardStyle.setSVGRenderingValues();
		final double boardLineThickness = boardStyle.cellRadiusPixels()/15.0;
		
		checkeredBoard = context.game().metadata().graphics().checkeredBoard();
		straightLines  = context.game().metadata().graphics().straightRingLines();
		
		final float swThin = (float) Math.max(1, boardLineThickness);
		final float swThick = swThin;
		
		setStrokesAndColours
		(
			bridge, 
			context,
			new Color(120, 190, 240),
			new Color(120, 190, 240),
			new Color(210, 230, 255),
			new Color(210, 0, 0),
			new Color(0, 230, 0),
			new Color(0, 0, 255),
			null,
			null,
			new Color(0, 0, 0),
			swThin,
			swThick
		);
		
		drawGround(g2d, context, true);
		
		fillCells(bridge, g2d, context);
		drawInnerCellEdges(g2d, context);
		drawOuterCellEdges(bridge, g2d, context);
		
		drawSymbols(g2d, context);

		if (context.game().metadata().graphics().showRegionOwner())
			drawRegions(g2d, context, colorSymbol(), strokeThick, hintRegions);
		
		drawGround(g2d, context, false);
		
		return g2d.getSVGDocument();
	}	
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param regionIndeces
	 * @param context
	 * @return
	 */
	protected Location findHintPosInRegion(final Integer[] regionIndeces, final SiteType siteType, final Context context)
	{
		if (regionIndeces.length == 1)
			return new FullLocation(regionIndeces[0].intValue(),0,siteType);

		double highestRow = -99999999;
		double lowestIndex = 99999999;
		Location bestLocationFound = null;

		for (final Integer cellIndex : regionIndeces)
		{
			final Point2D posn = context.topology().getGraphElements(context.board().defaultSite()).get(cellIndex.intValue()).centroid();
			
			final double cellX = posn.getX();
			final double cellY = posn.getY();
			
			if (hintLocationType == PuzzleHintLocationType.BetweenVertices)
			{
				final Point2D posnA = context.topology().getGraphElements(siteType).get(regionIndeces[0].intValue()).centroid();
				final Point2D posnB = context.topology().getGraphElements(siteType).get(regionIndeces[1].intValue()).centroid();
				final Point2D midPoint = new Point2D.Double((posnA.getX() + posnB.getX()) / 2, (posnA.getY() + posnB.getY()) / 2);
					
				double lowestDistance = 99999999;
				for (final Edge e : context.board().topology().edges())
				{
					final double edgeDistance = Math.hypot(midPoint.getX()-e.centroid().getX(), midPoint.getY()-e.centroid().getY());
					if (edgeDistance < lowestDistance)
					{
						lowestDistance = edgeDistance;
						bestLocationFound = new FullLocation(e.index(),0,SiteType.Edge);
					}
				}
				
				if (Math.abs(posnA.getX() - posnB.getX()) < Math.abs(posnA.getY() - posnB.getY()))
				{
					if (posnA.getY() < posnB.getY())
						hintDirections.add(CompassDirection.N);
					else
						hintDirections.add(CompassDirection.S);
				}
				else
				{
					if (posnA.getX() < posnB.getX())
						hintDirections.add(CompassDirection.W);
					else
						hintDirections.add(CompassDirection.E);
				}
			}
			else if 
			(
				cellX <= lowestIndex && cellY >= highestRow
				||
				cellX < lowestIndex     // cellY > highestRow.intValue() if top is preferred over left
			)
			{
				highestRow = posn.getY();
				lowestIndex = posn.getX();
				bestLocationFound = new FullLocation(cellIndex.intValue(),0,siteType);
				
				if (regionIndeces[0].equals(Integer.valueOf(regionIndeces[1].intValue() - 1)))
					hintDirections.add(CompassDirection.W);
				else
					hintDirections.add(CompassDirection.N);
			}
		}
		
		return bestLocationFound;
	}
	
	//-------------------------------------------------------------------------
	
	protected void detectHints(final Context context)
	{
		if (!context.game().isDeductionPuzzle())
			return;
		
		hintValues = new ArrayList<>();
		
		if (context.game().metadata().graphics().hintLocationType() != null)
			hintLocationType = context.game().metadata().graphics().hintLocationType();
		
		if (context.game().metadata().graphics().drawHintType() != null)
			drawHintType = context.game().metadata().graphics().drawHintType();
		
		// Cells
		if (context.game().rules().phases()[0].play().moves().isConstraintsMoves() && context.game().equipment().cellHints() != null)
		{
			final int numHints = context.game().equipment().cellHints().length;
			for (int i = 0; i < numHints; i++)
			{
				locationValues.add(findHintPosInRegion(context.game().equipment().cellsWithHints()[i], SiteType.Cell, context));
				hintValues.add(context.game().equipment().cellHints()[i]);
				
				final ArrayList<Location> hintRegion = new ArrayList<>();
				for (final Integer index : context.game().equipment().cellsWithHints()[i])
					hintRegion.add(new FullLocation(index.intValue(),0,SiteType.Cell));
				hintRegions.add(hintRegion);
			}
		}
		
		// Vertices
		if (context.game().rules().phases()[0].play().moves().isConstraintsMoves() && context.game().equipment().vertexHints() != null)
		{
			final int numHints = context.game().equipment().vertexHints().length;
			for (int i = 0; i < numHints; i++)
			{
				locationValues.add(findHintPosInRegion(context.game().equipment().verticesWithHints()[i], SiteType.Vertex, context));
				hintValues.add(context.game().equipment().vertexHints()[i]);
			}
		}
		
		// Edges
		if (context.game().rules().phases()[0].play().moves().isConstraintsMoves() && context.game().equipment().edgeHints() != null)
		{
			final int numHints = context.game().equipment().edgeHints().length;
			for (int i = 0; i < numHints; i++)
			{
				locationValues.add(findHintPosInRegion(context.game().equipment().edgesWithHints()[i], SiteType.Edge, context));
				hintValues.add(context.game().equipment().edgeHints()[i]);
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void drawPuzzleCandidates(final Graphics2D g2d, final Context context)
	{
		final Font oldFont = g2d.getFont();

		final int minPuzzleValue = context.board().getRange(context.board().defaultSite()).min(context);
		final int maxPuzzleValue = context.board().getRange(context.board().defaultSite()).max(context);
		final int valueRange = maxPuzzleValue - minPuzzleValue + 1;
		final int base = (int)Math.sqrt(Math.max(9, valueRange));

		final int bigFontSize = (int)(0.75 * boardStyle.placement().getHeight() / Math.max(9, valueRange) + 0.5);
		final int smallFontSize = (int)(0.25 * bigFontSize + 0.5);

		final Font smallFont = new Font(oldFont.getFontName(), Font.PLAIN, smallFontSize);

		// This game has a board
		final State state = context.state();

		final ContainerState cs = state.containerStates()[0];

		final double u = boardStyle.cellRadius()* boardStyle.placement().getHeight();
		final double off = 0.6 * u;
		
		// Draw the candidate values, greyed out as appropriate
		g2d.setFont(smallFont);
		
		// Determine relative candidate positions within each cell
		final Point2D.Double[] offsets = new Point2D.Double[valueRange];
		for (int n = 0; n < valueRange; n++)
		{
			final int row = n / base;
			final int col = n % base;

			final double x = (col - 0.5 * (base - 1)) * off;
			final double y = (row - 0.5 * (base - 1)) * off;

			offsets[n] = new Point2D.Double(x, y);
		}

		for (int site = 0; site < topology().getGraphElements(context.board().defaultSite()).size(); site++)
		{
			final TopologyElement vertex = topology().cells().get(site);
			final Point2D posn = vertex.centroid();

			final int cx = (int) (posn.getX() * boardStyle.placement().width + 0.5);
			final int cy = boardStyle.placement().height - (int) (posn.getY() * boardStyle.placement().height + 0.5);
			
			if (cs.isResolved(site, context.board().defaultSite()))
				continue;
			
			for (int b = 0; b <= valueRange; b++)
			{
				other.action.puzzle.ActionSet a = null;
				a = new ActionSet(context.board().defaultSite(), site, b + minPuzzleValue);
				a.setDecision(true);
				final Move m = new Move(a);
				m.setFromNonDecision(site);
				m.setToNonDecision(site);
				m.setEdgeMove(site);
				m.setDecision(true);
				
				if (!context.moves(context).moves().contains(m) || !cs.bit(site, b+minPuzzleValue, context.board().defaultSite()))
					continue;
				
				final int tx = (int)(cx + offsets[b].getX() + 0.5) + boardStyle.placement().x;
				final int ty = (int)(cy + offsets[b].getY() + 0.5) + boardStyle.placement().y;
				final Point drawPosn = new Point(tx,ty);
				
				boardStyle.drawPuzzleValue(b+minPuzzleValue, site, context, g2d, drawPosn, (int) (cellRadiusPixels() / base*1.5));
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void drawPuzzleHints(final Graphics2D g2d, final Context context)
	{
		if (!context.game().isDeductionPuzzle())
			return;
		
		if (hintValues == null)
			detectHints(context);

		for (final TopologyElement graphElement : topology().getAllGraphElements())
		{
			final SiteType type = graphElement.elementType();
			final int site = graphElement.index();

			final Point2D posn = graphElement.centroid();

			final Point drawnPosn = screenPosn(posn);

			for (int i = 0; i < hintValues.size(); i++)
			{
				if (locationValues.get(i).site() == site && locationValues.get(i).siteType() == type)
				{
					int maxHintvalue = 0;
					for (int j = 0; j < hintValues.size(); j++)
					{
						if (hintValues.get(i) != null)
						{
							if (hintValues.get(i).intValue() > maxHintvalue)
							{
								maxHintvalue = hintValues.get(i).intValue();
							}
						}
					}
					
					if (drawHintType == PuzzleDrawHintType.TopLeft)
					{
						final Font valueFont = new Font("Arial", Font.BOLD, (int) (boardStyle.cellRadiusPixels()/1.5));
						g2d.setColor(Color.BLACK);
						g2d.setFont(valueFont);
						final Rectangle2D rect = g2d.getFont().getStringBounds(Integer.toString(hintValues.get(i).intValue()), g2d.getFontRenderContext());
						g2d.drawString(hintValues.get(i).toString(), (int) (drawnPosn.x - boardStyle.cellRadiusPixels()/1.3), (int) (drawnPosn.y - rect.getHeight()/4));
					}
					else if (drawHintType == PuzzleDrawHintType.NextTo)
					{
						final Font valueFont = new Font("Arial", Font.BOLD, (boardStyle.cellRadiusPixels()));
						g2d.setColor(Color.BLACK);
						g2d.setFont(valueFont);
						final Rectangle2D rect = g2d.getFont().getStringBounds(Integer.toString(hintValues.get(i).intValue()), g2d.getFontRenderContext());
						if (hintDirections.get(i) == CompassDirection.N)
							g2d.drawString(hintValues.get(i).toString(), (int)(drawnPosn.x - rect.getWidth()/2), (int)(drawnPosn.y + rect.getHeight()/4 - cellRadiusPixels()*2));
						else if (hintDirections.get(i) == CompassDirection.W) 
							g2d.drawString(hintValues.get(i).toString(), (int)(drawnPosn.x - rect.getWidth()/2 - cellRadiusPixels()*2), (int)(drawnPosn.y + rect.getHeight()/4));
					}
					else
					{
						final Font valueFont = new Font("Arial", Font.BOLD, (boardStyle.cellRadiusPixels()));
						g2d.setColor(Color.BLACK);
						g2d.setFont(valueFont);
						final Rectangle2D rect = g2d.getFont().getStringBounds(Integer.toString(hintValues.get(i).intValue()), g2d.getFontRenderContext());
						g2d.drawString(hintValues.get(i).toString(), (int)(drawnPosn.x - rect.getWidth()/2), (int)(drawnPosn.y + rect.getHeight()/4));
					}
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Draws the regions of the board.
	 */
	protected void drawRegions(final Graphics2D g2d, final Context context, final Color borderColor, final BasicStroke stroke,  final ArrayList<ArrayList<Location>> regionList)
	{
		for (final ArrayList<Location> region : regionList)
			drawEdges(g2d, context, borderColor, stroke, ContainerUtil.getOuterRegionEdges(region,topology()), 0);
	}
	
	//-------------------------------------------------------------------------

}
