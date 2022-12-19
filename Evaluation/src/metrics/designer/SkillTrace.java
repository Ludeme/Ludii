package metrics.designer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.rng.RandomProviderState;

import game.Game;
import main.UnixPrintWriter;
import main.math.LinearRegression;
import metrics.Evaluation;
import metrics.Metric;
import other.AI;
import other.RankUtils;
import other.context.Context;
import other.model.Model;
import other.trial.Trial;
import search.mcts.MCTS;

/**
 * Skill trace of the game.
 * NOTE. This metric doesn't work with stored trials, and must instead generate new trials each time.
 * NOTE. Only works games that are supported by UCT
 * 
 * @author matthew.stephenson and Dennis Soemers
 */
public class SkillTrace extends Metric
{
	
	// Number of matches (iteration count doubles each time)
	private int numMatches = 8;
	
	// Number of trials per match
	private int numTrialsPerMatch = 30;
	
	// A hard time limit in seconds, after which any future trials are aborted
	private int hardTimeLimit = 60;
	
	// Output path for more details results
	private String outputPath = "";
	
	// Database storage options
	private boolean addToDatabaseFile = false;
	private String combinedResultsOutputPath = "SkillTraceResults.csv";
	private int currentDatabaseId = 1;

	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public SkillTrace()
	{
		super
		(
			"Skill trace", 
			"Skill trace of the game.", 
			0.0, 
			1.0,
			null
		);
	}
	
	//-------------------------------------------------------------------------
	
