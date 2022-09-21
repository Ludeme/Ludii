/*
 * Created on 16-mar-06
 *
 */
package core;

import java.util.Vector;

/**
 * @author Nikola Novarlic
 * An evaluation tree also containing test/debug information for each node.
 */
public class ExtendedEvaluationTree extends EvaluationTree {
	
	/**
	 * A Vector of Objects representing values (or other) for each component of the eval function.
	 */
	Vector componentVector = new Vector();
	public EvaluationFunction func = null;
	public Vector history = null;
	
	public void addInformation(Object a)
	{
		componentVector.add(a);
	}
	
	public int componentNumber()
	{
		return componentVector.size();
	}
	
	public Object getComponent(int k)
	{
		return componentVector.get(k);
	}
	
	public String getExtendedRepresentation()
	{
		String result = /*super.getExtendedRepresentation()*/ "";
		if (func!=null)
			for (int k=0; k<componentNumber(); k++)
			{
				EvaluationFunctionComponent efc = func.getComponent(k);
				if (efc==null) break;
				String name = efc.name;
				String value = getComponent(k).toString();
				result += name +": "+value+"\n";
			}
		if (history!=null)
		{
			result+="History: ";
			for (int k=0; k<history.size(); k++)
				result+= history.get(k)+" ";
			result+="\n";
		}
		return result;
	}

}
