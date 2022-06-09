package codecompletion.domain.model;

import codecompletion.domain.filehandling.DocHandler;
import interfaces.codecompletion.domain.model.iGrammar;

import java.util.List;

/**
 * @author filreh
 */
public class Grammar implements iGrammar {

    //singleton
    private static Grammar grammar;


    private String location;

    public static Grammar getInstance() {
        // create object if it's not already created
        if(grammar == null) {
            grammar = new Grammar();
        }

        // returns the singleton object
        return grammar;
    }

    private Grammar() {
        this.location = getLocation();
        //TODO initialize Grammar object
    }

    /**
     * This method takes a list of instances with matching keys to the context and filters out the ones
     * that do not match the context, leaving only valid choices behind.
     *
     * @param context
     * @param match
     * @return
     */
    @Override
    public List<Instance> filterOutInvalid(Context context, List<Instance> match) {
        //TODO
        return match;
    }

    @Override
    public String getLocation() {
        if(location == null) {
            DocHandler docHandler = DocHandler.getInstance();
            location = docHandler.getGrammarLocation();
        }
        return location;
    }
}
