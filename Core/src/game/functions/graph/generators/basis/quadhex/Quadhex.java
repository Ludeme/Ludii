package game.functions.graph.generators.basis.quadhex;

import java.awt.geom.Point2D;
import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.dim.DimFunction;
import game.functions.graph.generators.basis.Basis;
import game.types.board.BasisType;
import game.types.board.ShapeType;
import game.types.board.SiteType;
import game.util.graph.Edge;
import game.util.graph.Graph;
import game.util.graph.Vertex;
import main.math.MathRoutines;
import other.concept.Concept;
import other.context.Context;

//-----------------------------------------------------------------------------

/**
 * Defines a ``quadhex'' board.
 * 
 * @author cambolbro
 * 
 * @remarks The quadhex board is a hexagon tessellated by quadrilaterals, 
 *          as used for the Three Player Chess board.
 *          The number of cells per side will be twice the number of layers.
 */
public class Quadhex extends Basis
{
	private static final long serialVersionUID = 1L;
	
	private final boolean thirds;
	
	//-------------------------------------------------------------------------

	/**
	 * @param layers Number of layers.
	 * @param thirds Whether to split the board into three-subsections [False].
	 * 
	 * @example (quadhex 4)
	 */
	public Quadhex
	(
				  final DimFunction layers,
       @Opt @Name final Boolean thirds
	)
	{
		this.basis = BasisType.QuadHex;
		this.shape = ShapeType.Hexagon;

		this.dim = new int[] { layers.eval() };
		this.thirds = (thirds == null) ? false : thirds.booleanValue();
	}

	//-------------------------------------------------------------------------

	@Override
	public Graph eval(final Context context, final SiteType siteType)
	{
		final Graph graph = new Graph();
		
		final int layers = dim[0];
		
		if (thirds)
			threeUniformSections(graph, layers);
		else
			sixUniformSections(graph, layers);
		
		graph.makeFaces(true);
		
		graph.setBasisAndShape(basis, shape);
		graph.reorder();
		
		return graph;
	}	
			
	//-------------------------------------------------------------------------

	void sixUniformSections(final Graph graph, final int layers)
	{
		//  B----C
		//  |     \
		//  |      \
		//  |       D
		//  |      / .
		//  |    /    .
		//  |  /       .
		//  |/. . . . . E
		//  A
	
		final Point2D ptA = new Point2D.Double(0, 0);
		final Point2D ptB = new Point2D.Double(0, layers * Math.sqrt(3) / 2);
		final Point2D ptC = new Point2D.Double(layers / 2.0, layers * Math.sqrt(3) / 2);
		final Point2D ptE = new Point2D.Double(layers, 0);
		final Point2D ptD = new Point2D.Double
							(
								(ptC.getX() + ptE.getX()) / 2, 
								(ptC.getY() + ptE.getY()) / 2
							);

		for (int rotn = 0; rotn < 6; rotn++)
		{
			final double theta = rotn * Math.PI / 3;
		
			for (int row = 0; row < layers; row++)
			{
				final double r0 = row / (double)layers;
				final double r1 = (row + 1) / (double)layers;
				
				final Point2D ptAD0 = MathRoutines.rotate(theta, MathRoutines.lerp(r0, ptA, ptD));
				final Point2D ptAD1 = MathRoutines.rotate(theta, MathRoutines.lerp(r1, ptA, ptD));
				
				final Point2D ptBC0 = MathRoutines.rotate(theta, MathRoutines.lerp(r0, ptB, ptC));
				final Point2D ptBC1 = MathRoutines.rotate(theta, MathRoutines.lerp(r1, ptB, ptC));
				
				for (int col = 0; col < layers; col++)
				{
					final double c0 = col / (double)layers;
					final double c1 = (col + 1) / (double)layers;
					
					final Point2D ptAB0 = MathRoutines.lerp(c0, ptAD0, ptBC0);
					final Point2D ptAB1 = MathRoutines.lerp(c1, ptAD0, ptBC0);
					
					final Point2D ptDC1 = MathRoutines.lerp(c1, ptAD1, ptBC1);
					
					final Vertex vertexA = graph.findOrAddVertex(ptAB0);
					final Vertex vertexB = graph.findOrAddVertex(ptAB1);
					final Vertex vertexC = graph.findOrAddVertex(ptDC1);
					
					graph.findOrAddEdge(vertexA, vertexB); 
					graph.findOrAddEdge(vertexB, vertexC); 
					
					if (row == layers - 1)
					{
						final Point2D ptDC0 = MathRoutines.lerp(c0, ptAD1, ptBC1);
						final Vertex vertexD = graph.findOrAddVertex(ptDC0);
						graph.findOrAddEdge(vertexC, vertexD); 
					}
				}
			}
		}
	}
		
	//-------------------------------------------------------------------------

