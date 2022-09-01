package supplementary.experiments;

//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//import java.util.concurrent.ThreadLocalRandom;
//
//import game.Game;
//import gnu.trove.list.array.TFloatArrayList;
//import main.collections.FVector;
//import main.collections.FastArrayList;
//import main.collections.Pair;
//import metadata.ai.heuristics.Heuristics;
//import metadata.ai.heuristics.terms.CentreProximity;
//import metadata.ai.heuristics.terms.ComponentValues;
//import metadata.ai.heuristics.terms.CornerProximity;
//import metadata.ai.heuristics.terms.CurrentMoverHeuristic;
//import metadata.ai.heuristics.terms.HeuristicTerm;
//import metadata.ai.heuristics.terms.Influence;
//import metadata.ai.heuristics.terms.InfluenceAdvanced;
//import metadata.ai.heuristics.terms.Intercept;
//import metadata.ai.heuristics.terms.LineCompletionHeuristic;
//import metadata.ai.heuristics.terms.Material;
//import metadata.ai.heuristics.terms.MobilityAdvanced;
//import metadata.ai.heuristics.terms.MobilitySimple;
//import metadata.ai.heuristics.terms.OwnRegionsCount;
//import metadata.ai.heuristics.terms.PlayerRegionsProximity;
//import metadata.ai.heuristics.terms.PlayerSiteMapCount;
//import metadata.ai.heuristics.terms.RegionProximity;
//import metadata.ai.heuristics.terms.Score;
//import metadata.ai.heuristics.terms.SidesProximity;
//import metadata.ai.heuristics.terms.ThreatenedMaterial;
//import metadata.ai.heuristics.terms.ThreatenedMaterialMultipleCount;
//import metadata.ai.heuristics.terms.UnthreatenedMaterial;
//import other.AI;
//import other.GameLoader;
//import other.RankUtils;
//import other.context.Context;
//import other.move.Move;
//import other.state.State;
//import other.trial.Trial;
//import search.minimax.BiasedUBFM;
//import search.minimax.UBFM;
//import utils.data_structures.transposition_table.TranspositionTableUBFM;
//import utils.data_structures.transposition_table.TranspositionTableUBFM.UBFMTTData;
////import weka.classifiers.Classifier;
//import weka.core.Instances;
//import weka.core.Utils;
//import weka.core.converters.ArffLoader;
//import weka.classifiers.Evaluation;
//import weka.classifiers.functions.LinearRegression;

/**
 * Script to learn a set of heuristic terms and weight for a game, for it to be
 * used by minimax based algorithm like Alpha-Beta search or UBFM.
 * 
 * @author cyprien
 */

