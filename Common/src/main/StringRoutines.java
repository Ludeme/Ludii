package main;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Miscellaneous string manipulation routines.
 * 
 * @author cambolbro and Eric.Piette
 */
public final class StringRoutines
{

	//-------------------------------------------------------------------------
	
	public final static char[][] brackets = 
	{
		{ '(', ')' },	
		{ '{', '}' },	
		{ '[', ']' },	
		{ '<', '>' },	
	};

	public static final int Opening = 0;
	public static final int Closing = 1;

	//-------------------------------------------------------------------------

	public static boolean isOpenBracket(final char ch)
	{
		for (int n = 0; n < brackets.length; n++)
			if (ch == brackets[n][0])
				return true;
		return false;
	}

	public static boolean isCloseBracket(final char ch)
	{
		for (int n = 0; n < brackets.length; n++)
			if (ch == brackets[n][1])
				return true;
		return false;
	}

	public static boolean isBracket(final char ch)
	{
		return isOpenBracket(ch) || isCloseBracket(ch);
	}

	public static int numOpenBrackets(final String str)
	{
		int num = 0;
		for (int c = 0; c < str.length(); c++)
			if (isOpenBracket(str.charAt(c)))
				num++;
		return num;
	}

	public static int numCloseBrackets(final String str)
	{
		int num = 0;
		for (int c = 0; c < str.length(); c++)
			if (isCloseBracket(str.charAt(c)))
				num++;
		return num;
	}

	public static boolean balancedBrackets(final String str)
	{
		return numOpenBrackets(str) == numCloseBrackets(str);
	}
	
	public static int numChar(final String str, final char ch)
	{
		int num = 0;
		for (int c = 0; c < str.length(); c++)
			if (str.charAt(c) == ch)
				num++;
		return num;
	}
	
	public static int bracketIndex(final char ch, final int openOrClosed)
	{
		for (int n = 0; n < brackets.length; n++)
			if (brackets[n][openOrClosed] == ch)
				return n;
		return -1;
	}

	/**
	 * @param str
	 * @param from
	 * @return Location of matching closing bracket (including nesting), else -1 if none.
	 */
	public static int matchingBracketAt(final String str, final int from)
	{
		return matchingBracketAt(str, from, true);
	}

	/**
	 * @param str
	 * @param from
	 * @param doNesting
	 * @return Location of matching closing bracket, else -1 if none.
	 */
	public static int matchingBracketAt(final String str, final int from, final boolean doNesting)
	{
		// Check is actually opening bracket
		int c = from;
		final char ch = str.charAt(c);
		
		final int bid = bracketIndex(ch, Opening);
		if (bid == -1)
		{
			System.out.println("** Specified char '" + ch + "' is not an open bracket.");
			return -1;
		}
		
		// Check for matching closing bracket
		int bracketDepth = 0;
		boolean inString = false;
		while (c < str.length())
		{
			final char chB = str.charAt(c);
			
			if (chB == '"')
				inString = !inString;
			
			if (!inString)
			{
				final char chA = (c == 0) ? '?' : str.charAt(c - 1);
				if (chB == brackets[bid][Opening])
				{
					if (chA != '(' || chB != '<')  // check is not a (< ...) or (<= ...) ludeme
						bracketDepth++;
				}
				else if (chB == brackets[bid][Closing])
				{
					if (chA != '(' || chB != '>')  // check is not a (> ...) or (>= ...) ludeme
					{
						if (!doNesting)
							break;  // stop on first matching closing bracket, e.g. option "<<>"
						bracketDepth--;
					}
				}
			}
			
			if (bracketDepth == 0)
				break;  // found bracket that closes opening bracket	
			c++;
		}
			
		if (c >= str.length())
			return -1;  // no matching closing bracket found
			//throw new IllegalArgumentException("No matching closing bracket " + brackets[bid][Closing] + " found.");
			
		return c;
	}

