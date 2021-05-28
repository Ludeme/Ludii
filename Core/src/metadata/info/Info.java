package metadata.info;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import annotations.Or;
import metadata.MetadataItem;
import metadata.info.database.Aliases;
import metadata.info.database.Author;
import metadata.info.database.Classification;
import metadata.info.database.Credit;
import metadata.info.database.Date;
import metadata.info.database.Description;
import metadata.info.database.Origin;
import metadata.info.database.Publisher;
import metadata.info.database.Rules;
import metadata.info.database.Source;
import metadata.info.database.Version;

/**
 * General information about the game.
 * 
 * @author Matthew.Stephenson and cambolbro
 */
public class Info implements MetadataItem, Serializable
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	final List<InfoItem> items = new ArrayList<InfoItem>();
	
	//-------------------------------------------------------------------------

	/**
	 * @param item  The info item of the game.
	 * @param items The info items of the game.
	 * 
	 * @example (info { (description "Description of The game") (source "Source of
	 *          the game") (version "1.0.0") (classification
	 *          "board/space/territory") (origin "Origin of the game.") })
	 */
	public Info
	(
		@Or	final InfoItem item,
		@Or final InfoItem[] items
	)
	{
		int numNonNull = 0;
		if (item != null)
			numNonNull++;
		if (items != null)
			numNonNull++;

		if (numNonNull > 1)
			throw new IllegalArgumentException("Only one of @Or should be different to null");

		if(items != null)
			for (final InfoItem i : items)
				this.items.add(i);
		else
			this.items.add(item);
	}

	//-------------------------------------------------------------------------

	/**
	 * Add an element to the map.
	 * 
	 * @param map The map.
	 */
	public void addToMap(final Map<String, MetadataItem> map)
	{
		for (final InfoItem item : items)
			map.put(item.getClass().getSimpleName(), item);
	}

	// -------------------------------------------------------------------------

	/**
	 * 
	 * @return All the items.
	 */
	public List<InfoItem> getItem()
	{
		return Collections.unmodifiableList(items);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The Source of the game's rules.
	 */
	public ArrayList<String> getSource()
	{
		final ArrayList<String> sources = new ArrayList<>();
		for (final InfoItem infoItem : items)
			if (infoItem instanceof Source)
				sources.add(((Source) infoItem).source());
		return sources;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The English description of the rules.
	 */
	public ArrayList<String> getRules()
	{
		final ArrayList<String> rules = new ArrayList<>();
		for (final InfoItem infoItem : items)
			if (infoItem instanceof Rules)
				rules.add(((Rules) infoItem).rules());
		return rules;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The author of the game.
	 */
	public ArrayList<String> getAuthor()
	{
		final ArrayList<String> authors = new ArrayList<>();
		for (final InfoItem infoItem : items)
			if (infoItem instanceof Author)
				authors.add(((Author) infoItem).author());
		return authors;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The date that the game was created.
	 */
	public ArrayList<String> getDate()
	{
		final ArrayList<String> dates = new ArrayList<>();
		for (final InfoItem infoItem : items)
			if (infoItem instanceof Date)
				dates.add(((Date) infoItem).date());
		return dates;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The publisher of the game.
	 */
	public ArrayList<String> getPublisher()
	{
		final ArrayList<String> publishers = new ArrayList<>();
		for (final InfoItem infoItem : items)
			if (infoItem instanceof Publisher)
				publishers.add(((Publisher) infoItem).publisher());
		return publishers;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The credit of the game.
	 */
	public ArrayList<String> getCredit()
	{
		final ArrayList<String> credits = new ArrayList<>();
		for (final InfoItem infoItem : items)
			if (infoItem instanceof Credit)
				credits.add(((Credit) infoItem).credit());
		return credits;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The English description of the game.
	 */
	public ArrayList<String> getDescription()
	{
		final ArrayList<String> descriptions = new ArrayList<>();
		for (final InfoItem infoItem : items)
			if (infoItem instanceof Description)
				descriptions.add(((Description) infoItem).description());
		return descriptions;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The Origin of the game.
	 */
	public ArrayList<String> getOrigin()
	{
		final ArrayList<String> origins = new ArrayList<>();
		for (final InfoItem infoItem : items)
			if (infoItem instanceof Origin)
				origins.add(((Origin) infoItem).origin());
		return origins;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The Classification of the game.
	 */
	public ArrayList<String> getClassification()
	{
		final ArrayList<String> classifications = new ArrayList<>();
		for (final InfoItem infoItem : items)
			if (infoItem instanceof Classification)
				classifications.add(((Classification) infoItem).classification());
		return classifications;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The Version of the game.
	 */
	public ArrayList<String> getVersion()
	{
		final ArrayList<String> versions = new ArrayList<>();
		for (final InfoItem infoItem : items)
			if (infoItem instanceof Version)
				versions.add(((Version) infoItem).version());
		return versions;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The aliases of the game.
	 */
	public String[] getAliases()
	{
		for (final InfoItem infoItem : items)
			if (infoItem instanceof Aliases)
				return ((Aliases) infoItem).aliases();
		return new String[0];
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		final String open  = (items.size() <= 1) ? "" : "{";
		final String close = (items.size() <= 1) ? "" : "}";
		
		sb.append("    (info " + open + "\n");
		for (final InfoItem item : items)
			if (item != null)
				sb.append("        " + item.toString());
		sb.append("    " + close + ")\n");

		return sb.toString();
	}

	//-------------------------------------------------------------------------

}
