package other.concept;

/**
 * Defines known concepts used in a game.
 * 
 * Remarks: The documentation on top of each concept explains how the concept is
 * computed if that's leaf. If that's not a leaf, the concept is true if any
 * child is true.
 * 
 * Remarks: The id is for the moment the ordinal until the concepts are more
 * stable to replace it with the id in the constructor of each concept which
 * will be fixed.
 * 
 * @author Eric.Piette
 */
public enum Concept
{
	// -------------------------------------------------------------------------
    //                                 Properties
	// -------------------------------------------------------------------------
	
	/** */
	Properties
	(
		"1",
		1,
		"General properties of the game.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		false,
		null
	),
	
	/** */
	Format
	(
			"1.1",
			2,
			"Format of the game.",
			ConceptType.Properties, 
			ConceptDataType.BooleanData,
			new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
			false,
			Concept.Properties
	),
	
	/** */
	Time
	(
		"1.1.1",
		3,
		"Time model.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		false,
		Concept.Format
	),
	
	/** True if the mode is not realtime (which is all games which are not simulation). */
	Discrete
	(
		"1.1.1.1",
		4,
		"Players move at discrete intervals.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		true,
		Concept.Time
	),
	
	/** True if the mode is realtime (which is only the simulation for now). */
	Realtime
	(
		"1.1.1.2",
		5,
		"Moves not discrete.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		true,
		Concept.Time
	),
	
	/** */
	Turns
	(
		"1.1.2",
		6,
		"Player turns.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		false,
		Concept.Format
	),
	
	/** True if the mode is Alternating. */
	Alternating
	(
		"1.1.2.1",
		7,
		"Players take turns moving.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		true,
		Concept.Turns
	),
	
	/** True if the mode is Simultaneous. */
	Simultaneous
	(
		"1.1.2.2",
		8,
		"Players can move at the same time.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		true,
		Concept.Turns
	),
	
	/** All games involving Dice (to throw), cards or dominoes. */
	Stochastic
	(
		"1.1.3",
		9, 
		"Game involves chance elements.",
		ConceptType.Properties,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Format
	),
	
	/** Use hidden information. */
	HiddenInformation
	(
		"1.1.4",
		45, 
		"Game involves hidden information.",
		ConceptType.Properties,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Format
	),
	
	/** All games with subgames. */
	Match
	(
		"1.1.5",
		10,
		"Match game.",
		ConceptType.Properties,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Format
	),
	
	/** */
	Asymmetric
	(
		"1.1.6",
		11,
		"Asymmetry in rules and/or forces.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		false,
		Concept.Format
	),
	
	/** */
	AsymmetricRules
	(
		"1.1.6.1",
		12, 
		"Players have different rules.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.Asymmetric
	),
	
	/** TO DO */
	AsymmetricPlayRules
	(
		"1.1.6.1.1",
		13, 
		"Players have different play rules.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.AsymmetricRules
	),
	
	/** TO DO */
	AsymmetricEndRules
	(
		"1.1.6.1.2",
		14, 
		"Players have different end rules.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.AsymmetricRules
	),
	
	/** */
	AsymmetricForces
	(
		"1.1.6.2",
		15, 
		"Players have different forces.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.Asymmetric
	),
	
	/** TO DO. */
	AsymmetricSetup
	(
		"1.1.6.2.1",
		16, 
		"Different starting positions for each player.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.AsymmetricForces
	),
	
	/** True if any piece type is owned by a player and not all the others. */
	AsymmetricPiecesType
	(
		"1.1.6.2.2",
		17, 
		"Different piece types owned by each player.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.AsymmetricForces
	),
	
	/** */
	Players
	(
		"1.2",
		18, 
		"Players of the game.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.Properties
	),
	
	/** Number of players. */
	NumPlayers
	(
		"1.2.1",
		19,
		"Number of players.",
		ConceptType.Properties,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		false,
		Concept.Players
	),
	
	/** The mode is simulation. */
	Simulation
	(
		"1.2.1.1",
		20,
		"No players (environment runs the game).",
		ConceptType.Properties,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.NumPlayers
	),
	
	/** Number of players = 1. */
	Solitaire
	(
		"1.2.1.2",
		21,
		"Single player.",
		ConceptType.Properties,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.NumPlayers
	),
	
	/** Number of players = 2. */
	TwoPlayer
	(
		"1.2.1.3",
		22,
		"Two players.",
		ConceptType.Properties,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.NumPlayers
	),
	
	/** Number of players > 2. */
	Multiplayer
	(
		"1.2.1.4",
		23,
		"More than two players.",
		ConceptType.Properties,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.NumPlayers
	),
	
	/** */
	Cooperation
	(
		"1.3",
		24, 
		"Players have to cooperate.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.Properties
	),
	
	/** A (set Team ...) ludeme is used. */
	Team
	(
		"1.3.1",
		25, 
		"Game involves teams of players.",
		ConceptType.Properties,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Cooperation
	),
	
	/** A (set Team ...) ludeme is used in the play rules. */
	Coalition
	(
		"1.3.2",
		26, 
		"Players may form coalitions.",
		ConceptType.Properties,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Cooperation
	),
	
	/** */
	Puzzle
	(
		"1.4",
		27, 
		"Type of puzzle.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.Properties
	),
	
	/** True if any ludeme used only for deduction puzzle is used. */
	DeductionPuzzle
	(
		"1.4.1",
		28, 
		"Solution can be deduced.",
		ConceptType.Properties,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Puzzle
	),
	
