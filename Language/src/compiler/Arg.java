package compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import annotations.Hide;
import grammar.Grammar;
import main.grammar.Call;
import main.grammar.Instance;
import main.grammar.Report;
import main.grammar.Token;

//-----------------------------------------------------------------------------

/**
 * Constructor argument read in from file, in compilable format. 
 * Could be a constructor, terminal (String, enum, etc.) or list of constructors.  
 * @author cambolbro
 */
public abstract class Arg
{
	// Name of the symbol
	protected String symbolName = null;
	
	// Optional parameter name
	protected String parameterName = null;
	
	// Possible instances of this symbol that might be instantiated
	protected final List<Instance> instances = new ArrayList<Instance>();
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * 
	 * @param symbolName    Symbol name.
	 * @param parameterName Optional parameter label.
	 */
	public Arg(final String symbolName, final String parameterName)
	{
		this.symbolName    = (symbolName == null)    ? null : new String(symbolName);
		this.parameterName = (parameterName == null) ? null : new String(parameterName);
		
		//System.out.println("+ Constructed symbolName \"" + this.symbolName + "\", label=\"" + this.label + "\".");
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The name of the symbol.
	 */
	public String symbolName()
	{
		return symbolName;
	}

	/**
	 * @return The name of the parameter.
	 */
	public String parameterName()
	{
		return parameterName;
	}
	
	/**
	 * @return The instances.
	 */
	public List<Instance> instances()
	{
		return Collections.unmodifiableList(instances);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Factory method to generate appropriate subclass for this part.
	 * 
	 * @param grammar The grammar.
	 * @param token   The token.
	 * @return Appropriate Arg subclass for this part.
	 */
	public static Arg createFromToken(final Grammar grammar, final Token token)
	{
		Arg arg = null;
		
		//System.out.println("+ Creating Arg for token: " + token);
		
		switch (token.type())
		{
		case Terminal:
			return new ArgTerminal(token.name(), token.parameterLabel());
		case Class:
			arg = new ArgClass(token.name(), token.parameterLabel());
			for (final Token sub : token.arguments())
//				((ArgClass)arg).argsIn().add(createFromToken(grammar, sub));
				((ArgClass)arg).add(createFromToken(grammar, sub));
			break;
		case Array:
			arg = new ArgArray(token.name(), token.parameterLabel());
			for (final Token sub : token.arguments())
//				((ArgArray)arg).elements().add(createFromToken(grammar, sub));
				((ArgArray)arg).add(createFromToken(grammar, sub));
			break;
		default:
			return null;
		}
		
		return arg;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param grammar The grammar.
	 * @param report  The report.
	 * @return Whether was able to match all symbols in Arg tree.
	 */
	public abstract boolean matchSymbols(final Grammar grammar, final Report report);

	/**
	 * @param expected Class type expected for this argument.
	 * @param depth Depth of recursive nesting (for formatting console display messages).
	 * @param report 
	 * @param callNode Call node for this object's entry in the call tree.
	 * @param hasCompiled Record of whether items have compiled or not at least once in some context.
	 * @return The compiled object(s) based on this argument and its parameters, else null if none.
	 */
	public abstract Object compile
	(
		final Class<?> expected, final int depth, final Report report, 
		final Call callNode, final Map<String, Boolean> hasCompiled
	);
		
	//-------------------------------------------------------------------------

	/**
	 * @param expected Expected class to check.
	 * @return Whether the expected class has a matching instance.
	 */
	public Instance matchingInstance(final Class<?> expected)
	{
		if (this instanceof ArgArray)
		{
			// Is an array
			final List<Arg> elements = ((ArgArray)this).elements();
			if (elements == null || elements.isEmpty())
				return null;
			
			if 
			(
				elements.get(0) instanceof ArgArray 
				&& 
				((ArgArray)elements.get(0)).elements() != null
				&& 
				!((ArgArray)elements.get(0)).elements().isEmpty()
				&& 
				((ArgArray)elements.get(0)).elements().get(0) instanceof ArgArray
			)
			{
				// Doubly nested array
				final Class<?> componentType = expected.getComponentType().getComponentType().getComponentType();
				if (componentType == null)
					return null;
							
				for (final Arg element : elements)
					for (final Arg element2 : ((ArgArray)element).elements())
						for (final Arg element3 : ((ArgArray)element2).elements())
							for (final Instance instance : element3.instances())
							{
								final Class<?> elementType   = instance.cls();		
								//System.out.println("+ ArgArray elementType is " + elementType + ", componentType is " + componentType);
									
								if (!componentType.isAssignableFrom(elementType))
									continue;
							
								return instance;
							}
			} 
			else if (elements.get(0) instanceof ArgArray)
			{
				// Singly nested array
				if (expected.getComponentType() == null)
					return null;
				
				final Class<?> componentType = expected.getComponentType().getComponentType();
				if (componentType == null)
					return null;
							
				for (final Arg element : elements)
					for (final Arg element2 : ((ArgArray)element).elements())
					{
						if (element2 == null)
							continue;
						for (final Instance instance : element2.instances())
						{
							final Class<?> elementType   = instance.cls();		
							//System.out.println("+ ArgArray elementType is " + elementType + ", componentType is " + componentType);
								
							if (!componentType.isAssignableFrom(elementType))
								continue;
						
							return instance;
						}
					}
			}
			else
			{
				// Flat array
				final Class<?> componentType = expected.getComponentType();
				if (componentType == null)
					return null;

				for (final Arg element : elements)
					for (final Instance instance : element.instances())
					{
						final Class<?> elementType   = instance.cls();												
						//System.out.println("+ ArgArray elementType is " + elementType + ", componentType is " + componentType);
							
						if (!componentType.isAssignableFrom(elementType))
							continue;
					
						return instance;
					}
			}
		}
		else
		{
			// Check instances
			for (int inst = 0; inst < instances.size(); inst++)
			{
				final Instance instance = instances.get(inst);
			
				final Class<?> cls = instance.cls();	
				
				if (cls == null)
					continue; 
						
				if (!expected.isAssignableFrom(cls))
					continue;
				
				if (cls.getAnnotation(Hide.class) != null)
					continue;
	
				return instance;  // matching instance found
			}
		}
		
		return null;  // no matching instance
	}

	//-------------------------------------------------------------------------

}
