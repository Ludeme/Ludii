package metrics.suffix_tree;

import java.util.HashSet;
import java.util.List;

import common.LudRul;
import metrics.suffix_tree.SuffixTreeCollapsed.LeafNode;
import metrics.suffix_tree.SuffixTreeCollapsed.N;

public class QuerryResult
{

	private final N current;
	private final List<LeafNode> leavesUnderneath;
	private final int depth;

	public QuerryResult(final N current, final List<LeafNode> leavesUnderneath, final int depth)
	{
		this.current = current;
		this.leavesUnderneath = leavesUnderneath;
		this.depth = depth;
	}

	public PercentageMap<String> getLeaveTypePercentageMap()
	{
		final PercentageMap<String> pm = new PercentageMap<>();
		final HashSet<Seperator> uniqueSeperators = new HashSet<>();
		for (final LeafNode leafNode : leavesUnderneath)
		{
			uniqueSeperators.add(leafNode.getSeperator());
		}
		for (final Seperator seperator : uniqueSeperators)
		{
			@SuppressWarnings("unchecked")
			final
			ContainerSeperator<LudRul> realSep = (ContainerSeperator<LudRul>) seperator;
			final String cfn = realSep.getContainer().getCurrentClassName();
			pm.addInstance(cfn);
		}
		pm.getHashMap(); //to calculate frequency now and not later.
		return pm;
	}

	public N getCurrent()
	{
		return current;
	}

	public int getDepth()
	{
		return depth;
	}

}
