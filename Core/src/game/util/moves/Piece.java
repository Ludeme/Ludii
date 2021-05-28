package game.util.moves;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.functions.ints.IntFunction;
import game.functions.ints.board.Id;
import other.BaseLudeme;

/**
 * Specifies operations based on the ``what'' data.
 * 
 * @author Eric.Piette
 */
public class Piece extends BaseLudeme
{
	/** The index of the component. */
	private final IntFunction component;

	/** The indices of the components. */
	private final IntFunction[] components;

	/** The local state of the site of the component. */
	private final IntFunction state;
	
	/** The name of the component. */
	private final String name;

	/** The names of the components. */
	private final String[] names;

	//-------------------------------------------------------------------------

	/**
	 * @param nameComponent  The name of the component.
	 * @param component      The index of the component [The component with the
	 *                       index corresponding to the index of the mover,
	 *                       (mover)].
	 * @param nameComponents The names of the components.
	 * @param components     The indices of the components.
	 * @param state          The local state value to put on the site where the
	 *                       piece is placed.
	 * 
	 * @example (piece (mover))
	 */
	public Piece
	(
		@Or            final String        nameComponent, 
		@Or       	   final IntFunction   component, 
		@Or       	   final String[]      nameComponents, 
		@Or       	   final IntFunction[] components, 
			@Opt @Name final IntFunction   state 
	)
	{
		int numNonNull = 0;
		if (component != null)
			numNonNull++;
		if (components != null)
			numNonNull++;
		if (nameComponent != null)
			numNonNull++;
		if (nameComponents != null)
			numNonNull++;

		if (numNonNull != 1)
			throw new IllegalArgumentException(
					"Piece(): One nameComponent, component, nameComponents or components parameter must be non-null.");

		this.name = nameComponent;
		this.names = nameComponents;
		this.component = (nameComponent != null) ? new Id(nameComponent,null) : component;

		if(nameComponents != null)
		{
			this.components = new IntFunction[nameComponents.length];
			for(int i = 0; i < nameComponents.length;i++)
			{
				this.components[i] = new Id(nameComponents[i],null);
			}
		}
		else
			this.components = components;
		
		this.state = state;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The state value.
	 */
	public IntFunction state()
	{
		return state;
	}

	/**
	 * @return The component index.
	 */
	public IntFunction component()
	{
		return component;
	}
	
	/**
	 * @return The indices of the components.
	 */
	public IntFunction[] components()
	{
		return components;
	}

	/**
	 * @return The name of the component.
	 */
	public String nameComponent()
	{
		return name;
	}

	/**
	 * @return The names of the components.
	 */
	public String[] nameComponents()
	{
		return names;
	}
}
