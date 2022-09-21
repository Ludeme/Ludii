package ai.mc;

/**
 * A high-level strategy is any but the bottom strategy tier. These have continuations
 * (that is, productions into a lower strategy level) but do not have progress information
 * (since they do not deal with actual game-level moves). At this time, they are implemented
 * with a finite state automaton. Later on, this will be initialized from a code string.
 * Right now it is all hard-wired.
 * @author Nikola Novarlic
 *
 */
public class MCHighLevelStrategy extends MCStrategy {
	
	protected MCStrategy messages[];
	protected String stratStrings[];
	protected int transitions[][];
	protected MCStrategy continuations[][];
	

	
	public MCHighLevelStrategy(String n, int p0, int p1, int p2, int p3)
	{
		name = n;
		parameters[0] = p0;
		parameters[1] = p1;
		parameters[2] = p2;
		parameters[3] = p3;
		
		init();
	}
	
	/**
	 * Override this.
	 */
	protected void init()
	{
		messages = new MCStrategy[0];
		transitions = new int[0][0];
		continuations = new MCStrategy[0][];

		stratStrings = new String[0];
		for (int k=0; k<stratStrings.length; k++)
			stratStrings[k] = messages[k].toString();
	}
	

	public MCStrategy[] continuations(MCStrategy[] sofar) {
		
		int state = 0;
		
		for (int k=0; k<sofar.length; k++)
		{
			MCStrategy st = sofar[k];
			if (st==null) continue;
			int nu = strategyToNumber(st);
			if (nu<0) 
			{
				return null;
			}
			state = transitions[state][nu];
			if (state<0)
			{
				return null;
			}
		}
		
		MCStrategy out[] = new MCStrategy[continuations[state].length];
		System.arraycopy(continuations[state], 0, out, 0, out.length);
		return out;
	}
	
	protected int strategyToNumber(MCStrategy s)
	{
		String st = s.toString();
		for (int k=0; k<stratStrings.length; k++)
			if (st.equals(stratStrings[k])) return k;
		
		return -1;
	}

	public double[] progressInformation(MCState m, MCLink[] moves) 
	{
		return null;
	}
	
	public boolean equals(Object o)
	{
		if (this==o) return true;
		
		
		return (this.toString().equals(o.toString()));
	}
	


}
