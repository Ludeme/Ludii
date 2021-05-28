package game.functions.intArray.state;

import java.util.BitSet;
import java.util.List;

import annotations.Or;
import game.Game;
import game.functions.intArray.BaseIntArrayFunction;
import game.types.board.RelationType;
import game.types.state.GameType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.DirectionFacing;
import gnu.trove.list.array.TIntArrayList;
import other.context.Context;

/**
 * Returns the list of rotation indices according to a tiling type.
 * 
 * @author Eric.Piette
 */
public class Rotations extends BaseIntArrayFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Precomputed Direction if this is absolute. */
	private int[] precomputedDirection;

	//-------------------------------------------------------------------------

	/** The rotation direction. */
	final AbsoluteDirection[] directionsOfRotation;

	/**
	 * To return the rotations indices for a piece on a specific site according to
	 * the rotation type.
	 * 
	 * @param directionOfRotation The direction of the possible rotations.
	 * @param directionsOfRotation The directions of the possible rotations.
	 * @example (rotations Orthogonal)
	 */
	public Rotations
	(
		@Or final AbsoluteDirection   directionOfRotation,
		@Or final AbsoluteDirection[] directionsOfRotation
	)
	{
		int numNonNull = 0;
		if (directionOfRotation != null)
			numNonNull++;
		if (directionsOfRotation != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException("Only one Or should be non-null.");

		this.directionsOfRotation = (directionsOfRotation != null) ? directionsOfRotation : new AbsoluteDirection[]
		{ directionOfRotation };
	}

	@Override
	public int[] eval(Context context)
	{
		if (precomputedDirection != null)
			return precomputedDirection;

		final TIntArrayList directions = new TIntArrayList();

		final int numEdges = context.topology().numEdges();
		final int ratio = context.topology().supportedDirections(context.board().defaultSite()).size() / numEdges;

		// Get the rotations.
		for (final AbsoluteDirection absoluteDirection : directionsOfRotation)
		{
			final DirectionFacing direction = AbsoluteDirection.convert(absoluteDirection);

			if (direction != null) // Absolute direction equivalent to a facing direction.
			{
				// We convert that direction to the corresponding rotation index.
				final int rotation = AbsoluteDirection.convert(absoluteDirection).index() / ratio;
				if (!directions.contains(rotation))
					directions.add(rotation);
			}
			else // Absolute direction equivalent to a set of facing directions (e.g. Orthogonal)
			{
				final RelationType relation = AbsoluteDirection.converToRelationType(absoluteDirection);
				if (relation == null)
					continue;

				// We got the directions facing equivalent to that absolute direction.
				final List<DirectionFacing> directionsAbsolute = context.topology().supportedDirections(relation,
						context.board().defaultSite());

				// We convert these directions to the corresponding rotation indices.
				for (final DirectionFacing eqDirection : directionsAbsolute)
				{
					final AbsoluteDirection absDirection = eqDirection.toAbsolute();
					final int rotation = AbsoluteDirection.convert(absDirection).index() / ratio;
					if (!directions.contains(rotation))
						directions.add(rotation);
				}
			}
		}
		
		return directions.toArray();
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		final long gameFlags = GameType.Rotation;
		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		for (final AbsoluteDirection absoluteDirection : directionsOfRotation)
			concepts.or(AbsoluteDirection.concepts(absoluteDirection));
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

	@Override
	public boolean isStatic()
	{
		return true;
	}

	@Override
	public void preprocess(final Game game)
	{
		if (isStatic())
			precomputedDirection = eval(new Context(game, null));
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final String str = "Rotations";
		return str;
	}
}
