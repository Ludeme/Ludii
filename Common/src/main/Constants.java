package main;

/**
 * Global constants.
 * 
 * @author cambolbro and Eric.Piette
 */
public final class Constants
{
	//-------------------------------------------------------------------------
	// Admin
	
	/** Version number of the Ludii's grammar/ludeme-based language. */
	public static final String LUDEME_VERSION = "1.3.12";

	/** Date last modified. */
	public static final String DATE = "01/08/2023";
	
	/** lud-path for default game to load (on initial launch, when prefs/trial loading fails, etc.) */
	public static final String DEFAULT_GAME_PATH = "/lud/board/war/replacement/eliminate/all/Surakarta.lud";

	//-------------------------------------------------------------------------
	// Limits
	
	/** Maximum score. */
	public static final int MAX_SCORE = 1000000000;

	/** Minimum score. *
	public static final int MIN_SCORE = -1000000000;

	/** The maximum number of iterations allowed in a loop inside an eval method. */
	public static final int MAX_NUM_ITERATION = 10000;

	/** Maximum number of players. */
	public static final int MAX_PLAYERS = 16;

	/** Maximum number of consecutive player turns (for purposes of hash codes, not a strict limit). */
	public static final int MAX_CONSECUTIVES_TURNS = 16;

	/** Highest possible team id. */
	public static final int MAX_PLAYER_TEAM = MAX_PLAYERS;

	/** Default value for components, if not specified. */
	public static final int DEFAULT_PIECE_VALUE = 1;
	
	/** Highest possible number of rules phases in a game. */
	public static final int MAX_PHASES = 16;

	/** Maximum distance a piece can travel. */
	public static final int MAX_DISTANCE = 1000;

	/** The max number of pits on a dominoes. */
	public static final int MAX_PITS_DOMINOES = 16;

	/** The max number of sides of a tile component. */
	public static final int MAX_SIDES_TILE = 16;

	/** The max number of faces for a die. */
	public static final int MAX_FACE_DIE = 100;

	/** The maximum value of a piece. */
	public static final int MAX_VALUE_PIECE = 1024;

	/** Minimum image size. */
	public static final int MIN_IMAGE_SIZE = 2;
	
	/** Default limit on number of turns (to be multiplied by number of players) per trial. */
	public static final int DEFAULT_TURN_LIMIT = 1250;
	
	/** Default limit on number of moves per trial. */
	public static final int DEFAULT_MOVES_LIMIT = 10000;

	/** Max Stack Height. */
	public static final int MAX_STACK_HEIGHT = 32;

	/** Maximum number of cell colours. */
	public static final int MAX_CELL_COLOURS = 6;

	//-------------------------------------------------------------------------
	// Public Constants (in the grammar)
	
	/** Off-board marker. */
	public static final int OFF = -1;

	/** End-track marker. */
	public static final int END = -2;

	/** Value for a variable not defined. */
	public static final int UNDEFINED = -1;
	
	/** A large number that's unlikely to occur in a game. */
	public static final int INFINITY = 1000000000;
	
	//-------------------------------------------------------------------------
	// Internal constants (for internal use)
	
	/** Signifies no owner or no user. */
	public static final int NOBODY = 0;

	/** Signifies no piece. */
	public static final int NO_PIECE = 0;

	/** Empty cell indicator for testing blocked connections. */
	public static final int UNUSED = -4;	

	/** Highest absolute value of public constant. */
	public static final int CONSTANT_RANGE = 4;
	
	public static final double EPSILON = 1E-5;
	
	//-------------------------------------------------------------------------
	// Default values

	/** Default number of players we use if Players argument is null in Game. */
	public static final int DEFAULT_NUM_PLAYERS = 2;

	/** The integer corresponding to the default rotation of a site. */
	public static final int DEFAULT_ROTATION = 0;

	/** The integer corresponding to the default piece value of a site. */
	public static final int DEFAULT_VALUE = 0;

	/** The integer corresponding to the default state of a site. */
	public static final int DEFAULT_STATE = 0;

	/** The integer corresponding to the ground level. */
	public static final int GROUND_LEVEL = 0;

	/** Size of default board for boardless games on hex tiling. */
	public static final int SIZE_HEX_BOARDLESS = 21;

	/** Size of default board for boardless games. */
	public static final int SIZE_BOARDLESS = 41;
	
	//-------------------------------------------------------------------------
	// Emergency game descriptions
	
	public static String FAIL_SAFE_GAME_DESCRIPTION = 
			"(game \"Tic-Tac-Toe\"  \n" + 
			"    (players 2)  \n" + 
			"    (equipment { \n" + 
			"        (board (square 3)) \n" + 
			"        (piece \"Disc\" P1) \n" + 
			"        (piece \"Cross\" P2) \n" + 
			"    })  \n" + 
			"    (rules \n" + 
			"        (play (move Add (to (sites Empty))))\n" + 
			"        (end (if (is Line 3) (result Mover Win)))\n" + 
			"    )\n" + 
			")";
	
	public static String BASIC_GAME_DESCRIPTION = 
			"(game \"Name\"  \n" + 
			"    (players 2)  \n" + 
			"    (equipment { \n" + 
			"        (board (square 3)) \n" + 
			"        (piece \"Ball\" Each) \n" + 
			"    })  \n" + 
			"    (rules \n" + 
			"        (play (move Add (to (sites Empty))))\n" + 
			"        (end (if (no Moves Next) (result Mover Win)))\n" + 
			"    )\n" + 
			")";

	//-------------------------------------------------------------------------
	
}
