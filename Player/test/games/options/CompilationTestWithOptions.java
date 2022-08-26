package games.options;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import game.Game;
import main.StringRoutines;
import main.collections.ListUtils;
import main.options.Option;
import other.GameLoader;

//-----------------------------------------------------------------------------

/**
 * Unit Test to compile all the games on the lud folder, with all possible
 * combinations of options.
 *
 * @author Dennis Soemers and Eric.Piette and cambolbro
 */
public class CompilationTestWithOptions
{
	@Test
	public static void testCompilingLudFromFile()
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

					if (path.equals("../Common/res/lud/wip"))
						continue;

					if (path.equals("../Common/res/lud/wishlist"))
						continue;

					if (path.equals("../Common/res/lud/test"))
						continue;

					if (path.equals("../Common/res/lud/bad"))
						continue;
					
					gameDirs.add(fileEntry);
				}
				else
				{
					entries.add(fileEntry);
				}
			}
		}

		// Test of compilation for each of them.
		for (final File fileEntry : entries)
		{
			final String fileName = fileEntry.getPath();

			System.out.println("File: " + fileName);

			final Game game = GameLoader.loadGameFromFile(fileEntry);
			
			assert (game != null);
			
			final List<List<String>> optionCategories = new ArrayList<List<String>>();
				
			for (int o = 0; o < game.description().gameOptions().numCategories(); o++)
			{
				final List<Option> options = game.description().gameOptions().categories().get(o).options();
				final List<String> optionCategory = new ArrayList<String>();

				for (int i = 0; i < options.size(); i++)
				{
					final Option option = options.get(i);
					optionCategory.add(StringRoutines.join("/", option.menuHeadings().toArray(new String[0])));
				}

				if (optionCategory.size() > 0)
					optionCategories.add(optionCategory);
			}

			final List<List<String>> optionCombinations = ListUtils.generateTuples(optionCategories);

			if (optionCombinations.size() > 1)
			{
				for (final List<String> optionCombination : optionCombinations)
				{
					System.out.println("Compiling with options: " + optionCombination);
					assert (GameLoader.loadGameFromFile(fileEntry, optionCombination) != null);
				}
			}
		}
	}

}
