package game.equipment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import game.Game;
import game.equipment.component.Component;
import game.equipment.component.Die;
import game.equipment.component.Piece;
import game.equipment.component.tile.Domino;
import game.equipment.container.Container;
import game.equipment.container.board.Board;
import game.equipment.container.board.Track;
import game.equipment.container.other.Deck;
import game.equipment.container.other.Dice;
import game.equipment.container.other.Hand;
import game.equipment.other.Dominoes;
import game.equipment.other.Hints;
import game.equipment.other.Map;
import game.equipment.other.Regions;
import game.functions.region.RegionFunction;
import game.types.board.RelationType;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.types.state.GameType;
import main.Constants;
import main.StringRoutines;
import other.BaseLudeme;
import other.ItemType;
import other.context.Context;
import other.topology.Topology;
import other.topology.TopologyElement;
import other.translation.LanguageUtils;
import other.trial.Trial;

/**
 * Defines the equipment list of the game.
 *
 * @author cambolbro and Eric.Piette
 * 
 * @remarks To define the items (container, component etc.) of the game. Any
 *          type of component or container described in this chapter may be used
 *          as an \texttt{<item>} type.
 */
public final class Equipment extends BaseLudeme implements Serializable
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** List of containers. */
	private Container[] containers = null;

	/** List of components. */
	private Component[] components = null;

	/** List of regions. */
	private Regions[] regions = null;

	/** List of maps. */
	private Map[] maps = null;

	/** Total number of sites over all containers. */
	private int totalDefaultSites = 0;

	/** Which container a given accumulated site index refers to. */
	private int[] containerId;

	/** Which actual site within its container a given accumulated site index refers to. */
	private int[] offset;

	/** Which accumulated site index a given container starts at. */
	private int[] sitesFrom;
	
	/** Vertex with hints for Deduction Puzzle. */
	private Integer[][] vertexWithHints = new Integer[0][0];

	/** Cell with hints for Deduction Puzzle. */
	private Integer[][] cellWithHints = new Integer[0][0];

	/** Edge with hints for Deduction Puzzle. */
	private Integer[][] edgeWithHints = new Integer[0][0];

	/** The hints of the vertices. */
	private Integer[] vertexHints = new Integer[0];

	/** The hints of the cells. */
	private Integer[] cellHints = new Integer[0];

	/** The hints of the edges. */
	private Integer[] edgeHints = new Integer[0];
	
	/** Here we store items received from constructor, to be created when game.create() is called. */
	private Item[] itemsToCreate;

	//-------------------------------------------------------------------------

	/**
	 * @param items The items (container, component etc.).
	 * 
	 * @example (equipment { (board (square 3)) (piece "Disc" P1) (piece "Cross" P2)
	 *          })
	 */
	public Equipment
	(
		final Item[] items
	)
	{
		boolean hasABoard = false;
		for (final Item item : items)
		{
			if (item instanceof Board)
			{
				hasABoard = true;
				break;
			}
		}

		if (!hasABoard)
		{
			throw new IllegalArgumentException("At least a board has to be defined in the equipment.");
		}

		itemsToCreate = items;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		String text = "";
		final HashMap<String, String> ruleMap = new HashMap<>();

		if(containers != null && containers.length > 0)
		{
			for(int j = 0; j < containers.length; j++)
				text += containers[j].toEnglish(game);	
			
			text += ".";
		}

		if (components()!=null && components().length>1)
		{
			text+=" ";
			
			// We create a list of player names:
			final ArrayList<RoleType> playerRoleList = new ArrayList<>();

			// First we search for all player names:
			for (int j = 1; j <= components().length-1; j++) 
			{
				final RoleType playerRole = components()[j].role();
				
				if(!playerRoleList.contains(playerRole))
					playerRoleList.add(playerRole);
			}

			// Sort the names of player, so that every order of ludemes produce the same order of stuff here:
			playerRoleList.sort((e1, e2) -> { return e1.name().compareToIgnoreCase(e2.name()); });

			String pieceText = "";
			for(final RoleType playerRole: playerRoleList) 
			{
				final ArrayList<String> pieces = new ArrayList<>();

				final String playerName = LanguageUtils.RoleTypeAsText(playerRole, true);
				pieceText += (pieceText.isEmpty() ? "" : " ") + playerName + " plays ";
				for (int j = 1; j <= components().length-1; j++) 
				{
					if(playerRole.equals(components()[j].role())) 
						pieces.add(components()[j].getNameWithoutNumber());

					if(components()[j].generator() != null) 
					{
						String plural = StringRoutines.getPlural(components()[j].getNameWithoutNumber());
						
						// Check if the old existing rule for this component should be updated.
						final String newRule = components()[j].getNameWithoutNumber() + plural + " " + components()[j].generator().toEnglish(game) + ".";
						final String oldRule = ruleMap.get(components()[j].getNameWithoutNumber());
						if(!newRule.equals(oldRule)) 
						{
							if(oldRule == null) 
								ruleMap.put(components()[j].getNameWithoutNumber(), newRule);
							else 
								ruleMap.put(components()[j].getNameWithoutNumber() + "(" + components()[j].owner() + ")", newRule);
						}
					}
				}

				for(int n = 0; n < pieces.size(); n++) 
				{	
					if(n == pieces.size() - 1 && n > 0) 
						pieceText += " and ";
					else if(n > 0)
						pieceText += ", ";

					final String piece = pieces.get(n);

					pieceText += piece;

					pieceText += StringRoutines.getPlural(piece);
				}
				
				pieceText +=".";
			}
			
			text += (pieceText.isEmpty() ? "" : "\n") + pieceText;

			// Adding the rules for the pieces in the end
			if(!ruleMap.isEmpty()) 
			{
				final String[] ruleKeys = ruleMap.keySet().toArray(new String[ruleMap.size()]);
				Arrays.sort(ruleKeys);

				String ruleText = "";
				int count=0;
				for(final String rKey: ruleKeys) 
				{
					ruleText += (ruleText.isEmpty() ? "" : " ") + ruleMap.get(rKey);					
					count++;
		            if(count <= ruleKeys.length-1)
		            	ruleText+="\n     ";
				}
				
				text += "\nRules for Pieces: \n     " + ruleText;
			}
		}

		return text;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Creates all the items
	 * 
	 * @param game The game.
	 */
	public void createItems(final Game game)
	{
		// First create these as lists; at the end, transform them to arrays
		final List<Component> componentsWIP = new ArrayList<Component>();
		final List<Container> containersWIP = new ArrayList<Container>();
		final List<Regions> regionsWIP = new ArrayList<Regions>();
		final List<Map> mapsWIP = new ArrayList<Map>();
		
		// The empty component 
		// componentsWIP.add(null);
		final Piece emptyPiece = new Piece("Disc", RoleType.Neutral, null, null, null, null, null, null);
		emptyPiece.setIndex(0);
		componentsWIP.add(emptyPiece);

		if (itemsToCreate != null)
		{
			// To sort the list of items.
			final Item[] sortItems = sort(itemsToCreate);
	
			int indexDie = 1;
			int indexCard = 1;
			for (final Item item : sortItems)
			{
				// If this is container.
				if (ItemType.isContainer(item.type()))
				{
					final Container c = (Container) item;
	
					if 
					(
						item.type().ordinal() >= ItemType.Hand.ordinal()
						&& 
						item.type().ordinal() <= ItemType.Dice.ordinal()
					)
					{
						if (c.role() != null && c.role() == RoleType.Each)
						{
							final Hand hand = (Hand) c;
							for (int idPlayer = 1; idPlayer <= game.players().count(); idPlayer++)
							{
								final Hand newHand = hand.clone();
								if (hand.name() == null)
									newHand.setName("Hand");
								newHand.setName(newHand.name() + idPlayer);
								newHand.setRoleFromPlayerId(idPlayer);
								containersWIP.add(newHand);
							}
						}
						else if (c.type() == ItemType.Dice)
						{
							final int indexSameDice = multiDiceSameOwner((Dice) c, containersWIP);
							if (indexSameDice == Constants.UNDEFINED)
							{
								containersWIP.add(c);
								final Dice dice = (Dice) c;
								for (int i = indexDie; i <= dice.numLocs() + indexDie - 1; i++)
								{
									final Die die = new Die("Die" + i, dice.role(), Integer.valueOf(dice.getNumFaces()),
											null, null);
									die.setBiased(dice.getBiased());
									die.setFaces(dice.getFaces()[i - indexDie], dice.getStart());
									componentsWIP.add(die);
								}
								indexDie += dice.numLocs();
							}
							else
							{
								final Dice dice = (Dice) c;
								for (int i = indexDie; i <= dice.numLocs() + indexDie - 1; i++)
								{
									final Die die = new Die("Die" + i, dice.role(), Integer.valueOf(dice.getNumFaces()),
											null, null);
									die.setBiased(dice.getBiased());
									die.setFaces(dice.getFaces()[i - indexDie], dice.getStart());
									componentsWIP.add(die);
								}
								indexDie += dice.numLocs();
								containersWIP.set(indexSameDice,
										new Dice(Integer.valueOf(dice.getNumFaces()), null, dice.getFaces(), null,
												dice.role(), Integer.valueOf(
														((Dice) containersWIP.get(indexSameDice)).numLocs()
														+ dice.numLocs()), null));
							}
						}
						else if (c.isDeck())
						{
							containersWIP.add(c);
							final Deck deck = (Deck) c;
							final List<Component> cards = deck.generateCards(indexCard, componentsWIP.size());
							componentsWIP.addAll(cards);
							indexCard += cards.size();
						}
						else 
						{
							if (c.name() == null)
								c.setName("Hand"+((Hand)c).owner());
							containersWIP.add(c);
						}
					}
					else
					{
						containersWIP.add(c);
					}
	
					// To create the numbers
					if (game.isDeductionPuzzle())
					{
						final Board puzzleBoard = (Board) c;
						if (puzzleBoard.cellRange().max(new Context(game, new Trial(game))) != 0)
						{
							for (int num = puzzleBoard.cellRange()
									.min(new Context(game, new Trial(game))); num <= puzzleBoard.cellRange()
											.max(new Context(game, new Trial(game))); num++)
							{
								final Piece number = new Piece(Integer.valueOf(num) + "", RoleType.P1, null,
										null, null, null, null, null);
								//SettingsGeneral.setMaxNumberValue(
								//		puzzleBoard.cellRange().max(new Context(game, new Trial(game))));
								componentsWIP.add(number);
							}
						}
					}
				}
				else if (ItemType.isComponent(item.type()))
				{
					if (item.type() == ItemType.Component)
					{
						final Component comp = (Component)item;
						
//						if (isNumber(comp))
//						{
//							// Special generation for number in case of puzzle
//							if (comp.getValue() > SettingsGeneral.getMaxNumberValue())
//								SettingsGeneral.setMaxNumberValue(comp.getValue());
//						}
	
						if (comp.role() != null && comp.role() == RoleType.Each)
						{
							for (int idPlayer = 1; idPlayer <= game.players().count(); idPlayer++)
							{
								final Component compCopy = comp.clone();	
								if (comp.name() == null)
								{
									final String className = comp.getClass().toString();
									final String componentName = className.substring(className.lastIndexOf('.') + 1, className.length());
									compCopy.setName(componentName);
								}
								compCopy.setRoleFromPlayerId(idPlayer);
								compCopy.setName(compCopy.name());
								compCopy.setIndex(componentsWIP.size() - 1);

								componentsWIP.add(compCopy);
							}
						}
						else
						{
							if (comp.name() == null)
							{
								final String className = comp.getClass().toString();
								final String componentName = className.substring(className.lastIndexOf('.') + 1, className.length());
								comp.setName(componentName + comp.owner());
							}
							componentsWIP.add(comp);
						}
						
					}
					else if (item.type() == ItemType.Dominoes)
					{
						final Dominoes dominoes = (Dominoes) item;
						final ArrayList<Domino> listDominoes = dominoes.generateDominoes();
						for (final Domino domino : listDominoes)
							componentsWIP.add(domino);
					}
				}
				else if (ItemType.isRegion(item.type()))
				{
					regionsWIP.add((Regions) item);
				}
				else if (ItemType.isMap(item.type()))
				{
					mapsWIP.add((Map) item);
				}
				else if (ItemType.isHints(item.type()))
				{
					final Hints hints = (Hints) item;
					final int minSize = Math.min(hints.where().length, hints.values().length);
					final SiteType puzzleType = hints.getType();
					if (puzzleType.equals(SiteType.Vertex))
					{
						setVertexWithHints(new Integer[minSize][]);
						setVertexHints(new Integer[minSize]);
					}
					else if (puzzleType.equals(SiteType.Edge))
					{
						setEdgeWithHints(new Integer[minSize][]);
						setEdgeHints(new Integer[minSize]);
					}
					else if (puzzleType.equals(SiteType.Cell))
					{
						setCellWithHints(new Integer[minSize][]);
						setCellHints(new Integer[minSize]);
					}

					for (int i = 0; i < minSize; i++)
					{
						if (puzzleType.equals(SiteType.Vertex))
						{
							verticesWithHints()[i] = hints.where()[i];
							vertexHints()[i] = hints.values()[i];
						}
						else if (puzzleType.equals(SiteType.Edge))
						{
							edgesWithHints()[i] = hints.where()[i];
							edgeHints()[i] = hints.values()[i];
						}
						else if (puzzleType.equals(SiteType.Cell))
						{
							cellsWithHints()[i] = hints.where()[i];
							cellHints()[i] = hints.values()[i];
						}
					}
				}
			}
		}

//		if (containersWIP.size() == 0)
//		{
//			// Create a default board
//			//System.out.println("Creating default container...");
//			containersWIP.add
//			(
//				new Board
//				(
//							new RectangleOnSquare(new DimConstant(3), null, null, null), null, null, null, null, null
//				)
//			);
//		}

		// Only null placeholder is present
		if (componentsWIP.size() == 1 && !(game.isDeductionPuzzle()))
		{
			// Create a default piece per player
			// System.out.println("Creating default components...");
			for (int pid = 1; pid <= game.players().count(); pid++)
			{
				final String name = "Ball" + pid;
				// System.out.println("-- Creating " + name + " for " + RoleType.values()[pid] +
				// "...");

				final Component piece = new Piece(name, RoleType.values()[pid], null, null, null, null, null, null);
				componentsWIP.add(piece);
			}
		}
		
		// Now we can transform our lists to arrays
		containers = containersWIP.toArray(new Container[containersWIP.size()]);
		components = componentsWIP.toArray(new Component[componentsWIP.size()]);
		regions = regionsWIP.toArray(new Regions[regionsWIP.size()]);
		maps = mapsWIP.toArray(new Map[mapsWIP.size()]);
		
		initContainerAndParameters(game);
		
		for (final Container cont : containers)
		{
			if (cont != null)
				cont.create(game);
		}
		
		for (final Component comp : components)
		{
			if (comp != null)
				comp.create(game);
		}
		
		for (final Regions reg : regions)
		{
			reg.create(game);
		}
		
		for (final Map map : maps)
		{
			map.create(game);
		}
		
		// We're done, so can clean up this memory
		itemsToCreate = null;

		if (game.hasTrack())
		{
			//  Tell every track what its index is
			for (int i = 0; i < game.board().tracks().size(); ++i)
			{
				game.board().tracks().get(i).setTrackIdx(i);
			}
			
			// We pre-computed the ownedTrack array.
			final Track[][] ownedTracks = new Track[game.players().size() + 1][];
			for (int i = 0; i < ownedTracks.length; i++)
			{
				final List<Track> ownedTrack = new ArrayList<Track>();
				for (final Track track : game.board().tracks())
					if (track.owner() == i)
						ownedTrack.add(track);
				final Track[] ownedTrackArray = new Track[ownedTrack.size()];
				for (int j = 0; j < ownedTrackArray.length; j++)
					ownedTrackArray[j] = ownedTrack.get(j);

				ownedTracks[i] = ownedTrackArray;
			}
			game.board().setOwnedTrack(ownedTracks);
		}
		
	}

	/**
	 * @return The list of items sorted by Main container, hands, dice, deck
	 *         regions, maps, components.
	 */
	private static Item[] sort(final Item[] items)
	{
		final Item[] sortedItem = new Item[items.length];

		final int maxOrdinalValue = ItemType.values().length;

		int indexSortedItem = 0;
		for (int ordinalValue = 0; ordinalValue < maxOrdinalValue; ordinalValue++)
			for (final Item item : items)
			{
				if (item.type().ordinal() == ordinalValue)
				{
					sortedItem[indexSortedItem] = item;
					indexSortedItem++;
				}
			}
		
		return sortedItem;
	}

	/**
	 * @return The index of the dice if you have more than one Dice owner by the
	 *         same player (even for the shared player) else -1.
	 */
	private static int multiDiceSameOwner(final Dice c, final List<Container> containers)
	{
		for (int i = 0 ; i < containers.size(); i++)
		{
			final Container container = containers.get(i);
			if (container.isDice())
			{
				final Dice containerDice = (Dice) container;
				if (containerDice.owner() == c.owner() && !containerDice.equals(c))
					return i;
			}
		}
		return Constants.UNDEFINED;
	}

	/**
	 * To init totalSites, offset, sitesFrom, containerId.
	 * 
	 * @param game The game
	 */
	public void initContainerAndParameters(final Game game)
	{
		final long gameFlags = game.computeGameFlags();		// NOTE: need compute here!
		int index = 0;
		for (int e = 0; e < containers.length; e++)
		{
			final Container cont = containers[e];

			// Creation of the graph
			cont.createTopology(index,
					(cont.index() == 0) ? Constants.UNDEFINED : containers[0].topology().numEdges());
			
			final Topology topology = cont.topology();
			
			for (final SiteType type : SiteType.values())
			{
				topology.computeRelation(type);

				topology.computeSupportedDirection(type);

				// Convert the properties to the list of each pregeneration.
				for (final TopologyElement element : topology.getGraphElements(type))
					topology.convertPropertiesToList(type, element);

				final boolean threeDimensions = ((gameFlags & GameType.ThreeDimensions) != 0L);
				topology.computeRows(type, threeDimensions);
				topology.computeColumns(type, threeDimensions);

				if (!cont.isBoardless())
				{
					topology.crossReferencePhases(type);
					
					topology.computeLayers(type);
					topology.computeCoordinates(type);

					// Precompute distance tables
					topology.preGenerateDistanceTables(type);
				}

				// We compute the step distance only if needed by the game.
				if ((gameFlags & GameType.StepAdjacentDistance) != 0L)
					topology.preGenerateDistanceToEachElementToEachOther(type, RelationType.Adjacent);
				else if ((gameFlags & GameType.StepAllDistance) != 0L)
					topology.preGenerateDistanceToEachElementToEachOther(type, RelationType.All);
				else if ((gameFlags & GameType.StepOffDistance) != 0L)
					topology.preGenerateDistanceToEachElementToEachOther(type, RelationType.OffDiagonal);
				else if ((gameFlags & GameType.StepDiagonalDistance) != 0L)
					topology.preGenerateDistanceToEachElementToEachOther(type, RelationType.Diagonal);
				else if ((gameFlags & GameType.StepOrthogonalDistance) != 0L)
					topology.preGenerateDistanceToEachElementToEachOther(type, RelationType.Orthogonal);

				// We compute the edges crossing each other.
				topology.computeDoesCross();
			}
			
			if (e == 0)
				topology.pregenerateFeaturesData(game, cont);
			
			cont.setIndex(e);
			index += (cont.index() == 0)
					? Math.max(cont.topology().cells().size(),
							cont.topology().getGraphElements(cont.defaultSite()).size())
					: cont.numSites();
			topology.optimiseMemory();
		}

		// INIT TOTAL SITES
		for (final Container cont : containers)
			totalDefaultSites += cont.numSites();

		final int maxSiteMainBoard = Math.max(containers[0].topology().cells().size(),
				containers[0].topology().getGraphElements(containers[0].defaultSite()).size());
		
		int fakeTotalDefaultSite = maxSiteMainBoard;

		for (int i = 1; i < containers.length; i++)
			fakeTotalDefaultSite += containers[i].topology().cells().size();

		// INIT OFFSET
		offset = new int[fakeTotalDefaultSite];
		int accumulatedOffset = 0;
		for (int i = 0; i < containers.length;i++)
		{
			final Container cont = containers[i];
			if (i == 0)
			{
				for (int j = 0; j < maxSiteMainBoard; ++j)
				{
					offset[j + accumulatedOffset] = j;
				}

				accumulatedOffset += maxSiteMainBoard;
			}
			else
			{
				for (int j = 0; j < cont.numSites(); ++j)
				{
					offset[j + accumulatedOffset] = j;
				}

				accumulatedOffset += cont.numSites();
			}
		}

		sitesFrom = new int[containers.length];
		// INIT sitesFROM
		int count = 0;
		for (int i = 0; i < containers.length; i++)
		{
			sitesFrom[i] = count;
			count += (i == 0) ? maxSiteMainBoard : containers[i].numSites();
		}

		// INIT CONTAINER ID
		containerId = new int[fakeTotalDefaultSite];
		count = 0;
		int idBoard = 0;
		for (int i = 0; i < containers.length - 1; i++)
		{
			for (int j = sitesFrom[i]; j < sitesFrom[i + 1]; j++)
			{
				containerId[count] = idBoard;
				count++;
			}
			idBoard++;
		}
		for (int j = sitesFrom[sitesFrom.length - 1]; j < sitesFrom[sitesFrom.length - 1]
				+ containers[idBoard].numSites(); j++)
		{
			containerId[count] = idBoard;
			count++;
		}
	}

	/**
	 * @return List of all static regions in this equipment
	 */
	public List<Regions> computeStaticRegions()
	{
		final List<Regions> staticRegions = new ArrayList<Regions>();

		for (final Regions region : regions)
		{
			if (region.region() != null)
			{
				boolean allStatic = true;

				for (final RegionFunction regionFunc : region.region())
				{
					if (!regionFunc.isStatic())
					{
						allStatic = false;
						break;
					}
				}

				if (!allStatic)
					continue;
			}
			else if (region.sites() == null)
			{
				continue;
			}

			staticRegions.add(region);
		}

		return staticRegions;
	}

	//----------------------Getters--------------------------------------------

	/**
	 * @return Array of containers.
	 */
	public Container[] containers()
	{
		return containers;
	}

	/**
	 * @return Array of component.
	 */
	public Component[] components()
	{
		return components;
	}

	/**
	 * Clear the components. Will always keep a null entry at index 0.
	 */
	public void clearComponents()
	{
		components = new Component[]{null};
	}
	
	/**
	 * @return Array of regions.
	 */
	public Regions[] regions()
	{
		return regions;
	}

	/**
	 * @return Array of maps.
	 */
	public Map[] maps()
	{
		return maps;
	}

	/**
	 * @return Total number of default sites over all containers.
	 */
	public int totalDefaultSites()
	{
		return totalDefaultSites;
	}

	/**
	 * @return Which container a given accumulated site index refers to.
	 */
	public int[] containerId()
	{
		return containerId;
	}

	/**
	 * @return Which actual site within its container a given accumulated site index
	 *         refers to.
	 */
	public int[] offset()
	{
		return offset;
	}

	/**
	 * @return Which accumulated site index a given container starts at.
	 */
	public int[] sitesFrom()
	{
		return sitesFrom;
	}

	/**
	 * @return The vertices with hints.
	 */
	public Integer[][] verticesWithHints()
	{
		return vertexWithHints;
	}

	/**
	 * To set the vertices with hints.
	 * 
	 * @param regionWithHints
	 */
	public void setVertexWithHints(final Integer[][] regionWithHints)
	{
		vertexWithHints = regionWithHints;
	}

	/**
	 * @return The cells with hints.
	 */
	public Integer[][] cellsWithHints()
	{
		return cellWithHints;
	}

	/**
	 * To set the cells with hints.
	 * 
	 * @param cellWithHints
	 */
	public void setCellWithHints(final Integer[][] cellWithHints)
	{
		this.cellWithHints = cellWithHints;
	}

	/**
	 * @return The edges with hints.
	 */
	public Integer[][] edgesWithHints()
	{
		return edgeWithHints;
	}

	/**
	 * To set the edges with hints.
	 * 
	 * @param edgeWithHints
	 */
	public void setEdgeWithHints(final Integer[][] edgeWithHints)
	{
		this.edgeWithHints = edgeWithHints;
	}

	/**
	 * @return The hints of each vertex.
	 */
	public Integer[] vertexHints()
	{
		return vertexHints;
	}

	/**
	 * To set the hints of the vertices.
	 * 
	 * @param hints
	 */
	public void setVertexHints(final Integer[] hints)
	{
		vertexHints = hints;
	}

	/**
	 * @return The hints of each cell.
	 */
	public Integer[] cellHints()
	{
		return cellHints;
	}

	/**
	 * To set the hints of the cells.
	 * 
	 * @param hints
	 */
	public void setCellHints(final Integer[] hints)
	{
		cellHints = hints;
	}

	/**
	 * @return The hints of each edge.
	 */
	public Integer[] edgeHints()
	{
		return edgeHints;
	}

	/**
	 * To set the hints of the edges.
	 * 
	 * @param hints
	 */
	public void setEdgeHints(final Integer[] hints)
	{
		edgeHints = hints;
	}

	/**
	 * @param type The SiteType.
	 * @return The hints corresponding to the type.
	 */
	public Integer[] hints(final SiteType type)
	{
		switch (type)
		{
		case Edge:
			return edgeHints();
		case Vertex:
			return vertexHints();
		case Cell:
			return cellHints();
		}
		return new Integer[0];
	}

	/**
	 * @param type The SiteType.
	 * @return The regions with hints corresponding to the type.
	 */
	public Integer[][] withHints(final SiteType type)
	{
		switch (type)
		{
		case Edge:
			return edgesWithHints();
		case Vertex:
			return verticesWithHints();
		case Cell:
			return cellsWithHints();
		}
		return new Integer[0][0];
	}
}
