package metrics.suffix_tree.savety;

import java.util.ArrayList;
import java.util.List;

import metrics.suffix_tree.Letter;

public class NOdeSavety2
{
	NOdeSavety2 parent;
	NOdeSavety2 suflink;
	Letter l;
	int count = 0;
	ArrayList<NOdeSavety2> children = new ArrayList<>();
	private final int id = idCounter++;
	private static int idCounter = 0;
	
	@Override
	public String toString() {
		return l + " cs: " + children.size() + "linked: " + suflink;
	}
	

	public void insert(final List<Letter> toInsert)
	{
		//System.out.println("currentNode:" + getRepresentativeWord() +" toInsert" + toInsert);
		
		
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
			final Letter affix = toInsert.get(0);
			final List<Letter> suffix = toInsert.subList(1, toInsert.size());
			for (final NOdeSavety2 child : children)
			{
				if (child.l.equals(affix))
				{
					child.insert(suffix);

					return;
				}
			}
			//inner node needs to be added
			final NOdeSavety2 newNode = new NOdeSavety2();
			newNode.count=1;
			newNode.parent = this;
			newNode.l = affix;
			newNode.traverseSuffixLinks();
			this.children.add(newNode);
			newNode.insert(suffix);
		}

	}

	private void addLeaf(final Letter letter)
	{
		final NOdeSavety2 n = new NOdeSavety2();
		n.count=1;
		n.l = letter;
		n.parent = this;
		this.children.add(n);
		n.traverseSuffixLinks();

	}

	private void traverseSuffixLinks()
	{
		final NOdeSavety2 parentSufLink = parent.suflink;
		if (parentSufLink == null)
		{
			this.suflink = parent;
			return;
		}
		
		for (final NOdeSavety2 n : parentSufLink.children)
		{
			if (n.l.equals(l)) {
				this.suflink = n;
				n.count++;
				n.traverseSuffixLinks();
				return;
			}
				
				
		}
		final NOdeSavety2 newN = new NOdeSavety2();
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
		for (final NOdeSavety2 child : children)
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

}
