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
	private final String gameName;
	private boolean over = false;
	private final PlayerApp app;

	public RunGame(final PlayerApp app, final String name)
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
				System.out.println("TEST GUI FOR " + gameName);
				GameLoading.loadGameFromName(app, gameName, new ArrayList<String>(), false);
				app.manager().ref().context().game().toEnglish(app.manager().ref().context().game());
				//InstructionGeneration.instructionGeneration(app);
				over = true;
			});
		}
		catch (InvocationTargetException | InterruptedException e)
		{
			e.printStackTrace();
			fail();
		}
	}

	public boolean isOver()
	{
		return over;
	}
}
