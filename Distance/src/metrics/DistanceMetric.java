package metrics;

import java.util.ArrayList;
import java.util.List;

import common.LudRul;
import common.Score;
import game.Game;
import utils.data_structures.support.DistanceProgressListener;

/**
 * Interface for distance metric classes.
 * 
 * @author cambolbro
 */
public interface DistanceMetric
{
	/**
	 * @return Estimated distance between two games.
	 */
	public Score distance(final Game gameA, final Game gameB);

	/**
	 * Compares the distance and uses the lightweight representation of a game.
	 * 
	 * @return Estimated distance between two games.
	 */
	public Score distance(LudRul gameA, LudRul gameB);

	/**
	 * Some metrices only work within a static set of games.
	 * Those metrices need to return true. 
	 * 
	 * @return if this metric type needs to be initialized.
	 */
	public default boolean typeNeedsToBeInitialized() {
		return false;
	}
	
	/**
	 * Many distance metrices can have multiple settings.
	 * This returns a suggestion. (if the metric does not 
	 * have multiple settings it just returns the instance).    
	 * 
	 * @return a default instance of this distance metric
	 */
	public DistanceMetric getDefaultInstance();
	
	
	
	/**
	 * Many distance metrices can have multiple settings.
	 * This returns if a user selection window is implemented,
	 * such the according button in the Distance Dialog 
	 * can be grayed out or not.
	 * 
	 * @return a default instance of this distance metric
	 */
	public default boolean hasUserSelectionDialog() {
		return false;
	}
	
	/**
	 * Many distance metrices can have multiple settings.
	 * This returns a suggestion. (if the metric does not 
	 * have multiple settings it just returns the instance).    
	 * 
	 * @return a userSelected instance of this distance metric
	 */
	public default DistanceMetric showUserSelectionDialog() {
		return this;//default placeholder.
	}
	
	/**
	 * Some metrices only work within a static set of games.
	 * Those need to return true. 
	 * @param candidates 
	 * 
	 * @return if this metric instance is initialized.
	 */
	public default boolean isInitialized(ArrayList<LudRul> candidates) {
		return true;
	}
	
	/**
	 * Releases resources if used, like frequency maps, etc.
	 */
	public default void releaseResources()
	{
		//empty, so its optional for a class to implement it
	}
	
	/**
	 * @return Estimated minimum distance gameA and the games listed in gameB.
	 */
	public Score distance
	(
		final Game gameA, final List<Game> gameB, final int numberTrials, 
		final int maxTurns, final double thinkTime, final String AIName
	);

	/**
	 * Usually used to have an identifier for stored metrics
	 * @return the name of this metric. 
	 */
	public String getName();

	/**
	 * Distance between two expanded Descriptions
	 * @param expandedDescription1
	 * @param expandedDescription2
	 * @return score.
	 */
	public Score distance(String expandedDescription1, String expandedDescription2);

	public default String getToolTipText() {
		return getName();
		
	}

	public default void init(final ArrayList<LudRul> candidates,boolean forceRecalculation, final DistanceProgressListener dpl) {
		//needs to be overwritten
	}
}
