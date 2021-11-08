package app.views.players;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import app.PlayerApp;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Panel showing the Shared pieces
 *
 * @author Matthew.Stephenson and cambolbro and Eric.Piette
 */
public class PlayerViewShared extends PlayerViewUser
{
	
	/**
	 * Constructor.
	 */
	public PlayerViewShared(final PlayerApp app, final Rectangle rect, final int pid, final PlayerView playerView, final boolean portraitMode)
	{
		super(app, rect, pid, playerView, portraitMode);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void paint(final Graphics2D g2d)
	{
		if (hand != null)
		{
			final Context context = app.contextSnapshot().getContext(app);
			final Rectangle containerPlacement = new Rectangle((placement.x), placement.y - placement.height/2, (placement.width), placement.height);
			
			playerView.paintHand(g2d, context, containerPlacement, hand.index());
			
			//if (SettingsPlayer.showLastMove)
			//	DesktopGraphics.drawLastMove(g2d, hand.index());
		}
		
		paintDebug(g2d, Color.ORANGE);
	}

	//-------------------------------------------------------------------------

}