	@SuppressWarnings("boxing")
	@Override
	public Double apply
	(
			final Game game,
			final Evaluation evaluation,
			final Trial[] trials,
			final RandomProviderState[] randomProviderStates
	)
	{
		String outputString = "";
		
		final List<Double> strongAIResults = new ArrayList<>();
		double areaEstimate = 0.0;
		final long startTime = System.currentTimeMillis();
		
		final List<AI> ais = new ArrayList<AI>(game.players().count() + 1);
		ais.add(null);
		for (int p = 1; p <= game.players().count(); ++p)
			ais.add(MCTS.createUCT());
		
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		
		game.start(context);
		final int bf = game.moves(context).count();
		
		System.out.println(numTrialsPerMatch + " trials per level, time limit " + hardTimeLimit + "s, BF=" + bf + ".");
		outputString +=    numTrialsPerMatch + " trials per level, time limit " + hardTimeLimit + "s, BF=" + bf + ".\n";
		
		int weakIterationValue = 2;
		int matchCount = 0;
		for (/**/; matchCount < numMatches; matchCount++)
		{
			double strongAIAvgResult = 0.0;
			int strongAgentIdx = 1;
			for (int i = 0; i < numTrialsPerMatch; ++i)
			{
				game.start(context);
				
				for (int p = 1; p <= game.players().count(); ++p)
					ais.get(p).initAI(game, p);

				final Model model = context.model();

				while (!trial.over())
				{
					final int mover = context.state().playerToAgent(context.state().mover());
					final int numIterations;
					if (mover == strongAgentIdx)
						numIterations = weakIterationValue * 2;
					else
						numIterations = weakIterationValue;
					
					model.startNewStep(context, ais, -1.0, numIterations * game.moves(context).count(), -1, 0.0);
				}
				
				// Record the utility of the strong agent
				strongAIAvgResult += RankUtils.agentUtilities(context)[strongAgentIdx];

				// Change which player is controlled by the strong agent
				++strongAgentIdx;
				if (strongAgentIdx > game.players().count())
					strongAgentIdx = 1;
				
				// Check the current time, and if we have elapsed the limit then abort.
				if (System.currentTimeMillis() > (startTime + hardTimeLimit*1000))
					break;
			}
			
			// If we didn't finish all trials in time, then ignore the match results
			if (System.currentTimeMillis() > (startTime + hardTimeLimit*1000))
			{
				System.out.println("Aborting after " + String.valueOf(matchCount) + " levels.");
				outputString += "Aborting after " + String.valueOf(matchCount) + " levels.\n";
				break;
			}
			
			strongAIAvgResult /= numTrialsPerMatch;
			strongAIResults.add(Double.valueOf(strongAIAvgResult));
			areaEstimate += Math.max(strongAIAvgResult, 0.0);
			weakIterationValue *= 2;
			
			// Print match results in console
			System.out.println("Level " + (matchCount+1) + ", strong AI result: " + strongAIAvgResult);
			outputString +=    "Level " + (matchCount+1) + ", strong AI result: " + strongAIAvgResult + "\n";
		}
		
		// Predict next step y value.
		final double[] xAxis = IntStream.range(0, strongAIResults.size()).asDoubleStream().toArray();
		final double[] yAxis = strongAIResults.stream().mapToDouble(Double::doubleValue).toArray();
		final LinearRegression linearRegression = new LinearRegression(xAxis, yAxis);
		double yValueNextStep = linearRegression.predict(numMatches+1);
		yValueNextStep = Math.max(Math.min(yValueNextStep, 1.0), 0.0);
		
		// No matches were able to be completed within the time limit.
		if (matchCount == 0)
			return Double.valueOf(0);
		
		final double skillTrace = yValueNextStep + (1-yValueNextStep)*(areaEstimate/matchCount);
		
		final double secs = (System.currentTimeMillis() - startTime) / 1000.0;
		
		System.out.println(String.format("Skill trace %.3f in %.3fs (slope error %.3f, intercept error %.3f).", skillTrace, secs, linearRegression.slopeStdErr(), linearRegression.interceptStdErr()));
		outputString +=    String.format("Skill trace %.3f in %.3fs (slope error %.3f, intercept error %.3f).", skillTrace, secs, linearRegression.slopeStdErr(), linearRegression.interceptStdErr());
		
		// Store outputString in a text file if specified.
		if (outputPath.length() > 1)
		{
			final String outputSkillTracePath = outputPath + game.name() + "_skillTrace.txt";
			try (final PrintWriter writer = new UnixPrintWriter(new File(outputSkillTracePath), "UTF-8"))
			{
				writer.println(outputString);
			}
			catch (final FileNotFoundException | UnsupportedEncodingException e2)
			{
				e2.printStackTrace();
			}
		}
		
		// Append output as an entry of the csv defined in "combinedResultsOutputPath", for uploading to Database.
		if (addToDatabaseFile)
		{	
			try (final PrintWriter writer = new PrintWriter(new FileOutputStream(new File(combinedResultsOutputPath()), true)))
			{
				String entryString = currentDatabaseId + ",";
				entryString += game.name() + ",";
				entryString += game.metadata().info().getId().get(0) + ",";
				entryString += skillTrace + ",";
				entryString += numTrialsPerMatch + ",";
				entryString += matchCount + ",";
				entryString += hardTimeLimit + ",";
				entryString += linearRegression.slopeStdErr() + ",";
				entryString += linearRegression.interceptStdErr() + ",";
				writer.println(entryString);
				currentDatabaseId++;
			}
			catch (final FileNotFoundException e2)
			{
				e2.printStackTrace();
			}	
		}
		
		return Double.valueOf(skillTrace);
	}

	public void setNumMatches(final int numMatches) 
	{
		this.numMatches = numMatches;
	}

	public void setNumTrialsPerMatch(final int numTrialsPerMatch) 
	{
		this.numTrialsPerMatch = numTrialsPerMatch;
	}

	public void setHardTimeLimit(final int hardTimeLimit) 
	{
		this.hardTimeLimit = hardTimeLimit;
	}

	public void setOutputPath(final String s) 
	{
		outputPath = s;
	}

	public void setCombinedResultsOutputPath(final String combinedResultsOutputPath) 
	{
		this.combinedResultsOutputPath = combinedResultsOutputPath;
	}

	public void setAddToDatabaseFile(final boolean addToDatabaseFile) 
	{
		this.addToDatabaseFile = addToDatabaseFile;
	}

	public void setCurrentDatabaseId(final int currentDatabaseId) 
	{
		this.currentDatabaseId = currentDatabaseId;
	}

	public String combinedResultsOutputPath() 
	{
		return combinedResultsOutputPath;
	}

	//-------------------------------------------------------------------------

}
