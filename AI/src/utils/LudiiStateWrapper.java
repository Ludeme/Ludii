package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import game.equipment.container.Container;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import main.collections.FastArrayList;
import other.RankUtils;
import other.context.Context;
import other.context.TempContext;
import other.move.Move;
import other.state.State;
import other.state.container.ContainerState;
import other.state.owned.Owned;
import other.state.stacking.BaseContainerStateStacking;
import other.trial.Trial;

/**
 * Wrapper around a Ludii context (trial + state), with various extra methods required for
 * other frameworks that like to wrap around Ludii (e.g. OpenSpiel, Polygames)
 * 
 * @author Dennis Soemers
 */
public final class LudiiStateWrapper
{
	
	//-------------------------------------------------------------------------
	
	/** Reference back to our wrapped Ludii game */
	protected LudiiGameWrapper game;
	
	/** Our wrapped context */
	protected Context context;
	
	/** Our wrapped trial */
	protected Trial trial;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 * @param game
	 */
	public LudiiStateWrapper(final LudiiGameWrapper game)
	{
		this.game = game;
		trial = new Trial(game.game);
		context = new Context(game.game, trial);
		game.game.start(context);
	}
	
	/**
	 * Constructor
	 * @param gameWrapper
	 * @param context
	 */
	public LudiiStateWrapper(final LudiiGameWrapper gameWrapper, final Context context)
	{
		this.game = gameWrapper;
		trial = context.trial();
		this.context = context;
	}
	
