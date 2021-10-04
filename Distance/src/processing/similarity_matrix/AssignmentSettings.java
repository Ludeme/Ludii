package processing.similarity_matrix;

import common.ClassGroundTruth;

public class AssignmentSettings
{
	
	private final int knearestNeighbour;
	private final int knearestNeighbourNoise;
	private final ClassGroundTruth classGroundTruth;

	public AssignmentSettings(final int knearestNeighbour, final int knearestNeighbourNoise,final ClassGroundTruth cgt)
	{
		this.knearestNeighbour = knearestNeighbour;
		this.knearestNeighbourNoise = knearestNeighbourNoise;
		this.classGroundTruth = cgt;
	}
	
	public AssignmentSettings(final int knearestNeighbour, final int knearestNeighbourNoise)
	{
		this.knearestNeighbour = knearestNeighbour;
		this.knearestNeighbourNoise = knearestNeighbourNoise;
		this.classGroundTruth = ClassGroundTruth.getFolderAssignment();
	}

	public int getNearestNeighbour()
	{
		return knearestNeighbour;
	}

	public int getNearestNeighbourNoise()
	{
		return knearestNeighbourNoise;
	}

	public ClassGroundTruth getClassGroundTruth()
	{
		return classGroundTruth;
	}

}
