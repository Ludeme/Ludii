package main.collections;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;

/**
 * Wrapper around an array of floats, with various "vectorised" methods
 * that conveniently do the looping over a raw array for us.
 * 
 * This class should be more efficient than something like TFloatArrayList
 * when the dimensionality of vectors remain fixed. This is especially the
 * case when various vector-wide math operations supported by this class
 * are used, rather than manual loops over the data.
 * 
 * Whenever we want to insert or cut out entries (modify the dimensionality), 
 * we have to construct new instances. This is less efficient.
 * 
 * @author Dennis Soemers
 */
public final class FVector implements Serializable 
{
	
	//-------------------------------------------------------------------------
	
	/** */
	private static final long serialVersionUID = 1L;
	
	/** Our raw array of floats */
	protected final float[] floats;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Creates a new vector of dimensionality d (filled with 0s)
	 * @param d
	 */
	public FVector(final int d)
	{
		floats = new float[d];
	}
	
	/**
	 * Creates a new vector of dimensionality d filled with the given value in 
	 * all entries
	 * @param d
	 * @param fillValue
	 */
	public FVector(final int d, final float fillValue)
	{
		floats = new float[d];
		Arrays.fill(floats, fillValue);
	}
	
	/**
	 * Creates a new vector with a copy of the given array of floats as data.
	 * If a copy is not necessary, use FVector.wrap(floats) or 
	 * FVector(floats, true) instead!
	 * @param floats
	 */
	public FVector(final float[] floats)
	{
		this.floats = new float[floats.length];
		System.arraycopy(floats, 0, this.floats, 0, floats.length); 
	}
	
	/**
	 * Copy constructor
	 * @param other
	 */
	public FVector(final FVector other)
	{
		this(other.floats);
	}
	
	/**
	 * Constructor
	 * @param floats
	 */
	public FVector(final TFloatArrayList floats)
	{
		this.floats = floats.toArray();
	}
	
	/**
	 * Creates a new vector that "steals" the given array of floats if 
	 * steal = true (directly uses it as raw data rather than copying it).
	 * @param floats
	 * @param steal
	 */
	public FVector(final float[] floats, final boolean steal)
	{
		if (!steal)
		{
            throw new IllegalArgumentException(
            		"steal must be true when instantiating a vector that "
            		+ "steals data");
		}
		
		this.floats = floats;
	}
	
