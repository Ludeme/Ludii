package other.concept;

import java.util.List;
import java.util.ArrayList;

/**
 * Defines known concepts used in a game.
 * 
 * Remarks: The documentation on top of each concept explains how the concept is
 * computed if that's leaf. If that's not a leaf, the concept is true if any
 * child is true.
 * 
 * Remarks: The next id in case of a new concept is the id 780.
 * 
 * @author Eric.Piette
 */
public enum Concept
{
	//-------------------------------------------------------------------------
    //                                 Properties
	//-------------------------------------------------------------------------
	
	/** */
	Properties
	(
		"1",
		1,
		"General properties of the game.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
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
			ConceptComputationType.Compilation,
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
		ConceptComputationType.Compilation,
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
		ConceptComputationType.Compilation,
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
		ConceptComputationType.Compilation,
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
		ConceptComputationType.Compilation,
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
		ConceptComputationType.Compilation,
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
		ConceptComputationType.Compilation,
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
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Format
	),
	
	/** Use hidden information. */
	HiddenInformation
	(
		"1.1.4",
		10, 
		"Game involves hidden information.",
		ConceptType.Properties,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Format
	),
	
	/** All games with subgames. */
	Match
	(
		"1.1.5",
		11,
		"Match game.",
		ConceptType.Properties,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Format
	),
	
	/** */
	Asymmetric
	(
		"1.1.6",
		12,
		"Asymmetry in rules and/or forces.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		false,
		Concept.Format
	),
	
	/** */
	AsymmetricRules
	(
		"1.1.6.1",
		13, 
		"Players have different rules.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.Asymmetric
	),
	
	/** TO DO */
	AsymmetricPlayRules
	(
		"1.1.6.1.1",
		14, 
		"Players have different play rules.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.AsymmetricRules
	),
	
	/** TO DO */
	AsymmetricEndRules
	(
		"1.1.6.1.2",
		15, 
		"Players have different end rules.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.AsymmetricRules
	),
	
	/** */
	AsymmetricForces
	(
		"1.1.6.2",
		16, 
		"Players have different forces.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.Asymmetric
	),
	
	/** TO DO. */
	AsymmetricSetup
	(
		"1.1.6.2.1",
		17, 
		"Different starting positions for each player.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.AsymmetricForces
	),
	
	/** True if any piece type is owned by a player and not all the others. */
	AsymmetricPiecesType
	(
		"1.1.6.2.2",
		18, 
		"Different piece types owned by each player.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.AsymmetricForces
	),
	
	/** */
	Players
	(
		"1.2",
		19, 
		"Players of the game.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.Properties
	),
	
	/** Number of players. */
	NumPlayers
	(
		"1.2.1",
		20,
		"Number of players.",
		ConceptType.Properties,
		ConceptDataType.IntegerData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		false,
		Concept.Players
	),
	
	/** The mode is simulation. */
	Simulation
	(
		"1.2.1.1",
		21,
		"No players (environment runs the game).",
		ConceptType.Properties,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.NumPlayers
	),
	
	/** Number of players = 1. */
	Solitaire
	(
		"1.2.1.2",
		22,
		"Single player.",
		ConceptType.Properties,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.NumPlayers
	),
	
	/** Number of players = 2. */
	TwoPlayer
	(
		"1.2.1.3",
		23,
		"Two players.",
		ConceptType.Properties,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.NumPlayers
	),
	
	/** Number of players > 2. */
	Multiplayer
	(
		"1.2.1.4",
		24,
		"More than two players.",
		ConceptType.Properties,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.NumPlayers
	),
	
	/** */
	Cooperation
	(
		"1.3",
		25, 
		"Players have to cooperate.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.Properties
	),
	
	/** A (set Team ...) ludeme is used. */
	Team
	(
		"1.3.1",
		26, 
		"Game involves teams of players.",
		ConceptType.Properties,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Cooperation
	),
	
	/** A (set Team ...) ludeme is used in the play rules. */
	Coalition
	(
		"1.3.2",
		27, 
		"Players may form coalitions.",
		ConceptType.Properties,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Cooperation
	),
	
	/** */
	Puzzle
	(
		"1.4",
		28, 
		"Type of puzzle.",
		ConceptType.Properties, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.Properties
	),
	
	/** True if any ludeme used only for deduction puzzle is used. */
	DeductionPuzzle
	(
		"1.4.1",
		29, 
		"Solution can be deduced.",
		ConceptType.Properties,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Puzzle
	),
	
