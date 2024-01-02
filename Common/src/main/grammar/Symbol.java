package main.grammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import main.Constants;

//-----------------------------------------------------------------------------

/**
 * Symbol within the grammar, either: 
 * 1. Primitive  : primitive data type (terminal).
 * 2. Predefined : predefined utility class, e.g. BitSet.
 * 3. Constant   : enum constant (terminal). 
 * 4. Class      : denoted by <name> (non-terminal). 
 * 
 * @author cambolbro
 */
public class Symbol 
{
	//-------------------------------------------------------------------------

	/**
	 * Types of ludemes implemented. 
	 */
	public static enum LudemeType
	{
		/** Standard ludeme class, e.g. (from ...). */
		Ludeme,
		
		/** Super ludeme class, e.g. (move Add ...). */
		SuperLudeme,
		
		/** Sub ludeme class implements a case of super ludeme, e.g. MoveAdd. */
		SubLudeme,
		
		/** Appears in the grammar (as rule?) but never instantiated in descriptions. */
		Structural,

		/** Enum constant, e.g. Orthogonal. */
		Constant,
		
		/** Predefined data type and wrapper classes, e.g. String, Integer, etc. */
		Predefined,
		
		/** Primitive data types, e.g. int, float, boolean, etc. */
		Primitive,		
	}
		
	/** Ludeme type of this symbol. */
	private LudemeType ludemeType;

	//---------------------------------------------------------
	
	/** Symbol class name without decoration. */
	private String name = "";

	/** Path for loading associated class. */
	private String path = "";
	
	/** Keyword in lowerCamelCase form, derived from name. */
	private String token = "";
	
	/** 
	 * Unique name in grammar, made from keyword + minimal necessary scoping from package names.
	 * e.g. moves.if, ints.math.if, booleans.math.if, etc.
	 */
	private String grammarLabel = null;

//	/** Symbol name as it appears in descriptions, without scope. */
//	private String nameLowerCamelCase = "";

	/** Location of file. For Primitives and Predefined classes, this is
	 *  a notional package for ordering types logically in code base. */
	private String notionalLocation = "";
	
	private final boolean hasAlias;
	
	//---------------------------------------------------------

	/** Whether symbol describes an abstract class. */
	private boolean isAbstract = false;

	/** Return type based on "eval()" method, else lhs if Class and no "eval()". */
	private Symbol returnType = null;
	
	/** Whether symbol should be hidden from grammar, e.g. a behind-the-scenes support class. */
	private boolean hidden = false;
	
//	/** Whether the symbol name should not be shown in the constructor, e.g. ItemCount(). */ 
//	private boolean hiddenName = false;
	
	/** Degree of array nesting: 0=none, 1=[], 2=[][], 3=[][][], etc. */
	private int nesting = 0;
	
	//---------------------------------------------------------

	/** 
	 * Whether symbol occurs anywhere in the current grammar. 
	 * This includes game logic, metadata and implied <rules> that don't instantiate.
	 */
	private boolean usedInGrammar = false;
	
	/** 
	 * Whether symbol can occur in a .lud game description (game logic part only). 
	 * Must be an instantiable RHS clause. 
	 */
	private boolean usedInDescription = false;
	
	/** Whether symbol can occur in the metadata section. */
	private boolean usedInMetadata = false;

	//---------------------------------------------------------

	/** Whether symbol has been visited (avoid infinite loops during grammar generation). */
	private boolean visited = false;
	
	/** Depth in grammar hierarchy. */
	private int depth = Constants.UNDEFINED;
	
	//---------------------------------------------------------

	/** Production rule with this symbol as LHS (null if terminal). */
	private GrammarRule rule = null;

	/** Package that this symbol belongs to. */
	private PackageInfo pack = null;

	/** Class associated with this symbol (null if not a class). */
	private final Class<?> cls;  // = null;
	
	/** List of ancestors in class hierarchy. */
	private final List<Symbol> ancestors = new ArrayList<>();
	
	/** Super ludeme that this symbol is a subludeme of (if any). */
	private Symbol subLudemeOf = null;
	