	/**
	 * @return A copy of this vector (slightly shorter to type than 
	 * the copy constructor)
	 */
	public FVector copy()
	{
		return new FVector(this);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param d Dimensionality
	 * @return A vector filled with 1s of the given dimensionality
	 */
	public static FVector ones(final int d)
	{
		final FVector ones = new FVector(d);
		ones.fill(0, d, 1.f);
		return ones;
	}
	
	/**
	 * @param d Dimensionality
	 * @return A vector filled with 0s of the given dimensionality
	 */
	public static FVector zeros(final int d)
	{
		return new FVector(d);
	}
	
	/**
	 * Note that this method will "steal" the array it is given (it will
	 * directly use the same reference for its data).
	 * 
	 * @param floats
	 * @return A new vector that wraps around the given array of floats.
	 */
	public static FVector wrap(final float[] floats)
	{
		return new FVector(floats, true);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Index of the maximum value in this vector
	 */
	public int argMax()
	{
		float max = Float.NEGATIVE_INFINITY;
		final int d = floats.length;
		int maxIdx = -1;
		for (int i = 0; i < d; ++i)
		{
			if (floats[i] > max)
			{
				max = floats[i];
				maxIdx = i;
			}
		}
		return maxIdx;
	}
	
	/**
	 * @return Index of the maximum value in this vector, with random 
	 * tie-breaking
	 */
	public int argMaxRand()
	{
		float max = Float.NEGATIVE_INFINITY;
		final int d = floats.length;
		int maxIdx = -1;
		int numMaxFound = 0;
		for (int i = 0; i < d; ++i)
		{
			final float val = floats[i];
			if (val > max)
			{
				max = val;
				maxIdx = i;
				numMaxFound = 1;
			}
			else if 
			(
				val == max && 
				ThreadLocalRandom.current().nextInt() % ++numMaxFound == 0
			)
			{
				maxIdx = i;
			}
		}
		return maxIdx;
	}
	
	/**
	 * @return Index of the minimum value in this vector
	 */
	public int argMin()
	{
		float min = Float.POSITIVE_INFINITY;
		final int d = floats.length;
		int minIdx = -1;
		for (int i = 0; i < d; ++i)
		{
			if (floats[i] < min)
			{
				min = floats[i];
				minIdx = i;
			}
		}
		return minIdx;
	}
	
	/**
	 * @return Index of the minimum value in this vector, with random 
	 * tie-breaking
	 */
	public int argMinRand()
	{
		float min = Float.POSITIVE_INFINITY;
		final int d = floats.length;
		int minIdx = -1;
		int numMinFound = 0;
		for (int i = 0; i < d; ++i)
		{
			final float val = floats[i];
			if (val < min)
			{
				min = val;
				minIdx = i;
				numMinFound = 1;
			}
			else if (val == min && 
					ThreadLocalRandom.current().nextInt() % ++numMinFound == 0)
			{
				minIdx = i;
			}
		}
		return minIdx;
	}
	
	/**
	 * @return Dimensionality of the vector
	 */
	public int dim()
	{
		return floats.length;
	}
	
	/**
	 * @param entry
	 * @return Entry at given index
	 */
	public float get(final int entry)
	{
		return floats[entry];
	}
	
	/**
	 * @return Maximum value in this vector
	 */
	public float max()
	{
		float max = Float.NEGATIVE_INFINITY;
		final int d = floats.length;
		for (int i = 0; i < d; ++i)
		{
			if (floats[i] > max)
			{
				max = floats[i];
			}
		}
		return max;
	}
	
	/**
	 * @return Minimum value in this vector
	 */
	public float min()
	{
		float min = Float.POSITIVE_INFINITY;
		final int d = floats.length;
		for (int i = 0; i < d; ++i)
		{
			if (floats[i] < min)
			{
				min = floats[i];
			}
		}
		return min;
	}
	
	/**
	 * @return Mean of all the entries in this vector
	 */
	public float mean()
	{
		return sum() / floats.length;
	}
	
	/**
	 * @return The norm (L2-norm) of the vector
	 */
	public double norm()
	{
		float sumSquares = 0.f;
		final int d = floats.length;
		for (int i = 0; i < d; ++i)
		{
			sumSquares += floats[i] * floats[i];
		}
		return Math.sqrt(sumSquares);
	}
	
	/**
	 * Sets the given entry to the given value
	 * @param entry
	 * @param value
	 */
	public void set(final int entry, final float value)
	{
		floats[entry] = value;
	}
	
	/**
	 * @return Sum of the values in this vector
	 */
	public float sum()
	{
		float sum = 0.f;
		final int d = floats.length;
		for (int i = 0; i < d; ++i)
		{
			sum += floats[i];
		}
		return sum;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Replaces every entry in the vector with the absolute value of that entry.
	 * Note that this modifies the vector in-place
	 */
	public void abs()
	{
		final int d = floats.length;
		for (int i = 0; i < d; ++i)
		{
			floats[i] = Math.abs(floats[i]);
		}
	}
	
	/**
	 * Adds the given value to all entries. Note that this is a mathematical 
	 * add operation, not an append operation! 
	 * Note that this modifies the vector in-place
	 * @param value
	 */
	public void add(final float value)
	{
		final int d = floats.length;
		for (int i = 0; i < d; ++i)
		{
			floats[i] += value;
		}
	}
	
	/**
	 * Adds the entries in the given array to the corresponding entries 
	 * in this vector.
	 * Note that this modifies the vector in-place.
	 * @param toAdd
	 */
	public void add(final float[] toAdd)
	{
		final int d = floats.length;
		for (int i = 0; i < d; ++i)
		{
			floats[i] += toAdd[i];
		}
	}
	
	/**
	 * Adds the given other vector to this one. 
	 * Note that this modifies the vector in-place
	 * @param other
	 */
	public void add(final FVector other)
	{
		add(other.floats);
	}
	
	/**
	 * Adds the given value to one specific entry. 
	 * Note that this modifies the vector in-place
	 * @param entry
	 * @param value
	 */
	public void addToEntry(final int entry, final float value)
	{
		floats[entry] += value;
	}
	
	/**
	 * Adds the given other vector, scaled by the given scalar, to this one.
	 * Note that this modifies this vector in-place, but does not modify the 
	 * other vector.
	 * 
	 * Using this method is slightly more efficient than copying the other 
	 * vector, scaling it, and then adding it (because it avoids the copy), 
	 * but still avoids modifying the other vector without a copy.
	 * 
	 * @param other
	 * @param scalar
	 */
	public void addScaled(final FVector other, final float scalar)
	{
		final int d = floats.length;
		final float[] otherFloats = other.floats;
		for (int i = 0; i < d; ++i)
		{
			floats[i] += otherFloats[i] * scalar;
		}
	}
	
	/**
	 * Divides the vector by the given scalar. 
	 * Note that this modifies the vector in-place
	 * @param scalar
	 */
	public void div(final float scalar)
	{
		final int d = floats.length;
		final float mult = 1.f / scalar;
		for (int i = 0; i < d; ++i)
		{
			floats[i] *= mult;
		}
	}
	
	/**
	 * Performs element-wise division by the other vector.
	 * Note that this modifies the vector in-place
	 * @param other
	 */
	public void elementwiseDivision(final FVector other)
	{
		final int d = floats.length;
		final float[] otherFloats = other.floats;
		for (int i = 0; i < d; ++i)
		{
			floats[i] /= otherFloats[i];
		}
	}
	
	/**
	 * Computes the hadamard (i.e. element-wise) product with other vector. 
	 * Note that this modifies the vector in-place
	 * @param other
	 */
	public void hadamardProduct(final FVector other)
	{
		final int d = floats.length;
		final float[] otherFloats = other.floats;
		for (int i = 0; i < d; ++i)
		{
			floats[i] *= otherFloats[i];
		}
	}
	
	/**
	 * Takes the natural logarithm of every entry. 
	 * Note that this modifies the vector in-place
	 */
	public void log()
	{
		final int d = floats.length;
		for (int i = 0; i < d; ++i)
		{
			floats[i] = (float) Math.log(floats[i]);
		}
	}
	
	/**
	 * Multiplies the vector with the given scalar. 
	 * Note that this modifies the vector in-place
	 * @param scalar
	 */
	public void mult(final float scalar)
	{
		final int d = floats.length;
		for (int i = 0; i < d; ++i)
		{
			floats[i] *= scalar;
		}
	}
	
	/**
	 * Raises all entries in the vector to the given power. 
	 * Note that this modifies the vector in-place
	 * @param power
	 */
	public void raiseToPower(final double power)
	{
		final int d = floats.length;
		for (int i = 0; i < d; ++i)
		{
			floats[i] = (float) Math.pow(floats[i], power);
		}
	}
	
	/**
	 * Normalises this vector such that it sums up to 1, by
	 * dividing all entries by the sum of entries.
	 */
	public void normalise()
	{
		final int d = floats.length;
		
		float sum = 0.f;
		for (int i = 0; i < d; ++i)
		{
			sum += floats[i];
		}
		
		if (sum == 0.f)
		{
			// Probably a single-element vector with a 0.f entry; just make it uniform
			Arrays.fill(floats, 1.f / floats.length);
		}
		else
		{
			final float scalar = 1.f / sum;
			
			for (int i = 0; i < d; ++i)
			{
				floats[i] *= scalar;
			}
		}
	}
	
	/**
	 * Replaces every entry in the vector with the sign of that entry (-1.f, 0.f, or +1.f).
	 * Note that this modifies the vector in-place
	 */
	public void sign()
	{
		final int d = floats.length;
		for (int i = 0; i < d; ++i)
		{
			floats[i] = (floats[i]) > 0.f ? +1.f : (floats[i] < 0.f ? -1.f : 0.f);
		}
	}
	
	/**
	 * Computes the softmax of this vector. 
	 * Note that this modifies the vector in-place
	 */
	public void softmax()
	{
		final int d = floats.length;
		
		// for numeric stability, subtract max entry before computing exponent
		final float max = max();
		double sumExponents = 0.0;
		
		for (int i = 0; i < d; ++i)
		{
			final double exp = Math.exp(floats[i] - max);
			sumExponents += exp;
			floats[i] = (float) exp;	// already put the exponent in the array
		}
		
		// now just need to divide all entries by sum of exponents
		div((float) sumExponents);
	}
	
	/**
	 * This method assumes that the vector currently already encodes a discrete 
	 * probability distribution, computed by a softmax. It incrementally updates 
	 * the vector to account for the new information that the entry at the given
	 * index is invalid. The invalid entry will be set to 0.0, and any other
	 * entries will be updated such that the vector is equal as if the softmax 
	 * had been computed from scratch without the invalid entry.
	 * 
	 * @param invalidEntry
	 */
	public void updateSoftmaxInvalidate(final int invalidEntry)
	{
		final float invalidProb = floats[invalidEntry];
		floats[invalidEntry] = 0.f;
		
		if (invalidProb < 1.f)
		{
			final float scalar = 1.f / (1.f - invalidProb);
			final int d = floats.length;
			for (int i = 0; i < d; ++i)
			{
				floats[i] *= scalar;
			}
		}
	}
	
	/**
	 * Computes the softmax of this vector, with a temperature parameter
	 * (making it the same as computing a Boltzmann distribution).
	 * 
	 * Temperature --> 0 puts all probability mass on max.
	 * Temperature = 1 gives regular softmax.
	 * Temperature > 1 gives more uniform distribution.
	 * 
	 * Note that this modifies the vector in-place
	 */
	public void softmax(final double temperature)
	{
		final int d = floats.length;
		
		// for numeric stability, subtract max entry before computing exponent
		final float max = max();
		double sumExponents = 0.0;
		
		for (int i = 0; i < d; ++i)
		{
			final double exp = Math.exp((floats[i] - max) / temperature);
			sumExponents += exp;
			floats[i] = (float) exp;	// already put the exponent in the array
		}
		
		// now just need to divide all entries by sum of exponents
		div((float) sumExponents);
	}
	
	/**
	 * Takes the square root of every element in the vector
	 */
	public void sqrt()
	{
		final int d = floats.length;
		for (int i = 0; i < d; ++i)
		{
			floats[i] = (float) Math.sqrt(floats[i]);
		}
	}
	
	/**
	 * Subtracts the given value from all entries. 
	 * Note that this modifies the vector in-place
	 * @param value
	 */
	public void subtract(final float value)
	{
		final int d = floats.length;
		for (int i = 0; i < d; ++i)
		{
			floats[i] -= value;
		}
	}
	
	/**
	 * Subtracts the entries in the given array from the corresponding entries 
	 * in this vector.
	 * Note that this modifies the vector in-place.
	 * @param toSubtract
	 */
	public void subtract(final float[] toSubtract)
	{
		final int d = floats.length;
		for (int i = 0; i < d; ++i)
		{
			floats[i] -= toSubtract[i];
		}
	}
	
	/**
	 * Subtracts the given other vector from this one. 
	 * Note that this modifies the vector in-place
	 * @param other
	 */
	public void subtract(final FVector other)
	{
		subtract(other.floats);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Samples an index from the discrete distribution encoded by this vector.
	 * This can, for example, be used to sample from a vector that has been
	 * transformed into a distribution using softmax().
	 * 
	 * NOTE: implementation assumes that the vector already is a proper
	 * distribution, i.e. that it sums up to 1. This is not verified.
	 * 
	 * @return Index sampled from discrete distribution
	 */
	public int sampleFromDistribution()
	{
		final float rand = ThreadLocalRandom.current().nextFloat();
		final int d = floats.length;
		
		float accum = 0.f;
		for (int i = 0; i < d; ++i)
		{
			accum += floats[i];
			
			if (rand < accum)
			{
				return i;
			}
		}
		
		// This should never happen mathematically, 
		// but guess it might sometimes due to floating point inaccuracies
		for (int i = d - 1; i > 0; --i)
		{
			if (floats[i] > 0.f)
				return i;
		}
		
		// This should REALLY never happen
		return 0;
	}
	
	/**
	 * Samples an index proportional to the values in this vector. For example,
	 * if the value at index 0 is twice as large as all other values, index 0
	 * has twice as large a probability of being sampled as each of those other
	 * indices.
	 * 
	 * This method does not require the vector to encode a probability 
	 * distribution (the entries do not need to sum up to 1.0). However, if it 
	 * DOES encode a probability distribution (if the entries do sum up to 1.0), 
	 * every value will simply denote the probability of that index getting 
	 * picked.
	 * 
	 * Note that using this method to sample from a raw vector will lead to a 
	 * different distribution than if it is used to sampled from a raw vector
	 * that has been transformed to a proper probability distribution using a
	 * non-linear transformation (such as softmax).
	 * 
	 * Implicitly assumes that all values in the vector are >= 0.0, but does
	 * not check for this!
	 * 
	 * @return An index sampled proportionally to the values in this vector.
	 */
	public int sampleProportionally()
	{
		final float sum = sum();
		
		if (sum == 0.0)
		{
			// Special case: just sample uniformly if the values sum up to 0.0
			return ThreadLocalRandom.current().nextInt(floats.length);
		}
		
		final float rand = ThreadLocalRandom.current().nextFloat();
		final int d = floats.length;
		
		float accum = 0.f;
		for (int i = 0; i < d; ++i)
		{
			accum += floats[i] / sum;
			
			if (rand < accum)
			{
				return i;
			}
		}
		
		// this should never happen mathematically, 
		// but guess it might sometimes due to floating point inaccuracies
		return d - 1;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param other
	 * @return Computes the dot product with the other vector
	 */
	public float dot(final FVector other)
	{
		float sum = 0.f;
		final float[] otherFloats = other.floats;
		final int d = floats.length;
		for (int i = 0; i < d; ++i)
		{
			sum += floats[i] * otherFloats[i];
		}
		return sum;
	}
	
	/**
	 * @param sparseBinary
	 * @return Dot product between this vector and a TIntArrayList interpreted 
	 * as a sparse binary vector
	 */
	public float dotSparse(final TIntArrayList sparseBinary)
	{
		float sum = 0.f;
		final int numOnes = sparseBinary.size();
		for (int i = 0; i < numOnes; ++i)
		{
			sum += floats[sparseBinary.getQuick(i)];
		}
		return sum;
	}
	
	/**
	 * @param sparseBinary
	 * @param offset We'll add this offset to every index
	 * @return Dot product between this vector and a TIntArrayList interpreted 
	 * as a sparse binary vector
	 */
	public float dotSparse(final TIntArrayList sparseBinary, final int offset)
	{
		float sum = 0.f;
		final int numOnes = sparseBinary.size();
		for (int i = 0; i < numOnes; ++i)
		{
			sum += floats[sparseBinary.getQuick(i) + offset];
		}
		return sum;
	}
	
	/**
	 * @return Normalised entropy of the vector (assumed to be a distribution)
	 */
	public double normalisedEntropy()
    {
    	final int dim = dim();
    	
    	if (dim <= 1)
       		return 0.0;
    	
    	// compute unnormalised entropy 
    	// (in nats, unit won't matter after normalisation)
    	double entropy = 0.0;
    	
    	for (int i = 0; i < dim; ++i)
    	{
    		final float prob = floats[i];
    		
    		if (prob > 0.f)
    			entropy -= prob * Math.log(prob);
    	}
    	
    	// normalise and return
    	return (entropy / Math.log(dim));
    }
	
	/**
	 * @return True if this vector contains at least one NaN entry. Useful for debugging.
	 */
	public boolean containsNaN()
	{
		for (int i = 0; i < floats.length; ++i)
		{
			if (Float.isNaN(floats[i]))
				return true;
		}
		
		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Fills a block of this vector with the given value
	 * @param startInclusive Index to start filling (inclusive)
	 * @param endExclusive Index at which to end filling (exclusive)
	 * @param val Value to fill with
	 */
	public void fill
	(
		final int startInclusive, 
		final int endExclusive, 
		final float val
	)
	{
		Arrays.fill(floats, startInclusive, endExclusive, val);
	}
	
	/**
	 * Copies floats from given vector into this vector
	 * @param src Vector to copy from
	 * @param srcPos Index in source vector from which to start copying
	 * @param destPos Index in this vector in which to start inserting
	 * @param length Number of entries to copy
	 */
	public void copyFrom
	(
		final FVector src, 
		final int srcPos, 
		final int destPos, 
		final int length
	)
	{
		System.arraycopy(src.floats, srcPos, floats, destPos, length);
	}
	
	/**
	 * @param fromInclusive
	 * @param toExclusive
	 * @return New vector containing only the part in the given range of indices
	 */
	public FVector range(final int fromInclusive, final int toExclusive)
	{
		final float[] newArr = Arrays.copyOfRange(floats, fromInclusive, toExclusive);
		return FVector.wrap(newArr);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param newValue
	 * @return A new vector with the given value appended as extra entry
	 */
	public FVector append(final float newValue)
	{
		final FVector newVector = new FVector(floats.length + 1);
		System.arraycopy(floats, 0, newVector.floats, 0, floats.length);
		newVector.floats[floats.length] = newValue;
		return newVector;
	}
	
	/**
	 * @param entry
	 * @return A new vector where the given entry is cut out
	 */
	public FVector cut(final int entry)
	{
		return cut(entry, entry + 1);
	}
	
	/**
	 * @param startEntryInclusive
	 * @param endEntryExclusive
	 * @return A new vector where all entries between startEntryInclusive and 
	 * endEntryExclusive are cut out
	 */
	public FVector cut
	(
		final int startEntryInclusive, 
		final int endEntryExclusive
	)
	{
		final int newD = 
				floats.length - (endEntryExclusive - startEntryInclusive);
		final FVector newVector = new FVector(newD);
		System.arraycopy(floats, 0, newVector.floats, 0, startEntryInclusive);
		System.arraycopy(
				floats, 
				endEntryExclusive, newVector.floats, 
				startEntryInclusive, floats.length - endEntryExclusive);
		return newVector;
	}
	
	/**
	 * @param index
	 * @param value
	 * @return A new vector where the given extra value is inserted at the 
	 * given index
	 */
	public FVector insert(final int index, final float value)
	{
		FVector newVector = new FVector(floats.length + 1);
		System.arraycopy(floats, 0, newVector.floats, 0, index);
		newVector.floats[index] = value;
		System.arraycopy(
				floats, 
				index, newVector.floats, index + 1, floats.length - index);
		return newVector;
	}
	
	/**
	 * @param index
	 * @param values
	 * @return A new vector with the given block of values inserted at the 
	 * given index
	 */
	public FVector insert(final int index, final float[] values)
	{
		FVector newVector = new FVector(floats.length + values.length);
		System.arraycopy(floats, 0, newVector.floats, 0, index);
		System.arraycopy(values, 0, newVector.floats, index, values.length);
		System.arraycopy(
				floats, 
				index, newVector.floats, 
				index + values.length, floats.length - index);
		return newVector;
	}
	
	/**
	 * @param a
	 * @param b
	 * @return The concatenation of two vectors a and b (new object)
	 */
	public static FVector concat(final FVector a, final FVector b)
	{
		final FVector concat = new FVector(a.dim() + b.dim());
		System.arraycopy(a.floats, 0, concat.floats, 0, a.dim());
		System.arraycopy(b.floats, 0, concat.floats, a.dim(), b.dim());
		return concat;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param trueDist
	 * @param estDist
	 * @return Cross-entropy between a "true" and an "estimated" distribution 
	 * (both discrete distributions encoded by float vectors)
	 */
	public static float crossEntropy(final FVector trueDist, final FVector estDist)
	{
		final int d = trueDist.dim();
		final float[] trueFloats = trueDist.floats;
		final float[] estFloats = estDist.floats;
		float result = 0.f;
		
		for (int i = 0; i < d; ++i)
		{
			result -= trueFloats[i] * Math.log(estFloats[i]);
		}
		
		return result;
	}
	
	/**
	 * @param a
	 * @param b
	 * @return A new vector containing the element-wise maximum value of
	 * the two given vectors for every entry.
	 */
	public static FVector elementwiseMax(final FVector a, final FVector b)
	{
		final int d = a.dim();
		final float[] aFloats = a.floats;
		final float[] bFloats = b.floats;
		final float[] result = new float[d];
		
		for (int i = 0; i < d; ++i)
		{
			result[i] = Math.max(aFloats[i], bFloats[i]);
		}
		
		return FVector.wrap(result);
	}
	
	/**
	 * NOTE: we compute KL divergence using natural log, so the unit will be 
	 * nats rather than bits.
	 * 
	 * @param trueDist
	 * @param estDist
	 * @return KL Divergence = D_{KL} (trueDist || estDist) between true and 
	 * estimated discrete distributions 
	 */
	public static float klDivergence
	(
		final FVector trueDist, 
		final FVector estDist
	)
	{
		final int d = trueDist.dim();
		final float[] trueFloats = trueDist.floats;
		final float[] estFloats = estDist.floats;
		float result = 0.f;
		
		for (int i = 0; i < d; ++i)
		{
			if (trueFloats[i] != 0.f)
			{
				result -= trueFloats[i] * Math.log(estFloats[i] / trueFloats[i]);
			}
		}
		
		return result;
	}
	
	/**
	 * @param vectors
	 * @return The mean vector from the given array of vectors
	 * (i.e. a vector with, at every entry i, the mean of the entries i of 
	 * all vectors)
	 */
	public static FVector mean(final FVector[] vectors)
	{
		final int d = vectors[0].dim();
		final float[] means = new float[d];
		
		for (final FVector vector : vectors)
		{
			final float[] vals = vector.floats;
			for (int i = 0; i < d; ++i)
			{
				means[i] += vals[i];
			}
		}
		
		final FVector meanVector = FVector.wrap(means);
		meanVector.mult(1.f / vectors.length);
		return meanVector;
	}
	
	/**
	 * @param vectors
	 * @return The mean vector from the given list of vectors
	 * (i.e. a vector with, at every entry i, the mean of the entries i of 
	 * all vectors)
	 */
	public static FVector mean(final List<FVector> vectors)
	{
		final int d = vectors.get(0).dim();
		final float[] means = new float[d];
		
		for (final FVector vector : vectors)
		{
			final float[] vals = vector.floats;
			for (int i = 0; i < d; ++i)
			{
				means[i] += vals[i];
			}
		}
		
		final FVector meanVector = FVector.wrap(means);
		meanVector.mult(1.f / vectors.size());
		return meanVector;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Similar to numpy's linspace function. Creates a vector with evenly-spaced
	 * numbers over a specified interval.
	 * 
	 * @param start First value of the vector
	 * @param stop Value to stop at
	 * @param num Number of values to generate
	 * @param endInclusive If true, the vector will include "stop" as last element
	 * @return Constructed vector
	 */
	public static FVector linspace
	(
		final float start, 
		final float stop, 
		final int num, 
		final boolean endInclusive
	)
	{
		final FVector result = new FVector(num);
		
		final float step = endInclusive ? (stop - start) / (num - 1) : (stop - start) / num;
		
		for (int i = 0; i < num; ++i)
		{
			result.set(i, start + i * step);
		}
		
		return result;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return A string representation of this vector containing only numbers 
	 * and commas, i.e. no kinds of brackets. Useful for writing vectors to 
	 * files.
	 */
	public String toLine()
	{
		String result = "";
		for (int i = 0; i < floats.length; ++i)
		{
			result += floats[i];
			if (i < floats.length - 1)
			{
				result += ",";
			}
		}
		return result;
	}
	
	@Override
	public String toString()
	{
		return String.format("[%s]", toLine());
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(floats);
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (!(obj instanceof FVector))
			return false;
		
		final FVector other = (FVector) obj;
		return (Arrays.equals(floats, other.floats));
	}
	
	//-------------------------------------------------------------------------

}