	public static int matchingQuoteAt(final String str, final int from)
	{
		int c = from;
		final char ch = str.charAt(c);
		
		if (ch != '"')
			throw new IllegalArgumentException("String expected but no opening \" found.");
		
//		System.out.println("\nString is:");
		
		c++;		
		while (c < str.length())
		{
			final char chC = str.charAt(c);
			
//			System.out.println("chC=" + chC);
			
			switch (chC)
			{
			case '\\':
				//System.out.println("Slash!");
				if (c < str.length() - 1 && str.charAt(c+1) == '"')
					c++;  // skip embedded "\""
				break;
			//case '\':
			//	c++;
			//	break;
			case '"':
				return c;
			default:
				// Do nothing
			}
			c++;
		}
			
		//throw new IllegalArgumentException("No closing \" found when parsing String.");
		
		// Closing quote not found, but this may be valid, 
		// e.g. parsing comments before rulesets expanded.
		return -1;
	}
		
	//-------------------------------------------------------------------------

	public static String toDromedaryCase(final String className)
	{
		return className.substring(0, 1).toLowerCase() + className.substring(1);
	}

	public static String highlightText(final String fullText, final String highlight, final String tag, final String colour)
	{
		final String replacement = "<"+tag+" color="+colour+">"+highlight+"</"+tag+">";
		
		System.out.println(highlight + " --> " + replacement);
		return fullText.replace (highlight, replacement);
	}

	public static String escapeText(final String text)
	{
		return text
				.replace("&", "&amp;")
				.replace("'", "&apos;")
				.replace("\"", "&quot;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;")
				.replace(" ", "&nbsp;")
				.replace("\n", "<br/>");
	}
	
	/**
	 * @param gameName
	 * @return A "clean" version of the given game name, with no spaces, brackets, etc.
	 * 	Should be safe for use in filepaths (for outputs, experiments, analysis, etc.)
	 */
	public static String cleanGameName(final String gameName)
	{
		return gameName
				.trim()
				.replaceAll(Pattern.quote(" "), "_")
				.replaceAll(Pattern.quote(".lud"), "")
				.replaceAll(Pattern.quote("'"), "")
				.replaceAll(Pattern.quote("("), "")
				.replaceAll(Pattern.quote(")"), "");
	}
	
	/**
	 * @param rulesetName
	 * @return A "clean" version of the given ruleset name
	 */
	public static String cleanRulesetName(final String rulesetName)
	{
		return rulesetName
				.trim()
				.replaceAll(Pattern.quote(" "), "_")
				.replaceAll(Pattern.quote("("), "")
				.replaceAll(Pattern.quote(")"), "")
				.replaceAll(Pattern.quote(","), "")
				.replaceAll(Pattern.quote("\""), "")
				.replaceAll(Pattern.quote("'"), "")
				.replaceAll(Pattern.quote("["), "")
				.replaceAll(Pattern.quote("]"), "");
	}
	
	/**
	 * @param str
	 * @return A copy of the given string, with cleaned up whitespace.
	 * This means that we replace newlines by spaces, trim whitespace from 
	 * the beginning and end, and replace any sequence of more than one 
	 * space (including tabs) by a single space.
	 */
	public static String cleanWhitespace(final String str)
	{
		return str.trim().replaceAll("\\s+", " ");
	}

	//-------------------------------------------------------------------------

	/**
	 * From: https://stackoverflow.com/questions/5439529/determine-if-a-string-is-an-integer-in-java
	 * Rather hacky but guarantees that parseInt() will return an integer value.
	 * @param str
	 * @return Whether str describes an integer.
	 */
	public static boolean isInteger(final String str)
	{
		try
		{
			Integer.parseInt(str);
		}
		catch (@SuppressWarnings("unused") final Exception e)
		{
			return false;
		}
		return true;
	}

	// **
	// ** isFloat() and isFloat() may not be safe. They hang the Token.format() using ZhangShasha.
	// **

	/**
	 * From: https://stackoverflow.com/questions/5439529/determine-if-a-string-is-an-integer-in-java
	 * Rather hacky but guarantees that parseDouble() will return a double value.
	 * @param str
	 * @return Whether str describes a double.
	 */
	public static boolean isFloat(final String str)
	{
//		String strF = "123";
//		float f = Float.parseFloat(strF);
//		System.out.println("f=" + f + " (" + StringRoutines.isFloat(strF) + ")");

//		if (str.indexOf('.') < 0)
//			return false;  // hack: require floats to contain decimal point
		
		try
		{
			Float.parseFloat(str);
		}
		catch (@SuppressWarnings("unused") final Exception e)
		{
			try
			{
				Integer.parseInt(str);
			}
			catch (@SuppressWarnings("unused") final Exception e2)
			{
				return false;
			}
			//return false;
		}
		return true;
	}