public class HeuristicsTraining
{
//	
//	private final boolean debugDisplays = true;
//	
//	/** Duration of the whole training in seconds: */
//	private final double trainingDuration = 14400.;
//	
//	/** Thinking time of the AI in seconds: */
//	private final float thinkingTime = 0.3f;
//	
//	/** Path of the directory with the files: */
//	private static final String repository = "/data/learning/";
//	
//	private final String trainDataFile = "training.arff";
//	private final String testDataFile = "testing.arff";
//	
//	private final StringBuffer trainDataARFF = new StringBuffer();
//	private final StringBuffer testDataARFF  = new StringBuffer();
//	
//	/** Probability that an entry gets assigned to the testing data set: */
//	private final double proportionOfTestingEntries = 0.15;
//	
//	/** Score assosciated to a win: */
//	private final float scoreWin = 20f;
//	
//	/** Number of playouts before the weights are updated */
//	private final int numPlayoutsPerIteration = 40;
//	
//	/** Numbre of turns after which the games are stopped */
//	private int maxNbTurns = 40;
//	
//	/** Probability of discarding an entry: */
//	private final double probabilityDiscard = 0.9;
//	
//	/** Threhshold of the heuristics weights (under it thy are not saved) */
//	private final float heuristicWeightsThreshold = 0.001f;
//	
//	//-------------------------------------------------------------------------
//	
//	/** The different heuristic terms (with a weight of 1) */
//	final List<HeuristicTerm> heuristicTermsList;
//	
//	final int nbParameters;
//	
//	private Game game;
//	
//	int numPlayers;
//	
//	/** Total number of calls of the recursive function to fill training data */
//	int fillTrainingCallNumber;
//
//	// Thread executor (maximum number of threads possible)
//	final static int numThreads = Runtime.getRuntime().availableProcessors();
//	final ExecutorService executor = Executors.newFixedThreadPool(numThreads); // note: was static on the previous file
//	
//	//-------------------------------------------------------------------------
//	
//	public HeuristicsTraining(String gameName)
//	{		
//		game = GameLoader.loadGameFromName(gameName+".lud");
//		numPlayers = game.players().count();
//		
//		heuristicTermsList = createHeuristicTerms(game);
//		
//		int nbParams = 0;
//		for (HeuristicTerm heuristicTerm : heuristicTermsList)
//		{
//			heuristicTerm.init(game);
//			final FVector params = heuristicTerm.paramsVector();
//			if (params != null)
//				nbParams += params.dim();
//			else
//				nbParams += 1;
//		}
//		this.nbParameters = nbParams;
//		if (debugDisplays)
//			System.out.println("Number of parameters : "+Integer.toString(nbParameters));
//	}
//	
//	//-------------------------------------------------------------------------
//
//	
//	public static void main (String[] args)
//	{
//		String gameName;
//		if (args.length > 0)
//			gameName = args[0];
//		else
//			gameName = "Breakthrough";
//		
//		HeuristicsTraining heuristicTraining = new HeuristicsTraining(gameName);
//
//		try {
//			heuristicTraining.runHeuristicLearning();	
//		} catch (final Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	protected void runHeuristicLearning () throws Exception
//	{
//		final long startTime = System.currentTimeMillis();
//		final long stopTime = startTime + (long) (trainingDuration * 1000);
//		
//		final int nbHeuristics = heuristicTermsList.size();
//		
//		final Heuristics heuristics = new Heuristics(heuristicTermsList.toArray(new HeuristicTerm[0]));
//		
//		final List<Double> correlationCoeficients = new ArrayList<Double>();
//		
//		final FVector heuristicWeights = heuristics.paramsVector();
//		
//		int numIteration = 0;
//		
//		while (System.currentTimeMillis() < stopTime)
//		{
//			
//			//-----------------------------------------------------------------
//
//			// Initialising the training data files:
//			String[] dataFileNames = new String[] {trainDataFile,testDataFile};
//			StringBuffer[] dataFileContents = new StringBuffer[] {trainDataARFF, testDataARFF};
//			for (int k : new int[] {0,1})
//			{
//				dataFileContents[k].setLength(0);
//				dataFileContents[k].append("@relation Heuristics_score_differences_and_value\n\n");
//				
//				// Filling the heuristic function's weight:
//				dataFileContents[k].append("@attribute value NUMERIC\n");
//				
//				for (int i=0; i<nbHeuristics; i++)
//				{
//					FVector params = heuristicTermsList.get(i).paramsVector();
//					if (params != null)
//						for (int j=0; j<params.dim(); j++)
//							dataFileContents[k].append("@attribute "+heuristicTermsList.get(i).getClass().getSimpleName()+Integer.toString(i)+"_"+Integer.toString(j)+" NUMERIC\n");
//					else
//						dataFileContents[k].append("@attribute "+heuristicTermsList.get(i).getClass().getSimpleName()+" NUMERIC\n");
//				}
//				dataFileContents[k].append("\n@data\n");
//			}
//			
//			heuristics.updateParams(game, heuristicWeights, 0);
//
//			System.out.println("Heuristics:");
//			System.out.println(heuristics.toString());
//			System.out.println(heuristics.paramsVector().toString());
//
//			fillTrainingCallNumber = 0;
//			
//			// Run trials concurrently
//			final CountDownLatch latch = new CountDownLatch(numPlayoutsPerIteration);
//
//			final List<Future<Pair<String,String>>> futures = new ArrayList<Future<Pair<String,String>>>(numPlayoutsPerIteration);
//			
//			for (int k=0; k<numPlayoutsPerIteration; k++)
//			{
//				final int index = k;
//				final int iterationNumber = numIteration;
//				
//				futures.add
//				(
//					executor.submit
//					(
//						() -> 
//						{
//							try
//							{
//								// Generating training set for the linear regression:
//								System.out.printf("\nBeginning the playout number %d of iteration %d:\n",index,iterationNumber);
//								
//								final Pair<String,String> res = generateATrainingSet(heuristics, 1+(index%2));
//								
//								latch.countDown();
//								
//								return res;
//							}
//							catch (final Exception e)
//							{
//								e.printStackTrace();
//								return new Pair<String,String>("","");
//							}
//						}
//					)
//				);
//			}
//			
//			latch.await(); // wait for all trials to finish
//			
//			Pair<String,String> outputs;
//			for (int n=0; n<numPlayoutsPerIteration; ++n)
//			{	
//				outputs = futures.get(n).get();
//				
//				trainDataARFF.append(outputs.key());
//				testDataARFF.append(outputs.value());
//			}
//
//			// Saving data in a file
//			for (int i : new int[] {0,1})
//			{
//				try {
//				  File directory = new File(String.valueOf(repository+"training/"));
//				  directory.mkdirs();
//			      FileWriter myWriter = new FileWriter(repository+"training/"+dataFileNames[i]);
//			      myWriter.write(dataFileContents[i].toString());
//			      myWriter.close();
//			    } catch (IOException e) {
//			      System.out.println("An error occurred.");
//			      e.printStackTrace();
//			    }
//			}
//			
//			//-----------------------------------------------------------------
//			// Updating the heuristicWeights with Machine Learning:
//			System.out.println("\nUpdating the heuristicWeights with Machine Learning:");
//			
//			
//			Instances trainDataSet = loadDataSet(trainDataFile);
//			Instances testDataSet = loadDataSet(testDataFile);
//			
//			LinearRegression classifier = new LinearRegression();
//			
//			//classifier.setOptions(Utils.splitOptions("-S 1"));
//			
//			// Training:
//			classifier.buildClassifier(trainDataSet);
//			
//			for (int i=0; i<nbParameters; i++)
//				heuristicWeights.set(i, (float) classifier.coefficients()[i+1]);
//
//			// Evaluating the classifier:
//			Evaluation eval = new Evaluation(trainDataSet);
//			eval.evaluateModel(classifier, testDataSet);
//			
//			correlationCoeficients.add(eval.correlationCoefficient());
//			
//			System.out.println("** Linear Regression Evaluation with Datasets **");
//			System.out.println(eval.toSummaryString());
//			System.out.print(" the expression for the input data as per alogorithm is ");
//			System.out.println(classifier);
//			
//			//---------------------------------------------------------------------
//			// Saving weights in a file:
//			try {
//			  File directory = new File(String.valueOf(repository+"/learned_heuristics/"));
//			  directory.mkdirs();
//		      FileWriter myWriter = new FileWriter(repository+"/learned_heuristics/"+game.name()+Integer.toString(numIteration)+".sav");
//		      myWriter.write(heuristics.toStringThresholded(heuristicWeightsThreshold));
//		      myWriter.close();
//			} catch (IOException e) {
//		      System.out.println("An error occurred.");
//		      e.printStackTrace();
//			}
//			try {
//				  File directory = new File(String.valueOf(repository+"/parameters/"));
//				  directory.mkdirs();
//			      FileWriter myWriter = new FileWriter(repository+"/parameters/"+game.name()+Integer.toString(numIteration)+".sav");
//			      myWriter.write(heuristics.paramsVector().toString());
//			      myWriter.close();
//				} catch (IOException e) {
//			      System.out.println("An error occurred.");
//			      e.printStackTrace();
//				}
//			try {
//		      FileWriter myWriter = new FileWriter(repository+"correlation_coeficients_"+game.name()+".sav");
//		      myWriter.write(toString(correlationCoeficients));
//		      myWriter.close();
//			} catch (IOException e) {
//		      System.out.println("An error occurred.");
//		      e.printStackTrace();
//			}
//			
//			numIteration += 1;
//			
////			We consider that we have generated enough heuristic parameters after 75 iterations
//			if (numIteration > 75)
//				break;
//		};
//		System.out.println("Heuristic training done.");
//		
//	}
//	
//	public static Instances loadDataSet(String fileName) throws IOException
//	{
//		ArffLoader loader = new ArffLoader();
//		
//		loader.setSource(new File(repository + "training/"+fileName));
//		
//		Instances dataSet = loader.getDataSet();
//		
//		dataSet.setClassIndex(0); //argument is the index of score in the training entries
//		
//		return dataSet;
//	}
//
//	/**
//	 * Uses a transposition table to calculate the value associated to states and fill the training data.
//	 * Takes the value directly from the TT, and ignores leaves which are are not terminal states.
//	 * 
//	 * @param TT
//	 * @return 
//	 */
//	protected Pair<String, String> generateATrainingSet (final Heuristics heuristics, final int maximisingPlayer)
//	{
//		// Setting up a playout:
//		final Trial trial = new Trial(game);
//		final Context context = new Context(game, trial);
//		
//		final UBFM AI = new UBFM(heuristics);
//		
//		AI.setIfFullPlayouts(true); // switches to the "Descent" algorithm
////		AI.setNbStateEvaluationsPerNode(6);
//		AI.setSelectionEpsilon(0.3f);
//		AI.savingSearchTreeDescription = false;
//		AI.debugDisplay = false;
//		AI.forceAMaximisingPlayer(maximisingPlayer);
//		AI.setTTReset(false);
//
//		final List<AI> agents = new ArrayList<>();
//		agents.add(null);
//		agents.add(AI);
//		agents.add(AI);
//	
//		game.start(context);
//		
//		AI.initAI(game, maximisingPlayer);
//
//		final Context initialContext = AI.copyContext(context); // used later
//		
//		game.playout(context, agents, thinkingTime, null, -1, maxNbTurns, ThreadLocalRandom.current()); // TODO: change max nb playout actions
//		
//		System.out.println("done");
//		
//
//		System.out.println("Result of maximising player: "+Double.toString(RankUtils.agentUtilities(context)[maximisingPlayer]));
//		
//		if (debugDisplays)
//		{
//			System.out.println("\nGenerating training set for the linear regression:\n");
//		}
//		
//		System.out.println("Nubmer of entries in the TT: "+Integer.toString(AI.getTranspositionTable().nbEntries()));
//		
//		final long zobrist = initialContext.state().fullHash(initialContext);
//		final UBFMTTData tableData = AI.getTranspositionTable().retrieve(zobrist);
//		
//		// Marking the value in the TT so that it is not visited again in the same recursive call
//		AI.getTranspositionTable().store(tableData.fullHash, tableData.value, tableData.depth, TranspositionTableUBFM.MARKED, tableData.sortedScoredMoves);
//
//		final StringBuffer trainDataEntries = new StringBuffer();
//		final StringBuffer testDataEntries = new StringBuffer();
//		
//		float rootValue = fillTrainingData(AI, initialContext, trainDataEntries, testDataEntries, maximisingPlayer);
//		
//		if (debugDisplays)
//		{
//			System.out.println();
//			System.out.printf("Recursive calls of the function to fill the training data: %d \n", fillTrainingCallNumber);
//		}
//		
//		// Validating the value in the TT so that it is not visited again
//		AI.getTranspositionTable().store(tableData.fullHash, rootValue, tableData.depth, TranspositionTableUBFM.VALIDATED, tableData.sortedScoredMoves);
//				
//		return new Pair<String,String>(trainDataEntries.toString(), testDataEntries.toString());
//	}
//	
//	/**
//	 * Recursive function to fill the training data.
//	 * The value of a state is its value in the search tree ("tree learning").
//	 * The leaves of the search tree which are not terminal game states are ignored.
//	 * 
//	 * @param TT
//	 * @param context
//	 */
//	protected float fillTrainingData
//	(
//		final UBFM AI,
//		final Context context,
//		final StringBuffer trainingEntries,
//		final StringBuffer testingEntries,
//		final int maximisingPlayer
//	)
//	{
//		
//		// Recursive call:
//		fillTrainingCallNumber += 1;
//		
//		if (debugDisplays && (fillTrainingCallNumber % 10000 == 0))
//		{
//			System.out.print(Integer.toString(fillTrainingCallNumber)+"...");
//			//System.out.println("Number of validate entries:"+Integer.toString(AI.transpositionTable.nbMarkedEntries()));
//			//System.out.println();
//		}
//		
//		boolean registeringValue = false;
//		float value = Float.NaN;
//		
//		if (context.trial().over() || !context.active(maximisingPlayer))
//		{
//			// terminal node (at least for maximising player)
//			value = (float) RankUtils.agentUtilities(context)[maximisingPlayer];
//			registeringValue = true;
//			//System.out.println("terminal node");
//		}
//		else
//		{
//			final State state = context.state();
//			final long zobrist = state.fullHash(context);
//			final UBFMTTData tableData = AI.getTranspositionTable().retrieve(zobrist);
//			
//			if (tableData.sortedScoredMoves != null)
//			{
//				registeringValue = true;
//				
//				value = tableData.value;
//				
//				final FastArrayList<Move> legalMoves = game.moves(context).moves();
//				final int nbMoves = legalMoves.size();
////				final int mover = state.playerToAgent(state.mover());
//				
//				final TFloatArrayList childrenValues = new TFloatArrayList();
//	
//				for (int i=0; i<nbMoves; i++)
//				{
//					final Context contextCopy = AI.copyContext(context);
//					final Move move = legalMoves.get(i);
//					
//					game.apply(contextCopy, move);
//					
//					final State newState = contextCopy.state();
//					final long newZobrist = newState.fullHash(contextCopy);
//					final UBFMTTData newTableData = AI.getTranspositionTable().retrieve(newZobrist);
//					
//					if (newTableData != null)
//					{
//						if (newTableData.valueType != TranspositionTableUBFM.MARKED)
//						{
//							if (newTableData.valueType != TranspositionTableUBFM.VALIDATED)
//							{
//								
//								// Marking the value in the TT so that it is not visited again in the same recursive call
//								AI.getTranspositionTable().store(	newTableData.fullHash, newTableData.value,
//																	newTableData.depth, TranspositionTableUBFM.MARKED,
//																	newTableData.sortedScoredMoves);
//								
//								final float childValue = fillTrainingData(AI, contextCopy, trainingEntries, testingEntries, maximisingPlayer);
//								
//								// Un-marking the value in the TT so that it is not considered as a draw if encountered elsewhere in the tree
//								AI.getTranspositionTable().store(	newTableData.fullHash, childValue, 
//																	newTableData.depth, TranspositionTableUBFM.VALIDATED, 
//																	newTableData.sortedScoredMoves);
//							}
//							childrenValues.add(tableData.value);
//						}
//						else
//						{
//							// we consider that one can always chose to go back to a previously visited state, 
//							// so it is a draw as far as this path is concerned
//							childrenValues.add(0f);
//						}
//					}
//				}
//			}
//			
//			if (value > scoreWin)
//				value = scoreWin;
//			else if (value < -scoreWin)
//				value = -scoreWin;
//			
//			//-----------------------------------------------------------------
//			// Adding entry with heuristic value differences:
//			
//			if ((registeringValue) && (Math.random()>probabilityDiscard))
//			{
//				
//				StringBuffer dataFileContent;
//				if (Math.random()>proportionOfTestingEntries)
//					dataFileContent = trainingEntries;
//				else
//					dataFileContent = testingEntries;
//	
//				final String valueEntry = String.format("%.2g", value);
//				dataFileContent.append(valueEntry+" ".repeat(14-valueEntry.length())+",");
//				
//				final FVector trainingEntry = AI.heuristicValueFunction().computeStateFeatureVector(context, maximisingPlayer);
//
//				for (int k=0; k<nbParameters; k++)
//				{
//					final String entry = String.format("%.8g", trainingEntry.get(k));
//					dataFileContent.append(entry+" ".repeat(14-entry.length())+",");
//				}
//				
//				dataFileContent.deleteCharAt(dataFileContent.length()-1); //deleting the last comma
//				dataFileContent.append("\n");
//			}
//		}
//		
//		return value;
//	}
//	
//	//-------------------------------------------------------------------------
//
//	
//	protected List<HeuristicTerm> createHeuristicTerms (final Game game)
//	{
//		/** The initial weight of the heuristics is important for the initialisation */
//		
//		List<HeuristicTerm> heuristicTerms = new ArrayList<HeuristicTerm>();	
//		
//		if (CurrentMoverHeuristic.isApplicableToGame(game))
//			heuristicTerms.add(new CurrentMoverHeuristic(null, 0f));
//
//		if (Material.isApplicableToGame(game))
//			heuristicTerms.add(new Material(null, 1f, null, null));
//		
//		if (MobilityAdvanced.isApplicableToGame(game))
//			heuristicTerms.add(new MobilityAdvanced(null, 0f));
//		
////		if (InfluenceAdvanced.isApplicableToGame(game))
////			heuristicTerms.add(new InfluenceAdvanced(null, 0f));
//
////		if (LineCompletionHeuristic.isApplicableToGame(game))
////			heuristicTerms.add(new LineCompletionHeuristic(null, 0f, null));
//		
//		
////		if (OwnRegionsCount.isApplicableToGame(game))
////			heuristicTerms.add(new OwnRegionsCount(null, 0f));
//		
////		if (PlayerSiteMapCount.isApplicableToGame(game))
////			heuristicTerms.add(new PlayerSiteMapCount(null, 0f));
////		
////		if (Score.isApplicableToGame(game))
////			heuristicTerms.add(new Score(null, 0f));
//		
////		if (CentreProximity.isApplicableToGame(game))
////			heuristicTerms.add(new CentreProximity(null, 0f, null));
//		
////		if (ComponentValues.isApplicableToGame(game))
////		{
////			heuristicTerms.add(new ComponentValues(null, Float.valueOf(defaultWeight), null, null));
////			for (final Pair[] componentPairs : allComponentPairsCombinations)
////				heuristicTerms.add(new ComponentValues(null, Float.valueOf(defaultWeight), componentPairs, null));
////		}
//			
////		if (CornerProximity.isApplicableToGame(game))
////			heuristicTerms.add(new CornerProximity(null, 0f, null));
//	
//		if (SidesProximity.isApplicableToGame(game))
//			heuristicTerms.add(new SidesProximity(null, 0f, null));
//		
////		if (PlayerRegionsProximity.isApplicableToGame(game))
////		{
////			for (int p = 1; p <= game.players().count(); ++p)
////			{
////				heuristicTerms.add(new PlayerRegionsProximity(null, 0f, p, null));
////			}
////		}
//		
//		/*
//		 if (RegionProximity.isApplicableToGame(game))
//		{
//			for (int i = 0; i < game.equipment().regions().length; ++i)
//			{
//				heuristicTerms.add(new RegionProximity(null, Float.valueOf(weight), Integer.valueOf(i), null));
//				for (final Pair[] componentPairs : allComponentPairsCombinations)
//					heuristicTerms.add(new RegionProximity(null, Float.valueOf(weight), Integer.valueOf(i), componentPairs));
//			}
//		}
//		*/
//		
//		return heuristicTerms;
//	}
//	
//	//-------------------------------------------------------------------------
//	
//	protected String toString(final float[] weights)
//	{
//		final StringBuffer stringBuilder = new StringBuffer();
//		
//		for (int i=0; i<weights.length; i++)
//			stringBuilder.append(String.format("%.8g\n", weights[i]));
//		
//		return stringBuilder.toString();
//	}
//	
//	protected String toString(final List<Double> values)
//	{
//		final StringBuffer stringBuilder = new StringBuffer();
//		
//		stringBuilder.append("[");
//		for (int i=0; i<values.size(); i++)
//			stringBuilder.append(String.format("%.8g,", values.get(i)));
//		
//		stringBuilder.deleteCharAt(stringBuilder.length()-1);
//		stringBuilder.append("]");
//		return stringBuilder.toString();
//	}
}
