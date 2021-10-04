package processing.grammarGMLComposer;

public interface GMLComposerSettingsGrammarToken
{
	
	public default String getOutLineColorFromMover(final int mover, final Node nodeState)
	{
			if (mover==1)return "#00FFEE";
			
			if (mover==2)return "#00FFEE";
			return "#00FFAA";
	}

	public default String getFillColorFromMover(final int mover, final Node nodeState)
	{
			if (mover == 1||mover == 2) return "#CCFF00";
			return "#CCFFFF"; 

	}

	public default String getShapeFromMover(final int playerToMove, final Node nodeState)
	{
			
			if (playerToMove==1)return "rectangle";
			if (playerToMove==2)return "circle";
			return "roundrectangle"; //diamond
		
	}

	public static GMLComposerSettingsGrammarToken getDefaultInstance()
	{

		return new GMLComposerSettingsGrammarToken()
		{

			
		};
	}

	
	public default float getHeight(final Node nodeState, final int lines) {
		return 16.f*lines;
	}

	public default float getWidth(final Node nodeState) {
		return 30.f;
		
	}

	public default String getEdgeColor(final String edge) {
		
		
		
		return "#000000";
		
		
	}

	public default int getEdgeThickness(final String edge) {
		return 1;
	}

	public default String getName() {
		return "default";
	}

	
	
	
	public default String getNodeLabel(final Node nodeState) {
		return nodeState.getLabel();
	}
	
}
