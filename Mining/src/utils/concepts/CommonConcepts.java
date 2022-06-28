package utils.concepts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import game.Game;
import gnu.trove.list.array.TIntArrayList;
import compiler.Compiler;
import main.FileHandling;
import main.grammar.Description;
import other.concept.Concept;
import other.concept.ConceptDataType;
import other.concept.ConceptType;

/**
 * Method to get the common concepts (and avg for the numerical ones) between a
 * set of games and avg of same value/diff value for each boolean concept.
 * 
 * @author Eric.Piette
 */
public class CommonConcepts
{

	/** The games to compare. */
	private final static String[] gamesToCompare = new String[]
	{ "Go", "Oware" };

	/** The list of compiled games. */
	final static List<Game> games = new ArrayList<Game>();

	/** Put a value here to get only one single concept type. */
	final static ConceptType type = null;

	//---------------------------------------------------------------------
	
	/**
	 * Main method.
	 * 
	 * @param args
	 */
	public static void main(final String[] args) throws IllegalArgumentException, IllegalAccessException
	{
		final TIntArrayList booleanConceptsID = new TIntArrayList();
		final List<String> booleanConceptsName = new ArrayList<String>();
		final TIntArrayList nonBooleanConceptsID = new TIntArrayList();
		final List<String> nonBooleanConceptsName = new ArrayList<String>();
		
		// We get the concepts.
		for (final Concept concept : Concept.values())
			if (concept.dataType().equals(ConceptDataType.BooleanData))
			{
				if (type == null || concept.type().equals(type))
				{
					booleanConceptsID.add(concept.id());
					booleanConceptsName.add(concept.name());
				}
			}
			else if (!concept.dataType().equals(ConceptDataType.StringData))
			{
				nonBooleanConceptsID.add(concept.id());
				nonBooleanConceptsName.add(concept.name());
			}

		getGames();

		final int totalBooleanConcept = booleanConceptsID.size();
		
		// Check the number of times all the games have the same value for the concepts and the number of times they have a different value (only for boolean).
		int sameValue = 0;
		int differentValue = 0;
		for(int i = booleanConceptsID.size()-1; i>=0; i--)
		{
			final int idConcept = booleanConceptsID.get(i);
			final boolean hasConcept = games.get(0).booleanConcepts().get(idConcept);
			boolean allSameValue = true;
			for (int j = 1 ; j < games.size(); j++)
			{
				final Game game = games.get(j);
				if ((game.booleanConcepts().get(idConcept) && !hasConcept) || !game.booleanConcepts().get(idConcept) && hasConcept)
				{
					differentValue++;
					allSameValue = false;
					break;
				}
				if(allSameValue)
					sameValue++;
			}
		}
		
		// Keep Only the common boolean concepts.
		for(int i = booleanConceptsID.size()-1; i>=0; i--)
		{
			final int idConcept = booleanConceptsID.get(i);
			for (final Game game : games)
			{
				if (!game.booleanConcepts().get(idConcept))
				{
					booleanConceptsID.removeAt(i);
					booleanConceptsName.remove(i);
					break;
				}
			}
		}

		System.out.println("Common Boolean Concepts: \n");

		for (int i = 0; i < booleanConceptsName.size(); i++)
			System.out.println(booleanConceptsName.get(i));
		
		System.out.println("\nAVG Boolean Concepts with same value and AVG Boolean with different values: \n");
		System.out.println("Same Value = " + new DecimalFormat("##.##")
				.format((((double) sameValue / (double) totalBooleanConcept)) * 100) + " %");
		System.out.println("different Value = " + new DecimalFormat("##.##")
				.format((((double) differentValue / (double) totalBooleanConcept)) * 100) + " %");
		

		System.out.println("\nAvg Numerical Concepts:\n");

		// We export the non boolean concepts.
		for (int i = 0; i < nonBooleanConceptsID.size(); i++)
		{
			final Integer idConcept = Integer.valueOf(nonBooleanConceptsID.get(i));
			final String Conceptname = nonBooleanConceptsName.get(i);
			double sum = 0.0;
			
			for(final Game game: games)
				sum += Double.parseDouble(game.nonBooleanConcepts().get(idConcept));
			
			System.out.println(Conceptname + ": " + (sum / games.size()));
		}
	}

	//---------------------------------------------------------------------
	
	/** Get the compiled games. */
	public static void getGames()
	{
		final File startFolder = new File("../Common/res/lud");
		final List<File> gameDirs = new ArrayList<File>();
		gameDirs.add(startFolder);
		final List<File> entries = new ArrayList<File>();
		final String moreSpecificFolder = "";

		for (int i = 0; i < gameDirs.size(); ++i)
		{
			final File gameDir = gameDirs.get(i);

			for (final File fileEntry : gameDir.listFiles())
			{
				if (fileEntry.isDirectory())
				{
					final String fileEntryPath = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");

					if (fileEntryPath.equals("../Common/res/lud/plex"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/wip"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/wishlist"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/WishlistDLP"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/test"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/puzzle/deduction"))
						continue; // skip deduction puzzles

					if (fileEntryPath.equals("../Common/res/lud/bad"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/bad_playout"))
						continue;

					// We exclude that game from the tests because the legal
					// moves are too slow to test.
					if (fileEntryPath.contains("Residuel"))
						continue;

					gameDirs.add(fileEntry);
				}
				else
				{
					final String fileEntryPath = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");
					if (moreSpecificFolder.equals("") || fileEntryPath.contains(moreSpecificFolder))
						entries.add(fileEntry);
				}
			}
		}
		
		for (final File fileEntry : entries)
		{
			final String gameName = fileEntry.getName();
			boolean found = false;
			for (final String name : gamesToCompare)
			{
				if (gameName.equals(name + ".lud"))
				{
					found = true;
					break;
				}
			}
			if (!found)
				continue;

			final String ludPath = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");
			String desc = "";
			try
			{
				desc = FileHandling.loadTextContentsFromFile(ludPath);
			}
			catch (final FileNotFoundException ex)
			{
				throw new RuntimeException("Unable to open file '" + ludPath + "'", ex);
			}
			catch (final IOException ex)
			{
				throw new RuntimeException("Error reading file '" + ludPath + "'", ex);
			}

			// Parse and compile the game
			final Game game = (Game)Compiler.compileTest(new Description(desc), false);
			if (game == null)
				throw new RuntimeException("COMPILATION FAILED for the file : " + ludPath);
			else
				games.add(game);

		}
	}

}
