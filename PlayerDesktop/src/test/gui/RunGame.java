package test.gui;

import static org.junit.Assert.fail;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import app.PlayerApp;
import app.loading.GameLoading;

/**
 * Method used to run a game in the killothron.
 * 
 * @author Eric.Piette
 */
public class RunGame extends Thread
{
	/** The name of the game. */
	private final String gameName;
	
	/** Value used to determine when the game is over. */
	private boolean over = false;
	
	/** The graphical app used to show the game. */
	private final PlayerApp app;

	/**
	 * @param app The app.
	 * @param name The game name.
	 */
	public RunGame
	(
		final PlayerApp app, 
		final String name
		)
	{
		this.app = app;
		gameName = name;
	}

	@Override
	public void run()
	{
		try
		{
			EventQueue.invokeAndWait(() ->
			{
				GameLoading.loadGameFromName(app, gameName, new ArrayList<String>(), false);
				over = true;
			});
		}
		catch (InvocationTargetException | InterruptedException e)
		{
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * @return True if the game is over.
	 */
	public boolean isOver()
	{
		return over;
	}
	
}
