package app.display.views;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JTextArea;

import app.DesktopApp;
import app.PlayerApp;
import app.display.MainWindowDesktop;
import app.move.MoveVisuals;
import app.move.animation.MoveAnimation;
import app.utils.BufferedImageUtil;
import app.utils.DrawnImageInfo;
import app.views.View;
import app.views.tools.ToolView;
import game.equipment.container.Container;
import graphics.ImageProcessing;
import main.Constants;
import other.context.Context;
import other.location.FullLocation;
import other.location.Location;

//-----------------------------------------------------------------------------

/**
 * Panel that covers the entire DesktopApp.view(). Used to draw aspects that cover several other Views
 *
 * @author Matthew.Stephenson and cambolbro
 */
public final class OverlayView extends View
{
	/** Font for displaying values. */
	protected Font fontForDisplay;
	
	final JTextArea englishDescriptionField = new JTextArea();

	//------------------------------------------------------------------------

	/**
	 * Constructor.
	 */
	public OverlayView(final PlayerApp app)
	{
		super(app);
		DesktopApp.frame().add(englishDescriptionField);
	}

	//-------------------------------------------------------------------------

	@Override
	public void paint(final Graphics2D g2d)
	{		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		
		final ToolView toolview = DesktopApp.view().toolPanel();
		
		final Rectangle passRect = toolview.buttons.get(ToolView.PASS_BUTTON_INDEX).rect();
		
		Rectangle otherRect = new Rectangle(0,0);
		if (toolview.buttons.get(ToolView.OTHER_BUTTON_INDEX) != null)
			otherRect = toolview.buttons.get(ToolView.OTHER_BUTTON_INDEX).rect();
		
		final Context context = app.contextSnapshot().getContext(app);
			
		if (!app.settingsPlayer().isPerformingTutorialVisualisation() && !app.settingsPlayer().usingExhibitionApp())
			drawLoginDisc(app, g2d);

		// Draw unique section text for exhibition app.
		if (app.settingsPlayer().usingExhibitionApp())
		{
			final Font exhbitionTitleFont = new Font("Cantarell", Font.BOLD, 52);
			g2d.setFont(exhbitionTitleFont);
			g2d.setColor(Color.BLUE);
			g2d.drawString("Make Your Own Game", 40, 75);
			
			final Font exhbitionLabelFont = new Font("Cantarell", Font.PLAIN, 24);
			g2d.setFont(exhbitionLabelFont);
			if (app.manager().ref().context().equipment().containers().length > 3)
			{
				g2d.drawString("1. Choose a board", 30, 150);
				g2d.drawString("2. Choose pieces (drag onto play area)", 30, 298);
				g2d.drawString("Movement", 30, 455);
				g2d.drawString("  Direction", 30, 503);
				g2d.drawString("   Capture", 30, 551);
				g2d.drawString("3. Choose goal(s)", 30, 600);
			}
			else
			{
				// If playing a game, show toEnglish of that game's description
				final Font exhbitionDescriptionFont = new Font("Cantarell", Font.PLAIN, 20);
				englishDescriptionField.setFont(exhbitionDescriptionFont);
				englishDescriptionField.setBounds(30, 100, 600, 800);
				englishDescriptionField.setOpaque(false);
				englishDescriptionField.setLineWrap(true);
				englishDescriptionField.setWrapStyleWord(true);
				englishDescriptionField.setText(app.contextSnapshot().getContext(app).game().toEnglish(app.contextSnapshot().getContext(app).game()));
				englishDescriptionField.setVisible(true);
			}
		}
		
		if (app.bridge().settingsVC().thisFrameIsAnimated())
		{
			MoveAnimation.moveAnimation(app, g2d);
		}
		else
		{
			calculateFont();
			
			if (app.manager().liveAIs() != null && !app.manager().liveAIs().isEmpty() && app.settingsPlayer().showAIDistribution())
				MoveVisuals.drawAIDistribution(app, g2d, context, passRect, otherRect);
				
			if (app.settingsPlayer().showLastMove() && context.currentInstanceContext().trial().numMoves() > context.currentInstanceContext().trial().numInitialPlacementMoves())
				MoveVisuals.drawLastMove(app, g2d, context, passRect, otherRect);
			
			if (app.settingsPlayer().isPerformingTutorialVisualisation())
				MoveVisuals.drawTutorialVisualisatonArrows(app, g2d, context, passRect, otherRect);
			
			if (app.manager().settingsManager().showRepetitions())
				MoveVisuals.drawRepeatedStateMove(app, g2d, context, passRect, otherRect);
			
			if 
			(
				!app.bridge().settingsVC().selectedFromLocation().equals(new FullLocation(Constants.UNDEFINED))
				&& 
				app.bridge().settingsVC().pieceBeingDragged()
				&& 
				DesktopApp.view().getMousePosition() != null
			)
				drawDraggedPiece(g2d, app.bridge().settingsVC().selectedFromLocation(), DesktopApp.view().getMousePosition().x, DesktopApp.view().getMousePosition().y);
		}

		drawSandBoxIcon(g2d);
		drawExtraGameInformation(g2d, context);
		
		paintDebug(g2d, Color.BLACK);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Draw the Sandbox icon on the top left of the DesktopApp.view().
	 */
	private void drawSandBoxIcon(final Graphics2D g2d)
	{
		if (app.settingsPlayer().sandboxMode())
		{
			final URL resource = this.getClass().getResource("/sandbox.png");
			try
			{
				BufferedImage sandboxImage = ImageIO.read(resource);
				sandboxImage = BufferedImageUtil.resize(sandboxImage, placement.height/15, placement.height/15);
				g2d.drawImage(sandboxImage, sandboxImage.getWidth()/10, sandboxImage.getHeight()/10, null);
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Show ball in top right to indicate whether app is connected to the network (green) or not (red).
	 */
	protected static void drawLoginDisc(final PlayerApp app, final Graphics2D g2d)
	{
		final int r = 7;
		final Color markerColour = (app.manager().settingsNetwork().getLoginId() == 0) ? Color.RED : Color.GREEN;
		ImageProcessing.ballImage(g2d, DesktopApp.view().getWidth()-r*2-r, r, r, markerColour);	
	}

	//-------------------------------------------------------------------------

	/**
	 * Calculate the font size to display values at. 
	 */
	private void calculateFont()
	{
		int maxVertices = 0;
		int maxEdges = 0;
		int maxFaces = 0;
		
		final Context context = app.contextSnapshot().getContext(app);
		
		for (int i = 0; i < context.numContainers(); i++)
		{
			final Container container = context.equipment().containers()[i];

			maxVertices += container.topology().cells().size();
			maxEdges += container.topology().edges().size();
			maxFaces += container.topology().vertices().size();
		}
		
		final int maxDisplayNumber = Math.max(maxVertices, Math.max(maxEdges, maxFaces));
		
		final int fontMultiplier  = (int) (app.bridge().getContainerStyle(context.board().index()).cellRadius() * 2 * DesktopApp.view().getBoardPanel().boardSize());

		
		int fontSize = (fontMultiplier);
		if (maxDisplayNumber > 9)
			fontSize = fontMultiplier/2;
		if (maxDisplayNumber > 99)
			fontSize = fontMultiplier/3;
		if (maxDisplayNumber > 999)
			fontSize = fontMultiplier/4;

		fontForDisplay = new Font("Arial", Font.BOLD, fontSize);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Draw extra important information about the current game context (e.g. pot).
	 */
	private void drawExtraGameInformation(final Graphics2D g2d, final Context context)
	{
		// Skip extra game information in certain circumstances.
		if (app.settingsPlayer().isPerformingTutorialVisualisation())
			return;
		
		// temporary message
		if (MainWindowDesktop.volatileMessage().length() > 0)
		{
			drawStringBelowBoard(g2d, MainWindowDesktop.volatileMessage(), 0.98);
		}
		else if (DesktopApp.view().temporaryMessage().length() > 0)
		{
			drawStringBelowBoard(g2d, DesktopApp.view().temporaryMessage(), 0.98);
		}
		
		// shared pot
		if (context.game().requiresBet())
		{
			final String str = "Pot: $" + context.state().pot();
			drawStringBelowBoard(g2d, str, 0.95);
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Draw a specified string message below the board.
	 */
	private void drawStringBelowBoard(final Graphics2D g2d, final String message, final double percentageBelow)
	{
		final int pixels = DesktopApp.view().getBoardPanel().placement().width;
		final Font font = new Font("Arial", Font.PLAIN, 16);
		g2d.setFont(font);
		g2d.setColor(Color.BLACK);
		final Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(message, g2d);
		g2d.drawString(message, (int)(0.5 * pixels - bounds.getWidth()/2.0), (int)(percentageBelow * pixels + placement.y * 2));
	}

	//-------------------------------------------------------------------------

	/**
	 * Draw the piece being dragged or animated.
	 */
	public void drawDraggedPiece(final Graphics2D g2d, final Location selectedLocation, final int x, final int y)
	{	
		for (final DrawnImageInfo image : MoveAnimation.getMovingPieceImages(app, null, selectedLocation, x, y, false))
			g2d.drawImage(image.pieceImage(), (int)image.imageInfo().drawPosn().getX(), (int)image.imageInfo().drawPosn().getY(), null);
	}
    
	//-------------------------------------------------------------------------

}