	/**
	 * Copy constructor
	 * @param other
	 */
	public LudiiStateWrapper(final LudiiStateWrapper other)
	{
		this.game = other.game;
		this.context = new Context(other.context);
		this.trial = this.context.trial();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Copies data from given other LudiiStateWrapper.
	 * TODO can optimise this if we provide optimised copyFrom implementations
	 * for context and trial!
	 * 
	 * @param other
	 */
	public void copyFrom(final LudiiStateWrapper other)
	{
		this.game = other.game;
		this.context = new Context(other.context);
		this.trial = this.context.trial();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param actionID
	 * @param player
	 * @return A string (with fairly detailed information) on the move(s) represented
	 * by the given actionID in the current game state.
	 */
	public String actionToString(final int actionID, final int player)
	{
		final FastArrayList<Move> legalMoves;
		
		if (game.isSimultaneousMoveGame())
			legalMoves = AIUtils.extractMovesForMover(game.game.moves(context).moves(), player + 1);
		else
			legalMoves = game.game.moves(context).moves();
			
		final List<Move> moves = new ArrayList<Move>();
		for (final Move move : legalMoves)
		{
			if (game.moveToInt(move) == actionID)
				moves.add(move);
		}
		
		if (moves.isEmpty())
		{
			return "[Ludii found no move for ID: " + actionID + "!]";
		}
		else if (moves.size() == 1)
		{
			return moves.get(0).toTrialFormat(context);
		}
		else
		{
			final StringBuilder sb = new StringBuilder();
			
			sb.append("[Multiple Ludii moves for ID=" + actionID + ": ");
			sb.append(moves);
			sb.append("]");
			
			return sb.toString();
		}
	}
	
	/**
	 * Applies the given move
	 * @param move
	 */
	public void applyMove(final Move move)
	{
		game.game.apply(context, move);
	}
	
	/**
	 * Applies the nth legal move in current game state
	 * 
	 * @param n Legal move index. NOTE: index in Ludii's list of legal move,
	 * 	not a move converted into an int for e.g. OpenSpiel representation
	 */
	public void applyNthMove(final int n)
	{
		final FastArrayList<Move> legalMoves = game.game.moves(context).moves();
		final Move moveToApply = legalMoves.get(n);
		game.game.apply(context, moveToApply);
	}
	
	/**
	 * Applies a move represented by given int in the single-int-action-representation.
	 * Note that this method may have to randomly select a move among multiple legal
	 * moves if multiple different legal moves are represented by the same int.
	 * 
	 * @param action
	 * @param player
	 */
	public void applyIntAction(final int action, final int player)
	{
		final FastArrayList<Move> legalMoves;
		
		if (game.isSimultaneousMoveGame())
			legalMoves = AIUtils.extractMovesForMover(game.game.moves(context).moves(), player + 1);
		else
			legalMoves = game.game.moves(context).moves();
			
		final List<Move> moves = new ArrayList<Move>();
		for (final Move move : legalMoves)
		{
			if (game.moveToInt(move) == action)
				moves.add(move);
		}
		
		game.game.apply(context, moves.get(ThreadLocalRandom.current().nextInt(moves.size())));
	}
	
	@Override
	public LudiiStateWrapper clone()
	{
		return new LudiiStateWrapper(this);
	}
	
	/**
	 * @return Current player to move (not accurate in simultaneous-move games).
	 * Returns a 0-based index. Returns the player/agent to move, not necessarily
	 * the "colour" to move (may be different in games with Swap rule).
	 */
	public int currentPlayer()
	{
		return context.state().playerToAgent(context.state().mover()) - 1;
	}
	
	/**
	 * @return True if and only if current trial is over (terminal game state reached)
	 */
	public boolean isTerminal()
	{
		return trial.over();
	}
	
	/**
	 * @return The full Zobrist hash of the current state
	 */
	public long fullZobristHash()
	{
		return context.state().fullHash();
	}
	
	/**
	 * Resets this game state back to an initial game state
	 */
	public void reset()
	{
		game.game.start(context);
	}
	
	/**
	 * @return Array of legal Move objects
	 */
	public Move[] legalMovesArray()
	{
		return game.game.moves(context).moves().toArray(new Move[0]);
	}
	
	/**
	 * @return Array of indices for legal moves. For a game state with N legal moves,
	 * this will always simply be [0, 1, 2, ..., N-1]
	 */
	public int[] legalMoveIndices()
	{
		final FastArrayList<Move> moves = game.game.moves(context).moves();
		final int[] indices = new int[moves.size()];
		
		for (int i = 0; i < indices.length; ++i)
		{
			indices[i] = i;
		}
		
		return indices;
	}
	
	/**
	 * @return Number of legal moves in current state
	 */
	public int numLegalMoves()
	{
		return Math.max(1, game.game.moves(context).moves().size());
	}
	
	/**
	 * @return Array of integers corresponding to moves that are legal in current
	 * game state.
	 */
	public int[] legalMoveInts()
	{
		final FastArrayList<Move> moves = game.game.moves(context).moves();
		final TIntArrayList moveInts = new TIntArrayList(moves.size());
		
		// TODO could speed up this method by implementing an auto-sorting
		// class that extends TIntArrayList
		for (final Move move : moves)
		{
			final int toAdd = game.moveToInt(move);
			if (!moveInts.contains(toAdd))
				moveInts.add(toAdd);
		}
		
		moveInts.sort();

		return moveInts.toArray();
	}
	
	/**
	 * @param player
	 * @return Array of integers corresponding to moves that are legal in current
	 * game state for the given player.
	 */
	public int[] legalMoveIntsPlayer(final int player)
	{
		final FastArrayList<Move> legalMoves;
		
		if (game.isSimultaneousMoveGame())
			legalMoves = AIUtils.extractMovesForMover(game.game.moves(context).moves(), player + 1);
		else
			legalMoves = game.game.moves(context).moves();
		
		final TIntArrayList moveInts = new TIntArrayList(legalMoves.size());
		
		// TODO could speed up this method by implementing an auto-sorting
		// class that extends TIntArrayList
		for (final Move move : legalMoves)
		{
			final int toAdd = game.moveToInt(move);
			if (!moveInts.contains(toAdd))
				moveInts.add(toAdd);
		}
		
		moveInts.sort();
		return moveInts.toArray();
	}
	
	/**
	 * @return Array with a length equal to the number of legal moves in current state.
	 * Every element is an int array of size 3, containing [channel_idx, x, y] for 
	 * that move. This is used for action representation in Polygames.
	 */
	public int[][] legalMovesTensors()
	{
		final FastArrayList<Move> moves = game.game.moves(context).moves();
		final int[][] movesTensors;
		
		if (moves.isEmpty())
		{
			movesTensors = new int[1][];
			movesTensors[0] = new int[] {game.MOVE_PASS_CHANNEL_IDX, 0, 0};
		}
		else
		{
			movesTensors = new int[moves.size()][];

			for (int i = 0; i < moves.size(); ++i)
			{
				movesTensors[i] = game.moveToTensor(moves.get(i));
			}
		}

		return movesTensors;
	}
	
	/**
	 * Estimates a reward for a given player (assumed 0-index player) based on one
	 * or more random rollouts from the current state.
	 * 
	 * @param player
	 * @param numRollouts
	 * @param playoutCap Max number of random actions we'll select in playout
	 * @return Estimated reward
	 */
	public double getRandomRolloutsReward(final int player, final int numRollouts, final int playoutCap)
	{
		double sumRewards = 0.0;
		
		for (int i = 0; i < numRollouts; ++i)
		{
			final TempContext copyContext = new TempContext(context);
			game.game.playout(copyContext, null, 0.1f, null, 0, playoutCap, ThreadLocalRandom.current());
			final double[] returns = RankUtils.agentUtilities(copyContext);
			sumRewards += returns[player + 1];
		}
		
		return sumRewards / numRollouts;
	}
	
	/**
	 * NOTE: the returns are for the original player indices at the start of the episode;
	 * this can be different from player-to-colour assignments in the current game state
	 * if the Swap rule was used.
	 * 
	 * @return Array of utilities in [-1, 1] for all players. Player
	 * index assumed to be 0-based!
	 */
	public double[] returns()
	{
		if (!isTerminal())
			return new double[game.numPlayers()];
		
		final double[] returns = RankUtils.agentUtilities(context);
		return Arrays.copyOfRange(returns, 1, returns.length);
	}
	
	/**
	 * NOTE: the returns are for the original player indices at the start of the episode;
	 * this can be different from player-to-colour assignments in the current game state
	 * if the Swap rule was used.
	 * 
	 * @param player
	 * @return The returns for given player (index assumed to be 0-based!). Returns
	 * are always in [-1, 1]
	 */
	public double returns(final int player)
	{
		if (!isTerminal())
			return 0.0;
		
		final double[] returns = RankUtils.agentUtilities(context);
		return returns[player + 1];
	}
	
	/**
	 * Undo the last move.
	 * 
	 * NOTE: implementation is NOT efficient. It restarts to the initial game
	 * state, and re-applies all moves except for the last one
	 */
	public void undoLastMove()
	{
		final List<Move> moves = context.trial().generateCompleteMovesList();
		reset();
		
		for (int i = context.trial().numInitialPlacementMoves(); i < moves.size() - 1; ++i)
		{
			game.game.apply(context, moves.get(i));
		}
	}
	
	/**
	 * @return A flat, 1D array tensor representation of the current game state
	 */
	public float[] toTensorFlat()
	{
		// TODO we also want to support edges and faces for some games
		
		final Container[] containers = game.game.equipment().containers();
		final int numPlayers = game.game.players().count();
		final int numPieceTypes = game.game.equipment().components().length - 1;
		final boolean stacking = game.game.isStacking();
		final boolean usesCount = game.game.requiresCount();
		final boolean usesAmount = game.game.requiresBet();
		final boolean usesState = game.game.requiresLocalState();
		final boolean usesSwap = game.game.metaRules().usesSwapRule();
		
		final int[] xCoords = game.tensorCoordsX();
		final int[] yCoords = game.tensorCoordsY();
		final int tensorDimX = game.tensorDimX();
		final int tensorDimY = game.tensorDimY();
		
		final int numChannels = game.stateTensorNumChannels;
		
		final float[] flatTensor = new float[numChannels * tensorDimX * tensorDimY];
		
		int currentChannel = 0;
		
		if (!stacking)
		{
			// Just one channel per piece type
			final Owned owned = context.state().owned();
			
			for (int e = 1; e <= numPieceTypes; ++e)
			{
				for (int p = 1; p <= numPlayers + 1; ++p)
				{
					final TIntArrayList sites = owned.sites(p, e);
					
					for (int i = 0; i < sites.size(); ++i)
					{
						final int site = sites.getQuick(i);
						flatTensor[yCoords[site] + tensorDimY * (xCoords[site] + (currentChannel * tensorDimX))] = 1.f;
					}
				}
				
				++currentChannel;
			}
		}
		else
		{
			// We have to deal with stacking
			for (int c = 0; c < containers.length; ++c)
			{
				final Container cont = containers[c];
				final BaseContainerStateStacking cs = (BaseContainerStateStacking) context.state().containerStates()[c];
				final int contStartSite = game.game.equipment().sitesFrom()[c];
				
				for (int site = 0; site < cont.numSites(); ++site)
				{
					final int stackSize = cs.sizeStackCell(contStartSite + site);
					
					if (stackSize > 0)
					{
						// Store in channels for bottom 5 elements of stack
						for (int i = 0; i < LudiiGameWrapper.NUM_STACK_CHANNELS / 2; ++i)
						{
							if (i >= stackSize)
								break;
							
							final int what = cs.whatCell(contStartSite + site, i);
							final int channel = currentChannel + ((what - 1) * LudiiGameWrapper.NUM_STACK_CHANNELS + i);
							flatTensor[yCoords[contStartSite + site] + tensorDimY * (xCoords[contStartSite + site] + (channel * tensorDimX))] = 1.f;
						}
						
						// And same for top 5 elements of stack
						for (int i = 0; i < LudiiGameWrapper.NUM_STACK_CHANNELS / 2; ++i)
						{
							if (i >= stackSize)
								break;
							
							final int what = cs.whatCell(contStartSite + site, stackSize - 1 - i);
							final int channel = 
									currentChannel + ((what - 1) * LudiiGameWrapper.NUM_STACK_CHANNELS + 
											(LudiiGameWrapper.NUM_STACK_CHANNELS / 2) + i);
							flatTensor[yCoords[contStartSite + site] + tensorDimY * (xCoords[contStartSite + site] + (channel * tensorDimX))] = 1.f;
						}
						
						// Finally a non-binary channel storing the height of stack
						final int channel = currentChannel + LudiiGameWrapper.NUM_STACK_CHANNELS * numPieceTypes;
						flatTensor[yCoords[contStartSite + site] + tensorDimY * (xCoords[contStartSite + site] + (channel * tensorDimX))] = stackSize;
					}
				}
			}
			
			// + 1 for stack size channel
			currentChannel += LudiiGameWrapper.NUM_STACK_CHANNELS * numPieceTypes + 1;
		}
		
		if (usesCount)
		{
			// non-binary channel for counts
			for (int c = 0; c < containers.length; ++c)
			{
				final Container cont = containers[c];
				final ContainerState cs = context.state().containerStates()[c];
				final int contStartSite = game.game.equipment().sitesFrom()[c];
				
				for (int site = 0; site < cont.numSites(); ++site)
				{
					flatTensor[yCoords[contStartSite + site] + tensorDimY * (xCoords[contStartSite + site] + (currentChannel * tensorDimX))] = 
							cs.countCell(contStartSite + site);
				}
			}
			
			++currentChannel;
		}
		
		if (usesAmount)
		{
			// One channel per player for their amount
			for (int p = 1; p <= numPlayers; ++p)
			{
				final int amount = context.state().amount(p);
				final int startFill = tensorDimY * currentChannel * tensorDimX;
				final int endFill = startFill + (tensorDimY * tensorDimX);
				Arrays.fill(flatTensor, startFill, endFill, amount);
				
				++currentChannel;
			}
		}
		
		if (numPlayers > 1)
		{
			// One binary channel per player for whether or not they're current mover
			// (one will be all-1s, all the others will be all-0)
			// Takes into account swap rule!
			final int mover = context.state().playerToAgent(context.state().mover());
			final int startFill = tensorDimY * (currentChannel + mover - 1) * tensorDimX;
			System.arraycopy(game.allOnesChannelFlat(), 0, flatTensor, startFill, (tensorDimY * tensorDimX));

			currentChannel += numPlayers;
		}
		
		if (usesState)
		{
			// Channels for local state: 0, 1, 2, 3, 4, or >= 5
			for (int c = 0; c < containers.length; ++c)
			{
				final Container cont = containers[c];
				final int contStartSite = game.game.equipment().sitesFrom()[c];
				final ContainerState cs = context.state().containerStates()[c];
				
				for (int site = 0; site < cont.numSites(); ++site)
				{
					final int state = Math.min(cs.stateCell(contStartSite + site), LudiiGameWrapper.NUM_LOCAL_STATE_CHANNELS - 1);
					flatTensor[yCoords[contStartSite + site] + tensorDimY * (xCoords[contStartSite + site] + ((currentChannel + state) * tensorDimX))] = 1.f;
				}
			}
			
			currentChannel += LudiiGameWrapper.NUM_LOCAL_STATE_CHANNELS;
		}
		
		if (usesSwap)
		{
			// Channel for whether or not swap occurred
			if (context.state().orderHasChanged())
			{
				final int startFill = tensorDimY * currentChannel * tensorDimX;
				System.arraycopy(game.allOnesChannelFlat(), 0, flatTensor, startFill, (tensorDimY * tensorDimX));
			}
			
			currentChannel += 1;
		}
		
		// Channels for whether or not positions exist in containers
		final int startFill = tensorDimY * currentChannel * tensorDimX;
		System.arraycopy(game.containerPositionChannels(), 0, flatTensor, startFill, (containers.length * tensorDimY * tensorDimX));
		currentChannel += containers.length;
		
		// Channels marking from and to of last Move
		final List<Move> trialMoves = trial.generateCompleteMovesList();
		if (trialMoves.size() - trial.numInitialPlacementMoves() > 0)
		{
			final Move lastMove = trialMoves.get(trialMoves.size() - 1);
			final int from = lastMove.fromNonDecision();
			
			if (from != Constants.OFF)
				flatTensor[yCoords[from] + tensorDimY * (xCoords[from] + (currentChannel * tensorDimX))] = 1.f;
			
			++currentChannel;
			final int to = lastMove.toNonDecision();
			
			if (to != Constants.OFF)
				flatTensor[yCoords[to] + tensorDimY * (xCoords[to] + (currentChannel * tensorDimX))] = 1.f;
			
			++currentChannel;
		}
		else
		{
			currentChannel += 2;
		}
		
		// And the same for move before last move
		if (trialMoves.size() - trial.numInitialPlacementMoves() > 1)
		{
			final Move lastLastMove = trialMoves.get(trialMoves.size() - 2);
			final int from = lastLastMove.fromNonDecision();
			
			if (from != Constants.OFF)
				flatTensor[yCoords[from] + tensorDimY * (xCoords[from] + (currentChannel * tensorDimX))] = 1.f;
			
			++currentChannel;
			final int to = lastLastMove.toNonDecision();
			
			if (to != Constants.OFF)
				flatTensor[yCoords[to] + tensorDimY * (xCoords[to] + (currentChannel * tensorDimX))] = 1.f;
			
			++currentChannel;
		}
		else
		{
			currentChannel += 2;
		}
		
		// Assert that we correctly ran through all channels
		assert (currentChannel == numChannels);
		
		return flatTensor;
	}
	
	/**
	 * @return A single (3D) tensor representation of the current game state
	 */
	public float[][][] toTensor()
	{
		final int tensorDimX = game.tensorDimX();
		final int tensorDimY = game.tensorDimY();
		final int numChannels = game.stateTensorNumChannels;
		
		final float[] flatTensor = toTensorFlat();
		final float[][][] tensor = new float[numChannels][tensorDimX][tensorDimY];
		
		for (int c = 0; c < numChannels; ++c)
		{
			for (int x = 0; x < tensorDimX; ++x)
			{
				System.arraycopy(flatTensor, tensorDimY * (x + (c * tensorDimX)), tensor[c][x], 0, tensorDimY);
			}
		}
		
		return tensor;
	}
	
	/**
	 * @return The wrapped trial object
	 */
	public Trial trial()
	{
		return trial;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		final State state = context.state();
		
		sb.append("BEGIN LUDII STATE\n");
		
		sb.append("Mover colour = " + state.mover() + "\n");
		sb.append("Mover player/agent = " + state.playerToAgent(state.mover()) + "\n");
		sb.append("Next = " + state.next() + "\n");
		sb.append("Previous = " + state.prev() + "\n");
		
		for (int p = 1; p <= state.numPlayers(); ++p)
		{
			sb.append("Player " + p + " active = " + context.active(p) + "\n");
		}
		
		sb.append("State hash = " + state.stateHash() + "\n");
		
		if (game.game.requiresScore())
		{
			for (int p = 1; p <= state.numPlayers(); ++p)
			{
				sb.append("Player " + p + " score = " + context.score(p) + "\n");
			}
		}
		
		for (int p = 1; p <= state.numPlayers(); ++p)
		{
			sb.append("Player " + p + " ranking = " + context.trial().ranking()[p] + "\n");
		}
		
		for (int i = 0; i < state.containerStates().length; ++i)
		{
			final ContainerState cs = state.containerStates()[i];
			sb.append("BEGIN CONTAINER STATE " + i + "\n");
			
			sb.append(cs.toString() + "\n");
			
			sb.append("END CONTAINER STATE " + i + "\n");
		}
		
		sb.append("END LUDII GAME STATE\n");
		
		return sb.toString();
	}

}
