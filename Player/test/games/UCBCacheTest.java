package games;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

//-----------------------------------------------------------------------------

/**
 * Test whether caching of UCB calculation helps.
 * @author cambolbro
 */
public class UCBCacheTest
{
	private static final double C = 0.5;

	int numUCB;

	final Map<Long, Double> ucbMap = new HashMap<>();

	//-------------------------------------------------------------------------

	@Test
	public void test()
	{
		//fail("Not yet implemented");

		System.out.println("UCB cache test.");

		final int numTests = 10;

		// Without map
		numUCB = 0;
		long startAt = System.nanoTime();
		for (int test = 0; test < numTests; test++)
			makeTree(false);
		long endAt = System.nanoTime();
		double secs = (endAt - startAt) / 1000000000.0;
		System.out.println("Without : " + numTests + " tests in " + secs + "s.");
		System.out.println((numUCB / numTests) + " UCB on average per test.");

		// With map
		numUCB = 0;
		startAt = System.nanoTime();
		for (int test = 0; test < numTests; test++)
			makeTree(true);
		endAt = System.nanoTime();
		secs = (endAt - startAt) / 1000000000.0;
		System.out.println("With map: " + numTests + " tests in " + secs + "s.");
		System.out.println((numUCB / numTests) + " UCB on average per test.");
		System.out.println("ucbMap has " + ucbMap.size() + " entries.");
	}

	//-------------------------------------------------------------------------

	void makeTree(final boolean useMap)
	{
		final Node root = new Node(-1, null);
		final int numIterations = 1000000;

		for (int n = 0; n < numIterations; n++)
		{
			Node node = descend(root, useMap);
			node = expand(node);
			node.visit();
		}
		//		System.out.println("Tree size " + root.size() + ".");
	}

	//-------------------------------------------------------------------------

	Node descend(final Node nodeIn, final boolean useMap)
	{
		Node node = nodeIn;
		while (node.allVisited())
			node = ucb(node, useMap);
		return node;
	}

	//-------------------------------------------------------------------------

	@SuppressWarnings("static-method")
	Node expand(final Node parent)
	{
		final Node child = new Node(parent.choose(), parent);
		parent.children().add(child);
		return child;
	}

	//-------------------------------------------------------------------------

	@SuppressWarnings("static-method")
	Long key(final int parentVisits, final int childVisits)
	{
		return Long.valueOf((((long) parentVisits) << 32) | childVisits);
	}

	//-------------------------------------------------------------------------

	Node ucb(final Node parent, final boolean useMap)
	{
		Node choice = null;
		double bestScore = -1000;

		numUCB++;

		if (useMap)
		{
			double parentLog = -1;  //Math.log(Math.max(1, parent.visits()));
			for (final Node child : parent.children())
			{
				final double exploit = 0.5;  // remove reward variation
				double explore = 0;

				final Long key = key(parent.visits(), child.visits());
				final Double item = ucbMap.get(key);
				if (item == null)
				{
					if (parentLog == -1)
						parentLog = Math.log(Math.max(1, parent.visits()));
					explore = Math.sqrt(2 * parentLog / child.visits());
					ucbMap.put(key, Double.valueOf(explore));
				}
				else
				{
					explore = item.doubleValue();
				}
				final double score = exploit + C * explore;

				if (score > bestScore)
				{
					bestScore = score;
					choice = child;
				}
			}
		}
		else
		{
			final double parentLog = Math.log(Math.max(1, parent.visits()));
			for (final Node child : parent.children())
			{
				final double exploit = 0.5;  // remove reward variation
				final double explore = Math.sqrt(2 * parentLog / child.visits());
				final double score = exploit + C * explore;

				if (score > bestScore)
				{
					bestScore = score;
					choice = child;
				}
			}
		}
		return choice;
	}

}
