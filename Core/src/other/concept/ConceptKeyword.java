package other.concept;

/**
 * The keywords of the concepts.
 * 
 * @author Eric.Piette
 */
public enum ConceptKeyword
{
	/** To hop a neighbouring site. */
	Hop(1, "Hop move concepts."),

	/** Involves Enemy piece. */
	Enemy(2, "Enemy piece or player."),

	/** Tiling of the board. */
	Tiling(3, "Tiling of the board."),

	/** Shape of the board. */
	Shape(4, "Shape of the board."),

	/** Test a condition. */
	Test(5, "Test a condition."),

	/** Space concept. */
	Space(6, "Spatial concepts."),

	/** Moving a piece. */
	MovePiece(7, "Moving a piece."),

	/** Capture. */
	Capture(8, "Capture concepts."),

	/** Decision Move. */
	Decision(9, "Decide to make a move."),

	/** Line. */
	Line(10, "Line concepts."),

	/** Loop. */
	Loop(11, "Loop concepts."),

	/** Related to the legal moves. */
	LegalMoves(12, "Legal moves concepts."),

	/** Connected sites. */
	Connected(13, "connected regions concepts"),

	/** Group of sites. */
	Group(14, "Group concepts"),

	/** Related to Mancala games. */
	Mancala(15, "Mancala games"),

	/** Related to any move types. */
	Move(16, "Move concepts."),

	/** Related to a race. */
	Race(18, "Race concepts."),

	/** Related to a player. */
	Player(19, "Player concepts."),

	/** To slide a piece. */
	Slide(20, "Slide move concepts."),

	/** to step a piece. */
	Step(21, "Step move concepts."),

	/** Related to an empty site. */
	Empty(22, "Empty site concepts."),

	/** To leap a piece. */
	Leap(23, "Leap move concepts."),

	/** Related to a walk. */
	Walk(24, "Walk concepts."),

	/** To bet. */
	Bet(25, "Bet move concepts."),

	/** To vote. */
	Vote(26, "Vote move concepts"),

	/** Cards. */
	Card(27, "Card components."),

	/** Component. */
	Component(28, "Component concepts."),

	/** Domino. */
	Domino(29, "Domino component concepts."),
	
	/** Dice. */
	Dice(30, "Dice component concepts."),

	/** Related to stochastic. */
	Stochastic(31, "Stochastic concepts."),

	/** Related to hidden information. */
	Hidden(32, "Hidden information concepts."),

	/** Related to promotion. */
	Promotion(33, "Promotion move concepts."),

	/** Related to a puzzle. */
	Puzzle(34, "Puzzle games."),

	/** Related to a CSP. */
	CSP(35, "Constraint Satisfaction problems."),

	/** Container. */
	Container(36, "Container concepts."),

	/** Pattern. */
	Pattern(37, "Pattern concepts."),

	/** Path. */
	Path(38, "Path concepts."),

	/** Territory. */
	Territory(39, "Territory concepts."),

	/** Phase. */
	Phase(40, "Play Phase concepts."),

	/** LargePiece. */
	LargePiece(41, "Large piece concepts."),

	/** Tile. */
	Tile(42, "Tile piece concepts."),

	/** Score. */
	Score(43, "Score concepts."),

	/** Fill. */
	Fill(44, "Fill region concepts."),

	/** Rotation. */
	Rotation(45, "Rotated pieces."),

	/** Team. */
	Team(46, "Team concepts."),

	/** Track. */
	Track(47, "Track concepts."),

	/** Push. */
	Push(48, "Push move concepts."),

	/** The line of sight. */
	LineOfSight(49, "Line of sight concepts."),

	/** Stack. */
	Stack(50, "Stack concepts."),

	/** State. */
	State(51, "Game state concepts."),

	/** Graph. */
	Graph(52, "Graph concepts."),

	/** Flip. */
	Flip(53, "Flip move concepts."),

	/** Swap. */
	Swap(54, "Swap move concepts."),

	/** Repetition. */
	Repetition(55, "Repetition state concepts."),
	
	/** Amount. */
	Amount(56, "Amount data concepts."),

	/** Random. */
	Random(57, "Random concepts."),

	/** Placement. */
	Placement(58, "Initial Placement starting rules."),

	/** Hint. */
	Hint(59, "Board with hints."),

	/** Reach. */
	Reach(60, "Reach a region concepts."),

	/** Chess. */
	Chess(61, "Chess games."),

	/** Style of the board. */
	Style(62, "Style of the board."),

	/** Match. */
	Match(63, "Match concepts."),

	/** Pot. */
	Pot(64, "Pot concepts."),

	/** Direction. */
	Direction(65, "Direction concepts."),

	/** Piece Value. */
	PieceValue(66, "Piece value concepts."),

	/** Relative Direction. */
	RelativeDirection(67, "Relative Direction concepts."),

	/** Absolute Direction. */
	AbsoluteDirection(68, "Absolute Direction concepts."),

	/** Piece Count. */
	PieceCount(69, "Piece count concepts."),

	/** Column. */
	Column(70, "Column concepts."),

	/** Row. */
	Row(71, "Row concepts."),

	/** Board. */
	Board(72, "Board concepts."),

	/** Distance. */
	Distance(73, "Distance concepts."),

	/** Shogi. */
	Shogi(74, "Shogi games."),

	/** Xiangqi. */
	Xiangqi(75, "Xiangqi games."),

	/** Algebra. */
	Algebra(76, "Algebra."),

	/** Arithmetic. */
	Arithmetic(77, "Arithmetic."),

	/** Algorithmic. */
	Algorithmic(78, "Algorithmic."),

	/** Logical Operators. */
	LogicalOperator(79, "Logical Operators."),

	/** Comparison Operators. */
	ComparisonOperator(80, "Comparison Operators."),

	/** Set Operators. */
	SetOperator(81, "Set Operators."),

	/** Involves friend pieces. */
	Friend(82, "Involves friend pieces."),
	;

	//-------------------------------------------------------------------------

	/** The id of the keyword. */
	final int id;
	
	/** The description of the concept keyword. */
	final String description;

	//-------------------------------------------------------------------------

	/**
	 * To create a new keyword.
	 * 
	 * @param id  The id of the keyword.
	 */
	private ConceptKeyword
	(
		final int id,
		final String description
	)
	{
		this.id = id;
		this.description = description;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The id of the keyword.
	 */
	public int id()
	{
		return this.id;
	}
	
	/**
	 * @return The plain English description of the game concept keyword.
	 */
	public String description()
	{
		return description;
	}
}
