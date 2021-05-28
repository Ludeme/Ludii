package search.pns;

import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import other.AI;
import other.context.Context;
import other.move.Move;
import search.pns.PNSNode.PNSNodeTypes;
import search.pns.PNSNode.PNSNodeValues;

/**
 * Proof-number search.
 *
 * @author Dennis Soemers
 */
public class ProofNumberSearch extends AI
{
	
	//-------------------------------------------------------------------------
	
	/**
	 * Different goals that we can try to prove with PNS
	 *
	 * @author Dennis Soemers
	 */
	public enum ProofGoals
	{
		/** If we want to prove that a position is a win for current mover */
		PROVE_WIN,
		
		/** If we want to prove that a position is a loss for current mover */
		PROVE_LOSS
	}
	
	//-------------------------------------------------------------------------
	
	/** Our proof goal */
	protected final ProofGoals proofGoal;
	
	/** The player for which we aim to prove either a win or a loss */
	protected int proofPlayer = -1;
	
	/** The best possible rank we can get from the root state we're searching for */
	protected double bestPossibleRank = -1.0;
	
	/** The worst possible rank we can get from the root state we're searching for */
	protected double worstPossibleRank = -1.0;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public ProofNumberSearch()
	{
		this(ProofGoals.PROVE_WIN);
	}
	
