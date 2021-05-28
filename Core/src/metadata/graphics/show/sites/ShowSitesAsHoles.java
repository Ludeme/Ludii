package metadata.graphics.show.sites;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import metadata.graphics.GraphicsItem;
import metadata.graphics.util.HoleType;

/**
 * Indicates whether the sites of the board should be represented as holes.
 * 
 * @author Matthew.Stephenson
 * 
 * @remarks Only used by a specific number of board styles when creating the board's design (e.g. Mancala).
 */
@Hide
public class ShowSitesAsHoles implements GraphicsItem
{
	/** Hole Type. */
	private final HoleType type;

	/** The sites to modify the shape. */
	private final int[] indices;
	
	//-------------------------------------------------------------------------

	/**
	 * @param indices The indices of the special holes.
	 * @param type    The shape of the holes.
	 */
	public ShowSitesAsHoles
	(
		final Integer[] indices,
		final HoleType type
	)
	{
		this.type = type;

		this.indices = new int[indices.length];
		for (int i = 0; i < indices.length; i++)
			this.indices[i] = indices[i].intValue();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The shape of the holes.
	 */
	public HoleType type()
	{
		return type;
	}

	/**
	 * @return The indices of the holes.
	 */
	public int[] indices()
	{
		return indices;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		return concepts;
	}

	@Override
	public long gameFlags(final Game game)
	{
		final long gameFlags = 0l;
		return gameFlags;
	}

	@Override
	public boolean needRedraw()
	{
		return false;
	}

}
