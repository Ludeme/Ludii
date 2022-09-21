package ai.mc;

public abstract class MCStrategy {
	
	public String name;
	public int parameters[] = new int[4];
	
	public abstract MCStrategy[] continuations(MCStrategy[] sofar);
	
	public abstract double[] progressInformation(MCState m, MCLink moves[]);
	
	public String toString()
	{
		return name+" "+parameters[0]+" "+parameters[1]+" "+parameters[2]+" "+parameters[3];
	}

}
