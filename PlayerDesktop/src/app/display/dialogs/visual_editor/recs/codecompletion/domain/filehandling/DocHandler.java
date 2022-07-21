package app.display.dialogs.visual_editor.recs.codecompletion.domain.filehandling;

import app.display.dialogs.visual_editor.recs.utils.FileUtils;
import app.display.dialogs.visual_editor.recs.utils.StringUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Class is a singleton
 */
public class DocHandler {
    // TODO: refactor paths in similar way
    public static final String DOC_LOCATION = "../Common/res/recs/documents.txt";
    public static final String GRAMMAR = "grammar_location";
    public static final String GAMES = "games_location";
    public static final String MODELS = "models_location";
    public static final String MODEL = "location_model_";
    public static final String LOGO = "logo_location";
    public static final String GAMES_NAMES = "games_names_location";
    public static final String SEPARATOR = ":";
    public static final String MODEL_DOES_NOT_EXIST = "MODELDOESNOTEXIST";

    private static final boolean DEBUG = false;

    private String grammarLocation;
    private String gamesLocation;
    private String logoLocation;
    private String modelsLocation;
    private Map<Integer,String> modelLocations;

    //singleton
    private static DocHandler docHandler;
    private String gamesNamesLocation;

    public static DocHandler getInstance() {

        // create object if it's not already created
        if(docHandler == null) {
            docHandler = new DocHandler();
        }

        // returns the singleton object
        return docHandler;
    }

    /**
     * The constructor reads in alla available information from the file on startup
     */
    private DocHandler() {
        modelLocations = new HashMap<>();
        try(Scanner sc = FileUtils.readFile(DOC_LOCATION);)
        {
	        while (sc.hasNext()) {
	            String nextLine = sc.nextLine();
	            if(DEBUG)System.out.println(nextLine);
	            parseDocumentsLine(nextLine);
	        }
	        sc.close();
        }
    }

    /**
     * This method parses the different lines of the documents.txt:
     * -grammar location
     * -games location
     * -model location
     * @param line
     */
    private void parseDocumentsLine(String line) {
        String[] split = line.split(SEPARATOR);
        if(StringUtils.equals(split[0], GRAMMAR)) {
            grammarLocation = split[1];
            if(DEBUG)System.out.println(grammarLocation);
        }
        if(StringUtils.equals(split[0], GAMES)) {
            gamesLocation = split[1];
            if(DEBUG)System.out.println(gamesLocation);
        }
        if(StringUtils.equals(split[0], LOGO)) {
            logoLocation = split[1];
            if(DEBUG)System.out.println(logoLocation);
        }
        if(StringUtils.equals(split[0], MODELS)) {
            modelsLocation = split[1];
            if(DEBUG)System.out.println(modelsLocation);
        }
        if(StringUtils.equals(split[0], GAMES_NAMES)) {
            gamesNamesLocation = split[1];
            if(DEBUG)System.out.println(gamesNamesLocation);
        }
        if(split[0].startsWith(MODEL)) {
            int N = Integer.parseInt(split[0].charAt(MODEL.length())+"");
            modelLocations.put(N,split[1]);
            if(DEBUG)System.out.println(modelLocations.get(N));
        }
    }

    /**
     * This method writes the stored data about documents to the documents.txt
     * @throws IOException
     */
    public void writeDocumentsFile() {
        try (FileWriter fw = FileUtils.writeFile(DOC_LOCATION);){
            if(grammarLocation != null) {
                    fw.write(GRAMMAR +SEPARATOR+grammarLocation+"\n");
            }
            if(gamesLocation != null) {
                fw.write(GAMES +SEPARATOR+gamesLocation+"\n");
            }
            if(logoLocation != null) {
                fw.write(LOGO +SEPARATOR+logoLocation+"\n");
            }
            if(modelsLocation != null) {
                fw.write(MODELS +SEPARATOR+modelsLocation+"\n");
            }
            if(gamesNamesLocation != null) {
                fw.write(GAMES_NAMES +SEPARATOR+gamesNamesLocation+"\n");
            }
            for(Map.Entry<Integer, String> entry : modelLocations.entrySet()) {
                int N = entry.getKey();
                String modelLocation = entry.getValue();
                fw.write(MODEL +N+SEPARATOR+modelLocation+"\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addModelLocation(int N, String location) {
        modelLocations.put(N,location);
    }

    public String getGrammarLocation() {
        return grammarLocation;
    }

    public String getGamesLocation() {
        return gamesLocation;
    }

    public String getLogoLocation() {
        return logoLocation;
    }

    public String getModelsLocation() {
        return modelsLocation;
    }

    public String getGamesNamesLocation() {
        return gamesNamesLocation;
    }

    public String getModelLocation(int N) {
        return modelLocations.getOrDefault(N,MODEL_DOES_NOT_EXIST);
    }

    /**
     * Write the documents file on closing
     */
    public void close() {
        writeDocumentsFile();
    }
}
