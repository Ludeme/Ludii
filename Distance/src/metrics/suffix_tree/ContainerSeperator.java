package metrics.suffix_tree;

public class ContainerSeperator<T> extends Seperator
{

	private final T container;

	public T getContainer()
	{
		return container;
	}


	@Override
	public boolean isContainer() {
		return true;
	}


	public ContainerSeperator(final int index, final T containerObject, final String word)
	{
		super(index, word);
		this.container  = containerObject;
	}

	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
