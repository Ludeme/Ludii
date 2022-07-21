package app.display.dialogs.visual_editor.recs.utils;

import app.display.dialogs.visual_editor.recs.codecompletion.domain.model.Instance;
import app.display.dialogs.visual_editor.recs.codecompletion.domain.model.NGram;
import app.display.dialogs.visual_editor.recs.display.ProgressBar;

import java.util.*;

/**
 * @author filreh
 */
public class Model2CSV {
    private static final String COMMA_REPLACEMENT = "--COMMA--";
    private static final String COMMA = ",";
    private static final String EMPTY_STRING = "";
    private static final boolean DEBUG = false;
    /**
     * This method writes a model to a csv file. The location is according to the internal
     * storing mechanism that is based on the N parameter.
     *
     * @param model
     */
    public static void model2csv(NGram model, String location) {
        Map<String, List<Instance>> dictionary = model.getDictionary();
        int N = model.getN();

        Set<Map.Entry<String, List<Instance>>> dictionaryEntrySet = dictionary.entrySet();
        String header = "KEY,WORDS,MULTIPLICITY,"+N;
        List<String> lines = new ArrayList<>();

        int amtOperations = dictionaryEntrySet.size();
        //display a progress bar
        ProgressBar pb = new ProgressBar("Write Model to .csv", "Writing the model to "+location+".",amtOperations);

        int progress = 0;
        for(Map.Entry<String, List<Instance>> entry : dictionaryEntrySet) {
            String key = entry.getKey();
            //csv file splits the strings otherwise
            key = key.replaceAll(COMMA,COMMA_REPLACEMENT);

            boolean first = true;

            //for instances with the same key
            //only write the key once
            for(Instance instance : entry.getValue()) {
                String wordsAsString = EMPTY_STRING;
                List<String> words = instance.getWords();
                int i = 0;
                //for the words of each instance
                for(String word : words) {
                    wordsAsString += word;
                    if(i < words.size() - 1)
                        wordsAsString += " ";

                    i++;
                }
                wordsAsString = wordsAsString.replaceAll(COMMA,COMMA_REPLACEMENT);
                if(DEBUG)System.out.println("KEY: " + key);
                lines.add(key+COMMA+wordsAsString+COMMA+instance.getMultiplicity());

                //makes sure the key is only written on the first occurrence
                if(first) {
                    first = false;
                    key = EMPTY_STRING;
                }
            }

            // update progress bar
            pb.updateProgress(++progress);
        }

        pb.close();

        CSVUtils.writeCSV(location, header, lines);
    }

    /**
     * This method reads in a model from a .csv file.
     *
     * @param location
     * @return
     */
    @SuppressWarnings("all")
    public static NGram csv2model(String location) {
        List<String> lines = CSVUtils.readCSV(location);
        // start the reading
        String header = lines.get(0);
        String[] splitHeader = header.split(COMMA);
        String NString = splitHeader[3];
        int N = Integer.parseInt(NString);
        HashMap<String, List<Instance>> dictionary = new HashMap<>();
        List<Instance> value = new ArrayList<>();

        //since the csv is compressed, we keep the last string for the next instances
        String lastKey = EMPTY_STRING;
        boolean firstLine = true;
        for(String line : lines) {
            if(firstLine) {
                firstLine = false;
                continue;
            }
            if(DEBUG)System.out.println("--------------------------");
            if(DEBUG)System.out.println(line);
            String[] split = line.split(COMMA);
            if(DEBUG)System.out.println("|"+split[0]+"|"+ split[0].equals(EMPTY_STRING));
            String key = split[0];
            key = key.replaceAll(COMMA_REPLACEMENT,COMMA);

            if(key.equals(EMPTY_STRING)) {
                key = lastKey;
            } else if(!key.equals(EMPTY_STRING)) {
                lastKey = key;
            }

            String wordsAsString = split[1];
            String[] wordsAsArray = wordsAsString.split(" ");
            List<String> words = Arrays.asList(wordsAsArray);
            words.forEach(s -> s = s.replaceAll(COMMA_REPLACEMENT,COMMA));
            String multiplicityAsString = split[2];
            int multiplicity = Integer.parseInt(multiplicityAsString);
            if(dictionary.containsKey(key)) {
                value = dictionary.get(key);
            } else {
                value = new ArrayList<>();
            }
            if(words.size() > 1) {
                value.add(new Instance(words,multiplicity));
                dictionary.put(key,value);
            } else if(words.size() == 1){
                value.add(new Instance(Arrays.asList("",words.get(0)), multiplicity));
            }

        }

        NGram model = new NGram(N,dictionary);
        return model;
    }
}
