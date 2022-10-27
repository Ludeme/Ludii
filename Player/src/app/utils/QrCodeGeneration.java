package app.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
		makeQRCode(game, 10, 4, true);
	}
	
	public static void makeQRCode(final Game game, final int scale, final int border, final boolean includeRuleset)
	{
		// Determine the file name
		String fileName = "qr-" + game.name();
		if (game.getRuleset() != null && includeRuleset)
		{
			fileName += "-" + game.getRuleset().heading();
			fileName = fileName.replaceAll("Ruleset/", "");  // remove keyword
		}
		//fileName = fileName.replaceAll(" ", "-");  // remove empty spaces
		//fileName = fileName.replaceAll("/", "-");  // remove slashes spaces
		fileName += ".png";
		
		// Determine URL to encode
		String url = "https://ludii.games/details.php?keyword=" + game.name(); 
		if (game.getRuleset() != null && includeRuleset)
		{
			// Format: https://ludii.games/variantDetails.php?keyword=Achi&variant=563
			final int variant = DatabaseInformation.getRulesetId(game.name(), game.getRuleset().heading());
			url = "https://ludii.games/variantDetails.php?keyword=" + game.name() + 
				  "&variant=" + variant;
		}
		url = url.replaceAll(" ", "%20");  // make URL valid HTML (yuck)
		
		final QrCode qr = QrCode.encodeText(url, QrCode.Ecc.MEDIUM);
	
		// Make the image
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
	
	/**
	 * To be used only for exhibition QR codes.
	 */
//	public static void main(String[] args)
//	{
//		final File startFolder = new File("../Common/res/lud");
//		final List<File> gameDirs = new ArrayList<File>();
//		gameDirs.add(startFolder);
//
//		final List<File> entries = new ArrayList<File>();
//
//		//final String moreSpecificFolder = "../Common/res/lud/board/war/leaping/diagonal";
//		final String moreSpecificFolder = "";
//		
//		for (int i = 0; i < gameDirs.size(); ++i)
//		{
//			final File gameDir = gameDirs.get(i);
//
//			for (final File fileEntry : gameDir.listFiles())
//			{
//				if (fileEntry.isDirectory())
//				{
//					final String fileEntryPath = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");
//
//					if (fileEntryPath.equals("../Common/res/lud/plex"))
//						continue;
//
//					if (fileEntryPath.equals("../Common/res/lud/wip"))
//						continue;
//
//					if (fileEntryPath.equals("../Common/res/lud/wishlist"))
//						continue;
//
//					if (fileEntryPath.equals("../Common/res/lud/WishlistDLP"))
//						continue;
//
//					if (fileEntryPath.equals("../Common/res/lud/test"))
//						continue;
//
//					if (fileEntryPath.equals("../Common/res/lud/bad"))
//						continue;
//
//					if (fileEntryPath.equals("../Common/res/lud/bad_playout"))
//						continue;
//
//						gameDirs.add(fileEntry);
//				}
//				else
//				{
//					final String fileEntryPath = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");
//					if (moreSpecificFolder.equals("") || fileEntryPath.contains(moreSpecificFolder))
//						entries.add(fileEntry);
//				}
//			}
//		}
//		
//		for (File file : entries)
//		{
//			String gameName = file.getPath().substring(file.getPath().lastIndexOf('\\')+1);
//			gameName = gameName.substring(0, gameName.length()-4);
//			
//			// Determine the file name
//			String fileName = "qr-" + gameName;
//
//			//fileName = fileName.replaceAll(" ", "-");  // remove empty spaces
//			//fileName = fileName.replaceAll("/", "-");  // remove slashes spaces
//			fileName += ".png";
//			
//			// Determine URL to encode
//			String url = "https://ludii.games/details.php?keyword=" + gameName; 
//			url = url.replaceAll(" ", "%20");  // make URL valid HTML (yuck)
//			
//			final QrCode qr = QrCode.encodeText(url, QrCode.Ecc.MEDIUM);
//		
//			// Make the image
//			final BufferedImage img = ToImage.toLudiiCodeImage(qr, 5, 2);   
//			try
//			{
//				ImageIO.write(img, "png", new File(fileName));
//			}
//			catch (final IOException e1)
//			{
//				e1.printStackTrace();
//			} 
//		}
//	}
}
