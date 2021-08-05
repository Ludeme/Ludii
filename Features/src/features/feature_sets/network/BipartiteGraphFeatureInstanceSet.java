package features.feature_sets.network;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import features.spatial.instances.AtomicProposition;
import features.spatial.instances.AtomicProposition.StateVectorTypes;
import features.spatial.instances.FeatureInstance;
import game.Game;
import gnu.trove.list.array.TIntArrayList;

/**
 * A bipartite graph representation of a feature instance set. We have one
 * group of nodes representing atomic propositions, and one group of nodes
 * representing all feature instances.
 *
 * @author Dennis Soemers
 */
public class BipartiteGraphFeatureInstanceSet
{
	
	//-------------------------------------------------------------------------
	
	/** Proposition nodes stored as map, so we can easily avoid adding duplicates for same proposition */
	protected final Map<AtomicProposition, PropositionNode> propositionNodes;
	
	/** Also need the same propositions stored in a list */
	protected final List<PropositionNode> propositionNodesList;
	
	/** List of all feature instance nodes */
	protected final List<FeatureInstanceNode> instanceNodes;
	
	//-------------------------------------------------------------------------
	
	/**
	 * Constructor
	 */
	public BipartiteGraphFeatureInstanceSet()
	{
		propositionNodes = new HashMap<AtomicProposition, PropositionNode>();
		propositionNodesList = new ArrayList<PropositionNode>();
		instanceNodes = new ArrayList<FeatureInstanceNode>();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Inserts given instance into this set
	 * @param instance
	 */
	public void insertInstance(final FeatureInstance instance)
	{
		final List<AtomicProposition> atomicPropositions = instance.generateAtomicPropositions();
		final FeatureInstanceNode instanceNode = new FeatureInstanceNode(instanceNodes.size(), instance);
		instanceNodes.add(instanceNode);

		for (final AtomicProposition proposition : atomicPropositions)
		{
			PropositionNode propNode = propositionNodes.get(proposition);
			if (propNode == null)
			{
				propNode = new PropositionNode(propositionNodesList.size(), proposition);
				propositionNodesList.add(propNode);
				propositionNodes.put(proposition, propNode);
			}

			propNode.instances.add(instanceNode);
			instanceNode.propositions.add(propNode);
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return A PropFeatureInstanceSet representation of this feature set
	 */
	public PropFeatureInstanceSet toPropFeatureInstanceSet()
	{
		// The list of nodes we want to build up
		final List<PropNode> nodes = new ArrayList<PropNode>();
		
		// Compute conjunctive clauses
		final BitSet[] conjunctiveClauses = computeConjunctiveClauses();
				
		// Split clauses into Ungeneralised and Generalised sets
		// 	- Ungeneralised clauses are not generalised by any others, so they must for sure get at
		//	least one of their propositions checked
		//	- Generalised clauses are generalised by at least one other, so maybe it doesn't need to
		//	get anything checked (if one of its generalisers already ends up getting disproven)
		List<BitSet> ungeneralised = new ArrayList<BitSet>();
		List<BitSet> generalised = new ArrayList<BitSet>();
		
		for (int i = 0; i < conjunctiveClauses.length; ++i)
		{
			final BitSet iProps = conjunctiveClauses[i];
			if (iProps.isEmpty())
				continue;		// Skip this, don't need it in either set
			
			boolean isGeneralised = false;
			
			for (int j = 0; j < conjunctiveClauses.length; ++j)
			{
				if (i == j)
					continue;
				
				final BitSet jProps = conjunctiveClauses[j];
				if (jProps.isEmpty())
					continue;		// Skip this, don't need it in either set
				
				if (iProps.equals(jProps))
					continue;		// If they're equal, don't count as generalised either way, just preserve both
				
				final BitSet jPropsCopy = ((BitSet)jProps.clone());
				jPropsCopy.andNot(iProps);
				
				if (jPropsCopy.isEmpty())
				{
					// j has no propositions that i doesn't, so j generalises i
					isGeneralised = true;
					break;
				}
			}
			
			if (isGeneralised)
				generalised.add(iProps);
			else
				ungeneralised.add(iProps);
		}
		
		// Loop until ungeneralised is depleted (which must mean that generalised is also depleted)
		while (!ungeneralised.isEmpty())
		{
			// Generate bins of instances in ungeneralised based on the cardinalities of their clauses
			final List<List<BitSet>> ungeneralisedBins = new ArrayList<List<BitSet>>();
			ungeneralisedBins.add(null);
			
			for (final BitSet clause : ungeneralised)
			{
				final int clauseLength = clause.cardinality();
				
				while (ungeneralisedBins.size() <= clauseLength)
				{
					ungeneralisedBins.add(new ArrayList<BitSet>());
				}
				
				ungeneralisedBins.get(clauseLength).add(clause);
			}
			
			// For every proposition, we want to know how often it shows up in generalised (as tie-breaker)
			final int[] generalisedPropCounts = new int[propositionNodesList.size()];
			for (final BitSet props : generalised)
			{
				for (int i = props.nextSetBit(0); i >= 0; i = props.nextSetBit(i + 1))
				{
					generalisedPropCounts[i]++;
				}
			}
			
			// Figure out which propositions to add in this round
			// We'll track this as both a list and a BitSet
			// 	List is used because order is important,
			// 	BitSet is used for efficient testing of already-covered clauses
			final TIntArrayList propsToAdd = new TIntArrayList();
			final BitSet selectedProps = new BitSet();
			
			// All propositions of clause length 1 must for sure be added
			for (final BitSet clause : ungeneralisedBins.get(1))
			{
				final int propID = clause.nextSetBit(0);
				propsToAdd.add(propID);
				selectedProps.set(propID);
			}
			
			// From every other bin, select the proposition that:
			//	- is not already selected
			//	- occurs maximally often in uncovered clauses of that bin
			//	- as tie-breaker, occurs maximally often in ungeneralised
			for (int l = 2; l < ungeneralisedBins.size(); ++l)
			{
				final BitSet candidateProps = new BitSet();
				final int[] propOccurrences = new int[propositionNodesList.size()];
				
				for (final BitSet clause : ungeneralisedBins.get(l))
				{
					if (clause.intersects(selectedProps))
						continue;		// Already covered
					
					for (int i = clause.nextSetBit(0); i >= 0; i = clause.nextSetBit(i + 1))
					{
						candidateProps.set(i);
						propOccurrences[i]++;
					}
				}
								
				if (!candidateProps.isEmpty())
				{
					final TIntArrayList bestCandidates = new TIntArrayList();
					int maxOccurrence = 0;
					
					for (int i = candidateProps.nextSetBit(0); i >= 0; i = candidateProps.nextSetBit(i + 1))
					{
						if (propOccurrences[i] > maxOccurrence)
						{
							maxOccurrence = propOccurrences[i];
							bestCandidates.clear();
							bestCandidates.add(i);
						}
						else if (propOccurrences[i] == maxOccurrence)
						{
							bestCandidates.add(i);
						}
					}
					
					// Tie-breaker: most occurrences in generalised set
					int propToAdd = bestCandidates.getQuick(0);
					int maxGeneralisedOccurrences = generalisedPropCounts[propToAdd];
					
					for (int i = 1; i < bestCandidates.size(); ++i)
					{
						final int propID = bestCandidates.getQuick(i);
						final int generalisedOccurrences = generalisedPropCounts[propID];
						if (generalisedOccurrences > maxGeneralisedOccurrences)
						{
							maxGeneralisedOccurrences = generalisedOccurrences;
							propToAdd = propID;
						}
					}
					
					// Select proposition
					propsToAdd.add(propToAdd);
					selectedProps.set(propToAdd);
				}
			}
			
			// Add all the propositions we want to add.
			// IMPORTANT: do so in the order in which we selected them
			for (int i = 0; i < propsToAdd.size(); ++i)
			{
				final int propID = propsToAdd.getQuick(i);
				nodes.add(new PropNode(nodes.size(), propositionNodesList.get(propID).proposition));
			}
	
			// Update clauses in ungeneralised set
			for (int i = ungeneralised.size() - 1; i >= 0; --i)
			{
				final BitSet clause = ungeneralised.get(i);
				clause.andNot(selectedProps);
				
				if (clause.isEmpty())
					ungeneralised.remove(i);
			}
			
			// Update clauses in generalised set
			for (int i = generalised.size() - 1; i >= 0; --i)
			{
				final BitSet clause = generalised.get(i);
				clause.andNot(selectedProps);
				
				if (clause.isEmpty())
					generalised.remove(i);
			}
			
			// Re-compute ungeneralised and generalised sets
			final List<BitSet> newUngeneralised = new ArrayList<BitSet>();
			final List<BitSet> newGeneralised = new ArrayList<BitSet>();
			
			// Gather all remaining clauses in a set first, to remove duplicates
			final Set<BitSet> allClausesSet = new HashSet<BitSet>();
			allClausesSet.addAll(generalised);
			allClausesSet.addAll(ungeneralised);
			final BitSet[] allClauses = allClausesSet.toArray(new BitSet[allClausesSet.size()]);
			
			for (int i = 0; i < allClauses.length; ++i)
			{
				final BitSet iProps = allClauses[i];
				if (iProps.isEmpty())
					continue;		// Skip this, don't need it in either set
				
				boolean isGeneralised = false;
				
				for (int j = 0; j < allClauses.length; ++j)
				{
					if (i == j)
						continue;
					
					final BitSet jProps = allClauses[j];
					if (jProps.isEmpty())
						continue;		// Skip this, don't need it in either set
					
					final BitSet jPropsCopy = ((BitSet)jProps.clone());
					jPropsCopy.andNot(iProps);
					
					if (jPropsCopy.isEmpty())
					{
						// j has no propositions that i doesn't, so j generalises i
						isGeneralised = true;
						break;
					}
				}
				
				if (isGeneralised)
					newGeneralised.add(iProps);
				else
					newUngeneralised.add(iProps);
			}
			
			generalised = newGeneralised;
			ungeneralised = newUngeneralised;
		}
		
		// Tell every propnode which feature instances it should deactivate if false
		for (final PropNode propNode : nodes)
		{
			final List<FeatureInstanceNode> instances = propositionNodes.get(propNode.proposition()).instances;
			for (int i = instances.size() - 1; i >= 0; --i)
			{
				// Doing this in reverse order leads to better memory usage because we start with
				// the biggest index, which usually causes the dependentInstances bitset in the propNode
				// to get sized to precisely the correct size, rather than getting doubled several times
				// and overshooting
				propNode.setDependentInstance(instances.get(i).id);
			}
		}
		
		// Generate our array of feature instances
		final FeatureInstance[] featureInstances = new FeatureInstance[instanceNodes.size()];
		for (int i = 0; i < instanceNodes.size(); ++i)
		{
			featureInstances[i] = instanceNodes.get(i).instance;
		}
		
		// And array of propnodes
		final PropNode[] propNodes = nodes.toArray(new PropNode[nodes.size()]);
		
		return new PropFeatureInstanceSet(featureInstances, propNodes);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param numFeatures
	 * @param thresholdedFeatures Features that should be ignored because their abs weights are too low
	 * @param game
	 * @param perspectivePlayer
	 * @return A SPatterNet representation of this feature set
	 */
	public SPatterNet toSPatterNet
	(
		final int numFeatures, final BitSet thresholdedFeatures, final Game game, final int perspectivePlayer
	)
	{
		// Our final, correctly-sorted list of atomic propositions in the order in which they
		// should be evaluated
		final List<AtomicProposition> propositions = new ArrayList<AtomicProposition>();
		
		// Compute our conjunctions (one per feature instance)
		final List<BitSet> conjunctiveClauses = new ArrayList<BitSet>();
		for (final BitSet bitset : computeConjunctiveClauses())
		{
			conjunctiveClauses.add(bitset);
		}
		
		// Remember which features should auto-activate (due to 0-requirement instances)
		final BitSet autoActiveFeatures = new BitSet();
		
		// Create list of instance nodes where we already filter some unnecessary ones out
		final List<FeatureInstanceNode> filteredInstanceNodes = new ArrayList<FeatureInstanceNode>(instanceNodes.size());
		
		// First we add everything, afterwards we remove, because we need to synchronise also removing bitsets from
		// conjunctiveClauses
		filteredInstanceNodes.addAll(instanceNodes);
		
		for (int i = filteredInstanceNodes.size() - 1; i >= 0; --i)
		{
			final FeatureInstanceNode node = filteredInstanceNodes.get(i);
			final int featureIdx = node.instance.feature().spatialFeatureSetIndex();
			
			if (thresholdedFeatures.get(featureIdx))
			{
				// Should remove this instance because its feature is thresholded
				filteredInstanceNodes.remove(i);
				conjunctiveClauses.remove(i);
			}
			else if (conjunctiveClauses.get(i).isEmpty())
			{
				// Should remove this instance because it has no requirements, and remember that feature
				// should auto-activate
				filteredInstanceNodes.remove(i);
				conjunctiveClauses.remove(i);
				autoActiveFeatures.set(featureIdx);
			}
		}
		
		// A second pass to remove any instance of auto-active features
		for (int i = filteredInstanceNodes.size() - 1; i >= 0; --i)
		{
			final FeatureInstanceNode node = filteredInstanceNodes.get(i);
			final int featureIdx = node.instance.feature().spatialFeatureSetIndex();
			
			if (autoActiveFeatures.get(featureIdx))
			{
				filteredInstanceNodes.remove(i);
				conjunctiveClauses.remove(i);
			}
		}
		
		// Also get rid of any feature instances that are generalised by other instances for the same feature
		for (int i = filteredInstanceNodes.size() - 1; i >= 0; --i)
		{
			final FeatureInstanceNode instanceNode = filteredInstanceNodes.get(i);
			final int featureIdx = instanceNode.instance.feature().spatialFeatureSetIndex();
			
			for (int k = 0; k < filteredInstanceNodes.size(); ++k)
			{
				if (i != k)
				{
					final FeatureInstanceNode other = filteredInstanceNodes.get(k);
					if (other.instance.feature().spatialFeatureSetIndex() == featureIdx)
					{
						if (other.instance.generalises(instanceNode.instance))
						{
							filteredInstanceNodes.remove(i);
							conjunctiveClauses.remove(i);
							break;
						}
					}
				}
			}
		}
		
		// First keep instances unsorted, but gather data here that we can use to sort afterwards
		final List<SortableFeatureInstance> sortableFeatureInstances = new ArrayList<SortableFeatureInstance>();
		for (final FeatureInstanceNode node : filteredInstanceNodes)
		{
			sortableFeatureInstances.add(new SortableFeatureInstance(node.instance));
		}
		
		// Compute our disjunctions (one per feature)
		final DisjunctiveClause[] disjunctions = new DisjunctiveClause[numFeatures];
		for (int i = 0; i < numFeatures; ++i)
		{
			disjunctions[i] = new DisjunctiveClause();
		}
		for (int i = 0; i < filteredInstanceNodes.size(); ++i)
		{
			final int featureIdx = filteredInstanceNodes.get(i).instance.feature().spatialFeatureSetIndex();
			disjunctions[featureIdx].addConjunction(new Conjunction(conjunctiveClauses.get(i)));
		}
		
		// Compute which propositions are still relevant (maybe not all of them due to removals of feature instances)
		final BitSet relevantProps = new BitSet(propositionNodesList.size());
		for (final DisjunctiveClause disjunction : disjunctions)
		{
			relevantProps.or(disjunction.usedPropositions());
		}
		
		// For every proposition, compute which other propositions it should (dis)prove if found to be
		// true or false
		final BitSet[] proveIfTrue = new BitSet[propositionNodesList.size()];
		final BitSet[] disproveIfTrue = new BitSet[propositionNodesList.size()];
		final BitSet[] proveIfFalse = new BitSet[propositionNodesList.size()];
		final BitSet[] disproveIfFalse = new BitSet[propositionNodesList.size()];
		
		for (int i = 0; i < propositionNodesList.size(); ++i)
		{
			proveIfTrue[i] = new BitSet();
			disproveIfTrue[i] = new BitSet();
			proveIfFalse[i] = new BitSet();
			disproveIfFalse[i] = new BitSet();
			
			final AtomicProposition propI = propositionNodesList.get(i).proposition;
			
			for (int j = 0; j < propositionNodesList.size(); ++j)
			{
				if (i == j)
					continue;
				
				final AtomicProposition propJ = propositionNodesList.get(j).proposition;
				
				if (propI.provesIfTrue(propJ, game))
					proveIfTrue[i].set(j);
				else if (propI.disprovesIfTrue(propJ, game))
					disproveIfTrue[i].set(j);
				
				if (propI.provesIfFalse(propJ, game))
					proveIfFalse[i].set(j);
				else if (propI.disprovesIfFalse(propJ, game))
					disproveIfFalse[i].set(j);
			}
		}
		
		// Each of these lists should contain, at index i, all disjunctions with i assumed-proven conjunctions
		List<List<DisjunctiveClause>> ungeneralisedDisjunctions = new ArrayList<List<DisjunctiveClause>>();
		List<List<DisjunctiveClause>> generalisedDisjunctions = new ArrayList<List<DisjunctiveClause>>();
		
		// Start out putting everything in Generalised, and not yet assuming anything proven
		final List<DisjunctiveClause> zeroProvenDisjunctions = new ArrayList<DisjunctiveClause>();
		for (final DisjunctiveClause clause : disjunctions)
		{
			// Remove conjunctions from the disjunction that are generalised by other conjunctions within the same disjunction
			clause.eliminateGeneralisedConjunctions();
			
			// Only include clause if it's non-empty
			if (clause.length() > 0)
				zeroProvenDisjunctions.add(clause);
		}
		
		// Sort the disjunctions in increasing order of number of conjunctions; this will generally
		// let us more quickly fill up the ungeneralised set and hence discover generalisers more quickly
		zeroProvenDisjunctions.sort(new Comparator<DisjunctiveClause>()
		{
			@Override
			public int compare(final DisjunctiveClause o1, final DisjunctiveClause o2)
			{
				return o1.length() - o2.length();
			}
		});
		
		generalisedDisjunctions.add(zeroProvenDisjunctions);
		
		// Now compute which ones are ungeneralised for real
		UngeneralisedGeneralisedWrapper ungenGenWrapper = updateUngeneralisedGeneralised(ungeneralisedDisjunctions, generalisedDisjunctions);
		ungeneralisedDisjunctions = ungenGenWrapper.ungeneralisedDisjunctions;
		generalisedDisjunctions = ungenGenWrapper.generalisedDisjunctions;
		
		// We track picked propositions in both a list and a bitset.
		// List because order of picking is relevant, bitset for faster coverage tests
		final TIntArrayList pickedPropsList = new TIntArrayList();
		final BitSet pickedPropsBitset = new BitSet();
		
		// Mark any propositions that are irrelevant as "already picked", so we don't prioritise and pick them
		// anyway if they test for empty positions
		final BitSet irrelevantProps = (BitSet) relevantProps.clone();
		irrelevantProps.flip(0, propositionNodesList.size());
		pickedPropsBitset.or(irrelevantProps);
		
		// Keep going until every Ungeneralised_i list is empty (which must mean that all Generalised_X lists are also empty)
		int i = -1;
		while ((i = firstNonEmptyListIndex(ungeneralisedDisjunctions)) >= 0)
		{
			final List<DisjunctiveClause> ungeneralised_i = ungeneralisedDisjunctions.get(i);
			
			// Need to clear list of picked props, but bitset doesn't need to be cleared
			pickedPropsList.clear();
			
			// For every disjunction in this ungeneralised_i set, we have to pick at least one conjunction
			// to cover by at least one proposition
			//
			// Finding the minimum set of propositions such that at least one conjunction from every
			// disjunction is covered is the Hitting Set problem: NP-hard
			//
			// A simple heuristic for simple greedy approaches for Set Cover is to pick whichever proposition
			// covers the largest number of disjunctions first. This is also similar to the "Max occurrences"
			// idea in the MOMS heuristic for SAT.
			// (and splitting in generalised/ungeneralised may be similar to eliminating dominated columns in
			// set cover?)
			
			// Create bins of disjunctions based on their length (= number of conjunctions)
			final List<List<DisjunctiveClause>> disjunctionBins = new ArrayList<List<DisjunctiveClause>>();
			for (final DisjunctiveClause clause : ungeneralised_i)
			{
				final int clauseLength = clause.length();
				
				while (disjunctionBins.size() <= clauseLength)
				{
					disjunctionBins.add(new ArrayList<DisjunctiveClause>());
				}
				
				disjunctionBins.get(clauseLength).add(clause);
			}
			
			// We will start looking at disjunctions that have only a single conjunction; from each of these
			// we will for sure have to pick at least one proposition
			final List<DisjunctiveClause> singleConjList = disjunctionBins.get(1);
			
			// As an additional heuristic, we want to look at is-empty tests first
			// Sort in reverse order, since after this sorting we loop through it in reverse order
//			StringBuilder sb = new StringBuilder();
//			for (final DisjunctiveClause dis : singleConjList)
//			{
//				sb.append(propositionNodesList.get(dis.conjunctions().get(0).toProve().nextSetBit(0)) + ", ");
//			}
//			System.out.println("before sort: " + sb.toString());
			singleConjList.sort(new Comparator<DisjunctiveClause>()
			{

				@Override
				public int compare(final DisjunctiveClause o1, final DisjunctiveClause o2)
				{
					// We should only have a single conjunction per disjunction here
					final BitSet conj1BitSet = o1.conjunctions().get(0).toProve();
					final BitSet conj2BitSet = o2.conjunctions().get(0).toProve();
					
					int numEmptyChecks1 = 0;
					for (int j = conj1BitSet.nextSetBit(0); j >= 0; j = conj1BitSet.nextSetBit(j + 1))
					{
						if (propositionNodesList.get(j).proposition.stateVectorType() == StateVectorTypes.Empty)
							++numEmptyChecks1;
					}
					
					int numEmptyChecks2 = 0;
					for (int j = conj2BitSet.nextSetBit(0); j >= 0; j = conj2BitSet.nextSetBit(j + 1))
					{
						if (propositionNodesList.get(j).proposition.stateVectorType() == StateVectorTypes.Empty)
							++numEmptyChecks2;
					}
					
					return numEmptyChecks2 - numEmptyChecks1;
				}
				
			});
//			sb = new StringBuilder();
//			for (final DisjunctiveClause dis : singleConjList)
//			{
//				sb.append(propositionNodesList.get(dis.conjunctions().get(0).toProve().nextSetBit(0)) + ", ");
//			}
//			System.out.println("after sort: " + sb.toString());
//			System.out.println();
			
			// Within this bin of single-conjunction disjunctions, first add all the propositions of length-1
			// conjunctions; these have to be picked for sure
			for (int j = singleConjList.size() - 1; j >= 0; --j)
			{
				final DisjunctiveClause disj = singleConjList.get(j);
				if (disj.usedPropositions().cardinality() == 1)
				{
					// Entire disjunction uses only a single proposition
					final int propID = disj.usedPropositions().nextSetBit(0);
					if (!pickedPropsBitset.get(propID))
					{
						// Proposition not already picked, so pick it now
						pickProp(propID, pickedPropsList, pickedPropsBitset);
					}
					
					// Remove this disjunction, already handled it
					singleConjList.remove(j);
				}
			}
			
			// Any remaining disjunctions still have only 1 conjunction, but a conjunction with more than 1 prop
			// For any that are not already covered, we pick 1 proposition to add; as a heuristic, always pick
			// whichever proposition has the max occurrences 
			for (int j = 0; j < singleConjList.size(); ++j)
			{
				final DisjunctiveClause disj = singleConjList.get(j);
				pickCoveringPropositions
				(
					disj, pickedPropsList, pickedPropsBitset, 
					singleConjList, j + 1,				// First tie-breaker layer
					disjunctionBins, 2,					// Second tie-breaker layer
					ungeneralisedDisjunctions, i + 1,	// Third tie-breaker layer
					generalisedDisjunctions, 0,			// Fourth tie-breaker layer
					proveIfTrue,
					disproveIfTrue,
					proveIfFalse,
					disproveIfFalse
				);
			}
			
			// Now we need to go through bins of disjunctions that have 2 or more conjunctions each
			for (int j = 2; j < disjunctionBins.size(); ++j)
			{
				final List<DisjunctiveClause> bin = disjunctionBins.get(j);
				for (int k = 0; k < bin.size(); ++k)
				{
					final DisjunctiveClause disj = bin.get(k);
					pickCoveringPropositions
					(
						disj, pickedPropsList, pickedPropsBitset, 
						bin, k + 1,							// First tie-breaker layer
						disjunctionBins, j + 1,				// Second tie-breaker layer
						ungeneralisedDisjunctions, i + 1,	// Third tie-breaker layer
						generalisedDisjunctions, 0,			// Fourth tie-breaker layer
						proveIfTrue,
						disproveIfTrue,
						proveIfFalse,
						disproveIfFalse
					);
				}
			}
			
			// Add all the propositions that we've decided need adding
			for (int j = 0; j < pickedPropsList.size(); ++j)
			{
				final int propID = pickedPropsList.getQuick(j);
				final AtomicProposition prop = propositionNodesList.get(propID).proposition;
				propositions.add(prop);
			}
			
			// Inform all disjunctions about propositions that are now assumed to be true
			for (int j = 0; j < ungeneralisedDisjunctions.size(); ++j)
			{
				final List<DisjunctiveClause> list = ungeneralisedDisjunctions.get(j);
				for (int k = 0; k < list.size(); ++k)
				{
					list.get(k).assumeTrue(pickedPropsBitset);
				}
			}
			for (int j = 0; j < generalisedDisjunctions.size(); ++j)
			{
				final List<DisjunctiveClause> list = generalisedDisjunctions.get(j);
				for (int k = 0; k < list.size(); ++k)
				{
					list.get(k).assumeTrue(pickedPropsBitset);
				}
			}
			
			// Update the Ungeneralised/Generalised split again
			ungenGenWrapper = updateUngeneralisedGeneralised(ungeneralisedDisjunctions, generalisedDisjunctions);
			ungeneralisedDisjunctions = ungenGenWrapper.ungeneralisedDisjunctions;
			generalisedDisjunctions = ungenGenWrapper.generalisedDisjunctions;
		}
		
		// Tell all the sortable feature instances which post-sorting propositions they use
		// We did not yet sort the sortable feature instances, so for now they're in the same
		// order as the filtered feature instance nodes
		for (int j = 0; j < filteredInstanceNodes.size(); ++j)
		{
			final FeatureInstanceNode instanceNode = filteredInstanceNodes.get(j);
			
			for (final PropositionNode propNode : instanceNode.propositions)
			{
				final int newPropID = propositions.indexOf(propNode.proposition);
				if (newPropID >= 0)
					sortableFeatureInstances.get(j).propIDs.set(newPropID);
			}
		}

		// Thresholded features should not auto-activate
		autoActiveFeatures.andNot(thresholdedFeatures);
		
		// Sort our sortable feature instances
		sortableFeatureInstances.sort(null);
		
		// Generate all the final arrays we need
		final FeatureInstance[] sortedFeatureInstances = new FeatureInstance[sortableFeatureInstances.size()];
		final BitSet[] instancesPerProp = new BitSet[propositions.size()];
		final BitSet[] instancesPerFeature = new BitSet[numFeatures];
		final BitSet[] propsPerInstance = new BitSet[sortableFeatureInstances.size()];
		
		for (int j = 0; j < propositions.size(); ++j)
		{
			instancesPerProp[j] = new BitSet();
		}
		
		for (int j = 0; j < sortableFeatureInstances.size(); ++j)
		{
			sortedFeatureInstances[j] = sortableFeatureInstances.get(j).featureInstance;
			final BitSet instanceProps = sortableFeatureInstances.get(j).propIDs;
			propsPerInstance[j] = (BitSet) instanceProps.clone();
			
			for (int k = instanceProps.nextSetBit(0); k >= 0; k = instanceProps.nextSetBit(k + 1))
			{
				instancesPerProp[k].set(j);
			}
			
			final int featureIdx = sortedFeatureInstances[j].feature().spatialFeatureSetIndex();
			if (instancesPerFeature[featureIdx] == null)
				instancesPerFeature[featureIdx] = new BitSet();
			instancesPerFeature[featureIdx].set(j);
		}
		
		final TIntArrayList autoActiveFeaturesList = new TIntArrayList();
		for (int j = autoActiveFeatures.nextSetBit(0); j >= 0; j = autoActiveFeatures.nextSetBit(j + 1))
		{
			autoActiveFeaturesList.add(j);
		}
		
		// Recompute all the proves/disproves if true/false relations, but now for the sorted
		// propositions, and only in the "forwards" direction
		final BitSet[] provesIfTruePerProp = new BitSet[propositions.size()];
		final BitSet[] disprovesIfTruePerProp = new BitSet[propositions.size()];
		final BitSet[] provesIfFalsePerProp = new BitSet[propositions.size()];
		final BitSet[] disprovesIfFalsePerProp = new BitSet[propositions.size()];
		
		for (int j = 0; j < propositions.size(); ++j)
		{
			provesIfTruePerProp[j] = new BitSet();
			disprovesIfTruePerProp[j] = new BitSet();
			provesIfFalsePerProp[j] = new BitSet();
			disprovesIfFalsePerProp[j] = new BitSet();
			
			final AtomicProposition propJ = propositions.get(j);
			
			for (int k = j + 1; k < propositions.size(); ++k)
			{
				final AtomicProposition propK = propositions.get(k);
				
				if (propJ.provesIfTrue(propK, game))
					provesIfTruePerProp[j].set(k);
				else if (propJ.disprovesIfTrue(propK, game))
					disprovesIfTruePerProp[j].set(k);
				
				if (propJ.provesIfFalse(propK, game))
					provesIfFalsePerProp[j].set(k);
				else if (propJ.disprovesIfFalse(propK, game))
					disprovesIfFalsePerProp[j].set(k);
			}
		}
		
		final int[] featureIndices = new int[sortedFeatureInstances.length];
		for (int instanceIdx = 0; instanceIdx < featureIndices.length; ++instanceIdx)
		{
			featureIndices[instanceIdx] = sortedFeatureInstances[instanceIdx].feature().spatialFeatureSetIndex();
		}
		
		return new SPatterNet
				(
					featureIndices, 
					propositions.toArray(new AtomicProposition[propositions.size()]), 
					instancesPerProp, 
					instancesPerFeature, 
					propsPerInstance,
					autoActiveFeaturesList.toArray(),
					thresholdedFeatures,
					provesIfTruePerProp,
					disprovesIfTruePerProp,
					provesIfFalsePerProp,
					disprovesIfFalsePerProp
				);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Returns updated sets of ungeneralised and generalised disjunctions
	 * @param ungeneralisedDisjunctions
	 * @param generalisedDisjunctions
	 * @return Updated sets
	 */
	private static UngeneralisedGeneralisedWrapper updateUngeneralisedGeneralised
	(
		final List<List<DisjunctiveClause>> ungeneralisedDisjunctions,
		final List<List<DisjunctiveClause>> generalisedDisjunctions
	)
	{
		final List<List<DisjunctiveClause>> newUngeneralisedDisjunctions = new ArrayList<List<DisjunctiveClause>>();
		final List<List<DisjunctiveClause>> newGeneralisedDisjunctions = new ArrayList<List<DisjunctiveClause>>();
		
		@SuppressWarnings("unchecked")
		final List<List<DisjunctiveClause>>[] lists = new List[]{ungeneralisedDisjunctions, generalisedDisjunctions};
		
		// Loop through all disjunctions and put them in the correct place
		for (final List<List<DisjunctiveClause>> list : lists)
		{
			for (int i = 0; i < list.size(); ++i)
			{
				final List<DisjunctiveClause> list_i = list.get(i);
				
				// Do next loop in reverse order, more efficient due to removals
				for (int j = list_i.size() - 1; j >= 0; --j)
				{
					// Make sure index is still safe, because we may have multiple removals during search for generalisers
					if (j >= list_i.size())
					{
						j = list_i.size();
						continue;
					}
					
					final DisjunctiveClause disjunction = list_i.get(j);
					if (disjunction.length() > 0)		// Can't discard this entirely
					{
						// Search for a generaliser of this disjunction
						final boolean hasGeneraliser = searchGeneraliser(disjunction, ungeneralisedDisjunctions, generalisedDisjunctions);
						
						// Insert in correct position based on whether or not we are generalised
						final int numAssumedTrue = disjunction.numAssumedTrue();
						if (hasGeneraliser)
						{
							// Make sure list we want to go to actually exists
							while (newGeneralisedDisjunctions.size() <= numAssumedTrue)
							{
								newGeneralisedDisjunctions.add(new ArrayList<DisjunctiveClause>());
							}
								
							newGeneralisedDisjunctions.get(numAssumedTrue).add(disjunction);
						}
						else
						{
							// Make sure list we want to go to actually exists
							while (newUngeneralisedDisjunctions.size() <= numAssumedTrue)
							{
								newUngeneralisedDisjunctions.add(new ArrayList<DisjunctiveClause>());
							}
								
							newUngeneralisedDisjunctions.get(numAssumedTrue).add(disjunction);
						}
					}
				}
			}
		}
		
		return new UngeneralisedGeneralisedWrapper(newUngeneralisedDisjunctions, newGeneralisedDisjunctions);
	}
	
	/**
	 * Searches for a disjunctive clause, among all the given clauses, that generalises the given clause.
	 * 
	 * NOTE: this method may also decide to remove various duplicates as it detects them.
	 * 
	 * @param clause
	 * @param ungeneralisedDisjunctions
	 * @param generalisedDisjunctions
	 * @return True if we are generalised (by a non-equal clause)
	 */
	private static boolean searchGeneraliser
	(
		final DisjunctiveClause clause,
		final List<List<DisjunctiveClause>> ungeneralisedDisjunctions,
		final List<List<DisjunctiveClause>> generalisedDisjunctions
	)
	{
		@SuppressWarnings("unchecked")
		final List<List<DisjunctiveClause>>[] lists = new List[]{ungeneralisedDisjunctions, generalisedDisjunctions};
		for (int i = 0; i < lists.length; ++i)
		{
			final List<List<DisjunctiveClause>> list_i = lists[i];
			for (int j = 0; j < list_i.size(); ++j)
			{
				final List<DisjunctiveClause> list_j = list_i.get(j);
				for (int k = 0; k < list_j.size(); /**/)
				{
					final DisjunctiveClause other = list_j.get(k);
					
					if (other.generalises(clause))
					{
						if (clause.generalises(other))
						{
							// Generalise both ways around, so we're equal!
							if (clause.numAssumedTrue() > other.numAssumedTrue())
							{
								// "steal" the num assumed true, such that we can keep clause
								clause.setNumAssumedTrue(other.numAssumedTrue());
							}
							
							// Remove other, and continue loop without incrementing k
							list_j.remove(k);
							continue;
						}
						else
						{
							// Found a generaliser
							//System.out.println(other + " generalises " + clause);
							return true;
						}
					}
					
					// Need to increment index
					++k;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Picks a covering proposition from the given disjunction
	 * 
	 * @param disjunction
	 * @param pickedPropsList
	 * @param pickedPropsBitset
	 * @param firstTiebreakerList
	 * @param firstTiebreakerIndex
	 * @param secondTiebreakerLists
	 * @param secondTiebreakerIndex
	 * @param thirdTiebreakerLists
	 * @param thirdTiebreakerIndex
	 * @param fourthTiebreakerLists
	 * @param fourthTiebreakerIndex
	 * @param proveIfTrue
	 * @param disproveIfTrue
	 * @param proveIfFalse
	 * @param disproveIfFalse
	 */
	private void pickCoveringPropositions
	(
		final DisjunctiveClause disjunction,
		final TIntArrayList pickedPropsList,
		final BitSet pickedPropsBitset,
		final List<DisjunctiveClause> firstTiebreakerList,
		final int firstTiebreakerIndex,
		final List<List<DisjunctiveClause>> secondTiebreakerLists,
		final int secondTiebreakerIndex,
		final List<List<DisjunctiveClause>> thirdTiebreakerLists,
		final int thirdTiebreakerIndex,
		final List<List<DisjunctiveClause>> fourthTiebreakerLists,
		final int fourthTiebreakerIndex,
		final BitSet[] proveIfTrue,
		final BitSet[] disproveIfTrue,
		final BitSet[] proveIfFalse,
		final BitSet[] disproveIfFalse
	)
	{
		final BitSet candidateProps = (BitSet) disjunction.usedPropositions().clone();
		if (candidateProps.intersects(pickedPropsBitset))
			return;		// Already covered
		
		TIntArrayList maxIndices = maxScorePropIndices(
				candidateProps, disjunction, firstTiebreakerList, firstTiebreakerIndex, pickedPropsBitset, proveIfTrue, 
				disproveIfTrue, proveIfFalse, disproveIfFalse);
		if (maxIndices.size() == 1)
		{
			// No more tie-breakers, pick the max
			pickProp(maxIndices.getQuick(0), pickedPropsList, pickedPropsBitset);
			return;
		}
		
		// Need to continue to second tie-breaker
		// But first, eliminate non-max candidates
		candidateProps.clear();
		for (int i = 0; i < maxIndices.size(); ++i)
		{
			candidateProps.set(maxIndices.getQuick(i));
		}
		
		// Same story as before, but with next list of clauses for tie-breaking
		for (int i = secondTiebreakerIndex; i < secondTiebreakerLists.size(); ++i)
		{
			maxIndices = maxScorePropIndices(
					candidateProps, null, secondTiebreakerLists.get(i), 0, pickedPropsBitset, 
					proveIfTrue, disproveIfTrue, proveIfFalse, disproveIfFalse);
			if (maxIndices.size() == 1)
			{
				// No more tie-breakers, pick the max
				pickProp(maxIndices.getQuick(0), pickedPropsList, pickedPropsBitset);
				return;
			}
			
			// Eliminate non-max candidates and move on to next tie-breaker
			candidateProps.clear();
			for (int j = 0; j < maxIndices.size(); ++j)
			{
				candidateProps.set(maxIndices.getQuick(j));
			}
		}
		
		// Same story as before, but with next list of clauses for tie-breaking
		for (int i = thirdTiebreakerIndex; i < thirdTiebreakerLists.size(); ++i)
		{
			maxIndices = maxScorePropIndices(
					candidateProps, null, thirdTiebreakerLists.get(i), 0, pickedPropsBitset, 
					proveIfTrue, disproveIfTrue, proveIfFalse, disproveIfFalse);
			if (maxIndices.size() == 1)
			{
				// No more tie-breakers, pick the max
				pickProp(maxIndices.getQuick(0), pickedPropsList, pickedPropsBitset);
				return;
			}
			
			// Eliminate non-max candidates and move on to next tie-breaker
			candidateProps.clear();
			for (int j = 0; j < maxIndices.size(); ++j)
			{
				candidateProps.set(maxIndices.getQuick(j));
			}
		}
		
		// Same story as before, but with next list of clauses for tie-breaking
		for (int i = fourthTiebreakerIndex; i < fourthTiebreakerLists.size(); ++i)
		{
			maxIndices = maxScorePropIndices(
					candidateProps, null, fourthTiebreakerLists.get(i), 0, pickedPropsBitset, 
					proveIfTrue, disproveIfTrue, proveIfFalse, disproveIfFalse);
			if (maxIndices.size() == 1)
			{
				// No more tie-breakers, pick the max
				pickProp(maxIndices.getQuick(0), pickedPropsList, pickedPropsBitset);
				return;
			}
			
			// Eliminate non-max candidates and move on to next tie-breaker
			candidateProps.clear();
			for (int j = 0; j < maxIndices.size(); ++j)
			{
				candidateProps.set(maxIndices.getQuick(j));
			}
		}
		
		// No more tie-breakers, so let's just randomly pick one max index
		pickProp(maxIndices.getQuick(ThreadLocalRandom.current().nextInt(maxIndices.size())), pickedPropsList, pickedPropsBitset);
	}
	
	/**
	 * Helper method to pick a proposition of a given ID: may additionally first
	 * pick proposition with empty tests to prioritise them before other types of tests.
	 * 
	 * @param propID
	 * @param pickedPropsList
	 * @param pickedPropsBitset
	 */
	private void pickProp(final int propID, final TIntArrayList pickedPropsList, final BitSet pickedPropsBitset)
	{
		pickedPropsBitset.set(propID);
		final AtomicProposition pickedProp = propositionNodesList.get(propID).proposition;
		
		if (pickedProp.stateVectorType() != StateVectorTypes.Empty)
		{
			// See if we have any empty tests for the same site that are not yet picked; if so, prioritise them
			final int site = pickedProp.testedSite();
			
			for (int i = pickedPropsBitset.nextClearBit(0); i < propositionNodesList.size(); i = pickedPropsBitset.nextClearBit(i + 1))
			{
				final AtomicProposition unpickedProp = propositionNodesList.get(i).proposition;
				
				if (unpickedProp.testedSite() == site && unpickedProp.stateVectorType() == StateVectorTypes.Empty)
				{
					// We want to pick this prop first
					pickedPropsBitset.set(i);
					pickedPropsList.add(i);
				}
			}
		}
		
		pickedPropsList.add(propID);
	}
	
	/**
	 * @param candidateProps Candidate props
	 * @param disjunction The disjunction from which we obtained candidate props (or null for tie-breaking)
	 * @param clauses List of clauses from which to compute scores
	 * @param startIdx Index in list to start at
	 * @param coveredProps Propositions that are already covered
	 * @param proveIfTrue
	 * @param disproveIfTrue
	 * @param proveIfFalse
	 * @param disproveIfFalse
	 * @return List of indices of all candidate props that obtain the max score
	 */
	private TIntArrayList maxScorePropIndices
	(
		final BitSet candidateProps, 
		final DisjunctiveClause disjunction,
		final List<DisjunctiveClause> clauses, 
		final int startIdx,
		final BitSet coveredProps,
		final BitSet[] proveIfTrue,
		final BitSet[] disproveIfTrue,
		final BitSet[] proveIfFalse,
		final BitSet[] disproveIfFalse
	)
	{
		final double[] propScores = new double[propositionNodesList.size()];
		
		if (disjunction != null)
		{
			for (int candidateProp = candidateProps.nextSetBit(0); candidateProp >= 0; candidateProp = candidateProps.nextSetBit(candidateProp + 1))
			{
				propScores[candidateProp] += propScoreForDisjunction(disjunction, candidateProp, proveIfTrue, disproveIfTrue, proveIfFalse, disproveIfFalse);
			}
		}
		
		for (int i = startIdx; i < clauses.size(); ++i)
		{
			final DisjunctiveClause dis = clauses.get(i);
			final BitSet disProps = dis.usedPropositions();
			if (disProps.intersects(coveredProps))
				continue;	// Already covered, so skip it
			
			for (int candidateProp = candidateProps.nextSetBit(0); candidateProp >= 0; candidateProp = candidateProps.nextSetBit(candidateProp + 1))
			{
				if (disProps.get(candidateProp))
				{
					// This candidate proposition shows up in this disjunction, so add its score
					propScores[candidateProp] += propScoreForDisjunction(dis, candidateProp, proveIfTrue, disproveIfTrue, proveIfFalse, disproveIfFalse);
				}
			}
		}
		
		final TIntArrayList maxIndices = new TIntArrayList();
		double maxScore = -1.0;
		
		for (int candidateProp = candidateProps.nextSetBit(0); candidateProp >= 0; candidateProp = candidateProps.nextSetBit(candidateProp + 1))
		{
			//System.out.println("Prop = " + this.propositionNodesList.get(candidateProp).proposition);
			//System.out.println("Score = " + propScores[candidateProp]);
			if (propScores[candidateProp] > maxScore)
			{
				maxIndices.clear();
				maxIndices.add(candidateProp);
				maxScore = propScores[candidateProp];
			}
			else if (propScores[candidateProp] == maxScore)
			{
				maxIndices.add(candidateProp);
			}
		}
		//System.out.println();
		
		return maxIndices;
	}
	
	/**
	 * Implements the Jeroslow-Wang method for SAT (+ extra terms based on inferences between props)
	 * 
	 * @param disjunction
	 * @param prop
	 * @param proveIfTrue
	 * @param disproveIfTrue
	 * @param proveIfFalse
	 * @param disproveIfFalse
	 * @return Heuristic score for proposition in given disjunction
	 */
	private static double propScoreForDisjunction
	(
		final DisjunctiveClause disjunction, 
		final int prop,
		final BitSet[] proveIfTrue,
		final BitSet[] disproveIfTrue,
		final BitSet[] proveIfFalse,
		final BitSet[] disproveIfFalse
	)
	{
		double score = 0.0;
		
		for (final Conjunction conj : disjunction.conjunctions())
		{
			final BitSet conjProps = conj.toProve();
			
			if (conjProps.get(prop))
			{
				score += Math.pow(2.0, -conj.length());
			}
			else
			{
				final BitSet ifTrueOverlap = (BitSet) proveIfTrue[prop].clone();
				ifTrueOverlap.or(disproveIfTrue[prop]);
				ifTrueOverlap.and(conjProps);

				final BitSet ifFalseOverlap = (BitSet) proveIfFalse[prop].clone();
				ifFalseOverlap.or(disproveIfFalse[prop]);
				ifFalseOverlap.and(conjProps);
				
				score += (0.5 * ifTrueOverlap.cardinality() + 0.5 * ifFalseOverlap.cardinality()) * Math.pow(2.0, -conj.length());
			}
		}
		
		return score;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @return Computes array of conjunctive clauses, each represented as a bitset
	 * 	with bits on for propositions included in clause; one clause per instance node.
	 */
	private BitSet[] computeConjunctiveClauses()
	{
		final BitSet[] conjunctiveClauses = new BitSet[instanceNodes.size()];
		
		// For every feature instance, compute its conjunctive clause as a BitSet
		for (int i = 0; i < instanceNodes.size(); ++i)
		{
			final FeatureInstanceNode instanceNode = instanceNodes.get(i);
			final BitSet propIDs = new BitSet(this.propositionNodes.size());
			
			for (final PropositionNode propNode : instanceNode.propositions)
			{
				propIDs.set(propNode.id);
			}
			
			conjunctiveClauses[i] = propIDs;
		}
		
		// Convert to array
		return conjunctiveClauses;
	}
	
	/**
	 * @param lists
	 * @return Index of first non-empty list in given list of lists, or -1 if all empty
	 */
	private static int firstNonEmptyListIndex(final List<List<DisjunctiveClause>> lists)
	{
		for (int i = 0; i < lists.size(); ++i)
		{
			if (!lists.get(i).isEmpty())
				return i;
		}
		
		return -1;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param bitset
	 * @return A nice string representation of the propositions described by given bitset.
	 */
	private String toPropsString(final BitSet bitset)
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("{");
		
		for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1))
		{
			if (sb.length() > 1)
				sb.append(", ");
			
			sb.append(this.propositionNodesList.get(i).proposition.toString());
		}
		
		sb.append("}");
		return sb.toString();
	}
	
	/**
	 * @param bitset
	 * @return A nice string representation of the propositions used by a disjunction.
	 */
	@SuppressWarnings("unused")		// Do NOT remove SuppressWarning: sometimes use this for debugging!
	private String toPropsString(final DisjunctiveClause disjunction)
	{
		final StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < disjunction.conjunctions().size(); ++i)
		{
			sb.append("(");
			sb.append(toPropsString(disjunction.conjunctions().get(i).toProve()));
			sb.append(")");
			
			if (i < disjunction.conjunctions().size() - 1)
				sb.append(" OR ");
		}
		
		return sb.toString();
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Node for an atomic proposition
	 *
	 * @author Dennis Soemers
	 */
	public static class PropositionNode
	{
		/** Unique ID for this node */
		protected final int id;
		/** The atomic proposition */
		protected final AtomicProposition proposition;
		/** Nodes for feature instances that use this proposition */
		protected final List<FeatureInstanceNode> instances = new ArrayList<FeatureInstanceNode>();
		
		/**
		 * Constructor
		 * @param id
		 * @param proposition
		 */
		public PropositionNode(final int id, final AtomicProposition proposition)
		{
			this.id = id;
			this.proposition = proposition;
		}
		
		@Override
		public String toString()
		{
			return "[PropNode " + id + ": " + proposition + "]";
		}
	}
	
	/**
	 * Node for a feature instance
	 *
	 * @author Dennis Soemers
	 */
	protected static class FeatureInstanceNode
	{
		/** Unique ID for this node */
		protected final int id;
		/** The feature instance */
		protected final FeatureInstance instance;
		/** Nodes for propositions used by this feature */
		protected final List<PropositionNode> propositions = new ArrayList<PropositionNode>();
		
		/**
		 * Constructor
		 * @param id
		 * @param instance
		 */
		public FeatureInstanceNode(final int id, final FeatureInstance instance)
		{
			this.id = id;
			this.instance = instance;
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Small wrapper around a split of disjunctive clauses into ungeneralised
	 * and generalised (such that we can return them together from a single method)
	 *
	 * @author Dennis Soemers
	 */
	private static class UngeneralisedGeneralisedWrapper
	{
		public final List<List<DisjunctiveClause>> ungeneralisedDisjunctions;
		public final List<List<DisjunctiveClause>> generalisedDisjunctions;
		
		/**
		 * Constructor
		 * @param ungeneralisedDisjunctions
		 * @param generalisedDisjunctions
		 */
		public UngeneralisedGeneralisedWrapper
		(
			final List<List<DisjunctiveClause>> ungeneralisedDisjunctions,
			final List<List<DisjunctiveClause>> generalisedDisjunctions
		)
		{
			this.ungeneralisedDisjunctions = ungeneralisedDisjunctions;
			this.generalisedDisjunctions = generalisedDisjunctions;
		}
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * A wrapper around a feature instance with some data about propositions
	 * that it requires, and the ability to sort a list of wrappers based
	 * on that data.
	 *
	 * @author Dennis Soemers
	 */
	private static class SortableFeatureInstance implements Comparable<SortableFeatureInstance>
	{
		/** The feature instance */
		public final FeatureInstance featureInstance;
		
		/** IDs of propositions (after sorting) that are required by this instance */
		public final BitSet propIDs = new BitSet();
		
		/**
		 * Constructor
		 * @param featureInstance
		 */
		public SortableFeatureInstance(final FeatureInstance featureInstance)
		{
			this.featureInstance = featureInstance;
		}

		@Override
		public int compareTo(final SortableFeatureInstance o)
		{
			int myRightmost = propIDs.length() - 1;
			int otherRightmost = o.propIDs.length() - 1;
			
			while (myRightmost == otherRightmost && myRightmost >= 0)
			{
				myRightmost = propIDs.previousSetBit(myRightmost - 1);
				otherRightmost = o.propIDs.previousSetBit(otherRightmost - 1);
			}
			
			if (myRightmost > otherRightmost)
			{
				// We should go after other
				return 1;
			}
			else if (myRightmost < otherRightmost)
			{
				// We should go before other
				return -1;
			}
			
			return 0;
		}
	}
	
	//-------------------------------------------------------------------------

}
