package games;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.Assert;
import org.junit.Test;

import game.Game;
import compiler.Compiler;
import main.grammar.Description;
import other.context.Context;
import other.trial.Trial;

//-----------------------------------------------------------------------------

/**
 * Unit Test to compile all the games on the lud folder
 * 
 * @author Eric.Piette
 */
public class ErrorHandlingTests
{
	
	//-------------------------------------------------------------------------

	private final String TTT = 
			"(game \"Tic-Tac-Toe\"" + 
			"  (mode 2)" +  
			"  (equipment { (board (square 3) (square)) (disc P1) (cross P2) })" +  
			"  (rules " + 
			"    (play (to (mover) (empty)))" +
            "    (end (line length:3) (result Mover Win))"+  
            "  )"+         
            ")";
	
	private final String TTTMissingKeyword = 
			"(game \"Tic-Tac-Toe\"" + 
			"  (mode 2)" +  
			"  (equipment { (boardBad (square 3) (square)) (disc P1) (cross P2) })" +  
			"  (rules " + 
			"    (play (to (mover) (empty)))" +
            "    (end (line length:3) (result Mover Win))"+  
            "  )"+         
            ")";

//	private final String TTTMetadataGood = 
//			"(game \"Tic-Tac-Toe\"" + 
//			"  { (metadata \"Grammar\" \"v0.1\") }" +  
//			"  (mode 2)" +  
//			"  (equipment { (board (square 3) (square)) (disc P1) (cross P2) })" +  
//			"  (rules " + 
//			"    (play (to (mover) (empty)))" +
//            "    (end (line length:3) (result Mover Win))"+  
//            "  )"+         
//            ")";
//
//	private final String TTTMetadataBad = 
//			"(game \"Tic-Tac-Toe\"" + 
//			"  (metadata {\"Grammar\" \"v0.1\"})" +  
//			"  (mode 2)" +  
//			"  (equipment { (board (square 3) (square)) (disc P1) (cross P2) })" +  
//			"  (rules " + 
//			"    (play (to (mover) (empty)))" +
//            "    (end (line length:3) (result Mover Win))"+  
//            "  )"+         
//            ")";
	
		private final String TTTCantDecompose = 
			"(game \"Tic-Tac-Toe\"" + 
			"  (mode 2)" +  
			"  (equipment { (board (square 3) (square)) (disc P1) (cross P2) })" +  
			"  (rules " + 
			"    (play (to (mover) (empty)))" +
            "    (end (line length:3) (result Mover Win))"+  
            ")";
	
		private final String TTTBadTerminal = 
		"(game \"Tic-Tac-Toe\"" + 
		"  (mode 2)" +  
		"  (equipment { (board (square 3) (square)) (disc P57) (cross P2) })" +  
		"  (rules " + 
		"    (play (to (mover) (empty)))" +
        "    (end (line length:3) (result Mover Win))"+  
        "  )"+         
        ")";

		private final String TTTBadArray1 = 
		"(game \"Tic-Tac-Toe\"" + 
		"  (mode 2)" +  
		"  (equipment { (board (square 3) (square)) (to) (disc P1) (cross P2) })" +  
		"  (rules " + 
		"    (play (to (mover) (empty)))" +
        "    (end (line length:3) (result Mover Win))"+  
        "  )"+         
        ")";

		private final String TTTMissingQuote = 
		"(game \"Tic-Tac-Toe" + 
		"  (mode 2)" +  
		"  (equipment { (board (square 3) (square)) (to) (disc P1) (cross P2) })" +  
		"  (rules " + 
		"    (play (to (mover) (empty)))" +
        "    (end (line length:3) (result Mover Win))"+  
        "  )"+         
        ")";
		
		private final String TTT_RECURSIVE_DEFINES = 
				"(game \"Tic-Tac-Toe\"" + 
				"  (mode 2)" +
				"  (equipment { (board (square 3) (square)) (disc P1) (cross P2) })" +  
				"  (rules " + 
				"    (play (to (mover) (empty)))" +
	            "    (end (line length:3) (result Mover Win))"+  
	            "  )"+         
	            ")"+ 
	            "(define \"A\" \"B\"";
		
