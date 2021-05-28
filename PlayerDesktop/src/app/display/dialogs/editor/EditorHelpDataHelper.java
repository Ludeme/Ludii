package app.display.dialogs.editor;

import static app.display.dialogs.editor.EditorLookAndFeel.BR;
import static app.display.dialogs.editor.EditorLookAndFeel.CELL_END;
import static app.display.dialogs.editor.EditorLookAndFeel.CELL_START;
import static app.display.dialogs.editor.EditorLookAndFeel.DOC_ROW_START;
import static app.display.dialogs.editor.EditorLookAndFeel.DOC_TABLE_START;
import static app.display.dialogs.editor.EditorLookAndFeel.HEADING_END;
import static app.display.dialogs.editor.EditorLookAndFeel.HEADING_START;
import static app.display.dialogs.editor.EditorLookAndFeel.KEYWORD_END;
import static app.display.dialogs.editor.EditorLookAndFeel.KEYWORD_START;
import static app.display.dialogs.editor.EditorLookAndFeel.MIN_CELL_DISTANCE;
import static app.display.dialogs.editor.EditorLookAndFeel.PARAM_TABLE_START;
import static app.display.dialogs.editor.EditorLookAndFeel.ROW_END;
import static app.display.dialogs.editor.EditorLookAndFeel.ROW_START;
import static app.display.dialogs.editor.EditorLookAndFeel.TABLE_END;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import parser.KnownDefines;
import main.EditorHelpData;

//-----------------------------------------------------------------------------

/**
 * Formats the editor help data.
 *  
 * @author cambolbro
 */
public final class EditorHelpDataHelper
{
	private static boolean VERBOSE = true;
	
	/**
	 * @param type
	 * @param n
	 * @return full document for a given constructor
	 */
	public static final String fullDocumentForConstructor(final EditorHelpData data, final String type, final int n)
	{
		System.out.println("type: " + type);
		
		final StringBuilder sb = new StringBuilder();
		
		sb.append(DOC_TABLE_START);
		sb.append(DOC_ROW_START);
		sb.append(CELL_START);
		sb.append(escapeForHTML(data.typeDocString(type)));
		
		final String remarks = data.typeRemarksString(type);
		if (remarks != null && !remarks.isEmpty()) 
		{
			//sb.append(REMARK_START);
			sb.append(" <br> ");
			sb.append(escapeForHTML(remarks));
			//sb.append(REMARK_END);
		}
		
		sb.append(CELL_END);
		sb.append(ROW_END);
		
		// If there are no distinct constructors, we're finished
		if (n < 0) 
		{
			sb.append(TABLE_END);
			return sb.toString();
		}
				
		sb.append(DOC_ROW_START);
		sb.append(CELL_START).append(highlightKeyword(escapeForHTML(data.nthConstructorLine(type, n)))).append(CELL_END);
		sb.append(ROW_END);

		final List<String> paramLines = data.nthConstructorParamLines(type, n);
		if (paramLines != null && paramLines.size() > 0) 
		{
			sb.append(DOC_ROW_START);
			sb.append(CELL_START);
			
			sb.append(HEADING_START).append("Parameters").append(HEADING_END);
			
			sb.append(PARAM_TABLE_START);
			for (final String line: paramLines)
			{
				final int pos = line.lastIndexOf(":");
				if (pos > 0) {
					sb.append(ROW_START);
					sb.append(CELL_START).append(escapeForHTML(line.substring(0, pos).trim())).append(MIN_CELL_DISTANCE).append(CELL_END);
					sb.append(CELL_START).append(escapeForHTML(line.substring(pos+1).trim())).append(CELL_END);
					sb.append(ROW_END);
				} 
				else 
				{
					sb.append(ROW_START).append(CELL_START).append(escapeForHTML(line)).append(CELL_END).append(ROW_END);
				}
			}
			sb.append(TABLE_END);
			
			sb.append(CELL_END);
			sb.append(ROW_END);
		}
		
		final List<String> exampleLines = data.nthConstructorExampleLines(type, n);
		if (exampleLines != null && exampleLines.size() > 0) 
		{
			sb.append(DOC_ROW_START);
			sb.append(CELL_START);
			
			sb.append(HEADING_START).append("Examples").append(HEADING_END);
			for (final String line: exampleLines)
			{
				sb.append(BR);
				sb.append(escapeForHTML(line));
			}
			sb.append(CELL_END);
			sb.append(ROW_END);
		}

		sb.append(TABLE_END);

		return sb.toString();
	}

