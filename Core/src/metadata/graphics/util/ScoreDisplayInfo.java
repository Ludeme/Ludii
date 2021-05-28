package metadata.graphics.util;

import game.functions.ints.IntFunction;
import game.types.play.RoleType;

/**
 * Display information when drawing local states or values on a piece.
 * @author Matthew.Stephenson
 */
public class ScoreDisplayInfo {
	
	/** When the score should be shown. */
	private final WhenScoreType showScore;
	
	/** Player whose index is to be matched. */
	private final RoleType roleType;
	
	/** Replacement value to display instead of score. */
	private final IntFunction scoreReplacement;
	
	/** Extra string to append to the score displayed. */
	private final String scoreSuffix;
	
	/**
	 * Default constructor
	 */
	public ScoreDisplayInfo()
	{
		showScore = WhenScoreType.Always;
		roleType = RoleType.All;
		scoreReplacement = null;
		scoreSuffix = "";
	}
	
	/**
	 * @param showScore
	 * @param roleType
	 * @param scoreReplacement
	 * @param scoreSuffix
	 */
	public ScoreDisplayInfo(final WhenScoreType showScore, final RoleType roleType, final IntFunction scoreReplacement, final String scoreSuffix)
	{
		this.showScore = showScore;
		this.roleType = roleType;
		this.scoreReplacement = scoreReplacement;
		this.scoreSuffix = scoreSuffix;
	}

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

}
