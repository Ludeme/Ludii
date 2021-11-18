package graphics.qr_codes;

/* 
 * Fast QR Code generator library
 * 
 * Copyright (c) Project Nayuki. (MIT License)
 * https://www.nayuki.io/page/fast-qr-code-generator-library
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * - The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 * - The Software is provided "as is", without warranty of any kind, express or
 *   implied, including but not limited to the warranties of merchantability,
 *   fitness for a particular purpose and noninfringement. In no event shall the
 *   authors or copyright holders be liable for any claim, damages or other
 *   liability, whether in an action of contract, tort or otherwise, arising from,
 *   out of or in connection with the Software or the use or other dealings in the
 *   Software.
 */

/*---- Public helper enumeration ----*/

/**
 * Describes how a segment's data bits are interpreted.
 */
public enum Mode {
	
	/*-- Constants --*/
	
	NUMERIC     (0x1, 10, 12, 14),
	ALPHANUMERIC(0x2,  9, 11, 13),
	BYTE        (0x4,  8, 16, 16),
	KANJI       (0x8,  8, 10, 12),
	ECI         (0x7,  0,  0,  0);
	
	
	/*-- Fields --*/
	
	// The mode indicator bits, which is a uint4 value (range 0 to 15).
	final int modeBits;
	
	// Number of character count bits for three different version ranges.
	private final int[] numBitsCharCount;
	
	
	/*-- Constructor --*/
	
	private Mode(int mode, int... ccbits) {
		modeBits = mode;
		numBitsCharCount = ccbits;
	}
	
	
	/*-- Method --*/
	
	// Returns the bit width of the character count field for a segment in this mode
	// in a QR Code at the given version number. The result is in the range [0, 16].
	int numCharCountBits(int ver) {
		assert QrCode.MIN_VERSION <= ver && ver <= QrCode.MAX_VERSION;
		return numBitsCharCount[(ver + 7) / 17];
	}
	
}
