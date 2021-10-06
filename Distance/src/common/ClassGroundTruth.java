package common;

import java.util.ArrayList;
import java.util.HashMap;

public interface ClassGroundTruth
{

	public static ClassGroundTruth getFolderAssignment()
	{
		return new ClassGroundTruth() {

			@Override
			public String getClass(final LudRul value)
			{
				return value.getCurrentClassName();
			}
		};
	}

	public String getClass(LudRul value);

	public default HashMap<LudRul, String> getClass(final ArrayList<LudRul> candidates){
		final HashMap<LudRul, String> groundTruth = new HashMap<>(candidates.size());
		for (final LudRul candidate : candidates)
		{
			groundTruth.put(candidate,getClass(candidate));
		}
		return groundTruth;
	}
	

}
