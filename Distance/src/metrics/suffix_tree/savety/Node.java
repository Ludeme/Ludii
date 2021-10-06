//package metrics.suffix_tree.savety;
//
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.LinkedList;
//import java.util.TreeSet;
//import java.util.function.Predicate;
//
//public class Node implements Serializable
//{
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = 1L;
//	Node parent;
//	Node suflink;
//	
//	public int letterStartIndex;
//	public int letterEndIndex;
//	int count = 0;
//	transient TreeSet<Node> children = new TreeSet<>(comparator);
//	private final int id = idCounter++;
//	protected Letter l;
//	public int toPushDown;
//	
//	
//	
//	public static int pushdownCounter = 0;
//	private static int idCounter = 0;
//	public static Comparator<Node> comparator = createComperator();
//	
//	public Node(
//			final Letter letter, final int letterStartIndex, final int letterEndIndex, final Node parent
//	)
//	{
//		count=0;
//		l = letter;
//		this.letterStartIndex = letterStartIndex;
//		this.letterEndIndex = letterEndIndex;
//		this.parent = parent;
//	}
//
//
//	@Override
//	public String toString() {
//		return l + " cs: " + children.size() + "linked: " + suflink;
//	}
//	
//
//	private static Comparator<Node> createComperator()
//	{
//		return new Comparator<Node>()
//		{
//
//			@Override
//			public int compare(final Node o1, final Node o2)
//			{
//				return o1.l.compare(o2.l);
//			}
//		};
//	}
//
//	
//	public Node insertAndReturnMainInsertionPoint(final LinkedList<Letter> toInsert, final LinkedList<Integer> toInsertInt)
//	{		
//		if (toInsert.size() == 0) {
//			//this.count++;
//			this.traverseSuffixLinks();
//			return this.parent;
//		}
//		if (children.size() == 0)
//		{
//			if (toInsert.size() == 1)
//			{
//				addLeaf(toInsert.getFirst(),toInsertInt.getFirst());
//				return this;
//			} else
//			{
//				System.out.println(
//						"something off as this tree is build with increasing suffixes");
//			}
//
//		} else
//		{
//			final Letter affix = toInsert.removeFirst();
//			final Integer affixLetterIndex = toInsertInt.removeFirst();
//			final LinkedList<Letter> suffix = toInsert; 
//			
//			for (final Node child : children)
//			{
//				if (child.l.equals(affix))
//				{
//					return child.insertAndReturnMainInsertionPoint(suffix,toInsertInt);
//
//				}
//			}
//			//inner node needs to be added
//			final Node newNode = this.addLeaf(affix,affixLetterIndex);
//			
//			return newNode.insertAndReturnMainInsertionPoint(suffix,toInsertInt);
//		}
//		return this;
//	}
//	
//
//	private LinkedList<Letter> getRepresentativeWord(){
//		return getRepresentativeWord(new LinkedList<>());
//	}
//	private LinkedList<Letter> getRepresentativeWord(final LinkedList<Letter> list)
//	{
//		if (l==null)return list;
//		list.addFirst(l);
//		return parent.getRepresentativeWord(list);
//	}
//
//	private Node addLeaf(final Letter letter, final int letterIndex)
//	{
//		final Node n = new Node(letter,letterIndex,letterIndex,this);
//		
//		this.children.add(n);
//		n.traverseSuffixLinks();
//		return n;
//	}
//
//	private void traverseSuffixLinks()
//	{
//		final Node parentSufLink = parent.suflink;
//		if (parentSufLink == null)
//		{
//			this.suflink = parent;
//			return;
//		}
//		
//		for (final Node n : parentSufLink.children)
//		{
//			if (n.l.equals(l)) {
//				this.suflink = n;
//				return;
//			}
//				
//				
//		}
//		final Node newN = new Node(l,letterStartIndex,letterEndIndex,parent.suflink);
//		
//		parentSufLink.children.add(newN);
//		this.suflink = newN;
//		newN.traverseSuffixLinks();
//
//	}
//
//	public void printTree(final int depth)
//	{
//		for (int i = 0; i < depth; i++)
//			System.out.print(" ");
//		System.out.println(l + ": " + count);
//		for (final Node child : children)
//		{
//			child.printTree(depth + 1);
//		}
//	}
//
//
//	public boolean isLeaf()
//	{
//		
//		return this.children.size()==0;
//	}
//
//
//	public int getId()
//	{
//		
//		return id;
//	}
//
//
//	public int getDepth()
//	{
//		if (this.parent==null) return 0;
//		return 1+this.parent.getDepth();
//	}
//
//
//	public boolean isDummyNode()
//	{
//		return false;
//	}
//
//
//	public int getCount()
//	{
//		return count;
//	}
//
//
//	public int countNodes()
//	{
//		int sizeCounter = 1;
//		if (isLeaf())return sizeCounter;
//		
//		for (final Node node : children)
//		{
//			sizeCounter+=node.countNodes();
//		}
//		return sizeCounter;
//	}
//
//
//	public void cleanAndTrim(final ArrayList<Letter> globalString)
//	{
//		
//		children.removeIf(new Predicate<Node>()
//		{
//
//			@Override
//			public boolean test(final Node t)
//			{
//				return t.getLetter(globalString).equals(Letter.emptySuffix);
//			}
//		});
//		for (final Node node : children)
//		{
//			node.cleanAndTrim(globalString);
//		}
//	}
//
//
//	protected Letter getLetter(final ArrayList<Letter> globalString)
//	{
//		if (letterStartIndex==-1)return Letter.emptySuffix;
//		return globalString.get(letterStartIndex);
//	}
//
//
//	public void pushDown(final int above,  final int letterIndexFromTop)
//	{
//		pushdownCounter += 1;
//		
//		if (this.isRoot()) {
//			this.count += above; 
//		}else {
//			System.out.println("liPush: " + this.letterStartIndex);
//			
//			this.count += above;
//			
//			toPushDown = 0;
//			if (this.letterStartIndex==letterIndexFromTop) {
//				this.suflink.pushDown(count,letterIndexFromTop);
//			}
//				
//			
//		}
//		
//	}
//	
//	public int compare(final Node n2) {
//		return this.l.compare(n2.l);
//	}
//
//
//	public boolean isRoot()
//	{
//		return this.parent==null;
//	}
//
//
//	public String getLabel()
//	{
//		return ""+l + ":" + letterStartIndex + ":" + count; //"State"+
//		
//	}
//
//}
