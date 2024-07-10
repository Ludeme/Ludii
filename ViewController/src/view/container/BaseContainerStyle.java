package view.container;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import bridge.Bridge;
import game.equipment.container.Container;
import game.types.board.SiteType;
import main.Constants;
import metadata.graphics.util.PieceStackType;
import metadata.graphics.util.PuzzleDrawHintType;
import metadata.graphics.util.StackPropertyType;
import other.context.Context;
import other.location.FullLocation;
import other.location.Location;
import other.state.container.ContainerState;
import other.topology.Cell;
import other.topology.Edge;
import other.topology.Topology;
import other.topology.TopologyElement;
import other.topology.Vertex;
import util.ContainerUtil;
import util.DeveloperGUI;
import util.GraphUtil;
import util.LocationUtil;
import util.PlaneType;
import util.StackVisuals;
import util.StringUtil;
import view.container.aspects.axes.ContainerAxis;
import view.container.aspects.components.ContainerComponents;
import view.container.aspects.designs.ContainerDesign;
import view.container.aspects.placement.ContainerPlacement;
import view.container.aspects.tracks.ContainerTrack;

/**
 * Implementation of container style. 
 * @author matthew.stephenson and mrraow and cambolbro
 */
public abstract class BaseContainerStyle implements ContainerStyle
{
	// Container components from the ECS framework
	protected ContainerComponents containerComponents;
	protected ContainerTrack containerTrack;
	protected ContainerAxis containerAxis;
	protected ContainerDesign containerDesign;
	protected ContainerPlacement containerPlacement;
	
	//-------------------------------------------------------------------------
	
	/** Container to which this style applies. */
	private final Container container;
	
	/** Image for rendering. */
	protected String imageSVGString = null;

	/** Image to display the graph. */
	protected String graphSVGString = null;
	
	/** Image to display the dual. */
	protected String connectionsSVGString = null;
	
