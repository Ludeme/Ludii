package main.grammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import main.StringRoutines;

//-----------------------------------------------------------------------------

/**
 * Token from a game description in the Ludii grammar.
 * @author cambolbro
 */
public class Token
{
	public enum TokenType
	{
		Class,
		Array,
		Terminal
	}
	
	/** Name of this token. */
	private String name = null; 
	
	/** Label for this element (if a named parameter). */
	private String parameterLabel = null;
	
	/** Open and closing brackets (if any). */
	private char open  = 0;
	private char close = 0;

	/** Arguments for the token. */
	private final List<Token> arguments = new ArrayList<Token>();

	// Formatting details.
	// 78 chars should keep descriptions within page margin of Language Reference.
	public static final int MAX_CHARS = 78;  //80; 
	//public static final String TAB    = "    ";
	private final int TAB_SIZE = 4;

	//-------------------------------------------------------------------------

	/**
	 * Constructor. 
	 * Decomposes a string into a token tree.
	 */
	public Token(final String str, final Report report)
	{
		decompose(str, report);
	}

	//-------------------------------------------------------------------------

	public String name()
	{
		return name;
	}

	public String parameterLabel()
	{
		return parameterLabel;
	}

	public char open()
	{
		return open;
	}

	public char close()
	{
		return close;
	}

