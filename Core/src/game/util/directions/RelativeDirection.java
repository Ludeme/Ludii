package game.util.directions;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import game.functions.directions.Directions;
import game.functions.directions.DirectionsFunction;
import other.concept.Concept;

//-----------------------------------------------------------------------------

/**
 * Describes categories of relative directions.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * @remarks Relative directions are typically used to describe player movements or relationships between items.
 */
public enum RelativeDirection implements Direction
{
	/** Forward (only) direction. */
	Forward(true)
	{
		@Override
		public List<DirectionFacing> directions(final DirectionFacing baseDirn, final List<DirectionFacing> supportedDirections)
		{
			final List<DirectionFacing> directions = new ArrayList<DirectionFacing>();

			if (supportedDirections.contains(baseDirn))
				directions.add(baseDirn);

			return directions;
		}
	},
	
	/** Backward (only) direction. */
	Backward(true)
	{
		@Override
		public List<DirectionFacing> directions(final DirectionFacing baseDirn, final List<DirectionFacing> supportedDirections)
		{
			final List<DirectionFacing> directions = new ArrayList<DirectionFacing>();

			if (supportedDirections.contains(baseDirn.opposite()))
				directions.add(baseDirn.opposite());

			return directions;
		}
	},
	
	/** Rightward (only) direction. */
	Rightward(true)
	{
		@Override
		public List<DirectionFacing> directions(final DirectionFacing baseDirn, final List<DirectionFacing> supportedDirections)
		{
			final List<DirectionFacing> directions = new ArrayList<DirectionFacing>();

			if (supportedDirections.contains(baseDirn.rightward()))
				directions.add(baseDirn.rightward());

			return directions;
		}
	},
	
	/** Leftward (only) direction. */
	Leftward(true)
	{
		@Override
		public List<DirectionFacing> directions(final DirectionFacing baseDirn, final List<DirectionFacing> supportedDirections)
		{
			final List<DirectionFacing> directions = new ArrayList<DirectionFacing>();

			if (supportedDirections.contains(baseDirn.leftward()))
				directions.add(baseDirn.leftward());
			return directions;
		}
	},
	
	/** Forwards directions. */
	Forwards(true)
	{
		@Override
		public List<DirectionFacing> directions(final DirectionFacing baseDirn, final List<DirectionFacing> supportedDirections)
		{
			final List<DirectionFacing> directions = new ArrayList<DirectionFacing>();

			DirectionFacing directionToAdd = baseDirn.leftward().right();
			while (directionToAdd != baseDirn.rightward())
			{
				if (supportedDirections.contains(directionToAdd))
					directions.add(directionToAdd);
				directionToAdd = directionToAdd.right();
			}

			return directions;
		}
	},
	
	/** Backwards directions. */
	Backwards(true)
	{
		@Override
		public List<DirectionFacing> directions(final DirectionFacing baseDirn, final List<DirectionFacing> supportedDirections)
		{
			final List<DirectionFacing> directions = new ArrayList<DirectionFacing>();

			DirectionFacing directionToAdd = baseDirn.opposite().leftward().right();
			while (directionToAdd != baseDirn.opposite().rightward())
			{
				if (supportedDirections.contains(directionToAdd))
					directions.add(directionToAdd);
				directionToAdd = directionToAdd.right();
			}

			return directions;
		}
	},
	
	/** Rightwards directions. */
	Rightwards(true)
	{
		@Override
		public List<DirectionFacing> directions(final DirectionFacing baseDirn, final List<DirectionFacing> supportedDirections)
		{
			final List<DirectionFacing> directions = new ArrayList<DirectionFacing>();

			DirectionFacing directionToAdd = baseDirn.right();
			while (directionToAdd != baseDirn.opposite())
			{
				if (supportedDirections.contains(directionToAdd))
					directions.add(directionToAdd);
				directionToAdd = directionToAdd.right();
			}

			return directions;
		}
	},
	
	/** Leftwards directions. */
	Leftwards(true)
	{
		@Override
		public List<DirectionFacing> directions(final DirectionFacing baseDirn, final List<DirectionFacing> supportedDirections)
		{
			final List<DirectionFacing> directions = new ArrayList<DirectionFacing>();

			DirectionFacing directionToAdd = baseDirn.left();
			while (directionToAdd != baseDirn.opposite())
			{
				if (supportedDirections.contains(directionToAdd))
					directions.add(directionToAdd);
				directionToAdd = directionToAdd.left();
			}

			return directions;
		}
	},
	
	/** Forward-Left direction. */
	FL(true)
	{
		@Override
		public List<DirectionFacing> directions(final DirectionFacing baseDirn, final List<DirectionFacing> supportedDirections)
		{
			final List<DirectionFacing> directions = new ArrayList<DirectionFacing>(1);

			DirectionFacing left = baseDirn.left();
			while (!supportedDirections.contains(left))
			{
				left = left.left();
			}
			directions.add(left);
			return directions;
		}
	},
	
