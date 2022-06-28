package other;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import game.Game;
import main.collections.StringPair;

/**
 * A game loader object that can be useful for situations with lots of multi-threading
 * and partial overlap in games used between threads.
 * 
 * This game loader:
 * 	1) Blocks; will only compile and return one game at a time
 * 	2) Caches compiled game objects using weak references
 * 
 * If multiple threads want to compile and use the same Game object simultaneously,
 * they will only actually compile once and share the same object. This can save a
 * lot of memory. However, when no thread uses a Game object anymore, it will not
 * remain stuck in cache
 * 
 * @author Dennis Soemers
 */
public class WeaklyCachingGameLoader 
{
	
	//-------------------------------------------------------------------------
	
	/** Singleton instance */
	public static final WeaklyCachingGameLoader SINGLETON = new WeaklyCachingGameLoader();
	
	/** Our cache of weak references to compiled game objects */
	private final Map<StringPair, WeakReference<Game>> gameCache = new HashMap<StringPair, WeakReference<Game>>();
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor; private because we want singleton
	 */
	private WeaklyCachingGameLoader()
	{
		// Do nothing
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param gameName
	 * @param rulesetName
	 * @return Game object for given game name and ruleset name
	 */
	public synchronized Game loadGameFromName(final String gameName, final String rulesetName)
	{
		Game returnGame = null;
		final StringPair key = new StringPair(gameName, rulesetName != null ? rulesetName : "");
		final WeakReference<Game> gameRef = gameCache.get(key);
		
		if (gameRef == null)
		{
			returnGame = GameLoader.loadGameFromName(gameName, rulesetName);
			gameCache.put(key, new WeakReference<Game>(returnGame));
		}
		else
		{
			returnGame = gameRef.get();
			
			if (returnGame == null)
			{
				returnGame = GameLoader.loadGameFromName(gameName, rulesetName);
				gameCache.put(key, new WeakReference<Game>(returnGame));
			}
		}
		
		return returnGame;
	}
	
	//-------------------------------------------------------------------------

}
