package reconstruction.output;

import game.Game;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import main.Constants;
import main.UnixPrintWriter;
import main.options.Ruleset;
import manager.utils.game_logs.MatchRecord;
import metrics.Utils;
import org.apache.commons.rng.RandomProviderState;
import other.GameLoader;
import other.context.Context;
import other.move.Move;
import other.topology.Edge;
import other.trial.Trial;

/***
 * To compute the Closeness distance, the average player 1 and duration metrics on the experimental 20 squares and Royal Game of Ur rulesets.
 */
public class UR20SquaresXP {

  /** The trials. */
  private static List<Trial> trials = new ArrayList<Trial>();

  /** The folder with the trials to use. */
  private static String folderTrials = "/res/trials/";

  // The RNGs of each trial.
  private static List<RandomProviderState> allStoredRNG =
    new ArrayList<RandomProviderState>();

  /** The path of 20 Squares. */
  private static String twentySquaresName =
    "/lud/board/race/escape/20 Squares.lud";

  /** The path of Royal Game of Ur. */
  private static String royalGameOfUrName =
    "/lud/board/race/escape/Royal Game Of Ur.lud";

  /** Distance to the goal for 20 Squares */
  private static Map<Integer, Integer> distance20Squares = new HashMap<>();

  /** Distance to the goal for Royal Game of Ur */
  private static Map<Integer, Integer> distanceRoyalGameOfUr = new HashMap<>();

  // I will need to update the rulesets name here.
  //final static String rulesetName = "Ruleset/Haretavl - No Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl - Top Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl - Both Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl - No Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl - Top Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl - Both Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Players - No Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Players - Top Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Players - Both Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Players - No Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Players - Top Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Players - Both Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Two Dogs - No Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Two Dogs - Top Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Two Dogs - Both Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Two Dogs - No Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Two Dogs - Top Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Two Dogs - Both Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Players Two Dogs - No Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Players Two Dogs - Top Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Players Two Dogs - Both Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Players Two Dogs - No Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Players Two Dogs - Top Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Players Two Dogs - Both Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Three Dogs Two Hares - No Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Three Dogs Two Hares - Top Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Three Dogs Two Hares - Both Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Three Dogs Two Hares - No Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Three Dogs Two Hares - Top Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Three Dogs Two Hares - Both Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Four Dogs Two Hares - No Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Four Dogs Two Hares - Top Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Four Dogs Two Hares - Both Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Four Dogs Two Hares - No Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Four Dogs Two Hares - Top Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Four Dogs Two Hares - Both Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Blocking Game Four pieces - No Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Blocking Game Four pieces - Top Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Blocking Game Four pieces - Both Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Blocking Game Four pieces - No Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Blocking Game Four pieces - Top Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Blocking Game Four pieces - Both Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Line Game Three pieces - No Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Line Game Three pieces - Top Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Line Game Three pieces - Both Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Line Game Three pieces - No Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Line Game Three pieces - Top Extension No Joined Diagonal (Suggested)";
  static final String rulesetName =
    "Ruleset/Line Game Three pieces - Both Extension No Joined Diagonal (Suggested)";

