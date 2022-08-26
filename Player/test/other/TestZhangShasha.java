package other;


import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import utils.data_structures.support.zhang_shasha.Tree;


/**
 * Tests for Zhang Shasha tree edit distance
 *
 * @author Dennis Soemers
 */
public class TestZhangShasha
{
	
	@Test
	public static void testZhangShashaFromStrings() throws IOException
	{
		// Sample trees (in preorder).
		final String tree1Str1 = "f(d(a c(b)) e)";
		final String tree1Str2 = "f(c(d(a b)) e)";
		// Distance: 2 (main example used in the Zhang-Shasha paper)

		final String tree1Str3 = "a(b(c d) e(f g(i)))";
		final String tree1Str4 = "a(b(c d) e(f g(h)))";
		// Distance: 1

		final String tree1Str5 = "d";
		final String tree1Str6 = "g(h)";
		// Distance: 2

		final Tree tree1 = new Tree(tree1Str1);
		final Tree tree2 = new Tree(tree1Str2);

		final Tree tree3 = new Tree(tree1Str3);
		final Tree tree4 = new Tree(tree1Str4);

		final Tree tree5 = new Tree(tree1Str5);
		final Tree tree6 = new Tree(tree1Str6);

		final int distance1 = Tree.ZhangShasha(tree1, tree2);
		assertEquals(distance1, 2);

		final int distance2 = Tree.ZhangShasha(tree3, tree4);
		assertEquals(distance2, 1);

		final int distance3 = Tree.ZhangShasha(tree5, tree6);
		assertEquals(distance3, 2);
	}

}
