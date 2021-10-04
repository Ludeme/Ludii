package zhang_shasha;

import java.util.ArrayList;

public class Node {
	public String label; // node label
	public int index; // preorder index
	// note: trees need not be binary
	public ArrayList<Node> children = new ArrayList<Node>();
	public Node leftmost; // used by the recursive O(n) leftmost() function

	public Node() {

	}

	public Node(String label) {
		this.label = label;
	}
}
