package optimisers;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import main.collections.FVector;

/**
 * A variant of RMSProp that, as far as we're able to tell, DeepMind tends
 * to use more often than standard RMSProp (for example in the original
 * DQN Lua code).
 * 
 * The primary differences in comparison to regular RMSProp are:
 * 	1) Usage of plain (not Nesterov) momentum
 * 	2) Centering by subtracting moving average of gradients in denominator.
 * 	This means that gradients are normalized by the estimated variance of
 * 	gradient, rather than the uncentered second moment (according to comments
 * 	in TensorFlow implementation).
 * 
 * This implementation specifically follows Equations (38) - (41) from
 * https://arxiv.org/abs/1308.0850, which seems to be one of the only
 * (if not the only) published sources for this particular variant of
 * RMSProp.
 * 
 * The TensorFlow implementation of RMSProp appears to be identical to this,
 * when using momentum > 0.0 and centered = True.
 * 
 * @author Dennis Soemers
 */
public class DeepmindRMSProp extends Optimiser
{
	
	//-------------------------------------------------------------------------
	
	/** */
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------
	
	/** 
	 * Momentum term. 
	 * "Velocity" of previous update is scaled by this value and added to
	 * subsequent update.
	 */
	protected final float momentum;
	
	/**
	 * Decay factor used in updates of moving averages of (squared) gradients.
	 */
	protected final float decay;
	
	/** Small constant added to denominator */
	protected final float epsilon;
	
	/**
	 * Last "velocity" vector. Used for momentum.
	 */
	private FVector lastVelocity = null;
	
	/** Moving average of gradients */
	private FVector movingAvgGradients = null;
	
	/** Moving average of squared gradients */
	private FVector movingAvgSquaredGradients = null;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public DeepmindRMSProp() 
	{
		//super(0.005f);
		super(0.05f);
		this.momentum = 0.9f;
		//this.momentum = 0.f;
		this.decay = 0.9f;
		this.epsilon = 1.E-8f;
	}
	
	/**
	 * Constructor
	 * 
	 * @param baseStepSize
	 */
	public DeepmindRMSProp(final float baseStepSize) 
	{
		super(baseStepSize);
		this.momentum = 0.9f;
		//this.momentum = 0.f;
		this.decay = 0.9f;
		this.epsilon = 1.E-8f;
	}
	
	/**
	 * Constructor
	 * 
	 * @param baseStepSize
	 * @param momentum
	 * @param decay
	 * @param epsilon
	 */
	public DeepmindRMSProp
	(
		final float baseStepSize, 
		final float momentum, 
		final float decay, 
		final float epsilon
	)
	{
		super(baseStepSize);
		this.momentum = momentum;
		this.decay = decay;
		this.epsilon = epsilon;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void maximiseObjective
	(
		final FVector params, 
		final FVector gradients
	) 
	{
		final FVector velocity = gradients.copy();
		velocity.mult(baseStepSize / velocity.dim());
		
		if (movingAvgGradients == null)
		{
			// need to initialize vectors for moving averages
			movingAvgGradients = new FVector(gradients.dim());
			movingAvgSquaredGradients = new FVector(gradients.dim());
		}
		else
		{
			// may have to grow moving average vectors if feature set grew
			while (movingAvgGradients.dim() < gradients.dim())
			{
				movingAvgGradients = movingAvgGradients.append(0.f);
				movingAvgSquaredGradients = movingAvgSquaredGradients.append(0.f);
			}
		}
		
		// update moving averages
		movingAvgGradients.mult(decay);
		movingAvgGradients.addScaled(gradients, (1.f - decay));
		final FVector gradientsSquared = gradients.copy();
		gradientsSquared.hadamardProduct(gradientsSquared);
		movingAvgSquaredGradients.mult(decay);
		movingAvgSquaredGradients.addScaled(gradientsSquared, (1.f - decay));
		
		// use them to divide the new velocity
		final FVector denominator = movingAvgSquaredGradients.copy();
		final FVector temp = movingAvgGradients.copy();
		temp.hadamardProduct(temp);
		denominator.subtract(temp);
		denominator.add(epsilon);
		denominator.sqrt();
		
		velocity.elementwiseDivision(denominator);
		
		// add momentum
		if (momentum > 0.f && lastVelocity != null)
		{
			while (lastVelocity.dim() < velocity.dim())
			{
				// feature set has grown, so also need to grow the lastVelocity vector
				lastVelocity = lastVelocity.append(0.f);
			}
			
			velocity.addScaled(lastVelocity, momentum);
		}
				
		params.add(velocity);
		lastVelocity = velocity;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param lines
	 * @return Constructs an RMSProp object from instructions in the 
	 * given array of lines
	 */
	public static DeepmindRMSProp fromLines(final String[] lines)
	{
		float baseStepSize = 0.005f;
		float momentum = 0.9f;
		float decay = 0.9f;
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
			else if (lineParts[0].toLowerCase().startsWith("momentum="))
			{
				momentum = Float.parseFloat(
						lineParts[0].substring("momentum=".length()));
			}
			else if (lineParts[0].toLowerCase().startsWith("decay="))
			{
				decay = Float.parseFloat(
						lineParts[0].substring("decay=".length()));
			}
			else if (lineParts[0].toLowerCase().startsWith("epsilon="))
			{
				epsilon = Float.parseFloat(
						lineParts[0].substring("epsilon=".length()));
			}
		}
		
		return new DeepmindRMSProp(baseStepSize, momentum, decay, epsilon);
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
