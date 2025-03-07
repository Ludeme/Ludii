package utils.concepts.script;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import game.Game;
import main.FileHandling;
import main.UnixPrintWriter;
import main.options.Ruleset;
import other.GameLoader;

/**
 * Script to run the state concepts computation on the cluster.
 * 
 * @author Eric.Piette
 */
public class CreateLeMaitre4ClusterConceptScript
{

	/** Memory to assign to JVM */
	private static final String JVM_MEM_MIN = "512g"; // 128g
	
	/** Memory to assign to JVM */
	private static final String JVM_MEM_MAX = "512g"; // 128g

	/** Max wall time (in minutes) */
	private static final int MAX_WALL_TIME = 2880; // 2880 is the max on LeMaitre4
	
	// TODO no idea what this should be on Lemaitre4
	/** Cluster doesn't seem to let us request more memory than this for any single job (on a single node) */
	private static final int MAX_REQUEST_MEM = 600; // 600
	
	/** Number of cores per node (this is for Lemaitre4) */
	private static final int CORES_PER_NODE = 128; // 32
	
	/** Number of cores per Java call */
	private static final int CORES_PER_PROCESS = 128;
	
	/**Number of processes we can put in a single job (on a single node) */
	private static final int PROCESSES_PER_JOB = CORES_PER_NODE / CORES_PER_PROCESS;
	
