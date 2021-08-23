package app.util;

import java.awt.Toolkit;

import javax.swing.JDialog;

/**
 * Desktop specific settings
 * 
 * @author Matthew.Stephenson
 */
public class SettingsDesktop
{

	/** Default display width for the program (in pixels). */
	public static int defaultWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() * .75);

	/** Default display height for the program (in pixels). */
	public static int defaultHeight = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight() * .75);
	
	/** Whether a separate dialog (settings, puzzle, etc.) is open. */
	public static JDialog openDialog = null;
	
	/** Only used for tutorial generation purposes. */
	public static String tutorialVisualisationMoveType = "move";
	
}
