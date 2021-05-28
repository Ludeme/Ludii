package games;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//-----------------------------------------------------------------------------

/**
 * Pseudo UCT node for UCB cache test.
 * @author cambolbro
 */
public class Node
{
	public static final int BranchingFactor = 10;

	private final Node parent;
	private int visits = 0;
	private final List<Integer> actions = new ArrayList<>();
	private final List<Node> children = new ArrayList<>();

	//-------------------------------------------------------------------------

	public Node(final int action, final Node parent)
	{
		this.parent = parent;
		this.visits = 1;
		for (int n = 0; n < BranchingFactor; n++)
			actions.add(Integer.valueOf(n));
		Collections.shuffle(actions);
	}

	//-------------------------------------------------------------------------

	public int visits()
	{
		return visits;
	}

	public void visit()
	{
		visits++;
		if (parent != null)
			parent.visit();
	}

	public boolean allVisited()
	{
		return actions.isEmpty();
	}

	public List<Node> children()
	{
		return children;
	}

	public int choose()
	{
		final int choice = actions.get(0).intValue();
		actions.remove(0);
		return choice;
	}

	public int size()
	{
		int count = 1;
		for (final Node child : children)
			count += child.size();
		return count;
	}

	//-------------------------------------------------------------------------

}
