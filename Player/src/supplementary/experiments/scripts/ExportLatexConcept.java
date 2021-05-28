package supplementary.experiments.scripts;

import other.concept.Concept;

/**
 * To print the concepts in the taxonomy latex format.
 * 
 * @author Eric.Piette
 *
 */
public class ExportLatexConcept
{
	/**
	 * The main method.
	 * 
	 * @param args
	 */
	public static void main(final String[] args)
	{	
		final Concept[] concepts = Concept.values();
		final StringBuffer results = new StringBuffer();
		
		for(final Concept concept : concepts)
		{
			results.append(numIndent(concept.taxonomy()) + concept.taxonomy() + " "
					+ getLatexName(concept.name(), concept.isleaf()) + ": "
					+ concept.description() + "\n \n");
		}
		
		System.out.println(results.toString());
	}

	/**
	 * @param taxo The taxonomy.
	 * @return The number of necessary indentation in latex for this concept.
	 */
	public static String numIndent(final String taxo)
	{
		int numIndent = 0;
		for (int i = 1; i < taxo.length(); i++)
		{
			final char c = taxo.charAt(i);
			if (c != '.')
				numIndent++;
		}

		final StringBuffer indentation = new StringBuffer();

		for (int i = 0; i < numIndent; i++)
		{
			indentation.append("\\tb" + " ");
		}

		return indentation.toString();

	}

	/**
	 * @param name The name of the concept.
	 * @param isLeaf True if the concept is a leaf.
	 * @return The name with space before each upper case.
	 */
	public static String getLatexName(final String name, final boolean isLeaf)
	{
		final StringBuffer nameToPrint = new StringBuffer();
		for (int i = 0; i < name.length(); i++)
		{
			final char c = name.charAt(i);
			if (Character.isLowerCase(c) || i == 0)
				nameToPrint.append(c);
			else
				nameToPrint.append(" " + c);
		}

		if(isLeaf)
			return nameToPrint.toString();
		else
			return "\\textbf{" + nameToPrint.toString() + "}";
	}

}
