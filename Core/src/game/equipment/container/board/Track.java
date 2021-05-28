package game.equipment.container.board;

import java.io.Serializable;
import java.util.BitSet;
import java.util.List;

import annotations.Name;
import annotations.Opt;
import annotations.Or;
import game.Game;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.util.directions.DirectionFacing;
import game.util.equipment.TrackStep;
import game.util.graph.Radial;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.BaseLudeme;
import other.concept.Concept;
import other.topology.Cell;
import other.topology.Topology;
import other.topology.Vertex;

/**
 * Defines a named track for a container, which is typically the board.
 * 
 * @author Eric.Piette
 * 
 * @remarks Tracks are typically used for race games, or any game in which
 *          pieces move around a track. A number after a direction indicates the
 *          number of steps in that direction. For example, "N1,E3" means that
 *          track goes North for one step then turns East for three steps.
 */
public class Track extends BaseLudeme implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------------------------------------------------------------

	/**
	 * An element of a track
	 * 
	 * @author Eric.Piette
	 */
	public class Elem implements Serializable
	{
		private static final long serialVersionUID = 1L;

		// -------------------------------------------------------------------------------

		/** The site of the element. */
		public final int site;

		/** The previous site of the element. */
		public final int prev;

		/** The index on the track of the previous element. */
		public final int prevIndex;

		/** The next site of the element. */
		public final int next;

		/** The index on the track of the next element. */
		public final int nextIndex;

		/** The number of bumps on that elements. */
		public final int bump;
		
		/***
		 * An element of the track.
		 * 
		 * @param site      The site of the element.
		 * @param prev      The previous site of the element.
		 * @param prevIndex The index on the track of the previous element.
		 * @param next      The next site of the element.
		 * @param nextIndex The index on the track of the next element.
		 * @param bump      The number of bumps on that elements
		 */
		public Elem
		(
			final Integer site, 
			final Integer prev, 
			final Integer prevIndex, 
			final Integer next, 
			final Integer nextIndex,
			final Integer bump
		)
		{
			this.site = (site == null) ? -1 : site.intValue();
			this.prev = (prev == null) ? -1 : prev.intValue();
			this.prevIndex = (prevIndex == null) ? -1 : prevIndex.intValue();
			this.next = (next == null) ? -1 : next.intValue();
			this.nextIndex = (nextIndex == null) ? -1 : nextIndex.intValue();
			this.bump = bump.intValue();
		}
	}
	
	//-------------------------------------------------------------------------------
	
	/** The name of the track. */
	private final String name;
	
	/** The elements of the track. */
	private Elem[] elems;
	
	/** The tracks defined in the game description. */
	private Integer[] track;
	
	/** The track defined in the game description. */
	private final String trackDirection;

	/** The owner of the track.*/
	private final int owner;

	/** True if the track is a loop. */
	private final boolean looped;

	/** True if the track is directed. */
	private final boolean direct;
	
	/**
	 * True if the track has an internal loop (e.g. UR or big Pachisi).
	 */
	private boolean internalLoop = false;
	
	/** Our index in the board's list of tracks */
	private int trackIdx = Constants.UNDEFINED;

	//-------------------------------------------------------------------------------
	
	/**
	 * @param name           The name of the track.
	 * @param track          List of integers describing board site indices.
	 * @param trackDirection Description including site indices and cardinal.
	 * @param trackSteps     Description using track steps.
	 *                       directions (N, E, S, W).
	 * @param loop           True if the track is a loop [False].
	 * @param owner          The owner of the track [0].
	 * @param role           The role of the owner of the track [Neutral].
	 * @param directed       True if the track is directed [False].
	 * 
	 * @example (track "Track" "1,E,N,W" loop:True)
	 * 
	 * @example (track "Track1" {6 12..7 5..0 13..18 20..25 End} P1 directed:True)
	 * 
	 * @example (track "Track1" "20,3,W,N1,E,End" P1 directed:True)
	 */
	public Track
	(
				       final String      name,
		     @Or       final Integer[]   track,
		     @Or 	   final String      trackDirection,
			 @Or       final TrackStep[] trackSteps,
		@Opt     @Name final Boolean     loop,
		@Opt @Or       final Integer     owner,
		@Opt @Or       final RoleType    role,
		@Opt     @Name final Boolean     directed
	) 
	{
		int numNonNull = 0;
		if (track != null)
			numNonNull++;
		if (trackDirection != null)
			numNonNull++;
		if (trackSteps != null)
			numNonNull++;
		
		if (numNonNull != 1)
			throw new IllegalArgumentException("Exactly one Or parameter must be non-null.");
		
		int numNonNull2 = 0;
		if (owner != null)
			numNonNull2++;
		if (role != null)
			numNonNull2++;
		
		if (numNonNull2 > 1)
			throw new IllegalArgumentException("Zero or one Or parameter must be non-null.");
		
		this.name = (name == null) ? "Track" : name;
		this.owner = (owner == null) ? (role == null) ? 0 : role.owner() : owner.intValue();
		
		this.looped = (loop == null) ? false : loop.booleanValue();
		this.direct = (directed == null) ? false : directed.booleanValue();
		this.track = track;
		this.trackDirection = trackDirection;
		elems = null;
	}

	//-------------------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		if (looped)
			concepts.set(Concept.TrackLoop.id(), true);
		if (owner != 0)
			concepts.set(Concept.TrackOwned.id(), true);

		return concepts;
	}

	/**
	 * To build the graph.
	 * 
	 * @param game
	 */
	public void buildTrack(final Game game)
	{
		// Temporary until the track steps are fully implementing.
		if (trackDirection == null && track == null)
			return;

		final Topology topology = game.board().topology();

		if (game.board().defaultSite() == SiteType.Cell)
		{
			if (trackDirection != null)
			{
				final String[] steps = trackDirection.split(",");

				if (steps.length < 1)
					throw new IllegalArgumentException("The track " + name + " is not correct");

				if (!isNumber(steps[0]))
					throw new IllegalArgumentException("The first step in the track " + name + " is not a number");
				
				final int start = Integer.parseInt(steps[0]);

				// if (start >= graph.vertices().size())
				// throw new IllegalArgumentException(
				// "The site " + start + " is greater than the number of sites in the main
				// board");

				final TIntArrayList trackList = new TIntArrayList();
				trackList.add(start);
				Cell current = (start < game.board().topology().cells().size()) ? game.board().topology().cells().get(start)
						: null;

				for (int i = 1; i < steps.length; i++)
				{
					final String step = steps[i];

					final boolean stepIsNumber = isNumber(step);

					if (stepIsNumber)
					{
						final int site = Integer.valueOf(step).intValue();
						current = (site < game.board().topology().cells().size()) ? game.board().topology().cells().get(site)
								: null;
						trackList.add(site);
					}
					else if (step.equals("End"))
					{
						final int site = Integer.valueOf(Constants.END).intValue();
						trackList.add(site);
					}
					else
					{
						if (current == null)
							throw new IllegalArgumentException("The step " + step + " in the track " + name
									+ " is impossible without a correct site in the main board.");

						String direction = "";
						for (int j = 0; j < step.length(); j++)
							if (!Character.isDigit(step.charAt(j)))
								direction += step.charAt(j);

						int size = Constants.UNDEFINED;
						if (direction.length() != step.length())
							size = Integer.parseInt(step.substring(direction.length()));

						final DirectionFacing dirn = convertStringDirection(direction,
								game.board().topology().supportedDirections(SiteType.Cell));

						if (dirn == null)
							throw new IllegalArgumentException("The step " + step + " is wrong in the track " + name);

						final List<Radial> radials = topology.trajectories().radials(SiteType.Cell, current.index(),
								dirn.toAbsolute());

						if (size == Constants.UNDEFINED)
						{
								for (final Radial radial : radials)
								{
									for (int toIdx = 1; toIdx < radial.steps().length; toIdx++)
									{
										final int site = radial.steps()[toIdx].id();
										current = game.board().topology().cells().get(site);
										trackList.add(site);
									}
								}
						}
						else
						{
							for (final Radial radial : radials)
							{
								for (int toIdx = 1; toIdx < radial.steps().length && toIdx < size + 1; toIdx++)
								{
									final int site = radial.steps()[toIdx].id();
									current = game.board().topology().cells().get(site);
									trackList.add(site);
								}
							}
						}
					}
				}
				track = new Integer[trackList.size()];
				for (int i = 0; i < trackList.size(); i++)
				{
					final int loc = trackList.getQuick(i);
					track[i] = Integer.valueOf(loc);
				}

			}
		}
		else if (game.board().defaultSite() == SiteType.Vertex)
		{
			if (trackDirection != null)
			{
				final String[] steps = trackDirection.split(",");

				if (steps.length < 1)
					throw new IllegalArgumentException("The track " + name + " is not correct.");

				if (!isNumber(steps[0]))
					throw new IllegalArgumentException("The first step in the track " + name + " is not a number.");

				final int start = Integer.parseInt(steps[0]);

				// if (start >= graph.vertices().size())
				// throw new IllegalArgumentException(
				// "The site " + start + " is greater than the number of sites in the main
				// board");

				final TIntArrayList trackList = new TIntArrayList();
				trackList.add(start);
				Vertex current = (start < game.board().topology().vertices().size())
						? game.board().topology().vertices().get(start)
						: null;

				for (int i = 1; i < steps.length; i++)
				{
					final String step = steps[i];

					final boolean stepIsNumber = isNumber(step);

					if (stepIsNumber)
					{
						final int site = Integer.valueOf(step).intValue();
						current = (site < game.board().topology().vertices().size())
								? game.board().topology().vertices().get(site)
								: null;
						trackList.add(site);
					}
					else if (step.equals("End"))
					{
						final int site = Integer.valueOf(Constants.END).intValue();
						trackList.add(site);
					}
					else
					{
						if (current == null)
							throw new IllegalArgumentException("The step " + step + " in the track " + name
									+ " is impossible without a correct site in the main board.");

						String direction = "";
						for (int j = 0; j < step.length(); j++)
							if (!Character.isDigit(step.charAt(j)))
								direction += step.charAt(j);

						int size = Constants.UNDEFINED;
						if (direction.length() != step.length())
							size = Integer.parseInt(step.substring(direction.length()));

						final DirectionFacing dirn = convertStringDirection(direction,
								game.board().topology().supportedDirections(SiteType.Vertex));

						if (dirn == null)
							throw new IllegalArgumentException("The step " + step + " is wrong in the track " + name);

						final List<Radial> radials = topology.trajectories().radials(SiteType.Vertex, current.index(),
								dirn.toAbsolute());

						if (size == Constants.UNDEFINED)
						{
							for (final Radial radial : radials)
							{
								for (int toIdx = 1; toIdx < radial.steps().length; toIdx++)
								{
									final int site = radial.steps()[toIdx].id();
									current = game.board().topology().vertices().get(site);
									trackList.add(site);
								}
							}
						}
						else
						{
							for (final Radial radial : radials)
							{
								for (int toIdx = 1; toIdx < radial.steps().length && toIdx < size + 1; toIdx++)
								{
									final int site = radial.steps()[toIdx].id();
									current = game.board().topology().vertices().get(site);
									trackList.add(site);
								}
							}
						}
					}
				}
				track = new Integer[trackList.size()];
				for (int i = 0; i < trackList.size(); i++)
				{
					final int loc = trackList.getQuick(i);
					track[i] = Integer.valueOf(loc);
				}
			}
		}

		final TIntArrayList trackWithoutBump = new TIntArrayList();
		final TIntArrayList nbBumpByElem = new TIntArrayList();
		boolean hasBump = false;
		
		int countBump = 0;
		for (int i = 0; i < track.length; i++)
		{
			if (i == track.length - 1 || track[i].intValue() != track[i + 1].intValue())
			{
				trackWithoutBump.add(track[i].intValue());
				nbBumpByElem.add(countBump);
				countBump = 0;
			}
			else
			{
				hasBump = true;
				countBump++;
			}
		}

		final int[] newTrack = trackWithoutBump.toArray();
		
		this.elems = new Elem[newTrack.length];
		
		for (int i = 0; i < newTrack.length; i++)
		{
			Elem e = null;
			if (i == 0 && looped)
				e = new Elem(Integer.valueOf(newTrack[i]), Integer.valueOf(newTrack[newTrack.length-1]), Integer.valueOf(newTrack.length-1),  Integer.valueOf(newTrack[i+1]), Integer.valueOf(i +1), Integer.valueOf(nbBumpByElem.getQuick(i)));
			else if (i == 0 && !looped)
				e = new Elem(Integer.valueOf(newTrack[i]), null, null, Integer.valueOf(newTrack[i+1]), Integer.valueOf(i+1), Integer.valueOf(nbBumpByElem.getQuick(i)));
			else if (i == (newTrack.length-1) && looped) 
				e = new Elem(Integer.valueOf(newTrack[i]), Integer.valueOf(newTrack[i-1]), Integer.valueOf(i-1), Integer.valueOf(newTrack[0]), Integer.valueOf(0), Integer.valueOf(nbBumpByElem.getQuick(i)));
			else if(i == (newTrack.length-1) && !looped) 
				e = new Elem(Integer.valueOf(newTrack[i]), Integer.valueOf(newTrack[i-1]), Integer.valueOf(i-1), null, null, Integer.valueOf(nbBumpByElem.getQuick(i)));
			else if(direct)
				e = new Elem(Integer.valueOf(newTrack[i]), null, null, Integer.valueOf(newTrack[i+1]), Integer.valueOf(i+1), Integer.valueOf(nbBumpByElem.getQuick(i)));
			else
				e = new Elem(Integer.valueOf(newTrack[i]),Integer.valueOf(newTrack[i-1]), Integer.valueOf(i-1), Integer.valueOf(newTrack[i+1]), Integer.valueOf(i+1), Integer.valueOf(nbBumpByElem.getQuick(i)));
			this.elems[i] = e;
		}

		// We check if the track has an internal loop.
		if (!hasBump)
		{
			final TIntArrayList listSites = new TIntArrayList();
			for (final Elem elem : elems)
			{
				final int site = elem.site;
				if (listSites.contains(site))
				{
					internalLoop = true;
					break;
				}
				listSites.add(site);
			}
		}
	}
	
	/**
	 * @param direction
	 * @param supportedDirectionTypes
	 * @return The direction corresponding to a string name if it exists
	 */
	private static DirectionFacing convertStringDirection(String direction, List<DirectionFacing> supportedDirections)
	{
		for (final DirectionFacing directionSupported : supportedDirections)
			if (directionSupported.uniqueName().toString().equals(direction))
				return directionSupported;
		return null;
	}

	/**
	 * 
	 * @param str
	 * @return True if the string is a number.
	 */
	private static boolean isNumber(String str)
	{
		for (int i = 0; i < str.length(); i++)
			if (str.charAt(i) < '0' || str.charAt(i) > '9')
				return false;

		return true;
	}

	/**
	 * @return The name of the track.
	 */
	public String name() 
	{
		return this.name;
	}
	
	/**
	 * @return The elements of the tracks.
	 */
	public Elem[] elems() 
	{
		return this.elems;
	}
	
	/**
	 * @return The owner of the track.
	 */
	public int owner() 
	{
		return this.owner;
	}

	/**
	 * @return True if the track is looped (e.g. mancala games).
	 */
	public boolean islooped()
	{
		return this.looped;
	}

	/**
	 * @return True if the track has an internal looped (e.g. Ur variant or big
	 *         pachisi).
	 */
	public boolean hasInternalLoop()
	{
		return this.internalLoop;
	}
	
	/**
	 * @return This track's index in the board's list of tracks
	 */
	public int trackIdx()
	{
		return trackIdx;
	}
	
	/**
	 * Sets the track index
	 * @param trackIdx
	 */
	public void setTrackIdx(final int trackIdx)
	{
		this.trackIdx = trackIdx;
	}

	/**
	 * Note: That method is working only for simple track with no loop (loop track
	 * or track with internal loop).
	 * 
	 * @param site The board index of the site.
	 * @return The track index of the site.
	 */
	public int siteIndex(final int site)
	{
		for (int i = 0; i < elems().length; i++)
			if (elems()[i].site == site)
				return i;

		return Constants.UNDEFINED;
	}
}
