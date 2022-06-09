package codecompletion.domain.filehandling;

import codecompletion.domain.model.NGram;
import utils.FileUtils;
import utils.GZIPController;
import utils.Model2CSV;
import utils.StringUtils;

public class ModelFilehandler {
    public static final String MODEL_LOCATION = DocHandler.getInstance().getModelsLocation()+"/ngram_model_";
    /**
     * This method performs the operation of extracting a model from it's file.
     * @param N
     * @return
     */
    public static NGram readModel(int N) {
        DocHandler docHandler = DocHandler.getInstance();
        String location = docHandler.getModelLocation(N);
        if(location == null) {
            throw new NullPointerException("Model does not exist yet");
        }
        //removes file extension
        location = StringUtils.removeSuffix(location,".gz");
        //decompress
        GZIPController.decompress(location+".gz",location+".csv");
        //model2csv
        NGram model = Model2CSV.csv2model(location+".csv");
        return model;
    }

    public static void writeModel(NGram model) {
        int N = model.getN();
        String location = MODEL_LOCATION+N;
        //write to csv file
        Model2CSV.model2csv(model, location+".csv");
        //compress
        GZIPController.compress(location+".csv",location+".gz");
        //Delete .csv file
        FileUtils.deleteFile(location+".csv");
        //add the location to the documents.txt
        DocHandler docHandler = DocHandler.getInstance();
        docHandler.addModelLocation(N,location+".gz");
    }
}
