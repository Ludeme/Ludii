package supplementary.experiments.scripts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import game.Game;
import game.equipment.other.Regions;
import gnu.trove.list.array.TIntArrayList;
import main.CommandLineArgParse;
import main.CommandLineArgParse.ArgOption;
import main.CommandLineArgParse.OptionTypes;
import main.FileHandling;
import main.StringRoutines;
import main.UnixPrintWriter;
import main.collections.ListUtils;
import main.options.Ruleset;
import metadata.ai.heuristics.Heuristics;
import metadata.ai.heuristics.terms.CentreProximity;
import metadata.ai.heuristics.terms.ComponentValues;
import metadata.ai.heuristics.terms.CornerProximity;
import metadata.ai.heuristics.terms.HeuristicTerm;
import metadata.ai.heuristics.terms.Influence;
import metadata.ai.heuristics.terms.InfluenceAdvanced;
import metadata.ai.heuristics.terms.LineCompletionHeuristic;
import metadata.ai.heuristics.terms.Material;
import metadata.ai.heuristics.terms.MobilityAdvanced;
import metadata.ai.heuristics.terms.MobilitySimple;
import metadata.ai.heuristics.terms.NullHeuristic;
import metadata.ai.heuristics.terms.OwnRegionsCount;
import metadata.ai.heuristics.terms.PlayerRegionsProximity;
import metadata.ai.heuristics.terms.PlayerSiteMapCount;
import metadata.ai.heuristics.terms.RegionProximity;
import metadata.ai.heuristics.terms.Score;
import metadata.ai.heuristics.terms.SidesProximity;
import metadata.ai.heuristics.terms.UnthreatenedMaterial;
import other.GameLoader;
import search.minimax.AlphaBetaSearch;

/**
 * Method to generate cluster job scripts for finding the best
 * starting heuristics.
 * 
 * We consider every possible starting heuristic individually, always
 * with a weight of +1.0 or -1.0. Heuristics with multiple weights
 * (for instance for different piece types) have the same weights
 * for all types.
 * 
 * Heuristics evaluated by winning percentage against all other potential
 * starting heuristics.
 *
 * @author Dennis Soemers
 */
public class FindBestStartingHeuristicsScriptsGen
{
	/** Memory to assign per CPU (and to JVM), in MB */
	private static final String MEM_PER_CPU = "4096";
	
	/** Max wall time (in minutes) */
	private static final int MAX_WALL_TIME = 3000;
	
