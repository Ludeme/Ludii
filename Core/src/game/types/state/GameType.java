package game.types.state;

import game.Game;
import java.io.Serializable;

/**
 * Defines known characteristics of games.
 *
 * @author cambolbro and Eric.Piette
 *
 * @remarks These flags are used to determine which state to choose for a given
 *          game description.
 */
public interface GameType extends Serializable {
  /**
   * On if this game may generate moves that use from-positions (in addition to to-positions).
   */
  public static final long UsesFromPositions = 0x1L;

  /**
   * On if the game involved a state value for a site.
   */
  public static final long SiteState = 0x1L << 1;

  /**
   * On if the game involved a count for a site.
   */
  public static final long Count = 0x1L << 2;

  /**
   * On if the game has hidden info.
   */
  public static final long HiddenInfo = 0x1L << 3;

  /**
   * On if the game is a stacking game.
   */
  public static final long Stacking = 0x1L << 4;

  /**
   * On if the game is a boardless game.
   */
  public static final long Boardless = 0x1L << 5;

  /**
   * On if the game involves some stochastic values.
   */
  public static final long Stochastic = 0x1L << 6;

  /**
   * On if the game is a deduction puzzle.
   */
  public static final long DeductionPuzzle = 0x1L << 7;

  /**
   * On if the game involves score.
   */
  public static final long Score = 0x1L << 8;

  /**
   * On if we store the visited sites in the same turn.
   */
  public static final long Visited = 0x1L << 9;

  /**
   * On if the game has simultaneous actions.
   */
  public static final long Simultaneous = 0x1L << 10;

  /**
   * On if this game is 3D games.
   */
  public static final long ThreeDimensions = 0x1L << 11;

  /**
   * On if the game does not end if all the players pass.
   */
  public static final long NotAllPass = 0x1L << 12;

  /**
   * On if the game involves card.
   */
  public static final long Card = 0x1L << 13;

  /**
   * On if the game involves large piece.
   */
  public static final long LargePiece = 0x1L << 14;

  /**
   * On if the game involves some capture in sequence.
   */
  public static final long SequenceCapture = 0x1L << 15;

  /**
   * On if the games has some tracks defined.
   */
  public static final long Track = 0x1L << 16;

  /**
   * On if the game has some rotation values.
   */
  public static final long Rotation = 0x1L << 17;

  /**
   * On if the game has team.
   */
  public static final long Team = 0x1L << 18;

  /**
   * On if the game has some betting actions.
   */
  public static final long Bet = 0x1L << 19;

  /**
   * On if the game has some hash scores.
   */
  public static final long HashScores = 0x1L << 20;

  /**
   * On If the game has some hash amounts.
   */
  public static final long HashAmounts = 0x1L << 21;

  /**
   * On if the game has some hash phases.
   */
  public static final long HashPhases = 0x1L << 22;

  /**
   * On if the game is a graph game.
   */
  public static final long Graph = 0x1L << 23;

  /**
   * On if the game can be played on the vertices.
   */
  public static final long Vertex = 0x1L << 24;

  /**
   * On if the game can be played on the cells.
   */
  public static final long Cell = 0x1L << 25;

  /**
   * On if the game can played on the edges.
   */
  public static final long Edge = 0x1L << 26;

  /**
   * On if the game has dominoes.
   */
  public static final long Dominoes = 0x1L << 27;

  /**
   * On if the game has a line of play used.
   */
  public static final long LineOfPlay = 0x1L << 28;

  /**
   * On if the game has some replay actions.
   */
  public static final long MoveAgain = 0x1L << 29;

  /**
   * On if the game uses some piece values.
   */
  public static final long Value = 0x1L << 30;

  /**
   * On if the game has some vote actions.
   */
  public static final long Vote = 0x1L << 31;

  /**
   * On if the game has some Note actions.
   */
  public static final long Note = 0x1L << 32;

  /**
   * On if the game involves loop.
   */
  public static final long Loops = 0x1L << 33;

  /**
   * On if the game needs some adjacent step distance between sites.
   */
  public static final long StepAdjacentDistance = 0x1L << 34;

  /**
   * On if the game needs some orthogonal step distance between sites.
   */
  public static final long StepOrthogonalDistance = 0x1L << 35;

  /**
   * On if the game needs some diagonal step distance between sites.
   */
  public static final long StepDiagonalDistance = 0x1L << 36;

  /**
   * On if the game needs some off step distance between sites.
   */
  public static final long StepOffDistance = 0x1L << 37;

  /**
   * On if the game needs some neighbours step distance between sites.
   */
  public static final long StepAllDistance = 0x1L << 38;

  /**
   * On if the tracks on the game have an internal loop.
   */
  public static final long InternalLoopInTrack = 0x1L << 39;

  /**
   * On if the game uses a swap rule.
   */
  public static final long UsesSwapRule = 0x1L << 40;

  /**
   * On if the game checks the positional repetition in the game.
   */
  public static final long RepeatPositionalInGame = 0x1L << 41;

  /**
   * On if the game checks the positional repetition in the turn.
   */
  public static final long RepeatPositionalInTurn = 0x1L << 42;

  /**
   * On if the game uses some pending states/values.
   */
  public static final long PendingValues = 0x1L << 43;

  /**
   * On if the game uses some maps to values.
   */
  public static final long MapValue = 0x1L << 44;

  /**
   * On if the game uses some values to remember.
   */
  public static final long RememberingValues = 0x1L << 45;

  /**
   * On if the game involves payoff.
   */
  public static final long Payoff = 0x1L << 46;

  /**
   * On if the game checks the situational repetition in the game.
   */
  public static final long RepeatSituationalInGame = 0x1L << 47;

  /**
   * On if the game checks the situational repetition in the turn.
   */
  public static final long RepeatSituationalInTurn = 0x1L << 48;

  /**
   * On if the game checks the repetition of cycles.
   */
  public static final long CycleDetection = 0x1L << 49;

  /**
   * @param game The game.
   * @return Accumulated flags for this state type.
   */
  public long gameFlags(final Game game);

  /**
   * @return true of the function is immutable, allowing extra optimisations.
   */
  public boolean isStatic();

  /**
   * Called once after a game object has been created. Allows for any game-
   * specific preprocessing (e.g. precomputing and caching of static results).
   *
   * @param game
   */
  public void preprocess(final Game game);
}
