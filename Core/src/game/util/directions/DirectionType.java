package game.util.directions;

/**
 * Associates a direction to an unique index.
 * 
 * @author Eric.Piette
 */
public class DirectionType
{
	/** The unique index of the direction. */
	private final int index;

	/** The name of the current direction. */
	private final DirectionFacing directionActual;
	
	//-------------------------------------------------------------------------

	/**
	 * @param directionActual The current direction.
	 */
	public DirectionType(final DirectionFacing directionActual)
	{
		super();
		this.index =  directionActual.index();
		this.directionActual = directionActual;
	}	
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The direction.
	 */
	public DirectionFacing getDirection()
	{
		return this.directionActual;
	}

	/**
	 * @return The index of the direction.
	 */
	public int index() 
	{
		return index;
	}

	/**
	 * @return The actual direction.
	 */
	public DirectionFacing getDirectionActual() 
	{
		return directionActual;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		return "[Direction: " + directionActual + "]";
	}
}
