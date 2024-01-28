package games;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import compiler.Compiler;
import game.Game;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import game.types.play.ModeType;
import gnu.trove.list.array.TIntArrayList;
import main.FileHandling;
import main.grammar.Description;
import manager.utils.game_logs.MatchRecord;
import other.action.Action;
import other.context.Context;
import other.location.Location;
import other.move.Move;
import other.state.container.ContainerState;
import other.state.owned.Owned;
import other.trial.Trial;
import other.state.State;

/**
 * A Unit Test to load Trials from the TravisTrials repository, and check if they
 * all still play out the same way in the current Ludii codebase.
 * 
 * @author Eric.Piette
 */
public class TestTrialsUndo
{
	/**
	 * The test to run
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	@Test
	public static void test() throws FileNotFoundException, IOException
	{
		final boolean stateComparaison = false;
		final File startFolder = new File("../Common/res/lud");
		final List<File> gameDirs = new ArrayList<File>();
		gameDirs.add(startFolder);

		final List<File> entries = new ArrayList<File>();

		// final String moreSpecificFolder = "../Common/res/lud/board/sow";
		final String moreSpecificFolder = "";
		
		for (int i = 0; i < gameDirs.size(); ++i)
		{
			final File gameDir = gameDirs.get(i);

			for (final File fileEntry : gameDir.listFiles())
			{
				if (fileEntry.isDirectory())
				{
					final String fileEntryPath = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");

					if (fileEntryPath.equals("../Common/res/lud/plex"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/wip"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/wishlist"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/reconstruction"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/WishlistDLP"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/test"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/puzzle/deduction"))
						continue; // skip deduction puzzles

					if (fileEntryPath.equals("../Common/res/lud/bad"))
						continue;

					if (fileEntryPath.equals("../Common/res/lud/bad_playout"))
						continue;

//					// We exclude that game from the tests because the legal
//					// moves are too slow to test.
//					if (fileEntryPath.contains("Residuelllllllll"))
//						continue;

						gameDirs.add(fileEntry);
				}
				else
				{
					final String fileEntryPath = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");
					if (moreSpecificFolder.equals("") || fileEntryPath.contains(moreSpecificFolder))
						entries.add(fileEntry);
				}
			}
		}
		
		boolean gameReached = false;
		final String gameToReached = "";
		final ArrayList<String> gamesToSkip = new ArrayList<String>();
		gamesToSkip.add("Galatjang");
		gamesToSkip.add("Seesaw Draughts");
		gamesToSkip.add("Sahkku");
		gamesToSkip.add("Kriegsspiel");

		final long startTime = System.currentTimeMillis();

		for (final File fileEntry : entries)
		{
			if (fileEntry.getPath().contains("")) 
			//if (fileEntry.getName().equals("Ludus Latrunculorum.lud"))
			{
				if (fileEntry.getName().contains(gameToReached) || gameToReached.length() == 0)
					gameReached = true;

				if (!gameReached)
					continue;

				boolean skip = false;
				for(String gameToSkip : gamesToSkip)
					if (!gameToSkip.equals("") && fileEntry.getName().contains(gameToSkip))
						{
						skip = true;
						break;
						}
				
				if(skip)
					continue;

				final String ludPath = fileEntry.getPath().replaceAll(Pattern.quote("\\"), "/");
				final String trialDirPath = ludPath
						.replaceFirst(Pattern.quote("/Common/res/"), Matcher.quoteReplacement("/../TravisTrials/"))
						.replaceFirst(Pattern.quote("/lud/"), Matcher.quoteReplacement("/random_trials/"))
						.replace(".lud", "");

				final File trialsDir = new File(trialDirPath);

				if (!trialsDir.exists())
				{
					System.err.println("WARNING: No directory of trials exists at: " + trialsDir.getAbsolutePath());
					continue;
				}

				final File[] trialFiles = trialsDir.listFiles();

				if (trialFiles.length == 0)
				{
					System.err.println("WARNING: No trial files exist in directory: " + trialsDir.getAbsolutePath());
					continue;
				}

				// Load the string from lud file
				String desc = "";
				try
				{
					desc = FileHandling.loadTextContentsFromFile(ludPath);
				}
				catch (final FileNotFoundException ex)
				{
					fail("Unable to open file '" + ludPath + "'");
				}
				catch (final IOException ex)
				{
					fail("Error reading file '" + ludPath + "'");
				}

				// Parse and compile the game
				final Game game = (Game)Compiler.compileTest(new Description(desc), false);
				if (game == null)
					fail("COMPILATION FAILED for the file : " + ludPath);

				if (game.hasSubgames() || game.isSimulationMoveGame())
					continue;

				for (final File trialFile : trialFiles)
				{
					System.out.println("Testing playing trial and undo move by move: " + trialFile);
					final MatchRecord loadedRecord = MatchRecord.loadMatchRecordFromTextFile(trialFile, game);
					final Trial loadedTrial = loadedRecord.trial();
					final List<Move> loadedMoves = loadedTrial.generateCompleteMovesList();

					final Context context = new Context(game, new Trial(game));
					context.rng().restoreState(loadedRecord.rngState());

					final Trial trial = context.trial();
					game.start(context);

					int moveIdx = 0;
					while (moveIdx < trial.numInitialPlacementMoves())
						++moveIdx;
					
					// Apply all the trial.
					if(game.isStochasticGame())
					{
						while (!trial.over())
						{
							final Move loadedMove = loadedMoves.get(moveIdx);
							final List<Action> loadedMoveAllActions = loadedMove.getActionsWithConsequences(context);
							final Moves legalMoves = context.game().moves(context);
							boolean legalMoveFound = false;
							for(Move move : legalMoves.moves())
							{
								if (move.from() == loadedMove.from() && move.to() == loadedMove.to())
								{
									if (move.getActionsWithConsequences(context).equals(loadedMoveAllActions))
									{
										game.apply(context, move);
										if(!trial.over())
											++moveIdx;
										legalMoveFound= true;
										break;
									}
								}
							}
							
							if(!legalMoveFound)
							{
								System.err.println("BUG no legal moves found");
								fail();
							}
						}
					}
					else 
					{
						while (!trial.over())
						{
							final Move loadedMove = loadedMoves.get(moveIdx);
							game.apply(context, loadedMove);
							if(!trial.over())
								++moveIdx;
						}
					}
					
					// -------------------------------------------TEST Undo all the trial. ---------------------------------------------------------------------------------------
					
					while (moveIdx > trial.numInitialPlacementMoves() && moveIdx > 0)
					{
						game.undo(context);
						
						// Apply the trial from the initial state to the specific state we want to compare.
						if(stateComparaison)
						{
							final Context contextToCompare = new Context(game, new Trial(game));
							contextToCompare.rng().restoreState(loadedRecord.rngState());
							final Trial trialToCompare = contextToCompare.trial();
							final State stateToCompare = contextToCompare.state();
							final State state = context.state();
							game.start(contextToCompare);
							
							int indexTrialToCompare = 0;
							while (indexTrialToCompare < trialToCompare.numInitialPlacementMoves())
								++indexTrialToCompare;
							if(game.isStochasticGame())
							{
								while(indexTrialToCompare < moveIdx)
								{
									final Move loadedMove = loadedMoves.get(indexTrialToCompare);
									final List<Action> loadedMoveAllActions = loadedMove.getActionsWithConsequences(contextToCompare);
									final Moves legalMoves = context.game().moves(contextToCompare);
									boolean legalMoveFound = false;
									for(Move move : legalMoves.moves())
									{
										if (move.from() == loadedMove.from() && move.to() == loadedMove.to())
										{
											if (move.getActionsWithConsequences(contextToCompare).equals(loadedMoveAllActions))
											{
												game.apply(contextToCompare, move);
												legalMoveFound= true;
												if(!trialToCompare.over())
													++indexTrialToCompare;
												break;
											}
										}
									}
									
									if(!legalMoveFound)
									{
										System.err.println("STATE COMPARISON BUG no legal moves found");
										fail();
									}
								}
							}
							else 
							{
								while (indexTrialToCompare < moveIdx)
								{
									final Move loadedMove = loadedMoves.get(indexTrialToCompare);
									game.apply(contextToCompare, loadedMove);
									if(!trialToCompare.over())
										++indexTrialToCompare;
								}
							}

							// Check the container states.
							for(int cid = 0; cid < context.state().containerStates().length; cid++)
							{
								final ContainerState cs = context.containerState(cid);
								final ContainerState csToCompare = contextToCompare.containerState(cid);
								for(SiteType type : SiteType.values())
								{
									if(cid == 0 || (cid != 0 && type.equals(SiteType.Cell)))
									{
										for(int index = context.sitesFrom()[cid] ; index < (context.sitesFrom()[cid] + game.equipment().containers()[cid].topology().getGraphElements(type).size()); index++)
										{
											if(cs.sizeStack(index, type) != csToCompare.sizeStack(index, type))
											{
												System.out.println("IN MOVE " + trial.numberRealMoves() +  " != stack size for " + type + " " + index);
												System.out.println("correct one is " + csToCompare.sizeStack(index, type));
												System.out.println("undo one is " + cs.sizeStack(index, type));
												fail();
											}
											
											for(int level = 0; level < cs.sizeStack(index, type); level++)
											{
												if(cs.what(index, level, type) != csToCompare.what(index, level, type))
												{
													System.out.println("IN MOVE " + trial.numberRealMoves() +  " " + type + " != What at  " + index + " level " + level);
													System.out.println("correct one is " + csToCompare.what(index, level, type));
													System.out.println("undo one is " + cs.what(index, level, type));
													fail();
												}
												
												if(cs.who(index, level, type) != csToCompare.who(index, level, type))
												{
													System.out.println("IN MOVE " + trial.numberRealMoves() +  " " +type + " != Who at  " + index + " level " + level);
													System.out.println("correct one is " + csToCompare.who(index, level, type));
													System.out.println("undo one is " + cs.who(index, level, type));
													fail();
												}
												
												if(cs.state(index, level, type) != csToCompare.state(index, level, type))
												{
													System.out.println("IN MOVE " + trial.numberRealMoves() +  " " + type + " != State at  " + index + " level " + level);
													System.out.println("correct one is " + csToCompare.state(index, level, type));
													System.out.println("undo one is " + cs.state(index, level, type));
													fail();
												}
												
												if(cs.rotation(index, level, type) != csToCompare.rotation(index, level, type))
												{
													System.out.println(type + " != Rotation at  " + index + " level " + level);
													fail();
												}
												
												if(cs.value(index, level, type) != csToCompare.value(index, level, type))
												{
													System.out.println("IN MOVE " + trial.numberRealMoves() +  " " + type + " != Value at  " + index + " level " + level);
													System.out.println("correct one is " + csToCompare.value(index, level, type));
													System.out.println("undo one is " + cs.value(index, level, type));
													fail();
												}
												
												for(int pid = 1; pid < game.players().size() ; pid++)
												{
													if(cs.isHidden(pid, index, level, type) != csToCompare.isHidden(pid, index, level, type))
													{
														System.out.println("IN MOVE " + trial.numberRealMoves() + " " + type + " != isHidden at  " + index + " level " + level + " player " + pid);
														System.out.println("correct one is " + csToCompare.isHidden(pid, index, level, type));
														System.out.println("undo one is " + cs.isHidden(pid, index, level, type));
														fail();
													}
													
													if(cs.isHiddenWhat(pid, index, level, type) != csToCompare.isHiddenWhat(pid, index, level, type))
													{
														System.out.println(type + " != isHiddenWhat at  " + index + " level " + level + " player " + pid);
														fail();
													}
													
													if(cs.isHiddenWho(pid, index, level, type) != csToCompare.isHiddenWho(pid, index, level, type))
													{
														System.out.println(type + " != isHiddenWho at  " + index + " level " + level + " player " + pid);
														fail();
													}
													
													if(cs.isHiddenValue(pid, index, level, type) != csToCompare.isHiddenValue(pid, index, level, type))
													{
														System.out.println(type + " != isHiddenValue at  " + index + " level " + level + " player " + pid);
														fail();
													}
													
													if(cs.isHiddenState(pid, index, level, type) != csToCompare.isHiddenState(pid, index, level, type))
													{
														System.out.println(type + " != isHiddenState at  " + index + " level " + level + " player " + pid);
														fail();
													}
													
													if(cs.isHiddenRotation(pid, index, level, type) != csToCompare.isHiddenRotation(pid, index, level, type))
													{
														System.out.println(type + " != isHiddenRotation at  " + index + " level " + level + " player " + pid);
														fail();
													}
													
													if(cs.isHiddenCount(pid, index, level, type) != csToCompare.isHiddenCount(pid, index, level, type))
													{
														System.out.println(type + " != isHiddenCount at  " + index + " level " + level + " player " + pid);
														fail();
													}
												}
											}
											
											if(!game.isStacking())
											{
												if(cs.count(index, type) != csToCompare.count(index, type))
												{
													System.out.println("IN MOVE " + trial.numberRealMoves() + " " + type + " != Count at  " + index);
													System.out.println("correct one is " + csToCompare.count(index, type));
													System.out.println("undo one is " + cs.count(index, type));
													fail();
												}
											}
											
											if(cs.isEmpty(index, type) != csToCompare.isEmpty(index, type))
											{
												System.out.println("IN MOVE " + trial.numberRealMoves() + " " + type + " != Empty at  " + index);
												System.out.println("correct one is " + csToCompare.isEmpty(index, type));
												System.out.println("undo one is " + cs.isEmpty(index, type));
												fail();
											}
											
											if(game.isBoardless() && type == game.board().defaultSite())
												if(cs.isPlayable(index) != csToCompare.isPlayable(index))
												{
													System.out.println(type + " != Playable at  " + index);
													fail();
												}
										}
									}
								}
							}
							
							// Check the trial data.
							if(trial.numTurns() != trialToCompare.numTurns())
							{
								System.out.println("!= num of turns");
								fail();
							}
							if(trial.numForcedPasses() != trialToCompare.numForcedPasses())
							{
								System.out.println("!= num forced passes");
								fail();
							}
							if(!trial.previousState().equals(trialToCompare.previousState()))
							{
								System.out.println("IN MOVE " + trial.numberRealMoves() +   " != previous states");
								System.out.println("correct one is " + trialToCompare.previousState());
								System.out.println("undo one is " + trial.previousState());
								fail();
							}
							if(!trial.previousStateWithinATurn().equals(trialToCompare.previousStateWithinATurn()))
							{
								System.out.println("IN MOVE " + trial.numberRealMoves() +   " != previous states within turn");
								System.out.println("correct one is " + trialToCompare.previousStateWithinATurn());
								System.out.println("undo one is " + trial.previousStateWithinATurn());
								fail();
							}
							
							// Check the state data.
							if(state.mover() != stateToCompare.mover())
							{
								System.out.println("!= mover");
								fail();
							}
							
							if(state.prev() != stateToCompare.prev())
							{
								System.out.println("!= prev");
								fail();
							}
							
							if(state.next() != stateToCompare.next())
							{
								System.out.println("!= next");
								fail();
							}
							
							if(state.counter() != stateToCompare.counter())
							{
								System.out.println("!= counter");
								fail();
							}
							
							if(state.temp() != stateToCompare.temp())
							{
								System.out.println("!= tempValue");
								fail();
							}
							
							if(state.temp() != stateToCompare.temp())
							{
								System.out.println("!= tempValue");
								fail();
							}
							
							if(state.pendingValues() != null)
							{
								if(state.pendingValues().size() != stateToCompare.pendingValues().size())
								{
									System.out.println("IN MOVE " + trial.numberRealMoves() +  " != pendingValues");
									System.out.println("correct one is " + stateToCompare.pendingValues());
									System.out.println("undo one is " + state.pendingValues());
									fail();
								}
								else
								{
									final int[] pendingValuesUndo = state.pendingValues().toArray();
									final int[] pendingValuesToCompare = stateToCompare.pendingValues().toArray();
									
									for(int pendingValue : pendingValuesUndo)
									{
										boolean found = false;
										for(int pendingValueToCompare : pendingValuesToCompare)
										{
											if(pendingValue == pendingValueToCompare)
											{
												found = true;
												break;
											}
										}
										if(!found)
										{
											System.out.println("IN MOVE " + trial.numberRealMoves() +  " != pendingValues");
											System.out.println("correct one is " + stateToCompare.pendingValues());
											System.out.println("undo one is " + state.pendingValues());
											fail();
										}
									}
								}
							}
							
							for(int pid = 0; pid < game.players().size(); pid++)
							{
								if(state.amount(pid) !=  stateToCompare.amount(pid))
								{
									System.out.println("!= amount for player " + pid);
									fail();
								}
							}
							
							if(state.pot() != stateToCompare.pot())
							{
								System.out.println("!= money pot");
								fail();
							}
							
							for(int pid = 0; pid < game.players().size(); pid++)
							{
								if(state.currentPhase(pid) !=  stateToCompare.currentPhase(pid))
								{
									System.out.println("!= phase for player " + pid);
									fail();
								}
							}
							
							if(state.sumDice() != null)
							{
								for(int indexHandDice = 0; indexHandDice < state.sumDice().length; indexHandDice++)
									if(state.sumDice(indexHandDice) !=  stateToCompare.sumDice(indexHandDice))
									{
										System.out.println("!= sumDice for handDice " + indexHandDice);
										fail();
									}
							}
							
							if(state.currentDice() != null)
							{
								for(int indexHandDice = 0; indexHandDice < state.currentDice().length; indexHandDice++)
									for(int indexDie = 0; indexDie < state.currentDice()[indexHandDice].length; indexDie++)
									if(state.currentDice()[indexHandDice][indexDie] !=  stateToCompare.currentDice()[indexHandDice][indexDie])
									{
										System.out.println("IN MOVE " + trial.numberRealMoves() + " != currentdice for handDice " + indexHandDice + " die index " + indexDie);
										System.out.println("correct one is " + stateToCompare.currentDice()[indexHandDice][indexDie]);
										System.out.println("undo one is " + state.currentDice()[indexHandDice][indexDie]);
										fail();
									}
							}
							
							if(state.getValueMap() != null)
							{
								if(!state.getValueMap().equals(stateToCompare.getValueMap()))
								{
									System.out.println("IN MOVE " + trial.numberRealMoves() + " != value Map");
									System.out.println("correct one is " + stateToCompare.getValueMap());
									System.out.println("undo one is " + state.getValueMap());
									fail();
								}
							}
							
							if(state.isDiceAllEqual() != stateToCompare.isDiceAllEqual())
							{
								System.out.println("!= diceAllEqual");
								fail();
							}
							
							if(state.numTurnSamePlayer() != stateToCompare.numTurnSamePlayer())
							{
								System.out.println("!= numTurnSamePlayer");
								fail();
							}
							
							if(state.numTurn() != stateToCompare.numTurn())
							{
								System.out.println("!= numTurn");
								fail();
							}
							
							if(state.trumpSuit() != stateToCompare.trumpSuit())
							{
								System.out.println("!= trumpSuit");
								fail();
							}
							
							if(state.propositions() != null)
							{
								if(!state.propositions().equals(stateToCompare.propositions()))
								{
									System.out.println("!= propositions");
									fail();
								}
							}
							
							if(state.votes() != null)
							{
								if(state.votes().size() != stateToCompare.votes().size())
								{
									System.out.println("IN MOVE " + trial.numberRealMoves() + " != votes");
									System.out.println("correct one is " + stateToCompare.votes());
									System.out.println("undo one is " + state.votes());
									fail();
								}
								else
								{
									final int[] votesUndo = state.votes().toArray();
									final int[] votesToCompare = stateToCompare.votes().toArray();
									
									for(int vote : votesUndo)
									{
										boolean found = false;
										for(int voteToCompare : votesToCompare)
										{
											if(vote == voteToCompare)
											{
												found = true;
												break;
											}
										}
										if(!found)
										{
											System.out.println("IN MOVE " + trial.numberRealMoves() + " != votes");
											System.out.println("correct one is " + stateToCompare.votes());
											System.out.println("undo one is " + state.votes());
											fail();
										}
									}
								}
							}
							
							for(int pid = 1; pid < game.players().size(); pid++)
							{
								if(state.getValue(pid) !=  stateToCompare.getValue(pid))
								{
									System.out.println("!= value player " + pid);
									fail();
								}
							}
							
							if(state.isDecided() != stateToCompare.isDecided())
							{
								System.out.println("IN MOVE " + trial.numberRealMoves() + " != isDecided");
								System.out.println("correct one is " + stateToCompare.isDecided());
								System.out.println("undo one is " + state.isDecided());
								fail();
							}
							
							if(state.rememberingValues() != null)
							{
								if(!state.rememberingValues().equals(stateToCompare.rememberingValues()))
								{
									System.out.println("!= rememberingValues");
									fail();
								}
							}
							
							if(state.mapRememberingValues() != null)
							{
								if(!state.mapRememberingValues().equals(stateToCompare.mapRememberingValues()))
								{
									System.out.println("!= mapRememberingValues");
									fail();
								}
							}
							
							if(state.getNotes() != null)
							{
								if(!state.getNotes().equals(stateToCompare.getNotes()))
								{
									System.out.println("!= notes");
									fail();
								}
							}
							
							if(state.visited() != null)
							{
								if(!state.visited().equals(stateToCompare.visited()))
								{
									System.out.println("IN MOVE " + trial.numberRealMoves() +" != visited");
									System.out.println("correct one is " + stateToCompare.visited());
									System.out.println("undo one is " + state.visited());
									fail();
								}
							}
							
							if(state.sitesToRemove() != null)
							{
								if(!state.sitesToRemove().equals(stateToCompare.sitesToRemove()))
								{
									System.out.println("IN MOVE " + trial.numberRealMoves() + " != sitesToRemove");
									System.out.println("correct one is " + stateToCompare.sitesToRemove());
									System.out.println("undo one is " + state.sitesToRemove());
									fail();
								}
							}
							
							for(int pid = 1; pid < game.players().size(); pid++)
								if(state.getTeam(pid) !=  stateToCompare.getTeam(pid))
								{
									System.out.println("!= team player " + pid);
									fail();
								}
							
							if(state.remainingDominoes() != null)
							{
								if(!state.remainingDominoes().equals(stateToCompare.remainingDominoes()))
								{
									System.out.println("!= remainingDominoes");
									fail();
								}
							}
							
							if(state.numConsecutivesPasses() != stateToCompare.numConsecutivesPasses())
							{
								System.out.println("!= numConsecutivesPasses");
								fail();
							}
							
							if(state.storedState() != stateToCompare.storedState())
							{
								System.out.println("!= storedState");
								fail();
							}
							
							if(state.onTrackIndices() != null)
							{
								if(!state.onTrackIndices().equals(stateToCompare.onTrackIndices()))
								{
									System.out.println("IN MOVE " + trial.numberRealMoves() + " != onTrackIndices");
									for(int i = 0; i < state.onTrackIndices().onTrackIndices().length; i++)
									{
										System.out.println("What is " + i);
										System.out.println("correct one is " + stateToCompare.onTrackIndices().whats(i));
										System.out.println("undo one is " + state.onTrackIndices().whats(i));
									}
									
									fail();
								}
							}
							
							// Check the owned structure.
							for(int pid = 0; pid <= game.players().size(); pid++)
							{
								final Owned ownedUndo = state.owned();
								final TIntArrayList ownedSitesUndo = ownedUndo.sites(pid); 
								final List<? extends Location>[] ownedPositionsUndo = ownedUndo.positions(pid);

								final Owned ownedToCompare = stateToCompare.owned();
								final TIntArrayList ownedSitesToCompare = ownedToCompare.sites(pid);
								final List<? extends Location>[] ownedPositionsToCompare = ownedToCompare.positions(pid);
								
								if(ownedSitesToCompare.size() != ownedSitesUndo.size())
								{
									System.out.println("IN MOVE " + trial.numberRealMoves() + " != owned for pid = " + pid);
									
									System.out.println("correct one is");
									for(int i = 0; i < ownedPositionsToCompare.length ;i++)
										for(Location loc: ownedPositionsToCompare[i])
											System.out.println(loc.site() + " lvl = " + loc.level());
									
									System.out.println("undo one is ");
									for(int i = 0; i < ownedPositionsUndo.length ;i++)
										for(Location loc: ownedPositionsUndo[i])
											System.out.println(loc.site() + " lvl = " + loc.level());
									fail();
								}
								else
								{
									for(int i = 0; i < ownedPositionsUndo.length ;i++)
										for(Location loc: ownedPositionsUndo[i])
										{
											final int site = loc.site();
											final int level = loc.level();
											boolean found = false;
											for(int j = 0; j < ownedPositionsToCompare.length ;j++)
											{
												for(Location locToCompare: ownedPositionsToCompare[j])
												{
													final int siteToCompare = locToCompare.site();
													final int levelToCompare = locToCompare.level();
													if(site == siteToCompare && level == levelToCompare)
													{
														found = true;
														break;
													}
												}
												if(found)
													break;
											}
											
											if(!found)
											{
												System.out.println("IN MOVE " + trial.numberRealMoves() + " site " + site + " level = " + level +" should not be owned by " + pid);
												
												System.out.println("correct one is");
												for(int j = 0; j < ownedPositionsToCompare.length ;j++)
													for(Location locToCompare: ownedPositionsToCompare[j])
														System.out.println(locToCompare.site() + " lvl = " + locToCompare.level());
												
												fail();
											}
										}
									
//									for(int i = 0 ; i < ownedSitesUndo.size() ; i++)
//									{
//										final int site = ownedSitesUndo.get(i);
//										final int site = ownedSitesUndo.get(i);
//										if(!ownedSitesToCompare.contains(site))
//										{
//											System.out.println("IN MOVE " + trial.numberRealMoves() + " site " + site + " should not be owned by " + pid);
//										}
//									}
								}
							}
						}
						
						
						// When undo, never has to be over.
						if (trial.over())
						{
							System.out.println("Fail(): Testing undo trial: " + trialFile.getParent());
							System.out.println("Failed at trial file: " + trialFile);
							System.out.println("corrected moveIdx = " + (moveIdx - context.currentInstanceContext().trial().numInitialPlacementMoves()));
							System.out.println("moveIdx = " + moveIdx);
							System.out.println("Trial was not supposed to be over, but it is!");
							fail();
						}
						
						if(trial.numMoves() != moveIdx)
							System.out.println("Number of moves is wrong (currently = " + trial.numMoves()+") correct value should be " + moveIdx);
						assert(trial.numMoves() == moveIdx);
						
						final List<Action> loadedAllActions = loadedMoves.get(moveIdx-1).getActionsWithConsequences(context);
						final List<Action> trialMoveAllActions = trial.getMove(moveIdx-1).getActionsWithConsequences(context);
						assert (loadedAllActions.equals(trialMoveAllActions)) : 
						("Loaded Move Actions = " + loadedAllActions + ", trial actions = " + trialMoveAllActions);
						
						final Move loadedMove = loadedMoves.get(moveIdx);
						final List<Action> loadedMoveAllActions = loadedMove.getActionsWithConsequences(context);
						
						final Moves legalMoves = game.moves(context);
						
						if (loadedTrial.auxilTrialData() != null)
						{
							if (legalMoves.moves().size() != loadedTrial.auxilTrialData().legalMovesHistorySizes()
									.getQuick(moveIdx - trial.numInitialPlacementMoves()))
							{
								System.out.println("moveIdx = " + (moveIdx - trial.numInitialPlacementMoves()));
								System.out.println("legalMoves.moves().size() = " + legalMoves.moves().size());
								
								for(Move move : legalMoves.moves())
									System.out.println(move.getActionsWithConsequences(context));
								
								System.out.println(
										"loadedTrial.legalMovesHistorySizes().getQuick(moveIdx - trial.numInitPlace()) = "
												+ loadedTrial.auxilTrialData().legalMovesHistorySizes()
														.getQuick(moveIdx - trial.numInitialPlacementMoves()));
							}
	
							assert (legalMoves.moves().size() == loadedTrial.auxilTrialData().legalMovesHistorySizes()
									.getQuick(moveIdx - trial.numInitialPlacementMoves()));
						}
						
						if (game.mode().mode() == ModeType.Alternating)
						{
							Move matchingMove = null;
							for (final Move move : legalMoves.moves())
							{
								if (move.from() == loadedMove.from() && move.to() == loadedMove.to())
								{
									final List<Action> moveAllActions = move.getActionsWithConsequences(context);
									if(moveAllActions.size() == loadedMoveAllActions.size())
									{
										boolean moveFound = true;
										for(Action action:  moveAllActions)
										{
											if(!loadedMoveAllActions.contains(action))
											{
												moveFound = false;
												break;
											}
										}
										if(moveFound)
										{
											matchingMove = move;
											break;
										}
									}
								}
							}

							if (matchingMove == null)
							{
								if (loadedMove.isPass() && legalMoves.moves().isEmpty())
									matchingMove = loadedMove;
							}

							if (matchingMove == null)
							{
								System.out.println("moveIdx = " + (moveIdx - trial.numInitialPlacementMoves()));
								System.out.println("Loaded move = " + loadedMove.getActionsWithConsequences(context)
										+ " from is " + loadedMove.fromType() + " to is " + loadedMove.toType());

								for (final Move move : legalMoves.moves())
								{
									System.out.println("legal move = " + move.getActionsWithConsequences(context) + " move from is "
											+ move.fromType() + " to " + move.toType());
								}
							}

							assert (matchingMove != null);
						}

						moveIdx--;
					}
					
						// simultaneous-move game
//						else
//						{
//							// the full loaded move should be equal to one of the possible large combined moves				
//							final FastArrayList<Move> legal = legalMoves.moves();
//							
//							final int numPlayers = game.players().count();
//							@SuppressWarnings("unchecked")
//							final FastArrayList<Move>[] legalPerPlayer = new FastArrayList[numPlayers + 1];
//							final List<List<Integer>> legalMoveIndicesPerPlayer = new ArrayList<List<Integer>>(numPlayers + 1);
//							
//							for (int p = 1; p <= numPlayers; ++p)
//							{
//								legalPerPlayer[p] = AIUtils.extractMovesForMover(legal, p);
//									
//								final List<Integer> legalMoveIndices = new ArrayList<Integer>(legalPerPlayer[p].size());
//								for (int i = 0; i < legalPerPlayer[p].size(); ++i)
//								{
//									legalMoveIndices.add(Integer.valueOf(i));
//								}
//								legalMoveIndicesPerPlayer.add(legalMoveIndices);
//							}
//							
//							final List<List<Integer>> combinedMoveIndices = ListUtils.generateTuples(legalMoveIndicesPerPlayer);
//							
//							boolean foundMatch = false;
//							for (final List<Integer> submoveIndicesCombination : combinedMoveIndices)
//							{
//								// Combined all the per-player moves for this combination of indices
//								final List<Action> actions = new ArrayList<>();
//								final List<Moves> topLevelCons = new ArrayList<Moves>();
//								
//								for (int p = 1; p <= numPlayers; ++p)
//								{
//									final Move move = legalPerPlayer[p].get(submoveIndicesCombination.get(p - 1).intValue());
//									if (move != null)
//									{
//										final Move moveToAdd = new Move(move.actions());
//										actions.add(moveToAdd);
//										
//										if (move.then() != null)
//										{
//											for (int i = 0; i < move.then().size(); ++i)
//											{
//												if (move.then().get(i).applyAfterAllMoves())
//													topLevelCons.add(move.then().get(i));
//												else
//													moveToAdd.then().add(move.then().get(i));
//											}
//										}
//									}
//								}
//								
//								final Move combinedMove = new Move(actions);
//								combinedMove.setMover(numPlayers + 1);
//								combinedMove.then().addAll(topLevelCons);
//								
//								final List<Action> combinedMoveAllActions = combinedMove.getActionsWithConsequences(context);
//								if (loadedMoveAllActions.equals(combinedMoveAllActions))
//								{
//									foundMatch = true;
//									break;
//								}
//							}
//							
//							if (!foundMatch)
//							{
//								System.out.println("Found no combination of submoves that generate loaded move: " + loadedMoveAllActions);
//								fail();
//							}
//							
//							game.apply(context, loadedMoves.get(moveIdx));
//						}

//					if (trial.status() == null)
//					{
//						if (loadedTrial.status() != null)
//							System.out
//									.println("Game not over but should be in moveIdx = "
//											+ (moveIdx - trial.numInitialPlacementMoves()));
//						assert (loadedTrial.status() == null);
//					}
//					else
//						assert (trial.status().winner() == loadedTrial.status().winner());
//
//					assert (Arrays.equals(trial.ranking(), loadedTrial.ranking()));
				}
			}
		}

		System.out.println("Finished TestTrialsUndo!");

		final double allSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
		final int seconds = (int) (allSeconds % 60.0);
		final int minutes = (int) ((allSeconds - seconds) / 60.0);
		System.out.println("Done in " + minutes + " minutes " + seconds + " seconds");
	}

}
