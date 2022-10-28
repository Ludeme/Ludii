package supplementary.experiments.feature_sets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import features.feature_sets.BaseFeatureSet;
import features.feature_sets.BaseFeatureSet.FeatureSetImplementations;
import features.feature_sets.network.JITSPatterNetFeatureSet;
import features.feature_sets.network.SPatterNetFeatureSet;
import function_approx.LinearFunction;
import game.Game;
import gnu.trove.list.array.TLongArrayList;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.StringRoutines;
import main.UnixPrintWriter;
import main.math.statistics.Stats;
import other.GameLoader;
import utils.ExperimentFileUtils;

/**
 * Script to check SPatterNet instantiation time
 * 
 * @author Dennis Soemers
 */
public class InstantiationTime
{
	
	//-------------------------------------------------------------------------
	
	/** Games we have featuresets for */
	private static final String[] GAMES = 
			new String[]
			{
				"Alquerque.lud",
				"Amazons.lud",
				"ArdRi.lud",
				"Arimaa.lud",
				"Ataxx.lud",
				"Bao Ki Arabu (Zanzibar 1).lud",
				"Bizingo.lud",
				"Breakthrough.lud",
				"Chess.lud",
				"Chinese Checkers.lud",
				"English Draughts.lud",
				"Fanorona.lud",
				"Fox and Geese.lud",
				"Go.lud",
				"Gomoku.lud",
				"Gonnect.lud",
				"Havannah.lud",
				"Hex.lud",
				"Kensington.lud",
				"Knightthrough.lud",
				"Konane.lud",
				"Level Chess.lud",
				"Lines of Action.lud",
				"Pentalath.lud",
				"Pretwa.lud",
				"Reversi.lud",
				"Royal Game of Ur.lud",
				"Surakarta.lud",
				"Shobu.lud",
				"Tablut.lud",
				"Triad.lud",
				"XII Scripta.lud",
				"Yavalath.lud"
			};
	
	//-------------------------------------------------------------------------
	