	/** All the 1 player game which are not deduction puzzles. */
	PlanningPuzzle
	(
		"1.4.2",
		29, 
		"Solution is reached in moving pieces.",
		ConceptType.Properties,
		ConceptDataType.BooleanData,
		new ConceptPurpose[]{ ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Puzzle
	),
	
	// -------------------------------------------------------------------------
    //                                 Equipment
	// -------------------------------------------------------------------------
	
	/** */
	Equipment
	(
		"2",
		30, 
		"Equipment for playing the game.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		null
	),
	
	/** */
	Container
	(
		"2.1",
		31, 
		"Containers that hold components.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.Equipment
	),
	
	/** */
	Board
	(
		"2.1.1",
		32, 
		"Board shared by player for playing the game.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.Container
	),
	
	/** */
	Shape
	(
		"2.1.1.1",
		33, 
		"The shape of the board.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.Board
	),
	
	/** True if a sub ludeme correspond to a square shape. */
	SquareShape
	(
		"2.1.1.1.1",
		78, 
		"Square shape.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Shape
	),
	
	/** True if a sub ludeme correspond to a hexagonal shape. */
	HexShape
	(
		"2.1.1.1.2",
		79, 
		"Hexagonal shape.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Shape
	),
	
	/** True if a sub ludeme correspond to a triangle shape. */
	TriangleShape
	(
		"2.1.1.1.3",
		80, 
		"Triangle shape.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Shape
	),
	
	/** True if a sub ludeme correspond to a diamond shape. */
	DiamondShape
	(
		"2.1.1.1.4",
		81, 
		"Diamond shape.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Shape
	),
	
	/** True if a sub ludeme correspond to a rectangle shape. */
	RectangleShape
	(
		"2.1.1.1.5",
		82, 
		"Rectangle shape.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.Shape
	),
	
	/** True if a sub ludeme correspond to a spiral shape. */
	SpiralShape
	(
		"2.1.1.1.6",
		83, 
		"Spirale shape.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.Shape
	),
	
	/** True if a sub ludeme correspond to a circle shape. */
	CircleShape
	(
		"2.1.1.1.7",
		84, 
		"Circle shape.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Shape
	),
	
	/** True if a sub ludeme correspond to a prism shape. */
	PrismShape
	(
		"2.1.1.1.8",
		85, 
		"Prism shape.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Shape
	),
	
	/** True if a sub ludeme correspond to a star shape. */
	StarShape(
		"2.1.1.1.9",
		86, 
		"Star shape.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Shape
	),
	
	/** True if a sub ludeme correspond to a parallelogram shape. */
	ParallelogramShape(
		"2.1.1.1.10",
		87, 
		"Parallelogram shape.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Shape
	),
	
	/** True if a sub ludeme correspond to a square shape with pyramidal at true. */
	SquarePyramidalShape(
		"2.1.1.1.11",
		78, 
		"Square Pyramidal shape.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Shape
	),
	
	/** True if a sub ludeme correspond to a rectangle shape with pyramidal at true. */
	RectanglePyramidalShape
	(
		"2.1.1.1.12",
		78, 
		"Rectangle Pyramidal shape.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Shape
	),
	
	/** Board shape is based on a regular polygon. */
	RegularShape
	(
		"2.1.1.1.13",
		378, 
		"Regular shape.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Shape
	),
	
	/** Board shape is based on a general polygon. */
	PolygonShape
	(
		"2.1.1.1.14",
		379, 
		"General polygonal shape.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Shape
	),
	
	/** Board shape is concentric set of rings like a target. */
	TargetShape
	(
		"2.1.1.1.15",
		380, 
		"Target shape.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Shape
	),
	
	/** */
	Tiling
	(
		"2.1.1.2",
		34, 
		"The shape of the board.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.Board
	),
	
	/** True if any ludeme uses a square tiling. */
	SquareTiling
	(
		"2.1.1.2.1",
		35, 
		"Square tiling.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Tiling
	),
	
	/** True if any ludeme uses a hexagonal tiling. */
	HexTiling
	(
		"2.1.1.2.2",
		36, 
		"Hexagonal tiling.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Tiling
	),
	
	/** True if any ludeme uses a triangle tiling. */
	TriangleTiling
	(
		"2.1.1.2.3",
		37, 
		"Triangle tiling.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Tiling
	),
	
	/** True if any ludeme uses a brick tiling. */
	BrickTiling
	(
		"2.1.1.2.4",
		38, 
		"Brick tiling.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Tiling
	),
	
	/** True if a Semi regular tiling is used. */
	SemiRegularTiling
	(
		"2.1.1.2.5",
		39, 
		"Semi regular tiling.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Tiling
	),
	
	/** True if any ludeme uses a celtic tiling. */
	CelticTiling
	(
		"2.1.1.2.6",
		40, 
		"Celtic tiling.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Tiling
	),
	
	/** True if any ludeme uses a morris tiling. */
	MorrisTiling
	(
		"2.1.1.2.7",
		41, 
		"Morris tiling.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Tiling
	),
	
	/** True if any ludeme uses a quadHex tiling. */
	QuadHexTiling
	(
		"2.1.1.2.8",
		42, 
		"QuadHex tiling.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Tiling
	),
	
	/** True if any ludeme uses a circle tiling. */
	CircleTiling
	(
		"2.1.1.2.9",
		43, 
		"Circle tiling.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Tiling
	),
	
	/** True if any ludeme uses a concentric tiling. */
	ConcentricTiling
	(
		"2.1.1.2.10",
		377, 
		"Concentric tiling.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Tiling
	),
	
	/** True if any ludeme uses a spiral tiling. */
	SpiralTiling
	(
		"2.1.1.2.11",
		44, 
		"Spiral tiling.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Tiling
	),
	
	/** True if any ludeme uses an alquerque tiling. */
	AlquerqueTiling
	(
		"2.1.1.2.12",
		45, 
		"Alquerque tiling.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Tiling
	),
	
	/** (mancalaBoard ...) is used. */
	MancalaBoard
	(
		"2.1.1.3",
		35, 
		"Mancala board.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.Board
	),
	
	/** (mancalaBoard ...) is used with stores not None. */
	MancalaStores
	(
		"2.1.1.3.1",
		35, 
		"Mancala board with stores.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.MancalaBoard
	),
	
	/** (mancalaBoard ...) is used using 2 rows. */
	MancalaTwoRows
	(
		"2.1.1.3.2",
		35, 
		"Mancala board with 2 rows.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.MancalaBoard
	),
	
	/** (mancalaBoard ...) is used using 3 rows. */
	MancalaThreeRows
	(
		"2.1.1.3.3",
		35, 
		"Mancala board with 3 rows.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.MancalaBoard
	),
	
	/** (mancalaBoard ...) is used using 4 rows. */
	MancalaFourRows
	(
		"2.1.1.3.4",
		35, 
		"Mancala board with 4 rows.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.MancalaBoard
	),
	
	/** (mancalaBoard ...) is used using 6 rows. */
	MancalaSixRows
	(
		"2.1.1.3.5",
		35, 
		"Mancala board with 6 rows.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.MancalaBoard
	),
	
	/** (mancalaBoard ...) is used using with a circular tiling */
	MancalaCircular
	(
		"2.1.1.3.6",
		35, 
		"Mancala board with circular tiling.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.MancalaBoard
	),
	
	/** The track list of the board is not empty. */
	Track
	(
		"2.1.1.4",
		35, 
		"The board has a track.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.Board
	),
	
	/** One track uses a loop. */
	TrackLoop
	(
		"2.1.1.4.1",
		35, 
		"A track is a loop.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Track
	),
	
	/** One track uses a loop. */
	TrackOwned
	(
		"2.1.1.4.2",
		35, 
		"A track is owned.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Track
	),
	
	/** The ludeme (hints ...) is used. */
	Hints
	(
		"2.1.1.5", 
		103,
		"The board has some hints.", 
		ConceptType.Equipment, 
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** The list of regions is not empty. */
	Region
	(
		"2.1.1.6",
		36, 
		"The board has regions.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Board
	),
	
	/** The ludeme (boardless ...) is used. */
	Boardless
	(
		"2.1.1.7",
		37, 
		"Game is played on an implied grid.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Board
	),
	
	/** */
	PlayableSites
	(
		"2.1.1.8",
		38, 
		"Playable sites.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.Board
	),
	
	/** SiteType = Vertex in at least a ludeme. */
	Vertex
	(
		"2.1.1.8.1",
		89, 
		"Use Vertices.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI, }, 
		true,
		Concept.PlayableSites
	),
	
	/** SiteType = Cell in at least a ludeme. */
	Cell
	(
		"2.1.1.8.2",
		90, 
		"Use cells.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI, }, 
		true,
		Concept.PlayableSites
	),
	
	/** SiteType = Edge in at least a ludeme. */
	Edge
	(
		"2.1.1.8.3",
		91, 
		"Use edges.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI, }, 
		true,
		Concept.PlayableSites
	),
	
	/** Number of playables sites on the board. */
	NumPlayableSitesOnBoard
	(
		"2.1.1.8.4",
		126, 
		"Number of playables sites on the board.",
		ConceptType.Equipment, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.PlayableSites
	),
	
	/** Number of columns of the board. */
	NumColumns
	(
		"2.1.1.9",
		172, 
		"Number of columns of the board.",
		ConceptType.Equipment, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of rows of the board. */
	NumRows
	(
		"2.1.1.10",
		173, 
		"Number of rows of the board.",
		ConceptType.Equipment, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[]{ ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of corners of the board. */
	NumCorners
	(
		"2.1.1.11",
		174, 
		"Number of corners of the board.",
		ConceptType.Equipment, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Average number of directions of each playable site on the board. */
	NumDirections
	(
		"2.1.1.12",
		175, 
		"Average number of directions of each playable site on the board.",
		ConceptType.Equipment, 
		ConceptDataType.DoubleData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/**
	 * Average number of orthogonal directions of each playable site on the board.
	 */
	NumOrthogonalDirections
	(
		"2.1.1.13",
		176, 
		"Average number of orthogonal directions of each playable site on the board.",
		ConceptType.Equipment, 
		ConceptDataType.DoubleData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Average number of diagonal directions of each playable site on the board. */
	NumDiagonalDirections
	(
		"2.1.1.14",
		177, 
		"Average number of diagonal directions of each playable site on the board.",
		ConceptType.Equipment, 
		ConceptDataType.DoubleData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Average number of adjacent directions of each playable site on the board. */
	NumAdjacentDirections
	(
		"2.1.1.15",
		178, 
		"Average number of adjacent directions of each playable site on the board.",
		ConceptType.Equipment, 
		ConceptDataType.DoubleData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Average number of off diagonal directions of each playable site on the board. */
	NumOffDiagonalDirections
	(
		"2.1.1.16",
		179, 
		"Average number of off diagonal directions of each playable site on the board.",
		ConceptType.Equipment, 
		ConceptDataType.DoubleData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of outer sites of the board. */
	NumOuterSites
	(
		"2.1.1.17",
		180, 
		"Number of outer sites of the board.",
		ConceptType.Equipment, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of inner sites of the board. */
	NumInnerSites
	(
		"2.1.1.18",
		181, 
		"Number of inner sites of the board.",
		ConceptType.Equipment, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of layers of the board. */
	NumLayers
	(
		"2.1.1.19",
		182, 
		"Number of layers of the board.",
		ConceptType.Equipment, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of edges of the board. */
	NumEdges
	(
		"2.1.1.20",
		183, 
		"Number of edges of the board.",
		ConceptType.Equipment, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Board
	),
	
	/** Number of cells of the board. */
	NumCells
	(
		"2.1.1.21",
		184, 
		"Number of cells of the board.",
		ConceptType.Equipment, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of vertices of the board. */
	NumVertices
	(
		"2.1.1.22",
		185, 
		"Number of vertices of the board.",
		ConceptType.Equipment, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of perimeter sites of the board. */
	NumPerimeterSites
	(
		"2.1.1.23",
		186, 
		"Number of perimeter sites of the board.",
		ConceptType.Equipment, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of top sites of the board. */
	NumTopSites
	(
		"2.1.1.24",
		187, 
		"Number of top sites of the board.",
		ConceptType.Equipment, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of bottom sites of the board. */
	NumBottomSites
	(
		"2.1.1.25",
		188, 
		"Number of bottom sites of the board.",
		ConceptType.Equipment, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of right sites of the board. */
	NumRightSites
	(
		"2.1.1.26",
		189, 
		"Number of right sites of the board.",
		ConceptType.Equipment, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of left sites of the board. */
	NumLeftSites
	(
		"2.1.1.27",
		190, 
		"Number of left sites of the board.",
		ConceptType.Equipment, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of centre sites of the board. */
	NumCentreSites
	(
		"2.1.1.28",
		191, 
		"Number of centre sites of the board.",
		ConceptType.Equipment, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of convex corners of the board. */
	NumConvexCorners
	(
		"2.1.1.29",
		192, 
		"Number of convex corners of the board.",
		ConceptType.Equipment, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of concave corners of the board. */
	NumConcaveCorners
	(
		"2.1.1.30",
		193, 
		"Number of concave corners of the board.",
		ConceptType.Equipment, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	/** Number of phases of the board. */
	NumPhasesBoard
	(
		"2.1.1.31",
		194, 
		"Number of phases of the board.",
		ConceptType.Equipment, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** The ludeme (hand ...) is used. */
	Hand
	(
		"2.1.2",
		36, 
		"Player hands for storing own pieces.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Container
	),
	
	/** Number of containers. */
	NumContainers
	(
		"2.1.3",
		199, 
		"Number of containers.",
		ConceptType.Equipment, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Container
	),
	
	/** Sum of all the playable sites across all the containers */
	NumPlayableSites
	(
		"2.1.4",
		127, 
		"Number of playables sites in total.",
		ConceptType.Equipment, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Container
	),
	
	/** */
	Component
	(
		"2.2",
		37, 
		"Components manipulated by the players.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		false,
		Concept.Equipment
	),
	
	/** The ludeme (piece ...) is used. */
	Piece
	(
		"2.2.1",
		38, 
		"Game is played with pieces.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Component
	),
	
	/** A ludeme (set Value ...) is used. */
	PieceValue
	(
		"2.2.2",
		168, 
		"Pieces have value.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Component
	),
	
	/** A rotation state is set */
	PieceRotation
	(
		"2.2.3",
		132, 
		"Pieces have rotations.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Component
	),
	
	/** A direction is not null in at least a piece. */
	PieceDirection
	(
		"2.2.4",
		171, 
		"Pieces have forward direction.",
		ConceptType.Equipment,
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Component
	),
	
	/** A die is included in the components. */
	Dice
	(
		"2.2.5",
		39, 
		"Game is played with dice.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Component
	),
	
	/** Use biased dice. */
	BiasedDice
	(
		"2.2.6",
		201, 
		"Use biased dice.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Component
	),
	
	/** A card is included in the components. */
	Card
	(
		"2.2.7",
		40, 
		"Game is played with cards.",
		ConceptType.Equipment,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Component
	),
	
	/** A domino is included in the components. */
	Domino
	(
		"2.2.8",
		41, 
		"Game is played with dominoes.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Component
	),
	
	/** A large piece is included in the components. */
	LargePiece
	(
		"2.2.9",
		42, 
		"Game is played with large pieces.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Component
	),
	
	/** Use tiles. */
	Tile
	(
		"2.2.10",
		43, 
		"Game is played with tiles.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Component
	),
	
	/** Number of component types. */
	NumComponentsType
	(
		"2.2.11",
		195, 
		"Number of component types.",
		ConceptType.Equipment, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Component
	),
	
	/** Average number of component types per player. */
	NumComponentsTypePerPlayer
	(
		"2.2.12",
		196, 
		"Average number of component types per player.",
		ConceptType.Equipment, 
		ConceptDataType.DoubleData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Component
	),
	
	/** Number of dice. */
	NumDice
	(
		"2.2.13",
		197, 
		"Number of dice.",
		ConceptType.Equipment, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Component
	),
	
	// -------------------------------------------------------------------------
    //                                 Rules
	// -------------------------------------------------------------------------
	
	/** */
	Rules
	(
		"3",
		197, 
		"Rules of the game.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		null
	),
	
	// -------------------------------------------------------------------------
    //                                 MetaRules
	// -------------------------------------------------------------------------
	
	/** */
	Meta
	(
		"3.1",
		197, 
		"Global metarules that override all other rules.",
		ConceptType.Meta,
		ConceptDataType.BooleanData, new ConceptPurpose[]
		{ ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Rules
	),
	
	/** */
	OpeningContract
	(
		"3.1.1",
		197, 
		"Game involves an opening round equaliser.",
		ConceptType.Meta,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Meta
	),
	
	/** (swap) metarule is used. */
	SwapOption
	(
		"3.1.1.1",
		197, 
		"Second player may swap colours.",
		ConceptType.Meta, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.OpeningContract
	),
	
	/** */
	Repetition
	(
		"3.1.2",
		197, 
		"Game has repetition checks.",
		ConceptType.Meta,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Meta
	),
	
	/** (no Repeat InTurn) is used. */
	TurnKo
	(
		"3.1.2.1",
		98, 
		"No repeated piece positions within a single turn.",
		ConceptType.Meta,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Repetition
	),
	
	/** (no Repeat SituationalInTurn) is used. */
	SituationalTurnKo
	(
		"3.1.2.2",
		98, 
		"No repeated states withing a single turn.",
		ConceptType.Meta,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Repetition
	),
	
	/** (no Repeat InGame) is used. */
	PositionalSuperko
	(
		"3.1.2.3",
		98, 
		"No repeated piece positions.",
		ConceptType.Meta,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Repetition
	),
	
	/** (no Repeat Situational) is used. */
	SituationalSuperko
	(
		"3.1.2.4",
		98, 
		"No repeated states.",
		ConceptType.Meta, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Repetition
	),
	
	/** */
	AutoMove
	(
		"3.1.3",
		197, 
		"Apply all legal moves related to one single site.",
		ConceptType.Meta,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Meta
	),
	
	// -------------------------------------------------------------------------
    //                                 Start Rules
	// -------------------------------------------------------------------------
	
	/** */
	Start
	(
		"3.2",
		197, 
		"Start rules.",
		ConceptType.Start,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Rules
	),
	
	/**
	 * Places initially some pieces on the board.
	 */
	PiecesPlacedOnBoard
	(
		"3.2.1",
		105,
		"Places initially some pieces on the board.",
		ConceptType.Start, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Start
	),
	
	/**
	 * Places initially some pieces (different of shared dice) outside of the board.
	 */
	PiecesPlacedOutsideBoard
	(
		"3.2.2",
		106,
		"Places initially some pieces (different of shared dice) outside of the board.",
		ConceptType.Start, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Start
	),
	
	/** Places initially randomly some pieces. */
	InitialRandomPlacement
	(
		"3.2.3",
		102,
		"Places initially randomly some pieces.",
		ConceptType.Start, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Start
	),
	
	/** Sets initial score. */
	InitialScore
	(
		"3.2.4",
		100,
		"Sets initial score.",
		ConceptType.Start, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Start
	),
	
	/** Sets initial amount. */
	InitialAmount
	(
		"3.2.5",
		101,
		"Sets initial amount.",
		ConceptType.Start, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Start
	),
	
	/** Sets initial pot. */
	InitialPot
	(
		"3.2.6",
		140,
		"Sets initial pot.",
		ConceptType.Start, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Start
	),
	
	/** Sets initially some costs on graph elements. */
	InitialCost
	(
		"3.2.7",
		104,
		"Sets initially some costs on graph elements.",
		ConceptType.Start, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Start
	),
	
	/** Compute the number of pieces on the board at the start. */
	NumStartComponentsBoard
	(
		"3.2.8",
		104,
		"Number of components on board at start.",
		ConceptType.Start, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Start
	),
	
	/** Compute the number of pieces in the player hands at the start. */
	NumStartComponentsHand
	(
		"3.2.9",
		104,
		"Number of components in player hands at start.",
		ConceptType.Start, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Start
	),
	
	/** Compute the number of pieces at the start. */
	NumStartComponents
	(
		"3.2.10",
		104,
		"Number of components at start.",
		ConceptType.Start, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[]{ ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Start
	),
	
	// -------------------------------------------------------------------------
    //                                 Play Rules
	// -------------------------------------------------------------------------
	
	/** */
	Play
	(
		"3.3",
		197, 
		"Rules of general play.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Rules
	),
	
	/** */
	Moves
	(
		"3.3.1",
		197, 
		"Moves.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Play
	),
	
	/** */
	Add
	(
		"3.3.1.1", 
		60, 
		"Add move.", 
		ConceptType.Play, 
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false, 
		Concept.Moves
	),

	/** (move Add ...) is used. */
	AddDecision
	(
		"3.3.1.1.1",
		60, 
		"Decide to add pieces.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.Add
	),
	
	/** */
	Step
	(
		"3.3.1.2",
		20, 
		"Step move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		false,
		Concept.Moves
	),
	
	/** (Move Step ...) is used. */
	StepDecision
	(
		"3.3.1.2.1",
		20, 
		"Decide to step.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		true,
		Concept.Step
	),
	
	/** (is Empty ...) condition on (to...) of a step move. */
	StepToEmpty
	(
		"3.3.1.2.2",
		266, 
		"Decide to step to an empty site.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Step
	),

	/** (is Friend ...) condition on (to...) of a step move. */
	StepToFriend
	(
		"3.3.1.2.3",
		267, 
		"Decide to step to a friend piece.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Step
	),

	/** (is Enemy ...) condition on (to...) of a step move. */
	StepToEnemy
	(
		"3.3.1.2.4",
		268, 
		"Decide to step to an enemy piece.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Step
	),
	
	/**. */
	Slide
	(
		"3.3.1.3",
		19, 
		"Slide move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.Moves
	),
	
	/** (move Slide ...) is used */
	SlideDecision
	(
		"3.3.1.3.1",
		19, 
		"Decide to slide.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Slide
	),
	
	/** (move Slide ...) is used to move to empty sites. */
	SlideToEmpty
	(
		"3.3.1.3.2",
		19, 
		"Slide move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Slide
	),
	
	/**(move Slide ...) is used to move to enemy sites. */
	SlideToEnemy
	(
		"3.3.1.3.3",
		19, 
		"Slide move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Slide
	),
	
	/**(move Slide ...) is used to move to friend sites. */
	SlideToFriend
	(
		"3.3.1.3.4",
		19, 
		"Slide move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Slide
	),
	
	/** */
	Leap
	(
		"3.3.1.4",
		18, 
		"Leap move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.Moves
	),
	
	/** (move Leap ...) is used. */
	LeapDecision
	(
		"3.3.1.4.1",
		18, 
		"Decide to leap.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Leap
	),

	/** (is Empty ...) condition on (to...) of a leap move. */
	LeapToEmpty
	(
		"3.3.1.4.2",
		269, 
		"Decide to leap to an empty site.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Leap
	),

	/** (is Friend ...) condition on (to...) of a leap move. */
	LeapToFriend
	(
		"3.3.1.4.3",
		270, 
		"Decide to leap to a friend piece.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Leap
	),

	/** (is Enemy ...) condition on (to...) of a leap move. */
	LeapToEnemy
	(
		"3.3.1.4.4",
		271, 
		"Decide to leap to an enemy piece.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Leap
	),
	
	/** */
	Hop
	(
		"3.3.1.5",
		500, 
		"Hop move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.Moves
	),
	
	/** True if a (move Hop ...) is used. */
	HopDecision
	(
		"3.3.1.5.1",
		500, 
		"Decide to hop.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Hop
	),
	
	/** True if min range is greater than 1 in any hop decision move. */
	HopMoreThanOne
	(
		"3.3.1.5.2",
		63, 
		"Hop more than one site.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Hop
	),
	
	/** Hop move with (is Enemy ...) condition in the between and (is Empty ...) in the to. */
	HopEnemyToEmpty
	(
		"3.3.1.5.3",
		260, 
		"Hop an enemy to an empty site.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Hop
	),
	
	/** Decide to hop a friend piece to an empty site. */
	HopFriendToEmpty
	(
		"3.3.1.5.4",
		261, 
		"Hop a friend to an empty site.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Hop
	),
	
	/** Decide to hop an enemy piece to a friend piece. */
	HopEnemyToFriend
	(
		"3.3.1.5.5",
		262, 
		"Hop an enemy to a friend piece.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Hop
	),
	
	/** Decide to hop a friend piece to a friend piece. */
	HopFriendToFriend
	(
		"3.3.1.5.6",
		263, 
		"Hop a friend to a friend piece.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[]{ ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Hop
	),
	
	/** Decide to hop an enemy piece to an enemy piece. */
	HopEnemyToEnemy
	(
		"3.3.1.5.7",
		264, 
		"Hop an enemy to a enemy piece.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Hop
	),
	
	/** Decide to hop a friend piece to an enemy piece. */
	HopFriendToEnemy
	(
		"3.3.1.5.8",
		265, 
		"Hop a friend to an enemy piece.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Hop
	),
	
	/** The ludeme (sow ...) is used. */
	Sow
	(
		"3.3.1.6",
		11, 
		"Sowing stones.", 
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.Moves
	),
	
	/** The ludeme (sow ...) is used with an effect. */
	SowEffect
	(
		"3.3.1.6.1",
		11, 
		"Sowing moves with effect on final hole.", 
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.Sow
	),
	
	/** The effect of the sow move is to move the captured stones to a store or an hand. */
	SowCapture
	(
		"3.3.1.6.1.1",
		11, 
		"Sowing with capture.", 
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.SowEffect
	),
	
	/** The ludeme (sow ...) is used. */
	SowRemove
	(
		"3.3.1.6.1.2",
		11, 
		"Sowing with seeds removed.", 
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.SowEffect
	),
	
	/** The ludeme (sow ...) is used with backtracking at true. */
	SowBacktracking
	(
		"3.3.1.6.1.3",
		11, 
		"Sowing uses backtracking captures.", 
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.SowEffect
	),
	
	/** Properties of the sow moves (origin, skip, etc...) . */
	SowProperties
	(
		"3.3.1.6.2",
		11, 
		"Sowing properties.", 
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.Sow
	),
	
	/** The ludeme (sow ...) is used but skip some holes. */
	SowSkip
	(
		"3.3.1.6.2.1",
		11, 
		"Sowing in skiping some holes.", 
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.SowProperties
	),
	
	/** The ludeme (sow ...) sow first in the origin. */
	SowOriginFirst
	(
		"3.3.1.6.2.2",
		11, 
		"Sowing in the origin hole first.", 
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		true,
		Concept.SowProperties
	),
	
	/** The track used to sow is CW. */
	SowCW
	(
		"3.3.1.6.2.3",
		11, 
		"Sowing is performed CW.", 
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		true,
		Concept.SowProperties
	),
	
	/** The track used to sow is CCW. */
	SowCCW
	(
		"3.3.1.6.2.4",
		11, 
		"Sowing is performed CCW.", 
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		true,
		Concept.SowProperties
	),
	
	/** */
	Bet
	(
		"3.3.1.7",
		21, 
		"Bet move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		false,
		Concept.Moves
	),
	
	/** Decide to bet. */
	BetDecision
	(
		"3.3.1.7.1",
		21, 
		"Decide to bet.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		true,
		Concept.Bet
	),
	
	/** */
	Vote
	(
		"3.3.1.8",
		22, 
		"Vote move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		false,
		Concept.Moves
	),
	
	/** Decide to vote. */
	VoteDecision
	(
		"3.3.1.8.1",
		22, 
		"Decide to vote.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		true,
		Concept.Vote
	),
	
	/** (promote ...) or (move Promote ...) is used. */
	Promotion
	(
		"3.3.1.9",
		26, 
		"Promote move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Moves
	),
	
	/** Any child concept is true or (remove ...) is used. */
	Remove
	(
		"3.3.1.10",
		49, 
		"Remove move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.Moves
	),
	
	/** (move Remove ...) is used. */
	RemoveDecision
	(
		"3.3.1.10.1",
		56, 
		"Decide to remove pieces.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Remove
	),
	
	/** (move (from ...) (to....)) or (fromTo ...) is used. */
	FromTo
	(
		"3.3.1.11",
		50, 
		"Move a piece from a site to another.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.Moves
	),
	
	/** (move (from ...) (to....)). */
	FromToDecision
	(
		"3.3.1.11.1",
		50, 
		"Decide to move a piece from a site to another.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.FromTo
	),
	
	/** Moves concepts. */
	FromToWithinBoard
	(
		"3.3.1.11.2",
		50, 
		"Move a piece from a site to another withing the board.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.FromTo
	),
	
	/** Moves concepts. */
	FromToBetweenContainers
	(
		"3.3.1.11.3",
		50, 
		"Move a piece from a site to another between 2 different containers.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.FromTo
	),
	
	/** Moves concepts. */
	FromToEmpty
	(
		"3.3.1.11.4",
		50, 
		"Move a piece to an empty site.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.FromTo
	),
	
	/** Moves concepts. */
	FromToEnemy
	(
		"3.3.1.11.5",
		50, 
		"Move a piece to an enemy site.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.FromTo
	),
	
	/** Moves concepts. */
	FromToFriend
	(
		"3.3.1.11.6",
		50, 
		"Move a piece to a friend site.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.FromTo
	),
	
	/** (set Direction ...) is used. */
	Rotation
	(
		"3.3.1.12",
		46, 
		"Rotation move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Moves
	),
	
	/** (push ...) is used. */
	Push
	(
		"3.3.1.13",
		65, 
		"Push move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Moves
	),
	
	/** (flip ...) is used. */
	Flip
	(
		"3.3.1.14",
		94, 
		"Flip move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Moves
	),
	
	/** */
	SwapPieces
	(
		"3.3.1.15",
		96, 
		"Swap pieces move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		false,
		Concept.Moves
	),
	
	/** (move Swap Piece ...) is used. */
	SwapPiecesDecision
	(
		"3.3.1.15.1",
		96, 
		"Decide to swap pieces.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.SwapPieces
	),
	
	/** Decide to swap players. */
	SwapPlayers
	(
		"3.3.1.16",
		97, 
		"Swap players move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Moves
	),
	
	/** Decide to swap players. */
	SwapPlayersDecision
	(
		"3.3.1.16.1",
		97, 
		"Decide to swap players.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.SwapPlayers
	),
	
	/** Take control of enemy pieces. */
	TakeControl(
		"3.3.1.17",
		129,
		"Take control of enemy pieces.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Moves
	),
	
	/** */
	Shoot(
		"3.3.1.18",
		138,
		"Decide to shoot.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Moves
	),
	
	/** (move Shoot .... is used). */
	ShootDecision
	(
		"3.3.1.18.1",
		138,
		"Decide to shoot.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Shoot
	),
	
	/** (priority ...) is used. */
	Priority
	(
		"3.3.1.19",
		61, 
		"Some moves are priority.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Moves
	),
	
	/** (forEach Die ...) is used. */
	ByDieMove
	(
		"3.3.1.20",
		62, 
		"Each die can correspond to a different move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Moves
	),
	
	/** (max Moves ...). */
	MaxMovesInTurn
	(
		"3.3.1.21",
		238, 
		"Maximise the number of moves in a turn.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Moves
	),
	
	/** (max Distance ..). */
	MaxDistance
	(
		"3.3.1.22",
		240, 
		"Maximise the distance to move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Moves
	),
	
	/**  */
	SetMove
	(
		"3.3.1.23",
		240, 
		"Set Moves.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Moves
	),
	
	/** (move Set NextPlayer ..). */
	SetNextPlayer
	(
		"3.3.1.23.1",
		240, 
		"Decide who is the next player.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.SetMove
	),
	
	/** (moveAgain). */
	MoveAgain
	(
		"3.3.1.23.2",
		240, 
		"Set the next player to the mover.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.SetMove
	),
	
	/** (set Value ..). */
	SetValue
	(
		"3.3.1.23.3",
		240, 
		"Set the value of a piece.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.SetMove
	),
	
	/** (set Count ..). */
	SetCount
	(
		"3.3.1.23.4",
		240, 
		"Set the count of a piece.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.SetMove
	),
	
	/** (move Set TrumpSuit ..). */
	ChooseTrumpSuit
	(
		"3.3.1.24",
		240, 
		"Choose the trump suit.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Moves
	),
	
	/** (pass ...) or (move Pass ...) is used. */
	Pass
	(
		"3.3.1.25",
		240, 
		"Pass a turn.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Moves
	),
	
	/** (pass ...) or (move Pass ...) is used. */
	PassDecision
	(
		"3.3.1.25.1",
		240, 
		"Decide to pass a turn.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Pass
	),
	
	/** (roll ...) is used. */
	Roll
	(
		"3.3.1.26",
		240, 
		"Roll at least a die.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Moves
	),
	
	/** True if any child is true. */
	GraphMoves
	(
		"3.3.1.27",
		240, 
		"Graph moves.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Moves
	),
	
	/** (set Cost ...). */
	SetCost
	(
		"3.3.1.27.1",
		240, 
		"Set the cost of a graph element.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.GraphMoves
	),
	
	/** (set Phase ...). */
	SetPhase
	(
		"3.3.1.27.2",
		240, 
		"Set the phase of a graph element.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.GraphMoves
	),
	
	/** */
	Propose
	(
		"3.3.1.28",
		22, 
		"Propose a vote.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		false,
		Concept.Moves
	),
	
	/** Decide to propose. */
	ProposeDecision
	(
		"3.3.1.28.1",
		22, 
		"Decide to propose.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		true,
		Concept.Propose
	),
	
	/** */
	Capture
	(
		"3.3.2",
		197, 
		"Game involved captures.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Play
	),
	
	/** Replacement captures. */
	ReplacementCapture
	(
		"3.3.2.1",
		54, 
		"Capture in replacing.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Capture
	),
	
	/** True if a Remove move is done in the effect of a hop move. */
	HopCapture
	(
		"3.3.2.2",
		57, 
		"Capture in hopping.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Capture
	),
	
	/** True if a Remove move is done in the effect of a hop move and if the min range is greater than one. */
	HopCaptureMoreThanOne
	(
		"3.3.2.3",
		64, 
		"Capture in hopping many sites.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Capture
	),
	
	/** (directionCapture ...) is used. */
	DirectionCapture
	(
		"3.3.2.4",
		51, 
		"Capture pieces in a direction.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Capture
	),
	
	/** (enclose ...) is used. */
	EncloseCapture
	(
		"3.3.2.5",
		52, 
		"Capture in enclosing.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Capture
	),
	
	/** (custodial ...) is used. */
	CustodialCapture
	(
		"3.3.2.6",
		53, 
		"Capture in custodial.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Capture
	),
	
	/** (intervene ...) is used. */
	InterveneCapture
	(
		"3.3.2.7",
		55, 
		"Intervene capture.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Capture
	),
	
	/** (surround ...) is used. */
	SurroundCapture
	(
		"3.3.2.8",
		58, 
		"Ccapture in surrounding.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Capture
	),
	
	/** when: parameter is not null in (remove ...) */
	CaptureSequence
	(
		"3.3.2.9",
		134,
		"Capture pieces in a sequence at the end of the turn.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Capture
	),
	
	/** (max Capture ...) is used. */
	MaxCapture
	(
		"3.3.2.10",
		239, 
		"Maximise the number of captures.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Capture
	),
	
	/** */
	Conditions
	(
		"3.3.3",
		197, 
		"Conditions checked.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Play
	),
	
	/** */
	SpaceConditions
	(
		"3.3.3.1",
		197, 
		"Space conditions.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Conditions
	),
	
	/** (is Line ...) is used. */
	Line
	(
		"3.3.3.1.1",
		3000, 
		"Line Detection.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.SpaceConditions
	),
	
	/** (is Connected ...) is used. */
	Connection
	(
		"3.3.3.1.2",
		7, 
		"Connected regions detection.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.SpaceConditions
	),
	
	/** (is Group ...), (count Group ...), (sites Group) or (forEach Group ...) is used. */
	Group
	(
		"3.3.3.1.3",
		9, 
		"Detect a group.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.SpaceConditions
	),
	
	/** (is In ...) is used. */
	Contains
	(
		"3.3.3.1.4",
		1000,
		"Detect if a site is in a region.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		true,
		Concept.SpaceConditions
	),
	
	/** (is Loop ...) or (sites Loop ...) is used. */
	Loop
	(
		"3.3.3.1.5",
		30, 
		"Loop detection.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.SpaceConditions
	),
	
	/** (sites Pattern ...) or (is Pattern ...) is used. */
	Pattern
	(
		"3.3.3.1.6",
		32, 
		"Pattern detection.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.SpaceConditions
	),
	
	/** (pathExtent ...) is used. */
	PathExtent
	(
		"3.3.3.1.7",
		34, 
		"Path extent detection.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.SpaceConditions
	),
	
	/** (size Territory ...) is used. */
	Territory
	(
		"3.3.3.1.8",
		36, 
		"Territory detection.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.SpaceConditions
	),
	
	/** (= (sites Occupied by:....) <RegionFunction>). */
	Fill
	(
		"3.3.3.1.9",
		43, 
		"Check region filled by pieces.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.SpaceConditions
	),
	
	/** (Count Steps ...) is used. */
	Distance
	(
		"3.3.3.1.10",
		202, 
		"Check distance between two sites.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.SpaceConditions
	),
	
	/** */
	MoveConditions
	(
		"3.3.3.2",
		197, 
		"Move conditions.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Conditions
	),
	
	/** Detect stalemate. */
	Stalemate
	(
		"3.3.3.2.1",
		501, 
		"Detect stalemate.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.MoveConditions
	),
	
	/** (can Move ...) used. Put to false if CanNotMove is used on the same ludeme. */
	CanMove
	(
		"3.3.3.2.2",
		15, 
		"Check if a piece (or more) can make specific move(s).",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.MoveConditions
	),
	
	/** (not (can Move ...)) used. */
	CanNotMove
	(
		"3.3.3.2.3",
		16, 
		"Check if a piece (or more) can not make specific move(s).",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.MoveConditions
	),
	
	/** */
	PieceConditions
	(
		"3.3.3.3",
		197, 
		"Piece conditions.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Conditions
	),
	
	/** (= 0 (count Pieces ...) ...) is used. */
	NoPiece
	(
		"3.3.3.3.1",
		12, 
		"No piece detection.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.PieceConditions
	),
	
	/** (= Off (where ...) ...) is used. */
	NoTargetPiece
	(
		"3.3.3.3.2", 
		12, 
		"No target piece detection.", 
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.PieceConditions
	),

	/** (is Threatened ...) is used. */
	Threat
	(
		"3.3.3.3.3",
		14, 
		"Piece under threat detection.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.PieceConditions
	),
	
	/** (is Empty ...) is used. */
	IsEmpty
	(
		"3.3.3.3.4",
		258, 
		"Empty site detection.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.PieceConditions
	),
	
	/** (is Enemy ...) is used. */
	IsEnemy
	(
		"3.3.3.3.5",
		257, 
		"Occupied site by enemy detection.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.PieceConditions
	),
	
	/** (is Friend ...) is used. */
	IsFriend
	(
		"3.3.3.3.6",
		259, 
		"Occupied site by friend detection.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.PieceConditions
	),
	
	/** (sites LineOfSight ...), (slide ...) or (shoot ...) is used.. */
	LineOfSight
	(
		"3.3.3.3.7",
		66, 
		"Line of sight of pieces used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.PieceConditions
	),
	
	/** True if the counter is used to check the progress of the game. */
	ProgressCheck
	(
		"3.3.3.4",
		197, 
		"Progress condition.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Conditions
	),
	
	/** */
	Directions
	(
		"3.3.4",
		197, 
		"Directions used.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		new ConceptPurpose[]{ ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Play
	),
	
	/** */
	AbsoluteDirections
	(
		"3.3.4.1",
		197, 
		"Absolute directions used.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Directions
	),
	
	/** All enum is used in the directions. */
	AllDirections
	(
		"3.3.4.1.1",
		145,
		"All directions used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.AbsoluteDirections
	),
	
	/** Adjacent enum is used. */
	AdjacentDirection
	(
		"3.3.4.1.2",
		143,
		"Adjacent directions used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.AbsoluteDirections
	),
	
	/** Orthogonal enum is used. */
	OrthogonalDirection
	(
		"3.3.4.1.3",
		142,
		"Orthogonal directions used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.AbsoluteDirections
	),
	
	/** Diagonal enum is used. */
	DiagonalDirection
	(
		"3.3.4.1.4",
		144,
		"Diagonal directions used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.AbsoluteDirections
	),
	
	/** OffDiagonal used. */
	OffDiagonalDirection
	(
		"3.3.4.1.5",
		146,
		"Off diagonal directions used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.AbsoluteDirections
	),
	
	/** Rotational enum used. */
	RotationalDirection
	(
		"3.3.4.1.6",
		147,
		"Rotational directions used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.AbsoluteDirections
	),
	
	/** SameLayer enum used. */
	SameLayerDirection
	(
		"3.3.4.1.7",
		148,
		"Same layer directions used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.AbsoluteDirections
	),
	
	/** */
	RelativeDirections
	(
		"3.3.4.2",
		197, 
		"Directions used.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Directions
	),
	
	/** Forward enum is used. */
	ForwardDirection
	(
		"3.3.4.2.1",
		149,
		"Forward direction used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.RelativeDirections
	),
	
	/** Backward enum is used. */
	BackwardDirection
	(
		"3.3.4.2.2",
		150,
		"Backward direction used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.RelativeDirections
	),
	
	/** Forwards enum is used. */
	ForwardsDirection
	(
		"3.3.4.2.3",
		151,
		"Forwards direction used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.RelativeDirections
	),
	
	/** Backwards enum is used. */
	BackwardsDirection
	(
		"3.3.4.2.4",
		152,
		"Backwards direction used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.RelativeDirections
	),
	
	/** Rightward enum is used. */
	RightwardDirection
	(
		"3.3.4.2.5",
		153,
		"Rightward direction used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.RelativeDirections
	),
	
	/** Leftward enum is used. */
	LeftwardDirection
	(
		"3.3.4.2.6",
		154,
		"Leftward direction used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.RelativeDirections
	),
	
	/** Rightwards enum is used. */
	RightwardsDirection
	(
		"3.3.4.2.7",
		155,
		"Rightwards direction used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.RelativeDirections
	),
	
	/** Leftwards enum is used. */
	LeftwardsDirection
	(
		"3.3.4.2.8",
		156,
		"Leftwards direction used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.RelativeDirections
	),
	
	/** FL enum is used. */
	ForwardLeftDirection
	(
		"3.3.4.2.9",
		157,
		"Forward left direction used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.RelativeDirections
	),
	
	/** FR enum is used. */
	ForwardRightDirection
	(
		"3.3.4.2.10",
		158,
		"Forward right direction used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.RelativeDirections
	),
	
	/** BL enum is used. */
	BackwardLeftDirection(
		"3.3.4.2.11",
		159,
		"Backward left direction used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.RelativeDirections
	),
	
	/** BR enum is used. */
	BackwardRightDirection
	(
		"3.3.4.2.12",
		160,
		"Use backward right direction.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.RelativeDirections
	),
	
	/** SameDirection is used. */
	SameDirection
	(
		"3.3.4.2.13",
		161,
		"Same direction of the previous move used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.RelativeDirections
	),
	
	/** OppositeDirection is used. */
	OppositeDirection
	(
		"3.3.4.2.14",
		162,
		"Opposite direction of the previous move used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.RelativeDirections
	),
	
	/** */
	Information
	(
		"3.3.5",
		197, 
		"Information.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Play
	),
	
	/** Any ludeme hiding the piece type. */
	HidePieceType
	(
		"3.3.5.1",
		110, 
		"Hide piece type.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Information
	),
	
	/** Any ludeme hiding the piece owner. */
	HidePieceOwner
	(
		"3.3.5.2",
		136, 
		"Hide piece owner.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },  
		true,
		Concept.Information
	),
	
	/** Any ludeme hiding the piece count. */
	HidePieceCount
	(
		"3.3.5.3",
		163, 
		"Hide number of pieces.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },  
		true,
		Concept.Information
	),
	
	/** Any ludeme hiding the piece rotation. */
	HidePieceRotation
	(
		"3.3.5.4",
		164, 
		"Hide piece rotation.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },  
		true,
		Concept.Information
	),
	
	/** Any ludeme hiding the piece value. */
	HidePieceValue
	(
		"3.3.5.5",
		165, 
		"Hide piece value.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Information
	),
	
	/** Any ludeme hiding the piece state. */
	HidePieceState
	(
		"3.3.5.6",
		167, 
		"Hide the site state.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },  
		true,
		Concept.Information
	),
	
	/** Any ludeme hidding all the info on a site. */
	InvisiblePiece
	(
		"3.3.5.7",
		166, 
		"Piece can be invisible.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },  
		true,
		Concept.Information
	),
	
	/** (phase ...) is used. */
	Phase
	(
		"3.3.6",
		197, 
		"Phases of play.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Play
	),
	
	/** Number of play phases. */
	NumPlayPhase
	(
		"3.3.6.1",
		198, 
		"Number of play phases.",
		ConceptType.Play, 
		ConceptDataType.IntegerData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Phase
	),
	
	/** (set Score ...) is used. */
	Scoring
	(
		"3.3.7",
		41, 
		"Involve scores.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Play
	),
	
	/** If any ludeme can make the count of a site to be bigger than 1 or if a stack of the same piece can be placed. */
	PieceCount
	(
		"3.3.8",
		169, 
		"Many pieces of the same type on a site.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Play
	),
	
	/** (count Pips) is used. */
	SumDice
	(
		"3.3.9",
		95, 
		"Use sum of all dice.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Play
	),
	
	
	// -------------------------------------------------------------------------
    //                                 End
	// -------------------------------------------------------------------------
	
	/** */
	End
	(
		"3.4",
		197, 
		"Rules for ending the game.",
		ConceptType.End,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Rules
	),
	
	/** */
	SpaceEnd
	(
		"3.4.1",
		197, 
		"Space ending rules.",
		ConceptType.End,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.End
	),
	
	/** Line concept true in an ending condition. */
	LineEnd(
		"3.4.1.1",
		4, 
		"End in making a line.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.SpaceEnd
	),
	
	/** Connection concept true in an ending condition. */
	ConnectionEnd
	(
		"3.4.1.2",
		8, 
		"End if connected regions.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.SpaceEnd
	),
	
	/** Group concept true in an ending condition. */
	GroupEnd
	(
		"3.4.1.3",
		10, 
		"End in making a group.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.SpaceEnd
	),
	
	/** Loop concept true in an ending condition. */
	LoopEnd
	(
		"3.4.1.4",
		31, 
		"End in making a loop.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.SpaceEnd
	),
	
	/** Pattern concept in an ending condition. */
	PatternEnd
	(
		"3.4.1.5",
		33, 
		"End in making a pattern.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.SpaceEnd
	),
	
	/** PathExtent concept in an ending condition. */
	PathExtentEnd
	(
		"3.4.1.6",
		35, 
		"End with a path extent.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.SpaceEnd
	),
	
	/** Territory concept in an ending condition. */
	TerritoryEnd
	(
		"3.4.1.7",
		37, 
		"End related to a territory.",
		ConceptType.End,
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.SpaceEnd
	),
	
	/** */
	CaptureEnd
	(
		"3.4.2",
		197, 
		"Capture ending rules.",
		ConceptType.End,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.End
	),
	
	/** End with (CanNotMove + Threat). */
	Checkmate
	(
		"3.4.2.1",
		17, 
		"End if checkmate.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.CaptureEnd
	),
	
	/** End with (NoTargetPiece). */
	NoTargetPieceEnd
	(
		"3.4.2.2",
		17, 
		"End if a target piece is removed.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.CaptureEnd
	),
	
	/** */
	RaceEnd
	(
		"3.4.3",
		197, 
		"Race ending rules.",
		ConceptType.End,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.End
	),
	
	/** No Piece test in End + Win in result type. */
	Escape
	(
		"3.4.3.1",
		13, 
		"Win if no piece.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.RaceEnd
	),
	
	/** Fill concept in the ending conditions. */
	FillEnd
	(
		"3.4.3.2",
		44, 
		"End in filling a region.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },  
		true,
		Concept.RaceEnd
	),
	
	/** Contains concept in the ending condition. */
	ReachEnd
	(
		"3.4.3.3",
		109, 
		"End in reaching a region.",
		ConceptType.End, 
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },  
		true,
		Concept.RaceEnd
	),
	
	/** (byScore ...) ending condition. */
	ScoringEnd
	(
		"3.4.4",
		42, 
		"End in comparing scores.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.End
	),
	
	/** End if stalemate. */
	StalemateEnd
	(
		"3.4.5",
		6, 
		"End if stalemate.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.End
	),
	
	/** The counter is used in the ending rules. */
	NoProgressEnd
	(
		"3.4.6",
		6, 
		"The game does not progress to an end.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.End
	),
	
	/** A resultType Draw is used . */
	Draw
	(
		"3.4.7",
		6, 
		"The game can ends in a draw.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.End
	),
	
	// -------------------------------------------------------------------------
    //                                 Math
	// -------------------------------------------------------------------------
	
	/** */
	Math
	(
		"4",
		197, 
		"Mathematics.",
		ConceptType.Math,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		null
	),
	
	/** */
	Arithmetic
	(
		"4.1",
		197, 
		"Arithmetic.",
		ConceptType.Math,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Math
	),
	
	/** */
	Operations
	(
		"4.1.1",
		197, 
		"Operations.",
		ConceptType.Math,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Arithmetic
	),
	
	/** (+ ...) is used. */
	Addition
	(
		"4.1.1.1",
		219, 
		"Addition operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (- ...) is used. */
	Subtraction
	(
		"4.1.1.2",
		221, 
		"Subtraction operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (* ...) is used.. */
	Multiplication
	(
		"4.1.1.3",
		220, 
		"Multiplication operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (/ ...) is used. */
	Division
	(
		"4.1.1.4",
		222, 
		"Division operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (% ...) is used. */
	Modulo
	(
		"4.1.1.5",
		223, 
		"Modulo operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (abs ...) is used. */
	Absolute
	(
		"4.1.1.6",
		223, 
		"Absolute operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (sqrt ...) used.. */
	Roots
	(
		"4.1.1.7",
		228, 
		"Root operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (cos ...) is used. */
	Cosine
	(
		"4.1.1.8",
		224, 
		"Cosine operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (sin ...) is used. */
	Sine
	(
		"4.1.1.9",
		225, 
		"Sine operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (tan ...) is used. */
	Tangent
	(
		"4.1.1.10",
		226, 
		"Tangent operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (^ ...) is used. */
	Exponentiation
	(
		"4.1.1.11",
		227, 
		"Exponentiation operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (exp ...) is used. */
	Exponential
	(
		"4.1.1.12",
		231, 
		"Exponential operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (log ...) or (log10 ...) is used. */
	Logarithm
	(
		"4.1.1.13",
		232, 
		"Logarithm operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (min ...) is used. */
	Minimum
	(
		"4.1.1.14",
		229, 
		"Minimum value.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (max ...) is used. */
	Maximum
	(
		"4.1.1.15",
		230, 
		"Maximum value.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** */
	Comparison
	(
		"4.1.2",
		197, 
		"Comparison of numbers.",
		ConceptType.Math,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Arithmetic
	),
	
	/** = operator. */
	Equal
	(
		"4.1.2.1",
		250, 
		"= operator.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Comparison
	),
	
	/** != operator. */
	NotEqual
	(
		"4.1.2.2",
		251, 
		"!= operator.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Comparison
	),
	
	/** < operator. */
	LesserThan
	(
		"4.1.2.3",
		248, 
		"< operator.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Comparison
	),
	
	/** <= operator. */
	LesserThanOrEqual
	(
		"4.1.2.4",
		249, 
		"<= operator.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Comparison
	),
	
	/** > operator. */
	GreaterThan
	(
		"4.1.2.5",
		246, 
		"> operator.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Comparison
	),
	
	/** >= operator. */
	GreaterThanOrEqual
	(
		"4.1.2.6",
		247, 
		">= operator.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Comparison
	),
	
	/** */
	Parity
	(
		"4.1.3",
		197, 
		"Whether a number is even or odd.",
		ConceptType.Math,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Arithmetic
	),
	
	/** Even values. */
	Even
	(
		"4.1.3.1",
		216, 
		"Even values.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Parity
	),
	
	/** Odd values. */
	Odd
	(
		"4.1.3.2",
		217, 
		"Odd values.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Parity
	),
	
	/** */
	Logic
	(
		"4.2",
		197, 
		"Logic operations.",
		ConceptType.Math,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Math
	),	
	
	/** (and ...). */
	Conjunction
	(
		"4.2.1",
		241, 
		"Conjunction (And).",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Logic
	),
	
	/**(or ...). */
	Disjunction
	(
		"4.2.2",
		242, 
		"Disjunction (Or).",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Logic
	),
	
	/** (xor ...). */
	ExclusiveDisjunction
	(
		"4.2.3",
		253, 
		"Exclusive Disjunction (Xor).",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Logic
	),
	
	/** (not ...). */
	Negation
	(
		"4.2.4",
		252, 
		"Negation (Not).",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Logic
	),
	
	/** */
	Set
	(
		"4.3",
		197, 
		"Set operations.",
		ConceptType.Math,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Math
	),	
	
	/** (union ...). */
	Union
	(
		"4.3.1",
		254, 
		"Union operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Set
	),
	
	/** (intersection ...). */
	Intersection
	(
		"4.3.2",
		255, 
		"Intersection operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Set
	),
	
	/** (difference ...). */
	Complement
	(
		"4.3.3",
		256, 
		"Complement operation (Difference).",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Set
	),
	
	/** */
	Algorithmics
	(
		"4.4",
		197, 
		"Algorithmic operations.",
		ConceptType.Math,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Math
	),
	
	
	/** (if ...) is used. */
	ConditionalStatement
	(
		"4.4.1",
		243, 
		"Conditional Statement (If).",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Algorithmics
	),
	
	/** (for ...) is used. */
	ControlFlowStatement
	(
		"4.4.2",
		244, 
		"Control Flow Statement (For).",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Algorithmics
	),
	
	/** Float values. */
	Float
	(
		"4.5",
		218, 
		"Float values.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Math
	),
	
	// -------------------------------------------------------------------------
    //                                 Visual
	// -------------------------------------------------------------------------
	
	/** */
	Visual
	(
		"5",
		197, 
		"Important visual aspects.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		null
	),
	
	/** */
	Style
	(
		"5.1",
		197, 
		"Style of game elements.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		new ConceptPurpose[]{ ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Visual
	),
	
	/** */
	BoardStyle
	(
		"5.1.1",
		197, 
		"Style of the board.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Style
	),
	
	/** Use Graph style. */
	GraphStyle
	(
		"5.1.1.1",
		125,
		"Use Graph style.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.BoardStyle
	),
	
	/** Use Chess style. */
	ChessStyle
	(
		"5.1.1.2",
		113,
		"Use Chess style.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.BoardStyle
	),
	
	/** Use Go style.*/
	GoStyle
	(
		"5.1.1.3",
		114,
		"Use Go style.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[]{ ConceptPurpose.Reconstruction }, 
		true,
		Concept.BoardStyle
	),
	
	/** Use Mancala style.*/
	MancalaStyle
	(
		"5.1.1.4",
		115,
		"Use Mancala style.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.BoardStyle
	),
	
	/** Use PenAndPaper style.*/
	PenAndPaperStyle
	(
		"5.1.1.5",
		116,
		"Use PenAndPaper style.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.BoardStyle
	),
	
	/** Use Shibumi style.*/
	ShibumiStyle
	(
		"5.1.1.6",
		117,
		"Use Shibumi style.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.BoardStyle
	),
	
	/** Use Backgammon style.*/
	BackgammonStyle
	(
		"5.1.1.7",
		118,
		"Use Backgammon style.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.BoardStyle
	),
	
	/** Use Janggi style. */
	JanggiStyle
	(
		"5.1.1.8",
		119,
		"Use Janggi style.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.BoardStyle
	),
	
	/** Use Xiangqi style. */
	XiangqiStyle
	(
		"5.1.1.9",
		120,
		"Use Xiangqi style.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.BoardStyle
	),
	
	/** Use Shogi style. */
	ShogiStyle(
		"5.1.1.10",
		121,
		"Use Shogi style.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.BoardStyle
	),
	
	/** Use Table style. */
	TableStyle(
		"5.1.1.11",
		122,
		"Use Table style.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.BoardStyle
	),
	
	/** Use Surakarta style. */
	SurakartaStyle
	(
		"5.1.1.12",
		123,
		"Use Surakarta style.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.BoardStyle
	),
	
	/** Use Tafl style. */
	TaflStyle
	(
		"5.1.1.13",
		124,
		"Use Tafl style.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.BoardStyle
	),
	
	/** Board is not shown. */
	NoBoard
	(
		"5.1.1.14",
		237, 
		"Board is not shown.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.BoardStyle
	),
	
	/** */
	ComponentStyle
	(
		"5.1.2",
		197, 
		"Style of the component.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Style
	),
	
	/** Use animal components. */
	AnimalComponent
	(
		"5.1.2.1",
		203, 
		"Use animal components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.ComponentStyle
	),
	
	/** Use Chess components. */
	ChessComponent
	(
		"5.1.2.2",
		204, 
		"Use Chess components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		false,
		Concept.ComponentStyle
	),
	
	/** Use King components. */
	KingComponent
	(
		"5.1.2.2.1",
		204, 
		"Use Chess components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.ChessComponent
	),
	
	/** Use Queen components. */
	QueenComponent
	(
		"5.1.2.2.2",
		204, 
		"Use Queen components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.ChessComponent
	),
	
	/** Use Knight components. */
	KnightComponent
	(
		"5.1.2.2.3",
		204, 
		"Use Knight components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.ChessComponent
	),
	
	/** Use Rook components. */
	RookComponent
	(
		"5.1.2.2.4",
		204, 
		"Use Rook components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.ChessComponent
	),
	
	/** Use Bishop components. */
	BishopComponent
	(
		"5.1.2.2.5",
		204, 
		"Use Bishop components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.ChessComponent
	),
	
	/** Use Pawn components. */
	PawnComponent
	(
		"5.1.2.2.6",
		204, 
		"Use Pawn components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.ChessComponent
	),
	
	/** Use fairy Chess components. */
	FairyChessComponent
	(
		"5.1.2.3",
		205, 
		"Use fairy Chess components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.ComponentStyle
	),
	
	/** Use Ploy components. */
	PloyComponent
	(
		"5.1.2.4",
		206, 
		"Use Ploy components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.ComponentStyle
	),
	
	/** Use Shogi components. */
	ShogiComponent
	(
		"5.1.2.5",
		207, 
		"Use Shogi components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.ComponentStyle
	),
	
	/** Use Xiangqi components. */
	XiangqiComponent
	(
		"5.1.2.6",
		208, 
		"Use Xiangqi components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.ComponentStyle
	),
	
	/** Use Stratego components. */
	StrategoComponent
	(
		"5.1.2.7",
		209, 
		"Use Stratego components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.ComponentStyle
	),
	
	/** Use Janggi components. */
	JanggiComponent
	(
		"5.1.2.8",
		210, 
		"Use Janggi components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.ComponentStyle
	),
	
	/** Use Hand components. */
	HandComponent
	(
		"5.1.2.9",
		211, 
		"Use Hand components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.ComponentStyle
	),
	
	/** Use Checkers components. */
	CheckersComponent
	(
		"5.1.2.10",
		212, 
		"Use Checkers components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.ComponentStyle
	),
	
	/** Use Ball components. */
	BallComponent
	(
		"5.1.2.11",
		213, 
		"Use Ball components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.ComponentStyle
	),
	
	/** Use Tafl components. */
	TaflComponent
	(
		"5.1.2.12",
		214, 
		"Use Tafl components.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.ComponentStyle
	),
	
	/** Use Disc components. */
	DiscComponent
	(
		"5.1.2.13",
		215, 
		"Use Disc components.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.ComponentStyle
	),
	
	/** Use Marker components. */
	MarkerComponent
	(
		"5.1.2.14",
		213, 
		"Use Marker components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.ComponentStyle
	),
	
	/**
	 * Visual of a stack is modified.
	 */
	StackType
	(
		"5.2",
		111,
		"Visual of a stack.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] {}, 
		false,
		Concept.Visual
	),
	
	/**
	 * Use stacks.
	 */
	Stack
	(
		"5.2.1",
		112,
		"Stacks of pieces.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.StackType
	),
	
	/** Use Symbols. */
	Symbols
	(
		"5.3",
		234, 
		"Symbols on the board.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.Visual
	),
	
	
	/** Show piece value. */
	ShowPieceValue
	(
		"5.4",
		235, 
		"Show piece values.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.Visual
	),
	
	/** Show piece state. */
	ShowPieceState
	(
		"5.5",
		236, 
		"Show piece states.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Visual
	),
	
	
	// -------------------------------------------------------------------------
    //                                 Implementation
	// -------------------------------------------------------------------------

	/** */
	Implementation
	(
		"6",
		197, 
		"Internal implementation details, e.g. for performance predictions.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		null
	),
	
	/** */
	State
	(
		"6.1",
		197, 
		"State related implementation.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Implementation
	),
	
	/** */
	StateType
	(
		"6.1.1",
		197, 
		"Type of state used.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.State
	),
	
	/** Use stack state. */
	StackState
	(
		"6.1.1.1",
		88, 
		"Use stack state.",
		ConceptType.Implementation, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI, }, 
		true,
		Concept.StateType
	),
	
	/** */
	PieceState
	(
		"6.1.2",
		197, 
		"State related information about piece.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.State
	),
	
	/** Use site state. */
	SiteState
	(
		"6.1.2.1",
		131, 
		"Use site state.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.PieceState
	),
	
	/** Use (set State ...). */
	SetSiteState
	(
		"6.1.2.2",
		131, 
		"Set the site state.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.PieceState
	),
	
	
	/** Store visited sites in previous moves of a turn. */
	VisitedSites
	(
		"6.1.2.3",
		133, 
		"Store visited sites in previous moves of a turn.",
		ConceptType.Implementation, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.PieceState
	),
	
	/** Use state variable(s). */
	Variable
	(
		"6.1.3",
		139,
		"Use state variable(s).",
		ConceptType.Implementation, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		false,
		Concept.State
	),
	
	/** (set Var ...). */
	SetVar
	(
		"6.1.3.1",
		139,
		"The variable 'var' is set.",
		ConceptType.Implementation, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.Variable
	),
	
	/** (remember ...). */
	RememberValues
	(
		"6.1.3.2",
		139,
		"Some values are remembered.",
		ConceptType.Implementation, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.Variable
	),
	
	/** (remember ...). */
	ForgetValues
	(
		"6.1.3.3",
		139,
		"Some values are forgotten.",
		ConceptType.Implementation, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.Variable
	),
	
	/** (set Pending ...). */
	SetPending
	(
		"6.1.3.4",
		139,
		"The variable pending is set.",
		ConceptType.Implementation, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.Variable
	),
	
	/** Use internal counter of the state. */
	InternalCounter
	(
		"6.1.4",
		130, 
		"Use internal counter of the state.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		false,
		Concept.State
	),
	
	/** Set internal counter of the state. */
	SetInternalCounter
	(
		"6.1.4.1",
		130, 
		"Set internal counter of the state.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.InternalCounter
	),
	
	/** Use player value. */
	PlayerValue
	(
		"6.1.5",
		170, 
		"Use player value.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.State
	),
	
	/** */
	SetHidden
	(
		"6.1.6",
		170, 
		"Hidden information are set.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		false,
		Concept.State
	),
	
	/** (set Hidden ...) is used. */
	SetInvisible
	(
		"6.1.6.1",
		170, 
		"Invisibility is set.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.SetHidden
	),
	
	/** (set Hidden Count ...) is used. */
	SetHiddenCount
	(
		"6.1.6.2",
		170, 
		"Hidden count is set.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.SetHidden
	),
	
	/** (set Hidden Rotation ...) is used. */
	SetHiddenRotation
	(
		"6.1.6.3",
		170, 
		"Hidden rotation is set.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.SetHidden
	),
	
	/** (set Hidden State ...) is used. */
	SetHiddenState
	(
		"6.1.6.4",
		170, 
		"Hidden state is set.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.SetHidden
	),
	
	/** (set Hidden Value ...) is used. */
	SetHiddenValue
	(
		"6.1.6.5",
		170, 
		"Hidden value is set.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.SetHidden
	),
	
	/** (set Hidden What ...) is used. */
	SetHiddenWhat
	(
		"6.1.6.6",
		170, 
		"Hidden count are set.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.SetHidden
	),
	
	/** (set Hidden Who ...) is used. */
	SetHiddenWho
	(
		"6.1.6.7",
		170, 
		"Hidden who is set.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData, 
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.SetHidden
	),
	
	/** */
	Efficiency
	(
		"6.2",
		197, 
		"Implementation related to efficiency.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Implementation
	),
	
	
	/** The context can be copied during computation of the moves. */
	CopyContext
	(
		"6.2.1",
		93, 
		"The context can be copied during computation of the moves.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.Efficiency
	),
	
	/** Use consequences moves (then). */
	Then
	(
		"6.2.2",
		137, 
		"Use consequences moves (then).",
		ConceptType.Implementation, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.Efficiency
	),
	

	/** Describes moves per piece. */
	ForEachPiece
	(
		"6.2.3",
		141,
		"Describes moves per piece.",
		ConceptType.Implementation, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[]{ ConceptPurpose.AI },  
		true,
		Concept.Efficiency
	),


	
	/** Use a (do ...) ludeme. */
	DoLudeme
	(
		"6.2.4",
		200, 
		"Use a (do ...) ludeme.",
		ConceptType.Implementation, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI },  
		true,
		Concept.Efficiency
	),
	
	/** Use a (trigger ...) ludeme. */
	Trigger
	(
		"6.2.5",
		200, 
		"Use a (trigger ...) ludeme.",
		ConceptType.Implementation, 
		ConceptDataType.BooleanData,
		new ConceptPurpose[] { ConceptPurpose.AI },  
		true,
		Concept.Efficiency
	),

	;
	
	
	// -------------------------------------------------------------------------
	
	/** The type of the game concepts. */
	final ConceptType type;
	
	/** The type of the game concepts. */
	final ConceptDataType dataType;

	/** The description of the concept. */
	final String description;

	/** The taxonomy node. */
	final String taxonomy;

	/** The list of the possible purposes of the concept. */
	final ConceptPurpose[] purposes;
	
	/** True if the concept is a leaf in the taxonomy. */
	final boolean leaf;
	
	/** The id of the concept. */
	final int id;

	/** The parent concept in the taxonomy. */
	final Concept parent;

	// -------------------------------------------------------------------------

	/**
	 * To create a new concept.
	 * 
	 * @param taxonomy    The taxonomy node.
	 * @param description The description of the concept.
	 * @param type        The type of the concept.
	 * @param dataType    The type of the data.
	 * @param purposes    The possible uses of the concept.
	 * @param leaf        True if the concept is a leaf in the taxonomy.
	 * @param parent      The parent node in the taxonomy.
	 */
	private Concept
	(
	    final String taxonomy, 
	    final int id,
		final String description, 
		final ConceptType type, 
		final ConceptDataType dataType,
		final ConceptPurpose[] purposes,
		final boolean leaf,
		final Concept parent
	)
	{
		this.taxonomy = taxonomy;
		this.description = description;
		this.type = type;
		this.purposes = purposes;
		this.dataType = dataType;
		this.leaf = leaf;
		this.id = id;
		this.parent = parent;
	}

	// -------------------------------------------------------------------------

	/**
	 * @return The taxonomy node.
	 */
	public String taxonomy()
	{
		return taxonomy;
	}
	
	/**
	 * @return The id of the concept.
	 */
	public int id()
	{
		return this.ordinal() + 1;
	}
	
	/**
	 * @return The plain English description of the game concept.
	 */
	public String description()
	{
		return description;
	}
	
	
	/**
	 * @return The type of the concept.
	 */
	public ConceptType type()
	{
		return type;
	}
	
	/**
	 * @return The data type of the concept.
	 */
	public ConceptDataType dataType()
	{
		return dataType;
	}

	/**
	 * @return The different possible purposes of the concept.
	 */
	public ConceptPurpose[] purposes()
	{
		return purposes;
	}

	/**
	 * @return True if the concept is a leaf in the taxonomy.
	 */
	public boolean isleaf()
	{
		return leaf;
	}

	/**
	 * @return The parent node in the taxonomy.
	 */
	public Concept parent()
	{
		return parent;
	}
}
