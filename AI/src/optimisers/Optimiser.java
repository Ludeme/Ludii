package optimisers;

import java.io.Serializable;

import main.collections.FVector;

/**
 * Base class for optimizers. All optimizers are pretty much assumed to be
 * variants of Mini-Batch Gradient Descent.
 * 
 * @author Dennis Soemers
 */
public abstract class Optimiser implements Serializable
{
	
	//-------------------------------------------------------------------------
	
	/** */
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------
	
	/** Base step-size (or learning rate) to use */
	protected final float baseStepSize;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param baseStepSize
	 */
	public Optimiser(final float baseStepSize)
	{
		this.baseStepSize = baseStepSize;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Should be implemented to adjust the given vector of parameters in an
	 * attempt to maximise an objective function. The objective function is
	 * implied by a vector of (estimates of) gradients of that objective
	 * function with respect to the trainable parameters.
	 * 
	 * @param params 
	 * 	Parameters to train
	 * @param gradients 
	 * 	Vector of (estimates of) gradients of objective with respect to params.
	 */
	public abstract void maximiseObjective(final FVector params, final FVector gradients);
	
	/**
	 * Calls maximiseObjective() with negated gradients, in order to minimize
	 * the objective.
	 * 
	 * @param params
	 * @param gradients
	 */
	public final void minimiseObjective(final FVector params, final FVector gradients)
	{
		final FVector negatedGrads = gradients.copy();
		negatedGrads.mult(-1.f);
		maximiseObjective(params, negatedGrads);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Writes this optimiser's internal state to a binary file
	 * @param filepath
	 */
	public abstract void writeToFile(final String filepath);
	
	//-------------------------------------------------------------------------

}
