package app.display.views.tabs;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import app.DesktopApp;
import app.PlayerApp;
import app.display.views.tabs.pages.AnalysisPage;
import app.display.views.tabs.pages.InfoPage;
import app.display.views.tabs.pages.LudemePage;
import app.display.views.tabs.pages.MovesPage;
import app.display.views.tabs.pages.RulesPage;
import app.display.views.tabs.pages.StatusPage;
import app.display.views.tabs.pages.TurnsPage;
import app.views.View;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * View area containing all TabViews
 *
 * @author Matthew.Stephenson and cambolbro
 */
public class TabView extends View
{
	/** Background colour. */
	public final static Color bgColour = new Color(255, 255, 230);
	
	/** Size of tab headings. */
	public final static int fontSize = 16;  
	
	/** Tab Page values. */
	public static final int PanelStatus = 0;
	public static final int PanelMoves = 1;
	public static final int PanelTurns = 2;
	public static final int PanelAnalysis = 3;
	public static final int PanelLudeme = 4;
	public static final int PanelRules = 5;
	public static final int PanelInfo = 6;
	
	//-------------------------------------------------------------------------

	/** If the titles of the tabs has been already set. */
	private boolean titlesSet = false;
	
	/** Tab panels. */
	private final List<TabPage> pages = new ArrayList<>();
	
	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 */
	public TabView(final PlayerApp app)
	{
		super(app);
		
		pages.clear();
		
		final int toolHeight = DesktopApp.view().toolPanel().placement().height;
		final int boardSize = DesktopApp.view().getBoardPanel().placement().width;
		
		final int startX = boardSize;
		final int startY = DesktopApp.view().getPlayerPanel().placement().height;
		final int width  = DesktopApp.view().getWidth() - boardSize;
		final int height = DesktopApp.view().getHeight() - DesktopApp.view().getPlayerPanel().placement().height - toolHeight;
		
		placement.setBounds(startX, startY, width, height);
		
		// Add tab pages
		final Rectangle tabPagePlacement = new Rectangle(placement.x + 10, placement.y + TabView.fontSize + 6, placement.width - 16, placement.height - TabView.fontSize - 20);
		final TabPage statusPage   = new StatusPage(app, tabPagePlacement, " Status ",   "", PanelStatus, this);
		final TabPage movesPage    = new MovesPage(app, tabPagePlacement, " Moves ",    "", PanelMoves, this);
		final TabPage turnsPage    = new TurnsPage(app, tabPagePlacement, " Turns",     "", PanelTurns, this);
		final TabPage analysisPage = new AnalysisPage(app, tabPagePlacement, " Analysis ", "", PanelAnalysis, this);
		final TabPage ludemePage   = new LudemePage(app, tabPagePlacement, " Ludeme  ",  "", PanelLudeme, this);
		final TabPage rulesPage    = new RulesPage(app, tabPagePlacement, " Rules ",    "", PanelRules, this);
		final TabPage infoPage     = new InfoPage(app, tabPagePlacement, " Info  ",    "", PanelInfo, this);
		pages.add(statusPage);
		pages.add(movesPage);
		pages.add(turnsPage);
		pages.add(analysisPage);
		pages.add(ludemePage);
		pages.add(rulesPage);
		pages.add(infoPage);	
		
		if (DesktopApp.view().tabPanel() != null && (placement.width != DesktopApp.view().tabPanel().placement.width || placement.height != DesktopApp.view().tabPanel().placement.height))
		{
			// Just resizing this panel
			for (final TabPage p : pages)
			{
				pages.get(p.pageIndex).clear();
				pages.get(p.pageIndex).addText(p.solidText);
				pages.get(p.pageIndex).addFadedText(p.fadedText);
			}
		}
		else
		{
			resetTabs();
		}
		
		pages.get(PanelStatus).clear();
		pages.get(PanelStatus).addText(app.settingsPlayer().savedStatusTabString());
		
		select(app.settingsPlayer().tabSelected());
		
		for (final View view : pages)
			DesktopApp.view().getPanels().add(view);
	}

	//-------------------------------------------------------------------------
	
	public boolean titlesSet()
	{
		return titlesSet;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void paint(final Graphics2D g2d)
	{
		final int x0 = placement.x;
		final int y0 = placement.y;
		final int sx = placement.width;
		final int sy = placement.height;

		if (!titlesSet)
		{
			setTitleRects();
			titlesSet = true;
		}

		g2d.setColor(Color.white);
		g2d.fillRect(x0, y0, sx, sy);

		// Title bar
		final int tx0 = placement.x;
		final int ty0 = placement.y;
		final int tsx = placement.width;
		final int tsy = fontSize + 6;

		g2d.setColor(new Color(200, 200, 200));
		g2d.fillRect(tx0, ty0, tsx, tsy);
		
		for (final TabPage page : pages)
			page.paint(g2d);
		
		paintDebug(g2d, Color.GREEN);
	}

	//-------------------------------------------------------------------------

	/**
	 * Sets rectangle bounds for all tab page titles. Used to select specific tab pages.
	 */
	public void setTitleRects()
	{
		int x = placement.x;
		final int y = placement.y;
		
		for (final TabPage page : pages)
		{
			final int wd = (int)page.titleRect().getWidth();
			final int ht = fontSize + 6;
			
			page.setTitleRect(x, y, wd, ht);
			x += wd;
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Select the specified tab index
	 * @param pid
	 */
	public void select(final int pid)
	{
		for (final TabPage p : pages)
			p.show(false);
		
		pages.get(pid).show(true);
		app.settingsPlayer().setTabSelected(pid);
		app.repaint();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Handle click on tab title.
	 * @param pixel
	 */
	public void clickAt(final Point pixel)
	{
		for (final TabPage p : pages)
			if (p.titleRect.contains(pixel.x, pixel.y))
			{
				select(p.pageIndex);
				return;
			}
	}
	
	//-------------------------------------------------------------------------
	
	public void updateTabs(final Context context)
	{
		for(int i = 0; i < pages.size(); i++)
			pages.get(i).updatePage(context);
		
		// If we need to update the tabs, some games also need the board to be updated as well.
		if (context.game().metadata().graphics().needRedrawn())
			app.graphicsCache().setBoardImage(null);
	}
	
	//-------------------------------------------------------------------------
	
	public void resetTabs()
	{
		for(int i = 1; i < pages.size(); i++)
			pages.get(i).reset();
	}
	
	//-------------------------------------------------------------------------

	public TabPage page(final int i)
	{
		return pages.get(i);
	}
	
	public List<TabPage> pages()
	{
		return pages;
	}
	
	//-------------------------------------------------------------------------
	
}