  //final static String rulesetName = "Ruleset/Janes Soppi - No Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Janes Soppi - Top Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Janes Soppi - Both Extensions Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Janes Soppi - No Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Janes Soppi - Top Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Janes Soppi - Both Extensions No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Four Dogs Two Hares Starting Position 1 - No Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Four Dogs Two Hares Starting Position 1 - Both Extensions Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Four Dogs Two Hares Starting Position 1 - No Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Four Dogs Two Hares Starting Position 1 - Both Extensions No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Four Dogs Two Hares Starting Position 2 - No Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Four Dogs Two Hares Starting Position 2 - Both Extensions Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Four Dogs Two Hares Starting Position 2 - No Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Four Dogs Two Hares Starting Position 2 - Both Extensions No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Four Dogs Two Hares Switch Starting Position 1 - No Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Four Dogs Two Hares Switch Starting Position 1 - Both Extensions Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Four Dogs Two Hares Switch Starting Position 1 - No Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Four Dogs Two Hares Switch Starting Position 2 - No Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Four Dogs Two Hares Switch Starting Position 2 - Both Extensions Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Four Dogs Two Hares Switch Starting Position 2 - No Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Starting Position - No Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Starting Position - Top Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Starting Position - Both Extensions Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Starting Position - No Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Starting Position - Top Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Starting Position - Both Extensions No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Three Dogs Two Hares Starting Position - No Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Three Dogs Two Hares Starting Position 1 - No Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Three Dogs Two Hares Starting Position 2 - No Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Three Dogs Two Hares Starting Position - Both Extensions Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Three Dogs Two Hares Starting Position - Both Extensions No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Three Dogs Two Hares Starting Position 1 - Top Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Three Dogs Two Hares Starting Position 1 - Top Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Three Dogs Two Hares Starting Position 2 - Top Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Three Dogs Two Hares Starting Position 3 - Top Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Three Dogs Two Hares Starting Position 2 - Top Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Two Dogs Starting Position 1 - Top Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Two Dogs Starting Position 2 - Top Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Two Dogs Starting Position 1 - Top Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Two Dogs Starting Position 2 - Top Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Starting Position - Top Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Two Dogs Starting Position 1 - Top Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Two Dogs Starting Position 1 - Top Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Two Dogs Starting Position 2 - Top Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Two Dogs Starting Position 2 - Top Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Three Dogs Two Hares Starting Position 1 - Top Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Three Dogs Two Hares Starting Position 1 - Top Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Three Dogs Two Hares Starting Position 2 - Top Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Three Dogs Two Hares Starting Position 3 - Top Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Three Dogs Two Hares Starting Position 2 - Top Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Two Dogs - Top Extension No Joined Diagonal - Starting Position 3 (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Two Dogs Starting Position - No Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Starting Position - No Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Two Dogs Starting Position - No Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Three Dogs Two Hares Starting Position - No Extension Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Two Dogs Starting Position 1 - No Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Two Dogs Starting Position 2 - No Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Starting Position - No Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Three Dogs Two Hares Starting Position 1 - No Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Three Dogs Two Hares Starting Position 2 - No Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Two Dogs Starting Position 1 - No Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Two Dogs Starting Position 2 - No Extension No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Two Dogs Starting Position - Both Extensions Joined Diagonals (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Starting Position - Both Extensions Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Three Dogs Two Hares Starting Position - Both Extensions Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Two Dogs Starting Position - Both Extensions Joined Diagonals (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Two Dogs Starting Position - Both Extensions No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Two Dogs Starting Position - Both Extensions No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Three Dogs Two Hares Starting Position - Both Extensions No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Switch Starting Position - Both Extensions No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Four Dogs Two Hares Switch Starting Position 1 - Both Extensions No Joined Diagonal (Suggested)";
  //final static String rulesetName = "Ruleset/Haretavl Four Dogs Two Hares Switch Starting Position 2 - Both Extensions No Joined Diagonal (Suggested)";

  // -----------------------------------------------------------------------------------

