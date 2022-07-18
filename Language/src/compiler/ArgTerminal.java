package compiler; 

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import compiler.exceptions.TerminalNotFoundException;
//import game.functions.booleans.BooleanConstant;
//import game.functions.dim.DimConstant;
//import game.functions.floats.FloatConstant;
//import game.functions.ints.IntConstant;
import grammar.Grammar;
import main.StringRoutines;
import main.grammar.Call;
import main.grammar.Call.CallType;
import main.grammar.Instance;
import main.grammar.Report;
import main.grammar.Symbol;
import main.grammar.Symbol.LudemeType;

//-----------------------------------------------------------------------------

/**
 * Token argument in constructor argument list. The token may be a: 
 * 1. String: in which case "str" will have surrounding quotes. 
 * 2. Integer: in which case it will satisfy Global.isInteger(str). 
 * 3. Boolean: in which case it will be "true" or "false". 
 * 4. Enum constant: in which case str will have at least one matching symbol in the grammar.
 * @author cambolbro
 */
public class ArgTerminal extends Arg
{	
	//-------------------------------------------------------------------------

	/**
	 * Constructor
	 * @param name  Symbol name.
	 * @param label Optional parameter label.
	 */
	public ArgTerminal(final String name, final String label)
	{
		super(name, label);
	}
	
