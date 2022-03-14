package search.flat.HeuristicSampleAdaptedUtils;

import game.Game;
import other.context.Context;
import search.flat.HeuristicSampleAdapted;
import search.flat.HeuristicSampleAdapted.MoveHeuristicEvaluation;

/**
 * used to allow classes of modul "distance" to access
 * @author Markus
 *
 */
public interface HeuristicProportionViewInterface {

	void addObserver(HeuristicSampleAdapted heuristicSampleAdapted);

	void update(MoveHeuristicEvaluation latestMoveHeuristicEvaluation, Game game, Context context);

	

}
