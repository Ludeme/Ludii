package optimisers;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import main.collections.FVector;

/**
 * A standard Stochastic Gradient Descent optimiser, with optional support
 * for a simple momentum term.
 * 
 * @author Dennis Soemers
 */
public class SGD extends Optimiser
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
	 * Last "velocity" vector. Used for momentum.
	 */
	private FVector lastVelocity = null;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param baseStepSize
	 */
	public SGD(final float baseStepSize)
	{
		super(baseStepSize);
		this.momentum = 0.f;
	}
	
	/**
	 * Constructor with momentum
	 * 
	 * @param baseStepSize
	 * @param momentum
	 */
	public SGD(final float baseStepSize, final float momentum)
	{
		super(baseStepSize);
		this.momentum = momentum;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void maximiseObjective(
			final FVector params, 
			final FVector gradients) {
		
		final FVector velocity = gradients.copy();
		velocity.mult(baseStepSize);
		
		if (momentum > 0.f && lastVelocity != null)
		{
			while (lastVelocity.dim() < velocity.dim())
			{
				// feature set has grown, so also need to grow the lastVelocity
				// vector
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
	 * @return Constructs an SGD object from instructions in the 
	 * given array of lines
	 */
	public static SGD fromLines(final String[] lines)
	{
		float baseStepSize = 0.05f;
		float momentum = 0.f;
		
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
		}
		
		return new SGD(baseStepSize, momentum);
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
