package metrics.suffix_tree.savety;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.function.Predicate;

import metrics.suffix_tree.Letter;

public class NodeSavety4 implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	NodeSavety4 parent;
	NodeSavety4 suflink;
	Letter l;
	int count = 0;
	transient TreeSet<NodeSavety4> children = new TreeSet<>(comparator);
	private final int id = idCounter++;
	int pushdown;
	public static int pushdownCounter = 0;
	private static int idCounter = 0;
	public static Comparator<NodeSavety4> comparator = createComperator();
	
	@Override
	public String toString() {
		return l + " cs: " + children.size() + "linked: " + suflink;
	}
	

	private static Comparator<NodeSavety4> createComperator()
	{
		return new Comparator<NodeSavety4>()
		{

			@Override
			public int compare(final NodeSavety4 o1, final NodeSavety4 o2)
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
			
			for (final NodeSavety4 child : children)
			{
				if (child.l.equals(affix))
				{
					child.insert(suffix);
					return;
				}
			}
			//inner node needs to be added
			final NodeSavety4 newNode = new NodeSavety4();
			newNode.count=1;
			newNode.parent = this;
			newNode.l = affix;
			newNode.traverseSuffixLinks();
			this.children.add(newNode);
			newNode.insert(suffix);
		}
		
	}
	
	public NodeSavety4 insertAndReturnMainInsertionPoint(final LinkedList<Letter> toInsert)
	{		
		if (toInsert.size() == 0) {
			
			this.count++;
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
			
			for (final NodeSavety4 child : children)
			{
				if (child.l.equals(affix))
				{
					return child.insertAndReturnMainInsertionPoint(suffix);

				}
			}
			//inner node needs to be added
			final NodeSavety4 newNode = new NodeSavety4();
			newNode.count=1;
			newNode.pushdown=1;
			newNode.parent = this;
			newNode.l = affix;
			newNode.traverseSuffixLinks();
			this.children.add(newNode);
			return newNode.insertAndReturnMainInsertionPoint(suffix);
		}
		return this;
	}
	

	private LinkedList<Letter> getRepresentativeWord(){
		return getRepresentativeWord(new LinkedList<>());
	}
	private LinkedList<Letter> getRepresentativeWord(final LinkedList<Letter> list)
	{
		if (l==null)return list;
		list.addFirst(l);
		return parent.getRepresentativeWord(list);
	}

	private void addLeaf(final Letter letter)
	{
		final NodeSavety4 n = new NodeSavety4();
		n.count=0;
		n.pushdown=0;
		n.l = letter;
		n.parent = this;
		this.children.add(n);
		n.traverseSuffixLinks();
		

	}

	private void traverseSuffixLinks()
	{
		final NodeSavety4 parentSufLink = parent.suflink;
		if (parentSufLink == null)
		{
			this.suflink = parent;
			return;
		}
		
		for (final NodeSavety4 n : parentSufLink.children)
		{
			if (n.l.equals(l)) {
				this.suflink = n;
				n.count++;
				
				if (n.count - n.pushdown != 1) {
					//System.out.println("theory broken");
				}
				//n.traverseSuffixLinks();
				return;
			}
				
				
		}
		final NodeSavety4 newN = new NodeSavety4();
		newN.count=1;
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
		for (final NodeSavety4 child : children)
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
		
		for (final NodeSavety4 node : children)
		{
			sizeCounter+=node.countNodes();
		}
		return sizeCounter;
	}


	public void cleanAndTrim()
	{
		
		children.removeIf(new Predicate<NodeSavety4>()
		{

			@Override
			public boolean test(final NodeSavety4 t)
			{
				return t.l.equals(Letter.emptySuffix);
			}
		});
		for (final NodeSavety4 node : children)
		{
			node.cleanAndTrim();
		}
	}


	public void pushDown(final int above, final HashMap<NodeSavety4,Boolean> checked)
	{
		pushdownCounter += 1;
		if (checked.containsKey(this)) {
			return;
		}
		checked.put(this, Boolean.TRUE);
		this.count += above;
		if (this.suflink == null) {
			//idontknow
		}else {
			final int pushDownSum = this.pushdown+above;
			this.pushdown = 0;
			if (pushDownSum!=0)
				this.suflink.pushDown(pushDownSum,checked);
		}
		
	}
	
	public int compare(final NodeSavety4 n2) {
		return this.l.compare(n2.l);
	}

}
