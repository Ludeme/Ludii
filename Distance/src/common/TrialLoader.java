package common;

import java.io.File;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import common.trial_loader.AgentSetting;
import game.Game;
import other.AI;
import other.context.Context;
import other.trial.Trial;

public class TrialLoader
{

	public static Trial[] lazyloadTrials(
			final LudRul ludRul, final AgentSetting as, final int numPlayouts, final int numMaxMoves,
			final boolean regenerate
	)
	{
		final boolean serilasationProblemNotSolved = true;
		if (serilasationProblemNotSolved)return runTrials(ludRul, as, numPlayouts, numMaxMoves);
		
		final File f = getFileName(ludRul,as,numPlayouts,numMaxMoves);
		if (f.exists()&&!regenerate) {
			final Trial[] t = (Trial[]) DistanceUtils.deserialise(f.getAbsolutePath());
			return t;
		}else {
			final Trial[] t = runTrials(ludRul,as,numPlayouts,numMaxMoves);
			DistanceUtils.serialise(t, f.getAbsolutePath(), false);
			return t;
		}
	}

	private static Trial[] runTrials(
			final LudRul ludRul, final AgentSetting as, final int numPlayouts, final int numMaxMoves
	)
	{
		final Trial[] trials = new Trial[numPlayouts];
		final Game game = ludRul.getGame();
		 
		for (int i = 0; i < trials.length; i++)
		{
			final Trial trial = new Trial(game);
			final Context context = new Context(game, trial);
			game.start(context);
			final List<AI> ais = as.getAIs(2);
			game.playout(context, null, 1.0, null, 0, numMaxMoves, ThreadLocalRandom.current());
			trials[i] = trial;
		}
		
		
		return trials;
	}

	private static File getFileName(
			final LudRul ludRul, final AgentSetting as, final int numPlayouts, final int numMaxMoves
	)
	{	
		String a = ludRul.getGameNameIncludingOption(true);
		a = a+"_"+numPlayouts+"_"+numMaxMoves+ "_" + as.getName() + ".trl";
		final File f = new File(FolderLocations.resourceTrialFolder.getAbsolutePath() + "/" + a); 
		return f;
	}

}
