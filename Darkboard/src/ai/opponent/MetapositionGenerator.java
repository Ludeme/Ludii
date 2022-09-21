package ai.opponent;

import java.util.Random;
import java.util.Vector;

import ai.opponent.MetapositionConstraint;
import ai.player.Player;
import core.Chessboard;
import core.Metaposition;
import core.Move;
import database.PlayerModel;
import pgn.ExtendedPGNGame;
import umpire.local.IncompatibleMetapositionException;
import umpire.local.LocalUmpire;

public class MetapositionGenerator {
	
	static Random r = new Random();
	
	public static Vector<MetapositionConstraint> generateBasicConstraints(Metaposition source, ExtendedPGNGame pgn)
	{
		Vector<MetapositionConstraint> v = new Vector<MetapositionConstraint>();
		
		int mn = 0;
		boolean lastMoveWhite = false;
		ExtendedPGNGame.PGNMoveData last = null;
		
		if (pgn!=null)
		{
			mn = pgn.getMoveNumber();
			lastMoveWhite = (pgn.getMove(false, mn-1)==null);
			last = pgn.getLatestMove(lastMoveWhite);
		}
		//create pawn constraints...
		for (int k=0; k<8; k++)
		{
			int p = source.getMinPawns((byte)k);
			if (p<=0) continue;
			for (int a=0; a<p; a++)
			{
				MetapositionConstraint mc = new MetapositionConstraint();
				mc.movesAgo = 0;
				mc.pieces[Chessboard.PAWN] = true;
				mc.unsatisfiable = true;
				for (int j=1; j<7; j++) if (source.canContainEnemyPawn(k, j))
					mc.addSquare(k, j, source.ageMatrix[k*8 + j]);
				
				v.add(mc);
			}
		}
		
		//create king constraint...
		MetapositionConstraint mc = new MetapositionConstraint();
		mc.movesAgo = 0;
		mc.pieces[Chessboard.KING] = true;
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
				if (source.canContainEnemyKing((byte)k,(byte)j)) 
					mc.addSquare(k, j, source.ageMatrix[k*8 + j]);
		
		v.add(mc);
		
		//create piece capture constraint
		if (pgn!=null && lastMoveWhite!=source.isWhite() && last.capturex!=-1)
		{
			MetapositionConstraint mc2 = new MetapositionConstraint();
			for (int k=0; k<6; k++) if (source.canContain((byte)last.capturex, (byte)last.capturey, (byte)k))
				mc2.pieces[k] = true;
			
			mc.addSquare(last.capturex, last.capturey, 1);
			v.add(mc2);
		}
		
		return v;
	}
	
