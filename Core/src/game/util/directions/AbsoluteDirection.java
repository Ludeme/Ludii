package game.util.directions;

import java.util.BitSet;

import game.functions.directions.Directions;
import game.functions.directions.DirectionsFunction;
import game.types.board.RelationType;
import other.concept.Concept;

/**
 * Describes categories of absolute directions.
 * 
 * @author Eric.Piette and cambolbro
 * 
 * @remarks Absolute directions may be used to describe connectivity type, specific board sides, player movement directions, etc.
 */
public enum AbsoluteDirection implements Direction
{
	/** All directions. */
	All
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return true;
		}
	},
	
	/** Angled directions. */
	Angled
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.NW
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.NNW
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.WNW
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.WSW
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.SE
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.SW
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.SSE
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.SSW
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.NNE
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.ESE
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.ENE
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.NE;
		}
	},

	/** Adjacent directions. */
	Adjacent
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return false;
		}
	},
	
	/** Axial directions. */
	Axial
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.N
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.S
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.E
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.W
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.U
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.D;
		}
	},

	/** Orthogonal directions. */
	Orthogonal
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return false;
		}
	},
	
	/** Diagonal directions. */
	Diagonal
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return false;
		}
	},
	
	/** Off-diagonal directions. */
	OffDiagonal
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return false;
		}
	},
	/** Directions on the same layer. */
	SameLayer
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return false;
		}
	},
	/** Upward directions. */
	Upward
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return     dirn.getDirectionActual().uniqueName() == DirectionUniqueName.U
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.UN
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.UE
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.US
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.UW
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.UNW
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.UNE
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.USE
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.USW;
		}
	},
	/** Downward directions. */
	Downward
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return 	   dirn.getDirectionActual().uniqueName() == DirectionUniqueName.D
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.DN
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.DE
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.DS
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.DW
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.DNW
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.DNE
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.DSE
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.DSW;
		}
	},
	/** Rotational directions. */
	Rotational
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return 	   dirn.getDirectionActual().uniqueName() == DirectionUniqueName.CW
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.CCW
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.In
					|| dirn.getDirectionActual().uniqueName() == DirectionUniqueName.Out;
		}
	},
	
	/** Base directions. */
	Base
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return false;
		}
	},

	/** Support directions. */
	Support
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return false;
		}
	},

	// -------------------Intercardinal equivalent absolute direction--------------

	/** North. */
	N{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.N;
		}
	},
	
	/** East. */
	E
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.E;
		}
	},
	
	/** South. */
	S
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.S;
		}
	},
	
	/** West. */
	W
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.W;
		}
	},
	
	/** North-East. */
	NE
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.NE;
		}
	},
	
	/** South-East. */
	SE
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.SE;
		}
	},
	
	/** North-West. */
	NW
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.NW;
		}
	},
	
	/** South-West. */
	SW
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.SW;
		}
	},
	
	/** North-North-West. */
	NNW
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.NNW;
		}
	},
	
	/** West-North-West. */
	WNW
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.WNW;
		}
	},
	
	/** West-South-West. */
	WSW
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.WSW;
		}
	},
	
	/** South-South-West. */
	SSW
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.SSW;
		}
	},
	
	/** South-South-East. */
	SSE
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.SSE;
		}
	},
	
	/** East-South-East. */
	ESE
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.ESE;
		}
	},
	
	/** East-North-East. */
	ENE
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.ENE;
		}
	},
	
	/** North-North-East. */
	NNE
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.NNE;
		}
	},

	// -------------------Rotational equivalent absolute direction--------------

	/** Clockwise directions. */
	CW
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.CW;
		}
	},
	
	/** Counter-Clockwise directions. */
	CCW
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.CCW;
		}
	},
	
	/** Inwards directions. */
	In
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.In;
		}
	},
	
	/** Outwards directions. */
	Out
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.Out;
		}
	},

	// -------------------Spatial equivalent absolute direction--------------

	/** Upper direction. */
	U
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.U;
		}
	},
	/** Upwards-North direction. */
	UN
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.UN;
		}
	},
	/** Upwards-North-East direction. */
	UNE
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName()==DirectionUniqueName.UNE;
		}
	},
	/** Upwards-East direction. */
	UE
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.UE;
		}
	},
	/** Upwards-South-East direction. */
	USE
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.USE;
		}
	},
	/** Upwards-South direction. */
	US
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.US;
		}
	},
	/** Upwards-South-West direction. */
	USW
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.USW;
		}
	},
	/** Upwards-West direction. */
	UW
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.UW;
		}
	},
	/** Upwards-North-West direction. */
	UNW
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.UNW;
		}
	},

	/** Down direction. */
	D
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.D;
		}
	},
	/** Down-North direction. */
	DN
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.DN;
		}
	},
	/** Down-North-East. */
	DNE
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.DNE;
		}
	},
	/** Down-East direction. */
	DE
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.DE;
		}
	},
	/** Down-South-East. */
	DSE
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.DSE;
		}
	},
	/** Down-South direction. */
	DS
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.DS;
		}
	},
	/** Down-South-West. */
	DSW
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.DSW;
		}
	},
	/** Down-West direction. */
	DW
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.DW;
		}
	},
	/** Down North West. */
	DNW
	{
		@Override
		public boolean matches(final DirectionFacing baseDirn, final DirectionType dirn)
		{
			return dirn.getDirectionActual().uniqueName() == DirectionUniqueName.DNW;
		}
	},
	;

	//-------------------------------------------------------------------------

	/**
	 * NOTE: baseDirn may be null; it represents the facing of the piece, and may
	 * not be relevant
	 * 
	 * @param baseDirn
	 * @param dirn
	 * @return true if baseDirn+dirn matches the requirements of this category
	 */
	public abstract boolean matches(final DirectionFacing baseDirn, final DirectionType dirn);

	/**
	 * @param dirn The direction.
	 * @return Whether the given direction is a specific direction (e.g. N) as
	 *         opposed to belonging to a class of directions (e.g. Adjacent).
	 */
	public static boolean specific(final AbsoluteDirection dirn)
	{
		return convert(dirn) != null;
	}

	/**
	 * @return The specific direction corresponding to the absolute direction.
	 */
	public boolean specific()
	{
		return specific(this);
	}
	
	/**
	 * @param absoluteDirection
	 * @return the corresponding direction.
	 */
	public static DirectionFacing convert(final AbsoluteDirection absoluteDirection)
	{
		switch (absoluteDirection)
		{
		// Absolute with no equivalent directions
		case Adjacent:
			return null;
		case All:
			return null;
		case Axial:
			return null;
		case Angled:
			return null;
		case Diagonal:
			return null;
		case OffDiagonal:
			return null;
		case Orthogonal:
			return null;
		case Downward:
			return null;
		case SameLayer:
			return null;
		case Upward:
			return null;
		case Rotational:
			return null;

		// Absolute with an equivalent intercardinalDirection
		case E:
			return CompassDirection.E;
		case ENE:
			return CompassDirection.ENE;
		case ESE:
			return CompassDirection.ESE;
		case N:
			return CompassDirection.N;
		case NE:
			return CompassDirection.NE;
		case NNE:
			return CompassDirection.NNE;
		case NNW:
			return CompassDirection.NNW;
		case NW:
			return CompassDirection.NW;
		case S:
			return CompassDirection.S;
		case SE:
			return CompassDirection.SE;
		case SSE:
			return CompassDirection.SSE;
		case SSW:
			return CompassDirection.SSW;
		case SW:
			return CompassDirection.SW;
		case W:
			return CompassDirection.W;
		case WNW:
			return CompassDirection.WNW;
		case WSW:
			return CompassDirection.WSW;

		// Absolute with an equivalent RotationalDirection
		case CCW:
			return RotationalDirection.CCW;
		case CW:
			return RotationalDirection.CW;
		case In:
			return RotationalDirection.In;
		case Out:
			return RotationalDirection.Out;

		// Absolute with an equivalent SpatialDirection
		case U:
			return SpatialDirection.U;
		case UN:
			return SpatialDirection.UN;
		case UNE:
			return SpatialDirection.UNE;
		case UE:
			return SpatialDirection.UE;
		case USE:
			return SpatialDirection.USE;
		case US:
			return SpatialDirection.US;
		case USW:
			return SpatialDirection.USW;
		case UW:
			return SpatialDirection.UW;
		case UNW:
			return SpatialDirection.UNW;
		case D:
			return SpatialDirection.D;
		case DN:
			return SpatialDirection.DN;
		case DNE:
			return SpatialDirection.DNE;
		case DE:
			return SpatialDirection.DE;
		case DSE:
			return SpatialDirection.DSE;
		case DS:
			return SpatialDirection.DS;
		case DSW:
			return SpatialDirection.DSW;
		case DW:
			return SpatialDirection.DW;
		case DNW:
			return SpatialDirection.DNW;

		default:
			break;
		}

		return null;
	}

	@Override
	public DirectionsFunction directionsFunctions()
	{
		return new Directions(this, null);
	}

	/**
	 * @param absoluteDirection The direction to convert.
	 * @return The equivalent in relation type.
	 */
	public static RelationType converToRelationType(final AbsoluteDirection absoluteDirection)
	{
		switch (absoluteDirection)
		{
		case Adjacent:
			return RelationType.Adjacent;
		case Diagonal:
			return RelationType.Diagonal;
		case All:
			return RelationType.All;
		case OffDiagonal:
			return RelationType.OffDiagonal;
		case Orthogonal:
			return RelationType.Orthogonal;
		default:
			return null;
		}
	}

	/**
	 * @param absoluteDirection The direction.
	 * @return The involved concepts.
	 */
	public static BitSet concepts(final AbsoluteDirection absoluteDirection)
	{
		final BitSet concepts = new BitSet();
		switch (absoluteDirection)
		{
		case Adjacent:
		{
			concepts.set(Concept.AdjacentDirection.id(), true);
			break;
		}
		case Diagonal:
		{
			concepts.set(Concept.DiagonalDirection.id(), true);
			break;
		}
		case All:
		{
			concepts.set(Concept.AllDirections.id(), true);
			break;
		}
		case OffDiagonal:
		{
			concepts.set(Concept.OffDiagonalDirection.id(), true);
			break;
		}
		case Orthogonal:
		{
			concepts.set(Concept.OrthogonalDirection.id(), true);
			break;
		}
		case Rotational:
		{
			concepts.set(Concept.RotationalDirection.id(), true);
			break;
		}
		case SameLayer:
		{
			concepts.set(Concept.SameLayerDirection.id(), true);
			break;
		}
		default:
		}

		return concepts;
	}

}