	/**
	 * @param text
	 * @return the keyword embedded in this text
	 */
	public static final String extractKeyword (final String text)
	{
		if (text==null || text.length()==0) return "";
		if (text.charAt(0) != '(' && text.charAt(0) != '<' && text.charAt(0) != '[' && text.charAt(0) != '{') return text;
		
		int pos = 1;
		while (pos < text.length() && Character.isLetterOrDigit(text.charAt(pos)))
			pos++;
		
		return text.substring(1,pos);
	}

	/**
	 * @param text
	 * @return same text with the keyword highlighted
	 */
	public static final String highlightKeyword (final String text)
	{
		if (text==null || text.length()==0) return "";
		if (text.charAt(0) != '(') return text;
		int pos = 1;
		while (pos < text.length() && Character.isLetterOrDigit(text.charAt(pos)))
			pos++;
		
		return text.substring(0,1)+KEYWORD_START+text.substring(1,pos)+KEYWORD_END+text.substring(pos);
	}

	/**
	 * @param text
	 * @return text with various HTML entities escaped which might otherwise cause formatting problems
	 */
	public static final String escapeForHTML(final String text)
	{
		if (text==null|| text.isEmpty()) return "";
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}

	/**
	 * @param data
	 * @param rawCandidates
	 * @param isPartial in partial mode, only the main javadoc is shown, and constructors are stripped
	 * @return Suggestion lists matching these candidates.
	 */
	public static final List<SuggestionInstance> suggestionsForClasspaths
	(
		final EditorHelpData data, 
		final List<String> rawCandidates, 
		final boolean isPartial
	)
	{
		final List<SuggestionInstance> suggestions = new ArrayList<>();
		final Set<String> allCandidates = expandHierarchy(data, rawCandidates);
		
		Set<String> uniqueEnums = new HashSet<>();
		Set<String> uniqueConstructors = new HashSet<>();
		
		for (final String classPath : allCandidates)
		{
			if (isDefine(classPath)) 
			{
				final StringBuilder sb = new StringBuilder();
				
				sb.append(escapeForHTML(data.defineDocString(classPath)));
				
				final List<String> exampleLines = data.defineExampleLines(classPath);
				if (exampleLines != null && exampleLines.size() > 0) 
				{
					// FIXME - should use the same formatting as fullDocumentForConstructor, whatever that is!
					sb.append(BR);
					sb.append(BR);
					sb.append(HEADING_START).append("Examples").append(HEADING_END);
					for (final String line: exampleLines)
					{
						sb.append(BR);
						sb.append(escapeForHTML(line));
					}
				}
				
				if (isPartial)
				{
					final String token = extractKeyword(classPath);
					if (uniqueConstructors.add(token))
						suggestions.add(new SuggestionInstance(classPath, token, token, sb.toString()));
				}
				else
				{
					suggestions.add(new SuggestionInstance(classPath, classPath, classPath, sb.toString()));					
				}
			}
			
			if (isEnum(classPath)) 
			{
				String key = classPath.replace('$', '.');
				Collection<String> enums = data.enumConstantLines(key);
				if (enums==null || enums.size()==0) 
				{
					final String[] parts = classPath.split("\\$");
					key = parts[0];
					enums = data.enumConstantLines(key);
				}

				if (enums==null || enums.size()==0) 
				{
					if (VERBOSE) System.out.println("Can't find enums for "+classPath);
					continue;
				}
				
				if (VERBOSE) System.out.println("Processing "+enums.size()+" enums for "+classPath+": "+enums);
				
				final String javadoc = data.typeDocString(key);

				for (final String label : enums)
				{
					final int pos = label.indexOf(":");
					if (pos > 0) 
					{
						final String substitution = label.substring(0,pos).trim();
						final String embeddedDoc = label.substring(pos+1).trim();
						if (uniqueEnums.add(substitution))
							suggestions.add(new SuggestionInstance(classPath, label, substitution, javadoc+BR+BR+embeddedDoc));
					} 
					else 
					{
						if (uniqueEnums.add(label))
							suggestions.add(new SuggestionInstance(classPath, label, label, javadoc));
					}
				}
			}
			else if (classPath.equalsIgnoreCase("true")) 
			{
				// This would ideally be part of the grammar, but that would not 
				// let us distinguish between primitive boolean and BooleanFunction
				suggestions.add(new SuggestionInstance("false", "false", "false", "Make condition false."));
			}
			else if (classPath.equalsIgnoreCase("false"))
			{
				// This would ideally be part of the grammar, but that would not 
				// let us distinguish between primitive boolean and BooleanFunction
				suggestions.add(new SuggestionInstance("true", "true", "true", "Make condition true."));
			}
			else 
			{
				final int count = data.numConstructors(classPath);
				if (count > 0)
				{
					if (VERBOSE) System.out.println("Found "+count+" constructors for "+classPath);
					
					if (isPartial)
					{
						final String label = data.nthConstructorLine(classPath, 0);
						final String token = extractKeyword(label);
						if (uniqueConstructors.add(token))
						{
							final String javadoc = fullDocumentForConstructor(data, classPath, -1);
							suggestions.add(new SuggestionInstance(classPath, token, token, javadoc));
						}
					}
					else
					{
						// Is class with constructors
						for (int n = 0; n < count; n++)
						{
							final String label = data.nthConstructorLine(classPath, n);
							if (VERBOSE) System.out.println( "#"+n+": "+label);
	
							final String javadoc = fullDocumentForConstructor(data, classPath, n);
							suggestions.add(new SuggestionInstance(classPath, label, label, javadoc));
						}
					}
				}
				else
				{
					final String key = classPath;
					
					final String javadoc = data.typeDocString(key);
					
					final List<String> enums = data.enumConstantLines(key);
					if (enums != null && enums.size() > 0)
					{
						if (VERBOSE) System.out.println("Processing "+enums.size()+"enum constant lines for "+key+": "+enums);

						for (final String label : enums)
						{
							final int pos = label.indexOf(":");
							if (pos > 0) 
							{
								final String substitution = label.substring(0,pos).trim();
								final String embeddedDoc = label.substring(pos+1).trim();
								suggestions.add(new SuggestionInstance(classPath, label, substitution, javadoc+BR+BR+embeddedDoc));
							} 
							else 
							{
								suggestions.add(new SuggestionInstance(classPath, label, label, javadoc));
							}
						}
					}
					
					final List<String> subclasses = data.subclassDocLines(classPath);
					if (subclasses != null && subclasses.size() > 0)
					{
						for (final String label : subclasses)
						{
							final int pos = label.indexOf(":");
							if (pos > 0) 
							{
								final String substitution = label.substring(0,pos).trim();
								final String embeddedDoc = label.substring(pos+1).trim();
								suggestions.add(new SuggestionInstance(classPath, label, substitution, javadoc+BR+BR+embeddedDoc));
							} 
							else 
							{
								suggestions.add(new SuggestionInstance(classPath, label, label, javadoc));
							}
						}
						
					}
				}
				
			}
		}
		
		return suggestions;
	}
	
