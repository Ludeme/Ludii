package app.display.dialogs.MoveDialog;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
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

import app.PlayerApp;
import app.display.dialogs.util.DialogUtil;
import app.move.MoveHandler;
import game.types.board.SiteType;
import other.action.puzzle.ActionReset;
import other.action.puzzle.ActionSet;
import other.action.puzzle.ActionToggle;
import other.context.Context;
import other.move.Move;
import other.state.container.ContainerState;

/**
 * Dialog for selecting moves in deduction puzzles.
 * 
 * @author Matthew.Stephenson
 */
public class PuzzleDialog extends JDialog
{
	private static final long serialVersionUID = 1L;
	
	/** List of all JButtons on this dialog. */
	final List<JButton> buttonList = new ArrayList<>();

	//-------------------------------------------------------------------------
	
	/**
	 * Show the Dialog.
	 */
	public static void createAndShowGUI(final PlayerApp app, final Context context, final int site)
	{
		try
		{
			final PuzzleDialog dialog = new PuzzleDialog(app, context, site);
			final Point drawPosn = new Point(MouseInfo.getPointerInfo().getLocation().x - dialog.getWidth() / 2, MouseInfo.getPointerInfo().getLocation().y - dialog.getHeight() / 2);
			DialogUtil.initialiseForcedDialog(dialog, "Puzzle Values", new Rectangle(drawPosn));
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
	public PuzzleDialog(final PlayerApp app, final Context context, final int site)
	{
		final int maxValue = context.board().getRange(context.board().defaultSite()).max(context);
		final int minValue = context.board().getRange(context.board().defaultSite()).min(context);
		
		final int numButtonsNeeded = maxValue - minValue + 2;

		int columnNumber = 0;
		int rowNumber = 0;
		columnNumber = (int) Math.ceil(Math.sqrt(numButtonsNeeded));
		rowNumber = (int) Math.ceil((double) (numButtonsNeeded) / (double) columnNumber);
		final int buttonSize = 80;

		this.setSize(buttonSize * columnNumber, buttonSize * rowNumber + 30);
		getContentPane().setLayout(new GridLayout(0, columnNumber, 0, 0));

		final ContainerState cs = context.state().containerStates()[0];

		// Add in button that sets all bit values to true (i.e. reset button)
		final JButton buttonReset = new JButton();
		buttonReset.setFocusPainted(false);
		getContentPane().add(buttonReset);

		if (app.bridge().settingsColour().getBoardColours()[2] == null)
		{
			buttonReset.setBackground(app.bridge().settingsColour().getBoardColours()[2]);
		}
		else if (app.bridge().settingsColour().getBoardColours()[2] != null)
		{
			buttonReset.setBackground(app.bridge().settingsColour().getBoardColours()[2]);
		}

		buttonReset.addMouseListener(new MouseListener()
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
				final Move resetMove = new Move(new ActionReset(context.board().defaultSite(), site, maxValue + 1));
				resetMove.setDecision(true);
				
				if (MoveHandler.moveChecks(app, resetMove))
					app.manager().ref().applyHumanMoveToGame(app.manager(), resetMove);
				dispose();
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

		for (int i = minValue; i <= maxValue; i++)
		{
			final int puzzleValue = i;

			other.action.puzzle.ActionSet a = null;
			a = new ActionSet(context.board().defaultSite(), site, puzzleValue);
			a.setDecision(true);
			final Move m = new Move(a);
			m.setFromNonDecision(site);
			m.setToNonDecision(site);
			m.setEdgeMove(site);
			m.setDecision(true);

			final JButton button = new JButton();
			try
			{
				final BufferedImage puzzleImage = new BufferedImage(buttonSize,buttonSize,BufferedImage.TYPE_INT_ARGB);
				final Graphics2D g2d = puzzleImage.createGraphics();
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2d.setFont(new Font("Arial", Font.PLAIN, 30));
				g2d.setColor(Color.BLACK);
				app.bridge().getContainerStyle(0).drawPuzzleValue(puzzleValue, site, context, g2d, new Point(buttonSize/2,buttonSize/2), buttonSize/2);
				button.setIcon(new ImageIcon(puzzleImage));
				
			}
			catch (final Exception ex)
			{
				System.out.println(ex);
			}

			button.setFocusPainted(false);
			getContentPane().add(button);

			if (!context.moves(context).moves().contains(m) && !app.settingsPlayer().illegalMovesValid())
			{
				button.setEnabled(false);
			}
			else
			{
				paintButton(app, context, button, site, puzzleValue, context.board().defaultSite());

				other.action.puzzle.ActionToggle a2 = null;
				a2 = new ActionToggle(context.board().defaultSite(), site, puzzleValue);
				a2.setDecision(true);
				final Move m2 = new Move(a2);
				m2.setFromNonDecision(site);
				m2.setToNonDecision(site);
				m2.setEdgeMove(site);
				m2.setDecision(true);

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
							MoveHandler.puzzleMove(app, site, puzzleValue, true, context.board().defaultSite());
							dispose();
						}
						else if (e.getButton() == MouseEvent.BUTTON3)
						{
							if (context.moves(context).moves().contains(m2) || app.settingsPlayer().illegalMovesValid())
							{
								MoveHandler.puzzleMove(app, site, puzzleValue, false, context.board().defaultSite());

								EventQueue.invokeLater(() -> 
								{
									final ArrayList<Integer> optionsLeft = new ArrayList<>();
									for (int j = minValue; j <= maxValue; j++)
									{
										if (cs.bit(site, j, context.board().defaultSite())
												&& buttonList.get(j - minValue).isEnabled())
										{
											optionsLeft.add(Integer.valueOf(j));
										}
									}

									if (optionsLeft.size() == 1)
									{
										MoveHandler.puzzleMove(app, site, optionsLeft.get(0).intValue(), true, context.board().defaultSite());
									}
									
									paintButton(app, context, button, site, puzzleValue, context.board().defaultSite());
								});
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

			buttonList.add(button);
		}
	}
	
	//-------------------------------------------------------------------------

	@SuppressWarnings("static-method")
	void paintButton(final PlayerApp app, final Context context, final JButton button, final int site, final int puzzleValue, final SiteType siteType)
	{
		final ContainerState cs = context.state().containerStates()[0];
		if (!cs.bit(site, puzzleValue, siteType))
		{
			button.setBackground(Color.GRAY);
		}
		else
		{
			if (app.bridge().settingsColour().getBoardColours()[2] == null)
			{
				button.setBackground(app.bridge().settingsColour().getBoardColours()[2]);
			}
			else if (app.bridge().settingsColour().getBoardColours()[2] != null)
			{
				button.setBackground(app.bridge().settingsColour().getBoardColours()[2]);
			}
			else
			{
				button.setBackground(Color.white);
			}
		}
	}
}
