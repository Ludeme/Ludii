package experiments.fastGameLengths;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import main.Status;
import main.math.statistics.Stats;
import other.AI;
import other.GameLoader;
import other.context.Context;
import other.model.Model;
import other.trial.Trial;
import search.flat.HeuristicSampling;
import search.mcts.MCTS;
import search.mcts.backpropagation.MonteCarloBackprop;
import search.mcts.finalmoveselection.RobustChild;
import search.mcts.playout.HeuristicPlayout;
import search.mcts.selection.UCB1;
import search.minimax.AlphaBetaSearch; 

//-----------------------------------------------------------------------------

/**
 * Experiments to test Heuristic Sampling for fast game length estimates.
 * @author cambolbro
 */
public class FastGameLengths
{
	// Expected game lengths are from the Game Complexity wikipedia page:
	// https://en.wikipedia.org/wiki/Game_complexity
	// -1 means average game length is not know for this game.
		
	private final List<String> output = new ArrayList<>();
	
	/** Decimal format for printing. */
	private final static DecimalFormat df = new DecimalFormat("#.###");
	
	public enum GameName
	{
		//ArdRi(2, -1),           
		Breakthrough(2, -1),    
		//Hnefatafl(2, -1),       
		//Oware(3, 60),           
		//Tablut(4, -1),          
		//Reversi(2, 58),         
		//Quoridor(1, 150), 
		//Go(1, 150), 
		//Hex(2, 50), 
		//Connect6(2, 30), 
		//Domineering(2, 30),     
		//Amazons(3, 84),         
		//Fanorona(2, 44),        
		//Yavalath(4, -1), 
		NineMensMorris(3, 50),  
		TicTacToe(3, 9),       
		ConnectFour(3, 36),     
		EnglishDraughts(3, 70), 
		GoMoku(3, 30),          
		LinesOfAction(3, 44),   
		Halma(3, -1),           
		Chess(3, 70), 
		Shogi(3, 115), 
		;

		//-------------------------------------

		private int depth = 0;      // search depth 
		private int expected = -1;  // expected length
		
		//-------------------------------------
		
		private GameName(final int depth, final int expected)
		{
			this.depth    = depth;
			this.expected = expected;
		}

		//-------------------------------------

		public int depth()
		{
			return depth;
		}
		
		public int expected()
		{
			return expected;
		}
	}
	
	//-------------------------------------------------------------------------
		
	void test()
	{
		//test(GameName.NineMensMorris);

		for (final GameName gameName : GameName.values())
		{
			//if (gameName.ordinal() >= GameName.EnglishDraughts.ordinal())
			//if (gameName.ordinal() >= GameName.Shogi.ordinal())
			if (gameName.ordinal() >= GameName.Breakthrough.ordinal())
				test(gameName);
			//break;
		}		
	}
	
