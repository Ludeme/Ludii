package metrics.suffix_tree.savety;

import java.io.Serializable;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.function.Predicate;

import metrics.suffix_tree.Letter;

public class NodeSaveTy5 implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	NodeSaveTy5 parent;
	NodeSaveTy5 suflink;
	Letter l;
	int count = 0;
	transient TreeSet<NodeSaveTy5> children = new TreeSet<>(comparator);
	private final int id = idCounter++;
	int pushdown;
	public static int pushdownCounter = 0;
	private static int idCounter = 0;
	public static Comparator<NodeSaveTy5> comparator = createComperator();
	
	@Override
	public String toString() {
		return l + " cs: " + children.size() + "linked: " + suflink;
	}
	

	private static Comparator<NodeSaveTy5> createComperator()
	{
		return new Comparator<NodeSaveTy5>()
		{

			@Override
			public int compare(final NodeSaveTy5 o1, final NodeSaveTy5 o2)
			{
				return o1.l.compare(o2.l);
			}
		};
	}


	public void insert(final LinkedList<Letter> toInsert)
	{		
		if (toInsert.size() == 0) {
			
			this.count++;
			this.traverseSuffixLinks();
		}
		if (children.size() == 0)
		{
			if (toInsert.size() == 1)
			{
				addLeaf(toInsert.get(0));
			} else
			{
				System.out.println(
						"something off as this tree is build with increasing suffixes");
			}

		} else
		{
			final Letter affix = toInsert.removeFirst();
			final LinkedList<Letter> suffix = toInsert; 
			
			for (final NodeSaveTy5 child : children)
			{
				if (child.l.equals(affix))
				{
					child.insert(suffix);
					return;
				}
			}
			//inner node needs to be added
			final NodeSaveTy5 newNode = new NodeSaveTy5();
			newNode.count=0;
			newNode.parent = this;
			newNode.l = affix;
			newNode.traverseSuffixLinks();
			this.children.add(newNode);
			newNode.insert(suffix);
		}
		
	}
	
	public NodeSaveTy5 insertAndReturnMainInsertionPoint(final LinkedList<Letter> toInsert)
	{		
		if (toInsert.size() == 0) {
			
			this.traverseSuffixLinks();
			return this.parent;
		}
		if (children.size() == 0)
		{
			if (toInsert.size() == 1)
			{
				addLeaf(toInsert.get(0));
				return this;
			} else
			{
				System.out.println(
						"something off as this tree is build with increasing suffixes");
			}

		} else
		{
			final Letter affix = toInsert.removeFirst();
			final LinkedList<Letter> suffix = toInsert; 
			
			for (final NodeSaveTy5 child : children)
			{
				if (child.l.equals(affix))
				{
					return child.insertAndReturnMainInsertionPoint(suffix);

				}
			}
			//inner node needs to be added
			final NodeSaveTy5 newNode = new NodeSaveTy5();
			newNode.count=0;
			newNode.pushdown=0;
			newNode.parent = this;
			newNode.l = affix;
			newNode.traverseSuffixLinks();
			this.children.add(newNode);
			return newNode.insertAndReturnMainInsertionPoint(suffix);
		}
		return this;
	}
	

	private void addLeaf(final Letter letter)
	{
		final NodeSaveTy5 n = new NodeSaveTy5();
		n.count=0;
		n.pushdown=0;
		n.l = letter;
		n.parent = this;
		this.children.add(n);
		n.traverseSuffixLinks();
		

	}

	private void traverseSuffixLinks()
	{
		final NodeSaveTy5 parentSufLink = parent.suflink;
		if (parentSufLink == null)
		{
			this.suflink = parent;
			return;
		}
		
		for (final NodeSaveTy5 n : parentSufLink.children)
		{
			if (n.l.equals(l)) {
				this.suflink = n;
				return;
			}
				
				
		}
		final NodeSaveTy5 newN = new NodeSaveTy5();
		newN.count=0;
		newN.pushdown=0;
		newN.l = l;
		newN.parent = parent.suflink;
		parentSufLink.children.add(newN);
		this.suflink = newN;
		newN.traverseSuffixLinks();

	}

	public void printTree(final int depth)
	{
		for (int i = 0; i < depth; i++)
			System.out.print(" ");
		System.out.println(l + ": " + count);
		for (final NodeSaveTy5 child : children)
		{
			child.printTree(depth + 1);
		}
	}


	public boolean isLeaf()
	{
		
		return this.children.size()==0;
	}


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
		
		for (final NodeSaveTy5 node : children)
		{
			sizeCounter+=node.countNodes();
		}
		return sizeCounter;
	}


	public void cleanAndTrim()
	{
		
		children.removeIf(new Predicate<NodeSaveTy5>()
		{

			@Override
			public boolean test(final NodeSaveTy5 t)
			{
				return t.l.equals(Letter.emptySuffix);
			}
		});
		for (final NodeSaveTy5 node : children)
		{
			node.cleanAndTrim();
		}
	}


	public void pushDown(final int above)
	{
		pushdownCounter += 1;
		if (l!=null&&l.equals(Letter.emptySuffix)) {
			System.out.println("unexpected");
		}
		
		
		if (this.suflink == null) {
			this.count += above; //nobody cares for the empty suffix
			this.pushdown = 0;
		}else {
			this.count += above + this.pushdown;
			final int pushDownSum = this.pushdown+above;
			this.pushdown = 0;
			if (pushDownSum!=0)
				this.suflink.pushDown(pushDownSum);
		}
		
	}
	
	public int compare(final NodeSaveTy5 n2) {
		return this.l.compare(n2.l);
	}

}
