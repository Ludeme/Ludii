package grammar; 

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import annotations.Alias;
import main.Constants;
import main.FileHandling;
import main.StringRoutines;
import main.grammar.Clause;
import main.grammar.ClauseArg;
import main.grammar.GrammarRule;
import main.grammar.LudemeInfo;
import main.grammar.PackageInfo;
import main.grammar.Symbol;
import main.grammar.Symbol.LudemeType;
import main.grammar.ebnf.EBNF;

//-----------------------------------------------------------------------------

/**
 * Ludii class grammar generator.
 * 
 * @author cambolbro
 */
public class Grammar
{
	/** List of symbols. */
	private final List<Symbol> symbols = new ArrayList<Symbol>();

	/** Hash map for accessing symbols directly by name (might be multiples). */
	private final Map<String, List<Symbol>> symbolsByName = new HashMap<String, List<Symbol>>();
	
	/** Hash map for identifying symbols by partial keyword (most will have multiples). */
	private final Map<String, List<Symbol>> symbolsByPartialKeyword = new HashMap<String, List<Symbol>>();	
	
	/** List of rules. */
	private final List<GrammarRule> rules = new ArrayList<GrammarRule>();

	/** List of rule packages. */
	private final List<PackageInfo> packages = new ArrayList<PackageInfo>();

	/** Order in which packages are to be printed. */
	private final List<PackageInfo> packageOrder = new ArrayList<PackageInfo>();

	private Symbol rootGameSymbol = null;
	private Symbol rootMetadataSymbol = null;

	private EBNF ebnf = null;
	
	//-------------------------------------------------------------------------

	/** Primitive symbols with notional package names. */
	public static final String[][] Primitives =
	{
		{ "int", 	 "game.functions.ints"     },
		{ "boolean", "game.functions.booleans" },
		{ "float",   "game.functions.floats"   },
	};

	/** Predefined symbols (known data types) with notional and actual package names, and keyword. */
	public static final String[][] Predefined =
	{
		{ "java.lang.Integer", "game.functions.ints", 	  "java.lang", "int"     },
		{ "java.lang.Boolean", "game.functions.booleans", "java.lang", "boolean" }, 
		{ "java.lang.Float",   "game.functions.floats",   "java.lang", "float"   }, 
		{ "java.lang.String",  "game.types", 			  "java.lang", "string"  },
	};

	/** Function names and associated constants. */
	private final String[][] Functions =
	{ 
		// Function name Arg replacement Listed symbols (if any)
		{ "IntFunction", 		"int", 		  },
		{ "IntConstant", 		"int", 		  },
		{ "BooleanFunction", 	"boolean", 	  },
		{ "BooleanConstant", 	"boolean", 	  },
		{ "FloatFunction", 		"float", 	  },
		{ "FloatConstant", 		"float", 	  },
		{ "IntArrayFunction", 	"ints", 	  },
		{ "IntArrayConstant", 	"ints", 	  },
		{ "RegionFunction", 	"sites", 	  },
		{ "RegionConstant", 	"sites", 	  },
		//{ "Region", 			"sites", 	  },
		{ "RangeFunction", 		"range", 	  },
		{ "RangeConstant", 		"range", 	  },
		{ "DirectionsFunction", "directions", },
		{ "DirectionsConstant", "directions", },
		{ "GraphFunction", 		"graph",	  },
		{ "GraphConstant", 		"graph", 	  },
		//{ "GraphFunction", 		"tiling",	  },
		//{ "GraphConstant", 		"tiling", 	  },
		{ "DimFunction", 		"dim", 		  },
		{ "DimConstant", 		"dim", 		  },
	};

	/** Application constants. */
	public final static String[][] ApplicationConstants =
	{ 
		// Constant name, return type, notional package, value
		{ "Off",       "int", "global", ("" + Constants.OFF) },
		{ "End",       "int", "global", ("" + Constants.END) },
		{ "Undefined", "int", "global", ("" + Constants.UNDEFINED) },
		{ "Infinity",  "int", "global", ("" + Constants.INFINITY) },
//		{ "Unused",    "int", "global", ("" + Constants.UNUSED) },
//		{ "Nobody",    "int", "global", ("" + Constants.NOBODY) },
//		{ "NoPiece",   "int", "global", ("" + Constants.NO_PIECE) },
//		{ "Repeat",    "int", "global", ("" + Constants.REPEAT) },
	};

	//-------------------------------------------------------------------------

	private static volatile Grammar singleton = null;
	
	//-------------------------------------------------------------------------

	private Grammar()
	{		
		//final long startAt = System.nanoTime();
		generate();
		//final long endAt = System.nanoTime();
		//final double secs = (endAt - startAt) / 1000000000.0;
		//System.out.println("Grammar generated in " + secs + "s.");
	}

	//-------------------------------------------------------------------------

