package app.display.dialogs.visual_editor.recs.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CSVUtils {

    /**
     * This method creates a .csv file as such:
     * header
     * lines(0)
     * lines(1)
     * line(2)
     * ...
     * The method already expects the singular methods to be in the comma separated format
     * @param location
     * @param header
     * @param lines
     */
    public static void writeCSV(String location, String header, List<String> lines) {
        
        try (FileWriter fw = FileUtils.writeFile(location);)
        {
            fw.write(header);
            for(String line : lines) 
                fw.write("\n"+line);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method reads in a .csv file from the specified location. It returns all lines as a list.
     * @param location
     */
    public static List<String> readCSV(String location) {
    	try (Scanner sc = FileUtils.readFile(location);)
    	{
    		List<String> lines = new ArrayList<>();

            String header = sc.nextLine();
            lines.add(header);

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                lines.add(line);
            }
            sc.close();
            return lines;
        }
    }
}
