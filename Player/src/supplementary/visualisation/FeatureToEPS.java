package supplementary.visualisation;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import bridge.Bridge;
import features.spatial.AbsoluteFeature;
import features.spatial.RelativeFeature;
import features.spatial.SpatialFeature;
import features.spatial.Walk;
import features.spatial.elements.FeatureElement;
import features.spatial.elements.FeatureElement.ElementType;
import features.spatial.elements.RelativeFeatureElement;
import game.Game;
import other.context.Context;
import other.trial.Trial;

/**
 * Utility class to create .eps images from features.
 * 
 * @author Dennis Soemers
 */
public class FeatureToEPS
{
	
	//-------------------------------------------------------------------------
	
	private static final int J_HEX_RADIUS = 17;
//	private static final int J_HEX_WIDTH = (int) Math.ceil(Math.sqrt(3.0) * J_HEX_RADIUS);
//	private static final int J_HEX_HEIGHT = 2 * J_HEX_RADIUS;
	
	private static final int J_SQUARE_SIDE = 34;
	
	/** Radius of Hex tiles (circumradius = radius of outer circle) */
	private static final String HEX_RADIUS = "" + J_HEX_RADIUS;
	
	/** Width of a Hex tile (with pointy parts going up/down) */
//	private static final String HEX_WIDTH = "3.0 sqrt " + HEX_RADIUS + " mul";
	
	/** Height of a Hex tile (with pointy parts going up/down) */
//	private static final String HEX_HEIGHT = "2 " + HEX_RADIUS + " mul";
	
	private static final String SQUARE_SIDE = "" + J_SQUARE_SIDE;
	
	//-------------------------------------------------------------------------
	
