package app.views.tools.buttons;

import java.awt.Color;
import java.awt.Graphics2D;
//import java.awt.geom.GeneralPath;

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
public class ButtonSettings extends ToolButton
{
	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 *
	 * @param cx
	 * @param cy
	 * @param settingsButtonIndex 
	 */
	public ButtonSettings(final PlayerApp app, final int cx, final int cy, final int sx, final int sy, final int settingsButtonIndex)
	{
		super(app, "Settings", cx, cy, sx, sy, settingsButtonIndex);
		tooltipMessage = "Preferences";
	}

	//-------------------------------------------------------------------------

	@Override
	public void draw(final Graphics2D g2d)
	{
		final int cx = (int) rect.getCenterX();
		final int cy = (int) rect.getCenterY();
		
		g2d.setColor(getButtonColour());
		
		// Determine button scale, so that buttons are scaled up on the mobile version.
		// The desktop version assume a toolbar height of 32 pixels, this should be 64 for mobile version.
		final double scale = scaleForDevice();

		final int d = (int)(10 * scale);
		final int dd = (int)(7 * scale);

		g2d.drawLine(cx - d, cy, cx + d, cy);
		g2d.drawLine(cx, cy - d, cx, cy + d);

		g2d.drawLine(cx - dd, cy - dd, cx + dd, cy + dd);
		g2d.drawLine(cx - dd, cy + dd, cx + dd, cy - dd);

		final int r = 7;
		g2d.fillArc(cx - r, cy - r, 2 * r + 1, 2 * r + 1, 0, 360);

		final int rr = 3;
		g2d.setColor(Color.white);
		g2d.fillArc(cx - rr, cy - rr, 2 * rr + 1, 2 * rr + 1, 0, 360);
	}

	//-------------------------------------------------------------------------

	@Override
	// displays the settings popup
	public void press()
	{
		app.showSettingsDialog();
		app.bridge().settingsVC().setSelectedFromLocation(new FullLocation(Constants.UNDEFINED));
	}

	//-------------------------------------------------------------------------

}
