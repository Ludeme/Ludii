package metrics.suffix_tree;

import java.util.Collection;
import java.util.List;

public interface Node
{

	int getId();

	String getLabel(boolean asColumn, boolean representativeString);

	Node getSuflink();

	Collection<? extends Node> getNodeChildren();

	int getSupport();

	List<Letter> getFullPathLabel();

	boolean isLeaf();

	boolean isRoot();

	List<Letter> getPathLabel(boolean cutAfterSeperator);

}
