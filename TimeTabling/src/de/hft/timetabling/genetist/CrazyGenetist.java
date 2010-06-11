package de.hft.timetabling.genetist;

import java.util.Random;
import java.util.Set;

import de.hft.timetabling.common.ICourse;
import de.hft.timetabling.common.ICurriculum;
import de.hft.timetabling.common.IProblemInstance;
import de.hft.timetabling.common.ISolution;
import de.hft.timetabling.main.Main;
import de.hft.timetabling.services.ICrazyGenetistService;
import de.hft.timetabling.services.ISolutionTableService;
import de.hft.timetabling.services.IValidatorService;
import de.hft.timetabling.services.ServiceLocator;

/**
 * The CrazyGenetist class is for recombinating and mutating a solution table.
 * 
 * @author Steffen
 * @author Sotiris
 * 
 */
public class CrazyGenetist implements ICrazyGenetistService {

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

		// Choosing solutions that until they are not null and different
		int iterationRounds = (PERCENTAGE * ISolutionTableService.TABLE_SIZE) / 100;

		System.out.print("CRAZY GENETIST: Starting to create "
				+ iterationRounds + " children (" + PERCENTAGE + "%) ...");

		// The solutions ordered by rank (low to high, highest rank is best)
		ISolution[] rankedSolutions = new ISolution[solutionTable
				.getSize(false)];
		int solutionIndex = 0;
		for (int i = rankedSolutions.length - 1; i >= 0; i--) {
			rankedSolutions[i] = solutionTable.getSolution(solutionIndex);
			solutionIndex++;
		}

		// Compute slot sum
		int slotSum = 0;
		for (int i = 1; i <= rankedSolutions.length; i++) {
			slotSum += i;
		}

		for (int i = 0; i < iterationRounds; i++) {
			// Solution that is used to pull values out of.
			ISolution otherSolution = null;
			// Solution that is used as a basis for recombination
			ISolution basisSolution = null;

			while ((otherSolution == null) || (basisSolution == null)
					|| basisSolution.equals(otherSolution)) {

				Random random = new Random();
				int selectedSlot1 = random.nextInt(slotSum) + 1;
				int selectedSlot2 = random.nextInt(slotSum) + 1;
				int rank1 = slotToRank(selectedSlot1, solutionTable
						.getSize(false), slotSum);
				int rank2 = slotToRank(selectedSlot2, solutionTable
						.getSize(false), slotSum);
				basisSolution = rankedSolutions[rank1 - 1];
				otherSolution = rankedSolutions[rank2 - 1];
				IValidatorService validatorService = ServiceLocator
						.getInstance().getValidatorService();
				validatorService.isValidSolution(basisSolution);
				validatorService.isValidSolution(otherSolution);
			}

			// Recombination
			RECOMBINATION_STRATEGY.reset();
			ISolution recombinedSolution = RECOMBINATION_STRATEGY.recombine(
					basisSolution, otherSolution);
			if (recombinedSolution == null) {
				System.out.println("CRAZY GENETIST: Failure (recombination).");
				Main.mutateRecombineFailure++;
				continue;
			}

			// Mutation
			if (Math.random() <= 0.15) {
				recombinedSolution = mutateRoomStability(recombinedSolution);
			}
			// if (Math.random() <= 0.05) {
			// recombinedSolution = mutateCourseIsolation(recombinedSolution);
			// }

			// Hand in solution if valid
			IValidatorService validatorService = ServiceLocator.getInstance()
					.getValidatorService();
			if (validatorService.isValidSolution(recombinedSolution)) {
				Main.mutateRecombineSuccess++;
				basisSolution.increaseRecombinationCount();
				otherSolution.increaseRecombinationCount();
				solutionTable.removeWorstSolution();
				solutionTable.addSolution(recombinedSolution);
			} else {
				System.out.println("CRAZY GENETIST: Failure (validation).");
				Main.mutateRecombineFailure++;
			}
		}

		System.out.print(" done.\n");
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

	/**
	 * Mutation algorithm to mutate course stability.
	 * 
	 * @param solution
	 *            solution to mutate
	 * @return mutated solution
	 */
	private ISolution mutateCourseIsolation(ISolution solution) {
		ISolution newSolution = solution.clone();

		int n1 = 0, n2 = 0;
		while (n1 == n2) {
			n1 = (int) (newSolution.getCoding().length * Math.random());
			n2 = (int) (newSolution.getCoding().length * Math.random());
		}

		ICourse[] temp = newSolution.getCoding()[n1];
		newSolution.getCoding()[n1] = newSolution.getCoding()[n2];
		newSolution.getCoding()[n2] = temp;

		return newSolution;
	}

	/**
	 * Mutation algorithm.
	 * 
	 * @param solution
	 *            that should be mutated.
	 * @return mutated solution
	 */
	private ISolution mutateRoomStability(ISolution solution) {
		IProblemInstance pi = solution.getProblemInstance();
		ICourse[][] courses = solution.getCoding();
		int roomY = 0, periodX = 0;
		ICurriculum myCurriculum = null;

		while (myCurriculum == null) {
			roomY = (int) (pi.getRooms().size() * Math.random());
			periodX = (int) (pi.getNumberOfPeriods() * Math.random());
			// System.out.println("... trying to find curriculum [" + periodX+
			// "][" + roomY + "] ...");
			if (courses[periodX][roomY] != null) {
				Set<ICurriculum> cur = courses[periodX][roomY].getCurricula();
				int random = (int) (cur.size() * Math.random());
				ICurriculum curriculum = cur
						.toArray(new ICurriculum[cur.size()])[random];
				myCurriculum = getCurriculumOutOfSet(cur, curriculum.getId());
			}
		}

		for (int i = 0; i < courses.length; i++) {
			if (i != periodX) {
				for (int j = 0; j < courses[i].length; j++) {
					ICourse selectedCourse = courses[i][j];
					if (selectedCourse != null) {
						Set<ICurriculum> tmpCur = selectedCourse.getCurricula();

						for (ICurriculum iCurriculum : tmpCur) {
							if (iCurriculum.getId()
									.equals(myCurriculum.getId())) {
								courses[i][j] = courses[i][roomY];
								courses[i][roomY] = selectedCourse;
								// No check neccessary because we only changed
								// the room --> soft constraint!?
							}
						}
					}
				}
			}
		}

		ISolutionTableService solutionTable = ServiceLocator.getInstance()
				.getSolutionTableService();
		ISolution newSolution = solutionTable.createNewSolution(courses,
				solution.getProblemInstance());
		newSolution.setRecombinationCount(solution.getRecombinationCount() + 1);
		return newSolution;
	}

	/**
	 * Method to get a ICurriculum out of a Set<ICurriculum> chosen by the ID of
	 * a ICurriculum
	 * 
	 * @param set
	 *            Set<ICurriculum>
	 * @param searchedOneId
	 *            ID of ICurriculum that should be searched for
	 * @return the found item
	 */
	public ICurriculum getCurriculumOutOfSet(Set<ICurriculum> set,
			String searchedOneId) {
		for (ICurriculum iCurriculum : set) {
			if (iCurriculum.getId().equals(searchedOneId)) {
				return iCurriculum;
			}
		}
		return null;
	}

}