	/**
	 * The "official" atomic ludeme that represents this symbol in the database.
	 * e.g. int, Integer, IntFunction, IntConstant all map to the same ludeme (class.Integer). 
	 */
	private Symbol atomicLudeme = null;
	
	//-------------------------------------------------------------------------

	/**
	 * Default constructor.
	 * @param type
	 * @param path
	 * @param alias
	 * @param cls
	 */
	public Symbol
	(
		final LudemeType type, final String path, final String alias, final Class<?> cls
	)
	{
		this.ludemeType = type;
		this.path = new String(path);
		this.cls  = cls;
		this.hasAlias = (alias != null && alias != "");
		
		extractPackagePath();
		extractName();
		deriveKeyword(alias);
		
		grammarLabel = new String(token);  // lowerCamelCase version?

//		if (path.equals("game.functions.ints.IntFunction"))
//		{		
//			System.out.println("Symbol(): name=" + name + ", keyword=" + token + ", grammarLabel=" + grammarLabel);
//		}
//		System.out.println("\nname: " + name);
//		System.out.println("path: " + path);
//		System.out.println("notionalLocation: " + notionalLocation);
	}

	/**
	 * Constructor for Constant types.
	 * @param type
	 * @param path
	 * @param alias
	 * @param notionalLocation 
	 * @param cls
	 */
	public Symbol
	(
		final LudemeType type, final String path, final String alias, 
		final String notionalLocation, final Class<?> cls
	)
	{
		this.ludemeType = type;
		this.path = new String(path);
		this.notionalLocation = new String(notionalLocation);
		this.cls = cls;

		this.hasAlias = (alias != null && alias != "");
		
		extractName();
		deriveKeyword(alias);
		
		grammarLabel = new String(name);
	}

	/**
	 * Copy constructor.
	 * @param other
	 */
	public Symbol(final Symbol other)
	{
//		deepCopy(other);
//		symbolType = other.symbolType;
		ludemeType = other.ludemeType;
		name       = new String(other.name);
		path       = new String(other.path);
		token 	   = new String(other.token);
		
		hasAlias = other.hasAlias;
		
//		nameLowerCamelCase = new String(other.nameLowerCamelCase);
		grammarLabel = new String(other.grammarLabel);

		notionalLocation = new String(other.notionalLocation);

		isAbstract = other.isAbstract;
		returnType = other.returnType;
		
//		isList     = other.isList;
		nesting    = other.nesting;
		
		usedInGrammar     = other.usedInGrammar;
		usedInDescription = other.usedInDescription;
		usedInMetadata    = other.usedInMetadata;

		visited = other.visited;
		
		rule = other.rule;
		pack = other.pack;

		cls  = other.cls;
	}
	
	//-------------------------------------------------------------------------

//	/**
//	 * @return Symbol type.
//	 */
//	public SymbolType symbolType() 
//	{
//		return symbolType;
//	}
//
//	/**
//	 * @param type Symbol type to set.
//	 */
//	public void setSymbolType(final SymbolType type) 
//	{
//		symbolType = type;
//	}

	/**
	 * @return Ludeme type.
	 */
	public LudemeType ludemeType() 
	{
		return ludemeType;
	}

	/**
	 * @param type Ludeme type to set.
	 */
	public void setLudemeType(final LudemeType type) 
	{
		ludemeType = type;
	}
		
	/**
	 * @return Element (class) name.
	 */
	public String name() 
	{
		return name;
	}

	/**
	 * @return Absolute path.
	 */
	public String path() 
	{
		return path;
	}

	/**
	 * @return Keyword in lowerCamelCase form.
	 */
	public String token() 
	{
		return token;
	}

	public void setToken(final String word)
	{
		token = word;
	}
	
	/**
	 * @return Unique label within the grammar (for classes).
	 */
	public String grammarLabel()
	{
		return grammarLabel;
	}

//	/**
//	 * @return Symbol name as it appears in grammar, without scope.
//	 */
//	public String nameLowerCamelCase()
//	{
//		return nameLowerCamelCase;
//	}
	
	public void setGrammarLabel(final String gl)
	{
		grammarLabel = new String(gl);
	}
	
	public boolean hasAlias()
	{
		return hasAlias;
	}
	
	/**
	 * @return Package path.
	 */
	public String notionalLocation() 
	{
		return notionalLocation;
	}
	
