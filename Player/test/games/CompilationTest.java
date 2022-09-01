package games;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import compiler.Compiler;
import game.Game;
import main.FileHandling;
import main.grammar.Description;
import other.GameLoader;

/**
 * Unit Test to compile all the games on the lud folder
 *
 * @author Eric.Piette, Dennis Soemers
 */
public class CompilationTest
{
	// Commented because already done in the playout by option TEST.
//	@Test
//	public void testCompilingLudFromFile()
//	{
//		System.out.println("=========================================\nTest: Compile all .lud from file:\n");
//
//		boolean failure = false;
//		final long startAt = System.nanoTime();
//
//		final File startFolder = new File("../Common/res/lud/");
//		final List<File> gameDirs = new ArrayList<>();
//		gameDirs.add(startFolder);
//
//		final List<String> failedGames = new ArrayList<String>();
//
//		// We compute the .lud files (and not the ludemeplex).
//		final List<File> entries = new ArrayList<>();
//		final List<File> badEntries = new ArrayList<>();
//
//		for (int i = 0; i < gameDirs.size(); ++i)
//		{
//			final File gameDir = gameDirs.get(i);
//
//			for (final File fileEntry : gameDir.listFiles())
//			{
//				if (fileEntry.isDirectory())
//				{
//					final String path = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");
//					
//					if (path.equals("../Common/res/lud/plex"))
//						continue;
//
//					if (path.equals("../Common/res/lud/wip"))
//						continue;
//
//					if (path.equals("../Common/res/lud/wishlist"))
//						continue;
//
//					if (path.equals("../Common/res/lud/test"))
//						continue;
//										
//					if (path.equals("../Common/res/lud/bad"))
//					{
//						// We'll only find intentionally bad lud files here
//						for (final File fileEntryInter : fileEntry.listFiles())
//						{
//							badEntries.add(fileEntryInter);
//						}
//					}
//					else
//					{
//						// We'll find files that we should be able to compile here
//						gameDirs.add(fileEntry);
//					}
//				}
//				else
//				{
//					if (!fileEntry.getName().contains(".lud"))
//						continue;  // skip non-ludeme .DS_Store files
//					
//					entries.add(fileEntry);
//				}
//			}
//		}
//
//		// Test of compilation for each of them.
//		for (final File fileEntry : entries)
//		{
//			final String fileName = fileEntry.getPath();
//			
//			System.out.println("File: " + fileName);
//
//			// Load the string from file
//			String desc = "";
//			//			String line = null;
//			try
//			{
//				//				final FileReader fileReader = new FileReader(fileName);
//				//				final BufferedReader bufferedReader = new BufferedReader(fileReader);
//				//				while ((line = bufferedReader.readLine()) != null)
//				//					desc += line + "\n";
//				//				bufferedReader.close();
//				desc = FileHandling.loadTextContentsFromFile(fileName);
//			}
//			catch (final FileNotFoundException ex)
//			{
//				failure = true;
//				System.err.println("Unable to open file '" + fileName + "'");
//			}
//			catch (final IOException ex)
//			{
//				failure = true;
//				System.err.println("Error reading file '" + fileName + "'");
//			}
//
//			// Parse and compile the game
//			final Game game = (Game)Compiler.compileTest(new Description(desc), false);
//			if (game != null)
//			{
//				System.out.println("Compiled " + game.name() + ".\n");
//			}
//			else
//			{
//				failure = true;
//				failedGames.add(fileName);
//				System.err.println("** FAILED TO COMPILE GAME.");
//			}
//		}
//
//		// Test of compilation for bad entries.
//		for (final File fileEntry : badEntries)
//		{
//			final String fileName = fileEntry.getPath();
//			System.out.println("File: " + fileName);
//
//			// Load the string from file
//			String desc = "";
//			try
//			{
//				desc = FileHandling.loadTextContentsFromFile(fileName);
//			}
//			catch (final FileNotFoundException ex)
//			{
//				failure = true;
//				System.err.println("Unable to open file '" + fileName + "'");
//			}
//			catch (final IOException ex)
//			{
//				failure = true;
//				System.err.println("Error reading file '" + fileName + "'");
//			}
//
//			try
//			{
//				// Parse and compile the game
////				final UserSelections userSelections = new UserSelections();
//				final Game game =   (Game)Compiler.compileTest
//									(
////										desc, 
//										new Description(desc),
////										new int[GameOptions.MAX_OPTION_CATEGORIES], 
////										userSelections,
//										false
//									);
//				if (game != null)
//				{
//					failure = true;
//					System.err.println("Expected to fail compilation of bad file, but compilation was successful: " + game.name() + ".\n");
//				}
//			}
//			catch (final CompilerException exception)
//			{
//				// Ignore exception, we expect exceptions in bad .lud files
//			}
//		}
//
//		final long stopAt = System.nanoTime();
//		final double secs = (stopAt - startAt) / 1000000000.0;
//		System.out.println("Compiled " + entries.size() + " games.");
//		System.out.println("Time: " + secs + "s.");
//
//		if (!failedGames.isEmpty())
//		{
//			System.out.println("The uncompiled games are ");
//			for (final String name : failedGames)
//				System.out.println(name);
//		}
//
//		if (failure)
//			fail();
//	}

	@Test
	public static void testCompilingLudFromMemory()
	{
		System.out.println("\n=========================================\nTest: Compile all .lud from memory:\n");

		final List<String> failedGames = new ArrayList<String>();

		boolean failure = false;
		final long startAt = System.nanoTime();

		// Load from memory
		final String[] choices = FileHandling.listGames();

		for (final String fileName : choices)
		{
			if (fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/bad/"))
				continue;
			
			if (fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wip/"))
				continue;
			
			if (fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/WishlistDLP/"))
				continue;

			if (fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/test/"))
				continue;

			if (fileName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/reconstruction/"))
				continue;
			
			// Get game description from resource
			System.out.println("Game: " + fileName);

			String path = fileName.replaceAll(Pattern.quote("\\"), "/");
			path = path.substring(path.indexOf("/lud/"));

			String desc = "";
			String line;
			try
			(
				final InputStream in = GameLoader.class.getResourceAsStream(path);
				final BufferedReader rdr = new BufferedReader(new InputStreamReader(in));
			)
			{
				while ((line = rdr.readLine()) != null)
				{
					desc += line + "\n";
					//						System.out.println("line: " + line);
				}
			}
			catch (final IOException e1)
			{
				failure = true;
				e1.printStackTrace();
			}

			// Parse and compile the game
			Game game = null;
			try
			{
				game = (Game)Compiler.compileTest(new Description(desc), false);
			}
			catch (final Exception e)
			{
				failure = true;
				e.printStackTrace();
			}

			if (game != null)
			{
				System.out.println("Compiled " + game.name() + ".");
			}
			else
			{
				failure = true;
				failedGames.add(fileName);
				System.err.println("** FAILED TO COMPILE: " + fileName + ".");
			}
			
			// Uncomment this to generate QR codes for all games.
			//QrCodeGeneration.makeQRCode(game, 5, 2, false);
		}
		final long stopAt = System.nanoTime();
		final double secs = (stopAt - startAt) / 1000000000.0;
		System.out.println("\nDone in " + secs + "s.");

		if (!failedGames.isEmpty())
		{
			System.out.println("\nUncompiled games:");
			for (final String name : failedGames)
				System.out.println(name);
		}

		if (failure)
			fail();
	}

}
