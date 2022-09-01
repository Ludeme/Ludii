package other;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

//-----------------------------------------------------------------------------

/**
 * Unit Test to check that all our .lud files only contain ASCII characters
 *
 * @author Dennis Soemers
 */
public class TestLudFilesOnlyASCII
{
	@Test
	@SuppressWarnings("static-method")
	public void test() throws IOException
	{
		final File startFolder = new File("../Common/res/lud/");
		final List<File> gameDirs = new ArrayList<>();
		gameDirs.add(startFolder);

		final List<File> entries = new ArrayList<>();

		for (int i = 0; i < gameDirs.size(); ++i)
		{
			final File gameDir = gameDirs.get(i);

			for (final File fileEntry : gameDir.listFiles())
			{
				if (fileEntry.isDirectory())
				{
					gameDirs.add(fileEntry);
				}
				else
				{
					entries.add(fileEntry);
				}
			}
		}

		// Test of compilation for each of them.
		for (final File fileEntry : entries)
		{
			final String fileName = fileEntry.getPath();
			
			System.out.println("File: " + fileName);

			try (final BufferedReader br = new BufferedReader(new FileReader(fileName)))
			{
				int lineNumber = 1;
				while (true)
				{
					final String line = br.readLine();
					
					if (line == null)
						break;
					
					for (int i = 0; i < line.length(); ++i)
					{
						final char c = line.charAt(i);
						if (c >= 128)
						{
							System.err.println("Non-ASCII character in line " + lineNumber + ": " + Character.toString(c) + " (" + (i + 1) + "th character)");
							fail();
						}
					}
					
					++lineNumber;
				}
			}	
		}
	}
}
