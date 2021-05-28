package other.state.puzzle;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import game.Game;
import game.equipment.container.Container;
import game.types.board.SiteType;
import other.context.Context;
import other.state.State;
import other.state.container.ContainerState;
import other.state.zhash.ZobristHashGenerator;
import other.trial.Trial;

/**
 * @author cambolbro and Eric.Piette
 */
public class ContainerDeductionPuzzleStateLarge extends ContainerDeductionPuzzleState
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Vertice. */
	protected final List<BitSet> verticeList;

	/** Edges. */
	protected final List<BitSet> edgesList;

	/** Cells. */
	protected final List<BitSet> cellsList;

	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 * 
	 * @param generator The generator.
	 * @param game      The game.
	 * @param container The container.
	 */
	public ContainerDeductionPuzzleStateLarge(final ZobristHashGenerator generator, final Game game,
			final Container container)
	{
		super(generator, game, container);
		final int numEdges = game.board().topology().edges().size();
		final int numCells = game.board().topology().cells().size();
		final int numVertices = game.board().topology().vertices().size();
		
		if (game.isDeductionPuzzle())
			nbValuesVert = game.board().vertexRange().max(new Context(game, new Trial(game)))
					- game.board().vertexRange().min(new Context(game, new Trial(game))) + 1;
		else
			nbValuesVert = 1;

		if (game.isDeductionPuzzle())
			nbValuesEdge = game.board().edgeRange().max(new Context(game, new Trial(game)))
					- game.board().edgeRange().min(new Context(game, new Trial(game))) + 1;
		else
			nbValuesEdge = 1;
		
		if (game.isDeductionPuzzle())
			nbValuesCell = game.board().cellRange().max(new Context(game, new Trial(game)))
					- game.board().cellRange().min(new Context(game, new Trial(game))) + 1;
		else
			nbValuesCell = 1;
		
		verticeList = new ArrayList<BitSet>();
		for (int var = 0; var < numVertices; var++)
		{
			final BitSet values = new BitSet(nbValuesVert);
			values.set(0, nbValuesVert, true);
			verticeList.add(values);
		}

		edgesList = new ArrayList<BitSet>();
		for (int var = 0; var < numEdges; var++) 
		{
			final BitSet values = new BitSet(nbValuesEdge);
			values.set(0, nbValuesEdge, true);
			edgesList.add(values);
		}

		cellsList = new ArrayList<BitSet>();
		for (int var = 0; var < numCells; var++) 
		{
			final BitSet values = new BitSet(nbValuesCell);
			values.set(0, nbValuesCell, true);
			cellsList.add(values);
		}
	}

	/**
	 * Copy constructor.
	 *
	 * @param other
	 */
	public ContainerDeductionPuzzleStateLarge(final ContainerDeductionPuzzleStateLarge other)
	{
		super(other);
		
		nbValuesVert = other.nbValuesVert;
		nbValuesEdge = other.nbValuesEdge;
		nbValuesCell = other.nbValuesCell;

		if (other.verticeList == null)
			verticeList = null;
		else
		{
			verticeList = new ArrayList<BitSet>();
			for (final BitSet bs : other.verticeList)
				verticeList.add((BitSet)bs.clone());
		}
		
		if (other.edgesList == null)
			edgesList = null;
		else
		{
			edgesList = new ArrayList<BitSet>();
			for (final BitSet bs : other.edgesList)
				edgesList.add((BitSet)bs.clone());
		}
		
		if (other.cellsList == null)
			cellsList = null;
		else
		{
			cellsList = new ArrayList<BitSet>();
			for (final BitSet bs : other.cellsList)
				cellsList.add((BitSet)bs.clone());
		}
	}

	//-------------------------------------------------------------------------
	
	@Override
	public void reset(final State trialState, final Game game)
	{
		super.reset(trialState, game);

		if (verticeList != null)
			for (final BitSet bs : verticeList)
				bs.set(0, nbValuesVert, true);
		
		if (edgesList != null)
			for (final BitSet bs : edgesList)
				bs.set(0, nbValuesEdge, true);
		
		if (cellsList != null)
			for (final BitSet bs : cellsList)
				bs.set(0, nbValuesCell, true);	
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int remove(final State state, final int site, final SiteType type)
	{
		return 0;
	}

	@Override
	public ContainerState deepClone()
	{
		return new ContainerDeductionPuzzleStateLarge(this);
	}

	@Override
	public String nameFromFile()
	{
		return null;
	}

	//-------------------------------------------------------------------------

	@Override
	public int whoCell(final int site)
	{
		return 0;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param var
	 * @return Number of edges for this "edge" on the graph.
	 */
	@Override
	public int numberEdge(final int var)
	{
		for(int i = 0 ; i < nbValuesEdge ; i++)
			if(bitEdge(var, i))
				return i;
		return 0;
	}

	/**
	 * @param var
	 * @return True if that vertex variable is resolved
	 */
	@Override
	public boolean isResolvedVerts(final int var)
	{
		return verticeList.get(var - offset).cardinality() == 1;
	}

	/**
	 * @param var
	 * @return The assigned value for vertex, else 0 if not assigned yet.
	 */
	@Override
	public int whatVertex(final int var)
	{
		final BitSet bs = verticeList.get(var - offset);
		
		final int firstOnBit = bs.nextSetBit(0);
		if (firstOnBit == -1)
		{
			System.out.println("** Unexpected empty variable.");
			return 0;
		}
		
		if (bs.nextSetBit(firstOnBit + 1) == -1)
			return firstOnBit;
		else
			return 0;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Sets all values for the specified variable to "true".
	 * @param type
	 * @param var
	 * @param numValues
	 */
	@Override
	public void resetVariable(final SiteType type, final int var, final int numValues)
	{
		switch (type)
		{
		case Vertex: verticeList.get(var - offset).set(0, nbValuesVert, true);	break;
		case Edge:   edgesList.get(var).set(0, nbValuesEdge, true);	break;
		case Cell:   cellsList.get(var).set(0, nbValuesCell, true);	break;
		default:
			verticeList.get(var - offset).set(0, nbValuesVert, true);
			break;
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param type
	 * @param var
	 * @return Current values for the specified variable.
	 */
	@Override
	public BitSet values(final SiteType type, final int var)
	{
		switch (type)
		{
		case Vertex: return verticeList.get(var - offset);
		case Edge:   return edgesList.get(var);
		case Cell:   return cellsList.get(var);
		default:
			return verticeList.get(var - offset);
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 *
	 * @param var
	 * @param value
	 * @return if the value for the vert is possible
	 */
	@Override
	public boolean bitVert(final int var, final int value) 
	{
		return verticeList.get(var - offset).get(value);
	}

	/**
	 * @param var
	 * @param value
	 * Set a value to the variable for the vert.
	 */
	@Override
	public void setVert(final int var, final int value) 
	{
		final BitSet bs = verticeList.get(var - offset);
		bs.clear();
		bs.set(value, true);
	}

	/**
	 * @param var
	 * @param value
	 * Toggle a value to the variable for the cell.
	 */
	@Override
	public void toggleVerts(final int var, final int value) 
	{
		verticeList.get(var - offset).flip(value);
	}


	//-------------------------------------------------------------------------

	/**
	 * @param var
	 * @return True if that edge variable is resolved
	 */
	@Override
	public boolean isResolvedEdges(final int var)
	{
		return edgesList.get(var).cardinality() == 1;
	}

	/**
	 * @param var
	 * @return the assigned value for edge
	 */
	@Override
	public int whatEdge(final int var)
	{
		final BitSet bs = edgesList.get(var);
		
		final int firstOnBit = bs.nextSetBit(0);
		if (firstOnBit == -1)
		{
			System.out.println("** Unexpected empty variable.");
			return 0;
		}
		
		if (bs.nextSetBit(firstOnBit+1) == -1)
			return firstOnBit;
		else
			return 0;
	}

	/**
	 *
	 * @param var
	 * @param value
	 * @return if the value for the edge is possible
	 */
	@Override
	public boolean bitEdge(final int var, final int value) 
	{
		return edgesList.get(var).get(value);
	}

	/**
	 * @param var
	 * @param value
	 * Set a value to the variable for the edge.
	 */
	@Override
	public void setEdge(final int var, final int value) 
	{
		final BitSet bs = edgesList.get(var);
		bs.clear();
		bs.set(value, true);
	}

	/**
	 * @param var
	 * @param value
	 * Toggle a value to the variable for the edge.
	 */
	@Override
	public void toggleEdges(final int var, final int value) 
	{
		edgesList.get(var).flip(value);
	}

	//-------------------------------------------------------------------------

	/**
	 *
	 * @param var
	 * @param value
	 * @return if the value for the face is possible
	 */
	@Override
	public boolean bitCell(final int var, final int value) 
	{
		return cellsList.get(var).get(value);
	}

	/**
	 * @param var
	 * @param value
	 * Set a value to the variable for the face.
	 */
	@Override
	public void setCell(final int var, final int value) 
	{
		final BitSet bs = cellsList.get(var);
		bs.clear();
		bs.set(value, true);
	}

	/**
	 * @param var
	 * @param value
	 * Toggle a value to the variable for the face.
	 */
	@Override
	public void toggleCells(final int var, final int value) 
	{
		cellsList.get(var).flip(value);
	}

	/**
	 * @param var
	 * @return True if that variable is resolved
	 */
	@Override
	public boolean isResolvedCell(final int var)
	{
		return cellsList.get(var).cardinality() == 1;
	}

	/**
	 * @param var
	 * @return the assigned value for face
	 */

	@Override
	public int whatCell(final int var)
	{
		final BitSet bs = cellsList.get(var);
		
		final int firstOnBit = bs.nextSetBit(0);
		if (firstOnBit == -1)
		{
			System.out.println("** Unexpected empty variable.");
			return 0;
		}
		
		if (bs.nextSetBit(firstOnBit+1) == -1)
			return firstOnBit;
		else
			return 0;
	}
}