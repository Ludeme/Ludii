package main.options;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.StringRoutines;

//-----------------------------------------------------------------------------

/**
 * Record of an "(option ...)" instance.
 * 
 * @author cambolbro and Dennis Soemers and Eric.Piette and Matthew.Stephenson
 */
public class Ruleset
{
	/** Heading for this ruleset as it appears in the menu, e.g. "Standard Rules (Known)". */
	private String heading = null;
	
	/** List of option selections for this rule set. */
	private final List<String> optionSettings = new ArrayList<String>();

	/** Map between option header and option tags. */
	private final Map<String, List<String>> variations = new HashMap<String, List<String>>();
	
	/** Ruleset's priority. */
	private int priority = 0;
	
	//------------------------------------------------------------------------

	public Ruleset(final String str)
	{
		try { interpret(str); } 
		catch (final Exception e) { e.printStackTrace(); }
	}
		
	//------------------------------------------------------------------------
	
	public String heading()
	{
		return heading;
	}
	
	/**
	 * @return List of options that this ruleset selects.
	 */
	public List<String> optionSettings()
	{
		return Collections.unmodifiableList(optionSettings);
	}
	
	/**
	 * @return Map of allowed option variations within this ruleset for each option
	 *         header.
	 */
	public Map<String, List<String>> variations()
	{
		return variations;
	}
	
	/**
	 * @return The priority.
	 */
	public int priority()
	{
		return priority;
	}
	
	//------------------------------------------------------------------------

	/**
	 * Interprets ruleset from a given string.
	 */
	void interpret(final String strIn)
	{
		// Ruleset format:
		//
		//     (ruleset “Standard Game (Known)” { "Board Size/6x6” "End Rule/Standard" })
		//
		//     (ruleset false “Not in Database” { "Board Size/5x5” "End Rule/Standard" })

//		System.out.println("Interpreting ruleset:\n" + strIn);
		
		String str = new String(strIn).trim();
		
		// Extract priority (number of asterisks appended)
		priority = 0;
		while (str.charAt(str.length()-1) == '*')
		{
			priority++;
			str = str.substring(0, str.length()-1);  // strip of rightmost asterisk
		}
//		System.out.println("ruleset priority: " + priority);

		// Strip off opening and closing chars
		int c = str.indexOf("(ruleset ");
		if (c < 0)
			throw new RuntimeException("Ruleset not found: " + str);
			
		int cc = StringRoutines.matchingBracketAt(str, c);
		if (cc < 0)
			throw new RuntimeException("No closing bracket ')' in ruleset: " + str);

		str = extractVariations(str);
				
		// Extract menu heading
		c = str.indexOf('"');
		if (c < 0)
			throw new RuntimeException("Ruleset heading not found: " + str);

		cc = c + 1;
		while (cc < str.length() && (str.charAt(cc) != '"' || str.charAt(cc - 1) == '\\'))
			cc++;
		if (cc < 0)
			throw new RuntimeException("No closing quote for ruleset heading: " + str);
	
		heading = str.substring(c+1, cc);
		
//		System.out.println("Heading: " + heading);
		
		// Move past heading string
		str = str.substring(cc+1).trim();
		
//		System.out.println("Reduced ruleset is: " + str);

		// Extract option settings
		while (true)
		{
			c = str.indexOf('"');
			if (c < 0)
				break;  // no more option settings

			cc = c + 1;
			while (cc < str.length() && str.charAt(cc) != '"')
				cc++;
			if (cc < 0)
				throw new RuntimeException("No closing quote for option setting: " + str);

			final String option = str.substring(c+1, cc);
			optionSettings.add(option);
			
//			System.out.println("-- option: " + option);
		
			str = str.substring(cc+1).trim();  // move past option string			
		}

		// Add the default tags to the possible variations tags.
		for (final String variation : optionSettings)
		{
			final String header = variation.substring(0, variation.indexOf('/'));
			final String tag = variation.substring(variation.indexOf('/') + 1, variation.length());
			if (variations.containsKey(header))
				variations.get(header).add(0, tag);
		}
	}
	
	//------------------------------------------------------------------------

