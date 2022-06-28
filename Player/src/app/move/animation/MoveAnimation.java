package app.move.animation;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import app.PlayerApp;
import app.utils.BufferedImageUtil;
import app.utils.DrawnImageInfo;
import game.Game;
import game.equipment.container.Container;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import metadata.graphics.util.PieceStackType;
import metadata.graphics.util.StackPropertyType;
import other.action.Action;
import other.context.Context;
import other.location.FullLocation;
import other.location.Location;
import other.move.Move;
import other.state.State;
import other.state.container.ContainerState;
import other.topology.TopologyElement;
import util.ContainerUtil;
import util.HiddenUtil;
import util.ImageInfo;
import util.StackVisuals;

/**
 * Functions that deal with animating moves.
 * 
 * @author Matthew.Stephenson
 */
public class MoveAnimation
{
	
	/** Number of frames that an movement animation lasts for. */
	public static final boolean SLOW_IN_SLOW_OUT = true;
	
	/** Number of frames that an movement animation lasts for. */
	public static int MOVE_PIECE_FRAMES = 30;
	
	/** Number of frames that an add/remove animation lasts for. */
	public static final int FLASH_LENGTH = 10;
	
	/** Length of an animation frame in milliseconds. */
	public static final int ANIMATION_FRAME_LENGTH = 15;
	
	/** Length of time that an animation will last in milliseconds. */
	public static final long ANIMATION_WAIT_TIME = ANIMATION_FRAME_LENGTH * (MOVE_PIECE_FRAMES - 1);
	
	//-----------------------------------------------------------------------------

