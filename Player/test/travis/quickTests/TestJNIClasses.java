package travis.quickTests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Unit test to make sure that the fully qualified names of classes that we
 * access by those exact names through JNI (for example in Polygames) don't
 * change.
 *
 * @author Dennis Soemers
 */
public class TestJNIClasses
{
	
	@Test
	public static void testFullyQualifiedNames()
	{
		try
		{
			assertNotNull(Class.forName("utils.LudiiGameWrapper"));
			assertNotNull(Class.forName("utils.LudiiStateWrapper"));
		}
		catch (final ClassNotFoundException e)
		{
			fail();
		}
	}

}
