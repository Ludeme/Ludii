package app.display.dialogs.visual_editor.recs.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class FileUtils {
    public static boolean isFileCSV(String fileName) {
        if(!fileName.contains("."))
            return false;
        return ".csv".equals(fileName.substring(fileName.length() - 4));
    }

    /**
     * returns the contents of the file as a string where
     * @param f
     */
    public static String getContents(File f) throws FileNotFoundException {
        boolean verbose = false;
        String content = "";
        try(Scanner sc = new Scanner(f);)
        {
        while(sc.hasNextLine()) {
            String l = sc.nextLine();
            if(l.contains("//"))
                l = l.substring(0,l.indexOf("//"));
            if(!l.equals("")) {
                //System.out.println((l.length() > 1) + " " + l);
                while (l.length() > 1 && (l.charAt(0) == ' ' && l.charAt(1) == ' '))
                    l = l.substring(1);
                while (l.length() > 0 && (l.charAt(l.length() - 1) == ' '))
                    l = l.substring(0, l.length() - 1);

                content = content + l;
            }
        }
        int index = content.length();
        int i = 0;
        ArrayList<Integer> occ = new ArrayList<>();
        while (i < content.length()) {
            index = content.indexOf(")(", i);
            i++;
            if(index != -1) {
                i = index + 1;
                occ.add(Integer.valueOf(index));
            }
        }
        for(int j : occ) {
            if(verbose)System.out.println(content.substring(j, j + 2));
            if(verbose)System.out.println(content.substring(0,j + 1));
            if(verbose)System.out.println(content.substring(j + 1));
            content = content.substring(0,j + 1) + " " + content.substring(j + 1);
        }
        return content;
        }
    }

    /**
     * Found on https://stackoverflow.com/questions/1844688/how-to-read-all-files-in-a-folder-from-java
     * 12/2/21, 4pm
     * @param folder
     */
    public static ArrayList<File> listFilesForFolder(final File folder) {
        ArrayList<File> files = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                files.addAll(listFilesForFolder(fileEntry));
            } else {
                files.add(fileEntry);
            }
        }
        return files;
    }

    public static ArrayList<File> listFilesForFolder(String pathname) {
        File folder = new File(pathname);
        return listFilesForFolder(folder);
    }

    /**
     * Takes in an absolute path and reformats it into a path from the root of the repository
     * @param absolutePath
     */
    public static String reformatPathToRepository(String absolutePath) {
        int i = absolutePath.indexOf("src");
        //https://stackoverflow.com/questions/5596458/string-replace-a-backslash
        //big thanks to Paulo Ebermann: "Try replaceAll("\\\\", "") or replaceAll("\\\\/", "/").
        //The problem here is that a backslash is (1) an escape character in Java string literals, and (2) an escape
        // character in regular expressions – each of this uses need doubling the character, in effect needing 4 \ in
        // row."
        // This helped me in replacing backslashes
        return absolutePath.replaceAll("\\\\","/").substring(i);
    }

    /**
     * This method creates a file at the desired path and creates a FileWriter object to write into it
     * the only thing left to do is store the FileWriter in a variable:
     * FileWriter fw = writeFile("example");
     * and the use
     * fw.write("lorem ipsum");
     * to write to the file.
     * It is very important to close the FileWriter at the end of the writing process!:
     * fw.close();
     * @param pathname
     */
    public static FileWriter writeFile(String pathname) {
        try {
            //File f = new File(pathname);
            FileWriter fw = new FileWriter(pathname);
            return fw;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Scanner readFile(String pathname) {
        try {
            File f = new File(pathname);
            Scanner sc = new Scanner(f);
            return sc;
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return null;
    }

    public static Scanner readFile(File f) {
        try {
            Scanner sc = new Scanner(f);
            return sc;
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return null;
    }

    public static void deleteFile(String path) {
        File f = new File(path);
        if (f.delete()) {
            System.out.println("Deleted the file: " + f.getName());
        } else {
            System.out.println("Failed to delete the file.");
        }
    }
}