	public static void main(final String[] args)
	{
		final int numPlayout = 100;
		final int maxMove = 5000; //250; //5000; // Constants.DEFAULT_MOVES_LIMIT;
		final int thinkingTime = 1;
		final String agentName = "Random"; //"Alpha-Beta"; // Can be "UCT",  "Alpha-Beta", "Alpha-Beta-UCT", "AB-Odd-Even", "ABONEPLY", "UCTONEPLY", or "Random"
		final String clusterLogin = "epiette";
		final String folder = "/../../Trials/Trials"+agentName; //""; //"/../Trials/TrialsAll";
		final String mainScriptName = "Concepts.sh";
		final String folderName = "Concepts"+agentName;
		final String jobName = agentName + "Concept";
		final int numRulesetsPerBatch = 1; // 42

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

				// For the museum game.
//				if(!gameName.contains("Ludus Coriovalli"))
//					continue;
				
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

			System.out.println("***************************" + rulesetNames.size() + " rulesets ***************************");
			int scriptId = 0;
			
			for(int i = 0; i < (rulesetNames.size() / numRulesetsPerBatch + 1); i++)
			{
				final String scriptName = "Concepts" + scriptId + ".sh";
				System.out.println(scriptName + " " + "created.");
				mainWriter.println("sbatch " + scriptName);
				
				try (final PrintWriter writer = new UnixPrintWriter(new File(scriptName), "UTF-8"))
				{
					writer.println("#!/bin/bash");
					writer.println("#SBATCH -J GenConcepts" + jobName + "Script" + scriptId);
					writer.println("#SBATCH -p batch");
					writer.println("#SBATCH -o /globalscratch/ucl/ingi/"+clusterLogin+"/Out/Out_%J.out");
					writer.println("#SBATCH -e /globalscratch/ucl/ingi/"+clusterLogin+"/Err/Err_%J.err");
					writer.println("#SBATCH -t "+ MAX_WALL_TIME);
					writer.println("#SBATCH -N 1");

					final int numProcessesThisJob = PROCESSES_PER_JOB;
					
					writer.println("#SBATCH --cpus-per-task="+ (numProcessesThisJob * CORES_PER_PROCESS)); // 128s
					writer.println("#SBATCH --mem="+MAX_REQUEST_MEM+"G");
					writer.println("#SBATCH --exclusive");
					writer.println("module load Java/11.0.20");
					
					for(int j = 0; j < numRulesetsPerBatch; j++)
					{
						if((i*numRulesetsPerBatch+j) < rulesetNames.size())
						{
							String jobLine = "";
							//jobLine += "taskset -c "; 
							//jobLine += (3*j) + "," + (3*j + 1) + "," +  (3*j + 2) + " "; 
							jobLine += "java -Xms" + JVM_MEM_MIN + " -Xmx" + JVM_MEM_MAX + " -XX:+HeapDumpOnOutOfMemoryError -da -dsa -XX:+UseStringDeduplication -jar \"/globalscratch/ucl/ingi/"+ clusterLogin +"/ludii/Concepts/" + folderName + "/Ludii.jar\" --export-moveconcept-db ";
							jobLine += numPlayout + " " + MAX_WALL_TIME + " " + thinkingTime + " " + maxMove + " "  + "\"" + agentName + "\"" + " " + "\"" + folder  + "\"" + " " + "\"";
							jobLine += rulesetNames.get(i*numRulesetsPerBatch+j);
							jobLine += " " + "> /globalscratch/ucl/ingi/"+ clusterLogin +"/Out/Out_${SLURM_JOB_ID}_"+ j +".out &";
							writer.println(jobLine);
						}
					}
					writer.println("wait");
					
//					writer.println(
//							"java -Xms"+allocatedMemoryJava+"M -Xmx"+allocatedMemoryJava+"M -XX:+HeapDumpOnOutOfMemoryError -da -dsa -XX:+UseStringDeduplication -jar \"/home/"+clusterLogin+"/ludii/" + folderName + "/ludii.jar\" --export-moveconcept-db "
//									+ numPlayout + " " + maxTime + " " + thinkingTime + " " + maxMove + " "  + "\"" + agentName + "\"" + " " + "\"" + folder  + "\"" + " " + "\"" + gameName.substring(1) + "\"");
//					mainWriter.println("sbatch " + scriptName);
				}
				scriptId++;
			}
			
//			if(rulesetNames.isEmpty())
//			{
//				try (final PrintWriter writer = new UnixPrintWriter(new File(scriptName), "UTF-8"))
//				{
//					writer.println("#!/bin/bash");
//					writer.println("#SBATCH -J GenConcepts"+jobName + fileName+"Script" + scriptId);
//					writer.println("#SBATCH -p thin");
//					writer.println("#SBATCH -o /home/piettee/Out/Out_%J.out");
//					writer.println("#SBATCH -e /home/piettee/Out/Err_%J.err");
//					writer.println("#SBATCH -t 6000");
//					writer.println("#SBATCH -N 1");
//					writer.println("#SBATCH --cpus-per-task=128");
//					writer.println("#SBATCH --mem=234G");
//					writer.println("#SBATCH --exclusive");
//					writer.println("module load 2021");
//					writer.println("module load Java/11.0.2");
//					writer.println(
//							"java -Xms"+allocatedMemoryJava+"M -Xmx"+allocatedMemoryJava+"M -XX:+HeapDumpOnOutOfMemoryError -da -dsa -XX:+UseStringDeduplication -jar \"/home/"+clusterLogin+"/ludii/" + folderName + "/ludii.jar\" --export-moveconcept-db "
//									+ numPlayout + " " + maxTime + " " + thinkingTime + " " + maxMove + " "  + "\"" + agentName + "\"" + " " + "\"" + folder  + "\"" + " " + "\"" + gameName.substring(1) + "\"");
//					mainWriter.println("sbatch " + scriptName);
//				}
//			}
//			else
//			{
//				for(final String rulesetName : rulesetNames)
//				{
//					final String scriptName = "StateConcepts" + fileName + "-" + StringRoutines.cleanGameName(rulesetName.substring(8)) + ".sh";
//					
//					System.out.println(scriptName + " " + "created.");
//					
//					try (final PrintWriter writer = new UnixPrintWriter(new File(scriptName), "UTF-8"))
//					{
//						writer.println("#!/bin/bash");
//						writer.println("#SBATCH -J GenConcepts"+jobName + fileName+"Script" + scriptId);
//						writer.println("#SBATCH -p thin");
//						writer.println("#SBATCH -o /home/piettee/Out/Out_%J.out");
//						writer.println("#SBATCH -e /home/piettee/Out/Err_%J.err");
//						writer.println("#SBATCH -t 6000");
//						writer.println("#SBATCH -N 1");
//						writer.println("#SBATCH --cpus-per-task=128");
//						writer.println("#SBATCH --mem=234G");
//						writer.println("#SBATCH --exclusive");
//						writer.println("module load 2021");
//						writer.println("module load Java/11.0.2");
//						writer.println(
//								"java -Xms"+allocatedMemoryJava+"M -Xmx"+allocatedMemoryJava+"M -XX:+HeapDumpOnOutOfMemoryError -da -dsa -XX:+UseStringDeduplication -jar \"/home/"+clusterLogin+"/ludii/" + folderName + "/ludii.jar\" --export-moveconcept-db "
//										+ numPlayout + " " + maxTime + " " + thinkingTime + " " + maxMove + " " + "\"" + agentName + "\"" + " " + "\"" + folder  + "\"" + " " + "\"" + gameName.substring(1) + "\"" + " " + "\"" + rulesetName + "\"");
//						mainWriter.println("sbatch " + scriptName);
//					}
//				}
//			}
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
