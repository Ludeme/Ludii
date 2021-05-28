package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * Subclass for PrintWriter that will always print Unix line endings.
 * 
 * See: https://stackoverflow.com/a/14749004/6735980
 *
 * @author Dennis Soemers
 */
public class UnixPrintWriter extends PrintWriter
{

	/**
	 * Constructor
	 * @param file
	 * @throws FileNotFoundException
	 */
	public UnixPrintWriter(final File file) throws FileNotFoundException
	{
		super(file);
	}
	
	/**
	 * Constructor
	 * @param file
	 * @param csn
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public UnixPrintWriter(final File file, final String csn) throws FileNotFoundException, UnsupportedEncodingException
	{
		super(file, csn);
	}
	
	@Override
	public void println()
	{
		write('\n');
	}

}
