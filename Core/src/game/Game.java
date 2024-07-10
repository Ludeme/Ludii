package game;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.rng.RandomProviderState;

import annotations.Hide;
import annotations.Opt;
import game.equipment.Equipment;
import game.equipment.Item;
import game.equipment.component.Component;
import game.equipment.container.Container;
import game.equipment.container.board.Board;
import game.equipment.container.board.Track;
import game.equipment.container.other.Deck;
import game.equipment.container.other.Dice;
import game.equipment.other.Regions;
import game.functions.booleans.BooleanFunction;
import game.functions.booleans.deductionPuzzle.ForAll;
import game.functions.booleans.is.Is;
import game.functions.booleans.is.IsLineType;
import game.functions.dim.DimConstant;
import game.functions.graph.generators.basis.square.RectangleOnSquare;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.functions.ints.state.Mover;
import game.functions.region.RegionFunction;
import game.functions.region.sites.index.SitesEmpty;
import game.match.Subgame;
import game.mode.Mode;
import game.players.Player;
import game.players.Players;
import game.rules.Rules;
import game.rules.end.End;
import game.rules.end.Result;
import game.rules.meta.Automove;
import game.rules.meta.Gravity;
import game.rules.meta.MetaRule;
import game.rules.meta.NoStackOn;
import game.rules.meta.Pin;
import game.rules.meta.Swap;
import game.rules.phase.NextPhase;
import game.rules.phase.Phase;
import game.rules.play.Play;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.decision.MoveSiteType;
import game.rules.play.moves.nonDecision.effect.Add;
import game.rules.play.moves.nonDecision.effect.Pass;
import game.rules.play.moves.nonDecision.effect.Satisfy;
import game.rules.play.moves.nonDecision.effect.Then;
import game.rules.play.moves.nonDecision.effect.requirement.Do;
import game.rules.start.StartRule;
import game.rules.start.place.item.PlaceItem;
import game.rules.start.place.stack.PlaceCustomStack;
import game.types.board.RegionTypeStatic;
import game.types.board.RelationType;
import game.types.board.SiteType;
import game.types.play.ModeType;
import game.types.play.ResultType;
import game.types.play.RoleType;
import game.types.state.GameType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.DirectionFacing;
import game.util.equipment.Region;
import game.util.moves.To;
import gnu.trove.list.array.TIntArrayList;
import graphics.ImageUtil;
import main.Constants;
import main.ReflectionUtils;
import main.Status;
import main.Status.EndType;
import main.collections.FastArrayList;
import main.grammar.Description;
import main.options.Ruleset;
import metadata.Metadata;
import metadata.recon.ReconItem;
import other.AI;
import other.BaseLudeme;
import other.Ludeme;
import other.MetaRules;
import other.action.Action;
import other.action.others.ActionPass;
import other.concept.Concept;
import other.concept.ConceptDataType;
import other.context.Context;
import other.context.TempContext;
import other.model.Model;
import other.move.Move;
import other.playout.PlayoutAddToEmpty;
import other.playout.PlayoutFilter;
import other.playout.PlayoutMoveSelector;
import other.playout.PlayoutNoRepetition;
import other.state.State;
import other.state.container.ContainerState;
import other.topology.SiteFinder;
import other.topology.Topology;
import other.topology.TopologyElement;
import other.translation.LanguageUtils;
import other.trial.Trial;

/**
 * Defines the main ludeme that describes the players, mode, equipment and rules of a game.
 *
 * @author Eric.Piette and cambolbro 
 */
