package utils.trials;

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
 * Script to generate all the .sh to generate the different trials for the Snellius cluster on thin nodes.
 * 
 * @author Eric.Piette
 */
public class CreateSneliusClusterTrialsScript
{
	public static void main(final String[] args)
	{
		final int numPlayout = 100;
		final int maxMove = 5000; // Constants.DEFAULT_MOVES_LIMIT;
		//final int allocatedMemoryJava = 4096;
		final int thinkingTime = 1;
		final String agentName = "Alpha-Beta"; // Can be "UCT",  "Alpha-Beta", "Alpha-Beta-UCT", "AB-Odd-Even", or "Random"
		final String clusterLogin = "cbrowne";
		final String mainScriptName = "GenTrials.sh";
		
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

			System.out.println("***************************" + rulesetNames.size() + " rulesets ***************************");
			int scriptId = 0;
			
			for(int i = 0; i < (rulesetNames.size() / 42 + 1); i++)
			{
				final String scriptName = "GenTrial_" + scriptId + ".sh";
				mainWriter.println("sbatch " + scriptName);
				
				try (final PrintWriter writer = new UnixPrintWriter(new File(scriptName), "UTF-8"))
				{
					writer.println("#!/bin/bash");
					writer.println("#SBATCH -J GenTrials"+agentName+"Script" + scriptId);
					writer.println("#SBATCH -p thin");
					writer.println("#SBATCH -o /home/" + clusterLogin + "/Out/Out_%J.out");
					writer.println("#SBATCH -e /home/" + clusterLogin + "/Out/Err_%J.err");
					writer.println("#SBATCH -t 6000");
					writer.println("#SBATCH -N 1");
					writer.println("#SBATCH --cpus-per-task=128");
					writer.println("#SBATCH --mem=224G");
					writer.println("#SBATCH --exclusive");
					writer.println("module load 2021");
					writer.println("module load Java/11.0.2");
					
					for(int j = 0; j < 42; j++)
					{
						if((i*42+j) < rulesetNames.size())
						{
							String jobLine = "taskset -c ";
							jobLine += (3*j) + "," + (3*j + 1) + "," +  (3*j + 2) + " "; 
							jobLine += "java -Xms5120M -Xmx5120M -XX:+HeapDumpOnOutOfMemoryError -da -dsa -XX:+UseStringDeduplication -jar \"/home/" + clusterLogin + "/ludii/Trials/Ludii.jar\" --generate-trials-parallel ";
							jobLine += maxMove + " " + thinkingTime + " " + numPlayout + " "  + "\"" + agentName + "\"" + " " + "\"";
							jobLine += rulesetNames.get(i*42+j);
							jobLine += " " + "> /home/" + clusterLogin + "/Out/Out_${SLURM_JOB_ID}_"+ j +".out &";
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
