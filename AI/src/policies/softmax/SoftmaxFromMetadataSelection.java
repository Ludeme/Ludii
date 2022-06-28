package policies.softmax;

import java.util.ArrayList;
import java.util.List;

import features.feature_sets.BaseFeatureSet;
import function_approx.LinearFunction;
import game.Game;
import game.types.play.RoleType;
import main.collections.FVector;
import main.collections.FastArrayList;
import metadata.ai.features.Features;
import metadata.ai.features.trees.FeatureTrees;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;
import search.mcts.MCTS;

/**
 * A Softmax Policy that can automatically initialise itself by
 * using the Selection features embedded in a game's metadata.
 * 
 * @author Dennis Soemers
 */
public class SoftmaxFromMetadataSelection extends SoftmaxPolicy
{
	
	//-------------------------------------------------------------------------
	
	/** Softmax policy we wrap around; can change into a linear or decision tree based policy depending on metadata */
	private SoftmaxPolicy wrappedSoftmax = null;
		
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param epsilon Epsilon for epsilon-greedy feature-based playouts. 1 for uniform, 0 for always softmax
	 */
	public SoftmaxFromMetadataSelection(final double epsilon)
	{
		friendlyName = "Softmax Policy (Selection features from Game metadata)";
		this.epsilon = epsilon;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void initAI(final Game game, final int playerID)
	{
		try
		{
			final Features featuresMetadata = game.metadata().ai().features();
			
			if (featuresMetadata != null)
			{
				final List<BaseFeatureSet> featureSetsList = new ArrayList<BaseFeatureSet>();
				final List<LinearFunction> linFuncs = new ArrayList<LinearFunction>();
				
				wrappedSoftmax = new SoftmaxPolicyLinear();
				wrappedSoftmax.epsilon = epsilon;
				wrappedSoftmax.playoutActionLimit = 200;
				
				for (final metadata.ai.features.FeatureSet featureSet : featuresMetadata.featureSets())
				{
					if (featureSet.role() == RoleType.Shared)
						SoftmaxPolicyLinear.addFeatureSetWeights(0, featureSet.featureStrings(), featureSet.selectionWeights(), featureSetsList, linFuncs);
					else
						SoftmaxPolicyLinear.addFeatureSetWeights(featureSet.role().owner(), featureSet.featureStrings(), featureSet.selectionWeights(), featureSetsList, linFuncs);
				}
				
				((SoftmaxPolicyLinear) wrappedSoftmax).featureSets = featureSetsList.toArray(new BaseFeatureSet[featureSetsList.size()]);
				((SoftmaxPolicyLinear) wrappedSoftmax).linearFunctions = linFuncs.toArray(new LinearFunction[linFuncs.size()]);
			}
			else
			{
				// TODO no distinction between selection and playout here
				final FeatureTrees featureTrees = game.metadata().ai().trainedFeatureTrees();
				wrappedSoftmax = SoftmaxPolicyLogitTree.constructPolicy(featureTrees, epsilon);
				wrappedSoftmax.playoutActionLimit = 200;
			}
			
			wrappedSoftmax.initAI(game, playerID);
		}
		catch (final Exception e)
		{
			System.err.println("Game = " + game.name());
			e.printStackTrace();
		}
		
		super.initAI(game, playerID);
	}
	
	@Override
	public boolean supportsGame(final Game game)
	{
		// We support any game with appropriate features in metadata
		if (game.metadata().ai() != null)
		{
			if (game.metadata().ai().features() != null)
			{
				final Features featuresMetadata = game.metadata().ai().features();
				if (featuresMetadata.featureSets().length == 1 && featuresMetadata.featureSets()[0].role() == RoleType.Shared)
					return true;
				else
					return (featuresMetadata.featureSets().length == game.players().count());
			}
			else if (game.metadata().ai().trainedFeatureTrees() != null)
			{
				return true;
			}
			else
			{
				return false;
			}
		}

		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Our current wrapped softmax (linear or tree-based)
	 */
	public SoftmaxPolicy wrappedSoftmax()
	{
		return wrappedSoftmax;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Trial runPlayout(final MCTS mcts, final Context context) 
	{
		return wrappedSoftmax.runPlayout(mcts, context);
	}

	@Override
	public boolean playoutSupportsGame(final Game game) 
	{
		return supportsGame(game);
	}

	@Override
	public int backpropFlags() 
	{
		return 0;
	}

	@Override
	public void customise(final String[] inputs) 
	{
		System.err.println("customise() not implemented for SoftmaxFromMetadataSelection!");
	}

	@Override
	public float computeLogit(final Context context, final Move move) 
	{
		return wrappedSoftmax.computeLogit(context, move);
	}

	@Override
	public FVector computeDistribution(final Context context, final FastArrayList<Move> actions, final boolean thresholded) 
	{
		return wrappedSoftmax.computeDistribution(context, actions, thresholded);
	}

	@Override
	public Move selectAction(final Game game, final Context context, final double maxSeconds, final int maxIterations, final int maxDepth) 
	{
		return wrappedSoftmax.selectAction(game, context, maxSeconds, maxIterations, maxDepth);
	}
	
}
