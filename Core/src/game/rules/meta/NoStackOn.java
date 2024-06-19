package game.rules.meta;

import java.util.BitSet;
import java.util.List;

import game.Game;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.types.play.NoStackOnType;
import game.util.directions.AbsoluteDirection;
import game.util.graph.Radial;
import other.MetaRules;
import other.action.Action;
import other.action.ActionType;
import other.context.Context;
import other.move.Move;
import other.state.container.ContainerState;

/**
 * To filter some move moves in case some pieces can not be moved onto other fallen pieces.
 * 
 * @author Cedric.Antoine
 */
public class NoStackOn extends MetaRule
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * The pin type.
	 */
	final NoStackOnType type;

	/**
	 * @param type The type of pin.
	 * 
	 * @example (pin SupportMultiple)
	 */
	public NoStackOn(final NoStackOnType type)
	{
		this.type = type;
	}

	//-------------------------------------------------------------------------

	@Override
	public void eval(final Context context)
	{
		context.game().metaRules().setNoStackOnType(type);
	}

	/**
	 * @param context The context.
	 * @param legalMoves The original legal moves.
	 */
	public static void apply(final Context context, final Moves legalMoves)
	{
		final Game game = context.game();
		final MetaRules metaRules = game.metaRules();
		final NoStackOnType noStackOnType = metaRules.noStackOnType();
		if (noStackOnType != null)                      
        {                 
//			System.out.println("Applying not on fallen");
			final other.topology.Topology graph = context.topology();
			for (int indexMove = legalMoves.moves().size() - 1; indexMove >= 0; indexMove--)
			{
				final Move move = legalMoves.moves().get(indexMove);
				boolean forbiddenMove = false;
				for(final Action action : move.actions()) {
					if (action != null &&  action.actionType().equals(ActionType.Move))
					{			
						final int siteTo = action.to();
						final int siteFrom = action.from();
	//					System.out.println("From: " + action.from());
	//					System.out.println("To: " + action.to());
						
						
						
											
						final ContainerState cs = context.containerState(context.containerId()[siteTo]);
						if (cs.what(siteFrom, SiteType.Vertex) != 0 && !context.equipment().containers()[context.containerId()[siteTo]].isHand())  // modif ced
						{				
							final List<Radial> radials = graph.trajectories().radials(SiteType.Vertex, siteFrom)
									.distinctInDirection(AbsoluteDirection.Upward);
							
							for (final Radial radial : radials)
							{
	//							System.out.println("Radial: " + radial);
								for (int indexPath = 1; indexPath < radial.steps().length; indexPath++)
								{
									final int index = radial.steps()[indexPath].id();
	//								System.out.println("Index: " + index);
									if (siteTo == index) {
										forbiddenMove = true;
										break;
									}
									
								}	
								
								final List<Radial> oppositeRadials = radial.opposites();
								
								if (oppositeRadials != null) {
									for (final Radial oppositeRadial : oppositeRadials)
									{
	//									System.out.println("Radial: " + oppositeRadial);
										for (int indexPath = 1; indexPath < oppositeRadial.steps().length; indexPath++)
										{
											final int index = oppositeRadial.steps()[indexPath].id();
	//										System.out.println("Index: " + index);
											if (siteTo == index) {
												forbiddenMove = true;
												break;
											}
											
										}	
										
									}
								}		
							}									
						}
					}
				}
				if (forbiddenMove) {
	//				System.out.println("Removed");
	//				System.out.println("--------");
					legalMoves.moves().remove(indexMove);
				}
	//			System.out.println("NotRemoved");
	//			System.out.println("--------");
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

		if (!(obj instanceof NoStackOn))
			return false;

		return true;
	}
}