package app.display.dialogs.sandbox;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;

import app.DesktopApp;
import app.PlayerApp;
import app.display.MainWindowDesktop;
import app.display.dialogs.util.DialogUtil;
import game.equipment.component.Component;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import main.Constants;
import other.action.Action;
import other.action.move.ActionAdd;
import other.action.move.ActionRemove;
import other.action.state.ActionSetCount;
import other.action.state.ActionSetNextPlayer;
import other.action.state.ActionSetRotation;
import other.action.state.ActionSetState;
import other.context.Context;
import other.location.FullLocation;
import other.location.Location;
import other.move.Move;
import util.ContainerUtil;

/**
 * Dialog for showing sandbox options.
 * 
 * @author Matthew.Stephenson
 */
public class SandboxDialog extends JDialog
{
	private static final long serialVersionUID = 1L;
	List<JButton> buttonList = new ArrayList<>();

	//-------------------------------------------------------------------------
	
	/**
	 * Show the Dialog.
	 */
	public static void createAndShowGUI(final PlayerApp app, final Context context, final Location location, final SandboxValueType sandboxValueType)
	{
		try
		{
			if (context.components().length == 1)
			{
				DesktopApp.view().setTemporaryMessage("No valid components.");
				return;
			}
			
			final SandboxDialog dialog = new SandboxDialog(app, context, location, sandboxValueType);
			final Point drawPosn = new Point(MouseInfo.getPointerInfo().getLocation().x - dialog.getWidth() / 2, MouseInfo.getPointerInfo().getLocation().y - dialog.getHeight() / 2);
			DialogUtil.initialiseForcedDialog(dialog, "Sandbox Options", new Rectangle(drawPosn));
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
	public SandboxDialog(final PlayerApp app, final Context context, final Location location, final SandboxValueType sandboxValueType)
	{
		final int locnUpSite = location.site();
		final SiteType locnType = location.siteType();
		final int containerId = ContainerUtil.getContainerId(context, locnUpSite, locnType);
		
		final int currentMover = context.state().mover();
		final int nextMover = context.state().next();
		final int previousMover = context.state().prev();

		int numButtonsNeeded = context.components().length;
		
		final Component componentAtSite = context.components()[(context.containerState(containerId)).what(locnUpSite,locnType)];

		if (componentAtSite != null && componentAtSite.isDie())
		{
			MainWindowDesktop.setVolatileMessage(app, "Setting dice not supported yet.");
			EventQueue.invokeLater(() -> 
			{
				dispose();
				return;
			});
		}
		
		if (sandboxValueType == SandboxValueType.LocalState)
		{
			numButtonsNeeded = context.game().maximalLocalStates();
		}
		else if (sandboxValueType == SandboxValueType.Count)
		{
			numButtonsNeeded = context.game().maxCount();
		}
		else if (sandboxValueType == SandboxValueType.Rotation)
		{
			numButtonsNeeded = context.game().maximalRotationStates();
		}

		int columnNumber = 0;
		int rowNumber = 0;
		columnNumber = (int) Math.ceil(Math.sqrt(numButtonsNeeded));
		rowNumber = (int) Math.ceil((double)numButtonsNeeded / (double)columnNumber);
		
		final int buttonBorderSize = 20;
		final int imageSize = (int) (app.bridge().getContainerStyle(context.board().index()).cellRadius() * 2 * DesktopApp.view().getBoardPanel().boardSize());
		final int buttonSize = imageSize + buttonBorderSize;

		this.setSize(buttonSize*columnNumber, buttonSize*rowNumber + 30);
		getContentPane().setLayout(new GridLayout(0, columnNumber, 0, 0));

		if (sandboxValueType != SandboxValueType.Component)
		{
			for (int i = 0; i < numButtonsNeeded; i++)
			{
				try
				{
					// create an image of a number from a string
					final String text = Integer.toString(i);
					BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
				    Graphics2D g2d = img.createGraphics();
				    final Font font = new Font("Arial", Font.PLAIN, 40);
				    g2d.setFont(font);
				    FontMetrics fm = g2d.getFontMetrics();
				    final int width = fm.stringWidth(text);
				    final int height = fm.getHeight();
				    g2d.dispose();

				    img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				    g2d = img.createGraphics();
				    g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
				        RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
				    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				        RenderingHints.VALUE_ANTIALIAS_ON);
				    g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
				        RenderingHints.VALUE_COLOR_RENDER_QUALITY);
				    g2d.setRenderingHint(RenderingHints.KEY_DITHERING,
				        RenderingHints.VALUE_DITHER_ENABLE);
				    g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
				        RenderingHints.VALUE_FRACTIONALMETRICS_ON);
				    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				    g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
				        RenderingHints.VALUE_RENDER_QUALITY);
				    g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
				        RenderingHints.VALUE_STROKE_PURE);
				    g2d.setFont(font);
				    fm = g2d.getFontMetrics();
				    g2d.setColor(Color.BLACK);
				    g2d.drawString(text, 0, fm.getAscent());
				    g2d.dispose();

					final JButton button = new JButton();
					
					if (app.bridge().settingsColour().getBoardColours()[2] == null)
					{
						button.setBackground(app.bridge().settingsColour().getBoardColours()[2]);
					}
					else if (app.bridge().settingsColour().getBoardColours()[2] != null)
					{
						button.setBackground(app.bridge().settingsColour().getBoardColours()[2]);
					}

					try
					{
						button.setIcon(new ImageIcon(img));
					}
					catch (final Exception e)
					{
						e.printStackTrace();
					}

					button.setFocusPainted(false);
					getContentPane().add(button);

					final int value = i;

					button.addMouseListener(new MouseListener()
					{
						@Override
						public void mouseClicked(final MouseEvent e)
						{
							// do nothing
						}

						@Override
						public void mousePressed(final MouseEvent e)
						{
							// do nothing
						}

						@Override
						public void mouseReleased(final MouseEvent e)
					    {
					    	if (e.getButton() == MouseEvent.BUTTON1)
					        {
					    		Action action = null;
					    		
					    		if (sandboxValueType == SandboxValueType.LocalState)
									action = new ActionSetState(locnType, locnUpSite, Constants.UNDEFINED, value);
					    		if (sandboxValueType == SandboxValueType.Count)
								{
									final int what = context.game().equipment().components()
											[context.game().equipment().components().length - 1].index();
									action = new ActionSetCount(locnType, locnUpSite, what, value);
								}
					    		if (sandboxValueType == SandboxValueType.Rotation)
									action = new ActionSetRotation(locnType, locnUpSite, value);
					    		
					    		final Move moveToApply = new Move(action);
					    		final Moves csq = new BaseMoves(null);
					    		final Move nextMove = new Move(new ActionSetNextPlayer(context.state().mover()));
					    		csq.moves().add(nextMove);
					    		moveToApply.then().add(csq);
					    		
					    		moveToApply.apply(context, true);
					    		
					    		System.out.println(moveToApply);
					    		
					    		dispose();

					    		context.state().setMover(currentMover);
					    		context.state().setNext(nextMover);
					    		context.state().setPrev(previousMover);

					    		app.updateTabs(context);
					    		app.bridge().settingsVC().setSelectedFromLocation(new FullLocation(Constants.UNDEFINED));
					    		app.repaint();
							}
						}

						@Override
						public void mouseEntered(final MouseEvent e)
						{
							// do nothing
						}

						@Override
						public void mouseExited(final MouseEvent e)
						{
							// do nothing
						}
					});
				}
				catch (final Exception E)
				{
					// not a good component!
				}
			}
		}
		else
		{

			// add in button to remove existing component
			final JButton emptyButton = new JButton();
			if (app.bridge().settingsColour().getBoardColours()[2] == null)
			{
				emptyButton.setBackground(app.bridge().settingsColour().getBoardColours()[2]);
			}
			else if (app.bridge().settingsColour().getBoardColours()[2] != null)
			{
				emptyButton.setBackground(app.bridge().settingsColour().getBoardColours()[2]);
			}
	
			emptyButton.setFocusPainted(false);
			getContentPane().add(emptyButton);
	
			emptyButton.addMouseListener(new MouseListener()
			{
				@Override
				public void mouseClicked(final MouseEvent e)
				{
					// do nothing
				}
	
				@Override
				public void mousePressed(final MouseEvent e)
				{
					// do nothing
				}
	
				@Override
				public void mouseReleased(final MouseEvent e)
			    {
			    	if (e.getButton() == MouseEvent.BUTTON1)
			        {
						final Action actionRemove = new ActionRemove(context.board().defaultSite(), locnUpSite,
								Constants.UNDEFINED,
								true);
			    		
			    		final Move moveToApply = new Move(actionRemove);
			    		final Moves csq = new BaseMoves(null);
			    		final Move nextMove = new Move(new ActionSetNextPlayer(context.state().mover()));
			    		csq.moves().add(nextMove);
			    		moveToApply.then().add(csq);

			    		moveToApply.apply(context, true);
			    		
			    		dispose();
	
			    		context.state().setMover(currentMover);
			    		context.state().setNext(nextMover);
			    		context.state().setPrev(previousMover);
	
			    		app.updateTabs(context);
			    		app.bridge().settingsVC().setSelectedFromLocation(new FullLocation(Constants.UNDEFINED));
			    		app.repaint();
					}
				}
	
				@Override
				public void mouseEntered(final MouseEvent e)
				{
					// do nothing
				}
	
				@Override
				public void mouseExited(final MouseEvent e)
				{
					// do nothing
				}
			});
	
			for (final Component c: context.components())
			{
				try
				{
					if (!c.isDie())
					{
						final BufferedImage im = app.graphicsCache().getComponentImage(app.bridge(), containerId, c, c.owner(), 0, 0, 0, 0, locnType,imageSize, app.contextSnapshot().getContext(app), 0, 0, true);
	
						final JButton button = new JButton();
	
						if (app.bridge().settingsColour().getBoardColours()[2] == null)
						{
							button.setBackground(app.bridge().settingsColour().getBoardColours()[2]);
						}
						else if (app.bridge().settingsColour().getBoardColours()[2] != null)
						{
							button.setBackground(app.bridge().settingsColour().getBoardColours()[2]);
						}
	
						try
						{
							button.setIcon(new ImageIcon(im));
						}
						catch (final Exception e)
						{
							e.printStackTrace();
						}
	
						button.setFocusPainted(false);
						getContentPane().add(button);
	
						button.addMouseListener(new MouseListener()
						{
							@Override
							public void mouseClicked(final MouseEvent e)
							{
								// do nothing
							}
	
							@Override
							public void mousePressed(final MouseEvent e)
							{
								// do nothing
							}
	
							@Override
							public void mouseReleased(final MouseEvent e)
						    {
						    	if (e.getButton() == MouseEvent.BUTTON1)
						        {
									final Action actionAdd = new ActionAdd(locnType, locnUpSite, c.index(), 1,
											Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
											null);
						    		
						    		final Move moveToApply = new Move(actionAdd);
						    		final Moves csq = new BaseMoves(null);
						    		final Move nextMove = new Move(new ActionSetNextPlayer(context.state().mover()));
						    		csq.moves().add(nextMove);
						    		moveToApply.then().add(csq);
						    		
						    		moveToApply.apply(context, true);

						    		dispose();

						    		context.state().setMover(currentMover);
						    		context.state().setNext(nextMover);
						    		context.state().setPrev(previousMover);
	
						    		app.updateTabs(context);
						    		app.bridge().settingsVC().setSelectedFromLocation(new FullLocation(Constants.UNDEFINED));
						    		app.repaint();
	
									if (context.game().requiresLocalState() && context.game().maximalLocalStates() > 1)
									{
										createAndShowGUI(app, context, location, SandboxValueType.LocalState);
									}
									
									if (context.game().requiresRotation() && context.game().maximalRotationStates() > 1)
									{
										createAndShowGUI(app, context, location, SandboxValueType.Rotation);
									}
								}
							}
	
							@Override
							public void mouseEntered(final MouseEvent e)
							{
								// do nothing
							}
	
							@Override
							public void mouseExited(final MouseEvent e)
							{
								// do nothing
							}
						});
					}
				}
				catch (final Exception E)
				{
					// not a good component!
				}
			}
		}
		
	}

}
