package metrics.suffix_tree;

import java.io.File;
import java.util.ArrayList;

public interface SuffixTree
{

	ArrayList<Node> getAllNodes();

	/**
	 * exports the suffix tree to a gml file which can be opened by the yed graph editor
	 * @param folder output folder
	 * @param fileName output filename without extension
	 * @param showSuffixLinks ((should usually be false, but shows the suffix links used for the construction))
	 */
	public default void exportGml(final File folder, final String fileName, final boolean showSuffixLinks)
	{
		final GraphComposer gc = new GraphComposer();
		gc.compose(folder, fileName, this, showSuffixLinks);
		
	}

	void printAlphabet();

	/**
	 * 
	 * @param pattern the string pattern to be search for
	 * @return the number of occurrences of this patter
	 */
	int getNumOccurences(String pattern);
	
}
