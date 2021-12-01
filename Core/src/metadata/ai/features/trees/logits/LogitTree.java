package metadata.ai.features.trees.logits;

import game.types.play.RoleType;
import metadata.ai.AIItem;

/**
 * Describes a Logit Tree for features (a regression tree representation of a feature set,
 * which outputs logits).
 * 
 * @author Dennis Soemers
 */
public class LogitTree implements AIItem
{
	
	//-------------------------------------------------------------------------
	
	/** Role (should either be All, or a specific Player) */
	protected final RoleType role;
	
	/** Root node of the tree */
	protected final LogitNode root;
	
	//-------------------------------------------------------------------------
	
	/**
	 * For a single logit tree for one role.
	 * 
	 * @param role The Player (P1, P2, etc.) for which the logit tree should apply,
	 * or All if it is applicable to all players in a game.
	 * @param root The root node of the tree.
	 * 
	 * @example (logitTree P1 (if "rel:to=<{}>:pat=<els=[f{0}]>" then:(leaf { (pair "Intercept" 1.0) }) else:(leaf { (pair "Intercept" -1.0) })))
	 */
	public LogitTree(final RoleType role, final LogitNode root)
	{
		this.role = role;
		this.root = root;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Root node of this logit tree
	 */
	public LogitNode root()
	{
		return root;
	}
	
	/**
	 * @return The role that this tree belongs to
	 */
	public RoleType role()
	{
		return role;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("(logitTree " + role + "\n");
		sb.append(root.toString(1) + "\n");
		sb.append(")");
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------

}
