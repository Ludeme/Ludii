package supplementary.experiments.speed;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import features.WeightVector;
import features.feature_sets.BaseFeatureSet;
import features.feature_sets.LegacyFeatureSet;
import features.feature_sets.NaiveFeatureSet;
import features.feature_sets.network.JITSPatterNetFeatureSet;
import features.feature_sets.network.SPatterNetFeatureSet;
import features.generation.AtomicFeatureGenerator;
import function_approx.LinearFunction;
import game.Game;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.FileHandling;
import main.StringRoutines;
import main.UnixPrintWriter;
import main.collections.FVector;
import other.GameLoader;
import other.context.Context;
import other.playout.PlayoutMoveSelector;
import other.trial.Trial;
import playout_move_selectors.FeaturesSoftmaxMoveSelector;
import policies.softmax.SoftmaxFromMetadataSelection;
import policies.softmax.SoftmaxPolicy;
import policies.softmax.SoftmaxPolicyLinear;

/**
 * Experiment for measuring playouts per second.
 * 
 * @author Dennis Soemers, Eric.Piette
 */
public final class PlayoutsPerSec
{
	/** Number of seconds of warming up (per game) */
	private int warmingUpSecs;
	
	/** Number of seconds over which we measure playouts (per game) */
	private int measureSecs;
	
	/** Maximum number of actions to execute per playout (-1 for no cap) */
	private int playoutActionCap;
	
	/** Features to use for non-uniform playouts. Empty string means no features, i.e. uniform playouts */
	private String featuresToUse;
	
	/** Type of feature set to use (ignored if not using any features, or if using features from metadata) */
	private String featureSetType;
	
	/** Seed for RNG. -1 means just use ThreadLocalRandom.current() */
	private int seed;
	
	/** List of game names, at least one of which must be contained in a game's name for it to be included */
	private List<String> gameNames = null;
	
	/** Ruleset name. Will try to compile ALL games that match game name with this ruleset */
	private String ruleset = null;

	/** The name of the csv to export with the results. */
	private String exportCSV;
	
	/** If true, suppress standard out print messages (will still write CSV with results at the end) */
	private boolean suppressPrints;
	
