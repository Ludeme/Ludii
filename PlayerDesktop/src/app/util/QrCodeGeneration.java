package app.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import game.Game;
import graphics.qr_codes.QrCode;
import graphics.qr_codes.ToImage;
import main.DatabaseInformation;

/**
 * QR code generation functions.
 * 
 * @author cambolbro and matthew.stephenson
 */
public class QrCodeGeneration
{
	/**
	 * Saves a QR code png for the specified game. Will use the ruleset is available.
	 * @param game
	 */
	public static void makeQRCode(final Game game)
	{
		// Determine the file name
		String fileName = "qr-" + game.name();
		if (game.getRuleset() != null)
		{
			fileName += "-" + game.getRuleset().heading();
			fileName = fileName.replaceAll("Ruleset/", "");  // remove keyword
		}
		fileName = fileName.replaceAll(" ", "-");  // remove empty spaces
		fileName = fileName.replaceAll("/", "-");  // remove slashes spaces
		fileName += ".png";
		
		// Determine URL to encode
		String url = "https://ludii.games/details.php?keyword=" + game.name(); 
		if (game.getRuleset() != null)
		{
			// Format: https://ludii.games/variantDetails.php?keyword=Achi&variant=563
			final int variant = DatabaseInformation.getRulesetId(game.name(), game.getRuleset().heading());
			url = "https://ludii.games/variantDetails.php?keyword=" + game.name() + 
				  "&variant=" + variant;
		}
		url = url.replaceAll(" ", "%20");  // make URL valid HTML (yuck)
		
		final QrCode qr = QrCode.encodeText(url, QrCode.Ecc.MEDIUM);
	
		// Make the image
		final int scale = 10;
		final int border = 4;
	
		final BufferedImage img = ToImage.toLudiiCodeImage(qr, scale, border);   
		try
		{
			ImageIO.write(img, "png", new File(fileName));
		}
		catch (final IOException e1)
		{
			e1.printStackTrace();
		} 
	}
}
