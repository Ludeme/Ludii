package game.util.optimiser;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import game.Game;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.booleans.BooleanConstant;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntConstant;
import game.functions.region.BaseRegionFunction;
import game.functions.region.RegionConstant;
import main.ReflectionUtils;
import other.Ludeme;
import other.context.Context;
import other.trial.Trial;

/**
 * Optimiser which can optimise a compiled game by injecting more efficient ludemes.
 *
 * @author Dennis Soemers
 */
public class Optimiser
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Optimises the given (compiled) game object.
	 * @param game
	 */
	public static void optimiseGame(final Game game)
	{
		final Context dummyContext = new Context(game, new Trial(game));
		optimiseLudeme(game, dummyContext, new HashMap<Object, Set<String>>());
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Optimises the subtree rooted in the given ludeme
	 * @param ludeme
	 * @param dummyContext
	 * @param visited Map of fields we've already visited, to avoid cycles
	 */
	private static void optimiseLudeme
	(
		final Ludeme ludeme, final Context dummyContext, final Map<Object, Set<String>> visited
	)
	{
		final Class<? extends Ludeme> clazz = ludeme.getClass();
		final List<Field> fields = ReflectionUtils.getAllFields(clazz);
		
		try
		{
			for (final Field field : fields)
			{
				if (field.getName().contains("$"))
					continue;
				
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
					
					if (Enum.class.isAssignableFrom(valueClass))
						continue;

					if (Ludeme.class.isAssignableFrom(valueClass))
					{
						boolean recurse = true;
						
						if (BaseRegionFunction.class.isAssignableFrom(valueClass))
						{
							final BaseRegionFunction baseRegion = (BaseRegionFunction) value;
							if (baseRegion.isStatic())
							{
								final RegionConstant constReg = new RegionConstant(baseRegion.eval(dummyContext));
								injectLudeme(ludeme, constReg, baseRegion, new HashSet<Object>());
								recurse = false;
							}
						}
						else if (BaseIntFunction.class.isAssignableFrom(valueClass))
						{
							final BaseIntFunction baseInt = (BaseIntFunction) value;
							if (baseInt.isStatic())
							{
								final IntConstant constInt = new IntConstant(baseInt.eval(dummyContext));
								injectLudeme(ludeme, constInt, baseInt, new HashSet<Object>());
								recurse = false;
							}
						}
						else if (BaseBooleanFunction.class.isAssignableFrom(valueClass))
						{
							final BaseBooleanFunction baseBool = (BaseBooleanFunction) value;
							if (baseBool.isStatic())
							{
								final BooleanConstant constBool = new BooleanConstant(baseBool.eval(dummyContext));
								injectLudeme(ludeme, constBool, baseBool, new HashSet<Object>());
								recurse = false;
							}
						}
						
						if (recurse)
							optimiseLudeme((Ludeme) value, dummyContext, visited);
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
									boolean recurse = true;
						
									if (BaseRegionFunction.class.isAssignableFrom(elementClass))
									{
										final BaseRegionFunction baseRegion = (BaseRegionFunction) element;
										if (baseRegion.isStatic())
										{
											final RegionConstant constReg = new RegionConstant(baseRegion.eval(dummyContext));
											injectLudeme(ludeme, constReg, baseRegion, new HashSet<Object>());
											recurse = false;
										}
									}
									
									if (recurse)
										optimiseLudeme((Ludeme) element, dummyContext, visited);
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
									boolean recurse = true;
						
									if (BaseRegionFunction.class.isAssignableFrom(elementClass))
									{
										final BaseRegionFunction baseRegion = (BaseRegionFunction) element;
										if (baseRegion.isStatic())
										{
											final RegionConstant constReg = new RegionConstant(baseRegion.eval(dummyContext));
											injectLudeme(ludeme, constReg, baseRegion, new HashSet<Object>());
											recurse = false;
										}
									}
									
									if (recurse)
										optimiseLudeme((Ludeme) element, dummyContext, visited);
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
	 * Injects the given new ludeme such that it replaces the original ludeme 
	 * if the original ludeme is a field of the given parent ludeme
	 * 
	 * @param parentObject
	 * @param newLudeme
	 * @param origLudeme
	 * @param inspectedParentObjects Set of parent objects we already looked at (avoid cycles)
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private static void injectLudeme
	(
		final Object parentObject, 
		final Ludeme newLudeme, 
		final Ludeme origLudeme,
		final Set<Object> inspectedParentObjects
	) throws IllegalArgumentException, IllegalAccessException
	{
		if (inspectedParentObjects.contains(parentObject))
			return;
		
		inspectedParentObjects.add(parentObject);
		//System.out.println("injecting " + newLudeme + " as replacement for " + origLudeme + " in " + parentObject);
		
		if (parentObject.getClass().isArray())
		{
			final int arrLength = Array.getLength(parentObject);
			for (int i = 0; i < arrLength; ++i)
			{
				final Object obj = Array.get(parentObject, i);
				if (obj == null)
					continue;

				if (obj.getClass().isArray() || Iterable.class.isAssignableFrom(obj.getClass()))
					injectLudeme(obj, newLudeme, origLudeme, inspectedParentObjects);
				else if (obj == origLudeme)
					Array.set(parentObject, i, newLudeme);
			}
		}
		else 
		{
			final List<Field> parentFields = ReflectionUtils.getAllFields(parentObject.getClass());
			for (final Field field : parentFields)
			{
				if (field.getName().contains("$"))
					continue;
				
				field.setAccessible(true);
				
				if ((field.getModifiers() & Modifier.STATIC) != 0)
					continue;
				
				final Object fieldVal = field.get(parentObject);
				if (fieldVal == null)
					continue;
				
				if (fieldVal.getClass().isArray() || Iterable.class.isAssignableFrom(fieldVal.getClass()))
					injectLudeme(fieldVal, newLudeme, origLudeme, inspectedParentObjects);
				else if (fieldVal == origLudeme)
					field.set(parentObject, newLudeme);
			}
		}
	}
	
	//-------------------------------------------------------------------------

}
