package interfaces.codecompletion.domain.filehandling;

import java.util.List;

/**
 * @author filreh
 */
public interface iLudiiGameDatabase {
    /**
     * Returns a list of all the locations of game descriptions in the database.
     * @return
     */
    List<String> getLocations();

    /**
     * Returns the amount of games in the database
     * @return
     */
    int getAmountGames();

    /**
     * Returns the description of the game with the id in the locations list
     * @param id
     * @return
     */
    String getDescription(int id);

    /**
     * Returns the description of the game with the name specified. Create a map that links names to
     * ids and use the other method.
     * @param name
     * @return
     */
    String getDescription(String name) throws Exception;
}
