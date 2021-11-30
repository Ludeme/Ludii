package killothron;

import static org.junit.Assert.fail;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.json.JSONObject;

import app.PlayerApp;
import app.loading.GameLoading;
import app.utils.GameUtil;
import manager.Manager;
import manager.ai.AIDetails;
import manager.ai.AIUtil;

/**
 * Method used to run a game in the killothron.
 * 
 * @author Eric.Piette
 */
public class RunGame extends Thread
{
	/** The name of the game. */
	private final String gameName;
	
	/** The number of players. */
	private final int numPlayers;
	
	/** The graphical app used to show the game. */
	private final PlayerApp app;

	/**
	 * @param app        The app.
	 * @param name       The game name.
	 * @param numPlayers The number of players.
	 */
	public RunGame
	(
		final PlayerApp app, 
		final String name,
		final int numPlayers
	)
	{
		this.app = app;
		gameName = name;
		this.numPlayers = numPlayers;
	}

	@Override
	public void run()
	{
		final Manager manager = app.manager();
		
		try
		{
			EventQueue.invokeAndWait(() ->
			{
				GameLoading.loadGameFromName(app, gameName, new ArrayList<String>(), false);
				for(int pid = 1; pid <= numPlayers; pid++)
				{
					if (manager.aiSelected()[pid].ai() == null)		
					{
							final JSONObject json = new JSONObject().put("AI", new JSONObject().put("algorithm", "Random"));
							AIUtil.updateSelectedAI(app.manager(), json, pid, "Random");
							
							if (manager.aiSelected()[pid].ai() != null)
								manager.aiSelected()[pid].ai().closeAI();
							
							manager.aiSelected()[pid] = new AIDetails(manager, json, pid, "Random");
							manager.settingsNetwork().backupAiPlayers(manager);
							//pauseAgentsIfNeeded(manager);
							//manager.aiSelected()[pid].ai().initIfNeeded(app.contextSnapshot().getContext(app).game(), pid);
					}
				}
				GameUtil.startGame(app);
				app.manager().settingsManager().setAgentsPaused(app.manager(), false);
				app.manager().ref().nextMove(app.manager(), false);
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
		return app.manager().ref().context().trial().over();
	}
	
}
