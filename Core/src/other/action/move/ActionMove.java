package other.action.move;

import java.util.BitSet;
import java.util.List;

import game.Game;
import game.equipment.component.Component;
import game.equipment.container.board.Track;
import game.rules.play.moves.Moves;
import game.types.board.RelationType;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.DirectionFacing;
import game.util.graph.Radial;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.action.Action;
import other.action.ActionType;
import other.action.BaseAction;
import other.concept.Concept;
import other.context.Context;
import other.state.container.ContainerState;
import other.state.track.OnTrackIndices;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Moves a piece from a site to another (only one piece or one full stack).
 *
 * @author Eric.Piette
 */
public final class ActionMove extends BaseAction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The graph element type of the from site. */
	private final SiteType typeFrom;

	/** From site index. */
	private final int from;

	/** From level index (e.g. stacking game). */
	private int levelFrom;

	/** The graph element type of the to site. */
	private final SiteType typeTo;

	/** To site index. */
	private final int to;

	/** To level index (e.g. stacking game). */
	private final int levelTo;

	/** Site state value of the to site. */
	private final int state;

	/** Rotation value of the to site. */
	private final int rotation;

	/** piece value of the to site. */
	private final int value;

	/** Stacking game or not. */
	private final boolean onStacking;

	//----------------------Undo Data---------------------------------------------

	/** A variable to know that we already applied this action so we do not want to modify the data to undo if apply again. */
	private boolean alreadyApplied = false;

	//-- from data
	
	/** Previous Size stack of the from site. */
	private int previousSizeStackFrom;
	
	/** Previous Site state value of the from site. */
	private int previousStateFrom;

	/** Previous Rotation value of the from site. */
	private int previousRotationFrom;

	/** Previous Piece value of the from site. */
	private int previousValueFrom;

	/** Previous Count of the from site. */
	private int previousCountFrom;
	
	/** The previous hidden info values of the from site before to be removed. */
	private boolean[] previousHiddenFrom;
	
	/** The previous hidden what info values of the from site before to be removed. */
	private boolean[] previousHiddenWhatFrom;
	
	/** The previous hidden who info values of the from site before to be removed. */
	private boolean[] previousHiddenWhoFrom;

	/** The previous hidden count info values of the from site before to be removed. */
	private boolean[] previousHiddenCountFrom;

	/** The previous hidden rotation info values of the from site before to be removed. */
	private boolean[] previousHiddenRotationFrom;

	/** The previous hidden State info values of the from site before to be removed. */
	private boolean[] previousHiddenStateFrom;

	/** The previous hidden Value info values of the from site before to be removed. */
	private boolean[] previousHiddenValueFrom;
	
	//--- to data
	
	/** Previous Site state value of the to site. */
	private int previousStateTo;

	/** Previous Rotation value of the to site. */
	private int previousRotationTo;

	/** Previous Piece value of the to site. */
	private int previousValueTo;
	
	/** Previous What of the to site. */
	private int previousWhatTo;
	
	/** Previous Who of the to site. */
	private int previousWhoTo;

	/** Previous Count of the to site. */
	private int previousCountTo;
	
	/** The previous hidden info values of the to site before to be removed. */
	private boolean[] previousHiddenTo;
	
	/** The previous hidden what info values of the to site before to be removed. */
	private boolean[] previousHiddenWhatTo;
	
	/** The previous hidden who info values of the to site before to be removed. */
	private boolean[] previousHiddenWhoTo;

	/** The previous hidden count info values of the to site before to be removed. */
	private boolean[] previousHiddenCountTo;

	/** The previous hidden rotation info values of the to site before to be removed. */
	private boolean[] previousHiddenRotationTo;

	/** The previous hidden State info values of the to site before to be removed. */
	private boolean[] previousHiddenStateTo;

	/** The previous hidden Value info values of the to site before to be removed. */
	private boolean[] previousHiddenValueTo;
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param typeFrom   The graph element type of the from site.
	 * @param from       From site index.
	 * @param levelFrom  From level index.
	 * @param typeTo     The graph element type of the to site.
	 * @param to         To site index.
	 * @param levelTo    To level index.
	 * @param state      The state site of the to site.
	 * @param rotation   The rotation value of the to site.
	 * @param value      The piece value of the to site.
	 * @param onStacking True if we move a full stack.
	 */
	public ActionMove
	(
		final SiteType typeFrom,
		final int from,
		final int levelFrom,
		final SiteType typeTo,
		final int to,
		final int levelTo,
		final int state,
		final int rotation,
		final int value,
		final boolean onStacking
	)
	{
		this.typeFrom = typeFrom;
		this.from = from;
		this.levelFrom = levelFrom;
		this.typeTo = typeTo;
		this.to = to;
		this.levelTo = levelTo;
		this.state = state;
		this.rotation = rotation;
		this.value = value;
		this.onStacking = onStacking;
	}

	/**
	 * Reconstructs an ActionMove object from a detailed String
	 * (generated using toDetailedString())
	 * @param detailedString
	 */
	public ActionMove(final String detailedString)
	{
		assert (detailedString.startsWith("[Move:"));

		final String strTypeFrom = Action.extractData(detailedString, "typeFrom");
		typeFrom = (strTypeFrom.isEmpty()) ? null : SiteType.valueOf(strTypeFrom);

		final String strFrom = Action.extractData(detailedString, "from");
		from = Integer.parseInt(strFrom);

		final String strLevelFrom = Action.extractData(detailedString, "levelFrom");
		levelFrom = (strLevelFrom.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strLevelFrom);
		
		final String strTypeTo = Action.extractData(detailedString, "typeTo");
		typeTo = (strTypeTo.isEmpty()) ? null : SiteType.valueOf(strTypeTo);

		final String strTo = Action.extractData(detailedString, "to");
		to = Integer.parseInt(strTo);
		
		final String strLevelTo = Action.extractData(detailedString, "levelTo");
		levelTo = (strLevelTo.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strLevelTo);

		final String strState = Action.extractData(detailedString, "state");
		state = (strState.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strState);

		final String strRotation = Action.extractData(detailedString, "rotation");
		rotation = (strRotation.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strRotation);

		final String strValue = Action.extractData(detailedString, "value");
		value = (strValue.isEmpty()) ? Constants.UNDEFINED : Integer.parseInt(strValue);

		final String strStack = Action.extractData(detailedString, "stack");
		onStacking = (strStack.isEmpty()) ? false : Boolean.parseBoolean(strStack);

		final String strDecision = Action.extractData(detailedString, "decision");
		decision = (strDecision.isEmpty()) ? false : Boolean.parseBoolean(strDecision);
	}

	//-------------------------------------------------------------------------

	@Override
	public Action apply
	(
		final Context context,
		final boolean store
	)
	{
		final OnTrackIndices onTrackIndices = context.state().onTrackIndices();
		final int contIdFrom = typeFrom.equals(SiteType.Cell) ? context.containerId()[from] : 0;
		final int contIdTo = typeTo.equals(SiteType.Cell) ? context.containerId()[to] : 0;
		
		final boolean requiresStack = context.currentInstanceContext().game().isStacking();
		
		int currentStateFrom = Constants.UNDEFINED;
		int currentRotationFrom = Constants.UNDEFINED;
		int currentValueFrom = Constants.UNDEFINED;
		Component piece = null;

		final ContainerState csFrom = context.state().containerStates()[contIdFrom];
		final ContainerState csTo = context.state().containerStates()[contIdTo];
		
		// take the local state of the site from
		currentStateFrom = (levelFrom == Constants.UNDEFINED) ? 
				((csFrom.what(from, typeFrom) == 0) ? Constants.UNDEFINED : csFrom.state(from, typeFrom))
				: ((csFrom.what(from, levelFrom, typeFrom) == 0) ? Constants.UNDEFINED : csFrom.state(from, levelFrom, typeFrom))
				;
		currentRotationFrom = (levelFrom == Constants.UNDEFINED) ? csFrom.rotation(from, typeFrom) : csFrom.rotation(from, levelFrom, typeFrom);
		currentValueFrom =  (levelFrom == Constants.UNDEFINED) ? csFrom.value(from, typeFrom) : csFrom.value(from, levelFrom, typeFrom);
		
		// Keep in memory the data of the site from and to (for undo method)
		if(!alreadyApplied)
		{
			previousStateFrom = currentStateFrom;
			previousRotationFrom = currentRotationFrom;
			previousValueFrom = currentValueFrom;
			previousStateTo = (levelTo == Constants.UNDEFINED) ? csTo.state(to, typeTo) : csTo.state(to, levelTo, typeTo);
			previousRotationTo = (levelTo == Constants.UNDEFINED) ? csTo.rotation(to, typeTo) : csTo.rotation(to, levelTo, typeTo); 
			previousValueTo = (levelTo == Constants.UNDEFINED) ? csTo.value(to, typeTo) : csTo.value(to, levelTo, typeTo);
			previousWhoTo = (levelTo == Constants.UNDEFINED) ? csTo.who(to, typeTo) : csTo.who(to, levelTo, typeTo);
			previousWhatTo = (levelTo == Constants.UNDEFINED) ? csTo.what(to, typeTo) : csTo.what(to, levelTo, typeTo);
			
			if(onStacking)
				previousSizeStackFrom = csFrom.sizeStack(from, typeFrom);
			else
			{
				previousCountFrom = csFrom.count(from, typeFrom);
				previousCountTo = csTo.count(to, typeTo);
			}
			
			if (!requiresStack)
			{
				if(context.game().hiddenInformation())
				{
					previousHiddenFrom = new boolean[context.players().size()];
					previousHiddenWhatFrom = new boolean[context.players().size()];
					previousHiddenWhoFrom =  new boolean[context.players().size()];
					previousHiddenCountFrom =  new boolean[context.players().size()];
					previousHiddenStateFrom =  new boolean[context.players().size()];
					previousHiddenRotationFrom =  new boolean[context.players().size()];
					previousHiddenValueFrom =  new boolean[context.players().size()];
					for (int pid = 1; pid < context.players().size(); pid++)
					{
						previousHiddenFrom[pid] = csFrom.isHidden(pid, from, 0, typeFrom);
						previousHiddenWhatFrom[pid] = csFrom.isHiddenWhat(pid, from, 0, typeFrom);
						previousHiddenWhoFrom[pid] = csFrom.isHiddenWho(pid, from, 0, typeFrom);
						previousHiddenCountFrom[pid] = csFrom.isHiddenCount(pid, from, 0, typeFrom);
						previousHiddenStateFrom[pid] = csFrom.isHiddenState(pid, from, 0, typeFrom);
						previousHiddenRotationFrom[pid] = csFrom.isHiddenRotation(pid, from, 0, typeFrom);
						previousHiddenValueFrom[pid] = csFrom.isHiddenValue(pid, from, 0, typeFrom);
					}
					
					previousHiddenTo = new boolean[context.players().size()];
					previousHiddenWhatTo = new boolean[context.players().size()];
					previousHiddenWhoTo =  new boolean[context.players().size()];
					previousHiddenCountTo =  new boolean[context.players().size()];
					previousHiddenStateTo =  new boolean[context.players().size()];
					previousHiddenRotationTo =  new boolean[context.players().size()];
					previousHiddenValueTo =  new boolean[context.players().size()];
					for (int pid = 1; pid < context.players().size(); pid++)
					{
						previousHiddenTo[pid] = csTo.isHidden(pid, to, 0, typeTo);
						previousHiddenWhatTo[pid] = csTo.isHiddenWhat(pid, to, 0, typeTo);
						previousHiddenWhoTo[pid] = csTo.isHiddenWho(pid, to, 0, typeTo);
						previousHiddenCountTo[pid] = csTo.isHiddenCount(pid, to, 0, typeTo);
						previousHiddenStateTo[pid] = csTo.isHiddenState(pid, to, 0, typeTo);
						previousHiddenRotationTo[pid] = csTo.isHiddenRotation(pid, to, 0, typeTo);
						previousHiddenValueTo[pid] = csTo.isHiddenValue(pid, to, 0, typeTo);
					}
				}
			}
			alreadyApplied = true;
		}
		
		if (!requiresStack)
		{
			// If the origin is empty we do not apply this action.
			if (csFrom.what(from, typeFrom) == 0 && csFrom.count(from, typeFrom) == 0)
				return this;

			final int what = csFrom.what(from, typeFrom);
			final int count = csFrom.count(from, typeFrom);

			if (count == 1)
			{
				csFrom.remove(context.state(), from, typeFrom);

				// to keep the site of the item in cache for each player
				if (what != 0)
				{
					piece = context.components()[what];
					final int owner = piece.owner();
					context.state().owned().remove(owner, what, from, typeFrom);
				}

				// In case of LargePiece we update the empty chunkSet
				if (piece != null && piece.isLargePiece())
				{
					final Component largePiece = piece;
					final TIntArrayList locs = largePiece.locs(context, from, currentStateFrom, context.topology());
					for (int i = 0; i < locs.size(); i++)
					{
						csFrom.addToEmpty(locs.getQuick(i), SiteType.Cell);
						csFrom.setCount(context.state(), locs.getQuick(i), 0);
					}
					if (largePiece.isDomino() && context.containerId()[from] == 0)
					{
						for (int i = 0; i < 4; i++)
							csFrom.setValueCell(context.state(), locs.getQuick(i), largePiece.getValue());

						for (int i = 4; i < 8; i++)
							csFrom.setValueCell(context.state(), locs.getQuick(i), largePiece.getValue2());
					}
				}
			}
			else
			{
				csFrom.setSite(context.state(), from, Constants.UNDEFINED, Constants.UNDEFINED, count - 1, Constants.UNDEFINED,
						Constants.UNDEFINED, (context.game().usesLineOfPlay() ? 1 : Constants.OFF), typeFrom);
			}

			// update the local state of the site To
			if (currentStateFrom != Constants.UNDEFINED && state == Constants.UNDEFINED)
				csTo.setSite(context.state(), to, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, currentStateFrom,
						Constants.UNDEFINED, Constants.OFF, typeTo);
			else if (state != Constants.UNDEFINED)
				csTo.setSite(context.state(), to, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, state,
						Constants.UNDEFINED, Constants.OFF, typeTo);

			// update the rotation state of the site To
			if (currentRotationFrom != Constants.UNDEFINED && rotation == Constants.UNDEFINED)
				csTo.setSite(context.state(), to, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
						Constants.UNDEFINED, currentRotationFrom, Constants.OFF, typeTo);
			else if (rotation != Constants.UNDEFINED)
				csTo.setSite(context.state(), to, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
						Constants.UNDEFINED, rotation, Constants.OFF, typeTo);

			// update the piece value of the site To
			if (currentValueFrom != Constants.UNDEFINED && value == Constants.UNDEFINED)
				csTo.setSite(context.state(), to, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
						Constants.UNDEFINED, Constants.UNDEFINED, (context.game().usesLineOfPlay() ? 1 : currentValueFrom),
						typeTo);
			else if (value != Constants.UNDEFINED)
				csTo.setSite(context.state(), to, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
						Constants.UNDEFINED, Constants.UNDEFINED, (context.game().usesLineOfPlay() ? 1 : value),
						typeTo);
			
			final int who = (what < 1) ? 0 : context.components()[what].owner();

			if (csTo.what(to, typeTo) != 0 && (!context.game().requiresCount()
					|| context.game().requiresCount() && csTo.what(to, typeTo) != what))
			{
				final Component pieceToRemove = context.components()[csTo.what(to, typeTo)];
				final int owner = pieceToRemove.owner();
				context.state().owned().remove(owner, csTo.what(to, typeTo), to, typeTo);
			}

			if (csTo.what(to, typeTo) == what && csTo.count(to, typeTo) > 0)
			{
				csTo.setSite(context.state(), to, Constants.UNDEFINED, Constants.UNDEFINED,
						(context.game().requiresCount() ? csTo.count(to, typeTo) + 1 : 1),
						Constants.UNDEFINED, Constants.UNDEFINED,
						(context.game().usesLineOfPlay() ? 1 : Constants.OFF), typeTo);
			}
			else
			{
				csTo.setSite(context.state(), to, who, what, 1, Constants.UNDEFINED, Constants.UNDEFINED,
						(context.game().usesLineOfPlay() ? 1 : Constants.OFF), typeTo);
			}

			// to keep the site of the item in cache for each player
			if (what != 0 && csTo.count(to, typeTo) == 1)
			{
				piece = context.components()[what];
				final int owner = piece.owner();
				context.state().owned().add(owner, what, to, typeTo);
			}

			// In case of LargePiece we update the empty chunkSet
			if (piece != null && piece.isLargePiece())
			{
				final Component largePiece = piece;
				final TIntArrayList locs = largePiece.locs(context, to, state, context.topology());
				for (int i = 0; i < locs.size(); i++)
				{
					csTo.removeFromEmpty(locs.getQuick(i), SiteType.Cell);
					csTo.setCount(context.state(), locs.getQuick(i),
							(context.game().usesLineOfPlay() ? piece.index() : 1));
				}

				if (context.game().usesLineOfPlay() && context.containerId()[to] == 0)
				{
					for (int i = 0; i < 4; i++)
					{
						csTo.setValueCell(context.state(), locs.getQuick(i), largePiece.getValue());
					}

					for (int i = 4; i < 8; i++)
					{
						csTo.setValueCell(context.state(), locs.getQuick(i), largePiece.getValue2());
					}

					// We update the line of play for dominoes
					for (int i = 0; i < context.containers()[0].numSites(); i++)
						csTo.setPlayable(context.state(), i, false);

					for (int i = 0; i < context.containers()[0].numSites(); i++)
					{
						if (csTo.what(i, typeTo) != 0)
						{
							final Component currentComponent = context.components()[csTo.what(i, typeTo)];
							final int currentState = csTo.state(i, typeTo);
							final TIntArrayList locsToUpdate = largePiece.locs(context, i, currentState,
									context.topology());

							lineOfPlayDominoes(context, locsToUpdate.getQuick(0), locsToUpdate.getQuick(1),
									getDirnDomino(0, currentState), false, true);
							lineOfPlayDominoes(context, locsToUpdate.getQuick(7), locsToUpdate.getQuick(6),
									getDirnDomino(2, currentState), false, false);

							if (currentComponent.isDoubleDomino())
							{
								lineOfPlayDominoes(context, locsToUpdate.getQuick(2), locsToUpdate.getQuick(5),
										getDirnDomino(1, currentState), true, true);
								lineOfPlayDominoes(context, locsToUpdate.getQuick(3), locsToUpdate.getQuick(4),
										getDirnDomino(3, currentState), true, true);
							}
						}
					}
				}
			}

			// We update the structure about track indices if the game uses track.
			updateOnTrackIndices(what, onTrackIndices, context.board().tracks());

			// We keep the update for hidden info.
			if (context.game().hiddenInformation())
			{
				for (int pid = 1; pid < context.players().size(); pid++)
				{
					csTo.setHidden(context.state(), pid, to, 0, typeTo, csFrom.isHidden(pid, from, 0, typeFrom));
					csTo.setHiddenWhat(context.state(), pid, to, 0, typeTo, csFrom.isHiddenWhat(pid, from, 0, typeFrom));
					csTo.setHiddenWho(context.state(), pid, to, 0, typeTo, csFrom.isHiddenWho(pid, from, 0, typeFrom));
					csTo.setHiddenCount(context.state(), pid, to, 0, typeTo, csFrom.isHiddenCount(pid, from, 0, typeFrom));
					csTo.setHiddenRotation(context.state(), pid, to, 0, typeTo, csFrom.isHiddenRotation(pid, from, 0, typeFrom));
					csTo.setHiddenState(context.state(), pid, to, 0, typeTo, csFrom.isHiddenState(pid, from, 0, typeFrom));
					csTo.setHiddenValue(context.state(), pid, to, 0, typeTo, csFrom.isHiddenValue(pid, from, 0, typeFrom));
					if (csFrom.what(from, typeFrom) == 0)
					{
						csFrom.setHidden(context.state(), pid, from, 0, typeFrom, false);
						csFrom.setHiddenWhat(context.state(), pid, from, 0, typeFrom, false);
						csFrom.setHiddenWho(context.state(), pid, from, 0, typeFrom, false);
						csFrom.setHiddenCount(context.state(), pid, from, 0, typeFrom, false);
						csFrom.setHiddenRotation(context.state(), pid, from, 0, typeFrom, false);
						csFrom.setHiddenValue(context.state(), pid, from, 0, typeFrom, false);
						csFrom.setHiddenState(context.state(), pid, from, 0, typeFrom, false);
					}
				}
			}

			if (csTo.isEmpty(to, typeTo))
			{
				throw new RuntimeException("Did not expect locationTo to be empty at site locnTo="+to+"(who, what,count,state)=("
						+ csTo.who(to, typeTo) + "," + csTo.what(to, typeTo) + "," + csTo.count(to, typeTo)
								+ ","
								+ csTo.state(to, typeTo) + "," + csTo.state(to, typeTo) + ")");
			}
		}
		// on a stacking game
		else
		{
			if(from == to)
				return this;
			
			final ContainerState containerFrom = context.state().containerStates()[contIdFrom];
			final ContainerState containerTo = context.state().containerStates()[contIdTo];

			// To move a complete stack
			if(onStacking)
			{
				final int sizeStackFrom = containerFrom.sizeStack(from, typeFrom);
				for (int slevel = 0; slevel < containerFrom.sizeStack(from, typeFrom); slevel++)
				{
					if (levelTo == Constants.UNDEFINED)
						containerTo.addItemGeneric(context.state(), to, containerFrom.what(from, slevel, typeFrom),
								containerFrom.who(from, slevel, typeFrom), containerFrom.state(from, slevel, typeFrom),
								containerFrom.rotation(from, slevel, typeFrom), containerFrom.value(from, slevel, typeFrom),
								context.game(), typeTo);
					else
					{
						containerTo.insert(context.state(), typeTo, to, levelTo, containerFrom.what(from, slevel, typeFrom),
								containerFrom.who(from, slevel, typeFrom), state, rotation, value,
								context.game());
					}
				}

				// we update owned for loc From.
				for (int level = 0; level < containerFrom.sizeStack(from, typeFrom); level++)
				{
					final int whatFrom = containerFrom.what(from, level, typeFrom);
					if (whatFrom != 0)
					{
						final Component pieceFrom = context.components()[whatFrom];
						final int ownerFrom = pieceFrom.owner();
						if (ownerFrom != 0)
							context.state().owned().remove(ownerFrom, whatFrom, from, typeFrom);
					}
				}
				
				containerFrom.removeStackGeneric(context.state(), from, typeFrom);
				containerFrom.addToEmpty(from, typeFrom);
				containerTo.removeFromEmpty(to, typeTo);

				// we update owned for loc To.
				for (int level = containerTo.sizeStack(to, typeTo) - sizeStackFrom; level < containerTo.sizeStack(to, typeTo); level++)
				{
					if (level < 0)
						continue;
					final int whatTo = containerTo.what(to, level, typeTo);
					if (whatTo != 0)
					{
						final Component pieceTo = context.components()[whatTo];
						final int ownerTo = pieceTo.owner();
						if (ownerTo != 0)
							context.state().owned().add(ownerTo, whatTo, to, level, typeTo);
					}
				}
			}
			// to move only the top piece
			else if (levelFrom == Constants.UNDEFINED)
			{
				final int what = containerFrom.what(from, typeFrom);

				containerFrom.remove(context.state(), from, typeFrom);
				
				if (containerFrom.sizeStack(from, typeFrom) == 0)
					containerFrom.addToEmpty(from, typeFrom);

				final int who = (what < 1) ? 0 : context.components()[what].owner();

				if (!context.game().hasCard())
				{
					if (levelTo == Constants.UNDEFINED)
						containerTo.addItemGeneric(context.state(), to, what, who, context.game(), typeTo);
					else // ADD THE HIDDEN field FOR INSERTING A COMPONENT IN A STACK.
						containerTo.insertCell(context.state(), to, levelTo, what, who, state, rotation, value,
								context.game());
				}

				if (containerTo.sizeStack(to, typeTo) != 0)
					containerTo.removeFromEmpty(to, typeTo);

				// to keep the site of the item in cache for each player
				Component pieceFrom = null;
				int ownerFrom = 0;
				if (what != 0)
				{
					pieceFrom = context.components()[what];
					ownerFrom = pieceFrom.owner();
					context.state().owned().add(ownerFrom, what, to,
							containerTo.sizeStack(to, typeTo) - 1, typeTo);
					context.state().owned().remove(ownerFrom, what, from,
							containerFrom.sizeStack(from, typeFrom), typeFrom);
				}

				// We update the structure about track indices if the game uses track.
				updateOnTrackIndices(what, onTrackIndices, context.board().tracks());

//				if (context.state().onTrackIndices() != null)
//				{
//					System.out.println("TRACK UPDATED by ActionMove on stack top piece");
//					System.out.println(context.state().onTrackIndices());
//				}

			} // to move only a level of the stack.
			else
			{
				final int what = containerFrom.what(from, levelFrom, typeFrom);
				final int newStateTo = (state == Constants.UNDEFINED) ? containerFrom.state(from, levelFrom, typeFrom) : state;
				final int newRotationTo = (rotation == Constants.UNDEFINED) ? containerFrom.rotation(from, levelFrom, typeFrom) : rotation;
				final int newValueTo = (value == Constants.UNDEFINED) ? containerFrom.value(from, levelFrom, typeFrom) : value;
				
				containerFrom.remove(context.state(), from, levelFrom, typeFrom);

				if (containerFrom.sizeStack(from, typeFrom) == 0)
					containerFrom.addToEmpty(from, typeFrom);

				final int who = (what < 1) ? 0 : context.components()[what].owner();

				if (levelTo == Constants.UNDEFINED)
				{
					containerTo.addItemGeneric(context.state(), to, what, who, newStateTo, newRotationTo, newValueTo,
							context.game(), typeTo);

					if (containerTo.sizeStack(to, typeTo) != 0)
						containerTo.removeFromEmpty(to, typeTo);

					// to keep the site of the item in cache for each player
					Component pieceFrom = null;
					int ownerFrom = 0;
					if (what != 0)
					{
						pieceFrom = context.components()[what];
						ownerFrom = pieceFrom.owner();
						context.state().owned().add(ownerFrom, what, to, containerTo.sizeStack(to, typeTo) - 1, typeTo);
						context.state().owned().remove(ownerFrom, what, from, levelFrom, typeFrom);
					}
				}
				else
				{
					final int sizeStack = containerTo.sizeStack(to, typeTo);
					
					// we update the own list of the pieces on the top of that piece inserted.
					for (int i = sizeStack - 1; i >= levelTo; i--)
					{
						final int owner = containerTo.who(to, i, typeTo);
						final int pieceTo = containerTo.what(to, i, typeTo);
						context.state().owned().remove(owner, pieceTo, to, i, typeTo);
						context.state().owned().add(owner, pieceTo, to, i + 1, typeTo);
					}
					
					// We insert the piece.
					containerTo.insertCell(context.state(), to, levelTo, what, who, state, rotation, value, context.game());
				
					// we update the own list with the new piece
					final Component pieceTo = context.components()[what];
					final int owner = pieceTo.owner();
					context.state().owned().add(owner, what, to, levelTo, typeTo);

					if (containerTo.sizeStack(to, typeTo) != 0)
						containerTo.removeFromEmpty(to, typeTo);

					// to keep the site of the item in cache for each player
					Component pieceFrom = null;
					int ownerFrom = 0;
					if (what != 0)
					{
						pieceFrom = context.components()[what];
						ownerFrom = pieceFrom.owner();
						if (ownerFrom != 0)
							context.state().owned().remove(ownerFrom, what, from, levelFrom, typeFrom);
					}
				}

				// We update the structure about track indices if the game uses track.
				updateOnTrackIndices(what, onTrackIndices, context.board().tracks());
			}
		}
		
		return this;
	}

	/**
	 * To update the onTrackIndices after a move.
	 * 
	 * @param what           The index of the component moved.
	 * @param onTrackIndices The structure onTrackIndices
	 * @param tracks         The list of the tracks.
	 */
	public void updateOnTrackIndices(final int what, final OnTrackIndices onTrackIndices, final List<Track> tracks)
	{
		// We update the structure about track indices if the game uses track.
		if (what != 0 && onTrackIndices != null)
		{
			for (final Track track : tracks)
			{
				final int trackIdx = track.trackIdx();
				final TIntArrayList indicesLocA = onTrackIndices.locToIndex(trackIdx, from);

				for (int k = 0; k < indicesLocA.size(); k++)
				{
					final int indexA = indicesLocA.getQuick(k);
					final int countAtIndex = onTrackIndices.whats(trackIdx, what, indicesLocA.getQuick(k));

					if (countAtIndex > 0)
					{
						onTrackIndices.remove(trackIdx, what, 1, indexA);
						final TIntArrayList newWhatIndice = onTrackIndices.locToIndexFrom(trackIdx, to, indexA);

						if (newWhatIndice.size() > 0)
						{
							onTrackIndices.add(trackIdx, what, 1, newWhatIndice.getQuick(0));
						}
						else
						{
							final TIntArrayList newWhatIndiceIfNotAfter = onTrackIndices.locToIndex(trackIdx, to);
							if (newWhatIndiceIfNotAfter.size() > 0)
								onTrackIndices.add(trackIdx, what, 1, newWhatIndiceIfNotAfter.getQuick(0));
						}

						break;
					}
				}

				// If the piece was not in the track but enter on it, we update the structure
				// corresponding to that track.
				if (indicesLocA.size() == 0)
				{
					final TIntArrayList indicesLocB = onTrackIndices.locToIndex(trackIdx, to);
					if (indicesLocB.size() != 0)
						onTrackIndices.add(trackIdx, what, 1, indicesLocB.get(0));
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context)
	{
		// In undo ActionMove from = to and to = from.
		
		//final OnTrackIndices onTrackIndices = context.state().onTrackIndices();
		final int contIdTo = typeTo.equals(SiteType.Cell) ? context.containerId()[to] : 0;
		final int contIdFrom = typeFrom.equals(SiteType.Cell) ? context.containerId()[from] : 0;
		final Game game = context.game();
		
		final boolean requiresStack = context.currentInstanceContext().game().isStacking();
		
		if (!requiresStack)
		{
			// Nothing to do if no modification.
			if(from == to && state == Constants.UNDEFINED && rotation == Constants.UNDEFINED && value == Constants.UNDEFINED || previousCountFrom == 0) 
				return this;
			
			// System.out.println("loc is " + loc);
			final ContainerState csTo = context.state().containerStates()[contIdTo];
			final ContainerState csFrom = context.state().containerStates()[contIdFrom];

			// If the origin is empty we do not apply this action.
			if (csTo.what(to, typeTo) == 0 && csTo.count(to, typeTo) == 0)
				return this;

			final int what = csTo.what(to, typeTo);
			final int countTo = csTo.count(to, typeTo);
			int currentStateTo = Constants.UNDEFINED;
			int currentRotationTo = Constants.UNDEFINED;
			int currentValueTo = Constants.UNDEFINED;
			Component piece = null;

			// take the local state of the site from
			currentStateTo = (csTo.what(to, typeTo) == 0) ? Constants.UNDEFINED : csTo.state(to, typeTo);
			currentRotationTo = csTo.rotation(to, typeTo);
			currentValueTo = csTo.value(to, typeTo);
			
			if (countTo == 1)
			{
				csTo.remove(context.state(), to, typeTo);

				// to keep the site of the item in cache for each player
				if (what != 0)
				{
					piece = context.components()[what];
					//final int owner = piece.owner();
					//context.state().owned().remove(owner, what, to, typeTo);
				}

				// In case of LargePiece we update the empty chunkSet
				if (piece != null && piece.isLargePiece())
				{
					final Component largePiece = piece;
					final TIntArrayList locs = largePiece.locs(context, to, currentStateTo, context.topology());
					for (int i = 0; i < locs.size(); i++)
					{
						csTo.addToEmpty(locs.getQuick(i), SiteType.Cell);
						csTo.setCount(context.state(), locs.getQuick(i), 0);
						csTo.remove(context.state(), locs.getQuick(i), SiteType.Cell);
					}
					if (largePiece.isDomino() && context.containerId()[to] == 0)
					{
						for (int i = 0; i < 4; i++)
							csTo.setValueCell(context.state(), locs.getQuick(i), 0);

						for (int i = 4; i < 8; i++)
							csTo.setValueCell(context.state(), locs.getQuick(i), 0);
					}
				}
				
				// In case the to site was occupied by another piece we re-add it.
				if(previousWhatTo > 0)
				{
					if(csTo.what(to, typeTo) != previousWhatTo)
					{
						csTo.setSite(context.state(), to, previousWhoTo, previousWhatTo, 1, previousStateTo, previousRotationTo,
								(context.game().hasDominoes() ? 1 : previousValueTo), typeTo);

						Component pieceTo = null;

						// to keep the site of the item in cache for each player
						pieceTo = context.components()[previousWhatTo];
//						final int owner = pieceTo.owner();
//						context.state().owned().add(owner, previousWhatTo, to, typeTo);
						if (pieceTo.isDomino())
							context.state().remainingDominoes().remove(pieceTo.index());
						
						if(context.game().hiddenInformation())
						{
							for (int pid = 1; pid < context.players().size(); pid++)
							{
								csTo.setHidden(context.state(), pid, to, 0, typeTo, previousHiddenTo[pid]);
								csTo.setHiddenWhat(context.state(), pid, to, 0, typeTo, previousHiddenWhatTo[pid]);
								csTo.setHiddenWho(context.state(), pid, to, 0, typeTo, previousHiddenWhoTo[pid]);
								csTo.setHiddenCount(context.state(), pid, to, 0, typeTo, previousHiddenCountTo[pid]);
								csTo.setHiddenState(context.state(), pid, to, 0, typeTo, previousHiddenStateTo[pid]);
								csTo.setHiddenRotation(context.state(), pid, to, 0, typeTo, previousHiddenRotationTo[pid]);
								csTo.setHiddenValue(context.state(), pid, to, 0, typeTo, previousHiddenValueTo[pid]);
							}
						}
					}
				}
				else
				{
					if(previousStateTo > 0 || previousRotationTo > 0 || previousValueTo > 0)
						csTo.setSite(context.state(), to, Constants.UNDEFINED, Constants.UNDEFINED, 0, previousStateTo, previousRotationTo,
								(context.game().hasDominoes() ? 1 : previousValueTo), typeTo);
				}
			}
			else
			{
				if(game.hasDominoes()) // Special case for dominoes because the count is used for the value of each part of the domino too....
				{
					csTo.remove(context.state(), to, typeTo);
					
					// to keep the site of the item in cache for each player
					if (what != 0)
					{
						piece = context.components()[what];
//						final int owner = piece.owner();
//						context.state().owned().remove(owner, what, to, typeTo);
					}
					
					// In case of LargePiece we update the empty chunkSet
					if (piece != null && piece.isLargePiece())
					{
						final Component largePiece = piece;
						final TIntArrayList locs = largePiece.locs(context, to, currentStateTo, context.topology());
						for (int i = 0; i < locs.size(); i++)
						{
							csTo.addToEmpty(locs.getQuick(i), SiteType.Cell);
							csTo.setCount(context.state(), locs.getQuick(i), 0);
							csTo.remove(context.state(), locs.getQuick(i), SiteType.Cell);
						}
						if (largePiece.isDomino() && context.containerId()[to] == 0)
						{
							for (int i = 0; i < 4; i++)
								csTo.setValueCell(context.state(), locs.getQuick(i), 0);

							for (int i = 4; i < 8; i++)
								csTo.setValueCell(context.state(), locs.getQuick(i), 0);
						}
					}
				}
				else
				{
					csTo.setSite(context.state(), to, Constants.UNDEFINED, Constants.UNDEFINED, previousCountTo, Constants.UNDEFINED,
							Constants.UNDEFINED, (context.game().usesLineOfPlay() ? 1 : Constants.OFF), typeTo);
				}
			}

			// update the local state of the site From
			if (currentStateTo != Constants.UNDEFINED && previousStateFrom == Constants.UNDEFINED)
				csFrom.setSite(context.state(), from, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, currentStateTo,
						Constants.UNDEFINED, Constants.OFF, typeFrom);
			else if (previousStateFrom != Constants.UNDEFINED)
				csFrom.setSite(context.state(), from, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, previousStateFrom,
						Constants.UNDEFINED, Constants.OFF, typeFrom);

			// update the rotation state of the site From
			if (currentRotationTo != Constants.UNDEFINED && previousRotationFrom == Constants.UNDEFINED)
				csFrom.setSite(context.state(), from, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
						Constants.UNDEFINED, currentRotationTo, Constants.OFF, typeFrom);
			else if (previousRotationFrom != Constants.UNDEFINED)
				csFrom.setSite(context.state(), from, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
						Constants.UNDEFINED, previousRotationFrom, Constants.OFF, typeFrom);

			// update the piece value of the site From
			if (currentValueTo != Constants.UNDEFINED && previousValueFrom == Constants.UNDEFINED)
				csFrom.setSite(context.state(), from, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
						Constants.UNDEFINED, Constants.UNDEFINED, (context.game().usesLineOfPlay() ? 1 : currentValueTo), typeFrom);
			else if (previousValueFrom != Constants.UNDEFINED)
				csFrom.setSite(context.state(), from, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED,
						Constants.UNDEFINED, Constants.UNDEFINED, (context.game().usesLineOfPlay() ? 1 : previousValueFrom), typeFrom);
			
			// ERIC: NEED TO UPDATE THE STATE/ROTATION/VALUE OF THE TO SITE IF THEY CHANGED DURING THE APPLY METHOD.
			
			final int who = (what < 1) ? 0 : context.components()[what].owner();

//			if (csFrom.what(from, typeFrom) != 0 && (!context.game().requiresCount()
//					|| context.game().requiresCount() && csFrom.what(from, typeFrom) != what))
//			{
//				final Component pieceFromRemove = context.components()[csFrom.what(from, typeFrom)];
//				final int owner = pieceFromRemove.owner();
//				context.state().owned().remove(owner, csFrom.what(from, typeFrom), from, typeFrom);
//			}

			if (csFrom.what(from, typeFrom) == what && csFrom.count(from, typeFrom) > 0)
			{
				csFrom.setSite(context.state(), from, Constants.UNDEFINED, Constants.UNDEFINED,
						(context.game().requiresCount() ? csFrom.count(from, typeFrom) + 1 : 1),
						Constants.UNDEFINED, Constants.UNDEFINED, (context.game().usesLineOfPlay() ? 1 : Constants.OFF), typeFrom);
			}
			else
			{
				csFrom.setSite(context.state(), from, who, what, 1, Constants.UNDEFINED, Constants.UNDEFINED,
						(context.game().usesLineOfPlay() ? 1 : Constants.OFF), typeFrom);
			}

			// to keep the site of the item in cache for each player
			if (what != 0)
			{
				piece = context.components()[what];
//				final int owner = piece.owner();
//				if(csFrom.count(from, typeFrom) == 1)
//					context.state().owned().add(owner, what, from, typeFrom);
				
//				final Owned ownedUndo = context.state().owned();
//				final TIntArrayList ownedSites = ownedUndo.sites(owner); 
//				int ownedToCount = 0;
//				for(int i = 0; i < ownedSites.size() ; i++)
//					if(ownedSites.get(i) == to)
//						ownedToCount++;

//				while(ownedToCount > csTo.count(to, typeTo))
//				{
//					context.state().owned().remove(owner, what, to, typeTo);
//					ownedToCount--;
//				}
			}

			// In case of LargePiece we update the empty chunkSet
			if (piece != null && piece.isLargePiece())
			{
				final Component largePiece = piece;
				final TIntArrayList locs = largePiece.locs(context, from, previousStateFrom, context.topology());
				for (int i = 0; i < locs.size(); i++)
				{
					csFrom.removeFromEmpty(locs.getQuick(i), SiteType.Cell);
					csFrom.setCount(context.state(), locs.getQuick(i),
							(context.game().usesLineOfPlay() ? piece.index() : 1));
				}

				if (context.game().usesLineOfPlay() && context.containerId()[to] == 0)
				{
					for (int i = 0; i < 4; i++)
						csTo.setValueCell(context.state(), locs.getQuick(i), 0);

					for (int i = 4; i < 8; i++)
						csTo.setValueCell(context.state(), locs.getQuick(i), 0);

					// We update the line of play for dominoes
					for (int i = 0; i < context.containers()[0].numSites(); i++)
						csTo.setPlayable(context.state(), i, false);

					for (int i = 0; i < context.containers()[0].numSites(); i++)
					{
						if (csTo.what(i, typeTo) != 0)
						{
							final Component currentComponent = context.components()[csTo.what(i, typeTo)];
							final int currentState = csTo.state(i, typeTo);
							final TIntArrayList locsToUpdate = largePiece.locs(context, i, currentState,
									context.topology());

							lineOfPlayDominoes(context, locsToUpdate.getQuick(0), locsToUpdate.getQuick(1),
									getDirnDomino(0, currentState), false, true);
							lineOfPlayDominoes(context, locsToUpdate.getQuick(7), locsToUpdate.getQuick(6),
									getDirnDomino(2, currentState), false, false);

							if (currentComponent.isDoubleDomino())
							{
								lineOfPlayDominoes(context, locsToUpdate.getQuick(2), locsToUpdate.getQuick(5),
										getDirnDomino(1, currentState), true, true);
								lineOfPlayDominoes(context, locsToUpdate.getQuick(3), locsToUpdate.getQuick(4),
										getDirnDomino(3, currentState), true, true);
							}
						}
					}
				}
			}

			// We keep the update for hidden info.
			if (context.game().hiddenInformation())
			{
				for (int pid = 1; pid < context.players().size(); pid++)
				{
					csFrom.setHidden(context.state(), pid, from, 0, typeFrom, csTo.isHidden(pid, to, 0, typeTo));
					csFrom.setHiddenWhat(context.state(), pid, from, 0, typeFrom, csTo.isHiddenWhat(pid, to, 0, typeTo));
					csFrom.setHiddenWho(context.state(), pid, from, 0, typeFrom, csTo.isHiddenWho(pid, to, 0, typeTo));
					csFrom.setHiddenCount(context.state(), pid, from, 0, typeFrom, csTo.isHiddenCount(pid, to, 0, typeTo));
					csFrom.setHiddenRotation(context.state(), pid, from, 0, typeFrom, csTo.isHiddenRotation(pid, to, 0, typeTo));
					csFrom.setHiddenState(context.state(), pid, from, 0, typeFrom, csTo.isHiddenState(pid, to, 0, typeTo));
					csFrom.setHiddenValue(context.state(), pid, from, 0, typeFrom, csTo.isHiddenValue(pid, to, 0, typeTo));
					if (csTo.what(to, typeTo) == 0)
					{
						csTo.setHidden(context.state(), pid, to, 0, typeTo, false);
						csTo.setHiddenWhat(context.state(), pid, to, 0, typeTo, false);
						csTo.setHiddenWho(context.state(), pid, to, 0, typeTo, false);
						csTo.setHiddenCount(context.state(), pid, to, 0, typeTo, false);
						csTo.setHiddenRotation(context.state(), pid, to, 0, typeTo, false);
						csTo.setHiddenValue(context.state(), pid, to, 0, typeTo, false);
						csTo.setHiddenState(context.state(), pid, to, 0, typeTo, false);
					}
				}
			}

			if (csFrom.isEmpty(from, typeFrom))
			{
				throw new RuntimeException("Undo: Did not expect locationFrom to be empty at site locnFrom="+from+"(who, what,count,state)=("
						+ csFrom.who(from, typeFrom) + "," + csFrom.what(from, typeFrom) + "," + csFrom.count(from, typeFrom)
								+ ","
								+ csFrom.state(from, typeFrom) + "," + csFrom.state(from, typeFrom) + ")");
			}
			
			if(context.game().hiddenInformation())
			{
				for (int pid = 1; pid < context.players().size(); pid++)
				{
					csFrom.setHidden(context.state(), pid, from, 0, typeFrom, previousHiddenFrom[pid]);
					csFrom.setHiddenWhat(context.state(), pid, from, 0, typeFrom, previousHiddenWhatFrom[pid]);
					csFrom.setHiddenWho(context.state(), pid, from, 0, typeFrom, previousHiddenWhoFrom[pid]);
					csFrom.setHiddenCount(context.state(), pid, from, 0, typeFrom, previousHiddenCountFrom[pid]);
					csFrom.setHiddenState(context.state(), pid, from, 0, typeFrom, previousHiddenStateFrom[pid]);
					csFrom.setHiddenRotation(context.state(), pid, from, 0, typeFrom, previousHiddenRotationFrom[pid]);
					csFrom.setHiddenValue(context.state(), pid, from, 0, typeFrom, previousHiddenValueFrom[pid]);
				}
			}
			
		}
		// On a stacking game
		else
		{
			if(from == to)
				return this;
			
			final ContainerState containerTo = context.state().containerStates()[contIdTo];
			final ContainerState containerFrom = context.state().containerStates()[contIdFrom];
			
			final int stackSize = containerTo.sizeStack(to, typeTo);

			// To move a complete stack
			if(onStacking)
			{
				//final int sizeStackTo = containerTo.sizeStack(to, typeTo);
				for (int slevel = Math.abs(previousSizeStackFrom - stackSize) ; slevel < stackSize ; slevel++)
				{
					if (levelFrom == Constants.UNDEFINED)
						containerFrom.addItemGeneric(context.state(), from, containerTo.what(to, slevel, typeTo),
								containerTo.who(to, slevel, typeTo), containerTo.state(to, slevel, typeTo),
								containerTo.rotation(to, slevel, typeTo), containerTo.value(to, slevel, typeTo), context.game(), typeFrom);
					else
					{
						containerFrom.insert(context.state(), typeFrom, from, levelFrom, containerTo.what(to, slevel, typeTo),
								containerTo.who(to, slevel, typeTo), previousStateFrom, previousRotationFrom, previousValueFrom, context.game());
					}
				}

				// We update owned for loc To.
//				for (int level = 0; level < containerTo.sizeStack(to, typeTo); level++)
//				{
//					final int whatTo = containerTo.what(to, level, typeTo);
//					if (whatTo != 0)
//					{
//						final Component pieceTo = context.components()[whatTo];
//						final int ownerTo = pieceTo.owner();
//						if (ownerTo != 0)
//							context.state().owned().remove(ownerTo, whatTo, to, typeTo);
//					}
//				}
				
				for (int slevel = Math.abs(previousSizeStackFrom - stackSize) ; slevel < stackSize ; slevel++)
					containerTo.remove(context.state(), to, typeTo);
				
				if(containerTo.sizeStack(to, typeTo) == 0)
					containerTo.addToEmpty(to, typeTo);

				containerFrom.removeFromEmpty(from, typeFrom);

				// We update owned for loc From.
//				for (int level = containerFrom.sizeStack(from, typeFrom) - sizeStackTo; level < containerFrom.sizeStack(from, typeTo); level++)
//				{
//					if (level < 0)
//						continue;
//					final int whatFrom = containerFrom.what(from, level, typeFrom);
//					if (whatFrom != 0)
//					{
//						final Component pieceFrom = context.components()[whatFrom];
//						final int ownerFrom = pieceFrom.owner();
//						if (ownerFrom != 0)
//							context.state().owned().add(ownerFrom, whatFrom, from, level, typeFrom);
//					}
//				}
			}
			// To move only the top piece
			else if (levelTo == Constants.UNDEFINED)
			{
				final int what = containerTo.what(to, typeTo);

				containerTo.remove(context.state(), to, typeTo);
				
				if (containerTo.sizeStack(to, typeTo) == 0)
					containerTo.addToEmpty(to, typeTo);

				final int who = (what < 1) ? 0 : context.components()[what].owner();
				final int newStateFrom = (previousStateFrom == Constants.UNDEFINED) ? containerTo.state(to, levelTo, typeTo) : previousStateFrom;
				final int newRotationFrom = (previousRotationFrom == Constants.UNDEFINED) ? containerTo.rotation(to, levelTo, typeTo) : previousRotationFrom;
				final int newValueFrom = (previousValueFrom == Constants.UNDEFINED) ? containerTo.value(to, levelTo, typeTo) : previousValueFrom;

				if (levelFrom == Constants.UNDEFINED)
				{
					containerFrom.addItemGeneric(context.state(), from, what, who, newStateFrom, newRotationFrom, newValueFrom, context.game(), typeFrom);
					
					if (containerFrom.sizeStack(from, typeFrom) != 0)
						containerFrom.removeFromEmpty(from, typeFrom);
					
					// To keep the site of the item in cache for each player
//					Component pieceTo = null;
//					int ownerTo = 0;
//					if (what != 0)
//					{
//						pieceTo = context.components()[what];
//						ownerTo = pieceTo.owner();
//						final int sizeStack = containerFrom.sizeStack(from, typeFrom);
//							context.state().owned().add(ownerTo, what, from,
//									sizeStack - 1, typeFrom);
//						context.state().owned().remove(ownerTo, what, to,
//								containerTo.sizeStack(to, typeTo), typeTo);
//					}
				}
				else
				{
					// To keep the site of the item in cache for each player
//					Component pieceTo = null;
//					int ownerTo = 0;
					
					if (what != 0)
					{
						//pieceTo = context.components()[what];
						//ownerTo = pieceTo.owner();

						//final int sizeStack = containerFrom.sizeStack(from, typeFrom);
						// we update the own list of the pieces on the top of that piece inserted.
//						for (int i = sizeStack - 1; i >= levelFrom; i--)
//						{
//							final int owner = containerFrom.who(from, i, typeFrom);
//							final int piece = containerFrom.what(from, i, typeFrom);
//							context.state().owned().removeNoUpdate(owner, piece, from, i, typeFrom);
//							context.state().owned().add(owner, piece, from, i + 1, typeFrom);
//						}
						
						containerFrom.insert(context.state(), typeFrom, from, levelFrom, what, who, previousStateFrom, previousRotationFrom, previousValueFrom, context.game());
						
//						context.state().owned().add(ownerTo, what, from, levelFrom, typeFrom);
//						
//						context.state().owned().remove(ownerTo, what, to, containerTo.sizeStack(to, typeTo), typeTo);
					}
				}
				
			} // to move only a level of the stack.
			else
			{
				final int what = containerTo.what(to, levelTo, typeTo);
				final int newStateFrom = (previousStateFrom == Constants.UNDEFINED) ? containerTo.state(to, levelTo, typeTo) : previousStateFrom;
				final int newRotationFrom = (previousRotationFrom == Constants.UNDEFINED) ? containerTo.rotation(to, levelTo, typeTo) : previousRotationFrom;
				final int newValueFrom = (previousValueFrom == Constants.UNDEFINED) ? containerTo.value(to, levelTo, typeTo) : previousValueFrom;
				
				containerTo.remove(context.state(), to, levelTo, typeTo);

				if (containerTo.sizeStack(to, typeTo) == 0)
					containerTo.addToEmpty(to, typeTo);

				final int who = (what < 1) ? 0 : context.components()[what].owner();

				if (levelFrom == Constants.UNDEFINED)
				{
					containerFrom.addItemGeneric(context.state(), from, what, who, newStateFrom, newRotationFrom, newValueFrom, context.game(), typeFrom);

					if (containerFrom.sizeStack(from, typeFrom) != 0)
						containerFrom.removeFromEmpty(from, typeFrom);

					// to keep the site of the item in cache for each player
//					Component pieceTo = null;
//					int ownerTo = 0;
//					if (what != 0)
//					{
//						pieceTo = context.components()[what];
//						ownerTo = pieceTo.owner();
//							context.state().owned().add(ownerTo, what, from,
//									containerFrom.sizeStack(from, typeFrom) - 1, typeFrom);
//							context.state().owned().remove(ownerTo, what, to, levelTo, typeTo);
//					}
				}
				else
				{
					//final int sizeStack = containerFrom.sizeStack(from, typeFrom);
					
					// we update the own list of the pieces on the top of that piece inserted.
//					for (int i = sizeStack - 1; i >= levelFrom; i--)
//					{
//						final int owner = containerFrom.who(from, i, typeFrom);
//						final int piece = containerFrom.what(from, i, typeFrom);
//						context.state().owned().remove(owner, piece, from, i, typeFrom);
//						context.state().owned().add(owner, piece, from, i + 1, typeFrom);
//					}
					
					// We insert the piece.
					containerFrom.insertCell(context.state(), from, levelFrom, what, who, state, rotation, value, context.game());
				
					// we update the own list with the new piece
//					final Component piece = context.components()[what];
//					final int owner = piece.owner();
//					context.state().owned().add(owner, what, from, levelFrom, typeFrom);

					if (containerFrom.sizeStack(from, typeFrom) != 0)
						containerFrom.removeFromEmpty(from, typeFrom);

					// to keep the site of the item in cache for each player
//					Component pieceTo = null;
//					int ownerTo = 0;
//					if (what != 0)
//					{
//						pieceTo = context.components()[what];
//						ownerTo = pieceTo.owner();
//						if (ownerTo != 0)
//							context.state().owned().remove(ownerTo, what, to, levelTo, typeTo);
//					}
				}
			}
		}
		
		return this;
	}

	//-------------------------------------------------------------------------
	@Override
	public String toTrialFormat(final Context context)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("[Move:");

		if (typeFrom != null || (context != null && typeFrom != context.board().defaultSite()))
		{
			sb.append("typeFrom=" + typeFrom);
			sb.append(",from=" + from);
		}
		else
			sb.append("from=" + from);

		if (levelFrom != Constants.UNDEFINED)
			sb.append(",levelFrom=" + levelFrom);

		if (typeTo != null || (context != null && typeTo != context.board().defaultSite()))
			sb.append(",typeTo=" + typeTo);

		sb.append(",to=" + to);

		if (levelTo != Constants.UNDEFINED)
			sb.append(",levelTo=" + levelTo);

		if (state != Constants.UNDEFINED)
			sb.append(",state=" + state);

		if (rotation != Constants.UNDEFINED)
			sb.append(",rotation=" + rotation);

		if (value != Constants.UNDEFINED)
			sb.append(",value=" + value);

		if (onStacking)
			sb.append(",stack=" + onStacking);

		if (decision)
			sb.append(",decision=" + decision);

		sb.append(']');

		return sb.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (decision ? 1231 : 1237);
		result = prime * result + from;
		result = prime * result + levelFrom;
		result = prime * result + to;
		result = prime * result + levelTo;
		result = prime * result + state;
		result = prime * result + rotation;
		result = prime * result + value;
		result = prime * result + (onStacking ? 1231 : 1237);
		result = prime * result + ((typeFrom == null) ? 0 : typeFrom.hashCode());
		result = prime * result + ((typeTo == null) ? 0 : typeTo.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionMove))
			return false;

		final ActionMove other = (ActionMove) obj;

		return (decision == other.decision &&
				from == other.from &&
				levelFrom == other.levelFrom &&
				to == other.to &&
				levelTo == other.levelTo &&
				state == other.state &&
				rotation == other.rotation &&
				value == other.value &&
				onStacking == other.onStacking && typeFrom == other.typeFrom && typeTo == other.typeTo);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String getDescription() 
	{
		return "Move";
	}

	@Override
	public String toTurnFormat(final Context context, final boolean useCoords)
	{
		final StringBuilder sb = new StringBuilder();

		String newFrom = from + "";
		if (useCoords)
		{
			final int cid = (typeFrom == SiteType.Cell
					|| typeFrom == null && context.board().defaultSite() == SiteType.Cell) ? context.containerId()[from]
							: 0;
			if (cid == 0)
			{
				final SiteType realType = (typeFrom != null) ? typeFrom : context.board().defaultSite();
				newFrom = context.game().equipment().containers()[cid].topology().getGraphElements(realType).get(from)
						.label();
			}
		}

		if (typeFrom != null && !typeFrom.equals(context.board().defaultSite()))
			sb.append(typeFrom + " " + newFrom);
		else
			sb.append(newFrom);

		if (levelFrom != Constants.UNDEFINED && context.game().isStacking())
			sb.append("/" + levelFrom);

		String newTo = to + "";
		if (useCoords)
		{
			final int cid = (typeTo == SiteType.Cell
					|| typeTo == null && context.board().defaultSite() == SiteType.Cell) ? context.containerId()[to]
							: 0;
			if (cid == 0)
			{
				final SiteType realType = (typeTo != null) ? typeTo : context.board().defaultSite();
				newTo = context.game().equipment().containers()[cid].topology().getGraphElements(realType).get(to)
						.label();
			}
		}

		if (typeTo != null && !typeTo.equals(context.board().defaultSite()))
			sb.append("-" + typeTo + " " + newTo);
		else
			sb.append("-" + newTo);

		if (levelTo != Constants.UNDEFINED)
			sb.append("/" + levelTo);

		if (state != Constants.UNDEFINED)
			sb.append("=" + state);

		if (rotation != Constants.UNDEFINED)
			sb.append(" r" + rotation);

		if (value != Constants.UNDEFINED)
			sb.append(" v" + value);

		if (onStacking)
			sb.append(" ^");

		return sb.toString();
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		final StringBuilder sb = new StringBuilder();

		sb.append("(Move ");

		String newFrom = from + "";
		if (useCoords)
		{
			final int cid = (typeFrom == SiteType.Cell
					|| typeFrom == null && context.board().defaultSite() == SiteType.Cell) ? context.containerId()[from]
							: 0;
			if (cid == 0)
			{
				final SiteType realType = (typeFrom != null) ? typeFrom : context.board().defaultSite();
				newFrom = context.game().equipment().containers()[cid].topology().getGraphElements(realType).get(from)
						.label();
			}
		}

		if (typeFrom != null && typeTo != null
				&& (!typeFrom.equals(context.board().defaultSite()) || !typeFrom.equals(typeTo)))
			sb.append(typeFrom + " " + newFrom);
		else
			sb.append(newFrom);

		if (levelFrom != Constants.UNDEFINED)
			sb.append("/" + levelFrom);

		String newTo = to + "";
		if (useCoords)
		{
			final int cid = (typeTo == SiteType.Cell
					|| typeTo == null && context.board().defaultSite() == SiteType.Cell) ? context.containerId()[to]
							: 0;
			if (cid == 0)
			{
				final SiteType realType = (typeTo != null) ? typeTo : context.board().defaultSite();
				if (to < context.game().equipment().containers()[cid].topology().getGraphElements(realType).size())
					newTo = context.game().equipment().containers()[cid].topology().getGraphElements(realType).get(to)
							.label();
				else // The site is not existing.
					newTo = "??";
			}
		}

		if (typeFrom != null && typeTo != null
				&& (!typeTo.equals(context.board().defaultSite()) || !typeFrom.equals(typeTo)))
			sb.append(" - " + typeTo + " " + newTo);
		else
			sb.append("-" + newTo);

		if (levelTo != Constants.UNDEFINED)
			sb.append("/" + levelTo);

		if (state != Constants.UNDEFINED)
			sb.append(" state=" + state);

		if (rotation != Constants.UNDEFINED)
			sb.append(" rotation=" + rotation);

		if (state != Constants.UNDEFINED)
			sb.append(" state=" + state);

		if (onStacking)
			sb.append(" stack=" + onStacking);

		sb.append(')');

		return sb.toString();
	}
		
	//-------------------------------------------------------------------------

	@Override
	public SiteType fromType()
	{
		return typeFrom;
	}

	@Override
	public SiteType toType()
	{
		return typeTo;
	}

	@Override
	public int from()
	{
		return from;
	}

	@Override
	public int to()
	{
		return to;
	}

	@Override
	public int levelFrom()
	{
		return (levelFrom == Constants.UNDEFINED) ? Constants.GROUND_LEVEL : levelFrom;
	}

	@Override
	public int levelTo()
	{
		return (levelTo == Constants.UNDEFINED) ? Constants.GROUND_LEVEL : levelTo;
	}

	@Override
	public int state()
	{
		return state;
	}

	@Override
	public int rotation()
	{
		return rotation;
	}

	@Override
	public int value()
	{
		return value;
	}

	@Override
	public int count()
	{
		return 1;
	}

	@Override
	public boolean isStacking()
	{
		return onStacking;
	}

	@Override
	public void setLevelFrom(final int levelA)
	{
		levelFrom = levelA;
	}
	
	@Override
	public ActionType actionType()
	{
		return ActionType.Move;
	}

	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		final BitSet ludemeConcept = (movesLudeme != null) ? movesLudeme.concepts(context.game()) : new BitSet();

		final int contIdA = typeFrom.equals(SiteType.Cell) ? context.containerId()[from] : 0;
		final int contIdB = typeTo.equals(SiteType.Cell) ? context.containerId()[to] : 0;

		final ContainerState csA = context.state().containerStates()[contIdA];
		final ContainerState csB = context.state().containerStates()[contIdB];

		final int whatA = csA.what(from, typeFrom);
		final int whatB = csB.what(to, typeTo);

		final int whoA = csA.who(from, typeFrom);
		final int whoB = csB.who(to, typeTo);

		final BitSet concepts = new BitSet();

		// ---- Hop concepts

		if (ludemeConcept.get(Concept.HopDecision.id()))
			{
			concepts.set(Concept.HopDecision.id(), true);

			if (whatA != 0)
			{
				final Topology topology = context.topology();
				final TopologyElement fromV = topology.getGraphElements(typeFrom).get(from);

				final List<DirectionFacing> directionsSupported = topology.supportedDirections(RelationType.All,
						typeFrom);
				AbsoluteDirection direction = null;
				int distance = Constants.UNDEFINED;

				for (final DirectionFacing facingDirection : directionsSupported)
				{
					final AbsoluteDirection absDirection = facingDirection.toAbsolute();
					final List<Radial> radials = topology.trajectories().radials(typeFrom, fromV.index(), absDirection);

					for (final Radial radial : radials)
					{
						for (int toIdx = 1; toIdx < radial.steps().length; toIdx++)
						{
							final int toRadial = radial.steps()[toIdx].id();
							if (toRadial == to)
							{
								direction = absDirection;
								distance = toIdx;
								break;
							}
						}
						if (direction != null)
							break;
					}
				}

				if (direction != null)
				{
					final List<Radial> radials = topology.trajectories().radials(typeFrom, fromV.index(), direction);

					for (final Radial radial : radials)
					{
						for (int toIdx = 1; toIdx < distance; toIdx++)
						{
							final int between = radial.steps()[toIdx].id();
							final int whatBetween = csA.what(between, typeFrom);
							final int whoBetween = csA.who(between, typeFrom);
							if (whatBetween != 0)
							{
								if (areEnemies(context, whoA, whoBetween))
								{
									if (whatB == 0)
										concepts.set(Concept.HopDecisionEnemyToEmpty.id(), true);
									else
									{
										if (areEnemies(context, whoA, whoB))
											concepts.set(Concept.HopDecisionEnemyToEnemy.id(), true);
										else
											concepts.set(Concept.HopDecisionEnemyToFriend.id(), true);
									}
								}
								else
								{
									if (whatB == 0)
										concepts.set(Concept.HopDecisionFriendToEmpty.id(), true);
									else
									{
										if (areEnemies(context, whoA, whoB))
											concepts.set(Concept.HopDecisionFriendToEnemy.id(), true);
										else
											concepts.set(Concept.HopDecisionFriendToFriend.id(), true);
									}
								}
							}
						}
					}
				}
			}
		}

		if (ludemeConcept.get(Concept.HopEffect.id()))
			concepts.set(Concept.HopEffect.id(), true);

		// ---- Step concepts

		if (ludemeConcept.get(Concept.StepEffect.id()))
			concepts.set(Concept.StepEffect.id(), true);

		if (ludemeConcept.get(Concept.StepDecision.id()))
		{
			concepts.set(Concept.StepDecision.id(), true);
			if (whatA != 0)
			{
				if (whatB == 0)
					concepts.set(Concept.StepDecisionToEmpty.id(), true);
				else
				{
					if (areEnemies(context, whoA, whoB))
						concepts.set(Concept.StepDecisionToEnemy.id(), true);
					else
						concepts.set(Concept.StepDecisionToFriend.id(), true);
				}
			}
		}

		// ---- Leap concepts

		if (ludemeConcept.get(Concept.LeapEffect.id()))
			concepts.set(Concept.LeapEffect.id(), true);

		if (ludemeConcept.get(Concept.LeapDecision.id()))
		{
			concepts.set(Concept.LeapDecision.id(), true);
			if (whatA != 0)
			{
				if (whatB == 0)
					concepts.set(Concept.LeapDecisionToEmpty.id(), true);
				else
				{
					if (areEnemies(context, whoA, whoB))
						concepts.set(Concept.LeapDecisionToEnemy.id(), true);
					else
						concepts.set(Concept.LeapDecisionToFriend.id(), true);
				}
			}
		}

		// ---- Slide concepts

		if (ludemeConcept.get(Concept.SlideEffect.id()))
			concepts.set(Concept.SlideEffect.id(), true);

		if (ludemeConcept.get(Concept.SlideDecision.id()))
		{
			concepts.set(Concept.SlideDecision.id(), true);
			if (whatA != 0)
			{
				if (whatB == 0)
					concepts.set(Concept.SlideDecisionToEmpty.id(), true);
				else
				{
					if (areEnemies(context, whoA, whoB))
						concepts.set(Concept.SlideDecisionToEnemy.id(), true);
					else
						concepts.set(Concept.SlideDecisionToFriend.id(), true);
				}
			}
		}

		// ---- FromTo concepts

		if (ludemeConcept.get(Concept.FromToDecision.id()))
		{
			if (contIdA == contIdB)
				concepts.set(Concept.FromToDecisionWithinBoard.id(), true);
			else
				concepts.set(Concept.FromToDecisionBetweenContainers.id(), true);

			if (whatA != 0)
			{
				if (whatB == 0)
					concepts.set(Concept.FromToDecisionEmpty.id(), true);
				else
				{
					if (areEnemies(context, whoA, whoB))
						concepts.set(Concept.FromToDecisionEnemy.id(), true);
					else
						concepts.set(Concept.FromToDecisionFriend.id(), true);
				}
			}
		}

		if (ludemeConcept.get(Concept.FromToEffect.id()))
			concepts.set(Concept.FromToEffect.id(), true);
		
		// ---- Swap Pieces concepts

		if (ludemeConcept.get(Concept.SwapPiecesEffect.id()))
			concepts.set(Concept.SwapPiecesEffect.id(), true);

		if (ludemeConcept.get(Concept.SwapPiecesDecision.id()))
			concepts.set(Concept.SwapPiecesDecision.id(), true);


		// ---- Sow capture concepts

		if (ludemeConcept.get(Concept.SowCapture.id()))
			concepts.set(Concept.SowCapture.id(), true);

		return concepts;
	}

	/**
	 * @param context The context.
	 * @param who1    The id of a player.
	 * @param who2    The id of a player.
	 * @return True if these players are enemies.
	 */
	public static boolean areEnemies(final Context context, final int who1, final int who2)
	{
		if (who1 == 0 || who2 == 0 || who1 == who2)
			return false;

		if (context.game().requiresTeams())
		{
			final TIntArrayList teamMembers = new TIntArrayList();
			final int tid = context.state().getTeam(who1);
			for (int i = 1; i < context.game().players().size(); i++)
				if (context.state().getTeam(i) == tid)
					teamMembers.add(i);
			return !teamMembers.contains(who2);
		}

		return who1 != who2;
	}
}
