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
public class CreateDSRIClusterTrialsScript
{
	public static void main(final String[] args)
	{
		String bashName = "";
		String jobName = "";
		
		// For runAll.sh
		String deleteAll = "oc delete jobs --all\n"
				+ "oc delete builds --all\n"
				+ "oc delete buildconfigs --all\n\n";
		
	    // For Dockerfile
	    String beginDockerFile ="FROM openjdk:18-oraclelinux7\n"
	    		+ "RUN mkdir -p /app\n"
	    		+ "WORKDIR /app\n"
				+ "ENTRYPOINT [\"java\", \"-jar\", \"/data/ludii.jar\"]\n"
	    		+ "CMD [";
	    String endDockerFile ="]";
	   // args //"--generate-trials", "5000", "1", "100", "Random", "lud/board/war/replacement/checkmate/chess/Chess.lud";

		
		final int numPlayout = 100;
		final int maxMove = 5000; // Constants.DEFAULT_MOVES_LIMIT;
		//final int allocatedMemoryJava = 4096;
		final int thinkingTime = 1;
		final String agentName = "Random"; // Can be "UCT",  "Alpha-Beta", "Alpha-Beta-UCT", "AB-Odd-Even", or "Random"
		final String mainScriptName = "allRun.sh";
		
		try (final PrintWriter mainWriter = new UnixPrintWriter(new File(mainScriptName), "UTF-8"))
		{
			// Write the delete lines in the big bash.
			mainWriter.println(deleteAll);
			
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

				if (gameName.replaceAll(Pattern.quote("\\"), "/").contains("reconstruction"))
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
					// Get the name of the bash file.
					bashName = fileName;
					// Get the name of the job.
					jobName = bashName+agentName+"Trials";
					jobName = jobName.toLowerCase();
					jobName = jobName.replace("_", "");
					
					// Write the line in the big bash
					mainWriter.println(createBashJob(bashName));

					// Write bash file for a specific ruleset
					final String rulesetScriptName = "run"+ bashName +".sh";
					try (final PrintWriter rulesetWriter = new UnixPrintWriter(new File(rulesetScriptName), "UTF-8"))
					{
						rulesetWriter.println(createRulesetBashJob(jobName));		
						final File jobFolderFile = new File(jobName);
						if(!jobFolderFile.exists())
							jobFolderFile.mkdirs();
					}
					
					// Write YML file for a specific ruleset
					final String YMLName = jobName + File.separator + jobName +".yml";
					try (final PrintWriter ymlWriter = new UnixPrintWriter(new File(YMLName), "UTF-8"))
					{
						ymlWriter.println(createYML(jobName));
					}
					
					// Write Docker file for a specific ruleset
					final String dockerName = jobName + File.separator + "Dockerfile";
					try (final PrintWriter dockerWriter = new UnixPrintWriter(new File(dockerName), "UTF-8"))
					{
						dockerWriter.print(beginDockerFile);
						dockerWriter.print("\"--generate-trials\", ");
						dockerWriter.print("\"" + maxMove + "\", ");
						dockerWriter.print("\"" + thinkingTime + "\", ");
						dockerWriter.print("\"" + numPlayout + "\", ");
						dockerWriter.print("\"" + agentName + "\", ");
						dockerWriter.print("\"" + gameName.substring(1) + "\"");
						dockerWriter.println(endDockerFile);
					}
					
					System.out.println(createBashJob(bashName) + " " + "written.");
				}
				else
				{
					for(final String rulesetName : rulesetNames)
					{
						// Get the name of the bash file.
						bashName = fileName + "-" + StringRoutines.cleanGameName(rulesetName.substring(8));
						// Get the name of the job.
						jobName = bashName+agentName+"Trials";
						jobName = jobName.toLowerCase();
						jobName = jobName.replace("_", "");

						// Write the line in the big bash
						mainWriter.println(createBashJob(bashName));
						
						// Write bash file for a specific ruleset
						final String rulesetScriptName = "run"+ bashName +".sh";
						try (final PrintWriter rulesetWriter = new UnixPrintWriter(new File(rulesetScriptName), "UTF-8"))
						{
							rulesetWriter.println(createRulesetBashJob(jobName));
							final File jobFolderFile = new File(jobName);
							if(!jobFolderFile.exists())
								jobFolderFile.mkdirs();
						}
						
						// Write YML file for a specific ruleset
						final String YMLName = jobName + File.separator + jobName +".yml";
						try (final PrintWriter ymlWriter = new UnixPrintWriter(new File(YMLName), "UTF-8"))
						{
							ymlWriter.println(createYML(jobName));
						}
						
						// Write Docker file for a specific ruleset
						final String dockerName = jobName + File.separator + "Dockerfile";
						try (final PrintWriter dockerWriter = new UnixPrintWriter(new File(dockerName), "UTF-8"))
						{
							dockerWriter.print(beginDockerFile);
							dockerWriter.print("\"--generate-trials\", ");
							dockerWriter.print("\"" + maxMove + "\", ");
							dockerWriter.print("\"" + thinkingTime + "\", ");
							dockerWriter.print("\"" + numPlayout + "\", ");
							dockerWriter.print("\"" + agentName + "\", ");
							dockerWriter.print("\"" + gameName.substring(1) + "\", ");
							dockerWriter.print("\"" + rulesetName + "\"");
							dockerWriter.println(endDockerFile);
						}
						
						System.out.println(createBashJob(bashName) + " " + "written.");
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
	
	/**
	 * @param jobName The name of the job.
	 * @return The bash line in runAll.sh to run the job.
	 */
	public static String createBashJob(final String jobName)
	{
		return "bash run"+jobName+".sh&";
	}
	
	/**
	 * @param jobName The name of the job.
	 * @return The bash file to run the job for a specific ruleset.
	 */
	public static String createRulesetBashJob(final String jobName)
	{
		return "cd "+jobName+"\n"
	    		+ "oc new-build --name "+ jobName+ " --binary\n"
	    		+ "oc start-build "+jobName+" --from-dir=. --follow --wait\n"
	    		+ "oc apply -f "+jobName+".yml\n"
	    		+ "cd ..";
	}
	
	/**
	 * @param jobName The name of the job.
	 * @return The YML file for a specific ruleset.
	 */
	public static String createYML(final String jobName)
	{
		return "apiVersion: batch/v1\n"
	    		+ "kind: Job\n"
	    		+ "metadata:\n"
	    		+ "  name:"+jobName+"\n"
	    		+ "  labels:\n"
	    		+ "    app: \""+jobName+"\"\n"
	    		+ "spec:\n"
	    		+ "  template:\n"
	    		+ "    metadata:\n"
	    		+ "      name: " + jobName +"\n"
	    		+ "    spec:\n"
	    		+ "      serviceAccountName: anyuid\n"
	    		+ "      containers:\n"
	    		+ "        - name: " + jobName+"\n"
	    		+ "          image: image-registry.openshift-image-registry.svc:5000/ludii/"+jobName+":latest\n"
	    		+ "          imagePullPolicy: Always\n"
	    		+ "          # command: [\"--help\"] \n"
	    		+ "          volumeMounts:\n"
	    		+ "            - mountPath: /data\n"
	    		+ "              name: data\n"
	    		+ "      volumes:\n"
	    		+ "        - name: data\n"
	    		+ "          persistentVolumeClaim:\n"
	    		+ "            claimName: ludii-job-storage\n"
	    		+ "      restartPolicy: Never";
	}

}
