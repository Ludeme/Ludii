/*
 * Created on 18-mar-06
 *
 */
package gui.test;


// import java.awt.event.ActionEvent;

import java.io.File;
import java.util.Scanner;

import ai.opponent.OpponentProfile;
import ai.player.DeepDarkboard101;
import ai.player.HumanPlayer;
import ai.player.Player;
import ai.player.PlayerListener;
import core.Chessboard;
import core.Globals;
import core.Move;
import pgn.ExtendedPGNGame;
import umpire.local.FENData;
import umpire.local.LocalUmpire;
import umpire.local.StepwiseLocalUmpire;

/**
 * @author Nikola Novarlic
 *
 */
public class OnePlayerFrame /*extends JFrame implements ActionListener, TreeSelectionListener*/ {
	
	public class PGNFileFilter 
		{
			public String getDescription()
			{
				return "PGN files";
			}
		
			public boolean accept(File f)
			{
				return (f.getName().endsWith(".pgn") || f.isDirectory());
			}
		}
	
	private static int gamesPlayed = 0;
	private static boolean initialized = false;
	
	/*
	// Remove JFrame 
	JChessboard board;
	PGNDisplay pgnDisplay;
	JTextField moveField;
	*/
	UmpireText nodeDescriptionArea;
	/*
	JComboBox combo;
	JCheckBox showOpponent;
	*/
	
	// JCheckBoxMenuItem promPieces[] = new JCheckBoxMenuItem[4];
	
	public StepwiseLocalUmpire umpire;
	//public Darkboard p1, p2;
	public Player p1,p2;
	ExtendedPGNGame pgn = null;
	int turn=0;
	boolean turnwhite=true;
	boolean gameInProgress=true;
	private String umpireMessages = "";
	
	public class GameThread extends Thread
	{
		public void run()
		{
			main(new String[0]);
		}
	}
	
	public class UmpireText /*extends JTextArea*/ implements PlayerListener
	{
		boolean finished=false;
		
		public int currentMove()
		{
			return (umpire.transcript.getMoveNumber());
		}
		
		public String playerColor(Player p)
		{
			return (p.isWhite? "White's turn" : "Black's turn");
		}
		
		public String opponentColor(Player p)
		{
			return (!p.isWhite? "White's turn" : "Black's turn");
		}


		public String checkString(int check)
		{
			switch(check)
			{
			case Chessboard.CHECK_FILE: return "File check; ";
			case Chessboard.CHECK_RANK: return "Rank check; ";
			case Chessboard.CHECK_SHORT_DIAGONAL: return "Short-diagonal check; ";
			case Chessboard.CHECK_LONG_DIAGONAL: return "Long-diagonal check; ";
			case Chessboard.CHECK_KNIGHT: return "Knight check; ";
			}
			return "";
		}

		public String communicateUmpireMessage() {
			return umpireMessages;
		}

		public String triesString(int tries)
		{
			return ""+tries+(tries!=1? " Tries; " : " Try; ");
		}
		
		public void preAppend(String s)
		{
			// setText(s+"\n"+getText());
		}

		public void communicateIllegalMove(Player p, Move m) {
			// TODO Auto-generated method stub
		}

		public String communicateLegalMove(Player p, int capture, int oppTries, int oppCheck, int oppCheck2) {
			if (!p.isHuman() || finished) return "";
			String s = (currentMove()+(p.isWhite? 0 : 1))+". "+opponentColor(p)+"; ";
			if (capture!=Chessboard.NO_CAPTURE)
			{
				if (capture==Chessboard.CAPTURE_PAWN) s+="Pawn at "; else s+="Piece at ";
				s+=Move.squareString(p.lastMove.toX, p.lastMove.toY)+" captured; ";
			}
			if (oppTries>0)
			{
				s+=triesString(oppTries);
			}
			s+=checkString(oppCheck); s+=checkString(oppCheck2);
			// System.out.println(s);
			umpireMessages += s;
			umpireMessages += "\n";
			preAppend(s);
			return s;
		}

