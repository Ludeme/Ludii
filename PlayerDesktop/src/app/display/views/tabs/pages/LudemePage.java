package app.display.views.tabs.pages;

import java.awt.Rectangle;

import app.PlayerApp;
import app.display.views.tabs.TabPage;
import app.display.views.tabs.TabView;
import game.Game;
import other.context.Context;

/**
 * Tab for displaying ludeme description of the current game.
 * 
 * @author Matthew.Stephenson
 */
public class LudemePage extends TabPage
{

	//-------------------------------------------------------------------------
	
	public LudemePage(final PlayerApp app, final Rectangle rect, final String title, final String text, final int pageIndex, final TabView parent)
	{
		super(app, rect, title, text, pageIndex, parent);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void updatePage(final Context context)
	{
		//reset();
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void reset()
	{
		clear();
		final Game game = app.contextSnapshot().getContext(app).game();
		addText(game.description().raw());
	}
	
	//-------------------------------------------------------------------------

}
