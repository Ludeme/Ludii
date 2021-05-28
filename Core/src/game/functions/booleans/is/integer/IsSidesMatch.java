package game.functions.booleans.is.integer;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.equipment.component.Component;
import game.equipment.component.tile.Path;
import game.functions.booleans.BaseBooleanFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.last.LastTo;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.DirectionFacing;
import game.util.graph.Step;
import main.Constants;
import other.context.Context;
import other.state.container.ContainerState;
import other.topology.Cell;

/**
 * Is used to detect whether the terminus of a tile matches with its neighbours.
 * 
 * @author Eric.Piette
 * @remarks Used to detect whether a specific player is the mover. If no tile is
 *          on the location the function returns true.
 */
@Hide
public final class IsSidesMatch extends BaseBooleanFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The position of the tile to check. */
	private final IntFunction toFn;

	//-------------------------------------------------------------------------

	/**
	 * @param to The location of the tile [(lastTo)].
	 */
	public IsSidesMatch
	(
		@Opt final IntFunction to
	)
	{
		this.toFn = (to == null) ? new LastTo(null) : to;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean eval(final Context context)
	{
		final int to = toFn.eval(context);

		if (to == Constants.UNDEFINED)
			return false;

		final int cid = context.containerId()[to];
		final other.topology.Topology graph = context.containers()[cid].topology();
		final int ratioAdjOrtho = graph.supportedAdjacentDirections(SiteType.Cell).size()
				/ graph.supportedOrthogonalDirections(SiteType.Cell).size();
		final ContainerState cs = context.containerState(cid);
		final int what = cs.whatCell(to);

		if (what == 0)
			return false;

		final Component component = context.components()[what];

		if (!component.isTile())
			return true;

		final int numberEdges = graph.numEdges();
		int rotation = cs.rotation(to, SiteType.Cell) / ratioAdjOrtho;
		int[] terminus = component.terminus();
		final Integer numTerminus = component.numTerminus();
		final Path[] paths = component.paths();

		if (numTerminus != null)
		{
			terminus = new int[numberEdges];
			for (int i = 0; i < numberEdges; i++)
				terminus[i] = numTerminus.intValue();
		}

		final int[][] coloredTerminus = new int[numberEdges][];
		for (int i = 0; i < numberEdges; i++)
			coloredTerminus[i] = new int[terminus[i]];

		for (final Path path : paths)
		{
			final int side1 = path.side1().intValue();
			final int terminus1 = path.terminus1().intValue();
			final int side2 = path.side2().intValue();
			final int terminus2 = path.terminus2().intValue();
			final int colour = path.colour().intValue();

			coloredTerminus[side1][terminus1] = colour;
			coloredTerminus[side2][terminus2] = colour;
		}

		while (rotation != 0)
		{
			for (int i = 0; i < numberEdges - 1; i++)
			{
				final int[] temp = coloredTerminus[i + 1];
				coloredTerminus[i + 1] = coloredTerminus[0];
				coloredTerminus[0] = temp;
			}
			rotation--;
		}

		final Cell toCell = graph.cells().get(to);

//			System.out.println("toV is " + toCell.index());
//			System.out.print("{");
//			for (int i = 0; i < coloredTerminus.length; i++)
//			{
//				System.out.print(coloredTerminus[i][0] + " ");
//			}
//			System.out.println("}");

		for (final Cell vOrtho : toCell.orthogonal())
		{
			final int whatOrtho = cs.what(vOrtho.index(), SiteType.Cell);
			if (whatOrtho == 0)
				continue;

			final Component compOrtho = context.components()[whatOrtho];

			if (!compOrtho.isTile())
				continue;

			int rotationOrtho = cs.rotation(vOrtho.index(), SiteType.Cell) / ratioAdjOrtho;
			int[] terminusOrtho = compOrtho.terminus();
			final Integer numTerminusOrtho = compOrtho.numTerminus();
			final Path[] pathsOrtho = compOrtho.paths();

			if (numTerminusOrtho != null)
			{
				terminusOrtho = new int[numberEdges];
				for (int i = 0; i < numberEdges; i++)
					terminusOrtho[i] = numTerminusOrtho.intValue();
			}

			final int[][] coloredTerminusOrtho = new int[numberEdges][];
			for (int i = 0; i < numberEdges; i++)
				coloredTerminusOrtho[i] = new int[terminusOrtho[i]];

			for (final Path path : pathsOrtho)
			{
				final int side1 = path.side1().intValue();
				final int terminus1 = path.terminus1().intValue();
				final int side2 = path.side2().intValue();
				final int terminus2 = path.terminus2().intValue();
				final int colour = path.colour().intValue();

				coloredTerminusOrtho[side1][terminus1] = colour;
				coloredTerminusOrtho[side2][terminus2] = colour;
			}

			while (rotationOrtho != 0)
			{
				for (int i = 0; i < numberEdges - 1; i++)
				{
					final int[] temp = coloredTerminusOrtho[i + 1];
					coloredTerminusOrtho[i + 1] = coloredTerminusOrtho[0];
					coloredTerminusOrtho[0] = temp;
				}
				rotationOrtho--;
			}

//				System.out.println("vOrtho is " + vOrtho.index());
//				System.out.print("{");
//				for (int i = 0; i < coloredTerminusOrtho.length; i++)
//				{
//					System.out.print(coloredTerminusOrtho[i][0] + " ");
//				}
//				System.out.println("}");

			final List<DirectionFacing> directions = graph.supportedOrthogonalDirections(SiteType.Cell);
			
			int indexSideTile = 0;
			for (; indexSideTile < directions.size(); indexSideTile++)
			{
				final DirectionFacing direction = directions.get(indexSideTile);
				final AbsoluteDirection absDirection = direction.toAbsolute();
				final List<game.util.graph.Step> steps = graph.trajectories().steps(SiteType.Cell, toCell.index(),
						SiteType.Cell, absDirection);

				boolean found = false;
				for (final Step step : steps)
					if (step.to().id() == vOrtho.index())
					{
//						System.out.println("Direction form = " + vOrtho.index() + " and the step.to() = " + to
//								+ " is " + direction);
//						System.out.println("SO INDEX SIDE IS " + indexSideTile);
						found = true;
						break;
					}

				if (found)
					break;
			}
			

			final int[] toColor = coloredTerminus[indexSideTile];
			final int indexSideOrthoTile = (indexSideTile
					+ (graph.numEdges() / 2))
					% graph.numEdges(); 
			final int[] orthoColor = coloredTerminusOrtho[indexSideOrthoTile];

			for (int i = 0; i < toColor.length; i++)
				if (toColor[i] != orthoColor[orthoColor.length - (1 + i)])
				{
					// System.out.println("not match");
					return false;
				}
		}

		// System.out.println("match");
		return true;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public long gameFlags(final Game game)
	{
		return toFn.gameFlags(game);
	}

	@Override
	public BitSet concepts(final Game game)
	{
		return toFn.concepts(game);
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(toFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(toFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		toFn.preprocess(game);
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
			game.addRequirementToReport(
					"The ludeme (is SidesMatch ...) is used but the equipment has no tiles.");
			missingRequirement = true;
		}

		missingRequirement |= toFn.missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= toFn.willCrash(game);
		return willCrash;
	}
}
