package travis.quickTests;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import gnu.trove.list.array.TIntArrayList;
import other.concept.Concept;
import other.concept.ConceptComputationType;
import other.concept.ConceptDataType;
import other.concept.ConceptPurpose;
import other.concept.ConceptType;

/**
 * Check if all the ids are different for all the enum class related to the
 * concepts (for the db).
 * 
 * @author Eric.Piette
 *
 */
@SuppressWarnings("static-method")
public class AllDifferentIdConceptEnum
{

	@Test
	public void Concept()
	{
		final TIntArrayList ids = new TIntArrayList();
		for (final Concept concept : Concept.values())
		{
			if (ids.contains(concept.id()))
			{
				System.err.println("The id " + concept.id() + " is used twice for the Concept enum class.");
				fail();
			}
			else
				ids.add(concept.id());
		}
	}

	@Test
	public void ConceptTaxo()
	{
		final List<String> taxos = new ArrayList<String>();
		for (final Concept concept : Concept.values())
		{
			if (taxos.contains(concept.taxonomy()))
			{
				System.err.println("The taxo " + concept.taxonomy() + " is used twice for the Concept enum class.");
				fail();
			}
			else
				taxos.add(concept.taxonomy());
		}
	}

	@Test
	public void ConceptDataType()
	{
		final TIntArrayList ids = new TIntArrayList();
		for (final ConceptDataType conceptDataType : ConceptDataType.values())
		{
			if (ids.contains(conceptDataType.id()))
			{
				System.err.println(
						"The id " + conceptDataType.id() + " is used twice for the ConceptDataType enum class.");
				fail();
			}
			else
				ids.add(conceptDataType.id());
		}
	}

	@Test
	public void ConceptKeyword()
	{
		final TIntArrayList ids = new TIntArrayList();
		for (final Concept conceptKeyword : Concept.values())
		{
			if (ids.contains(conceptKeyword.id()))
			{
				System.err
						.println("The id " + conceptKeyword.id() + " is used twice for the ConceptKeyword enum class.");
				fail();
			}
			else
				ids.add(conceptKeyword.id());
		}
	}

	@Test
	public void ConceptPurpose()
	{
		final TIntArrayList ids = new TIntArrayList();
		for (final ConceptPurpose conceptPurpose : ConceptPurpose.values())
		{
			if (ids.contains(conceptPurpose.id()))
			{
				System.err
						.println("The id " + conceptPurpose.id() + " is used twice for the ConceptPurpose enum class.");
				fail();
			}
			else
				ids.add(conceptPurpose.id());
		}
	}

	@Test
	public void ConceptType()
	{
		final TIntArrayList ids = new TIntArrayList();
		for (final ConceptType conceptType : ConceptType.values())
		{
			if (ids.contains(conceptType.id()))
			{
				System.err.println("The id " + conceptType.id() + " is used twice for the ConceptType enum class.");
				fail();
			}
			else
				ids.add(conceptType.id());
		}
	}

	@Test
	public void ConceptComputationType()
	{
		final TIntArrayList ids = new TIntArrayList();
		for (final ConceptComputationType conceptComputationType : ConceptComputationType.values())
		{
			if (ids.contains(conceptComputationType.id()))
			{
				System.err.println("The id " + conceptComputationType.id() + " is used twice for the ConceptComputationType enum class.");
				fail();
			}
			else
				ids.add(conceptComputationType.id());
		}
	}
	
}