	/**
	 * Constructor
	 * 
	 * @param proofGoal
	 */
	public ProofNumberSearch(final ProofGoals proofGoal)
	{
		friendlyName = "Proof-Number Search";
		this.proofGoal = proofGoal;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Move selectAction
	(
		final Game game, 
		final Context context, 
		final double maxSeconds,
		final int maxIterations,
		final int maxDepth
	)
	{
		//System.out.println("WARNING! This does not yet appear to be correct in all cases!");
		
		bestPossibleRank = context.computeNextWinRank();
		worstPossibleRank = context.computeNextLossRank();
		
		if (proofPlayer != context.state().mover())
		{
			System.err.println("Warning: Current mover = " + context.state().mover() + ", but proof player = " + proofPlayer + "!");
		}
		
		final PNSNode root = new PNSNode(null, copyContext(context), proofGoal, proofPlayer);
		evaluate(root);
		setProofAndDisproofNumbers(root);
		
		PNSNode currentNode = root;
		
		while (root.proofNumber() != 0 && root.disproofNumber() != 0)
		{
			final PNSNode mostProvingNode = selectMostProvingNode(currentNode);
			expandNode(mostProvingNode);
			currentNode = updateAncestors(mostProvingNode);
		}
		
//		System.out.println();
//		System.out.println("Proof goal = " + proofGoal);
//		System.out.println("Proof player = " + proofPlayer);
//		System.out.println("Root type = " + root.nodeType());
//		System.out.println("root pn = " + root.proofNumber());
//		System.out.println("root dn = " + root.disproofNumber());
//		for (int i = 0; i < root.children.length; ++i)
//		{
//			if (root.children[i] == null)
//			{
//				System.out.println("child " + i + " = null");
//			}
//			else
//			{
//				System.out.println("child " + i + " pn = " + root.children[i].proofNumber());
//				System.out.println("child " + i + " dn = " + root.children[i].disproofNumber());
//			}
//		}
		
		if (proofGoal == ProofGoals.PROVE_WIN)
		{
			if (root.proofNumber() == 0)
				System.out.println("Proved a win!");
			else
				System.out.println("Disproved a win!");
		}
		else
		{
			if (root.proofNumber() == 0)
				System.out.println("Proved a loss!");
			else
				System.out.println("Disproved a loss!");
		}
		
		return root.legalMoves[ThreadLocalRandom.current().nextInt(root.legalMoves.length)];
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Evaluates the given node
	 * @param node
	 */
	private void evaluate(final PNSNode node)
	{
		final Context context = node.context();
		
		if (context.trial().over())
		{
			final double rank = context.trial().ranking()[proofPlayer];
			
			if (rank == bestPossibleRank)
			{
				if (proofGoal == ProofGoals.PROVE_WIN)
					node.setValue(PNSNodeValues.TRUE);
				else
					node.setValue(PNSNodeValues.FALSE);
			}
			else if (rank == worstPossibleRank)
			{
				if (proofGoal == ProofGoals.PROVE_WIN)
					node.setValue(PNSNodeValues.FALSE);
				else
					node.setValue(PNSNodeValues.TRUE);
			}
			else
			{
				node.setValue(PNSNodeValues.FALSE);
			}
		}
		else
		{
			node.setValue(PNSNodeValues.UNKNOWN);
		}
	}
	
	/**
	 * Sets proof and disproof numbers for given node
	 * @param node
	 */
	private static void setProofAndDisproofNumbers(final PNSNode node)
	{
		if (node.isExpanded())	// internal node
		{
			if (node.nodeType() == PNSNodeTypes.AND_NODE)
			{
				node.setProofNumber(0);
				node.setDisproofNumber(Integer.MAX_VALUE);
				
				for (final PNSNode child : node.children())
				{
					if (node.proofNumber() == Integer.MAX_VALUE || child.proofNumber() == Integer.MAX_VALUE)
						node.setProofNumber(Integer.MAX_VALUE);
					else
						node.setProofNumber(node.proofNumber() + child.proofNumber());
					
					if (child != null && child.disproofNumber() < node.disproofNumber())
						node.setDisproofNumber(child.disproofNumber());
				}
			}
			else	// OR node
			{
				node.setProofNumber(Integer.MAX_VALUE);
				node.setDisproofNumber(0);
				
				for (final PNSNode child : node.children())
				{
					if (node.disproofNumber() == Integer.MAX_VALUE || child.disproofNumber() == Integer.MAX_VALUE)
						node.setDisproofNumber(Integer.MAX_VALUE);
					else
						node.setDisproofNumber(node.disproofNumber() + child.disproofNumber());
					
					if (child != null && child.proofNumber() < node.proofNumber())
						node.setProofNumber(child.proofNumber());
				}
			}
		}
		else		// leaf node
		{
			switch(node.value())
			{
			case FALSE:
				node.setProofNumber(Integer.MAX_VALUE);
				node.setDisproofNumber(0);
				break;
			case TRUE:
				node.setProofNumber(0);
				node.setDisproofNumber(Integer.MAX_VALUE);
				break;
			case UNKNOWN:
				// Init as described in 7.1 of 
				// "GAME-TREE SEARCH USING PROOF NUMBERS: HE FIRST TWENTY YEARS"
				if (node.nodeType() == PNSNodeTypes.AND_NODE)
				{
					node.setProofNumber(Math.max(1, node.children.length));
					node.setDisproofNumber(1);
				}
				else		// OR node
				{
					node.setProofNumber(1);
					node.setDisproofNumber(Math.max(1, node.children.length));
				}
				
				break;
			}
		}
	}
	
	/**
	 * @param inCurrentNode
	 * @return Most proving node in subtree rooted in given current node
	 */
	private static PNSNode selectMostProvingNode(final PNSNode inCurrentNode)
	{
		//System.out.println();
		//System.out.println("starting");
		PNSNode current = inCurrentNode;
		
		while (current.isExpanded())
		{
			final PNSNode[] children = current.children();
			int nextIdx = 0;
			PNSNode next = children[nextIdx];
			
			if (current.nodeType() == PNSNodeTypes.OR_NODE)
			{
				while (true)
				{
					if (next != null)
					{
						//System.out.println("next not null");
						if (next.proofNumber() == current.proofNumber())
							break;
					}
					
					++nextIdx;
					if (nextIdx < children.length)
						next = children[nextIdx];
					else
						break;
				}
			}
			else		// AND node
			{
				while (true)
				{
					if (next != null)
					{
						//System.out.println("next not null");
						if (next.disproofNumber() == current.disproofNumber())
							break;
					}
					
					++nextIdx;
					if (nextIdx < children.length)
						next = children[nextIdx];
					else
						break;
				}
			}	
			
			current = next;
		}
		
		return current;
	}
	
	/**
	 * Expands the given node
	 * @param node
	 */
	private void expandNode(final PNSNode node)
	{
		final PNSNode[] children = node.children();

		for (int i = 0; i < children.length; ++i)
		{
			final Context newContext = new Context(node.context());
        	newContext.game().apply(newContext, node.legalMoves[i]);
        	final PNSNode child = new PNSNode(node, newContext, proofGoal, proofPlayer);
			children[i] = child;
			
			evaluate(child);
			setProofAndDisproofNumbers(child);
			
			if 
			(
				(node.nodeType() == PNSNodeTypes.OR_NODE && child.proofNumber() == 0) ||
				(node.nodeType() == PNSNodeTypes.AND_NODE && child.disproofNumber() == 0)
			)
			{
				break;
			}
		}
		
		node.setExpanded(true);
	}
	
	/**
	 * Updates proof and disproof numbers for all ancestors of given node
	 * @param inNode
	 * @return Node from which to search for next most proving node
	 */
	private static PNSNode updateAncestors(final PNSNode inNode)
	{
		PNSNode node = inNode;
		
		do
		{
			final int oldProof = node.proofNumber();
			final int oldDisproof = node.disproofNumber();
			
			setProofAndDisproofNumbers(node);
			
			if (node.proofNumber() == oldProof && node.disproofNumber() == oldDisproof)
			{
				// No change on the path
				return node;
			}
			
			// Delete (dis)proved subtrees
			if (node.proofNumber() == 0 || node.disproofNumber() == 0)
				node.deleteSubtree();
			
			if (node.parent == null)
				return node;
			
			node = node.parent;
		} 
		while (true);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public void initAI(final Game game, final int playerID)
	{
		proofPlayer = playerID;
	}
	
	@Override
	public boolean supportsGame(final Game game)
	{
		if (game.players().count() != 2)
			return false;
		
		if (game.isStochasticGame())
			return false;
		
		if (game.hiddenInformation())
			return false;
		
		return game.isAlternatingMoveGame();
	}
	
	//-------------------------------------------------------------------------

}
