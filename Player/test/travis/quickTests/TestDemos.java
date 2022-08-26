package travis.quickTests;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

import game.Game;
import game.rules.play.moves.Moves;
import game.types.play.ModeType;
import main.FileHandling;
import manager.utils.game_logs.MatchRecord;
import other.AI;
import other.GameLoader;
import other.action.Action;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;
import utils.AIFactory;

/**
 * Tests that all the games, AIs and trials specified in demos are still valid.
 *
 * @author Dennis Soemers
 */
public class TestDemos
{
	
	@Test
	public static void testDemos()
	{
		final String[] allDemos = FileHandling.getResourceListing(TestDemos.class, "demos/", ".json");
		
		for (String demo : allDemos)
		{
			if (!demo.endsWith(".json"))
				continue;

			demo = demo.replaceAll(Pattern.quote("\\"), "/");

			if (demo.contains("/demos/"))
				demo = demo.substring(demo.indexOf("/demos/"));

			if (!demo.startsWith("/"))
				demo = "/" + demo;

			if (!demo.startsWith("/demos"))
				demo = "/demos" + demo;
			
			System.out.println("Testing demo: " + demo + "...");

			try (final InputStream inputStream = TestDemos.class.getResourceAsStream(demo))
			{
				final JSONObject json = new JSONObject(new JSONTokener(inputStream));
				final JSONObject jsonDemo = json.getJSONObject("Demo");
				
				final String gameName = jsonDemo.getString("Game");
				final List<String> gameOptions = new ArrayList<>();
		
				final JSONArray optionsArray = jsonDemo.optJSONArray("Options");
				if (optionsArray != null)
					for (final Object object : optionsArray)
						gameOptions.add((String) object);
				
				// Make sure we can load the game
				final Game game = GameLoader.loadGameFromName(gameName, gameOptions);
				assert(game != null);
				
				for (int p = 1; p <= game.players().count(); ++p)
				{
					final JSONObject jsonPlayer = jsonDemo.optJSONObject("Player " + p);
					if (jsonPlayer != null)
					{
						if (jsonPlayer.has("AI"))
						{
							// Make sure we can load the AI and that it supports this game
							final AI ai = AIFactory.fromJson(jsonPlayer);
							assert (ai != null);
							assert (ai.supportsGame(game));
						}
					}
				}
				
				if (jsonDemo.has("Trial"))
				{
					// Make sure that we can load and execute this trial
					final String trialFile = jsonDemo.getString("Trial").replaceAll(Pattern.quote("\\"), "/");
		
					try 
					(
						final InputStreamReader reader = 
							new InputStreamReader(TestDemos.class.getResourceAsStream(trialFile));		
					)
					{
						final MatchRecord loadedRecord = 
								MatchRecord.loadMatchRecordFromInputStream
								(
									reader, 
									game
								);
						final Trial loadedTrial = loadedRecord.trial();
						final List<Move> loadedMoves = loadedTrial.generateCompleteMovesList();
					
						final Trial trial = new Trial(game);
						final Context context = new Context(game, trial);
						context.rng().restoreState(loadedRecord.rngState());
						
						game.start(context);
						
						int moveIdx = 0;
						
						while (moveIdx < trial.numInitialPlacementMoves())
						{
							assert(loadedMoves.get(moveIdx).equals(trial.getMove(moveIdx)));
							++moveIdx;
						}
						
						while (moveIdx < loadedMoves.size())
						{
							while (moveIdx < trial.numMoves())
							{
								// looks like some actions were auto-applied (e.g. in ByScore End condition)
								// so we just check if they're equal, without applying them again from loaded file
								assert
									(loadedMoves.get(moveIdx).getActionsWithConsequences(context).equals(trial.getMove(moveIdx).getActionsWithConsequences(context))) 
									: 
									(
										"Loaded Move Actions = " + loadedMoves.get(moveIdx).getActionsWithConsequences(context) + 
										", trial actions = " + trial.getMove(moveIdx).getActionsWithConsequences(context)
									);
								++moveIdx;
							}
							
							if (moveIdx == loadedMoves.size())
								break;
							
							assert(!trial.over());
							
							final Moves legalMoves = game.moves(context);
							final List<List<Action>> legalMovesAllActions = new ArrayList<List<Action>>();
							for (final Move legalMove : legalMoves.moves())
							{
								legalMovesAllActions.add(legalMove.getActionsWithConsequences(context));
							}
							
							if (game.mode().mode() == ModeType.Alternating)
							{
								Move matchingMove = null;
								for (int i = 0; i < legalMovesAllActions.size(); ++i)
								{
									if (legalMovesAllActions.get(i).equals(loadedMoves.get(moveIdx).getActionsWithConsequences(context)))
									{
										matchingMove = legalMoves.moves().get(i);
										break;
									}
								}
								
								if (matchingMove == null)
								{
									if (loadedMoves.get(moveIdx).isPass() && legalMoves.moves().isEmpty())
										matchingMove = loadedMoves.get(moveIdx);
								}
								
								if (matchingMove == null)
								{
									for (int i = 0; i < legalMovesAllActions.size(); ++i)
									{
										System.out.println(legalMovesAllActions.get(i) + " does not match " + loadedMoves.get(moveIdx).getActionsWithConsequences(context));
									}
									
									fail();
								}
								
								game.apply(context, matchingMove);
							}
							else
							{
								// simultaneous-move game
								// we expect each of the actions of the loaded move to be contained
								// in at least one of the legal moves							
								for (final Action subAction : loadedMoves.get(moveIdx).actions())
								{
									boolean foundMatch = false;
									
									for (int i = 0; i < legalMovesAllActions.size(); ++i)
										if (legalMovesAllActions.get(i).contains(subAction))
										{
											foundMatch = true;
											break;
										}
									
									if (!foundMatch)
									{
										System.out.println("Found no matching subAction!");
										System.out.println("subAction = " + subAction);
										for (int i = 0; i < legalMovesAllActions.size(); ++i)
											System.out.println("Legal move = " + legalMovesAllActions.get(i));
										fail();
									}
								}
								game.apply(context, loadedMoves.get(moveIdx));
							}
							++moveIdx;
						}
						
						if (trial.status() == null)
							assert(loadedTrial.status() == null);
						else
							assert(trial.status().winner() == loadedTrial.status().winner());
						
						assert(Arrays.equals(trial.ranking(), loadedTrial.ranking()));
					}
					catch (final IOException e)
					{
						e.printStackTrace();
					}
				}
			}
			catch (final IOException e)
			{
				e.printStackTrace();
				fail();
			}
		}
	}
}
