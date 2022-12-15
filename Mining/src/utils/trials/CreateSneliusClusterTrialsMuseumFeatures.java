package utils.trials;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import game.Game;
import main.StringRoutines;
import main.UnixPrintWriter;
import main.options.Ruleset;
import other.GameLoader;

/**
 * Script to generate all the .sh to generate the different trials for the Snellius cluster on thin nodes.
 * 
 * This is for the museum game rulesets with the features.
 * 
 * @author Eric.Piette and Dennis Soemers
 */
public class CreateSneliusClusterTrialsMuseumFeatures
{
	
	private static final String[] POLICIES = 
			new String[] 
					{
						"Tree_1",
						"Tree_2",
						"Tree_3",
						"Tree_4",
						"Tree_5",
						"TSPG"
					};
	
	public static void main(final String[] args)
	{
		final int numPlayout = 100;
		final int maxMove = 250; // Constants.DEFAULT_MOVES_LIMIT;
		//final int allocatedMemoryJava = 4096;
		final int thinkingTime = 1;
		//final String clusterLogin = "piettee";
		final String mainScriptName = "GenTrials.sh";
		
		final ArrayList<ProcessData> processDataList = new ArrayList<ProcessData>();
		try (final PrintWriter mainWriter = new UnixPrintWriter(new File(mainScriptName), "UTF-8"))
		{
			final String gameName = "/Ludus Coriovalli.lud";

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
					if (!ruleset.optionSettings().isEmpty()) // We check if the ruleset is implemented.
						gameRulesetNames.add(ruleset.heading());
				}
			}

			// We get the name of all the rulesets
			for (final String rulesetName : gameRulesetNames)
			{
				for (int i = 0; i < POLICIES.length - 1; ++i)
				{
					for (int j = i + 1; j < POLICIES.length; ++j)
					{
						processDataList.add
						(
							new ProcessData
							(
								gameName.substring(1) + "\"" + " " + "\"" + rulesetName + "\"",
								POLICIES[i], 
								POLICIES[j],
								StringRoutines.cleanGameName(gameName.replaceAll(Pattern.quote(".lud"), "")),
								StringRoutines.cleanRulesetName(rulesetName).replaceAll(Pattern.quote("/"), "_")
							)
						);
					}
				}
				
				System.out.println(gameName.substring(1)+ "/"+ rulesetName);
			}

			int scriptId = 0;
			
