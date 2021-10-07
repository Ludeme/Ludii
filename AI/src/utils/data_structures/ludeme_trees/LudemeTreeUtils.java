package utils.data_structures.ludeme_trees;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import main.ReflectionUtils;
import other.Ludeme;
import utils.data_structures.support.zhang_shasha.Node;
import utils.data_structures.support.zhang_shasha.Tree;

/**
 * Utils for building trees of Ludemes for AI purposes.
 *
 * @author Dennis Soemers
 */
public class LudemeTreeUtils
{
	
	/**
	 * Builds a Zhang-Shasha tree (for tree edit distance computations) for the given
	 * root ludeme. 
	 * 
	 * @param rootLudeme
	 * @return Tree that can be used for Zhang-Shasha tree edit distance computations
	 */
	public static Tree buildLudemeZhangShashaTree(final Ludeme rootLudeme)
	{
		if (rootLudeme == null)
		{
			return new Tree(new Node(""));
		}
		else
		{
			final Node root = buildTree(rootLudeme, new HashMap<Object, Set<String>>());
			return new Tree(root);
		}
	}
	
	/**
	 * Recursively builds tree for given ludeme
	 * @param ludeme Root of ludemes-subtree to traverse
	 * @param visited Map of fields we've already visited, to avoid cycles
	 * @return Root node for the subtree rooted in given ludeme
	 */
	private static Node buildTree
	(
		final Ludeme ludeme, 
		final Map<Object, Set<String>> visited
	)
	{
		final Class<? extends Ludeme> clazz = ludeme.getClass();
		final List<Field> fields = ReflectionUtils.getAllFields(clazz);
		final Node node = new Node(clazz.getName());
		
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
						final Ludeme innerLudeme = (Ludeme) value;
						final Node child = buildTree(innerLudeme, visited);
						node.children.add(child);
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
									final Ludeme innerLudeme = (Ludeme) element;
									final Node child = buildTree(innerLudeme, visited);
									node.children.add(child);
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
									final Ludeme innerLudeme = (Ludeme) element;
									final Node child = buildTree(innerLudeme, visited);
									node.children.add(child);
								}
							}
						}
					}
					else if (field.getType().isPrimitive() || String.class.isAssignableFrom(valueClass))
					{
						node.children.add(new Node(value.toString()));
					}
				}
				else
				{
					node.children.add(new Node("null"));
				}
				
				// Remove again, to avoid excessively shortening the subtree if we encounter
				// this object again later
				visited.get(ludeme).remove(field.getName());
			}
		}
		catch (final IllegalArgumentException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
		
		return node;
	}

}
