package app.utils;

/**
 * Different ways that moves can be animated
 * 
 * @author Matthew.Stephenson
 */
public enum AnimationVisualsType 
{
	
	None, 		// No piece animation.
	Single,		// Single action animation.
	All;		// All action animation.
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns the AnimationVisualsType who's value matches the provided name.
	 */
	public static AnimationVisualsType getAnimationVisualsType(final String name)
	{
        for (final AnimationVisualsType animationVisualsType : AnimationVisualsType.values())
            if (animationVisualsType.name().equals(name)) 
            	return animationVisualsType;
        
        return None;
    }

	//-------------------------------------------------------------------------
	
}
