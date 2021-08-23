package app.tutorialVisualisation;

import java.util.ArrayList;
import java.util.List;

public class MoveListParser
{
	private static String simpleMove(final String move) 
	{
		System.out.println(move);
		
		String generalizedMove = "";
		final String[] splitMove = move.split("-");
		final int fromX = splitMove[0].charAt(0);
		final int fromY = Integer.parseInt(splitMove[0].substring(1));
		final int toX = splitMove[1].charAt(0);
		final int toY = Integer.parseInt(splitMove[1].substring(1));
		// Vertical move
		if (fromX == toX) {
			generalizedMove += "vertical ";
			// Determine orientation and distance
			if (toY - fromY == 1) {
				generalizedMove += "step up";
			} else if (toY - fromY == -1) {
				generalizedMove += "step down";
			} else if (toY - fromY > 1) {
				generalizedMove += "leap up";
			} else if (toY - fromY < 1) {
				generalizedMove += "leap down";
			}
		// Horizontal move
		} else if (fromY == toY) {
			generalizedMove += "horizontal ";
			// Determine orientation and distance
			if (toX - fromX == 1) {
				generalizedMove += "step right";
			} else if (toX - fromX == -1) {
				generalizedMove += "step left";
			} else if (toX - fromX > 1) {
				generalizedMove += "leap right";
			} else if (toX - fromX < 1) {
				generalizedMove += "leap left";
			}
		// Diagonal move
		} else if (Math.abs(toX - fromX) == Math.abs(toY - fromY) && (toX - fromX != 0)) {
			generalizedMove += "diagonal ";
			// Determine distance
			if (Math.abs(toX - fromX) == 1) {
				generalizedMove += "step ";
			} else {
				generalizedMove += "leap ";
			}
			// Determine horizontal orientation
			if (toX - fromX > 0) {
				generalizedMove += "right-";
			} else {
				generalizedMove += "left-";
			}
			// Determine vertical orientation
			if (toY - fromY > 0) {
				generalizedMove += "up";
			} else {
				generalizedMove += "down";
			}
		// Knight move	
		} else if ((Math.abs(toX - fromX) == 2 && Math.abs(toY - fromY) == 1) || (Math.abs(toX - fromX) == 1 && Math.abs(toY - fromY) == 2)) {
			generalizedMove += "knight move ";
			// Determine horizontal orientation
			if (toX - fromX > 0) {
				generalizedMove += "right-";
			} else {
				generalizedMove += "left-";
			}
			// Determine vertical orientation
			if (toY - fromY > 0) {
				generalizedMove += "up";
			} else {
				generalizedMove += "down";
			}
		}
		return generalizedMove;
	}

	private static String captureMove(final String capture, final String movement) {
		String generalizedMove = "";
		// First get the simple move
		generalizedMove += simpleMove(movement);
		// Determine the captured piece
		final String[] splitMovement = movement.split("-");
		final int toX = splitMovement[1].charAt(0);
		final int toY = Integer.parseInt(splitMovement[1].substring(1));
		final int capturedX = capture.charAt(0);
		final int capturedY = Integer.parseInt(capture.substring(1, capture.length()-1));
		if (capturedX == toX && capturedY == toY) {
			generalizedMove += ", stomp";
		} else {
			generalizedMove += ", jumpover";
		}


		return generalizedMove;
	}

	private static String placementMove(final String move) {
		if (move.substring(3, 7).equals("Disc")) {
			return "disc placement";
		} else if (move.substring(3, 8).equals("Cross")) {
			return "cross placement";
		} else {
			return "";
		}
	}

	private static String shootingPlacementMove(final String location, final String shot) {
		final int locX = location.charAt(0);
		final int locY = Integer.parseInt(location.substring(1));
		final int shotX = shot.charAt(0);
		final int shotY = Integer.parseInt(location.substring(1));
		String generalizedMove = "";

		// Vertical shot
		if (shotX == locX) {
			generalizedMove += "vertical ";
			// Determine orientation
			if (shotY - locY > 0) {
				generalizedMove += "shot up";
			} else {
				generalizedMove += "shot down";
			}
		// Horizontal shot
		} else if (shotY == locY) {
			generalizedMove += "horizontal ";
			// Determine orientation and distance
			if (shotX - locX > 0) {
				generalizedMove += "shot right";
			} else {
				generalizedMove += "shot left";
			}
		// Diagonal shot
		} else if (Math.abs(shotX - locX) == Math.abs(shotY - locY) && (shotX - locX != 0)) {
			generalizedMove += "diagonal shot ";
			// Determine horizontal orientation
			if (shotX - locX > 0) {
				generalizedMove += "right-";
			} else {
				generalizedMove += "left-";
			}
			// Determine vertical orientation
			if (shotY - locY > 0) {
				generalizedMove += "up";
			} else {
				generalizedMove += "down";
			}
		} else {
			System.out.println("Something went wrong" + locX + locY + shotX + shotY);
		}
			return generalizedMove;
	}

	public static List<String> toGeneralizedMoveList(final String input) {
		final String[] individualMoves = input.split("\n");
		final List<String> output = new ArrayList<String>();

		for (int i = 0; i < individualMoves.length; i++) {
			String generalizedMove = "";
			final String pieceIndex = individualMoves[i].split(":")[1];
			individualMoves[i] = individualMoves[i].split(":")[0];

			// Single move
			if (individualMoves[i].startsWith("Move")) {
				final String action = individualMoves[i].split(" ")[1];
				// Simple move
				if (action.charAt(2) == '-') {
					generalizedMove = simpleMove(action);
				}
				// Piece placement
				else if (action.charAt(2) == '+') {
					generalizedMove = placementMove(action);
				}
			}
			// Combined move
			if (individualMoves[i].startsWith("Extra")) {
				// Split on , [ or ]
				final String[] actions = individualMoves[i].split("\\[|\\]|, ");
				// Capture move
				if (actions[1].endsWith("-")) {
					final String capture = actions[1];
					String movement = "A1-A1";
					for (final String action: actions) {
						if (action.length() == 5 && action.charAt(2) == '-') {
							movement = action;
						}
					}
					generalizedMove = captureMove(capture, movement);
					if (actions[actions.length-2].startsWith("Next")) {
						generalizedMove += ", promotion";
						i++;
					}
				}
				// Combined move with next or simple move
				else {
					generalizedMove = simpleMove(actions[1]);
					if (actions[actions.length-1].startsWith("Next")) {
						// Get the placement location
						final String move = individualMoves[i+1].split(" ")[1];
						// Get the location from where there is shot
						final String location = actions[1].split("-")[1];
						generalizedMove += ", " + shootingPlacementMove(location, move);
						i++;
					} else if (actions[actions.length-2].startsWith("Next")) {
						generalizedMove += ", promotion";
						i++;
					}
				}
			}

			if (generalizedMove != "") {
				generalizedMove += "," + pieceIndex;
				output.add(generalizedMove);
			}
		}

		return output;
	}
}