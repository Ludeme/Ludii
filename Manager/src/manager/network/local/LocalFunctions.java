package manager.network.local;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import game.rules.play.moves.Moves;
import manager.Manager;
import other.context.Context;

/**
 * Local network functions that can be called by external agents using sockets.
 * Messages are formatted as "XXXX ACTION EXTRA", where XXXX is the port number to return messages to, ACTION is the keyword for the desired task (see below), and EXTRA is any additional information.
 * Example messages include:
 * "5555 move 4" 	(make the 4th legal move)
 * "5555 legal" 	(return all legal moves)
 * "5555 player"	(return the current mover)
 * 
 * @author Matthew.Stephenson
 */
public class LocalFunctions 
{
	static ServerSocket serverSocket;
	static Socket socket;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Initialise the server socket and await messages.
	 */
	public static void initialiseServerSocket(final Manager manager, final int port)
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
						
						// Reply string to respond to incoming messages.
						String reply = "";
						
						// Request about a move made.
						if (message.length() >= 9 && message.substring(5, 9).equals("move"))
						{
							reply = "move failure";
							final Context context = manager.ref().context();
							final Moves legal = context.game().moves(context);
							for (int i = 0; i < legal.moves().size(); i++)
							{
								if (i == Integer.parseInt(message.substring(10).trim()))
								{
									manager.ref().applyHumanMoveToGame(manager, context.game().moves(context).moves().get(i));
									reply = "move success";
								}
							}
							initialiseClientSocket(Integer.parseInt(message.substring(0,4)), reply);
						}
						// Request about legal moves.
						else if (message.length() >= 10 && message.substring(5, 10).equals("legal"))
						{
							final Context context = manager.ref().context();
							final Moves legal = context.game().moves(context);
							for (int i = 0; i < legal.moves().size(); i++)
								reply += i + " - " + legal.moves().get(i).getActionsWithConsequences(context) + "\n";
							initialiseClientSocket(Integer.parseInt(message.substring(0,4)), "legal\n" + reply);
						}
						// Request about current mover.
						else if (message.length() >= 11 && message.substring(5, 11).equals("player"))
						{
							reply = Integer.toString(manager.ref().context().state().mover());
							initialiseClientSocket(Integer.parseInt(message.substring(0,4)), "player " + reply);
						}
						
						System.out.println("Reply= " + reply);
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
	 * Initialise the client socket when a message needs to be sent.
	 */
	public static void initialiseClientSocket(final int port, final String Message)
	{
		new Thread(new Runnable() 
		{
			@Override
			public void run() 
		    {
				try (final Socket clientSocket = new Socket("localhost",port))
				{  
					try(final DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream()))
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
		}).start();
	}
	
	//-------------------------------------------------------------------------
	
}
