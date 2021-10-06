package main;

import java.util.concurrent.ThreadFactory;

/**
 * Thread factory that produces daemon threads
 * 
 * @author Dennis Soemers
 */
public class DaemonThreadFactory implements ThreadFactory
{
	//-------------------------------------------------------------------------
	
	/** Singleton */
	public final static ThreadFactory INSTANCE = new DaemonThreadFactory();
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor (private, singleton)
	 */
	private DaemonThreadFactory()
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------

	@Override
	public Thread newThread(final Runnable r) 
	{
		final Thread t = new Thread(r);
		t.setDaemon(true);
		return t;
	}

	//-------------------------------------------------------------------------

}
