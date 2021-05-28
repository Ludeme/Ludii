/**
 * The {\it players} of a game are the entities that compete within the game according to its rules. 
 * Players can be:
 * 
 * \begin{itemize}
 * \item {\it Human}: i.e. you!
 * \item {\it AI}: Artificial intelligence agents.
 * \item {\it Remote}: Remote players over a network, which may be Human or AI.
 * \end{itemize}
 * 
 * Each player has a name and a number according to the default play order. 
 * The {\tt Neutral} player (index 0) is used to denote equipment that belongs to no player, 
 * and to make moves associated with the environment rather than any actual player. 
 * The {\tt Shared} player (index $N+1$ where $N$ is the number of players) is used to denote equipment that belongs to all players. 
 * The actual players are denoted {\tt P1}, {\tt P2}, {\tt P3}, ... in game descriptions.
 */
package game.players;