	protected Bridge bridge;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor.
	 */
	public BaseContainerStyle(final Bridge bridge, final Container container)
	{
		this.container = container;
		this.bridge = bridge;
		
		ContainerUtil.normaliseGraphElements(topology());
		ContainerUtil.centerGraphElements(topology());
		
		containerPlacement = new ContainerPlacement(bridge, this);
		containerComponents = new ContainerComponents(bridge, this);
		containerTrack = new ContainerTrack();
		containerAxis = new ContainerAxis();
		containerDesign = new ContainerDesign();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Set all the required rendering values for drawing the board.
	 */
	public SVGGraphics2D setSVGRenderingValues()
	{
		final SVGGraphics2D g2d = new SVGGraphics2D((int)containerPlacement.unscaledPlacement().getWidth(), (int)containerPlacement.unscaledPlacement().getHeight());
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		return g2d;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void draw(final Graphics2D g2d, final PlaneType plane, final Context oriContext)
	{		
		final Context context = oriContext.currentInstanceContext();
		try
		{
			switch (plane) 
			{
			case BOARD: 
				bridge.graphicsRenderer().drawBoard(context, g2d, containerPlacement.unscaledPlacement());
				break;
			case TRACK:
				containerTrack.drawTracks(bridge, g2d, context, this);
				break;
			case AXES:
				containerAxis.drawAxes(bridge, g2d);
				break;
			case GRAPH: 
				bridge.graphicsRenderer().drawGraph(context, g2d, containerPlacement.unscaledPlacement());
				break;
			case CONNECTIONS:
				bridge.graphicsRenderer().drawConnections(context, g2d, containerPlacement.unscaledPlacement());
				break;
			case HINTS: 
				if (context.game().metadata().graphics().drawHintType() == PuzzleDrawHintType.None)
					break;
				containerDesign.drawPuzzleHints(g2d, context);
				break;
			case CANDIDATES: 
				containerDesign.drawPuzzleCandidates(g2d, context);
				break;
			case COMPONENTS:
				containerComponents.drawComponents(g2d, context);
				break;
			case PREGENERATION:
				DeveloperGUI.drawPregeneration(bridge, g2d, context, this);
				break;
			case INDICES:
				drawIndices(g2d, context);
				break;
			case POSSIBLEMOVES:
				drawPossibleMoves(g2d, context);
				break;
			case COSTS:
				drawElementCost(g2d, context);
				break;
			default:
				break;
			}
		}
		catch (final Exception e)
		{
			bridge.settingsVC().setErrorReport(bridge.settingsVC().errorReport() + "VC_ERROR: Error detected when attempting to draw " + plane.name() + "\n");
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public void render(final PlaneType plane, final Context oriContext)
	{
		final Context context = oriContext.currentInstanceContext();
		try
		{
			switch (plane) 
			{
			case BOARD: 
				imageSVGString =  containerDesign.createSVGImage(bridge, context);
				break;
			case TRACK:
				break;
			case AXES:
				break;
			case GRAPH: 
				graphSVGString =  GraphUtil.createSVGGraphImage(this);
				break;
			case CONNECTIONS: 
				connectionsSVGString = GraphUtil.createSVGConnectionsImage(this);
				break;
			case HINTS:
				break;
			case CANDIDATES:
				break;
			case COMPONENTS:
				break;
			case PREGENERATION:
				break;
			case INDICES:
				break;
			case POSSIBLEMOVES:
				break;
			case COSTS:
				break;
			default:
				break;
			}
		}
		catch (final Exception e)
		{
			bridge.settingsVC().setErrorReport(bridge.settingsVC().errorReport() + "VC_ERROR: Error detected when attempting to render " + plane.name() + "\n");
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------
	
	/** 
	 * Draw the cost of each element on the board (if enabled in metadata).
	 */
	public void drawElementCost(final Graphics2D g2d, final Context context) 
	{
		if (context.metadata().graphics().showCost())
		{
			g2d.setFont(new Font(g2d.getFont().getFontName(), Font.BOLD, cellRadiusPixels()/5));
	
			for (final TopologyElement graphElement : drawnCells())
			{				
				g2d.setColor(new Color(0, 200, 0));
				StringUtil.drawStringAtPoint(g2d, String.valueOf(graphElement.cost()), graphElement, screenPosn(graphElement.centroid()), true);
			}
			for (final TopologyElement graphElement : drawnEdges())
			{				
				g2d.setColor(new Color(100, 0, 100));
				StringUtil.drawStringAtPoint(g2d, String.valueOf(graphElement.cost()), graphElement, screenPosn(graphElement.centroid()), true);
			}
			for (final TopologyElement graphElement : drawnVertices())
			{				
				g2d.setColor(new Color(255, 0, 0));
				StringUtil.drawStringAtPoint(g2d, String.valueOf(graphElement.cost()), graphElement, screenPosn(graphElement.centroid()), true);
			}
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Draws indices/coordinates of all graph elements, based on set preferences.
	 */
	public void drawIndices(final Graphics2D g2d, final Context context)
	{	
		g2d.setFont(bridge.settingsVC().displayFont());
	
		final List<TopologyElement> possibleElements = drawnGraphElements();

		for (final TopologyElement graphElement : possibleElements)
		{
			if (graphElement.elementType() == SiteType.Cell)
				g2d.setColor(new Color(0, 200, 0));
			else if (graphElement.elementType() == SiteType.Edge)
				g2d.setColor(new Color(100, 0, 100));
			else if (graphElement.elementType() == SiteType.Vertex)
				g2d.setColor(new Color(255, 0, 0));
			
			if (container.index() > 0 || context.board().defaultSite() == graphElement.elementType())
				drawIndexIfRequired(bridge.settingsVC().showIndices(), bridge.settingsVC().showCoordinates(), g2d, graphElement);
			
			if (graphElement.elementType() == SiteType.Cell)
				drawIndexIfRequired(bridge.settingsVC().showCellIndices(), bridge.settingsVC().showCellCoordinates(), g2d, graphElement);
			else if (graphElement.elementType() == SiteType.Edge)
				drawIndexIfRequired(bridge.settingsVC().showEdgeIndices(), bridge.settingsVC().showEdgeCoordinates(), g2d, graphElement);
			else if (graphElement.elementType() == SiteType.Vertex)
				drawIndexIfRequired(bridge.settingsVC().showVertexIndices(), bridge.settingsVC().showVertexCoordinates(), g2d, graphElement);
		}
		
		// Draw container index
		g2d.setColor(new Color(0, 0, 0));
		g2d.setFont(new Font("Arial", Font.BOLD, bridge.settingsVC().displayFont().getSize()*3));
		if (bridge.settingsVC().showContainerIndices())
		{
			final Point2D.Double contianerCenter = new Point2D.Double(containerPlacement.placement().getCenterX(), containerPlacement.placement().getCenterY());
			if (container.index() > 0)
				contianerCenter.y = contianerCenter.y + containerPlacement.cellRadiusPixels();
			StringUtil.drawStringAtPoint(g2d, "" + container.index(), null, contianerCenter, bridge.settingsVC().coordWithOutline());
		}
	}
	
	/**
	 * Draws the index/coordinate of the specified graphElement, depending on the set preferences.
	 */
	private void drawIndexIfRequired(final boolean showIndices, final boolean showCoordinates, final Graphics2D g2d, final TopologyElement graphElement)
	{
		if (showIndices)
			StringUtil.drawStringAtPoint(g2d, "" + graphElement.index(), graphElement, screenPosn(graphElement.centroid()), bridge.settingsVC().coordWithOutline());
		if (showCoordinates)
			StringUtil.drawStringAtPoint(g2d, "" + graphElement.label(), graphElement, screenPosn(graphElement.centroid()), bridge.settingsVC().coordWithOutline());
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Draw the possible moves that the user can perform.
	 * @param g2d
	 */
	public void drawPossibleMoves(final Graphics2D g2d, final Context context)
	{
		if (bridge.settingsVC().thisFrameIsAnimated() || context.game().isDeductionPuzzle())
			return;
		
		final int transparencyAmount = 63;  //125; // between 0 and 255
		final int sz = Math.min(16, (int)(0.4 * containerPlacement.cellRadiusPixels()));

		// Possible consequence move locations
		if (bridge.settingsVC().selectingConsequenceMove())
		{
			for (final Location possibleToLocation : bridge.settingsVC().possibleConsequenceLocations())
			{
				if (ContainerUtil.getContainerId(context, possibleToLocation.site(), possibleToLocation.siteType()) == container().index())
				{
					final int indexOnContainer = ContainerUtil.getContainerSite(context, possibleToLocation.site(), possibleToLocation.siteType());
					
					Point drawPosn = null;
					if (possibleToLocation.siteType() == SiteType.Cell)
						drawPosn = screenPosn(drawnCells().get(indexOnContainer).centroid());
					if (possibleToLocation.siteType() == SiteType.Edge)
						drawPosn = screenPosn(drawnEdges().get(indexOnContainer).centroid());
					if (possibleToLocation.siteType() == SiteType.Vertex)
						drawPosn = screenPosn(drawnVertices().get(indexOnContainer).centroid());
					
					final ContainerState cs = context.state().containerStates()[container().index()];
					final int localState = cs.state(possibleToLocation.site(), possibleToLocation.level(), possibleToLocation.siteType());
					final int value = cs.value(possibleToLocation.site(), possibleToLocation.level(), possibleToLocation.siteType());
					final PieceStackType componentStackType = PieceStackType.getTypeFromValue((int) context.metadata().graphics().stackMetadata(context, container(), possibleToLocation.site(), possibleToLocation.siteType(), localState, value, StackPropertyType.Type));
					final int stackSize = cs.sizeStack(possibleToLocation.site(), possibleToLocation.siteType());
					final Point2D.Double offsetDistance = StackVisuals.calculateStackOffset(bridge, context, container(), componentStackType, containerPlacement.cellRadiusPixels(), possibleToLocation.level(), possibleToLocation.site(), possibleToLocation.siteType(), stackSize, localState, value);
					
					g2d.setColor(new Color(0, 0, 0, transparencyAmount));
					g2d.fillOval((int) (drawPosn.x-2 - sz/2 + offsetDistance.x), (int) (drawPosn.y-2 - sz/2 + offsetDistance.y), sz+4, sz+4);
					g2d.setColor(new Color(249, 166, 0, transparencyAmount));
					g2d.fillOval((int) (drawPosn.x - sz/2 + offsetDistance.x), (int) (drawPosn.y - sz/2 + offsetDistance.y), sz, sz);
				}
			}
		}
		
		// Possible from move locations
		else if (bridge.settingsVC().selectedFromLocation().equals(new FullLocation(Constants.UNDEFINED)) && !context.trial().over() && bridge.settingsVC().showPossibleMoves())
		{			
			for (final Location location : LocationUtil.getLegalFromLocations(context))
			{
				if (ContainerUtil.getContainerId(context, location.site(), location.siteType()) == container().index())
				{
					final int indexOnContainer = ContainerUtil.getContainerSite(context, location.site(), location.siteType());
					
					Point2D drawPosn = new Point();
					if (location.siteType() == SiteType.Cell)
						drawPosn = screenPosn(drawnCells().get(indexOnContainer).centroid());
					if (location.siteType() == SiteType.Edge)
						drawPosn = screenPosn(drawnEdges().get(indexOnContainer).centroid());
					if (location.siteType() == SiteType.Vertex)
						drawPosn = screenPosn(drawnVertices().get(indexOnContainer).centroid());
					
					// take into account placement on the stack
					final ContainerState cs = context.state().containerStates()[container().index()];
					final int localState = cs.state(location.site(), location.level(), location.siteType());
					final int value = cs.value(location.site(), location.level(), location.siteType());
					final PieceStackType componentStackType = PieceStackType.getTypeFromValue((int) context.metadata().graphics().stackMetadata(context, container(), location.site(), location.siteType(), localState, value, StackPropertyType.Type));
					final int stackSize = cs.sizeStack(location.site(), location.siteType());
					final Point2D.Double offsetDistance = StackVisuals.calculateStackOffset(bridge, context, container(), componentStackType, containerPlacement.cellRadiusPixels(), location.level(), location.site(), location.siteType(), stackSize, localState, value);
					
					g2d.setColor(new Color(0, 0, 0, transparencyAmount));
					g2d.fillOval((int) (drawPosn.getX()-2 - sz/2 + offsetDistance.x), (int) (drawPosn.getY()-2 - sz/2 + offsetDistance.y), sz+4, sz+4);
					g2d.setColor(new Color(0, 127, 255, transparencyAmount));
					g2d.fillOval((int) (drawPosn.getX() - sz/2 + offsetDistance.x), (int) (drawPosn.getY() - sz/2 + offsetDistance.y), sz, sz);
				}
			}
		}
		
		// Possible to move locations
		else
		{
			if (!bridge.settingsVC().selectedFromLocation().equals(new FullLocation(Constants.UNDEFINED)) && !context.trial().over() && bridge.settingsVC().showPossibleMoves())
			{
				for (final Location location : LocationUtil.getLegalToLocations(bridge, context))
				{
					if (ContainerUtil.getContainerId(context, location.site(), location.siteType()) == container().index())
					{
						final int indexOnContainer = ContainerUtil.getContainerSite(context, location.site(), location.siteType());
						
						Point2D drawPosn = new Point();
						if (location.siteType() == SiteType.Cell)
							drawPosn = screenPosn(drawnCells().get(indexOnContainer).centroid());
						if (location.siteType() == SiteType.Edge)
							drawPosn = screenPosn(drawnEdges().get(indexOnContainer).centroid());
						if (location.siteType() == SiteType.Vertex)
							drawPosn = screenPosn(drawnVertices().get(indexOnContainer).centroid());
						
						// take into account placement on the stack
						final ContainerState cs = context.state().containerStates()[container().index()];
						final int localState = cs.state(location.site(), location.level(), location.siteType());
						final int value = cs.value(location.site(), location.level(), location.siteType());
						final PieceStackType componentStackType = PieceStackType.getTypeFromValue((int) context.metadata().graphics().stackMetadata(context, container(), location.site(), location.siteType(), localState, value, StackPropertyType.Type));
						final int stackSize = cs.sizeStack(location.site(), location.siteType());
						final Point2D.Double offsetDistance = StackVisuals.calculateStackOffset(bridge, context, container(), componentStackType, containerPlacement.cellRadiusPixels(), location.level(), location.site(), location.siteType(), stackSize, localState, value);
						
						g2d.setColor(new Color(0, 0, 0, transparencyAmount));
						g2d.fillOval((int) (drawPosn.getX()-2 - sz/2 + offsetDistance.x), (int) (drawPosn.getY()-2 - sz/2 + offsetDistance.y), sz+4, sz+4);
						g2d.setColor(new Color(255, 0, 0, transparencyAmount));
						g2d.fillOval((int) (drawPosn.getX() - sz/2 + offsetDistance.x), (int) (drawPosn.getY() - sz/2 + offsetDistance.y), sz, sz);
					}
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public List<TopologyElement> drawnGraphElements()
	{
		final List<TopologyElement> allGraphElements = new ArrayList<>();
		
		for (final TopologyElement g : drawnCells())
			allGraphElements.add(g);
		
		for (final TopologyElement g : drawnEdges())
			allGraphElements.add(g);
		
		for (final TopologyElement g : drawnVertices())
			allGraphElements.add(g);

		return allGraphElements;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public TopologyElement drawnGraphElement(final int index, final SiteType graphElementType)
	{
		if (graphElementType == SiteType.Cell)
			for (final TopologyElement g : drawnCells())
				if (g.index() == index)
					return g;
		
		if (graphElementType == SiteType.Edge)
			for (final TopologyElement g : drawnEdges())
				if (g.index() == index)
					return g;
		
		if (graphElementType == SiteType.Vertex)
			for (final TopologyElement g : drawnVertices())
				if (g.index() == index)
					return g;

		return null;
	}
	
	//-------------------------------------------------------------------------

	public SiteType getElementType(final int index)
	{
		for (final TopologyElement element : drawnGraphElements())
			if (element.index() == index)
				return element.elementType();
		return null;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String graphSVGImage()
	{
		return graphSVGString;
	}
	
	@Override
	public String dualSVGImage()
	{
		return connectionsSVGString;
	}
	
	@Override
	public String containerSVGImage()
	{
		return imageSVGString;
	}
	
	@Override
	public List<Cell> drawnCells()
	{
		return containerPlacement.drawnCells();
	}
	
	@Override
	public List<Edge> drawnEdges()
	{
		return containerPlacement.drawnEdges();
	}
	
	@Override
	public List<Vertex> drawnVertices()
	{
		return containerPlacement.drawnVertices();
	}
	
	@Override
	public Topology topology()
	{
		return container().topology();
	}

	@Override
	public final double pieceScale() 
	{
		return containerComponents.pieceScale();
	}
	
	@Override
	public double containerZoom()
	{
		return containerPlacement.containerZoom();
	}
	
	@Override
	public void drawPuzzleValue(final int value, final int site, final Context context, final Graphics2D g2d, final Point drawPosn, final int imageSize) 
	{
		containerComponents.drawPuzzleValue(value, site, context, g2d, drawPosn, imageSize);
	}

	@Override
	public Container container() 
	{
		return container;
	}
	
	@Override
	public Rectangle placement() 
	{
		return containerPlacement.placement();
	}
	
	@Override
	public int maxDim() 
	{
		return (int) Math.max(containerPlacement.placement().getWidth(), containerPlacement.placement().getHeight());
	}

	@Override
	public double cellRadius() 
	{
		return containerPlacement.cellRadius();
	}

	@Override
	public int cellRadiusPixels() 
	{
		return containerPlacement.cellRadiusPixels();
	}

	@Override
	public Point screenPosn(final Point2D posn) 
	{
		return containerPlacement.screenPosn(posn);
	}
	
	@Override
	public void setPlacement(final Context context, final Rectangle placement) 
	{
		containerPlacement.setPlacement(context, placement);
	}
	
	@Override
	public final double containerScale() 
	{
		return containerPlacement.containerScale();
	}
	
	@Override
	public boolean ignorePieceSelectionLimit()
	{
		return containerDesign.ignorePieceSelectionLimit();
	}
	
	@Override
	public Rectangle unscaledPlacement() 
	{
		return containerPlacement.unscaledPlacement();
	}

}
