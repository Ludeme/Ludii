package travis.quickTests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import main.collections.FVector;

/**
 * Unit tests for FVector
 * 
 * @author Dennis Soemers
 */
public class FVectorTests 
{
	
	private static final float FLOAT_TOLERANCE = 0.0001f;
	
	@Test
	public void testLinspaceInclusive()
	{
		final FVector linspace = FVector.linspace(0.f, 1.f, 4, true);
		assertEquals(linspace.dim(), 4);
		assertEquals(linspace.get(0), 0.f / 3.f, FLOAT_TOLERANCE);
		assertEquals(linspace.get(1), 1.f / 3.f, FLOAT_TOLERANCE);
		assertEquals(linspace.get(2), 2.f / 3.f, FLOAT_TOLERANCE);
		assertEquals(linspace.get(3), 3.f / 3.f, FLOAT_TOLERANCE);
	}
	
	@Test
	public void testLinspaceExclusive()
	{
		final FVector linspace = FVector.linspace(0.f, 1.f, 4, false);
		assertEquals(linspace.dim(), 4);
		assertEquals(linspace.get(0), 0.f / 4.f, FLOAT_TOLERANCE);
		assertEquals(linspace.get(1), 1.f / 4.f, FLOAT_TOLERANCE);
		assertEquals(linspace.get(2), 2.f / 4.f, FLOAT_TOLERANCE);
		assertEquals(linspace.get(3), 3.f / 4.f, FLOAT_TOLERANCE);
	}

}
