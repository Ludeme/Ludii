package games;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import main.FileHandling;

/**
 * Unit Test to count the number of Tokens for each game
 *
 * @author Eric.Piette
 */
public class TokensTest
{

	@Test
	public void test()
	{
		final File startFolder = new File("../Common/res/lud/");
		final List<File> gameDirs = new ArrayList<>();
		gameDirs.add(startFolder);

		// We compute the .lud files (and not the ludemeplex).
		final List<File> entries = new ArrayList<>();

		for (int i = 0; i < gameDirs.size(); ++i)
		{
			final File gameDir = gameDirs.get(i);

			for (final File fileEntry : gameDir.listFiles())
			{
				if (fileEntry.isDirectory())
				{
					if (fileEntry.getPath().equals("..\\Common\\res\\lud\\plex"))
						continue;

					if (fileEntry.getPath().equals("..\\Common\\res\\lud\\wip"))
						continue;

					if (fileEntry.getPath().equals("..\\Common\\res\\lud\\test"))
						continue;

					if (fileEntry.getPath().equals("..\\Common\\res\\lud\\bad"))
						continue;
					
					// We'll find files that we should be able to compile here
					gameDirs.add(fileEntry);
				}
				else if (fileEntry.getName().endsWith(".lud") || fileEntry.getName().endsWith(".rbg"))
				{
					entries.add(fileEntry);
				}
			}
		}
		
		for (final File fileEntry : entries)
		{
			if (fileEntry.getName().contains(".lud"))
			{
				final String fileName = fileEntry.getPath();
				System.out.println("File: " + fileName);

				// Load the string from file
				String desc = "";
//				String line = null;
				try
				{
//					final FileReader fileReader = new FileReader(fileName);
//					final BufferedReader bufferedReader = new BufferedReader(fileReader);
//					while ((line = bufferedReader.readLine()) != null)
//						desc += line + "\n";
//					bufferedReader.close();
					desc = FileHandling.loadTextContentsFromFile(fileName);				}
				catch (final FileNotFoundException ex)
				{
					System.out.println("Unable to open file '" + fileName + "'");
				}
				catch (final IOException ex)
				{
					System.out.println("Error reading file '" + fileName + "'");
				}

				int tokens = 0;
				boolean onAToken = false;
				String currentToken = "";
				for (int i = 0; i < desc.length(); i++) 
				{
					if (desc.charAt(i) != ' ' && desc.charAt(i) != '(' && desc.charAt(i) != ')' && desc.charAt(i) != '}' && desc.charAt(i) != '{' && desc.charAt(i) != '\n')
					{
						if (!onAToken)
							onAToken = true;
						
						currentToken += desc.charAt(i);
					}
					else
					{
						if (currentToken.equals("metadata"))
						{
							break;
						}
						else if (onAToken) 
						{
							tokens++;
							onAToken = false;
							currentToken = "";
						}
					}

				}
				System.out.println(tokens + " tokens for " + fileName);
			}

			else
			if (fileEntry.getName().contains(".rbg"))
			{
				final String fileName = fileEntry.getPath();
				System.out.println("File: " + fileName);

				// Load the string from file
				String desc = "";
//				String line = null;
				try
				{
//					final FileReader fileReader = new FileReader(fileName);
//					final BufferedReader bufferedReader = new BufferedReader(fileReader);
//					while ((line = bufferedReader.readLine()) != null)
//						desc += line + "\n";
//					bufferedReader.close();
					desc = FileHandling.loadTextContentsFromFile(fileName);	
				}
				catch (final FileNotFoundException ex)
				{
					System.out.println("Unable to open file '" + fileName + "'");
				}
				catch (final IOException ex)
				{
					System.out.println("Error reading file '" + fileName + "'");
				}

				int tokens = 0;
				boolean onAToken = false;
				for(int i = 0 ; i < desc.length() ; i++) 
				{
					if (desc.charAt(i) != ' ' && desc.charAt(i) != '(' && desc.charAt(i) != ')' && desc.charAt(i) != '}' && desc.charAt(i) != '{' && desc.charAt(i) != '$' && desc.charAt(i) != '\n' && desc.charAt(i) != ',' && desc.charAt(i) != ';' && desc.charAt(i) != '=' && desc.charAt(i) != '=' && desc.charAt(i) != '^' && desc.charAt(i) != '+' && desc.charAt(i) != '-' && desc.charAt(i) != '/' && desc.charAt(i) != '*' && desc.charAt(i) != '\n')
					{
						if (!onAToken)
							onAToken = true;
					}
					else
					{
						if (onAToken) 
						{
							tokens++;
							onAToken=false;
						}
					}

				}
				System.out.println(tokens + " tokens for " + fileName);
			}
		}
	}

}
