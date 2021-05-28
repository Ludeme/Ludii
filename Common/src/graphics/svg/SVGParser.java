package graphics.svg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;

import graphics.svg.element.BaseElement;
import graphics.svg.element.Element;
import graphics.svg.element.ElementFactory;

//-----------------------------------------------------------------------------

/**
 * Class for parsing SVG files.
 * @author cambolbro
 */
public class SVGParser
{
	private String fileName = "";
	private final SVG svg = new SVG();

	//-------------------------------------------------------------------------

	public SVGParser()
	{
	}
		
	public SVGParser(final String filePath)
	{
		try
		{
			loadAndParse(filePath);
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------

	public String fileName()
	{
		return fileName;
	}

	public SVG svg()
	{
		return svg;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Load and parse SVG content from the specified file.
	 * @param fname
	 * @throws IOException
	 */
	public void loadAndParse(final String fname) throws IOException
	{
		fileName = new String(fname);
	
		// Load the file
		String content = "";

//		BufferedReader reader;
//		reader = new BufferedReader(new FileReader(fileName));
//		
//		// Read its content into a string
//		String line = reader.readLine();
//	    while (line != null) 
//	    {
//	    	content += line;
//	    	line = reader.readLine();
//	    }
//	    reader.close();
	    
	   	final InputStream in = getClass().getResourceAsStream(fileName); 
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in)))
		{
			// Read its content into a string
			String line = reader.readLine();
			while (line != null) 
			{
				content += line;
				line = reader.readLine();
			}
			reader.close();
		}
	    
		//System.out.println("SVG content is: " + content);
		
	    parse(content);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Load the specified SVG content from the file with the given name.
	 * @param content
	 */
	public boolean parse(final String content)
	{	
		svg.clear();

//		// Extract width and height from header
//		if (content.contains(" width="))
//		{
//			final Double result = SVGParser.extractDouble(content, " width=");
//			if (result == null)
//				return false;
//			svg.setWidth(result.doubleValue());
//		}
//
//		if (content.contains(" height="))
//		{
//			final Double result = SVGParser.extractDouble(content, " height=");
//			if (result == null)
//				return false;
//			svg.setHeight(result.doubleValue());
//		}
		
		// Load SVG elements
		for (final Element prototype : ElementFactory.get().prototypes())
		{
			// Load all occurrences of this prototype
			final String label = prototype.label();			
			int pos = 0;
			while (pos < content.length())
			{
				pos = content.indexOf("<"+label, pos);
				if (pos == -1)
					break;

				final int to = content.indexOf(">", pos); 
				if (to == -1)
				{
					System.out.println("* Failed to close expression: " + content.substring(pos));
					break;
				}
				
				String expr = content.substring(pos, to+1);

//				System.out.println("expr: " + expr);
				expr = expr.replaceAll(",", " ");
				expr = expr.replaceAll(";", " ");
				expr = expr.replaceAll("\n", " ");
				expr = expr.replaceAll("\r", " ");
				expr = expr.replaceAll("\t", " ");
				expr = expr.replaceAll("\b", " ");
				expr = expr.replaceAll("\f", " ");
				expr = expr.replaceAll("-", " -");
				
				while (expr.contains("  "))
					expr = expr.replaceAll("  ", " ");

				final Element element = ElementFactory.get().generate(label);
				if (!element.load(expr))
					return false;
				((BaseElement)element).setFilePos(pos);
				svg.elements().add(element);
				
				pos = to;
			}
		}
		sortElements();
		svg.setBounds();
		
//		System.out.println(toString());
		
		return true;
	}

	//-------------------------------------------------------------------------

	/**
	 * Sort elements in order of occurrence.
	 */
	void sortElements()
	{	
		Collections.sort
		(
			svg.elements(), 
			new Comparator<Element>() 
			{
				@Override
				public int compare(final Element a, final Element b)
				{
					final int filePosA = ((BaseElement)a).filePos();
					final int filePosB = ((BaseElement)b).filePos();
					
					if (filePosA < filePosB)
						return -1;
					if (filePosA > filePosB)
						return 1;
					return 0;
				}
			}
		);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param ch
	 * @return Whether ch is possibly part of a numeric string.
	 */
	public static boolean isNumeric(final char ch)
	{
		return ch >= '0' && ch <= '9' || ch == '-' || ch == '.'; 
	}
	
//	public static boolean isDoubleChar(final char ch)
//	{
//		return ch >= '0' && ch <= '9' || ch == '.' || ch == '-';
//	}

	//-------------------------------------------------------------------------

	/**
	 * @param expr
	 * @param from
	 * @return Extract double from expression, else return null.
	 */
	public static Double extractDoubleAt(final String expr, final int from)
	{
//		final StringBuilder sb = new StringBuilder();
//		
//		int c = from;
//		char ch;
//		while (c < expr.length())
//		{
//			ch = expr.charAt(c);
//			if (!isNumeric(ch))
//				break;
//			sb.append(ch);
//			c++;
//		} 

		int c = from;
		while (c < expr.length() && !isNumeric(expr.charAt(c)))
			c++;

		int cc = c+1;
		while (cc < expr.length() && isNumeric(expr.charAt(cc)))
			cc++;
		//cc--;
		final String sub = expr.substring(c, cc);
		
//	System.out.println("sub=" + sub + ", expr=" + expr);

		Double result = null;
		try
		{
			result = Double.parseDouble(sub);
		}
		catch (final Exception e)
		{
			 e.printStackTrace();
		}
		return result;
	}		

	//-------------------------------------------------------------------------

	/**
	 * @param expr
	 * @param heading
	 * @return Extract double from expression, else return null.
	 */
	public static Double extractDouble(final String expr, final String heading)
	{
		int c = 0;
		while (c < expr.length() && !isNumeric(expr.charAt(c)))
			c++;

		int cc = c+1;
		while (cc < expr.length() && isNumeric(expr.charAt(cc)))
			cc++;
		//cc--;
		final String sub = expr.substring(c, cc);
		
//		System.out.println("sub=" + sub + ", expr=" + expr);
		
//		// Extract the substring enclosed by quotation marks
//		final int pos    = expr.indexOf(heading);
//		final int from   = expr.indexOf("\"", pos);     // opening quote
//		final int to     = expr.indexOf("\"", from+1);  // closing quote
//		final String sub = expr.substring(from+1, to);
		
		Double result = null;
		try
		{
			result = Double.parseDouble(sub);
		}
		catch (final Exception e)
		{
			 e.printStackTrace();
		}
		return result;
	}		
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param str
	 * @param pos
	 * @return String at the specified position, else null if none.
	 */
	public static String extractStringAt(final String str, final int pos)
	{
		final StringBuilder sb = new StringBuilder();
		
		if (str.charAt(pos) == '"')
		{
			// Is a string, look for closing quote marks
			for (int c = pos+1; c < str.length() && str.charAt(c) != '"'; c++)
				sb.append(str.charAt(c));
		}
		else
		{
			// Is not a string, look for other terminator
			for (int c = pos; c < str.length() && str.charAt(c) != ';' && str.charAt(c) != ' ' && str.charAt(c) != '"'; c++)
				sb.append(str.charAt(c));
		}
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append(fileName + " has ");
		sb.append(svg);
		
		return sb.toString();
	}

	//-------------------------------------------------------------------------

}
