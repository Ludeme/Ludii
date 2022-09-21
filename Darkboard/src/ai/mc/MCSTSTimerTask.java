package ai.mc;

import java.util.TimerTask;

public class MCSTSTimerTask extends TimerTask {
	

	
	public void run()
	{
		MCSTSNode.stop = true;
	}
	


}
