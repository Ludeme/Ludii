package app.utils;

/**
 * Exhibition (in-depth) application settings
 * 
 * @author Matthew.Stephenson
 */
public class SettingsExhibition
{
	/** If the app should be loaded in the exhibition display format. */
	public static final boolean exhibitionVersion = true;
	
	/** The resolution of the app (some aspects may be hard-coded to this size). */
	public static final int exhibitionDisplayWidth = 1920;
	public static final int exhibitionDisplayHeight = 1080;
	
	/** The game to load (there exists both an english and swedish version of each game. */
	public static final String exhibitionGamePath = "/lud/wip/exhibition/Baghchal Exhibition Swedish.lud";
	
	/** If Player 2 should be controlled by an AI agent. */
	public static final boolean againstAI = true;
	public static final double thinkingTime = 3.0;
}
