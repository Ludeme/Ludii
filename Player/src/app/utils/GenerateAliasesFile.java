package app.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import game.Game;
import other.GameLoader;

/**
 * Small class with main method to generate our resource file 
 * containing aliases of games.
 *
 * @author Dennis Soemers
 */
public class GenerateAliasesFile
{
	/** Filepath to which we'll write the file containing data on aliases */
	private static final String ALIASES_FILEPATH = "../Common/res/help/Aliases.txt";
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	private GenerateAliasesFile()
	{
		// Do not instantiate
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Our main method
	 * @param args
	 */
	public static void main(final String[] args)
	{
		final File startFolder = new File("../Common/res/lud/");
		final List<File> gameDirs = new ArrayList<>();
		gameDirs.add(startFolder);

		// We compute the .lud files (and not the ludemeplex).
		final List<File> entries = new ArrayList<>();

		for (int i = 0; i < gameDirs.size(); ++i)
		{
			final File gameDir = gameDirs.get(i);

			for (final File fileEntry : gameDir.listFiles())
			{
				if (fileEntry.isDirectory())
				{
					final String path = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");

					if (path.equals("../Common/res/lud/plex"))
						continue;

					if (path.equals("../Common/res/lud/wishlist"))
						continue;

					if (path.equals("../Common/res/lud/WishlistDLP"))
						continue;

					if (path.equals("../Common/res/lud/wip"))
						continue;

					if (path.equals("../Common/res/lud/test"))
						continue;

					if (path.equals("../Common/res/lud/bad"))
						continue;

					if (path.equals("../Common/res/lud/bad_playout"))
						continue;
					
					if (path.equals("../Common/res/lud/subgame"))
						continue;
					
					if (path.equals("../Common/res/lud/reconstruction"))
						continue;

					gameDirs.add(fileEntry);
				}
				else if (fileEntry.getName().contains(".lud"))
				{
					entries.add(fileEntry);
				}
			}
		}
		
		try 
		(
			final PrintWriter writer = 
			new PrintWriter(new OutputStreamWriter(new FileOutputStream(ALIASES_FILEPATH), StandardCharsets.UTF_8))
		)
		{	
			for (final File fileEntry : entries)
			{
				System.out.println("Processing: " + fileEntry.getAbsolutePath() + "...");
				final Game game = GameLoader.loadGameFromFile(fileEntry);
				final String[] aliases = game.metadata().info().getAliases();
				
				if (aliases != null && aliases.length > 0)
				{
					// First print the game path
					final String fileEntryPath = fileEntry.getAbsolutePath().replaceAll(Pattern.quote("\\"), "/");
					final int ludPathStartIdx = fileEntryPath.indexOf("/lud/");
					writer.println(fileEntryPath.substring(ludPathStartIdx));
					
					// Now print the aliases
					for (final String alias : aliases)
					{
						writer.println(alias);
					}
				}
			}
			
			System.out.println("Finished processing aliases.");
			System.out.println("Wrote to file: " + new File(ALIASES_FILEPATH).getAbsolutePath());
		}
		catch (final FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------

}
