
package main.grammar;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import annotations.Alias;
import main.StringRoutines;

//-----------------------------------------------------------------------------

/**
 * Instance of an item actually compiled.
 * @author cambolbro and matthew.stephenson
 */
public class Call
{
	public enum CallType
	{
		Null,
		Class,
		Array,
		Terminal
	}
	
	private CallType type = null; 
	
	//---------------------------------------------------------

	/** 
	 * Record of instance that created this call.
	 * Only keep necessary info from Instance to minimise memory usage.
	 */
	//private final Instance instance;
	private final Symbol symbol;
	private final Object object;
	private final String constant;
	
	/** Expected type for this call. */
	private final Class<?> expected;
	
	/** Arguments for the call (if class). */
	private final List<Call> args = new ArrayList<>();

	/** Indent level for formatting. */
	private final int TAB_SIZE = 4;
	
	/** Named parameter in grammar for this argument, if any. */
	private String label = null;

	//-------------------------------------------------------------------------

	/**
	 * Default constructor for Array call. 
	 */
	public Call(final CallType type)
	{
		this.type     = type;
		symbol   = null;
		object   = null;
		constant = null;
		expected = null;
	}

	/**
	 * Constructor for Terminal call. 
	 */
	public Call(final CallType type, final Instance instance, final Class<?> expected)
	{
		this.type     = type;
		symbol   = instance.symbol();
		object   = instance.object();
		constant = instance.constant();
		this.expected = expected;
	}

	//-------------------------------------------------------------------------

	public CallType type()
	{
		return type;
	}
	
	public Symbol symbol()
	{
		return symbol;
	}
	
	public Class<?> cls()
	{
		return symbol == null ? null : symbol.cls();
	}
	
	public Object object()
	{
		return object;
	}

	public String constant()
	{
		return constant;
	}

	public List<Call> args()
	{
		return Collections.unmodifiableList(args);
	}

	public Class<?> expected()
	{
		return expected;
	}

	public String label()
	{
		return label;
	}
	