  /**
   * Main method
   * @param args
   */
  public static void main(final String[] args) {
    distanceRoyalGameOfUr.put(20, 15);
    distanceRoyalGameOfUr.put(21, 15);
    distanceRoyalGameOfUr.put(3, 14);
    distanceRoyalGameOfUr.put(17, 14);
    distanceRoyalGameOfUr.put(16, 13);
    distanceRoyalGameOfUr.put(2, 13);
    distanceRoyalGameOfUr.put(15, 12);
    distanceRoyalGameOfUr.put(1, 12);
    distanceRoyalGameOfUr.put(14, 11);
    distanceRoyalGameOfUr.put(0, 11);
    distanceRoyalGameOfUr.put(6, 10);
    distanceRoyalGameOfUr.put(7, 9);
    distanceRoyalGameOfUr.put(8, 8);
    distanceRoyalGameOfUr.put(9, 7);
    distanceRoyalGameOfUr.put(10, 6);
    distanceRoyalGameOfUr.put(11, 5);
    distanceRoyalGameOfUr.put(12, 4);
    distanceRoyalGameOfUr.put(13, 3);
    distanceRoyalGameOfUr.put(5, 2);
    distanceRoyalGameOfUr.put(19, 2);
    distanceRoyalGameOfUr.put(4, 1);
    distanceRoyalGameOfUr.put(18, 1);
    distance20Squares.put(20, 17);
    distance20Squares.put(21, 17);
    distance20Squares.put(3, 16);
    distance20Squares.put(12, 16);
    distance20Squares.put(2, 15);
    distance20Squares.put(11, 15);
    distance20Squares.put(1, 14);
    distance20Squares.put(10, 14);
    distance20Squares.put(0, 13);
    distance20Squares.put(9, 13);
    distance20Squares.put(4, 12);
    distance20Squares.put(5, 11);
    distance20Squares.put(6, 10);
    distance20Squares.put(7, 9);
    distance20Squares.put(8, 8);
    distance20Squares.put(13, 7);
    distance20Squares.put(14, 6);
    distance20Squares.put(15, 5);
    distance20Squares.put(16, 4);
    distance20Squares.put(17, 3);
    distance20Squares.put(18, 2);
    distance20Squares.put(19, 1);
    computeMetrics(rulesetName);
  }

  // -----------------------------------------------------------------------------------

  /**
   * Compute the metrics we want in this experimentation (duration, avg P1, Closeness)
   * @param rulesetExpected
   */
  public static void computeMetrics(final String rulesetExpected) {
    Game rulesetGame = getRuleset(twentySquaresName, rulesetExpected);
    getTrials(rulesetGame);

    System.out.println("trial size = " + trials.size());

    // The vectors used to get the average distance of P1 and P2 to the goal.
    final TDoubleArrayList averageDistanceP1ToGoal = new TDoubleArrayList();
    final TDoubleArrayList averageDistanceP2ToGoal = new TDoubleArrayList();
    int totalDurationAllTrials = 0;
    int numTimesSwitchAdvantage = 0;
    int numTimesP1Won = 0;

    for (int trialIndex = 0; trialIndex < trials.size(); trialIndex++) {
      // The edge usage on the current trial.
      final TDoubleArrayList edgesUsageCurrentTrial = new TDoubleArrayList();
      for (
        int i = 0;
        i < rulesetGame.board().topology().edges().size();
        i++
      ) edgesUsageCurrentTrial.add(0.0);

      final Trial trial = trials.get(trialIndex);
      final RandomProviderState rngState = allStoredRNG.get(trialIndex);

      // Setup a new instance of the game
      final Context context = Utils.setupNewContext(rulesetGame, rngState);
      int totalEdgesUsage = 0;

      totalDurationAllTrials +=
        trial.numMoves() - trial.numInitialPlacementMoves();

      // Run the playout.
      for (
        int i = trial.numInitialPlacementMoves();
        i < trial.numMoves();
        i++
      ) {
        // We go to the next move.
        context.game().apply(context, trial.getMove(i));

        // FOR UR and 20 squares
        // To count the frequency/usage of each edge on the board.
        final Move lastMove = context.trial().lastMove();
        final int vertexFrom = lastMove.fromNonDecision();
        // To not take in account moves coming from the hand.
        if (
          vertexFrom < 0 ||
          vertexFrom >= rulesetGame.board().topology().vertices().size()
        ) continue;
        final int vertexTo = lastMove.toNonDecision();

        for (
          int j = 0;
          j < rulesetGame.board().topology().edges().size();
          j++
        ) {
          final Edge edge = rulesetGame.board().topology().edges().get(j);
          if (
            (edge.vertices().get(0).index() == vertexFrom &&
              edge.vertices().get(1).index() == vertexTo) ||
            (edge.vertices().get(0).index() == vertexTo &&
              edge.vertices().get(1).index() == vertexFrom)
          ) edgesUsageCurrentTrial.set(j, edgesUsageCurrentTrial.get(j) + 1);
        }
        totalEdgesUsage++;
      }

      if (trial.ranking()[1] == 1) numTimesP1Won++;
    }

    //		System.out.println("Final results are");
    //		for(int i = 0; i < rulesetGame.board().topology().edges().size(); i++)
    //			System.out.println(i + "," + edgesUsageMinisingSymetryDistance.get(i));

    final String output =
      "EdgesResultLudus Coriovalli-" +
      rulesetName.substring(rulesetName.indexOf("/") + 1) +
      ".csv";
    // Write the new CSV.
    try (
      final PrintWriter writer = new UnixPrintWriter(new File(output), "UTF-8")
    ) {
      //			for(int i = 0; i < rulesetGame.board().topology().edges().size(); i++)
      //				writer.println(i + "," + edgesUsageMinisingSymetryDistance.get(i));
    } catch (FileNotFoundException e1) {
      e1.printStackTrace();
    } catch (UnsupportedEncodingException e1) {
      e1.printStackTrace();
    }
  }

