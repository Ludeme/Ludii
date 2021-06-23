package app;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import app.move.MoveHandler;
import app.move.animation.MoveAnimation;
import app.utils.BufferedImageUtil;
import app.utils.ContextSnapshot;
import app.utils.GameUtil;
import app.utils.GraphicsCache;
import app.utils.RemoteDialogFunctionsPublic;
import app.utils.SVGUtil;
import app.utils.SettingsPlayer;
import app.views.View;
import bridge.Bridge;
import bridge.PlatformGraphics;
import game.equipment.container.board.Board;
import game.types.board.SiteType;
import main.Constants;
import main.collections.FastArrayList;
import manager.Manager;
import manager.PlayerInterface;
import metadata.graphics.util.PieceStackType;
import metadata.graphics.util.StackPropertyType;
import other.context.Context;
import other.location.FullLocation;
import other.location.Location;
import other.move.Move;
import other.state.State;
import other.state.container.ContainerState;
import tournament.Tournament;
import util.HiddenUtil;
import util.ImageInfo;
import util.PlaneType;
import util.StringUtil;

/**
 * Abstract PlayerApp class to be extended by each platform specific Player.
 * 
 * @author Matthew.Stephenson
 */
public abstract class PlayerApp implements PlayerInterface, ActionListener, ItemListener, PlatformGraphics
{
	private final Manager manager = new Manager(this);
	private final Bridge bridge = new Bridge();
	private final ContextSnapshot contextSnapshot = new ContextSnapshot();
	private final SettingsPlayer settingsPlayer = new SettingsPlayer();
	private final GraphicsCache graphicsCache = new GraphicsCache();
	private final RemoteDialogFunctionsPublic remoteDialogFunctionsPublic = RemoteDialogFunctionsPublic.construct();

	//-------------------------------------------------------------------------
	
	public abstract Tournament tournament();
	public abstract void setTournament(Tournament tournament);
	public abstract void reportError(String error);
	public abstract void repaintComponentBetweenPoints(Context context, Location moveFrom, Point startPoint, Point endPoint);
	public abstract void showPuzzleDialog(final int site);
	public abstract void showPossibleMovesDialog(final Context context, final FastArrayList<Move> possibleMoves);
	public abstract void saveTrial();
	public abstract void playSound(String soundName);
	public abstract void setVolatileMessage(String text);
	public abstract void clearGraphicsCache();
	public abstract void resetUIVariables();
	public abstract void writeTextToFile(String fileName, String log);
	public abstract void loadGameSpecificPreferences();
	public abstract void resetMenuGUI();
	public abstract void showSettingsDialog();
	public abstract void showOtherDialog(FastArrayList<Move> otherPossibleMoves);
	public abstract void showInfoDialog();
	
	public abstract int width();
	public abstract int height();
	
	public abstract List<View> getPanels();
	
	public abstract Rectangle[] playerSwatchList();
	public abstract Rectangle[] playerNameList();
	public abstract boolean[] playerSwatchHover();
	public abstract boolean[] playerNameHover();
	
	public abstract void repaint(Rectangle rect);
	
	//-------------------------------------------------------------------------
	
	public Manager manager() 
	{
		return manager;
	}

	public Bridge bridge() 
	{
		return bridge;
	}
	
	public SettingsPlayer settingsPlayer() 
	{
		return settingsPlayer;
	}
	
	public ContextSnapshot contextSnapshot() 
	{
		return contextSnapshot;
	}
	
	public GraphicsCache graphicsCache() 
	{
		return graphicsCache;
	}
	
