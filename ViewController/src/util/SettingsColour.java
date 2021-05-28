package util;

import java.awt.Color;
import java.util.Arrays;

import main.Constants;
import other.context.Context;

/**
 * Colour settings.
 * 
 * @author matthewStephenson and cambolbro
 */
public class SettingsColour 
{
	
	/** 
	 * Original player colours. Used as a reference when we want to reset the player colours to default.
	 */
	public final static Color[] ORIGINAL_PLAYER_COLOURS =
		{ new Color(250, 250, 250), 
				new Color(250, 250, 250), new Color(50, 50, 50), new Color(190, 0, 0), new Color(0, 190, 0), 
				new Color(0, 0, 190), new Color(190, 0, 190), new Color(0, 190, 190), new Color(255, 153, 0),
				new Color(255, 255, 153), new Color(153, 204, 255), new Color(255, 153, 204), new Color(204, 153, 255), 
				new Color(255, 204, 153), new Color(153, 204, 0), new Color(255, 204, 0), new Color(255, 102, 0),
				new Color(250, 250, 250)};
	
	/**
	 * Current player colours.
	 */
	private final Color[] playerColours =
		{ new Color(250, 250, 250), 
				new Color(250, 250, 250), new Color(50, 50, 50), new Color(190, 0, 0), new Color(0, 190, 0), 
				new Color(0, 0, 190), new Color(190, 0, 190), new Color(0, 190, 190), new Color(255, 153, 0),
				new Color(255, 255, 153), new Color(153, 204, 255), new Color(255, 153, 204), new Color(204, 153, 255), 
				new Color(255, 204, 153), new Color(153, 204, 0), new Color(255, 204, 0), new Color(255, 102, 0),
				new Color(250, 250, 250)};
	
	/**
	 * array of possible board colours (single tile colour) 0 = inner edge, 1 = outer edge, 
	 * 2 = first phase, 3 = second phase, 4 = third phase, 5 = fourth phase, 6 = third phase, 
	 * 7 = fourth phase, 8 = symbols, 9 = vertices, 10 = outer vertices
	 */
	private final Color[] boardColours = { null, null, null, null, null, null, null, null, null, null, null };

	//-------------------------------------------------------------------------
	
	/** 
	 * Use this function to get the player colour, has check for shared player. 
	 */
	public final Color playerColour(final Context context, final int playerId)
	{
		if (playerId > context.game().players().count())
			return playerColours[Constants.MAX_PLAYERS+1];
		return playerColours[playerId];
	}
	
	//-------------------------------------------------------------------------
	
	public void setPlayerColour(final int playerId, final Color colour)
	{
		playerColours[playerId] = colour;
	}
	
	public Color[] getBoardColours() 
	{
		return boardColours;
	}

	public void resetColours() 
	{
		for (int i = 0; i < ORIGINAL_PLAYER_COLOURS.length; i++)
			playerColours[i] = ORIGINAL_PLAYER_COLOURS[i];
		Arrays.fill(boardColours, null);
	}

	//-------------------------------------------------------------------------
	
}