	/**
	 * From: https://stackoverflow.com/questions/5439529/determine-if-a-string-is-an-integer-in-java
	 * Rather hacky but guarantees that parseDouble() will return a double value.
	 * @param str
	 * @return Whether str describes a double.
	 */
	public static boolean isDouble(final String str)
	{
		try
		{
			Double.parseDouble(str);
		}
		catch (@SuppressWarnings("unused") final Exception e)
		{
			return false;
		}
		return true;
	}

//	public static boolean isNumber(final String str)
//	{
//		if (str == "")
//			return false;
//		return isNumber(str) || isFloat(str) || isDouble(str);
//	}
	
	//-------------------------------------------------------------------------

	public static boolean isDigit(final char ch)
	{
		return ch >= '0' && ch <= '9';
	}

	public static boolean isLetter(final char ch)
	{
		return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z';
	}

	public static boolean isNumeric(final char ch)
	{
		return ch >= '0' && ch <= '9' || ch == '.' || ch == 'e' || ch == '-';  // // || ch == '+'
	}

	//-------------------------------------------------------------------------

	/**
	 * @param str
	 * @return String with first character converted to lowercase.
	 */
	public static String lowerCaseInitial(final String str)
	{
		if (str.length() < 1)
			return "";
		return Character.toLowerCase(str.charAt(0)) + str.substring(1);
	}

