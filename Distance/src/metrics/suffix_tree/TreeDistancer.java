package metrics.suffix_tree;

import java.util.Iterator;
import java.util.TreeMap;

import metrics.suffix_tree.SuffixTreeCollapsed.DummyNode;
import metrics.suffix_tree.SuffixTreeCollapsed.N;
import sandbox.Weighter;

public abstract class TreeDistancer
{
	/**
	 * Just look at the different numbers in the nodes
	 */
	public static TreeDistancer naiveDistance = TreeDistancer.getNaiveTreeDistance();
	public static TreeDistancer jaccardDistance = TreeDistancer.getJaccardDistance();

	
	private static TreeDistancer getNaiveTreeDistance()
	{
		return new TreeDistancer()
		{
			@Override
			public double distance(final SuffixTreeCollapsed sta, final SuffixTreeCollapsed stb)
			{
				final Weighter w = Weighter.noWeight;
				final DistanceFunction d = DistanceFunction.Manhattan;
				final double[] score = TreeDistancer.postOrderTraversal(sta,stb,d,w);
				 
				return score[0];
			}
		};
	}
	private static TreeDistancer getJaccardDistance()
	{
		return new TreeDistancer()
		{
			@Override
			public double distance(final SuffixTreeCollapsed sta, final SuffixTreeCollapsed stb)
			{
				final Weighter w = Weighter.noWeight;
				final DistanceFunction d = DistanceFunction.JACCARD;
				final double[] score = TreeDistancer.postOrderTraversal(sta,stb,d,w);
				 
				return score[0]/score[1];
			}
		};
	}
	
	
	protected static double[] postOrderTraversal(
			final SuffixTreeCollapsed sta, final SuffixTreeCollapsed stb, final DistanceFunction d, final Weighter w
	)
	{
		final AlphabetComparer ac = new AlphabetComparer(sta.getAlphabet(),stb.getAlphabet());
		final int depth = 0;
		final N ra = sta.getRootNode();
		final N rb = stb.getRootNode();
		final DummyNode dn = sta.getDummyNode();
		return postOrderTraversal(dn,ac, depth, ra, rb, d,w);
	}


	private static double[] postOrderTraversal(
			final DummyNode dummyNode, final AlphabetComparer ac, final int depth, final N na, final N nb, final DistanceFunction d, final Weighter w
	)
	{
		if (na.isDummyNode()&&nb.isDummyNode())return new double[]{0,0};
		//double score = d.distance(w.weight(depth, depth), na.getCount(), nb.getCount());
		final double[] score = d.distance(w.weight(depth, depth), na.getNumOccurences(), nb.getNumOccurences());
		
		final TreeMap<Letter,N> childrenA = na.getChildren();
		final TreeMap<Letter,N> childrenB = nb.getChildren();
		
		final Iterator<N> iteratorA = childrenA.values().iterator();
		final Iterator<N> iteratorB = childrenB.values().iterator();
		N childA = iteratorA.hasNext()?iteratorA.next():null;
		N childB = iteratorB.hasNext()?iteratorB.next():null;
		while(childA!=null||childB!=null) {
			if (childA==null) {
				addToFirst(score,postOrderTraversal(dummyNode,ac,depth+1,dummyNode,childB,d,w));
				childB = iteratorB.hasNext()?iteratorB.next():null;
				continue;
			}
			if (childB==null) {
				addToFirst(score,postOrderTraversal(dummyNode,ac,depth+1,childA,dummyNode,d,w));
				childA = iteratorA.hasNext()?iteratorA.next():null;
				continue;
			}
			if (ac.containsEqualLetter(childA, childB)) {
				addToFirst(score,postOrderTraversal(dummyNode,ac,depth+1,childA,childB,d,w));
				childA = iteratorA.hasNext()?iteratorA.next():null;
				childB = iteratorB.hasNext()?iteratorB.next():null;
				continue;
			}
			final int compare = childA.getNthLetter(0).compare(childB.getNthLetter(0));
			if (compare < 0) {
				addToFirst(score,postOrderTraversal(dummyNode,ac,depth+1,childA,dummyNode,d,w));
				childA = iteratorA.hasNext()?iteratorA.next():null;
			}else {
				addToFirst(score,postOrderTraversal(dummyNode,ac,depth+1,dummyNode,childB,d,w));
				childB = iteratorB.hasNext()?iteratorB.next():null;
			}
		}
		
		
		return score;
	}


	private static void addToFirst(final double[] score, final double[] postOrderTraversal)
	{
		score[0] += postOrderTraversal[0];
		score[1] += postOrderTraversal[1];
	}

	public abstract double distance(SuffixTreeCollapsed sta, SuffixTreeCollapsed stb);

	

}
