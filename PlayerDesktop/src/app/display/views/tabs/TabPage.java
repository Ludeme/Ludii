package app.display.views.tabs;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import app.DesktopApp;
import app.PlayerApp;
import app.display.views.tabs.pages.InfoPage;
import app.display.views.tabs.pages.RulesPage;
import app.utils.SettingsExhibition;
import app.views.View;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * View showing a single tab page.
 *
 * @author Matthew.Stephenson and cambolbro
 */
public abstract class TabPage extends View
{
	/** Tab title. */
	protected String title = "Tab";

	/** Text area. */
	protected JTextPane textArea = new JTextPane();
	{
		textArea.setContentType("text/html");
	}
	
	protected HTMLDocument doc = (HTMLDocument)textArea.getDocument();
	protected Style textstyle = textArea.addStyle("text style", null);

	/** Font colour. */
	protected Color fontColour;
	
	/** Faded font colour. */
	protected Color fadedFontColour;

	/** Scroll pane for the text area. */
	protected JScrollPane scrollPane = new JScrollPane(textArea);
	
	/** Solid text to show on the text area. */
	public String solidText = "";
	
	/** Faded text to show on the text area. */
	public String fadedText = "";
	
	/** Rectangle bounding box for title. */
	public Rectangle titleRect = null;  //new Rectangle();

	/** Whether or not a mouse is over the title. */
	protected boolean mouseOverTitle = false;
	
	/** Tab page index. */
	public final int pageIndex;

	/** Tab view that holds all tab pages. */
	private final TabView parent;
	
	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 *
	 * @param rect
	 * @param title
	 * @param text
	 */
	public TabPage
	(
		final PlayerApp app, final Rectangle rect, final String title, final String text, final int pageIndex, final TabView parent
	)
	{
		super(app);
		this.parent = parent;

		placement = rect;
		
		this.title = new String(title);
		this.pageIndex = pageIndex;

		final int charWidth = 9;  // approximate char width for spacing tab page headers
		final int wd = charWidth * this.title.length();
		final int ht = TabView.fontSize;
		
		titleRect = new Rectangle(rect.x, rect.y, wd, ht);
		
		scrollPane.setBounds(placement);
		scrollPane.setBorder(null);
		scrollPane.setVisible(false);
		scrollPane.setFocusable(false);
		textArea.setFocusable(true);

		textArea.setEditable(false);
		textArea.setBackground(new Color(255, 255, 255));
		final DefaultCaret caret = (DefaultCaret) textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		textArea.setFont(new Font("Arial", Font.PLAIN, app.settingsPlayer().tabFontSize()));
		textArea.setContentType("text/html");
		textArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.valueOf(true));

		fontColour = new Color(50, 50, 50);
		textArea.setBackground(Color.white);
		
		fadedFontColour = new Color(fontColour.getRed() + (int) ((255 - fontColour.getRed()) * 0.75),
				fontColour.getGreen() + (int) ((255 - fontColour.getGreen()) * 0.75),
				fontColour.getBlue() + (int) ((255 - fontColour.getBlue()) * 0.75));

		StyleConstants.setForeground(textstyle, fontColour);

		textArea.setVisible(false); // true);
		textArea.setText(text);
		
		DesktopApp.view().setLayout(null);
		DesktopApp.view().add(scrollPane());
		
