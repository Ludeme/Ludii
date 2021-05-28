package main.math;

/**
 * Static class with bit twiddling routines
 * Many taken from the fantastic:
 * https://graphics.stanford.edu/~seander/bithacks.html
 * @author Stephen Tavener
 *
 */
public final class BitTwiddling {

	/**
	 * No, you can't... and I won't :P
	 */
	private BitTwiddling() {
		// Go away
	}
	
	/**
	 * @param a
	 * @param b
	 * @return true if a and b have opposite signs
	 */
	public static final boolean oppositeSigns (final int a, final int b) {
		return ((a ^ b) < 0);
	}
	
	/**
	 * @param a
	 * @param b
	 * @return true if a and b have opposite signs
	 */
	public static final boolean oppositeSigns (final long a, final long b) {
		return ((a ^ b) < 0);
	}
	
	/**
	 * @param a
	 * @return true if a is a power of two (exactly one bit set)
	 */
	public static final boolean exactlyOneBitSet (final int a) {
		if (a == 0) return false;
		return (a & (a - 1))==0;
	}
	
	/**
	 * @param a
	 * @return true if a is a power of two (exactly one bit set)
	 */
	public static final boolean exactlyOneBitSet (final long a) {
		if (a == 0) return false;
		return (a & (a - 1))==0;
	}	

	/**
	 * @param a
	 * @return true if a is a power of two (exactly one bit set) or zero
	 */
	public static final boolean leOneBitSet (final int a) {
		return (a & (a - 1))==0;
	}
	
	/**
	 * @param a
	 * @return true if a is a power of two (exactly one bit set) or zero
	 */
	public static final boolean leOneBitSet (final long a) {
		return (a & (a - 1))==0;
	}	
	
	/**
	 * @param a Number to test
	 * @return position of top bit set in this int
	 */
	public static final int topBitPos (final int a) 
	{
		return 31-Integer.numberOfLeadingZeros(a);
	}

	/**
	 * @param a Number to test
	 * @return position of top bit set in this int
	 */
	public static final int topBitPos (final long a) 
	{
		return 63-Long.numberOfLeadingZeros(a);
	}

	/**
	 * @param v
	 * @return index of lowest bit, 0 if no bit set
	 */
	public static final int lowBitPos (int v)
	{
		return Integer.numberOfTrailingZeros(v);
	}

	/**
	 * @param v
	 * @return index of lowest bit, 0 if no bit set
	 */
	public static final int lowBitPos (long v)
	{
		return Long.numberOfTrailingZeros(v);
	}
	
	/**
	 * @param a Number to test
	 * @return bottom bit
	 */
	public static final int bottomBit (final int a) 
	{
		return Integer.lowestOneBit(a);
	}

	/**
	 * @param a Number to test
	 * @return bottom bit
	 */
	public static final long bottomBit (final long a) 
	{
		return Long.lowestOneBit(a);
	}
	
	/**
	 * @param v
	 * @return number of bits set
	 */
	public static final int countBits(final int v) 
	{
		return Integer.bitCount(v);
	}
	
	/**
	 * A generalization of the best bit counting method to integers of bit-widths up to 128 (parameterized by type T) is this:
	 *
	 *		v = v - ((v >> 1) & (T)~(T)0/3);                           // temp
	 *		v = (v & (T)~(T)0/15*3) + ((v >> 2) & (T)~(T)0/15*3);      // temp
	 *		v = (v + (v >> 4)) & (T)~(T)0/255*15;                      // temp
	 *		c = (T)(v * ((T)~(T)0/255)) >> (sizeof(T) - 1) * CHAR_BIT; // count
	 *
	 * @param v
	 * @return number of bits set
	 */
	public static final int countBits(final long v) 
	{
		return Long.bitCount(v);
	}	
	
	/**
	 * @param v
	 * @return next permutation in sequence
	 */
	public static final int nextPermutation (final int v) 
	{
		final int t = (v | (v - 1)) + 1;  
		return t | ((((t & -t) / (v & -v)) >>> 1) - 1);  
	}

	/**
	 * @param v
	 * @return next permutation in sequence
	 */
	public static final long nextPermutation (final long v) 
	{
		final long t = (v | (v - 1)) + 1;  
		return t | ((((t & -t) / (v & -v)) >>> 1) - 1);  
	}
	
	/**
	 * @param n
	 * @return 1 if value is non-zero, 0 if it is zero
	 */
	public static final int oneIfNonZero (int n)
	{
		return ((n | (~n + 1)) >>> 31);
	}
	
	/**
	 * @param n
	 * @return 1 if value is zero, 0 if it is non-zero
	 */
	public static final int oneIfZero (int n)
	{
		return oneIfNonZero(n)^1;
	}

