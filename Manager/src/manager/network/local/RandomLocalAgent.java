package manager.network.local;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

/**
 * An example random agent that makes moves using the Ludii socket interface.
 * Start a separate Ludii application, and select the Remote -> Initialise Server Socket menu option.
 * Enter the same port number as specified below for the portNumberLudii variable.
 * Now run this program.
 * This agent will make a random move every time the turn number of the game being played equals the playerNumber variable below.
 * 
 * @author Matthew.Stephenson
 */
public class RandomLocalAgent 
{
	// Change this based on your player number in the game.
	static final int playerNumber = 2;
	// Change this to the port number of the Ludii application.
	static final int portNumberLudii = 4444;
	// Change this to the port number used by the agent.
	static final int portNumberAgent = 5555;
	
	// Last recorded mover
	static int currentPlayerNumber = 0;
	// Last recorded legal moves
	static String currentLegalMoves = "";
	
	static ServerSocket serverSocket;
	static Socket socket;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Initialise the agent's own server socket, to receive incoming messages from the Ludii application.
	 * @param port
	 */
	public static void initialiseServerSocket(final int port)
	{
		new Thread(new Runnable() 
		{
			@Override
			public void run() 
		    {
				try
				{  
					serverSocket = new ServerSocket(port);  
					
					while (true)
					{
						// Establish connection. 
						socket = serverSocket.accept(); 
						final DataInputStream dis = new DataInputStream(socket.getInputStream());  
						
						// Print any messages from socket.
						final String message = dis.readUTF();
						System.out.println("message= " + message);
						
						// Request about legal moves.
						if (message.substring(0,5).equals("legal"))
						{
							currentLegalMoves = message.substring(6);
						}
						// Request about current mover.
						if (message.substring(0,6).equals("player"))
						{
							currentPlayerNumber = Integer.parseInt(message.substring(7));
						}
					}
				}
				catch(final Exception e)
				{
					e.printStackTrace();
					try 
					{
						serverSocket.close();
						socket.close();
					} 
					catch (final IOException e1) 
					{
						e1.printStackTrace();
					}  
				} 
		    }
		}).start();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Sends the specified message to the specified port.
	 * @param port
	 * @param Message
	 */
	public static void initialiseClientSocket(final int port, final String Message)
	{
		try (final Socket clientSocket = new Socket("localhost",port))
		{  
			try (final DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream());  )
			{
				dout.writeUTF(Message);  
				dout.flush();  
				dout.close();  
				clientSocket.close();  
			}
			catch(final Exception e)
			{
				e.printStackTrace();
			}  	 
		}
		catch(final Exception e)
		{
			e.printStackTrace();
		}  	 
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Agent will continuously request the Ludii application for information, and make moves where appropriate.
	 * @param args
	 */
	public static void main(final String args[])
	{
		initialiseServerSocket(portNumberAgent);
		
		// Update agent stored information (current mover, legal moves)
		final Runnable runnableUpdateValues = new Runnable()
		{
			@Override
			public void run()
			{
				while (true)
				{
					initialiseClientSocket(portNumberLudii, "" + portNumberAgent + " player");
					initialiseClientSocket(portNumberLudii, "" + portNumberAgent + " legal");
					try
					{
						Thread.sleep(10);
					}
					catch (final InterruptedException e)
					{
						// You probably logged out and the thread was interrupted.
					}
				}
			}
		};
		final Thread repeatUpdateValuesThread = new Thread(runnableUpdateValues);
		repeatUpdateValuesThread.start();

		// If it's the agent's turn, make a random move.
		final long timeInterval = 100;
		final Runnable runnableMakeMove = new Runnable()
		{
			@Override
			public void run()
			{
				while (true)
				{
					if (playerNumber == currentPlayerNumber)
					{
						final String[] allLegalMoves = currentLegalMoves.split("\n");
						final int randomNum = ThreadLocalRandom.current().nextInt(0, allLegalMoves.length);
						initialiseClientSocket(portNumberLudii, "" + portNumberAgent + " move " + randomNum);
					}

					try
					{
						Thread.sleep(timeInterval);
					}
					catch (final InterruptedException e)
					{
						// You probably logged out and the thread was interrupted.
					}
				}
			}
		};
		final Thread repeatMakeMoveThread = new Thread(runnableMakeMove);
		repeatMakeMoveThread.start();
	}
	
	//-------------------------------------------------------------------------
	
}
