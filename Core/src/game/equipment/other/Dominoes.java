package game.equipment.other;

import java.util.ArrayList;
import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.equipment.Item;
import game.equipment.component.tile.Domino;
import game.types.play.RoleType;
import main.Constants;
import other.ItemType;
import other.concept.Concept;

/**
 * Defines a dominoes set.
 * 
 * @author Eric.Piette
 *
 */
public class Dominoes extends Item
{
	/** The number of dominoes. */
	final int upTo;

	/**
	 * Definition of a set of dominoes.
	 * 
	 * @param upTo The number of dominoes [6].
	 * @example (dominoes)
	 */
	public Dominoes
	(
		@Opt @Name final Integer upTo
	)
	{
		super(null, Constants.UNDEFINED, RoleType.Shared);
		this.upTo = (upTo == null) ? 6 : upTo.intValue();

		// Limit on the max dots on the dominoes.
		if (this.upTo < 0 || this.upTo > Constants.MAX_PITS_DOMINOES)
			throw new IllegalArgumentException(
					"The limit of the dominoes pips can not be negative or to exceed " + Constants.MAX_PITS_DOMINOES
							+ ".");

		setType(ItemType.Dominoes);
	}

	/***
	 * @return A list of all the dominoes of this set.
	 */
	public ArrayList<Domino> generateDominoes()
	{
		final ArrayList<Domino> dominoes = new ArrayList<Domino>();

		for (int i = 0; i <= upTo; i++)
			for (int j = i; j <= upTo; j++)
			{
				final Domino domino = new Domino(
						"Domino" + i + j, RoleType.Shared,
						Integer.valueOf(i),
						Integer.valueOf(j), null);
				dominoes.add(domino);
			}

		return dominoes;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.Domino.id(), true);
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		return readEvalContext;
	}
}
