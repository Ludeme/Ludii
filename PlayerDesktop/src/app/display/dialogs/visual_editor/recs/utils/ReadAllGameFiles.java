package app.display.dialogs.visual_editor.recs.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


/**
 * This class is supposed to find all .lud files in the given directory and write their locations to a
 * .txt file located at src/main/resources/locations.txt.
 */
public class ReadAllGameFiles {
    private static final boolean DEBUG = false;

    /**
     * This method can be used to
     * - test the methods below for functionality,
     * - see an example of how you would use them or
     * - generate the locations.txt file
     * @param args
     */
    public static void main(String[] args) {
        ArrayList<String> locations = findAllGames("src/main/resources/Ludii/lud");
        writeGameLocations(locations, "src/main/resources/locations.txt");
        if(DEBUG)System.out.println("Amount of games found: "+readGameLocations("src/main/resources/locations.txt").size());
        if(DEBUG)System.out.println(locations.equals(readGameLocations("src/main/resources/locations.txt")));
    }
    public static ArrayList<String> findAllGames(String directory) {
        File folder = new File(directory);
        ArrayList<File> files = FileUtils.listFilesForFolder(folder);
        ArrayList<String> locations = new ArrayList<>();
        for(File f : files) {
            String location = f.getAbsolutePath();
            locations.add(location);
            if(DEBUG)System.out.println(location);
        }
        return locations;
    }



    public static boolean writeGameLocations(ArrayList<String> locations, String pathname) {
        FileWriter fw = FileUtils.writeFile(pathname);
        try {
            fw.write(locations.size()+"");
            for(String location: locations) {
                fw.write("\n"+location);
            }
            fw.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static ArrayList<String> readGameLocations(String path) {
        Scanner sc = FileUtils.readFile(path);
        ArrayList<String> locations = new ArrayList<>();
        int amountLocations = Integer.parseInt(sc.nextLine());
        for(int i = 0; i < amountLocations; i++) {
            locations.add(sc.nextLine());
        }
        sc.close();
        return locations;
    }
}
