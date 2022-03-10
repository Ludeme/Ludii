package parser;

import java.util.ArrayList;
import java.util.List;

import main.grammar.Description;
import main.grammar.Report;

//-----------------------------------------------------------------------------

/**
 * Completes partial game descriptions ready for expansion.
 * @author cambolbro
 */
public class Completer
{
	
	//-------------------------------------------------------------------------

	public static boolean needsCompleting(final Description description)
	{
		final String rawGame = description.rawGameDescription();
		System.out.println("Raw game description is:\n" + rawGame);
		
		return rawGame.contains("[") && rawGame.contains("]");
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param raw    Partial raw game description.
	 * @param report Report log for warnings and errors.
	 * @return List of completed (raw) game descriptions ready for expansion and parsing.        
	 */
	public static List<String> complete(final String raw, final Report report)
	{
		// Create list of alternative Descriptions, as each will need to be expanded
		final List<String> completions = new ArrayList<String>();
		
		// ...
		
		return completions;
	}

	//-------------------------------------------------------------------------

}
