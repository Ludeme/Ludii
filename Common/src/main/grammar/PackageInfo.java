package main.grammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//-----------------------------------------------------------------------------

/**
 * Java package in class hierarchy.
 * @author cambolbro
 */
public class PackageInfo 
{
	protected String path = "";
	protected List<GrammarRule> rules = new ArrayList<GrammarRule>();

	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 * @param path
	 */
	public PackageInfo(final String path) 
	{
		this.path = path;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Package name.
	 */
	public String path() 
	{
		return path;
	}

	/**
	 * @return Final term of package name.
	 */
	public String shortName() 
	{
		final String[] subs = path.split("\\.");
		if (subs.length == 0)
			return path;
		return subs[subs.length - 1];
	}

	/**
	 * @return Rules in this package.
	 */
	public List<GrammarRule> rules() 
	{
		return Collections.unmodifiableList(rules);
	}

	//-------------------------------------------------------------------------

	public void add(final GrammarRule rule)
	{
		rules.add(rule);
	}
	
	public void add(final int n, final GrammarRule rule)
	{
		rules.add(n, rule);
	}
	
	public void remove(final int n)
	{
		rules.remove(n);
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Order rules within package alphabetically.
	 */
	public void listAlphabetically()
	{
		Collections.sort(rules, new Comparator<GrammarRule>() 
		{
			@Override
            public int compare(final GrammarRule a, final GrammarRule b) 
            {
//            	return a.lhs().name().compareTo(b.lhs().name());
            	return a.lhs().grammarLabel().compareTo(b.lhs().grammarLabel());
            }
        });
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		String str = "";
		
		str += "//";
		while (str.length() < GrammarRule.MAX_LINE_WIDTH)  // shorten so that prints neatly
			str += "-";
		str += "\n";

		str += "// " + path + "\n\n";

		int numUsed = 0;
		
		for (GrammarRule rule : rules) 
		{
//			if (rule.remove())
//				continue;
			if 
			(
				!rule.lhs().usedInGrammar() 
				&& 
				!rule.lhs().usedInDescription() 
				&& 
				!rule.lhs().usedInMetadata()
			)
				continue;

			if (rule.rhs() == null || rule.rhs().isEmpty())
				continue;

			str += rule.toString() + "\n";
			numUsed++;
		}
		str += "\n";

		if (numUsed == 0)
			return "";  // no rules -- ignore this package 
				
		return str;
	}

	//-------------------------------------------------------------------------

}
