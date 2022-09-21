package ai.mc;

import java.util.concurrent.Callable;

public class MCPureTask implements Callable<Object>, Runnable {
	
	MCGameSimulator sim = new MCGameSimulator();
	int set;
	int depth;
	
	public MCPureTask(int s, int d)
	{
		set = s;
		depth = d;
	}

	public Object call() throws Exception {
		// TODO Auto-generated method stub
		run();
		return null;
	}

	public void run() {
		// TODO Auto-generated method stub
		//for (int k=0; k<500; k++)
		sim.init(ai.mc.MCSTSNode.roots[set-1].state);
		while (!ai.mc.MCSTSNode.stop)
		{
			ai.mc.MCSTSNode n = ai.mc.MCSTSNode.roots[set-1].pureMCSelect(sim,set,depth);
			if (n!=null && n.parent!=null) n.pureMCEval(sim, set, depth);
		}
	}

}
