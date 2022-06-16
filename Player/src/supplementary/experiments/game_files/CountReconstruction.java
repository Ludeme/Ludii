package supplementary.experiments.game_files;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import main.FileHandling;

/**
 * Simple "experiment" to count the number of reconstruction we have in every
 * category. 
 * 
 * @author Eric Piette
 */
public class CountReconstruction
{
	/** Num spaces per indentation level for printing */
	private static final int NUM_INDENT_SPACES = 4;

	/**
	 * Main method
	 * @param args
	 */
	public static void main(final String[] args)
	{		
		final String[] allGames = FileHandling.listGames();
		final Category rootCategory = new Category("All Games to reconstruct");
		
		int widestStringLength = 0;
		
		for (String game : allGames)
		{
			if(!game.contains("reconstruction"))
				continue;
			
			game = game.replaceAll(Pattern.quote("\\"), "/");
			final String[] gameParts = game.split(Pattern.quote("/"));
			
			// categories which we've traversed so far
			final List<Category> traversedCategories = new ArrayList<Category>();
			
			// we start in the "All Games" category
			Category currentCategory = rootCategory;
			int currentIndentationLevel = 0;
			traversedCategories.add(currentCategory);
			
			for (int i = 2; i < gameParts.length; ++i)
			{
				final String part = gameParts[i];
				
				if (part.endsWith(".lud"))
				{
					// we've found the game, increment counts for all traversed categories
					for (final Category category : traversedCategories)
					{
						category.numGames += 1;
					}
				}
				else if 
				(
					part.equals("bad") ||
					part.equals("bad_playout") ||
							part.equals("wip") || part.equals("wishlist") || part.equals("WishlistDLP")
						||
							part.equals("test") || part.equals("subgame") || part.equals("proprietary")
				)
				{
					// should skip this game
					break;
				}
				else
				{
					// this is a subcategory, see if we already have it
					Category subcategory = null;
					for (final Category sub : currentCategory.subcategories)
					{
						if (sub.name.equals(part))
						{
							// found it
							subcategory = sub;
							break;
						}
					}
					
					currentIndentationLevel += 1;
					
					if (subcategory == null)
					{
						// need to create new subcategory
						subcategory = new Category(part);
						currentCategory.subcategories.add(subcategory);
						
						widestStringLength = 
								Math.max
								(
									widestStringLength, 
									currentIndentationLevel * NUM_INDENT_SPACES + part.length()
								);
					}
					
					traversedCategories.add(subcategory);
					currentCategory = subcategory;
				}
			}
		}
		
		// start printing
		for (int i = 0; i < widestStringLength + Math.min(4, NUM_INDENT_SPACES) + 4; ++i)
		{
			System.out.print("=");
		}
		System.out.println();
		System.out.println("Number of .lud files per category:");
		System.out.println("");
		
		printCategory(rootCategory, 0, widestStringLength);
		
		for (int i = 0; i < widestStringLength + Math.min(4, NUM_INDENT_SPACES) + 4; ++i)
		{
			System.out.print("=");
		}
		System.out.println();
	}
	
	/**
	 * Helper method to print a categories recursively
	 * 
	 * @param category
	 * @param indentation
	 * @param widestStringLength
	 * @return Number of games in this category
	 */
	private static int printCategory(final Category category, final int indentation, final int widestStringLength)
	{
		// indent before category name
		for (int i = 0; i < indentation; ++i)
		{
			for (int j = 0; j < NUM_INDENT_SPACES; ++j)
			{
				System.out.print(" ");
			}
		}
		
		// print category name
		System.out.print(category.name + ":");
		
		// append extra indentation to accomodate widest category string
		final int numCharactersPrinted = indentation * NUM_INDENT_SPACES + category.name.length() + 1;
		final int numCharactersWanted = widestStringLength + Math.min(4, NUM_INDENT_SPACES);
		
		for (int i = 0; i < (numCharactersWanted - numCharactersPrinted); ++i)
		{
			System.out.print(" ");
		}
		
		// finally print the count
		System.out.print(String.format("%4s", Integer.valueOf(category.numGames)));
		
		// complete the line
		System.out.println();
		
		// print subcategories
		int accum = 0;
		for (final Category subcategory : category.subcategories)
		{
			accum += printCategory(subcategory, indentation + 1, widestStringLength);
		}
		
		if (accum < category.numGames && category.subcategories.size() > 0)
		{
			final int numOther = category.numGames - accum;
			final Category fakeCategory = new Category("other");
			fakeCategory.numGames = numOther;
			printCategory(fakeCategory, indentation + 1, widestStringLength);
		}
		
		return category.numGames;
	}
	
	/**
	 * Helper class for a category of games
	 * 
	 * @author Dennis Soemers
	 */
	private static class Category
	{
		
		/** Category name */
		protected final String name;
		
		/** Number of games in this category (and subcategories) */
		protected int numGames = 0;
		
		/** List of subcategories */
		protected List<Category> subcategories = new ArrayList<Category>();
		
		/**
		 * Constructor
		 * @param name
		 */
		public Category(final String name)
		{
			this.name = name;
		}
		
	}
	
}
