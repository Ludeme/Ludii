package games.options;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import org.junit.Test;

import game.Game;
import main.StringRoutines;
import main.collections.ListUtils;
import main.options.Option;
import other.GameLoader;
import other.context.Context;
import other.trial.Trial;

/**
 * Unit Test to run one playout per game, for every possible
 * combination of options
 * 
 * @author Eric.Piette, Dennis Soemers and cambolbro
 */
public class OnePlayoutPerGameTestWithOptions
{

	@Test
	@SuppressWarnings("static-method")
	public void test()
	{
		final File startFolder = new File("../Common/res/lud/");
		final List<File> gameDirs = new ArrayList<File>();
		gameDirs.add(startFolder);

		final List<File> entries = new ArrayList<File>();
		
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

					if (path.equals("../Common/res/lud/WishlistDLP"))
						continue;

					if (path.equals("../Common/res/lud/test"))
						continue;

					if (path.equals("../Common/res/lud/puzzle/deduction"))
						continue; // skip deduction puzzles for now
					
					if (path.equals("../Common/res/lud/bad"))
						continue;
					
					if (path.equals("../Common/res/lud/bad_playout"))
						continue;
					
					gameDirs.add(fileEntry);
				}
				else
				{
					entries.add(fileEntry);
				}
			}
		}
		
		// The following entries should correctly compile and run
		for (final File fileEntry : entries)
		{
			if (fileEntry.getName().contains(".lud"))
			{
				final String fileName = fileEntry.getPath();
				System.out.println("File: " + fileName);

				final Game game = GameLoader.loadGameFromFile(fileEntry);
				
				System.out.println(game.name());

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
	
				// If no option (just the default game) we do not run the test.
				if (optionCombinations.size() == 1)
					continue;

				for (final List<String> optionCombination : optionCombinations)
				{
					System.out.println("Compiling and running playout with options: " + optionCombination);
					final Game gameWithOptions = GameLoader.loadGameFromFile(fileEntry, optionCombination);
					final Trial trial = new Trial(gameWithOptions);
					final Context context = new Context(gameWithOptions, trial);
					
					gameWithOptions.start(context);
					gameWithOptions.playout(context, null, 1.0, null, 0, -1, ThreadLocalRandom.current());
				}
			}
		}
	}

}
