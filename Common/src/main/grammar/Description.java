package main.grammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.Constants;
import main.StringRoutines;
import main.options.GameOptions;
import main.options.Ruleset;

//-----------------------------------------------------------------------------

/**
 * Game description with full details after expansion.
 * 
 * @author cambolbro
 */
public class Description
{
	// Raw, unprocessed description, with defines and options, as manually authored by user
	private String raw = null;   
	
//	// Description with options expanded 
//  	private String optionsExpanded = null;  
 
  	// Full description with all defines, options, instances, etc. expanded 
  	private String expanded = null;  
  	
  	// Metadata description after expansion and filtering to current option set
  	private String metadata = null;  
   	 	
  	// GameOptions defined in game description
  	private final GameOptions gameOptions = new GameOptions();

  	// Rulesets defined in game description
  	private final List<Ruleset> rulesets = new ArrayList<Ruleset>();
 
  	// Tree(s) of tokens making up the (expanded) game description
   	private final TokenForest tokenForest = new TokenForest();
  	
  	// Tree of items corresponding to tokens, for parsing
  	private ParseItem parseTree = null;
  	
  	// Tree of classes and objects actually called by compiled item
  	private Call callTree = null;
  	
  	// File path that the description came from.
  	private String filePath = null;

  	// Whether this description was completed as a reconstruction
  	private boolean isReconstruction = false;
  	
  	// Maximum number of reconstructions to generate (if using reconstruction syntax).
  	private int maxReconstructions = 1;
  	
  	// Map of define instances used in the current game expansion.
  	private final Map<String, DefineInstances> defineInstances = new HashMap<String, DefineInstances>();
  	
  	//-------------------------------------------------------------------------

  	public Description(final String raw)
  	{
  		this.raw = new String(raw);
  	}
  	
  	//-------------------------------------------------------------------------
  	
  	public String raw()
	{
		return raw;
	}

	public void setRaw(final String str)
	{
		raw = new String(str);
	}

	public String expanded()
	{
		return expanded;
	}

	public void setExpanded(final String str)
	{
		expanded = new String(str);
	}

	public String metadata()
	{
		return metadata;
	}

	public void setMetadata(final String str)
	{
		metadata = new String(str);
	}

	public GameOptions gameOptions()
	{
		return gameOptions;
	}

	public List<Ruleset> rulesets()
	{
		return Collections.unmodifiableList(rulesets);
	}

	public TokenForest tokenForest()
	{
		return tokenForest;
	}
	
	public ParseItem parseTree()
	{
		return parseTree;
	}

	public void setParseTree(final ParseItem tree)
	{
		parseTree = tree;
	}
	
	public Call callTree()
	{
		return callTree;
	}

	public void setCallTree(final Call tree)
	{
		callTree = tree;
	}
	
	public String filePath() 
	{
		return filePath;
	}

	public void setFilePath(final String filePath) 
	{
		this.filePath = filePath;
	}
	
	/**
	 * @return Whether this description was completed as a reconstruction.
	 */
   	public boolean isReconstruction()
  	{
  		return isReconstruction;
  	}
   	
   	public void setIsRecontruction(final boolean value)
   	{
   		isReconstruction = value;
   	}
	
	/**
	 * @return Maximum number of reconstructions to generate (if using reconstruction syntax).
	 */
   	public int maxReconstructions()
  	{
  		return maxReconstructions;
  	}
   	
   	public void setMaxReconstructions(final int num)
   	{
   		maxReconstructions = num;
   	}

   	public final Map<String, DefineInstances> defineInstances()
   	{
   		return defineInstances;
   	}
   	
	//-------------------------------------------------------------------------

	public void clearRulesets()
	{
		rulesets.clear();
	}
	
	public void add(final Ruleset ruleset)
	{
		rulesets.add(ruleset);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Creates parse tree from the first Token tree.
	 */
	public void createParseTree()
	{
		parseTree = createParseTree(tokenForest.tokenTree(), null);
	}

	/**
	 * @return Parse tree created from this token and its parent.
	 */
	private static ParseItem createParseTree(final Token token, final ParseItem parent)
	{
		final ParseItem item = new ParseItem(token, parent);
		
		for (final Token arg : token.arguments())
			item.add(createParseTree(arg, item));	
		
		return item;
	}
	
 	//-------------------------------------------------------------------------
	
	/**
	 * @param selectedOptions List of strings describing selected options
	 * @return Index of ruleset to be selected automatically based on selected
	 * options. Returns Constants.UNDEFINED if there is no match.
	 */
	public int autoSelectRuleset(final List<String> selectedOptions)
	{
		final List<String> allActiveOptions = gameOptions.allOptionStrings(selectedOptions);
		
		for (int i = 0; i < rulesets.size(); ++i)
		{
			if (!rulesets.get(i).optionSettings().isEmpty())	// Eric wants to hide unimplemented rulesets
			{		
				boolean fullMatch = true;
				
				for (final String requiredOpt : rulesets.get(i).optionSettings())
				{
					if (!allActiveOptions.contains(requiredOpt))
					{
						fullMatch = false;
						break;
					}
				}
				
				if (fullMatch)
					return i;
			}
		}
		
		return Constants.UNDEFINED;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return "(game ...)" ludeme from raw description.
	 */
	public String rawGameDescription()
	{
		final int c = raw.indexOf("(game");
		
		// This description does not contain a (game ...) ludeme
		if (c < 0)
			return "";
		
		final int cc = StringRoutines.matchingBracketAt(raw, c);
		
		final String sub = raw.substring(c, cc + 1);
		//System.out.println("Raw game description: " + sub);
		
		return sub;
	}
	
	//-------------------------------------------------------------------------

}
