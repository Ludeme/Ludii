package app.display.views.tabs.pages;

import java.awt.Rectangle;
import java.util.Arrays;

import app.PlayerApp;
import app.display.views.tabs.TabPage;
import app.display.views.tabs.TabView;
import game.Game;
import metadata.Metadata;
import other.context.Context;

/**
 * Tab for displaying information about the current game.
 * 
 * @author Matthew.Stephenson
 */
public class InfoPage extends TabPage
{

	//-------------------------------------------------------------------------
	
	public InfoPage(final PlayerApp app, final Rectangle rect, final String title, final String text, final int pageIndex, final TabView parent)
	{
		super(app, rect, title, text, pageIndex, parent);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void updatePage(final Context context)
	{
		reset();
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void reset()
	{
		clear();
		
		final Game game = app.contextSnapshot().getContext(app).game();
		try
		{
			final Metadata metadata = game.metadata();
			if (metadata != null)
			{
				// info tab
				if (metadata.info().getDescription().size() > 0)
				{
					addText("Description:\n");
					addText(metadata.info().getDescription().get(metadata.info().getDescription().size()-1));
					addText("\n\n");
				}
				if (metadata.info().getAuthor().size() > 0)
				{
					addText("Author:\n");
					addText(metadata.info().getAuthor().get(metadata.info().getAuthor().size()-1));
					addText("\n\n");
				}
				if (metadata.info().getPublisher().size() > 0)
				{
					addText("Publisher:\n");
					addText(metadata.info().getPublisher().get(metadata.info().getPublisher().size()-1));
					addText("\n\n");
				}
				if (metadata.info().getDate().size() > 0)
				{
					addText("Date:\n");
					addText(metadata.info().getDate().get(metadata.info().getDate().size()-1));
					addText("\n\n");
				}
				if (metadata.info().getAliases().length > 0)
				{
					addText("Aliases:\n");
					addText(Arrays.toString(metadata.info().getAliases()));
					addText("\n\n");
				}
				if (metadata.info().getOrigin().size() > 0)
				{
					addText("Origin:\n");
					addText(metadata.info().getOrigin().get(metadata.info().getOrigin().size()-1));
					addText("\n\n");
				}
				if (metadata.info().getClassification().size() > 0)
				{
					addText("Classification:\n");
					addText(metadata.info().getClassification().get(metadata.info().getClassification().size()-1));
					addText("\n\n");
				}
				if (metadata.info().getCredit().size() > 0)
				{
					addText("Credit:\n");
					addText(metadata.info().getCredit().get(metadata.info().getCredit().size()-1));
					addText("\n\n");
				}
				if (metadata.info().getVersion().size() > 0)
				{
					addText("Version:\n");
					addText(metadata.info().getVersion().get(metadata.info().getVersion().size()-1));
					addText("\n\n");
				}
				
				clear();
				addText(solidText);
			}
		}
		catch (final Exception e)
		{
			// One of the above was not defined properly.
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------

}
