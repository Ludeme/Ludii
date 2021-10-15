package other.context;

import java.util.Arrays;

import game.functions.region.RegionFunction;
import game.util.equipment.Region;
import main.Constants;

/**
 * Class storing all the data from context used in the eval method of each
 * ludeme.
 * 
 * WARNING: All the data here should never be used outside of the eval methods.
 * 
 * @author Eric.Piette
 */
public class EvalContext
{
	/** Variable used to iterate the 'from' locations. */
	private int from = Constants.OFF;

	/** Variable used to iterate the levels. */
	private int level = Constants.OFF;

	/** Variable used to iterate the 'to' locations. */
	private int to = Constants.OFF;

	/** Variable used to iterate the 'between' locations. */
	private int between = Constants.OFF;

	/** Variable used to iterate the number of pips of each die. */
	private int pipCount = Constants.OFF;

	/** Variable used to iterate the players. */
	private int player = Constants.OFF;

	/** Variable used to iterate the tracks. */
	private int track = Constants.OFF;

	/** Variable used to iterate some sites. */
	private int site = Constants.OFF;

	/** Variable used to iterate values. */
	private int value = Constants.OFF;

	/** Variable used to iterate regions. */
	private Region region = null;

	/** Variable used to iterate hint regions. */
	private RegionFunction hintRegion = null;

	/** Variable used to iterate the hints. */
	private int hint = Constants.OFF;

	/** Variable used to iterate edges. */
	private int edge = Constants.OFF;

	/** Variable used to iterate teams. */
	private int[] team = null;

	//-------------------------------------------------------------------------

	/**
	 * Default constructor.
	 */
	public EvalContext()
	{
		// Nothing to do.
	}

	/**
	 * Copy constructor.
	 * 
	 * @param other
	 */
	public EvalContext(final EvalContext other)
	{
		setFrom(other.from());
		setTo(other.to());
		setLevel(other.level());
		setBetween(other.between());
		setPipCount(other.pipCount());
		setPlayer(other.player());
		setTrack(other.track());
		setSite(other.site);
		setValue(other.value);
		setRegion(other.region);
		if (other.team != null)
			setTeam(Arrays.copyOf(other.team, other.team.length));

		setHint(other.hint());
		setEdge(other.edge());
		setHintRegion(other.hintRegion);
	}

	//-------------------------------------------------------------------------

	/**
	 * @return From iterator.
	 */
	public int from()
	{
		return from;
	}

	/**
	 * Set the from iterator.
	 * 
	 * @param from The from iterator.
	 */
	public void setFrom(final int from)
	{
		this.from = from;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Track iterator.
	 */
	public int track()
	{
		return this.track;
	}

	/**
	 * Set the track iterator.
	 * 
	 * @param track The track iterator.
	 */
	public void setTrack(final int track)
	{
		this.track = track;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return To iterator.
	 */
	public int to()
	{
		return to;
	}

	/**
	 * Set the to iterator.
	 * 
	 * @param to The to iterator.
	 */
	public void setTo(final int to)
	{
		this.to = to;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Between iterator.
	 */
	public int between()
	{
		return between;
	}

	/**
	 * Set the between iterator.
	 * 
	 * @param between The between iterator.
	 */
	public void setBetween(final int between)
	{
		this.between = between;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Player iterator.
	 */
	public int player()
	{
		return player;
	}

	/**
	 * Set the player iterator.
	 * 
	 * @param player The player iterator.
	 */
	public void setPlayer(final int player)
	{
		this.player = player;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return PipCount iterator.
	 */
	public int pipCount()
	{
		return pipCount;
	}

	/**
	 * Set the pipCount iterator.
	 * 
	 * @param pipCount The player iterator.
	 */
	public void setPipCount(final int pipCount)
	{
		this.pipCount = pipCount;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Level iterator.
	 */
	public int level()
	{
		return level;
	}

	/**
	 * Set the level iterator.
	 * 
	 * @param level The level iterator.
	 */
	public void setLevel(final int level)
	{
		this.level = level;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Hint iterator.
	 */
	public int hint()
	{
		return hint;
	}

	/**
	 * Set the hint iterator.
	 * 
	 * @param hint The hint iterator.
	 */
	public void setHint(final int hint)
	{
		this.hint = hint;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Edge iterator.
	 */
	public int edge()
	{
		return edge;
	}

	/**
	 * Set the edge iterator.
	 * 
	 * @param edge The edge iterator.
	 */
	public void setEdge(final int edge)
	{
		this.edge = edge;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Site iterator.
	 */
	public int site()
	{
		return site;
	}

	/**
	 * Set the site iterator.
	 * 
	 * @param site The site iterator.
	 */
	public void setSite(final int site)
	{
		this.site = site;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Value iterator.
	 */
	public int value()
	{
		return value;
	}

	/**
	 * Set the value iterator.
	 * 
	 * @param value The value iterator.
	 */
	public void setValue(final int value)
	{
		this.value = value;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Region iterator.
	 */
	public Region region()
	{
		return region;
	}

	/**
	 * To set the region iterator.
	 * 
	 * @param region The region iterator.
	 */
	public void setRegion(final Region region)
	{
		this.region = region;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return The hint region function iterator.
	 */
	public RegionFunction hintRegion()
	{
		return hintRegion;
	}

	/**
	 * To set the region function iterator.
	 * 
	 * @param region The region function iterator.
	 */
	public void setHintRegion(final RegionFunction region)
	{
		hintRegion = region;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Team iterator.
	 */
	public int[] team()
	{
		return team;
	}

	/**
	 * Set the team iterator.
	 * 
	 * @param team The team iterator.
	 */
	public void setTeam(final int[] team)
	{
		this.team = team;
	}
}
