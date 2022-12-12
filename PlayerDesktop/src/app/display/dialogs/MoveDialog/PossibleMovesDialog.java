package app.display.dialogs.MoveDialog;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JButton;

import org.jfree.graphics2d.svg.SVGGraphics2D;

import app.DesktopApp;
import app.PlayerApp;
import app.display.dialogs.util.DialogUtil;
import app.move.MoveHandler;
import app.move.MoveUtil;
import app.utils.BufferedImageUtil;
import app.utils.SVGUtil;
import app.utils.SettingsExhibition;
import game.equipment.component.Component;
import graphics.ImageUtil;
import graphics.svg.SVGtoImage;
import main.collections.FastArrayList;
import other.action.Action;
import other.action.ActionType;
import other.action.cards.ActionSetTrumpSuit;
import other.action.move.move.ActionMove;
import other.action.others.ActionPropose;
import other.action.others.ActionVote;
import other.action.state.ActionBet;
import other.action.state.ActionSetNextPlayer;
import other.action.state.ActionSetRotation;
import other.context.Context;
import other.move.Move;
import other.state.container.ContainerState;
import util.ContainerUtil;
import util.HiddenUtil;
import view.component.ComponentStyle;

/**
 * Dialog for showing different special moves that cannot be displayed on the board directly.
 * 
 * @author Matthew.Stephenson
 */
