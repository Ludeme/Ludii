package graphics.svg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import graphics.svg.SVGPathOp.PathOpType;
import main.StringRoutines;
import main.math.MathRoutines;

//-----------------------------------------------------------------------------

/**
 * Create an image from an SVG file.
 * @author cambolbro and Matthew
 */
public class SVGtoImage
{
	public static final char[] SVG_Symbols =
	{
		'a', 'c', 'h', 'l', 'm', 'q', 's', 't', 'v', 'z'
	};

	//-------------------------------------------------------------------------

	public SVGtoImage()
	{
	}

	//-------------------------------------------------------------------------

	/**
	 * @param ch
	 * @return Whether the specified char, lowercased, is an SVG symbol.
	 */
	public static boolean isSVGSymbol(final char ch)
	{
		final char chLower = Character.toLowerCase(ch);
		for (int s = 0; s < SVG_Symbols.length; s++)
			if (chLower == SVG_Symbols[s])
				return true;
		return false;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Get the SVG string from a given SVG filePath.
	 * @param filePath
	 * @return SVG string.
	 */
	public static String getSVGString(final String filePath)
	{
		final BufferedReader reader = getBufferedReaderFromImagePath(filePath);
		
		// Load the string from file
		String str = "";
		String line = null;
		try 
		{
			while ((line = reader.readLine()) != null)
				str += line + "\n";
			reader.close();
		} catch (final IOException e) 
		{
			e.printStackTrace();
		}

		return str;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Load SVG from file path and render.
	 * @param g2d
	 * @param filePath
	 * @param rectangle
	 * @param borderColour
	 * @param fillColour
	 * @param rotation
	 */
	public static void loadFromFilePath
	(
		final Graphics2D g2d, final String filePath, final Rectangle2D rectangle,
		final Color borderColour, final Color fillColour, final int rotation
	)
	{
		final BufferedReader reader = getBufferedReaderFromImagePath(filePath);
		loadFromReader(g2d, reader, rectangle, borderColour, fillColour, rotation);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Load SVG from bufferedReader and render.
	 * Specify how much to shift the image from the center point using the x and y parameters.
	 * @param g2d
	 * @param bufferedReader
	 * @param borderColour
	 * @param fillColour
	 */
	public static void loadFromReader
	(
		final Graphics2D g2d, final BufferedReader bufferedReader, final Rectangle2D rectangle,
		final Color borderColour, final Color fillColour, final int rotation
	)
	{
		// Load the string from file
		String svg = "";
		String line = null;
		try
		{
			while ((line = bufferedReader.readLine()) != null)
				svg += line + "\n";
			bufferedReader.close();
		}
		catch (final Exception ex)
		{
			ex.printStackTrace();
		}

		loadFromSource(g2d, svg, rectangle, borderColour, fillColour, rotation);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Load SVG from the SVG source string.
	 * Specify how much to shift the image from the center point using the x and y parameters.
	 * @param g2d
	 * @param svg
	 * @param borderColour
	 * @param fillColour
	 * @param rotation
	 */
	public static void loadFromSource
	(
		final Graphics2D g2d, final String svg, final Rectangle2D rectangle,
			final Color borderColour, final Color fillColour, final int rotation
	)
	{
		final SVGtoImage temp = new SVGtoImage();
		
		final List<List<SVGPathOp>> paths = new ArrayList<>();
		final Rectangle2D.Double bounds = new Rectangle2D.Double();

		if (temp.parse(svg, paths))  //, bounds))
		{
			temp.findBounds(paths, bounds);
			temp.render
			(
				g2d, rectangle, borderColour, fillColour,
				paths, bounds, rotation
			);
		}
	}

	//-------------------------------------------------------------------------

	/**
	 * Get SVG Bounds
	 * @return Bounds of SVG file.
	 */
	public static Rectangle2D getBounds
	(
		final String filePath, final int imgSz
	)
	{
		final BufferedReader reader = getBufferedReaderFromImagePath(filePath);
		return getBounds(reader, imgSz);
	}

	/**
	 * Get SVG Bounds
	 * @return Bounds of SVG file.
	 */
	public static Rectangle2D getBounds
	(
		final BufferedReader reader, final int imgSz
	)
	{
		final SVGtoImage temp = new SVGtoImage();

		// Load the string from file
		String str = "";
		String line = null;
		try
		{
			while ((line = reader.readLine()) != null)
				str += line + "\n";
			reader.close();
		}
		catch (final Exception ex)
		{
			ex.printStackTrace();
		}

		final List<List<SVGPathOp>> paths = new ArrayList<>();
		final Rectangle2D.Double bounds = new Rectangle2D.Double();

		if (temp.parse(str, paths))  //, bounds))
		{
			temp.findBounds(paths, bounds);

			final int x0 = (int)(bounds.getX()) - 1;
			final int x1 = (int)(bounds.getX() + bounds.getWidth()) + 1;
			final int sx = x1 - x0;

			final int y0 = (int)(bounds.getY()) - 1;
			final int y1 = (int)(bounds.getY() + bounds.getHeight()) + 1;
			final int sy = y1 - y0;

			final double scale = imgSz / (double)Math.max(sx, sy);

			bounds.width = (int)(scale * sx + 0.5);
			bounds.height = (int)(scale * sy + 0.5);

			return bounds;
		}

		return null;
	}

	/**
	 * Get SVG Bounds
	 * @return Desired scale for SVG file.
	 */
	public static double getDesiredScale
	(
		final String filePath, final int imgSz
	)
	{
		final BufferedReader reader = getBufferedReaderFromImagePath(filePath);
		
		try 
		{
			final SVGtoImage temp = new SVGtoImage();
	
			// Load the string from file
			String str = "";
			String line = null;
			while ((line = reader.readLine()) != null)
				str += line + "\n";
			reader.close();
	
			final List<List<SVGPathOp>> paths = new ArrayList<>();
			final Rectangle2D.Double bounds = new Rectangle2D.Double();
	
			if (temp.parse(str, paths))  //, bounds))
			{
				temp.findBounds(paths, bounds);
	
				final int x0 = (int)(bounds.getX()) - 1;
				final int x1 = (int)(bounds.getX() + bounds.getWidth()) + 1;
				final int sx = x1 - x0;
	
				final int y0 = (int)(bounds.getY()) - 1;
				final int y1 = (int)(bounds.getY() + bounds.getHeight()) + 1;
				final int sy = y1 - y0;
	
				return imgSz / (double)Math.max(sx, sy);
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}

		return 0;
	}

	//----------------------------------------------------------------------------

	boolean parse
	(
		final String in, final List<List<SVGPathOp>> paths
	)
	{
		paths.clear();
		
		String str = new String(in);
		while (str.contains("<path"))
		{
			final int c = str.indexOf("<path");
			final int cc = StringRoutines.matchingBracketAt(str, c);
			
			final String pathStr = str.substring(c, cc+1);
			
			int d = pathStr.indexOf("d=");
			while (d < pathStr.length() && pathStr.charAt(d) != '"')
				d++;
			final int dd = StringRoutines.matchingQuoteAt(pathStr, d);
			
			final String data = pathStr.substring(d+1, dd);
			
			// Process path data
			final List<String> tokens = tokenise(data);
					
			processPathData(tokens, paths);
			
			str = str.substring(cc + 1);		
		}

		return true;
	}
	
	//-------------------------------------------------------------------------

	List<String> tokenise(final String data)
	{
		final List<String> tokens = new ArrayList<String>();
		
		int c = 0;
		while (c < data.length())
		{
			final char ch = data.charAt(c);
			if (isSVGSymbol(ch))
			{
				// Create token for SVG symbol
				tokens.add(new String("" + ch));
			}
			else if (StringRoutines.isNumeric(ch))
			{
				// Create token for number
				int cc = c;
				int numDots = 0;
				
				while (cc < data.length()-1 && StringRoutines.isNumeric(data.charAt(cc+1)))
				{
					if (data.charAt(cc) == '.')
					{
						// Second decimal point in number: is actually start of next number
						if (numDots > 0)
						{
							cc--;
							break;
						}
						numDots++;
					}
					cc++;
					
					if (cc < data.length() - 1 && cc > c+1 && data.charAt(cc) != 'e' && data.charAt(cc+1) == '-')
						break;  // leading '-' of next number may run on directly
					
					if (cc > c && data.charAt(cc-1) != 'e' && data.charAt(cc) == '-')
					{
						// Special case: Single digit directly followed by leading '-' of next number
						cc--;
						break;  
					}
				}
				final String token = data.substring(c, cc+1);
				if (token.contains("e"))
					tokens.add("0");  // involves power of 10, assume is negligibly small
				else
					tokens.add(token);
				c = cc;  // move to end of numeric sequence
			}
			else if (ch == '<')
			{
				final int cc = StringRoutines.matchingBracketAt(data, c);
				c = cc;  // move to end of bracketed clause
			}
			c++;
		}

		return tokens;
	}
	
	//-------------------------------------------------------------------------

	boolean processPathData(final List<String> tokens, final List<List<SVGPathOp>> paths)
	{
		final List<SVGPathOp> path = new ArrayList<>();
		paths.add(path);
		
		char lastOperator = '?';

		int s = 0;
		while (s < tokens.size())
		{
			String token = tokens.get(s);
		
			if (token.isEmpty())
			{
				//System.out.println("** Unexpected empty string.");
				s++;
				continue;
			}

			// Check whether this token is an SVG symbol
			char ch = token.charAt(0);
			final boolean hasSymbol = (token.length() == 1 && isSVGSymbol(ch));
			if (hasSymbol)
			{
				s++;  // step past symbol token to the next (numeric) token
				if (s >= tokens.size())
					return true;  // reached final token
				token = tokens.get(s);
			}
			else
			{
				ch = lastOperator;  // use the last symbol (should already be at numeric token)
			}
			lastOperator = ch;
			
			// **
			// ** Note that token can validly be non-numeric, 
			// ** if it is a MoveTo just after a ClosePath.
			// **
			
			switch (ch)
			{
			case 'a':
			case 'A':
			{
				// Arc to: consume next seven numbers
				if (s >= tokens.size() - 7)
				{
					// System.out.println("** Not enough points for an ArcTo.");
					return false;
				}
				final SVGPathOp op = new SVGPathOp
				(
					PathOpType.ArcTo,
					ch == 'A',
					new String[]
					{
						tokens.get(s),   tokens.get(s+1),  // rx, ry
						tokens.get(s+5), tokens.get(s+6),  // x, y
						tokens.get(s+2), tokens.get(s+3), tokens.get(s+4)
					}
				);
				path.add(op);
				s += 7;
				break;
			}
			case 'm':
			case 'M':
			{
				// Move to: consume next two numbers
				if (s >= tokens.size() - 2)
				{
					// System.out.println("** Not enough points for a MoveTo.");
					return false;
				}
				final SVGPathOp op = new SVGPathOp
				(
					PathOpType.MoveTo,
					ch == 'M',
					new String[] { tokens.get(s), tokens.get(s+1) }
				);
				path.add(op);
				s += 2;
				break;
			}
			case 'l':
			case 'L':
			{
				// Line to: consume next two numbers
				if (s >= tokens.size() - 2)
				{
					// System.out.println("** Not enough points for a LineTo.");
					return false;
				}
				final SVGPathOp op = new SVGPathOp
				(
					PathOpType.LineTo,
					ch == 'L',
					new String[] { tokens.get(s), tokens.get(s+1) }
				);
				path.add(op);
				s += 2;
				break;
			}
			case 'h':
			case 'H':
			{
				// H Line to: consume next number
				if (s >= tokens.size() - 1)
				{
					// System.out.println("** Not enough points for a HLineTo.");
					return false;
				}
				final SVGPathOp op = new SVGPathOp
				(
					PathOpType.HLineTo,
					ch == 'H',
					new String[] { tokens.get(s), "0" }
				);
				path.add(op);
				s += 1;
				break;
			}
			case 'v':
			case 'V':
			{
				// V Line to: consume next number
				if (s >= tokens.size() - 1)
				{
					// System.out.println("** Not enough points for a VLineTo.");
					return false;
				}
				final SVGPathOp op = new SVGPathOp
				(
					PathOpType.VLineTo,
					ch == 'V',
					new String[] { "0", tokens.get(s) }
				);
				path.add(op);
				s += 1;
				break;
			}
			case 'q':
			case 'Q':
			{
				// Quadratic to: consume next four numbers
				if (s >= tokens.size() - 4)
				{
					// System.out.println("** Not enough points for a QuadraticTo.");
					return false;
				}
				final SVGPathOp op = new SVGPathOp
				(
					PathOpType.QuadraticTo,
					ch == 'Q',
					new String[] { tokens.get(s), tokens.get(s+1), tokens.get(s+2), tokens.get(s+3) }
				);
				path.add(op);
				s += 4;
				break;
			}
			case 'c':
			case 'C':
			{
				// Curve to: consume next six numbers
				if (s >= tokens.size() - 6)
				{
					// System.out.println("** Not enough points for a CurveTo.");
					return false;
				}
				final SVGPathOp op = new SVGPathOp
				(
					PathOpType.CurveTo,
					ch == 'C',
					new String[] { tokens.get(s), tokens.get(s+1), tokens.get(s+2), tokens.get(s+3), tokens.get(s+4), tokens.get(s+5) }
				);
				path.add(op);
				s += 6;
				break;
			}
			case 's':
			case 'S':
			{
				// Short curve to: consume next four numbers
				if (s >= tokens.size() - 4)
				{
					// System.out.println("** Not enough points for a ShortCurveTo.");
					return false;
				}
				final SVGPathOp op = new SVGPathOp
				(
					PathOpType.ShortCurveTo,
					ch == 'S',
					new String[] { tokens.get(s), tokens.get(s+1), tokens.get(s+2), tokens.get(s+3) }
				);
				path.add(op);
				s += 4;
				break;
			}
			case 't':
			case 'T':
			{
				// Short quadratic to: consume next two numbers
				if (s >= tokens.size() - 2)
				{
					// System.out.println("** Not enough points for a ShortQuadraticTo.");
					return false;
				}
				final SVGPathOp op = new SVGPathOp
				(
					PathOpType.ShortQuadraticTo,
					ch == 'T',
					new String[] { tokens.get(s), tokens.get(s+1) }
				);
				path.add(op);
				s += 2;
				break;
			}
			case 'z':
			case 'Z':
			{
				// Closepath to: consume this item
				final SVGPathOp op = new SVGPathOp
				(
					PathOpType.ClosePath,
					ch == 'Z',
					null
				);
				path.add(op);
				//s++;
				break;
			}
			default:
				// System.out.println("** Path entry with no op: " + tokens.get(s) + " " +
				// tokens.get(s+1));
				return false;
			}
		}
		
		return true;
	}

	//-------------------------------------------------------------------------

	/**
	 * @param in
	 * @param target
	 * @return Number corresponding to label in string, else 0.
	 */
	int findPositiveInteger(final String in, final String label)
	{
		int from = in.indexOf(label);  //findSubstring(in, label);
		if (from == -1)
		{
			System.out.println("** Failed to find '" + label + "' in '" + in + "'.");
			return 0;
		}
		from++;  // step past opening quote

		String str = "";

		int cc = from + label.length();
		while (cc < in.length() && in.charAt(cc) >= '0' && in.charAt(cc) <= '9')
			str += in.charAt(cc++);

		int value = 0;
		try
		{
			value = Integer.parseInt(str);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		return value;
	}

	//-------------------------------------------------------------------------

	public void findBounds(final List<List<SVGPathOp>> paths, final Rectangle2D.Double bounds)
	{
		double minX =  1000000;
		double minY =  1000000;
		double maxX = -1000000;
		double maxY = -1000000;

		double lastX = 0;
		double lastY = 0;

		double x=0, y=0, x1=0, y1=0, x2=0, y2=0;

		for (final List<SVGPathOp> path : paths)
		{
			lastX = 0;
			lastY = 0;

			for (final SVGPathOp op : path)
			{
				switch (op.type())
				{
				case ArcTo:
					final double rx = op.pts().get(0).x;
					final double ry = op.pts().get(0).y;
					x = op.pts().get(1).x + (op.absolute() ? 0 : lastX);
					y = op.pts().get(1).y + (op.absolute() ? 0 : lastY);
					lastX = x + rx;
					lastY = y;

					x1 = x - rx;
					y1 = y - ry;
					x2 = x + rx;
					y2 = y + ry;

					if (x1 < minX) minX = x1;
					if (y1 < minY) minY = y1;
					if (x1 > maxX) maxX = x1;
					if (y1 > maxY) maxY = y1;

					if (x2 < minX) minX = x2;
					if (y2 < minY) minY = y2;
					if (x2 > maxX) maxX = x2;
					if (y2 > maxY) maxY = y2;

					break;
				case MoveTo:
					x = op.pts().get(0).x + (op.absolute() ? 0 : lastX);
					y = op.pts().get(0).y + (op.absolute() ? 0 : lastY);
					lastX = x;
					lastY = y;
					break;
				case LineTo:
					x = op.pts().get(0).x + (op.absolute() ? 0 : lastX);
					y = op.pts().get(0).y + (op.absolute() ? 0 : lastY);
					lastX = x;
					lastY = y;
					break;
				case HLineTo:
					x = op.pts().get(0).x + (op.absolute() ? 0 : lastX);
					lastX = x;
					break;
				case VLineTo:
					y = op.pts().get(0).y + (op.absolute() ? 0 : lastY);
					lastY = y;
					break;
				case QuadraticTo:
					x = op.pts().get(1).x + (op.absolute() ? 0 : lastX);
					y = op.pts().get(1).y + (op.absolute() ? 0 : lastY);
					lastX = x;
					lastY = y;

					x1 = op.pts().get(0).x + (op.absolute() ? 0 : lastX);
					y1 = op.pts().get(0).y + (op.absolute() ? 0 : lastY);

					break;
				case CurveTo:
					x = op.pts().get(2).x + (op.absolute() ? 0 : lastX);
					y = op.pts().get(2).y + (op.absolute() ? 0 : lastY);
					lastX = x;
					lastY = y;

					x1 = op.pts().get(0).x + (op.absolute() ? 0 : lastX);
					y1 = op.pts().get(0).y + (op.absolute() ? 0 : lastY);
					x2 = op.pts().get(1).x + (op.absolute() ? 0 : lastX);
					y2 = op.pts().get(1).y + (op.absolute() ? 0 : lastY);

					break;
				case ShortQuadraticTo:
					x = op.pts().get(0).x + (op.absolute() ? 0 : lastX);
					y = op.pts().get(0).y + (op.absolute() ? 0 : lastY);
					lastX = x;
					lastY = y;
					break;
				case ShortCurveTo:
					x = op.pts().get(1).x + (op.absolute() ? 0 : lastX);
					y = op.pts().get(1).y + (op.absolute() ? 0 : lastY);
					lastX = x;
					lastY = y;

					x1 = op.pts().get(0).x + (op.absolute() ? 0 : lastX);
					y1 = op.pts().get(0).y + (op.absolute() ? 0 : lastY);

					break;
				case ClosePath:
					x = lastX;
					y = lastY;
					break;
				default:
					// Do nothing
				}

				if (x < minX)
					minX = x;

				if (y < minY)
					minY = y;

				if (x > maxX)
					maxX = x;

				if (y > maxY)
					maxY = y;
			}
		}

		bounds.setRect(minX, minY, maxX-minX, maxY-minY);
	}

	//-------------------------------------------------------------------------

	final boolean verbose = false;

	/**
	 * Render the SVG code just parsed to a target area.
	 */
	public void render
	(
		final Graphics2D g2d, final Rectangle2D targetArea,
		final Color borderColour, final Color fillColour, 
		final List<List<SVGPathOp>> paths, final Rectangle2D.Double bounds, 
		final int rotation
	)
	{
		final double targetWidth  = targetArea.getWidth();
		final double targetHeight = targetArea.getHeight();
		final double targetX = targetArea.getX();
		final double targetY = targetArea.getY();
		
		double x0 = bounds.getX() - 1;
		final double x1 = bounds.getX() + bounds.getWidth() + 1;
		final double sx = x1 - x0;

		double y0 = bounds.getY() - 1;
		final double y1 = bounds.getY() + bounds.getHeight() + 1;
		final double sy = y1 - y0;
		
		final double maxDim = Math.max(targetWidth, targetHeight);

		final double scaleX = targetWidth  / Math.max(sx, sy);
		final double scaleY = targetHeight / Math.max(sx, sy);
		
		x0 -= Math.max(sy - sx, 0) / 2.0;
		y0 -= Math.max(sx - sy, 0) / 2.0;

		// centering if possible
//		if (g2d instanceof SVGGraphics2D) {
//			final SVGGraphics2D svg2d = (SVGGraphics2D) g2d;
//			x0 -= (((svg2d.getWidth() - imgSx) / 2) + x) / scaleX;
//			y0 -= (((svg2d.getHeight() - imgSy) / 2) + y) / scaleY;
//		} else {
//			x0 -= x / scaleX;
//			y0 -= y / scaleY;
//		}
		
		x0 -= targetX / scaleX;
		y0 -= targetY / scaleY;

		// Rotate the image if needed.
		if (rotation != 0)
			g2d.rotate(Math.toRadians(rotation), targetX + maxDim/2, targetY + maxDim/2);

		// Pass 1: Fill footprint in player colour
		if (fillColour != null)
			renderPaths(g2d, x0, y0, scaleX, scaleY, fillColour, null, paths);  //, bounds);

		// Pass 2: Fill border in border colour
		if (borderColour != null)
			renderPaths(g2d, x0, y0, scaleX, scaleY, null, borderColour, paths);  //, bounds);
		
		// Need to rotate back afterwards.
		if (rotation != 0)
			g2d.rotate(-Math.toRadians(rotation), targetX + maxDim/2, targetY + maxDim/2);
	}

	//-------------------------------------------------------------------------

	void renderPaths
	(
		final Graphics2D g2d, final double x0, final double y0, final double scaleX, final double scaleY,
		final Color fillColour, final Color borderColour,
		final List<List<SVGPathOp>> paths  //, final Rectangle2D.Double bounds
	)
	{
		double x=0, y=0, x1, y1, x2, y2, curX, curY, oldX, oldY;

		for (final List<SVGPathOp> opList : paths)
		{
			final GeneralPath path = new GeneralPath();
			//path.moveTo(0, 0);
			
			final List<Point2D> pts = new ArrayList<>();

			Point2D prev = null;
			
			double startX = 0;
			double startY = 0;

			double lastX = 0;
			double lastY = 0;

			for (final SVGPathOp op : opList)
			{
				Point2D current = path.getCurrentPoint();
				if (current == null)
					current = new Point2D.Double(0, 0);

				//System.out.println("current.x=" + (current == null ? "null" : current.getX()) + ", lastX=" + lastX);
				
				switch (op.type())
				{
				case ArcTo:
					System.out.println("** Warning: Path ArcTo not fully supported yet.");

					x1 = (lastX - x0) * scaleX;
					y1 = (lastY - y0) * scaleY;

					x2 = (op.pts().get(1).x + (op.absolute() ? 0 : lastX) - x0) * scaleX;
					y2 = (op.pts().get(1).y + (op.absolute() ? 0 : lastY) - y0) * scaleY;

					final double rx = op.pts().get(0).x * scaleX;
					final double ry = op.pts().get(0).y * scaleY;

					final double theta = op.xAxisRotation();
					final double fa = op.largeArcSweep();
					final double fs = op.sweepFlag();

					final double xx1 =  Math.cos(theta) * (x1 - x2) / 2.0 + Math.sin(theta) * (y1 - y2) / 2.0;
					final double yy1 = -Math.sin(theta) * (x1 - x2) / 2.0 + Math.cos(theta) * (y1 - y2) / 2.0;

					final int signF = (fa == fs) ? 1 : -1;

					final double term = Math.sqrt
										(
											(rx * rx * ry * ry - rx * rx *yy1 * yy1 - ry * ry * xx1 * xx1)
											/
											(rx * rx * yy1 * yy1 + ry * ry * xx1 * xx1)
										);

					final double ccx = 	signF * term * (rx * yy1 / ry);
					final double ccy = 	signF * term * (- ry * xx1 / rx);

					final double cx = (Math.cos(theta) * ccx - Math.sin(theta) * ccy + (x1 + x2) / 2.0 - x0) * scaleX;
					final double cy = (Math.sin(theta) * ccx + Math.cos(theta) * ccy + (y1 + y2) / 2.0 - y0) * scaleY;

					path.append(new Ellipse2D.Double(cx-rx, cy-ry, 2*rx, 2*ry), true);
					path.lineTo(x2, y2);

					lastX = x2 / scaleX + x0;
					lastY = y2 / scaleY + y0;

					prev = new Point2D.Double(x2, y2);
					pts.add(op.pts().get(1));  // only include destination point, not control points

					if (verbose)
						System.out.println(String.format("A%.1f %.1f %.1f %.1f", rx, ry, x, y));
					break;
				case MoveTo:
					if (fillColour != null && !pts.isEmpty())
					{
						// Draw the path so far
						if (MathRoutines.isClockwise(pts))
						{
							path.closePath();
							g2d.setPaint(fillColour);
							g2d.fill(path);
						}
						pts.clear();
						path.reset();
						//path.moveTo((lastX - x0) * scale, (lastY - y0) * scale);
					}

					x = (op.pts().get(0).x + (op.absolute() ? 0 : lastX) - x0) * scaleX;
					y = (op.pts().get(0).y + (op.absolute() ? 0 : lastY) - y0) * scaleY;

					lastX = op.pts().get(0).x + (op.absolute() ? 0 : lastX);
					lastY = op.pts().get(0).y + (op.absolute() ? 0 : lastY);

					startX = lastX;
					startY = lastY;			
					
					path.moveTo(x, y);

					prev = new Point2D.Double(x, y);
					pts.add(op.pts().get(0));

					if (verbose)
						System.out.println(String.format("M%.1f %.1f", x, y));
					break;
				case LineTo:
					x = (op.pts().get(0).x + (op.absolute() ? 0 : lastX) - x0) * scaleX;
					y = (op.pts().get(0).y + (op.absolute() ? 0 : lastY) - y0) * scaleY;

					lastX = op.pts().get(0).x + (op.absolute() ? 0 : lastX);
					lastY = op.pts().get(0).y + (op.absolute() ? 0 : lastY);

					path.lineTo(x, y);

					prev = new Point2D.Double(current.getX(), current.getY());
					pts.add(op.pts().get(0));

					if (verbose)
						System.out.println(String.format("L%.1f %.1f", x, y));
					break;
				case HLineTo:
					x = (op.pts().get(0).x + (op.absolute() ? 0 : lastX) - x0) * scaleX;
					y = current.getY();
					
					lastX = op.pts().get(0).x + (op.absolute() ? 0 : lastX);
					
					path.lineTo(x, y);

					prev = new Point2D.Double(current.getX(), current.getY());
					pts.add(op.pts().get(0));

					if (verbose)
						System.out.println(String.format("H%.1f %.1f", x, y));
					break;
				case VLineTo:
					x = current.getX();
					y = (op.pts().get(0).y + (op.absolute() ? 0 : lastY) - y0) * scaleY;

					lastY = op.pts().get(0).y + (op.absolute() ? 0 : lastY);

					path.lineTo(x, y);

					prev = new Point2D.Double(current.getX(), current.getY());
					pts.add(op.pts().get(0));

					if (verbose)
						System.out.println(String.format("V%.1f %.1f", x, y));
					break;
				case QuadraticTo:
					x1 = (op.pts().get(0).x + (op.absolute() ? 0 : lastX) - x0) * scaleX;
					y1 = (op.pts().get(0).y + (op.absolute() ? 0 : lastY) - y0) * scaleY;
					x  = (op.pts().get(1).x + (op.absolute() ? 0 : lastX) - x0) * scaleX;
					y  = (op.pts().get(1).y + (op.absolute() ? 0 : lastY) - y0) * scaleY;

					lastX = op.pts().get(1).x + (op.absolute() ? 0 : lastX);
					lastY = op.pts().get(1).y + (op.absolute() ? 0 : lastY);

					path.quadTo(x1, y1, x, y);

					prev = new Point2D.Double(x1, y1);
					pts.add(op.pts().get(1));  // only include destination point, not control points

					if (verbose)
						System.out.println(String.format("Q%.1f %.1f %.1f %.1f", x1, y1, x, y));
					break;
				case CurveTo:
					x1 = (op.pts().get(0).x + (op.absolute() ? 0 : lastX) - x0) * scaleX;
					y1 = (op.pts().get(0).y + (op.absolute() ? 0 : lastY) - y0) * scaleY;
					x2 = (op.pts().get(1).x + (op.absolute() ? 0 : lastX) - x0) * scaleX;
					y2 = (op.pts().get(1).y + (op.absolute() ? 0 : lastY) - y0) * scaleY;
					x  = (op.pts().get(2).x + (op.absolute() ? 0 : lastX) - x0) * scaleX;
					y  = (op.pts().get(2).y + (op.absolute() ? 0 : lastY) - y0) * scaleY;

					lastX = op.pts().get(2).x + (op.absolute() ? 0 : lastX);
					lastY = op.pts().get(2).y + (op.absolute() ? 0 : lastY);

					path.curveTo(x1, y1, x2, y2, x, y);

					prev = new Point2D.Double(x2, y2);
					pts.add(op.pts().get(2));  // only include destination point, not control points

					if (verbose)
						System.out.println(String.format("C%.1f %.1f %.1f %.1f %.1f %.1f", x1, y1, x2, y2, x, y));
					break;
				case ShortQuadraticTo:
					x  = (op.pts().get(0).x + (op.absolute() ? 0 : lastX) - x0) * scaleX;
					y  = (op.pts().get(0).y + (op.absolute() ? 0 : lastY) - y0) * scaleY;

					lastX = op.pts().get(0).x + (op.absolute() ? 0 : lastX);
					lastY = op.pts().get(0).y + (op.absolute() ? 0 : lastY);

					// Calculate x1 and y1:
					// 		(newx1, newy1) = (curx - (oldx2 - curx), cury - (oldy2 - cury))
					//                	   = (2*curx - oldx2, 2*cury - oldy2)

					curX = current.getX();
					curY = current.getY();

					oldX = prev.getX();
					oldY = prev.getY();

					x1 = 2 * curX - oldX;
					y1 = 2 * curY - oldY;

					path.quadTo(x1, y1, x, y);

					prev = new Point2D.Double(x1, y1);
					pts.add(op.pts().get(1));  // only include destination point, not control points

					if (verbose)
						System.out.println(String.format("Q%.1f %.1f %.1f %.1f", x1, y1, x, y));
					break;
				case ShortCurveTo:
					x2 = (op.pts().get(0).x + (op.absolute() ? 0 : lastX) - x0) * scaleX;
					y2 = (op.pts().get(0).y + (op.absolute() ? 0 : lastY) - y0) * scaleY;
					x  = (op.pts().get(1).x + (op.absolute() ? 0 : lastX) - x0) * scaleX;
					y  = (op.pts().get(1).y + (op.absolute() ? 0 : lastY) - y0) * scaleY;

					lastX = op.pts().get(1).x + (op.absolute() ? 0 : lastX);
					lastY = op.pts().get(1).y + (op.absolute() ? 0 : lastY);

					// Calculate x1 and y1:
					// 		(newx1, newy1) = (curx - (oldx2 - curx), cury - (oldy2 - cury))
					//                	   = (2*curx - oldx2, 2*cury - oldy2)

					curX = current.getX();
					curY = current.getY();

					oldX = prev.getX();
					oldY = prev.getY();

					x1 = 2 * curX - oldX;
					y1 = 2 * curY - oldY;

					path.quadTo(x1, y1, x, y);

					prev = new Point2D.Double(x1, y1);
					pts.add(op.pts().get(1));  // only include destination point, not control points

					if (verbose)
						System.out.println(String.format("Q%.1f %.1f %.1f %.1f", x2, y2, x, y));
					break;
				case ClosePath:
					path.closePath();

					if (fillColour != null)
					{
						// Fill the full image footprint in the player's colour
						g2d.setPaint(fillColour);
						g2d.fill(path);

						path.reset();		
						pts.clear();
						prev = null;
					}

					lastX = startX;
					lastY = startY;
					
					if (verbose)
						System.out.println("Z");
					break;
				default:
					// Do nothing
				}
			}

			if (fillColour == null)
			{
				// Only fill the border path
				g2d.setPaint(borderColour);
				g2d.fill(path);
			}
		}
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Returns the BufferedReader object for a specified filePath.
	 * Works for both resource files, and external files.
	 * @param filePath
	 * @return
	 */
	private static BufferedReader getBufferedReaderFromImagePath(final String filePath)
	{
		try
		{
			final InputStream in = SVGtoImage.class.getResourceAsStream(filePath);
			return new BufferedReader(new InputStreamReader(in));
		}
		catch (final Exception e)
		{
			// Could not find SVG within resource folder, might be an absolute path.
			try 
			{
				return new BufferedReader(new FileReader(filePath));
			} 
			catch (final FileNotFoundException e1) 
			{
				e.printStackTrace();
				e1.printStackTrace();
			}
		}
		
		return null;
	}
	
	//-------------------------------------------------------------------------

}
