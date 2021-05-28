package main;

/**
 * A home for miscellaneous useful routines. Like stackTrace().
 * 
 * @author cambolbro
 */
public final class Utilities
{

	//-------------------------------------------------------------------------

	/**
	 * Show a stack trace.
	 */
	public static void stackTrace()
	{
		final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		System.out.println("======================");
		for (int n = 0; n < ste.length; n++)
			System.out.println(ste[n]);
		System.out.println("======================");
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Get a stack trace.
	 */
	public static String stackTraceString()
	{
		String stackTrace = "";
		final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		stackTrace += "======================";
		for (int n = 0; n < ste.length; n++)
			stackTrace += ste[n];
		stackTrace += "======================";
		return stackTrace;
	}

	//-------------------------------------------------------------------------

}
