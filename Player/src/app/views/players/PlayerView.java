package app.views.players;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import app.PlayerApp;
import app.views.View;
import game.Game;
import other.context.Context;
import util.PlaneType;

//-----------------------------------------------------------------------------

/**
 * View area containing all specific player views
 *
 * @author Matthew.Stephenson and cambolbro and Eric.Piette
 */
public class PlayerView extends View
{
	/** Player areas/sections/pages. */
	public List<PlayerViewUser> playerSections = new ArrayList<>();
	
	/** Font. */
	public Font playerNameFont = new Font("Arial", Font.PLAIN, 16);

	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 */
	public PlayerView(final PlayerApp app, final boolean portraitMode, final boolean exhibitionMode)
	{
		super(app);
		playerSections.clear();
		final Game game = app.contextSnapshot().getContext(app).game();
		final int numPlayers = game.players().count();
		
		final int maxHandHeight = 100;					// Maximum height of a player's hand.
		final double maxPanelPercentageHeight = 0.7;	// Maximum height of the entire panel (as percentage of app height).			
		
		int boardSize = app.height();
		int startX = boardSize + 8;
		int startY = 8;
		int width = app.width() - boardSize;
		int height = Math.min(maxHandHeight, (int)(app.height()*maxPanelPercentageHeight/numPlayers));
		
		if (app.manager().isWebApp() && portraitMode && numPlayers <= 4)
			playerNameFont = new Font("Arial", Font.PLAIN, 32);
		
		if (portraitMode)
		{
			boardSize = app.width();
			startX = 8;
			startY = app.manager().isWebApp() ? boardSize + 88 : boardSize + 48;	// +40 for the height of the toolView, +80 on mobile
			width = boardSize - 8;
			height = Math.min(maxHandHeight, (int)((app.height() - boardSize)*maxPanelPercentageHeight/numPlayers));
		}
		
		if (exhibitionMode)
		{
			for (int pid = 1; pid <= numPlayers; pid++)
			{
				final int x0 = startX + 5;
				int y0 = 75;
				if (pid == 2)
					y0 = 600;
				width = 600;
				height = 150;
				final Rectangle place = new Rectangle(x0, y0, width, (int) (height * 0.7));
				final PlayerViewUser playerPage = new PlayerViewUser(app, place, pid, this);
				app.getPanels().add(playerPage);
				playerSections.add(playerPage);
			}
		}
		else
		{
			// create a specific user page for each player.
			for (int pid = 1; pid <= numPlayers; pid++)
			{
				final int x0 = startX;
				final int y0 = startY + (pid-1) * height;
				final Rectangle place = new Rectangle(x0, y0, width, height);
				final PlayerViewUser playerPage = new PlayerViewUser(app, place, pid, this);
				app.getPanels().add(playerPage);
				playerSections.add(playerPage);
			}
		}
		
		// create the shared player pages (if it exists)
		if (app.contextSnapshot().getContext(app).hasSharedPlayer())
		{
			Rectangle place = new Rectangle(0, 0, boardSize, boardSize/10);
			
			// Place the shared hand in different location for exhibition app.
			if (app.settingsPlayer().usingExhibitionApp())
				place = new Rectangle(20, 290, app.width()-app.height(), boardSize/5);
			
			final PlayerViewShared naturePlayerPage = new PlayerViewShared(app, place, numPlayers + 1, this);
			app.getPanels().add(naturePlayerPage);
			playerSections.add(naturePlayerPage);
		}
		
		final int playerPanelWidth = app.width() - boardSize;
		final int playerPanelHeight = numPlayers * height + 24;
		
		placement.setBounds(boardSize, 0, playerPanelWidth, playerPanelHeight);
	}

	//-------------------------------------------------------------------------

	/**
	 * Draw player details and hand/dice/deck of the player.
	 * @param g2d
	 */
	@Override
	public void paint(final Graphics2D g2d)
	{		
		for (final PlayerViewUser p : playerSections)
			p.paint(g2d);
		
		paintDebug(g2d, Color.PINK);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Get the Maximum width of all player names (including extras).
	 */
	public int maximalPlayerNameWidth(final Context context, final Graphics2D g2d)
	{
		int numUsers = playerSections.size();
		if (app.contextSnapshot().getContext(app).hasSharedPlayer())
			numUsers -= 1;
		
		int maxNameWidth = 0;
		for (int panelIndex = 0; panelIndex < numUsers; panelIndex++)
		{
			final String stringNameAndExtras = playerSections.get(panelIndex).getNameAndExtrasString(context, g2d);
			final Rectangle2D bounds = playerNameFont.getStringBounds(stringNameAndExtras, g2d.getFontRenderContext());
			maxNameWidth = Math.max((int) bounds.getWidth(), maxNameWidth);
		}
		return maxNameWidth;
	}
	
	//-------------------------------------------------------------------------
	
	public void paintHand(final Graphics2D g2d, final Context context, final Rectangle place, final int handIndex)
	{
		app.bridge().getContainerStyle(handIndex).setPlacement(context, place);
		
		if (app.settingsPlayer().showPieces())
			app.bridge().getContainerStyle(handIndex).draw(g2d, PlaneType.COMPONENTS, context);
		
		app.bridge().getContainerStyle(handIndex).draw(g2d, PlaneType.INDICES, context);
		app.bridge().getContainerStyle(handIndex).draw(g2d, PlaneType.POSSIBLEMOVES, context);
		
	}
}
