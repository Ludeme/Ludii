package main.options;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import exception.DuplicateOptionUseException;
import exception.UnusedOptionException;
import main.Constants;
import main.StringRoutines;

/**
 * Maintains a list of game option categories, with the current user selection for each.
 * 
 * @author cambolbro and mrraow
 */
public class GameOptions
{
	/** Maximum number of option categories. */
	public static final int MAX_OPTION_CATEGORIES = 10;
	
	/** List of option categories. */
	private final List<OptionCategory> categories = new ArrayList<OptionCategory>();

	/** Whether options have been loaded for this Game instance. */
	private boolean optionsLoaded = false;

	//-------------------------------------------------------------------------
	
	public List<OptionCategory> categories()
	{
		return Collections.unmodifiableList(categories);
	}

//	public int currentOption(final int category)
//	{
//		return categories.get(category).selection();
//	}
//
//	public void setCurrentOptions(final int[] selections)
//	{
//		for (int n = 0; n < Math.min(selections.length, categories.size()); n++)
//			categories.get(n).setSelection(selections[n]);
//		optionsLoaded = true;
//	}
//	
//	public void setCurrentOption(final int category, final int selection)
//	{
//		categories.get(category).setSelection(selection);
//	}

	public boolean optionsLoaded()
	{
		return optionsLoaded;
	}
	
	public void setOptionsLoaded(final boolean set)
	{
		optionsLoaded = set;
	}

	public void setOptionCategories(final List<Option>[] optionsAvList)
	{
		categories.clear();
		for (int n = 0; n < optionsAvList.length; n++)
			categories.add(new OptionCategory(optionsAvList[n]));
		optionsLoaded = true;
	}
	
//	public String currentSelectionsAsString()
//	{
//		final StringBuilder sb = new StringBuilder();
//		sb.append("[");
//		for (int n = 0; n < categories.size(); n++)
//		{
//			if (n > 0)
//				sb.append(", ");
//			sb.append(categories.get(n).selection());
//		}
//		sb.append("]");
//		return sb.toString();
//	}
//	
//	public List<Integer> currentSelectionsAsList()
//	{
//		final List<Integer> list = new ArrayList<Integer>();
//		for (final OptionCategory category : categories)
//			list.add(new Integer(category.selection()));
//		return list;
//	}
	
//	public List<Integer> currentSelectionsAsList(final int[] optionSelections)
//	{
//		final List<Integer> list = new ArrayList<Integer>();
//		for (int cat = 0; cat < categories.size(); cat++)
//		//for (final OptionCategory category : categories)
//			list.add(new Integer(optionSelections[cat]));  //category.selection()));
//		return list;
//	}

	//-------------------------------------------------------------------------

