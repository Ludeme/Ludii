package test.gui;

import java.util.ArrayList;
import java.util.Collections;

import app.DesktopApp;
import game.Game;
import main.FileHandling;
import other.GameLoader;

/**
 * To start a killothron (beat a weak ai on all games and send report to a mail).
 * Note: All games except, match, hidden information, simultaneous games or simulation games.
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
			if (s.contains("/lud/plex"))
				continue;

			if (s.contains("/lud/wip"))
				continue;

			if (s.contains("/lud/wishlist"))
				continue;

			if (s.contains("/lud/reconstruction"))
				continue;

			if (s.contains("/lud/WishlistDLP"))
				continue;

			if (s.contains("/lud/test"))
				continue;

			if (s.contains("/res/lud/bad"))
				continue;

			if (s.contains("/res/lud/bad_playout"))
				continue;
			
			validChoices.add(s);
		}
		
		Collections.shuffle(validChoices);

		final String gameToReach = "";

		boolean reached = (gameToReach.equals("") ? true : false);
		int numGame = 0;
		
		for (final String gameName : validChoices)
		{
			if (reached)
			{
				final Game game = GameLoader.loadGameFromName(gameName);
				if(!game.hasSubgames() && !game.hiddenInformation() && !game.isSimultaneousMoveGame() && !game.isSimulationMoveGame())
				{
					numGame++;
					System.out.println("game " + numGame + ": " + game.name() + " is running");
					final RunGame thread = new RunGame(app, gameName, game.players().count());
					thread.run();
					while (!thread.isOver())
					{
						try
						{
							Thread.sleep(100);
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
				}
			}
			else if (gameName.contains(gameToReach))
			{
				reached = true;
			}
		}
	}
}
