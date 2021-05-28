package experiments.strategicDimension;

import java.util.concurrent.Future;

import game.Game;

/**
 * Future trial for concurrent SD trials.
 * @author cambolbro
 */
public interface FutureTrial
{
    /**
     * @param game    The single game object, shared across threads. 
     * @param trialId The index of this trial within its epoch.
     * @param lower   Lower iteration count of this epoch for inferior agent.
     * @param upper   Upper iteration count of this epoch for superior agent.
     * @return Result of trial relative to superior agent (0=loss, 0.5=draw, 1=win).  
     */
    public Future<Double> runTrial
    (
    	final Game game, final int trialId, final int lower, final int upper
    );
}
