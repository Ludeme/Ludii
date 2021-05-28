package main.options;

import java.util.ArrayList;
import java.util.List;

import main.Constants;

/**
 * Record of the user's option and ruleset selections.
 * 
 * @author cambolbro and Dennis Soemers
 */
public class UserSelections
{

	/** Record of user's current option selections. */
	private List<String> selectedOptionStrings = new ArrayList<String>();

	/** Record of user's current ruleset selection. */
	private int ruleset = Constants.UNDEFINED;
	
	//-------------------------------------------------------------------------
	
	public UserSelections(final List<String> selectedOptionStrings)
	{
		this.selectedOptionStrings = selectedOptionStrings;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return List of Strings describing option selections
	 */
	public List<String> selectedOptionStrings()
	{
		return selectedOptionStrings;
	}
	
	/**
	 * Sets the array of user option selections
	 * @param optionSelections 
	 */
	public void setSelectOptionStrings(final List<String> optionSelections)
	{
		this.selectedOptionStrings = optionSelections;
	}
	
	public int ruleset()
	{
		return ruleset;
	}
	
	public void setRuleset(final int set)
	{
		ruleset = set;
	}
	
	//-------------------------------------------------------------------------

}
