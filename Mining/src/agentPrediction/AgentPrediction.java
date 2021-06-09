package agentPrediction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import game.Game;
import other.concept.Concept;
import other.concept.ConceptComputationType;

public class AgentPrediction 
{

	/**
	 * @return Name of the best predicted agent from our pre-trained set of models.
	 */
	public static String predictBestAgentName(final Game game)
	{
		String sInput = null;
		String sError = null;

        try {
            
        	final String modelName = "RandomForestClassifier";
        	
        	final String conceptNameString = "RulesetName,Properties,Format,Time,Discrete,Realtime,Turns,Alternating,Simultaneous,"
        			+ "Stochastic,HiddenInformation,Match,Asymmetric,AsymmetricRules,AsymmetricPlayRules,AsymmetricEndRules,"
        			+ "AsymmetricForces,AsymmetricSetup,AsymmetricPiecesType,Players,NumPlayers,Simulation,Solitaire,TwoPlayer,"
        			+ "Multiplayer,Cooperation,Team,Coalition,Puzzle,DeductionPuzzle,PlanningPuzzle,Equipment,Container,Board,"
        			+ "Shape,SquareShape,HexShape,TriangleShape,DiamondShape,RectangleShape,SpiralShape,CircleShape,PrismShape,"
        			+ "StarShape,ParallelogramShape,SquarePyramidalShape,RectanglePyramidalShape,RegularShape,PolygonShape,"
        			+ "TargetShape,Tiling,SquareTiling,HexTiling,TriangleTiling,BrickTiling,SemiRegularTiling,CelticTiling,"
        			+ "MorrisTiling,QuadHexTiling,CircleTiling,ConcentricTiling,SpiralTiling,AlquerqueTiling,MancalaBoard,"
        			+ "MancalaStores,MancalaTwoRows,MancalaThreeRows,MancalaFourRows,MancalaSixRows,MancalaCircular,Track,"
        			+ "TrackLoop,TrackOwned,Hints,Region,Boardless,PlayableSites,Vertex,Cell,Edge,NumPlayableSitesOnBoard,"
        			+ "NumColumns,NumRows,NumCorners,NumDirections,NumOrthogonalDirections,NumDiagonalDirections,NumAdjacentDirections,"
        			+ "NumOffDiagonalDirections,NumOuterSites,NumInnerSites,NumLayers,NumEdges,NumCells,NumVertices,NumPerimeterSites,"
        			+ "NumTopSites,NumBottomSites,NumRightSites,NumLeftSites,NumCentreSites,NumConvexCorners,NumConcaveCorners,"
        			+ "NumPhasesBoard,Hand,NumContainers,NumPlayableSites,Component,Piece,PieceValue,PieceRotation,PieceDirection,"
        			+ "Dice,BiasedDice,Card,Domino,LargePiece,Tile,NumComponentsType,NumComponentsTypePerPlayer,NumDice,Rules,Meta,"
        			+ "OpeningContract,SwapOption,Repetition,TurnKo,SituationalTurnKo,PositionalSuperko,SituationalSuperko,AutoMove,"
        			+ "Start,PiecesPlacedOnBoard,PiecesPlacedOutsideBoard,InitialRandomPlacement,InitialScore,InitialAmount,InitialPot,"
        			+ "InitialCost,NumStartComponentsBoard,NumStartComponentsHand,NumStartComponents,Play,Moves,Add,AddDecision,Step,"
        			+ "StepDecision,StepToEmpty,StepToFriend,StepToEnemy,Slide,SlideDecision,SlideToEmpty,SlideToEnemy,SlideToFriend,"
        			+ "Leap,LeapDecision,LeapToEmpty,LeapToFriend,LeapToEnemy,Hop,HopDecision,HopMoreThanOne,HopEnemyToEmpty,"
        			+ "HopFriendToEmpty,HopEnemyToFriend,HopFriendToFriend,HopEnemyToEnemy,HopFriendToEnemy,Sow,SowEffect,SowCapture,"
        			+ "SowRemove,SowBacktracking,SowProperties,SowSkip,SowOriginFirst,SowCW,SowCCW,Bet,BetDecision,Vote,VoteDecision,"
        			+ "Promotion,Remove,RemoveDecision,FromTo,FromToDecision,FromToWithinBoard,FromToBetweenContainers,FromToEmpty,"
        			+ "FromToEnemy,FromToFriend,Rotation,Push,Flip,SwapPieces,SwapPiecesDecision,SwapPlayers,SwapPlayersDecision,"
        			+ "TakeControl,Shoot,ShootDecision,Priority,ByDieMove,MaxMovesInTurn,MaxDistance,SetMove,SetNextPlayer,MoveAgain,"
        			+ "SetValue,SetCount,ChooseTrumpSuit,Pass,PassDecision,Roll,GraphMoves,SetCost,SetPhase,Propose,ProposeDecision,"
        			+ "Capture,ReplacementCapture,HopCapture,HopCaptureMoreThanOne,DirectionCapture,EncloseCapture,CustodialCapture,"
        			+ "InterveneCapture,SurroundCapture,CaptureSequence,MaxCapture,Conditions,SpaceConditions,Line,Connection,Group,"
        			+ "Contains,Loop,Pattern,PathExtent,Territory,Fill,Distance,MoveConditions,Stalemate,CanMove,CanNotMove,PieceConditions,"
        			+ "NoPiece,NoTargetPiece,Threat,IsEmpty,IsEnemy,IsFriend,LineOfSight,ProgressCheck,Directions,AbsoluteDirections,"
        			+ "AllDirections,AdjacentDirection,OrthogonalDirection,DiagonalDirection,OffDiagonalDirection,RotationalDirection,"
        			+ "SameLayerDirection,RelativeDirections,ForwardDirection,BackwardDirection,ForwardsDirection,BackwardsDirection,"
        			+ "RightwardDirection,LeftwardDirection,RightwardsDirection,LeftwardsDirection,ForwardLeftDirection,ForwardRightDirection,"
        			+ "BackwardLeftDirection,BackwardRightDirection,SameDirection,OppositeDirection,Information,HidePieceType,HidePieceOwner,"
        			+ "HidePieceCount,HidePieceRotation,HidePieceValue,HidePieceState,InvisiblePiece,Phase,NumPlayPhase,Scoring,PieceCount,SumDice"
        			+ ",End,SpaceEnd,LineEnd,ConnectionEnd,GroupEnd,LoopEnd,PatternEnd,PathExtentEnd,TerritoryEnd,CaptureEnd,Checkmate,"
        			+ "NoTargetPieceEnd,RaceEnd,Escape,FillEnd,ReachEnd,ScoringEnd,StalemateEnd,NoProgressEnd,Draw,Math,Arithmetic,Operations,"
        			+ "Addition,Subtraction,Multiplication,Division,Modulo,Absolute,Roots,Cosine,Sine,Tangent,Exponentiation,Exponential,"
        			+ "Logarithm,Minimum,Maximum,Comparison,Equal,NotEqual,LesserThan,LesserThanOrEqual,GreaterThan,GreaterThanOrEqual,Parity,"
        			+ "Even,Odd,Logic,Conjunction,Disjunction,ExclusiveDisjunction,Negation,Set,Union,Intersection,Complement,Algorithmics,"
        			+ "ConditionalStatement,ControlFlowStatement,Float,Visual,Style,BoardStyle,GraphStyle,ChessStyle,GoStyle,MancalaStyle,"
        			+ "PenAndPaperStyle,ShibumiStyle,BackgammonStyle,JanggiStyle,XiangqiStyle,ShogiStyle,TableStyle,SurakartaStyle,TaflStyle,"
        			+ "NoBoard,ComponentStyle,AnimalComponent,ChessComponent,KingComponent,QueenComponent,KnightComponent,RookComponent,"
        			+ "BishopComponent,PawnComponent,FairyChessComponent,PloyComponent,ShogiComponent,XiangqiComponent,StrategoComponent,"
        			+ "JanggiComponent,HandComponent,CheckersComponent,BallComponent,TaflComponent,DiscComponent,MarkerComponent,"
        			+ "StackType,Stack,Symbols,ShowPieceValue,ShowPieceState,Implementation,State,StateType,StackState,PieceState,"
        			+ "SiteState,SetSiteState,VisitedSites,Variable,SetVar,RememberValues,ForgetValues,SetPending,InternalCounter,"
        			+ "SetInternalCounter,PlayerValue,SetHidden,SetInvisible,SetHiddenCount,SetHiddenRotation,SetHiddenState,SetHiddenValue,"
        			+ "SetHiddenWhat,SetHiddenWho,Efficiency,CopyContext,Then,ForEachPiece,DoLudeme,Trigger";
        	
        	final String conceptValueString = "UNUSED,"
        			+ "1,1,1,1,0,1,1,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,1,0,0,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,"
        			+ "0,0,0,0,1,1,1,0,0,0,0,1,1,0,0,1,0,1,1,0,0,12,7,2,2,4.17,2.83,1.33,2.83,0,12,0,1,17,6,12,12,5,5,1,1,2,2,0,3,0,1,12,1,"
        			+ "1,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,100,0,100,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,"
        			+ "0,0,0,0,0,0,1,1,1,0,0,1,0,1,0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,"
        			+ "0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,"
        			+ "0,0,0,0,0,0,0,0,0,1,1,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,1,1,1,1,"
        			+ "0,1,1,1,1,0,0,1,1,0,0,1,1,0,0,1,1,1,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,"
        			+ "1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,1,1,0,0,0";
        	
            final Process p = Runtime.getRuntime().exec("python3 ../../LudiiPrivate/DataMiningScripts/Sklearn/GetBestPredictedAgent.py " + modelName + " " + conceptNameString + " " + conceptValueString);

            // Read file output
            final BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((sInput = stdInput.readLine()) != null) 
            {
            	// String returned in the form " ['PREDICTEDAGENT=Alpha Beta'] "
            	if (sInput.contains("PREDICTEDAGENT"))
            		return sInput.split("'")[1].split("=")[1];
            }
            
            // Read any errors.
            final BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((sError = stdError.readLine()) != null) 
            {
            	System.out.println("Python Error\n");
                System.out.println(sError);
            }
        }
        catch (final IOException e) 
        {
            e.printStackTrace();
        }
    
		return "Random";
	}
	
	/**
	 * @return The concepts as a string with comma between them.
	 */
	public static String compilationConceptString()
	{
		Concept[] concepts = Concept.values();
		final StringBuffer sb = new StringBuffer();
		for(Concept concept: concepts)
			if(concept.computationType().equals(ConceptComputationType.Compilation))
				sb.append(concept.name()+",");
	
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	
}