	public void setLabel(final String str)
	{
		label = new String(str);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Add argument to the list.
	 */
	public void addArg(final Call arg)
	{
		args.add(arg);
	}
	
//	/**
//	 * Remove the last argument (if any).
//	 */
//	public void removeLastArg()
//	{
//		if (args.size() == 0)
//			System.out.println("** Call.removeLastArg(): No args to remove!");
//		
//		args.remove(args.size() - 1);
//	}

	//-------------------------------------------------------------------------

	/**
	 * @return Number of tokens in the tree from this token down.
	 */
	public int count()
	{
		int count = 1;
		for (final Call sub : args)
			count += sub.count();
		return count;
	}
	
	/**
	 * @return Number of tokens in the tree from this token down.
	 */
	public int countClasses()
	{
		int count = type() == CallType.Class ? 1 : 0;
		for (final Call sub : args)
			count += sub.countClasses();
		return count; 
	}
	
	/**
	 * @return Number of tokens in the tree from this token down.
	 */
	public int countTerminals()
	{
		int count = type() == CallType.Terminal ? 1 : 0;
		for (final Call sub : args)
			count += sub.countTerminals();
		return count; 
	}
	
	/**
	 * @return Number of tokens in the tree from this token down.
	 */
	public int countClassesAndTerminals()
	{
		int count = type() != CallType.Array ? 1 : 0;
		for (final Call sub : args)
			count += sub.countClassesAndTerminals();
		return count; 
	}

	//-------------------------------------------------------------------------

	/**
	 * Export this call node (and its args) to file.
	 * @param fileName
	 */
	public void export(final String fileName)
	{
        try (final FileWriter writer = new FileWriter(fileName))
        {
        	final String str = toString();
        	writer.write(str);
        }
        catch (final IOException e) 
        { 
        	e.printStackTrace(); 
        }
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return format(0, true);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public boolean equals(final Object o)
	{
		return toString().equals(((Call) o).toString());
	}
	
	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return String representation of callTree for display purposes.
	 */
	String format(final int depth, final boolean includeLabels)
	{
		final StringBuilder sb = new StringBuilder();
		
		final String indent = StringRoutines.indent(TAB_SIZE, depth);
		
		switch (type())
		{
		case Null:
			sb.append(indent + "-\n");
			break;
		case Array:
			sb.append(indent + "{\n");
			for (final Call arg : args)
				sb.append(arg.format(depth, includeLabels));

			if (includeLabels && label != null)
				sb.append(" \"" + label + ":\"");

			break;
		case Class:
			sb.append(indent + cls().getName());
			if (!cls().getName().equals(expected.getName()))
				sb.append(" (" + expected.getName() + ")");
			
			if (includeLabels && label != null)
				sb.append(" \"" + label + ":\"");
			
			sb.append("\n");
				
			for (final Call arg : args)
				sb.append(arg.format(depth + 1, includeLabels));
			break;
		case Terminal:
			if (object().getClass().getSimpleName().equals("String"))
				sb.append(indent + "\"" + object() + "\" (" + expected.getName() + ")");
			else
				sb.append(indent + object() + " (" + expected.getName() + ")");
			
			if (includeLabels && label != null)
				sb.append(" \"" + label + ":\"");

			if (constant != null)
				sb.append(" Constant=" + constant);

			sb.append("\n");
			break;
		default:
			System.out.println("** Call.format() should never hit default.");
			break;
		}
			
		if (type() == CallType.Array)
			sb.append(indent + "}\n");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
//	/**
//	 * @return LudemeInfo list representation of callTree for ludemplex analysis purposes.
//	 */
//	public List<LudemeInfo> analysisFormatList(final int depth, final List<LudemeInfo> ludemes)
//	{
//		final List<LudemeInfo> ludemesFound = new ArrayList<>();
//		
//		switch (type)
//		{
//		case Null:
//			break;
//		case Array:
//			for (final Call arg : args)
//				ludemesFound.addAll(arg.analysisFormatList(depth, ludemes));
//			break;
//		case Class:
//			final LudemeInfo ludemeInfo = LudemeInfo.findLudemeInfo(this, ludemes);
//			if (ludemeInfo != null)
//			{
//				ludemesFound.add(ludemeInfo);
//				if (args.size() > 0)
//					for (final Call arg : args)
//						ludemesFound.addAll(arg.analysisFormatList(depth + 1, ludemes));
//			}
//			break;
//		case Terminal:
//			final LudemeInfo ludemeInfo2 = LudemeInfo.findLudemeInfo(this, ludemes);
//			if (ludemeInfo2 != null)
//				ludemesFound.add(ludemeInfo2);
//			break;
//		default:
//			System.out.println("** Call.format() should never hit default.");
//			break;
//		}
//		
//		return new ArrayList<>(new HashSet<>(ludemesFound));
//	}
	
	/**
	 * @return LudemeInfo dictionary representation of callTree for ludemplex analysis purposes.
	 */
	public Map<LudemeInfo, Integer> analysisFormat(final int depth, final List<LudemeInfo> ludemes)
	{
		final Map<LudemeInfo, Integer> ludemesFound = new HashMap<>();
		for (final LudemeInfo ludemeInfo : ludemes)
			ludemesFound.put(ludemeInfo, 0);
		
		switch (type)
		{
		case Null:
			break;
		case Array:
			for (final Call arg : args)
			{
				final Map<LudemeInfo, Integer> ludemesFound2 = arg.analysisFormat(depth, ludemes);
				for (final LudemeInfo ludemeInfo : ludemesFound2.keySet())
					ludemesFound.put(ludemeInfo, ludemesFound.get(ludemeInfo) + ludemesFound2.get(ludemeInfo));
			}
			break;
		case Class:
			final LudemeInfo ludemeInfo = LudemeInfo.findLudemeInfo(this, ludemes);
			if (ludemeInfo != null)
			{
				ludemesFound.put(ludemeInfo, ludemesFound.get(ludemeInfo) + 1);
				for (final Call arg : args)
				{
					final Map<LudemeInfo, Integer> ludemesFound2 = arg.analysisFormat(depth + 1, ludemes);
					for (final LudemeInfo ludemeInfoChildren : ludemesFound2.keySet())
						ludemesFound.put(ludemeInfoChildren, ludemesFound.get(ludemeInfoChildren) + ludemesFound2.get(ludemeInfoChildren));
				}
			}
			break;
		case Terminal:
			final LudemeInfo ludemeInfo2 = LudemeInfo.findLudemeInfo(this, ludemes);
			if (ludemeInfo2 != null)
				ludemesFound.put(ludemeInfo2, ludemesFound.get(ludemeInfo2) + 1);
			break;
		default:
			System.out.println("** Call.format() should never hit default.");
			break;
		}
		
		// remove ludemes with a count of zero.
		final Map<LudemeInfo, Integer> ludemesFoundGreaterZero = new HashMap<>();
		for (final LudemeInfo ludemeInfo : ludemesFound.keySet())
			if (ludemesFound.get(ludemeInfo) > 0)
				ludemesFoundGreaterZero.put(ludemeInfo, ludemesFound.get(ludemeInfo));
		
		return ludemesFoundGreaterZero;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return String representation of call tree in preorder notation, e.g. f(a b(c)).
	 */
	public String preorderFormat(final int depth, final List<LudemeInfo> ludemes)
	{
		String ludemesFound = "";
		
		switch (type)
		{
		case Null:
			break;
		case Array:
			String newString = "(";
			for (final Call arg : args)
				newString += arg.preorderFormat(depth, ludemes) + " ";
			newString += ")";
			if (newString.replaceAll("\\s+","").length() > 2)
				ludemesFound += "Array" + newString;
			break;
		case Class:
			final LudemeInfo ludemeInfo = LudemeInfo.findLudemeInfo(this, ludemes);
			if (ludemeInfo != null)
			{
				String newString2 = "(";
				if (args.size() > 0)
					for (final Call arg : args)
						newString2 += arg.preorderFormat(depth + 1, ludemes) + " ";
				newString2 += ")";
				if (newString2.replaceAll("\\s+","").length() > 2)
					ludemesFound += ludemeInfo.symbol().name() + newString2;
				else
					ludemesFound += ludemeInfo.symbol().name();
			}
			break;
		case Terminal:
			final LudemeInfo ludemeInfo2 = LudemeInfo.findLudemeInfo(this, ludemes);
			if (ludemeInfo2 != null)
				ludemesFound += ludemeInfo2.symbol().name() + " ";
			break;
		default:
			System.out.println("** Call.format() should never hit default.");
			break;
		}
		
		return ludemesFound;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return String representation of callTree for database storing purposes (mimics game description style).
	 */
	public List<String> ludemeFormat(final int depth)
	{
		List<String> stringList = new ArrayList<String>();
		
		switch (type)
		{
		case Null:
			break;
		case Array:
			if (label != null && depth > 0)
				stringList.add(label + ":");
			
			stringList.add("{");
			
			for (final Call arg : args)
			{
				stringList.addAll(arg.ludemeFormat(depth));
				stringList.add(" ");
			}
			break;
		case Class:
			final Annotation[] annotations = cls().getAnnotations();
			String name = cls().getName().split("\\.")[cls().getName().split("\\.").length-1];
			for (final Annotation annotation : annotations)
				if (annotation instanceof Alias)
					name = ((Alias)annotation).alias();
			name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
			
			if (label != null && depth > 0)
				stringList.add(label + ":");
			
			stringList.add("(");
			stringList.add(name);

			if (args.size() > 0)
			{
				stringList.add(" ");
				
				for (final Call arg : args)
					stringList.addAll(arg.ludemeFormat(depth + 1));

				stringList = removeCharsFromStringList(stringList, 1);
			}
			
			stringList.add(") ");
			break;
		case Terminal:
			if (label != null)
				stringList.add(label + ":");
			
			if (constant != null)
				stringList.add(constant + " ");
			else if (object().getClass().getSimpleName().equals("String"))
				stringList.add("\"" + object() + "\" ");
			else
				stringList.add(object() + " ");

			break;
		default:
			System.out.println("** Call.format() should never hit default.");
			break;
		}
			
		if (type == CallType.Array)
		{
			if (!stringList.get(stringList.size()-1).equals("{"))
					stringList = removeCharsFromStringList(stringList, 2);
			
			stringList.add("} ");
		}
		
		return stringList;
	}

	//-------------------------------------------------------------------------	

	/**
	 * Removes a specified number of chars from a list of Strings, starting at the end and working backwards.
	 */
	private static List<String> removeCharsFromStringList(final List<String> originalStringList, final int numChars) 
	{
		final List<String> stringList = originalStringList;
		for (int i = 0; i < numChars; i++)
		{
			final String oldString = stringList.get(stringList.size()-1);
			final String newString = oldString.substring(0, oldString.length()-1);
			stringList.remove(stringList.size()-1);
			if (newString.length() > 0)
				stringList.add(newString);
		}
		return stringList;
	}

	//-------------------------------------------------------------------------	

}
