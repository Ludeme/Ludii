package training.expert_iteration.gradients;

import main.collections.FVector;
import metadata.ai.heuristics.Heuristics;
import training.expert_iteration.ExItExperience;

/**
 * Class with helper methods to compute gradients for self-play training
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

}
