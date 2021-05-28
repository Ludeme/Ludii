package graphics;

import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

//------------------------------------

/** 
 * Image processing filters.
 */
public class Filters 
{
	/**
	 * Prepares a convolve operator for a Gaussian blur filter.
	 * @param radius radius of blur in pixels.
	 * @param horizontal whether the blur is horizontal or vertical.
	 * @return corresponding convolve operator.
	 */
	public static ConvolveOp gaussianBlurFilter(int radius, boolean horizontal) 
	{
		if (radius < 1) 
		{
			System.out.printf("radius=%d.\n", Integer.valueOf(radius));
			//throw new IllegalArgumentException("Radius must be >= 1"); 
			return null;
		}
	        
		int size = radius * 2 + 1;
	    float[] data = new float[size];
        
        float sigma = radius / 3.0f;
        float twoSigmaSquare = 2.0f * sigma * sigma;
        float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
        float total = 0.0f;
        
        for (int i = -radius; i <= radius; i++) 
        {
            float distance = i * i;
            int index = i + radius;
            data[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
            total += data[index];
        }
        
        for (int i = 0; i < data.length; i++)	
        	data[i] /= total;
        
        Kernel kernel = horizontal ? new Kernel(size, 1, data) : new Kernel(1, size, data);
        
        return new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
    }
}