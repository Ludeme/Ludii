package game.util.directions;

import other.Ludeme;

/**
 * Provides a general ``direction'' description for use in a variety of
 * contexts.
 * 
 * @author mrraow and Eric.Piette
 * 
 * @usage Provides an Interface as each basis can have its own set of
 *        directions.
 */
public interface DirectionFacing extends Ludeme
{
	/**
	 * @return Direction left of this one.
	 */
	public DirectionFacing left();

	/**
	 * @return Direction right of this one.
	 */
	public DirectionFacing right();

	/**
	 * @return Direction rightward of this one.
	 */
	public DirectionFacing rightward();

	/**
	 * @return Direction leftward of this one.
	 */
	public DirectionFacing leftward();

	/**
	 * @return The opposite direction.
	 */
	public DirectionFacing opposite();

	/**
	 * @return Index of this direction.
	 */
	public int index();

	/**
	 * @return Index of this direction.
	 */
	public DirectionUniqueName uniqueName();
	
	/**
	 * @return Number of possible distinct values in Direction enum.
	 */
	public int numDirectionValues();
	
	/**
	 * @return Direction converted to an AbsoluteDirection
	 */
	public AbsoluteDirection toAbsolute();
	
	//-------------------------------------------------------------------------
	
	/**
	 * An efficient implementation of Maps with Direction objects as key. 
	 * Very similar to EnumMap, but works with keys of the Direction type 
	 * (which is an interface that is expected to always be implemented 
	 * by an enum, but is not yet an enum itself).
	 * 
	 * This map should only ever contain keys of a single enum implementing
	 * Direction; keys from different enums should not be mixed!
	 * 
	 * @author Dennis Soemers
	 *
	 * @param <V>
	 */
	public static class DirectionMap<V>
	{

		/** Stored values (one per possible key) */
		protected final Object[] values;

		/**
		 * For every possible key, true if and only if we actually stored something
		 * there
		 */
		protected final boolean[] occupied;

		/** Number of stored objects */
		protected int size = 0;

		/**
		 * Constructor
		 * 
		 * @param exampleKey
		 */
		public DirectionMap(final DirectionFacing exampleKey)
		{
			values = new Object[exampleKey.numDirectionValues()];
			occupied = new boolean[exampleKey.numDirectionValues()];
		}

		/**
		 * @param key
		 * @return Value stored for given key, or null if none stored
		 */
		@SuppressWarnings("unchecked")
		public V get(final DirectionFacing key)
		{
			return (V) values[key.index()];
		}

		/**
		 * Puts given value for given key in the map
		 * 
		 * @param key
		 * @param value
		 * @return Previously stored value (null if no value previously stored)
		 */
		public V put(final DirectionFacing key, final V value)
		{
			final int idx = key.index();
			@SuppressWarnings("unchecked")
			final V old = (V) values[idx];
			values[idx] = value;

			if (!occupied[idx])
			{
				++size;
				occupied[idx] = true;
			}

			return old;
		}

	}

}
