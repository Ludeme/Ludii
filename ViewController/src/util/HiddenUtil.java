package util;

import java.util.BitSet;

import game.types.board.SiteType;
import main.Constants;
import other.context.Context;
import other.state.container.ContainerState;

/**
 * Functions to deal with hidden information
 * 
 * @author Matthew.Stephenson
 */
public class HiddenUtil 
{
	public final static int hiddenIndex = 0;
	public final static int hiddenWhatIndex = 1;
	public final static int hiddenWhoIndex = 2;
	public final static int hiddenStateIndex = 3;
	public final static int hiddenValueIndex = 4;
	public final static int hiddenCountIndex = 5;
	public final static int hiddenRotationIndex = 6;
	
	//-----------------------------------------------------------------------------
	
	/**
	 * Return true if the location is hidden from a specific who.
	 */
	public static boolean siteHidden(final Context context, final ContainerState cs, final int site, final int level, final int who, final SiteType type)
	{
		if (who > Constants.MAX_PLAYERS)
		{
			// If a spectator, check if info is hidden from ANY player
			for (int i = 1; i <= context.game().players().count(); i++)
			{
				if (cs.isHidden(i, site, level, type))
					return true;
			}
			return false;
		}
		else
		{
			return cs.isHidden(who, site, level, type);
		}
	}
	
	//-----------------------------------------------------------------------------
	
	/**
	 * Return true if the location's what is hidden from a specific who.
	 */
	public static boolean siteWhatHidden(final Context context, final ContainerState cs, final int site, final int level, final int who, final SiteType type)
	{
		// System.out.println("site = " + site);

		if (who > Constants.MAX_PLAYERS)
		{
			// If a spectator, check if info is hidden from ANY player
			for (int i = 1; i <= context.game().players().count(); i++)
			{
				if (cs.isHiddenWhat(i, site, level, type))
					return true;
			}
			return false;
		}
		else
		{
			return cs.isHiddenWhat(who, site, level, type);
		}
	}
	
	//-----------------------------------------------------------------------------
	
	/**
	 * Return true if the location's who is hidden from a specific who.
	 */
	public static boolean siteWhoHidden(final Context context, final ContainerState cs, final int site, final int level, final int who, final SiteType type)
	{
		if (who > Constants.MAX_PLAYERS)
		{
			// If a spectator, check if info is hidden from ANY player
			for (int i = 1; i <= context.game().players().count(); i++)
			{
				if (cs.isHiddenWho(i, site, level, type))
					return true;
			}
			return false;
		}
		else
		{
			return cs.isHiddenWho(who, site, level, type);
		}
	}
	
	//-----------------------------------------------------------------------------
	
	/**
	 * Return true if the location's state is hidden from a specific who.
	 */
	public static boolean siteStateHidden(final Context context, final ContainerState cs, final int site, final int level, final int who, final SiteType type)
	{
		if (who > Constants.MAX_PLAYERS)
		{
			// If a spectator, check if info is hidden from ANY player
			for (int i = 1; i <= context.game().players().count(); i++)
			{
				if (cs.isHiddenState(i, site, level, type))
					return true;
			}
			return false;
		}
		else
		{
			return cs.isHiddenState(who, site, level, type);
		}
	}
	
	//-----------------------------------------------------------------------------
	
	/**
	 * Return true if the location's count is hidden from a specific who.
	 */
	public static boolean siteCountHidden(final Context context, final ContainerState cs, final int site, final int level, final int who, final SiteType type)
	{
		if (who > Constants.MAX_PLAYERS)
		{
			// If a spectator, check if info is hidden from ANY player
			for (int i = 1; i <= context.game().players().count(); i++)
			{
				if (cs.isHiddenCount(i, site, level, type))
					return true;
			}
			return false;
		}
		else
		{
			return cs.isHiddenCount(who, site, level, type);
		}
	}
	
	//-----------------------------------------------------------------------------
	
	/**
	 * Return true if the location's value is hidden from a specific who.
	 */
	public static boolean siteValueHidden(final Context context, final ContainerState cs, final int site, final int level, final int who, final SiteType type)
	{
		if (who > Constants.MAX_PLAYERS)
		{
			// If a spectator, check if info is hidden from ANY player
			for (int i = 1; i <= context.game().players().count(); i++)
			{
				if (cs.isHiddenValue(i, site, level, type))
					return true;
			}
			return false;
		}
		else
		{
			return cs.isHiddenValue(who, site, level, type);
		}
	}
	
	//-----------------------------------------------------------------------------
	
	/**
	 * Return true if the location's rotation is hidden from a specific who.
	 */
	public static boolean siteRotationHidden(final Context context, final ContainerState cs, final int site, final int level, final int who, final SiteType type)
	{
		if (who > Constants.MAX_PLAYERS)
		{
			// If a spectator, check if info is hidden from ANY player
			for (int i = 1; i <= context.game().players().count(); i++)
			{
				if (cs.isHiddenRotation(i, site, level, type))
					return true;
			}
			return false;
		}
		else
		{
			return cs.isHiddenRotation(who, site, level, type);
		}
	}
	
	//-----------------------------------------------------------------------------
	
	/**
	 * Return an integer value representing the hidden information about a location for a specific who.
	 */
	public static int siteHiddenBitsetInteger(final Context context, final ContainerState cs, final int site, final int level, final int who, final SiteType type)
	{
		int hiddenInteger = 0;
		if (who != 0)
		{
			hiddenInteger += (siteHidden(context, cs, site, level, who, type) ? 1 : 0);
			hiddenInteger += ((siteWhatHidden(context, cs, site, level, who, type) ? 1 : 0) * Math.pow(2, hiddenWhatIndex));
			hiddenInteger += ((siteWhoHidden(context, cs, site, level, who, type) ? 1 : 0) * Math.pow(2, hiddenWhoIndex));
			hiddenInteger += ((siteStateHidden(context, cs, site, level, who, type) ? 1 : 0) * Math.pow(2, hiddenStateIndex));
			hiddenInteger += ((siteValueHidden(context, cs, site, level, who, type) ? 1 : 0) * Math.pow(2, hiddenValueIndex));
			hiddenInteger += ((siteCountHidden(context, cs, site, level, who, type) ? 1 : 0) * Math.pow(2, hiddenCountIndex));
			hiddenInteger += ((siteRotationHidden(context, cs, site, level, who, type) ? 1 : 0) * Math.pow(2, hiddenRotationIndex));
		}
		return hiddenInteger;
	}
	
	//-----------------------------------------------------------------------------
	
	/**
	 * Converts a hidden integer value into a bitset.
	 */
	public static BitSet intToBitSet(final int valueInput)
	{
		int value = valueInput;
        final BitSet bits = new BitSet();
        int index = 0;
        while (value != 0) 
        {
            if (value % 2 != 0) 
            {
                bits.set(index);
            }
            ++index;
            value = value >>> 1;
        }
        return bits;
    }
	
	//-----------------------------------------------------------------------------
	
}