public class PossibleMovesDialog extends MoveDialog
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	/**
	 * Show the Dialog.
	 */
	public static void createAndShowGUI(final PlayerApp app, final Context context, final FastArrayList<Move> validMoves, final boolean centerOnBoard)
	{
		if (SettingsExhibition.exhibitionVersion)
		{
			app.manager().ref().applyHumanMoveToGame(app.manager(), validMoves.get(0));
			return;
		}
		
		try
		{
			final PossibleMovesDialog dialog = new PossibleMovesDialog(app, context, validMoves);
			
			Point drawPosn = new Point(MouseInfo.getPointerInfo().getLocation().x - dialog.getWidth() / 2, MouseInfo.getPointerInfo().getLocation().y - dialog.getHeight() / 2);
			if (centerOnBoard)
				drawPosn = new Point(
						(int)(DesktopApp.frame().getX() + DesktopApp.view().getBoardPanel().placement().getCenterX() - dialog.getWidth() / 2), 
						(int)(DesktopApp.frame().getY() + DesktopApp.view().getBoardPanel().placement().getCenterY() - dialog.getHeight() / 2) + menuBarHeight);
			
			DialogUtil.initialiseForcedDialog(dialog, "Possible Moves", new Rectangle(drawPosn));
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Create the dialog.
	 */
	public PossibleMovesDialog(final PlayerApp app, final Context context, final FastArrayList<Move> validMoves)
	{
		setDialogLayout(app, context, validMoves.size());
		
		for (final Move m : validMoves)
		{	
			boolean moveShown = false;
			
			// Check for displaying special actions
			for (final Action a : m.actions())
			{		
				final int fromContainerIndex = ContainerUtil.getContainerId(context, m.from(), m.fromType());
				
				// Rotation move
				if (a instanceof ActionSetRotation)
				{			
					final int componentValue = app.contextSnapshot().getContext(app).containerState(fromContainerIndex).what(m.from(), m.fromType());
					if (componentValue != 0)
					{
						final Component c = context.components()[componentValue];
						final BufferedImage componentImage = app.graphicsCache().getComponentImage(app.bridge(), fromContainerIndex, c, c.owner(), 0, 0, m.from(), 0, m.fromType(), imageSize, app.contextSnapshot().getContext(app), 0, a.rotation(), true);
						final JButton button = AddButton(app, m, componentImage, "");
						setDialogSize(button, columnNumber, rowNumber, buttonBorderSize);
						
						moveShown = true;
						break;
					}
				}
				
				// Adding/Promoting a component
				else if (a.actionType() == ActionType.Add || a.actionType() == ActionType.Promote)
				{
					final ContainerState cs = app.contextSnapshot().getContext(app).containerState(fromContainerIndex);
					final int hiddenValue = HiddenUtil.siteHiddenBitsetInteger(context, cs, a.levelFrom(), a.levelFrom(), a.who(), a.fromType());
					final int componentWhat = a.what();
					final int componentValue = a.value();
					final int componentState = a.state();
					final ComponentStyle componentStyle = app.bridge().getComponentStyle(componentWhat);
					componentStyle.renderImageSVG(context, fromContainerIndex, imageSize, componentState, componentValue, true, hiddenValue, a.rotation());
					final SVGGraphics2D svg = componentStyle.getImageSVG(componentState);
					BufferedImage componentImage = null;
					if (svg != null)
						componentImage = SVGUtil.createSVGImage(svg.getSVGDocument(),imageSize,imageSize);	
					
					final JButton button = AddButton(app, m, componentImage, "");
					setDialogSize(button, columnNumber, rowNumber, buttonBorderSize);
					
					moveShown = true;
					break;
				}
				
				// Set trump move
				else if (a instanceof ActionSetTrumpSuit)
				{
					final int trumpValue = ((ActionSetTrumpSuit) a).what();
					String trumpImage = "";
					Color imageColor = Color.BLACK;
					switch(trumpValue)
					{
						case 1: trumpImage = "card-suit-club"; break;
						case 2: trumpImage = "card-suit-spade"; break;
						case 3: trumpImage = "card-suit-diamond"; imageColor = Color.RED; break;
						case 4: trumpImage = "card-suit-heart"; imageColor = Color.RED; break;
					}
					BufferedImage componentImage = SVGUtil.createSVGImage(trumpImage, (int) (imageSize*0.8), (int) (imageSize*0.8));
					componentImage = BufferedImageUtil.setPixelsToColour(componentImage, imageColor);
					
					final JButton button = AddButton(app, m, componentImage, "");
					setDialogSize(button, columnNumber, rowNumber, buttonBorderSize);
					
					moveShown = true;
					break;
				}
				
				// Set next player move
				else if (a instanceof ActionSetNextPlayer && !m.isSwap())
				{
					final int nextPlayerValue = ((ActionSetNextPlayer) a).who();
					final String buttonText = "Next player: " + nextPlayerValue;
					
					final JButton button = AddButton(app, m, null, buttonText);
					setDialogSize(button, columnNumber, rowNumber, buttonBorderSize);
					
					moveShown = true;
					break;
				}
				
				// Pick the bet
				else if (a instanceof ActionBet)
				{
					final int betValue = ((ActionBet) a).count();
					final int betWho = ((ActionBet) a).who();
					final String buttonText = "P" + betWho + ", Bet: " + betValue;
					
					final JButton button = AddButton(app, m, null, buttonText);
					setDialogSize(button, columnNumber, rowNumber, buttonBorderSize);
					
					moveShown = true;
					break;
				}
				
				// Propose
				else if (a instanceof ActionPropose)
				{
					final String proposition = ((ActionPropose) a).proposition();
					final String buttonText = "Propose: " + proposition;
					
					final JButton button = AddButton(app, m, null, buttonText);
					setDialogSize(button, columnNumber, rowNumber, buttonBorderSize);
					
					moveShown = true;
					break;
				}
				
				// Vote
				else if (a instanceof ActionVote)
				{
					final String vote = ((ActionVote) a).vote();
					final String buttonText = "Vote: " + vote;
					
					final JButton button = AddButton(app, m, null, buttonText);
					setDialogSize(button, columnNumber, rowNumber, buttonBorderSize);
					
					moveShown = true;
					break;
				}
				
				// Moving a large piece
				if (a instanceof ActionMove)
				{			
					final int componentValue = app.contextSnapshot().getContext(app).containerState(fromContainerIndex).what(m.from(), m.fromType());
					if (componentValue != 0)
					{
						final Component c = context.components()[componentValue];
						if (c.isLargePiece())
						{
							final ComponentStyle componentStyle = app.bridge().getComponentStyle(c.index());
							final int maxSize = Math.max(componentStyle.largePieceSize().x, componentStyle.largePieceSize().y);
							final double scaleFactor = 0.9 * imageSize / maxSize;
							BufferedImage componentImage = app.graphicsCache().getComponentImage(app.bridge(), fromContainerIndex, c, c.owner(), a.state(), 0, m.from(), 0, m.fromType(), imageSize, app.contextSnapshot().getContext(app), 0, 0, true);
							componentImage = BufferedImageUtil.resize(componentImage, (int)(scaleFactor * componentStyle.largePieceSize().x), (int)(scaleFactor * componentStyle.largePieceSize().y));
							final JButton button = AddButton(app, m, componentImage, "");
							setDialogSize(button, columnNumber, rowNumber, buttonBorderSize);
						
							moveShown = true;
							break;
						}
					}
				}
			}
			
			// If no "special" action found
			if (!moveShown)
			{
				// Pass
				if (m.isPass())
				{ 
					final SVGGraphics2D g2d = new SVGGraphics2D(imageSize, imageSize);
					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
					g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
					g2d.setColor(Color.BLACK);
					g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
	
					SVGtoImage.loadFromFilePath
					(
						g2d, ImageUtil.getImageFullPath("button-pass"), new Rectangle(0,0,imageSize,imageSize), 
						Color.BLACK, Color.WHITE, 0
					);
					final BufferedImage swapImage = SVGUtil.createSVGImage(g2d.getSVGDocument(), imageSize, imageSize);
					
					final JButton button = AddButton(app, m, swapImage, "");
					setDialogSize(button, columnNumber, rowNumber, buttonBorderSize);
				}
				
				// Swap
				else if (m.isSwap())
				{ 
					final SVGGraphics2D g2d = new SVGGraphics2D(imageSize, imageSize);
					g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
					g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
					g2d.setColor(Color.BLACK);
					g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
	
					SVGtoImage.loadFromFilePath
					(
						g2d, ImageUtil.getImageFullPath("button-swap"), new Rectangle(0,0,imageSize,imageSize), 
						Color.BLACK, Color.WHITE, 0
					);
					final BufferedImage swapImage = SVGUtil.createSVGImage(g2d.getSVGDocument(), imageSize, imageSize);
					
					final JButton button = AddButton(app, m, swapImage, "");
					setDialogSize(button, columnNumber, rowNumber, buttonBorderSize);
				}
				
				// Default fallback
				else
				{
//					// Only display non-duplicated moves.
//					final List<Action> moveActions = m.getActionsWithConsequences(context.currentInstanceContext());
//					final List<Action> nonDuplicateActions = new ArrayList<>();
//					for (final Action a1 : moveActions)
//					{
//						for (final Move m2 : validMoves)
//						{	
//							if (!m2.getActionsWithConsequences(context.currentInstanceContext()).contains(a1))
//							{
//								nonDuplicateActions.add(a1);
//								break;
//							}
//						}
//					}
//					
//					if (nonDuplicateActions.size() > 0)
//						for (final Action a : nonDuplicateActions)
//							actionString += a.toString() + "<br>";
//					else
//						actionString += moveActions.toString() + "<br>";
					
					final String actionString = MoveUtil.getMoveFormat(app, m, context);
					final JButton button = AddButton(app, m, null, actionString);
					setDialogSize(button, columnNumber, rowNumber, buttonBorderSize);
				}
			}
		}
	}

	//-------------------------------------------------------------------------
	
	@Override
	protected void buttonMove(final PlayerApp app, final Move move)
	{
		if (MoveHandler.moveChecks(app, move))
			app.manager().ref().applyHumanMoveToGame(app.manager(), move);
	}
	
	//-------------------------------------------------------------------------
	
}
