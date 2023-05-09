package main.collections;

/*
 * Copyright 1995-2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.lang.reflect.Field;
import java.util.Arrays;

import gnu.trove.list.array.TIntArrayList;
import main.math.BitTwiddling;

//-----------------------------------------------------------------------------

/**
 * This class implements a vector of bits that grows as needed. Each component
 * of the bit set has a {@code boolean} value. The bits of a {@code BitSet} are
 * indexed by nonnegative integers. Individual indexed bits can be examined,
 * set, or cleared. One {@code BitSet} may be used to modify the contents of
 * another {@code BitSet} through logical AND, logical inclusive OR, and logical
 * exclusive OR operations.
 *
 * <p>
 * By default, all bits in the set initially have the value {@code false}.
 *
 * <p>
 * Every bit set has a current size, which is the number of bits of space
 * currently in use by the bit set. Note that the size is related to the
 * implementation of a bit set, so it may change with implementation. The length
 * of a bit set relates to logical length of a bit set and is defined
 * independently of implementation.
 *
 * <p>
 * Unless otherwise noted, passing a null parameter to any of the methods in a
 * {@code BitSet} will result in a {@code NullPointerException}.
 *
 * <p>
 * A {@code BitSet} is not safe for multithreaded use without external
 * synchronization.
 *
 * @author Arthur van Hoff
 * @author Michael McCloskey
 * @author Martin Buchholz
 * @since JDK1.0
 */
public final class ChunkSet implements Cloneable, java.io.Serializable
{
	/*
	 * BitSets are packed into arrays of "words." Currently a word is a long, which
	 * consists of 64 bits, requiring 6 address bits. The choice of word size is
	 * determined purely by performance concerns.
	 */
	/** */
	private final static int ADDRESS_BITS_PER_WORD = 6;

	/** */
	private final static int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;

	/**
	 * The internal field corresponding to the serialField "bits".
	 */
	private long[] words;

	/**
	 * The number of words in the logical size of this BitSet.
	 */
	private transient int wordsInUse = 0;

	/**
	 * Whether the size of "words" is user-specified. If so, we assume the user
	 * knows what he's doing and try harder to preserve it.
	 */
	private transient boolean sizeIsSticky = false;

	/** 
	 * Modified serialVersionUID from BitSet, (de)serialization processes have been customized,
	 * not compatible with JDKs standard BitSet implementation
	 */
	private static final long serialVersionUID = 1L;

	/** */
//    private final static int BIT_INDEX_MASK = BITS_PER_WORD - 1;

	/** Used to shift left or right for a partial word mask */
	private static final long WORD_MASK = 0xffffffffffffffffL;

	/** 0x1010101010101010101010101010101010101010101010101010101010101010 */
	final static long MASK_NOT_1 = 0xaaaaaaaaaaaaaaaaL;

	/** 0x1100110011001100110011001100110011001100110011001100110011001100 */
	final static long MASK_NOT_2 = 0xccccccccccccccccL;

	/** 0x1111000011110000111100001111000011110000111100001111000011110000 */
	final static long MASK_NOT_4 = 0xf0f0f0f0f0f0f0f0L;

	/** 0x1111111100000000111111110000000011111111000000001111111100000000 */
	final static long MASK_NOT_8 = 0xff00ff00ff00ff00L;

	/** 0x1111111111111111000000000000000011111111111111110000000000000000 */
	final static long MASK_NOT_16 = 0xffff0000ffff0000L;

	/** 0x1111111111111111111111111111111100000000000000000000000000000000 */
	final static long MASK_NOT_32 = 0xffffffff00000000L;

	/**
	 * Number of bits per chunk. Must be a power of 2 up to 32: 1, 2, 4, 8, 16, 32.
	 */
	protected final int chunkSize;

	/** Mask for this chunk size. */
	protected final long chunkMask;                // = (0x1L << chunkSize) - 1L;

	final static long[] bitNMasks = new long[64]; 
	{ 
		for (int n = 0; n < 64; n++)
		{
			bitNMasks[n] = (0x1L << n) - 0x1L;
			//System.out.println(n + ": " + bitNMasks[n]);
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Creates a new bit set. All bits are initially {@code false}.
	 */
	public ChunkSet()
	{
		initWords(BITS_PER_WORD);
		sizeIsSticky = false;
		this.chunkSize = 1;
		this.chunkMask = (0x1L << chunkSize) - 1;
	}

	/**
	 * Creates a bit set whose initial size is large enough to explicitly represent
	 * bits with indices in the range {@code 0} through {@code nbits-1}. All bits
	 * are initially {@code false}.
	 *
	 * @param chunkSize
	 * @param numChunks
	 * @throws NegativeArraySizeException if the specified initial size is negative
	 */
	public ChunkSet(final int chunkSize, final int numChunks)
	{
		// **
		// ** Do not store numChunks as a final, as board may grow.
		// **
		this.chunkSize = chunkSize;
		this.chunkMask = (0x1L << chunkSize) - 1;

		assert (BitTwiddling.isPowerOf2(chunkSize));
//		if (!BitTwiddling.isPowerOf2(chunkSize))
//		{
//			System.out.println("** BitSetS: chunkSize " + chunkSize + " is not a power of 2.");
//			Utilities.stackTrace();
//		}

		final int nbits = chunkSize * numChunks;
//   	System.out.print("BitSetS(" + chunkSize + "," + numChunks + "): nbits=" + nbits + ".");

		// nbits can't be negative; size 0 is OK
		assert (nbits >= 0);
//		if (nbits < 0)
//			throw new NegativeArraySizeException("nbits < 0: " + nbits);
		
		initWords(nbits);
		sizeIsSticky = true;

//        System.out.println(", size=" + size() + ", length=" + length() + ".");
	}

	//-------------------------------------------------------------------------

	/**
	 * @serialField bits long[]
	 *
	 *              The bits in this BitSet. The ith bit is stored in bits[i/64] at
	 *              bit position i % 64 (where bit position 0 refers to the least
	 *              significant bit and 63 refers to the most significant bit).
	 */
	private static final ObjectStreamField[] serialPersistentFields =
	{ new ObjectStreamField("bits", long[].class),};

	/**
	 * Given a bit index, return word index containing it.
	 * 
	 * @param bitIndex
	 * @return Word index containing bit.
	 */
	private static int wordIndex(int bitIndex)
	{
		return bitIndex >> ADDRESS_BITS_PER_WORD;
	}

	/**
	 * Every public method must preserve these invariants.
	 */
	private void checkInvariants()
	{
		assert (wordsInUse == 0 || words[wordsInUse - 1] != 0);
		assert (wordsInUse >= 0 && wordsInUse <= words.length);
		assert (wordsInUse == words.length || words[wordsInUse] == 0);
	}

	/**
	 * Sets the field wordsInUse to the logical size in words of the bit set.
	 * WARNING:This method assumes that the number of words actually in use is less
	 * than or equal to the current value of wordsInUse!
	 */
	private void recalculateWordsInUse()
	{
		// Traverse the bitset until a used word is found
		int i;
		for (i = wordsInUse - 1; i >= 0; i--)
			if (words[i] != 0)
				break;

		wordsInUse = i + 1; // The new logical size
	}

	/**
	 * @param nbits
	 */
	private void initWords(int nbits)
	{
		words = new long[wordIndex(nbits - 1) + 1];
	}

//    /**
//     * Creates a bit set using words as the internal representation.
//     * The last word (if there is one) must be non-zero.
//     * @param words 
//     */
//    private BitSetS(long[] words) 
//    {
//        this.words = words;
//        this.wordsInUse = words.length;
//        checkInvariants();
//    }

	/**
	 * Ensures that the BitSet can hold enough words.
	 * 
	 * @param wordsRequired the minimum acceptable number of words.
	 */
	private void ensureCapacity(int wordsRequired)
	{
		if (words.length < wordsRequired)
		{
			// Allocate larger of doubled size or required size
			final int request = Math.max(2 * words.length, wordsRequired);
			words = Arrays.copyOf(words, request);
			sizeIsSticky = false;
		}
	}

	/**
	 * Ensures that the BitSet can accommodate a given wordIndex, temporarily
	 * violating the invariants. The caller must restore the invariants before
	 * returning to the user, possibly using recalculateWordsInUse().
	 * 
	 * @param wordIndex the index to be accommodated.
	 */
	private void expandTo(int wordIndex)
	{
		final int wordsRequired = wordIndex + 1;
		if (wordsInUse < wordsRequired)
		{
			ensureCapacity(wordsRequired);
			wordsInUse = wordsRequired;
		}
	}

	/**
	 * Checks that fromIndex ... toIndex is a valid range of bit indices.
	 * 
	 * @param fromIndex
	 * @param toIndex
	 */
	private static void checkRange(int fromIndex, int toIndex)
	{
		if (fromIndex < 0)
			throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);
		if (toIndex < 0)
			throw new IndexOutOfBoundsException("toIndex < 0: " + toIndex);
		if (fromIndex > toIndex)
			throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + " > toIndex: " + toIndex);
	}

