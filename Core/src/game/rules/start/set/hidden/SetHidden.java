package game.rules.start.set.hidden;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import game.Game;
import game.functions.booleans.BooleanConstant;
import game.functions.booleans.BooleanFunction;
import game.functions.ints.IntConstant;
import game.functions.ints.IntFunction;
import game.rules.start.StartRule;
import game.types.board.HiddenData;
import game.types.board.SiteType;
import game.types.play.RoleType;
import game.types.state.GameType;
import gnu.trove.list.array.TIntArrayList;
import main.StringRoutines;
import other.IntArrayFromRegion;
import other.PlayersIndices;
import other.action.Action;
import other.action.hidden.ActionSetHidden;
import other.action.hidden.ActionSetHiddenCount;
import other.action.hidden.ActionSetHiddenRotation;
import other.action.hidden.ActionSetHiddenState;
import other.action.hidden.ActionSetHiddenValue;
import other.action.hidden.ActionSetHiddenWhat;
import other.action.hidden.ActionSetHiddenWho;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;

/**
 * Sets the hidden information of a region.
 * 
 * @author Eric.Piette
 */
@Hide
public final class SetHidden extends StartRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Hidden data type. */
	private final HiddenData[] dataTypes;
	
	/** Which region. */
	private final IntArrayFromRegion region;

	/** Level. */
	private final IntFunction levelFn;
	
	/** Value to set. */
	private final BooleanFunction valueFn;

	/** The player to set the hidden information. */
	private final IntFunction whoFn;

	/** The RoleType if used */
	private final RoleType roleType;

	/** Cell/Edge/Vertex. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * To set hidden information.
	 * 
	 * @param dataTypes  The types of hidden data [Invisible].
	 * @param type       The graph element type [default of the board].
	 * @param region     The region to set the hidden information.
	 * @param level      The level to set the hidden information [0].
	 * @param value      The value to set [True].
	 * @param to         The roleType with these hidden information.
	 */
	public SetHidden
	(
		      @Opt  final HiddenData[]       dataTypes, 
			  @Opt  final SiteType           type, 
			        final IntArrayFromRegion region,
		@Name @Opt  final IntFunction        level,
		      @Opt  final BooleanFunction    value, 
	   @Name        final RoleType           to
	)
	{
		this.dataTypes = dataTypes;
		this.region = region;
		levelFn = (level == null) ? new IntConstant(0) : level;
		valueFn = (value == null) ? new BooleanConstant(true) : value;
		this.type = type;
		whoFn = RoleType.toIntFunction(to);
		roleType = to;
	}

	//-------------------------------------------------------------------------

	@Override
	public void eval(final Context context)
	{
		final int[] sites = region.eval(context);
		final int level = levelFn.eval(context);
		final SiteType realType = (type != null) ? type : context.game().board().defaultSite();
		final boolean value = valueFn.eval(context);
		final int who = whoFn.eval(context);
		final int numPlayers = context.game().players().count();

		if (RoleType.manyIds(roleType))
		{
			final TIntArrayList idPlayers = PlayersIndices.getIdRealPlayers(context, roleType);
			if (dataTypes == null)
			{
				for (final int site : sites)
				{
					for (int i = 0; i < idPlayers.size(); i++)
					{
						final int pid = idPlayers.get(i);
						final Action action = new ActionSetHidden(pid, realType, site, level, value);
						action.apply(context, true);
						final Move move = new Move(action);
						context.trial().addMove(move);
						context.trial().addInitPlacement();
					}
				}
			}
			else
			{
				for (final HiddenData hiddenData : dataTypes)
				{
					switch (hiddenData)
					{
					case What:
						for (final int site : sites)
						{
							for (int i = 0; i < idPlayers.size(); i++)
							{
								final int pid = idPlayers.get(i);
								final Action action = new ActionSetHiddenWhat(pid, realType, site, level, value);
								action.apply(context, true);
								final Move move = new Move(action);
								context.trial().addMove(move);
								context.trial().addInitPlacement();
							}
						}
						break;
					case Who:
						for (final int site : sites)
						{
							for (int i = 0; i < idPlayers.size(); i++)
							{
								final int pid = idPlayers.get(i);
								final Action action = new ActionSetHiddenWho(pid, realType, site, level, value);
								action.apply(context, true);
								final Move move = new Move(action);
								context.trial().addMove(move);
								context.trial().addInitPlacement();
							}
						}
						break;
					case State:
						for (final int site : sites)
						{
							for (int i = 0; i < idPlayers.size(); i++)
							{
								final int pid = idPlayers.get(i);
								final Action action = new ActionSetHiddenState(pid, realType, site, level, value);
								action.apply(context, true);
								final Move move = new Move(action);
								context.trial().addMove(move);
								context.trial().addInitPlacement();
							}
						}
						break;
					case Count:
						for (final int site : sites)
						{
							for (int i = 0; i < idPlayers.size(); i++)
							{
								final int pid = idPlayers.get(i);
								final Action action = new ActionSetHiddenCount(pid, realType, site, level, value);
								action.apply(context, true);
								final Move move = new Move(action);
								context.trial().addMove(move);
								context.trial().addInitPlacement();
							}
						}
						break;
					case Rotation:
						for (final int site : sites)
						{
							for (int i = 0; i < idPlayers.size(); i++)
							{
								final int pid = idPlayers.get(i);
								final Action action = new ActionSetHiddenRotation(pid, realType, site, level, value);
								action.apply(context, true);
								final Move move = new Move(action);
								context.trial().addMove(move);
								context.trial().addInitPlacement();
							}
						}
						break;
					case Value:
						for (final int site : sites)
						{
							for (int i = 0; i < idPlayers.size(); i++)
							{
								final int pid = idPlayers.get(i);
								final Action action = new ActionSetHiddenValue(pid, realType, site, level, value);
								action.apply(context, true);
								final Move move = new Move(action);
								context.trial().addMove(move);
								context.trial().addInitPlacement();
							}
						}
						break;
					default:
						break;
					}
				}
			}
		}
		else
		{
			if (who >= 1 && who <= numPlayers) // The player has to be a real player.
			{
				if (dataTypes == null)
				{
					for (final int site : sites)
					{
						final Action action = new ActionSetHidden(who, realType, site, level, value);
						action.apply(context, true);
						final Move move = new Move(action);
						context.trial().addMove(move);
						context.trial().addInitPlacement();
					}
				}
				else
				{
					for (final HiddenData hiddenData : dataTypes)
					{
						switch (hiddenData)
						{
						case What:
							for (final int site : sites)
							{
								final Action action = new ActionSetHiddenWhat(who, realType, site, level, value);
								action.apply(context, true);
								final Move move = new Move(action);
								context.trial().addMove(move);
								context.trial().addInitPlacement();
							}
							break;
						case Who:
							for (final int site : sites)
							{
								final Action action = new ActionSetHiddenWho(who, realType, site, level, value);
								action.apply(context, true);
								final Move move = new Move(action);
								context.trial().addMove(move);
								context.trial().addInitPlacement();
							}
							break;
						case State:
							for (final int site : sites)
							{
								final Action action = new ActionSetHiddenState(who, realType, site, level, value);
								action.apply(context, true);
								final Move move = new Move(action);
								context.trial().addMove(move);
								context.trial().addInitPlacement();
							}
							break;
						case Count:
							for (final int site : sites)
							{
								final Action action = new ActionSetHiddenCount(who, realType, site, level, value);
								action.apply(context, true);
								final Move move = new Move(action);
								context.trial().addMove(move);
								context.trial().addInitPlacement();
							}
							break;
						case Rotation:
							for (final int site : sites)
							{
								final Action action = new ActionSetHiddenRotation(who, realType, site, level, value);
								action.apply(context, true);
								final Move move = new Move(action);
								context.trial().addMove(move);
								context.trial().addInitPlacement();
							}
							break;
						case Value:
							for (final int site : sites)
							{
								final Action action = new ActionSetHiddenValue(who, realType, site, level, value);
								action.apply(context, true);
								final Move move = new Move(action);
								context.trial().addMove(move);
								context.trial().addInitPlacement();
							}
							break;
						default:
							break;
						}
					}
				}
			}
		}
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = GameType.HiddenInfo;

		gameFlags |= SiteType.gameFlags(type);
		gameFlags |= region.gameFlags(game);
		gameFlags |= levelFn.gameFlags(game);
		gameFlags |= valueFn.gameFlags(game);
		gameFlags |= whoFn.gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		concepts.or(SiteType.concepts(type));
		concepts.or(region.concepts(game));
		concepts.or(levelFn.concepts(game));
		concepts.or(valueFn.concepts(game));
		concepts.or(whoFn.concepts(game));
		concepts.set(Concept.HiddenInformation.id(), true);
		if (dataTypes == null)
			concepts.set(Concept.InvisiblePiece.id(), true);
		else
			for (final HiddenData dataType : dataTypes)
			{
				switch (dataType)
				{
				case What:
					concepts.set(Concept.HidePieceType.id(), true);
					break;
				case Who:
					concepts.set(Concept.HidePieceOwner.id(), true);
					break;
				case Count:
					concepts.set(Concept.HidePieceCount.id(), true);
					break;
				case Value:
					concepts.set(Concept.HidePieceValue.id(), true);
					break;
				case Rotation:
					concepts.set(Concept.HidePieceRotation.id(), true);
					break;
				case State:
					concepts.set(Concept.HidePieceState.id(), true);
					break;
				default:
					break;
				}
			}
		
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(region.writesEvalContextRecursive());
		writeEvalContext.or(levelFn.writesEvalContextRecursive());
		writeEvalContext.or(valueFn.writesEvalContextRecursive());
		writeEvalContext.or(whoFn.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(region.readsEvalContextRecursive());
		readEvalContext.or(levelFn.readsEvalContextRecursive());
		readEvalContext.or(valueFn.readsEvalContextRecursive());
		readEvalContext.or(whoFn.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= region.missingRequirement(game);
		missingRequirement |= levelFn.missingRequirement(game);
		missingRequirement |= valueFn.missingRequirement(game);
		missingRequirement |= whoFn.missingRequirement(game);

		if (roleType != null && !game.requiresTeams())
		{
			if (RoleType.isTeam(roleType) && !game.requiresTeams())
			{
				game.addRequirementToReport(
						"(set Hidden ...): A roletype corresponding to a team is used but the game has no team: "
								+ roleType + ".");
				missingRequirement = true;
			}

			final int indexRoleType = roleType.owner();
			if (indexRoleType > game.players().count())
			{
				game.addRequirementToReport(
						"The roletype used in the rule (set Hidden ...) is wrong: " + roleType + ".");
				missingRequirement = true;
			}
		}

		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= region.willCrash(game);
		willCrash |= levelFn.willCrash(game);
		willCrash |= valueFn.willCrash(game);
		willCrash |= whoFn.willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		region.preprocess(game);
		levelFn.preprocess(game);
		valueFn.preprocess(game);
		whoFn.preprocess(game);
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		String hiddenDataTypeString = "all properties";
		if (dataTypes != null)
		{
			hiddenDataTypeString = "";
			for (final HiddenData h : dataTypes)
				hiddenDataTypeString += h.name().toLowerCase() + ", ";
			hiddenDataTypeString = "properties " + hiddenDataTypeString.substring(0, hiddenDataTypeString.length()-2);
		}
		
		String regionString = "";
		if (region != null)
			regionString = " in region " + region.toEnglish(game);
		
		String levelString = "";
		if (levelFn != null)
			levelString = " at level " + levelFn.toEnglish(game);
		
		String valueString = "";
		if (valueFn != null)
			valueString = " to value " + valueFn.toEnglish(game);
		
		String whoString = "";
		if (whoFn != null)
			whoString = " for Player " + whoFn.toEnglish(game);
		else if (roleType != null)
			whoString = " for " + roleType.name().toLowerCase();
		
		String typeString = " sites";
		if (type != null)
			typeString = " " + type.name().toLowerCase() + StringRoutines.getPlural(type.name()) + " ";
		
		return "set the hidden values for " + hiddenDataTypeString + valueString + whoString + " at all" + typeString + regionString + levelString;
	}
	
	//-------------------------------------------------------------------------

}
