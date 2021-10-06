//package metrics.suffix_tree.savety;
//
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.LinkedHashSet;
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
//public class SuffixTreeExpanded implements Serializable,SuffixTree
//{
//	
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = 1L;
//	private final NodeExpanded startNode;
//	private final Alphabet a;
//	private final String name;
//	private ArrayList<Letter> globalString;
//	
//	private final boolean checks = false;
//	//private ArrayList<Node> allNodes;
//
//	public SuffixTreeExpanded(final String name, final String[] concatWords)
//	{
//		this.name = name;
//		final HashSet<String> wordList = new HashSet<>(Arrays.asList(concatWords));
//		this.a = new Alphabet(wordList);
//		System.out.println(a);
//		startNode = new NodeExpanded(null,-1,-1,null); 
//		buildTree(new String[][]{concatWords});
//		
//	}
//	public SuffixTreeExpanded(final String name, final String[][] concatWords)
//	{
//		this.name = name;
//		final HashSet<String> wordList = new HashSet<>();
//		for (final String[] string : concatWords)
//		{
//			wordList.addAll(Arrays.asList(string));
//		}
//		this.a = new Alphabet(wordList);
//		System.out.println(a);
//		startNode = new NodeExpanded(null,-1,-1,null);
//		buildTree(concatWords);
//		
//	}
//
//	
//	public SuffixTreeExpanded(final String name, final TreeBuildingIngredients ingredients)
//	{
//		final long t3 = System.currentTimeMillis();
//		this.a = ingredients.getAlphabet();
//		this.name = name;
//		this.startNode = new NodeExpanded(null,-1,-1,null);
//		buildTree(ingredients.getConvertedTrials());
//		final long t4 = System.currentTimeMillis();
//		final long dt = t4-t3;
//		//final double nNodes = startNode.countNodes();
//		final double nLetters = ingredients.getNumLetters();
//		System.out.println("building took " + dt + " for " + name);
//		//System.out.println("nodes To Time: " + nNodes/dt + " with nodes " + (int)nNodes);
//		System.out.println("letters To Time: " + nLetters/dt + " with letters " + (int)nLetters);
//	}
//	
//	public SuffixTreeExpanded(final String name, final Trial[] trials, final Letteriser let)
//	{
//		this.name = name;
//		
//		final long t3 = System.currentTimeMillis();
//		
//		
//		final TreeBuildingIngredients ingredients = let.createTreeBuildingIngredients(trials);
//		this.a = ingredients.getAlphabet();
//		startNode = new NodeExpanded(null,-1,-1,null);
//		
//		buildTree(ingredients.getConvertedTrials());
//		
//		//allNodes = getAllNodesDepthFirst();
//		final long t2 = System.currentTimeMillis();
//		final long dt2 = t3-t2;
//		System.out.println("Tree size: " + startNode.countNodes()+ " building took: " + dt2 + " counting took: " + dt2 + "ms");
//	}
//
//	private void buildTree(final ArrayList<ArrayList<Letter>> convertedTrials)
//	{
//		NodeExpanded.pushdownCounter = 0;
//		NodeExpanded.pullDownCounter = 0;
//		insertConverted(convertedTrials);
//		
//		cleanAndTrim();	
//		//printNodeCounts(convertedTrials);
//		
//	}
//
//	private void pullDownSupport()
//	{
//		startNode.pullDown();
//		System.out.println(NodeExpanded.pullDownCounter +" vs " + NodeExpanded.pushdownCounter);
//		
//		
//	}
//	private void buildTree(final String[][] concatWords)
//	{
//		final ArrayList<ArrayList<Letter>> convertedStrings = new ArrayList<>();
//		for (int i = 0; i < concatWords.length; i++)
//		{
//			final String[] string = concatWords[i];
//			final ArrayList<Letter> convert = a.encode(string); 
//			convertedStrings.add(convert);
//		}
//		buildTree(convertedStrings);	
//	}
//	
//	
//	private void insertConverted(final ArrayList<ArrayList<Letter>> convertedTrials)
//	{
//		this.globalString = new ArrayList<Letter>();
//		for (int i = 0; i < convertedTrials.size(); i++)
//		{
//			System.out.println("Inserted Sentences" + i + " of " + convertedTrials.size());
//			final ArrayList<Letter> convTrial = convertedTrials.get(i);
//			final int startPointIncl = globalString.size();
//			globalString.addAll(convTrial);
//			final int endPointExcl = globalString.size();
//			algorithmRemeberLastLeaf(convTrial,startPointIncl,endPointExcl);
//		}
//	}
//
//
//	private ArrayList<NodeExpanded> getAllNodesDepthFirst()
//	{
//		final ArrayList<NodeExpanded> allNodesLocal = getAllNodesDepthFirst(startNode,new ArrayList<NodeExpanded>());
//		allNodesLocal.trimToSize();
//		return allNodesLocal;
//	}
//
//
//	private ArrayList<NodeExpanded> getAllNodesDepthFirst(final NodeExpanded currentNode, final ArrayList<NodeExpanded> nodes)
//	{
//		if (currentNode.isLeaf())return nodes;
//		for (final NodeExpanded node : currentNode.children)
//		{
//			nodes.add(node);
//			getAllNodesDepthFirst(node, nodes);
//		}
//		return nodes;
//	}
//
//
//	public String getName()
//	{
//		return name;
//	}
//	
//
//	public SuffixTreeExpanded(final String testWord)
//	{
//		this(null, testWord.split(""));
//	}
//	public SuffixTreeExpanded(final List<String> testWords)
//	{
//		this(null, arrayifyAndSplit(testWords));
//	}
//
//	
//	private static String[][] arrayifyAndSplit(final List<String> testWords)
//	{
//		final String[][] s = new String[testWords.size()][];
//		for (int i = 0; i < s.length; i++)
//		{
//			s[i] = testWords.get(i).split("");
//		}
//		
//		
//		return s;
//	}
//	public SuffixTreeExpanded(final LudRul c, final int num_playouts, final int num_maxMoves, final Letteriser letteriser)
//	{
//		this(c.getGameNameIncludingOption(true),DistanceUtils.generateRandomTrialsFromGame(c.getGame(), num_playouts, num_maxMoves),letteriser);
//	}
//
//	
//	public void printAlphabet()
//	{
//		System.out.println(a);
//		
//	}
//
//
//	private void algorithmRemeberLastLeaf(final ArrayList<Letter> convert, final int startPointGlobalStringIncl, final int endPointGlobalStringIncl)
//	{
//		
//		System.out.println("fullList: " + convert);
//		NodeExpanded lastInsertionPoint = startNode;
//		final LinkedList<Letter> toInsert = new LinkedList<>();
//		toInsert.add(Letter.emptySuffix);
//		final LinkedList<Integer> toInsertGlobalIndex = new LinkedList<>();
//		toInsertGlobalIndex.add(-1);
//		lastInsertionPoint = lastInsertionPoint.insertAndReturnMainInsertionPoint(toInsert,toInsertGlobalIndex);
//		for (int i = 0; i < convert.size(); i++)
//		{
//			toInsert.clear();
//			toInsert.add(convert.get(i));
//			final int letterGlobalStringIndex = startPointGlobalStringIncl+i;
//			toInsert.add(Letter.emptySuffix);
//			toInsertGlobalIndex.clear();
//			toInsertGlobalIndex.add(letterGlobalStringIndex);
//			toInsertGlobalIndex.add(-1);
//			lastInsertionPoint = lastInsertionPoint.insertAndReturnMainInsertionPoint(toInsert,toInsertGlobalIndex);	
//			//startNode.printTree(0);
//			//System.out.println("");
//		}
//		
//		NodeExpanded pushDownNode = lastInsertionPoint; //ignore the empty suffix
//		
//		//traverseMainString
//		
//		LinkedHashSet<NodeExpanded> nodesToPushDown = new LinkedHashSet<>();
//		while (!pushDownNode.isRoot()) {
//			nodesToPushDown.add(pushDownNode);
//			pushDownNode.toPushDown=0;	
//			pushDownNode = pushDownNode.parent;
//		}
//		
//		
//		int startPush = 1;
//		while(!nodesToPushDown.isEmpty()) {
//			final LinkedHashSet<NodeExpanded> nodesToPushDownNextRound = new LinkedHashSet<>();
//			for (final NodeExpanded pushMe : nodesToPushDown)
//			{
//				pushMe.pushDown(startPush,pushMe.letterStartIndex,nodesToPushDownNextRound);
//			}
//			startPush=0;
//			nodesToPushDown = nodesToPushDownNextRound;		
//		}
//		
//		/*
//		pushDownNode = lastInsertionPoint;
//		while (!pushDownNode.isRoot()) {
//			
//			pushDownNode.pushDown(1,pushDownNode.letterStartIndex);	
//			pushDownNode = pushDownNode.parent;	
//		}*/
//		if (checks)
//			checkForMissingPushdowns();
//		System.out.println("pushdowncounter" + NodeExpanded.pushdownCounter);
//		//Node.pushdownCounter = 0;
//		
//		
//		final Runtime runtime = Runtime.getRuntime();
//		final long memory = runtime.totalMemory() - runtime.freeMemory();
//        System.out.println("Used memory is bytes: " + memory);
//        System.out.println("Used memory is megabytes: "
//                + (memory)/1024/1024);
//		
//		//System.out.println("Tree size: " + startNode.countNodes());
//		//startNode.printTree(0);	
//	}
//	
//
//	private void checkForMissingPushdowns()
//	{
//		final LinkedList<NodeExpanded> linkedList = new LinkedList<>();
//		linkedList.add(startNode);
//		while (!linkedList.isEmpty())
//		{
//			final NodeExpanded current = linkedList.removeFirst();
//			if (current.toPushDown!=0) {
//				System.out.println("found error " + current.getRepresentativeWord() + " " + current.toPushDown);
//			}
//			
//			linkedList.addAll(current.children);
//		}
//	}
//	private void printNodeCounts(final ArrayList<ArrayList<Letter>> convertedTrials)
//	{
//		int total = 0;
//		for (final ArrayList<Letter> arrayList : convertedTrials)
//		{
//			total+=arrayList.size();
//		}
//		printNodeCounts(total);
//		
//	}
//	
//	private void printNodeCounts(final int insertedStringLenth)
//	{
//		
//		int total = 0;
//		int leaf = 0;
//		int internal = 0;
//		
//		final LinkedList<NodeExpanded> holdup = new LinkedList<NodeExpanded>();
//		
//		holdup.add(startNode);
//
//		while (holdup.size()!=0)
//		{
//			
//			final NodeExpanded current = holdup.removeFirst();
//			if (current.toPushDown != 0)
//				System.out.println("not everything pushed down");
//			if (current.children.size()==0)
//				leaf++;
//			else
//				internal++;
//			total++;
//			
//			for (final NodeExpanded child : current.children)
//			{
//				holdup.addFirst(child);
//			}
//		}
//		
//		System.out.println(insertedStringLenth + " input lentgth");
//		System.out.println("Optimal would be at most" + (insertedStringLenth+1) + " leaves and " + insertedStringLenth + " internal nodes");
//		System.out.println("Found " + leaf + " leaves and " + internal + " internal nodes and total " + total + " nodes");
//		
//		
//	}
//	public ArrayList<ArrayList<NodeExpanded>> getSortedNodesByDepth()
//	{
//		final ArrayList<ArrayList<NodeExpanded>> debthSorted = new ArrayList<ArrayList<NodeExpanded>>();
//		final ArrayList<NodeExpanded> nodes = new ArrayList<>();
//		final HashMap<NodeExpanded, Boolean> checked = new HashMap<>();
//		final LinkedList<NodeExpanded> holdup = new LinkedList<NodeExpanded>();
//		
//		holdup.add(startNode);
//		checked.put(startNode,Boolean.valueOf(true));
//		while (holdup.size()!=0)
//		{
//			final NodeExpanded first = holdup.removeFirst();
//			System.out.println(first.getId() + " " +first.getDepth());
//			nodes.add(first);
//			for (final NodeExpanded firstChild : first.children)
//			{
//				final Boolean oldVal = checked.put(firstChild, Boolean.valueOf(true));
//				if (oldVal!=null)continue;
//				holdup.add(firstChild);
//			}
//		}
//		for (final NodeExpanded node : nodes)
//		{
//			final int d = node.getDepth();
//			while(d>=debthSorted.size()) {
//				debthSorted.add(new ArrayList<>());
//			}
//				
//			final ArrayList<NodeExpanded> list = debthSorted.get(d);
//			list.add(node);
//		}
//		
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
//		NodeExpanded currentNode = startNode;
//		final LinkedList<Letter> find = new LinkedList<>(encode);
//		
//		whileLoop:while (find.size()!=0) {
//			final Letter currentLetter = find.getFirst();
//			for (final NodeExpanded n : currentNode.children)
//			{
//				if (n.l.equals(currentLetter)) {
//					find.removeFirst();
//					currentNode = n;
//					continue whileLoop;
//				}
//			}
//			System.out.println("Letter not found. Something wrong");
//		}
//		return currentNode.count;
//	}
//	public NodeExpanded getRootNode()
//	{
//		return startNode;
//	}
//	public Alphabet getAlphabet()
//	{
//		return a;
//	}
//	public void cleanAndTrim()
//	{
//		startNode.cleanAndTrim(globalString);
//	}
//
//	public void printSupport()
//	{
//		final ArrayList<Entry<Integer, Integer>> es = getSortedCountedSupport();
//		
//		for (final Entry<Integer, Integer> entry : es)
//		{
//			System.out.println("Support " + entry.getKey() + " appears " + entry.getValue());
//		}
//	}
//
//	private ArrayList<Entry<Integer, Integer>> getSortedCountedSupport()
//	{
//		final CountMap<Integer> supportsToOccurence = countSupport();
//		final ArrayList<Entry<Integer, Integer>> es = supportsToOccurence.getSortedByKey();
//		return es;
//	}
//
//	private CountMap<Integer> countSupport()
//	{
//		final CountMap<Integer> supToCount = new CountMap<Integer>();
//		final LinkedList<NodeExpanded> liste = new LinkedList<>();
//		liste.push(startNode);
//		while(!liste.isEmpty()) {
//			final NodeExpanded current = liste.removeFirst();
//			final int count = current.count;
//			supToCount.addInstance(Integer.valueOf(count));
//			final TreeSet<NodeExpanded> children = current.children;
//			for (final NodeExpanded child : children)
//			{
//				liste.addFirst(child);
//			}
//		}
//		return supToCount;
//	}
//	
//	
//	
//	@Override
//	public ArrayList<Node> getAllNodes()
//	{
//		final ArrayList<Node> nodes = new ArrayList<>();
//		addAllNodes(startNode,nodes);
//		return nodes;
//	}
//	
//	private void addAllNodes(final NodeExpanded n, final ArrayList<Node> nodes)
//	{
//		nodes.add(n);
//		final TreeSet<NodeExpanded> children = n.children;
//		for (final NodeExpanded nodeExpanded : children)
//		{
//			addAllNodes(nodeExpanded, nodes);
//		}
//		
//	}
//
//}