	/** */
	private FeatureToEPS()
	{
		// do not use
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Creates a .eps file visualising the given feature
	 * @param feature
	 * @param player
	 * @param game
	 * @param outputFile File to write output to (EPS program)
	 */
	public static void createEPS
	(
		final SpatialFeature feature, 
		final int player, 
		final Game game,
		final File outputFile
	)
	{		
		// Gather some info on game
		final Bridge bridge = new Bridge();
		final Context dummyContext = new Context(game, new Trial(game));
		final Color friendColour = bridge.settingsColour().playerColour(dummyContext, player);
		final int opponentID = player == 2 ? 1 : 2;
		final Color enemyColour = bridge.settingsColour().playerColour(dummyContext, opponentID);
				
		final double friendR = friendColour.getRed() / 255.0;
		final double friendG = friendColour.getGreen() / 255.0;
		final double friendB = friendColour.getBlue() / 255.0;
		final double enemyR = enemyColour.getRed() / 255.0;
		final double enemyG = enemyColour.getGreen() / 255.0;
		final double enemyB = enemyColour.getBlue() / 255.0;
				
//		boolean squareTiling = (numOrths == 4);
//		boolean hexTiling = (numOrths == 6);
//		boolean playOnIntersections = (game.board() instanceof GoBoard);
//		
//		final int jCellHeight;
//		final int jCellWidth;
//		final String cellHeight;
//		final String cellWidth;
//		
//		if (squareTiling)
//		{
//			jCellHeight = J_SQUARE_SIDE;
//			jCellWidth = J_SQUARE_SIDE;
//			cellHeight = SQUARE_SIDE;
//			cellWidth = SQUARE_SIDE;
//		}
//		else if (hexTiling)
//		{
//			jCellHeight = J_HEX_HEIGHT;
//			jCellWidth = J_HEX_WIDTH;
//			cellHeight = HEX_HEIGHT;
//			cellWidth = HEX_WIDTH;
//		}
//		else
//		{
//			System.err.println("Unsupported tiling!");
//			return;
//		}
				
		// we'll update these as we draw things
		int bboxLeftX = 0;
		int bboxLowerY = 0;
		int bboxRightX = 0;
		int bboxTopY = 0;
		
		if (feature instanceof AbsoluteFeature)
		{
			// TODO ?
			System.err.println("Cannot (yet?) visualise absolute feature");
			return;
		}

		final RelativeFeature rel = (RelativeFeature) feature;
		final Walk from = rel.fromPosition();
		final Walk to = rel.toPosition();
		final FeatureElement[] elements = rel.pattern().featureElements();
		
		double maxX = 0.0;
		double minX = 0.0;
		double maxY = 0.0;
		double minY = 0.0;

		// Compute all the offsets for all the walks			TODO last-from and last-to
		final double[] fromOffset = computeOffset(from);
		final double[] toOffset = computeOffset(to);
		final List<double[]> elementOffsets = new ArrayList<double[]>();
		final List<RelativeFeatureElement> relEls = new ArrayList<RelativeFeatureElement>();

		for (final FeatureElement element : elements)
		{
			if (element instanceof RelativeFeatureElement)
			{
				final RelativeFeatureElement relEl = (RelativeFeatureElement) element;
				elementOffsets.add(computeOffset(relEl.walk()));
				relEls.add(relEl);
			}
		}

		// This will contain the program for this image
		final List<String> imageProgram = new ArrayList<String>();

		//final Set<Cell> cellsToDraw = new HashSet<Cell>();
		
		if (fromOffset != null)
		{
			// TODO draw from-position
		}
		
		if (toOffset != null)
		{
			imageProgram.add(toOffset[0] + " " + toOffset[1] + " IncentiviseTo");
			
			maxX = Math.max(maxX, toOffset[0]);
			minX = Math.min(minX, toOffset[0]);
			
			maxY = Math.max(maxY, toOffset[1]);
			minY = Math.min(minY, toOffset[1]);
			
			// Remember that we should draw this cell
			//cellsToDraw.add(new Cell(toOffset));
		}

		for (int elIdx = 0; elIdx < relEls.size(); ++elIdx)
		{
			final RelativeFeatureElement el = relEls.get(elIdx);
			final ElementType type = el.type();
			final double[] offset = elementOffsets.get(elIdx);
			
			maxX = Math.max(maxX, offset[0]);
			minX = Math.min(minX, offset[0]);
			
			maxY = Math.max(maxY, offset[1]);
			minY = Math.min(minY, offset[1]);

			if (el.not() && type != ElementType.Off)
			{
				imageProgram.add(0, offset[0] + " " + offset[1] + " Not");
			}

			if (type == ElementType.Empty)
			{
				// draw a little white circle to indicate empty position
				imageProgram.add(0, offset[0] + " " + offset[1] + " Empty");
			}
			else if (type == ElementType.Friend)
			{
				// draw a friend
				imageProgram.add(0, offset[0] + " " + offset[1] + " Friend");
			}
			else if (type == ElementType.Enemy)
			{
				// draw an enemy
				imageProgram.add(0, offset[0] + " " + offset[1] + " Enemy");
			}
			else if (type == ElementType.Off)
			{
				if (!el.not())
				{
					// mark cell as off-board
					imageProgram.add(0, offset[0] + " " + offset[1] + " OffBoard");
				}
			}
			else if (type == ElementType.Any)	
			{
				// TODO
				System.err.println("TODO: draw any element");
			}
			else if (type == ElementType.P1)
			{
				// TODO
				System.err.println("TODO: draw P1 element");
			}
			else if (type == ElementType.P2)
			{
				// TODO
				System.err.println("TODO: draw P2 element");
			}
			else if (type == ElementType.Item)	
			{
				// TODO
				System.err.println("TODO: draw Item element");
			}
			else if (type == ElementType.IsPos)
			{
				// TODO
				System.err.println("TODO: draw IsPos element");
			}

			// Remember that we should draw this cell
			//cellsToDraw.add(new Cell(offset));
		}

		// Draw our cells
//		for (int dx = minCellCol; dx <= maxCellCol; ++dx)
//		{
//			for (int dy = minCellRow; dy <= maxCellRow; ++dy)
//			{
//				if (cellsToDraw.contains(new Cell(new int[]{dx, dy})))
//				{
//					imageProgram.add(0, dx + " " + dy + " Cell");
//				}
//			}
//		}

		// Draw dashed versions of all the unused cells
//		for (int dx = minCellCol; dx <= maxCellCol; ++dx)
//		{
//			for (int dy = minCellRow; dy <= maxCellRow; ++dy)
//			{
//				if (!cellsToDraw.contains(new Cell(new int[]{dx, dy})))
//				{
//					imageProgram.add(0, dx + " " + dy + " CellDashed");
//				}
//			}
//		}
		
		// compute bounding box
		bboxRightX = (int) Math.ceil((maxX - minX + 2) * J_SQUARE_SIDE);
		bboxTopY = (int) Math.ceil((maxY - minY + 2) * J_SQUARE_SIDE);
		
		// we'll collect and generate the feature-specific program lines in here
		final List<String> program = new ArrayList<String>();
		
		program.add("% translate a bit to ensure we have some whitespace on left and bottom");
		program.add(J_SQUARE_SIDE + " " + J_SQUARE_SIDE + " translate");
		program.add("");
		
		// start writing program for this row + col
		program.add("gsave");
		program.add("% translate to origin of this particular image");
		program.add((-minX) + " " + J_SQUARE_SIDE + " mul " + (-minY) + " " + J_SQUARE_SIDE + " mul translate");
		program.add("");

		for (final String line : imageProgram)
		{
			program.add(line);
		}

		program.add("");
		program.add("grestore");

		program.add("");
		
		// start writing
		try (final PrintWriter w = new PrintWriter(outputFile.getAbsolutePath(), "ASCII"))
		{	
			// write some general stuff at start of file
			w.println("%!PS");
			w.println("%%LanguageLevel: 3");
			w.println("%%Creator: Ludii");
			w.println("%%CreationDate: " + LocalDate.now().toString());
			w.println("%%BoundingBox: " + bboxLeftX + " " + bboxLowerY + " " + bboxRightX + " " + bboxTopY);
			w.println("%%EndComments");
			w.println("%%BeginProlog");
			w.println("%%EndProlog");
			w.println("");
			// write page size
			w.println("<< /PageSize [" + (bboxRightX - bboxLeftX) + " " + (bboxTopY - bboxLowerY) + "] >> setpagedevice");
			w.println("");
			
			// write some constants
			w.println("%---------------- Constants -------------------");
			w.println("");
			w.println("/Root3 3.0 sqrt def");
			w.println("");
			
			// write useful variables for our pieces / cells / shapes / etc.
			w.println("%--------------- Variables ------------------");
			w.println("");
			w.println("/HexRadius " + HEX_RADIUS + " def");
			w.println("/HexDiameter { HexRadius 2 mul } def");
			w.println("");
			w.println("/SquareSide " + SQUARE_SIDE + " def");
			w.println("");
			w.println("/CircleRadius { 17 .75 mul }  def");
			w.println("/CircleLineWidth 2 def");
			w.println("");
			w.println("/EmptyRadius { 17 .4 mul } def");
			w.println("/EmptyLineWidth 0.75 def");
			w.println("");
			w.println("");
			
			// write useful functions
			w.println("% ----------- Functions -------------");
			w.println("");
			w.println("/inch {72 mul} def");
			w.println("/cm {182.88 mul} def");
			w.println("");
			
			w.println("/X");
			w.println("{   % call: i j X");
			w.println("    2 dict begin ");
			w.println("    /j exch def");
			w.println("    /i exch def");
			w.println("    i SquareSide mul ");
			w.println("    end");
			w.println("} def");
			w.println("");

			w.println("/Y");
			w.println("{   % call: i j Y");
			w.println("    2 dict begin ");
			w.println("    /j exch def");
			w.println("    /i exch def");
			w.println("    j SquareSide mul ");
			w.println("    end");
			w.println("} def");
			w.println("");

			w.println("/XY");
			w.println("{   % call: i j XY");
			w.println("    2 dict begin ");
			w.println("    /j exch def");
			w.println("    /i exch def");
			w.println("    i j X i j Y");
			w.println("    end");
			w.println("} def");
			
//			if (squareTiling)
//			{
//				// functions for square tilings
//				w.println("/X");
//				w.println("{   % call: i j X");
//				w.println("    2 dict begin ");
//				w.println("    /j exch def");
//				w.println("    /i exch def");
//				w.println("    i SquareSide mul ");
//				w.println("    end");
//				w.println("} def");
//				w.println("");
//				
//				w.println("/Y");
//				w.println("{   % call: i j Y");
//				w.println("    2 dict begin ");
//				w.println("    /j exch def");
//				w.println("    /i exch def");
//				w.println("    j SquareSide mul ");
//				w.println("    end");
//				w.println("} def");
//				w.println("");
//				
//				w.println("/XY");
//				w.println("{   % call: i j XY");
//				w.println("    2 dict begin ");
//				w.println("    /j exch def");
//				w.println("    /i exch def");
//				w.println("    i j X i j Y");
//				w.println("    end");
//				w.println("} def");
//				
//				if (playOnIntersections)
//				{
//					w.println("");
//					w.println("/Cell");
//					w.println("{	% call: i j Cell");
//					w.println("	2 dict begin");
//					w.println("    /j exch def");
//					w.println("    /i exch def");
//					w.println("    ");
//					w.println("    gsave");
//					w.println("    ");
//					w.println("	% fill squares");
//					// TODO
//					w.println("	");
//					w.println("	% draw lines");
//					w.println("	0 setgray");
//					w.println("	.1 setlinewidth");
//					w.println("	");
//					w.println("	newpath i j XY moveto SquareSide 0 rlineto stroke");
//					w.println("	newpath i j XY moveto SquareSide neg 0 rlineto stroke");
//					w.println("	newpath i j XY moveto 0 SquareSide rlineto stroke");
//					w.println("	newpath i j XY moveto 0 SquareSide neg rlineto stroke");
//					w.println("	");
//					w.println("	grestore");
//					w.println("    end");
//					w.println("} def");
//					w.println("");
//					
//					w.println("/CellDashed");
//					w.println("{	% call: i j CellDashed");
//					w.println("	2 dict begin");
//					w.println("    /j exch def");
//					w.println("    /i exch def");
//					w.println("    ");
//					w.println("    gsave");
//					w.println("    ");
//					w.println("	% fill squares");
//					// TODO
//					w.println("	");
//					w.println("	% draw lines");
//					w.println("	0 setgray");
//					w.println("	.1 setlinewidth");
//					w.println("	[4 4] 0 setdash");
//					w.println("	");
//					w.println("	newpath i j XY moveto SquareSide 0 rlineto stroke");
//					w.println("	newpath i j XY moveto SquareSide neg 0 rlineto stroke");
//					w.println("	newpath i j XY moveto 0 SquareSide rlineto stroke");
//					w.println("	newpath i j XY moveto 0 SquareSide neg rlineto stroke");
//					w.println("	");
//					w.println("	[] 0 setdash");
//					w.println("	grestore");
//					w.println("    end");
//					w.println("} def");
//					w.println("");
//					
//					w.println("/OffBoard");
//					w.println("{	% call: i j OffBoard");
//					w.println("	2 dict begin");
//					w.println("    /j exch def");
//					w.println("    /i exch def");
//					w.println("    ");
//					w.println("    gsave");
//					w.println("    ");
//					w.println("	% fill squares");
//					w.println("	0.75 setgray");
//					w.println("	newpath i j XY moveto");
//					w.println("	SquareSide 1 sub 0 rmoveto");
//					w.println("	0 SquareSide 1 sub rlineto");
//					w.println("	2 SquareSide 1 sub mul neg 0 rlineto");
//					w.println("	0 2 SquareSide 1 sub mul neg rlineto");
//					w.println("	2 SquareSide 1 sub mul 0 rlineto");
//					w.println("	closepath fill");
//					w.println("	");
//					w.println("	% draw lines");
//					w.println("	0 setgray");
//					w.println("	.1 setlinewidth");
//					w.println("	[4 4] 0 setdash");
//					w.println("	");
//					w.println("	newpath i j XY moveto SquareSide 0 rlineto stroke");
//					w.println("	newpath i j XY moveto SquareSide neg 0 rlineto stroke");
//					w.println("	newpath i j XY moveto 0 SquareSide rlineto stroke");
//					w.println("	newpath i j XY moveto 0 SquareSide neg rlineto stroke");
//					w.println("	");
//					w.println("	[] 0 setdash");
//					w.println("	grestore");
//					w.println("	end");
//					w.println("} def");
//				}
//				else
//				{
//					// TODO cells for square tilings where we play inside the cells
//				}
//			}
//			else if (hexTiling)
//			{
//				// functions for hex tilings
//				w.println("/X");
//				w.println("{   % call: i j X");
//				w.println("    2 dict begin ");
//				w.println("    /j exch def");
//				w.println("    /i exch def");
//				w.println("    i j -.5 mul add HexRadius mul Root3 mul ");
//				w.println("    end");
//				w.println("} def");
//				w.println("");
//				
//				w.println("/Y");
//				w.println("{   % call: i j Y");
//				w.println("    2 dict begin ");
//				w.println("    /j exch def");
//				w.println("    /i exch def");
//				w.println("    j HexRadius mul 3 mul 2 div ");
//				w.println("    end");
//				w.println("} def");
//				w.println("");
//				
//				w.println("/XY");
//				w.println("{   % call: i j XY");
//				w.println("    2 dict begin ");
//				w.println("    /j exch def");
//				w.println("    /i exch def");
//				w.println("    i j X i j Y");
//				w.println("    end");
//				w.println("} def");
//			}
			
			w.println("");
			w.println("/Friend");
			w.println("{   % call: i j Friend");
			w.println("    2 dict begin ");
			w.println("    /j exch def");
			w.println("    /i exch def");
			w.println("");
			w.println("    CircleLineWidth setlinewidth");
			w.println("");
			w.println("    " + friendR + " " + friendG + " " + friendB + " setrgbcolor");
			w.println("    newpath i j XY CircleRadius 0 360 arc fill");
			w.println("");
			w.println("    0 setgray");
			w.println("    newpath i j XY CircleRadius 0 360 arc stroke");
			w.println("    end");
			w.println("} def");
			w.println("");
			
			w.println("/Enemy");
			w.println("{   % call: i j Enemy");
			w.println("    2 dict begin ");
			w.println("    /j exch def");
			w.println("    /i exch def");
			w.println("");
			w.println("    CircleLineWidth setlinewidth");
			w.println("");
			w.println("    " + enemyR + " " + enemyG + " " + enemyB + " setrgbcolor");
			w.println("    newpath i j XY CircleRadius 0 360 arc fill");
			w.println("");
			w.println("    0 setgray");
			w.println("    newpath i j XY CircleRadius 0 360 arc stroke");
			w.println("    end");
			w.println("} def");
			w.println("");
			
			w.println("/Empty");
			w.println("{   % call: i j Empty");
			w.println("    2 dict begin ");
			w.println("    /j exch def");
			w.println("    /i exch def");
			w.println("");
			w.println("    EmptyLineWidth setlinewidth");
			w.println("	[2 2] 0 setdash");
			w.println("");
			w.println("    1 setgray");
			w.println("    newpath i j XY EmptyRadius 0 360 arc fill");
			w.println("");
			w.println("    0 setgray");
			w.println("    newpath i j XY EmptyRadius 0 360 arc stroke");
			w.println("	[] 0 setdash");
			w.println("    end");
			w.println("} def");
			w.println("");
			
			w.println("/IncentiviseTo");
			w.println("{   % call: i j IncentiviseTo");
			w.println("    2 dict begin ");
			w.println("    /j exch def");
			w.println("    /i exch def");
			w.println("");
			w.println("	gsave");
			w.println("    CircleLineWidth setlinewidth");
			w.println("");
			w.println("	0 0 1 setrgbcolor");
			w.println("");
			w.println("    newpath i j XY EmptyRadius 0 360 arc clip");
			w.println("    newpath i j XY moveto SquareSide 0 rmoveto SquareSide -2 mul 0 rlineto stroke");
			w.println("    newpath i j XY moveto 0 SquareSide rmoveto 0 SquareSide -2 mul rlineto stroke");
			w.println("	grestore");
			w.println("    end");
			w.println("} def");
			w.println("");
			
			w.println("/Not");
			w.println("{   % call: i j Not");
			w.println("    2 dict begin ");
			w.println("    /j exch def");
			w.println("    /i exch def");
			w.println("");
			w.println("	gsave");
			w.println("    CircleLineWidth setlinewidth");
			w.println("");
			w.println("	1 0 0 setrgbcolor");
			w.println("");
			w.println("    newpath i j XY EmptyRadius 0 360 arc clip");
			w.println("    newpath i j XY moveto SquareSide -2 div SquareSide 2 div rmoveto SquareSide SquareSide neg rlineto stroke");
			w.println("    newpath i j XY moveto SquareSide -2 div SquareSide -2 div rmoveto SquareSide SquareSide rlineto stroke");
			w.println("	grestore");
			w.println("    end");
			w.println("} def");
			w.println("");
			
			w.println("/textheight");
			w.println("{ 	% based on: https://stackoverflow.com/a/7122326/6735980");
			w.println("    gsave                                  % save graphic context");
			w.println("    {");
			w.println("        100 100 moveto                     % move to some point");
			w.println("        (HIpg) true charpath pathbbox      % gets text path bounding box (LLx LLy URx URy)");
			w.println("        exch pop 3 -1 roll pop             % keeps LLy and URy");
			w.println("        exch sub                           % URy - LLy");
			w.println("    }");
			w.println("    stopped                                % did the last block fail?");
			w.println("    {");
			w.println("        pop pop                            % get rid of \"stopped\" junk");
			w.println("        currentfont /FontMatrix get 3 get  % gets alternative text height");
			w.println("    }");
			w.println("    if");
			w.println("    grestore                               % restore graphic context");
			w.println("} bind def");
			w.println("");
			
			w.println("/StringAroundPoint");
			w.println("{	% call: newpath i j XY moveto (string) StringAroundPoint");
			w.println("	dup stringwidth pop		% get width of string");
			w.println("	-2 div					% negate, and divide by 2");
			w.println("	textheight -2.9 div		% don't know why, but div by 3 seems to work better than 2");
			w.println("	rmoveto					% move to left and down by half width and height");
			w.println("	show					% show the string");
			w.println("} def");
			w.println("");
			
			// write the actual feature-specific program
			w.println("%-------------- Program --------------");
			// TODO write comment with command-line for generating this image
			w.println("");
			w.println("/Times-Bold findfont 24 scalefont setfont");
			w.println("");
			for (final String line : program)
			{
				w.println(line);
			}
			w.println("");
			
			// write final parts of file
			w.println("");
			w.println("%------------------------------------------");
			w.println("");
			w.println("showpage");
			w.println("");
			w.println("%%Trailer");
			w.println("%%EOF");
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param walk
	 * @return Offset in "step-units" resulting in walking the walk, from anchor
	 */
	private static double[] computeOffset(final Walk walk)
	{
		if (walk == null)
			return null;
		
		double dx = 0.0;
		double dy = 0.0;
		
		// We assume 0.0 points "north", so for normal math that's a 90 degrees angle
		double currAngle = Math.toRadians(90.0);
		for (int i = 0; i < walk.steps().size(); ++i)
		{
			// Subtract steps because we assume clockwise, but normal math assumes counterclockwise
			final float step = walk.steps().getQuick(i);
			currAngle -= step * Math.PI * 2.0;
			
			// Take one step in current direction, add to offsets
			dx += Math.cos(currAngle);
			dy += Math.sin(currAngle);
		}
		
		return new double[]{dx, dy};
	}
	
	//-------------------------------------------------------------------------
	
//	/**
//	 * A cell on our "board", specified by dx and dy from feature's anchor position.
//	 * 
//	 * @author Dennis Soemers
//	 */
//	private static class Cell
//	{
//		/** dx */
//		public final int dx;
//		
//		/** dy */
//		public final int dy;
//		
//		/**
//		 * Constructor
//		 * @param offset
//		 */
//		public Cell(final int[] offset)
//		{
//			dx = offset[0];
//			dy = offset[1];
//		}
//
//		@Override
//		public int hashCode()
//		{
//			final int prime = 31;
//			int result = 1;
//			result = prime * result + dx;
//			result = prime * result + dy;
//			return result;
//		}
//
//		@Override
//		public boolean equals(final Object obj)
//		{
//			if (!(obj instanceof Cell))
//				return false;
//			
//			final Cell other = (Cell) obj;
//
//			return (dx == other.dx && dy == other.dy);
//		}
//	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Main method
	 * @param args
	 */
	public static void main(final String[] args)
	{
		// TODO
	}

}