	public static Vector<Metaposition> generateMetaposition(Player p, Metaposition source, int howMany, ExtendedPGNGame pgn)
	{
		
		Metaposition out;
		int pawnsToPlace;
		int piecesToPlace;
		int counter = 0;
		
		Vector<Integer> pcs = new Vector<Integer>();
		
		int mn = 0;
		boolean lastMoveWhite = false;
		ExtendedPGNGame.PGNMoveData last = null;
		
		if (pgn!=null)
		{
			mn = pgn.getMoveNumber();
			lastMoveWhite = (pgn.getMove(false, mn-1)==null);
			last = pgn.getLatestMove(lastMoveWhite);
		}
		
		
		while (true)
		{
		out = new Metaposition(p);
		out.setWhite(!source.isWhite());
		out.setAge(source.getAge());
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
				out.setEmpty((byte)k, (byte)j);
				//out.setFriendlyPiece(k, j, Chessboard.EMPTY);
		
		//artificially reconstruct the 'age' of the squares
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				int pieces = 0;
				int squares = 0;
				for (int x=k-2; x<=k+2; x++)
					for (int y=j-2; y<=j+2; y++)
					{
						if (x<0 || y<0 || x>7 || y>7) continue;
						squares++;
						if (source.getFriendlyPiece(x, y)!=Chessboard.EMPTY) pieces++;
					}
				double pieceRatio = 1.0*pieces/squares;
				int maxAge = (int)(100*pieceRatio*pieceRatio);
				int actualAge = (maxAge<=0? 0 : r.nextInt(maxAge));
				if (pieceRatio>0.0 && actualAge<1) actualAge = 1;
				if (actualAge>25) actualAge = 25;
				out.ageMatrix[k*8 + j] = (char)actualAge;
				
				if (actualAge>0)
				{
					out.setPiecePossible(k, j, Chessboard.KNIGHT);
					out.setPiecePossible(k, j, Chessboard.BISHOP);
					out.setPiecePossible(k, j, Chessboard.ROOK);
					out.setPiecePossible(k, j, Chessboard.QUEEN);
					out.setPiecePossible(k, j, Chessboard.KING);
					if (j!=0 && j!=7) out.setPiecePossible(k, j, Chessboard.PAWN);
				}
			}
		out.updateTotalAge();
		
		for (int k=0; k<8; k++)
		{
			out.setMinPawns((byte)k, (byte)0);
			out.setMaxPawns((byte)k, (byte)2);
		}
		
		//count this player's material and assume the opponent knows as much
		int pawns = 0; int pieces = 0;
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				int pc = source.getFriendlyPiece(k, j); 
				if (pc==Chessboard.PAWN) pawns++;
				else if (pc!=Chessboard.EMPTY && (pc!=Chessboard.KING)) pieces++;
			}
		out.pawnsLeft = (byte)pawns;
		out.piecesLeft = (byte)pieces;
		
		Vector<MetapositionConstraint> cons = generateBasicConstraints(source,pgn);
		
