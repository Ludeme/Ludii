package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.regex.Pattern;

import game.Game;
import game.types.state.GameType;
import main.FileHandling;
import main.StringRoutines;
import main.UnixPrintWriter;
import other.GameLoader;

/**
 * Method to create a csv file listing all the GameTypes used by each game.
 *
 * @author Eric Piette
 */
public class ExportGameType
{
	/**
	 * Main method
	 * 
	 * @param args
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static void main(final String[] args) throws IllegalArgumentException, IllegalAccessException
	{
		try (final PrintWriter writer = new UnixPrintWriter(new File("./res/concepts/output/LudiiGameFlags.csv"),
				"UTF-8"))
		{
			final Field[] fields = GameType.class.getFields();
			final String[] flags = new String[fields.length];
			final long[] flagsValues = new long[fields.length];
			
			for (int i = 0; i < fields.length; i++)
			{
				flags[i] = fields[i].toString();
				flags[i] = flags[i].substring(flags[i].lastIndexOf('.') + 1);
				flagsValues[i] = fields[i].getLong(GameType.class);
			}

			final String[] headers = new String[flags.length + 1];
			headers[0] = "Game Name";
			for (int i = 0; i < flags.length; i++)
				headers[i + 1] = flags[i];

			// Write header line
			writer.println(StringRoutines.join(",", headers));

			final String[] gameNames = FileHandling.listGames();

			for (final String gameName : gameNames)
			{
				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/bad/"))
					continue;

				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wip/"))
					continue;

				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/WishlistDLP/"))
					continue;

				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/test/"))
					continue;

				System.out.println("Loading game: " + gameName);
				final Game game = GameLoader.loadGameFromName(gameName);
				final String flagsOn[] = new String[flags.length + 1];
				flagsOn[0] = game.name();
				for (int i = 0; i < flagsValues.length; i++)
				{
					if ((game.gameFlags() & flagsValues[i]) != 0L)
						flagsOn[i + 1] = "Yes";
					else
						flagsOn[i + 1] = "";
				}

				// Write row for this game
				writer.println(StringRoutines.join(",", flagsOn));
			}
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}

}
