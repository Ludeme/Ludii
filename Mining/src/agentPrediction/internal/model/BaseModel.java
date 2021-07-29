package agentPrediction.internal.model;

import java.util.List;

import gnu.trove.list.array.TIntArrayList;
import manager.Manager;

public interface BaseModel 
{
	/** The name of the model used, must match up with the filepath to the stored model. */
	String modelName();

	/** Returns the predicted win-rate for each agent in agentStrings, based on the game concepts. */
	double[] predictAI(final Manager manager, List<String> flags, TIntArrayList flagsValues, String[] agentStrings);

}
