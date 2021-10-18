package utils.data_structures.support.zhang_shasha;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;

import gnu.trove.list.array.TIntArrayList;

/**
 * Code originally from: https://github.com/ijkilchenko/ZhangShasha
 * 
 * Afterwards modified for style / various improvements
 *
 * @author Dennis Soemers
 */
public class Tree 
{
	Node root = new Node();

	// function l() which gives the leftmost child
	TIntArrayList l = new TIntArrayList();
	
	/** List of keyroots, i.e., nodes with a left child and the tree root */
	TIntArrayList keyroots = new TIntArrayList();
	
	/** List of the labels of the nodes used for node comparison */
	ArrayList<String> labels = new ArrayList<String>();

	/**
	 * Constructor for tree described in preorder notation, e.g. f(a b(c))
	 * @param s
	 * @throws IOException
	 */
	public Tree(final String s)
	{
		try
		{
			final StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(s));
			tokenizer.nextToken();
			root = parseString(root, tokenizer);
			if (tokenizer.ttype != StreamTokenizer.TT_EOF) 
			{
				throw new RuntimeException("Leftover token: " + tokenizer.ttype);
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Constructor with root already given
	 * @param root
	 */
	public Tree(final Node root)
	{
		this.root = root;
	}

	private static Node parseString(final Node node, final StreamTokenizer tokenizer) throws IOException 
	{
		node.label = tokenizer.sval;
		tokenizer.nextToken();
		if (tokenizer.ttype == '(') 
		{
			tokenizer.nextToken();
			do 
			{
				node.children.add(parseString(new Node(), tokenizer));
			} 
			while (tokenizer.ttype != ')');
			tokenizer.nextToken();
		}
		return node;
	}

	public void traverse() 
	{
		// put together an ordered list of node labels of the tree
		traverse(root, labels);
	}

	private static void traverse(final Node node, final ArrayList<String> labels) 
	{
		for (int i = 0; i < node.children.size(); i++) 
		{
			traverse(node.children.get(i), labels);
		}
		labels.add(node.label);
	}

	public void index() 
	{
		// index each node in the tree according to traversal method
		index(root, 0);
	}

	private static int index(final Node node, final int indexIn) 
	{
		int index = indexIn;
		for (int i = 0; i < node.children.size(); i++) 
		{
			index = index(node.children.get(i), index);
		}
		index++;
		node.index = index;
		return index;
	}

	public void l() 
	{
		// put together a function which gives l()
		leftmost();
		l = new TIntArrayList();
		l(root, l);
		
	}

	private void l(final Node node, final TIntArrayList ll) 
	{
		for (int i = 0; i < node.children.size(); i++) 
		{
			l(node.children.get(i), ll);
		}
		ll.add(node.leftmost.index);
	}

	private void leftmost() 
	{
		leftmost(root);
	}

	private static void leftmost(final Node node) 
	{
		if (node == null)
			return;
		
		for (int i = 0; i < node.children.size(); i++) 
		{
			leftmost(node.children.get(i));
		}
		if (node.children.size() == 0) 
		{
			node.leftmost = node;
		} 
		else 
		{
			node.leftmost = node.children.get(0).leftmost;
		}
	}

	public void keyroots() 
	{
		// calculate the keyroots
		for (int i = 0; i < l.size(); i++) 
		{
			int flag = 0;
			for (int j = i + 1; j < l.size(); j++) 
			{
				if (l.getQuick(j) == l.getQuick(i)) 
				{
					flag = 1;
				}
			}
			if (flag == 0) 
			{
				keyroots.add(i + 1);
			}
		}
	}

	public static int ZhangShasha(final Tree tree1, final Tree tree2) 
	{
		tree1.index();
		tree1.l();
		tree1.keyroots();
		tree1.traverse();
		tree2.index();
		tree2.l();
		tree2.keyroots();
		tree2.traverse();

		final TIntArrayList l1 = tree1.l;
		final TIntArrayList keyroots1 = tree1.keyroots;
		final TIntArrayList l2 = tree2.l;
		final TIntArrayList keyroots2 = tree2.keyroots;

		// space complexity of the algorithm
		final int[][] TD = new int[l1.size() + 1][l2.size() + 1];

		// solve subproblems
		for (int i1 = 1; i1 < keyroots1.size() + 1; i1++) 
		{
			for (int j1 = 1; j1 < keyroots2.size() + 1; j1++) 
			{
				final int i = keyroots1.getQuick(i1 - 1);
				final int j = keyroots2.getQuick(j1 - 1);
				TD[i][j] = treedist(l1, l2, i, j, tree1, tree2, TD);
			}
		}

		return TD[l1.size()][l2.size()];
	}

	private static int treedist
	(
		final TIntArrayList l1, 
		final TIntArrayList l2, 
		final int i, 
		final int j, 
		final Tree tree1, 
		final Tree tree2,
		final int[][] TD
	)
	{
		final int[][] forestdist = new int[i + 1][j + 1];

		// costs of the three atomic operations
		final int Delete = 1;
		final int Insert = 1;
		final int Relabel = 1;

		forestdist[0][0] = 0;
		for (int i1 = l1.getQuick(i - 1); i1 <= i; i1++) 
		{
			forestdist[i1][0] = forestdist[i1 - 1][0] + Delete;
		}
		for (int j1 = l2.getQuick(j - 1); j1 <= j; j1++) 
		{
			forestdist[0][j1] = forestdist[0][j1 - 1] + Insert;
		}
		for (int i1 = l1.getQuick(i - 1); i1 <= i; i1++) 
		{
			for (int j1 = l2.getQuick(j - 1); j1 <= j; j1++) 
			{
				final int i_temp = (l1.getQuick(i - 1) > i1 - 1) ? 0 : i1 - 1;
				final int j_temp = (l2.getQuick(j - 1) > j1 - 1) ? 0 : j1 - 1;
				if ((l1.getQuick(i1 - 1) == l1.getQuick(i - 1)) && (l2.getQuick(j1 - 1) == l2.get(j - 1))) 
				{
					final int Cost = (tree1.labels.get(i1 - 1).equals(tree2.labels.get(j1 - 1))) ? 0 : Relabel;
					forestdist[i1][j1] = Math.min(
							Math.min(forestdist[i_temp][j1] + Delete, forestdist[i1][j_temp] + Insert),
							forestdist[i_temp][j_temp] + Cost);
					TD[i1][j1] = forestdist[i1][j1];
				} 
				else 
				{
					final int i1_temp = l1.getQuick(i1 - 1) - 1;
					final int j1_temp = l2.getQuick(j1 - 1) - 1;

					final int i_temp2 = (l1.getQuick(i - 1) > i1_temp) ? 0 : i1_temp;
					final int j_temp2 = (l2.getQuick(j - 1) > j1_temp) ? 0 : j1_temp;

					forestdist[i1][j1] = Math.min(
							Math.min(forestdist[i_temp][j1] + Delete, forestdist[i1][j_temp] + Insert),
							forestdist[i_temp2][j_temp2] + TD[i1][j1]);
				}
			}
		}
		return forestdist[i][j];
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		toString(root, sb, 0);
		return sb.toString();
	}
	
	private static void toString(final Node node, final StringBuilder sb, final int indent)
	{
		for (int i = 0; i < indent; ++i)
		{
			sb.append(" ");
		}
		
		sb.append(node.label + "\n");
		for (final Node child : node.children)
		{
			toString(child, sb, indent + 2);
		}
	}
}