	/** If true, we disable custom (optimised) playout strategies on any games played */
	private boolean noCustomPlayouts;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	private PlayoutsPerSec()
	{
		// Nothing to do here
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Start the experiment
	 */
	public void startExperiment()
	{
		final String[] allGameNames = FileHandling.listGames();
		final List<String> gameNameToTest = new ArrayList<String>();

		for (final String gameName : allGameNames)
		{
			final String name = gameName.replaceAll(Pattern.quote("\\"), "/");
			
			boolean nameMatch = false;
			for (final String mustContain : gameNames)
			{
				if (name.contains(mustContain))
				{
					nameMatch = true;
					break;
				}
			}
			
			if (!nameMatch)
				continue;

			final String[] nameParts = name.split(Pattern.quote("/"));
			boolean exclude = false;

			for (int i = 0; i < nameParts.length - 1; i++)
			{
				final String part = nameParts[i].toLowerCase();
				if (part.contains("plex"))
				{
					exclude = true;
					break;
				}

				if (part.contains("wishlist"))
				{
					exclude = true;
					break;
				}

				if (part.contains("wip"))
				{
					exclude = true;
					break;
				}

				if (part.contains("subgame"))
				{
					exclude = true;
					break;
				}

				if (part.contains("deduction"))
				{
					exclude = true;
					break;
				}

				if (part.contains("reconstruction"))
				{
					exclude = true;
					break;
				}

				if (part.contains("test"))
				{
					exclude = true;
					break;
				}

				if (part.contains("def"))
				{
					exclude = true;
					break;
				}

				if (part.contains("proprietary"))
				{
					exclude = true;
					break;
				}
			}

			if (!exclude)
			{
				gameNameToTest.add(name);
			}
		}

		if (!suppressPrints)
		{
			System.out.println("NUM GAMES = " + gameNameToTest.size());
	
			System.out.println();
			System.out.println("Using " + warmingUpSecs + " warming-up seconds per game.");
			System.out.println("Measuring results over " + measureSecs + " seconds per game.");
			System.out.println();
		}

		final List<String> results = new ArrayList<String>();
		results.add(StringRoutines.join(",", new String[]{ "Name", "p/s", "m/s", "TotalPlayouts" }));

		for (final String gameName : gameNameToTest)
		{
			final Game game;
			
			if (ruleset != null && !ruleset.equals(""))
				game = GameLoader.loadGameFromName(gameName, ruleset);
			else
				game = GameLoader.loadGameFromName(gameName, new ArrayList<String>());
			
			if (noCustomPlayouts && game.hasCustomPlayouts())
			{
				game.disableCustomPlayouts();
			}
			
			final String[] result = new String[4];
			if (game != null && !suppressPrints)
				System.out.println("Run: " + game.name());

			result[0] = game.name();
			
			// Load features and weights if we want to use them
			final PlayoutMoveSelector playoutMoveSelector = constructPlayoutMoveSelector(game);

			final Trial trial = new Trial(game);
			final Context context = new Context(game, trial);

			// Warming up
			long stopAt = 0L;
			long start = System.nanoTime();
			double abortAt = start + warmingUpSecs * 1000000000.0;
			while (stopAt < abortAt)
			{
				game.start(context);
				game.playout(context, null, 1.0, playoutMoveSelector, -1, playoutActionCap, ThreadLocalRandom.current());
				stopAt = System.nanoTime();
			}
			System.gc();

			// Set up RNG for this game
			final Random rng;
			if (seed == -1)
				rng = ThreadLocalRandom.current();
			else
				rng = new Random((long)game.name().hashCode() * (long)seed);

			// The Test
			stopAt = 0L;
			start = System.nanoTime();
			abortAt = start + measureSecs * 1000000000.0;
			int playouts = 0;
			int moveDone = 0;
			while (stopAt < abortAt)
			{
				game.start(context);
				game.playout(context, null, 1.0, playoutMoveSelector, -1, playoutActionCap, rng);
				moveDone += context.trial().numMoves();
				stopAt = System.nanoTime();
				++playouts;
			}

			final double secs = (stopAt - start) / 1000000000.0;
			final double rate = (playouts / secs);
			final double rateMove = (moveDone / secs);

			result[1] = String.valueOf(rate);
			result[2] = String.valueOf(rateMove);
			result[3] = String.valueOf(playouts);
			results.add(StringRoutines.join(",", result));

			if (!suppressPrints)
				System.out.println(game.name() + "\t-\t" + rate + " p/s\t-\t" + rateMove + " m/s\n");
		}

		try (final PrintWriter writer = new UnixPrintWriter(new File(exportCSV), "UTF-8"))
		{
			for (final String toWrite : results)
				writer.println(StringRoutines.join(",", toWrite));
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Helper method to construct playout move selector based on features-related args
	 * @param game
	 * @return
	 */
	private PlayoutMoveSelector constructPlayoutMoveSelector(final Game game)
	{
		final PlayoutMoveSelector playoutMoveSelector;
		
		if (featuresToUse.length() > 0)
		{
			if (featuresToUse.toLowerCase().contains("metadata"))
			{
				// Load features from metadata
				final SoftmaxFromMetadataSelection softmax = new SoftmaxFromMetadataSelection(0.0);
				softmax.initAI(game, -1);
				
				final SoftmaxPolicy wrappedSoftmax = softmax.wrappedSoftmax();
				if (wrappedSoftmax instanceof SoftmaxPolicyLinear)
				{
					final SoftmaxPolicyLinear linearWrappedSoftmax = (SoftmaxPolicyLinear) wrappedSoftmax;
					if (linearWrappedSoftmax.featureSets().length > 0)
					{
						final BaseFeatureSet[] featureSets = linearWrappedSoftmax.featureSets();
						final WeightVector[] weights = new WeightVector[linearWrappedSoftmax.linearFunctions().length];
						for (int i = 0; i < linearWrappedSoftmax.linearFunctions().length; ++i)
						{
							if (linearWrappedSoftmax.linearFunctions()[i] != null)
								weights[i] = linearWrappedSoftmax.linearFunctions()[i].effectiveParams();
						}
						playoutMoveSelector = new FeaturesSoftmaxMoveSelector(featureSets, weights, false);
					}
					else
					{
						playoutMoveSelector = null;
					}
				}
				else
				{
					playoutMoveSelector = null;
				}
			}
			else if (featuresToUse.toLowerCase().startsWith("atomic-"))
			{
				final String[] strSplit = featuresToUse.split(Pattern.quote("-"));
				final int maxWalkSize = Integer.parseInt(strSplit[1]);
				final int maxStraightWalkSize = Integer.parseInt(strSplit[2]);
				final AtomicFeatureGenerator featureGen = new AtomicFeatureGenerator(game, maxWalkSize, maxStraightWalkSize);
				final BaseFeatureSet featureSet;
				
				if (featureSetType.equals("SPatterNet"))
				{
					featureSet = new SPatterNetFeatureSet(featureGen.getAspatialFeatures(), featureGen.getSpatialFeatures());
				}
				else if (featureSetType.equals("Legacy"))
				{
					featureSet = new LegacyFeatureSet(featureGen.getAspatialFeatures(), featureGen.getSpatialFeatures());
				}
				else if (featureSetType.equals("Naive"))
				{
					featureSet = new NaiveFeatureSet(featureGen.getAspatialFeatures(), featureGen.getSpatialFeatures());
				}
				else if (featureSetType.equals("JITSPatterNet"))
				{
					featureSet = JITSPatterNetFeatureSet.construct(featureGen.getAspatialFeatures(), featureGen.getSpatialFeatures());
				}
				else
				{
					throw new IllegalArgumentException("Cannot recognise --feature-set-type: " + featureSetType);
				}
				
				final int[] supportedPlayers = new int[game.players().count()];
				for (int i = 0; i < supportedPlayers.length; ++i)
				{
					supportedPlayers[i] = i + 1;
				}
				
				featureSet.init(game, supportedPlayers, null);
				playoutMoveSelector = 
						new FeaturesSoftmaxMoveSelector
						(
							new BaseFeatureSet[]{featureSet}, 
							new WeightVector[]{new WeightVector(new FVector(featureSet.getNumFeatures()))},
							false
						);
			}
			else if (featuresToUse.startsWith("latest-trained-uniform-"))
			{
				// We'll take the latest trained weights from specified directory, but
				// ignore weights (i.e. continue running uniformly)
				String trainedDirPath = featuresToUse.substring("latest-trained-uniform-".length());
				if (!trainedDirPath.endsWith("/"))
					trainedDirPath += "/";
				final File trainedDir = new File(trainedDirPath);
				
				int lastCheckpoint = -1;
				for (final File file : trainedDir.listFiles())
				{
					if (!file.isDirectory())
					{
						if (file.getName().startsWith("FeatureSet_P") && file.getName().endsWith(".fs"))
						{
							final int checkpoint = 
									Integer.parseInt
									(
										file
										.getName()
										.split(Pattern.quote("_"))[2]
										.replaceFirst(Pattern.quote(".fs"), "")
									);
							
							if (checkpoint > lastCheckpoint)
								lastCheckpoint = checkpoint;
						}
					}
				}
				
				final BaseFeatureSet[] playerFeatureSets = new BaseFeatureSet[game.players().count() + 1];
				for (int p = 1; p < playerFeatureSets.length; ++p)
				{
					final BaseFeatureSet featureSet;
					
					if (featureSetType.equals("SPatterNet"))
					{
						featureSet = 
								new SPatterNetFeatureSet
								(
									trainedDirPath + 
									String.format
									(
										"%s_%05d.%s", 
										"FeatureSet_P" + p, 
										Integer.valueOf(lastCheckpoint), 
										"fs"
									)
								);
					}
					else if (featureSetType.equals("Legacy"))
					{
						featureSet = 
								new LegacyFeatureSet
								(
									trainedDirPath + 
									String.format
									(
										"%s_%05d.%s", 
										"FeatureSet_P" + p, 
										Integer.valueOf(lastCheckpoint), 
										"fs"
									)
								);
					}
					else if (featureSetType.equals("Naive"))
					{
						featureSet = 
								new NaiveFeatureSet
								(
									trainedDirPath + 
									String.format
									(
										"%s_%05d.%s", 
										"FeatureSet_P" + p, 
										Integer.valueOf(lastCheckpoint), 
										"fs"
									)
								);
					}
					else if (featureSetType.equals("JITSPatterNet"))
					{
						featureSet = 
								JITSPatterNetFeatureSet.construct
								(
									trainedDirPath + 
									String.format
									(
										"%s_%05d.%s", 
										"FeatureSet_P" + p, 
										Integer.valueOf(lastCheckpoint), 
										"fs"
									)
								);
					}
					else
					{
						throw new IllegalArgumentException("Cannot recognise --feature-set-type: " + featureSetType);
					}
					
					playerFeatureSets[p] = featureSet;
				}
				
				final WeightVector[] weightVectors = new WeightVector[playerFeatureSets.length];
				for (int p = 1; p < playerFeatureSets.length; ++p)
				{
					playerFeatureSets[p].init(game, new int[]{p}, null);
					weightVectors[p] = new WeightVector(new FVector(playerFeatureSets[p].getNumFeatures()));
				}
				
				playoutMoveSelector = 
						new FeaturesSoftmaxMoveSelector
						(
							playerFeatureSets, 
							weightVectors,
							false
						);
			}
			else if (featuresToUse.startsWith("latest-trained-"))
			{
				// We'll take the latest trained weights from specified directory, 
				// including weights (i.e. not playing uniformly random)
				String trainedDirPath = featuresToUse.substring("latest-trained-".length());
				if (!trainedDirPath.endsWith("/"))
					trainedDirPath += "/";
				final File trainedDir = new File(trainedDirPath);
				
				int lastCheckpoint = -1;
				for (final File file : trainedDir.listFiles())
				{
					if (!file.isDirectory())
					{
						if (file.getName().startsWith("FeatureSet_P") && file.getName().endsWith(".fs"))
						{
							final int checkpoint = 
									Integer.parseInt
									(
										file
										.getName()
										.split(Pattern.quote("_"))[2]
										.replaceFirst(Pattern.quote(".fs"), "")
									);
							
							if (checkpoint > lastCheckpoint)
								lastCheckpoint = checkpoint;
						}
					}
				}
				
				final BaseFeatureSet[] playerFeatureSets = new BaseFeatureSet[game.players().count() + 1];
				for (int p = 1; p < playerFeatureSets.length; ++p)
				{
					final BaseFeatureSet featureSet;
					
					if (featureSetType.equals("SPatterNet"))
					{
						featureSet = 
								new SPatterNetFeatureSet
								(
									trainedDirPath + 
									String.format
									(
										"%s_%05d.%s", 
										"FeatureSet_P" + p, 
										Integer.valueOf(lastCheckpoint), 
										"fs"
									)
								);
					}
					else if (featureSetType.equals("Legacy"))
					{
						featureSet = 
								new LegacyFeatureSet
								(
									trainedDirPath + 
									String.format
									(
										"%s_%05d.%s", 
										"FeatureSet_P" + p, 
										Integer.valueOf(lastCheckpoint), 
										"fs"
									)
								);
					}
					else if (featureSetType.equals("Naive"))
					{
						featureSet = 
								new NaiveFeatureSet
								(
									trainedDirPath + 
									String.format
									(
										"%s_%05d.%s", 
										"FeatureSet_P" + p, 
										Integer.valueOf(lastCheckpoint), 
										"fs"
									)
								);
					}
					else if (featureSetType.equals("JITSPatterNet"))
					{
						featureSet = 
								JITSPatterNetFeatureSet.construct
								(
									trainedDirPath + 
									String.format
									(
										"%s_%05d.%s", 
										"FeatureSet_P" + p, 
										Integer.valueOf(lastCheckpoint), 
										"fs"
									)
								);
					}
					else
					{
						throw new IllegalArgumentException("Cannot recognise --feature-set-type: " + featureSetType);
					}
					
					playerFeatureSets[p] = featureSet;
				}
				
				final WeightVector[] weightVectors = new WeightVector[playerFeatureSets.length];
				for (int p = 1; p < playerFeatureSets.length; ++p)
				{
					playerFeatureSets[p].init(game, new int[]{p}, null);	// Still null since we won't do thresholding
					
					final LinearFunction linearFunc = 
							LinearFunction.fromFile
							(
								trainedDirPath +
								String.format
								(
									"%s_%05d.%s", 
									"PolicyWeightsCE_P" + p, 
									Integer.valueOf(lastCheckpoint), 
									"txt"
								)
							);
					weightVectors[p] = linearFunc.effectiveParams();
				}
				
				playoutMoveSelector = 
						new FeaturesSoftmaxMoveSelector
						(
							playerFeatureSets, 
							weightVectors,
							false
						);
			}
			else
			{
				throw new IllegalArgumentException("Cannot understand --features-to-use: " + featuresToUse);
			}
		}
		else
		{
			playoutMoveSelector = null;
		}
		
		return playoutMoveSelector;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Main metohd
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(final String[] args)
	{		
		// define options for arg parser
		final CommandLineArgParse argParse = 
				new CommandLineArgParse
				(
					true,
					"Measure playouts per second for one or more games."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--warming-up-secs", "--warming-up")
				.help("Number of seconds of warming up (per game).")
				.withDefault(Integer.valueOf(10))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--measure-secs")
				.help("Number of seconds over which we measure playouts (per game).")
				.withDefault(Integer.valueOf(30))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--playout-action-cap")
				.help("Maximum number of actions to execute per playout (-1 for no cap).")
				.withDefault(Integer.valueOf(-1))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--seed")
				.help("Seed to use for RNG. Default (-1) just uses ThreadLocalRandom.current().")
				.withDefault(Integer.valueOf(-1))
				.withNumVals(1)
				.withType(OptionTypes.Int));
		argParse.addOption(new ArgOption()
				.withNames("--game-names")
				.help("Only games that include at least one of the provided strings in their name are included.")
				.withDefault(Arrays.asList(""))
				.withNumVals("+")
				.withType(OptionTypes.String));
		argParse.addOption(new ArgOption()
				.withNames("--ruleset")
				.help("Ruleset to compile. Will assume the ruleset name to be valid for ALL games run.")
				.withDefault("")
				.withNumVals(1)
				.withType(OptionTypes.String));
		argParse.addOption(new ArgOption()
				.withNames("--export-csv")
				.help("Filename (or filepath) to write results to. By default writes to ./results.csv")
				.withDefault("results.csv")
				.withNumVals(1)
				.withType(OptionTypes.String));
		argParse.addOption(new ArgOption()
				.withNames("--suppress-prints")
				.help("Use this to suppress standard out print messages (will still write CSV at the end).")
				.withNumVals(0)
				.withType(OptionTypes.Boolean));
		argParse.addOption(new ArgOption()
				.withNames("--no-custom-playouts")
				.help("Use this to disable custom (optimised) playout strategies on any games played.")
				.withNumVals(0)
				.withType(OptionTypes.Boolean));
		
		argParse.addOption(new ArgOption()
				.withNames("--features-to-use")
				.help("Features to use (no features are used by default)")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.withDefault(""));
		argParse.addOption(new ArgOption()
				.withNames("--feature-set-type")
				.help("Type of featureset to use (SPatterNet by default, ignored if --features-to-use left blank or if using features from metadata)")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.withDefault("SPatterNet")
				.withLegalVals("SPatterNet", "Legacy", "Naive", "JITSPatterNet"));
		
		// Parse the args
		if (!argParse.parseArguments(args))
			return;
		
		// use the parsed args
		final PlayoutsPerSec experiment = new PlayoutsPerSec();
		
		experiment.warmingUpSecs = argParse.getValueInt("--warming-up-secs");
		experiment.measureSecs = argParse.getValueInt("--measure-secs");
		experiment.playoutActionCap = argParse.getValueInt("--playout-action-cap");
		experiment.seed = argParse.getValueInt("--seed");
		experiment.gameNames = (List<String>) argParse.getValue("--game-names");
		experiment.ruleset = argParse.getValueString("--ruleset");
		experiment.exportCSV = argParse.getValueString("--export-csv");
		experiment.suppressPrints = argParse.getValueBool("--suppress-prints");
		experiment.noCustomPlayouts = argParse.getValueBool("--no-custom-playouts");
		
		experiment.featuresToUse = argParse.getValueString("--features-to-use");
		experiment.featureSetType = argParse.getValueString("--feature-set-type");

		experiment.startExperiment();
	}

}