	void test(final GameName gameName)
	{
		Game game = null;
		
		switch (gameName)
		{
		case NineMensMorris:
			game = GameLoader.loadGameFromName("Nine Men's Morris.lud");
			break;
		case Chess:
			game = GameLoader.loadGameFromName("Chess.lud");
			break;
		case ConnectFour:
			game = GameLoader.loadGameFromName("Connect Four.lud");
			break;
		case EnglishDraughts:
			game = GameLoader.loadGameFromName("English Draughts.lud");
			break;
		case GoMoku:
			game = GameLoader.loadGameFromName("GoMoku.lud");
			break;
		case Halma:
			game = GameLoader.loadGameFromName("Halma.lud", Arrays.asList("Board Size/6x6"));
			break;
		case Breakthrough:
			game = GameLoader.loadGameFromName("Breakthrough.lud", Arrays.asList("Board Size/6x6"));
			break;
		case LinesOfAction:
			game = GameLoader.loadGameFromName("Lines of Action.lud");
			break;
		case Shogi:
			game = GameLoader.loadGameFromName("Shogi.lud");
			break;
		case TicTacToe:
			game = GameLoader.loadGameFromName("Tic-Tac-Toe.lud");
			break;
		}
		
		System.out.println("==================================================");
		System.out.println("Loaded game " + game.name() + ", " + gameName.expected() + " moves expected.");
		
		output.clear();
		
		output.add("   [");
		output.add("      [ (" + game.name() + ") " + gameName.expected() + " ]");
		
		try
		{
//			lengthRandomParallel(game, 100);
//	
//			System.out.println("BF (parallel) = " + branchingFactorParallel(game, 10));
					
			int threshold = 2; 
			for (int hs = 0; hs < 4; hs++)
			{
				lengthHS(gameName, game, threshold, true);
				threshold *= 2;
			}

//			if 
//			(
//				gameName == GameName.NineMensMorris
//				||
//				gameName == GameName.EnglishDraughts
//			)
//			{
//				// Repeat without HS continuation
//				threshold = 2; 
//				for (int hs = 0; hs < 4; hs++)
//				{
//					lengthHS(gameName, game, threshold, false);
//					threshold *= 2;
//				}
//			}
//
//			for (int depth = 1; depth <= 2; depth++)
//			//for (int depth = 1; depth <= gameName.depth(); depth++)
//				lengthAlphaBeta(gameName, game, depth);

			lengthAlphaBeta(gameName, game, 3);
			
			lengthUCT(gameName, game, 1000);
	
			//compareUCThs(gameName, game, 1000);
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

	@SuppressWarnings("static-method")
	public int gameLength(final Trial trial, final Game game)
	{
		//return trial.numLogicalDecisions(game);
		//return trial.numMoves() - trial.numForcedPasses();
		return trial.numTurns() - trial.numForcedPasses();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param game Single game object shared between threads.
	 */
	void compareUCThs
	(
		final GameName gameName, final Game game, final int iterations
	) throws Exception
	{
		final int MaxTrials = 1000;
	
		final long startAt = System.nanoTime();
				
		AI aiA = null;
		AI aiB = null;
		
		System.out.println("\nUCT (" + iterations + " iterations).");
				
		// Run trials concurrently
		final ExecutorService executor = Executors.newFixedThreadPool(MaxTrials);
		final List<Future<TrialRecord>> futures = new ArrayList<>(MaxTrials);
		
		final CountDownLatch latch = new CountDownLatch(MaxTrials);
			
		for (int t = 0; t < MaxTrials; t++)
		{
			final int starter = t % 2;
			
			final List<AI> ais = new ArrayList<>();
			ais.add(null);  // null placeholder for player 0
			
			final String heuristicsFilePath = "src/experiments/fastGameLengths/Heuristics_" + gameName + "_Good.txt";
			try
			{				
				//aiA = new AlphaBetaSearch(heuristicsFilePath);
				aiA = MCTS.createUCT();
				
				aiB = new MCTS
					  (
						  new UCB1(),
						  new HeuristicPlayout(heuristicsFilePath),
						  new MonteCarloBackprop(),
						  new RobustChild()
					  );
				aiB.setFriendlyName("UCThs");
			} 
			catch (final Exception e)
			{
				e.printStackTrace();
			}  
			
			if (starter == 0)
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
							model.startNewStep(context, ais, -1, iterations, -1, 0);
	
						final Status status = context.trial().status();
						System.out.print(status.winner());	
				
						latch.countDown();
				
						return new TrialRecord(starter, trial);
					}
				)
			);
		}
		
		latch.await();  // wait for all trials to finish
		
		final double secs = (System.nanoTime() - startAt) / 1000000000.0;
		System.out.println("UCT (" + iterations + ") " + secs + "s (" + (secs / MaxTrials) + "s per game).");

		showResults(game, "UCThs Results", MaxTrials, futures, secs);
		
		executor.shutdown();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param game Single game object shared between threads.
	 */
	void lengthUCT
	(
		final GameName gameName, final Game game, final int iterations
	) throws Exception
	{
		final int MaxTrials = 10;  //100;  //10;
	
		final long startAt = System.nanoTime();
				
		AI aiA = null;
		AI aiB = null;
		
		System.out.println("\nUCT (" + iterations + " iterations).");
				
		// Run trials concurrently
		final ExecutorService executor = Executors.newFixedThreadPool(MaxTrials);
		final List<Future<TrialRecord>> futures = new ArrayList<>(MaxTrials);
		
		final CountDownLatch latch = new CountDownLatch(MaxTrials);
			
		for (int t = 0; t < MaxTrials; t++)
		{
			final int starter = t % 2;
			
			final List<AI> ais = new ArrayList<>();
			ais.add(null);  // null placeholder for player 0
			
			aiA = MCTS.createUCT();
			aiB = MCTS.createUCT();
			
			if (starter == 0)
			{
				ais.add(aiA);
				ais.add(aiB);
			}
			else
			{
				ais.add(aiB);
				ais.add(aiA);
			}
			
			//futures.add(future.runTrial(executor, game, ais, starter, iterations));	
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
							model.startNewStep(context, ais, -1, iterations, -1, 0);
	
						final Status status = context.trial().status();
						System.out.print(status.winner());	
				
						latch.countDown();
				
						return new TrialRecord(starter, trial);
					}
				)
			);
		}
		
		latch.await();  // wait for all trials to finish
				
		final double secs = (System.nanoTime() - startAt) / 1000000000.0;
		System.out.println("\nUCT (" + iterations + ") " + secs + "s (" + (secs / MaxTrials) + "s per game).");
	
		showResults(game, "UCT", MaxTrials, futures, secs);
		
		executor.shutdown();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param game Single game object shared between threads.
	 */
	final void lengthHS
	(
		final GameName gameName, final Game game, 
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
			
			final String heuristicsFilePath = "src/experiments/fastGameLengths/Heuristics_" + gameName + "_Good.txt";
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
	
	/**
	 * @param game Single game object shared between threads.
	 */
	void lengthAlphaBeta
	(
		final GameName gameName, final Game game, final int depth
	) throws Exception
	{
		final int MaxTrials = 10;  //100;
		
		final long startAt = System.nanoTime();
				
		AI aiA = null;
		AI aiB = null;
		
		final String label = "AB " + depth;
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
			
			final String heuristicsFilePath = "src/experiments/fastGameLengths/Heuristics_" + gameName + "_Good.txt";
			aiA = new AlphaBetaSearch(heuristicsFilePath);
			aiB = new AlphaBetaSearch(heuristicsFilePath);
			//aiB = new AlphaBetaSearch("src/experiments/fastGameLengths/Heuristics_Tablut_Current.txt");
			
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
							model.startNewStep(context, ais, -1, -1, depth, 0);
			
						latch.countDown();
			
						return new TrialRecord(starter, trial);
					}
				)
			);
		}
		
		latch.await();  // wait for all trials to finish
		
		
		final double secs = (System.nanoTime() - startAt) / 1000000000.0;
		System.out.println("Alpha-Beta (" + depth + ") in " + secs + "s (" + (secs / MaxTrials) + "s per game).");

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
			
			final int length = gameLength(trial, game);
			
			//System.out.print((t == 0 ? "\n" : "") + length + " ");
			
			if (length < 1000)
				stats.addSample(gameLength(trial, game));
			
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
			stats.addSample(gameLength(trial, game));
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
			stats.addSample(gameLength(trial, game));
		}

		stats.measure();
		
		final double secs = (System.nanoTime() - startAt) / 1000000000.0;
		
		stats.showFull();
		System.out.println("Random concurrent in " + secs + "s (" + (secs / numTrials) +"s per game).");
	
		formatOutput(stats, numTrials, secs);
	
		executor.shutdown();
		
		return stats.mean();
	}

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

