package metadata;

import java.io.Serializable;
import java.util.BitSet;

import annotations.Hide;
import annotations.Opt;
import game.Game;
import metadata.ai.Ai;
import metadata.graphics.Graphics;
import metadata.info.Info;
import metadata.recon.Recon;

/**
 * The metadata of a game.
 * 
 * @author cambolbro and Eric.Piette and Dennis Soemers and Matthew.Stephenson
 */
public class Metadata implements MetadataItem, Serializable
{
	private static final long serialVersionUID = 1L;

	// -----------------------------------------------------------------------

	/** The info metadata. */
	private final Info info;
	
	/** Our graphics related metadata. */
	private final Graphics graphics;
	
	/** Our AI-related metadata (heuristics, features, etc.). */
	private final Ai ai;
	
	/** Our Recon-related metadata (heuristics, features, etc.). */
	private final Recon recon;
	
	//-----------------------------------------------------------------------

	/**
	 * @param info     The info metadata.
	 * @param graphics The graphics metadata.
	 * @param ai       Metadata for AIs playing this game.
	 * @param recon    The metadata related to reconstruction.
	 * 
	 * @example (metadata (info { (description "Description of The game") (source
	 *          "Source of the game") (version "1.0.0") (classification
	 *          "board/space/territory") (origin "Origin of the game.") }) (graphics
	 *          { (board Style Go) (player Colour P1 (colour Black)) (player Colour
	 *          P2 (colour White)) }) (ai (bestAgent "UCT")) )
	 */
	public Metadata
	(
		@Opt final Info info,
		@Opt final Graphics graphics,
		@Opt final Ai ai,
		@Opt final Recon recon 
	)
	{
		// Set info metadata.
		if (info != null)
		{
			this.info  = info;
		}
		else
		{
			// Initialise an empty info metadata object if non specified.
			this.info = new Info(null, null);
		}
		
		// Set graphics metadata.
		if (graphics != null)
		{
			this.graphics  = graphics;
		}
		else
		{
			// Initialise an empty graphics metadata object if non specified.
			this.graphics = new Graphics(null, null);
		}
		
		// Set AI metadata.
		if (ai != null)
			this.ai = ai;
		else
			this.ai = new Ai(null, null, null, null, null, null);
		
		// Set Recon metadata.
		if (recon != null)
			this.recon = recon;
		else
			this.recon = new Recon(null, null);
	}

	/**
	 * Default constructor for Compiler to call.
	 */
	@Hide
	public Metadata()
	{
		this.info 	  = new Info(null, null);
		this.graphics = new Graphics(null, null);
		this.ai 	  = new Ai(null, null, null, null, null, null);	
		this.recon 	  = new Recon(null, null);	
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return The info metadata.
	 */
	public Info info()
	{
		return info;
	}
	
	/**
	 * @return The graphics metadata.
	 */
	public Graphics graphics()
	{
		return graphics;
	}
	
	/**
	 * @return Our AI metadata
	 */
	public Ai ai()
	{
		return ai;
	}
	
	/**
	 * @return Our Recon metadata
	 */
	public Recon recon()
	{
		return recon;
	}
	
	//-------------------------------------------------------------------------

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		
		sb.append("(metadata\n");

		if (info != null)
			sb.append(info.toString());
		if (graphics != null)
			sb.append(graphics.toString());
		if (ai != null)
			sb.append(ai.toString());
		if (recon != null)
			sb.append(recon.toString());
		
		sb.append(")\n");
		
		return sb.toString();
	}

	//-------------------------------------------------------------------------

	/**
	 * @param game The game.
	 * @return Accumulated concepts.
	 */
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(graphics.concepts(game));
		return concepts;
	}

	/**
	 * @param game The game.
	 * @return Accumulated game flags.
	 */
	public long gameFlags(final Game game)
	{
		long gameFlags = 0l;
		gameFlags |= graphics.gameFlags(game);
		return gameFlags;
	}

}
