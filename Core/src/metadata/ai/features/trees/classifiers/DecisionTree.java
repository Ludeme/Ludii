package metadata.ai.features.trees.classifiers;

import game.types.play.RoleType;
import metadata.ai.AIItem;

/**
 * Describes a Decision Tree for features (a decision tree representation of a feature set,
 * which outputs class predictions).
 * 
 * @author Dennis Soemers
 */
public class DecisionTree implements AIItem
{
	
	//-------------------------------------------------------------------------
	
	/** Role (should either be All, or a specific Player) */
	protected final RoleType role;
	
	/** Root node of the tree */
	protected final DecisionTreeNode root;
	
	//-------------------------------------------------------------------------
	
	/**
	 * For a single decision tree for one role.
	 * 
	 * @param role The Player (P1, P2, etc.) for which the logit tree should apply,
	 * or All if it is applicable to all players in a game.
	 * @param root The root node of the tree.
	 * 
	 * @example (decisionTree P1 (if "rel:to=<{}>:pat=<els=[f{0}]>" then:(leaf bottom25:0.0 iqr:0.2 top25:0.8) else:(leaf bottom25:0.6 iqr:0.35 top25:0.05)))
	 */
	public DecisionTree(final RoleType role, final DecisionTreeNode root)
	{
		this.role = role;
		this.root = root;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The role that this tree was built for.
	 */
	public RoleType role()
	{
		return role;
	}
	
	/**
	 * @return The root node of this tree
	 */
	public DecisionTreeNode root()
	{
		return root;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("(decisionTree " + role + "\n");
		sb.append(root.toString(1) + "\n");
		sb.append(")");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------

}
