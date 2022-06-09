package validation.controller;

import utils.CSVUtils;
import utils.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Report {

    private final int N;
    private final List<String> recordedVariables;
    private final String header;
    private List<String> lines;
    private String location;

    public Report(int N, String location) {
        this.N = N;
        this.location = location;
        recordedVariables = Arrays.asList(new String[]{"i","time_nano","time_milis", "time_s","time_min","time_h",
                "top_1_precision","top_3_precision","top_5_precision","top_7_precision"});
        String tempHeader = "";
        for(String variable : recordedVariables) {
            tempHeader += variable + ",";
        }
        tempHeader = StringUtils.removeSuffix(tempHeader,",");
        header = tempHeader;
        lines = new ArrayList<>();
    }

    /**
     * This requires a list of doubles with these values in this order:
     * "i","time_nano","time_milis", "time_s","time_min","time_h",
     * "top_1_precision","top_3_precision","top_5_precision","top_7_precision"
     *
     * It also converts this list of values into one string
     * @param record
     */
    public void addRecord(List<Double> record) {
        String line = "";
        double iDouble = record.get(0);
        int i = (int) iDouble;
        line += i + ",";

        //times
        double nanoDouble = record.get(1);
        long nano = (long) nanoDouble;
        line += nano + ",";

        double milisDouble = record.get(2);
        long milis = (long) milisDouble;
        line += milis + ",";

        double sDouble = record.get(3);
        int s = (int) sDouble;
        line += s + ",";

        double minDouble = record.get(4);
        int min = (int) minDouble;
        line += min + ",";

        double hDouble = record.get(5);
        int h = (int) hDouble;
        line += h + ",";

        // precision
        double top1 = record.get(6);
        line += top1 + ",";

        double top3 = record.get(7);
        line += top3 + ",";

        double top5 = record.get(8);
        line += top5 + ",";

        double top7 = record.get(9);
        line += top7;

        lines.add(line);
    }

    public void writeToCSV() {
        CSVUtils.writeCSV(location, header, lines);
    }
}
