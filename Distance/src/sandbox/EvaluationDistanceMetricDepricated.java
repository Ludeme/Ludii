package sandbox;

public class EvaluationDistanceMetricDepricated {
	/*
	public static String closestCluster(final LudRul candidate,
			final DistanceMetric dm, final HashMap<String,List<LudRul>> folderToPosibleGameOptionCombination, final DistanceMatrix<LudRul, String> distanceMatrix) {
		final String folder = DistanceUtils.getCurrentFolderName(candidate.getFile());
		System.out.println("Measuring distances of: " + candidate.getGameNameIncludingOption() +" of " + folder + " to every class:");
		 
		 final HashMap<String,Double> distancesToFolderName = getDistancesToFolderName(candidate,folderToPosibleGameOptionCombination,dm);
		 for (final Entry<String, Double> distances : distancesToFolderName.entrySet()) {
			distanceMatrix.put(candidate, distances.getKey(), distances.getValue());
		}
		 final ArrayList<Entry<String, Double>> al = sortFolderNameByDistance(distancesToFolderName.entrySet());
		 printOutDistances(candidate,al);
		 
		 return al.get(0).getKey();
	}
	
	public static double evaluateDistanceMeasure2(
			final DistanceMetric dm
	)
	{
		
		final HashMap<String,List<LudRul>> folderToPosibleGameOptionCombination = DistanceUtils.getFolderToPossibleGameOptionCombination();
		//final ArrayList<LudRul> candidates =  selectNofEachFolder(1,folderToPosibleGameOptionCombination);
		//Collections.shuffle(candidates);
		final ArrayList<LudRul> candidates = DistanceUtils.getAllLudiiGameFilesAndRulesetCombination(false);
		//while (candidates.size()>2)candidates.remove(candidates.size()-1);
		final HashMap<LudRul,String> folderAssignment = new HashMap<>();
		
		final ArrayList<String> folderNames = new ArrayList<>(folderToPosibleGameOptionCombination.keySet());
		final DistanceMatrix<LudRul,String> distanceMatrix = new DistanceMatrix<>(candidates,folderNames);  
		
		int i = 0;
		for (final LudRul candidate : candidates) {
			final String closest = closestCluster(candidate, dm, folderToPosibleGameOptionCombination,distanceMatrix);
			folderAssignment.put(candidate, closest);
			System.out.println(++i + "/"+ candidates.size() + " of candidates evaluated");
		}
		
		double correctlyAssigned = 0; 
		for (final Entry<LudRul,String> entry : folderAssignment.entrySet()) {
			
			final String goalFolder = entry.getKey().getCurrentFolderName();
			final String assignedFolder = entry.getValue();
			System.out.println(entry.getKey().getGameNameIncludingOption() + " " + assignedFolder + " goal: " + goalFolder );
			if (goalFolder.equals(assignedFolder))correctlyAssigned++;
		}
		final double ratio = (correctlyAssigned/folderAssignment.size());
		System.out.println("Correctly assigned: " + correctlyAssigned + "\\" + folderAssignment.size() + " : " + ratio);
		distanceMatrix.printDistanceMatrixToFile("Game",DistanceUtils.outputfolder, "gameToFolder.csv", ",");
		distanceMatrix.printSortedDistanceMatricesToFile(DistanceUtils.outputfolder, "gameToFolderSorted.csv", ",");
		return ratio;
	}
	
	public static ArrayList<LudRul> selectNofEachFolder(
			final int n, final HashMap<String,List<LudRul>> folderToPosibleGameOptionCombination
	)
	{	
		final ArrayList<LudRul> candidates = new ArrayList<>(); 
		for (final Entry<String, List<LudRul>> folderAndCorrespondingFiles : folderToPosibleGameOptionCombination.entrySet()) {
			final List<LudRul> val = folderAndCorrespondingFiles.getValue();
			final ArrayList<LudRul> entriesInFolder = new ArrayList<>(val);
			Collections.shuffle(entriesInFolder);
			for (int i = 0; i < entriesInFolder.size()&&i<n; i++) {
				candidates.add(entriesInFolder.get(i));
			}
		}
			
		return candidates;
	}
	
	private static void printOutDistances(
			final LudRul candidate, final ArrayList<Entry<String,Double>> distancesToFolderName
	)
	{
		final String folder = DistanceUtils.getCurrentFolderName(candidate.getFile());
		System.out.println("Distance of " + candidate.getGameNameIncludingOption() +" of " + folder + " to every class:");
		for (final Entry<String, Double> entry : distancesToFolderName) {
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
		
		
		
	}

	
	private static ArrayList<Entry<String,Double>> sortFolderNameByDistance(
			final Set<Entry<String,Double>> set
	)
	{
		final ArrayList<Entry<String,Double>> list = new ArrayList<>(set);
		list.sort(new Comparator<Entry<String, Double>>() {
			@Override
			public int compare(
					final Entry<String, Double> o1, final Entry<String, Double> o2
			)
			{
				
				return Double.compare(o1.getValue(), o2.getValue());
			}
		});
		return list;
	}

	private static HashMap<String,Double> getDistancesToFolderName(
			final LudRul candidate, final HashMap<String,List<LudRul>> folderToPosibleGameOptionCombination, final DistanceMetric dm
	)
	{
		final HashMap<String, Double> distancesToFolderName = new HashMap<>();
		//final Game gameA = GameLoader.loadGameFromFile(candidate.getFile(), candidate.getRuleSet());
		final Set<Entry<String, List<LudRul>>> es = folderToPosibleGameOptionCombination.entrySet();
		
		int bigCounter = 0;
		for (final Entry<String, List<LudRul>> entry : es) {
			double distance = 0;
			double minDistance = Integer.MAX_VALUE;
			final String folder = entry.getKey();
			final List<LudRul> gamesToCompareWith = entry.getValue();
			int counter = 0;
			for (final LudRul gameToCompareWith : gamesToCompareWith) {
				if (candidate.getFile().equals(gameToCompareWith.getFile()))continue;
				counter++;
				bigCounter++;
				//final Game gameB = GameLoader.loadGameFromFile(gameToCompareWith.getFile(), gameToCompareWith.getRuleSet());
				final double newDistance = dm.distance(candidate, gameToCompareWith).score();
				//final double newDistance = dm.distance(gameA, gameB).score();
				distance += newDistance;
				minDistance = Math.min(minDistance, newDistance);
				//System.out.println("Distance from " + candidate.getGameNameIncludingOption() + " to " + gameToCompareWith.getGameNameIncludingOption() + ": " + newDistance);
			}
			if (counter==0) {
				counter=1;
				distance=0;
				minDistance = 0;
			}
			distance/=counter;
			distancesToFolderName.put(folder,distance);
			System.out.println("Distance to " + folder + ": " + distance + "  " + bigCounter + " evaluations done");
		}
		
		return distancesToFolderName;
	}

	
	
	public static Map<String, List<Game>> targetGamesToFolder(
			final List<File> targetGames
	)
	{
		final Map<String,List<Game>> folderNameToGames = new HashMap<>();
		for (final File file : targetGames) {
			final List<Game> games = DistanceUtils.getAllOptionalGameTypes(file);
			final String folder = DistanceUtils.getFolderNameFromFile(file);
			List<Game> f = folderNameToGames.get(folder);
			if (f==null) {
				f= new ArrayList<Game>();
				folderNameToGames.put(folder, f);
			}
			f.addAll(games);
		}
		return folderNameToGames;
	}*/
}
