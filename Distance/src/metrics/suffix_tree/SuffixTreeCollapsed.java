package metrics.suffix_tree;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.TreeMap;

import common.FolderLocations;
import game.Game;
import other.trial.Trial;

/**
 * https://www.geeksforgeeks.org/ukkonens-suffix-tree-construction-part-1/
 * @author Markus
 *
 */
public class SuffixTreeCollapsed implements SuffixTree
{
	public DummyNode getDummyNode() {
		return dm;
	}
	public DummyNode dm = new DummyNode();
	int globalEndExclusive; //all suffix nodes have this one, which will increase till the end is reached
	ArrayList<Letter> globalText; //all nodes will point to this global text, so node size is limited to two ints
	ArrayList<N> leaves = new ArrayList<>(); //not sure if necessary, but at least for testing
	TreeMap<Integer,Seperator> seperators = new TreeMap<>();
	int count = 0; //could be the clobal end exlclusive?
	InnerNode lastNewInnerNode = null;
	
	N rootNode = new Root();
	
	N activeNode = null; //these three variables help to speed up the next phase during insertion
	Letter activeEdge = null; //not sure, but doesnt make sense if its not the node. maybe the treemap allready
	N activeChildNode = null;
	int activeLength = 0; 
	
	public int innerNodeCounter = 0;
	private Alphabet alphabet;
	
	public int getSupport(final Letter l) {
		return 0;
	}
	public int getSupport(final List<Letter> l) {
		return 0;
	}
	
	
	
	public void assessAllOccurences()
	{
		rootNode.countAllOccurentes();
		
	}
	public void printSizes()
	{
		System.out.println("leaves " + leaves.size() + "  innerNodes: " + innerNodeCounter);
		
	}
	
	public void printTree()
	{
		rootNode.printTree(0);
		
	}
	public SuffixTreeCollapsed() {
		globalText = new ArrayList<>();
	}
	
