package app.views.tools.buttons;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

import app.PlayerApp;
import app.utils.TrialUtil;
import app.views.tools.ToolButton;
import app.views.tools.ToolView;
import manager.ai.AIUtil;
import other.context.Context;

/**
 * Settings button.
 * 
 * @author Matthew.Stephenson and cambolbro
 */
public class ButtonStart extends ToolButton
{
	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 *
	 * @param cx
	 * @param cy
	 * @param startButtonIndex 
	 */
	public ButtonStart(final PlayerApp app, final int cx, final int cy, final int sx, final int sy, final int startButtonIndex)
	{
		super(app, "Start", cx, cy, sx, sy, startButtonIndex);
		tooltipMessage = "Back to Start";
	}

	//-------------------------------------------------------------------------

	@Override
	public void draw(final Graphics2D g2d)
	{
		final double cx = rect.getCenterX();
		final double cy = rect.getCenterY();
		
		g2d.setColor(getButtonColour());
		
		// Determine button scale, so that buttons are scaled up on the mobile version.
		// The desktop version assume a toolbar height of 32 pixels, this should be 64 for mobile version.
		final double scale = scaleForDevice();

		g2d.setStroke(new BasicStroke((float)(3 * scale), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));

		GeneralPath path = new GeneralPath();
		path.moveTo(cx + 10 * scale, cy + 7 * scale);
		path.lineTo(cx, cy);
		path.lineTo(cx + 10 * scale , cy - 7 * scale);
		g2d.draw(path);
		
		g2d.setStroke(new BasicStroke((2), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
		path = new GeneralPath();
		path.moveTo(cx - 4 * scale, cy + 9 * scale);
		path.lineTo(cx - 4 * scale, cy - 9 * scale);
		g2d.draw(path);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	protected boolean isEnabled()
	{
		final Context context = app.manager().ref().context();
		final int numInitialPlacementMoves = context.currentInstanceContext().trial().numInitialPlacementMoves();
		if 
		(
			(
				context.currentSubgameIdx() > 1
				||
				context.trial().numMoves() > numInitialPlacementMoves
			)
			&& 
			app.manager().settingsNetwork().getActiveGameId() == 0
		)
			return true;
		
		return false;
	}

	//-------------------------------------------------------------------------

	// Goes to the start (first) location of the current trial
	@Override
	public void press()
	{
		if (isEnabled())
		{
			Context context = app.manager().ref().context();
			
			// Go back one move first.
			ToolView.jumpToMove(app, context.trial().numMoves() - 1);
			
			context = app.manager().ref().context();
			
			ToolView.jumpToMove(app, TrialUtil.getInstanceStartIndex(context));
			
			// Don't pause agents unless we have to.
			if (!app.settingsPlayer().startButtonPausesAI())
				AIUtil.pauseAgentsIfNeeded(app.manager());
		}
	}

	//-------------------------------------------------------------------------

}