  // ---------------------------

  /**
   * @param game The game.
   */
  private static Game getRuleset(
    final String gameName,
    final String rulesetExpected
  ) {
    final Game game = GameLoader.loadGameFromName(gameName);
    final List<Ruleset> rulesetsInGame = game.description().rulesets();
    Game rulesetGame = null;
    // Code for games with many rulesets
    if (rulesetsInGame != null && !rulesetsInGame.isEmpty()) {
      for (int rs = 0; rs < rulesetsInGame.size(); rs++) {
        final Ruleset ruleset = rulesetsInGame.get(rs);

        // We check if we want a specific ruleset.
        if (
          !rulesetExpected.isEmpty() &&
          !rulesetExpected.equals(ruleset.heading())
        ) continue;

        rulesetGame = GameLoader.loadGameFromName(
          gameName,
          ruleset.optionSettings()
        );
      }
    }

    if (rulesetGame == null) System.err.println("Game or Ruleset unknown");

    System.out.println("Game Name = " + rulesetGame.name());
    System.out.println("Ruleset Name = " + rulesetGame.getRuleset().heading());

    return rulesetGame;
  }

  // ---------------------------

  /**
   * @param game The game.
   */
  private static void getTrials(final Game game) {
    final File currentFolder = new File(".");
    final File folder = new File(
      currentFolder.getAbsolutePath() + folderTrials
    );
    final String gameName = game.name();
    final String rulesetName =
      game.getRuleset() == null ? "" : game.getRuleset().heading();

    String trialFolderPath = folder + "/" + gameName;
    if (!rulesetName.isEmpty()) trialFolderPath +=
      File.separator + rulesetName.replace("/", "_");

    final File trialFolder = new File(trialFolderPath);

    if (!trialFolder.exists()) System.out.println(
      "DO NOT FOUND TRIALS - Path is " + trialFolder
    );

    for (final File trialFile : trialFolder.listFiles()) {
      //System.out.println(trialFile.getName());
      if (trialFile.getName().contains(".txt")) {
        MatchRecord loadedRecord;
        try {
          loadedRecord = MatchRecord.loadMatchRecordFromTextFile(
            trialFile,
            game
          );
          final Trial loadedTrial = loadedRecord.trial();
          trials.add(loadedTrial);
          allStoredRNG.add(loadedRecord.rngState());
        } catch (final FileNotFoundException e) {
          e.printStackTrace();
        } catch (final IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
