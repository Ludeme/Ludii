package killothron;

import java.util.ArrayList;
import java.util.Collections;

import app.DesktopApp;
import game.Game;
import main.FileHandling;
import other.GameLoader;

/**
 * To start a killothon (beat a weak ai on all games and send report to a mail).
 * Note: All games except, match, hidden information, simultaneous games or simulation games.
 * 
 * @author Eric.Piette
 */
public class Killothon
{
	/**
	 * Main method
	 * @param args
	 */
	public static void main(final String[] args)
	{
		final int sleepTime = 1; // Sleep time before to update the game (in ms).
		final double timeToThink = 60000; // Time for the challenger to think smartly (in ms).
		
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
		
		Collections.shuffle(validChoices); // Random order for the games.

		int idGame = 0; // index of the game.
		for (final String gameName : validChoices)
		{
				final Game game = GameLoader.loadGameFromName(gameName);
				if(!game.hasSubgames() && !game.hiddenInformation() && !game.isSimultaneousMoveGame() && !game.isSimulationMoveGame())
				{
					idGame++;
					System.out.println("game " + idGame + ": " + game.name() + " is running");
					
					// Start the game.
					final RunGame thread = new RunGame(app, gameName, game.players().count());
					double time = System.currentTimeMillis();
					double remainingTime = timeToThink; // One minute
					
					// Run the game.
					thread.run();
					while (!thread.isOver())
					{
						try
						{
							Thread.sleep(sleepTime);
							
							if(remainingTime > 0) // We check the remaining time to be able to think smartly for the challenger.
							{
								final double timeUsed = System.currentTimeMillis() - time;
								if(thread.mover() == 1) // If that's the challenger we decrement the time used.
								{
									remainingTime = remainingTime - timeUsed;
									//System.out.println("remaining Time = " + remainingTime/1000 + " s");
								}
							    time = System.currentTimeMillis();
							    
								if(remainingTime <= 0)
									thread.setFirstPlayerToRandom();
							}
						}
						catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}
					
					// Print the results.
					System.out.println("Ranking of P1 : " + thread.ranking()[1] + " finished in " + thread.gameLength() + " moves.");
				}
		}
	}
}