		private final String TTTBadRange1 = 
			"(game \"Tic-Tac-Toe\"" + 
			"  (mode 2)" +  
			"  (equipment { " +
			"     (board (square 3) (square)) (disc P1) (cross P2)" +
			"     (track \"Track\" \"Board\" {1.. 12...7} true)" +
			"  })" +
			"  (rules " + 
			"    (play (to (mover) (empty)))" +
            "    (end (line length:3) (result Mover Win))"+  
            "  )"+         
            ")";
	
		private final String TTTBadRange2 = 
			"(game \"Tic-Tac-Toe\"" + 
			"  (mode 2)" +  
			"  (equipment { " +
			"     (board (square 3) (square)) (disc P1) (cross P2)" +
			"     (track \"Track\" \"Board\" {1..1000000} true)" +
			"  })" +
			"  (rules " + 
			"    (play (to (mover) (empty)))" +
            "    (end (line length:3) (result Mover Win))"+  
            "  )"+         
            ")";
	
		private final String TTTRecursiveOption = 
			"(game \"Tic-Tac-Toe\"" + 
			"  (mode 2)" +  
			"  (equipment { (board (square 3) (square)) (disc P1) (cross P2) <1> })" +  
			"  (rules " + 
			"    (play (to (mover) (empty)))" +
            "    (end (line length:3) (result Mover Win))"+  
            "  )"+         
            ")" + 
            "(option <1> \"Board Size/3x3\" <(option <1> \"Board Size/3x3\" <1>)>)";
		
			private final String TTTBadDefine1 = 
			"(game \"Tic-Tac-Toe\"" + 
			"  (mode 2)" +  
			"  (define \"step\" (step (in (to) (empty) \"step\")))" +
			"  (equipment { (board (square 3) (square)) (disc P1) (cross P2) })" +  
			"  (rules " + 
			"    (play \"step\")" +
            "    (end (line length:3) (result Mover Win))"+  
            "  )"+         
            ")";
	
			private final String TTTBadDefine2 = 
			"(game \"Tic-Tac-Toe\"" + 
			"  (mode 2)" +  
			"  (define \"step\" (step (in (to) (empty)))" +
			"  (equipment { (board (square 3) (square)) (disc P1) (cross P2) })" +  
			"  (rules " + 
			"    (play \"step\")" +
            "    (end (line length:3) (result Mover Win))"+  
            "  )"+         
            ")";

			private final String TTTBadDefine3 = 
			"(game \"Tic-Tac-Toe\"" + 
			"  (mode 2)" +  
			"  (define \"step\" (step (in (to) (empty)))))" +
			"  (equipment { (board (square 3) (square)) (disc P1) (cross P2) })" +  
			"  (rules " + 
			"    (play \"step\")" +
            "    (end (line length:3) (result Mover Win))"+  
            "  )"+         
            ")";

			private final String TTTBadBrackets1 = 
			"(game \"Tic-Tac-Toe\"" + 
			"  (mode 2)" +  
			"  (equipment { (board (square 3) (square)) (disc P1) (cross P2) })" +  
			"  (rules " + 
			"    (play (to (mover) (empty)})" +
            "    (end (line length:3) (result Mover Win))"+  
            "  )"+         
            ")";
	
			private final String TTTURLInString = 
			"(game \"Tic-Tac-Toe (see http://www.bgg.com)\"" + 
			"  (mode 2) " +  
			"  (equipment { (board (square 3) (square)) (disc P1) (cross P2) })" +  
			"  (rules " + 
			"    (play (to (mover) (empty)))" +
            "    (end (line length:3) (result Mover Win))"+  
            "  )"+         
            ")";

