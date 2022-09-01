package app.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import app.PlayerApp;
import manager.Manager;

/**
 * Public class for calling remote dialog functions.
 * Fake function calls. Remote dialog functionality is not available in the source code.
 * 
 * @author Matthew.Stephenson and Dennis Soemers
 */
public class RemoteDialogFunctionsPublic 
{
	
	//-------------------------------------------------------------------------

	/** Class loader used to load private network code if available (not included in public source code repo) */
	private static URLClassLoader privateNetworkCodeClassLoader = null;
	
	// Static block to initialise classloader
	static 
	{
		final File networkPrivateBin = new File("../../LudiiPrivate/NetworkPrivate/bin");
		if (networkPrivateBin.exists())
		{
			try
			{
				privateNetworkCodeClassLoader = new URLClassLoader(new URL[]{networkPrivateBin.toURI().toURL()});
			} 
			catch (final MalformedURLException e)
			{
				// If this fails, that's fine, just means we don't have private code available
			}
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Constructs a wrapper around the remote dialog functions functions.
	 */
	public static RemoteDialogFunctionsPublic construct()
	{
		// See if we can find private code first
		final ClassLoader classLoader = 
				privateNetworkCodeClassLoader != null ? privateNetworkCodeClassLoader : RemoteDialogFunctionsPublic.class.getClassLoader();
		try
		{
			final Class<?> privateClass = Class.forName("app.display.dialogs.remote.util.RemoteDialogFunctionsPrivate", true, classLoader);

			if (privateClass != null)
			{
				// Found private network code, use its zero-args constructor
				return (RemoteDialogFunctionsPublic) privateClass.getConstructor().newInstance();
			}
		}
		catch (final Exception e)
		{
			// Nothing to do
		}
		
		// Failed to load the private class, so we're probably working just with public source code
		return new RemoteDialogFunctionsPublic();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Show the remote dialog.
	 */
	@SuppressWarnings("static-method")
	public void showRemoteDialog(final PlayerApp app) 
	{
		app.addTextToStatusPanel("Sorry. Remote play functionality is not available from the source code.\n");
	}
	
	/**
	 * Refresh the remote dialog display.
	 */
	public void refreshNetworkDialog() 
	{
		// Do nothing.
	}
	
	/**
	 * Leave current online game and update all required GUI elements.
	 */
	public void leaveGameUpdateGui(final Manager manager) 
	{
		// Do nothing.
	}
	
	//-------------------------------------------------------------------------
	
}
