package other.context;

import game.equipment.container.Container;
import game.rules.play.moves.BaseMoves;
import game.rules.play.moves.Moves;
import game.types.board.SiteType;
import other.state.container.ContainerState;

/**
 * Return the context as it should be seen for a player for games with hidden
 * information.
 * 
 * @author Eric.Piette
 */
public class InformationContext extends Context
{
	/** The id of the player point of view. */
	final int playerPointOfView;

	/** Context with all info to compute the legal moves. */
	final Context originalContext;

	// -------------------------------------------------------------------------

	/**
	 * @param context The real context.
	 * @param player The id of the player point of view.
	 */
	public InformationContext(final Context context, final int player)
	{
		super(context);
		
		this.playerPointOfView = player;
		this.originalContext = new Context(context);

		if (context.game().hiddenInformation() && player >= 1 && player <= context.game().players().count())
		{
			// All players have now the same information of the one in entry.
			for (int cid = 0; cid < this.state().containerStates().length; cid++)
			{
				final ContainerState cs = this.state().containerStates()[cid];
				final Container container = this.containers()[cid];
				if (this.game().isCellGame())
				{
					for (int cellId = this.sitesFrom()[cid]; cellId < this.sitesFrom()[cid]
							+ container.topology().cells().size(); cellId++)
					{
						for (int levelId = 0; levelId < cs.sizeStack(cellId, SiteType.Cell); levelId++)
						{
							final boolean isHidden = cs.isHidden(player, cellId, levelId, SiteType.Cell);
							final boolean isHiddenWhat = cs.isHiddenWhat(player, cellId, levelId, SiteType.Cell);
							final boolean isHiddenWho = cs.isHiddenWho(player, cellId, levelId, SiteType.Cell);
							final boolean isHiddenState = cs.isHiddenState(player, cellId, levelId, SiteType.Cell);
							final boolean isHiddenRotation = cs.isHiddenRotation(player, cellId, levelId, SiteType.Cell);
							final boolean isHiddenValue = cs.isHiddenValue(player, cellId, levelId, SiteType.Cell);
							final boolean isHiddenCount = cs.isHiddenCount(player, cellId, levelId, SiteType.Cell);

							for (int pid = 1; pid < this.game().players().size(); pid++)
							{
								cs.setHidden(this.state(), pid, cellId, levelId, SiteType.Cell, isHidden);
								cs.setHiddenWhat(this.state(), pid, cellId, levelId, SiteType.Cell, isHiddenWhat);
								cs.setHiddenWho(this.state(), pid, cellId, levelId, SiteType.Cell, isHiddenWho);
								cs.setHiddenState(this.state(), pid, cellId, levelId, SiteType.Cell, isHiddenState);
								cs.setHiddenRotation(this.state(), pid, cellId, levelId, SiteType.Cell, isHiddenRotation);
								cs.setHiddenValue(this.state(), pid, cellId, levelId, SiteType.Cell, isHiddenValue);
								cs.setHiddenCount(this.state(), pid, cellId, levelId, SiteType.Cell, isHiddenCount);
							}
						}
					}
				}
			}

			if (context.game().isStacking())
			{
				for (int cid = 0; cid < this.state().containerStates().length; cid++)
				{
					final ContainerState cs = this.state().containerStates()[cid];
					final Container container = this.containers()[cid];
					if (this.game().isCellGame())
					{
						for (int cellId = this.sitesFrom()[cid]; cellId < this.sitesFrom()[cid]
								+ container.topology().cells().size(); cellId++)
						{
							for (int levelId = 0; levelId < cs.sizeStack(cellId, SiteType.Cell); levelId++)
							{
								if (cs.isHidden(player, cellId, levelId, SiteType.Cell))
									cs.setSite(this.state(), cellId, levelId, 0, 0, 0, 0, 0, 0);
								else
								{
									final int what = cs.isHiddenWhat(player, cellId, levelId, SiteType.Cell) 
											? 0
											: cs.what(cellId, levelId, SiteType.Cell);
									final int who = cs.isHiddenWho(player, cellId, levelId, SiteType.Cell) 
											? 0
											: cs.who(cellId, levelId, SiteType.Cell);
									final int stateValue = cs.isHiddenState(player, cellId, levelId, SiteType.Cell) 
											? 0
											: cs.state(cellId, levelId, SiteType.Cell);
									final int value = cs.isHiddenValue(player, cellId, levelId, SiteType.Cell) 
											? 0
											: cs.value(cellId, levelId, SiteType.Cell);
									final int rotation = cs.isHiddenRotation(player, cellId, levelId, SiteType.Cell) 
											? 0
											: cs.rotation(cellId, levelId, SiteType.Cell);
									cs.remove(this.state(), cellId, levelId, SiteType.Cell);
									cs.insertCell(this.state(), cellId, levelId, what, who, stateValue, rotation,
											value,
											this.game());
								}
							}
						}
					}
					
					if (cid == 0)
					{
						if (this.game().isVertexGame())
						{
							for (int vertexId = 0; vertexId < container.topology().vertices().size(); vertexId++)
							{
								for (int levelId = 0; levelId < cs.sizeStack(vertexId, SiteType.Vertex); levelId++)
								{
									if (cs.isHidden(player, cid, vertexId, SiteType.Vertex))
										cs.setSite(this.state(), vertexId, levelId, 0, 0, 0, 0, 0, 0);
									else
									{
										final int what = cs.isHiddenWhat(player, vertexId, levelId, SiteType.Vertex) 
												? 0
												: cs.what(vertexId, levelId, SiteType.Vertex);
										final int who = cs.isHiddenWho(player, vertexId, levelId, SiteType.Vertex) 
												? 0
												: cs.who(vertexId, levelId, SiteType.Vertex);
										final int stateValue = cs.isHiddenState(player, vertexId, levelId,
												SiteType.Vertex) 
												? 0 
												: cs.state(vertexId, levelId, SiteType.Vertex);
										final int value = cs.isHiddenValue(player, vertexId, levelId, SiteType.Vertex) 
												? 0
												: cs.value(vertexId, levelId, SiteType.Vertex);
										final int rotation = cs.isHiddenRotation(player, vertexId, levelId,
												SiteType.Vertex) 
												? 0 
												: cs.rotation(vertexId, levelId, SiteType.Vertex);
										cs.remove(this.state(), vertexId, levelId, SiteType.Vertex);
										cs.insertVertex(this.state(), vertexId, levelId, what, who, stateValue,
												rotation, value, this.game());
									}
								}
							}
						}

						if (this.game().isEdgeGame())
						{
							for (int edgeId = 0; edgeId < container.topology().edges().size(); edgeId++)
							{
								for (int levelId = 0; levelId < cs.sizeStack(edgeId, SiteType.Edge); levelId++)
								{
									if (cs.isHidden(player, cid, edgeId, SiteType.Edge))
										cs.setSite(this.state(), edgeId, levelId, 0, 0, 0, 0, 0, 0);
									else
									{
										final int what = cs.isHiddenWhat(player, edgeId, levelId, SiteType.Edge) 
												? 0
												: cs.what(edgeId, levelId, SiteType.Edge);
										final int who = cs.isHiddenWho(player, edgeId, levelId, SiteType.Edge) 
												? 0
												: cs.who(edgeId, levelId, SiteType.Edge);
										final int stateValue = cs.isHiddenState(player, edgeId, levelId, SiteType.Edge)
												? 0
												: cs.state(edgeId, levelId, SiteType.Edge);
										final int value = cs.isHiddenValue(player, edgeId, levelId, SiteType.Edge) 
												? 0
												: cs.value(edgeId, levelId, SiteType.Edge);
										final int rotation = cs.isHiddenRotation(player, edgeId, levelId, SiteType.Edge)
												? 0
												: cs.rotation(edgeId, levelId, SiteType.Edge);
										cs.remove(this.state(), edgeId, levelId, SiteType.Edge);
										cs.insertEdge(this.state(), edgeId, levelId, what, who, stateValue, rotation,
												value, this.game());
									}
								}
							}
						}
					}
				}
			}
			else
			{
				for (int cid = 0; cid < this.state().containerStates().length; cid++)
				{
					final ContainerState cs = this.state().containerStates()[cid];
					final Container container = this.containers()[cid];

					if (this.game().isCellGame())
					{
						for (int cellId = this.sitesFrom()[cid]; cellId < this.sitesFrom()[cid]
								+ container.topology().cells().size(); cellId++)
						{
							final boolean wasEmpty = cs.isEmpty(cellId, SiteType.Cell);

							// System.out.println("was Empty = " + wasEmpty);

							if (cs.isHidden(player, cellId, 0, SiteType.Cell))
								cs.setSite(this.state(), cellId, 0, 0, 0, 0, 0, 0, SiteType.Cell);
							else
							{
								final int what = cs.isHiddenWhat(player, cellId, 0, SiteType.Cell) 
										? 0
										: cs.what(cellId, SiteType.Cell);
								final int who = cs.isHiddenWho(player, cellId, 0, SiteType.Cell) 
										? 0
										: cs.who(cellId, SiteType.Cell);
								final int stateValue = cs.isHiddenState(player, cellId, 0, SiteType.Cell) 
										? 0
										: cs.state(cellId, SiteType.Cell);
								final int value = cs.isHiddenValue(player, cellId, 0, SiteType.Cell) 
										? 0
										: cs.value(cellId, SiteType.Cell);
								final int rotation = cs.isHiddenRotation(player, cellId, 0, SiteType.Cell) 
										? 0
										: cs.rotation(cellId, SiteType.Cell);
								final int count = cs.isHiddenCount(player, cellId, 0, SiteType.Cell) 
										? 0
										: cs.count(cellId, SiteType.Cell);
								cs.setSite(this.state(), cellId, who, what, count, stateValue, rotation, value,
										SiteType.Cell);
							}

							final boolean isEmpty = cs.isEmpty(cellId, SiteType.Cell);

							// System.out.println("is Empty = " + isEmpty);

							// We keep the empty info.
							if (!wasEmpty && isEmpty)
								cs.removeFromEmpty(cellId, SiteType.Cell);

							// System.out.println("so empty value is " + cs.isEmpty(cellId, SiteType.Cell));

						}
					}

					if (cid == 0)
					{
						if (this.game().isVertexGame())
						{
							for (int vertexId = 0; vertexId < container.topology().vertices().size(); vertexId++)
							{
								final boolean wasEmpty = cs.isEmpty(vertexId, SiteType.Vertex);

								if (cs.isHidden(player, vertexId, 0, SiteType.Vertex))
									cs.setSite(this.state(), vertexId, 0, 0, 0, 0, 0, 0, SiteType.Vertex);
								else
								{
									final int what = cs.isHiddenWhat(player, vertexId, 0, SiteType.Vertex) 
											? 0
											: cs.what(vertexId, SiteType.Vertex);
									final int who = cs.isHiddenWho(player, vertexId, 0, SiteType.Vertex) 
											? 0
											: cs.who(vertexId, SiteType.Vertex);
									final int stateValue = cs.isHiddenState(player, vertexId, 0, SiteType.Vertex) 
											? 0
											: cs.state(vertexId, SiteType.Vertex);
									final int value = cs.isHiddenValue(player, vertexId, 0, SiteType.Vertex) 
											? 0
											: cs.value(vertexId, SiteType.Vertex);
									final int rotation = cs.isHiddenRotation(player, vertexId, 0, SiteType.Vertex) 
											? 0
											: cs.rotation(vertexId, SiteType.Vertex);
									final int count = cs.isHiddenCount(player, vertexId, 0, SiteType.Vertex) 
											? 0
											: cs.count(vertexId, SiteType.Vertex);
									cs.setSite(this.state(), vertexId, who, what, count, stateValue, rotation, value,
											SiteType.Vertex);
								}

								final boolean isEmpty = cs.isEmpty(vertexId, SiteType.Vertex);

								// We keep the empty info.
								if (!wasEmpty && isEmpty)
									cs.removeFromEmpty(vertexId, SiteType.Vertex);
							}
						}

						if (this.game().isEdgeGame())
						{
							for (int edgeId = 0; edgeId < container.topology().edges().size(); edgeId++)
							{
								final boolean wasEmpty = cs.isEmpty(edgeId, SiteType.Edge);

								if (cs.isHidden(player, edgeId, 0, SiteType.Edge))
									cs.setSite(this.state(), edgeId, 0, 0, 0, 0, 0, 0, SiteType.Edge);
								else
								{
									final int what = cs.isHiddenWhat(player, edgeId, 0, SiteType.Edge) 
											? 0
											: cs.what(edgeId, SiteType.Edge);
									final int who = cs.isHiddenWho(player, edgeId, 0, SiteType.Edge) 
											? 0
											: cs.who(edgeId, SiteType.Edge);
									final int stateValue = cs.isHiddenState(player, edgeId, 0, SiteType.Edge) 
											? 0
											: cs.state(edgeId, SiteType.Edge);
									final int value = cs.isHiddenValue(player, edgeId, 0, SiteType.Edge) 
											? 0
											: cs.value(edgeId, SiteType.Edge);
									final int rotation = cs.isHiddenRotation(player, edgeId, 0, SiteType.Edge) 
											? 0
											: cs.rotation(edgeId, SiteType.Edge);
									final int count = cs.isHiddenCount(player, edgeId, 0, SiteType.Edge) 
											? 0
											: cs.count(edgeId, SiteType.Edge);
									cs.setSite(this.state(), edgeId, who, what, count, stateValue, rotation, value,
											SiteType.Edge);
								}

								final boolean isEmpty = cs.isEmpty(edgeId, SiteType.Edge);

								// We keep the empty info.
								if (!wasEmpty && isEmpty)
									cs.removeFromEmpty(edgeId, SiteType.Edge);
							}
						}
					}
				}
			}
		}
	}

	// -------------------------------------------------------------------------

	@Override
	public Moves moves(final Context context)
	{
		if (originalContext.state().mover() == playerPointOfView)
			return originalContext.game().moves(originalContext);

		return new BaseMoves(null);
	}

	@Override
	public int pointofView()
	{
		return pointofView();
	}
}
