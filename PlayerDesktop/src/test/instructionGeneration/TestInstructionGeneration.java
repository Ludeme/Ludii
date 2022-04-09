package test.instructionGeneration;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import app.DesktopApp;
import app.loading.GameLoading;
import app.manualGeneration.ManualGeneration;
import main.FileHandling;

/**
 * Generates instruction websites for all games.
 *
 * @author Matthew.Stephenson
 */
public class TestInstructionGeneration
{
	
	//-------------------------------------------------------------------------
	
	public void test()
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

		final String gameToReach = "";

		boolean reached = (gameToReach.equals("") ? true : false);

		for (final String gameName : validChoices)
		{
			if (reached)
			{
				final ThreadRunningGame thread = new ThreadRunningGame(app, gameName);
				thread.run();
				while (!ManualGeneration.isProcessComplete())
				{
					try
					{
						Thread.sleep(1000);
					}
					catch (final InterruptedException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			else if (gameName.contains(gameToReach))
			{
				reached = true;
			}
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * The thread generating the instructions for a game.
	 * 
	 * @author Matthew.Stephenson
	 */
	public class ThreadRunningGame extends Thread
	{
		private final String gameName;
		private final DesktopApp app;

		public ThreadRunningGame(final DesktopApp app, final String name)
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
					ManualGeneration.manualGeneration(app);
				});
			}
			catch (InvocationTargetException | InterruptedException e)
			{
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	public static void main(final String[] args)
	{
		final TestInstructionGeneration temp = new TestInstructionGeneration();
		temp.test();
	}
	
	//-------------------------------------------------------------------------
	
}
