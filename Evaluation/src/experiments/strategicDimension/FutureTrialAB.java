package experiments.strategicDimension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import game.Game;
import main.Status;
import other.AI;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;
import search.minimax.AlphaBetaSearch;

//-----------------------------------------------------------------------------

/**
 * Thread for running AB version of SD trial. 
 * @author cambolbro
 */
public class FutureTrialAB implements FutureTrial
{       
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    
    /**
     * @param game    The single game object, shared across threads. 
     * @param trialId The index of this trial within its epoch.
     * @param lower   Lower iteration count of this epoch for inferior agent.
     * @param upper   Upper iteration count of this epoch for superior agent.
     * @return Result of trial relative to superior agent (0=loss, 0.5=draw, 1=win).  
     */
    @Override
    public Future<Double> runTrial
    (
    	final Game game, final int trialId, final int lower, final int upper
    ) 
    {
        return executor.submit(() -> 
        {
        	//System.out.println("Submitted id=" + id + ", lower=" + lower + ", upper=" + upper + ".");
        	
        	final int numPlayers = game.players().count();
        	
        	final int pidHigher = 1 + trialId % numPlayers;  // alternate between players
        	
        	final Trial trial = new Trial(game);
			final Context context = new Context(game, trial);
				
			game.start(context);
			
			// Set up AIs
			final List<AI> agents = new ArrayList<AI>();
			agents.add(null);  // null player 0
			for (int pid = 1; pid < numPlayers + 1; pid++)
			{
				final AI ai = new AlphaBetaSearch();  
				ai.initAI(game, pid);
				agents.add(ai);
			}

			while (!context.trial().over())
			{
				final int mover = context.state().mover();
				
				if (mover < 1 || mover > numPlayers)
					System.out.println("** Bad mover index: " + mover);
				
				final AI agent = agents.get(mover);
				
				final Move move = agent.selectAction
				(
					game, 
					new Context(context),
					-1,
					-1,
					(mover == pidHigher ? upper : lower)
				);		
				game.apply(context, move);
					
				//if (trial.numberOfTurns() % 10 == 0)
				//	System.out.print(".");
			}
			//System.out.println(trialId + ": " + context.trial().status() + " (P" + pidHigher + " is superior).");
			
			final Status status = context.trial().status();
			System.out.print(status.winner());	
			
			if (status.winner() == 0)
				return Double.valueOf(0.5);  // is a draw
			if (status.winner() == pidHigher)
				return Double.valueOf(1);  // superior player wins
			return Double.valueOf(0);  // superior player does not win
		});
    }
}
