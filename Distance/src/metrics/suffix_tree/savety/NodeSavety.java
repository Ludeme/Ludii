package metrics.suffix_tree.savety;

import java.util.ArrayList;
import java.util.List;

import metrics.suffix_tree.Letter;

public class NodeSavety
{
	NodeSavety parent;
	NodeSavety linkNode;
	Letter l;
	int count=0;
	
	ArrayList<NodeSavety> children = new ArrayList<>();
	
	public void insert(final List<Letter> toInsert)
	{
		if (children.size()==0||toInsert.size()==0) {
			
		}else {
			final Letter affix = toInsert.get(0);
			final List<Letter> suffix = toInsert.subList(1, toInsert.size()-1);
			foorLoop:for (final NodeSavety node : children)
			{
				if (node.l.equals(affix)){
					node.insert(suffix);
					
					break foorLoop;
				}
			}
		}
		
	}

}
