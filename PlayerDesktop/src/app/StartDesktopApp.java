package app;

/**
 * Main point of Entry for running the Ludii application.
 * 
 * @author Matthew.Stephenson and Dennis Soemers
 */
public class StartDesktopApp
{
	private final static DesktopApp desktopApp = new DesktopApp();
	
	public static void main(final String[] args)
	{
		// The actual launching
		if (args.length == 0)
		{
			desktopApp.createDesktopApp();
		}
		else
		{
			PlayerCLI.runCommand(args);
		}
	}

	// Used in case any agents need DesktopApp functions.
	public static DesktopApp desktopApp()
	{
		return desktopApp;
	}
}