	public SuffixTreeCollapsed(final String testWord)
	{
		globalText = new ArrayList<>();
		final HashSet<String> wordList = new HashSet<>();
		final String[] splitted = testWord.split("");
		for (final String letter : splitted)
		{
			wordList.add(letter);
		}
		alphabet = new Alphabet(wordList);
		final ArrayList<Letter> words = alphabet.encode(splitted);
		
		insertIntoTree(words);
		assessAllOccurences();
		
	}
	public SuffixTreeCollapsed(final List<String> splittableWords)
	{
		globalText = new ArrayList<>();
		final HashSet<String> wordList = new HashSet<>();
		final ArrayList<String[]> splitts = new ArrayList<>();
		for (final String toSplitWord : splittableWords)
		{
			final String[] splitted = toSplitWord.split("");
			for (final String letter : splitted)
			{
				wordList.add(letter);
			}
			splitts.add(splitted);
		}
		
		
		alphabet = new Alphabet(wordList);
		for (final String[] splitt : splitts)
		{
			final ArrayList<Letter> words = alphabet.encode(splitt);
			insertIntoTree(words);
		}
		
		assessAllOccurences();
	}
	public SuffixTreeCollapsed(final String name, final Game g, final Trial[] trials, final Letteriser let)
	{
		this(name,let.createTreeBuildingIngredients(g,trials));
		
	}
	public SuffixTreeCollapsed(
			final String name, final TreeBuildingIngredients treeIngred
	)
	{
		alphabet = treeIngred.getAlphabet();
		insertAllIntoTree(treeIngred.getConvertedTrials());
	}
	public void insertAllIntoTree(final ArrayList<ArrayList<Letter>> convertedTrials)
	{
		for (int i = 0; i < convertedTrials.size(); i++)
		{
			System.out.println(i + " " + convertedTrials.size());
			final ArrayList<Letter> arrayList = convertedTrials.get(i);
			insertIntoTree(arrayList);
		}
	}
	public void insertIntoTree(final ArrayList<Letter> newWord) {
		final Seperator seperator = alphabet.getSeperator(seperators.size());
		newWord.add(seperator);
		//newWord.add(Letter.emptySuffix);
		final int currentStart = globalText.size();
		globalText.ensureCapacity(globalText.size()+newWord.size());
		globalText.addAll(newWord);
		seperators.put(Integer.valueOf(globalText.size()-1),seperator);
		globalEndExclusive = currentStart;
		
		int remainingSuffixCount = 0; 
		
		activeNode = rootNode;
		activeLength = 0;
		activeEdge = null;
		lastNewInnerNode = null; //cant be the difference
		Letter currentLetter = null;
		
		outer:for (int i = 0; i < newWord.size(); i++)
		{
			lastNewInnerNode=null;
			globalEndExclusive++;
			remainingSuffixCount++;
			
			
			final int currentCaracter = i+currentStart;
			currentLetter = globalText.get(currentCaracter);
			
			while (remainingSuffixCount>0)
			{
				//printTree();
				if (activeLength==0) {
					activeEdge = currentLetter; 
				}
				activeChildNode = activeNode.containsChild(activeEdge);
				//if (activeChildNode!=null)
				if (activeChildNode==null) {
					//create leaf
					final N newLeaf = new LeafNode(currentCaracter,currentCaracter);
					activeNode.addChild(currentLetter,newLeaf);
					leaves.add(newLeaf);
					
					if (lastNewInnerNode != null) {
						lastNewInnerNode.setSuffixLink(activeNode);
						lastNewInnerNode = null;
					}
				}else {
					//walkDown the "active edge" if necessary
					if (walkDown(currentCaracter))
						continue;
					
					final Letter nthL = activeChildNode.getNthLetter(activeLength);
					if (nthL.equals(currentLetter)) {
						activeLength++; //either here or after else
						if (lastNewInnerNode != null) {
							lastNewInnerNode.setSuffixLink(activeNode);
							lastNewInnerNode = null;
						}
						continue outer;
					}else {
						//node has to be split... i wonder if here is the case if length is zero and node has to be created
						final N newLeaf = new LeafNode(currentCaracter,currentCaracter);
						final InnerNode newInner = new InnerNode();
						newInner.start = activeChildNode.getStart();
						newInner.endExclusive = newInner.start + activeLength;
						
						leaves.add(newLeaf);
						activeNode.replaceChild(activeChildNode,newInner);
						newInner.addChild(currentLetter, newLeaf);
						newInner.addChild(activeChildNode.getNthLetter(activeLength), activeChildNode);
						
						activeChildNode.setStart(newInner.getEndExclusive());
						
						if (lastNewInnerNode != null)lastNewInnerNode.setSuffixLink(newInner);							
						lastNewInnerNode = newInner;
						//okay, where the active node now
						
					}	
				}
				remainingSuffixCount--;
				if (activeNode.isRoot()&&activeLength>0) {
					activeLength--;
					activeEdge = globalText.get(currentCaracter-remainingSuffixCount+1);
				}else if (!activeNode.isRoot()) {
					activeNode = activeNode.getSuffixLink();
				}	
			}
			
			
		}
		
		
	}
	
	

	private boolean walkDown(final int currentCaracter)
	{
		final int pathLength = activeChildNode.getPathLength();
		if (activeLength >= pathLength) {
			activeLength -= pathLength;
			activeEdge = globalText.get(currentCaracter-activeLength);
			final N candidate = activeChildNode.containsChild(activeEdge);
			activeNode = activeChildNode;
			activeChildNode = candidate;
			if (activeNode==null||activeNode.isLeaf()) {
				System.out.println("what now");
			}
			return true;
		}
		return false;
	}

	public class DummyNode extends N
	{
		
		
		@Override
		public boolean isDummyNode() {
			return true;
		}
		
		@Override
		public int getNumOccurences() {
			return 0;
		}


