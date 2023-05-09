package graphics.svg.element.shape.path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//-----------------------------------------------------------------------------

/**
 * Class that holds a factory method for creating new SVG path operations. 
 * @author cambolbro
 */
public class PathOpFactory
{
	// For a good description of path ops, see: https://www.w3.org/TR/SVG/paths.html
	
	// List of concrete classes to be instantiated
	private final static List<PathOp> prototypes = new ArrayList<PathOp>();
	{
		// Path operation prototypes
		prototypes.add(new MoveTo());
		prototypes.add(new LineTo());
		prototypes.add(new HorzLineTo());
		prototypes.add(new VertLineTo());
		prototypes.add(new QuadTo());
		prototypes.add(new CubicTo());
		prototypes.add(new ShortQuadTo());
		prototypes.add(new ShortCubicTo());
		prototypes.add(new Arc());
		prototypes.add(new Close());
	}
	
	// Singleton occurrence of this class
	private static PathOpFactory singleton = null;
	
	//-------------------------------------------------------------------------

	/**
	 * Private constructor: only this class can construct itself.
	 */
	private PathOpFactory() 
	{
		// Nothing to do...
	}
	
	//-------------------------------------------------------------------------

	public static PathOpFactory get()
	{
		if (singleton == null)
			singleton = new PathOpFactory();  // lazy initialisation
		return singleton;
	}
	
	public List<PathOp> prototypes()
	{
		return Collections.unmodifiableList(prototypes);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param label Element type to make.
	 * @return New element of specified type, with fields unset.
	 */
	public PathOp generate(final char label)
	{
		// Find the appropriate prototype
		PathOp prototype = null;
		for (PathOp prototypeN : prototypes)
			if (prototypeN.matchesLabel(label))
			{
				prototype = prototypeN;
				break;
			}
		
		if (prototype == null)
		{
			System.out.println("* Failed to find prototype for PathOp " + label + ".");
			return null;
		}

		final PathOp op = prototype.newInstance();  // create new unset clone
		op.setLabel(label);
		return op;
	}
	
	//-------------------------------------------------------------------------

}
