package training.expert_iteration.gradients;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import features.FeatureVector;
import gnu.trove.list.array.TIntArrayList;
import main.collections.FVector;
import metadata.ai.heuristics.Heuristics;
import optimisers.Optimiser;
import policies.softmax.SoftmaxPolicyLinear;
import training.expert_iteration.ExItExperience;

/**
 * Class with helper methods to compute gradients for self-play training
 * (and related stuff, like errors/losses)
 * 
 * @author Dennis Soemers
 */
public class Gradients 
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Don't need a constructor for this class
	 */
	private Gradients()
	{
		// Do nothing;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param estimatedDistribution
	 * @param targetDistribution
	 * @return Vector of errors for estimated distribution in comparison to 
	 * target distribution (simply estimated - target)
	 */
	public static FVector computeDistributionErrors
	(
		final FVector estimatedDistribution, 
		final FVector targetDistribution
	)
	{
		final FVector errors = estimatedDistribution.copy();
		errors.subtract(targetDistribution);
		return errors;
	}
	
	public static FVector computeCrossEntropyErrors
	(
		final SoftmaxPolicyLinear policy, 
		final FVector expertDistribution, 
		final FeatureVector[] featureVectors,
		final int p,
		final boolean handleAliasing
	)
	{
		final FVector apprenticePolicy = policy.computeDistribution(featureVectors, p);
		final FVector expertPolicy;
		
		if (handleAliasing)
		{
			// Need to handle aliased moves
			final Map<FeatureVector, TIntArrayList> movesPerFeatureVector = 
				new HashMap<FeatureVector, TIntArrayList>();
			for (int moveIdx = 0; moveIdx < featureVectors.length; ++moveIdx)
			{
				final FeatureVector featureVector = featureVectors[moveIdx];
				if (!movesPerFeatureVector.containsKey(featureVector))
					movesPerFeatureVector.put(featureVector, new TIntArrayList());
				
				movesPerFeatureVector.get(featureVector).add(moveIdx);
			}
			
			expertPolicy = expertDistribution.copy();		// Don't want to permanently modify the original
			
			final boolean[] alreadyUpdatedValue = new boolean[expertPolicy.dim()];
			for (int moveIdx = 0; moveIdx < expertPolicy.dim(); ++moveIdx)
			{
				if (alreadyUpdatedValue[moveIdx])
					continue;
				
				final TIntArrayList aliasedMoves = movesPerFeatureVector.get(featureVectors[moveIdx]);
				if (aliasedMoves.size() > 1)
				{
					//System.out.println(aliasedMoves.size() + " aliased moves");
					float maxVal = 0.f;
					for (int i = 0; i < aliasedMoves.size(); ++i)
					{
						final float val = expertPolicy.get(aliasedMoves.getQuick(i));
						if (val > maxVal)
							maxVal = val;
					}
					
					// Set all aliased moves to the max probability
					for (int i = 0; i < aliasedMoves.size(); ++i)
					{
						expertPolicy.set(aliasedMoves.getQuick(i), maxVal);
						alreadyUpdatedValue[aliasedMoves.getQuick(i)] = true;
					}
				}
			}
			
			// Renormalise the expert policy
			expertPolicy.normalise();
			
//			System.out.println("---------------------------------------------------");
//			for (final Entry<FeatureVector, TIntArrayList> entry : movesPerFeatureVector.entrySet())
//			{
//				if (entry.getValue().size() > 1)
//				{
//					final FVector origErrors = cePolicy.computeDistributionErrors(apprenticePolicy, sample.expertDistribution());
//					final FVector modifiedErrors = cePolicy.computeDistributionErrors(apprenticePolicy, expertPolicy);
//					System.out.print("Orig errors for repeated feature vector:     ");
//					for (int moveIdx = 0; moveIdx < entry.getValue().size(); ++moveIdx)
//					{
//						System.out.print(origErrors.get(entry.getValue().getQuick(moveIdx)) + ", ");
//					}
//					System.out.println();
//					System.out.print("Modified errors for repeated feature vector: ");
//					for (int moveIdx = 0; moveIdx < entry.getValue().size(); ++moveIdx)
//					{
//						System.out.print(modifiedErrors.get(entry.getValue().getQuick(moveIdx)) + ", ");
//					}
//					System.out.println();
//				}
//			}
//			System.out.println("---------------------------------------------------");
		}
		else
		{
			expertPolicy = expertDistribution;
		}
		
		return computeDistributionErrors(apprenticePolicy, expertPolicy);
	}
	
	/**
	 * @param valueFunction
	 * @param p
	 * @param sample
	 * @return Vector of value function gradients, or null if value function is null or player is invalid.
	 */
	public static FVector computeValueGradients(final Heuristics valueFunction, final int p, final ExItExperience sample)
	{
		if (valueFunction != null && p > 0)
		{
			// Compute gradients for value function
			final FVector valueFunctionParams = valueFunction.paramsVector();
			final float predictedValue = (float) Math.tanh(valueFunctionParams.dot(sample.stateFeatureVector()));
			final float gameOutcome = (float) sample.playerOutcomes()[sample.state().state().mover()];
			
			final float valueError = predictedValue - gameOutcome;
			final FVector valueGradients = new FVector(valueFunctionParams.dim());
			
			// Need to multiply this by feature value to compute gradient per feature
			final float gradDivFeature = 2.f * valueError * (1.f - predictedValue*predictedValue);
			
			for (int i = 0; i < valueGradients.dim(); ++i)
			{
				valueGradients.set(i, gradDivFeature * sample.stateFeatureVector().get(i));
			}
			
//			System.out.println();
//			System.out.println("State Features = " + sample.stateFeatureVector());
//			System.out.println("pred. value = " + predictedValue);
//			System.out.println("observed outcome = " + gameOutcome);
//			System.out.println("value error = " + valueError);
//			System.out.println("value grads = " + valueGradients);
//			System.out.println();
		}
		
		return null;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param gradientVectors
	 * @return Mean vector of gradients, or null if there are no vectors of gradients.
	 */
	public static FVector meanGradients(final List<FVector> gradientVectors)
	{
		if (!gradientVectors.isEmpty())
			return FVector.mean(gradientVectors);
		
		return null;
	}
	
	/**
	 * @param gradientVectors
	 * @param sumImportanceSamplingWeights
	 * @return A single vector of gradients computed using Weighted Importance Sampling, rather
	 * 	than by taking directly the mean of the given list of vectors, or null if there are no
	 * 	vectors of gradients.
	 */
	public static FVector wisGradients
	(
		final List<FVector> gradientVectors, final float sumImportanceSamplingWeights
	)
	{
		if (gradientVectors.isEmpty())
			return null;
		
		final FVector wisGradients = gradientVectors.get(0).copy();
		for (int i = 1; i < gradientVectors.size(); ++i)
		{
			wisGradients.add(gradientVectors.get(i));
		}
		
		if (sumImportanceSamplingWeights > 0.0)
			wisGradients.div(sumImportanceSamplingWeights);
		
		return wisGradients;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Runs a gradient descent step + weight decay to minimise some loss for
	 * which the gradients are provided.
	 * 
	 * @param optimiser
	 * @param params
	 * @param gradients
	 * @param weightDecayLambda
	 */
	public static void minimise
	(
		final Optimiser optimiser, 
		final FVector params, 
		final FVector gradients, 
		final float weightDecayLambda
	)
	{
		final FVector weightDecayVector = new FVector(params);
		weightDecayVector.mult(weightDecayLambda);
		optimiser.minimiseObjective(params, gradients);
		params.subtract(weightDecayVector);
	}
	
	/**
	 * Runs a gradient ascent step + weight decay to maximise some objective for
	 * which the gradients are provided.
	 * 
	 * @param optimiser
	 * @param params
	 * @param gradients
	 * @param weightDecayLambda
	 */
	public static void maximise
	(
		final Optimiser optimiser, 
		final FVector params, 
		final FVector gradients, 
		final float weightDecayLambda
	)
	{
		final FVector weightDecayVector = new FVector(params);
		weightDecayVector.mult(weightDecayLambda);
		optimiser.maximiseObjective(params, gradients);
		params.subtract(weightDecayVector);
	}
	
	//-------------------------------------------------------------------------

}
