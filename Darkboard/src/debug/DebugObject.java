package debug;

import java.util.Vector;

public class DebugObject {
	
	public Vector<Object[]> dumpObject()
	{
		Vector<Object[]> result = new Vector();
		
		/*ReflectPermission rp = new ReflectPermission("suppressAccessChecks");
		rp.checkGuard(this);
		
		try
		{
			
			Field[] fields = getClass().getDeclaredFields();
			
			for (int k=0; k<fields.length; k++)
			{
				Object[] o = new Object[2];
				o[0] = fields[k].getName();
				o[1] = fields[k].get(Darkboard.class.cast(this));
			}
		
		} catch (Exception e) { e.printStackTrace(); }
		
		for (int k=0; k<result.size(); k++) System.out.println(result.get(k)[0]+" "+result.get(k)[1]);*/
		
		return result;
	}

}
