package app.display.views.tabs.pages;

import java.awt.Rectangle;

import app.PlayerApp;
import app.display.views.tabs.TabPage;
import app.display.views.tabs.TabView;
import other.context.Context;

/**
 * Tab for displaying all status messages (persistent between games).
 * 
 * @author Matthew.Stephenson
 */
public class StatusPage extends TabPage
{
	
	//-------------------------------------------------------------------------
	
	public StatusPage(final PlayerApp app, final Rectangle rect, final String title, final String text, final int pageIndex, final TabView parent)
	{
		super(app, rect, title, text, pageIndex, parent);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public void updatePage(final Context context)
	{		
		app.settingsPlayer().setSavedStatusTabString(text());
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void reset()
	{
		clear();
		addText(app.manager().aiSelected()[1].name() + " to move.\n");
	}
	
	//-------------------------------------------------------------------------

}