public class Game extends BaseLudeme implements API, Serializable
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Game name. */
	protected final String name;
	
	/** Selected options. */
	private List<String> options = new ArrayList<>();

	/** Game control. */
	protected final Mode mode;

	/** The players of the game. */
	protected final Players players;

	/** Game's equipment. */
	protected Equipment equipment;

	/** Game's rules. */
	private Rules rules;
	
	/** Store which meta rule is activated or not. */
	private final MetaRules metaRules = new MetaRules();

	/** Table of Strings that can be used for voting mechanisms. Populated by preprocess() calls */
	private final List<String> voteStringsTable = new ArrayList<String>();

	/** Current game description. */
	private Description description = new Description("Unprocessed");
	
	/** Maximum number of turns for this game */
	protected int maxTurnLimit = Constants.DEFAULT_TURN_LIMIT;
	
	/** Maximum number of moves for this game */
	protected int maxMovesLimit = Constants.DEFAULT_MOVES_LIMIT;

	//-----------------------------State/Context-------------------------------

	/** The number of starting actions done during the initialisation of the game. */
	private int numStartingAction = 0;
	
	/** Flags corresponding to the gameType of this game. */
	private long gameFlags;

	/** Flags corresponding to the boolean concepts of this game. */
	protected BitSet booleanConcepts;

	/** Map corresponding to the non boolean concepts of this game. */
	protected Map<Integer, String> conceptsNonBoolean;

	/** Reference state type, for creating new versions of appropriate type. */
	protected State stateReference;
	
	/** Set to true once we've finished preprocessing */
	protected boolean finishedPreprocessing = false;
	
	/** Copy of the starting context for games with no stochastic element in the starting rules. */
	private Context startContext;
	
	/** True if some stochastic elements are in the starting rules. */
	private boolean stochasticStartingRules = false;

	//-----------------------------Shortcuts-----------------------------------

	/** Access container by label. */
	private final Map<String, Container> mapContainer = new HashMap<>();

	/** Access component by label. */
	private final Map<String, Component> mapComponent = new HashMap<>();

	/** The list of the different sets of dice. */
	private final List<Dice> handDice = new ArrayList<>();

	/** The list of the different decks. */
	private final List<Deck> handDeck = new ArrayList<>();
	
	/** All variables constraint by the puzzle.*/
	private final TIntArrayList constraintVariables = new TIntArrayList();

	//-----------------------Metadata-------------------------------------------

	/** The game's metadata */
	protected Metadata metadata = null;

	/** The expected concepts values for reconstruction. */
	protected ArrayList<metadata.recon.concept.Concept> expectedConcepts = new ArrayList<metadata.recon.concept.Concept>();

	// -----------------------Warning/Crash reports-----------------------------

	/** The report with all the warning due of some missing required ludemes. */
	private final List<String> requirementReport = new ArrayList<String>();

	/** True if any required ludeme is missing. */
	protected boolean hasMissingRequirement;

	/** The report with all the crashes due of some ludemes used badly. */
	private final List<String> crashReport = new ArrayList<String>();

	/** True if any ludeme can crash the game. */
	protected boolean willCrash;

	//---------------------------------AI--------------------------------------

	/**
	 * Simply counts how often we've called start() on this game object.
	 * Used to avoid unnecessary re-initialisations of AIs.
	 */
	private int gameStartCount = 0;

	//-------------------------------------------------------------------------

	/**
	 * @param name      The name of the game.
	 * @param players   The players of the game.
	 * @param mode      The mode of the game [Alternating].
	 * @param equipment The equipment of the game.
	 * @param rules     The rules of the game.
	 * @example (game "Tic-Tac-Toe" (players 2) (equipment { (board (square 3))
	 *          (piece "Disc" P1) (piece "Cross" P2) }) (rules (play (move Add (to
	 *          (sites Empty)))) (end (if (is Line 3) (result Mover Win))) ) )
	 * 
	 */
	public Game
	(
				final String 	name,
				final Players   players,
		@Opt    final Mode 		mode,
			    final Equipment equipment,
			    final Rules 	rules
	)
	{
		this.name = new String(name);

		this.players = (players == null) ? new Players(Integer.valueOf(Constants.DEFAULT_NUM_PLAYERS)) : players;

		if (this.players.count() == 0)
			this.mode = new Mode(ModeType.Simulation);
		else if (this.players.count() == 1)
			this.mode = new Mode(ModeType.Alternating);
		else if (mode != null)
			this.mode = mode;
		else
			this.mode = new Mode(ModeType.Alternating);

		this.equipment = equipment;
		this.rules = rules;
	}

	/**
	 * Bare skeleton loader for distance metric tests.
	 * 
	 * @param name            The name of the game.
	 * @param gameDescription The game description.
	 */
	@Hide
	public Game(final String name, final Description gameDescription)
	{
		this.name = new String(name);
		description = gameDescription;
		mode = null;
		players = null;
		equipment = null;
		rules = null;
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		if (game.hasSubgames())
			return "Sorry! Matches are not yet supported.";
		
		String playersString = players.toEnglish(game);
		playersString = LanguageUtils.NumberAsText(players.count(), "player", "players") + (playersString.isEmpty() ? "" : ": (" + playersString + ")");

		final String equipmentString = equipment.toEnglish(game);
		final String rulesString = rules.toEnglish(game);
		
		String turnFormatString = "Players take turns moving.";
		if (game.isSimultaneousMoveGame())
			turnFormatString = "Players moves at the same time.";

		return String.format("The game \"%s\" is played by %s %s\n%s\n%s\n\n", 
				name, 
				playersString,
				equipmentString,
				turnFormatString,
				rulesString); 
	}
	
	//----------------------------Getter and Shortcuts-----------------------------------

	/**
	 * @return Game name.
	 */
	public String name()
	{
		return name;
	}

	/**
	 * @return The game's metadata
	 */
	public Metadata metadata()
	{
		return metadata;
	}
	
	/**
	 * @return The game's expected concepts used for reconstruction.
	 */
	public ArrayList<metadata.recon.concept.Concept> expectedConcepts()
	{
		return expectedConcepts;
	}
	
	/**
	 * Sets the game's metadata and update the concepts.
	 * @param md The metadata.
	 */
	public void setMetadata(final Object md)
	{
		metadata = (Metadata)md;

		// We add the concepts of the metadata to the game.
		if (metadata != null)
		{
			final BitSet metadataConcept = metadata.concepts(this);
			booleanConcepts.or(metadata.concepts(this));
			final boolean stackTypeUsed = booleanConcepts.get(Concept.StackType.id());
			if 
			(
				stackTypeUsed 
				&& 
				!metadataConcept.get(Concept.Stack.id()) 
				&& 
				booleanConcepts.get(Concept.StackState.id())
			)
			   booleanConcepts.set(Concept.Stack.id(), false);

			if 
			(
				booleanConcepts.get(Concept.MancalaBoard.id()) 
				&& 
				booleanConcepts.get(Concept.Track.id())
			)
			{
				final Container board = equipment.containers()[0];
				final Topology topology = board.topology();
				for (final Track track : board.tracks())
				{
					final int firstSite = track.elems()[0].site;
					final int nextSite = track.elems()[0].next;
					final List<DirectionFacing> directionsSupported = topology.supportedDirections(RelationType.All, SiteType.Vertex);
					for (final DirectionFacing facingDirection : directionsSupported)
					{
						final AbsoluteDirection absDirection = facingDirection.toAbsolute();
						final List<game.util.graph.Step> steps = topology.trajectories().steps(SiteType.Vertex, firstSite, SiteType.Vertex, absDirection);

						for (final game.util.graph.Step step : steps)
						{
							final int to = step.to().id();
							if (nextSite == to)
							{
								if 
								(
									absDirection.equals(AbsoluteDirection.N) 
									|| 
									absDirection.equals(AbsoluteDirection.E)
									|| 
									absDirection.equals(AbsoluteDirection.CCW)
								)
									booleanConcepts.set(Concept.SowCCW.id(), true);
								else if 
								(
									absDirection.equals(AbsoluteDirection.S)
									|| 
									absDirection.equals(AbsoluteDirection.W)
									|| 
									absDirection.equals(AbsoluteDirection.CW)
								)
									booleanConcepts.set(Concept.SowCW.id(), true);
							}
						}
					}
				}
			}

			final long gameFlagsWithMetadata = computeGameFlags(); // NOTE: need compute here!

			for (final SiteType type : SiteType.values())
			{
				// We compute the step distance only if needed by the game.
				if 
				(
					(gameFlagsWithMetadata & GameType.StepAdjacentDistance) != 0L
					&& 
					board().topology().distancesToOtherSite(type) == null
				)
					board().topology().preGenerateDistanceToEachElementToEachOther(type, RelationType.Adjacent);
				else if 
				(
					(gameFlagsWithMetadata & GameType.StepAllDistance) != 0L
					&& 
					board().topology().distancesToOtherSite(type) == null
				)
					board().topology().preGenerateDistanceToEachElementToEachOther(type, RelationType.All);
				else if 
				(
					(gameFlagsWithMetadata & GameType.StepOffDistance) != 0L
					&& 
					board().topology().distancesToOtherSite(type) == null
				)
					board().topology().preGenerateDistanceToEachElementToEachOther(type, RelationType.OffDiagonal);
				else if 
				(
					(gameFlagsWithMetadata & GameType.StepDiagonalDistance) != 0L
					&& 
					board().topology().distancesToOtherSite(type) == null
				)
					board().topology().preGenerateDistanceToEachElementToEachOther(type, RelationType.Diagonal);
				else if 
				(
					(gameFlagsWithMetadata & GameType.StepOrthogonalDistance) != 0L
					&& 
					board().topology().distancesToOtherSite(type) == null
				)
					board().topology().preGenerateDistanceToEachElementToEachOther(type, RelationType.Orthogonal);
			}

			if (metadata.graphics() != null)
				metadata.graphics().computeNeedRedraw(this);
			
			if(metadata.recon() != null)
			{
				final List<ReconItem> reconItems = metadata.recon().getItem();
				for(ReconItem item : reconItems)
					expectedConcepts.add((metadata.recon.concept.Concept) item);
			}
		}
		else
		{
			metadata = new Metadata(null, null, null, null);
		}
	}
	
	/**
	 * @return Game control.
	 */
	public Mode mode()
	{
		return mode;
	}

	/**
	 * @return Players record.
	 */
	public Players players()
	{
		return players;
	}

	/**
	 * @return Game's equipment.
	 */
	public Equipment equipment()
	{
		return equipment;
	}

	/**
	 * @return Game's rules.
	 */
	public Rules rules()
	{
		return rules;
	}

	/**
	 * @return The meta rules of the game which are activated.
	 */
	public MetaRules metaRules()
	{
		return metaRules;
	}

	/**
	 * @return The different instances of a match.
	 */
	@SuppressWarnings("static-method")
	public Subgame[] instances()
	{
		return null;
	}

	/**
	 * @return Master Container map.
	 */
	public Map<String, Container> mapContainer()
	{
		return Collections.unmodifiableMap(mapContainer);
	}
	
	/**
	 * @return Number of distinct containers.
	 */
	public int numContainers()
	{
		return equipment().containers().length;
	}

	/**
	 * @return Number of distinct components.
	 */
	public int numComponents()
	{
		return equipment().components().length - 1;
	}

	/**
	 * @param nameC
	 * @return A component object from its name.
	 */
	public Component getComponent(final String nameC)
	{
		return mapComponent.get(nameC);
	}

	/**
	 * @return Reference to main board object.
	 */
	public Board board()
	{
		// Assume board is always at position 0
		return (Board) equipment().containers()[0];
	}

	/**
	 * @return constraintVariables
	 */
	public TIntArrayList constraintVariables()
	{
		return constraintVariables;
	}

	/**
	 * @return The dice hands the game.
	 */
	public List<Dice> handDice()
	{
		return Collections.unmodifiableList(handDice);
	}

	/**
	 * @return The hand decks of the game.
	 */
	public List<Deck> handDeck()
	{
		return Collections.unmodifiableList(handDeck);
	}

	/**
	 * @param index
	 * @return To get a specific hand dice.
	 */
	public Dice getHandDice(final int index)
	{
		return handDice.get(index);
	}

	/**
	 * @return The description of the game.
	 */
	public Description description()
	{
		return description;
	}

	/**
	 * Set the description of the game.
	 * 
	 * @param gd The description.
	 */
	public void setDescription(final Description gd)
	{
		description = gd;
	}
	
	/**
	 * @param trackName Name of a track.
	 * @return Unique index of the track with given name (-1 if no such track
	 *         exists)
	 */
	public int trackNameToIndex(final String trackName)
	{
		final List<Track> tracks = board().tracks();
		for (int i = 0; i < tracks.size(); ++i)
			if (tracks.get(i).name().equals(trackName))
				return i;
		
		return Constants.UNDEFINED;
	}
	
	/**
	 * @param voteInt The index of the vote.
	 * @return String representation of vote for given int
	 */
	public String voteString(final int voteInt)
	{
		// NOTE: if we receive a -1 index here, that means that preprocess()
		// was not properly called on some vote-related ludeme!
		return voteStringsTable.get(voteInt);
	}
	
	/**
	 * Registers a given String as something that players can vote on.
	 * 
	 * @param voteString The vote.
	 * @return int representation of the corresponding vote
	 */
	public int registerVoteString(final String voteString)
	{
		for (int i = 0; i < voteStringsTable.size(); ++i)
			if (voteString.equals(voteStringsTable.get(i)))
				return i;	// Already registered
		
		voteStringsTable.add(voteString);
		return voteStringsTable.size() - 1;
	}
	
	/**
	 * @return Number of different vote strings we have in this game
	 */
	public int numVoteStrings()
	{
		return voteStringsTable.size();
	}
	
	//---------------------Properties of the game------------------------------

	/**
	 * @return True is the game uses instance.
	 */
	@SuppressWarnings("static-method")
	public boolean hasSubgames()
	{
		return false;
	}

	/**
	 * @return True if we're an alternating-move game.
	 */
	public boolean isAlternatingMoveGame()
	{
		return mode.mode().equals(ModeType.Alternating);
	}

	/**
	 * @return True if we're a simultaneous-move game.
	 */
	public boolean isSimultaneousMoveGame()
	{
		return mode.mode().equals(ModeType.Simultaneous);
	}

	/**
	 * @return True if we're a simulation-move game.
	 */
	public boolean isSimulationMoveGame()
	{
		return mode.mode().equals(ModeType.Simulation);
	}

	/**
	 * @return True if we're a stochastic (i.e. not deterministic) game.
	 */
	public boolean isStochasticGame()
	{
		return ((gameFlags & GameType.Stochastic) != 0L);
	}

	/**
	 * @return True if the game uses some piece values.
	 */
	public boolean requiresPieceValue()
	{
		return ((gameFlags & GameType.Value) != 0L) || ((gameFlags & GameType.Dominoes) != 0L);
	}

	/**
	 * @return True if the game involves some moves applied to vertices or edges.
	 */
	public boolean isGraphGame()
	{
		return ((gameFlags & GameType.Graph) != 0L);
	}

	/**
	 * @return True if the game involves some vertices moves.
	 */
	public boolean isVertexGame()
	{
		return ((gameFlags & GameType.Vertex) != 0L);
	}

	/**
	 * @return True if the game involves some edges moves.
	 */
	public boolean isEdgeGame()
	{
		return ((gameFlags & GameType.Edge) != 0L);
	}

	/**
	 * @return True if the game involves some faces moves.
	 */
	public boolean isCellGame()
	{
		return ((gameFlags & GameType.Cell) != 0L);
	}

	/**
	 * @return True if this is a puzzle.
	 */
	public boolean isDeductionPuzzle()
	{
		return ((gameFlags() & GameType.DeductionPuzzle) != 0L);
	}

	/**
	 * @return True if this is game using a vote system.
	 */
	public boolean usesVote()
	{
		return ((gameFlags() & GameType.Vote) != 0L);
	}

	/**
	 * @return True if this is game using a note system.
	 */
	public boolean usesNote()
	{
		return ((gameFlags() & GameType.Note) != 0L);
	}

	/**
	 * @return True if this game used a ludeme in relation with visited.
	 */
	public boolean requiresVisited()
	{
		return ((gameFlags() & GameType.Visited) != 0L);
	}

	/**
	 * @return True if this game uses a score.
	 */
	public boolean requiresScore()
	{
		return ((gameFlags() & GameType.Score) != 0L);
	}

	/**
	 * @return True if this game uses a payoff.
	 */
	public boolean requiresPayoff()
	{
		return ((gameFlags() & GameType.Payoff) != 0L);
	}

	/**
	 * @return True if this game used an amount.
	 */
	public boolean requiresBet()
	{
		return ((gameFlags() & GameType.Bet) != 0L);
	}

	/**
	 * @return True if this game uses a local state.
	 */
	public boolean requiresLocalState()
	{
		return ((gameFlags() & GameType.SiteState) != 0L) || hasLargePiece();
	}

	/**
	 * @return True if this game uses a rotation state.
	 */
	public boolean requiresRotation()
	{
		return ((gameFlags() & GameType.Rotation) != 0L);
	}

	/**
	 * @return True if this game uses some teams.
	 */
	public boolean requiresTeams()
	{
		return ((gameFlags() & GameType.Team) != 0L);
	}

	/**
	 * @return True if this game needs a track cache.
	 */
	public boolean needTrackCache()
	{
		return ((gameFlags() & GameType.Track) != 0L);
	}

	/**
	 * @return True if this game uses a comparison of positional states
	 */
	public boolean usesNoRepeatPositionalInGame()
	{
		return ((gameFlags() & GameType.RepeatPositionalInGame) != 0L);
	}

	/**
	 * @return True if this game uses a comparison of positionalstates within a turn
	 */
	public boolean usesNoRepeatPositionalInTurn()
	{
		return ((gameFlags() & GameType.RepeatPositionalInTurn) != 0L);
	}

	/**
	 * @return True if this game uses a comparison of situational states
	 */
	public boolean usesNoRepeatSituationalInGame()
	{
		return ((gameFlags() & GameType.RepeatSituationalInGame) != 0L);
	}

	/**
	 * @return True if this game uses a comparison of situational states within a
	 *         turn
	 */
	public boolean usesNoRepeatSituationalInTurn()
	{
		return ((gameFlags() & GameType.RepeatSituationalInTurn) != 0L);
	}

	/**
	 * @return True if the game involved a sequence of capture.
	 */
	public boolean hasSequenceCapture()
	{
		return ((gameFlags & GameType.SequenceCapture) != 0L);
	}
	
	/**
	 * @return True if the game involved a cycle detection.
	 */
	public boolean hasCycleDetection()
	{
		return ((gameFlags & GameType.CycleDetection) != 0L);
	}
	
	/**
	 * @return True if the game involved a sequence of capture.
	 */
	public boolean hasLargeStack()
	{
		return board().largeStack();
	}

	/**
	 * @return True if this game uses a local state.
	 */
	public boolean requiresCount()
	{
		if(isStacking())
			return false;
		for (final Container c : equipment.containers())
			if (c.isHand())
				return true;
		return ((gameFlags() & GameType.Count) != 0L);
	}

	/**
	 * @return True if some information is hidden.
	 */
	public boolean hiddenInformation()
	{
		return ((gameFlags() & GameType.HiddenInfo) != 0L);
	}

	/**
	 * @return Whether we don't need to check all pass.
	 */
	public boolean requiresAllPass()
	{
		return ((gameFlags() & GameType.NotAllPass) == 0L) && mode.mode() != ModeType.Simulation;
	}

	/**
	 * @return True if any component is a card.
	 */
	public boolean hasCard()
	{
		return ((gameFlags & GameType.Card) != 0L);
	}

	/**
	 * @return True if any track has an internal loop in a track.
	 */
	public boolean hasInternalLoopInTrack()
	{
		return ((gameFlags & GameType.InternalLoopInTrack) != 0L);
	}

	/**
	 * @return True if any component is a large piece.
	 */
	public boolean hasLargePiece()
	{
		return ((gameFlags & GameType.LargePiece) != 0L);
	}

	/**
	 * @return true if the game is a stacking game
	 */
	public boolean isStacking()
	{
		return (gameFlags() & GameType.Stacking) != 0L || hasCard() || board().largeStack();
	}

	/**
	 * @return true if the game uses a line of Play
	 */
	public boolean usesLineOfPlay()
	{
		return (gameFlags() & GameType.LineOfPlay) != 0L;
	}

	/**
	 * @return true if the game uses (moveAgain) ludeme.
	 */
	public boolean usesMoveAgain()
	{
		return (gameFlags() & GameType.MoveAgain) != 0L;
	}

	/**
	 * @return true if the game uses some pending values/states.
	 */
	public boolean usesPendingValues()
	{
		return (gameFlags() & GameType.PendingValues) != 0L;
	}

	/**
	 * @return true if the game uses some values mapped.
	 */
	public boolean usesValueMap()
	{
		return (gameFlags() & GameType.MapValue) != 0L;
	}

	/**
	 * @return true if the game uses some remembering values.
	 */
	public boolean usesRememberingValues()
	{
		return (gameFlags() & GameType.RememberingValues) != 0L;
	}

	/**
	 * @return True if the game is boardless.
	 */
	public boolean isBoardless()
	{
		return board().isBoardless();
	}

	/**
	 * @return True if the game involved some dice hands.
	 */
	public boolean hasHandDice()
	{
		return !handDice.isEmpty();
	}

	/**
	 * @return True if the game involved some tracks.
	 */
	public boolean hasTrack()
	{
		return !board().tracks().isEmpty();
	}

	/**
	 * @return True if the game has dominoes
	 */
	public boolean hasDominoes()
	{
		for (int i = 1; i < equipment.components().length; i++)
			if (equipment.components()[i].name().contains("Domino"))
				return true;

		return false;
	}

	/**
	 * @return True if the game uses some decks.
	 */
	public boolean hasHandDeck()
	{
		return !handDeck.isEmpty();
	}

	/**
	 * @return True if the game requires a hand.
	 */
	public boolean requiresHand()
	{
		for (final Container c : equipment.containers())
			if (c.isHand())
				return true;

		return false;
	}

	/**
	 * @return True if this game uses at least one custom playout strategy in any of
	 *         its phases.
	 */
	public boolean hasCustomPlayouts()
	{
		if (mode().playout() != null)
			return true;

		for (final Phase phase : rules().phases())
			if (phase.playout() != null)
				return true;

		return false;
	}

	//-------------------------State and Context-------------------------------

	/**
	 * @return Reference copy of initial state.
	 */
	public State stateReference()
	{
		return stateReference;
	}

	/**
	 * @return Whether game state requires item indices to be stored. e.g. for ko or
	 *         superko testing.
	 */
	public boolean requiresItemIndices()
	{
		if ((players.count() + 1) < equipment().components().length)
			return true;

		for (int numPlayer = 0; numPlayer < players.count() + 1; numPlayer++)
		{
			int nbComponent = 0;
			for (int i = 1; i < equipment().components().length; i++)
			{
				final Component c = equipment().components()[i];
				if (c.owner() == numPlayer)
					nbComponent++;
				if (nbComponent > 1)
					return true;
			}
		}
		return false;
	}

	/**
	 * @return The maximum count we need for sites with count.
	 */
	public int maxCount()
	{
		if(isStacking())
			return 1;
		
		if (hasDominoes())
			return equipment.components().length;

		int counter = 0;
		if (rules != null && rules.start() != null)
			for (final StartRule s : rules.start().rules())
				counter += s.count(this) * s.howManyPlace(this);

		int maxCountFromComponent = 0;
		for (int i = 1; i < equipment.components().length; i++)
		{
			final Component component = equipment.components()[i];
			if (component.maxCount() > maxCountFromComponent)
				maxCountFromComponent = component.maxCount();
		}

		return Math.max(maxCountFromComponent, Math.max(counter, equipment.totalDefaultSites()));
	}

	/**
	 * @return The maximum local states possible for all the items.
	 */
	public int maximalLocalStates()
	{
		int maxLocalState = 2;

		boolean localStateToCompute = true;

		if (rules != null && rules.start() != null)
		{
			for (final StartRule s : rules.start().rules())
			{
				final int state = s.state(this);
				if (maxLocalState < state)
				{
					maxLocalState = state;
					localStateToCompute = false;
				}
			}
		}

		for (int i = 1; i < equipment.components().length; i++)
		{
			final Component c = equipment.components()[i];
			if (c.isDie())
			{
				if (c.getNumFaces() > maxLocalState)
				{
					maxLocalState = c.getNumFaces();
					localStateToCompute = false;
				}
			}
			else if (c.isLargePiece())
			{
				final int numSteps = c.walk().length * 4;
				if (numSteps > maxLocalState)
				{
					maxLocalState = numSteps;
					localStateToCompute = false;
				}
			}
		}

		int maxLocalStateFromComponent = 0;
		for (int i = 1; i < equipment.components().length; i++)
		{
			final Component component = equipment.components()[i];
			if (component.maxState() > maxLocalStateFromComponent)
				maxLocalStateFromComponent = component.maxState();
		}

		if (localStateToCompute)
			return Math.max(maxLocalStateFromComponent, players().size());

		return Math.max(maxLocalStateFromComponent, maxLocalState);
	}

	/**
	 * @return The maximum possible values for all the items.
	 */
	public int maximalValue()
	{
		int maxValueFromComponent = 0;
		for (int i = 1; i < equipment.components().length; i++)
		{
			final Component component = equipment.components()[i];
			if (component.maxValue() > maxValueFromComponent)
				maxValueFromComponent = component.maxValue();
		}

		return Math.max(Constants.MAX_VALUE_PIECE, maxValueFromComponent);
	}

	/**
	 * @return The rotation states possible for all the items.
	 */
	public int maximalRotationStates()
	{
		return Math.max(
				equipment().containers()[0].topology().supportedDirections(SiteType.Cell).size(),
				equipment().containers()[0].topology().supportedDirections(SiteType.Vertex).size()
				);
	}

	/**
	 * @return True if the game uses automove.
	 */
	public boolean automove()
	{
		return metaRules.automove();
	}

	/**
	 * @return List of all the elements that we play on in this game's
	 * graph (currently returns vertices if we play on vertices, or cells
	 * otherwise).
	 */
	public List<? extends TopologyElement> graphPlayElements()
	{
		switch (board().defaultSite())
		{
		case Cell:
			return board().topology().cells();
		case Edge:
			return board().topology().edges();
		case Vertex:
			return board().topology().vertices();
		}
		
		return null;
	}
	
	/**
	 * @return Precomputed table of distances to centre; automatically picks 
	 * either the table for vertices, or the table for cells, based on whether
	 * or not the game is played on intersections
	 */
	public int[] distancesToCentre()
	{
		if (board().defaultSite() == SiteType.Vertex)
			return board().topology().distancesToCentre(SiteType.Vertex);
		else
			return board().topology().distancesToCentre(SiteType.Cell);
	}
	
	/**
	 * @return Precomputed table of distances to corners; automatically picks 
	 * either the table for vertices, or the table for cells, based on whether
	 * or not the game is played on intersections
	 */
	public int[] distancesToCorners()
	{
		if (board().defaultSite() == SiteType.Vertex)
			return board().topology().distancesToCorners(SiteType.Vertex);
		else
			return board().topology().distancesToCorners(SiteType.Cell);
	}
	
	/**
	 * @return Precomputed tables of distances to regions; automatically picks
	 * either the table for vertices, or the table for cells, based on whether
	 * or not the game is played on intersections
	 */
	public int[][] distancesToRegions()
	{
		if (board().defaultSite() == SiteType.Vertex)
			return board().topology().distancesToRegions(SiteType.Vertex);
		else
			return board().topology().distancesToRegions(SiteType.Cell);
	}
	
	/**
	 * @return Precomputed table of distances to sides; automatically picks 
	 * either the table for vertices, or the table for cells, based on whether
	 * or not the game is played on intersections
	 */
	public int[] distancesToSides()
	{
		if (board().defaultSite() == SiteType.Vertex)
			return board().topology().distancesToSides(SiteType.Vertex);
		else
			return board().topology().distancesToSides(SiteType.Cell);
	}

	//-----------------------------------AI------------------------------------

	/**
	 * @return How often have we called start() on this Game object?
	 */
	public int gameStartCount()
	{
		return gameStartCount;
	}

	/**
	 * To increment the game start counter.
	 */
	public void incrementGameStartCount()
	{
		gameStartCount += 1;
	}

	//----------------------------------Flags----------------------------------

	/**
	 * To remove a flag. Only used in case of special computation (e.g. sequence of
	 * capture).
	 *
	 * @param flag
	 */
	public void removeFlag(final long flag)
	{
		gameFlags -= flag;
	}

	/**
	 * To add a flag. Only used in case of special computation (e.g. sequence of
	 * capture).
	 *
	 * @param flag
	 */
	public void addFlag(final long flag)
	{
		gameFlags |= flag;
	}

	/**
	 * @return Game flags (computed based on rules).
	 */
	public long computeGameFlags()
	{
		long flags = 0L;

		try
		{
			flags |= SiteType.gameFlags(board().defaultSite());
			
			// If any hand, dice or deck, we also need the cells.
			if (equipment().containers().length > 1)
				flags |= GameType.Cell;

			// Accumulate flags for all the containers.
			for (int i = 0; i < equipment().containers().length; i++)
				flags |= equipment().containers()[i].gameFlags(this);

			// Accumulate flags for all the components.
			for (int i = 1; i < equipment().components().length; i++)
				flags |= equipment().components()[i].gameFlags(this);

			// Accumulate flags for all the regions.
			for (int i = 0; i < equipment().regions().length; i++)
				flags |= equipment().regions()[i].gameFlags(this);

			// Accumulate flags for all the maps.
			for (int i = 0; i < equipment().maps().length; i++)
				flags |= equipment().maps()[i].gameFlags(this);

			// Accumulate flags over all rules
			if (rules.meta() != null)
				for (final MetaRule meta : rules.meta().rules())
					flags |= meta.gameFlags(this);

			// Accumulate flags over all rules
			if (rules.start() != null)
				for (final StartRule start : rules.start().rules())
				{
					final long startGameFlags = start.gameFlags(this);
					flags |= startGameFlags;
					if((startGameFlags & GameType.Stochastic) != 0L )
						stochasticStartingRules = true;
				}

			
			
			if (rules.end() != null)
				flags |= rules.end().gameFlags(this);

			for (final Phase phase : rules.phases())
				flags |= phase.gameFlags(this);

			for (int e = 1; e < equipment.components().length; ++e)
			{
				if (((gameFlags & GameType.Stochastic) == 0L) && equipment.components()[e].isDie())
					flags |= GameType.Stochastic;
				if (((gameFlags & GameType.LargePiece) == 0L) && equipment.components()[e].isLargePiece())
					flags |= GameType.LargePiece;
			}

			// set model flags
			if (mode.mode() == ModeType.Simultaneous)
				flags |= GameType.Simultaneous;

			if (hasHandDice())
				flags |= GameType.NotAllPass;

			if (hasTrack())
			{
				flags |= GameType.Track;
				for (final Track track : board().tracks())
				{
					if (track.hasInternalLoop())
					{
						flags |= GameType.InternalLoopInTrack;
						break;
					}
				}
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
		if (metadata() != null)
			flags |= metadata().gameFlags(this);
		
		return flags;
	}

	/**
	 * Methods computing the warning report in looking if all the ludemes have the
	 * right requirement. Example: using the ludeme (roll) needs dice to be defined.
	 * 
	 * @return True if any required ludeme is missing.
	 */
	public boolean computeRequirementReport()
	{
		boolean missingRequirement = false;

		// Accumulate missing requirements for all the players.
		missingRequirement |= players.missingRequirement(this);

		// Accumulate missing requirements for all the components.
		for (int i = 0; i < equipment().containers().length; i++)
			missingRequirement |= equipment().containers()[i].missingRequirement(this);

		// Accumulate missing requirements for all the components.
		for (int i = 1; i < equipment().components().length; i++)
			missingRequirement |= equipment().components()[i].missingRequirement(this);

		// Accumulate crashes for all the regions.
		for (int i = 0; i < equipment().regions().length; i++)
			missingRequirement |= equipment().regions()[i].missingRequirement(this);

		// Accumulate crashes for all the maps.
		for (int i = 0; i < equipment().maps().length; i++)
			missingRequirement |= equipment().maps()[i].missingRequirement(this);

		// Accumulate missing requirements over meta rules
		if (rules.meta() != null)
			for (final MetaRule meta : rules.meta().rules())
				missingRequirement |= meta.missingRequirement(this);

		// Accumulate missing requirements over starting rules
		if (rules.start() != null)
			for (final StartRule start : rules.start().rules())
				missingRequirement |= start.missingRequirement(this);

		// Accumulate missing requirements over the playing rules.
		for (final Phase phase : rules.phases())
			missingRequirement |= phase.missingRequirement(this);

		// Accumulate missing requirements over the ending rules.
		if (rules.end() != null)
			missingRequirement |= rules.end().missingRequirement(this);

		// We check if two identical meta rules are used.
		if (rules.meta() != null)
		{
			boolean twiceSameMetaRule = false;
			for (int i = 0; i < rules.meta().rules().length; i++)
			{
				final MetaRule meta = rules.meta().rules()[i];
				for (int j = i + 1; j < rules.meta().rules().length; j++)
				{
					final MetaRule metaToCompare = rules.meta().rules()[j];
					if (meta.equals(metaToCompare))
					{
						twiceSameMetaRule = true;
						break;
					}
				}
				if (twiceSameMetaRule)
					break;
			}

			if (twiceSameMetaRule)
			{
				addRequirementToReport("The same meta rule is used twice or more.");
				missingRequirement = true;
			}
		}
		
		return missingRequirement;
	}

	/**
	 * Add a new missing requirement even to the report.
	 * 
	 * @param requirementEvent The requirement event to add.
	 */
	public void addRequirementToReport(final String requirementEvent)
	{
		requirementReport.add(requirementEvent);
	}

	/**
	 * @return The report of the missing requirements.
	 */
	public List<String> requirementReport()
	{
		return requirementReport;
	}

	/**
	 * @return True if any required ludeme is missing.
	 */
	public boolean hasMissingRequirement()
	{
		return hasMissingRequirement;
	}

	/**
	 * Methods computing the crash report in looking if any used ludeme can create a
	 * crash. Example: using some deduction puzzle ludeme in a game with more or
	 * less than one player.
	 * 
	 * @return True if any ludeme can crash the game during its play.
	 */
	public boolean computeCrashReport()
	{
		boolean crash = false;

		// Accumulate crashes for all the players.
		crash |= players.willCrash(this);

		// Accumulate crashes for all the components.
		for (int i = 0; i < equipment().containers().length; i++)
			crash |= equipment().containers()[i].willCrash(this);

		// Accumulate crashes for all the components.
		for (int i = 1; i < equipment().components().length; i++)
			crash |= equipment().components()[i].willCrash(this);

		// Accumulate crashes for all the regions.
		for (int i = 0; i < equipment().regions().length; i++)
			crash |= equipment().regions()[i].willCrash(this);

		// Accumulate crashes for all the maps.
		for (int i = 0; i < equipment().maps().length; i++)
			crash |= equipment().maps()[i].willCrash(this);

		if (players().count() != 1)
			if (equipment().vertexHints().length != 0 || equipment().cellsWithHints().length != 0
					|| equipment().edgesWithHints().length != 0)
			{
				crash = true;
				addCrashToReport("The game uses some hints but the number of players is not 1");
			}

		// Accumulate crashes over meta rules
		if (rules.meta() != null)
			for (final MetaRule meta : rules.meta().rules())
				crash |= meta.willCrash(this);

		// Accumulate crashes over starting rules
		if (rules.start() != null)
			for (final StartRule start : rules.start().rules())
				crash |= start.willCrash(this);

		// Accumulate crashes over the playing rules.
		for (final Phase phase : rules.phases())
			crash |= phase.willCrash(this);

		// Accumulate crashes over the ending rules.
		if (rules.end() != null)
			crash |= rules.end().willCrash(this);

		return crash;
	}

	/**
	 * Add a new crash event to the report.
	 * 
	 * @param crashEvent The crash event to add.
	 */
	public void addCrashToReport(final String crashEvent)
	{
		crashReport.add(crashEvent);
	}

	/**
	 * @return The report of the crashes.
	 */
	public List<String> crashReport()
	{
		return crashReport;
	}

	/**
	 * @return True if the game will crash.
	 */
	public boolean willCrash()
	{
		return willCrash;
	}

	/**
	 * @return BitSet corresponding to ludeme modifying data in EvalContext.
	 */
	public BitSet computeWritingEvalContextFlag()
	{
		final BitSet writingEvalContextFlags = new BitSet();

		try
		{
			// Accumulate writeEvalContext over the players.
			writingEvalContextFlags.or(players.writesEvalContextRecursive());

			// Accumulate writeEvalContext for all the containers.
			for (int i = 0; i < equipment().containers().length; i++)
				writingEvalContextFlags.or(equipment().containers()[i].writesEvalContextRecursive());

			// Accumulate concepts for all the components.
			for (int i = 1; i < equipment().components().length; i++)
				writingEvalContextFlags.or(equipment().components()[i].writesEvalContextRecursive());

			// Accumulate concepts for all the regions.
			for (int i = 0; i < equipment().regions().length; i++)
				writingEvalContextFlags.or(equipment().regions()[i].writesEvalContextRecursive());

			// Accumulate concepts for all the maps.
			for (int i = 0; i < equipment().maps().length; i++)
				writingEvalContextFlags.or(equipment().maps()[i].writesEvalContextRecursive());

			// Accumulate concepts over meta rules
			if (rules.meta() != null)
				for (final MetaRule meta : rules.meta().rules())
					writingEvalContextFlags.or(meta.writesEvalContextRecursive());

			// Accumulate concepts over starting rules
			if (rules.start() != null)
				for (final StartRule start : rules.start().rules())
					writingEvalContextFlags.or(start.writesEvalContextRecursive());

			// Accumulate concepts over the playing rules.
			for (final Phase phase : rules.phases())
				writingEvalContextFlags.or(phase.writesEvalContextRecursive());

			// Accumulate concepts over the ending rules.
			if (rules.end() != null)
				writingEvalContextFlags.or(rules.end().writesEvalContextRecursive());
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}

		// TO PRINT RESULTS
//		final List<String> dataEvalContext = new ArrayList<String>();
//		if (writingEvalContextFlags.get(EvalContextData.From.id()))
//			dataEvalContext.add("From");
//		if (writingEvalContextFlags.get(EvalContextData.Level.id()))
//			dataEvalContext.add("Level");
//		if (writingEvalContextFlags.get(EvalContextData.To.id()))
//			dataEvalContext.add("To");
//		if (writingEvalContextFlags.get(EvalContextData.Between.id()))
//			dataEvalContext.add("Between");
//		if (writingEvalContextFlags.get(EvalContextData.PipCount.id()))
//			dataEvalContext.add("PipCount");
//		if (writingEvalContextFlags.get(EvalContextData.Player.id()))
//			dataEvalContext.add("Player");
//		if (writingEvalContextFlags.get(EvalContextData.Track.id()))
//			dataEvalContext.add("Track");
//		if (writingEvalContextFlags.get(EvalContextData.Site.id()))
//			dataEvalContext.add("Site");
//		if (writingEvalContextFlags.get(EvalContextData.Value.id()))
//			dataEvalContext.add("Value");
//		if (writingEvalContextFlags.get(EvalContextData.Region.id()))
//			dataEvalContext.add("Region");
//		if (writingEvalContextFlags.get(EvalContextData.HintRegion.id()))
//			dataEvalContext.add("HintRegion");
//		if (writingEvalContextFlags.get(EvalContextData.Hint.id()))
//			dataEvalContext.add("Hint");
//		if (writingEvalContextFlags.get(EvalContextData.Edge.id()))
//			dataEvalContext.add("Edge");
//
//		System.out.println("WRITING:");
//		for (final String data : dataEvalContext)
//			System.out.print(data + " ");
//		System.out.println();

		return writingEvalContextFlags;
	}

	/**
	 * @return BitSet corresponding to ludeme reading data in EvalContext.
	 */
	public BitSet computeReadingEvalContextFlag()
	{
		final BitSet readingEvalContextFlags = new BitSet();

		try
		{
			// Accumulate writeEvalContext over the players.
			readingEvalContextFlags.or(players.readsEvalContextRecursive());

			// Accumulate writeEvalContext for all the containers.
			for (int i = 0; i < equipment().containers().length; i++)
				readingEvalContextFlags.or(equipment().containers()[i].readsEvalContextRecursive());

			// Accumulate concepts for all the components.
			for (int i = 1; i < equipment().components().length; i++)
				readingEvalContextFlags.or(equipment().components()[i].readsEvalContextRecursive());

			// Accumulate concepts for all the regions.
			for (int i = 0; i < equipment().regions().length; i++)
				readingEvalContextFlags.or(equipment().regions()[i].readsEvalContextRecursive());

			// Accumulate concepts for all the maps.
			for (int i = 0; i < equipment().maps().length; i++)
				readingEvalContextFlags.or(equipment().maps()[i].readsEvalContextRecursive());

			// Accumulate concepts over meta rules
			if (rules.meta() != null)
				for (final MetaRule meta : rules.meta().rules())
					readingEvalContextFlags.or(meta.readsEvalContextRecursive());

			// Accumulate concepts over starting rules
			if (rules.start() != null)
				for (final StartRule start : rules.start().rules())
					readingEvalContextFlags.or(start.readsEvalContextRecursive());

			// Accumulate concepts over the playing rules.
			for (final Phase phase : rules.phases())
				readingEvalContextFlags.or(phase.readsEvalContextRecursive());

			// Accumulate concepts over the ending rules.
			if (rules.end() != null)
				readingEvalContextFlags.or(rules.end().readsEvalContextRecursive());
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}

		// TO PRINT RESULTS
//		final List<String> dataEvalContext = new ArrayList<String>();
//		if (readingEvalContextFlags.get(EvalContextData.From.id()))
//			dataEvalContext.add("From");
//		if (readingEvalContextFlags.get(EvalContextData.Level.id()))
//			dataEvalContext.add("Level");
//		if (readingEvalContextFlags.get(EvalContextData.To.id()))
//			dataEvalContext.add("To");
//		if (readingEvalContextFlags.get(EvalContextData.Between.id()))
//			dataEvalContext.add("Between");
//		if (readingEvalContextFlags.get(EvalContextData.PipCount.id()))
//			dataEvalContext.add("PipCount");
//		if (readingEvalContextFlags.get(EvalContextData.Player.id()))
//			dataEvalContext.add("Player");
//		if (readingEvalContextFlags.get(EvalContextData.Track.id()))
//			dataEvalContext.add("Track");
//		if (readingEvalContextFlags.get(EvalContextData.Site.id()))
//			dataEvalContext.add("Site");
//		if (readingEvalContextFlags.get(EvalContextData.Value.id()))
//			dataEvalContext.add("Value");
//		if (readingEvalContextFlags.get(EvalContextData.Region.id()))
//			dataEvalContext.add("Region");
//		if (readingEvalContextFlags.get(EvalContextData.HintRegion.id()))
//			dataEvalContext.add("HintRegion");
//		if (readingEvalContextFlags.get(EvalContextData.Hint.id()))
//			dataEvalContext.add("Hint");
//		if (readingEvalContextFlags.get(EvalContextData.Edge.id()))
//			dataEvalContext.add("Edge");
//
//		System.out.println("READING:");
//		for (final String data : dataEvalContext)
//			System.out.print(data + " ");
//		System.out.println();

		return readingEvalContextFlags;
	}

	/**
	 * @return True if the equipment has some stochastic ludeme involved.
	 * 
	 * @remark Currently checking only the regions in the equipment.
	 */
	public boolean equipmentWithStochastic()
	{
		final BitSet regionConcept = new BitSet();
		for (int i = 0; i < equipment().regions().length; i++)
			regionConcept.or(equipment().regions()[i].concepts(this));
		
		return regionConcept.get(Concept.Stochastic.id());
	}

	/**
	 * @return BitSet corresponding to the boolean concepts.
	 */
	public BitSet computeBooleanConcepts()
	{
		final BitSet concept = new BitSet();

		try
		{
			// Accumulate concepts over the players.
			concept.or(players.concepts(this));

			// Accumulate concepts for all the containers.
			for (int i = 0; i < equipment().containers().length; i++)
				concept.or(equipment().containers()[i].concepts(this));

			// Accumulate concepts for all the components.
			for (int i = 1; i < equipment().components().length; i++)
				concept.or(equipment().components()[i].concepts(this));

			// Accumulate concepts for all the regions.
			for (int i = 0; i < equipment().regions().length; i++)
				concept.or(equipment().regions()[i].concepts(this));

			// Accumulate concepts for all the maps.
			for (int i = 0; i < equipment().maps().length; i++)
				concept.or(equipment().maps()[i].concepts(this));

			// Look if the game uses hints.
			if (equipment().vertexHints().length != 0 || equipment().cellsWithHints().length != 0
					|| equipment().edgesWithHints().length != 0)
				concept.set(Concept.Hints.id(), true);

			// Check if some regions are defined.
			if (equipment().regions().length != 0)
				concept.set(Concept.Region.id(), true);

			// We check if the owned pieces are asymmetric or not.
			final List<List<Component>> ownedPieces = new ArrayList<List<Component>>();
			for (int i = 0; i < players.count(); i++)
				ownedPieces.add(new ArrayList<Component>());

			// Check if the game has some asymmetric owned pieces.
			for (int i = 1; i < equipment().components().length; i++)
			{
				final Component component = equipment().components()[i];
				if (component.owner() > 0 && component.owner() <= players.count())
					ownedPieces.get(component.owner() - 1).add(component);
			}

			if (!ownedPieces.isEmpty())
				for (final Component component : ownedPieces.get(0))
				{
					final String nameComponent = component.getNameWithoutNumber();
					final int owner = component.owner();
					for (int i = 1; i < ownedPieces.size(); i++)
					{
						boolean found = false;
						for (final Component otherComponent : ownedPieces.get(i))
						{
							if (otherComponent.owner() != owner
									&& otherComponent.getNameWithoutNumber().equals(nameComponent))
							{
								found = true;
								break;
							}
						}

						if (!found)
						{
							concept.set(Concept.AsymmetricPiecesType.id(), true);
							break;
						}
					}
				}

			// Accumulate concepts over meta rules
			if (rules.meta() != null)
				for (final MetaRule meta : rules.meta().rules())
					concept.or(meta.concepts(this));

			// Accumulate concepts over starting rules
			if (rules.start() != null)
				for (final StartRule start : rules.start().rules())
					concept.or(start.concepts(this));

			// Accumulate concepts over the playing rules.
			for (final Phase phase : rules.phases())
				concept.or(phase.concepts(this));

			// We check if the game has more than one phase.
			if (rules().phases().length > 1)
				concept.set(Concept.Phase.id(), true);

			// Accumulate concepts over the ending rules.
			if (rules.end() != null)
				concept.or(rules.end().concepts(this));

			concept.set(Concept.End.id(), true);
			
			// Look if the game uses a stack state.
			if (isStacking())
			{
				concept.set(Concept.StackState.id(), true);
				concept.set(Concept.Stack.id(), true);
			}

			// Look the graph element types used.
			concept.or(SiteType.concepts(board().defaultSite()));

			// Accumulate the stochastic concepts.
			if (concept.get(Concept.Dice.id()))
				concept.set(Concept.Stochastic.id(), true);
			if (concept.get(Concept.Domino.id()))
				concept.set(Concept.Stochastic.id(), true);
			if (concept.get(Concept.Card.id()))
				concept.set(Concept.Stochastic.id(), true);

			if (concept.get(Concept.Dice.id()) || concept.get(Concept.LargePiece.id()))
				concept.set(Concept.SiteState.id(), true);

			if (concept.get(Concept.LargePiece.id()))
				concept.set(Concept.SiteState.id(), true);

			if (concept.get(Concept.Domino.id()))
				concept.set(Concept.PieceCount.id(), true);

			for (int i = 1; i < equipment().components().length; i++)
			{
				final Component component = equipment().components()[i];
				if (component.getNameWithoutNumber() == null)
					continue;
				final String componentName = component.getNameWithoutNumber().toLowerCase();
				if (componentName.equals("ball"))
					concept.set(Concept.BallComponent.id(), true);
				else if (componentName.equals("disc"))
					concept.set(Concept.DiscComponent.id(), true);
				else if (componentName.equals("marker"))
					concept.set(Concept.MarkerComponent.id(), true);
				else if (componentName.equals("king") || componentName.equals("king_nocross"))
					concept.set(Concept.KingComponent.id(), true);
				else if (componentName.equals("knight"))
					concept.set(Concept.KnightComponent.id(), true);
				else if (componentName.equals("queen"))
					concept.set(Concept.QueenComponent.id(), true);
				else if (componentName.equals("bishop") || componentName.equals("bishop_nocross"))
					concept.set(Concept.BishopComponent.id(), true);
				else if (componentName.equals("rook"))
					concept.set(Concept.RookComponent.id(), true);
				else if (componentName.equals("pawn"))
					concept.set(Concept.PawnComponent.id(), true);
				else
				{
					final String svgPath = ImageUtil.getImageFullPath(componentName);
					if (svgPath == null) // The SVG can not be find.
						continue;
					if (svgPath.contains("tafl"))
						concept.set(Concept.TaflComponent.id(), true);
					else if (svgPath.contains("animal"))
						concept.set(Concept.AnimalComponent.id(), true);
					else if (svgPath.contains("fairyChess"))
						concept.set(Concept.FairyChessComponent.id(), true);
					else if (svgPath.contains("chess"))
						concept.set(Concept.ChessComponent.id(), true);
					else if (svgPath.contains("ploy"))
						concept.set(Concept.PloyComponent.id(), true);
					else if (svgPath.contains("shogi"))
						concept.set(Concept.ShogiComponent.id(), true);
					else if (svgPath.contains("xiangqi"))
						concept.set(Concept.XiangqiComponent.id(), true);
					else if (svgPath.contains("stratego"))
						concept.set(Concept.StrategoComponent.id(), true);
					else if (svgPath.contains("Janggi"))
						concept.set(Concept.JanggiComponent.id(), true);
					else if (svgPath.contains("hand"))
						concept.set(Concept.HandComponent.id(), true);
					else if (svgPath.contains("checkers"))
						concept.set(Concept.CheckersComponent.id(), true);
				}
			}

			// Check the time model.
			if (mode.mode().equals(ModeType.Simulation))
				concept.set(Concept.Realtime.id(), true);
			else
				concept.set(Concept.Discrete.id(), true);

			// Check the mode.
			if (mode.mode().equals(ModeType.Alternating))
				concept.set(Concept.Alternating.id(), true);
			else if (mode.mode().equals(ModeType.Simultaneous))
				concept.set(Concept.Simultaneous.id(), true);
			else if (mode.mode().equals(ModeType.Simulation))
				concept.set(Concept.Simulation.id(), true);

			// Check the number of players.
			if (players.count() == 1)
			{
				concept.set(Concept.Solitaire.id(), true);
				if (!concept.get(Concept.DeductionPuzzle.id()))
					concept.set(Concept.PlanningPuzzle.id(), true);
			}
			else if (players.count() == 2)
				concept.set(Concept.TwoPlayer.id(), true);
			else if (players.count() > 2)
				concept.set(Concept.Multiplayer.id(), true);

			// We put to true all the parents of concept which are true.
			for (final Concept possibleConcept : Concept.values())
				if (concept.get(possibleConcept.id()))
				{
					Concept conceptToCheck = possibleConcept;
					while (conceptToCheck != null)
					{
						if (conceptToCheck.dataType().equals(ConceptDataType.BooleanData))
							concept.set(conceptToCheck.id(), true);
						conceptToCheck = conceptToCheck.parent();
					}
				}
			
			// Detection of some concepts based on the ludemeplexes used.
			for (String key: description().defineInstances().keySet()) {
				final String define = key.substring(1, key.length()-1);
				
	            if(define.equals("AlquerqueBoard") && description().defineInstances().get(key).define().isKnown())
	            	concept.set(Concept.AlquerqueBoard.id(), true);
	            
	            if(define.equals("AlquerqueGraph") && description().defineInstances().get(key).define().isKnown())
	            	concept.set(Concept.AlquerqueBoard.id(), true);
	            
	            if(define.equals("AlquerqueBoardWithBottomAndTopTriangles") && description().defineInstances().get(key).define().isKnown())
	            {
	            	concept.set(Concept.AlquerqueBoard.id(), true);
	            	concept.set(Concept.AlquerqueBoardWithTwoTriangles.id(), true);
	            }
	            
	            if(define.equals("AlquerqueGraphWithBottomAndTopTriangles") && description().defineInstances().get(key).define().isKnown())
	            {
	            	concept.set(Concept.AlquerqueBoard.id(), true);
	            	concept.set(Concept.AlquerqueBoardWithTwoTriangles.id(), true);
	            }
	            
	            if(define.equals("AlquerqueBoardWithBottomTriangle") && description().defineInstances().get(key).define().isKnown())
	            {
	            	concept.set(Concept.AlquerqueBoard.id(), true);
	            	concept.set(Concept.AlquerqueBoardWithOneTriangle.id(), true);
	            }
	            
	            if(define.equals("AlquerqueGraphWithBottomTriangle") && description().defineInstances().get(key).define().isKnown())
	            {
	            	concept.set(Concept.AlquerqueBoard.id(), true);
	            	concept.set(Concept.AlquerqueBoardWithOneTriangle.id(), true);
	            }
	            
	            if(define.equals("AlquerqueBoardWithFourTriangles") && description().defineInstances().get(key).define().isKnown())
	            {
	            	concept.set(Concept.AlquerqueBoard.id(), true);
	            	concept.set(Concept.AlquerqueBoardWithFourTriangles.id(), true);
	            }
	            
	            if(define.equals("AlquerqueGraphWithFourTriangles") && description().defineInstances().get(key).define().isKnown())
	            {
	            	concept.set(Concept.AlquerqueBoard.id(), true);
	            	concept.set(Concept.AlquerqueBoardWithFourTriangles.id(), true);
	            }
	            
	            if(define.equals("AlquerqueBoardWithEightTriangles") && description().defineInstances().get(key).define().isKnown())
	            {
	            	concept.set(Concept.AlquerqueBoard.id(), true);
	            	concept.set(Concept.AlquerqueBoardWithEightTriangles.id(), true);
	            }
	            
	            if(define.equals("ThreeMensMorrisBoard") && description().defineInstances().get(key).define().isKnown())
	            	concept.set(Concept.ThreeMensMorrisBoard.id(), true);
	            
	            if(define.equals("ThreeMensMorrisBoardWithLeftAndRightTriangles") && description().defineInstances().get(key).define().isKnown())
	            {
	            	concept.set(Concept.ThreeMensMorrisBoard.id(), true);
	            	concept.set(Concept.ThreeMensMorrisBoardWithTwoTriangles.id(), true);
	            }
	            
	            if(define.equals("ThreeMensMorrisGraphWithLeftAndRightTriangles") && description().defineInstances().get(key).define().isKnown())
	            {
	            	concept.set(Concept.ThreeMensMorrisBoard.id(), true);
	            	concept.set(Concept.ThreeMensMorrisBoardWithTwoTriangles.id(), true);
	            }
	            
	            if(define.equals("NineMensMorrisBoard") && description().defineInstances().get(key).define().isKnown())
	            	concept.set(Concept.NineMensMorrisBoard.id(), true);
	            
	            if(define.equals("StarBoard") && description().defineInstances().get(key).define().isKnown())
	            	concept.set(Concept.StarBoard.id(), true);
	            
	            if(define.equals("CrossBoard") && description().defineInstances().get(key).define().isKnown())
	            	concept.set(Concept.CrossBoard.id(), true);
	            
	            if(define.equals("CrossGraph") && description().defineInstances().get(key).define().isKnown())
	            	concept.set(Concept.CrossBoard.id(), true);
	            
	            if(define.equals("KintsBoard") && description().defineInstances().get(key).define().isKnown())
	            	concept.set(Concept.KintsBoard.id(), true);
	            
	            if(define.equals("PachisiBoard") && description().defineInstances().get(key).define().isKnown())
	            	concept.set(Concept.PachisiBoard.id(), true);
	            
	            if(define.equals("FortyStonesWithFourGapsBoard") && description().defineInstances().get(key).define().isKnown())
	            	concept.set(Concept.FortyStonesWithFourGapsBoard.id(), true);
	            
	        }
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}

		return concept;
	}

	/**
	 * @return The game flags.
	 */
	public long gameFlags()
	{
		return gameFlags;
	}

	/**
	 * @return The boolean concepts.
	 */
	public BitSet booleanConcepts()
	{
		return booleanConcepts;
	}

	/**
	 * @return The non boolean concepts.
	 */
	public Map<Integer, String> computeNonBooleanConcepts()
	{
		final Map<Integer, String> nonBooleanConcepts = new HashMap<Integer, String>();

		// Compute the average number of each absolute direction.
		final SiteType defaultSiteType = board().defaultSite();
		final List<? extends TopologyElement> elements = board().topology().getGraphElements(defaultSiteType);
		final int numDefaultElements = elements.size();
		int totalNumDirections = 0;
		int totalNumOrthogonalDirections = 0;
		int totalNumDiagonalDirections = 0;
		int totalNumAdjacentDirections = 0;
		int totalNumOffDiagonalDirections = 0;
		for (final TopologyElement element : elements)
		{
			totalNumDirections += element.neighbours().size();
			totalNumOrthogonalDirections += element.orthogonal().size();
			totalNumDiagonalDirections += element.diagonal().size();
			totalNumAdjacentDirections += element.adjacent().size();
			totalNumOffDiagonalDirections += element.off().size();
		}
		String avgNumDirection = new DecimalFormat("##.##").format((double) totalNumDirections / (double) numDefaultElements) + "";
		avgNumDirection = avgNumDirection.replaceAll(",", ".");

		String avgNumOrthogonalDirection = new DecimalFormat("##.##").format((double) totalNumOrthogonalDirections / (double) numDefaultElements) + "";
		avgNumOrthogonalDirection = avgNumOrthogonalDirection.replaceAll(",", ".");

		String avgNumDiagonalDirection = new DecimalFormat("##.##").format((double) totalNumDiagonalDirections / (double) numDefaultElements) + "";
		avgNumDiagonalDirection = avgNumDiagonalDirection.replaceAll(",", ".");

		String avgNumAdjacentlDirection = new DecimalFormat("##.##").format((double) totalNumAdjacentDirections / (double) numDefaultElements) + "";
		avgNumAdjacentlDirection = avgNumAdjacentlDirection.replaceAll(",", ".");

		String avgNumOffDiagonalDirection = new DecimalFormat("##.##").format((double) totalNumOffDiagonalDirections / (double) numDefaultElements) + "";
		avgNumOffDiagonalDirection = avgNumOffDiagonalDirection.replaceAll(",", ".");

		for (final Concept concept : Concept.values())
			if (!concept.dataType().equals(ConceptDataType.BooleanData))
			{
				switch (concept)
				{
				case NumPlayableSites:
					int countPlayableSites = 0;
					for (int cid = 0; cid < equipment.containers().length; cid++)
					{
						final Container container = equipment.containers()[cid];
						if (cid != 0)
							countPlayableSites += container.numSites();
						else
						{
							if (booleanConcepts.get(Concept.Cell.id()))
								countPlayableSites += container.topology().cells().size();

							if (booleanConcepts.get(Concept.Vertex.id()))
								countPlayableSites += container.topology().vertices().size();

							if (booleanConcepts.get(Concept.Edge.id()))
								countPlayableSites += container.topology().edges().size();
						}
					}
					nonBooleanConcepts.put(Integer.valueOf(concept.id()), countPlayableSites + "");
					break;
				case NumPlayableSitesOnBoard:
					int countPlayableSitesOnBoard = 0;
					final Container container = equipment.containers()[0];
					if (booleanConcepts.get(Concept.Cell.id()))
						countPlayableSitesOnBoard += container.topology().cells().size();

					if (booleanConcepts.get(Concept.Vertex.id()))
						countPlayableSitesOnBoard += container.topology().vertices().size();

					if (booleanConcepts.get(Concept.Edge.id()))
						countPlayableSitesOnBoard += container.topology().edges().size();
					nonBooleanConcepts.put(Integer.valueOf(concept.id()), countPlayableSitesOnBoard + "");
					break;
				case NumPlayers:
					nonBooleanConcepts.put(Integer.valueOf(concept.id()),
							players.count() + "");
					break;
				case NumColumns:
					nonBooleanConcepts.put(Integer.valueOf(concept.id()),
							board().topology().columns(defaultSiteType).size() + "");
					break;
				case NumRows:
					nonBooleanConcepts.put(Integer.valueOf(concept.id()),
							board().topology().rows(defaultSiteType).size() + "");
					break;
				case NumCorners:
					nonBooleanConcepts.put(Integer.valueOf(concept.id()),
							board().topology().corners(defaultSiteType).size() + "");
					break;
				case NumDirections:
					nonBooleanConcepts.put(Integer.valueOf(concept.id()), avgNumDirection);
					break;
				case NumOrthogonalDirections:
					nonBooleanConcepts.put(Integer.valueOf(concept.id()), avgNumOrthogonalDirection);
					break;
				case NumDiagonalDirections:
					nonBooleanConcepts.put(Integer.valueOf(concept.id()), avgNumDiagonalDirection);
					break;
				case NumAdjacentDirections:
					nonBooleanConcepts.put(Integer.valueOf(concept.id()), avgNumAdjacentlDirection);
					break;
				case NumOffDiagonalDirections:
					nonBooleanConcepts.put(Integer.valueOf(concept.id()), avgNumOffDiagonalDirection);
					break;
				case NumOuterSites:
					nonBooleanConcepts.put(Integer.valueOf(concept.id()),
							board().topology().outer(defaultSiteType).size() + "");
					break;
				case NumInnerSites:
					nonBooleanConcepts.put(Integer.valueOf(concept.id()),
							board().topology().inner(defaultSiteType).size() + "");
					break;
				case NumLayers:
					nonBooleanConcepts.put(Integer.valueOf(concept.id()),
							board().topology().layers(defaultSiteType).size() + "");
					break;
				case NumEdges:
					nonBooleanConcepts.put(Integer.valueOf(concept.id()), board().topology().edges().size() + "");
					break;
				case NumCells:
					nonBooleanConcepts.put(Integer.valueOf(concept.id()), board().topology().cells().size() + "");
					break;
				case NumVertices:
					nonBooleanConcepts.put(Integer.valueOf(concept.id()), board().topology().vertices().size() + "");
					break;
				case NumPerimeterSites:
					nonBooleanConcepts.put(Integer.valueOf(concept.id()),
							board().topology().perimeter(defaultSiteType).size() + "");
					break;
				case NumTopSites:
					nonBooleanConcepts.put(Integer.valueOf(concept.id()),
							board().topology().top(defaultSiteType).size() + "");
					break;
				case NumBottomSites:
					nonBooleanConcepts.put(Integer.valueOf(concept.id()),
							board().topology().bottom(defaultSiteType).size() + "");
					break;
				case NumRightSites:
					nonBooleanConcepts.put(Integer.valueOf(concept.id()),
							board().topology().right(defaultSiteType).size() + "");
					break;
				case NumLeftSites:
					nonBooleanConcepts.put(Integer.valueOf(concept.id()),
							board().topology().left(defaultSiteType).size() + "");
					break;
				case NumCentreSites:
					nonBooleanConcepts.put(Integer.valueOf(concept.id()),
							board().topology().centre(defaultSiteType).size() + "");
					break;
				case NumConvexCorners:
					nonBooleanConcepts.put(Integer.valueOf(concept.id()),
							board().topology().cornersConvex(defaultSiteType).size() + "");
					break;
				case NumConcaveCorners:
					nonBooleanConcepts.put(Integer.valueOf(concept.id()),
							board().topology().cornersConcave(defaultSiteType).size() + "");
					break;
				case NumPhasesBoard:
					int numPhases = 0;
					final List<List<TopologyElement>> phaseElements = board().topology().phases(defaultSiteType);
					for (final List<TopologyElement> topoElements : phaseElements)
						if (topoElements.size() != 0)
							numPhases++;
					nonBooleanConcepts.put(Integer.valueOf(concept.id()), numPhases + "");
					break;
				case NumComponentsType:
					nonBooleanConcepts.put(Integer.valueOf(concept.id()), equipment().components().length - 1 + "");
					break;
				case NumComponentsTypePerPlayer:
					final int[] componentsPerPlayer = new int[players.size()];
					for (int i = 1; i < equipment().components().length; i++)
					{
						final Component component = equipment().components()[i];
						if (component.owner() > 0 && component.owner() < players().size())
							componentsPerPlayer[component.owner()]++;
					}
					int numOwnerComponent = 0;
					for (int i = 1; i < componentsPerPlayer.length; i++)
						numOwnerComponent += componentsPerPlayer[i];
					String avgNumComponentPerPlayer =  players.count() <= 0 ? "0" : new DecimalFormat("##.##")
							.format((double) numOwnerComponent / (double) players.count()) + "";
					avgNumComponentPerPlayer = avgNumComponentPerPlayer.replaceAll(",", ".");
					nonBooleanConcepts.put(Integer.valueOf(concept.id()), avgNumComponentPerPlayer);
					break;
				case NumPlayPhase:
					nonBooleanConcepts.put(Integer.valueOf(concept.id()), rules().phases().length + "");
					break;
				case NumDice:
					int numDice = 0;
					for (int i = 1; i < equipment().components().length; i++)
						if (equipment().components()[i].isDie())
							numDice++;
					nonBooleanConcepts.put(Integer.valueOf(concept.id()), numDice + "");
					break;
				case NumContainers:
					nonBooleanConcepts.put(Integer.valueOf(concept.id()), equipment().containers().length + "");
					break;
				default:
					break;
				}
			}
		
		return nonBooleanConcepts;
	}
	
	/**
	 * @return The starting concepts. To use only for recons purposes because this is not taking in account the RNG.
	 */
	public Map<String, Double> startsConceptsWithoutRNG()
	{
		final Map<String, Double> mapStarting = new HashMap<String, Double>();
		
		double numStartComponents = 0.0;
		double numStartComponentsHands = 0.0;
		double numStartComponentsBoard = 0.0;
		
		// Setup a new instance of the game
		final BitSet concepts = computeBooleanConcepts();	
		final Context context = new Context(this,  new Trial(this));
		this.start(context);
		for (int cid = 0; cid < context.containers().length; cid++)
		{
			final Container cont = context.containers()[cid];
			final ContainerState cs = context.containerState(cid);
			if (cid == 0)
			{
				if (concepts.get(Concept.Cell.id()))
					for (int cell = 0; cell < cont.topology().cells().size(); cell++)
					{
						final int count = isStacking() ? cs.sizeStack(cell, SiteType.Cell) : cs.count(cell, SiteType.Cell);
						numStartComponents += count;
						numStartComponentsBoard += count;
					}
				
				if (concepts.get(Concept.Vertex.id()))
					for (int vertex = 0; vertex < cont.topology().vertices().size(); vertex++)
					{
						final int count = isStacking() ? cs.sizeStack(vertex, SiteType.Vertex) : cs.count(vertex, SiteType.Vertex);
						numStartComponents += count;
						numStartComponentsBoard += count;
					}
		
				if (concepts.get(Concept.Edge.id()))
					for (int edge = 0; edge < cont.topology().edges().size(); edge++)
					{
						final int count = isStacking() ? cs.sizeStack(edge, SiteType.Edge) : cs.count(edge, SiteType.Edge);
						numStartComponents += count;
						numStartComponentsBoard += count;
					}
			}
			else
			{
				if (concepts.get(Concept.Cell.id()))
					for (int cell = context.sitesFrom()[cid]; cell < context.sitesFrom()[cid]
							+ cont.topology().cells().size(); cell++)
					{
						final int count = isStacking() ? cs.sizeStack(cell, SiteType.Cell) : cs.count(cell, SiteType.Cell);
						numStartComponents += count;
						numStartComponentsHands += count;
					}
			}
		}
		
		mapStarting.put(Concept.NumStartComponents.name(), Double.valueOf(numStartComponents));
		mapStarting.put(Concept.NumStartComponentsHand.name(), Double.valueOf(numStartComponentsHands));
		mapStarting.put(Concept.NumStartComponentsBoard.name(), Double.valueOf(numStartComponentsBoard));

		mapStarting.put(Concept.NumStartComponentsPerPlayer.name(), Double.valueOf(numStartComponents / (players().count() == 0 ? 1 : players().count())));
		mapStarting.put(Concept.NumStartComponentsHandPerPlayer.name(), Double.valueOf(numStartComponentsHands / (players().count() == 0 ? 1 : players().count())));
		mapStarting.put(Concept.NumStartComponentsBoardPerPlayer.name(), Double.valueOf(numStartComponentsBoard / (players().count() == 0 ? 1 : players().count())));
		
		return mapStarting;
	}

	/**
	 * @return The non boolean concepts.
	 */
	public Map<Integer, String> nonBooleanConcepts()
	{
		return conceptsNonBoolean;
	}

	//-------------------------Game related methods----------------------------

	/**
	 * @param context The context.
	 * @param forced True if the pass move is forced
	 * @return A pass move created to be applied in given context.
	 */
	public static Move createPassMove(final Context context, final boolean forced)
	{
		return createPassMove(context.state().mover(),forced);
	}
	
	/**
	 * @param player The player who is expected to make this move.
	 * @param forced True if the pass move is forced
	 * @return A pass move created to be applied in given context (with given player
	 *         as mover.
	 */
	public static Move createPassMove(final int player, final boolean forced)
	{
		final ActionPass actionPass = new ActionPass(forced);
		actionPass.setDecision(true);
		final Move passMove = new Move(actionPass);
		passMove.setMover(player);
		passMove.setMovesLudeme(new Pass(null));
		return passMove;
	}

	/**
	 * Initialise the game graph and other variables.
	 */
	@Override
	public void create()
	{
		if (finishedPreprocessing)
			System.err.println("Warning! Game.create() has already previously been called on " + name());

		if (equipment == null) // If no equipment defined we use the default one.
			equipment = new Equipment
			(
				new Item[]
						{ 
							new Board
							(
								new RectangleOnSquare
									(
										new DimConstant(3), 
										null, 
										null, 
										null
									), 
								null,
								null,
								null, 
								null, 
								null,
								Boolean.FALSE
							) 
						}
			);

		if (rules == null) // If no rules defined we use the default ones.
		{
			rules = new Rules
					(
						null, // no metarules
						null, // empty board
					new Play
						(
							game.rules.play.moves.decision.Move.construct
							(
								MoveSiteType.Add, 
								null,
								new To
								(
									null, 
									SitesEmpty.construct(null, null), 
									null, 
									null, 
									null,
									null, 
									null
								), 
							null, 
							null,
							null
							)
						),
					new End
					(
						new game.rules.end.If
						(
							Is.construct
							(
								IsLineType.Line, null, new IntConstant(3), 
								null, 
								null,
								null, 
								null, 
								null, 
								null, 
								null, 
								null, 
								null,
								null,
								null,
								null,
								null,
								null
							),
							null,
							null,
							new Result(RoleType.Mover, ResultType.Win)
						), 
						null
					)
				);
		}

		// Create the times of the equipment.
		equipment.createItems(this);

		// We add the index of the owner at the end of the name of each component.
		for (int i = 1; i < equipment.components().length; i++)
		{
			final Component component = equipment.components()[i];

			if (component.isTile() && component.numSides() == Constants.OFF)
				component.setNumSides(board().topology().cells().get(0).edges().size());

			final String componentName = component.name();
			final RoleType role = component.role();

			// Not for the puzzle, not for a domino or a die
			if (players.count() != 1 && !componentName.contains("Domino") && !componentName.contains("Die"))
				if (role == RoleType.Neutral || (role.owner() > 0 && role.owner() <= Constants.MAX_PLAYERS))
					component.setName(componentName + role.owner());

			// For puzzle we modify the name only if the role is equal to Neutral
			if (players.count() == 1 && !componentName.contains("Domino") && !componentName.contains("Die"))
				if (role == RoleType.Neutral)
					component.setName(componentName + role.owner());
		}

		// We build the tracks and compute the maps
		for (final Track track : board().tracks())
			track.buildTrack(this);
		for (final game.equipment.other.Map map : equipment.maps())
			map.computeMap(this);

		// In case of direction for a player, all the pieces of this player will use it.
		for (int j = 1; j < players.players().size(); j++)
		{
			final Player p = players.players().get(j);
			final DirectionFacing direction = p.direction();
			final int pid = p.index();
			if (direction != null)
			{
				for (int i = 1; i < equipment.components().length; i++)
				{
					final Component component = equipment.components()[i];
					if (pid == component.owner())
						component.setDirection(direction);
				}
			}
		}

		for (final Container c : equipment.containers())
			if (c.isDice())
				handDice.add((Dice) c);
			else if (c.isDeck())
				handDeck.add((Deck) c);

		gameFlags = computeGameFlags();

		if ((gameFlags & GameType.UsesSwapRule) != 0L)
			metaRules.setUsesSwapRule(true);

		stateReference = new State(this, StateConstructorLock.INSTANCE);

		// No component for the deduction puzzle (for sandbox)
		if (isDeductionPuzzle())
			equipment.clearComponents();

		mapContainer.clear();
		mapComponent.clear();

		for (int e = 0; e < equipment.containers().length; e++)
		{
			final Container equip = equipment.containers()[e];
			mapContainer.put(equip.name(), equip);
		}

		// e = 0 is the empty component
		for (int e = 1; e < equipment.components().length; e++)
		{
			final Component equip = equipment.components()[e];
			equip.setIndex(e);
			mapComponent.put(equip.name(), equip);
		}
		// System.out.println(map.size() + " items mapped.");

		// Initialise control: state, number of players, etc.
		stateReference.initialise(this);

		// preprocess regions
		final game.equipment.other.Regions[] regions = equipment().regions();

		for (final game.equipment.other.Regions region : regions)
			region.preprocess(this);

		// preprocessing step for any static ludemes and check if the items name exist.
		if (rules.start() != null)
			for (final StartRule start : rules.start().rules())
				start.preprocess(this);

		if (rules.end() != null)
			rules.end().preprocess(this);

		for (final Phase phase : rules.phases())
			phase.preprocess(this);

		booleanConcepts = computeBooleanConcepts();
		conceptsNonBoolean = computeNonBooleanConcepts();
		hasMissingRequirement = computeRequirementReport();
		willCrash = computeCrashReport();

		// System.out.println("Game.create(): numPlayers=" +
		// stateReference.numPlayers()
		// + ", active=" + stateReference.active());

		// Create mappings between ints and moves
//		for (final Phase phase : rules.phases())
//			phase.play().moves().updateMoveIntMapper(this, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE,
//					Integer.MIN_VALUE);

		// Precompute distance tables which rely on preprocessing done above
		// (e.g. regions)
		if (board().defaultSite() == SiteType.Cell)
			equipment.containers()[0].topology().preGenerateDistanceToRegionsCells(this, regions);
		else
			equipment.containers()[0].topology().preGenerateDistanceToRegionsVertices(this, regions);

		postCreation();

		// Create custom, optimised playout strategies
		addCustomPlayouts();
		
		finishedPreprocessing = true;
	}
	
	/**
	 * @return End rules for this game
	 */
	public End endRules()
	{
		return rules.end();
	}

	//-------------------------------------------------------------------------

	/**
	 * Init the state and start the game.
	 */
	@Override
	public void start(final Context context)
	{		
		context.getLock().lock();
		
		//System.out.println("Starting game with RNG internal state: " + Arrays.toString(((RandomProviderDefaultState)context.rng().saveState()).getState()));
		
		try
		{
			if (startContext != null)
			{
				context.resetToContext(startContext);
			}
			else
			{
				// Normal case for single-trial games
				context.reset();
	
				// We keep a trace on all the remaining dominoes
				if (hasDominoes())
					for (int componentId = 1; componentId < equipment().components().length; componentId++)
						context.state().remainingDominoes().add(componentId);
	
				// Place the dice in a hand dice.
				for (final Dice c : context.game().handDice())
					for (int i = 1; i <= c.numLocs(); i++)
					{
						final StartRule rule = new PlaceItem("Die" + i, c.name(), SiteType.Cell, null, null, null, null,
								null, null);
						rule.eval(context);
					}
	
				// Place randomly the cards of the deck in the game.
				for (final Deck d : context.game().handDeck())
				{
					final TIntArrayList components = new TIntArrayList(d.indexComponent());
					final int nbCards = components.size();
	
					for (int i = 0; i < nbCards; i++)
					{
						final int j = (context.rng().nextInt(components.size()));
						final int index = components.getQuick(j);
						final StartRule rule = new PlaceCustomStack("Card" + index, null, d.name(), SiteType.Cell, null,
								null, null, null, null, null);
						components.remove(index);
						rule.eval(context);
					}
				}
	
				if (rules.start() != null)
					rules.start().eval(context);
	
				// Apply the metarule
				if (rules.meta() != null)
					for (final MetaRule meta : rules.meta().rules())
						meta.eval(context);
	
				// We store the starting positions of each component.
				for (int i = 0; i < context.components().length; i++)
					context.trial().startingPos().add(new Region());
	
				if (!isDeductionPuzzle())
				{
					final ContainerState cs = context.containerState(0);
					for (int site = 0; site < board().topology().getGraphElements(board().defaultSite()).size(); site++)
					{
						final int what = cs.what(site, board().defaultSite());
						context.trial().startingPos().get(what).add(site);
					}
				}
				else
				{
					final Satisfy set = (Satisfy) rules().phases()[0].play().moves();
					initConstraintVariables(set.constraints(), context);
				}
	
//				for (int what = 1; what < context.components().size(); what++)
//				{
//					final String nameCompo = context.components().get(what).name();
//					System.out.println("Component " + nameCompo + " starting pos = " + context.trial().startingPos(what));
//				}
	
				// To update the sum of the dice container.
				if (hasHandDice())
				{
					for (int i = 0; i < handDice().size(); i++)
					{
						final Dice dice = handDice().get(i);
						final ContainerState cs = context.containerState(dice.index());
	
						final int siteFrom = context.sitesFrom()[dice.index()];
						final int siteTo = context.sitesFrom()[dice.index()] + dice.numSites();
						int sum = 0;
						for (int site = siteFrom; site < siteTo; site++)
						{
							sum += context.components()[cs.whatCell(site)].getFaces()[cs.stateCell(site)];
							context.state().currentDice()[i][site - siteFrom] = context.components()[cs.whatCell(site)]
									.getFaces()[cs.stateCell(site)];
						}
						context.state().sumDice()[i] = sum;
					}
				}
	
				if (usesNoRepeatPositionalInGame() && context.state().mover() != context.state().prev())
					context.trial().previousState().add(context.state().stateHash());
	
				if (usesNoRepeatPositionalInTurn())
				{
					if (context.state().mover() == context.state().prev())
					{
						context.trial().previousStateWithinATurn().add(context.state().stateHash());
					}
					else
					{
						context.trial().previousStateWithinATurn().clear();
						context.trial().previousStateWithinATurn().add(context.state().stateHash());
					}
				}
	
				// only save the first state in trial after applying start rules
				context.trial().saveState(context.state());
	
				numStartingAction = context.trial().numMoves();		// FIXME should not be Game property if it can be variable with stochastic start rules
	
				if (!stochasticStartingRules)
					startContext = new Context(context);
			}
			
			// Important for AIs
			//incrementGameStartCount();
			
			// Make sure our "real" context's RNG actually gets used and progresses
			if (!context.trial().over() && context.game().isStochasticGame())
				context.game().moves(context);
		}
		finally
		{
			context.getLock().unlock();
		}
	}

	/**
	 * Compute the legal moves of the game according to the context.
	 */
	@Override
	public Moves moves(final Context context)
	{
		context.getLock().lock();
		
		try
		{
			final Trial trial = context.trial();
			if (trial.cachedLegalMoves() == null || trial.cachedLegalMoves().moves().isEmpty())
			{
				final Moves legalMoves;
	
				if (trial.over())
				{
					legalMoves = new BaseMoves(null);
				}
				else if (isAlternatingMoveGame())
				{
					final int mover = context.state().mover();
					final int indexPhase = context.state().currentPhase(mover);
					final Phase phase = context.game().rules.phases()[indexPhase];
					legalMoves = phase.play().moves().eval(context);

					// Meta-rule: we apply the meta rule if existing.
					Swap.apply(context, legalMoves);
					
					// Meta-rule: We apply the Pin meta-rule if existing.
					Pin.apply(context,legalMoves);
					
					// Meta-rule:  We apply the NoStackOnFallen meta-rule if existing.
					NoStackOn.apply(context, legalMoves);
					
					if (metaRules.automove() || metaRules.gravityType() != null)
					{
						for (final Move legalMove : legalMoves.moves())
						{
							// Meta-rule: We apply the auto move rules if existing.
							Automove.apply(context, legalMove);
		
							// Meta-rule: We apply the gravity rules if existing.
							Gravity.apply(context, legalMove);
						}
					}
				}
				else if (isSimulationMoveGame())
				{
					final Phase phase = context.game().rules.phases()[0];
					final Moves moves = phase.play().moves().eval(context);
					legalMoves = new BaseMoves(null);

					if (!moves.moves().isEmpty())
					{
						final Move singleMove = new Move(moves.get(0));

						for (int i = 1; i < moves.moves().size(); i++)
							for (final Action action : moves.get(i).actions())
								singleMove.actions().add(action);

						legalMoves.moves().add(singleMove);
					}
				}
				else
				{
					legalMoves = new BaseMoves(null);
	
					for (int p = 1; p <= players.count(); ++p)
					{
						// TODO: In the (probably very common) case where multiple players are in same
						// phase, this implementation seems really inefficient? Always computing the same
						// moves?
						final int indexPhase = context.state().currentPhase(p);
						final Phase phase = context.game().rules.phases()[indexPhase];
						final FastArrayList<Move> phaseMoves = phase.play().moves().eval(context).moves();

						boolean addedMove = false;
						for (final Move move : phaseMoves)
						{
							if (move.mover() == p)
							{
								legalMoves.moves().add(move);
								addedMove = true;
							}
						}
						
						if (!addedMove && context.active(p))
						{
							// Need to add forced pass for p
							legalMoves.moves().add(createPassMove(p,true));
						}
					}

					for (final Move move : legalMoves.moves())
					{
						if (move.then().isEmpty())
							if (legalMoves.then() != null)
								move.then().add(legalMoves.then().moves());
					}
				}

//				System.out.println("LEGAL MOVES ARE");
//				for (final Move m : legalMoves.moves())
//				{
//					System.out.println(m);
//					final BitSet moveConcepts = m.concepts(context);
//					for (int indexConcept = 0; indexConcept < Concept.values().length; indexConcept++)
//					{
//						final Concept concept = Concept.values()[indexConcept];
//						if (moveConcepts.get(concept.id()))
//							System.out.println(concept.name());
//					}
//				}

				trial.setLegalMoves(legalMoves, context);
	
				if (context.active())
				{
					// empty <=> stalemated
					context.state().setStalemated(context.state().mover(), legalMoves.moves().isEmpty());
				}
			}

			return trial.cachedLegalMoves();
		}
		finally
		{
			context.getLock().unlock();
		}
	}
	
	/**
	 * Apply a move to the current context.
	 */
	@Override
	public Move apply(final Context context, final Move move)
	{
		return apply(context, move, false);		// By default false --> don't skip computing end rules
	}
	
	/**
	 * Get matching move from the legal moves.
	 * @param context
	 * @param move
	 * @return Matching move from the legal moves.
	 */
	public Move getMatchingLegalMove(final Context context, final Move move)
	{
		Move realMoveToApply = null;
		final Moves legal = moves(context);
		final List<Action> moveActions = move.getActionsWithConsequences(context);

		for (final Move m : legal.moves())
		{
			if (Model.movesEqual(move, moveActions, m, context))
			{
				realMoveToApply = m;
				break;
			}
		}
		
		return realMoveToApply;
	}
	
	/**
	 * Apply a move to the current context
	 * 
	 * @param context      The context.
	 * @param move         The move to apply.
	 * @param skipEndRules If true, we do not spend any time computing end rules.
	 *                     Note that this can make the resulting context unsuitable
	 *                     for further use.
	 * @return Applied move (with consequents resolved etc.
	 */
	public Move apply(final Context context, final Move move, final boolean skipEndRules)
	{
		context.getLock().lock();
		
		try
		{
			// Save data before applying end rules (for undo).
			context.storeCurrentData();
			
			// If a decision was done previously we reset it.
			if (context.state().isDecided() != Constants.UNDEFINED)
				context.state().setIsDecided(Constants.UNDEFINED);
			
			return applyInternal(context, move, skipEndRules);
		}
		finally
		{
			context.getLock().unlock();
		}
	}
	
	/**
	 * @param context      The context.
	 * @param move         The move to apply.
	 * @param skipEndRules If true, we do not spend any time computing end rules.
	 *                     Note that this can make the resulting context unsuitable
	 *                     for further use.
	 * 
	 * @return The move applied without forced play.
	 */
	public Move applyInternal(final Context context, final Move move, final boolean skipEndRules)
	{
		final Trial trial = context.trial();
		final State state = context.state();
		final Game game = context.game();
		final int mover = state.mover();
		
		if (move.isPass() && !state.isStalemated(mover))
		{
			// probably means our stalemated flag was incorrectly not set to true,
			// make sure it's correct
			computeStalemated(context);
		}

		state.rebootPending();

		final Move returnMove = (Move) move.apply(context, true); // update the game state
		
		if (skipEndRules)	// We'll leave the state in a half-finished state and just return directly
			return returnMove;
	
//		System.out.println("Last move: " + context.trial().lastMove());
//		System.out.println("lastTo=" + context.trial().lastMove().getTo());
		
		// Check game status here
		if (trial.numMoves() > game.numStartingAction)
		{
			// Only check if action not part of setup
			if (context.active() && game.rules.phases() != null)
			{
				final End endPhase = game.rules.phases()[state.currentPhase(state.mover())].end();
				if (endPhase != null)
					endPhase.eval(context);
			}

			if (!trial.over())
			{
				final End endRule = game.rules.end();

				if (endRule != null)
					endRule.eval(context);
			}

			if (context.active() && checkMaxTurns(context))
			{
				int winner = 0;

				if (game.players().count() > 1)
				{
					final double score = context.computeNextDrawRank();
					assert(score >= 1.0 && score <= trial.ranking().length);
					for (int player = 1; player < trial.ranking().length; player++)
					{
						if (trial.ranking()[player] == 0.0)
						{
							trial.ranking()[player] = score;
						}
						else if (context.trial().ranking()[player] == 1.0)
						{
							winner = player;
						}
					}
				}
				else
				{
					trial.ranking()[1] = 0.0;
				}

				context.setAllInactive();
				
				EndType endType = EndType.NaturalEnd;
				if (state.numTurn() >= getMaxTurnLimit() * players.count())
					endType = EndType.TurnLimit;
				else if ((trial.numMoves() - trial.numInitialPlacementMoves()) >= getMaxMoveLimit())
					endType = EndType.MoveLimit;

				trial.setStatus(new Status(winner, endType));
			}

			if (!context.active())
			{
				state.setPrev(mover);
				// break;
			}
			else // We update the current Phase for each player if this is a game with phases.
			{
				if (game.rules.phases() != null)
				{
					for (int pid = 1; pid <= game.players().count(); pid++)
					{
						final Phase phase = game.rules.phases()[state.currentPhase(pid)];
						for (int i = 0; i < phase.nextPhase().length; i++)
						{
							final NextPhase cond = phase.nextPhase()[i];
							final int who = cond.who().eval(context);
							if (who == game.players.count() + 1 || pid == who)
							{
								final int nextPhase = cond.eval(context);
								if (nextPhase != Constants.UNDEFINED)
								{
									state.setPhase(pid, nextPhase);
									break;
								}
							}
						}

					}
				}
			}
			state.incrCounter();
		}
		
		if (usesNoRepeatPositionalInGame() && state.mover() != context.state().prev())
			trial.previousState().add(state.stateHash());
		else
			if (hasCycleDetection())
				trial.previousState().add(state.stateHash());
		
		if (usesNoRepeatPositionalInTurn())
		{
			if (state.mover() == state.prev())
			{
				trial.previousStateWithinATurn().add(state.stateHash());
			}
			else
			{
				trial.previousStateWithinATurn().clear();
				trial.previousStateWithinATurn().add(state.stateHash());
			}
		}

		if (usesNoRepeatSituationalInGame() && state.mover() != state.prev())
			trial.previousState().add(state.fullHash());

		if (usesNoRepeatSituationalInTurn())
		{
			if (state.mover() == state.prev())
			{
				trial.previousStateWithinATurn().add(state.fullHash());
			}
			else
			{
				trial.previousStateWithinATurn().clear();
				trial.previousStateWithinATurn().add(state.fullHash());
			}
		}

		if (context.active())
		{
			if (requiresVisited())
			{
				if (state.mover() != state.next())
				{
					state.reInitVisited();
				}
				else
				{
					final int from = returnMove.fromNonDecision();
					final int to = returnMove.toNonDecision();
					state.visit(from);
					state.visit(to);
				}
			}

			//context.setUnionFindCalled(false);
			//context.setUnionFindDeleteCalled(false);

			state.setPrev(mover);
			state.setMover(state.next());

			// Count the number of turns played by the same player
			if (state.prev() == state.mover() && !returnMove.isSwap())
				state.incrementNumTurnSamePlayer();
			else
				state.reinitNumTurnSamePlayer();

			int next = (state.mover()) % game.players.count() + 1;
			while (!context.active(next))
			{
				next++;
				if (next > game.players.count())
					next = 1;
			}
			state.setNext(next);
			
			state.updateNumConsecutivePasses(returnMove.isPass());
		}
		
		// To update the sum of the dice container.
		if (hasHandDice())
		{
			for (int i = 0; i < handDice().size(); i++)
			{
				final Dice dice = handDice().get(i);
				final ContainerState cs = context.containerState(dice.index());

				final int siteFrom = context.sitesFrom()[dice.index()];
				final int siteTo = context.sitesFrom()[dice.index()] + dice.numSites();
				int sum = 0;
				for (int site = siteFrom; site < siteTo; site++)
				{
					sum += context.components()[cs.whatCell(site)].getFaces()[cs.stateCell(site)];
					//context.state().currentDice()[i][site - siteFrom] = context.components()[cs.whatCell(site)].getFaces()[cs.stateCell(site)];
				}
				state.sumDice()[i] = sum;
			}
		}

		// tell the trial that it can save its current state (if it wants)
		// need to do this last because mover switching etc. is included in state
		trial.saveState(state);
		
		// Store the current RNG for the undo methods.
		final RandomProviderState randomProviderState = context.rng().saveState();
		trial.addRNGState(randomProviderState);

		trial.clearLegalMoves();
		
		// Make sure our "real" context's RNG actually gets used and progresses
		// For temporary copies of context, we need not do this
		if (!(context instanceof TempContext) && !trial.over() && context.game().isStochasticGame())
			context.game().moves(context);

		// System.out.println("RETURN MOVE IS " + returnMove);

		return returnMove;
	}
	
	/**
	 * To undo the last move previously played.
	 * @param context The context.
	 * @return The move applied to undo the last move played.
	 */
	@SuppressWarnings("static-method")
	public Move undo(final Context context)
	{
		context.getLock().lock();
		
		try
		{
			final Trial trial = context.trial();
			// Step 1: restore previous RNG.
			trial.removeLastRNGStates();
			if(!trial.RNGStates().isEmpty())
			{
				final RandomProviderState previousRNGState = trial.RNGStates().get(trial.RNGStates().size()-1);
				context.rng().restoreState(previousRNGState);
			}
			
			final Move move = context.trial().lastMove();
			move.undo(context, true);
			return move;
		}
		finally
		{
			context.getLock().unlock();
		}
	}
	
	/**
	 * Ensures that the stalemated flag for the current mover in the given context
	 * is set correctly. Calling this should not be necessary if game.moves() has
	 * already been called to compute the full list of legal moves for the current
	 * state of the trial, but may be necessary otherwise.
	 * 
	 * @param context The context.
	 */
	public void computeStalemated(final Context context)
	{
		final Trial trial = context.trial();
		final State state = context.state();
		final Game game = context.game();
		final int mover = state.mover();
		
		if (isAlternatingMoveGame())
		{
			if (context.active())
			{
				final int indexPhase = state.currentPhase(mover);
				final Phase phase = context.game().rules().phases()[indexPhase];
				boolean foundLegalMove = false;

				if (metaRules.usesSwapRule() && trial.moveNumber() == context.game().players.count() - 1)
					foundLegalMove = true;
				else
					foundLegalMove = phase.play().moves().canMove(context);
				
				context.state().setStalemated(state.mover(), !foundLegalMove);
			}
		}
		else
		{
			// Simultaneous-move case too difficult to deal with, just compute
			// all legal moves to ensure stalemated flag is set
			game.moves(context);
		}
	}
	
	/**
	 * Tries to add some custom playout implementations to phases where possible.
	 */
	private void addCustomPlayouts()
	{
		if (mode.playout() == null)
		{
			// No global playout implementation specified
			for (final Phase phase : rules.phases())
			{
				if (phase.playout() == null)
				{
					// No phase-specific playout implementation specified
					final Moves moves = phase.play().moves();

					// NOTE: not using equals() here is intentional! Most of our Moves
					// do not have proper equals() implementations
					if (!hasLargePiece() && moves instanceof Add && moves.then() == null)
					{
						final Add to = (Add) moves;
						if
						(
							to.components().length == 1 &&
							to.components()[0] instanceof Mover &&
							to.region() instanceof SitesEmpty.EmptyDefault &&
							to.legal() == null &&
							!to.onStack()
						)
						{
							if 
							(
								mode.mode() == ModeType.Alternating &&
								!usesNoRepeatPositionalInGame() &&
								!usesNoRepeatPositionalInTurn()
							)
							{
								// We can safely use AddToEmpty playouts in this phase
								phase.setPlayout(new PlayoutAddToEmpty(to.type()));
							}
						}
					}
					else if (!isStochasticGame())
					{
						if (moves instanceof Do)
						{
							if (((Do) moves).ifAfter() != null)
							{
								// We can safely use FilterPlayouts in this phase
								phase.setPlayout(new PlayoutFilter());
							}
						}
						else if (moves instanceof game.rules.play.moves.nonDecision.operators.logical.If)
						{
							final game.rules.play.moves.nonDecision.operators.logical.If ifRule = (game.rules.play.moves.nonDecision.operators.logical.If) moves;
	
							if (ifRule.elseList() instanceof Do)
							{
								if (((Do) ifRule.elseList()).ifAfter() != null)
								{
									// We can safely use FilterPlayouts in this phase
									phase.setPlayout(new PlayoutFilter());
								}
							}
						}
						else if (moves instanceof game.rules.play.moves.nonDecision.operators.logical.Or)
						{
							final game.rules.play.moves.nonDecision.operators.logical.Or orRule = (game.rules.play.moves.nonDecision.operators.logical.Or) moves;
							
							if 
							(
								orRule.list().length == 2 
								&&
								orRule.list()[0] instanceof Do 
								&&
								orRule.list()[1] instanceof game.rules.play.moves.nonDecision.effect.Pass
							)
							{
								if (((Do) orRule.list()[0]).ifAfter() != null)
								{
									// This is also a FilterPlayouts case we support (for Go for example)
									phase.setPlayout(new PlayoutFilter());
								}
							}
						}
					}
					
					if 
					(
						phase.playout() == null 
						&& 
						(usesNoRepeatPositionalInGame() || usesNoRepeatPositionalInTurn())
					)
					{
						// Can use no-repetition-playout
						phase.setPlayout(new PlayoutNoRepetition());
					}
				}
			}
		}
	}
	
	/**
	 * Executes post-creation steps.
	 */
	private void postCreation()
	{
		//Optimiser.optimiseGame(this);
		checkAddMoveCaches(this, true, new HashMap<Object, Set<String>>());
	}
	
	/**
	 * Traverses the tree of ludemes, and disables move caches
	 * in Add-move-generators that are inside other move generators
	 * with consequents (because otherwise consequents keep getting
	 * added to the same Move objects all the time).
	 * 
	 * @param ludeme Root of subtree to traverse
	 * @param inAllowCache If false, we'll disable cache usage.
	 * @param visited Map of fields we've already visited, to avoid cycles
	 */
	private static void checkAddMoveCaches
	(
		final Ludeme ludeme, 
		final boolean inAllowCache,
		final Map<Object, Set<String>> visited
	)
	{
		final Class<? extends Ludeme> clazz = ludeme.getClass();
		final List<Field> fields = ReflectionUtils.getAllFields(clazz);
		
		try
		{
			for (final Field field : fields)
			{
				if (field.getName().contains("$"))
					continue;
				
				field.setAccessible(true);
				
				if ((field.getModifiers() & Modifier.STATIC) != 0)
					continue;
				
				if (visited.containsKey(ludeme) && visited.get(ludeme).contains(field.getName()))
					continue;		// avoid stack overflow
								
				final Object value = field.get(ludeme);

				if (!visited.containsKey(ludeme))
					visited.put(ludeme, new HashSet<String>());
				
				visited.get(ludeme).add(field.getName());
				
				if (value != null)
				{
					final Class<?> valueClass = value.getClass();
					
					if (Enum.class.isAssignableFrom(valueClass))
						continue;

					if (Ludeme.class.isAssignableFrom(valueClass))
					{
						final Ludeme innerLudeme = (Ludeme) value;
						final boolean allowCache = inAllowCache && allowsToActionCaches(ludeme, innerLudeme);
						setActionCacheAllowed(innerLudeme, allowCache);
						checkAddMoveCaches(innerLudeme, allowCache, visited);
					}
					else if (valueClass.isArray())
					{
						final Object[] array = ReflectionUtils.castArray(value);
						
						for (final Object element : array)
						{
							if (element != null)
							{
								final Class<?> elementClass = element.getClass();
								
								if (Ludeme.class.isAssignableFrom(elementClass))
								{
									final Ludeme innerLudeme = (Ludeme) element;
									final boolean allowCache =  inAllowCache 
																&&
																allowsToActionCaches(ludeme, innerLudeme);
									setActionCacheAllowed(innerLudeme, allowCache);
									checkAddMoveCaches(innerLudeme, allowCache, visited);
								}
							}
						}
					}
					else if (Iterable.class.isAssignableFrom(valueClass))
					{
						final Iterable<?> iterable = (Iterable<?>) value;
						
						for (final Object element : iterable)
						{
							if (element != null)
							{
								final Class<?> elementClass = element.getClass();
								
								if (Ludeme.class.isAssignableFrom(elementClass))
								{
									final Ludeme innerLudeme = (Ludeme) element;
									final boolean allowCache =  inAllowCache 
																&& 
																allowsToActionCaches(ludeme, innerLudeme);
									setActionCacheAllowed(innerLudeme, allowCache);
									checkAddMoveCaches(innerLudeme, allowCache, visited);
								}
							}
						}
					}
				}
			}
		}
		catch (final IllegalArgumentException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Helper method for recursive method above.
	 * 
	 * @param outerLudeme
	 * @param innerLudeme
	 * @return True if the given ludeme allows Add-ludemes or Claim-ludemes rooted in the 
	 * 	inner ludeme to use action cache
	 */
	private static boolean allowsToActionCaches(final Ludeme outerLudeme, final Ludeme innerLudeme)
	{
		if (outerLudeme instanceof Moves)
		{
			if (innerLudeme instanceof Then)
				return true;	// consequents subtree itself is fine
			
			return (((Moves) outerLudeme).then() == null);
		}
		
		return true;
	}
	
	/**
	 * Helper method for recursive method above. Disables action cache if not allowed
	 * on any given ludemes that are of type Add or Claim.
	 * 
	 * @param ludeme
	 * @param allowed
	 */
	private static void setActionCacheAllowed(final Ludeme ludeme, final boolean allowed)
	{
		if (allowed)
			return;
		
		if (ludeme instanceof game.rules.play.moves.nonDecision.effect.Add)
			((game.rules.play.moves.nonDecision.effect.Add) ludeme).disableActionCache();
		else if (ludeme instanceof game.rules.play.moves.nonDecision.effect.Claim)
			((game.rules.play.moves.nonDecision.effect.Claim) ludeme).disableActionCache();
	}
	
	/**
	 * Run a full playout for a game.
	 */
	@Override
	public Trial playout
	(
		final Context context, final List<AI> ais, final double thinkingTime,
		final PlayoutMoveSelector playoutMoveSelector, final int maxNumBiasedActions, 
		final int maxNumPlayoutActions, final Random random
	)
	{
		// TODO should maybe turn this check into an assertion? or always run it???
//		if (!context.haveStarted())
//			System.err.println("Didn't start!");
		
		final Random rng = (random != null) ? random : ThreadLocalRandom.current();
		
		Trial trial = context.trial();
		final int numStartMoves = trial.numMoves();
		while (true)
		{
			if 
			(
				trial.over() 
				|| 
				(
					maxNumPlayoutActions >= 0 
					&& 
					trial.numMoves() - numStartMoves >= maxNumPlayoutActions
				)
			)
				break;
			
			final int numAllowedActions;
			final int numAllowedBiasedActions;
			
			if (maxNumPlayoutActions >= 0)
				numAllowedActions = Math.max(0, maxNumPlayoutActions - (trial.numMoves() - numStartMoves));
			else
				numAllowedActions = maxNumPlayoutActions;
			
			if (maxNumBiasedActions >= 0)
				numAllowedBiasedActions = Math.max(0, maxNumBiasedActions - (trial.numMoves() - numStartMoves));
			else
				numAllowedBiasedActions = maxNumBiasedActions;
			
			final Phase phase = rules().phases()[context.state().currentPhase(context.state().mover())];
			if (phase.playout() != null)
			{
				trial = phase.playout().playout(context, ais, thinkingTime, playoutMoveSelector, 
						numAllowedBiasedActions, numAllowedActions, rng);
			}
			else if (context.game().mode().playout() != null)
			{
				trial = context.game().mode().playout().playout(context, ais, thinkingTime, playoutMoveSelector,
						numAllowedBiasedActions, numAllowedActions, rng);
			}
			else
			{
				trial = context.model().playout(context, ais, thinkingTime, playoutMoveSelector, 
						numAllowedBiasedActions, numAllowedActions, rng);
			}
		}
		
		return trial;
	}
	
	/**
	 * To Compute the constraint variables from the set of constraints.
	 * 
	 * @param constraints The constraints.
	 * @param context     The context.
	 */
	public void initConstraintVariables(final BooleanFunction[] constraints, final Context context)
	{
//		if (context.game().equipment().verticesWithHints().length != 0)
//		{
//			if (context.game().equipment().verticesWithHints() != null)
//				for (int i = 0; i < context.game().equipment().verticesWithHints().length; i++)
//					for (int j = 0; j < context.game().equipment().verticesWithHints()[i].length; j++)
//						if (!constraintVariables
//								.contains(context.game().equipment().verticesWithHints()[i][j].intValue()))
//							constraintVariables.add(context.game().equipment().verticesWithHints()[i][j].intValue());
//		}

		for (final BooleanFunction constraint: constraints) 
		{
			if (constraint instanceof ForAll)
			{
				final int numSite = context.topology().getGraphElements(context.board().defaultSite()).size();
				for (int index = 0; index < numSite; index++)
				{
					if (!constraintVariables.contains(index))
						constraintVariables.add(index);
				}
				break;
			}
			else
			{
				if (constraint.staticRegion() != null && constraint.staticRegion().equals(RegionTypeStatic.Regions)) 
				{
					final Regions[] regions = context.game().equipment().regions();
					for (final Regions region : regions)
					{
						if (region.regionTypes() != null) 
						{
							final RegionTypeStatic[] areas = region.regionTypes();
							for (final RegionTypeStatic area : areas) 
							{
								final Integer[][] regionsList = region.convertStaticRegionOnLocs(area, context);
								for (final Integer[] locs : regionsList)
								{
									for (final Integer loc : locs)
										if (loc != null && !constraintVariables.contains(loc.intValue()))
											constraintVariables.add(loc.intValue());
								}
							}
						}
						else if (region.sites() != null)
						{
							for (final int loc : region.sites())
							{
								if (!constraintVariables.contains(loc))
									constraintVariables.add(loc);
							}
						}
						else if (region.region() != null)
						{
							for (final RegionFunction regionFn : region.region())
							{
								final int[] sites = regionFn.eval(context).sites();
								for (final int loc : sites)
								{
									if (!constraintVariables.contains(loc))
										constraintVariables.add(loc);
								}
							}
						}
					}
				}
				else
				{
					if (constraint.locsConstraint() != null) 
					{
						for (final IntFunction function: constraint.locsConstraint()) 
						{
							final int index = function.eval(context);
							if(!constraintVariables.contains(index))
								constraintVariables.add(index);
						}
					}
					else
					{
						if (constraint.regionConstraint() != null) 
						{
							final int[] sites = constraint.regionConstraint().eval(context).sites();
							for (final int index : sites)
								if (!constraintVariables.contains(index))
									constraintVariables.add(index);
						}
					}
				}
			}
		}

	}

	//-----------------------------------Max Turn------------------------------

	/**
	 * @param context
	 * @return True if the max turn limit or move limit is reached.
	 */
	public boolean checkMaxTurns(final Context context)
	{
		return (context.state().numTurn() >= getMaxTurnLimit() * players.count()
				|| (context.trial().numMoves() - context.trial().numInitialPlacementMoves()) >= getMaxMoveLimit());
	}

	/**
	 * To set the max turn limit.
	 * 
	 * @param limitTurn
	 */
	public void setMaxTurns(final int limitTurn)
	{
		maxTurnLimit = limitTurn;
		
		if (isDeductionPuzzle())
			setMaxMoveLimit(limitTurn);
	}
	
	/**
	 * @return Max number of turns for this game
	 */
	public int getMaxTurnLimit()
	{
		return maxTurnLimit;
	}
	
	/**
	 * Set the maximum number of moves for this game
	 * @param limitMoves
	 */
	public void setMaxMoveLimit(final int limitMoves)
	{
		maxMovesLimit = limitMoves;
	}
	
	/**
	 * @return Max number of moves for this game
	 */
	public int getMaxMoveLimit()
	{
		return maxMovesLimit;
	}
	
	/**
	 * @return Options selected when compiling this game.
	 */
	public List<String> getOptions() 
	{
		return options;
	}

	/**
	 * Set the options used when compiling this game.
	 * 
	 * NOTE: call hierarchy in Eclipse is not entirely accurate,
	 * this method also gets called (through Reflection) by compiler!
	 * 
	 * @param options
	 */
	public void setOptions(final List<String> options) 
	{
		this.options = options;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Ruleset that corresponds to the options selected when compiling this game.
	 */
	public Ruleset getRuleset()
	{
		final List<String> allOptions = description().gameOptions().allOptionStrings(getOptions());
		
		for (final Ruleset r : description().rulesets())
			if (!r.optionSettings().isEmpty())
				if (description().gameOptions().allOptionStrings(r.optionSettings()).equals(allOptions))
					return r;
		
		return null;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Disables any custom playout implementations in this game (or all of its
	 * instances in the case of a Match) that may cause trouble in cases where
	 * we need trials to memorise more info than just the applied moves (i.e. 
	 * custom playouts that do not call game.moves() to generate complete lists of 
	 * legal moves).
	 */
	public void disableMemorylessPlayouts()
	{
		if (mode().playout() != null && !mode().playout().callsGameMoves())
			mode().setPlayout(null);

		for (final Phase phase : rules().phases())
		{
			if (phase.playout() != null && !phase.playout().callsGameMoves())
				phase.setPlayout(null);
		}
	}
	
	/**
	 * Disables any custom playout implementations in this game (or all of its
	 * instances in the case of a Match).
	 */
	public void disableCustomPlayouts()
	{
		if (mode().playout() != null)
			mode().setPlayout(null);

		for (final Phase phase : rules().phases())
		{
			if (phase.playout() != null)
				phase.setPlayout(null);
		}
	}
	
	//-------------------------------------------------------------------------

	/**
	 * Dummy class to grant the Game class access to the State's
	 * non-copy-constructor.
	 * 
	 * No one else should call that constructor! Just copy our reference state!
	 * 
	 * @author Dennis Soemers
	 */
	public static final class StateConstructorLock
	{
		/** Singleton instance; INTENTIONALLY PRIVATE! Only we should use! */
		protected static final StateConstructorLock INSTANCE = new StateConstructorLock();
		
		/**
		 * Constructor; intentionally private! Only we should use!
		 */
		private StateConstructorLock()
		{
			// Do nothing
		}
	}

	// -----------------------------Graphics------------------------------------

	/**
	 * @return True if we've finished preprocessing, false otherwise.
	 */
	public boolean finishedPreprocessing()
	{
		return finishedPreprocessing;
	}
	
	/**
	 * @return True if the game contains a component owned by a shared player.
	 */
	public boolean hasSharedPlayer()
	{
		for (final Container comp : equipment().containers())
			if (comp.owner() > players.count())
				return true;
		
		return false;
	}

	/**
	 * To get a sub list of a moves corresponding to some specific coordinates (e.g.
	 * "A1" or "A1-B1"). Note: Uses for the API. Needs to be improved to take more
	 * parameters (level, siteType etc).
	 * 
	 * @param str           The coordinates corresponding the coordinates.
	 * @param context       The context.
	 * @param originalMoves The list of moves.
	 * @return The moves corresponding to these coordinates.
	 */
	public static List<Move> getMovesFromCoordinates
	(
		final String str, 
		final Context context,
		final List<Move> originalMoves
	)
	{
		final String fromCoordinate = str.contains("-") ? str.substring(0, str.indexOf('-')) : str;
		final String toCoordinate = str.contains("-") ? str.substring(str.indexOf('-') + 1, str.length())
				: fromCoordinate;

		final TopologyElement fromElement = SiteFinder.find(context.board(), fromCoordinate,
				context.board().defaultSite());

		final TopologyElement toElement = SiteFinder.find(context.board(), toCoordinate,
				context.board().defaultSite());

		final List<Move> moves = new ArrayList<Move>();

		// If the entry is wrong.
		if (fromElement == null || toElement == null)
			return moves;

		for (final Move m : originalMoves)
			if (m.fromNonDecision() == fromElement.index() && m.toNonDecision() == toElement.index())
				moves.add(m);

		return moves;
	}
	
	/**
	 * @return True if all piece types are not defined with P1 to P16 roletypes.
	 */
	public boolean noPieceOwnedBySpecificPlayer()
	{
		final String pieceDescribed = "(piece";
		final String expandedDesc = description.expanded();
		
		int index = expandedDesc.indexOf(pieceDescribed, 0);
		int startIndex = index;
		int numParenthesis = 0;
		while(index != Constants.UNDEFINED)
		{
			if(expandedDesc.charAt(index) == '(')
				numParenthesis++;
			else if(expandedDesc.charAt(index) == ')')
				numParenthesis--;
			
			// We get a complete piece description.
			if(numParenthesis == 0)
			{
				index++;
				String pieceDescription = expandedDesc.substring(startIndex+pieceDescribed.length()+1, index-1);
				
				// This piece description is part of the equipment.
				if(pieceDescription.charAt(0) == '\"')
				{
					pieceDescription = pieceDescription.substring(1);
					pieceDescription = pieceDescription.substring(pieceDescription.indexOf('\"')+1);
					int i = 0;
					for(; i < pieceDescription.length();)
					{
						char c = Character.toLowerCase(pieceDescription.charAt(i));
						
						// We found the roleType.
						if(c >= 'a' && c <= 'z')
						{
							int j = i;
							for(; j < pieceDescription.length();)
							{
								if(c < 'a' || c > 'z')
								{
									// This roletype is specific to a player.
									if(pieceDescription.substring(i, j).equals("P"))
										return false;
									break;
								}
								j++;
								if(j < pieceDescription.length())
									c = Character.toLowerCase(pieceDescription.charAt(j));
							}
							break;
						}
						else if(c == '(' || c == ')')
							break;
						
						i++;
					}
					
				}
				index = expandedDesc.indexOf(pieceDescribed, index);
				startIndex = index;
				numParenthesis = 0;
			}
			else
				index++;
		}
		
		return true;
	}

}