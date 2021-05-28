package metadata.graphics.piece.families;

import java.util.BitSet;

import annotations.Hide;
import game.Game;
import metadata.graphics.GraphicsItem;
import other.concept.Concept;

/**
 * Specifies a list of families for the game's pieces.
 * 
 * @author Matthew.Stephenson
 * 
 * @remarks Used for games where the pieces have multiple possible design schemes, e.g. Chess.
 */
@Hide
public class PieceFamilies implements GraphicsItem
{
	/** Array of family names. */
	private final String[] pieceFamilies;
		
	//-------------------------------------------------------------------------

	/**
	 * @param pieceFamilies	Set of family names for the pieces used in the game.
	 */
	public PieceFamilies
	(
		final String[] pieceFamilies
	)
	{
		this.pieceFamilies = pieceFamilies;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return All piece families.
	 */
	public String[] pieceFamilies()
	{
		return pieceFamilies;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		for (final String pieceFamily : pieceFamilies)
			if (pieceFamily.equals("Abstract"))
				concepts.set(Concept.MarkerComponent.id(), true);
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
