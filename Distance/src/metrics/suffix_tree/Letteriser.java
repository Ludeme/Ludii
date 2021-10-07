package metrics.suffix_tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import game.Game;
import main.math.Point3D;
import metrics.moveBased.MoveTypeRepeatedLocalAlignment;
import other.action.Action;
import other.action.ActionType;
import other.context.Context;
import other.move.Move;
import other.topology.Topology;
import other.topology.TopologyElement;
import other.trial.Trial;

public abstract class Letteriser
{
	public static final Letteriser lowRes = getLowResActioniser();
	public static final Letteriser moveDist = getMoveDistanceActioniser();
	
	public static final Letteriser[] possibleLetteriser = new Letteriser[] {lowRes,moveDist};
	 
	private static final Letteriser getMoveDistanceActioniser() {
		return new Letteriser()
		{
			@Override
			public String getName() {
				return "moveDist";
			}
			
			@Override
			public String[][] getWords(final Game game,final Trial[] trials)
			{
				final Context context = new Context(game, trials[0]);
				final Topology topology = context.board().topology();
				final ArrayList<TopologyElement> allGraphElements = topology.getAllGraphElements();
				final String[][] wordswords = new String[trials.length][];
			
				for (int i = 0; i < trials.length; i++)
				{
					final Trial trial = trials[i];
					final String[] words = getWords(topology,allGraphElements,trial);
					wordswords[i] = words;
				}
				
				return wordswords;
			}
			
			
			public String[] getWords(final Topology topology, final ArrayList<TopologyElement> allGraphElements,final Trial trial)
			{
				
				final java.awt.geom.Point2D.Double centre = topology.centrePoint();
				final List<Move> m = trial.generateCompleteMovesList();
				final ArrayList<String> words = new ArrayList<>();
				for (final Move move : m)
				{
					//System.out.println(move);
					for (final Action action : move.actions())
					{
						//System.out.println(action);
						//System.out.println(action.actionType());						
						
						if (action.isDecision()) {
							final Class<? extends Action> className = action.getClass();
							//final double d3d =  getDistance(centre,allGraphElements,action.from(),action.to(),action.actionType());
							//magic constant
							final String d3dString = getDistanceString(centre,allGraphElements,action.from(),action.to(),action.actionType());
							//final int dist100 = (int) Math.round(d3d*100);			
							//System.out.println(action.from() +" " + action.to() + " " + dist100 + " 3dDist " + d3d);
							
							final String[] splitted = className.toString().split("\\.");
							final String word = splitted[splitted.length-1]+d3dString;
							
							//System.out.println(word);
							
							
							words.add(word);
						}else {
							final Class<? extends Action> className = action.getClass();
							words.add(className.toString());
						}
						
					}
				}
				String[] wordArray = new String[words.size()];
				wordArray = words.toArray(wordArray);
				return wordArray;
			}
			
			@Override
			public String[] getWords(final Game game,final Trial trial)
			{		
				return getWords(game, new Trial[] {trial})[0];			
			}

			@SuppressWarnings("unused")
			private double getDistance(
			final java.awt.geom.Point2D.Double centre, final ArrayList<TopologyElement> allGraphElements, final int from, final int to, final ActionType actionType
					)
			{
				final boolean fromInList = (from>=0&&from <allGraphElements.size());
				final boolean toInList = (to>=0&&to<allGraphElements.size());
				if (fromInList&&toInList) {
					if (from==to&&actionType!=null&&(actionType.equals(ActionType.Add)||actionType.equals(ActionType.Select))) {
						if (from!=to) {
							System.out.println("interesting");
						}
						final Point3D fr1 = allGraphElements.get(to).centroid3D();
						
						return fr1.distance(centre);
					}
				}
				Point3D fr0;
				Point3D fr1;	
				if (fromInList&&toInList) {
					fr0 = allGraphElements.get(from).centroid3D();
					fr1 = allGraphElements.get(to).centroid3D();
					
					return fr0.distance(fr1);
				}
				if (toInList) {
					fr1 = allGraphElements.get(to).centroid3D();
					return fr1.distance(centre);
				}
				if (fromInList) {
					fr0 = allGraphElements.get(from).centroid3D();
					return fr0.distance(centre);
				}
		return 0;
	}
			private String getDistanceString(
					final java.awt.geom.Point2D.Double centre, final ArrayList<TopologyElement> allGraphElements, final int from, final int to, final ActionType actionType
							)
					{
						final boolean fromInList = (from>=0&&from <allGraphElements.size());
						final boolean toInList = (to>=0&&to<allGraphElements.size());
						if (fromInList&&toInList) {
							if (from==to&&actionType!=null&&(actionType.equals(ActionType.Add)||actionType.equals(ActionType.Remove)||actionType.equals(ActionType.Select))) {
								if (from!=to) {
									System.out.println("interesting");
								}
								final Point3D fr1 = allGraphElements.get(to).centroid3D();
								final double dx = fr1.x()-centre.x;
								final double dy = fr1.y()-centre.y;
								final double dz = fr1.z()-0.0;
								
								return convert(dx,dy,dz,100);
							}
						}
						Point3D fr0;
						Point3D fr1;	
						if (fromInList&&toInList) {
							fr0 = allGraphElements.get(from).centroid3D();
							fr1 = allGraphElements.get(to).centroid3D();
							final double dx = fr1.x()-fr0.x();
							final double dy = fr1.y()-fr0.y();
							final double dz = fr1.z()-fr0.z();
							return convert(dx,dy,dz,100);
						}
						if (toInList) {
							fr1 = allGraphElements.get(to).centroid3D();
							final double dx = fr1.x()-centre.x;
							final double dy = fr1.y()-centre.y;
							final double dz = fr1.z()-0;
							return convert(dx,dy,dz,100);
						}
						if (fromInList) {
							fr0 = allGraphElements.get(from).centroid3D();
							final double dx = fr0.x()-centre.x;
							final double dy = fr0.y()-centre.y;
							final double dz = fr0.z()-0;
							return convert(-dx,-dy,dz,100);
						}
				return "0";
			}


			private String convert(final double dx, final double dy, final double dz, final int i)
			{
				
				return (int)(dx*100)+"_"+(int)(dy*100)+"_"+(int)(dz*100);
				//return ""+(int)(100*Math.sqrt(dx*dx+dy*dy+dz*dz));
			}
		};
	}
	
	
	private static Letteriser getLowResActioniser() {
		return new Letteriser()
		{
			@Override
			public String getName() {
				return "lowResActioniser";
			}
			
			@Override
			public String[] getWords(final Game g, final Trial trials)
			{
				return MoveTypeRepeatedLocalAlignment.getWordsFromTrials(trials);
			}

			@Override
			public String[][] getWords(final Game g, final Trial[] trials)
			{
				
					final String[] so = MoveTypeRepeatedLocalAlignment.getWordsFromTrials(trials);
				
				return new String[][] {so};
			}
			
			
		};
	}
	
