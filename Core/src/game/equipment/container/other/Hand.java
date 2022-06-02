package game.equipment.container.other;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.equipment.container.Container;
import game.functions.dim.DimConstant;
import game.functions.graph.generators.basis.hex.RectangleOnHex;
import game.functions.graph.generators.basis.square.RectangleOnSquare;
import game.functions.graph.generators.basis.tri.RectangleOnTri;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.util.graph.Face;
import game.util.graph.Graph;
import main.Constants;
import metadata.graphics.util.ContainerStyleType;
import other.ItemType;
import other.concept.Concept;
import other.topology.Cell;
import other.topology.Topology;
import other.topology.Vertex;

/**
 * Defines a hand of a player.
 * 
 * @author Eric.Piette
 * 
 * @remarks For any game with components outside of the board.
 */
public class Hand extends Container
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The number of locations in this container. */
	protected int numLocs;

	//-------------------------------------------------------------------------

	/**
	 * @param role The owner of the hand.
	 * @param size The numbers of sites in the hand.
	 *
	 * @example (hand Each size:5)
	 */
	public Hand
	(
			 	   final RoleType role,
		@Opt @Name final Integer  size
	)
	{
		super(null, Constants.UNDEFINED, role);
		
		final String className = this.getClass().toString();
		final String containerName = className.substring(className.lastIndexOf('.') + 1, className.length());
		
		if (role.owner() > 0 && role.owner() <= Constants.MAX_PLAYERS)
		{
			if (name() == null)
				this.setName(containerName + role.owner());
		}
		else if (role == RoleType.Neutral)
		{
			if (name() == null)
				this.setName(containerName + role.owner());
		}
		else if (role == RoleType.Shared)
		{
			if (name() == null)
				this.setName(containerName + role.owner());
		}
		
		this.numLocs = (size == null) ? 1 : size.intValue();
		
		this.style = ContainerStyleType.Hand;
		setType(ItemType.Hand);
	}

	/**
	 * Copy constructor.
	 *
	 * Protected because we do not want the compiler to detect it, this is called
	 * only in Clone method.
	 * 
	 * @param other
	 */
	protected Hand(final Hand other)
	{
		super(other);
		numLocs = other.numLocs;
	}

	//-------------------------------------------------------------------------

	@Override
	public void createTopology(final int beginIndex, final int numEdge)
	{
		final double unit = 1.0 / numLocs;

		topology = new Topology();
		final int realNumEdge = (numEdge == Constants.UNDEFINED) ? 4 : numEdge;

		Graph graph = null;

		if (realNumEdge == 6)
			graph = new RectangleOnHex(new DimConstant(1), new DimConstant(this.numLocs)).eval(null, SiteType.Cell);
		else if (realNumEdge == 3)
			graph = new RectangleOnTri(new DimConstant(1), new DimConstant(this.numLocs)).eval(null, SiteType.Cell);
		else
			graph = new RectangleOnSquare(new DimConstant(1), new DimConstant(this.numLocs), null, null).eval(null,
					SiteType.Cell);
		
		// Add the cells to the topology.
		for (int i = 0; i < this.numLocs; i++)
		{
			final Face face = graph.faces().get(i);
			final Cell cell = new Cell(face.id() + beginIndex, face.pt().x() + (i * unit), face.pt().y(),
					face.pt().z());
			cell.setCoord(cell.row(), cell.col(), 0);
			cell.setCentroid(face.pt().x(), face.pt().y(), 0);
			topology.cells().add(cell);

			// We add the vertices of the cells and vice versa.
			for (final game.util.graph.Vertex v : face.vertices())
			{
				final double x = v.pt().x();
				final double y = v.pt().y();
				final double z = v.pt().z();
				final Vertex vertex = new Vertex(Constants.UNDEFINED, x, y, z);
				cell.vertices().add(vertex);
			}
		}

		numSites = topology.cells().size();
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return The number of sites on this hand.
	 */
	public int numLocs()
	{
		return this.numLocs;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Hand clone() 
	{
		return new Hand(this);
	}
	
	@Override
	public boolean isHand()
	{
		return true;
	}

	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.set(Concept.Hand.id(), true);
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

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		if (role() != null)
		{
			final int indexOwnerPhase = role().owner();
			if (
				(
					indexOwnerPhase < 1 
					&& 
					!role().equals(RoleType.Shared)
					&& 
					!role().equals(RoleType.All)
				)
				|| 
				indexOwnerPhase > game.players().count()
			   )
			{
				game.addRequirementToReport(
						"A hand is defined in the equipment with an incorrect owner: " + role() + ".");
				missingRequirement = true;
			}
		}
		return missingRequirement;
	}
}
