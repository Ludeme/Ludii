package app.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import main.StringRoutines;

/**
 * Convenience class to count the number of rulesets implemented.
 * 
 * @author Eric.Piette
 */
public final class CountRulesetsDone
{
	public static void main(final String[] args)
	{
		countRuleSets();
	}

	// -------------------------------------------------------------------------

	private static void countRuleSets()
	{
		int count = 0;
		final File startFolder = new File("../Common/res/lud");
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
					final String fileEntryPath = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");

					if (fileEntryPath.equals("../Common/res/lud/plex"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/wip"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/wishlist"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/reconstruction"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/WishlistDLP"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/test"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/puzzle/deduction"))
						continue; // skip puzzles for now

					if (fileEntryPath.equals("../Common/res/lud/bad"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/bad_playout"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/subgame"))
						continue;

					gameDirs.add(fileEntry);
				}
				else
					entries.add(fileEntry);
			}
		}

		for (final File file : entries)
		{
			final String path = file.getAbsolutePath();
			final StringBuilder sb = new StringBuilder();

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8")))
			{
				String line;
				do
				{
					line = reader.readLine();
					sb.append(line);
				}
				while (line != null);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}

			final String str = sb.toString();

			int indexRuleset = str.indexOf("(ruleset ", 0);

			// Only one ruleset
			if (indexRuleset < 0)
				count++;

			while (indexRuleset >= 0)
			{
				// File contains ruleset
				while (indexRuleset < str.length() && str.charAt(indexRuleset) != '{')
					indexRuleset++;

				if (indexRuleset < str.length())
				{
					// We have a curly brace
					final int indexCurlyBrace = StringRoutines.matchingBracketAt(str, indexRuleset);
					if (indexCurlyBrace < 0)
						throw new RuntimeException("No closing '}' in ruleset: " + str);

					final String sub = str.substring(indexRuleset + 1, indexCurlyBrace);
					boolean isChar = false;
					for (int indexChar = 0; indexChar < sub.length(); indexChar++)
					{
						final char ch = sub.charAt(indexChar);
						if (StringRoutines.isTokenChar(ch) || StringRoutines.isNameChar(ch)
								|| StringRoutines.isNumeric(ch) || StringRoutines.isBracket(ch))
						{
							isChar = true;
							break;
						}
					}
					if (isChar)
						count++;
				}

				indexRuleset = str.indexOf("(ruleset ", indexRuleset);
			}
		}

		System.out.println(count + " rulesets implemented");
	}
}