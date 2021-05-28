package app.display.dialogs.editor;

import java.util.List;
import java.util.ArrayList;

public class LudiiTokeniser
{
	private static final char OPEN_PARENTHESES = '(';
	private static final char CLOSE_PARENTHESES = ')';
	private static final char OPEN_CURLY = '{';
	private static final char CLOSE_CURLY = '}';
	private static final char OPEN_SQUARE = '[';
	private static final char CLOSE_SQUARE = ']';
	private static final char OPEN_ANGLE = '<';
	private static final char CLOSE_ANGLE = '>';
	private static final char STRING_DELIMITER = '"';
	private static final char LABEL_DELIMITER = ':';

	private final List<String> tokens = new ArrayList<>();
	private final StringBuilder token = new StringBuilder();

	public LudiiTokeniser (final String gameDescription)
	{
		boolean inString = false;
		boolean inNumber = false;
		
		boolean whitespaceLast = false;
		
		for (char ch: gameDescription.toCharArray()) 
		{
			// Deal with any special cases...
			if (inString) 
			{
				token.append(ch);
				if (ch == STRING_DELIMITER) 
				{
					startNewToken();
					inString=false;
				}
				
				continue;
			}
			
			if (inNumber) {
				if (isNumber(ch)) {
					token.append(ch);
					continue;
				} else {
					startNewToken();
					inNumber=false;
					// Fall through
				}
			}

			// Keep whitespace separate from normal characters for easy identification of tokens
			final boolean isWhitespace = Character.isWhitespace(ch);
			if (whitespaceLast != isWhitespace) startNewToken();
			whitespaceLast = isWhitespace;
			
			switch (ch)
			{
			case OPEN_PARENTHESES:
			case CLOSE_PARENTHESES:
			case OPEN_CURLY:
			case CLOSE_CURLY:
			case OPEN_SQUARE:
			case CLOSE_SQUARE:
			case OPEN_ANGLE:
			case CLOSE_ANGLE:
				startNewToken();
				addCompleteToken(ch);
				break;
				
			case STRING_DELIMITER:
				startNewToken();
				inString=true;
				token.append(ch);
				break;			
				
			case LABEL_DELIMITER:
				token.append(ch);
				startNewToken();
				break;

			default:
				if (isNumber(ch))
				{
					startNewToken();
					inNumber=true;
				}
				token.append(ch);
				break;
			}
		}
		startNewToken();
	}
	
	private static boolean isNumber(char ch)
	{
		final boolean isDigit = (ch=='+'||ch=='-'||ch=='.'||Character.isDigit(ch));
		return isDigit;
	}


	private boolean addCompleteToken(char ch)
	{
		return tokens.add(String.valueOf(ch));
	}

	private void startNewToken()
	{
		if (token.length()!=0) 
		{
			tokens.add(token.toString());
			token.setLength(0);
		}
	}

	/**
	 * @return all tokens in this file, as an array
	 */
	public String[] getTokens()
	{
		return tokens.toArray(new String[0]);
	}
	
	/**
	 * @param token
	 * @param inAngle 
	 * @param lastToken 
	 * @return true if this starts a new level of indentation
	 */
	public static EditorTokenType typeForToken (final String token, final boolean inAngle, final EditorTokenType lastToken)
	{
		if (token == null || token.length()==0) return EditorTokenType.OTHER;
		if (token.length()==1) 
		{
			switch (token.charAt(0))
			{
			case OPEN_PARENTHESES: return EditorTokenType.OPEN_ROUND;
			case OPEN_CURLY: return EditorTokenType.OPEN_CURLY;
			case OPEN_SQUARE: return EditorTokenType.OPEN_SQUARE;
			case OPEN_ANGLE: return EditorTokenType.OPEN_ANGLE;
			case CLOSE_PARENTHESES: return EditorTokenType.CLOSE_ROUND;
			case CLOSE_CURLY: return EditorTokenType.CLOSE_CURLY;
			case CLOSE_SQUARE: return EditorTokenType.CLOSE_SQUARE;
			case CLOSE_ANGLE: return EditorTokenType.CLOSE_ANGLE;
			}
		}
		if (token.charAt(0) == STRING_DELIMITER) return EditorTokenType.STRING;
		if (isFloat(token)) return EditorTokenType.FLOAT;
		if (isInteger(token)) return EditorTokenType.INT;
		if (token.endsWith(Character.toString(LABEL_DELIMITER))) return EditorTokenType.LABEL;
		if (token.trim().isEmpty()) return EditorTokenType.WHITESPACE;
		if (inAngle) return EditorTokenType.RULE;
		if (lastToken != null && lastToken==EditorTokenType.OPEN_ROUND && Character.isLowerCase(token.charAt(0))) return EditorTokenType.CLASS; 
		if (Character.isUpperCase(token.charAt(0))) return EditorTokenType.ENUM; 
		
		return EditorTokenType.OTHER;
	}

	/**
	 * @param token
	 * @return true if this should be formatted as an integer
	 */
	private static boolean isInteger (final String token)
	{
		try {
			Long.parseLong(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * @param token
	 * @return true if this should be formatted as a floating point number
	 */
	public static boolean isFloat (final String token)
	{
		if (!token.contains(".")) return false;
		try {
			Double.parseDouble(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}