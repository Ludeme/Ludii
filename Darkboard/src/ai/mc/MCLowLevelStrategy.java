package ai.mc;

/**
 * These strategies are immediately above actual moves, and as such they do not have continuations.
 * @author Nikola Novarlic
 *
 */
public class MCLowLevelStrategy extends MCStrategy 
{

	public MCStrategy[] continuations(MCStrategy[] sofar) 
	{
		return null;
	}

	public double[] progressInformation(MCState m, MCLink[] moves) 
	{

		return null;
	}

}