	/**
	 * Store the required animation information about a move to be animated.
	 */
	public static void saveMoveAnimationDetails(final PlayerApp app, final Move move)
	{
		final AnimationType animationType = getMoveAnimationType(app, move);
		
		if (!animationType.equals(AnimationType.NONE))
		{
			try
			{
				app.settingsPlayer().setDrawingMovingPieceTime(0);
				app.bridge().settingsVC().setAnimationMove(move);
				app.bridge().settingsVC().setThisFrameIsAnimated(true);
				
				app.settingsPlayer().setAnimationParameters(getMoveAnimationParameters(app, move));
				
				app.settingsPlayer().getAnimationTimer().cancel();
				app.settingsPlayer().setAnimationTimer(new Timer());
				app.settingsPlayer().getAnimationTimer().scheduleAtFixedRate(new TimerTask()
				{
				    @Override
				    public void run()
				    {
				    	app.settingsPlayer().setDrawingMovingPieceTime(app.settingsPlayer().getDrawingMovingPieceTime()+1);
				    }
				}, 0, ANIMATION_FRAME_LENGTH);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Draw the move animated, based on the previously stored animation details.
	 */
	public static void moveAnimation(final PlayerApp app, final Graphics2D g2d)
	{
		final AnimationParameters aimationParameters = app.settingsPlayer().animationParameters();
		
		for (int i = 0; i < aimationParameters.pieceImages.size(); i ++)
		{
			BufferedImage pieceImage = aimationParameters.pieceImages.get(i);
			final int time = app.settingsPlayer().getDrawingMovingPieceTime();
			final double transparency = getMoveAnimationTransparency(app, time, aimationParameters.animationType);
			
			if (transparency > 0)
				pieceImage = BufferedImageUtil.makeImageTranslucent(pieceImage, transparency);
			
			final Point drawPoint = MoveAnimation.getMoveAnimationPoint(app, aimationParameters.fromLocations.get(i), aimationParameters.toLocations.get(i), time, aimationParameters.animationType);
			g2d.drawImage(pieceImage, (int)drawPoint.getX(), (int)drawPoint.getY(), null);
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Get AnimationParameters of a provided move.
	 */
	public static AnimationParameters getMoveAnimationParameters(final PlayerApp app, final Move move)
	{
		final Context context = app.contextSnapshot().getContext(app);
		
		final Location moveFrom = move.getFromLocation();
		final Location moveTo = move.getToLocation();
		
		final int containerIdFrom = ContainerUtil.getContainerId(context, moveFrom.site(), moveFrom.siteType());
		final int containerIdTo = ContainerUtil.getContainerId(context, moveTo.site(), moveTo.siteType());
		
		final Point2D graphPointStart = app.bridge().getContainerStyle(containerIdFrom).drawnGraphElement(moveFrom.site(), moveFrom.siteType()).centroid();
		final Point2D graphEndStart = app.bridge().getContainerStyle(containerIdTo).drawnGraphElement(moveTo.site(), moveTo.siteType()).centroid();
		
		final Point startPoint = app.bridge().getContainerStyle(containerIdFrom).screenPosn(graphPointStart);
		final Point endPoint = app.bridge().getContainerStyle(containerIdTo).screenPosn(graphEndStart);
		
		final List<DrawnImageInfo> startDrawnInfo = MoveAnimation.getMovingPieceImages(app, move, moveFrom, startPoint.x, startPoint.y, true);
		final List<DrawnImageInfo> endDrawnInfo = MoveAnimation.getMovingPieceImages(app, move, moveFrom, endPoint.x, endPoint.y, true);
		
		final List<BufferedImage> pieceImages = new ArrayList<>();
		final List<Point> startPoints = new ArrayList<>();
		final List<Point> endPoints = new ArrayList<>();
		
		for (final DrawnImageInfo d : startDrawnInfo)
		{
			startPoints.add(d.imageInfo().drawPosn());
			pieceImages.add(d.pieceImage());
		}
		for (final DrawnImageInfo d : endDrawnInfo)
		{
			endPoints.add(d.imageInfo().drawPosn());
		}
		
		// Placeholders in case no images/points found
		if (startPoints.size() == 0)
			startPoints.add(new Point(0,0));
		if (endPoints.size() == 0)
			endPoints.add(new Point(0,0));
		if (pieceImages.size() == 0)
			pieceImages.add(new BufferedImage(1,1,1));
		
		return new AnimationParameters
				(
					MoveAnimation.getMoveAnimationType(app, move),
					pieceImages,
					startPoints,
					endPoints,
					MoveAnimation.ANIMATION_WAIT_TIME
				);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Get transparency value for a given animationType and time through the animation.
	 */
	private static double getMoveAnimationTransparency(final PlayerApp app, final int time, final AnimationType animationType)
	{
		if (animationType.equals(AnimationType.PULSE))
		{    			
			// How transparent the animated piece should be.
			double currentflashValue = 0.0;

			final int flashCycleValue = time
					% (MoveAnimation.FLASH_LENGTH * 2);

			if (flashCycleValue >= MoveAnimation.FLASH_LENGTH)
			{
				currentflashValue = 1.0 - (time % (MoveAnimation.FLASH_LENGTH)) / (double) (MoveAnimation.FLASH_LENGTH);
			}
			else
			{
				currentflashValue = (time % (MoveAnimation.FLASH_LENGTH)) / (double) (MoveAnimation.FLASH_LENGTH);
			}

			return 1.0 - currentflashValue;
		}
		
		return 0.0;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Draw an animation for a move between two locations, at a given time frame.
	 */
	private static Point getMoveAnimationPoint(final PlayerApp app, final Point startPoint, final Point endPoint, final int time, final AnimationType animationType)
	{
    	try
    	{
    		final Location moveFrom = app.bridge().settingsVC().getAnimationMove().getFromLocation();
    		
    		// Piece is moving from one location to another.
    		if (animationType.equals(AnimationType.DRAG))
    		{
				final Point2D.Double pointOnTimeLine = new Point2D.Double();

				if (MoveAnimation.SLOW_IN_SLOW_OUT)
				{
					double multiplyFactor = (time/(double)(MoveAnimation.MOVE_PIECE_FRAMES));
					multiplyFactor = (Math.cos(multiplyFactor*Math.PI + Math.PI) + 1) / 2;
					pointOnTimeLine.x = (startPoint.x + ((endPoint.x - startPoint.x) * multiplyFactor));
					pointOnTimeLine.y = (startPoint.y + ((endPoint.y - startPoint.y) * multiplyFactor));
				}
				else
				{
					final double multiplyFactor = (time/(double)(MoveAnimation.MOVE_PIECE_FRAMES));
					pointOnTimeLine.x = (startPoint.x + ((endPoint.x - startPoint.x) * multiplyFactor));
					pointOnTimeLine.y = (startPoint.y + ((endPoint.y - startPoint.y) * multiplyFactor));
				}
				
				app.repaintComponentBetweenPoints(app.contextSnapshot().getContext(app), moveFrom, startPoint, endPoint);
				return new Point((int)pointOnTimeLine.x, (int) pointOnTimeLine.y);
    		}
    		// Piece is being added/removed/changed at a single site.
    		else if (animationType.equals(AnimationType.PULSE))
    		{    			
    			app.repaintComponentBetweenPoints(app.contextSnapshot().getContext(app), moveFrom, startPoint, endPoint);
    			return new Point(startPoint.x, startPoint.y);
    		}
    	}
    	catch (final Exception e)
    	{
    		// If something goes wrong, cancel the animation.
    		app.settingsPlayer().setDrawingMovingPieceTime(MoveAnimation.MOVE_PIECE_FRAMES);
    	}
    	
    	return null;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Get the type of animation for the move.
	 */
	public static AnimationType getMoveAnimationType(final PlayerApp app, final Move move)
	{
		final Context context = app.contextSnapshot().getContext(app);
		final Game game = context.game();
		
		if (move == null)
			return AnimationType.NONE;
		
		if (app.bridge().settingsVC().noAnimation())
			return AnimationType.NONE;
		
		if (game.isDeductionPuzzle())
			return AnimationType.NONE;
		
		if (game.hasLargePiece())
			return AnimationType.NONE;
		
		if (move.from() == -1)
			return AnimationType.NONE;
		
		if (move.to() == -1)
			return AnimationType.NONE;
		
//		if (app.settingsPlayer().animationType().equals(AnimationVisualsType.All) && move.actionType().equals(ActionType.Select))
//			return AnimationType.NONE;
		
		if (!move.getFromLocation().equals(move.getToLocation()))
			return AnimationType.DRAG;
		
		if (move.getFromLocation().equals(move.getToLocation()))
			return AnimationType.PULSE;
		
		return AnimationType.NONE; // manager.settingsManager().showAnimation();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Reset all necessary animation variables to their defaults.
	 */
	public static void resetAnimationValues(final PlayerApp app)
	{
		app.settingsPlayer().setDrawingMovingPieceTime(MOVE_PIECE_FRAMES);
		app.bridge().settingsVC().setAnimationMove(null);
		app.bridge().settingsVC().setThisFrameIsAnimated(false);
		app.settingsPlayer().getAnimationTimer().cancel();
		app.settingsPlayer().setAnimationParameters(new AnimationParameters());
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Draw the piece being moved.
	 */
	public static List<DrawnImageInfo> getMovingPieceImages(final PlayerApp app, final Move move, final Location selectedLocation, final int x, final int y, final boolean drawingAnimation)
	{						
		final List<DrawnImageInfo> allMovingPieceImages = new ArrayList<>();
		
		final Context context = app.contextSnapshot().getContext(app);
		final Moves legal = context.game().moves(context);
		
		if (move != null)
		{
			// Replace legal moves with the specific move being done.
			final Moves moves = new BaseMoves(null);
			moves.moves().add(move);
		}
		
		// If all moves from this location involve the same level range, then use that level range.
		final int[] levelMinMax = StackVisuals.getLevelMinAndMax(legal, selectedLocation);

		for (int i = 0; i < context.numContainers(); i++)
		{
			final Container container = context.equipment().containers()[i];
			final int containerIndex = container.index();
			final List<TopologyElement> graphElements = app.bridge().getContainerStyle(containerIndex).drawnGraphElements();
			final State state = app.contextSnapshot().getContext(app).state();
			final ContainerState cs = state.containerStates()[i];
			
			for (final TopologyElement graphElement : graphElements)
			{
				if 
				(
					graphElement.index() == selectedLocation.site() 
					&& 
					graphElement.elementType() == selectedLocation.siteType()
				)
				{
					int lowestSelectedLevel = -1;
					for (int level = levelMinMax[0]; level <= levelMinMax[1]; level++)
					{
						final int localState = cs.state(selectedLocation.site(), level, selectedLocation.siteType());
						final int value = cs.value(selectedLocation.site(), level, selectedLocation.siteType());
						final PieceStackType componentStackType = PieceStackType.getTypeFromValue((int) context.metadata().graphics().stackMetadata(context, container, selectedLocation.site(), selectedLocation.siteType(), localState, value, StackPropertyType.Type));
						
						// get the what of the component at the selected location
						int what = cs.what(graphElement.index(), level, graphElement.elementType());
						
						// If adding a piece at the site, get the what of the move (first action that matches selected location) instead.
						if (what == 0)
						{
							if (move != null)
								what = move.what();
							
							for (final Move m : legal.moves())
							{
								if (m.getFromLocation().equals(selectedLocation))
								{
									for (final Action a : m.actions())
									{
										final Location actionLocationA = new FullLocation(a.from(), a.levelFrom(),
												a.fromType());
										final Location actionLocationB = new FullLocation(a.to(), a.levelTo(),
												a.toType());
										final Location testingLocation = new FullLocation(selectedLocation.site(), level, selectedLocation.siteType());
										if (actionLocationA.equals(testingLocation) && actionLocationB.equals(testingLocation))
										{
											what = a.what();
											break;
										}
									}
								}
								
								if (what != 0)
									break;
							}
						}

						// If a piece was found
						if (what > 0)
						{
							app.settingsPlayer().setDragComponent
							(
								app.contextSnapshot().getContext(app).equipment().components()[what]
							);
							
							try
							{
								if (lowestSelectedLevel == -1)
									lowestSelectedLevel = level;
								if (drawingAnimation)
									lowestSelectedLevel = 0;
								
								BufferedImage pieceImage = null;
								
								final int cellSize = app.bridge().getContainerStyle(0).cellRadiusPixels(); // This line uses the size of the piece where it current is when drawing it 
								
								final int hiddenValue = HiddenUtil.siteHiddenBitsetInteger(context, cs, graphElement.index(), level, context.state().mover(), graphElement.elementType());
								
								int imageSize = cellSize*2;
								try
								{
									imageSize = app.graphicsCache().getComponentImageSize(containerIndex, app.settingsPlayer().dragComponent().index(), cs.who(graphElement.index(), graphElement.elementType()), localState, cs.value(graphElement.index(), graphElement.elementType()), hiddenValue, cs.rotation(graphElement.index(), graphElement.elementType()));
								}
								catch (final Exception e)
								{
									// Component image doesn't exist yet.
								}
									
								if (app.settingsPlayer().dragComponent().isLargePiece())
								{
									// If the local state of the from is different from that of the move, the piece can be rotated.
									for (final Move m : legal.moves())
										if (cs.state(graphElement.index(), graphElement.elementType()) != m.state())
											app.setVolatileMessage("You can rotate this piece by pressing 'r'");
									
									// If we have rotated the piece all the way around to its starting walk.
									if (cs.state(graphElement.index(), graphElement.elementType()) + app.settingsPlayer().currentWalkExtra() >= app.settingsPlayer().dragComponent().walk().length * (app.contextSnapshot().getContext(app).board().topology().supportedDirections(SiteType.Cell).size() / 2))
										app.settingsPlayer().setCurrentWalkExtra(-cs.state(graphElement.index(), graphElement.elementType()));
	
									pieceImage = app.graphicsCache().getComponentImage(app.bridge(), 0, app.settingsPlayer().dragComponent(), cs.who(graphElement.index(), graphElement.elementType()), cs.state(graphElement.index(), graphElement.elementType()) + app.settingsPlayer().currentWalkExtra(), cs.value(graphElement.index(), graphElement.elementType()), graphElement.index(), 0, graphElement.elementType(), cellSize*2, app.contextSnapshot().getContext(app), hiddenValue, cs.rotation(graphElement.index(), graphElement.elementType()), true);
									app.settingsPlayer().setDragComponentState(cs.state(graphElement.index(), graphElement.elementType()) + app.settingsPlayer().currentWalkExtra());
								}
								else
								{
									pieceImage = app.graphicsCache().getComponentImage(app.bridge(), i, app.settingsPlayer().dragComponent(), cs.who(graphElement.index(), graphElement.elementType()), cs.state(graphElement.index(), graphElement.elementType()), cs.value(graphElement.index(), graphElement.elementType()), graphElement.index(), 0 , graphElement.elementType(), imageSize, app.contextSnapshot().getContext(app), hiddenValue, cs.rotation(graphElement.index(), graphElement.elementType()), true);
								}
								
								final Point2D.Double dragPosition = new Point2D.Double(x - (pieceImage.getWidth() / 2), y - (pieceImage.getHeight() / 2));
								
								final int stackSize = cs.sizeStack(selectedLocation.site(), selectedLocation.siteType());
								final Point2D.Double offsetDistance = StackVisuals.calculateStackOffset(app.bridge(), context, container, componentStackType, cellSize, level-lowestSelectedLevel, selectedLocation.site(), selectedLocation.siteType(), stackSize, localState, value);

								allMovingPieceImages.add(new DrawnImageInfo(pieceImage, new ImageInfo(new Point((int)(dragPosition.x + offsetDistance.x), (int)(dragPosition.y + offsetDistance.y)), graphElement.index(), level, graphElement.elementType())));

								if (!context.currentInstanceContext().game().isStacking())
									return allMovingPieceImages;
							}
							catch (final NullPointerException e)
							{
								e.printStackTrace();
							}
						}
						else
						{
							break;
						}
					}
					return allMovingPieceImages;
				}
			}
		}
		
		return allMovingPieceImages;
	}
	
	//-------------------------------------------------------------------------

}
