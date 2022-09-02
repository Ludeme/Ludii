package view.container.aspects.designs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import game.Game;
import game.equipment.other.Regions;
import game.types.board.SiteType;
import game.util.graph.Properties;
import graphics.ImageUtil;
import graphics.svg.SVGtoImage;
import main.math.MathRoutines;
import main.math.Vector;
import metadata.graphics.util.BoardGraphicsType;
import metadata.graphics.util.CurveType;
import metadata.graphics.util.MetadataImageInfo;
import other.context.Context;
import other.location.FullLocation;
import other.location.Location;
import other.topology.Cell;
import other.topology.Edge;
import other.topology.Topology;
import other.topology.TopologyElement;
import other.topology.Vertex;
import util.ContainerUtil;
import util.GraphUtil;
import util.ShadedCells;
import util.StringUtil;
import util.StrokeUtil;
import view.container.aspects.placement.BoardPlacement;
import view.container.styles.BoardStyle;

public class BoardDesign extends ContainerDesign
{
	protected final BoardStyle boardStyle;
	protected final BoardPlacement boardPlacement;
	
	//-------------------------------------------------------------------------
	
	/** various GUI values for colours and strokes */
	protected Color colorEdgesInner;
	protected Color colorEdgesOuter;
	protected Color colorVerticesInner;
	protected Color colorVerticesOuter;
	protected Color colorFillPhase0;
	protected Color colorFillPhase1;
	protected Color colorFillPhase2;
	protected Color colorFillPhase3;
	protected Color colorFillPhase4;
	protected Color colorFillPhase5;
	protected BasicStroke strokeThin;
	protected BasicStroke strokeThick;
	protected Color colorSymbol;
	
	/** If the board has a checkered pattern (set by metadata or specific board styles). */
	protected boolean checkeredBoard;
	
	/** If all lines should be drawn straight rather than curved. */
	protected boolean straightLines;
	
	//-------------------------------------------------------------------------
	
	protected List<MetadataImageInfo> symbols = new ArrayList<>();
	protected List<List<MetadataImageInfo>> symbolRegions = new ArrayList<>();
	
	//-------------------------------------------------------------------------
	
	public BoardDesign(final BoardStyle boardStyle, final BoardPlacement boardPlacement)
	{
		this.boardStyle = boardStyle;
		this.boardPlacement = boardPlacement;
	}
	
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
		
		Color colorEdge = new Color(120, 190, 240);

		if (!bridge.settingsVC().flatBoard() && !context.game().metadata().graphics().noSunken())
			colorEdge = null;

		setStrokesAndColours
		(
			bridge,
			context,
			colorEdge,
			colorEdge,
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
		
		// Background
		drawGround(g2d, context, true);
		
		// Cells
		fillCells(bridge, g2d, context);
		
		// Edges
		drawInnerCellEdges(g2d, context);
		drawOuterCellEdges(bridge, g2d, context);
		
		// Symbols
		drawSymbols(g2d, context);
		
		// Foreground
		drawGround(g2d, context, false);

		return g2d.getSVGDocument();
	}	

	//-------------------------------------------------------------------------

