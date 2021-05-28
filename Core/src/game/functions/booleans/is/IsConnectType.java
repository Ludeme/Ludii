package game.functions.booleans.is;

/**
 * Defines the types of Is for a connected or blocked test.
 * 
 * @author Eric.Piette
 */
public enum IsConnectType
{
	/** To check if regions are connected by pieces owned by a player. */
	Connected,

	/** To check if a player can not connect regions with his pieces. */
	Blocked,
}
