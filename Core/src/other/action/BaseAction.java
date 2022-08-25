package other.action;

import java.util.BitSet;
import java.util.List;

import annotations.Hide;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.util.directions.AbsoluteDirection;
import game.util.directions.DirectionFacing;
import game.util.graph.Radial;
import main.Constants;
import other.context.Context;
import other.state.container.ContainerState;
import other.topology.Cell;
import other.topology.Topology;

/**
 * Action with default return values.
 * 
 * @author cambolbro and Eric.Piette
 */
@Hide
public abstract class BaseAction implements Action
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------
	
	/** decision action or not. */
	public boolean decision = false;

	//-------------------------------------------------------------------------
	
	@Override
	public int from()
	{
		return Constants.UNDEFINED;
	}
	
	@Override
	public SiteType fromType()
	{
		return SiteType.Cell;
	}

	@Override
	public int levelFrom()
	{
		return Constants.GROUND_LEVEL;
	}

	//-------------------------------------------------------------------------

	@Override
	public int to()
	{
		return Constants.UNDEFINED;
	}

	@Override
	public SiteType toType()
	{
		return SiteType.Cell;
	}

	@Override
	public int levelTo()
	{
		return Constants.GROUND_LEVEL;
	}

	//-------------------------------------------------------------------------

	@Override
	public int what()
	{
		return Constants.NO_PIECE;
	}

	@Override
	public int state()
	{
		return Constants.DEFAULT_STATE;
	}

	@Override
	public int rotation()
	{
		return Constants.DEFAULT_ROTATION;
	}

	@Override
	public int value()
	{
		return Constants.DEFAULT_ROTATION;
	}

	@Override
	public int count()
	{
		return Constants.NO_PIECE;
	}

	@Override
	public boolean isStacking()
	{
		return false;
	}

	@Override
	public boolean[] hidden()
	{
		return null;
	}

	@Override
	public int who()
	{
		return Constants.NOBODY;
	}

	@Override
	public boolean isDecision()
	{
		return decision;
	}

	@Override
	public boolean isPass()
	{
		return false;
	}
	
	@Override
	public boolean isForfeit()
	{
		return false;
	}

	@Override
	public boolean isSwap()
	{
		return false;
	}
	
	@Override
	public boolean isVote()
	{
		return false;
	}
	
	@Override
	public boolean isPropose()
	{
		return false;
	}
	
	@Override
	public boolean isAlwaysGUILegal()
	{
		return false;
	}

	@Override
	public String proposition()
	{
		return null;
	}

	@Override
	public String vote()
	{
		return null;
	}

	@Override
	public String message()
	{
		return null;
	}

	@Override
	public void setDecision(final boolean decision)
	{
		this.decision = decision;
	}

	@Override
	public Action withDecision(final boolean dec)
	{
		decision = dec;
		return this;
	}

	@Override
	public ActionType actionType() // TEMPORARY UNTIL ALL THE SUPER ACTIONS ARE NOT DONE.
	{
		return null;
	}

	@Override
	public boolean matchesUserMove(final int siteA, final int levelA, final SiteType graphElementTypeA, final int siteB,
			final int levelB, final SiteType graphElementTypeB)
	{
		return (from() == siteA && levelFrom() == levelA && fromType() == graphElementTypeA && to() == siteB
				&& levelTo() == levelB
						&& toType() == graphElementTypeB);
	}

	//-------------------------------------------------------------------------
	
	/**
	 * To update the playable site with the line of play of the dominoes.
	 * 
	 * @param context
	 * @param site
	 * @param dirn
	 */
	protected static void lineOfPlayDominoes(final Context context, final int site1, final int site2,
			final AbsoluteDirection dirn, final boolean doubleDomino, final boolean leftOrientation)
	{
		final ContainerState cs = context.containerState(0);
		final Topology topology = context.topology();
		final Cell c1 = context.topology().cells().get(site1);
		final Cell c2 = context.topology().cells().get(site2);

		final List<Radial> radialsC1 = topology.trajectories().radials(SiteType.Cell, c1.index(), dirn);
		final List<Radial> radialsC2 = topology.trajectories().radials(SiteType.Cell, c2.index(), dirn);

		if (radialsC1.size() > 0 && radialsC2.size() > 0)
		{
			final Radial radialC1 = radialsC1.get(0);
			final Radial radialC2 = radialsC2.get(0);
			
			if (radialC1.steps().length > 2 && radialC2.steps().length > 2
					&& cs.isEmpty(radialC1.steps()[1].id(), SiteType.Cell)
					&& cs.isEmpty(radialC2.steps()[1].id(), SiteType.Cell))
			{
				for (int i = 1; i < radialC1.steps().length && i < radialC2.steps().length
						&& i < 5; i++)
				{
					final int to = radialC1.steps()[i].id();
					final int to2 = radialC2.steps()[i].id();
					if (cs.isEmpty(to, SiteType.Cell) && cs.isEmpty(to2, SiteType.Cell))
					{
						cs.setPlayable(context.state(), to, true);
						cs.setPlayable(context.state(), to2, true);

						if (!doubleDomino && i < 3)
						{
							final DirectionFacing direction = AbsoluteDirection.convert(dirn);
							final DirectionFacing leftDirection = direction.left().left();
							final AbsoluteDirection absoluteLeftDirection = leftDirection.toAbsolute();
							final DirectionFacing rightDirection = direction.right().right();
							final AbsoluteDirection absoluteRightDirection = rightDirection.toAbsolute();

							if (leftOrientation)
							{
								final List<Radial> radialsC1Left = topology.trajectories().radials(SiteType.Cell,
										c1.index(), absoluteLeftDirection);
								final List<Radial> radialsC2Right = topology.trajectories().radials(SiteType.Cell,
										c2.index(), absoluteRightDirection);

								if (radialsC1Left.size() > 0 && radialsC1Left.get(0).steps().length > 1)
								{
									final int leftOfTo = radialsC1Left.get(0).steps()[1].id();
									if (cs.isEmpty(leftOfTo, SiteType.Cell))
										cs.setPlayable(context.state(), leftOfTo, true);
								}
								if (radialsC2Right.size() > 0 && radialsC2Right.get(0).steps().length > 1)
								{
									final int leftOfTo = radialsC2Right.get(0).steps()[1].id();
									if (cs.isEmpty(leftOfTo, SiteType.Cell))
										cs.setPlayable(context.state(), leftOfTo, true);
								}
							}
							else
							{
								final List<Radial> radialsC1Right = topology.trajectories().radials(SiteType.Cell,
										c1.index(), absoluteRightDirection);
								final List<Radial> radialsC2Left = topology.trajectories().radials(SiteType.Cell,
										c2.index(), absoluteLeftDirection);

								if (radialsC1Right.size() > 0 && radialsC1Right.get(0).steps().length > 1)
								{
									final int leftOfTo = radialsC1Right.get(0).steps()[1].id();
									if (cs.isEmpty(leftOfTo, SiteType.Cell))
										cs.setPlayable(context.state(), leftOfTo, true);
								}
								if (radialsC2Left.size() > 0 && radialsC2Left.get(0).steps().length > 1)
								{
									final int leftOfTo = radialsC2Left.get(0).steps()[1].id();
									if (cs.isEmpty(leftOfTo, SiteType.Cell))
										cs.setPlayable(context.state(), leftOfTo, true);
								}
							}
						}

					}
					else
						return;
				}
			}
		}
	}

	/**
	 * @param side
	 * @param state
	 * @return The direction of the line of play according to the side and the state
	 *         of the domino.
	 */
	@SuppressWarnings("static-method")
	protected AbsoluteDirection getDirnDomino(final int side, final int state)
	{
		switch (side)
		{
		case 0: // WEST SIDE
			switch (state)
			{
			case 0:
				return AbsoluteDirection.W;
			case 1:
				return AbsoluteDirection.N;
			case 2:
				return AbsoluteDirection.E;
			case 3:
				return AbsoluteDirection.S;
			}
			break;
		case 1: // NORTH SIDE
			switch (state)
			{
			case 0:
				return AbsoluteDirection.N;
			case 1:
				return AbsoluteDirection.E;
			case 2:
				return AbsoluteDirection.S;
			case 3:
				return AbsoluteDirection.W;
			}
			break;
		case 2: // EAST SIDE
			switch (state)
			{
			case 0:
				return AbsoluteDirection.E;
			case 1:
				return AbsoluteDirection.S;
			case 2:
				return AbsoluteDirection.W;
			case 3:
				return AbsoluteDirection.N;
			}
			break;
		case 3: // SOUTH SIDE
			switch (state)
			{
			case 0:
				return AbsoluteDirection.S;
			case 1:
				return AbsoluteDirection.W;
			case 2:
				return AbsoluteDirection.N;
			case 3:
				return AbsoluteDirection.E;
			}
			break;
		default:
			return null;
		}
		return null;
	}

	@Override
	public void setLevelFrom(final int levelA)
	{
		// do nothing in general.
	}

	@Override
	public void setLevelTo(final int levelB)
	{
		// do nothing in general.
	}

	@Override
	public boolean isOtherMove()
	{
		return false;
	}
	
	@Override
	public boolean isForced()
	{
		return false;
	}
	
	@Override
	public boolean containsNextInstance()
	{
		return false;
	}

	@Override
	public int playerSelected()
	{
		return Constants.UNDEFINED;
	}

	@Override
	public String toString()
	{
		return toTrialFormat(null);
	}

	@Override
	public String toMoveFormat(final Context context, final boolean useCoords)
	{
		return toTrialFormat(context);
	}

	@Override
	public BitSet concepts(final Context context, final Moves movesLudeme)
	{
		return new BitSet();
	}
}
