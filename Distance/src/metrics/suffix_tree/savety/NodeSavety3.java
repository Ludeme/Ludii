package metrics.suffix_tree.savety;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

import metrics.suffix_tree.Letter;

public class NodeSavety3 implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	NodeSavety3 parent;
	NodeSavety3 suflink;
	Letter l;
	int count = 0;
	transient ArrayList<NodeSavety3> children = new ArrayList<>();
	private final int id = idCounter++;
	private static int idCounter = 0;
	
	@Override
	public String toString() {
		return l + " cs: " + children.size() + "linked: " + suflink;
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
			
			for (final NodeSavety3 child : children)
			{
				if (child.l.equals(affix))
				{
					child.insert(suffix);
					return;
				}
			}
			//inner node needs to be added
			final NodeSavety3 newNode = new NodeSavety3();
			newNode.count=1;
			newNode.parent = this;
			newNode.l = affix;
			newNode.traverseSuffixLinks();
			this.children.add(newNode);
			newNode.insert(suffix);
		}
		
	}
	
	public NodeSavety3 insertAndReturnMainInsertionPoint(final LinkedList<Letter> toInsert)
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
			
			for (final NodeSavety3 child : children)
			{
				if (child.l.equals(affix))
				{
					return child.insertAndReturnMainInsertionPoint(suffix);

				}
			}
			//inner node needs to be added
			final NodeSavety3 newNode = new NodeSavety3();
			newNode.count=1;
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
		final NodeSavety3 n = new NodeSavety3();
		n.count=1;
		n.l = letter;
		n.parent = this;
		this.children.add(n);
		n.traverseSuffixLinks();
		

	}

	private void traverseSuffixLinks()
	{
		final NodeSavety3 parentSufLink = parent.suflink;
		if (parentSufLink == null)
		{
			this.suflink = parent;
			return;
		}
		
		for (final NodeSavety3 n : parentSufLink.children)
		{
			if (n.l.equals(l)) {
				this.suflink = n;
				n.count++;
				
				n.traverseSuffixLinks();
				return;
			}
				
				
		}
		final NodeSavety3 newN = new NodeSavety3();
		parentSufLink.children.add(newN);
		newN.count=1;
		newN.l = l;
		newN.parent = parent.suflink;
		this.suflink = newN;
		newN.traverseSuffixLinks();

	}

	public void printTree(final int depth)
	{
		for (int i = 0; i < depth; i++)
			System.out.print(" ");
		System.out.println(l + ": " + count);
		for (final NodeSavety3 child : children)
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
		
		for (final NodeSavety3 node : children)
		{
			sizeCounter+=node.countNodes();
		}
		return sizeCounter;
	}


	public void cleanAndTrim()
	{
		
		final int s = children.size();
		for (int i = s-1; i >= 0 ; i--)
		{
			final NodeSavety3 currentChild = children.get(i);
			if (currentChild.l.equals(Letter.emptySuffix)) {
				children.remove(i);
			}else {
				currentChild.cleanAndTrim();
			}
		}
		children.trimToSize();
		
	}

}
