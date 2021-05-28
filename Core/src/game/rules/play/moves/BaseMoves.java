package game.rules.play.moves;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.rules.play.moves.nonDecision.effect.Then;
import other.context.Context;

/**
 * Placeholder object for creating default Moves objects without initialisation.
 * 
 * @author cambolbro
 */
@Hide
public class BaseMoves extends Moves
{
	/** */
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/**
	 * The base Moves.
	 * 
	 * @param then The subsequents of the moves.
	 */
	public BaseMoves
	(
		@Opt final Then then
	)
	{
		super(then);
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(Context context)
	{
		return this;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		return 0L;
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

	//-------------------------------------------------------------------------

	@Override
	public String toEnglish(final Game game)
	{
		return "<BaseMoves>";
	}
}
