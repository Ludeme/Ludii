package supplementary.experiments.ludemes;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import game.Game;
import gnu.trove.map.hash.TObjectIntHashMap;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import other.GameLoader;
import other.Ludeme;
import main.ReflectionUtils;

/**
 * Implements a function to list all the ludemes used by a
 * specific game (AFTER compilation!).
 *
 * @author Dennis Soemers
 */
public final class ListUsedLudemes
{
	/** Name of game for which to list ludemes */
	private String gameName;
	
	/** Options to tweak game (variant, rules, board, etc.) */
	private List<String> gameOptions;
	
	//-------------------------------------------------------------------------
	
	/**
	 * This generates the results
	 */
	public void listUsedLudemes()
	{
		final Game game = GameLoader.loadGameFromName(gameName, gameOptions);
		
		final TObjectIntHashMap<String> ludemeCounts = new TObjectIntHashMap<String>();
		ludemeCounts.put(Game.class.getName(), 1);
		updateLudemeCounts(ludemeCounts, game, new HashMap<Object, Set<String>>());
		
		final String[] ludemeNames = ludemeCounts.keySet().toArray(new String[0]);
		Arrays.sort(ludemeNames);
		
		System.out.println("Ludemes used by game: " + gameName);
		
		for (final String ludemeName : ludemeNames)
		{
			System.out.println(ludemeName + ": " + ludemeCounts.get(ludemeName));
		}
	}
	
	/**
	 * Recursive method to update ludeme counts in given map, to include ludemes encountered
	 * in subtree rooted in the given ludeme.
	 * 
	 * @param ludemeCounts
	 * @param ludeme
	 * @param visited
	 */
	private static void updateLudemeCounts
	(
		final TObjectIntHashMap<String> ludemeCounts, 
		final Ludeme ludeme,
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
				{
					continue;
				}
				
				if (visited.containsKey(ludeme) && visited.get(ludeme).contains(field.getName()))
				{
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
						ludemeCounts.adjustOrPutValue(valueClass.getName(), 1, 1);
						updateLudemeCounts(ludemeCounts, (Ludeme) value, visited);
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
									ludemeCounts.adjustOrPutValue(element.getClass().getName(), 1, 1);
									updateLudemeCounts(ludemeCounts, (Ludeme) element, visited);
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
									ludemeCounts.adjustOrPutValue(element.getClass().getName(), 1, 1);
									updateLudemeCounts(ludemeCounts, (Ludeme) element, visited);
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
	@SuppressWarnings("unchecked")
	public static void main(final String[] args)
	{
		// define options for arg parser
		final CommandLineArgParse argParse = 
				new CommandLineArgParse
				(
					true,
					"List all the ludemes used by a game (after compilation)."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--game")
				.help("Name of the game to inspect. Should end with \".lud\".")
				.withNumVals("1")
				.withType(OptionTypes.String)
				.setRequired());
		
		argParse.addOption(new ArgOption()
				.withNames("--game-options")
				.help("Game Options to load.")
				.withDefault(new ArrayList<String>(0))
				.withNumVals("*")
				.withType(OptionTypes.String));
		
		// parse the args
		if (!argParse.parseArguments(args))
			return;
		
		// use the parsed args
		final ListUsedLudemes func = new ListUsedLudemes();
		
		func.gameName = (String) argParse.getValue("--game");
		func.gameOptions = (List<String>) argParse.getValue("--game-options");
		
		func.listUsedLudemes();
	}

}
