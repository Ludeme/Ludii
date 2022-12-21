package reconstruction.output;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import main.FileHandling;
import main.UnixPrintWriter;
import other.GameLoader;

/**
 * Update the GameRulesets Table with the outcome rulesets from the reconstruction process.
 * @author Eric.Piette
 */
public class UpdateReconsGame
{
	// Load ruleset avg common true concepts from specific directory.
	final static String gameRulesetsFilePath = "./res/recons/input/GameRulesets.csv";
	
	// rulesets reconstructed.
	final static String pathReconstructed    = "./res/recons/output/";
	
	// game name.
	final static String gameName             = "Shatranj al-Kabir (Constantinople)";
	
	public static void main(final String[] args)
	{
		updateReconsGame();
	}
	
	/**
	 * Generate the new GameRulesets.csv with the new rulesets.
	 * @param nextId The next id to use.
	 */
	private static void updateReconsGame()
	{
		final String pathFolderReconstrutions = pathReconstructed + gameName + "/";
		
		// Get the current description of the game to reconstruct.
		final String[] choices = FileHandling.listGames();
		String desc = "";
		for (final String fileName : choices)
		{
			if (!fileName.replaceAll(Pattern.quote("\\"), "/").contains(gameName))
				continue;
			
			String path = fileName.replaceAll(Pattern.quote("\\"), "/");
			path = path.substring(path.indexOf("/lud/"));

			String line;
			try
			(
				final InputStream in = GameLoader.class.getResourceAsStream(path);
				final BufferedReader rdr = new BufferedReader(new InputStreamReader(in));
			)
			{
				while ((line = rdr.readLine()) != null)
					desc += line + "\n";
			}
			catch (final IOException e1)
			{
				e1.printStackTrace();
			}
		}
		
		//System.out.println(desc);

		String temp = desc;
		final String separation = "//------------------------------------------------------------------------------";
		final String beforeOptions = temp.substring(0, temp.indexOf("(option"));
		temp = temp.substring(temp.indexOf("(option"));
		
		int countParenthesis = 0;
		int indexChar = 0;
		for(; indexChar < temp.length(); indexChar++)
		{
			if(temp.charAt(indexChar) == '(')
				countParenthesis++;
			else
				if(temp.charAt(indexChar) == ')')
					countParenthesis--;
			
			if(countParenthesis == 0)
				break;
		}
		
		String options = temp.substring(0, indexChar+1);
		
		//System.out.println(options);

		temp = temp.substring(temp.indexOf("(rulesets"));
		countParenthesis = 0;
		indexChar = 0;
		for(; indexChar < temp.length(); indexChar++)
		{
			if(temp.charAt(indexChar) == '(')
				countParenthesis++;
			else
				if(temp.charAt(indexChar) == ')')
					countParenthesis--;
			
			if(countParenthesis == 0)
				break;
		}

		final String rulesets = temp.substring(0, indexChar+1);
		final String metadata = temp.substring(temp.indexOf("(metadata"));
		
		//System.out.println(rulesets);
		
		File folder = new File(pathFolderReconstrutions);
		File[] listOfFiles = folder.listFiles();
		// We sort the files by number.
		List<File> recons = new ArrayList<File>();
		for (int i = 0; i < listOfFiles.length; i++)
			recons.add(listOfFiles[i]);
		Collections.sort(recons, (r1, r2) -> extractInt(r1.getName()) < extractInt(r2.getName()) ? -1 : extractInt(r1.getName()) > extractInt(r2.getName()) ? 1 : 0);
		
		// We update the rulesets and options
		final StringBuffer newRulesets = new StringBuffer("(rulesets {\n");
		final StringBuffer newOptions = new StringBuffer("(option \"Variant\" <Variant> args:{ <variant> }\n{\n");
		for (int i = 0; i < recons.size(); i++) {
			final String reconsName = recons.get(i).getName().substring(0,recons.get(i).getName().length()-4); // 4 is the ".lud"
			
			// Update the rulesets list.
			final String rulesetToAdd = "\n(ruleset \"Ruleset/" + reconsName + " (Reconstructed)\" {\n     \"Variant/"+reconsName+"\"\n})";
			newRulesets.append(rulesetToAdd);
			
			// Get the description of the recons.
			final StringBuffer descReconsBuffer = new StringBuffer("");
			try (BufferedReader br = new BufferedReader(new FileReader(pathFolderReconstrutions + reconsName + ".lud"))) 
			{
				String line = br.readLine();
				while (line != null)
				{
					descReconsBuffer.append(line+"\n");
					line = br.readLine();
				}
				br.close();
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
			
			String descRecons = descReconsBuffer.toString();
			// Remove the metadata if it is there
			if(descRecons.contains("(metadata"))
				descRecons = descRecons.substring(0, descRecons.indexOf("(metadata"));
			// Remove the last closing parenthesis
			descRecons = descRecons.substring(0, descRecons.lastIndexOf(')'));
			// Remove the first line with the (game "...)
			descRecons = descRecons.substring(8 + gameName.length());
			
			// Compute the option to add to the list of option
			final String optionToAdd = "(item \""+reconsName+"\" <\n" + descRecons + "\n > \"The " +  reconsName + " ruleset.\")\n\n";
			newOptions.append(optionToAdd);
		}
		newRulesets.append(rulesets.substring(rulesets.indexOf("(rulesets {") + 11) + "\n\n");
		
		
		int countCurlyBracket = 0;
		indexChar = 0;
		for(; indexChar < options.length(); indexChar++)
		{
			if(options.charAt(indexChar) == '{')
				countCurlyBracket++;
			
			if(countCurlyBracket == 2)
				break;
		}
		options = options.substring(indexChar+1);
		options = options.substring(0,options.length() - 2);
		newOptions.append(options);
		newOptions.append("\n })");
		
		final String newFileContent = beforeOptions + newOptions.toString() + "\n\n" + separation + "\n\n" + newRulesets.toString() + separation + "\n\n " + metadata;
		
		//System.out.println(newFileContent);

		// Write the file.
		final String output = gameName + ".lud";
		try (final PrintWriter writer = new UnixPrintWriter(new File(output), "UTF-8"))
		{
			writer.println(newFileContent);
		}
		catch (FileNotFoundException e1)
		{
			e1.printStackTrace();
		}
		catch (UnsupportedEncodingException e1)
		{
			e1.printStackTrace();
		}
		
		System.out.println("New " + gameName + ".lud generated." );
	}

	//-------------------------------------------------------------------------
	
	/**
	 * @param s The input string.
	 * @return The digits in a string.
	 */
	private static int extractInt(String s) {
        String num = s.replaceAll("\\D", "");
        return num.isEmpty() ? 0 : Integer.parseInt(num);
    }

}