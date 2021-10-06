package metrics.suffix_tree.savety;
//package metrics.suffix_tree;
//
//import java.io.File;
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map.Entry;
//import java.util.TreeSet;
//
//import common.CountMap;
//import common.DistanceUtils;
//import common.LudRul;
//import util.Trial;
//
//public class SuffixTree3 implements Serializable
//{
//
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = 1L;
//	private final Node startNode;
//	private final Alphabet a;
//	private final String name;
//	// private ArrayList<Node> allNodes;
//
//	public SuffixTree3(final String name, final String[] concatWords)
//	{
//		this.name = name;
//		final HashSet<String> wordList = new HashSet<>(
//				Arrays.asList(concatWords));
//		this.a = new Alphabet(wordList);
//		System.out.println(a);
//		startNode = new Node();
//		final ArrayList<Letter> convert = a.encode(concatWords);
//		insert(convert);
//		this.cleanAndTrim();
//	}
//
//	public SuffixTree3(final String name, final String[][] concatWords)
//	{
//		this.name = name;
//		final HashSet<String> wordList = new HashSet<>();
//		for (final String[] string : concatWords)
//		{
//			wordList.addAll(Arrays.asList(string));
//		}
//		this.a = new Alphabet(wordList);
//		System.out.println(a);
//		startNode = new Node();
//		for (int i = 0; i < concatWords.length; i++)
//		{
//			final String[] string = concatWords[i];
//			final ArrayList<Letter> convert = a.encode(string);
//			insert(convert);
//			System.out.println("inserted " + i + " of " + concatWords.length);
//		}
//
//		this.cleanAndTrim();
//	}
//
//	public SuffixTree3(
//			final String name, final TreeBuildingIngredients ingredients
//	)
//	{
//		final long t3 = System.currentTimeMillis();
//		this.a = ingredients.getAlphabet();
//		this.name = name;
//		this.startNode = new Node();
//		buildTree(ingredients.getConvertedTrials());
//		final long t4 = System.currentTimeMillis();
//		final long dt = t4 - t3;
//		final double nNodes = startNode.countNodes();
//		final double nLetters = ingredients.getNumLetters();
//		System.out.println("building took " + dt + " for " + name);
//		System.out.println("nodes To Time: " + nNodes / dt + " with nodes "
//				+ (int) nNodes);
//		System.out.println("letters To Time: " + nLetters / dt
//				+ " with letters " + (int) nLetters);
//	}
//
//	public SuffixTree3(
//			final String name, final Trial[] trials, final Letteriser let
//	)
//	{
//		this.name = name;
//
//		final long t3 = System.currentTimeMillis();
//
//		final TreeBuildingIngredients ingredients = let
//				.createTreeBuildingIngredients(trials);
//		this.a = ingredients.getAlphabet();
//		startNode = new Node();
//
//		buildTree(ingredients.getConvertedTrials());
//
//		// allNodes = getAllNodesDepthFirst();
//		final long t2 = System.currentTimeMillis();
//		final long dt2 = t3 - t2;
//		System.out.println("Tree size: " + startNode.countNodes()
//				+ " building took: " + dt2 + " counting took: " + dt2 + "ms");
//	}
//
//	private void buildTree(final ArrayList<ArrayList<Letter>> convertedTrials)
//	{
//		insertConverted(convertedTrials);
//		cleanAndTrim();
//	}
//
//	private void insertConverted(
//			final ArrayList<ArrayList<Letter>> convertedTrials
//	)
//	{
//
//		for (final ArrayList<Letter> convTrial : convertedTrials)
//		{
//			insert(convTrial);
//		}
//	}
//
//	private ArrayList<Node> getAllNodesDepthFirst()
//	{
//		final ArrayList<Node> allNodesLocal = getAllNodesDepthFirst(startNode,
//				new ArrayList<Node>());
//		allNodesLocal.trimToSize();
//		return allNodesLocal;
//	}
//
//	private ArrayList<Node> getAllNodesDepthFirst(
//			final Node currentNode, final ArrayList<Node> nodes
//	)
//	{
//		if (currentNode.isLeaf())
//			return nodes;
//		for (final Node node : currentNode.children)
//		{
//			nodes.add(node);
//			getAllNodesDepthFirst(node, nodes);
//		}
//		return nodes;
//	}
//
//	public String getName()
//	{
//		return name;
//	}
//
//	private void insert(final ArrayList<Letter> converted)
//	{
//		algorithmRemeberLastLeaf(converted);
//	}
//
//	public SuffixTree3(final String testWord)
//	{
//		this(null, testWord.split(""));
//	}
//
//	public SuffixTree3(final List<String> testWords)
//	{
//		this(null, arrayifyAndSplit(testWords));
//	}
//
//	private static String[][] arrayifyAndSplit(final List<String> testWords)
//	{
//		final String[][] s = new String[testWords.size()][];
//		for (int i = 0; i < s.length; i++)
//		{
//			s[i] = testWords.get(i).split("");
//		}
//
//		return s;
//	}
//
//	public SuffixTree3(
//			final LudRul c, final int num_playouts, final int num_maxMoves,
//			final Letteriser letteriser
//	)
//	{
//		this(c.getGameNameIncludingOption(true),
//				DistanceUtils.generateRandomTrialsFromGame(c.getGame(),
//						num_playouts, num_maxMoves),
//				letteriser);
//	}
//
//	public void printAlphabet()
//	{
//		System.out.println(a);
//
//	}
//
//	private void algorithmRemeberLastLeaf(final ArrayList<Letter> convert)
//	{
//
//		// System.out.println("fullList: " + convert);
//		Node lastInsertionPoint = startNode;
//		final LinkedList<Letter> toInsert = new LinkedList<>();
//		toInsert.add(Letter.emptySuffix);
//		lastInsertionPoint = lastInsertionPoint
//				.insertAndReturnMainInsertionPoint(toInsert);
//		for (int i = 0; i < convert.size(); i++)
//		{
//			toInsert.clear();
//			toInsert.add(convert.get(i));
//			toInsert.add(Letter.emptySuffix);
//			lastInsertionPoint = lastInsertionPoint
//					.insertAndReturnMainInsertionPoint(toInsert);
//			// startNode.printTree(0);
//			// System.out.println("");
//		}
//
//		final HashMap<Letter, Boolean> checked = new HashMap<>(); // hashmap as
//																	// nodes are
//																	// unique
//																	// and
//																	// faster
//		Node pushDownNode = lastInsertionPoint; // ignore the empty suffix
//
//		// traverseMainString
//
//		while (pushDownNode != null)
//		{
//			pushDownNode.pushdown = 1;
//			pushDownNode = pushDownNode.parent;
//		}
//		pushDownNode = lastInsertionPoint; // ignore the empty suffix
//		while (pushDownNode.l != null)
//		{
//			if (pushDownNode.pushdown != 0)
//			{
//				pushDownNode.pushDown(0);
//
//			}
//
//			pushDownNode = pushDownNode.parent;
//		}
//		System.out.println("pushdowncounter" + Node.pushdownCounter);
//		// check if something is left to pushdown
//		final int nNodes = 1;
//		final ArrayList<Node> nodes = new ArrayList<>();
//		nodes.add(startNode);
//		Node.pushdownCounter = 0;
//		/*
//		 * while(!nodes.isEmpty()) { final Node n =
//		 * nodes.remove(nodes.size()-1); nNodes++; if (n.pushdown!= 0) {
//		 * n.pushdown=0; } nodes.addAll(n.children); }
//		 * System.out.println("nnodes " + nNodes);
//		 */
//
//		final Runtime runtime = Runtime.getRuntime();
//		final long memory = runtime.totalMemory() - runtime.freeMemory();
//		System.out.println("Used memory is bytes: " + memory);
//		System.out.println("Used memory is megabytes: "
//				+ (memory) / 1024 / 1024);
//
//		// System.out.println("Tree size: " + startNode.countNodes());
//		// startNode.printTree(0);
//	}
//
//	public ArrayList<ArrayList<Node>> getSortedNodesByDepth()
//	{
//		final ArrayList<ArrayList<Node>> debthSorted = new ArrayList<ArrayList<Node>>();
//		final ArrayList<Node> nodes = new ArrayList<>();
//		final HashMap<Node, Boolean> checked = new HashMap<>();
//		final LinkedList<Node> holdup = new LinkedList<Node>();
//
//		holdup.add(startNode);
//		checked.put(startNode, Boolean.valueOf(true));
//		while (holdup.size() != 0)
//		{
//			final Node first = holdup.removeFirst();
//			System.out.println(first.getId() + " " + first.getDepth());
//			nodes.add(first);
//			for (final Node firstChild : first.children)
//			{
//				final Boolean oldVal = checked.put(firstChild,
//						Boolean.valueOf(true));
//				if (oldVal != null)
//					continue;
//				holdup.add(firstChild);
//			}
//		}
//		for (final Node node : nodes)
//		{
//			final int d = node.getDepth();
//			while (d >= debthSorted.size())
//			{
//				debthSorted.add(new ArrayList<>());
//			}
//
//			final ArrayList<Node> list = debthSorted.get(d);
//			list.add(node);
//		}
//
//		return debthSorted;
//	}
//
//	public int getNOccurences(final String testSubString)
//	{
//		return getNOccurences(testSubString.split(""));
//	}
//
//	private int getNOccurences(final String[] split)
//	{
//		return getNOccurences(a.encode(split));
//	}
//
//	private int getNOccurences(final ArrayList<Letter> encode)
//	{
//		Node currentNode = startNode;
//		final LinkedList<Letter> find = new LinkedList<>(encode);
//
//		whileLoop: while (find.size() != 0)
//		{
//			final Letter currentLetter = find.getFirst();
//			for (final Node n : currentNode.children)
//			{
//				if (n.l.equals(currentLetter))
//				{
//					find.removeFirst();
//					currentNode = n;
//					continue whileLoop;
//				}
//			}
//			System.out.println("Letter not found. Something wrong");
//		}
//		return currentNode.count;
//	}
//
//	public Node getRootNode()
//	{
//		return startNode;
//	}
//
//	public Alphabet getAlphabet()
//	{
//		return a;
//	}
//
//	public void cleanAndTrim()
//	{
//		startNode.cleanAndTrim();
//	}
//
//	public void printSupport()
//	{
//		final ArrayList<Entry<Integer, Integer>> es = getSortedCountedSupport();
//
//		for (final Entry<Integer, Integer> entry : es)
//		{
//			System.out.println("Support " + entry.getKey() + " appears "
//					+ entry.getValue());
//		}
//	}
//
//	private ArrayList<Entry<Integer, Integer>> getSortedCountedSupport()
//	{
//		final CountMap<Integer> supportsToOccurence = countSupport();
//		final ArrayList<Entry<Integer, Integer>> es = supportsToOccurence
//				.getSortedByKey();
//		return es;
//	}
//
//	private CountMap<Integer> countSupport()
//	{
//		final CountMap<Integer> supToCount = new CountMap<Integer>();
//		final LinkedList<Node> liste = new LinkedList<>();
//		liste.push(startNode);
//		while (!liste.isEmpty())
//		{
//			final Node current = liste.removeFirst();
//			final int count = current.count;
//			supToCount.addInstance(Integer.valueOf(count));
//			final TreeSet<Node> children = current.children;
//			for (final Node child : children)
//			{
//				liste.addFirst(child);
//			}
//		}
//		return supToCount;
//	}
//
//	public void exportGml(
//			final File folder, final String fileName,
//			final boolean showSuffixLinks
//	)
//	{
//		final GraphComposer gc = new GraphComposer();
//		// gc.compose(folder, fileName, this, showSuffixLinks);
//
//	}
//
//}
