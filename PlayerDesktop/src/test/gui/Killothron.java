package test.gui;

import java.util.ArrayList;
import java.util.Collections;

import app.DesktopApp;
import main.FileHandling;

/**
 * To start a killothron (beat a weak ai on all games and send report to a mail).
 * 
 * @author Eric.Piette
 */
public class Killothron
{
	/**
	 * Main method
	 * @param args
	 */
	public static void main(final String[] args)
	{
		final DesktopApp app = new DesktopApp();
		app.createDesktopApp();
		final String[] choices = FileHandling.listGames();
		final ArrayList<String> validChoices = new ArrayList<>();

		for (final String s : choices)
		{
			if (!s.contains("/bad/") && !s.contains("/bad_playout/") && !s.contains("/test/") && !s.contains("/wip/")
					&& !s.contains("/wishlist/"))
			{
				validChoices.add(s);
			}
		}
		
		Collections.shuffle(validChoices);

		final String gameToReach = "";

		boolean reached = (gameToReach.equals("") ? true : false);

		for (final String gameName : validChoices)
		{
			if (reached)
			{
				final RunGame thread = new RunGame(app, gameName);
				thread.run();
				while (!thread.isOver())
					try
					{
						Thread.sleep(100);
					}
					catch (InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			else if (gameName.contains(gameToReach))
			{
				reached = true;
			}
		}
	}
}
