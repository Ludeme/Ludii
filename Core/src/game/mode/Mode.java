package game.mode;

import java.io.Serializable;
import java.util.BitSet;

import game.Game;
import game.types.play.ModeType;
import other.BaseLudeme;
import other.model.AlternatingMove;
import other.model.Model;
import other.model.SimulationMove;
import other.model.SimultaneousMove;
import other.playout.Playout;

/**
 * Describes the mode of play.
 * 
 * @author cambolbro and Eric.Piette
 */
public final class Mode extends BaseLudeme implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** Control type. */
	protected ModeType mode;

	// Custom playout for certain game types
	protected Playout playout;

	//-------------------------------------------------------------------------

	/**
	 * @param mode The mode of the game.
	 * 
	 * @example (mode Simultaneous)
	 */
	public Mode
	(
		final ModeType mode
	)
	{
		this.mode = mode;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Control type.
	 */
	public ModeType mode()
	{
		return mode;
	}

	/**
	 * To set the mode of the game.
	 * @param modeType
	 */
	public void setMode(ModeType modeType)
	{
		this.mode = modeType;
	}

	/**
	 * @return Playout implementation
	 */
	public Playout playout()
	{
		return playout;
	}
	
	/**
	 * Set playout implementation to use
	 * @param newPlayout
	 */
	public void setPlayout(final Playout newPlayout)
	{
		playout = newPlayout;
	}
	
	/**
	 * @return A newly-created model for this mode
	 */
	public Model createModel()
	{
		final Model model;
		
		switch (mode)
		{
		case Alternating:
			model = new AlternatingMove();
			break;
		case Simultaneous:
			model = new SimultaneousMove();
			break;
		case Simulation:
			model = new SimulationMove();
			break;
		default:
			model = null;
			break;
		}
		
		return model;
	}

	@Override
	public BitSet concepts(final Game game)
	{
//		switch (mode)
//		{
//		case Alternating:
//			return GameConcept.Alternating;
//		case Simultaneous:
//			return GameConcept.Simultaneous;
//		case Simulation:
//			return GameConcept.Simulation;
//		default:
//			return GameConcept.Alternating;
//		}
		return new BitSet();
	}
}
