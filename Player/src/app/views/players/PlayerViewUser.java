package app.views.players;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import app.PlayerApp;
import app.utils.GUIUtil;
import app.views.View;
import game.Game;
import game.equipment.Equipment;
import game.equipment.container.Container;
import game.functions.ints.IntFunction;
import game.types.state.GameType;
import metadata.graphics.util.ScoreDisplayInfo;
import metadata.graphics.util.WhenScoreType;
import metadata.graphics.util.colour.ColourRoutines;
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

	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 *
	 * @param rect
	 * @param pid
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
		
		drawColourSwatch(g2d, mover, winnerNumbers, context);
		drawPlayerName(g2d, mover, winnerNumbers, context);
		//drawAIFace(g2d);

		int componentPushBufferX = 0;
		final int swatchWidth = app.playerSwatchList()[playerId].width;
		final int maxNameWidth = playerView.maximalPlayerNameWidth(context, g2d);
		componentPushBufferX = swatchWidth + maxNameWidth + placement.height/2;
		
		if (hand != null)
		{
			final int containerMarginWidth = (int) (0.05 * placement.height); // add a small 5% margin on either side of the container
			final Rectangle containerPlacement = new Rectangle(
																placement.x + componentPushBufferX + containerMarginWidth, 
																placement.y - placement.height/2, 
																placement.width - componentPushBufferX - containerMarginWidth*2, 
																placement.height
																);
			
			playerView.paintHand(g2d, context, containerPlacement, hand.index());	
		}
		
		paintDebug(g2d, Color.RED);
	}

	//-------------------------------------------------------------------------

	/**
	 * Draw Swatch showing player number and colour.
	 * Returns a rectangle representing the bounding box of the Swatch
	 * @param g2d
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
	 * @param g2d
	 * @param mover
	 * @param winnerNumbers
	 * @param context
	 */
	private void drawPlayerName(final Graphics2D g2d, final int mover, final ArrayList<Integer> winnerNumbers, final Context context)
	{
		g2d.setFont(PlayerView.playerNameFont);
		final String stringNameAndExtras = getNameAndExtrasString(context, g2d);
		final Rectangle2D bounds = PlayerView.playerNameFont.getStringBounds(stringNameAndExtras, g2d.getFontRenderContext());	
		
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

//	/**
//	 * Draws the ComboBox for selecting the Agent algorithm.
//	 * @param g2d
//	 * @param context
//	 * @param associatedAI
//	 */
//	private void drawPlayerComboBox(final Graphics2D g2d, final Context context, final AIDetails associatedAI)
//	{
//		// Initialise the comboBox, if it doesn't exist.
//		if (myComboBox == null)
//		{
//			final String[] comboBoxContents = GUIUtil.getAiStrings(true).toArray(new String[GUIUtil.getAiStrings(true).size()]);
//			myComboBox = new JComboBox<String>(comboBoxContents); 
//			DesktopApp.frame().setLayout(null);  
//			DesktopApp.frame().add(myComboBox);  
//			myComboBox.addActionListener(new ActionListener()
//			{
//				@Override
//				public void actionPerformed(final ActionEvent e)
//				{
//					final int newPlayerIndex = ContextSnapshot.getContext(app).state().playerToAgent(playerId);
//					
//					if (!ignoreAIComboBoxEvents)
//					{
//						final JSONObject json = new JSONObject().put("AI",
//							new JSONObject()
//							.put("algorithm", myComboBox.getSelectedItem().toString())
//							);
//						
//						AIUtil.updateSelectedAI(json, newPlayerIndex, AIMenuName.getAIMenuName(myComboBox.getSelectedItem().toString()));
//					}
//					
//					// need to initialise the AI if "Ludii AI" selected, so we can get the algorithm name.
//					if (myComboBox.getSelectedItem().toString().equals("Ludii AI"))
//						DesktopApp.aiSelected()[newPlayerIndex].ai().initIfNeeded(ContextSnapshot.getContext(app).game(), newPlayerIndex);
//					
//					app.manager().settingsNetwork().backupAiPlayers();
//				}
//			});
//		}
//		
//		// Can only set AI if not online.
//		if (app.manager().settingsNetwork().getActiveGameId() != 0 && (!app.manager().settingsNetwork().getOnlineAIAllowed() || playerId != app.manager().settingsNetwork().getNetworkPlayerNumber()))
//		{
//			if (myComboBox.isVisible())
//				myComboBox.setVisible(false);
//		}
//		else
//		{
//			if (!myComboBox.isVisible())
//				myComboBox.setVisible(true);
//		}
//		
//		// Make sure combobox has correct entry selected.
//		if (!myComboBox.getSelectedItem().equals(associatedAI.menuItemName().label))
//		{
//			ignoreAIComboBoxEvents = true;
//			myComboBox.setSelectedItem(associatedAI.menuItemName().label);
//			ignoreAIComboBoxEvents = false;
//		}
//
//		// Set position, but don't bother if invisible.
//		if (myComboBox.isVisible())
//		{
//			final int width = 120; 
//			final int height = 20;
//			final Rectangle2D bounds = PlayerView.playerNameFont.getStringBounds("A DUMMY STRING FOR HEIGHT PURPOSES", g2d.getFontRenderContext());
//			final Rectangle2D square = Desktopapp.playerSwatchList[playerId];
//			final Point2D drawPosn = new Point2D.Double(square.getCenterX() + square.getWidth(), square.getCenterY());
//			int strAINameY, strAINameX = 0;
//			if (context.game().players().count() > numberPlayersForReducedHeightFormat)
//			{
//				strAINameY = (int) drawPosn.getY();
//				strAINameX = (int) drawPosn.getX();
//			}
//			else
//			{
//				strAINameY = (int) (drawPosn.getY() + bounds.getHeight()/3) - 1;
//				strAINameX = (int) drawPosn.getX();
//			}
//			if (context.game().players().count() > numberPlayersForReducedHeightFormat)
//			{
//				myComboBox.setBounds
//				(
//					strAINameX + playerView.maximalPlayerNameWidth(context, g2d) + 10, 
//					strAINameY - height/2, 
//					width, height
//				);
//			}
//			else
//			{
//				final int macAIOffX = GUIUtil.isMac() ? -5 : 0;
//				final int macAIOffY = GUIUtil.isMac() ?  6 : 0;
//				
//				myComboBox.setBounds
//				(
//					strAINameX + macAIOffX, 
//					strAINameY - height/2 + macAIOffY, 
//					width, height
//				);
//			}
//		}
//		
//		// Initialise/draw spinner for this player.
//		final int discR = 8;
//		final int discX = myComboBox.getX() + myComboBox.getWidth()/2 - 4;
//		final int discY = myComboBox.getY() + myComboBox.getHeight()/2 + 4;
//		if (MainWindow.spinners[playerId] == null)
//			MainWindow.spinners[playerId] = new Spinner(Desktopapp, new Rectangle(discX-discR, discY+discR+5, discR*2,  discR*2));
//		MainWindow.spinners[playerId].drawSpinner(g2d);
//	}
	
	//-------------------------------------------------------------------------
	
//	/**
//	 * Draws the combobox for the agent's think time.
//	 * @param g2d
//	 * @param context
//	 * @param associatedAI
//	 */
//	private void drawThinkTimeComboBox(final Graphics2D g2d, final Context context, final AIDetails associatedAI)
//	{
//		// Initialise the comboBox, if it doesn't exist.
//		if (myComboBoxThinkTime == null)
//		{	
//			final String[] comboBoxContentsThinkTime = {"1s", "2s", "3s", "5s", "10s", "30s", "60s", "120s", "180s", "240s", "300s"};
//			myComboBoxThinkTime = new JComboBox<String>(comboBoxContentsThinkTime); 
//			myComboBoxThinkTime.setEditable(true);	
//			DesktopApp.frame().setLayout(null);  
//			DesktopApp.frame().add(myComboBoxThinkTime);   
//			
//			myComboBoxThinkTime.addActionListener(new ActionListener()
//			{
//				@Override
//				public void actionPerformed(final ActionEvent e)
//				{
//					final int newPlayerIndex = ContextSnapshot.getContext(app).state().playerToAgent(playerId);
//					
//					String comboBoxThinkTime = myComboBoxThinkTime.getSelectedItem().toString();
//					if (comboBoxThinkTime.toLowerCase().charAt(comboBoxThinkTime.length()-1) == 's')
//						comboBoxThinkTime = comboBoxThinkTime.substring(0, comboBoxThinkTime.length()-1);
//
//					double thinkTime = 0.0;
//					try
//					{
//						thinkTime = Double.valueOf(comboBoxThinkTime.toString()).doubleValue();
//					}
//					catch (final Exception e2)
//					{
//						System.out.println("Invalid think time: " + comboBoxThinkTime.toString());
//					}
//					
//					if (thinkTime <= 0)
//					{
//						thinkTime = 1;
//						myComboBoxThinkTime.setSelectedIndex(0);
//					}
//					
//					DesktopApp.aiSelected()[newPlayerIndex].setThinkTime(thinkTime);
//					app.manager().settingsNetwork().backupAiPlayers();
//				}
//			});
//		}
//		
//		// Can only set think time if AI is selected.
//		if (associatedAI.ai() == null)
//		{
//			if (myComboBoxThinkTime.isVisible())
//				myComboBoxThinkTime.setVisible(false);
//		}
//		else
//		{
//			if (!myComboBoxThinkTime.isVisible())
//				myComboBoxThinkTime.setVisible(true);
//		}
//		
//		// Make sure combobox has correct entry selected.
//		final DecimalFormat format = new DecimalFormat("0.#");
//		myComboBoxThinkTime.setSelectedItem(String.valueOf(format.format(associatedAI.thinkTime())) + "s");
//		
//		// Set position, but don't bother if invisible.
//		if (myComboBoxThinkTime.isVisible())
//		{
//			final int width = 70; 
//			final int height = 20;
//			myComboBoxThinkTime.setBounds(myComboBox.getX() + myComboBox.getWidth() + 10, myComboBox.getY(), width, height);
//		}
//	}

	//-------------------------------------------------------------------------

//	/**
//	 * Draw AI face with expression showing positional estimate.
//	 * @param g2d
//	 */
//	void drawAIFace(final Graphics2D g2d)
//	{
//		final Point2D drawPosn = new Point2D.Double(myComboBox.getX() + myComboBox.getWidth() + myComboBoxThinkTime.getWidth() + 16,  myComboBox.getY()+2);
//		
//		final double r = PlayerView.playerNameFont.getSize();
//
//		final AI ai = DesktopApp.aiSelected()[ContextSnapshot.getContext(app).state().playerToAgent(playerId)].ai();
//		InputStream in = null;
//		final Color faceColor = Color.WHITE;
//
//		if (ai != null)
//		{
//			final double happinessValue = ai.estimateValue();
//			
//			if (happinessValue < -0.8)
//				in = getClass().getResourceAsStream("/svg/faces/symbola_sad.svg");
//			else if (happinessValue < -0.5)
//				in = getClass().getResourceAsStream("/svg/faces/symbola_scared.svg");
//			else if (happinessValue < -0.2)
//				in = getClass().getResourceAsStream("/svg/faces/symbola_worried.svg");
//			else if (happinessValue < 0.2)
//				in = getClass().getResourceAsStream("/svg/faces/symbola_neutral.svg");
//			else if (happinessValue < 0.5)
//				in = getClass().getResourceAsStream("/svg/faces/symbola_pleased.svg");
//			else if (happinessValue < 0.8)
//				in = getClass().getResourceAsStream("/svg/faces/symbola_happy.svg");
//			else
//				in = getClass().getResourceAsStream("/svg/faces/symbola_cool.svg");
//
//			try (final BufferedReader reader = new BufferedReader(new InputStreamReader(in)))
//			{
//				final SVGGraphics2D svg = new SVGGraphics2D((int)r, (int) r);
//				SVGtoImage.loadFromReader(svg, reader, (int) r, (int) r, 0, 0, Color.BLACK, faceColor, true, 0);
//				g2d.drawImage(SVGUtil.createSVGImage(svg.getSVGDocument(), (int) r, (int) r), (int) drawPosn.getX(), (int) drawPosn.getY(), null);
//			}
//			catch (final IOException e)
//			{
//				e.printStackTrace();
//			}
//		}
//	}
	
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
		
		final int playerIndex = instanceContext.state().playerToAgent(playerId);
		final Font playerNameFont = g2d.getFont();
		
		String strName = app.manager().aiSelected()[playerIndex].name();
		
		// if Metadata overrides this, then include this metadata name.
		final String metadataName = context.game().metadata().graphics().playerName(playerIndex, context);
		if (metadataName != null)
			strName += " (" + metadataName + ")";

		// Assemble the extra details string with scores etc.
		String strExtras = "";
		String strAIName = "";
		
		if (app.manager().aiSelected()[playerIndex].ai() != null)
			strAIName += " (" + app.manager().aiSelected()[playerIndex].ai().friendlyName + ") ";
		
		//if (DesktopApp.aiSelected()[playerIndex].menuItemName().label.equals("From JAR"))
		//	strAIName += " (" + DesktopApp.aiSelected()[playerIndex].ai().friendlyName + ")";
		
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
		
		if (app.manager().settingsNetwork().playerTimeRemaining()[app.contextSnapshot().getContext(app).state().playerToAgent(playerId)-1] > 0)
			strExtras += " Time: " + app.manager().settingsNetwork().playerTimeRemaining()[app.contextSnapshot().getContext(app).state().playerToAgent(playerId)-1] + "s";
		
		strExtras = strAIName + strExtras;
		
		// cut string off at a specified pixel width
		final int maxLengthPixels = 150;
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
		
		return strName + " " + strExtras;
	}
	
	
	//-------------------------------------------------------------------------

	/**
	 * Returns all the players who are winners, can be multiple if a team game.
	 * @param context
	 * @return
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
	 * @param equipment
	 */
	private void determineHand(final Equipment equipment)
	{
		for (int i = 0; i < equipment.containers().length; i++)
			if (equipment.containers()[i].isHand())
				if ((equipment.containers()[i]).owner() == playerId)
					hand = equipment.containers()[i];
	}
	
	
	//-------------------------------------------------------------------------

//	/**
//	 * Calculates the combined with of all AI interface aspects (combo-boxes and AI expression)
//	 * @return
//	 */
//	private int aiInterfaceWidth(final Context context)
//	{
//		// Rough approximations.
//		final int agentComboBoxWidth = 150;
//		final int thinkTimeComboboxWidth = 70;
//		final int aiExpressionWidth = 30;
//		if (playerView.anyPlayersAreAgents(context))
//			return agentComboBoxWidth + thinkTimeComboboxWidth + aiExpressionWidth;
//		
//		return agentComboBoxWidth;
//	}

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
	
	//-------------------------------------------------------------------------

}
