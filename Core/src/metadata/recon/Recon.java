package metadata.recon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import annotations.Or;
import metadata.MetadataItem;
import metadata.recon.concept.Concept;

/**
 * Reconstruction metadata.
 * 
 * @author Matthew.Stephenson and Eric.Piette
 */
public class Recon implements Serializable
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	final List<ReconItem> items = new ArrayList<ReconItem>();
	
	//-------------------------------------------------------------------------

	/**
	 * @param item  The info item of the game.
	 * @param items The info items of the game.
	 * 
	 * @example (recon {(concept "Num Players" 3)})
	 */
	public Recon
	(
		@Or	final ReconItem item,
		@Or final ReconItem[] items
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
			for (final ReconItem i : items)
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
		for (final ReconItem item : items)
			map.put(item.getClass().getSimpleName(), item);
	}

	//-------------------------------------------------------------------------

	/**
	 * 
	 * @return All the items.
	 */
	public List<ReconItem> getItem()
	{
		return Collections.unmodifiableList(items);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return The required concept values.
	 */
	public Map<String, Double> getConceptValues()
	{
		final Map<String, Double> conceptValues = new HashMap<>();
		for (final ReconItem infoItem : items)
			if (infoItem instanceof Concept)
				conceptValues.put(((Concept) infoItem).conceptName(), ((Concept) infoItem).value());
		return conceptValues;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		final String open  = (items.size() <= 1) ? "" : "{";
		final String close = (items.size() <= 1) ? "" : "}";
		
		sb.append("    (info " + open + "\n");
		for (final ReconItem item : items)
			if (item != null)
				sb.append("        " + item.toString());
		sb.append("    " + close + ")\n");

		return sb.toString();
	}

	//-------------------------------------------------------------------------

}
