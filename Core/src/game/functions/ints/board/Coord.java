package game.functions.ints.board;

import java.util.BitSet;
import java.util.List;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.types.board.SiteType;
import main.Constants;
import other.context.Context;
import other.topology.SiteFinder;
import other.topology.TopologyElement;

/**
 * Returns the site index of a given board coordinate.
 * 
 * @author Eric.Piette
 */
public final class Coord extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The coordinate of the site. */
	private final String coord;
	
	/** The row index. */
	private final IntFunction rowFn;
	
	/** The column index. */
	private final IntFunction columnFn;
	
	/** Cell, Edge or Vertex. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/** The pre-computed value. */
	private int precomputedValue = Constants.OFF;

	//-------------------------------------------------------------------------

	/**
	 * For getting a site according to the coordinate.
	 * 
	 * @param type       The graph element type [default SiteType of the board].
	 * @param coordinate The coordinates of the site.
	 * 
	 * @example (coord "A1")
	 */
	public Coord
	(
		@Opt final SiteType type, 
		     final String   coordinate
	)
	{
		coord = coordinate;
		this.type = type;
		rowFn = null;
		columnFn = null;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * For getting a site according to the row and column indices.
	 * 
	 * @param type   The graph element type [default SiteType of the board].
	 * @param row    The row index.
	 * @param column The column index.
	 * 
	 * @example (coord row:1 column:5)
	 */
	public Coord
	(
		@Opt  final SiteType type, 
		@Name final IntFunction row,
		@Name final IntFunction column
	)
	{
		coord = null;
		rowFn = row;
		columnFn = column;
		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (precomputedValue != Constants.OFF)
			return precomputedValue;

		if (coord != null)
		{
			final TopologyElement element = SiteFinder.find(context.board(), coord, type);

			if (element == null)
				return Constants.OFF;
			else
				return element.index();
		}
		else
		{
			final int row = rowFn.eval(context);
			final int column = columnFn.eval(context);
			final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
			final List<? extends TopologyElement> elements = context.topology().getGraphElements(realType);
			for (final TopologyElement element : elements)
				if (element.row() == row && element.col() == column)
					return element.index();

			return Constants.OFF;
		}

	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		if(coord != null)
			return true;
		else
			return rowFn.isStatic() && columnFn.isStatic();
	}

	@Override
	public long gameFlags(final Game game)
	{
		long flags = 0L;
		flags |= SiteType.gameFlags(type);
		if(rowFn != null)
			flags |= rowFn.gameFlags(game);
		if(columnFn != null)
			flags |= columnFn.gameFlags(game);
		return flags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		if(rowFn != null)
			concepts.or(rowFn.concepts(game));
		if(columnFn != null)
			concepts.or(columnFn.concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		if (rowFn != null)
			writeEvalContext.or(rowFn.writesEvalContextRecursive());
		if (columnFn != null)
			writeEvalContext.or(columnFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		if (rowFn != null)
			readEvalContext.or(rowFn.readsEvalContextRecursive());
		if (columnFn != null)
			readEvalContext.or(columnFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		if (rowFn != null)
			rowFn.preprocess(game);
		if (columnFn != null)
			columnFn.preprocess(game);
		if (isStatic())
			precomputedValue = eval(new Context(game, null));
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		if (coord != null)
			return coord;
		else
			return "the " + type.name().toLowerCase() + " at row " + rowFn.toEnglish(game) + " and column " + columnFn.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------
}
