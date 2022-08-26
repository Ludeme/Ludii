package concepts;

import static org.junit.Assert.fail;

import org.junit.Test;

import other.concept.Concept;

/**
 * Test for the huge concept enum in order to check taxo and child/parent.
 * 
 * @author Eric.Piette
 */
public class ConceptTests
{
	@Test
	@SuppressWarnings("static-method")
	public void ConceptLeaf()
	{
		for (final Concept concept : Concept.values())
		{
			final boolean leaf = concept.isleaf();
			if (leaf)
			{
				final String taxo = concept.taxonomy();
				final Concept parentConcept = concept.parent();

				if (parentConcept != null)
				{
					final String parentTaxo = parentConcept.taxonomy();
					final String parentShouldBe = taxo.substring(0, taxo.lastIndexOf('.'));
					if (!parentTaxo.equals(parentShouldBe))
					{
						System.err.println("Something incorrect about parent of " + concept.name());
						fail();
					}
				}
			}
			else
			{
				boolean childFound = false;
				for (final Concept child : Concept.values())
				{
					if (child.parent() != null)
					{
						if (child.parent().equals(concept))
						{
							childFound = true;
							break;
						}
					}
				}

				if (!childFound)
				{
					System.err.println("No Child for no leaf concept = " + concept.name());
					fail();
				}
			}
		}
	}

}

