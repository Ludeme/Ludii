package killothron;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.DesktopApp;
import game.Game;
import main.Constants;
import main.FileHandling;
import main.StringRoutines;
import main.UnixPrintWriter;
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
		final double startTime = System.currentTimeMillis();
		
		final int sleepTime = 1; // Sleep time before to update the game (in ms).
		final double timeToThink = 60000; // Time for the challenger to think smartly (in ms).
		final int movesLimitPerPlayer = 200; // Max number of moves per player.
		final int numGamesToPlay = Constants.INFINITY;
		
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

		final String output = "KilothonResults.csv";
		try (final PrintWriter writer = new UnixPrintWriter(new File(output), "UTF-8"))
		{
			int idGame = 0; // index of the game.
			for (final String gameName : validChoices)
			{
					final Game game = GameLoader.loadGameFromName(gameName);
					if(!game.hasSubgames() && !game.hiddenInformation() && !game.isSimultaneousMoveGame() && !game.isSimulationMoveGame())
					{
						idGame++;
						System.out.println("game " + idGame + ": " + game.name() + " is running");
						
						// Start the game.
						final RunGame thread = new RunGame(app, gameName, game.players().count(), movesLimitPerPlayer);
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
	
						final List<String> lineToWrite = new ArrayList<String>();
						lineToWrite.add(game.name() + ""); // game name 
						lineToWrite.add(thread.ranking()[1] + ""); // ranking of P1
						lineToWrite.add(thread.gameLength() + ""); // game length
						writer.println(StringRoutines.join(",", lineToWrite));
					}
					
					if((idGame -1) > numGamesToPlay) // To stop the killothon after a specific number of games (for test).
						break;
			}
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		
		app.appClosedTasks();
		final double killothonTime = System.currentTimeMillis() - startTime;
		final double allSeconds = killothonTime / 1000.0;
		final int seconds = (int) (allSeconds % 60.0);
		final int minutes = (int) ((allSeconds - seconds) / 60.0);
		final int milliSeconds = (int) (killothonTime - (seconds * 1000));
		System.out.println("Killothon done in " + minutes + " minutes " + seconds + " seconds " + milliSeconds + " ms.");
	}
}
