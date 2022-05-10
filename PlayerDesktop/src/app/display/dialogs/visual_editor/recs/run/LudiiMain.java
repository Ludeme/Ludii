package app.display.dialogs.visual_editor.recs.run;


import app.display.dialogs.visual_editor.recs.gzip.GZIPCompression;
import app.display.dialogs.visual_editor.recs.gzip.GZIPDecompression;
import app.display.dialogs.visual_editor.recs.model.Ludii.NGramModelLudii;
import app.display.dialogs.visual_editor.recs.split.LudiiFileCleanup;
import app.display.dialogs.visual_editor.recs.split.SentenceSplit;
import app.display.dialogs.visual_editor.recs.utils.FileUtils;
import app.display.dialogs.visual_editor.recs.utils.ReadAllGameFiles;
import app.display.dialogs.visual_editor.recs.utils.Triple;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LudiiMain {
    public static boolean DEBUG = true;
    public static void main(String[] args) throws IOException, InterruptedException {
        String input = "(game \"Aksadyuta\" (players 2)";
        List<String> split = SentenceSplit.splitText(input);
        split = LudiiFileCleanup.cleanup(split);
        System.out.println("CONTEXT:"+split);
        getPrediction(split);
    }

    public static void getPrediction(List<String> context, int picklistLength) {
        long start = System.currentTimeMillis();
        NGramModelLudii m = NGramModelLudii.readModel("res/Compression2/LudiiModel7.gz");
        if(DEBUG)System.out.println(System.currentTimeMillis() - start);
        start = System.currentTimeMillis();
        if(DEBUG)System.out.println(System.currentTimeMillis() - start);
        int i = 1;
        for(String rec : m.getPicklist(context, picklistLength)) {
            System.out.println(i+". "+rec);
            i++;
        }
    }
    public static void getPrediction(List<String> context) {
        long start = System.currentTimeMillis();
        NGramModelLudii m = NGramModelLudii.readModel("res/Compression2/LudiiModel7.gz");
        if(DEBUG)System.out.println(System.currentTimeMillis() - start);
        start = System.currentTimeMillis();
        if(DEBUG)System.out.println(System.currentTimeMillis() - start);
        int i = 1;
        for(String rec : m.getPicklist(context)) {
            System.out.println(i+". "+rec);
            i++;
        }
    }

    public static void compressWrite(NGramModelLudii m, String path) {
        String tmpPath = "res/tmp/tmp.csv";
        m.writeModel(tmpPath);
        GZIPCompression.compress(tmpPath,path);
        FileUtils.deleteFile(tmpPath);
    }

    public static NGramModelLudii decompressRead(String path) {
        String tmpDirectory = "res/tmp/tmp.csv";
        GZIPDecompression.decompress(path,tmpDirectory);
        NGramModelLudii m = NGramModelLudii.readModel(tmpDirectory);
        FileUtils.deleteFile(tmpDirectory);
        return m;
    }

    public static void createAndWriteModels(int maxN) {
        List<Triple<Integer,Long,Long>> timeComplexity = new ArrayList<>();
        for(int N = 7; N <= maxN; N++) {
            long start = System.currentTimeMillis();
            //gather all lud files
            List<String> locations = ReadAllGameFiles.findAllGames("res/Ludii/lud");
            String input = LudiiFileCleanup.allLinesOneString(locations.get(0));
            NGramModelLudii m = new NGramModelLudii(input,N);
            locations = locations.subList(1, locations.size());
            for(String location : locations) {
                m.addToModel(LudiiFileCleanup.allLinesOneString(location));
            }
            String path = "res/Compression2/LudiiModel"+N+".gz";
            m.writeModel(path);
            long finish = System.currentTimeMillis();
            long computationTime = finish - start;
            long bytes;
            try {
                bytes = Files.size(Paths.get(path));
                timeComplexity.add(new Triple<>(N,computationTime,bytes));
                FileWriter fw = FileUtils.writeFile("res/Compression2/timecomplexity.gz");
                Objects.requireNonNull(fw).write("N,Time(ms),bytes\n");
                for(Triple<Integer,Long,Long> t : timeComplexity) {
                    fw.write(t.getR()+","+t.getS()+","+t.getT()+"\n");
                }
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void compressionComplexity() throws IOException {
        FileWriter fw = FileUtils.writeFile("res/Compression2/compressionComplexity.csv");
        Objects.requireNonNull(fw).write("N,compression_ms,compression_s,compression_min,decompression_ms,decompression_s,decompression_min\n");
        for(int N = 2; N <= 17; N++) {
            String compressedPath = "res/Compression2/compressed/CompressedLudiiModel"+N+".gz";
            long startDecompression = System.currentTimeMillis();
            NGramModelLudii m = decompressRead(compressedPath);
            long finishDecompression = System.currentTimeMillis();
            long decompressionTime = finishDecompression - startDecompression;
            String outputPath = "res/Compression2/CompressedLudiiModel"+N+".gz";
            long startCompression = System.currentTimeMillis();
            compressWrite(m,outputPath);
            long finishCompression = System.currentTimeMillis();
            long compressionTime = finishCompression - startCompression;
            fw.write(N+","+compressionTime+","+(double)compressionTime/1000+","
                    +(double)compressionTime/(1000*60)+","+decompressionTime+","
                    +(double)decompressionTime/1000+","+(double)decompressionTime/(1000*60)+"\n");
        }
        fw.close();
    }


    /*public static String oldWriteCompress(int N) {
        //gather all lud files
        List<String> locations = ReadAllGameFiles.findAllGames("res/Ludii/lud");
        String input = LudiiFileCleanup.allLinesOneString(locations.get(0));
        NGramModelLudii m = new NGramModelLudii(input,N);
        locations = locations.subList(1, locations.size());
        for(String s : locations) {
            m.addToModel(LudiiFileCleanup.allLinesOneString(s));
        }
        String path = "res/Compression2/LudiiModel"+N+".csv";
        m.writeModel(path);
        String compressedPath = "res/Compression2/CompressedLudiiModel"+N+".gz";
        GZIPCompression.compress(path,compressedPath);
        List<String> context = Arrays.asList("(");
        int i = 1;
        for(String rec : m.getPicklist(context)) {
            System.out.println(i+". "+rec);
            i++;
        }
        return compressedPath;
    }*/
}
