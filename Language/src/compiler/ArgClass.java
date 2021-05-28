package compiler; 

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import annotations.Hide;
import compiler.exceptions.BadKeywordException;
import compiler.exceptions.BadSymbolException;
import compiler.exceptions.BadSyntaxException;
import compiler.exceptions.ListNotSupportedException;
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
 * Arg consisting of a class constructor and its arguments.
 * @author cambolbro
 */
public class ArgClass extends Arg
{
	/** List of input parameters for this ludeme class class constructor. */ 
	private final List<Arg> argsIn = new ArrayList<Arg>();
		
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param name  Symbol name.
	 * @param label Optional parameter label.
	 */
	public ArgClass(final String name, final String label)
	{
		super(name, label);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return The list of arguments in it.
	 */
	public List<Arg> argsIn()
	{
		return Collections.unmodifiableList(argsIn);
	}
		
	/**
	 * Add an element.
	 * 
	 * @param arg The new element.
	 */
	public void add(final Arg arg)
	{
		argsIn.add(arg);
	}
	
	//-------------------------------------------------------------------------

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public boolean matchSymbols(final Grammar grammar, final Report report)
	{
		//System.out.println("At matchSymbols with symbolName: \"" + symbolName + "\".");
		
		final char initial = symbolName.charAt(0);
		if (Character.isAlphabetic(initial) && !Character.isLowerCase(initial))
			throw new BadKeywordException(symbolName, "Class names should be lowercase.");
		
		for (final Arg arg : argsIn)
			arg.matchSymbols(grammar, report);

		final String name = StringRoutines.upperCaseInitial(symbolName);
		final List<Symbol> existing = grammar.symbolListFromClassName(name);
		if (existing == null)
		{
//			System.out.println("** Symbol not found from \"" + keyword + "\" (original symbolName is \"" + symbolName + "\").");
//			if (instances == null || instances.size() < 1)
//			{
//				System.out.println("** No instances!");
//			}
//			else
//			{
//				for (Instance instance : instances)
//					System.out.println("** Instance is: " + instance.cls().getName());
//			}
			throw new BadKeywordException(name, null);
		}
		
		
		// Create list of instances
		final List<Symbol> symbols = new ArrayList(existing);
		instances.clear();
		for (final Symbol symbol : symbols)
		{
			if (symbol == null) 
				throw new BadSymbolException(symbolName);
			
			final Class<?> cls = loadClass(symbol);
			if (cls == null)
			{
				// Probably tried to load class with same name as enum, ignore it. 
				// e.g. tried Mover() when it should be RoleType.Mover.
				continue;  
			}
			instances.add(new Instance(symbol, null));  //cls));
		}
		
		return true;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Load class matching each symbol.
	 */
	private static Class<?> loadClass(final Symbol symbol)
	{
		Class<?> cls = null;
		
		if (symbol.ludemeType() != LudemeType.Constant)
		{
			cls = symbol.cls();
		}
//		else
//		{
//			try { cls = Class.forName(symbol.path()); } 
//			catch (final ClassNotFoundException e)
//			{
//				// e.printStackTrace();
//			}
//		}
						
		if (cls == null)
		{
			if (symbol.ludemeType() != LudemeType.Constant)
			{
				// If constant is enum, ignore class with same name
				final Exception e = new Exception("Couldn't load ArgClass " + symbol.path() + ".");
				e.printStackTrace();
				// FIXME - should this be checked?
			}
		}
//		else
//		{
//			System.out.println("cls   : " + cls.getName() + "\nsymbol: " + symbol.path() + "\n");
//		}
		
		return cls;
	}

	//-------------------------------------------------------------------------

	@Override
	public Object compile
	(
		final Class<?> expected, final int depth, final Report report, 
		final Call callNode, final Map<String, Boolean> hasCompiled
	)
	{
		String pre = "";
		for (int n = 0; n < depth; n++)
			pre += ". ";
		pre += "C: ";

		if (depth != -1)
		{
			report.addLogLine("\n" + pre + "==========================================");
			report.addLogLine(pre + "Compiling ArgClass: " + this.symbolName);
			report.addLogLine(pre + "\n" + pre + "Expected: name=" + expected.getName() + ", type=" + expected.getTypeName() + ".");
		}
		
		if (expected.getName().contains("[L"))
			return null;  // should not be handling arrays here

		if (depth != -1)
		{
			report.addLogLine(pre + instances.size() + " instances:");
		}
		
		Call call = null;
				
		for (int inst = 0; inst < instances.size(); inst++)
		{
			final Instance instance = instances.get(inst);
			if (depth != -1)
			{
				report.addLogLine(pre + "-- instance " + inst + ": " + instance);
			}
		
			final Class<?> cls = instance.cls();	
			if (cls == null)
			{
				continue; 
			}
	
			if (expected.isArray())
			{
				final Class<?> elementType = expected.getComponentType();
				if (!elementType.isAssignableFrom(cls))
				{
					if (depth != -1)
					{
						report.addLogLine(pre + "Skipping non-assignable class " + cls.getName() + " (in array).");
					}
					continue;
				}
			}
			else if (!expected.isAssignableFrom(cls))
			{
				if (depth != -1)
				{
					report.addLogLine(pre + "Skipping non-assignable class " + cls.getName() + ".");
				}
				continue;
			}

			if (cls.getAnnotation(Hide.class) != null)
			{
				// Do not compile hidden class
				continue;
			}
						
			// Construct the object
			Object object = null;
		
			if (depth != -1)
			{
				report.addLogLine(pre + "\n" + pre + "Constructing: " + cls + "...");
			}
			
			// Ensure that an entry exist for this expected class (but beware that return type may be compiled instead).
			final String key = expected.getName();
			//System.out.println("AC key: " + key);
			if (!hasCompiled.containsKey(key))
				hasCompiled.put(key, Boolean.FALSE);
			
			// We'll first try static construct() methods, and then constructors
			for (final boolean tryConstructors : new boolean[] {false, true})
			{
				final List<Executable> executables = new ArrayList<Executable>();
				
				if (tryConstructors)
				{
					// Get list of constructors
					executables.addAll(Arrays.asList(cls.getDeclaredConstructors()));
				}
				else
				{
					// Get list of static construct() methods
					final Method[] methods = cls.getDeclaredMethods();
					for (final Method method : methods)
						if (method.getName().equals("construct") && Modifier.isStatic(method.getModifiers()))
							executables.add(method);
				}
				
				if (depth != -1)
				{
					report.addLogLine(pre + executables.size() + " constructors found.");
				}
	
				for (int c = 0; c < executables.size(); c++)
				{
					final Executable exec = executables.get(c);
					if (depth != -1)
					{
						report.addLogLine(pre + "\n" + pre + "Constructor " + c + ": " + exec.toString());
					}
		
					if (exec.getAnnotation(Hide.class) != null)
					{
						// Do not compile hidden class
						continue;
					}
					
					// Get argument types and annotations for this constructor's arguments
					Parameter[] params = null;
					Class<?>[]  types  = null;
					Annotation[][] annos = null;
					
					try 
					{
						params = exec.getParameters();
						types  = exec.getParameterTypes();
						annos  = exec.getParameterAnnotations();
					} 
					catch (final IllegalArgumentException e)  
					{
						e.printStackTrace();
					}  			
					final int numSlots = params.length;
					
					if (numSlots < argsIn.size())
					{
						if (depth != -1)
							report.addLogLine(pre + "Not enough args in constructor for " + argsIn.size() + " input args.");			
						continue;
					}
					
					if (numSlots == 0)
					{
						// No arguments to match
						try 
						{
							if (tryConstructors)
								object = ((Constructor<?>)exec).newInstance(); 
							else
								object = ((Method)exec).invoke(null, new Object[0]);
						} 
						catch (final Exception e) 
						{ 
							// Failed to compile.
							if (depth != -1)
							{
								report.addLogLine(pre + "*********************");			
								report.addLogLine(pre + "Failed to create new instance (no args).");			
								report.addLogLine(pre + "*********************\n");					
							}
							e.printStackTrace();
						}
						
						if (object != null)
						{
							// Success!
							call = new Call(CallType.Class, instance, expected);
							if (callNode != null)
								callNode.addArg(call);

							break;  // success!
						}
					}
					
					// Try to match arguments for this constructor
					
					// Get optional and named args based on annotations
					final String[] name = new String[numSlots];
					int numOptional = 0;
					
					final BitSet isOptional = new BitSet();
					
					for (int a = 0; a < numSlots; a++)
					{
						name[a] = null;  // just to be sure!
	
						if (depth != -1)
						{
							report.addLog(pre + "- con arg " + a + ": " + types[a].getName());
						}
						
						if (types[a].getName().equals("java.util.List"))
							throw new ListNotSupportedException();
						
						for (int b = 0; b < annos[a].length; b++)
						{
							if 
							(
								annos[a][b].toString().equals("@annotations.Opt()") 
								|| 
								annos[a][b].toString().equals("@annotations.Or()")
								|| 
								annos[a][b].toString().equals("@annotations.Or2()")
							)
							{
								isOptional.set(a, true);
								numOptional++;
								if (depth != -1)
									report.addLog(" [Opt] (or an Or)");
							}
							else if (annos[a][b].toString().equals("@annotations.Name()"))
							{
								name[a] = params[a].getName();
								
								if (Character.isUpperCase(name[a].charAt(0)))
								{
									// First char is capital, probably If, Else, etc.
									name[a] = Character.toLowerCase(name[a].charAt(0)) + name[a].substring(1);
								}
								
								if (depth != -1)
									report.addLog(" [name=" + name[a] + "]");
							}
						}
						if (depth != -1)
							report.addLogLine("");
					}
										
					if (argsIn.size() < numSlots - numOptional)
					{
						if (depth != -1)
							report.addLogLine(pre + "Not enough input args (" + argsIn.size() + ") for non-optional constructor args (" + (numSlots - numOptional) + ").");								
						continue;
					}
	
					// Try possible combinations of input arguments
					final Object[] argObjects = new Object[numSlots];
					
					// Try arg combinations
					final List<List<Arg>> combos = argCombos(argsIn, numSlots);
					for (int cmb = 0; cmb < combos.size(); cmb++)
					{
						final List<Arg> combo = combos.get(cmb);
					
						// Create a potential call for this combination
						call = new Call(CallType.Class, instance, expected);
						
						if (depth != -1) 
						{					
							report.addLog(pre);
							
							int count = 0;
							for (int n = 0; n < combo.size(); n++) 
							{
								final Arg arg = combo.get(n);
								
								report.addLog((arg == null ? "-" : Character.valueOf((char)('A' + count))) + " ");
								
								if (arg != null)
									count++;
							}

							report.addLogLine("");
						}
						
						// Attempt to match this combination of args
						int slot;
						
						// Quick pre-test: abort if any null argIn is not an optional parameter
						for (slot = 0; slot < numSlots; slot++)
							if (combo.get(slot) == null && !isOptional.get(slot))
								break;
					
						if (slot < numSlots)
							continue;
							
						for (slot = 0; slot < numSlots; slot++)
						{
							argObjects[slot] = null;
							final Arg argIn = combo.get(slot);
	
							if (depth != -1) 
							{
								report.addLog(pre + "argIn " + slot + ": ");
								report.addLogLine(argIn == null ? "null" : (argIn.symbolName() + " (" + argIn.getClass().getName() +")") + ".");
							}
	
							if (depth != -1) 
							{
								if (argIn != null && argIn.parameterName() != null)
									report.addLogLine(pre + "argIn has parameterName: " + argIn.parameterName());
							}
	
							if (argIn == null)
							{
								// Null placeholder for this slot
								if (!isOptional.get(slot))
									break;
								
								if (callNode != null)
								{
									// Add null placeholder arg
									call.addArg(new Call(CallType.Null));
								}
							}
							else
							{
								// This argIn must compile!			
								if (name[slot] != null && (argIn.parameterName() == null || !argIn.parameterName().equals(name[slot])))
								{
									// argIn name does not match named constructor parameter 
									if (depth != -1)
									{
										report.addLogLine(pre + "- Named arg '" + name[slot] + "' in constructor does not match argIn parameterName '" + argIn.parameterName() + "'.");
									}
									break;
								}
							
								if (argIn.parameterName() != null && (name[slot] == null || !argIn.parameterName().equals(name[slot])))
								{
									// Named argIn does not match constructor parameter name 
									if (depth != -1)
									{
										report.addLogLine(pre + "- Named argIn '" + argIn.parameterName() + "' does not match parameter constructor arg label '" + name[slot] + "'.");
									}
									break;
								}
		
								// ArgIn must match the constructor argument for this slot!
								
								// **
								// ** Check here that candidate arg can possibly match the known arg.
								// ** Small saving on time if used, but greatly reduces error log size.
								// ** Doesn't work for Omega (4-player option) and Temeen Tavag (Square option).
								// **
								
//								if (argIn.matchingInstance(types[slot]) == null)
//								{
//									//System.out.println("++ argIn " + argIn + " doesn't match type " + types[slot]);
//									break;  // the arg at this slot doesn't match the required arg 
//								}
								
								final Call callDummy = (callNode == null) ? null : new Call(CallType.Null);
								
								final Object match = argIn.compile
													 (
														 types[slot], 
														 (depth == -1 ? -1 : depth+1), 
														 report,
														 callDummy,
														 hasCompiled
													 );
								
								if (match == null)
								{
									// Can't compile argIn for this constructor parameter
									if (depth != -1)
										report.addLogLine(pre + "- Arg '" + argIn.toString() + "' doesn't match '" + types[slot] + ".");
									break;
								}
								
								// Arguments match
								argObjects[slot] = match;
								
								if (callNode != null && callDummy.args().size() > 0)
								{
									// Add a call for this argument
									final Call argCall = callDummy.args().get(0);
									if (name[slot] != null)
										argCall.setLabel(name[slot]);
									call.addArg(argCall);
								}
								
								if (depth != -1)
								{
									report.addLogLine(pre + "arg " + slot + " corresponds to " + argIn + ",");
									report.addLogLine(pre + "  returned match " + match + " for expected " + types[slot]);
								}
							}
						}
					
						if (slot >= numSlots)
						{
							// All args match, no conflicts, all slots have a valid object or are null (and optional).
							if (depth != -1)
								report.addLogLine(pre + "++ Matched all input args.");							
						
							if (depth != -1)
							{
								report.addLogLine(pre + "   Trying to create instance of " + exec.getName() + " with " + argObjects.length + " args:");							
								for (int o = 0; o < argObjects.length; o++)
									report.addLogLine(pre + "   - argObject " + o + ": " + 
											((argObjects[o] == null) ? "null" : argObjects[o].toString()));
							}
						
							try 
							{ 
								if (tryConstructors)
									object = ((Constructor<?>)exec).newInstance(argObjects); 
								else
									object = ((Method)exec).invoke(null, argObjects);
							} 
							catch (final Exception e) 
							{ 
								// Failed to compile
								report.addLogLine("***************************");
								
								e.printStackTrace();
								
								// Possibly an intialisation error, e.g. null placeholder for Integer parameter
								if (depth != -1)
								{
									report.addLogLine(pre + "\n" + pre + "*********************");
									report.addLogLine(pre + "Failed to create new instance (with args).");
								
									report.addLogLine(pre + "Expected types:");

									for (final Type type : types)
										report.addLogLine(pre + "= " + type);
								
									report.addLogLine(pre + "Actual argObjects:");
									
									for (final Object obj : argObjects)
										report.addLogLine(pre + "= " + obj);									
								
									report.addLogLine(pre + "*********************\n");
								}
							}
	
							if (object != null)
							{
								// Successfully compiled object
								if (callNode != null)
									callNode.addArg(call);
								break;  
							}
						}
					}
				}
				
				if (object != null)
					break;  // successfully compiled object
			}
			
			if (object != null)
			{
				//System.out.println("Compiled " + object.getClass().getName());
				
				if (depth != -1)
				{
					report.addLogLine(pre + "------------------------------");
					report.addLogLine(pre + "Compiled object " + object + " (key=" + key + ") successfully.");
					report.addLogLine(pre + "------------------------------");
				}
				instance.setObject(object);

				// Expected class was compiled (but possibly as return type!)
				hasCompiled.put(key, Boolean.TRUE);
				
				// Also indicate object type was compiled, to be sure.
				hasCompiled.put(object.getClass().getName(), Boolean.TRUE);
				
				//System.out.println("+ Did compile: " + key);
						
				return object;
			}
		}
		
		if (symbolName.equals("game"))
		{
			throw new BadSyntaxException("game", "Could not create \"game\" ludeme from description."); 
		}

		if (symbolName.equals("match"))
		{
			throw new BadSyntaxException("match", "Could not create a \"match\" ludeme from description."); 
		}

		return null;  // no match found
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param args
	 * @param numSlots
	 * @return List of possible arg combinations padded with nulls to give length. 
	 */
	private static List<List<Arg>> argCombos(final List<Arg> args, final int numSlots)
	{
		final List<List<Arg>> combos = new ArrayList<List<Arg>>();
				
		final Arg[] current = new Arg[numSlots];
		for (int i = 0; i < current.length; i++)
			current[i] = null;
		
		argCombos(args, numSlots, 0, 0, current, combos);

//		for (final int[] indices : Constants.combos[args.size()][numSlots])
//		{
//			final List<Arg> combo = new ArrayList<Arg>();
//			for (int n = 0; n < numSlots; n++)
//				combo.add(indices[n] == 0 ? null : args.get(indices[n]-1));
//			combos.add(combo);
//		}
		
		return combos;
	}
	
	private static void argCombos
	(
		final List<Arg> args, final int numSlots, final int numUsed, final int slot,  
		final Arg[] current, final List<List<Arg>> combos
	)
	{
		if (numUsed > args.size())
		{
			// Overshot -- too many null placeholders to allow all args to be placed
			return;
		}
		
		if (slot == numSlots) 
		{
			// All slots filled
			if (numUsed < args.size())
				return;  // all args not used
			
			// Combo completed -- store in list
			final List<Arg> combo = new ArrayList<Arg>();
			for (int n = 0; n < numSlots; n++)
				combo.add(current[n]);
			combos.add(combo);
			return;
		}
			
		if (numUsed < args.size())
		{
			// Try next arg in next slot
			current[slot] = args.get(numUsed);
			argCombos(args, numSlots, numUsed+1, slot+1, current, combos);
			current[slot] = null;
		}
		
		// Try null placeholder
		argCombos(args, numSlots, numUsed, slot+1, current, combos);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		String strT = "";
		
		if (parameterName != null)
			strT += parameterName + ":";
		
		strT += "(" + symbolName;
		if (argsIn.size() > 0)
			for (final Arg arg : argsIn)
				strT += " " + arg.toString();
		strT += ")";
		return strT;
	}
	
	//-------------------------------------------------------------------------

}
