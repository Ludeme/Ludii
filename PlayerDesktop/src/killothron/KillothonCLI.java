package killothron;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import game.Game;
import main.Constants;
import main.FileHandling;
import main.StringRoutines;
import main.UnixPrintWriter;
import other.AI;
import other.GameLoader;
import other.context.Context;
import other.model.Model;
import other.trial.Trial;
import utils.AIFactory;

/**
 * To start a killothon without a GUI (beat a weak ai on all games and send report to a mail).
 * Note: All games except, match, hidden information, simultaneous games or simulation games.
 * 
 * @author Eric.Piette
 */
public class KillothonCLI
{
	/**
	 * Main method
	 * @param args
	 */
	public static void main(final String[] args)
	{
		final double startTime = System.currentTimeMillis();
		
		final double timeToThink = 60000; // Time for the challenger to think smartly (in ms).
		final int movesLimitPerPlayer = 200; // Max number of moves per player.
		final int numGamesToPlay = Constants.INFINITY;
		
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
					final int numPlayers = game.players().count();
					
					if(!game.hasSubgames() && !game.hiddenInformation() && !game.isSimultaneousMoveGame() && !game.isSimulationMoveGame())
					{
						idGame++;
						System.out.println("game " + idGame + ": " + game.name() + " is running");
						
						// Start the game.
						
						final List<AI> ais = new ArrayList<AI>();
						ais.add(null);
						for(int pid = 1; pid <= numPlayers; pid++)
						{
							if(pid == 1)
								ais.add(AIFactory.createAI("UCT"));
							else
								ais.add(new utils.RandomAI());
						}

						game.setMaxMoveLimit(numPlayers*movesLimitPerPlayer); // limit of moves per player.
						final Context context = new Context(game, new Trial(game));
						final Trial trial = context.trial();
						game.start(context);
						
						// Init the ais.
						for (int p = 1; p <= game.players().count(); ++p)
							ais.get(p).initAI(game, p);
						final Model model = context.model();
						
						double remainingTime = timeToThink; // One minute
						while (!trial.over())
						{
							final int mover = context.state().mover();
							final double time = System.currentTimeMillis();
							model.startNewStep(context, ais, 1);
							final double timeUsed = System.currentTimeMillis() - time;
						    
							if(remainingTime > 0) // We check the remaining time to be able to think smartly for the challenger.
							{
								if(mover == 1)
								{
									remainingTime = remainingTime - timeUsed;
									if(remainingTime <= 0)
									{
										System.out.println("switch P1 to Random");
										ais.get(1).closeAI();
										ais.set(1, new utils.RandomAI());
										ais.get(1).initAI(game, 1);
									}
//									else
//									{
//										System.out.println("remaining Time = " + remainingTime / 1000 + " s");
//									}
								}
							}
						}

						
						// Print the results.
						System.out.println("Ranking of P1 : " + trial.ranking()[1] + " finished in " + trial.numberRealMoves() + " moves.");
	
						final List<String> lineToWrite = new ArrayList<String>();
						lineToWrite.add(game.name() + ""); // game name 
						lineToWrite.add(trial.ranking()[1] + ""); // ranking of P1
						lineToWrite.add(trial.numberRealMoves() + ""); // game length
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
		
		final double killothonTime = System.currentTimeMillis() - startTime;
		final double allSeconds = killothonTime / 1000.0;
		final int seconds = (int) (allSeconds % 60.0);
		final int minutes = (int) ((allSeconds - seconds) / 60.0);
		final int milliSeconds = (int) (killothonTime - (seconds * 1000));
		System.out.println("Killothon done in " + minutes + " minutes " + seconds + " seconds " + milliSeconds + " ms.");
	}
}