	/**
	 * Interprets ruleset from a given string.
	 */
	private String extractVariations(final String strIn)
	{
		final List<String> vars = new ArrayList<String>();
		vars.clear();
		
		final int varAt = strIn.indexOf("variations:");
		if (varAt == -1)
			return strIn;  // no variations
		
		int openAt = varAt + 11;
		while (openAt < strIn.length() && strIn.charAt(openAt) != '{')
			openAt++;
		
		if (openAt >= strIn.length())
			throw new RuntimeException("No opening bracket for ruleset variations: " + strIn);
		
		final int closeAt = StringRoutines.matchingBracketAt(strIn, openAt);
		if (closeAt == -1)
			throw new RuntimeException("No closing bracket for ruleset variations: " + strIn);
		
		// Extract menu heading
		int c = openAt + 1;

		// Extract ruleset variations
		while (true)
		{
			c = strIn.indexOf('"', c);
			if (c < 0)
				break;  // no more option settings

			int cc = c + 1;
			while (cc < strIn.length() && strIn.charAt(cc) != '"')
				cc++;
			if (cc < 0)
				throw new RuntimeException("No closing quote for option variation: " + strIn.substring(c));

			final String varn = strIn.substring(c+1, cc);
			vars.add(varn);

			c = cc + 1;
		}
		
		// Get the map of variations according to each header.
		for (final String variation : vars)
		{
			final String header = variation.substring(0, variation.indexOf('/'));
			final String tag = variation.substring(variation.indexOf('/') + 1, variation.length());
			if (!variations.containsKey(header))
				variations.put(header, new ArrayList<String>());
			variations.get(header).add(tag);
		}

		// Return string with variations section removed
		return strIn.substring(0, varAt) + strIn.substring(closeAt + 1);
	}
	
	
	//------------------------------------------------------------------------
	
	/**
	 * @return List of all option string lists (optionSettings) that this ruleset can have.
	 */
	public List<List<String>> allOptionSettings()
	{
		List<List<String>> allOptionSettings = new ArrayList<>();
		
		if (variations.isEmpty())
		{
			allOptionSettings.add(optionSettings());
		}
		else
		{
			allOptionSettings.add(new ArrayList<>());
			for (final String OptionHeader : variations.keySet())
			{
				final List<List<String>> nextOptionSettings = new ArrayList<>();
			    for (final List<String> optionSetting : allOptionSettings)
			    {
			    	for (int i = 0; i < variations.get(OptionHeader).size(); i++) 
				    {
			    		final List<String> newOptionSetting = new ArrayList<>(optionSetting);
			    		newOptionSetting.add(OptionHeader + "/" + variations.get(OptionHeader).get(i));
			    		nextOptionSettings.add(newOptionSetting);
				    }
			    }
			    allOptionSettings = new ArrayList<>(nextOptionSettings);
			}
		}
		
		return allOptionSettings;
	}
	
	//------------------------------------------------------------------------

//	/**
//	 * Set option selections based on this ruleset.
//	 */
//	@Deprecated
//	public void setOptionSelections(final GameOptions gameOptions, final int[] selections)
//	{
//		final BitSet used = new BitSet();
//		
//		final int numCategories = gameOptions.categories().size();
//		
//		for (final String optionSetting : optionSettings)
//		{
////			System.out.println("Handling optionSetting: " + optionSetting);
//			
//			final String[] subs = optionSetting.split("/");
//			
//			if (subs.length < 2)
//				throw new RuntimeException("Badly formed option heading: " + optionSetting);
//			
//			for (int cat = 0; cat < numCategories; cat++)
//			{
//				final OptionCategory category = gameOptions.categories().get(cat); 
//				if (category.heading().equals(subs[0]))
//				{
//					// Found the category
////					System.out.println("+ matches category: " + category);
//					
//					for (int o = 0; o < category.options().size(); o++)
//					{	
//						final Option option = category.options().get(o);
//						if (option.menuHeadings().get(1).equals(subs[1]))
//						{
//							// Found the options
////							System.out.println("+ + and matches option: " + option);
//
//							if (used.get(cat))
//								throw new RuntimeException("Option category already set in ruleset: " + optionSetting);
//							
//							selections[cat] = o;
//							used.set(cat, true);
//						}
//					}
//				}
//			}
//		}
//		
//		if (used.cardinality() != numCategories)
//			throw new RuntimeException("Not all options are specified in ruleset: " + toString());
//	}
	
	//------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("[\"" + heading + "\" {");
		for (final String option : optionSettings)
			sb.append(" \"" + option + "\"");
		sb.append(" }]");
		return sb.toString();
	}
	
	//------------------------------------------------------------------------
	
}
