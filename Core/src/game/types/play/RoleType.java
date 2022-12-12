package game.types.play;

import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.ints.board.Id;
import main.Constants;

/**
 * Defines the possible role types of the players in a game.
 * 
 * @author cambolbro and Eric.Piette
 * 
 * @remarks Each player will have at least one, and possibly more than one, role type in a game.
 *          For example, players may belong to permanent or temporary teams, or may be denoted 
 *          as the {\tt Ally} or {\tt Enemy} of a given player, etc.
 */
public enum RoleType
{
	/** Neutral role, owned by nobody. */
	Neutral(0),
	
	/** Player 1. */
	P1(1),
	/** Player 2. */
	P2(2),
	/** Player 3. */
	P3(3),
	/** Player 4. */
	P4(4),
	/** Player 5. */
	P5(5),
	/** Player 6. */
	P6(6),
	/** Player 7. */
	P7(7),
	/** Player 8. */
	P8(8),
	/** Player 9. */
	P9(9),
	/** Player 10. */
	P10(10),
	/** Player 11. */
	P11(11),
	/** Player 12. */
	P12(12),
	/** Player 13. */
	P13(13),
	/** Player 14. */
	P14(14),
	/** Player 15. */
	P15(15),
	/** Player 16. */
	P16(16),
	
	/** Team 1 (index 1). */
	Team1(1),
	/** Team 2 (index 2). */
	Team2(2),
	/** Team 3 (index 3). */
	Team3(3),
	/** Team 4 (index 4). */
	Team4(4),
	/** Team 5 (index 5). */
	Team5(5),
	/** Team 6 (index 6). */
	Team6(6),
	/** Team 7 (index 7). */
	Team7(7),
	/** Team 8 (index 8). */
	Team8(8),
	/** Team 9 (index 9). */
	Team9(9),
	/** Team 10 (index 10). */
	Team10(10),
	/** Team 11 (index 11). */
	Team11(11),
	/** Team 12 (index 12). */
	Team12(12),
	/** Team 13 (index 13). */
	Team13(13),
	/** Team 14 (index 14). */
	Team14(14),
	/** Team 15 (index 15). */
	Team15(15),
	/** Team 16 (index 16). */
	Team16(16),
	
	/** Team of the mover (index Mover). */
	TeamMover(Constants.NOBODY),
	
	/** Applies to each player (for iteration), e.g. same piece owned by each player */
	Each(Constants.NOBODY),
	
	/** Shared role, shared by all players. */
	Shared(Constants.NOBODY),

	/** All players. */
	All(Constants.NOBODY),

	/** Player who is moving. */
	Mover(Constants.NOBODY),
	
	/** Player who is moving next turn. */
	Next(Constants.NOBODY),
	
	/** Player who made the previous decision move. */
	Prev(Constants.NOBODY),

	/** Players who are not moving. */
	NonMover(Constants.NOBODY),
	
	/** Enemy players. */
	Enemy(Constants.NOBODY),	

	/** Friend players (Mover + Allies). */
	Friend(Constants.NOBODY), 
	
	/** Ally players. */
	Ally(Constants.NOBODY),
	
	/** Placeholder for iterator over all players, e.g. from end.ForEach. */
	Player(Constants.NOBODY);
	
	//-------------------------------------------------------------------------
	
	private static final RoleType[] PlayerIdToRole = RoleType.values();
	
	private final int owner;
	
	/**
	 * Default constructor, to avoid RoleType(null) warning during compilation (e.g. Trax).
	 */
	private RoleType()
	{
		owner = Constants.NOBODY;
	}

	/**
	 * Constructor.
	 * 
	 * @param owner The index.
	 */
	private RoleType(final int owner)
	{
		this.owner = owner;
	}
	
	/**
	 * @return The corresponding player.
	 */
	public int owner()
	{
		return owner;
	}

	/**
	 * @param role The roleType.
	 * @return True if the roleType is about a team.
	 */
	public static boolean isTeam(final RoleType role)
	{
		return (role.toString().contains("Team"));
	}
	
	/**
	 * @param role The roleType.
	 * @return True if the roleType can corresponds to many players.
	 */
	public static boolean manyIds(final RoleType role)
	{
		return isTeam(role) | role.equals(Ally) | role.equals(Enemy) | role.equals(NonMover) | role.equals(All) | role.equals(Friend);
	}

	/**
	 * @param pid The index of the player.
	 * @return The corresponding roletype of the index.
	 */
	public static RoleType roleForPlayerId(final int pid)
	{
		if (pid > 0 && pid <= Constants.MAX_PLAYERS)
			return PlayerIdToRole[pid];
		
		return Neutral;
	}
	
	/**
	 * @param roleType
	 * @return An IntFunction representation of given RoleType
	 */
	public static IntFunction toIntFunction(final RoleType roleType)
	{
		if (roleType.owner > 0)
			return new IntConstant(roleType.owner);
		else
			return new Id(null, roleType);
	}
}