	/**
	 * @param n
	 * @return 1 if value is non-zero, 0 if it is zero
	 */
	public static final long oneIfNonZero (long n)
	{
		return ((n | (~n + 1L)) >>> 63);
	}
	
	/**
	 * @param n
	 * @return 1 if value is zero, 0 if it is non-zero
	 */
	public static final long oneIfZero (long n)
	{
		return oneIfNonZero(n)^1L;
	}

	/** 
	 * Reverses one byte of data
	 * 5 ops, no division 
	 * 
	 * The following shows the flow of the bit values with the boolean variables a, b, c, d, e, f, g, and h, which comprise an 8-bit byte. 
	 * Notice how the first multiply fans out the bit pattern to multiple copies, while the last multiply combines them in the fifth byte from the right.
	 * abcd efgh (-> hgfe dcba)
	 * 
	 *	*                                                      1000 0000  0010 0000  0000 1000  0000 0010 (0x80200802)
	 *	-------------------------------------------------------------------------------------------------
	 *	                                            0abc defg  h00a bcde  fgh0 0abc  defg h00a  bcde fgh0
	 *	&                                           0000 1000  1000 0100  0100 0010  0010 0001  0001 0000 (0x0884422110)
	 *	-------------------------------------------------------------------------------------------------
	 *	                                            0000 d000  h000 0c00  0g00 00b0  00f0 000a  000e 0000
	 *	*                                           0000 0001  0000 0001  0000 0001  0000 0001  0000 0001 (0x0101010101)
	 *	-------------------------------------------------------------------------------------------------
	 *	                                            0000 d000  h000 0c00  0g00 00b0  00f0 000a  000e 0000
	 *	                                 0000 d000  h000 0c00  0g00 00b0  00f0 000a  000e 0000
	 *	                      0000 d000  h000 0c00  0g00 00b0  00f0 000a  000e 0000
	 *	           0000 d000  h000 0c00  0g00 00b0  00f0 000a  000e 0000
	 *	0000 d000  h000 0c00  0g00 00b0  00f0 000a  000e 0000
	 *	-------------------------------------------------------------------------------------------------
	 *	0000 d000  h000 dc00  hg00 dcb0  hgf0 dcba  hgfe dcba  hgfe 0cba  0gfe 00ba  00fe 000a  000e 0000
	 *	>> 32
	 *	-------------------------------------------------------------------------------------------------
	 *	                                            0000 d000  h000 dc00  hg00 dcb0  hgf0 dcba  hgfe dcba  
	 *	&                                                                                       1111 1111
	 *	-------------------------------------------------------------------------------------------------
	 *	        hgfe dcba
	 *	Note that the last two steps can be combined on some processors because the registers can be accessed as bytes; just multiply so that a register stores the upper 32 bits of the result and the take the low byte. Thus, it may take only 6 operations.
	 *
	 * @param value 
	 * @return 8 bits, order reversed
	 */
	public static int reverseByte (final int value) 
	{
		return ((int)(((value * 0x80200802L) & 0x0884422110L) * 0x0101010101L >>> 32)) & 0xFF;
	}
	
	//-------------------------------------------------------------------------


	/**
	 * @param value
	 * @return Number of bits required to cover integers from 0..value.
	 */
	public static int bitsRequired(final int value)
	{
		return (int) Math.ceil(Math.log(value + 1) / Math.log(2));
	}

	/**
	 * @param numBits
	 * @return Mask with specified number of bits [0..31] turned on.
	 */
	public static int maskI(final int numBits)
	{
		return (0x1 << numBits) - 1;
	}

	/**
	 * @param numBits
	 * @return Mask with specified number of bits [0..63] turned on.
	 */
	public static long maskL(final int numBits)
	{
		return (0x1L << numBits) - 1L;
	}

	/**
	 * @param n
	 * @return Lowest power of 2 >= n.
	 */
	public static int nextPowerOf2(final int n)
	{
		if (n <= 1) return 1; // n^0 == 1
		return (int) Math.pow(2, Math.ceil(Math.log(n) / Math.log(2)));
	}

	/**
	 * @param n
	 * @return Whether the specified value is a positive power of 2.
	 */
	public static boolean isPowerOf2(final int n)
	{
		return n > 0 && ((n & (n - 1)) == 0);
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @param x
	 * @return Base-2 log of an integer x, rounded down
	 */
	public static int log2RoundDown(final int x)
	{
		return (Integer.SIZE - 1) - Integer.numberOfLeadingZeros(x);
	}
	
	/**
	 * @param x
	 * @return Base-2 log of an integer x, rounded up
	 */
	public static int log2RoundUp(final int x)
	{
		return Integer.SIZE - Integer.numberOfLeadingZeros(x - 1);
	}
	
	//-------------------------------------------------------------------------
	
}
