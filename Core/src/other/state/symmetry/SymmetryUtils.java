package other.state.symmetry;

import java.awt.geom.Point2D;
import java.util.BitSet;

/**
 * Utilities to support symmetry processing
 * @author mrraow
 */
public class SymmetryUtils 
{
	/**
	 * Note that ludii counts players from 1 upwards, with element 0 reserved for 'empty' and other shenanigans
	 * This function will provide all permutations of { 1... n } keel=ping 0 in the bottom index 
	 * @param numPlayers
	 * @return a valid permutation of these players 
	 */
	public static final int[][] playerPermutations (int numPlayers)
	{
		// Special case; if there are > 4 players, we have too many cases to sensibly identify, so just return the identity operator
		if (numPlayers > 4) 
		{
			int[][] permutations = new int[1][numPlayers+1];
			for (int who = 1; who <= numPlayers; who++)
				permutations[0][who] = who;
			return permutations;
		}
		
		// Degenerate case
		if (numPlayers==1) return new int[][] { { 0, 1 } };

		// On with the plot
		int[][] permutations = new int[factorial(numPlayers)][numPlayers+1];
		
		for (int who = 1; who <= numPlayers; who++)
			permutations[0][who] = who;
		
		for (int p = 1; p < permutations.length; p++)
			permutations[p] = nextPermutation(permutations[p-1]);
		
		return permutations;
	}

	/**
	 * @param numPlayers
	 * @return numPlayers!
	 */
	private static int factorial(int numPlayers) 
	{
		int product = 1;
		for (int n = 1; n <= numPlayers; n++) 
			product *= n;
		
		return product;
	}
	
	private static int[] nextPermutation (final int[] previous)
	{
		final int[] next = previous.clone();

        // Find last j : next[j] < next[j+1]
        int j = previous.length - 2;
        while (j >= 1 && next[j] > next[j+1]) j--;

        // Find last l : next[j] <= next[l]
        int l = previous.length - 1;
        while (next[j] > next[l]) l--;

        // Swap
        swap (next, j, l);
        
        // L4: Reverse elements j+1 ... count-1:
        int lo = j + 1;
        int hi = previous.length - 1;
        while (lo < hi) swap(next, lo++, hi--);
        
        return next;
    }
	
	private static final void swap (final int[] array, final int idx1, final int idx2) 
	{
		final int temp = array[idx1];
		array[idx1] = array[idx2];
		array[idx2] = temp;
	}

	/**
	 * @param op1
	 * @param op2
	 * @return the result when op1 then op2 are applied in order
	 */
	public static int[] combine(int[] op1, int[] op2) 
	{
		final int[] result = new int[op1.length];
		
		for (int idx = 0; idx < op1.length; idx++)
			result[idx] = op2[op1[idx]];
			
		return result;
	}

	/**
	 * @param origin
	 * @param source
	 * @param steps
	 * @param numSymmetries
	 * @return a point corresponding to source rotated around origin by the specified fraction of a circle
	 */
	public static Point2D rotateAroundPoint (final Point2D origin, final Point2D source, final int steps, final int numSymmetries)
	{
		final double angle = Math.PI * 2.0 * steps / numSymmetries;
		
		final double normalisedX = source.getX() - origin.getX();
		final double normalisedY = source.getY() - origin.getY();
		
		final double rotatedX = normalisedX*Math.cos(angle) - normalisedY*Math.sin(angle);
		final double rotatedY = normalisedY*Math.cos(angle) + normalisedX*Math.sin(angle);
		
		return new Point2D.Double(origin.getX()+rotatedX, origin.getY()+rotatedY);
	}
	
	/**
	 * @param origin
	 * @param source
	 * @param steps
	 * @param numSymmetries
	 * @return a point corresponding to source reflected around a line through origin with the specified angle
	 */
	public static Point2D reflectAroundLine (final Point2D origin, final Point2D source, final int steps, final int numSymmetries)
	{
		// Special cases because tan(PI/2) is asymptotic
		if (2*steps == numSymmetries) 
		{
			final double reflectedY = source.getY();
			final double reflectedX = origin.getX()*2 - source.getX();
			return new Point2D.Double(reflectedX, reflectedY);
		}
		
		final double angle = Math.PI * steps / numSymmetries;
		
		// Find the line y = mx+c
		final double m = Math.tan(angle);
		final double c = origin.getY() - m * origin.getX(); // calculate c = y - mx 

		// Reflect around the line...
		final double d = (source.getX() + (source.getY() - c)*m)/(1 + m*m);
		final double reflectedX = 2*d - source.getX();
		final double reflectedY = 2*d*m - source.getY() + 2*c;
		
		return new Point2D.Double(reflectedX, reflectedY);
	}

	/**
	 * @param p1
	 * @param p2
	 * @param allowedError
	 * @return true if two points are close enough to be considered the same (allows for precision errors in trig functions) 
	 */
	public static boolean closeEnough (final Point2D p1, final Point2D p2, final double allowedError)
	{
		return p1.distance(p2) <= allowedError;
	}

	/**
	 * @param mapping
	 * @return Returns true if the mapping maps every entry uniquely to a number in the range 0... length-1
	 */
	public static boolean isBijective (final int[] mapping)
	{
		BitSet set = new BitSet(mapping.length);
		for (int cell : mapping) 
		{
			if (cell < 0 || cell >= mapping.length) return false;
			set.set(cell);
		}
		
		return set.cardinality() == mapping.length;
	}	
}
