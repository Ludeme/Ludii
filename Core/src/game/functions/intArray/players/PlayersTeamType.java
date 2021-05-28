package game.functions.intArray.players;

/**
 * Defines the types of team which can be iterated.
 * 
 * @author Eric.Piette
 */
public enum PlayersTeamType
{
	/** Team 1. */
	Team1(1),
	/** Team 2. */
	Team2(2),
	/** Team 3. */
	Team3(3),
	/** Team 4. */
	Team4(4),
	/** Team 5. */
	Team5(5),
	/** Team 6. */
	Team6(6),
	/** Team 7. */
	Team7(7),
	/** Team 8. */
	Team8(8),
	/** Team 9. */
	Team9(9),
	/** Team 10. */
	Team10(10),
	/** Team 11. */
	Team11(11),
	/** Team 12. */
	Team12(12),
	/** Team 13. */
	Team13(13),
	/** Team 14. */
	Team14(14),
	/** Team 15. */
	Team15(15),
	/** Team 16. */
	Team16(16);

// -------------------------------------------------------------------------

	/**
	 * The index of the team.
	 */
	private final int index;

	/**
	 * Constructor.
	 * 
	 * @param owner The index.
	 */
	private PlayersTeamType(final int index)
	{
		this.index = index;
	}

	/**
	 * @return The index.
	 */
	public int index()
	{
		return index;
	}

}