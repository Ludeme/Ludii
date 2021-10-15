package util;

import java.awt.geom.Point2D;
import java.io.Serializable;

import other.location.Location;

/**
 * World Location of a component
 * 
 * @author Matthew.Stephenson and Eric.Piette
 */
public class WorldLocation implements Serializable
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** Internal location of the component. */
	private final Location location;
	
	/** World position of the component. */
	private final Point2D position;

	//-------------------------------------------------------------------------

	public WorldLocation(final Location location, final Point2D position)
	{
		this.location = location;
		this.position = position;
	}

	//-------------------------------------------------------------------------

	public Location location() 
	{
		return this.location;
	}

	public Point2D position()
	{
		return this.position;
	}

	//--------------------------------------------------------------------------
	
}
