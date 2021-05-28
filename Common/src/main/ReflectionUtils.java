package main;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Some useful methods for Reflection
 *
 * @author Dennis Soemers
 */
public class ReflectionUtils
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Converts an object which should be an array according to reflection, into
	 * an array.
	 * 
	 * @param array
	 * @return The given object, but as an array of Objects
	 */
	public static Object[] castArray(final Object array)
	{
		final Object[] casted = new Object[Array.getLength(array)];
		for (int i = 0; i < casted.length; ++i)
		{
			casted[i] = Array.get(array, i);
		}
		return casted;
	}
	
	/**
	 * Helper method to collect all declared fields of a class, including
	 * fields inherited from superclasses.
	 * @param clazz
	 * @return All fields of the given class (including inherited fields)
	 */
	public static List<Field> getAllFields(final Class<?> clazz)
	{
		final List<Field> fields = new ArrayList<Field>();
		fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
		
		if (clazz.getSuperclass() != null)
		{
			fields.addAll(getAllFields(clazz.getSuperclass()));
		}
		
		return fields;
	}
	
	//-------------------------------------------------------------------------

}
