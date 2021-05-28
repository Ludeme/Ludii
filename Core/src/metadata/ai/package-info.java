/**
 * @chapter Ludii's {\it artificial intelligence} (AI) agents use hints provided in the {\tt ai} metadata items 
 * to help them play each game effectively.
 * These AI hints can apply to the game as a whole, or be targeted at particular variant rulesets or 
 * combinations of options within each game. 
 * Games benefit from the AI metadata but do not depend upon it; that is, Ludii's default AI agents will still 
 * play each game if no AI metadata is provided, but probably not as well.  
 * 
 * @section The {\tt ai} metadata category collects relevant AI-related information. 
 * This information includes which {\it search algorithm} to use for move planning, what its settings should be, 
 * which {\it heuristics} are most useful, and which {\it features} (i.e. geometric piece patterns) are most important.  
 */
package metadata.ai;
