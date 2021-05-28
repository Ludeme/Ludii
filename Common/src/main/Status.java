package main;

import java.io.Serializable;

/**
 * Final status of a trial (will be null if game is still in progress).
 * 
 * @author cambolbro
 */
public final class Status implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Ways in which a trial can end
	 *
	 * @author Dennis Soemers
	 */
	public static enum EndType
	{
		/** Trial didn't end yet (Status should be null, so this should never happen) */
		NoEnd,
		/** Don't know how the Trial ended (likely old trial where we didn't record this) */
		Unknown,
		
		/** A normal end to a game */
		NaturalEnd,
		/** Reached artificial move limit */
		MoveLimit,
		/** Reached artificial turn limit */
		TurnLimit
	}
	
	//-------------------------------------------------------------------------

	/** The winner of the game. */
	private final int winner;
	
	/** Way in which a trial ended */
	private final EndType endType;

	//-------------------------------------------------------------------------
	
	/**
	 * Constructor. NOTE: assumes trial ended naturally
	 * 
	 * @param winner
	 */
	public Status(final int winner)
	{
		this.winner = winner;
		this.endType = EndType.NaturalEnd;
	}

	/**
	 * Constructor.
	 * 
	 * @param winner
	 * @param endType
	 */
	public Status(final int winner, final EndType endType)
	{
		this.winner = winner;
		this.endType = endType;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Index of winner, else 0 if none (draw, tie, abandoned).
	 */
	public int winner()
	{
		return winner;
	}
	
	/**
	 * @return Type describing how this game ended
	 */
	public EndType endType()
	{
		return endType;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + winner;
		return result;
	}

	@Override
	public boolean equals(final Object other)
	{
		if (!(other instanceof Status))
		{
			return false;
		}
		
		final Status otherStatus = (Status) other;
		
		return (winner == otherStatus.winner);
	}

	@Override
	public String toString()
	{
		String str = "";
		if (winner == 0)
		{
			str = "Nobody wins.";
		}
		else 
		{
			str = "Player " + winner + " wins.";
		}
		
		return str;
	}

	//-------------------------------------------------------------------------

}
