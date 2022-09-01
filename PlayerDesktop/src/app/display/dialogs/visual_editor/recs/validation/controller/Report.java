package app.display.dialogs.visual_editor.recs.validation.controller;

import app.display.dialogs.visual_editor.recs.utils.CSVUtils;
import app.display.dialogs.visual_editor.recs.utils.StringUtils;

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
        recordedVariables = Arrays.asList(new String[]{"i","time_nano",
                "top_1_precision_training","top_3_precision_training","top_5_precision_training","top_7_precision_training",
                "top_1_precision_test","top_3_precision_test","top_5_precision_test","top_7_precision_test"});
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
     * "i","time_nano",
     * "top_1_precision","top_3_precision","top_5_precision","top_7_precision"
     *
     * It also converts this list of values into one string
     * @param record
     */
    public void addRecord(List<Double> record) {
        String line = "";
        double iDouble = record.get(0).doubleValue();
        int i = (int) iDouble;
        line += i + ",";

        //times
        double nanoDouble = record.get(1).doubleValue();
        long nano = (long) nanoDouble;
        line += nano + ",";

        // precision
        double top1Training = record.get(2).doubleValue();
        line += top1Training + ",";

        double top3Training = record.get(3).doubleValue();
        line += top3Training + ",";

        double top5Training = record.get(4).doubleValue();
        line += top5Training + ",";

        double top7Training = record.get(5).doubleValue();
        line += top7Training + ",";

        double top1Test = record.get(6).doubleValue();
        line += top1Test + ",";

        double top3Test = record.get(7).doubleValue();
        line += top3Test + ",";

        double top5Test = record.get(8).doubleValue();
        line += top5Test + ",";

        double top7Test = record.get(9).doubleValue();
        line += top7Test;

        lines.add(line);
    }

    public void writeToCSV() {
        CSVUtils.writeCSV(location, header, lines);
    }

	public int getN()
	{
		return N;
	}
}