		for (int k=0; k<cons.size(); k++)
		{
			MetapositionConstraint mc = cons.get(k);
			satisfyConstraint(source,out,mc.pieceType(),cons,k);
		}
		

		
		//count how many pieces and pawns have been placed
		pawns = 0; pieces = 0;
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				int pc = out.getFriendlyPiece(k, j); 
				if (pc==Chessboard.PAWN) pawns++;
				else if (pc!=Chessboard.EMPTY && (pc!=Chessboard.KING)) pieces++;
			}
		
		pawnsToPlace = source.pawnsLeft-pawns;
		piecesToPlace = source.piecesLeft-pieces;
		
		if (pawnsToPlace>=0 && piecesToPlace>=0) break;
		
		counter++;
		if (counter==100) return new Vector<Metaposition>();
		
		}
		
		Vector<Metaposition> v = new Vector<Metaposition>();
		
		LocalUmpire lu = new LocalUmpire(new Player(), new Player());
		
		for (int k=0; v.size()<howMany && k<100; k++)
		{
			Metaposition n = Metaposition.getChessboard(out);
			Vector<MetapositionConstraint> vmc = new Vector<MetapositionConstraint>();
			
			//add missing pawns
			for (int a=0; a<pawnsToPlace; a++)
			{
				//find all files with fewer maximum pawns than are present...
				MetapositionConstraint mc = new MetapositionConstraint();
				vmc.clear();
				vmc.add(mc);
				mc.pieces[Chessboard.PAWN] = true;
				for (int file=0; file<8; file++)
				{
					int pwn = 0;
					for (int rank=0; rank<8; rank++)
						if (n.getFriendlyPiece(file, rank)==Chessboard.PAWN) pwn++;
					if (pwn<source.getMaxPawns((byte)file))
					{
						for (int rank=0; rank<8; rank++)
							if (source.canContainEnemyPawn(file, rank) && n.getFriendlyPiece(file, rank)==Chessboard.EMPTY)
								mc.addSquare(file, rank, source.ageMatrix[file*8 + rank]);
					}
				}
				satisfyConstraint(source, n, Chessboard.PAWN, vmc, 0);
				
			}
			
			//add missing pieces
			for (int a=0; a<piecesToPlace; a++)
			{
				int knights = 0; int bishops = 0; int rooks = 0; int queens = 0;
				int pc;
				
				if (k>0) pc = pcs.get(a).intValue(); //make sure you always generate the same pieces
				else
				{
					for (int file=0; file<8; file++)
						for (int rank=0; rank<8; rank++)
						{
							pc = n.getFriendlyPiece(file, rank);
							if (pc==Chessboard.KNIGHT) knights++;
							else if (pc==Chessboard.BISHOP) bishops++;
							else if (pc==Chessboard.ROOK) rooks++;
							else if (pc==Chessboard.QUEEN) queens++;
						}
							
					double knightProb = 1.0;
					double bishopProb = 1.0;
					double rookProb = 1.0;
					double queenProb = 0.5;
							
					knightProb -= (0.5*knights);
					bishopProb -= (0.5*bishops);
					rookProb -= (0.5*rooks);
					queenProb -= (0.5*queens);
					if (queenProb<=0.0) queenProb = 0.0001;
							
					double choice = r.nextDouble()*(knightProb+bishopProb+rookProb+queenProb);
					if (choice<knightProb)
						pc = Chessboard.KNIGHT;
					else
					{
						choice -= knightProb;
						if (choice<bishopProb) pc = Chessboard.BISHOP;
						else
						{
							choice -= bishopProb;
							if (choice<rookProb) pc = Chessboard.ROOK; else pc = Chessboard.QUEEN;
						}
					}
					pcs.add(new Integer(pc));
				}		
				//now place the piece somewhere compatible
				MetapositionConstraint mc = new MetapositionConstraint();
				vmc.clear();
				vmc.add(mc);
				mc.pieces[pc] = true;
				for (int file=0; file<8; file++)
					for (int rank=0; rank<8; rank++)
						if (source.canContain((byte)file, (byte)rank, (byte)pc) && n.getFriendlyPiece(file, rank)==Chessboard.EMPTY)
							mc.addSquare(file, rank, source.ageMatrix[file*8 + rank]);

				satisfyConstraint(source, n, pc, vmc, 0);
			}
			
			Metaposition white = (source.isWhite()? source : n);
			Metaposition black = (source.isWhite()? n : source);
			try
			{
				if (pgn!=null && lu.areMetapositionsCompatibleWithUmpireMessage(white, black, !lastMoveWhite, last.capturex, last.capturey, last.check1, last.check2, last.pawntries)) v.add(n);
				if (pgn==null) v.add(n);
			} catch (IncompatibleMetapositionException e)
			{
				continue;
			}
		}
		
		
		return v;
	}
	
	/**
	 * Puts a piece somewhere to satisfy a constraint. Also considers related constraints.
	 * @param m
	 * @param type
	 * @param cons
	 * @param index
	 */
	protected static void satisfyConstraint(Metaposition source, Metaposition m, int type, Vector<MetapositionConstraint> cons, int index)
	{
		MetapositionConstraint mc = cons.get(index);
		double satisf = mc.metapositionSatisfiesConstraint(m);
		if (satisf>=1.0 || satisf>=r.nextDouble()) return;
		
		//PlayerModel pm = (source.isWhite()? Globals.blackModel: Globals.whiteModel); 
		PlayerModel pm = source.owner.getOpponentModel();
		
		Vector<int[]> where = mc.squares;
		
		Vector<int[]> where2 = new Vector<int[]>();
		for (int k=0; k<where.size(); k++)
		{
			int[] a = where.get(k);
			if (m.getFriendlyPiece(a[0], a[1])==Chessboard.EMPTY) where2.add(a);
		}
		if (where2.size()<1) return;
		
		int totalProb = 0;
		int probs[] = new int[where2.size()];
		for (int k=0; k<where2.size(); k++) 
		{
			double ageValue = (where2.get(k)[2] /* *mc.pieceBonusWithConstraintVector(where2.get(k)[0], where2.get(k)[1], cons)*/ );
			//probs[k] = probs[k]*probs[k]; //let's steer sampling towards the more likely states
			int mov = (source.isWhite()? source.owner.moveNumber-2 : source.owner.moveNumber-1);
			if (ageValue>mov) ageValue = mov;
			double opponentModel = 100.0*pm.get(mov).quantities[where2.get(k)[0]][where2.get(k)[1]][type];
			//age and the value from opponent modeling are weighted and summed. In the beginning, when
			//little information is available, modeling is worth more; later, age becomes more important
			/*double weight = 0.01 + mov/100.0;
			if (weight>0.9) weight = 0.9;
			probs[k] = (int)(ageValue*weight + opponentModel*(1.0-weight));*/
			probs[k] = (int)(opponentModel*ageValue);
			totalProb += probs[k];
		}
		
		int rand = (totalProb<1? 1 : r.nextInt(totalProb));
		for (int k=0; k<where2.size(); k++)
		{
			if (rand<probs[k])
			{
				//put the piece here...
				int age = m.ageMatrix[where2.get(k)[0]*8 + where2.get(k)[1]];
				m.setFriendlyPiece(where2.get(k)[0], where2.get(k)[1], type);
				m.ageMatrix[where2.get(k)[0]*8 + where2.get(k)[1]] = 0;
				m.totalAge -= age;
				return;
			} else rand -= probs[k];
		}
		
	}
	
	/**
	 * Changes a piece's position to (hopefully) make a metaposition compatible with an observation
	 * @param start
	 * @param model
	 * @return
	 */
	public static Metaposition mutateMetaposition(Metaposition start, Metaposition model)
	{
		Vector<int[]> cumbersomePieces = new Vector<int[]>(); //pieces that should not be where they are
		Vector<int[]> guiltyEmptiness = new Vector<int[]>(); //empty spaces that should not be empty
		
		PlayerModel pm = model.owner.getOpponentModel();
		int move = (model.isWhite()? model.owner.moveNumber-2 : model.owner.moveNumber-1);
		
		if (cumbersomePieces.size()==0) return null;
		
		for (int k=0; k<8; k++)
			for (int j=0; j<8; j++)
			{
				int piece = start.getFriendlyPiece(k, j);
				if (piece==Chessboard.EMPTY && model.getFriendlyPiece(k, j)==Chessboard.EMPTY && !model.mayBeEmpty(k, j))
				{
					int v[] = {k,j};
					guiltyEmptiness.add(v);
				}
				if (piece!=Chessboard.EMPTY && (model.getFriendlyPiece(k, j)!=Chessboard.EMPTY || !model.canContain((byte)k, (byte)j, (byte)piece)))
				{
					int v[] = {k,j};
					cumbersomePieces.add(v);
				}
			}
		
		int movedPieces = 0;
		int tries = 0;
		Metaposition out = Metaposition.getChessboard(start);
		
		while (cumbersomePieces.size()>0 || guiltyEmptiness.size()>0 || movedPieces==0)
		{
			if (tries>50) return out;
			
			int source[]=null;
			if (cumbersomePieces.size()>0) source = cumbersomePieces.remove(r.nextInt(cumbersomePieces.size()));
			int dest[]=null;
			if (cumbersomePieces.size()>0) dest = cumbersomePieces.remove(r.nextInt(cumbersomePieces.size()));
			
			if (source==null)
			{
				Vector<int[]> abc = new Vector<int[]>();
				//select a random piece
				for (int k=0; k<8; k++)
					for (int j=0; j<8; j++)
					{
						if (out.getFriendlyPiece(k, j)!=Chessboard.EMPTY)
						{
							int v[] = {k,j}; abc.add(v);
						}
					}
				
				if (abc.size()<1) return null;
				source = abc.get(r.nextInt(abc.size()));
			}
			
			if (dest==null)
			{
				Vector<int[]> abc = new Vector<int[]>();
				int totalAge = 0;
				
				//select a random piece
				int startingPiece = out.getFriendlyPiece(source[0], source[1]);
				for (int k=0; k<8; k++)
					for (int j=0; j<8; j++)
					{
						if (model.canContain((byte)k, (byte)j, (byte)startingPiece))
						{
							int v[] = {k,j}; abc.add(v);
							int age = model.ageMatrix[k*8+j];
							if (age>move) age = move;
							totalAge += (int)(pm.get(move).quantities[k][j][startingPiece]*model.ageMatrix[k*8+j]);
						}
					}
				if (abc.size()<1) { tries++; continue; }
				int rand = r.nextInt(totalAge);
				for (int k=0; k<abc.size(); k++)
				{
					int s = model.ageMatrix[abc.get(k)[0]*8+abc.get(k)[1]];
					s = s*s;
					if (rand<s) { dest = abc.get(k); break; } else rand -= s;
				}
				//dest = abc.get(r.nextInt(abc.size()));
			}
			
			if (out.getFriendlyPiece(source[0], source[1])==Chessboard.PAWN)
			{
				if (model.getMinPawns((byte)source[0])==out.getPawnsForFile(source[0]) && source[0]!=dest[0])
				{
					tries++;
					continue;
				}
				if (model.getMaxPawns((byte)dest[0])==out.getPawnsForFile(dest[0]) && source[0]!=dest[0])
				{
					tries++;
					continue;
				}
			}
			
			if (!out.canContain((byte)dest[0], (byte)dest[1], (byte)out.getFriendlyPiece(source[0], source[1])))
			{
				tries++;
				Vector<int[]> abc = new Vector<int[]>();
				//select a random piece
				for (int k=0; k<8; k++)
					for (int j=0; j<8; j++)
					{
						if (out.getFriendlyPiece(k, j)!=Chessboard.EMPTY)
						{
							int v[] = {k,j}; abc.add(v);
						}
					}
				cumbersomePieces.add(abc.get(r.nextInt(abc.size())));
				continue;
			}
			
			out.setFriendlyPiece(dest[0], dest[1], out.getFriendlyPiece(source[0], source[1]));
			out.setEmpty((byte)source[0],(byte)source[1]);
			movedPieces++;
		}
		
		return out;
	}
	
	static Vector<Metaposition> evolveMetaposition(Metaposition opponent, Metaposition yours, Vector<Move> legal, Vector<Metaposition> evol, int number)
	{
		Vector<Metaposition> out = new Vector<Metaposition>();
		int probs[] = new int[legal.size()];
		int total = 0;
		
		int move = (yours.isWhite()? yours.owner.moveNumber-2 : yours.owner.moveNumber-1);
		PlayerModel pm = yours.owner.getOpponentModel();
		
		for (int k=0; k<legal.size(); k++)
		{
			Move m = legal.get(k);
			int pc = opponent.getFriendlyPiece(m.fromX, m.fromY);
			double probNow = (move==0? 1.0 : pm.get(move-1).quantities[m.fromX][m.fromY][pc]);
			double probLater = pm.get(move).quantities[m.fromX][m.fromY][pc];
			double probYonder = pm.get(move).quantities[m.toX][m.toY][pc];
			if (probLater<0.00001) probLater = 0.00001;
			double ratio = probNow / probLater /* * probNow*/;
			if (ratio<0.1) ratio = 0.1;
			if (ratio>10.0) ratio = 10.0;
			ratio *= probYonder;
			probs[k] = (int)(ratio*10000);
			total += probs[k];
			//ratio = ratio*ratio;
			//double p = 
		}
		
		for (int k=0; k<number; k++)
		{
			int rand = (total<1? 1 : r.nextInt(total));
			for (int j=0; j<legal.size(); j++)
			{
				if (rand<probs[j])
				{
					out.add(evol.get(j));
					break;
				} else rand -= probs[j];
			}
		}
		
		return out;
	}

}
