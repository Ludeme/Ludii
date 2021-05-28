package game.functions.ints.tile;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import annotations.Opt;
import annotations.Or;
import game.Game;
import game.equipment.component.Component;
import game.functions.directions.Directions;
import game.functions.directions.DirectionsFunction;
import game.functions.ints.BaseIntFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.last.LastTo;
import game.functions.ints.state.Mover;
import game.functions.region.RegionFunction;
import game.functions.region.sites.simple.SitesLastTo;
import game.types.board.RelationType;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.RelativeDirection;
import game.util.equipment.Region;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;
import other.topology.Cell;
import other.topology.Topology;

/**
 * Returns the maximum extent of a path.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * @remarks The path extent is the maximum board width and/or height that the path extends to. 
 *          This is used in tile-based games with paths, such as Trax.
  */
public final class PathExtent extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	/** The colour of the path. */
	private final IntFunction colourFn;

	/** The starting point of the path. */
	private final IntFunction startFn;

	/** The starting points of the path. */
	private final RegionFunction regionStartFn;

	//-------------------------------------------------------------------------

	/**
	 * @param colour      The colour of the path [(mover)].
	 * @param start       The starting point of the path [(lastTo)].
	 * @param regionStart The starting points of the path [(regionLastToMove)].
	 * 
	 * @example (pathExtent (mover))
	 */
	public PathExtent
	(
			@Opt IntFunction    colour,
		@Or @Opt IntFunction    start,
		@Or @Opt RegionFunction regionStart
	)
	{
		int numNonNull = 0;
		if (start != null)
			numNonNull++;
		if (regionStart != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Zero or one Or parameter can be non-null.");

		this.colourFn = (colour == null) ? new Mover() : colour;
		this.startFn = (start == null) ? new LastTo(null) : start;
		this.regionStartFn = (regionStart == null) ? ((start == null) ? new SitesLastTo() : null) : regionStart;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		final int[] regionToCheck;
		if (regionStartFn != null)
		{
			final Region region = regionStartFn.eval(context);
			regionToCheck = region.sites();
		}
		else
		{
			regionToCheck = new int[1];
			regionToCheck[0] = startFn.eval(context);
		}

		int maxExtent = 0;

		for (int p = 0; p < regionToCheck.length; p++)
		{
			final int from = regionToCheck[p];
			if (from == Constants.UNDEFINED)
				return maxExtent;

			// final int length = extentFn.eval(context);
			final int colourLoop = colourFn.eval(context);
			final Topology graph = context.topology();
			final Cell fromCell = graph.cells().get(from);
			final int fromRow = fromCell.row();
			final int fromCol = fromCell.col();
			final int cid = context.containerId()[from];
			final ContainerState cs = context.state().containerStates()[cid];
			final int whatSideId = cs.what(from, SiteType.Cell);
			if (whatSideId == 0 || !context.components()[whatSideId].isTile())
				return maxExtent;

			final DirectionsFunction directionFunction = new Directions(RelativeDirection.Forward, null,
					RelationType.Orthogonal, null);
			final int ratioAdjOrtho = context.topology().numEdges();

			final TIntArrayList tileConnected = new TIntArrayList();
			final TIntArrayList originTileConnected = new TIntArrayList();
			tileConnected.add(from);
			originTileConnected.add(from);

			for (int index = 0; index < tileConnected.size(); index++)
			{
				final int site = tileConnected.getQuick(index);
				final Cell cell = graph.cells().get(site);
				final int what = cs.what(site, SiteType.Cell);
				final Component component = context.components()[what];
				final int rotation = (cs.rotation(site, SiteType.Cell) * 2) / ratioAdjOrtho;
				final game.equipment.component.tile.Path[] paths = Arrays.copyOf(component.paths(),
						component.paths().length);

				for (int i = 0; i < paths.length; i++)
				{
					final game.equipment.component.tile.Path path = paths[i];
					if (path.colour().intValue() == colourLoop)
					{
						// Side1
						final List<AbsoluteDirection> directionsStep1 = directionFunction.convertToAbsolute(SiteType.Cell,
								cell, null, null, Integer.valueOf(path.side1(rotation,
										graph.numEdges())),
								context);
						final AbsoluteDirection directionSide1 = directionsStep1.get(0);
						final List<game.util.graph.Step> stepsSide1 = graph.trajectories().steps(SiteType.Cell,
								cell.index(),
								SiteType.Cell, directionSide1);
						if (stepsSide1.size() != 0)
						{
							final int site1Connected = stepsSide1.get(0).to().id();
							final Cell cell1Connected = graph.cells().get(site1Connected);
							final int rowCell1 = cell1Connected.row();
							final int colCell1 = cell1Connected.col();

							final int drow = Math.abs(rowCell1 - fromRow);
							final int dcol = Math.abs(colCell1 - fromCol);

							if (drow > maxExtent)
								maxExtent = drow;
							if (dcol > maxExtent)
								maxExtent = dcol;

							final int whatSide1 = cs.what(site1Connected, SiteType.Cell);
							if (originTileConnected.getQuick(index) != site1Connected && whatSide1 != 0
									&& context.components()[whatSide1].isTile())
							{
								tileConnected.add(site1Connected);
								originTileConnected.add(site);
							}
						}

						// Side 2
						final List<AbsoluteDirection> directionsSide2 = directionFunction.convertToAbsolute(
								SiteType.Cell, cell, null, null,
								Integer.valueOf(path.side2(rotation, graph.numEdges())), context);
						final AbsoluteDirection directionSide2 = directionsSide2.get(0);
						final List<game.util.graph.Step> stepsSide2 = graph.trajectories().steps(SiteType.Cell,
								cell.index(), SiteType.Cell, directionSide2);

						if (stepsSide2.size() != 0)
						{
							final int site2Connected = stepsSide2.get(0).to().id();
							final Cell cell2Connected = graph.cells().get(site2Connected);
							final int rowCell2 = cell2Connected.row();
							final int colCell2 = cell2Connected.col();

							final int drow = Math.abs(rowCell2 - fromRow);
							final int dcol = Math.abs(colCell2 - fromCol);

							if (drow > maxExtent)
								maxExtent = drow;
							if (dcol > maxExtent)
								maxExtent = dcol;

							final int whatSide2 = cs.what(site2Connected, SiteType.Cell);
							if (originTileConnected.getQuick(index) != site2Connected && whatSide2 != 0
									&& context.components()[whatSide2].isTile())
							{
								tileConnected.add(site2Connected);
								originTileConnected.add(site);
							}
						}
					}
				}
			}
		}

		return maxExtent;
	}

	//-----------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = colourFn.gameFlags(game) | startFn.gameFlags(game);

		if (regionStartFn != null)
			gameFlags |= regionStartFn.gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(colourFn.concepts(game));
		concepts.or(startFn.concepts(game));
		concepts.set(Concept.PathExtent.id(), true);

		if (regionStartFn != null)
			concepts.or(regionStartFn.concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(colourFn.writesEvalContextRecursive());
		writeEvalContext.or(startFn.writesEvalContextRecursive());

		if (regionStartFn != null)
			writeEvalContext.or(regionStartFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		colourFn.preprocess(game);
		if (startFn != null)
			startFn.preprocess(game);
		if (regionStartFn != null)
			regionStartFn.preprocess(game);
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		boolean gameHasTile = false;
		for (int i = 1; i < game.equipment().components().length; i++)
			if (game.equipment().components()[i].isTile())
			{
				gameHasTile = true;
				break;
			}

		if (!gameHasTile)
		{
			game.addRequirementToReport("The ludeme (pathExtent ...) is used but the equipment has no tiles.");
			missingRequirement = true;
		}

		missingRequirement |= colourFn.missingRequirement(game);
		missingRequirement |= startFn.missingRequirement(game);

		if (regionStartFn != null)
			missingRequirement |= regionStartFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= colourFn.willCrash(game);
		willCrash |= startFn.willCrash(game);

		if (regionStartFn != null)
			willCrash |= regionStartFn.willCrash(game);
		return willCrash;
	}
}
