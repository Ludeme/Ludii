package reconstruction.output;

import java.io.BufferedReader;
import java.io.FileReader;

import gnu.trove.list.array.TIntArrayList;

/**
 * Update the GameRulesets Table with the outcome rulesets from the reconstruction process.
 * @author Eric.Piette
 */
public class UpdateGameRulesetsTable
{
	// Load ruleset avg common true concepts from specific directory.
	final static String gameRulesetsFilePath = "./res/recons/input/GameRulesets.csv";
	
	public static void main(final String[] args)
	{
		final int maxId = getMaxId();
		System.out.println(maxId);
	}
	
	/**
	 * @return the max id of the rulesets
	 */
	private static int getMaxId()
	{
		// ids of the rulesets
		final TIntArrayList ids = new TIntArrayList();	
		
		try (BufferedReader br = new BufferedReader(new FileReader(gameRulesetsFilePath))) 
		{
		    String line;	// column names
		    while ((line = br.readLine()) != null) 
		    {
		    	if(line.length() > 2 &&  line.charAt(0) == '"' && Character.isDigit(line.charAt(1)))
		    	{
		    		final String subLine = line.substring(1);
		    		int i = 0;
		    		char c = subLine.charAt(i);
		    		while(c != '"')
		    		{
		    			i++;
		    			c = subLine.charAt(i);
		    		}
		    		ids.add(Integer.parseInt(subLine.substring(0,i)));
		    	}
		    }
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
		return ids.max();
	}
}
