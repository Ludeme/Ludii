package features;

import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import features.spatial.Pattern;
import features.spatial.RelativeFeature;
import features.spatial.SpatialFeature;
import features.spatial.Walk;
import features.spatial.elements.FeatureElement.ElementType;
import features.spatial.elements.RelativeFeatureElement;
import features.spatial.instances.FeatureInstance;
import game.Game;
import game.types.board.SiteType;
import other.GameLoader;
import other.context.Context;
import other.state.container.ContainerState;
import other.trial.Trial;

@SuppressWarnings("static-method")
public class TestFeatureCombinations
{
	
	@Test
	public void testA()
	{
		final Game game = GameLoader.loadGameFromName("Breakthrough.lud");

		// active feature A = rel:from=<{}>:pat=<refl=true,rots=all,els=[f{0.0,0.25}]>
		// rot A = 0.25
		// ref A = 1
		// anchor A = 22
		// active feature B = rel:to=<{}>:pat=<refl=true,rots=all,els=[e{0.0,0.0}]>
		// rot B = 0.0
		// ref B = 1
		// anchor B = 29
		
		final Pattern patternA = new Pattern(new RelativeFeatureElement(ElementType.Friend, new Walk(0.f, 0.25f)));
		final SpatialFeature featureA = new RelativeFeature(patternA, null, new Walk());
		final FeatureInstance instanceA = new FeatureInstance(featureA, 22, 1, 0.25f, SiteType.Cell);
		
		final Pattern patternB = new Pattern(new RelativeFeatureElement(ElementType.Enemy, new Walk(0.f, 0.f)));
		final SpatialFeature featureB = new RelativeFeature(patternB, new Walk(), null);
		final FeatureInstance instanceB = new FeatureInstance(featureB, 29, 1, 0.f, SiteType.Cell);
		
		final SpatialFeature newFeature = SpatialFeature.combineFeatures(game, instanceA, instanceB);
		
		final Pattern targetPattern = new Pattern(
				new RelativeFeatureElement(ElementType.Friend, new Walk(0.25f, 0.25f)), 
				new RelativeFeatureElement(ElementType.Enemy, new Walk(0.f, -0.25f, 0.25f, 0.f))
				);
		final SpatialFeature targetFeature = new RelativeFeature(targetPattern, new Walk(0.f, -0.25f), new Walk());
		
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		final ContainerState containerState = context.containerState(0);
		
		final List<FeatureInstance> newInstances = newFeature.instantiateFeature(game, containerState, 1, -1, -1, -1, -1, -1);
		final List<FeatureInstance> targetInstances = targetFeature.instantiateFeature(game, containerState, 1, -1, -1, -1, -1, -1);
		
		if (newInstances.size() != targetInstances.size())
		{
			System.out.println("newFeature = " + newFeature);
			System.out.println("targetFeature = " + targetFeature);
		}
		
		assert(newInstances.size() == targetInstances.size());
		
		for (final FeatureInstance newInstance : newInstances)
		{
			boolean found = false;
			
			for (final FeatureInstance targetInstance : targetInstances)
			{
				if (newInstance.functionallyEquals(targetInstance))
				{
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				fail(newInstance + " was not a target instance!");
			}
		}
		
		for (final FeatureInstance targetInstance : targetInstances)
		{
			boolean found = false;
			
			for (final FeatureInstance newInstance : newInstances)
			{
				if (targetInstance.functionallyEquals(newInstance))
				{
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				fail(targetInstance + " was not a new instance!");
			}
		}
	}
	
	@Test
	public void testB()
	{
		final Game game = GameLoader.loadGameFromName("Breakthrough.lud");
		
		// active feature A = rel:from=<{}>:pat=<refl=true,rots=all,els=[e{0.0,0.0,0.0}]>
		// rot A = 0.75
		// ref A = 1
		// anchor A = 38
		// active feature B = rel:to=<{}>:pat=<refl=true,rots=all,els=[-{0.0,0.0}]>
		// rot B = 0.5
		// ref B = 1
		// anchor B = 45
		
		final Pattern patternA = new Pattern(new RelativeFeatureElement(ElementType.Enemy, new Walk(0.f, 0.f, 0.f)));
		final SpatialFeature featureA = new RelativeFeature(patternA, null, new Walk());
		final FeatureInstance instanceA = new FeatureInstance(featureA, 38, 1, 0.75f, SiteType.Cell);
		
		final Pattern patternB = new Pattern(new RelativeFeatureElement(ElementType.Empty, new Walk(0.f, 0.f)));
		final SpatialFeature featureB = new RelativeFeature(patternB, new Walk(), null);
		final FeatureInstance instanceB = new FeatureInstance(featureB, 45, 1, 0.5f, SiteType.Cell);
		
		final SpatialFeature newFeature = SpatialFeature.combineFeatures(game, instanceA, instanceB);
		
		final Pattern targetPattern = new Pattern(
				new RelativeFeatureElement(ElementType.Enemy, new Walk(-0.25f, 0.f, 0.f)), 
				new RelativeFeatureElement(ElementType.Empty, new Walk(0.f, -0.25f, -0.25f, 0.f))
				);
		final SpatialFeature targetFeature = new RelativeFeature(targetPattern, new Walk(0.f, -0.25f), new Walk());
		
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		final ContainerState containerState = context.containerState(0);
		
		final List<FeatureInstance> newInstances = newFeature.instantiateFeature(game, containerState, 1, -1, -1, -1, -1, -1);
		final List<FeatureInstance> targetInstances = targetFeature.instantiateFeature(game, containerState, 1, -1, -1, -1, -1, -1);
		
		for (final FeatureInstance newInstance : newInstances)
		{
			boolean found = false;
			
			for (final FeatureInstance targetInstance : targetInstances)
			{
				if (newInstance.functionallyEquals(targetInstance))
				{
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				fail(newInstance + " was not a target instance!");
			}
		}
		
		for (final FeatureInstance targetInstance : targetInstances)
		{
			boolean found = false;
			
			for (final FeatureInstance newInstance : newInstances)
			{
				if (targetInstance.functionallyEquals(newInstance))
				{
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				System.out.println("target feature = " + targetFeature);
				System.out.println("new feature = " + newFeature);
				System.out.println("num target instances = " + targetInstances.size());
				System.out.println("num new instances = " + newInstances.size());
				
				fail(targetInstance + " was not a new instance!");
			}
		}
	}
	
	@Test
	public void testC()
	{
		final Game game = GameLoader.loadGameFromName("Amazons.lud");
		
		// active feature A = rel:to=<{}>:pat=<refl=true,rots=all,els=[-{0.0,0.0}]>
		// rot A = 0.75
		// ref A = 1
		// anchor A = 39
		// active feature B = rel:from=<{0.0,0.25}>:to=<{}>:pat=<refl=true,rots=all,els=[!-{0.0,0.0}, !-{0.0,0.25,-0.25,0.25}]>
		// rot B = 0.75
		// ref B = -1
		// anchor B = 39
		
		final Pattern patternA = new Pattern(new RelativeFeatureElement(ElementType.Empty, new Walk(0.f, 0.f)));
		final SpatialFeature featureA = new RelativeFeature(patternA, new Walk(), null);
		final FeatureInstance instanceA = new FeatureInstance(featureA, 39, 1, 0.75f, SiteType.Cell);
		
		final Pattern patternB = new Pattern(
				new RelativeFeatureElement(ElementType.Empty, true, new Walk(0.f, 0.f)),
				new RelativeFeatureElement(ElementType.Empty, true, new Walk(0.f, 0.25f, -0.25f, 0.25f)));
		final SpatialFeature featureB = new RelativeFeature(patternB, new Walk(), new Walk(0.f, 0.25f));
		final FeatureInstance instanceB = new FeatureInstance(featureB, 39, -1, 0.75f, SiteType.Cell);
		
		final SpatialFeature newFeature = SpatialFeature.combineFeatures(game, instanceA, instanceB);
		
		final Pattern targetPattern = new Pattern(
				new RelativeFeatureElement(ElementType.Empty, new Walk(0.f, 0.f)), 
				new RelativeFeatureElement(ElementType.Empty, true, new Walk(0.5f, 0.f)),
				new RelativeFeatureElement(ElementType.Empty, true, new Walk(0.5f, 0.25f, -0.25f, 0.25f))
				);
		final SpatialFeature targetFeature = new RelativeFeature(targetPattern, new Walk(), new Walk(0.5f, 0.25f));
		
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		final ContainerState containerState = context.containerState(0);
		
		final List<FeatureInstance> newInstances = newFeature.instantiateFeature(game, containerState, 1, -1, -1, -1, -1, -1);
		final List<FeatureInstance> targetInstances = targetFeature.instantiateFeature(game, containerState, 1, -1, -1, -1, -1, -1);
		
		if (newInstances.size() != targetInstances.size())
		{
			System.out.println("newFeature = " + newFeature);
			System.out.println("targetFeature = " + targetFeature);
		}
		
		assert(newInstances.size() == targetInstances.size());
		
		for (final FeatureInstance newInstance : newInstances)
		{
			boolean found = false;
			
			for (final FeatureInstance targetInstance : targetInstances)
			{
				if (newInstance.functionallyEquals(targetInstance))
				{
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				System.out.println("newFeature = " + newFeature);
				System.out.println("targetFeature = " + targetFeature);
				
				fail(newInstance + " was not a target instance!");
			}
		}
		
		for (final FeatureInstance targetInstance : targetInstances)
		{
			boolean found = false;
			
			for (final FeatureInstance newInstance : newInstances)
			{
				if (targetInstance.functionallyEquals(newInstance))
				{
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				System.out.println("newFeature = " + newFeature);
				System.out.println("targetFeature = " + targetFeature);
				
				fail(targetInstance + " was not a new instance!");
			}
		}
	}
	
	@Test
	public void testD()
	{
		final Game game = GameLoader.loadGameFromName("Amazons.lud");

		// active feature A = rel:from=<{0.5,-0.25}>:to=<{}>:pat=<refl=true,rots=all,els=[-{0.25,0.0}, -{0.5,0.25,-0.25,0.0,0.0,0.0}, !-{0.0,0.25,1.25,0.0,0.0}]>
		// rot A = 0.0
		// ref A = -1
		// anchor A = 55
		// active feature B = rel:from=<{}>:to=<{0.0,0.25}>:pat=<refl=true,rots=all,els=[!-{-0.25,0.0,0.0,0.0}, -{0.0,0.25,-0.25,0.0,0.0,0.0}]>
		// rot B = 0.75
		// ref B = -1
		// anchor B = 44
		
		final Pattern patternA = new Pattern(
				new RelativeFeatureElement(ElementType.Empty, new Walk(0.25f, 0.f)),
				new RelativeFeatureElement(ElementType.Empty, new Walk(0.5f, 0.25f, -0.25f, 0.f, 0.f, 0.f)),
				new RelativeFeatureElement(ElementType.Empty, true, new Walk(0.f, 0.25f, 1.25f, 0.f, 0.f))
				);
		final SpatialFeature featureA = new RelativeFeature(patternA, new Walk(), new Walk(0.5f, -0.25f));
		final FeatureInstance instanceA = new FeatureInstance(featureA, 55, -1, 0.f, SiteType.Cell);
		
		final Pattern patternB = new Pattern(
				new RelativeFeatureElement(ElementType.Empty, true, new Walk(-0.25f, 0.f, 0.f, 0.f)),
				new RelativeFeatureElement(ElementType.Empty, new Walk(0.f, 0.25f, -0.25f, 0.f, 0.f, 0.f))
				);
		final SpatialFeature featureB = new RelativeFeature(patternB, new Walk(0.f, 0.25f), new Walk());
		final FeatureInstance instanceB = new FeatureInstance(featureB, 44, -1, 0.75f, SiteType.Cell);
		
		final SpatialFeature newFeature = SpatialFeature.combineFeatures(game, instanceA, instanceB);
		
		final Pattern targetPattern = new Pattern(
				new RelativeFeatureElement(ElementType.Empty, new Walk(-0.25f, 0.f)), 
				new RelativeFeatureElement(ElementType.Empty, new Walk(0.25f, 0.f, 0.f, 0.f)),
				new RelativeFeatureElement(ElementType.Empty, true, new Walk(0.f, -0.25f, -0.25f, 0.f, 0.f)),
				new RelativeFeatureElement(ElementType.Empty, true, new Walk(0.5f, 0.f, 0.f, 0.f, 0.f, 0.25f)),
				new RelativeFeatureElement(ElementType.Empty, new Walk(0.5f, 0.f, 0.f, 0.f, 0.f, -0.25f))
				);
		final SpatialFeature targetFeature = new RelativeFeature(targetPattern, new Walk(), new Walk(0.5f, 0.25f));
		
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		final ContainerState containerState = context.containerState(0);
		
		final List<FeatureInstance> newInstances = newFeature.instantiateFeature(game, containerState, 1, -1, -1, -1, -1, -1);
		final List<FeatureInstance> targetInstances = targetFeature.instantiateFeature(game, containerState, 1, -1, -1, -1, -1, -1);
		
		if (newInstances.size() != targetInstances.size())
		{
			System.out.println("newFeature = " + newFeature);
			System.out.println("targetFeature = " + targetFeature);
		}
		
		assert(newInstances.size() == targetInstances.size());
		
		for (final FeatureInstance newInstance : newInstances)
		{
			boolean found = false;
			
			for (final FeatureInstance targetInstance : targetInstances)
			{
				if (newInstance.functionallyEquals(targetInstance))
				{
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				System.out.println("newFeature = " + newFeature);
				System.out.println("targetFeature = " + targetFeature);
				
				fail(newInstance + " was not a target instance!");
			}
		}
		
		for (final FeatureInstance targetInstance : targetInstances)
		{
			boolean found = false;
			
			for (final FeatureInstance newInstance : newInstances)
			{
				if (targetInstance.functionallyEquals(newInstance))
				{
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				System.out.println("newFeature = " + newFeature);
				System.out.println("targetFeature = " + targetFeature);
				
				fail(targetInstance + " was not a new instance!");
			}
		}
	}
	
	@Test
	public void testE()
	{
		final Game game = GameLoader.loadGameFromName("Amazons.lud");

		// Generated inconsistent pattern: refl=true,rots=all,els=[-{0.0,-0.25}, e{0.0,0.5,0.0}, e{0.0,-0.25}]
		// active feature A = rel:from=<{}>:pat=<refl=true,rots=all,els=[-{0.0,0.25}]>
		// rot A = 0.25
		// ref A = -1
		// anchor A = 91
		// active feature B = rel:to=<{}>:pat=<refl=true,rots=all,els=[e{0.0,0.0}, e{0.25}]>
		// rot B = 0.25
		// ref B = 1
		// anchor B = 92
		
		final Pattern patternA = new Pattern(
				new RelativeFeatureElement(ElementType.Empty, new Walk(0.f, 0.25f))
				);
		final SpatialFeature featureA = new RelativeFeature(patternA, null, new Walk());
		final FeatureInstance instanceA = new FeatureInstance(featureA, 91, -1, 0.25f, SiteType.Cell);
		
		final Pattern patternB = new Pattern(
				new RelativeFeatureElement(ElementType.Enemy, new Walk(0.f, 0.f)),
				new RelativeFeatureElement(ElementType.Enemy, new Walk(0.25f))
				);
		final SpatialFeature featureB = new RelativeFeature(patternB, new Walk(), null);
		final FeatureInstance instanceB = new FeatureInstance(featureB, 92, 1, 0.25f, SiteType.Cell);
		
		final SpatialFeature newFeature = SpatialFeature.combineFeatures(game, instanceA, instanceB);
		
		final Pattern targetPattern = new Pattern(
				new RelativeFeatureElement(ElementType.Empty, new Walk(-0.25f, -0.25f)), 
				new RelativeFeatureElement(ElementType.Enemy, new Walk(0.25f, 0.25f)),
				new RelativeFeatureElement(ElementType.Enemy, new Walk(0.25f, 0.f, 0.f))
				);
		final SpatialFeature targetFeature = new RelativeFeature(targetPattern, new Walk(0.25f), new Walk());
		
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		final ContainerState containerState = context.containerState(0);
		
		final List<FeatureInstance> newInstances = newFeature.instantiateFeature(game, containerState, 1, -1, -1, -1, -1, -1);
		final List<FeatureInstance> targetInstances = targetFeature.instantiateFeature(game, containerState, 1, -1, -1, -1, -1, -1);
		
		if (newInstances.size() != targetInstances.size())
		{
			System.out.println("newFeature = " + newFeature);
			System.out.println("targetFeature = " + targetFeature);
		}
		
		assert(newInstances.size() == targetInstances.size());
		
		for (final FeatureInstance newInstance : newInstances)
		{
			boolean found = false;
			
			for (final FeatureInstance targetInstance : targetInstances)
			{
				if (newInstance.functionallyEquals(targetInstance))
				{
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				System.out.println("newFeature = " + newFeature);
				System.out.println("targetFeature = " + targetFeature);
				
				fail(newInstance + " was not a target instance!");
			}
		}
		
		for (final FeatureInstance targetInstance : targetInstances)
		{
			boolean found = false;
			
			for (final FeatureInstance newInstance : newInstances)
			{
				if (targetInstance.functionallyEquals(newInstance))
				{
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				System.out.println("newFeature = " + newFeature);
				System.out.println("targetFeature = " + targetFeature);
				
				fail(targetInstance + " was not a new instance!");
			}
		}
	}
	
	@Test
	public void testF()
	{
		final Game game = GameLoader.loadGameFromName("Tic-Tac-Toe.lud");

		// Instance A: [Move to 1: (response to last move to 5)] [anchor=1, ref=1, rot=0,00] [rel:last_to=<{0,1/4}>:to=<{}>:pat=<els=[]>]
		// Instance B: [Move to 1: 4 must NOT be empty, ] [anchor=1, ref=1, rot=0,00] [rel:to=<{}>:pat=<els=[!-{0}]>] into rel:to=<{}>:pat=<els=[!-{0}]>
		//
		// Expect to combine into feature: to={}, last_to={0, 1/4}, !-{0}
		
		final Pattern patternA = new Pattern();
		final SpatialFeature featureA = new RelativeFeature(patternA, new Walk(), null, new Walk(0.f, 0.25f), null);
		final FeatureInstance instanceA = new FeatureInstance(featureA, 1, 1, 0.f, SiteType.Cell);
		
		final Pattern patternB = new 
				Pattern
				(
					new RelativeFeatureElement(ElementType.Empty, true, new Walk(0.f))
				);
		final SpatialFeature featureB = new RelativeFeature(patternB, new Walk(), null);
		final FeatureInstance instanceB = new FeatureInstance(featureB, 1, 1, 0.f, SiteType.Cell);
		
		final SpatialFeature newFeature = SpatialFeature.combineFeatures(game, instanceA, instanceB);
		
		final Pattern targetPattern = 
				new Pattern
				(
					new RelativeFeatureElement(ElementType.Empty, true, new Walk(0.f))
				);
		final SpatialFeature targetFeature = new RelativeFeature(targetPattern, new Walk(), null, new Walk(0.f, 0.25f), null);
		
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		final ContainerState containerState = context.containerState(0);
		
		final List<FeatureInstance> newInstances = newFeature.instantiateFeature(game, containerState, 1, -1, -1, -1, -1, -1);
		final List<FeatureInstance> targetInstances = targetFeature.instantiateFeature(game, containerState, 1, -1, -1, -1, -1, -1);
		
		if (newInstances.size() != targetInstances.size())
		{
			System.out.println("newFeature = " + newFeature);
			System.out.println("targetFeature = " + targetFeature);
		}
		
		assert(newInstances.size() == targetInstances.size());
		
		for (final FeatureInstance newInstance : newInstances)
		{
			boolean found = false;
			
			for (final FeatureInstance targetInstance : targetInstances)
			{
				if (newInstance.functionallyEquals(targetInstance))
				{
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				System.out.println("newFeature = " + newFeature);
				System.out.println("targetFeature = " + targetFeature);
				
				fail(newInstance + " was not a target instance!");
			}
		}
		
		for (final FeatureInstance targetInstance : targetInstances)
		{
			boolean found = false;
			
			for (final FeatureInstance newInstance : newInstances)
			{
				if (targetInstance.functionallyEquals(newInstance))
				{
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				System.out.println("newFeature = " + newFeature);
				System.out.println("targetFeature = " + targetFeature);
				
				fail(targetInstance + " was not a new instance!");
			}
		}
	}
	
	@Test
	public void testG()
	{
		final Game game = GameLoader.loadGameFromName("Ko-app-paw-na.lud");

		// active feature A = rel:last_to=<{0,1/4}>:from=<{}>:to=<{0}>:pat=<els=[R0{0,0,1/4}]>
		// rot A = 0.0
		// ref A = -1
		// anchor A = 13
		// active feature B = rel:to=<{}>:pat=<els=[-{0,-1/4}, !R0{1/4,1/4}]>
		// rot B = 0.75
		// ref B = -1
		// anchor B = 18
		
		// A and B have different anchors, and both have region proximity requirements. We
		// expect only the R0 test from A to be preserved, and the one from B to be discarded
		
		final Pattern patternA = new Pattern(new RelativeFeatureElement(ElementType.RegionProximity, new Walk(0.f, 0.f, 0.25f), 0));
		final SpatialFeature featureA = new RelativeFeature(patternA, new Walk(0.f), new Walk(), new Walk(0.f, 0.25f), null);
		final FeatureInstance instanceA = new FeatureInstance(featureA, 13, -1, 0.f, SiteType.Vertex);
		
		final Pattern patternB = 
				new Pattern
				(
					new RelativeFeatureElement(ElementType.Empty, new Walk(0.f, -0.25f)),
					new RelativeFeatureElement(ElementType.RegionProximity, true, new Walk(0.25f, 0.25f), 0)
				);
		final SpatialFeature featureB = new RelativeFeature(patternB, new Walk(), null);
		final FeatureInstance instanceB = new FeatureInstance(featureB, 18, -1, 0.75f, SiteType.Vertex);
		
		final SpatialFeature newFeature = SpatialFeature.combineFeatures(game, instanceA, instanceB);
		
		final Pattern targetPattern = 
				new Pattern
				(
					new RelativeFeatureElement(ElementType.RegionProximity, new Walk(0.f, 0.f, -0.25f), 0), 
					new RelativeFeatureElement(ElementType.Empty, new Walk(0.f, 0.25f, 0.25f))
				);
		final SpatialFeature targetFeature = new RelativeFeature(targetPattern, new Walk(0.f), new Walk(), new Walk(0.f, -0.25f), null);
		
		final Trial trial = new Trial(game);
		final Context context = new Context(game, trial);
		final ContainerState containerState = context.containerState(0);
		
		final List<FeatureInstance> newInstances = newFeature.instantiateFeature(game, containerState, 1, -1, -1, -1, -1, -1);
		final List<FeatureInstance> targetInstances = targetFeature.instantiateFeature(game, containerState, 1, -1, -1, -1, -1, -1);
		
		if (newInstances.size() != targetInstances.size())
		{
			System.out.println("newFeature = " + newFeature);
			System.out.println("targetFeature = " + targetFeature);
		}
		
		assert(newInstances.size() == targetInstances.size());
		
		for (final FeatureInstance newInstance : newInstances)
		{
			boolean found = false;
			
			for (final FeatureInstance targetInstance : targetInstances)
			{
				if (newInstance.functionallyEquals(targetInstance))
				{
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				fail(newInstance + " was not a target instance!");
			}
		}
		
		for (final FeatureInstance targetInstance : targetInstances)
		{
			boolean found = false;
			
			for (final FeatureInstance newInstance : newInstances)
			{
				if (targetInstance.functionallyEquals(newInstance))
				{
					found = true;
					break;
				}
			}
			
			if (!found)
			{
				fail(targetInstance + " was not a new instance!");
			}
		}
	}

}