	/**
	 * @return Whether this element is based on an abstract class.
	 */
	public boolean isAbstract() 
	{
		return isAbstract;
	}

	/**
	 * @param val
	 */
	public void setIsAbstract(final boolean val) 
	{
		isAbstract = val;
	}
	
	/**
	 * @return Whether symbol should be hidden from grammar.
	 */
	public boolean hidden() 
	{
		return hidden;
	}

	/**
	 * @param val
	 */
	public void setHidden(final boolean val) 
	{
		hidden = val;
	}
	
//	/**
//	 * @return Whether symbol name should not be shown in constructor.
//	 */
//	public boolean hiddenName() 
//	{
//		return hiddenName;
//	}
//
//	public void setHiddenName(final boolean val) 
//	{
//		hiddenName = val;
//	}

	/**
	 * @return Return type based on "apply()" method, else lhs if Class and no "apply()".
	 */
	public Symbol returnType()
	{
		return returnType;
	}
	
	/**
	 * @param symbol
	 */
	public void setReturnType(final Symbol symbol)
	{
		returnType = symbol;
	}
	
	/**
	 * @return Degree of nesting (array depth).
	 */
	public int nesting()
	{
		return nesting;
	}
	
	/**
	 * @param val
	 */
	public void setNesting(final int val)
	{
		nesting = val;
	}
	
	/**
	 * @return Whether this symbol is used anywhere in the grammar.
	 */
	public boolean usedInGrammar() 
	{
		return usedInGrammar;
	}

	/**
	 * @param value Whether this symbol is used anywhere in the grammar.
	 */
	public void setUsedInGrammar(final boolean value) 
	{
		usedInGrammar = value;
	}

	/**
	 * @return Whether this symbol can occur in game descriptions.
	 */
	public boolean usedInDescription() 
	{
		return usedInDescription;
	}

	/**
	 * @param value Whether this symbol can occur in game description.
	 */
	public void setUsedInDescription(final boolean value) 
	{
		usedInDescription = value;
	}

	/**
	 * @return Whether this symbol can occur in the metadata.
	 */
	public boolean usedInMetadata() 
	{
		return usedInMetadata;
	}

	/**
	 * @param value Whether this symbol can occur in the metadata.
	 */
	public void setUsedInMetadata(final boolean value) 
	{
		usedInMetadata = value;
	}

	/**
	 * @return Whether this symbol has been visited.
	 */
	public boolean visited() 
	{
		return visited;
	}

	/**
	 * @param value
	 */
	public void setVisited(final boolean value) 
	{
		visited = value;
	}

	public int depth()
	{
		return depth;
	}
	
	public void setDepth(final int value)
	{
		depth = value;
	}
	
	/**
	 * @return Rule that this symbol is LHS for (null if terminal).
	 */
	public GrammarRule rule() 
	{
		return rule;
	}

	/**
	 * @param r
	 */
	public void setRule(final GrammarRule r) 
	{
		rule = r;
	}

	/**
	 * @return Package that this symbol belongs to.
	 */
	public PackageInfo pack() 
	{
		return pack;
	}

	/**
	 * @param pi
	 */
	public void setPack(final PackageInfo pi) 
	{
		pack = pi;
	}

	public Class<?> cls()
	{
		return cls;
	}

	public List<Symbol> ancestors()
	{
		return Collections.unmodifiableList(ancestors);
	}

	public Symbol subLudemeOf()
	{
		return subLudemeOf;
	}

	public void setSubLudemeOf(final Symbol symbol)
	{
		subLudemeOf = symbol;
	}

	public Symbol atomicLudeme()
	{
		return atomicLudeme;
	}

	public void setAtomicLudeme(final Symbol symbol)
	{
		atomicLudeme = symbol;
	}
	
	//-------------------------------------------------------------------------

	public void addAncestor(final Symbol ancestor)
	{
		if (!ancestors.contains(ancestor))
			ancestors.add(ancestor);
	}

