//package metrics.suffix_tree.savety;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map.Entry;
//import java.util.TreeMap;
//
//import common.FolderLocations;
//
///**
// * https://www.geeksforgeeks.org/ukkonens-suffix-tree-construction-part-1/
// * @author Markus
// *
// */
//public class SuffixTreeCollapsedSavety implements SuffixTree
//{
//	int globalEndExclusive; //all suffix nodes have this one, which will increase till the end is reached
//	ArrayList<Letter> globalText; //all nodes will point to this global text, so node size is limited to two ints
//	ArrayList<N> leaves = new ArrayList<>(); //not sure if necessary, but at least for testing
//	
//	int count = 0; //could be the clobal end exlclusive?
//	InnerNode lastNewInnerNode = null;
//	
//	N rootNode = new Root();
//	
//	N activeNode = null; //these three variables help to speed up the next phase during insertion
//	Letter activeEdge = null; //not sure, but doesnt make sense if its not the node. maybe the treemap allready
//	N activeChildNode = null;
//	int activeLength = 0; 
//	
//	int leafEnd = -1; 
//	//int rootEnd = null; 
//	//int splitEnd = null; 
//	int size = -1;
//	public int innerNodeCounter = 0;
//	private Alphabet alphabet;
//	
//	public int getSupport(final Letter l) {
//		return 0;
//	}
//	public int getSupport(final List<Letter> l) {
//		return 0;
//	}
//	
//	public static void main(final String[] args) {
//		final SuffixTreeCollapsedSavety stc = new SuffixTreeCollapsedSavety();
//		final HashSet<String> wordList = new HashSet<>();
//		wordList.add("a");
//		wordList.add("b");
//		wordList.add("c");
//		wordList.add("x");
//		wordList.add("d");
//		wordList.add("n");
//		stc.alphabet = new Alphabet(wordList);
//		final ArrayList<Letter> words = stc.alphabet.encode("abcabxabcd".split(""));
//		final ArrayList<Letter> words2 = stc.alphabet.encode("banana".split(""));
//		//words.add(Letter.emptySuffix);
//		//words.addAll(words2);
//		stc.insertIntoTree(words);
//		
//		//stc.printTree();
//		stc.insertIntoTree(words2);
//		stc.countAllOccurences();
//		stc.printTree();
//		stc.printSizes();
//		final SuffixTreeCollapsedSavety stcc = new SuffixTreeCollapsedSavety("xabxa#babxba");
//		stcc.exportGml(FolderLocations.outputTmpFolder, "doubleTrouble", false);
//	}
//	
//	public void countAllOccurences()
//	{
//		rootNode.countAllOccurentes();
//		
//	}
//	public void printSizes()
//	{
//		System.out.println("leaves " + leaves.size() + "  innerNodes: " + innerNodeCounter);
//		
//	}
//	private void printTree()
//	{
//		rootNode.printTree(0);
//		
//	}
//	public SuffixTreeCollapsedSavety() {
//		globalText = new ArrayList<>();
//	}
//	
//	public SuffixTreeCollapsedSavety(final String testWord)
//	{
//		globalText = new ArrayList<>();
//		final HashSet<String> wordList = new HashSet<>();
//		final String[] splitted = testWord.split("");
//		for (final String letter : splitted)
//		{
//			wordList.add(letter);
//		}
//		alphabet = new Alphabet(wordList);
//		final ArrayList<Letter> words = alphabet.encode(splitted);
//		
//		insertIntoTree(words);
//		countAllOccurences();
//		
//	}
//	public void insertAllIntoTree(final ArrayList<ArrayList<Letter>> convertedTrials)
//	{
//		for (int i = 0; i < convertedTrials.size(); i++)
//		{
//			System.out.println(i + " " + convertedTrials.size());
//			final ArrayList<Letter> arrayList = convertedTrials.get(i);
//			insertIntoTree(arrayList);
//		}
//	}
//	public void insertIntoTree(final ArrayList<Letter> newWord) {
//		newWord.add(Letter.emptySuffix);
//		final int currentStart = globalText.size();
//		globalText.ensureCapacity(globalText.size()+newWord.size());
//		globalText.addAll(newWord);
//		final int currentEndExclusive = globalText.size();
//		globalEndExclusive = currentStart;
//		
//		
//		
//		int remainingSuffixCount = 0; 
//		
//		
//		activeNode = rootNode;
//		activeLength = 0;
//		activeEdge = null;
//		Letter currentLetter = null;
//		
//		outer:for (int i = 0; i < newWord.size(); i++)
//		{
//			lastNewInnerNode=null;
//			globalEndExclusive++;
//			remainingSuffixCount++;
//			
//			
//			final int currentCaracter = i+currentStart;
//			currentLetter = globalText.get(currentCaracter);
//			
//			while (remainingSuffixCount>0)
//			{
//				//printTree();
//				if (activeLength==0) {
//					activeEdge = currentLetter; 
//				}
//				activeChildNode = activeNode.containsChild(activeEdge);
//				//if (activeChildNode!=null)
//				if (activeChildNode==null) {
//					//create leaf
//					final N newLeaf = new LeafNode(currentCaracter,currentCaracter);
//					activeNode.addChild(currentLetter,newLeaf);
//					leaves.add(newLeaf);
//					
//					if (lastNewInnerNode != null) {
//						lastNewInnerNode.setSuffixLink(activeNode);
//						lastNewInnerNode = null;
//					}
//				}else {
//					//walkDown the "active edge" if necessary
//					if (walkDown(currentCaracter))
//						continue;
//					
//					final Letter nthL = activeChildNode.getNthLetter(activeLength);
//					if (nthL.equals(currentLetter)) {
//						activeLength++; //either here or after else
//						if (lastNewInnerNode != null) {
//							lastNewInnerNode.setSuffixLink(activeNode);
//							lastNewInnerNode = null;
//						}
//						continue outer;
//					}else {
//						//node has to be split... i wonder if here is the case if length is zero and node has to be created
//						final N newLeaf = new LeafNode(currentCaracter,currentCaracter);
//						final InnerNode newInner = new InnerNode();
//						newInner.start = activeChildNode.getStart();
//						newInner.endExclusive = newInner.start + activeLength;
//						
//						leaves.add(newLeaf);
//						activeNode.replaceChild(activeChildNode,newInner);
//						newInner.addChild(currentLetter, newLeaf);
//						newInner.addChild(activeChildNode.getNthLetter(activeLength), activeChildNode);
//						
//						activeChildNode.setStart(newInner.getEndExclusive());
//						
//						if (lastNewInnerNode != null)lastNewInnerNode.setSuffixLink(newInner);							
//						lastNewInnerNode = newInner;
//						//okay, where the active node now
//						
//					}	
//				}
//				remainingSuffixCount--;
//				if (activeNode.isRoot()&&activeLength>0) {
//					activeLength--;
//					activeEdge = globalText.get(i-remainingSuffixCount+1);
//				}else if (!activeNode.isRoot()) {
//					activeNode = activeNode.getSuffixLink();
//				}	
//			}
//			
//			
//		}
//		
//		
//	}
//	
//	
//
//	private boolean walkDown(final int currentCaracter)
//	{
//		final int pathLength = activeChildNode.getPathLength();
//		if (activeLength >= pathLength) {
//			activeLength -= pathLength;
//			activeEdge = globalText.get(currentCaracter-activeLength);
//			final N candidate = activeChildNode.containsChild(activeEdge);
//			activeNode = activeChildNode;
//			activeChildNode = candidate;
//			if (activeNode==null||activeNode.isLeaf()) {
//				System.out.println("what now");
//			}
//			return true;
//		}
//		return false;
//	}
//
//
//
//	public abstract class N implements Comparable<N>,Node{
//		int occurences = 0; //this will show how many times times a letter occurs;
//		
//		public abstract N getSuffixLink();
//
//		public void countAllOccurentes()
//		{
//			if (isLeaf()) {
//				occurences=1;
//				return;
//			}
//			
//			int sum = 0;
//			for (final N child : this.getChildren().values())
//			{
//				child.countAllOccurentes();
//				sum += child.occurences;
//			}
//			occurences = sum;
//			
//		}
//
//		public void printTree(final int depth)
//		{
//			for (int j = 0; j < depth; j++)
//			{
//				System.out.print("\t");
//			}
//			System.out.println(this.toString());
//			final TreeMap<Letter, N> children = this.getChildren();
//			if (children==null)return;
//			for (final Entry<Letter, N> entry : children.entrySet()) {
//		        final N value = entry.getValue();
//		        value.printTree(depth+1);
//			}
//			
//		}
//
//		protected abstract void setStart(int endExclusive);
//
//		protected abstract void replaceChild(N oldNode, N newNode);
//
//		
//
//		public abstract int getStart();
//
//		protected abstract int getEndExclusive();
//
//		public void addChild(final Letter currentL, final N newNode)
//		{
//			this.getChildren().put(currentL, newNode);
//		}
//		@Override
//		public List<Letter> getFullPathLabel(){
//			return null;
//		}
//		
//
//		public abstract List<Letter> getPathLabel();
//		public abstract int getPathLength();
//		public abstract boolean isLeaf();
//		public abstract boolean isRoot();
//		
//		public abstract Letter getNthLetter(int i);
//		
//		protected abstract TreeMap<Letter, N> getChildren();
//		
//		@Override
//		public int getId() {
//			return 0;
//		}
//		@Override
//		public String getLabel(final boolean asColumn, final boolean representativeString) {
//			final StringBuilder sb = new StringBuilder();
//			final List<Letter> pf = getPathLabel();
//			sb.append("\n");
//			for (final Letter letter : pf)
//			{
//				sb.append(letter.afterDot() + "\n");
//			}
//			sb.append("");
//			return getNumOccurences() + ":" + sb;
//		}
//
//		protected int getNumOccurences() {
//			return occurences;
//		}
//
//		@Override
//		public Node getSuflink() {
//			return this.getSuffixLink();
//		}
//		
//		
//		public N containsChild(final Letter l) {
//			final TreeMap<Letter, N> children = this.getChildren();
//			if (children==null)return null;
//			return children.get(l);
//		}
//		
//		@Override
//		public abstract String toString();
//		
//		
//		@Override
//		public Collection<N> getNodeChildren(){
//			final TreeMap<Letter, N> children = this.getChildren();
//			return children.values();
//			
//		}
//
//		@Override
//		public int compareTo(final N o)
//		{
//			final Letter firstfirst = this.getNthLetter(0);
//			final Letter secondfirst = o.getNthLetter(0);
//			if (firstfirst==null&&secondfirst==null)return 0;
//			if (firstfirst==null)return 1;
//			if (secondfirst==null)return -1;
//			return firstfirst.compare(secondfirst);
//		}
//		
//	}
//	public class Root extends N{
//		TreeMap<Letter, N> children = new TreeMap<Letter, N>(); //the treeset preserves the ordering when implementing. Usefull for comparing two trees.
//		List<Letter> emptyPathLabel = new ArrayList<Letter>(0); //just an empty list;
//		@Override
//		public boolean isLeaf()
//		{
//			return false;
//		}
//		@Override
//		public boolean isRoot()
//		{
//			return true;
//		}
//		@Override
//		public List<Letter> getPathLabel()
//		{
//			return emptyPathLabel;
//		}
//		@Override
//		public int getPathLength()
//		{
//			return 0;
//		}
//		@Override
//		public int compareTo(final N o)
//		{
//			return -1;
//		}
//		@Override
//		public Letter getNthLetter(final int i)
//		{
//			return null;
//		}
//		@Override
//		public N getSuffixLink()
//		{
//			return null;
//		}
//		@Override
//		protected TreeMap<Letter, N> getChildren()
//		{
//			return children;
//		}
//		@Override
//		public String toString()
//		{
//			// TODO Auto-generated method stub
//			return null;
//		}
//		@Override
//		protected int getEndExclusive()
//		{
//			return 0;
//		}
//		@Override
//		public int getStart()
//		{
//			return -1;
//		}
//		@Override
//		protected void replaceChild(final N oldNode, final N newNode)
//		{
//			this.children.put(oldNode.getNthLetter(0), newNode);
//		}
//		@Override
//		protected void setStart(final int endExclusive)
//		{
//			//do nothing
//		}
//		@Override
//		public int getSupport()
//		{
//			// TODO Auto-generated method stub
//			return 0;
//		}
//		
//		
//	}
//	public class InnerNode extends N{
//		TreeMap<Letter, N> children = new TreeMap<Letter, N>(); //the treemap preserves the ordering when implementing. Usefull for comparing two trees.
//		int start;
//		int endExclusive;
//		final int counter = innerNodeCounter++;
//		private N suffixLink = rootNode;
//		@Override
//		public boolean isLeaf()
//		{
//			return false;
//		}
//		public void setSuffixLink(final N newSuffixLink)
//		{
//			this.suffixLink = newSuffixLink;
//		}
//		@Override
//		public boolean isRoot()
//		{
//			return false;
//		}
//		@Override
//		public List<Letter> getPathLabel()
//		{
//			return globalText.subList(start, endExclusive);
//		}
//		@Override
//		public int getPathLength()
//		{
//			return endExclusive-start;
//		}
//		
//		@Override
//		public Letter getNthLetter(final int i)
//		{
//			if (start+i<endExclusive)return globalText.get(start+i);
//			return null;
//		}
//		@Override
//		public N getSuffixLink()
//		{
//			return this.suffixLink;
//		}
//		@Override
//		protected TreeMap<Letter, N> getChildren()
//		{
//			return children;
//		}
//		@Override
//		public String toString()
//		{
//			try
//			{
//				return "inner" + counter +": " + this.getPathLabel() + "  ->  " + suffixLink;
//			} catch (final Exception e)
//			{
//				return "inner" + counter +": " + this.getPathLabel() + "  ->  " + suffixLink;
//			}
//			
//		}
//		@Override
//		protected int getEndExclusive()
//		{
//			return endExclusive;
//		}
//		@Override
//		public int getStart()
//		{
//			return start;
//		}
//		@Override
//		protected void replaceChild(final N oldNode, final N newNode)
//		{
//			this.children.put(oldNode.getNthLetter(0), newNode);
//		}
//		@Override
//		protected void setStart(final int newStart)
//		{
//			this.start = newStart;
//		}
//		@Override
//		public int getSupport()
//		{
//			// TODO Auto-generated method stub
//			return 0;
//		}
//		
//		
//	}
//	public static TreeMap<Letter, N> emptyMap = new TreeMap<>();
//	public class LeafNode extends N{
//		int suffixIndex;
//		int start;
//		
//		public LeafNode(final int suffixIndex, final int start)
//		{
//			this.start = start;
//			this.suffixIndex = suffixIndex;
//		}
//		@Override
//		public boolean isLeaf()
//		{
//			return true;
//		}
//		@Override
//		public boolean isRoot()
//		{
//			return false;
//		}
//		@Override
//		public List<Letter> getPathLabel()
//		{
//			return globalText.subList(start, globalEndExclusive);
//		}
//		@Override
//		public int getPathLength()
//		{
//			return globalEndExclusive-start;
//		}
//	
//		@Override
//		public Letter getNthLetter(final int i)
//		{
//			if (start+i<globalEndExclusive)return globalText.get(start+i);
//			return null;
//		}
//		@Override
//		public N getSuffixLink()
//		{
//			// TODO Auto-generated method stub ???
//			return null;
//		}
//		@Override
//		public N containsChild(final Letter l) {
//			return null;
//		}
//		@Override
//		protected TreeMap<Letter, N> getChildren()
//		{
//			return emptyMap;
//		}
//		@Override
//		public String toString()
//		{
//			return "leaf: " + getPathLabel();
//		}
//		@Override
//		protected int getEndExclusive()
//		{
//			return globalEndExclusive;
//		}
//		@Override
//		public int getStart()
//		{
//			return start;
//		}
//		@Override
//		protected void replaceChild(final N oldNode, final N newNode)
//		{
//			//do nothing
//		}
//		@Override
//		protected void setStart(final int newStart)
//		{
//			this.start = newStart;
//		}
//		@Override
//		public int getSupport()
//		{
//			// TODO Auto-generated method stub
//			return 0;
//		}
//		
//	
//		
//	}
//	@Override
//	public ArrayList<Node> getAllNodes()
//	{
//		final ArrayList<Node> nodes = new ArrayList<>();
//		getAllNodes(rootNode,nodes);
//		
//		
//		return nodes;
//	}
//	private void getAllNodes(final N node, final ArrayList<Node> nodes)
//	{
//		nodes.add(node);
//		final TreeMap<Letter, N> children = node.getChildren();
//		for (final N n : children.values())
//		{
//			getAllNodes(n, nodes);
//		}
//		
//	}
//	@Override
//	public void printAlphabet()
//	{
//		System.out.println(alphabet);
//	}
//	@Override
//	public int getNOccurences(final String string)
//	{
//		
//		return getOccurence(alphabet.encode(string.split("")));
//	}
//	private int getOccurence(final ArrayList<Letter> encode)
//	{
//		N currentNode = rootNode;
//		Iterator<Letter> nodeIterator = currentNode.getPathLabel().iterator();
//		for (final Iterator<Letter> iterator = encode.iterator(); iterator.hasNext();)
//		{
//			final Letter currentLetter = iterator.next();
//			if (!nodeIterator.hasNext()) {
//				final N nextNode = currentNode.getChildren().get(currentLetter);
//				if (nextNode==null)return 0;
//				currentNode = nextNode;
//				nodeIterator = nextNode.getPathLabel().iterator();
//			}
//			if (!currentLetter.equals(nodeIterator.next())) return 0; //firstone is allready checked
//		}
//		return currentNode.occurences;
//	}
//	
//}
