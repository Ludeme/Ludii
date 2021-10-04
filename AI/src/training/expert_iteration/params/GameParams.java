package training.expert_iteration.params;

import java.util.List;

/**
 * Wrapper around params for game setup/configuration in training runs.
 *
 * @author Dennis Soemers
 */
public class GameParams
{
	
	//-------------------------------------------------------------------------
	
	/** Name of the game to play. Should end with .lud */
	public String gameName;
	
	/** List of game options to use when compiling game */
	public List<String> gameOptions;
	
	/** Name of ruleset to compile. Any options will be ignored if ruleset is provided. */
	public String ruleset;
	
	/** Maximum game duration (in moves) */
	public int gameLengthCap;
	
	//-------------------------------------------------------------------------

}