	public void addAncestorsFrom(final Symbol other)
	{
		for (final Symbol ancestor : other.ancestors)
			addAncestor(ancestor);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Whether this element is a (typically non-terminal) ludeme class.
	 */
	public boolean isClass()
	{
		return 
			ludemeType == LudemeType.Ludeme
			||
			ludemeType == LudemeType.SuperLudeme
			||
			ludemeType == LudemeType.SubLudeme
			||
			ludemeType == LudemeType.Structural;
	}

	/**
	 * @return Whether this element is a terminal symbol.
	 */
	public boolean isTerminal() 
	{
		return !isClass();  //symbolType == SymbolType.Primitive || symbolType == SymbolType.Predefined || symbolType == SymbolType.Constant;
	}

	//-------------------------------------------------------------------------
	
//	/**
//	 * @return What type of ludeme this symbol represents.
//	 */
//	public LudemeType ludemeType()
//	{
//		if (cls.isEnum())
//			return LudemeType.Constant;
//		
//		if 
//		(
//			cls.getSimpleName().equals("String") 
//			|| 
//			cls.getSimpleName().equals("Integer") 
//			|| 
//			cls.getSimpleName().equals("Float") 
//			|| 
//			cls.getSimpleName().equals("Boolean")
//		)
//			return LudemeType.Predefined;
//		
//		if 
//		(
//			cls.getSimpleName().equals("int") 
//			|| 
//			cls.getSimpleName().equals("float") 
//			|| 
//			cls.getSimpleName().equals("boolean") 
//		)
//			return LudemeType.Primitive;
//		
//		if (cls.isInterface())
//		{
//			// Assume is structural rule that links subrules but is never instantiated
//			//System.out.println("** Is an interface: " + this);
//			return LudemeType.Structural;
//		}
//		
//		final Constructor<?>[] constructors = cls.getConstructors();
//		if (constructors.length == 0)
//		{
//			// No constructors: probably a super ludeme
//			//System.out.println("** No constructors found for: " + this);
//			return LudemeType.SuperLudeme;
//		}
//		
////		if (constructors[0].isSynthetic())
////			return LudemeType.SuperLudeme;
//		
//		return LudemeType.Ludeme;
//	}

	//-------------------------------------------------------------------------

	/**
	 * @return Whether this symbol matches the specified one.
	 */
	public boolean matches(final Symbol other)
	{
		return 
			//path.equalsIgnoreCase(other.path())
			path.equals(other.path())
			&&
			nesting == other.nesting;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Whether this symbol matches the specified one.
	 */
	public boolean compatibleWith(final Symbol other)
	{
		if (cls.isAssignableFrom(other.cls()))
			return true;

		if (cls.isAssignableFrom(other.returnType().cls()))
			return true;
		
		if (name.equals("Play"))
		{
			if (other.name().equals("Phase"))
				return true;
		}
		else if (name.equals("Item"))
		{
			if (other.name().equals("Regions"))  //returnType().name().equalsIgnoreCase("Item"))
				return true;
		}
		else if (name.equals("BooleanFunction"))
		{
			if 
			(
				other.returnType().name().equalsIgnoreCase("boolean")
				||
				other.returnType().name().equals("Boolean")
				||
				other.returnType().name().equals("BooleanConstant")
			)
				return true;
		}
		else if (name.equals("IntFunction"))
		{
			if 
			(
				other.returnType().name().equals("int")
				||
				other.returnType().name().equals("Integer")
				||
				other.returnType().name().equals("IntConstant")
			)
				return true;
		}
//		else if (name.equals("DimFunction"))
//		{
//			if 
//			(
//				other.returnType().name().equals("int")
//				||
//				other.returnType().name().equals("Integer")
//			)
//				return true;
//		}
		else if (name.equals("FloatFunction"))
		{
			if 
			(
				other.returnType().name().equals("float")
				||
				other.returnType().name().equals("Float")
				||
				other.returnType().name().equals("FloatConstant")
			)
				return true;
		}
		else if (name.equals("RegionFunction"))
		{
			if 
			(
				other.returnType().name().equals("Region")
				||
				other.returnType().name().equals("Sites")
			)
				return true;
		}
		else if (name.equals("GraphFunction"))
		{
			if 
			(
				other.returnType().name().equals("Graph")
				||
				other.returnType().name().equals("Tiling")
			)
				return true;
		}
		else if (name.equals("RangeFunction"))
		{
			if (other.returnType().name().equals("Range"))
				return true;
		}
//		else if (name.equals("DimFunction"))
//		{
//			if (other.returnType().name().equals("Dim"))
//				return true;
//		}
		else if (name.equals("Directions"))
		{
			if (other.returnType().name().equals("Directions"))
				return true;
		}
		else if (name.equals("IntArrayFunction"))
		{
			if (other.returnType().name().equals("int[]"))
				return true;
		}
//		else if (name.equals("TrackStep"))
//		{
//			if 
//			(
//				other.returnType().name().equals("TrackStep")
//				||
//				other.returnType().name().equals("Dim")
//				||
//				other.returnType().name().equals("TrackStepType")
//				||
//				other.returnType().name().equals("CompassDirection")
//			)
//				return true;
//		}
		
		return false;
	}
		
	//-------------------------------------------------------------------------

	/**
	 * Scopes the keyword with the rightmost package name.
	 * @return Shortest label that disambiguates this symbol name from the other symbol name.
	 */
	public String disambiguation(final Symbol other)
	{
//		System.out.println("path=" + path);
		
		final String label      = isClass() ? token : name;
		final String labelOther = isClass() ? other.token : other.name;
		
		final String[] subs = path.split("\\.");
		final String[] subsOther = other.path.split("\\.");
		
		for (int level = 1; level < subs.length; level++)
		{
			String newLabel = new String(label);  //keyword);
			for (int ll = 1; ll < level; ll++)
				newLabel = subs[subs.length-ll-1] + "." + newLabel;
			
			String newLabelOther = new String(labelOther);  //other.keyword);
			for (int ll = 1; ll < level; ll++)
				newLabelOther = subsOther[subsOther.length-ll-1] + "." + newLabelOther;
		
			if (!newLabel.equals(newLabelOther))
			{
				// Successfully disambiguated
				//grammarLabel = new String(newLabel);
				//other.grammarLabel = new String(newLabelOther);
				return newLabel;
			}
		}
		
		//System.out.println("!! FAILED TO DISAMBIGUATE: " + toString() + " from " + other.toString() + ".");
		return null;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param arg
	 * @return Whether this symbol is a valid return type of the specified arg.
	 */
	public boolean validReturnType(final ClauseArg arg) 
	{
		if 
		(
			path.equals(arg.symbol().path())
			&&
			nesting <= arg.nesting()
		)
			return true;
		
		// Check for function matches
		if 
		(
			arg.symbol().name.contains("Function")
			||
			arg.symbol().name.contains("Constant")
		)
		{
			if (arg.symbol().name.equals("MoveListFunction"))
			{
				if (name.equals("Move"))
					return true;
			}

			if (arg.symbol().name.equals("BitSetFunction"))
			{
				if (name.equals("BitSet"))
					return true;
			}
			
			// TODO: Should RegionFunction also be handled here?
		}
		
		return false;
	}

	/**
	 * @param clause
	 * @return Whether this symbol is a valid return type of the specified clause.
	 */
	public boolean validReturnType(final Clause clause) 
	{
		//System.out.println("validReturnType for: " + clause.symbol().name());
		
		return 
			path.equals(clause.symbol().path())
			&&
			nesting <= clause.symbol().nesting();
	}

	//-------------------------------------------------------------------------

	/**
	 * @param other
	 * @return Whether this symbol is a collection of the specified one.
	 */
	public boolean isCollectionOf(final Symbol other)
	{
		return 
			path.equals(other.path())
			&&
			nesting > other.nesting;
	}

	//-------------------------------------------------------------------------

	/**
	 * Extract name from classPath.
	 */
	void extractName() 
	{
//		System.out.println("Source path: " + path);
//		System.out.println("Source name: " + name);
//		System.out.println("Source className: " + className + "\n");
		
//		if (path.contains("java.util.List<") && path.contains(">"))
//		{
//			// Handle full List description
//			isList = true;
//			final int c = path.indexOf("java.util.List<");
//			path = path.substring(c + 15);
//			path = path.replace(">", "");
//		}
//		else if (path.contains("List<") && path.contains(">"))
//		{
//			// Handle short List description
//			isList = true;
//			final int c = path.indexOf("List<");
//			path = path.substring(c + 5);
//			path = path.replace(">", "");
//		}

		while (true)
		{
			final int c = path.indexOf("[]");
			if (c == -1)
				break;
			nesting++;
			path = path.substring(0, c) + path.substring(c+2);
		}
		
		name = new String(path);
		name = name.replace('/', '.');  // handle absolute paths
		name = name.replace('$', '.');  // handle inner classes

		if (name.contains(".java"))
			name = name.substring(0, name.length() - 5);  // remove class extension

		int c;
		for (c = name.length() - 1; c >= 0; c--)
			if (name.charAt(c) == '.')
				break;

		if (c >= 0) 
			name = name.substring(c);
	
		if (name.length() > 0 && name.charAt(0) == '.')
			name = name.substring(1);  // remove leading dot
		
		if (name.contains(">"))
			name = name.replace('$', '.');  // remnant from list
	}

	//-------------------------------------------------------------------------

	/**
	 * Extract name from classPath.
	 */
	void extractPackagePath() 
	{
		notionalLocation = new String(path);

		notionalLocation = notionalLocation.replace('/', '.');  // handle absolute paths
		// name = name.replace('$', '.'); // Don't include inner classes!

		if (notionalLocation.endsWith(".java"))
			notionalLocation = notionalLocation.substring(0, name.length() - 5);  // remove file extension

		int c;
		for (c = notionalLocation.length() - 1; c >= 0; c--)
			if (notionalLocation.charAt(c) == '.')
				break;

		if (c >= 0) 
			notionalLocation = notionalLocation.substring(0, c);
	}

	//-------------------------------------------------------------------------

	/**
	 * Derive keyword in lowerCamelCase from name.
	 */
	void deriveKeyword(final String alias)
	{
		if (alias != null)
		{
			// Take substring to right of last dot
			int c;
			for (c = alias.length() - 1; c >= 0; c--)
				if (alias.charAt(c) == '.')
					break;
			
			token = (c < 0) ? new String(alias) : alias.substring(c+1);
			
			//System.out.println("deriveKeyword from alias " + alias + ".");
			
			return;
		}
		
		token = new String(name);
		
		if (isClass())
		{			
			// Make lowerCamelCase
			for (int c = 0; c < token.length(); c++)
				if (c == 0 || token.charAt(c - 1) == '.')
					token = 
						token.substring(0, c) 
						+ 
						token.substring(c, c + 1).toLowerCase()
						+ 
						token.substring(c + 1);
		}
		
//		System.out.println("derived keyword: " + keyword + "\n");
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Java description for later instantiation of this symbol.
	 */
	public String javaDescription() 
	{
		String str = name;
					
		for (int n = 0; n < nesting; n++)
			str += "[]";

		return str;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param forceLower Whether to use lowerCamelCase for all types.
	 * @return String description of symbol.. 
	 */
	public String toString(final boolean forceLower) 
	{
		//final String safeKeyword = (grammarLabel == null) ? keyword : grammarLabel;
		//String str = (forceLower || !terminal()) ? safeKeyword : name;
		
		String str = (forceLower || !isTerminal()) ? grammarLabel : name;

		if (ludemeType != LudemeType.Constant)
			str = "<" + str + ">";
		
		for (int n = 0; n < nesting; n++)
			str += "{" + str + "}";

		return str;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString() 
	{
		return toString(false);
	}

	//-------------------------------------------------------------------------

	public String info()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append
		(
			(usedInGrammar() ? "g" : "~") + 
			(usedInDescription() ? "d" : "~") + 
			(usedInMetadata() ? "m" : "~") +
			(isAbstract() ? "*" : "~") + 
			" " + toString() + 
			" name=" + name + 
			" type=" + ludemeType + 
			" (" + path() + ") => " + returnType() +
			//"\n    pack=" + notionalLocation() +
			", pack=" + notionalLocation() +
			", label=" + grammarLabel() +
			", cls=" + (cls() == null ? "null" : cls().getName()) + 
			", keyword=" + token +
			", atomic=" + atomicLudeme.name() + 
			", atomic path=" + atomicLudeme.path()
//			"\n    depth=" + depth() 
		);
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------

}
