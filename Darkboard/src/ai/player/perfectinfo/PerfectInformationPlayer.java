package ai.player.perfectinfo;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import ai.player.AIPlayer;
import core.Globals;
import core.Move;

/**
 * This player has perfect information and uses an external chess engine such as gnuchess.
 * @author Nikola Novarlic
 *
 */
public abstract class PerfectInformationPlayer extends AIPlayer {
	
	String command[];
	//String command = "ping 127.0.0.1";
	double trueRatio = 0.0; //percentage of moves to be played for real; the rest will be random, to offset perfect information.
	boolean white;
	Random generator = new Random();
	
	public Process engine = null;
	public InputStream engineOutputStream = null;
	public InputStream engineErrorStream = null;
	public OutputStream engineInputStream = null;
	
	
	
	public PerfectInformationPlayer()
	{
		super();
	}
	
	public void destroy()
	{
		Globals.processTable.remove(command[0]);
		engine.destroy();
	}
	
	/**
	 * Creates or assigns the external process. Should not be modified by children.
	 *
	 */
	public void init()
	{
		//first retrieve process from table
		engine = Globals.processTable.get(command[0]);
		
		if (engine==null)
		{
			Runtime rt = Runtime.getRuntime();
			try
			{
				engine = rt.exec(command);
				engineOutputStream = engine.getInputStream();
				engineInputStream = engine.getOutputStream();
				engineErrorStream = engine.getErrorStream();
				Globals.processTable.put(command[0], engine);
				
				Thread.sleep(2000);
				
				String command = null;
				do
				{
					command = readCommand(false);
				} while (command!=null);
				
				sendInitCommands();
				
			} catch (Exception e) { e.printStackTrace(); }
		} else
		{
			engineOutputStream = engine.getInputStream();
			engineInputStream = engine.getOutputStream();
			engineErrorStream = engine.getErrorStream();
		}
	}
	
	/**
	 * First commands to be sent after initialization.
	 *
	 */
	public void sendInitCommands()
	{

	}
	
	protected void sendCommand(String c)
	{
		String comm = c + "\n";
		try
		{
			engineInputStream.write(comm.getBytes());
			engineInputStream.flush();

			//System.out.println("Sent command "+c);
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	protected String readCommand(boolean block)
	{
		String comm = "";
		char i[] = new char[1];
		i[0] = 0;
		
		try
		{
			//System.out.println("Available on output stream: "+engineOutputStream.available());
			if (engineOutputStream.available()==0 && !block) return null;
			
			while (i[0]!='\n' || engineOutputStream.available()!=0)
			{
				i[0] = (char)engineOutputStream.read();
				if (i[0]!='\n') comm = comm + new String(i);
				
				if (engineOutputStream.available()==0)
				{
					if (comm.endsWith(": ")) break;
				}
			}

		} catch (Exception e) { e.printStackTrace(); }
		
		//System.out.println("Received command "+comm);
		return comm;
	}
	
	protected Move representsMove(String comm)
	{
		return null;
	}
	

	
	public void communicateIllegalMove(Move m)
	{
		super.communicateIllegalMove(m);
		
		//System.out.println("Illegal Gnuchess move? "+m);
	}
	
}
