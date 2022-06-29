package supplementary.experiments;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import main.collections.FastArrayList;
import metadata.ai.heuristics.Heuristics;
import metadata.ai.heuristics.terms.CentreProximity;
import metadata.ai.heuristics.terms.ComponentValues;
import metadata.ai.heuristics.terms.CornerProximity;
import metadata.ai.heuristics.terms.CurrentMoverHeuristic;
import metadata.ai.heuristics.terms.HeuristicTerm;
import metadata.ai.heuristics.terms.Influence;
import metadata.ai.heuristics.terms.InfluenceAdvanced;
import metadata.ai.heuristics.terms.Intercept;
import metadata.ai.heuristics.terms.LineCompletionHeuristic;
import metadata.ai.heuristics.terms.Material;
import metadata.ai.heuristics.terms.MobilityAdvanced;
import metadata.ai.heuristics.terms.MobilitySimple;
import metadata.ai.heuristics.terms.OwnRegionsCount;
import metadata.ai.heuristics.terms.PlayerRegionsProximity;
import metadata.ai.heuristics.terms.PlayerSiteMapCount;
import metadata.ai.heuristics.terms.RegionProximity;
import metadata.ai.heuristics.terms.Score;
import metadata.ai.heuristics.terms.SidesProximity;
import metadata.ai.heuristics.terms.ThreatenedMaterial;
import metadata.ai.heuristics.terms.ThreatenedMaterialMultipleCount;
import metadata.ai.heuristics.terms.UnthreatenedMaterial;
import metadata.ai.misc.Pair;
import other.AI;
import other.GameLoader;
import other.RankUtils;
import other.context.Context;
import other.move.Move;
import other.state.State;
import other.trial.Trial;
import search.minimax.BestFirstSearch;
import utils.data_structures.transposition_table.TranspositionTableBFS;
import utils.data_structures.transposition_table.TranspositionTableBFS.BFSTTData;
//import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ArffLoader;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;

/**
 * 
 * Script to learn a set of heuristic terms and weight for a game, for it to be
 * used by minimax based algorithm like Alpha-Beta search or UBFS.
 * 
 * @author cyprien
 *
 */

public class HeuristicsLearning
{
	
	private static final BestFirstSearch BestFirstSearch = null;

	private final Boolean debugDisplays = true;
	
	/** Duration of the whole training in seconds: */
	private final Double trainingDuration = 1000.;
	
	/** Game played: */
	private final String gameName = "Breakthrough";
	
	/** Thinking time of the AI in seconds: */
	private final float thinkingTime = 1f;
	
	private final String trainDataFile = "training_data/training.arff";
	private final String testDataFile = "training_data/testing.arff";
	
	private final StringBuffer trainDataARFF = new StringBuffer();
	private final StringBuffer testDataARFF = new StringBuffer();
	
	/** Probability that an entry gets assigned to the testing data set: */
	private final Double proportionOfTestingEntries = 0.15;
	
	//-------------------------------------------------------------------------
	
	/** The different heuristic terms (with a weight of 1) */
	private HeuristicTerm[] allHeuristicTerms;
	
	/** Training data X (list of vectors containing the value of each heuristics):*/
	private List<float[]> trainingDataX = new ArrayList<float[]>(); //currently unused
	
	/** Training data Y (list of floats representing the value of each states):*/
	private List<Float> trainingDataY = new ArrayList<Float>(); //currently unused
	
	private Game game;
	
	private int maximisingPlayer = 1;
	
	int numPlayers;
	
	int callNumber;
	
	//-------------------------------------------------------------------------
	
	public HeuristicsLearning()
	{
		// pass
	}
	
	
	public static void main (String[] args) throws Exception
	{
		HeuristicsLearning core = new HeuristicsLearning();
		
		core.runHeuristicLearning();		
	}
	