	public RemoteDialogFunctionsPublic remoteDialogFunctionsPublic() 
	{
		return remoteDialogFunctionsPublic;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Location locationOfClickedImage(final Point pt)
	{
		final ArrayList<Location> overlappedLocations = new ArrayList<>();
		for (int imageIndex = 0; imageIndex < graphicsCache().allDrawnComponents().size(); imageIndex++)
		{
			//check if pixel is on image
			final BufferedImage image = graphicsCache().allDrawnComponents().get(imageIndex).pieceImage();
			final Point imageDrawPosn = graphicsCache().allDrawnComponents().get(imageIndex).imageInfo().drawPosn();
			if (BufferedImageUtil.pointOverlapsImage(pt, image, imageDrawPosn))
			{
				final int clickedIndex = graphicsCache().allDrawnComponents().get(imageIndex).imageInfo().site();
				final int clickedLevel = graphicsCache().allDrawnComponents().get(imageIndex).imageInfo().level();
				final SiteType clickedType = graphicsCache().allDrawnComponents().get(imageIndex).imageInfo().graphElementType();
				
				overlappedLocations.add(new FullLocation(clickedIndex, clickedLevel, clickedType));
			}
		}
		
		if (overlappedLocations.size() == 1)
		{
			return overlappedLocations.get(0);
		}
		else if (overlappedLocations.size() > 1)
		{
			Location highestLocation = null;
			int highestLevel = -1;
			for (final Location location : overlappedLocations)
			{
				if (location.level() > highestLevel)
				{
					highestLevel = location.level();
					highestLocation = location;
				}
			}
			return highestLocation;
		}
		
		return new FullLocation(-1, 0, SiteType.Cell);
	}
	
	//-----------------------------------------------------------------------------
	
	@Override
	public void drawSVG(final Context context, final Graphics2D g2d, final SVGGraphics2D svg, final ImageInfo imageInfo)
	{
		final BufferedImage componentImage = SVGUtil.createSVGImage(svg.getSVGElement(), imageInfo.imageSize(), imageInfo.imageSize());
		g2d.drawImage(componentImage, imageInfo.drawPosn().x - imageInfo.imageSize()/2, imageInfo.drawPosn().y - imageInfo.imageSize()/2, null);
	}

	//-----------------------------------------------------------------------------
	
	@Override
	public void drawComponent(final Graphics2D g2d, final Context context, final ImageInfo imageInfo)
	{
		final State state = context.state();
		final ContainerState cs = state.containerStates()[imageInfo.containerIndex()];
		
		final int hiddenValue = HiddenUtil.siteHiddenBitsetInteger(context, cs, imageInfo.site(), imageInfo.level(), context.state().mover(), imageInfo.graphElementType());

		final BufferedImage componentImage = graphicsCache().getComponentImage(bridge, imageInfo.containerIndex(), imageInfo.component(), imageInfo.component().owner(), imageInfo.localState(), imageInfo.value(), imageInfo.site(), imageInfo.level(), imageInfo.graphElementType(), imageInfo.imageSize(), context, hiddenValue, imageInfo.rotation(), false);

		graphicsCache().drawPiece(g2d, context,  componentImage, imageInfo.drawPosn(), imageInfo.site(), imageInfo.level(), imageInfo.graphElementType(), imageInfo.transparency());
		
		drawPieceCount(g2d, context, imageInfo, cs);
	}
	
	//-----------------------------------------------------------------------------

	private void drawPieceCount(final Graphics2D g2d, final Context context, final ImageInfo imageInfo, final ContainerState cs)
	{
		if (context.equipment().components()[cs.what(imageInfo.site(), imageInfo.level(), imageInfo.graphElementType())].isDomino())
			return;
		
		final int localState = cs.state(imageInfo.site(), imageInfo.level(), imageInfo.graphElementType());
		final PieceStackType componentStackType = PieceStackType.getTypeFromValue((int) context.metadata().graphics().stackMetadata(context, context.equipment().containers()[imageInfo.containerIndex()], imageInfo.site(), imageInfo.graphElementType(), localState, StackPropertyType.Type));
		
		if (imageInfo.count() < 0)
		{
			drawCountValue(context, g2d, imageInfo, "?", 1, 1);
		}
		if (imageInfo.count() > 1)
		{
			drawCountValue(context, g2d, imageInfo, Integer.toString(imageInfo.count()), 1, 1);
		}
		else if (componentStackType.equals(PieceStackType.Count) && cs.sizeStack(imageInfo.site(), imageInfo.graphElementType()) > 1)
		{
			drawCountValue(context, g2d, imageInfo, Integer.toString(cs.sizeStack(imageInfo.site(), imageInfo.graphElementType())), 1, 1);
		}
		else if (componentStackType.equals(PieceStackType.CountColoured) && cs.sizeStack(imageInfo.site(), imageInfo.graphElementType()) > 1)
		{
			// Record the number of pieces owned by each player in this stack.
			final int [] playerCountArray = new int[Constants.MAX_PLAYERS];
			Arrays.fill(playerCountArray, 0);
			for (int i = 0; i < Constants.MAX_STACK_HEIGHT; i++)
				if (cs.what(imageInfo.site(), i, imageInfo.graphElementType()) != 0)
						playerCountArray[cs.who(imageInfo.site(), i, imageInfo.graphElementType())]++;

			// Record the total number of counts to be drawn.
			int totalCountsDrawn = 0;
			for (int i = 0; i < Constants.MAX_PLAYERS; i++)
				if (playerCountArray[i] != 0)
					totalCountsDrawn++;
			
			// Display the counts for each player.
			int numberCountsDrawn = 0;
			for (int i = 0; i < Constants.MAX_PLAYERS; i++)
				if (playerCountArray[i] != 0)
					drawCountValue(context, g2d, imageInfo, Integer.toString(playerCountArray[i]), ++numberCountsDrawn, totalCountsDrawn);
		}
	}
	
	//-----------------------------------------------------------------------------
	
	/**
	 * For drawing a specific count value on an image, its position adjusted based on how many are to be drawn.
	 * @param g2d
	 * @param imageInfo
	 * @param count					The count value.
	 * @param numberCountsDrawn		The index of this count, out of the total counts to be drawn.
	 * @param totalCountsDrawn		The total number of counts to be drawn.
	 * @param textColour			The colour of the count string.
	 */
	private void drawCountValue(final Context context, final Graphics2D g2d, final ImageInfo imageInfo, final String count, final int numberCountsDrawn, final int totalCountsDrawn)
	{
		g2d.setColor(bridge.getComponentStyle(imageInfo.component().index()).getSecondaryColour());
		g2d.setFont(new Font("Arial", Font.PLAIN, 16));
		
		if (imageInfo.containerIndex() > 0)
		{
			final Rectangle2D countRect = g2d.getFont().getStringBounds("x"+count, g2d.getFontRenderContext());
			final int drawPosnX = (int)(imageInfo.drawPosn().x + imageInfo.imageSize()/2 - countRect.getWidth()/2);
			final int drawPosnY = (int)(imageInfo.drawPosn().y + imageInfo.imageSize() + countRect.getHeight()/2 * 1.5);
			g2d.drawString("x"+count,drawPosnX,drawPosnY);
		}
		else
		{
			final Rectangle2D countRect = g2d.getFont().getStringBounds(count, g2d.getFontRenderContext());
			final int drawPosnX = imageInfo.drawPosn().x + imageInfo.imageSize()/2;
			final int drawPosnY = imageInfo.drawPosn().y + imageInfo.imageSize()/2;
			
			g2d.setColor(Color.BLACK);
			StringUtil.drawStringAtPoint(g2d, count, null, new Point2D.Double(drawPosnX,drawPosnY+(numberCountsDrawn-1)*countRect.getHeight()-(totalCountsDrawn-1)*countRect.getHeight()/2), true);
		}
	}

	//-----------------------------------------------------------------------------
	
	@Override
	public void drawBoard(final Context context, final Graphics2D g2d, final Rectangle2D boardDimensions)
	{
		if (graphicsCache().boardImage() == null)
		{
			final Board board = context.board();
			bridge.getContainerStyle(board.index()).render(PlaneType.BOARD, context);
			
			final String svg = bridge.getContainerStyle(board.index()).containerSVGImage();
			if (svg == null || svg.equals(""))
				return;
			
			graphicsCache().setBoardImage(SVGUtil.createSVGImage(svg, boardDimensions.getWidth(), boardDimensions.getHeight()));
		}

		if (!context.game().metadata().graphics().boardHidden())
		{
			g2d.drawImage(graphicsCache().boardImage(), 0, 0, null);
		}
	}
	
	//-----------------------------------------------------------------------------
	
	@Override
	public void drawGraph(final Context context, final Graphics2D g2d, final Rectangle2D boardDimensions)
	{
		if (graphicsCache().graphImage() == null || context.board().isBoardless())
		{
			final Board board = context.board();
			bridge.getContainerStyle(board.index()).render(PlaneType.GRAPH, context);
			
			final String svg = bridge.getContainerStyle(board.index()).graphSVGImage();
			if (svg == null)
				return;

			graphicsCache().setGraphImage(SVGUtil.createSVGImage(svg, boardDimensions.getWidth(), boardDimensions.getHeight()));
		}

		g2d.drawImage(graphicsCache().graphImage(), 0, 0, null);
	}
	
	//-----------------------------------------------------------------------------

	@Override
	public void drawConnections(final Context context, final Graphics2D g2d, final Rectangle2D boardDimensions)
	{
		if (graphicsCache().connectionsImage() == null || context.board().isBoardless())
		{
			final Board board = context.board();
			bridge.getContainerStyle(board.index()).render(PlaneType.CONNECTIONS, context);
			
			final String svg = bridge.getContainerStyle(board.index()).dualSVGImage();
			if (svg == null)
				return;

			graphicsCache().setConnectionsImage(SVGUtil.createSVGImage(svg, boardDimensions.getWidth(), boardDimensions.getHeight()));
		}

		g2d.drawImage(graphicsCache().connectionsImage(), 0, 0, null);
	}
	
	//-----------------------------------------------------------------------------
	
	@Override
	public void postMoveUpdates(final Move move, final int moveNumber)
	{    	
		if (settingsPlayer().showAnimation() && !bridge().settingsVC().pieceBeingDragged())
		{
			MoveAnimation.saveMoveAnimationDetails(this, move);
			
			new java.util.Timer().schedule
			( 
		        new java.util.TimerTask() 
		        {
		            @Override
		            public void run() 
		            {
		            	postAnimationUpdates(move, moveNumber);
		            }
		        }, 
		        MoveAnimation.ANIMATION_WAIT_TIME 
			);
		}
		else
		{
			postAnimationUpdates(move, moveNumber);
		}
	}
	
	/**
	 * Called after any animations for the moves have finished.
	 */
	private void postAnimationUpdates(final Move move, final int moveNumber)
	{
		contextSnapshot().setContext(this);
		final Context context = contextSnapshot().getContext(this);
		
		GameUtil.gameOverTasks(this);
		
		if (!context.game().isSimulationMoveGame())
			MoveHandler.checkMoveWarnings(this);
		
		if (manager().aiSelected()[context.state().playerToAgent(move.mover())].ai() != null)
			playSound("Pling-KevanGC-1485374730");
		
		if (settingsPlayer().saveTrialAfterMove())
			saveTrial();
		
		MoveAnimation.resetAnimationValues(this);
		updateTabs(context);
    	repaint();
	}
	
	//-----------------------------------------------------------------------------
	
}
