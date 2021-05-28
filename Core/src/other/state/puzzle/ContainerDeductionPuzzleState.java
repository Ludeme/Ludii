package other.state.puzzle;

import java.util.BitSet;

import game.Game;
import game.equipment.container.Container;
import game.types.board.SiteType;
import main.collections.ChunkSet;
import main.math.BitTwiddling;
import other.context.Context;
import other.state.State;
import other.state.container.ContainerState;
import other.state.symmetry.SymmetryValidator;
import other.state.zhash.ZobristHashGenerator;
import other.trial.Trial;

/**
 * The state for the deduction puzzle with a range of values under 32 for each
 * graph element.
 * 
 * @author Eric.Piette
 */
public class ContainerDeductionPuzzleState extends BaseContainerStateDeductionPuzzles
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** The number of values of the vertices. */
	protected int nbValuesVert = 1;
	
	/** ChunkSet verts. */
	protected ChunkSet verts;

	/** ChunkSet edges. */
	protected ChunkSet edges;

	/** NbValues for edges */
	protected int nbValuesEdge = 1;

	/** ChunkSet cells. */
	protected ChunkSet cells;

	/** NbValues for cells */
	protected int nbValuesCell = 1;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor.
	 * 
	 * @param generator
	 * @param game
	 * @param container
	 */
	public ContainerDeductionPuzzleState(final ZobristHashGenerator generator, final Game game, final Container container)
	{
		super(game, container, game.equipment().totalDefaultSites());

		final int numEdges = game.board().topology().edges().size();
		final int numFaces = game.board().topology().cells().size();
		final int numVertices = game.board().topology().vertices().size();
		
		if (game.isDeductionPuzzle())
			nbValuesVert = game.board().vertexRange().max(new Context(game, new Trial(game))) + 1;
		else
			nbValuesVert = 1;

		if (game.isDeductionPuzzle())
			nbValuesEdge = game.board().edgeRange().max(new Context(game, new Trial(game))) + 1;
		else
			nbValuesEdge = 1;
		
		if (game.isDeductionPuzzle())
			nbValuesCell = game.board().cellRange().max(new Context(game, new Trial(game))) + 1;
		else
			nbValuesCell = 1;
		
		if ((nbValuesVert) <= 31 && nbValuesEdge <= 31 && nbValuesCell <= 31)
		{
			final int chunkSize = BitTwiddling.nextPowerOf2(nbValuesVert);
			this.verts = new ChunkSet(chunkSize, numVertices);
			for (int var = 0; var < numVertices; var++)
				this.verts.setNBits(var, nbValuesVert, true);

			final int chunkSizeEdge = BitTwiddling.nextPowerOf2(nbValuesEdge);
			this.edges = new ChunkSet(chunkSizeEdge, numEdges);
			for (int var = 0 ; var < numEdges ; var++) 
				this.edges.setNBits(var, nbValuesEdge, true);

			final int chunkSizeFace = BitTwiddling.nextPowerOf2(nbValuesCell);
			this.cells = new ChunkSet(chunkSizeFace,numFaces);
			for (int i = 0 ; i < numFaces ; i ++)
				this.cells.setNBits(i, nbValuesCell, true);
		}
	}

	/**
	 * Copy constructor.
	 *
	 * @param other
	 */
	public ContainerDeductionPuzzleState(final ContainerDeductionPuzzleState other)
	{
		super(other);
		
		nbValuesVert = other.nbValuesVert;
		nbValuesEdge = other.nbValuesEdge;
		nbValuesCell = other.nbValuesCell;

		this.verts = (other.verts == null) ? null : (ChunkSet) other.verts.clone();
		this.edges = (other.edges == null) ? null : (ChunkSet) other.edges.clone();
		this.cells = (other.cells == null) ? null : (ChunkSet) other.cells.clone();
	}
	
	//-------------------------------------------------------------------------

	@Override
	public void reset(final State trialState, final Game game)
	{
		super.reset(trialState, game);

		if (verts != null && edges != null && cells != null)
		{
			final int numEdges = game.board().topology().edges().size();
			final int numCells = game.board().topology().cells().size();
			final int numVertices = game.board().topology().vertices().size();

			verts.clear();

			for (int var = 0; var < numVertices; var++)
				this.verts.setNBits(var, nbValuesVert, true);

			edges.clear();

			for (int var = 0 ; var < numEdges ; var++) 
				this.edges.setNBits(var, nbValuesEdge, true);

			cells.clear();

			for (int i = 0; i < numCells; i++)
				this.cells.setNBits(i, nbValuesCell, true);
		}
	}

	@Override
	public int remove(final State state, final int site, final SiteType type)
	{
		return 0;
	}

	@Override
	public int remove(final State state, final int site, final int level, final SiteType type)
	{
		return remove(state, site, type);
	}

	@Override
	public ContainerState deepClone()
	{
		return new ContainerDeductionPuzzleState(this);
	}

	@Override
	public int numberEdge(final int var)
	{
		for (int i = 0 ; i < nbValuesEdge ; i++)
			if (bitEdge(var, i))
				return i;
		return 0;
	}

	@Override
	public boolean isResolvedVerts(final int var)
	{
		return verts.isResolved(var);
	}

	@Override
	public int whatVertex(final int var)
	{
		return verts.resolvedTo(var);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public void resetVariable(final SiteType type, final int var, final int numValues)
	{
		switch (type)
		{
		case Vertex: verts.setNBits(var, numValues, true);	break;
		case Edge:   edges.setNBits(var, numValues, true);	break;
		case Cell:   cells.setNBits(var, numValues, true);	break;
		default:
			verts.setNBits(var, numValues, true);
			break;
		}
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public BitSet values(final SiteType type, final int var)
	{
		final BitSet bs = new BitSet();
		switch (type)
		{
		case Vertex: 
			{
				final int values = verts.getChunk(var);
				for (int n = 0; n < nbValuesVert; n++)
					if ((values & (0x1 << n)) != 0)
						bs.set(n, true);
			}
			break;
		case Edge: 
			{
				final int values = edges.getChunk(var);
			for (int n = 0; n < nbValuesEdge; n++)
					if ((values & (0x1 << n)) != 0)
						bs.set(n, true);
			}
			break;
		case Cell: 
			{
				final int values = cells.getChunk(var);
			for (int n = 0; n < nbValuesCell; n++)
					if ((values & (0x1 << n)) != 0)
						bs.set(n, true);
			}
			break;
		default:
			final int values = cells.getChunk(var);
			for (int n = 0; n < nbValuesCell; n++)
				if ((values & (0x1 << n)) != 0)
					bs.set(n, true);
			break;
		}
		return bs;
	}
	

	//-------------------------------------------------------------------------
	
	@Override
	public boolean bitVert(final int var, final int value) 
	{
		return (verts.getBit(var, value) == 1);
	}

	@Override
	public void setVert(final int var, final int value) 
	{
		verts.resolveToBit(var, value);
	}

	@Override
	public void toggleVerts(final int var, final int value) 
	{
		verts.toggleBit(var, value);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isResolvedEdges(final int var)
	{
		return edges.isResolved(var);
	}

	@Override
	public int whatEdge(final int var)
	{
		return edges.resolvedTo(var);
	}

	@Override
	public boolean bitEdge(final int var, final int value) 
	{
		return (edges.getBit(var, value) == 1);
	}

	@Override
	public void setEdge(final int var, final int value) 
	{
		edges.resolveToBit(var, value);
	}

	@Override
	public void toggleEdges(final int var, final int value) 
	{
		edges.toggleBit(var, value);
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean bitCell(final int var, final int value) 
	{
		return (cells.getBit(var, value) == 1);
	}

	@Override
	public void setCell(final int var, final int value) 
	{
		cells.resolveToBit(var, value);
	}

	@Override
	public void toggleCells(final int var, final int value) 
	{
		cells.toggleBit(var, value);
	}

	@Override
	public boolean isResolvedCell(final int var)
	{
		return cells.isResolved(var);
	}

	@Override
	public int whatCell(final int var)
	{
		return cells.resolvedTo(var);
	}

	@Override
	public void setPlayable(final State trialState, final int site, final boolean on)
	{
		// Nothing to do.
	}

	//-------------------------------------------------------------------------

	@Override
	public int sizeStackCell(final int var) 
	{
		if (cells.isResolved(var))
			return 1;
		
		return 0;
	}
	
	@Override
	public int sizeStackEdge(final int var) 
	{
		if (edges.isResolved(var))
			return 1;

		return 0;
	}
	
	@Override
	public int sizeStackVertex(final int var)
	{
		if (verts.isResolved(var))
			return 1;
		
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
	public int valueCell(int site)
	{
		return 0;
	}
	
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

	@Override
	public void addItem(State trialState, int site, int what, int who, Game game) 
	{
		// Nothing to do.
	}

	@Override
	public void insert(State trialState, final SiteType type, int site, int level, int what, int who, final int state,
			final int rotation, final int value, Game game)
	{
		// Nothing to do.
	}

	@Override
	public void insertCell(State trialState, int site, int level, int what, int who, final int state, final int rotation,
			final int value, Game game)
	{
		// Nothing to do.
	}

	@Override
	public void addItem(State trialState, int site, int what, int who, int stateVal, int rotationVal, int valueVal,
			Game game)
	{
		// Nothing to do.
	}

	@Override
	public void addItem(State trialState, int site, int what, int who, Game game, boolean[] hidden,
			final boolean masked)
	{
		// Nothing to do.
	}

	@Override
	public void removeStack(State state, int site) 
	{
		// Nothing to do.
	}

	@Override
	public int whatCell(int site, int level) 
	{
		return whatCell(site);
	}

	@Override
	public int stateCell(int site, int level) 
	{
		return stateCell(site);
	}

	@Override
	public int rotationCell(int site, int level) 
	{
		return rotationCell(site);
	}

	@Override
	public int remove(State state, int site, int level) 
	{
		return remove(state, site, SiteType.Cell);
	}

	@Override
	public void setSite(State trialState, int site, int level, int whoVal, int whatVal, int countVal, int stateVal,
			int rotationVal, int valueVal)
	{
		// do nothing
	}
	
	@Override
	public long canonicalHash(SymmetryValidator validator, final State state, final boolean whoOnly)
	{
		return 0;
	}

	@Override
	public int valueCell(int site, int level)
	{
		return 0;
	}

	@Override
	public int valueVertex(int site)
	{
		return 0;
	}

	@Override
	public int valueVertex(int site, int level)
	{
		return 0;
	}

	@Override
	public int value(int site, SiteType graphElementType)
	{
		return 0;
	}

	@Override
	public int value(int site, int level, SiteType graphElementType)
	{
		return 0;
	}

	@Override
	public int valueEdge(int site)
	{
		return 0;
	}

	@Override
	public int valueEdge(int site, int level)
	{
		return 0;
	}
}
