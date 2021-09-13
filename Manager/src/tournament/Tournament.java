package tournament;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import game.Game;
import manager.Manager;
import manager.ai.AIUtil;

/**
 * A Ludii Tournament
 *
 * @author Dennis Soemers and Matthew Stephenson and cambolbro
 */
public class Tournament
{
	/** List of games we wish to play in tournament */
	private final List<String> gamesToPlay;

	/** List of agents to participate in tournament */
	private final List<Object> agentsToPlay;

	/** Results of tournament */
	private final List<String[]> results;

	private List<int[]> matchUps;
	private int matchUpIndex;
	private int[] matchUp;

	//-------------------------------------------------------------------------

	/**
	 * Constructor from JSON
	 * @param json
	 */
	public Tournament(final JSONObject json)
	{
		final JSONArray listGames = json.getJSONArray("GAMES");
		gamesToPlay = new ArrayList<>(listGames.length());

		System.out.println("Tournament games:");
		for (final Object obj : listGames)
		{
			final String game = (String) obj;
			gamesToPlay.add(game);
			System.out.println(game);
		}

		final JSONArray listAgents = json.getJSONArray("AGENTS");
		agentsToPlay = new ArrayList<>(listAgents.length());

		System.out.println("Tournament agents:");
		for (final Object obj : listAgents)
		{
			agentsToPlay.add(obj);
			System.out.println(obj);
		}

		results = new ArrayList<>();
	}

	//-------------------------------------------------------------------------

	/**
	 * Sets up the tournament for a (new) start
	 */
	public void setupTournament()
	{
		// reset the global tournament variables
		results.clear();

		final int totalNumberPlayers = agentsToPlay.size();

		final List<int[]> matchUpsFlipped = generate(totalNumberPlayers, 2);
		matchUps = generate(totalNumberPlayers, 2);
		for (int j = 0; j < matchUps.size(); j++)
		{
			for (int i = 0; i < matchUps.get(j).length / 2; i++)
			{
				final int temp = matchUps.get(j)[i];
				matchUps.get(j)[i] = matchUps.get(j)[matchUps.get(j).length - i - 1];
				matchUps.get(j)[matchUps.get(j).length - i - 1] = temp;
			}
		}
		matchUps.addAll(matchUpsFlipped);
		matchUpIndex = 0;
	}

	/**
	 * Run the next game for the tournament
	 */
	public void startNextTournamentGame(final Manager manager)
	{
		if (gamesToPlay.size() > 0 && matchUps.size() > 0)
		{
			matchUp = matchUps.get(matchUpIndex);

			for (int i = 0; i < matchUp.length; i++)
			{
				final Object agent = agentsToPlay.get(matchUp[i]);
				final JSONObject json;

				if (agent instanceof JSONObject)
					json = (JSONObject) agent;
				else
					json = new JSONObject().put("AI", new JSONObject().put("algorithm", agent));

				AIUtil.updateSelectedAI(manager, json, i + 1, json.getJSONObject("AI").getString("algorithm"));
			}

			final List<String> gameAndOptions = Arrays.asList(gamesToPlay.get(0).split("-"));
			if (gameAndOptions.size() > 1)
			{
				System.out.println(gameAndOptions.get(1));
				manager.getPlayerInterface().loadGameFromName(gameAndOptions.get(0).trim(), gameAndOptions.subList(1, gameAndOptions.size()), false);
			}
			else
			{
				manager.getPlayerInterface().loadGameFromName(gameAndOptions.get(0).trim(), new ArrayList<String>(), false);
			}

			matchUpIndex++;
			if (matchUpIndex >= matchUps.size())
			{
				matchUpIndex = 0;
				gamesToPlay.remove(0);
			}

			manager.settingsManager().setAgentsPaused(manager, false);
			manager.ref().nextMove(manager, false);
		}
		else
		{
			// The tournament is over, show the results
			System.out.println("FINAL RESULTS SHORT");
//			String finalResultsToSend = "FINAL RESULTS SHORT";

			for (int i = 0; i < results.size(); i++)
			{
				final String result = Arrays.toString(results.get(i));
				System.out.println(result);
//				finalResultsToSend += result;
			}

			System.out.println("\nFINAL RESULTS LONG");
//			finalResultsToSend += "FINAL RESULTS LONG";

			for (int i = 0; i < results.size(); i++)
			{
				final String gameData = "GAME(" + (i + 1) + ") " + results.get(i)[0];
				System.out.println(gameData);
//				finalResultsToSend += gameData;

				try
				{
					for (int j = 0; j < results.get(i)[1].length(); j++)
					{
						final String result = "Player "
								+ (Integer.parseInt(results.get(i)[1].split(",")[j].replace("[", "")
										.replace("]", "").trim()) + 1)
								+ " : " + results.get(i)[j + 2];
						System.out.println(result);
//						finalResultsToSend += result;
					}
				}
				catch (final Exception e)
				{
					// just skip the players that don't have scores
				}
			}
		}
	}

	/**
	 * Storse the results obtained in a single match
	 *
	 * @param game
	 * @param ranking
	 */
	public void storeResults(final Game game, final double[] ranking)
	{
		final String[] result = new String[10];

		try
		{
			result[0] = game.name();
			result[1] = Arrays.toString(matchUp);
			result[2] = Double.toString(ranking[1]);
			result[3] = Double.toString(ranking[2]);
			result[4] = Double.toString(ranking[3]);
			result[5] = Double.toString(ranking[4]);
			result[6] = Double.toString(ranking[5]);
			result[7] = Double.toString(ranking[6]);
			result[8] = Double.toString(ranking[7]);
			result[9] = Double.toString(ranking[8]);
		}
		catch (final Exception E)
		{
			// player number requested probably doesn't exist, carry on as normal
		}

		results.add(result);
	}

	/**
	 * Called when the tournament is being aborted / ended
	 */
	public void endTournament()
	{
		// Do nothing (for now)
	}

	//-------------------------------------------------------------------------

	/**
	 * generates all nCr combinations (all round robin tournament combinations)
	 */
	private static List<int[]> generate(final int n, final int r)
	{
		final List<int[]> combinations = new ArrayList<>();
		final int[] combination = new int[r];

		// initialize with lowest lexicographic combination
		for (int i = 0; i < r; i++)
		{
			combination[i] = i;
		}

		while (combination[r - 1] < n)
		{
			combinations.add(combination.clone());

			// generate next combination in lexicographic order
			int t = r - 1;
			while (t != 0 && combination[t] == n - r + t)
			{
				t--;
			}
			combination[t]++;
			for (int i = t + 1; i < r; i++)
			{
				combination[i] = combination[i - 1] + 1;
			}
		}

		return combinations;
	}

}
