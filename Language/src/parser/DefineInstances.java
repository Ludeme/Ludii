package parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Record of a define instances in a game description.
 * @author cambolbro
 */
public class DefineInstances
{
	private final Define define;
	private final List<String> instances = new ArrayList<String>();
	
	//---------------------------------------------------- --------------------

	/**
	 * @param define The define in entry.
	 */
	public DefineInstances(final Define define)
	{
		this.define = define;
	}
	
	//---------------------------------------------------- --------------------
	
	public Define define()
	{
		return define;
	}

	public List<String> instances()
	{
		return Collections.unmodifiableList(instances);
	}
	
	//---------------------------------------------------- --------------------

	public void addInstance(final String instance)
	{
		instances.add(instance);
	}
	
	//---------------------------------------------------- --------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("~~~~~~~~~~~~~~~~~~~~\nDefine: " + define);
		for (final String instance : instances)
			sb.append("\nInstance: " +  instance);
			
		return sb.toString();
	}
	
	
	//---------------------------------------------------- --------------------
	

}
