package game.rules.play.moves.nonDecision.effect.take.control;

import java.util.BitSet;

import annotations.Hide;
import annotations.Name;
import annotations.Opt;
import annotations.Or;
import annotations.Or2;
import game.Game;
import game.equipment.component.Component;
import game.functions.ints.IntFunction;
import game.functions.ints.board.Id;
import game.functions.region.RegionFunction;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.rules.play.moves.nonDecision.effect.Effect;
import game.rules.play.moves.nonDecision.effect.Then;
import game.types.board.SiteType;
import game.types.play.RoleType;
import gnu.trove.list.array.TIntArrayList;
import main.Constants;
import other.IntArrayFromRegion;
import other.action.move.ActionAdd;
import other.action.move.ActionRemove;
import other.concept.Concept;
import other.context.Context;
import other.move.Move;
import other.state.container.ContainerState;

/**
 * Modifies the owner of some pieces on the board.
 * 
 * @author Eric.Piette
 * 
 * @remarks To modify the owner, modify the index of the piece to the equivalent
 *          of the other player. To work, an equivalent piece for the player who
 *          has to take the control must exist. For example for Ploy 4P.
 */
@Hide
public final class TakeControl extends Effect
{
	private static final long serialVersionUID = 1L;

	//-------------------------------------------------------------------------

	/** The new owner. */
	private final IntFunction newOwnerFn;

	/** The role used for the new owner. */
	private final RoleType newOwnerRole;

	/** The current owner. */
	private final IntFunction ownerFn;

	/** The role used for the current owner. */
	private final RoleType ownerRole;

	/** The region to take the control. */
	private final IntArrayFromRegion region;

	/** Cell/Edge/Vertex. */
	private SiteType type;

	//-------------------------------------------------------------------------

	/**
	 * @param of   The roleType of the pieces to take control of.
	 * @param Of   The player index of the pieces to take control of.
	 * @param by   The roleType taking the control.
	 * @param By   The player index of the player taking control.
	 * @param at   The site to take the control.
	 * @param to   The region to take the control.
	 * @param type The graph element type [default SiteType of the board].
	 * @param then The moves applied after that move is applied.
	 */
	public TakeControl
	(
		     @Or  @Name final RoleType       of,
		     @Or  @Name final IntFunction    Of,
		     @Or2 @Name final RoleType       by,
		     @Or2 @Name final IntFunction    By,
		@Opt @Or  @Name final IntFunction    at,
		@Opt @Or  @Name final RegionFunction to,
		@Opt            final SiteType       type,
		@Opt            final Then           then
	)
	{
		super(then);
		newOwnerFn = by != null ? RoleType.toIntFunction(by) : By;
		ownerFn = of != null ? new Id(null, of) : Of;
		this.type = type;
		newOwnerRole = by;
		ownerRole = of;
		region = (at == null && to == null) ? null : new IntArrayFromRegion(at, to);
	}

	//-------------------------------------------------------------------------

	@Override
	public Moves eval(final Context context)
	{
		// Return intersected list of moves
		final Moves moves = new BaseMoves(super.then());

		final int newOwner = newOwnerFn.eval(context);
		final int owner = ownerFn.eval(context);
		
		final TIntArrayList ownedSites = new TIntArrayList();

		if (ownerRole.equals(RoleType.All))
		{
			for (int pid = 0; pid <= context.players().size(); pid++)
				ownedSites.addAll(context.state().owned().sites(pid));
		}
		else
			ownedSites.addAll(context.state().owned().sites(owner));
		
		// We look only the sites which are in the region if a region is defined.
		if (region != null)
		{
			final TIntArrayList sites = new TIntArrayList(region.eval(context));
			for (int i = ownedSites.size() - 1; i >= 0; i--)
			{
				final int ownedSite = ownedSites.get(i);
				if (!sites.contains(ownedSite))
					ownedSites.removeAt(i);
			}
		}

		for (int i = 0; i < ownedSites.size(); i++)
		{
			final int site = ownedSites.getQuick(i);
			final ContainerState cs = context.containerState(context.containerId()[site]);
			final int what = cs.what(site, type);
			final int state = cs.state(site, type);
			final int rotation = cs.rotation(site, type);
			final int value = cs.value(site, type);
			final int count = cs.count(site, type);

			final Component componentOwned = context.components()[what];
			final String name = componentOwned.getNameWithoutNumber();
			int newWhat = Constants.UNDEFINED;
			for (int indexComponent = 1; indexComponent < context.components().length; indexComponent++)
			{
				final Component newComponentOwned = context.components()[indexComponent];
				if (newComponentOwned.owner() == newOwner && newComponentOwned.getNameWithoutNumber().equals(name))
				{
					newWhat = indexComponent;
					break;
				}
			}

			if (newWhat == Constants.UNDEFINED)
				continue;

			final ActionRemove actionRemove = new other.action.move.ActionRemove(type, site, Constants.UNDEFINED, true);
			final ActionAdd actionAdd = new ActionAdd(type, site, newWhat, count, state, rotation, value,
					null);
			final Move move = new Move(actionRemove);
			move.actions().add(actionAdd);
			move.setFromNonDecision(site);
			move.setToNonDecision(site);
			moves.moves().add(move);
		}

		if (then() != null)
			for (int j = 0; j < moves.moves().size(); j++)
				moves.moves().get(j).then().add(then().moves());

		// Store the Moves in the computed moves.
		for (int j = 0; j < moves.moves().size(); j++)
			moves.moves().get(j).setMovesLudeme(this);

		return moves;
	}

