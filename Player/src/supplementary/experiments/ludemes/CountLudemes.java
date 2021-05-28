package supplementary.experiments.ludemes;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import grammar.ClassEnumerator;

/**
 * Counts ludemes by category.
 * 
 * @author cambolbro (based on code by Dennis Soemers).
 */
public class CountLudemes
{	
	/**
	 * Record of a ludeme class category and number of times it occurs.
	 */
	class Record
	{
		private final String label;
		private final Class<?> category;
		private int count = 0;

		//-------------------------------------

		public Record(final String label, final Class<?> category)
		{
			this.label = label;
			this.category = category;
		}

		//-------------------------------------
		
		public String label()
		{
			return label;
		}

		public Class<?> category()
		{
			return category;
		}

		public int count()
		{
			return count;
		}

		public void reset()
		{
			count = 0;
		}

		public void increment()
		{
			count++;
		}

	}
	
	//---------------------------------------------------------
	
	private final List<Record> records = new ArrayList<Record>();
	
	private String result = "No count yet.";
	
	//-------------------------------------------------------------------------

	public CountLudemes()
	{
		prepareCategories();
		countLudemes();
	}
	
	//-------------------------------------------------------------------------

	public String result()
	{
		return result;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Add ludemes categories to be counted here.
	 */
	void prepareCategories()
	{
		records.clear();
		
		try
		{	
			records.add(new Record("Ludeme classes",      Class.forName("util.Ludeme")));
			records.add(new Record("Integer functions",   Class.forName("game.functions.ints.BaseIntFunction")));
			records.add(new Record("Boolean functions",   Class.forName("game.functions.booleans.BaseBooleanFunction")));	
			records.add(new Record("Region functions",    Class.forName("game.functions.region.RegionFunction")));	
			records.add(new Record("Equipment (total)",   Class.forName("game.equipment.Item")));
			records.add(new Record("Containers",          Class.forName("game.equipment.container.Container")));
			records.add(new Record("Components",          Class.forName("game.equipment.component.Component")));
			records.add(new Record("Start rules",         Class.forName("game.rules.start.StartRule")));
			records.add(new Record("Moves rules",         Class.forName("game.rules.play.moves.Moves")));
			records.add(new Record("End rules",           Class.forName("game.rules.end.End")));		
			//records.add(new Record("Game type modifiers", Class.forName("game.types.GameType")));						
			//records.add(new Record("", Class.forName("game.")));
		}
		catch (final ClassNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Count ludemes by category.
	 * @return String describing ludeme counts by category.
	 */
	public String countLudemes()
	{
		final String rootPackage = "game.Game";
		
		for (final Record record : records)
			record.reset();
		
		// Get all classes from root package down
		Class<?> clsRoot = null;
		try
		{
			clsRoot = Class.forName(rootPackage);
		} 
		catch (final ClassNotFoundException e)
		{
			e.printStackTrace();
			return "Couldn't find root package \"game\".";
		}
		final List<Class<?>> classes = ClassEnumerator.getClassesForPackage(clsRoot.getPackage());
		
		// Get the reference Ludeme class
		Class<?> clsLudeme = null;
		try
		{
			clsLudeme = Class.forName("util.Ludeme");
		}
		catch (final ClassNotFoundException e)
		{
			e.printStackTrace();
		}

		// Count ludemes in each category
		int numEnums = 0;
		int numEnumConstants = 0;
		for (final Class<?> cls : classes)
		{
			for (final Record record : records)
			{
				if 
				(
					clsLudeme.isAssignableFrom(cls)
					&&
					record.category().isAssignableFrom(cls)
					&&
					!Modifier.isAbstract(cls.getModifiers())
					&&
					!cls.getName().contains("$")
				)
				{
					// Is a ludeme class
					record.increment();
				}
			}
			
			if (cls.isEnum())
			{
				// Is an enum class
				numEnums++;
				numEnumConstants += cls.getEnumConstants().length;
			}
		}
		
		// Format the result
		final StringBuilder sb = new StringBuilder();
		//sb.append(classes.size() + " classes found from \"" + rootPackage + "\" root package.\n");
		
		sb.append("\n");
		
		for (final Record record : records)
			sb.append(record.label() + ": " + record.count() + "\n");
		
		sb.append("Enum classes: " + numEnums + "\n");
		sb.append("Enum constants: " + numEnumConstants + "\n");
		
		sb.append("\n");
		
		result = sb.toString();
		return result;
	}

	//-------------------------------------------------------------------------

	
}
