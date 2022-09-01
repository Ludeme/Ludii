package experiments.strategicDimension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import gnu.trove.list.array.TIntArrayList;
import other.GameLoader;
import other.context.Context;
import other.trial.Trial;

//-----------------------------------------------------------------------------

/**
 * Experiments to test Strategic Dimension.
 * @author cambolbro
 */
public class StrategicDimension
{
	static void test()
	{
		//final Game game = GameLoader.loadGameFromName("Tic-Tac-Toe.lud");
		//final Game game = GameLoader.loadGameFromName("Breakthrough.lud");
		//final Game game = GameLoader.loadGameFromName("Chess.lud");
		//final Game game = GameLoader.loadGameFromName("Amazons.lud");
		
		//final Game game = GameLoader.loadGameFromName("Breakthrough.lud", Arrays.asList("Board Size/4x4"));
		//final Game game = GameLoader.loadGameFromName("Breakthrough.lud", Arrays.asList("Board Size/5x5"));
		//final Game game = GameLoader.loadGameFromName("Breakthrough.lud", Arrays.asList("Board Size/6x6"));
		//final Game game = GameLoader.loadGameFromName("Breakthrough.lud", Arrays.asList("Board Size/7x7"));
		final Game game = GameLoader.loadGameFromName("Breakthrough.lud", Arrays.asList("Board Size/8x8"));
		//final Game game = GameLoader.loadGameFromName("Breakthrough.lud", Arrays.asList("Board Size/9x9"));
		//final Game game = GameLoader.loadGameFromName("Breakthrough.lud", Arrays.asList("Board Size/10x10"));
		//final Game game = GameLoader.loadGameFromName("Breakthrough.lud", Arrays.asList("Board Size/11x11"));
		//final Game game = GameLoader.loadGameFromName("Breakthrough.lud", Arrays.asList("Board Size/12x12"));

		//final Game game = GameLoader.loadGameFromName("Breakthrough (No AI).lud", Arrays.asList("Board Size/4x4"));
		//final Game game = GameLoader.loadGameFromName("Breakthrough (No AI).lud", Arrays.asList("Board Size/5x5"));
		//final Game game = GameLoader.loadGameFromName("Breakthrough (No AI).lud", Arrays.asList("Board Size/6x6"));
		//final Game game = GameLoader.loadGameFromName("Breakthrough (No AI).lud", Arrays.asList("Board Size/7x7"));
		//final Game game = GameLoader.loadGameFromName("Breakthrough (No AI).lud", Arrays.asList("Board Size/8x8"));
	
		System.out.println("Game " + game.name() + " loaded.");
		
//		game.disableMemorylessPlayouts();
		
		final double bf = branchingFactorParallel(game);
		System.out.println("Average branching factor is " + bf + ".");	
					
		// Run pairings
		//final List<Double> winRates = runEpochs(game, Double.valueOf(bf));
		final List<Double> winRates = runEpochs(game, null);
		System.out.println("Win rates are: " + winRates);
			
		// Estimate SD
		// ...
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param game Single game object shared between threads.
	 * @param bf   Average branching factor (null for AB search).
	 * @return     List of superior win rates for each epoch.
	 */
	final static List<Double> runEpochs(final Game game, final Double bf)
	{
		final int NUM_EPOCHS = 4;
		final int TRIALS_PER_EPOCH = 50;
		
		final boolean isMC = (bf != null);
		
		final List<Double> winRates = new ArrayList<>();
		
		int baseIterations = isMC ? (int)(bf.doubleValue() / 4 + 0.5) : 0;
		
		// Generate result for each pairing
		for (int epoch = 0; epoch < NUM_EPOCHS; epoch++)
		{
			final int lower = isMC ? baseIterations : epoch + 1;
			final int upper = isMC ? lower * 2 : lower + 1;
			
			System.out.println("\nEpoch " + epoch + ": " + lower + " vs " + upper + "...");

			// Run trials concurrently
			final List<Future<Double>> results = new ArrayList<>(TRIALS_PER_EPOCH);
			for (int t = 0; t < TRIALS_PER_EPOCH; t++)
			{
				final FutureTrial future = isMC ? new FutureTrialMC() : new FutureTrialAB();				
				results.add(future.runTrial(game, t, lower, upper));	
			}
			
			// Accumulate win rates for superior agent over all trials
			double winRate = 0;
			try 
			{
				for (int t = 0; t < TRIALS_PER_EPOCH; t++)
					winRate += results.get(t).get().doubleValue();
			}
			catch (final InterruptedException | ExecutionException e)
			{
				e.printStackTrace();
				//fail();
			}
			winRate /= TRIALS_PER_EPOCH;
			winRates.add(Double.valueOf(winRate));
			System.out.println("\nSuperior win rate is " + winRate + ".");
			
			// Step to next iteration
			baseIterations = upper;
		}
		
		return winRates;
	}
	
	//-------------------------------------------------------------------------

	static double branchingFactor(final Game game)
	{
		final int NUM_TRIALS = 10;
		
		//final long startAt = System.nanoTime();
		
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		
		int totalDecisions = 0;
		
		for (int t = 0; t < NUM_TRIALS; t++)
		{
			game.start(context);
			final Trial endTrial = game.playout(context, null, 1.0, null, -1, -1, ThreadLocalRandom.current());
			
			final int numDecisions = endTrial.numMoves() - endTrial.numInitialPlacementMoves();
			totalDecisions += numDecisions;
		}
		
		//final double secs = (System.nanoTime() - startAt) / 1000000000.0;
		//System.out.println("secs=" + secs);
		
		return totalDecisions / (double)NUM_TRIALS;
	}

	static double branchingFactorParallel(final Game game)
	{
		final int NUM_TRIALS = 10;
		
		//final long startAt = System.nanoTime();

		final ExecutorService executorService = Executors.newFixedThreadPool(NUM_TRIALS);
		final List<Future<Context>> playedContexts = new ArrayList<Future<Context>>(NUM_TRIALS);
	
		for (int t = 0; t < NUM_TRIALS; t++)
		{
			final Trial trial = new Trial(game);
			final Context context = new Context(game, trial);
			trial.storeLegalMovesHistorySizes();
			
			playedContexts.add
			(
				executorService.submit
				(
					() -> 
					{
						game.start(context);
						game.playout(context, null, 1.0, null, -1, -1, ThreadLocalRandom.current());
						return context;
					}
				)
			);
		}
			
		// Accumulate total BFs over all trials
		double totalBF = 0;
		try 
		{
			for (int t = 0; t < NUM_TRIALS; t++)
			{
				final Context context = playedContexts.get(t).get();
				final TIntArrayList branchingFactors = context.trial().auxilTrialData().legalMovesHistorySizes();
				
				int bfAcc = 0;
				for (int m = 0; m < branchingFactors.size(); m++)
					bfAcc += branchingFactors.getQuick(m);
				
				final double avgBfThisTrial = bfAcc / (double)branchingFactors.size();
				totalBF += avgBfThisTrial;
			}
		}
		catch (final InterruptedException | ExecutionException e)
		{
			e.printStackTrace();
			//fail();
		}
		
		//final double secs = (System.nanoTime() - startAt) / 1000000000.0;
		//System.out.println("secs=" + secs);
		
		return totalBF / NUM_TRIALS;
	}	
	
	//-------------------------------------------------------------------------
	
	public static void main(String[] args)
	{
		StrategicDimension.test();
	}

	//-------------------------------------------------------------------------

}
