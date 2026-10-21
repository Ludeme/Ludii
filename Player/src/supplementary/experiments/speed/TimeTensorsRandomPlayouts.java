package supplementary.experiments.speed;

import game.Game;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.DaemonThreadFactory;
import main.FileHandling;
import main.StringRoutines;
import main.UnixPrintWriter;
import other.GameLoader;
import other.context.Context;
import other.playout.PlayoutMoveSelector;
import other.trial.Trial;
import supplementary.experiments.speed.PlayoutsPerSec.PlayoutsTimingData;
import utils.LudiiGameWrapper;
import utils.LudiiStateWrapper;

/**
 * Experiment for timing random playouts in which we also create a state tensor
 * for every state that we encounter. Note that this also means that we cannot
 * use optimised playout strategies.
 *
 * @author Dennis Soemers
 */
public final class TimeTensorsRandomPlayouts {

  /**
   * Names of the games to play. Each should end with ".lud".
   * Use "all" to run all games we can find. Runs all games by default
   */
  private List<String> gameNames;

  /** List of game directories to exclude from experiment */
  private List<String> excludeDirs;

  /** Options to tweak game (variant, rules, board, etc.) */
  private List<String> gameOptions;

  /** Number of seconds of warming up (per game) */
  private int warmingUpSecs;

  /** Number of seconds over which we measure playouts (per game) */
  private int measureSecs;

  /** Number of threads to use for parallel playouts */
  private int numThreads;

  /** Maximum number of actions to execute per playout (-1 for no cap) */
  private int playoutActionCap;

  /** Seed for RNG. -1 means just use ThreadLocalRandom.current() */
  private int seed;

  /** The name of the csv to export with the results. */
  private String exportCSV;

  protected static class PlayoutsTimingData {

    protected final double seconds;
    protected final int numPlayouts;
    protected final int numMoves;

    public PlayoutsTimingData(
      final double seconds,
      final int numPlayouts,
      final int numMoves
    ) {
      this.seconds = seconds;
      this.numPlayouts = numPlayouts;
      this.numMoves = numMoves;
    }
  }

  //-------------------------------------------------------------------------

  /**
   * Constructor
   */
  private TimeTensorsRandomPlayouts() {
    // Nothing to do here
  }

  //-------------------------------------------------------------------------

