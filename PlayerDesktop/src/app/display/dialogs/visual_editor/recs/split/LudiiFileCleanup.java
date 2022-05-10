package app.display.dialogs.visual_editor.recs.split;

import app.display.dialogs.visual_editor.recs.utils.FileUtils;
import app.display.dialogs.visual_editor.recs.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LudiiFileCleanup {

    public static List<String> cleanup(List<String> split) {
        List<String> cleanedUpSplit = new ArrayList<>();
        boolean foundMetadata = false;
        for(String w : split) {
            //check for metadata
            if(w.equals("(metadata")) {
                foundMetadata = true;
                break;
            }
            //check for comments
            if(!w.contains("//") /* && //TODO: check for weird tab symbol*/) {
                //check if
                //a. is collection
                //a.i.: {}
                //a.ii: contains ..
                if(w.contains("..")) {
                    cleanedUpSplit.add("COLLECTION");
                    continue;
                }
                //b. is number
                if(StringUtils.isInteger(w)) {
                    cleanedUpSplit.add("INTEGER");
                    continue;
                } else if(StringUtils.isRealNumber(w)) {
                    cleanedUpSplit.add("REAL_NUMBER");
                    continue;
                }
                //c. comments
                if(w.contains("//")) {
                    w = w.substring(0,w.lastIndexOf("//"));
                }
                if(StringUtils.containsOnly(w,'-')) {
                    continue;
                }
                cleanedUpSplit.add(w);
            }
        }
        if(foundMetadata) {
            cleanedUpSplit = cleanedUpSplit.subList(0,cleanedUpSplit.size()-1);
        }
        return cleanedUpSplit;
    }

    public static String allLinesOneString(String srcContentrootPath) {
        Scanner sc = FileUtils.readFile(srcContentrootPath);
        String allLines = "";
        while(sc.hasNextLine()) {
            String nextLine = sc.nextLine();
            allLines += " " + nextLine;
        }
        sc.close();

        return allLines;
    }
}
