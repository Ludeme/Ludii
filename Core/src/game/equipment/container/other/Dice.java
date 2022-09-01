package game.equipment.container.other;

import java.util.BitSet;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
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
 * Generates a set of dice.
 * 
 * @author Eric.Piette
 * 
 * @remarks Used for any dice game to define a set of dice. Only the set of dice
 *          can be rolled.
 */
public final class Dice extends Container
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The number of faces of the die. */
	private final int numFaces;

	/** The start value of the die. */
	private final Integer start;

	/** The faces of the die. */
	private Integer[][] faces;

	/** The biased value of the die. */
	private Integer[] biased;

	/** The number of locations in this container. */
	protected int numLocs;

	//-------------------------------------------------------------------------

	/**
	 * @param d          The number of faces of the die [6].
	 * @param faces      The values of each face.
	 * @param facesByDie The values of each face for each die.
	 * @param from       The starting value of each die [1].
	 * @param role       The owner of the dice [Shared].
	 * @param num        The number of dice in the set.
	 * @param biased     The biased values of each die.
	 * 
	 * @example (dice d:2 from:0 num:4)
	 */
	public Dice
	(
			    @Opt @Name final Integer     d,
			@Or @Opt @Name final Integer[]   faces,
			@Or @Opt @Name final Integer[][] facesByDie,
			@Or @Opt @Name final Integer     from,
			    @Opt       final RoleType    role,
				     @Name final Integer     num,
			    @Opt @Name final Integer[]   biased
	)
	{
		super(null, Constants.UNDEFINED, (role == null) ? RoleType.Shared : role);

		// Limit on the max number of faces.
		if (d != null)
		{
			if (d.intValue() < 0 || d.intValue() > Constants.MAX_FACE_DIE)
				throw new IllegalArgumentException("The number of faces of a die can not be negative or to exceed "
						+ Constants.MAX_FACE_DIE + ".");
		}
		else if (faces != null)
		{
			if (faces.length > Constants.MAX_FACE_DIE)
				throw new IllegalArgumentException(
						"The number of faces of a die can not exceed " + Constants.MAX_FACE_DIE + ".");
		}
		else if (facesByDie != null)
		{
			if (facesByDie.length > Constants.MAX_FACE_DIE)
				throw new IllegalArgumentException(
						"The number of faces of a die can not exceed " + Constants.MAX_FACE_DIE + ".");
		}

		final String className = this.getClass().toString();
		final String containerName = className.substring(className.lastIndexOf('.') + 1, className.length());

		final RoleType realRole = (role == null) ? RoleType.Shared : role;

		if (realRole.owner() > 0 && realRole.owner() <= Constants.MAX_PLAYERS)
		{
			if (name() == null)
				this.setName(containerName + realRole.owner());
		}
		else if (realRole == RoleType.Neutral)
		{
			if (name() == null)
				this.setName(containerName + realRole.owner());
		}
		else if (realRole == RoleType.Shared)
		{
			if (name() == null)
				this.setName(containerName + realRole.owner());
		}

		this.numLocs = num.intValue();

		this.style = ContainerStyleType.Hand;

		this.numFaces = (d != null) ? d.intValue()
				: (faces != null) ? faces.length : facesByDie != null ? facesByDie[0].length : 6;

		int numNonNull = 0;
		if (from != null)
			numNonNull++;
		if (faces != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Zero or one Or parameter must be non-null.");

		this.start = (faces != null || facesByDie != null) ? null : (from == null) ? Integer.valueOf(1) : from;

		if (facesByDie != null)
			this.faces = facesByDie;
		else if (faces != null)
		{
			final Integer[][] sameFacesByDie = new Integer[this.numLocs][faces.length];
			for (int i = 0; i < this.numLocs; i++)
				sameFacesByDie[i] = faces;
			this.faces = sameFacesByDie;
		}
		else
		{
			final Integer[][] sequentialFaces = new Integer[this.numLocs][this.numFaces];
			for (int i = 0; i < this.numLocs; i++)
				for (int j = 0; j < this.numFaces; j++)
					sequentialFaces[i][j] = Integer.valueOf(start.intValue() + j);
			this.faces = sequentialFaces;
		}

		this.biased = biased;
		setType(ItemType.Dice);
	}

	//-------------------------------------------------------------------------

	/**
	 * Copy constructor.
	 *
	 * Protected because we do not want the compiler to detect it, this is called
	 * only in Clone method.
	 * 
	 * @param other The dice.
	 */
	protected Dice(final Dice other)
	{
		super(other);

		numFaces = other.numFaces;
		start = other.start;

		if (other.biased != null)
		{
			biased = new Integer[other.biased.length];
			for (int i = 0; i < other.biased.length; i++)
				biased[i] = other.biased[i];
		}
		else
			biased = null;

		if (other.faces != null)
		{
			faces = new Integer[other.faces.length][];
			for (int i = 0; i < other.faces.length; i++)
				for (int j = 0; j < other.faces[i].length; j++)
					faces[i][j] = other.faces[i][j];
		}
		else
			faces = null;
	}

	@Override
	public Dice clone()
	{
		return new Dice(this);
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The biased values.
	 */
	public Integer[] getBiased()
	{
		return biased;
	}

	/**
	 * @return The faces values of each die.
	 */
	public Integer[][] getFaces()
	{
		return faces;
	}

	/**
	 * @return The starting value of each die.
	 */
	public Integer getStart()
	{
		return start;
	}

	/**
	 * @return The number of faces of each die.
	 */
	public int getNumFaces()
	{
		return numFaces;
	}

	@Override
	public boolean isDice()
	{
		return true;
	}

	@Override
	public boolean isHand()
	{
		return true;
	}

	@Override
	public void createTopology(int beginIndex, int numEdge)
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
		for (int i = 0; i < graph.faces().size(); i++)
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

	/**
	 * @return The number of sites on this dice container.
	 */
	public int numLocs()
	{
		return this.numLocs;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.set(Concept.Dice.id(), true);
		if (biased != null)
			concepts.set(Concept.BiasedDice.id(), true);
		
		if(numFaces == 2)
			concepts.set(Concept.DiceD2.id(), true);

		if(numFaces == 3)
			concepts.set(Concept.DiceD3.id(), true);

		if(numFaces == 4)
			concepts.set(Concept.DiceD4.id(), true);

		if(numFaces == 6)
			concepts.set(Concept.DiceD6.id(), true);

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
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
						!role().equals(RoleType.Neutral)
						&& 
						!role().equals(RoleType.All)
					) 
					|| 
					indexOwnerPhase > game.players().count()
				)
			{
				game.addRequirementToReport(
						"A dice is defined in the equipment with an incorrect owner: " + role() + ".");
				missingRequirement = true;
			}
		}
		return missingRequirement;
	}

}