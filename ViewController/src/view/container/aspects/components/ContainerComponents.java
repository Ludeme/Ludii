package view.container.aspects.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import bridge.Bridge;
import game.equipment.component.Component;
import game.equipment.container.Container;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import main.Constants;
import metadata.graphics.util.PieceColourType;
import metadata.graphics.util.PieceStackType;
import metadata.graphics.util.StackPropertyType;
import other.context.Context;
import other.location.FullLocation;
import other.location.Location;
import other.state.State;
import other.state.container.ContainerState;
import other.topology.Cell;
import other.topology.TopologyElement;
import other.topology.Vertex;
import util.ContainerUtil;
import util.GraphUtil;
import util.HiddenUtil;
import util.ImageInfo;
import util.StackVisuals;
import view.container.BaseContainerStyle;

/**
 * Defines how the components are drawn on the associated container style.
 * 
 * @author Matthew.Stephenson
 */
public class ContainerComponents 
{
	/** The container style associated with this components aspect. */
	private final BaseContainerStyle containerStyle;
	
	/** Additional piece scale multiplier for components in this style. */
	private double pieceScale = 1.0;
	
	/** Parent bridge object. */
	protected Bridge bridge;
	
	//-------------------------------------------------------------------------
	
	public ContainerComponents(final Bridge bridge, final BaseContainerStyle containerStyle)
	{
		this.bridge = bridge;
		this.containerStyle = containerStyle;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Draw all necessary components on the container.
	 */
	public void drawComponents(final Graphics2D g2d, final Context context)
	{
		final List<TopologyElement> allGraphElements = GraphUtil.reorderGraphElementsTopDown(containerStyle.drawnGraphElements(), context);
		drawComponents(g2d, context, (ArrayList<? extends TopologyElement>) allGraphElements);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Draw all necessary components on the container, on a provided set of graphElements.
	 */
	protected void drawComponents(final Graphics2D g2d, final Context context, final ArrayList<? extends TopologyElement> allGraphElements)
	{		
		final State state = context.state();
		final Container container = containerStyle.container();
		final int cellRadiusPixels = containerStyle.cellRadiusPixels();
		final Moves legal = context.moves(context);
		
		if (container != null && state.containerStates().length > container.index())
		{
			if (context.metadata().graphics().replaceComponentsWithFilledCells())
			{
				fillCellsBasedOnOwner(g2d, context);
				return;
			}
			
			final ContainerState cs = state.containerStates()[container.index()];
			
			// Draw pieces
			for (int j = 0; j < allGraphElements.size(); j++)
			{
				final TopologyElement graphElement = allGraphElements.get(j);
				final Point2D posn = graphElement.centroid();

				final int site = allGraphElements.get(j).index();
				final SiteType type = graphElement.elementType();
				
				final boolean isEmpty = cs.isEmpty(site, type);
				final int stackSize = cs.sizeStack(site, type);
				
				for (int level = 0; level < stackSize; level++)
				{
					final int what = cs.what(site, level, type);

					if (!isEmpty)
					{
						int localState = cs.state(site, level, type);
						final int value = cs.value(site, level, type);
						final Component component = context.equipment().components()[what];
						
						// if the what is zero, then it's a hidden piece.
						if (what == 0)
						{
							// Cannot hide the what for games with large pieces.
							if (context.game().hasLargePiece())
								continue;
							
							component.setRoleFromPlayerId(cs.who(site, level, type));
							component.create(context.game());
						}

						// When drawing dice, use local state of the next roll.
						if (component.isDie())
						{
							final int diceLocalState = context.diceSiteState().get(site);
							if (diceLocalState != -99)
								localState = diceLocalState;
						}
						
						final int mover = context.state().mover();
						
						int count = cs.count(site, type);
						if (HiddenUtil.siteCountHidden(context, cs, site, level, mover, type))
							count = -1;
						if (HiddenUtil.siteHidden(context, cs, site, level, mover, type))
							count = 0;

						double transparency = 0;
						if 
						(
							bridge.settingsVC().selectedFromLocation().site() == site 
							&&
							bridge.settingsVC().selectedFromLocation().level() == level 
							&& 
							bridge.settingsVC().selectedFromLocation().siteType() == type
						)
							transparency = 0.5;
						
						int imageSize = (int) (cellRadiusPixels * 2 * pieceScale() * bridge.getComponentStyle(component.index()).scale(context, container.index(), localState, value));	
						imageSize = Math.max(imageSize, Constants.MIN_IMAGE_SIZE); // Image must be at least 2 pixels in size.
						
						final PieceStackType componentStackType = PieceStackType.getTypeFromValue((int) context.metadata().graphics().stackMetadata(context, container, site, type, localState, value, StackPropertyType.Type));
						final Point2D.Double stackOffset = StackVisuals.calculateStackOffset(bridge, context, container, componentStackType, cellRadiusPixels, level, site, type, stackSize, localState,value);

						final Point drawPosn = containerStyle.screenPosn(posn);
						drawPosn.x += stackOffset.x - imageSize/2;
						drawPosn.y += stackOffset.y - imageSize/2;

						// These values are used for large pieces to represent vector from origin to center
						if (component.isLargePiece() && bridge.getComponentStyle(component.index()).origin().size() > localState && container.index() == 0)
						{
							final Point origin = bridge.getComponentStyle(component.index()).origin().get(localState);		
							if (origin != null)
							{
								drawPosn.x -= origin.x;
								drawPosn.y -= origin.y;
							}
						}

						if (bridge.settingsVC().pieceBeingDragged() || bridge.settingsVC().thisFrameIsAnimated())
						{
							try
							{
								Location location;
								if (bridge.settingsVC().pieceBeingDragged())
									location = bridge.settingsVC().selectedFromLocation();
								else
									location = bridge.settingsVC().getAnimationMove().getFromLocation();
	
								if 
								(
									location.equals(new FullLocation(site, level, type))
									||
									(
										// If dragging/animating a piece in a stack, don't draw the above pieces either.
										site == location.site()
										&&
										type == location.siteType()
										&&
										level >= StackVisuals.getLevelMinAndMax(legal, location)[0]
										&&
										level <= StackVisuals.getLevelMinAndMax(legal, location)[1]
									)
								)
								{
									if (count > 1)
										count--;
									else
										continue;
								}
							}
							catch (final Exception e)
							{
								// carry on, sometimes animation timers don't line up...
							}
						}
						
						if (component.isTile() && container.index() == 0 && !HiddenUtil.siteHidden(context, cs, site, level, mover, type))
							for (final Integer cellIndex : ContainerUtil.cellsCoveredByPiece(context, container, component, site, localState))
								drawTilePiece(g2d, context, component, cellIndex.intValue(), stackOffset.x, stackOffset.y, container, localState, value, imageSize);
						
						bridge.graphicsRenderer().drawComponent(g2d, context, new ImageInfo(drawPosn, site, level, type, component, localState, value, transparency, cs.rotation(site, level, type), container.index(), imageSize, count));
					}
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Draws a tile component at the specified site.
	 */
	private void drawTilePiece(final Graphics2D g2d, final Context context, final Component component, final int site, final double stackOffsetX, final double stackOffsetY, final Container container, final int localState, final int value, final int imageSize)
	{
		final GeneralPath path = new GeneralPath();
		final int containerSite = ContainerUtil.getContainerSite(context, site, SiteType.Cell); 
		final int containerIndex = ContainerUtil.getContainerId(context, containerSite, SiteType.Cell);
		
		final Cell cellToFill = bridge.getContainerStyle(container.index()).drawnCells().get(containerSite);
		Point nextPoint = bridge.getContainerStyle(container.index()).screenPosn(cellToFill.vertices().get(0).centroid());
		path.moveTo(nextPoint.x + stackOffsetX, nextPoint.y + stackOffsetY);
		for (final Vertex vertex : cellToFill.vertices())
		{
			nextPoint = bridge.getContainerStyle(container.index()).screenPosn(vertex.centroid());
			path.lineTo(nextPoint.x + stackOffsetX, nextPoint.y + stackOffsetY);
		}
		path.closePath();

		final Color fillColour = context.game().metadata().graphics().pieceColour(context, component.owner(), component.name(), containerIndex, localState, value, PieceColourType.Fill);
		if (fillColour != null)
			g2d.setColor(fillColour);
		else
			g2d.setColor(bridge.settingsColour().playerColour(context, component.owner()));
		
		g2d.fill(path);
		
		final Color pieceEdgeColour = context.game().metadata().graphics().pieceColour(context, component.owner(), component.name(), containerIndex, localState, value, PieceColourType.Edge);
	 	if (pieceEdgeColour != null)
	 	{
	 		final Shape oldClip = g2d.getClip();
	 		g2d.setStroke(new BasicStroke(imageSize/10 + 1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
	 		g2d.setColor(pieceEdgeColour);
	 		g2d.setClip(path);
	  		g2d.draw(path);
	  		g2d.setClip(oldClip);
	 	}
	}
	
	//-------------------------------------------------------------------------

	/** 
	 * Fills cells of the container that are owned by specific players with their colour. 
	 */
	public void fillCellsBasedOnOwner(final Graphics2D g2d, final Context context)
	{
		final ContainerState cs = context.state().containerStates()[0];
		for (int f = 0; f < context.topology().cells().size(); f++)
		{
			if (cs.whoCell(f) != 0)
			{
				final Cell face = context.topology().cells().get(f);
				final GeneralPath path = new GeneralPath();
				for (int v = 0; v < face.vertices().size(); v++)
				{
					if (path.getCurrentPoint() == null)
					{
						final Vertex prev = face.vertices().get(face.vertices().size() - 1);
						final Point drawPrev = containerStyle.screenPosn(prev.centroid());
						path.moveTo(drawPrev.x, drawPrev.y);
					}
					final Vertex corner = face.vertices().get(v);
					final Point drawCorner = containerStyle.screenPosn(corner.centroid());
					path.lineTo(drawCorner.x, drawCorner.y);
				}

				g2d.setColor(bridge.settingsColour().playerColour(context, cs.whoCell(f)));
				g2d.fill(path);
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Draws a puzzle value at a specified site.
	 */
	public void drawPuzzleValue(final int value, final int site, final Context context, final Graphics2D g2d, final Point drawPosn, final int imageSize) 
	{	
		// Do nothing by default.
	}
	
	//-------------------------------------------------------------------------

	public double pieceScale() 
	{
		return pieceScale;
	}

	public void setPieceScale(final double pieceScale) 
	{
		this.pieceScale = pieceScale;
	}
	
	//-------------------------------------------------------------------------

}
