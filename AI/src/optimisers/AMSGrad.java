package optimisers;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import main.collections.FVector;

/**
 * AMSGrad optimizer, with the original bias corrections from Adam
 * included again.
 * 
 * @author Dennis Soemers
 */
public class AMSGrad extends Optimiser
{
	
	//-------------------------------------------------------------------------
	
	/** */
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------

	/** beta_1 constant */
	protected final float beta1;
	
	/** beta_2 constant */
	protected final float beta2;
	
	/** Small constant added to denominator */
	protected final float epsilon;
	
	/** Moving average of gradients */
	private FVector movingAvgGradients = null;
	
	/** Moving average of squared gradients */
	private FVector movingAvgSquaredGradients = null;
	
	/** 
	 * Vector of maximum values encountered for moving averages of 
	 * squared gradients 
	 */
	private FVector maxMovingAvgSquaredGradients = null;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param baseStepSize
	 */
	public AMSGrad(final float baseStepSize) 
	{
		super(baseStepSize);
		this.beta1 = 0.9f;
		this.beta2 = 0.999f;
		this.epsilon = 1.E-8f;
	}
	
	/**
	 * Constructor
	 * 
	 * @param baseStepSize
	 * @param beta1
	 * @param beta2
	 * @param epsilon
	 */
	public AMSGrad
	(
		final float baseStepSize, 
		final float beta1, 
		final float beta2, 
		final float epsilon
	)
	{
		super(baseStepSize);
		this.beta1 = beta1;
		this.beta2 = beta2;
		this.epsilon = epsilon;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void maximiseObjective(
			final FVector params, 
			final FVector gradients) {
		
		if (movingAvgGradients == null)
		{
			// need to initialize vectors for moving averages
			movingAvgGradients = new FVector(gradients.dim());
			movingAvgSquaredGradients = new FVector(gradients.dim());
			maxMovingAvgSquaredGradients = new FVector(gradients.dim());
		}
		else
		{
			// may have to grow moving average vectors if feature set grew
			while (movingAvgGradients.dim() < gradients.dim())
			{
				movingAvgGradients = movingAvgGradients.append(0.f);
				movingAvgSquaredGradients = 
						movingAvgSquaredGradients.append(0.f);
				maxMovingAvgSquaredGradients = 
						maxMovingAvgSquaredGradients.append(0.f);
			}
		}
		
		// update moving averages
		movingAvgGradients.mult(beta1);
		movingAvgGradients.addScaled(gradients, (1.f - beta1));
		final FVector gradientsSquared = gradients.copy();
		gradientsSquared.hadamardProduct(gradientsSquared);
		movingAvgSquaredGradients.mult(beta2);
		movingAvgSquaredGradients.addScaled(gradientsSquared, (1.f - beta2));
		
		maxMovingAvgSquaredGradients = FVector.elementwiseMax(
				maxMovingAvgSquaredGradients,
				movingAvgSquaredGradients);
		
		// compute update
		final FVector velocity = movingAvgGradients.copy();
		// division by 1 - beta1 is bias correction from Adam
		velocity.mult(baseStepSize / (1.f - beta1));
		final FVector denominator = maxMovingAvgSquaredGradients.copy();
		// another bias correction from Adam
		denominator.div(1.f - beta2);
		denominator.sqrt();
		denominator.add(epsilon);
		velocity.elementwiseDivision(denominator);
		
		params.add(velocity);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param lines
	 * @return Constructs an AMSGrad object from instructions in the 
	 * given array of lines
	 */
	public static AMSGrad fromLines(final String[] lines)
	{
		float baseStepSize = 3E-4f;
		float beta1 = 0.9f;
		float beta2 = 0.999f;
		float epsilon = 1.E-8f;
		
		for (String line : lines)
		{
			final String[] lineParts = line.split(",");
			
			//-----------------------------------------------------------------
			// main parts
			//-----------------------------------------------------------------
			if (lineParts[0].toLowerCase().startsWith("basestepsize="))
			{
				baseStepSize = Float.parseFloat(
						lineParts[0].substring("basestepsize=".length()));
			}
			else if (lineParts[0].toLowerCase().startsWith("beta1="))
			{
				beta1 = Float.parseFloat(
						lineParts[0].substring("beta1=".length()));
			}
			else if (lineParts[0].toLowerCase().startsWith("beta2="))
			{
				beta2 = Float.parseFloat(
						lineParts[0].substring("beta2=".length()));
			}
			else if (lineParts[0].toLowerCase().startsWith("epsilon="))
			{
				epsilon = Float.parseFloat(
						lineParts[0].substring("epsilon=".length()));
			}
		}
		
		return new AMSGrad(baseStepSize, beta1, beta2, epsilon);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void writeToFile(final String filepath)
	{
		try 
		(
			final ObjectOutputStream out = 
				new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filepath)))
		)
		{
			out.writeObject(this);
			out.flush();
			out.close();
		} 
		catch (final IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------

}