	/**
	 * Sets the bit at the specified index to the complement of its current value.
	 *
	 * @param bitIndex the index of the bit to flip
	 * @throws IndexOutOfBoundsException if the specified index is negative
	 * @since 1.4
	 */
	public void flip(int bitIndex)
	{
		if (bitIndex < 0)
			throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

		final int wordIndex = wordIndex(bitIndex);
		expandTo(wordIndex);

		words[wordIndex] ^= (1L << bitIndex);

		recalculateWordsInUse();
		checkInvariants();
	}

	/**
	 * Sets each bit from the specified {@code fromIndex} (inclusive) to the
	 * specified {@code toIndex} (exclusive) to the complement of its current value.
	 *
	 * @param fromIndex index of the first bit to flip
	 * @param toIndex   index after the last bit to flip
	 * @throws IndexOutOfBoundsException if {@code fromIndex} is negative, or
	 *                                   {@code toIndex} is negative, or
	 *                                   {@code fromIndex} is larger than
	 *                                   {@code toIndex}
	 * @since 1.4
	 */
	public void flip(int fromIndex, int toIndex)
	{
		checkRange(fromIndex, toIndex);

		if (fromIndex == toIndex)
			return;

		final int startWordIndex = wordIndex(fromIndex);
		final int endWordIndex   = wordIndex(toIndex - 1);
		expandTo(endWordIndex);

		final long firstWordMask = WORD_MASK << fromIndex;
		final long lastWordMask  = WORD_MASK >>> -toIndex;
		if (startWordIndex == endWordIndex)
		{
			// Case 1: One word
			words[startWordIndex] ^= (firstWordMask & lastWordMask);
		} else
		{
			// Case 2: Multiple words
			// Handle first word
			words[startWordIndex] ^= firstWordMask;

			// Handle intermediate words, if any
			for (int i = startWordIndex + 1; i < endWordIndex; i++)
				words[i] ^= WORD_MASK;

			// Handle last word
			words[endWordIndex] ^= lastWordMask;
		}

		recalculateWordsInUse();
		checkInvariants();
	}

	/**
	 * Sets the bit at the specified index to {@code true}.
	 *
	 * @param bitIndex a bit index
	 * @throws IndexOutOfBoundsException if the specified index is negative
	 * @since JDK1.0
	 */
	public void set(int bitIndex)
	{
		if (bitIndex < 0)
			throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

		final int wordIndex = wordIndex(bitIndex);
		expandTo(wordIndex);

		words[wordIndex] |= (1L << bitIndex); // Restores invariants

		checkInvariants();
	}

	/**
	 * Sets the bit at the specified index to the specified value.
	 *
	 * @param bitIndex a bit index
	 * @param value    a boolean value to set
	 * @throws IndexOutOfBoundsException if the specified index is negative
	 * @since 1.4
	 */
	public void set(int bitIndex, boolean value)
	{
		if (value)
			set(bitIndex);
		else
			clear(bitIndex);
	}

	/**
	 * Sets the bits from the specified {@code fromIndex} (inclusive) to the
	 * specified {@code toIndex} (exclusive) to {@code true}.
	 *
	 * @param fromIndex index of the first bit to be set
	 * @param toIndex   index after the last bit to be set
	 * @throws IndexOutOfBoundsException if {@code fromIndex} is negative, or
	 *                                   {@code toIndex} is negative, or
	 *                                   {@code fromIndex} is larger than
	 *                                   {@code toIndex}
	 * @since 1.4
	 */
	public void set(int fromIndex, int toIndex)
	{
		checkRange(fromIndex, toIndex);

		if (fromIndex == toIndex)
			return;

		// Increase capacity if necessary
		final int startWordIndex = wordIndex(fromIndex);
		final int endWordIndex   = wordIndex(toIndex - 1);
		expandTo(endWordIndex);

		final long firstWordMask = WORD_MASK << fromIndex;
		final long lastWordMask  = WORD_MASK >>> -toIndex;
		if (startWordIndex == endWordIndex)
		{
			// Case 1: One word
			words[startWordIndex] |= (firstWordMask & lastWordMask);
		} else
		{
			// Case 2: Multiple words
			// Handle first word
			words[startWordIndex] |= firstWordMask;

			// Handle intermediate words, if any
			for (int i = startWordIndex + 1; i < endWordIndex; i++)
				words[i] = WORD_MASK;

			// Handle last word (restores invariants)
			words[endWordIndex] |= lastWordMask;
		}

		checkInvariants();
	}

	/**
	 * Sets the bits from the specified {@code fromIndex} (inclusive) to the
	 * specified {@code toIndex} (exclusive) to the specified value.
	 *
	 * @param fromIndex index of the first bit to be set
	 * @param toIndex   index after the last bit to be set
	 * @param value     value to set the selected bits to
	 * @throws IndexOutOfBoundsException if {@code fromIndex} is negative, or
	 *                                   {@code toIndex} is negative, or
	 *                                   {@code fromIndex} is larger than
	 *                                   {@code toIndex}
	 * @since 1.4
	 */
	public void set(int fromIndex, int toIndex, boolean value)
	{
		if (value)
			set(fromIndex, toIndex);
		else
			clear(fromIndex, toIndex);
	}

	/**
	 * Sets the bit specified by the index to {@code false}.
	 *
	 * @param bitIndex the index of the bit to be cleared
	 * @throws IndexOutOfBoundsException if the specified index is negative
	 * @since JDK1.0
	 */
	public void clear(int bitIndex)
	{
		if (bitIndex < 0)
			throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

		final int wordIndex = wordIndex(bitIndex);
		if (wordIndex >= wordsInUse)
			return;

		words[wordIndex] &= ~(1L << bitIndex);

		recalculateWordsInUse();
		checkInvariants();
	}

