package skillTraceAnalysis;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import game.Game;
import main.FileHandling;
import metrics.designer.SkillTrace;
import other.GameLoader;

public class SkillTraceAnalysis 
{

	//-------------------------------------------------------------------------
	
	/**
	 * Predicts the win-rate for a variety of games, AI agents and prediction algorithms.
	 */
	public static void main(final String[] args)
	{		
		final SkillTrace skillTraceMetric = new SkillTrace();
		skillTraceMetric.setAddToDatabaseFile(true);
		skillTraceMetric.setCurrentDatabaseId(1);
		final String[] choices = FileHandling.listGames();
		
		// Record games that have already been done, and should not be redone.
		final List<String> gameNamesAlreadyDone = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(skillTraceMetric.combinedResultsOutputPath()), "UTF-8")))
		{
			while (true)
			{
				final String line = reader.readLine();
				if (line == null)
				{
					break;
				}
				final String gameName = line.split(",")[1];
				gameNamesAlreadyDone.add(gameName);
			}
		}
		catch (final Exception e) 
		{
			e.printStackTrace();
		}
		
		for (final String s : choices)
		{
			if (FileHandling.shouldIgnoreLudRelease(s))
				continue;
			
			final String gameName = s.split("\\/")[s.split("\\/").length-1];
			final Game game = GameLoader.loadGameFromName(gameName);
			if (gameNamesAlreadyDone.contains(game.name()))
			{
				System.out.println("\n------------");
				System.out.println(game.name() + " skipped");
				continue;
			}
			
			System.out.println("\n------------");
			System.out.println(game.name());
			
			skillTraceMetric.apply(game, null, null, null);
		}
	}

	//-------------------------------------------------------------------------
	
}
