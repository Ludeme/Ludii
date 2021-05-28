package compiler; 

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import compiler.exceptions.CompilerException;
import compiler.exceptions.UnknownArrayErrorException;
import grammar.Grammar;
import main.grammar.Call;
import main.grammar.Report;
import main.grammar.Call.CallType;

//-----------------------------------------------------------------------------

/**
 * Argument in constructor consisting of a list of arguments (of the same type).
 * @author cambolbro
 */
public class ArgArray extends Arg
{
	/**
	 * Elements will probably be constructors, 
	 * but could possibly be a nested array. 
	 */
	private final List<Arg> elements = new ArrayList<Arg>();
		
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param name  The name of the argument.
	 * @param label the label of the argument.
	 */
	public ArgArray(final String name, final String label)
	{
		super(name, label);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The elements.
	 */
	public List<Arg> elements()
	{
		return Collections.unmodifiableList(elements);
	}

	/**
	 * Add an element.
	 * 
	 * @param arg The num element.
	 */
	public void add(final Arg arg)
	{
		elements.add(arg);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean matchSymbols(final Grammar grammar, final Report report)
	{	
		for (final Arg arg : elements)
			if (!arg.matchSymbols(grammar, report))
				return false;
		
		return true;
	}

	//-------------------------------------------------------------------------

	@Override
	public Object compile
	(
		final Class<?> expected, final int depth, final Report report, 
		final Call callNode, final Map<String, Boolean> hasCompiled
	)
	{
		final String key = "Array of " + expected.getName();
		if (!hasCompiled.containsKey(key))
			hasCompiled.put(key, Boolean.FALSE);
		
		String pre = "";
		for (int n = 0; n < depth; n++)
			pre += ". ";
		pre += "[]: ";

		// Create an empty array Call for this item
		final Call call = (callNode == null) ? null : new Call(CallType.Array);
		
		if (depth != -1)
		{
			//System.out.println("\n" + pre + "[][][][][][][][][][][][][][][][][][][][][][][][][][]");
			//System.out.println(pre + "Compiling ArgArray (expected=" + expected.getName() + "):");
			report.addLogLine("\n" + pre + "[][][][][][][][][][][][][][][][][][][][][][][][][][]");
			report.addLogLine(pre + "Compiling ArgArray (expected=" + expected.getName() + "):");
		}
		
		final Class<?> elementType = expected.getComponentType();
		if (depth != -1)
		{
			//System.out.println(pre + "Element type is: " + elementType);
			report.addLogLine(pre + "Element type is: " + elementType);
		}
		
		if (elementType == null)
		{
			//throw new ArrayTypeNotFoundException(expected.getName());
			return null;
		}
		
		// Create array of parameterised type.
		// From: https://docs.oracle.com/javase/tutorial/reflect/special/arrayInstance.html
		Object objs = null;
		try 
		{
			objs = Array.newInstance(elementType, elements.size());
           	for (int i = 0; i < elements.size(); i++)
           	{
           		final Arg elem = elements.get(i);
           		final Object match = elem.compile
           							 (
           								 elementType, 
           								 (depth == -1 ? -1 : depth+1), 
           								 report, 
           								 call,
           								 hasCompiled
           							 );
           		if (match == null)
           		{
           			// No match
           			return null;
           		}
           		Array.set(objs, i, match);
           	}
		}
		catch (final CompilerException e)
		{
			throw e;
		}
		catch(final Exception e)
		{
			e.printStackTrace();
			throw new UnknownArrayErrorException(expected.getName(), e.getMessage());
		}
        
		final Object[] array = (Object[])objs;
		if (depth != -1)
		{
			//System.out.println(pre + "+ Array okay, " + array.length + " elements matched.");
			report.addLogLine(pre + "+ Array okay, " + array.length + " elements matched.");
		}
		
		if (callNode != null)
			callNode.addArg(call);

		hasCompiled.put(key, Boolean.TRUE);
		
		return array;
		//final List<Object> objects = new ArrayList<Object>();
		//objects.add(array);
		//return objects;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		String str = "";		
		str += "{ ";
		for (int a = 0; a < elements.size(); a++)
		{
			final Arg arg = elements.get(a);
			str += arg.toString() + " ";
		}
		str += "}";
		return str;
	}

	//-------------------------------------------------------------------------

}
