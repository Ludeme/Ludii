package test;

//import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Map;

//import org.junit.jupiter.api.Test;

import parser.Completer;

//-----------------------------------------------------------------------------

/**
 * Test various reconstruction routines.
 * @author cambolbro
 */
class TestReconstruction
{
	
	//-------------------------------------------------------------------------

	void testSaving()
	{
		try
		{
			Completer.saveReconstruction("Test", "(test ...)");
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
		
	//-------------------------------------------------------------------------

	void testLoadLuds()
	{
		final Map<String, String> luds = Completer.getAllLudContents();
		final Map<String, String> defs = Completer.getAllDefContents();
		
		System.out.println(luds.size() + " luds loaded, " + defs.size() + " defs loaded.");
	}
	
	//-------------------------------------------------------------------------

//	@Test
//	public void test()
//	{
//		testSaving();
//		testLoadLuds();
//	}

	public static void main(String[] args)
	{
		final TestReconstruction app = new TestReconstruction();
		
		app.testSaving();
		app.testLoadLuds();
	}
	
}