		@Override
		public N getSuffixLink()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected void setStart(final int endExclusive)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void replaceChild(final N oldNode, final N newNode)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public int getStart()
		{
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		protected int getEndExclusive()
		{
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public List<Letter> getPathLabel(final boolean cutAfterSeperator)
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getPathLength()
		{
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean isLeaf()
		{
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isRoot()
		{
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Letter getNthLetter(final int i)
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected TreeMap<Letter, N> getChildren()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String toString()
		{
			// TODO Auto-generated method stub
			return null;
		}
		
	}


	public abstract class N implements Comparable<N>,Node{
		int occurences = 0; //this will show how many times times a letter occurs;
		protected N parent;
		
		public abstract N getSuffixLink();

		@Override
		public List<Letter> getFullPathLabel(){
			return this.getFullPathLabel(new LinkedList<>());
		}
		protected List<Letter> getFullPathLabel(final LinkedList<Letter> linkedList){
			if (isRoot())return linkedList;
			linkedList.addAll(getPathLabel(true));
			return parent.getFullPathLabel(linkedList);
		}
		
		@Override
		public int getSupport() {
			return occurences;
		}
		public void countAllOccurentes()
		{
			if (isLeaf()) {
				occurences=1;
				return;
			}
			
			int sum = 0;
			for (final N child : getChildren().values())
			{
				child.countAllOccurentes();
				child.parent = this;
				sum += child.occurences;
			}
			occurences = sum;
			
		}

		public void printTree(final int depth)
		{
			for (int j = 0; j < depth; j++)
			{
				System.out.print("\t");
			}
			System.out.println(toString());
			final TreeMap<Letter, N> children = getChildren();
			if (children==null)return;
			for (final Entry<Letter, N> entry : children.entrySet()) {
		        final N value = entry.getValue();
		        value.printTree(depth+1);
			}
			
		}

		protected abstract void setStart(int endExclusive);

		protected abstract void replaceChild(N oldNode, N newNode);

		

		public abstract int getStart();

		protected abstract int getEndExclusive();

		public void addChild(final Letter currentL, final N newNode)
		{
			getChildren().put(currentL, newNode);
		}

		@Override
		public abstract List<Letter> getPathLabel(boolean cutAfterSeperator);
		public abstract int getPathLength();
		@Override
		public abstract boolean isLeaf();
		@Override
		public abstract boolean isRoot();
		
		public abstract Letter getNthLetter(int i);
		
		protected abstract TreeMap<Letter, N> getChildren();
		
		@Override
		public int getId() {
			return 0;
		}
		@Override
		public String getLabel(final boolean asColumn, final boolean representativeString) {
			final StringBuilder sb = new StringBuilder();
			final List<Letter> pf = getPathLabel(true);
			if(asColumn)sb.append("\n");
			for (final Letter letter : pf)
			{
				if(asColumn)sb.append(letter.afterDot() + "\n");
				else sb.append(letter.afterDot() + ",");
			}
			sb.append("");
			return getNumOccurences() + ":" + sb;
		}

		public int getNumOccurences() {
			return occurences;
		}

		@Override
		public Node getSuflink() {
			return getSuffixLink();
		}
		
		
		public N containsChild(final Letter l) {
			final TreeMap<Letter, N> children = getChildren();
			if (children==null)return null;
			return children.get(l);
		}
		
		@Override
		public abstract String toString();
		
		
		@Override
		public Collection<N> getNodeChildren(){
			final TreeMap<Letter, N> children = getChildren();
			return children.values();
			
		}

		@Override
		public int compareTo(final N o)
		{
			final Letter firstfirst = getNthLetter(0);
			final Letter secondfirst = o.getNthLetter(0);
			if (firstfirst==null&&secondfirst==null)return 0;
			if (firstfirst==null)return 1;
			if (secondfirst==null)return -1;
			return firstfirst.compare(secondfirst);
		}

		public boolean isDummyNode()
		{
			return false;
		}

		public boolean isSeperator()
		{
			return false;
		}

		public boolean belongsTo(final Seperator seperator)
		{
			return false;
		}
		
	}
	public class Root extends N{
		TreeMap<Letter, N> children = new TreeMap<Letter, N>(); //the treeset preserves the ordering when implementing. Usefull for comparing two trees.
		List<Letter> emptyPathLabel = new ArrayList<Letter>(0); //just an empty list;
		@Override
		public boolean isLeaf()
		{
			return false;
		}
		@Override
		public boolean isRoot()
		{
			return true;
		}
		@Override
		public List<Letter> getPathLabel(final boolean cutAfterSeperator)
		{
			return emptyPathLabel;
		}
		@Override
		public int getPathLength()
		{
			return 0;
		}
		@Override
		public int compareTo(final N o)
		{
			return -1;
		}
		@Override
		public Letter getNthLetter(final int i)
		{
			return null;
		}
		@Override
		public N getSuffixLink()
		{
			return null;
		}
		@Override
		protected TreeMap<Letter, N> getChildren()
		{
			return children;
		}
		@Override
		public String toString()
		{
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		protected int getEndExclusive()
		{
			return 0;
		}
		@Override
		public int getStart()
		{
			return -1;
		}
		@Override
		protected void replaceChild(final N oldNode, final N newNode)
		{
			children.put(oldNode.getNthLetter(0), newNode);
		}
		@Override
		protected void setStart(final int endExclusive)
		{
			//do nothing
		}
		
		
	}
	public class InnerNode extends N{
		TreeMap<Letter, N> children = new TreeMap<Letter, N>(); //the treemap preserves the ordering when implementing. Usefull for comparing two trees.
		int start;
		int endExclusive;
		final int counter = innerNodeCounter++;
		private N suffixLink = rootNode;
		
		@Override
		public boolean isLeaf()
		{
			return false;
		}
		public void setSuffixLink(final N newSuffixLink)
		{
			suffixLink = newSuffixLink;
		}
		@Override
		public boolean isRoot()
		{
			return false;
		}
		@Override
		public List<Letter> getPathLabel(final boolean cutAfterSeperator)
		{
			
			final Integer positionOfNextSeperator = seperators.ceilingEntry(Integer.valueOf(start)).getKey();
			final int min = Math.min(positionOfNextSeperator.intValue()+1, endExclusive);
			return globalText.subList(start, min);
		}
		@Override
		public int getPathLength()
		{
			return endExclusive-start;
		}
		
		@Override
		public Letter getNthLetter(final int i)
		{
			if (start+i<endExclusive)return globalText.get(start+i);
			return null;
		}
		@Override
		public N getSuffixLink()
		{
			return suffixLink;
		}
		@Override
		protected TreeMap<Letter, N> getChildren()
		{
			return children;
		}
		@Override
		public String toString()
		{
			try
			{
				return "inner" + counter +": " + getPathLabel(true) + "  ->  " + suffixLink;
			} catch (final Exception e)
			{
				return "inner" + counter +": " + getPathLabel(true) + "  ->  " + suffixLink;
			}
			
		}
		@Override
		protected int getEndExclusive()
		{
			return endExclusive;
		}
		@Override
		public int getStart()
		{
			return start;
		}
		@Override
		protected void replaceChild(final N oldNode, final N newNode)
		{
			children.put(oldNode.getNthLetter(0), newNode);
		}
		@Override
		protected void setStart(final int newStart)
		{
			start = newStart;
		}
		
		
	}
	public static TreeMap<Letter, N> emptyMap = new TreeMap<>();
	public class LeafNode extends N{
		int suffixIndex;
		int start;
		
		public LeafNode(final int suffixIndex, final int start)
		{
			this.start = start;
			this.suffixIndex = suffixIndex;
		}
		@Override
		public boolean isLeaf()
		{
			return true;
		}
		@Override
		public boolean isRoot()
		{
			return false;
		}
		@Override
		public List<Letter> getPathLabel(final boolean cutAfterSeperator)
		{
			if (!cutAfterSeperator)return globalText.subList(start, globalEndExclusive);
			final Integer positionOfNextSeperator = seperators.ceilingEntry(Integer.valueOf(start)).getKey();
			final int min = Math.min(positionOfNextSeperator.intValue()+1, globalEndExclusive);
			return globalText.subList(start, min);
			
		}
		@Override
		public int getPathLength()
		{
			return globalEndExclusive-start;
		}
	
		@Override
		public Letter getNthLetter(final int i)
		{
			if (start+i<globalEndExclusive)return globalText.get(start+i);
			return null;
		}
		@Override
		public N getSuffixLink()
		{
			// TODO Auto-generated method stub ???
			return null;
		}
		@Override
		public N containsChild(final Letter l) {
			return null;
		}
		@Override
		protected TreeMap<Letter, N> getChildren()
		{
			return emptyMap;
		}
		@Override
		public String toString()
		{
			return "leaf: " + getPathLabel(true);
		}
		@Override
		protected int getEndExclusive()
		{
			return globalEndExclusive;
		}
		@Override
		public int getStart()
		{
			return start;
		}
		@Override
		protected void replaceChild(final N oldNode, final N newNode)
		{
			//do nothing
		}
		@Override
		protected void setStart(final int newStart)
		{
			start = newStart;
		}
		
		public Seperator getSeperator() {
			final Integer positionOfNextSeperator = seperators.ceilingEntry(Integer.valueOf(start)).getKey();
			return (Seperator) globalText.get(positionOfNextSeperator.intValue());
		}
		
		@Override
		public boolean belongsTo(final Seperator seperator)
		{
			final Integer positionOfNextSeperator = seperators.ceilingEntry(Integer.valueOf(start)).getKey();
			return seperator.equals(globalText.get(positionOfNextSeperator.intValue()));
		}
		
	}
	@Override
	public ArrayList<Node> getAllNodes()
	{
		final ArrayList<Node> nodes = new ArrayList<>();
		getAllNodes(rootNode,nodes);
		
		
		return nodes;
	}
	private void getAllNodes(final N node, final ArrayList<Node> nodes)
	{
		nodes.add(node);
		final TreeMap<Letter, N> children = node.getChildren();
		for (final N n : children.values())
		{
			getAllNodes(n, nodes);
		}
		
	}
	@Override
	public void printAlphabet()
	{
		System.out.println(alphabet);
	}
	@Override
	public int getNumOccurences(final String string)
	{
		
		return getNOccurence(alphabet.encode(string.split("")));
	}
	private int getNOccurence(final ArrayList<Letter> encode)
	{
		N currentNode = rootNode;
		Iterator<Letter> nodeIterator = currentNode.getPathLabel(true).iterator();
		for (final Iterator<Letter> iterator = encode.iterator(); iterator.hasNext();)
		{
			final Letter currentLetter = iterator.next();
			if (!nodeIterator.hasNext()) {
				final N nextNode = currentNode.getChildren().get(currentLetter);
				if (nextNode==null)return 0;
				currentNode = nextNode;
				nodeIterator = nextNode.getPathLabel(true).iterator();
			}
			if (!currentLetter.equals(nodeIterator.next())) return 0; //firstone is allready checked
		}
		return currentNode.occurences;
	}
	public void setAlphabet(final Alphabet alphabet)
	{
		this.alphabet = alphabet;
		
	}
	public void printFullWord()
	{
		System.out.println(globalText);
	}
	public void printSupport()
	{
		final ArrayList<Node> allNodes = getAllNodes();
		allNodes.sort(new Comparator<Node>()
		{

			@Override
			public int compare(final Node o1, final Node o2)
			{
				return -Integer.compare(o1.getSupport(),o2.getSupport());
			}
		});
		
		final File fout = new File(FolderLocations.outputTmpFolder.getAbsolutePath() + "/support.txt");
		try (FileOutputStream fos = new FileOutputStream(fout);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			)
		{
			for (int i = 0; i < allNodes.size()&& i < 1000; i++)
			{
				final Node node = allNodes.get(i);
				bw.write(node.getSupport() + ": " + node.getFullPathLabel().toString());
				bw.newLine();
			}
		 
			bw.close();
			
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
		
		
	}
	public Alphabet getAlphabet()
	{
		return alphabet;
	}
	public N getRootNode()
	{
		return rootNode;
	}
	public ArrayList<QuerryResult> findNode(
			final List<Letter> code, final int numberOfMissmatches, final boolean ignoreQuotationMarks,
			final ArrayList<Seperator> seperatorsToIgnor
	)
	{
		//breadth first bottom up probably faster, but more complicated. 
		//depth first from root is performed and it has to be searched anyways
		
		//final HashSet<N> nodesToIgnore = new HashSet<>();
		//if (seperatorsToIgnor!=null && seperatorsToIgnor.size()>0)
		//	depthFirstIgnoreSearch(this.rootNode,seperatorsToIgnor,nodesToIgnore);
		//depthFirstIgnoreSearch(this.rootNode,seperatorsToIgnor,nodesToIgnore);
		
		final ArrayList<QuerryResult> results = new ArrayList<>();
		final LinkedList<Letter> codeList = new LinkedList<>(code);
		depthFirstQuerryIgnoreSearch(rootNode,numberOfMissmatches,codeList,results,seperatorsToIgnor,0,ignoreQuotationMarks);
		//if there would be more querry results, three/four variable to sort 
		
		return results;
	}
	private boolean depthFirstQuerryIgnoreSearch(
			final N current, final int numberOfMissmatchesSrc, final List<Letter> code, final ArrayList<QuerryResult> results,
			final ArrayList<Seperator> seperatorsToIgnor, final int depthSrc, final boolean ignoreQuotationMarks
	)
	{
		if (numberOfMissmatchesSrc<0)return false;
		int numberOfMissmatches = numberOfMissmatchesSrc;
		int depth = depthSrc;
		if (current.isLeaf()) {
			if (seperatorsToIgnor.contains(((LeafNode)current).getSeperator())) {
				return false;
			}		
		}
		int counter = 0;
		final Iterator<Letter> nodeIterator = current.getPathLabel(true).iterator();
		final Iterator<Letter> searchIterator = code.iterator();
		for (; searchIterator.hasNext()&&nodeIterator.hasNext()&&numberOfMissmatches>=0;)
		{
			final Letter currentNodeLetter = nodeIterator.next();
			final Letter currentSearchLetter = searchIterator.next();
			depth= depth+1;
			counter++;
			if (!currentSearchLetter.equals(currentNodeLetter,ignoreQuotationMarks)) {
				numberOfMissmatches=numberOfMissmatches-1;
				counter--;
			}
		}
		if (numberOfMissmatches<0||!searchIterator.hasNext()) {
			System.out.println("search stopped, return what you got");
			final List<LeafNode> leavesUnderneath = getLeavesUnderneath(current);
			if (leavesUnderneath.size()==0)return false;
			final QuerryResult qr = new QuerryResult(current,leavesUnderneath,depth);
			results.add(qr);
			return true;
		}else {
			final Letter nextNodeFirstLetter = searchIterator.next();
			final TreeMap<Letter, N> children = current.getChildren();
			final List<Letter> cutCode = code.subList(counter, code.size());
			
			if (numberOfMissmatches>0) {
				for (final N child : children.values())
				{
					depthFirstQuerryIgnoreSearch(child, numberOfMissmatches, cutCode, results, seperatorsToIgnor, depth,ignoreQuotationMarks);
				}
			}else {
				final N fittingChild = children.get(nextNodeFirstLetter);
				if (fittingChild==null) {
					System.out.println("search stopped, return what you got");
					//count all leaves underneath
					final List<LeafNode> leavesUnderneath = getLeavesUnderneath(current);
					if (leavesUnderneath.size()==0)return false;
					final QuerryResult qr = new QuerryResult(current,leavesUnderneath,depth);
					results.add(qr);
					return true;
				}
				if (depthFirstQuerryIgnoreSearch(fittingChild, numberOfMissmatches, cutCode, results, seperatorsToIgnor, depth,ignoreQuotationMarks)) {
					return true;
				}else {
					final List<LeafNode> leavesUnderneath = getLeavesUnderneath(current);
					if (leavesUnderneath.size()==0)return false;
					final QuerryResult qr = new QuerryResult(current,leavesUnderneath,depth);
					results.add(qr);
					return true;
				}
			}
			
			
		}
		return true;
		
		
	}
	private static List<LeafNode> getLeavesUnderneath(final N current)
	{
		final ArrayList<LeafNode> list = new ArrayList<>();
		
		final Stack<N> stack = new Stack<>();
		stack.push(current);
		while (!stack.isEmpty())
		{
			final N popped = stack.pop();
			if (popped.isLeaf())
				list.add((LeafNode)popped);
			else
				stack.addAll(popped.getNodeChildren());
			
		}
		
		return list;
	}
	
	
	
	@SuppressWarnings("unused") //doesnt seem to be used, but wanna keep it for now. jan 2021
	private boolean depthFirstIgnoreSearch(
			final N current, final ArrayList<Seperator> seperatorsToIgnor,
			final HashSet<N> nodesToIgnore
	)
	{
		if (current.isLeaf()) {
			if (seperatorsToIgnor.contains(((LeafNode)current).getSeperator())) {
				nodesToIgnore.add(current);
				return true;
			}
			return false;
		}
		
		int ignoreCount = 0;
		for (final N child : current.getChildren().values())
		{
			if (depthFirstIgnoreSearch(child, seperatorsToIgnor, nodesToIgnore)) {
				ignoreCount++;
			}
		}
		if (ignoreCount == current.getChildren().size()) {
			nodesToIgnore.add(current);
			return true;
		}
		return false;
		
	}
	
}
