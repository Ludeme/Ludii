package manager.utils.game_logs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.rng.core.RandomProviderDefaultState;

import game.Game;
import game.equipment.container.Container;
import other.state.State;
import other.state.container.ContainerState;
import other.trial.Trial;

/**
 * A collection of one or more game trials which can be serialized and
 * deserialized.
 * 
 * @author Dennis Soemers
 */
public class GameLogs
{
	/** */
	private final String gameName;

	/** */
	private final Game game;

	/** */
	private final List<MatchRecord> matchRecords = new ArrayList<MatchRecord>();

	//-------------------------------------------------------------------------

	public GameLogs(final Game game)
	{
		gameName = game.name();
		this.game = game;
	}

	//-------------------------------------------------------------------------

	public void addMatchRecord(final MatchRecord matchRecord)
	{
		matchRecords.add(matchRecord);
	}
	
	public List<MatchRecord> matchRecords()
	{
		return matchRecords;
	}
	
	public Game game()
	{
		return game;
	}

	//-------------------------------------------------------------------------
	
	public void testIntegrity()
	{
		for (final MatchRecord matchRecord : matchRecords)
		{
			matchRecord.testIntegrity(game);
		}
	}
	
	//-------------------------------------------------------------------------

	public static GameLogs fromFile(final File file, final Game game)
	{
		GameLogs gameLogs = null;

		try (ObjectInputStream reader = new ObjectInputStream(
			new BufferedInputStream(new FileInputStream(file))))
		{

			final String gameName = reader.readUTF();

			//final Game game = PlayerCli.getGameInstance(gameName, nbRow, nbCol);
			gameLogs = new GameLogs(game);

			while (reader.available() > 0)
			{
				final int numRngStateBytes = reader.readInt();
				
				final byte[] rngStateBytes = new byte[numRngStateBytes];
				final int numBytesRead = reader.read(rngStateBytes);
				
				if (numBytesRead != numRngStateBytes)
				{
					System.err.println
					(
						"Warning: GameLogs.fromFile() expected " + numRngStateBytes + 
						" bytes, but only read " + numBytesRead + " bytes!"
					);
				}
					
				final RandomProviderDefaultState rngState = new RandomProviderDefaultState(rngStateBytes);
				//System.out.println("loaded state = " + Arrays.toString(rngState.getState()));
				 
				final Trial trial = (Trial) reader.readObject();
				final List<State> states = trial.auxilTrialData().stateHistory();
								
				// fix reference to container for all ItemStates
				if (states != null)
				{
					for (final State state : states)
					{
						final ContainerState[] itemStates = state.containerStates();
						
						for (final ContainerState itemState : itemStates)
						{
							if (itemState != null)
							{
								final String containerName = itemState.nameFromFile();
								
								for (final Container container : game.equipment().containers())
								{
									if (container != null && container.name().equals(containerName))
									{
										itemState.setContainer(container);
										break;
									}
								}
							}
						}
					}
				}
				  
				gameLogs.addMatchRecord(new MatchRecord(trial, rngState, gameName));
				 
			}
		}
		catch (final IOException | ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		
		//System.out.println(gameLogs.matchRecords.get(0).trial.actions());

		return gameLogs;
	}

	public String getGameName()
	{
		return gameName;
	}

	//-------------------------------------------------------------------------

}
