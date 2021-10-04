package processing.grammarGMLComposer;

import java.util.ArrayList;
import java.util.Collection;

import main.grammar.ebnf.EBNFClause;
import main.grammar.ebnf.EBNFRule;

public class Node
{

	ArrayList<Node> children = new ArrayList<Node>();
	private final boolean isRule;
	private final EBNFRule rule;
	private final boolean isTerminal;
	private final EBNFClause ebnfClause;
	
	public Node(final EBNFRule rule)
	{
		this.isRule = true;
		this.rule = rule;
		this.isTerminal = false;
		this.ebnfClause = null;
	}


	private Node(final EBNFClause ebnfClause)
	{
		this.isRule = false;
		this.rule = null;
		this.isTerminal = true;
		this.ebnfClause = ebnfClause;
	}

	@Override
	public boolean equals(final Object o2) {
		if (!(o2 instanceof Node))return false;
		return this.getUniqueIdentifier().equals(((Node)o2).getUniqueIdentifier());
	}
	
	public EBNFRule getRule()
	{
		return rule;
	}

	public boolean isRule()
	{
		return isRule;
	}

	public Collection<? extends Node> getNodeChildren()
	{
		return children;
	}

	public static Node createTerminal(final EBNFClause ebnfClause)
	{
		final Node n = new Node(ebnfClause);
		
		return n;
	}
	String getLabel()
	{
		if (isRule)return rule.lhs().toString();
		if (isTerminal) return ebnfClause.toString();
		return null;
	}

	public String getUniqueIdentifier()
	{
		if (isRule)return rule.lhs().toString();
		if (isTerminal) return ebnfClause.token().toString();
		return null;
	}

	public void connectChild(final Node child)
	{
		if (!this.children.contains(child))
			this.children.add(child);
	}

}