//	double branchingFactorSerial(final Game game, final int numTrials)
//	{
//		final long startAt = System.nanoTime();
//		
//		final Trial trial = new Trial(game);
//		final Context context = new Context(game, trial);
//		
//		int totalDecisions = 0;
//		
//		for (int t = 0; t < numTrials; t++)
//		{
//			game.start(context);
//			final Trial endTrial = game.playout(context, null, 1.0, null, -1, -1, ThreadLocalRandom.current());
//			final int numDecisions = endTrial.numMoves() - endTrial.numInitialPlacementMoves();
//			totalDecisions += numDecisions;
//		}
//		
//		final double secs = (System.nanoTime() - startAt) / 1000000000.0;
//		System.out.println("BF serial in " + secs + "s.");
//		
//		return totalDecisions / (double)numTrials;
//	}

	static double branchingFactorParallel
	(
		final Game game, final int numTrials
	) throws Exception
	{
		//final long startAt = System.nanoTime();

		// Disable custom playouts that cannot properly store history of legal moves per state
		game.disableMemorylessPlayouts();
						
		final ExecutorService executor = Executors.newFixedThreadPool(numTrials);
		final List<Future<Trial>> futures = new ArrayList<Future<Trial>>(numTrials);
	
		final CountDownLatch latch = new CountDownLatch(numTrials);

		for (int t = 0; t < numTrials; t++)
		{
			final Trial trial = new Trial(game);
			final Context context = new Context(game, trial);
			trial.storeLegalMovesHistorySizes();
			
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

		// Accumulate total BFs over all trials
		double totalBF = 0;
		for (int t = 0; t < numTrials; t++)
		{
			final Trial trial = futures.get(t).get();

			//final Trial trial = playedContexts.get(t).get();
			final TIntArrayList branchingFactors = trial.auxilTrialData().legalMovesHistorySizes();
				
			double bfAcc = 0;
			if (branchingFactors.size() > 0)
			{
				for (int m = 0; m < branchingFactors.size(); m++)
					bfAcc += branchingFactors.getQuick(m);
				bfAcc /= branchingFactors.size();
			}
			totalBF += bfAcc;
		}
		
		//final double secs = (System.nanoTime() - startAt) / 1000000000.0;
		//System.out.println("secs=" + secs);
		
		executor.shutdown();
		
		return totalBF / numTrials;
	}	
	
	//-------------------------------------------------------------------------
	
	public static void main(final String[] args)
	{
		final FastGameLengths sd = new FastGameLengths();
		sd.test();
	}

	//-------------------------------------------------------------------------

}
