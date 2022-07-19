package supplementary.experiments.feature_importance;

import java.util.ArrayList;
import java.util.List;

import features.Feature;
import features.FeatureVector;
import features.WeightVector;
import features.feature_sets.BaseFeatureSet;
import function_approx.LinearFunction;
import game.Game;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.StringRoutines;
import main.collections.ArrayUtils;
import main.collections.FVector;
import other.GameLoader;
import policies.softmax.SoftmaxPolicyLinear;
import search.mcts.MCTS;
import training.expert_iteration.ExItExperience;
import utils.AIFactory;
import utils.ExperimentFileUtils;
import utils.data_structures.experience_buffers.ExperienceBuffer;
import utils.data_structures.experience_buffers.PrioritizedReplayBuffer;
import utils.data_structures.experience_buffers.UniformExperienceBuffer;

public class AnalyseFeatureImportances
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Do the analysis
	 * @param argParse
	 */
	private static void analyseFeatureImportances(final CommandLineArgParse argParse)
	{
		final String gameName = argParse.getValueString("--game-name");
		String gameTrainingDirPath = argParse.getValueString("--game-training-dir");
		if (!gameTrainingDirPath.endsWith("/"))
			gameTrainingDirPath += "/";
		
		final Game game = GameLoader.loadGameFromName(gameName);
		
		// Construct a string to load an MCTS guided by features, from that we can then easily extract the
		// features again afterwards
		final StringBuilder playoutSb = new StringBuilder();
		playoutSb.append("playout=softmax");

		for (int p = 1; p <= game.players().count(); ++p)
		{
			final String policyFilepath = 
					ExperimentFileUtils.getLastFilepath
					(
						gameTrainingDirPath + "PolicyWeights" + 
						"TSPG_P" + p, 
						"txt"
					);

			playoutSb.append(",policyweights" + p + "=" + policyFilepath);
		}

		playoutSb.append(",boosted=true");	// True for TSPG, false for CE

		final StringBuilder selectionSb = new StringBuilder();
		selectionSb.append("learned_selection_policy=playout");

		final String agentStr = StringRoutines.join
				(
					";", 
					"algorithm=MCTS",
					"selection=noisyag0selection",
					playoutSb.toString(),
					"final_move=robustchild",
					"tree_reuse=true",
					selectionSb.toString(),
					"friendly_name=BiasedMCTS"
				);

		final MCTS mcts = (MCTS) AIFactory.createAI(agentStr);
		final SoftmaxPolicyLinear playoutSoftmax = (SoftmaxPolicyLinear) mcts.playoutStrategy();

		final BaseFeatureSet[] featureSets = playoutSoftmax.featureSets();
		final LinearFunction[] linearFunctions = playoutSoftmax.linearFunctions();

		playoutSoftmax.initAI(game, -1);
		
		// NOTE: just doing Player 1 for now
		// Load experience buffer for Player p
		final String bufferFilepath = 
				ExperimentFileUtils.getLastFilepath
				(
					gameTrainingDirPath + 
					"ExperienceBuffer_P" + 1, 
					"buf"
				);

		ExperienceBuffer buffer = null;
		try
		{
			buffer = PrioritizedReplayBuffer.fromFile(game, bufferFilepath);
		}
		catch (final Exception e)
		{
			if (buffer == null)
			{
				try
				{
					buffer = UniformExperienceBuffer.fromFile(game, bufferFilepath);
				}
				catch (final Exception e2)
				{
					e.printStackTrace();
					e2.printStackTrace();
					return;
				}
			}
		}
		
		// From here onwards very similar code to the decision tree building code
		final BaseFeatureSet featureSet = featureSets[1];
		final LinearFunction linFunc = linearFunctions[1];
		
		final WeightVector oracleWeightVector = linearFunctions[1].effectiveParams();
		final ExItExperience[] samples = buffer.allExperience();
		final List<FeatureVector> allFeatureVectors = new ArrayList<FeatureVector>();
		final TFloatArrayList allTargetLabels = new TFloatArrayList();
		
		for (final ExItExperience sample : samples)
		{
			if (sample != null && sample.moves().size() > 1)
			{
				final FeatureVector[] featureVectors = sample.generateFeatureVectors(featureSet);
				final float[] logits = new float[featureVectors.length];

				for (int i = 0; i < featureVectors.length; ++i)
				{
					final FeatureVector featureVector = featureVectors[i];
					logits[i] = oracleWeightVector.dot(featureVector);
				}
				
				final float maxLogit = ArrayUtils.max(logits);
				final float minLogit = ArrayUtils.min(logits);
				
				if (maxLogit == minLogit)
					continue;		// Nothing to learn from this, just skip it
				
				for (int i = 0; i < featureVectors.length; ++i)
				{
					final FeatureVector featureVector = featureVectors[i];
					allFeatureVectors.add(featureVector);
				}
				
				// Maximise logits for winning moves and minimise for losing moves
				for (int i = sample.winningMoves().nextSetBit(0); i >= 0; i = sample.winningMoves().nextSetBit(i + 1))
				{
					logits[i] = maxLogit;
				}
				
				for (int i = sample.losingMoves().nextSetBit(0); i >= 0; i = sample.losingMoves().nextSetBit(i + 1))
				{
					logits[i] = minLogit;
				}
				
				final FVector policy = new FVector(logits);
				policy.softmax();
				
				final float maxProb = policy.max();
				
				final float[] targets = new float[logits.length];
				for (int i = 0; i < targets.length; ++i)
				{
					targets[i] = policy.get(i) / maxProb;
				}
				
				for (final float target : targets)
				{
					allTargetLabels.add(target);
				}
			}
		}
		
		// For every aspatial and every spatial feature, if not already picked, compute mean prob for true and false branches
		final int numAspatialFeatures = featureSet.getNumAspatialFeatures();
		final int numSpatialFeatures = featureSet.getNumSpatialFeatures();
		
		final double[] sumProbsIfFalseAspatial = new double[numAspatialFeatures];
		final int[] numFalseAspatial = new int[numAspatialFeatures];
		final double[] sumProbsIfTrueAspatial = new double[numAspatialFeatures];
		final int[] numTrueAspatial = new int[numAspatialFeatures];

		for (int i = 0; i < numAspatialFeatures; ++i)
		{
			for (int j = 0; j < allFeatureVectors.size(); ++j)
			{
				final FeatureVector featureVector = allFeatureVectors.get(j);
				final float targetProb = allTargetLabels.getQuick(j);

				if (featureVector.aspatialFeatureValues().get(i) != 0.f)
				{
					sumProbsIfTrueAspatial[i] += targetProb;
					++numTrueAspatial[i];
				}
				else
				{
					sumProbsIfFalseAspatial[i] += targetProb;
					++numFalseAspatial[i];
				}
			}
		}

		final double[] sumProbsIfFalseSpatial = new double[numSpatialFeatures];
		final int[] numFalseSpatial = new int[numSpatialFeatures];
		final double[] sumProbsIfTrueSpatial = new double[numSpatialFeatures];
		final int[] numTrueSpatial = new int[numSpatialFeatures];

		for (int i = 0; i < allFeatureVectors.size(); ++i)
		{
			final FeatureVector featureVector = allFeatureVectors.get(i);
			final float targetProb = allTargetLabels.getQuick(i);

			final boolean[] active = new boolean[numSpatialFeatures];
			final TIntArrayList sparseSpatials = featureVector.activeSpatialFeatureIndices();

			for (int j = 0; j < sparseSpatials.size(); ++j)
			{
				active[sparseSpatials.getQuick(j)] = true;
			}

			for (int j = 0; j < active.length; ++j)
			{
				if (active[j])
				{
					sumProbsIfTrueSpatial[j] += targetProb;
					++numTrueSpatial[j];
				}
				else
				{
					sumProbsIfFalseSpatial[j] += targetProb;
					++numFalseSpatial[j];
				}
			}
		}

		final double[] meanProbsIfFalseAspatial = new double[numAspatialFeatures];
		final double[] meanProbsIfTrueAspatial = new double[numAspatialFeatures];
		final double[] meanProbsIfFalseSpatial = new double[numSpatialFeatures];
		final double[] meanProbsIfTrueSpatial = new double[numSpatialFeatures];

		for (int i = 0; i < numAspatialFeatures; ++i)
		{
			if (numFalseAspatial[i] > 0)
				meanProbsIfFalseAspatial[i] = sumProbsIfFalseAspatial[i] / numFalseAspatial[i];

			if (numTrueAspatial[i] > 0)
				meanProbsIfTrueAspatial[i] = sumProbsIfTrueAspatial[i] / numTrueAspatial[i];
		}

		for (int i = 0; i < numSpatialFeatures; ++i)
		{
			if (numFalseSpatial[i] > 0)
				meanProbsIfFalseSpatial[i] = sumProbsIfFalseSpatial[i] / numFalseSpatial[i];

			if (numTrueSpatial[i] > 0)
				meanProbsIfTrueSpatial[i] = sumProbsIfTrueSpatial[i] / numTrueSpatial[i];
		}
		
		
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Pair of a feature and a score (for sorting)
	 * 
	 * @author Dennis Soemers
	 */
	private static class ScoredFeature
	{
		public final double score;
		public final Feature feature;
		
		/**
		 * Constructor
		 * @param score
		 * @param feature
		 */
		public ScoredFeature(final double score, final Feature feature)
		{
			this.score = score;
			this.feature = feature;
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Main method
	 * @param args
	 */
	public static void main(final String[] args)
	{
		// define options for arg parser
		final CommandLineArgParse argParse = 
				new CommandLineArgParse
				(
					true,
					"Analyses feature importances for a game."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--game-training-dir")
				.help("The directory with training outcomes for the game to analyse.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		argParse.addOption(new ArgOption()
				.withNames("--game-name")
				.help("Name of the game.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		// parse the args
		if (!argParse.parseArguments(args))
			return;
		
		analyseFeatureImportances(argParse);
	}
	
	//-------------------------------------------------------------------------

}
