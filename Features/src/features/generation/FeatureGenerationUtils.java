package features.generation;

import java.util.EnumSet;
import java.util.List;

import features.spatial.elements.FeatureElement.ElementType;
import game.Game;
import game.equipment.component.Component;
import game.equipment.other.Regions;
import game.util.directions.DirectionFacing;
import game.util.directions.RelativeDirection;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import other.context.Context;

/**
 * Utility methods for feature generation
 * 
 * @author Dennis Soemers and cambolbro
 */
public class FeatureGenerationUtils 
{
	
	//-------------------------------------------------------------------------
	
	private FeatureGenerationUtils()
	{
		// should not be used
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Generates walks that are likely to correspond to the given direction choice,
	 * with optional restrictions imposed by Piece facing (if facing != null).
	 * 
	 * The two "out" lists should be empty, and will be populated by this method.
	 * 
	 * In possibly ambiguous cases, this method will generate relatively "safe",
	 * highly general features that will almost surely cover the real legal moves,
	 * as well as some more specific features that are likely to correspond more
	 * closely with movement rules, but also may in some games fail to capture
	 * all legal moves.
	 * 
	 * @param game
	 * @param dirnChoice
	 * @param facing
	 * @param outAllowedRotations Will be populated with lists of permitted rotations
	 * 	(null if no restriction on rotations)
	 * @param outWalks Will be populated with lists, each of which is a Walk
	 * 	(null if we want a feature with unrestricted from-pos)
	 */
	public static void generateWalksForDirnChoice
	(
		final Game game,
		final RelativeDirection dirnChoice,
		final DirectionFacing facing,
		final List<TFloatArrayList> outAllowedRotations,
		final List<TFloatArrayList> outWalks
	)
	{
//		final Tiling tiling = game.board().tiling();
//		final DirectionType[] supportedDirectionTypes = tiling.getSupportedDirectionTypes();
//		final DirectionType[] supportedOrthDirectionTypes = tiling.getSupportedOrthogonalDirectionTypes();
//		
//		final boolean allDirsOrth = Arrays.equals(supportedDirectionTypes, supportedOrthDirectionTypes);
//		
//		final int[] directionIndices;
//		if (facing != null)
//		{
//			directionIndices = tiling.getChosenDirectionIndices(facing, dirnChoice);
//		}
//		else
//		{
//			directionIndices = tiling.getChosenDirectionIndices(facing, dirnChoice);
//		}
//		
//		boolean allDirectionsAllowed = true;
//		for (final DirectionType dirType : supportedDirectionTypes)
//		{
//			boolean foundIt = false;
//			
//			for (int i = 0; i < directionIndices.length; ++i)
//			{
//				if (directionIndices[i] == dirType.index())
//				{
//					foundIt = true;
//					break;
//				}
//			}
//			
//			if (!foundIt)
//			{
//				allDirectionsAllowed = false;
//				break;
//			}
//		}
//		
//		if (allDirsOrth)
//		{
//			// this means that we're in a situation where all connections are orthogonal
//			// for example, this could be a hexagonal tiling
//			if (allDirectionsAllowed)
//			{
//				// easiest case; all we need is a single-step walk, all rotations allowed
//				outAllowedRotations.add(null);
//				outWalks.add(TFloatArrayList.wrap(0.f));
//			}
//			else
//			{
//				// difficult case; for now, we'll just separately generate a walk without any
//				// allowed rotations per direction index
//				//
//				// later on we probably could handle some more special cases with some support
//				// for rotations, if that turns out to be useful
//				System.err.println(
//						"Note: FeatureGenerationutils.generateWalksForDirnChoice() "
//						+ "returning no-rot walks!");
//				
//				for (final int directionIndex : directionIndices)
//				{
//					int orthDirTypeIdx = -1;
//					
//					for (int i = 0; i < supportedOrthDirectionTypes.length; ++i)
//					{
//						if (supportedOrthDirectionTypes[i].index() == directionIndex)
//						{
//							orthDirTypeIdx = i;
//							break;
//						}
//					}
//					
//					if (orthDirTypeIdx == -1)
//					{
//						System.err.println(
//								"Error in FeatureGenerationutils.generateWalksForDirnChoice(): "
//								+ "orthDirTypeIdx == -1");
//					}
//					else
//					{
//						outAllowedRotations.add(TFloatArrayList.wrap(0.f));
//						outWalks.add(TFloatArrayList.wrap(
//								(float) orthDirTypeIdx / (float) supportedOrthDirectionTypes.length));
//					}
//				}
//			}
//		}
//		else
//		{
//			// this means that we're in a situation where some connections are not orthogonal
//			// for example, this could be a square tiling / chessboard, where diagonals exist
//			//
//			// in this case, we'll have to start out with some a general feature just to
//			// make sure we don't miss out on any legal moves in strange boards
//			outAllowedRotations.add(null);
//			outWalks.add(null);
//			
//			// try to generate good orthogonal-move features if necessary
//			boolean orthDirAllowed = false;
//			
//			for (int i = 0; i < directionIndices.length; ++i)
//			{
//				boolean foundIt = false;
//				
//				for (final DirectionType dirType : supportedOrthDirectionTypes)
//				{
//					if (dirType.index() == directionIndices[i])
//					{
//						foundIt = true;
//						break;
//					}
//				}
//				
//				if (foundIt)
//				{
//					orthDirAllowed = true;
//					break;
//				}
//			}
//			
//			if (orthDirAllowed)
//			{
//				outAllowedRotations.add(null);
//				outWalks.add(TFloatArrayList.wrap(0.f));
//			}
//			
//			// try to generate good diagonal-move features if necessary
//			boolean nonOrthDirAllowed = false;
//			
//			for (int i = 0; i < directionIndices.length; ++i)
//			{
//				boolean foundIt = false;
//				
//				for (final DirectionType dirType : supportedOrthDirectionTypes)
//				{
//					if (dirType.index() == directionIndices[i])
//					{
//						foundIt = true;
//						break;
//					}
//				}
//				
//				if (!foundIt)
//				{
//					nonOrthDirAllowed = true;
//					break;
//				}
//			}
//			
//			// we'll create all-rotations-allowed walks of {0.f, 0.25}.
//			// these should specifically cover diagonal movements in many games
//			// (e.g. games with square tilings). In some strange boards they may
//			// fail, but then we still have the more general walks above as back-up.
//			if (nonOrthDirAllowed)
//			{
//				outAllowedRotations.add(null);
//				outWalks.add(TFloatArrayList.wrap(0.f, 0.25f));
//			}
//		}
	}
	
	/**
	 * @param game
	 * @param context
	 * @param elementType
	 * @param site
	 * @param itemIndex
	 * @return Whether or not the given elementType is applicable to the given site in the given context
	 */
	public static boolean testElementTypeInState
	(
		final Game game, final Context context, final ElementType elementType, 
		final int site, final int itemIndex
	)
	{
//		if (site == -1)
//		{
//			return elementType == ElementType.Off;
//		}
//		else
//		{
//			final ContainerState cs = context.trial().state().containerStates()[0];
//			
//			if (elementType == ElementType.Empty) 
//			{
//				return cs.whoCell(site) == 0;
//			}
//			else if (elementType == ElementType.Friend) 
//			{
//				return cs.whoCell(site) == context.trial().state().mover();
//			}
//			else if (elementType == ElementType.Enemy) 
//			{
//				return cs.whoCell(site) != context.trial().state().mover() && cs.whoCell(site) != 0;
//			}
//			else if (elementType == ElementType.Off)
//			{
//				return false;
//			}
//			else if (elementType == ElementType.Any) 
//			{
//				return true;
//			}
//			else if (elementType == ElementType.P1) 
//			{
//				return cs.whoCell(site) == 1;
//			}
//			else if (elementType == ElementType.P2) 
//			{
//				return cs.whoCell(site) == 2;
//			}
//			else if (elementType == ElementType.Item) 
//			{
//				return cs.whatCell(site) == itemIndex;
//			}
//			else if (elementType == ElementType.IsPos) 
//			{
//				return site == itemIndex;
//			}
//			else
//			{
//				System.err.println(
//						"FeatureGenerationUtils.testElementTypeInState cannot handle: " + elementType);
//				return false;
//			}
//		}
		
		return false;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param game
	 * @return A set of all feature element types that may be useful in the given game
	 */
	public static EnumSet<ElementType> usefulElementTypes(final Game game)
	{
		final EnumSet<ElementType> elementTypes = EnumSet.of(ElementType.Empty, ElementType.Friend, ElementType.Off);
		
		if (game.players().count() > 1)
		{
			elementTypes.add(ElementType.Enemy);
		}
		
		final Component[] components = game.equipment().components();
		
//		if (components.length > 1)		// >1 instead of >=1 because we have dummy entry of null at index 0
//		{
//			elementTypes.add(ElementType.LineOfSightOrth);
//			elementTypes.add(ElementType.LineOfSightDiag);
//		}
		
		final int[] componentsPerPlayer = new int[game.players().count() + 1];
		
		for (final Component component : components)
		{
			if (component != null && component.owner() <= game.players().count())
			{
				componentsPerPlayer[component.owner()]++;
			}
		}
		
		if (componentsPerPlayer[0] > 1)
		{
			elementTypes.add(ElementType.Item);
		}
		else
		{
			for (int i = 1; i < componentsPerPlayer.length; ++i)
			{
				if (componentsPerPlayer[i] > 1)
				{
					elementTypes.add(ElementType.Item);
				}
			}
		}
		
		final TIntArrayList connectivities = game.board().topology().trueOrthoConnectivities(game);
		if (connectivities.size() > 1)
		{
			// We have different graph elements with different connectivity numbers
			elementTypes.add(ElementType.Connectivity);
		}
		
		if (game.distancesToRegions() != null)
		{
			final Regions[] regions = game.equipment().regions();
		
			if (regions.length > 0)
			{
				for (int i = 0; i < regions.length; ++i)
				{
					if (game.distancesToRegions()[i] != null)
					{
						// We have at least one region with meaningful distances, so RegionProximity is relevant
						elementTypes.add(ElementType.RegionProximity);
						break;
					}
				}
			}
		}
		
		return elementTypes;
	}
	
	//-------------------------------------------------------------------------

}