	/**
	 * Sets the bits from the specified {@code fromIndex} (inclusive) to the
	 * specified {@code toIndex} (exclusive) to {@code false}.
	 *
	 * @param fromIndex index of the first bit to be cleared
	 * @param toIndex   index after the last bit to be cleared
	 * @throws IndexOutOfBoundsException if {@code fromIndex} is negative, or
	 *                                   {@code toIndex} is negative, or
	 *                                   {@code fromIndex} is larger than
	 *                                   {@code toIndex}
	 * @since 1.4
	 */
	public void clear(int fromIndex, int toIndex)
	{
		checkRange(fromIndex, toIndex);

		if (fromIndex == toIndex)
			return;

		final int startWordIndex = wordIndex(fromIndex);
		if (startWordIndex >= wordsInUse)
			return;

		int to           = toIndex;
		int endWordIndex = wordIndex(to - 1);
		if (endWordIndex >= wordsInUse)
		{
			to = length();
			endWordIndex = wordsInUse - 1;
		}

		final long firstWordMask = WORD_MASK << fromIndex;
		final long lastWordMask  = WORD_MASK >>> -to;
		if (startWordIndex == endWordIndex)
		{
			// Case 1: One word
			words[startWordIndex] &= ~(firstWordMask & lastWordMask);
		} else
		{
			// Case 2: Multiple words
			// Handle first word
			words[startWordIndex] &= ~firstWordMask;

			// Handle intermediate words, if any
			for (int i = startWordIndex + 1; i < endWordIndex; i++)
				words[i] = 0;

			// Handle last word
			words[endWordIndex] &= ~lastWordMask;
		}

		recalculateWordsInUse();
		checkInvariants();
	}

	/**
	 * Sets all of the bits in this BitSet to {@code false}.
	 *
	 * @since 1.4
	 */
	public void clear()
	{
		while (wordsInUse > 0)
			words[--wordsInUse] = 0;
	}

	/**
	 * Returns the value of the bit with the specified index. The value is
	 * {@code true} if the bit with the index {@code bitIndex} is currently set in
	 * this {@code BitSet}; otherwise, the result is {@code false}.
	 *
	 * @param bitIndex the bit index
	 * @return the value of the bit with the specified index
	 * @throws IndexOutOfBoundsException if the specified index is negative
	 */
	public boolean get(int bitIndex)
	{
		if (bitIndex < 0)
			throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

		checkInvariants();

		final int wordIndex = wordIndex(bitIndex);
		return (wordIndex < wordsInUse) && ((words[wordIndex] & (1L << bitIndex)) != 0);
	}

//    /**
//     * Returns a new {@code BitSet} composed of bits from this {@code BitSet}
//     * from {@code fromIndex} (inclusive) to {@code toIndex} (exclusive).
//     *
//     * @param  fromIndex index of the first bit to include
//     * @param  toIndex index after the last bit to include
//     * @return a new {@code BitSet} from a range of this {@code BitSet}
//     * @throws IndexOutOfBoundsException if {@code fromIndex} is negative,
//     *         or {@code toIndex} is negative, or {@code fromIndex} is
//     *         larger than {@code toIndex}
//     * @since  1.4
//     */
//    public BitSetS get(int fromIndex, int toIndex) 
//    {
//        checkRange(fromIndex, toIndex);
//
//        checkInvariants();
//
//        int len = length();
//
//        // If no set bits in range return empty bitset
//        if (len <= fromIndex || fromIndex == toIndex)
//            return new BitSetS(0);
//
//        // An optimization
//        if (toIndex > len)
//            toIndex = len;
//
//        BitSetS result = new BitSetS(toIndex - fromIndex);
//        int targetWords = wordIndex(toIndex - fromIndex - 1) + 1;
//        int sourceIndex = wordIndex(fromIndex);
//        boolean wordAligned = ((fromIndex & BIT_INDEX_MASK) == 0);
//
//        // Process all words but the last word
//        for (int i = 0; i < targetWords - 1; i++, sourceIndex++)
//            result.words[i] = wordAligned ? words[sourceIndex] :
//                (words[sourceIndex] >>> fromIndex) |
//                (words[sourceIndex+1] << -fromIndex);
//
//        // Process the last word
//        long lastWordMask = WORD_MASK >>> -toIndex;
//        result.words[targetWords - 1] =
//            ((toIndex-1) & BIT_INDEX_MASK) < (fromIndex & BIT_INDEX_MASK)
//            ? /* straddles source words */
//            ((words[sourceIndex] >>> fromIndex) |
//             (words[sourceIndex+1] & lastWordMask) << -fromIndex)
//            :
//            ((words[sourceIndex] & lastWordMask) >>> fromIndex);
//
//        // Set wordsInUse correctly
//        result.wordsInUse = targetWords;
//        result.recalculateWordsInUse();
//        result.checkInvariants();
//
//        return result;
//    }

	/**
	 * Returns the index of the first bit that is set to {@code true} that occurs on
	 * or after the specified starting index. If no such bit exists then {@code -1}
	 * is returned.
	 *
	 * <p>
	 * To iterate over the {@code true} bits in a {@code BitSet}, use the following
	 * loop:
	 *
	 * <pre>
	 *  {@code
	 * for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
	 *     // operate on index i here
	 * }}
	 * </pre>
	 *
	 * @param fromIndex the index to start checking from (inclusive)
	 * @return the index of the next set bit, or {@code -1} if there is no such bit
	 * @throws IndexOutOfBoundsException if the specified index is negative
	 * @since 1.4
	 */
	public int nextSetBit(int fromIndex)
	{
		if (fromIndex < 0)
			throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);

		checkInvariants();

		int u = wordIndex(fromIndex);
		if (u >= wordsInUse)
			return -1;

		long word = words[u] & (WORD_MASK << fromIndex);

		while (true)
		{
			if (word != 0)
				return (u * BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
			if (++u == wordsInUse)
				return -1;
			word = words[u];
		}
	}

	/**
	 * Returns the index of the first bit that is set to {@code false} that occurs
	 * on or after the specified starting index.
	 *
	 * @param fromIndex the index to start checking from (inclusive)
	 * @return the index of the next clear bit
	 * @throws IndexOutOfBoundsException if the specified index is negative
	 * @since 1.4
	 */
	public int nextClearBit(int fromIndex)
	{
		// Neither spec nor implementation handle bitsets of maximal length.
		// See 4816253.
		if (fromIndex < 0)
			throw new IndexOutOfBoundsException("fromIndex < 0: " + fromIndex);

		checkInvariants();

		int u = wordIndex(fromIndex);
		if (u >= wordsInUse)
			return fromIndex;

		long word = ~words[u] & (WORD_MASK << fromIndex);

		while (true)
		{
			if (word != 0)
				return (u * BITS_PER_WORD) + Long.numberOfTrailingZeros(word);
			if (++u == wordsInUse)
				return wordsInUse * BITS_PER_WORD;
			word = ~words[u];
		}
	}

	/**
	 * Returns the "logical size" of this {@code BitSet}: the index of the highest
	 * set bit in the {@code BitSet} plus one. Returns zero if the {@code BitSet}
	 * contains no set bits.
	 *
	 * @return the logical size of this {@code BitSet}
	 * @since 1.2
	 */
	public int length()
	{
		if (wordsInUse == 0)
			return 0;

		return BITS_PER_WORD * (wordsInUse - 1) + (BITS_PER_WORD - Long.numberOfLeadingZeros(words[wordsInUse - 1]));
	}

