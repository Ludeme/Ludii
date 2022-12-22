package skillTraceAnalysis;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import game.Game;
import main.FileHandling;
import metrics.designer.SkillTrace;
import other.GameLoader;
import other.context.Context;
import other.trial.Trial;

/**
 * Calculates the Skill Trace statistics for all games in Ludii.
 * 
 * @author Matthew.Stephenson
 */
public class SkillTraceAnalysis 
{

	/**
	 * Main entry point.
	 */
	public static void main(final String[] args)
	{		
		final SkillTrace skillTraceMetric = new SkillTrace();
		skillTraceMetric.setAddToDatabaseFile(true);
		final String[] choicesUnsorted = FileHandling.listGames();
		
		// Order all games by their branching factor estimate.
		final Map<String,Integer> choicesBranchingFactors = new HashMap<>();
		for (final String s : choicesUnsorted)
		{
			if (FileHandling.shouldIgnoreLudRelease(s))
				continue;
			
			// Load the game and estimate branching factor as the number of legal moves at the start of the game.
			final Game game = GameLoader.loadGameFromName(s);
			final Trial trial = new Trial(game);
			final Context context = new Context(game, trial);
			game.start(context);
			final int bf = game.moves(context).count();
			System.out.println(game.name() + " BF: " + bf);
			choicesBranchingFactors.put(s, Integer.valueOf(bf));
		}
		final LinkedHashMap<String, Integer> choicesSortedBranchingFactors = new LinkedHashMap<>();
		choicesBranchingFactors.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEachOrdered(x -> choicesSortedBranchingFactors.put(x.getKey(), x.getValue()));
		final Set<String> choicesSorted = choicesSortedBranchingFactors.keySet();
		
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
				final String gameName = line.split(",")[0];
				gameNamesAlreadyDone.add(gameName);
			}
		}
		catch (final Exception e) 
		{
			e.printStackTrace();
		}
		
		for (final String s : choicesSorted)
		{
			final Game game = GameLoader.loadGameFromName(s);
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

}
