package game.functions.graph;

import java.util.BitSet;

import game.Game;
import game.types.board.SiteType;
import game.types.state.GameType;
import game.util.graph.Graph;
import other.context.Context;

/**
 * Returns a graph defined by lists of vertices, edges and faces.
 * 
 * @author cambolbro
 */

// **
// ** Do not @Hide, or loses mapping in grammar!
// **

public interface GraphFunction extends GameType
{
	/**
	 * @param context  The context.
	 * @param siteType The graph element type.
	 * @return The result of applying this function to this trial.
	 */
	public Graph eval(final Context context, final SiteType siteType);

	/**	 
	 * @return Original board dimension settings of graph.
	 */
	public int[] dim();

	/**
	 * @param game The game.
	 * @return Accumulated flags corresponding to the game concepts.
	 */
	public BitSet concepts(final Game game);

	/**
	 * @return Accumulated flags corresponding to read data in EvalContext.
	 */
	public BitSet readsEvalContextRecursive();

	/**
	 * @return Accumulated flags corresponding to write data in EvalContext.
	 */
	public BitSet writesEvalContextRecursive();
}
