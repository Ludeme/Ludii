package graphics.qr_codes;

/* 
 * Fast QR Code generator demo
 * 
 * Run this command-line program with no arguments. The program creates/overwrites a bunch of
 * PNG and SVG files in the current working directory to demonstrate the creation of QR Codes.
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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

//-----------------------------------------------------------------------------

public final class QrCodeGeneratorDemo 
{
	// The main application program.
	public static void main(String[] args) throws IOException 
	{
		doLudiiDemo();
	}
	
	private static void doLudiiDemo() throws IOException 
	{
		// Make the QR code object
		final String text = "https://ludii.games/variantDetails.php?keyword=Achi&variant=563";
		final QrCode qr = QrCode.encodeText(text, QrCode.Ecc.MEDIUM);
		
		// Make the image
		final int scale = 10;
		final int border = 4;
		
		//final BufferedImage img = ToImage.toImage(qr, scale, border);   
		//ToImage.addLudiiLogo(img, scale, false);
		final BufferedImage img = ToImage.toLudiiCodeImage(qr, scale, border);   
		ImageIO.write(img, "png", new File("qr-game-1.png")); 
		
		//String svg = ToImage.toSvgString(qr, 4, "#FFFFFF", "#000000");
		//File svgFile = new File("game-1.svg");
		//Files.write(svgFile.toPath(), svg.getBytes(StandardCharsets.UTF_8));
	}
}