	void threeUniformSections(final Graph graph, final int layers)
	{
		//final double tolerance = 0.001;
		
		// C . . . . . . . O . . . . . . . D
		//  .                             .
		//   .           __F__           .
		//    .      __––  |  ––__      .
		//     . __––      |      ––__ .
		//      G          |          H
		//       \         |         /
		//        \        |        /
		//         A–––––––E–––––––B
		
		final double L32 = layers * Math.sqrt(3) / 2;

		final Point2D ptO = new Point2D.Double(0, 0);

		final Point2D ptA = new Point2D.Double(-layers/2.0, -L32);
		//final Point2D ptB = new Point2D.Double(layers/2.0, -L32);
		final Point2D ptC = new Point2D.Double(-layers, 0);
		//final Point2D ptD = new Point2D.Double(layers, 0);
		final Point2D ptE = new Point2D.Double(0, -L32);
		
		final double ratio = layers / (layers + 0.5);
		
		final Point2D ptF = MathRoutines.lerp(ratio, ptE, ptO);
		final Point2D ptG = MathRoutines.lerp(ratio/2, ptA, ptC);
		//final Point2D ptH = MathRoutines.lerp(ratio/2, ptB, ptD);

		final Graph section = new Graph();
		
		for (int row = 0; row < layers; row++)
		{
			final double r0 = row / (double)layers;
			final double r1 = (row + 1) / (double)layers;
			
			final Point2D ptAE0 = MathRoutines.lerp(r0, ptA, ptE);
			final Point2D ptAE1 = MathRoutines.lerp(r1, ptA, ptE);
			
			final Point2D ptGF0 = MathRoutines.lerp(r0, ptG, ptF);
			final Point2D ptGF1 = MathRoutines.lerp(r1, ptG, ptF);
			
			for (int col = 0; col < layers; col++)
			{
				final double c0 = col / (double)layers;
				final double c1 = (col + 1) / (double)layers;
				
				final Point2D ptAG0 = MathRoutines.lerp(c0, ptAE0, ptGF0);
				final Point2D ptAG1 = MathRoutines.lerp(c1, ptAE0, ptGF0);
				
				final Point2D ptEF0 = MathRoutines.lerp(c0, ptAE1, ptGF1);
				final Point2D ptEF1 = MathRoutines.lerp(c1, ptAE1, ptGF1);
				
				final Vertex vertexA = section.findOrAddVertex(ptAG0);
				final Vertex vertexB = section.findOrAddVertex(ptAG1);
				final Vertex vertexC = section.findOrAddVertex(ptEF0);
				final Vertex vertexD = section.findOrAddVertex(ptEF1);
				
				section.findOrAddEdge(vertexA, vertexB); 
				section.findOrAddEdge(vertexC, vertexD); 
				section.findOrAddEdge(vertexA, vertexC); 
				section.findOrAddEdge(vertexB, vertexD); 

				final Point2D ptAA = new Point2D.Double(-ptAG0.getX(), ptAG0.getY());
				final Point2D ptBB = new Point2D.Double(-ptAG1.getX(), ptAG1.getY());
				final Point2D ptCC = new Point2D.Double(-ptEF0.getX(), ptEF0.getY());
				final Point2D ptDD = new Point2D.Double(-ptEF1.getX(), ptEF1.getY());
				
				final Vertex vertexAA = section.findOrAddVertex(ptAA);
				final Vertex vertexBB = section.findOrAddVertex(ptBB);
				final Vertex vertexCC = section.findOrAddVertex(ptCC);
				final Vertex vertexDD = section.findOrAddVertex(ptDD);
				
				section.findOrAddEdge(vertexAA, vertexBB); 
				section.findOrAddEdge(vertexCC, vertexDD); 
				section.findOrAddEdge(vertexAA, vertexCC); 
				section.findOrAddEdge(vertexBB, vertexDD); 
			}
		}

		// Dpulicate three sections, rotated
		final int verticesPerSection = section.vertices().size(); 
		final double theta = 2 * Math.PI / 3;
		final Vertex[][] save = new Vertex[3][3];
		
		for (int rotn = 0; rotn < 3; rotn++)
		{
			for (final Vertex vertex : section.vertices())
				graph.addVertex(vertex.pt2D().getX(), vertex.pt2D().getY());
			
			for (final Edge edge : section.edges())
				graph.addEdge
				(
					edge.vertexA().id() + rotn * verticesPerSection,
					edge.vertexB().id() + rotn * verticesPerSection
				);
			
			// Perform rotation
			for (final Vertex vertex : section.vertices())
			{
				// x′ = x.cosθ − y.sinθ
				// y′ = y.cosθ + x.sinθ
				final double dx = vertex.pt().x();
				final double dy = vertex.pt().y();
				
				final double xx = dx * Math.cos(theta) - dy * Math.sin(theta);
				final double yy = dy * Math.cos(theta) + dx * Math.sin(theta);
				
				vertex.pt().set(xx, yy);
			}
			
			save[0][rotn] = graph.vertices().get(rotn * verticesPerSection + 4 * layers);
			save[1][rotn] = graph.vertices().get(rotn * verticesPerSection + 4 * layers + 2); 
			save[2][rotn] = graph.vertices().get(rotn * verticesPerSection + layers * (2 * layers + 3)); 
		}
		
		// Make middle triangle
		graph.findOrAddEdge(save[2][0], save[2][1]);
		graph.findOrAddEdge(save[2][1], save[2][2]);
		graph.findOrAddEdge(save[2][2], save[2][0]);
		
		// Join corners of sections
		graph.findOrAddEdge(save[1][0], save[0][1]);
		graph.findOrAddEdge(save[1][1], save[0][2]);
		graph.findOrAddEdge(save[1][2], save[0][0]);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(Game game)
	{
		return 0;
	}

	@Override
	public void preprocess(Game game)
	{
		// Nothing to do.
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.QuadHexTiling.id(), true);
		concepts.set(Concept.HexShape.id(), true);
		concepts.set(Concept.PolygonShape.id(), true);
		return concepts;
	}

	//-------------------------------------------------------------------------
	
}
