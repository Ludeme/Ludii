package game.functions.ints.board;

import java.util.BitSet;

import annotations.Opt;
import game.Game;
import game.equipment.component.Component;
import game.equipment.container.Container;
import game.functions.ints.BaseIntFunction;
import game.types.play.RoleType;
import main.Constants;
import other.context.Context;

/**
 * Returns the index of a component, player or region.
 * 
 * @author cambolbro and Eric.Piette
 * @remarks To translate a component, a player or a region to an index.
 */
public final class Id extends BaseIntFunction
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Which component. */
	private final String nameComponent;

	/** Which type of role. */
	private final RoleType who;

	//-------------------------------------------------------------------------

	/**
	 * To get the index of a component containing the name and owns by who.
	 * 
	 * @param name The name of the component.
	 * @param who  The owner of the component.
	 * 
	 * @example (id "Pawn" Mover)
	 * 
	 * @example (id P1)
	 */
	public Id
	(
		@Opt final String   name, 
		     final RoleType who
	)
	{
		this.nameComponent = name;
		this.who = who;
	}

	//-------------------------------------------------------------------------

	/**
	 * To get the index of a component containing its name.
	 * 
	 * @param name The name of the component.
	 * @return The index of the component.
	 * 
	 * @example (id "Pawn1")
	 */
	public static IndexOfComponent construct(final String name)	
	{
		return new IndexOfComponent(name);
	}
	
	//-------------------------------------------------------------------------

	@Override
	public int eval(final Context context)
	{
		if (who == RoleType.Player)
			return context.player(); // iterating over all players
		
		// Return index of specified player, else -1 if not found.
		if (who != null && nameComponent == null)
		{
			switch (who)
			{
			case Neutral: return 0;
			case P1:	return 1;
			case P2:	return 2;
			case P3:	return 3;
			case P4:	return 4;
			case P5:	return 5;
			case P6:	return 6;
			case P7:	return 7;
			case P8:	return 8;
			case P9:	return 9;
			case P10:	return 10;
			case P11:	return 11;
			case P12:	return 12;
			case P13:	return 13;
			case P14:	return 14;
			case P15:	return 15;
			case P16:	return 16;
			case Team1:	return 1;
			case Team2:	return 2;
			case Team3:	return 3;
			case Team4:	return 4;
			case Team5:	return 5;
			case Team6:	return 6;
			case Team7:	return 7;
			case Team8:	return 8;
			case Team9:	return 9;
			case Team10: return 10;
			case Team11: return 11;
			case Team12: return 12;
			case Team13: return 13;
			case Team14: return 14;
			case Team15: return 15;
			case Team16: return 16;
			case TeamMover: return context.state().getTeam(context.state().mover());
			case Shared: return context.game().players().count() + 1;
			case All:	return context.game().players().count() + 1;
			case Each:	return context.game().players().count() + 1;
//			case Any:	return context.game().players().count() + 1;
			case Mover:	return context.state().mover();
			case Next:	return context.state().next();
			case Prev:	return context.state().prev();
				//$CASES-OMITTED$
			default:	return Constants.OFF;
			}
		}
		else if (who != null)
		{
			final int playerId;
			switch (who)
			{
			case Neutral:
				playerId = 0;
				break;
			case P1:
				playerId = 1;
				break;
			case P2:
				playerId = 2;
				break;
			case P3:
				playerId = 3;
				break;
			case P4:
				playerId = 4;
				break;
			case P5:
				playerId = 5;
				break;
			case P6:
				playerId = 6;
				break;
			case P7:
				playerId = 7;
				break;
			case P8:
				playerId = 8;
				break;
			case Shared:
				playerId = context.game().players().count() + 1;
				break;
			case Mover:
				playerId = context.state().mover();
				break;
			case Next:
				playerId = context.state().next();
				break;
			case Prev:
				playerId = context.state().prev();
				break;
//			case Iterator:
//				return context.iterator();
				//$CASES-OMITTED$
			default:
				return Constants.OFF;
			}
			
			for (int i = 1; i < context.components().length; i++)
			{
				final Component component = context.components()[i];
				if (component.name().contains(nameComponent) && component.owner() == playerId)
					return i;
			}

			return -1;
			
		}
		
		// Return index of specified component, else -1 if not found.
		if (nameComponent != null)
		{
			for (int i = 0; i < context.containers().length; i++)
			{
				final Container container = context.containers()[i];
				if (container.name().equals(nameComponent))
					return i;
			}

			for (int i = 1; i < context.components().length; i++)
			{
				final Component component = context.components()[i];
				if (component.name().equals(nameComponent))
					return i;
			}
		}

		return -1;
	}

	//-------------------------------------------------------------------------

	@Override
	public boolean isStatic()
	{
		return (who == RoleType.Neutral ||
				who == RoleType.P1 ||
				who == RoleType.P2 ||
				who == RoleType.P3 ||
				who == RoleType.P4 ||
				who == RoleType.P5 ||
				who == RoleType.P6 ||
				who == RoleType.P7 ||
				who == RoleType.P8 ||
				who == RoleType.Shared ||
				who == RoleType.All ||
				who == RoleType.Each);
	}

	@Override
	public long gameFlags(final Game game)
	{
		return 0L;
	}
	
	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		return readEvalContext;
	}

	@Override
	public void preprocess(final Game game)
	{
		// nothing to do
	} 

	//-------------------------------------------------------------------------
	
	/**
	 * IndexOf Component.
	 * 
	 * @author cambolbro and Eric.Piette and Dennis Soemers
	 */
	public static class IndexOfComponent extends BaseIntFunction  //BaseLudeme implements IntFunction
	{
		/** */
		private static final long serialVersionUID = 1L;
	
		/** Which component. */
		protected final String nameComponent;
	
		/** Pre-computed index */
		protected int precomputedIdx = -1;
	
		//---------------------------------------------------------------------
	
		/**
		 * Constructor for a component.
		 * 
		 * @param name
		 */
		public IndexOfComponent(final String name)
		{
			this.nameComponent = new String(name);
		}
	
		//---------------------------------------------------------------------
	
		@Override
		public int eval(final Context context)
		{
			if (precomputedIdx == -1)
				preprocess(context.game());
	
			return precomputedIdx;
		}
	
		//---------------------------------------------------------------------
	
		@Override
		public boolean isStatic()
		{
			return true;
		}
	
		@Override
		public long gameFlags(final Game game)
		{
			return 0L;
		}
		
		@Override
		public BitSet concepts(final Game game)
		{
			final BitSet concepts = new BitSet();
			return concepts;
		}

		@Override
		public BitSet writesEvalContextRecursive()
		{
			final BitSet writeEvalContext = new BitSet();
			return writeEvalContext;
		}

		@Override
		public BitSet readsEvalContextRecursive()
		{
			final BitSet readEvalContext = new BitSet();
			return readEvalContext;
		}

		@Override
		public void preprocess(final Game game)
		{
			for (int i = 0; i < game.equipment().containers().length; i++)
			{
				final Container container = game.equipment().containers()[i];
				if (container.name().equals(nameComponent))
				{
					precomputedIdx = i;
					return;
				}
			}

			for (int i = 1; i < game.equipment().components().length; i++)
			{
				final Component component = game.equipment().components()[i];
				if (component.name().equals(nameComponent))
				{
					precomputedIdx = i;
					return;
				}
			}
		}
	}

}
