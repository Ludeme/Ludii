package main;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for parsing of command line arguments.
 * 
 * Functionality loosely based on the argparse module of Python 3.
 * 
 * @author Dennis Soemers
 */
public final class CommandLineArgParse
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Types of options we may have
	 * 
	 * @author Dennis Soemers
	 */
	public enum OptionTypes
	{
		/** Boolean option */
		Boolean,
		/** Int option */
		Int,
		/** Float option */
		Float,
		/** Double option */
		Double,
		/** String option */
		String
	}
	
	//-------------------------------------------------------------------------
	
	/** Whether or not arguments are case sensitive. True by default */
	protected final boolean caseSensitive;
	
	/** Description of the program for which we're parsing arguments */
	protected final String description;
	
	/** Nameless options (values must be provided by user in fixed order) */
	protected final List<ArgOption> namelessOptions = new ArrayList<ArgOption>();
	
	/** Named options (these may be provided by user in any order) */
	protected final Map<String, ArgOption> namedOptions = new HashMap<String, ArgOption>();
	
	/** List of named options that are required (nameless options are always required) */
	protected final List<ArgOption> requiredNamedOptions = new ArrayList<ArgOption>();
	
	/** All options, precisely in the order in which they were provided. */
	protected final List<ArgOption> allOptions = new ArrayList<ArgOption>();
	
	/** List containing user-provided values for nameless args (populated by parseArguments() call) */
	protected final List<Object> providedNamelessValues = new ArrayList<Object>();
	
	/** Map containing all user-provided values (populated by parseArguments() call) */
	protected final Map<String, Object> providedValues = new HashMap<String, Object>();
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public CommandLineArgParse()
	{
		this(true);
	}
	
	/**
	 * Constructor
	 * @param caseSensitive
	 */
	public CommandLineArgParse(final boolean caseSensitive)
	{
		this(caseSensitive, null);
	}
	
	/**
	 * Constructor
	 * @param caseSensitive
	 * @param description
	 */
	public CommandLineArgParse(final boolean caseSensitive, final String description)
	{
		this.caseSensitive = caseSensitive;
		this.description = description;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Adds the given option
	 * @param argOption
	 */
	public void addOption(final ArgOption argOption)
	{
		// some error checking
		if (argOption.names != null)
		{
			for (String name : argOption.names)
			{
				if (!caseSensitive)
					name = name.toLowerCase();
				
				if (name.equals("-h") || name.equals("--help"))
				{
					System.err.println
					(
						"Not adding option! Cannot use arg name: " + name + 
						". This is reserved for help message."
					);
					return;
				}
			}
		}
		else
		{
			if (argOption.expectsList())
			{
				System.err.println("Multi-valued nameless arguments are not currently supported!");
				return;
			}
			
			if (!namedOptions.isEmpty())
			{
				System.err.println("Adding nameless options after named options is not currently supported!");
				return;
			}
		}
		
		if 
		(
			argOption.numValsStr != null && 
			!argOption.numValsStr.equals("+") && 
			!argOption.numValsStr.equals("*")
		)
		{
			System.err.println("Not adding option! Invalid numVals specified: " + argOption.numValsStr);
			return;
		}
		
		// try to automatically determine type if not specified
		if (argOption.type == null)
		{
			if (argOption.defaultVal != null)
			{
				if (argOption.defaultVal instanceof Boolean)
					argOption.type = OptionTypes.Boolean;
				else if (argOption.defaultVal instanceof Integer)
					argOption.type = OptionTypes.Int;
				else if (argOption.defaultVal instanceof Float)
					argOption.type = OptionTypes.Float;
				else if (argOption.defaultVal instanceof Double)
					argOption.type = OptionTypes.Double;
				else
					argOption.type = OptionTypes.String;
			}
			else
			{
				if (argOption.expectsList())
				{
					// probably not a boolean flag, let's default to strings
					argOption.type = OptionTypes.String;
				}
				else if (argOption.numVals == 1)
				{
					// also probably not a boolean flag
					argOption.type = OptionTypes.String;
				}
				else
				{
					// expects 0 args, so likely a boolean flag
					argOption.type = OptionTypes.Boolean;
				}
			}
		}
		
		// only allow boolean args to have non-list 0 values
		if 
		(
			argOption.type != OptionTypes.Boolean && 
			argOption.numValsStr == null && 
			argOption.numVals == 0
		)
		{
			System.err.println("Not adding option! Cannot accept 0 values for non-boolean option.");
			return;
		}
		
		// set a default value of false for booleans with no default set yet
		if (argOption.type == OptionTypes.Boolean && argOption.defaultVal == null)
			argOption.defaultVal = Boolean.FALSE;
		
		//System.out.println("adding option: " + argOption);
		
		// add option to all the relevant lists/maps
		allOptions.add(argOption);
		
		// make sure default value is legal
		if (argOption.defaultVal != null && argOption.legalVals != null)
		{
			boolean found = false;
			
			for (final Object legalVal : argOption.legalVals)
			{
				if (legalVal.equals(argOption.defaultVal))
				{
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				System.err.println
				(
					"Error: default value " + argOption.defaultVal + 
					" is not legal. Legal values = " + Arrays.toString(argOption.legalVals)
				);
				return;
			}
		}
		
		if (argOption.names == null)
		{
			namelessOptions.add(argOption);
		}
		else
		{
			for (String name : argOption.names)
			{
				if (!caseSensitive)
					name = name.toLowerCase();
				
				if (namedOptions.containsKey(name))
					System.err.println("Error: Duplicate name:" + name);
				
				namedOptions.put(name, argOption);
			}
			
			if (argOption.required)
				requiredNamedOptions.add(argOption);
		}
	}
	
	/**
	 * Parses the given arguments.
	 * @param args
	 * @return True if arguments were parsed successfully, false otherwise.
	 * Will also return false if the user requested help through "-h" or "--help"
	 * (because that typically means we'd want to stop running the program).
	 */
	public boolean parseArguments(final String[] args)
	{
		String currentToken = null;
		int nextNamelessOption = 0;
		ArgOption currentOption = null;
		String currentOptionName = null;
		List<Object> currentValues = null;
		
		try
		{
			for (int i = 0; i < args.length; /**/)
			{
				currentToken = args[i];
				final String token = caseSensitive ? currentToken : currentToken.toLowerCase();
				
				if (token.equals("-h") || token.equals("--help"))
				{
					printHelp(System.out);
					return false;
				}
				
				if (nextNamelessOption < namelessOptions.size())
				{
					// we should be starting with a new nameless option
					if (!finishArgOption(currentOption, currentOptionName, currentValues))
						return false;
					
					if (namedOptions.containsKey(token))
					{
						System.err.println
						(
							"Error: found name \"" + currentToken + "\" while expecting more nameless options."
						);
						return false;
					}
					
					currentOption = namelessOptions.get(nextNamelessOption);
					currentOptionName = "NAMELESS_" + nextNamelessOption;
					currentValues = new ArrayList<Object>(1);
					
					currentValues.add(tokenToVal(token, currentOption.type));
					
					++nextNamelessOption;
				}
				else if (namedOptions.containsKey(token))
				{
					// looks like we're starting with a new named option
					if (!finishArgOption(currentOption, currentOptionName, currentValues))
						return false;
					
					currentOption = namedOptions.get(token);
					currentOptionName = currentToken;
					currentValues = new ArrayList<Object>();
				}
				else
				{
					// add this token as a value
					currentValues.add(tokenToVal(token, currentOption.type));
				}
				
				++i;
			}
			
			// also make sure to finish handling the very last option
			if (!finishArgOption(currentOption, currentOptionName, currentValues))
				return false;
		}
		catch (final Exception e)
		{
			System.err.println("Parsing args failed on token \"" + currentToken + "\" with exception:");
			e.printStackTrace();
			System.err.println();
			printHelp(System.err);
			System.err.println();
			return false;
		}
		
		// let's make sure we have values for all the required options
		if (providedNamelessValues.size() < namelessOptions.size())
		{
			System.err.println("Missing value for nameless option " + providedNamelessValues.size());
			return false;
		}
		
		for (final ArgOption option : requiredNamedOptions)
		{
			final String key = caseSensitive ? option.names[0] : option.names[0].toLowerCase();

			if (!providedValues.containsKey(key))
			{
				System.err.println("Missing value for required option: " + option.names[0]);
				return false;
			}
		}
		
		return true;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param i
	 * @return The value for the i'th positional (nameless) argument
	 */
	public Object getValue(final int i)
	{
		return providedNamelessValues.get(i);
	}
	
	/**
	 * @param i
	 * @return The value for the i'th positional (nameless) argument as boolean
	 */
	public boolean getValueBool(final int i)
	{
		return ((Boolean) providedNamelessValues.get(i)).booleanValue();
	}
	
	/**
	 * @param i
	 * @return The value for the i'th positional (nameless) argument as int
	 */
	public int getValueInt(final int i)
	{
		return ((Integer) providedNamelessValues.get(i)).intValue();
	}
	
	/**
	 * @param i
	 * @return The value for the i'th positional (nameless) argument as float
	 */
	public float getValueFloat(final int i)
	{
		return ((Float) providedNamelessValues.get(i)).floatValue();
	}
	
	/**
	 * @param i
	 * @return The value for the i'th positional (nameless) argument as double
	 */
	public double getValueDouble(final int i)
	{
		return ((Double) providedNamelessValues.get(i)).doubleValue();
	}
	
	/**
	 * @param i
	 * @return The value for the i'th positional (nameless) argument as String
	 */
	public String getValueString(final int i)
	{
		return (String) providedNamelessValues.get(i);
	}
	
	/**
	 * @param name Name of the option for which to get value
	 * @return The value for the argument with given name (may return default value)
	 */
	public Object getValue(final String name)
	{
		String key = name;
		if (!caseSensitive)
			key = key.toLowerCase();
		
		return providedValues.getOrDefault(key, namedOptions.get(key).defaultVal);
	}
	
	/**
	 * @param name Name of the option for which to get value
	 * @return The value for the argument with given name (may return default value) as boolean
	 */
	public boolean getValueBool(final String name)
	{
		return ((Boolean) getValue(name)).booleanValue();
	}
	
	/**
	 * @param name Name of the option for which to get value
	 * @return The value for the argument with given name (may return default value) as int
	 */
	public int getValueInt(final String name)
	{
		return ((Integer) getValue(name)).intValue();
	}
	
	/**
	 * @param name Name of the option for which to get value
	 * @return The value for the argument with given name (may return default value) as float
	 */
	public float getValueFloat(final String name)
	{
		return ((Float) getValue(name)).floatValue();
	}
	
	/**
	 * @param name Name of the option for which to get value
	 * @return The value for the argument with given name (may return default value) as double
	 */
	public double getValueDouble(final String name)
	{
		return ((Double) getValue(name)).doubleValue();
	}
	
	/**
	 * @param name Name of the option for which to get value
	 * @return The value for the argument with given name (may return default value) as String
	 */
	public String getValueString(final String name)
	{
		return (String) getValue(name);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Print help message to given output stream
	 * @param out
	 */
	public void printHelp(final PrintStream out)
	{
		if (description != null)
			out.print(description);
		else
			out.print("No program description.");
		
		out.println();
		out.println();
		
		if (!namelessOptions.isEmpty())
		{
			out.println("Positional arguments:");
			for (final ArgOption option : namelessOptions)
			{
				printOptionLine(option, out);
			}
			out.println();
		}
		
		out.println("Required named arguments:");
		for (int i = 0; i < allOptions.size(); ++i)
		{
			final ArgOption option = allOptions.get(i);
			
			// skip nameless options, already had those
			if (option.names != null)
			{
				if (option.required)
				{
					printOptionLine(option, out);
				}
			}
		}
		
		out.println();
		
		out.println("Optional named arguments:");
		out.println(" -h, --help                                                      Show this help message.");
		
		for (int i = 0; i < allOptions.size(); ++i)
		{
			final ArgOption option = allOptions.get(i);
			
			// skip nameless options, already had those
			if (option.names != null)
			{
				if (!option.required)
				{
					printOptionLine(option, out);
				}
			}
		}
	}
	
	/**
	 * Prints a help line for the given option
	 * @param option
	 * @param out
	 */
	private static void printOptionLine(final ArgOption option, final PrintStream out)
	{
		final StringBuilder sb = new StringBuilder();
		
		if (option.names == null)
		{
			if (option.legalVals != null)
			{
				sb.append(" {");
				for (int i = 0; i < option.legalVals.length; ++i)
				{
					if (i > 0)
						sb.append(",");
					
					sb.append(option.legalVals[i]);
				}
				sb.append("}");
			}
			else
			{
				sb.append(" " + option.type.toString().toUpperCase());
			}
		}
		else
		{
			sb.append(" ");
			
			for (int i = 0; i < option.names.length; ++i)
			{
				sb.append(option.names[i]);
				
				if (i + 1 < option.names.length)
					sb.append(", ");
			}
			
			String metaVar = option.names[0].toUpperCase();
			while (metaVar.startsWith("-"))
			{
				metaVar = metaVar.substring(1);
			}
			metaVar = metaVar.replaceAll("-", "_");
			
			if (option.numValsStr == null)
			{
				if (option.numVals > 0)
				{
					if (option.numVals == 1)
					{
						sb.append(" " + metaVar);
					}
					else
					{
						for (int i = 1; i <= option.numVals; ++i)
						{
							sb.append(" " + metaVar + "_" + i);
						}
					}
				}
			}
			else if (option.numValsStr.equals("+"))
			{
				sb.append(" " + metaVar + "_1");
				sb.append(" [ " + metaVar + "_* ... ]");
			}
			else if (option.numValsStr.equals("*"))
			{
				sb.append(" [ " + metaVar + "_* ... ]");
			}
		}
		
		if (sb.length() >= 65)
		{
			sb.append("\t");
		}
		else
		{
			while (sb.length() < 65)
			{
				sb.append(" ");
			}
		}
		
		sb.append(option.help);
		
		out.println(sb.toString());
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Finish handling the arg option we're currently processing
	 * @param currentOption
	 * @param currentOptionName
	 * @param currentValues
	 * @return True if everything is fine, false if there's a problem
	 */
	private boolean finishArgOption
	(
		final ArgOption currentOption, 
		final String currentOptionName, 
		final List<Object> currentValues
	)
	{
		if (currentOption != null)
		{
			// first check if our current option has as many values as it needs
			if (currentOption.numValsStr == null)
			{
				if (currentValues.size() != currentOption.numVals)
				{
					System.err.println
					(
						"Error: " + currentOptionName + " requires " + currentOption.numVals + 
						" values, but received " + currentValues.size() + " values."
					);
					return false;
				}
			}
			else if (currentOption.numValsStr.equals("+"))
			{
				if (currentValues.size() == 0)
				{
					System.err.println
					(
						"Error: " + currentOptionName + " requires more than 0" + 
						" values, but only received 0 values."
					);
					return false;
				}
			}
			
			// make sure all provided values are legal
			if (currentOption.legalVals != null)
			{
				for (final Object val : currentValues)
				{
					boolean found = false;
					
					for (final Object legalVal : currentOption.legalVals)
					{
						if (val.equals(legalVal))
						{
							found = true;
							break;
						}
					}
					
					if (!found)
					{
						System.err.println
						(
							"Error: " + val + " is an illegal value."
							+ " Legal values = " + Arrays.toString(currentOption.legalVals)
						);
						return false;
					}
				}
			}
			
			if (currentOption.names == null)
			{
				if (currentOption.expectsList())
				{
					// just put the list as value
					providedNamelessValues.add(currentValues);
				}
				else if (currentValues.size() == 0 && currentOption.type == OptionTypes.Boolean)
				{
					// boolean flag without any values, simply means "set to true"
					providedNamelessValues.add(Boolean.TRUE);
				}
				else
				{
					// extract element from list (should be just a single element)
					providedNamelessValues.add(currentValues.get(0));
				}
			}
			else
			{
				for (String name : currentOption.names)
				{
					if (!caseSensitive)
						name = name.toLowerCase();
					
					if (currentOption.expectsList())
					{
						// just put the list as value
						providedValues.put(name, currentValues);
					}
					else if (currentValues.size() == 0 && currentOption.type == OptionTypes.Boolean)
					{
						// boolean flag without any values, simply means "set to true"
						providedValues.put(name, Boolean.TRUE);
					}
					else
					{
						// extract element from list (should be just a single element)
						providedValues.put(name, currentValues.get(0));
					}
				}
			}
		}
		
		return true;
	}
	
	private static Object tokenToVal(final String token, final OptionTypes type)
	{
		if (type == OptionTypes.Boolean)
			return Boolean.parseBoolean(token);
		else if (type == OptionTypes.Double)
			return Double.parseDouble(token);
		else if (type == OptionTypes.Float)
			return Float.parseFloat(token);
		else if (type == OptionTypes.Int)
			return Integer.parseInt(token);
		else
			return token;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Class for options that we may have.
	 * 
	 * @author Dennis Soemers
	 */
	public final static class ArgOption
	{
		
		//---------------------------------------------------------------------
		
		/** List of names/flags for this option */
		protected String[] names = null;
		
		/** 
		 * Option type.
		 * 
		 * If the type is not specified, we will try to intelligently determine
		 * the type based on any specified default values. If there is no
		 * specified default value either, we'll assume boolean by default,
		 * or String if the number of values has been specified to be greater
		 * than 0.
		 */
		protected OptionTypes type = null;
		
		/** Expected number of values we expect to be supplied */
		protected int numVals = 0;
		
		/** 
		 * String description of expected number of values. Can be:
		 * - null, meaning we just look at the numVals int
		 * - "*", meaning we allow any number >= 0
		 * - "+", meaning we allow any number > 0
		 */
		protected String numValsStr = null;
		
		/** 
		 * Default value to be returned if no value supplied.
		 * Only used for args that are not nameless, and not required.
		 */
		protected Object defaultVal = null;
		
		/** 
		 * If true, the parser will fail if no value is provided by the user. 
		 * False by default.
		 */
		protected boolean required = false;
		
		/** If not null, only objects in this array will be legal */
		protected Object[] legalVals = null;
		
		/** Description for this option in help message */
		protected String help = "";
		
		//---------------------------------------------------------------------
		
		/**
		 * Constructor
		 */
		public ArgOption()
		{
			// do nothing
		}
		
		//---------------------------------------------------------------------
		
		/**
		 * Set names/flags that can be used for this arg option
		 * @param optionNames
		 * @return Arg option with names.
		 */
		public ArgOption withNames(final String... optionNames)
		{
			this.names = optionNames;
			return this;
		}
		
		/**
		 * Set type for this arg option
		 * @param optionType
		 * @return  Arg option with type.
		 */
		public ArgOption withType(final OptionTypes optionType)
		{
			this.type = optionType;
			return this;
		}
		
		/**
		 * Set the expected number of values for this arg option
		 * @param optionNumVals
		 * @return  Arg option with values.
		 */
		public ArgOption withNumVals(final int optionNumVals)
		{
			this.numVals = optionNumVals;
			return this;
		}
		
		/**
		 * Set the expected number of values for this arg option
		 * @param optionNumValsStr
		 * 	Use "*" for anything >= 0, or "+" for anything > 0
		 * @return  Arg option with values.
		 */
		public ArgOption withNumVals(final String optionNumValsStr)
		{
			if (StringRoutines.isInteger(optionNumValsStr))
				return withNumVals(Integer.parseInt(optionNumValsStr));
			
			this.numValsStr = optionNumValsStr;
			return this;
		}
		
		/**
		 * Set the default value for this arg option
		 * @param optionDefaultVal
		 * @return  Arg option with default.
		 */
		public ArgOption withDefault(final Object optionDefaultVal)
		{
			this.defaultVal = optionDefaultVal;
			return this;
		}
		
		/**
		 * Mark this arg option as required
		 * @return  Arg option with required set.
		 */
		public ArgOption setRequired()
		{
			return setRequired(true);
		}
		
		/**
		 * Set whether this arg option is required
		 * @param required
		 * @return  Arg option with required set.
		 */
		public ArgOption setRequired(final boolean required)
		{
			this.required = required;
			return this;
		}
		
		/**
		 * Set restricted list of legal values
		 * @param optionLegalVals
		 * @return  Arg option with legal values.
		 */
		public ArgOption withLegalVals(final Object... optionLegalVals)
		{
			this.legalVals = optionLegalVals;
			return this;
		}
		
		/**
		 * Set the description for help message of this arg option
		 * @param optionHelp
		 * @return Arg option with help.
		 */
		public ArgOption help(final String optionHelp)
		{
			this.help = optionHelp;
			return this;
		}
		
		//---------------------------------------------------------------------
		
		/**
		 * @return Whether we expect a list of values (rather than a single value)
		 */
		protected boolean expectsList()
		{
			if (numValsStr != null)
				return true;
			return (numVals > 1);
		}
		
		//---------------------------------------------------------------------
		
		@Override
		public String toString()
		{
			final StringBuilder sb = new StringBuilder();
			sb.append("[ArgOption: ");
			
			if (names != null)
			{
				for (int i = 0; i < names.length; ++i)
				{
					sb.append(names[i]);
					
					if (i + 1 < names.length)
						sb.append(", ");
				}
			}
			
			sb.append(" type=" + type);
			
			if (numValsStr != null)
				sb.append(" numVals=" + numValsStr);
			else
				sb.append(" numVals=" + numVals);
			
			if (defaultVal != null)
				sb.append(" default=" + defaultVal);
			
			if (required)
				sb.append(" required");
			
			if (legalVals != null)
			{
				sb.append(" legalVals=" + Arrays.toString(legalVals));
			}
			
			if (help.length() > 0)
				sb.append("\t\t" + help);
			
			sb.append("]");
			return sb.toString();
		}
		
		//---------------------------------------------------------------------
		
	}
	
	//-------------------------------------------------------------------------

}