			private final String TTTBadPieceName = 
			"(game \"Tic-Tac-Toe\"" + 
			"  (mode 2)" +  
			"  (equipment { (board (square 3) (square)) (disc P1) (cross P2) })" +  
			"  (rules " + 
			"    (start (place \"DiscWithBadName\" 0))" +
			"    (play (to (mover) (empty)))" +
            "    (end (line length:3) (result Mover Win))"+  
            "  )"+         
            ")";

	//-------------------------------------------------------------------------
	
	@Test
	public void testGoodFile()
	{
		verifyDoesCompile(TTT);
	}

	@Test
	public void testMissingKeyword()
	{
		verifyCompileFails(TTTMissingKeyword);
	}

	@Test
	public void testCantDecompose()
	{
		verifyCompileFails(TTTCantDecompose);
	}

	@Test
	public void testBadTerminal()
	{
		verifyCompileFails(TTTBadTerminal);
	}

	@Test
	public void testMissingQuote()
	{
		verifyCompileFails(TTTMissingQuote);
	}
	
	@Test
	public void testBadArray()
	{
		verifyCompileFails(TTTBadArray1);
	}

	@Test
	public void testRecursiveDefines()
	{
		verifyCompileFails(TTT_RECURSIVE_DEFINES);
	}
	
	@Test
	public void testRange1()
	{
		verifyCompileFails(TTTBadRange1);
	}
	
	@Test
	public void testRange2()
	{
		verifyCompileFails(TTTBadRange2);
	}
	
	@Test
	public void testRecursiveOption()
	{
		verifyCompileFails(TTTRecursiveOption);
	}
	
	@Test
	public void testBadDefine1()
	{
		verifyCompileFails(TTTBadDefine1);
	}
	
	@Test
	public void testBadDefine2()
	{
		verifyCompileFails(TTTBadDefine2);
	}
	
	@Test
	public void testBadDefine3()
	{
		verifyCompileFails(TTTBadDefine3);
	}
	
	@Test
	public void testBadBrackets1()
	{
		verifyCompileFails(TTTBadBrackets1);
	}
	
	@Test
	public void testURLInString()
	{
		verifyDoesCompile(TTTURLInString);
	}
	
	@Test
	public void testBadPieceName()
	{
		verifyCompilesButFailsToRun(TTTBadPieceName);
	}
	
	//-------------------------------------------------------------------------
	
	private static void verifyCompileFails(final String rules)
	{
		try
		{
			final Game game = (Game)Compiler.compileTest(new Description(rules), true);
			if (game == null) 
				System.out.println("Expected a game but got null");
			Assert.fail("An exception should have been thrown.");
		}
		catch (final Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	private static void verifyDoesCompile(final String rules)
	{
		final Game game = (Game)Compiler.compileTest(new Description(rules), true);
		if (game == null) 
			Assert.fail("Game should not be null.");
	}

//	private static void verifyCompilesButFailsToInitialise(final String rules)
//	{
//		final Game game = Compiler.getCompiler().compileTest(rules, true);
//		if (game == null) 
//			Assert.fail("Game should not be null.");
//		try
//		{
//			game.create(800);
//		}
//		catch (final Exception e)
//		{
//			e.printStackTrace();
//			System.out.println(e.getMessage());
//		}
//	}

	private static void verifyCompilesButFailsToRun(final String rules)
	{
		final Game game = (Game)Compiler.compileTest(new Description(rules), true);
		if (game == null) 
			Assert.fail("Game should not be null.");

		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);

		try
		{
			game.start(context);
			game.playout(context, null, 1.0, null, 0, -1, ThreadLocalRandom.current());		
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}
	
//	@Test
//	public void testBadArray()
//	{
//		try
//		{
//			Compiler.getCompiler().compile(TTTBadArray1, false, false);
//			Assert.fail("An exception should have been thrown.");
//		}
//		catch (Exception e)
//		{
//			System.out.println(e.getMessage());
//		}
//	}

	//-------------------------------------------------------------------------

}
