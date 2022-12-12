package game.rules.play.moves.nonDecision.operators.foreach.piece;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiPredicate;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import annotations.Or2;
import game.Game;
import game.equipment.component.Component;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntFunction;
import game.functions.ints.state.Mover;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Then;
import game.rules.play.moves.nonDecision.operator.Operator;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.types.state.GameType;
import gnu.trove.list.array.TIntArrayList;
import other.ContainerId;
import other.concept.Concept;
import other.context.Context;
import other.context.EvalContextData;
import other.location.Location;
import other.move.Move;
import other.move.MovesIterator;
import other.state.State;
import other.state.container.ContainerState;
import other.state.owned.Owned;
import other.state.stacking.BaseContainerStateStacking;

/**
 * Iterates through the pieces, generating moves based on their positions.
 * 
 * @author mrraow and cambolbro and Eric.Piette
 * 
 * @remarks To generate a set of legal moves by type of piece. If some specific
 *          moves are described in this ludeme, they are applied to all the
 *          pieces described on that ludeme, if not the moves of each component
 *          are used.
 */
@Hide
public final class ForEachPiece extends Operator
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * Useful only if we need a special move by piece and the generator on the
	 * piece.
	 */
	protected final Moves specificMoves;

	/** Useful only to specify the moves only for a specific item. */
	protected final String[] items;
	
	/** To apply ByPiece on this player. */
	protected final IntFunction player;
	
	/** Which container. */
	protected final ContainerId containerId;
	
	/** If true only the piece in the top of the stack. */
	protected final BooleanFunction topFn;
	
	/** The value set by the description before the null check. */
	protected final BooleanFunction topValueSet;

	/** Cell/Edge/Vertex. */
	protected SiteType type;

	/** The roleType of the owner of the pieces. */
	protected RoleType role;

	/**
	 * For every player, a small array with component indices relevant for that
	 * player. This is precomputed in the preprocess() step.
	 */
	protected int[][] compIndicesPerPlayer;

	//-------------------------------------------------------------------------

	/**
	 * @param on            Type of graph element where the pieces are.
	 * @param item          The name of the piece.
	 * @param items         The names of the pieces.
	 * @param container     The index of the container.
	 * @param containerName The name of the container.
	 * @param specificMoves The specific moves to apply to the pieces.
	 * @param player        The owner of the piece [(player (mover))].
	 * @param role          RoleType of the owner of the piece [Mover].
	 * @param top           To apply the move only to the top piece in case of a
	 *                      stack [False].
	 * @param then          The moves applied after that move is applied.
	 */
	public ForEachPiece
	(
		@Opt      @Name  final SiteType               on,
		@Opt @Or		 final String                 item, 
		@Opt @Or		 final String[]               items, 
		@Opt @Or2 @Name  final IntFunction            container, 
		@Opt @Or2 		 final String                 containerName,
		@Opt 			 final Moves                  specificMoves, 
		@Opt @Or2		 final game.util.moves.Player player, 
		@Opt @Or2		 final RoleType               role, 
		@Opt      @Name  final BooleanFunction        top, 
		@Opt 			 final Then                   then
	)
	{
		super(then);

		if (items != null)
			this.items = items;
		else
			this.items = (item == null) ? new String[0] : new String[]{ item };
		this.specificMoves = specificMoves;
		this.player = (player == null) ? ((role == null) ? new Mover() : RoleType.toIntFunction(role)) : player.index();
		containerId = new ContainerId(container, containerName, null, null, null);
		topValueSet= top;
		topFn = (top == null) ? new BooleanConstant(false) : top;
		type = on;
		this.role = role;
	}

	//-------------------------------------------------------------------------

	@Override
	public MovesIterator movesIterator(final Context context)
	{
		return new MovesIterator()
		{
			// A bunch of things we only need to compute once for our iterator
			private final int specificPlayer = player.eval(context);
			private final Owned owned = context.state().owned();
			private final List<? extends Location>[] ownedComponents = owned.positions(specificPlayer);
			private final Component[] components = context.components();
			private final int cont = containerId.eval(context);
			private final ContainerState cs = context.containerState(cont);
			private final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
			private final int minIndex = cs == null ? 0 : context.game().equipment().sitesFrom()[cont];
			private final int maxIndex = cs == null ? 0 : minIndex + ((cont != 0) ? context.containers()[cont].numSites()
					: context.topology().getGraphElements(realType).size());
			private final boolean top = topFn.eval(context);
			private final int[] moverCompIndices = compIndicesPerPlayer[specificPlayer];

			// Keep track of where we are with our iterator
			private int compIdx = 0; // Index for components list
			private int locIdx = 0; // Index for list of locations
			private int moveIdx = 0; // Index for list of moves generated by a specific piece
			private Moves pieceMoves = null; // List of moves generated by a specific piece

			// Compute our first move to return
			private Move nextMove = context.trial().over() ? null : computeNextMove();

			@Override
			public boolean hasNext()
			{
				return (nextMove != null);
			}

			@Override
			public Move next()
			{
				final Move ret = nextMove;

				if (then() != null)
					ret.then().add(then().moves());

				nextMove = computeNextMove();
				ret.setMover(context.state().mover());

				return ret;
			}

			/**
			 * Computes the move to return for the subsequent next() call
			 * 
			 * @return
			 */
			private Move computeNextMove()
			{
				if(cs == null)
					return null;
				
				while (true)
				{
					if (pieceMoves != null)
					{
						// We may still have more moves to return in list already generated
						if (moveIdx < pieceMoves.moves().size())
						{
							return pieceMoves.moves().get(moveIdx++);
						}
						else
						{
							pieceMoves = null; // Time to move on to next piece
//									moveIdx = 0;
//									locIdx = 0;
//									++compIdx;
						}
					}
					else
					{
						if (compIdx < moverCompIndices.length)
						{
							final int componentId = moverCompIndices[compIdx];

							List<? extends Location> positions = null;
							if (role != RoleType.All && role != RoleType.Each)
							{
								positions = ownedComponents[owned.mapCompIndex(specificPlayer, componentId)];
							}
							else
							{
								final int ownerComponent = context.components()[componentId].owner();
								final List<? extends Location>[] ownedCurrentComponent = owned
										.positions(ownerComponent);

								positions = ownedCurrentComponent[owned.mapCompIndex(ownerComponent, componentId)];
							}

							// If the ludeme asks for a specific site type we keep only the correct ones.
							List<Location> filteredPositions = null;
							if (type != null && !positions.isEmpty())
							{
								filteredPositions = new ArrayList<Location>();
								for (int i = positions.size() - 1; i >= 0; i--)
								{
									final Location loc = positions.get(i);
									if (loc.siteType().equals(type))
										filteredPositions.add(loc);
								}
							}

							final List<? extends Location> correctPositions = (filteredPositions == null) ? positions
									: filteredPositions;

							if (correctPositions != null && !correctPositions.isEmpty())
							{
								final Component component = components[componentId];

								if (locIdx < correctPositions.size())
								{
									final int location = correctPositions.get(locIdx).site();
									if (location >= minIndex && location < maxIndex)
									{
										final int level = correctPositions.get(locIdx).level();

										// We're incrementing locIdx, so reset moveIdx
										moveIdx = 0;
										++locIdx;

										if (top)
										{
											final BaseContainerStateStacking css = (BaseContainerStateStacking) cs;
											if (css.sizeStack(location, realType) != (level + 1))
												continue;
										}
										
										final int origFrom = context.from();
										final int origLevel = context.level();
										final State state = context.state();

										context.setFrom(location);
										context.setLevel(level);

										if (specificMoves == null)
										{
											// If the specific player is the mover or shared we compute directly the
											// pieces movement.
											if (specificPlayer == state.mover()
													|| specificPlayer > context.game().players().count()
													|| specificPlayer == 0)
											{
												pieceMoves = component.generate(context);
											}
											else
											{
												// We have to modify the context in case that the specific player is
												// Next.
												final int oldPrev = state.prev();
												final int oldMover = state.mover();
												final int oldNext = state.next();
												state.setPrev(oldMover);
												state.setMover(specificPlayer);
												state.setNext(oldMover);
												pieceMoves = component.generate(context);
												state.setPrev(oldPrev);
												state.setMover(oldMover);
												state.setNext(oldNext);
												
												// Old code: commented because making full copy of context 
												// seems unnecessary and slow
												//
//												final Context newContext = new TempContext(context);
//												newContext.state().setPrev(context.state().mover());
//												newContext.state().setMover(specificPlayer);
//												newContext.state().setNext(context.state().mover());
//												pieceMoves = component.generate(newContext);
											}
										}
										else
										{
											// If the specific player is the mover or shared we compute directly the
											// pieces movement.
											if (specificPlayer == state.mover()
													|| specificPlayer > context.game().players().count()
													|| specificPlayer == 0)
											{
												pieceMoves = specificMoves.eval(context);
											}
											else
											{
												// We have to modify the context in case that the specific player is
												// Next.
												final int oldPrev = state.prev();
												final int oldMover = state.mover();
												final int oldNext = state.next();
												state.setPrev(oldMover);
												state.setMover(specificPlayer);
												state.setNext(oldMover);
												pieceMoves = specificMoves.eval(context);
												state.setPrev(oldPrev);
												state.setMover(oldMover);
												state.setNext(oldNext);
												
												// Old code: commented because making full copy of context 
												// seems unnecessary and slow
												//
//												final Context newContext = new Context(context);
//												newContext.state().setPrev(context.state().mover());
//												newContext.state().setMover(specificPlayer);
//												newContext.state().setNext(context.state().mover());
//												pieceMoves = specificMoves.eval(newContext);
											}
										}
										
										context.setFrom(origFrom);
										context.setLevel(origLevel);
									}
									else
									{
										// We're incrementing locIdx, so reset moveIdx
										moveIdx = 0;
										++locIdx;
									}
								}
								else
								{
									// We're incrementing index for list of comp indices, so reset locIdx and
									// moveIdx
									moveIdx = 0;
									locIdx = 0;
									++compIdx;
								}
							}
							else
							{
								// We're incrementing index for list of comp indices, so reset locIdx and
								// moveIdx
								moveIdx = 0;
								locIdx = 0;
								++compIdx;
							}
						}
						else
						{
							// No more moves to be found
							return null;
						}
					}
				}
			}

			@Override
			public boolean canMoveConditionally(final BiPredicate<Context, Move> predicate)
			{
				while (nextMove != null)
				{
					if (then() != null)
						nextMove.then().add(then().moves());
					nextMove.setMover(specificPlayer);

					if (predicate.test(context, nextMove))
						return true;
					else
						nextMove = computeNextMove();
				}

				return false;
			}

		};
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final Iterator<Move> it = movesIterator(context);
		final Moves moves = new BaseMoves(super.then());

		while (it.hasNext())
			moves.moves().add(it.next());

		return moves;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = player.gameFlags(game) | super.gameFlags(game);
		gameFlags |= topFn.gameFlags(game);

		if(topValueSet != null)
			gameFlags |= GameType.Stacking;
		
		if (type != null)
			gameFlags |= SiteType.gameFlags(type);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		if (specificMoves != null)
		{
			gameFlags |= specificMoves.gameFlags(game);
		}
		else
		{
			final Component[] components = game.equipment().components();

			for (int e = 1; e < components.length; ++e)
			{
				final Moves generator = components[e].generator();
				if (generator != null)
				{
					gameFlags |= generator.gameFlags(game);
				}
			}
		}
		
		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		if (type != null)
			concepts.or(SiteType.concepts(type));
		concepts.or(player.concepts(game));
		concepts.or(topFn.concepts(game));
		concepts.set(Concept.ForEachPiece.id(), true);

		if (then() != null)
			concepts.or(then().concepts(game));

		if (specificMoves != null)
		{
			concepts.or(specificMoves.concepts(game));
		}
		else
		{
			final Component[] components = game.equipment().components();

			for (int e = 1; e < components.length; ++e)
			{
				final Moves generator = components[e].generator();
				if (generator != null)
				{
					concepts.or(generator.concepts(game));
				}
			}
		}

		concepts.set(Concept.ControlFlowStatement.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = writesEvalContextFlat();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(player.writesEvalContextRecursive());
		writeEvalContext.or(topFn.writesEvalContextRecursive());
		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());

		if (specificMoves != null)
			writeEvalContext.or(specificMoves.writesEvalContextRecursive());
		return writeEvalContext;
	}
	
	@Override
	public BitSet writesEvalContextFlat()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.set(EvalContextData.From.id(), true);
		writeEvalContext.set(EvalContextData.Level.id(), true);
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(player.readsEvalContextRecursive());
		readEvalContext.or(topFn.readsEvalContextRecursive());
		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());

		if (specificMoves != null)
			readEvalContext.or(specificMoves.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= player.missingRequirement(game);
		missingRequirement |= topFn.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);

		if (specificMoves != null)
		{
			missingRequirement |= specificMoves.missingRequirement(game);
		}
		else
		{
			final Component[] components = game.equipment().components();

			for (int e = 1; e < components.length; ++e)
			{
				final Moves generator = components[e].generator();
				if (generator != null)
				{
					missingRequirement |= generator.missingRequirement(game);
				}
			}
		}
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= player.willCrash(game);
		willCrash |= topFn.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);

		if (specificMoves != null)
		{
			willCrash |= specificMoves.willCrash(game);
		}
		else
		{
			final Component[] components = game.equipment().components();

			for (int e = 1; e < components.length; ++e)
			{
				final Moves generator = components[e].generator();
				if (generator != null)
				{
					willCrash |= generator.willCrash(game);
				}
			}
		}
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		boolean isStatic = player.isStatic();
		isStatic = isStatic && topFn.isStatic();

		if (specificMoves != null)
			return isStatic && specificMoves.isStatic();
		else
			return false;
	}

	@Override
	public void preprocess(final Game game)
	{
		super.preprocess(game);

		if (specificMoves != null)
			specificMoves.preprocess(game);

		final Component[] comps = game.equipment().components();
		for (int e = 1; e < comps.length; ++e)
		{
			final Component comp = comps[e];
			if (comp.generator() != null)
			{
				comp.generator().preprocess(game);
			}
		}

		final boolean allPlayers = (role == RoleType.All || role == RoleType.Each);

		compIndicesPerPlayer = new int[game.players().size() + 1][];
		for (int p = 0; p <= game.players().size(); ++p)
		{
			final TIntArrayList compIndices = new TIntArrayList();

			for (int e = 1; e < comps.length; ++e)
			{
				final Component comp = comps[e];

				if (comp.owner() == p || (allPlayers && p == game.players().size()))
				{
					if (items.length == 0)
					{
						compIndices.add(e);
					}
					else
					{
						for (final String item : items)
						{
							if (comp.getNameWithoutNumber() != null && comp.getNameWithoutNumber().equals(item))
							{
								compIndices.add(e);
								break;
							}
						}
					}
				}
			}

			compIndicesPerPlayer[p] = compIndices.toArray();
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Specific moves for the piece
	 */
	public Moves specificMoves()
	{
		return specificMoves;
	}

	/**
	 * @return The kind of items.
	 */
	public String[] items()
	{
		return items;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toEnglish(final Game game)
	{
		return "move one of your pieces";	//"During your turn, move one of your pieces";
	}
}
