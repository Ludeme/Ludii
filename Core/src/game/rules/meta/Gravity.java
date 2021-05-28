package game.rules.meta;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import annotations.Opt;
import game.Game;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.types.play.GravityType;
import game.util.directions.AbsoluteDirection;
import game.util.graph.Step;
import main.Constants;
import other.MetaRules;
import other.action.Action;
import other.action.move.ActionMove;
import other.concept.Concept;
import other.context.Context;
import other.context.TempContext;
import other.move.Move;
import other.state.container.ContainerState;
import other.topology.Topology;

/**
 * To apply a certain type of gravity after making a move.
 * 
 * @author Eric.Piette
 */
public class Gravity extends MetaRule
{
	private static final long serialVersionUID = 1L;

	// -------------------------------------------------------------------------

	/**
	 * The gravity type.
	 */
	final GravityType type;
	
	/**
	 * @param type The type of gravity [PyramidalDrop].
	 * 
	 * @example (gravity)
	 */
	public Gravity
	(
		@Opt final GravityType type
	)
	{
		this.type = (type == null) ? GravityType.PyramidalDrop : type;
	}

	// -------------------------------------------------------------------------

	@Override
	public void eval(final Context context)
	{
		context.game().metaRules().setGravityType(type);
	}
	
	/**
	 * @param context The context.
	 * @param move The original move.
	 */
	public static void apply(final Context context, final Move move)
	{
		final Game game = context.game();
		final MetaRules metaRules = game.metaRules();
		if (metaRules.gravityType() != null)
		{
			if (metaRules.gravityType().equals(GravityType.PyramidalDrop))
			{
				final Topology topology = context.topology();
				boolean pieceDropped = false;
				final Move droppedMove = new Move(new ArrayList<Action>());

				final Context newContext = new TempContext(context);
				game.applyInternal(newContext, move, false);
				do
				{
					pieceDropped = false;
					for (int site = 0; site < topology.vertices().size(); site++)
					{
						final ContainerState cs = newContext.containerState(newContext.containerId()[site]);
						if (cs.what(site, SiteType.Vertex) != 0)
						{
							final List<game.util.graph.Step> steps = topology.trajectories()
									.steps(SiteType.Vertex, site, SiteType.Vertex, AbsoluteDirection.Downward);

							for (final Step step : steps)
							{
								final int toSite = step.to().id();
								if (cs.what(toSite, SiteType.Vertex) == 0)
								{
									final Action action = new ActionMove(SiteType.Vertex, site, 0,
											SiteType.Vertex, toSite, 0, Constants.UNDEFINED,
											Constants.UNDEFINED, Constants.UNDEFINED, false);
									final Move moveToApply = new Move(action);
									moveToApply.apply(newContext, false);
									droppedMove.actions().add(action);
									pieceDropped = true;
									break;
								}
							}
						}
						if (pieceDropped)
							break;
					}
				}
				while (pieceDropped);

				final Moves droppedMoves = new BaseMoves(null);
				droppedMoves.moves().add(droppedMove);
				move.then().add(droppedMoves);
			}
		}
	}

	// -------------------------------------------------------------------------

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
		concepts.set(Concept.CopyContext.id(), true);
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

		if (!(obj instanceof Gravity))
			return false;

		return true;
	}
}