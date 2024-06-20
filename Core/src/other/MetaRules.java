package other;

import game.types.play.GravityType;
import game.types.play.PinType;
import game.types.play.RepetitionType;
import game.types.play.NoStackOnType;

/**
 * To store which meta rule is activated or not.
 * 
 * @author Eric.Piette
 */
public class MetaRules
{
	/** To know if the metarule automove is activate for that game. */
	private boolean automove = false;

	/** To know the gravityType to apply. */
	private GravityType gravityType = null;

	/** To know the pinType to apply. */
	private PinType pinType = null;

	/** To know if the metarule swap is activate for that game. */
	private boolean usesSwapRule = false;

	/** To know if a metarule about repetition is activated. */
	private RepetitionType repetitionType = null;
	
	/** To know if the metarule no suicide is on. */
	private boolean usesNoSuicide = false;
	
	/** To know if the metarule NoStackOnFallen is activate for that game. */
	private NoStackOnType noStackOnType = null;

	//-------------------------------------------------------------------------

	/**
	 * @return True if the game uses automove.
	 */
	public boolean automove()
	{
		return automove;
	}

	/**
	 * To set the automove value.
	 * 
	 * @param automove The value to set.
	 */
	public void setAutomove(final boolean automove)
	{
		this.automove = automove;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return True if the game uses the swap rule.
	 */
	public boolean usesSwapRule()
	{
		return usesSwapRule;
	}

	/**
	 * To set the flag indicating whether or not this game uses swap rule.
	 * 
	 * @param swap The value to set.
	 */
	public void setUsesSwapRule(final boolean swap)
	{
		usesSwapRule = swap;
	}

	//-------------------------------------------------------------------------

	/**
	 * To set the pin meta rule.
	 * 
	 * @param type The NoStackOn type.
	 */
	public void setNoStackOnType(final NoStackOnType type)
	{
		noStackOnType = type;
	}

	/**
	 * @return The type of the pin.
	 */
	public NoStackOnType noStackOnType()
	{
		return noStackOnType;
	}

	//-------------------------------------------------------------------------

	
	/**
	 * To set the repetition meta rule.
	 * 
	 * @param type The repetition type.
	 */
	public void setRepetitionType(final RepetitionType type)
	{
		repetitionType = type;
	}

	/**
	 * @return The type of the repetition.
	 */
	public RepetitionType repetitionType()
	{
		return repetitionType;
	}

	//-------------------------------------------------------------------------

	/**
	 * To set the gravity meta rule.
	 * 
	 * @param type The gravity type.
	 */
	public void setGravityType(final GravityType type)
	{
		gravityType = type;
	}

	/**
	 * @return The type of the gravity.
	 */
	public GravityType gravityType()
	{
		return gravityType;
	}

	//-------------------------------------------------------------------------

	/**
	 * To set the pin meta rule.
	 * 
	 * @param type The pin type.
	 */
	public void setPinType(final PinType type)
	{
		pinType = type;
	}

	/**
	 * @return The type of the pin.
	 */
	public PinType pinType()
	{
		return pinType;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * To set the no suicide meta rule.
	 * 
	 * @param value The no suicide value.
	 */
	public void setNoSuicide(final boolean value)
	{
		usesNoSuicide = value;
	}

	/**
	 * @return The value of the no suicide meta rule.
	 */
	public boolean usesNoSuicide()
	{
		return usesNoSuicide;
	}
}
