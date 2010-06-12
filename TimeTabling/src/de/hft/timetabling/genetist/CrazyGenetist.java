package de.hft.timetabling.genetist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.main.Main;
import de.hft.timetabling.services.ICrazyGenetistService;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.IValidatorService;
import de.hft.timetabling.services.ServiceLocator;

/**
 * The CrazyGenetist is responsible for recombining and mutating solutions.
 * 
 * @author Steffen
 * @author Sotiris
 * 
 */
public class CrazyGenetist implements ICrazyGenetistService {

	@Override
	public int getRecombinationPercentage() {
		return RECOMBINATION_STRATEGY.getRecombinationPercentage();
	}

	/**
	 * public Method to start recombination and mutation process. The solution
	 * table will get from serviceLocator.getSolutionTableService(). The
	 * Solutions that are recombined and mutated are chosen randomly.
	 */
	@Override
	public void recombineAndMutate() {
		ISolutionTableService solutionTable = ServiceLocator.getInstance()
				.getSolutionTableService();

		if (solutionTable.getSize(false) <= 2) {
			return;
		}

		// The solutions ordered by rank (low to high, highest rank is best)
		List<ISolution> rankedSolutions = new ArrayList<ISolution>(
				solutionTable.getSize(false));
		for (int i = solutionTable.getSize(false) - 1; i >= 0; i--) {
			rankedSolutions.add(solutionTable.getSolution(i));
		}

		int nrRecombinations = (getRecombinationPercentage() * solutionTable
				.getSize(false)) / 100;
		for (int i = 0; i < nrRecombinations; i++) {

			int rankingSystemSlotSum = computeRankingSystemSlotSum(rankedSolutions);
			ISolution firstParentSolution = null;
			ISolution secondParentSolution = null;

			while (((firstParentSolution == null)
					|| (secondParentSolution == null) || firstParentSolution
					.equals(secondParentSolution))
					&& (rankedSolutions.size() > 2)) {

				Random random = new Random();
				int selectedSlot1 = random.nextInt(rankingSystemSlotSum) + 1;
				int rank1 = slotToRank(selectedSlot1, rankedSolutions.size(),
						rankingSystemSlotSum);
				firstParentSolution = rankedSolutions.get(rank1 - 1);
				int selectedSlot2 = random.nextInt(rankingSystemSlotSum) + 1;
				int rank2 = slotToRank(selectedSlot2, rankedSolutions.size(),
						rankingSystemSlotSum);
				secondParentSolution = rankedSolutions.get(rank2 - 1);
			}

			// Not enough solutions left due to elimination.
			if ((firstParentSolution == null) || (secondParentSolution == null)) {
				break;
			}

			// Recombination
			RECOMBINATION_STRATEGY.reset();
			ISolution recombinedSolution = RECOMBINATION_STRATEGY.recombine(
					firstParentSolution, secondParentSolution);
			if (recombinedSolution == null) {
				Main.mutateRecombineFailure++;
				continue;
			}

			// Mutation
			recombinedSolution = RECOMBINATION_STRATEGY
					.mutate(recombinedSolution);

			// Hand in solution if valid
			IValidatorService validatorService = ServiceLocator.getInstance()
					.getValidatorService();
			if (validatorService.isValidSolution(recombinedSolution)) {
				Main.mutateRecombineSuccess++;
				firstParentSolution.increaseRecombinationCount();
				secondParentSolution.increaseRecombinationCount();
				Set<ISolution> eliminatedSolutions = new HashSet<ISolution>();
				RECOMBINATION_STRATEGY.eliminate(firstParentSolution,
						secondParentSolution, eliminatedSolutions);
				for (ISolution eliminatedSolution : eliminatedSolutions) {
					rankedSolutions.remove(eliminatedSolution);
				}
				solutionTable.addSolution(recombinedSolution);
			} else {
				Main.mutateRecombineFailure++;
			}
		}
	}

	private int computeRankingSystemSlotSum(List<ISolution> rankedSolutions) {
		int slotSum = 0;
		for (int i = 1; i <= rankedSolutions.size(); i++) {
			slotSum += i;
		}
		return slotSum;
	}

	/**
	 * Returns the rank that the given slot is assigned to.
	 * 
	 * @param slot
	 *            A rank consists of as many slots as the rank number is. For
	 *            example, rank 100 has 100 slots.
	 * @param nrSolutions
	 *            The number of solutions in total.
	 * @param slotSum
	 *            The sum of all slots.
	 */
	private int slotToRank(int slot, int nrSolutions, int slotSum) {
		int rank = 1;
		int currentSlotSum = 1;
		while (rank <= nrSolutions) {
			if (slot <= currentSlotSum) {
				break;
			}
			rank++;
			currentSlotSum += rank + 1;
		}
		return rank;
	}

}