	/**
	 * Returns true if this {@code BitSet} contains no bits that are set to
	 * {@code true}.
	 *
	 * @return boolean indicating whether this {@code BitSet} is empty
	 * @since 1.4
	 */
	public boolean isEmpty()
	{
		return wordsInUse == 0;
	}

	/**
	 * Returns true if the specified {@code BitSet} has any bits set to {@code true}
	 * that are also set to {@code true} in this {@code BitSet}.
	 *
	 * @param set {@code BitSet} to intersect with
	 * @return boolean indicating whether this {@code BitSet} intersects the
	 *         specified {@code BitSet}
	 * @since 1.4
	 */
	public boolean intersects(final ChunkSet set)
	{
		for (int i = Math.min(wordsInUse, set.wordsInUse) - 1; i >= 0; i--)
			if ((words[i] & set.words[i]) != 0)
				return true;
		return false;
	}

	/**
	 * Returns the number of bits set to {@code true} in this {@code BitSet}.
	 *
	 * @return the number of bits set to {@code true} in this {@code BitSet}
	 * @since 1.4
	 */
	public int cardinality()
	{
		int sum = 0;
		for (int i = 0; i < wordsInUse; i++)
			sum += Long.bitCount(words[i]);
		return sum;
	}

	/**
	 * Performs a logical <b>AND</b> of this target bit set with the argument bit
	 * set. This bit set is modified so that each bit in it has the value
	 * {@code true} if and only if it both initially had the value {@code true} and
	 * the corresponding bit in the bit set argument also had the value
	 * {@code true}.
	 *
	 * @param set a bit set
	 */
	public void and(final ChunkSet set)
	{
		if (this == set)
			return;

		while (wordsInUse > set.wordsInUse)
			words[--wordsInUse] = 0;

		// Perform logical AND on words in common
		for (int i = 0; i < wordsInUse; i++)
			words[i] &= set.words[i];

		recalculateWordsInUse();
		checkInvariants();
	}

	/**
	 * Performs a logical <b>OR</b> of this bit set with the bit set argument. This
	 * bit set is modified so that a bit in it has the value {@code true} if and
	 * only if it either already had the value {@code true} or the corresponding bit
	 * in the bit set argument has the value {@code true}.
	 *
	 * @param set a bit set
	 */
	public void or(final ChunkSet set)
	{
		if (this == set)
			return;

		final int wordsInCommon = Math.min(wordsInUse, set.wordsInUse);

		if (wordsInUse < set.wordsInUse)
		{
			ensureCapacity(set.wordsInUse);
			wordsInUse = set.wordsInUse;
		}

		// Perform logical OR on words in common
		for (int i = 0; i < wordsInCommon; i++)
			words[i] |= set.words[i];

		// Copy any remaining words
		if (wordsInCommon < set.wordsInUse)
			System.arraycopy(set.words, wordsInCommon, words, wordsInCommon, wordsInUse - wordsInCommon);

		// recalculateWordsInUse() is unnecessary
		checkInvariants();
	}

//    /**
//     * Performs a logical <b>OR</b> of this bit set with the bit set
//     * argument. This bit set is modified so that a bit in it has the
//     * value {@code true} if and only if it either already had the
//     * value {@code true} or the corresponding bit in the bit set
//     * argument has the value {@code true}.
//     *
//     * @param set a bit set
//     */
//    public void deepCopy(final BitSetS set) 
//    {
//        if (this == set)
//            return;
//
//        //int wordsInCommon = Math.max(wordsInUse, set.wordsInUse);
//
//        if (wordsInUse != set.wordsInUse) 
//        {
//            ensureCapacity(set.wordsInUse);
//            wordsInUse = set.wordsInUse;
//        }
//
////        // Perform copy of words in common
////        for (int i = 0; i < wordsInUse; i++)
////        	words[i] = set.words[i];
//
//        // Copy any remaining words
//        System.arraycopy
//        (
//        	set.words, 0, words, 0, wordsInUse
//        );
//
//        // recalculateWordsInUse() is unnecessary
//        checkInvariants();
//    }

	/**
	 * Performs a logical <b>XOR</b> of this bit set with the bit set argument. This
	 * bit set is modified so that a bit in it has the value {@code true} if and
	 * only if one of the following statements holds:
	 * <ul>
	 * <li>The bit initially has the value {@code true}, and the corresponding bit
	 * in the argument has the value {@code false}.
	 * <li>The bit initially has the value {@code false}, and the corresponding bit
	 * in the argument has the value {@code true}.
	 * </ul>
	 *
	 * @param set a bit set
	 */
	public void xor(ChunkSet set)
	{
		final int wordsInCommon = Math.min(wordsInUse, set.wordsInUse);

		if (wordsInUse < set.wordsInUse)
		{
			ensureCapacity(set.wordsInUse);
			wordsInUse = set.wordsInUse;
		}

		// Perform logical XOR on words in common
		for (int i = 0; i < wordsInCommon; i++)
			words[i] ^= set.words[i];

		// Copy any remaining words
		if (wordsInCommon < set.wordsInUse)
			System.arraycopy(set.words, wordsInCommon, words, wordsInCommon, set.wordsInUse - wordsInCommon);

		recalculateWordsInUse();
		checkInvariants();
	}

	/**
	 * Clears all of the bits in this {@code BitSet} whose corresponding bit is set
	 * in the specified {@code BitSet}.
	 *
	 * @param set the {@code BitSet} with which to mask this {@code BitSet}
	 * @since 1.2
	 */
	public void andNot(ChunkSet set)
	{
		// Perform logical (a & !b) on words in common
		for (int i = Math.min(wordsInUse, set.wordsInUse) - 1; i >= 0; i--)
			words[i] &= ~set.words[i];

		recalculateWordsInUse();
		checkInvariants();
	}

	/**
	 * Returns a hash code value for this bit set. The hash code depends only on
	 * which bits have been set within this <code>BitSet</code>. The algorithm used
	 * to compute it may be described as follows.
	 * <p>
	 * Suppose the bits in the <code>BitSet</code> were to be stored in an array of
	 * <code>long</code> integers called, say, <code>words</code>, in such a manner
	 * that bit <code>k</code> is set in the <code>BitSet</code> (for nonnegative
	 * values of <code>k</code>) if and only if the expression
	 * 
	 * <pre>
	 * ((k &gt;&gt; 6) &lt; words.length) && ((words[k &gt;&gt; 6] & (1L &lt;&lt; (bit & 0x3F))) != 0)
	 * </pre>
	 * 
	 * is true. Then the following definition of the <code>hashCode</code> method
	 * would be a correct implementation of the actual algorithm:
	 * 
	 * <pre>
	 * public int hashCode()
	 * {
	 * 	long h = 1234;
	 * 	for (int i = words.length; --i &gt;= 0;)
	 * 	{
	 * 		h ^= words[i] * (i + 1);
	 * 	}
	 * 	return (int) ((h &gt;&gt; 32) ^ h);
	 * }
	 * </pre>
	 * 
	 * Note that the hash code values change if the set of bits is altered.
	 * <p>
	 * Overrides the <code>hashCode</code> method of <code>Object</code>.
	 *
	 * @return a hash code value for this bit set.
	 */
	@Override
	public int hashCode()
	{
		long h = 1234;
		for (int i = wordsInUse; --i >= 0;)
			h ^= words[i] * (i + 1);

		return (int) ((h >> 32) ^ h);
	}