	public List<Token> arguments()
	{
		return Collections.unmodifiableList(arguments);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Type of token.
	 */
	public TokenType type()
	{		
		if (open == '(' && close == ')' && name != null)
			return TokenType.Class;
		
		if (open == '{' && close == '}')  
			return TokenType.Array;
		
		if (open == 0 && close == 0)
		{
			if (name != null)
				return TokenType.Terminal;
		}
		
//		System.out.println("** Token.type(): Unidentified token: " + name);
//		System.out.println("   in: " + toString());
		
		return null;
	}
	
	public boolean isTerminal()
	{
		return open == 0 && close == 0;
	}
	
	public boolean isClass()
	{
		return open == '(' && close == ')' && name != null;
	}
	
	public boolean isArray()
	{
		return open == '{' && close == '}';  // && name == null;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Number of tokens in the tree from this token down.
	 */
	public int count()
	{
		int count = 1;
		for (final Token sub : arguments)
			count += sub.count();
		return count;
	}
	
	/**
	 * @return Number of tokens in the tree from this token down.
	 */
	public int countKeywords()
	{
		int count = type() == TokenType.Array ? 0 : 1;
		for (final Token sub : arguments)
			count += sub.countKeywords();
		return count; 
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Total length of this token and its arguments.
	 */
	int length()
	{
		int length = 0;
		
		if (name != null)
			length += name.length();
		
		if (open != 0 && close != 0)
			length += 2;

		if (parameterLabel != null)
			length += parameterLabel.length() + 1;
			
		if (!arguments.isEmpty())
		{
			for (final Token sub : arguments)
				length += sub.length();
			length += arguments.size() - 1;  // add separators
		}
	
		return length;
	}
	
	//-------------------------------------------------------------------------
	
	public void decompose(final String strIn, final Report report)
	{
		String str = new String(strIn).trim();

		//System.out.println("Decomposing: " + str);
		
		if (str.isEmpty())
		{
			report.addError("Can't decompose token from empty string.");
			return;
		}

		if (StringRoutines.isName(str))
			str = consumeParameterName(str, 0, true);

		String argsString = null;
				
		final char ch = str.charAt(0);
		if (ch == '"')
		{
			//System.out.println("Is string: " + str);

			consumeString(str);
			return;
		}
		else if (ch == '(')
		{
			//System.out.println("Is class: " + str);
			
			open  = '(';	
			final int cb = StringRoutines.matchingBracketAt(str, 0);
			if (cb == -1)
			{
//				System.out.println("** Token.decompose(): No closing bracket ')' for clause:\n" + str);
				
//				int numOpen = 0;
//				int numClosed = 0;
//				for (int n = 0; n < str.length(); n++)
//				{
//					if (str.charAt(n) == '(')
//						numOpen++;
//					else if (str.charAt(n) == ')')
//						numClosed++;
//				}
//				System.out.println("numOpen=" + numOpen + ", numClosed=" + numClosed);
//				System.out.println("strIn:\n" + strIn);
				
				report.addError("No closing bracket ')' for clause '" + Report.clippedString(str, 20) + "'.");
			
				return;
				
				//close = '?';
				//str = str.substring(1);  // trim bracket
			}
			//else
			{
				close = ')';
				str = str.substring(1, cb);  // trim brackets
			}
			argsString = consumeToken(str);
		}
		else if (ch == '{')
		{
			//System.out.println("Is array: " + str);

			open  = '{';
			final int cb = StringRoutines.matchingBracketAt(str, 0);
			if (cb == -1)
			{
//				System.out.println("** Token.decompose(): No closing bracket '}' for clause: " + str);
//				System.out.println("strIn:\n" + strIn);
				
				report.addError("No closing bracket '}' for clause '" + Report.clippedString(str, 20) + "'.");
			
				return;
			}
			close = '}';
			str = str.substring(1, cb);  // trim brackets
			
			argsString = new String(str);
		}
		else if (ch != ' ')
		{
			//System.out.println("Is terminal: " + str);
			consumeToken(str);
			return;
		}
		else
		{
			//System.out.println("Unknown part type: " + str);
		}

//		System.out.println("Splitting args...");
		
		if (argsString != null)
			handleArgs(argsString, report);
	}
	
	//-------------------------------------------------------------------------
	
	void handleArgs(final String strIn, final Report report)
	{
		String str = new String(strIn);
//		String prevStr = strIn;
		
		while (!str.isEmpty())
		{
			str = str.trim();
			if (str.isEmpty())
				break;
			
			int c = 0;
			if (StringRoutines.isName(str))
			{
				// Step past parameter name
				while (c < str.length())
				{
					final char ch = str.charAt(c++);
					if (ch == ':')
						break;
				}
			}

			if (c >= str.length())
			{
				final String msg = "Named arg with no value \"" + str + "\". Null arg to define?";
				report.addWarning(msg);
				//System.out.println(msg);
				break;
			}

			// Don't strip named parameter names out here, they are
			// passed on to subparts
//			if (c > 0)
//			{	
//				// Strip parameter name from front
//				System.out.println("Stripping parameter name: " + str);
//				str = str.substring(c);
//				System.out.println("...new str is \"" + str + "\".");
//				c = 0;
//			}
			
			final char ch = str.charAt(c);
			
//			final int fromC = (c == 0) ? 0 : (c + 1);
			
//			System.out.println("+ str before=\"" + str + "\".");

			if (ch == '"')
			{
				// Arg is a string
				//System.out.println("Handling arg as string: " + str);
				
				int cc = c+1;
				while (cc < str.length())
				{
					if (str.charAt(cc) == '"')
					{
						if (str.charAt(cc-1) != '\\')  // step past embedded quotation mark "\""
							break;
					}
					cc++;
				}
				//cc++;
				
				if (cc >= str.length())
				{
//					System.out.println("** Token.handleArgs(): No closing quote '\"' for: " + str.substring(c));
//					System.out.println("strIn:\n" + strIn);
					
					report.addError("No closing quote '\"' for token arg '" + 
									Report.clippedString(str.substring(c), 20) + "'.");
					return;
				}
				
				if (str.substring(0, cc+1).trim().isEmpty())
					System.out.println("A - Empty substring.");
				
				final Token sub = new Token(str.substring(0, cc+1), report);  //, this);
				arguments.add(sub);
//				System.out.println("+ Created sub: " + sub.token);				
//				c = cc+1;
				str = str.substring(cc+1);
			}
			else if (ch == '{')
			{
				// Arg is an array
				//System.out.println("Handling arg as array: " + str);
				
				final int cb = StringRoutines.matchingBracketAt(str, c);
				if (cb == -1)
				{
//					System.out.println("** Token.handleArgs(): No closing bracket '}' for: " + str.substring(c));
//					System.out.println("strIn:\n" + strIn);
					
					report.addError("No closing bracket '}' for token arg '" + Report.clippedString(str.substring(c), 20) + "'.");
					return;
				}
				
				if (str.substring(0, cb+1).trim().isEmpty())
					System.out.println("B - Empty substring.");

				final Token sub = new Token(str.substring(0, cb+1), report);
				arguments.add(sub);
//				System.out.println("+ Created sub: " + sub.token);
				//c = cb + 1;
				
				str = str.substring(cb+1);
			}
			else if (ch == '(')
			{
				// Arg is a class
				//System.out.println("Handling arg as class: " + str);
				
				final int cb = StringRoutines.matchingBracketAt(str, c);
				if (cb == -1)
				{
//					System.out.println("** Token.handleArgs(): No closing bracket ')' for: " + str.substring(c));
					
					report.addError("No closing bracket ')' for token arg '" + 
									Report.clippedString(str.substring(c), 20) + "'.");
					return;
					
					//str = str.substring(1);
				}
				//else
				{
					//System.out.println("+ Storing class subpart: " + str.substring(0, cb+1));
				
					if (str.substring(0, cb+1).trim().isEmpty())
						System.out.println("C - Empty substring.");

					final Token sub = new Token(str.substring(0, cb+1), report);
					arguments.add(sub);
//					System.out.println("+ Created sub: " + sub.token);
//					c = cb + 1;
					str = str.substring(cb+1);
				}
			}
			else if (ch != ' ')
			{
				// Arg is unknown
				//System.out.println("** Handling arg as something unknown:\n" + str);
				
				int cc = c;
				while (cc < str.length() && StringRoutines.isTokenChar(str.charAt(cc)))
					cc++;
				
				if (cc == 0)
				{
//					System.out.println("** Can't handle empty arg substring (prevStr=\"" + prevStr + "\").");
//					System.out.println("** strIn=" + strIn);
//					System.out.println("** Maybe a wrong bracket type, or incorrect use of commas as list separators?");
					str = str.substring(1);
					
					report.addError("Empty substring from '" + Report.clippedString(strIn, 20) + 
									"'. Maybe a wrong bracket type '}'?");
					return;
				}
				
//				if (str.substring(0, cc).trim().isEmpty())
//					System.out.println("D - Empty substring.");

				final Token sub = new Token(str.substring(0, cc), report);
				arguments.add(sub);
	//			System.out.println("+ Created sub: " + sub.token);				
	//			c = cc+1;
				str = str.substring(cc);
			}
			else
			{
				// Not handling arg
				System.out.println("** Token.handleArgs(): Not handling arg: " + str);
				
				str = str.substring(1);
			}
			
//			System.out.println("+ str after =\"" + str + "\".\n");
			
//			prevStr = str;
		}
		
		// Remove any null arguments, which can occur if the last line is a closing bracket.
		// e.g.  (ai
		//           "Go_ai"
		//       )
		for (int a = arguments.size() - 1; a >= 0; a--)
			if (arguments.get(a) == null || arguments.get(a).type() == null)
			{
//				System.out.println("Removing null arg " + a + "...");
				arguments.remove(a);
			}
	}

	//-------------------------------------------------------------------------

	void consumeString(final String strIn)
	{
		final String str = new String(strIn);
			
//		System.out.print("Consuming string: " + str);

		if (str.isEmpty() || str.charAt(0) != '"')
		{
			System.out.println("Not a string: " + str);
			return;
		}
		
		name = "\"";
		int c = 1;
		while (c < str.length())
		{
			final char ch = str.charAt(c);
			final boolean isQuote    = (ch == '"');
			final boolean isEmbedded = (ch == '"' && str.charAt(c-1) == '\\');
			if (isQuote && !isEmbedded)
				break;
			c++;
			//if (ch != '\\')
			
			if (isEmbedded)
				name = name.substring(0, name.length()-1) + "'";  //"@#@";  //"\"";  //'\\' + '"';
			else
				name += ch;
		}
		name += "\"";
			
//		// Restore embedded substrings
//		if (name != null)
//			name = name.replaceAll("@#@", "'"); //"!");

		//name = "\"" + str.substring(1, c-1) + "\"";
		
//		System.out.println(" => String token name is: " + name);
	}

	String consumeToken(final String strIn)
	{
		final String str = new String(strIn);
			
//		System.out.print("Consuming terminal: " + str);

		if (str.isEmpty())
		{
			System.out.println("Not a token: \"" + str + "\"");
			System.out.println("Check for empty clause \"()\".");
			return null;
		}
		
		name = "";
		int c = 0;
		while (c < str.length())
		{
			final char ch = str.charAt(c++);
			if (!StringRoutines.isTokenChar(ch))
				break;
			name += ch;
		}		
//		System.out.println(" => token is: " + tokenName);
		
//		System.out.println("consumeToken(): name=" + name + " => " + str.substring(c).trim());
		
		return str.substring(c).trim();
	}

	String consumeParameterName(final String strIn, final int cIn, final boolean store)
	{
		final String str = new String(strIn);
			
//		System.out.println("Consuming label: " + str);

		if (str.isEmpty())
		{
			System.out.println("Not a parameter name: " + str);
			return null;
		}
		
		if (store)
			parameterLabel = "";
		
		int c = cIn;
		while (c < str.length())
		{
			final char ch = str.charAt(c++);
			if (ch == ':')
				break;
			
			if (store)
				parameterLabel += ch;
		}		
//		System.out.println(" => parameter tokenName is: " + tokenName);
		
		final String str2 = str.substring(0, cIn) + str.substring(c);
		return str2.trim();
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return format();
	}
	
	//-------------------------------------------------------------------------	
	
	public String format()
	{
		final List<String> lines = new ArrayList<String>();
		format(lines, 0, false);
		
		// Combine line with "game" ludeme and name
		for (int n = 0; n < lines.size()-1; n++)
			if 
			(
				lines.get(n).contains("(game") 
				|| 
				lines.get(n).contains("(match") 
				|| 
				lines.get(n).contains("(piece")
			)
				mergeNameLinesAt(lines, n);
		
		compressNumberPairArrayElements(lines);

		// Merge simple array elements into more compact format 
		mergeArrayLines(lines);
				
		final StringBuilder sb = new StringBuilder();
		for (final String line : lines)
			sb.append(line + "\n");  //"  [" + line.length() + ", " + line.trim().length() + "]\n");
		return sb.toString();
	}
	
	public void format(final List<String> lines, final int depth, final boolean doSplit)
	{
		String line = StringRoutines.indent(TAB_SIZE, depth);

		// Check if fits on one line
		final String tokenLine = formatSingleLine();
		
		// ...but always split (equipment ...) and (rules ...) ludemes
		final boolean isEquipmentToken = tokenLine.indexOf("(equipment") == 0;
		final boolean isRulesToken     = tokenLine.indexOf("(rules")     == 0;
//		System.out.println("tokenLine is \"" + tokenLine.trim() + ", isEquipmentToken is " + isEquipmentToken + ".");

		if (line.length() + tokenLine.length() <= MAX_CHARS && !doSplit && !isRulesToken && !isEquipmentToken)
		{
			// Print this token as a single line
			lines.add(line + tokenLine);
			return;
		}
				
		if (parameterLabel != null)
			line += parameterLabel + ":";  // named parameter

		if (isTerminal())
		{
			// Simple terminal token
			lines.add(line + name);
			return;
		}
		
		// Must split this token's arguments over several lines 
		// This token is a class constructor or array
		line += open;
		
		if (name != null)
			line += name;  // must be a class constructor
			
		lines.add(line);			
			
		for (final Token arg : arguments)
		{
			final String argStr = arg.formatSingleLine();
			
			final boolean isEquipmentArg = argStr.indexOf("(equipment") == 0;
			final boolean isRulesArg     = argStr.indexOf("(rules")     == 0;
//			System.out.println("argStr is \"" + argStr.trim() + ", isEquipmentArg is " + isEquipmentArg + ".");

			if 
			(
				StringRoutines.indent(TAB_SIZE, depth+1).length() + argStr.length() > MAX_CHARS 
				||
				isEquipmentToken || isEquipmentArg
				|| 
				isRulesToken || isRulesArg
			)
			{
				// Split arg over several lines
				final List<String> subLines = new ArrayList<String>();
				arg.format(subLines, depth+1, isEquipmentToken);  // remember for next level of nesting
				lines.addAll(subLines);
			}
			else
			{
				// Fit arg on one line
				lines.add(StringRoutines.indent(TAB_SIZE, depth+1) + argStr);
			}
		}
		
		lines.add(StringRoutines.indent(TAB_SIZE, depth) + close);
	}

	//-------------------------------------------------------------------------	
	
	static void mergeNameLinesAt(final List<String> lines, final int n)
	{
		if (n >= lines.size())
			return;  // no next line
		
		final boolean isName = lines.get(n+1).trim().charAt(0) == '"';
		if (isName)
			mergeLinesAt(lines, n);
	}

	static void mergeLinesAt(final List<String> lines, final int n)
	{
		final String line = lines.get(n) + " " + lines.get(n+1).trim();
		lines.remove(n);
		lines.remove(n);
		lines.add(n, line);
	}

	//-------------------------------------------------------------------------	

	static void mergeArrayLines(final List<String> lines)
	{
		int n = 0;
		while (n < lines.size())
		{
			//final String line = lines.get(n);
			if (isArrayOpen(lines.get(n)))
			{
				boolean containsClass = false;
				int nn;
				for (nn = n+1; nn < lines.size(); nn++)
				{
					if (isClass(lines.get(nn)))
						containsClass = true;
					if (isArrayClose(lines.get(nn)))
						break;
				}	
				
				final boolean isEquipment = n > 0 && lines.get(n-1).contains("(equipment");
				
//				System.out.println("line is \"" + lines.get(n).trim() + ", prev is \"" + lines.get(n-1).trim() + ", isEquipment is " + isEquipment + ".");
				
				if (nn < lines.size() && !containsClass && !isEquipment)
				{
					// Merge the lines of this array
					n++;  // move to next line
					
					while (n < lines.size() - 1)
					{
						final String nextLine = lines.get(n + 1);
						if (isArrayClose(nextLine))
							break;
						
						if (lines.get(n).length() + nextLine.trim().length() < MAX_CHARS)
							mergeLinesAt(lines, n);
						else
							n++;
					}
				}
			}
			n++;
		}
	}

	static boolean isArrayOpen(final String line)
	{
		final int numOpen  = StringRoutines.numOpenBrackets(line);
		final int numClose = StringRoutines.numCloseBrackets(line);
		return line.contains("{") && numOpen == 1 && numClose == 0;		
	}

	static boolean isArrayClose(final String line)
	{
		final int numOpen  = StringRoutines.numOpenBrackets(line);
		final int numClose = StringRoutines.numCloseBrackets(line);
		return line.contains("}") && numOpen == 0 && numClose == 1;
	}
	
	static boolean isClass(final String line)
	{
		return line.contains("(");
	}

	//-------------------------------------------------------------------------	

	static void compressNumberPairArrayElements(final List<String> lines)
	{
		for (int n = 0; n < lines.size(); n++)
		{
			String  line = lines.get(n);
			
			if (!line.contains("{ ") || !line.contains(" }"))
				continue;
							
			int c = line.indexOf("{ ");
			if (c >= 0)
			{
				final char ch = line.charAt(c + 2);
				if (ch == '"' || StringRoutines.isNumeric(ch))
					line = line.substring(0, c+1) + line.substring(c+2);
			}
			
			c = line.indexOf(" }");
			if (c >= 0)
			{
				final char ch = line.charAt(c - 1);
				if (ch == '"' || StringRoutines.isNumeric(ch))
					line = line.substring(0, c) + line.substring(c+1);
			}
		
			lines.remove(n);
			lines.add(n, line);
		}
	}

	//-------------------------------------------------------------------------	

	public String formatSingleLine()
	{
		final StringBuilder sb = new StringBuilder();

		if (parameterLabel != null)
			sb.append(parameterLabel + ":");  // named parameter

		if (isTerminal())
		{
			// Simple terminal token
			sb.append(name);
			return sb.toString();
		}
		
		sb.append(open);
			
		if (isClass())
			sb.append(name);

		for (final Token sub : arguments)
			sb.append(" " + sub.formatSingleLine());
			
		if (isArray())
			sb.append(" ");  // pad past last array entry
		
		sb.append(close);

		return sb.toString();	
	}
	
	//-------------------------------------------------------------------------

	public String formatZhangShasha
	(
		final String indent,  final int depth, 
		final boolean inline, final boolean zhangShasha
	)
	{
		//System.out.println("Formatting:" + token + "\n-------------------------");
		
		String str = "";
			
		if (open == 0)
		{
			// Just print token (prepended with label, if given)
			if (zhangShasha && StringRoutines.isInteger(name))
			{
				// Convert number to string
				if (parameterLabel != null)
					str += indent + "\"" + parameterLabel + ":" + name + "\"";
				else
					str += "\"" + name + "\"";
			}
			else
			{
				if (parameterLabel != null)
					str += indent + parameterLabel + ":";
				str += name;
			}
			return str;
		}
		
		// Is a named parameter
		if (parameterLabel != null)
			str += indent + parameterLabel + ":";
		
		final String tab = StringRoutines.indent(TAB_SIZE, 1);
		
		if (name != null)
		{
			// Token is a constructor
			final int len = length();
			if (name.equals("game"))
			{
				// Print name and model on one line
				if (zhangShasha)
					str += name + open;
				else
					str += open + name;

				// Put name on same line
				str += " " + arguments.get(0).formatZhangShasha("", depth+1, true, zhangShasha);
				
				// Look for class and
				for (final Token arg : arguments)
				{
					if (arg != null && arg.type() == TokenType.Class)
						str += indent + tab + arg.formatZhangShasha(tab, depth+1, false, zhangShasha) + "\n";
				}
				
				str += close;  // + "\n";
			}
			else if (len < MAX_CHARS && (depth > 1 || inline))
			{
				// Print on one line
				//System.out.println("name=" + name + ", is number=" + StringRoutines.isInteger(name));
				
				if (zhangShasha)
					str += name + open;
				else
					str += open + name;			
								
				for (final Token sub : arguments)
					str += " " + sub.formatZhangShasha("", depth+1, true, zhangShasha);
				
				str += close;  // + "\n";
			}
			else
			{
				// Print over multiple lines
				if (zhangShasha)
					str += name + open;
				else
					str += open + name;
				str += "\n";
				
				for (final Token sub : arguments)
					str += indent + tab + sub.formatZhangShasha(indent+tab, depth+1, false, zhangShasha) + "\n";
				
				str += indent + close;  // + "\n";
			}
		}
		else
		{
			// Token is an array
			final int len = length();
			if (len < MAX_CHARS || shortArguments())  // && arguments.size() < 15)
			{
				// Print on one line
				if (zhangShasha)
					str += "array(";
				else
					str += open;
				
				if (name != null)
					str += name;
				
				for (final Token sub : arguments)
					str += " " + sub.formatZhangShasha("", depth+1, true, zhangShasha);
				
				if (zhangShasha)
					str += " )";
				else
					str += " " + close;  // + "\n";
			}
			else
			{
				// Print over multiple lines
				if (zhangShasha)
					str += "array(";
				else
					str += open;
				
				if (name != null)
					str += name + "\n";
				
				for (final Token sub : arguments)
					str += indent + tab + sub.formatZhangShasha(indent+tab, depth+1, false, zhangShasha) + "\n";
				
				if (zhangShasha)
					str += indent + ")";
				else
					str += indent + close;  // + "\n";
			}
		}

		return str;	
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Whether arguments are relatively small, typically a list of cell coordinates.
	 */
	public boolean shortArguments()
	{
		int maxLen = 0;
		for (final Token sub : arguments)
		{
			final int len = sub.length();
			if (len > maxLen)
				maxLen = len;
		}
		return maxLen < 6;
	}
	
	//-------------------------------------------------------------------------	
	
	public String dump(final String indent)
	{
		final String label = "" + type().name().charAt(0) + type().name().charAt(1) + ": "; 
		final StringBuilder sb = new StringBuilder();
		
		sb.append(label + indent);

		if (parameterLabel != null)
			sb.append(parameterLabel + ":");

		if (open != 0)
			sb.append(open);
		
		if (name != null)
			sb.append(name);
		
//		if (parameterLabel != null)
//			sb.append(" (" + parameterLabel + ":)");
		
		final String tab = StringRoutines.indent(TAB_SIZE, 1);
		
		if (arguments.size() > 0)
		{
			sb.append("\n");
			
			for (final Token arg : arguments)
				sb.append(arg.dump(indent + tab));
			
			if (close != 0)
				sb.append(label + indent + close);
		}
		else
		{
			if (close != 0)
				sb.append(close);
		}	
		sb.append("\n");
		
		return sb.toString();
	}

	//-------------------------------------------------------------------------	
	
	/**
	 * @return All tokens that are included in this token's tree.
	 */
	public Set<Token> getAllTokensInTree()
	{
		final Set<Token> allTokens = new HashSet<>();
		allTokens.add(this);
		
		for (final Token arg : arguments)
			allTokens.addAll(arg.getAllTokensInTree());
		
		return allTokens;
	}
	
	//-------------------------------------------------------------------------	

}
