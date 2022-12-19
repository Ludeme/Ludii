package skillTraceAnalysis;

import game.Game;
import main.FileHandling;
import metrics.Metric;
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
		final Metric skillTraceMetric = new SkillTrace();
		final String[] choices = FileHandling.listGames();
		
		for (final String s : choices)
		{
			final String gameName = s.split("\\/")[s.split("\\/").length-1];
			final Game game = GameLoader.loadGameFromName(gameName);
			System.out.println("\n------------");
			System.out.println(game.name());
			skillTraceMetric.apply(game, null, null, null);
		}
	}

	//-------------------------------------------------------------------------
	
}