	/** Forward-Left-Left direction. */
	FLL(true)
	{
		@Override
		public List<DirectionFacing> directions(final DirectionFacing baseDirn, final List<DirectionFacing> supportedDirections)
		{
			final List<DirectionFacing> directions = new ArrayList<DirectionFacing>(1);

			DirectionFacing left = baseDirn.left();
			while (!supportedDirections.contains(left))
			{
				left = left.left();
			}
			while (!supportedDirections.contains(left))
			{
				left = left.left();
			}
			directions.add(left);
			return directions;
		}
	},
	
	/** Forward-Left-Left-Left direction. */
	FLLL(true)
	{
		@Override
		public List<DirectionFacing> directions(final DirectionFacing baseDirn, final List<DirectionFacing> supportedDirections)
		{
			final List<DirectionFacing> directions = new ArrayList<DirectionFacing>(1);

			DirectionFacing left = baseDirn.left();
			while (!supportedDirections.contains(left))
			{
				left = left.left();
			}
			while (!supportedDirections.contains(left))
			{
				left = left.left();
			}
			while (!supportedDirections.contains(left))
			{
				left = left.left();
			}
			directions.add(left);
			return directions;
		}
	},
	
	/** Backward-Left direction. */
	BL(true)
	{
		@Override
		public List<DirectionFacing> directions(final DirectionFacing baseDirn, final List<DirectionFacing> supportedDirections)
		{
			final List<DirectionFacing> directions = new ArrayList<DirectionFacing>(1);

			DirectionFacing left = baseDirn.opposite().left();
			while (!supportedDirections.contains(left))
			{
				left = left.left();
			}
			directions.add(left);
			return directions;
		}
	},
	
	/** Backward-Left-Left direction. */
	BLL(true)
	{
		@Override
		public List<DirectionFacing> directions(final DirectionFacing baseDirn, final List<DirectionFacing> supportedDirections)
		{
			final List<DirectionFacing> directions = new ArrayList<DirectionFacing>(1);

			DirectionFacing left = baseDirn.opposite().left();
			while (!supportedDirections.contains(left))
			{
				left = left.left();
			}
			while (!supportedDirections.contains(left))
			{
				left = left.left();
			}
			directions.add(left);
			return directions;
		}
	},
	
	/** Backward-Left-Left-Left direction. */
	BLLL(true)
	{
		@Override
		public List<DirectionFacing> directions(final DirectionFacing baseDirn, final List<DirectionFacing> supportedDirections)
		{
			final List<DirectionFacing> directions = new ArrayList<DirectionFacing>(1);

			DirectionFacing left = baseDirn.opposite().left();
			while (!supportedDirections.contains(left))
			{
				left = left.left();
			}
			while (!supportedDirections.contains(left))
			{
				left = left.left();
			}
			while (!supportedDirections.contains(left))
			{
				left = left.left();
			}
			directions.add(left);
			return directions;
		}
	},
	
	/** Forward-Right direction. */
	FR(true)
	{
		@Override
		public List<DirectionFacing> directions(final DirectionFacing baseDirn, final List<DirectionFacing> supportedDirections)
		{
			final List<DirectionFacing> directions = new ArrayList<DirectionFacing>(1);

			DirectionFacing right = baseDirn.right();
			while (!supportedDirections.contains(right))
			{
				right = right.right();
			}
			directions.add(right);
			return directions;
		}
	},
	
	/** Forward-Right-Right direction. */
	FRR(true)
	{
		@Override
		public List<DirectionFacing> directions(final DirectionFacing baseDirn, final List<DirectionFacing> supportedDirections)
		{
			final List<DirectionFacing> directions = new ArrayList<DirectionFacing>(1);

			DirectionFacing right = baseDirn.right();
			while (!supportedDirections.contains(right))
			{
				right = right.right();
			}
			while (!supportedDirections.contains(right))
			{
				right = right.right();
			}
			directions.add(right);
			return directions;
		}
	},
	
	/** Forward-Right-Right-Right direction. */
	FRRR(true)
	{
		@Override
		public List<DirectionFacing> directions(final DirectionFacing baseDirn, final List<DirectionFacing> supportedDirections)
		{
			final List<DirectionFacing> directions = new ArrayList<DirectionFacing>(1);

			DirectionFacing right = baseDirn.right();
			while (!supportedDirections.contains(right))
			{
				right = right.right();
			}
			while (!supportedDirections.contains(right))
			{
				right = right.right();
			}
			while (!supportedDirections.contains(right))
			{
				right = right.right();
			}
			directions.add(right);
			return directions;
		}
	},
	
