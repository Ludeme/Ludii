package test.instructionGeneration;

import static org.junit.Assert.fail;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import app.DesktopApp;
import app.PlayerApp;
import app.loading.GameLoading;
import instructionGeneration.InstructionGeneration;
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
				InstructionGeneration.resetVriables();
				final ThreadRunningGame thread = new ThreadRunningGame(app, gameName);
				thread.run();
				while (!InstructionGeneration.isProcessComplete())
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
					InstructionGeneration.instructionGeneration(app);
				});
			}
			catch (InvocationTargetException | InterruptedException e)
			{
				e.printStackTrace();
				fail();
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