	protected List<HeuristicTerm> createHeuristicTerms (final Game game)
	{
		List<HeuristicTerm> heuristicTerms = new ArrayList<HeuristicTerm>();
		
		final List<Pair[]> allComponentPairsCombinations = new ArrayList<>();
		for (int i = 0; i < game.equipment().components().length-1; i++)
		{
			final Pair[] componentPairs  = new Pair[game.equipment().components().length-1];
			for (int j = 0; j < game.equipment().components().length-1; j++)
			{
				if (j == i)
					componentPairs[j] = new Pair(game.equipment().components()[j+1].name(), 1f);
				else
					componentPairs[j] = new Pair(game.equipment().components()[j+1].name(), 0f);
			}
			allComponentPairsCombinations.add(componentPairs);
		}
		
		final float weight = 1f;

		//if (LineCompletionHeuristic.isApplicableToGame(game))
		//	heuristicTerms.add(new LineCompletionHeuristic(null, Float.valueOf(weight), null));
		
		if (MobilitySimple.isApplicableToGame(game))
			heuristicTerms.add(new MobilitySimple(null, Float.valueOf(weight)));
		
		if (Influence.isApplicableToGame(game))
			heuristicTerms.add(new Influence(null, Float.valueOf(weight)));
		
		if (OwnRegionsCount.isApplicableToGame(game))
			heuristicTerms.add(new OwnRegionsCount(null, Float.valueOf(weight)));
		
		if (PlayerSiteMapCount.isApplicableToGame(game))
			heuristicTerms.add(new PlayerSiteMapCount(null, Float.valueOf(weight)));
		
		if (Score.isApplicableToGame(game))
			heuristicTerms.add(new Score(null, Float.valueOf(weight)));
		
		if (CentreProximity.isApplicableToGame(game))
		{
			heuristicTerms.add(new CentreProximity(null, Float.valueOf(weight), null));
			for (final Pair[] componentPairs : allComponentPairsCombinations)
				heuristicTerms.add(new CentreProximity(null, Float.valueOf(weight), componentPairs));
		}
		
		if (ComponentValues.isApplicableToGame(game))
		{
			heuristicTerms.add(new ComponentValues(null, Float.valueOf(weight), null, null));
			for (final Pair[] componentPairs : allComponentPairsCombinations)
				heuristicTerms.add(new ComponentValues(null, Float.valueOf(weight), componentPairs, null));
		}
			
		if (CornerProximity.isApplicableToGame(game))
		{
			heuristicTerms.add(new CornerProximity(null, Float.valueOf(weight), null));
			for (final Pair[] componentPairs : allComponentPairsCombinations)
				heuristicTerms.add(new CornerProximity(null, Float.valueOf(weight), componentPairs));
		}
	
		if (Material.isApplicableToGame(game))
		{
			heuristicTerms.add(new Material(null, Float.valueOf(weight), null, null));
			for (final Pair[] componentPairs : allComponentPairsCombinations)
				heuristicTerms.add(new Material(null, Float.valueOf(weight), componentPairs, null));
		}
	
		if (SidesProximity.isApplicableToGame(game))
		{
			heuristicTerms.add(new SidesProximity(null, Float.valueOf(weight), null));
			for (final Pair[] componentPairs : allComponentPairsCombinations)
				heuristicTerms.add(new CentreProximity(null, Float.valueOf(weight), componentPairs));
		}
		
		if (PlayerRegionsProximity.isApplicableToGame(game))
		{
			for (int p = 1; p <= game.players().count(); ++p)
			{
				heuristicTerms.add(new PlayerRegionsProximity(null, Float.valueOf(weight), Integer.valueOf(p), null));
				for (final Pair[] componentPairs : allComponentPairsCombinations)
					heuristicTerms.add(new PlayerRegionsProximity(null, Float.valueOf(weight), Integer.valueOf(p), componentPairs));
			}
		}
		
		/*
		 if (RegionProximity.isApplicableToGame(game))
		{
			for (int i = 0; i < game.equipment().regions().length; ++i)
			{
				heuristicTerms.add(new RegionProximity(null, Float.valueOf(weight), Integer.valueOf(i), null));
				for (final Pair[] componentPairs : allComponentPairsCombinations)
					heuristicTerms.add(new RegionProximity(null, Float.valueOf(weight), Integer.valueOf(i), componentPairs));
			}
		}
		*/
		
		return heuristicTerms;
	}
	
	
	protected void runHeuristicLearning () throws Exception
	{
		final long startTime = System.currentTimeMillis();
		long stopTime = startTime + (long) (trainingDuration * 1000);
		
		game = GameLoader.loadGameFromName(gameName+".lud");
		numPlayers = game.players().count();
		
		final List<HeuristicTerm> heuristicTermsList = createHeuristicTerms(game);
		allHeuristicTerms = new HeuristicTerm[heuristicTermsList.size()];
		for (int i=0; i<heuristicTermsList.size(); i++)
		{
			allHeuristicTerms[i] = heuristicTermsList.get(i);
		}
		
		final int nbHeuristics = allHeuristicTerms.length;
		
		final Heuristics heuristics = new Heuristics(allHeuristicTerms);
		
		final float[] heuristicWeights = new float[nbHeuristics];
		// Initialising weights with random values
		for (int i=0; i<nbHeuristics; i++)
			heuristicWeights[i] = ThreadLocalRandom.current().nextFloat();
		
		int numIteration = 1;
		
		while (System.currentTimeMillis() < stopTime)
		{
		
			//-----------------------------------------------------------------
			// Setting up a playout:
			
			final Trial trial = new Trial(game);
			final Context context = new Context(game, trial);
			
			final List<AI> agents = new ArrayList<>();
			
			for (int i=0; i<nbHeuristics; i++)
			allHeuristicTerms[i].setWeight(heuristicWeights[i]);

			String[] dataFileNames = new String[] {trainDataFile,testDataFile};
			StringBuffer[] dataFileContents = new StringBuffer[] {trainDataARFF, testDataARFF};
			for (int k : new int[] {0,1})
			{
				dataFileContents[k].setLength(0);
				dataFileContents[k].append("@relation Heuristics_score_differences_and_value\n\n");
				
				// Filling the heuristic function's weight:
				for (int i=0; i<nbHeuristics; i++)
				{
					if (i==1)
						dataFileContents[k].append("@attribute value NUMERIC\n");
					dataFileContents[k].append("@attribute "+allHeuristicTerms[i].getClass().getSimpleName()+Integer.toString(i)+" NUMERIC\n");
				}
				dataFileContents[k].append("\n@data\n");
			}
			
			
			final BestFirstSearch AI = new BestFirstSearch(heuristics);
			AI.setIfFullPlayouts(true);
			AI.savingSearchTreeDescription = false;
			
			agents.add(null);
			agents.add(AI);
			agents.add(AI);
		
			game.start(context);
		
			AI.initAI(game, maximisingPlayer);

			final Context initialContext = AI.copyContext(context); // used later
			
			if (debugDisplays) System.out.printf("\nBeginning the playout number %d:\n",numIteration);
			
			game.playout(context, agents, thinkingTime, null, -1, 200, ThreadLocalRandom.current()); // TODO: change max nb playout actions
			
			System.out.println("done");
			
			//-----------------------------------------------------------------
			// Generating training set for the linear regression:
			
			generateTrainingSet( AI , initialContext );
			
			
			//-----------------------------------------------------------------
			// Updating the heuristicWeights with Machine Learning:
			if (debugDisplays) System.out.println("\nUpdating the heuristicWeights with Machine Learning:");
			
			// Saving data in a file
			for (int i : new int[] {0,1})
			{
				try {
			      FileWriter myWriter = new FileWriter("/home/cyprien/Documents/M1/Internship/"+dataFileNames[i]);
			      myWriter.write(dataFileContents[i].toString());
			      myWriter.close();
			    } catch (IOException e) {
			      System.out.println("An error occurred.");
			      e.printStackTrace();
			    }
			}
			
			Instances trainDataSet = loadDataSet(trainDataFile);
			Instances testDataSet = loadDataSet(testDataFile);
			
			LinearRegression classifier = new LinearRegression();
			classifier.setOptions(Utils.splitOptions("-S 1"));
			
			classifier.buildClassifier(trainDataSet);
			
			Evaluation eval = new Evaluation(trainDataSet);
			eval.evaluateModel(classifier, testDataSet);
			
			System.out.println("** Linear Regression Evaluation with Datasets **");
			System.out.println(eval.toSummaryString());
			System.out.print(" the expression for the input data as per alogorithm is ");
			System.out.println(classifier);
			
			assert classifier.coefficients().length == nbHeuristics;
			
			for (int i=0; i<nbHeuristics; i++)
			{
				heuristicWeights[i] = (float) classifier.coefficients()[i];
			}
			
			numIteration += 1;
		}
		
		//---------------------------------------------------------------------
		// Saving weights in a file: (TODO)
		
		
	}
	
