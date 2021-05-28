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
import grammar.ClassEnumerator;
import main.CommandLineArgParse;
import main.FileHandling;
import main.ReflectionUtils;
import other.GameLoader;
import other.Ludeme;

/**
 * Lists information on usage for ALL ludemes
 * 
 * @author Dennis Soemers
 */
public class ListAllLudemesUsage
{
	/**
	 * This generates the results
	 */
	public static void listAllLudemesUsage()
	{
		Class<?> clsRoot = null;
		try
		{
			clsRoot = Class.forName("game.Game");
		} 
		catch (final ClassNotFoundException e)
		{
			e.printStackTrace();
			return;
		}
		
		final List<Class<?>> classes = ClassEnumerator.getClassesForPackage(clsRoot.getPackage());
		
		final Map<String, Set<String>> ludemesToUsingGames = new HashMap<String, Set<String>>();
		for (final Class<?> clazz : classes)
		{
			if (clazz.getName().contains("$"))
				continue;  // is an internal class or enum?
			
			if (Ludeme.class.isAssignableFrom(clazz))
			{
				ludemesToUsingGames.put(clazz.getName(), new HashSet<String>());
			}
		}
		
		final String[] allGames = FileHandling.listGames();
		
		for (String gameName : allGames)
		{
			gameName = gameName.replaceAll(Pattern.quote("\\"), "/");
			final String[] gameNameParts = gameName.split(Pattern.quote("/"));
			
			boolean skipGame = false;
			for (final String part : gameNameParts)
			{
				if 
				(
					part.equals("bad") || 
					part.equals("bad_playout") || 
					part.equals("wip") || 
					part.equals("test") ||
					part.equals("wishlist")
				)
				{
					skipGame = true;
					break;
				}
			}
			
			if (skipGame)
				continue;
			
			System.out.println("Checking game: " + gameName + "...");
			
			final Game game = GameLoader.loadGameFromName(gameName);
			ludemesToUsingGames.get(Game.class.getName()).add(gameName);
			updateMap(ludemesToUsingGames, game, gameName, new HashMap<Object, Set<String>>());
		}
		System.out.println();

		final String[] ludemeNames = ludemesToUsingGames.keySet().toArray(new String[ludemesToUsingGames.keySet().size()]);
		Arrays.sort(ludemeNames);
		
		System.out.println("Usage of all ludemes:");
		
		for (final String ludemeName : ludemeNames)
		{
			final Set<String> games = ludemesToUsingGames.get(ludemeName);
			
			final StringBuilder sb = new StringBuilder();
			sb.append(ludemeName + ": ");
			
			while (sb.length() < 62)
			{
				sb.append(" ");
			}
			
			sb.append(games.size() + " games");
			
			if (games.size() > 0)
			{
				final String[] sortedGames = games.toArray(new String[games.size()]);
				Arrays.sort(sortedGames);
				
				sb.append("(");
				
				for (int i = 0; i < sortedGames.length; ++i)
				{
					final String[] nameSplit = sortedGames[i].split(Pattern.quote("/"));
					sb.append(nameSplit[nameSplit.length - 1]);
					
					if (i + 1 < sortedGames.length)
						sb.append(", ");
				}
				
				sb.append(")");
			}
			
			System.out.println(sb.toString());
		}
		
		System.out.println();
		
		int numUnusedLudemes = 0;
		System.out.println("Unused Ludemes:");
		for (final String ludemeName : ludemeNames)
		{
			final Set<String> games = ludemesToUsingGames.get(ludemeName);
			if (games.isEmpty())
			{
				System.out.println(ludemeName);
				++numUnusedLudemes;
			}
		}
		
		System.out.println();
		System.out.println("Number of ludemes used in at least 1 game: " + (ludemeNames.length - numUnusedLudemes));
	}
	
	/**
	 * Recursive method to update our map from ludemes to lists of games, 
	 * to include ludemes encountered in subtree rooted in the given ludeme.
	 * 
	 * @param ludemesToUsingGames
	 * @param ludeme
	 * @param gameName
	 * @param visited
	 */
	private static void updateMap
	(
		final Map<String, Set<String>> ludemesToUsingGames, 
		final Ludeme ludeme, 
		final String gameName,
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
				//System.out.println("Field = " + field.getName() + " of class = " + clazz.getName());
				
				if ((field.getModifiers() & Modifier.STATIC) != 0)
				{
					//System.out.println("skipping " + field.getName() + " because static");
					continue;
				}
				
				if (visited.containsKey(ludeme) && visited.get(ludeme).contains(field.getName()))
				{
					//System.out.println("skipping " + field.getName() + " because already visited");
					continue;		// avoid stack overflow
				}
				
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
						if (ludemesToUsingGames.containsKey(valueClass.getName()))
						{
							ludemesToUsingGames.get(valueClass.getName()).add(gameName);
						}
						else if 
						(
							valueClass.getDeclaringClass() != null && 
							ludemesToUsingGames.containsKey(valueClass.getDeclaringClass().getName())
						)
						{
							// An inner ludeme class, give credit to outer class ludeme
							ludemesToUsingGames.get(valueClass.getDeclaringClass().getName()).add(gameName);
						}
						else if (!valueClass.getName().contains("$"))
						{
							System.err.println("WARNING: ludeme class " + valueClass.getName() + " not in map!");
						}
						
						updateMap(ludemesToUsingGames, (Ludeme) value, gameName, visited);
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
									if (ludemesToUsingGames.containsKey(elementClass.getName()))
									{
										ludemesToUsingGames.get(elementClass.getName()).add(gameName);
									}
									else if 
									(
										elementClass.getDeclaringClass() != null && 
										ludemesToUsingGames.containsKey(elementClass.getDeclaringClass().getName())
									)
									{
										// An inner ludeme class, give credit to outer class ludeme
										ludemesToUsingGames.get(elementClass.getDeclaringClass().getName()).add(gameName);
									}
									else if (!elementClass.getName().contains("$"))
									{
										System.err.println("WARNING: ludeme class " + elementClass.getName() + " not in map!");
									}

									updateMap(ludemesToUsingGames, (Ludeme) element, gameName, visited);
								}
							}
						}
					}
					else if (Iterable.class.isAssignableFrom(valueClass))
					{
						final Iterable<?> iterable = (Iterable<?>) value;
						
						for (final Object element : iterable)
						{
							if (element != null)
							{
								final Class<?> elementClass = element.getClass();
								
								if (Ludeme.class.isAssignableFrom(elementClass))
								{
									if (ludemesToUsingGames.containsKey(elementClass.getName()))
									{
										ludemesToUsingGames.get(elementClass.getName()).add(gameName);
									}
									else if 
									(
										elementClass.getDeclaringClass() != null && 
										ludemesToUsingGames.containsKey(elementClass.getDeclaringClass().getName())
									)
									{
										// An inner ludeme class, give credit to outer class ludeme
										ludemesToUsingGames.get(elementClass.getDeclaringClass().getName()).add(gameName);
									}
									else if (!elementClass.getName().contains("$"))
									{
										System.err.println("WARNING: ludeme class " + elementClass.getName() + " not in map!");
									}
									
									updateMap(ludemesToUsingGames, (Ludeme) element, gameName, visited);
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
		final CommandLineArgParse argParse = 
				new CommandLineArgParse
				(
					true,
					"List information on usage for ALL ludemes in Ludii."
				);
		
		if (!argParse.parseArguments(args))
			return;
		
		ListAllLudemesUsage.listAllLudemesUsage();
	}

}
