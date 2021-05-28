package tournament;

import java.awt.EventQueue;

import manager.Manager;
import other.context.Context;

/**
 * Ludii Tournament util functions
 *
 * @author Dennis Soemers and Matthew Stephenson
 */
public class TournamentUtil
{
	/**
	 * If Tournament is running then need to save the results of this game.
	 */
	public static void saveTournamentResults(final Manager manager, final Context context)
	{
		if (manager.tournament() != null)
		{
			System.out.println("SAVING RESULTS");
			manager.tournament().storeResults(context.game(), context.trial().ranking());

			new java.util.Timer().schedule(
		        new java.util.TimerTask()
		        {
		            @Override
		            public void run()
		            {
		            	EventQueue.invokeLater(() -> 
						{
		            		System.out.println("LOADING NEXT GAME");
		            		manager.tournament().startNextTournamentGame(manager);
		            	});
		            }
		        },
		        5000L
			);
		}
	}
	
	//-------------------------------------------------------------------------
	
}
