package app.views.tools.buttons;

import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

import app.PlayerApp;
import app.views.tools.ToolButton;
import game.types.play.ModeType;
import main.Constants;
import manager.ai.AIUtil;
import other.location.FullLocation;

//-----------------------------------------------------------------------------

/**
 * Play/Pause button.
 *
 * @author Matthew.Stephenson and cambolbro
 */
public class ButtonPlayPause extends ToolButton
{
	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 *
	 * @param cx
	 * @param cy
	 * @param playButtonIndex 
	 */
	public ButtonPlayPause(final PlayerApp app, final int cx, final int cy, final int sx, final int sy, final int playButtonIndex)
	{
		super(app, "PlayPause", cx, cy, sx, sy, playButtonIndex);
		tooltipMessage = "Player/Pause";
	}

	//-------------------------------------------------------------------------

	@Override
	public void draw(final Graphics2D g2d)
	{
		final double cx = rect.getCenterX();
		final double cy = rect.getCenterY();
		
		g2d.setColor(getButtonColour());
		//g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
		GeneralPath path = new GeneralPath();
		
		// Determine button scale, so that buttons are scaled up on the mobile version.
		// The desktop version assume a toolbar height of 32 pixels, this should be 64 for mobile version.
		final double scale = scaleForDevice();
		
		if (app.manager().settingsManager().agentsPaused())
		{
			// Display Play Symbol
			path.moveTo(cx + 9 * scale, cy);
			path.lineTo(cx - 7 * scale, cy - 9 * scale);
			path.lineTo(cx - 7 * scale, cy + 9 * scale);
			g2d.fill(path);
		}
		else
		{
			// Display Pause Symbol
			path.moveTo(cx - 7 * scale , cy + 9 * scale );
			path.lineTo(cx - 7 * scale , cy - 9 * scale );
			path.lineTo(cx - 2 * scale , cy - 9 * scale );
			path.lineTo(cx - 2 * scale , cy + 9 * scale );
			g2d.fill(path);
			path = new GeneralPath();
			path.moveTo(cx + 2 * scale , cy + 9 * scale );
			path.lineTo(cx + 2 * scale , cy - 9 * scale );
			path.lineTo(cx + 7 * scale , cy - 9 * scale );
			path.lineTo(cx + 7 * scale , cy + 9 * scale );
			g2d.fill(path);
		}
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	protected boolean isEnabled()
	{
		if (app.manager().ref().context().game().mode().mode().equals(ModeType.Simulation))
			return true;
		
		if (AIUtil.anyAIPlayer(app.manager()) && (app.manager().settingsNetwork().getActiveGameId() == 0 || app.manager().settingsNetwork().getOnlineAIAllowed()))
			return true;
		
		return false;
	}

	//-------------------------------------------------------------------------

	@Override
	// Either pauses or resumes the agents playing the game
	public void press()
	{
		if (isEnabled())
		{
			if (!app.manager().settingsManager().agentsPaused())
			{
				app.manager().settingsManager().setAgentsPaused(app.manager(), true);
			}
			else
			{
				app.manager().settingsManager().setAgentsPaused(app.manager(), false);
				app.manager().ref().nextMove(app.manager(), false);
			}
			
			app.bridge().settingsVC().setSelectedFromLocation(new FullLocation(Constants.UNDEFINED));
		}
	}

	//-------------------------------------------------------------------------

}
