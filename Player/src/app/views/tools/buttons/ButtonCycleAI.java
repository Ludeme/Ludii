package app.views.tools.buttons;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
//import java.awt.geom.GeneralPath;

import app.PlayerApp;
import app.views.tools.ToolButton;
import main.Constants;
import manager.ai.AIUtil;
import other.location.FullLocation;

//-----------------------------------------------------------------------------

/**
 * Cycle AI button.
 *
 * @author Matthew.Stephenson
 */
public class ButtonCycleAI extends ToolButton
{
	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 *
	 * @param cx
	 * @param cy
	 * @param settingsButtonIndex 
	 */
	public ButtonCycleAI(final PlayerApp app, final int cx, final int cy, final int sx, final int sy, final int settingsButtonIndex)
	{
		super(app, "Cycle AI", cx, cy, sx, sy, settingsButtonIndex);
		tooltipMessage = "Preferences";
	}

	//-------------------------------------------------------------------------

	@Override
	public void draw(final Graphics2D g2d)
	{
		final int cx = (int) rect.getCenterX();
		final int cy = (int) rect.getCenterY();
		
		g2d.setColor(getButtonColour());
		final Font oldFont = g2d.getFont();
		final int r = 10;
		g2d.fillArc(cx - r, cy - r, 2 * r + 1, 2 * r + 1, 0, 360);
		final int fontSize = 17;
		final int flags = Font.ITALIC | Font.BOLD;
		final Font font = new Font("Arial", flags, fontSize);
		g2d.setFont(font);
		g2d.setColor(Color.white);
		g2d.drawString("c", cx - 3, cy + 6);
		g2d.setFont(oldFont);
	}

	//-------------------------------------------------------------------------

	@Override
	// displays the settings popup
	public void press()
	{
		AIUtil.cycleAgents(app.manager());
		app.bridge().settingsVC().setSelectedFromLocation(new FullLocation(Constants.UNDEFINED));
	}

	//-------------------------------------------------------------------------

}
