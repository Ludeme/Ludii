package metadata.graphics.show.score;

import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import game.functions.ints.IntFunction;
import game.types.play.RoleType;
import metadata.graphics.GraphicsItem;
import metadata.graphics.util.WhenScoreType;

/**
 * Indicates whether the score should be shown only in certain situations.
 * 
 * @author Matthew.Stephenson
 * 
 * @remarks Should be used only in games where the score is used for end-game 
 *          calculations, e.g. for comparing number of pieces in Surakarta.
 */
@Hide
public class ShowScore implements GraphicsItem
{
	
	/** When the score should be shown. */
	private final WhenScoreType showScore;
	
	/** Player whose index is to be matched. */
	private final RoleType roleType;
	
	/** Replacement value to display instead of score. */
	private final IntFunction scoreReplacement;
	
	/** Extra string to append to the score displayed. */
	private final String scoreSuffix;
		
	//-------------------------------------------------------------------------

	/**
	 * @param showScore  		When the score should be shown [Always].
	 * @param roleType 			Player whose index is to be matched [All].
	 * @param scoreReplacement 	Replacement value to display instead of score.
	 * @param scoreSuffix  		Extra string to append to the score displayed [""].
	 */
	public ShowScore
	(
		@Opt final WhenScoreType showScore,
		@Opt final RoleType roleType, 
		@Opt final IntFunction scoreReplacement, 
		@Opt final String scoreSuffix
	)
	{
		this.showScore = (showScore == null) ? WhenScoreType.Always : showScore;
		this.roleType = (roleType == null) ? RoleType.All : roleType;
		this.scoreReplacement = scoreReplacement;
		this.scoreSuffix = (scoreSuffix == null) ? "" : scoreSuffix;
	}

	//-------------------------------------------------------------------------

	/**
	 * @return When the score should be shown.
	 */
	public WhenScoreType showScore()
	{
		return showScore;
	}
	
	/**
	 * @return Player whose index is to be matched.
	 */
	public RoleType roleType()
	{
		return roleType;
	}
	
	/**
	 * @return Replacement value to display instead of score.
	 */
	public IntFunction scoreReplacement()
	{
		return scoreReplacement;
	}
	
	/**
	 * @return Extra string to append to the score displayed.
	 */
	public String scoreSuffix()
	{
		return scoreSuffix;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		return concepts;
	}

	@Override
	public long gameFlags(final Game game)
	{
		final long gameFlags = 0l;
		return gameFlags;
	}

	@Override
	public boolean needRedraw()
	{
		return false;
	}

}
