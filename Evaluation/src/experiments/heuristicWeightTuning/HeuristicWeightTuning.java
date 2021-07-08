package experiments.heuristicWeightTuning;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import experiments.utils.TrialRecord;
import game.Game;
import main.Constants;
import main.math.Stats;
import other.AI;
import other.GameLoader;
import other.context.Context;
import other.model.Model;
import other.trial.Trial;
import search.minimax.HeuristicSampling;

//-----------------------------------------------------------------------------

/**
 * Experiments to tune the weights of heuristics
 * 
 * @author matthew.stephenson and cambolbro
 */
public class HeuristicWeightTuning
{
	
	private final List<String> output = new ArrayList<>();
	
	/** Decimal format for printing. */
	private final static DecimalFormat df = new DecimalFormat("#.###");
	
	//-------------------------------------------------------------------------
	
	void test()
	{
		final Game game = GameLoader.loadGameFromName("Amazons.lud");
		
		output.clear();
		output.add("   [");
		output.add("      [ (" + game.name() + ") ]");
		
		try
		{
			lengthRandomParallel(game, 100);
			lengthHS(game, 2, false);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
		output.add("   ]");
		
		for (final String str : output)
			System.out.println(str);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param game Single game object shared between threads.
	 */
	final void lengthHS
	(
		final Game game, 
		final int fraction, final boolean continuation
	) throws Exception
	{
		final int MaxTrials = 100;
				
		final long startAt = System.nanoTime();
				
		AI aiA = null;
		AI aiB = null;
		
		//System.out.println("\nHS (1/" + fraction + ")" + (continuation ? "*" : "") + ".");
		final String label = "HS 1/" + fraction + (continuation ? "" : "-");
		System.out.println("\n" + label + ":");
				
		// Run trials concurrently
		final ExecutorService executor = Executors.newFixedThreadPool(MaxTrials);
		final List<Future<TrialRecord>> futures = new ArrayList<>(MaxTrials);
		
		final CountDownLatch latch = new CountDownLatch(MaxTrials);
			
		for (int t = 0; t < MaxTrials; t++)
		{
			final int starter = t % 2;
			
			final List<AI> ais = new ArrayList<>();
			ais.add(null);  // null placeholder for player 0
			
			final String heuristicsFilePath = "src/experiments/fastGameLengths/Heuristics_" + game.name() + "_Good.txt";
			aiA = new HeuristicSampling(heuristicsFilePath);
			aiB = new HeuristicSampling(heuristicsFilePath);
				
			((HeuristicSampling)aiA).setThreshold(fraction);
			((HeuristicSampling)aiB).setThreshold(fraction);

			((HeuristicSampling)aiA).setContinuation(continuation);
			((HeuristicSampling)aiB).setContinuation(continuation);
			
			if (t % 2 == 0)
			{
				ais.add(aiA);
				ais.add(aiB);
			}
			else
			{
				ais.add(aiB);
				ais.add(aiA);
			}
			
			futures.add
			(
				executor.submit
				(
					() -> 
					{
						final Trial trial = new Trial(game);
						final Context context = new Context(game, trial);
				
						game.start(context);
	
						for (int p = 1; p <= game.players().count(); ++p)
							ais.get(p).initAI(game, p);
	
						final Model model = context.model();
						while (!trial.over())
							model.startNewStep(context, ais, -1, -1, 1, 0);
	
						latch.countDown();
				
						return new TrialRecord(starter, trial);
					}
				)
			);
		}
		
		latch.await();  // wait for all trials to finish
		
		final double secs = (System.nanoTime() - startAt) / 1000000000.0;
		System.out.println("Heuristic Sampling (1/" + fraction + ") " + secs + "s (" + (secs / MaxTrials) + "s per game).");

		showResults(game, label, MaxTrials, futures, secs);
		
		executor.shutdown();
	}
	
	//-------------------------------------------------------------------------

	void showResults
	(
		final Game game, final String label, final int numTrials, 
		final List<Future<TrialRecord>> futures, final double secs
	) throws Exception
	{
		// Accumulate wins per player		
		final Stats stats = new Stats(label);
		final double[] results = new double[Constants.MAX_PLAYERS + 1];

		for (int t = 0; t < numTrials; t++)
		{
			final TrialRecord trialRecord = futures.get(t).get();
			final Trial trial = trialRecord.trial();
			
			final int length = trial.numMoves();
			
			//System.out.print((t == 0 ? "\n" : "") + length + " ");
			
			if (length < 1000)
				stats.addSample(trial.numMoves());
			
			final int result = trial.status().winner();  //futures.get(t).get().intValue();
			if (result == 0)
			{
				// Draw: share win
				results[0] += 0.5;
				results[1] += 0.5;
			}
			else
			{
				// Reward winning AI
				if (trialRecord.starter() == 0)
				{
					if (result == 1)
						results[0]++;
					else
						results[1]++;
				}
				else 
				{
					if (result == 1)
						results[1]++;
					else
						results[0]++;
				}
			}
			
			//System.out.println(trialRecord.starter() + " => " + trial.status().winner());
		}
				
		//System.out.println("\naiA=" + results[0] + ", aiB=" + results[1] + ".");
		System.out.println("aiA success rate " + results[0] / numTrials * 100 + "%.");  //+ ", aiB=" + results[1] + ".");
	
		stats.measure();
		stats.showFull();
		
		formatOutput(stats, numTrials, secs);

		//System.out.println("Expected length is " + (gameName.expected() == -1 ? "not known" : gameName.expected()) + ".");
	}
	
	//-------------------------------------------------------------------------

	double lengthRandomSerial(final Game game, final int numTrials)
	{
		final long startAt = System.nanoTime();
		
		final Trial refTrial = new Trial(game);
		final Context context = new Context(game, refTrial);
		
		final Stats stats = new Stats("Serial Random");
		
		//int totalLength = 0;
		for (int t = 0; t < numTrials; t++)
		{
			game.start(context);
			final Trial trial = game.playout(context, null, 1.0, null, -1, -1, ThreadLocalRandom.current());
			//totalLength += trial.numLogicalDecisions(game);
			stats.addSample(trial.numMoves());
		}
		stats.measure();
		
		final double secs = (System.nanoTime() - startAt) / 1000000000.0;

		stats.showFull();
		System.out.println("Serial in " + secs + "s.");
		
		return stats.mean();
	}

	double lengthRandomParallel(final Game game, final int numTrials) throws Exception
	{
		final long startAt = System.nanoTime();

		final ExecutorService executor = Executors.newFixedThreadPool(numTrials);
		final List<Future<Trial>> futures = new ArrayList<Future<Trial>>(numTrials);
	
		final CountDownLatch latch = new CountDownLatch(numTrials);
		
		for (int t = 0; t < numTrials; t++)
		{
			final Trial trial = new Trial(game);
			final Context context = new Context(game, trial);
			//trial.storeLegalMovesHistorySizes();
			
			futures.add
			(
				executor.submit
				(
					() -> 
					{
						game.start(context);
						game.playout(context, null, 1.0, null, -1, -1, ThreadLocalRandom.current());
						latch.countDown();
						return trial;
					}
				)
			);
		}
			
		latch.await();  // wait for all trials to finish
		
		// Accumulate lengths over all trials
		final String label = "Random";
		final Stats stats = new Stats(label);

		//double totalLength = 0;
		for (int t = 0; t < numTrials; t++)
		{
			final Trial trial = futures.get(t).get();
			stats.addSample(trial.numMoves());
		}

		stats.measure();
		
		final double secs = (System.nanoTime() - startAt) / 1000000000.0;
		
		stats.showFull();
		System.out.println("Random concurrent in " + secs + "s (" + (secs / numTrials) +"s per game).");
	
		formatOutput(stats, numTrials, secs);
	
		executor.shutdown();
		
		return stats.mean();
	}
	
	//-------------------------------------------------------------------------
	
	void formatOutput(final Stats stats, final int numTrials, final double secs)
	{
		output.add
		(
			"      [ (" + stats.label() + ") " + stats.n() + " " + df.format(stats.mean())
			+ 
			" " + (int)stats.min() + " " + (int)stats.max() 
			+ 
			" " + df.format(stats.sd()) + " " + df.format(stats.se()) + " " + df.format(stats.ci()) 
			+ 
			" " + df.format(secs / numTrials * 1000.0)
			+ 
			" ]"
		);
	}
	
	//-------------------------------------------------------------------------
	
	public static void main(final String[] args)
	{
		final HeuristicWeightTuning sd = new HeuristicWeightTuning();
		sd.test();
	}

	//-------------------------------------------------------------------------

}