	/** Backward-Right direction. */
	BR(true)
	{
		@Override
		public List<DirectionFacing> directions(final DirectionFacing baseDirn, final List<DirectionFacing> supportedDirections)
		{
			final List<DirectionFacing> directions = new ArrayList<DirectionFacing>(1);

			DirectionFacing right = baseDirn.opposite().right();
			while (!supportedDirections.contains(right))
			{
				right = right.right();
			}
			directions.add(right);
			return directions;
		}
	},
	
	/** Backward-Right-Right direction. */
	BRR(true)
	{
		@Override
		public List<DirectionFacing> directions(final DirectionFacing baseDirn, final List<DirectionFacing> supportedDirections)
		{
			final List<DirectionFacing> directions = new ArrayList<DirectionFacing>(1);

			DirectionFacing right = baseDirn.opposite().right();
			while (!supportedDirections.contains(right))
			{
				right = right.right();
			}
			while (!supportedDirections.contains(right))
			{
				right = right.right();
			}
			directions.add(right);
			return directions;
		}
	},
	
	/** Backward-Right-Right-Right direction. */
	BRRR(true)
	{
		@Override
		public List<DirectionFacing> directions(final DirectionFacing baseDirn, final List<DirectionFacing> supportedDirections)
		{
			final List<DirectionFacing> directions = new ArrayList<DirectionFacing>(1);

			DirectionFacing right = baseDirn.opposite().right();
			while (!supportedDirections.contains(right))
			{
				right = right.right();
			}
			while (!supportedDirections.contains(right))
			{
				right = right.right();
			}
			while (!supportedDirections.contains(right))
			{
				right = right.right();
			}
			directions.add(right);
			return directions;
		}
	},
	
	/** Same direction. */
	SameDirection(true) {
		@Override
		public List<DirectionFacing> directions(final DirectionFacing baseDirn, final List<DirectionFacing> supportedDirections)
		{
			final List<DirectionFacing> directions = new ArrayList<DirectionFacing>();
			return directions;
		}
	},
	
	/** Opposite direction. */
	OppositeDirection(true)
	{
		@Override
		public List<DirectionFacing> directions(final DirectionFacing baseDirn, final List<DirectionFacing> supportedDirections)
		{
			final List<DirectionFacing> directions = new ArrayList<DirectionFacing>();
			return directions;
		}
	},
	;

	//-------------------------------------------------------------------------

	/**
	 * @param baseDirn            The original direction.
	 * @param supportedDirections The supported directions.
	 * 
	 * @return The corresponding directions.
	 */
	public abstract List<DirectionFacing> directions(final DirectionFacing baseDirn, final List<DirectionFacing> supportedDirections);

	/**
	 * @param isAbsolute
	 */
	private RelativeDirection(final boolean isAbsolute)
	{
		// Nothing to do.
	}

	@Override
	public DirectionsFunction directionsFunctions()
	{
		return new Directions(this, null, null, null);
	}

	/**
	 * @param relativeDirection The direction.
	 * @return The involved concepts.
	 */
	public static BitSet concepts(final RelativeDirection relativeDirection)
	{
		final BitSet concepts = new BitSet();
		switch (relativeDirection)
		{
		case Forward:
		{
			concepts.set(Concept.ForwardDirection.id(), true);
			break;
		}
		case Backward:
		{
			concepts.set(Concept.BackwardDirection.id(), true);
			break;
		}
		case Forwards:
		{
			concepts.set(Concept.ForwardsDirection.id(), true);
			break;
		}
		case Backwards:
		{
			concepts.set(Concept.BackwardsDirection.id(), true);
			break;
		}
		case Rightward:
		{
			concepts.set(Concept.RightwardDirection.id(), true);
			break;
		}
		case Leftward:
		{
			concepts.set(Concept.LeftwardDirection.id(), true);
			break;
		}
		case Rightwards:
		{
			concepts.set(Concept.RightwardsDirection.id(), true);
			break;
		}
		case Leftwards:
		{
			concepts.set(Concept.LeftwardsDirection.id(), true);
			break;
		}
		case FL:
		{
			concepts.set(Concept.ForwardLeftDirection.id(), true);
			break;
		}
		case FR:
		{
			concepts.set(Concept.ForwardRightDirection.id(), true);
			break;
		}
		case BL:
		{
			concepts.set(Concept.BackwardLeftDirection.id(), true);
			break;
		}
		case BR:
		{
			concepts.set(Concept.BackwardRightDirection.id(), true);
			break;
		}

		case SameDirection:
		{
			concepts.set(Concept.SameDirection.id(), true);
			break;
		}
		case OppositeDirection:
		{
			concepts.set(Concept.OppositeDirection.id(), true);
			break;
		}
		default:
		}

		return concepts;
	}
}
