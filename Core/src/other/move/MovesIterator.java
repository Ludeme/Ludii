package other.move;

import java.util.Iterator;
import java.util.function.BiPredicate;

import other.context.Context;

/**
 * Abstract class for iterators over moves. Includes the additional abstract "canMoveConditionally"
 * method for optimisation purposes, on top of the standard Iterator interface.
 *
 * @author Dennis Soemers
 */
public abstract class MovesIterator implements Iterator<Move>
{
	
	/**
	 * NOTE: this is used because it allows us to return true as soon as we find one move
	 * that satisfies the given condition, and don't need to also generate the next legal
	 * move after that as we typically would in a normal iterator-based implementation.
	 * 
	 * @param predicate
	 * @return True if this moves iterator contains at least one move that
	 * 	satisfies the given condition for a given context.
	 */
	public abstract boolean canMoveConditionally(final BiPredicate<Context, Move> predicate);

}
