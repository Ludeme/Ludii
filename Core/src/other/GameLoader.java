package other; 

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import compiler.Compiler;
import game.Game;
import game.match.Subgame;
import main.FileHandling;
import main.grammar.Description;
import main.grammar.Report;
import main.options.GameOptions;
import main.options.Option;
import main.options.Ruleset;
import main.options.UserSelections;

//-----------------------------------------------------------------------------

/**
 * Utility class for game-loading.
 * @author Dennis Soemers and cambolbro
 */
public final class GameLoader
{

	//-------------------------------------------------------------------------

	/**
	 * Load game from name.
	 * @param name Filename + .lud extension.
	 * @return Loads game for the given name
	 */
	public static Game loadGameFromName(final String name)
	{
		return loadGameFromName(name, new ArrayList<String>());
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Load game from name.
	 * @param name Filename + .lud extension.
	 * @param rulesetName Name of the ruleset to load.
	 * @return Loads game for the given name
	 */
	public static Game loadGameFromName(final String name, final String rulesetName)
	{
		if (rulesetName.length() == 0)
			return loadGameFromName(name);
		
		final Game tempGame = GameLoader.loadGameFromName(name);
		final List<Ruleset> rulesets = tempGame.description().rulesets();
		if (rulesets != null && !rulesets.isEmpty())
		{
			for (int rs = 0; rs < rulesets.size(); rs++)
				if (rulesets.get(rs).heading().equals(rulesetName))
					return loadGameFromName(name, rulesets.get(rs).optionSettings());
		}
		
		System.err.println("ERROR: Ruleset name not found, loading default game options");
		System.err.println("Game name = " + name);
		System.err.println("Ruleset name = " + rulesetName);
		return loadGameFromName(name);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Load game from name.
	 * @param name Filename + .lud extension.
	 * @param options List of options to select
	 * @return Loads game for the given name
	 */
	@SuppressWarnings("resource")
	public static Game loadGameFromName
	(
		final String name, 
		final List<String> options
	)
	{
		InputStream in = GameLoader.class.getResourceAsStream(name.startsWith("/lud/") ? name : "/lud/" + name);
		
//		System.out.println("Loading game from name, options are: " + options);
		
		if (in == null)
		{
			// exact match with full filepath under /lud/ not found; let's try
			// to see if we can figure out which game the user intended
			final String[] allGameNames = FileHandling.listGames();
			int shortestNonMatchLength = Integer.MAX_VALUE;
			String bestMatchFilepath = null;
			final String givenName = name.toLowerCase().replaceAll(Pattern.quote("\\"), "/");
			
			for (final String gameName : allGameNames)
			{
				final String str = gameName.toLowerCase().replaceAll(Pattern.quote("\\"), "/");
				
				if (str.endsWith(givenName))
				{
					final int nonMatchLength = str.length() - givenName.length();
					final String[] strSplit = str.split(Pattern.quote("/"));
					
					if (strSplit[strSplit.length - 1].equals(givenName))
					{
						// This is an exact match
						bestMatchFilepath = "..\\Common\\res\\" + gameName;
						break;
					}
					else if (nonMatchLength < shortestNonMatchLength)
					{
						shortestNonMatchLength = nonMatchLength;
						bestMatchFilepath = "..\\Common\\res\\" + gameName;
					}
				}
			}
			
			String resourceStr = bestMatchFilepath.replaceAll(Pattern.quote("\\"), "/");
			resourceStr = resourceStr.substring(resourceStr.indexOf("/lud/"));			
			in = GameLoader.class.getResourceAsStream(resourceStr);
		}
			
		final StringBuilder sb = new StringBuilder();
		try (final BufferedReader rdr = new BufferedReader(new InputStreamReader(in)))
		{
			String line;
			while ((line = rdr.readLine()) != null)
				sb.append(line + "\n");
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
				
		final Game game = (Game)Compiler.compile
						  (
							  new Description(sb.toString()), 
							  new UserSelections(options), 
							  new Report(),
							  false
						  );
		if (game.hasSubgames())
		{
			// Also need to compile instances
			for (final Subgame instance : game.instances())
			{
				final ArrayList<String> option = new ArrayList<String>();
				if (instance.optionName() != null)
					option.add(instance.optionName());

				instance.setGame(GameLoader.loadGameFromName(instance.gameName() + ".lud", option));
			}
		}
		
		//try { in.close(); } 
		//catch (final IOException e) { e.printStackTrace(); }
		
		return game;
	}

	/**
	 * @param file .lud file to load game from
	 * @return Game loaded from file
	 */
	public static Game loadGameFromFile(final File file)
	{
		return loadGameFromFile(file, new ArrayList<String>());
	}
	
	/**
	 * Load game from file (+ ruleset).
	 * @param file .lud file to load game from
	 * @param rulesetName Name of the ruleset to load.
	 * @return Loads game for the given name
	 */
	public static Game loadGameFromFile(final File file, final String rulesetName)
	{
		if (rulesetName.length() == 0)
			return loadGameFromFile(file);
		
		final Game tempGame = GameLoader.loadGameFromFile(file);
		final List<Ruleset> rulesets = tempGame.description().rulesets();
		if (rulesets != null && !rulesets.isEmpty())
		{
			for (int rs = 0; rs < rulesets.size(); rs++)
				if (rulesets.get(rs).heading().equals(rulesetName))
					return loadGameFromFile(file, rulesets.get(rs).optionSettings());
		}
		
		System.err.println("ERROR: Ruleset name not found, loading default game options");
		System.err.println("Game file = " + file.getAbsolutePath());
		System.err.println("Ruleset name = " + rulesetName);
		return loadGameFromFile(file);
	}
	
	/**
	 * @param file .lud file to load game from
	 * @param options List of options to select
	 * @return Game loaded from file
	 */
	public static Game loadGameFromFile
	(
		final File file, 
		final List<String> options
	)
	{
//		System.out.println("Loading game from file, options are: " + options);
		
		// Get game description from resource
		final StringBuilder sb = new StringBuilder();
		if (file != null)
		{
			try 
			(
				final BufferedReader rdr = new BufferedReader(
						new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))
			)
			{
				String line;
				while ((line = rdr.readLine()) != null)
					sb.append(line + "\n");
			}
			catch (final IOException e)
			{
				e.printStackTrace();
				return null;
			}
		}

		final Game game = 	(Game)Compiler.compile
							(
								new Description(sb.toString()), 
								new UserSelections(options), 
								new Report(),
								false
							);
		
		if (game.hasSubgames())
		{
			// Also need to compile instances
			for (final Subgame instance : game.instances())
			{
				final ArrayList<String> option = new ArrayList<String>();
				if (instance.optionName() != null)
					option.add(instance.optionName());

				instance.setGame(GameLoader.loadGameFromName(instance.gameName() + ".lud", option));
			}
		}
		
		return game;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param optionStrings
	 * @param gameOptions
	 * @return int array representation of selected options
	 */
	public static int[] convertStringsToOptions
	(
		final List<String> optionStrings, 
		final GameOptions gameOptions
	)
	{
		final int[] optionSelections = new int[GameOptions.MAX_OPTION_CATEGORIES];
		
		for (final String optionStr : optionStrings)
		{
			final String[] headings = optionStr.split(Pattern.quote("/"));
			boolean foundMatch = false;
			
			for (int cat = 0; cat < gameOptions.numCategories(); cat++)
			{
				final List<Option> optionsList = gameOptions.categories().get(cat).options();
				
				for (int i = 0; i < optionsList.size(); ++i)
				{
					final Option option = optionsList.get(i);
					final List<String> optionHeadings = option.menuHeadings();
					
					if (optionHeadings.size() != headings.length)
						continue;
					
					boolean allMatch = true;
					for (int j = 0; j < headings.length; ++j)
					{
						if (!headings[j].equalsIgnoreCase(optionHeadings.get(j)))
						{
							allMatch = false;
							break;
						}
					}
					
					if (allMatch)
					{
						foundMatch = true;
						optionSelections[cat] = i;
						break;
					}
				}
				
				if (foundMatch)
					break;
			}
			
			if (!foundMatch)
				System.err.println("Warning! GameLoader::convertStringToOptions() could not resolve option: " + optionStr);
		}
		
		return optionSelections;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns the complete file path for a given lud name.
	 * @param name The name of the game.
	 * @return The file path.
	 */
	public static String getFilePath(final String name)
	{
		String inName = name.replaceAll(Pattern.quote("\\"), "/");
		
		if (!inName.endsWith(".lud"))
			inName += ".lud";
		
		if (inName.startsWith("../Common/res"))
			inName = inName.substring("../Common/res".length());

		if (!inName.startsWith("/lud/"))
			inName = "/lud/" + inName;
		
		try (InputStream in = GameLoader.class.getResourceAsStream(inName))
		{
			if (in == null)
			{
				// exact match with full filepath under /lud/ not found; let's try
				// to see if we can figure out which game the user intended
				final String[] allGameNames = FileHandling.listGames();
				int shortestNonMatchLength = Integer.MAX_VALUE;
				String bestMatchFilepath = null;
				String givenName = inName.toLowerCase().replaceAll(Pattern.quote("\\"), "/");
	
				if (givenName.startsWith("/lud/"))
					givenName = givenName.substring("/lud/".length());
				else if (givenName.startsWith("lud/"))
					givenName = givenName.substring("lud/".length());
				
				for (final String gameName : allGameNames)
				{
					final String str = gameName.toLowerCase().replaceAll(Pattern.quote("\\"), "/");
					
					if (str.endsWith("/" + givenName))
					{
						final int nonMatchLength = str.length() - givenName.length();
						if (nonMatchLength < shortestNonMatchLength)
						{
							shortestNonMatchLength = nonMatchLength;
							bestMatchFilepath = "..\\Common\\res\\" + gameName;
						}
					}
				}
	
				if (bestMatchFilepath == null)
				{
					for (final String gameName : allGameNames)
					{
						final String str = gameName.toLowerCase().replaceAll(Pattern.quote("\\"), "/");
						if (str.endsWith(givenName))
						{
							final int nonMatchLength = str.length() - givenName.length();
							if (nonMatchLength < shortestNonMatchLength)
							{
								shortestNonMatchLength = nonMatchLength;
								bestMatchFilepath = "..\\Common\\res\\" + gameName;
							}
						}
					}
				}
	
				if (bestMatchFilepath == null)
				{
					final String[] givenSplit = givenName.split(Pattern.quote("/"));
					if (givenSplit.length > 1)
					{
						final String givenEnd = givenSplit[givenSplit.length - 1];
						for (final String gameName : allGameNames)
						{
							final String str = gameName.toLowerCase().replaceAll(Pattern.quote("\\"), "/");
							if (str.endsWith(givenEnd))
							{
								final int nonMatchLength = str.length() - givenName.length();
								if (nonMatchLength < shortestNonMatchLength)
								{
									shortestNonMatchLength = nonMatchLength;
									bestMatchFilepath = "..\\Common\\res\\" + gameName;
								}
							}
						}
					}
				}
				
				// Probably loading an external .lud from filepath.
				if (bestMatchFilepath == null)
					return null;
				
				String resourceStr = bestMatchFilepath.replaceAll(Pattern.quote("\\"), "/");
				resourceStr = resourceStr.substring(resourceStr.indexOf("/lud/"));
				
				return resourceStr;
			}

			return inName;
		}
		catch (final Exception e)
		{
			System.out.println("Did you change the name??");
		}
		
		return null;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * To compile the game of an instance of a match.
	 * 
	 * @param instance
	 */
	public static void compileInstance(final Subgame instance)
	{
		final ArrayList<String> option = new ArrayList<String>();
		if (instance.optionName() != null)
			option.add(instance.optionName());

		instance.setGame(GameLoader.loadGameFromName(instance.gameName() + ".lud", option));

		if (instance.optionName() != null)
		{
			final GameOptions instanceObjectOptions = new GameOptions();
			final Option optionInstance = new Option();
			final List<String> headings = new ArrayList<String>();
			headings.add(instance.optionName());
			optionInstance.setHeadings(headings);
			@SuppressWarnings("unchecked")
			final List<Option>[] optionsAvailable = new ArrayList[1];
			final ArrayList<Option> optionList = new ArrayList<Option>();
			optionList.add(optionInstance);
			optionsAvailable[0] = optionList;
			instanceObjectOptions.setOptionCategories(optionsAvailable);
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return All the analysis.
	 */
	public static List<String[]> allAnalysisGameRulesetNames()
	{
		final List<String[]> allGameRulesetNames = new ArrayList<>();
		final String[] choices = FileHandling.listGames();
		
		for (final String s : choices)
		{
			// Temporary restriction to check smaller set of games
//			if (!s.contains("/chess/"))
//				continue;
			
			if (!FileHandling.shouldIgnoreLudAnalysis(s))
			{
				final Game tempGame = GameLoader.loadGameFromName(s);
				final List<Ruleset> rulesets = tempGame.description().rulesets();
				if (rulesets != null && !rulesets.isEmpty())
				{
					for (int rs = 0; rs < rulesets.size(); rs++)
						if (!rulesets.get(rs).optionSettings().isEmpty())
							allGameRulesetNames.add(new String[] {s, rulesets.get(rs).heading()});
				}
				else
				{
					allGameRulesetNames.add(new String[] {s, ""});
				}
			}
		}
		
		return allGameRulesetNames;
	}
	
	//-------------------------------------------------------------------------

}
