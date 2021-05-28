package other;

/**
 * The item type of the equipment, ordered to be sorted in the list of Item in
 * Equipment.java
 * 
 * Note: This is not a ludeme, just used internally.
 * 
 * @author Eric.Piette
 */
public enum ItemType
{
	/**
	 * A container.
	 */
	Container,

	/**
	 * A hand container.
	 */
	Hand,

	/**
	 * A fan container.
	 */
	Fan,

	/**
	 * A dice container.
	 */
	Dice,

	/**
	 * The hints.
	 */
	Hints,

	/**
	 * The regions.
	 */
	Regions,

	/**
	 * A map.
	 */
	Map,

	/**
	 * The dominoes.
	 */
	Dominoes,

	/**
	 * A component.
	 */
	Component;
	
	
	/**
	 * @param itemType
	 * @return True if and only if the given item type is a type of container
	 */
	public static boolean isContainer(final ItemType itemType)
	{
		return itemType.ordinal() <= Dice.ordinal();
	}
	
	/**
	 * @param itemType
	 * @return True if and only if the given item type is a type of component (Component or Dominoes)
	 */
	public static boolean isComponent(final ItemType itemType)
	{
		return itemType.ordinal() >= Dominoes.ordinal();
	}
	
	/**
	 * @param itemType
	 * @return True if and only if the given item type is a Regions type
	 */
	public static boolean isRegion(final ItemType itemType)
	{
		return itemType == Regions;
	}
	
	/**
	 * @param itemType
	 * @return True if and only if the given item type is a Map type
	 */
	public static boolean isMap(final ItemType itemType)
	{
		return itemType == Map;
	}
	
	/**
	 * @param itemType
	 * @return True if and only if the given item type is a Hints type
	 */
	public static boolean isHints(final ItemType itemType)
	{
		return itemType == Hints;
	}
}
