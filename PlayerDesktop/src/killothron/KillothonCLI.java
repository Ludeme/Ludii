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

import game.Game;
import main.Constants;
import main.FileHandling;
import main.StringRoutines;
import main.UnixPrintWriter;
import other.AI;
import other.GameLoader;
import other.RankUtils;
import other.context.Context;
import other.model.Model;
import other.trial.Trial;
import utils.AIFactory;

/**
 * To start a killothon without a GUI (beat a weak ai on all games and send report to a mail).
 * Note: All games except, match, hidden information, simultaneous games or simulation games.
 * 
 * @author Eric.Piette
 */
public class KillothonCLI
{
	/**
	 * Main method
	 * @param args
	 */
	@SuppressWarnings("resource")
	public static void main(final String[] args)
	{
		final double startTime = System.currentTimeMillis();
		final double timeToThink = 60000; // Time for the challenger to think smartly (in ms).
		final int movesLimitPerPlayer = 200; // Max number of moves per player.
		final int numGamesToPlay = Constants.INFINITY;
		final String login = "Challenger";
		double sumUtilities = 0;
		int sumNumMoves = 0;
		
		// Get the list of games.
		final String[] gameList = FileHandling.listGames();
		final ArrayList<String> validChoices = new ArrayList<>();
		for (final String s : gameList)
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
					
					// look only the games we want in the Killothon.
					if(!game.hasSubgames() && !game.hiddenInformation() && !game.isSimultaneousMoveGame() && !game.isSimulationMoveGame())
					{
						idGame++;
						System.out.println("game " + idGame + ": " + game.name() + " is running");
						
						// Set the AIs.
						final List<AI> ais = new ArrayList<AI>();
						ais.add(null);
						for(int pid = 1; pid <= numPlayers; pid++)
						{
							if(pid == 1)
								ais.add(AIFactory.createAI("UCT"));
							else
								ais.add(new utils.RandomAI());
						}

						// Start the game.
						game.setMaxMoveLimit(numPlayers*movesLimitPerPlayer); // Limit of moves per player.
						final Context context = new Context(game, new Trial(game));
						final Trial trial = context.trial();
						game.start(context);
						
						// Init the ais.
						for (int p = 1; p <= game.players().count(); ++p)
							ais.get(p).initAI(game, p);
						final Model model = context.model();
						
						// Run the game.
						double remainingTime = timeToThink; // One minute
						while (!trial.over())
						{
							final int mover = context.state().mover();
							final double time = System.currentTimeMillis();
							model.startNewStep(context, ais, 1);
							final double timeUsed = System.currentTimeMillis() - time;
						    
							// We check the remaining time to be able to think smartly for the challenger.
							if(remainingTime > 0) 
							{
								if(mover == 1)
								{
									remainingTime = remainingTime - timeUsed;
									if(remainingTime <= 0)
									{
										System.out.println("switch P1 to Random");
										ais.get(1).closeAI();
										ais.set(1, new utils.RandomAI());
										ais.get(1).initAI(game, 1);
									}
								}
							}
						}

						// Get results.
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
					
					if((idGame + 1) > numGamesToPlay) // To stop the killothon after a specific number of games (for test).
						break;
			}
		}
		catch (final FileNotFoundException | UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		
		final double killothonTime = System.currentTimeMillis() - startTime;
		int seconds = (int) (killothonTime / 1000) % 60 ;
		int minutes = (int) ((killothonTime / (1000*60)) % 60);
		int hours   = (int) ((killothonTime / (1000*60*60)) % 24);
		System.out.println("Killothon done in " + hours + " hours " + minutes + " minutes " + seconds + " seconds.");
		
		// Sent results.
	    String to = "ludii.killothon@gmail.com";
	    String from = "ludii.killothon@gmail.com"; // TODO change to the second mail used to send results.
	 
        Properties properties = System.getProperties();
        properties = new Properties();
        properties.put("mail.smtp.user", "ludii.killothon@gmail.com");
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
	                    return new PasswordAuthentication("ludii.killothon@gmail.com", "killothon2022"); // TODO change to the second mail used to send results.
	                }
	            });
	 
	    try
	    {
	       // MimeMessage object.
	       MimeMessage message = new MimeMessage(session);
	       
	       // Set Subject: subject of the email
	       message.setSubject("Results of killothon");
	       
	       // Set From Field: adding senders email to from field.
	       message.setFrom(new InternetAddress(from));
	       
	       // Make the body message.
	       BodyPart messageBodyPart1 = new MimeBodyPart();  
	       String bodyMsg = "Killothon run by " + login;
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
           transport.connect("smtp.gmail.com", 465, "ludii.killothon@gmail.com", "killothon2022"); // TODO change to the second mail used to send results.
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
