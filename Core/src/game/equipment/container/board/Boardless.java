package game.equipment.container.board;

import java.util.BitSet;

import annotations.Opt;
import game.Game;
import game.functions.dim.DimConstant;
import game.functions.dim.DimFunction;
import game.functions.graph.generators.basis.hex.HexagonOnHex;
import game.functions.graph.generators.basis.square.RectangleOnSquare;
import game.functions.graph.generators.basis.tri.TriangleOnTri;
import game.types.board.SiteType;
import game.types.board.TilingBoardlessType;
import game.types.state.GameType;
import main.Constants;
import metadata.graphics.util.ContainerStyleType;
import other.BaseLudeme;
import other.concept.Concept;
import other.translation.LanguageUtils;

/**
 * Defines a boardless container growing in function of the pieces played.
 *
 * @author Eric.Piette
 * 
 * @remarks The playable sites of the board will be all the sites adjacent to
 *          the places already played/placed. No pregeneration is computed on
 *          the graph except the centre.
 */
public class Boardless extends Board
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @param tiling    The tiling of the boardless container.
	 * @param dimension The "fake" size of the board used for boardless [41].
	 * 
	 * @example (boardless Hexagonal)
	 */
	public Boardless
	(
		     final TilingBoardlessType tiling,
		@Opt final DimFunction         dimension
	)
	{
		super
		(
			tiling == TilingBoardlessType.Square
				? new RectangleOnSquare(dimension == null ? new DimConstant(Constants.SIZE_BOARDLESS) : dimension, null, null, null)
				: tiling == TilingBoardlessType.Hexagonal 
					? new HexagonOnHex(dimension == null ? new DimConstant(Constants.SIZE_HEX_BOARDLESS) : dimension)
						: new TriangleOnTri(dimension == null ? new DimConstant(Constants.SIZE_BOARDLESS) : dimension),
				null, 
				null,
				null, 
				null, 
				SiteType.Cell
		);

		this.style = ContainerStyleType.Boardless;
	}

	@Override
	public boolean isBoardless()
	{
		return true;
	}

	@Override
	public long gameFlags(final Game game)
	{
		return super.gameFlags(game) | GameType.Boardless;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.Boardless.id(), true);
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
	
	@Override
	public String toEnglish(final Game game) 
	{
		return "table" ;
	}
}