	/**
	 * Returns the number of bits of space actually in use by this {@code BitSet} to
	 * represent bit values. The maximum element in the set is the size - 1st
	 * element.
	 *
	 * @return the number of bits currently in this bit set
	 */
	public int size()
	{
		return words.length * BITS_PER_WORD;
	}

	/**
	 * Compares this object against the specified object. The result is {@code true}
	 * if and only if the argument is not {@code null} and is a {@code Bitset}
	 * object that has exactly the same set of bits set to {@code true} as this bit
	 * set. That is, for every nonnegative {@code int} index {@code k},
	 * 
	 * <pre>
	 * ((BitSet) obj).get(k) == this.get(k)
	 * </pre>
	 * 
	 * must be true. The current sizes of the two bit sets are not compared.
	 *
	 * @param obj the object to compare with
	 * @return {@code true} if the objects are the same; {@code false} otherwise
	 * @see #size()
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof ChunkSet))
			return false;
		if (this == obj)
			return true;

		final ChunkSet set = (ChunkSet) obj;

		checkInvariants();
		set.checkInvariants();

		if (wordsInUse != set.wordsInUse)
			return false;

		// Check words in use by both BitSets
		for (int i = 0; i < wordsInUse; i++)
			if (words[i] != set.words[i])
				return false;

		return true;
	}

	/**
	 * Cloning this {@code BitSet} produces a new {@code BitSet} that is equal to
	 * it. The clone of the bit set is another bit set that has exactly the same
	 * bits set to {@code true} as this bit set.
	 *
	 * @return a clone of this bit set
	 * @see #size()
	 */
	@Override
	public ChunkSet clone()
	{
		if (!sizeIsSticky)
			trimToSize();

		try
		{
			final ChunkSet result = (ChunkSet) super.clone();
			result.words = words.clone();
			result.checkInvariants();
			return result;
		} 
		catch (final CloneNotSupportedException e)
		{
			e.printStackTrace();
			throw new InternalError();
		}
	}

	/**
	 * Attempts to reduce internal storage used for the bits in this bit set.
	 * Calling this method may, but is not required to, affect the value returned by
	 * a subsequent call to the {@link #size()} method.
	 */
	public void trimToSize()
	{
		if (wordsInUse != words.length)
		{
			words = Arrays.copyOf(words, wordsInUse);
			checkInvariants();
		}
	}

	/**
	 * Save the state of the {@code BitSet} instance to a stream (i.e., serialize
	 * it).
	 * 
	 * @param s
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream s) throws IOException
	{
		checkInvariants();

		if (!sizeIsSticky)
			trimToSize();
		
		s.writeObject(words);
		s.writeInt(chunkSize);
		s.writeLong(chunkMask);
	}

	/**
	 * Reconstitute the {@code BitSet} instance from a stream (i.e., deserialize
	 * it).
	 * 
	 * @param s
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException
	{
		words = (long[]) s.readObject();
		final int newChunkSize = s.readInt();
		final long newChunkMask = s.readLong();
		
		// chunkSize and chunkMask are final, so need Reflection to set them
		try 
		{
			final Field chunkSizeField = getClass().getDeclaredField("chunkSize");
			chunkSizeField.setAccessible(true);
			chunkSizeField.set(this, newChunkSize);
			
			final Field chunkMaskField = getClass().getDeclaredField("chunkMask");
			chunkMaskField.setAccessible(true);
			chunkMaskField.set(this, newChunkMask);
		} 
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

		// Assume maximum length then find real length
		// because recalculateWordsInUse assumes maintenance
		// or reduction in logical size
		wordsInUse = words.length;
		recalculateWordsInUse();
		sizeIsSticky = (words.length > 0 && words[words.length - 1] == 0L); // heuristic
		checkInvariants();
	}

	/**
	 * Returns a string representation of this bit set. For every index for which
	 * this {@code BitSet} contains a bit in the set state, the decimal
	 * representation of that index is included in the result. Such indices are
	 * listed in order from lowest to highest, separated by ",&nbsp;" (a comma and a
	 * space) and surrounded by braces, resulting in the usual mathematical notation
	 * for a set of integers.
	 *
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * BitSet drPepper = new BitSet();
	 * </pre>
	 * 
	 * Now {@code drPepper.toString()} returns "{@code {}}".
	 * <p>
	 * 
	 * <pre>
	 * drPepper.set(2);
	 * </pre>
	 * 
	 * Now {@code drPepper.toString()} returns "{@code {2}}".
	 * <p>
	 * 
	 * <pre>
	 * drPepper.set(4);
	 * drPepper.set(10);
	 * </pre>
	 * 
	 * Now {@code drPepper.toString()} returns "{@code {2, 4, 10}}".
	 *
	 * @return a string representation of this bit set
	 */
	@Override
	public String toString()
	{
		checkInvariants();

		final int           numBits = (wordsInUse > 128) ? cardinality() : wordsInUse * BITS_PER_WORD;
		final StringBuilder b       = new StringBuilder(6 * numBits + 2);
		b.append('{');

		int i = nextSetBit(0);
		if (i != -1)
		{
			b.append(i);
			for (i = nextSetBit(i + 1); i >= 0; i = nextSetBit(i + 1))
			{
				final int endOfRun = nextClearBit(i);
				do
				{
					b.append(", ").append(i);
				} while (++i < endOfRun);
			}
		}

		b.append('}');
		return b.toString();
	}

	// =========================================================================
	// NEW ADDITIONS FOR CHUNKING
	// =========================================================================

	/**
	 * @return Number of bits per chunk. Will be a power of 2 up to 32: 1, 2, 4, 8,
	 *         16, 32.
	 */
	public int chunkSize()
	{
		return chunkSize;
	}

	/**
	 * @return Number of complete chunks.
	 */
	public int numChunks()
	{
		return (chunkSize == 0) ? 0 : size() / chunkSize;
	}
	
	/**
	 * @return Number of chunks that have at least one set bit
	 */
	public int numNonZeroChunks()
	{
		int count = 0;
		final int numChunks = numChunks();
		for (int i = 0; i < numChunks; ++i)
		{
			if (getChunk(i) != 0)
			{
				++count;
			}
		}
		return count;
	}

//	/**
//	 * @param fromIndex Index of bit to start at.
////	 * @param numBits   Chunk size in bits (< 32).
//	 * @return Integer defined by the specified bits.
//	 */
//	public int getChunk(final int fromIndex)  //, final int numBits)
//	{
//		final int toIndex = fromIndex + chunkSize;		
//		final int wordIndexTo = wordIndex(toIndex);
//		if (wordIndexTo > wordsInUse)
//			return 0;  // out of range: partial or non-existent chunk of bits
//		
//		final long chunkMask = (0x1L << chunkSize) - 1;		
//        final int down = fromIndex % 64;
//    	
//        final int startWordIndex = wordIndex(fromIndex);
//        final int endWordIndex   = wordIndex(toIndex - 1);
//
//        final int value = (startWordIndex == endWordIndex) 
//        	? (int)((words[startWordIndex] >>> down) & chunkMask)  // single word
//        	: (int)(    // straddles two words
//            			((words[startWordIndex] >>> down) & chunkMask)
//            			|
//            			((words[endWordIndex] << (64-down)) & chunkMask)
//        			); 
//        
//        checkInvariants();
//		return value;  // & (int)chunkMask;
//	}

