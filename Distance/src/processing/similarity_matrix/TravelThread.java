package processing.similarity_matrix;

import java.util.ArrayList;

import common.LudRul;

public class TravelThread
{
	final VisualiserInterface visualiser;
	Thread thread;
	boolean killswitch;

	public TravelThread(final VisualiserInterface vis)
	{
		this.visualiser = vis;
	}

	public void checkThread(
			final boolean selected, final ArrayList<LudRul> sortedCandidates
	)
	{
		if (selected)turnOn(sortedCandidates);
		else turnOf();
		
		
	}

	private void turnOf()
	{
		if (thread != null) {
			killswitch = true;
			while(thread!=null) {
				waitMilSec(1l);
			}
		}
		
	}

	private void turnOn(final ArrayList<LudRul> sortedCandidates)
	{
		final TravelThread parent = this;
		final ArrayList<LudRul> travelCandidates = new ArrayList<LudRul>(sortedCandidates);
		turnOf();
		thread = new Thread(){
		    @Override
			public void run(){
		    	int count = 0;
		      while(!killswitch) {
		    	
		    	visualiser.reSort(travelCandidates.get(count));
		    	parent.waitMilSec(256l);
		    	count++;
		    	if (count >= sortedCandidates.size())
		    		count = 0;
		      }
		      thread = null;
		    }
		  };
		  killswitch = false;
		  thread.start();
	}

	protected void waitMilSec(final long length)
	{
		try
		{
			Thread.sleep(length);
		} catch (final InterruptedException e)
		{
			
			e.printStackTrace();
		}
	}

}