	//-------------------------------------------------------------------------

	@Override
	public long gameFlags(final Game game)
	{
		long gameFlags = super.gameFlags(game);
		
		gameFlags |= newOwnerFn.gameFlags(game);
		gameFlags |= ownerFn.gameFlags(game);
		gameFlags |= SiteType.gameFlags(type);
		if (region != null)
			gameFlags |= region.gameFlags(game);

		if (then() != null)
			gameFlags |= then().gameFlags(game);

		return gameFlags;
	}

	@Override
	public BitSet concepts(final Game game)
	{
		final BitSet concepts = new BitSet();
		concepts.or(SiteType.concepts(type));
		concepts.or(super.concepts(game));
		concepts.or(newOwnerFn.concepts(game));
		concepts.or(ownerFn.concepts(game));
		concepts.set(Concept.TakeControl.id(), true);
		if (region != null)
			concepts.or(region.concepts(game));

		if (then() != null)
			concepts.or(then().concepts(game));

		return concepts;
	}

	@Override
	public BitSet writesEvalContextRecursive()
	{
		final BitSet writeEvalContext = new BitSet();
		writeEvalContext.or(super.writesEvalContextRecursive());
		writeEvalContext.or(newOwnerFn.writesEvalContextRecursive());
		writeEvalContext.or(ownerFn.writesEvalContextRecursive());
		if (then() != null)
			writeEvalContext.or(then().writesEvalContextRecursive());
		if (region != null)
			writeEvalContext.or(region.writesEvalContextRecursive());
		return writeEvalContext;
	}

	@Override
	public BitSet readsEvalContextRecursive()
	{
		final BitSet readEvalContext = new BitSet();
		readEvalContext.or(super.readsEvalContextRecursive());
		readEvalContext.or(newOwnerFn.readsEvalContextRecursive());
		readEvalContext.or(ownerFn.readsEvalContextRecursive());
		if (then() != null)
			readEvalContext.or(then().readsEvalContextRecursive());
		if (region != null)
			readEvalContext.or(region.readsEvalContextRecursive());
		return readEvalContext;
	}

	@Override
	public boolean missingRequirement(final Game game)
	{
		boolean missingRequirement = false;

		// We check if the slave player role is correct.
		if (ownerRole != null)
		{
			final int indexOwnerPhase = ownerRole.owner();
			if (((indexOwnerPhase < 1 && !ownerRole.equals(RoleType.Neutral) && !ownerRole.equals(RoleType.All)
					&& !ownerRole.equals(RoleType.Mover))
					&& !ownerRole.equals(RoleType.Prev)
					&& !ownerRole.equals(RoleType.Next)) || indexOwnerPhase > game.players().count())
			{
				game.addRequirementToReport(
						"In (take Control ...) the RoleType used for the of: parameter is incorrect: " + ownerRole
								+ ".");
				missingRequirement = true;
			}
		}

		// We check if the master player role is correct.
		if (newOwnerRole != null)
		{
			final int indexOwnerPhase = newOwnerRole.owner();
			if (((indexOwnerPhase < 1 && !newOwnerRole.equals(RoleType.Neutral) && !newOwnerRole.equals(RoleType.Mover))
					&& !newOwnerRole.equals(RoleType.Prev)
					&& !newOwnerRole.equals(RoleType.Next)) || indexOwnerPhase > game.players().count())
			{
				game.addRequirementToReport(
						"In (take Control ...) the RoleType used for the by: parameter is incorrect: " + newOwnerRole
								+ ".");
				missingRequirement = true;
			}
		}

		missingRequirement |= super.missingRequirement(game);
		missingRequirement |= newOwnerFn.missingRequirement(game);
		missingRequirement |= ownerFn.missingRequirement(game);
		if (region != null)
			missingRequirement |= region.missingRequirement(game);

		if (then() != null)
			missingRequirement |= then().missingRequirement(game);
		return missingRequirement;
	}

	@Override
	public boolean willCrash(final Game game)
	{
		boolean willCrash = false;
		willCrash |= super.willCrash(game);
		willCrash |= newOwnerFn.willCrash(game);
		willCrash |= ownerFn.willCrash(game);

		if (region != null)
			willCrash |= region.willCrash(game);

		if (then() != null)
			willCrash |= then().willCrash(game);
		return willCrash;
	}

	@Override
	public boolean isStatic()
	{
		return false;
	}

	@Override
	public void preprocess(final Game game)
	{
		type = SiteType.use(type, game);
		super.preprocess(game);
		newOwnerFn.preprocess(game);
		ownerFn.preprocess(game);

		if (region != null)
			region.preprocess(game);
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String toEnglish(final Game game)
	{
		return "take control of the piece on " + type.name() + " " + region.toEnglish(game);
	}
	
	//-------------------------------------------------------------------------

}
