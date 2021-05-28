package app;

/**
 * Main point of Entry for running the Ludii application.
 * 
 * @author Matthew.Stephenson and Dennis Soemers
 */
public class StartDesktopApp
{
	public static void main(final String[] args)
	{
		// The actual launching
		if (args.length == 0)
		{
			final DesktopApp app = new DesktopApp();
			app.createDesktopApp();
		}
		else
		{
			PlayerCLI.runCommand(args);
		}
	}
}