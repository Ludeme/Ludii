package supplementary.experiments.feature_sets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import features.feature_sets.BaseFeatureSet;
import features.feature_sets.BaseFeatureSet.FeatureSetImplementations;
import features.feature_sets.BaseFeatureSet.MoveFeaturesKey;
import features.feature_sets.BaseFeatureSet.ProactiveFeaturesKey;
import features.feature_sets.BaseFeatureSet.ReactiveFeaturesKey;
import features.feature_sets.network.JITSPatterNetFeatureSet;
import features.feature_sets.network.SPatterNet;
import features.feature_sets.network.SPatterNetFeatureSet;
import function_approx.LinearFunction;
import game.Game;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.StringRoutines;
import main.UnixPrintWriter;
import other.AI;
import other.GameLoader;
import other.context.Context;
import other.trial.Trial;
import policies.softmax.SoftmaxPolicyLinear;
import search.mcts.MCTS;
import search.mcts.MCTS.QInit;
import search.mcts.backpropagation.MonteCarloBackprop;
import search.mcts.finalmoveselection.RobustChild;
import search.mcts.selection.AG0Selection;
import utils.ExperimentFileUtils;

/**
 * Script to check memory usage of different feature set implementations
 * 
 * @author Dennis Soemers
 */
public class MemoryUsage
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
	private static void evalMemoryUsage(final CommandLineArgParse parsedArgs)
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
					"spatternet_num_keys_proactive",
					"spatternet_num_keys_reactive",
					"spatternet_num_props_proactive",
					"spatternet_num_props_reactive",
					"jit_num_keys_proactive",
					"jit_num_keys_reactive",
					"jit_num_props_proactive",
					"jit_num_props_reactive",
					"keys_ratio",
					"keys_ratio_proactive",
					"keys_ratio_reactive",
					"props_ratio",
					"props_ratio_proactive",
					"props_ratio_reactive"
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
				
				final Set<ReactiveFeaturesKey> spatternetReactiveKeys = new HashSet<ReactiveFeaturesKey>();
				final Set<ProactiveFeaturesKey> spatternetProactiveKeys = new HashSet<ProactiveFeaturesKey>();
				final Set<ReactiveFeaturesKey> jitSpatternetReactiveKeys = new HashSet<ReactiveFeaturesKey>();
				final Set<ProactiveFeaturesKey> jitSpatternetProactiveKeys = new HashSet<ProactiveFeaturesKey>();
				
				long spatternetNumPropsProactive = 0L;
				long spatternetNumPropsReactive = 0L;
				long jitNumPropsProactive = 0L;
				long jitNumPropsReactive = 0L;
				
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
					System.out.println("Implementation: " + impl);
					for (int p = 1; p <= numPlayers; ++p)
					{
						final String parentDir = new File(policyWeightFilepathsPerPlayer[p]).getParent();
						final String featureSetFilepath = parentDir + File.separator + linFuncs[p].featureSetFile();
						
						if (impl == FeatureSetImplementations.SPATTERNET)
							featureSets[p] = new SPatterNetFeatureSet(featureSetFilepath);
						else if (impl == FeatureSetImplementations.JITSPATTERNET)
							featureSets[p] = JITSPatterNetFeatureSet.construct(featureSetFilepath);
					}
					
					final SoftmaxPolicyLinear softmax = new SoftmaxPolicyLinear(linFuncs, featureSets);
					final List<AI> ais = new ArrayList<AI>();
					ais.add(null);
					
					for (int p = 1; p <= numPlayers; ++p)
					{
						final MCTS mcts = 
								new MCTS
								(
									new AG0Selection(),
									softmax,
									new MonteCarloBackprop(),
									new RobustChild()
								);
						mcts.setLearnedSelectionPolicy(softmax);
						mcts.setQInit(QInit.WIN);
						
						ais.add(mcts);
					}
					
					// Play for 60 move ~= 60 seconds
					final Trial trial = new Trial(game);
					final Context context = new Context(game, trial);
					boolean needStart = true;
					
					for (int i = 0; i < 60; ++i)
					{
						if (needStart)
						{
							needStart = false;
							game.start(context);
							
							final long startTime = System.currentTimeMillis();
							for (int p = 1; p <= numPlayers; ++p)
							{
								ais.get(p).initAI(game, p);
							}
							final long endTime = System.currentTimeMillis();
							
							System.out.println("init for " + numPlayers + " players took " + ((endTime - startTime) / 1000.0) + " seconds.");
						}
						
						context.model().startNewStep(context, ais, 1.0);
						
						if (trial.over())
							needStart = true;
					}
					
					// Check memory usage
					if (impl == FeatureSetImplementations.SPATTERNET)
					{
						for (int p = 1; p <= numPlayers; ++p)
						{
							System.out.println("p = " + p);
							final SPatterNetFeatureSet featureSet = (SPatterNetFeatureSet) featureSets[p];
							final HashMap<ReactiveFeaturesKey, SPatterNet> reactiveSPatterNets = featureSet.reactiveFeaturesThresholded();
							final HashMap<ProactiveFeaturesKey, SPatterNet> proactiveSPatterNets = featureSet.proactiveFeaturesThresholded();
							
							spatternetReactiveKeys.addAll(reactiveSPatterNets.keySet());
							spatternetProactiveKeys.addAll(proactiveSPatterNets.keySet());
							
							System.out.println("Num reactive keys = " + reactiveSPatterNets.keySet().size());
							System.out.println("Num proactive keys = " + proactiveSPatterNets.keySet().size());
							
							for (final Entry<ReactiveFeaturesKey, SPatterNet> reactiveEntry : reactiveSPatterNets.entrySet())
							{
								spatternetNumPropsReactive += reactiveEntry.getValue().numPropositions();
							}
							
							for (final Entry<ProactiveFeaturesKey, SPatterNet> proactiveEntry : proactiveSPatterNets.entrySet())
							{
								spatternetNumPropsProactive += proactiveEntry.getValue().numPropositions();
							}
						}
					}
					else if (impl == FeatureSetImplementations.JITSPATTERNET)
					{
						for (int p = 1; p <= numPlayers; ++p)
						{
							System.out.println("p = " + p);
							final JITSPatterNetFeatureSet featureSet = (JITSPatterNetFeatureSet) featureSets[p];
							final Map<MoveFeaturesKey, SPatterNet> spatterNets = featureSet.spatterNetMapThresholded();
							
							for (final MoveFeaturesKey key : spatterNets.keySet())
							{
								if (key instanceof ReactiveFeaturesKey)
								{
									if (jitSpatternetReactiveKeys.add((ReactiveFeaturesKey)key))
										jitNumPropsReactive += spatterNets.get(key).numPropositions();
								}
								else
								{
									if (jitSpatternetProactiveKeys.add((ProactiveFeaturesKey)key))
										jitNumPropsProactive += spatterNets.get(key).numPropositions();
									
	//								if (!spatternetProactiveKeys.contains(key))
	//									System.out.println("num props = " + spatterNets.get(key).numPropositions());
								}
							}
							
							for (final MoveFeaturesKey key : featureSet.spatterNetMap().keySet())
							{
								if (key instanceof ReactiveFeaturesKey)
								{
									if (jitSpatternetReactiveKeys.add((ReactiveFeaturesKey)key))
										jitNumPropsReactive += featureSet.spatterNetMap().get(key).numPropositions();
								}
								else
								{
									if (jitSpatternetProactiveKeys.add((ProactiveFeaturesKey)key))
										jitNumPropsProactive += featureSet.spatterNetMap().get(key).numPropositions();
									
	//								if (!spatternetProactiveKeys.contains(key))
	//									System.out.println("num props = " + featureSet.spatterNetMap().get(key).numPropositions());
								}
							}
							
							System.out.println("Num thresholded keys = " + spatterNets.keySet().size());
							System.out.println("Num non-thresholded keys = " + featureSet.spatterNetMap().keySet().size());
						}
					}
					
					System.out.println();
				}
				
	//			for (final ReactiveFeaturesKey key : jitSpatternetReactiveKeys)
	//			{
	//				if (!spatternetReactiveKeys.contains(key))
	//					System.out.println("JIT-only reactive key: " + key);
	//			}
	//			
	//			for (final ProactiveFeaturesKey key : jitSpatternetProactiveKeys)
	//			{
	//				if (!spatternetProactiveKeys.contains(key))
	//					System.out.println("JIT-only proactive key: " + key);
	//			}
				
				System.out.println();
				
				
	//			StringRoutines.join
	//				(
	//					",", 
	//					"game",
	//					"spatternet_num_keys_proactive",
	//					"spatternet_num_keys_reactive",
	//					"spatternet_num_props_proactive",
	//					"spatternet_num_props_reactive",
	//					"jit_num_keys_proactive",
	//					"jit_num_keys_reactive",
	//					"jit_num_props_proactive",
	//					"jit_num_props_reactive",
	//					"keys_ratio",
	//					"keys_ratio_proactive",
	//					"keys_ratio_reactive"
	//				)
				
				final List<String> stringsToWrite = new ArrayList<String>();
				stringsToWrite.add(StringRoutines.quote(gameName));
				stringsToWrite.add(String.valueOf(spatternetProactiveKeys.size()));
				stringsToWrite.add(String.valueOf(spatternetReactiveKeys.size()));
				stringsToWrite.add(String.valueOf(spatternetNumPropsProactive));
				stringsToWrite.add(String.valueOf(spatternetNumPropsReactive));
				stringsToWrite.add(String.valueOf(jitSpatternetProactiveKeys.size()));
				stringsToWrite.add(String.valueOf(jitSpatternetReactiveKeys.size()));
				stringsToWrite.add(String.valueOf(jitNumPropsProactive));
				stringsToWrite.add(String.valueOf(jitNumPropsReactive));
				stringsToWrite.add(String.valueOf(((double) spatternetProactiveKeys.size() + spatternetReactiveKeys.size()) / (jitSpatternetProactiveKeys.size() + jitSpatternetReactiveKeys.size())));
				stringsToWrite.add(String.valueOf(((double) spatternetProactiveKeys.size()) / (jitSpatternetProactiveKeys.size())));
				stringsToWrite.add(String.valueOf(((double) spatternetReactiveKeys.size()) / (jitSpatternetReactiveKeys.size())));
				stringsToWrite.add(String.valueOf(((double) spatternetNumPropsProactive + spatternetNumPropsReactive) / (jitNumPropsProactive + jitNumPropsReactive)));
				stringsToWrite.add(String.valueOf(((double) spatternetNumPropsProactive) / (jitNumPropsProactive)));
				stringsToWrite.add(String.valueOf(((double) spatternetNumPropsReactive) / (jitNumPropsReactive)));
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
					"Eval memory usage of feature sets."
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
		
		evalMemoryUsage(argParse);
	}

}