  /**
   * Start the experiment
   */
  @SuppressWarnings("unchecked")
  public void startExperiment() {
    // Gather all the game names
    final List<String> gameNamesToTest = new ArrayList<String>();

    if (gameNames.get(0).equalsIgnoreCase("all")) {
      // lowercase all the exclude dirs, makes tests easier
      for (int i = 0; i < excludeDirs.size(); ++i) {
        excludeDirs.set(i, excludeDirs.get(i).toLowerCase());
      }

      final String[] allGameNames = FileHandling.listGames();

      for (final String gameName : allGameNames) {
        final String name = gameName.replaceAll(Pattern.quote("\\"), "/");
        final String[] nameParts = name.split(Pattern.quote("/"));

        boolean exclude = false;
        for (final String part : nameParts) {
          if (
            excludeDirs.contains(part.toLowerCase()) ||
            part.equals("plex") ||
            part.equals("bad") ||
            part.equals("bad_playout") ||
            part.equals("wip") ||
            part.equals("test")
          ) {
            exclude = true;
            break;
          }
        }

        if (!exclude) {
          gameNamesToTest.add(name);
        }
      }
    } else {
      final String[] allGameNames = FileHandling.listGames();

      for (String gameName : gameNames) {
        gameName = gameName.replaceAll(Pattern.quote("\\"), "/");

        for (String name : allGameNames) {
          name = name.replaceAll(Pattern.quote("\\"), "/");

          if (name.endsWith(gameName)) {
            gameNamesToTest.add(name);
          }
        }
      }
    }

    if (numThreads <= 0) System.err.println(
      "The number of threads must be >= 1."
    );

    System.out.println("NUM GAMES = " + gameNamesToTest.size());
    System.out.println("NUM THREADS = " + numThreads);

    System.out.println();
    System.out.println(
      "Using " + warmingUpSecs + " warming-up seconds per game."
    );
    System.out.println(
      "Measuring results over " + measureSecs + " seconds per game."
    );

    System.out.println();

    final List<String> results = new ArrayList<String>();
    results.add(
      StringRoutines.join(",", new String[] {
        "Name",
        "p/s",
        "m/s",
        "TotalPlayouts",
      })
    );

    for (final String gameName : gameNamesToTest) {
      final Game game = GameLoader.loadGameFromName(gameName, gameOptions);
      final LudiiGameWrapper gameWrapper = new LudiiGameWrapper(game);

      final String[] result = new String[4];
      if (game != null) System.out.println("Run: " + game.name());

      result[0] = game.name();

      {
        // Warming up (just doing this single-threaded)
        final LudiiStateWrapper stateWrapper = new LudiiStateWrapper(
          gameWrapper
        );

        long stopAt = 0L;
        long start = System.nanoTime();
        double abortAt = start + warmingUpSecs * 1000000000.0;
        while (stopAt < abortAt) {
          stateWrapper.reset();

          int numActionsPlayed = 0;
          while (
            !stateWrapper.isTerminal() &&
            (numActionsPlayed < playoutActionCap || playoutActionCap < 0)
          ) {
            // Compute tensor for current state
            @SuppressWarnings("unused") // Do NOT remove! We need this for accurate timings!!!!!!!!!
            final float[][][] stateTensor = stateWrapper.toTensor();
            @SuppressWarnings("unused") // Do NOT remove! We need this for accurate timings!!!!!!!!!
            final int[][] legalMoveTensors = stateWrapper.legalMovesTensors();

            // Play random action
            stateWrapper.applyNthMove(
              ThreadLocalRandom.current().nextInt(stateWrapper.numLegalMoves())
            );
            ++numActionsPlayed;
          }

          stopAt = System.nanoTime();
        }
      }
      System.gc();

      // The Test
      final PlayoutsTimingData[] resultsPerThread =
        new PlayoutsTimingData[numThreads];

      if (numThreads == 1) {
        // Special case, no need for threads here
        resultsPerThread[0] = timePlayouts(gameWrapper, 0);
      } else {
        // Multiple threads
        @SuppressWarnings("resource")
        final ExecutorService threadPool = Executors.newFixedThreadPool(
          numThreads,
          DaemonThreadFactory.INSTANCE
        );

        @SuppressWarnings("rawtypes")
        final Future[] resultFutures = new Future[numThreads];

        for (int threadIdx = 0; threadIdx < numThreads; ++threadIdx) {
          final int finalThreadIdx = threadIdx;
          resultFutures[finalThreadIdx] = threadPool.submit(() -> {
            return timePlayouts(gameWrapper, finalThreadIdx);
          });
        }

        try {
          for (int i = 0; i < numThreads; ++i) {
            resultsPerThread[i] = (
              (Future<PlayoutsTimingData>) resultFutures[i]
            ).get();
          }
        } catch (final InterruptedException | ExecutionException e) {
          e.printStackTrace();
        }

        threadPool.shutdown();

        try {
          threadPool.awaitTermination(measureSecs * 2, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
          e.printStackTrace();
        }
      }

      long sumPlayouts = 0L;
      long sumMoves = 0L;
      double sumSeconds = 0.0;

      for (final PlayoutsTimingData data : resultsPerThread) {
        sumPlayouts += data.numPlayouts;
        sumMoves += data.numMoves;
        sumSeconds += data.seconds;
      }

      final double rate = sumPlayouts / (sumSeconds / numThreads);
      final double rateMove = sumMoves / (sumSeconds / numThreads);

      result[1] = String.valueOf(rate);
      result[2] = String.valueOf(rateMove);
      result[3] = String.valueOf(sumPlayouts);
      results.add(StringRoutines.join(",", result));

      System.out.println(
        game.name() + "\t-\t" + rate + " p/s\t-\t" + rateMove + " m/s\n"
      );
    }

    try (
      final PrintWriter writer = new UnixPrintWriter(
        new File(exportCSV),
        "UTF-8"
      )
    ) {
      for (final String toWrite : results)
        writer.println(StringRoutines.join(",", toWrite));
    } catch (final FileNotFoundException | UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

  private PlayoutsTimingData timePlayouts(
    final LudiiGameWrapper gameWrapper,
    final int threadIdx
  ) {
    // Set up RNG for this game
    final Random rng;
    if (seed == -1) rng = ThreadLocalRandom.current();
    else rng = new Random(
      (long) gameWrapper.name().hashCode() * (long) (seed + threadIdx)
    );

    final LudiiStateWrapper stateWrapper = new LudiiStateWrapper(gameWrapper);
    int playouts = 0;
    int moveDone = 0;

    long stopAt = 0L;
    final long start = System.nanoTime();
    final double abortAt = start + measureSecs * 1000000000.0;

    while (stopAt < abortAt) {
      stateWrapper.reset();

      int numActionsPlayed = 0;
      while (
        !stateWrapper.isTerminal() &&
        (numActionsPlayed < playoutActionCap || playoutActionCap < 0)
      ) {
        // Compute tensor for current state
        @SuppressWarnings("unused") // Do NOT remove! We need this for accurate timings!!!!!!!!!
        final float[][][] stateTensor = stateWrapper.toTensor();
        @SuppressWarnings("unused") // Do NOT remove! We need this for accurate timings!!!!!!!!!
        final int[][] legalMoveTensors = stateWrapper.legalMovesTensors();

        // Play random action
        stateWrapper.applyNthMove(rng.nextInt(stateWrapper.numLegalMoves()));
        ++numActionsPlayed;
      }
      stopAt = System.nanoTime();
      ++playouts;
      moveDone += numActionsPlayed;
    }

    final double secs = (stopAt - start) / 1000000000.0;

    return new PlayoutsTimingData(secs, playouts, moveDone);
  }

  //-------------------------------------------------------------------------

  /**
   * Main method
   * @param args
   */
  @SuppressWarnings("unchecked")
  public static void main(final String[] args) {
    // define options for arg parser
    final CommandLineArgParse argParse = new CommandLineArgParse(
      true,
      "Measure playouts per second for one or more games, including" +
        " time spent on building state and legal move tensors."
    );

    argParse.addOption(
      new ArgOption()
        .withNames("--game-names")
        .help(
          "Only games that include at least one of the provided strings in their name are included."
        )
        .withDefault(Arrays.asList(""))
        .withNumVals("+")
        .withType(OptionTypes.String)
    );

    argParse.addOption(
      new ArgOption()
        .withNames("--exclude-dirs")
        .help("List of game directories to exclude from experiment.")
        .withDefault(Arrays.asList("puzzle"))
        .withNumVals("*")
        .withType(OptionTypes.String)
    );

    argParse.addOption(
      new ArgOption()
        .withNames("--game-options")
        .help("Game Options to load.")
        .withDefault(new ArrayList<String>(0))
        .withNumVals("*")
        .withType(OptionTypes.String)
    );

    argParse.addOption(
      new ArgOption()
        .withNames("--warming-up-secs", "--warming-up")
        .help("Number of seconds of warming up (per game).")
        .withDefault(Integer.valueOf(10))
        .withNumVals(1)
        .withType(OptionTypes.Int)
    );
    argParse.addOption(
      new ArgOption()
        .withNames("--measure-secs")
        .help("Number of seconds over which we measure playouts (per game).")
        .withDefault(Integer.valueOf(30))
        .withNumVals(1)
        .withType(OptionTypes.Int)
    );
    argParse.addOption(
      new ArgOption()
        .withNames("--num-threads")
        .help("Number of threads to use for parallel playouts.")
        .withDefault(Integer.valueOf(1))
        .withNumVals(1)
        .withType(OptionTypes.Int)
    );
    argParse.addOption(
      new ArgOption()
        .withNames("--export-csv")
        .help(
          "Filename (or filepath) to write results to. By default writes to ./results.csv"
        )
        .withDefault("results.csv")
        .withNumVals(1)
        .withType(OptionTypes.String)
    );
    argParse.addOption(
      new ArgOption()
        .withNames("--playout-action-cap")
        .help(
          "Maximum number of actions to execute per playout (-1 for no cap)."
        )
        .withDefault(Integer.valueOf(-1))
        .withNumVals(1)
        .withType(OptionTypes.Int)
    );
    argParse.addOption(
      new ArgOption()
        .withNames("--seed")
        .help(
          "Seed to use for RNG. Default (-1) just uses ThreadLocalRandom.current()."
        )
        .withDefault(Integer.valueOf(-1))
        .withNumVals(1)
        .withType(OptionTypes.Int)
    );

    // parse the args
    if (!argParse.parseArguments(args)) return;

    // use the parsed args
    final TimeTensorsRandomPlayouts experiment =
      new TimeTensorsRandomPlayouts();

    experiment.gameNames = (List<String>) argParse.getValue("--game-names");
    experiment.excludeDirs = (List<String>) argParse.getValue("--exclude-dirs");
    experiment.gameOptions = (List<String>) argParse.getValue("--game-options");
    experiment.warmingUpSecs = argParse.getValueInt("--warming-up-secs");
    experiment.measureSecs = argParse.getValueInt("--measure-secs");
    experiment.numThreads = argParse.getValueInt("--num-threads");
    experiment.exportCSV = argParse.getValueString("--export-csv");
    experiment.playoutActionCap = argParse.getValueInt("--playout-action-cap");
    experiment.seed = argParse.getValueInt("--seed");

    experiment.startExperiment();
  }
}