		public void communicateOutcome(Player p, int outcome) {
			if (!p.isHuman() || finished) return;
			String s = (p.isWhite? "White":"Black");
			String t = (!p.isWhite? "White":"Black");
			switch(outcome)
			{
			case Player.PARAM_CHECKMATE_DEFEAT: preAppend(s+" checkmated"); System.out.println(s+" checkmated"); umpireMessages += (s+" checkmated"); umpireMessages += "\n"; break;
			case Player.PARAM_CHECKMATE_VICTORY: preAppend(t+" checkmated"); System.out.println(t+" checkmated"); umpireMessages += (t+" checkmated"); umpireMessages += "\n";break;
			case Player.PARAM_RESIGN_DEFEAT: preAppend(s+" resigned"); System.out.println(s+" resigned"); umpireMessages += (s+" resigned"); umpireMessages += "\n";break;
			case Player.PARAM_RESIGN_VICTORY: preAppend(t+" resigned"); System.out.println(t+" resigned"); umpireMessages += (t+" resigned"); umpireMessages += "\n"; break;
			case Player.PARAM_NO_MATERIAL: preAppend("Insufficient Material Draw"); System.out.println("Insufficient Material Draw"); umpireMessages += ("Insufficient Material Draw"); umpireMessages += "\n"; break;
			case Player.PARAM_STALEMATE_DRAW: preAppend("Stalemate"); System.out.println("Stalemate"); umpireMessages += (s+" checkmated"); umpireMessages += "\n"; break;
			case Player.PARAM_AGREED_DRAW: preAppend("Mutual agreement draw"); System.out.println("Mutual agreement draw"); umpireMessages += ("Mutual agreement draw"); umpireMessages += "\n"; break;
			case Player.PARAM_50_DRAW: preAppend("Fifty moves draw"); System.out.println("Fifty moves draw"); umpireMessages += ("Fifty moves draw"); umpireMessages += "\n"; break;
			}
			finished = true;
			/*
			// Remove JFrame
			 
			if (!showOpponent.isSelected()) showOpponent.doClick();
			*/
		}

		public String communicateUmpireMessage(Player p, int capX, int capY, int tries, int check, int check2, int capture) {
			if (!p.isHuman() || finished) return "";
			String s = (currentMove()+(p.isWhite? 1 : 0))+". "+playerColor(p)+"; ";

			/*
			int capture=0;
			if (capX<0) capture = Chessboard.NO_CAPTURE;
			else
			{
				int x = p.simplifiedBoard.getFriendlyPiece(capX, capY);
				if (x==Chessboard.PAWN) capture = Chessboard.CAPTURE_PAWN; else capture = Chessboard.CAPTURE_PIECE;
			}
			*/
			if (capture!=Chessboard.NO_CAPTURE)
			{
				if (capture==Chessboard.CAPTURE_PAWN) s+="Pawn at "; else s+="Piece at ";
				s+=Move.squareString(capX, capY)+" captured; ";
			}
			if (tries>0)
			{
				s+=triesString(tries);
			}
			s+=checkString(check); s+=checkString(check2);
			// System.out.println(s);
			umpireMessages += s;
			umpireMessages += "\n";
			preAppend(s);
			return s;
		}

		public void updateTime(Player p, long newQty) {
			// TODO Auto-generated method stub
			
		}

		public void communicateInfo(Player p, int code, int parameter) {
			// TODO Auto-generated method stub
			
			switch (code)
			{
			case Player.INFO_DRAW_OFFER_REJECTED:
				// JOptionPane.showMessageDialog(null,"Draw offer was rejected.","Umpire message",JOptionPane.INFORMATION_MESSAGE);
				// System.out.println("Draw offer was rejected.");
				umpireMessages += "Draw offer was rejected.";
				umpireMessages += "\n";
			}
			
		}

		public void communicateObject(Player p, Object tag, Object value, int messageType) {
			// TODO Auto-generated method stub
			
		}

