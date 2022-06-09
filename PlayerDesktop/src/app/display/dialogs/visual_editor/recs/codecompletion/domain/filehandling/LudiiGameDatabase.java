package codecompletion.domain.filehandling;

import interfaces.codecompletion.domain.filehandling.iLudiiGameDatabase;
import utils.FileUtils;
import utils.NGramUtils;
import utils.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author filreh
 */
public class LudiiGameDatabase implements iLudiiGameDatabase {

    private static final boolean DEBUG = false;
    private final DocHandler docHandler;

    private ArrayList<String> locations;
    private Map<Integer, String> descriptions;
    private Map<String, Integer> names;
    //Singleton
    private static LudiiGameDatabase db;

    public static LudiiGameDatabase getInstance() {
        // create object if it's not already created
        if(db == null) {
            db = new LudiiGameDatabase();
        }

        // returns the singleton object
        return db;
    }

    private LudiiGameDatabase() {
        this.docHandler = DocHandler.getInstance();
        init();
    }

    private void init() {
        fetchGameLocations();
        names = new HashMap<>();
        descriptions = new HashMap<>();
        fetchGameNames();
    }

    private void fetchGameLocations() {
        File folder = new File(docHandler.getGamesLocation());
        ArrayList<File> files = FileUtils.listFilesForFolder(folder);
        locations = new ArrayList<>();
        for(File f : files) {
            String location = f.getAbsolutePath();
            locations.add(location);
            if(DEBUG)System.out.println(location);
        }
    }

    /**
     * This method reads in a file that contains all game names.
     */
    private void fetchGameNames() {
        DocHandler docHandler = DocHandler.getInstance();
        String location = docHandler.getGamesNamesLocation();

        Scanner sc = FileUtils.readFile(location);
        int id = 0;
        while (sc.hasNext()) {
            String curGameName = sc.nextLine();
            names.put(curGameName,id++);
        }
        sc.close();
    }

    /**
     * This method analyses each game description one by one and writes the name of each game to a file.
     */
    private void fetchGameNamesFromDescriptions() {
        DocHandler docHandler = DocHandler.getInstance();
        String location = docHandler.getGamesNamesLocation();

        FileWriter fw = FileUtils.writeFile(location);
        for(int i = 0; i < getAmountGames(); i++) {
            String gameDescription = getDescription(i);
            String gameName = NGramUtils.getGameName(gameDescription);
            names.put(gameName, i);

            try {
                fw.write(gameName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns a list of all the locations of game descriptions in the database.
     *
     * @return
     */
    @Override
    public List<String> getLocations() {
        return locations;
    }

    /**
     * Returns the amount of games in the database
     *
     * @return
     */
    @Override
    public int getAmountGames() {
        return locations.size();
    }

    /**
     * Returns the description of the game with the id in the locations list
     *
     * @param id
     * @return
     */
    @Override
    public String getDescription(int id) {
        //fetches description if it was already read in
        String description = descriptions.getOrDefault(id,"null");

        // else, reads it in
        if(StringUtils.equals(description,"null")) {
            String location = locations.get(id);
            description = GameFileHandler.readGame(location);
        }
        return description;
    }

    /**
     * Returns the description of the game with the name specified. Create a map that links names to
     * ids and use the other method.
     *
     * @param name
     * @return
     */
    @Override
    public String getDescription(String name) {
        int id = names.get(name);
        return getDescription(id);
    }

    public List<String> getNames() {
        String[] namesArr = new String[getAmountGames()+1];
        for(Map.Entry<String, Integer> entry : names.entrySet()) {
            int id = entry.getValue();
            String name = entry.getKey();
            namesArr[id] = name;
        }
        List<String> namesList = Arrays.asList(namesArr);
        return namesList;
    }
}
