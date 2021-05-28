package analysis;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import game.types.play.RepetitionType;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import compiler.Compiler;
import main.grammar.Description;
import main.grammar.Report;
import main.options.UserSelections;
import other.context.Context;
import other.trial.Trial;

/**
 * Methods to estimate various complexity measures using random trials.
 *
 * @author Dennis Soemers and Matthew.Stephenson
 */
public class Complexity
{
	
	//-------------------------------------------------------------------------
	
	/**
 * Estimates average branching factor for the given game by running random trials
	 * for the given number of seconds.
	 * 
	 * @param gameResource Path for game's resource path
	 * @param numSeconds
	 * @return A map from Strings to doubles:	<br>
	 * 	"Avg Trial Branching Factor" --> estimated average branching factor per trial	<br>
	 * 	"Avg State Branching Factor" --> estimated average branching factor per state	<br>
	 * 	"Num Trials" --> number of trials over which we estimated
	 */
	public static TObjectDoubleHashMap<String> estimateBranchingFactor
	(
		final String gameResource, 
//		final GameOptions gameOptions, 
//		final int[] optionSelections,
		final UserSelections userSelections,
		final double numSeconds
	)
	{
		// WARNING: do NOT modify this method to directly take a compiled game as argument
		// we may have to modify the game object itself by getting rid of custom playout
		// implementations, because they cannot properly store all the data we need to store.
		
		// First compile our game
		String desc = "";
		
		// First try to load from memory
		try 
		(
			final BufferedReader rdr = new BufferedReader(new InputStreamReader(Complexity.class.getResourceAsStream(gameResource)))
		)
		{
			String line;
			while ((line = rdr.readLine()) != null)
				desc += line + "\n";
		}
		catch (final Exception e)
		{
			// Second try to load from file
			try 
			(
				final BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(gameResource), "UTF-8"));
			)
			{
				String line;
				while ((line = rdr.readLine()) != null)
					desc += line + "\n";
			}
			catch (final Exception e2)
			{
				e.printStackTrace();
			}
		}

		final Game game = (Game)Compiler.compile
						  (
							  new Description(desc), 
							  userSelections, 
							  new Report(),
							  false
						   );
		
		// Disable custom playout implementations if they cannot properly store 
		// history of legal moves per state.
		game.disableMemorylessPlayouts();
		
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		trial.storeLegalMovesHistorySizes();
		
		System.gc();
		
		long stopAt = 0L;
		final long start = System.nanoTime();
		final long abortAt = start + (long) Math.ceil(numSeconds * 1000000000L);
		int numTrials = 0;
		long numStates = 0L;
		
		long sumBranchingFactors = 0L;
		double sumAvgTrialBranchingFactors = 0.0;
		
		while (stopAt < abortAt)
		{
			game.start(context);
			final Trial endTrial = game.playout(context, null, 1.0, null, -1, -1, ThreadLocalRandom.current());
			final int numDecisions = endTrial.numMoves() - endTrial.numInitialPlacementMoves();
			
			long trialSumBranchingFactors = 0L;
			final TIntArrayList branchingFactors = endTrial.auxilTrialData().legalMovesHistorySizes();
			for (int i = 0; i < branchingFactors.size(); ++i)
			{
				trialSumBranchingFactors += branchingFactors.getQuick(i);
			}
			numStates += branchingFactors.size();
			
			sumBranchingFactors += trialSumBranchingFactors;
			sumAvgTrialBranchingFactors += trialSumBranchingFactors / (double) numDecisions;
			
			++numTrials;
			stopAt = System.nanoTime();
		}
		
		final TObjectDoubleHashMap<String> map = new TObjectDoubleHashMap<String>();
		map.put("Avg Trial Branching Factor", sumAvgTrialBranchingFactors / numTrials);
		map.put("Avg State Branching Factor", sumBranchingFactors / (double) numStates);
		map.put("Num Trials", numTrials);
		
		return map;
	}
	
	/**
	 * Estimates average game lengths for the given game by running random trials
	 * for the given number of seconds.
	 * 
	 * @param game
	 * @param numSeconds
	 * @return A map from Strings to doubles:													<br>
	 * 	"Avg Num Decisions" --> average number of decisions per trial							<br>
	 * 	"Avg Num Player Switches" --> average number of switches of player-to-move per trial	<br>
	 * 	"Num Trials" --> number of trials over which we estimated
	 */
	public static TObjectDoubleHashMap<String> estimateGameLength
	(
		final Game game,
		final double numSeconds
	)
	{
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		
		System.gc();
		
		long stopAt = 0L;
		final long start = System.nanoTime();
		final long abortAt = start + (long) Math.ceil(numSeconds * 1000000000L);
		int numTrials = 0;
		long numDecisions = 0L;
		long numPlayerSwitches = 0L;
		
		while (stopAt < abortAt)
		{
			game.start(context);
			final Trial endTrial = game.playout(context, null, 1.0, null, -1, -1, ThreadLocalRandom.current());
			numDecisions += endTrial.numMoves() - endTrial.numInitialPlacementMoves();
			numPlayerSwitches += (context.state().numTurn() - 1);
			++numTrials;
			stopAt = System.nanoTime();
		}
		
		final TObjectDoubleHashMap<String> map = new TObjectDoubleHashMap<String>();
		map.put("Avg Num Decisions", numDecisions / (double) numTrials);
		map.put("Avg Num Player Switches", numPlayerSwitches / (double) numTrials);
		map.put("Num Trials", numTrials);
		
		return map;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Estimates game tree complexity for the given game by running random trials
	 * for the given number of seconds.
	 * 
	 * @param gameResource Path for game's resource path
	 * @param userSelections
	 * @param numSeconds
	 * @param forceNoStateRepetitionRule If true, we force the game to use a No State Repetition rule
	 * @return A map from Strings to doubles:	<br>
	 * 	"Avg Num Decisions" --> average number of decisions per trial					<br>
	 * 	"Avg Trial Branching Factor" --> estimated average branching factor per trial	<br>
	 * 	"Estimated Complexity Power" --> power (which 10 should be raised to) of 
	 * 	estimated game tree complexity b^d												<br>
	 * 	"Num Trials" --> number of trials over which we estimated
	 */
	public static TObjectDoubleHashMap<String> estimateGameTreeComplexity
	(
		final String gameResource, 
		final UserSelections userSelections,
		final double numSeconds,
		final boolean forceNoStateRepetitionRule
	)
	{
		// WARNING: do NOT modify this method to directly take a compiled game as argument
		// we may have to modify the game object itself by getting rid of custom playout
		// implementations, because they cannot properly store all the data we need to store.
		
		// First compile our game
		String desc = "";
		
		// First try to load from memory
		try 
		(
			final BufferedReader rdr = new BufferedReader(new InputStreamReader(Complexity.class.getResourceAsStream(gameResource)))
		)
		{
			String line;
			while ((line = rdr.readLine()) != null)
				desc += line + "\n";
		}
		catch (final Exception e)
		{
			// Second try to load from file
			try 
			(
				final BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(gameResource), "UTF-8"));
			)
			{
				String line;
				while ((line = rdr.readLine()) != null)
					desc += line + "\n";
			}
			catch (final Exception e2)
			{
				e.printStackTrace();
			}
		}

		final Game game = (Game)Compiler.compile
						  (
							  new Description(desc), 
							  userSelections, 
							  new Report(),
							  false
						  );
		
		// disable custom playout implementations if they cannot properly store history of legal moves per state
		game.disableMemorylessPlayouts();
		
		if (forceNoStateRepetitionRule)
			game.metaRules().setRepetitionType(RepetitionType.Positional);
		
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		trial.storeLegalMovesHistorySizes();
		
		System.gc();
		
		long stopAt = 0L;
		final long start = System.nanoTime();
		final long abortAt = start + (long) Math.ceil(numSeconds * 1000000000L);
		int numTrials = 0;
		long sumNumDecisions = 0L;
		
		double sumAvgTrialBranchingFactors = 0.0;
		
		while (stopAt < abortAt)
		{
			game.start(context);
			final Trial endTrial = game.playout(context, null, 1.0, null, -1, -1, ThreadLocalRandom.current());
			final int numDecisions = endTrial.numMoves() - endTrial.numInitialPlacementMoves();
			
			long trialSumBranchingFactors = 0L;
			final TIntArrayList branchingFactors = endTrial.auxilTrialData().legalMovesHistorySizes();
			for (int i = 0; i < branchingFactors.size(); ++i)
			{
				trialSumBranchingFactors += branchingFactors.getQuick(i);
			}
			
			sumAvgTrialBranchingFactors += trialSumBranchingFactors / (double) numDecisions;
			sumNumDecisions += numDecisions;
			
			++numTrials;
			stopAt = System.nanoTime();
		}
		
		final TObjectDoubleHashMap<String> map = new TObjectDoubleHashMap<String>();
		final double d = sumNumDecisions / (double) numTrials;
		final double b = sumAvgTrialBranchingFactors / numTrials;
		map.put("Avg Num Decisions", d);
		map.put("Avg Trial Branching Factor", b);
		map.put("Estimated Complexity Power", d * Math.log10(b));
		map.put("Num Trials", numTrials);
		
		return map;
	}
	
	//-------------------------------------------------------------------------

}
