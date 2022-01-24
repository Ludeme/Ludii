package killothron;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import app.DesktopApp;
import game.Game;
import main.Constants;
import main.FileHandling;
import main.StringRoutines;
import main.UnixPrintWriter;
import other.GameLoader;
import other.RankUtils;
import other.trial.Trial;

/**
 * To start a kilothon with a GUI (beat a weak ai on all games and send report to a mail).
 * Note: All games except, match, hidden information, simultaneous games or simulation games.
 * 
 * @author Eric.Piette
 */
public class KilothonGUI
{
	/**
	 * Main method
	 * @param args
	 */
	@SuppressWarnings("resource")
	public static void main(final String[] args)
	{
		final double startTime = System.currentTimeMillis();
		
		final int sleepTime = 1; // Sleep time before to update the game (in ms).
		final double timeToThink = 60000; // Time for the challenger to think smartly (in ms).
		final int movesLimitPerPlayer = 200; // Max number of moves per player.
		final int numGamesToPlay = Constants.INFINITY;
		final String login = "Challenger";
		double sumUtilities = 0;
		int sumNumMoves = 0;
		
		final DesktopApp app = new DesktopApp();
		app.createDesktopApp();
		final String[] choices = FileHandling.listGames();
		final ArrayList<String> validChoices = new ArrayList<>();

		for (final String s : choices)
		{
			if (s.contains("/lud/plex"))
				continue;

			if (s.contains("/lud/wip"))
				continue;

			if (s.contains("/lud/wishlist"))
				continue;

			if (s.contains("/lud/reconstruction"))
				continue;

			if (s.contains("/lud/WishlistDLP"))
				continue;

			if (s.contains("/lud/test"))
				continue;

			if (s.contains("/res/lud/bad"))
				continue;

			if (s.contains("/res/lud/bad_playout"))
				continue;
			
			validChoices.add(s);
		}
		
		 // Random order for the games.
		Collections.shuffle(validChoices);

		int idGame = 0; // index of the game.
		final String output = "KilothonResults.csv";
		try (final PrintWriter writer = new UnixPrintWriter(new File(output), "UTF-8"))
		{
			for (final String gameName : validChoices)
			{
					final Game game = GameLoader.loadGameFromName(gameName);
					final int numPlayers = game.players().count();

					// look only the games we want in the Kilothon.
					if(!game.hasSubgames() && !game.hiddenInformation() && !game.isSimultaneousMoveGame() && !game.isSimulationMoveGame())
					{
						idGame++;
						System.out.println("game " + idGame + ": " + game.name() + " is running");
						
						// Start the game.
						final RunGame thread = new RunGame(app, gameName, numPlayers, movesLimitPerPlayer);
						double time = System.currentTimeMillis();
						double remainingTime = timeToThink; // One minute
						
						// Run the game.
						thread.run();
						while (!thread.isOver())
						{
							try
							{
								Thread.sleep(sleepTime);
								
								if(remainingTime > 0) // We check the remaining time to be able to think smartly for the challenger.
								{
									final double timeUsed = System.currentTimeMillis() - time;
									if(thread.mover() == 1) // If that's the challenger we decrement the time used.
									{
										remainingTime = remainingTime - timeUsed;
										//System.out.println("remaining Time = " + remainingTime/1000 + " s");
									}
								    time = System.currentTimeMillis();
								    
									if(remainingTime <= 0)
										thread.setFirstPlayerToRandom();
								}
							}
							catch (InterruptedException e)
							{
								e.printStackTrace();
							}
						}
						
						// Print the results.
						final Trial trial = thread.trial();
						final double numMoves = trial.numberRealMoves();
						final double rankingP1 = trial.ranking()[1];
						final double rewardP1 = RankUtils.rankToUtil(rankingP1, numPlayers);
						System.out.println("Reward of P1 = " + rewardP1 + " (ranking = " + rankingP1 + ") finished in " + trial.numberRealMoves() + " moves.");
						sumUtilities += rewardP1;
						sumNumMoves += numMoves;
						final List<String> lineToWrite = new ArrayList<String>();
						lineToWrite.add(game.name() + ""); // game name 
						lineToWrite.add(rankingP1 + ""); // ranking of P1
						lineToWrite.add(rewardP1 + ""); // reward of P1
						lineToWrite.add(numMoves + ""); // game length
						writer.println(StringRoutines.join(",", lineToWrite));
					}
					
					if((idGame + 1) > numGamesToPlay) // To stop the kilothon after a specific number of games (for test).
						break;
			}
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		
		app.appClosedTasks();
		final double kilothonTime = System.currentTimeMillis() - startTime;
		int seconds = (int) (kilothonTime / 1000) % 60 ;
		int minutes = (int) ((kilothonTime / (1000*60)) % 60);
		int hours   = (int) ((kilothonTime / (1000*60*60)) % 24);
		System.out.println("Kilothon done in " + hours + " hours " + minutes + " minutes " + seconds + " seconds.");
		
		// Sent results.
	    String to = "ludii.kilothon@gmail.com";
	    String from = "competitionSender@gmail.com";
	 
        Properties properties = System.getProperties();
        properties = new Properties();
        properties.put("mail.smtp.user", from);
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.starttls.enable","true");
        properties.put("mail.smtp.debug", "true");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.socketFactory.port", "587");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.socketFactory.fallback", "false");
	 
	    // creating session object to get properties
	    Session session = Session.getInstance(properties,
	            new javax.mail.Authenticator() {
	                @Override
					protected PasswordAuthentication getPasswordAuthentication() {
	                    return new PasswordAuthentication(from, "sendResultCompetition"); 
	                }
	            });
	 
	    try
	    {
	       // MimeMessage object.
	       MimeMessage message = new MimeMessage(session);
	       
	       // Set Subject: subject of the email
	       message.setSubject("Results of kilothon");
	       
	       // Set From Field: adding senders email to from field.
	       message.setFrom(new InternetAddress(from));
	       
	       // Make the body message.
	       BodyPart messageBodyPart1 = new MimeBodyPart();  
	       String bodyMsg = "Kilothon run by " + login;
	       bodyMsg += "\nAgent name = " + "UCT";
	       bodyMsg += "\nSmart thinking time (in ms) = " + timeToThink;
	       bodyMsg += "\nMoves limit per player = " + movesLimitPerPlayer;
	       bodyMsg += "\nGames played = " + idGame;
	       bodyMsg += "\nAVG utility = " + (sumUtilities/idGame);
	       bodyMsg += "\nNum Moves = " + sumNumMoves;
	       bodyMsg += "\nAVG Moves = " + (sumNumMoves/idGame);
	       bodyMsg += "\nDone in " + hours + " hours " + minutes + " minutes " + seconds + " seconds.";
	       messageBodyPart1.setText(bodyMsg);  
	       
	       // Add the attachment.
	       MimeBodyPart messageBodyPart2 = new MimeBodyPart();  
	       DataSource source = new FileDataSource(output);  
	       messageBodyPart2.setDataHandler(new DataHandler(source)); 
	       messageBodyPart2.setFileName(output);
	 
	       // Set up the full message.
	       Multipart multipart = new MimeMultipart();  
	       multipart.addBodyPart(messageBodyPart1);  
	       multipart.addBodyPart(messageBodyPart2);
	       message.setContent(multipart);  
	 
	       // Set To Field: adding recipient's email to from field.
	       message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
	 
	       // Send email.
           Transport transport = session.getTransport("smtps");
           transport.connect("smtp.gmail.com", 465, from, "sendResultCompetition");
           transport.sendMessage(message, message.getAllRecipients());
           transport.close();  
	       System.out.println("Mail successfully sent");
	    }
	    catch (MessagingException mex)
	    {
	       mex.printStackTrace();
	    }
	}
}