	/**
	 * @param str
	 * @return String with first character converted to uppercase.
	 */
	public static String upperCaseInitial(final String str)
	{
		if (str.length() < 1)
			return "";
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param strings
	 * @return New array of strings, where each of the given strings has its
	 * 	first character converted to uppercase.
	 */
	public static String[] upperCaseInitialEach(final String... strings)
	{
		final String[] ret = new String[strings.length];
		
		for (int i = 0; i < ret.length; ++i)
			ret[i] = upperCaseInitial(strings[i]);
		
		return ret;
	}
	
	//-------------------------------------------------------------------------

	public static boolean isToken(final String str)
	{
		final int lpos = str.indexOf(':');
		if (lpos == -1)
			return false;
		
		for (int c = 0; c < lpos; c++)
			if (!isTokenChar(str.charAt(c)))
				return false;
		
		return true;
	}

	public static boolean isName(final String str)
	{
		final int lpos = str.indexOf(':');
		if (lpos == -1)
			return false;
		
		for (int c = 0; c < lpos; c++)
			if (!isNameChar(str.charAt(c)))
				return false;
		
		return true;
	}

	/**
	 * @return Whether the string is a coordinate, e.g. "A1", "ZZ123", "37"
	 */
	public static boolean isCoordinate(final String str)
	{
		if (str == null)
			return false;

		int c = str.length() - 1;
		if (!isDigit(str.charAt(c)))
			return false;  // last character should always be a digit
		
		while (c >= 0 && isDigit(str.charAt(c)))
			c--;
		
		if (c < 0)
			return true;  // string is all digits, e.g. custom board with no axes

		if (c > 2)
			return false;  // coordinate should have no more two letters
		
		if (c > 1 && str.length() > 1 && str.charAt(0) != str.charAt(1))
			return false;  // if first two chars are a letter, they should be the same, e.g. "AA1".
		
		while (c >= 0 && isLetter(str.charAt(c)))
			c--;
		
		return c < 0;  // whether string is all letters followed by all digits
	}

	//-------------------------------------------------------------------------

	/**
	 * ASCII chart: https://bluesock.org/~willg/dev/ascii.html
	 * @return Whether character is a visible ASCII character.
	 */
	public static boolean isVisibleChar(final char ch)
	{
		// <Space> is 32
		return ch >= 32 && ch < 127; 
	}

	/**
	 * @return Whether character can occur in a named parameter.
	 */
	public static boolean isNameChar(final char ch)
	{
		return 
			ch >= 'a' && ch <= 'z'
			||
			ch >= 'A' && ch <= 'Z'
			||
			ch >= '0' && ch <= '9'
			||
			ch == '_'
			||
			ch == '-' 
			;
	}

	// **
	// ** NOTE: Any tokens in aliases, such as math symbols, must be listed here!
	// **
	public static boolean isTokenChar(final char ch)
	{
		return 
			ch >= 'a' && ch <= 'z'
			||
			ch >= 'A' && ch <= 'Z'
			||
			ch >= '0' && ch <= '9'
			||
			ch == '_'
			||
			ch == '"'  // string
			||
			ch == '-'  // negative number
			||
			ch == '.'  // floating point number
			||
			ch == '+'  // math notation in alias
			||
			ch == '-'  // math notation in alias
			||
			ch == '*'  // math notation in alias
			||
			ch == '/'  // math notation in alias
			||
			ch == '%'  // math notation in alias
			||
			ch == '='  // math notation in alias
			||
			ch == '!'  // math notation in alias
			||
			ch == 'v'  // math notation in alias
			||
			ch == '^'  // math notation in alias
			||
			ch == '~'  // math notation in alias
			||
			ch == '<'  // math notation in alias
			||
			ch == '>'  // math notation in alias
			||
			ch == '&'  // math notation in alias
			||
			ch == '|'  // math notation in alias
			||
			ch == '#'  // hash for hex colour codes #123456
			;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return First token in string, else empty string.
	 */
	public static String getFirstToken(final String str)
	{
		if (str == "")
			return "";
		
		final int len = str.length();
		
		int c = 0;
		while (c < len && !isTokenChar(str.charAt(c)))
			c++;
		
		if (c >= len)
			return "";  // no token chars
		
		int cc = c + 1;
		while (cc < len && isTokenChar(str.charAt(cc)))
			cc++;
		
		if (cc >= len)
			cc = len;  // - 1;
		
		return str.substring(c, cc);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Number at end of string, else -1 if none.
	 *         This will typically be a player index e.g. "Ball1".
	 */
	public static int numberAtEnd(final String strIn)
	{
		final String str = strIn.trim();
	
		boolean found = false;
		int index = 0;
		int tens  = 1;
					
		for (int n = str.length()-1; n >= 0; n--)
		{
			final char ch = str.charAt(n);
			if (!StringRoutines.isDigit(ch))
				break;
						
			found = true;
			index += (ch - '0') * tens;
			tens *= 10;
		}
		
		return found ? index : -1;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Converts given float to a String approximating the float as a fraction
	 * of two integers 
	 * 
	 * Based on: https://stackoverflow.com/a/5968920/6735980
	 * 
	 * @param fIn
	 * @param factor Maximum number we try dividing by
	 * @return String representation of fraction
	 */
	public static String floatToFraction(final float fIn, final int factor)
	{
		final StringBuilder sb = new StringBuilder();
		
		float f = fIn;
	    if (f < 0.f) 
	    {
	        sb.append('-');
	        f = -f;
	    }
	    
	    final long l = (long) f;
	    if (l != 0) 
	    	sb.append(l);
	    
	    f -= l;
	    float error = Math.abs(f);
	    int bestDenominator = 1;
	    
	    for (int i = 2; i <= factor; ++i) 
	    {
	        final float error2 = Math.abs(f - (float) Math.round(f * i) / i);
	        if (error2 < error) 
	        {
	            error = error2;
	            bestDenominator = i;
	        }
	    }
	    
	    if (bestDenominator > 1)
	        sb.append(Math.round(f * bestDenominator)).append('/') .append(bestDenominator);
	    else
	    	sb.append(Math.round(f));
	    
	    return sb.toString();
	}
	
	/**
	 * @param joinStr
	 * @param strings
	 * @return All the given strings merged into a single string, with "joinStr"
	 * used to separate the parts.
	 */
	public static String join(final String joinStr, final List<String> strings)
	{
		return join(joinStr, strings.toArray(new String[strings.size()]));
	}
	
	/**
	 * @param joinStr
	 * @param strings
	 * @return All the given strings merged into a single string, with "joinStr"
	 * used to separate the parts.
	 */
	public static String join(final String joinStr, final String... strings)
	{
		final StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < strings.length; ++i)
		{
			if (i > 0)
				sb.append(joinStr);
			
			sb.append(strings[i]);
		}
		
		return sb.toString();
	}
	
	/**
	 * @param joinStr
	 * @param objects
	 * @return Strings of all the given objects merged into a single string, with "joinStr"
	 * used to separate the parts.
	 */
	public static String join(final String joinStr, final Object... objects)
	{
		final StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < objects.length; ++i)
		{
			if (i > 0)
				sb.append(joinStr);
			
			sb.append(objects[i].toString());
		}
		
		return sb.toString();
	}
	
	/**
	 * @param str
	 * @return The given string, wrapped in a pair of quotes: "str"
	 */
	public static String quote(final String str)
	{
		return "\"" + str + "\"";
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Whitespace indent of suitable length.
	 */
	public static String indent(final int tabSize, final int tabCount)
	{
		final StringBuilder sb = new StringBuilder();
		final int numSpaces = tabSize * tabCount;
		for (int s = 0; s < numSpaces; s++)
			sb.append(" ");
		return sb.toString();
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Removes the trailing numbers from a String.
	 * @param string
	 */
	public static String removeTrailingNumbers(final String string)
	{
		String newString = string;
		if (!newString.chars().allMatch(Character::isDigit))
		{
			int valueToRemove = 0;
			for (int i = newString.length() - 1; i >= 0; i--)
			{
				if (newString.charAt(i) >= '0' && newString.charAt(i) <= '9')
					valueToRemove++;
				else
					break;
			}
			newString = newString.substring(0, newString.length() - valueToRemove);
		}
		return newString;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Gets the trailing numbers from a String.
	 * @param string
	 */
	public static String getTrailingNumbers(final String string)
	{
		String newString = string;
		if (!newString.chars().allMatch(Character::isDigit))
		{
			int valueToRemove = 0;
			for (int i = newString.length() - 1; i >= 0; i--)
			{
				if (newString.charAt(i) >= '0' && newString.charAt(i) <= '9')
					valueToRemove++;
				else
					break;
			}
			newString = newString.substring(newString.length() - valueToRemove);
		}
		return newString;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Unique name of this game, which will be the first String
	 *         argument in (game "Game Name" ...).
	 */
	public static String gameName(final String str)
	{
		int c = str.indexOf("(game ");
		if (c < 0)
		{
			// Don't want to throw exception as might be (match ...)
			return null;  
			//throw new RuntimeException("gameName(): Game object not found.");
		}
		
		c += 5;
		while (c < str.length() && str.charAt(c) != '"')
			c++;
		if (c >= str.length())
			throw new RuntimeException("gameName(): Game name not found.");
		
		final int cc = StringRoutines.matchingQuoteAt(str, c);
		if (cc < 0 || cc >= str.length())
			throw new RuntimeException("gameName(): Game name not found.");
	
		return str.substring(c+1, cc);
	}

	//-------------------------------------------------------------------------
	
	public static String getPlural(final String string) 
	{
		if
		(
			string.endsWith("s") 
			|| 
			string.endsWith("sh")
			|| 
			string.endsWith("ch")
			|| 
			string.endsWith("x")
			|| 
			string.endsWith("z")
		)
		{
			return "es";
		}

		return "s";
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param originalDesc Description of a ruleset after expansion.
	 * @return Formatted description on a single line.
	 */
	public static String formatOneLineDesc(final String originalDesc)
	{
		final StringBuffer formattedDesc = new StringBuffer("");
		for(int i = 0; i < originalDesc.length(); i++)
		{
			final char c = originalDesc.charAt(i);
			if(Character.isLetterOrDigit(c) || c == '(' || c == ')' || c == '{' 
					|| c == '}' || c == '"' || c == '.' || c == ',' || c == ':'
					|| c == '=' || c == '<' || c == '>' || c == '+' || c == '-'
					|| c == '/' || c == '^' || c == '%' || c == '*' || c == '['
					|| c == ']' || c == '#' || c == '?' || Character.isSpaceChar(c)
			)
			{
				if(i != 0 && Character.isSpaceChar(c))
				{
					final char lastChar = formattedDesc.toString().charAt(formattedDesc.length()-1);
					if(!Character.isSpaceChar(lastChar))
					{
						formattedDesc.append(c);
					}
				}
				else 
				{
					formattedDesc.append(c);
					if(c == '{') // add a space after the open curly bracket to not be next to an open parenthesis.
						formattedDesc.append(' ');
				}
			}
		}
		return formattedDesc.toString();
	}
	
}

		