	/**
	 * If chunk is out of range, this function will return 0.  
	 * This is for compatibility with setChunk, which will grow the array as needed.
	 * 
	 * Chunks will not straddle word boundaries as chunkSize is a power of 2. //
	 * @param chunk Chunk to get. // * @param numBits Chunk size in bits (power of 2
	 *              <= 32, i.e. 1, 2, 4, 8, 16 or 32).
	 * @return Integer defined by the specified bits.
	 */
	public int getChunk(final int chunk) // , final int numBits)
	{
		final int bitIndex  = chunk * chunkSize;
		final int wordIndex = bitIndex >> 6;  // is (n / 64)
		
		if (wordIndex >= words.length) return 0;

//		final long chunkMask = (0x1L << chunkSize) - 1;		
		final int down      = bitIndex & 63;  // is (n % 64)

		// System.out.println("fromIndex=" + fromIndex + ", numBits=" + numBits + ".");
		// System.out.println("wordIndex=" + wordIndex + ", wordsInUse=" + wordsInUse +
		// ", words.length=" + words.length + ".");

		return (int) ((words[wordIndex] >>> down) & chunkMask);
	}
	
	/**
	 * @return A list of indices of all the non-zero chunks
	 */
	public TIntArrayList getNonzeroChunks()
	{
		final TIntArrayList indices = new TIntArrayList();
		final int chunksPerWord = Long.SIZE / chunkSize;
		
		for (int wordIdx = 0; wordIdx < words.length; ++wordIdx)
		{
			final long word = words[wordIdx];
			if (word != 0)
			{
				for (int chunkWord = 0; chunkWord < chunksPerWord; ++chunkWord)
				{
					if ((word & (chunkMask << (chunkWord * chunkSize))) != 0L)
						indices.add(wordIdx * chunksPerWord + chunkWord);
				}
			}
		}
		
		return indices;
	}
	
//	/**
//	 * Chunks will not straddle word boundaries as chunkSize is a power of 2.
////	 * @param fromIndex Index of bit to start at.
//     * @param chunk Chunk to get. 
////	 * @param numBits   Chunk size in bits (power of 2 <= 32, i.e. 1, 2, 4, 8, 16 or 32).
//     * @param offset
//     * @param mask  
//	 * @return Integer defined by the specified bits.
//	 */
//	public int getSubchunk(final int chunk, final int offset, final long mask)
//	{
//		final int bitIndex = chunk * chunkSize;
//		final int wordIndex = bitIndex >> 6;		
//		
////		final long chunkMask = (0x1L << chunkSize) - 1;		
//        final int down = bitIndex % 64 + offset;
//    	
//        //System.out.println("fromIndex=" + fromIndex + ", numBits=" + numBits + ".");
//        //System.out.println("wordIndex=" + wordIndex + ", wordsInUse=" + wordsInUse + ", words.length=" + words.length + ".");
//        
//        return (int)((words[wordIndex] >>> down) & mask);
//	}

	//-------------------------------------------------------------------------

//	/**
//	 * Encode integer value in the specified bits. Increase the Bitset size if necessary.
//	 * @param fromIndex Index of bit to start at.
////	 * @param numBits   Chunk size in bits (< 32).
//	 * @param value     Value to encode.
//	 */
//	public void setChunk(final int fromIndex, final int value)  //final int numBits, final int value)
//	{
//		 if (chunkSize == 0)
//            return;
//
//		final int toIndex = fromIndex + chunkSize;
//		checkRange(fromIndex, toIndex);
//
//        // Increase capacity if necessary
//        final int startWordIndex = wordIndex(fromIndex);
//        final int endWordIndex   = wordIndex(toIndex - 1);
//        expandTo(endWordIndex);
//
//		final long chunkMask = (0x1L << chunkSize) - 1;
//        final int up = fromIndex % 64;
//
//    	words[startWordIndex] &= ~(chunkMask << up);
//        words[startWordIndex] |= ((value & chunkMask) << up);
//
//        if (startWordIndex != endWordIndex) 
//        {
//            // Straddles two words
//            final int down = (64 - up);
//            words[endWordIndex] &= ~(chunkMask >>> down);
//            words[endWordIndex] |= ((value & chunkMask) >>> down);
//        }
//        checkInvariants();
//	}

//	/**
//	 * Encode integer value in the specified bits. Increase the Bitset size if necessary.
//	 * Chunks will not straddle word boundaries as chunkSize is a power of 2.
//	 * @param fromIndex Index of bit to start at.
////	 * @param numBits   Chunk size in bits (power of 2 <= 32, i.e. 1, 2, 4, 8, 16 or 32).
//	 * @param value     Value to encode.
//	 */
//	public void setChunk(final int fromIndex, final int value)  //final int numBits, final int value)
//	{
//		 if (chunkSize == 0)
//            return;
//
//		final int wordIndex = fromIndex >> 6;		
//        expandTo(wordIndex);
//
//		final long chunkMask = (0x1L << chunkSize) - 1;		
//        final int up = fromIndex % 64;
//
//    	words[wordIndex] &= ~(chunkMask << up);
//        words[wordIndex] |= ((value & chunkMask) << up);
//	}

	/**
	 * Encode integer value in the specified bits. Increase the Bitset size if
	 * necessary. Chunks will not straddle word boundaries as chunkSize is a power
	 * of 2. // * @param fromIndex Index of bit to start at.
	 * 
	 * @param chunk Chunk to set. // * @param numBits Chunk size in bits (power of 2
	 *              <= 32, i.e. 1, 2, 4, 8, 16 or 32).
	 * @param value Value to encode.
	 */
	public void setChunk(final int chunk, final int value) // final int numBits, final int value)
	{
		if (value < 0 || value >= (1<<chunkSize)) 
			throw new IllegalArgumentException ("Chunk value " + value + " is out of range for size = " + chunkSize);
		
		if (chunkSize == 0)
			return;

		final int bitIndex  = chunk * chunkSize;

		final int wordIndex = bitIndex >> 6;  // is (n / 64)
		expandTo(wordIndex);

//		final long chunkMask = (0x1L << chunkSize) - 1;		
		final int up = bitIndex & 63;  // is (n % 64)

		words[wordIndex] &= ~(chunkMask << up);
		words[wordIndex] |= (((long)value) << up);
		
		recalculateWordsInUse();
		checkInvariants();
	}
	
	/**
	 * Sets the value for the given chunk, and returns the value the chunk
	 * had prior to setting. This is a useful optimisation because we often
	 * want to get and immediately after set, and they partially share the
	 * same implementation.
	 * 
	 * @param chunk
	 * @param value
	 * @return Integer defined by the specified bits (prior to setting new value).
	 */
	public int getAndSetChunk(final int chunk, final int value)
	{
		if (value < 0 || value >= (1<<chunkSize)) 
			throw new IllegalArgumentException ("Chunk value " + value + " is out of range for size = " + chunkSize);
		
		if (chunkSize == 0)
			return 0;

		final int bitIndex  = chunk * chunkSize;
		final int wordIndex = bitIndex >> 6;  // is (n / 64)
		expandTo(wordIndex);

		final int down      = bitIndex & 63;  // is (n % 64)
		final int oldVal    = (int) ((words[wordIndex] >>> down) & chunkMask);
		
		words[wordIndex] &= ~(chunkMask << down);
		words[wordIndex] |= (((long)value) << down);
		
		recalculateWordsInUse();
		checkInvariants();

		return oldVal;
	}

//	/**
//	 * Encode integer value in the specified bits. Increase the Bitset size if necessary.
//	 * Chunks will not straddle word boundaries as chunkSize is a power of 2.
////	 * @param fromIndex Index of bit to start at.
//     * @param chunk     Chunk to set.
////	 * @param numBits   Chunk size in bits (power of 2 <= 32, i.e. 1, 2, 4, 8, 16 or 32).
//	 * @param value     Value to encode.
//	 * @param offset
//	 * @param mask
//	 */
//	public void setSubchunk(final int chunk, final int value, final int offset, final long mask) 
//	{
//		 if (chunkSize == 0)
//            return;
//
//		final int bitIndex = chunk * chunkSize;
//		 
//		final int wordIndex = bitIndex >> 6;		
//        expandTo(wordIndex);
//
////		final long chunkMask = (0x1L << chunkSize) - 1;		
//        final int up = bitIndex % 64 + offset;
//
//    	words[wordIndex] &= ~(mask << up);
//        words[wordIndex] |= ((value & mask) << up);
//	}

