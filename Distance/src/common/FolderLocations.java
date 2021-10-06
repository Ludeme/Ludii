package common;

import java.io.File;

public class FolderLocations
{

	/**
	 * the folder related to distance measurements
	 */
	public final static File outputfolder = new File("../Distance/out/");
	public final static File outputTmpFolder = new File("../Distance/out/tmp/");
	public final static File resourceFolder = new File("../Distance/res/");
	public final static File resourceMetricsFolder = new File("../Distance/res/metrics/");
	public final static File resourceTmpFolder = new File("../Distance/res/tmp/");
	public final static File resourceTrialFolder = new File("../Distance/res/trials/");
	public final static File resourceAnnealingPosition = new File("../Distance/res/annealingPosition/");
	public final static File generationsTmpFolder = new File("../Generation/src/output/tmp/");
	public final static File generationsErrorLogsFolder = new File("../Generation/src/output/errorLogs/");
	public final static File generationsCosmeticErrorsFolder = new File("../Generation/src/output/cosmeticErrors/");
	public final static File boardFolder = new File("../Common/res/lud/board/");
	public final static File boardLineFolder = new File("../Common/res/lud/board/space/line/");
	public final static File otherWarFolder = new File("../Common/res/lud/board/war/other/");
	public final static File otherLeapingFolder = new File("../Common/res/lud/board/war/leaping/other/");
	public final static File ludFolder = new File("../Common/res/lud/");
	public final static File knightFolder = new File("../Common/res/lud/test/knightMoves");

}
