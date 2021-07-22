package other;

import game.types.play.RoleType;
import gnu.trove.list.array.TIntArrayList;
import other.context.Context;

/**
 * Utility class to get the indices of players.
 * 
 * @author Eric.Piette
 */
public class PlayersIndices
{
	/**
	 * @param context The context.
	 * @param role    The role of the player.
	 * 
	 * @return The ids of the real players (between 1 and n) corresponding to the roleType.
	 */
	public static TIntArrayList getIdRealPlayers(final Context context, final RoleType role)
	{
		final TIntArrayList idPlayers = new TIntArrayList();

		switch (role)
		{
		case All:
			for (int pid = 1; pid < context.game().players().size(); ++pid)
				idPlayers.add(pid);
			break;
		case Enemy:
			if (context.game().requiresTeams())
			{
				final int teamMover = context.state().getTeam(context.state().mover());
				for (int pid = 1; pid < context.game().players().size(); ++pid)
					if (pid != context.state().mover() && !context.state().playerInTeam(pid, teamMover))
						idPlayers.add(pid);
			}
			else
			{
				for (int pid = 1; pid < context.game().players().size(); ++pid)
					if (pid != context.state().mover())
						idPlayers.add(pid);
			}
			break;
		case Ally:
			if (context.game().requiresTeams())
			{
				final int teamMover = context.state().getTeam(context.state().mover());
				for (int pid = 1; pid < context.game().players().size(); ++pid)
					if (pid != context.state().mover() && context.state().playerInTeam(pid, teamMover))
						idPlayers.add(pid);
			}
			break;
		case Friend:
			if (context.game().requiresTeams())
			{
				final int teamMover = context.state().getTeam(context.state().mover());
				for (int pid = 1; pid < context.game().players().size(); ++pid)
					if (context.state().playerInTeam(pid, teamMover))
						idPlayers.add(pid);
			}
			else
				idPlayers.add(context.state().mover());
			break;
		case NonMover:
			for (int pid = 1; pid < context.game().players().size(); ++pid)
				if (pid != context.state().mover())
					idPlayers.add(pid);
			break;
		case Mover:
			idPlayers.add(context.state().mover());
			break;
		case Next:
			idPlayers.add(context.state().next());
			break;
		case Prev:
			idPlayers.add(context.state().prev());
			break;
		case P1:
			idPlayers.add(1);
			break;
		case P2:
			idPlayers.add(2);
			break;
		case P3:
			idPlayers.add(3);
			break;
		case P4:
			idPlayers.add(4);
			break;
		case P5:
			idPlayers.add(5);
			break;
		case P6:
			idPlayers.add(6);
			break;
		case P7:
			idPlayers.add(7);
			break;
		case P8:
			idPlayers.add(8);
			break;
		case P9:
			idPlayers.add(9);
			break;
		case P10:
			idPlayers.add(0);
			break;
		case P11:
			idPlayers.add(11);
			break;
		case P12:
			idPlayers.add(12);
			break;
		case P13:
			idPlayers.add(13);
			break;
		case P14:
			idPlayers.add(14);
			break;
		case P15:
			idPlayers.add(15);
			break;
		case P16:
			idPlayers.add(16);
			break;
		case Player:
			idPlayers.add(context.player());
			break;
		case Team1:
			if (context.game().requiresTeams())
			{
				for (int pid = 1; pid < context.game().players().size(); ++pid)
					if (context.state().playerInTeam(pid, 1))
						idPlayers.add(pid);
			}
			else
				idPlayers.add(1);
			break;
		case Team2:
			if (context.game().requiresTeams())
			{
				for (int pid = 1; pid < context.game().players().size(); ++pid)
					if (context.state().playerInTeam(pid, 2))
						idPlayers.add(pid);
			}
			else
				idPlayers.add(2);
			break;
		case Team3:
			if (context.game().requiresTeams())
			{
				for (int pid = 1; pid < context.game().players().size(); ++pid)
					if (context.state().playerInTeam(pid, 3))
						idPlayers.add(pid);
			}
			else
				idPlayers.add(3);
			break;
		case Team4:
			if (context.game().requiresTeams())
			{
				for (int pid = 1; pid < context.game().players().size(); ++pid)
					if (context.state().playerInTeam(pid, 4))
						idPlayers.add(pid);
			}
			else
				idPlayers.add(4);
			break;
		case Team5:
			if (context.game().requiresTeams())
			{
				for (int pid = 1; pid < context.game().players().size(); ++pid)
					if (context.state().playerInTeam(pid, 5))
						idPlayers.add(pid);
			}
			else
				idPlayers.add(5);
			break;
		case Team6:
			if (context.game().requiresTeams())
			{
				for (int pid = 1; pid < context.game().players().size(); ++pid)
					if (context.state().playerInTeam(pid, 6))
						idPlayers.add(pid);
			}
			else
				idPlayers.add(6);
			break;
		case Team7:
			if (context.game().requiresTeams())
			{
				for (int pid = 1; pid < context.game().players().size(); ++pid)
					if (context.state().playerInTeam(pid, 7))
						idPlayers.add(pid);
			}
			else
				idPlayers.add(7);
			break;
		case Team8:
			if (context.game().requiresTeams())
			{
				for (int pid = 1; pid < context.game().players().size(); ++pid)
					if (context.state().playerInTeam(pid, 8))
						idPlayers.add(pid);
			}
			else
				idPlayers.add(8);
			break;
		case Team9:
			if (context.game().requiresTeams())
			{
				for (int pid = 1; pid < context.game().players().size(); ++pid)
					if (context.state().playerInTeam(pid, 9))
						idPlayers.add(pid);
			}
			else
				idPlayers.add(9);
			break;
		case Team10:
			if (context.game().requiresTeams())
			{
				for (int pid = 1; pid < context.game().players().size(); ++pid)
					if (context.state().playerInTeam(pid, 10))
						idPlayers.add(pid);
			}
			else
				idPlayers.add(10);
			break;
		case Team11:
			if (context.game().requiresTeams())
			{
				for (int pid = 1; pid < context.game().players().size(); ++pid)
					if (context.state().playerInTeam(pid, 11))
						idPlayers.add(pid);
			}
			else
				idPlayers.add(11);
			break;
		case Team12:
			if (context.game().requiresTeams())
			{
				for (int pid = 1; pid < context.game().players().size(); ++pid)
					if (context.state().playerInTeam(pid, 12))
						idPlayers.add(pid);
			}
			else
				idPlayers.add(12);
			break;
		case Team13:
			if (context.game().requiresTeams())
			{
				for (int pid = 1; pid < context.game().players().size(); ++pid)
					if (context.state().playerInTeam(pid, 13))
						idPlayers.add(pid);
			}
			else
				idPlayers.add(13);
			break;
		case Team14:
			if (context.game().requiresTeams())
			{
				for (int pid = 1; pid < context.game().players().size(); ++pid)
					if (context.state().playerInTeam(pid, 14))
						idPlayers.add(pid);
			}
			else
				idPlayers.add(14);
			break;
		case Team15:
			if (context.game().requiresTeams())
			{
				for (int pid = 1; pid < context.game().players().size(); ++pid)
					if (context.state().playerInTeam(pid, 15))
						idPlayers.add(pid);
			}
			else
				idPlayers.add(15);
			break;
		case Team16:
			if (context.game().requiresTeams())
			{
				for (int pid = 1; pid < context.game().players().size(); ++pid)
					if (context.state().playerInTeam(pid, 16))
						idPlayers.add(pid);
			}
			else
				idPlayers.add(16);
			break;
		case TeamMover:
			if (context.game().requiresTeams())
			{
				final int teamMover = context.state().getTeam(context.state().mover());
				for (int pid = 1; pid < context.game().players().size(); ++pid)
					if (context.state().playerInTeam(pid, teamMover))
						idPlayers.add(pid);
			}
			else
				idPlayers.add(context.state().mover());
			break;
		default:
			break;
		}

		return idPlayers;
	}

