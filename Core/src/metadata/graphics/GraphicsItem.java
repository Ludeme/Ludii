package metadata.graphics;

import java.util.BitSet;

import game.Game;
import metadata.MetadataItem;

/**
 * Metadata containing graphics hints.
 * @author cambolbro 
 */
public interface GraphicsItem extends MetadataItem
{
	/**
	 * @param game The game.
	 * @return Accumulated concepts.
	 */
	public BitSet concepts(final Game game);

	/**
	 * @param game The game.
	 * @return Accumulated game flags.
	 */
	public long gameFlags(final Game game);

	/**
	 * @return True if this ludeme needs to be redrawn after each move.
	 */
	public boolean needRedraw();
}
