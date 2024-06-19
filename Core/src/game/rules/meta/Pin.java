package game.rules.meta;

import java.util.BitSet;
import java.util.List;

import game.Game;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.types.play.PinType;
import game.util.directions.AbsoluteDirection;
import game.util.graph.Step;
import other.MetaRules;
import other.action.Action;
import other.action.ActionType;
import other.context.Context;
import other.move.Move;
import other.state.container.ContainerState;

/**
 * To filter some remove moves in case some pieces can not be removed/moved because of pieces on top of them.
 * 
 * @author Eric.Piette
 */
public class Pin extends MetaRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * The pin type.
	 */
	final PinType type;

	/**
	 * @param type The type of pin.
	 * 
	 * @example (pin SupportMultiple)
	 */
	public Pin(final PinType type)
	{
		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public void eval(final Context context)
	{
		context.game().metaRules().setPinType(type);
	}

	/**
	 * @param context The context.
	 * @param legalMoves The original legal moves.
	 */
	public static void apply(final Context context, final Moves legalMoves)
	{
		final Game game = context.game();
		final MetaRules metaRules = game.metaRules();
		final PinType pinType = metaRules.pinType();
		if (pinType != null)
		{
			if (pinType.equals(PinType.SupportMultiple))
			{
				for (int indexMove = legalMoves.moves().size() - 1; indexMove >= 0; indexMove--)
				{
					final Move move = legalMoves.moves().get(indexMove);
					boolean forbiddenMove = false;
					for(final Action action : move.actions())
						if (action != null && (action.actionType().equals(ActionType.Remove)) || (action.actionType().equals(ActionType.Move)))
						{			

							int siteToRemove = -1; 
							
							if (action.actionType().equals(ActionType.Remove)) { 
								siteToRemove = action.to(); 
							}
							else if (action.actionType().equals(ActionType.Move)) { 
								siteToRemove = action.from(); 
							}
							
							final ContainerState cs = context.containerState(context.containerId()[siteToRemove]);
							if (cs.what(siteToRemove, SiteType.Vertex) != 0 && !context.equipment().containers()[context.containerId()[siteToRemove]].isHand())  // modif ced
							{
								final List<game.util.graph.Step> steps = game.board().topology().trajectories()
										.steps(SiteType.Vertex, siteToRemove, SiteType.Vertex, AbsoluteDirection.Upward);

								int numOccupiedUpWardSites = 0;
								for (final Step step : steps)
								{
									final int toSite = step.to().id();
									if (cs.what(toSite, SiteType.Vertex) != 0)
										numOccupiedUpWardSites++;
								}
								if (numOccupiedUpWardSites > 1)
								{
									forbiddenMove = true;
									break;
								}
							}
						}

					if (forbiddenMove) {
						legalMoves.moves().remove(indexMove);
					}
				}
			}
		}
	}
	
	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		return 0;
	}

	@Override
	public boolean isStatic()
	{
		return true;
	}

	@Override
	public void preprocess(final Game game)
	{
		// Nothing to do.
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		return concepts;
	}

	@Override
	public int hashCode()
	{
		final int result = 1;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof Pin))
			return false;

		return true;
	}
}