	public abstract String[] getWords(Game game,final Trial trials);
	public abstract String[][] getWords(Game game,final Trial[] trials);
	public abstract String getName();
	@Override
	public String toString() {
		return getName();
	}
	
	/**
	 * 
	 * @param g
	 * @param trials
	 * @return the resulting alphabet
	 */
	public TreeBuildingIngredients createTreeBuildingIngredients( final Game g,
			final Trial[] trials
	)
	{
		final HashSet<String> uniqueActionNames = new HashSet<>();
		final ArrayList<String[]> actionInTrials = new ArrayList<>();
		for (final Trial trial : trials)
		{
			final String[] concatWords = getWords(g,trial);
			
			actionInTrials.add(concatWords);
			for (final String string : concatWords)
			{
				uniqueActionNames.add(string);
			}		
		}
		final Alphabet a = new Alphabet(uniqueActionNames);
		final ArrayList<ArrayList<Letter>> convertedTrials = a.encodeAllTrials(actionInTrials);
		return new TreeBuildingIngredients(a,convertedTrials);
	}


	public static Letteriser showUserSelectionDialog()
	{
		 
	    final JComboBox<Letteriser> combo = new JComboBox<>(possibleLetteriser);    
	    JOptionPane.showConfirmDialog(null, combo,
				"Choose the letteriser", JOptionPane.DEFAULT_OPTION);
	     
	    final Letteriser let = (Letteriser) combo.getSelectedItem();
	    return let;
	}

	

}
