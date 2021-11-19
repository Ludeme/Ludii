package game.rules.play.moves.nonDecision.effect;

import java.util.BitSet;

import annotations.Opt;
import game.Game;
import game.equipment.component.Component;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.types.state.GameType;
import main.Constants;
import other.action.Action;
import other.action.move.move.ActionMove;
import other.context.Context;
import other.move.Move;

/**
 * Plays any card in a player's hand to the board at their position.
 * 
 * @author Eric.Piette and cambolbro
 */
public final class PlayCard extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * @param then The moves applied after that move is applied.
	 * 
	 * @example (playCard)
	 */
	public PlayCard
	(
		@Opt final Then then
	) 
	{ 
		super(then);
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		final BaseMoves moves = new BaseMoves(super.then());

		for (int cid = 1; cid < context.containers().length; cid++)
		{
			if (context.containers()[cid].owner() == context.state().mover())
			{
				final int siteFrom = context.sitesFrom()[cid];
				for (int site = siteFrom; site < context.containers()[cid].numSites() + siteFrom; site++)
				{
					for (int level = 0; level < context.containerState(cid).sizeStackCell(site); level++)
					{
						final int to = context.state().mover() - 1;
						final Action actionMove = new ActionMove(SiteType.Cell, site, level, SiteType.Cell, to,
								Constants.UNDEFINED, Constants.UNDEFINED, Constants.UNDEFINED, Constants.OFF, false);
						if (isDecision())
							actionMove.setDecision(true);
						final Move move = new Move(actionMove);
						move.setFromNonDecision(site);
						move.setLevelMinNonDecision(level);
						move.setLevelMaxNonDecision(level);
						move.setToNonDecision(to);
						move.setMover(context.state().mover());
						moves.moves().add(move);
					}
				}
			}
		}

		if (then() != null)
			for (int j = 0; j < moves.moves().size(); j++)
				moves.moves().get(j).then().add(then().moves());

		// Store the Moves in the computed moves.
		for (int j = 0; j < moves.moves().size(); j++)
			moves.moves().get(j).setMovesLudeme(this);

		return moves;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = super.gameFlags(game) | GameType.Cell | GameType.Card | GameType.HiddenInfo
				| GameType.NotAllPass | GameType.UsesFromPositions;

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(super.concepts(game));
		if (then() != null)
			concepts.or(then().concepts(game));
		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());

		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());

		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;
		boolean gameHasCard = false;
		for (int i = 1; i < game.equipment().components().length; i++)
		{
			final Component component = game.equipment().components()[i];
			if (component.isCard())
			{
				gameHasCard = true;
				break;
			}

		}

		if (!gameHasCard)
		{
			game.addRequirementToReport("The ludeme (playCard ...) is used but the equipment has no cards.");
			missingRequirement = true;
		}
		missingRequirement |= super.missingRequirement(game);
		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public void preprocess(final Game game)
	{
		super.preprocess(game);
	}

}
