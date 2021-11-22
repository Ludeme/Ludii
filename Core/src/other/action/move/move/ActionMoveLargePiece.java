package other.action.move.move;

import game.Game;
import game.equipment.component.Component;
import game.types.board.SiteType;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.action.Action;
import other.context.Context;
import other.state.container.ContainerState;
import other.state.track.OnTrackIndices;

/**
 * Moves a piece from a site to another (only the top piece).
 *
 * @author Eric.Piette
 */
public final class ActionMoveLargePiece extends ActionMoveTopPiece
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The graph element type of the from site. */
	private final SiteType typeFrom;

	/** From site index. */
	private final int from;

	/** The graph element type of the to site. */
	private final SiteType typeTo;

	/** To site index. */
	private final int to;

	/** Site state value of the to site. */
	private final int state;

	/** Rotation value of the to site. */
	private final int rotation;

	/** piece value of the to site. */
	private final int value;

	//----------------------Undo Data---------------------------------------------

	/** A variable to know that we already applied this action so we do not want to modify the data to undo if apply again. */
	private boolean alreadyApplied = false;

	//-- from data
	
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
	 * @param typeTo     The graph element type of the to site.
	 * @param to         To site index.
	 * @param state      The state site of the to site.
	 * @param rotation   The rotation value of the to site.
	 * @param value      The piece value of the to site.
	 */
	public ActionMoveLargePiece
	(
		final SiteType typeFrom,
		final int from,
		final SiteType typeTo,
		final int to,
		final int state,
		final int rotation,
		final int value
	)
	{
		super(typeFrom, from, typeTo, to, state, rotation, value);
		this.typeFrom = typeFrom;
		this.from = from;
		this.typeTo = typeTo;
		this.to = to;
		this.state = state;
		this.rotation = rotation;
		this.value = value;
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
		currentStateFrom = (csFrom.what(from, typeFrom) == 0) ? Constants.UNDEFINED : csFrom.state(from, typeFrom);
		currentRotationFrom = csFrom.rotation(from, typeFrom);
		currentValueFrom =  csFrom.value(from, typeFrom);
		
		// Keep in memory the data of the site from and to (for undo method)
		if(!alreadyApplied)
		{
			previousStateFrom = currentStateFrom;
			previousRotationFrom = currentRotationFrom;
			previousValueFrom = currentValueFrom;
			previousStateTo = csTo.state(to, typeTo);
			previousRotationTo =csTo.rotation(to, typeTo); 
			previousValueTo = csTo.value(to, typeTo);
			previousWhoTo = csTo.who(to, typeTo);
			previousWhatTo = csTo.what(to, typeTo);
			previousCountFrom = csFrom.count(from, typeFrom);
			previousCountTo = csTo.count(to, typeTo);
			
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
				csTo.setSite(context.state(), to, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, currentStateFrom, Constants.UNDEFINED, Constants.OFF, typeTo);
			else if (state != Constants.UNDEFINED)
				csTo.setSite(context.state(), to, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, state, Constants.UNDEFINED, Constants.OFF, typeTo);

			// update the rotation state of the site To
			if (currentRotationFrom != Constants.UNDEFINED && rotation == Constants.UNDEFINED)
				csTo.setSite(context.state(), to, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, currentRotationFrom, Constants.OFF, typeTo);
			else if (rotation != Constants.UNDEFINED)
				csTo.setSite(context.state(), to, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, rotation, Constants.OFF, typeTo);

			// update the piece value of the site To
			if (currentValueFrom != Constants.UNDEFINED && value == Constants.UNDEFINED)
				csTo.setSite(context.state(), to, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, (context.game().usesLineOfPlay() ? 1 : currentValueFrom), typeTo);
			else if (value != Constants.UNDEFINED)
				csTo.setSite(context.state(), to, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, (context.game().usesLineOfPlay() ? 1 : value), typeTo);
			
			final int who = (what < 1) ? 0 : context.components()[what].owner();

			if (csTo.what(to, typeTo) != 0 && (!context.game().requiresCount() || context.game().requiresCount() && csTo.what(to, typeTo) != what))
			{
				final Component pieceToRemove = context.components()[csTo.what(to, typeTo)];
				final int owner = pieceToRemove.owner();
				context.state().owned().remove(owner, csTo.what(to, typeTo), to, typeTo);
			}

			final int valueToSet = (context.game().usesLineOfPlay() ? 1 : Constants.OFF);
			if (csTo.what(to, typeTo) == what && csTo.count(to, typeTo) > 0)
			{
				final int countToSet = (context.game().requiresCount() ? csTo.count(to, typeTo) + 1 : 1);
				csTo.setSite(context.state(), to, Constants.UNDEFINED, Constants.UNDEFINED, countToSet, Constants.UNDEFINED, Constants.UNDEFINED, valueToSet, typeTo);
			}
			else
			{
				csTo.setSite(context.state(), to, who, what, 1, Constants.UNDEFINED, Constants.UNDEFINED, valueToSet, typeTo);
			}

			// To keep the site of the item in cache for each player.
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
					csTo.setCount(context.state(), locs.getQuick(i), (context.game().usesLineOfPlay() ? piece.index() : 1));
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

							lineOfPlayDominoes(context, locsToUpdate.getQuick(0), locsToUpdate.getQuick(1), getDirnDomino(0, currentState), false, true);
							lineOfPlayDominoes(context, locsToUpdate.getQuick(7), locsToUpdate.getQuick(6), getDirnDomino(2, currentState), false, false);

							if (currentComponent.isDoubleDomino())
							{
								lineOfPlayDominoes(context, locsToUpdate.getQuick(2), locsToUpdate.getQuick(5), getDirnDomino(1, currentState), true, true);
								lineOfPlayDominoes(context, locsToUpdate.getQuick(3), locsToUpdate.getQuick(4), getDirnDomino(3, currentState), true, true);
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
						+ csTo.who(to, typeTo) + "," + csTo.what(to, typeTo) + "," + csTo.count(to, typeTo) + ","
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

			final int what = containerFrom.what(from, typeFrom);

			containerFrom.remove(context.state(), from, typeFrom);
				
			if (containerFrom.sizeStack(from, typeFrom) == 0)
				containerFrom.addToEmpty(from, typeFrom);

			final int who = (what < 1) ? 0 : context.components()[what].owner();

			if (!context.game().hasCard())
				containerTo.addItemGeneric(context.state(), to, what, who, context.game(), typeTo);

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
				context.state().owned().remove(ownerFrom, what, from, containerFrom.sizeStack(from, typeFrom), typeFrom);
			}

			// We update the structure about track indices if the game uses track.
			updateOnTrackIndices(what, onTrackIndices, context.board().tracks());
		}
		
		return this;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public Action undo(final Context context)
	{
		// In undo ActionMove from = to and to = from.
		
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
					piece = context.components()[what];

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
						piece = context.components()[what];
					
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
				piece = context.components()[what];

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
			
			final int what = containerTo.what(to, typeTo);

			containerTo.remove(context.state(), to, typeTo);
				
			if (containerTo.sizeStack(to, typeTo) == 0)
				containerTo.addToEmpty(to, typeTo);

			final int who = (what < 1) ? 0 : context.components()[what].owner();
			final int newStateFrom = (previousStateFrom == Constants.UNDEFINED) ? containerTo.state(to, typeTo) : previousStateFrom;
			final int newRotationFrom = (previousRotationFrom == Constants.UNDEFINED) ? containerTo.rotation(to, typeTo) : previousRotationFrom;
			final int newValueFrom = (previousValueFrom == Constants.UNDEFINED) ? containerTo.value(to, typeTo) : previousValueFrom;

			containerFrom.addItemGeneric(context.state(), from, what, who, newStateFrom, newRotationFrom, newValueFrom, context.game(), typeFrom);
					
			if (containerFrom.sizeStack(from, typeFrom) != 0)
				containerFrom.removeFromEmpty(from, typeFrom);
				
		} 
		
		return this;
	}
}