	/** All the 1 player game which are not deduction puzzles. */
	PlanningPuzzle
	(
		"1.4.2",
		30, 
		"Solution is reached in moving pieces.",
		ConceptType.Properties,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[]{ ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Puzzle
	),
	
	//-------------------------------------------------------------------------
    //                                 Equipment
	//-------------------------------------------------------------------------
	
	/** */
	Equipment
	(
		"2",
		31, 
		"Equipment for playing the game.",
		ConceptType.Equipment, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		null
	),
	
	/** */
	Container
	(
		"2.1",
		32, 
		"Containers that hold components.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.Equipment
	),
	
	/** */
	Board
	(
		"2.1.1",
		33, 
		"Board shared by player for playing the game.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.Container
	),
	
	/** */
	Shape
	(
		"2.1.1.1",
		34, 
		"The shape of the board.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.Board
	),
	
	/** True if a sub ludeme correspond to a square shape. */
	SquareShape
	(
		"2.1.1.1.1",
		35, 
		"Square shape.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Shape
	),
	
	/** True if a sub ludeme correspond to a hexagonal shape. */
	HexShape
	(
		"2.1.1.1.2",
		36, 
		"Hexagonal shape.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Shape
	),
	
	/** True if a sub ludeme correspond to a triangle shape. */
	TriangleShape
	(
		"2.1.1.1.3",
		37, 
		"Triangle shape.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Shape
	),
	
	/** True if a sub ludeme correspond to a diamond shape. */
	DiamondShape
	(
		"2.1.1.1.4",
		38, 
		"Diamond shape.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Shape
	),
	
	/** True if a sub ludeme correspond to a rectangle shape. */
	RectangleShape
	(
		"2.1.1.1.5",
		39, 
		"Rectangle shape.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.Shape
	),
	
	/** True if a sub ludeme correspond to a spiral shape. */
	SpiralShape
	(
		"2.1.1.1.6",
		40, 
		"Spirale shape.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction }, 
		true,
		Concept.Shape
	),
	
	/** True if a sub ludeme correspond to a circle shape. */
	CircleShape
	(
		"2.1.1.1.7",
		41, 
		"Circle shape.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Shape
	),
	
	/** True if a sub ludeme correspond to a prism shape. */
	PrismShape
	(
		"2.1.1.1.8",
		42, 
		"Prism shape.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Shape
	),
	
	/** True if a sub ludeme correspond to a star shape. */
	StarShape(
		"2.1.1.1.9",
		43, 
		"Star shape.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Shape
	),
	
	/** True if a sub ludeme correspond to a parallelogram shape. */
	ParallelogramShape(
		"2.1.1.1.10",
		44, 
		"Parallelogram shape.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Shape
	),
	
	/** True if a sub ludeme correspond to a square shape with pyramidal at true. */
	SquarePyramidalShape(
		"2.1.1.1.11",
		45, 
		"Square Pyramidal shape.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Shape
	),
	
	/** True if a sub ludeme correspond to a rectangle shape with pyramidal at true. */
	RectanglePyramidalShape
	(
		"2.1.1.1.12",
		46, 
		"Rectangle Pyramidal shape.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Shape
	),
	
	/** Board shape is based on a regular polygon. */
	RegularShape
	(
		"2.1.1.1.13",
		47, 
		"Regular shape.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Shape
	),
	
	/** Board shape is based on a general polygon. */
	PolygonShape
	(
		"2.1.1.1.14",
		48, 
		"General polygonal shape.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Shape
	),
	
	/** Board shape is concentric set of rings like a target. */
	TargetShape
	(
		"2.1.1.1.15",
		49, 
		"Target shape.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction },
		true,
		Concept.Shape
	),
	
	/** */
	Tiling
	(
		"2.1.1.2",
		50, 
		"The tiling of the board.",
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.Board
	),
	
	/** True if any ludeme uses a square tiling. */
	SquareTiling
	(
		"2.1.1.2.1",
		51, 
		"Square tiling.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Tiling
	),
	
	/** True if any ludeme uses a hexagonal tiling. */
	HexTiling
	(
		"2.1.1.2.2",
		52, 
		"Hexagonal tiling.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Tiling
	),
	
	/** True if any ludeme uses a triangle tiling. */
	TriangleTiling
	(
		"2.1.1.2.3",
		53, 
		"Triangle tiling.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Tiling
	),
	
	/** True if any ludeme uses a brick tiling. */
	BrickTiling
	(
		"2.1.1.2.4",
		54, 
		"Brick tiling.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Tiling
	),
	
	/** True if a Semi regular tiling is used. */
	SemiRegularTiling
	(
		"2.1.1.2.5",
		55, 
		"Semi regular tiling.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Tiling
	),
	
	/** True if any ludeme uses a celtic tiling. */
	CelticTiling
	(
		"2.1.1.2.6",
		56, 
		"Celtic tiling.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Tiling
	),
	
	/** True if any ludeme uses a morris tiling. */
	MorrisTiling
	(
		"2.1.1.2.7",
		57, 
		"Morris tiling.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Tiling
	),
	
	/** True if any ludeme uses a quadHex tiling. */
	QuadHexTiling
	(
		"2.1.1.2.8",
		58, 
		"QuadHex tiling.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Tiling
	),
	
	/** True if any ludeme uses a circle tiling. */
	CircleTiling
	(
		"2.1.1.2.9",
		59, 
		"Circle tiling.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Tiling
	),
	
	/** True if any ludeme uses a concentric tiling. */
	ConcentricTiling
	(
		"2.1.1.2.10",
		60, 
		"Concentric tiling.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Tiling
	),
	
	/** True if any ludeme uses a spiral tiling. */
	SpiralTiling
	(
		"2.1.1.2.11",
		61, 
		"Spiral tiling.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Tiling
	),
	
	/** True if any ludeme uses an alquerque tiling. */
	AlquerqueTiling
	(
		"2.1.1.2.12",
		62, 
		"Alquerque tiling.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Tiling
	),
	
	/** (mancalaBoard ...) is used. */
	MancalaBoard
	(
		"2.1.1.3",
		63, 
		"Mancala board.",
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.Board
	),
	
	/** (mancalaBoard ...) is used with stores not None. */
	MancalaStores
	(
		"2.1.1.3.1",
		64, 
		"Mancala board with stores.",
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.MancalaBoard
	),
	
	/** (mancalaBoard ...) is used using 2 rows. */
	MancalaTwoRows
	(
		"2.1.1.3.2",
		65, 
		"Mancala board with 2 rows.",
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.MancalaBoard
	),
	
	/** (mancalaBoard ...) is used using 3 rows. */
	MancalaThreeRows
	(
		"2.1.1.3.3",
		66, 
		"Mancala board with 3 rows.",
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.MancalaBoard
	),
	
	/** (mancalaBoard ...) is used using 4 rows. */
	MancalaFourRows
	(
		"2.1.1.3.4",
		67, 
		"Mancala board with 4 rows.",
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.MancalaBoard
	),
	
	/** (mancalaBoard ...) is used using 6 rows. */
	MancalaSixRows
	(
		"2.1.1.3.5",
		68, 
		"Mancala board with 6 rows.",
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.MancalaBoard
	),
	
	/** (mancalaBoard ...) is used using with a circular tiling */
	MancalaCircular
	(
		"2.1.1.3.6",
		69, 
		"Mancala board with circular tiling.",
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.MancalaBoard
	),
	
	/** "AlquerqueBoard" ludemeplex (or a similar one) is used. */
	AlquerqueBoard
	(
		"2.1.1.4",
		780, 
		"Alquerque board.",
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.Board
	),
	
	/** "AlquerqueBoardWithBottomTriangle" ludemeplex is used. */
	AlquerqueBoardWithOneTriangle
	(
		"2.1.1.4.1",
		781, 
		"Alquerque board with one triangle extension.",
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.AlquerqueBoard
	),
	
	/** "AlquerqueBoardWithBottomAndTopTriangles" ludemeplex is used. */
	AlquerqueBoardWithTwoTriangles
	(
		"2.1.1.4.2",
		782, 
		"Alquerque board with two triangle extensions.",
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.AlquerqueBoard
	),
	
	/** "AlquerqueBoardWithFourTriangles" ludemeplex is used. */
	AlquerqueBoardWithFourTriangles
	(
		"2.1.1.4.3",
		783, 
		"Alquerque board with four triangle extensions.",
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.AlquerqueBoard
	),
	
	/** "AlquerqueBoardWithEightTriangles" ludemeplex is used. */
	AlquerqueBoardWithEightTriangles
	(
		"2.1.1.4.4",
		784, 
		"Alquerque board with eight triangle extensions.",
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.AlquerqueBoard
	),
	
	/** One of the "*MorrisBoard*" ludemeplexes is used. */
	MorrisBoard
	(
		"2.1.1.5",
		785, 
		"Morris board.",
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.Board
	),
	
	/** "ThreeMenMorrisBoard" ludemeplex is used. */
	ThreeMensMorrisBoard
	(
		"2.1.1.5.1",
		786, 
		"Three Men's Morris Board.",
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.MorrisBoard
	),
	
	/** "ThreeMenMorrisBoardWithTwoTriangles" ludemeplex is used. */
	ThreeMensMorrisBoardWithTwoTriangles
	(
		"2.1.1.5.2",
		787, 
		"Three Men's Morris Board with two triangle extensions.",
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.MorrisBoard
	),
	
	/** "NineMensMorrisBoard" ludemeplex is used. */
	NineMensMorrisBoard
	(
		"2.1.1.5.3",
		788, 
		"Nine Men's Morris Board.",
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.MorrisBoard
	),
	
	/** "StarBoard" ludemeplex is used. */
	StarBoard
	(
		"2.1.1.6",
		789, 
		"Star board.",
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Board
	),
	
	/** "CrossBoard" ludemeplex is used. */
	CrossBoard
	(
		"2.1.1.7",
		790, 
		"Cross board.",
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Board
	),
	
	/** "KintsBoard" ludemeplex is used. */
	KintsBoard
	(
		"2.1.1.8",
		791, 
		"Kints board.",
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Board
	),
	
	/** "PachisiBoard" ludemeplex is used. */
	PachisiBoard
	(
		"2.1.1.9",
		792, 
		"Pachisi board.",
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Board
	),
	
	/** "FortyStonesWithFourGapsBoard" ludemeplex is used. */
	FortyStonesWithFourGapsBoard
	(
		"2.1.1.10",
		793, 
		"Forty Stones in a circle with four gaps after each 10 stones.",
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Board
	),
	
	/** The track list of the board is not empty. */
	Track
	(
		"2.1.1.11",
		70, 
		"The board has a track.",
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.Board
	),
	
	/** One track uses a loop. */
	TrackLoop
	(
		"2.1.1.11.1",
		71, 
		"A track is a loop.",
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Track
	),
	
	/** One track uses a loop. */
	TrackOwned
	(
		"2.1.1.11.2",
		72, 
		"A track is owned.",
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Track
	),
	
	/** The ludeme (hints ...) is used. */
	Hints
	(
		"2.1.1.12", 
		73,
		"The board has some hints.", 
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** The list of regions is not empty. */
	Region
	(
		"2.1.1.13",
		74, 
		"The board has regions.",
		ConceptType.Container, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Board
	),
	
	/** The ludeme (boardless ...) is used. */
	Boardless
	(
		"2.1.1.14",
		75, 
		"Game is played on an implied grid.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Board
	),
	
	/** */
	PlayableSites
	(
		"2.1.1.15",
		76, 
		"Playable sites.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.Board
	),
	
	/** SiteType = Vertex in at least a ludeme. */
	Vertex
	(
		"2.1.1.15.1",
		77, 
		"Use Vertices.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI, }, 
		true,
		Concept.PlayableSites
	),
	
	/** SiteType = Cell in at least a ludeme. */
	Cell
	(
		"2.1.1.15.2",
		78, 
		"Use cells.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI, }, 
		true,
		Concept.PlayableSites
	),
	
	/** SiteType = Edge in at least a ludeme. */
	Edge
	(
		"2.1.1.15.3",
		79, 
		"Use edges.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI, }, 
		true,
		Concept.PlayableSites
	),
	
	/** Number of playables sites on the board. */
	NumPlayableSitesOnBoard
	(
		"2.1.1.15.4",
		80, 
		"Number of playables sites on the board.",
		ConceptType.Container, 
		ConceptDataType.IntegerData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.PlayableSites
	),
	
	/** Number of columns of the board. */
	NumColumns
	(
		"2.1.1.16",
		81, 
		"Number of columns of the board.",
		ConceptType.Container, 
		ConceptDataType.IntegerData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of rows of the board. */
	NumRows
	(
		"2.1.1.17",
		82, 
		"Number of rows of the board.",
		ConceptType.Container, 
		ConceptDataType.IntegerData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[]{ ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of corners of the board. */
	NumCorners
	(
		"2.1.1.18",
		83, 
		"Number of corners of the board.",
		ConceptType.Container, 
		ConceptDataType.IntegerData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Average number of directions of each playable site on the board. */
	NumDirections
	(
		"2.1.1.19",
		84, 
		"Average number of directions of each playable site on the board.",
		ConceptType.Container, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/**
	 * Average number of orthogonal directions of each playable site on the board.
	 */
	NumOrthogonalDirections
	(
		"2.1.1.20",
		85, 
		"Average number of orthogonal directions of each playable site on the board.",
		ConceptType.Container, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Average number of diagonal directions of each playable site on the board. */
	NumDiagonalDirections
	(
		"2.1.1.21",
		86, 
		"Average number of diagonal directions of each playable site on the board.",
		ConceptType.Container, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Average number of adjacent directions of each playable site on the board. */
	NumAdjacentDirections
	(
		"2.1.1.22",
		87, 
		"Average number of adjacent directions of each playable site on the board.",
		ConceptType.Container, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Average number of off diagonal directions of each playable site on the board. */
	NumOffDiagonalDirections
	(
		"2.1.1.23",
		88, 
		"Average number of off diagonal directions of each playable site on the board.",
		ConceptType.Container, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of outer sites of the board. */
	NumOuterSites
	(
		"2.1.1.24",
		89, 
		"Number of outer sites of the board.",
		ConceptType.Container, 
		ConceptDataType.IntegerData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of inner sites of the board. */
	NumInnerSites
	(
		"2.1.1.25",
		90, 
		"Number of inner sites of the board.",
		ConceptType.Container, 
		ConceptDataType.IntegerData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of layers of the board. */
	NumLayers
	(
		"2.1.1.26",
		91, 
		"Number of layers of the board.",
		ConceptType.Container, 
		ConceptDataType.IntegerData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of edges of the board. */
	NumEdges
	(
		"2.1.1.27",
		92, 
		"Number of edges of the board.",
		ConceptType.Container, 
		ConceptDataType.IntegerData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Board
	),
	
	/** Number of cells of the board. */
	NumCells
	(
		"2.1.1.28",
		93, 
		"Number of cells of the board.",
		ConceptType.Container, 
		ConceptDataType.IntegerData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of vertices of the board. */
	NumVertices
	(
		"2.1.1.29",
		94, 
		"Number of vertices of the board.",
		ConceptType.Container, 
		ConceptDataType.IntegerData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of perimeter sites of the board. */
	NumPerimeterSites
	(
		"2.1.1.30",
		95, 
		"Number of perimeter sites of the board.",
		ConceptType.Container, 
		ConceptDataType.IntegerData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of top sites of the board. */
	NumTopSites
	(
		"2.1.1.31",
		96, 
		"Number of top sites of the board.",
		ConceptType.Container, 
		ConceptDataType.IntegerData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of bottom sites of the board. */
	NumBottomSites
	(
		"2.1.1.32",
		97, 
		"Number of bottom sites of the board.",
		ConceptType.Container, 
		ConceptDataType.IntegerData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of right sites of the board. */
	NumRightSites
	(
		"2.1.1.33",
		98, 
		"Number of right sites of the board.",
		ConceptType.Container, 
		ConceptDataType.IntegerData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of left sites of the board. */
	NumLeftSites
	(
		"2.1.1.34",
		99, 
		"Number of left sites of the board.",
		ConceptType.Container, 
		ConceptDataType.IntegerData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of centre sites of the board. */
	NumCentreSites
	(
		"2.1.1.35",
		100, 
		"Number of centre sites of the board.",
		ConceptType.Container, 
		ConceptDataType.IntegerData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of convex corners of the board. */
	NumConvexCorners
	(
		"2.1.1.36",
		101, 
		"Number of convex corners of the board.",
		ConceptType.Container, 
		ConceptDataType.IntegerData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** Number of concave corners of the board. */
	NumConcaveCorners
	(
		"2.1.1.37",
		102, 
		"Number of concave corners of the board.",
		ConceptType.Container, 
		ConceptDataType.IntegerData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	/** Number of phases of the board. */
	NumPhasesBoard
	(
		"2.1.1.38",
		103, 
		"Number of phases of the board.",
		ConceptType.Container, 
		ConceptDataType.IntegerData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Board
	),
	
	/** The ludeme (hand ...) is used. */
	Hand
	(
		"2.1.2",
		104, 
		"Player hands for storing own pieces.",
		ConceptType.Container, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Container
	),
	
	/** Number of containers. */
	NumContainers
	(
		"2.1.3",
		105, 
		"Number of containers.",
		ConceptType.Container, 
		ConceptDataType.IntegerData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Container
	),
	
	/** Sum of all the playable sites across all the containers */
	NumPlayableSites
	(
		"2.1.4",
		106, 
		"Number of playables sites in total.",
		ConceptType.Container, 
		ConceptDataType.IntegerData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Container
	),
	
	/** */
	Component
	(
		"2.2",
		107, 
		"Components manipulated by the players.",
		ConceptType.Component, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		false,
		Concept.Equipment
	),
	
	/** The ludeme (piece ...) is used. */
	Piece
	(
		"2.2.1",
		108, 
		"Game is played with pieces.",
		ConceptType.Component, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Component
	),
	
	/** A ludeme (set Value ...) is used. */
	PieceValue
	(
		"2.2.2",
		109, 
		"Pieces have value.",
		ConceptType.Component, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Component
	),
	
	/** A rotation state is set */
	PieceRotation
	(
		"2.2.3",
		110, 
		"Pieces have rotations.",
		ConceptType.Component, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Component
	),
	
	/** A direction is not null in at least a piece. */
	PieceDirection
	(
		"2.2.4",
		111, 
		"Pieces have forward direction.",
		ConceptType.Component,
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Component
	),
	
	/** A die is included in the components. */
	Dice
	(
		"2.2.5",
		112, 
		"Game is played with dice.",
		ConceptType.Component, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.Component
	),
	
	/** A D2 is included in the components. */
	DiceD2
	(
		"2.2.5.1",
		794, 
		"Game is played with D2 dice.",
		ConceptType.Component, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Dice
	),
	
	/** A D3 is included in the components. */
	DiceD3
	(
		"2.2.5.2",
		795, 
		"Game is played with D3 dice.",
		ConceptType.Component, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Dice
	),
	
	/** A D4 is included in the components. */
	DiceD4
	(
		"2.2.5.3",
		796, 
		"Game is played with D4 dice.",
		ConceptType.Component, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Dice
	),
	
	/** A D6 is included in the components. */
	DiceD6
	(
		"2.2.5.4",
		797, 
		"Game is played with D6 dice.",
		ConceptType.Component, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Dice
	),
	
	/** Use biased dice. */
	BiasedDice
	(
		"2.2.6",
		113, 
		"Use biased dice.",
		ConceptType.Component, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Component
	),
	
	/** A card is included in the components. */
	Card
	(
		"2.2.7",
		114, 
		"Game is played with cards.",
		ConceptType.Component,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Component
	),
	
	/** A domino is included in the components. */
	Domino
	(
		"2.2.8",
		115, 
		"Game is played with dominoes.",
		ConceptType.Component, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Component
	),
	
	/** A large piece is included in the components. */
	LargePiece
	(
		"2.2.9",
		116, 
		"Game is played with large pieces.",
		ConceptType.Component, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Component
	),
	
	/** Use tiles. */
	Tile
	(
		"2.2.10",
		117, 
		"Game is played with tiles.",
		ConceptType.Component, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Component
	),
	
	/** Number of component types. */
	NumComponentsType
	(
		"2.2.11",
		118, 
		"Number of component types.",
		ConceptType.Component, 
		ConceptDataType.IntegerData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Component
	),
	
	/** Average number of component types per player. */
	NumComponentsTypePerPlayer
	(
		"2.2.12",
		119, 
		"Average number of component types per player.",
		ConceptType.Component, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Component
	),
	
	/** Number of dice. */
	NumDice
	(
		"2.2.13",
		120, 
		"Number of dice.",
		ConceptType.Component, 
		ConceptDataType.IntegerData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Component
	),
	
	//-------------------------------------------------------------------------
    //                                 Rules
	//-------------------------------------------------------------------------
	
	/** */
	Rules
	(
		"3",
		121, 
		"Rules of the game.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		null
	),
	
	//-------------------------------------------------------------------------
    //                                 MetaRules
	//-------------------------------------------------------------------------
	
	/** */
	Meta
	(
		"3.1",
		122, 
		"Global metarules that override all other rules.",
		ConceptType.Meta,
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Rules
	),
	
	/** */
	OpeningContract
	(
		"3.1.1",
		123, 
		"Game involves an opening round equaliser.",
		ConceptType.Meta,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Meta
	),
	
	/** (swap) metarule is used. */
	SwapOption
	(
		"3.1.1.1",
		124, 
		"Second player may swap colours.",
		ConceptType.Meta, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.OpeningContract
	),
	
	/** */
	Repetition
	(
		"3.1.2",
		125, 
		"Game has repetition checks.",
		ConceptType.Meta,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Meta
	),
	
	/** (no Repeat PositionalInTurn) is used. */
	TurnKo
	(
		"3.1.2.1",
		126, 
		"No repeated piece positions within a single turn.",
		ConceptType.Meta,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Repetition
	),
	
	/** (no Repeat SituationalInTurn) is used. */
	SituationalTurnKo
	(
		"3.1.2.2",
		127, 
		"No repeated states withing a single turn.",
		ConceptType.Meta,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Repetition
	),
	
	/** (no Repeat Positional) is used. */
	PositionalSuperko
	(
		"3.1.2.3",
		128, 
		"No repeated piece positions.",
		ConceptType.Meta,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Repetition
	),
	
	/** (no Repeat Situational) is used. */
	SituationalSuperko
	(
		"3.1.2.4",
		129, 
		"No repeated states.",
		ConceptType.Meta, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Repetition
	),
	
	/** */
	AutoMove
	(
		"3.1.3",
		130, 
		"Apply all legal moves related to one single site.",
		ConceptType.Meta,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Meta
	),
	
	//-------------------------------------------------------------------------
    //                                 Start Rules
	//-------------------------------------------------------------------------
	
	/** */
	Start
	(
		"3.2",
		131, 
		"Start rules.",
		ConceptType.Start,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
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
		132,
		"Places initially some pieces on the board.",
		ConceptType.Start, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
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
		133,
		"Places initially some pieces (different of shared dice) outside of the board.",
		ConceptType.Start, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Start
	),
	
	/** Places initially randomly some pieces. */
	InitialRandomPlacement
	(
		"3.2.3",
		134,
		"Places initially randomly some pieces.",
		ConceptType.Start, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Start
	),
	
	/** Sets initial score. */
	InitialScore
	(
		"3.2.4",
		135,
		"Sets initial score.",
		ConceptType.Start, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Start
	),
	
	/** Sets initial amount. */
	InitialAmount
	(
		"3.2.5",
		136,
		"Sets initial amount.",
		ConceptType.Start, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Start
	),
	
	/** Sets initial pot. */
	InitialPot
	(
		"3.2.6",
		137,
		"Sets initial pot.",
		ConceptType.Start, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Start
	),
	
	/** Sets initially some costs on graph elements. */
	InitialCost
	(
		"3.2.7",
		138,
		"Sets initially some costs on graph elements.",
		ConceptType.Start, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Start
	),
	
	/** Compute the number of pieces on the board at the start. */
	NumStartComponentsBoard
	(
		"3.2.8",
		139,
		"Number of components on board at start.",
		ConceptType.Start, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Start
	),
	
	/** Compute the number of pieces in the player hands at the start. */
	NumStartComponentsHand
	(
		"3.2.9",
		140,
		"Number of components in player hands at start.",
		ConceptType.Start, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Start
	),
	
	/** Compute the number of pieces at the start. */
	NumStartComponents
	(
		"3.2.10",
		141,
		"Number of components at start.",
		ConceptType.Start, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[]{ ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Start
	),
	
	//-------------------------------------------------------------------------
    //                                 Play Rules
	//-------------------------------------------------------------------------
	
	/** */
	Play
	(
		"3.3",
		142, 
		"Rules of general play.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Rules
	),
	
	/** */
	Moves
	(
		"3.3.1",
		143, 
		"Moves.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Play
	),
	
	/** */
	MovesDecision
	(
		"3.3.1.1",
		144, 
		"Moves.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Moves
	),
	
	/** */
	NoSiteMoves
	(
		"3.3.1.1.1",
		145, 
		"Moves.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.MovesDecision
	),
	
	/** Decide to bet. */
	BetDecision
	(
		"3.3.1.1.1.1",
		146, 
		"Decide to bet.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		false,
		Concept.NoSiteMoves
	),
	
	/** Frequency of BetDecision. */
	BetDecisionFrequency
	(
		"3.3.1.1.1.1.1",
		147, 
		"Frequency of \"Bet Decision\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.BetDecision
	),
	
	
	/** Decide to vote. */
	VoteDecision
	(
		"3.3.1.1.1.2",
		148, 
		"Decide to vote.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		false,
		Concept.NoSiteMoves
	),
	
	/** Frequency of VoteDecision. */
	VoteDecisionFrequency
	(
		"3.3.1.1.1.2.1",
		149, 
		"Frequency of \"Vote Decision\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.VoteDecision
	),
	
	/** Decide to swap players. */
	SwapPlayersDecision
	(
		"3.3.1.1.1.3",
		150, 
		"Decide to swap players.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.NoSiteMoves
	),
	
	/** Frequency of SwapPiecesDecision. */
	SwapPlayersDecisionFrequency
	(
		"3.3.1.1.1.3.1",
		151, 
		"Frequency of \"Swap Players Decision\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.SwapPlayersDecision
	),
	
	/** (move Set TrumpSuit ..). */
	ChooseTrumpSuitDecision
	(
		"3.3.1.1.1.4",
		152, 
		"Choose the trump suit.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.NoSiteMoves
	),
	
	/** Frequency of ChooseTrumpSuit. */
	ChooseTrumpSuitDecisionFrequency
	(
		"3.3.1.1.1.4.1",
		153, 
		"Frequency of \"Choose Trump Suit Decision\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.ChooseTrumpSuitDecision
	),
	
	/** (move Pass ...) is used. */
	PassDecision
	(
		"3.3.1.1.1.5",
		154, 
		"Decide to pass a turn.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.NoSiteMoves
	),
	
	/** Frequency of PassDecision. */
	PassDecisionFrequency
	(
		"3.3.1.1.1.5.1",
		155, 
		"Frequency of \"Pass Decision\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.PassDecision
	),
	
	/** Decide to propose. */
	ProposeDecision
	(
		"3.3.1.1.1.6",
		156, 
		"Decide to propose.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		false,
		Concept.NoSiteMoves
	),
	
	/** Frequency of ProposeDecision. */
	ProposeDecisionFrequency
	(
		"3.3.1.1.1.6.1",
		157, 
		"Frequency of \"Propose Decision\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.ProposeDecision
	),
	
	/** */
	SingleSiteMoves
	(
		"3.3.1.1.2",
		158, 
		"Moves.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.MovesDecision
	),
	
	/** (move Add ...) is used. */
	AddDecision
	(
		"3.3.1.1.2.1",
		159, 
		"Decide to add pieces.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false, 
		Concept.SingleSiteMoves
	),
	
	/** Frequency of AddDecision. */
	AddDecisionFrequency
	(
		"3.3.1.1.2.1.1",
		160, 
		"Frequency of \"Add Decision\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.AddDecision
	),
	
	/** (move Promote ...) is used. */
	PromotionDecision
	(
		"3.3.1.1.2.2",
		161, 
		"Promote move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.SingleSiteMoves
	),
	
	/** Frequency of Promotion. */
	PromotionDecisionFrequency
	(
		"3.3.1.1.2.2.1",
		162, 
		"Frequency of \"Promotion Decision\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.PromotionDecision
	),
	
	/** (move Remove ...) is used. */
	RemoveDecision
	(
		"3.3.1.1.2.3",
		163, 
		"Decide to remove pieces.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.SingleSiteMoves
	),
	
	/** Frequency of RemoveDecision. */
	RemoveDecisionFrequency
	(
		"3.3.1.1.2.3.1",
		164, 
		"Frequency of \"Remove Decision\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.RemoveDecision
	),
	
	/** (set Direction ...) is used. */
	RotationDecision
	(
		"3.3.1.1.2.4",
		165, 
		"Rotation move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.SingleSiteMoves
	),
	
	/** Frequency of Rotation Decision. */
	RotationDecisionFrequency
	(
		"3.3.1.1.2.4.1",
		166, 
		"Frequency of \"Rotation Decision\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.RotationDecision
	),
	
	/** */
	TwoSitesMoves
	(
		"3.3.1.1.3",
		167, 
		"Moves.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.MovesDecision
	),
	
	/** (Move Step ...) is used. */
	StepDecision
	(
		"3.3.1.1.3.1",
		168, 
		"Decide to step.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		false,
		Concept.TwoSitesMoves
	),
	
	/** Frequency of StepDecision. */
	StepDecisionFrequency
	(
		"3.3.1.1.3.1.1",
		169, 
		"Frequency of \"Step Decision\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.StepDecision
	),
	
	/** (is Empty ...) condition on (to...) of a step move. */
	StepDecisionToEmpty
	(
		"3.3.1.1.3.1.2",
		170, 
		"Decide to step to an empty site.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.StepDecision
	),
	
	/** Frequency of StepToEmpty. */
	StepDecisionToEmptyFrequency
	(
		"3.3.1.1.3.1.2.1",
		171, 
		"Frequency of \"Step Decision To Empty\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.StepDecisionToEmpty
	),

	/** (is Friend ...) condition on (to...) of a step move. */
	StepDecisionToFriend
	(
		"3.3.1.1.3.1.3",
		172, 
		"Decide to step to a friend piece.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.StepDecision
	),
	
	/** Frequency of StepToEmpty. */
	StepDecisionToFriendFrequency
	(
		"3.3.1.1.3.1.3.1",
		173, 
		"Frequency of \"Step Decision To Friend\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.StepDecisionToFriend
	),

	/** (is Enemy ...) condition on (to...) of a step move. */
	StepDecisionToEnemy
	(
		"3.3.1.1.3.1.4",
		174, 
		"Decide to step to an enemy piece.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.StepDecision
	),
	
	/** Frequency of StepToEnemy. */
	StepDecisionToEnemyFrequency
	(
		"3.3.1.1.3.1.4.1",
		175, 
		"Frequency of \"Step Decision To Enemy\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.StepDecisionToEnemy
	),
	
	/** (move Slide ...) is used */
	SlideDecision
	(
		"3.3.1.1.3.2",
		176, 
		"Decide to slide.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.TwoSitesMoves
	),
	
	/** Frequency of StepToEnemy. */
	SlideDecisionFrequency
	(
		"3.3.1.1.3.2.1",
		177, 
		"Frequency of \"Slide Decision\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.SlideDecision
	),
	
	/** (move Slide ...) is used to move to empty sites. */
	SlideDecisionToEmpty
	(
		"3.3.1.1.3.2.2",
		178, 
		"Slide move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.SlideDecision
	),
	
	/** Frequency of SlideToEmpty. */
	SlideDecisionToEmptyFrequency
	(
		"3.3.1.1.3.2.2.1",
		179, 
		"Frequency of \"Slide Decision To Empty\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.SlideDecisionToEmpty
	),
	
	/**(move Slide ...) is used to move to enemy sites. */
	SlideDecisionToEnemy
	(
		"3.3.1.1.3.2.3",
		180, 
		"Slide move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.SlideDecision
	),
	
	/** Frequency of SlideToEnemy. */
	SlideDecisionToEnemyFrequency
	(
		"3.3.1.1.3.2.3.1",
		181, 
		"Frequency of \"Slide Decision To Enemy\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.SlideDecisionToEnemy
	),
	
	/**(move Slide ...) is used to move to friend sites. */
	SlideDecisionToFriend
	(
		"3.3.1.1.3.2.4",
		182, 
		"Slide move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.SlideDecision
	),
	
	/** Frequency of SlideToFriend. */
	SlideDecisionToFriendFrequency
	(
		"3.3.1.1.3.2.4.1",
		183, 
		"Frequency of \"Slide Decision To Friend\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.SlideDecisionToFriend
	),
	
	/** (move Leap ...) is used. */
	LeapDecision
	(
		"3.3.1.1.3.3",
		184, 
		"Decide to leap.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.TwoSitesMoves
	),
	
	/** Frequency of LeapDecision. */
	LeapDecisionFrequency
	(
		"3.3.1.1.3.3.1",
		185, 
		"Frequency of \"Leap Decision Decision\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.LeapDecision
	),

	/** (is Empty ...) condition on (to...) of a leap move. */
	LeapDecisionToEmpty
	(
		"3.3.1.1.3.3.2",
		186, 
		"Decide to leap to an empty site.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		false,
		Concept.LeapDecision
	),
	
	/** Frequency of LeapToEmpty. */
	LeapDecisionToEmptyFrequency
	(
		"3.3.1.1.3.3.2.1",
		187, 
		"Frequency of \"Leap Decision To Empty\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.LeapDecisionToEmpty
	),

	/** (is Friend ...) condition on (to...) of a leap move. */
	LeapDecisionToFriend
	(
		"3.3.1.1.3.3.3",
		188, 
		"Decide to leap to a friend piece.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		false,
		Concept.LeapDecision
	),
	
	/** Frequency of LeapToFriend. */
	LeapDecisionToFriendFrequency
	(
		"3.3.1.1.3.3.3.1",
		189, 
		"Frequency of \"Leap Decision To Friend\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.LeapDecisionToFriend
	),

	/** (is Enemy ...) condition on (to...) of a leap move. */
	LeapDecisionToEnemy
	(
		"3.3.1.1.3.3.4",
		190, 
		"Decide to leap to an enemy piece.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		false,
		Concept.LeapDecision
	),
	
	/** Frequency of LeapToEnemy. */
	LeapDecisionToEnemyFrequency
	(
		"3.3.1.1.3.3.4.1",
		191, 
		"Frequency of \"Leap Decision To Enemy\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.LeapDecisionToEnemy
	),
	
	/** True if a (move Hop ...) is used. */
	HopDecision
	(
		"3.3.1.1.3.4",
		192, 
		"Decide to hop.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.TwoSitesMoves
	),
	
	/** Frequency of HopDecision. */
	HopDecisionFrequency
	(
		"3.3.1.1.3.4.1",
		193, 
		"Frequency of \"Hop Decision\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.HopDecision
	),
	
	/** True if min range is greater than 1 in any hop decision move. */
	HopDecisionMoreThanOne
	(
		"3.3.1.1.3.4.2",
		194, 
		"Hop more than one site.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.HopDecision
	),
	
	/** Frequency of HopMoreThanOne. */
	HopDecisionMoreThanOneFrequency
	(
		"3.3.1.1.3.4.2.1",
		195, 
		"Frequency of \"Hop Decision More Than One\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.HopDecisionMoreThanOne
	),
	
	/** Hop move with (is Enemy ...) condition in the between and (is Empty ...) in the to. */
	HopDecisionEnemyToEmpty
	(
		"3.3.1.1.3.4.3",
		196, 
		"Hop an enemy to an empty site.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.HopDecision
	),
	
	/** Frequency of HopDecisionEnemyToEmpty. */
	HopDecisionEnemyToEmptyFrequency
	(
		"3.3.1.1.3.4.3.1",
		197, 
		"Frequency of \"Hop Decision Enemy To Empty\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.HopDecisionEnemyToEmpty
	),
	
	/** Decide to hop a friend piece to an empty site. */
	HopDecisionFriendToEmpty
	(
		"3.3.1.1.3.4.4",
		198, 
		"Hop a friend to an empty site.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.HopDecision
	),
	
	/** Frequency of HopDecisionFriendToEmpty. */
	HopDecisionFriendToEmptyFrequency
	(
		"3.3.1.1.3.4.4.1",
		199, 
		"Frequency of \"Hop DecisionFriend To Empty\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.HopDecisionFriendToEmpty
	),
	
	/** Decide to hop an enemy piece to a friend piece. */
	HopDecisionEnemyToFriend
	(
		"3.3.1.1.3.4.5",
		200, 
		"Hop an enemy to a friend piece.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.HopDecision
	),
	
	/** Frequency of HopDecisionEnemyToFriend. */
	HopDecisionEnemyToFriendFrequency
	(
		"3.3.1.1.3.4.5.1",
		201, 
		"Frequency of \"Hop Decision Enemy To Friend\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.HopDecisionEnemyToFriend
	),
	
	/** Decide to hop a friend piece to a friend piece. */
	HopDecisionFriendToFriend
	(
		"3.3.1.1.3.4.6",
		202, 
		"Hop a friend to a friend piece.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[]{ ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.HopDecision
	),
	
	/** Frequency of HopDecisionFriendToFriend. */
	HopDecisionFriendToFriendFrequency
	(
		"3.3.1.1.3.4.6.1",
		203, 
		"Frequency of \"Hop Decision Friend To Friend\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.HopDecisionFriendToFriend
	),
	
	/** Decide to hop an enemy piece to an enemy piece. */
	HopDecisionEnemyToEnemy
	(
		"3.3.1.1.3.4.7",
		204, 
		"Hop an enemy to a enemy piece.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		false,
		Concept.HopDecision
	),
	
	/** Frequency of HopDecisionEnemyToEnemy. */
	HopDecisionEnemyToEnemyFrequency
	(
		"3.3.1.1.3.4.7.1",
		205, 
		"Frequency of \"Hop Decision Enemy To Enemy\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.HopDecisionEnemyToEnemy
	),
	
	/** Decide to hop a friend piece to an enemy piece. */
	HopDecisionFriendToEnemy
	(
		"3.3.1.1.3.4.8",
		206, 
		"Hop a friend to an enemy piece.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.HopDecision
	),
	
	/** Frequency of HopDecisionFriendToEnemy. */
	HopDecisionFriendToEnemyFrequency
	(
		"3.3.1.1.3.4.8.1",
		207, 
		"Frequency of \"Hop Decision Friend To Enemy\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.HopDecisionFriendToEnemy
	),
	
	/** (move (from ...) (to....)). */
	FromToDecision
	(
		"3.3.1.1.3.5",
		208, 
		"Decide to move a piece from a site to another.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.TwoSitesMoves
	),
	
	/** Frequency of FromToDecision. */
	FromToDecisionFrequency
	(
		"3.3.1.1.3.5.1",
		209, 
		"Frequency of \"FromTo Decision\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.FromToDecision
	),
	
	/** Moves concepts. */
	FromToDecisionWithinBoard
	(
		"3.3.1.1.3.5.2",
		210, 
		"Move a piece from a site to another withing the board.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.FromToDecision
	),
	
	/** Frequency of FromToDecisionWithinBoard. */
	FromToDecisionWithinBoardFrequency
	(
		"3.3.1.1.3.5.2.1",
		211, 
		"Frequency of \"FromTo Decision Within Board\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.FromToDecisionWithinBoard
	),
	
	/** Moves concepts. */
	FromToDecisionBetweenContainers
	(
		"3.3.1.1.3.5.3",
		212, 
		"Move a piece from a site to another between 2 different containers.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.FromToDecision
	),
	
	/** Frequency of FromToDecisionBetweenContainers. */
	FromToDecisionBetweenContainersFrequency
	(
		"3.3.1.1.3.5.3.1",
		213, 
		"Frequency of \"FromTo Decision Between Containers\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.FromToDecisionBetweenContainers
	),
	
	/** Moves concepts. */
	FromToDecisionEmpty
	(
		"3.3.1.1.3.5.4",
		214, 
		"Move a piece to an empty site.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.FromToDecision
	),
	
	/** Frequency of FromToDecisionEmpty. */
	FromToDecisionEmptyFrequency
	(
		"3.3.1.1.3.5.4.1",
		215, 
		"Frequency of \"FromTo Decision Empty\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.FromToDecisionEmpty
	),
	
	/** Moves concepts. */
	FromToDecisionEnemy
	(
		"3.3.1.1.3.5.5",
		216, 
		"Move a piece to an enemy site.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.FromToDecision
	),
	
	/** Frequency of FromToDecisionEnemy. */
	FromToDecisionEnemyFrequency
	(
		"3.3.1.1.3.5.5.1",
		217, 
		"Frequency of \"FromTo Decision Enemy\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.FromToDecisionEnemy
	),
	
	/** Moves concepts. */
	FromToDecisionFriend
	(
		"3.3.1.1.3.5.6",
		218, 
		"Move a piece to a friend site.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.FromToDecision
	),
	
	/** Frequency of FromToFriend. */
	FromToDecisionFriendFrequency
	(
		"3.3.1.1.3.5.6.1",
		219, 
		"Frequency of \"FromTo Decision Friend\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.FromToDecisionFriend
	),
	
	/** (move Swap Piece ...) is used. */
	SwapPiecesDecision
	(
		"3.3.1.1.3.6",
		220, 
		"Decide to swap pieces.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		false,
		Concept.TwoSitesMoves
	),
	
	/** Frequency of SwapPiecesDecision. */
	SwapPiecesDecisionFrequency
	(
		"3.3.1.1.3.6.1",
		221, 
		"Frequency of \"Swap Pieces Decision\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.SwapPiecesDecision
	),
	
	/** (move Shoot .... is used). */
	ShootDecision
	(
		"3.3.1.1.3.7",
		222,
		"Decide to shoot.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.TwoSitesMoves
	),
	
	/** Frequency of ShootDecision. */
	ShootDecisionFrequency
	(
		"3.3.1.1.3.7.1",
		223, 
		"Frequency of \"Shoot Decision\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.ShootDecision
	),
	
	/** */
	MovesNonDecision
	(
		"3.3.1.2",
		224, 
		"Moves.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Moves
	),
	
	/** */
	MovesEffects
	(
		"3.3.1.2.1",
		225, 
		"Moves.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.MovesNonDecision
	),
	
	/** (bet ...) */
	BetEffect
	(
		"3.3.1.2.1.1",
		226, 
		"Bet effect.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		false,
		Concept.MovesEffects
	),

	/** Computed with playouts. */
	BetEffectFrequency
	(
		"3.3.1.2.1.1.1",
		227, 
		"Frequency of \"Bet Effect\".",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		true,
		Concept.BetEffect
	),
	
	/** (vote ...) */
	VoteEffect
	(
		"3.3.1.2.1.2",
		228, 
		"Vote effect.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		false,
		Concept.MovesEffects
	),

	/** Computed with playouts. */
	VoteEffectFrequency
	(
		"3.3.1.2.1.2.1",
		229, 
		"Frequency of \"Vote Effect\".",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		true,
		Concept.VoteEffect
	),
	
	/** (swap Players ...). */
	SwapPlayersEffect
	(
		"3.3.1.2.1.3",
		230, 
		"Swap players effect.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.MovesEffects
	),
	
	/** Computed with playouts. */
	SwapPlayersEffectFrequency
	(
		"3.3.1.2.1.3.1",
		231, 
		"Frequency of \"Swap Players Effect\".",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.SwapPlayersEffect
	),
	
	/** Take control of enemy pieces. */
	TakeControl
	(
		"3.3.1.2.1.4",
		232,
		"Take control of enemy pieces.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.MovesEffects
	),

	/** Computed with playouts. */
	TakeControlFrequency
	(
		"3.3.1.2.1.4.1",
		233, 
		"Frequency of \"Take Control\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.TakeControl
	),
	
	/** (pass ...) is used. */
	PassEffect
	(
		"3.3.1.2.1.5",
		234, 
		"Pass a turn.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.MovesEffects
	),
	
	/** Computed with playouts. */
	PassEffectFrequency
	(
		"3.3.1.2.1.5.1",
		235, 
		"Frequency of \"Pass Effect\".",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.PassEffect
	),
	
	/** (roll ...) is used. */
	Roll
	(
		"3.3.1.2.1.6",
		236, 
		"Roll at least a die.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.MovesEffects
	),
	
	/** Computed with playouts. */
	RollFrequency
	(
		"3.3.1.2.1.6.1",
		237, 
		"Frequency of \"Roll\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.Roll
	),
	
	/** */
	ProposeEffect
	(
		"3.3.1.2.1.7",
		238, 
		"Propose a vote effect.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		false,
		Concept.MovesEffects
	),
	
	/** Computed with playouts. */
	ProposeEffectFrequency
	(
		"3.3.1.2.1.7.1",
		239, 
		"Frequency of \"Propose Effect\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.ProposeEffect
	),
	
	/** */
	AddEffect
	(
		"3.3.1.2.1.8",
		240, 
		"Add effect.", 
		ConceptType.Play, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false, 
		Concept.MovesEffects
	),
	
	/** Computed with playouts. */
	AddEffectFrequency
	(
		"3.3.1.2.1.8.1",
		241, 
		"Frequency of \"Add Effect\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.AddEffect
	),

	/** The ludeme (sow ...) is used. */
	Sow
	(
		"3.3.1.2.1.9",
		242, 
		"Sowing stones.", 
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.MovesEffects
	),
	
	/** Computed with playouts. */
	SowFrequency
	(
		"3.3.1.2.1.9.1",
		243, 
		"Frequency of \"Sow\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.Sow
	),
	
	/** The ludeme (sow ...) is used with an effect. */
	SowWithEffect
	(
		"3.3.1.2.1.9.2",
		244, 
		"Sowing moves with effect on final hole.", 
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.Sow
	),
	
	/** The effect of the sow move is to move the captured stones to a store or an hand. */
	SowCapture
	(
		"3.3.1.2.1.9.2.1",
		245, 
		"Sowing with capture.", 
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.SowWithEffect
	),
	
	/** Frequency of SowCapture. */
	SowCaptureFrequency
	(
		"3.3.1.2.1.9.2.1.1",
		246, 
		"Frequency of \"Sow Capture\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.SowCapture
	),
	
	/** The ludeme (sow ...) is used. */
	SowRemove
	(
		"3.3.1.2.1.9.2.2",
		247, 
		"Sowing with seeds removed.", 
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.SowWithEffect
	),
	
	/** Frequency of SowRemove. */
	SowRemoveFrequency
	(
		"3.3.1.2.1.9.2.2.1",
		248, 
		"Frequency of \"Sow Remove\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.SowRemove
	),
	
	/** The ludeme (sow ...) is used with backtracking at true. */
	SowBacktracking
	(
		"3.3.1.2.1.9.2.3",
		249, 
		"Sowing uses backtracking captures.", 
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.SowWithEffect
	),
	
	/** Frequency of SowRemove. */
	SowBacktrackingFrequency
	(
		"3.3.1.2.1.9.2.3.1",
		250, 
		"Frequency of \"Sow Backtracking\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.SowBacktracking
	),
	
	/** Properties of the sow moves (origin, skip, etc...) . */
	SowProperties
	(
		"3.3.1.2.1.9.3",
		251, 
		"Sowing properties.", 
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.Sow
	),
	
	/** The ludeme (sow ...) is used but skip some holes. */
	SowSkip
	(
		"3.3.1.2.1.9.3.1",
		252, 
		"Sowing in skiping some holes.", 
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.SowProperties
	),
	
	/** The ludeme (sow ...) sow first in the origin. */
	SowOriginFirst
	(
		"3.3.1.2.1.9.3.2",
		253, 
		"Sowing in the origin hole first.", 
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		true,
		Concept.SowProperties
	),
	
	/** The track used to sow is CW. */
	SowCW
	(
		"3.3.1.2.1.9.3.3",
		254, 
		"Sowing is performed CW.", 
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		true,
		Concept.SowProperties
	),
	
	/** The track used to sow is CCW. */
	SowCCW
	(
		"3.3.1.2.1.9.3.4",
		255, 
		"Sowing is performed CCW.", 
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		true,
		Concept.SowProperties
	),
	
	/** (promote ...) is used. */
	PromotionEffect
	(
		"3.3.1.2.1.10",
		256, 
		"Promote effect.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.MovesEffects
	),

	/** Computed with playouts. */
	PromotionEffectFrequency
	(
		"3.3.1.2.1.10.1",
		257, 
		"Frequency of \"Promote Effect\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.PromotionEffect
	),
	
	/** (remove ...) is used. */
	RemoveEffect
	(
		"3.3.1.2.1.11",
		258, 
		"Remove effect.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.MovesEffects
	),

	/** Computed with playouts. */
	RemoveEffectFrequency
	(
		"3.3.1.2.1.11.1",
		259, 
		"Frequency of \"Remove Effect\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.RemoveEffect
	),
	
	/** (push ...) is used. */
	PushEffect
	(
		"3.3.1.2.1.12",
		260, 
		"Push move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.MovesEffects
	),
	
	/** Frequency of Push. */
	PushEffectFrequency
	(
		"3.3.1.2.1.12.1",
		261, 
		"Frequency of \"Push Effect\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.PushEffect
	),
	
	/** (flip ...) is used. */
	Flip
	(
		"3.3.1.2.1.13",
		262, 
		"Flip move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.MovesEffects
	),
	
	/** Frequency of Flip. */
	FlipFrequency
	(
		"3.3.1.2.1.13.1",
		263, 
		"Frequency of \"Flip Effect\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.Flip
	),
	
	/**  */
	SetMove
	(
		"3.3.1.2.1.14",
		264, 
		"Set Moves.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.MovesEffects
	),
	
	/** (move Set NextPlayer ..). */
	SetNextPlayer
	(
		"3.3.1.2.1.14.1",
		265, 
		"Decide who is the next player.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.SetMove
	),
	
	/** Frequency of SetNextPlayer. */
	SetNextPlayerFrequency
	(
		"3.3.1.2.1.14.1.1",
		266, 
		"Frequency of \"Set Next Player\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.SetNextPlayer
	),
	
	/** (moveAgain). */
	MoveAgain
	(
		"3.3.1.2.1.14.2",
		267, 
		"Set the next player to the mover.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.SetMove
	),
	
	/** Frequency of MoveAgain. */
	MoveAgainFrequency
	(
		"3.3.1.2.1.14.2.1",
		268, 
		"Frequency of \"Move Again\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.MoveAgain
	),
	
	/** (set Value ..). */
	SetValue
	(
		"3.3.1.2.1.14.3",
		269, 
		"Set the value of a piece.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.SetMove
	),
	
	/** Frequency of SetValue. */
	SetValueFrequency
	(
		"3.3.1.2.1.14.3.1",
		270, 
		"Frequency of \"Set Value\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.SetValue
	),
	
	/** (set Count ..). */
	SetCount
	(
		"3.3.1.2.1.14.4",
		271, 
		"Set the count of a piece.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.SetMove
	),
	
	/** Frequency of SetCount. */
	SetCountFrequency
	(
		"3.3.1.2.1.14.4.1",
		272, 
		"Frequency of \"Set Count\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.SetCount
	),
	
	/** (set Cost ...). */
	SetCost
	(
		"3.3.1.2.1.14.5",
		273, 
		"Set the cost of a graph element.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.SetMove
	),
	
	/** Frequency of SetCost. */
	SetCostFrequency
	(
		"3.3.1.2.1.14.5.1",
		274, 
		"Frequency of \"Set Cost\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.SetCost
	),
	
	/** (set Phase ...). */
	SetPhase
	(
		"3.3.1.2.1.14.6",
		275, 
		"Set the phase of a graph element.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.SetMove
	),
	
	/** Frequency of SetPhase. */
	SetPhaseFrequency
	(
		"3.3.1.2.1.14.6.1",
		276, 
		"Frequency of \"Set Phase\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.SetPhase
	),
	
	/** (set TrumpSuit ..). */
	SetTrumpSuit
	(
		"3.3.1.2.1.14.7",
		277, 
		"Set the trump suit.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.SetMove
	),
	
	/** Frequency of SetTrumpSuit. */
	SetTrumpSuitFrequency
	(
		"3.3.1.2.1.14.7.1",
		278, 
		"Frequency of \"Set Trump Suit\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.SetTrumpSuit
	),

	/** (set Direction ...) is used. */
	SetRotation
	(
		"3.3.1.2.1.14.8",
		279, 
		"Rotation move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.SetMove
	),
	
	/** Frequency of Rotation. */
	SetRotationFrequency
	(
		"3.3.1.2.1.14.8.1",
		280, 
		"Frequency of \"Set Rotation\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.SetRotation
	),
	
	/** */
	StepEffect
	(
		"3.3.1.2.1.15",
		281, 
		"Step effect.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		false,
		Concept.MovesEffects
	),
	
	/** */
	StepEffectFrequency
	(
		"3.3.1.2.1.15.1",
		282, 
		"Frequency of \"Step Effect\".",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		true,
		Concept.StepEffect
	),
	
	/**. */
	SlideEffect
	(
		"3.3.1.2.1.16",
		283, 
		"Slide effect.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.MovesEffects
	),
	
	/** */
	SlideEffectFrequency
	(
		"3.3.1.2.1.16.1",
		284, 
		"Frequency of \"Slide Effect\".",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		true,
		Concept.SlideEffect
	),
	
	/** */
	LeapEffect
	(
		"3.3.1.2.1.17",
		285, 
		"Leap effect.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.MovesEffects
	),
	
	/** */
	LeapEffectFrequency
	(
		"3.3.1.2.1.17.1",
		286, 
		"Frequency of \"Leap Effect\".",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		true,
		Concept.LeapEffect
	),
	
	/** */
	HopEffect
	(
		"3.3.1.2.1.18",
		287, 
		"Hop effect.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.MovesEffects
	),
	
	/** */
	HopEffectFrequency
	(
		"3.3.1.2.1.18.1",
		288, 
		"Frequency of \"Hop Effect\".",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		true,
		Concept.HopEffect
	),
	
	/** (move (from ...) (to....)) or (fromTo ...) is used. */
	FromToEffect
	(
		"3.3.1.2.1.19",
		289, 
		"Effect to move a piece from a site to another.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.MovesEffects
	),
	
	/** */
	FromToEffectFrequency
	(
		"3.3.1.2.1.19.1",
		290, 
		"Frequency of \"FromTo Effect\".",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		true,
		Concept.FromToEffect
	),
	
	/** */
	SwapPiecesEffect
	(
		"3.3.1.2.1.20",
		291, 
		"Swap pieces effect.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		false,
		Concept.MovesEffects
	),
	
	/** */
	SwapPiecesEffectFrequency
	(
		"3.3.1.2.1.20.1",
		292, 
		"Frequency of \"Swap Pieces Effect\".",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		true,
		Concept.SwapPiecesEffect
	),
	
	/** */
	ShootEffect(
		"3.3.1.2.1.21",
		293,
		"Shoot effect.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.MovesEffects
	),
	
	/** */
	ShootEffectFrequency
	(
		"3.3.1.2.1.21.1",
		294, 
		"Frequency of \"Shoot Effect\".",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		true,
		Concept.ShootEffect
	),
	
	/** */
	MovesOperators
	(
		"3.3.1.2.2",
		295, 
		"Moves.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.MovesNonDecision
	),
	
	/** (priority ...) is used. */
	Priority
	(
		"3.3.1.2.2.1",
		296, 
		"Some moves are priority.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.MovesOperators
	),
	
	/** (forEach Die ...) is used. */
	ByDieMove
	(
		"3.3.1.2.2.2",
		297, 
		"Each die can correspond to a different move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.MovesOperators
	),
	
	/** (max Moves ...). */
	MaxMovesInTurn
	(
		"3.3.1.2.2.3",
		298, 
		"Maximise the number of moves in a turn.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.MovesOperators
	),
	
	/** (max Distance ..). */
	MaxDistance
	(
		"3.3.1.2.2.4",
		299, 
		"Maximise the distance to move.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.MovesOperators
	),
	
	/** */
	Capture
	(
		"3.3.2",
		300, 
		"Game involved captures.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Play
	),
	
	/** Replacement captures. */
	ReplacementCapture
	(
		"3.3.2.1",
		301, 
		"Capture in replacing.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.Capture
	),
	
	/** Frequency of ReplacementCapture. */
	ReplacementCaptureFrequency
	(
		"3.3.2.1.1",
		302, 
		"Frequency of \"Replacement Capture\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.ReplacementCapture
	),
	
	/** True if a Remove move is done in the effect of a hop move. */
	HopCapture
	(
		"3.3.2.2",
		303, 
		"Capture in hopping.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.Capture
	),
	
	/** Frequency of HopCapture. */
	HopCaptureFrequency
	(
		"3.3.2.2.1",
		304, 
		"Frequency of \"Hop Capture\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.HopCapture
	),
	
	/** True if a Remove move is done in the effect of a hop move and if the min range is greater than one. */
	HopCaptureMoreThanOne
	(
		"3.3.2.3",
		305, 
		"Capture in hopping many sites.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.Capture
	),
	
	/** Frequency of HopCaptureMoreThanOne. */
	HopCaptureMoreThanOneFrequency
	(
		"3.3.2.3.1",
		306, 
		"Frequency of \"Hop Capture More Than One\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.HopCaptureMoreThanOne
	),
	
	/** (directionCapture ...) is used. */
	DirectionCapture
	(
		"3.3.2.4",
		307, 
		"Capture pieces in a direction.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.Capture
	),
	
	/** Frequency of DirectionCapture. */
	DirectionCaptureFrequency
	(
		"3.3.2.4.1",
		308, 
		"Frequency of \"Direction Capture\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.DirectionCapture
	),
	
	/** (enclose ...) is used. */
	EncloseCapture
	(
		"3.3.2.5",
		309, 
		"Capture in enclosing.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.Capture
	),
	
	/** Frequency of EncloseCapture. */
	EncloseCaptureFrequency
	(
		"3.3.2.5.1",
		310, 
		"Frequency of \"Enclose Capture\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.EncloseCapture
	),
	
	/** (custodial ...) is used. */
	CustodialCapture
	(
		"3.3.2.6",
		311, 
		"Capture in custodial.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.Capture
	),
	
	/** Frequency of CustodialCapture. */
	CustodialCaptureFrequency
	(
		"3.3.2.6.1",
		312, 
		"Frequency of \"Custodial Capture\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.CustodialCapture
	),
	
	/** (intervene ...) is used. */
	InterveneCapture
	(
		"3.3.2.7",
		313, 
		"Intervene capture.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.Capture
	),
	
	/** Frequency of InterveneCapture. */
	InterveneCaptureFrequency
	(
		"3.3.2.7.1",
		314, 
		"Frequency of \"Intervene Capture\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.InterveneCapture
	),
	
	/** (surround ...) is used. */
	SurroundCapture
	(
		"3.3.2.8",
		315, 
		"Ccapture in surrounding.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.Capture
	),
	
	/** Frequency of SurroundCapture. */
	SurroundCaptureFrequency
	(
		"3.3.2.8.1",
		316, 
		"Frequency of \"Surround Capture\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.SurroundCapture
	),
	
	/** when: parameter is not null in (remove ...) */
	CaptureSequence
	(
		"3.3.2.9",
		317,
		"Capture pieces in a sequence at the end of the turn.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Capture
	),
	
	/** Frequency of CaptureSequence. */
	CaptureSequenceFrequency
	(
		"3.3.2.9.1",
		318, 
		"Frequency of \"Capture Sequence\" move.",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.CaptureSequence
	),
	
	/** (max Capture ...) is used. */
	MaxCapture
	(
		"3.3.2.10",
		319, 
		"Maximise the number of captures.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Capture
	),
	
	/** */
	Conditions
	(
		"3.3.3",
		320, 
		"Conditions checked.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Play
	),
	
	/** */
	SpaceConditions
	(
		"3.3.3.1",
		321, 
		"Space conditions.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Conditions
	),
	
	/** (is Line ...) is used. */
	Line
	(
		"3.3.3.1.1",
		322, 
		"Line Detection.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.SpaceConditions
	),
	
	/** (is Connected ...) is used. */
	Connection
	(
		"3.3.3.1.2",
		323, 
		"Connected regions detection.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.SpaceConditions
	),
	
	/** (is Group ...), (count Group ...), (sites Group) or (forEach Group ...) is used. */
	Group
	(
		"3.3.3.1.3",
		324, 
		"Detect a group.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.SpaceConditions
	),
	
	/** (is In ...) is used. */
	Contains
	(
		"3.3.3.1.4",
		325,
		"Detect if a site is in a region.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] {ConceptPurpose.AI, ConceptPurpose.Reconstruction},
		true,
		Concept.SpaceConditions
	),
	
	/** (is Loop ...) or (sites Loop ...) is used. */
	Loop
	(
		"3.3.3.1.5",
		326, 
		"Loop detection.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.SpaceConditions
	),
	
	/** (sites Pattern ...) or (is Pattern ...) is used. */
	Pattern
	(
		"3.3.3.1.6",
		327, 
		"Pattern detection.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.SpaceConditions
	),
	
	/** (pathExtent ...) is used. */
	PathExtent
	(
		"3.3.3.1.7",
		328, 
		"Path extent detection.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.SpaceConditions
	),
	
	/** (size Territory ...) is used. */
	Territory
	(
		"3.3.3.1.8",
		329, 
		"Territory detection.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.SpaceConditions
	),
	
	/** (= (sites Occupied by:....) <RegionFunction>). */
	Fill
	(
		"3.3.3.1.9",
		330, 
		"Check region filled by pieces.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.SpaceConditions
	),
	
	/** (Count Steps ...) is used. */
	Distance
	(
		"3.3.3.1.10",
		331, 
		"Check distance between two sites.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.SpaceConditions
	),
	
	/** */
	MoveConditions
	(
		"3.3.3.2",
		332, 
		"Move conditions.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Conditions
	),
	
	/** Detect if no move. */
	NoMoves
	(
		"3.3.3.2.1",
		333, 
		"Detect no legal moves.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.MoveConditions
	),
	
	/** Detect if no move for the mover. */
	NoMovesMover
	(
		"3.3.3.2.1.1",
		334, 
		"Detect no legal moves for the mover.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.NoMoves
	),
	
	/** Detect if no move for the next player. */
	NoMovesNext
	(
		"3.3.3.2.1.2",
		335, 
		"Detect no legal moves for the next player.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.NoMoves
	),
	
	/** (can Move ...) used. Put to false if CanNotMove is used on the same ludeme. */
	CanMove
	(
		"3.3.3.2.2",
		336, 
		"Check if a piece (or more) can make specific move(s).",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.MoveConditions
	),
	
	/** (not (can Move ...)) used. */
	CanNotMove
	(
		"3.3.3.2.3",
		337, 
		"Check if a piece (or more) can not make specific move(s).",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.MoveConditions
	),
	
	/** */
	PieceConditions
	(
		"3.3.3.3",
		338, 
		"Piece conditions.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Conditions
	),
	
	/** (= 0 (count Pieces ...) ...) is used. */
	NoPiece
	(
		"3.3.3.3.1",
		339, 
		"No piece detection.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.PieceConditions
	),
	
	/** (= 0 (count Pieces Mover ...) ...) is used. */
	NoPieceMover
	(
		"3.3.3.3.1.1",
		340, 
		"No piece detection for the pieces of the mover.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.NoPiece
	),
	
	/** (= 0 (count Pieces Next...) ...) is used. */
	NoPieceNext
	(
		"3.3.3.3.1.2",
		341, 
		"No piece detection for the pieces of the next player.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.NoPiece
	),
	
	/** (= Off (where ...) ...) is used. */
	NoTargetPiece
	(
		"3.3.3.3.2", 
		342, 
		"No target piece detection.", 
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.PieceConditions
	),

	/** (is Threatened ...) is used. */
	Threat
	(
		"3.3.3.3.3",
		343, 
		"Piece under threat detection.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.PieceConditions
	),
	
	/** (is Empty ...) is used. */
	IsEmpty
	(
		"3.3.3.3.4",
		344, 
		"Empty site detection.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.PieceConditions
	),
	
	/** (is Enemy ...) is used. */
	IsEnemy
	(
		"3.3.3.3.5",
		345, 
		"Occupied site by enemy detection.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.PieceConditions
	),
	
	/** (is Friend ...) is used. */
	IsFriend
	(
		"3.3.3.3.6",
		346, 
		"Occupied site by friend detection.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.PieceConditions
	),
	
	/** (sites LineOfSight ...), (slide ...) or (shoot ...) is used.. */
	LineOfSight
	(
		"3.3.3.3.7",
		347, 
		"Line of sight of pieces used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.PieceConditions
	),
	
	/** (</<=/>/>= <IntFunction> (count Pieces ...)) is used. */
	CountPiecesComparison
	(
		"3.3.3.3.8",
		348, 
		"The number of pieces is compared.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.PieceConditions
	),
	
	/** (</<=/>/>= <IntFunction> (count Pieces Mover...)) is used. */
	CountPiecesMoverComparison
	(
		"3.3.3.3.8.1",
		349, 
		"The number of pieces of the mover is compared.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.CountPiecesComparison
	),
	
	/** (</<=/>/>= <IntFunction> (count Pieces Next ...)) is used. */
	CountPiecesNextComparison
	(
		"3.3.3.3.8.2",
		350, 
		"The number of pieces of the next player is compared.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.CountPiecesComparison
	),
	
	/** True if the counter is used to check the progress of the game. */
	ProgressCheck
	(
		"3.3.3.4",
		351, 
		"Progress condition.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Conditions
	),
	
	/** */
	Directions
	(
		"3.3.4",
		352, 
		"Directions used.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[]{ ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Play
	),
	
	/** */
	AbsoluteDirections
	(
		"3.3.4.1",
		353, 
		"Absolute directions used.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Directions
	),
	
	/** All enum is used in the directions. */
	AllDirections
	(
		"3.3.4.1.1",
		354,
		"All directions used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.AbsoluteDirections
	),
	
	/** Adjacent enum is used. */
	AdjacentDirection
	(
		"3.3.4.1.2",
		355,
		"Adjacent directions used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.AbsoluteDirections
	),
	
	/** Orthogonal enum is used. */
	OrthogonalDirection
	(
		"3.3.4.1.3",
		356,
		"Orthogonal directions used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.AbsoluteDirections
	),
	
	/** Diagonal enum is used. */
	DiagonalDirection
	(
		"3.3.4.1.4",
		357,
		"Diagonal directions used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.AbsoluteDirections
	),
	
	/** OffDiagonal used. */
	OffDiagonalDirection
	(
		"3.3.4.1.5",
		358,
		"Off diagonal directions used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.AbsoluteDirections
	),
	
	/** Rotational enum used. */
	RotationalDirection
	(
		"3.3.4.1.6",
		359,
		"Rotational directions used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.AbsoluteDirections
	),
	
	/** SameLayer enum used. */
	SameLayerDirection
	(
		"3.3.4.1.7",
		360,
		"Same layer directions used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.AbsoluteDirections
	),
	
	/** */
	RelativeDirections
	(
		"3.3.4.2",
		361, 
		"Directions used.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Directions
	),
	
	/** Forward enum is used. */
	ForwardDirection
	(
		"3.3.4.2.1",
		362,
		"Forward direction used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.RelativeDirections
	),
	
	/** Backward enum is used. */
	BackwardDirection
	(
		"3.3.4.2.2",
		363,
		"Backward direction used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.RelativeDirections
	),
	
	/** Forwards enum is used. */
	ForwardsDirection
	(
		"3.3.4.2.3",
		364,
		"Forwards direction used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.RelativeDirections
	),
	
	/** Backwards enum is used. */
	BackwardsDirection
	(
		"3.3.4.2.4",
		365,
		"Backwards direction used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.RelativeDirections
	),
	
	/** Rightward enum is used. */
	RightwardDirection
	(
		"3.3.4.2.5",
		366,
		"Rightward direction used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.RelativeDirections
	),
	
	/** Leftward enum is used. */
	LeftwardDirection
	(
		"3.3.4.2.6",
		367,
		"Leftward direction used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.RelativeDirections
	),
	
	/** Rightwards enum is used. */
	RightwardsDirection
	(
		"3.3.4.2.7",
		368,
		"Rightwards direction used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.RelativeDirections
	),
	
	/** Leftwards enum is used. */
	LeftwardsDirection
	(
		"3.3.4.2.8",
		369,
		"Leftwards direction used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.RelativeDirections
	),
	
	/** FL enum is used. */
	ForwardLeftDirection
	(
		"3.3.4.2.9",
		370,
		"Forward left direction used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.RelativeDirections
	),
	
	/** FR enum is used. */
	ForwardRightDirection
	(
		"3.3.4.2.10",
		371,
		"Forward right direction used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.RelativeDirections
	),
	
	/** BL enum is used. */
	BackwardLeftDirection(
		"3.3.4.2.11",
		372,
		"Backward left direction used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.RelativeDirections
	),
	
	/** BR enum is used. */
	BackwardRightDirection
	(
		"3.3.4.2.12",
		373,
		"Use backward right direction.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.RelativeDirections
	),
	
	/** SameDirection is used. */
	SameDirection
	(
		"3.3.4.2.13",
		374,
		"Same direction of the previous move used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.RelativeDirections
	),
	
	/** OppositeDirection is used. */
	OppositeDirection
	(
		"3.3.4.2.14",
		375,
		"Opposite direction of the previous move used.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.RelativeDirections
	),
	
	/** */
	Information
	(
		"3.3.5",
		376, 
		"Information.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Play
	),
	
	/** Any ludeme hiding the piece type. */
	HidePieceType
	(
		"3.3.5.1",
		377, 
		"Hide piece type.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Information
	),
	
	/** Any ludeme hiding the piece owner. */
	HidePieceOwner
	(
		"3.3.5.2",
		378, 
		"Hide piece owner.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },  
		true,
		Concept.Information
	),
	
	/** Any ludeme hiding the piece count. */
	HidePieceCount
	(
		"3.3.5.3",
		379, 
		"Hide number of pieces.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },  
		true,
		Concept.Information
	),
	
	/** Any ludeme hiding the piece rotation. */
	HidePieceRotation
	(
		"3.3.5.4",
		380, 
		"Hide piece rotation.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },  
		true,
		Concept.Information
	),
	
	/** Any ludeme hiding the piece value. */
	HidePieceValue
	(
		"3.3.5.5",
		381, 
		"Hide piece value.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true,
		Concept.Information
	),
	
	/** Any ludeme hiding the piece state. */
	HidePieceState
	(
		"3.3.5.6",
		382, 
		"Hide the site state.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },  
		true,
		Concept.Information
	),
	
	/** Any ludeme hidding all the info on a site. */
	InvisiblePiece
	(
		"3.3.5.7",
		383, 
		"Piece can be invisible.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },  
		true,
		Concept.Information
	),
	
	/** (phase ...) is used. */
	Phase
	(
		"3.3.6",
		384, 
		"Phases of play.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Play
	),
	
	/** Number of play phases. */
	NumPlayPhase
	(
		"3.3.6.1",
		385, 
		"Number of play phases.",
		ConceptType.Play, 
		ConceptDataType.IntegerData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Phase
	),
	
	/** (set Score ...) is used. */
	Scoring
	(
		"3.3.7",
		386, 
		"Involve scores.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Play
	),
	
	/** If any ludeme can make the count of a site to be bigger than 1 or if a stack of the same piece can be placed. */
	PieceCount
	(
		"3.3.8",
		387, 
		"Many pieces of the same type on a site.",
		ConceptType.Play,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.Play
	),
	
	/** (count Pips) is used. */
	SumDice
	(
		"3.3.9",
		388, 
		"Use sum of all dice.",
		ConceptType.Play, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Play
	),
	
	
	//-------------------------------------------------------------------------
    //                                 End
	//-------------------------------------------------------------------------
	
	/** */
	End
	(
		"3.4",
		389, 
		"Rules for ending the game.",
		ConceptType.End,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Rules
	),
	
	/** */
	SpaceEnd
	(
		"3.4.1",
		390, 
		"Space ending rules.",
		ConceptType.End,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.End
	),
	
	/** Line concept true in an ending condition. */
	LineEnd
	(
		"3.4.1.1",
		391, 
		"End in making a line.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.SpaceEnd
	),
	
	/** Frequency of LineEnd. */
	LineEndFrequency
	(
		"3.4.1.1.1",
		392, 
		"Frequency of \"Line End\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.LineEnd
	),
	
	/** Line concept true in an ending condition if a non-next player win. */
	LineWin
	(
		"3.4.1.1.2",
		393, 
		"Win in making a line.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.LineEnd
	),
	
	/** Frequency of LineWin. */
	LineWinFrequency
	(
		"3.4.1.1.2.1",
		394, 
		"Frequency of \"Line Win\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.LineWin
	),
	
	/** Line concept true in an ending condition if a non-next player loss. */
	LineLoss
	(
		"3.4.1.1.3",
		395, 
		"Loss in making a line.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.LineEnd
	),
	
	/** Frequency of LineLoss. */
	LineLossFrequency
	(
		"3.4.1.1.3.1",
		396, 
		"Frequency of \"Line Loss\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.LineLoss
	),
	
	/** Line concept true in an ending condition is a draw. */
	LineDraw
	(
		"3.4.1.1.4",
		397, 
		"Draw in making a line.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.LineEnd
	),
	
	/** Frequency of LineDrawn. */
	LineDrawFrequency
	(
		"3.4.1.1.4.1",
		398, 
		"Frequency of \"Line Draw\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.LineDraw
	),
	
	/** Connection concept true in an ending condition. */
	ConnectionEnd
	(
		"3.4.1.2",
		399, 
		"End if connected regions.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.SpaceEnd
	),
	
	/** Frequency of ConnectionEnd. */
	ConnectionEndFrequency
	(
		"3.4.1.2.1",
		400, 
		"Frequency of \"Connection End\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.ConnectionEnd
	),
	
	/** Connection concept true in an ending condition if a non-next player win. */
	ConnectionWin
	(
		"3.4.1.2.2",
		401, 
		"Win in connecting regions.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.ConnectionEnd
	),
	
	/** Frequency of ConnectionWin. */
	ConnectionWinFrequency
	(
		"3.4.1.2.2.1",
		402, 
		"Frequency of \"Connection Win\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.ConnectionWin
	),
	
	/** Connection concept true in an ending condition if a non-next player loss. */
	ConnectionLoss
	(
		"3.4.1.2.3",
		403, 
		"Loss in connecting regions.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.ConnectionEnd
	),
	
	/** Frequency of ConnectionLoss. */
	ConnectionLossFrequency
	(
		"3.4.1.2.3.1",
		404, 
		"Frequency of \"Connection Loss\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.ConnectionLoss
	),
	
	/** Connection concept true in an ending condition is a draw. */
	ConnectionDraw
	(
		"3.4.1.2.4",
		405, 
		"Draw in connecting regions.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.ConnectionEnd
	),
	
	/** Frequency of LineDrawn. */
	ConnectionDrawFrequency
	(
		"3.4.1.2.4.1",
		406, 
		"Frequency of \"Connection Draw\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.ConnectionDraw
	),
	
	/** Group concept true in an ending condition. */
	GroupEnd
	(
		"3.4.1.3",
		407, 
		"End in making a group.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.SpaceEnd
	),
	
	/** Frequency of GroupEnd. */
	GroupEndFrequency
	(
		"3.4.1.3.1",
		408, 
		"Frequency of \"Group End\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.GroupEnd
	),
	
	/** Group concept true in an ending condition if a non-next player win. */
	GroupWin
	(
		"3.4.1.3.2",
		409, 
		"Win in making a group.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.GroupEnd
	),
	
	/** Frequency of GroupWin. */
	GroupWinFrequency
	(
		"3.4.1.3.2.1",
		410, 
		"Frequency of \"Group Win\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.GroupWin
	),
	
	/** Group concept true in an ending condition if a non-next player loss. */
	GroupLoss
	(
		"3.4.1.3.3",
		411, 
		"Loss in making a group.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.GroupEnd
	),
	
	/** Frequency of ConnectionLoss. */
	GroupLossFrequency
	(
		"3.4.1.3.3.1",
		412, 
		"Frequency of \"Group Loss\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.GroupLoss
	),
	
	/** Group concept true in an ending condition is a draw. */
	GroupDraw
	(
		"3.4.1.3.4",
		413, 
		"Draw in making a group.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.GroupEnd
	),
	
	/** Frequency of GroupDrawn. */
	GroupDrawFrequency
	(
		"3.4.1.3.4.1",
		414, 
		"Frequency of \"Group Draw\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.GroupDraw
	),
	
	/** Loop concept true in an ending condition. */
	LoopEnd
	(
		"3.4.1.4",
		415, 
		"End in making a loop.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.SpaceEnd
	),
	
	/** Frequency of LoopEnd. */
	LoopEndFrequency
	(
		"3.4.1.4.1",
		416, 
		"Frequency of \"Loop End\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.LoopEnd
	),
	
	/** Loop concept true in an ending condition if a non-next player win. */
	LoopWin
	(
		"3.4.1.4.2",
		417, 
		"Win in making a loop.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.LoopEnd
	),
	
	/** Frequency of LoopWin. */
	LoopWinFrequency
	(
		"3.4.1.4.2.1",
		418, 
		"Frequency of \"Loop Win\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.LoopWin
	),
	
	/** Loop concept true in an ending condition if a non-next player loss. */
	LoopLoss
	(
		"3.4.1.4.3",
		419, 
		"Loss in making a loop.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.LoopEnd
	),
	
	/** Frequency of LoopLoss. */
	LoopLossFrequency
	(
		"3.4.1.4.3.1",
		420, 
		"Frequency of \"Loop Loss\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.LoopLoss
	),
	
	/** Loop concept true in an ending condition is a draw. */
	LoopDraw
	(
		"3.4.1.4.4",
		421, 
		"Draw in making a loop.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.LoopEnd
	),
	
	/** Frequency of LoopDrawn. */
	LoopDrawFrequency
	(
		"3.4.1.4.4.1",
		422, 
		"Frequency of \"Loop Draw\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.LoopDraw
	),
	
	/** Pattern concept in an ending condition. */
	PatternEnd
	(
		"3.4.1.5",
		423, 
		"End in making a pattern.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.SpaceEnd
	),
	
	/** Frequency of PatternEnd. */
	PatternEndFrequency
	(
		"3.4.1.5.1",
		424, 
		"Frequency of \"Pattern End\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.PatternEnd
	),
	
	/** Pattern concept true in an ending condition if a non-next player win. */
	PatternWin
	(
		"3.4.1.5.2",
		425, 
		"Win in making a pattern.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.PatternEnd
	),
	
	/** Frequency of PatternWin. */
	PatternWinFrequency
	(
		"3.4.1.5.2.1",
		426, 
		"Frequency of \"Pattern Win\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.PatternWin
	),
	
	/** Pattern concept true in an ending condition if a non-next player loss. */
	PatternLoss
	(
		"3.4.1.5.3",
		427, 
		"Loss in making a pattern.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.PatternEnd
	),
	
	/** Frequency of PatternLoss. */
	PatternLossFrequency
	(
		"3.4.1.5.3.1",
		428, 
		"Frequency of \"Pattern Loss\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.PatternLoss
	),
	
	/** Pattern concept true in an ending condition is a draw. */
	PatternDraw
	(
		"3.4.1.5.4",
		429, 
		"Draw in making a Pattern.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.PatternEnd
	),
	
	/** Frequency of PatternDrawn. */
	PatternDrawFrequency
	(
		"3.4.1.5.4.1",
		430, 
		"Frequency of \"Pattern Draw\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.PatternDraw
	),
	
	/** PathExtent concept in an ending condition. */
	PathExtentEnd
	(
		"3.4.1.6",
		431, 
		"End with a path extent.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.SpaceEnd
	),
	
	/** Frequency of PathExtentEnd. */
	PathExtentEndFrequency
	(
		"3.4.1.6.1",
		432, 
		"Frequency of \"Path Extent End\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.PathExtentEnd
	),
	
	/** PathExtent concept true in an ending condition if a non-next player win. */
	PathExtentWin
	(
		"3.4.1.6.2",
		433, 
		"Win with a path extent.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.PathExtentEnd
	),
	
	/** Frequency of PathExtentWin. */
	PathExtentWinFrequency
	(
		"3.4.1.6.2.1",
		434, 
		"Frequency of \"PathExtent Win\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.PathExtentWin
	),
	
	/** PathExtent concept true in an ending condition if a non-next player loss. */
	PathExtentLoss
	(
		"3.4.1.6.3",
		435, 
		"Loss with a path extent.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.PathExtentEnd
	),
	
	/** Frequency of PathExtentLoss. */
	PathExtentLossFrequency
	(
		"3.4.1.6.3.1",
		436, 
		"Frequency of \"PathExtent Loss\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.PathExtentLoss
	),
	
	/** PathExtent concept true in an ending condition is a draw. */
	PathExtentDraw
	(
		"3.4.1.6.4",
		437, 
		"Draw with a path extent.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.PathExtentEnd
	),
	
	/** Frequency of PathExtentDrawn. */
	PathExtentDrawFrequency
	(
		"3.4.1.6.4.1",
		438, 
		"Frequency of \"PathExtent Draw\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.PathExtentDraw
	),
	
	/** Territory concept in an ending condition. */
	TerritoryEnd
	(
		"3.4.1.7",
		439, 
		"End related to a territory.",
		ConceptType.End,
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.SpaceEnd
	),
	
	/** Frequency of TerritoryEnd. */
	TerritoryEndFrequency
	(
		"3.4.1.7.1",
		440, 
		"Frequency of \"Territory End\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.TerritoryEnd
	),
	
	/** TerritoryEnd concept true in an ending condition if a non-next player win. */
	TerritoryWin
	(
		"3.4.1.7.2",
		441, 
		"Win related to a territory.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.TerritoryEnd
	),
	
	/** Frequency of TerritoryWin. */
	TerritoryWinFrequency
	(
		"3.4.1.7.2.1",
		442, 
		"Frequency of \"Territory Win\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.TerritoryWin
	),
	
	/** Territory concept true in an ending condition if a non-next player loss. */
	TerritoryLoss
	(
		"3.4.1.7.3",
		443, 
		"Loss related to a territory.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.TerritoryEnd
	),
	
	/** Frequency of TerritoryLoss. */
	TerritoryLossFrequency
	(
		"3.4.1.7.3.1",
		444, 
		"Frequency of \"Territory Loss\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.TerritoryLoss
	),
	
	/** Territory concept true in an ending condition is a draw. */
	TerritoryDraw
	(
		"3.4.1.7.4",
		445, 
		"Draw related to a territory.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.TerritoryEnd
	),
	
	/** Frequency of TerritoryDrawn. */
	TerritoryDrawFrequency
	(
		"3.4.1.7.4.1",
		446, 
		"Frequency of \"Territory Draw\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.TerritoryDraw
	),
	
	/** */
	CaptureEnd
	(
		"3.4.2",
		447, 
		"Capture ending rules.",
		ConceptType.End,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.End
	),
	
	/** End with (CanNotMove + Threat). */
	Checkmate
	(
		"3.4.2.1",
		448, 
		"End if checkmate.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.CaptureEnd
	),
	
	/** Frequency of Checkmate. */
	CheckmateFrequency
	(
		"3.4.2.1.1",
		449, 
		"Frequency of \"Checkmate\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.Checkmate
	),
	
	/** Checkmate concept true in an ending condition if a non-next player win. */
	CheckmateWin
	(
		"3.4.2.1.2",
		450, 
		"Win if checkmate.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.Checkmate
	),
	
	/** Frequency of CheckmateWin. */
	CheckmateWinFrequency
	(
		"3.4.2.1.2.1",
		451, 
		"Frequency of \"Checkmate Win\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.CheckmateWin
	),
	
	/** Checkmate concept true in an ending condition if a non-next player loss. */
	CheckmateLoss
	(
		"3.4.2.1.3",
		452, 
		"Loss if checkmate.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.Checkmate
	),
	
	/** Frequency of CheckmateLoss. */
	CheckmateLossFrequency
	(
		"3.4.2.1.3.1",
		453, 
		"Frequency of \"Checkmate Loss\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.CheckmateLoss
	),
	
	/** Checkmate concept true in an ending condition is a draw. */
	CheckmateDraw
	(
		"3.4.2.1.4",
		454, 
		"Draw if checkmate.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.Checkmate
	),
	
	/** Frequency of CheckmateDrawn. */
	CheckmateDrawFrequency
	(
		"3.4.2.1.4.1",
		455, 
		"Frequency of \"Checkmate Draw\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.CheckmateDraw
	),
	
	/** End with (NoTargetPiece). */
	NoTargetPieceEnd
	(
		"3.4.2.2",
		456, 
		"End if a target piece is removed.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.CaptureEnd
	),
	
	/** Frequency of NoTargetPieceEnd. */
	NoTargetPieceEndFrequency
	(
		"3.4.2.2.1",
		457, 
		"Frequency of \"No Target Piece End\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.NoTargetPieceEnd
	),
	
	/** NoTargetPiece concept true in an ending condition if a non-next player win. */
	NoTargetPieceWin
	(
		"3.4.2.2.2",
		458, 
		"Win if a target piece is removed.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.NoTargetPieceEnd
	),
	
	/** Frequency of NoTargetPieceWin. */
	NoTargetPieceWinFrequency
	(
		"3.4.2.2.2.1",
		459, 
		"Frequency of \"No Target Piece Win\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.NoTargetPieceWin
	),
	
	/** NoTargetPiece concept true in an ending condition if a non-next player loss. */
	NoTargetPieceLoss
	(
		"3.4.2.2.3",
		460, 
		"Loss if a target piece is removed.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.NoTargetPieceEnd
	),
	
	/** Frequency of NoTargetPieceLoss. */
	NoTargetPieceLossFrequency
	(
		"3.4.2.2.3.1",
		461, 
		"Frequency of \"No Target Piece Loss\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.NoTargetPieceLoss
	),
	
	/** NoTargetPiece concept true in an ending condition is a draw. */
	NoTargetPieceDraw
	(
		"3.4.2.2.4",
		462, 
		"Draw if a target piece is removed.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.NoTargetPieceEnd
	),
	
	/** Frequency of NoTargetPieceDrawn. */
	NoTargetPieceDrawFrequency
	(
		"3.4.2.2.4.1",
		463, 
		"Frequency of \"No Target Piece Draw\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.NoTargetPieceDraw
	),
	
	/** End with (= 0 (count Pieces Next...) .... (result Mover Win)) or equivalent. */
	EliminatePiecesEnd
	(
		"3.4.2.3",
		464, 
		"End if all enemy pieces are removed.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.CaptureEnd
	),
	
	/** Frequency of EliminatePiecesEnd. */
	EliminatePiecesEndFrequency
	(
		"3.4.2.3.1",
		465, 
		"Frequency of \"Eliminate All Pieces End\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.EliminatePiecesEnd
	),
	
	/** NoPiece concept true in an ending condition if a non-next player win. */
	EliminatePiecesWin
	(
		"3.4.2.3.2",
		466, 
		"Win if all enemy pieces are removed.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.EliminatePiecesEnd
	),
	
	/** Frequency of EliminatePiecesWin. */
	EliminatePiecesWinFrequency
	(
		"3.4.2.3.2.1",
		467, 
		"Frequency of \"Eliminate Pieces Win\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.EliminatePiecesWin
	),
	
	/** NoPiece concept true in an ending condition if a non-next player loss. */
	EliminatePiecesLoss
	(
		"3.4.2.3.3",
		468, 
		"Loss if all enemy pieces are removed.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.EliminatePiecesEnd
	),
	
	/** Frequency of EliminatePiecesLoss. */
	EliminatePiecesLossFrequency
	(
		"3.4.2.3.3.1",
		469, 
		"Frequency of \"Eliminate Pieces Loss\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.EliminatePiecesLoss
	),
	
	/** NoPiece concept true in an ending condition is a draw. */
	EliminatePiecesDraw
	(
		"3.4.2.3.4",
		470, 
		"Draw if a target piece is removed.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.EliminatePiecesEnd
	),
	
	/** Frequency of EliminatePiecesDraw. */
	EliminatePiecesDrawFrequency
	(
		"3.4.2.3.4.1",
		471, 
		"Frequency of \"Eliminate Pieces Draw\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.EliminatePiecesDraw
	),
	
	/** */
	RaceEnd
	(
		"3.4.3",
		472, 
		"Race ending rules.",
		ConceptType.End,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.End
	),
	
	/** No Piece test in End + Win in result type. */
	NoOwnPiecesEnd
	(
		"3.4.3.1",
		473, 
		"End if all own pieces removed (escape games).",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.RaceEnd
	),
	
	/** Frequency of NoOwnPieces. */
	NoOwnPiecesEndFrequency
	(
		"3.4.3.1.1",
		474, 
		"Frequency of \"No Own Pieces End\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.NoOwnPiecesEnd
	),
	
	/** NoOwnPieces concept true in an ending condition if a non-next player win. */
	NoOwnPiecesWin
	(
		"3.4.3.1.2",
		475, 
		"Win if all own pieces removed.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.NoOwnPiecesEnd
	),
	
	/** Frequency of NoOwnPiecesWin. */
	NoOwnPiecesWinFrequency
	(
		"3.4.3.1.2.1",
		476, 
		"Frequency of \"No Own Pieces Win\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.NoOwnPiecesWin
	),
	
	/** NoOwnPieces concept true in an ending condition if a non-next player loss. */
	NoOwnPiecesLoss
	(
		"3.4.3.1.3",
		477, 
		"Loss if all own pieces are removed.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.NoOwnPiecesEnd
	),
	
	/** Frequency of NoOwnPiecesLoss. */
	NoOwnPiecesLossFrequency
	(
		"3.4.3.1.3.1",
		478, 
		"Frequency of \"No Own Pieces Loss\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.NoOwnPiecesLoss
	),
	
	/** NoOwnPieces concept true in an ending condition is a draw. */
	NoOwnPiecesDraw
	(
		"3.4.3.1.4",
		479, 
		"Draw if all own pieces are removed.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.NoOwnPiecesEnd
	),
	
	/** Frequency of NoOwnPiecesDraw. */
	NoOwnPiecesDrawFrequency
	(
		"3.4.3.1.4.1",
		480, 
		"Frequency of \"No Own Pieces Draw\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.NoOwnPiecesDraw
	),
	
	/** Fill concept in the ending conditions. */
	FillEnd
	(
		"3.4.3.2",
		481, 
		"End in filling a region.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },  
		false,
		Concept.RaceEnd
	),
	
	/** Frequency of FillEnd. */
	FillEndFrequency
	(
		"3.4.3.2.1",
		482, 
		"Frequency of \"Fill End\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.FillEnd
	),
	
	/** Fill concept true in an ending condition if a non-next player win. */
	FillWin
	(
		"3.4.3.2.2",
		483, 
		"Win in filling a region.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.FillEnd
	),
	
	/** Frequency of FillWin. */
	FillWinFrequency
	(
		"3.4.3.2.2.1",
		484, 
		"Frequency of \"Fill Win\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.FillWin
	),
	
	/** Fill concept true in an ending condition if a non-next player loss. */
	FillLoss
	(
		"3.4.3.2.3",
		485, 
		"Loss in filling a region.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.FillEnd
	),
	
	/** Frequency of FillLoss. */
	FillLossFrequency
	(
		"3.4.3.2.3.1",
		486, 
		"Frequency of \"Fill Loss\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.FillLoss
	),
	
	/** Fill concept true in an ending condition is a draw. */
	FillDraw
	(
		"3.4.3.2.4",
		487, 
		"Draw in filling a region.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.FillEnd
	),
	
	/** Frequency of FillDraw. */
	FillDrawFrequency
	(
		"3.4.3.2.4.1",
		488, 
		"Frequency of \"Fill Draw\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.FillDraw
	),
	
	/** Contains concept in the ending condition. */
	ReachEnd
	(
		"3.4.3.3",
		489, 
		"End in reaching a region.",
		ConceptType.End, 
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },  
		false,
		Concept.RaceEnd
	),
	
	/** Frequency of ReachEnd. */
	ReachEndFrequency
	(
		"3.4.3.3.1",
		490, 
		"Frequency of \"Reach End\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.ReachEnd
	),
	
	/** Contains concept true in an ending condition if a non-next player win. */
	ReachWin
	(
		"3.4.3.3.2",
		491, 
		"Win in reaching a region.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.ReachEnd
	),
	
	/** Frequency of ReachWin. */
	ReachWinFrequency
	(
		"3.4.3.3.2.1",
		492, 
		"Frequency of \"Reach Win\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.ReachWin
	),
	
	/** Contains concept true in an ending condition if a non-next player loss. */
	ReachLoss
	(
		"3.4.3.3.3",
		493, 
		"Loss in reaching a region.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.ReachEnd
	),
	
	/** Frequency of ReachLoss. */
	ReachLossFrequency
	(
		"3.4.3.3.3.1",
		494, 
		"Frequency of \"Reach Loss\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.ReachLoss
	),
	
	/** Contains concept true in an ending condition is a draw. */
	ReachDraw
	(
		"3.4.3.3.4",
		495, 
		"Draw in reaching a region.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.ReachEnd
	),
	
	/** Frequency of ReachDraw. */
	ReachDrawFrequency
	(
		"3.4.3.3.4.1",
		496, 
		"Frequency of \"Reach Draw\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.ReachDraw
	),
	
	/** (byScore ...) ending condition. */
	ScoringEnd
	(
		"3.4.4",
		497, 
		"End in comparing scores.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.End
	),
	
	/** Frequency of ScoringEnd. */
	ScoringEndFrequency
	(
		"3.4.4.1",
		498, 
		"Frequency of \"Scoring End\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.ScoringEnd
	),
	
	/** Score concept true in an ending condition if a non-next player win. */
	ScoringWin
	(
		"3.4.4.2",
		499, 
		"Win in comparing score.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.ScoringEnd
	),
	
	/** Frequency of ScoreWin. */
	ScoringWinFrequency
	(
		"3.4.4.2.1",
		500, 
		"Frequency of \"Score Win\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.ScoringWin
	),
	
	/** Score concept true in an ending condition if a non-next player loss. */
	ScoringLoss
	(
		"3.4.4.3",
		501, 
		"Loss in comparing score.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.ScoringEnd
	),
	
	/** Frequency of ReachLoss. */
	ScoringLossFrequency
	(
		"3.4.4.3.1",
		502, 
		"Frequency of \"Score Loss\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.ScoringLoss
	),
	
	/** Contains concept true in an ending condition is a draw. */
	ScoringDraw
	(
		"3.4.4.4",
		503, 
		"Draw in comparing score.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.ScoringEnd
	),
	
	/** Frequency of ReachDraw. */
	ScoringDrawFrequency
	(
		"3.4.4.4.1",
		504, 
		"Frequency of \"Reach Draw\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.ScoringDraw
	),
	
	/** End if no moves. */
	NoMovesEnd
	(
		"3.4.5",
		505, 
		"End if no legal moves (stalemate).",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.End
	),
	
	/** Frequency of StalemateEnd. */
	NoMovesEndFrequency
	(
		"3.4.5.1",
		506, 
		"Frequency of \"No Moves End\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.NoMovesEnd
	),
	
	/** NoMoves concept true in an ending condition if a non-next player win. */
	NoMovesWin
	(
		"3.4.5.2",
		507, 
		"Win if no legal moves.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.NoMovesEnd
	),
	
	/** Frequency of NoMovesWin. */
	NoMovesWinFrequency
	(
		"3.4.5.2.1",
		508, 
		"Frequency of \"No Moves Win\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.NoMovesWin
	),
	
	/** NoMoves concept true in an ending condition if a non-next player loss. */
	NoMovesLoss
	(
		"3.4.5.3",
		509, 
		"Loss if no legal moves.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.NoMovesEnd
	),
	
	/** Frequency of NoMovesLoss. */
	NoMovesLossFrequency
	(
		"3.4.5.3.1",
		510, 
		"Frequency of \"No Moves Loss\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.NoMovesLoss
	),
	
	/** NoMoves concept true in an ending condition is a draw. */
	NoMovesDraw
	(
		"3.4.5.4",
		511, 
		"Draw if no legal moves.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.NoMovesEnd
	),
	
	/** Frequency of NoMovesDraw. */
	NoMovesDrawFrequency
	(
		"3.4.5.4.1",
		512, 
		"Frequency of \"No Moves Draw\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.NoMovesDraw
	),
	
	/** The counter is used in the ending rules. */
	NoProgressEnd
	(
		"3.4.6",
		513, 
		"The game does not progress to an end (e.g. 50 moves rule in Chess).",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.End
	),
	
	/** Frequency of NoProgressEnd. */
	NoProgressEndFrequency
	(
		"3.4.6.1",
		514, 
		"Frequency of \"No Progress End\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.NoProgressEnd
	),
	
	/** ProgressCheck concept true in an ending condition if a non-next player win. */
	NoProgressWin
	(
		"3.4.6.2",
		515, 
		"Win if no progress to an end.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.NoProgressEnd
	),
	
	/** Frequency of NoMovesWin. */
	NoProgressWinFrequency
	(
		"3.4.6.2.1",
		516, 
		"Frequency of \"No Progress Win\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.NoProgressWin
	),
	
	/** ProgressCheck concept true in an ending condition if a non-next player loss. */
	NoProgressLoss
	(
		"3.4.6.3",
		517, 
		"Loss if no progress to an end.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.NoProgressEnd
	),
	
	/** Frequency of NoMovesLoss. */
	NoProgressLossFrequency
	(
		"3.4.6.3.1",
		518, 
		"Frequency of \"No Progress Loss\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.NoProgressLoss
	),
	
	/** ProgressCheck concept true in an ending condition is a draw. */
	NoProgressDraw
	(
		"3.4.6.4",
		519, 
		"Draw if no progress to an end.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		false,
		Concept.NoProgressEnd
	),
	
	/** Frequency of NoMovesDraw. */
	NoProgressDrawFrequency
	(
		"3.4.6.4.1",
		520, 
		"Frequency of \"No Progress Draw\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.NoProgressDraw
	),
	
	/** A resultType Draw is used . */
	Draw
	(
		"3.4.7",
		521, 
		"The game can ends in a draw.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		false,
		Concept.End
	),
	
	/** Frequency of Draw. */
	DrawFrequency
	(
		"3.4.7.1",
		522, 
		"Frequency of \"Draw\".",
		ConceptType.Play, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction }, 
		true, 
		Concept.Draw
	),
	
	/** A misere end rule is detected . */
	Misere
	(
		"3.4.8",
		523, 
		"A two-players game can ends with the mover losing or the next player winning.",
		ConceptType.End, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, ConceptPurpose.Reconstruction },
		true,
		Concept.End
	),
	
	//-------------------------------------------------------------------------
    //                                 Behaviour
	//-------------------------------------------------------------------------
	
	/** */
	Behaviour
	(
		"4",
		524, 
		"Behaviour.",
		ConceptType.Behaviour,
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		null
	),
	
	/** */
	StateRepetition
	(
		"4.1",
		525, 
		"State repetition.",
		ConceptType.Behaviour,
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Behaviour
	),
	
	/** Computed with playouts. */
	PositionalRepetition
	(
		"4.1.1",
		526, 
		"Average number of repeated positional states.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.StateRepetition
	),
	
	/** Computed with playouts. */
	SituationalRepetition
	(
		"4.1.2",
		527, 
		"Average number of repeated situational states.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.StateRepetition
	),
	
	/** */
	Duration
	(
		"4.2",
		528, 
		"Game duration.",
		ConceptType.Behaviour,
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Behaviour
	),
	
	/** Computed with playouts. */
	DurationActions
	(
		"4.2.1",
		529, 
		"Number of actions in a game.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Duration
	),
	
	/** Computed with playouts. */
	DurationMoves
	(
		"4.2.2",
		530, 
		"Number of moves in a game.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Duration
	),
	
	/** Computed with playouts. */
	DurationTurns
	(
		"4.2.3",
		531, 
		"Number of turns in a game.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Duration
	),
	
	/** */
	Complexity
	(
		"4.3",
		532, 
		"Game complexity.",
		ConceptType.Behaviour,
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Behaviour
	),
	
	/** Computed with playouts. */
	DecisionMoves
	(
		"4.3.1",
		533, 
		"Percentage of moves where there was more than one possible move.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Complexity
	),
	
	/** Computed with playouts. */
	GameTreeComplexity
	(
		"4.3.2",
		534, 
		"Game Tree Complexity Estimate.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Complexity
	),
	
	/** Computed with playouts. */
	StateTreeComplexity
	(
		"4.3.3",
		535, 
		"State Space Complexity Upper Bound.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Complexity
	),
	
	/** */
	BoardCoverage
	(
		"4.4",
		536, 
		"Board Coverage.",
		ConceptType.Behaviour,
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Behaviour
	),
	
	/** Computed with playouts. */
	BoardCoverageDefault
	(
		"4.4.1",
		537, 
		"Percentage of default board sites which a piece was placed on at some point.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.BoardCoverage
	),
	
	/** Computed with playouts. */
	BoardCoverageFull
	(
		"4.4.2",
		538, 
		"Percentage of all board sites which a piece was placed on at some point.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.BoardCoverage
	),
	
	/** Computed with playouts. */
	BoardCoverageUsed
	(
		"4.4.3",
		539, 
		"Percentage of used board sites which a piece was placed on at some point.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.BoardCoverage
	),
	
	/** */
	GameOutcome
	(
		"4.5",
		540, 
		"Game Outcome.",
		ConceptType.Behaviour,
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Behaviour
	),
	
	/** Computed with playouts. */
	AdvantageP1
	(
		"4.5.1",
		541, 
		"Percentage of games where player 1 won.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.GameOutcome
	),
	
	/** Computed with playouts. */
	Balance
	(
		"4.5.2",
		542, 
		"Similarity between player win rates.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.GameOutcome
	),
	
	/** Computed with playouts. */
	Completion
	(
		"4.5.3",
		543, 
		"Percentage of games which have a winner (not drawor timeout).",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.GameOutcome
	),
	
	/** Computed with playouts. */
	Drawishness
	(
		"4.5.4",
		544, 
		"Percentage of games which end in a draw.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.GameOutcome
	),
	
	/** Computed with playouts. */
	Timeouts
	(
		"4.5.5",
		545, 
		"Percentage of games which end via timeout.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.GameOutcome
	),

	/** Computed with playouts. */
	OutcomeUniformity
	(
		"4.5.6",
		779, 
		"Inverse of average per-player variance in outcomes.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.GameOutcome
	),
	
	/** */
	StateEvaluation
	(
		"4.6",
		546, 
		"State Evaluation.",
		ConceptType.Behaviour,
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Behaviour
	),
	
	/** */
	Clarity
	(
		"4.6.1",
		547, 
		"Clarity.",
		ConceptType.Behaviour,
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.StateEvaluation
	),
	
	/** Computed with playouts. */
	Narrowness
	(
		"4.6.1.1",
		548, 
		"Narrowness.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Clarity
	),
	
	/** Computed with playouts. */
	Variance
	(
		"4.6.1.2",
		549, 
		"Variance.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Clarity
	),
	
	/** */
	Decisiveness
	(
		"4.6.2",
		550, 
		"Decisiveness.",
		ConceptType.Behaviour,
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.StateEvaluation
	),
	
	/** Computed with playouts. */
	DecisivenessMoves
	(
		"4.6.2.1",
		551, 
		"Decisiveness Moves.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Decisiveness
	),
	
	/** Computed with playouts. */
	DecisivenessThreshold
	(
		"4.6.2.2",
		552, 
		"Decisiveness Threshold.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Decisiveness
	),
	
	/** Computed with playouts. */
	LeadChange
	(
		"4.6.3",
		553, 
		"LeadChange.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.StateEvaluation
	),
	
	/** Computed with playouts. */
	Stability
	(
		"4.6.4",
		554, 
		"Stability.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.StateEvaluation
	),
	
	/** */
	Drama
	(
		"4.6.5",
		555, 
		"Drama.",
		ConceptType.Behaviour,
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.StateEvaluation
	),
	
	/** Computed with playouts. */
	DramaAverage
	(
		"4.6.5.1",
		556, 
		"Drama Average.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Drama
	),
	
	/** Computed with playouts. */
	DramaMedian
	(
		"4.6.5.2",
		557, 
		"Drama Median.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Drama
	),
	
	/** Computed with playouts. */
	DramaMaximum
	(
		"4.6.5.3",
		558, 
		"Drama Maximum.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Drama
	),
	
	/** Computed with playouts. */
	DramaMinimum
	(
		"4.6.5.4",
		559, 
		"Drama Minimum.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Drama
	),
	
	/** Computed with playouts. */
	DramaVariance
	(
		"4.6.5.5",
		560, 
		"Drama Variance.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Drama
	),
	
	/** Computed with playouts. */
	DramaChangeAverage
	(
		"4.6.5.6",
		561, 
		"Drama Change Average.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Drama
	),
	
	/** Computed with playouts. */
	DramaChangeSign
	(
		"4.6.5.7",
		562, 
		"Drama Change Sign.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Drama
	),
	
	/** Computed with playouts. */
	DramaChangeLineBestFit
	(
		"4.6.5.8",
		563, 
		"Drama Change Line Best Fit.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Drama
	),
	
	/** Computed with playouts. */
	DramaChangeNumTimes
	(
		"4.6.5.9",
		564, 
		"Drama Change Num Times.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Drama
	),
	
	/** Computed with playouts. */
	DramaMaxIncrease
	(
		"4.6.5.10",
		565, 
		"Drama Max Increase.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Drama
	),
	
	/** Computed with playouts. */
	DramaMaxDecrease
	(
		"4.6.5.11",
		566, 
		"Drama Max Decrease.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Drama
	),
	
	/** */
	MoveEvaluation
	(
		"4.6.6",
		567, 
		"Drama.",
		ConceptType.Behaviour,
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.StateEvaluation
	),
	
	/** Computed with playouts. */
	MoveEvaluationAverage
	(
		"4.6.6.1",
		568, 
		"Move Evaluation Average.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.MoveEvaluation
	),
	
	/** Computed with playouts. */
	MoveEvaluationMedian
	(
		"4.6.6.2",
		569, 
		"Move Evaluation Median.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.MoveEvaluation
	),
	
	/** Computed with playouts. */
	MoveEvaluationMaximum
	(
		"4.6.6.3",
		570, 
		"Move Evaluation Maximum.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.MoveEvaluation
	),
	
	/** Computed with playouts. */
	MoveEvaluationMinimum
	(
		"4.6.6.4",
		571, 
		"Move Evaluation Minimum.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.MoveEvaluation
	),
	
	/** Computed with playouts. */
	MoveEvaluationVariance
	(
		"4.6.6.5",
		572, 
		"Move Evaluation Variance.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.MoveEvaluation
	),
	
	/** Computed with playouts. */
	MoveEvaluationChangeAverage
	(
		"4.6.6.6",
		573, 
		"Move Evaluation Change Average.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.MoveEvaluation
	),
	
	/** Computed with playouts. */
	MoveEvaluationChangeSign
	(
		"4.6.6.7",
		574, 
		"Move Evaluation Change Sign.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.MoveEvaluation
	),
	
	/** Computed with playouts. */
	MoveEvaluationChangeLineBestFit
	(
		"4.6.6.8",
		575, 
		"Move Evaluation Change Line Best Fit.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.MoveEvaluation
	),
	
	/** Computed with playouts. */
	MoveEvaluationChangeNumTimes
	(
		"4.6.6.9",
		576, 
		"Move Evaluation Change Num Times.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.MoveEvaluation
	),
	
	/** Computed with playouts. */
	MoveEvaluationMaxIncrease
	(
		"4.6.6.10",
		577, 
		"Move Evaluation Max Increase.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.MoveEvaluation
	),
	
	/** Computed with playouts. */
	MoveEvaluationMaxDecrease
	(
		"4.6.6.11",
		578, 
		"Move Evaluation Max Decrease.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.MoveEvaluation
	),
	
	/** */
	StateEvaluationDifference
	(
		"4.6.7",
		579, 
		"Drama.",
		ConceptType.Behaviour,
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.StateEvaluation
	),
	
	/** Computed with playouts. */
	StateEvaluationDifferenceAverage
	(
		"4.6.7.1",
		580, 
		"State Evaluation Difference Average.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.StateEvaluationDifference
	),
	
	/** Computed with playouts. */
	StateEvaluationDifferenceMedian
	(
		"4.6.7.2",
		581, 
		"State Evaluation Difference Median.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.StateEvaluationDifference
	),
	
	/** Computed with playouts. */
	StateEvaluationDifferenceMaximum
	(
		"4.6.7.3",
		582, 
		"State Evaluation Difference Maximum.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.StateEvaluationDifference
	),
	
	/** Computed with playouts. */
	StateEvaluationDifferenceMinimum
	(
		"4.6.7.4",
		583, 
		"State Evaluation Difference Minimum.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.StateEvaluationDifference
	),
	
	/** Computed with playouts. */
	StateEvaluationDifferenceVariance
	(
		"4.6.7.5",
		584, 
		"State Evaluation Difference Variance.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.StateEvaluationDifference
	),
	
	/** Computed with playouts. */
	StateEvaluationDifferenceChangeAverage
	(
		"4.6.7.6",
		585, 
		"State Evaluation Difference Change Average.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.StateEvaluationDifference
	),
	
	/** Computed with playouts. */
	StateEvaluationDifferenceChangeSign
	(
		"4.6.7.7",
		586, 
		"State Evaluation Difference Change Sign.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.StateEvaluationDifference
	),
	
	/** Computed with playouts. */
	StateEvaluationDifferenceChangeLineBestFit
	(
		"4.6.7.8",
		587, 
		"State Evaluation Difference Change Line Best Fit.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.StateEvaluationDifference
	),
	
	/** Computed with playouts. */
	StateEvaluationDifferenceChangeNumTimes
	(
		"4.6.7.9",
		588, 
		"State Evaluation Difference Change Num Times.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.StateEvaluationDifference
	),
	
	/** Computed with playouts. */
	StateEvaluationDifferenceMaxIncrease
	(
		"4.6.7.10",
		589, 
		"State Evaluation Difference Max Increase.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.StateEvaluationDifference
	),
	
	/** Computed with playouts. */
	StateEvaluationDifferenceMaxDecrease
	(
		"4.6.7.11",
		590, 
		"State Evaluation Difference Max Decrease.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.StateEvaluationDifference
	),
	
	/** */
	BoardSitesOccupied
	(
		"4.7",
		591, 
		"Board sites occupied.",
		ConceptType.Behaviour,
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Behaviour
	),
	
	/** Computed with playouts. */
	BoardSitesOccupiedAverage
	(
		"4.7.1",
		592, 
		"Average percentage of board sites which have a piece on it in any given turn.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.BoardSitesOccupied
	),
	
	/** Computed with playouts. */
	BoardSitesOccupiedMedian
	(
		"4.7.2",
		593, 
		"Median percentage of board sites which have a piece on it in any given turn.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.BoardSitesOccupied
	),
	
	/** Computed with playouts. */
	BoardSitesOccupiedMaximum
	(
		"4.7.3",
		594, 
		"Maximum percentage of board sites which have a piece on it in any given turn.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.BoardSitesOccupied
	),
	
	/** Computed with playouts. */
	BoardSitesOccupiedMinimum
	(
		"4.7.4",
		595, 
		"Minimum percentage of board sites which have a piece on it in any given turn.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.BoardSitesOccupied
	),
	
	/** Computed with playouts. */
	BoardSitesOccupiedVariance
	(
		"4.7.5",
		596, 
		"Variance in percentage of board sites which have a piece on it in any given turn.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.BoardSitesOccupied
	),
	
	/** Computed with playouts. */
	BoardSitesOccupiedChangeAverage
	(
		"4.7.6",
		597, 
		"Change in percentage of board sites which have a piece on it in any given turn.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.BoardSitesOccupied
	),
	
	/** Computed with playouts. */
	BoardSitesOccupiedChangeSign
	(
		"4.7.7",
		598, 
		"Sign Change of board sites which have a piece on it in any given turn.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.BoardSitesOccupied
	),
	
	/** Computed with playouts. */
	BoardSitesOccupiedChangeLineBestFit
	(
		"4.7.8",
		599, 
		"Line Best Fit Change of board sites which have a piece on it in any given turn.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.BoardSitesOccupied
	),
	
	/** Computed with playouts. */
	BoardSitesOccupiedChangeNumTimes
	(
		"4.7.9",
		600, 
		"Number of times the change of board sites which have a piece on it in any given turn.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.BoardSitesOccupied
	),
	
	/** Computed with playouts. */
	BoardSitesOccupiedMaxIncrease
	(
		"4.7.10",
		601, 
		"Max Increase of board sites which have a piece on it in any given turn.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.BoardSitesOccupied
	),
	
	/** Computed with playouts. */
	BoardSitesOccupiedMaxDecrease
	(
		"4.7.11",
		602, 
		"Max Decrease of board sites which have a piece on it in any given turn.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.BoardSitesOccupied
	),
	
	/** */
	BranchingFactor
	(
		"4.8",
		603, 
		"Branching factor.",
		ConceptType.Behaviour,
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Behaviour
	),
	
	/** Computed with playouts. */
	BranchingFactorAverage
	(
		"4.8.1",
		604, 
		"Average number of possible moves.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.BranchingFactor
	),
	
	/** Computed with playouts. */
	BranchingFactorMedian
	(
		"4.8.2",
		605, 
		"Median number of possible moves.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.BranchingFactor
	),
	
	/** Computed with playouts. */
	BranchingFactorMaximum
	(
		"4.8.3",
		606, 
		"Maximum number of possible moves.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.BranchingFactor
	),
	
	/** Computed with playouts. */
	BranchingFactorMinimum
	(
		"4.8.4",
		607, 
		"Minimum number of possible moves.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.BranchingFactor
	),
	
	/** Computed with playouts. */
	BranchingFactorVariance
	(
		"4.8.5",
		608, 
		"Variance in number of possible moves.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.BranchingFactor
	),
	
	/** Computed with playouts. */
	BranchingFactorChangeAverage
	(
		"4.8.6",
		609, 
		"Change in percentage of possible moves.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.BranchingFactor
	),
	
	/** Computed with playouts. */
	BranchingFactorChangeSign
	(
		"4.8.7",
		610, 
		"Change sign of possible moves.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.BranchingFactor
	),
	
	/** Computed with playouts. */
	BranchingFactorChangeLineBestFit
	(
		"4.8.8",
		611, 
		"Change line best fit of possible moves.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.BranchingFactor
	),
	
	/** Computed with playouts. */
	BranchingFactorChangeNumTimesn
	(
		"4.8.9",
		612, 
		"Change num times of possible moves.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.BranchingFactor
	),
	
	/** Computed with playouts. */
	BranchingFactorChangeMaxIncrease
	(
		"4.8.10",
		613, 
		"Change max increase of possible moves.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.BranchingFactor
	),
	
	/** Computed with playouts. */
	BranchingFactorChangeMaxDecrease
	(
		"4.8.11",
		614, 
		"Change max decrease of possible moves.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.BranchingFactor
	),

	/** */
	DecisionFactor
	(
		"4.9",
		615, 
		"Decision factor.",
		ConceptType.Behaviour,
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Behaviour
	),
	
	/** Computed with playouts. */
	DecisionFactorAverage
	(
		"4.9.1",
		616, 
		"Average number of possible moves when the number of possible moves is greater than 1.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.DecisionFactor
	),
	
	/** Computed with playouts. */
	DecisionFactorMedian
	(
		"4.9.2",
		617, 
		"Median number of possible moves when the number of possible moves is greater than 1.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.DecisionFactor
	),
	
	/** Computed with playouts. */
	DecisionFactorMaximum
	(
		"4.9.3",
		618, 
		"Maximum number of possible moves when the number of possible moves is greater than 1.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.DecisionFactor
	),
	
	/** Computed with playouts. */
	DecisionFactorMinimum
	(
		"4.9.4",
		619, 
		"Minimum number of possible moves when the number of possible moves is greater than 1.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.DecisionFactor
	),
	
	/** Computed with playouts. */
	DecisionFactorVariance
	(
		"4.9.5",
		620, 
		"Variance in number of possible moves when the number of possible moves is greater than 1.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.DecisionFactor
	),
	
	/** Computed with playouts. */
	DecisionFactorChangeAverage
	(
		"4.9.6",
		621, 
		"Change in percentage of possible moves when the number of possible moves is greater than 1.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.DecisionFactor
	),
	
	/** Computed with playouts. */
	DecisionFactorChangeSign
	(
		"4.9.7",
		622, 
		"Change sign of possible moves when the number of possible moves is greater than 1.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.DecisionFactor
	),
	
	/** Computed with playouts. */
	DecisionFactorChangeLineBestFit
	(
		"4.9.8",
		623, 
		"Change line best fit of possible moves when the number of possible moves is greater than 1.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.DecisionFactor
	),
	
	/** Computed with playouts. */
	DecisionFactorChangeNumTimes
	(
		"4.9.9",
		624, 
		"Change num times of possible moves when the number of possible moves is greater than 1.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.DecisionFactor
	),
	
	/** Computed with playouts. */
	DecisionFactorMaxIncrease
	(
		"4.9.10",
		625, 
		"Max increase of possible moves when the number of possible moves is greater than 1.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.DecisionFactor
	),
	
	/** Computed with playouts. */
	DecisionFactorMaxDecrease
	(
		"4.9.11",
		626, 
		"Max Decrease of possible moves when the number of possible moves is greater than 1.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.DecisionFactor
	),

	/** */
	MoveDistance
	(
		"4.10",
		627, 
		"Move distance.",
		ConceptType.Behaviour,
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Behaviour
	),
	
	/** Computed with playouts. */
	MoveDistanceAverage
	(
		"4.10.1",
		628, 
		"Average distance traveled by pieces when they move around the board.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.MoveDistance
	),
	
	/** Computed with playouts. */
	MoveDistanceMedian
	(
		"4.10.2",
		629, 
		"Median distance traveled by pieces when they move around the board.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.MoveDistance
	),
	
	/** Computed with playouts. */
	MoveDistanceMaximum
	(
		"4.10.3",
		630, 
		"Maximum distance traveled by pieces when they move around the board.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.MoveDistance
	),
	
	/** Computed with playouts. */
	MoveDistanceMinimum
	(
		"4.10.4",
		631, 
		"Minimum distance traveled by pieces when they move around the board.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.MoveDistance
	),
	
	/** Computed with playouts. */
	MoveDistanceVariance
	(
		"4.10.5",
		632, 
		"Variance in distance traveled by pieces when they move around the board.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.MoveDistance
	),
	
	/** Computed with playouts. */
	MoveDistanceChangeAverage
	(
		"4.10.6",
		633, 
		"Change average in distance traveled by pieces when they move around the board.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.MoveDistance
	),
	
	/** Computed with playouts. */
	MoveDistanceChangeSign
	(
		"4.10.7",
		634, 
		"Change sign in distance traveled by pieces when they move around the board.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.MoveDistance
	),
	
	/** Computed with playouts. */
	MoveDistanceChangeLineBestFit
	(
		"4.10.8",
		635, 
		"Change line best fit in distance traveled by pieces when they move around the board.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.MoveDistance
	),
	
	/** Computed with playouts. */
	MoveDistanceChangeNumTimes
	(
		"4.10.9",
		636, 
		"Change num times in distance traveled by pieces when they move around the board.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.MoveDistance
	),
	
	/** Computed with playouts. */
	MoveDistanceMaxIncrease
	(
		"4.10.10",
		637, 
		"Max increase in distance traveled by pieces when they move around the board.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.MoveDistance
	),
	
	/** Computed with playouts. */
	MoveDistanceMaxDecrease
	(
		"4.10.11",
		638, 
		"Max decrease in distance traveled by pieces when they move around the board.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.MoveDistance
	),
	
	/** */
	PieceNumber
	(
		"4.11",
		639, 
		"Piece number.",
		ConceptType.Behaviour,
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Behaviour
	),
	
	/** Computed with playouts. */
	PieceNumberAverage
	(
		"4.11.1",
		640, 
		"Average number of pieces on the board.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.PieceNumber
	),
	
	/** Computed with playouts. */
	PieceNumberMedian
	(
		"4.11.2",
		641, 
		"Median number of pieces on the board.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.PieceNumber
	),
	
	/** Computed with playouts. */
	PieceNumberMaximum
	(
		"4.11.3",
		642, 
		"Maximum number of pieces on the board.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.PieceNumber
	),
	
	/** Computed with playouts. */
	PieceNumberMinimum
	(
		"4.11.4",
		643, 
		"Minimum number of pieces on the board.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.PieceNumber
	),
	
	/** Computed with playouts. */
	PieceNumberVariance
	(
		"4.11.5",
		644, 
		"Variance in number of pieces on the board.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.PieceNumber
	),
	
	/** Computed with playouts. */
	PieceNumberChangeAverage
	(
		"4.11.6",
		645, 
		"Change in percentage of pieces on the board.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.PieceNumber
	),
	
	/** Computed with playouts. */
	PieceNumberChangeSign
	(
		"4.11.7",
		646, 
		"Change in sign of pieces on the board.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.PieceNumber
	),
	
	/** Computed with playouts. */
	PieceNumberChangeLineBestFit
	(
		"4.11.8",
		647, 
		"Change line best fit of pieces on the board.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.PieceNumber
	),
	
	/** Computed with playouts. */
	PieceNumberChangeNumTimes
	(
		"4.11.9",
		648, 
		"Change in number of pieces on the board.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.PieceNumber
	),
	
	/** Computed with playouts. */
	PieceNumberMaxIncrease
	(
		"4.11.10",
		649, 
		"Max increase of pieces on the board.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.PieceNumber
	),
	
	/** Computed with playouts. */
	PieceNumberMaxDecrease
	(
		"4.11.11",
		650, 
		"Max decrease of pieces on the board.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.PieceNumber
	),
	
	/** */
	ScoreDifference
	(
		"4.12",
		651, 
		"Score Difference.",
		ConceptType.Behaviour,
		ConceptDataType.BooleanData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Behaviour
	),
	
	/** Computed with playouts. */
	ScoreDifferenceAverage
	(
		"4.12.1",
		652, 
		"Average difference in player scores.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.ScoreDifference
	),
	
	/** Computed with playouts. */
	ScoreDifferenceMedian
	(
		"4.12.2",
		653, 
		"Median difference in player scores.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.ScoreDifference
	),
	
	/** Computed with playouts. */
	ScoreDifferenceMaximum
	(
		"4.12.3",
		654, 
		"Maximum difference in player scores.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.ScoreDifference
	),
	
	/** Computed with playouts. */
	ScoreDifferenceMinimum
	(
		"4.12.4",
		655, 
		"Minimum difference in player scores.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.ScoreDifference
	),
	
	/** Computed with playouts. */
	ScoreDifferenceVariance
	(
		"4.12.5",
		656, 
		"Variance in difference in player scores.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.ScoreDifference
	),
	
	/** Computed with playouts. */
	ScoreDifferenceChangeAverage
	(
		"4.12.6",
		657, 
		"Change average in difference in player scores.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.ScoreDifference
	),
	
	/** Computed with playouts. */
	ScoreDifferenceChangeSign
	(
		"4.12.7",
		658, 
		"Change sign in difference in player scores.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.ScoreDifference
	),
	
	/** Computed with playouts. */
	ScoreDifferenceChangeLineBestFit
	(
		"4.12.8",
		659, 
		"Change line best fit in difference in player scores.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.ScoreDifference
	),
	
	/** Computed with playouts. */
	ScoreDifferenceChangeNumTimes
	(
		"4.12.9",
		660, 
		"Change number times in difference in player scores.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.ScoreDifference
	),
	
	/** Computed with playouts. */
	ScoreDifferenceMaxIncrease
	(
		"4.12.10",
		661, 
		"Max increase in difference in player scores.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.ScoreDifference
	),
	
	/** Computed with playouts. */
	ScoreDifferenceMaxDecrease
	(
		"4.12.11",
		662, 
		"Max decrease in difference in player scores.",
		ConceptType.Behaviour,
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.ScoreDifference
	),
	
	//-------------------------------------------------------------------------
    //                                 Math
	//-------------------------------------------------------------------------
	
	/** */
	Math
	(
		"5",
		663, 
		"Mathematics.",
		ConceptType.Math,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		null
	),
	
	/** */
	Arithmetic
	(
		"5.1",
		664, 
		"Arithmetic.",
		ConceptType.Math,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Math
	),
	
	/** */
	Operations
	(
		"5.1.1",
		665, 
		"Operations.",
		ConceptType.Math,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Arithmetic
	),
	
	/** (+ ...) is used. */
	Addition
	(
		"5.1.1.1",
		666, 
		"Addition operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (- ...) is used. */
	Subtraction
	(
		"5.1.1.2",
		667, 
		"Subtraction operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (* ...) is used.. */
	Multiplication
	(
		"5.1.1.3",
		668, 
		"Multiplication operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (/ ...) is used. */
	Division
	(
		"5.1.1.4",
		669, 
		"Division operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (% ...) is used. */
	Modulo
	(
		"5.1.1.5",
		670, 
		"Modulo operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (abs ...) is used. */
	Absolute
	(
		"5.1.1.6",
		671, 
		"Absolute operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (sqrt ...) used.. */
	Roots
	(
		"5.1.1.7",
		672, 
		"Root operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (cos ...) is used. */
	Cosine
	(
		"5.1.1.8",
		673, 
		"Cosine operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (sin ...) is used. */
	Sine
	(
		"5.1.1.9",
		674, 
		"Sine operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (tan ...) is used. */
	Tangent
	(
		"5.1.1.10",
		675, 
		"Tangent operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (^ ...) is used. */
	Exponentiation
	(
		"5.1.1.11",
		676, 
		"Exponentiation operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (exp ...) is used. */
	Exponential
	(
		"5.1.1.12",
		677, 
		"Exponential operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (log ...) or (log10 ...) is used. */
	Logarithm
	(
		"5.1.1.13",
		678, 
		"Logarithm operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (min ...) is used. */
	Minimum
	(
		"5.1.1.14",
		679, 
		"Minimum value.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** (max ...) is used. */
	Maximum
	(
		"5.1.1.15",
		680, 
		"Maximum value.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Operations
	),
	
	/** */
	Comparison
	(
		"5.1.2",
		681, 
		"Comparison of numbers.",
		ConceptType.Math,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Arithmetic
	),
	
	/** = operator. */
	Equal
	(
		"5.1.2.1",
		682, 
		"= operator.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Comparison
	),
	
	/** != operator. */
	NotEqual
	(
		"5.1.2.2",
		683, 
		"!= operator.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Comparison
	),
	
	/** < operator. */
	LesserThan
	(
		"5.1.2.3",
		684, 
		"< operator.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI },
		true,
		Concept.Comparison
	),
	
	/** <= operator. */
	LesserThanOrEqual
	(
		"5.1.2.4",
		685, 
		"<= operator.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Comparison
	),
	
	/** > operator. */
	GreaterThan
	(
		"5.1.2.5",
		686, 
		"> operator.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Comparison
	),
	
	/** >= operator. */
	GreaterThanOrEqual
	(
		"5.1.2.6",
		687, 
		">= operator.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Comparison
	),
	
	/** */
	Parity
	(
		"5.1.3",
		688, 
		"Whether a number is even or odd.",
		ConceptType.Math,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Arithmetic
	),
	
	/** Even values. */
	Even
	(
		"5.1.3.1",
		689, 
		"Even values.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Parity
	),
	
	/** Odd values. */
	Odd
	(
		"5.1.3.2",
		690, 
		"Odd values.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Parity
	),
	
	/** */
	Logic
	(
		"5.2",
		691, 
		"Logic operations.",
		ConceptType.Math,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Math
	),	
	
	/** (and ...). */
	Conjunction
	(
		"5.2.1",
		692, 
		"Conjunction (And).",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Logic
	),
	
	/**(or ...). */
	Disjunction
	(
		"5.2.2",
		693, 
		"Disjunction (Or).",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Logic
	),
	
	/** (xor ...). */
	ExclusiveDisjunction
	(
		"5.2.3",
		694, 
		"Exclusive Disjunction (Xor).",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Logic
	),
	
	/** (not ...). */
	Negation
	(
		"5.2.4",
		695, 
		"Negation (Not).",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Logic
	),
	
	/** */
	Set
	(
		"5.3",
		696, 
		"Set operations.",
		ConceptType.Math,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Math
	),	
	
	/** (union ...). */
	Union
	(
		"5.3.1",
		697, 
		"Union operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Set
	),
	
	/** (intersection ...). */
	Intersection
	(
		"5.3.2",
		698, 
		"Intersection operation.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Set
	),
	
	/** (difference ...). */
	Complement
	(
		"5.3.3",
		699, 
		"Complement operation (Difference).",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Set
	),
	
	/** */
	Algorithmics
	(
		"5.4",
		700, 
		"Algorithmic operations.",
		ConceptType.Math,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		false,
		Concept.Math
	),
	
	
	/** (if ...) is used. */
	ConditionalStatement
	(
		"5.4.1",
		701, 
		"Conditional Statement (If).",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Algorithmics
	),
	
	/** (for ...) is used. */
	ControlFlowStatement
	(
		"5.4.2",
		702, 
		"Control Flow Statement (For).",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Algorithmics
	),
	
	/** Float values. */
	Float
	(
		"5.5",
		703, 
		"Float values.",
		ConceptType.Math, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.Reconstruction, ConceptPurpose.AI }, 
		true,
		Concept.Math
	),
	
	//-------------------------------------------------------------------------
    //                                 Visual
	//-------------------------------------------------------------------------
	
	/** */
	Visual
	(
		"6",
		704, 
		"Important visual aspects.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		false,
		null
	),
	
	/** */
	Style
	(
		"6.1",
		705, 
		"Style of game elements.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[]{ }, 
		false,
		Concept.Visual
	),
	
	/** */
	BoardStyle
	(
		"6.1.1",
		706, 
		"Style of the board.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		false,
		Concept.Style
	),
	
	/** Use Graph style. */
	GraphStyle
	(
		"6.1.1.1",
		707,
		"Use Graph style.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.BoardStyle
	),
	
	/** Use Chess style. */
	ChessStyle
	(
		"6.1.1.2",
		708,
		"Use Chess style.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.BoardStyle
	),
	
	/** Use Go style.*/
	GoStyle
	(
		"6.1.1.3",
		709,
		"Use Go style.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[]{ }, 
		true,
		Concept.BoardStyle
	),
	
	/** Use Mancala style.*/
	MancalaStyle
	(
		"6.1.1.4",
		710,
		"Use Mancala style.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { },
		true,
		Concept.BoardStyle
	),
	
	/** Use PenAndPaper style.*/
	PenAndPaperStyle
	(
		"6.1.1.5",
		711,
		"Use PenAndPaper style.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.BoardStyle
	),
	
	/** Use Shibumi style.*/
	ShibumiStyle
	(
		"6.1.1.6",
		712,
		"Use Shibumi style.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.BoardStyle
	),
	
	/** Use Backgammon style.*/
	BackgammonStyle
	(
		"6.1.1.7",
		713,
		"Use Backgammon style.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.BoardStyle
	),
	
	/** Use Janggi style. */
	JanggiStyle
	(
		"6.1.1.8",
		714,
		"Use Janggi style.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.BoardStyle
	),
	
	/** Use Xiangqi style. */
	XiangqiStyle
	(
		"6.1.1.9",
		715,
		"Use Xiangqi style.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.BoardStyle
	),
	
	/** Use Shogi style. */
	ShogiStyle(
		"6.1.1.10",
		716,
		"Use Shogi style.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.BoardStyle
	),
	
	/** Use Table style. */
	TableStyle(
		"6.1.1.11",
		717,
		"Use Table style.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.BoardStyle
	),
	
	/** Use Surakarta style. */
	SurakartaStyle
	(
		"6.1.1.12",
		718,
		"Use Surakarta style.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.BoardStyle
	),
	
	/** Use Tafl style. */
	TaflStyle
	(
		"6.1.1.13",
		719,
		"Use Tafl style.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.BoardStyle
	),
	
	/** Board is not shown. */
	NoBoard
	(
		"6.1.1.14",
		720, 
		"Board is not shown.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.BoardStyle
	),
	
	/** */
	ComponentStyle
	(
		"6.1.2",
		721, 
		"Style of the component.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		false,
		Concept.Style
	),
	
	/** Use animal components. */
	AnimalComponent
	(
		"6.1.2.1",
		722, 
		"Use animal components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.ComponentStyle
	),
	
	/** Use Chess components. */
	ChessComponent
	(
		"6.1.2.2",
		723, 
		"Use Chess components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		false,
		Concept.ComponentStyle
	),
	
	/** Use King components. */
	KingComponent
	(
		"6.1.2.2.1",
		724, 
		"Use Chess components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.ChessComponent
	),
	
	/** Use Queen components. */
	QueenComponent
	(
		"6.1.2.2.2",
		725, 
		"Use Queen components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.ChessComponent
	),
	
	/** Use Knight components. */
	KnightComponent
	(
		"6.1.2.2.3",
		726, 
		"Use Knight components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.ChessComponent
	),
	
	/** Use Rook components. */
	RookComponent
	(
		"6.1.2.2.4",
		727, 
		"Use Rook components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.ChessComponent
	),
	
	/** Use Bishop components. */
	BishopComponent
	(
		"6.1.2.2.5",
		728, 
		"Use Bishop components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.ChessComponent
	),
	
	/** Use Pawn components. */
	PawnComponent
	(
		"6.1.2.2.6",
		729, 
		"Use Pawn components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.ChessComponent
	),
	
	/** Use fairy Chess components. */
	FairyChessComponent
	(
		"6.1.2.3",
		730, 
		"Use fairy Chess components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.ComponentStyle
	),
	
	/** Use Ploy components. */
	PloyComponent
	(
		"6.1.2.4",
		731, 
		"Use Ploy components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.ComponentStyle
	),
	
	/** Use Shogi components. */
	ShogiComponent
	(
		"6.1.2.5",
		732, 
		"Use Shogi components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.ComponentStyle
	),
	
	/** Use Xiangqi components. */
	XiangqiComponent
	(
		"6.1.2.6",
		733, 
		"Use Xiangqi components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.ComponentStyle
	),
	
	/** Use Stratego components. */
	StrategoComponent
	(
		"6.1.2.7",
		734, 
		"Use Stratego components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.ComponentStyle
	),
	
	/** Use Janggi components. */
	JanggiComponent
	(
		"6.1.2.8",
		735, 
		"Use Janggi components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.ComponentStyle
	),
	
	/** Use Hand components. */
	HandComponent
	(
		"6.1.2.9",
		736, 
		"Use Hand components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.ComponentStyle
	),
	
	/** Use Checkers components. */
	CheckersComponent
	(
		"6.1.2.10",
		737, 
		"Use Checkers components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.ComponentStyle
	),
	
	/** Use Ball components. */
	BallComponent
	(
		"6.1.2.11",
		738, 
		"Use Ball components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.ComponentStyle
	),
	
	/** Use Tafl components. */
	TaflComponent
	(
		"6.1.2.12",
		739, 
		"Use Tafl components.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.ComponentStyle
	),
	
	/** Use Disc components. */
	DiscComponent
	(
		"6.1.2.13",
		740, 
		"Use Disc components.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.ComponentStyle
	),
	
	/** Use Marker components. */
	MarkerComponent
	(
		"6.1.2.14",
		741, 
		"Use Marker components.",
		ConceptType.Visual,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.ComponentStyle
	),
	
	/**
	 * Visual of a stack is modified.
	 */
	StackType
	(
		"6.2",
		742,
		"Visual of a stack.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] {}, 
		false,
		Concept.Visual
	),
	
	/**
	 * Use stacks.
	 */
	Stack
	(
		"6.2.1",
		743,
		"Stacks of pieces.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI },
		true,
		Concept.StackType
	),
	
	/** Use Symbols. */
	Symbols
	(
		"6.3",
		744, 
		"Symbols on the board.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.Visual
	),
	
	
	/** Show piece value. */
	ShowPieceValue
	(
		"6.4",
		745, 
		"Show piece values.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { }, 
		true,
		Concept.Visual
	),
	
	/** Show piece state. */
	ShowPieceState
	(
		"6.5",
		746, 
		"Show piece states.",
		ConceptType.Visual, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { },
		true,
		Concept.Visual
	),
	
	
	//-------------------------------------------------------------------------
    //                                 Implementation
	//-------------------------------------------------------------------------

	/** */
	Implementation
	(
		"7",
		747, 
		"Internal implementation details, e.g. for performance predictions.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		false,
		null
	),
	
	/** */
	State
	(
		"7.1",
		748, 
		"State related implementation.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		false,
		Concept.Implementation
	),
	
	/** */
	StateType
	(
		"7.1.1",
		749, 
		"Type of state used.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		false,
		Concept.State
	),
	
	/** Use stack state. */
	StackState
	(
		"7.1.1.1",
		750, 
		"Use stack state.",
		ConceptType.Implementation, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI, }, 
		true,
		Concept.StateType
	),
	
	/** */
	PieceState
	(
		"7.1.2",
		751, 
		"State related information about piece.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		false,
		Concept.State
	),
	
	/** Use site state. */
	SiteState
	(
		"7.1.2.1",
		752, 
		"Use site state.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.PieceState
	),
	
	/** Use (set State ...). */
	SetSiteState
	(
		"7.1.2.2",
		753, 
		"Set the site state.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.PieceState
	),
	
	
	/** Store visited sites in previous moves of a turn. */
	VisitedSites
	(
		"7.1.2.3",
		754, 
		"Store visited sites in previous moves of a turn.",
		ConceptType.Implementation, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.PieceState
	),
	
	/** Use state variable(s). */
	Variable
	(
		"7.1.3",
		755,
		"Use state variable(s).",
		ConceptType.Implementation, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		false,
		Concept.State
	),
	
	/** (set Var ...). */
	SetVar
	(
		"7.1.3.1",
		756,
		"The variable 'var' is set.",
		ConceptType.Implementation, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.Variable
	),
	
	/** (remember ...). */
	RememberValues
	(
		"7.1.3.2",
		757,
		"Some values are remembered.",
		ConceptType.Implementation, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.Variable
	),
	
	/** (remember ...). */
	ForgetValues
	(
		"7.1.3.3",
		758,
		"Some values are forgotten.",
		ConceptType.Implementation, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.Variable
	),
	
	/** (set Pending ...). */
	SetPending
	(
		"7.1.3.4",
		759,
		"The variable pending is set.",
		ConceptType.Implementation, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.Variable
	),
	
	/** Use internal counter of the state. */
	InternalCounter
	(
		"7.1.4",
		760, 
		"Use internal counter of the state.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		false,
		Concept.State
	),
	
	/** Set internal counter of the state. */
	SetInternalCounter
	(
		"7.1.4.1",
		761, 
		"Set internal counter of the state.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.InternalCounter
	),
	
	/** Use player value. */
	PlayerValue
	(
		"7.1.5",
		762, 
		"Use player value.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.State
	),
	
	/** */
	SetHidden
	(
		"7.1.6",
		763, 
		"Hidden information are set.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		false,
		Concept.State
	),
	
	/** (set Hidden ...) is used. */
	SetInvisible
	(
		"7.1.6.1",
		764, 
		"Invisibility is set.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.SetHidden
	),
	
	/** (set Hidden Count ...) is used. */
	SetHiddenCount
	(
		"7.1.6.2",
		765, 
		"Hidden count is set.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.SetHidden
	),
	
	/** (set Hidden Rotation ...) is used. */
	SetHiddenRotation
	(
		"7.1.6.3",
		766, 
		"Hidden rotation is set.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.SetHidden
	),
	
	/** (set Hidden State ...) is used. */
	SetHiddenState
	(
		"7.1.6.4",
		767, 
		"Hidden state is set.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.SetHidden
	),
	
	/** (set Hidden Value ...) is used. */
	SetHiddenValue
	(
		"7.1.6.5",
		768, 
		"Hidden value is set.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.SetHidden
	),
	
	/** (set Hidden What ...) is used. */
	SetHiddenWhat
	(
		"7.1.6.6",
		769, 
		"Hidden count are set.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.SetHidden
	),
	
	/** (set Hidden Who ...) is used. */
	SetHiddenWho
	(
		"7.1.6.7",
		770, 
		"Hidden who is set.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData, 
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.SetHidden
	),
	
	/** */
	Efficiency
	(
		"7.2",
		771, 
		"Implementation related to efficiency (run on Intel E7-8860, 2.2 GHz, 4GB RAM, Seed = 2077).",
		ConceptType.Implementation,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		false,
		Concept.Implementation
	),
	
	
	/** The context can be copied during computation of the moves. */
	CopyContext
	(
		"7.2.1",
		772, 
		"The context can be copied during computation of the moves.",
		ConceptType.Implementation,
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.Efficiency
	),
	
	/** Use consequences moves (then). */
	Then
	(
		"7.2.2",
		773, 
		"Use consequences moves (then).",
		ConceptType.Implementation, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI }, 
		true,
		Concept.Efficiency
	),
	

	/** Describes moves per piece. */
	ForEachPiece
	(
		"7.2.3",
		774,
		"Describes moves per piece.",
		ConceptType.Implementation, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[]{ ConceptPurpose.AI },  
		true,
		Concept.Efficiency
	),
	
	/** Use a (do ...) ludeme. */
	DoLudeme
	(
		"7.2.4",
		775, 
		"Use a (do ...) ludeme.",
		ConceptType.Implementation, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI },  
		true,
		Concept.Efficiency
	),
	
	/** Use a (trigger ...) ludeme. */
	Trigger
	(
		"7.2.5",
		776, 
		"Use a (trigger ...) ludeme.",
		ConceptType.Implementation, 
		ConceptDataType.BooleanData,
		ConceptComputationType.Compilation,
		new ConceptPurpose[] { ConceptPurpose.AI },  
		true,
		Concept.Efficiency
	),
	
	/** Computed with playouts. */
	PlayoutsPerSecond
	(
		"7.2.6",
		777, 
		"Number of playouts computed per second.",
		ConceptType.Implementation, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI },  
		true,
		Concept.Efficiency
	),
	
	/** Computed with playouts. */
	MovesPerSecond
	(
		"7.2.7",
		778, 
		"Number of moves computed per second.",
		ConceptType.Implementation, 
		ConceptDataType.DoubleData,
		ConceptComputationType.Playout,
		new ConceptPurpose[] { ConceptPurpose.AI },  
		true,
		Concept.Efficiency
	),
	
	;
	
	
	//-------------------------------------------------------------------------
	
	/** The type of the game concepts. */
	final ConceptType type;
	
	/** The data type of the game concepts. */
	final ConceptDataType dataType;
	
	/** The computation type of the game concepts. */
	final ConceptComputationType computationType;

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

	//-------------------------------------------------------------------------

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
		final ConceptComputationType computationType,
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
		this.computationType = computationType;
		this.leaf = leaf;
		this.id = id;
		this.parent = parent;
	}

	//-------------------------------------------------------------------------

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
		return id; //this.ordinal() + 1;
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
	 * @return The computation type of the concept.
	 */
	public ConceptComputationType computationType()
	{
		return computationType;
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
	
	/**
	 * @return The concepts used by the portfolio (which are all except implementation ones, behavior ones and visual ones).
	 */
	public static Concept[] portfolioConcepts()
	{
		final List<Concept> portfolioConcepts = new ArrayList<Concept>();
		for(Concept concept : Concept.values())
			if(!concept.type().equals(ConceptType.Implementation) && !concept.type().equals(ConceptType.Behaviour) && !concept.type().equals(ConceptType.Visual))
					portfolioConcepts.add(concept);		
		
		final Concept[] returnConcepts = new Concept[portfolioConcepts.size()];
		for(int i = 0; i < returnConcepts.length; i++)
			returnConcepts[i] = portfolioConcepts.get(i);
		
		return returnConcepts;
	}
}
