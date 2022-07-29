package app.display.dialogs.visual_editor.recs.interfaces.codecompletion.domain.filehandling;

import java.util.List;

/**
 * @author filreh
 */
public interface iLudiiGameDatabase {
    /**
     * Returns a list of all the locations of game descriptions in the database.
     */
    List<String> getLocations();

    /**
     * Returns the amount of games in the database
     */
    int getAmountGames();

    /**
     * Returns the description of the game with the id in the locations list
     * @param id
     */
    String getDescription(int id);

    /**
     * Returns the description of the game with the name specified. Create a map that links names to
     * ids and use the other method.
     * @param name
     */
    String getDescription(String name) throws Exception;
}
