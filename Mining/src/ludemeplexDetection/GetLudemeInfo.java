package ludemeplexDetection;

import java.util.Collection;
import java.util.List;

import grammar.Grammar;
import main.EditorHelpData;
import main.grammar.LudemeInfo;
import main.grammar.Symbol.LudemeType;

//-----------------------------------------------------------------------------

/**
 * Get ludeme info from JavaDoc help and database.
 *
 * @author cambolbro and Matthew.Stephenson
 */
public class GetLudemeInfo
{

	// Cached version of ludeme information.
	private static List<LudemeInfo> ludemeInfo = null;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Get ludeme info.
	 */
	public static List<LudemeInfo> getLudemeInfo()
	{
		if (ludemeInfo == null)
		{
			final List<LudemeInfo> ludemes = Grammar.grammar().ludemesUsed(); 
			System.out.println(ludemes.size() + " ludemes loaded.");
			
			// Get ids from database for known ludemes
			int idCounter = 1;
			
			// Get JavaDoc help for known ludemes
			final EditorHelpData help = EditorHelpData.get();
	
			for (final LudemeInfo ludeme : ludemes)
			{
				String classPath = ludeme.symbol().cls().getName();
				String description = "";

				if (ludeme.symbol().ludemeType() == null)
				{
					System.out.println("** Null ludemeType for: " + ludeme.symbol());
					continue;
				}
				
				ludeme.setId(idCounter);
				idCounter++;
			
				// Check for ludemes that could be Structural type but aren't
				if 
				(
						ludeme.symbol().usedInGrammar()
						&&
						!ludeme.symbol().usedInDescription()
						&&
						!ludeme.symbol().usedInMetadata()
						&&
						ludeme.symbol().ludemeType() != LudemeType.Structural
						&&
						ludeme.symbol().ludemeType() != LudemeType.Constant
				)
						System.out.println("Could be made a Structural ludeme: " + ludeme.symbol());
					
				if (ludeme.symbol().ludemeType().equals(LudemeType.Primitive))
				{
					if (classPath.equals("int"))
						description = "An integer value.";
					else if (classPath.equals("float"))
						description = "A floating point value.";
					else if (classPath.equals("boolean"))
						description = "A boolean value.";
				}
				else if (ludeme.symbol().ludemeType().equals(LudemeType.Constant))
				{
					// Handle enum constant
					classPath += "$" + ludeme.symbol().name();
					
					String key = classPath.replace('$', '.');
					Collection<String> enums = help.enumConstantLines(key);
					
					// Get list of descriptions for this enum type
					if (enums==null || enums.size()==0) 
					{
						final String[] parts = classPath.split("\\$");
						key = parts[0];
						enums = help.enumConstantLines(key);
					}
	
					// Find matching description
					if (enums != null)
					{
						for (final String str : enums)
						{
							final String[] parts = str.split(": ");
							if (parts[0].equals(ludeme.symbol().name()))
							{
								description = parts[1];
								break;
							}
						}
					}
				}
				else
				{
					// Is ludeme class
					description = help.typeDocString(classPath);
				}
	
				ludeme.setDescription(description);
			}
			
			ludemeInfo = ludemes;
		}
		
		return ludemeInfo;
	}
	
	//-------------------------------------------------------------------------
	
	public static void main(final String[] args)
	{
		getLudemeInfo();
	}
	
	//-------------------------------------------------------------------------

}