	public static Instances loadDataSet(String fileName) throws IOException {
		/**
		 * we can set the file i.e., loader.setFile("finename") to load the data
		 */
		int classIdx = 1;
		/** the arffloader to load the arff file */
		ArffLoader loader = new ArffLoader();
		/** load the traing data */
		loader.setSource(new File("/home/cyprien/Documents/M1/Internship/" + fileName));
		
		Instances dataSet = loader.getDataSet();
		/** set the index based on the data given in the arff files */
		dataSet.setClassIndex(classIdx);
		return dataSet;
	}

	/**
	 * Uses a transposition table to calculate the value assosciated to states and fill the training data.
	 * Will use a recursive function to evaluate the visted nodes with minimax.
	 * @param TT
	 */
	protected void generateTrainingSet (final BestFirstSearch AI, final Context initialContext)
	{
		if (debugDisplays) System.out.println("\nGenerating training set for the linear regression:");
		
		trainingDataX.clear();
		trainingDataY.clear();
		
		AI.transpositionTable.dispValueStats();
		
		callNumber = 0;
		
		final long zobrist = initialContext.state().fullHash();
		final BFSTTData tableData = AI.transpositionTable.retrieve(zobrist);
		// Marking the value in the TT so that it is not visited again in the same recursive call
		AI.transpositionTable.store(tableData.bestMove, tableData.fullHash, tableData.value, tableData.depth, TranspositionTableBFS.MARKED_EXACT_VALUE, tableData.sortedScoredMoves);
		
		float rootValue = fillTrainingData(AI, initialContext);
		
		// Validating the value in the TT so that it is not visited again
		AI.transpositionTable.store(tableData.bestMove, tableData.fullHash, rootValue, tableData.depth, TranspositionTableBFS.MARKED_EXACT_VALUE, tableData.sortedScoredMoves);
				
		return;
	}
	
