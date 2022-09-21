/*
 * Created on 18-mar-06
 *
 */
package umpire.local;

/**
 * Listener interface for a local umpire, notifies when state changes (so display can be updated, etc)
 * @author Nikola Novarlic
 *
 */
public interface ChessboardStateListener {

	public void chessboardStateChanged();

}
