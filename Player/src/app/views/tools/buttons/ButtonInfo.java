package app.views.tools.buttons;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import app.PlayerApp;
import app.views.tools.ToolButton;
import main.Constants;
import other.location.FullLocation;

//-----------------------------------------------------------------------------

/**
 * Settings button.
 *
 * @author Matthew.Stephenson and cambolbro
 */
public class ButtonInfo extends ToolButton
{
	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 *
	 * @param cx
	 * @param cy
	 * @param infoButtonIndex 
	 */
	public ButtonInfo(final PlayerApp app, final int cx, final int cy, final int sx, final int sy, final int infoButtonIndex)
	{
		super(app, "Info", cx, cy, sx, sy, infoButtonIndex);
		tooltipMessage = "Info";
	}

	//-------------------------------------------------------------------------

	@Override
	public void draw(final Graphics2D g2d)
	{
		final int cx = (int) rect.getCenterX();
		final int cy = (int) rect.getCenterY();
		
		g2d.setColor(getButtonColour());
	
		final Font oldFont = g2d.getFont();
		
		// Determine button scale, so that buttons are scaled up on the mobile version.
		// The desktop version assume a toolbar height of 32 pixels, this should be 64 for mobile version.
		final double scale = scaleForDevice();

		final int r = (int)(10 * scale);
		g2d.fillArc(cx - r, cy - r, 2 * r + 1, 2 * r + 1, 0, 360);

		final int fontSize = (int)(17 * scale);
		final int flags = Font.ITALIC | Font.BOLD;
		final Font font = new Font("Arial", flags, fontSize);
		g2d.setFont(font);
		g2d.setColor(Color.white);
		g2d.drawString("i", cx - (int)(3 * scale), cy + (int)(6 * scale));
		g2d.setFont(oldFont);
	}

	//-------------------------------------------------------------------------

	@Override
	// displays the information popup
	public void press()
	{
		app.showInfoDialog();
		app.bridge().settingsVC().setSelectedFromLocation(new FullLocation(Constants.UNDEFINED));
	}

	//-------------------------------------------------------------------------

}