	/**
	 * Recursive function to fill the training data.
	 * The value of a state is it's minimax value in the search tree ("tree learning").
	 * @param TT
	 * @param context
	 */
	protected float fillTrainingData
	(
		final BestFirstSearch AI,
		final Context context
	)
	{
		final float value;
		
		if (callNumber % 1000 == 0)
		{
			System.out.print(Integer.toString(callNumber)+"-");
			//System.out.println("Number of validate entries:"+Integer.toString(AI.transpositionTable.nbMarkedEntries()));
			System.out.println();
		}
		
		final State firstState = context.state();
		final long firstZobrist =  firstState.fullHash();
		
		if (context.trial().over() || !context.active(maximisingPlayer))
		{
			// terminal node (at least for maximising player)
			value = (float) RankUtils.agentUtilities(context)[maximisingPlayer];
			//System.out.println("terminal node");
		}
		else
		{
			final FastArrayList<Move> legalMoves = game.moves(context).moves();
			final int nbMoves = legalMoves.size();
			final State state = context.state();
			final int mover = state.playerToAgent(state.mover());
			
			final List<Float> childrenValues = new ArrayList<Float>();

			for (int i=0; i<nbMoves; i++)
			{
				final Context contextCopy = AI.copyContext(context);
				final Move move = legalMoves.get(i);
				
				game.apply(contextCopy, move);
				
				final State newState = contextCopy.state();
				final long zobrist = newState.fullHash();
				final BFSTTData tableData = AI.transpositionTable.retrieve(zobrist);
				
				if (tableData != null)
				{
					if (tableData.valueType != TranspositionTableBFS.MARKED_EXACT_VALUE)
					{
						if (tableData.valueType != TranspositionTableBFS.VALIDATED_EXACT_VALUE)
						{
							// Recursive call:
							callNumber += 1;
							
							// Marking the value in the TT so that it is not visited again in the same recursive call
							AI.transpositionTable.store(tableData.bestMove, tableData.fullHash, tableData.value, tableData.depth, TranspositionTableBFS.MARKED_EXACT_VALUE, tableData.sortedScoredMoves);
							
							final float childValue = fillTrainingData(AI, contextCopy);
							
							// Un-marking the value in the TT so that it is not considered as a draw if encountered elsewhere in the tree
							AI.transpositionTable.store(tableData.bestMove, tableData.fullHash, childValue, tableData.depth, TranspositionTableBFS.VALIDATED_EXACT_VALUE, tableData.sortedScoredMoves);
							
							//System.out.print("(rec call of fillTraining data)");
							if (childValue != 0)
								childrenValues.add(childValue);
						}
						else
						{
							childrenValues.add(tableData.value);
						}
					}
					else
					{
						// we consider that one can always chose to go back to a previously visited state, so it is a draw as far as this path is concerned
						childrenValues.add(0f);
					}
				}
			}
			
			if (childrenValues.size()>0)
			{
				float myMax = childrenValues.get(0); // actually a min if the opposent is playing
				
				for (float childValue : childrenValues)
				{
					if (((childValue>myMax)&&(mover==maximisingPlayer))||((childValue<myMax)&&(mover!=maximisingPlayer)))
					{
						myMax = childValue;
					}
				};
				
				value = myMax;
			}
			else
			{
				value = 0;
			}
			
			// Adding entry with heuristic value differences:
			
			final float[] trainingEntry = new float[allHeuristicTerms.length];
			final float oppScoreMultiplier = 1f / numPlayers;	// this gives us nicer heuristics around 0

			if (value != 0)
			{
				StringBuffer dataFileContent;
				if (Math.random()>proportionOfTestingEntries)
					dataFileContent = trainDataARFF;
				else
					dataFileContent = testDataARFF;
	
				for (int i=0; i<allHeuristicTerms.length; i++)
				{
					if (i==1)
						dataFileContent.append( String.format("%.2g,", value) );
					trainingEntry[i] = 0f;
					for (int player = 1; player <= numPlayers; ++player)
					{
						if (player == maximisingPlayer)
							trainingEntry[i] += allHeuristicTerms[i].computeValue(context,player,-1);
						else
							trainingEntry[i] -= allHeuristicTerms[i].computeValue(context,player,-1)*oppScoreMultiplier;
					}
					
					dataFileContent.append( String.format("%.8g,", trainingEntry[i]) );
				}
				
				dataFileContent.deleteCharAt(dataFileContent.length()-1);
				dataFileContent.append("\n");
				
				trainingDataX.add(trainingEntry);
				trainingDataY.add(value);
			}
		}
		
		return value;
	}
}