	//-------------------------------------------------------------------------

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public boolean matchSymbols(final Grammar grammar, final Report report)
	{
		Object object = null;
		Symbol symbol = null;
		
		final String className = StringRoutines.upperCaseInitial(symbolName);
		final List<Symbol> match = grammar.symbolsByName(className);
		final List<Symbol> symbols = (match == null) ? null : new ArrayList(match);

		instances.clear();

		if (symbols == null || Grammar.applicationConstantIndex(symbolName) != -1)
		{
			// Check if is a known type
			if 
			(
				symbolName.length() >= 2 
				&& 
				symbolName.charAt(0) == '\"'
				&& 
				symbolName.charAt(symbolName.length() - 1) == '\"'
			)
			{
				// Is a String
				symbol = grammar.symbolsByName("String").get(0);
//				System.out.println("* arg " + symbolName + " has " + instances.size() + " matches (String).");

				String str = symbolName;
				while (str.contains("\""))
					str = str.replace("\"", "");

				object = new String(str);
				instances.add(new Instance(symbol, object));
			}
			else if 
			(
				StringRoutines.isInteger(symbolName) 
				|| 
				Grammar.applicationConstantIndex(symbolName) != -1
			)
			{
				// Is an integer
				int value;
				
				final int acIndex = Grammar.applicationConstantIndex(symbolName);
				final String valueName = (acIndex == -1) ? symbolName : Grammar.ApplicationConstants[acIndex][3];
				
				final String constantName = (acIndex == -1) ? null : symbolName;
				
				try
				{
					value = Integer.parseInt(valueName);
				}
				catch (NumberFormatException e)
				{
					e.printStackTrace();
					return false;
				}
				catch (NullPointerException e)
				{
					e.printStackTrace();
					return false;
				}
	
				// 1. IntConstant version
				//object = new IntConstant(value);
				try
				{
					// Load class from Core (is outside build path)
					object = Class.forName("game.functions.ints.IntConstant")
							 .getDeclaredConstructor(int.class)
							 .newInstance(Integer.valueOf(value));
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				} 
				
				symbol = grammar.symbolsByName("IntConstant").get(0);
				instances.add(new Instance(symbol, object, constantName));	
				
				// 1a. DimConstant version
				//object = new DimConstant(value);
				try
				{
					// Load class from Core (is outside build path)
					object = Class.forName("game.functions.dim.DimConstant")
							 .getDeclaredConstructor(int.class)
							 .newInstance(Integer.valueOf(value));
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				} 

				symbol = grammar.symbolsByName("DimConstant").get(0);
				instances.add(new Instance(symbol, object, constantName));

				// 2. Integer version
				symbol = grammar.symbolsByName("Integer").get(0);
				object = Integer.valueOf(value);
				instances.add(new Instance(symbol, object, constantName));

				// 3. Primitive int version
				symbol = grammar.symbolsByName("int").get(0);
				object = Integer.valueOf(value);
				instances.add(new Instance(symbol, object, constantName));

//				System.out.println("* arg " + symbolName + " has " + instances.size() + " matches (value=" + value + ").");
			}
			else if (symbolName.equalsIgnoreCase("true") || symbolName.equalsIgnoreCase("false"))
			{
				// Is a boolean
				boolean value = symbolName.equalsIgnoreCase("true");

				// 1. BooleanConstant version
				//object = BooleanConstant.construct(value);
				try
				{
					// Load class from Core (is outside build path)
					object = Class.forName("game.functions.booleans.BooleanConstant")
							 .getDeclaredConstructor(boolean.class)
							 .newInstance(Boolean.valueOf(value));
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				} 

				symbol = grammar.symbolsByName("BooleanConstant").get(0);
				instances.add(new Instance(symbol, object));

				// 2. Boolean version
				symbol = grammar.symbolsByName("Boolean").get(0);
				object = Boolean.valueOf(value);
				instances.add(new Instance(symbol, object));

				// 3. Primitive boolean version
				symbol = grammar.symbolsByName("boolean").get(0); // null;
				object = Boolean.valueOf(value);
				instances.add(new Instance(symbol, object));

//				System.out.println("* arg " + symbolName + " has " + instances.size() + " matches (value=" + value + ").");
			}
			// 2. Check if is a predefined data type?
			// 3. Check if is a named item?
			// 4. Check if is a named ludeme?
			else
			{
				// Can't match it with anything
				// symbols = grammar.symbolMap().get("String");
//				System.out.println("!! arg " + symbolName + " has no matches.");
			}
			
			if 
			(
				StringRoutines.isFloat(symbolName) 
			)
			{
				// Is a float
				float value;
									
				final String valueName = symbolName;
						
				try
				{
					value = Float.parseFloat(valueName);
				}
				catch (NumberFormatException e)
				{
					e.printStackTrace();
					return false;
				}
				catch (NullPointerException e)
				{
					e.printStackTrace();
					return false;
				}
	
				// 1. FloatConstant version
				//object = new FloatConstant(value);
				try
				{
					// Load class from Core (is outside build path)
					object = Class.forName("game.functions.floats.FloatConstant")
							 .getDeclaredConstructor(float.class)
							 .newInstance(Float.valueOf(value));
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				} 

				
				symbol = grammar.symbolsByName("FloatConstant").get(0);
				instances.add(new Instance(symbol, object));

				// 2. Float version
				symbol = grammar.symbolsByName("Float").get(0);
				object = Float.valueOf(value);
				instances.add(new Instance(symbol, object));

				// 3. Primitive float version
				symbol = grammar.symbolsByName("float").get(0);
				object = Float.valueOf(value);
				instances.add(new Instance(symbol, object));
				
//				System.out.println("* arg " + symbolName + " has " + instances.size() + " matches (value=" + value + ").");
			}
		}
		else
		{
			// At least one matching symbol
			for (final Symbol sym : symbols)
			{
				if (sym.ludemeType() == LudemeType.Constant)
				{
					// Is probably an enum
					final Class<?> cls = sym.cls();
					
					// FIXME: Add placeholder object to match number of entries in lists?
					// Or add object?
					if (cls == null)
					{
						System.out.println("** ArgTerminal: null cls, symbolName=" + symbolName + ", parameterName=" + parameterName);
						report.addLogLine("** ArgTerminal: null cls, symbolName=" + symbolName + ", parameterName=" + parameterName);
					}
					
					Object[] enums = cls.getEnumConstants();
					if (enums != null && enums.length > 0)
					{
						for (Object obj : enums)
						{
//							System.out.println("  obj: " + obj);
							if (obj.toString().equals(sym.token()))
							{
								final Instance instance = new Instance(sym, obj);
								instances.add(instance);
							}
						}
					}

//					if (instance.cls() != null)
//						break; // success!
				}
				else
				{
					// FIXME: Check this. Should these be set by their relevant class?
				}
			}
		}

		if (instances.size() == 0)
		{
//			System.out.println("!! arg " + symbolName + " has no instance after matching symbols.");
			return false;
		}

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
		final String key = expected.getName() + " (terminal)";
		if (!hasCompiled.containsKey(key))
			hasCompiled.put(key, Boolean.FALSE);

		String pre = "";
		for (int n = 0; n < depth; n++)
			pre += ". ";
		pre += "T: ";

		if (depth != -1)
		{
			//System.out.println("\n" + pre + "Compiling ArgTerminal: " + toString());
			//System.out.println(pre + "Trying expected type: " + expected);
			report.addLogLine("\n" + pre + "Compiling ArgTerminal: " + toString());
			report.addLogLine(pre + "Trying expected type: " + expected);
		}

		if (depth != -1)
		{
			for (Instance instance : instances)
			{
				final Symbol symbol = instance.symbol();
				//System.out.println(pre + "T: > " + symbol + " (" + symbol.path() + ") " + symbol.keyword() + ".");
				report.addLogLine(pre + "T: > " + symbol + " (" + symbol.path() + ") " + symbol.token() + ".");
			}
		}

		if (depth != -1)
		{
			//System.out.println(pre + "Instances:");
			report.addLogLine(pre + "Instances:");
		}
		
		for (int n = 0; n < instances.size(); n++)
		{
			final Instance instance = instances.get(n);
			if (depth != -1)
			{
//				System.out.println
//				(
//					pre + "\n" + pre + 
//					"Instance " + n + " is " + instance.symbol().grammarLabel() + 
//					": symbol=" + instance.symbol() + 
//					" (path=" + instance.symbol().path() + ")."
//				);
				report.addLogLine
				(
					pre + "\n" + pre + 
					"Instance " + n + " is " + instance.symbol().grammarLabel() + 
					": symbol=" + instance.symbol() + 
					" (path=" + instance.symbol().path() + ")."
				);
			}
			
			final Class<?> cls = instance.cls();

			if (depth != -1)
			{
				//System.out.println(pre + "- cls is: " + (cls == null ? "null" : cls.getName()));
				report.addLogLine(pre + "- cls is: " + (cls == null ? "null" : cls.getName()));
			}

			if (cls == null)
			{
				//System.out.println(pre + "- unexpected null cls.");
				report.addLogLine(pre + "- unexpected null cls.");
				
				throw new TerminalNotFoundException(expected.getName());
			}

			if (expected.isAssignableFrom(cls))
			{
				//System.out.println("Terminal match: " + instance.object());
				
				if (depth != -1)
				{
					//System.out.println(pre + "+ MATCH! Returning object " + instance.object());
					report.addLogLine(pre + "+ MATCH! Returning object " + instance.object());
				}
				
				if (callNode != null)
				{
					// Create a terminal object Call for this item
					final Call call = new Call(CallType.Terminal, instance, expected);
					callNode.addArg(call);
				}
				
				hasCompiled.put(key, Boolean.TRUE);
				
				return instance.object();
				//final List<Object> objects = new ArrayList<Object>();
				//objects.add(instance.object());
				//return objects;
			}
		}
		if (depth != -1)
		{
			//System.out.println(pre + "\n" + pre + "* Failed to compile ArgTerminal: " + toString());
			report.addLogLine(pre + "\n" + pre + "* Failed to compile ArgTerminal: " + toString());
		}

		//throw new TerminalNotFoundException(expected.getName());
		
		return null;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return (parameterName == null ? "" : parameterName + ":") + symbolName;
	}

	//-------------------------------------------------------------------------

}
