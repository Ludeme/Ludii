package utils.trials;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import game.Game;
import main.FileHandling;
import main.UnixPrintWriter;
import main.options.Ruleset;
import other.GameLoader;

/**
 * Script to generate all the .sh to generate the different trials for LeMaitre4 cluster.
 * 
 * @author Eric.Piette
 */
public class CreateLeMaitre4ClusterTrialsScript
{
	/** Max wall time (in minutes) */
	private static final int MAX_WALL_TIME = 1500;

	/** Memory to assign to JVM */
	private static final String JVM_MEM_MIN = "128g"; // 512g
	
	/** Memory to assign to JVM */
	private static final String JVM_MEM_MAX = "128g"; // 512g
	
	// TODO no idea what this should be on Lemaitre4
	/** Cluster doesn't seem to let us request more memory than this for any single job (on a single node) */
	private static final int MAX_REQUEST_MEM = 150; // 600
	
	/** Number of cores per node (this is for Lemaitre4) */
	private static final int CORES_PER_NODE = 32; // 128
	
	/** Number of cores per Java call */
	private static final int CORES_PER_PROCESS = 4;
	
	/**Number of processes we can put in a single job (on a single node) */
	private static final int PROCESSES_PER_JOB = CORES_PER_NODE / CORES_PER_PROCESS;
	
	public static void main(final String[] args)
	{
		final int numPlayout = 100;
		final int maxMove = 5000; // Constants.DEFAULT_MOVES_LIMIT;
		final int thinkingTime = 1;
		final String agentName = "Random"; // Can be "UCT",  "Alpha-Beta", "Alpha-Beta-UCT", "AB-Odd-Even", or "Random"
		final String clusterLogin = "epiette";
		final String mainScriptName = "GenTrials.sh";
		final int numRulesetsPerBatch = 8; // 48
		
		final ArrayList<String> rulesetNames = new ArrayList<String>();
		try (final PrintWriter mainWriter = new UnixPrintWriter(new File(mainScriptName), "UTF-8"))
		{
			final String[] gameNames = FileHandling.listGames();

			for (int index = 0; index < gameNames.length; index++)
			{
				final String gameName = gameNames[index];
				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/bad/"))
					continue;

				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/wip/"))
					continue;

				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/WishlistDLP/"))
					continue;

				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("/lud/test/"))
					continue;

				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("subgame"))
					continue;

				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("reconstruction/pending/"))
					continue;
				
				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("reconstruction/validation/"))
					continue;

				final Game game = GameLoader.loadGameFromName(gameName);
				
//				final String fileName = gameName.isEmpty() ? ""
//						: StringRoutines.cleanGameName(gameName.substring(gameName.lastIndexOf('/') + 1, gameName.length()));
				
				final List<String> gameRulesetNames = new ArrayList<String>();
				final List<Ruleset> rulesetsInGame = game.description().rulesets();
				
				// Get all the rulesets of the game if it has some.
				if (rulesetsInGame != null && !rulesetsInGame.isEmpty())
				{
					for (int rs = 0; rs < rulesetsInGame.size(); rs++)
					{
						final Ruleset ruleset = rulesetsInGame.get(rs);
						if (!ruleset.optionSettings().isEmpty() && !ruleset.heading().contains("Incomplete")) // We check if the ruleset is implemented.
							gameRulesetNames.add(ruleset.heading());
					}
				}

				// We get the name of all the rulesets
				if(gameRulesetNames.isEmpty())
				{
					rulesetNames.add(gameName.substring(1) + "\"");
					System.out.println(gameName.substring(1));
				}
				else
				{
					for(final String rulesetName : gameRulesetNames)
					{
						rulesetNames.add(gameName.substring(1) + "\"" + " " + "\"" + rulesetName + "\"");
						System.out.println(gameName.substring(1)+ "/"+ rulesetName);
					}
				}
			}

			// Write scripts with all the processes
			Collections.shuffle(rulesetNames);
			
			System.out.println("***************************" + rulesetNames.size() + " rulesets ***************************");
			int scriptId = 0;

			for(int i = 0; i < (rulesetNames.size() / numRulesetsPerBatch + 1); i++)
			{
				final String scriptName = "GenTrial_" + scriptId + ".sh";
				mainWriter.println("sbatch " + scriptName);
				
				try (final PrintWriter writer = new UnixPrintWriter(new File(scriptName), "UTF-8"))
				{
					writer.println("#!/bin/bash");
					writer.println("#SBATCH -J GenTrials"+agentName+"Script" + scriptId);
					writer.println("#SBATCH -p batch");
					writer.println("#SBATCH -o /globalscratch/ucl/ingi/" + clusterLogin + "/Out/Out_%J.out");
					writer.println("#SBATCH -e /globalscratch/ucl/ingi/" + clusterLogin + "/Err/Err_%J.err");
					writer.println("#SBATCH -t " + MAX_WALL_TIME);
					writer.println("#SBATCH -N 1");

					final int numProcessesThisJob = PROCESSES_PER_JOB;
					
					writer.println("#SBATCH --cpus-per-task=" + (numProcessesThisJob * CORES_PER_PROCESS));
					writer.println("#SBATCH --mem=" + MAX_REQUEST_MEM + "G");
					writer.println("#SBATCH --exclusive");
					writer.println("module load Java/11.0.20");
					
					for(int j = 0; j < numRulesetsPerBatch; j++)
					{
						if((i*numRulesetsPerBatch+j) < rulesetNames.size())
						{
							String jobLine = "";
							jobLine += "taskset -c ";
							jobLine += (CORES_PER_PROCESS*j) + "," + (CORES_PER_PROCESS*j + 1) + "," +  (CORES_PER_PROCESS*j + 2) + "," +  (CORES_PER_PROCESS*j + 3) + " "; 
							jobLine += "java -Xms" + JVM_MEM_MIN + " -Xmx" + JVM_MEM_MAX + " -XX:+HeapDumpOnOutOfMemoryError -da -dsa -XX:+UseStringDeduplication -jar \"/globalscratch/ucl/ingi/" + clusterLogin + "/ludii/Trials/Ludii.jar\" --generate-trials-parallel ";
							jobLine += maxMove + " " + thinkingTime + " " + numPlayout + " "  + "\"" + agentName + "\"" + " " + "\"";
							jobLine += rulesetNames.get(i*numRulesetsPerBatch+j);
							jobLine += " " + "> /globalscratch/ucl/ingi/" + clusterLogin + "/Out/Out_${SLURM_JOB_ID}_"+ j +".out &";
							writer.println(jobLine);
						}
					}
					writer.println("wait");
				}
				scriptId++;
			}
		}
		catch (final FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (final UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}
}