		textArea.addHyperlinkListener(new HyperlinkListener() 
	    {
	        @Override
	        public void hyperlinkUpdate(final HyperlinkEvent e) 
	        {
	            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) 
	            {
	                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) 
	                {
	                    try
						{
							Desktop.getDesktop().browse(new URI(e.getURL().toString()));
						}
						catch (IOException | URISyntaxException e1)
						{
							e1.printStackTrace();
						}
	                }
	            }
	        }
	    });
	}
	
	//-------------------------------------------------------------------------
	
	public abstract void updatePage(final Context context);
	public abstract void reset();

	//-------------------------------------------------------------------------

	public String title()
	{
		return title;
	}

	public Rectangle titleRect() 
	{
		return titleRect;
	}
	
	public void setTitleRect(final int x, final int y, final int wd, final int ht)
	{
		titleRect = new Rectangle(x, y, wd, ht);
	}
	
	public JScrollPane scrollPane()
	{
		return scrollPane;
	}

	//-------------------------------------------------------------------------

	/**
	 * Show/hide tab page.
	 *
	 * @param show
	 */
	public void show(final boolean show)
	{
		textArea.setVisible(show);
		scrollPane.setVisible(show);
	}

	//-------------------------------------------------------------------------

	/**
	 * Clear the console text.
	 */
	public void clear()
	{
		textArea.setText("");
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Add text to tab page.
	 */
	public void addText(final String str)
	{
		StyleConstants.setForeground(textstyle, fontColour);
		try
		{
			if (this instanceof InfoPage || this instanceof RulesPage)
			{
				final String htmlString = str.replaceAll("\n", "<br>");
				
			    final HTMLEditorKit editorKit = (HTMLEditorKit)textArea.getEditorKit();
			    doc = (HTMLDocument)textArea.getDocument();
			    try
				{
			    	// NOTE. This line can sometimes cause freezes when running tutorial generation. Not sure why...
			    	if (!app.settingsPlayer().isPerformingTutorialVisualisation())
			    		editorKit.insertHTML(doc, doc.getLength(), htmlString, 0, 0, null);
				}
				catch (final IOException e1)
				{
					e1.printStackTrace();
				}

			    final StringWriter writer = new StringWriter();
			    try
				{
					editorKit.write(writer, doc, 0, doc.getLength());
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}

			    solidText = textArea.getText().replaceAll("\n", "");
			}
			else
			{
				doc.insertString(doc.getLength(), str, textstyle);
				solidText = doc.getText(0, doc.getLength());
			}
		}
		catch (final BadLocationException ex)
		{
			ex.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Add faded text to tab page.
	 */
	protected void addFadedText(final String str)
	{
		StyleConstants.setForeground(textstyle, fadedFontColour);
		try
		{
			doc.insertString(doc.getLength(), str, textstyle);
			fadedText = doc.getText(solidText.length(), doc.getLength()-solidText.length());
        	Rectangle r = null;
    		textArea.setCaretPosition(solidText.length());
            r = textArea.modelToView(textArea.getCaretPosition());
            textArea.scrollRectToVisible(r);
        }
        catch (final Exception e) 
        {
        	// carry on
        }
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Current text.
	 */
	public String text()
	{
		try
		{
			return doc.getText(0, doc.getLength());
		}
		catch (final BadLocationException e)
		{
			e.printStackTrace();
			return textArea.getText();
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Draw player details.
	 *
	 * @param g2d
	 */
	@Override
	public void paint(final Graphics2D g2d)
	{
		if (!parent.titlesSet())
			parent.setTitleRects();
			
		drawTabPageTitle(g2d);
				
		paintDebug(g2d, Color.YELLOW);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Draw title of the tab page.
	 */
	private void drawTabPageTitle(final Graphics2D g2d)
	{
		if (SettingsExhibition.exhibitionVersion)
			return;
		
		final Font oldFont = g2d.getFont();
		final Font font = new Font("Arial", Font.BOLD, TabView.fontSize);
		g2d.setFont(font);

		final Color dark = new Color(50, 50, 50);
		final Color light = new Color(255, 255, 255);
		final Color mouseOver = new Color(150,150,150);

		if (pageIndex == app.settingsPlayer().tabSelected())
			g2d.setColor(dark);
		else if (mouseOverTitle)
			g2d.setColor(mouseOver);
		else
			g2d.setColor(light);

		final String str = title();
		final Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(str, g2d);

		final int tx = titleRect.x + (int)((titleRect.width / 2 - bounds.getWidth()/2));
		final int ty = titleRect.y + titleRect.height / 2 + 5;
		g2d.drawString(str, tx, ty);
		
		g2d.setFont(oldFont);
	}

	//-------------------------------------------------------------------------

	@Override
	public void mouseOverAt(final Point pixel)
	{
		// See if mouse is over any of the tabs titles	
		if (titleRect.contains(pixel.x, pixel.y))
		{
			if (!mouseOverTitle)
			{
				mouseOverTitle = true;
				DesktopApp.view().repaint(titleRect);
			}
		}
		else
		{
			if (mouseOverTitle)
			{
				mouseOverTitle = false;
				DesktopApp.view().repaint(titleRect);
			}
		}
	}

	//-------------------------------------------------------------------------

}
