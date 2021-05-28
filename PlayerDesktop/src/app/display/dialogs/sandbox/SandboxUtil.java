package app.display.dialogs.sandbox;

import game.Game;

/**
 * Various util functions to do with the Sandbox mode.
 * 
 * @author Matthew.Stephenson
 */
public class SandboxUtil
{
	//-------------------------------------------------------------------------

	/**
	 * Returns an error message if sandbox is not allowed for the specifed game.
	 */
	public static String isSandboxAllowed(final Game game)
	{
		String errorMessage = "";
//		if (game.usesUnionFindAdjacent() || game.usesUnionFindOrthogonal())
//		{
//			errorMessage = "Sandbox is not supported in games that use Union Find.";
//		}
		if (game.hasLargePiece())
		{
			errorMessage = "Sandbox is not supported in games that have large pieces.";
		}
		else if (game.hasHandDice())
		{
			errorMessage = "Sandbox is not supported in games that have dice.";
		}
		return errorMessage;
	}
	
}
