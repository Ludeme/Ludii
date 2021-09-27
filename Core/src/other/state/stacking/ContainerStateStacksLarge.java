package other.state.stacking;

import game.Game;
import game.equipment.container.Container;
import game.types.board.SiteType;
import main.Constants;
import main.collections.ChunkSet;
import main.collections.ListStack;
import other.state.State;
import other.state.zhash.ZobristHashGenerator;

/**
 * TODO: ZobristHashes Global State for a large stack container item.
 * 
 * @author Eric.Piette and mrraow
 */
public class ContainerStateStacksLarge extends BaseContainerStateStacking
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Type of Item on the stack on a site. */
	private ListStack[] listStacks;

	private final int type;
	
	//-------------------------------------------------------------------------

	/**
	 * @param generator
	 * @param game
	 * @param container
	 * @param type
	 */
	public ContainerStateStacksLarge(final ZobristHashGenerator generator, final Game game, final Container container, final int type)
	{
		super
		(
			game, 
			container, 
			container.numSites()
		);
		
		listStacks = new ListStack[container.numSites()];
		for (int i = 0 ; i < listStacks.length; i++)
		{
			listStacks[i] = new ListStack(type);
		}
		
		this.type = type;
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 */
	public ContainerStateStacksLarge(final ContainerStateStacksLarge other)
	{
		super(other);
		
		if (other.listStacks == null)
		{
			listStacks = null;
		}
		else
		{
			listStacks = new ListStack[other.listStacks.length];
			
			for(int i = 0 ; i < listStacks.length; i++)
			{
				listStacks[i] = new ListStack(other.listStacks[i]);
			}
		}
		
		type = other.type;
	}

	@Override
	public ContainerStateStacksLarge deepClone() 
	{
		return new ContainerStateStacksLarge(this);
	}

	//-------------------------------------------------------------------------

	@Override
	protected long calcCanonicalHash (final int[] siteRemap, final int[] edgeRemap, final int[] vertexRemap, final int[] playerRemap, final boolean whoOnly) 
	{
		return 0; // Cards do not support symmetry
	}

	//-------------------------------------------------------------------------

	@Override
	public void reset(final State trialState, final Game game)
	{
		super.reset(trialState, game);
		
		listStacks = new ListStack[game.equipment().totalDefaultSites()];
		for (int i = 0 ; i < listStacks.length; i++)
			listStacks[i] = new ListStack(type);
	}

	//-------------------------------------------------------------------------

	/**
	 * @param site
	 * @param what
	 * @param who
	 * @param game
	 */
	@Override
	public void addItem(final State trialState, final int site, final int what, final int who, final Game game) 
	{
//		System.out.println("Stack BEFORE is:");
//		System.out.println(listStacks[site]);

		listStacks[site - offset].incrementSize();
		listStacks[site - offset].setWhat(what);
		listStacks[site - offset].setWho(who);

//		final TIntArrayList hidden = new TIntArrayList();
//		hidden.add(0);
//		for (int i = 0; i < game.players().count(); i++)
//		{
//			hidden.add(1);
//		}
//		listStacks[site - offset].addHidden(hidden);

//		System.out.println("Stack After is:");
//		System.out.println(listStacks[site]);
//		System.out.println("********");
	}

	/**
	 * @param site
	 * @param what
	 * @param who
	 * @param game
	 */
	@Override
	public void addItem(final State trialState, final int site, final int what, final int who, final Game game,
			final boolean[] hidden, final boolean masked)
	{
//		System.out.println("Stack BEFORE is:");
//		System.out.println(listStacks[site]);

		listStacks[site - offset].incrementSize();
		listStacks[site - offset].setWhat(what);
		listStacks[site - offset].setWho(who);

//		final TIntArrayList hiddenList = new TIntArrayList();
//		hiddenList.add(0);
//		for (int i = 0; i < hidden.length; i++)
//		{
//			if (hidden[i])
//			{
//				if (masked)
//					hiddenList.add(Constants.MASK);
//				else
//					hiddenList.add(Constants.INVISIBLE);
//			}
//			else
//				hiddenList.add(Constants.VISIBLE);
//		}
//		listStacks[site - offset].addHidden(hiddenList);

//		System.out.println("Stack After is:");
//		System.out.println(listStacks[site]);
//		System.out.println("********");
	}

	@Override
	public void insert(State trialState, SiteType siteType, int site, int level, int whatItem, int whoItem, final int state,
			final int rotation, final int value, Game game)
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
//		System.out.println("Insertion on site " + site + " lvl " + level + " of " + what + " for player " + who);
//
//		System.out.println("--------------------");
//		System.out.println("Stack BEFORE is:");
//		System.out.println(listStacks[site]);
//		System.out.println("********");

		if (level < listStacks[site - offset].size())
		{
			listStacks[site - offset].insertWhat(what, level);
			listStacks[site - offset].insertWho(who, level);
//			final TIntArrayList hidden = new TIntArrayList();
//			hidden.add(0);
//			for (int i = 0; i < game.players().count(); i++)
//				hidden.add(Constants.INVISIBLE);
//			listStacks[site - offset].insertHidden(hidden, level);
			listStacks[site - offset].incrementSize();
		}
//		System.out.println("Stack after is:");
//		System.out.println(listStacks[site]);
//		System.out.println();
//
//		System.out.println("SIZE OF STACK IN " + site + " is " + listStacks[site].size());
	}

	/**
	 * @param site
	 * @param what
	 * @param who
	 * @param game
	 */
	@Override
	public void addItem(final State trialState, final int site, final int what, final int who, final int stateVal,
			final int rotationVal, int valueVal, final Game game)
	{
		listStacks[site - offset].incrementSize();
		listStacks[site - offset].setWhat(what);
		listStacks[site - offset].setWho(who);
		listStacks[site - offset].setState(stateVal);
		listStacks[site - offset].setRotation(rotationVal);
		listStacks[site - offset].setValue(valueVal);

//		final TIntArrayList hidden = new TIntArrayList();
//		hidden.add(null);
//		for (int i = 0; i < game.players().count(); i++)
//			hidden.add(Constants.INVISIBLE);
//		listStacks[site - offset].addHidden(hidden);
	}
	
	@Override
	public void setSite(final State trialState, final int site, final int whoVal, final int whatVal, final int countVal,
			final int stateVal, final int rotationVal, final int valueVal, final SiteType type)
	{
		final boolean wasEmpty = isEmpty(site, type);
		
		if (whoVal != Constants.UNDEFINED) listStacks[site - offset].setWho(whoVal);
		if (whatVal != Constants.UNDEFINED) listStacks[site - offset].setWhat(whatVal);
		if (stateVal != Constants.UNDEFINED) listStacks[site - offset].setState(stateVal);
		if (rotationVal != Constants.UNDEFINED) listStacks[site - offset].setRotation(rotationVal);
		if (valueVal != Constants.UNDEFINED) listStacks[site - offset].setValue(valueVal);
		
		
		final boolean isEmpty = isEmpty(site, type);
		if (wasEmpty == isEmpty) return;
		
		if (isEmpty) 
			addToEmptyCell(site);
		else 
			removeFromEmptyCell(site - offset);
	}


	@Override
	public void setSite(final State trialState, final int site, final int level, final int whoVal, final int whatVal,
			final int countVal, final int stateVal, final int rotationVal, final int valueVal)
	{
		final boolean wasEmpty = isEmpty(site, SiteType.Cell);
		
		if (whoVal != Constants.UNDEFINED) listStacks[site - offset].setWho(whoVal, level);
		if (whatVal != Constants.UNDEFINED) listStacks[site - offset].setWhat(whatVal, level);
		if (stateVal != Constants.UNDEFINED) listStacks[site - offset].setState(stateVal, level);
		if (rotationVal != Constants.UNDEFINED) listStacks[site - offset].setRotation(rotationVal, level);
		if (valueVal != Constants.UNDEFINED) listStacks[site - offset].setValue(valueVal, level);
		
		
		final boolean isEmpty = isEmpty(site, SiteType.Cell);
		if (wasEmpty == isEmpty) return;
		
		if (isEmpty) 
			addToEmptyCell(site);
		else 
			removeFromEmptyCell(site - offset);
	}

	@Override
	public boolean isOccupied(final int site) 
	{
		return listStacks[site - offset] != null && listStacks[site - offset].what() != 0;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public int whoCell(final int site) 
	{
		if (listStacks[site - offset] == null) 
			return 0;
		
		return listStacks[site - offset].who();
	}

	@Override
	public int whoCell(final int site, final int level)
	{
		if (listStacks[site - offset] == null) 
			return 0;
		
		return listStacks[site - offset].who(level);
	}

	@Override
	public int whatCell(final int site)
	{
		if (listStacks[site - offset] == null) 
			return 0;
		
		return listStacks[site - offset].what();
	}

	@Override
	public int stateCell(final int site)
	{
		if (listStacks[site - offset] == null) 
			return 0;
		
		return listStacks[site - offset].state();
	}

	@Override
	public int rotationCell(final int site)
	{
		if (listStacks[site - offset] == null)
			return 0;
		
		return listStacks[site - offset].rotation();
	}

	@Override
	public int valueCell(final int site)
	{
		if (listStacks[site - offset] == null)
			return 0;

		return listStacks[site - offset].value();
	}

	@Override
	public int whatCell(final int site, final int level)
	{
		if (listStacks[site - offset] == null) 
			return 0;
		
		return listStacks[site - offset].what(level);
	}
	
	@Override
	public int stateCell(final int site, final int level)
	{
		if (listStacks[site - offset] == null) 
			return 0;
		
		return listStacks[site - offset].state(level);
	}
	
	@Override
	public int rotationCell(final int site, final int level)
	{
		if (listStacks[site - offset] == null)
			return 0;
		
		return listStacks[site - offset].rotation(level);
	}

	@Override
	public int valueCell(final int site, final int level)
	{
		if (listStacks[site - offset] == null)
			return 0;

		return listStacks[site - offset].value(level);
	}

	@Override
	public int remove(final State state, final int site, final SiteType graphElement)
	{

		if (listStacks[site - offset] == null)
			return 0;

		final int componentRemove = listStacks[site - offset].what();
		listStacks[site - offset].decrementSize();

		return componentRemove;
	}
	
	@Override
	public int remove(final State state, final int site, final int level, final SiteType graphElement)
	{
		if (listStacks[site - offset] == null)
			return 0;

		final int componentRemove = listStacks[site - offset].what(level);
		for (int i = level; i < sizeStackCell(site) - 1; i++)
		{
			listStacks[site - offset].setWhat(listStacks[site - offset].what(i + 1), i);
			listStacks[site - offset].setWho(listStacks[site - offset].who(i + 1), i);
			listStacks[site - offset].setState(listStacks[site - offset].state(i + 1), i);
			listStacks[site - offset].setRotation(listStacks[site - offset].rotation(i + 1), i);
			listStacks[site - offset].setValue(listStacks[site - offset].value(i + 1), i);
			// listStacks[site - offset].setHidden(listStacks[site - offset].hiddenArray(i +
			// 1), i);
		}

		listStacks[site - offset].setWhat(0);
		listStacks[site - offset].setWho(0);
//		listStacks[site - offset].addHidden(null);
		listStacks[site - offset].decrementSize();

		return componentRemove;
	}

	@Override
	public int remove(final State state, final int site, final int level) 
	{
		if (listStacks[site - offset] == null) return 0;

		final int componentRemove = listStacks[site - offset].what(level);
		for (int i = level; i < sizeStackCell(site) - 1; i++)
		{
			listStacks[site - offset].setWhat(listStacks[site - offset].what(i + 1), i);
			listStacks[site - offset].setWho(listStacks[site - offset].who(i + 1), i);
			listStacks[site - offset].setState(listStacks[site - offset].state(i + 1), i);
			listStacks[site - offset].setRotation(listStacks[site - offset].rotation(i + 1), i);
			listStacks[site - offset].setValue(listStacks[site - offset].value(i + 1), i);
			// listStacks[site - offset].setHidden(listStacks[site - offset].hiddenArray(i +
			// 1), i);
		}
		
		listStacks[site - offset].setWhat(0);
		listStacks[site - offset].setWho(0);
		// listStacks[site - offset].addHidden(null);
		listStacks[site - offset].decrementSize();
		
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
		if (listStacks[site - offset] == null)
			return;
		
		final int previousSize = listStacks[site - offset].size();
		for (int i = 0; i < previousSize; i++)
		{
			listStacks[site - offset].decrementSize();
		}
	}

	@Override
	public int countCell(final int site) 
	{
		if (listStacks[site - offset] == null) 
			return 0;
		
		return 1;
	}

	/**
	 * @param site
	 * @return Size of stack.
	 */
	@Override
	public int sizeStackCell(final int site) 
	{
		if (listStacks[site - offset] == null) 
			return 0;
		
		return listStacks[site - offset].size();
	}

	@Override
	public boolean isPlayable(final int site) 
	{
		return listStacks[site - offset] != null;
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
	public void addItemVertex(State trialState, int site, int whatValue, int whoId, Game game)
	{
		// Nothing to do.
	}

	@Override
	public void insertVertex(State trialState, int site, int level, int whatValue, int whoId, final int state,
			final int rotation, final int value, Game game)
	{
		// Nothing to do.
	}

	@Override
	public void addItemVertex(State trialState, int site, int whatValue, int whoId, int stateVal, int rotationVal,
			int valueVal,
			Game game)
	{
		// Nothing to do.
	}

	@Override
	public void addItemVertex(State trialState, int site, int whatValue, int whoId, Game game, boolean[] hiddenValues,
			boolean masked)
	{
		// Nothing to do.
	}

	@Override
	public void removeStackVertex(State trialState, int site)
	{
		// Nothing to do.
	}

	@Override
	public void addItemEdge(State trialState, int site, int whatValue, int whoId, Game game)
	{
		// Nothing to do.
	}

	@Override
	public void insertEdge(State trialState, int site, int level, int whatValue, int whoId, final int state,
			final int rotation, final int value, Game game)
	{
		// Nothing to do.
	}

	@Override
	public void addItemEdge(State trialState, int site, int whatValue, int whoId, int stateVal, int rotationVal,
			int valueVal,
			Game game)
	{
		// Nothing to do.
	}

	@Override
	public void addItemEdge(State trialState, int site, int whatValue, int whoId, Game game, boolean[] hiddenValues,
			boolean masked)
	{
		// Nothing to do.
	}

	@Override
	public void removeStackEdge(State trialState, int site)
	{
		// Nothing to do.
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

	@Override
	public void addToEmpty(final int site, final SiteType graphType)
	{
		addToEmptyCell(site);
	}

	@Override
	public void removeFromEmpty(final int site, final SiteType graphType)
	{
		removeFromEmptyCell(site);
	}

	@Override
	public boolean isHidden(final int player, final int site, final int level, final SiteType graphElementType)
	{
		return listStacks[site - offset].isHidden(player, level);
	}

	@Override
	public boolean isHiddenWhat(final int player, final int site, final int level, final SiteType graphElementType)
	{
		return listStacks[site - offset].isHiddenWhat(player, level);
	}

	@Override
	public boolean isHiddenWho(final int player, final int site, final int level, final SiteType graphElementType)
	{
		return listStacks[site - offset].isHiddenWho(player, level);
	}

	@Override
	public boolean isHiddenState(final int player, final int site, final int level, final SiteType graphElementType)
	{
		return listStacks[site - offset].isHiddenState(player, level);
	}

	@Override
	public boolean isHiddenRotation(final int player, final int site, final int level, final SiteType graphElementType)
	{
		return listStacks[site - offset].isHiddenRotation(player, level);
	}

	@Override
	public boolean isHiddenValue(final int player, final int site, final int level, final SiteType graphElementType)
	{
		return listStacks[site - offset].isHiddenValue(player, level);
	}

	@Override
	public boolean isHiddenCount(final int player, final int site, final int level, final SiteType graphElementType)
	{
		return listStacks[site - offset].isHiddenCount(player, level);
	}

	@Override
	public void setHidden(final State state, final int player, final int site, final int level,
			final SiteType graphElementType, final boolean on)
	{
		listStacks[site - offset].setHidden(player, level, on);
	}

	@Override
	public void setHiddenWhat(final State state, final int player, final int site, final int level,
			final SiteType graphElementType, final boolean on)
	{
		listStacks[site - offset].setHiddenWhat(player, level, on);
	}

	@Override
	public void setHiddenWho(final State state, final int player, final int site, final int level,
			final SiteType graphElementType, final boolean on)
	{
		listStacks[site - offset].setHiddenWho(player, level, on);
	}

	@Override
	public void setHiddenState(final State state, final int player, final int site, final int level,
			final SiteType graphElementType, final boolean on)
	{
		listStacks[site - offset].setHiddenState(player, level, on);
	}

	@Override
	public void setHiddenRotation(final State state, final int player, final int site, final int level,
			final SiteType graphElementType, final boolean on)
	{
		listStacks[site - offset].setHiddenRotation(player, level, on);
	}

	@Override
	public void setHiddenValue(final State state, final int player, final int site, final int level,
			final SiteType graphElementType, final boolean on)
	{
		listStacks[site - offset].setHiddenValue(player, level, on);
	}

	@Override
	public void setHiddenCount(final State state, final int player, final int site, final int level,
			final SiteType graphElementType, final boolean on)
	{
		listStacks[site - offset].setHiddenCount(player, level, on);
	}
}
