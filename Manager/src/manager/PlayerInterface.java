package manager;

import java.util.List;

import org.json.JSONObject;

import other.context.Context;
import other.move.Move;

/**
 * Interface for specifying functions within the PlayerApp, which can be called from within the Manager project.
 * 
 * @author Matthew.Stephenson
 */
public interface PlayerInterface
{
	JSONObject getNameFromJar();
	JSONObject getNameFromJson();
	JSONObject getNameFromAiDef();
	void loadGameFromName(final String name, final List<String> options, final boolean debug);
	void addTextToStatusPanel(final String text);
	void addTextToAnalysisPanel(final String text);
	void selectAnalysisTab();
	void repaint();
	void reportForfeit(int playerForfeitNumber);
	void reportTimeout(int playerForfeitNumber);
	void reportDrawAgreed();
	void updateFrameTitle(boolean alsoUpdateMenu);
	void updateTabs(Context context);
	void restartGame();
	void repaintTimerForPlayer(int playerId);
	void setTemporaryMessage(final String text);
	void refreshNetworkDialog();
	void postMoveUpdates(Move move, boolean noAnimation);
}