	/**
	 * @param context
	 * @param colorLines
	 * @param colorFill1
	 * @param colorFill2
	 * @param colorFill3
	 * @param colorFill4
	 * @param colorDecoration
	 * @param colorBorder
	 */
	protected void setStrokesAndColours
	(
		final Bridge bridge,
		final Context context,
		final Color colorIn,
		final Color colorOut,
		final Color colorFill1,
		final Color colorFill2,
		final Color colorFill3,
		final Color colorFill4,
		final Color colorFill5,
		final Color colorFill6,
		final Color colorDecoration,
		final float swThin,
		final float swThick
	)
	{
		// Define the default board colours and line thicknesses
		bridge.settingsColour().getBoardColours()[BoardGraphicsType.InnerEdges.value()] = colorIn;
		bridge.settingsColour().getBoardColours()[BoardGraphicsType.OuterEdges.value()] = colorOut;
		bridge.settingsColour().getBoardColours()[BoardGraphicsType.InnerVertices.value()] = colorIn;
		bridge.settingsColour().getBoardColours()[BoardGraphicsType.OuterVertices.value()] = colorIn;
	    bridge.settingsColour().getBoardColours()[BoardGraphicsType.Phase0.value()] = colorFill1;
	    bridge.settingsColour().getBoardColours()[BoardGraphicsType.Phase1.value()] = colorFill2;
	    bridge.settingsColour().getBoardColours()[BoardGraphicsType.Phase2.value()] = colorFill3;
	    bridge.settingsColour().getBoardColours()[BoardGraphicsType.Phase3.value()] = colorFill4;
	    bridge.settingsColour().getBoardColours()[BoardGraphicsType.Phase4.value()] = colorFill5;
	    bridge.settingsColour().getBoardColours()[BoardGraphicsType.Phase5.value()] = colorFill6;
	    bridge.settingsColour().getBoardColours()[BoardGraphicsType.Symbols.value()] = colorDecoration; 

	    // Check the .lud metadata for overriding fill colours
	    for (int bid = 0; bid < bridge.settingsColour().getBoardColours().length; bid++)
	    {
	    	final Color colour = context.game().metadata().graphics().boardColour(BoardGraphicsType.getTypeFromValue(bid));  	
	    	if (colour != null)
	    	{
				bridge.settingsColour().getBoardColours()[bid] = colour;
	    	}
	    }

	    final float lineThickness   = swThin * context.game().metadata().graphics().boardThickness(BoardGraphicsType.InnerEdges);
	    final float borderThickness = swThick * context.game().metadata().graphics().boardThickness(BoardGraphicsType.OuterEdges);

	    colorEdgesInner = bridge.settingsColour().getBoardColours()[BoardGraphicsType.InnerEdges.value()];
	    colorEdgesOuter = bridge.settingsColour().getBoardColours()[BoardGraphicsType.OuterEdges.value()];
	    colorVerticesInner = bridge.settingsColour().getBoardColours()[BoardGraphicsType.InnerVertices.value()];
	    colorVerticesOuter = bridge.settingsColour().getBoardColours()[BoardGraphicsType.OuterVertices.value()];
		colorFillPhase0 = bridge.settingsColour().getBoardColours()[BoardGraphicsType.Phase0.value()];
		colorFillPhase1 = bridge.settingsColour().getBoardColours()[BoardGraphicsType.Phase1.value()];
		colorFillPhase2 = bridge.settingsColour().getBoardColours()[BoardGraphicsType.Phase2.value()];
		colorFillPhase3 = bridge.settingsColour().getBoardColours()[BoardGraphicsType.Phase3.value()];
		colorFillPhase4 = bridge.settingsColour().getBoardColours()[BoardGraphicsType.Phase4.value()];
		colorFillPhase5 = bridge.settingsColour().getBoardColours()[BoardGraphicsType.Phase5.value()];
		setColorSymbol(bridge.settingsColour().getBoardColours()[BoardGraphicsType.Symbols.value()]);

		if (bridge.settingsColour().getBoardColours()[BoardGraphicsType.InnerEdges.value()] != null)
			colorEdgesInner = bridge.settingsColour().getBoardColours()[BoardGraphicsType.InnerEdges.value()];
		
		if (bridge.settingsColour().getBoardColours()[BoardGraphicsType.OuterEdges.value()] != null)
			colorEdgesOuter = bridge.settingsColour().getBoardColours()[BoardGraphicsType.OuterEdges.value()];
		
		if (bridge.settingsColour().getBoardColours()[BoardGraphicsType.InnerVertices.value()] != null)
			colorVerticesInner = bridge.settingsColour().getBoardColours()[BoardGraphicsType.InnerVertices.value()];
		
		if (bridge.settingsColour().getBoardColours()[BoardGraphicsType.OuterVertices.value()] != null)
			colorVerticesOuter = bridge.settingsColour().getBoardColours()[BoardGraphicsType.OuterVertices.value()];
	
		if (bridge.settingsColour().getBoardColours()[BoardGraphicsType.Phase0.value()] != null)
			colorFillPhase0 = bridge.settingsColour().getBoardColours()[BoardGraphicsType.Phase0.value()];
		
		if (bridge.settingsColour().getBoardColours()[BoardGraphicsType.Phase1.value()] != null)
			colorFillPhase1 = bridge.settingsColour().getBoardColours()[BoardGraphicsType.Phase1.value()];

		if (bridge.settingsColour().getBoardColours()[BoardGraphicsType.Phase2.value()] != null)
			colorFillPhase2 = bridge.settingsColour().getBoardColours()[BoardGraphicsType.Phase2.value()];

		if (bridge.settingsColour().getBoardColours()[BoardGraphicsType.Phase3.value()] != null)
			colorFillPhase3 = bridge.settingsColour().getBoardColours()[BoardGraphicsType.Phase3.value()];
		
		if (bridge.settingsColour().getBoardColours()[BoardGraphicsType.Phase4.value()] != null)
			colorFillPhase4 = bridge.settingsColour().getBoardColours()[BoardGraphicsType.Phase4.value()];
		
		if (bridge.settingsColour().getBoardColours()[BoardGraphicsType.Phase5.value()] != null)
			colorFillPhase5 = bridge.settingsColour().getBoardColours()[BoardGraphicsType.Phase5.value()];

		if (bridge.settingsColour().getBoardColours()[BoardGraphicsType.Symbols.value()] != null)
			setColorSymbol(bridge.settingsColour().getBoardColours()[BoardGraphicsType.Symbols.value()]);

		strokeThin  = new BasicStroke(lineThickness,   BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
		strokeThick = new BasicStroke(borderThickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
		
		setSymbols(bridge, context);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Draws either the background or foreground images for the board if specified in metadata.
	 * @param g2d
	 * @param context
	 * @param background
	 */
	protected void drawGround(final SVGGraphics2D g2d, final Context context, final boolean background) 
	{
		List<MetadataImageInfo> allGroundImages = new ArrayList<>();
		if (background)
			allGroundImages = context.metadata().graphics().boardBackground(context);
		else
			allGroundImages = context.metadata().graphics().boardForeground(context);
		
		for (final MetadataImageInfo groundImageInfo : allGroundImages)
		{
			final Point drawPosn = boardStyle.screenPosn(new Point2D.Double(0.5, 0.5));
			
			if (groundImageInfo.path() == null && groundImageInfo.text() == null)
			{
				drawBoardOutline(g2d, groundImageInfo.scale(), groundImageInfo.offestX(), groundImageInfo.offestY(), 
						groundImageInfo.mainColour(), groundImageInfo.secondaryColour(), groundImageInfo.rotation());
			}
			else if (groundImageInfo.path() != null)
			{
				final String fullPath = ImageUtil.getImageFullPath(groundImageInfo.path());
				
				Color edgeColour = colorSymbol();
				Color fillColour = null;
				
				if (groundImageInfo.mainColour() != null)
					fillColour = groundImageInfo.mainColour();
				if (groundImageInfo.secondaryColour() != null)
					edgeColour = groundImageInfo.secondaryColour();
				
				final int rotation = groundImageInfo.rotation();
				
				final int offsetX = (int) (groundImageInfo.offestX() * boardStyle.maxDim());
				final int offsetY = (int) (groundImageInfo.offestY() * boardStyle.maxDim());

				final Rectangle2D rect = 
						new Rectangle2D.Double
						(
							drawPosn.x + offsetX - (groundImageInfo.scaleX() * boardStyle.maxDim())/2, 
							drawPosn.y + offsetY - (groundImageInfo.scaleY() * boardStyle.maxDim())/2, 
							(int) (groundImageInfo.scaleX() * boardStyle.maxDim()), 
							(int) (groundImageInfo.scaleY() * boardStyle.maxDim())
						);
				
				SVGtoImage.loadFromFilePath(g2d, fullPath, rect, edgeColour, fillColour, rotation);
			}
			else if (groundImageInfo.text() != null)
			{
				g2d.setColor(groundImageInfo.mainColour());
				final int fontSize = (int) ((0.85 * boardStyle.cellRadius() * boardStyle.placement().width + 0.5) * groundImageInfo.scale());
				final Font font = new Font("Arial", Font.PLAIN, fontSize);
				g2d.setFont(font);
				g2d.drawString(groundImageInfo.text(), drawPosn.x, drawPosn.y);
			}
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param g2d
	 * @param fillColor
	 * @param stroke
	 */
	protected void fillCells(final Bridge bridge, final Graphics2D g2d, final Context context)
	{
		g2d.setStroke(strokeThin);
		
		final List<Cell> cells = topology().cells();
		for (final Cell cell : cells)
		{
			final Color[][] colours = 
					ShadedCells.shadedPhaseColours
					(
						colorFillPhase0, colorFillPhase1, colorFillPhase2, 
						colorFillPhase3, colorFillPhase4, colorFillPhase5
					); 
			
			final GeneralPath path = new GeneralPath();
			for (int v = 0; v < cell.vertices().size(); v++)
			{
				final Vertex vertexA = cell.vertices().get(v);
				final Vertex vertexB = cell.vertices().get((v + 1) % cell.vertices().size());
				
				if (v == 0)
				{
					final Point ptA = boardStyle.screenPosn(vertexA.centroid());
					path.moveTo(ptA.x, ptA.y);
				}
				
				// Find which edge this is
				Edge edge = null;
				for (final Edge edgeA : vertexA.edges())
					if (edgeA.vA().index() == vertexB.index() || edgeA.vB().index() == vertexB.index())
					{
						edge = edgeA;
						break;
					}
				
				for (final MetadataImageInfo s : symbols)
				{
					if (s.line() != null && s.line().length >=2)
					{
						if 
						(
							edge.vA().index() == s.line()[0].intValue() && edge.vB().index() == s.line()[1].intValue()
							||
							edge.vB().index() == s.line()[0].intValue() && edge.vA().index() == s.line()[1].intValue()
						)
						{
							if (s.curve() != null)
							{
								edge.setTangentA(new Vector(s.curve()[0].doubleValue(),s.curve()[1].doubleValue()));
								edge.setTangentB(new Vector(s.curve()[2].doubleValue(),s.curve()[3].doubleValue()));
							}
						}
					}
				}
				addEdgeToPath(context.game(), path, edge, edge.vA().index() == vertexA.index(), 0);
			}

			g2d.setColor(colorFillPhase0);

			if (checkeredBoard)
				ShadedCells.setCellColourByPhase
				(
					g2d, cell.index(), topology(), 
					colorFillPhase0, colorFillPhase1, colorFillPhase2, 
					colorFillPhase3, colorFillPhase4, colorFillPhase5
				);
			
			// if cell is marked as a symbol, but no symbol is specified, then just fill the cell with the decoration colour
			for (final List<MetadataImageInfo> regionInfo : symbolRegions)
			{
				for (final MetadataImageInfo d : regionInfo)
				{
					if (d.siteType() == SiteType.Cell && d.site() == cell.index() && d.path() == null && d.text() == null)
					{
						g2d.setColor(d.mainColour());
						final int phase = !checkeredBoard ? 0 : topology().phaseByElementIndex(SiteType.Cell, cell.index());
						colours[phase][1] = d.mainColour();
						break;
					}
				}
			}
			
			if (bridge.settingsVC().flatBoard() || context.game().metadata().graphics().noSunken())
				g2d.fill(path);
			else
				ShadedCells.drawShadedCell(g2d, cell, path, colours, checkeredBoard, topology());	
		}
	}

	//-------------------------------------------------------------------------

	protected void drawEdges
	(
		final Graphics2D g2d,
		final Context context,
		final Color lineColour, 
		final Stroke lineStroke,
		final List<Edge> edges,
		final double offsetY
	)
	{
		drawEdgesWithMetadata(g2d, context, lineColour, lineStroke, edges, offsetY);
		
		for (final List<MetadataImageInfo> regionInfo : symbolRegions)
		{			
			final List<Edge> regionEdges = new ArrayList<>();
			Color regionColour = null;
			
			for (final MetadataImageInfo d : regionInfo)
			{
				if (d.siteType() == SiteType.Edge && d.path() == null)
				{
					regionEdges.add(topology().edges().get(d.site()));
					regionColour = d.mainColour();
				}
			}
			
			final Color originalColour = g2d.getColor();
			drawEdgesWithMetadata(g2d, context, regionColour, lineStroke, regionEdges, offsetY);
			g2d.setColor(originalColour);
		}
	}

	private void drawEdgesWithMetadata
	(
		final Graphics2D g2d,
		final Context context,
		final Color lineColour, 
		final Stroke lineStroke,
		final List<Edge> edges,
		final double offsetY
	)
	{
		final double errorDistanceBuffer = 0.0001;
		
		g2d.setStroke(lineStroke);
		g2d.setColor(lineColour);

		final GeneralPath path = new GeneralPath();
		
		final List<Edge> edgesToDraw = new ArrayList<>();

		for (final Edge edge : edges)
			if 
			(
				getMetadataImageInfoForEdge(edge) == null 		// Ignore edges which have already been drawn as part of the metadata (symbols).
				&&	
				(
					(context.game().metadata().graphics().showStraightEdges() && !edge.isCurved())
					||
					(context.game().metadata().graphics().showCurvedEdges() && edge.isCurved())
				)
			)
				edgesToDraw.add(edge);
		
		Vertex vertexA;
		Vertex vertexB;
		
		while (edgesToDraw.size() > 0)
		{
			Edge edge = edgesToDraw.get(0);
			boolean nextEdgeFound = true;

			vertexA = edge.vA();
			final Point2D centroidA = edge.vA().centroid();
			final Point drawPosnA = boardStyle.screenPosn(centroidA);
			path.moveTo(drawPosnA.x, drawPosnA.y + offsetY);
			
			vertexB = edge.vB();
			Point2D centroidB = edge.vB().centroid();

			while (nextEdgeFound == true)
			{
				nextEdgeFound = false;
				
				addEdgeToPath(context.game(), path, edge, edge.vA().index() == vertexA.index(), offsetY);
				edgesToDraw.remove(edge);

				for (final Edge nextEdge : edgesToDraw)
				{
					// Forwards direction (vB of nextEdge is the next vertex).
					if 
					(	
						Math.abs(centroidB.getX() - nextEdge.vA().centroid().getX()) < errorDistanceBuffer 
						&& 
						Math.abs(centroidB.getY() - nextEdge.vA().centroid().getY()) < errorDistanceBuffer
					)
					{
						nextEdgeFound = true;
						edge = nextEdge;
						vertexA = edge.vA();
						vertexB = edge.vB();
						centroidB = vertexB.centroid();
						break;
					}
					// Backwards direction (vA of nextEdge is the next vertex).
					else if 
					(
						Math.abs(centroidB.getX() - nextEdge.vB().centroid().getX()) < errorDistanceBuffer
						&& 
						Math.abs(centroidB.getY() - nextEdge.vB().centroid().getY()) < errorDistanceBuffer
					)
					{
						nextEdgeFound = true;
						edge = nextEdge;
						vertexA = edge.vB();
						vertexB = edge.vA();
						centroidB = vertexB.centroid();
						break;
					}
				}
			}
			
			// If we have come full loop back to where we started, then close the path.
			if (Math.abs(centroidA.getX() - centroidB.getX()) < errorDistanceBuffer
					&& Math.abs(centroidA.getY() - centroidB.getY()) < errorDistanceBuffer)
				path.closePath();
		}
		g2d.draw(path);
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Draw Vertices based on the board's graph.
	 */
	protected void drawVertices(final Bridge bridge, final Graphics2D g2d, final Context context, final double radius)
	{
		drawVertices(bridge, g2d, context, null, radius);
	}
	
	protected void drawVertices(final Bridge bridge, final Graphics2D g2d, final Context context, final Color vertexColour, final double radius)
	{
		drawVertices(bridge, g2d, context, vertexColour, radius, 0);
	}
	
	protected void drawVertices(final Bridge bridge, final Graphics2D g2d, final Context context, final Color vertexColour, final double radius, final double offsetY)
	{
		if (context.game().metadata().graphics().showRegionOwner() && !context.game().isDeductionPuzzle())
		{
			// Show region vertices in owner's colour
			final Regions[] regionsList = context.game().equipment().regions(); 

			final Color borderColor = new Color(127, 127, 127);
			
			final double rI = radius * 2;
			final double rO = rI + 2;

			for (final Regions currentRegions : regionsList)
			{
				final int owner = currentRegions.owner();
				final int[] sites = currentRegions.eval(context);
	
				for (final int sid : sites)
				{
					// Show this site in the owner's colour	
					final Vertex vertex = topology().vertices().get(sid);
					final Point pt = boardStyle.screenPosn(vertex.centroid());
					pt.setLocation(pt.x, pt.y + offsetY);
					
					g2d.setColor(borderColor);
					final java.awt.Shape ellipseO = new Ellipse2D.Double(pt.x-rO, pt.y-rO, 2*rO, 2*rO);
					g2d.fill(ellipseO);
					
					final Color playerColour = bridge.settingsColour().playerColour(context, owner);
					g2d.setColor(playerColour);
					final java.awt.Shape ellipseI = new Ellipse2D.Double(pt.x-rI, pt.y-rI, 2*rI, 2*rI);
					g2d.fill(ellipseI);
				}
			}
		}

		for (final Vertex vertex : topology().vertices())
		{
			g2d.setStroke(strokeThin);

			if (vertex.properties().get(Properties.OUTER))
				g2d.setColor(colorVerticesOuter);
			else
				g2d.setColor(colorVerticesInner);
			
			if (vertexColour != null && g2d.getColor().getAlpha() != 0)
				g2d.setColor(vertexColour); 

			// if vertex is marked as a symbol, but no symbol is specified, then just colour the vertex with the decoration colour
			for (final List<MetadataImageInfo> regionInfo : symbolRegions)
			{
				for (final MetadataImageInfo d : regionInfo)
				{
					if (d.siteType() == SiteType.Vertex && d.site() == vertex.index() && d.path() == null && d.text() == null)
					{
						g2d.setColor(d.mainColour());
						break;
					}
				}
			}
			
			final Point pt = boardStyle.screenPosn(vertex.centroid());
			pt.setLocation(pt.x, pt.y + offsetY);			
			
			// Draw the vertex
			final java.awt.Shape ellipseO = new Ellipse2D.Double(pt.x-radius, pt.y-radius, 2*radius, 2*radius);
			g2d.fill(ellipseO);
		}
	}

	//-------------------------------------------------------------------------
	
	protected void drawBoardOutline(final SVGGraphics2D g2d)
	{
		drawBoardOutline(g2d, 1, 0, 0, null, null, 0);
	}
	
	/**
	 * Draws the rectangular outline of the board
	 * @param rotation 
	 * @param secondaryColour 
	 * @param mainColour 
	 * @param offestY 
	 * @param offestX 
	 */
	protected void drawBoardOutline
	(
		final SVGGraphics2D g2d, final double scale, final float offestX, 
		final float offestY, final Color mainColour, final Color secondaryColour, 
		final int rotation
	)
	{
		final List<Vertex> vertices = topology().vertices();
		
		g2d.setStroke(strokeThin);

		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		
		final GeneralPath path = new GeneralPath();

		for (final Vertex vertex : vertices)
		{
			final Point posn = boardStyle.screenPosn(vertex.centroid());
			
			final int x = posn.x;
			final int y = posn.y;

			if (minX > x)
				minX = x;
			if (minY > y)
				minY = y;
			
			if (maxX < x)
				maxX = x;
			if (maxY < y)
				maxY = y;
			
			g2d.setColor(mainColour == null ? colorFillPhase0 : mainColour);
		}
		
		minX += offestX;
		maxX += offestX;
		minY += offestY;
		maxY += offestY;

		// Margin of empty area around board
		final int margin = (int)(boardStyle.cellRadiusPixels() * scale + 0.5);
		path.moveTo(minX - margin, minY - margin);
		path.lineTo(minX - margin, maxY + margin);
		path.lineTo(maxX + margin, maxY + margin);
		path.lineTo(maxX + margin, minY - margin);
		path.lineTo(minX - margin, minY - margin);
		g2d.rotate(rotation);
		g2d.fill(path);
		
		if (secondaryColour != null)
		{
			g2d.setColor(secondaryColour);
			g2d.draw(path);
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Draw symbols on the board.
	 */
	protected void drawSymbols(final Graphics2D g2d, final Context context)
	{		
		for (final MetadataImageInfo s : symbols)
		{
			// Draw lines
			if (s.line() != null && s.line().length >= 2)
			{
				Color colour = s.mainColour();
				final float scale = s.scale();
				if (colour == null)
					colour = colorEdgesOuter;
				g2d.setColor(colour);
				
				final BasicStroke strokeLineThin = new BasicStroke(strokeThin.getLineWidth() * scale, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
				final BasicStroke strokeLineThick = new BasicStroke(strokeThick.getLineWidth() * scale, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
				final Stroke strokeLine = StrokeUtil.getStrokeFromStyle(s.lineStyle(), strokeLineThin, strokeLineThick);
				g2d.setStroke(strokeLine);
				
				final TopologyElement v1 = boardStyle.container().topology().getGraphElements(s.siteType()).get(s.line()[0].intValue());
				final TopologyElement v2 = boardStyle.container().topology().getGraphElements(s.siteType()).get(s.line()[1].intValue());	
				
				if (s.curve() == null)
				{
					g2d.drawLine
					(
						boardStyle.screenPosn(v1.centroid()).x,
						boardStyle.screenPosn(v1.centroid()).y,
						boardStyle.screenPosn(v2.centroid()).x,
						boardStyle.screenPosn(v2.centroid()).y
					);
				}
				else
				{
					final Vector tangentA = new Vector(s.curve()[0].floatValue(), s.curve()[1].floatValue());
					final Vector tangentB = new Vector(s.curve()[2].floatValue(), s.curve()[3].floatValue());
					final GeneralPath path = new GeneralPath();
					path.moveTo(boardStyle.screenPosn(v1.centroid()).x, boardStyle.screenPosn(v1.centroid()).y);
					curvePath(context.game(), path, v1.centroid(), v2.centroid(), tangentA, tangentB, 0, s.curveType());
					g2d.draw(path);
				}
			}
			
			// Draw regular symbols (images)
	
			if ((s.path() == null && s.text() == null) || s.site() == -1)
				continue;
			
			TopologyElement e = null;
			
			if (s.siteType() == SiteType.Cell)
			{
				e = boardStyle.topology().cells().get(s.site());
			}
			else if (s.siteType() == SiteType.Edge)
			{
				e = boardStyle.topology().edges().get(s.site());
			}
			else if (s.siteType() == SiteType.Vertex)
			{
				e = boardStyle.topology().vertices().get(s.site());
			}
			final Point drawPosn = boardStyle.screenPosn(e.centroid());
			
			if (s.path() != null)
			{
				final String fullPath = ImageUtil.getImageFullPath(s.path());

				Color edgeColour = colorSymbol();
				Color fillColour = null;
				
				if (s.mainColour() != null)
					fillColour = s.mainColour();
				if (s.secondaryColour() != null)
					edgeColour = s.secondaryColour();
				
				final int rotation = s.rotation();
				
				final int offsetX = (int) (s.offestX() * boardStyle.cellRadiusPixels() * 2);
				final int offsetY = (int) (s.offestY() * boardStyle.cellRadiusPixels() * 2);
				
				final Rectangle2D rect = new Rectangle2D.Double
						(
							drawPosn.x + offsetX - s.scaleX() * boardStyle.cellRadiusPixels(), 
							drawPosn.y + offsetY - s.scaleY() * boardStyle.cellRadiusPixels(), 
							(int) (s.scaleX() * boardStyle.cellRadiusPixels() * 2), 
							(int) (s.scaleY() * boardStyle.cellRadiusPixels() * 2)
						);
				
				SVGtoImage.loadFromFilePath(g2d, fullPath, rect, edgeColour, fillColour, rotation);
			}
			
			if (s.text() != null)
			{
				g2d.setColor(s.mainColour());
				final int fontSize = (int) ((0.85 * boardStyle.cellRadius() * boardStyle.placement().width + 0.5) * s.scale());
				final Font font = new Font("Arial", Font.PLAIN, fontSize);
				g2d.setFont(font);
				StringUtil.drawStringAtPoint(g2d, s.text(), e, drawPosn, true);
			}
		}
		
		// Draw indices on sites if specified.
		for (final TopologyElement e : boardStyle.topology().getAllGraphElements())
		{
			final Integer additionalValue = context.game().metadata().graphics().showSiteIndex(context.game(), e);
			if (additionalValue != null)
			{
				final Point drawPosn = boardStyle.screenPosn(e.centroid());
				g2d.setColor(Color.WHITE);
				final int fontSize = (int) (0.85 * boardStyle.cellRadius() * boardStyle.placement().width + 0.5);
				final Font font = new Font("Arial", Font.PLAIN, fontSize);
				g2d.setFont(font);
				StringUtil.drawStringAtPoint(g2d, String.valueOf(e.index() + additionalValue.intValue()), e, drawPosn, true);
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	private MetadataImageInfo getMetadataImageInfoForEdge(final Edge edge)
	{
		for (final MetadataImageInfo s : symbols)
			if (s.line() != null && s.line().length >=2 && s.siteType() == SiteType.Vertex)
				if 
				(
					edge.vA().index() == s.line()[0].intValue() && edge.vB().index() == s.line()[1].intValue()
					||
					edge.vB().index() == s.line()[0].intValue() && edge.vA().index() == s.line()[1].intValue()
				)
					return s;
		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/** 
	 * Sets the locations for drawing symbols and colouring regions.
	 */
	private void setSymbols(final Bridge bridge, final Context context) 
	{
		symbols.clear();
		symbolRegions.clear();
	    
	    // all cells that the player wants to colour
	    for (final List<MetadataImageInfo> regionInfo : context.game().metadata().graphics().regionsToFill(context))
	    {
	    	if (regionInfo.size() == 0)
	    		continue;
	    	
	    	final MetadataImageInfo regionGraphics = regionInfo.get(0);
	    	
	    	// Site type of the region is the same as what should be drawn on.
	    	if (regionGraphics.regionSiteType() == regionGraphics.siteType())
	    	{
	    		symbolRegions.add(regionInfo);
	    	}
	    	// Region is defined using cells, but we want to draw the (perimeter) edges.
	    	else if(regionGraphics.regionSiteType() == SiteType.Cell && regionGraphics.siteType() == SiteType.Edge)
	    	{
	    		final ArrayList<Location> region = new ArrayList<>();
		    	for (final MetadataImageInfo m : regionInfo)
		    	{
		    		region.add(new FullLocation(m.site(), 0, m.siteType()));
		    	}
		    	
		    	final List<Edge> edgeLocations = ContainerUtil.getOuterRegionEdges(region, topology());
		    	
		    	for (int e = 0; e < edgeLocations.size(); e++)
		    	{
		    		Color colour = regionGraphics.mainColour();
		    		if (colour == null)
		    		{
		    			final Regions r = ContainerUtil.getRegionOfEdge(context, edgeLocations.get(e));
		    			if (r != null)
		    				// If the color wasn't specified, check if any player owns this region.
		    				colour = bridge.settingsColour().playerColour(context, r.role().owner());
		    			else
		    				// If no player owns this region, set colour to Symbol colour.
		    				colour = colorSymbol();
		    		}
		    		
		    		final Integer[] line = {Integer.valueOf(edgeLocations.get(e).vA().index()), Integer.valueOf(edgeLocations.get(e).vB().index())};
					symbols.add(new MetadataImageInfo(line, SiteType.Vertex, colour, regionGraphics.scale()));
		    	}
	    	}
	    }

	    symbols.addAll(context.game().metadata().graphics().drawLines(context));
	    symbols.addAll(context.game().metadata().graphics().drawSymbol(context));
	}
	
	//-------------------------------------------------------------------------

	private void addEdgeToPath(final Game game, final GeneralPath path, final Edge edge, final boolean forwards, final double offsetY)
	{
		final Vertex vertexA = forwards ? edge.vA() : edge.vB();
		final Vertex vertexB = forwards ? edge.vB() : edge.vA();
		
		final Vector tangentA = forwards ? edge.tangentA() : edge.tangentB();
		final Vector tangentB = forwards ? edge.tangentB() : edge.tangentA();

		if (tangentA != null && tangentB != null && !straightLines)
		{
			CurveType curveType = CurveType.Spline;
			if (getMetadataImageInfoForEdge(edge) != null)
				curveType = getMetadataImageInfoForEdge(edge).curveType();
			
			// Draw curve for this edge
			curvePath(game, path, vertexA.centroid(), vertexB.centroid(), tangentA, tangentB, offsetY, curveType);
		}
		else
		{
			// Draw straight line
			final Point ptB = boardStyle.screenPosn(vertexB.centroid());
			ptB.setLocation(ptB.x, ptB.y + offsetY);
			path.lineTo(ptB.x, ptB.y);
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Draws a curved path based on a spline curve
	 * @param path
	 * @param vACentroid
	 * @param vBCentroid
	 * @param tangentA
	 * @param tangentB
	 * @param offsetY
	 */
	private void curvePath(final Game game, final GeneralPath path, final Point2D vACentroid, final Point2D vBCentroid, final Vector tangentA, final Vector tangentB, final double offsetY, final CurveType curveType)
	{
		final double dist = MathRoutines.distance(vACentroid, vBCentroid);
		
		final double off = game.metadata().graphics().boardCurvature();
		
		double aax = vACentroid.getX() + off * dist * tangentA.x();
		double aay = vACentroid.getY() + off * dist * tangentA.y();
		double bbx = vBCentroid.getX() + off * dist * tangentB.x();
		double bby = vBCentroid.getY() + off * dist * tangentB.y();
		
		if (curveType == CurveType.Bezier)
		{
			aax = tangentA.x();
			aay = tangentA.y();
			bbx = tangentB.x();
			bby = tangentB.y();
		}
		
		final Point ptAA = boardStyle.screenPosn(new Point2D.Double(aax, aay));
		final Point ptBB = boardStyle.screenPosn(new Point2D.Double(bbx, bby));
		
		final Point ptB = boardStyle.screenPosn(vBCentroid);
		ptB.setLocation(ptB.x, ptB.y + offsetY);
		
		path.curveTo(ptAA.x, ptAA.y, ptBB.x, ptBB.y, ptB.x, ptB.y);
	}
	
	//-------------------------------------------------------------------------
	
	protected void drawInnerCellEdges(final Graphics2D g2d, final Context context)
	{
		drawInnerCellEdges(g2d, context, colorEdgesInner, strokeThin);
	}
	
	protected void drawInnerCellEdges(final Graphics2D g2d, final Context context, final Color lineColour, final Stroke lineStroke)
	{
		drawInnerCellEdges(g2d, context, lineColour, lineStroke, 0);
	}
	
	protected void drawInnerCellEdges(final Graphics2D g2d, final Context context, final Color lineColour, final Stroke lineStroke, final double offsetY)
	{
		if (lineColour != null && lineColour.getAlpha() > 0)
			drawEdges(g2d, context, lineColour, lineStroke, GraphUtil.innerEdgeRelations(topology()), offsetY);
	}
	
	//-------------------------------------------------------------------------
	
	protected void drawOuterCellEdges(final Bridge bridge, final Graphics2D g2d, final Context context)
	{
		drawOuterCellEdges(g2d, context, colorEdgesOuter, strokeThick());
	}
	
	protected void drawOuterCellEdges(final Graphics2D g2d, final Context context, final Color lineColour, final Stroke lineStroke)
	{
		drawOuterCellEdges(g2d, context, lineColour, lineStroke, 0);
	}
	
	protected void drawOuterCellEdges(final Graphics2D g2d, final Context context, final Color lineColour, final Stroke lineStroke, final double offsetY)
	{
		if (lineColour != null && lineColour.getAlpha() > 0)
			drawEdges(g2d, context, lineColour, lineStroke, GraphUtil.outerEdgeRelations(topology()), offsetY);
	}
	
	//-------------------------------------------------------------------------
	

	protected void drawDiagonalEdges(final Graphics2D g2d, final Context context)
	{
		drawDiagonalEdges(g2d, context, colorEdgesInner, strokeThin);
	}

	protected void drawDiagonalEdges(final Graphics2D g2d, final Context context, final Color lineColour, final Stroke lineStroke)
	{
		drawDiagonalEdges(g2d, context, lineColour, lineStroke, 0);
	}
	
	protected void drawDiagonalEdges(final Graphics2D g2d, final Context context, final Color lineColour, final Stroke lineStroke, final double offsetY)
	{
		drawEdges(g2d, context, lineColour, lineStroke, GraphUtil.diagonalEdgeRelations(topology()), offsetY);
	}
	
	//-------------------------------------------------------------------------
	
	protected void drawOrthogonalConnections(final Graphics2D g2d, final Context context)
	{
		drawOrthogonalConnections(g2d, context, colorEdgesInner, strokeThin);
	}
	
	protected void drawOrthogonalConnections(final Graphics2D g2d, final Context context, final Color lineColour, final Stroke lineStroke)
	{
		drawOrthogonalConnections(g2d, context, lineColour, lineStroke, 0);
	}
	
	protected void drawOrthogonalConnections(final Graphics2D g2d, final Context context, final Color lineColour, final Stroke lineStroke, final double offsetY)
	{
		drawEdges(g2d, context, lineColour, lineStroke, GraphUtil.orthogonalCellConnections(topology()), offsetY);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Draw Diagonal connections based on the board's graph, using default line colour and stroke.
	 */
	protected void drawDiagonalConnections(final Graphics2D g2d, final Context context)
	{
		drawDiagonalConnections(g2d, context, colorEdgesInner, strokeThin);
	}
	
	protected void drawDiagonalConnections(final Graphics2D g2d, final Context context, final Color lineColour, final Stroke lineStroke)
	{
		drawDiagonalConnections(g2d, context, lineColour, lineStroke, 0);
	}
	
	/**
	 * Draw Diagonal connections based on the board's graph, using specified line colour and stroke.
	 */
	protected void drawDiagonalConnections(final Graphics2D g2d, final Context context, final Color lineColour, final Stroke lineStroke, final double offsetY)
	{
		drawEdges(g2d, context, lineColour, lineStroke, GraphUtil.diagonalCellConnections(topology()), offsetY);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Draw Off Diagonal connections based on the board's graph, using default line colour and stroke.
	 */
	protected void drawOffDiagonalConnections(final Graphics2D g2d, final Context context)
	{
		drawOffDiagonalConnections(g2d, context, colorEdgesInner, strokeThin);
	}
	
	protected void drawOffDiagonalConnections(final Graphics2D g2d, final Context context, final Color lineColour, final Stroke lineStroke)
	{
		drawOffDiagonalConnections(g2d, context, lineColour, lineStroke, 0);
	}
	
	/**
	 * Draw Off Diagonal connections based on the board's graph, using specified line colour and stroke.
	 */
	protected void drawOffDiagonalConnections(final Graphics2D g2d, final Context context, final Color lineColour, final Stroke lineStroke, final double offsetY)
	{
		drawEdges(g2d, context, lineColour, lineStroke, GraphUtil.offCellConnections(topology()), offsetY);
	}

	//-------------------------------------------------------------------------
	
	public BasicStroke strokeThick() 
	{
		return strokeThick;
	}

	public Color colorSymbol() 
	{
		return colorSymbol;
	}

	public void setColorSymbol(final Color colorSymbol) 
	{
		this.colorSymbol = colorSymbol;
	}
	
	public Topology topology()
	{
		return boardStyle.topology();
	}
	
	public int cellRadiusPixels()
	{
		return boardStyle.cellRadiusPixels();
	}
	
	public Point screenPosn(final Point2D posn)
	{
		return boardStyle.screenPosn(posn);
	}

	//-------------------------------------------------------------------------

}
