import approaches.random.Generator;
import org.junit.Test;

/**
 * Random game tester.
 * @author cambolbro
 */
public class RandomGameTester
{
    @Test
	public void testGames()
	{
		Generator.testGames(1000, true, false, false, false);
	}
}
