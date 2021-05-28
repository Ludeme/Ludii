package other;

import java.util.Arrays;

/**
 * A collection of sites within a container.
 * 
 * @author cambolbro
 */
public class Sites
{
	private int         count = 0;
	private final int[] sites;

	//-------------------------------------------------------------------------

	/**
	 * Constructor.
	 * 
	 * @param count
	 */
	public Sites(final int count)
	{
		this.count = count;
		this.sites = new int[count];
		for (int n = 0; n < count; n++)
			this.sites[n] = n;
	}

	/**
	 * Constructor.
	 * 
	 * @param sites
	 */
	public Sites(final int[] sites)
	{
		this.count = sites.length;
		this.sites = new int[this.count];
//		for (int n = 0; n < count; n++)
//			this.sites[n] = sites[n];
		System.arraycopy(sites, 0, this.sites, 0, this.count);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 */
	public Sites(final Sites other)
	{
		this.count = other.count;
		this.sites = new int[other.sites.length];
//		for (int n = 0; n < count; n++)
//			this.sites[n] = other.sites[n];
		System.arraycopy(other.sites, 0, this.sites, 0, this.sites.length);
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Number of sites.
	 */
	public int count()
	{
		return count;
	}

	/**
	 * @return Collection of site values.
	 */
	public int[] sites()
	{
		return sites;
	}

	//-------------------------------------------------------------------------

	/**
	 * Note: May be sparse!
	 * 
	 * @param newCount
	 */
	public void set(final int newCount)
	{
		if (newCount > sites.length)
		{
			System.out.println("** Sites.set() A: Bad count " + newCount + " for " + sites.length + " entries.");
			try
			{
				throw new Exception("Exception.");
			} 
			catch (final Exception e)
			{
				e.printStackTrace();
			}
			return;
		}

		count = newCount;
		for (int n = 0; n < count; n++)
			sites[n] = n;

		for (int n = count; n < sites.length; ++n)
			sites[n] = -1;
	}

	/**
	 * Note: May be sparse!
	 * 
	 * @param other
	 */
	public void set(final Sites other)
	{
		if (other.count > sites.length)
		{
			System.out.println("** Sites.set() B: Bad count " + other.count + " for " + sites.length + " entries.");
			return;
		}

		count = other.count;
//		for (int n = 0; n < count; n++)
//			sites[n] = other.sites[n];
		System.arraycopy(other.sites, 0, sites, 0, count);

		for (int n = count; n < sites.length; ++n)
			sites[n] = -1;
	}

	/**
	 * @param n
	 * @return Nth value.
	 */
	public int nthValue(final int n)
	{
		return sites[n];
	}

	/**
	 * @param val
	 */
	public void add(final int val)
	{
		if (count >= sites.length)
		{
			System.out.println("** Sites.add(): Trying to add " + val + " to full array.");
			return;
		}

		sites[count] = val;
		count++;
	}

	/**
	 * Remove the specified value.
	 * 
	 * @param val
	 */
	public void remove(final int val)
	{
		if (count < 1)
		{
			System.out.println("** Sites.remove(): Trying to remove " + val + " from " + count + " entries.");
			return;
		}

		// At start, sites[n]==n.
		// Once we use an entry, we swap always to a lower index,
		// So start at cell and work down...
		for (int n = Math.min(val, count - 1); n >= 0; n--)
			if (sites[n] == val)
			{
				// Move tail into slot n
				sites[n] = sites[count - 1];
				sites[count - 1] = -1;
				count--;
				return; // n;
			}

		// ...unless we added a cell back in!
		for (int n = count - 1; n > Math.min(val, count - 1); n--)
			if (sites[n] == val)
			{
				// Move tail into slot n
				sites[n] = sites[count - 1];
				sites[count - 1] = -1;
				count--;
				return; // n;
			}

		// Should be impossible
		// return -1;

		System.out.println("** Sites.remove(): Failed to find value " + val + ".");
	}

	/**
	 * Remove the Nth entry.
	 * 
	 * @param n
	 * @return Value of Nth entry.
	 */
	public int removeNth(final int n)
	{
		if (count < 1)
		{
			System.out.println("** Sites.remove(): Trying to remove " + n + "th entry from " + count + " entries.");
			return -1;
		}

		// Move tail into slot n
		final int val = sites[n];
		sites[n] = sites[count - 1];
		sites[count - 1] = -1;
		count--;
		return val;
	}

	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return "Sites{" + Arrays.toString(sites) + " (count at:= " + count + ")}";
	}

}
