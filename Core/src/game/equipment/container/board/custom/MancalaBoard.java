package game.equipment.container.board.custom;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.container.board.Board;
import game.equipment.container.board.Track;
import game.functions.dim.DimConstant;
import game.functions.floats.FloatConstant;
import game.functions.graph.BaseGraphFunction;
import game.functions.graph.GraphFunction;
import game.functions.graph.generators.basis.square.Square;
import game.functions.graph.generators.shape.Rectangle;
import game.functions.graph.operators.Shift;
import game.functions.graph.operators.Union;
import game.types.board.SiteType;
import game.types.board.StoreType;
import game.util.graph.Graph;
import other.context.Context;

/**
 * Defines a Mancala-style board.
 *
 * @author Eric.Piette
 */
public class MancalaBoard extends Board
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Number of rows. */
	private final int numRows;

	/** Number of columns. */
	private final int numColumns;

	/** Type of the store. */
	private final StoreType storeType;

	/** Number of stores. */
	private final int numStore;

	//-------------------------------------------------------------------------

	/**
	 * 
	 * @param rows        The number of rows.
	 * @param columns     The number of columns.
	 * @param store       The type of the store.
	 * @param numStores   The number of store.
	 * @param track       The track on the board.
	 * @param tracks      The tracks on the board.
	 * @param largeStack  The game can involves stack(s) higher than 32.
	 * 
	 * @example (mancalaBoard 2 6)
	 */
	public MancalaBoard
	(
		              final Integer   rows,
		              final Integer   columns,
		@Opt    @Name final StoreType store,
		@Opt    @Name final Integer   numStores,
		@Opt    @Name final Boolean   largeStack,
		@Opt @Or      final Track     track,
		@Opt @Or      final Track[]   tracks
	)
	{
		super(new BaseGraphFunction()
		{
			private static final long serialVersionUID = 1L;

			// -------------------------------------------------------------------------

			@Override
			public Graph eval(final Context context, final SiteType siteType)
			{
				final int numRows   = rows.intValue();
				final int numColumns = columns.intValue();
				final StoreType storeType = (store == null) ? StoreType.Outer : store;
				final int numberStore = (numStores == null) ? 2 : numStores.intValue();
				
				if (storeType.equals(StoreType.Inner) || numberStore != 2 || numRows < 2 || numRows > 6)
					return Square.construct(null, new DimConstant(rows.intValue()), null, null).eval(context, siteType);

				if (numRows == 2)
					return makeMancalaTwoRows(storeType, numColumns).eval(context, siteType);
				else if (numRows == 3)
					return makeMancalaThreeRows(storeType, numColumns).eval(context, siteType);
				else if (numRows == 4)
					return makeMancalaFourRows(storeType, numColumns).eval(context, siteType);
				else if (numRows == 5)
					return makeMancalaFiveRows(storeType, numColumns).eval(context, siteType);
				else if (numRows == 6)
					return makeMancalaSixRows(storeType, numColumns).eval(context, siteType);

				// If wrong parameter, no need of a graph.
				return new Graph(new Float[0][0], new Integer[0][0]);
			}

			@Override
			public long gameFlags(Game game)
			{
				return 0;
			}

			@Override
			public void preprocess(Game game)
			{
				// Nothing to do.
			}

			/**
			 * @return A GraphFunction for a Mancala board with two rows.
			 */
			public GraphFunction makeMancalaTwoRows(final StoreType storeType, final int numColumns)
			{
				final GraphFunction bottomRow = 
						Rectangle.construct
						(
							new DimConstant(1), new DimConstant(numColumns), null
						);

				final GraphFunction topRow = 
						new Shift
						(
							new FloatConstant(0), new FloatConstant(1), null,
							Rectangle.construct(new DimConstant(1), 
							new DimConstant(numColumns), null)
						);

				if (!storeType.equals(StoreType.None))
				{
					final GraphFunction leftStore = 
							new Graph
							(
								new Float[][] {{ Float.valueOf(-0.85f), Float.valueOf(0.5f) }}, 
								null
							);

					final GraphFunction rightStore = 
							new Shift
							(
								new FloatConstant(-0.15f), new FloatConstant(0f), null, 
								new Graph
								(
									new Float[][] {{ Float.valueOf(numColumns), Float.valueOf(0.5f) }}, 
									null
								)
							);

					return new Union
					(
						new GraphFunction[] { leftStore, bottomRow, topRow, rightStore }, 
						Boolean.TRUE
					);
				}

				return  new Union
						(
							new GraphFunction[] { bottomRow, topRow }, 
							Boolean.TRUE
						);
			}

			/**
			 * @return A GraphFunction for a Mancala board with three rows.
			 */
			public GraphFunction makeMancalaThreeRows(final StoreType storeType, final int numColumns)
			{
				final GraphFunction bottomRow = Rectangle.construct(new DimConstant(1), new DimConstant(numColumns),
						null);

				final GraphFunction middleRow = new Shift(new FloatConstant(0), new FloatConstant(1), null,
						Rectangle.construct(new DimConstant(1), new DimConstant(numColumns), null));

				final GraphFunction topRow = new Shift(new FloatConstant(0), new FloatConstant(2), null,
						Rectangle.construct(new DimConstant(1), new DimConstant(numColumns), null));

				if (!storeType.equals(StoreType.None))
				{
					final GraphFunction leftStore = new Graph(new Float[][]
					{
							{ Float.valueOf(-1), Float.valueOf(1) } }, null);

					final GraphFunction rightStore = new Shift(new FloatConstant(0), new FloatConstant(0), null,
							new Graph(new Float[][]
					{
							{ Float.valueOf(numColumns), Float.valueOf(1) } 
					}, null));

					return new Union(new GraphFunction[]
					{ leftStore, bottomRow, middleRow, topRow, rightStore }, Boolean.TRUE);
				}

				return new Union(new GraphFunction[]
				{ bottomRow, middleRow, topRow }, Boolean.TRUE);
			}

			/**
			 * @return A GraphFunction for a Mancala board with four rows.
			 */
			public GraphFunction makeMancalaFourRows(final StoreType storeType, final int numColumns)
			{
				final GraphFunction bottomOuterRow = Rectangle.construct(new DimConstant(1),
						new DimConstant(numColumns), null);

				final GraphFunction bottomInnerRow = new Shift(new FloatConstant(0), new FloatConstant(1), null,
						Rectangle.construct(new DimConstant(1), new DimConstant(numColumns), null));

				final GraphFunction topInnerRow = new Shift(new FloatConstant(0), new FloatConstant(2), null,
						Rectangle.construct(new DimConstant(1), new DimConstant(numColumns), null));

				final GraphFunction topOuterRow = new Shift(new FloatConstant(0), new FloatConstant(3), null,
						Rectangle.construct(new DimConstant(1), new DimConstant(numColumns), null));

				if (!storeType.equals(StoreType.None))
				{
					final GraphFunction leftStore = new Graph(new Float[][]
					{
							{ Float.valueOf((float) -0.9), Float.valueOf((float) 1.5) } }, null);

					final GraphFunction rightStore = new Shift(new FloatConstant((float) -0.1), new FloatConstant(0),
							null,
							new Graph(new Float[][]
					{
							{ Float.valueOf(numColumns), Float.valueOf((float) 1.5) } }, null));

					return new Union(new GraphFunction[]
					{ leftStore, bottomOuterRow, bottomInnerRow, topInnerRow, topOuterRow, rightStore }, Boolean.TRUE);
				}

				return new Union(new GraphFunction[]
				{ bottomOuterRow, bottomInnerRow, topInnerRow, topOuterRow }, Boolean.TRUE);
			}

			/**
			 * @return A GraphFunction for a Mancala board with five rows.
			 */
			public GraphFunction makeMancalaFiveRows(final StoreType storeType, final int numColumns)
			{
				final GraphFunction bottomOuterRow = Rectangle.construct(new DimConstant(1),
						new DimConstant(numColumns), null);

				final GraphFunction bottomInnerRow = new Shift(new FloatConstant(0), new FloatConstant(1), null,
						Rectangle.construct(new DimConstant(1), new DimConstant(numColumns), null));

				final GraphFunction middleRow = new Shift(new FloatConstant(0), new FloatConstant(2), null,
						Rectangle.construct(new DimConstant(1), new DimConstant(numColumns), null));

				final GraphFunction topInnerRow = new Shift(new FloatConstant(0), new FloatConstant(3), null,
						Rectangle.construct(new DimConstant(1), new DimConstant(numColumns), null));

				final GraphFunction topOuterRow = new Shift(new FloatConstant(0), new FloatConstant(4), null,
						Rectangle.construct(new DimConstant(1), new DimConstant(numColumns), null));

				if (!storeType.equals(StoreType.None))
				{
					final GraphFunction leftStore = new Graph(new Float[][]
					{
							{ Float.valueOf((float) -1.0), Float.valueOf((float) 2.0) } }, null);

					final GraphFunction rightStore = new Shift(new FloatConstant((float) -0.1), new FloatConstant(0),
							null, new Graph(new Float[][]
					{
							{ Float.valueOf((float) (numColumns + 0.1)), Float.valueOf((float) 2.0) } }, null));

					return new Union(new GraphFunction[]
					{ leftStore, bottomOuterRow, bottomInnerRow, middleRow, topInnerRow, topOuterRow, rightStore },
							Boolean.TRUE);
				}

				return new Union(new GraphFunction[]
				{ bottomOuterRow, bottomInnerRow, middleRow, topInnerRow, topOuterRow }, Boolean.TRUE);
			}

			/**
			 * @return A GraphFunction for a Mancala board with six rows.
			 */
			public GraphFunction makeMancalaSixRows(final StoreType storeType, final int numColumns)
			{
				final GraphFunction bottomOuterRow = Rectangle.construct(new DimConstant(1),
						new DimConstant(numColumns), null);

				final GraphFunction bottomInnerRow = new Shift(new FloatConstant(0), new FloatConstant(1), null,
						Rectangle.construct(new DimConstant(1), new DimConstant(numColumns), null));

				final GraphFunction bottomInnerInnerRow = new Shift(new FloatConstant(0), new FloatConstant(2), null,
						Rectangle.construct(new DimConstant(1), new DimConstant(numColumns), null));

				final GraphFunction topInnerInnerRow = new Shift(new FloatConstant(0), new FloatConstant(3), null,
						Rectangle.construct(new DimConstant(1), new DimConstant(numColumns), null));

				final GraphFunction topInnerRow = new Shift(new FloatConstant(0), new FloatConstant(4), null,
						Rectangle.construct(new DimConstant(1), new DimConstant(numColumns), null));

				final GraphFunction topOuterRow = new Shift(new FloatConstant(0), new FloatConstant(5), null,
						Rectangle.construct(new DimConstant(1), new DimConstant(numColumns), null));

				if (!storeType.equals(StoreType.None))
				{
					final GraphFunction leftStore = new Graph(new Float[][]
					{
							{ Float.valueOf((float) -0.9), Float.valueOf((float) 2.5) } }, null);

					final GraphFunction rightStore = new Shift(new FloatConstant((float) -0.1), new FloatConstant(0),
							null, new Graph(new Float[][]
					{
							{ Float.valueOf(numColumns), Float.valueOf((float) 2.5) } }, null));

					return new Union(new GraphFunction[]
					{ leftStore, bottomOuterRow, bottomInnerRow, bottomInnerInnerRow, topInnerInnerRow, topInnerRow,
							topOuterRow, rightStore }, Boolean.TRUE);
				}

				return new Union(new GraphFunction[]
				{ bottomOuterRow, bottomInnerRow, bottomInnerInnerRow, topInnerInnerRow, topInnerRow, topOuterRow },
						Boolean.TRUE);
			}

		}, track, tracks, null, null, SiteType.Vertex, largeStack);
		

		// We store these parameters to access them in the Mancala design.
		this.numRows   = rows.intValue();
		this.numColumns = columns.intValue();
		this.storeType = (store == null) ? StoreType.Outer : store;
		this.numStore = (numStores == null) ? 2 : numStores.intValue();

		if (numRows > 6 || numRows < 2)
			throw new IllegalArgumentException("Board: Only 2 to 6 rows are supported for the Mancala board.");

		int numNonNull = 0;
		if (track != null)
			numNonNull++;
		if (tracks != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Board: Only one of `track' or `tracks' can be non-null.");
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @return The number of rows.
	 */
	public int numRows()
	{
		return numRows;
	}

	/**
	 * @return The number of columns.
	 */
	public int numColumns()
	{
		return numColumns;
	}

	/**
	 * @return The type of the store.
	 */
	public StoreType storeType()
	{
		return storeType;
	}

	/**
	 * @return The number of stores.
	 */
	public int numStore()
	{
		return numStore;
	}

	/**
	 * @return The graph function corresponding to a two rows mancala board.
	 */
	public GraphFunction createTwoRowMancala()
	{
		final GraphFunction leftStore = new Graph(new Float[][]
		{
				{ Float.valueOf((float) 0.85), Float.valueOf((float) 0.5) } }, null);

		final GraphFunction rightStore = new Shift(new FloatConstant((float) -0.15), new FloatConstant(0), null,
				new Graph(new Float[][]
				{
						{ Float.valueOf(numColumns), Float.valueOf((float) 0.5) } }, null));

		final GraphFunction bottomRow = Rectangle.construct(new DimConstant(1), new DimConstant(numColumns), null);

		final GraphFunction topRow = new Shift(new FloatConstant(0), new FloatConstant(1), null,
				Rectangle.construct(new DimConstant(1), new DimConstant(numColumns), null));

		return new Union(new GraphFunction[]
		{ leftStore, bottomRow, topRow, rightStore }, Boolean.TRUE);
	}
	
	// ----------------------------------

	@Override
	public String toEnglish(final Game game) 
	{
		String englishString = numRows + " x " + numColumns + " Mancala board";
		
		if (numStore > 0)
			englishString += " with " + numStore + " " + storeType.name().toLowerCase() + " stores";

		return englishString;
	}

	// ----------------------------------

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		return readEvalContext;
	}
}

