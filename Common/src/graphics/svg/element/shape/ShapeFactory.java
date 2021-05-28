package graphics.svg.element.shape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import graphics.svg.element.shape.path.Path;

//-----------------------------------------------------------------------------

/**
 * Singleton class that holds a factory method for creating new SVG elements. 
 * @author cambolbro
 */
public class ShapeFactory
{
	// List of concrete classes to be instantiated
	private final static List<Shape> prototypes = new ArrayList<Shape>();
	{
		// Shape prototypes
		prototypes.add(new Circle());
		prototypes.add(new Ellipse());
		prototypes.add(new Line());
		prototypes.add(new Polygon());
		prototypes.add(new Polyline());
		prototypes.add(new Rect());
		prototypes.add(new Path());
	}
	
	// Singleton occurrence of this class
	private static ShapeFactory singleton = null;
	
	//-------------------------------------------------------------------------

	/**
	 * Private constructor: only this class can construct itself.
	 */
	private ShapeFactory() 
	{
		// Nothing to do...
	}
	
	//-------------------------------------------------------------------------

	public static ShapeFactory get()
	{
		if (singleton == null)
			singleton = new ShapeFactory();  // lazy initialisation
		return singleton;
	}
	
	public List<Shape> prototypes()
	{
		return Collections.unmodifiableList(prototypes);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @param label Element type to make.
	 * @return New element of specified type, with fields unset.
	 */
	public Shape generate(final String label)
	{
		for (Shape prototype : prototypes)
			if (prototype.label().equals(label))
				//return prototype.newShape();  // return an unset clone
				return (Shape)prototype.newInstance();  // return an unset clone
		
		System.out.println("* Failed to find prototype for Element " + label + ".");
		return null;
	}
	
	//-------------------------------------------------------------------------

}