	/**
	 * Eval the memory usage of feature sets
	 * @param parsedArgs
	 */
	private static void evalInitTimes(final CommandLineArgParse parsedArgs)
	{
		String trainingOutDir = parsedArgs.getValueString("--training-out-dir");
		if (!trainingOutDir.endsWith("/"))
			trainingOutDir += "/";
		
		try (final PrintWriter writer = new UnixPrintWriter(new File(parsedArgs.getValueString("--out-file")), "UTF-8"))
		{
			// Write header
			writer.println
			(
				StringRoutines.join
				(
					",", 
					"game",
					"spatternet_mean_init",
					"spatternet_std_init",
					"spatternet_n_init",
					"jit_mean_init",
					"jit_std_init",
					"jit_n_init"
				)
			);
			
			for (final String gameName : GAMES)
			{
				System.out.println("Game: " + gameName);
				final Game game = GameLoader.loadGameFromName(gameName);
				final int numPlayers = game.players().count();
				
				final String cleanGameName = StringRoutines.cleanGameName(gameName.replaceAll(Pattern.quote(".lud"), ""));
				//final File gameTrainingDir = new File(trainingOutDir + cleanGameName + "/");
				
				final String[] policyWeightFilepathsPerPlayer = new String[numPlayers + 1];
				for (int p = 1; p <= numPlayers; ++p)
				{
					String policyWeightsFilepath = trainingOutDir + cleanGameName + "/PolicyWeightsCE_P" + p +  "_00201.txt";
					
					if (!new File(policyWeightsFilepath).exists())
					{
						final String parentDir = new File(policyWeightsFilepath).getParent();
						
						// Replace with whatever is the latest file we have
						if (policyWeightsFilepath.contains("Selection"))
						{
							policyWeightsFilepath = 
									ExperimentFileUtils.getLastFilepath(parentDir + "/PolicyWeightsSelection_P" + p, "txt");
						}
						else if (policyWeightsFilepath.contains("Playout"))
						{
							policyWeightsFilepath = 
									ExperimentFileUtils.getLastFilepath(parentDir + "/PolicyWeightsPlayout_P" + p, "txt");
						}
						else if (policyWeightsFilepath.contains("TSPG"))
						{
							policyWeightsFilepath = 
									ExperimentFileUtils.getLastFilepath(parentDir + "/PolicyWeightsTSPG_P" + p, "txt");
						}
						else if (policyWeightsFilepath.contains("PolicyWeightsCE"))
						{
							policyWeightsFilepath = 
									ExperimentFileUtils.getLastFilepath(parentDir + "/PolicyWeightsCE_P" + p, "txt");
						}
						else
						{
							policyWeightsFilepath = null;
						}
					}
	
					if (policyWeightsFilepath == null)
						System.err.println("Cannot resolve policy weights filepath: " + trainingOutDir + cleanGameName + "/PolicyWeightsCE_P" + p +  "_00201.txt");
					
					policyWeightFilepathsPerPlayer[p] = policyWeightsFilepath;
				}
				
				final LinearFunction[] linFuncs = new LinearFunction[numPlayers + 1];
				for (int p = 1; p <= numPlayers; ++p)
				{
					linFuncs[p] = LinearFunction.fromFile(policyWeightFilepathsPerPlayer[p]);
				}
				
				final BaseFeatureSet[] featureSets = new BaseFeatureSet[numPlayers + 1];
				
				final TLongArrayList startTimesSpatternet = new TLongArrayList();
				final TLongArrayList endTimesSpatternet = new TLongArrayList();
				final TLongArrayList startTimesJIT = new TLongArrayList();
				final TLongArrayList endTimesJIT = new TLongArrayList();
				
				for 
				(
					final FeatureSetImplementations impl : 
						new FeatureSetImplementations[] 
								{
									FeatureSetImplementations.SPATTERNET, 
									FeatureSetImplementations.JITSPATTERNET
								}
				)
				{
					//System.out.println("Implementation: " + impl);
					for (int p = 1; p <= numPlayers; ++p)
					{
						final String parentDir = new File(policyWeightFilepathsPerPlayer[p]).getParent();
						final String featureSetFilepath = parentDir + File.separator + linFuncs[p].featureSetFile();
						
						if (impl == FeatureSetImplementations.SPATTERNET)
							featureSets[p] = new SPatterNetFeatureSet(featureSetFilepath);
						else if (impl == FeatureSetImplementations.JITSPATTERNET)
							featureSets[p] = JITSPatterNetFeatureSet.construct(featureSetFilepath);
					}
					
					// Warming up
					final long warmingUpStartTime = System.currentTimeMillis();
					long timeSpent = 0L;
					
					while (timeSpent < 30000L)
					{
						for (int p = 1; p <= numPlayers; ++p)
						{
							featureSets[p].gameRef().clear();	// Force reinit
							featureSets[p].init(game, new int[] {p}, null);
							timeSpent = System.currentTimeMillis() - warmingUpStartTime;
							
							if (timeSpent >= 30000L)
								break;
						}
					}
					
					// Start actually measuring
					final long timingStartTime = System.currentTimeMillis();
					
					while (System.currentTimeMillis() - timingStartTime < 5 * 60 * 1000L)		// 5 minutes
					{
						if (impl == FeatureSetImplementations.SPATTERNET)
						{
							startTimesSpatternet.add(System.currentTimeMillis());
							
							for (int p = 1; p <= numPlayers; ++p)
							{
								featureSets[p].gameRef().clear();	// Force reinit
								featureSets[p].init(game, new int[] {p}, null);
							}
							
							endTimesSpatternet.add(System.currentTimeMillis());
							//System.out.println(endTimesSpatternet.getQuick(endTimesSpatternet.size() - 1) - startTimesSpatternet.getQuick(startTimesSpatternet.size() - 1));
						}
						else if (impl == FeatureSetImplementations.JITSPATTERNET)
						{
							startTimesJIT.add(System.currentTimeMillis());
							
							for (int p = 1; p <= numPlayers; ++p)
							{
								featureSets[p].gameRef().clear();	// Force reinit
								featureSets[p].init(game, new int[] {p}, null);
							}
							
							endTimesJIT.add(System.currentTimeMillis());
						}
					}
				}
				
				final Stats spatternetStats = new Stats();
				for (int i = 0; i < startTimesSpatternet.size(); ++i)
				{
					spatternetStats.addSample(endTimesSpatternet.getQuick(i) - startTimesSpatternet.getQuick(i));
				}
				spatternetStats.measure();
				
				final Stats jitStats = new Stats();
				for (int i = 0; i < startTimesJIT.size(); ++i)
				{
					jitStats.addSample(endTimesJIT.getQuick(i) - startTimesJIT.getQuick(i));
				}
				jitStats.measure();

				final List<String> stringsToWrite = new ArrayList<String>();
				stringsToWrite.add(StringRoutines.quote(gameName));
				stringsToWrite.add(String.valueOf(spatternetStats.mean()));
				stringsToWrite.add(String.valueOf(spatternetStats.sd()));
				stringsToWrite.add(String.valueOf(spatternetStats.n()));
				stringsToWrite.add(String.valueOf(jitStats.mean()));
				stringsToWrite.add(String.valueOf(jitStats.sd()));
				stringsToWrite.add(String.valueOf(jitStats.n()));
				writer.println(StringRoutines.join(",", stringsToWrite));
			}
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Main method
	 * @param args
	 */
	public static void main(final String[] args)
	{
		// Define options for arg parser
		final CommandLineArgParse argParse = 
				new CommandLineArgParse
				(
					true,
					"Eval init times of feature sets."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--training-out-dir")
				.help("Output directory for training results.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		argParse.addOption(new ArgOption()
				.withNames("--out-file")
				.help("Filepath to write our output CSV to.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		// Parse the args
		if (!argParse.parseArguments(args))
			return;
		
		evalInitTimes(argParse);
	}

}