	/**
	 * @param context        The context.
	 * @param occupiedByRole The role of the player.
	 * @param occupiedbyId   The specific player in entry.
	 * 
	 * @return The ids of the players corresponding to the roleTypes.
	 */
	public static TIntArrayList getIdPlayers(final Context context, final RoleType occupiedByRole, final int occupiedbyId)
	{
		final TIntArrayList idPlayers = new TIntArrayList();

		if (occupiedByRole != null)
		{
			switch (occupiedByRole)
			{
			case All:
				for (int pid = 0; pid <= context.game().players().size(); ++pid)
					idPlayers.add(pid);
				break;
			case Enemy:
				if (context.game().requiresTeams())
				{
					final int teamMover = context.state().getTeam(context.state().mover());
					for (int pid = 1; pid < context.game().players().size(); ++pid)
						if (pid != context.state().mover() && !context.state().playerInTeam(pid, teamMover))
							idPlayers.add(pid);
				}
				else
				{
					for (int pid = 1; pid < context.game().players().size(); ++pid)
						if (pid != context.state().mover())
							idPlayers.add(pid);
				}
				break;
			case Ally:
				if (context.game().requiresTeams())
				{
					final int teamMover = context.state().getTeam(context.state().mover());
					for (int pid = 1; pid < context.game().players().size(); ++pid)
						if (pid != context.state().mover() && context.state().playerInTeam(pid, teamMover))
							idPlayers.add(pid);
				}
				break;
			case Friend:
				if (context.game().requiresTeams())
				{
					final int teamMover = context.state().getTeam(context.state().mover());
					for (int pid = 1; pid < context.game().players().size(); ++pid)
						if (context.state().playerInTeam(pid, teamMover))
							idPlayers.add(pid);
				}
				else
					idPlayers.add(context.state().mover());
				break;
			case Mover:
				idPlayers.add(context.state().mover());
				break;
			case Next:
				idPlayers.add(context.state().next());
				break;
			case Prev:
				idPlayers.add(context.state().prev());
				break;
			case NonMover:
				for (int pid = 0; pid < context.game().players().size(); ++pid)
					if (pid != context.state().mover())
						idPlayers.add(pid);
				break;
			case Team1:
				if (context.game().requiresTeams())
				{
					for (int pid = 1; pid < context.game().players().size(); ++pid)
						if (context.state().playerInTeam(pid, 1))
							idPlayers.add(pid);
				}
				else
					idPlayers.add(1);
				break;
			case Team2:
				if (context.game().requiresTeams())
				{
					for (int pid = 1; pid < context.game().players().size(); ++pid)
						if (context.state().playerInTeam(pid, 2))
							idPlayers.add(pid);
				}
				else
					idPlayers.add(2);
				break;
			case Team3:
				if (context.game().requiresTeams())
				{
					for (int pid = 1; pid < context.game().players().size(); ++pid)
						if (context.state().playerInTeam(pid, 3))
							idPlayers.add(pid);
				}
				else
					idPlayers.add(3);
				break;
			case Team4:
				if (context.game().requiresTeams())
				{
					for (int pid = 1; pid < context.game().players().size(); ++pid)
						if (context.state().playerInTeam(pid, 4))
							idPlayers.add(pid);
				}
				else
					idPlayers.add(4);
				break;
			case Team5:
				if (context.game().requiresTeams())
				{
					for (int pid = 1; pid < context.game().players().size(); ++pid)
						if (context.state().playerInTeam(pid, 5))
							idPlayers.add(pid);
				}
				else
					idPlayers.add(5);
				break;
			case Team6:
				if (context.game().requiresTeams())
				{
					for (int pid = 1; pid < context.game().players().size(); ++pid)
						if (context.state().playerInTeam(pid, 6))
							idPlayers.add(pid);
				}
				else
					idPlayers.add(6);
				break;
			case Team7:
				if (context.game().requiresTeams())
				{
					for (int pid = 1; pid < context.game().players().size(); ++pid)
						if (context.state().playerInTeam(pid, 7))
							idPlayers.add(pid);
				}
				else
					idPlayers.add(7);
				break;
			case Team8:
				if (context.game().requiresTeams())
				{
					for (int pid = 1; pid < context.game().players().size(); ++pid)
						if (context.state().playerInTeam(pid, 8))
							idPlayers.add(pid);
				}
				else
					idPlayers.add(8);
				break;
			case Team9:
				if (context.game().requiresTeams())
				{
					for (int pid = 1; pid < context.game().players().size(); ++pid)
						if (context.state().playerInTeam(pid, 9))
							idPlayers.add(pid);
				}
				else
					idPlayers.add(9);
				break;
			case Team10:
				if (context.game().requiresTeams())
				{
					for (int pid = 1; pid < context.game().players().size(); ++pid)
						if (context.state().playerInTeam(pid, 10))
							idPlayers.add(pid);
				}
				else
					idPlayers.add(10);
				break;
			case Team11:
				if (context.game().requiresTeams())
				{
					for (int pid = 1; pid < context.game().players().size(); ++pid)
						if (context.state().playerInTeam(pid, 11))
							idPlayers.add(pid);
				}
				else
					idPlayers.add(11);
				break;
			case Team12:
				if (context.game().requiresTeams())
				{
					for (int pid = 1; pid < context.game().players().size(); ++pid)
						if (context.state().playerInTeam(pid, 12))
							idPlayers.add(pid);
				}
				else
					idPlayers.add(12);
				break;
			case Team13:
				if (context.game().requiresTeams())
				{
					for (int pid = 1; pid < context.game().players().size(); ++pid)
						if (context.state().playerInTeam(pid, 13))
							idPlayers.add(pid);
				}
				else
					idPlayers.add(13);
				break;
			case Team14:
				if (context.game().requiresTeams())
				{
					for (int pid = 1; pid < context.game().players().size(); ++pid)
						if (context.state().playerInTeam(pid, 14))
							idPlayers.add(pid);
				}
				else
					idPlayers.add(14);
				break;
			case Team15:
				if (context.game().requiresTeams())
				{
					for (int pid = 1; pid < context.game().players().size(); ++pid)
						if (context.state().playerInTeam(pid, 15))
							idPlayers.add(pid);
				}
				else
					idPlayers.add(15);
				break;
			case Team16:
				if (context.game().requiresTeams())
				{
					for (int pid = 1; pid < context.game().players().size(); ++pid)
						if (context.state().playerInTeam(pid, 16))
							idPlayers.add(pid);
				}
				else
					idPlayers.add(16);
				break;
			case TeamMover:
				if (context.game().requiresTeams())
				{
					final int teamMover = context.state().getTeam(context.state().mover());
					for (int pid = 1; pid < context.game().players().size(); ++pid)
						if (context.state().playerInTeam(pid, teamMover))
							idPlayers.add(pid);
				}
				else
					idPlayers.add(context.state().mover());
				break;
			default:
				idPlayers.add(occupiedbyId);
				break;
			}
		}
		else
			idPlayers.add(occupiedbyId);

		return idPlayers;
	}

}
