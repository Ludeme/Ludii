package other.state.stacking;

import java.util.Arrays;

import game.Game;
import game.equipment.container.Container;
import game.types.board.SiteType;
import game.types.state.GameType;
import main.Constants;
import main.collections.ChunkSet;
import other.state.State;
import other.state.zhash.HashedBitSet;
import other.state.zhash.HashedChunkStack;
import other.state.zhash.ZobristHashGenerator;
import other.state.zhash.ZobristHashUtilities;
import other.topology.Cell;

/**
 * Global State for a stacking container item.
 * 
 * @author Eric.Piette
 */
public class ContainerStateStacks extends BaseContainerStateStacking
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Type of Item on the stack on a site. */
	private final HashedChunkStack[] chunkStacks;

	/** Playable sites (for boardless games). */
	private final HashedBitSet playable;

	private final long[][][] chunkStacksWhatHash;
	private final long[][][] chunkStacksWhoHash;
	private final long[][][] chunkStacksStateHash;
	private final long[][][] chunkStacksRotationHash;
	private final long[][][] chunkStacksValueHash;
	private final long[][][] chunkStacksHiddenHash;
	private final long[][][] chunkStacksHiddenWhatHash;
	private final long[][][] chunkStacksHiddenWhoHash;
	private final long[][][] chunkStacksHiddenStateHash;
	private final long[][][] chunkStacksHiddenRotationHash;
	private final long[][][] chunkStacksHiddenValueHash;
	private final long[][][] chunkStacksHiddenCountHash;
	private final long[][] chunkStacksSizeHash;
	
	/** The type of stack. */
	protected final int type;
	
	/** The number of components. */
	public final int numComponents;

	/** The number of players. */
	public final int numPlayers;

	/** The number of states. */
	public final int numStates;

	/** The number of rotations. */
	public final int numRotation;
	
	/** The number of piece values. */
	public final int numValues;

	/** True if the state involves hidden info. */
	private final boolean hiddenInfo;

	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 * @param generator 
	 * @param game 
	 * @param container 
	 * @param type 
	 */
	public ContainerStateStacks
	(
		final ZobristHashGenerator generator, 
		final Game game, 
		final Container container, 
		final int type
	)
	{
		super
		(
			game, 
			container, 
			container.numSites()
		);

		final int numSites = container.topology().cells().size();

		chunkStacks = new HashedChunkStack[numSites];
		
		this.numComponents = game.numComponents();
		this.numPlayers = game.players().count();
		this.numStates = game.maximalLocalStates();
		this.numRotation = game.maximalRotationStates();
		this.numValues = Constants.MAX_VALUE_PIECE;
		
		final int maxValWhat = numComponents;
		final int maxValWho = numPlayers + 1;
		final int maxValState = numStates;
		final int maxValRotation = numRotation;
		final int maxValues = numValues;

		chunkStacksWhatHash =  ZobristHashUtilities.getSequence(generator, numSites, Constants.MAX_STACK_HEIGHT, maxValWhat + 1);
		chunkStacksWhoHash =  ZobristHashUtilities.getSequence(generator, numSites, Constants.MAX_STACK_HEIGHT, maxValWho + 1);
		chunkStacksStateHash =  ZobristHashUtilities.getSequence(generator, numSites, Constants.MAX_STACK_HEIGHT, maxValState + 1);
		chunkStacksRotationHash = ZobristHashUtilities.getSequence(generator, numSites, Constants.MAX_STACK_HEIGHT, maxValRotation + 1);
		chunkStacksValueHash = ZobristHashUtilities.getSequence(generator, numSites, Constants.MAX_STACK_HEIGHT, maxValues + 1);
		chunkStacksSizeHash =  ZobristHashUtilities.getSequence(generator, numSites, Constants.MAX_STACK_HEIGHT);
		
		this.type = type;
		
		if ((game.gameFlags() & GameType.HiddenInfo) == 0L)
		{
			chunkStacksHiddenHash = null;
			chunkStacksHiddenWhatHash = null;
			chunkStacksHiddenWhoHash = null;
			chunkStacksHiddenStateHash = null;
			chunkStacksHiddenRotationHash = null;
			chunkStacksHiddenValueHash = null;
			chunkStacksHiddenCountHash = null;
			hiddenInfo = false;
		}
		else
		{
			chunkStacksHiddenHash = ZobristHashUtilities.getSequence(generator, numSites, Constants.MAX_STACK_HEIGHT, 2);
			chunkStacksHiddenWhatHash = ZobristHashUtilities.getSequence(generator, numSites, Constants.MAX_STACK_HEIGHT, 2);
			chunkStacksHiddenWhoHash = ZobristHashUtilities.getSequence(generator, numSites, Constants.MAX_STACK_HEIGHT, 2);
			chunkStacksHiddenStateHash = ZobristHashUtilities.getSequence(generator, numSites, Constants.MAX_STACK_HEIGHT, 2);
			chunkStacksHiddenRotationHash = ZobristHashUtilities.getSequence(generator, numSites, Constants.MAX_STACK_HEIGHT, 2);
			chunkStacksHiddenValueHash = ZobristHashUtilities.getSequence(generator, numSites, Constants.MAX_STACK_HEIGHT, 2);
			chunkStacksHiddenCountHash = ZobristHashUtilities.getSequence(generator, numSites, Constants.MAX_STACK_HEIGHT, 2);
			hiddenInfo = true;
		}

		if (game.isBoardless() && container.index() == 0)
		{
			playable = new HashedBitSet(generator, numSites);
		}
		else
		{
			playable = null;
		}
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 */
	public ContainerStateStacks(final ContainerStateStacks other)
	{
		super(other);
		
		this.numComponents = other.numComponents;
		this.numPlayers    = other.numPlayers;
		this.numStates     = other.numStates;
		this.numRotation   = other.numRotation;
		this.numValues = other.numValues;

		playable = (other.playable == null) ? null : other.playable.clone();

		if (other.chunkStacks == null)
		{
			chunkStacks = null;
		}
		else
		{
			chunkStacks = new HashedChunkStack[other.chunkStacks.length];
			
			for (int i = 0; i < other.chunkStacks.length; ++i)
			{
				final HashedChunkStack otherChunkStack = other.chunkStacks[i];
				
				if (otherChunkStack != null)
				{
					chunkStacks[i] = otherChunkStack.clone();
				}
			}
		}
		
		chunkStacksWhatHash = other.chunkStacksWhatHash;
		chunkStacksWhoHash = other.chunkStacksWhoHash;
		chunkStacksStateHash = other.chunkStacksStateHash;
		chunkStacksRotationHash = other.chunkStacksRotationHash;
		chunkStacksValueHash = other.chunkStacksValueHash;
		chunkStacksSizeHash = other.chunkStacksSizeHash;
		type = other.type;
		hiddenInfo = other.hiddenInfo;

		if (other.chunkStacksHiddenHash == null)
		{
			chunkStacksHiddenHash = null;
			chunkStacksHiddenWhatHash = null;
			chunkStacksHiddenWhoHash = null;
			chunkStacksHiddenStateHash = null;
			chunkStacksHiddenRotationHash = null;
			chunkStacksHiddenValueHash = null;
			chunkStacksHiddenCountHash = null;
		}
		else
		{
			chunkStacksHiddenHash = other.chunkStacksWhatHash;
			chunkStacksHiddenWhatHash = other.chunkStacksHiddenWhatHash;
			chunkStacksHiddenWhoHash = other.chunkStacksHiddenWhoHash;
			chunkStacksHiddenStateHash = other.chunkStacksHiddenStateHash;
			chunkStacksHiddenRotationHash = other.chunkStacksHiddenRotationHash;
			chunkStacksHiddenValueHash = other.chunkStacksHiddenValueHash;
			chunkStacksHiddenCountHash = other.chunkStacksHiddenCountHash;
		}
	}

	//-------------------------------------------------------------------------

	@Override
	protected long calcCanonicalHash(final int[] siteRemap, final int[] edgeRemap, final int[] vertexRemap, final int[] playerRemap, final boolean whoOnly) 
	{
		long hash = 0;
		if (offset != 0) return 0; // Not the board!
		
		for (int pos = 0; pos < chunkStacks.length && pos < siteRemap.length; pos++)
		{
			final int newPos = siteRemap[pos];
			if (chunkStacks[pos] == null) continue;
			
			hash ^= chunkStacks[pos].remapHashTo (
					chunkStacksWhatHash[newPos],
					chunkStacksWhoHash[newPos],
					chunkStacksStateHash[newPos],
					chunkStacksRotationHash[newPos],
					chunkStacksValueHash[newPos],
					chunkStacksSizeHash[newPos],
					whoOnly);
		}

		return hash;
	}

	//-------------------------------------------------------------------------

	@Override
	public void reset(final State trialState, final Game game)
	{
		super.reset(trialState, game);
		for (final HashedChunkStack set : chunkStacks)
		{
			if (set == null) continue;
			trialState.updateStateHash(set.calcHash());
		}
		Arrays.fill(chunkStacks, null);
	}

	private void verifyPresent(final int site) 
	{
		if (chunkStacks[site - offset] != null) 
			return;
		
		chunkStacks[site - offset] = new HashedChunkStack(
				numComponents, numPlayers, numStates, numRotation, numValues,
				type, hiddenInfo,
				chunkStacksWhatHash[site - offset],
				chunkStacksWhoHash[site - offset],
				chunkStacksStateHash[site - offset],
				chunkStacksRotationHash[site - offset],
				chunkStacksValueHash[site - offset],
				chunkStacksSizeHash[site - offset]);
	}

	//-------------------------------------------------------------------------

	@Override
	public void addItem(final State trialState, final int site, final int what, final int who, final Game game) 
	{
		verifyPresent(site);
		chunkStacks[site - offset].incrementSize(trialState);
		chunkStacks[site - offset].setWhat(trialState, what);
		chunkStacks[site - offset].setWho(trialState, who);

		if (playable != null)
		{
			setPlayable(trialState, site - offset, false);

			final Cell cell = container().topology().cells().get(site);
			for (final Cell vNbors : cell.adjacent())
				if (!isOccupied(vNbors.index()))
					setPlayable(trialState, vNbors.index(), true);
		}
	}

	@Override
	public void addItem(final State trialState, final int site, final int what, final int who, final Game game,
			final boolean[] hide, final boolean masked)
	{
		verifyPresent(site);
		chunkStacks[site - offset].incrementSize(trialState);
		chunkStacks[site - offset].setWhat(trialState, what);
		chunkStacks[site - offset].setWho(trialState, who);

		if (playable != null)
		{
			setPlayable(trialState, site, false);

			final Cell cell = container().topology().cells().get(site);
			for (final Cell vNbors : cell.adjacent())
				if (!isOccupied(vNbors.index()))
					setPlayable(trialState, vNbors.index(), true);
		}
	}

	@Override
	public void addItem(final State trialState, final int site, final int what, final int who, final int stateVal,
			final int rotationVal, final int value, final Game game)
	{
		verifyPresent(site);
		chunkStacks[site - offset].incrementSize(trialState);
		chunkStacks[site - offset].setWhat(trialState, what);
		chunkStacks[site - offset].setWho(trialState, who);
		chunkStacks[site - offset].setState(trialState, stateVal);
		chunkStacks[site - offset].setRotation(trialState, rotationVal);
		chunkStacks[site - offset].setValue(trialState, value);

		if (playable != null)
		{
			setPlayable(trialState, site, false);

			final Cell cell = container().topology().cells().get(site);
			for (final Cell vNbors : cell.adjacent())
				if (!isOccupied(vNbors.index()))
					setPlayable(trialState, vNbors.index(), true);
		}
	}
	
	@Override
	public void insert(State trialState, SiteType siteType, int site, int level, int whatItem, int whoItem,
			final int state, final int rotation, final int value, Game game)
	{
		if (siteType == null || siteType.equals(SiteType.Cell) || container().index() != 0)
			insertCell(trialState, site, level, whatItem, whoItem, state, rotation, value, game);
		else if (siteType.equals(SiteType.Edge))
			insertEdge(trialState, site, level, whatItem, whoItem, state, rotation, value, game);
		else
			insertVertex(trialState, site, level, whatItem, whoItem, state, rotation, value, game);
	}

	@Override
	public void insertCell(final State trialState, final int site, final int level, final int what, final int who,
			final int state, final int rotation, final int value,
			final Game game)
	{
		verifyPresent(site);
		final int size = chunkStacks[site - offset].size();

		final boolean wasEmpty = (size == 0);

		if (level == size)
		{
			chunkStacks[site - offset].incrementSize(trialState);
			chunkStacks[site - offset].setWhat(trialState, what);
			chunkStacks[site - offset].setWho(trialState, who);
			chunkStacks[site - offset].setState(trialState, (state == Constants.UNDEFINED ? 0 : state));
			chunkStacks[site - offset].setRotation(trialState, (rotation == Constants.UNDEFINED ? 0 : rotation));
			chunkStacks[site - offset].setValue(trialState, 0, (value == Constants.UNDEFINED ? 0 : value));
		}
		else if (level < size)
		{
			chunkStacks[site - offset].incrementSize(trialState);
			for (int i = size - 1; i >= level; i--)
			{
				final int whatLevel = chunkStacks[site - offset].what(i);
				chunkStacks[site - offset].setWhat(trialState, whatLevel, i + 1);

				final int whoLevel = chunkStacks[site - offset].who(i);
				chunkStacks[site - offset].setWho(trialState, whoLevel, i + 1);

				final int rotationLevel = chunkStacks[site - offset].rotation(i);
				chunkStacks[site - offset].setRotation(trialState, rotationLevel, i + 1);

				final int valueLevel = chunkStacks[site - offset].value(i);
				chunkStacks[site - offset].setValue(trialState, valueLevel, i + 1);

				final int stateLevel = chunkStacks[site - offset].state(i);
				chunkStacks[site - offset].setState(trialState, stateLevel, i + 1);
			}
			chunkStacks[site - offset].setWhat(trialState, what, level);
			chunkStacks[site - offset].setWho(trialState, who, level);
			chunkStacks[site - offset].setState(trialState, (state == Constants.UNDEFINED ? 0 : state), level);
			chunkStacks[site - offset].setRotation(trialState, (rotation == Constants.UNDEFINED ? 0 : rotation), level);
			chunkStacks[site - offset].setValue(trialState, (value == Constants.UNDEFINED ? 0 : value), level);
		}

		final boolean isEmpty = (chunkStacks[site - offset].size() == 0);

		if (wasEmpty == isEmpty)
			return;

		if (isEmpty)
			addToEmptyCell(site);
		else
			removeFromEmptyCell(site);
	}

	@Override
	public int whoCell(final int site) 
	{
		if (chunkStacks[site - offset] == null) 
			return 0;
		
		return chunkStacks[site - offset].who();
	}

	@Override
	public int whoCell(final int site, final int level)
	{
		if (chunkStacks[site - offset] == null) 
			return 0;
		
		return chunkStacks[site - offset].who(level);
	}

	@Override
	public int whatCell(final int site)
	{
		if (chunkStacks[site - offset] == null) 
			return 0;
		
		return chunkStacks[site - offset].what();
	}

	@Override
	public int whatCell(final int site, final int level)
	{
		if (chunkStacks[site - offset] == null) 
			return 0;
		
		return chunkStacks[site - offset].what(level);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void setSite(final State trialState, final int site, final int whoVal, final int whatVal, final int countVal,
			final int stateVal, final int rotationVal, final int valueVal, final SiteType type)
	{
		if (type == SiteType.Cell)
		{
			verifyPresent(site);

			final boolean wasEmpty = isEmpty(site, SiteType.Cell);

			if (whoVal != Constants.UNDEFINED)
				chunkStacks[site - offset].setWho(trialState, whoVal);
			if (whatVal != Constants.UNDEFINED)
				chunkStacks[site - offset].setWhat(trialState, whatVal);
			if (stateVal != Constants.UNDEFINED)
				chunkStacks[site - offset].setState(trialState, stateVal);
			if (rotationVal != Constants.UNDEFINED)
				chunkStacks[site - offset].setRotation(trialState, rotationVal);
			if (valueVal != Constants.UNDEFINED)
				chunkStacks[site - offset].setValue(trialState, valueVal);

			final boolean isEmpty = isEmpty(site, SiteType.Cell);

			if (wasEmpty == isEmpty)
				return;

			if (isEmpty)
			{
				addToEmptyCell(site);

				if (playable != null)
				{
					checkPlayable(trialState, site - offset);

					final Cell v = container().topology().cells().get(site - offset);
					for (final Cell vNbors : v.adjacent())
						checkPlayable(trialState, vNbors.index());
				}

			}
			else
			{
				removeFromEmptyCell(site - offset);

				if (playable != null)
				{
					setPlayable(trialState, site - offset, false);

					final Cell v = container().topology().cells().get(site - offset);
					for (final Cell vNbors : v.adjacent())
						if (!isOccupied(vNbors.index()))
							setPlayable(trialState, vNbors.index(), true);
				}
			}
		}
	}

	@Override
	public void setSite(final State trialState, final int site, final int level, final int whoVal, final int whatVal,
			final int countVal, final int stateVal, final int rotationVal, final int valueVal)
	{
		verifyPresent(site);

		final boolean wasEmpty = isEmpty(site, SiteType.Cell);
		
		if (whoVal != Constants.UNDEFINED) chunkStacks[site - offset].setWho(trialState, whoVal, level);
		if (whatVal != Constants.UNDEFINED) chunkStacks[site - offset].setWhat(trialState, whatVal, level);
		if (stateVal != Constants.UNDEFINED) chunkStacks[site - offset].setState(trialState, stateVal, level);
		if (rotationVal != Constants.UNDEFINED) chunkStacks[site - offset].setRotation(trialState, rotationVal, level);
		if (valueVal != Constants.UNDEFINED) chunkStacks[site - offset].setValue(trialState, valueVal, level);
		
		final boolean isEmpty = isEmpty(site, SiteType.Cell);
		if (wasEmpty == isEmpty) return;
		
		if (isEmpty) 
		{
			addToEmptyCell(site);
			
			if (playable != null) 
			{
				checkPlayable(trialState, site - offset);
				
				final Cell v = container().topology().cells().get(site - offset);
				for (final Cell vNbors : v.adjacent())
					checkPlayable(trialState, vNbors.index());
			}

		} 
		else 
		{
			removeFromEmptyCell(site - offset);
			
			if (playable != null) 
			{
				setPlayable(trialState, site - offset, false);

				final Cell v = container().topology().cells().get(site - offset);
				for (final Cell vNbors : v.adjacent())
					if (!isOccupied(vNbors.index()))
						setPlayable(trialState, vNbors.index(), true);
			}
		}
	}

	private void checkPlayable(final State trialState, final int site) 
	{
		if (isOccupied(site - offset)) 
		{
			setPlayable(trialState, site - offset, false);
			return;
		}
				
		final Cell v = container().topology().cells().get(site - offset);
		for (final Cell vNbors : v.adjacent())
			if (isOccupied(vNbors.index()))
			{
				setPlayable(trialState, site - offset, true);
				return;
			}
		
		setPlayable(trialState, site - offset, false);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isOccupied(final int site) 
	{
		return chunkStacks[site - offset] != null && chunkStacks[site - offset].what() != 0;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public int stateCell(final int site)
	{
		if (chunkStacks[site - offset] == null) 
			return 0;
		
		return chunkStacks[site - offset].state();
	}

	@Override
	public int stateCell(final int site, final int level)
	{
		if (chunkStacks[site - offset] == null) 
			return 0;
		
		return chunkStacks[site - offset].state(level);
	}
	
	@Override
	public int rotationCell(final int site)
	{
		if (chunkStacks[site - offset] == null)
			return 0;
		return chunkStacks[site - offset].rotation();
	}

	@Override
	public int rotationCell(final int site, final int level)
	{
		if (chunkStacks[site - offset] == null)
			return 0;
		return chunkStacks[site - offset].rotation(level);
	}

	@Override
	public int valueCell(final int site)
	{
		if (chunkStacks[site - offset] == null)
			return 0;
		return chunkStacks[site - offset].value();
	}

	@Override
	public int valueCell(final int site, final int level)
	{
		if (chunkStacks[site - offset] == null)
			return 0;
		return chunkStacks[site - offset].value(level);
	}

	@Override
	public int remove(final State state, final int site, final SiteType graphElement)
	{
		if (chunkStacks[site - offset] == null) 
			return 0;

		final int componentRemove = chunkStacks[site - offset].what();
		chunkStacks[site - offset].setWhat(state, 0);
		chunkStacks[site - offset].setWho(state, 0);
		chunkStacks[site - offset].setState(state, 0);
		chunkStacks[site - offset].setRotation(state, 0);
		chunkStacks[site - offset].setValue(state, 0);
		chunkStacks[site - offset].decrementSize(state);
		return componentRemove;
	}
	
	@Override
	public int remove(final State state, final int site, final int level, final SiteType graphElement)
	{
		if (chunkStacks[site - offset] == null)
			return 0;

		final int componentRemove = chunkStacks[site - offset].what(level);
		for (int i = level; i < sizeStackCell(site) - 1; i++)
		{
			chunkStacks[site - offset].setWhat(state, chunkStacks[site - offset].what(i + 1), i);
			chunkStacks[site - offset].setWho(state, chunkStacks[site - offset].who(i + 1), i);
			chunkStacks[site - offset].setState(state, chunkStacks[site - offset].state(i + 1), i);
			chunkStacks[site - offset].setRotation(state, chunkStacks[site - offset].rotation(i + 1), i);
			chunkStacks[site - offset].setValue(state, chunkStacks[site - offset].value(i + 1), i);
		}
		chunkStacks[site - offset].setWhat(state, 0);
		chunkStacks[site - offset].setWho(state, 0);
		chunkStacks[site - offset].decrementSize(state);
		return componentRemove;
	}

	@Override
	public int remove(final State state, final int site, final int level) 
	{
		if (chunkStacks[site - offset] == null) 
			return 0;

		final int componentRemove = chunkStacks[site - offset].what(level);
		for (int i = level; i < sizeStackCell(site) - 1; i++)
		{
			chunkStacks[site - offset].setWhat(state, chunkStacks[site - offset].what(i + 1), i);
			chunkStacks[site - offset].setWho(state, chunkStacks[site - offset].who(i + 1), i);
			chunkStacks[site - offset].setState(state, chunkStacks[site - offset].state(i + 1), i);
			chunkStacks[site - offset].setRotation(state, chunkStacks[site - offset].rotation(i + 1), i);
			chunkStacks[site - offset].setValue(state, chunkStacks[site - offset].value(i + 1), i);
		}
		chunkStacks[site - offset].setWhat(state, 0);
		chunkStacks[site - offset].setWho(state, 0);
		chunkStacks[site - offset].decrementSize(state);
		return componentRemove;
	}

	/**
	 * Remove a stack.
	 * @param state 
	 * @param site
	 */
	@Override
	public void removeStack(final State state, final int site) 
	{
		if (chunkStacks[site - offset] == null) 
			return;
		
		state.updateStateHash(chunkStacks[site - offset].calcHash());
		chunkStacks[site - offset] = null;
	}

	@Override
	public int countCell(final int site) 
	{
		return whoCell(site) == 0 ? 0 : 1;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param site
	 * @return Size of stack.
	 */
	@Override
	public int sizeStackCell(final int site) 
	{
		if (chunkStacks[site - offset] == null) 
			return 0;
		
		return chunkStacks[site - offset].size();
	}

	/**
	 * @param site
	 * @return Size of stack vertex.
	 */
	@Override
	public int sizeStackVertex(final int site)
	{
		return 0;
	}

	/**
	 * @param site
	 * @return Size of stack edge.
	 */
	@Override
	public int sizeStackEdge(final int site)
	{
		return 0;
	}

	//-------------------------------------------------------------------------

	@Override
	public ContainerStateStacks deepClone() 
	{
		return new ContainerStateStacks(this);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isHidden(final int player, final int site, final int level, final SiteType graphElementType)
	{
		if (!hiddenInfo)
			return false;

		return chunkStacks[site - offset].isHidden(player, site, level, graphElementType);
	}
	
	@Override
	public boolean isHiddenWhat(final int player, final int site, final int level, final SiteType graphElementType)
	{
		if (!hiddenInfo)
			return false;

		return chunkStacks[site - offset].isHiddenWhat(player, site, level, graphElementType);
	}
	
	@Override
	public boolean isHiddenWho(final int player, final int site, final int level, final SiteType graphElementType)
	{
		if (!hiddenInfo)
			return false;

		return chunkStacks[site - offset].isHiddenWho(player, site, level, graphElementType);
	}
	
	@Override
	public boolean isHiddenState(final int player, final int site, final int level, final SiteType graphElementType)
	{
		if (!hiddenInfo)
			return false;

		return chunkStacks[site - offset].isHiddenState(player, site, level, graphElementType);
	}
	
	@Override
	public boolean isHiddenRotation(final int player, final int site, final int level, final SiteType graphElementType)
	{
		if (!hiddenInfo)
			return false;

		return chunkStacks[site - offset].isHiddenRotation(player, site, level, graphElementType);
	}
	
	@Override
	public boolean isHiddenValue(final int player, final int site, final int level, final SiteType graphElementType)
	{
		if (!hiddenInfo)
			return false;

		return chunkStacks[site - offset].isHiddenValue(player, site, level, graphElementType);
	}
	
	@Override
	public boolean isHiddenCount(final int player, final int site, final int level, final SiteType graphElementType)
	{
		if (!hiddenInfo)
			return false;

		return chunkStacks[site - offset].isHiddenCount(player, site, level, graphElementType);
	}
	
	@Override
	public void setHidden(final State state, final int player, final int site, final int level, final SiteType graphElementType,
			final boolean on)
	{
		if (!hiddenInfo)
			return;

		chunkStacks[site - offset].setHidden(state, player, site, level, graphElementType, on);
	}
	
	@Override
	public void setHiddenWhat(final State state, final int player, final int site, final int level, final SiteType graphElementType,
			final boolean on)
	{
		if (!hiddenInfo)
			return;

		chunkStacks[site - offset].setHiddenWhat(state, player, site, level, graphElementType, on);
	}

	@Override
	public void setHiddenWho(final State state, final int player, final int site, final int level, final SiteType graphElementType,
			final boolean on)
	{
		if (!hiddenInfo)
			return;

		chunkStacks[site - offset].setHiddenWho(state, player, site, level, graphElementType, on);
	}

	@Override
	public void setHiddenState(final State state, final int player, final int site, final int level,
			final SiteType graphElementType, final boolean on)
	{
		if (!hiddenInfo)
			return;

		chunkStacks[site - offset].setHiddenState(state, player, site, level, graphElementType, on);
	}

	@Override
	public void setHiddenRotation(final State state, final int player, final int site, final int level,
			final SiteType graphElementType, final boolean on)
	{
		if (!hiddenInfo)
			return;

		chunkStacks[site - offset].setHiddenRotation(state, player, site, level, graphElementType, on);
	}

	@Override
	public void setHiddenValue(final State state, final int player, final int site, final int level,
			final SiteType graphElementType, final boolean on)
	{
		if (!hiddenInfo)
			return;

		chunkStacks[site - offset].setHiddenValue(state, player, site, level, graphElementType, on);
	}

	@Override
	public void setHiddenCount(final State state, final int player, final int site, final int level,
			final SiteType graphElementType, final boolean on)
	{
		if (!hiddenInfo)
			return;

		chunkStacks[site - offset].setHiddenCount(state, player, site, level, graphElementType, on);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isPlayable(final int site)
	{
		if (playable == null)
			throw new RuntimeException("Tried to access playable bitset in non-boardless game.");

		return playable.get(site - offset);
	}

	@Override
	public void setPlayable(final State trialState, final int site, final boolean on)
	{
		playable.set(trialState, site - offset, on);
	}

	@Override
	public int whoEdge(final int edge)
	{
		return 0;
	}

	@Override
	public int whoVertex(final int vertex)
	{
		return 0;
	}

	@Override
	public int whatEdge(int site)
	{
		return 0;
	}

	@Override
	public int countEdge(int site)
	{
		return 0;
	}

	@Override
	public int stateEdge(int site)
	{
		return 0;
	}

	@Override
	public int rotationEdge(int site)
	{
		return 0;
	}

	@Override
	public int valueEdge(int site)
	{
		return 0;
	}

	@Override
	public int whatVertex(int site)
	{
		return 0;
	}

	@Override
	public int countVertex(int site)
	{
		return 0;
	}

	@Override
	public int stateVertex(int site)
	{
		return 0;
	}

	@Override
	public int rotationVertex(int site)
	{
		return 0;
	}

	@Override
	public int valueVertex(int site)
	{
		return 0;
	}

	@Override
	public int whoVertex(int site, int level)
	{
		return 0;
	}

	@Override
	public int whatVertex(int site, int level)
	{
		return 0;
	}

	@Override
	public int stateVertex(int site, int level)
	{
		return 0;
	}

	@Override
	public int rotationVertex(int site, int level)
	{
		return 0;
	}

	@Override
	public int valueVertex(int site, int level)
	{
		return 0;
	}

	@Override
	public int whoEdge(int site, int level)
	{
		return 0;
	}

	@Override
	public int whatEdge(int site, int level)
	{
		return 0;
	}

	@Override
	public int stateEdge(int site, int level)
	{
		return 0;
	}

	@Override
	public int rotationEdge(int site, int level)
	{
		return 0;
	}

	@Override
	public int valueEdge(int site, int level)
	{
		return 0;
	}

	@Override
	public void addItemVertex(State trialState, int site, int whatValue, int whoId, Game game)
	{
		// Nothing todo.
	}

	@Override
	public void insertVertex(State trialState, int site, int level, int whatValue, int whoId, final int state,
			final int rotation, final int value, Game game)
	{
		// Nothing todo.
	}

	@Override
	public void addItemVertex(State trialState, int site, int whatValue, int whoId, int stateVal, int rotationVal,
			int valueVal,
			Game game)
	{
		// Nothing todo.
	}

	@Override
	public void addItemVertex(State trialState, int site, int whatValue, int whoId, Game game, boolean[] hiddenValues,
			boolean masked)
	{
		// Nothing todo.
	}

	@Override
	public void removeStackVertex(State trialState, int site)
	{
		// Nothing todo.
	}

	@Override
	public void addItemEdge(State trialState, int site, int whatValue, int whoId, Game game)
	{
		// Nothing todo.
	}

	@Override
	public void insertEdge(State trialState, int site, int level, int whatValue, int whoId, final int state,
			final int rotation, final int value, Game game)
	{
		// Nothing todo.
	}

	@Override
	public void addItemEdge(State trialState, int site, int whatValue, int whoId, int stateVal, int rotationVal,
			int valueVal,
			Game game)
	{
		// Nothing todo.
	}

	@Override
	public void addItemEdge(State trialState, int site, int whatValue, int whoId, Game game, boolean[] hiddenValues,
			boolean masked)
	{
		// Nothing todo.
	}

	@Override
	public void removeStackEdge(State trialState, int site)
	{
		// Nothing todo.
	}

	//-------------------------------------------------------------------------

	@Override
	public void setValueCell(final State trialState, final int site, final int valueVal)
	{
		// Nothing to do.
	}

	@Override
	public void setCount(final State trialState, final int site, final int countVal)
	{
		// Nothing to do.
	}
	
	//-------------------------------------------------------------------------
	
	@Override public ChunkSet emptyChunkSetCell() { throw new UnsupportedOperationException("TODO"); }
	@Override public ChunkSet emptyChunkSetVertex() { throw new UnsupportedOperationException("TODO"); }
	@Override public ChunkSet emptyChunkSetEdge() { throw new UnsupportedOperationException("TODO"); }
	@Override public int numChunksWhoVertex() { throw new UnsupportedOperationException("TODO"); }
	@Override public int numChunksWhoCell() { throw new UnsupportedOperationException("TODO"); }
	@Override public int numChunksWhoEdge() { throw new UnsupportedOperationException("TODO"); }
	@Override public int chunkSizeWhoVertex() { throw new UnsupportedOperationException("TODO"); }
	@Override public int chunkSizeWhoCell() { throw new UnsupportedOperationException("TODO"); }
	@Override public int chunkSizeWhoEdge() { throw new UnsupportedOperationException("TODO"); }
	@Override public int numChunksWhatVertex() { throw new UnsupportedOperationException("TODO"); }
	@Override public int numChunksWhatCell() { throw new UnsupportedOperationException("TODO"); }
	@Override public int numChunksWhatEdge() { throw new UnsupportedOperationException("TODO"); }
	@Override public int chunkSizeWhatVertex() { throw new UnsupportedOperationException("TODO"); }
	@Override public int chunkSizeWhatCell() { throw new UnsupportedOperationException("TODO"); }
	@Override public int chunkSizeWhatEdge() { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean matchesWhoVertex(ChunkSet mask, ChunkSet pattern) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean matchesWhoCell(ChunkSet mask, ChunkSet pattern) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean matchesWhoEdge(ChunkSet mask, ChunkSet pattern) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean matchesWhatVertex(ChunkSet mask, ChunkSet pattern) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean matchesWhatCell(ChunkSet mask, ChunkSet pattern) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean matchesWhatEdge(ChunkSet mask, ChunkSet pattern) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean matchesWhoVertex(final int wordIdx, final long mask, final long matchingWord) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean matchesWhoCell(final int wordIdx, final long mask, final long matchingWord) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean matchesWhoEdge(final int wordIdx, final long mask, final long matchingWord) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean matchesWhatVertex(final int wordIdx, final long mask, final long matchingWord) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean matchesWhatCell(final int wordIdx, final long mask, final long matchingWord) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean matchesWhatEdge(final int wordIdx, final long mask, final long matchingWord) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean violatesNotWhoVertex(ChunkSet mask, ChunkSet pattern) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean violatesNotWhoCell(ChunkSet mask, ChunkSet pattern) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean violatesNotWhoEdge(ChunkSet mask, ChunkSet pattern) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean violatesNotWhatVertex(ChunkSet mask, ChunkSet pattern) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean violatesNotWhatCell(ChunkSet mask, ChunkSet pattern) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean violatesNotWhatEdge(ChunkSet mask, ChunkSet pattern) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean violatesNotWhoVertex(ChunkSet mask, ChunkSet pattern, int startWord) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean violatesNotWhoCell(ChunkSet mask, ChunkSet pattern, int startWord) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean violatesNotWhoEdge(ChunkSet mask, ChunkSet pattern, int startWord) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean violatesNotWhatVertex(ChunkSet mask, ChunkSet pattern, int startWord) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean violatesNotWhatCell(ChunkSet mask, ChunkSet pattern, int startWord) { throw new UnsupportedOperationException("TODO"); }
	@Override public boolean violatesNotWhatEdge(ChunkSet mask, ChunkSet pattern, int startWord) { throw new UnsupportedOperationException("TODO"); }
	@Override public ChunkSet cloneWhoVertex() { throw new UnsupportedOperationException("TODO"); }
	@Override public ChunkSet cloneWhoCell() { throw new UnsupportedOperationException("TODO"); }
	@Override public ChunkSet cloneWhoEdge() { throw new UnsupportedOperationException("TODO"); }
	@Override public ChunkSet cloneWhatVertex() { throw new UnsupportedOperationException("TODO"); }
	@Override public ChunkSet cloneWhatCell() { throw new UnsupportedOperationException("TODO"); }
	@Override public ChunkSet cloneWhatEdge() { throw new UnsupportedOperationException("TODO"); }

}