	public void clear()
	{
		categories.clear();
		optionsLoaded = false;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return The number of option categories we have
	 */
	public int numCategories()
	{
		return categories.size();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Add this option to the relevant option category, else create a new option category.
	 */
	public void add(final Option option)
	{
		// Search for existing option category with this option's tag
		for (final OptionCategory category : categories)
			if (option.tag().equals(category.tag()))
			{
				category.add(option);
				return;
			}
		
		// Start a new option category
		final OptionCategory category = new OptionCategory(option);
		categories.add(category);
	}
	
	public void add(final OptionCategory category)
	{
		categories.add(category);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param selectedOptionStrings Strings for current option string selections. 
	 * 	We assume default options for any categories without explicit selections.
	 * @return An int array with for each option category, the index of the
	 * 	current option within that category.
	 */
	public int[] computeOptionSelections(final List<String> selectedOptionStrings)
	{
		final int[] optionSelections = new int[numCategories()];
		final boolean[] usedOptionStrings = new boolean[selectedOptionStrings.size()];
		
		for (int cat = 0; cat < categories.size(); cat++)
		{
			final OptionCategory category = categories.get(cat);
			
			int maxPriority = Integer.MIN_VALUE;
			int activeOptionIdx = Constants.UNDEFINED;
			for (int i = 0; i < category.options().size(); i++)
			{
				final Option option = category.options().get(i);
				final String optionStr = StringRoutines.join("/", option.menuHeadings());
				
				final int optionStrIndex = selectedOptionStrings.indexOf(optionStr);
				if (optionStrIndex >= 0)
				{
					// This option was explicitly selected, so we take it and we break
					if (usedOptionStrings[optionStrIndex])
						throw new DuplicateOptionUseException(optionStr);
					
					usedOptionStrings[optionStrIndex] = true;
					activeOptionIdx = i;
					break;
				}
				
				if (option.priority() > maxPriority)
				{
					// New max priority option; this is what we'll take if we don't find an explicitly selected option
					activeOptionIdx = i;
					maxPriority = option.priority();
				}
			}
			
			optionSelections[cat] = activeOptionIdx;
		}
		
		for (int i = 0; i < usedOptionStrings.length; ++i)
		{
			if (!usedOptionStrings[i])
				throw new UnusedOptionException(selectedOptionStrings.get(i));
		}
		
		return optionSelections;
	}
	
	/**
	 * @param selectedOptionStrings Strings of explicitly selected options. May
	 * 	not contain any Strings for categories left at defaults
	 * @return A list of strings for ALL active options (including Strings for
	 * default options if no non-defaults were selected in their categories)
	 */
	public List<String> allOptionStrings(final List<String> selectedOptionStrings)
	{
		final List<String> strings = new ArrayList<String>();
		final boolean[] usedOptionStrings = new boolean[selectedOptionStrings.size()];
		
		for (int cat = 0; cat < categories.size(); cat++)
		{
			final OptionCategory category = categories.get(cat);
			
			int maxPriority = Integer.MIN_VALUE;
			String activeOptionStr = null;
			for (int i = 0; i < category.options().size(); i++)
			{
				final Option option = category.options().get(i);
				final String optionStr = StringRoutines.join("/", option.menuHeadings());
				
				final int optionStrIndex = selectedOptionStrings.indexOf(optionStr);
				if (optionStrIndex >= 0)
				{
					// This option was explicitly selected, so we take it and we break
					if (usedOptionStrings[optionStrIndex])
						throw new DuplicateOptionUseException(optionStr);
					
					usedOptionStrings[optionStrIndex] = true;
					activeOptionStr = optionStr;
					break;
				}
				
				if (option.priority() > maxPriority)
				{
					// New max priority option; this is what we'll take if we don't find an explicitly selected option
					activeOptionStr = optionStr;
					maxPriority = option.priority();
				}
			}
			
			strings.add(activeOptionStr);
		}
		
		for (int i = 0; i < usedOptionStrings.length; ++i)
		{
			if (!usedOptionStrings[i])
				throw new UnusedOptionException(selectedOptionStrings.get(i));
		}
		
		return strings;
	}
	
	/**
	 * @param optionString
	 * @return True if an option described by the given String exists, false otherwise
	 */
	public boolean optionExists(final String optionString)
	{
		for (int cat = 0; cat < categories.size(); cat++)
		{
			final OptionCategory category = categories.get(cat);
			
			for (int i = 0; i < category.options().size(); i++)
			{
				final Option option = category.options().get(i);
				final String optionStr = StringRoutines.join("/", option.menuHeadings());
				
				if (optionString.equals(optionStr))
					return true;
			}
		}
		
		return false;
	}

	/**
	 * @param optionSelections
	 * @return List of strings describing selected options in int-array format
	 */
	public List<String> toStrings(final int[] optionSelections)
	{
		final List<String> strings = new ArrayList<String>();
		
		for (int cat = 0; cat < categories.size(); cat++)
		{
			final OptionCategory category = categories.get(cat);
			
			final int selection = optionSelections[cat];
			final Option option = category.options().get(selection);
			final List<String> headings = option.menuHeadings();
			strings.add(StringRoutines.join("/", headings));
		}
		
		return strings;
	}
	
	/**
	 * @param selectedOptionStrings List of Strings describing explicitly-selected options
	 * @return List of Option objects for all active objects
	 */
	public List<Option> activeOptionObjects(final List<String> selectedOptionStrings)
	{
		final List<Option> options = new ArrayList<Option>(numCategories());
		
		final int[] selections = computeOptionSelections(selectedOptionStrings);
		for (int i = 0; i < categories.size(); ++i)
		{
			final OptionCategory category = categories.get(i);
			options.add(category.options().get(selections[i]));
		}
		
		return options;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		for (final OptionCategory category : categories)
			sb.append(category.toString() + "\n");
		
		return sb.toString();
				
	}
	
	//-------------------------------------------------------------------------
	
}