			for(int i = 0; i < (processDataList.size() / 42 + 1); i++)
			{
				final String scriptName = "GenTrial_" + scriptId + ".sh";
				mainWriter.println("sbatch " + scriptName);
				
				try (final PrintWriter writer = new UnixPrintWriter(new File(scriptName), "UTF-8"))
				{
					writer.println("#!/bin/bash");
					writer.println("#SBATCH -J GenTrialsMuseumFeatures" + scriptId);
					writer.println("#SBATCH -p thin");
					writer.println("#SBATCH -o /home/piettee/Out/Out_%J.out");
					writer.println("#SBATCH -e /home/piettee/Out/Err_%J.err");
					writer.println("#SBATCH -t 6000");
					writer.println("#SBATCH -N 1");
					writer.println("#SBATCH --cpus-per-task=128");
					writer.println("#SBATCH --mem=234G");
					writer.println("#SBATCH --exclusive");
					writer.println("module load 2021");
					writer.println("module load Java/11.0.2");
					
					for(int j = 0; j < 42; j++)
					{
						final int pIdx = i * 42 + j;
						
						if (pIdx < processDataList.size())
						{
							final ProcessData processData = processDataList.get(pIdx);
							final String agentString1;
							final String agentString2;
							
							// Build string for first agent
							if (processData.agent1.equals("TSPG"))
							{
								final List<String> policyStrParts = new ArrayList<String>();
								policyStrParts.add("algorithm=Softmax");
								for (int p = 1; p <= 2; ++p)
								{
									policyStrParts.add
									(
										"policyweights" + 
										p + 
										"=/home/piettee/ludii/features" + 
										processData.cleanGameName + "_" + processData.cleanRulesetName +
										"/PolicyWeightsTSPG_P" + p + "_00201.txt"
									);
								}
								policyStrParts.add("friendly_name=TSPG");
								policyStrParts.add("boosted=true");
								
								agentString1 = 
										StringRoutines.join
										(
											";", 
											policyStrParts
										);
							}
							else
							{
								agentString1 = 
									StringRoutines.join
									(
										";", 
										"algorithm=SoftmaxPolicyLogitTree",
										"policytrees=/" + 
										StringRoutines.join
										(
											"/", 
											"home",
											"piettee",
											"ludii",
											"features" + processData.cleanGameName + "_" + processData.cleanRulesetName,
											"CE_Selection_Logit_" + processData.agent1 + ".txt"
										),
										"friendly_name=" + processData.agent1,
										"greedy=false"
									);
							}
							
							// Build string for second agent
							if (processData.agent2.equals("TSPG"))
							{
								final List<String> policyStrParts = new ArrayList<String>();
								policyStrParts.add("algorithm=Softmax");
								for (int p = 1; p <= 2; ++p)
								{
									policyStrParts.add
									(
										"policyweights" + 
										p + 
										"=/home/piettee/ludii/features" + 
										processData.cleanGameName + "_" + processData.cleanRulesetName +
										"/PolicyWeightsTSPG_P" + p + "_00201.txt"
									);
								}
								policyStrParts.add("friendly_name=TSPG");
								policyStrParts.add("boosted=true");
								
								agentString2 = 
										StringRoutines.join
										(
											";", 
											policyStrParts
										);
							}
							else
							{
								agentString2 = 
									StringRoutines.join
									(
										";", 
										"algorithm=SoftmaxPolicyLogitTree",
										"policytrees=/" + 
										StringRoutines.join
										(
											"/", 
											"home",
											"piettee",
											"ludii",
											"features" + processData.cleanGameName + "_" + processData.cleanRulesetName,
											"CE_Selection_Logit_" + processData.agent2 + ".txt"
										),
										"friendly_name=" + processData.agent2,
										"greedy=false"
									);
							}
							
							String jobLine = "taskset -c ";
							jobLine += (3*j) + "," + (3*j + 1) + "," +  (3*j + 2) + " "; 
							jobLine += "java -Xms5120M -Xmx5120M -XX:+HeapDumpOnOutOfMemoryError -da -dsa -XX:+UseStringDeduplication -jar \"/home/piettee/ludii/Trials/Ludii.jar\" --generate-trials-parallel ";
							jobLine += maxMove + " " + thinkingTime + " " + numPlayout + " "  + "\"" + agentString1 + "\"" + " " + "\"";
							jobLine += processData.rulesetName;
							jobLine += " " + StringRoutines.quote(agentString2);
							jobLine += " " + processData.agent1 + "_vs_" + processData.agent2;
							jobLine += " " + "> /home/piettee/Out/Out_${SLURM_JOB_ID}_"+ j +".out &";
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
	
	/**
	 * Wrapper for data we need for an individual Java process (of which we run multiple per job)
	 * 
	 * @author Dennis Soemers
	 */
	private static final class ProcessData
	{
		
		public final String rulesetName;
		public final String agent1;
		public final String agent2;
		public final String cleanGameName;
		public final String cleanRulesetName;
		
		/**
		 * Constructor
		 * @param rulesetName
		 * @param agent1
		 * @param agent2
		 * @param cleanGameName
		 * @param cleanRulesetName
		 */
		public ProcessData
		(
			final String rulesetName, 
			final String agent1, 
			final String agent2,
			final String cleanGameName,
			final String cleanRulesetName
		)
		{
			this.rulesetName = rulesetName;
			this.agent1 = agent1;
			this.agent2 = agent2;
			this.cleanGameName = cleanGameName;
			this.cleanRulesetName = cleanRulesetName;
		}
		
	}
}
