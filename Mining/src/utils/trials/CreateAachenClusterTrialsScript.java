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
import main.StringRoutines;
import main.UnixPrintWriter;
import main.options.Ruleset;
import other.GameLoader;

/**
 * Script to generate all the .sh to generate the different trials
 * 
 * @author Eric.Piette
 */
public class CreateAachenClusterTrialsScript
{
	public static void main(final String[] args)
	{
		final int numPlayout = 100;
		final int maxMove = 5000; // Constants.DEFAULT_MOVES_LIMIT;
		final int allocatedMemoryJava = 4096;
		final int thinkingTime = 1;
		final String agentName = "UCT"; // Can be "UCT",  "Alpha-Beta", "Alpha-Beta-UCT", "AB-Odd-Even", or "Random"
		final String clusterLogin = "ls670643";
		final String mainScriptName = "GenTrials.sh";
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
				
				final String fileName = gameName.isEmpty() ? ""
						: StringRoutines.cleanGameName(gameName.substring(gameName.lastIndexOf('/') + 1, gameName.length()));
				
				final List<String> rulesetNames = new ArrayList<String>();
				final List<Ruleset> rulesetsInGame = game.description().rulesets();
				
				// Get all the rulesets of the game if it has some.
				if (rulesetsInGame != null && !rulesetsInGame.isEmpty())
				{
					for (int rs = 0; rs < rulesetsInGame.size(); rs++)
					{
						final Ruleset ruleset = rulesetsInGame.get(rs);
						if (!ruleset.optionSettings().isEmpty()) // We check if the ruleset is implemented.
							rulesetNames.add(ruleset.heading());
					}
				}
				
				if(rulesetNames.isEmpty())
				{
					final String scriptName = "GenTrials" + fileName + ".sh";
	
					System.out.println(scriptName + " " + "created.");
					
					try (final PrintWriter writer = new UnixPrintWriter(new File(scriptName), "UTF-8"))
					{
						writer.println("#!/usr/local_rwth/bin/zsh");
						writer.println("#SBATCH -J GenTrials" + agentName + fileName);
						writer.println("#!/usr/local_rwth/bin/zsh");
						writer.println("#SBATCH -o /work/" + clusterLogin + "/result/Out" + agentName + fileName + "Gentrials_%J.out");
						writer.println("#SBATCH -e /work/" + clusterLogin + "/result/Err" + agentName + fileName + "Gentrials_%J.err");
						writer.println("#SBATCH -t 6000");
						writer.println("#SBATCH --mem-per-cpu="+(int)(allocatedMemoryJava*1.25));
						writer.println("#SBATCH -A um_dke");
						writer.println("unset JAVA_TOOL_OPTIONS");
						writer.println(
								"java -Xms"+allocatedMemoryJava+"M -Xmx"+allocatedMemoryJava+"M -XX:+HeapDumpOnOutOfMemoryError -da -dsa -XX:+UseStringDeduplication -jar \"/home/"+clusterLogin+"/ludii/Trials/Trials"+ agentName +"/ludii.jar\" --generate-trials "
										+ maxMove + " " + thinkingTime + " " + numPlayout  + " " + "\"" + agentName + "\"" + " " + "\"" + gameName.substring(1) + "\"");
						mainWriter.println("sbatch " + scriptName);
					}
				}
				else
				{
					for(final String rulesetName : rulesetNames)
					{
						final String scriptName = "GenTrials" + fileName + "-" + StringRoutines.cleanGameName(rulesetName.substring(8)) + ".sh";
						
						System.out.println(scriptName + " " + "created.");
						
						try (final PrintWriter writer = new UnixPrintWriter(new File(scriptName), "UTF-8"))
						{
							writer.println("#!/usr/local_rwth/bin/zsh");
							writer.println("#SBATCH -J GenTrials" + agentName + fileName);
							writer.println("#!/usr/local_rwth/bin/zsh");
							writer.println("#SBATCH -o /work/"+clusterLogin+"/result/Out" + agentName + fileName + "Gentrials_%J.out");
							writer.println("#SBATCH -e /work/"+clusterLogin+"/result/Err" + agentName + fileName + "Gentrials_%J.err");
							writer.println("#SBATCH -t 6000");
							writer.println("#SBATCH --mem-per-cpu="+(int)(allocatedMemoryJava*1.25));
							writer.println("#SBATCH -A um_dke");
							writer.println("unset JAVA_TOOL_OPTIONS");
							writer.println(
									"java -Xms"+allocatedMemoryJava+"M -Xmx"+allocatedMemoryJava+"M -XX:+HeapDumpOnOutOfMemoryError -da -dsa -XX:+UseStringDeduplication -jar \"/home/"+clusterLogin+"/ludii/Trials//Trials"+ agentName +"/ludii.jar\" --generate-trials "
											+ maxMove + " " + thinkingTime + " " + numPlayout + " "  + "\"" + agentName + "\"" + " " + "\"" + gameName.substring(1) + "\"" + " " + "\"" + rulesetName + "\"");
							mainWriter.println("sbatch " + scriptName);
						}
					}
				}
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