	/** Don't submit more than this number of jobs at a single time */
	private static final int MAX_JOBS_PER_BATCH = 800;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor (don't need this)
	 */
	private FindBestStartingHeuristicsScriptsGen()
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Generates our scripts
	 * @param argParse
	 */
	private static void generateScripts(final CommandLineArgParse argParse)
	{
		final List<String> jobScriptNames = new ArrayList<String>();
		final AlphaBetaSearch dummyAlphaBeta = new AlphaBetaSearch();
		
		String scriptsDir = argParse.getValueString("--scripts-dir");
		scriptsDir = scriptsDir.replaceAll(Pattern.quote("\\"), "/");
		if (!scriptsDir.endsWith("/"))
			scriptsDir += "/";
		
		final String[] allGameNames = Arrays.stream(FileHandling.listGames()).filter(s -> (
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/bad/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wip/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/WishlistDLP/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/test/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wishlist/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/reconstruction/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/simulation/")) &&
				!(s.replaceAll(Pattern.quote("\\"), "/").contains("/lud/proprietary/"))
			)).toArray(String[]::new);
		
		// Generate eval scripts
		final String userName = argParse.getValueString("--user-name");
		
		for (final String fullGamePath : allGameNames)
		{
			final String[] gamePathParts = fullGamePath.replaceAll(Pattern.quote("\\"), "/").split(Pattern.quote("/"));
			final String gameName = gamePathParts[gamePathParts.length - 1].replaceAll(Pattern.quote(".lud"), "");
			final Game gameNoRuleset = GameLoader.loadGameFromName(gameName + ".lud");
			final List<Ruleset> gameRulesets = new ArrayList<Ruleset>(gameNoRuleset.description().rulesets());
			gameRulesets.add(null);
			boolean foundRealRuleset = false;
			
			for (final Ruleset ruleset : gameRulesets)
			{
				final Game game;
				String fullRulesetName = "";
				if (ruleset == null && foundRealRuleset)
				{
					// Skip this, don't allow game without ruleset if we do have real implemented ones
					continue;
				}
				else if (ruleset != null && !ruleset.optionSettings().isEmpty())
				{
					fullRulesetName = ruleset.heading();
					foundRealRuleset = true;
					game = GameLoader.loadGameFromName(gameName + ".lud", fullRulesetName);
				}
				else if (ruleset != null && ruleset.optionSettings().isEmpty())
				{
					// Skip empty ruleset
					continue;
				}
				else
				{
					game = gameNoRuleset;
				}
				
				if (game.isDeductionPuzzle())
					continue;
				
				if (game.isSimulationMoveGame())
					continue;
				
				if (!game.isAlternatingMoveGame())
					continue;
				
				if (game.hasSubgames())
					continue;
				
				if (!dummyAlphaBeta.supportsGame(game))
					continue;
				
				final String filepathsGameName = StringRoutines.cleanGameName(gameName);
				final String filepathsRulesetName = StringRoutines.cleanRulesetName(fullRulesetName.replaceAll(Pattern.quote("Ruleset/"), ""));
				
				final List<String> relevantHeuristics = new ArrayList<String>();
				
				if (CentreProximity.isSensibleForGame(game))
					relevantHeuristics.addAll(heuristicFilepaths("CentreProximity", game, argParse));
				
				if (ComponentValues.isSensibleForGame(game))
					relevantHeuristics.addAll(heuristicFilepaths("ComponentValues", game, argParse));
	
				if (CornerProximity.isSensibleForGame(game))
					relevantHeuristics.addAll(heuristicFilepaths("CornerProximity", game, argParse));
	
				if (LineCompletionHeuristic.isSensibleForGame(game))
					relevantHeuristics.addAll(heuristicFilepaths("LineCompletionHeuristic", game, argParse));
	
				if (Material.isSensibleForGame(game))
					relevantHeuristics.addAll(heuristicFilepaths("Material", game, argParse));
				
				if (UnthreatenedMaterial.isSensibleForGame(game))
					relevantHeuristics.addAll(heuristicFilepaths("UnthreatenedMaterial", game, argParse));
	
				if (MobilitySimple.isSensibleForGame(game))
					relevantHeuristics.addAll(heuristicFilepaths("MobilitySimple", game, argParse));
				
				if (MobilityAdvanced.isSensibleForGame(game))
					relevantHeuristics.addAll(heuristicFilepaths("MobilityAdvanced", game, argParse));
				
				if (NullHeuristic.isSensibleForGame(game))
					relevantHeuristics.addAll(heuristicFilepaths("NullHeuristic", game, argParse));
				
				if (Influence.isSensibleForGame(game))
					relevantHeuristics.addAll(heuristicFilepaths("Influence", game, argParse));
				
				if (InfluenceAdvanced.isSensibleForGame(game))
					relevantHeuristics.addAll(heuristicFilepaths("InfluenceAdvanced", game, argParse));
	
				if (OwnRegionsCount.isSensibleForGame(game))
					relevantHeuristics.addAll(heuristicFilepaths("OwnRegionsCount", game, argParse));
	
				if (PlayerRegionsProximity.isSensibleForGame(game))
					relevantHeuristics.addAll(heuristicFilepaths("PlayerRegionsProximity", game, argParse));
	
				if (PlayerSiteMapCount.isSensibleForGame(game))
					relevantHeuristics.addAll(heuristicFilepaths("PlayerSiteMapCount", game, argParse));
	
				if (RegionProximity.isSensibleForGame(game))
					relevantHeuristics.addAll(heuristicFilepaths("RegionProximity", game, argParse));
	
				if (Score.isSensibleForGame(game))
					relevantHeuristics.addAll(heuristicFilepaths("Score", game, argParse));
	
				if (SidesProximity.isSensibleForGame(game))
					relevantHeuristics.addAll(heuristicFilepaths("SidesProximity", game, argParse));
				
				final int numPlayers = game.players().count();
				
				for (int evalHeuristicIdx = 0; evalHeuristicIdx < relevantHeuristics.size(); ++evalHeuristicIdx)
				{
					final String heuristicToEval = relevantHeuristics.get(evalHeuristicIdx);
					
					final TIntArrayList candidateOpponentIndices = new TIntArrayList(relevantHeuristics.size() - 1);
					for (int i = 0; i < relevantHeuristics.size(); ++i)
					{
						if (i != evalHeuristicIdx)
							candidateOpponentIndices.add(i);
					}
					
					final int numOpponents = numPlayers - 1;
					final List<TIntArrayList> opponentCombinations = new ArrayList<TIntArrayList>();
					
					final int opponentCombLength;
					
					if (candidateOpponentIndices.size() >= numOpponents)
						opponentCombLength = numOpponents;
					else
						opponentCombLength = candidateOpponentIndices.size();
					
					generateAllCombinations(candidateOpponentIndices, opponentCombLength, 0, new int[opponentCombLength], opponentCombinations);
					
					while (opponentCombinations.size() > 10)
					{
						// Too many combinations of opponents; we'll randomly remove some
						ListUtils.removeSwap(opponentCombinations, ThreadLocalRandom.current().nextInt(opponentCombinations.size()));
					}
					
					if (candidateOpponentIndices.size() < numOpponents)
					{
						// We don't have enough candidates to fill up all opponent slots;
						// we'll just fill up every combination with duplicates of its last entry
						for (final TIntArrayList combination : opponentCombinations)
						{
							while (combination.size() < numOpponents)
							{
								combination.add(combination.getQuick(combination.size() - 1));
							}
						}
					}
					
					final int numCombinations = opponentCombinations.size();
					int numGamesPerComb = 10;
					
					while (numCombinations * numGamesPerComb < 120)
					{
						numGamesPerComb += 10;
					}
					
					final String[] heuristicToEvalSplit = heuristicToEval.split(Pattern.quote("/"));
					String heuristicToEvalName = heuristicToEvalSplit[heuristicToEvalSplit.length - 1];
					heuristicToEvalName = heuristicToEvalName.substring(0, heuristicToEvalName.length() - ".txt".length());
					
					final String jobScriptFilename = "Eval" + filepathsGameName + filepathsRulesetName + heuristicToEvalName + ".sh";
					
					try (final PrintWriter writer = new UnixPrintWriter(new File(scriptsDir + jobScriptFilename), "UTF-8"))
					{
						writer.println("#!/usr/local_rwth/bin/zsh");
						writer.println("#SBATCH -J Eval" + filepathsGameName + filepathsRulesetName + heuristicToEvalName);
						writer.println("#SBATCH -o /work/" + userName + "/FindStartingHeuristic/Out"
								+ filepathsGameName + filepathsRulesetName + heuristicToEvalName + "_%J.out");
						writer.println("#SBATCH -e /work/" + userName + "/FindStartingHeuristic/Err"
								+ filepathsGameName + filepathsRulesetName + heuristicToEvalName + "_%J.err");
						writer.println("#SBATCH -t " + MAX_WALL_TIME);
						writer.println("#SBATCH --mem-per-cpu=" + MEM_PER_CPU);
						writer.println("#SBATCH -A " + argParse.getValueString("--project"));
						writer.println("unset JAVA_TOOL_OPTIONS");
						
						for (final TIntArrayList agentsCombination : opponentCombinations)
						{
							// Add the index of the heuristic to eval in front
							agentsCombination.insert(0, evalHeuristicIdx);
							
							assert (agentsCombination.size() == numPlayers);
							
							final String[] agentStrings = new String[numPlayers];
							String matchupStr = "";
							for (int i = 0; i < numPlayers; ++i)
							{
								final String heuristicFilepath = relevantHeuristics.get(agentsCombination.getQuick(i));
								
								final String[] heuristicSplit = heuristicFilepath.split(Pattern.quote("/"));
								String heuristicName = heuristicSplit[heuristicSplit.length - 1];
								heuristicName = heuristicName.substring(0, heuristicName.length() - ".txt".length());
								
								agentStrings[i] = StringRoutines.quote(StringRoutines.join
										(
											";", 
											"algorithm=HeuristicSampling",
											"heuristics=" + StringRoutines.quote(heuristicFilepath),
											"friendly_name=" + heuristicName
										));
								matchupStr += heuristicName;
							}
							
							final String javaCall = StringRoutines.join
									(
										" ", 
										"java",
										"-Xms" + MEM_PER_CPU + "M",
										"-Xmx" + MEM_PER_CPU + "M",
										"-XX:+HeapDumpOnOutOfMemoryError",
				                        "-da",
				                        "-dsa",
				                        "-XX:+UseStringDeduplication",
				                        "-jar",
				                        StringRoutines.quote("/home/" + userName + "/FindStartingHeuristic/Ludii.jar"),
				                        "--eval-agents",
				                        "--game",
				                        StringRoutines.quote(gameName + ".lud"),
				                        "--ruleset",
				                        StringRoutines.quote(fullRulesetName),
				                        "-n",
				                        "" + numGamesPerComb,
				                        "--game-length-cap 1000",
				                        "--thinking-time 1",
				                        "--warming-up-secs 0",
				                        "--out-dir",
				                        StringRoutines.quote
				                        (
				                        	"/work/" + 
				                        	userName + 
				                        	"/FindStartingHeuristic/" + 
				                        	filepathsGameName + filepathsRulesetName +
				                        	"/" + matchupStr + "/"
				                        ),
				                        "--agents",
				                        StringRoutines.join(" ", agentStrings),
				                        "--max-wall-time",
				                        "" + Math.max((MAX_WALL_TIME / opponentCombinations.size()), 60),
				                        "--output-alpha-rank-data",
				                        "--no-print-out",
				                        "--round-to-next-permutations-divisor"
									);
							
							writer.println(javaCall);
						}
						
						jobScriptNames.add(jobScriptFilename);
					}
					catch (final FileNotFoundException | UnsupportedEncodingException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		
		final List<List<String>> jobScriptsLists = new ArrayList<List<String>>();
		List<String> remainingJobScriptNames = jobScriptNames;

		while (remainingJobScriptNames.size() > 0)
		{
			if (remainingJobScriptNames.size() > MAX_JOBS_PER_BATCH)
			{
				final List<String> subList = new ArrayList<String>();

				for (int i = 0; i < MAX_JOBS_PER_BATCH; ++i)
				{
					subList.add(remainingJobScriptNames.get(i));
				}

				jobScriptsLists.add(subList);
				remainingJobScriptNames = remainingJobScriptNames.subList(MAX_JOBS_PER_BATCH, remainingJobScriptNames.size());
			}
			else
			{
				jobScriptsLists.add(remainingJobScriptNames);
				remainingJobScriptNames = new ArrayList<String>();
			}
		}

		for (int i = 0; i < jobScriptsLists.size(); ++i)
		{
			try (final PrintWriter writer = new UnixPrintWriter(new File(scriptsDir + "SubmitJobs_Part" + i + ".sh"), "UTF-8"))
			{
				for (final String jobScriptName : jobScriptsLists.get(i))
				{
					writer.println("sbatch " + jobScriptName);
				}
			}
			catch (final FileNotFoundException | UnsupportedEncodingException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Generates all combinations of given target combination-length from
	 * the given list of candidates (without replacement, order does not
	 * matter).
	 * 
	 * @param candidates
	 * @param combinationLength
	 * @param startIdx Index at which to start filling up results array
	 * @param currentCombination (partial) combination constructed so far
	 * @param combinations List of all result combinations
	 */
	private static void generateAllCombinations
	(
		final TIntArrayList candidates,
		final int combinationLength,
		final int startIdx,
		final int[] currentCombination,
		final List<TIntArrayList> combinations
	)
	{
		if (combinationLength == 0)
		{
			combinations.add(new TIntArrayList(currentCombination));
		}
		else
		{
			for (int i = startIdx; i <= candidates.size() - combinationLength; ++i)
			{
				currentCombination[currentCombination.length - combinationLength] = candidates.getQuick(i);
				generateAllCombinations(candidates, combinationLength - 1, i + 1, currentCombination, combinations);
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns filepaths for a heuristic (and writes them if they don't already
	 * exist)
	 * @param heuristicDescription
	 * @param game
	 * @param argParse
	 * @return List of filepaths for heuristic term (filepaths for use on cluster)
	 */
	private static List<String> heuristicFilepaths
	(
		final String heuristicDescription, 
		final Game game,
		final CommandLineArgParse argParse
	)
	{
		final List<String> returnFilepaths = new ArrayList<String>();
		final List<String> writeFilepaths = new ArrayList<String>();
		final List<HeuristicTerm> heuristicTerms = new ArrayList<HeuristicTerm>();
		
		final String userName = argParse.getValueString("--user-name");
		
		String scriptsDir = argParse.getValueString("--scripts-dir");
		scriptsDir = scriptsDir.replaceAll(Pattern.quote("\\"), "/");
		if (!scriptsDir.endsWith("/"))
			scriptsDir += "/";
		
		if (heuristicDescription.equals("CentreProximity"))
		{
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/CentreProximityPos.txt");
			writeFilepaths.add(scriptsDir + "CentreProximityPos.txt");
			heuristicTerms.add(new CentreProximity(null, Float.valueOf(1.f), null));
			
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/CentreProximityNeg.txt");
			writeFilepaths.add(scriptsDir + "CentreProximityNeg.txt");
			heuristicTerms.add(new CentreProximity(null, Float.valueOf(-1.f), null));
		}
		else if (heuristicDescription.equals("ComponentValues"))
		{
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/ComponentValuesPos.txt");
			writeFilepaths.add(scriptsDir + "ComponentValuesPos.txt");
			heuristicTerms.add(new ComponentValues(null, Float.valueOf(1.f), null, null));
			
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/ComponentValuesNeg.txt");
			writeFilepaths.add(scriptsDir + "ComponentValuesNeg.txt");
			heuristicTerms.add(new ComponentValues(null, Float.valueOf(-1.f), null, null));
		}
		else if (heuristicDescription.equals("CornerProximity"))
		{
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/CornerProximityPos.txt");
			writeFilepaths.add(scriptsDir + "CornerProximityPos.txt");
			heuristicTerms.add(new CornerProximity(null, Float.valueOf(1.f), null));
			
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/CornerProximityNeg.txt");
			writeFilepaths.add(scriptsDir + "CornerProximityNeg.txt");
			heuristicTerms.add(new CornerProximity(null, Float.valueOf(-1.f), null));
		}
		else if (heuristicDescription.equals("LineCompletionHeuristic"))
		{
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/LineCompletionHeuristicPos.txt");
			writeFilepaths.add(scriptsDir + "LineCompletionHeuristicPos.txt");
			heuristicTerms.add(new LineCompletionHeuristic(null, Float.valueOf(1.f), null));
			
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/LineCompletionHeuristicNeg.txt");
			writeFilepaths.add(scriptsDir + "LineCompletionHeuristicNeg.txt");
			heuristicTerms.add(new LineCompletionHeuristic(null, Float.valueOf(-1.f), null));
		}
		else if (heuristicDescription.equals("Material"))
		{
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/MaterialPos.txt");
			writeFilepaths.add(scriptsDir + "MaterialPos.txt");
			heuristicTerms.add(new Material(null, Float.valueOf(1.f), null, null));
			
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/MaterialNeg.txt");
			writeFilepaths.add(scriptsDir + "MaterialNeg.txt");
			heuristicTerms.add(new Material(null, Float.valueOf(-1.f), null, null));
		}
		else if (heuristicDescription.equals("MobilityAdvanced"))
		{
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/MobilityAdvancedPos.txt");
			writeFilepaths.add(scriptsDir + "MobilityAdvancedPos.txt");
			heuristicTerms.add(new MobilityAdvanced(null, Float.valueOf(1.f)));
			
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/MobilityAdvancedNeg.txt");
			writeFilepaths.add(scriptsDir + "MobilityAdvancedNeg.txt");
			heuristicTerms.add(new MobilityAdvanced(null, Float.valueOf(-1.f)));
		}
		else if (heuristicDescription.equals("MobilitySimple"))
		{
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/MobilitySimplePos.txt");
			writeFilepaths.add(scriptsDir + "MobilitySimplePos.txt");
			heuristicTerms.add(new MobilitySimple(null, Float.valueOf(1.f)));
			
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/MobilitySimpleNeg.txt");
			writeFilepaths.add(scriptsDir + "MobilitySimpleNeg.txt");
			heuristicTerms.add(new MobilitySimple(null, Float.valueOf(-1.f)));
		}
		else if (heuristicDescription.equals("NullHeuristic"))
		{
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/NullHeuristicPos.txt");
			writeFilepaths.add(scriptsDir + "NullHeuristicPos.txt");
			heuristicTerms.add(new NullHeuristic());
		}
		else if (heuristicDescription.equals("Influence"))
		{
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/InfluencePos.txt");
			writeFilepaths.add(scriptsDir + "InfluencePos.txt");
			heuristicTerms.add(new Influence(null, Float.valueOf(1.f)));
			
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/InfluenceNeg.txt");
			writeFilepaths.add(scriptsDir + "InfluenceNeg.txt");
			heuristicTerms.add(new Influence(null, Float.valueOf(-1.f)));
		}
		else if (heuristicDescription.equals("InfluenceAdvanced"))
		{
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/InfluenceAdvancedPos.txt");
			writeFilepaths.add(scriptsDir + "InfluenceAdvancedPos.txt");
			heuristicTerms.add(new InfluenceAdvanced(null, Float.valueOf(1.f)));
			
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/InfluenceAdvancedNeg.txt");
			writeFilepaths.add(scriptsDir + "InfluenceAdvancedNeg.txt");
			heuristicTerms.add(new InfluenceAdvanced(null, Float.valueOf(-1.f)));
		}
		else if (heuristicDescription.equals("OwnRegionsCount"))
		{
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/OwnRegionsCountPos.txt");
			writeFilepaths.add(scriptsDir + "OwnRegionsCountPos.txt");
			heuristicTerms.add(new OwnRegionsCount(null, Float.valueOf(1.f)));
			
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/OwnRegionsCountNeg.txt");
			writeFilepaths.add(scriptsDir + "OwnRegionsCountNeg.txt");
			heuristicTerms.add(new OwnRegionsCount(null, Float.valueOf(-1.f)));
		}
		else if (heuristicDescription.equals("PlayerRegionsProximity"))
		{
			final Regions[] regions = game.equipment().regions();

			for (int p = 1; p <= game.players().count(); ++p)
			{
				boolean foundOwnedRegion = false;
				
				for (final Regions region : regions)
				{
					if (region.owner() == p)
					{
						foundOwnedRegion = true;
						break;
					}
				}
				
				if (foundOwnedRegion)
				{
					returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/PlayerRegionsProximityPos_" + p + ".txt");
					writeFilepaths.add(scriptsDir + "PlayerRegionsProximityPos_" + p + ".txt");
					heuristicTerms.add(new PlayerRegionsProximity(null, Float.valueOf(1.f), Integer.valueOf(p), null));
					
					returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/PlayerRegionsProximityNeg_" + p + ".txt");
					writeFilepaths.add(scriptsDir + "PlayerRegionsProximityNeg_" + p + ".txt");
					heuristicTerms.add(new PlayerRegionsProximity(null, Float.valueOf(-1.f), Integer.valueOf(p), null));
				}
			}
		}
		else if (heuristicDescription.equals("PlayerSiteMapCount"))
		{
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/PlayerSiteMapCountPos.txt");
			writeFilepaths.add(scriptsDir + "PlayerSiteMapCountPos.txt");
			heuristicTerms.add(new PlayerSiteMapCount(null, Float.valueOf(1.f)));
			
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/PlayerSiteMapCountNeg.txt");
			writeFilepaths.add(scriptsDir + "PlayerSiteMapCountNeg.txt");
			heuristicTerms.add(new PlayerSiteMapCount(null, Float.valueOf(-1.f)));
		}
		else if (heuristicDescription.equals("RegionProximity"))
		{
			final Regions[] regions = game.equipment().regions();
		
			for (int i = 0; i < regions.length; ++i)
			{
				if (game.distancesToRegions()[i] != null)
				{
					returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/RegionProximityPos_" + i + ".txt");
					writeFilepaths.add(scriptsDir + "RegionProximityPos_" + i + ".txt");
					heuristicTerms.add(new RegionProximity(null, Float.valueOf(1.f), Integer.valueOf(i), null));
					
					returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/RegionProximityNeg_" + i + ".txt");
					writeFilepaths.add(scriptsDir + "RegionProximityNeg_" + i + ".txt");
					heuristicTerms.add(new RegionProximity(null, Float.valueOf(-1.f), Integer.valueOf(i), null));
				}
			}
		}
		else if (heuristicDescription.equals("Score"))
		{
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/ScorePos.txt");
			writeFilepaths.add(scriptsDir + "ScorePos.txt");
			heuristicTerms.add(new Score(null, Float.valueOf(1.f)));
			
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/ScoreNeg.txt");
			writeFilepaths.add(scriptsDir + "ScoreNeg.txt");
			heuristicTerms.add(new Score(null, Float.valueOf(-1.f)));
		}
		else if (heuristicDescription.equals("SidesProximity"))
		{
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/SidesProximityPos.txt");
			writeFilepaths.add(scriptsDir + "SidesProximityPos.txt");
			heuristicTerms.add(new SidesProximity(null, Float.valueOf(1.f), null));
			
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/SidesProximityNeg.txt");
			writeFilepaths.add(scriptsDir + "SidesProximityNeg.txt");
			heuristicTerms.add(new SidesProximity(null, Float.valueOf(-1.f), null));
		}
		else if (heuristicDescription.equals("UnthreatenedMaterial"))
		{
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/UnthreatenedMaterialPos.txt");
			writeFilepaths.add(scriptsDir + "UnthreatenedMaterialPos.txt");
			heuristicTerms.add(new UnthreatenedMaterial(null, Float.valueOf(1.f), null));
			
			returnFilepaths.add("/home/" + userName + "/FindStartingHeuristic/UnthreatenedMaterialNeg.txt");
			writeFilepaths.add(scriptsDir + "UnthreatenedMaterialNeg.txt");
			heuristicTerms.add(new UnthreatenedMaterial(null, Float.valueOf(-1.f), null));
		}
		else
		{
			throw new RuntimeException("Did not recognise heuristic description: " + heuristicDescription);
		}
		
		// Write our heuristic files
		for (int i = 0; i < heuristicTerms.size(); ++i)
		{
			final HeuristicTerm heuristic = heuristicTerms.get(i);
			final File file = new File(writeFilepaths.get(i));
			
			if (!file.exists())
			{
				try (final PrintWriter writer = new UnixPrintWriter(file, "UTF-8"))
				{
					writer.write(new Heuristics(heuristic).toString());
				}
				catch (final FileNotFoundException | UnsupportedEncodingException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		return returnFilepaths;
	}
	
	/**
	 * Main method to generate all our scripts
	 * @param args
	 */
	public static void main(final String[] args)
	{
		// define options for arg parser
		final CommandLineArgParse argParse = 
				new CommandLineArgParse
				(
					true,
					"Evaluate playing strength of different heuristic sampling agents with different"
					+ " default heuristics against each other."
				);
		
		argParse.addOption(new ArgOption()
				.withNames("--project")
				.help("Project for which to submit the job on cluster.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		argParse.addOption(new ArgOption()
				.withNames("--user-name")
				.help("Username on the cluster.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		argParse.addOption(new ArgOption()
				.withNames("--scripts-dir")
				.help("Directory in which to store generated scripts.")
				.withNumVals(1)
				.withType(OptionTypes.String)
				.setRequired());
		
		// parse the args
		if (!argParse.parseArguments(args))
			return;
		
		generateScripts(argParse);
	}
	
	//-------------------------------------------------------------------------

}
