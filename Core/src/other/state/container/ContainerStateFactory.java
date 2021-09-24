package other.state.container;

import game.Game;
import game.equipment.container.Container;
import main.Constants;
import main.collections.ChunkStack;
import other.context.Context;
import other.state.puzzle.ContainerDeductionPuzzleState;
import other.state.puzzle.ContainerDeductionPuzzleStateLarge;
import other.state.stacking.ContainerGraphStateStacks;
import other.state.stacking.ContainerStateStacksLarge;
import other.state.stacking.ContainerStateStacks;
import other.state.zhash.ZobristHashGenerator;
import other.trial.Trial;

/**
 * Factory pattern
 * @author mrraow
 */
public class ContainerStateFactory 
{
	/**
	 * @param generator
	 * @param game
	 * @param container
	 * 
	 * @return The correct container state.
	 */
	public static final ContainerState createStateForContainer(final ZobristHashGenerator generator, final Game game, final Container container)
	{
		final int containerSites = container.numSites();

		final int maxWhatValComponents = game.numComponents();
		final int maxWhatValNumPlayers = game.players().count();

		final int maxStateValMaximalLocal = 2 + game.maximalLocalStates();

		final int maxPieces = game.maxCount();
		final int maxCountValMaxPieces = (maxPieces == 0) ? 1 : maxPieces;

		final boolean requiresStack = game.isStacking();
		final boolean requiresCard = game.hasCard();
		final boolean requiresCount = game.requiresCount();
		final boolean requiresState = game.requiresLocalState();
		final boolean requiresRotation = game.requiresRotation();
		final boolean requiresPuzzle = game.isDeductionPuzzle();
		final boolean requiresIndices = game.requiresItemIndices();
		final boolean requiresPieceValue = game.requiresPieceValue();

		final int numChunks = containerSites;
		int maxWhatVal = Constants.UNDEFINED;
		int maxStateVal = Constants.UNDEFINED;
		int maxRotationVal = Constants.UNDEFINED;
		int maxCountVal = Constants.UNDEFINED;
		final int maxPieceValue = (requiresPieceValue) ? game.maximalValue() : Constants.UNDEFINED;
		
		if (requiresPuzzle)
			return constructPuzzle(generator, game, container);

		// Special case for cards game
		if (requiresCard)
			return new ContainerStateStacksLarge(generator, game, container, ChunkStack.TYPE_INDEX_STATE);
		
		if (!container.isHand() && game.isGraphGame() && requiresStack)
			return new ContainerGraphStateStacks(generator, game, container, ChunkStack.TYPE_INDEX_STATE);

		// Special case for the hands		
		if (container.isHand())
		{
			if (requiresStack)
				return constructStack(generator, game, container, requiresState, requiresPieceValue, requiresIndices);

			maxWhatVal = maxWhatValComponents;
			
			if (requiresCount) 
				maxCountVal = maxCountValMaxPieces;
			
			maxStateVal = maxStateValMaximalLocal;

			if (requiresRotation)
				maxRotationVal = game.maximalRotationStates();

			if (game.isGraphGame())
				return new ContainerGraphState(generator, game, container, maxWhatVal, maxStateVal,
						maxCountVal, maxRotationVal, maxPieceValue);

			return new ContainerFlatState(generator, game, container, numChunks, maxWhatVal, maxStateVal, maxCountVal,
					maxRotationVal, maxPieceValue);
		}

		// Special case for stacking
		if (requiresStack)
			return constructStack(generator, game, container, requiresState, requiresPieceValue, requiresIndices);

		// Complex sizing recreating previous processing, but probably wrong... No no, good!
		maxCountVal = requiresCount ? maxCountValMaxPieces : -1;
		maxWhatVal = requiresIndices ? maxWhatValComponents : maxWhatValNumPlayers;
		
		if (!requiresCount && !requiresIndices && !requiresState && !game.isGraphGame())
			maxWhatVal = -1;
		
		if (requiresState) 
			maxStateVal = maxStateValMaximalLocal;
		
		if (requiresRotation)
			maxRotationVal = game.maximalRotationStates();
		
		if (game.isGraphGame())
		{
			if (game.isVertexGame() && !game.isCellGame() && !game.isEdgeGame())
			{
				return new ContainerFlatVertexState(generator, game, container, maxWhatVal, maxStateVal, maxCountVal,
						maxRotationVal, maxPieceValue);
			}

			if (!game.isVertexGame() && !game.isCellGame() && game.isEdgeGame())
			{
				return new ContainerFlatEdgeState(generator, game, container, maxWhatVal, maxStateVal, maxCountVal,
						maxRotationVal, maxPieceValue);
			}

			return new ContainerGraphState(generator, game, container, maxWhatVal, maxStateVal, maxCountVal,
					maxRotationVal, maxPieceValue);
		}

		return new ContainerFlatState(generator, game, container, numChunks, maxWhatVal, maxStateVal, maxCountVal,
				maxRotationVal, maxPieceValue);
	}

	/**
	 * @param generator
	 * @param game
	 * @param container
	 * @param requiresState
	 * @param requiresIndices
	 * @param requiresValue
	 * @return A container state for stack.
	 */
	private static ContainerState constructStack(final ZobristHashGenerator generator, final Game game, final Container container,
			final boolean requiresState, final boolean requiresIndices, final boolean requiresValue)
	{
		if (!container.isHand() && !requiresIndices && !requiresState && !requiresValue)
			return new ContainerStateStacks(generator, game, container, ChunkStack.TYPE_PLAYER_STATE);

		return new ContainerStateStacks(generator, game, container, ChunkStack.TYPE_INDEX_STATE);
	}

	/**
	 * @param generator
	 * @param game
	 * @param container
	 * @return A container state for puzzles.
	 */
	private static ContainerState constructPuzzle(final ZobristHashGenerator generator, final Game game,
			final Container container) 
	{
		final int numComponents = game.numComponents();
		final int nbValuesEdge;
		final int nbValuesVertex;

		if (game.isDeductionPuzzle()) // not useful but just to avoid an error on the parsing in worse case.
			nbValuesEdge = game.board().edgeRange().max(new Context(game, new Trial(game)))
					- game.board().edgeRange().min(new Context(game, new Trial(game))) + 1;
		else
			nbValuesEdge = 1;


		if (game.isDeductionPuzzle()) // not useful but just to avoid an error on the parsing in worse case.
			nbValuesVertex = game.board().cellRange().max(new Context(game, new Trial(game)))
					- game.board().cellRange().min(new Context(game, new Trial(game))) + 1;
		else
			nbValuesVertex = 1;

		if ((numComponents + 1) > 31 || nbValuesEdge > 31 || nbValuesVertex > 31)
			return new ContainerDeductionPuzzleStateLarge(generator, game, container);
		else
			return new ContainerDeductionPuzzleState(generator, game, container);
	}
}
