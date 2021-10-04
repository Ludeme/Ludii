package metrics.suffix_tree;

public class Seperator extends Letter
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	boolean isContainer() {
		return false;
	}
	
	public Seperator(final int index, final String word)
	{
		super(-index-1, "$_" + index + "_");
		
	}

}
