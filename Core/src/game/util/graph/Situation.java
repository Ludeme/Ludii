package game.util.graph;

import main.math.RCL;

/**
 * Graph element positional situation, for coordinate labelling.
 * @author cambolbro
 */
public class Situation
{
	private final RCL rcl = new RCL(); 
	String label = "";
	
	/**
	 * @return an RCL coordinate.
	 */
	public RCL rcl()
	{
		return rcl;
	}
	
	/**
	 * @return The corresponding label.
	 */
	public String label()
	{
		return label;
	}
	
	/**
	 * To set the label.
	 * 
	 * @param str The new label.
	 */
	public void setLabel(final String str)
	{
		label = new String(str);
	}
}