	/**
	 * Access grammar here.
	 * @return The singleton grammar object.
	 */
	public static Grammar grammar()
	{
		if (singleton == null)
		{
			synchronized(Grammar.class) 
			{
				if (singleton == null)
					singleton = new Grammar();
			}
		}
		return singleton;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param name
	 * @return Map of symbols by name.
	 */
	public List<Symbol> symbolsByName(final String name)
	{
		return symbolsByName.get(name);
	}
	
//	/**
//	 * @return Map of symbols by partial keyword.
//	 */
//	public Map<String, List<Symbol>> symbolsByPartialKeyword()
//	{
//		return symbolsByPartialKeyword;
//	}
	
	/**
	 * @return The list of the symbols.
	 */
	public List<Symbol> symbols()
	{
		return Collections.unmodifiableList(symbols);
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Note: This looks like a getter but also may do some processing.
	 * 
	 * @return The EBNF.
	 */
	public EBNF ebnf()
	{
		if (ebnf == null)
			ebnf = new EBNF(grammar().toString());
		
		//findEBNFClasses(ebnf);
		
		return ebnf;
	}
	
	//-------------------------------------------------------------------------

//	/**
//	 * Do here in Grammar rather than EBNF, so EBNF doesn't depend on Grammar. 
//	 */
//	public void findEBNFClasses()
//	{
//		for (final EBNFRule rule : Grammar.grammar().ebnf().rules().values())
//		{	
//			System.out.println("EBNF rule: " + rule);
//			//final Class<?> cls = null;
//			// ...
//			//rule.setClass(cls);
//		}
//	}
	
	//-------------------------------------------------------------------------

	/**
	 * Generate the grammar from the current code base.
	 */
	void execute()
	{
		System.out.println("Ludii library " + Constants.LUDEME_VERSION + ".");

		generate();

		final String outFileName = "grammar-" + Constants.LUDEME_VERSION + ".txt";
		System.out.println("Saving to file " + outFileName + ".");

		try
		{
			export(outFileName);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Generate grammar from the class library.
	 */
	public void generate()
	{
		symbols.clear();
		getRules().clear();
		packages.clear();
				
		createSymbols();	
		disambiguateSymbols();
		
		createRules();
		addReturnTypeClauses();
		addApplicationConstantsToRule();
		crossReferenceSubclasses();
		//replaceListFunctionArgs();
		linkDirectionsRules();
		linkRegionRules();
		handleDimFunctions();
		handleGraphAndRangeFunctions();
		handleTrackSteps();
		linkToPackages();
		instantiateSingleEnums();
		
		visitSymbols(rootGameSymbol);
		visitSymbols(rootMetadataSymbol);
		
		setDisplayOrder(rootGameSymbol);
		removeRedundantFunctionNames();
		createSymbolMap();
		alphabetiseRuleClauses();
		removeDuplicateClauses();
		filterOutPrimitiveWrappers();
		
		setUsedInGrammar();
		setUsedInDescription();
		setUsedInMetadata();
		
		setLudemeTypes();
		setAtomicLudemes();
		findAncestors();
		tidyUpFormat();

		final boolean debug = FileHandling.isCambolbro() && false;
		if (debug)
		{
			System.out.println(symbolDetails());
		}
		
//		System.out.println("=================\nPackages:");
//		for (Package pack : packages)
//			System.out.println(pack.path());
//		System.out.println("\n=================\n");

		// Show symbol count
		int numGrammar    = 0;
		int numMetadata   = 0;
		int numClasses    = 0;
		int numConstants  = 0;
		int numPredefined = 0;
		int numPrimitive  = 0;
		
		for (final Symbol symbol : symbols)
		{	
			if (symbol.usedInDescription())
				numGrammar++;
			if (symbol.usedInMetadata())
				numMetadata++;
			
			if (symbol.isClass())
				numClasses++;
			if (symbol.ludemeType() == LudemeType.Constant)
				numConstants++;
			if (symbol.ludemeType() == LudemeType.Predefined)
				numPredefined++;
			if (symbol.ludemeType() == LudemeType.Primitive)
				numPrimitive++;
		}
		
		if (debug)
		{
			System.out.println
			(
				symbols.size() + " symbols: " + numClasses + " classes, " + 
				numConstants + " constants, " + numPredefined + " predefined, " + numPrimitive + " primitives."
			);
			System.out.println(getRules().size() + " rules, " + numGrammar + " used in grammar, " + 
								numMetadata + " used in metadata.");
		}
			
		if (debug)
		{
			// Check ludemes used
			System.out.println("Ludemes used:");
			
			final List<LudemeInfo> ludemesUsed = ludemesUsed();
			for (final LudemeInfo li : ludemesUsed)
				System.out.println(li.symbol().token() + " (" + li.symbol().name() + ") : " + li.symbol().cls().getName());
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Create all symbols in the grammar.
	 */
	void createSymbols()
	{		
		Symbol symbolInt = null;
		
		// Create Primitive symbols
		for (int pid = 0; pid < Primitives.length; pid++)
		{
			Class<?> cls = null;
			if (Primitives[pid][0].equals("int"))
				cls = int.class;
			else if (Primitives[pid][0].equals("float"))
				cls = float.class;
			else if (Primitives[pid][0].equals("boolean"))
				cls = boolean.class;
			
			final Symbol symbol = new Symbol(LudemeType.Primitive, Primitives[pid][0], null, Primitives[pid][1], cls);
			symbol.setReturnType(symbol);  // returns itself
			symbols.add(symbol);
			
			if (Primitives[pid][0].equals("int"))
				symbolInt = symbol;
		}
		
		// Create Predefined symbols
		for (int pid = 0; pid < Predefined.length; pid++)
		{
			Class<?> cls = null;
			try
			{
				cls = Class.forName(Predefined[pid][0]);
			} catch (final ClassNotFoundException e)
			{
				e.printStackTrace();
			}
			final Symbol symbol = new Symbol
								  (
										LudemeType.Predefined, Predefined[pid][0], null, 
										Predefined[pid][1], cls
								  );																									
			symbol.setToken(Predefined[pid][3]);
			symbol.setGrammarLabel(Predefined[pid][3]);
			symbol.setReturnType(symbol);  // returns itself
			symbols.add(symbol);			
		}
		
		// Create symbols for application constants
		for (int cs = 0; cs < ApplicationConstants.length; cs++)
		{
			// **
			// ** Assumes that all application constants are ints. Extend this if needed.
			// **
			final Class<?> cls = int.class;
			final Symbol symbolC = new Symbol
								   (
										LudemeType.Constant, ApplicationConstants[cs][0], 
										null, ApplicationConstants[cs][2], cls
								   );
			symbolC.setReturnType(symbolInt);		
			symbols.add(symbolC);
		}

		// Traverse class hierarchy to generate symbols
		findSymbolsFromClasses("game.Game");
		findSymbolsFromClasses("metadata.Metadata");

		checkHiddenClasses();
		checkAbstractClasses();
		handleEnums();
		overrideReturnTypes();

		// Check that appropriate symbols are included in grammar
//		for (final Symbol symbol : symbols)
//			//if (symbol.hasAlias())
//			if (!symbol.name().equalsIgnoreCase(symbol.token()))
//				symbol.setUsedInGrammar(true);  // is probably an alias or remapped form something else
		
//		// Ensure that functions are not used in grammar
//		for (final Symbol symbol : symbols)
//			if (symbol.name().contains("Function"))
//				System.out.println("Symbol function found: " + symbol.info());
		
		// Set root symbols
		rootGameSymbol = null;
		rootMetadataSymbol = null;

		for (final Symbol symbol : symbols)
		{
			if (symbol.path().equals("game.Game"))
				rootGameSymbol = symbol;
			
			if (symbol.path().equals("metadata.Metadata"))
				rootMetadataSymbol = symbol;
		}
		
		if (rootGameSymbol == null || rootMetadataSymbol == null)
			throw new RuntimeException("Cannot find game.Game or metadata.Metadata.");
	}

	//-------------------------------------------------------------------------

	/**
	 * Traverse files in library to find symbols.
	 * 
	 * @param rootPackageName The root package name.
	 */
	public void findSymbolsFromClasses(final String rootPackageName)
	{
		Class<?> clsRoot = null;
		try
		{
			clsRoot = Class.forName(rootPackageName);
		} 
		catch (final ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		
		final List<Class<?>> classes = ClassEnumerator.getClassesForPackage(clsRoot.getPackage());

		for (final Class<?> cls : classes)
		{
			if (cls.getName().contains("$"))
				continue;  // is an internal class or enum?

			if (cls.getName().contains("package-info"))
				continue;  // placeholder file for documentation
			
			String alias = null;

			// From: http://tutorials.jenkov.com/java-reflection/annotations.html
			final Annotation[] annotations = cls.getAnnotations();
			for (final Annotation annotation : annotations)
			{
				if (annotation instanceof Alias)
				{
					final Alias anno = (Alias)annotation;
					alias = anno.alias();
				}
			}
			
			final String classPath = cls.getName();
			
			// **
			// ** Assume it is Ludeme type until proven otherwise.
			// **
			
			final Symbol symbol = new Symbol(LudemeType.Ludeme, classPath, alias, cls);
			symbol.setReturnType(symbol);  // returns itself (unless superceded by eval())
			symbols.add(symbol);
		
			// Add this package name, if not already found
			final Package pack = cls.getPackage();
			final String packageName = pack.getName();
	
			int p;
			for (p = 0; p < packages.size(); p++)
				if (packages.get(p).path().equals(packageName))
					break;
			if (p >= packages.size())
				packages.add(new PackageInfo(packageName));
		}
	}

	//-------------------------------------------------------------------------

	void disambiguateSymbols()
	{
		for (int sa = 0; sa < symbols.size(); sa++)
		{
			final Symbol symbolA = symbols.get(sa);
			if (!symbolA.isClass())
				continue;
			
			String grammarLabel = "";
			
			for (int sb = 0; sb < symbols.size(); sb++)
			{
				if (sa == sb)
					continue;
				
				final Symbol symbolB = symbols.get(sb);
				if (!symbolB.isClass())
					continue;
				
				if (symbolA.name().equals(symbolB.name()))
				{
					// Classes with same name found
					final String label = symbolA.disambiguation(symbolB);					
			
					if (label == null)
						continue;
					
					if (label.length() > grammarLabel.length())
						grammarLabel = new String(label);
				}
			}
			
			if (grammarLabel != "")
				symbolA.setGrammarLabel(grammarLabel);
		}
	}
		
	//-------------------------------------------------------------------------

	void createSymbolMap()
	{
		symbolsByName.clear();
		for (final Symbol symbol : symbols)
		{
			final String key = symbol.name();
			List<Symbol> list = symbolsByName.get(key);
			if (list != null)
			{
				// Symbol(s) with same name already in map
				list.add(symbol);
			}
			else
			{
				// Add symbol to new list
				list = new ArrayList<Symbol>();
				list.add(symbol);
				symbolsByName.put(key, list);
			}
		}
		
//		// Sort lists (shortest to longest)
//		for (final List<Symbol> list : symbolsByName.values())
//		{
//			Collections.sort(list, new Comparator<Symbol>()
//			{		
//				@Override
//				public int compare(final Symbol sa, final Symbol sb)
//				{
//					final int lenA = sa.name().length();
//					final int lenB = sb.name().length();
//					
//					if (lenA == lenB)
//						return sa.name().compareTo(sb.name());
//						
//					return lenA < lenB ? -1 : 1;
//				}
//			});
//		}
		
		// Create map for partial keyword matches
		symbolsByPartialKeyword.clear();
		for (final Symbol symbol : symbols)
		{
			final String fullKey = symbol.token();
			
			for (int i = 1; i < fullKey.length() + 1; i++)
			{
				final String key = fullKey.substring(0, i);
//				System.out.println("key=" + key);
				
				List<Symbol> list = symbolsByPartialKeyword.get(key);
				if (list != null)
				{
					// Symbol(s) with same name already in map
					list.add(symbol);
				}
				else
				{
					// Add symbol to new list
					list = new ArrayList<Symbol>();
					list.add(symbol);
					symbolsByPartialKeyword.put(key, list);
				}
			}
		}
		//System.out.println(symbolMap.size() + " symbol entries.");
		//System.out.println(symbolMapPartial.size() + " partial entries.");
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @param className May be class name or alias.
	 * @return Symbol list matching the specified keyword.
	 */
	public List<Symbol> symbolListFromClassName(final String className)
	{
		final List<Symbol> list = symbolsByName.get(className);
		if (list != null)
			return list;  // found symbol by class name
		
		// Check for aliases
		for (final Symbol symbol : symbols)
			if (symbol.token().equals(className))
				return symbolsByName.get(symbol.name());
		
		return null;  // could not find
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param partialKeyword The partial keyword.
	 * @return Symbols matching the given partial keyword.
	 */
	public List<Symbol> symbolsWithPartialKeyword(final String partialKeyword)
	{
		return symbolsByPartialKeyword.get(partialKeyword);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param description Game description (may be expanded or not).
	 * @param cursorAt    Character position of cursor.
	 * @param usePartial  True if we use the partial keywords.
	 * @return Paths of best matches for the keyword at the cursor position.
	 */
	public List<String> classPaths(final String description, final int cursorAt, final boolean usePartial)
	{
		final List<String> list = new ArrayList<String>();
		
		if (cursorAt <= 0 || cursorAt >= description.length())
		{
			System.out.println("** Grammar.classPaths(): Invalid cursor position " + cursorAt + " specified.");
			return list;
		}
		
		// Get partial keyword up to cursor
		int c = cursorAt - 1;
		char ch = description.charAt(c);
		
		if (!StringRoutines.isTokenChar(ch))
		{
			// Not currently on a token (or at the start of one): return nothing
			return list;
		}
		
		while (c > 0 && StringRoutines.isTokenChar(ch))
		{
			c--;
			ch = description.charAt(c);
		}
						
		// Get full keyword
		int cc = cursorAt;
		char ch2 = description.charAt(cc);
		
		while (cc < description.length() && StringRoutines.isTokenChar(ch2))
		{
			cc++;
			if (cc < description.length())
				ch2 = description.charAt(cc);
		}
		
		if (cc >= description.length())
		{
			System.out.println("** Grammar.classPaths(): Couldn't find end of token from position " + cursorAt + ".");
			return list;
		}
		
		if (ch2 == ':')
		{
			// Token is a parameter name: return nothing
			return list;
		}
		
		String partialKeyword = description.substring(c+1, cursorAt);
		String fullKeyword = description.substring(c+1, cc);
		
		boolean isRule = false;
		
		if (partialKeyword.charAt(0) == '<')
		{
			isRule = true;
			partialKeyword = partialKeyword.substring(1);
		}
		
		if (fullKeyword.charAt(0) == '<' && fullKeyword.charAt(fullKeyword.length() - 1) == '>')
		{
			isRule = true;
			fullKeyword = fullKeyword.substring(1, fullKeyword.length() - 1);
		}
		
		if 
		(
			description.charAt(c) == '<' 
			|| 
			description.charAt(c) == '[' && description.charAt(c+1) == '<'
			|| 
			description.charAt(c) == '(' && description.charAt(c+1) == '<'
		)
			isRule = true;
		
		// Handle primitive and predefined types
		if (fullKeyword.charAt(0) == '"')
		{
			// Is a string: note that quotes '"' are a valid token character 
			list.add("java.lang.String");
			return list;
		}
		else if (fullKeyword.equals("true"))
		{
			list.add("true");
		}
		else if (fullKeyword.equals("false"))
		{
			list.add("false");
		}
		else if (StringRoutines.isInteger(fullKeyword))
		{
			list.add("int");
		}
		else if (StringRoutines.isFloat(fullKeyword) || StringRoutines.isDouble(fullKeyword))
		{
			list.add("float");
		}

		// Find for matches
		final String keyword = usePartial ? partialKeyword : fullKeyword;
		
		final List<Symbol> matches = symbolsWithPartialKeyword(keyword);
		if (matches == null)
		{
			// Nothing to return
			return list;
		}
		
		// Filter out unwanted matches
		if (ch == '(')
		{
			// Is a class constructor
			for (final Symbol symbol : matches)
				if (symbol.cls() != null && !symbol.cls().isEnum())
					list.add(symbol.path());
		}
		else if (ch == '<' || isRule)
		{
			// Is a rule
			for (final Symbol symbol : matches)
				list.add(symbol.path());
		}
		else if (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t' || ch == ':' || ch == '{')
		{
			// Is a terminal (probably an enum)
			for (final Symbol symbol : matches)
				if (symbol.cls() != null && symbol.cls().isEnum())
				{
					int lastDot = symbol.path().length() - 1;
					while (lastDot >= 0 && symbol.path().charAt(lastDot) != '.')
						lastDot--;
					final String enumString = symbol.path().substring(0,lastDot) + '$' + symbol.path().substring(lastDot + 1);
					list.add(enumString);
				}
		}

		// Filter ludemes/metadata as appropriate
		final int metadataAt = description.indexOf("(metadata");
		final boolean inMetadata = metadataAt != -1 && metadataAt < cursorAt;

		for (int n = list.size() - 1; n >= 0; n--)
		{
			final boolean isMetadata = list.get(n).contains("metadata.");
			if (isMetadata && !inMetadata || !isMetadata && inMetadata)
				list.remove(n);
		}

		return list;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param name
	 * @return Index of application constant with same name, else -1 if none.
	 */
	public static int applicationConstantIndex(final String name)
	{
		for (int ac = 0; ac < ApplicationConstants.length; ac++)
			if (ApplicationConstants[ac][0].equals(name))
				return ac;
		return -1;
	}

	//-------------------------------------------------------------------------

	/**
	 * Check for symbols that should be hidden.
	 */
	void checkHiddenClasses()
	{
		for (final Symbol symbol : symbols)
		{
			final Class<?> cls = symbol.cls();
			if (cls == null)
				continue;

			final Annotation[] annos = cls.getAnnotations();
			for (final Annotation anno : annos)
			{
				if (anno.annotationType().getName().equals("annotations.Hide"))
				{
					symbol.setHidden(true);
					break;
				}
			}
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Determine which classes are abstract. These constitute rules rather than instances.
	 */
	void checkAbstractClasses()
	{
		for (final Symbol symbol : symbols)
		{
			final Class<?> cls = symbol.cls();
			if (cls == null)
				continue;

			if (Modifier.isAbstract(cls.getModifiers()))
				symbol.setIsAbstract(true);
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Handle enums (including inner classes).
	 */
	void handleEnums()
	{
		final List<Symbol> newSymbols = new ArrayList<Symbol>();

		for (final Symbol symbol : symbols)
		{
			final Class<?> cls = symbol.cls();
			if (cls == null)
				continue;

			if (cls.isEnum())
				extractEnums(cls, symbol, newSymbols);

			// Check for inner classes
			for (final Class<?> inner : cls.getClasses())
			{
				if (inner.isEnum())
				{
					extractEnums(inner, symbol, newSymbols);
				}
				else
				{
					// Ignore inner classes that are not enums, even if they're ludemes
					//System.out.println("!! Grammar.handleEnums(): Inner class found but not enum and not handled: ");
					//System.out.println("   " + inner.getName() + "\n");
				}
			}
		}

		for (final Symbol newSymbol : newSymbols)
			if (findSymbolMatch(newSymbol) == null)
				symbols.add(newSymbol);
	}

	//-------------------------------------------------------------------------

	/**
	 * Extract enums from class (including inner class).
	 */
	static void extractEnums(final Class<?> cls, final Symbol symbol, final List<Symbol> newSymbols)
	{
		final Symbol symbolEnum = new Symbol(LudemeType.Structural, cls.getName(), null, cls);
		symbolEnum.setReturnType(symbolEnum);
		newSymbols.add(symbolEnum);
		
		for (final Object enumObj : cls.getEnumConstants())
		{
			final String symbolName = enumObj.toString();
			final String path = symbolEnum.path() + "." + symbolName;
			
			// Check for alias
			String alias = null;
			
			// From: https://clevercoder.net/2016/12/12/getting-annotation-value-enum-constant/
			Field declaredField = null;
			try 
			{ 
				declaredField = cls.getDeclaredField(((Enum<?>)enumObj).name());	
			} 
			catch (final NoSuchFieldException e) 
			{ 
				e.printStackTrace(); 
			}
          
			if (declaredField != null) 
			{
				final Annotation[] annotations = declaredField.getAnnotations();
				for (final Annotation annotation : annotations)
					if (annotation instanceof Alias)
					{
						final Alias anno = (Alias)annotation;
						alias = anno.alias();
					}
			}
			
			final Symbol symbolValue =  new Symbol
										(
											LudemeType.Constant, path, alias, 
											symbolEnum.notionalLocation(), cls
										);
			symbolValue.setReturnType(symbolEnum); // link value to its enum type
			newSymbols.add(symbolValue);
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Override return types from classes with eval() method.
	 */
	void overrideReturnTypes()
	{
		final List<Symbol> newSymbols = new ArrayList<Symbol>();

		for (final Symbol symbol : symbols)
		{
			if (!symbol.isClass())
				continue;

			final Class<?> cls = symbol.cls();
			if (cls == null)
				continue;

			// Handle aliases here as well
			String alias = null;
	
			// From: http://tutorials.jenkov.com/java-reflection/annotations.html
			final Annotation[] classAnnotations = cls.getAnnotations();
			for (final Annotation annotation : classAnnotations)
			{
				if (annotation instanceof Alias)
				{
					final Alias anno = (Alias)annotation;
					alias = anno.alias();
				}
			}
			
			// Check for run() method
			final Method[] methods = cls.getDeclaredMethods();
			for (final Method method : methods)
			{
				if (!method.getName().equals("eval"))
					continue;

				final Type returnType = method.getReturnType();
				if (returnType == null)
				{
					System.out.println("** Bad return type.");
					continue;
				}

				final String returnTypeName = returnType.getTypeName();
				if (returnTypeName.equals("void"))
					continue;

//				if (returnTypeName.equals("Moves"))
//					continue;

				final Symbol temp = new Symbol(null, returnTypeName, alias, cls);

				Symbol returnSymbol = findSymbolByPath(temp.path());
				if (returnSymbol == null)
				{
					continue;
				}

				if (symbol.nesting() != temp.nesting())
				{
					// Create a new return symbol for a collection of this symbol
					returnSymbol = new Symbol(returnSymbol);
					returnSymbol.setNesting(temp.nesting());
					returnSymbol.setReturnType(returnSymbol);
					newSymbols.add(returnSymbol);
				}
				symbol.setReturnType(returnSymbol);
			}
		}

		for (final Symbol newSymbol : newSymbols)
			if (findSymbolMatch(newSymbol) == null)
				symbols.add(newSymbol);
	}

	//-------------------------------------------------------------------------

	/**
	 * @param path
	 * @return Symbol with specified path, else null if not found.
	 */
	public Symbol findSymbolByPath(final String path)
	{
		for (final Symbol symbol : symbols)
			if (symbol.path().equalsIgnoreCase(path))
				return symbol;
		return null;
	}

	//-------------------------------------------------------------------------

	/**
	 * Create rules based on symbol return types.
	 */
	void createRules()
	{
		for (final Symbol symbol : symbols)
		{
			if (symbol.hidden())
				continue; // skip hidden symbols

			if (symbol.ludemeType() == LudemeType.Constant)
			{
				// Add constant clause to enum rule
				final Symbol lhs = symbol.returnType();
				final GrammarRule rule = getRule(lhs);
				final Clause clause = new Clause(symbol);
				
				rule.addToRHS(clause);
			}
			else
			{
				// Ensure that rule exists (or is created) for this symbol
				final Symbol lhs = symbol;
				final GrammarRule rule = getRule(lhs);

				// Create clauses
				if (symbol.isClass() && !symbol.isAbstract())
				{
					// Create constructor clause(s)
					expandConstructors(rule, symbol);
				}
				else
				{
					// Add non-constructor clause
					final Clause clause = new Clause(symbol);					
					if 
					(
						!rule.containsClause(clause)
						&& 
						(
							symbol.ludemeType() == LudemeType.Primitive 
							|| 
							symbol.ludemeType() == LudemeType.Predefined
							|| 
							!lhs.path().equals(symbol.path())
						)
						&&  // avoid repetition of LHS collections on RHS
						!(lhs.matches(symbol) && lhs.nesting() > 0) 
					)
					{
						rule.addToRHS(clause);
					}
					else
					{
						//System.out.println("- Failed test for generating clause for: " + symbol.name());
					}
				}
			}
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * @param lhs
	 * @return Rule with the specified LHS, creates new rule if necessary.
	 */
	GrammarRule getRule(final Symbol lhs)
	{
		for (final GrammarRule rule : getRules())
			if (rule.lhs().matches(lhs))
				return rule; // existing rule

		// Create new rule
		final GrammarRule rule = new GrammarRule(lhs);
		getRules().add(rule);

		return rule;
	}

	/**
	 * @param lhs
	 * @return Rule with the specified LHS, else null if not found.
	 */
	GrammarRule findRule(final Symbol lhs)
	{
		for (final GrammarRule rule : getRules())
			if (rule.lhs().matches(lhs))
				return rule;
		return null;
	}

	/**
	 * @param path
	 * @return Package with the specified path, else null if none.
	 */
	public PackageInfo findPackage(final String path)
	{
		for (final PackageInfo pack : packages)
			if (path.equalsIgnoreCase(pack.path()))
				return pack;
		return null;
	}

	//-------------------------------------------------------------------------

	/**
	 * Expand constructors for this concrete base class.
	 * 
	 * @param rule
	 * @param symbol
	 */
	void expandConstructors(final GrammarRule rule, final Symbol symbol)
	{
		if (symbol.hidden())
			return;

		final Class<?> cls = symbol.cls();
		if (cls == null)
			return;

		// Handle aliases here as well
		String alias = null;

		// From: http://tutorials.jenkov.com/java-reflection/annotations.html
		final Annotation[] classAnnotations = cls.getAnnotations();
		for (final Annotation annotation : classAnnotations)
		{
			if (annotation instanceof Alias)
			{
				final Alias anno = (Alias)annotation;
				alias = anno.alias();
			}
		}

		// Get list of either constructors or construct() methods 
		final List<Executable> ctors = new ArrayList<Executable>();
		
		// Get list of constructors
		ctors.addAll(Arrays.asList(cls.getConstructors()));

		// Get list of static construct() methods
		final Method[] methods = cls.getDeclaredMethods();
		for (final Method method : methods)
			if (method.getName().equals("construct") && Modifier.isStatic(method.getModifiers()))
				ctors.add(method);	

		for (final Executable ctor : ctors)
		{
			final List<ClauseArg> constructorArgs = new ArrayList<ClauseArg>();
			
			final Annotation[] ctorAnnotations = ctor.getAnnotations();
			final Annotation[][] annotations = ctor.getParameterAnnotations();
			final Type[] types = ctor.getGenericParameterTypes();
			final Parameter[] parameters = ctor.getParameters();

			boolean isHidden = false;
			for (int ca = 0; ca < ctorAnnotations.length; ca++)
			{
				final Annotation ann = ctorAnnotations[ca];

				if (ann.annotationType().getName().equals("annotations.Hide"))
					isHidden = true;
			}

			int prevOrType  = 0;  // 'or' type of previous item
			int prevAndType = 0;  // 'and' type of previous item
				
			int orGroup  = 0;
			int andGroup = 0;

			for (int n = 0; n < types.length; n++)
			{
				final Type type = types[n];
				final String typeName = type.getTypeName();
	
				// Args maintain own records of List<> and array[] nesting,
				// so find base symbol without collections.
				
				// **
				// ** Assume it is Ludeme type unless proven otherwise.
				// **
				
				final Symbol temp = new Symbol(LudemeType.Ludeme, typeName, alias, cls);  //null);
				temp.setNesting(0);
				
				final Symbol symbolP = findSymbolMatch(temp);
				if (symbolP == null)
				{
					//System.out.println("- No matching symbolP found for symbol: " + symbol.name());
					continue;
				}
				
				// Check for parameter name
				String label = (n < parameters.length && parameters[n].isNamePresent()) 
								? parameters[n].getName()
								: null;

				// Check for annotations
				boolean optional = false;
				boolean isNamed  = false;
				
				int orType  = 0;
				int andType = 0;

				for (int a = 0; a < annotations[n].length; a++)
				{
					final Annotation ann = annotations[n][a];
					
					if (ann.annotationType().getName().equals("annotations.Opt"))
						optional = true;

					if (ann.annotationType().getName().equals("annotations.Name"))
						isNamed = true;
										
					if (ann.annotationType().getName().equals("annotations.Or"))
						orType = 1;
					
					if (ann.annotationType().getName().equals("annotations.Or2"))
					{
						if (orType != 0)
							System.out.println("** Both @Or and @Or2 specified for label.");
						orType = 2;
					}

					if (ann.annotationType().getName().equals("annotations.And"))
						andType = 1;
					
					if (ann.annotationType().getName().equals("annotations.And2"))
					{
						if (andType != 0)
							System.out.println("** Both @And and @And2 specified for label.");
						andType = 2;
					}
				}
				
				final String actualParameterName = label;
				
				if (!isNamed)
					label = null;

				if (orType != 0 && orType != prevOrType)
					orGroup++;  // new consecutive 'or' group

				if (andType != 0 && andType != prevAndType)
					andGroup++;  // new consecutive 'and' group
				
				final ClauseArg arg = 	new ClauseArg
									  	(
									  		symbolP, actualParameterName, label, optional, 
									  		(orType  == 0 ? 0 : orGroup), 
									  		(andType == 0 ? 0 : andGroup)
									  	);

				int nesting = 0;
				for (int c = 0; c < typeName.length() - 1; c++)
					if (typeName.charAt(c) == '[' && typeName.charAt(c + 1) == ']')
						nesting++;
				if (nesting > 0)
					arg.setNesting(nesting);

				constructorArgs.add(arg);
				
				prevOrType  = orType;
				prevAndType = andType;
			}
				
			final Clause clause = new Clause(symbol, constructorArgs, isHidden);
			rule.addToRHS(clause);
		}
	}

	/**
	 * @param symbol
	 * @return Specified symbol, else null if not found.
	 */
	Symbol findSymbolMatch(final Symbol symbol)
	{
		for (final Symbol sym : symbols)
			if (sym.matches(symbol))
				return sym;
		return null;
	}

	//-------------------------------------------------------------------------

	/**
	 * Cross-reference subclass LHSs to clauses in superclass rules.
	 */
	void crossReferenceSubclasses()
	{
		for (final Symbol symbol : symbols)
		{
			if (!symbol.isClass())
				continue;

			if (symbol.hidden())
				continue;
			
			final Class<?> cls = symbol.cls();
			if (cls == null)
				continue;

			final Class<?> clsSuper = cls.getSuperclass();
			if (clsSuper == null)
				continue;

			final Symbol symbolSuper = findSymbolByPath(clsSuper.getName());
			if (symbolSuper == null)
				continue;

			final GrammarRule ruleSuper = findRule(symbolSuper);
			if (ruleSuper == null)
				continue;

			final Clause clause = new Clause(symbol);

			if (!ruleSuper.containsClause(clause))
				ruleSuper.addToRHS(clause);
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Add clauses for rules of overridden return types.
	 */
	void addReturnTypeClauses()
	{
		for (final Symbol symbol : symbols)
		{
//			final boolean isAdd = symbol.cls().getName().equals("game.functions.graph.operators.Add");
//			if (isAdd)
//				System.out.println("\n" + symbol.cls().getName());
			
			if (!symbol.isClass() || symbol.matches(symbol.returnType()))
				continue;  // not overridden

			if (symbol.hidden())
				continue;
			
			final GrammarRule rule = findRule(symbol.returnType());
			if (rule == null)
				continue;

			final Clause clause = new Clause(symbol);
			if (!rule.containsClause(clause))
			{
				rule.addToRHS(clause);
//				if (isAdd)
//					System.out.println("- Adding clause to rule: " + rule);
			}
		}
	}

	//-------------------------------------------------------------------------
	
	void addApplicationConstantsToRule()
	{
		// Find <int> symbol and rule
		Symbol symbolInt = null;
		for (final Symbol symbol : symbols)
			if (symbol.grammarLabel().equals("int"))
			{
				symbolInt = symbol;
				break;
			}
		if (symbolInt == null)
			throw new RuntimeException("Failed to find symbol for <int>.");

		final GrammarRule ruleInt = findRule(symbolInt);
		if (ruleInt == null)
			throw new RuntimeException("Failed to find <int> rule.");
		
		// Find each constant symbol and add to <int> rule
		for (int cs = 0; cs < ApplicationConstants.length; cs++)
			for (final Symbol symbol : symbols)
				if (symbol.grammarLabel().equals(ApplicationConstants[cs][0]))
				{
					ruleInt.addToRHS(new Clause(symbol));
					break;
				}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Link symbols and rules to packages.
	 */
	void linkToPackages()
	{
		// Cross-reference symbols to their packages
		for (final Symbol symbol : symbols)
			symbol.setPack(findPackage(symbol.notionalLocation()));

		// List rules per packages
		for (final GrammarRule rule : getRules())
		{
			final PackageInfo pack = rule.lhs().pack();
			
			// Hack to stop duplicate rules showing for primitives.
			//
			// TODO: Stop out why these redundant rules are being added in the first place.
			// CBB:  Does not affect operation, not urgent, fix when convenient.
			//
			if 
			(
				(
					rule.lhs().grammarLabel().equals("int")
					||
					rule.lhs().grammarLabel().equals("boolean")
					||
					rule.lhs().grammarLabel().equals("float")
				)
				&&
				rule.rhs().size() == 1
			)
				continue;
			
			if (pack != null)
				pack.add(rule);
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Set display order of rules and packages.
	 */
	void setDisplayOrder(final Symbol rootSymbol)
	{
		// Order packages in depth-first order from root package
		for (final PackageInfo pack : packages)
		{
			pack.listAlphabetically();
			prioritiseSuperClasses(pack);
			prioritisePackageClass(pack);
		}
		setPackageOrder(rootSymbol);
	}

	//-------------------------------------------------------------------------

	/**
	 * Order rules within each package so the base class comes first.
	 * @param pack
	 */
	static void prioritisePackageClass(final PackageInfo pack)
	{
		//final List<GrammarRule> rulesP = pack.rules();
		
		// First pass: Promote partial match (e.g. "player")
		final List<GrammarRule> promote = new ArrayList<GrammarRule>();
		for (int r = pack.rules().size()-1; r >= 0; r--)
		{
			final GrammarRule rule = pack.rules().get(r);

			if 
			(
				pack.path().contains(rule.lhs().grammarLabel()) 
				&& 
				rule.lhs().grammarLabel().length() >= 3  // avoid trivial matches such as "le" in "boolean"
			)
			{
				// Partial match found
				pack.remove(r);
				//rulesP.add(0, rule);
				promote.add(rule);
//				break;
//				System.out.println("Promoting rule " + rule.lhs().name() + " (A).");
			}
		}
		
		for (final GrammarRule rule : promote)
			pack.add(0, rule);
		
		// Second pass: Promote exact match (e.g. "players")
		promote.clear();
		for (int r = pack.rules().size()-1; r >= 0; r--)
		{
			final GrammarRule rule = pack.rules().get(r);

			if (pack.path().equals(rule.lhs().name()))
			{
				// Exact match found
				pack.remove(r);
				//rulesP.add(0, rule);
				promote.add(rule);
//				break;
//				System.out.println("Promoting rule " + rule.lhs().name() + " (B).");
			}
		}
		
		for (final GrammarRule rule : promote)
			pack.add(0, rule);
	}

	//-------------------------------------------------------------------------

	/**
	 * Prioritise rule order within packages so that superclasses come before
	 * subclasses.
	 * 
	 * @param pack
	 */
	static void prioritiseSuperClasses(final PackageInfo pack)
	{
		// Move symbol with same name as package to top of package list
		for (int n = 0; n < pack.rules().size(); n++)
		{
			final GrammarRule rule = pack.rules().get(n);
			if (rule.lhs().name().equalsIgnoreCase(pack.shortName()))
			{
				pack.remove(n);
				pack.add(0, rule);
			}
		}

		// Swap when collection of symbols is in wrong order
		for (int a = 0; a < pack.rules().size(); a++)
		{
			final GrammarRule ruleA = pack.rules().get(a);
			for (int b = a + 1; b < pack.rules().size(); b++)
			{
				final GrammarRule ruleB = pack.rules().get(b);
				if (ruleB.lhs().isCollectionOf(ruleA.lhs()))
				{
					pack.remove(b);
					pack.remove(a);
					pack.add(a, ruleB);
					pack.add(b, ruleA);
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------

	void linkDirectionsRules()
	{
		// Add clauses for <directionFacing>
		final Symbol symbolDirectionFacing = findSymbolByPath("game.util.directions.DirectionFacing");
		//symbolDirectionFacing.setUsedInDescription(true);
		symbolDirectionFacing.setUsedInGrammar(true);

		final GrammarRule ruleDirectionFacing = findRule(symbolDirectionFacing);

		final Symbol symbolAbsolute = findSymbolByPath("game.util.directions.AbsoluteDirection"); 
		//symbolAbsolute.setUsedInDescription(true);
		symbolAbsolute.setUsedInGrammar(true);
		
		final Symbol symbolRelative = findSymbolByPath("game.util.directions.RelativeDirection"); 
		//symbolRelative.setUsedInDescription(true);
		symbolRelative.setUsedInGrammar(true);
		
		final Symbol symbolDirections = findSymbolByPath("game.functions.directions.Directions");
		symbolDirections.setUsedInDescription(true);
		symbolDirections.setUsedInGrammar(true);

		final Symbol symbolIf = findSymbolByPath("game.functions.directions.If"); 
		symbolIf.setUsedInDescription(true);
		symbolIf.setUsedInGrammar(true);
				
		ruleDirectionFacing.addToRHS(new Clause(symbolAbsolute));
		ruleDirectionFacing.addToRHS(new Clause(symbolRelative));
		ruleDirectionFacing.addToRHS(new Clause(symbolDirections));
		ruleDirectionFacing.addToRHS(new Clause(symbolIf));
		
		// Add clauses for <direction>
		final Symbol symbolDirection = findSymbolByPath("game.util.directions.Direction");
		//symbolDirection.setUsedInDescription(true);
		symbolDirection.setUsedInGrammar(true);
	
		final GrammarRule ruleDirection = findRule(symbolDirection);

		ruleDirection.addToRHS(new Clause(symbolAbsolute));
		ruleDirection.addToRHS(new Clause(symbolRelative));
		ruleDirection.addToRHS(new Clause(symbolDirections));
		ruleDirection.addToRHS(new Clause(symbolIf));
	}
	
	//-------------------------------------------------------------------------

	void linkRegionRules()
	{
		final Symbol symbolRegion = findSymbolByPath("game.util.equipment.Region");
		final GrammarRule ruleRegion = findRule(symbolRegion);
		
		final Symbol symbolSites = findSymbolByPath("game.functions.region.sites.Sites");
		final GrammarRule ruleSites = findRule(symbolSites);
		
		for (final Clause clause : ruleRegion.rhs())
			ruleSites.addToRHS(clause);
		
		symbolRegion.setUsedInDescription(false);
		symbolSites.setUsedInDescription(true);
		
		// Remove <equipment.region> rule
		for (int r = getRules().size() - 1; r >= 0; r--)
		{
			final GrammarRule rule = getRules().get(r);
			if (rule.lhs().grammarLabel().equals("equipment.region"))
				getRules().remove(r);
		}
	}
	
	//-------------------------------------------------------------------------

	void handleDimFunctions()
	{
		// Find <dim> symbol and rule
		final Symbol symbolDim = findSymbolByPath("game.functions.dim.BaseDimFunction");
		symbolDim.setUsedInDescription(true);
		
		final GrammarRule ruleDim = findRule(symbolDim);
		
		// Find <int> symbol
		Symbol symbolInt = null;
		for (final Symbol symbol : symbols)
			if (symbol.grammarLabel().equals("int"))
			{
				symbolInt = symbol;
				break;
			}
		if (symbolInt == null)
			throw new RuntimeException("Failed to find symbol for <int>.");

		ruleDim.addToRHS(new Clause(symbolInt));
	}

	void handleGraphAndRangeFunctions()
	{
		final Symbol symbolGraph = findSymbolByPath("game.functions.graph.BaseGraphFunction");
		symbolGraph.setUsedInGrammar(true);
		symbolGraph.setUsedInDescription(true);

		final Symbol symbolRange = findSymbolByPath("game.functions.range.BaseRangeFunction");
		symbolRange.setUsedInGrammar(true);
		symbolRange.setUsedInDescription(true);
	}
	
	/**
	 * Remove class name from track step declarations. 
	 */
	void handleTrackSteps()
	{
//		final Symbol symbolTrackStep = findSymbolByPath("game.util.equipment.TrackStep");
//		final GrammarRule ruleTrackStep = findRule(symbolTrackStep);
//		
//		final Symbol symbolDim  = findSymbolByPath("game.functions.dim.BaseDimFunction");
//		//final GrammarRule ruleDim = findRule(symbolDim);
//		
//		final Symbol symbolDirn = findSymbolByPath("game.util.directions.Compass"); 
//		//final GrammarRule ruleDirn = findRule(symbolDirn);
//		
//		final Symbol symbolStep = findSymbolByPath("game.types.board.TrackStepType");
//		//final GrammarRule ruleStep = findRule(symbolStep);
//		
//		ruleTrackStep.clearRHS();
//		
//		ruleTrackStep.addToRHS(new Clause(symbolDim));
//		ruleTrackStep.addToRHS(new Clause(symbolDirn));
//		ruleTrackStep.addToRHS(new Clause(symbolStep));
	}

	//-------------------------------------------------------------------------

	/**
	 * Set package order for printing.
	 */
	void visitSymbols(final Symbol rootSymbol)
	{
		if (rootSymbol == null)
		{
			System.out.println("** GrammarWriter.visitSymbols() error: Null root symbol.");
			return;
		}

		final boolean isGame = rootSymbol.name().contains("Game");
		
		// Don't initialise values, as <directions> would not be picked up
//		// Prepare export queue, depth first from root
//		for (final Symbol symbol : symbols)
//		{
//			symbol.setVisited(false);
//			symbol.setDepth(Constants.UNDEFINED);
//
//			if (isGame)
//				symbol.setUsedInGrammar(false);
//			else
//				symbol.setUsedInMetadata(false);
//		}

		visitSymbol(rootSymbol, 0, isGame);
	}

	/**
	 * Visit symbols in depth first order and record packages in order.
	 * @param symbol
	 */
	void visitSymbol(final Symbol symbol, final int depth, final boolean isGame)
	{
		if (symbol == null)
			return; // no effect on which rules to show

		if (symbol.depth() == Constants.UNDEFINED)
			symbol.setDepth(depth);
		else if (depth < symbol.depth())
			symbol.setDepth(depth);  // overwrite existing depth with a shallower one
		
		if (symbol.visited())
			return;

		if (isGame)
		{
			//symbol.setUsedInDescription(true);
			symbol.setUsedInGrammar(true);
		}
		else
		{
			symbol.setUsedInMetadata(true);
		}
		
		symbol.setVisited(true);
		
		if (symbol.ludemeType() == LudemeType.Constant)
			return;

		if (symbol.rule() == null || symbol.rule().rhs() == null)
			return;  // probably redirected return type

		if (symbol.rule().rhs() == null)
			System.out.println("* Symbol with null expression: " + symbol.grammarLabel());  //symbol.name());

		// Don't add steps for abstract or hidden classes, which do not appear in the grammar
		final boolean isVisible = !symbol.isAbstract() && !symbol.hidden();
		final int nextDepth = depth + (isVisible ? 1 : 0);
		
		// Recurse to embedded symbols in RHS expression
		for (final Clause clause : symbol.rule().rhs())
		{
			visitSymbol(clause.symbol(), nextDepth, isGame);

			// Check for matching clause symbols
			for (final GrammarRule ruleC : getRules())
				if (ruleC.lhs().validReturnType(clause))
					visitSymbol(ruleC.lhs().returnType(), nextDepth, isGame);

			// **
			// ** TODO: Have more general arg matching, e.g. play() is easily missed.
			// **

			// Check for matching arg symbols
			if (clause.args() != null) // is a constructor
				for (final ClauseArg arg : clause.args())
				{
					visitSymbol(arg.symbol(), nextDepth, isGame);
					for (final GrammarRule ruleA : getRules())
						if (ruleA.lhs().validReturnType(arg))
							visitSymbol(ruleA.lhs().returnType(), nextDepth, isGame);
				}
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Set package order for printing.
	 */
	void setPackageOrder(final Symbol rootSymbol)
	{
		packageOrder.clear();

		if (rootSymbol == null)
		{
			System.out.println("** GrammarWriter.setPackageOrder() error: Null root symbol.");
			return;
		}

		// Prepare export queue, depth first from root
		for (final Symbol symbol : symbols)
			symbol.setVisited(false);
			
		setPackageOrderRecurse(rootSymbol);

		// Move utility packages to end of list
		final String[] packsToMove =
		{ 
			"game.functions", 
			"game.util", 
			"game.types", 
		};
		for (int pm = 0; pm < packsToMove.length; pm++)
			for (int p = packageOrder.size()-1; p >= 0; p--)
				if (packageOrder.get(p).path().contains(packsToMove[pm]))
				{
					// Move this package to the bottom of the list
					final PackageInfo packInfo = packageOrder.get(p);
					packageOrder.remove(p);
					packageOrder.add(packInfo);
				}
	}

	/**
	 * Visit symbols in depth first order and record packages in order.
	 * @param symbol
	 */
	void setPackageOrderRecurse(final Symbol symbol)
	{
		if (symbol == null || symbol.visited() || symbol.ludemeType() == LudemeType.Constant)
			return; // no effect on which rules to show

		symbol.setVisited(true);

		if (symbol.rule() == null || symbol.rule().rhs() == null)
			return;  // probably redirected return type

		final PackageInfo pack = symbol.pack();
		if (pack == null)
		{
			//System.out.println("* Null pack for symbol='" + symbol.toString() + "'.");
			return;  // probably String
		}

		int p;
		for (p = 0; p < packageOrder.size(); p++)
			if (packageOrder.get(p).path().equals(pack.path()))
				break;

		if (p >= packageOrder.size())
			packageOrder.add(pack); // add unvisited package to the list

		if (symbol.rule().rhs() == null)
		{
			System.out.println("* Symbol with null expression: " + symbol.grammarLabel());  //symbol.name());
		}

		// Recurse to embedded symbols in RHS expression
		for (final Clause clause : symbol.rule().rhs())
		{
			setPackageOrderRecurse(clause.symbol());

			// Check for matching clause symbols
			for (final GrammarRule ruleC : getRules())
				if (ruleC.lhs().validReturnType(clause))
				{
					setPackageOrderRecurse(ruleC.lhs().returnType());
				}

			// **
			// ** TODO: Have more general arg matching, e.g. play() is easily missed.
			// **

			// Check for matching arg symbols
			if (clause.args() != null) // is a constructor
				for (final ClauseArg arg : clause.args())
				{
					setPackageOrderRecurse(arg.symbol());
					for (final GrammarRule ruleA : getRules())
						if (ruleA.lhs().validReturnType(arg))
							setPackageOrderRecurse(ruleA.lhs().returnType());
				}
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Remove rules and clauses with redundant function and constant names.
	 */
	void removeRedundantFunctionNames()
	{
		for (final GrammarRule rule : getRules())
		{
			// Remove redundant function rules by turning off LHS
			for (int f = 0; f < getFunctions().length; f++)
				if (rule.lhs().grammarLabel().equalsIgnoreCase(getFunctions()[f][0]))
				{
					rule.lhs().setUsedInDescription(false);
					rule.lhs().setUsedInMetadata(false);
				}

			// Remove redundant clauses from rules
			for (int c = rule.rhs().size() - 1; c >= 0; c--)
			{
				final Clause clause = rule.rhs().get(c);
				for (int f = 0; f < getFunctions().length; f++)
					if (clause.symbol().grammarLabel().equalsIgnoreCase(getFunctions()[f][0]))
					{
						rule.removeFromRHS(c);
						break;
					}
			}
		}
	}

	//-------------------------------------------------------------------------

	void alphabetiseRuleClauses()
	{
		for (final GrammarRule rule : getRules())
			rule.alphabetiseClauses();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Removes duplicate clauses from rules, 
	 * For example, <int> ::= Off | Off
	 * 
	 * BE CAREFUL: Do not eliminate multiple constructors!
	 * 
	 */
	void removeDuplicateClauses()
	{
		for (final GrammarRule rule : getRules())
		{
			for (int ca = rule.rhs().size() - 1; ca >= 0; ca--)
			{
				final Clause clauseA = rule.rhs().get(ca);
				if (clauseA.isConstructor())
					continue;  // don't remove
				
				boolean doRemove = false;
				for (int cb = ca - 1; cb >= 0; cb--)
				{
					final Clause clauseB = rule.rhs().get(cb);
					if (clauseA.matches(clauseB))
					{
						doRemove = true;
						break;
					}
				}
				if (doRemove)
					rule.removeFromRHS(ca);
			}
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Filter out redundant rules for primitive wrapper classes
	 */
	void filterOutPrimitiveWrappers()
	{
		// Filter out trivial rules that map primitive filters to themselves
		for (final GrammarRule rule : getRules())
		{
			if 
			(
				rule.lhs().grammarLabel().equals("Integer") 
				&& 
				rule.rhs().size() == 1
				&&
				rule.rhs().get(0).symbol().grammarLabel().equals("Integer")
				||
				rule.lhs().grammarLabel().equals("Float") 
				&& 
				rule.rhs().size() == 1
				&&
				rule.rhs().get(0).symbol().grammarLabel().equals("Float")
				||
				rule.lhs().grammarLabel().equals("Boolean") 
				&& 
				rule.rhs().size() == 1
				&&
				rule.rhs().get(0).symbol().grammarLabel().equals("Boolean")
				||
				rule.lhs().grammarLabel().equals("String") 
				&& 
				rule.rhs().size() == 1
				&&
				rule.rhs().get(0).symbol().grammarLabel().equals("String")
			)
			{
				rule.lhs().setUsedInDescription(false);   // turn off so is not shown in grammar
				rule.lhs().setUsedInMetadata(false);  // turn off so is not shown in grammar
			}
		}

		// Replace primitive wrapper symbols on RHS with their primitive equivalents 
		Symbol symbolInt   = null;
		Symbol symbolFloat = null;
		Symbol symbolBoolean = null;
		Symbol symbolString = null;
		
//		Symbol symbolTrue  = null;
//		Symbol symbolFalse = null;

		for (final Symbol symbol : symbols)
		{
			if (symbol.grammarLabel().equals("int"))
				symbolInt = symbol;
			if (symbol.grammarLabel().equals("float"))
				symbolFloat = symbol;
			if (symbol.grammarLabel().equals("boolean"))
				symbolBoolean = symbol;
			if (symbol.grammarLabel().equals("String"))
				symbolString = symbol;
//			if (symbol.grammarLabel().equals("true"))
//				symbolTrue = symbol;
//			if (symbol.grammarLabel().equals("false"))
//				symbolFalse = symbol;
		}
		
		for (final GrammarRule rule : getRules())
			for (final Clause clause : rule.rhs())
				if (clause != null && clause.args() != null)
					for (final ClauseArg arg : clause.args())
					{
						if (arg.symbol().grammarLabel().equals("Integer"))
							arg.setSymbol(symbolInt);
						if (arg.symbol().grammarLabel().equals("Float"))
							arg.setSymbol(symbolFloat);
						if (arg.symbol().grammarLabel().equals("Boolean"))
							arg.setSymbol(symbolBoolean);
						if (arg.symbol().grammarLabel().equals("String"))
							arg.setSymbol(symbolString);
					}
		
//		// Add 'true' and 'false' constants to <boolean> rule
//		for (final GrammarRule rule : rules)
//			if (rule.lhs().grammarLabel().equals("boolean"))
//			{
//				rule.add(new Clause(symbolTrue));
//				rule.add(new Clause(symbolFalse));
//			}
		
//		// Remove rule for primitive wrappers
//		for (int r = rules.size() - 1; r >= 0; r--)
//		{
//			final GrammarRule rule = rules.get(r);
//			
//			//System.out.println("Rule for: " + rule.lhs().cls().getName());
//			
//			if 
//			(
//				rule.lhs().cls().getName().equals("java.lang.Integer")
//				||
//				rule.lhs().cls().getName().equals("java.lang.Boolean")
//				||
//				rule.lhs().cls().getName().equals("java.lang.Float")
//				||
//				rule.lhs().cls().getName().contains("Function")
//				||
//				rule.lhs().cls().getName().contains("Constant")
//			)
//			{
//				//System.out.println("** Removing rule for: " + rule.lhs().cls().getName());
//				rule.lhs().setUsedInGrammar(false);
//				rule.lhs().setUsedInDescription(false);
//				rule.lhs().setUsedInMetadata(false);
//
//				rules.remove(r);
//			}
//		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Instantiate enums in-place if they only have one value.
	 */
	void instantiateSingleEnums()
	{
		// Instantiate single enum values
		for (final GrammarRule rule : getRules())
			for (final Clause clause : rule.rhs())
				if (clause != null && clause.args() != null)
					for (int a = clause.args().size() - 1; a >= 0; a--)
					{
						final ClauseArg arg = clause.args().get(a);
						final Symbol symbol = arg.symbol();			
						if (symbol.cls() != null && symbol.cls().isEnum())
						{
							final GrammarRule ruleS = symbol.rule();
							if (ruleS != null && ruleS.rhs().size() == 1)
							{
								final Clause enumValue = ruleS.rhs().get(0);
								arg.setSymbol(enumValue.symbol());
								enumValue.symbol().setUsedInGrammar(true);
							}
						}
					}		
		
		// Remove enum rules with a single value (should have all been picked up by previous test)
		for (int r = getRules().size() - 1; r >= 0; r--)
		{
			final GrammarRule rule = getRules().get(r);
			if (rule.lhs().cls() != null && rule.lhs().cls().isEnum() && rule.rhs().size() == 1)
				getRules().remove(r);
		}
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return Returns a string containing the path abbreviation for each ludeme and the name of the ludeme.
	 */
	public String getFormattedSymbols()
	{
		String str = "";
		for (final Symbol s : symbols)
		{
			final String[] pathList = s.path().split("\\.");
			String strAbrev = "";
			for (int i = 0; i < pathList.length; i++)
			{
				strAbrev += pathList[i].charAt(0);
			}
			str += "\n" + strAbrev + " : " + s.toString().replace("<", "").replace(">", "");
		}
		return str;
	}	
	
	//-------------------------------------------------------------------------

	/**
	 * Export grammar to file.
	 * @param fileName
	 * @throws IOException
	 */
	public void export(final String fileName) throws IOException
	{
		final File file = new File(fileName);
		if (!file.exists())
			file.createNewFile();

		try
		(
			final FileWriter fw = new FileWriter(file.getName(), false);
			final BufferedWriter writer = new BufferedWriter(fw);
		)
		{
			final String str = toString();
			writer.write(str);
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Tidy up final result, including replacing function names with return types.
	 */
	private void tidyUpFormat()
	{
		// Replace remaining function names
		for (int f = 0; f < getFunctions().length; f++)
		{
			String name = getFunctions()[f][0];
			name = name.substring(0, 1).toLowerCase() + name.substring(1);  // lowerCamelCase keyword
		
			for (final GrammarRule rule : rules)
			{
				String label = rule.lhs().grammarLabel();
				if (label.contains(name))
				{
					label = label.replace(name, getFunctions()[f][1]);
					rule.lhs().setGrammarLabel(label);
				}
			}			
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Set flag for ludemes used in the grammar.
	 */
	private void setUsedInGrammar()
	{
		// Start with all symbols that can occur in descriptions and metadata
		for (final Symbol symbol : symbols)
		{
			if 
			(
				symbol.usedInDescription() 
//				|| 
//				symbol.usedInMetadata()
			)
			{
				symbol.setUsedInGrammar(true);
				continue;
			}
		}	
		
		// Iteratively find rules that use them
		boolean didUpdate = false;
		do
		{
			didUpdate = false;
			for (final GrammarRule rule : rules)
			{
				if (rule.lhs().usedInGrammar())
					for (final Clause clause : rule.rhs())
						if (!clause.symbol().usedInGrammar())
						{
							clause.symbol().setUsedInGrammar(true);
							didUpdate = true;
						}
			}	
		} while (didUpdate);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Set flag for ludemes used in game descriptions.
	 * These will have instantiations in RHS rule clauses. 
	 */
	private void setUsedInDescription()
	{
		for (final GrammarRule rule : rules)
			for (final Clause clause : rule.rhs())
			{
				final Symbol symbol = clause.symbol();
				
				if (!symbol.usedInGrammar())
					continue;  // don't include metadata
				
				if 
				(
					clause.isConstructor()
					||
					symbol.isTerminal()
				)
					symbol.setUsedInDescription(true);
				
				if (clause.args() != null)
				{
					// Also check clause args for enum constants etc.
					for (final ClauseArg arg : clause.args())
					{
						final Symbol argSymbol = arg.symbol();
					
						if (!argSymbol.usedInGrammar())
							continue;  // don't include metadata
						
						if (argSymbol.isTerminal())
							argSymbol.setUsedInDescription(true);
					}
				}
			}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Set flag for ludemes used in metadata.
	 */
	private void setUsedInMetadata()
	{
		for (final Symbol symbol : symbols)
		{	
//			if (symbol.cls() != null && MetadataItem.class.isAssignableFrom(symbol.cls()))
//			{
//				System.out.println("Metatada symbol path: " + symbol.path());
//				symbol.setUsedInMetadata(true);
//			}
			
			// Avoid dependency on Core
			
			if (symbol.path().contains("metadata"))
				symbol.setUsedInMetadata(true);
		}
		
		for (final GrammarRule rule : rules)
			for (final Clause clause : rule.rhs())
			{
				final Symbol symbol = clause.symbol();
				
				if (!symbol.usedInMetadata())
					continue;  // don't include grammar
				
				if (clause.args() != null)
				{
					// Check metadata clause args
					for (final ClauseArg arg : clause.args())
					{
						final Symbol argSymbol = arg.symbol();
						argSymbol.setUsedInMetadata(true);
					}
				}
			}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Set ludeme type for each symbol.
	 */
	private void setLudemeTypes()
	{
		for (final Symbol symbol : symbols)
		{
			final Class<?> cls = symbol.cls();
			
			if (cls == null)
			{
				// Symbol has no class
				System.out.println("Symbol has no class: " + symbol);
				symbol.setLudemeType(null);
				continue;
			}
		
			if (cls.isEnum())
			{
				symbol.setLudemeType(LudemeType.Constant);
			}
			else if 
			(
				cls.getSimpleName().equals("String") 
				|| 
				cls.getSimpleName().equals("Integer") 
				|| 
				cls.getSimpleName().equals("Float") 
				|| 
				cls.getSimpleName().equals("Boolean")
			)
			{
				symbol.setLudemeType(LudemeType.Predefined);
			}
			else if 
			(
				cls.getSimpleName().equals("int") 
				|| 
				cls.getSimpleName().equals("float") 
				|| 
				cls.getSimpleName().equals("boolean") 
			)
			{
				symbol.setLudemeType(LudemeType.Primitive);
			}
			else
			{
				// Must be a ludeme class
				if (cls.isInterface())
				{
					// Assume is structural rule that links subrules but is never instantiated
					//System.out.println("** Is an interface: " + this);
					symbol.setLudemeType(LudemeType.Structural);
				}
				else
				{
					// Check for super ludeme
					final Constructor<?>[] constructors = cls.getConstructors();
					if (constructors.length == 0)
					{
						// No constructors: probably a super ludeme
						//System.out.println("** No constructors found for: " + this);
						symbol.setLudemeType(LudemeType.SuperLudeme);
					}
					else
					{
						symbol.setLudemeType(LudemeType.Ludeme);
					}
				}
			}
		}
		
		// Overwrite ludemes that appear in grammar but not descriptions as structural
		for (final GrammarRule rule : rules)
		{
			final Symbol symbol = rule.lhs();			
			if 
			(
				(symbol.usedInGrammar() || symbol.usedInMetadata())  
				&& 
				!symbol.usedInDescription()
			)
				symbol.setLudemeType(LudemeType.Structural);
		}
	
//		Class<?> clsT = null;
//		try
//		{
//			clsT = Class.forName("game.types.play.GravityType");
//		} catch (ClassNotFoundException e)
//		{
//			e.printStackTrace();
//		}
//		System.out.println(clsT);
//		System.out.println("Is enum: " + clsT.isEnum());
		
		// Check for incorrectly labelled enum types
		for (final Symbol symbol : symbols)
		{
			final Class<?> cls = symbol.cls();
			if (cls.isEnum() && symbol.ludemeType() == LudemeType.Constant)
			{
				// Is classified as an enum constant
//				System.out.println("Enum class " + symbol.token() + " is type: " + symbol.ludemeType());		
				
				// Check if is actually a constant...
				boolean isConstant = false;
				final Object[] constants = cls.getEnumConstants();
				for (final Object obj : constants)
					if (obj.toString().equals(symbol.token()))
					{
//						System.out.println("- Constant obj: " + obj.toString());
						isConstant = true;
						break;
					}
				
				if (!isConstant)
				{
					// Correct this mistakenly classified enum class
					symbol.setLudemeType(LudemeType.Ludeme);
					//symbol.setSymbolType(SymbolType.Class);
				}
			}
		}
		
		findSubludemes();
	}
	
//	/**
//	 * Find subludemes. 
//	 * These will be be classes in the subfolder of a superludeme (or below)
//	 * that are not in the grammar or game descriptions.
//	 */
//	private void findSubludemes()
//	{
//		// Find all super ludemes and store package
//		final Map<String, Symbol> supers = new HashMap<>();
//		for (final Symbol symbol : symbols)
//			if (symbol.ludemeType() == LudemeType.SuperLudeme)
//			{
//				String path = symbol.path();
//				path = path.substring(0, path.lastIndexOf('.'));
//				supers.put(path, symbol);
//			}
//		
//		// Find all ludeme classes that do not appear in grammar 
//		// and are in a superludeme package (or below).
//		for (final Symbol symbol : symbols)
//		{
//			if (symbol.ludemeType() != LudemeType.Ludeme)
//				continue;  // can't be a subludeme
//			
//			if (symbol.usedInGrammar() || symbol.usedInDescription())
//				continue;  // can't be a subludeme
//				
//			if (symbol.name().contains("Type"))
//			{
//				// is parameter type class
//				symbol.setLudemeType(LudemeType.Structural);
//				continue;  
//			}
//			
//			for (final String superPath : supers.keySet())
//				if (symbol.path().contains(superPath))
//				{
//					symbol.setLudemeType(LudemeType.SubLudeme);
//					break;
//				}
//		}
//	}

	/**
	 * Find subludemes. 
	 * These will be be classes in the subfolder of a superludeme (or below)
	 * that are not in the grammar or game descriptions.
	 */
	private void findSubludemes()
	{
		// Find all super ludemes and store package
		final Map<String, Symbol> supers = new HashMap<>();
		for (final Symbol symbol : symbols)
			if (symbol.ludemeType() == LudemeType.SuperLudeme)
			{
				String path = symbol.path();
				path = path.substring(0, path.lastIndexOf('.'));
				supers.put(path, symbol);
			}
		
		// Find all ludeme classes that do not appear in grammar 
		// and are in a superludeme package (or below).
		for (final Symbol symbol : symbols)
		{
			if (symbol.ludemeType() != LudemeType.Ludeme)
				continue;  // can't be a subludeme
			
			if (symbol.usedInGrammar() || symbol.usedInDescription())
				continue;  // can't be a subludeme
				
			if (symbol.name().contains("Type"))
			{
				// is parameter type class
				symbol.setLudemeType(LudemeType.Structural);
				
//				if (symbol.name().equalsIgnoreCase("isLineType"))
//					System.out.println("IsLineType subludeme found...");
				
				// Catch super ludeme parameter enum types
				for (final String superPath : supers.keySet())
					if (symbol.path().contains(superPath))
						symbol.setSubLudemeOf(supers.get(superPath));
				
				continue;  
			}

			for (final String superPath : supers.keySet())
				if (symbol.path().contains(superPath))
				{
					symbol.setLudemeType(LudemeType.SubLudeme);
					symbol.setSubLudemeOf(supers.get(superPath));
					break;
				}
		}
		
		// Now catch super ludeme enum parameter types
		for (final Symbol symbol : symbols)
		{
			if (symbol.ludemeType() != LudemeType.Ludeme)
				continue;  // can't be a subludeme
							
			if (!symbol.name().contains("Type"))
				continue;  // not a super ludeme enum parameter type
			
			for (final String superPath : supers.keySet())
				if (symbol.path().contains(superPath))
				{
					if (symbol.usedInGrammar() || symbol.usedInDescription())
						symbol.setLudemeType(LudemeType.Structural);
					symbol.setSubLudemeOf(supers.get(superPath));
				}
			}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Set ludeme type for each symbol.
	 */
	private void setAtomicLudemes()
	{
		// public static final String[][] Primitives =
		// {
		// 	   { "int",  "game.functions.ints" },
		// };
		//	
		// /** Predefined symbols (known data types) with notional and actual package names, and keyword. */
		// public static final String[][] Predefined =
		// {
		//	   { "java.lang.Integer", "game.functions.ints", "java.lang", "int" },
		// };
		//	
		// /** Function names and associated constants. */
		// private final String[][] Functions =
		// { 
		//	   { "IntFunction", "int" },
		//	   { "IntConstant",	"int" },
		// };
		//	
		// /** Application constants. */
		// public final static String[][] ApplicationConstants =
		// { 
		// 	   { "Off", "int", "global", ("" + Constants.OFF) },
		// };

		
		for (final Symbol symbol : symbols)
		{
			// 1. Set symbol as its own atomic symbol (unless proven otherwise)
			symbol.setAtomicLudeme(symbol);  
			
			// 2. Primitives already remap to correct atomic ludeme
			// 	  Do nothing.
			
			// 3. Remap predefined types
			if (symbol.name().equals("String"))
				continue;  // special case
			
			boolean found = false;
			for (int n = 0; n < Predefined.length; n++)
				if (symbol.cls().getName().equals(Predefined[n][0]))
				{
					// Is a predefined type
//					System.out.println("Predefined type " + symbol.name() + "...");
//					System.out.println("- return type is " + symbol.returnType());
					
					final List<Symbol> to = this.symbolsByName.get(Predefined[n][3]);
					if (to != null && to.size() >= 1)
					{		
//						System.out.println(to.size( ) + " symbols found:");
//						for (final Symbol st : to)
//							System.out.println("-- " + st.name());

						symbol.setAtomicLudeme(to.get(0));
						found = true;
						break;
					}
					
					//System.out.println("- return type is " + symbol.returnType());
				}
			if (found)
				continue;

			// 4. Remap functions
			found = false;
			for (int n = 0; n < Functions.length; n++)
				if (symbol.name().equals(Functions[n][0]))
				{
					// Is a function
//					System.out.println("Function type " + symbol.name() + "...");
//					System.out.println("- return type is " + symbol.returnType());
					
					symbol.setAtomicLudeme(symbol.returnType());
					found = true;
					break;
				}
			if (found)
				continue;
			
			// 5. Remap application constants
			found = false;
			for (int n = 0; n < ApplicationConstants.length; n++)
				if 
				(
					symbol.name().equals(ApplicationConstants[n][0]) 
					&& 
					symbol.returnType().name().equals("int")
				)
				{
					// Is a function
//					System.out.println("Application constant " + symbol.name() + "...");
//					System.out.println("- return type is " + symbol.returnType());

					final List<Symbol> to = this.symbolsByName.get(ApplicationConstants[n][0]);
					for (final Symbol st : to)
					{
//						System.out.println("-- " + st.name() + " (" + st.cls().getName() + ")");
						
						if (!st.cls().getName().equals("int"))
							continue;  // only match to int type
							
//						System.out.println("* Match *");
						
						symbol.setAtomicLudeme(st);
						found = true;
						break;
					}
				}
			if (found)
				continue;
		}
	}	
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return List of ludemes used in grammar, with relevant info.
	 */
	public List<LudemeInfo> ludemesUsed()
	{
		// **
		// ** Note: Assumes that simplest forms of ludemes are listed first!
		// **
		// **       e.g. boolean, Boolean, BooleanConstant and BooleanFunction
		// **            will all reduce to first occurrence (boolean).
		// **
		// ** But different OS and/or Java version may list items in different order!
		// **
		final List<LudemeInfo> ludemesUsed = new ArrayList<>();
		for (final Symbol symbol : symbols)
		{
//			if (symbol.path().equals("game.functions.booleans.math.Equals"))
//				System.out.println("Equals...");
			
			if (!symbol.usedInGrammar())
			{
				//System.out.println("Skipping: " + symbol.token() + " (" + symbol.cls().getName() + ")");
				continue;
			}
			
			if (symbol.usedInMetadata() && !symbol.name().equals("String"))
				continue;

//			if (symbol.path().equals("game.functions.booleans.math.Equals"))
//				System.out.println("A");

			//symbol.usedInMetadata()
			//||
			//symbol.ludemeType() == LudemeType.SubLudeme
			//|| 
			//IncludeInGrammar.class.isAssignableFrom(symbol.cls())
			
			// Check if symbol class has @Hide annotation
//			boolean isHide = false;
//			final Annotation[] annotations = symbol.cls().getAnnotations();
//			for (int a = 0; a < annotations.length && !isHide; a++)
//				if (annotations[a].annotationType().getName().equals("annotations.Hide"))
//					isHide = true;
//			if (isHide)
			if (symbol.hidden())
			{
				//System.out.println("Skipping hidden ludeme class: " + symbol.name());
				continue;
			}

			if 
			(
				symbol.name().equals("Integer")
				||
				symbol.name().equals("Boolean")
				||
				symbol.name().equals("Float")
			)
				continue;
			
//			if (symbol.path().equals("game.functions.booleans.math.Equals"))
//				System.out.println("B");

			if (symbol.ludemeType() != LudemeType.Constant)
			{				
				// Check whether symbol is already present.
				//
				// We don't want to do this for constants, as we can
				// legitimately have more than one constant with the
				// same grammar label, e.g. All in RelationType, RoleType, etc. 
				//
				boolean present = false;
				for (final LudemeInfo used : ludemesUsed)
					//if (used.symbol().token().equals(symbol.token()))
					if (used.symbol().grammarLabel().equals(symbol.grammarLabel()))
					{
						present = true;
						break;
					}
				
				if (present)
					continue;  // don't add duplicates, e.g. boolean/Boolean/BooleanConstant/BooleanFunction, etc.
			}
			
//			if (symbol.path().equals("game.functions.booleans.math.Equals"))
//				System.out.println("C");

			final LudemeInfo ludeme = new LudemeInfo(symbol);
			
			// TODO: Add relevant info here: database id, JavaDoc help, etc.
			
			ludemesUsed.add(ludeme);
		}
		
//		System.out.println("\n==================================\nLudemes used:");
//		for (final LudemeInfo li : ludemesUsed)
//			System.out.println(li.symbol().info());
		
		return ludemesUsed;
	}
	
	//-------------------------------------------------------------------------

//	/**
//	 * @return Set containing class names of all official ludemes.
//	 */
//	public Set<String> ludemeNamesUsed()
//	{
//		final Set<String> set = new HashSet<String>();
//		
//		final List<LudemeInfo> ludemesUsed = ludemesUsed();
//		for (final LudemeInfo ludeme : ludemesUsed)
//			set.add(ludeme.symbol().cls().getName());
//			
//		return set;
//	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Find ancestors of each symbol.
	 */
	private void findAncestors()
	{
		// Make symbol superclasses ancestors
		for (final Symbol symbol : symbols)
		{
			Class<?> parent = symbol.cls();
			while (true)
			{
				parent = parent.getSuperclass();
				if (parent == null)
					break;
				
				final Symbol parentSymbol = findSymbolByPath(parent.getName());
				if (parentSymbol == null)
					break;
				
				symbol.addAncestor(parentSymbol);
				
				//System.out.println("Adding ancestor " + parentSymbol.name() + " to " + symbol.name() + ".");
			}
		}

		// Set return type as ancestor (if not already present).
		// This will catch enums in super ludeme enum parameter types, but not all ancestors of parent.
		for (final Symbol symbol : symbols)
		{
			final Symbol parentSymbol = symbol.returnType();
			
			if (parentSymbol == null)
				continue;
			
			if (parentSymbol.path().equals(symbol.path()))
				continue;  // symbol returns its own type
			
			// Add parent symbol and its ancestors to current symbol
			symbol.addAncestor(parentSymbol);
			symbol.addAncestorsFrom(parentSymbol);
		}
		
		// Make super ludemes ancestors of their enum parameter types
		for (final Symbol symbol : symbols)
		{
			final Symbol parentSymbol = symbol.subLudemeOf();
			if (parentSymbol != null)
			{
				// Add parent symbol and its ancestors to current symbol
				symbol.addAncestor(parentSymbol);
				symbol.addAncestorsFrom(parentSymbol);  // do we want to include ancestors of super ludeme?
			}
		}
		
		// Set return type as ancestor (if not already present).
		// Repeat to catch all ancestors of enums in super ludeme enum parameter types.
		for (final Symbol symbol : symbols)
		{
			final Symbol returnSymbol = symbol.returnType();
			
			if (returnSymbol == null)
				continue;
			
			if (returnSymbol.path().equals(symbol.path()))
				continue;  // symbol returns its own type
			
			// Need to get actual symbol from the master list, 
			// as returnType() may be a duplicate (don't know why).
			final Symbol parentSymbol = findSymbolByPath(returnSymbol.path());  
			
			// Add parent symbol and its ancestors to current symbol
			symbol.addAncestor(parentSymbol);
			symbol.addAncestorsFrom(parentSymbol);
		}
			
//		final Symbol is = findSymbolByPath("game.functions.booleans.is.Is");
//		final Symbol isLineType = findSymbolByPath("game.functions.booleans.is.IsLineType");
//		final Symbol line = findSymbolByPath("game.functions.booleans.is.IsLineType.Line");
//		System.out.println("Line return type: " + line.returnType());
//				
//		System.out.println("Ancestors of Is: " + is.ancestors());
//		System.out.println("Ancestors of IsLineType: " + isLineType.ancestors());
//		System.out.println("Ancestors of Line: " + line.ancestors());
//	
//		System.out.println("IsLineType is type: " + isLineType.ludemeType());
//		System.out.println("IsLineType is subludeme of: " + isLineType.subLudemeOf());
//		
//		System.out.print("Paths for Is are:");
//		for (final Symbol symbol : symbolsByName("Is"))
//			System.out.println(" " + symbol.path());
//		System.out.println();
//		
//		System.out.print("Paths for IsLineType are:");
//		for (final Symbol symbol : symbolsByName("IsLineType"))
//			System.out.println(" " + symbol.path());
//		System.out.println();
//		
//		System.out.print("Paths for Line are:");
//		for (final Symbol symbol : symbolsByName("Line"))
//			System.out.println(" " + symbol.path());
//		System.out.println();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Symbol details for debugging.
	 */
	public String symbolDetails()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("\n+++++++++++++++++++ SYMBOLS ++++++++++++++++++++++\n");
		for (final Symbol symbol : symbols)
			sb.append(symbol.info() + "\n");
	
		sb.append("\n\n++++++++++++++++++++ RULES +++++++++++++++++++++++\n");
		for (final GrammarRule rule : getRules())
			sb.append
			(
				(rule.lhs().usedInGrammar() ? "g" : "~") + 
				(rule.lhs().usedInDescription() ? "d" : "~") + 
				(rule.lhs().usedInMetadata() ? "m" : "~") + 
				" " + rule +
				" [" + rule.lhs().cls().getName() + "] " +
				"\n"
			);
		
		FileHandling.saveStringToFile(sb.toString(), "symbol-details.txt", "");
		return(sb.toString());
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return List of grammar rules.
	 */
	public List<GrammarRule> getRules() 
	{
		return rules;
	}

	/**
	 * @return Function labels.
	 */
	public String[][] getFunctions() 
	{
		return Functions;
	}
	
	//-------------------------------------------------------------------------

	public String aliases()
	{
		final StringBuilder sb = new StringBuilder();
		
		for (final Symbol symbol : symbols)
			if (symbol.hasAlias())
				sb.append(symbol.name() + " (" + symbol.path() + ") has alias: " + symbol.token() + "\n");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		for (final PackageInfo pack : packageOrder)
			sb.append(pack.toString());
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------

}
