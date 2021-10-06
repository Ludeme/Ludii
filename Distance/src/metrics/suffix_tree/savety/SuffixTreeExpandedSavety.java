//package metrics.suffix_tree.savety;
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
//public class SuffixTreeExpandedSavety implements Serializable
//{
//	
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = 1L;
//	private final NodeSaveTy5 startNodeSaveTy5;
//	private final Alphabet a;
//	private final String name;
//	//private ArrayList<NodeSaveTy5> allNodeSaveTy5s;
//
//	public SuffixTreeExpandedSavety(final String name, final String[] concatWords)
//	{
//		this.name = name;
//		final HashSet<String> wordList = new HashSet<>(Arrays.asList(concatWords));
//		this.a = new Alphabet(wordList);
//		System.out.println(a);
//		startNodeSaveTy5 = new NodeSaveTy5(); 
//		buildTree(new String[][]{concatWords});
//		
//	}
//	public SuffixTreeExpandedSavety(final String name, final String[][] concatWords)
//	{
//		this.name = name;
//		final HashSet<String> wordList = new HashSet<>();
//		for (final String[] string : concatWords)
//		{
//			wordList.addAll(Arrays.asList(string));
//		}
//		this.a = new Alphabet(wordList);
//		System.out.println(a);
//		startNodeSaveTy5 = new NodeSaveTy5();
//		buildTree(concatWords);
//		
//	}
//
//	
//	public SuffixTreeExpandedSavety(final String name, final TreeBuildingIngredients ingredients)
//	{
//		final long t3 = System.currentTimeMillis();
//		this.a = ingredients.getAlphabet();
//		this.name = name;
//		this.startNodeSaveTy5 = new NodeSaveTy5();
//		buildTree(ingredients.getConvertedTrials());
//		final long t4 = System.currentTimeMillis();
//		final long dt = t4-t3;
//		final double nNodeSaveTy5s = startNodeSaveTy5.countNodes();
//		final double nLetters = ingredients.getNumLetters();
//		System.out.println("building took " + dt + " for " + name);
//		System.out.println("NodeSaveTy5s To Time: " + nNodeSaveTy5s/dt + " with NodeSaveTy5s " + (int)nNodeSaveTy5s);
//		System.out.println("letters To Time: " + nLetters/dt + " with letters " + (int)nLetters);
//	}
//	
//	public SuffixTreeExpandedSavety(final String name, final Trial[] trials, final Letteriser let)
//	{
//		this.name = name;
//		
//		final long t3 = System.currentTimeMillis();
//		
//		
//		final TreeBuildingIngredients ingredients = let.createTreeBuildingIngredients(trials);
//		this.a = ingredients.getAlphabet();
//		startNodeSaveTy5 = new NodeSaveTy5();
//		
//		buildTree(ingredients.getConvertedTrials());
//		
//		//allNodeSaveTy5s = getAllNodeSaveTy5sDepthFirst();
//		final long t2 = System.currentTimeMillis();
//		final long dt2 = t3-t2;
//		System.out.println("Tree size: " + startNodeSaveTy5.countNodes()+ " building took: " + dt2 + " counting took: " + dt2 + "ms");
//	}
//
//	private void buildTree(final ArrayList<ArrayList<Letter>> convertedTrials)
//	{
//		insertConverted(convertedTrials);
//		cleanAndTrim();	
//		printNodeSaveTy5Counts(convertedTrials);
//	}
//
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
//		for (final ArrayList<Letter> convTrial : convertedTrials)
//		{
//			algorithmRemeberLastLeaf(convTrial);	
//		}
//	}
//
//
//	private ArrayList<NodeSaveTy5> getAllNodesDepthFirst()
//	{
//		final ArrayList<NodeSaveTy5> allNodeSaveTy5sLocal = getAllNodeSaveTy5sDepthFirst(startNodeSaveTy5,new ArrayList<NodeSaveTy5>());
//		allNodeSaveTy5sLocal.trimToSize();
//		return allNodeSaveTy5sLocal;
//	}
//
//
//	private ArrayList<NodeSaveTy5> getAllNodeSaveTy5sDepthFirst(final NodeSaveTy5 currentNodeSaveTy5, final ArrayList<NodeSaveTy5> NodeSaveTy5s)
//	{
//		if (currentNodeSaveTy5.isLeaf())return NodeSaveTy5s;
//		for (final NodeSaveTy5 NodeSaveTy5 : currentNodeSaveTy5.children)
//		{
//			NodeSaveTy5s.add(NodeSaveTy5);
//			getAllNodeSaveTy5sDepthFirst(NodeSaveTy5, NodeSaveTy5s);
//		}
//		return NodeSaveTy5s;
//	}
//
//
//	public String getName()
//	{
//		return name;
//	}
//	
//	
//	private void insert(final ArrayList<Letter> converted)
//	{ 
//		algorithmRemeberLastLeaf(converted);		
//	}
//
//	public SuffixTreeExpandedSavety(final String testWord)
//	{
//		this(null, testWord.split(""));
//	}
//	public SuffixTreeExpandedSavety(final List<String> testWords)
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
//	public SuffixTreeExpandedSavety(final LudRul c, final int num_playouts, final int num_maxMoves, final Letteriser letteriser)
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
//	private void algorithmRemeberLastLeaf(final ArrayList<Letter> convert)
//	{
//		
//		//System.out.println("fullList: " + convert);
//		NodeSaveTy5 lastInsertionPoint = startNodeSaveTy5;
//		final LinkedList<Letter> toInsert = new LinkedList<>();
//		toInsert.add(Letter.emptySuffix);
//		lastInsertionPoint = lastInsertionPoint.insertAndReturnMainInsertionPoint(toInsert);
//		for (int i = 0; i < convert.size(); i++)
//		{
//			toInsert.clear();
//			toInsert.add(convert.get(i));
//			toInsert.add(Letter.emptySuffix);
//			lastInsertionPoint = lastInsertionPoint.insertAndReturnMainInsertionPoint(toInsert);	
//			//startNodeSaveTy5.printTree(0);
//			//System.out.println("");
//		}
//		
//		final HashMap<Letter,Boolean> checked = new HashMap<>(); //hashmap as NodeSaveTy5s are unique and faster
//		NodeSaveTy5 pushDownNodeSaveTy5 = lastInsertionPoint; //ignore the empty suffix
//		
//		//traverseMainString
//		
//		while (pushDownNodeSaveTy5!=null) {
//			pushDownNodeSaveTy5.pushdown=1;
//			pushDownNodeSaveTy5 = pushDownNodeSaveTy5.parent;
//		}
//		pushDownNodeSaveTy5 = lastInsertionPoint; //ignore the empty suffix
//		while (pushDownNodeSaveTy5.l!=null) {
//			if (pushDownNodeSaveTy5.pushdown!=0) {
//				pushDownNodeSaveTy5.pushDown(0);
//				
//			}
//			
//			pushDownNodeSaveTy5 = pushDownNodeSaveTy5.parent;	
//		}
//		System.out.println("pushdowncounter" + NodeSaveTy5.pushdownCounter);
//		//check if something is left to pushdown
//		final int nNodeSaveTy5s = 1;
//		final ArrayList<NodeSaveTy5> NodeSaveTy5s = new ArrayList<>();
//		NodeSaveTy5s.add(startNodeSaveTy5);
//		NodeSaveTy5.pushdownCounter = 0;
//		
//		printNodeSaveTy5Counts(convert.size());
//		
//		/*while(!NodeSaveTy5s.isEmpty()) {
//			final NodeSaveTy5 n = NodeSaveTy5s.remove(NodeSaveTy5s.size()-1);
//			nNodeSaveTy5s++;
//			if (n.pushdown!= 0) {
//				n.pushdown=0;
//			}
//			NodeSaveTy5s.addAll(n.children);
//		}
//		System.out.println("nNodeSaveTy5s " + nNodeSaveTy5s);*/
//		
//		final Runtime runtime = Runtime.getRuntime();
//		final long memory = runtime.totalMemory() - runtime.freeMemory();
//        System.out.println("Used memory is bytes: " + memory);
//        System.out.println("Used memory is megabytes: "
//                + (memory)/1024/1024);
//		
//		//System.out.println("Tree size: " + startNodeSaveTy5.countNodeSaveTy5s());
//		//startNodeSaveTy5.printTree(0);	
//	}
//	
//
//	private void printNodeSaveTy5Counts(final ArrayList<ArrayList<Letter>> convertedTrials)
//	{
//		int total = 0;
//		for (final ArrayList<Letter> arrayList : convertedTrials)
//		{
//			total+=arrayList.size();
//		}
//		printNodeSaveTy5Counts(total);
//		
//	}
//	
//	private void printNodeSaveTy5Counts(final int insertedStringLenth)
//	{
//		
//		int total = 0;
//		int leaf = 0;
//		int internal = 0;
//		
//		final LinkedList<NodeSaveTy5> holdup = new LinkedList<NodeSaveTy5>();
//		
//		holdup.add(startNodeSaveTy5);
//
//		while (holdup.size()!=0)
//		{
//			final NodeSaveTy5 current = holdup.removeFirst();
//			if (current.children.size()==0)
//				leaf++;
//			else
//				internal++;
//			total++;
//			
//			for (final NodeSaveTy5 child : current.children)
//			{
//				holdup.addFirst(child);
//			}
//		}
//		
//		System.out.println(insertedStringLenth + " input lentgth");
//		System.out.println("Optimal would be " + (insertedStringLenth+1) + " leaves and " + insertedStringLenth + " internal NodeSaveTy5s");
//		System.out.println("Found " + leaf + " leaves and " + internal + " internal NodeSaveTy5s and total " + total + " NodeSaveTy5s");
//		
//		
//	}
//	public ArrayList<ArrayList<NodeSaveTy5>> getSortedNodeSaveTy5sByDepth()
//	{
//		final ArrayList<ArrayList<NodeSaveTy5>> debthSorted = new ArrayList<ArrayList<NodeSaveTy5>>();
//		final ArrayList<NodeSaveTy5> NodeSaveTy5s = new ArrayList<>();
//		final HashMap<NodeSaveTy5, Boolean> checked = new HashMap<>();
//		final LinkedList<NodeSaveTy5> holdup = new LinkedList<NodeSaveTy5>();
//		
//		holdup.add(startNodeSaveTy5);
//		checked.put(startNodeSaveTy5,Boolean.valueOf(true));
//		while (holdup.size()!=0)
//		{
//			final NodeSaveTy5 first = holdup.removeFirst();
//			System.out.println(first.getId() + " " +first.getDepth());
//			NodeSaveTy5s.add(first);
//			for (final NodeSaveTy5 firstChild : first.children)
//			{
//				final Boolean oldVal = checked.put(firstChild, Boolean.valueOf(true));
//				if (oldVal!=null)continue;
//				holdup.add(firstChild);
//			}
//		}
//		for (final NodeSaveTy5 NodeSaveTy5 : NodeSaveTy5s)
//		{
//			final int d = NodeSaveTy5.getDepth();
//			while(d>=debthSorted.size()) {
//				debthSorted.add(new ArrayList<>());
//			}
//				
//			final ArrayList<NodeSaveTy5> list = debthSorted.get(d);
//			list.add(NodeSaveTy5);
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
//		NodeSaveTy5 currentNodeSaveTy5 = startNodeSaveTy5;
//		final LinkedList<Letter> find = new LinkedList<>(encode);
//		
//		whileLoop:while (find.size()!=0) {
//			final Letter currentLetter = find.getFirst();
//			for (final NodeSaveTy5 n : currentNodeSaveTy5.children)
//			{
//				if (n.l.equals(currentLetter)) {
//					find.removeFirst();
//					currentNodeSaveTy5 = n;
//					continue whileLoop;
//				}
//			}
//			System.out.println("Letter not found. Something wrong");
//		}
//		return currentNodeSaveTy5.count;
//	}
//	public NodeSaveTy5 getRootNodeSaveTy5()
//	{
//		return startNodeSaveTy5;
//	}
//	public Alphabet getAlphabet()
//	{
//		return a;
//	}
//	public void cleanAndTrim()
//	{
//		startNodeSaveTy5.cleanAndTrim();
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
//		final LinkedList<NodeSaveTy5> liste = new LinkedList<>();
//		liste.push(startNodeSaveTy5);
//		while(!liste.isEmpty()) {
//			final NodeSaveTy5 current = liste.removeFirst();
//			final int count = current.count;
//			supToCount.addInstance(Integer.valueOf(count));
//			final TreeSet<NodeSaveTy5> children = current.children;
//			for (final NodeSaveTy5 child : children)
//			{
//				liste.addFirst(child);
//			}
//		}
//		return supToCount;
//	}
//	public void exportGml(final File folder, final String fileName, final boolean showSuffixLinks)
//	{
//		final GraphComposer gc = new GraphComposer();
//		//gc.compose(folder, fileName, this, showSuffixLinks);
//		
//	}
//	
//	public SuffixTreeCollapsed getCollapsedSuffixTree() {
//		
//		return null;
//		
//	}
//
//}
