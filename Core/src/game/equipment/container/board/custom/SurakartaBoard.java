package game.equipment.container.board.custom;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.equipment.container.board.Board;
import game.equipment.container.board.Track;
import game.functions.graph.GraphFunction;
import game.types.board.SiteType;
import main.Constants;
import metadata.graphics.util.ContainerStyleType;

/**
 * Defines a Surakarta-style board.
 *
 * @author cambolbro
 * 
 * @remarks Surakata-style boards have loops that pieces must travel around
 *          in order to capture other pieces.
 *          The following board shapes are supported: 
 *          Square, Rectangle, Hexagon, Triangle.
 */
public class SurakartaBoard extends Board
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	// Number of loops to create
	private int numLoops;
	
	// Row to start loops from (corners are row 0).
	private final int startAtRow;

	//-------------------------------------------------------------------------

	/**
	 * @param graphFn     The graph function used to build the board.
	 * @param loops       Number of loops, i.e. special capture tracks [(minDim - 1) / 2].
	 * @param from        Which row to start loops from [1].
	 * @param largeStack  The game can involves stack(s) higher than 32.
	 * 
	 * @example (surakartaBoard (square 6) loops:2)
	 */
	public SurakartaBoard
	(
			       final GraphFunction graphFn,
		@Opt @Name final Integer       loops,
		@Opt @Name final Integer       from,
		@Opt @Name final Boolean       largeStack
	)
	{
		super(graphFn, null, null, null, null, SiteType.Vertex, largeStack);
		
		numLoops   = (loops != null) ? loops.intValue() : Constants.UNDEFINED;
		startAtRow = (from  != null) ? from.intValue()  : 1;
	}

	//-------------------------------------------------------------------------

	@Override
	public void createTopology(final int beginIndex, final int numEdges)
	{
		super.createTopology(beginIndex, numEdges);

		// Done here because that's needed in the computation of the tracks for Surakarta
		for (final SiteType type : SiteType.values())
		{
			topology.computeRows(type, false);
			topology.computeColumns(type, false);
		}

		final int dim0 = topology().rows(SiteType.Vertex).size() - 1;
		final int dim1 = topology().columns(SiteType.Vertex).size() - 1;

		if (numLoops == Constants.UNDEFINED) 
		{
			switch (topology().graph().basis())
			{
			case Square:  	 numLoops = (Math.min(dim0, dim1) - 1) / 2; break;
			case Triangular: numLoops = (dim0 ) / 2; break;
			//$CASES-OMITTED$
			default: System.out.println("** Board type " + topology().graph().basis() + " not supported for Surkarta.");
			}
		}
		
		final int totalLoops = numLoops;

//		System.out.println("SurakartaBoard: dim0=" + dim0 + ", dim1=" + dim1 + ".");
//		System.out.println("                numLoops=" + numLoop + ", startAtRow=" + startAtRow + ".");
		
//		System.out.println("dim0=" + dim0 + ", dim1=" + dim1);

		switch (topology().graph().basis())
		{
		case Square:  	 createTracksSquare(dim0, dim1, totalLoops); 	   break;
		case Triangular: createTracksTriangular(dim0, totalLoops); break;
		//$CASES-OMITTED$
		default: System.out.println("** Board type " + topology().graph().basis() + " not supported for Surkarta.");
		}

		numSites = topology.vertices().size();

		style = ContainerStyleType.Graph;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @param dim0       Number of cells vertically.
	 * @param dim1       Number of cells horizontally.
	 * @param totalLoops Number of loops to create.
	 */
	void createTracksSquare(final int dim0, final int dim1, final int totalLoops)
	{
		final int rows = dim0 + 1;
		final int cols = dim1 + 1;

		// Create the tracks, forward and backward for each loop
		final List<Integer> track = new ArrayList<Integer>();
		for (int lid = 0; lid < totalLoops; lid++)
		{
			// Generate the basic loop, negating potential speed bumps
			final int loop = startAtRow + lid;
			track.clear();
						
			// Bottom row rightwards
			for (int col = 0; col < cols; col++)
			{
				int site = loop * cols + col;
				if (col == 0 || col == cols - 1)
					site = -site;
				track.add(Integer.valueOf(site));
			}
		
			// Right column upwards
			for (int row = 0; row < rows; row++)
			{
				int site = cols - 1 - loop + row * cols;
				if (row == 0 || row == rows - 1)
					site = -site;
				track.add(Integer.valueOf(site));
			}
			
			// Top row leftwards
			for (int col = 0; col < cols; col++)
			{
				int site = rows * cols - 1 - loop * cols - col;
				if (col == 0 || col == cols - 1)
					site = -site;
				track.add(Integer.valueOf(site));
			}
		
			// Left column downwards
			for (int row = 0; row < rows; row++)
			{
				int site = rows * cols - cols + loop - row * cols;
				if (row == 0 || row == rows - 1)
					site = -site;
				track.add(Integer.valueOf(site));
			}
				
//			System.out.println("Track: " + track);
			
			// Generate the forward and backward track with speed bumps
			final List<Integer> forward  = new ArrayList<Integer>();
			for (int n = 0; n < track.size(); n++)
			{
				final int a = track.get(n).intValue();
				final int b = track.get((n + 1) % track.size()).intValue();
				
				forward.add(Integer.valueOf(Math.abs(a)));
				if (a < 0 && b < 0)
					forward.add(Integer.valueOf(Math.abs(a)));  // add double speed bump
			}
			
			final List<Integer> backward = new ArrayList<Integer>();
			Collections.reverse(track);
			for (int n = 0; n < track.size(); n++)
			{
				final int a = track.get(n).intValue();
				final int b = track.get((n + 1) % track.size()).intValue();
				
				backward.add(Integer.valueOf(Math.abs(a)));
				if (a < 0 && b < 0)
					backward.add(Integer.valueOf(Math.abs(a)));  // add double speed bump
			}
			
			final Integer[] arrayForward  = forward.toArray(new Integer[0]);
			final Integer[] arrayBackward = backward.toArray(new Integer[0]);
			
			final String nameForward  = "Track" + loop + "F";
			final String nameBackward = "Track" + loop + "B";
			
			final Track trackForward  = new Track(nameForward,  arrayForward,  null, null, Boolean.TRUE, null, null, Boolean.TRUE);
			final Track trackBackward = new Track(nameBackward, arrayBackward, null, null, Boolean.TRUE, null, null, Boolean.TRUE);
			
			tracks.add(trackForward);
			tracks.add(trackBackward);
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * @param dim        Number of cells on a side.
	 * @param totalLoops Number of loops to create.
	 */
	void createTracksTriangular(final int dim, final int totalLoops)
	{
		final int rows = dim + 1;
		final int cols = dim + 1;

		// Create the tracks, forward and backward for each loop
		final List<Integer> track = new ArrayList<Integer>();
		for (int lid = 0; lid < totalLoops; lid++)
		{
			// Generate the basic loop, negating potential speed bumps
			final int loop = startAtRow + lid;
			track.clear();
			
			// Three runs
			int v = 0;
			int dec = cols;
			for (int step = 0; step < loop; step++)
				v += dec--;
			
			// Bottom row rightwards
//			System.out.println("v starts at " + v);
						
			for (int step = 0; step < rows - loop; step++)
			{
				int site = v;
				if (step == 0 || step >= rows - loop - 1)
					site = -site;
				track.add(Integer.valueOf(site));
				v++;
			}
		
			// Right column upwards \
			v = cols - 1 - loop;
			dec = rows - 1;
			for (int step = 0; step < rows - loop; step++)
			{
				int site = v;
				if (step == 0 || step >= rows - loop - 1)
					site = -site;
				track.add(Integer.valueOf(site));
				v += dec--;
			}
					
			// Left column downwards /
			// v should be at the correct value
			dec += 3;
			for (int step = 0; step < rows - loop; step++)
			{
				int site = v;
				if (step == 0 || step >= rows - loop - 1)
					site = -site;
				track.add(Integer.valueOf(site));
				v -= dec++;
			}
				
//			System.out.println("Track: " + track);
			
			// Generate the forward and backward track with speed bumps
			final List<Integer> forward  = new ArrayList<Integer>();
			for (int n = 0; n < track.size(); n++)
			{
				final int a = track.get(n).intValue();
				final int b = track.get((n + 1) % track.size()).intValue();
				
				forward.add(Integer.valueOf(Math.abs(a)));
				if (a < 0 && b < 0)
					forward.add(Integer.valueOf(Math.abs(a)));  // add double speed bump
			}
			
			final List<Integer> backward = new ArrayList<Integer>();
			Collections.reverse(track);
			for (int n = 0; n < track.size(); n++)
			{
				final int a = track.get(n).intValue();
				final int b = track.get((n + 1) % track.size()).intValue();
				
				backward.add(Integer.valueOf(Math.abs(a)));
				if (a < 0 && b < 0)
					backward.add(Integer.valueOf(Math.abs(a)));  // add double speed bump
			}
			
			final Integer[] arrayForward  = forward.toArray(new Integer[0]);
			final Integer[] arrayBackward = backward.toArray(new Integer[0]);
			
			final String nameForward  = "Track" + loop + "F";
			final String nameBackward = "Track" + loop + "B";
			
			final Track trackForward  = new Track(nameForward,  arrayForward,  null, null, Boolean.TRUE, null, null, Boolean.TRUE);
			final Track trackBackward = new Track(nameBackward, arrayBackward, null, null, Boolean.TRUE, null, null, Boolean.TRUE);
			
			tracks.add(trackForward);
			tracks.add(trackBackward);
		}
	}
	
	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
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
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		final int dim0 = topology().rows(SiteType.Vertex).size() - 1;
		final int dim1 = topology().columns(SiteType.Vertex).size() - 1;
		
		String englishString = dim0 + " x " + dim1 + " Surakarta board";
		englishString += " with " + topology().graph().basis().name() + " tiling,";
		englishString += " with " + numLoops + " loops which start at row " + startAtRow;

		return englishString;
	}
	
	//-------------------------------------------------------------------------

}
