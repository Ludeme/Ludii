package app.views.players;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import app.PlayerApp;
import app.utils.GUIUtil;
import app.utils.SVGUtil;
import app.utils.Spinner;
import app.views.View;
import game.Game;
import game.equipment.Equipment;
import game.equipment.container.Container;
import game.functions.ints.IntFunction;
import game.types.state.GameType;
import graphics.svg.SVGtoImage;
import manager.ai.AIUtil;
import metadata.graphics.util.ScoreDisplayInfo;
import metadata.graphics.util.WhenScoreType;
import metadata.graphics.util.colour.ColourRoutines;
import other.AI;
import other.context.Context;
import other.model.SimultaneousMove;

//-----------------------------------------------------------------------------

/**
 * Panel showing a specific player's status and details.
 *
 * @author Matthew.Stephenson and cambolbro and Eric.Piette
 */
public class PlayerViewUser extends View
{
	/** Player index: 1, 2, ... 0 is shared. */
	protected int playerId = 0;
	
	/** PlayerView object that generated this UserView. */
	public PlayerView playerView;
	
	/** Container associated with this view. */
	Container hand = null;
	
	/** Store a spinner for this player, to represent if an AI is thinking about a move for it. */
	public Spinner spinner = null;

	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 */
	public PlayerViewUser(final PlayerApp app, final Rectangle rect, final int pid, final PlayerView playerView)
	{
		super(app);
		this.playerView = playerView;
		playerId = pid;
		determineHand(app.contextSnapshot().getContext(app).equipment());
		placement = rect;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void paint(final Graphics2D g2d)
	{		
		final Context context = app.contextSnapshot().getContext(app);
		final int mover = context.state().mover();
		final ArrayList<Integer> winnerNumbers = getWinnerNumbers(context);
		
		int componentPushBufferX = 0;
		
		if (!app.settingsPlayer().usingExhibitionApp())
		{
			drawColourSwatch(g2d, mover, winnerNumbers, context);
			drawPlayerName(g2d, mover, winnerNumbers, context);
			drawAIFace(g2d);
			
			final int swatchWidth = app.playerSwatchList()[playerId].width;
			final int maxNameWidth = playerView.maximalPlayerNameWidth(context, g2d);
			componentPushBufferX = (int) (swatchWidth + maxNameWidth + app.playerNameList()[playerId].getHeight()*2);

			if (AIUtil.anyAIPlayer(app.manager()))
				componentPushBufferX += playerView.playerNameFont.getSize()*3;
		}

		if (hand != null)
		{
			final int containerMarginWidth = (int) (0.05 * placement.height);
			final Rectangle containerPlacement = new Rectangle(
																placement.x + componentPushBufferX + containerMarginWidth, 
																placement.y - placement.height/2, 
																placement.width - componentPushBufferX - containerMarginWidth*2, 
																placement.height
																);
			
			playerView.paintHand(g2d, context, containerPlacement, hand.index());
		}
		
		drawAISpinner(g2d, context);
		
		paintDebug(g2d, Color.RED);
	}

	//-------------------------------------------------------------------------

	/**
	 * Draw Swatch showing player number and colour.
	 */
	private void drawColourSwatch(final Graphics2D g2d, final int mover, final ArrayList<Integer> winnerNumbers, final Context context)
	{
		g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
		
		final int discR = (int)(0.275 * placement.height);
		final int cx = placement.x + discR;
		final int cy = placement.y + placement.height / 2;

		final Color fillColour = app.bridge().settingsColour().playerColour(context, playerId);
		final boolean fullColour =
				app.contextSnapshot().getContext(app).trial().over() && winnerNumbers.contains(Integer.valueOf(playerId))
				||
				!app.contextSnapshot().getContext(app).trial().over() && playerId == mover;

		final int fcr = fillColour.getRed();
		final int fcg = fillColour.getGreen();
		final int fcb = fillColour.getBlue();

		// Draw a coloured ring around the swatch if network game to represent player is online/offline
		if (app.manager().settingsNetwork().getActiveGameId() != 0)
		{
			Color markerColour = Color.RED;
			if (app.manager().settingsNetwork().onlinePlayers()[playerId] == true)
			{
				markerColour = Color.GREEN;
			}
			g2d.setColor(markerColour);
			g2d.fillArc(cx-discR-4, cy-discR-4, discR*2+8, discR*2+8, 0, 360);	
		}
		
		// Draw faint outline
		if (fullColour)
			g2d.setColor(new Color(63, 63, 63));
		else
			g2d.setColor(new Color(215, 215, 215));
		
		g2d.fillArc(cx-discR-2, cy-discR-2, discR*2+4, discR*2+4, 0, 360);
		
		if (app.playerSwatchHover()[playerId])
		{
			// Draw faded colour
			final int rr = fcr + (int) ((255 - fcr) * 0.5);
			final int gg = fcg + (int) ((255 - fcg) * 0.5);
			final int bb = fcb + (int) ((255 - fcb) * 0.5);
			g2d.setColor(new Color(rr, gg, bb));
		}
		else
		{
			if (fullColour)
			{
				g2d.setColor(fillColour);
			}
			else
			{
				// Draw faded colour
				final int rr = fcr + (int) ((255 - fcr) * 0.75);
				final int gg = fcg + (int) ((255 - fcg) * 0.75);
				final int bb = fcb + (int) ((255 - fcb) * 0.75);
				g2d.setColor(new Color(rr, gg, bb));
			}
		}
		g2d.fillArc(cx-discR, cy-discR, discR*2,  discR*2,  0,  360);

		if (app.playerSwatchHover()[playerId])
		{
			g2d.setColor(new Color(150, 150, 150));
		}
		else
		{
			if (playerId == mover || app.contextSnapshot().getContext(app).model() instanceof SimultaneousMove)
				g2d.setColor(new Color(50, 50, 50));
			else
				g2d.setColor(new Color(215, 215, 215));
		}
		
		// Draw the player number
		final Font oldFont = g2d.getFont();
		final Font indexFont = new Font("Arial", Font.BOLD, (int)(1.0 * discR));
		g2d.setFont(indexFont);
		final String str = "" + playerId;
		final Rectangle2D bounds = indexFont.getStringBounds(str, g2d.getFontRenderContext());

		final int tx = cx - (int)(0.5 * bounds.getWidth());
		final int ty = cy + (int)(0.3 * bounds.getHeight()) + 1;

		final Color contrastColour = ColourRoutines.getContrastColorFavourLight(fillColour);
		if (fullColour)
			g2d.setColor(contrastColour);
		else
			g2d.setColor(new Color(Math.max(contrastColour.getRed(), 215), Math.max(contrastColour.getGreen(), 215), Math.max(contrastColour.getBlue(), 215)));

		g2d.drawString(str, tx, ty);
		g2d.setFont(oldFont);

		// Indicate if player is no longer active in game
		final boolean gameOver = context.trial().over();
		if (!context.active(playerId) && !gameOver)
		{
			// Player not active -- strike through
			g2d.setColor(new Color(255, 255, 255));
			g2d.setStroke(new BasicStroke(7, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
			g2d.drawLine(cx-20, cy-20, cx+20, cy+20);
			g2d.drawLine(cx-20, cy+20, cx+20, cy-20);
		}
		
		app.playerSwatchList()[playerId] = new Rectangle(cx-discR, cy-discR, discR*2, discR*2);
	}

	//-------------------------------------------------------------------------

	/**
	 * Draws the player's name.
	 */
	private void drawPlayerName(final Graphics2D g2d, final int mover, final ArrayList<Integer> winnerNumbers, final Context context)
	{
		g2d.setFont(playerView.playerNameFont);
		
		final String stringNameAndExtras = getNameAndExtrasString(context, g2d);
		final Rectangle2D bounds = playerView.playerNameFont.getStringBounds(stringNameAndExtras, g2d.getFontRenderContext());	
		
		final Rectangle2D square = app.playerSwatchList()[playerId];
		final Point2D drawPosn = new Point2D.Double(square.getCenterX() + square.getWidth(), square.getCenterY());

		// Determine name and comboBox placement
		final int strNameY = (int) (drawPosn.getY() + bounds.getHeight()/3);
		final int strNameX = (int) drawPosn.getX();
		
		// Determine the colour of the player name
		if (!context.trial().over() || !winnerNumbers.contains(Integer.valueOf(playerId)))
		{
			if (app.playerNameHover()[playerId])
			{
				g2d.setColor(new Color(150, 150, 150));
			}
			else
			{
				if (playerId == mover || app.contextSnapshot().getContext(app).model() instanceof SimultaneousMove)
					g2d.setColor(new Color(50, 50, 50));
				else
					g2d.setColor(new Color(215, 215, 215));
			}
		}
		else
		{
			// Show winner
			g2d.setColor(Color.red);
		}
		
		final Rectangle NameAndExtrasBounds = bounds.getBounds();
		NameAndExtrasBounds.x = strNameX;
		NameAndExtrasBounds.y = (int) (strNameY - bounds.getHeight());
		
		app.playerNameList()[playerId] = NameAndExtrasBounds;
		
		g2d.drawString(stringNameAndExtras, strNameX, strNameY);
	}

	//-------------------------------------------------------------------------

	/**
	 * Draw AI face with expression showing positional estimate.
	 */
	void drawAIFace(final Graphics2D g2d)
	{
		final AI ai = app.manager().aiSelected()[app.manager().playerToAgent(playerId)].ai();

		if (ai != null)
		{
			final double happinessValue = ai.estimateValue();
			
			String imagePath = "/svg/faces/symbola_cool.svg";
			if (happinessValue < -0.8)
				imagePath = "/svg/faces/symbola_sad.svg";
			else if (happinessValue < -0.5)
				imagePath = "/svg/faces/symbola_scared.svg";
			else if (happinessValue < -0.2)
				imagePath = "/svg/faces/symbola_worried.svg";
			else if (happinessValue < 0.2)
				imagePath = "/svg/faces/symbola_neutral.svg";
			else if (happinessValue < 0.5)
				imagePath = "/svg/faces/symbola_pleased.svg";
			else if (happinessValue < 0.8)
				imagePath = "/svg/faces/symbola_happy.svg";

			try (final BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(imagePath))))
			{
				final Rectangle2D nameRect = app.playerNameList()[playerId];
				final double r = playerView.playerNameFont.getSize();
				final SVGGraphics2D svg = new SVGGraphics2D((int)r, (int) r);
				SVGtoImage.loadFromReader(svg, reader, new Rectangle2D.Double(0,0,r,r), Color.BLACK, Color.WHITE, 0);
				final Point2D drawPosn = new Point2D.Double(nameRect.getX() + nameRect.getWidth() + g2d.getFont().getSize()/5,  nameRect.getCenterY() - g2d.getFont().getSize()/5);
				g2d.drawImage(SVGUtil.createSVGImage(svg.getSVGDocument(), (int) r, (int) r), (int) drawPosn.getX(), (int) drawPosn.getY(), null);
				reader.close();
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Start the spinner for each AI that is thinking/moving.
	 */
	private void drawAISpinner(final Graphics2D g2d, final Context context)
	{
		if (app.manager().isWebApp())
			return;
		
		if (app.settingsPlayer().usingExhibitionApp())
		{
			if (spinner == null)
				spinner = new Spinner(new Rectangle2D.Double(850,290,200,200));
			spinner.setDotRadius(5);
		}
		else
		{	
			final Rectangle2D nameRect = app.playerNameList()[playerId];
			final double r = playerView.playerNameFont.getSize();
			final Point2D drawPosn = new Point2D.Double(nameRect.getX() + nameRect.getWidth() + r + 15,  nameRect.getCenterY() - 3);
			
			if (spinner == null || drawPosn.getX() != spinner.originalRect().getX())
				spinner = new Spinner(new Rectangle2D.Double(drawPosn.getX(),drawPosn.getY(), r, r));
		}

		if (spinner != null)
		{
			if (context.state().mover() == playerId && !app.manager().aiSelected()[app.manager().playerToAgent(playerId)].menuItemName().equals("Human") && app.manager().liveAIs() != null && !app.manager().liveAIs().isEmpty())
				spinner.startSpinner();
			else
				spinner.stopSpinner();
		}
		
		spinner.drawSpinner(g2d);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Check if the mouse position is over any items that have hover colours
	 */
	@Override
	public void mouseOverAt(final Point pixel)
	{
		// Check if mouse is over player swatch.
		for (int i = 0; i < app.playerSwatchList().length; i++)
		{
			final Rectangle rectangle = app.playerSwatchList()[i];
			final boolean overlap = GUIUtil.pointOverlapsRectangle(pixel, rectangle);
			
			if (app.playerSwatchHover()[i] != overlap)
			{
				app.playerSwatchHover()[i] = overlap;
				app.repaint(rectangle.getBounds());
			}
		}

		// Check if mouse is over player name.
		for (int i = 0; i < app.playerNameList().length; i++)
		{
			final Rectangle rectangle = app.playerNameList()[i];
			final boolean overlap = GUIUtil.pointOverlapsRectangle(pixel, rectangle);
			
			if (app.playerNameHover()[i] != overlap)
			{
				app.playerNameHover()[i] = overlap;
				app.repaint(rectangle.getBounds());
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Gets the complete string to be printed for this player, including name, score, algorithm, etc.
	 */
	public String getNameAndExtrasString(final Context context, final Graphics2D g2d)
	{
		final Context instanceContext = context.currentInstanceContext();
		final Game instance = instanceContext.game();
		
		final int playerIndex = app.manager().playerToAgent(playerId);
		final Font playerNameFont = g2d.getFont();
		
		String strName = app.manager().aiSelected()[playerIndex].name();
		
		// if Metadata overrides this, then include this metadata name.
		final String metadataName = context.game().metadata().graphics().playerName(context, playerIndex);
		if (metadataName != null)
			strName += " (" + metadataName + ")";

		// Assemble the extra details string with scores etc.
		String strExtras = "";
		String strAIName = "";
		
		if (app.manager().aiSelected()[playerIndex].ai() != null)
			strAIName += " (" + app.manager().aiSelected()[playerIndex].ai().friendlyName() + ") ";
		
		// Score
		final ScoreDisplayInfo scoreDisplayInfo = instance.metadata().graphics().scoreDisplayInfo(instanceContext, playerId);
		if (scoreDisplayInfo.scoreReplacement() != null)
		{
			if 
			(
				scoreDisplayInfo.showScore() == WhenScoreType.Always || 
				(scoreDisplayInfo.showScore() == WhenScoreType.AtEnd && instanceContext.trial().over())
			)
			{
				final IntFunction replacementScoreFunction = scoreDisplayInfo.scoreReplacement();
				replacementScoreFunction.preprocess(instance);
				final int replacementScore = replacementScoreFunction.eval(instanceContext);
				strExtras += " (" + replacementScore;
			}
		}
		else if ((instance.gameFlags() & GameType.Score) != 0L)
		{
			if 
			(
				scoreDisplayInfo.showScore() == WhenScoreType.Always || 
				(scoreDisplayInfo.showScore() == WhenScoreType.AtEnd && instanceContext.trial().over())
			)
			{
				strExtras += " (" + instanceContext.score(playerId);
			}
		}
		strExtras += scoreDisplayInfo.scoreSuffix();
		
		if (context.isAMatch())
		{
			if (strExtras.equals(""))
				strExtras += " (";
			else
				strExtras += " : ";

			strExtras += context.score(playerId);
		}
		
		if (!strExtras.equals(""))
			strExtras += ")";

		if (app.contextSnapshot().getContext(app).game().requiresBet())
			strExtras += " $" + context.state().amount(playerId);

		if (app.contextSnapshot().getContext(app).game().requiresTeams())
			strExtras += " Team " + app.contextSnapshot().getContext(app).state().getTeam(playerId);
		
		if (app.manager().settingsNetwork().playerTimeRemaining()[app.manager().playerToAgent(playerId)-1] > 0)
			strExtras += " Time: " + app.manager().settingsNetwork().playerTimeRemaining()[app.contextSnapshot().getContext(app).state().playerToAgent(playerId)-1] + "s";
		
		strName += strAIName;
		
		// cut string off at a specified pixel width
		final int maxLengthPixels = g2d.getFont().getSize() > 20 ? 250 : 200;			// More pixels allocated if font is large (i.e. Mobile)
		String shortendedString = "";
		for (int i = 0; i < strName.length(); i++)
		{
			shortendedString += strName.charAt(i);
			final int stringWidth = (int) playerNameFont.getStringBounds(shortendedString, g2d.getFontRenderContext()).getWidth();
			if (stringWidth > maxLengthPixels)
			{
				shortendedString = shortendedString.substring(0, i-2);
				shortendedString += "...";
				strName = shortendedString;
				break;
			}
		}
		
		return strName + strExtras;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Returns all the players who are winners, can be multiple if a team game.
	 */
	private static ArrayList<Integer> getWinnerNumbers(final Context context)
	{
		final Game game = context.game();
		final ArrayList<Integer> winnerNumbers = new ArrayList<>();
		final int firstWinner = (context.trial().status() == null) ? 0 : context.trial().status().winner();
		if (game.requiresTeams())
		{
			final int winningTeam = context.state().getTeam(firstWinner);
			for (int i = 1; i < game.players().size(); i++)
				if (context.state().getTeam(i) == winningTeam)
					winnerNumbers.add(Integer.valueOf(i));
		}
		else
		{
			winnerNumbers.add(Integer.valueOf(firstWinner));
		}
		return winnerNumbers;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Determine the hand container associated with this view.
	 */
	private void determineHand(final Equipment equipment)
	{
		for (int i = 0; i < equipment.containers().length; i++)
			if (equipment.containers()[i].isHand())
				if ((equipment.containers()[i]).owner() == playerId)
					hand = equipment.containers()[i];
	}

	//-------------------------------------------------------------------------
	
	@Override
	public int containerIndex()
	{
		if (hand == null)
			return -1;
		
		return hand.index();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Player index.
	 */
	public int playerId()
	{
		return playerId;
	}

}
