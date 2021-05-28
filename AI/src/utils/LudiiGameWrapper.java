package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import game.Game;
import game.equipment.component.Component;
import game.equipment.container.Container;
import game.types.state.GameType;
import main.Constants;
import main.FileHandling;
import main.math.MathRoutines;
import metrics.support.zhang_shasha.Tree;
import other.GameLoader;
import other.action.Action;
import other.action.others.ActionPropose;
import other.action.others.ActionVote;
import other.move.Move;
import other.topology.TopologyElement;
import utils.data_structures.ludeme_trees.LudemeTreeUtils;

/**
 * Wrapper around a Ludii game, with various extra methods required for
 * other frameworks that like to wrap around Ludii (e.g. OpenSpiel, Polygames)
 * 
 * @author Dennis Soemers
 */
public final class LudiiGameWrapper
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Wrapper class for keys used to index into map of already-compiled 
	 * game wrappers.
	 *
	 * @author Dennis Soemers
	 */
	private static class GameWrapperCacheKey
	{
		private final String gameName;
		private final List<String> options;
		
		/**
		 * Constructor
		 * @param gameName
		 * @param options
		 */
		public GameWrapperCacheKey(final String gameName, final List<String> options)
		{
			this.gameName = gameName;
			this.options = options;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((gameName == null) ? 0 : gameName.hashCode());
			result = prime * result + ((options == null) ? 0 : options.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
				return true;

			if (obj == null)
				return false;

			if (!(obj instanceof GameWrapperCacheKey))
				return false;
			
			final GameWrapperCacheKey other = (GameWrapperCacheKey) obj;
			return (gameName.equals(other.gameName) && options.equals(other.options));
		}
	}
	
	/** Cache of already-instantiated LudiiGameWrapper objects */
	private static Map<GameWrapperCacheKey, LudiiGameWrapper> gameWrappersCache = 
			new HashMap<GameWrapperCacheKey, LudiiGameWrapper>();
	
	//-------------------------------------------------------------------------
	
	/** x- and/or y-coordinates that differ by at most this amount are considered equal */
	protected static final double EPSILON = 0.00001;
	
	/** Number of channels we use for stacks (just 1 for not-stacking-games) */
	protected static final int NUM_STACK_CHANNELS = 10;
	
	/** Number of channels for local state per site (in games that have local state per site) */
	protected static final int NUM_LOCAL_STATE_CHANNELS = 6;
	
	/** Maximum distance we consider between from and to x/y coords for move channels */
	protected static final int DEFAULT_MOVE_TENSOR_DIST_CLIP = 3;
	
	/** Clipping variable for levelMin / levelMax terms in move channel index computation */
	protected static final int MOVE_TENSOR_LEVEL_CLIP = 2;
	
	//-------------------------------------------------------------------------
	
	/** Our game object */
	protected final Game game;
	
	/** X-coordinates in state tensors for all sites in game */
	protected int[] xCoords;
	
	/** Y-coordinates in state tensors for all sites in game */
	protected int[] yCoords;
	
	/** X-dimension for state tensors */
	protected int tensorDimX;
	
	/** Y-dimension for state tensors */
	protected int tensorDimY;
	
	/** Number of channels we need for state tensors */
	protected int stateTensorNumChannels;
	
	/** Array of names for the channels in state tensors */
	protected String[] stateTensorChannelNames;
	
	/** Maximum absolute distance we consider between from and to positions for move tensors */
	protected final int moveTensorDistClip;
	
	/** Channel index for the first proposition channel in move-tensor-representation */
	protected int FIRST_PROPOSITION_CHANNEL_IDX;
	
	/** Channel index for the first vote channel in move-tensor-representation */
	protected int FIRST_VOTE_CHANNEL_IDX;
	
	/** Channel index for Pass move in move-tensor-representation */
	protected int MOVE_PASS_CHANNEL_IDX;
	
	/** Channel index for Swap move in move-tensor-representation */
	protected int MOVE_SWAP_CHANNEL_IDX;
	
	/** A flat version of a complete channel of only 1s */
	protected float[] ALL_ONES_CHANNEL_FLAT;
	
	/** 
	 * A flat version of multiple concatenated channels (one per container), 
	 * indicating whether or not positions exist in containers 
	 */
	protected float[] CONTAINER_POSITION_CHANNELS;
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param gameName
	 * @return Returns LudiiGameWrapper for game name (default options)
	 */
	public synchronized static LudiiGameWrapper construct(final String gameName)
	{
		final GameWrapperCacheKey key = new GameWrapperCacheKey(gameName, new ArrayList<String>());
		LudiiGameWrapper wrapper = gameWrappersCache.get(key);
		
		if (wrapper == null)
		{
			wrapper = new LudiiGameWrapper(GameLoader.loadGameFromName(gameName));
			gameWrappersCache.put(key, wrapper);
		}
		
		return wrapper;
	}
	
	/**
	 * @param gameName
	 * @param gameOptions
	 * @return Returns LudiiGameWrapper for give game name and options
	 */
	public static LudiiGameWrapper construct(final String gameName, final String... gameOptions)
	{
		final GameWrapperCacheKey key = new GameWrapperCacheKey(gameName, Arrays.asList(gameOptions));
		LudiiGameWrapper wrapper = gameWrappersCache.get(key);
		
		if (wrapper == null)
		{
			wrapper = new LudiiGameWrapper(GameLoader.loadGameFromName(gameName, Arrays.asList(gameOptions)));
			gameWrappersCache.put(key, wrapper);
		}
		
		return wrapper;
	}
	
	/**
	 * @param file
	 * @return Returns LudiiGameWrapper for .lud file
	 */
	public static LudiiGameWrapper construct(final File file)
	{
		final Game game = GameLoader.loadGameFromFile(file);
		return new LudiiGameWrapper(game);
	}
	
	/**
	 * @param file
	 * @param gameOptions
	 * @return Returns LudiiGameWrapper for .lud file with game options
	 */
	public static LudiiGameWrapper construct(final File file, final String... gameOptions)
	{
		final Game game = GameLoader.loadGameFromFile(file, Arrays.asList(gameOptions));
		return new LudiiGameWrapper(game);
	}
	
	/**
	 * Constructor for already-instantiated game.
	 * @param game
	 */
	public LudiiGameWrapper(final Game game)
	{
		this.game = game;
		
		if ((game.gameFlags() & GameType.UsesFromPositions) == 0L)
			moveTensorDistClip = 0;		// No from-positions in any moves in this game
		else
			moveTensorDistClip = DEFAULT_MOVE_TENSOR_DIST_CLIP;
		
		computeTensorCoords();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The version of Ludii that we're using (a String in 
	 * "x.y.z" format).
	 */
	public static String ludiiVersion()
	{
		return Constants.LUDEME_VERSION;
	}
	
	/**
	 * @return True if and only if the game is a simultaneous-move game
	 */
	public boolean isSimultaneousMoveGame()
	{
		return !game.isAlternatingMoveGame();
	}
	
	/**
	 * @return True if and only if the game is a stochastic game
	 */
	public boolean isStochasticGame()
	{
		return game.isStochasticGame();
	}
	
	/**
	 * @return True if and only if the game is an imperfect-information game
	 */
	public boolean isImperfectInformationGame()
	{
		return game.hiddenInformation();
	}
	
	/**
	 * @return Game's name
	 */
	public String name()
	{
		return game.name();
	}
	
	/**
	 * @return Number of players
	 */
	public int numPlayers()
	{
		return game.players().count();
	}
	
	/**
	 * @return X coordinates in state tensors for all sites
	 */
	public int[] tensorCoordsX()
	{
		return xCoords;
	}
	
	/**
	 * @return Y coordinates in state tensors for all sites
	 */
	public int[] tensorCoordsY()
	{
		return yCoords;
	}
	
	/**
	 * @return Size of x-dimension for state tensors
	 */
	public int tensorDimX()
	{
		return tensorDimX;
	}
	
	/**
	 * @return Size of y-dimension for state tensors
	 */
	public int tensorDimY()
	{
		return tensorDimY;
	}
	
	/**
	 * @return Shape of tensors for moves: [numChannels, size(x dimension), size(y dimension)]
	 */
	public int[] moveTensorsShape()
	{
		return new int[] {MOVE_SWAP_CHANNEL_IDX + 1, tensorDimX(), tensorDimY()};
	}
	
	/**
	 * @return Shape of tensors for states: [numChannels, size(x dimension), size(y dimension)]
	 */
	public int[] stateTensorsShape()
	{
		return new int[] {stateTensorNumChannels, tensorDimX(), tensorDimY()};
	}
	
	/**
	 * @return Array of names for all the channels in our state tensors
	 */
	public String[] stateTensorChannelNames()
	{
		return stateTensorChannelNames;
	}
	
	/**
	 * @param move
	 * @return A tensor representation of given move (shape = [3])
	 */
	public int[] moveToTensor(final Move move)
	{
		if (move.isPropose())
		{
			int offset = 0;
			
			for (final Action a : move.actions())
			{
				if (a instanceof ActionPropose && a.isDecision())
				{
					final ActionPropose action = (ActionPropose) a;
					offset = action.propositionInt();
					break;
				}
			}
			
			return new int[] {FIRST_PROPOSITION_CHANNEL_IDX + offset, 0, 0};
		}
		else if (move.isVote())
		{
			int offset = 0;
			
			for (final Action a : move.actions())
			{
				if (a instanceof ActionVote && a.isDecision())
				{
					final ActionVote action = (ActionVote) a;
					offset = action.voteInt();
					break;
				}
			}
			
			return new int[] {FIRST_VOTE_CHANNEL_IDX + offset, 0, 0};
		}
		else if (move.isPass())
		{
			return new int[] {MOVE_PASS_CHANNEL_IDX, 0, 0};
		}
		else if (move.isSwap())
		{
			return new int[] {MOVE_SWAP_CHANNEL_IDX, 0, 0};
		}
		else if (move.isOtherMove())
		{
			//  TODO stop treating these all as passes
			return new int[] {MOVE_PASS_CHANNEL_IDX, 0, 0};
		}
		else
		{
			final int from = move.fromNonDecision();
			final int to = move.toNonDecision();
			final int levelMin = move.levelMinNonDecision();
			final int levelMax = move.levelMaxNonDecision();

			assert (to >= 0);

			final int fromX = from != Constants.OFF ? xCoords[from] : -1;
			final int fromY = from != Constants.OFF ? yCoords[from] : -1;
			final int toX = xCoords[to];
			final int toY = yCoords[to];

			final int diffX = from != Constants.OFF ? toX - fromX : 0;
			final int diffY = from != Constants.OFF ? toY - fromY : 0;

			int channelIdx = MathRoutines.clip(
					diffX, 
					-moveTensorDistClip, 
					moveTensorDistClip) + moveTensorDistClip;

			channelIdx *= (moveTensorDistClip * 2 + 1);
			channelIdx += MathRoutines.clip(
					diffY, 
					-moveTensorDistClip, 
					moveTensorDistClip) + moveTensorDistClip;

			if (game.isStacking())
			{
				channelIdx *= (LudiiGameWrapper.MOVE_TENSOR_LEVEL_CLIP + 1);
				channelIdx += MathRoutines.clip(levelMin, 0, LudiiGameWrapper.MOVE_TENSOR_LEVEL_CLIP);

				channelIdx *= (LudiiGameWrapper.MOVE_TENSOR_LEVEL_CLIP + 1);
				channelIdx += MathRoutines.clip(levelMax - levelMin, 0, LudiiGameWrapper.MOVE_TENSOR_LEVEL_CLIP);
			}

			return new int[] {channelIdx, toX, toY};
		}
	}
	
	/**
	 * @param moveTensor
	 * @return A single int representation of a move (converted from its tensor representation)
	 */
	public int moveTensorToInt(final int[] moveTensor)
	{
		final int[] moveTensorsShape = moveTensorsShape();
		return moveTensorsShape[1] * moveTensorsShape[2] * moveTensor[0] + 
				moveTensorsShape[2] * moveTensor[1] + 
				moveTensor[2];
	}
	
	/**
	 * @param move
	 * @return A single int representation of a move
	 */
	public int moveToInt(final Move move)
	{
		return moveTensorToInt(moveToTensor(move));
	}
	
	/**
	 * @return Number of distinct actions that we can represent in our tensor-based
	 * 	representations for this game.
	 */
	public int numDistinctActions()
	{
		final int[] moveTensorsShape = moveTensorsShape();
		return moveTensorsShape[1] * moveTensorsShape[2] * moveTensorsShape[3];
	}
	
	/**
	 * @return Max duration of game (measured in moves)
	 */
	public int maxGameLength()
	{
		return game.getMaxMoveLimit();
	}
	
	/**
	 * @return A flat representation of a channel fully filled with only 1s.
	 */
	public float[] allOnesChannelFlat()
	{
		return ALL_ONES_CHANNEL_FLAT;
	}
	
	/**
	 * @return A flat version of multiple concatenated channels (one per container), 
	 * indicating whether or not positions exist in containers 
	 */
	public float[] containerPositionChannels()
	{
		return CONTAINER_POSITION_CHANNELS;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Computes x and y coordinates in state tensors for all sites in the game.
	 */
	private void computeTensorCoords()
	{
		if (game.hasSubgames())
		{
			System.err.println("Computing tensors for Matches is not yet supported.");
			return;
		}
		
		final Container[] containers = game.equipment().containers();
		final List<? extends TopologyElement> graphElements = game.graphPlayElements();
		xCoords = new int[game.equipment().totalDefaultSites()];
		yCoords = new int[game.equipment().totalDefaultSites()];
		final int numBoardSites = graphElements.size();
		
		// first sort by X, to find x indices for vertices
		final List<? extends TopologyElement> sortedGraphElements = 
				new ArrayList<TopologyElement>(graphElements);
		sortedGraphElements.sort(new Comparator<TopologyElement>() 
		{

			@Override
			public int compare(final TopologyElement o1, final TopologyElement o2)
			{
				if (o1.centroid().getX() < o2.centroid().getX())
					return -1;
				else if (o1.centroid().getX() == o2.centroid().getX())
					return 0;
				else
					return 1;
			}
			
		});
		
		int currIdx = 0;
		double currXPos = sortedGraphElements.get(0).centroid().getX();
		for (final TopologyElement e : sortedGraphElements)
		{			
			final double xPos = e.centroid().getX();
			if (xPos - EPSILON > currXPos)
			{
				++currIdx;
				currXPos = xPos;
			}
			
			xCoords[e.index()] = currIdx;
		}
		
		final int maxBoardIndexX = currIdx;
		
		// now the same, but for y indices
		sortedGraphElements.sort(new Comparator<TopologyElement>() 
		{

			@Override
			public int compare(final TopologyElement o1, final TopologyElement o2)
			{
				if (o1.centroid().getY() < o2.centroid().getY())
					return -1;
				else if (o1.centroid().getY() == o2.centroid().getY())
					return 0;
				else
					return 1;
			}
			
		});
		
		currIdx = 0;
		double currYPos = sortedGraphElements.get(0).centroid().getY();
		for (final TopologyElement e : sortedGraphElements)
		{
			final double yPos = e.centroid().getY();
			if (yPos - EPSILON > currYPos)
			{
				++currIdx;
				currYPos = yPos;
			}
			
			yCoords[e.index()] = currIdx;
		}
		
		final int maxBoardIndexY = currIdx;
		
		tensorDimX = maxBoardIndexX + 1;
		tensorDimY = maxBoardIndexY + 1;
		
		// Maybe need to extend the board a bit for hands / other containers
		final int numContainers = game.numContainers();
		
		if (numContainers > 1)
		{
			int maxNonBoardContIdx = -1;
			for (int c = 1; c < numContainers; ++c)
			{
				maxNonBoardContIdx = Math.max(containers[c].numSites() - 1, maxNonBoardContIdx);
			}
			
			boolean handsAsRows = false;
			if (maxBoardIndexX < maxBoardIndexY && maxNonBoardContIdx <= maxBoardIndexX)
				handsAsRows = true;
			else if (maxNonBoardContIdx > maxBoardIndexX && maxBoardIndexX > maxBoardIndexY)
				handsAsRows = true;
			
			if (handsAsRows)
			{
				// We paste hands as extra rows for the board
				tensorDimY += 1;	// a dummy row to split board from other containers
				tensorDimY += (numContainers - 1);	// one extra row per container
				
				if (maxNonBoardContIdx > maxBoardIndexX)
				{
					// Hand rows are longer than the board's rows, so need extra cols too
					tensorDimX += (maxNonBoardContIdx - maxBoardIndexX);
				}
				
				// Compute coordinates for all vertices in extra containers
				int nextContStartIdx = numBoardSites;
				
				for (int c = 1; c < numContainers; ++c)
				{
					final Container cont = containers[c];
					
					for (int site = 0; site < cont.numSites(); ++site)
					{
						xCoords[site + nextContStartIdx] = site;
						yCoords[site + nextContStartIdx] = maxBoardIndexY + 1 + c;
					}
					
					nextContStartIdx += cont.numSites();
				}
			}
			else
			{
				// We paste hands as extra cols for the board
				tensorDimX += 1;	// a dummy col to split board from other containers
				tensorDimX += (numContainers - 1);	// one extra col per container
				
				if (maxNonBoardContIdx > maxBoardIndexY)
				{
					// Hand cols are longer than the board's cols, so need extra rows too
					tensorDimY += (maxNonBoardContIdx - maxBoardIndexY);
				}
				
				// Compute coordinates for all cells in extra containers
				for (int c = 1; c < numContainers; ++c)
				{
					final Container cont = containers[c];
					int nextContStartIdx = numBoardSites;
					
					for (int site = 0; site < cont.numSites(); ++site)
					{
						xCoords[site + nextContStartIdx] = maxBoardIndexX + 1 + c;
						yCoords[site + nextContStartIdx] = site;
					}
					
					nextContStartIdx += cont.numSites();
				}
			}
		}
		
		final Component[] components = game.equipment().components();
		final int numPlayers = game.players().count();
		final int numPieceTypes = components.length - 1;
		final boolean stacking = game.isStacking();
		final boolean usesCount = game.requiresCount();
		final boolean usesAmount = game.requiresBet();
		final boolean usesState = game.requiresLocalState();
		final boolean usesSwap = game.metaRules().usesSwapRule();
		
		final List<String> channelNames = new ArrayList<String>();
		
		// Number of channels required for piece types
		stateTensorNumChannels = stacking ? NUM_STACK_CHANNELS * numPieceTypes : numPieceTypes;
		
		if (!stacking)
		{
			for (int e = 1; e <= numPieceTypes; ++e)
			{
				channelNames.add("Piece Type " + e + " (" + components[e].name() + ")");
			}
		}
		else
		{
			for (int e = 1; e <= numPieceTypes; ++e)
			{
				for (int i = 0; i < NUM_STACK_CHANNELS / 2; ++i)
				{
					channelNames.add("Piece Type " + e + " (" + components[e].name() + ") at level " + i + " from stack bottom.");
				}
				
				for (int i = 0; i < NUM_STACK_CHANNELS / 2; ++i)
				{
					channelNames.add("Piece Type " + e + " (" + components[e].name() + ") at level " + i + " from stack top.");
				}
			}
		}
		
		if (stacking)
		{
			stateTensorNumChannels += 1;	// one more channel for size of stack
			channelNames.add("Stack sizes (non-binary channel!)");
		}
		
		if (usesCount)
		{
			stateTensorNumChannels += 1;	// channel for count
			channelNames.add("Counts (non-binary channel!)");
		}
		
		if (usesAmount)
		{
			stateTensorNumChannels += numPlayers;	// channel for amount
			
			for (int p = 1; p <= numPlayers; ++p)
			{
				channelNames.add("Amount for Player " + p);
			}
		}
		
		if (numPlayers > 1)
		{
			stateTensorNumChannels += numPlayers;	// channels for current mover
			
			for (int p = 1; p <= numPlayers; ++p)
			{
				channelNames.add("Is Player " + p + " the current mover?");
			}
		}
		
		if (usesState)
		{
			stateTensorNumChannels += NUM_LOCAL_STATE_CHANNELS;
			
			for (int i = 0; i < NUM_LOCAL_STATE_CHANNELS; ++i)
			{
				if (i + 1 == NUM_LOCAL_STATE_CHANNELS)
					channelNames.add("Local state >= " + i);
				else
					channelNames.add("Local state == " + i);
			}
		}
		
		if (usesSwap)
		{
			stateTensorNumChannels += 1;
			channelNames.add("Did Swap Occur?");
		}
		
		stateTensorNumChannels += numContainers;	// for maps of whether positions exist in containers
		
		for (int c = 0; c < numContainers; ++c)
		{
			channelNames.add("Does position exist in container " + c + " (" + containers[c].name() + ")?");
		}
		
		stateTensorNumChannels += 4;	// channels for last move and move before last move (from and to)
		
		channelNames.add("Last move's from-position");
		channelNames.add("Last move's to-position");
		channelNames.add("Second-to-last move's from-position");
		channelNames.add("Second-to-last move's to-position");
		
		assert (channelNames.size() == stateTensorNumChannels);
		stateTensorChannelNames = channelNames.toArray(new String[stateTensorNumChannels]);
		
		final int firstAuxilChannelIdx = computeFirstAuxilChannelIdx();
		
		if (game.usesVote())
		{
			FIRST_PROPOSITION_CHANNEL_IDX = firstAuxilChannelIdx;
			FIRST_VOTE_CHANNEL_IDX = FIRST_PROPOSITION_CHANNEL_IDX + game.numVoteStrings();
			
			MOVE_PASS_CHANNEL_IDX = FIRST_VOTE_CHANNEL_IDX + game.numVoteStrings();
		}
		else
		{
			MOVE_PASS_CHANNEL_IDX = firstAuxilChannelIdx;
		}
		
		MOVE_SWAP_CHANNEL_IDX = MOVE_PASS_CHANNEL_IDX + 1;
		
		ALL_ONES_CHANNEL_FLAT = new float[tensorDimX * tensorDimY];
		Arrays.fill(ALL_ONES_CHANNEL_FLAT, 1.f);
		
		CONTAINER_POSITION_CHANNELS = new float[containers.length * tensorDimX * tensorDimY];
		for (int c = 0; c < containers.length; ++c)
		{
			final Container cont = containers[c];
			final int contStartSite = game.equipment().sitesFrom()[c];
			
			for (int site = 0; site < cont.numSites(); ++site)
			{
				CONTAINER_POSITION_CHANNELS[yCoords[contStartSite + site] + tensorDimY * (xCoords[contStartSite + site] + (c * tensorDimX))] = 1.f;
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return First channel index for auxiliary in move-tensor-representation
	 */
	private int computeFirstAuxilChannelIdx()
	{
		// legal values for diff x = {-clip, ..., -2, -1, 0, 1, 2, ..., +clip}
		final int numValsDiffX = 2 * moveTensorDistClip + 1;

		// legal values for diff y = {-clip, ..., -2, -1, 0, 1, 2, ..., +clip} (mult with diff x)
		final int numValsDiffY = numValsDiffX * (2 * moveTensorDistClip + 1);

		if (!game.isStacking())
		{
			return numValsDiffY;
		}
		else
		{
			// legal values for clipped levelMin = {0, 1, 2, ..., clip} (mult with all the above)
			final int numValsLevelMin = numValsDiffY * (MOVE_TENSOR_LEVEL_CLIP + 1);

			// legal values for clipped levelMax - levelMin = {0, 1, 2, ..., clip} (mult with all the above)
			final int numValsLevelMax = numValsLevelMin * (MOVE_TENSOR_LEVEL_CLIP + 1);

			// The max index using variables mentioned above is 1 less than the number of values
			// we computed, since we start at 0.
			// So, the number we just computed can be used as the next index (for Pass moves)
			return numValsLevelMax;
		}
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Computes and returns array of indices of source channels that we should
	 * transfer from, for move tensors. At index i of the returned array, we
	 * have the index of the move tensor channel that we should transfer from
	 * in the source domain. 
	 * 
	 * This array should be of length equal to the
	 * number of channels in this game.
	 * 
	 * If the source game does not contain any matching channels for our
	 * i'th channel, we have a value of -1 in the returned array.
	 * 
	 * @param sourceGame
	 * @return Indices of source channels we should transfer from
	 */
	public int[] moveTensorSourceChannels(final LudiiGameWrapper sourceGame)
	{
		final int[] sourceChannelIndices = new int[moveTensorsShape()[0]];
		
		for (int targetChannel = 0; targetChannel < sourceChannelIndices.length; ++targetChannel)
		{
			if (targetChannel == MOVE_PASS_CHANNEL_IDX)
			{
				sourceChannelIndices[targetChannel] = sourceGame.MOVE_PASS_CHANNEL_IDX;
			}
			else if (targetChannel == MOVE_SWAP_CHANNEL_IDX)
			{
				sourceChannelIndices[targetChannel] = sourceGame.MOVE_SWAP_CHANNEL_IDX;
			}
			else
			{
				// TODO not handling stacking games yet in these cases
				
				if ((game.gameFlags() & GameType.UsesFromPositions) == 0L)
				{
					// Target domain is placement game
					if ((sourceGame.game.gameFlags() & GameType.UsesFromPositions) == 0L)
					{
						// Source domain is placement game
						sourceChannelIndices[targetChannel] = targetChannel;
					}
					else
					{
						// Source domain is movement game
						//
						// We can't properly make every different movement channel from source
						// domain map to the single placement channel
						// So, we'll be more strict and instead only map the dx = dy = 0 movement
						// channel to the target placement channel, leaving all other source movement
						// channels unused.
						if (targetChannel != 0)
							throw new UnsupportedOperationException("LudiiGameWrapper::moveTensorSourceChannels() expected targetChannel == 0!");
						
						int channelIdx = MathRoutines.clip(
								0, 
								-sourceGame.moveTensorDistClip, 
								sourceGame.moveTensorDistClip) + sourceGame.moveTensorDistClip;
			
						channelIdx *= (sourceGame.moveTensorDistClip * 2 + 1);
						channelIdx += MathRoutines.clip(
								0, 
								-sourceGame.moveTensorDistClip, 
								sourceGame.moveTensorDistClip) + sourceGame.moveTensorDistClip;
						
						sourceChannelIndices[targetChannel] = channelIdx;
					}
				}
				else
				{
					// Target domain is movement game
					if ((sourceGame.game.gameFlags() & GameType.UsesFromPositions) == 0L)
					{
						// Source domain is placement game
						// 
						// We'll transfer the 0 channel (= placement channel) to all the different
						// movement channels
						sourceChannelIndices[targetChannel] = 0;
					}
					else
					{
						// Source domain is movement game
						sourceChannelIndices[targetChannel] = targetChannel;
					}
				}
			}
		}
		
		return sourceChannelIndices;
	}
	
	/**
	 * Computes and returns array of indices of source channels that we should
	 * transfer from, for state tensors. At index i of the returned array, we
	 * have the index of the state tensor channel that we should transfer from
	 * in the source domain. 
	 * 
	 * This array should be of length equal to the
	 * number of channels in this game.
	 * 
	 * If the source game does not contain any matching channels for our
	 * i'th channel, we have a value of -1 in the returned array.
	 * 
	 * @param sourceGame
	 * @return Indices of source channels we should transfer from
	 */
	public int[] stateTensorSourceChannels(final LudiiGameWrapper sourceGame)
	{
		final String[] sourceChannelNames = sourceGame.stateTensorChannelNames();
		final int[] sourceChannelIndices = new int[stateTensorsShape()[0]];
		
		final Component[] targetComps = game.equipment().components();
		final Component[] sourceComps = sourceGame.game.equipment().components();
		
		for (int targetChannel = 0; targetChannel < sourceChannelIndices.length; ++targetChannel)
		{
			final String targetChannelName = stateTensorChannelNames[targetChannel];
			
			if (targetChannelName.startsWith("Piece Type "))
			{
				if (targetChannelName.endsWith(" from stack bottom.") || targetChannelName.endsWith(" from stack top."))
				{
					// TODO handle stacking games
					throw new UnsupportedOperationException("Stacking games not yet handled by stateTensorSourceChannels()!");
				}
				else
				{
					final int pieceType = Integer.parseInt(
							targetChannelName.substring("Piece Type ".length()).split(Pattern.quote(" "))[0]);
					
					final Component targetPiece = targetComps[pieceType];
					final String targetPieceName = targetPiece.name();
					final int owner = targetPiece.owner();
					int bestMatch = -1;
					
					// First try to find a source piece with same name and same owner
					for (int i = 1; i < sourceComps.length; ++i)
					{
						final Component sourcePiece = sourceComps[i];
						if (sourcePiece.owner() == owner && sourcePiece.name().equals(targetPieceName))
						{
							bestMatch = i;
							break;
						}
					}
					
					if (bestMatch == -1)
					{
						// Try to find a source piece with similar ludeme tree
						final Tree ludemeTree = LudemeTreeUtils.buildLudemeZhangShashaTree(targetPiece.generator());
						int lowestDist = Integer.MAX_VALUE;
						
						for (int i = 1; i < sourceComps.length; ++i)
						{
							final Component sourcePiece = sourceComps[i];
							if (sourcePiece.owner() == owner)
							{
								final Tree otherTree = LudemeTreeUtils.buildLudemeZhangShashaTree(sourcePiece.generator());
								final int treeEditDist = Tree.ZhangShasha(ludemeTree, otherTree);
								
								if (treeEditDist < lowestDist)
								{
									lowestDist = treeEditDist;
									bestMatch = i;
								}
							}
						}
					}
					
					if (bestMatch >= 0)
					{
						int sourceChannelIdx = -1;
						for (int i = 0; i < sourceChannelNames.length; ++i)
						{
							if (sourceChannelNames[i].equals("Piece Type " + bestMatch + " (" + sourceComps[bestMatch].name() + ")"))
							{
								sourceChannelIdx = i;
								break;
							}
						}
						sourceChannelIndices[targetChannel] = sourceChannelIdx;
					}
					else
					{
						sourceChannelIndices[targetChannel] = -1;
					}
				}
			}
			else if (targetChannelName.startsWith("Does position exist in container "))
			{
				final int containerIdx = Integer.parseInt(
							targetChannelName.substring("Does position exist in container ".length()).split(Pattern.quote(" "))[0]);
				
				// For now we just search for container with same index, since usually we're consistent in how we
				// order the containers in different games
				
				// TODO can probably do something smarter later. Like maybe looking at the ratio of sites that
				// a container has relative to the number of sites across entire game?
				
				int idx = -1;
				for (int i = 0; i < sourceChannelNames.length; ++i)
				{
					final String sourceChannelName = sourceChannelNames[i];
					if (sourceChannelName.startsWith("Does position exist in container "))
					{
						final int sourceContainerIdx = Integer.parseInt(
								sourceChannelName.substring("Does position exist in container ".length()).split(Pattern.quote(" "))[0]);
						
						if (containerIdx == sourceContainerIdx)
						{
							idx = i;
							break;
						}
					}
				}
				
				if (idx >= 0)
				{
					sourceChannelIndices[targetChannel] = idx;
				}
				else
				{
					sourceChannelIndices[targetChannel] = -1;
				}
			}
			else if 
			(
				targetChannelName.equals("Stack sizes (non-binary channel!)")										||
				targetChannelName.equals("Counts (non-binary channel!)")											||
				targetChannelName.startsWith("Amount for Player ")													||
				(targetChannelName.startsWith("Is Player ") && targetChannelName.endsWith(" the current mover?")) 	||
				targetChannelName.startsWith("Local state >= ")														||
				targetChannelName.startsWith("Local state == ")														||
				targetChannelName.startsWith("Did Swap Occur?")														||
				targetChannelName.startsWith("Last move's from-position")											||
				targetChannelName.startsWith("Last move's to-position")												||
				targetChannelName.startsWith("Second-to-last move's from-position")									||
				targetChannelName.startsWith("Second-to-last move's to-position")														
			)
			{
				sourceChannelIndices[targetChannel] = identicalChannelIdx(targetChannelName, sourceChannelNames);
			}
			else
			{
				throw new UnsupportedOperationException("stateTensorSourceChannels() does not recognise channel name: " + targetChannelName);
			}
		}
		
		return sourceChannelIndices;
	}
	
	/**
	 * @param targetChannel
	 * @param sourceChannels
	 * @return Index of source channel that is identical to given target channel.
	 * 	Returns -1 if no identical source channel exists.
	 */
	private static int identicalChannelIdx(final String targetChannel, final String[] sourceChannels)
	{
		int idx = -1;
		for (int i = 0; i < sourceChannels.length; ++i)
		{
			if (sourceChannels[i].equals(targetChannel))
			{
				idx = i;
				break;
			}
		}
		return idx;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Main method prints some relevant information for these wrappers
	 * @param args
	 */
	public static void main(final String[] args)
	{
		final String[] gameNames = FileHandling.listGames();
		
		for (final String name : gameNames)
		{
			if (name.replaceAll(Pattern.quote("\\"), "/").contains("/wip/"))
				continue;
			
			if (name.replaceAll(Pattern.quote("\\"), "/").contains("/wishlist/"))
				continue;
			
			if (name.replaceAll(Pattern.quote("\\"), "/").contains("/test/"))
				continue;
			
			if (name.replaceAll(Pattern.quote("\\"), "/").contains("/bad_playout/"))
				continue;
			
			if (name.replaceAll(Pattern.quote("\\"), "/").contains("/bad/"))
				continue;
			
			if (name.replaceAll(Pattern.quote("\\"), "/").contains("/plex/"))
				continue;
			
			if (name.replaceAll(Pattern.quote("\\"), "/").contains("/math/graph/"))
				continue;

			System.out.println("name = " + name);
			final LudiiGameWrapper game = LudiiGameWrapper.construct(name);
			
			if (!game.game.hasSubgames())
			{
				System.out.println("State tensor shape = " + Arrays.toString(game.stateTensorsShape()));
				System.out.println("Moves tensor shape = " + Arrays.toString(game.moveTensorsShape()));
			}
		}
	}

}
