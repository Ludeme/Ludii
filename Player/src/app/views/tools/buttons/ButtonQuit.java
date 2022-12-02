package app.views.tools.buttons;

import java.awt.Font;
import java.awt.Graphics2D;

import app.PlayerApp;
import app.views.tools.ToolButton;

//-----------------------------------------------------------------------------

/**
 * Quit button.
 *
 * @author Matthew.Stephenson and cambolbro
 */
public class ButtonQuit extends ToolButton
{
	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 *
	 * @param cx
	 * @param cy
	 * @param quitButtonIndex 
	 */
	public ButtonQuit(final PlayerApp app, final int cx, final int cy, final int sx, final int sy, final int quitButtonIndex)
	{
		super(app, "Quit", cx, cy, sx, sy, quitButtonIndex);
		tooltipMessage = "Quit";
	}

	//-------------------------------------------------------------------------

	@Override
	public void draw(final Graphics2D g2d)
	{
		final int cx = (int) rect.getCenterX();
		final int cy = (int) rect.getCenterY() + 5;
		
		g2d.setColor(getButtonColour());
	
		final Font oldFont = g2d.getFont();
		
		// Determine button scale, so that buttons are scaled up on the mobile version.
		// The desktop version assume a toolbar height of 32 pixels, this should be 64 for mobile version.
		final double scale = scaleForDevice();

		final int fontSize = (int)(26 * scale);
		final int flags = Font.BOLD;
		final Font font = new Font("Arial", flags, fontSize);
		g2d.setFont(font);
		g2d.setColor(getButtonColour());
		g2d.drawString("X", cx - (int)(3 * scale), cy + (int)(6 * scale));
		g2d.setFont(oldFont);
	}

	//-------------------------------------------------------------------------

	@Override
	// displays the information popup
	public void press()
	{
		System.exit(0);
	}

	//-------------------------------------------------------------------------

}