	private static Set<String> expandHierarchy(final EditorHelpData data, final List<String> rawCandidates)
	{
		final Set<String> results = new HashSet<>();
		
		if (VERBOSE) System.out.println("Expanding: "+rawCandidates);
		
		for (int pos = 0; pos < rawCandidates.size(); pos++)
		{
			final String candidate = rawCandidates.get(pos);
			final String key = removeAngleBrackets(candidate);
			
			final List<String> subclasses = data.subclassDocLines(key);
			if (subclasses != null && subclasses.size() > 0)
				results.addAll(expandHierarchy(data, subclasses));
			else if (data.numConstructors(key) > 0 ||  "true".equals(candidate) || "false".equals(candidate) || isEnum(key)) 
				results.add(key);
		}
		
		return results;
	}

	private static String removeAngleBrackets(final String candidate)
	{
		if (candidate.startsWith("<")) return candidate.substring(1, candidate.indexOf(">"));
		return candidate;
	}

	/**
	 * @param label
	 * @return label with markup
	 */
	public static final String formatLabel (final String label)
	{
		// Split at whitespace, fix HTML markup... ampersand must be first!
		final String[] tokens = escapeForHTML(label).split(" ");
		
		// Add markup		
		if (tokens[0].startsWith("("))
			tokens[0] = "("+KEYWORD_START+tokens[0].substring(1)+KEYWORD_END; 
		else
			tokens[0] = KEYWORD_START+tokens[0]+KEYWORD_END;
		
		final String result = "<html>"+String.join("&nbsp;", tokens)+"</html>";
		return result;
	}
	
    private static boolean isEnum(final String classPath)
	{
		return classPath.contains("$");
	}

	private static final boolean isDefine(final String classPath)
	{
		final String key = EditorHelpDataHelper.extractKeyword(classPath);
		return KnownDefines.getKnownDefines().knownDefines().get(key) != null;
	}

}
