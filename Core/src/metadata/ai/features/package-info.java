/**
 * The {\tt features} package includes information about features used to bias Monte Carlo playouts. 
 * Each feature describes a geometric pattern of pieces that is relevant to the game, and recommends 
 * moves to make -- or to not make! -- based on the current game state. 
 * Biasing random playouts to encourage good moves that intelligent humans would make, and discourage 
 * moves that they would not make, leads to more realistic playouts and stronger AI play. 
 * 
 * For example, a game in which players aim to make line of 5 of their pieces might benefit from a 
 * feature that encourages the formation of open-ended lines of 4. 
 * Each feature represents a simple strategy relevant to the game. 
 */
package metadata.ai.features;