	//-------------------------------------------------------------------------

//	/**
//	 * Clear the specified bits. Does not increase Bitset size.
//	 * @param fromIndex Index of bit to start at.
////	 * @param numBits   Chunk size in bits (< 32).
//	 */
//	public void clearChunk(final int fromIndex)  //, final int numBits)
//	{
//		 if (chunkSize == 0)
//            return;
//
//		final int toIndex = fromIndex + chunkSize;
//		checkRange(fromIndex, toIndex);
//
//        // DO NOT increase capacity!
//		final int startWordIndex = wordIndex(fromIndex);
//        final int endWordIndex   = wordIndex(toIndex - 1);
//        //expandTo(endWordIndex);
//
////		final long chunkMask = (0x1L << chunkSize) - 1;
//        final int up = fromIndex % 64;
//
//    	words[startWordIndex] &= ~(chunkMask << up);
// 
//        if (startWordIndex != endWordIndex && endWordIndex <= wordsInUse)    
//        {            // MAYBE THIS SHOULD BE: endWordIndex < wordsInUse)
//            // Straddles two words
//            final int down = (64 - up);
//            words[endWordIndex] &= ~(chunkMask >>> down);
//         }
//        checkInvariants();
//	}

	/**
	 * Clear the specified chunk. Chunks will not straddle word boundaries as
	 * chunkSize is a power of 2. // * @param fromIndex Index of bit to start at.
	 * 
	 * @param chunk Chunk to clear. // * @param numBits Chunk size in bits (power of
	 *              2 <= 32, i.e. 1, 2, 4, 8, 16 or 32).
	 */
	public void clearChunk(final int chunk) // , final int numBits)
	{
		if (chunkSize == 0)
			return;

		final int bitIndex  = chunk * chunkSize;

		final int wordIndex = bitIndex >> 6;  // is (n / 64)
		if (wordIndex > wordsInUse) // or: if (wordIndex >= wordsInUse) ???
			return;

//		final long chunkMask = (0x1L << chunkSize) - 1;		
		final int up = bitIndex & 63;  // is (n % 64)

		words[wordIndex] &= ~(chunkMask << up);
		
		recalculateWordsInUse();
		checkInvariants();
	}

//	/**
//	 * Clear the specified chunk.
//	 * Chunks will not straddle word boundaries as chunkSize is a power of 2.
////	 * @param fromIndex Index of bit to start at.
//     * @param chunk Chunk to clear. 
////	 * @param numBits   Chunk size in bits (power of 2 <= 32, i.e. 1, 2, 4, 8, 16 or 32).
//     * @param offset
//     * @param mask 
//	 */
//	public void clearSubchunk(final int chunk, final int offset, final long mask)
//	{
//		 if (chunkSize == 0)
//            return;
//
//		final int bitIndex = chunk * chunkSize;
//		 
//		final int wordIndex = bitIndex >> 6;
//		if (wordIndex > wordsInUse)  // or: if (wordIndex >= wordsInUse) ???
//			return;
// 
////		final long chunkMask = (0x1L << chunkSize) - 1;		
//        final int up = bitIndex % 64 + offset;
//
//    	words[wordIndex] &= ~(mask << up);
// 	}

	/**
	 * Sets all of the bits in this BitSet to {@code false} without resizing.
	 */
	public void clearNoResize()
	{
		for (int w = 0; w < wordsInUse; w++)
			words[w] = 0;
		
		wordsInUse = 0;
		checkInvariants();
	}
	
	//-------------------------------------------------------------------------
	// Bit routines for CSP puzzle states
	
	public int getBit(final int chunk, final int bit)
	{
		final int bitIndex  = chunk * chunkSize;
		final int wordIndex = bitIndex >> 6;  // is (n / 64)
		final int down = bitIndex & 63;  // is (n % 64)

		return (int) ((words[wordIndex] >>> (down + bit)) & 0x1L);
	}
	
	public void setBit(final int chunk, final int bit, final boolean value)
	{
		if (chunkSize == 0)
			return;

		final int bitIndex  = chunk * chunkSize;

		final int wordIndex = bitIndex >> 6;  // is (n / 64)
		expandTo(wordIndex);

		final int up = bitIndex & 63;  // is (n % 64)
		final long bitMask = 0x1L << (up + bit);

		if (value)
			words[wordIndex] |= bitMask;
		else
			words[wordIndex] &= ~bitMask;
		
		recalculateWordsInUse();
		checkInvariants();
	}
	
	public void toggleBit(final int chunk, final int bit)
	{
		if (chunkSize == 0)
			return;

		final int bitIndex = chunk * chunkSize;

		final int wordIndex = bitIndex >> 6;  // is (n / 64)
		expandTo(wordIndex);

		final int up = bitIndex & 63;  // is (n % 64)		
		final long bitMask = 0x1L << (up + bit);

		words[wordIndex] ^= bitMask;
		
		recalculateWordsInUse();
		checkInvariants();
	}

	public void setNBits(final int chunk, final int numBits, final boolean value)
	{
		if (chunkSize == 0)
			return;

		final int bitIndex  = chunk * chunkSize;

		final int wordIndex = bitIndex >> 6;  // is (n / 64)
		expandTo(wordIndex);

		final int up = bitIndex & 63;  // is (n % 64)
		final long bitsNMask = bitNMasks[numBits] << up;

		if (value)
			words[wordIndex] |= bitsNMask;
		else
			words[wordIndex] &= ~bitsNMask;
		
		recalculateWordsInUse();
		checkInvariants();
	}
	
	public void resolveToBit(final int chunk, final int bit)
	{
		if (chunkSize == 0)
			return;

		final int bitIndex = chunk * chunkSize;

		final int wordIndex = bitIndex >> 6;  // is (n / 64)
		expandTo(wordIndex);

		final int up = bitIndex & 63;  // is (n % 64)		
		final long bitMask = 0x1L << (up + bit);

		words[wordIndex] &= ~(chunkMask << up);
		words[wordIndex] |= bitMask;
		
		recalculateWordsInUse();
		checkInvariants();
	}
	
	public int numBitsOn(final int chunk)
	{
		final int bitIndex  = chunk * chunkSize;
		final int wordIndex = bitIndex >> 6;  // is (n / 64)

		final int down  = bitIndex & 63;  // is (n % 64)
		final int value = (int)((words[wordIndex] >>> down) & chunkMask);
		
		// Count on-bits in the chunk
		int numBits = 0;
		for (int b = 0; b < chunkSize; b++)
			if (((0x1 << b) & value) != 0)
				numBits++;
		
		return numBits;
	}
	
