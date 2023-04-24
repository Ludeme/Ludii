package game.rules;

import java.io.Serializable;

import annotations.Name;
import annotations.Opt;
import game.Game;
import game.rules.end.End;
import game.rules.meta.Meta;
import game.rules.phase.Phase;
import game.rules.play.Play;
import game.rules.start.Start;
import game.types.play.RoleType;
import other.BaseLudeme;

/**
 * Sets the game's rules.
 * 
 * @author cambolbro and Eric.Piette
 */
public final class Rules extends BaseLudeme implements Serializable
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** Metarules defined before play that supersede all other rules. */
	private final Meta metarules;

	/** Starting instructions. */
	private final Start start;

	/** Phases of the game. */
	private final Phase[] phases;
	
	/** Global game end (not coupled to phases, does not require any player to be active) */
	private End end;

	//-------------------------------------------------------------------------

	/**
	 * For defining the rules with start, play and end.
	 * 
	 * @param meta  Metarules defined before play that supersede all other rules.
	 * @param start Rules defining the starting position.
	 * @param play  Rules of play.
	 * @param end   Ending rules.
	 * 
	 * @example (rules (play (move Add (to (sites Empty)))) (end (if (is Line 3)
	 *          (result Mover Win))) )
	 */
	public Rules
	(
		@Opt final Meta  meta,
		@Opt final Start start,
			 final Play  play,
			 final End 	 end
	)
	{
		metarules = meta;
		this.start     = start;
		phases    = new Phase[] {new Phase("Default Phase", RoleType.Shared, null, play, null, null, null)};
		this.end       = end;
	}
	
	/**
	 * For defining the rules with some phases.
	 * 
	 * @param meta   Metarules defined before play that supersede all other rules.
	 * @param start  The starting rules.
	 * @param play   The playing rules shared between each phase.
	 * @param phases The phases of the game.
	 * @param end    The ending rules shared between each phase.
	 * 
	 * @example (rules (start (place "Ball" "Hand" count:3))
	 * 
	 *          phases:{ (phase "Placement" (play (fromTo (from (handSite Mover))
	 *          (to (sites Empty))) ) (nextPhase ("HandEmpty" P2) "Movement") )
	 * 
	 *          (phase "Movement" (play (forEach Piece)) ) } (end (if (is Line 3)
	 *          (result Mover Win))) )
	 */
	public Rules
	(
		@Opt  final Meta    meta,
		@Opt  final Start   start,
		@Opt  final Play    play,
		@Name final Phase[] phases,
		@Opt  final End     end
	)
	{
		metarules = meta;
		this.start     = start;
		this.phases    = phases;
		
		for (final Phase phase : phases)
		{
			if (phase.play() == null)
				phase.setPlay(play);
			else if (play != null)
				phase.setPlay
				(
					new Play
					(
								new game.rules.play.moves.nonDecision.operators.logical.Or(phase.play().moves(), play.moves(),
										null)
					)
				);
		} 
		
		this.end = end;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game) 
	{
		String text = "";

		if(start != null) 
		{
			String startRules = "";
			
			for(int i = 0; i < start.rules().length; i++) 
			{
				final String rule = start.rules()[i].toEnglish(game);
				
				if(!rule.isEmpty())
					startRules += "\n     " + rule.substring(0, 1).toUpperCase() + rule.substring(1);
			}
			
			if(!startRules.isEmpty())
				text += "Setup:" + startRules + ".";
		}

		String phaseRules = "";
		for (final Phase phase : phases) 
		{
			final String rule = phase.play().toEnglish(game);
			
			if(!rule.isEmpty())
				phaseRules += (phaseRules.isEmpty() ? "" : " ") + rule;
		}
		
		if(!phaseRules.isEmpty())
			text += (text.isEmpty() ? "" : "\n") + "Rules: \n     " + phaseRules.substring(0, 1).toUpperCase() + phaseRules.substring(1) + ".";

		if(end != null) 
		{
			String endRules = "";
			
			for (int i = 0; i < end.endRules().length; i++) 
			{
				final String rule = end.endRules()[i].toEnglish(game);
				
				if(!rule.isEmpty())
					endRules += (endRules.isEmpty() ? "" : " ") + rule;
			}
			
			if(!endRules.isEmpty())
				text += (text.isEmpty() ? "" : "\n") + "Aim: \n     " + endRules.substring(0, 1).toUpperCase() + endRules.substring(1) + ".";
		}
		
		return text;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Meta instructions.
	 */
	public Meta meta()
	{
		return metarules;
	}

	/**
	 * @return Starting instructions.
	 */
	public Start start()
	{
		return start;
	}

	/**
	 * @return Phases of the game.
	 */
	public Phase[] phases()
	{
		return phases;
	}
	
	/**
	 * @return End rules of the game
	 */
	public End end()
	{
		return end;
	}
	
	/**
	 * To set the ending rules.
	 * 
	 * @param e
	 */
	public void setEnd(final End e)
	{
		end = e;
	}
}
