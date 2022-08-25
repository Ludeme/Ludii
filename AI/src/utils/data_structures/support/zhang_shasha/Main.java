package utils.data_structures.support.zhang_shasha;

/**
 * Code originally from: https://github.com/ijkilchenko/ZhangShasha
 * 
 * Afterwards modified for style / various improvements
 *
 * @author Dennis Soemers
 */
public class Main 
{
	
	public static void main(String[] args)
	{
		// Sample trees (in preorder).
		String tree1Str1 = "f(d(a c(b)) e)";
		String tree1Str2 = "f(c(d(a b)) e)";
		// Distance: 2 (main example used in the Zhang-Shasha paper)

		String tree1Str3 = "a(b(c d) e(f g(i)))";
		String tree1Str4 = "a(b(c d) e(f g(h)))";
		// Distance: 1

		String tree1Str5 = "d";
		String tree1Str6 = "g(h)";
		// Distance: 2

		Tree tree1 = new Tree(tree1Str1);
		Tree tree2 = new Tree(tree1Str2);

		Tree tree3 = new Tree(tree1Str3);
		Tree tree4 = new Tree(tree1Str4);

		Tree tree5 = new Tree(tree1Str5);
		Tree tree6 = new Tree(tree1Str6);

		int distance1 = Tree.ZhangShasha(tree1, tree2);
		System.out.println("Expected 2; got " + distance1);

		int distance2 = Tree.ZhangShasha(tree3, tree4);
		System.out.println("Expected 1; got " + distance2);

		int distance3 = Tree.ZhangShasha(tree5, tree6);
		System.out.println("Expected 2; got " + distance3);
		
		//----------------------------------------

		final String a = 
			"game(TicTacToe players(a))";  
		final String b = 
			"game(TicTacToe players(b))";  

		final Tree ta = new Tree(a);
		final Tree tb = new Tree(b);
		
		int dist = Tree.ZhangShasha(ta, tb);
		System.out.println("dist=" + dist + ".");
		
		final String ttt1 = 
			"game(\"Tic-Tac-Toe\" "+   
			"    players(\"2\") " +  
			"    equipment(   " +
			"        board(square(\"3\")) " + 
			"        piece(\"Disc\" P1) " +
            "        piece(\"Cross\" P2) " +
    		"    ) " + 
            "    rules( " +
            "        play(add(empty)) " +
            "            end(if(isLine(\"3\") result(Mover Win))) " +
            "    )" +
            ")";
		final Tree treeA = new Tree(ttt1);
		System.out.println("treeA: " + treeA);
	
		final String ttt1a = 
			"game(\"Tic-Tac-Toe\" "+   
			"    players(\"2\") " +  
			"    equipment(   " +
			"        board(hexagon(\"3\")) " + 
			"        piece(\"Disc\" P1) " +
            "        piece(\"Cross\" P2) " +
    		"    ) " + 
            "    rules( " +
            "        play(add(empty)) " +
            "            end(if(isLine(\"4\") result(Mover Win))) " +
            "    )" +
            ")";
		final Tree treeAa = new Tree(ttt1a);
		System.out.println("treeAa: " + treeAa);
	
		int distX = Tree.ZhangShasha(treeA, treeA);
		int distY = Tree.ZhangShasha(treeA, treeAa);
		System.out.println("distX=" + distX + ", distY=" + distY + ".");
		
		final String ttt2 = 
			"(game \"Tic-Tac-Toe\" " +  
			"    (players 2) " +   
			"    (equipment { " +
			"        (board (hexagon 3)) " + 
			"        (piece \"Disc\" P1) " +
            "        (piece \"Cross\" P2) " +
    		"    }) " + 
            "    (rules " +
            "        (play (add (empty))) " +
            "            (end (if (isLine 3) (result Mover Win))) " +
            "    )" +
            ")";
		final Tree treeB = new Tree(ttt2);
		System.out.println("treeB: " + treeB);

		final String hex =
			"(game \"Hex\" " +  
			"    (players 2) " + 
			"    (equipment { " +
			"        (board (rhombus 11)) " + 
			"        (piece \"Ball\" Each) " +
			"        (regions P1 { (sites Side NE) (sites Side SW) } ) " +
			"        (regions P2 { (sites Side NW) (sites Side SE) } ) " +
			"    }) " + 
			"    (rules " +
			"        (meta (swap)) " +
			"        (play (add (empty))) " +
			"        (end (if (isConnected Mover) (result Mover Win))) " + 
			"    ) " +
			")";
		final Tree treeC = new Tree(hex);
		System.out.println("treeC: " + treeC);

		int distAA = Tree.ZhangShasha(treeA, treeA);
		int distAB = Tree.ZhangShasha(treeA, treeB);
		int distAC = Tree.ZhangShasha(treeA, treeC);
		int distBC = Tree.ZhangShasha(treeB, treeC);
		int distBA = Tree.ZhangShasha(treeB, treeA);
		int distCA = Tree.ZhangShasha(treeC, treeA);
		int distCB = Tree.ZhangShasha(treeC, treeB);
		
		System.out.println("distAA=" + distAA + ".");
		System.out.println("distAB=" + distAB + ", distAC=" + distAC + ", distBC=" + distBC + ".");
		System.out.println("distBA=" + distBA + ", distCA=" + distCA + ", distCB=" + distCB + ".");
		
	}
}
