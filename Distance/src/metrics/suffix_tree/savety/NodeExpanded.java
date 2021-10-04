package metrics.suffix_tree.savety;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Predicate;

import metrics.suffix_tree.Letter;
import metrics.suffix_tree.Node;

public class NodeExpanded implements Serializable,Node
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	NodeExpanded parent;
	NodeExpanded suflink;
	
	public int letterStartIndex;
	public int letterEndIndex;
	int count = 0;
	transient TreeSet<NodeExpanded> children = new TreeSet<>(comparator);
	private final int id = idCounter++;
	public Letter l;
	public int toPushDown;
	private int alternaCount;
	
	
	
	public static int pushdownCounter = 0;
	public static int pullDownCounter = 0;
	private static int idCounter = 0;
	public static Comparator<NodeExpanded> comparator = createComperator();
	
	public NodeExpanded(
			final Letter letter, final int letterStartIndex, final int letterEndIndex, final NodeExpanded parent
	)
	{
		count=0;
		l = letter;
		this.letterStartIndex = letterStartIndex;
		this.letterEndIndex = letterEndIndex;
		this.parent = parent;
	}


	@Override
	public String toString() {
		return l + " cs: " + children.size() + "linked: " + suflink;
	}
	

	private static Comparator<NodeExpanded> createComperator()
	{
		return new Comparator<NodeExpanded>()
		{

			@Override
			public int compare(final NodeExpanded o1, final NodeExpanded o2)
			{
				return o1.l.compare(o2.l);
			}
		};
	}

	
	public NodeExpanded insertAndReturnMainInsertionPoint(final LinkedList<Letter> toInsert, final LinkedList<Integer> toInsertInt)
	{		
		if (toInsert.size() == 0) {
			//this.alternaCount++;
			//this.traverseSuffixLinks();
			return this.parent;
		}
		if (children.size() == 0)
		{
			if (toInsert.size() == 1)
			{
				addLeaf(toInsert.getFirst(),toInsertInt.getFirst().intValue());
				return this;
			} else
			{
				System.out.println(
						"something off as this tree is build with increasing suffixes");
			}

		} else
		{
			final Letter affix = toInsert.removeFirst();
			final Integer affixLetterIndex = toInsertInt.removeFirst();
			final LinkedList<Letter> suffix = toInsert; 
			
			for (final NodeExpanded child : children)
			{
				if (child.l.equals(affix))
				{
					return child.insertAndReturnMainInsertionPoint(suffix,toInsertInt);

				}
			}
			//inner node needs to be added
			final NodeExpanded newNode = this.addLeaf(affix,affixLetterIndex.intValue());
			
			return newNode.insertAndReturnMainInsertionPoint(suffix,toInsertInt);
		}
		return this;
	}
	

	public LinkedList<Letter> getRepresentativeWord(){
		return getRepresentativeWord(new LinkedList<>());
	}
	private LinkedList<Letter> getRepresentativeWord(final LinkedList<Letter> list)
	{
		if (l==null)return list;
		list.addFirst(l);
		return parent.getRepresentativeWord(list);
	}

	private NodeExpanded addLeaf(final Letter letter, final int letterIndex)
	{
		final NodeExpanded n = new NodeExpanded(letter,letterIndex,letterIndex,this);
		
		this.children.add(n);
		n.traverseSuffixLinks();
		return n;
	}

	private void traverseSuffixLinks()
	{
		final NodeExpanded parentSufLink = parent.suflink;
		if (parentSufLink == null)
		{
			this.suflink = parent;
			return;
		}
		
		for (final NodeExpanded n : parentSufLink.children)
		{
			if (n.l.equals(l)) {
				this.suflink = n;
				return;
			}
				
				
		}
		final NodeExpanded newN = new NodeExpanded(l,letterStartIndex,letterEndIndex,parent.suflink);
		
		parentSufLink.children.add(newN);
		this.suflink = newN;
		newN.traverseSuffixLinks();

	}

	public void printTree(final int depth)
	{
		for (int i = 0; i < depth; i++)
			System.out.print(" ");
		System.out.println(l + ": " + count);
		for (final NodeExpanded child : children)
		{
			child.printTree(depth + 1);
		}
	}


	@Override
	public boolean isLeaf()
	{
		
		return this.children.size()==0;
	}


	@Override
	public int getId()
	{
		
		return id;
	}


	public int getDepth()
	{
		if (this.parent==null) return 0;
		return 1+this.parent.getDepth();
	}


	public boolean isDummyNode()
	{
		return false;
	}


	public int getCount()
	{
		return count;
	}


	public int countNodes()
	{
		int sizeCounter = 1;
		if (isLeaf())return sizeCounter;
		
		for (final NodeExpanded node : children)
		{
			sizeCounter+=node.countNodes();
		}
		return sizeCounter;
	}


	public void cleanAndTrim(final ArrayList<Letter> globalString)
	{
		
		children.removeIf(new Predicate<NodeExpanded>()
		{

			@Override
			public boolean test(final NodeExpanded t)
			{
				return t.getLetter(globalString).equals(Letter.emptySuffix);
			}
		});
		for (final NodeExpanded node : children)
		{
			node.cleanAndTrim(globalString);
		}
	}


	protected Letter getLetter(final ArrayList<Letter> globalString)
	{
		if (letterStartIndex==-1)return Letter.emptySuffix;
		return globalString.get(letterStartIndex);
	}


	public void pushDown(final int above,  final int letterIndexFromTop)
	{
		pushdownCounter += 1;
		
		if (this.isRoot()) {
			this.count += above; 
		}else {
			this.count += above;
			this.toPushDown += above;
			
			if (this.letterStartIndex==letterIndexFromTop) {
				this.suflink.pushDown(toPushDown,letterIndexFromTop);
				toPushDown = 0;
			}else {
				this.suflink.pushDown(toPushDown,letterIndexFromTop);
				toPushDown = 0;
				//here should be a stopping point to avoid repeated 
				//transfer down, but this currently only works for the fist word
				//as traveling again from some other main insertion line doesnt 
				//ensure that everything gets pushed down
			}
				
			
		}
		
	}
	public void pushDown(final int above,  final int letterIndexFromTop, final LinkedHashSet<NodeExpanded> nodesToPushDownNextRound)
	{
		pushdownCounter += 1;
		
		if (this.isRoot()) {
			this.count += above; 
		}else {
			this.count += above;
			this.toPushDown += above;
			if (above == 0 && toPushDown==0)return;
			
			if (this.letterStartIndex==letterIndexFromTop) {
				this.suflink.pushDown(toPushDown,letterIndexFromTop,nodesToPushDownNextRound);
				toPushDown = 0;
			}else {
				nodesToPushDownNextRound.add(this);
			}
				
			
		}
		
	}
	
	public int compare(final NodeExpanded n2) {
		return this.l.compare(n2.l);
	}


	@Override
	public boolean isRoot()
	{
		return this.parent==null;
	}


	@Override
	public String getLabel(final boolean asColumn, final boolean representativeString)
	{
		return ""+l + ":" + letterStartIndex + ":" + count + ":" + alternaCount; //"State"+
		
	}


	public void pullDown()
	{
		NodeExpanded.pullDownCounter++;
		if (isLeaf()) {
			alternaCount += 1;
			return;
		}
		this.alternaCount = 0;
		for (final NodeExpanded child : children)
		{
			child.pullDown();
			this.alternaCount+=child.alternaCount;
		}
		if (children.size()>1)this.alternaCount--;
		if (this.alternaCount!=this.count) {
			System.out.println("something off with" + this.getRepresentativeWord() +" " +  this.alternaCount + " "+ this.count);
		}
	}


	@Override
	public Node getSuflink()
	{
		return this.suflink;
	}


	@Override
	public Collection<Node> getNodeChildren()
	{
		return new ArrayList<Node>(this.children);
	}


	@Override
	public int getSupport()
	{
		return count;
	}


	@Override
	public List<Letter> getFullPathLabel()
	{
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public List<Letter> getPathLabel(final boolean cutAfterSeperator)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
