package app.display.dialogs.MoveDialog;

import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;

import app.PlayerApp;
import app.display.dialogs.util.DialogUtil;
import metadata.graphics.util.colour.ColourRoutines;
import other.context.Context;
import other.move.Move;

/**
 * Dialog for showing various moves.
 * 
 * @author Matthew.Stephenson
 */
public abstract class MoveDialog extends JDialog
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** Additional height to add to account for the menu bar. */
	final static int menuBarHeight = 30;
	
	/** Border space around each button. */
	final static int buttonBorderSize = 20;
	
	/** Number of columns of buttons. */
	int columnNumber = 0;
	
	/** Number of rows of buttons. */
	int rowNumber = 0;
	
	/** Size of images on buttons. */
	int imageSize = 0;
	
	//-------------------------------------------------------------------------

	/**
	 * Create the dialog.
	 */
	protected MoveDialog()
	{
		// Don't initialise.
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Changes the size of the dialog, to account for a newly added button.
	 * @param button
	 * @param columnNumber
	 * @param rowNumber
	 * @param buttonBorderSize
	 */
	protected void setDialogSize(final JButton button, final int columnNumber, final int rowNumber, final int buttonBorderSize)
	{
		final int maxWidth = Math.max(getWidth(), (int)((button.getPreferredSize().getWidth()) * columnNumber));
		final int maxHeight = Math.max(getHeight(), (int)((button.getPreferredSize().getHeight() + buttonBorderSize) * rowNumber) + menuBarHeight);
		this.setSize(maxWidth, maxHeight);
	}

	//-------------------------------------------------------------------------

	/**
	 * Adds a button to apply a specified move
	 * @param move 		The move to apply
	 * @param image 	The image for the button's icon
	 * @param text 		The text for the button
	 */
	protected JButton AddButton(final PlayerApp app, final Move move, final BufferedImage image, final String text)
	{
		final JButton button = new JButton();
		button.setBackground(app.bridge().settingsColour().getBoardColours()[2]);
		button.setForeground(ColourRoutines.getContrastColorFavourDark(app.bridge().settingsColour().getBoardColours()[2]));

		if (image != null)
			button.setIcon(new ImageIcon(image));
		
		if (text.length() > 0)
		{
			final String htmlText = "<html><center> " + text + " </center></html>";
			button.setText(DialogUtil.getWrappedText(button.getGraphics(), button, htmlText));
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
					buttonMove(app, move);
					dispose();
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
		
		return button;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Perform this code when a button is pressed
	 */
	protected void buttonMove(final PlayerApp app, final Move m)
	{
		// do nothing;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Set the layout of this dialog.
	 */
	protected void setDialogLayout(final PlayerApp app, final Context context, final int numButtons)
	{
		columnNumber = (int) Math.ceil(Math.sqrt(numButtons));
		rowNumber = (int) Math.ceil((double) numButtons / (double) columnNumber);
		imageSize = Math.min(100, app.bridge().getContainerStyle(context.board().index()).cellRadiusPixels() * 2);
		getContentPane().setLayout(new GridLayout(0, columnNumber, 0, 0));
	}
	
}