	public boolean isResolved(final int chunk)
	{
		return numBitsOn(chunk) == 1;
	}
	
	/**
	 * @param chunk
	 * @return Resolved value, else 0 if not resolved yet.
	 */
	public int resolvedTo(final int chunk)
	{
		final int bitIndex  = chunk * chunkSize;
		final int wordIndex = bitIndex >> 6;  // is (n / 64)

		final int down  = bitIndex & 63;  // is (n % 64)
		final int value = (int)((words[wordIndex] >>> down) & chunkMask);
		
		// Check the on-bits in the chunk
		int result = -1;
		int numBits = 0;
		for (int b = 0; b < chunkSize; b++)
			if (((0x1 << b) & value) != 0)
			{
				result = b;
				numBits++;
			}
		
		return (numBits == 1) ? result : 0;
	}

	//-------------------------------------------------------------------------

	/**
	 * Equivalent to: this <<= numBits;
	 * 
	 * @param numBits Number of bits to shift (< 64).
	 * @param expand  Whether to expand the bitset if necessary.
	 */
	public void shiftL(final int numBits, final boolean expand)
	{
		if (numBits == 0)
			return;

		if (expand)
		{
			// Ensure that no shifted bits will be lost
			final int maxIndex = wordIndex(length() + numBits);
			expandTo(maxIndex);
		}

		final int remnant = 64 - numBits;
		long      carry   = 0;
		for (int idx = 0; idx < wordsInUse; idx++)
		{
			final long temp = words[idx] >>> remnant;
			words[idx] = (words[idx] << numBits) | carry;
			carry = temp;
		}

		if (!expand)
		{
			// Mask out unused bits of highest word so that original size is not exceeded
			final int size = size();
			if ((size & 63) > 0)
			{
				final long mask = (0x1L << (size & 63)) - 1;
				words[wordsInUse - 1] &= mask;
			}
		}
		// Any leftover bits are either added to expanded bitset or lost, as specified
		
		recalculateWordsInUse();
		checkInvariants();
	}

	/**
	 * Equivalent to: this >>>= numBits;
	 * 
	 * @param numBits Number of bits to shift (< 64).
	 */
	public void shiftR(final int numBits)
	{
		if (numBits == 0)
			return;

		final int remnant = 64 - numBits;
		long      carry   = 0;
		for (int idx = wordsInUse - 1; idx >= 0; idx--)
		{
			final long temp = words[idx] << remnant;
			words[idx] = (words[idx] >>> numBits) | carry;
			carry = temp;
		}
		// Any leftover bits are lost
		
		recalculateWordsInUse();
		checkInvariants();
	}

	//-------------------------------------------------------------------------

	/**
	 * We say that this ChunkSet matches the specified pattern if and only
	 * if all the bits covered by the given mask in this ChunkSet are equal
	 * to the corresponding bits in the given pattern.
	 * 
	 * @param mask
	 * @param pattern
	 * @return Whether this ChunkSet (masked) matches the specified pattern.
	 */
	public boolean matches(final ChunkSet mask, final ChunkSet pattern)
	{
		final int maskWordsInUse = mask.wordsInUse;
		
		if (wordsInUse < maskWordsInUse)
			return false;
		
		for (int n = 0; n < maskWordsInUse; n++)
			if ((words[n] & mask.words[n]) != pattern.words[n])
				return false;

		return true;
	}
	
	/**
	 * @param wordIdx
	 * @param mask
	 * @param matchingWord
	 * @return True if the word at wordIdx, after masking by mask, matches the given word
	 */
	public boolean matchesWord(final int wordIdx, final long mask, final long matchingWord)
	{
		if (words.length <= wordIdx)
			return false;
		
		return ((words[wordIdx] & mask) == matchingWord);
	}
	
	/**
	 * Adds given mask to given word
	 * @param wordIdx
	 * @param mask
	 */
	public void addMask(final int wordIdx, final long mask)
	{
		expandTo(wordIdx);
		words[wordIdx] |= mask;
		checkInvariants();
	}

	//-------------------------------------------------------------------------
	
	/**
	 * We say that this ChunkSet violates the specified not-pattern if and only
	 * if there exists at least one chunk in this ChunkSet which is covered by
	 * the given mask and which is equal to the corresponding chunk in the
	 * given pattern.
	 * 
	 * @param mask
	 * @param pattern
	 * @return Whether this ChunkSet (masked) violates the specified 
	 * not-pattern.
	 */
	public boolean violatesNot(final ChunkSet mask, final ChunkSet pattern)
	{
		return violatesNot(mask, pattern, 0);
	}

	/**
	 * We say that this ChunkSet violates the specified not-pattern if and only
	 * if there exists at least one chunk in this ChunkSet which is covered by
	 * the given mask and which is equal to the corresponding chunk in the
	 * given pattern.
	 * 
	 * @param mask
	 * @param pattern
	 * @param startWord The first word to start looping at
	 * @return Whether this ChunkSet (masked) violates the specified 
	 * not-pattern.
	 */
	public boolean violatesNot
	(
		final ChunkSet mask, final ChunkSet pattern, final int startWord
	)
	{
		// TODO I don't think we want this check here...
		/*
		if (wordsInUse < mask.wordsInUse)
		{
			System.out.println("wordsInUse < mask.wordsInUse in violatesNot()!");
			System.out.println("this = " + this);
			System.out.println("mask = " + mask);
			System.out.println("pattern = " + pattern);
			return true;
		}
		*/

		final int wordsToCheck = Math.min(wordsInUse, mask.wordsInUse);
		for (int n = startWord; n < wordsToCheck; n++)
		{
			// if ((words[n] & mask.words[n]) != pattern.words[n])
			// return false;

			// TODO: Remove ifs?

			// Redundant ifs?

			long temp = ~(words[n] ^ pattern.words[n]) & mask.words[n];
			if (chunkSize > 1)
			{
				temp = ((temp & MASK_NOT_1) >>> 1) & temp;
				if (chunkSize > 2)
				{
					temp = ((temp & MASK_NOT_2) >>> 2) & temp;
					if (chunkSize > 4)
					{
						temp = ((temp & MASK_NOT_4) >>> 4) & temp;
						if (chunkSize > 8)
						{
							temp = ((temp & MASK_NOT_8) >>> 8) & temp;
							if (chunkSize > 16)
							{
								temp = ((temp & MASK_NOT_16) >>> 16) & temp;
								if (chunkSize > 32)
								{
									temp = ((temp & MASK_NOT_32) >>> 32) & temp;
								}
							}
						}
					}
				}
			}
			if (temp != 0)
				return true; // violation
		}
		return false; // no violation
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return A string-representation that conveniently shows the values of
	 * non-zero chunks.
	 */
	public String toChunkString()
	{
		checkInvariants();

		final int numBits = (wordsInUse > 128) ? cardinality() : wordsInUse * BITS_PER_WORD;
		final StringBuilder b = new StringBuilder(6 * numBits + 2);
		b.append('{');
		
		for (int i = 0; i < numChunks(); ++i)
		{
			final int value = getChunk(i);
			
			if (value != 0)
			{
				if (b.toString().length() > 1)
				{
					b.append(", ");
				}
				
				b.append("chunk " + i + " = " + value);
			}
		}

		b.append('}');
		return b.toString();
	}

	//-------------------------------------------------------------------------
}
