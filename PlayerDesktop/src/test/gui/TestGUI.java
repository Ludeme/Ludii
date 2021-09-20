package test.gui;

import static org.junit.Assert.fail;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.Test;

import app.DesktopApp;
import app.PlayerApp;
import app.loading.GameLoading;
import main.FileHandling;

/**
 * Unit Test to run all the games and the gui on each of them.
 *
 * @author Eric.Piette
 */
public class TestGUI
{
	@Test
	public void test() throws InterruptedException
	{
		System.out.println(
				"\n=========================================\nTest: Compile all .lud from memory and load the GUI:\n");

		final DesktopApp app = new DesktopApp();
		app.createDesktopApp();
		final String[] choices = FileHandling.listGames();
		final ArrayList<String> validChoices = new ArrayList<>();

		for (final String s : choices)
		{
			if (!s.contains("/bad/") && !s.contains("/bad_playout/") && !s.contains("/test/") && !s.contains("/wip/")
					&& !s.contains("/wishlist/"))
			{
				validChoices.add(s);
			}
		}
		
		Collections.shuffle(validChoices);

		final String gameToReach = "";

		boolean reached = (gameToReach.equals("") ? true : false);

		for (final String gameName : validChoices)
		{
			if (reached)
			{
				final ThreadRunningGame thread = new ThreadRunningGame(app, gameName);
				thread.run();
				while (!thread.isOver())
					Thread.sleep(100);
			}
			else if (gameName.contains(gameToReach))
			{
				reached = true;
			}
		}
	}

	/**
	 * The thread running the game with the GUI.
	 * 
	 * @author Eric.Piette
	 */
	public class ThreadRunningGame extends Thread
	{
		private final String gameName;
		private boolean over = false;
		private final PlayerApp app;

		public ThreadRunningGame(final PlayerApp app, final String name)
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
}
