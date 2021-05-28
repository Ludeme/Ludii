package supplementary.experiments.ludemes;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import game.Game;
import gnu.trove.map.hash.TObjectIntHashMap;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import other.GameLoader;
import other.Ludeme;
import main.FileHandling;
import main.ReflectionUtils;

/**
 * Implements a function to list all the games using a
 * particular ludeme (AFTER compilation!).
 * 
 * Only considers games compiled with their default options.
 *
 * @author Dennis Soemers
 */
public class ListGamesUsingLudeme
{
	/** Name of ludeme for which to list games that use it */
	private String ludemeName;
	
	//-------------------------------------------------------------------------
	
	/**
	 * This generates the results
	 */
	public void listGames()
	{
		final String[] allGames = FileHandling.listGames();
		final TObjectIntHashMap<String> gameCounts = new TObjectIntHashMap<String>();
		
		final Set<String> matchingLudemes = new HashSet<String>();
		
		for (String gameName : allGames)
		{
			gameName = gameName.replaceAll(Pattern.quote("\\"), "/");
			final String[] gameNameParts = gameName.split(Pattern.quote("/"));
			
			boolean skipGame = false;
			for (final String part : gameNameParts)
			{
				if (part.equals("bad") || part.equals("bad_playout") || part.equals("wip") || part.equals("test"))
				{
					skipGame = true;
					break;
				}
			}
			
			if (skipGame)
				continue;
			
			final Game game = GameLoader.loadGameFromName(gameName);
			updateGameCounts(gameCounts, matchingLudemes, game, gameName, ludemeName, new HashMap<Object, Set<String>>());
		}
		
		if (matchingLudemes.size() > 1)
		{
			System.err.println("Warning! Target ludeme name is ambiguous. Included ludemes:");
			
			for (final String name : matchingLudemes)
			{
				System.err.println(name);
			}
			
			System.err.println("");
		}

		final String[] gameNames = gameCounts.keySet().toArray(new String[gameCounts.keySet().size()]);
		Arrays.sort(gameNames);
		
		System.out.println("Games using ludeme: " + ludemeName);
		
		for (final String gameName : gameNames)
		{
			System.out.println(gameName + ": " + gameCounts.get(gameName));
		}
	}
	
	/**
	 * Recursive method to update game counts in given map, to include ludemes encountered
	 * in subtree rooted in the given ludeme.
	 * 
	 * @param gameCounts
	 * @param matchingLudemes
	 * @param ludeme
	 * @param gameName
	 * @param targetLudemeName
	 */
	private static void updateGameCounts
	(
		final TObjectIntHashMap<String> gameCounts, 
		final Set<String> matchingLudemes,
		final Ludeme ludeme, 
		final String gameName,
		final String targetLudemeName,
		final Map<Object, Set<String>> visited
	)
	{
		final Class<? extends Ludeme> clazz = ludeme.getClass();
		final List<Field> fields = ReflectionUtils.getAllFields(clazz);
		
		try
		{
			for (final Field field : fields)
			{
				field.setAccessible(true);
				
				if ((field.getModifiers() & Modifier.STATIC) != 0)
					continue;
				
				if (visited.containsKey(ludeme) && visited.get(ludeme).contains(field.getName()))
					continue;		// avoid stack overflow
				
				final Object value = field.get(ludeme);
				
				if (!visited.containsKey(ludeme))
					visited.put(ludeme, new HashSet<String>());
				
				visited.get(ludeme).add(field.getName());
				
				if (value != null)
				{
					final Class<?> valueClass = value.getClass();
					
					if (Ludeme.class.isAssignableFrom(valueClass))
					{
						// We've found a ludeme!
						boolean matchesTargetLudeme = false;
						
						if (valueClass.getName().endsWith(targetLudemeName))
						{
							matchesTargetLudeme = true;
						}
						else if (valueClass.getDeclaringClass() != null)
						{
							if (valueClass.getDeclaringClass().getName().endsWith(targetLudemeName))
								matchesTargetLudeme = true;
						}
						
						if (matchesTargetLudeme)
						{
							matchingLudemes.add(valueClass.getName());
							gameCounts.adjustOrPutValue(gameName, 1, 1);
						}
						
						//System.out.println("recursing into " + field.getName() + " of " + clazz.getName());
						updateGameCounts(gameCounts, matchingLudemes, (Ludeme) value, gameName, targetLudemeName, visited);
					}
					else if (valueClass.isArray())
					{
						final Object[] array = ReflectionUtils.castArray(value);
						
						for (final Object element : array)
						{
							if (element != null)
							{
								final Class<?> elementClass = element.getClass();
								
								if (Ludeme.class.isAssignableFrom(elementClass))
								{
									boolean matchesTargetLudeme = false;
							
									if (elementClass.getName().endsWith(targetLudemeName))
									{
										matchesTargetLudeme = true;
									}
									else if (elementClass.getDeclaringClass() != null)
									{
										if (elementClass.getDeclaringClass().getName().endsWith(targetLudemeName))
											matchesTargetLudeme = true;
									}
									
									if (matchesTargetLudeme)
									{
										matchingLudemes.add(elementClass.getName());
										gameCounts.adjustOrPutValue(gameName, 1, 1);
									}
									
									updateGameCounts(gameCounts, matchingLudemes, (Ludeme) element, gameName, targetLudemeName, visited);
								}
							}
						}
					}
					else if (List.class.isAssignableFrom(valueClass))
					{
						final List<?> list = (List<?>) value;
						
						for (final Object element : list)
						{
							if (element != null)
							{
								final Class<?> elementClass = element.getClass();
								
								if (Ludeme.class.isAssignableFrom(elementClass))
								{
									// We've found a ludeme!
									boolean matchesTargetLudeme = false;
									
									if (elementClass.getName().endsWith(targetLudemeName))
									{
										matchesTargetLudeme = true;
									}
									else if (elementClass.getDeclaringClass() != null)
									{
										if (elementClass.getDeclaringClass().getName().endsWith(targetLudemeName))
											matchesTargetLudeme = true;
									}
									
									if (matchesTargetLudeme)
									{
										matchingLudemes.add(elementClass.getName());
										gameCounts.adjustOrPutValue(gameName, 1, 1);
									}
									
									updateGameCounts(gameCounts, matchingLudemes, (Ludeme) element, gameName, targetLudemeName, visited);
								}
							}
						}
					}
				}
			}
		}
		catch (final IllegalArgumentException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Main method
	 * @param args
	 */
	public static void main(final String[] args)
	{
		// define options for arg parser
		final CommandLineArgParse argParse = 
				new CommandLineArgParse
				(
					true,
					"List all the games that use a particular ludeme (after compilation)."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--ludeme")
				.help("Name of the ludeme for which to find users.")
				.withNumVals("1")
				.withType(OptionTypes.String)
				.setRequired());
		
		// parse the args
		if (!argParse.parseArguments(args))
			return;
		
		// use the parsed args
		final ListGamesUsingLudeme func = new ListGamesUsingLudeme();
		
		func.ludemeName = (String) argParse.getValue("--ludeme");		
		func.listGames();
	}

}