		public boolean isInterestedInObject(Player p, Object tag, int messageType) {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
	
	public OnePlayerFrame(Player pl1, Player pl2, ExtendedPGNGame pgn, FENData fen)
	{
		/*
		// Removing JFrame
		
		super("Darkboard");
		getContentPane().setLayout(null);
		this.setResizable(false);
		this.setSize(680,406);
		*/
		
		
		//p1 = new Darkboard(true);
		//p2 = new ExperimentalDarkboard(false);
		p1 = pl1;
		p2 = pl2;
		umpire = new StepwiseLocalUmpire(p1,p2);
		umpire.adjudication = false;
		/*
		 // Removing JFrame
		
		board = new JChessboard(umpire);
		board.setLocation(20,20);
		this.getContentPane().add(board);
		umpire.addListener(board);
		
		board.setViewPoint(pl1.isHuman()? pl1 : pl2);
		
		showOpponent = new JCheckBox("Show Opponent");
		showOpponent.addActionListener(this);
		showOpponent.setActionCommand("showOpponent");
		showOpponent.setSize(200, 24);
		showOpponent.setLocation(360,20);
		this.getContentPane().add(showOpponent);
		*/
		/*
		JButton b1 = new JButton("New Game");
		b1.setActionCommand("newGame");
		b1.addActionListener(this);
		b1.setSize(120,30);
		b1.setLocation(240,360);
		this.getContentPane().add(b1);
		
		
		JButton b3 = new JButton("Save");
		b3.setActionCommand("pgnSave");
		b3.addActionListener(this);
		b3.setSize(80,30);
		b3.setLocation(380,360);
		this.getContentPane().add(b3);
		*/
		
		
		// Removing JFrame
		nodeDescriptionArea = new UmpireText();
		// nodeDescriptionArea.setEditable(false);
		//nodeDescriptionArea.setSize(300,100);
		//nodeDescriptionArea.setLocation(360,240);
		/*
		 // Remove JFrame
		JScrollPane nP = new JScrollPane(nodeDescriptionArea);
		nP.setSize(300,100);
		nP.setLocation(360, 240);
		*/
		if (pl1.isHuman()) pl1.addPlayerListener(nodeDescriptionArea);
		else pl2.addPlayerListener(nodeDescriptionArea);
		
		//this.getContentPane().add(nodeDescriptionArea);
		// this.getContentPane().add(nP);
		
		
		//initialize the local umpire if needbe
		umpire.stepwiseInit(fen, pgn);
		ExtendedPGNGame gam = umpire.transcript;
		/*
		 // Removing JFrame
		 
		pgnDisplay = new PGNDisplay();
		pgnDisplay.setLayout(null);
		pgnDisplay.setGame(gam);
		pgnDisplay.setLocation(0,0);
		pgnDisplay.setSize(300,180);
		JScrollPane sp = new JScrollPane(pgnDisplay);
		pgnDisplay.setLocation(0,0);
		pgnDisplay.setSize(300,180);
		//sp.setLayout(null);
		sp.setLocation(360,50);
		sp.setSize(300,180);
		this.getContentPane().add(sp);
		*/
		gam.firePGNChange();
		/*
		 // Removing JFrame

		JMenuBar bar = new JMenuBar();
		JMenu menu = new JMenu("File");
		bar.add(menu);
		JMenuItem newgame = new JMenuItem("New Game (Simple)");
		JMenuItem newgame2 = new JMenuItem("New Game...");
		JMenuItem savegame = new JMenuItem("Save as PGN...");
		JMenuItem showgame = new JMenuItem("Load PGN...");
		newgame.addActionListener(this);
		newgame.setActionCommand("newGame");
		newgame2.addActionListener(this);
		newgame2.setActionCommand("newGameAdvanced");
		savegame.addActionListener(this);
		savegame.setActionCommand("pgnSave");
		showgame.addActionListener(this);
		showgame.setActionCommand("loadGameFromFile");
		newgame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.META_MASK));
		newgame2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.META_MASK|InputEvent.SHIFT_DOWN_MASK));
		savegame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.META_MASK));
		showgame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.META_MASK));
		menu.add(newgame); menu.add(newgame2); menu.add(savegame); menu.add(showgame);
		*/
		/*
		 // Removing JFrame
		 
		JMenu action = new JMenu("Action");
		JMenu setPromPiece = new JMenu("Set Promotion Piece");
		bar.add(action);
		
		String n[] = {"Queen","Knight","Rook","Bishop"};
		for (int k=0; k<4; k++)
		{
			promPieces[k] = new JCheckBoxMenuItem(n[k],k==0);
			promPieces[k].addActionListener(this);
			promPieces[k].setActionCommand(n[k]);
			setPromPiece.add(promPieces[k]);
		}
		action.add(setPromPiece);
		action.addSeparator();
		
		JMenuItem offerDraw = new JMenuItem("Offer Draw");
		offerDraw.addActionListener(this);
		offerDraw.setActionCommand("offerDraw");
		action.add(offerDraw);
		JMenuItem resign = new JMenuItem("Resign");
		resign.addActionListener(this);
		resign.setActionCommand("resign");
		action.add(resign);
		
		this.setJMenuBar(bar);
		   
		this.setLocationRelativeTo(null);
		this.setVisible(true);
		this.getContentPane().repaint();
		*/
	}
	
	private void interrogatePlayer(boolean white)
	{
		//Darkboard ai = (white? p1 : p2);
		Player ai = (white? p1 : p2);
		
		ai.globals.usingExtendedGameTree = true;
		Move m = ai.getNextMove();
		
		//umpire.stepwiseArbitrate(m);
		// System.out.println(m.toString());
		/*
		 // Remove JFrame
		moveField.setText(m.toString());
		*/
	}
	
	public static Move parseMoveString(String s,boolean white)
	{
		Move m = new Move();
		
		if (s==null) return null;
		
		if (s.equals("O-O"))
		{
			m.fromX = 4;
			m.toX = 6;
			m.piece = Chessboard.KING;
			if (white) m.toY = m.fromY = 0;
			else m.toY = m.fromY = 7;
			return m;
		}
		if (s.equals("O-O-O"))
		{
			m.fromX = 4;
			m.toX = 2;
			m.piece = Chessboard.KING;
			if (white) m.toY = m.fromY = 0;
			else m.toY = m.fromY = 7;
			return m;
		}
		
		int index = 0;
		m.piece = Chessboard.PAWN;
		switch (s.charAt(0))
		{
			case 'N':
				m.piece = Chessboard.KNIGHT;
				index = 1;
				break;
			case 'B':
				m.piece = Chessboard.BISHOP;
				index = 1;
				break;
			case 'R':
				m.piece = Chessboard.ROOK;
				index = 1;
				break;
			case 'Q':
				m.piece = Chessboard.QUEEN;
				index = 1;
				break;
			case 'K':
				m.piece = Chessboard.KING;
				index = 1;
				break;
		}
		
		m.fromX = (byte)(s.charAt(index) - 'a');
		m.fromY = (byte)(s.charAt(index+1) - '1');
		m.toX = (byte)(s.charAt(index+3) - 'a');
		m.toY = (byte)(s.charAt(index+4) - '1');		
		
		if (s.indexOf("=")!=-1)
		{
			switch (s.charAt(s.length()-1))
			{
				case 'N': m.promotionPiece = Chessboard.KNIGHT; break;
				case 'B': m.promotionPiece = Chessboard.BISHOP; break;
				case 'R': m.promotionPiece = Chessboard.ROOK; break;
				case 'Q': m.promotionPiece = Chessboard.QUEEN; break;
			}
		}
		
		return m;
	}

	
	private void loadGame(File f)
	{
		/*
		 // Remove JFrame
		try
		{
			FileInputStream fis = new FileInputStream(f);
			PGNParser parser = new PGNParser(fis);
			p1 = new Darkboard(true);
			p2 = new Darkboard(false);
			umpire = new StepwiseLocalUmpire(p1,p2);
			umpire.addListener(board);
			board.master = umpire;
			umpire.stepwiseInit(null,null);
			pgn = parser.parseNextGame();
			umpire.resetBoard();
			umpire.turn = 0;
			turn = 0;
			turnwhite = true;
			board.repaint();
		}
		catch (Exception e)
		{
		}
		*/
	}
	
	
	public void loadGameFromFile()
	{
		/*
		 // Remove JFrame
		 
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Select game");
		chooser.setFileFilter(new PGNFileFilter());
		if (chooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION)
		{
			File f = chooser.getSelectedFile();
			// loadGame(f);
			try 
			{
				//Get content from PGN file
				String content = "";
				File myObj = new File(f.getAbsolutePath());
			    Scanner myReader = new Scanner(myObj);
			    while (myReader.hasNextLine()) {
			        String data = myReader.nextLine();
			        content += data;
			        content += "\n";
			     }
			    myReader.close();
			    
			    //Create frame where the game will be displayed  
			    JFrame frame = new JFrame("DarkBoard Game"); 
				frame.setPreferredSize(new Dimension(800, 600));
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				
				JTextArea textarea = new JTextArea(content);
				frame.getContentPane().add(textarea, BorderLayout.CENTER);
				
				//Make the text area scrollable  
				JScrollPane scroll = new JScrollPane (textarea, 
						   JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

				frame.add(scroll);
				
				//Display the window
				frame.setLocationRelativeTo(null); 
				frame.pack(); 
				frame.setVisible(true);
			}
			catch (Exception e)
			{
			}
		}
		*/
		
	}
	
	public void saveGame()
	{
		/*
		 // Remove JFrame
		 
		JFileChooser chooser = new JFileChooser();

		//chooser.setLayout(null);
		chooser.setDialogTitle("Save game");
		chooser.setFileFilter(new PGNFileFilter());
		//chooser.setFileFilter(new PGNFileFilter());
		if (chooser.showSaveDialog(this)==JFileChooser.APPROVE_OPTION)
		{
			File f = chooser.getSelectedFile();
			umpire.transcript.setWhite(p1.getPlayerName());
			umpire.transcript.setBlack(p2.getPlayerName());
			umpire.transcript.saveToFile(f);
			
		}
		*/	
	}
	
	public void startGame()
	{
		new GameThread().start();
	}
	
	public static void main(String args[])
	{
		//if (args.length>0) 
		if (!initialized)
		{
			String path = System.getProperty("user.home") + "/darkboard_data/";
			// System.out.println(path);
			// Darkboard.initialize(path);
			initialized = true;
		} 


		/*File appPath = new File(System.getProperty("java.class.path"));
		try
		{
		appPath = appPath.getCanonicalFile().getParentFile();
		}
		catch (IOException e)
		{
		e.printStackTrace();
		}
		System.out.println("Path: " + appPath.toString());*/
		System.out.println(System.getProperty("java.class.path"));
		
		Globals.hasGui = true;
		
		
		//EvaluationGlobals.usingExtendedGameTree = true;
		
		OnePlayerFrame f;
		
		OpponentProfile op = OpponentProfile.getProfile("rjay");
		
		if (gamesPlayed%2 == 0)
		{
			// System.out.println("ABC!");
			//f = new OnePlayerFrame(new HumanPlayer(true),new PureMCPlayer(false,3),null,null);
			f = new OnePlayerFrame(new HumanPlayer(true),new DeepDarkboard101(false,op.openingBookWhite,op.openingBookBlack,"rjay"),null,null);
			// System.out.print(op.openingBookBlack.toString());
			//f = new OnePlayerFrame(new HumanPlayer(true),new ExperimentalDarkboard(false),null,null);
			//f = new OnePlayerFrame(new HumanPlayer(true),new OldDarkboard(false),null,null);
		} else
		{
			//f = new OnePlayerFrame(new ExperimentalDarkboard(true),new HumanPlayer(false),null,null);
			f = new OnePlayerFrame(new DeepDarkboard101(true,op.openingBookWhite,op.openingBookBlack,"rjay"),new HumanPlayer(false),null,null);
		}
		gamesPlayed++; //alternate between white and black
		
		/*f.umpire.emptyBoard();
		f.umpire.insertPiece(Chessboard.KING,0,false);
		f.umpire.insertPiece(Chessboard.ROOK,0,true);
		f.umpire.insertPiece(Chessboard.ROOK,0,true);
		f.umpire.insertPiece(Chessboard.KING,1,false);*/
		//f.p1.receivedUmpireMessages = true;
		//f.umpire.stepwiseInit(f.umpire.board);
		
		while (f.umpire.getGameOutcome()==LocalUmpire.NO_OUTCOME)
		{
			Player p = f.umpire.turn();
			//f.interrogatePlayer(t==0);
			System.out.print("\n");
			for(int i = 7; i >= 0; i--) {
				for(int j = 0; j < 8; j++) {
					System.out.print(f.umpire.board[j][i]);
					System.out.print(" ");
				}
				System.out.print("\n");
			}
			// System.out.println(f.nodeDescriptionArea.getText());
			Move m;
			if(p instanceof HumanPlayer) {
				Scanner InputStream = new Scanner(System.in);
				// System.out.print("Enter the next move: ");
				String playerMove = InputStream.nextLine();
				m = parseMoveString(playerMove, p.isWhite);
				p.emulateNextMove(m); 
				// System.out.println("My move: " + m.toString());
				
			}
			else m = p.getNextMove();
			f.umpire.stepwiseArbitrate(m);
		}
		// System.out.println("Game Over!");
		//Save each game to PGN:  
		f.umpire.transcript.setWhite("player");
		f.umpire.transcript.setBlack("agent");
		f.umpire.transcript.saveToFile();
	}
	
	
	//Implementation of ActionListener
	public void actionPerformed(/*ActionEvent ae*/)
	{	
		/*
		 // Remove JFrame
		
		int pSel = -1;
		if (ae.getActionCommand().equals("Queen")) { pPiece = Chessboard.QUEEN; pSel = 0;}
		if (ae.getActionCommand().equals("Knight")) { pPiece = Chessboard.KNIGHT; pSel = 1;}
		if (ae.getActionCommand().equals("Rook")) { pPiece = Chessboard.BISHOP; pSel = 2;}
		if (ae.getActionCommand().equals("Bishop")) { pPiece = Chessboard.ROOK; pSel = 3;}
		*/
		int pPiece = -1;
		if (pPiece!=-1)
		{
			// for (int k=0; k<4; k++) this.promPieces[k].setSelected(k==pSel);
			((HumanPlayer)(p1.isHuman()? p1 : p2)).setPromotionPiece(pPiece);
		}
	}

	/*
	 // Remove JFrame
	//Implementation of TreeSelectionListener
	public void valueChanged(TreeSelectionEvent e)
	{
		 
		TreePath tp = e.getPath();
		EvaluationTree et = (EvaluationTree)(tp.getLastPathComponent());
		nodeDescriptionArea.setText(et.getExtendedRepresentation());
		board.setMetaposition(et.sc);
		
	}
	*/
}

