package view.container.aspects.designs.board;

import bridge.Bridge;
import other.context.Context;
import view.container.aspects.designs.BoardDesign;
import view.container.aspects.placement.Board.BoardlessPlacement;
import view.container.styles.board.BoardlessStyle;

public class BoardlessDesign extends BoardDesign
{
	private final BoardlessStyle boardlessStyle;
	private final BoardlessPlacement boardlessPlacement;
	
	//-------------------------------------------------------------------------

	public BoardlessDesign(final BoardlessStyle boardlessStyle, final BoardlessPlacement boardlessPlacement) 
	{
		super(boardlessStyle, boardlessPlacement);
		this.boardlessStyle = boardlessStyle;
		this.boardlessPlacement = boardlessPlacement;
	}

	//-------------------------------------------------------------------------
	
	@Override
	public String createSVGImage(final Bridge bridge, final Context context)
	{
		boardlessPlacement.updateZoomImage(context);
		
		// Board image
		setStrokesAndColours
		(
			bridge, 
			context,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			1,
			1
		);
		return "";
	}

	public BoardlessStyle getBoardlessStyle()
	{
		return boardlessStyle;
	}
}
