package other.action.move.move;

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
import other.state.State;
import other.state.container.ContainerState;
import other.state.track.OnTrackIndices;
import other.topology.Topology;
import other.topology.TopologyElement;

/**
 * Moves a piece from a site to another (only the top piece).
 *
 * @author Eric.Piette
 */
public class ActionMoveTopPiece extends BaseAction
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

	/** Previous What value of the from site. */
	private int[] previousWhatFrom;
	
	/** Previous Who value of the from site. */
	private int[] previousWhoFrom;
	
	/** Previous Site state value of the from site. */
	private int[] previousStateFrom;

	/** Previous Rotation value of the from site. */
	private int[] previousRotationFrom;

	/** Previous Piece value of the from site. */
	private int[] previousValueFrom;

	/** Previous Piece count of the from site. */
	private int previousCountFrom;

	/** The previous hidden info values of the from site before to be removed. */
	private boolean[][] previousHiddenFrom;
	
	/** The previous hidden what info values of the from site before to be removed. */
	private boolean[][] previousHiddenWhatFrom;
	
	/** The previous hidden who info values of the from site before to be removed. */
	private boolean[][] previousHiddenWhoFrom;

	/** The previous hidden count info values of the from site before to be removed. */
	private boolean[][] previousHiddenCountFrom;

	/** The previous hidden rotation info values of the from site before to be removed. */
	private boolean[][] previousHiddenRotationFrom;

	/** The previous hidden State info values of the from site before to be removed. */
	private boolean[][] previousHiddenStateFrom;

	/** The previous hidden Value info values of the from site before to be removed. */
	private boolean[][] previousHiddenValueFrom;
	
	//--- to data
	
	/** Previous What of the to site. */
	private int[] previousWhatTo;
	
	/** Previous Who of the to site. */
	private int[] previousWhoTo;
	
	/** Previous Site state value of the to site. */
	private int[] previousStateTo;

	/** Previous Rotation value of the to site. */
	private int[] previousRotationTo;

	/** Previous Piece value of the to site. */
	private int[] previousValueTo;

	/** Previous Piece count of the to site. */
	private int previousCountTo;

	/** The previous hidden info values of the to site before to be removed. */
	private boolean[][] previousHiddenTo;
	
	/** The previous hidden what info values of the to site before to be removed. */
	private boolean[][] previousHiddenWhatTo;
	
	/** The previous hidden who info values of the to site before to be removed. */
	private boolean[][] previousHiddenWhoTo;

	/** The previous hidden count info values of the to site before to be removed. */
	private boolean[][] previousHiddenCountTo;

	/** The previous hidden rotation info values of the to site before to be removed. */
	private boolean[][] previousHiddenRotationTo;

	/** The previous hidden State info values of the to site before to be removed. */
	private boolean[][] previousHiddenStateTo;

	/** The previous hidden Value info values of the to site before to be removed. */
	private boolean[][] previousHiddenValueTo;
	
	//-------------------------------------------------------------------------
	
	private boolean actionLargePiece = false;
	
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
	public ActionMoveTopPiece
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

		final int what = csFrom.what(from, typeFrom);
		
		// We check if this a large piece to call the related class.
		if(what != 0)
		{
			piece = context.components()[what];
			if(piece.isLargePiece())
			{
				actionLargePiece = true;
				applyLargePiece(context, store);
				return this;
			}
		}
		
		// take the local state of the site from
		currentStateFrom = (csFrom.what(from, typeFrom) == 0) ? Constants.UNDEFINED : csFrom.state(from, typeFrom);
		currentRotationFrom = csFrom.rotation(from, typeFrom);
		currentValueFrom =  csFrom.value(from, typeFrom);
		
		// Keep in memory the data of the site from and to (for undo method)
		if(!alreadyApplied)
		{
			if(!requiresStack)
			{
				previousCountFrom = csFrom.count(from, typeFrom);
				previousWhatFrom = new int[1];
				previousWhoFrom = new int[1];
				previousStateFrom = new int[1];
				previousRotationFrom = new int[1];
				previousValueFrom = new int[1];
				previousWhatFrom[0] = csFrom.what(from, 0, typeFrom);
				previousWhoFrom[0] = csFrom.who(from, 0, typeFrom);
				previousStateFrom[0] = csFrom.state(from, 0, typeFrom);
				previousRotationFrom[0] = csFrom.rotation(from, 0, typeFrom);
				previousValueFrom[0] = csFrom.value(from, 0, typeFrom);

				previousCountTo = csTo.count(to, typeTo);
				previousWhatTo = new int[1];
				previousWhoTo = new int[1];
				previousStateTo = new int[1];
				previousRotationTo = new int[1];
				previousValueTo = new int[1];
				previousWhatTo[0] = csTo.what(to, 0, typeTo);
				previousWhoTo[0] = csTo.who(to, 0, typeTo);
				previousStateTo[0] = csTo.state(to, 0, typeTo);
				previousRotationTo[0] = csTo.rotation(to, 0, typeTo);
				previousValueTo[0] = csTo.value(to, 0, typeTo);
				
				if(context.game().hiddenInformation())
				{
					previousHiddenFrom = new boolean[1][context.players().size()];
					previousHiddenWhatFrom = new boolean[1][context.players().size()];
					previousHiddenWhoFrom = new boolean[1][context.players().size()];
					previousHiddenCountFrom = new boolean[1][context.players().size()];
					previousHiddenRotationFrom = new boolean[1][context.players().size()];
					previousHiddenStateFrom = new boolean[1][context.players().size()];
					previousHiddenValueFrom = new boolean[1][context.players().size()];
					for (int pid = 1; pid < context.players().size(); pid++)
					{
						previousHiddenFrom[0][pid] = csFrom.isHidden(pid, from, 0, typeFrom);
						previousHiddenWhatFrom[0][pid] = csFrom.isHiddenWhat(pid, from, 0, typeFrom);
						previousHiddenWhoFrom[0][pid] = csFrom.isHiddenWho(pid, from, 0, typeFrom);
						previousHiddenCountFrom[0][pid] = csFrom.isHiddenCount(pid, from, 0, typeFrom);
						previousHiddenStateFrom[0][pid] = csFrom.isHiddenState(pid, from, 0, typeFrom);
						previousHiddenRotationFrom[0][pid] = csFrom.isHiddenRotation(pid, from, 0, typeFrom);
						previousHiddenValueFrom[0][pid] = csFrom.isHiddenValue(pid, from, 0, typeFrom);
					}
					
					previousHiddenTo = new boolean[1][context.players().size()];
					previousHiddenWhatTo = new boolean[1][context.players().size()];
					previousHiddenWhoTo = new boolean[1][context.players().size()];
					previousHiddenCountTo = new boolean[1][context.players().size()];
					previousHiddenRotationTo = new boolean[1][context.players().size()];
					previousHiddenStateTo = new boolean[1][context.players().size()];
					previousHiddenValueTo = new boolean[1][context.players().size()];
					
					for (int pid = 1; pid < context.players().size(); pid++)
					{
						previousHiddenTo[0][pid] = csTo.isHidden(pid, to, 0, typeTo);
						previousHiddenWhatTo[0][pid] = csTo.isHiddenWhat(pid, to, 0, typeTo);
						previousHiddenWhoTo[0][pid] = csTo.isHiddenWho(pid, to, 0, typeTo);
						previousHiddenCountTo[0][pid] = csTo.isHiddenCount(pid, to, 0, typeTo);
						previousHiddenStateTo[0][pid] = csTo.isHiddenState(pid, to, 0, typeTo);
						previousHiddenRotationTo[0][pid] = csTo.isHiddenRotation(pid, to, 0, typeTo);
						previousHiddenValueTo[0][pid] = csTo.isHiddenValue(pid, to, 0, typeTo);
					}
				}
			}
			else // Stacking game.
			{
				final int sizeStackFrom = csFrom.sizeStack(from, typeFrom);

				previousWhatFrom = new int[sizeStackFrom];
				previousWhoFrom = new int[sizeStackFrom];
				previousStateFrom = new int[sizeStackFrom];
				previousRotationFrom = new int[sizeStackFrom];
				previousValueFrom = new int[sizeStackFrom];
				
				for(int lvl = 0 ; lvl < sizeStackFrom; lvl++)
				{
					previousWhatFrom[lvl] = csFrom.what(from, lvl, typeFrom);
					previousWhoFrom[lvl] = csFrom.who(from, lvl, typeFrom);
					previousStateFrom[lvl] = csFrom.state(from, lvl, typeFrom);
					previousRotationFrom[lvl] = csFrom.rotation(from, lvl, typeFrom);
					previousValueFrom[lvl] = csFrom.value(from, lvl, typeFrom);
					
					if(context.game().hiddenInformation())
					{
						previousHiddenFrom = new boolean[sizeStackFrom][context.players().size()];
						previousHiddenWhatFrom = new boolean[sizeStackFrom][context.players().size()];
						previousHiddenWhoFrom = new boolean[sizeStackFrom][context.players().size()];
						previousHiddenCountFrom = new boolean[sizeStackFrom][context.players().size()];
						previousHiddenRotationFrom = new boolean[sizeStackFrom][context.players().size()];
						previousHiddenStateFrom = new boolean[sizeStackFrom][context.players().size()];
						previousHiddenValueFrom = new boolean[sizeStackFrom][context.players().size()];
						for (int pid = 1; pid < context.players().size(); pid++)
						{
							previousHiddenFrom[lvl][pid] = csFrom.isHidden(pid, from, lvl, typeFrom);
							previousHiddenWhatFrom[lvl][pid] = csFrom.isHiddenWhat(pid, from, lvl, typeFrom);
							previousHiddenWhoFrom[lvl][pid] = csFrom.isHiddenWho(pid, from, lvl, typeFrom);
							previousHiddenCountFrom[lvl][pid] = csFrom.isHiddenCount(pid, from, lvl, typeFrom);
							previousHiddenStateFrom[lvl][pid] = csFrom.isHiddenState(pid, from, lvl, typeFrom);
							previousHiddenRotationFrom[lvl][pid] = csFrom.isHiddenRotation(pid, from, lvl, typeFrom);
							previousHiddenValueFrom[lvl][pid] = csFrom.isHiddenValue(pid, from, lvl, typeFrom);
						}
					}
				}
				
				final int sizeStackTo = csTo.sizeStack(to, typeTo);
				previousWhatTo = new int[sizeStackTo];
				previousWhoTo = new int[sizeStackTo];
				previousStateTo = new int[sizeStackTo];
				previousRotationTo = new int[sizeStackTo];
				previousValueTo = new int[sizeStackTo];
				for(int lvl = 0 ; lvl < sizeStackTo; lvl++)
				{
					previousWhatTo[lvl] = csTo.what(to, lvl, typeTo);
					previousWhoTo[lvl] = csTo.who(to, lvl, typeTo);
					previousStateTo[lvl] = csTo.state(to, lvl, typeTo);
					previousRotationTo[lvl] = csTo.rotation(to, lvl, typeTo);
					previousValueTo[lvl] = csTo.value(to, lvl, typeTo);
					
					if(context.game().hiddenInformation())
					{
						previousHiddenTo = new boolean[sizeStackTo][context.players().size()];
						previousHiddenWhatTo = new boolean[sizeStackTo][context.players().size()];
						previousHiddenWhoTo = new boolean[sizeStackTo][context.players().size()];
						previousHiddenCountTo = new boolean[sizeStackTo][context.players().size()];
						previousHiddenRotationTo = new boolean[sizeStackTo][context.players().size()];
						previousHiddenStateTo = new boolean[sizeStackTo][context.players().size()];
						previousHiddenValueTo = new boolean[sizeStackTo][context.players().size()];
						for (int pid = 1; pid < context.players().size(); pid++)
						{
							previousHiddenTo[lvl][pid] = csTo.isHidden(pid, to, lvl, typeTo);
							previousHiddenWhatTo[lvl][pid] = csTo.isHiddenWhat(pid, to, lvl, typeTo);
							previousHiddenWhoTo[lvl][pid] = csTo.isHiddenWho(pid, to, lvl, typeTo);
							previousHiddenCountTo[lvl][pid] = csTo.isHiddenCount(pid, to, lvl, typeTo);
							previousHiddenStateTo[lvl][pid] = csTo.isHiddenState(pid, to, lvl, typeTo);
							previousHiddenRotationTo[lvl][pid] = csTo.isHiddenRotation(pid, to, lvl, typeTo);
							previousHiddenValueTo[lvl][pid] = csTo.isHiddenValue(pid, to, lvl, typeTo);
						}
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
			}
			else
			{
				final int valueFrom = (context.game().usesLineOfPlay() ? 1 : Constants.OFF);
				csFrom.setSite(context.state(), from, Constants.UNDEFINED, Constants.UNDEFINED, count - 1, Constants.UNDEFINED, Constants.UNDEFINED, valueFrom, typeFrom);
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
			{
				final int valueFrom = (context.game().usesLineOfPlay() ? 1 : currentValueFrom);
				csTo.setSite(context.state(), to, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, valueFrom, typeTo);
			}
			else if (value != Constants.UNDEFINED)
			{
				final int valueFrom = (context.game().usesLineOfPlay() ? 1 : value);
				csTo.setSite(context.state(), to, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, valueFrom, typeTo);
			}
			
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
						+ csTo.who(to, typeTo) + "," + csTo.what(to, typeTo) + "," + csTo.count(to, typeTo) + "," + csTo.state(to, typeTo) + "," + csTo.state(to, typeTo) + ")");
			}
		}
		// on a stacking game
		else
		{
			if(from == to)
				return this;
			
			csFrom.remove(context.state(), from, typeFrom);
				
			if (csFrom.sizeStack(from, typeFrom) == 0)
				csFrom.addToEmpty(from, typeFrom);

			final int who = (what < 1) ? 0 : context.components()[what].owner();

			if (!context.game().hasCard())
				csTo.addItemGeneric(context.state(), to, what, who, context.game(), typeTo);

			if (csTo.sizeStack(to, typeTo) != 0)
				csTo.removeFromEmpty(to, typeTo);

			// to keep the site of the item in cache for each player
			Component pieceFrom = null;
			int ownerFrom = 0;
			if (what != 0)
			{
				pieceFrom = context.components()[what];
				ownerFrom = pieceFrom.owner();
				context.state().owned().add(ownerFrom, what, to, csTo.sizeStack(to, typeTo) - 1, typeTo);
				context.state().owned().remove(ownerFrom, what, from, csFrom.sizeStack(from, typeFrom), typeFrom);
			}

			// We update the structure about track indices if the game uses track.
			updateOnTrackIndices(what, onTrackIndices, context.board().tracks());
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
	public Action undo(final Context context, boolean discard)
	{
		// If a large piece was moved we call the right class.
		if(actionLargePiece)
		{
			undoLargePiece(context);
			return this;
		}
		
		final int contIdFrom = typeFrom.equals(SiteType.Cell) ? context.containerId()[from] : 0;
		final int contIdTo = typeTo.equals(SiteType.Cell) ? context.containerId()[to] : 0;
		final ContainerState csFrom = context.state().containerStates()[contIdFrom];
		final ContainerState csTo = context.state().containerStates()[contIdTo];
		final Game game = context.game();
		final State gameState = context.state();
		final boolean requiresStack = context.currentInstanceContext().game().isStacking();
		
		final int sizeStackFrom = csFrom.sizeStack(from, typeFrom);
		final int sizeStackTo = csTo.sizeStack(to, typeTo);
		
		if(requiresStack) // Stacking undo.
		{
			// We restore the from site
			for(int lvl = sizeStackFrom -1 ; lvl >= 0; lvl--)
				csFrom.remove(context.state(), from, lvl, typeFrom);
	
			for(int lvl = 0 ; lvl < previousWhatFrom.length; lvl++)
			{
				csFrom.addItemGeneric(gameState, from, previousWhatFrom[lvl], previousWhoFrom[lvl], previousStateFrom[lvl], previousRotationFrom[lvl], previousValueFrom[lvl], game, typeFrom);
				if(context.game().hiddenInformation())
				{
					for (int pid = 1; pid < context.players().size(); pid++)
					{
						csFrom.setHidden(gameState, pid, from, lvl, typeFrom, previousHiddenFrom[lvl][pid]);
						csFrom.setHiddenWhat(gameState, pid, from, lvl, typeFrom, previousHiddenWhatFrom[lvl][pid]);
						csFrom.setHiddenWho(gameState, pid, from, lvl, typeFrom, previousHiddenWhoFrom[lvl][pid]);
						csFrom.setHiddenCount(gameState, pid, from, lvl, typeFrom, previousHiddenCountFrom[lvl][pid]);
						csFrom.setHiddenState(gameState, pid, from, lvl, typeFrom, previousHiddenStateFrom[lvl][pid]);
						csFrom.setHiddenRotation(gameState, pid, from, lvl, typeFrom, previousHiddenRotationFrom[lvl][pid]);
						csFrom.setHiddenValue(gameState, pid, from, lvl, typeFrom, previousHiddenValueFrom[lvl][pid]);
					}
				}
			}
			
			// We restore the to site
			for(int lvl = sizeStackTo -1 ; lvl >= 0; lvl--)
				csTo.remove(context.state(), to, lvl, typeTo);
			
			for(int lvl = 0 ; lvl < previousWhatTo.length; lvl++)
			{
				csTo.addItemGeneric(gameState, to, previousWhatTo[lvl], previousWhoTo[lvl], previousStateTo[lvl], previousRotationTo[lvl], previousValueTo[lvl], game, typeTo);
				if(context.game().hiddenInformation())
				{
					for (int pid = 1; pid < context.players().size(); pid++)
					{
						csTo.setHidden(gameState, pid, to, lvl, typeTo, previousHiddenTo[lvl][pid]);
						csTo.setHiddenWhat(gameState, pid, to, lvl, typeTo, previousHiddenWhatTo[lvl][pid]);
						csTo.setHiddenWho(gameState, pid, to, lvl, typeTo, previousHiddenWhoTo[lvl][pid]);
						csTo.setHiddenCount(gameState, pid, to, lvl, typeTo, previousHiddenCountTo[lvl][pid]);
						csTo.setHiddenState(gameState, pid, to, lvl, typeTo, previousHiddenStateTo[lvl][pid]);
						csTo.setHiddenRotation(gameState, pid, to, lvl, typeTo, previousHiddenRotationTo[lvl][pid]);
						csTo.setHiddenValue(gameState, pid, to, lvl, typeTo, previousHiddenValueTo[lvl][pid]);
					}
				}
			}
		}
		else // Non stacking undo.
		{
			csFrom.remove(context.state(), from, typeFrom);
			csTo.remove(context.state(), to, typeTo);
			csFrom.setSite(context.state(), from, previousWhoFrom[0], previousWhatFrom[0], previousCountFrom, previousStateFrom[0], previousRotationFrom[0], previousValueFrom[0], typeFrom);
			csTo.setSite(context.state(), to, previousWhoTo[0], previousWhatTo[0], previousCountTo, previousStateTo[0], previousRotationTo[0], previousValueTo[0], typeTo);
			
			if(context.game().hiddenInformation())
			{
				if(previousHiddenFrom.length > 0)
					for (int pid = 1; pid < context.players().size(); pid++)
					{
						csFrom.setHidden(gameState, pid, from, 0, typeFrom, previousHiddenFrom[0][pid]);
						csFrom.setHiddenWhat(gameState, pid, from, 0, typeFrom, previousHiddenWhatFrom[0][pid]);
						csFrom.setHiddenWho(gameState, pid, from, 0, typeFrom, previousHiddenWhoFrom[0][pid]);
						csFrom.setHiddenCount(gameState, pid, from, 0, typeFrom, previousHiddenCountFrom[0][pid]);
						csFrom.setHiddenState(gameState, pid, from, 0, typeFrom, previousHiddenStateFrom[0][pid]);
						csFrom.setHiddenRotation(gameState, pid, from, 0, typeFrom, previousHiddenRotationFrom[0][pid]);
						csFrom.setHiddenValue(gameState, pid, from, 0, typeFrom, previousHiddenValueFrom[0][pid]);
					}

				if(previousHiddenTo.length > 0)
					for (int pid = 1; pid < context.players().size(); pid++)
					{
						csTo.setHidden(gameState, pid, to, 0, typeTo, previousHiddenTo[0][pid]);
						csTo.setHiddenWhat(gameState, pid, to, 0, typeTo, previousHiddenWhatTo[0][pid]);
						csTo.setHiddenWho(gameState, pid, to, 0, typeTo, previousHiddenWhoTo[0][pid]);
						csTo.setHiddenCount(gameState, pid, to, 0, typeTo, previousHiddenCountTo[0][pid]);
						csTo.setHiddenState(gameState, pid, to, 0, typeTo, previousHiddenStateTo[0][pid]);
						csTo.setHiddenRotation(gameState, pid, to, 0, typeTo, previousHiddenRotationTo[0][pid]);
						csTo.setHiddenValue(gameState, pid, to, 0, typeTo, previousHiddenValueTo[0][pid]);
					}
			}
		}
		
		if (csTo.sizeStack(to, typeTo) == 0)
			csTo.addToEmpty(to, typeTo);
		
		if (csFrom.sizeStack(from, typeFrom) != 0)
			csFrom.removeFromEmpty(from, typeFrom);
		
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

		if (typeTo != null || (context != null && typeTo != context.board().defaultSite()))
			sb.append(",typeTo=" + typeTo);

		sb.append(",to=" + to);

		if (state != Constants.UNDEFINED)
			sb.append(",state=" + state);

		if (rotation != Constants.UNDEFINED)
			sb.append(",rotation=" + rotation);

		if (value != Constants.UNDEFINED)
			sb.append(",value=" + value);

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
		result = prime * result + to;
		result = prime * result + state;
		result = prime * result + rotation;
		result = prime * result + value;
		result = prime * result + 1237;
		result = prime * result + ((typeFrom == null) ? 0 : typeFrom.hashCode());
		result = prime * result + ((typeTo == null) ? 0 : typeTo.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ActionMoveTopPiece))
			return false;

		final ActionMoveTopPiece other = (ActionMoveTopPiece) obj;

		return (decision == other.decision &&
				from == other.from &&
				to == other.to &&
				state == other.state &&
				rotation == other.rotation &&
				value == other.value &&
				typeFrom == other.typeFrom && typeTo == other.typeTo);
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

		if (state != Constants.UNDEFINED)
			sb.append("=" + state);

		if (rotation != Constants.UNDEFINED)
			sb.append(" r" + rotation);

		if (value != Constants.UNDEFINED)
			sb.append(" v" + value);

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

		if (state != Constants.UNDEFINED)
			sb.append(" state=" + state);

		if (rotation != Constants.UNDEFINED)
			sb.append(" rotation=" + rotation);

		if (state != Constants.UNDEFINED)
			sb.append(" state=" + state);

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
		return Constants.GROUND_LEVEL;
	}

	@Override
	public int levelTo()
	{
		return  Constants.GROUND_LEVEL;
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
		return false;
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

				final List<DirectionFacing> directionsSupported = topology.supportedDirections(RelationType.All, typeFrom);
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
									
									if(distance > 1)
									{
										concepts.set(Concept.HopDecisionMoreThanOne.id(), true);
										concepts.set(Concept.HopCaptureMoreThanOne.id(), true);
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
									if(distance > 1)
									{
										concepts.set(Concept.HopDecisionMoreThanOne.id(), true);
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


		// ---- Sow concepts

		if (ludemeConcept.get(Concept.SowCapture.id()))
			concepts.set(Concept.SowCapture.id(), true);

		if (ludemeConcept.get(Concept.Sow.id()))
			concepts.set(Concept.Sow.id(), true);

		if (ludemeConcept.get(Concept.SowRemove.id()))
			concepts.set(Concept.SowRemove.id(), true);

		if (ludemeConcept.get(Concept.SowBacktracking.id()))
			concepts.set(Concept.SowBacktracking.id(), true);

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
	
	
	//-------------------------------------Large Piece code--------------------------------------
	
	/**
	 * Apply method only for large piece.
	 * @param context The context.
	 * @param store To store or not in the list of legal moves.
	 * @return The action.
	 */
	public Action applyLargePiece
	(
		final Context context,
		final boolean store
	)
	{
		final OnTrackIndices onTrackIndices = context.state().onTrackIndices();
		final int contIdFrom = typeFrom.equals(SiteType.Cell) ? context.containerId()[from] : 0;
		final int contIdTo = typeTo.equals(SiteType.Cell) ? context.containerId()[to] : 0;
		
		int currentStateFrom = Constants.UNDEFINED;
		int currentRotationFrom = Constants.UNDEFINED;
		int currentValueFrom = Constants.UNDEFINED;
		Component piece = null;

		final ContainerState csFrom = context.state().containerStates()[contIdFrom];
		final ContainerState csTo = context.state().containerStates()[contIdTo];
		
		// Take the local state of the site from.
		currentStateFrom = (csFrom.what(from, typeFrom) == 0) ? Constants.UNDEFINED : csFrom.state(from, typeFrom);
		currentRotationFrom = csFrom.rotation(from, typeFrom);
		currentValueFrom =  csFrom.value(from, typeFrom);
		
		// Keep in memory the data of the site from and to (for undo method).
		if(!alreadyApplied)
		{
			previousStateFrom = new int[1];
			previousRotationFrom = new int[1];
			previousValueFrom = new int[1];
			previousStateTo = new int[1];
			previousRotationTo = new int[1];
			previousValueTo = new int[1];
			previousWhoTo = new int[1];
			previousWhatTo = new int[1];
			
			
			previousStateFrom[0] = currentStateFrom;
			previousRotationFrom[0] = currentRotationFrom;
			previousValueFrom[0] = currentValueFrom;
			previousStateTo[0] = csTo.state(to, typeTo);
			previousRotationTo[0] = csTo.rotation(to, typeTo); 
			previousValueTo[0] = csTo.value(to, typeTo);
			previousWhoTo[0] = csTo.who(to, typeTo);
			previousWhatTo[0] = csTo.what(to, typeTo);
			previousCountFrom = csFrom.count(from, typeFrom);
			previousCountTo = csTo.count(to, typeTo);
			
			if(context.game().hiddenInformation())
			{
				previousHiddenFrom = new boolean[1][context.players().size()];
				previousHiddenWhatFrom = new boolean[1][context.players().size()];
				previousHiddenWhoFrom =  new boolean[1][context.players().size()];
				previousHiddenCountFrom =  new boolean[1][context.players().size()];
				previousHiddenStateFrom =  new boolean[1][context.players().size()];
				previousHiddenRotationFrom =  new boolean[1][context.players().size()];
				previousHiddenValueFrom =  new boolean[1][context.players().size()];
				for (int pid = 1; pid < context.players().size(); pid++)
				{
					previousHiddenFrom[0][pid] = csFrom.isHidden(pid, from, 0, typeFrom);
					previousHiddenWhatFrom[0][pid] = csFrom.isHiddenWhat(pid, from, 0, typeFrom);
					previousHiddenWhoFrom[0][pid] = csFrom.isHiddenWho(pid, from, 0, typeFrom);
					previousHiddenCountFrom[0][pid] = csFrom.isHiddenCount(pid, from, 0, typeFrom);
					previousHiddenStateFrom[0][pid] = csFrom.isHiddenState(pid, from, 0, typeFrom);
					previousHiddenRotationFrom[0][pid] = csFrom.isHiddenRotation(pid, from, 0, typeFrom);
					previousHiddenValueFrom[0][pid] = csFrom.isHiddenValue(pid, from, 0, typeFrom);
				}
					
				previousHiddenTo = new boolean[1][context.players().size()];
				previousHiddenWhatTo = new boolean[1][context.players().size()];
				previousHiddenWhoTo =  new boolean[1][context.players().size()];
				previousHiddenCountTo =  new boolean[1][context.players().size()];
				previousHiddenStateTo =  new boolean[1][context.players().size()];
				previousHiddenRotationTo =  new boolean[1][context.players().size()];
				previousHiddenValueTo =  new boolean[1][context.players().size()];
				for (int pid = 1; pid < context.players().size(); pid++)
				{
					previousHiddenTo[0][pid] = csTo.isHidden(pid, to, 0, typeTo);
					previousHiddenWhatTo[0][pid] = csTo.isHiddenWhat(pid, to, 0, typeTo);
					previousHiddenWhoTo[0][pid] = csTo.isHiddenWho(pid, to, 0, typeTo);
					previousHiddenCountTo[0][pid] = csTo.isHiddenCount(pid, to, 0, typeTo);
					previousHiddenStateTo[0][pid] = csTo.isHiddenState(pid, to, 0, typeTo);
					previousHiddenRotationTo[0][pid] = csTo.isHiddenRotation(pid, to, 0, typeTo);
					previousHiddenValueTo[0][pid] = csTo.isHiddenValue(pid, to, 0, typeTo);
				}
			}
			
			alreadyApplied = true;
		}
		
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
				final int countTo = (context.game().usesLineOfPlay() ? piece.index() : 1);
				csTo.setCount(context.state(), locs.getQuick(i), countTo);
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
						final TIntArrayList locsToUpdate = largePiece.locs(context, i, currentState, context.topology());

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
		
		return this;
	}

	//-------------------------------------------------------------------------
	
	/**
	 * Undo method for large piece.
	 * @param context The context.
	 * @return The action undo.
	 */
	public Action undoLargePiece(final Context context)
	{
		final int contIdTo = typeTo.equals(SiteType.Cell) ? context.containerId()[to] : 0;
		final int contIdFrom = typeFrom.equals(SiteType.Cell) ? context.containerId()[from] : 0;
		final Game game = context.game();
		
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
			if(previousWhatTo[0] > 0)
			{
				if(csTo.what(to, typeTo) != previousWhatTo[0])
				{
					final int toValue =  (context.game().hasDominoes() ? 1 : previousValueTo[0]);
					csTo.setSite(context.state(), to, previousWhoTo[0], previousWhatTo[0], 1, previousStateTo[0], previousRotationTo[0], toValue, typeTo);

					Component pieceTo = null;

					// to keep the site of the item in cache for each player
					pieceTo = context.components()[previousWhatTo[0]];
					if (pieceTo.isDomino())
						context.state().remainingDominoes().remove(pieceTo.index());
						
					if(context.game().hiddenInformation())
					{
						for (int pid = 1; pid < context.players().size(); pid++)
						{
							csTo.setHidden(context.state(), pid, to, 0, typeTo, previousHiddenTo[0][pid]);
							csTo.setHiddenWhat(context.state(), pid, to, 0, typeTo, previousHiddenWhatTo[0][pid]);
							csTo.setHiddenWho(context.state(), pid, to, 0, typeTo, previousHiddenWhoTo[0][pid]);
							csTo.setHiddenCount(context.state(), pid, to, 0, typeTo, previousHiddenCountTo[0][pid]);
							csTo.setHiddenState(context.state(), pid, to, 0, typeTo, previousHiddenStateTo[0][pid]);
							csTo.setHiddenRotation(context.state(), pid, to, 0, typeTo, previousHiddenRotationTo[0][pid]);
							csTo.setHiddenValue(context.state(), pid, to, 0, typeTo, previousHiddenValueTo[0][pid]);
						}
					}
				}
			}
			else
			{
				if(previousStateTo[0] > 0 || previousRotationTo[0] > 0 || previousValueTo[0] > 0)
				{
					final int toValue =  (context.game().hasDominoes() ? 1 : previousValueTo[0]);
					csTo.setSite(context.state(), to, Constants.UNDEFINED, Constants.UNDEFINED, 0, previousStateTo[0], previousRotationTo[0], toValue, typeTo);
				}
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
		if (currentStateTo != Constants.UNDEFINED && previousStateFrom[0] == Constants.UNDEFINED)
			csFrom.setSite(context.state(), from, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, currentStateTo, Constants.UNDEFINED, Constants.OFF, typeFrom);
		else if (previousStateFrom[0] != Constants.UNDEFINED)
			csFrom.setSite(context.state(), from, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, previousStateFrom[0], Constants.UNDEFINED, Constants.OFF, typeFrom);

		// update the rotation state of the site From
		if (currentRotationTo != Constants.UNDEFINED && previousRotationFrom[0] == Constants.UNDEFINED)
			csFrom.setSite(context.state(), from, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, currentRotationTo, Constants.OFF, typeFrom);
		else if (previousRotationFrom[0] != Constants.UNDEFINED)
			csFrom.setSite(context.state(), from, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, previousRotationFrom[0], Constants.OFF, typeFrom);

		// update the piece value of the site From
		if (currentValueTo != Constants.UNDEFINED && previousValueFrom[0] == Constants.UNDEFINED)
		{
			final int fromValue = (context.game().usesLineOfPlay() ? 1 : currentValueTo);
			csFrom.setSite(context.state(), from, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, fromValue, typeFrom);
		}
		else if (previousValueFrom[0] != Constants.UNDEFINED)
		{
			final int fromValue = (context.game().usesLineOfPlay() ? 1 : previousValueFrom[0]);
			csFrom.setSite(context.state(), from, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, fromValue, typeFrom);
		}
			
		final int who = (what < 1) ? 0 : context.components()[what].owner();

		if (csFrom.what(from, typeFrom) == what && csFrom.count(from, typeFrom) > 0)
		{
			final int fromCount = (context.game().requiresCount() ? csFrom.count(from, typeFrom) + 1 : 1);
			final int fromValue = (context.game().usesLineOfPlay() ? 1 : Constants.OFF);
			csFrom.setSite(context.state(), from, Constants.UNDEFINED, Constants.UNDEFINED, fromCount, Constants.UNDEFINED, Constants.UNDEFINED, fromValue, typeFrom);
		}
		else
		{
			final int fromValue = (context.game().usesLineOfPlay() ? 1 : Constants.OFF);
			csFrom.setSite(context.state(), from, who, what, 1, Constants.UNDEFINED, Constants.UNDEFINED, fromValue, typeFrom);
		}

		// to keep the site of the item in cache for each player
		if (what != 0)
			piece = context.components()[what];

		// In case of LargePiece we update the empty chunkSet
		if (piece != null && piece.isLargePiece())
		{
			final Component largePiece = piece;
			final TIntArrayList locs = largePiece.locs(context, from, previousStateFrom[0], context.topology());
			for (int i = 0; i < locs.size(); i++)
			{
				csFrom.removeFromEmpty(locs.getQuick(i), SiteType.Cell);
				final int fromCount = (context.game().usesLineOfPlay() ? piece.index() : 1);
				csFrom.setCount(context.state(), locs.getQuick(i), fromCount);
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
						final TIntArrayList locsToUpdate = largePiece.locs(context, i, currentState, context.topology());

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
							+ "," + csFrom.state(from, typeFrom) + "," + csFrom.state(from, typeFrom) + ")");
		}
			
		return this;
	}
	
}
